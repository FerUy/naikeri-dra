package com.naikeri.sgw.configuration.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;

@XmlRootElement(name = "Applications")
@XmlAccessorType(XmlAccessType.FIELD)
public class Applications {

    @XmlElement(name ="ApplicationID")
    private List<ApplicationID> applicationIDList;

    public List<ApplicationID> getApplicationIDList() {
        return applicationIDList;
    }

    public void setApplicationIDList(List<ApplicationID> applicationIDList) {
        this.applicationIDList = applicationIDList;
    }
}