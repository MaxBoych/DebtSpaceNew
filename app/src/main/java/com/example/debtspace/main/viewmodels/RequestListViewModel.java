package com.example.debtspace.main.viewmodels;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.debtspace.config.AppConfig;
import com.example.debtspace.main.interfaces.OnDatabaseEventListener;
import com.example.debtspace.main.interfaces.OnDownloadDataListListener;
import com.example.debtspace.main.repositories.RequestListRepository;
import com.example.debtspace.models.Request;
import com.example.debtspace.models.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

public class RequestListViewModel extends ViewModel {

    private List<Request> mList;
    private Request mChangedRequest;

    private Context mContext;

    private MutableLiveData<AppConfig.LoadStageState> mLoadState;
    private MutableLiveData<AppConfig.EventStageState> mEventState;
    private MutableLiveData<String> mErrorMessage;

    public RequestListViewModel() {
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

    public void downloadRequestList() {
        mLoadState.setValue(AppConfig.LoadStageState.PROGRESS);
        new RequestListRepository(mContext).downloadRequestList(new OnDownloadDataListListener<Request>() {
            @Override
            public void onDownloadSuccessful(List<Request> list) {
                updateList(list);
            }

            @Override
            public void onFailure(String errorMessage) {
                updateError(errorMessage);
            }
        });
    }

    private void updateList(List<Request> list) {
        Collections.sort(list);
        mList = new ArrayList<>(list);
        mLoadState.setValue(AppConfig.LoadStageState.SUCCESS);
    }

    private void updateError(String errorMessage) {
        mErrorMessage.setValue(errorMessage);
        mLoadState.setValue(AppConfig.LoadStageState.FAIL);
    }

    public void addListChangeListener() {
        new RequestListRepository(mContext).observeEvents(new OnDatabaseEventListener<Request>() {

            @Override
            public void onAdded(Request object) {
                notifyAdded(object);
            }

            @Override
            public void onModified(Request object) {}

            @Override
            public void onRemoved(Request object) {
                notifyRemoved(object);
            }

            @Override
            public void onFailure(String errorMessage) {
                notifyEventFailure(errorMessage);
            }
        });
    }

    private void notifyAdded(Request request) {
        mEventState.setValue(AppConfig.EventStageState.PROGRESS);
        mChangedRequest = request;
        boolean doesNotExist = addItemToTop(request);
        if (doesNotExist) {
            mEventState.setValue(AppConfig.EventStageState.ADDED);
        } else {
            mEventState.setValue(AppConfig.EventStageState.NONE);
        }
    }

    private void notifyRemoved(Request request) {
        mEventState.setValue(AppConfig.EventStageState.PROGRESS);
        mChangedRequest = request;
        mEventState.setValue(AppConfig.EventStageState.REMOVED);
    }

    private boolean addItemToTop(Request request) {
        if (mList != null) {
            for (Request r : mList) {
                if (r.getUsername().equals(request.getUsername())) {
                    return false;
                }
            }
            mList.add(0, request);
        }
        return true;
    }

    public int removeItem(String username) {
        if (mList != null) {
            ListIterator<Request> iterator = mList.listIterator();
            while (iterator.hasNext()) {
                int index = iterator.nextIndex();
                Request request = iterator.next();
                if (request.getUsername().equals(username)) {
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

    public List<Request> getList() {
        return mList;
    }

    public User getRequest(int position) {
        return mList.get(position);
    }

    public LiveData<AppConfig.LoadStageState> getLoadState() {
        return mLoadState;
    }

    public LiveData<AppConfig.EventStageState> getEventState() {
        return mEventState;
    }

    public Request getChangedRequest() {
        return mChangedRequest;
    }

    public LiveData<String> getErrorMessage() {
        return mErrorMessage;
    }
}
