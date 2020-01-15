package com.example.debtspace.main.repositories;

import android.content.Context;
import android.util.Log;

import com.example.debtspace.application.DebtSpaceApplication;
import com.example.debtspace.config.AppConfig;
import com.example.debtspace.config.ErrorsConfig;
import com.example.debtspace.main.interfaces.OnDatabaseEventListener;
import com.example.debtspace.main.interfaces.OnDownloadDataListListener;
import com.example.debtspace.main.interfaces.OnDownloadDataListener;
import com.example.debtspace.main.interfaces.OnFindUserListener;
import com.example.debtspace.models.Debt;
import com.example.debtspace.models.DebtBond;
import com.example.debtspace.models.GroupDebt;
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
import java.util.Objects;

public class DebtListRepository {

    private FirebaseFirestore mDatabase;
    private CollectionReference mUsers;
    private CollectionReference mDebts;
    private StorageReference mStorage;
    private String mUsername;

    private List<Debt> mList;
    private int mSize;
    private int mCount;

    public DebtListRepository(Context context) {
        mDatabase = DebtSpaceApplication.from(context).getDatabase();
        mStorage = DebtSpaceApplication.from(context).getStorage();
        mUsername = DebtSpaceApplication.from(context).getUsername();
        mUsers = mDatabase.collection(AppConfig.USERS_COLLECTION_NAME);
        mDebts = mDatabase.collection(AppConfig.DEBTS_COLLECTION_NAME);

        mList = new ArrayList<>();
    }

    public void downloadDebtListData(OnDownloadDataListListener<Debt> listener) {
        downloadGroupsIDs(new OnDownloadDataListListener<String>() {
            @Override
            public void onDownloadSuccessful(List<String> list) {
                downloadGroupDebts(list, listener);
            }

            @Override
            public void onFailure(String errorMessage) {
                listener.onFailure(errorMessage);
            }
        });
    }

    public void observeDebtEvents(OnDatabaseEventListener<Debt> listener) {
        observeSingleDebtEvents(listener);
        observeGroupDebtEvents(listener);
    }

