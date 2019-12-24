package com.example.debtspace.main.interfaces;

import com.example.debtspace.models.User;

public interface OnFindUserListener {

    void onSuccessful(User user);

    void onDoesNotExist();

    void onFailure(String errorMessage);
}
