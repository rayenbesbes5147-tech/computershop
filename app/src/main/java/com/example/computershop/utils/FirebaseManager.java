package com.example.computershop.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.computershop.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class FirebaseManager {
    private static final String TAG = "FirebaseManager";
    private static FirebaseAuth mAuth;
    private static FirebaseFirestore db;

    // Add FirebaseCallback interface
    public interface FirebaseCallback {
        void onSuccess(Object result);
        void onFailure(String error);
    }

    public static void init() {
        try {
            if (mAuth == null) mAuth = FirebaseAuth.getInstance();
            if (db == null) db = FirebaseFirestore.getInstance();
            Log.d(TAG, "FirebaseManager init OK");
        } catch (Exception e) {
            Log.e(TAG, "init error: " + e.getMessage());
        }
    }

    public static FirebaseAuth getAuth() {
        if (mAuth == null) init();
        return mAuth;
    }

    // alias name for backward compatibility
    public static FirebaseFirestore getDatabase() {
        if (db == null) init();
        return db;
    }

    // clearer name
    public static FirebaseFirestore getFirestore() {
        return getDatabase();
    }

    public static FirebaseUser getCurrentUser() {
        return getAuth().getCurrentUser();
    }

    // Add this method to get current user ID
    public static String getCurrentUserId() {
        FirebaseUser user = getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public static void registerUser(String email, String password, String login, String pays, OnCompleteListener<AuthResult> listener) {
        try {
            getAuth().createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = getAuth().getCurrentUser();
                            if (firebaseUser != null) {
                                User user = new User(firebaseUser.getUid(), login, email, System.currentTimeMillis(), pays, "user");
                                saveUserToFirestore(user, task, listener);
                            } else {
                                listener.onComplete(task);
                            }
                        } else {
                            listener.onComplete(task);
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "registerUser exception: " + e.getMessage());
        }
    }

    public static void loginUser(String email, String password, OnCompleteListener<AuthResult> listener) {
        getAuth().signInWithEmailAndPassword(email, password).addOnCompleteListener(listener);
    }

    public static void logoutUser() {
        if (mAuth != null) mAuth.signOut();
    }

    private static void saveUserToFirestore(User user, Task<AuthResult> authTask, OnCompleteListener<AuthResult> listener) {
        if (db == null) init();
        db.collection("Internaute").document(user.getIdInt())
                .set(user)
                .addOnSuccessListener(aVoid -> listener.onComplete(authTask))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "saveUserToFirestore failed: " + e.getMessage());
                    listener.onComplete(authTask);
                });
    }

    public static void getUserFromFirestore(String userId, OnCompleteListener<DocumentSnapshot> listener) {
        if (db == null) init();
        db.collection("Internaute").document(userId).get().addOnCompleteListener(listener);
    }

    // Products
    public static void saveProduct(com.example.computershop.models.Product product, OnCompleteListener<Void> listener) {
        if (db == null) init();
        if (product.getIdArt() == null || product.getIdArt().isEmpty()) {
            product.setIdArt(db.collection("Article").document().getId());
        }
        db.collection("Article").document(product.getIdArt()).set(product).addOnCompleteListener(listener);
    }

    public static void deleteProduct(String productId, OnCompleteListener<Void> listener) {
        if (db == null) init();
        db.collection("Article").document(productId).delete().addOnCompleteListener(listener);
    }

    public static void getProducts(OnCompleteListener<QuerySnapshot> listener) {
        if (db == null) init();
        db.collection("Article").get().addOnCompleteListener(listener);
    }

    // Add this new method for the callback interface
    public static void getProducts(FirebaseCallback callback) {
        if (db == null) init();
        db.collection("Article").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(task.getResult());
                    } else {
                        callback.onFailure(task.getException() != null ? task.getException().getMessage() : "Unknown error");
                    }
                });
    }

    public static void getProductById(String productId, OnCompleteListener<DocumentSnapshot> listener) {
        if (db == null) init();
        db.collection("Article").document(productId).get().addOnCompleteListener(listener);
    }

    // Cart - Update these methods to use the new callback interface
    public static void addToCart(com.example.computershop.models.CartItem cartItem, OnCompleteListener<Void> listener) {
        if (db == null) init();
        if (cartItem.getNumPanier() == null || cartItem.getNumPanier().isEmpty()) {
            cartItem.setNumPanier(db.collection("Panier").document().getId());
        }
        db.collection("Panier").document(cartItem.getNumPanier()).set(cartItem).addOnCompleteListener(listener);
    }

    // Add new method with FirebaseCallback
    public static void addToCart(com.example.computershop.models.CartItem cartItem, FirebaseCallback callback) {
        if (db == null) init();
        if (cartItem.getNumPanier() == null || cartItem.getNumPanier().isEmpty()) {
            cartItem.setNumPanier(db.collection("Panier").document().getId());
        }
        db.collection("Panier").document(cartItem.getNumPanier()).set(cartItem)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(null);
                    } else {
                        callback.onFailure(task.getException() != null ? task.getException().getMessage() : "Unknown error");
                    }
                });
    }

    // ADD THIS METHOD - Update cart item
    public static void updateCartItem(com.example.computershop.models.CartItem cartItem, OnCompleteListener<Void> listener) {
        if (db == null) init();
        db.collection("Panier").document(cartItem.getNumPanier()).set(cartItem).addOnCompleteListener(listener);
    }

    // ADD THIS METHOD - Update cart item with FirebaseCallback
    public static void updateCartItem(com.example.computershop.models.CartItem cartItem, FirebaseCallback callback) {
        if (db == null) init();
        db.collection("Panier").document(cartItem.getNumPanier()).set(cartItem)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(null);
                    } else {
                        callback.onFailure(task.getException() != null ? task.getException().getMessage() : "Unknown error");
                    }
                });
    }

    public static void getCartItems(String userId, OnCompleteListener<QuerySnapshot> listener) {
        if (db == null) init();
        db.collection("Panier").whereEqualTo("idInt", userId).get().addOnCompleteListener(listener);
    }

    // Add new method with FirebaseCallback
    public static void getCartItems(String userId, FirebaseCallback callback) {
        if (db == null) init();
        db.collection("Panier").whereEqualTo("idInt", userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<com.example.computershop.models.CartItem> cartItems = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            com.example.computershop.models.CartItem cartItem = document.toObject(com.example.computershop.models.CartItem.class);
                            if (cartItem != null) {
                                cartItem.setNumPanier(document.getId());
                                cartItems.add(cartItem);
                            }
                        }
                        callback.onSuccess(cartItems);
                    } else {
                        callback.onFailure(task.getException() != null ? task.getException().getMessage() : "Unknown error");
                    }
                });
    }

    public static void removeFromCart(String cartItemId, OnCompleteListener<Void> listener) {
        if (db == null) init();
        db.collection("Panier").document(cartItemId).delete().addOnCompleteListener(listener);
    }

    // Add new method with FirebaseCallback
    public static void removeFromCart(String cartItemId, FirebaseCallback callback) {
        if (db == null) init();
        db.collection("Panier").document(cartItemId).delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(null);
                    } else {
                        callback.onFailure(task.getException() != null ? task.getException().getMessage() : "Unknown error");
                    }
                });
    }

    // Users admin
    public static void getAllUsers(OnCompleteListener<QuerySnapshot> listener) {
        if (db == null) init();
        db.collection("Internaute").get().addOnCompleteListener(listener);
    }

    public static void updateUser(User user, OnCompleteListener<Void> listener) {
        if (db == null) init();
        db.collection("Internaute").document(user.getIdInt()).set(user).addOnCompleteListener(listener);
    }

    public static void deleteUser(String userId, OnCompleteListener<Void> listener) {
        if (db == null) init();
        db.collection("Internaute").document(userId).delete().addOnCompleteListener(listener);
    }
}