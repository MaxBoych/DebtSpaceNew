package com.example.debtspace.main.viewmodels;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.debtspace.config.AppConfig;
import com.example.debtspace.main.interfaces.OnUpdateDataListener;
import com.example.debtspace.main.repositories.DebtRemovalConfirmRepository;
import com.example.debtspace.models.DebtRequest;

public class DebtRemovalConfirmViewModel extends ViewModel {

    private MutableLiveData<AppConfig.LoadStageState> mLoadState;
    private MutableLiveData<String> mErrorMessage;

    private Context mContext;

    public DebtRemovalConfirmViewModel() {
        mLoadState = new MutableLiveData<>();
        mLoadState.setValue(AppConfig.LoadStageState.NONE);
        mErrorMessage = new MutableLiveData<>();
        mErrorMessage.setValue(AppConfig.DEFAULT_ERROR_VALUE);
    }

    public void setContext(Context context) {
        mContext = context;
    }

    public void acceptDebtRemovalRequest(DebtRequest request) {
        mLoadState.setValue(AppConfig.LoadStageState.PROGRESS);
        new DebtRemovalConfirmRepository(mContext).accept(request, new OnUpdateDataListener() {
            @Override
            public void onUpdateSuccessful() {
                mLoadState.setValue(AppConfig.LoadStageState.SUCCESS);
            }

            @Override
            public void onFailure(String errorMessage) {
                mErrorMessage.setValue(errorMessage);
                mLoadState.setValue(AppConfig.LoadStageState.FAIL);
            }
        });
    }

    public void rejectDebtRemovalRequest(String id) {
        mLoadState.setValue(AppConfig.LoadStageState.PROGRESS);
        new DebtRemovalConfirmRepository(mContext).reject(id, new OnUpdateDataListener() {
            @Override
            public void onUpdateSuccessful() {
                mLoadState.setValue(AppConfig.LoadStageState.SUCCESS);
            }

            @Override
            public void onFailure(String errorMessage) {
                mErrorMessage.setValue(errorMessage);
                mLoadState.setValue(AppConfig.LoadStageState.FAIL);
            }
        });
    }

    public MutableLiveData<AppConfig.LoadStageState> getLoadState() {
        return mLoadState;
    }

    public LiveData<String> getErrorMessage() {
        return mErrorMessage;
    }
}
