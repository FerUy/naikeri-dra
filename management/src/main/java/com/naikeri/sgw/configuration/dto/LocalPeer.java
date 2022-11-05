package com.naikeri.sgw.configuration.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "LocalPeer")
@XmlAccessorType(XmlAccessType.FIELD)
public class LocalPeer {

    @XmlElement(name = "URI")
    private Uri uri;

    @XmlElement(name = "IPAddresses")
    private IpAddresses ipAddresses;

    @XmlElement(name = "Realm")
    private Realm realm;

    @XmlElement(name = "VendorID")
    private VendorId vendorID;

    @XmlElement(name = "ProductName")
    private ProductName productName;

    @XmlElement(name = "FirmwareRevision")
    private FirmwareRevision firmwareRevision;

    @XmlElement(name = "Applications")
    private Applications applications;

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public IpAddresses getIpAddresses() {
        return ipAddresses;
    }

    public void setIpAddresses(IpAddresses ipAddresses) {
        this.ipAddresses = ipAddresses;
    }

    public Realm getRealm() {
        return realm;
    }

    public void setRealm(Realm realm) {
        this.realm = realm;
    }

    public VendorId getVendorID() {
        return vendorID;
    }

    public void setVendorID(VendorId vendorID) {
        this.vendorID = vendorID;
    }

    public ProductName getProductName() {
        return productName;
    }

    public void setProductName(ProductName productName) {
        this.productName = productName;
    }

    public FirmwareRevision getFirmwareRevision() {
        return firmwareRevision;
    }

    public void setFirmwareRevision(FirmwareRevision firmwareRevision) {
        this.firmwareRevision = firmwareRevision;
    }

    public Applications getApplications() {
        return applications;
    }

    public void setApplications(Applications applications) {
        this.applications = applications;
    }
}