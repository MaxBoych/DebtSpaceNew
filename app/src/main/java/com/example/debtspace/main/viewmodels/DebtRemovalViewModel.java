package com.example.debtspace.main.viewmodels;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.debtspace.config.AppConfig;
import com.example.debtspace.main.interfaces.OnUpdateDataListener;
import com.example.debtspace.main.repositories.DebtRemovalRepository;
import com.example.debtspace.models.DebtRequest;

public class DebtRemovalViewModel extends ViewModel {

    private MutableLiveData<AppConfig.LoadStageState> mLoadState;
    private MutableLiveData<String> mErrorMessage;

    private Context mContext;

    public DebtRemovalViewModel() {
        mLoadState = new MutableLiveData<>();
        mLoadState.setValue(AppConfig.LoadStageState.NONE);
        mErrorMessage = new MutableLiveData<>();
        mErrorMessage.setValue(AppConfig.DEFAULT_ERROR_VALUE);
    }

    public void setContext(Context context) {
        mContext = context;
    }

    public void sendDebtRequest(DebtRequest request) {
        setLoadState(AppConfig.LoadStageState.PROGRESS);
        new DebtRemovalRepository(mContext).sendDebtRequest(request, mContext, new OnUpdateDataListener() {
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
    }

    private void setLoadState(AppConfig.LoadStageState state) {
        mLoadState.setValue(state);
    }

    private void updateError(String errorMessage) {
        mErrorMessage.setValue(errorMessage);
    }

    public MutableLiveData<AppConfig.LoadStageState> getLoadState() {
        return mLoadState;
    }

    public LiveData<String> getErrorMessage() {
        return mErrorMessage;
    }
}
