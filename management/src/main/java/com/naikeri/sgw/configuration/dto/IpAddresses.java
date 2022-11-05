package com.naikeri.sgw.configuration.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;

@XmlRootElement(name = "IPAddresses")
@XmlAccessorType(XmlAccessType.FIELD)
public class IpAddresses {

    @XmlElement(name = "IPAddress")
    private List<IpAddress> ipAddressList;

    public List<IpAddress> getIpAddressList() {
        return ipAddressList;
    }

    public void setIpAddressList(List<IpAddress> ipAddressList) {
        this.ipAddressList = ipAddressList;
    }
}
