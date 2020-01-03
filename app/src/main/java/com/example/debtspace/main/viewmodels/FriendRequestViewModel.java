package com.example.debtspace.main.viewmodels;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.debtspace.config.Configuration;
import com.example.debtspace.main.interfaces.OnUpdateDataListener;
import com.example.debtspace.main.repositories.FriendRequestRepository;

public class FriendRequestViewModel extends ViewModel {

    private MutableLiveData<Configuration.LoadStageState> mState;
    private MutableLiveData<String> mErrorMessage;

    public FriendRequestViewModel() {
        mState = new MutableLiveData<>();
        mErrorMessage = new MutableLiveData<>();
        mState.setValue(Configuration.LoadStageState.NONE);
        mErrorMessage.setValue(Configuration.DEFAULT_ERROR_VALUE);
    }

    public void sendFriendRequest(String username, Context context) {
        mState.setValue(Configuration.LoadStageState.PROGRESS);
        new FriendRequestRepository(context).checkExistenceFriends(username, new OnUpdateDataListener() {
            @Override
            public void onUpdateSuccessful() {
                mState.setValue(Configuration.LoadStageState.SUCCESS);
            }

            @Override
            public void onFailure(String errorMessage) {
                mErrorMessage.setValue(errorMessage);
                mState.setValue(Configuration.LoadStageState.FAIL);
            }
        });
    }

    public MutableLiveData<Configuration.LoadStageState> getState() {
        return mState;
    }

    public LiveData<String> getErrorMessage() {
        return mErrorMessage;
    }
}
