package com.example.debtspace.main.repositories;

import android.content.Context;
import android.util.Log;

import com.example.debtspace.application.DebtSpaceApplication;
import com.example.debtspace.config.AppConfig;
import com.example.debtspace.config.ErrorsConfig;
import com.example.debtspace.main.interfaces.OnUpdateDataListener;
import com.example.debtspace.utilities.StringUtilities;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FriendRequestRepository {

    private FirebaseFirestore mDatabase;
    private String mUsername;

    public FriendRequestRepository(Context context) {
        mDatabase = DebtSpaceApplication.from(context).getDatabase();

        mUsername = DebtSpaceApplication.from(context).getUsername();
    }

    private void sendFriendRequest(String username, OnUpdateDataListener listener) {
        Map<String, String> map = new HashMap<>();
        String date = StringUtilities.getCurrentDateAndTime();
        map.put(AppConfig.DATE_KEY, date);

        mDatabase.collection(AppConfig.NOTIFICATIONS_COLLECTION_NAME)
                .document(username)
                .collection(AppConfig.FRIENDS_COLLECTION_NAME)
                .document(mUsername)
                .set(map)
                .addOnSuccessListener(aVoid ->
                        listener.onUpdateSuccessful())
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfig.ERROR_SEND_FRIEND_REQUEST + username);
                });
    }

    public void checkExistenceFriends(String username, OnUpdateDataListener listener) {
        mDatabase.collection(AppConfig.DEBTS_COLLECTION_NAME)
                .document(mUsername)
                .collection(AppConfig.FRIENDS_COLLECTION_NAME)
                .get()
                .addOnSuccessListener(documents -> {
                    boolean doesNotContain = true;
                    for (DocumentSnapshot document : documents) {
                        if (document.exists() && document.getId().equals(username)) {
                            doesNotContain = false;
                            listener.onFailure(ErrorsConfig.ERROR_USER_ALREADY_FRIEND + username);
                            break;
                        }
                    }

                    if (doesNotContain) {
                        checkExistenceRequests(username, listener);
                    }
                })
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfig.ERROR_DATA_READING_DEBTS);
                });
    }

    private void checkExistenceRequests(String username, OnUpdateDataListener listener) {
        mDatabase.collection(AppConfig.NOTIFICATIONS_COLLECTION_NAME)
                .document(username)
                .collection(AppConfig.FRIENDS_COLLECTION_NAME)
                .document(mUsername)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        listener.onFailure(ErrorsConfig.ERROR_REQUEST_ALREADY_SENT + username);
                    } else {
                        sendFriendRequest(username, listener);
                    }
                })
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfig.ERROR_DATA_READING_NOTIFICATIONS);
                });
    }
}
