package com.example.computershop.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.computershop.R;
import com.example.computershop.models.CartItem;
import com.example.computershop.models.Product;
import com.example.computershop.utils.FirebaseManager;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
    private List<Product> products;
    private OnProductClickListener listener;
    private Context context;
    private String currentUserId;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public interface OnCartUpdateListener {
        void onCartUpdated();
    }

    private OnCartUpdateListener cartUpdateListener;

    public ProductAdapter(List<Product> products, OnProductClickListener listener, String currentUserId, OnCartUpdateListener cartUpdateListener) {
        this.products = products;
        this.listener = listener;
        this.currentUserId = currentUserId;
        this.cartUpdateListener = cartUpdateListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);

        // Safety check
        if (product != null) {
            holder.productName.setText(product.getLibArt());
            holder.productPrice.setText(String.format("%.2f DT", product.getPrixArt()));

            if (holder.productCategory != null && product.getCatArt() != null) {
                holder.productCategory.setText(product.getCatArt());
            }

            // Load product image
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(product.getImageUrl())
                        .placeholder(R.drawable.image_placeholder)
                        .error(R.drawable.image_placeholder)
                        .into(holder.productImage);
            } else {
                holder.productImage.setImageResource(R.drawable.image_placeholder);
            }

            // Show stock indicator
            if (holder.stockIndicator != null) {
                if (product.getStock() <= 5 && product.getStock() > 0) {
                    holder.stockIndicator.setText("Low Stock");
                    holder.stockIndicator.setVisibility(View.VISIBLE);
                } else if (product.getStock() == 0) {
                    holder.stockIndicator.setText("Out of Stock");
                    holder.stockIndicator.setVisibility(View.VISIBLE);
                    holder.addToCartBtn.setEnabled(false);
                    holder.addToCartBtn.setAlpha(0.5f);
                } else {
                    holder.stockIndicator.setVisibility(View.GONE);
                }
            }
        }

        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProductClick(product);
            }
        });

        // Add to cart button click
        holder.addToCartBtn.setOnClickListener(v -> {
            if (product != null && product.getStock() > 0) {
                addProductToCart(product);
            } else {
                Toast.makeText(context, "Product is out of stock", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addProductToCart(Product product) {
        String currentUserId = FirebaseManager.getCurrentUserId();
        if (currentUserId == null) {
            Toast.makeText(context, "Please login to add items to cart", Toast.LENGTH_SHORT).show();
            return;
        }

        // First check if product already exists in cart
        FirebaseManager.getCartItems(currentUserId, new FirebaseManager.FirebaseCallback() {
            @Override
            public void onSuccess(Object result) {
                List<CartItem> cartItems = (List<CartItem>) result;
                CartItem existingItem = null;

                // Find if product already in cart
                for (CartItem item : cartItems) {
                    if (item.getIdArt().equals(product.getIdArt())) {
                        existingItem = item;
                        break;
                    }
                }

                if (existingItem != null) {
                    // Update existing item quantity
                    int newQuantity = existingItem.getQuantité() + 1; // Always add 1 from product list
                    existingItem.setQuantité(newQuantity);

                    FirebaseManager.updateCartItem(existingItem, new FirebaseManager.FirebaseCallback() {
                        @Override
                        public void onSuccess(Object result) {
                            runOnUiThread(() -> {
                                Toast.makeText(context, "Quantity updated in cart!", Toast.LENGTH_SHORT).show();
                                if (cartUpdateListener != null) {
                                    cartUpdateListener.onCartUpdated();
                                }
                            });
                        }

                        @Override
                        public void onFailure(String error) {
                            runOnUiThread(() -> {
                                Toast.makeText(context, "Failed to update cart: " + error, Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                } else {
                    // Create new cart item
                    String cartNumber = "CART_" + currentUserId + "_" + System.currentTimeMillis();

                    CartItem cartItem = new CartItem(
                            cartNumber,
                            product.getIdArt(),
                            currentUserId,
                            1, // Default quantity is 1
                            "Standard",
                            System.currentTimeMillis()
                    );

                    FirebaseManager.addToCart(cartItem, new FirebaseManager.FirebaseCallback() {
                        @Override
                        public void onSuccess(Object result) {
                            runOnUiThread(() -> {
                                Toast.makeText(context, product.getLibArt() + " added to cart!", Toast.LENGTH_SHORT).show();
                                if (cartUpdateListener != null) {
                                    cartUpdateListener.onCartUpdated();
                                }
                            });
                        }

                        @Override
                        public void onFailure(String error) {
                            runOnUiThread(() -> {
                                Toast.makeText(context, "Failed to add to cart: " + error, Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                }
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(context, "Failed to check cart: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // Helper method to run on UI thread
    private void runOnUiThread(Runnable action) {
        if (context instanceof android.app.Activity) {
            ((android.app.Activity) context).runOnUiThread(action);
        }
    }

    @Override
    public int getItemCount() {
        return products != null ? products.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView productName;
        TextView productPrice;
        TextView productCategory;
        ImageView productImage;
        ImageButton addToCartBtn;
        TextView stockIndicator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            productCategory = itemView.findViewById(R.id.productCategory);
            productImage = itemView.findViewById(R.id.productImage);
            addToCartBtn = itemView.findViewById(R.id.addToCartBtn);
            stockIndicator = itemView.findViewById(R.id.stockIndicator);
        }
    }
}