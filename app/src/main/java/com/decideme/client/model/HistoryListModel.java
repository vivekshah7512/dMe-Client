package com.decideme.client.model;

import java.util.ArrayList;

public class HistoryListModel {

    private String response;
    private String message;
    private ArrayList<HistoryList> upcoming_data = new ArrayList<HistoryList>();
    private ArrayList<HistoryList> past_data = new ArrayList<>();

    public String getResponse() {
        return response;
    }

    public String getMessage() {
        return message;
    }

    public ArrayList<HistoryList> getUpcoming_data() {
        return upcoming_data;
    }

    public ArrayList<HistoryList> getPast_data() {
        return past_data;
    }
}
