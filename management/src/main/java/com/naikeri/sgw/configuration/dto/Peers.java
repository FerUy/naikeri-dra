package com.naikeri.sgw.configuration.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;

@XmlRootElement(name = "Peers")
@XmlAccessorType(XmlAccessType.FIELD)
public class Peers {
    @XmlElement(name = "Peer")
    private List<Peer> peerList;

    public List<Peer> getPeerList() {
        return peerList;
    }

    public void setPeerList(List<Peer> peerList) {
        this.peerList = peerList;
    }
}
