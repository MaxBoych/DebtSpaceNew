package com.example.debtspace.auth.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.debtspace.auth.interfaces.OnAuthProgressListener;
import com.example.debtspace.auth.repositories.AuthRepository;
import com.example.debtspace.config.Configuration;
import com.example.debtspace.main.interfaces.OnFindUserListener;
import com.example.debtspace.models.User;
import com.example.debtspace.utilities.FirebaseUtilities;
import com.example.debtspace.utilities.StringUtilities;

public class AuthViewModel extends ViewModel {

    private MutableLiveData<Configuration.AuthStageState> mSignInState;
    private MutableLiveData<Configuration.AuthStageState> mSignUpState;
    private MutableLiveData<String> mErrorMessage;

    public AuthViewModel() {
        mSignInState = new MutableLiveData<>();
        mSignInState.setValue(Configuration.AuthStageState.NONE);
        mSignUpState = new MutableLiveData<>();
        mSignUpState.setValue(Configuration.AuthStageState.NONE);
        mErrorMessage = new MutableLiveData<>();
        mErrorMessage.setValue("");
    }

    public void signIn(String email, String password) {
        mSignInState.setValue(Configuration.AuthStageState.PROGRESS);
        if (StringUtilities.isNotValidEmail(email)) {
            mSignInState.setValue(Configuration.AuthStageState.ERROR_EMAIL);
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
        mSignUpState.setValue(Configuration.AuthStageState.SUCCESS);
    }

    private void updateErrorForSignIn(String errorMessage) {
        mErrorMessage.setValue(errorMessage);
        mSignInState.setValue(Configuration.AuthStageState.FAIL);
    }

    public void signUp(String firstName, String lastName,
                       String username, String email, String password) {
        mSignUpState.setValue(Configuration.AuthStageState.PROGRESS);
        if (StringUtilities.isEmpty(username) || username.equals(Configuration.STRING_DEFAULT)) {
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
        mSignUpState.setValue(Configuration.AuthStageState.FAIL);
    }

    private void updateSignUpStateToErrorUsername() {
        mSignUpState.setValue(Configuration.AuthStageState.ERROR_USERNAME);
    }

    private void signUpContinue(String firstName, String lastName,
                                String username, String email, String password) {
        if (StringUtilities.isEmpty(firstName)) {
            mSignUpState.setValue(Configuration.AuthStageState.ERROR_FIRST_NAME);

        } else if (StringUtilities.isEmpty(lastName)) {
            mSignUpState.setValue(Configuration.AuthStageState.ERROR_LAST_NAME);

        } else if (StringUtilities.isNotValidEmail(email)) {
            mSignUpState.setValue(Configuration.AuthStageState.ERROR_EMAIL);

        } else if (!StringUtilities.isValidPassword(password)) {
            mSignUpState.setValue(Configuration.AuthStageState.ERROR_PASSWORD);

        } else {

            new AuthRepository().signUp(firstName, lastName, username,
                    email, password, new OnAuthProgressListener() {

                        @Override
                        public void onSuccessful() {
                            mSignUpState.setValue(Configuration.AuthStageState.SUCCESS);
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            updateErrorForSignUp(errorMessage);
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
