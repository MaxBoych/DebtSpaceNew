package com.example.debtspace.main.viewmodels;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.debtspace.config.Configuration;
import com.example.debtspace.main.interfaces.OnGetFirestoreDataListener;
import com.example.debtspace.main.repositories.UserSearchListRepository;
import com.example.debtspace.models.User;
import com.example.debtspace.utilities.StringUtilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class UserSearchListViewModel extends ViewModel {

    private MutableLiveData<List<User>> mList;
    private MutableLiveData<Configuration.LoadStageState> mState;
    private MutableLiveData<String> mErrorMessage;

    public UserSearchListViewModel() {
        mList = new MutableLiveData<>();
        mState = new MutableLiveData<>();
        mErrorMessage = new MutableLiveData<>();
        mState.setValue(Configuration.LoadStageState.NONE);
        mErrorMessage.setValue(Configuration.DEFAULT_ERROR_VALUE);
    }

    public void downloadUserSearchList(CharSequence s, Context context) {
        String string = s.toString().toLowerCase();
        if (!StringUtilities.isEmpty(string)) {
            mState.setValue(Configuration.LoadStageState.PROGRESS);

            new UserSearchListRepository(context).getUsersBySubstring(string, new OnGetFirestoreDataListener() {
                @Override
                public void onGetSuccessful(List<Map<String, Map<String, Object>>> data) {
                    List<Map<String, String>> list = new ArrayList<>();
                    for (Map<String, Map<String, Object>> mapObject : data) {
                        for (Map.Entry<String, Map<String, Object>> map : mapObject.entrySet()) {
                            Map<String, String> m = new HashMap<>();
                            for (Map.Entry<String, Object> entry : map.getValue().entrySet()) {
                                if (!entry.getKey().equals(Configuration.GROUPS_FIELD_NAME)) {
                                    m.put(entry.getKey(), (String) entry.getValue());
                                }
                            }
                            list.add(m);
                        }
                    }
                    setUserSearchList(list);
                    mState.setValue(Configuration.LoadStageState.SUCCESS);
                }

                @Override
                public void onFailure(String errorMessage) {
                    mErrorMessage.setValue(errorMessage);
                    mState.setValue(Configuration.LoadStageState.FAIL);
                }
            });
        }
    }

    private void setUserSearchList(List<Map<String, String>> users) {
        List<User> list = new ArrayList<>();
        for (Map<String, String> user : users) {
            list.add(new User(user));
        }
        mList.setValue(list);
    }

    public User getUser(int position) {
        return Objects.requireNonNull(mList.getValue()).get(position);
    }

    public LiveData<List<User>> getUserSearchList() {
        return mList;
    }

    public LiveData<Configuration.LoadStageState> getUserSearchState() {
        return mState;
    }

    public LiveData<String> getErrorMessage() {
        return mErrorMessage;
    }
}
