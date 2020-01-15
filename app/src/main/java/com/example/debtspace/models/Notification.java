package com.example.debtspace.models;

public class Notification implements Comparable<Notification> {

    String id;
    String date;
    String name;
    String username;

    Notification() {}

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    @Override
    public int compareTo(Notification o) {
        return o.date.compareTo(this.date);
    }
}
