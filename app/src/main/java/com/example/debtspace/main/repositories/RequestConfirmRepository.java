package com.example.debtspace.main.repositories;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.example.debtspace.application.DebtSpaceApplication;
import com.example.debtspace.config.Configuration;
import com.example.debtspace.config.ErrorsConfiguration;
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
        mDebts = mDatabase.collection(Configuration.DEBTS_COLLECTION_NAME);
        mRequests = mDatabase.collection(Configuration.NOTIFICATIONS_COLLECTION_NAME);

        mAmount = 3;
        mCount = 0;
    }

    public void acceptFriendRequest(String username, OnUpdateDataListener listener) {
        @SuppressLint("SimpleDateFormat")
        String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime());
        Map<String, Object> data = new HashMap<>();
        data.put(Configuration.DATE_KEY, date);
        data.put(Configuration.DEBT_KEY, Configuration.DEFAULT_DEBT_VALUE);

        setData(data, mUsername, username, listener);
        setData(data, username, mUsername, listener);

        deleteNotificationData(username, listener);
    }

    private void setData(Map<String, Object> data, String toWhom, String fromWhom, OnUpdateDataListener listener) {
        mDebts.document(toWhom)
                .collection(Configuration.FRIENDS_COLLECTION_NAME)
                .document(fromWhom)
                .set(data)
                .addOnSuccessListener(aVoid ->
                        readinessCheck(listener))
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(Configuration.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfiguration.ERROR_UPLOAD_DEBT_DATA + fromWhom);
                });
    }

    public void rejectFriendRequest(String username, OnUpdateDataListener listener) {
        deleteNotificationData(username, listener);
    }

    private void deleteNotificationData(String username, OnUpdateDataListener listener) {
        mRequests.document(mUsername)
                .collection(Configuration.FRIENDS_COLLECTION_NAME)
                .document(username)
                .delete()
                .addOnSuccessListener(aVoid ->
                        readinessCheck(listener))
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(Configuration.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfiguration.ERROR_DELETE_REQUEST + username);
                });
    }

    private void readinessCheck(OnUpdateDataListener listener) {
        mCount++;
        if (mCount == mAmount) {
            listener.onUpdateSuccessful();
        }
    }
}
