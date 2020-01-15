package com.example.debtspace.main.repositories;

import android.content.Context;
import android.util.Log;

import com.example.debtspace.application.DebtSpaceApplication;
import com.example.debtspace.config.AppConfig;
import com.example.debtspace.config.ErrorsConfig;
import com.example.debtspace.main.interfaces.OnDownloadDataListListener;
import com.example.debtspace.main.interfaces.OnFindUserListener;
import com.example.debtspace.models.User;
import com.example.debtspace.utilities.FirebaseUtilities;
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
        mUsersStorage = mStorage.child(AppConfig.USERS_COLLECTION_NAME);
        mUsername = DebtSpaceApplication.from(context).getUsername();
        mUsers = mDatabase.collection(AppConfig.USERS_COLLECTION_NAME);

        mList = new ArrayList<>();
        mSize = 0;
        mCount = 0;
    }

    public void getUsersBySubstring(int filterID, String string, OnDownloadDataListListener<User> listener) {
        switch (filterID) {
            case AppConfig.SEARCH_FILTER_ALL_USERS_ID:
                getAllUsers(string, listener);
                break;
            case AppConfig.SEARCH_FILTER_FRIENDS_ID:
                getFriends(string, listener);
                break;
        }
    }

    private void getAllUsers(String string, OnDownloadDataListListener<User> listener) {
        mUsers.orderBy(AppConfig.USERNAME_FIELD_NAME)
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
                    if (mSize == 0) {
                        listener.onDownloadSuccessful(mList);
                    }
                    for (User user : users) {
                        downloadImage(user, listener);
                    }
                })
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.d(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfig.ERROR_DOWNLOAD_USERS_FOR_SEARCH);
                });
    }

    private void getFriends(String string, OnDownloadDataListListener<User> listener) {
        mDatabase.collection(AppConfig.DEBTS_COLLECTION_NAME)
                .document(mUsername)
                .collection(AppConfig.FRIENDS_COLLECTION_NAME)
                .get()
                .addOnSuccessListener(documents -> {
                    List<String> usernames = new ArrayList<>();
                    for (DocumentSnapshot document : documents) {
                        String username = document.getId();
                        if (username.contains(string)) {
                            usernames.add(username);
                        }
                    }
                    mSize = usernames.size();
                    if (mSize == 0) {
                        listener.onDownloadSuccessful(mList);
                    }
                    for (String username : usernames) {
                            FirebaseUtilities.findUserByUsername(username, new OnFindUserListener() {

                                @Override
                                public void onSuccessful(User user) {
                                    downloadImage(user, listener);
                                }

                                @Override
                                public void onDoesNotExist() {
                                    downloadImage(new User(username), listener);
                                }

                                @Override
                                public void onFailure(String errorMessage) {
                                    downloadImage(new User(username), listener);
                                }
                            });
                        }
                })
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.d(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfig.ERROR_DOWNLOAD_USERS_FOR_SEARCH);
                });
    }

    /*private void checkFriendsAndMyself(List<User> users, OnDownloadDataListListener<User> listener) {
        mDatabase.collection(AppConfig.DEBTS_COLLECTION_NAME)
                .document(mUsername)
                .collection(AppConfig.FRIENDS_COLLECTION_NAME)
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
                        Log.d(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfig.ERROR_CHECK_FRIENDS_IN_SEARCH);
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
                            Log.e(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                        }
                        listener.onFailure(ErrorsConfig.ERROR_DOWNLOAD_USER_IMAGE + user.getUsername());
                    }
                });
    }

    private void useDefaultImage(User user, OnDownloadDataListListener<User> listener) {
        mUsersStorage.child(AppConfig.DEFAULT_IMAGE_VALUE)
                .getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    user.setUriImage(uri);
                    readinessCheck(user, listener);
                })
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfig.ERROR_DOWNLOAD_DEFAULT_USER_IMAGE + user.getUsername());
                });
    }

    private void readinessCheck(User user, OnDownloadDataListListener<User> listener) {
        mList.add(user);
        mCount++;
        if (mCount == mSize) {
            listener.onDownloadSuccessful(mList);
        }
    }

    /*private void debtReadinessCheck(Debt debt, OnDownloadDataListListener<Debt> listener) {
        mList.add(debt);
        mCount++;
        Log.d("#DS", "check " + mCount + " " + mSize);
        if (mCount == mSize) {
            listener.onDownloadSuccessful((List<Debt>) mList);
        }
    }*/
}
