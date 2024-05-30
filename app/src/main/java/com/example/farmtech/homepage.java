package com.example.farmtech;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class homepage extends AppCompatActivity {

    private BottomNavigationView bottomnavigation;
    private FrameLayout framelayout;
    FloatingActionButton fab;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        bottomnavigation = findViewById(R.id.bottomnavigationview);
        framelayout = findViewById(R.id.framelayout);
        fab = findViewById(R.id.addd);

        String username = getIntent().getStringExtra("username");
        String email = getIntent().getStringExtra("email");
        String password = getIntent().getStringExtra("password");

        // Retrieve image URI from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String imageUri = prefs.getString("imageUri", null);

        // Prepare bundle with user info and image URI
        Bundle bundle = new Bundle();
        bundle.putString("username", username);
        bundle.putString("email", email);
        bundle.putString("password", password);
        if (imageUri != null) {
            bundle.putString("imageUri", imageUri);
        }

        // Load homeFragment with the bundle
        homeFragment fragment = new homeFragment();
        fragment.setArguments(bundle);
        loadFragment(fragment, true);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new addFragment(), false);
            }
        });
        bottomnavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.home) {
                    loadFragment(fragment, false);
                } else if (itemId == R.id.statics) {
                    loadFragment(new staticFragment(), false);
                } else if (itemId == R.id.notifications) {
                    loadFragment(new notificationFragment(), false);
                } else if (itemId == R.id.settings) {
                    loadFragment(new settingFragment(), false);
                }
                return true;
            }
        });
    }

    private void loadFragment(Fragment fragment, boolean isappinitialized){
        FragmentManager fragmentmanager = getSupportFragmentManager();
        FragmentTransaction fragmenttransaction = fragmentmanager.beginTransaction();
        if(isappinitialized){
            fragmenttransaction.add(R.id.framelayout, fragment);
        }
        else{
            fragmenttransaction.replace(R.id.framelayout, fragment);
        }
        fragmenttransaction.commit();
    }

}
