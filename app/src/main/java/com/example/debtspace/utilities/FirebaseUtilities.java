package com.example.debtspace.utilities;

import android.util.Log;

import com.example.debtspace.config.AppConfig;
import com.example.debtspace.config.ErrorsConfig;
import com.example.debtspace.main.interfaces.OnFindUserListener;
import com.example.debtspace.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseUtilities {

    public static void findUserByUsername(String username, OnFindUserListener onFindUserListener) {
        FirebaseFirestore.getInstance()
                .collection(AppConfig.USERS_COLLECTION_NAME)
                .document(username)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        onFindUserListener.onSuccessful(user);
                    } else {
                        Log.w(AppConfig.APPLICATION_LOG_TAG, ErrorsConfig.WARNING_USER_DOES_NOT_EXIST + username);
                        onFindUserListener.onDoesNotExist();
                    }
                })
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    onFindUserListener.onFailure(ErrorsConfig.ERROR_FIND_USER + username);
                });
    }

    public static void firebaseSignOut() {
        FirebaseAuth.getInstance().signOut();
    }
}
