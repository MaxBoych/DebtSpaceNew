package com.example.debtspace.main.adapters;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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
        private final TextView name;
        private final TextView debt;
        private final TextView comment;
        private final TextView date;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.history_user_name);
            debt = itemView.findViewById(R.id.history_user_debt);
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

        holder.name.setText(item.getUsername());
        holder.debt.setText(item.getDebt());
        holder.comment.setText(item.getComment());
        holder.date.setText(item.getDate());

        GradientDrawable debtBackground = (GradientDrawable) holder.debt.getBackground();
        double debtValue = Double.parseDouble(item.getDebt());
        if (debtValue > 0) {
            String val = Double.toString(debtValue);
            holder.debt.setText(val);
            debtBackground.setColor(Color.RED);
        } else if (debtValue == 0) {
            holder.debt.setText("0");
            debtBackground.setColor(Color.GRAY);
        } else {
            String val = Double.toString(-debtValue);
            holder.debt.setText(val);
            debtBackground.setColor(Color.GREEN);
        }
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }
}
