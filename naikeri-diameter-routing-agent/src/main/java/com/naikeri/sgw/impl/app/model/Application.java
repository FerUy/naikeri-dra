package com.naikeri.sgw.impl.app.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class Application {

    @XmlAttribute
    private String name;
    @XmlAttribute
    private boolean enabled;
    @XmlAttribute
    private int workers;
    @XmlAttribute
    private String channel;
    @XmlAttribute
    private String queue;

    @XmlElement
    private Rules rules;

    public Application() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getWorkers() {
        return workers;
    }

    public void setWorkers(int workers) {
        this.workers = workers;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public Rules getRules() {
        return rules;
    }

    public void setRules(Rules rules) {
        this.rules = rules;
    }

    @Override
    public String toString() {
        return "Application{" +
                "name='" + name + '\'' +
                ", enabled=" + enabled +
                ", workers=" + workers +
                ", channel='" + channel + '\'' +
                ", queue='" + queue + '\'' +
                ", rules=" + rules +
                '}';
    }
}
