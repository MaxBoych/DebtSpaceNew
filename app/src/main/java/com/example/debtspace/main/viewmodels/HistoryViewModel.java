package com.example.debtspace.main.viewmodels;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.debtspace.config.Configuration;
import com.example.debtspace.main.interfaces.OnDownloadDataListener;
import com.example.debtspace.models.HistoryItem;
import com.example.debtspace.main.repositories.HistoryRepository;

import java.util.List;

public class HistoryViewModel extends ViewModel {
    private MutableLiveData<List<HistoryItem>> mList;
    private MutableLiveData<Configuration.LoadStageState> mState;
    private MutableLiveData<String> mErrorMessage;

    public HistoryViewModel() {
        mList = new MutableLiveData<>();
        mState = new MutableLiveData<>();
        mErrorMessage = new MutableLiveData<>();
        mState.setValue(Configuration.LoadStageState.NONE);
        mErrorMessage.setValue(Configuration.DEFAULT_ERROR_VALUE);
    }

    public void downloadHistoryList(Context context) {
        mState.setValue(Configuration.LoadStageState.PROGRESS);
        new HistoryRepository(context).downloadHistoryData(new OnDownloadDataListener<HistoryItem>() {

            @Override
            public void onDownloadSuccessful(List<HistoryItem> list) {
                mList.setValue(list);
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
        return mList;
    }

    public LiveData<Configuration.LoadStageState> getListState() {
        return mState;
    }

    public LiveData<String> getErrorMessage() {
        return mErrorMessage;
    }
}
