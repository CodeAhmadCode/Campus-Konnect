package com.example.madproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";

    private RecyclerView messageRecyclerView;
    private EditText messageEditText;
    private Button sendButton;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
User user;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String currentUserId, chatUserId, chatUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Log.d("ChatActivity", "onCreate called");
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        chatUserId = getIntent().getStringExtra("userId");
        chatUserName = getIntent().getStringExtra("userName");
        if (chatUserId == null || chatUserName == null) {
            Toast.makeText(this, "Error: User information not provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Log.d("ChatActivity", "chatUserId: " + chatUserId + ", chatUserName: " + chatUserName);

        if (chatUserId == null || chatUserName == null) {
            Log.e("ChatActivity", "Error: User information not provided");
            Toast.makeText(this, "Error: User information not provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        setTitle(chatUserName);

        mDatabase = FirebaseDatabase.getInstance().getReference("messages");

        messageRecyclerView = findViewById(R.id.messageRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList, currentUserId);
        messageRecyclerView.setAdapter(messageAdapter);
        messageRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        sendButton.setOnClickListener(v -> sendMessage());

        loadMessages();
    }

    private void loadMessages() {
        String chatRoomId = getChatRoomId(currentUserId, chatUserId);
        mDatabase.child(chatRoomId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Message message = dataSnapshot.getValue(Message.class);
                if (message != null) {
                    messageList.add(message);
                    messageAdapter.notifyItemInserted(messageList.size() - 1);
                    messageRecyclerView.scrollToPosition(messageList.size() - 1);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading messages: " + databaseError.getMessage());
            }
        });
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();
        if (!messageText.isEmpty()) {
            String chatRoomId = getChatRoomId(currentUserId, chatUserId);
            String messageId = mDatabase.child(chatRoomId).push().getKey();
            if (messageId != null) {
                Message message = new Message(messageId, currentUserId, chatUserId, messageText, System.currentTimeMillis());
                mDatabase.child(chatRoomId).child(messageId).setValue(message)
                        .addOnSuccessListener(aVoid -> {
                            messageEditText.setText("");
                            Log.d(TAG, "Message sent successfully");
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error sending message: " + e.getMessage());
                            Toast.makeText(ChatActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                        });
            }
        }
    }

    private String getChatRoomId(String userId1, String userId2) {
        return userId1.compareTo(userId2) < 0 ? userId1 + "_" + userId2 : userId2 + "_" + userId1;
    }
}