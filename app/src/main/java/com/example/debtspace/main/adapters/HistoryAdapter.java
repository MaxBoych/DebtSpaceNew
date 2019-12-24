package com.example.debtspace.main.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.debtspace.R;
import com.example.debtspace.models.HistoryItem;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ItemViewHolder> {
    private List<HistoryItem> mList;

    public HistoryAdapter(List<HistoryItem> store) {
        mList = store;
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView friendName;
        private final TextView friendDebt;
        private final TextView comment;
        private final TextView date;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            friendName = itemView.findViewById(R.id.history_user_name);
            friendDebt = itemView.findViewById(R.id.history_user_debt);
            comment = itemView.findViewById(R.id.history_comment);
            date = itemView.findViewById(R.id.history_date);
        }
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemViewHolder(
                LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.item_history_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        HistoryItem item = mList.get(position);

        holder.friendName.setText(item.getUser());
        holder.friendDebt.setText(item.getDebt());
        holder.comment.setText(item.getComment());
        holder.date.setText(item.getDate());
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }
}
