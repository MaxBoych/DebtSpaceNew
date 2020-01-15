package com.example.debtspace.main.interfaces;

import com.example.debtspace.models.DebtRequest;
import com.example.debtspace.models.FriendRequest;
import com.example.debtspace.models.GroupDebt;
import com.example.debtspace.models.HistoryItem;
import com.example.debtspace.models.User;

public interface OnMainStateChangeListener {

    void onDebtListScreen();

    void onUserSearchListScreen();

    void onGroupDebtScreen();

    void onGroupDebtScreen(GroupDebt debt);

    void onFriendRequestScreen(User user);

    void onProfileScreen();

    void onImageManagementScreen(String id, OnImageSharingListener listener);

    void onHistoryScreen();

    void onStrikeScreen(User user);

    void onFriendRemovalScreen(String name, String username);

    void onNotificationListScreen();

    void onRequestConfirmScreen(FriendRequest request);

    void onDebtRemovalScreen(HistoryItem item);

    void onDebtRemovalConfirmScreen(DebtRequest request);

    void onHistoryRemovalScreen(OnPassSignalListener listener);

    void onAuthScreen();

    void onNetworkLostScreen();

    void onFailure(String errorMessage);
}
