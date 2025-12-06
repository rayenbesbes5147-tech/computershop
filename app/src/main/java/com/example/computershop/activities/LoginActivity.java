package com.example.computershop.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.computershop.R;
import com.example.computershop.utils.FirebaseManager;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

public class LoginActivity extends AppCompatActivity {
    private EditText emailInput;
    private EditText passwordInput;
    private Button loginBtn;
    private TextView registerLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FirebaseManager.init();

        // Check if user is already logged in
        FirebaseUser currentUser = FirebaseManager.getCurrentUser();
        if (currentUser != null) {
            navigateToHome(currentUser.getUid());
            return;
        }

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginBtn = findViewById(R.id.loginBtn);
        registerLink = findViewById(R.id.registerLink);

        loginBtn.setOnClickListener(v -> handleLogin());
        registerLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void handleLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty()) {
            emailInput.setError("Email is required");
            return;
        }

        if (password.isEmpty()) {
            passwordInput.setError("Password is required");
            return;
        }

        loginBtn.setEnabled(false);

        FirebaseManager.loginUser(email, password, task -> {
            loginBtn.setEnabled(true);

            if (task.isSuccessful()) {
                FirebaseUser user = FirebaseManager.getCurrentUser();
                if (user != null) {
                    navigateToHome(user.getUid());
                }
            } else {
                Toast.makeText(LoginActivity.this, "Login failed: " + task.getException().getMessage(), 
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToHome(String userId) {
        // Check if the logged-in email is the admin email
        FirebaseUser currentUser = FirebaseManager.getCurrentUser();
        if (currentUser != null && "admin@gmail.com".equals(currentUser.getEmail())) {
            // Redirect to Admin Portal
            startActivity(new Intent(LoginActivity.this, AdminActivity.class));
            finish();
            return;
        }

        // For regular users, check role from Firestore
        FirebaseManager.getUserFromFirestore(userId, task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String role = document.getString("role");
                    if ("admin".equals(role)) {
                        startActivity(new Intent(LoginActivity.this, AdminDashboardActivity.class));
                    } else {
                        startActivity(new Intent(LoginActivity.this, ProductListActivity.class));
                    }
                    finish();
                }
            }
        });
    }
}
