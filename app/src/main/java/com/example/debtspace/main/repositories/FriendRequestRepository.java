package com.example.debtspace.main.repositories;

import android.content.Context;

import com.example.debtspace.application.DebtSpaceApplication;
import com.example.debtspace.config.Configuration;
import com.example.debtspace.main.interfaces.OnUpdateDataListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
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
        String date = new SimpleDateFormat("yyyy/mm/dd", Locale.getDefault()).format(new Date());
        map.put(Configuration.DATE_KEY, date);

        mDatabase.collection(Configuration.NOTIFICATIONS_COLLECTION_NAME)
                .document(username)
                .collection(Configuration.FRIENDS_COLLECTION_NAME)
                .document(mUsername)
                .set(map)
                .addOnSuccessListener(aVoid ->
                        listener.onUpdateSuccessful())
                .addOnFailureListener(e ->
                        listener.onFailure(e.getMessage()));
    }

    public void checkExistenceFriends(String username, OnUpdateDataListener listener) {
        mDatabase.collection(Configuration.DEBTS_COLLECTION_NAME)
                .document(mUsername)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Map<String, Object> data = documentSnapshot.getData();
                    if (documentSnapshot.exists() && data != null && data.containsKey(username)) {
                        listener.onFailure("This user " + username + " is already a friend");
                    } else {
                        checkExistenceRequests(username, listener);
                    }
                })
                .addOnFailureListener(e ->
                        listener.onFailure(e.getMessage()));
    }

    private void checkExistenceRequests(String username, OnUpdateDataListener listener) {
        mDatabase.collection(Configuration.NOTIFICATIONS_COLLECTION_NAME)
                .document(username)
                .collection(Configuration.FRIENDS_COLLECTION_NAME)
                .document(mUsername)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        listener.onFailure("Friend request has already been sent to this user " + username);
                    } else {
                        sendFriendRequest(username, listener);
                    }
                })
                .addOnFailureListener(e ->
                        listener.onFailure(e.getMessage()));
    }
}
