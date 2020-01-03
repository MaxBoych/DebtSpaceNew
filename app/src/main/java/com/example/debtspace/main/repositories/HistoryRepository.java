package com.example.debtspace.main.repositories;

import android.content.Context;

import com.example.debtspace.application.DebtSpaceApplication;
import com.example.debtspace.config.Configuration;
import com.example.debtspace.main.interfaces.OnDownloadDataListener;
import com.example.debtspace.models.HistoryItem;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryRepository {

    private FirebaseFirestore mDatabase;
    private String mUsername;

    public HistoryRepository(Context context) {
        mDatabase = DebtSpaceApplication.from(context).getDatabase();
        mUsername = DebtSpaceApplication.from(context).getUsername();
    }

    public void downloadHistoryData(OnDownloadDataListener<HistoryItem> listener) {
        List<HistoryItem> items = new ArrayList<>();

        mDatabase.collection(Configuration.HISTORY_COLLECTION_NAME)
                .document(mUsername)
                .collection(Configuration.DATES_COLLECTION_NAME)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Map<String, Object> map = document.getData();
                        Map<String, String> item = new HashMap<>();
                        for (String key : map.keySet()) {
                            item.put(key, (String) map.get(key));
                        }
                        items.add(new HistoryItem(item));
                    }
                    listener.onDownloadSuccessful(items);
                })
                .addOnFailureListener(e ->
                        listener.onFailure(e.getMessage()));
    }
}
