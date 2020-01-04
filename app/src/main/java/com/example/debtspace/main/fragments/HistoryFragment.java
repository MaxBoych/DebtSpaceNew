package com.example.debtspace.main.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.debtspace.R;
import com.example.debtspace.main.adapters.HistoryAdapter;
import com.example.debtspace.main.viewmodels.HistoryViewModel;

public class HistoryFragment extends Fragment {
    private RecyclerView mList;
    private HistoryAdapter mAdapter;
    private HistoryViewModel mViewModel;
    private ProgressBar mProgress;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_history, container, false);
        mList = view.findViewById(R.id.history_list);
        mProgress = view.findViewById(R.id.history_progress_bar);

        initViewModel();
        observeHistory();
        mViewModel.downloadHistoryList(getContext());

        return view;
    }

    private void initViewModel() {
        mViewModel = ViewModelProviders.of(this).get(HistoryViewModel.class);

        initHistory();

        mViewModel.getDataList()
                .observe(this, Items -> {
                    initHistory();
                    mAdapter.notifyDataSetChanged();
                });
    }

    private void initHistory() {
        mAdapter = new HistoryAdapter(mViewModel.getDataList().getValue(), getContext());
        mList.setLayoutManager(new LinearLayoutManager(this.getContext()));
        mList.setAdapter(mAdapter);
    }

    private void observeHistory() {
        mViewModel.getListState().observe(this, userSearchStageState -> {
            switch (userSearchStageState) {
                case SUCCESS:
                case FAIL:
                case NONE:
                    mProgress.setVisibility(View.GONE);
                    break;
                case PROGRESS:
                    mProgress.setVisibility(View.VISIBLE);
                    break;
            }
        });
    }
}
