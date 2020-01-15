package com.example.debtspace.main.repositories;

import android.content.Context;
import android.util.Log;

import com.example.debtspace.application.DebtSpaceApplication;
import com.example.debtspace.config.AppConfig;
import com.example.debtspace.config.ErrorsConfig;
import com.example.debtspace.main.interfaces.OnDatabaseEventListener;
import com.example.debtspace.main.interfaces.OnDownloadDataListListener;
import com.example.debtspace.main.interfaces.OnFindUserListener;
import com.example.debtspace.models.DebtRequest;
import com.example.debtspace.models.FriendRequest;
import com.example.debtspace.models.Notification;
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
import java.util.UUID;

public class NotificationListRepository {

    private FirebaseFirestore mDatabase;
    private CollectionReference mFriendRequests;
    private CollectionReference mDebtRequests;
    private StorageReference mStorage;
    private StorageReference mUsersStorage;
    private String mUsername;

    private List<Notification> mList;
    private int mSize;
    private int mCount;

    public NotificationListRepository(Context context) {
        mDatabase = DebtSpaceApplication.from(context).getDatabase();
        mStorage = DebtSpaceApplication.from(context).getStorage();
        mUsersStorage = mStorage.child(AppConfig.USERS_COLLECTION_NAME);
        mUsername = DebtSpaceApplication.from(context).getUsername();
        mFriendRequests = mDatabase.collection(AppConfig.NOTIFICATIONS_COLLECTION_NAME)
                .document(mUsername)
                .collection(AppConfig.FRIENDS_COLLECTION_NAME);
        mDebtRequests = mDatabase.collection(AppConfig.NOTIFICATIONS_COLLECTION_NAME)
                .document(mUsername)
                .collection(AppConfig.DEBTS_COLLECTION_NAME);

        mList = new ArrayList<>();
        mSize = 0;
        mCount = 0;
    }

    public void downloadRequestList(OnDownloadDataListListener<Notification> listener) {
        downloadFriendRequests(listener);
    }

