package com.chatroom.model;

public class Message {
    private String senderUID, recipientUID, text, file, status;
    private long timestamp;

    public Message(String senderUID, String recipientUID, String text, String file, String status, long timestamp) {
        this.senderUID = senderUID;
        this.recipientUID = recipientUID;
        this.text = text;
        this.file = file;
        this.status = status;
        this.timestamp = timestamp;
    }

    public Message() {
    }

    public String getSenderUID() {
        return senderUID;
    }

    public void setSenderUID(String senderUID) {
        this.senderUID = senderUID;
    }

    public String getRecipientUID() {
        return recipientUID;
    }

    public void setRecipientUID(String recipientUID) {
        this.recipientUID = recipientUID;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

}
