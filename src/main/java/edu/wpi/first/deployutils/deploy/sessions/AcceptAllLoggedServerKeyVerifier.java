package edu.wpi.first.deployutils.deploy.sessions;

import java.net.SocketAddress;
import java.security.PublicKey;

import org.apache.sshd.client.keyverifier.ServerKeyVerifier;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.gradle.api.logging.Logger;

public class AcceptAllLoggedServerKeyVerifier implements ServerKeyVerifier {
    private final Logger logger;

    public AcceptAllLoggedServerKeyVerifier(Logger logger) {
        this.logger = logger;
    }

    @Override
    public boolean verifyServerKey(ClientSession clientSession, SocketAddress remoteAddress, PublicKey serverKey) {
        logger.info("Server at {} presented unverified {} key: {}",
                remoteAddress, (serverKey == null) ? null : serverKey.getAlgorithm(),
                KeyUtils.getFingerPrint(serverKey));
        return true;
    }

}
