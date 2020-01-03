package com.example.debtspace.main.adapters;

import android.content.Context;
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
import com.example.debtspace.main.interfaces.OnListItemClickListener;
import com.example.debtspace.models.User;

import java.util.List;

public class RequestListAdapter extends RecyclerView.Adapter<RequestListAdapter.FriendRequestsListViewHolder> {

    private List<User> mList;
    private Context mContext;

    private OnListItemClickListener mOnListItemClickListener;

    public RequestListAdapter(List<User> list, Context context) {
        mList = list;
        mContext = context;
    }

    public void setOnListItemClickListener(OnListItemClickListener listener) {
        mOnListItemClickListener = listener;
    }

    @NonNull
    @Override
    public FriendRequestsListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View friend_view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_user_list, parent, false);
        return new FriendRequestsListViewHolder(friend_view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendRequestsListViewHolder holder, int position) {
        User user = mList.get(position);

        Uri uri = user.getImageUri();
        Glide.with(mContext)
                .load(uri)
                .centerCrop()
                .into(holder.image);

        String fullUserName = user.getFirstName() + " " + user.getLastName();
        holder.name.setText(fullUserName);
        holder.username.setText(user.getUsername());
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    class FriendRequestsListViewHolder extends RecyclerView.ViewHolder {

        private final ImageView image;
        private final TextView name;
        private final TextView username;

        FriendRequestsListViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.user_photo);
            name = itemView.findViewById(R.id.user_name);
            username = itemView.findViewById(R.id.user_username);

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
