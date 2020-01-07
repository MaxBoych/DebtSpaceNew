package com.example.debtspace.main.viewmodels;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.debtspace.config.Configuration;
import com.example.debtspace.main.interfaces.OnUpdateDataListener;
import com.example.debtspace.main.repositories.RequestConfirmRepository;

public class RequestConfirmViewModel extends ViewModel {

    private MutableLiveData<Configuration.LoadStageState> mLoadState;
    private MutableLiveData<String> mErrorMessage;

    public RequestConfirmViewModel() {
        mLoadState = new MutableLiveData<>();
        mErrorMessage = new MutableLiveData<>();
        mLoadState.setValue(Configuration.LoadStageState.NONE);
        mErrorMessage.setValue(Configuration.DEFAULT_ERROR_VALUE);
    }

    public void acceptFriendRequest(String username, Context context) {
        mLoadState.setValue(Configuration.LoadStageState.PROGRESS);
        new RequestConfirmRepository(context).acceptFriendRequest(username, new OnUpdateDataListener() {
            @Override
            public void onUpdateSuccessful() {
                mLoadState.setValue(Configuration.LoadStageState.SUCCESS);
            }

            @Override
            public void onFailure(String errorMessage) {
                mErrorMessage.setValue(errorMessage);
                mLoadState.setValue(Configuration.LoadStageState.FAIL);
            }
        });
    }

    public void rejectFriendRequest(String username, Context context) {
        mLoadState.setValue(Configuration.LoadStageState.PROGRESS);
        new RequestConfirmRepository(context).rejectFriendRequest(username, new OnUpdateDataListener() {
            @Override
            public void onUpdateSuccessful() {
                mLoadState.setValue(Configuration.LoadStageState.SUCCESS);
            }

            @Override
            public void onFailure(String errorMessage) {
                mErrorMessage.setValue(errorMessage);
                mLoadState.setValue(Configuration.LoadStageState.FAIL);
            }
        });
    }

    public MutableLiveData<Configuration.LoadStageState> getLoadState() {
        return mLoadState;
    }

    public LiveData<String> getErrorMessage() {
        return mErrorMessage;
    }
}
