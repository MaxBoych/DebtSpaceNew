package com.example.debtspace.main.repositories;

import com.example.debtspace.config.Configuration;
import com.example.debtspace.main.interfaces.OnUpdateDataListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RequestConfirmRepository {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseFirestore mDatabase;
    private CollectionReference mDebts;
    private String mUsername;

    private int mAmount;
    private int mCount;

    public RequestConfirmRepository() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();
        mDebts = mDatabase.collection(Configuration.DEBTS_COLLECTION_NAME);

        mUsername = Objects.requireNonNull(Objects.requireNonNull(mFirebaseAuth
                .getCurrentUser())
                .getDisplayName());

        mAmount = 3;
        mCount = 0;
    }

    public void acceptFriendRequest(String username, OnUpdateDataListener listener) {
        Map<String, Object> dataCurrentUser = new HashMap<>();
        dataCurrentUser.put(username, Configuration.DEFAULT_DEBT_VALUE);
        updateData(dataCurrentUser, mUsername, listener);

        Map<String, Object> dataFriend = new HashMap<>();
        dataFriend.put(mUsername, Configuration.DEFAULT_DEBT_VALUE);
        updateData(dataFriend, username, listener);

        deleteNotificationData(username, listener);
    }

    private void checkDebts(Map<String, Object> data, String username, OnUpdateDataListener listener) {
        mDebts.document(username)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        updateData(data, username, listener);
                    } else {
                        setData(data, username, listener);
                    }
                })
                .addOnFailureListener(e ->
                        listener.onFailure(e.getMessage())
                );
    }

    private void updateData(Map<String, Object> data, String username, OnUpdateDataListener listener) {
        mDebts.document(username)
                .update(data)
                .addOnSuccessListener(aVoid ->
                        readinessCheck(listener))
                .addOnFailureListener(e ->
                        listener.onFailure(e.getMessage())
                );
    }

    private void setData(Map<String, Object> data, String username, OnUpdateDataListener listener) {
        mDebts.document(username)
                .set(data)
                .addOnSuccessListener(aVoid ->
                        readinessCheck(listener))
                .addOnFailureListener(e ->
                        listener.onFailure(e.getMessage())
                );
    }

    public void rejectFriendRequest(String username, OnUpdateDataListener listener) {
        deleteNotificationData(username, listener);
    }

    private void deleteNotificationData(String username, OnUpdateDataListener listener) {
        mDatabase.collection(Configuration.NOTIFICATIONS_COLLECTION_NAME)
                .document(mUsername)
                .collection(Configuration.FRIENDS_COLLECTION_NAME)
                .document(username)
                .delete()
                .addOnSuccessListener(aVoid ->
                        readinessCheck(listener))
                .addOnFailureListener(e ->
                        listener.onFailure(e.getMessage())
                );
    }

    private void readinessCheck(OnUpdateDataListener listener) {
        mCount++;
        if (mCount == mAmount) {
            listener.onUpdateSuccessful();
        }
    }
}
