package com.naikeri.sgw.impl.app.model;

import javax.xml.bind.annotation.*;
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
