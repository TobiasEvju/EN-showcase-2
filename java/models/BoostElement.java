package com.example.demo.models;

import java.util.ArrayList;

public class BoostElement {
    private ArrayList<String> content;
    private String payloadType;


    public BoostElement(ArrayList<String> content, String payloadType) {
        this.content = content;
        this.payloadType = payloadType;
    }


    public ArrayList<String> getContent() {
        return content;
    }

    public void setContent(ArrayList<String> content) {
        this.content = content;
    }

    public String getPayloadType() {
        return payloadType;
    }

    public void setPayloadType(String payloadType) {
        this.payloadType = payloadType;
    }
}
