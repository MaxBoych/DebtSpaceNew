package com.example.debtspace.main.viewmodels;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.debtspace.config.AppConfig;
import com.example.debtspace.main.interfaces.OnDatabaseEventListener;
import com.example.debtspace.main.interfaces.OnDownloadDataListListener;
import com.example.debtspace.models.HistoryItem;
import com.example.debtspace.main.repositories.HistoryRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

public class HistoryViewModel extends ViewModel {

    private List<HistoryItem> mList;

    private Context mContext;
    private MutableLiveData<AppConfig.LoadStageState> mLoadState;
    private MutableLiveData<AppConfig.EventStageState> mEventState;
    private MutableLiveData<String> mErrorMessage;

    private HistoryItem mChangedRequest;

    public HistoryViewModel() {
        mList = new ArrayList<>();
        mLoadState = new MutableLiveData<>();
        mLoadState.setValue(AppConfig.LoadStageState.NONE);
        mEventState = new MutableLiveData<>();
        mEventState.setValue(AppConfig.EventStageState.NONE);
        mErrorMessage = new MutableLiveData<>();
        mErrorMessage.setValue(AppConfig.DEFAULT_ERROR_VALUE);
    }

    public void setContext(Context context) {
        mContext = context;
    }

    public void downloadHistoryList() {
        mLoadState.setValue(AppConfig.LoadStageState.PROGRESS);
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
        mLoadState.setValue(AppConfig.LoadStageState.SUCCESS);
    }

    private void updateError(String errorMessage) {
        mErrorMessage.setValue(errorMessage);
        mLoadState.setValue(AppConfig.LoadStageState.FAIL);
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
        mEventState.setValue(AppConfig.EventStageState.PROGRESS);
        mChangedRequest = item;
        boolean doesNotExist = addItemToTop(item);
        if (doesNotExist) {
            mEventState.setValue(AppConfig.EventStageState.ADDED);
        } else {
            mEventState.setValue(AppConfig.EventStageState.NONE);
        }
    }

    private boolean addItemToTop(HistoryItem item) {
        if (mList != null) {
            for (HistoryItem i : mList) {
                if (i.getId().equals(item.getId())) {
                    return false;
                }
            }
            mList.add(0, item);
        }
        return true;
    }

    public int removeItem(HistoryItem item) {
        if (mList != null) {
            ListIterator<HistoryItem> iterator = mList.listIterator();
            while (iterator.hasNext()) {
                int index = iterator.nextIndex();
                HistoryItem i = iterator.next();
                if (i.getId().equals(item.getId())) {
                    iterator.remove();
                    return index;
                }
            }
        }
        return -1;
    }

    private void notifyEventFailure(String errorMessage) {
        mErrorMessage.setValue(errorMessage);
        mEventState.setValue(AppConfig.EventStageState.FAIL);
    }

    public void clearViewModel() {
        mList.clear();
    }

    public HistoryItem getHistoryItem(int position) {
        return mList.get(position);
    }

    public List<HistoryItem> getList() {
        return mList;
    }

    public LiveData<AppConfig.LoadStageState> getLoadState() {
        return mLoadState;
    }

    public LiveData<AppConfig.EventStageState> getEventState() {
        return mEventState;
    }

    public HistoryItem getChangedRequest() {
        return mChangedRequest;
    }

    public LiveData<String> getErrorMessage() {
        return mErrorMessage;
    }
}
