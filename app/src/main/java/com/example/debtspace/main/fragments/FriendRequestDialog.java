package com.example.debtspace.main.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.debtspace.R;
import com.example.debtspace.config.AppConfig;
import com.example.debtspace.main.viewmodels.FriendRequestViewModel;
import com.example.debtspace.models.User;

import java.util.Objects;

public class FriendRequestDialog extends DialogFragment implements View.OnClickListener {

    private TextView mUserName;
    private TextView mUserUsername;
    private Button mYes;
    private Button mCancel;

    private FriendRequestViewModel mViewModel;

    private ProgressBar mProgressBar;

    public FriendRequestDialog newInstance(User user) {
        Bundle args = new Bundle();
        args.putString(AppConfig.NAME_KEY, user.getFirstName() + " " + user.getLastName());
        args.putString(AppConfig.USERNAME_KEY, user.getUsername());
        FriendRequestDialog fragment = new FriendRequestDialog();
        fragment.setArguments(args);

        return fragment;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_friend_request, container, false);

        mUserName = view.findViewById(R.id.user_name);
        mUserUsername = view.findViewById(R.id.user_username);
        mYes = view.findViewById(R.id.yes_button);
        mCancel = view.findViewById(R.id.cancel_button);
        mProgressBar = view.findViewById(R.id.progress_bar);

        if (getArguments() != null) {
            String name = getArguments().getString(AppConfig.NAME_KEY);
            mUserName.setText(name);
            String username = getArguments().getString(AppConfig.USERNAME_KEY);
            mUserUsername.setText(username);
        }

        initViewModel();

        mYes.setOnClickListener(this);
        mCancel.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.yes_button) {
            sendFriendRequest(mUserUsername.getText().toString());
        } else if (v.getId() == R.id.cancel_button) {
            dismiss();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Objects.requireNonNull(Objects.requireNonNull(getDialog()).getWindow())
                .setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.WRAP_CONTENT);
    }

    private void initViewModel() {
        mViewModel = ViewModelProviders.of(this).get(FriendRequestViewModel.class);
        observeLoadState();
    }

    private void observeLoadState() {
        mViewModel.getLoadState().observe(this, state -> {
            switch (state) {
                case SUCCESS:
                    setLoadProgressBarVisibility(View.GONE);
                    dismiss();
                    break;
                case FAIL:
                    setLoadProgressBarVisibility(View.GONE);
                    showError();
                    break;
                case NONE:
                    setLoadProgressBarVisibility(View.GONE);
                    break;
                case PROGRESS:
                    setLoadProgressBarVisibility(View.VISIBLE);
                    break;
            }
        });
    }

    private void showError() {
        Toast.makeText(getContext(),
                mViewModel.getErrorMessage().getValue(),
                Toast.LENGTH_LONG)
                .show();
    }

    private void sendFriendRequest(String username) {
        mViewModel.sendFriendRequest(username);
    }

    private void setLoadProgressBarVisibility(int view) {
        mProgressBar.setVisibility(view);
    }
}
