package com.example.madproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ActivityAdapter adapter;
    private List<Activity> activityList;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private Spinner sortSpinner;
    private ImageButton sortIcon;
    private int currentSortOption = 0; // 0 for Newest, 1 for Oldest, 2 for Most Interested

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Check if a user is logged in
        if (mAuth.getCurrentUser() == null) {
            startLoginActivity();
            return;
        }

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        activityList = new ArrayList<>();
        adapter = new ActivityAdapter(activityList, mAuth.getCurrentUser().getEmail(), this::onInterestClick, this::onLongClick);
        recyclerView.setAdapter(adapter);

        Button chatButton = findViewById(R.id.chatButton);
        chatButton.setOnClickListener(v -> openUserList());

        FloatingActionButton fab = findViewById(R.id.addButton);
        fab.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, PostActivityActivity.class)));

        loadActivities();

        Button profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(v -> openProfile());
    }

    private void startLoginActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void loadActivities() {
        mDatabase.child("activities").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                activityList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        Activity activity = snapshot.getValue(Activity.class);
                        if (activity != null) {
                            activityList.add(activity);
                        }
                    } catch (Exception e) {
                        Log.e("MainActivity", "Error parsing activity: " + e.getMessage());
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("MainActivity", "loadActivities:onCancelled", databaseError.toException());
            }
        });
    }

    private void openUserList() {
        if (mAuth.getCurrentUser() != null) {
            Intent intent = new Intent(this, UserListActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
        }
    }

    private void onInterestClick(Activity activity) {
        String userEmail = mAuth.getCurrentUser().getEmail();
        if (userEmail != null && !userEmail.equals(activity.getCreatorId())) {
            DatabaseReference activityRef = mDatabase.child("activities").child(activity.getId());

            if (activity.isUserInterested(userEmail)) {
                activity.removeInterestedUser(userEmail);
            } else {
                activity.addInterestedUser(userEmail);
            }

            activityRef.setValue(activity)
                    .addOnSuccessListener(aVoid -> adapter.notifyDataSetChanged())
                    .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Failed to update interest", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "You can't show interest in your own activity", Toast.LENGTH_SHORT).show();
        }
    }

    private void onLongClick(Activity activity) {
        if (activity.getCreatorId().equals(mAuth.getCurrentUser().getEmail())) {
            showEditDeleteDialog(activity);
        }
    }

    private void showEditDeleteDialog(Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose an action")
                .setItems(new CharSequence[]{"Edit", "Delete"}, (dialog, which) -> {
                    if (which == 0) {
                        // Edit option
                        Intent intent = new Intent(MainActivity.this, PostActivityActivity.class);
                        intent.putExtra("EDIT_ACTIVITY", activity);
                        startActivity(intent);
                    } else {
                        // Delete option
                        mDatabase.child("activities").child(activity.getId()).removeValue()
                                .addOnSuccessListener(aVoid -> Toast.makeText(MainActivity.this, "Activity deleted", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Failed to delete activity", Toast.LENGTH_SHORT).show());
                    }
                });
        builder.create().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            mAuth.signOut();
            startLoginActivity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openProfile() {
        Log.d("MainActivity", "openProfile method called");
        try {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            Log.d("MainActivity", "FirebaseAuth instance obtained");

            if (auth.getCurrentUser() != null) {
                String userId = auth.getCurrentUser().getUid();
                Log.d("MainActivity", "User ID: " + userId);

                Intent intent = new Intent(this, ProfileActivity.class);
                intent.putExtra("USER_ID", userId);
                Log.d("MainActivity", "Intent created with USER_ID");

                startActivity(intent);
                Log.d("MainActivity", "startActivity called");
            } else {
                Log.d("MainActivity", "Current user is null");
                Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error in openProfile: ", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
