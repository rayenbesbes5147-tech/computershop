package com.example.computershop.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.computershop.R;
import com.example.computershop.adapters.ProductAdapter;
import com.example.computershop.models.CartItem;
import com.example.computershop.models.Product;
import com.example.computershop.utils.FirebaseManager;

import java.util.ArrayList;
import java.util.List;

public class ProductListActivity extends AppCompatActivity {
    private RecyclerView productsRecyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private List<Product> productListFull;
    private SearchView searchView;
    private ImageButton cartBtn;
    private ImageButton logoutBtn;
    private TextView cartBadge;
    private String currentUserId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        // Get current user ID
        currentUserId = FirebaseManager.getCurrentUserId();

        // Initialize views
        productsRecyclerView = findViewById(R.id.productsRecyclerView);
        searchView = findViewById(R.id.searchView);
        cartBtn = findViewById(R.id.cartBtn);
        logoutBtn = findViewById(R.id.logoutBtn);
        cartBadge = findViewById(R.id.cartBadge);

        // Initialize product lists
        productList = new ArrayList<>();
        productListFull = new ArrayList<>();

        // Initialize adapter with cart functionality
        productAdapter = new ProductAdapter(productList,
                new ProductAdapter.OnProductClickListener() {
                    @Override
                    public void onProductClick(Product product) {
                        // Navigate to product detail
                        Intent intent = new Intent(ProductListActivity.this, ProductDetailActivity.class);
                        intent.putExtra("product", product);
                        startActivity(intent);
                    }
                },
                currentUserId,
                new ProductAdapter.OnCartUpdateListener() {
                    @Override
                    public void onCartUpdated() {
                        updateCartBadge();
                    }
                });

        // Setup RecyclerView
        productsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        productsRecyclerView.setAdapter(productAdapter);

        // Load products and cart data
        loadProductsFromFirestore();
        updateCartBadge();

        // Search functionality
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterLocal(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterLocal(newText);
                return true;
            }
        });

        // Cart button click
        cartBtn.setOnClickListener(v -> {
            if (currentUserId != null) {
                startActivity(new Intent(ProductListActivity.this, CartActivity.class));
            } else {
                Toast.makeText(ProductListActivity.this, "Please login to view cart", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(ProductListActivity.this, LoginActivity.class));
            }
        });

        // Logout button click
        logoutBtn.setOnClickListener(v -> {
            FirebaseManager.logoutUser();
            startActivity(new Intent(ProductListActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void loadProductsFromFirestore() {
        FirebaseManager.getProducts(new FirebaseManager.FirebaseCallback() {
            @Override
            public void onSuccess(Object result) {
                com.google.firebase.firestore.QuerySnapshot snapshot = (com.google.firebase.firestore.QuerySnapshot) result;
                productList.clear();
                productListFull.clear();

                if (snapshot != null && !snapshot.isEmpty()) {
                    for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
                        Product product = doc.toObject(Product.class);
                        if (product != null) {
                            product.setIdArt(doc.getId());
                            productList.add(product);
                            productListFull.add(product);
                        }
                    }
                }
                productAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(ProductListActivity.this, "Failed to load products: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCartBadge() {
        if (currentUserId == null) {
            cartBadge.setVisibility(TextView.GONE);
            return;
        }

        FirebaseManager.getCartItems(currentUserId, new FirebaseManager.FirebaseCallback() {
            @Override
            public void onSuccess(Object result) {
                List<CartItem> cartItems = (List<CartItem>) result;
                runOnUiThread(() -> {
                    if (cartItems != null && !cartItems.isEmpty()) {
                        int totalItems = 0;
                        for (CartItem item : cartItems) {
                            totalItems += item.getQuantité();
                        }
                        cartBadge.setText(String.valueOf(totalItems));
                        cartBadge.setVisibility(TextView.VISIBLE);
                    } else {
                        cartBadge.setVisibility(TextView.GONE);
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    cartBadge.setVisibility(TextView.GONE);
                });
            }
        });
    }

    private void filterLocal(String query) {
        if (query == null) query = "";
        String q = query.trim().toLowerCase();
        productList.clear();

        if (q.isEmpty()) {
            productList.addAll(productListFull);
        } else {
            for (Product p : productListFull) {
                boolean matchName = p.getLibArt() != null && p.getLibArt().toLowerCase().contains(q);
                boolean matchCat = p.getCatArt() != null && p.getCatArt().toLowerCase().contains(q);
                boolean matchPrice = String.valueOf(p.getPrixArt()).contains(q);
                if (matchName || matchCat || matchPrice) {
                    productList.add(p);
                }
            }
        }
        productAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh cart badge when returning to this activity
        updateCartBadge();
    }
}