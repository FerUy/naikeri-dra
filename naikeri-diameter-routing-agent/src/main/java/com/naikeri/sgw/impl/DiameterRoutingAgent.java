package com.naikeri.sgw.impl;

import com.naikeri.sgw.impl.app.util.GettingRules;
import com.naikeri.sgw.impl.app.util.ServerSocketCLI;
import com.naikeri.sgw.impl.db.DataSource;

public class DiameterRoutingAgent extends SignalingGateway {

    public static SignalingGateway signalingGateway;
    public static void main(String[] args) {
        DataSource.initialize();
        GettingRules.initialize().start();
        signalingGateway = DiameterRoutingAgent.initialize(args);
        signalingGateway.start();
        ServerSocketCLI.initialize(signalingGateway).start();
    }

}