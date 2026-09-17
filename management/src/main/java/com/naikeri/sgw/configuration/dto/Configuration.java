package com.naikeri.sgw.configuration.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Configuration")
@XmlAccessorType(XmlAccessType.FIELD)
public class Configuration {


    @XmlElement(name = "LocalPeer")
    private  LocalPeer localPeer;

    @XmlElement(name = "Parameters")
    private Parameters parameters;

    @XmlElement(name = "Network")
    private Network network;

    @XmlElement(name = "Extensions")
    private Extensions extensions;

    public Network getNetwork() {
        return network;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public LocalPeer getLocalPeer() {
        return localPeer;
    }

    public void setLocalPeer(LocalPeer localPeer) {
        this.localPeer = localPeer;
    }

    public Parameters getParameters() {
        return parameters;
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

    public Extensions getExtensions() {
        return extensions;
    }

    public void setExtensions(Extensions extensions) {
        this.extensions = extensions;
    }
}