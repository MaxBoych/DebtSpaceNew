package com.example.debtspace.models;

import android.net.Uri;

import com.example.debtspace.config.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class User {

    String firstName;
    String lastName;
    String username;
    private String score;
    Uri imageUri;
    private List<String> groups;

    public User() {}

    public User(Map<String, String> map) {
        firstName = map.get(Configuration.FIRST_NAME_KEY);
        lastName = map.get(Configuration.LAST_NAME_KEY);
        username = map.get(Configuration.USERNAME_KEY);
        score = map.get(Configuration.SCORE_KEY);
    }

    public User(String username) {
        this.username = username;
    }

    public User(String firstName, String lastName, String username, String score) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.groups = new ArrayList<>();
        this.score = score;
    }

    public void setUriImage(Uri uri) {
        imageUri = uri;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getScore() {
        return score;
    }

    public List<String> getGroups() {
        return groups;
    }
}
