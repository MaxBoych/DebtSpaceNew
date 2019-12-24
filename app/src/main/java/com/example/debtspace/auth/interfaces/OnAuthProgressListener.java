package com.example.debtspace.auth.interfaces;

public interface OnAuthProgressListener {

    void onSuccessful();

    void onFailure(String errorMessage);
}
