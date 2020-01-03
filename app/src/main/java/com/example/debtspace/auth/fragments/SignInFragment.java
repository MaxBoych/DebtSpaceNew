package com.example.debtspace.auth.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.debtspace.R;
import com.example.debtspace.auth.interfaces.OnAuthStateChangeListener;
import com.example.debtspace.auth.viewmodels.AuthViewModel;

public class SignInFragment extends Fragment implements View.OnClickListener {

    private EditText mEmail;
    private EditText mPassword;

    private ProgressBar mProgressBar;

    private Button mSignInButton;
    private Button mFromSignInToSignUpButton;

    private TextView mSignInError;

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

        View view = inflater.inflate(R.layout.fragment_sign_in, container, false);

        mAuthViewModel = ViewModelProviders.of(this).get(AuthViewModel.class);

        mEmail = view.findViewById(R.id.email);
        mPassword = view.findViewById(R.id.password);

        mSignInButton = view.findViewById(R.id.button_sign_in);
        mFromSignInToSignUpButton = view.findViewById(R.id.from_sign_in_to_sign_up);

        mProgressBar = view.findViewById(R.id.sign_in_progress_bar);

        mSignInError = view.findViewById(R.id.sign_in_error);

        observeSignIn();

        mSignInButton.setOnClickListener(this);
        mFromSignInToSignUpButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_sign_in) {
            String email = mEmail.getText().toString();
            String password = mPassword.getText().toString();

            mAuthViewModel.signIn(email, password);
        } else if (v.getId() == R.id.from_sign_in_to_sign_up) {
            mOnAuthStateChangeListener.onSignUpScreen();
        }
    }

    private void observeSignIn() {
        mAuthViewModel.getSignInState().observe(this, authStageState -> {
            switch (authStageState) {
                case SUCCESS:
                    editTextSetNull();
                    buttonsSetEnabled(false);
                    mProgressBar.setVisibility(View.GONE);
                    mOnAuthStateChangeListener.onMainScreen();
                    break;
                case FAIL:
                    buttonsSetEnabled(true);
                    mProgressBar.setVisibility(View.GONE);
                    mSignInError.setVisibility(View.VISIBLE);

                    String errorMessage = mAuthViewModel.getErrorMessage().getValue();
                    mOnAuthStateChangeListener.onFailure(errorMessage);
                    break;
                case PROGRESS:
                    mSignInError.setVisibility(View.GONE);
                    editTextSetNull();
                    buttonsSetEnabled(false);
                    mProgressBar.setVisibility(View.VISIBLE);
                    break;
                case ERROR_EMAIL:
                    errorEmail();
                    buttonsSetEnabled(true);
                    mProgressBar.setVisibility(View.GONE);
                    break;
                case NONE:
                    editTextSetNull();
                    buttonsSetEnabled(true);
                    mSignInError.setVisibility(View.GONE);
                    mProgressBar.setVisibility(View.GONE);
                    break;
            }
        });
    }

    private void editTextSetNull() {
        mEmail.setError(null);
        mPassword.setError(null);
    }

    private void buttonsSetEnabled(boolean bool) {
        mSignInButton.setEnabled(bool);
        mFromSignInToSignUpButton.setEnabled(bool);
    }

    private void errorEmail() {
        mEmail.setError("Email failed validation!");
        mPassword.setError(null);
    }
}
