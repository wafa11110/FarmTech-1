package com.example.farmtech;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class profileaccount extends AppCompatActivity {
    TextView profileEmail, profileUsername, profilePassword, titleUsername;
    Button signout;
    FirebaseAuth mAuth;
    ImageView back;
    CircleImageView profileImage;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profileaccount);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        changeStatusBarTextColor(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        profileEmail = findViewById(R.id.textViewname1);
        profileUsername = findViewById(R.id.textViewname);
        profilePassword = findViewById(R.id.textViewname2);
        titleUsername = findViewById(R.id.textViewuserName);
        signout = findViewById(R.id.textViewname3);
        back = findViewById(R.id.imageViewback);
        profileImage = findViewById(R.id.imageViewProfile);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signout();
            }
        });

        // Request permission to access external storage
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
        }

        // Add click listener to profile image to choose a new image
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            }
        });

        // Restore image from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String imageUri = prefs.getString("imageUri", null);
        if (imageUri != null) {
            Glide.with(this).load(Uri.parse(imageUri)).into(profileImage);
        }

        showAllUserData();
    }

    public void showAllUserData() {
        Intent intent = getIntent();
        String emailUser = intent.getStringExtra("email");
        String usernameUser = intent.getStringExtra("username");
        String passwordUser = intent.getStringExtra("password");
        titleUsername.setText(usernameUser);
        profileEmail.setText(emailUser);
        profileUsername.setText(usernameUser);
        profilePassword.setText(passwordUser);
    }

    private void signout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(profileaccount.this, formulaire2.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                profileImage.setImageBitmap(bitmap);

                // Save the image URI to SharedPreferences
                SharedPreferences.Editor editor = getSharedPreferences("MyPrefs", MODE_PRIVATE).edit();
                editor.putString("imageUri", uri.toString());
                editor.apply();

                // Send the image URI to homepage
                Intent intent = new Intent(profileaccount.this, homepage.class);
                intent.putExtra("imageUri", uri.toString());
                startActivity(intent);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted to read your External storage", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void changeStatusBarTextColor(int flags) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(flags);
        }
    }
}
