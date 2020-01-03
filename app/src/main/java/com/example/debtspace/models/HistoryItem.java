package com.example.debtspace.models;

import com.example.debtspace.config.Configuration;

import java.util.Map;

public class HistoryItem implements Comparable<HistoryItem> {

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

    public HistoryItem(String username, String debt, String comment, String date) {
        this.debt = debt;
        this.username = username;
        this.comment = comment;
        this.date = date;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setDebt(String debt) {
        this.debt = debt;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void  setDate(String date) {
        this.date = date;
    }

    public String getDebt() {
        return debt;
    }

    public String getUsername() {
        return username;
    }

    public String getDate() {
        return date;
    }

    public String getComment() {
        return comment;
    }

    @Override
    public int compareTo(HistoryItem o) {
        return o.date.compareTo(this.date);
    }
}

