package com.example.debtspace.main.repositories;

import android.content.Context;
import android.util.Log;

import com.example.debtspace.application.DebtSpaceApplication;
import com.example.debtspace.config.AppConfig;
import com.example.debtspace.config.ErrorsConfig;
import com.example.debtspace.main.interfaces.OnUpdateDataListener;
import com.example.debtspace.models.DebtRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DebtRemovalConfirmRepository {

    private FirebaseFirestore mDatabase;
    private CollectionReference mDebts;
    private CollectionReference mRequests;
    private String mUsername;

    private int mAmount;
    private int mCount;

    public DebtRemovalConfirmRepository(Context context) {
        mDatabase = DebtSpaceApplication.from(context).getDatabase();
        mUsername = DebtSpaceApplication.from(context).getUsername();
        mDebts = mDatabase.collection(AppConfig.DEBTS_COLLECTION_NAME);
        mRequests = mDatabase.collection(AppConfig.NOTIFICATIONS_COLLECTION_NAME);

        mCount = 0;
    }

    public void accept(DebtRequest request, OnUpdateDataListener listener) {
        mAmount = 7;
        doRemoval(request, listener);
        deleteNotificationData(request.getId(), listener);
    }

    private void doRemoval(DebtRequest request, OnUpdateDataListener listener) {
        mDebts.document(mUsername)
                .collection(AppConfig.FRIENDS_COLLECTION_NAME)
                .get()
                .addOnSuccessListener(documents -> {
                    String username = request.getUsername();
                    for (DocumentSnapshot document : documents) {
                        if (document.getId().equals(username)) {
                            Map<String, Object> data = document.getData();
                            if (data != null) {
                                debtRecount(data, request, listener);
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

    private void debtRecount(Map<String, Object> data, DebtRequest request, OnUpdateDataListener listener) {
        String username = request.getUsername();
        String debt = request.getDebt();
        double lastDebt = Double.parseDouble(Objects.requireNonNull(data.get(AppConfig.DEBT_KEY)).toString());
        double debtRequest = Double.parseDouble(debt);

        double debtCurrentUser = lastDebt + debtRequest;
        double debtFriend = -lastDebt - debtRequest;

        /*if (debtRequest > 0) {

        } else {
            double debtCurrentUser = lastDebt + debtRequest;
            double debtFriend = -lastDebt - debtRequest;
        }*/

        Map<String, Object> updatedCurrentUser = new HashMap<>();
        updatedCurrentUser.put(AppConfig.DEBT_KEY, Double.toString(debtCurrentUser));
        updatedCurrentUser.put(AppConfig.DATE_KEY, request.getDate());
        updateData(updatedCurrentUser, mUsername, username, listener);

        Map<String, Object> updatedFriend = new HashMap<>();
        updatedFriend.put(AppConfig.DEBT_KEY, Double.toString(debtFriend));
        updatedFriend.put(AppConfig.DATE_KEY, request.getDate());
        updateData(updatedFriend, username, mUsername, listener);


        updateScore(mUsername, debt, listener);
        updateScore(username, Double.toString(-debtRequest), listener);

        String id = request.getId();
        removeHistoryItem(id, mUsername, username, listener);
        removeHistoryItem(id, username, mUsername, listener);
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

    private void updateData(Map<String, Object> updated, String toWhom, String fromWhom, OnUpdateDataListener listener) {
        mDebts.document(toWhom)
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

    private void removeHistoryItem(String id, String toWhom, String fromWhom, OnUpdateDataListener listener) {
        mDatabase.collection(AppConfig.HISTORY_COLLECTION_NAME)
                .document(toWhom)
                .collection(AppConfig.DATES_COLLECTION_NAME)
                .document(id)
                .delete()
                .addOnSuccessListener(aVoid ->
                        readinessCheck(listener))
                .addOnFailureListener(e -> {
                    readinessCheck(listener);
                });
    }

    public void reject(String id, OnUpdateDataListener listener) {
        mAmount = 1;
        deleteNotificationData(id, listener);
    }

    private void deleteNotificationData(String id, OnUpdateDataListener listener) {
        Log.d("#DS", id);
        mRequests.document(mUsername)
                .collection(AppConfig.DEBTS_COLLECTION_NAME)
                .document(id)
                .delete()
                .addOnSuccessListener(aVoid ->
                        readinessCheck(listener))
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfig.ERROR_DELETE_FRIEND_REQUEST + id);
                });
    }

    private void readinessCheck(OnUpdateDataListener listener) {
        mCount++;
        if (mCount == mAmount) {
            listener.onUpdateSuccessful();
        }
    }
}
