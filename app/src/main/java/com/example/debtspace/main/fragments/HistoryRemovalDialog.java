package com.example.debtspace.main.fragments;

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
import com.example.debtspace.main.interfaces.OnPassSignalListener;
import com.example.debtspace.main.viewmodels.HistoryRemovalViewModel;
import com.example.debtspace.utilities.StringUtilities;

import java.util.Objects;

public class HistoryRemovalDialog extends DialogFragment implements View.OnClickListener {

    private Button mClear;
    private Button mCancel;
    private String mCode;
    private EditText mInput;

    private HistoryRemovalViewModel mViewModel;

    private ProgressBar mProgressBar;
    private OnPassSignalListener mOnPassSignalListener;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_friend_removal, container, false);

        mClear = view.findViewById(R.id.clear_button);
        mCancel = view.findViewById(R.id.cancel_button);
        mProgressBar = view.findViewById(R.id.progress_bar);
        mInput = view.findViewById(R.id.input_field);

        mCode = StringUtilities.getRandomString(AppConfig.RANDOM_CODE_SIZE);
        TextView generatedCode = view.findViewById(R.id.generated_code);
        generatedCode.setText(mCode);

        mInput = view.findViewById(R.id.input_field);

        initViewModel();

        mClear.setOnClickListener(this);
        mCancel.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.clear_button && mCode.equals(mInput.getText().toString())) {
            clearHistory();
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

    public void setPassSignalListener(OnPassSignalListener listener) {
        mOnPassSignalListener = listener;
    }

    private void initViewModel() {
        mViewModel = ViewModelProviders.of(this).get(HistoryRemovalViewModel.class);
        mViewModel.setContext(getContext());
        observeLoadState();
    }

    private void observeLoadState() {
        mViewModel.getLoadState().observe(this, state -> {
            switch (state) {
                case SUCCESS:
                    setLoadProgressBarVisibility(View.GONE);
                    mOnPassSignalListener.onPass();
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

    private void clearHistory() {
        mViewModel.clearWholeHistory();
    }

    private void setLoadProgressBarVisibility(int view) {
        mProgressBar.setVisibility(view);
    }
}
