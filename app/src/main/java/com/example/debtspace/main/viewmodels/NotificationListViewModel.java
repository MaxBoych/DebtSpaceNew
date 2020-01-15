package com.example.debtspace.main.viewmodels;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.debtspace.config.AppConfig;
import com.example.debtspace.main.interfaces.OnDatabaseEventListener;
import com.example.debtspace.main.interfaces.OnDownloadDataListListener;
import com.example.debtspace.main.interfaces.OnUpdateDataListener;
import com.example.debtspace.main.repositories.HistoryRemovalRepository;
import com.example.debtspace.main.repositories.NotificationListRepository;
import com.example.debtspace.models.FriendRequest;
import com.example.debtspace.models.Notification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

public class NotificationListViewModel extends ViewModel {

    private List<Notification> mList;
    private Notification mChangedNotification;

    private Context mContext;

    private MutableLiveData<AppConfig.LoadStageState> mLoadState;
    private MutableLiveData<AppConfig.EventStageState> mEventState;
    private MutableLiveData<String> mErrorMessage;

    public NotificationListViewModel() {
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
        new NotificationListRepository(mContext).downloadRequestList(new OnDownloadDataListListener<Notification>() {
            @Override
            public void onDownloadSuccessful(List<Notification> list) {
                updateList(list);
                setLoadState(AppConfig.LoadStageState.SUCCESS);
            }

            @Override
            public void onFailure(String errorMessage) {
                updateError(errorMessage);
                setLoadState(AppConfig.LoadStageState.FAIL);
            }
        });
    }

    /*public void removeHistoryItem(String username) {
        setLoadState(AppConfig.LoadStageState.PROGRESS);
        new HistoryRemovalRepository(mContext).removeHistoryItem(username, new OnUpdateDataListener() {
            @Override
            public void onUpdateSuccessful() {
                setLoadState(AppConfig.LoadStageState.SUCCESS);
            }

            @Override
            public void onFailure(String errorMessage) {
                updateError(errorMessage);
                setLoadState(AppConfig.LoadStageState.FAIL);
            }
        });
    }*/

    private void setLoadState(AppConfig.LoadStageState state) {
        mLoadState.setValue(state);
    }

    private void updateList(List<Notification> list) {
        Collections.sort(list);
        mList = new ArrayList<>(list);
    }

    private void updateError(String errorMessage) {
        mErrorMessage.setValue(errorMessage);
    }

    public void observeNotificationEvents() {
        new NotificationListRepository(mContext).observeNotificationEvents(new OnDatabaseEventListener<Notification>() {

            @Override
            public void onAdded(Notification object) {
                setEventState(AppConfig.EventStageState.PROGRESS);
                setNotification(object);
                boolean doesNotExist = addItemToTop(object);
                if (doesNotExist) {
                    setEventState(AppConfig.EventStageState.ADDED);
                } else {
                    setEventState(AppConfig.EventStageState.NONE);
                }
            }

            @Override
            public void onModified(Notification object) {}

            @Override
            public void onRemoved(Notification object) {
                setEventState(AppConfig.EventStageState.PROGRESS);
                setNotification(object);
                setEventState(AppConfig.EventStageState.REMOVED);
            }

            @Override
            public void onFailure(String errorMessage) {
                notifyEventFailure(errorMessage);
            }
        });
    }

    private void setEventState(AppConfig.EventStageState state) {
        mEventState.setValue(state);
    }

    private void setNotification(Notification notification) {
        mChangedNotification = notification;
    }

    private boolean addItemToTop(Notification notification) {
        if (mList != null) {
            for (Notification n : mList) {
                if (n.getId().equals(notification.getId())) {
                    return false;
                }
            }
            mList.add(0, notification);
        }
        return true;
    }

    public int removeItem(Notification notification) {
        if (mList != null) {
            ListIterator<Notification> iterator = mList.listIterator();
            while (iterator.hasNext()) {
                int index = iterator.nextIndex();
                Notification n = iterator.next();
                if (n.getId().equals(notification.getId())) {
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

    public List<Notification> getList() {
        return mList;
    }

    public Notification getNotification(int position) {
        return mList.get(position);
    }

    public LiveData<AppConfig.LoadStageState> getLoadState() {
        return mLoadState;
    }

    public LiveData<AppConfig.EventStageState> getEventState() {
        return mEventState;
    }

    public Notification getChangedRequest() {
        return mChangedNotification;
    }

    public LiveData<String> getErrorMessage() {
        return mErrorMessage;
    }
}
