package com.example.debtspace.main.viewmodels;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.debtspace.config.Configuration;
import com.example.debtspace.main.interfaces.OnUpdateDataListener;
import com.example.debtspace.main.repositories.StrikeRepository;
import com.example.debtspace.models.HistoryItem;

public class StrikeViewModel extends ViewModel {

    private MutableLiveData<Configuration.LoadStageState> mState;
    private MutableLiveData<String> mErrorMessage;

    public StrikeViewModel() {
        mState = new MutableLiveData<>();
        mErrorMessage = new MutableLiveData<>();
        mState.setValue(Configuration.LoadStageState.NONE);
        mErrorMessage.setValue(Configuration.DEFAULT_ERROR_VALUE);
    }

    public void doStrike(HistoryItem item, Context context) {
        mState.setValue(Configuration.LoadStageState.PROGRESS);
        new StrikeRepository(context).doStrike(item, new OnUpdateDataListener() {
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
