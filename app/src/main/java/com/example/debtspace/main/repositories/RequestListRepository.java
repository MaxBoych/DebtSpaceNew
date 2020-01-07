package com.example.debtspace.main.repositories;

import android.content.Context;
import android.util.Log;

import com.example.debtspace.application.DebtSpaceApplication;
import com.example.debtspace.config.Configuration;
import com.example.debtspace.config.ErrorsConfiguration;
import com.example.debtspace.main.interfaces.OnDatabaseEventListener;
import com.example.debtspace.main.interfaces.OnDownloadDataListListener;
import com.example.debtspace.main.interfaces.OnFindUserListener;
import com.example.debtspace.models.Request;
import com.example.debtspace.models.User;
import com.example.debtspace.utilities.FirebaseUtilities;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RequestListRepository {

    private FirebaseFirestore mDatabase;
    private CollectionReference mRequests;
    private StorageReference mStorage;
    private StorageReference mUsersStorage;
    private String mUsername;

    private List<Request> mList;
    private int mSize;
    private int mCount;

    public RequestListRepository(Context context) {
        mDatabase = DebtSpaceApplication.from(context).getDatabase();
        mStorage = DebtSpaceApplication.from(context).getStorage();
        mUsersStorage = mStorage.child(Configuration.USERS_COLLECTION_NAME);
        mUsername = DebtSpaceApplication.from(context).getUsername();
        mRequests = mDatabase.collection(Configuration.NOTIFICATIONS_COLLECTION_NAME)
                .document(mUsername)
                .collection(Configuration.FRIENDS_COLLECTION_NAME);

        mList = new ArrayList<>();
        mSize = 0;
        mCount = 0;
    }

    public void downloadRequestList(OnDownloadDataListListener<Request> listener) {
        mRequests.get()
                .addOnSuccessListener(documents -> {
                    if (documents.isEmpty()) {
                        listener.onDownloadSuccessful(mList);
                    }
                    mSize = documents.size();
                    for (DocumentSnapshot document : documents) {
                        Map<String, Object> data = document.getData();
                        if (data != null) {
                            String username = document.getId();
                            String date = (String) data.get(Configuration.DATE_KEY);

                            FirebaseUtilities.findUserByUsername(username, new OnFindUserListener() {

                                @Override
                                public void onSuccessful(User user) {
                                    downloadImage(new Request(user, date), listener);
                                }

                                @Override
                                public void onDoesNotExist() {
                                    readinessCheck(listener);
                                }

                                @Override
                                public void onFailure(String errorMessage) {
                                    readinessCheck(listener);
                                }
                            });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(Configuration.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfiguration.ERROR_DOWNLOAD_REQUESTS);
                });
    }

    private void readinessCheck(OnDownloadDataListListener<Request> listener) {
        mCount++;
        if (mCount == mSize) {
            listener.onDownloadSuccessful(mList);
        }
    }

    private void downloadImage(Request request, OnDownloadDataListListener<Request> listener) {
        mUsersStorage.child(request.getUsername())
                .getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    request.setUriImage(uri);
                    mList.add(request);
                    readinessCheck(listener);
                })
                .addOnFailureListener(e -> {
                    int errorCode = ((StorageException) e).getErrorCode();
                    if (errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) {
                        useDefaultImage(request, listener);
                    } else {
                        if (e.getMessage() != null) {
                            Log.e(Configuration.APPLICATION_LOG_TAG, e.getMessage());
                        }
                        listener.onFailure(ErrorsConfiguration.ERROR_DOWNLOAD_USER_IMAGE + request.getUsername());
                    }
                });
    }

    private void useDefaultImage(Request request, OnDownloadDataListListener<Request> listener) {
        mUsersStorage.child(Configuration.DEFAULT_IMAGE_VALUE)
                .getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    request.setUriImage(uri);
                    mList.add(request);
                    readinessCheck(listener);
                })
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(Configuration.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfiguration.ERROR_DOWNLOAD_DEFAULT_USER_IMAGE + request.getUsername());
                });
    }

    public void observeEvents(OnDatabaseEventListener<Request> listener) {
        mRequests.addSnapshotListener((query, e) -> {
            if (e != null && e.getMessage() != null) {
                Log.d(Configuration.APPLICATION_LOG_TAG, e.getMessage());
                listener.onFailure(ErrorsConfiguration.ERROR_DATA_READING_NOTIFICATIONS);
            } else if (query != null) {
                for (DocumentChange change : query.getDocumentChanges()) {
                    DocumentSnapshot document = change.getDocument();
                    if (change.getType() == DocumentChange.Type.ADDED) {
                        Map<String, Object> data = document.getData();
                        if (data != null) {
                            String date = (String) data.get(Configuration.DATE_KEY);
                            String username = document.getId();

                            FirebaseUtilities.findUserByUsername(username, new OnFindUserListener() {

                                @Override
                                public void onSuccessful(User user) {
                                    downloadImageForEvent(new Request(user, date), listener);
                                }

                                @Override
                                public void onDoesNotExist() {}

                                @Override
                                public void onFailure(String errorMessage) {
                                    listener.onFailure(errorMessage);
                                }
                            });
                        }
                    }

                }
            }
        });
    }

    private void downloadImageForEvent(Request request, OnDatabaseEventListener<Request> listener) {
        mUsersStorage.child(request.getUsername())
                .getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    request.setUriImage(uri);
                    listener.onAdded(request);
                })
                .addOnFailureListener(e -> {
                    int errorCode = ((StorageException) e).getErrorCode();
                    if (errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) {
                        useDefaultImageForEvent(request, listener);
                    } else {
                        if (e.getMessage() != null) {
                            Log.e(Configuration.APPLICATION_LOG_TAG, e.getMessage());
                        }
                        listener.onFailure(ErrorsConfiguration.ERROR_DOWNLOAD_USER_IMAGE + request.getUsername());
                    }
                });
    }

    private void useDefaultImageForEvent(Request request, OnDatabaseEventListener<Request> listener) {
        mUsersStorage.child(Configuration.DEFAULT_IMAGE_VALUE)
                .getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    request.setUriImage(uri);
                    listener.onAdded(request);
                })
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(Configuration.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfiguration.ERROR_DOWNLOAD_DEFAULT_USER_IMAGE + request.getUsername());
                });
    }
}
