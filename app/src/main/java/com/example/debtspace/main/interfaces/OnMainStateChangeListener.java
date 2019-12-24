package com.example.debtspace.main.interfaces;

import com.example.debtspace.models.GroupDebt;
import com.example.debtspace.models.User;

public interface OnMainStateChangeListener {

    void onDebtListScreen();

    void onUserSearchListScreen();

    void onGroupDebtScreen();

    void onGroupDebtScreen(GroupDebt debt);

    void onFriendRequestScreen(User user);

    void onProfileScreen();

    void onImageManagementScreen(String id);

    void onHistoryScreen();

    void onStrikeScreen(User user);

    void onRequestScreen();

    void onRequestConfirmScreen(User user);

    void onAuthScreen();

    void onFailure(String errorMessage);
}
