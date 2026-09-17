package com.naikeri.sgw.impl.app.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import java.util.Arrays;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class Rules {

    private List<String> dropPolicy;
    @XmlElement(name = "rule")
    private List<Rule> rules;

    public Rules() {
    }

    public boolean dropPolicyEnabled(String dropPolicy) {
        return this.dropPolicy != null && this.dropPolicy.contains(dropPolicy);
    }

    @XmlAttribute(name = "drop-policy")
    public void setDrop_Policy(String dropPolicy) {
        this.dropPolicy = Arrays.asList(dropPolicy.split(","));
    }

    public List<Rule> list() {
        return rules;
    }

    @Override
    public String toString() {
        return "Rules{" +
                "dropPolicy='" + dropPolicy + '\'' +
                ", rule=" + rules +
                '}';
    }
}
