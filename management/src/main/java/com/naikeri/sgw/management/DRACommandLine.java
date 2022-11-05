package com.naikeri.sgw.management;

import com.naikeri.sgw.configuration.controller.ClientSocketCLI;
import picocli.CommandLine;

public class DRACommandLine {
    public static void main(String[] args) {
        if (ClientSocketCLI.initialize() > 0) {
            ClientSocketCLI.sendAction("init");
            try {
                if (args != null && args[0] != null) {
                    String operation = args[0];
                    if (operation.equals("assoc")) {
                        int exitCode = new CommandLine(new Association()).execute(args);
                        ClientSocketCLI.stopConnection();
                        System.exit(exitCode);
                    } else if (operation.equals("peer")) {
                        int exitCode = new CommandLine(new Peer()).execute(args);
                        ClientSocketCLI.stopConnection();
                        System.exit(exitCode);
                    } else {
                        System.out.println("Invalid Command");
                        ClientSocketCLI.stopConnection();
                    }
                }

            } catch (Exception ex) {
                ClientSocketCLI.stopConnection();
                System.out.println(ex.getMessage());
            }
        }
    }
}