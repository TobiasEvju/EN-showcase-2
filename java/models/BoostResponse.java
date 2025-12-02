package com.example.demo.models;

import java.util.ArrayList;

public class BoostResponse {

    private String conversationId;
    private String reference;
    private String avatarUrl;
    private String dateCreated;
    private ArrayList<String> textResponse;
    private ArrayList<LinkResponse> linkResponse;

    private String id;
    private String language;
    private String source;


    public BoostResponse(String conversationId, String reference, String avatarUrl, String dateCreated, ArrayList<BoostElement> elements, String id, String language, String source) {
        this.conversationId = conversationId;
        this.reference = reference;
        this.avatarUrl = avatarUrl;
        this.dateCreated = dateCreated;
        this.id = id;
        this.language = language;
        this.source = source;
    }

    public BoostResponse() {
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }


    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }


    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getLanguage() { return language; }

    public void setLanguage(String language) { this.language = language; }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public ArrayList<String> getTextResponse() {
        return textResponse;
    }

    public void setTextResponse(ArrayList<String> textResponse) {
        this.textResponse = textResponse;
    }

    public ArrayList<LinkResponse> getLinkResponse() {
        return linkResponse;
    }

    public void setLinkResponse(ArrayList<LinkResponse> linkResponse) {
        this.linkResponse = linkResponse;
    }
}
