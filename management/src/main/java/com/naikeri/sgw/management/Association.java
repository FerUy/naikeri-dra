package com.naikeri.sgw.management;

import com.naikeri.sgw.configuration.controller.ConfigurationController;
import com.naikeri.sgw.configuration.dto.Configuration;
import com.naikeri.sgw.configuration.dto.Peer;
import com.naikeri.sgw.configuration.dto.Peers;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "assoc", description = "Performs associations manipulation operations", mixinStandardHelpOptions = true, version = "DRA CLI 1.0")
public class Association implements Callable<String> {
    @CommandLine.Option(names = "assoc", description = "initializator")
    private boolean initialToken;

    @CommandLine.Option(names = "-edit", description = "Edit a given association")
    private String edit;

    @CommandLine.Option(names = "-list", description = "Returns associations list")
    private boolean list;


    @Override
    public String call() throws Exception {
        if (edit != null) {
            System.out.println("Association edited, new name is " + edit);
        }

        if (list) {
            ConfigurationController controller = new ConfigurationController();
            Configuration configuration = controller.getConfiguration();

            if (configuration !=null && configuration.getNetwork() != null && configuration.getNetwork().getPeers()!=null){
                Peers peers = configuration.getNetwork().getPeers();
                int peerCounter = 0;
                for (Peer peer : peers.getPeerList()){
                    peerCounter++;
                    System.out.println("Peer " + peerCounter + " -> " + peer.getName());
                }

            }

        }
        return "";
    }
}