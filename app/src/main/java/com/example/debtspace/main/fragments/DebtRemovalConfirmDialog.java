package com.example.debtspace.main.fragments;

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
import com.example.debtspace.config.AppConfig;
import com.example.debtspace.main.viewmodels.DebtRemovalConfirmViewModel;
import com.example.debtspace.models.DebtRequest;

import java.util.Objects;

public class DebtRemovalConfirmDialog extends DialogFragment implements View.OnClickListener {

    private TextView mName;
    private TextView mDebtDate;
    private DebtRequest mRequest;
    private Button mAccept;
    private Button mReject;

    private DebtRemovalConfirmViewModel mViewModel;

    private ProgressBar mProgressBar;

    public DebtRemovalConfirmDialog newInstance(DebtRequest request) {
        Bundle args = new Bundle();
        args.putString(AppConfig.ID_KEY, request.getId());
        args.putString(AppConfig.NAME_KEY, request.getName());
        args.putString(AppConfig.USERNAME_KEY, request.getUsername());
        args.putString(AppConfig.DEBT_KEY, request.getDebt());
        args.putString(AppConfig.DATE_KEY, request.getDate());
        args.putString(AppConfig.DEBT_DATE_KEY, request.getDebtDate());
        DebtRemovalConfirmDialog fragment = new DebtRemovalConfirmDialog();
        fragment.setArguments(args);

        return fragment;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_debt_removal_confirm, container, false);

        mName = view.findViewById(R.id.user_name);
        mDebtDate = view.findViewById(R.id.debt_date);

        mAccept = view.findViewById(R.id.accept_button);
        mReject = view.findViewById(R.id.reject_button);
        mProgressBar = view.findViewById(R.id.progress_bar);

        Bundle args = getArguments();
        if (args != null) {
            mRequest = new DebtRequest(args);
            mName.setText(mRequest.getName());

            TextView debt = view.findViewById(R.id.request_debt);
            debt.setText(mRequest.getDebt());
            GradientDrawable debtBackground = (GradientDrawable) debt.getBackground();
            double debtValue = Double.parseDouble(mRequest.getDebt());
            if (debtValue < 0) {
                String val = Double.toString(-debtValue);
                debt.setText(val);
                debtBackground.setColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.red));
            } else {
                String val = Double.toString(debtValue);
                debt.setText(val);
                debtBackground.setColor(Color.GREEN);
            }

            mDebtDate.setText(mRequest.getDebtDate());

            initViewModel();

            mAccept.setOnClickListener(this);
            mReject.setOnClickListener(this);
        }

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.accept_button) {
            acceptRequest(mRequest);
        } else if (v.getId() == R.id.reject_button) {
            rejectRequest(mRequest.getId());
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
        mViewModel = ViewModelProviders.of(this).get(DebtRemovalConfirmViewModel.class);
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

    private void acceptRequest(DebtRequest request) {
        mViewModel.acceptDebtRemovalRequest(request);
    }

    private void rejectRequest(String id) {
        mViewModel.rejectDebtRemovalRequest(id);
    }

    private void setLoadProgressBarVisibility(int view) {
        mProgressBar.setVisibility(view);
    }
}
