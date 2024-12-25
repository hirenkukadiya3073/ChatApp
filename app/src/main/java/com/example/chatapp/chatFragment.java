package com.example.chatapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

public class chatFragment extends Fragment {

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private FirestoreRecyclerAdapter<firebasemodel, NoteViewHolder> chatAdapter;
    private RecyclerView mrecyclerview;

    private LinearLayoutManager linearLayoutManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.chatfragment, container, false);

        // Initialize Firebase Auth and Firestore
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        // Initialize RecyclerView
        mrecyclerview = v.findViewById(R.id.recyclerview);

        setupRecyclerView();

        return v;
    }

    private void setupRecyclerView() {
        String currentUserId = firebaseAuth.getUid();

        if (currentUserId == null) {
            Log.e("chatFragment", "Current User ID is null. Cannot proceed with Firestore query.");
            Toast.makeText(getContext(), "Error: Unable to fetch user details.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Query Firestore for users excluding the current user
        Query query = firebaseFirestore.collection("Users").whereNotEqualTo("uid", currentUserId);
        FirestoreRecyclerOptions<firebasemodel> allUsers = new FirestoreRecyclerOptions.Builder<firebasemodel>()
                .setQuery(query, firebasemodel.class)
                .build();

        chatAdapter = new FirestoreRecyclerAdapter<firebasemodel, NoteViewHolder>(allUsers) {
            @Override
            protected void onBindViewHolder(@NonNull NoteViewHolder holder, int position, @NonNull firebasemodel model) {
                holder.bind(model);
            }

            @NonNull
            @Override
            public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chatviewlayout, parent, false);
                return new NoteViewHolder(view);
            }
        };

        // Set RecyclerView properties
        mrecyclerview.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mrecyclerview.setLayoutManager(linearLayoutManager);
        mrecyclerview.setAdapter(chatAdapter);
    }

    public class NoteViewHolder extends RecyclerView.ViewHolder {

        private TextView particularUsername;
        private TextView statusOfUser;
        private ImageView imageViewOfUser;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            particularUsername = itemView.findViewById(R.id.nameofuser);
            statusOfUser = itemView.findViewById(R.id.statusofuser);
            imageViewOfUser = itemView.findViewById(R.id.imageviewofuser);
        }

        public void bind(firebasemodel model) {
            // Set user details
            particularUsername.setText(model.getName());
            Picasso.get().load(model.getImage()).placeholder(R.drawable.defaultprofile).into(imageViewOfUser);

            // Handle user status
            if ("Online".equals(model.getStatus())) {
                statusOfUser.setTextColor(Color.GREEN);
            } else {
                statusOfUser.setTextColor(Color.GRAY);
            }
            statusOfUser.setText(model.getStatus());

            // Set click listener for specific chat
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), specificchat.class);
                intent.putExtra("name", model.getName());
                intent.putExtra("receiveruid", model.getUid());
                intent.putExtra("imageuri", model.getImage());
                startActivity(intent);
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (chatAdapter != null) {
            chatAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (chatAdapter != null) {
            chatAdapter.stopListening();
        }
    }
}

