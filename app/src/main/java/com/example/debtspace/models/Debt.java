package com.example.debtspace.models;

import android.net.Uri;

public class Debt implements Comparable<Debt> {

    private User user;
    private Uri uriImage;
    String debt;
    String date;

    public Debt() {}

    public Debt(User user, String debt, String date) {
        this.user = user;
        this.debt = debt;
        this.date = date;
    }

    public Debt(DebtBond debtBond) {
        this.user = new User(debtBond.getUsername());
        this.debt = debtBond.getDebt();
        this.date = debtBond.getDate();
    }

    public void setUriImage(Uri uri) {
        uriImage = uri;
    }

    public User getUser() {
        return user;
    }

    public void setDebt(String debt) {
        this.debt = debt;
    }

    public String getDebt() {
        return debt;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public Uri getUriImage() {
        return uriImage;
    }

    @Override
    public int compareTo(Debt o) {
        return o.date.compareTo(this.date);
    }
}
