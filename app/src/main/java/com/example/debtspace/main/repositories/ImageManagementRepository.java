package com.example.debtspace.main.repositories;

import android.content.Context;
import android.net.Uri;
import android.widget.ProgressBar;

import com.example.debtspace.application.DebtSpaceApplication;
import com.example.debtspace.config.Configuration;
import com.example.debtspace.main.interfaces.OnDownloadDataListener;
import com.example.debtspace.main.interfaces.OnUpdateDataListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ImageManagementRepository {

    private StorageReference mStorage;
    private String mID;

    public ImageManagementRepository(String id, Context context) {
        if (id.equals(Configuration.NONE_ID)) {
            mStorage = DebtSpaceApplication.from(context).getStorage()
                    .child(Configuration.USERS_COLLECTION_NAME);

            mID = DebtSpaceApplication.from(context).getUsername();
        } else {
            mStorage = DebtSpaceApplication.from(context).getStorage()
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
