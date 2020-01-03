package com.example.debtspace.main.repositories;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.debtspace.application.DebtSpaceApplication;
import com.example.debtspace.config.Configuration;
import com.example.debtspace.main.interfaces.OnDownloadDataListener;
import com.example.debtspace.main.interfaces.OnFindUserListener;
import com.example.debtspace.models.User;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProfileRepository {

    private FirebaseFirestore mDatabase;
    private StorageReference mStorage;
    private String mUsername;

    public ProfileRepository(Context context) {
        mDatabase = DebtSpaceApplication.from(context).getDatabase();
        mStorage = DebtSpaceApplication.from(context)
                .getStorage()
                .child(Configuration.USERS_COLLECTION_NAME);
        mUsername = DebtSpaceApplication.from(context).getUsername();
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
                            Log.d("#DS", "user != null");
                            //downloadImage(user, listener);
                            listener.onSuccessful(user);
                        }
                    } else {
                        Log.d("#DS", "in does not exist");
                        listener.onDoesNotExist();
                    }
                })
                .addOnFailureListener(e -> {
                            Log.d("#DS", Objects.requireNonNull(e.getMessage()));
                            listener.onFailure("Can't find user data for" + mUsername);
                        }
                );
    }

    public void downloadImage(OnDownloadDataListener<Uri> listener) {
        mStorage.child(mUsername)
                .getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    List<Uri> list = new ArrayList<>();
                    list.add(uri);
                    listener.onDownloadSuccessful(list);
                })
                .addOnFailureListener(e ->
                        useDefaultImage(listener)
                );
    }

    private void useDefaultImage(OnDownloadDataListener<Uri> listener) {
        mStorage.child(Configuration.DEFAULT_IMAGE_VALUE)
                .getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    List<Uri> list = new ArrayList<>();
                    list.add(uri);
                    listener.onDownloadSuccessful(list);
                })
                .addOnFailureListener(e ->
                        listener.onFailure(e.getMessage())
                );
    }
}
