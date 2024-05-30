
package com.example.farmtech;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class homeFragment extends Fragment {

    private RecyclerView recyclerView;
    private List<Farm> farmList;
    private FirebaseFirestore db;
    private FarmAdapter adapter;
    private CircleImageView profileHome;
    private ImageView weatherIcon;
    private TextView locationTextView, temperatureTextView, humidityTextView, windSpeedTextView, datetext;
    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE = 100;
    private static final String BASE_URL = "http://api.openweathermap.org/";
    private static final String API_KEY = "338860d5b19d6cab2be80c955b4bc386"; // Replace with your OpenWeatherMap API Key

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        profileHome = view.findViewById(R.id.profileicon);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        locationTextView = view.findViewById(R.id.textlocation);
        temperatureTextView = view.findViewById(R.id.temperatureTextView);
        humidityTextView = view.findViewById(R.id.humidityTextView);
        windSpeedTextView = view.findViewById(R.id.windSpeedTextView);
        datetext = view.findViewById(R.id.date);
        weatherIcon = view.findViewById(R.id.wheather);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        datetext.setText(currentDate);

        db = FirebaseFirestore.getInstance();
        farmList = new ArrayList<>();
        loadData();
        handleProfileIconClick();

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_READ_EXTERNAL_STORAGE);
        }

        loadWeatherData(); // Load saved weather data

        return view;
    }

    private void handleProfileIconClick() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            String username = bundle.getString("username");
            String email = bundle.getString("email");
            String password = bundle.getString("password");
            String imageUri = bundle.getString("imageUri");

            if (imageUri != null) {
                Glide.with(requireContext()).load(Uri.parse(imageUri)).into(profileHome);
            }

            profileHome.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), profileaccount.class);
                    intent.putExtra("username", username);
                    intent.putExtra("email", email);
                    intent.putExtra("password", password);
                    startActivity(intent);
                }
            });
        }
    }

    private void loadData() {
        db.collection("farms")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        farmList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String farmName = document.getString("farmName");
                            String location = document.getString("location");
                            String crop = document.getString("crop");
                            String imageUrl = document.getString("imageUrl");
                            Farm farm = new Farm(document.getId(), farmName, location, crop, imageUrl);
                            farmList.add(farm);
                        }
                        adapter = new FarmAdapter(farmList, homeFragment.this);
                        recyclerView.setAdapter(adapter);

                        adapter.setOnItemClickListener(position -> {
                            Farm selectedFarm = farmList.get(position);
                            String selectedLocation = selectedFarm.getLocation();
                            convertAddressToCoordinates(selectedLocation);

                        });
                    } else {
                        Toast.makeText(getActivity(), "Error getting farms", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void convertAddressToCoordinates(String address) {
        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(address, 1);
            if (!addresses.isEmpty()) {
                Address location = addresses.get(0);
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                updateWeather(latitude, longitude);
            } else {
                Toast.makeText(getActivity(), "Location not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateWeather(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (!addresses.isEmpty()) {
                String cityName = addresses.get(0).getLocality();
                String countryName = addresses.get(0).getCountryName();
                String fullLocation = cityName + ", " + countryName;
                locationTextView.setText(fullLocation);
            } else {
                locationTextView.setText("Location not found");
            }
        } catch (IOException e) {
            e.printStackTrace();
            locationTextView.setText("Geocoder not available");
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherService service = retrofit.create(WeatherService.class);
        Call<WeatherResponse> call = service.getWeather(latitude, longitude, API_KEY);

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(@NotNull Call<WeatherResponse> call, @NotNull Response<WeatherResponse> response) {
                if (response.isSuccessful()) {
                    WeatherResponse weatherResponse = response.body();
                    double temperature = weatherResponse.getMain().getTemp();
                    double humidity = weatherResponse.getMain().getHumidity();
                    double windSpeed = weatherResponse.getWind().getSpeed();
                    temperature = temperature - 273.15;
                    String temperatureText = String.format(Locale.US, "%.1f°C", temperature);
                    String humidityText = String.format(Locale.US, "%.1f%%", humidity);
                    String windSpeedText = String.format(Locale.US, "%.1fkm/h", windSpeed);
                    temperatureTextView.setText(temperatureText);
                    humidityTextView.setText(humidityText);
                    windSpeedTextView.setText(windSpeedText);

                    saveWeatherData(locationTextView.getText().toString(), temperature, humidity, windSpeed, weatherResponse.getWeather().get(0).getIcon()); // Save weather data

                    int weatherConditionId = weatherResponse.getWeather().get(0).getId();
                    String iconCode = weatherResponse.getWeather().get(0).getIcon();

                    updateWeatherIcon(weatherConditionId, iconCode);
                }
            }

            @Override
            public void onFailure(@NotNull Call<WeatherResponse> call, @NotNull Throwable t) {
                Toast.makeText(getActivity(), "Failed to get weather data: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateWeatherIcon(int weatherConditionId, String iconCode) {
        if (weatherConditionId >= 200 && weatherConditionId <= 232) {
            if (iconCode.endsWith("d")) {
                weatherIcon.setImageResource(R.drawable.i6); // عواصف رعدية نهارا
            } else {
                weatherIcon.setImageResource(R.drawable.i6); // عواصف رعدية ليلا
            }
        } else if (weatherConditionId >= 500 && weatherConditionId <= 531) {
            if (iconCode.endsWith("d")) {
                weatherIcon.setImageResource(R.drawable.i7); // مطر نهارا
            } else {
                weatherIcon.setImageResource(R.drawable.i10); // مطر ليلا
            }
        } else if (weatherConditionId >= 600 && weatherConditionId <= 622) {
            weatherIcon.setImageResource(R.drawable.i7); // ثلج
        } else if (weatherConditionId >= 701 && weatherConditionId <= 781) {
            weatherIcon.setImageResource(R.drawable.i6); // ضباب
        } else if (weatherConditionId == 800) {
            if (iconCode.endsWith("d")) {
                weatherIcon.setImageResource(R.drawable.i1); // سماء صافية نهارا
            } else {
                weatherIcon.setImageResource(R.drawable.i8); // سماء صافية ليلا
            }
        } else if (weatherConditionId >= 801 && weatherConditionId <= 804) {
            if (iconCode.endsWith("d")) {
                weatherIcon.setImageResource(R.drawable.i2); // غيوم نهارا
            } else {
                weatherIcon.setImageResource(R.drawable.i8); // غيوم ليلا
            }
        }
    }

    private void saveWeatherData(String location, double temperature, double humidity, double windSpeed, String iconCode) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("WeatherData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("location", location);
        editor.putFloat("temperature", (float) temperature);
        editor.putFloat("humidity", (float) humidity);
        editor.putFloat("windSpeed", (float) windSpeed);
        editor.putString("iconCode", iconCode);
        editor.apply();
    }

    private void loadWeatherData() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("WeatherData", Context.MODE_PRIVATE);
        String location = sharedPreferences.getString("location", "Location not found");
        float temperature = sharedPreferences.getFloat("temperature", 0);
        float humidity = sharedPreferences.getFloat("humidity", 0);
        float windSpeed = sharedPreferences.getFloat("windSpeed", 0);
        String iconCode = sharedPreferences.getString("iconCode", "");

        locationTextView.setText(location);
        temperatureTextView.setText(String.format(Locale.US, "%.1f°C", temperature));
        humidityTextView.setText(String.format(Locale.US, "%.1f%%", humidity));
        windSpeedTextView.setText(String.format(Locale.US, "%.1fkm/h", windSpeed));

        int weatherConditionId = getWeatherConditionId(iconCode);
        updateWeatherIcon(weatherConditionId, iconCode);
    }

    private int getWeatherConditionId(String iconCode) {
        switch (iconCode) {
            case "01d":
                return 800; // Clear sky day
            case "01n":
                return 800; // Clear sky night
            case "02d":
                return 801; // Few clouds day
            case "02n":
                return 801; // Few clouds night
            case "03d":
                return 802; // Scattered clouds day
            case "03n":
                return 802; // Scattered clouds night
            case "04d":
                return 803; // Broken clouds day
            case "04n":
                return 803; // Broken clouds night
            case "09d":
                return 521; // Shower rain day
            case "09n":
                return 521; // Shower rain night
            case "10d":
                return 500; // Rain day
            case "10n":
                return 500; // Rain night
            case "11d":
                return 211; // Thunderstorm day
            case "11n":
                return 211; // Thunderstorm night
            case "13d":
                return 600; // Snow day
            case "13n":
                return 600; // Snow night
            case "50d":
                return 741; // Mist day
            case "50n":
                return 741; // Mist night
            default:
                return -1; // Unknown
        }
    }


    public void deleteFarmFromFirestore(String farmId, int position) {
        db.collection("farms").document(farmId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    farmList.remove(position);
                    adapter.notifyItemRemoved(position);
                    Toast.makeText(getActivity(), "Farm deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(getActivity(), "Error deleting farm", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                int position = adapter.getCurrentPosition();
                adapter.updateImage(position, imageUri);
            }
        }
    }
}
