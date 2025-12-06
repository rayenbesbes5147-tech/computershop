package com.example.computershop.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.computershop.R;
import com.example.computershop.utils.FirebaseManager;
import com.google.firebase.auth.FirebaseUser;

public class AdminActivity extends AppCompatActivity {
    private TextView adminEmailTv;
    private Button viewDashboardBtn;
    private Button logoutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        adminEmailTv = findViewById(R.id.adminEmailTv);
        viewDashboardBtn = findViewById(R.id.viewDashboardBtn);
        logoutBtn = findViewById(R.id.logoutBtn);

        // Get and display current admin email
        FirebaseUser currentUser = FirebaseManager.getCurrentUser();
        if (currentUser != null) {
            adminEmailTv.setText("Logged in as: " + currentUser.getEmail());
        }

        // Navigate to Admin Dashboard (full admin functionality)
        viewDashboardBtn.setOnClickListener(v -> {
            startActivity(new Intent(AdminActivity.this, AdminDashboardActivity.class));
        });

        // Logout
        logoutBtn.setOnClickListener(v -> {
            FirebaseManager.logoutUser();
            Toast.makeText(AdminActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AdminActivity.this, LoginActivity.class));
            finish();
        });
    }
}
