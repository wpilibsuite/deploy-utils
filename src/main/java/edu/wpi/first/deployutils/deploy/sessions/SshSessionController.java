package edu.wpi.first.deployutils.deploy.sessions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.util.io.output.NullOutputStream;
import org.apache.sshd.sftp.client.SftpClient;
import org.apache.sshd.sftp.client.SftpClientFactory;
import org.gradle.api.Action;

import edu.wpi.first.deployutils.deploy.CommandDeployResult;
import edu.wpi.first.deployutils.deploy.StorageService;
import edu.wpi.first.deployutils.deploy.target.location.SshDeployLocation;

public class SshSessionController extends AbstractSessionController implements IPSessionController {

    private ClientSession session;
    private String host, user;
    private int port;

    public SshSessionController(String host, int port, SshDeployLocation location) throws IOException {
        super(location.getTarget().getMaxChannels(), location.getTarget().getStorageServiceProvider().get());
        this.host = host;
        this.port = port;
        this.user = location.getUser();
        String password = location.getPassword();
        int timeout = location.getTarget().getTimeout();
        StorageService storage = location.getTarget().getStorageServiceProvider().get();

        getLogger().info("Connecting to session (timeout=" + timeout + ")");

        ClientSession localSession = null;
        try {
            localSession = storage.getSshClient().connect(user, host, port).verify(timeout * 1000).getSession();
            Optional<Action<ClientSession>> sshConfig = location.getClientSessionConfiguration();
            if (sshConfig.isPresent()) {
                sshConfig.get().execute(localSession);
            } else {
                localSession.setServerKeyVerifier(new AcceptAllLoggedServerKeyVerifier(getLogger()));
                if (password != null && !password.isBlank()) {
                    localSession.addPasswordIdentity(password);
                }
            }
            localSession.auth().verify(timeout * 1000);
            this.session = localSession;
            localSession = null;
        } finally {
            if (localSession != null) {
                localSession.close();
            }
        }

        getLogger().info("Connected!");
    }

    private CommandDeployResult executeInternal(String command) throws IOException {
        int sem = acquire();

        try (OutputStream channelErr = new NullOutputStream();
            ByteArrayOutputStream channelOut = new ByteArrayOutputStream();
            ClientChannel channel = session.createExecChannel(command)) {
            channel.setOut(channelOut);
            channel.setErr(channelErr);
            channel.open().await();

            Collection<ClientChannelEvent> waitMask = channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), 0L);
            if (waitMask.contains(ClientChannelEvent.TIMEOUT)) {
                return new CommandDeployResult(command, "Failed to retrieve command result in time", -1);
            }

            Integer exitStatus = channel.getExitStatus();
            if (exitStatus == null) {
                return new CommandDeployResult(command, "No exit status received", -1);
            }
            byte[] outBytes = channelOut.toByteArray();
            return new CommandDeployResult(command, new String(outBytes, StandardCharsets.UTF_8), exitStatus.intValue());
        } finally {
            release(sem);
        }
    }

    private void putInternal(Map<String, File> files) throws IOException {
        int sem = acquire();

        try (SftpClient sftp = SftpClientFactory.instance().createSftpClient(session)) {
            for (Map.Entry<String, File> file : files.entrySet()) {
                try (var remoteFile = sftp.write(file.getKey())) {
                    Files.copy(file.getValue().toPath(), remoteFile);
                }
            }
        } finally {
            release(sem);
        }
    }

    private void deleteInternal(List<String> files) throws IOException {
        int sem = acquire();

        try (SftpClient sftp = SftpClientFactory.instance().createSftpClient(session)) {
            for (String file : files) {
                sftp.remove(file);
            }
        } finally {
            release(sem);
        }
    }

    @Override
    public void close() throws IOException {
        session.close();
    }

    @Override
    public String friendlyString() {
        return user + "@" + host + ":" + port;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[" + friendlyString() + "]";
    }

    @Override
    public String getHost() {
        return this.host;
    }

    @Override
    public int getPort() {
        return this.port;
    }

    private void putInternal(InputStream source, String dest) throws IOException {
        int sem = acquire();

        try (SftpClient sftp = SftpClientFactory.instance().createSftpClient(session)) {
            try (var remoteFile = sftp.write(dest)) {
                source.transferTo(remoteFile);
            }
        } finally {
            release(sem);
        }
    }

    @Override
    public CommandDeployResult execute(String command) {
        try {
            return executeInternal(command);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void put(Map<String, File> files) {
        try {
            putInternal(files);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void put(InputStream source, String dest) {
        try {
            putInternal(source, dest);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(List<String> files) {
        if (files.isEmpty()) {
            return;
        }
        try {
            deleteInternal(files);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
