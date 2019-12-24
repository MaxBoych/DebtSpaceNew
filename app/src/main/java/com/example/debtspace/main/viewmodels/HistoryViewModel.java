package com.example.debtspace.main.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.debtspace.config.Configuration;
import com.example.debtspace.main.interfaces.OnDownloadDataListener;
import com.example.debtspace.models.HistoryItem;
import com.example.debtspace.main.repositories.HistoryRepository;

import java.util.List;

public class HistoryViewModel extends ViewModel {
    private MutableLiveData<List<HistoryItem>> mDataList;
    private MutableLiveData<Configuration.LoadStageState> mState;
    private MutableLiveData<String> mErrorMessage;

    public HistoryViewModel() {
        mDataList = new MutableLiveData<>();
        mState = new MutableLiveData<>();
        mErrorMessage = new MutableLiveData<>();
        mState.setValue(Configuration.LoadStageState.NONE);
        mErrorMessage.setValue(Configuration.DEFAULT_ERROR_VALUE);
    }

    public void downloadHistoryList() {
        mState.setValue(Configuration.LoadStageState.PROGRESS);
        new HistoryRepository().downloadHistoryData(new OnDownloadDataListener<HistoryItem>() {

            @Override
            public void onDownloadSuccessful(List<HistoryItem> data) {
                mDataList.setValue(data);
                mState.setValue(Configuration.LoadStageState.SUCCESS);
            }

            @Override
            public void onFailure(String errorMessage) {
                mErrorMessage.setValue(errorMessage);
                mState.setValue(Configuration.LoadStageState.FAIL);
            }
        });
    }

    public LiveData<List<HistoryItem>> getDataList() {
        return mDataList;
    }

    public LiveData<Configuration.LoadStageState> getListState() {
        return mState;
    }

    public LiveData<String> getErrorMessage() {
        return mErrorMessage;
    }
}
