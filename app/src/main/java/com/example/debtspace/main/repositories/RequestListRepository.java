package com.example.debtspace.main.repositories;

import android.util.Log;

import com.example.debtspace.config.Configuration;
import com.example.debtspace.main.interfaces.OnDownloadDataListener;
import com.example.debtspace.main.interfaces.OnFindUserListener;
import com.example.debtspace.main.interfaces.OnGetFirestoreDataListener;
import com.example.debtspace.models.User;
import com.example.debtspace.utilities.FirebaseUtilities;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RequestListRepository {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseFirestore mDatabase;
    private StorageReference mStorage;
    private String mUsername;

    private List<User> mList;
    private int mListSize;
    private int mItemCount;

    public RequestListRepository() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference();

        mUsername = Objects.requireNonNull(Objects.requireNonNull(mFirebaseAuth
                .getCurrentUser())
                .getDisplayName());

        mList = new ArrayList<>();
        mListSize = 0;
        mItemCount = 0;
    }

    public void getRequests(OnDownloadDataListener<User> listener) {
        getFriendRequestsAsList(listener);
    }

    private void getFriendRequestsAsList(OnDownloadDataListener<User> listener) {

        Query query = mDatabase.collection(Configuration.NOTIFICATIONS_COLLECTION_NAME)
                .document(mUsername)
                .collection(Configuration.FRIENDS_COLLECTION_NAME);

        FirebaseUtilities.getDataFromDatabase(query, new OnGetFirestoreDataListener() {
            @Override
            public void onGetSuccessful(List<Map<String, Map<String, Object>>> data) {
                if (data.size() != 0) {
                    Map<String, Map<String, Object>> map = data.get(0);
                    mListSize = map.size();
                    for (String username : map.keySet()) {
                        FirebaseUtilities.findUserByUsername(username, new OnFindUserListener() {

                            @Override
                            public void onSuccessful(User user) {
                                downloadImage(user, listener);
                            }

                            @Override
                            public void onDoesNotExist() {
                                readinessCheck(listener);

                                Log.d("#DS findUser", "FAIL. User with username " + username + " does not exist.");
                            }

                            @Override
                            public void onFailure(String errorMessage) {
                                readinessCheck(listener);

                                Log.d("#DS findUser", "FAIL. Error find username " + username + " ; " + errorMessage);
                            }
                        });
                    }
                } else {
                    listener.onDownloadSuccessful(mList);
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                listener.onFailure(errorMessage);
            }
        });
    }

    private void readinessCheck(OnDownloadDataListener<User> listener) {
        mItemCount++;
        if (mItemCount == mListSize) {
            listener.onDownloadSuccessful(mList);
        }
    }

    private void downloadImage(User user, OnDownloadDataListener<User> listener) {
        mStorage.child(Configuration.USERS_COLLECTION_NAME)
                .child(user.getUsername())
                .getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    user.setUriImage(uri);
                    mList.add(user);
                    readinessCheck(listener);
                })
                .addOnFailureListener(e ->
                        useDefaultImage(user, listener)
                );
    }

    private void useDefaultImage(User user, OnDownloadDataListener<User> listener) {
        mStorage.child(Configuration.USERS_COLLECTION_NAME)
                .child(Configuration.DEFAULT_IMAGE_VALUE)
                .getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    user.setUriImage(uri);
                    mList.add(user);
                    readinessCheck(listener);
                })
                .addOnFailureListener(e ->
                        listener.onFailure(e.getMessage())
                );
    }
}
