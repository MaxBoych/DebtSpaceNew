package com.example.debtspace.main.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.debtspace.config.Configuration;
import com.example.debtspace.main.interfaces.OnDownloadDataListener;
import com.example.debtspace.main.repositories.RequestListRepository;
import com.example.debtspace.models.User;

import java.util.List;
import java.util.Objects;

public class RequestListViewModel extends ViewModel {

    private MutableLiveData<List<User>> mList;

    private MutableLiveData<Configuration.LoadStageState> mState;
    private MutableLiveData<String> mErrorMessage;

    public RequestListViewModel() {
        mList = new MutableLiveData<>();
        mState = new MutableLiveData<>();
        mErrorMessage = new MutableLiveData<>();
        mState.setValue(Configuration.LoadStageState.NONE);
        mErrorMessage.setValue(Configuration.DEFAULT_ERROR_VALUE);

        downloadRequestList();
    }

    public void downloadRequestList() {
        mState.setValue(Configuration.LoadStageState.PROGRESS);
        new RequestListRepository().getRequests(new OnDownloadDataListener<User>() {
            @Override
            public void onDownloadSuccessful(List<User> list) {
                setRequestList(list);
                mState.setValue(Configuration.LoadStageState.SUCCESS);
            }

            @Override
            public void onFailure(String errorMessage) {
                mErrorMessage.setValue(errorMessage);
                mState.setValue(Configuration.LoadStageState.FAIL);
            }
        });
    }

    private void setRequestList(List<User> list) {
        mList.setValue(list);
    }

    public LiveData<List<User>> getList() {
        return mList;
    }

    public User getRequest(int position) {
        return Objects.requireNonNull(mList.getValue()).get(position);
    }

    public LiveData<Configuration.LoadStageState> getListState() {
        return mState;
    }

    public LiveData<String> getErrorMessage() {
        return mErrorMessage;
    }
}
