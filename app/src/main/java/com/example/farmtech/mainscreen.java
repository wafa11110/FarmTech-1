package com.example.farmtech;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class mainscreen extends AppCompatActivity {
    Button start,signin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainscreen);
        getWindow().setStatusBarColor(ContextCompat.getColor(mainscreen.this, R.color.white));
        changeStatusBarTextColor(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        start =findViewById(R.id.starte);
        signin =findViewById(R.id.button);
        start.setOnClickListener(v -> {

            Intent i = new Intent(mainscreen.this,formulaire1.class);
            startActivity(i);
            finish();

        });
        signin.setOnClickListener(v -> {

            Intent i = new Intent(mainscreen.this,formulaire2.class);
            startActivity(i);
            finish();

        });
    }
    private void changeStatusBarTextColor(int flags){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            View decor =getWindow().getDecorView();
            decor.setSystemUiVisibility(flags);
        }
    }
}