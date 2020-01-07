package com.example.debtspace.main.repositories;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.debtspace.application.DebtSpaceApplication;
import com.example.debtspace.config.Configuration;
import com.example.debtspace.config.ErrorsConfiguration;
import com.example.debtspace.main.interfaces.OnDownloadDataListListener;
import com.example.debtspace.main.interfaces.OnFindUserListener;
import com.example.debtspace.models.User;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageException;
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
                            listener.onSuccessful(user);
                        }
                    } else {
                        Log.w(Configuration.APPLICATION_LOG_TAG, ErrorsConfiguration.WARNING_USER_DOES_NOT_EXIST + mUsername);
                        listener.onDoesNotExist();
                    }
                })
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(Configuration.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfiguration.ERROR_DOWNLOAD_USER_DATA + mUsername);
                });
    }

    public void downloadImage(OnDownloadDataListListener<Uri> listener) {
        mStorage.child(mUsername)
                .getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    List<Uri> list = new ArrayList<>();
                    list.add(uri);
                    listener.onDownloadSuccessful(list);
                })
                .addOnFailureListener(e -> {
                    int errorCode = ((StorageException) e).getErrorCode();
                    if (errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) {
                        useDefaultImage(listener);
                    } else {
                        if (e.getMessage() != null) {
                            Log.e(Configuration.APPLICATION_LOG_TAG, e.getMessage());
                        }
                        listener.onFailure(ErrorsConfiguration.ERROR_DOWNLOAD_USER_IMAGE + mUsername);
                    }
                });
    }

    private void useDefaultImage(OnDownloadDataListListener<Uri> listener) {
        mStorage.child(Configuration.DEFAULT_IMAGE_VALUE)
                .getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    List<Uri> list = new ArrayList<>();
                    list.add(uri);
                    listener.onDownloadSuccessful(list);
                })
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(Configuration.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfiguration.ERROR_DOWNLOAD_DEFAULT_USER_IMAGE);
                });
    }
}
