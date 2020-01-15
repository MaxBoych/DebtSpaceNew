package com.example.debtspace.main.repositories;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.debtspace.application.DebtSpaceApplication;
import com.example.debtspace.config.AppConfig;
import com.example.debtspace.config.ErrorsConfig;
import com.example.debtspace.main.interfaces.OnDatabaseEventListener;
import com.example.debtspace.main.interfaces.OnDownloadDataListListener;
import com.example.debtspace.main.interfaces.OnFindUserListener;
import com.example.debtspace.models.User;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ProfileRepository {

    private FirebaseFirestore mDatabase;
    private DocumentReference mCurrentUser;
    private StorageReference mStorage;
    private String mUsername;

    public ProfileRepository(Context context) {
        mDatabase = DebtSpaceApplication.from(context).getDatabase();
        mStorage = DebtSpaceApplication.from(context)
                .getStorage()
                .child(AppConfig.USERS_COLLECTION_NAME);
        mUsername = DebtSpaceApplication.from(context).getUsername();
        mCurrentUser = mDatabase.collection(AppConfig.USERS_COLLECTION_NAME)
                .document(mUsername);
    }

    public void observeUserDataEvents(OnDatabaseEventListener<User> listener) {
        mCurrentUser.addSnapshotListener((document, e) -> {
            if (e != null) {
                if (e.getMessage() != null) {
                    Log.e(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                }
                listener.onFailure(ErrorsConfig.ERROR_DATA_READING);
            } else if (document != null && document.exists()) {
                User user = document.toObject(User.class);
                listener.onModified(user);
            }
        });
    }

    public void downloadUserData(OnFindUserListener listener) {
        mCurrentUser.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            listener.onSuccessful(user);
                        }
                    } else {
                        Log.w(AppConfig.APPLICATION_LOG_TAG, ErrorsConfig.WARNING_USER_DOES_NOT_EXIST + mUsername);
                        listener.onDoesNotExist();
                    }
                })
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfig.ERROR_DOWNLOAD_USER_DATA + mUsername);
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
                            Log.e(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                        }
                        listener.onFailure(ErrorsConfig.ERROR_DOWNLOAD_USER_IMAGE + mUsername);
                    }
                });
    }

    private void useDefaultImage(OnDownloadDataListListener<Uri> listener) {
        mStorage.child(AppConfig.DEFAULT_IMAGE_VALUE)
                .getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    List<Uri> list = new ArrayList<>();
                    list.add(uri);
                    listener.onDownloadSuccessful(list);
                })
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfig.ERROR_DOWNLOAD_DEFAULT_USER_IMAGE);
                });
    }
}
