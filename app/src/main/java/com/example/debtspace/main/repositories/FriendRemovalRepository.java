package com.example.debtspace.main.repositories;

import android.content.Context;
import android.util.Log;

import com.example.debtspace.application.DebtSpaceApplication;
import com.example.debtspace.config.AppConfig;
import com.example.debtspace.config.ErrorsConfig;
import com.example.debtspace.main.interfaces.OnUpdateDataListener;
import com.google.firebase.firestore.FirebaseFirestore;

public class FriendRemovalRepository {

    private FirebaseFirestore mDatabase;
    private String mUsername;

    private int mSize;
    private int mCount;

    public FriendRemovalRepository(Context context) {
        mDatabase = DebtSpaceApplication.from(context).getDatabase();
        mUsername = DebtSpaceApplication.from(context).getUsername();

        mSize = 2;
        mCount = 0;
    }

    public void removeFriend(String username, OnUpdateDataListener listener) {
        removeFriend(mUsername, username, listener);
        removeFriend(username, mUsername, listener);
    }

    private void removeFriend(String who, String whom, OnUpdateDataListener listener) {
        mDatabase.collection(AppConfig.DEBTS_COLLECTION_NAME)
                .document(who)
                .collection(AppConfig.FRIENDS_COLLECTION_NAME)
                .document(whom)
                .delete()
                .addOnSuccessListener(aVoid ->
                        readinessCheck(listener))
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfig.ERROR_REMOVE_FRIEND + whom);
                });
    }

    private void readinessCheck(OnUpdateDataListener listener) {
        mCount++;
        if (mCount == mSize) {
            listener.onUpdateSuccessful();
        }
    }
}
