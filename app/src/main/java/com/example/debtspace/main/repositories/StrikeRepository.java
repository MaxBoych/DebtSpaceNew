package com.example.debtspace.main.repositories;

import android.content.Context;
import android.util.Log;

import com.example.debtspace.application.DebtSpaceApplication;
import com.example.debtspace.config.Configuration;
import com.example.debtspace.config.ErrorsConfiguration;
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
        mDatabase.collection(Configuration.DEBTS_COLLECTION_NAME)
                .document(mUsername)
                .collection(Configuration.FRIENDS_COLLECTION_NAME)
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
                        Log.e(Configuration.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfiguration.ERROR_DO_STRIKE);
                });
    }

    private void debtRecount(Map<String, Object> data, HistoryItem item, OnUpdateDataListener listener) {
        String username = item.getUsername();
        String debt = item.getDebt();
        double lastDebt = Double.parseDouble(Objects.requireNonNull(data.get(Configuration.DEBT_KEY)).toString());
        double debtRequest = Double.parseDouble(debt);

        double debtCurrentUser = lastDebt - debtRequest;
        double debtFriend = -lastDebt + debtRequest;

        Map<String, Object> updatedCurrentUser = new HashMap<>();
        updatedCurrentUser.put(Configuration.DEBT_KEY, Double.toString(debtCurrentUser));
        updatedCurrentUser.put(Configuration.DATE_KEY, item.getDate());
        updateData(updatedCurrentUser, mUsername, username, listener);

        Map<String, Object> updatedFriend = new HashMap<>();
        updatedFriend.put(Configuration.DEBT_KEY, Double.toString(debtFriend));
        updatedFriend.put(Configuration.DATE_KEY, item.getDate());
        updateData(updatedFriend, username, mUsername, listener);

        updateScore(mUsername, Configuration.MINUS_STRING + debt, listener);
        updateScore(username, debt, listener);

        HistoryItem historyCurrentUser = new HistoryItem(Double.toString(-debtRequest),
                item.getComment(), item.getDate());
        uploadDebt(mUsername, username, historyCurrentUser, listener);

        HistoryItem historyFriend = new HistoryItem(debt,
                item.getComment(), item.getDate());
        uploadDebt(username, mUsername, historyFriend, listener);


    }

    private void updateScore(String username, String debt, OnUpdateDataListener listener) {
        mDatabase.collection(Configuration.USERS_COLLECTION_NAME)
                .document(username)
                .get()
                .addOnSuccessListener(document -> {
                    Map<String, Object> data = document.getData();
                    if (data != null) {
                        String lastScore = (String) data.get(Configuration.SCORE_KEY);
                        if (lastScore != null) {
                            String newScore = Double.toString(Double.parseDouble(lastScore) + Double.parseDouble(debt));
                            setNewScore(username, newScore, listener);
                        } else {
                            listener.onFailure(ErrorsConfiguration.ERROR_UPDATE_SCORE + username);
                        }
                    } else {
                        listener.onFailure(ErrorsConfiguration.ERROR_UPDATE_SCORE + username);
                    }
                })
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(Configuration.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfiguration.ERROR_UPDATE_SCORE + username);
                });
    }

    private void setNewScore(String username, String score, OnUpdateDataListener listener) {
        mDatabase.collection(Configuration.USERS_COLLECTION_NAME)
                .document(username)
                .update(Configuration.SCORE_FIELD_NAME, score)
                .addOnSuccessListener(aVoid ->
                        readinessCheck(listener))
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(Configuration.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfiguration.ERROR_SET_NEW_SCORE + username);
                });

    }

    private void uploadDebt(String toWhom, String fromWhom, HistoryItem data, OnUpdateDataListener listener) {
        FirebaseUtilities.findUserByUsername(fromWhom, new OnFindUserListener() {
            @Override
            public void onSuccessful(User user) {
                data.setName(user.getFirstName() + " " + user.getLastName());
                sendDebtToHistory(toWhom, fromWhom, data, listener);
            }

            @Override
            public void onDoesNotExist() {
                sendDebtToHistory(toWhom, fromWhom, data, listener);
            }

            @Override
            public void onFailure(String errorMessage) {
                sendDebtToHistory(toWhom, fromWhom, data, listener);
            }
        });
    }

    private void updateData(Map<String, Object> updated, String toWhom, String fromWhom, OnUpdateDataListener listener) {
        mDatabase.collection(Configuration.DEBTS_COLLECTION_NAME)
                .document(toWhom)
                .collection(Configuration.FRIENDS_COLLECTION_NAME)
                .document(fromWhom)
                .update(updated)
                .addOnSuccessListener(aVoid -> {
                    readinessCheck(listener);
                })
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(Configuration.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfiguration.ERROR_UPLOAD_DEBT_DATA + fromWhom);
                });
    }

    private void sendDebtToHistory(String toWhom, String fromWhom, HistoryItem data, OnUpdateDataListener listener) {
        mDatabase.collection(Configuration.HISTORY_COLLECTION_NAME)
                .document(toWhom)
                .collection(Configuration.DATES_COLLECTION_NAME)
                .document(fromWhom)
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    Log.d("#DS", "send to history successful");
                    readinessCheck(listener);
                })
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(Configuration.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfiguration.ERROR_UPLOAD_REQUESTS + fromWhom);
                });
    }

    private void readinessCheck(OnUpdateDataListener listener) {
        mCount++;
        if (mCount == mRequestAmount) {
            listener.onUpdateSuccessful();
        }
    }
}
