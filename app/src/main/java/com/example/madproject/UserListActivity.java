package com.example.madproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SearchView searchView;
    private UserAdapter userAdapter;
    private List<User> userList;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        recyclerView = findViewById(R.id.userRecyclerView);
        searchView = findViewById(R.id.searchView);
        userList = new ArrayList<>();

        userAdapter = new UserAdapter(userList, this, user -> {
            if (user != null) {
                Intent intent = new Intent(UserListActivity.this, ChatActivity.class);
                intent.putExtra("userId", user.getUserId());
                intent.putExtra("userName", user.getName());
                startActivity(intent);
            } else {
                Log.e("UserListActivity", "Clicked user is null");
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(userAdapter);

        loadUsers();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterUsers(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterUsers(newText);
                return true;
            }
        });

        Button profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(v -> openProfile());


        Button homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserListActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void filterUsers(String query) {
        if (query == null || query.isEmpty()) {
            userAdapter.filterList(userList);
            return;
        }

        List<User> filteredList = new ArrayList<>();
        for (User user : userList) {
            if (user.getName() != null && user.getName().toLowerCase().contains(query.toLowerCase()) ||
                    user.getEmail() != null && user.getEmail().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(user);
            }
        }
        userAdapter.filterList(filteredList);
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
    private void loadUsers() {
        mDatabase.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null && user.getUserId() != null && !user.getUserId().equals(mAuth.getCurrentUser().getUid())) {
                        userList.add(user);
                        Log.d("UserListActivity", "Loaded user: " + user.getName());
                    }
                }
                Log.d("UserListActivity", "Total users loaded: " + userList.size());
                userAdapter.filterList(userList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("UserListActivity", "Error loading users: " + databaseError.getMessage());
            }
        });
    }
}