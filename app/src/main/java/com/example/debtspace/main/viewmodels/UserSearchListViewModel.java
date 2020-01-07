package com.example.debtspace.main.viewmodels;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.debtspace.config.Configuration;
import com.example.debtspace.main.interfaces.OnDownloadDataListListener;
import com.example.debtspace.main.repositories.UserSearchListRepository;
import com.example.debtspace.models.User;
import com.example.debtspace.utilities.StringUtilities;

import java.util.ArrayList;
import java.util.List;

public class UserSearchListViewModel extends ViewModel {

    private List<User> mList;
    private MutableLiveData<Configuration.LoadStageState> mState;
    private MutableLiveData<String> mErrorMessage;

    public UserSearchListViewModel() {
        mList = new ArrayList<>();
        mState = new MutableLiveData<>();
        mErrorMessage = new MutableLiveData<>();
        mState.setValue(Configuration.LoadStageState.NONE);
        mErrorMessage.setValue(Configuration.DEFAULT_ERROR_VALUE);
    }

    public void downloadUserSearchList(CharSequence s, Context context) {
        String string = s.toString().toLowerCase();
        if (!StringUtilities.isEmpty(string)) {
            mState.setValue(Configuration.LoadStageState.PROGRESS);

            new UserSearchListRepository(context).getUsersBySubstring(string, new OnDownloadDataListListener<User>() {
                @Override
                public void onDownloadSuccessful(List<User> list) {
                    updateList(list);
                }

                @Override
                public void onFailure(String errorMessage) {
                    mErrorMessage.setValue(errorMessage);
                    mState.setValue(Configuration.LoadStageState.FAIL);
                }
            });
        }
    }

    private void updateList(List<User> list) {
        mList = new ArrayList<>(list);
        mState.setValue(Configuration.LoadStageState.SUCCESS);
    }

    public User getUser(int position) {
        return mList.get(position);
    }

    public List<User> getList() {
        return mList;
    }

    public LiveData<Configuration.LoadStageState> getLoadState() {
        return mState;
    }

    public LiveData<String> getErrorMessage() {
        return mErrorMessage;
    }
}
