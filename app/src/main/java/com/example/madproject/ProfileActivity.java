package com.example.madproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView profileImageView;
    private EditText nameEditText;
    private LinearLayout interestsLayout;
    private Button saveButton;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private StorageReference mStorage;
    private String userId;
    private Uri imageUri;
    private List<CheckBox> interestCheckBoxes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference();

        profileImageView = findViewById(R.id.profileImageView);
        nameEditText = findViewById(R.id.nameEditText);
        interestsLayout = findViewById(R.id.interestsLayout);
        saveButton = findViewById(R.id.saveButton);

        userId = getIntent().getStringExtra("USER_ID");
        if (userId == null) {
            userId = mAuth.getCurrentUser().getUid();
        }
        boolean isOwnProfile = userId.equals(mAuth.getCurrentUser().getUid());

        if (!isOwnProfile) {
            nameEditText.setEnabled(false);
            interestsLayout.setEnabled(false);
            saveButton.setVisibility(View.GONE);
        }

        setupInterestCheckboxes();
        loadProfile();

        saveButton.setOnClickListener(v -> saveProfile());
        profileImageView.setOnClickListener(v -> selectImage());
        Button logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> logout());

        Button homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        Button chatButton = findViewById(R.id.chatButton);
        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, UserListActivity.class);
                startActivity(intent);
            }
        });
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
        finish();
    }

    private void setupInterestCheckboxes() {
        interestCheckBoxes = new ArrayList<>();
        String[] interestsArray = getResources().getStringArray(R.array.interests);
        for (String interest : interestsArray) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(interest);
            checkBox.setTextColor(Color.WHITE);
            interestsLayout.addView(checkBox);
            interestCheckBoxes.add(checkBox);
        }
    }

    private void loadProfile() {
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    nameEditText.setText(user.getName());
                    if (user.getImageUrl() != null && !user.getImageUrl().isEmpty()) {
                        Glide.with(ProfileActivity.this)
                                .load(user.getImageUrl())
                                .into(profileImageView);
                    }
                    List<String> userInterests = user.getInterests();
                    if (userInterests != null) {
                        for (CheckBox checkBox : interestCheckBoxes) {
                            checkBox.setChecked(userInterests.contains(checkBox.getText().toString()));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfile() {
        String name = nameEditText.getText().toString();
        List<String> interests = new ArrayList<>();
        for (CheckBox checkBox : interestCheckBoxes) {
            if (checkBox.isChecked()) {
                interests.add(checkBox.getText().toString());
            }
        }

        if (imageUri != null) {
            uploadImage(name, interests);
        } else {
            updateProfile(name, null, interests);
        }
    }

    private void uploadImage(final String name, final List<String> interests) {
        final StorageReference imageRef = mStorage.child("profile_images").child(userId + ".jpg");
        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            updateProfile(name, imageUrl, interests);
                        })
                        .addOnFailureListener(e -> {
                            Log.e("ProfileActivity", "Failed to get download URL", e);
                            Toast.makeText(ProfileActivity.this, "Failed to get download URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            updateProfile(name, null, interests);
                        }))
                .addOnFailureListener(e -> {
                    Log.e("ProfileActivity", "Failed to upload image", e);
                    Toast.makeText(ProfileActivity.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    updateProfile(name, null, interests);
                });
    }

    private void updateProfile(String name, String imageUrl, List<String> interests) {
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User existingUser = dataSnapshot.getValue(User.class);
                if (existingUser != null) {
                    // Preserve the existing email
                    String email = existingUser.getEmail();

                    // Create a new User object with updated information
                    User updatedUser = new User(userId, name, email, interests, imageUrl);

                    // Update the user in the database
                    mDatabase.child("users").child(userId).setValue(updatedUser)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(ProfileActivity.this, "Profile saved", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, "Failed to save profile", Toast.LENGTH_SHORT).show());
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to load existing user data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ProfileActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                profileImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}