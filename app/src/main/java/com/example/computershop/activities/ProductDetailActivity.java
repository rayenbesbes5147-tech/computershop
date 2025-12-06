package com.example.computershop.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.computershop.R;
import com.example.computershop.models.CartItem;
import com.example.computershop.models.Product;
import com.example.computershop.utils.FirebaseManager;
import com.google.android.material.button.MaterialButton;

public class ProductDetailActivity extends AppCompatActivity {
    private static final String TAG = "ProductDetailActivity";

    private ImageView productImage;
    private TextView productName, productPrice, productDescription, stockText, categoryChip, quantityText;
    private ImageButton backBtn, decreaseBtn, increaseBtn;
    private Spinner packagingSpinner;
    private MaterialButton addToCartBtn;
    private Product currentProduct;
    private int currentQuantity = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        initializeViews();
        setupPackagingSpinner(); // Add this line
        setupClickListeners();

        currentProduct = (Product) getIntent().getSerializableExtra("product");
        if (currentProduct != null) {
            displayProductDetails();
        } else {
            Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        productImage = findViewById(R.id.productImage);
        productName = findViewById(R.id.productName);
        productPrice = findViewById(R.id.productPrice);
        productDescription = findViewById(R.id.productDescription);
        stockText = findViewById(R.id.stockText);
        categoryChip = findViewById(R.id.categoryChip);
        quantityText = findViewById(R.id.quantityText);
        backBtn = findViewById(R.id.backBtn);
        decreaseBtn = findViewById(R.id.decreaseBtn);
        increaseBtn = findViewById(R.id.increaseBtn);
        packagingSpinner = findViewById(R.id.packagingSpinner);
        addToCartBtn = findViewById(R.id.addToCartBtn);
    }

    private void setupPackagingSpinner() {
        // Use XML string array
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.packaging_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        packagingSpinner.setAdapter(adapter);
    }
    private void setupClickListeners() {
        backBtn.setOnClickListener(v -> finish());

        decreaseBtn.setOnClickListener(v -> {
            if (currentQuantity > 1) {
                currentQuantity--;
                quantityText.setText(String.valueOf(currentQuantity));
            }
        });

        increaseBtn.setOnClickListener(v -> {
            if (currentProduct != null && currentQuantity < currentProduct.getStock()) {
                currentQuantity++;
                quantityText.setText(String.valueOf(currentQuantity));
            } else {
                Toast.makeText(this, "Cannot exceed available stock", Toast.LENGTH_SHORT).show();
            }
        });

        addToCartBtn.setOnClickListener(v -> handleAddToCart());
    }

    private void displayProductDetails() {
        // Load product image
        if (currentProduct.getImageUrl() != null && !currentProduct.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(currentProduct.getImageUrl())
                    .placeholder(R.drawable.image_placeholder)
                    .error(R.drawable.image_placeholder)
                    .into(productImage);
        }

        // Set product information
        productName.setText(currentProduct.getLibArt());
        productPrice.setText(String.format("%.2f DT", currentProduct.getPrixArt()));

        if (currentProduct.getDescription() != null && !currentProduct.getDescription().isEmpty()) {
            productDescription.setText(currentProduct.getDescription());
        } else {
            productDescription.setText("No description available");
        }

        // Set category and stock
        categoryChip.setText(currentProduct.getCatArt());
        stockText.setText(String.format("In Stock: %d", currentProduct.getStock()));

        // Update stock color based on availability
        if (currentProduct.getStock() == 0) {
            stockText.setText("Out of Stock");
            stockText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            addToCartBtn.setEnabled(false);
            addToCartBtn.setAlpha(0.5f);
        } else if (currentProduct.getStock() <= 5) {
            stockText.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        }
    }

    private void handleAddToCart() {
        Log.d(TAG, "Add to cart button clicked");

        try {
            // Check if user is logged in
            if (FirebaseManager.getCurrentUser() == null) {
                Toast.makeText(this, "Please login to add items to cart", Toast.LENGTH_SHORT).show();
                return;
            }

            String currentUserId = FirebaseManager.getCurrentUser().getUid();
            Log.d(TAG, "User ID: " + currentUserId);

            if (currentProduct == null) {
                Toast.makeText(this, "Product information is missing", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if spinner has items and get selected item safely
            if (packagingSpinner.getAdapter() == null || packagingSpinner.getAdapter().getCount() == 0) {
                Toast.makeText(this, "Packaging options not loaded", Toast.LENGTH_SHORT).show();
                return;
            }

            String packaging = packagingSpinner.getSelectedItem().toString();
            Log.d(TAG, "Packaging: " + packaging + ", Quantity: " + currentQuantity);

            // Validate quantity
            if (currentQuantity <= 0) {
                Toast.makeText(this, "Please select a valid quantity", Toast.LENGTH_SHORT).show();
                return;
            }

            if (currentQuantity > currentProduct.getStock()) {
                Toast.makeText(this, "Quantity exceeds available stock", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create cart item
            CartItem cartItem = new CartItem(
                    null,
                    currentProduct.getIdArt(),
                    currentUserId,
                    currentQuantity,
                    packaging,
                    System.currentTimeMillis()
            );

            Log.d(TAG, "CartItem created for product: " + currentProduct.getIdArt());

            // Show loading state
            addToCartBtn.setEnabled(false);
            addToCartBtn.setText("Adding...");

            // Use the OnCompleteListener version
            FirebaseManager.addToCart(cartItem, task -> {
                runOnUiThread(() -> {
                    addToCartBtn.setEnabled(true);
                    addToCartBtn.setText("Add to Cart");

                    if (task.isSuccessful()) {
                        Toast.makeText(ProductDetailActivity.this,
                                currentProduct.getLibArt() + " added to cart!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Cart item added successfully");
                        finish();
                    } else {
                        String error = task.getException() != null ?
                                task.getException().getMessage() : "Unknown error";
                        Toast.makeText(ProductDetailActivity.this,
                                "Failed to add to cart: " + error, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Failed to add to cart: " + error);
                    }
                });
            });

        } catch (Exception e) {
            Log.e(TAG, "Exception in handleAddToCart: " + e.getMessage(), e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            addToCartBtn.setEnabled(true);
            addToCartBtn.setText("Add to Cart");
        }
    }
}