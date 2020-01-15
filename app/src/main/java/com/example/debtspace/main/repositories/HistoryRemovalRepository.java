package com.example.debtspace.main.repositories;

import android.content.Context;
import android.util.Log;

import com.example.debtspace.application.DebtSpaceApplication;
import com.example.debtspace.config.AppConfig;
import com.example.debtspace.config.ErrorsConfig;
import com.example.debtspace.main.interfaces.OnUpdateDataListener;
import com.example.debtspace.models.DebtRequest;
import com.example.debtspace.utilities.StringUtilities;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.atomic.AtomicLong;

public class HistoryRemovalRepository {

    private FirebaseFirestore mDatabase;
    private String mUsername;

    public HistoryRemovalRepository(Context context) {
        mDatabase = DebtSpaceApplication.from(context).getDatabase();
        mUsername = DebtSpaceApplication.from(context).getUsername();
    }

    public void clearWholeHistory(OnUpdateDataListener listener) {
        AtomicLong deleted = new AtomicLong();
        long batchSize = AppConfig.BATCH_SIZE;
        mDatabase.collection(AppConfig.HISTORY_COLLECTION_NAME)
                .document(mUsername)
                .collection(AppConfig.DATES_COLLECTION_NAME)
                .limit(batchSize)
                .get()
                .addOnSuccessListener(documents -> {
                    for (DocumentSnapshot document : documents) {
                        document.getReference().delete();
                        deleted.getAndIncrement();
                    }

                    if (deleted.get() >= batchSize) {
                        clearWholeHistory(listener);
                    }
                })
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfig.ERROR_CLEAR_WHOLE_HISTORY);
                });
    }
}
