package com.example.madproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EmailVerificationActivity extends AppCompatActivity {

    private TextView messageTextView;
    private Button resendEmailButton, continueButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);

        mAuth = FirebaseAuth.getInstance();

        messageTextView = findViewById(R.id.messageTextView);
        resendEmailButton = findViewById(R.id.resendEmailButton);
        continueButton = findViewById(R.id.continueButton);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            messageTextView.setText("A verification email has been sent to " + user.getEmail());
        }

        resendEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resendVerificationEmail();
            }
        });

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkEmailVerification();
            }
        });
    }

    private void resendVerificationEmail() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(EmailVerificationActivity.this,
                                    "Verification email sent again.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(EmailVerificationActivity.this,
                                    "Failed to send verification email.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void checkEmailVerification() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.reload().addOnCompleteListener(task -> {
                if (user.isEmailVerified()) {
                    // Email is verified, proceed to main activity
                    startActivity(new Intent(EmailVerificationActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(EmailVerificationActivity.this,
                            "Please verify your email before continuing.",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}