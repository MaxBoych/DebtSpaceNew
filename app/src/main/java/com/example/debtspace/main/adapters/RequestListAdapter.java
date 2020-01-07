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
import com.example.debtspace.models.Request;

import java.util.ArrayList;
import java.util.List;

public class RequestListAdapter extends RecyclerView.Adapter<RequestListAdapter.FriendRequestsListViewHolder> {

    private List<Request> mList;
    private Context mContext;

    private OnListItemClickListener mOnListItemClickListener;

    public RequestListAdapter(List<Request> list, Context context) {
        if (list != null) {
            mList = new ArrayList<>(list);
        }
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
                .inflate(R.layout.item_request_list, parent, false);
        return new FriendRequestsListViewHolder(friend_view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendRequestsListViewHolder holder, int position) {
        Request request = mList.get(position);

        Uri uri = request.getImageUri();
        Glide.with(mContext)
                .load(uri)
                .centerCrop()
                .into(holder.image);

        String fullUserName = request.getFirstName() + " " + request.getLastName();
        holder.name.setText(fullUserName);
        holder.username.setText(request.getUsername());
        holder.date.setText(request.getDate());
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    class FriendRequestsListViewHolder extends RecyclerView.ViewHolder {

        private final ImageView image;
        private final TextView name;
        private final TextView username;
        private final TextView date;

        FriendRequestsListViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.request_image);
            name = itemView.findViewById(R.id.request_name);
            username = itemView.findViewById(R.id.request_username);
            date = itemView.findViewById(R.id.request_date);

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

    public void updateList(List<Request> list) {
        mList = new ArrayList<>(list);
        this.notifyDataSetChanged();
    }

    public void addItemToTop(Request request) {
        mList.add(0, request);
        this.notifyItemInserted(0);
    }
}
