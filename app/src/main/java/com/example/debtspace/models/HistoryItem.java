package com.example.debtspace.models;

import com.example.debtspace.config.Configuration;

import java.util.Map;

public class HistoryItem {

    private String debt;
    private String username;
    private String date;
    private String comment;

    public HistoryItem(Map<String, String> map) {
        debt = map.get(Configuration.DEBT_KEY);
        username = map.get(Configuration.USERNAME_KEY);
        comment = map.get(Configuration.COMMENT_KEY);
        date = map.get(Configuration.DATE_KEY);
    }

    public HistoryItem(String user, String debt, String comment, String date) {
        this.debt = debt;
        this.username = user;
        this.comment = comment;
        this.date = date;
    }

    public String getDebt() {
        return debt;
    }

    public String getUser() {
        return username;
    }

    public String getDate() {
        return date;
    }

    public String getComment() {
        return comment;
    }
}

