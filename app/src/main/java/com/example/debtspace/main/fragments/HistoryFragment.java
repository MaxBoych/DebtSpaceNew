package com.example.debtspace.main.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.debtspace.R;
import com.example.debtspace.main.adapters.HistoryAdapter;
import com.example.debtspace.main.viewmodels.HistoryViewModel;
import com.example.debtspace.models.HistoryItem;

public class HistoryFragment extends Fragment {

    private RecyclerView mList;
    private HistoryAdapter mAdapter;
    private HistoryViewModel mViewModel;
    private ProgressBar mProgressBar;
    private ProgressBar mEventProgressBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_history, container, false);
        mList = view.findViewById(R.id.history_list);
        mProgressBar = view.findViewById(R.id.history_progress_bar);
        mEventProgressBar = view.findViewById(R.id.history_event_progress_bar);

        initViewModel();

        return view;
    }

    private void initViewModel() {
        mViewModel = ViewModelProviders.of(this).get(HistoryViewModel.class);
        mViewModel.setContext(getContext());
        initAdapter();
        observeLoadState();
        observeEventState();
        mViewModel.downloadHistoryList();
    }

    private void initAdapter() {
        mAdapter = new HistoryAdapter(mViewModel.getList(), getContext());
        mList.setLayoutManager(new LinearLayoutManager(this.getContext()));
        mList.setAdapter(mAdapter);
    }

    private void observeLoadState() {
        mViewModel.getLoadState().observe(this, state -> {
            switch (state) {
                case SUCCESS:
                    mAdapter.updateList(mViewModel.getList());
                    mProgressBar.setVisibility(View.GONE);
                    mViewModel.addListChangeListener();
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

    private void observeEventState() {
        mViewModel.getEventState().observe(this, state -> {
            switch (state) {
                case ADDED:
                    HistoryItem addedRequest = mViewModel.getAddedRequest();
                    mAdapter.addItemToTop(addedRequest);
                    mEventProgressBar.setVisibility(View.GONE);
                    break;
                case PROGRESS:
                    mEventProgressBar.setVisibility(View.VISIBLE);
                    break;
                case NONE:
                    mEventProgressBar.setVisibility(View.GONE);
                    break;
                case FAIL:
                    mEventProgressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(),
                            mViewModel.getErrorMessage().getValue(),
                            Toast.LENGTH_LONG)
                            .show();
                    break;
            }
        });
    }
}
