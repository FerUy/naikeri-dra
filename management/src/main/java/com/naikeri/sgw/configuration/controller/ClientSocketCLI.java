package com.naikeri.sgw.configuration.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Base64;

public class ClientSocketCLI {
    private static PrintWriter out;
    private static BufferedReader in;
    private static Socket clientSocket;

    // Must match ServerSocketCLI.CLI_PORT in the routing agent
    private static final int CLI_PORT = 5556;

    private ClientSocketCLI() {
    }

    public static int initialize() {
        int response = 1;
        try {
            clientSocket = new Socket("127.0.0.1", CLI_PORT);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (Exception ex) {
            response = 0;
            System.out.println("An error has occurred while attempting to initialize the socket client, please check if the Naikeri DRA is running");
        }
        return response;
    }

    public static String sendAction(String msg) {
        String encodeMsg = Base64.getEncoder().encodeToString(msg.getBytes());
        out.println(encodeMsg);
        String resp = null;
        try {
            resp = in.readLine();
        } catch (IOException ex) {
            System.out.println("An error has occurred while attempting to send action to the socket server " + ex.getMessage());
        }
        return resp;
    }

    public static void stopConnection() {
        try {
            in.close();
            out.close();
            clientSocket.close();
        } catch (Exception ex) {
            System.out.println("An error has occurred while attempting to stop the socket client " + ex.getMessage());
        }

    }
}
