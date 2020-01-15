package com.example.debtspace.main.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.debtspace.R;
import com.example.debtspace.application.DebtSpaceApplication;
import com.example.debtspace.config.AppConfig;
import com.example.debtspace.main.interfaces.OnPassSignalListener;
import com.example.debtspace.main.viewmodels.DebtRemovalViewModel;
import com.example.debtspace.models.DebtRequest;
import com.example.debtspace.models.HistoryItem;

import java.util.Objects;

public class DebtRemovalDialog extends DialogFragment implements View.OnClickListener {

    private DebtRequest mRequest;
    private Button mYes;
    private Button mCancel;

    private DebtRemovalViewModel mViewModel;

    private ProgressBar mProgressBar;

    public DebtRemovalDialog newInstance(HistoryItem item) {
        Bundle args = new Bundle();
        args.putString(AppConfig.ID_KEY, item.getId());
        args.putString(AppConfig.NAME_KEY, item.getName());
        args.putString(AppConfig.USERNAME_KEY, item.getUsername());
        args.putString(AppConfig.DEBT_KEY, item.getDebt());
        args.putString(AppConfig.DEBT_DATE_KEY, item.getDate());
        DebtRemovalDialog fragment = new DebtRemovalDialog();
        fragment.setArguments(args);

        return fragment;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_debt_removal, container, false);

        mYes = view.findViewById(R.id.yes_button);
        mCancel = view.findViewById(R.id.cancel_button);
        mProgressBar = view.findViewById(R.id.progress_bar);

        Bundle args = getArguments();
        if (args != null) {
            mRequest = new DebtRequest(args);

            TextView name = view.findViewById(R.id.user_name);
            name.setText(mRequest.getName());

            TextView debt = view.findViewById(R.id.request_debt);
            GradientDrawable debtBackground = (GradientDrawable) debt.getBackground();
            double debtValue = Double.parseDouble(mRequest.getDebt());
            if (debtValue < 0) {
                String val = Double.toString(-debtValue);
                debt.setText(val);
                debtBackground.setColor(Color.GREEN);
            } else {
                String val = Double.toString(debtValue);
                debt.setText(val);
                debtBackground.setColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.red));
            }

            TextView date = view.findViewById(R.id.debt_date);
            date.setText(mRequest.getDebtDate());

            initViewModel();

            mYes.setOnClickListener(this);
            mCancel.setOnClickListener(this);
        }

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.yes_button) {
            sendDebtRequest();
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
        mViewModel = ViewModelProviders.of(this).get(DebtRemovalViewModel.class);
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

    private void sendDebtRequest() {
        mViewModel.sendDebtRequest(mRequest);
    }

    private void setLoadProgressBarVisibility(int view) {
        mProgressBar.setVisibility(view);
    }
}
