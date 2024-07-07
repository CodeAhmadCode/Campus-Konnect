package com.example.madproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder> {

    private List<Activity> activities;
    private String currentUserId;
    private OnInterestClickListener interestListener;
    private OnLongClickListener longClickListener;

    public ActivityAdapter(List<Activity> activities, String currentUserId, OnInterestClickListener interestListener, OnLongClickListener longClickListener) {
        this.activities = activities;
        this.currentUserId = currentUserId;
        this.interestListener = interestListener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activity, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        holder.bind(activities.get(position));
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    class ActivityViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, descriptionTextView, locationTextView, dateTimeTextView, creatorTextView, interestedCountTextView;
        Button interestButton;

        ActivityViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            locationTextView = itemView.findViewById(R.id.locationTextView);
            dateTimeTextView = itemView.findViewById(R.id.dateTimeTextView);
            creatorTextView = itemView.findViewById(R.id.creatorTextView);
            interestedCountTextView = itemView.findViewById(R.id.interestedCountTextView);
            interestButton = itemView.findViewById(R.id.interestButton);

            itemView.setOnLongClickListener(v -> {
                longClickListener.onLongClick(activities.get(getAdapterPosition()));
                return true;
            });
        }

        void bind(Activity activity) {
            titleTextView.setText(activity.getTitle());
            descriptionTextView.setText(activity.getDescription());
            locationTextView.setText(activity.getLocation());
            dateTimeTextView.setText(formatDateTime(activity.getDateTime()));
            creatorTextView.setText("Posted by: " + (activity.getCreatorName() != null ? activity.getCreatorName() : "Unknown"));
            interestedCountTextView.setText(activity.getInterestedCount() + " interested");

            if (activity.isUserInterested(currentUserId)) {
                interestButton.setText("Not Interested");
            } else {
                interestButton.setText("Interested");
            }

            interestButton.setOnClickListener(v -> interestListener.onInterestClick(activity));
        }

        private String formatDateTime(long dateTime) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            return sdf.format(new Date(dateTime));
        }
    }

    public interface OnInterestClickListener {
        void onInterestClick(Activity activity);
    }

    public interface OnLongClickListener {
        void onLongClick(Activity activity);
    }
}