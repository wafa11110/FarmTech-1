package com.example.farmtech;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

public class language extends AppCompatActivity {

    private static final String PREFERENCES_FILE = "my_app_prefs";
    private static final String KEY_LANGUAGE = "language";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load saved language
        SharedPreferences prefs = getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        String language = prefs.getString(KEY_LANGUAGE, Locale.getDefault().getLanguage());
        setLocale(language);

        setContentView(R.layout.activity_language);

        Button btnEnglish = findViewById(R.id.tvEnglish);
        Button btnArabic = findViewById(R.id.tvArabic);
        Button btnFrench = findViewById(R.id.tvFrench);



        btnEnglish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeLanguage("en");
            }
        });

        btnArabic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeLanguage("ar");
            }
        });

        btnFrench.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeLanguage("fr");
            }
        });
    }

    public void setLocale(String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.setLocale(myLocale);
        res.updateConfiguration(conf, dm);


        SharedPreferences.Editor editor = getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE).edit();
        editor.putString(KEY_LANGUAGE, lang);
        editor.apply();
    }


    public void changeLanguage(String lang) {
        setLocale(lang);
        recreate();
    }
}



