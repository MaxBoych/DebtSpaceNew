package com.example.debtspace.main.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.debtspace.R;
import com.example.debtspace.config.AppConfig;
import com.example.debtspace.main.interfaces.OnListItemClickListener;
import com.example.debtspace.models.Debt;
import com.example.debtspace.models.GroupDebt;

import java.util.ArrayList;
import java.util.List;

public class DebtListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private OnListItemClickListener mOnListItemClickListener;
    //private OnLoadMoreListener mOnLoadMoreListener;

    private List<Debt> mList;
    private Context mContext;
    //private RecyclerView mRecyclerView;
    //private LinearLayoutManager mManager;
    //private int mVisibleAmount;
    //private int mVisibleThreshold;

    public DebtListAdapter(RecyclerView recyclerView, List<Debt> list, Context context) {
        if (list != null) {
            mList = new ArrayList<>(list);
        }
        mContext = context;
        //mRecyclerView = recyclerView;
        //mManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        //mVisibleAmount = 5;
        //mVisibleThreshold = 5;

        //addScrollListener();
    }

    /*private void addScrollListener() {
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int totalItemCount = mManager.getItemCount();
                int lastVisibleItem = mManager.findLastVisibleItemPosition();
                //Log.d("#DS", "total: " + totalItemCount + " last: " + lastVisibleItem);
                if (totalItemCount <= lastVisibleItem + 1) {
                    increaseVisibleAmount();
                    mOnLoadMoreListener.onLoadMore();
                }
            }
        });
    }*/

    public void setOnListItemClickListener(OnListItemClickListener listener) {
        mOnListItemClickListener = listener;
    }

    /*public void setOnLoadMoreListener(OnLoadMoreListener listener) {
        mOnLoadMoreListener = listener;
    }*/

    public void updateList(List<Debt> list) {
        mList = new ArrayList<>(list);
        this.notifyDataSetChanged();
    }

    @SuppressWarnings("ConstantConditions")
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == AppConfig.GROUP_DEBT_TYPE) {
            View view = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.group_debt, parent, false);
            return new  GroupDebtViewHolder(view);
        } else if (viewType == AppConfig.DEBT_TYPE) {
            View view = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.debt, parent, false);
            return new DebtViewHolder(view);
        }

        return null;
    }

    @Override
    public int getItemViewType(int position) {
        Debt debt = mList.get(position);
        if (debt instanceof GroupDebt) {
            return AppConfig.GROUP_DEBT_TYPE;
        } else if (debt != null) {
            return AppConfig.DEBT_TYPE;
        } else {
            return -1;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case AppConfig.GROUP_DEBT_TYPE:
                GroupDebt groupDebt = (GroupDebt) mList.get(position);
                GroupDebtViewHolder groupDebtViewHolder = (GroupDebtViewHolder) holder;

                groupDebtViewHolder.total_debt.setText(groupDebt.getDebt());
                GradientDrawable totalDebtBackground = (GradientDrawable) groupDebtViewHolder.total_debt.getBackground();
                totalDebtBackground.setColor(Color.YELLOW);

                //String num_of_part = Integer.toString(groupDebt.getMembers().size());
                //groupDebtViewHolder.number_of_participants.setText(num_of_part);

                double user_debt = Math.round((Double.parseDouble(groupDebt.getDebt()) / groupDebt.getMembers().size()) * 1000) / 1000;
                String u_debt = Double.toString(user_debt);
                groupDebtViewHolder.user_debt.setText(u_debt);
                GradientDrawable userDebtBackground = (GradientDrawable) groupDebtViewHolder.user_debt.getBackground();
                userDebtBackground.setColor(ContextCompat.getColor(mContext, R.color.red));

                Uri uri1 = groupDebt.getUriImage();
                if (uri1 != null) {
                    Glide.with(mContext)
                            .load(uri1)
                            .centerCrop()
                            .into(groupDebtViewHolder.image);
                }

                groupDebtViewHolder.name.setText(groupDebt.getName());
                groupDebtViewHolder.total_debt.setText(groupDebt.getDebt());
                break;

            case AppConfig.DEBT_TYPE:
                DebtViewHolder debtViewHolder = (DebtViewHolder) holder;

                Debt debt = mList.get(position);
                Uri uri2 = debt.getUriImage();
                if (uri2 != null) {
                    Glide.with(mContext)
                            .load(uri2)
                            .centerCrop()
                            .into(debtViewHolder.image);
                }

                String userFullName = debt.getUser().getFirstName() + " " + debt.getUser().getLastName();
                debtViewHolder.name.setText(userFullName);

                GradientDrawable debtBackground = (GradientDrawable) debtViewHolder.debt.getBackground();
                double debtValue = Double.parseDouble(debt.getDebt());
                if (debtValue > 0) {
                    String val = Double.toString(debtValue);
                    debtViewHolder.debt.setText(val);
                    debtBackground.setColor(ContextCompat.getColor(mContext, R.color.red));
                } else if (debtValue == 0) {
                    debtViewHolder.debt.setText("0");
                    debtBackground.setColor(Color.GRAY);
                } else {
                    String val = Double.toString(-debtValue);
                    debtViewHolder.debt.setText(val);
                    debtBackground.setColor(Color.GREEN);
                }

                break;
        }
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    /*private void increaseVisibleAmount() {
        mVisibleAmount += mVisibleThreshold;
        if (mVisibleAmount > mList.size()) {
            mVisibleAmount = mList.size();
        }
    }*/

    class DebtViewHolder extends RecyclerView.ViewHolder {

        private final ImageView image;
        private final TextView name;
        private final TextView debt;

        DebtViewHolder(@NonNull View itemView) {

            super(itemView);

            image = itemView.findViewById(R.id.debt_user_image);
            name = itemView.findViewById(R.id.debt_user_name);
            debt = itemView.findViewById(R.id.debt_user_debt);

            itemView.setOnClickListener(v -> {
                if (mOnListItemClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        mOnListItemClickListener.onItemClicked(position);
                    }
                }
            });
        }
    }

    class GroupDebtViewHolder extends RecyclerView.ViewHolder {

        private final ImageView image;
        private final TextView name;
        private final TextView total_debt;
        private final TextView user_debt;
        //private final TextView number_of_participants;

        GroupDebtViewHolder(@NonNull View itemView) {

            super(itemView);

            image = itemView.findViewById(R.id.group_debt_image);
            name = itemView.findViewById(R.id.group_debt_name);
            total_debt = itemView.findViewById(R.id.group_total_debt);
            user_debt = itemView.findViewById(R.id.group_user_debt);
            //number_of_participants = itemView.findViewById(R.id.number_of_participants);

            itemView.setOnClickListener(v -> {
                if (mOnListItemClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        mOnListItemClickListener.onItemClicked(position);
                    }
                }
            });
        }
    }

    public void addItemToTop(Debt debt) {
        mList.add(0, debt);
        this.notifyItemInserted(0);
    }

    public void setAndMoveItem(int index, Debt debt) {
        mList.set(index, debt);
        Debt debtCopy = mList.remove(index);
        mList.add(0, debtCopy);

        for (Debt debt1 : mList) {
            if (debt1 instanceof GroupDebt) {
                continue;
            }
        }

        this.notifyItemMoved(index, 0);
        this.notifyItemRangeChanged(0, index + 1);
    }

    public void removeItem(int index) {
        if (index != -1) {
            mList.remove(index);
            this.notifyItemRemoved(index);
        }
    }
}
