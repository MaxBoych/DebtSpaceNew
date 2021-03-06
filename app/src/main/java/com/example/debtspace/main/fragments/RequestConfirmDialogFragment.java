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
import com.example.debtspace.main.viewmodels.RequestConfirmViewModel;
import com.example.debtspace.models.User;

import java.util.Objects;

public class RequestConfirmDialogFragment extends DialogFragment implements View.OnClickListener {

    private TextView mName;
    private TextView mUsername;
    private Button mAccept;
    private Button mReject;

    private RequestConfirmViewModel mViewModel;

    private ProgressBar mProgressBar;

    public RequestConfirmDialogFragment newInstance(User user) {
        Bundle args = new Bundle();
        RequestConfirmDialogFragment fragment = new RequestConfirmDialogFragment();

        args.putString(Configuration.NAME_KEY, user.getFirstName() + " " + user.getLastName());
        args.putString(Configuration.USERNAME_KEY, user.getUsername());
        fragment.setArguments(args);

        return fragment;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_request_confirm, container, false);

        mName = view.findViewById(R.id.request_name);
        mUsername = view.findViewById(R.id.request_username);
        mAccept = view.findViewById(R.id.request_accept);
        mReject = view.findViewById(R.id.request_reject);
        mProgressBar = view.findViewById(R.id.request_progress_bar);

        String name = Objects.requireNonNull(getArguments()).getString(Configuration.NAME_KEY);
        mName.setText(name);
        String username = getArguments().getString(Configuration.USERNAME_KEY);
        mUsername.setText(username);

        initViewModel();
        observeState();

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

    private void observeState() {

        mViewModel.getState().observe(this, stageState -> {
            switch (stageState) {
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
        mViewModel.acceptFriendRequest(username);
    }

    private void rejectRequest(String username) {
        mViewModel.rejectFriendRequest(username);
    }
}
