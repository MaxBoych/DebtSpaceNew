package com.example.debtspace.main.viewmodels;

import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.debtspace.config.AppConfig;
import com.example.debtspace.main.interfaces.OnDatabaseEventListener;
import com.example.debtspace.main.interfaces.OnDownloadDataListListener;
import com.example.debtspace.main.interfaces.OnFindUserListener;
import com.example.debtspace.main.repositories.ProfileRepository;
import com.example.debtspace.models.User;

import java.util.List;

public class ProfileViewModel extends ViewModel {

    private User mUser;
    private MutableLiveData<AppConfig.ProfileLoadStageState> mLoadState;
    private MutableLiveData<AppConfig.EventStageState> mEventState;
    private String mErrorMessage;
    private Uri mUri;

    private Context mContext;

    public ProfileViewModel() {
        mUser = new User();

        mLoadState = new MutableLiveData<>();
        mLoadState.setValue(AppConfig.ProfileLoadStageState.NONE);
        mEventState = new MutableLiveData<>();
        mEventState.setValue(AppConfig.EventStageState.NONE);
        mErrorMessage = AppConfig.DEFAULT_ERROR_VALUE;
    }

    public void setContext(Context context) {
        mContext = context;
    }

    public void observeUserDataEvents() {
        new ProfileRepository(mContext).observeUserDataEvents(new OnDatabaseEventListener<User>() {
            @Override
            public void onAdded(User object) {}

            @Override
            public void onModified(User object) {
                setUser(object);
                setEventState(AppConfig.EventStageState.MODIFIED);
            }

            @Override
            public void onRemoved(User object) {}

            @Override
            public void onFailure(String errorMessage) {
                setErrorMessage(errorMessage);
                setEventState(AppConfig.EventStageState.FAIL);
            }
        });
    }

    private void setEventState(AppConfig.EventStageState state) {
        mEventState.setValue(state);
    }

    public void downloadUserData() {
        mLoadState.setValue(AppConfig.ProfileLoadStageState.PROGRESS);
        new ProfileRepository(mContext).downloadUserData(new OnFindUserListener() {

            @Override
            public void onSuccessful(User user) {
                setUser(user);
                setLoadState(AppConfig.ProfileLoadStageState.SUCCESS_LOAD_DATA);
            }

            @Override
            public void onDoesNotExist() {}

            @Override
            public void onFailure(String errorMessage) {
                setErrorMessage(errorMessage);
                setLoadState(AppConfig.ProfileLoadStageState.FAIL);
            }
        });
    }

    public void downloadUserImage() {
        mLoadState.setValue(AppConfig.ProfileLoadStageState.PROGRESS);
        new ProfileRepository(mContext).downloadImage(new OnDownloadDataListListener<Uri>() {
            @Override
            public void onDownloadSuccessful(List<Uri> list) {
                setUri(list.get(0));
                setLoadState(AppConfig.ProfileLoadStageState.SUCCESS_LOAD_IMAGE);
            }

            @Override
            public void onFailure(String errorMessage) {
                setErrorMessage(errorMessage);
                setLoadState(AppConfig.ProfileLoadStageState.FAIL);
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

    public LiveData<AppConfig.ProfileLoadStageState> getState() {
        return mLoadState;
    }

    public LiveData<AppConfig.EventStageState> getEventState() {
        return mEventState;
    }

    public String getErrorMessage() {
        return mErrorMessage;
    }

    private void setLoadState(AppConfig.ProfileLoadStageState state) {
        mLoadState.setValue(state);
    }
}
