package com.example.debtspace.main.repositories;

import android.content.Context;
import android.util.Log;

import com.example.debtspace.application.DebtSpaceApplication;
import com.example.debtspace.config.Configuration;
import com.example.debtspace.config.ErrorsConfiguration;
import com.example.debtspace.main.interfaces.OnDatabaseEventListener;
import com.example.debtspace.main.interfaces.OnDownloadDataListListener;
import com.example.debtspace.models.HistoryItem;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HistoryRepository {

    private FirebaseFirestore mDatabase;
    private CollectionReference mHistory;
    private String mUsername;

    public HistoryRepository(Context context) {
        mDatabase = DebtSpaceApplication.from(context).getDatabase();
        mUsername = DebtSpaceApplication.from(context).getUsername();
        mHistory = mDatabase.collection(Configuration.HISTORY_COLLECTION_NAME)
                .document(mUsername)
                .collection(Configuration.DATES_COLLECTION_NAME);
    }

    public void downloadHistoryData(OnDownloadDataListListener<HistoryItem> listener) {
        List<HistoryItem> items = new ArrayList<>();
        mHistory.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Map<String, Object> data = document.getData();
                        items.add(new HistoryItem(data, document.getId()));
                    }
                    listener.onDownloadSuccessful(items);
                })
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(Configuration.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfiguration.ERROR_DOWNLOAD_HISTORY);
                });
    }

    public void observeEvents(OnDatabaseEventListener<HistoryItem> listener) {
        mHistory.addSnapshotListener((query, e) -> {
            if (e != null && e.getMessage() != null) {
                Log.e(Configuration.APPLICATION_LOG_TAG, e.getMessage());
                listener.onFailure(ErrorsConfiguration.ERROR_DATA_READING_HISTORY);
            } else if (query != null) {
                for (DocumentChange change : query.getDocumentChanges()) {
                    DocumentSnapshot document = change.getDocument();
                    Map<String, Object> data = document.getData();
                    if (data != null) {
                        HistoryItem item = new HistoryItem(data, document.getId());
                        if (change.getType() == DocumentChange.Type.ADDED) {
                            listener.onAdded(item);
                        }
                    }
                }
            }
        });
    }
}
