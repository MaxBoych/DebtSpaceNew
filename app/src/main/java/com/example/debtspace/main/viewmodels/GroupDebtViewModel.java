package com.example.debtspace.main.viewmodels;

import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.debtspace.config.AppConfig;
import com.example.debtspace.config.ErrorsConfig;
import com.example.debtspace.main.interfaces.OnDownloadDataListListener;
import com.example.debtspace.main.interfaces.OnDownloadDataListener;
import com.example.debtspace.main.interfaces.OnUpdateDataListener;
import com.example.debtspace.main.repositories.GroupDebtRepository;
import com.example.debtspace.models.User;
import com.example.debtspace.utilities.StringUtilities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GroupDebtViewModel extends ViewModel {

    private Uri mUriImage;
    private List<User> mFoundList;
    private List<User> mFriendList;
    private List<User> mAddedList;

    private Context mContext;

    private MutableLiveData<AppConfig.LoadStageState> mLoadState;
    private MutableLiveData<String> mErrorMessage;
    private Boolean mIsSubmit;

    public GroupDebtViewModel() {
        mFoundList = new ArrayList<>();
        mAddedList = new ArrayList<>();
        mFriendList = new ArrayList<>();
        mLoadState = new MutableLiveData<>();
        mLoadState.setValue(AppConfig.LoadStageState.NONE);
        mErrorMessage = new MutableLiveData<>();
        mErrorMessage.setValue(AppConfig.DEFAULT_ERROR_VALUE);
        mIsSubmit = false;
    }

    public void setContext(Context context) {
        mContext = context;
    }

    public void addToGroup(int position) {
        List<User> list = new ArrayList<>(mFoundList);
        User friend = list.get(position);
        mFriendList.remove(friend);
        list.remove(position);
        setFoundList(list);

        list = new ArrayList<>(mAddedList);
        list.add(friend);
        updateAddedList(list);
    }

    public void removeFromGroup(int position) {
        List<User> list = new ArrayList<>(mAddedList);
        User friend = list.get(position);
        list.remove(position);
        updateAddedList(list);

        Iterator<User> iterator = mFriendList.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getUsername().equals(friend.getUsername())) {
                iterator.remove();
                break;
            }
        }
        mFriendList.add(friend);
        setFoundList(mFriendList);
    }

    public void textChangeListen(CharSequence s) {
        mLoadState.setValue(AppConfig.LoadStageState.PROGRESS);

        String string = s.toString().toLowerCase();
        if (StringUtilities.isEmpty(string)) {
            setFoundList(mFriendList);
        } else {
            setFoundList(searchFriends(string));
        }
    }

    private void setFoundList(List<User> list) {
        if (!mAddedList.isEmpty()) {

            Iterator<User> iterator = list.iterator();
            while (iterator.hasNext()) {
                User user = iterator.next();
                for (User addUser : mAddedList) {
                    if (user.getUsername().equals(addUser.getUsername())) {
                        iterator.remove();
                    }
                }
            }
        }

        mFoundList = new ArrayList<>(list);
        mLoadState.setValue(AppConfig.LoadStageState.SUCCESS);
    }

    public void downloadFriendList() {
        mLoadState.setValue(AppConfig.LoadStageState.PROGRESS);
        new GroupDebtRepository(mContext).downloadFoundListData(new OnDownloadDataListListener<User>() {
            @Override
            public void onDownloadSuccessful(List<User> data) {
                setFriendList(data);
            }

            @Override
            public void onFailure(String errorMessage) {
                updateError(errorMessage);
            }
        });
    }

    public void downloadAddedList(ArrayList<String> usernames, String groupID) {
        mLoadState.setValue(AppConfig.LoadStageState.PROGRESS);
        new GroupDebtRepository(mContext).downloadListItems(usernames, new OnDownloadDataListListener<User>() {
            @Override
            public void onDownloadSuccessful(List<User> list) {
                updateAddedList(list);
                downloadImage(groupID);
            }

            @Override
            public void onFailure(String errorMessage) {
                updateError(errorMessage);
            }
        });
    }

    private void updateError(String errorMessage) {
        mErrorMessage.setValue(errorMessage);
        mLoadState.setValue(AppConfig.LoadStageState.FAIL);
    }

    private void setFriendList(List<User> list) {
        mFriendList = new ArrayList<>(list);
        setFoundList(list);
    }

    private void updateAddedList(List<User> list) {
        mAddedList = new ArrayList<>(list);
    }

    private List<User> searchFriends(String string) {
        List<User> found = new ArrayList<>();
        for (User user : mFriendList) {
            if (user.getUsername().startsWith(string)) {
                found.add(user);
            }
        }

        return found;
    }

    public void createGroup(String groupName, String debt, Uri uri) {
        mLoadState.setValue(AppConfig.LoadStageState.PROGRESS);
        List<User> users = new ArrayList<>(mAddedList);
        if (users.size() >= AppConfig.MINIMUM_GROUP_MEMBERS) {
            List<String> members = new ArrayList<>();
            for (User user : users) {
                members.add(user.getUsername());
            }
            new GroupDebtRepository(mContext).uploadGroup(groupName, debt,
                    members, uri, new OnUpdateDataListener() {

                @Override
                public void onUpdateSuccessful() {
                    updateSubmitToTrue();
                }

                @Override
                public void onFailure(String errorMessage) {
                    updateError(errorMessage);
                }
            });
        } else {
            mErrorMessage.setValue(ErrorsConfig.ERROR_MINIMUM_MEMBERS);
            mLoadState.setValue(AppConfig.LoadStageState.FAIL);
        }
    }

    private void updateSubmitToTrue() {
        mIsSubmit = true;
        mLoadState.setValue(AppConfig.LoadStageState.SUCCESS);
    }

    public void updateGroup(String groupID, String groupName, String debt) {
        mLoadState.setValue(AppConfig.LoadStageState.PROGRESS);
        List<User> users = new ArrayList<>(mAddedList);
        if (users.size() >= AppConfig.MINIMUM_GROUP_MEMBERS) {
            List<String> members = new ArrayList<>();
            for (User user : users) {
                members.add(user.getUsername());
            }
            new GroupDebtRepository(mContext).updateGroup(groupID, groupName,
                    debt, members, new OnUpdateDataListener() {

                @Override
                public void onUpdateSuccessful() {
                    updateSubmitToTrue();
                }

                @Override
                public void onFailure(String errorMessage) {
                    updateError(errorMessage);
                }
            });
        } else {
            mErrorMessage.setValue(ErrorsConfig.ERROR_MINIMUM_MEMBERS);
            mLoadState.setValue(AppConfig.LoadStageState.FAIL);
        }
    }

    private void downloadImage(String groupID) {
        new GroupDebtRepository(mContext).downloadGroupImage(groupID, new OnDownloadDataListener<Uri>() {
            @Override
            public void onDownloadSuccessful(Uri uri) {
                mUriImage = uri;
                downloadFriendList();
            }

            @Override
            public void onFailure(String errorMessage) {
                updateError(errorMessage);
            }
        });
    }

    public List<User> getFoundList() {
        return mFoundList;
    }

    public List<User> getAddedList() {
        return mAddedList;
    }

    public LiveData<AppConfig.LoadStageState> getLoadState() {
        return mLoadState;
    }

    public Uri getUriImage() {
        return mUriImage;
    }

    public Boolean getIsCreate() {
        return mIsSubmit;
    }

    public LiveData<String> getErrorMessage() {
        return mErrorMessage;
    }
}
