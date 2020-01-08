package com.example.debtspace.main.viewmodels;

import android.content.Context;
import android.net.Uri;
import android.widget.ProgressBar;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.debtspace.config.AppConfig;
import com.example.debtspace.main.interfaces.OnDownloadDataListListener;
import com.example.debtspace.main.interfaces.OnUpdateDataListener;
import com.example.debtspace.main.repositories.ImageManagementRepository;

import java.util.List;

public class ImageManagementViewModel extends ViewModel {

    private Uri mImageUri;
    private MutableLiveData<AppConfig.ImageStageState> mLoadState;
    private MutableLiveData<String> mErrorMessage;

    public ImageManagementViewModel() {
        mImageUri = null;
        mLoadState = new MutableLiveData<>();
        mErrorMessage = new MutableLiveData<>();
        mLoadState.setValue(AppConfig.ImageStageState.NONE);
        mErrorMessage.setValue(AppConfig.DEFAULT_ERROR_VALUE);
    }

    public void uploadImage(Uri uri, ProgressBar progressBar, String id, Context context) {
        mLoadState.setValue(AppConfig.ImageStageState.PROGRESS);
        new ImageManagementRepository(id, context).uploadImage(uri, progressBar, new OnUpdateDataListener() {
            @Override
            public void onUpdateSuccessful() {
                setImageUri(null);
                mLoadState.setValue(AppConfig.ImageStageState.UPLOAD_SUCCESS);
            }

            @Override
            public void onFailure(String errorMessage) {
                mErrorMessage.setValue(errorMessage);
                mLoadState.setValue(AppConfig.ImageStageState.FAIL);
            }
        });
    }

    public void downloadImage(String id, Context context) {
        mLoadState.setValue(AppConfig.ImageStageState.PROGRESS);
        new ImageManagementRepository(id, context).downloadImage(new OnDownloadDataListListener<Uri>() {
            @Override
            public void onDownloadSuccessful(List<Uri> list) {
                setImageUri(list.get(0));
                mLoadState.setValue(AppConfig.ImageStageState.DOWNLOAD_SUCCESS);
            }

            @Override
            public void onFailure(String errorMessage) {
                mErrorMessage.setValue(errorMessage);
                mLoadState.setValue(AppConfig.ImageStageState.FAIL);
            }
        });
    }

    public void deleteImage(String id, Context context) {
        mLoadState.setValue(AppConfig.ImageStageState.PROGRESS);
        new ImageManagementRepository(id, context).deleteImage(new OnUpdateDataListener() {
            @Override
            public void onUpdateSuccessful() {
                setImageUri(null);
                mLoadState.setValue(AppConfig.ImageStageState.DELETE_SUCCESS);
            }

            @Override
            public void onFailure(String errorMessage) {
                mErrorMessage.setValue(errorMessage);
                mLoadState.setValue(AppConfig.ImageStageState.FAIL);
            }
        });
    }



    private void setImageUri(Uri uri) {
        mImageUri = uri;
    }

    public Uri getImageUri() {
        return mImageUri;
    }

    public MutableLiveData<AppConfig.ImageStageState> getLoadState() {
        return mLoadState;
    }

    public LiveData<String> getErrorMessage() {
        return mErrorMessage;
    }
}
