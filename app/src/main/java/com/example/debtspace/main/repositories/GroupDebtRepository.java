package com.example.debtspace.main.repositories;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.debtspace.application.DebtSpaceApplication;
import com.example.debtspace.config.AppConfig;
import com.example.debtspace.config.ErrorsConfig;
import com.example.debtspace.main.interfaces.OnDownloadDataListListener;
import com.example.debtspace.main.interfaces.OnDownloadDataListener;
import com.example.debtspace.main.interfaces.OnFindUserListener;
import com.example.debtspace.main.interfaces.OnUpdateDataListener;
import com.example.debtspace.models.User;
import com.example.debtspace.utilities.FirebaseUtilities;
import com.example.debtspace.utilities.StringUtilities;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupDebtRepository {

    private FirebaseFirestore mDatabase;
    private CollectionReference mGroups;
    private CollectionReference mUsers;
    private StorageReference mStorage;
    private String mUsername;

    private int mSize;
    private int mCount;

    public GroupDebtRepository(Context context) {
        mDatabase = DebtSpaceApplication.from(context).getDatabase();
        mStorage = DebtSpaceApplication.from(context).getStorage();
        mUsername = DebtSpaceApplication.from(context).getUsername();
        mGroups = mDatabase.collection(AppConfig.DEBTS_COLLECTION_NAME)
                .document(mUsername)
                .collection(AppConfig.GROUP_DEBTS_COLLECTION_NAME);
        mUsers = mDatabase.collection(AppConfig.USERS_COLLECTION_NAME);

        mSize = 0;
        mCount = 0;
    }

    public void downloadFoundListData(OnDownloadDataListListener<User> listener) {
        mDatabase.collection(AppConfig.DEBTS_COLLECTION_NAME)
                .document(mUsername)
                .collection(AppConfig.FRIENDS_COLLECTION_NAME)
                .get()
                .addOnSuccessListener(documents -> {
                    List<String> users = new ArrayList<>();
                    for (DocumentSnapshot document : documents) {
                        if (document.exists()) {
                            users.add(document.getId());
                        }
                    }
                    downloadListItems(users, listener);
                })
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfig.ERROR_DOWNLOAD_SINGLE_DEBTS);
                });
    }

    public void downloadListItems(List<String> usernames, OnDownloadDataListListener<User> listener) {
        usernames.remove(mUsername);
        List<User> users = new ArrayList<>();
        mSize = usernames.size();

        for (String username : usernames) {
            FirebaseUtilities.findUserByUsername(username, new OnFindUserListener() {

                @Override
                public void onSuccessful(User user) {
                    downloadFriendImage(user, users, listener);
                }

                @Override
                public void onDoesNotExist() {
                    readinessCheck(users, listener);
                }

                @Override
                public void onFailure(String errorMessage) {
                    readinessCheck(users, listener);

                    Log.e(AppConfig.APPLICATION_LOG_TAG, errorMessage);
                }
            });
        }
    }

    private void downloadFriendImage(User user, List<User> users, OnDownloadDataListListener<User> listener) {
        mStorage.child(AppConfig.USERS_COLLECTION_NAME)
                .child(user.getUsername())
                .getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    user.setUriImage(uri);
                    users.add(user);
                    readinessCheck(users, listener);
                })
                .addOnFailureListener(e -> {
                    int errorCode = ((StorageException) e).getErrorCode();
                    if (errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) {
                        useDefaultUserImage(user, users, listener);
                    } else {
                        if (e.getMessage() != null) {
                            Log.e(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                        }
                        listener.onFailure(ErrorsConfig.ERROR_DOWNLOAD_USER_IMAGE + user.getUsername());
                    }
                });
    }

    private void useDefaultUserImage(User user, List<User> users, OnDownloadDataListListener<User> listener) {
        mStorage.child(AppConfig.USERS_COLLECTION_NAME)
                .child(AppConfig.DEFAULT_IMAGE_VALUE)
                .getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    user.setUriImage(uri);
                    users.add(user);
                    readinessCheck(users, listener);
                })
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfig.ERROR_DOWNLOAD_DEFAULT_USER_IMAGE + user.getUsername());
                });
    }

    private void readinessCheck(List<User> users, OnDownloadDataListListener<User> listener) {
        mCount++;
        if (mCount == mSize) {
            listener.onDownloadSuccessful(users);
        }
    }

    public void uploadGroup(String groupName, String debt,
                            List<String> members, Uri uri, OnUpdateDataListener listener) {
        String groupID = mDatabase.collection(AppConfig.GROUP_DEBTS_COLLECTION_NAME)
                .document()
                .getId();
        members.add(mUsername);
        Map<String, Object> groupData = new HashMap<>();
        groupData.put(AppConfig.NAME_KEY, groupName);
        groupData.put(AppConfig.DEBT_KEY, debt);
        String date = StringUtilities.getCurrentDateAndTime();
        groupData.put(AppConfig.DATE_KEY, date);
        groupData.put(AppConfig.MEMBERS_KEY, members);

        mGroups.document(groupID)
                .set(groupData)
                .addOnSuccessListener(aVoid ->
                        uploadGroupDataToMembers(groupID, members, uri, listener))
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfig.ERROR_UPLOAD_GROUP + groupID);
                });


        /*mDatabase.collection(AppConfig.GROUP_DEBTS_COLLECTION_NAME)
                .document(groupID)
                .set(groupData)
                .addOnSuccessListener(aVoid ->
                        uploadGroupDataToMembers(groupID, members, uri, listener))
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfig.ERROR_UPLOAD_GROUP + groupID);
                });*/
    }

    public void updateGroup(String groupID, String groupName, String debt,
                            List<String> members, OnUpdateDataListener listener) {
        members.add(mUsername);
        String date = StringUtilities.getCurrentDateAndTime();

        mGroups.document(groupID)
                .update(AppConfig.NAME_KEY, groupName,
                        AppConfig.DEBT_KEY, debt,
                        AppConfig.MEMBERS_KEY, members,
                        AppConfig.DATE_KEY, date)
                .addOnSuccessListener(aVoid ->
                        listener.onUpdateSuccessful())
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfig.ERROR_UPDATE_GROUP + groupID);
                });
    }

    private void uploadGroupDataToMembers(String groupID, List<String> users,
                                          Uri uri, OnUpdateDataListener listener) {
        mSize = users.size();
        for (String username : users) {
            mUsers.document(username)
                    .update(AppConfig.GROUPS_FIELD_NAME, FieldValue.arrayUnion(groupID))
                    .addOnSuccessListener(aVoid -> {
                        mCount++;
                        if (mCount == mSize) {
                            if (uri != null) {
                                uploadImage(groupID, uri, listener);
                            } else {
                                listener.onUpdateSuccessful();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (e.getMessage() != null) {
                            Log.e(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                        }
                        listener.onFailure(ErrorsConfig.ERROR_UPLOAD_GROUP_DATA_TO_MEMBERS + groupID);
                    });
        }
    }

    private void uploadImage(String groupID, Uri uri, OnUpdateDataListener listener) {
        mStorage.child(AppConfig.GROUP_DEBTS_COLLECTION_NAME)
                .child(groupID)
                .putFile(uri)
                .addOnSuccessListener(taskSnapshot ->
                        listener.onUpdateSuccessful())
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfig.ERROR_UPLOAD_GROUP_IMAGE + groupID);
                });
    }

    public void downloadGroupImage(String groupID, OnDownloadDataListener<Uri> listener) {
        mStorage.child(AppConfig.GROUP_DEBTS_COLLECTION_NAME)
                .child(groupID)
                .getDownloadUrl()
                .addOnSuccessListener(listener::onDownloadSuccessful)
                .addOnFailureListener(e -> {
                    int errorCode = ((StorageException) e).getErrorCode();
                    if (errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) {
                        useDefaultGroupImage(groupID, listener);
                    } else {
                        if (e.getMessage() != null) {
                            Log.e(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                        }
                        listener.onFailure(ErrorsConfig.ERROR_DOWNLOAD_GROUP_IMAGE + groupID);
                    }
                });
    }

    private void useDefaultGroupImage(String groupID, OnDownloadDataListener<Uri> listener) {
        mStorage.child(AppConfig.GROUP_DEBTS_COLLECTION_NAME).child(AppConfig.DEFAULT_IMAGE_VALUE)
                .getDownloadUrl()
                .addOnSuccessListener(listener::onDownloadSuccessful)
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfig.ERROR_DOWNLOAD_DEFAULT_GROUP_IMAGE + groupID);
                });
    }
}
