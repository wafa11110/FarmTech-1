package com.example.farmtech;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class FarmAdapter extends RecyclerView.Adapter<FarmAdapter.ViewHolder> {
    private List<Farm> farmList;
    private int currentPosition;
    private Fragment fragment;
    private OnItemClickListener mListener;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public FarmAdapter(List<Farm> farmList, Fragment fragment) {
        this.farmList = farmList;
        this.fragment = fragment;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.homeframe, parent, false);
        return new ViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Farm farm = farmList.get(position);
        holder.bind(farm);

        holder.itemView.setOnClickListener(v -> {
            selectedPosition = holder.getAdapterPosition();
            if (mListener != null) {
                mListener.onItemClick(selectedPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return farmList.size();
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void updateImage(int position, Uri imageUri) {
        Farm farm = farmList.get(position);
        farm.setImageUrl(imageUri.toString());
        notifyItemChanged(position);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("farms").document(farm.getId())
                .update("imageUrl", imageUri.toString())
                .addOnSuccessListener(aVoid -> Toast.makeText(fragment.getActivity(), "Image updated", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(fragment.getActivity(), "Failed to update image", Toast.LENGTH_SHORT).show());
    }

    public void removeItem(int position) {
        farmList.remove(position);
        notifyItemRemoved(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView farmNameTextView;
        private TextView cropTextView;
        private TextView locationTextView;
        private ImageView farmImageView;
        private Button harvestButton;
        private FarmAdapter adapter;

        public ViewHolder(@NonNull View itemView, FarmAdapter adapter) {
            super(itemView);
            this.adapter = adapter;

            farmNameTextView = itemView.findViewById(R.id.farm_Name);
            cropTextView = itemView.findViewById(R.id.textviewcrop);
            locationTextView = itemView.findViewById(R.id.textviewlocation);
            farmImageView = itemView.findViewById(R.id.imagefarm);
            harvestButton = itemView.findViewById(R.id.supprimerframelayout);

            farmImageView.setOnClickListener(v -> {
                adapter.currentPosition = getAdapterPosition();
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                adapter.fragment.startActivityForResult(intent, 1);
            });

            harvestButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    showDeleteConfirmationDialog(position);
                }
            });
        }

        public void bind(Farm farm) {
            farmNameTextView.setText(farm.getFarmName());
            cropTextView.setText(farm.getCropType());
            locationTextView.setText(farm.getLocation());
            if (farm.getImageUrl() != null && !farm.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(farm.getImageUrl())
                        .placeholder(R.drawable.baseline_photo_camera_24)
                        .into(farmImageView);
            } else {
                farmImageView.setImageResource(R.drawable.baseline_photo_camera_24);
            }
        }

        private void showDeleteConfirmationDialog(int position) {
            Context context = itemView.getContext();
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            LayoutInflater inflater = LayoutInflater.from(context);
            View dialogView = inflater.inflate(R.layout.dialog_delete_harvest, null);

            // Create a ShapeDrawable with rounded corners
            ShapeDrawable shapeDrawable = new ShapeDrawable();
            shapeDrawable.setShape(new RoundRectShape(
                    new float[] {50, 50, 50, 50, 50, 50, 50, 50}, // Array of radii for the corners
                    null, // No inset
                    null  // No inner rounded corners
            ));
            shapeDrawable.getPaint().setColor(context.getResources().getColor(android.R.color.white));
            shapeDrawable.getPaint().setStyle(Paint.Style.FILL);

            // Set the background of the dialogView to the ShapeDrawable
            dialogView.setBackground(shapeDrawable);

            builder.setView(dialogView);

            AlertDialog dialog = builder.create();

            Button cancelButton = dialogView.findViewById(R.id.Button1);
            Button deleteButton = dialogView.findViewById(R.id.Button2);

            cancelButton.setOnClickListener(v -> dialog.dismiss());

            deleteButton.setOnClickListener(v -> {
                String farmId = adapter.farmList.get(position).getId();
                ((homeFragment) adapter.fragment).deleteFarmFromFirestore(farmId, position);
                dialog.dismiss();
            });

            dialog.show();
        }

    }
}
