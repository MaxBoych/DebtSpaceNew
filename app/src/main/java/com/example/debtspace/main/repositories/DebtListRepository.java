package com.example.debtspace.main.repositories;

import android.util.Log;

import com.example.debtspace.config.Configuration;
import com.example.debtspace.main.interfaces.OnDownloadDataListener;
import com.example.debtspace.main.interfaces.OnFindUserListener;
import com.example.debtspace.models.Debt;
import com.example.debtspace.models.DebtBond;
import com.example.debtspace.models.GroupDebt;
import com.example.debtspace.models.User;
import com.example.debtspace.utilities.FirebaseUtilities;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DebtListRepository {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseFirestore mDatabase;
    private StorageReference mStorage;
    private String mUsername;

    private List<Debt> mList;
    private int mSize;
    private int mCount;

    public DebtListRepository() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference();
        mUsername = Objects.requireNonNull(mFirebaseAuth
                .getCurrentUser())
                .getDisplayName();

        mList = new ArrayList<>();
    }

    public void uploadDebtListData(OnDownloadDataListener<Debt> listener) {
        uploadGroupsIDs(new OnDownloadDataListener<String>() {
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

    private void uploadGroupsIDs(OnDownloadDataListener<String> listener) {
        mDatabase.collection(Configuration.USERS_COLLECTION_NAME)
                .document(mUsername)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    @SuppressWarnings("unchecked")
                    List<String> ids = (List<String>) Objects.requireNonNull(documentSnapshot.getData())
                            .get(Configuration.GROUPS_FIELD_NAME);
                    listener.onDownloadSuccessful(ids);
                })
                .addOnFailureListener(e ->
                        listener.onFailure(e.getMessage())
                );
    }

    private void downloadSingleDebts(OnDownloadDataListener<Debt> listener) {
        mDatabase.collection(Configuration.DEBTS_COLLECTION_NAME)
                .document(mUsername)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> map = documentSnapshot.getData();
                        List<DebtBond> debtBonds = new ArrayList<>();
                        for (String partnerUsername : Objects.requireNonNull(map).keySet()) {
                            debtBonds.add(new DebtBond(partnerUsername, (String) map.get(partnerUsername)));
                        }
                        continueDownloadSingleDebts(debtBonds, listener);
                    } else {
                        listener.onDownloadSuccessful(null);
                    }
                })
                .addOnFailureListener(e ->
                        listener.onFailure(e.getMessage())
                );
    }

    private void continueDownloadSingleDebts(List<DebtBond> debtBonds, OnDownloadDataListener<Debt> listener) {
        mSize += debtBonds.size();
        for (DebtBond debtBond : debtBonds) {
            String username = debtBond.getPartnerUsername();
            String debt = debtBond.getDebt();
            FirebaseUtilities.findUserByUsername(username, new OnFindUserListener() {

                @Override
                public void onSuccessful(User user) {
                    Debt debtObj = new Debt(user, debt);
                    downloadDebtImage(debtObj, listener);
                }

                @Override
                public void onDoesNotExist() {
                    Debt debtObj = new Debt(new User(username), debt);
                    addSingleDebt(debtObj, listener);

                    Log.d("#DS findUser failed", "User with username " + username + " does not exist.");
                }

                @Override
                public void onFailure(String errorMessage) {
                    Debt debtObj = new Debt(new User(username), debt);
                    addSingleDebt(debtObj, listener);

                    Log.d("#DS findUser failed", "Error find username " + username + " ; " + errorMessage);
                }
            });
        }
    }

    private void downloadGroupDebts(List<String> groupIDs, OnDownloadDataListener<Debt> listener) {
        mSize = groupIDs.size();
        if (mSize == 0) {
            downloadSingleDebts(listener);
        }
        CollectionReference collRef = mDatabase.collection(Configuration.GROUP_DEBTS_COLLECTION_NAME);
        for (String id : groupIDs) {
            collRef.document(id)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        Map<String, Object> data = documentSnapshot.getData();
                        GroupDebt groupDebt = new GroupDebt(Objects.requireNonNull(data));
                        groupDebt.setID(id);
                        downloadGroupImage(groupDebt, listener);
                    })
                    .addOnFailureListener(e ->
                            listener.onFailure(e.getMessage())
                    );
        }
    }

    private void downloadGroupImage(GroupDebt group, OnDownloadDataListener<Debt> listener) {
        mStorage.child(Configuration.GROUP_DEBTS_COLLECTION_NAME)
                .child(group.getId())
                .getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    group.setUriImage(uri);
                    addGroupDebt(group, listener);
                })
                .addOnFailureListener(e -> {
                    int errorCode = ((StorageException) e).getErrorCode();
                    if (errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) {
                        useDefaultImage(group, mStorage.child(Configuration.GROUP_DEBTS_COLLECTION_NAME), listener);
                    } else {
                        listener.onFailure(e.getMessage());
                    }
                });
    }

    private void downloadDebtImage(Debt debt, OnDownloadDataListener<Debt> listener) {
        mStorage.child(Configuration.USERS_COLLECTION_NAME)
                .child(debt.getUser().getUsername())
                .getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    debt.setUriImage(uri);
                    addSingleDebt(debt, listener);
                })
                .addOnFailureListener(e -> {
                    int errorCode = ((StorageException) e).getErrorCode();
                    if (errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) {
                        useDefaultImage(debt, mStorage.child(Configuration.USERS_COLLECTION_NAME), listener);
                    } else {
                        listener.onFailure(e.getMessage());
                    }
                });
    }

    private void useDefaultImage(Debt debt, StorageReference reference, OnDownloadDataListener<Debt> listener) {
        reference.child(Configuration.DEFAULT_IMAGE_VALUE)
                .getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    debt.setUriImage(uri);
                    if (debt instanceof GroupDebt) {
                        addGroupDebt((GroupDebt) debt, listener);
                    } else {
                        addSingleDebt(debt, listener);
                    }
                })
                .addOnFailureListener(e ->
                        listener.onFailure(e.getMessage())
                );
    }

    private void addGroupDebt(GroupDebt debt, OnDownloadDataListener<Debt> listener) {
        mList.add(debt);
        mCount++;
        if (mCount == mSize) {
            downloadSingleDebts(listener);
        }
    }

    private void addSingleDebt(Debt debt, OnDownloadDataListener<Debt> listener) {
        mList.add(debt);
        mCount++;
        if (mCount == mSize) {
            listener.onDownloadSuccessful(mList);
        }
    }
}
