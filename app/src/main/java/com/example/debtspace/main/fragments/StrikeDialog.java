package com.example.debtspace.main.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.debtspace.R;
import com.example.debtspace.config.AppConfig;
import com.example.debtspace.main.interfaces.OnMainStateChangeListener;
import com.example.debtspace.main.viewmodels.StrikeViewModel;
import com.example.debtspace.models.HistoryItem;
import com.example.debtspace.models.User;
import com.example.debtspace.utilities.StringUtilities;

import java.util.Objects;
import java.util.UUID;

public class StrikeDialog extends DialogFragment implements View.OnClickListener {

    private EditText mBill;
    private Button mStrike;
    private Button mRemove;
    private TextView mName;
    private TextView mComment;
    private String mUsername;

    private StrikeViewModel mViewModel;
    private ProgressBar mProgressBar;

    private OnMainStateChangeListener mOnMainStateChangeListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mOnMainStateChangeListener = (OnMainStateChangeListener) context;
    }

    public StrikeDialog newInstance(User user) {
        Bundle args = new Bundle();
        args.putString(AppConfig.NAME_KEY, user.getFirstName() + " " + user.getLastName());
        args.putString(AppConfig.USERNAME_KEY, user.getUsername());
        StrikeDialog fragment = new StrikeDialog();
        fragment.setArguments(args);
        return fragment;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_strike, container, false);
        mName = view.findViewById(R.id.strike_user_name);
        mComment = view.findViewById(R.id.strike_comment);
        mBill = view.findViewById(R.id.strike_bill);
        mStrike = view.findViewById(R.id.strike_button);
        mRemove = view.findViewById(R.id.remove_friend_button);
        mProgressBar = view.findViewById(R.id.strike_progress_bar);

        mUsername = Objects.requireNonNull(getArguments()).getString(AppConfig.USERNAME_KEY);
        mName.setText(Objects.requireNonNull(getArguments()).getString(AppConfig.NAME_KEY));

        initViewModel();
        observeLoadState();

        mStrike.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.strike_button && !StringUtilities.isEmpty(mBill.getText().toString())) {
            String date = StringUtilities.getCurrentDateAndTime();
            HistoryItem item = new HistoryItem(mBill.getText().toString(),
                    mComment.getText().toString(), date, mUsername);
            doStrike(item);
        } else if (v.getId() == R.id.remove_friend_button) {
            mOnMainStateChangeListener.onFriendRemovalScreen(mName.getText().toString(), mUsername);
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
        mViewModel = ViewModelProviders.of(this).get(StrikeViewModel.class);
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

    private void doStrike(HistoryItem item) {
        mViewModel.doStrike(item, getContext());
    }
}
