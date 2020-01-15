package com.example.debtspace.main.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.debtspace.R;
import com.example.debtspace.main.adapters.DebtListAdapter;
import com.example.debtspace.main.interfaces.OnMainStateChangeListener;
import com.example.debtspace.main.viewmodels.DebtListViewModel;
import com.example.debtspace.models.Debt;
import com.example.debtspace.models.GroupDebt;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class DebtListFragment extends Fragment implements View.OnClickListener {

    private RecyclerView mList;
    private DebtListAdapter mAdapter;

    private DebtListViewModel mViewModel;

    private ProgressBar mProgressBar;
    private ProgressBar mEventProgressBar;

    private FloatingActionButton mCreateGroupDebt;

    private OnMainStateChangeListener mOnMainStateChangeListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mOnMainStateChangeListener = (OnMainStateChangeListener) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_debt_list, viewGroup, false);

        mList = view.findViewById(R.id.debt_list);
        mProgressBar = view.findViewById(R.id.debt_list_progress_bar);
        mEventProgressBar = view.findViewById(R.id.debt_list_event_progress_bar);
        mCreateGroupDebt = view.findViewById(R.id.button_create_group_debt);

        initViewModel();

        view.findViewById(R.id.button_to_user_search).setOnClickListener(this);
        mCreateGroupDebt.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_to_user_search) {
            mOnMainStateChangeListener.onUserSearchListScreen();
        } else if (v.getId() == R.id.button_create_group_debt) {
            mOnMainStateChangeListener.onGroupDebtScreen();
        }
    }

    private void initViewModel() {
        mViewModel = ViewModelProviders.of(this).get(DebtListViewModel.class);
        mViewModel.setContext(getContext());
        initAdapter();
        observeLoadState();
        observeEventState();
        mViewModel.downloadDebtList();
    }

    private void initAdapter() {
        mList.setLayoutManager(new GridLayoutManager(this.getContext(), 1));

        mAdapter = new DebtListAdapter(mList, mViewModel.getDebtList(), getContext());
        mAdapter.setOnListItemClickListener(position -> {
            Debt item = mViewModel.getDebtListItem(position);
            if (item instanceof GroupDebt) {
                mOnMainStateChangeListener.onGroupDebtScreen((GroupDebt) item);
            } else {
                mOnMainStateChangeListener.onStrikeScreen(item.getUser());
            }
        });

        mList.setAdapter(mAdapter);
    }

    private void observeLoadState() {
        mViewModel.getLoadState().observe(this, state -> {
            switch (state) {
                case SUCCESS:
                    updateAdapter();
                    setLoadProgressBarVisibility(View.GONE);
                    break;
                case NONE:
                    setLoadProgressBarVisibility(View.GONE);
                    break;
                case FAIL:
                    setLoadProgressBarVisibility(View.GONE);
                    showError();
                    break;
                case PROGRESS:
                    setLoadProgressBarVisibility(View.VISIBLE);
                    break;
            }
        });
    }

    private void updateAdapter() {
        mAdapter.updateList(mViewModel.getDebtList());
        mViewModel.observeDebtEvents();
    }

    private void showError() {
        Toast.makeText(getContext(),
                mViewModel.getErrorMessage().getValue(),
                Toast.LENGTH_LONG)
                .show();
    }

    private void setLoadProgressBarVisibility(int view) {
        mProgressBar.setVisibility(view);
    }

    private void observeEventState() {
        mViewModel.getEventState().observe(this, state -> {
            switch (state) {
                case ADDED:
                    eventAdded();
                    setEventProgressBarVisibility(View.GONE);
                    break;
                case MODIFIED:
                    eventModified();
                    setEventProgressBarVisibility(View.GONE);
                    break;
                case REMOVED:
                    eventRemoved();
                    setEventProgressBarVisibility(View.GONE);
                    break;
                case PROGRESS:
                    setEventProgressBarVisibility(View.VISIBLE);
                    break;
                case NONE:
                    setEventProgressBarVisibility(View.GONE);
                    break;
                case FAIL:
                    setEventProgressBarVisibility(View.GONE);
                    showError();
                    break;
            }
        });
    }

    private void eventAdded() {
        Debt addedDebt = mViewModel.getChangedDebt();
        mAdapter.addItemToTop(addedDebt);
    }

    private void eventModified() {
        Debt changedDebt = mViewModel.getChangedDebt();
        int modifyIndex = mViewModel.modifyItem(changedDebt);
        if (modifyIndex != -1) {
            Debt modifiedDebt = mViewModel.getDebtListItem(0);
            mAdapter.setAndMoveItem(modifyIndex, modifiedDebt);
        }
    }

    private void eventRemoved() {
        Debt removedDebt = mViewModel.getChangedDebt();
        int removeIndex = mViewModel.removeItem(removedDebt);
        if (removeIndex != -1) {
            mAdapter.removeItem(removeIndex);
        }
    }

    private void setEventProgressBarVisibility(int view) {
        mEventProgressBar.setVisibility(view);
    }
}
