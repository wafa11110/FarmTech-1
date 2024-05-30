package com.example.farmtech;
import android.content.Intent;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;

public class faq extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);

        // Set click listeners for the question TextViews
        findViewById(R.id.question1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleAnswerVisibility(findViewById(R.id.answer1));
            }
        });

        findViewById(R.id.question2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleAnswerVisibility(findViewById(R.id.answer2));
            }
        });

        findViewById(R.id.question3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleAnswerVisibility(findViewById(R.id.answer3));
            }
        });

        findViewById(R.id.question4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleAnswerVisibility(findViewById(R.id.answer4));
            }
        });
    }

    private void toggleAnswerVisibility(View answerView) {
        if (answerView.getVisibility() == View.VISIBLE) {
            answerView.setVisibility(View.GONE);
        } else {
            answerView.setVisibility(View.VISIBLE);
        }
    }
}
