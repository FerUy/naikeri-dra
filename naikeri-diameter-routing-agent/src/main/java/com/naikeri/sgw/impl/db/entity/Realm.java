package com.naikeri.sgw.impl.db.entity;

import com.naikeri.sgw.impl.db.persistence.Column;
import com.naikeri.sgw.impl.db.persistence.Table;
import org.jdiameter.api.LocalAction;

import java.util.Arrays;

@Table(name = "realm")
public class Realm {

    @Column(name = "realm_id")
    private Long id;
    private String name;
    private String[] peers;
    @Column(name = "local_action")
    private String localAction;
    private Boolean dynamic;
    @Column(name = "exp_time")
    private Long expTime;
    private ApplicationId applicationId;

    public Realm() {
    }

    public Realm(Long id, String name, String[] peers, String localAction, Boolean dynamic, Long expTime, ApplicationId applicationId) {
        this.id = id;
        this.name = name;
        this.peers = peers;
        this.localAction = localAction;
        this.dynamic = dynamic;
        this.expTime = expTime;
        this.applicationId = applicationId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getPeers() {
        return peers;
    }

    public void setPeers(String[] peers) {
        this.peers = peers;
    }

    public LocalAction getLocalAction() {
        return LocalAction.valueOf(localAction);
    }

    public void setLocalAction(String localAction) {
        this.localAction = localAction;
    }

    public Boolean getDynamic() {
        return dynamic;
    }

    public void setDynamic(Boolean dynamic) {
        this.dynamic = dynamic;
    }

    public Long getExpTime() {
        return expTime;
    }

    public void setExpTime(Long expTime) {
        this.expTime = expTime;
    }

    public ApplicationId getApplicationId(){
        return applicationId;
    }

    public org.jdiameter.api.ApplicationId getDiameterApplicationId() {
        if(applicationId.getVendorId() !=0 && applicationId.getAuthApplId() != 0)
            return org.jdiameter.api.ApplicationId.createByAuthAppId(applicationId.getVendorId(), applicationId.getAuthApplId());
        else
            return org.jdiameter.api.ApplicationId.createByAccAppId(applicationId.getVendorId(), applicationId.getAcctApplId());
    }

    public void setApplicationId(ApplicationId applicationId) {
        this.applicationId = applicationId;
    }

    @Override
    public String toString() {
        return "Realm{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", peers='" + Arrays.toString(peers) + '\'' +
                ", localAction='" + localAction + '\'' +
                ", dynamic=" + dynamic +
                ", expTime=" + expTime +
                ", applicationId=" + applicationId +
                '}';
    }
}
