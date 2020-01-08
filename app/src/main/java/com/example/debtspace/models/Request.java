package com.example.debtspace.models;

public class Request extends User implements Comparable<Request> {

    private String date;

    public Request(User user, String date) {
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.imageUri = user.getImageUri();
        this.username = user.getUsername();
        this.date = date;
    }

    public Request(String username) {
        this.username = username;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    @Override
    public int compareTo(Request o) {
        return o.date.compareTo(this.date);
    }
}
