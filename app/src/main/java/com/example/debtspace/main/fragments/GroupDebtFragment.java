package com.example.debtspace.main.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.debtspace.R;
import com.example.debtspace.config.Configuration;
import com.example.debtspace.main.adapters.GroupDebtAddedListAdapter;
import com.example.debtspace.main.adapters.GroupDebtFoundListAdapter;
import com.example.debtspace.main.interfaces.OnImageSharingListener;
import com.example.debtspace.main.interfaces.OnMainStateChangeListener;
import com.example.debtspace.main.viewmodels.GroupDebtViewModel;
import com.example.debtspace.models.GroupDebt;

import java.util.ArrayList;
import java.util.Objects;

public class GroupDebtFragment extends Fragment implements View.OnClickListener, OnImageSharingListener {

    private ImageView mImage;
    private Uri mImageUri;
    private EditText mName;
    private EditText mFriendSearch;
    private EditText mDebt;
    private String mID;
    private Button mSubmit;
    private boolean mIsCreate;

    private RecyclerView mFoundList;
    private RecyclerView mAddedList;
    private GroupDebtFoundListAdapter mFoundAdapter;
    private GroupDebtAddedListAdapter mAddedAdapter;

    private GroupDebtViewModel mViewModel;

    private ProgressBar mProgressBar;

    private OnMainStateChangeListener mOnMainStateChangeListener;

    public GroupDebtFragment newInstance(GroupDebt debt) {
        Bundle args = new Bundle();
        args.putString(Configuration.ID_KEY, debt.getId());
        args.putString(Configuration.NAME_KEY, debt.getName());
        args.putString(Configuration.DEBT_KEY, debt.getDebt());
        args.putStringArrayList(Configuration.MEMBERS_KEY, new ArrayList<>(debt.getMembers()));
        GroupDebtFragment fragment = new GroupDebtFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mOnMainStateChangeListener = (OnMainStateChangeListener) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_group_debt, container, false);
        mImage = view.findViewById(R.id.group_debt_image);
        mFoundList = view.findViewById(R.id.group_debt_found_list);
        mAddedList = view.findViewById(R.id.group_debt_added_list);
        mName = view.findViewById(R.id.group_debt_name);
        mDebt = view.findViewById(R.id.group_debt_debt);
        mFriendSearch = view.findViewById(R.id.group_debt_search);
        mProgressBar = view.findViewById(R.id.group_debt_progress_bar);
        mSubmit = view.findViewById(R.id.button_submit);
        mIsCreate = true;

        initViewModel();
        if (getArguments() != null) {
            mID = getArguments().getString(Configuration.ID_KEY);
            mName.setText(getArguments().getString(Configuration.NAME_KEY));
            mDebt.setText(getArguments().getString(Configuration.DEBT_KEY));
            mViewModel.downloadAddedList(getArguments().getStringArrayList(Configuration.MEMBERS_KEY), mID);
            mSubmit.setText(getString(R.string.update_group));
            mIsCreate = false;
        } else {
            mViewModel.downloadFriendList();
        }
        textChangeListen();

        mSubmit.setOnClickListener(this);
        mImage.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_submit) {
            if (mIsCreate) {
                createGroup();
            } else {
                updateGroup();
            }
        } else if (v.getId() == R.id.group_debt_image) {
            mOnMainStateChangeListener.onImageManagementScreen(mID, this);
        }
    }

    @Override
    public void onUploaded(Uri uri) {
        mImageUri = uri;
        drawImage();
    }

    private void initViewModel() {
        mViewModel = ViewModelProviders.of(this).get(GroupDebtViewModel.class);
        mViewModel.setContext(getContext());
        initFoundAdapter();
        initAddedAdapter();
        observeLoadState();
    }

    private void initFoundAdapter() {
        mFoundAdapter = new GroupDebtFoundListAdapter(mViewModel.getFoundList(), getContext());
        mFoundList.setLayoutManager(new GridLayoutManager(this.getContext(), 1));

        mFoundAdapter.setOnListItemClickListener(position ->
                mViewModel.addToGroup(position));

        mFoundList.setAdapter(mFoundAdapter);
    }

    private void initAddedAdapter() {
        mAddedAdapter = new GroupDebtAddedListAdapter(mViewModel.getAddedList());
        mAddedList.setLayoutManager(new GridLayoutManager(this.getContext(), 1));

        mAddedAdapter.setOnListItemClickListener(position ->
                mViewModel.removeFromGroup(position));

        mAddedList.setAdapter(mAddedAdapter);
    }

    private void observeLoadState() {
        mViewModel.getLoadState().observe(this, state -> {
            switch (state) {
                case SUCCESS:
                    mProgressBar.setVisibility(View.GONE);
                    if (mViewModel.getIsCreate()) {
                        mOnMainStateChangeListener.onDebtListScreen();
                    }

                    mAddedAdapter.updateList(mViewModel.getAddedList());
                    mFoundAdapter.updateList(mViewModel.getFoundList());
                    Uri uri = mViewModel.getUriImage();
                    if (uri != null) {
                        mImageUri = uri;
                        drawImage();
                    }

                    setEnabled(true);
                    break;
                case FAIL:
                    mProgressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(),
                            mViewModel.getErrorMessage().getValue(),
                            Toast.LENGTH_LONG)
                            .show();
                    setEnabled(true);
                    break;
                case NONE:
                    mProgressBar.setVisibility(View.GONE);
                    setEnabled(true);
                    break;
                case PROGRESS:
                    mProgressBar.setVisibility(View.VISIBLE);
                    setEnabled(false);
                    break;
            }
        });
    }

    private void setEnabled(boolean bool) {
        mName.setEnabled(bool);
        mDebt.setEnabled(bool);
        mFriendSearch.setEnabled(bool);
        mSubmit.setEnabled(bool);
    }

    private void textChangeListen() {

        mFriendSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mViewModel.textChangeListen(s);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void drawImage() {
        if (mImageUri != null) {
            Glide.with(Objects.requireNonNull(getContext()))
                    .load(mImageUri)
                    .centerCrop()
                    .into(mImage);
        }
    }

    private void createGroup() {
        mViewModel.createGroup(mName.getText().toString(), mDebt.getText().toString(), mImageUri);
    }

    private void updateGroup() {
        mViewModel.updateGroup(mID, mName.getText().toString(), mDebt.getText().toString());
    }
}
