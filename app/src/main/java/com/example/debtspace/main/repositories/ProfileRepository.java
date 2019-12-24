package com.example.debtspace.main.repositories;

import com.example.debtspace.config.Configuration;
import com.example.debtspace.main.interfaces.OnFindUserListener;
import com.example.debtspace.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

public class ProfileRepository {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseFirestore mDatabase;
    private StorageReference mStorage;
    private String mUsername;

    public ProfileRepository() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance()
                .getReference()
                .child(Configuration.USERS_COLLECTION_NAME);
        mUsername = Objects.requireNonNull(mFirebaseAuth
                .getCurrentUser())
                .getDisplayName();
    }

    public void downloadUserData(OnFindUserListener listener) {
        DocumentReference document = mDatabase
                .collection(Configuration.USERS_COLLECTION_NAME)
                .document(mUsername);

        document.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            downloadImage(user, listener);
                        }
                    } else {
                        listener.onDoesNotExist();
                    }
                })
                .addOnFailureListener(e ->
                        listener.onFailure(e.getMessage()));
    }

    private void downloadImage(User user, OnFindUserListener listener) {
        mStorage.child(user.getUsername())
                .getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    user.setUriImage(uri);
                    listener.onSuccessful(user);
                })
                .addOnFailureListener(e ->
                        useDefaultImage(user, listener)
                );
    }

    private void useDefaultImage(User user, OnFindUserListener listener) {
        mStorage.child(Configuration.DEFAULT_IMAGE_VALUE)
                .getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    user.setUriImage(uri);
                    listener.onSuccessful(user);
                })
                .addOnFailureListener(e ->
                        listener.onFailure(e.getMessage())
                );
    }
}
