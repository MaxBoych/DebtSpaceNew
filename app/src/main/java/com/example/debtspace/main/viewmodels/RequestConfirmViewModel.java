package com.example.debtspace.main.viewmodels;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.debtspace.config.AppConfig;
import com.example.debtspace.main.interfaces.OnUpdateDataListener;
import com.example.debtspace.main.repositories.RequestConfirmRepository;

public class RequestConfirmViewModel extends ViewModel {

    private MutableLiveData<AppConfig.LoadStageState> mLoadState;
    private MutableLiveData<String> mErrorMessage;

    public RequestConfirmViewModel() {
        mLoadState = new MutableLiveData<>();
        mErrorMessage = new MutableLiveData<>();
        mLoadState.setValue(AppConfig.LoadStageState.NONE);
        mErrorMessage.setValue(AppConfig.DEFAULT_ERROR_VALUE);
    }

    public void acceptFriendRequest(String username, Context context) {
        mLoadState.setValue(AppConfig.LoadStageState.PROGRESS);
        new RequestConfirmRepository(context).acceptFriendRequest(username, new OnUpdateDataListener() {
            @Override
            public void onUpdateSuccessful() {
                mLoadState.setValue(AppConfig.LoadStageState.SUCCESS);
            }

            @Override
            public void onFailure(String errorMessage) {
                mErrorMessage.setValue(errorMessage);
                mLoadState.setValue(AppConfig.LoadStageState.FAIL);
            }
        });
    }

    public void rejectFriendRequest(String username, Context context) {
        mLoadState.setValue(AppConfig.LoadStageState.PROGRESS);
        new RequestConfirmRepository(context).rejectFriendRequest(username, new OnUpdateDataListener() {
            @Override
            public void onUpdateSuccessful() {
                mLoadState.setValue(AppConfig.LoadStageState.SUCCESS);
            }

            @Override
            public void onFailure(String errorMessage) {
                mErrorMessage.setValue(errorMessage);
                mLoadState.setValue(AppConfig.LoadStageState.FAIL);
            }
        });
    }

    public MutableLiveData<AppConfig.LoadStageState> getLoadState() {
        return mLoadState;
    }

    public LiveData<String> getErrorMessage() {
        return mErrorMessage;
    }
}