    private void downloadFriendRequests(OnDownloadDataListListener<Notification> listener) {
        mFriendRequests.get()
                .addOnSuccessListener(documents -> {
                    if (documents.isEmpty()) {
                        listener.onDownloadSuccessful(mList);
                    }
                    mSize = documents.size();
                    if (mSize == 0) {
                        downloadDebtRequests(listener);
                    }
                    for (DocumentSnapshot document : documents) {
                        Map<String, Object> data = document.getData();
                        if (data != null) {
                            String username = document.getId();
                            String date = (String) data.get(AppConfig.DATE_KEY);

                            FirebaseUtilities.findUserByUsername(username, new OnFindUserListener() {

                                @Override
                                public void onSuccessful(User user) {
                                    downloadImage(new FriendRequest(user, date), listener);
                                }

                                @Override
                                public void onDoesNotExist() {
                                    readinessCheck(listener, false);
                                }

                                @Override
                                public void onFailure(String errorMessage) {
                                    readinessCheck(listener, false);
                                }
                            });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfig.ERROR_DOWNLOAD_REQUESTS);
                });
    }

    private void downloadDebtRequests(OnDownloadDataListListener<Notification> listener) {
        mDebtRequests.get()
                .addOnSuccessListener(documents -> {
                    if (documents.isEmpty()) {
                        listener.onDownloadSuccessful(mList);
                    }
                    mSize = documents.size();
                    mCount = 0;
                    for (DocumentSnapshot document : documents) {
                        DebtRequest request = document.toObject(DebtRequest.class);
                        mList.add(request);
                        readinessCheck(listener, true);
                    }
                })
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfig.ERROR_DOWNLOAD_REQUESTS);
                });
    }

    private void readinessCheck(OnDownloadDataListListener<Notification> listener, boolean isReady) {
        mCount++;
        if (mCount == mSize) {
            if (isReady) {
                listener.onDownloadSuccessful(mList);
            } else {
                downloadDebtRequests(listener);
            }
        }
    }

    private void downloadImage(FriendRequest request, OnDownloadDataListListener<Notification> listener) {
        mUsersStorage.child(request.getUsername())
                .getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    request.setImageUri(uri);
                    mList.add(request);
                    readinessCheck(listener, false);
                })
                .addOnFailureListener(e -> {
                    int errorCode = ((StorageException) e).getErrorCode();
                    if (errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) {
                        useDefaultImage(request, listener);
                    } else {
                        if (e.getMessage() != null) {
                            Log.e(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                        }
                        listener.onFailure(ErrorsConfig.ERROR_DOWNLOAD_USER_IMAGE + request.getUsername());
                    }
                });
    }

    private void useDefaultImage(FriendRequest request, OnDownloadDataListListener<Notification> listener) {
        mUsersStorage.child(AppConfig.DEFAULT_IMAGE_VALUE)
                .getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    request.setImageUri(uri);
                    mList.add(request);
                    readinessCheck(listener, false);
                })
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfig.ERROR_DOWNLOAD_DEFAULT_USER_IMAGE + request.getUsername());
                });
    }

    public void observeNotificationEvents(OnDatabaseEventListener<Notification> listener) {
        observeFriendRequestEvents(listener);
        observeDebtRequestEvents(listener);
    }

    private void observeFriendRequestEvents(OnDatabaseEventListener<Notification> listener) {
        mFriendRequests.addSnapshotListener((query, e) -> {
            if (e != null && e.getMessage() != null) {
                Log.d(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                listener.onFailure(ErrorsConfig.ERROR_DATA_READING_NOTIFICATIONS);
            } else if (query != null) {
                for (DocumentChange change : query.getDocumentChanges()) {
                    DocumentSnapshot document = change.getDocument();
                    Map<String, Object> data = document.getData();
                    if (data != null) {
                        String date = (String) data.get(AppConfig.DATE_KEY);
                        String username = document.getId();

                        if (change.getType() == DocumentChange.Type.ADDED) {
                            FirebaseUtilities.findUserByUsername(username, new OnFindUserListener() {

                                @Override
                                public void onSuccessful(User user) {
                                    downloadImageForEvent(new FriendRequest(user, date), listener);
                                }

                                @Override
                                public void onDoesNotExist() {
                                }

                                @Override
                                public void onFailure(String errorMessage) {
                                    listener.onFailure(errorMessage);
                                }
                            });
                        } else if (change.getType() == DocumentChange.Type.REMOVED) {
                            listener.onRemoved(new FriendRequest(username));
                        }
                    }
                }
            }
        });
    }

    private void observeDebtRequestEvents(OnDatabaseEventListener<Notification> listener) {
        mDebtRequests.addSnapshotListener((query, e) -> {
            if (e != null && e.getMessage() != null) {
                Log.d(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                listener.onFailure(ErrorsConfig.ERROR_DATA_READING_NOTIFICATIONS);
            } else if (query != null) {
                for (DocumentChange change : query.getDocumentChanges()) {
                    DocumentSnapshot document = change.getDocument();
                    Map<String, Object> data = document.getData();
                    if (data != null) {
                        String id = document.getId();
                        String username = (String) data.get(AppConfig.USERNAME_KEY);
                        if (change.getType() == DocumentChange.Type.ADDED) {
                            FirebaseUtilities.findUserByUsername(username, new OnFindUserListener() {

                                @Override
                                public void onSuccessful(User user) {
                                    listener.onAdded(new DebtRequest(id, user, data));
                                }

                                @Override
                                public void onDoesNotExist() {
                                }

                                @Override
                                public void onFailure(String errorMessage) {
                                    listener.onFailure(errorMessage);
                                }
                            });
                        } else if (change.getType() == DocumentChange.Type.REMOVED) {
                            listener.onRemoved(new DebtRequest(id));
                        }
                    }
                }
            }
        });
    }

    private void downloadImageForEvent(FriendRequest request, OnDatabaseEventListener<Notification> listener) {
        mUsersStorage.child(request.getUsername())
                .getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    request.setImageUri(uri);
                    listener.onAdded(request);
                })
                .addOnFailureListener(e -> {
                    int errorCode = ((StorageException) e).getErrorCode();
                    if (errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) {
                        useDefaultImageForEvent(request, listener);
                    } else {
                        if (e.getMessage() != null) {
                            Log.e(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                        }
                        listener.onFailure(ErrorsConfig.ERROR_DOWNLOAD_USER_IMAGE + request.getUsername());
                    }
                });
    }

    private void useDefaultImageForEvent(FriendRequest request, OnDatabaseEventListener<Notification> listener) {
        mUsersStorage.child(AppConfig.DEFAULT_IMAGE_VALUE)
                .getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    request.setImageUri(uri);
                    listener.onAdded(request);
                })
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfig.ERROR_DOWNLOAD_DEFAULT_USER_IMAGE + request.getUsername());
                });
    }
}
