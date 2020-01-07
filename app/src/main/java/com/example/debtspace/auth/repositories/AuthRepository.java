package com.example.debtspace.auth.repositories;

import android.util.Log;

import com.example.debtspace.auth.interfaces.OnAuthProgressListener;
import com.example.debtspace.config.Configuration;
import com.example.debtspace.config.ErrorsConfiguration;
import com.example.debtspace.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class AuthRepository {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseFirestore mDatabase;

    public AuthRepository() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();
    }

    public void signIn(String email, String password, OnAuthProgressListener listener) {
        mFirebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult ->
                        listener.onSuccessful())
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(Configuration.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfiguration.ERROR_SIGN_IN);
                });
    }

    public void signUp(String firstName, String lastName,
                       String username, String email, String password,
                       OnAuthProgressListener listener) {

        mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult ->
                        createUser(firstName, lastName, username, listener))
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(Configuration.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfiguration.ERROR_SIGN_UP);
                });
    }

    private void createUser(String firstName, String lastName, String username,
                            OnAuthProgressListener listener) {

        User user = new User(firstName, lastName, username, Configuration.DEFAULT_DEBT_VALUE);
        mDatabase.collection(Configuration.USERS_COLLECTION_NAME)
                .document(username)
                .set(user)
                .addOnSuccessListener(voidTask ->
                        setDisplayName(username, listener))
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(Configuration.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfiguration.ERROR_CREATE_USER);
                });
    }

    private void setDisplayName(String username, OnAuthProgressListener listener) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest
                .Builder()
                .setDisplayName(username)
                .build();

        Objects.requireNonNull(mFirebaseAuth.getCurrentUser()).updateProfile(profileUpdates)
                .addOnSuccessListener(aVoid ->
                        listener.onSuccessful())
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(Configuration.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfiguration.ERROR_SAVE_USERNAME);
                });
    }

    public FirebaseAuth getFirebaseAuth() {
        return mFirebaseAuth;
    }
}
