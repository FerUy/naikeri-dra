package com.naikeri.sgw.configuration.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Extensions")
@XmlAccessorType(XmlAccessType.FIELD)

public class Extensions {
    @XmlElement(name = "Connection")
    private Connection connection;

    @XmlElement(name = "NetworkGuard")
    private NetworkGuard networkGuard;

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public NetworkGuard getNetworkGuard() {
        return networkGuard;
    }

    public void setNetworkGuard(NetworkGuard networkGuard) {
        this.networkGuard = networkGuard;
    }
}
