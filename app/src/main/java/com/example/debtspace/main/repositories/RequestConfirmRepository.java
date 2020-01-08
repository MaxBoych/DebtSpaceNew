package com.example.debtspace.main.repositories;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.example.debtspace.application.DebtSpaceApplication;
import com.example.debtspace.config.AppConfig;
import com.example.debtspace.config.ErrorsConfig;
import com.example.debtspace.main.interfaces.OnUpdateDataListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class RequestConfirmRepository {

    private FirebaseFirestore mDatabase;
    private CollectionReference mDebts;
    private CollectionReference mRequests;
    private String mUsername;

    private int mAmount;
    private int mCount;

    public RequestConfirmRepository(Context context) {
        mDatabase = DebtSpaceApplication.from(context).getDatabase();
        mUsername = DebtSpaceApplication.from(context).getUsername();
        mDebts = mDatabase.collection(AppConfig.DEBTS_COLLECTION_NAME);
        mRequests = mDatabase.collection(AppConfig.NOTIFICATIONS_COLLECTION_NAME);

        mAmount = 3;
        mCount = 0;
    }

    public void acceptFriendRequest(String username, OnUpdateDataListener listener) {
        @SuppressLint("SimpleDateFormat")
        String date = new SimpleDateFormat(AppConfig.PATTERN_DATE).format(Calendar.getInstance().getTime());
        Map<String, Object> data = new HashMap<>();
        data.put(AppConfig.DATE_KEY, date);
        data.put(AppConfig.DEBT_KEY, AppConfig.DEFAULT_DEBT_VALUE);

        setData(data, mUsername, username, listener);
        setData(data, username, mUsername, listener);

        deleteNotificationData(username, listener);
    }

    private void setData(Map<String, Object> data, String toWhom, String fromWhom, OnUpdateDataListener listener) {
        mDebts.document(toWhom)
                .collection(AppConfig.FRIENDS_COLLECTION_NAME)
                .document(fromWhom)
                .set(data)
                .addOnSuccessListener(aVoid ->
                        readinessCheck(listener))
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfig.ERROR_UPLOAD_DEBT_DATA + fromWhom);
                });
    }

    public void rejectFriendRequest(String username, OnUpdateDataListener listener) {
        deleteNotificationData(username, listener);
    }

    private void deleteNotificationData(String username, OnUpdateDataListener listener) {
        mRequests.document(mUsername)
                .collection(AppConfig.FRIENDS_COLLECTION_NAME)
                .document(username)
                .delete()
                .addOnSuccessListener(aVoid ->
                        readinessCheck(listener))
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfig.ERROR_DELETE_REQUEST + username);
                });
    }

    private void readinessCheck(OnUpdateDataListener listener) {
        mCount++;
        if (mCount == mAmount) {
            listener.onUpdateSuccessful();
        }
    }
}
