package com.example.debtspace.auth.interfaces;

public interface OnAuthStateChangeListener {

    void onAuthScreen();

    void onSignInScreen();

    void onSignUpScreen();

    void onMainScreen();

    void onFailure(String errorMessage);
}
