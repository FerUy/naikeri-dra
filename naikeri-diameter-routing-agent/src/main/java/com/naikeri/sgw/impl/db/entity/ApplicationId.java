package com.naikeri.sgw.impl.db.entity;

import com.naikeri.sgw.impl.db.persistence.Column;
import com.naikeri.sgw.impl.db.persistence.Table;

@Table(name = "application_id")
public class ApplicationId {

    @Column(name = "appl_id")
    private Long id;
    @Column(name = "vendor_id")
    private Long vendorId;
    @Column(name = "auth_appl_id")
    private Long authApplId;
    @Column(name = "acct_appl_id")
    private Long acctApplId;

    public ApplicationId() {
    }

    public ApplicationId(Long id, Long vendorId, Long authApplId, Long acctApplId) {
        this.id = id;
        this.vendorId = vendorId;
        this.authApplId = authApplId;
        this.acctApplId = acctApplId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVendorId() {
        return vendorId;
    }

    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
    }

    public Long getAuthApplId() {
        return authApplId;
    }

    public void setAuthApplId(Long authApplId) {
        this.authApplId = authApplId;
    }

    public Long getAcctApplId() {
        return acctApplId;
    }

    public void setAcctApplId(Long acctApplId) {
        this.acctApplId = acctApplId;
    }

    @Override
    public String toString() {
        return "ApplicationId{" +
                "id=" + id +
                ", vendorId=" + vendorId +
                ", authApplId=" + authApplId +
                ", acctApplId=" + acctApplId +
                '}';
    }
}
