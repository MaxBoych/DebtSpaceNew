package com.example.debtspace.main.repositories;

import com.example.debtspace.config.Configuration;
import com.example.debtspace.main.interfaces.OnGetFirestoreDataListener;
import com.example.debtspace.utilities.FirebaseUtilities;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class UserSearchListRepository {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseFirestore mDatabase;

    public UserSearchListRepository() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();
    }

    public void getUsersBySubstring(String string, OnGetFirestoreDataListener onGetFirestoreDataListener) {
        Query query = mDatabase.collection(Configuration.USERS_COLLECTION_NAME)
                .orderBy(Configuration.USERNAME_FIELD_NAME)
                .startAt(string.trim())
                .endAt(string.trim() + "\uf8ff");

        FirebaseUtilities.getDataFromDatabase(query, onGetFirestoreDataListener);
    }
}
