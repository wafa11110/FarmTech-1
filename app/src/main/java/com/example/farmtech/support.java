package com.example.farmtech;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class support extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        Button emailButton = findViewById(R.id.email_support_button);
        emailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // تكوين ال intent
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setData(Uri.parse("mailto:")); // only email apps should handle this
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"ghodbane.ikram77@gmail.com"});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "استفسار");
                emailIntent.setType("message/rfc822"); // تحديد نوع البيانات
                if (emailIntent.resolveActivity(getPackageManager()) != null) {
                    // فتح تطبيق البريد الإلكتروني
                    startActivity(Intent.createChooser(emailIntent, "Send email"));
                }
            }
        });
    }
}

