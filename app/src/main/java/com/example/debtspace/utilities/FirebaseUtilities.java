package com.example.debtspace.utilities;

import com.example.debtspace.config.Configuration;
import com.example.debtspace.main.interfaces.OnFindUserListener;
import com.example.debtspace.main.interfaces.OnGetFirestoreDataListener;
import com.example.debtspace.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class FirebaseUtilities {

    public static void findUserByUsername(String username, OnFindUserListener onFindUserListener) {
        FirebaseFirestore.getInstance()
                .collection(Configuration.USERS_COLLECTION_NAME)
                .document(username)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        onFindUserListener.onSuccessful(user);
                    } else {
                        onFindUserListener.onDoesNotExist();
                    }
                })
                .addOnFailureListener(e -> onFindUserListener.onFailure(e.getMessage()));
    }

    public static void getDataFromDatabase(Query query, OnGetFirestoreDataListener listener) {
        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Map<String, Map<String, Object>>> list = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Map<String, Map<String, Object>> map = new HashMap<>();
                        map.put(document.getId(), document.getData());
                        list.add(map);
                    }

                    try {
                        listener.onGetSuccessful(list);
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                })
                .addOnFailureListener(e ->
                        listener.onFailure(e.getMessage()));
    }

    public static void firebaseSignOut() {
        FirebaseAuth.getInstance().signOut();
    }
}
