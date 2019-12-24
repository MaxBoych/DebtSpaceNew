package com.example.debtspace.main.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.debtspace.config.Configuration;
import com.example.debtspace.main.interfaces.OnFindUserListener;
import com.example.debtspace.main.repositories.ProfileRepository;
import com.example.debtspace.models.User;

public class ProfileViewModel extends ViewModel {

    private MutableLiveData<User> mUser;
    private MutableLiveData<Configuration.LoadStageState> mState;
    private MutableLiveData<String> mErrorMessage;

    public ProfileViewModel() {
        mUser = new MutableLiveData<>();
        mState = new MutableLiveData<>();
        mErrorMessage = new MutableLiveData<>();
        mState.setValue(Configuration.LoadStageState.PROGRESS);
        mErrorMessage.setValue(Configuration.DEFAULT_ERROR_VALUE);
    }

    public void downloadUserData() {
        new ProfileRepository().downloadUserData(new OnFindUserListener() {

            @Override
            public void onSuccessful(User user) {
                mUser.setValue(user);
                mState.setValue(Configuration.LoadStageState.SUCCESS);
            }

            @Override
            public void onDoesNotExist() {}

            @Override
            public void onFailure(String errorMessage) {
                mErrorMessage.setValue(errorMessage);
                mState.setValue(Configuration.LoadStageState.FAIL);

            }
        });
    }

    public User getUser() {
        return mUser.getValue();
    }

    public LiveData<Configuration.LoadStageState> getState() {
        return mState;
    }

    public LiveData<String> getErrorMessage() {
        return mErrorMessage;
    }
}
