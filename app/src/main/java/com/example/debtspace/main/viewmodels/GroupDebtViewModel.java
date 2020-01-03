package com.example.debtspace.main.viewmodels;

import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.debtspace.config.Configuration;
import com.example.debtspace.main.interfaces.OnDownloadDataListener;
import com.example.debtspace.main.interfaces.OnUpdateDataListener;
import com.example.debtspace.main.repositories.GroupDebtRepository;
import com.example.debtspace.models.User;
import com.example.debtspace.utilities.StringUtilities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class GroupDebtViewModel extends ViewModel {

    private Uri mUriImage;
    private MutableLiveData<List<User>> mFoundList;
    private List<User> mFriendList;
    private MutableLiveData<List<User>> mAddedList;

    private MutableLiveData<Configuration.LoadStageState> mState;
    private Boolean mIsSubmit;

    private MutableLiveData<String> mErrorMessage;

    public GroupDebtViewModel() {
        mFoundList = new MutableLiveData<>();
        mAddedList = new MutableLiveData<>();
        mState = new MutableLiveData<>();
        mErrorMessage = new MutableLiveData<>();

        mFoundList.setValue(new ArrayList<>());
        mAddedList.setValue(new ArrayList<>());
        mFriendList = new ArrayList<>();
        mState.setValue(Configuration.LoadStageState.NONE);
        mIsSubmit = false;
        mErrorMessage.setValue(Configuration.DEFAULT_ERROR_VALUE);
    }

    public void addToGroup(int position) {
        List<User> list = new ArrayList<>(Objects.requireNonNull(mFoundList.getValue()));
        User friend = list.get(position);
        mFriendList.remove(friend);
        list.remove(position);
        setFoundList(list);

        list = mAddedList.getValue();
        Objects.requireNonNull(list).add(friend);
        setAddedList(list);
    }

    public void removeFromGroup(int position) {
        List<User> list = new ArrayList<>(Objects.requireNonNull(mAddedList.getValue()));
        User friend = list.get(position);
        list.remove(position);
        setAddedList(list);

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
        mState.setValue(Configuration.LoadStageState.PROGRESS);

        String string = s.toString().toLowerCase();
        if (StringUtilities.isEmpty(string)) {
            setFoundList(mFriendList);
        } else {
            setFoundList(searchFriends(string));
        }
    }

    private void setFoundList(List<User> list) {
        List<User> added = mAddedList.getValue();
        if (!Objects.requireNonNull(added).isEmpty()) {

            Iterator<User> iterator = list.iterator();
            while (iterator.hasNext()) {
                User user = iterator.next();
                for (User addUser : added) {
                    if (user.getUsername().equals(addUser.getUsername())) {
                        iterator.remove();
                    }
                }
            }
        }

        mFoundList.setValue(list);
        mState.setValue(Configuration.LoadStageState.SUCCESS);
    }

    public void downloadFriendList(Context context) {
        mState.setValue(Configuration.LoadStageState.PROGRESS);
        new GroupDebtRepository(context).downloadFoundListData(new OnDownloadDataListener<User>() {
            @Override
            public void onDownloadSuccessful(List<User> data) {
                setFriendList(data);
            }

            @Override
            public void onFailure(String errorMessage) {
                mErrorMessage.setValue(errorMessage);
                mState.setValue(Configuration.LoadStageState.FAIL);
            }
        });
    }

    public void downloadAddedList(ArrayList<String> usernames, String groupID, Context context) {
        mState.setValue(Configuration.LoadStageState.PROGRESS);
        new GroupDebtRepository(context).downloadListItems(usernames, new OnDownloadDataListener<User>() {
            @Override
            public void onDownloadSuccessful(List<User> list) {
                setAddedList(list);
                downloadImage(groupID, context);
            }

            @Override
            public void onFailure(String errorMessage) {
                mErrorMessage.setValue(errorMessage);
                mState.setValue(Configuration.LoadStageState.FAIL);
            }
        });
    }

    private void setFriendList(List<User> list) {
        mFriendList.addAll(list);
        setFoundList(list);
    }

    private void setAddedList(List<User> list) {
        mAddedList.setValue(list);
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

    public void createGroup(String groupName, String debt, Uri uri,Context context) {
        mState.setValue(Configuration.LoadStageState.PROGRESS);
        List<User> users = mAddedList.getValue();
        if (Objects.requireNonNull(users).size() >= Configuration.MINIMUM_GROUP_MEMBERS) {
            List<String> members = new ArrayList<>();
            for (User user : users) {
                members.add(user.getUsername());
            }
            new GroupDebtRepository(context).insertGroupToDatabase(groupName, debt, members, uri, new OnUpdateDataListener() {
                @Override
                public void onUpdateSuccessful() {
                    mIsSubmit = true;
                    mState.setValue(Configuration.LoadStageState.SUCCESS);
                }

                @Override
                public void onFailure(String errorMessage) {
                    mErrorMessage.setValue(errorMessage);
                    mState.setValue(Configuration.LoadStageState.FAIL);
                }
            });
        } else {
            mErrorMessage.setValue("At least 3 members of the group are required (including you)");
            mState.setValue(Configuration.LoadStageState.FAIL);
        }
    }

    public void updateGroup(String groupID, String groupName, String debt, Context context) {
        mState.setValue(Configuration.LoadStageState.PROGRESS);
        List<User> users = mAddedList.getValue();
        if (Objects.requireNonNull(users).size() >= Configuration.MINIMUM_GROUP_MEMBERS) {
            List<String> members = new ArrayList<>();
            for (User user : users) {
                members.add(user.getUsername());
            }
            new GroupDebtRepository(context).updateGroupInDatabase(groupID, groupName, debt, members, new OnUpdateDataListener() {
                @Override
                public void onUpdateSuccessful() {
                    mIsSubmit = true;
                    mState.setValue(Configuration.LoadStageState.SUCCESS);
                }

                @Override
                public void onFailure(String errorMessage) {
                    mErrorMessage.setValue(errorMessage);
                    mState.setValue(Configuration.LoadStageState.FAIL);
                }
            });
        } else {
            mErrorMessage.setValue("At least 3 members of the group are required (including you)");
            mState.setValue(Configuration.LoadStageState.FAIL);
        }
    }

    private void downloadImage(String groupID, Context context) {
        new GroupDebtRepository(context).downloadGroupImage(groupID, new OnDownloadDataListener<Uri>() {
            @Override
            public void onDownloadSuccessful(List<Uri> list) {
                //mState.setValue(Configuration.LoadStageState.SUCCESS);
                mUriImage = list.get(0);
                downloadFriendList(context);
            }

            @Override
            public void onFailure(String errorMessage) {
                mErrorMessage.setValue(errorMessage);
                mState.setValue(Configuration.LoadStageState.FAIL);
            }
        });
    }

    public LiveData<List<User>> getFoundList() {
        return mFoundList;
    }

    public LiveData<List<User>> getAddedList() {
        return mAddedList;
    }

    public LiveData<Configuration.LoadStageState> getGroupDebtState() {
        return mState;
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
