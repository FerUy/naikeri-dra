package com.naikeri.sgw.configuration.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ApplicationID")
@XmlAccessorType(XmlAccessType.FIELD)
public class RealmApplicationID {
    @XmlElement(name = "VendorId")
    private RealmVendorID vendorID;

    @XmlElement(name = "AuthApplId")
    private RealmAuthApplId authApplId;

    @XmlElement(name = "AcctApplId")
    private RealmAcctApplId acctApplId;

    public RealmVendorID getVendorID() {
        return vendorID;
    }

    public void setVendorID(RealmVendorID vendorID) {
        this.vendorID = vendorID;
    }

    public RealmAuthApplId getAuthApplId() {
        return authApplId;
    }

    public void setAuthApplId(RealmAuthApplId authApplId) {
        this.authApplId = authApplId;
    }

    public RealmAcctApplId getAcctApplId() {
        return acctApplId;
    }

    public void setAcctApplId(RealmAcctApplId acctApplId) {
        this.acctApplId = acctApplId;
    }
}
