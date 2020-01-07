package com.example.debtspace.main.repositories;

import android.content.Context;
import android.util.Log;

import com.example.debtspace.application.DebtSpaceApplication;
import com.example.debtspace.config.Configuration;
import com.example.debtspace.config.ErrorsConfiguration;
import com.example.debtspace.main.interfaces.OnDownloadDataListListener;
import com.example.debtspace.models.User;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class UserSearchListRepository {

    private FirebaseFirestore mDatabase;
    private StorageReference mStorage;
    private StorageReference mUsersStorage;
    private String mUsername;
    private CollectionReference mUsers;

    private List<User> mList;
    private int mSize;
    private int mCount;

    public UserSearchListRepository(Context context) {
        mDatabase = DebtSpaceApplication.from(context).getDatabase();
        mStorage = DebtSpaceApplication.from(context).getStorage();
        mUsersStorage = mStorage.child(Configuration.USERS_COLLECTION_NAME);
        mUsername = DebtSpaceApplication.from(context).getUsername();
        mUsers = mDatabase.collection(Configuration.USERS_COLLECTION_NAME);

        mList = new ArrayList<>();
        mSize = 0;
        mCount = 0;
    }

    public void getUsersBySubstring(String string, OnDownloadDataListListener<User> listener) {
        mUsers.orderBy(Configuration.USERNAME_FIELD_NAME)
                .startAt(string.trim())
                .endAt(string.trim() + "\uf8ff")
                .get()
                .addOnSuccessListener(documents -> {
                    List<User> users = new ArrayList<>();
                    for (DocumentSnapshot document : documents) {
                        User user = document.toObject(User.class);
                        if (user != null && !user.getUsername().equals(mUsername)) {
                            users.add(user);
                        }
                    }

                    mSize = users.size();
                    for (User user : users) {
                        downloadImage(user, listener);
                    }
                    //checkFriendsAndMyself(users, listener);
                })
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.d(Configuration.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfiguration.ERROR_DOWNLOAD_USERS_FOR_SEARCH);
                });
    }

    /*private void checkFriendsAndMyself(List<User> users, OnDownloadDataListListener<User> listener) {
        mDatabase.collection(Configuration.DEBTS_COLLECTION_NAME)
                .document(mUsername)
                .collection(Configuration.FRIENDS_COLLECTION_NAME)
                .get()
                .addOnSuccessListener(documents -> {
                    Set<String> set = new HashSet<>();
                    set.add(mUsername);
                    for (DocumentSnapshot document : documents) {
                        set.add(document.getId());
                    }

                    Iterator<User> iterator = users.iterator();
                    while (iterator.hasNext()) {
                        User user = iterator.next();
                        if (set.contains(user.getUsername())) {
                            iterator.remove();
                        }
                    }


                })
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.d(Configuration.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfiguration.ERROR_CHECK_FRIENDS_IN_SEARCH);
                });
    }*/

    private void downloadImage(User user, OnDownloadDataListListener<User> listener) {
        mUsersStorage.child(user.getUsername())
                .getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    user.setUriImage(uri);
                    readinessCheck(user, listener);
                })
                .addOnFailureListener(e -> {
                    int errorCode = ((StorageException) e).getErrorCode();
                    if (errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) {
                        useDefaultImage(user, listener);
                    } else {
                        if (e.getMessage() != null) {
                            Log.e(Configuration.APPLICATION_LOG_TAG, e.getMessage());
                        }
                        listener.onFailure(ErrorsConfiguration.ERROR_DOWNLOAD_USER_IMAGE + user.getUsername());
                    }
                });
    }

    private void useDefaultImage(User user, OnDownloadDataListListener<User> listener) {
        mUsersStorage.child(Configuration.DEFAULT_IMAGE_VALUE)
                .getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    user.setUriImage(uri);
                    readinessCheck(user, listener);
                })
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(Configuration.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfiguration.ERROR_DOWNLOAD_DEFAULT_USER_IMAGE + user.getUsername());
                });
    }

    private void readinessCheck(User user, OnDownloadDataListListener<User> listener) {
        mList.add(user);
        mCount++;
        if (mCount == mSize) {
            listener.onDownloadSuccessful(mList);
        }
    }
}
