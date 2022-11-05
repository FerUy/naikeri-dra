package com.naikeri.sgw.impl.app.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
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
        if (this.dropPolicy != null && this.dropPolicy.contains(dropPolicy))
            return true;
        return false;
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
