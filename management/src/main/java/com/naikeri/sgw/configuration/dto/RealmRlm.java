package com.naikeri.sgw.configuration.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;

@XmlRootElement(name = "Realm")
@XmlAccessorType(XmlAccessType.FIELD)
public class RealmRlm {

    @XmlElement(name = "ApplicationID")
    private List<RealmApplicationID> applicationIDList;

    @XmlAttribute(name = "name")
    private String name;

    @XmlAttribute(name = "peers")
    private String peers;

    @XmlAttribute(name = "local_action")
    private String localAction;

    @XmlAttribute(name = "dynamic")
    private String dynamic;

    @XmlAttribute(name = "exp_time")
    private String expTime;

    public List<RealmApplicationID> getApplicationIDList() {
        return applicationIDList;
    }

    public void setApplicationIDList(List<RealmApplicationID> applicationIDList) {
        this.applicationIDList = applicationIDList;
    }
}
