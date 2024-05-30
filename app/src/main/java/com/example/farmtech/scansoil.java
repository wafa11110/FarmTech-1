package com.example.farmtech;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import org.tensorflow.lite.Interpreter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

public class scansoil extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private Interpreter tflite;
    private ImageView imageView;
    private TextView resultTextView;
    private TextView confidencesResultTextView;
    private Button takePictureButton;
    private Button apploadPictureButton;
    private ImageView backButton; // New button to confirm and return result

    private String soilTypeResult; // Variable to hold the soil type result

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scansoil);

        imageView = findViewById(R.id.imageView);
        resultTextView = findViewById(R.id.resultTextView);
        confidencesResultTextView = findViewById(R.id.confidencesResultTextView);
        takePictureButton = findViewById(R.id.takePictureButton);
        apploadPictureButton = findViewById(R.id.apploadPictureButton);
        backButton = findViewById(R.id.back); // Initialize the new button

        try {
            tflite = new Interpreter(loadModelFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        takePictureButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            } else {
                openCamera();
            }
        });

        apploadPictureButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 2);
        });

        // Set click listener for confirm button
        backButton.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("soilType", soilTypeResult);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                // Permission denied, show a message to the user
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);
            classifyImage(imageBitmap);
        } else if (requestCode == 2 && resultCode == RESULT_OK && data != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                imageView.setImageBitmap(bitmap);
                classifyImage(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private MappedByteBuffer loadModelFile() throws IOException {
        FileInputStream fileInputStream = new FileInputStream(getAssets().openFd("model_unquantt.tflite").getFileDescriptor());
        FileChannel fileChannel = fileInputStream.getChannel();
        long startOffset = getAssets().openFd("model_unquantt.tflite").getStartOffset();
        long declaredLength = getAssets().openFd("model_unquantt.tflite").getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private void classifyImage(Bitmap bitmap) {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true);
        int batchSize = 1;
        int[] inputShape = {batchSize, 224, 224, 3};
        float[][][][] input = new float[batchSize][224][224][3];
        for (int i = 0; i < 224; i++) {
            for (int j = 0; j < 224; j++) {
                int pixel = resizedBitmap.getPixel(i, j);
                input[0][i][j][0] = (pixel >> 16) & 0xFF;
                input[0][i][j][1] = (pixel >> 8) & 0xFF;
                input[0][i][j][2] = pixel & 0xFF;
            }
        }

        float[][] output = new float[1][3]; // Adjust the size based on the number of classes in your model
        tflite.run(input, output);

        float[] confidences = output[0];
        int maxIndex = 0;
        for (int i = 1; i < confidences.length; i++) {
            if (confidences[i] > confidences[maxIndex]) {
                maxIndex = i;
            }
        }

        String[] labels = {"Sandy soil", "Clay soil", "Loamy soil"}; // Adjust based on your categories
        String result = labels[maxIndex];
        soilTypeResult = result; // Store the result
        resultTextView.setText(result);

        StringBuilder confidencesString = new StringBuilder();
        for (int i = 0; i < labels.length; i++) {
            confidencesString.append(labels[i]).append(": ").append(String.format("%.1f%%", confidences[i] * 100)).append("\n");
        }
        confidencesResultTextView.setText(confidencesString.toString());
    }
}
