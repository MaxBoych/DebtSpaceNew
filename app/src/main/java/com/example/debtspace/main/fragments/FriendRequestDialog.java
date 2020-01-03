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
import com.example.debtspace.config.Configuration;
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
        args.putString(Configuration.NAME_KEY, user.getFirstName() + " " + user.getLastName());
        args.putString(Configuration.USERNAME_KEY, user.getUsername());
        FriendRequestDialog fragment = new FriendRequestDialog();
        fragment.setArguments(args);

        return fragment;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_friend_request, container, false);

        mUserName = view.findViewById(R.id.request_name);
        mUserUsername = view.findViewById(R.id.request_username);
        mYes = view.findViewById(R.id.friend_request_yes);
        mCancel = view.findViewById(R.id.friend_request_cancel);
        mProgressBar = view.findViewById(R.id.request_progress_bar);

        String name = Objects.requireNonNull(getArguments()).getString(Configuration.NAME_KEY);
        mUserName.setText(name);
        String username = Objects.requireNonNull(getArguments()).getString(Configuration.USERNAME_KEY);
        mUserUsername.setText(username);

        initViewModel();
        observeState();

        mYes.setOnClickListener(this);
        mCancel.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.friend_request_yes) {
            sendFriendRequest(mUserUsername.getText().toString());
        } else if (v.getId() == R.id.friend_request_cancel) {
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
    }

    private void observeState() {

        mViewModel.getState().observe(this, listStageState -> {
            switch (listStageState) {
                case SUCCESS:
                    mProgressBar.setVisibility(View.GONE);
                    dismiss();
                    break;
                case FAIL:
                    mProgressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(),
                            mViewModel.getErrorMessage().getValue(),
                            Toast.LENGTH_LONG)
                            .show();
                    break;
                case NONE:
                    mProgressBar.setVisibility(View.GONE);
                    break;
                case PROGRESS:
                    mProgressBar.setVisibility(View.VISIBLE);
                    break;
            }
        });
    }

    private void sendFriendRequest(String username) {
        mViewModel.sendFriendRequest(username, getContext());
    }
}
