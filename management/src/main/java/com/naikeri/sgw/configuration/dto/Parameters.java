package com.naikeri.sgw.configuration.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Parameters")
@XmlAccessorType(XmlAccessType.FIELD)
public class Parameters {

    @XmlElement(name = "AcceptUndefinedPeer")
    private AcceptUndefinedPeer acceptUndefinedPeer;

    @XmlElement(name = "DuplicateProtection")
    private DuplicateProtection duplicateProtection;

    @XmlElement(name = "DuplicateTimer")
    private DuplicateTimer duplicateTimer;

    @XmlElement(name = "UseUriAsFqdn")
    private UseUriAsFqdn useUriAsFqdn;

    @XmlElement(name = "QueueSize")
    private QueueSize queueSize;

    @XmlElement(name = "MessageTimeOut")
    private MessageTimeOut messageTimeOut;

    @XmlElement(name = "StopTimeOut")
    private StopTimeOut stopTimeOut;

    @XmlElement(name = "CeaTimeOut")
    private CeaTimeOut ceaTimeOut;

    @XmlElement(name = "IacTimeOut")
    private IacTimeOut iacTimeOut;

    @XmlElement(name = "DwaTimeOut")
    private DwaTimeOut dwaTimeOut;

    @XmlElement(name = "DpaTimeOut")
    private DpaTimeOut dpaTimeOut;

    @XmlElement(name = "RecTimeOut")
    private RecTimeOut recTimeOut;

    @XmlElement(name = "PeerFSMThreadCount")
    private PeerFSMThreadCount peerFSMThreadCount;

    public AcceptUndefinedPeer getAcceptUndefinedPeer() {
        return acceptUndefinedPeer;
    }

    public void setAcceptUndefinedPeer(AcceptUndefinedPeer acceptUndefinedPeer) {
        this.acceptUndefinedPeer = acceptUndefinedPeer;
    }

    public DuplicateProtection getDuplicateProtection() {
        return duplicateProtection;
    }

    public void setDuplicateProtection(DuplicateProtection duplicateProtection) {
        this.duplicateProtection = duplicateProtection;
    }

    public DuplicateTimer getDuplicateTimer() {
        return duplicateTimer;
    }

    public void setDuplicateTimer(DuplicateTimer duplicateTimer) {
        this.duplicateTimer = duplicateTimer;
    }

    public UseUriAsFqdn getUseUriAsFqdn() {
        return useUriAsFqdn;
    }

    public void setUseUriAsFqdn(UseUriAsFqdn useUriAsFqdn) {
        this.useUriAsFqdn = useUriAsFqdn;
    }

    public QueueSize getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(QueueSize queueSize) {
        this.queueSize = queueSize;
    }

    public MessageTimeOut getMessageTimeOut() {
        return messageTimeOut;
    }

    public void setMessageTimeOut(MessageTimeOut messageTimeOut) {
        this.messageTimeOut = messageTimeOut;
    }

    public StopTimeOut getStopTimeOut() {
        return stopTimeOut;
    }

    public void setStopTimeOut(StopTimeOut stopTimeOut) {
        this.stopTimeOut = stopTimeOut;
    }

    public CeaTimeOut getCeaTimeOut() {
        return ceaTimeOut;
    }

    public void setCeaTimeOut(CeaTimeOut ceaTimeOut) {
        this.ceaTimeOut = ceaTimeOut;
    }

    public IacTimeOut getIacTimeOut() {
        return iacTimeOut;
    }

    public void setIacTimeOut(IacTimeOut iacTimeOut) {
        this.iacTimeOut = iacTimeOut;
    }

    public DwaTimeOut getDwaTimeOut() {
        return dwaTimeOut;
    }

    public void setDwaTimeOut(DwaTimeOut dwaTimeOut) {
        this.dwaTimeOut = dwaTimeOut;
    }

    public DpaTimeOut getDpaTimeOut() {
        return dpaTimeOut;
    }

    public void setDpaTimeOut(DpaTimeOut dpaTimeOut) {
        this.dpaTimeOut = dpaTimeOut;
    }

    public RecTimeOut getRecTimeOut() {
        return recTimeOut;
    }

    public void setRecTimeOut(RecTimeOut recTimeOut) {
        this.recTimeOut = recTimeOut;
    }

    public PeerFSMThreadCount getPeerFSMThreadCount() {
        return peerFSMThreadCount;
    }

    public void setPeerFSMThreadCount(PeerFSMThreadCount peerFSMThreadCount) {
        this.peerFSMThreadCount = peerFSMThreadCount;
    }
}
