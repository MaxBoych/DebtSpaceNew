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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.debtspace.R;
import com.example.debtspace.main.adapters.UserSearchListAdapter;
import com.example.debtspace.main.interfaces.OnMainStateChangeListener;
import com.example.debtspace.main.viewmodels.UserSearchListViewModel;
import com.example.debtspace.models.User;

public class UserSearchListFragment extends Fragment implements View.OnClickListener {

    private EditText mUserSearch;
    private RecyclerView mList;
    private UserSearchListAdapter mAdapter;

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
        observeUserSearchList();
        textChangeListen();

        view.findViewById(R.id.button_to_debt_list).setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_to_debt_list) {
            mOnMainStateChangeListener.onDebtListScreen();
        }
    }

    private void initViewModel() {
        mViewModel = ViewModelProviders.of(this).get(UserSearchListViewModel.class);

        initUserSearchList();

        mViewModel.getUserSearchList()
                .observe(this, users -> {
                    initUserSearchList();
                    mAdapter.notifyDataSetChanged();
                });
    }

    private void initUserSearchList() {
        mAdapter = new UserSearchListAdapter(mViewModel.getUserSearchList().getValue());
        mAdapter.setOnListItemClickListener(position -> {
            User user = mViewModel.getUser(position);
            mOnMainStateChangeListener.onFriendRequestScreen(user);
        });
        mList.setLayoutManager(new GridLayoutManager(this.getContext(), 1));
        mList.setAdapter(mAdapter);
    }

    private void observeUserSearchList() {

        mViewModel.getUserSearchState().observe(this, listStageState -> {
            switch (listStageState) {
                case SUCCESS:
                case FAIL:
                case NONE:
                    mProgressBar.setVisibility(View.GONE);
                    break;
                case PROGRESS:
                    mProgressBar.setVisibility(View.VISIBLE);
                    break;
            }
        });
    }

    private void textChangeListen() {

        mUserSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mViewModel.downloadUserSearchList(s);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
}
