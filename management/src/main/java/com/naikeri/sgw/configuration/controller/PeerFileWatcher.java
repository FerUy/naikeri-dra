package com.naikeri.sgw.configuration.controller;

public class PeerFileWatcher extends FileWatcher {
    public PeerFileWatcher(String watchFile) {
        super(watchFile);
    }

    @Override
    public void onModified() {
        //System.out.println("Modification detected in diameter-server.xml file!");
    }
}