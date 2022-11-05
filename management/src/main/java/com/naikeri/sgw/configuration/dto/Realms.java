package com.naikeri.sgw.configuration.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;

@XmlRootElement(name = "Realms")
@XmlAccessorType(XmlAccessType.FIELD)
public class Realms {

    @XmlElement(name = "Realm")
    private List<RealmRlm> realmList;

    public List<RealmRlm> getRealmList() {
        return realmList;
    }

    public void setRealmList(List<RealmRlm> realmList) {
        this.realmList = realmList;
    }
}
