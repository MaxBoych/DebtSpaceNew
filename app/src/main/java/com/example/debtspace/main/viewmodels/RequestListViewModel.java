package com.example.debtspace.main.viewmodels;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.debtspace.config.Configuration;
import com.example.debtspace.main.interfaces.OnDatabaseEventListener;
import com.example.debtspace.main.interfaces.OnDownloadDataListListener;
import com.example.debtspace.main.repositories.RequestListRepository;
import com.example.debtspace.models.Request;
import com.example.debtspace.models.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RequestListViewModel extends ViewModel {

    private List<Request> mList;
    private Request mAddedRequest;

    private Context mContext;

    private MutableLiveData<Configuration.LoadStageState> mLoadState;
    private MutableLiveData<Configuration.EventStageState> mEventState;
    private MutableLiveData<String> mErrorMessage;

    public RequestListViewModel() {
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

    public void downloadRequestList() {
        mLoadState.setValue(Configuration.LoadStageState.PROGRESS);
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
        mLoadState.setValue(Configuration.LoadStageState.SUCCESS);
    }

    private void updateError(String errorMessage) {
        mErrorMessage.setValue(errorMessage);
        mLoadState.setValue(Configuration.LoadStageState.FAIL);
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
            public void onRemoved(Request object) {}

            @Override
            public void onFailure(String errorMessage) {
                notifyEventFailure(errorMessage);
            }
        });
    }

    private void notifyAdded(Request request) {
        mEventState.setValue(Configuration.EventStageState.PROGRESS);
        mAddedRequest = request;
        boolean doesNotExist = addItemToTop(request);
        if (doesNotExist) {
            mEventState.setValue(Configuration.EventStageState.ADDED);
        } else {
            mEventState.setValue(Configuration.EventStageState.NONE);
        }
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

    private void notifyEventFailure(String errorMessage) {
        mErrorMessage.setValue(errorMessage);
        mEventState.setValue(Configuration.EventStageState.FAIL);
    }

    public List<Request> getList() {
        return mList;
    }

    public User getRequest(int position) {
        return mList.get(position);
    }

    public LiveData<Configuration.LoadStageState> getLoadState() {
        return mLoadState;
    }

    public LiveData<Configuration.EventStageState> getEventState() {
        return mEventState;
    }

    public Request getAddedRequest() {
        return mAddedRequest;
    }

    public LiveData<String> getErrorMessage() {
        return mErrorMessage;
    }
}
