package com.example.farmtech;

import android.content.Intent;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.io.IOException;
import android.location.Address;
import android.location.Geocoder;
import android.app.Activity;

public class addFragment extends Fragment {

    private static final String TAG = "addFragment";
    private static final int SCAN_SOIL_REQUEST_CODE = 102;
    private static final int MAP_REQUEST_CODE = 101;

    private double latitude, longitude;
    private EditText farmNameEditText;
    private EditText spaceEditText;
    private EditText cropEditText;
    private EditText locationEditText;
    private EditText soilTypeEditText;
    private Button saveButton;
    private ImageView chooseLocationButton, scanimage;
    private TextView recommendationTextView;

    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add, container, false);

        farmNameEditText = view.findViewById(R.id.farmname);
        spaceEditText = view.findViewById(R.id.space);
        cropEditText = view.findViewById(R.id.crop);
        locationEditText = view.findViewById(R.id.location);
        soilTypeEditText = view.findViewById(R.id.soil);
        saveButton = view.findViewById(R.id.starte2);
        chooseLocationButton = view.findViewById(R.id.imageMap);
        scanimage = view.findViewById(R.id.imagesoil);
        recommendationTextView = view.findViewById(R.id.Recommandationtextview);

        db = FirebaseFirestore.getInstance();

        chooseLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MapsActivity.class);
                startActivityForResult(intent, MAP_REQUEST_CODE);
            }
        });

        scanimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), scansoil.class);
                startActivityForResult(intent, SCAN_SOIL_REQUEST_CODE);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDataToFirestore();
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MAP_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            latitude = data.getDoubleExtra("latitude", 0.0);
            longitude = data.getDoubleExtra("longitude", 0.0);
            showLocation();
        } else if (requestCode == SCAN_SOIL_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            String soilType = data.getStringExtra("soilType");
            soilTypeEditText.setText(soilType);
        }
    }

    private void showLocation() {
        Geocoder geocoder = new Geocoder(requireActivity(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (!addresses.isEmpty()) {
                String address = addresses.get(0).getAddressLine(0);
                locationEditText.setText(address);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveDataToFirestore() {
        String farmName = farmNameEditText.getText().toString().trim();
        String space = spaceEditText.getText().toString().trim();
        String crop = cropEditText.getText().toString().trim();
        String location = locationEditText.getText().toString().trim();
        String soilType = soilTypeEditText.getText().toString().trim();

        if (TextUtils.isEmpty(farmName)) {
            farmNameEditText.setError("Farm Name is required");
            return;
        }

        if (TextUtils.isEmpty(space)) {
            spaceEditText.setError("Farm Space is required");
            return;
        }

        if (TextUtils.isEmpty(crop)) {
            cropEditText.setError("Crop Type is required");
            return;
        }

        if (TextUtils.isEmpty(location)) {
            locationEditText.setError("Farm Location is required");
            return;
        }

        if (TextUtils.isEmpty(soilType)) {
            soilTypeEditText.setError("Soil Type is required");
            return;
        }

        Map<String, Object> farmData = new HashMap<>();
        farmData.put("farmName", farmName);
        farmData.put("space", space);
        farmData.put("cropType", crop);
        farmData.put("location", location);
        farmData.put("soilType", soilType);

        db.collection("farms")
                .add(farmData)
                .addOnSuccessListener(new OnSuccessListener<Object>() {
                    @Override
                    public void onSuccess(Object o) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        clearFields();
                        Toast.makeText(getContext(), "Data saved successfully", Toast.LENGTH_SHORT).show();
                        getRecommendations(farmName, space, crop, location, soilType);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                        Toast.makeText(getContext(), "Failed to save data", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void clearFields() {
        farmNameEditText.setText("");
        spaceEditText.setText("");
        cropEditText.setText("");
        locationEditText.setText("");
        soilTypeEditText.setText("");
    }
    private void getRecommendations(String farmName, String space, String crop, String location, String soilType) {


        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.1.4:5000/") // استخدم عنوان IP الصحيح
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RecommendationApi recommendationApi = retrofit.create(RecommendationApi.class);

        Map<String, String> requestData = new HashMap<>();
        requestData.put("farmName", farmName);
        requestData.put("farm_space", space);
        requestData.put("crop_type", crop);
        requestData.put("farm_location", location);
        requestData.put("soil_type", soilType);

        call.enqueue(new Callback<RecommendationResponse>() {
            @Override
            public void onResponse(Call<RecommendationResponse> call, Response<RecommendationResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String recommendation = response.body().getRecommendation();
                    recommendationTextView.setText(recommendation);
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e("API Error", errorBody);
                        recommendationTextView.setText("Failed to get recommendation: " + errorBody);
                    } catch (IOException e) {
                        e.printStackTrace();
                        recommendationTextView.setText("Failed to get recommendation: response is not successful");
                    }
                }
            }

            @Override
            public void onFailure(Call<RecommendationResponse> call, Throwable t) {
                Log.e("API Call Failure", t.getMessage());
                recommendationTextView.setText("Error: " + t.getMessage());
            }
        });
    }
}