package com.example.debtspace.main.viewmodels;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.debtspace.config.AppConfig;
import com.example.debtspace.main.interfaces.OnDatabaseEventListener;
import com.example.debtspace.main.interfaces.OnDownloadDataListListener;
import com.example.debtspace.main.repositories.DebtListRepository;
import com.example.debtspace.models.Debt;
import com.example.debtspace.models.GroupDebt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

public class DebtListViewModel extends ViewModel {

    private List<Debt> mList;
    //private DebtBond mChangedDebt;
    private Debt mChangedDebt;

    private MutableLiveData<AppConfig.LoadStageState> mLoadState;
    private MutableLiveData<AppConfig.EventStageState> mEventState;
    private MutableLiveData<String> mErrorMessage;

    private Context mContext;

    public DebtListViewModel() {
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

    public void downloadDebtList() {
        mLoadState.setValue(AppConfig.LoadStageState.PROGRESS);
        new DebtListRepository(mContext).downloadDebtListData(new OnDownloadDataListListener<Debt>() {
            @Override
            public void onDownloadSuccessful(List<Debt> data) {
                updateList(data);
            }

            @Override
            public void onFailure(String errorMessage) {
                updateError(errorMessage);
            }
        });
    }

    private void updateList(List<Debt> debtList) {
        Collections.sort(debtList);
        mList = new ArrayList<>(debtList);
        mLoadState.setValue(AppConfig.LoadStageState.SUCCESS);
    }

    private void updateError(String errorMessage) {
        mErrorMessage.setValue(errorMessage);
        mLoadState.setValue(AppConfig.LoadStageState.FAIL);
    }

    public void observeDebtEvents() {
        new DebtListRepository(mContext).observeDebtEvents(new OnDatabaseEventListener<Debt>() {

            @Override
            public void onAdded(Debt object) {
                notifyAdded(object);
            }

            @Override
            public void onModified(Debt object) {
                notifyModified(object);
            }

            @Override
            public void onRemoved(Debt object) {
                notifyRemoved(object);
            }

            @Override
            public void onFailure(String errorMessage) {
                notifyEventFailure(errorMessage);
            }
        });
    }

    private void notifyAdded(Debt debt) {
        mEventState.setValue(AppConfig.EventStageState.PROGRESS);
        mChangedDebt = debt;
        boolean doesNotExist = addItemToTop(debt);
        if (doesNotExist) {
            mEventState.setValue(AppConfig.EventStageState.ADDED);
        } else {
            mEventState.setValue(AppConfig.EventStageState.NONE);
        }
    }

    private void notifyModified(Debt debt) {
        mEventState.setValue(AppConfig.EventStageState.PROGRESS);
        mChangedDebt = debt;
        mEventState.setValue(AppConfig.EventStageState.MODIFIED);
    }

    private void notifyRemoved(Debt debt) {
        mEventState.setValue(AppConfig.EventStageState.PROGRESS);
        mChangedDebt = debt;
        mEventState.setValue(AppConfig.EventStageState.REMOVED);
    }

    private void notifyEventFailure(String errorMessage) {
        mErrorMessage.setValue(errorMessage);
        mEventState.setValue(AppConfig.EventStageState.FAIL);
    }

    /*private void transformToDebt(DebtBond debtBond) {
        new DebtListRepository(mContext).transformToDebt(debtBond, new OnDownloadDataListener<Debt>() {
            @Override
            public void onDownloadSuccessful(Debt object) {
                notifyAdded(object);
            }

            @Override
            public void onFailure(String errorMessage) {
                notifyEventFailure(errorMessage);
            }
        });
    }*/

    /*private void notifyAdded(Debt debt) {
        mChangedDebt = debt;
        boolean doesNotExist = addItemToTop(debt);
        if (doesNotExist) {
            mEventState.setValue(AppConfig.EventStageState.ADDED);
        } else {
            mEventState.setValue(AppConfig.EventStageState.NONE);
        }
    }*/

    private boolean addItemToTop(Debt debt) {
        if (mList != null) {
            if (debt instanceof GroupDebt) {
                for (Debt d : mList) {
                    if (d instanceof GroupDebt) {
                        if (((GroupDebt) d).getId().equals(((GroupDebt) debt).getId())) {
                            return false;
                        }
                    }
                }
            } else {
                for (Debt d : mList) {
                    if (!(d instanceof GroupDebt)) {
                        if (d.getUser().getUsername().equals(debt.getUser().getUsername())) {
                            return false;
                        }
                    }
                }
            }

            mList.add(0, debt);
            return true;
        }

        return false;
    }

    public int modifyItem(Debt debt) {
        if (mList != null) {
            ListIterator<Debt> iterator = mList.listIterator();
            if (debt instanceof GroupDebt) {
                while (iterator.hasNext()) {
                    int index = iterator.nextIndex();
                    Debt d = iterator.next();
                    if (d instanceof GroupDebt) {
                        if (((GroupDebt) d).getId().equals(((GroupDebt) debt).getId())) {
                            ((GroupDebt) d).setName(((GroupDebt) debt).getName());
                            d.setDebt(debt.getDebt());
                            ((GroupDebt) d).setMembers(((GroupDebt) debt).getMembers());
                            d.setDate(debt.getDate());
                            iterator.remove();
                            mList.add(0, d);
                            return index;
                        }
                    }
                }
            } else {
                while (iterator.hasNext()) {
                    int index = iterator.nextIndex();
                    Debt d = iterator.next();
                    if (!(d instanceof GroupDebt)) {
                        if (d.getUser().getUsername().equals(debt.getUser().getUsername())) {
                            d.setDebt(debt.getDebt());
                            d.setDate(debt.getDate());
                            iterator.remove();
                            mList.add(0, d);
                            return index;
                        }
                    }
                }
            }
        }
        return -1;
    }

    public int removeItem(Debt debt) {
        if (mList != null) {
            ListIterator<Debt> iterator = mList.listIterator();
            if (debt instanceof GroupDebt) {
                while (iterator.hasNext()) {
                    int index = iterator.nextIndex();
                    Debt d = iterator.next();
                    if (d instanceof GroupDebt) {
                        if (((GroupDebt) d).getId().equals(((GroupDebt) debt).getId())) {
                            iterator.remove();
                            return index;
                        }
                    }
                }
            } else {
                while (iterator.hasNext()) {
                    int index = iterator.nextIndex();
                    Debt d = iterator.next();
                    if (!(d instanceof GroupDebt)) {
                        if (d.getUser().getUsername().equals(debt.getUser().getUsername())) {
                            iterator.remove();
                            return index;
                        }
                    }
                }
            }
        }
        return -1;
    }

    public Debt getDebtListItem(int position) {
        return mList.get(position);
    }

    public List<Debt> getDebtList() {
        return mList;
    }

    public LiveData<AppConfig.LoadStageState> getLoadState() {
        return mLoadState;
    }

    public LiveData<AppConfig.EventStageState> getEventState() {
        return mEventState;
    }

    public Debt getChangedDebt() {
        return mChangedDebt;
    }

    public LiveData<String> getErrorMessage() {
        return mErrorMessage;
    }
}
