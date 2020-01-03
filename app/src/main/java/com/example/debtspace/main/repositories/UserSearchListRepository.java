package com.example.debtspace.main.repositories;

import android.content.Context;

import com.example.debtspace.application.DebtSpaceApplication;
import com.example.debtspace.config.Configuration;
import com.example.debtspace.main.interfaces.OnGetFirestoreDataListener;
import com.example.debtspace.utilities.FirebaseUtilities;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class UserSearchListRepository {

    private FirebaseFirestore mDatabase;
    private CollectionReference mUsers;

    public UserSearchListRepository(Context context) {
        mDatabase = DebtSpaceApplication.from(context).getDatabase();
        mUsers = mDatabase.collection(Configuration.USERS_COLLECTION_NAME);
    }

    public void getUsersBySubstring(String string, OnGetFirestoreDataListener onGetFirestoreDataListener) {
        Query query = mUsers.orderBy(Configuration.USERNAME_FIELD_NAME)
                .startAt(string.trim())
                .endAt(string.trim() + "\uf8ff");

        FirebaseUtilities.getDataFromDatabase(query, onGetFirestoreDataListener);
    }
}
