package com.example.debtspace.models;

import android.net.Uri;

public class FriendRequest extends Notification {

    private Uri imageUri;

    public FriendRequest() {}

    public FriendRequest(User user, String date) {
        this.id = "";
        this.name = user.getFirstName() + " " + user.getLastName();
        this.imageUri = user.getImageUri();
        this.username = user.getUsername();
        this.date = date;
    }

    public FriendRequest(String username) {
        this.username = username;
        this.id = "";
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }
}
