package com.example.farmtech;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class staticFragment extends Fragment {

    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private List<Farm> farmList;
    private SharedPreferences sharedPreferences;

    private static final int SCAN_CROP_REQUEST_CODE = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_static, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        db = FirebaseFirestore.getInstance();
        farmList = new ArrayList<>();
        sharedPreferences = requireActivity().getSharedPreferences("FarmTech", Context.MODE_PRIVATE);

        loadData();

        return view;
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
                            String crop = document.getString("cropType");
                            String soilType = document.getString("soilType");
                            String space = document.getString("space");

                            Farm farm = new Farm(farmName, location, crop, soilType, space);
                            farmList.add(farm);
                        }
                        FarmAdapter adapter = new FarmAdapter(farmList);
                        recyclerView.setAdapter(adapter);
                    } else {
                        Toast.makeText(getActivity(), "Failed to load data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public class FarmAdapter extends RecyclerView.Adapter<FarmAdapter.ViewHolder> {
        private List<Farm> farmList;

        public FarmAdapter(List<Farm> farmList) {
            this.farmList = farmList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.staticframe, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Farm farm = farmList.get(position);
            holder.farmNameTextView.setText(farm.getFarmName());
            holder.locationTextView.setText(farm.getLocation());
            holder.soilTypeTextView.setText(farm.getSoilType());
            holder.spaceTextView.setText(farm.getSpace());

            List<BarEntry> entries = loadBarChartData(farm.getFarmName());
            BarDataSet dataSet = new BarDataSet(entries, farm.getCropType());
            dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
            BarData barData = new BarData(dataSet);
            holder.barChart.setData(barData);

            // Configure BarChart
            holder.barChart.getDescription().setEnabled(false);
            holder.barChart.setDrawGridBackground(false);
            holder.barChart.setDrawBarShadow(false);
            holder.barChart.setDrawValueAboveBar(true);
            holder.barChart.getAxisLeft().setAxisMinimum(0f); // Minimum value for Y axis
            holder.barChart.getAxisRight().setEnabled(false);
            holder.barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

            // Set XAxis labels
            setBarChartXAxisLabels(holder.barChart, farm.getFarmName());

            holder.moreVertImageView.setOnClickListener(v -> showMoreOptions(holder.itemView, farm, holder.barChart));
        }

        @Override
        public int getItemCount() {
            return farmList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private TextView farmNameTextView;
            private TextView locationTextView;
            private TextView soilTypeTextView;
            private TextView spaceTextView;
            private BarChart barChart;
            private ImageView moreVertImageView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                farmNameTextView = itemView.findViewById(R.id.farmname);
                locationTextView = itemView.findViewById(R.id.location);
                soilTypeTextView = itemView.findViewById(R.id.soiltextview);
                spaceTextView = itemView.findViewById(R.id.spacetextview);
                barChart = itemView.findViewById(R.id.barChart);
                moreVertImageView = itemView.findViewById(R.id.more_vert);
            }
        }
    }

    private List<BarEntry> loadBarChartData(String farmName) {
        List<BarEntry> entries = new ArrayList<>();
        String data = sharedPreferences.getString(farmName, "");
        if (!data.isEmpty()) {
            String[] pairs = data.split(";");
            for (int i = 0; i < pairs.length; i++) {
                String[] entry = pairs[i].split(",");
                entries.add(new BarEntry(i, Float.parseFloat(entry[1])));
            }
        }
        return entries;
    }

    private void setBarChartXAxisLabels(BarChart barChart, String farmName) {
        String data = sharedPreferences.getString(farmName, "");
        if (!data.isEmpty()) {
            String[] pairs = data.split(";");
            List<String> dates = new ArrayList<>();
            for (String pair : pairs) {
                String[] entry = pair.split(",");
                dates.add(entry[0]);
            }

            XAxis xAxis = barChart.getXAxis();
            xAxis.setValueFormatter(new IndexAxisValueFormatter(dates));
            xAxis.setGranularity(1f); // Minimum interval for X axis labels
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        }
    }

    private void showMoreOptions(View view, Farm farm, BarChart barChart) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Options")
                .setItems(new CharSequence[]{"Add Value", "Scan Crop", "Delete Last Value"}, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            showAddValueDialog(view, farm, barChart);
                            break;
                        case 1:
                            Intent intent = new Intent(getActivity(), scanCrop.class);
                            startActivityForResult(intent, SCAN_CROP_REQUEST_CODE);
                            break;
                        case 2:
                            deleteLastValue(view, farm, barChart);
                            break;
                    }
                });
        builder.show();
    }

    private void showAddValueDialog(View view, Farm farm, BarChart barChart) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        LayoutInflater inflater = LayoutInflater.from(view.getContext());
        View dialogView = inflater.inflate(R.layout.dialog_add_value, null);
        builder.setView(dialogView);

        EditText editTextValue = dialogView.findViewById(R.id.edittextadd);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String value = editTextValue.getText().toString();
            if (!value.isEmpty()) {
                addValueToChart(farm.getFarmName(), Float.parseFloat(value), barChart);
                Toast.makeText(view.getContext(), "Value added: " + value, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(view.getContext(), "Please enter a value", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void addValueToChart(String farmName, float value, BarChart barChart) {
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        String data = sharedPreferences.getString(farmName, "");
        data += currentDate + "," + value + ";";
        sharedPreferences.edit().putString(farmName, data).apply();

        List<BarEntry> entries = loadBarChartData(farmName);
        BarDataSet dataSet = new BarDataSet(entries, "Crop Data");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        // Set XAxis labels
        setBarChartXAxisLabels(barChart, farmName);

        barChart.invalidate();
    }

    private void deleteLastValue(View view, Farm farm, BarChart barChart) {
        String farmName = farm.getFarmName();
        String data = sharedPreferences.getString(farmName, "");

        if (!data.isEmpty()) {
            String[] pairs = data.split(";");
            StringBuilder newData = new StringBuilder();
            for (int i = 0; i < pairs.length - 1; i++) {
                newData.append(pairs[i]).append(";");
            }
            sharedPreferences.edit().putString(farmName, newData.toString()).apply();

            List<BarEntry> entries = loadBarChartData(farmName);
            BarDataSet dataSet = new BarDataSet(entries, "Crop Data");
            dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
            BarData barData = new BarData(dataSet);
            barChart.setData(barData);

            // Set XAxis labels
            setBarChartXAxisLabels(barChart, farmName);

            barChart.invalidate();
            Toast.makeText(view.getContext(), "Last value deleted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(view.getContext(), "No values to delete", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SCAN_CROP_REQUEST_CODE && resultCode == getActivity().RESULT_OK && data != null) {
            String resultValue = data.getStringExtra("soilTypeResult");
            if (resultValue != null) {
                Toast.makeText(getContext(), "Crop type scanned: " + resultValue, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class Farm {
        private String farmName;
        private String location;
        private String cropType;
        private String soilType;
        private String space;

        public Farm(String farmName, String location, String cropType, String soilType, String space) {
            this.farmName = farmName;
            this.location = location;
            this.cropType = cropType;
            this.soilType = soilType;
            this.space = space;
        }

        public String getFarmName() {
            return farmName;
        }

        public String getLocation() {
            return location;
        }

        public String getCropType() {
            return cropType;
        }

        public String getSoilType() {
            return soilType;
        }

        public String getSpace() {
            return space;
        }
    }
}
