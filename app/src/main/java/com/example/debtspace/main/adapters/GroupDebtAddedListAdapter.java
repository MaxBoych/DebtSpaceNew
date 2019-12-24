package com.example.debtspace.main.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.debtspace.R;
import com.example.debtspace.main.interfaces.OnListItemClickListener;
import com.example.debtspace.models.User;

import java.util.List;

public class GroupDebtAddedListAdapter extends RecyclerView.Adapter<GroupDebtAddedListAdapter.GroupDebtAddedListViewHolder> {

    private List<User> mList;

    private OnListItemClickListener mOnListItemClickListener;

    public GroupDebtAddedListAdapter(List<User> list) {
        this.mList = list;
    }

    public void setOnListItemClickListener(OnListItemClickListener listener) {
        mOnListItemClickListener = listener;
    }

    @NonNull
    @Override
    public GroupDebtAddedListAdapter.GroupDebtAddedListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view_item = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_added_list, parent, false);
        return new GroupDebtAddedListAdapter.GroupDebtAddedListViewHolder(view_item);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupDebtAddedListAdapter.GroupDebtAddedListViewHolder holder, int position) {
        User user = mList.get(position);

        String fullUserName = user.getFirstName() + " " + user.getLastName();
        holder.name.setText(fullUserName);
        holder.username.setText(user.getUsername());
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    class GroupDebtAddedListViewHolder extends RecyclerView.ViewHolder {

        private final TextView name;
        private final TextView username;

        GroupDebtAddedListViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.added_user_name);
            username = itemView.findViewById(R.id.added_user_username);

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
