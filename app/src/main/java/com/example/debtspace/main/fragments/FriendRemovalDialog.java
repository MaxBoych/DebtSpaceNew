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
import com.example.debtspace.main.viewmodels.FriendRemovalViewModel;

import java.util.Objects;

public class FriendRemovalDialog extends DialogFragment implements View.OnClickListener {

    private TextView mUserName;
    private TextView mUserUsername;
    private Button mYes;
    private Button mCancel;

    private FriendRemovalViewModel mViewModel;

    private ProgressBar mProgressBar;

    public FriendRemovalDialog newInstance(String name, String username) {
        Bundle args = new Bundle();
        args.putString(AppConfig.NAME_KEY, name);
        args.putString(AppConfig.USERNAME_KEY, username);
        FriendRemovalDialog fragment = new FriendRemovalDialog();
        fragment.setArguments(args);

        return fragment;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_friend_removal, container, false);

        mUserName = view.findViewById(R.id.user_name);
        mUserUsername = view.findViewById(R.id.user_username);
        mYes = view.findViewById(R.id.yes_button);
        mCancel = view.findViewById(R.id.cancel_button);
        mProgressBar = view.findViewById(R.id.progress_bar);

        String name = Objects.requireNonNull(getArguments()).getString(AppConfig.NAME_KEY);
        mUserName.setText(name);
        String username = Objects.requireNonNull(getArguments()).getString(AppConfig.USERNAME_KEY);
        mUserUsername.setText(username);

        initViewModel();

        mYes.setOnClickListener(this);
        mCancel.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.yes_button) {
            removeFriend(mUserUsername.getText().toString());
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
        mViewModel = ViewModelProviders.of(this).get(FriendRemovalViewModel.class);
        mViewModel.setContext(getContext());
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

    private void removeFriend(String username) {
        mViewModel.removeFriend(username);
    }

    private void setLoadProgressBarVisibility(int view) {
        mProgressBar.setVisibility(view);
    }
}
