package com.example.debtspace.main.viewmodels;

import android.net.Uri;
import android.widget.ProgressBar;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.debtspace.config.Configuration;
import com.example.debtspace.main.interfaces.OnDownloadDataListener;
import com.example.debtspace.main.interfaces.OnUpdateDataListener;
import com.example.debtspace.main.repositories.ImageManagementRepository;

import java.util.List;

public class ImageManagementViewModel extends ViewModel {

    private Uri mImageUri;
    private MutableLiveData<Configuration.ImageStageState> mState;
    private MutableLiveData<String> mErrorMessage;

    public ImageManagementViewModel() {
        mImageUri = null;
        mState = new MutableLiveData<>();
        mErrorMessage = new MutableLiveData<>();
        mState.setValue(Configuration.ImageStageState.NONE);
        mErrorMessage.setValue(Configuration.DEFAULT_ERROR_VALUE);
    }

    public void uploadImage(Uri uri, ProgressBar progressBar, String id) {
        mState.setValue(Configuration.ImageStageState.PROGRESS);
        new ImageManagementRepository(id).uploadImage(uri, progressBar, new OnUpdateDataListener() {
            @Override
            public void onUpdateSuccessful() {
                setImageUri(null);
                mState.setValue(Configuration.ImageStageState.UPLOAD_SUCCESS);
            }

            @Override
            public void onFailure(String errorMessage) {
                mErrorMessage.setValue(errorMessage);
                mState.setValue(Configuration.ImageStageState.FAIL);
            }
        });
    }

    public void downloadImage(String id) {
        mState.setValue(Configuration.ImageStageState.PROGRESS);
        new ImageManagementRepository(id).downloadImage(new OnDownloadDataListener<Uri>() {
            @Override
            public void onDownloadSuccessful(List<Uri> list) {
                setImageUri(list.get(0));
                mState.setValue(Configuration.ImageStageState.DOWNLOAD_SUCCESS);
            }

            @Override
            public void onFailure(String errorMessage) {
                mErrorMessage.setValue(errorMessage);
                mState.setValue(Configuration.ImageStageState.FAIL);
            }
        });
    }

    public void deleteImage(String id) {
        mState.setValue(Configuration.ImageStageState.PROGRESS);
        new ImageManagementRepository(id).deleteImage(new OnUpdateDataListener() {
            @Override
            public void onUpdateSuccessful() {
                setImageUri(null);
                mState.setValue(Configuration.ImageStageState.DELETE_SUCCESS);
            }

            @Override
            public void onFailure(String errorMessage) {
                mErrorMessage.setValue(errorMessage);
                mState.setValue(Configuration.ImageStageState.FAIL);
            }
        });
    }



    private void setImageUri(Uri uri) {
        mImageUri = uri;
    }

    public Uri getImageUri() {
        return mImageUri;
    }

    public MutableLiveData<Configuration.ImageStageState> getState() {
        return mState;
    }

    public LiveData<String> getErrorMessage() {
        return mErrorMessage;
    }
}
