package com.example.debtspace.main.repositories;

import com.example.debtspace.config.Configuration;
import com.example.debtspace.main.interfaces.OnUpdateDataListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FriendRequestRepository {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseFirestore mDatabase;
    private String mUsername;

    public FriendRequestRepository() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();

        mUsername = Objects.requireNonNull(Objects.requireNonNull(mFirebaseAuth
                .getCurrentUser())
                .getDisplayName());
    }

    private void sendFriendRequest(String username, OnUpdateDataListener listener) {

        Map<String, String> map = new HashMap<>();
        map.put(Configuration.DATE_KEY, "yyyy/mm/dd hh:mm:ss");

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
