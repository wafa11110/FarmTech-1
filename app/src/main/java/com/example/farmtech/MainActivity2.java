package com.example.farmtech;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity2 extends AppCompatActivity {

    ViewPager mSLideViewPager;
    LinearLayout mDotLayout;
    Button backbtn, nextbtn, skipbtn;

    TextView[] dots;
    ViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        getWindow().setStatusBarColor(ContextCompat.getColor(MainActivity2.this,R.color.white));
        changeStatusBarTextColor(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        backbtn = findViewById(R.id.backbtn);
        nextbtn = findViewById(R.id.nextbtn);
        skipbtn = findViewById(R.id.skipButton);
        backbtn.setVisibility(View.INVISIBLE);
        backbtn.setOnClickListener(v -> {

            if (getitem(0) > 0) {

                mSLideViewPager.setCurrentItem(getitem(-1), true);
            }
        });

        nextbtn.setOnClickListener(v -> {

            if (getitem(0) < 2)
                mSLideViewPager.setCurrentItem(getitem(1),true);
            else {

                Intent i = new Intent(MainActivity2.this,mainscreen.class);
                startActivity(i);
                finish();

            }

        });

        skipbtn.setOnClickListener(v -> {


            Intent i = new Intent(MainActivity2.this,mainscreen.class);
            startActivity(i);
            finish();

        });

        mSLideViewPager = findViewById(R.id.slideViewPager);
        mDotLayout = findViewById(R.id.indicator_layout);

        viewPagerAdapter = new ViewPagerAdapter(this);

        mSLideViewPager.setAdapter(viewPagerAdapter);

        setUpindicator(0);
        mSLideViewPager.addOnPageChangeListener(viewListener);

    }

    public void setUpindicator(int position){

        dots = new TextView[3];
        mDotLayout.removeAllViews();

        for (int i = 0 ; i < dots.length ; i++){

            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(getResources().getColor(R.color.inactive,getApplicationContext().getTheme()));
            mDotLayout.addView(dots[i]);

        }

        dots[position].setTextColor(getResources().getColor(R.color.active,getApplicationContext().getTheme()));

    }

    ViewPager.OnPageChangeListener viewListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            setUpindicator(position);
            if (position == 0){

                backbtn.setVisibility(View.INVISIBLE);
                nextbtn.setVisibility(View.VISIBLE);
            }else {
                backbtn.setVisibility(View.VISIBLE);
                nextbtn.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    private int getitem(int i){

        return mSLideViewPager.getCurrentItem() + i;
    }
    private void changeStatusBarTextColor(int flags){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            View decor =getWindow().getDecorView();
            decor.setSystemUiVisibility(flags);
        }
    }
}