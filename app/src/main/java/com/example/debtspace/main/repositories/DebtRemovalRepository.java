package com.example.debtspace.main.repositories;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.debtspace.application.DebtSpaceApplication;
import com.example.debtspace.config.AppConfig;
import com.example.debtspace.config.ErrorsConfig;
import com.example.debtspace.main.interfaces.OnFindUserListener;
import com.example.debtspace.main.interfaces.OnUpdateDataListener;
import com.example.debtspace.models.DebtRequest;
import com.example.debtspace.models.User;
import com.example.debtspace.utilities.FirebaseUtilities;
import com.example.debtspace.utilities.StringUtilities;
import com.google.firebase.firestore.FirebaseFirestore;

public class DebtRemovalRepository {

    private FirebaseFirestore mDatabase;
    private String mUsername;

    public DebtRemovalRepository(Context context) {
        mDatabase = DebtSpaceApplication.from(context).getDatabase();
        mUsername = DebtSpaceApplication.from(context).getUsername();
    }

    public void sendDebtRequest(DebtRequest request, Context context, OnUpdateDataListener listener) {
        getUserName(request, context, listener);
    }

    private void send(DebtRequest request, OnUpdateDataListener listener) {
        String username = request.getUsername();
        request.setUsername(mUsername);
        String date = StringUtilities.getCurrentDateAndTime();
        request.setDate(date);
        mDatabase.collection(AppConfig.NOTIFICATIONS_COLLECTION_NAME)
                .document(username)
                .collection(AppConfig.DEBTS_COLLECTION_NAME)
                .document(request.getId())
                .set(request)
                .addOnSuccessListener(aVoid ->
                        listener.onUpdateSuccessful())
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfig.ERROR_SEND_DEBT_REQUEST + username);
                });
    }

    private void getUserName(DebtRequest request, Context context, OnUpdateDataListener listener) {
        SharedPreferences preferences = context.getSharedPreferences(mUsername, Context.MODE_PRIVATE);
        String name = preferences.getString(AppConfig.NAME_KEY, null);
        if (name != null) {
            request.setName(name);
            send(request, listener);
        } else {
            FirebaseUtilities.findUserByUsername(mUsername, new OnFindUserListener() {
                @Override
                public void onSuccessful(User user) {
                    request.setName(user.getFullName());
                    send(request, listener);
                }

                @Override
                public void onDoesNotExist() {
                    request.setName(mUsername);
                    send(request, listener);
                }

                @Override
                public void onFailure(String errorMessage) {
                    request.setName(mUsername);
                    send(request, listener);
                }
            });
        }
    }
}
