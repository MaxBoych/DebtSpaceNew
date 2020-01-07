package com.example.debtspace.main.viewmodels;

import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.debtspace.config.Configuration;
import com.example.debtspace.main.interfaces.OnDownloadDataListListener;
import com.example.debtspace.main.interfaces.OnFindUserListener;
import com.example.debtspace.main.repositories.ProfileRepository;
import com.example.debtspace.models.User;

import java.util.List;

public class ProfileViewModel extends ViewModel {

    private User mUser;
    private MutableLiveData<Configuration.ProfileLoadStageState> mState;
    private String mErrorMessage;
    private Uri mUri;

    private Context mContext;

    public ProfileViewModel() {
        mUser = new User();

        mState = new MutableLiveData<>();
        mState.setValue(Configuration.ProfileLoadStageState.NONE);
        mErrorMessage = Configuration.DEFAULT_ERROR_VALUE;
    }

    public void setContext(Context context) {
        mContext = context;
    }

    public void downloadUserData() {
        mState.setValue(Configuration.ProfileLoadStageState.PROGRESS);
        new ProfileRepository(mContext).downloadUserData(new OnFindUserListener() {

            @Override
            public void onSuccessful(User user) {
                setUser(user);
                mState.setValue(Configuration.ProfileLoadStageState.SUCCESS_LOAD_DATA);
            }

            @Override
            public void onDoesNotExist() {}

            @Override
            public void onFailure(String errorMessage) {
                setErrorMessage(errorMessage);
                mState.setValue(Configuration.ProfileLoadStageState.FAIL);
            }
        });
    }

    public void downloadUserImage() {
        mState.setValue(Configuration.ProfileLoadStageState.PROGRESS);
        new ProfileRepository(mContext).downloadImage(new OnDownloadDataListListener<Uri>() {
            @Override
            public void onDownloadSuccessful(List<Uri> list) {
                setUri(list.get(0));
                mState.setValue(Configuration.ProfileLoadStageState.SUCCESS_LOAD_IMAGE);
            }

            @Override
            public void onFailure(String errorMessage) {
                setErrorMessage(errorMessage);
                mState.setValue(Configuration.ProfileLoadStageState.FAIL);
            }
        });
    }

    private void setUser(User user) {
        mUser = user;
    }

    private void setUri(Uri uri) {
        mUri = uri;
    }

    private void setErrorMessage(String errorMessage) {
        mErrorMessage = errorMessage;
    }

    public Uri getUri() {
        return mUri;
    }

    public User getUser() {
        return mUser;
    }

    public LiveData<Configuration.ProfileLoadStageState> getState() {
        return mState;
    }

    public String getErrorMessage() {
        return mErrorMessage;
    }
}
