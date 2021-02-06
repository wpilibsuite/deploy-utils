package edu.wpi.first.embeddedtools.deploy.sessions;

public interface IPSessionController extends SessionController {
    String getHost();
    int getPort();
}
