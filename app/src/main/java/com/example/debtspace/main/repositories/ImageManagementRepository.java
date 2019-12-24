package com.example.debtspace.main.repositories;

import android.net.Uri;
import android.widget.ProgressBar;

import com.example.debtspace.config.Configuration;
import com.example.debtspace.main.interfaces.OnDownloadDataListener;
import com.example.debtspace.main.interfaces.OnUpdateDataListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ImageManagementRepository {

    private FirebaseAuth mFirebaseAuth;
    private StorageReference mStorage;
    private String mID;

    public ImageManagementRepository(String id) {
        mFirebaseAuth = FirebaseAuth.getInstance();

        if (id.equals(Configuration.NONE_ID)) {
            mStorage = FirebaseStorage.getInstance()
                    .getReference()
                    .child(Configuration.USERS_COLLECTION_NAME);

            mID = Objects.requireNonNull(Objects.requireNonNull(mFirebaseAuth
                    .getCurrentUser())
                    .getDisplayName());
        } else {
            mStorage = FirebaseStorage.getInstance()
                    .getReference()
                    .child(Configuration.GROUP_DEBTS_COLLECTION_NAME);

            mID = id;
        }
    }

    public void uploadImage(Uri uri, ProgressBar progressBar, OnUpdateDataListener listener) {
        mStorage.child(mID)
                .putFile(uri)
                .addOnSuccessListener(taskSnapshot ->
                        listener.onUpdateSuccessful())
                .addOnFailureListener(e ->
                        listener.onFailure(e.getMessage()
                        ))
                .addOnProgressListener(taskSnapshot -> {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    progressBar.setProgress((int) progress);
                });
    }

    public void downloadImage(OnDownloadDataListener<Uri> listener) {
        mStorage.child(mID)
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

    public void deleteImage(OnUpdateDataListener listener) {
        mStorage.child(mID)
                .delete()
                .addOnSuccessListener(aVoid ->
                        listener.onUpdateSuccessful())
                .addOnFailureListener(e ->
                        listener.onFailure(e.getMessage())
                );
    }
}
