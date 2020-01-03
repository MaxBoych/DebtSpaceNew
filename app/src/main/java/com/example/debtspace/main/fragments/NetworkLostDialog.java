package com.example.debtspace.main.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.debtspace.R;

public class NetworkLostDialog extends DialogFragment implements View.OnClickListener {

    private Button mOK;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_network_lost, container, false);
        mOK = view.findViewById(R.id.network_lost_ok);

        mOK.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.network_lost_ok) {
            dismiss();
        }
    }
}
