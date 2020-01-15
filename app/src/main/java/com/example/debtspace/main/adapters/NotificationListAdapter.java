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
import com.example.debtspace.models.DebtRequest;
import com.example.debtspace.models.FriendRequest;
import com.example.debtspace.models.Notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Notification> mList;
    private Context mContext;

    private OnListItemClickListener mOnListItemClickListener;

    public NotificationListAdapter(List<Notification> list, Context context) {
        if (list != null) {
            mList = new ArrayList<>(list);
        }
        mContext = context;
    }

    public void setOnListItemClickListener(OnListItemClickListener listener) {
        mOnListItemClickListener = listener;
    }

    @SuppressWarnings("ConstantConditions")
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == AppConfig.FRIEND_REQUEST_TYPE) {
            View friendRequest = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.friend_request, parent, false);
            return new FriendRequestViewHolder(friendRequest);
        } else if (viewType == AppConfig.DEBT_REQUEST_TYPE) {
            View debtRequest = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.debt_request, parent, false);
            return new DebtRequestViewHolder(debtRequest);
        }

        return null;
    }

    @Override
    public int getItemViewType(int position) {
        Notification notification = mList.get(position);
        if (notification instanceof FriendRequest) {
            return AppConfig.FRIEND_REQUEST_TYPE;
        } else if (notification instanceof DebtRequest) {
            return AppConfig.DEBT_REQUEST_TYPE;
        } else {
            return -1;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Notification notification = mList.get(position);

        if (notification instanceof FriendRequest) {
            FriendRequest request = (FriendRequest) notification;
            FriendRequestViewHolder friendHolder = (FriendRequestViewHolder) holder;

            Uri uri = request.getImageUri();
            Glide.with(mContext)
                    .load(uri)
                    .centerCrop()
                    .into(friendHolder.image);
            friendHolder.name.setText(request.getName());
            friendHolder.username.setText(request.getUsername());
            friendHolder.date.setText(request.getDate());

        } else if (notification instanceof DebtRequest) {
            DebtRequest request = (DebtRequest) notification;
            DebtRequestViewHolder debtHolder = (DebtRequestViewHolder) holder;

            debtHolder.name.setText(request.getName());
            debtHolder.date.setText(request.getDate());

            GradientDrawable debtBackground = (GradientDrawable) debtHolder.debt.getBackground();
            double debtValue = Double.parseDouble(request.getDebt());
            if (debtValue < 0) {
                String val = Double.toString(-debtValue);
                debtHolder.debt.setText(val);
                debtBackground.setColor(ContextCompat.getColor(mContext, R.color.red));
            } else if (debtValue == 0) {
                debtHolder.debt.setText(AppConfig.DEFAULT_DEBT_VALUE);
                debtBackground.setColor(Color.GRAY);
            } else {
                String val = Double.toString(debtValue);
                debtHolder.debt.setText(val);
                debtBackground.setColor(Color.GREEN);
            }

            debtHolder.debtDate.setText(request.getDebtDate());
        }
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    class FriendRequestViewHolder extends RecyclerView.ViewHolder {

        private final ImageView image;
        private final TextView name;
        private final TextView username;
        private final TextView date;

        FriendRequestViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.request_image);
            name = itemView.findViewById(R.id.user_name);
            username = itemView.findViewById(R.id.user_username);
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

    class DebtRequestViewHolder extends RecyclerView.ViewHolder {

        private final TextView name;
        private final TextView date;
        private final TextView debt;
        private final TextView debtDate;

        DebtRequestViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.user_name);
            date = itemView.findViewById(R.id.request_date);
            debt = itemView.findViewById(R.id.request_debt);
            debtDate = itemView.findViewById(R.id.debt_date);

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

    public void updateList(List<Notification> list) {
        mList = new ArrayList<>(list);
        this.notifyDataSetChanged();
    }

    public void addItemToTop(Notification request) {
        mList.add(0, request);
        this.notifyItemInserted(0);
    }

    public void removeItem(int index) {
        if (index != -1) {
            mList.remove(index);
            this.notifyItemRemoved(index);
        }
    }
}
