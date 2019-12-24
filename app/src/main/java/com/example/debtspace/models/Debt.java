package com.example.debtspace.models;

import android.net.Uri;

public class Debt {

    private User user;
    private Uri uriImage;
    private String debt;

    public Debt() {}

    public Debt(User user, String debt) {

        this.user = user;
        this.debt = debt;
    }

    public void setUriImage(Uri uri) {
        uriImage = uri;
    }

    public User getUser() {
        return user;
    }

    public String getDebt() {
        return debt;
    }

    public Uri getUriImage() {
        return uriImage;
    }
}
