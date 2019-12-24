package com.example.debtspace.main.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.debtspace.R;
import com.example.debtspace.main.adapters.RequestListAdapter;
import com.example.debtspace.main.interfaces.OnMainStateChangeListener;
import com.example.debtspace.main.viewmodels.RequestListViewModel;
import com.example.debtspace.models.User;
import com.google.android.material.tabs.TabLayout;

public class RequestListFragment extends Fragment {

    private RecyclerView mList;
    private RequestListAdapter mAdapter;

    private RequestListViewModel mViewModel;

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

        View view = inflater.inflate(R.layout.fragment_notification_list, container, false);
        mList = view.findViewById(R.id.notification_list);
        mProgressBar = view.findViewById(R.id.notification_list_progress_bar);

        TabLayout tablayout = view.findViewById(R.id.TabLayout);

        initViewModel();

        updateList();

        observeList();

        tablayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch(tab.getPosition()) {
                    case 0:
                    case 1:
                        mViewModel.downloadRequestList();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        return view;
    }

    private void initViewModel() {
        mViewModel = ViewModelProviders.of(this).get(RequestListViewModel.class);

        mViewModel.getList()
                .observe(this, users -> {
                    updateList();
                    mAdapter.notifyDataSetChanged();
                });
    }

    private void updateList() {
        mAdapter = new RequestListAdapter(mViewModel.getList().getValue());
        mAdapter.setOnListItemClickListener(position -> {
            User item = mViewModel.getRequest(position);
            mOnMainStateChangeListener.onRequestConfirmScreen(item);
        });
        mList.setLayoutManager(new GridLayoutManager(this.getContext(), 1));
        mList.setAdapter(mAdapter);
    }

    private void observeList() {

        mViewModel.getListState().observe(this, listStageState -> {
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
}
