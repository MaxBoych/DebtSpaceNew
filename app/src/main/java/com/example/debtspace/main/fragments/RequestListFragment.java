package com.example.debtspace.main.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

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
import com.example.debtspace.models.Request;
import com.example.debtspace.models.User;

public class RequestListFragment extends Fragment {

    private RecyclerView mList;
    private RequestListAdapter mAdapter;

    private RequestListViewModel mViewModel;

    private ProgressBar mProgressBar;
    private ProgressBar mEventProgressBar;

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
        mEventProgressBar = view.findViewById(R.id.notification_list_event_progress_bar);

        initViewModel();

        return view;
    }

    private void initViewModel() {
        mViewModel = ViewModelProviders.of(this).get(RequestListViewModel.class);
        mViewModel.setContext(getContext());
        initAdapter();
        observeLoadState();
        observeEventState();
        mViewModel.downloadRequestList();
    }

    private void initAdapter() {
        mAdapter = new RequestListAdapter(mViewModel.getList(), getContext());
        mAdapter.setOnListItemClickListener(position -> {
            User item = mViewModel.getRequest(position);
            mOnMainStateChangeListener.onRequestConfirmScreen(item);
        });
        mList.setLayoutManager(new GridLayoutManager(this.getContext(), 1));
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
                    Request addedRequest = mViewModel.getChangedRequest();
                    mAdapter.addItemToTop(addedRequest);
                    mEventProgressBar.setVisibility(View.GONE);
                    break;
                case REMOVED:
                    Request removedRequest = mViewModel.getChangedRequest();
                    int index = mViewModel.removeItem(removedRequest.getUsername());
                    mAdapter.removeItem(index);
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
