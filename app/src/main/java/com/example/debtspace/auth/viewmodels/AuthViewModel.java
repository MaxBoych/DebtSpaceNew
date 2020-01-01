package com.example.debtspace.auth.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.debtspace.auth.interfaces.OnAuthProgressListener;
import com.example.debtspace.main.interfaces.OnFindUserListener;
import com.example.debtspace.auth.repositories.AuthRepository;
import com.example.debtspace.config.Configuration;
import com.example.debtspace.models.User;
import com.example.debtspace.utilities.StringUtilities;
import com.example.debtspace.utilities.FirebaseUtilities;

public class AuthViewModel extends ViewModel {

    private MutableLiveData<Configuration.AuthStageState> mSignInState;
    private MutableLiveData<Configuration.AuthStageState> mSignUpState;
    private MutableLiveData<String> mErrorMessage;

    public AuthViewModel() {
        mSignInState = new MutableLiveData<>();
        mSignUpState = new MutableLiveData<>();
        mErrorMessage = new MutableLiveData<>();
        mSignInState.setValue(Configuration.AuthStageState.NONE);
        mSignUpState.setValue(Configuration.AuthStageState.NONE);
        mErrorMessage.setValue("");
    }

    public void signIn(String email, String password) {

        mSignInState.postValue(Configuration.AuthStageState.PROGRESS);

        if (StringUtilities.isNotValidEmail(email)) {
            mSignInState.postValue(Configuration.AuthStageState.ERROR_EMAIL);

        } else {

            new AuthRepository().signIn(email, password, new OnAuthProgressListener() {
                @Override
                public void onSuccessful() {
                    mSignInState.postValue(Configuration.AuthStageState.SUCCESS);
                }

                @Override
                public void onFailure(String errorMessage) {
                    mErrorMessage.postValue(errorMessage);
                    mSignInState.postValue(Configuration.AuthStageState.FAIL);
                }
            });
        }
    }

    public void signUp(String firstName, String lastName,
                       String username, String email, String password) {

        mSignUpState.postValue(Configuration.AuthStageState.PROGRESS);

        if (StringUtilities.isEmpty(username)) {
            mSignUpState.postValue(Configuration.AuthStageState.ERROR_USERNAME);
            return;
        }

        FirebaseUtilities.findUserByUsername(username, new OnFindUserListener() {
            @Override
            public void onSuccessful(User user) {
                mSignUpState.postValue(Configuration.AuthStageState.ERROR_USERNAME);
            }

            @Override
            public void onDoesNotExist() {
                signUpContinue(firstName, lastName, username, email, password);
            }

            @Override
            public void onFailure(String errorMessage) {
                mErrorMessage.postValue(errorMessage);
                mSignUpState.postValue(Configuration.AuthStageState.FAIL);
            }
        });
    }

    private void signUpContinue(String firstName, String lastName,
                                String username, String email, String password) {
        if (StringUtilities.isEmpty(firstName)) {
            mSignUpState.postValue(Configuration.AuthStageState.ERROR_FIRST_NAME);

        } else if (StringUtilities.isEmpty(lastName)) {
            mSignUpState.postValue(Configuration.AuthStageState.ERROR_LAST_NAME);

        } else if (StringUtilities.isNotValidEmail(email)) {
            mSignUpState.postValue(Configuration.AuthStageState.ERROR_EMAIL);

        } else if (!StringUtilities.isValidPassword(password)) {
            mSignUpState.postValue(Configuration.AuthStageState.ERROR_PASSWORD);

        } else {

            new AuthRepository().signUp(firstName, lastName, username,
                    email, password, new OnAuthProgressListener() {

                        @Override
                        public void onSuccessful() {
                            Log.d("#DS RESULT", "SUCCESSFUL");
                            mSignUpState.postValue(Configuration.AuthStageState.SUCCESS);
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            mErrorMessage.postValue(errorMessage);
                            mSignUpState.postValue(Configuration.AuthStageState.FAIL);
                        }
                    });
        }
    }

    public LiveData<Configuration.AuthStageState> getSignInState() {
        return mSignInState;
    }

    public LiveData<Configuration.AuthStageState> getSignUpState() {
        return mSignUpState;
    }

    public LiveData<String> getErrorMessage() {
        return mErrorMessage;
    }
}
