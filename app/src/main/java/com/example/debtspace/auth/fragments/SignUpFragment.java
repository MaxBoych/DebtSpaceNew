package com.example.debtspace.auth.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.debtspace.R;
import com.example.debtspace.auth.interfaces.OnAuthStateChangeListener;
import com.example.debtspace.auth.viewmodels.AuthViewModel;

public class SignUpFragment extends Fragment implements View.OnClickListener {

    private EditText mFirstName;
    private EditText mLastName;
    private EditText mUsername;
    private EditText mEmail;
    private EditText mPassword;

    private ProgressBar mProgressBar;

    private Button mSignUpButton;
    private Button mFromSignUpToSignInButton;

    private AuthViewModel mAuthViewModel;

    private OnAuthStateChangeListener mOnAuthStateChangeListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        mOnAuthStateChangeListener = (OnAuthStateChangeListener) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);

        mAuthViewModel = ViewModelProviders.of(this).get(AuthViewModel.class);

        mFirstName = view.findViewById(R.id.first_name);
        mLastName = view.findViewById(R.id.last_name);
        mUsername = view.findViewById(R.id.username);
        mEmail = view.findViewById(R.id.email);
        mPassword = view.findViewById(R.id.password);

        mSignUpButton = view.findViewById(R.id.button_sign_up);
        mFromSignUpToSignInButton = view.findViewById(R.id.from_sign_up_to_sign_in);

        mProgressBar = view.findViewById(R.id.sign_up_progress_bar);

        observeSignUp();

        mSignUpButton.setOnClickListener(this);
        mFromSignUpToSignInButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_sign_up) {
            String firstName = mFirstName.getText().toString();
            String lastName = mLastName.getText().toString();
            String username = mUsername.getText().toString().toLowerCase();
            String email = mEmail.getText().toString();
            String password = mPassword.getText().toString();

            mAuthViewModel.signUp(firstName, lastName,
                    username, email, password);

        } else if (v.getId() == R.id.from_sign_up_to_sign_in) {
            mOnAuthStateChangeListener.onSignInScreen();
        }
    }

    private void observeSignUp() {
        mAuthViewModel.getSignUpState().observe(this, authStageState -> {
            switch (authStageState) {
                case SUCCESS:
                    editTextSetNull();
                    buttonsSetEnabled(false);
                    mProgressBar.setVisibility(View.GONE);
                    mOnAuthStateChangeListener.onSignInScreen();
                    break;
                case FAIL:
                    buttonsSetEnabled(true);
                    mProgressBar.setVisibility(View.GONE);

                    String errorMessage = mAuthViewModel.getErrorMessage().getValue();
                    mOnAuthStateChangeListener.onFailure(errorMessage);
                    break;
                case PROGRESS:
                    editTextSetNull();
                    buttonsSetEnabled(false);
                    mProgressBar.setVisibility(View.VISIBLE);
                    break;
                case ERROR_FIRST_NAME:
                    errorFirstName();
                    buttonsSetEnabled(true);
                    mProgressBar.setVisibility(View.GONE);
                    break;
                case ERROR_LAST_NAME:
                    errorLastName();
                    buttonsSetEnabled(true);
                    mProgressBar.setVisibility(View.GONE);
                    break;
                case ERROR_USERNAME:
                    errorUsername();
                    buttonsSetEnabled(true);
                    mProgressBar.setVisibility(View.GONE);
                    break;
                case ERROR_EMAIL:
                    errorEmail();
                    buttonsSetEnabled(true);
                    mProgressBar.setVisibility(View.GONE);
                    break;
                case ERROR_PASSWORD:
                    errorPassword();
                    buttonsSetEnabled(true);
                    mProgressBar.setVisibility(View.GONE);
                    break;
                case NONE:
                    editTextSetNull();
                    buttonsSetEnabled(true);
                    mProgressBar.setVisibility(View.GONE);
                    break;
            }
        });
    }

    private void editTextSetNull() {
        mFirstName.setError(null);
        mLastName.setError(null);
        mUsername.setError(null);
        mEmail.setError(null);
        mPassword.setError(null);
    }

    private void buttonsSetEnabled(boolean bool) {
        mSignUpButton.setEnabled(bool);
        mFromSignUpToSignInButton.setEnabled(bool);
    }

    private void errorFirstName() {
        mFirstName.setError("First name failed validation!");
        mLastName.setError(null);
        mUsername.setError(null);
        mEmail.setError(null);
        mPassword.setError(null);
    }

    private void errorLastName() {
        mLastName.setError("Last name failed validation!");
        mFirstName.setError(null);
        mUsername.setError(null);
        mEmail.setError(null);
        mPassword.setError(null);
    }

    private void errorUsername() {
        mUsername.setError("Username failed validation or already exists!");
        mFirstName.setError(null);
        mLastName.setError(null);
        mEmail.setError(null);
        mPassword.setError(null);
    }

    private void errorEmail() {
        mEmail.setError("Email failed validation!");
        mFirstName.setError(null);
        mLastName.setError(null);
        mUsername.setError(null);
        mPassword.setError(null);
    }

    private void errorPassword() {
        mPassword.setError("Password must contain at least 6 symbols: " +
                "uppercase, lowercase, digits");
        mFirstName.setError(null);
        mLastName.setError(null);
        mUsername.setError(null);
        mEmail.setError(null);
    }
}
