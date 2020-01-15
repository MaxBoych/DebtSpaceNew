package com.example.debtspace.main.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.debtspace.R;
import com.example.debtspace.config.AppConfig;
import com.example.debtspace.main.adapters.UserSearchListAdapter;
import com.example.debtspace.main.interfaces.OnMainStateChangeListener;
import com.example.debtspace.main.viewmodels.UserSearchListViewModel;
import com.example.debtspace.models.User;

public class UserSearchListFragment extends Fragment implements View.OnClickListener {

    private EditText mUserSearch;
    private RecyclerView mList;
    private UserSearchListAdapter mAdapter;
    private TextWatcher mTextWatcher;
    private int mFilterID;

    /*private Button mAllUsers;
    private Button mFriends;
    private Button mGroups;*/

    private UserSearchListViewModel mViewModel;
    private ProgressBar mProgressBar;

    private OnMainStateChangeListener mOnMainStateChangeListener;

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

        View view = inflater.inflate(R.layout.fragment_user_search_list, container, false);
        mList = view.findViewById(R.id.user_search_list);
        mUserSearch = view.findViewById(R.id.user_search_field);
        mProgressBar = view.findViewById(R.id.user_search_list_progress_bar);

        initViewModel();

        view.findViewById(R.id.all_users_button).setOnClickListener(this);
        view.findViewById(R.id.friends_button).setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.all_users_button) {
            addTextChangedListener(AppConfig.SEARCH_FILTER_ALL_USERS_ID);
        } else if (v.getId() == R.id.friends_button) {
            addTextChangedListener(AppConfig.SEARCH_FILTER_FRIENDS_ID);
        }
    }

    private void initViewModel() {
        mViewModel = ViewModelProviders.of(this).get(UserSearchListViewModel.class);
        initAdapter();
        observeLoadState();
        addTextChangedListener(AppConfig.SEARCH_FILTER_ALL_USERS_ID);
    }

    private void initAdapter() {
        mAdapter = new UserSearchListAdapter(mViewModel.getList(), getContext());
        mAdapter.setOnListItemClickListener(position -> {
            if (mFilterID == AppConfig.SEARCH_FILTER_ALL_USERS_ID) {
                User user = mViewModel.getUser(position);
                mOnMainStateChangeListener.onFriendRequestScreen(user);
            }
        });
        mList.setLayoutManager(new GridLayoutManager(this.getContext(), 1));
        mList.setAdapter(mAdapter);
    }

    private void observeLoadState() {
        mViewModel.getLoadState().observe(this, state -> {
            switch (state) {
                case SUCCESS:
                    mAdapter.updateList(mViewModel.getList(), mViewModel.getFilterID());
                    mProgressBar.setVisibility(View.GONE);
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

    private void addTextChangedListener(int filterID) {
        mFilterID = filterID;
        mUserSearch.removeTextChangedListener(mTextWatcher);
        mTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mViewModel.downloadUserSearchList(filterID, s, getContext());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };
        mUserSearch.addTextChangedListener(mTextWatcher);
        mUserSearch.setText(mUserSearch.getText());
    }
}
