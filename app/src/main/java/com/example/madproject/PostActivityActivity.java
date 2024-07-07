package com.example.madproject;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PostActivityActivity extends AppCompatActivity {

    private EditText titleEditText, descriptionEditText, locationEditText, dateTimeEditText;
    private Button postButton;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private Activity editingActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_activity);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        titleEditText = findViewById(R.id.titleEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        locationEditText = findViewById(R.id.locationEditText);
        dateTimeEditText = findViewById(R.id.dateTimeEditText);
        postButton = findViewById(R.id.postButton);

        editingActivity = (Activity) getIntent().getSerializableExtra("EDIT_ACTIVITY");
        if (editingActivity != null) {
            fillFields(editingActivity);
            postButton.setText("Update Activity");
        }

        postButton.setOnClickListener(v -> postActivity());
    }

    private void fillFields(Activity activity) {
        titleEditText.setText(activity.getTitle());
        descriptionEditText.setText(activity.getDescription());
        locationEditText.setText(activity.getLocation());
        dateTimeEditText.setText(formatDateTime(activity.getDateTime()));
    }

    private void postActivity() {
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String location = locationEditText.getText().toString().trim();
        String dateTimeString = dateTimeEditText.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty() || location.isEmpty() || dateTimeString.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        long dateTime;
        try {
            dateTime = parseDateTime(dateTimeString);
        } catch (ParseException e) {
            Toast.makeText(this, "Invalid date/time format. Use dd/MM/yyyy HH:mm", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        String userEmail = mAuth.getCurrentUser().getEmail();

        if (editingActivity != null) {
            // Update existing activity
            editingActivity.setTitle(title);
            editingActivity.setDescription(description);
            editingActivity.setLocation(location);
            editingActivity.setDateTime(dateTime);

            mDatabase.child("activities").child(editingActivity.getId()).setValue(editingActivity)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(PostActivityActivity.this, "Activity updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(PostActivityActivity.this, "Failed to update activity", Toast.LENGTH_SHORT).show());
        } else {
            // Create new activity
            String activityId = mDatabase.child("activities").push().getKey();
            Activity newActivity = new Activity(activityId, title, description, location, dateTime, userEmail, userEmail);

            mDatabase.child("activities").child(activityId).setValue(newActivity)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(PostActivityActivity.this, "Activity posted successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(PostActivityActivity.this, "Failed to post activity", Toast.LENGTH_SHORT).show());
        }
    }

    private long parseDateTime(String dateTimeString) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        Date date = sdf.parse(dateTimeString);
        return date.getTime();
    }

    private String formatDateTime(long dateTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(dateTime));
    }
}