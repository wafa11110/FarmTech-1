package com.example.farmtech;

import android.os.Bundle;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import androidx.appcompat.app.AppCompatActivity;

public class MapActivity extends AppCompatActivity {
    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Initialize osmdroid configuration
        Configuration.getInstance().load(this, getSharedPreferences("YourAppName", MODE_PRIVATE));

        // Initialize the MapView
        mapView = findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK); // You can set different tile sources here
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(15.0); // Initial zoom level

        // Set the initial map center
        mapView.getController().setCenter(new GeoPoint(40.7128, -74.0060)); // New York City coordinates
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume(); // Needed for osmdroid
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause(); // Needed for osmdroid
    }
}
