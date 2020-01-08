package com.example.debtspace.auth.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.debtspace.auth.interfaces.OnAuthProgressListener;
import com.example.debtspace.auth.repositories.AuthRepository;
import com.example.debtspace.config.AppConfig;
import com.example.debtspace.main.interfaces.OnFindUserListener;
import com.example.debtspace.models.User;
import com.example.debtspace.utilities.FirebaseUtilities;
import com.example.debtspace.utilities.StringUtilities;

public class AuthViewModel extends ViewModel {

    private MutableLiveData<AppConfig.AuthStageState> mSignInState;
    private MutableLiveData<AppConfig.AuthStageState> mSignUpState;
    private MutableLiveData<String> mErrorMessage;

    public AuthViewModel() {
        mSignInState = new MutableLiveData<>();
        mSignInState.setValue(AppConfig.AuthStageState.NONE);
        mSignUpState = new MutableLiveData<>();
        mSignUpState.setValue(AppConfig.AuthStageState.NONE);
        mErrorMessage = new MutableLiveData<>();
        mErrorMessage.setValue(AppConfig.DEFAULT_ERROR_VALUE);
    }

    public void signIn(String email, String password) {
        mSignInState.setValue(AppConfig.AuthStageState.PROGRESS);
        if (StringUtilities.isNotValidEmail(email)) {
            mSignInState.setValue(AppConfig.AuthStageState.ERROR_EMAIL);
        } else {
            new AuthRepository().signIn(email, password, new OnAuthProgressListener() {
                @Override
                public void onSuccessful() {
                    updateSignInStateToSuccess();
                }

                @Override
                public void onFailure(String errorMessage) {
                    updateErrorForSignIn(errorMessage);
                }
            });
        }
    }

    private void updateSignInStateToSuccess() {
        mSignInState.setValue(AppConfig.AuthStageState.SUCCESS);
    }

    private void updateErrorForSignIn(String errorMessage) {
        mErrorMessage.setValue(errorMessage);
        mSignInState.setValue(AppConfig.AuthStageState.FAIL);
    }

    public void signUp(String firstName, String lastName,
                       String username, String email, String password) {
        mSignUpState.setValue(AppConfig.AuthStageState.PROGRESS);
        if (StringUtilities.isEmpty(username) || username.equals(AppConfig.STRING_DEFAULT)) {
            updateSignUpStateToErrorUsername();
            return;
        }

        FirebaseUtilities.findUserByUsername(username, new OnFindUserListener() {
            @Override
            public void onSuccessful(User user) {
                updateSignUpStateToErrorUsername();
            }

            @Override
            public void onDoesNotExist() {
                signUpContinue(firstName, lastName, username, email, password);
            }

            @Override
            public void onFailure(String errorMessage) {
                updateErrorForSignUp(errorMessage);
            }
        });
    }

    private void updateErrorForSignUp(String errorMessage) {
        mErrorMessage.setValue(errorMessage);
        mSignUpState.setValue(AppConfig.AuthStageState.FAIL);
    }

    private void updateSignUpStateToErrorUsername() {
        mSignUpState.setValue(AppConfig.AuthStageState.ERROR_USERNAME);
    }

    private void signUpContinue(String firstName, String lastName,
                                String username, String email, String password) {
        if (StringUtilities.isEmpty(firstName)) {
            mSignUpState.setValue(AppConfig.AuthStageState.ERROR_FIRST_NAME);

        } else if (StringUtilities.isEmpty(lastName)) {
            mSignUpState.setValue(AppConfig.AuthStageState.ERROR_LAST_NAME);

        } else if (StringUtilities.isNotValidEmail(email)) {
            mSignUpState.setValue(AppConfig.AuthStageState.ERROR_EMAIL);

        } else if (!StringUtilities.isValidPassword(password)) {
            mSignUpState.setValue(AppConfig.AuthStageState.ERROR_PASSWORD);

        } else {

            new AuthRepository().signUp(firstName, lastName, username,
                    email, password, new OnAuthProgressListener() {

                        @Override
                        public void onSuccessful() {
                            mSignUpState.setValue(AppConfig.AuthStageState.SUCCESS);
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            updateErrorForSignUp(errorMessage);
                        }
                    });
        }
    }

    public LiveData<AppConfig.AuthStageState> getSignInState() {
        return mSignInState;
    }

    public LiveData<AppConfig.AuthStageState> getSignUpState() {
        return mSignUpState;
    }

    public LiveData<String> getErrorMessage() {
        return mErrorMessage;
    }
}
