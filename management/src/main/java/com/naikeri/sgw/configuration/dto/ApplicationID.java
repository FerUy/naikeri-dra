package com.naikeri.sgw.configuration.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ApplicationID")
@XmlAccessorType(XmlAccessType.FIELD)
public class ApplicationID {

    @XmlElement(name = "VendorId")
    private VendorId vendorId;

    @XmlElement(name = "AuthApplId")
    private AuthApplId authApplId;

    public VendorId getVendorId() {
        return vendorId;
    }

    public void setVendorId(VendorId vendorId) {
        this.vendorId = vendorId;
    }

    public AuthApplId getAuthApplId() {
        return authApplId;
    }

    public void setAuthApplId(AuthApplId authApplId) {
        this.authApplId = authApplId;
    }
}