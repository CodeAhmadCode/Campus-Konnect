package com.example.madproject;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<User> userList;
    private Context context;
    private OnUserClickListener onUserClickListener;

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    public UserAdapter(List<User> userList, Context context, OnUserClickListener listener) {
        this.userList = new ArrayList<>(userList);
        this.context = context;
        this.onUserClickListener = listener;
    }

    public void filterList(List<User> filteredList) {
        this.userList = new ArrayList<>(filteredList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView emailTextView;
        ImageView profileImageView;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            emailTextView = itemView.findViewById(R.id.emailTextView);
            profileImageView = itemView.findViewById(R.id.profileImageView);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onUserClickListener != null) {
                    onUserClickListener.onUserClick(userList.get(position));
                }
            });
        }

        void bind(User user) {
            if (user != null) {
                Log.d("UserAdapter", "Binding user: " + user.getName() + ", ID: " + user.getUserId() + ", Email: " + user.getEmail());
                nameTextView.setText(user.getName() != null ? user.getName() : "");
                emailTextView.setText(user.getEmail() != null ? user.getEmail() : "");

                if (user.getImageUrl() != null && !user.getImageUrl().isEmpty()) {
                    Glide.with(context)
                            .load(user.getImageUrl())
                            .apply(RequestOptions.circleCropTransform())
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile)
                            .into(profileImageView);
                } else {
                    profileImageView.setImageResource(R.drawable.ic_profile);
                }
            } else {
                Log.e("UserAdapter", "Attempted to bind null user");
            }
        }
    }
}