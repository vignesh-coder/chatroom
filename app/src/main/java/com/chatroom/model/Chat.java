package com.chatroom.model;

public class Chat {
    private long timestamp;

    public Chat(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Chat() {

    }
}
