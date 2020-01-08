package com.example.debtspace.main.repositories;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.ProgressBar;

import com.example.debtspace.application.DebtSpaceApplication;
import com.example.debtspace.config.AppConfig;
import com.example.debtspace.config.ErrorsConfig;
import com.example.debtspace.main.interfaces.OnDownloadDataListListener;
import com.example.debtspace.main.interfaces.OnUpdateDataListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ImageManagementRepository {

    private StorageReference mStorage;
    private String mID;

    public ImageManagementRepository(String id, Context context) {
        if (id.equals(AppConfig.NONE_ID)) {
            mStorage = DebtSpaceApplication.from(context).getStorage()
                    .child(AppConfig.USERS_COLLECTION_NAME);

            mID = DebtSpaceApplication.from(context).getUsername();
        } else {
            mStorage = DebtSpaceApplication.from(context).getStorage()
                    .child(AppConfig.GROUP_DEBTS_COLLECTION_NAME);

            mID = id;
        }
    }

    public void uploadImage(Uri uri, ProgressBar progressBar, OnUpdateDataListener listener) {
        mStorage.child(mID)
                .putFile(uri)
                .addOnSuccessListener(taskSnapshot ->
                        listener.onUpdateSuccessful())
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfig.ERROR_UPLOAD_IMAGE);
                });
    }

    public void downloadImage(OnDownloadDataListListener<Uri> listener) {
        mStorage.child(mID)
                .getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    List<Uri> list = new ArrayList<>();
                    list.add(uri);
                    listener.onDownloadSuccessful(list);
                })
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfig.ERROR_DOWNLOAD_IMAGE);
                });
    }

    public void deleteImage(OnUpdateDataListener listener) {
        mStorage.child(mID)
                .delete()
                .addOnSuccessListener(aVoid ->
                        listener.onUpdateSuccessful())
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null) {
                        Log.e(AppConfig.APPLICATION_LOG_TAG, e.getMessage());
                    }
                    listener.onFailure(ErrorsConfig.ERROR_DELETE_IMAGE);
                });
    }
}
