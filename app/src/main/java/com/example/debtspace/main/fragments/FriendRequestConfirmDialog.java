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
import com.example.debtspace.main.viewmodels.RequestConfirmViewModel;
import com.example.debtspace.models.FriendRequest;

import java.util.Objects;

public class FriendRequestConfirmDialog extends DialogFragment implements View.OnClickListener {

    private TextView mName;
    private TextView mUsername;
    private Button mAccept;
    private Button mReject;

    private RequestConfirmViewModel mViewModel;

    private ProgressBar mProgressBar;

    public FriendRequestConfirmDialog newInstance(FriendRequest request) {
        Bundle args = new Bundle();
        FriendRequestConfirmDialog fragment = new FriendRequestConfirmDialog();

        args.putString(AppConfig.NAME_KEY, request.getName());
        args.putString(AppConfig.USERNAME_KEY, request.getUsername());
        fragment.setArguments(args);

        return fragment;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_friend_request_confirm, container, false);

        mName = view.findViewById(R.id.user_name);
        mUsername = view.findViewById(R.id.user_username);
        mAccept = view.findViewById(R.id.request_accept);
        mReject = view.findViewById(R.id.request_reject);
        mProgressBar = view.findViewById(R.id.progress_bar);

        String name = Objects.requireNonNull(getArguments()).getString(AppConfig.NAME_KEY);
        mName.setText(name);
        String username = getArguments().getString(AppConfig.USERNAME_KEY);
        mUsername.setText(username);

        initViewModel();
        observeLoadState();

        mAccept.setOnClickListener(this);
        mReject.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.request_accept) {
            acceptRequest(mUsername.getText().toString());
        } else if (v.getId() == R.id.request_reject) {
            rejectRequest(mUsername.getText().toString());
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
        mViewModel = ViewModelProviders.of(this).get(RequestConfirmViewModel.class);
    }

    private void observeLoadState() {
        mViewModel.getLoadState().observe(this, state -> {
            switch (state) {
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

    private void acceptRequest(String username) {
        mViewModel.acceptFriendRequest(username, getContext());
    }

    private void rejectRequest(String username) {
        mViewModel.rejectFriendRequest(username, getContext());
    }
}
