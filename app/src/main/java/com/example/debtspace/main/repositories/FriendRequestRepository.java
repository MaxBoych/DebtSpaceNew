package com.example.debtspace.main.repositories;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.example.debtspace.application.DebtSpaceApplication;
import com.example.debtspace.config.Configuration;
import com.example.debtspace.config.ErrorsConfiguration;
import com.example.debtspace.main.interfaces.OnUpdateDataListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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
        @SuppressLint("SimpleDateFormat")
        String date = new SimpleDateFormat(Configuration.PATTERN_DATE).format(Calendar.getInstance().getTime());
        map.put(Configuration.DATE_KEY, date);

        mDatabase.collection(Configuration.NOTIFICATIONS_COLLECTION_NAME)
                .document(username)
                .collection(Configuration.FRIENDS_COLLECTION_NAME)
                .document(mUsername)
                .set(map)
                .addOnSuccessListener(aVoid ->
                        listener.onUpdateSuccessful())
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(Configuration.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfiguration.ERROR_SEND_FRIEND_REQUEST + username);
                });
    }

    public void checkExistenceFriends(String username, OnUpdateDataListener listener) {
        mDatabase.collection(Configuration.DEBTS_COLLECTION_NAME)
                .document(mUsername)
                .collection(Configuration.FRIENDS_COLLECTION_NAME)
                .get()
                .addOnSuccessListener(documents -> {
                    boolean doesNotContain = true;
                    for (DocumentSnapshot document : documents) {
                        if (document.exists() && document.getId().equals(username)) {
                            doesNotContain = false;
                            listener.onFailure(ErrorsConfiguration.ERROR_USER_ALREADY_FRIEND + username);
                            break;
                        }
                    }

                    if (doesNotContain) {
                        checkExistenceRequests(username, listener);
                    }
                })
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(Configuration.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfiguration.ERROR_DATA_READING_DEBTS);
                });
    }

    private void checkExistenceRequests(String username, OnUpdateDataListener listener) {
        mDatabase.collection(Configuration.NOTIFICATIONS_COLLECTION_NAME)
                .document(username)
                .collection(Configuration.FRIENDS_COLLECTION_NAME)
                .document(mUsername)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        listener.onFailure(ErrorsConfiguration.ERROR_REQUEST_ALREADY_SENT + username);
                    } else {
                        sendFriendRequest(username, listener);
                    }
                })
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(Configuration.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfiguration.ERROR_DATA_READING_NOTIFICATIONS);
                });
    }
}
