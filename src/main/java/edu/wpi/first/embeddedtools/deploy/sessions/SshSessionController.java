package edu.wpi.first.embeddedtools.deploy.sessions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import org.codehaus.groovy.runtime.IOGroovyMethods;

import edu.wpi.first.embeddedtools.deploy.CommandDeployResult;

public class SshSessionController extends AbstractSessionController implements IPSessionController {

    private Session session;
    private String host, user;
    private int port, timeout;

    static JSch jsch;

    static JSch getJsch() {
        if (jsch == null)
            jsch = new JSch();
        return jsch;
    }

    public SshSessionController(String host, int port, String user, String password, int timeout, int maxConcurrent) {
        super(maxConcurrent);
        this.host = host;
        this.port = port;
        this.user = user;
        this.timeout = timeout;

        // TODO MAKE THIS USE COPY in EmbeddedTools
        try {
            this.session = jsch.getSession(user, host, port);
        } catch (JSchException e) {
            throw new RuntimeException(e);
        }
        this.session.setPassword(password);

        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        config.put("PreferredAuthentications", "password");
        this.session.setConfig(config);
    }

    @Override
    public void open() {
        getLogger().info("Connecting to session (timeout=" + timeout + ")");
        try {
            session.setTimeout(timeout * 1000);
            session.connect(timeout * 1000);
        } catch (JSchException e) {
            throw new RuntimeException(e);
        }

        getLogger().info("Connected!");
    }

    public CommandDeployResult execute(String command) {
        int sem = acquire();

        ChannelExec exec;
        try {
            exec = (ChannelExec) session.openChannel("exec");
            exec.setCommand(command);
            exec.setPty(false);
            exec.setAgentForwarding(false);

            InputStream is = exec.getInputStream();
            exec.connect();
            exec.run();
            try {
                return new CommandDeployResult(command, IOGroovyMethods.getText(is), exec.getExitStatus());
            } finally {
                exec.disconnect();
                release(sem);
            }
        } catch (JSchException | IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void put(Map<String, File> files) {
        int sem = acquire();

        ChannelSftp sftp;
        try {
            sftp = (ChannelSftp) session.openChannel("sftp");
            sftp.connect();
            try {
                for (Map.Entry<String, File> file : files.entrySet()) {
                    sftp.put(file.getValue().getAbsolutePath(), file.getKey());
                }
            } finally {
                sftp.disconnect();
                release(sem);
            }
        } catch (JSchException | SftpException e2) {
            throw new RuntimeException(e2);
        }

    }

    @Override
    public void finalize() {
        try {
            session.disconnect();
        } catch (Exception e) { }
    }

    @Override
    public void close() throws IOException {
        try {
            session.disconnect();
        } catch (Exception e) { }
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
}
