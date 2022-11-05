package com.naikeri.sgw.impl.app.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import java.util.regex.Pattern;

@XmlAccessorType(XmlAccessType.FIELD)
public class Match {

    private Pattern regexOriginHost;
    private Pattern regexOriginRealm;
    private Pattern regexDestinationHost;
    private Pattern regexDestinationRealm;
    private Pattern regexImsi;
    @XmlAttribute(name = "subscription-id")
    private Boolean subscriptionId;

    public Match() {
    }

    @XmlAttribute(name = "origin-host")
    public void setOriginHost(String originHost) {
        this.regexOriginHost = Pattern.compile(originHost);
    }

    @XmlAttribute(name = "origin-realm")
    public void setOriginRealm(String originRealm) {
        this.regexOriginRealm = Pattern.compile(originRealm);
    }

    @XmlAttribute(name = "imsi")
    public void setImsi(String imsi) {
        this.regexImsi = Pattern.compile(imsi);
    }

    @XmlAttribute(name = "destination-realm")
    public void setDestinationRealm(String destinationRealm){
        this.regexDestinationRealm = Pattern.compile(destinationRealm);
    }

    @XmlAttribute(name = "destination-host")
    public void setDestinationHost(String destinationHost){
        this.regexDestinationHost= Pattern.compile(destinationHost);
    }


    public Pattern originHost() {
        return regexOriginHost == null ? Pattern.compile(".*") : regexOriginHost;
    }

    public Pattern originRealm() {
        return regexOriginRealm == null ? Pattern.compile(".*") : regexOriginRealm;
    }

    public Pattern imsi() {
        return regexImsi;
    }

    public Boolean isSubscriptionId() {
        return subscriptionId;
    }

    public Pattern destinationRealm(){
        return regexDestinationRealm == null ? Pattern.compile(".*") : regexDestinationRealm;
    }

    public Pattern destinationHost(){
        return regexDestinationHost == null ? Pattern.compile(".*") : regexDestinationHost;
    }

}
