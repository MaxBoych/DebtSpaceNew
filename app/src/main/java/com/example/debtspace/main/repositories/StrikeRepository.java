package com.example.debtspace.main.repositories;

import android.content.Context;
import android.util.Log;

import com.example.debtspace.application.DebtSpaceApplication;
import com.example.debtspace.config.AppConfig;
import com.example.debtspace.config.ErrorsConfig;
import com.example.debtspace.main.interfaces.OnFindUserListener;
import com.example.debtspace.main.interfaces.OnUpdateDataListener;
import com.example.debtspace.models.HistoryItem;
import com.example.debtspace.models.User;
import com.example.debtspace.utilities.FirebaseUtilities;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class StrikeRepository {

    private FirebaseFirestore mDatabase;
    private String mUsername;
    private int mRequestAmount;
    private int mCount;

    public StrikeRepository(Context context) {
        mDatabase = DebtSpaceApplication.from(context).getDatabase();
        mUsername = DebtSpaceApplication.from(context).getUsername();
        mRequestAmount = 6;
        mCount = 0;
    }

    public void doStrike(HistoryItem item, OnUpdateDataListener listener) {
        mDatabase.collection(AppConfig.DEBTS_COLLECTION_NAME)
                .document(mUsername)
                .collection(AppConfig.FRIENDS_COLLECTION_NAME)
                .get()
                .addOnSuccessListener(documents -> {
                    String username = item.getUsername();
                    for (DocumentSnapshot document : documents) {
                        if (document.getId().equals(username)) {
                            Map<String, Object> data = document.getData();
                            if (data != null) {
                                debtRecount(data, item, listener);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfig.ERROR_DO_STRIKE);
                });
    }

    private void debtRecount(Map<String, Object> data, HistoryItem item, OnUpdateDataListener listener) {
        String username = item.getUsername();
        String debt = item.getDebt();
        double lastDebt = Double.parseDouble(Objects.requireNonNull(data.get(AppConfig.DEBT_KEY)).toString());
        double debtRequest = Double.parseDouble(debt);

        double debtCurrentUser = lastDebt - debtRequest;
        double debtFriend = -lastDebt + debtRequest;

        Map<String, Object> updatedCurrentUser = new HashMap<>();
        updatedCurrentUser.put(AppConfig.DEBT_KEY, Double.toString(debtCurrentUser));
        updatedCurrentUser.put(AppConfig.DATE_KEY, item.getDate());
        updateData(updatedCurrentUser, mUsername, username, listener);

        Map<String, Object> updatedFriend = new HashMap<>();
        updatedFriend.put(AppConfig.DEBT_KEY, Double.toString(debtFriend));
        updatedFriend.put(AppConfig.DATE_KEY, item.getDate());
        updateData(updatedFriend, username, mUsername, listener);

        updateScore(mUsername, AppConfig.MINUS_STRING + debt, listener);
        updateScore(username, debt, listener);

        String id = UUID.randomUUID().toString();
        HistoryItem historyCurrentUser = new HistoryItem(Double.toString(-debtRequest),
                item.getComment(), item.getDate(), username);
        uploadDebt(id, mUsername, username, historyCurrentUser, listener);

        HistoryItem historyFriend = new HistoryItem(debt,
                item.getComment(), item.getDate(), mUsername);
        uploadDebt(id, username, mUsername, historyFriend, listener);


    }

    private void updateScore(String username, String debt, OnUpdateDataListener listener) {
        mDatabase.collection(AppConfig.USERS_COLLECTION_NAME)
                .document(username)
                .get()
                .addOnSuccessListener(document -> {
                    Map<String, Object> data = document.getData();
                    if (data != null) {
                        String lastScore = (String) data.get(AppConfig.SCORE_KEY);
                        if (lastScore != null) {
                            String newScore = Double.toString(Double.parseDouble(lastScore) + Double.parseDouble(debt));
                            setNewScore(username, newScore, listener);
                        } else {
                            listener.onFailure(ErrorsConfig.ERROR_UPDATE_SCORE + username);
                        }
                    } else {
                        listener.onFailure(ErrorsConfig.ERROR_UPDATE_SCORE + username);
                    }
                })
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfig.ERROR_UPDATE_SCORE + username);
                });
    }

    private void setNewScore(String username, String score, OnUpdateDataListener listener) {
        mDatabase.collection(AppConfig.USERS_COLLECTION_NAME)
                .document(username)
                .update(AppConfig.SCORE_FIELD_NAME, score)
                .addOnSuccessListener(aVoid ->
                        readinessCheck(listener))
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfig.ERROR_SET_NEW_SCORE + username);
                });

    }

    private void uploadDebt(String id, String toWhom, String fromWhom, HistoryItem item, OnUpdateDataListener listener) {
        FirebaseUtilities.findUserByUsername(fromWhom, new OnFindUserListener() {
            @Override
            public void onSuccessful(User user) {
                item.setName(user.getFirstName() + " " + user.getLastName());
                sendDebtToHistory(id, toWhom, fromWhom, item, listener);
            }

            @Override
            public void onDoesNotExist() {
                sendDebtToHistory(id, toWhom, fromWhom, item, listener);
            }

            @Override
            public void onFailure(String errorMessage) {
                sendDebtToHistory(id, toWhom, fromWhom, item, listener);
            }
        });
    }

    private void updateData(Map<String, Object> updated, String toWhom, String fromWhom, OnUpdateDataListener listener) {
        mDatabase.collection(AppConfig.DEBTS_COLLECTION_NAME)
                .document(toWhom)
                .collection(AppConfig.FRIENDS_COLLECTION_NAME)
                .document(fromWhom)
                .update(updated)
                .addOnSuccessListener(aVoid ->
                        readinessCheck(listener))
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfig.ERROR_UPLOAD_DEBT_DATA + fromWhom);
                });
    }

    private void sendDebtToHistory(String id, String toWhom, String fromWhom, HistoryItem item, OnUpdateDataListener listener) {
        mDatabase.collection(AppConfig.HISTORY_COLLECTION_NAME)
                .document(toWhom)
                .collection(AppConfig.DATES_COLLECTION_NAME)
                .document(id)
                .set(item)
                .addOnSuccessListener(aVoid ->
                        readinessCheck(listener))
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfig.ERROR_UPLOAD_REQUESTS + fromWhom);
                });
    }

    private void readinessCheck(OnUpdateDataListener listener) {
        mCount++;
        if (mCount == mRequestAmount) {
            listener.onUpdateSuccessful();
        }
    }
}
