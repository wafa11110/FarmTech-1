package com.example.farmtech;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

public class settingFragment extends Fragment {

    TextView supportLabel, faqLabel, aboutLabel, languageLabel;
    Switch notificationSwitch;
    SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        supportLabel = view.findViewById(R.id.supportpage);
        faqLabel = view.findViewById(R.id.faqpage);
        aboutLabel = view.findViewById(R.id.aboutpage);
        languageLabel = view.findViewById(R.id.Languagepage);
        notificationSwitch = view.findViewById(R.id.notifications_switch);

        sharedPreferences = getActivity().getSharedPreferences("FarmTech", Context.MODE_PRIVATE);

        boolean isNotificationEnabled = sharedPreferences.getBoolean("notifications_enabled", true);
        notificationSwitch.setChecked(isNotificationEnabled);

        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                showConfirmationDialog(true);
            } else {
                showConfirmationDialog(false);
            }
        });

        languageLabel.setOnClickListener(v -> startActivity(new Intent(getActivity(), language.class)));
        supportLabel.setOnClickListener(v -> startActivity(new Intent(getActivity(), support.class)));
        faqLabel.setOnClickListener(v -> startActivity(new Intent(getActivity(), faq.class)));
        aboutLabel.setOnClickListener(v -> startActivity(new Intent(getActivity(), about.class)));

        return view;
    }

    private void showConfirmationDialog(boolean enable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(enable ? "Enable Notifications" : "Disable Notifications")
                .setMessage(enable ? "Do you want to enable notifications?" : "Do you want to disable notifications?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    sharedPreferences.edit().putBoolean("notifications_enabled", enable).apply();
                    Toast.makeText(getActivity(), enable ? "Notifications Enabled" : "Notifications Disabled", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    notificationSwitch.setChecked(!enable);
                    dialog.dismiss();
                })
                .create()
                .show();
    }
}
