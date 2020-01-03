package com.example.debtspace.main.repositories;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.debtspace.application.DebtSpaceApplication;
import com.example.debtspace.config.Configuration;
import com.example.debtspace.main.interfaces.OnDownloadDataListener;
import com.example.debtspace.main.interfaces.OnFindUserListener;
import com.example.debtspace.main.interfaces.OnUpdateDataListener;
import com.example.debtspace.models.User;
import com.example.debtspace.utilities.FirebaseUtilities;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GroupDebtRepository {

    private FirebaseFirestore mDatabase;
    private StorageReference mStorage;
    private String mUsername;

    private int mSize;
    private int mCount;

    public GroupDebtRepository(Context context) {
        mDatabase = DebtSpaceApplication.from(context).getDatabase();
        mStorage = DebtSpaceApplication.from(context).getStorage();

        mUsername = DebtSpaceApplication.from(context).getUsername();

        mSize = 0;
        mCount = 0;
    }

    public void downloadFoundListData(OnDownloadDataListener<User> listener) {
        mDatabase.collection(Configuration.DEBTS_COLLECTION_NAME)
                .document(mUsername)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Map<String, Object> map = documentSnapshot.getData();
                    if (documentSnapshot.exists() && map != null) {
                        List<String> users = new ArrayList<>(map.keySet());
                        downloadListItems(users, listener);
                    }
                })
                .addOnFailureListener(e ->
                        listener.onFailure(e.getMessage()));
    }

    public void downloadListItems(List<String> usernames, OnDownloadDataListener<User> listener) {
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

                    Log.d("#DS findUser failed", "User with username " + username + " does not exist.");
                }

                @Override
                public void onFailure(String errorMessage) {
                    readinessCheck(users, listener);

                    Log.d("#DS findUser failed", "Error find username " + username + " ; " + errorMessage);
                }
            });
        }
    }

    private void downloadFriendImage(User user, List<User> users, OnDownloadDataListener<User> listener) {
        mStorage.child(Configuration.USERS_COLLECTION_NAME)
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
                        listener.onFailure(e.getMessage());
                    }
                });
    }

    private void useDefaultUserImage(User user, List<User> users, OnDownloadDataListener<User> listener) {
        mStorage.child(Configuration.USERS_COLLECTION_NAME)
                .child(Configuration.DEFAULT_IMAGE_VALUE)
                .getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    user.setUriImage(uri);
                    users.add(user);
                    readinessCheck(users, listener);
                })
                .addOnFailureListener(e ->
                        listener.onFailure(e.getMessage())
                );
    }

    private void readinessCheck(List<User> users, OnDownloadDataListener<User> listener) {
        mCount++;
        if (mCount == mSize) {
            listener.onDownloadSuccessful(users);
        }
    }

    public void insertGroupToDatabase(String groupName, String debt,
                                      List<String> members, Uri uri, OnUpdateDataListener listener) {
        String groupID = mDatabase.collection(Configuration.GROUP_DEBTS_COLLECTION_NAME)
                .document()
                .getId();
        members.add(mUsername);
        Map<String, Object> groupData = new HashMap<>();
        groupData.put(Configuration.NAME_KEY, groupName);
        groupData.put(Configuration.DEBT_KEY, debt);
        groupData.put(Configuration.MEMBERS_KEY, members);

        mDatabase.collection(Configuration.GROUP_DEBTS_COLLECTION_NAME)
                .document(groupID)
                .set(groupData)
                .addOnSuccessListener(aVoid ->
                        addGroupDataToUsers(groupID, members, uri, listener))
                .addOnFailureListener(e ->
                        listener.onFailure(e.getMessage())
                );
    }

    public void updateGroupInDatabase(String groupID, String groupName, String debt,
                                      List<String> members, OnUpdateDataListener listener) {
        members.add(mUsername);
        mDatabase.collection(Configuration.GROUP_DEBTS_COLLECTION_NAME)
                .document(groupID)
                .update(Configuration.NAME_KEY, groupName,
                        Configuration.DEBT_KEY, debt,
                        Configuration.MEMBERS_KEY, members)
                .addOnSuccessListener(aVoid ->
                        listener.onUpdateSuccessful())
                .addOnFailureListener(e ->
                        listener.onFailure(e.getMessage())
                );
    }

    private void addGroupDataToUsers(String groupID, List<String> users, Uri uri, OnUpdateDataListener listener) {
        mSize = users.size();
        for (String username : users) {
            mDatabase.collection(Configuration.USERS_COLLECTION_NAME)
                    .document(username)
                    .update(Configuration.GROUPS_FIELD_NAME, FieldValue.arrayUnion(groupID))
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
                    .addOnFailureListener(e ->
                            listener.onFailure(e.getMessage())
                    );
        }
    }

    private void uploadImage(String groupID, Uri uri, OnUpdateDataListener listener) {
        mStorage.child(Configuration.GROUP_DEBTS_COLLECTION_NAME)
                .child(groupID)
                .putFile(uri)
                .addOnSuccessListener(taskSnapshot ->
                        listener.onUpdateSuccessful())
                .addOnFailureListener(e -> {
                            Log.d("#DS", Objects.requireNonNull(e.getMessage()));
                            listener.onFailure("ERROR: Can't upload group image");
                        }
                );
    }

    public void downloadGroupImage(String groupID, OnDownloadDataListener<Uri> listener) {
        mStorage.child(Configuration.GROUP_DEBTS_COLLECTION_NAME)
                .child(groupID)
                .getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    List<Uri> list = new ArrayList<>();
                    list.add(uri);
                    listener.onDownloadSuccessful(list);
                })
                .addOnFailureListener(e -> {
                    int errorCode = ((StorageException) e).getErrorCode();
                    if (errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) {
                        useDefaultGroupImage(listener);
                    } else {
                        listener.onFailure(e.getMessage());
                    }
                });
    }

    private void useDefaultGroupImage(OnDownloadDataListener<Uri> listener) {
        mStorage.child(Configuration.GROUP_DEBTS_COLLECTION_NAME).child(Configuration.DEFAULT_IMAGE_VALUE)
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
