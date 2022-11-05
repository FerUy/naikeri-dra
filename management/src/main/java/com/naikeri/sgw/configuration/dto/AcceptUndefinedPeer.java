package com.naikeri.sgw.configuration.dto;


import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "AcceptUndefinedPeer")
@XmlAccessorType(XmlAccessType.FIELD)
public class AcceptUndefinedPeer {

    @XmlAttribute(name = "value")
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
