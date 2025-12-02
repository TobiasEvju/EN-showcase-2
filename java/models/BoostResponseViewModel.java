package com.example.demo.models;

import java.util.ArrayList;

public class BoostResponseViewModel {
    private ArrayList<String> textResponse;
    private ArrayList<LinkResponse> linkResponse;
    private String conversationId;

    public BoostResponseViewModel(ArrayList<String> textResponse, ArrayList<LinkResponse> linkResponse, String conversationId) {
        this.textResponse = textResponse;
        this.linkResponse = linkResponse;
        this.conversationId = conversationId;
    }

    public BoostResponseViewModel() {
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

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
}
