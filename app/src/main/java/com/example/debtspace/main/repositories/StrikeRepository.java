package com.example.debtspace.main.repositories;

import com.example.debtspace.config.Configuration;
import com.example.debtspace.main.interfaces.OnUpdateDataListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class StrikeRepository {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseFirestore mDatabase;
    private String mUsername;
    private int mCount;

    public StrikeRepository() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();

        mUsername = Objects.requireNonNull(Objects.requireNonNull(mFirebaseAuth
                .getCurrentUser())
                .getDisplayName());
    }

    public void doStrike(String username, String debt, OnUpdateDataListener listener) {
        DocumentReference document = mDatabase.collection(Configuration.DEBTS_COLLECTION_NAME)
                .document(mUsername);

        document.get()
                .addOnSuccessListener(documentSnapshot -> {
                    Map<String, Object> data = documentSnapshot.getData();
                    if (data != null) {
                        debtRecount(data, username, debt, listener);
                    }
                })
                .addOnFailureListener(e ->
                        listener.onFailure(e.getMessage())
                );
    }

    private void debtRecount(Map<String, Object> data, String username,
                             String debtRequest, OnUpdateDataListener listener) {
        if (data.containsKey(username)) {
            Map<String, Object> updatedCurrentUser = new HashMap<>();
            double lastDebt = Double.parseDouble(Objects.requireNonNull(data.get(username)).toString());
            double newDebt = Double.parseDouble(debtRequest);

            double debtCurrentUser = lastDebt - newDebt;
            double debtFriend = -lastDebt + newDebt;
            updatedCurrentUser.put(username, Double.toString(debtCurrentUser));

            Map<String, Object> updatedFriend = new HashMap<>();
            updatedFriend.put(mUsername, Double.toString(debtFriend));

            updateData(updatedCurrentUser, mUsername, listener);
            updateData(updatedFriend, username, listener);
        } else {
            listener.onFailure("Cannot recount debt: user \"" + username + "\" is not found");
        }
    }

    private void updateData(Map<String, Object> updated, String username, OnUpdateDataListener listener) {

        mDatabase.collection(Configuration.DEBTS_COLLECTION_NAME)
                .document(username)
                .update(updated)
                .addOnSuccessListener(aVoid -> {
                    mCount++;
                    if (mCount == 2) {
                        listener.onUpdateSuccessful();
                    }
                })
                .addOnFailureListener(e ->
                        listener.onFailure(e.getMessage())
                );
    }
}
