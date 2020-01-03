package com.example.debtspace.main.viewmodels;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.debtspace.config.Configuration;
import com.example.debtspace.main.interfaces.OnDownloadDataListener;
import com.example.debtspace.models.Debt;
import com.example.debtspace.main.repositories.DebtListRepository;

import java.util.List;
import java.util.Objects;

public class DebtListViewModel extends ViewModel {

    private MutableLiveData<List<Debt>> mList;

    private MutableLiveData<Configuration.LoadStageState> mState;
    private MutableLiveData<String> mErrorMessage;

    public DebtListViewModel() {
        mList = new MutableLiveData<>();
        mState = new MutableLiveData<>();
        mErrorMessage = new MutableLiveData<>();
        mState.setValue(Configuration.LoadStageState.NONE);
        mErrorMessage.setValue(Configuration.DEFAULT_ERROR_VALUE);
    }

    public void uploadDebtList(Context context) {
        mState.setValue(Configuration.LoadStageState.PROGRESS);
        new DebtListRepository(context).uploadDebtListData(new OnDownloadDataListener<Debt>() {
            @Override
            public void onDownloadSuccessful(List<Debt> data) {
                setDebtList(data);
            }

            @Override
            public void onFailure(String errorMessage) {
                mErrorMessage.setValue(errorMessage);
                mState.setValue(Configuration.LoadStageState.FAIL);
            }
        });
    }

    private void setDebtList(List<Debt> debtList) {
        mList.setValue(debtList);
        mState.setValue(Configuration.LoadStageState.SUCCESS);
    }

    public Debt getDebtListItem(int position) {
        return Objects.requireNonNull(mList.getValue()).get(position);
    }

    public LiveData<List<Debt>> getDebtList() {
        return mList;
    }

    public LiveData<Configuration.LoadStageState> getDebtListState() {
        return mState;
    }

    public LiveData<String> getErrorMessage() {
        return mErrorMessage;
    }
}
