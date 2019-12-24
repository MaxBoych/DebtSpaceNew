package com.example.debtspace.auth.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.debtspace.R;
import com.example.debtspace.auth.interfaces.OnAuthStateChangeListener;

public class AuthFragment extends Fragment implements View.OnClickListener {

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

        View view = inflater.inflate(R.layout.fragment_auth, container, false);

        view.findViewById(R.id.from_auth_to_sign_in).setOnClickListener(this);
        view.findViewById(R.id.from_auth_to_sign_up).setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.from_auth_to_sign_in) {
            mOnAuthStateChangeListener.onSignInScreen();

        } else if (v.getId() == R.id.from_auth_to_sign_up) {
            mOnAuthStateChangeListener.onSignUpScreen();
        }
    }
}
