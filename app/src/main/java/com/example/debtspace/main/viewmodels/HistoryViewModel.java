package com.example.debtspace.main.viewmodels;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.debtspace.config.Configuration;
import com.example.debtspace.main.interfaces.OnDatabaseEventListener;
import com.example.debtspace.main.interfaces.OnDownloadDataListListener;
import com.example.debtspace.models.HistoryItem;
import com.example.debtspace.main.repositories.HistoryRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoryViewModel extends ViewModel {

    private List<HistoryItem> mList;

    private Context mContext;
    private MutableLiveData<Configuration.LoadStageState> mLoadState;
    private MutableLiveData<Configuration.EventStageState> mEventState;
    private MutableLiveData<String> mErrorMessage;

    private HistoryItem mAddedRequest;

    public HistoryViewModel() {
        mList = new ArrayList<>();
        mLoadState = new MutableLiveData<>();
        mLoadState.setValue(Configuration.LoadStageState.NONE);
        mEventState = new MutableLiveData<>();
        mEventState.setValue(Configuration.EventStageState.NONE);
        mErrorMessage = new MutableLiveData<>();
        mErrorMessage.setValue(Configuration.DEFAULT_ERROR_VALUE);
    }

    public void setContext(Context context) {
        mContext = context;
    }

    public void downloadHistoryList() {
        mLoadState.setValue(Configuration.LoadStageState.PROGRESS);
        new HistoryRepository(mContext).downloadHistoryData(new OnDownloadDataListListener<HistoryItem>() {

            @Override
            public void onDownloadSuccessful(List<HistoryItem> list) {
                updateList(list);
            }

            @Override
            public void onFailure(String errorMessage) {
                updateError(errorMessage);
            }
        });
    }

    private void updateList(List<HistoryItem> list) {
        Collections.sort(list);
        mList = new ArrayList<>(list);
        mLoadState.setValue(Configuration.LoadStageState.SUCCESS);
    }

    private void updateError(String errorMessage) {
        mErrorMessage.setValue(errorMessage);
        mLoadState.setValue(Configuration.LoadStageState.FAIL);
    }

    public void addListChangeListener() {
        new HistoryRepository(mContext).observeEvents(new OnDatabaseEventListener<HistoryItem>() {

            @Override
            public void onAdded(HistoryItem object) {
                notifyAdded(object);
            }

            @Override
            public void onModified(HistoryItem object) {}

            @Override
            public void onRemoved(HistoryItem object) {}

            @Override
            public void onFailure(String errorMessage) {
                notifyEventFailure(errorMessage);
            }
        });
    }

    private void notifyAdded(HistoryItem item) {
        mEventState.setValue(Configuration.EventStageState.PROGRESS);
        mAddedRequest = item;
        boolean doesNotExist = addItemToTop(item);
        if (doesNotExist) {
            mEventState.setValue(Configuration.EventStageState.ADDED);
        } else {
            mEventState.setValue(Configuration.EventStageState.NONE);
        }
    }

    private boolean addItemToTop(HistoryItem item) {
        if (mList != null) {
            for (HistoryItem i : mList) {
                if (i.getDate().equals(item.getDate()) && i.getUsername().equals(item.getUsername())) {
                    return false;
                }
            }
            mList.add(0, item);
        }
        return true;
    }

    private void notifyEventFailure(String errorMessage) {
        mErrorMessage.setValue(errorMessage);
        mEventState.setValue(Configuration.EventStageState.FAIL);
    }

    public List<HistoryItem> getList() {
        return mList;
    }

    public LiveData<Configuration.LoadStageState> getLoadState() {
        return mLoadState;
    }

    public LiveData<Configuration.EventStageState> getEventState() {
        return mEventState;
    }

    public HistoryItem getAddedRequest() {
        return mAddedRequest;
    }

    public LiveData<String> getErrorMessage() {
        return mErrorMessage;
    }
}
