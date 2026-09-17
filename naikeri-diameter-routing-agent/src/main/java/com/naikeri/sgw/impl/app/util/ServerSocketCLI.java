package com.naikeri.sgw.impl.app.util;

import com.naikeri.sgw.impl.SignalingGateway;
import com.naikeri.sgw.network.layers.DiameterLayer;
import org.jdiameter.api.Peer;
import org.jdiameter.api.PeerState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

public class ServerSocketCLI extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(ServerSocketCLI.class);
    private static final int CLI_PORT = 5556;

    private static ServerSocketCLI instance = null;
    private static DiameterLayer diameterLayer = null;
    private static SignalingGateway signalingGateway;

    private static void getInstance(SignalingGateway signalingGateway) {
        if (instance == null) {
            instance = new ServerSocketCLI(signalingGateway);
        }
    }

    public ServerSocketCLI(SignalingGateway signalingGateway) {
        ServerSocketCLI.signalingGateway = signalingGateway;
    }

    public static ServerSocketCLI initialize(SignalingGateway signalingGateway) {
        ServerSocketCLI.getInstance(signalingGateway);
        return instance;
    }

    @Override
    public void run() {
        logger.info("Starting Server Socket");
        try (ServerSocket serverSocket = new ServerSocket(CLI_PORT)) {
            while (!serverSocket.isClosed()) {
                new ClientHandler(serverSocket.accept()).start();
            }
        } catch (Exception e) {
            logger.error("Error on start server socket", e);
        }
    }

    private static class ClientHandler extends Thread {
        private final Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            try {
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));

                String inputLine;
                String[] params = new String[0];
                StringBuilder stringBuilder = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    byte[] decodedBytes = Base64.getDecoder().decode(inputLine);
                    String decodedMsg = new String(decodedBytes);
                    logger.info("Getting this in socket server {}", decodedMsg);
                    String[] dataSocket = decodedMsg.split("#");
                    if ("peer".equals(dataSocket[0])) {
                        if (dataSocket.length > 2) {
                            params = dataSocket[2].split("\\|");
                        }
                        HashMap<String, Object> paramsData = new HashMap<>();
                        switch (dataSocket[1]) {
                            case "list":
                                List<Peer> peerList = diameterLayer.getPeerList();
                                for (Peer peer : peerList) {
                                    stringBuilder.append(peer.getUri()).append("|");
                                    stringBuilder.append(peer.getState(PeerState.class).toString());
                                    stringBuilder.append("#");
                                }
                                break;

                            case "start":
                                paramsData.put("peerURI", params[0]);
                                paramsData.put("connecting", params[1]);
                                paramsData.put("ip", params[3]);
                                paramsData.put("realm", params[5]);
                                diameterLayer.addPeer(paramsData);
                                break;

                            case "stop":
                                paramsData.put("peerName", params[0]);
                                paramsData.put("disconnectCause", params[1]);
                                paramsData.put("connecting", params[2]);
                                diameterLayer.stopPeer(paramsData);
                                break;
                        }
                    } else if ("init".equals(dataSocket[0])) {
                        ServerSocketCLI.diameterLayer = (DiameterLayer) signalingGateway.getLayer("diameter");
                    }
                    out.println(stringBuilder);
                }

                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException e) {
                logger.error("Error on server socket", e);
            }

        }
    }
}
