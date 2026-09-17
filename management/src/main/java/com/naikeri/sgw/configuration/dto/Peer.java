package com.naikeri.sgw.configuration.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Peer")
@XmlAccessorType(XmlAccessType.FIELD)
public class Peer {
    @XmlAttribute(name = "name")
    private String name;

    @XmlAttribute(name = "attempt_connect")
    private boolean attemptConnect;

    @XmlAttribute(name = "rating")
    private int rating;

    @XmlAttribute(name = "ip")
    private String ip;

    @XmlAttribute(name = "standby_addresses")
    private String standbyAddresses;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAttemptConnect() {
        return attemptConnect;
    }

    public void setAttemptConnect(boolean attemptConnect) {
        this.attemptConnect = attemptConnect;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getStandbyAddresses() {
        return standbyAddresses;
    }

    public void setStandbyAddresses(String standbyAddresses) {
        this.standbyAddresses = standbyAddresses;
    }

    public String getIp() {
        return ip == null ? "" : ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
