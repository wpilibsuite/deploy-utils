package edu.wpi.first.embeddedtools.deploy.sessions;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import edu.wpi.first.embeddedtools.deploy.CommandDeployResult;

public class DrySessionController extends AbstractSessionController implements IPSessionController {
    public DrySessionController() {
        super(1);
    }

    @Override
    public void open() {
        getLogger().info("DrySessionController opening");
    }

    @Override
    public CommandDeployResult execute(String command) {
        return new CommandDeployResult(command, "", 0);
    }

    @Override
    public void put(Map<String, File> files) { }

    @Override
    public String friendlyString() {
        return "DrySessionController";
    }

    @Override
    public void close() throws IOException {
        getLogger().info("DrySessionController closing");
    }

    @Override
    public String getHost() {
        return "dryhost";
    }

    @Override
    public int getPort() {
        return 22;
    }
}
