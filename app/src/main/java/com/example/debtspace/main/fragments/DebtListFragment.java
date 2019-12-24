package com.example.debtspace.main.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class DebtListFragment extends Fragment implements View.OnClickListener {

    private RecyclerView mList;
    private DebtListAdapter mAdapter;

    private DebtListViewModel mViewModel;

    private ProgressBar mProgressBar;

    private Button mCreateDebt;

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
        mCreateDebt = view.findViewById(R.id.button_create_group_debt);

        initViewModel();
        observeDebtList();
        mViewModel.uploadDebtList();

        view.findViewById(R.id.button_sign_out).setOnClickListener(this);
        view.findViewById(R.id.button_to_user_search).setOnClickListener(this);
        mCreateDebt.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_sign_out) {
            mOnMainStateChangeListener.onAuthScreen();
        } else if (v.getId() == R.id.button_to_user_search) {
            mOnMainStateChangeListener.onUserSearchListScreen();
        } else if (v.getId() == R.id.button_create_group_debt) {
            mOnMainStateChangeListener.onGroupDebtScreen();
        }
    }

    private void initViewModel() {
        mViewModel = ViewModelProviders.of(this).get(DebtListViewModel.class);

        updateDebtList();

        mViewModel.getDebtList()
                .observe(this, debtListItems -> {
                    updateDebtList();
                    mAdapter.notifyDataSetChanged();
                });
    }

    private void updateDebtList() {
        mAdapter = new DebtListAdapter(mViewModel.getDebtList().getValue());
        mAdapter.setOnListItemClickListener(position -> {
            Debt item = mViewModel.getDebtListItem(position);
            if (item instanceof GroupDebt) {
                mOnMainStateChangeListener.onGroupDebtScreen((GroupDebt) item);
            } else {
                mOnMainStateChangeListener.onStrikeScreen(item.getUser());
            }
        });
        mList.setLayoutManager(new GridLayoutManager(this.getContext(), 1));
        mList.setAdapter(mAdapter);
    }

    private void observeDebtList() {
        mViewModel.getDebtListState().observe(this, userSearchStageState -> {
            switch (userSearchStageState) {
                case SUCCESS:
                case NONE:
                    mProgressBar.setVisibility(View.GONE);
                    break;
                case FAIL:
                    mProgressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(),
                            mViewModel.getErrorMessage().getValue(),
                            Toast.LENGTH_LONG)
                            .show();
                    break;
                case PROGRESS:
                    mProgressBar.setVisibility(View.VISIBLE);
                    break;
            }
        });

    }
}
