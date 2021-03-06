package com.example.debtspace.main.fragments;

import androidx.fragment.app.DialogFragment;
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
import androidx.lifecycle.ViewModelProviders;

import com.example.debtspace.R;
import com.example.debtspace.config.Configuration;
import com.example.debtspace.main.viewmodels.StrikeViewModel;
import com.example.debtspace.models.HistoryItem;
import com.example.debtspace.models.User;
import com.example.debtspace.utilities.StringUtilities;

import java.util.Objects;

public class StrikeDialogFragment extends DialogFragment implements View.OnClickListener {

    private EditText mBill;
    private Button mStrike;
    private TextView mName;
    private TextView mComment;
    private String mUsername;

    private StrikeViewModel mViewModel;
    private ProgressBar mProgressBar;

    public StrikeDialogFragment newInstance(User user) {
        Bundle args = new Bundle();
        args.putString(Configuration.NAME_KEY, user.getFirstName() + " " + user.getLastName());
        args.putString(Configuration.USERNAME_KEY, user.getUsername());
        StrikeDialogFragment fragment = new StrikeDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_strike, container, false);
        mName = view.findViewById(R.id.profile_name);
        mComment = view.findViewById(R.id.strike_comment);
        mBill = view.findViewById(R.id.strike_bill);
        mStrike = view.findViewById(R.id.strike_button);
        mProgressBar = view.findViewById(R.id.strike_progress_bar);

        mUsername = Objects.requireNonNull(getArguments()).getString(Configuration.USERNAME_KEY);
        mName.setText(Objects.requireNonNull(getArguments()).getString(Configuration.NAME_KEY));

        initViewModel();
        observeStrikeState();

        mStrike.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.strike_button && !StringUtilities.isEmpty(mBill.getText().toString())) {
            HistoryItem item = new HistoryItem(mUsername, mBill.getText().toString(),
                    mComment.getText().toString(), "mm/dd/yyyy");
            doStrike(item);
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

    private void observeStrikeState() {

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

    private void doStrike(HistoryItem item) {
        mViewModel.doStrike(item);
    }
}
