package com.naikeri.sgw.impl.app.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;
import java.util.stream.Collectors;

@XmlRootElement(name = "SignalingGateway")
@XmlAccessorType(XmlAccessType.FIELD)
public class SignalingGatewayRules {

    @XmlElementWrapper(name = "Applications")
    @XmlElement(name = "Application")
    private List<Application> applications;


    public SignalingGatewayRules() {
    }

    public List<Application> getApplications() {
        List<Application> filterApplications = applications.stream().filter(f -> f.isEnabled()).collect(Collectors.toList());
        return filterApplications;
    }

    public void setApplications(List<Application> applications) {
        this.applications = applications;
    }

    @Override
    public String toString() {
        return "SignalingGatewayRule {" +
                "applications=" + applications +
                '}';
    }
}
