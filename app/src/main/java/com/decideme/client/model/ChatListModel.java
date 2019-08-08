package com.decideme.client.model;

import java.util.ArrayList;

public class ChatListModel {

    private String response;
    private String message;
    private ArrayList<ChatList> chat_data = new ArrayList<ChatList>();

    public String getResponse() {
        return response;
    }

    public String getMessage() {
        return message;
    }

    public ArrayList<ChatList> getChat_data() {
        return chat_data;
    }
}
