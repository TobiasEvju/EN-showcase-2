package com.example.demo.models;

public class BoostSendDTO {
    private String message;
    private String conversationId;

    public BoostSendDTO(String message) {
        this.message = message;
    }

    public BoostSendDTO(String message, String conversationId) {
        this.message = message;
        this.conversationId = conversationId;
    }

    public BoostSendDTO() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
}
