package com.example.debtspace.main.repositories;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.debtspace.application.DebtSpaceApplication;
import com.example.debtspace.config.Configuration;
import com.example.debtspace.main.interfaces.OnFindUserListener;
import com.example.debtspace.main.interfaces.OnUpdateDataListener;
import com.example.debtspace.models.HistoryItem;
import com.example.debtspace.models.User;
import com.example.debtspace.utilities.FirebaseUtilities;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class StrikeRepository {

    private FirebaseFirestore mDatabase;
    private String mUsername;
    private int mRequestAmount;
    private int mCount;

    public StrikeRepository(Context context) {
        mDatabase = DebtSpaceApplication.from(context).getDatabase();

        mUsername = DebtSpaceApplication.from(context).getUsername();

        mRequestAmount = 4;
        mCount = 0;
    }

    public void doStrike(HistoryItem item, OnUpdateDataListener listener) {
        DocumentReference profile = mDatabase.collection(Configuration.USERS_COLLECTION_NAME)
                .document(mUsername);

        profile.get()
                .addOnSuccessListener(documentSnapshot -> {
                    Map<String, Object> data = documentSnapshot.getData();
                    if (data != null) {
                        Double debt = Double.parseDouble(item.getDebt());
                        Double totalDebt = Double.parseDouble(Objects.requireNonNull(data.get("score")).toString());
                        totalDebt += debt;
                        data.put("score", totalDebt.toString());
                        profile.set(data);
                        DocumentReference profile1 = mDatabase.collection(Configuration.USERS_COLLECTION_NAME)
                                .document(item.getUsername());
                        profile1.get()
                                .addOnSuccessListener(documentSnapshot1 -> {
                                    Map<String, Object> data1 = documentSnapshot1.getData();
                                    if(data1 != null) {
                                        Double debt1 = Double.parseDouble(item.getDebt());
                                        Double totalDebt1 = Double.parseDouble(Objects.requireNonNull(data1.get("score")).toString());
                                        totalDebt1 -= debt1;
                                        data1.put("score", totalDebt1.toString());
                                        profile1.set(data1);
                                    }
                                });
                    }
                })
                .addOnFailureListener(e ->
                        listener.onFailure(e.getMessage())
                );

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



            HistoryItem historyCurrentUser = new HistoryItem(Double.toString(-debtRequest),
                    item.getComment(), item.getDate());

            FirebaseUtilities.findUserByUsername(username, new OnFindUserListener() {
                @Override
                public void onSuccessful(User user) {
                    historyCurrentUser.setName(user.getFirstName() + " " + user.getLastName());
                    sendDebtToHistory(mUsername, historyCurrentUser, listener);
                }

                @Override
                public void onDoesNotExist() {
                    sendDebtToHistory(mUsername, historyCurrentUser, listener);
                }

                @Override
                public void onFailure(String errorMessage) {
                    sendDebtToHistory(mUsername, historyCurrentUser, listener);
                }
            });

            HistoryItem historyFriend = new HistoryItem(debt,
                    item.getComment(), item.getDate());

            FirebaseUtilities.findUserByUsername(mUsername, new OnFindUserListener() {
                @Override
                public void onSuccessful(User user) {
                    historyFriend.setName(user.getFirstName() + " " + user.getLastName());
                    sendDebtToHistory(username, historyFriend, listener);
                }

                @Override
                public void onDoesNotExist() {
                    sendDebtToHistory(username, historyFriend, listener);
                }

                @Override
                public void onFailure(String errorMessage) {
                    sendDebtToHistory(username, historyFriend, listener);
                }
            });
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