    private void observeSingleDebtEvents(OnDatabaseEventListener<Debt> listener) {
        mDebts.document(mUsername)
                .collection(AppConfig.FRIENDS_COLLECTION_NAME)
                .addSnapshotListener((query, e) -> {
                    if (e != null) {
                        if (e.getMessage() != null) {
                            Log.e(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                        }
                        listener.onFailure(ErrorsConfig.ERROR_DATA_READING);
                    } else if (query != null) {
                        for (DocumentChange change : query.getDocumentChanges()) {
                            DocumentSnapshot document = change.getDocument();
                            Map<String, Object> data = document.getData();
                            if (data != null) {
                                DebtBond debtBond = new DebtBond(document.getId(), data);
                                switch (change.getType()) {
                                    case ADDED:
                                        transformToDebt(debtBond, new OnDownloadDataListener<Debt>() {
                                            @Override
                                            public void onDownloadSuccessful(Debt object) {
                                                listener.onAdded(object);
                                            }

                                            @Override
                                            public void onFailure(String errorMessage) {
                                                listener.onFailure(errorMessage);
                                            }
                                        });
                                        break;
                                    case MODIFIED:
                                        listener.onModified(new Debt(debtBond));
                                        break;
                                    case REMOVED:
                                        listener.onRemoved(new Debt(debtBond));
                                        break;
                                }
                            }
                        }
                    }
                });
    }

    private void observeGroupDebtEvents(OnDatabaseEventListener<Debt> listener) {
        mDebts.document(mUsername)
                .collection(AppConfig.GROUP_DEBTS_COLLECTION_NAME)
                .addSnapshotListener((query, e) -> {
                    if (e != null) {
                        if (e.getMessage() != null) {
                            Log.e(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                        }
                        listener.onFailure(ErrorsConfig.ERROR_DATA_READING);
                    } else if (query != null) {
                        for (DocumentChange change : query.getDocumentChanges()) {
                            DocumentSnapshot document = change.getDocument();
                            Map<String, Object> data = document.getData();
                            if (data != null) {
                                GroupDebt groupDebt = new GroupDebt(data, document.getId());
                                switch (change.getType()) {
                                    case ADDED:
                                        listener.onAdded(groupDebt);
                                        break;
                                    case MODIFIED:
                                        listener.onModified(groupDebt);
                                        break;
                                    case REMOVED:
                                        listener.onRemoved(groupDebt);
                                        break;
                                }
                            }
                        }
                    }
                });
    }

    private void transformToDebt(DebtBond debtBond, OnDownloadDataListener<Debt> listener) {
        String username = debtBond.getUsername();
        FirebaseUtilities.findUserByUsername(username, new OnFindUserListener() {
            @Override
            public void onSuccessful(User user) {
                Debt debt = new Debt(user, debtBond.getDebt(), debtBond.getDate());
                listener.onDownloadSuccessful(debt);
            }

            @Override
            public void onDoesNotExist() {
                Debt debt = new Debt(new User(username), debtBond.getDebt(), debtBond.getDate());
                listener.onDownloadSuccessful(debt);
            }

            @Override
            public void onFailure(String errorMessage) {
                Debt debt = new Debt(new User(username), debtBond.getDebt(), debtBond.getDate());
                listener.onDownloadSuccessful(debt);

                Log.e(AppConfig.APPLICATION_LOG_TAG, errorMessage);
            }
        });
    }

    private void downloadGroupsIDs(OnDownloadDataListListener<String> listener) {
        mUsers.document(mUsername)
                .get()
                .addOnSuccessListener(document -> {
                    @SuppressWarnings("unchecked")
                    List<String> ids = (List<String>) Objects.requireNonNull(document.getData())
                            .get(AppConfig.GROUPS_FIELD_NAME);
                    listener.onDownloadSuccessful(ids);
                })
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfig.ERROR_DOWNLOAD_GROUP_IDS);
                });
    }

    private void downloadSingleDebts(OnDownloadDataListListener<Debt> listener) {
        mDebts.document(mUsername)
                .collection(AppConfig.FRIENDS_COLLECTION_NAME)
                .get()
                .addOnSuccessListener(documents -> {
                    List<DebtBond> debtBonds = new ArrayList<>();
                    for (DocumentSnapshot document : documents) {
                        if (document.exists()) {
                            Map<String, Object> data = document.getData();
                            if (data != null) {
                                debtBonds.add(new DebtBond(document.getId(), data));
                            }
                        }
                    }
                    continueDownloadSingleDebts(debtBonds, listener);
                })
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfig.ERROR_DOWNLOAD_SINGLE_DEBTS);
                });
    }

    private void continueDownloadSingleDebts(List<DebtBond> debtBonds, OnDownloadDataListListener<Debt> listener) {
        mSize += debtBonds.size();
        if (mSize == mCount) {
            listener.onDownloadSuccessful(mList);
        }
        for (DebtBond debtBond : debtBonds) {
            String username = debtBond.getUsername();
            String debt = debtBond.getDebt();
            String date = debtBond.getDate();
            FirebaseUtilities.findUserByUsername(username, new OnFindUserListener() {

                @Override
                public void onSuccessful(User user) {
                    Debt debtObj = new Debt(user, debt, date);
                    downloadDebtImage(debtObj, listener);
                }

                @Override
                public void onDoesNotExist() {
                    Debt debtObj = new Debt(new User(username), debt, date);
                    addSingleDebt(debtObj, listener);
                }

                @Override
                public void onFailure(String errorMessage) {
                    Debt debtObj = new Debt(new User(username), debt, date);
                    addSingleDebt(debtObj, listener);

                    Log.e(AppConfig.APPLICATION_LOG_TAG, errorMessage);
                }
            });
        }
    }

    private void downloadGroupDebts(List<String> groupIDs, OnDownloadDataListListener<Debt> listener) {
        mSize = groupIDs.size();
        if (mSize == 0) {
            downloadSingleDebts(listener);
        }
        CollectionReference collRef = mDatabase.collection(AppConfig.GROUP_DEBTS_COLLECTION_NAME);
        for (String id : groupIDs) {
            collRef.document(id)
                    .get()
                    .addOnSuccessListener(document -> {
                        Map<String, Object> data = document.getData();
                        if (data != null) {
                            GroupDebt groupDebt = new GroupDebt(data, document.getId());
                            downloadGroupImage(groupDebt, listener);
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (e.getMessage() != null) {
                            Log.e(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                        }
                        listener.onFailure(ErrorsConfig.ERROR_DOWNLOAD_GROUP_DEBTS);
                    });
        }
    }

    private void downloadGroupImage(GroupDebt group, OnDownloadDataListListener<Debt> listener) {
        mStorage.child(AppConfig.GROUP_DEBTS_COLLECTION_NAME)
                .child(group.getId())
                .getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    group.setUriImage(uri);
                    addGroupDebt(group, listener);
                })
                .addOnFailureListener(e -> {
                    int errorCode = ((StorageException) e).getErrorCode();
                    if (errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) {
                        useDefaultImage(group, mStorage.child(AppConfig.GROUP_DEBTS_COLLECTION_NAME), listener);
                    } else {
                        if (e.getMessage() != null) {
                            Log.e(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                        }
                        listener.onFailure(ErrorsConfig.ERROR_DOWNLOAD_GROUP_IMAGE + group.getId());
                    }
                });
    }

    private void downloadDebtImage(Debt debt, OnDownloadDataListListener<Debt> listener) {
        mStorage.child(AppConfig.USERS_COLLECTION_NAME)
                .child(debt.getUser().getUsername())
                .getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    debt.setUriImage(uri);
                    addSingleDebt(debt, listener);
                })
                .addOnFailureListener(e -> {
                    int errorCode = ((StorageException) e).getErrorCode();
                    if (errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) {
                        useDefaultImage(debt, mStorage.child(AppConfig.USERS_COLLECTION_NAME), listener);
                    } else {
                        if (e.getMessage() != null) {
                            Log.e(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                        }
                        listener.onFailure(ErrorsConfig.ERROR_DOWNLOAD_USER_IMAGE + debt.getUser().getUsername());
                    }
                });
    }

    private void useDefaultImage(Debt debt, StorageReference reference, OnDownloadDataListListener<Debt> listener) {
        reference.child(AppConfig.DEFAULT_IMAGE_VALUE)
                .getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    debt.setUriImage(uri);
                    if (debt instanceof GroupDebt) {
                        addGroupDebt((GroupDebt) debt, listener);
                    } else {
                        addSingleDebt(debt, listener);
                    }
                })
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    if (debt instanceof GroupDebt) {
                        listener.onFailure(ErrorsConfig.ERROR_DOWNLOAD_DEFAULT_GROUP_IMAGE + ((GroupDebt) debt).getId());
                    } else {
                        listener.onFailure(ErrorsConfig.ERROR_DOWNLOAD_DEFAULT_USER_IMAGE + debt.getUser().getUsername());
                    }
                });
    }

    private void addGroupDebt(GroupDebt debt, OnDownloadDataListListener<Debt> listener) {
        mList.add(debt);
        mCount++;
        if (mCount == mSize) {
            downloadSingleDebts(listener);
        }
    }

    private void addSingleDebt(Debt debt, OnDownloadDataListListener<Debt> listener) {
        mList.add(debt);
        mCount++;
        if (mCount == mSize) {
            listener.onDownloadSuccessful(mList);
        }
    }
}
