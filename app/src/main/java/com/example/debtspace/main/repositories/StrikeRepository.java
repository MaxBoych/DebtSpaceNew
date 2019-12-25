package com.example.debtspace.main.repositories;

import com.example.debtspace.config.Configuration;
import com.example.debtspace.main.interfaces.OnUpdateDataListener;
import com.example.debtspace.models.HistoryItem;
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
    private int mRequestAmount;
    private int mCount;

    public StrikeRepository() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();

        mUsername = Objects.requireNonNull(Objects.requireNonNull(mFirebaseAuth
                .getCurrentUser())
                .getDisplayName());

        mRequestAmount = 4;
        mCount = 0;
    }

    public void doStrike(HistoryItem item, OnUpdateDataListener listener) {
        DocumentReference document = mDatabase.collection(Configuration.DEBTS_COLLECTION_NAME)
                .document(mUsername);

        document.get()
                .addOnSuccessListener(documentSnapshot -> {
                    Map<String, Object> data = documentSnapshot.getData();
                    if (data != null) {
                        debtRecount(data, item, listener);
                    }
                })
                .addOnFailureListener(e ->
                        listener.onFailure(e.getMessage())
                );
    }

    private void debtRecount(Map<String, Object> data, HistoryItem item, OnUpdateDataListener listener) {
        String username = item.getUsername();
        String debt = item.getDebt();
        if (data.containsKey(username)) {
            double lastDebt = Double.parseDouble(Objects.requireNonNull(data.get(username)).toString());
            double debtRequest = Double.parseDouble(debt);

            double debtCurrentUser = lastDebt - debtRequest;
            double debtFriend = -lastDebt + debtRequest;

            Map<String, Object> updatedCurrentUser = new HashMap<>();
            updatedCurrentUser.put(username, Double.toString(debtCurrentUser));

            Map<String, Object> updatedFriend = new HashMap<>();
            updatedFriend.put(mUsername, Double.toString(debtFriend));

            updateData(updatedCurrentUser, mUsername, listener);
            updateData(updatedFriend, username, listener);

            HistoryItem historyCurrentUser = new HistoryItem(item.getUsername(), Double.toString(-debtRequest),
                    item.getComment(), item.getDate());

            HistoryItem historyFriend = new HistoryItem(mUsername, debt,
                    item.getComment(), item.getDate());

            sendDebtToHistory(mUsername, historyCurrentUser, listener);
            sendDebtToHistory(username, historyFriend, listener);
        } else {
            listener.onFailure("Cannot recount debt: user \"" + username + "\" is not found");
        }
    }

    private void updateData(Map<String, Object> updated, String username, OnUpdateDataListener listener) {
        mDatabase.collection(Configuration.DEBTS_COLLECTION_NAME)
                .document(username)
                .update(updated)
                .addOnSuccessListener(aVoid ->
                        readinessCheck(listener))
                .addOnFailureListener(e ->
                        listener.onFailure(e.getMessage())
                );
    }

    private void sendDebtToHistory(String username, HistoryItem data, OnUpdateDataListener listener) {
        mDatabase.collection(Configuration.HISTORY_COLLECTION_NAME)
                .document(username)
                .collection(Configuration.DATES_COLLECTION_NAME)
                .document()
                .set(data)
                .addOnSuccessListener(aVoid ->
                        readinessCheck(listener))
                .addOnFailureListener(e ->
                        listener.onFailure(e.getMessage())
                );
    }

    private void readinessCheck(OnUpdateDataListener listener) {
        mCount++;
        if (mCount == mRequestAmount) {
            listener.onUpdateSuccessful();
        }
    }
}
