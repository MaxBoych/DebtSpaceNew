package com.example.debtspace.main.viewmodels;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.debtspace.config.AppConfig;
import com.example.debtspace.main.interfaces.OnUpdateDataListener;
import com.example.debtspace.main.repositories.FriendRequestRepository;

public class FriendRequestViewModel extends ViewModel {

    private MutableLiveData<AppConfig.LoadStageState> mLoadState;
    private MutableLiveData<String> mErrorMessage;

    public FriendRequestViewModel() {
        mLoadState = new MutableLiveData<>();
        mErrorMessage = new MutableLiveData<>();
        mLoadState.setValue(AppConfig.LoadStageState.NONE);
        mErrorMessage.setValue(AppConfig.DEFAULT_ERROR_VALUE);
    }

    public void sendFriendRequest(String username, Context context) {
        mLoadState.setValue(AppConfig.LoadStageState.PROGRESS);
        new FriendRequestRepository(context).checkExistenceFriends(username, new OnUpdateDataListener() {
            @Override
            public void onUpdateSuccessful() {
                updateStateToSuccess();
            }

            @Override
            public void onFailure(String errorMessage) {
                updateError(errorMessage);
            }
        });
    }

    private void updateStateToSuccess() {
        mLoadState.setValue(AppConfig.LoadStageState.SUCCESS);
    }

    private void updateError(String errorMessage) {
        mErrorMessage.setValue(errorMessage);
        mLoadState.setValue(AppConfig.LoadStageState.FAIL);
    }

    public MutableLiveData<AppConfig.LoadStageState> getLoadState() {
        return mLoadState;
    }

    public LiveData<String> getErrorMessage() {
        return mErrorMessage;
    }
}
