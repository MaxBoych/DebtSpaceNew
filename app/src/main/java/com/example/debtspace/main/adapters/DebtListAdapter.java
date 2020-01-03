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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.debtspace.R;
import com.example.debtspace.config.Configuration;
import com.example.debtspace.main.interfaces.OnListItemClickListener;
import com.example.debtspace.models.Debt;
import com.example.debtspace.models.GroupDebt;

import java.util.List;

public class DebtListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private OnListItemClickListener mOnListItemClickListener;

    private List<Debt> mList;
    private Context mContext;

    public DebtListAdapter(List<Debt> list, Context context) {
        mList = list;
        mContext = context;
    }

    public void setOnListItemClickListener(OnListItemClickListener listener) {
        mOnListItemClickListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == Configuration.GROUP_DEBT_TYPE) {
            View view = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.group_debt, parent, false);
            return new  GroupDebtViewHolder(view);
        } else {
            View view = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.debt, parent, false);
            return new DebtViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Debt debt = mList.get(position);
        if (debt instanceof GroupDebt) {
            return Configuration.GROUP_DEBT_TYPE;
        } else if (debt != null) {
            return Configuration.DEBT_TYPE;
        } else {
            return -1;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case Configuration.GROUP_DEBT_TYPE:
                GroupDebt groupDebt = (GroupDebt) mList.get(position);
                GroupDebtViewHolder groupDebtViewHolder = (GroupDebtViewHolder) holder;

                groupDebtViewHolder.total_debt.setText(groupDebt.getDebt());
                GradientDrawable totalDebtBackground = (GradientDrawable) groupDebtViewHolder.total_debt.getBackground();
                totalDebtBackground.setColor(Color.YELLOW);

                String num_of_part = Integer.toString(groupDebt.getMembers().size());
                groupDebtViewHolder.number_of_participants.setText(num_of_part);

                double user_debt = Math.round((Double.parseDouble(groupDebt.getDebt()) / groupDebt.getMembers().size()) * 1000) / 1000;
                String u_debt = Double.toString(user_debt);
                groupDebtViewHolder.user_debt.setText(u_debt);
                GradientDrawable userDebtBackground = (GradientDrawable) groupDebtViewHolder.user_debt.getBackground();
                userDebtBackground.setColor(Color.RED);

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

            case Configuration.DEBT_TYPE:
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
                    debtBackground.setColor(Color.RED);
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

    class DebtViewHolder extends RecyclerView.ViewHolder {

        private final ImageView image;
        private final TextView name;
        //private final TextView username;
        private final TextView debt;

        DebtViewHolder(@NonNull View itemView) {

            super(itemView);

            image = itemView.findViewById(R.id.debt_user_image);
            name = itemView.findViewById(R.id.debt_user_name);
            //username = itemView.findViewById(R.id.debt_user_username);
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
        private final TextView number_of_participants;

        GroupDebtViewHolder(@NonNull View itemView) {

            super(itemView);

            image = itemView.findViewById(R.id.group_debt_image);
            name = itemView.findViewById(R.id.group_debt_name);
            total_debt = itemView.findViewById(R.id.group_total_debt);
            user_debt = itemView.findViewById(R.id.group_user_debt);
            number_of_participants = itemView.findViewById(R.id.number_of_participants);

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
}


