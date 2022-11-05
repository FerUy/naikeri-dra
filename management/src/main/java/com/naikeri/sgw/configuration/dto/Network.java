package com.naikeri.sgw.configuration.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Network")
@XmlAccessorType(XmlAccessType.FIELD)
public class Network {
    @XmlElement(name = "Peers")
    private Peers peers;

    @XmlElement(name = "Realms")
    private Realms realms;

    public Peers getPeers() {
        return peers;
    }

    public void setPeers(Peers peers) {
        this.peers = peers;
    }

    public Realms getRealms() {
        return realms;
    }

    public void setRealms(Realms realms) {
        this.realms = realms;
    }
}
