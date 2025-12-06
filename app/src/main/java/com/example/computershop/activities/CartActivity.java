package com.example.computershop.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.computershop.R;
import com.example.computershop.adapters.CartAdapter;
import com.example.computershop.models.CartItem;
import com.example.computershop.models.Product;
import com.example.computershop.utils.FirebaseManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartActivity extends AppCompatActivity {
    private RecyclerView cartRecyclerView;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItems;
    private Map<String, Product> productMap;
    private TextView totalPriceText, subtotalText, shippingText;
    private MaterialButton checkoutBtn;
    private View emptyCartState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        initializeViews();
        setupRecyclerView();
        loadCart();

        checkoutBtn.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(CartActivity.this, "Your cart is empty", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(CartActivity.this, "Order placed successfully!", Toast.LENGTH_SHORT).show();
                // Clear cart after successful order
                clearCart();
            }
        });

        findViewById(R.id.backBtn).setOnClickListener(v -> finish());
    }

    private void initializeViews() {
        cartRecyclerView = findViewById(R.id.cartRecyclerView);
        totalPriceText = findViewById(R.id.totalPriceText);
        subtotalText = findViewById(R.id.subtotalText);
        shippingText = findViewById(R.id.shippingText);
        checkoutBtn = findViewById(R.id.checkoutBtn);
        emptyCartState = findViewById(R.id.emptyCartState);
    }

    private void setupRecyclerView() {
        cartItems = new ArrayList<>();
        productMap = new HashMap<>();

        cartAdapter = new CartAdapter(cartItems, cartItem -> {
            removeFromCart(cartItem.getNumPanier());
        }, productMap);

        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartRecyclerView.setAdapter(cartAdapter);
    }

    private void loadCart() {
        String userId = FirebaseManager.getCurrentUser().getUid();
        FirebaseManager.getCartItems(userId, task -> {
            if (task.isSuccessful()) {
                com.google.firebase.firestore.QuerySnapshot snapshot = task.getResult();
                cartItems.clear();
                productMap.clear();

                if (snapshot != null && !snapshot.isEmpty()) {
                    for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
                        CartItem cartItem = doc.toObject(CartItem.class);
                        if (cartItem != null) {
                            cartItem.setNumPanier(doc.getId());
                            cartItems.add(cartItem);
                            loadProductDetails(cartItem.getIdArt());
                        }
                    }
                    showCartItems();
                } else {
                    showEmptyCart();
                }
                cartAdapter.notifyDataSetChanged();
                calculateTotal();
            } else {
                showEmptyCart();
            }
        });
    }

    private void loadProductDetails(String productId) {
        FirebaseManager.getProductById(productId, task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Product product = task.getResult().toObject(Product.class);
                if (product != null) {
                    productMap.put(productId, product);
                    cartAdapter.notifyDataSetChanged();
                    calculateTotal();
                }
            }
        });
    }

    private void removeFromCart(String cartItemId) {
        FirebaseManager.removeFromCart(cartItemId, task -> {
            if (task.isSuccessful()) {
                Toast.makeText(CartActivity.this, "Item removed from cart", Toast.LENGTH_SHORT).show();
                loadCart();
            } else {
                Toast.makeText(CartActivity.this, "Failed to remove item", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearCart() {
        // Remove all cart items
        for (CartItem item : cartItems) {
            FirebaseManager.removeFromCart(item.getNumPanier(), task -> {
                // Individual removal callbacks
            });
        }
        cartItems.clear();
        productMap.clear();
        cartAdapter.notifyDataSetChanged();
        showEmptyCart();
        calculateTotal();
    }

    private void calculateTotal() {
        double subtotal = 0;
        for (CartItem item : cartItems) {
            if (productMap.containsKey(item.getIdArt())) {
                Product product = productMap.get(item.getIdArt());
                subtotal += product.getPrixArt() * item.getQuantité();
            }
        }

        subtotalText.setText(String.format("$%.2f", subtotal));
        totalPriceText.setText(String.format("$%.2f", subtotal)); // Free shipping

        // Update checkout button text with item count
        int totalItems = cartItems.size();
        checkoutBtn.setText(totalItems > 0 ?
                String.format("Checkout (%d items)", totalItems) : "Proceed to Checkout");
    }

    private void showCartItems() {
        cartRecyclerView.setVisibility(View.VISIBLE);
        emptyCartState.setVisibility(View.GONE);
    }

    private void showEmptyCart() {
        cartRecyclerView.setVisibility(View.GONE);
        emptyCartState.setVisibility(View.VISIBLE);
        totalPriceText.setText("$0.00");
        subtotalText.setText("$0.00");
    }
}