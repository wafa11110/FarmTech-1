package com.example.farmtech;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class notificationFragment extends Fragment {

    private RecyclerView recyclerView;
    private List<String> notifications;
    private SharedPreferences sharedPreferences;
    private ImageView bellIcon;
    private TextView noNotificationsText;
    private TextView additionalMessageText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        bellIcon = view.findViewById(R.id.bellIcon);
        noNotificationsText = view.findViewById(R.id.noNotificationsText);
        additionalMessageText = view.findViewById(R.id.additionalMessageText);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        sharedPreferences = getActivity().getSharedPreferences("FarmTech", Context.MODE_PRIVATE);
        boolean isNotificationEnabled = sharedPreferences.getBoolean("notifications_enabled", true);

        notifications = new ArrayList<>();

        if (isNotificationEnabled) {
            String notificationData = sharedPreferences.getString("notification_data", "");
            if (!notificationData.isEmpty()) {
                String[] notificationArray = notificationData.split(";");
                for (String notification : notificationArray) {
                    notifications.add(notification);
                }
            }
        }

        NotificationAdapter adapter = new NotificationAdapter(notifications);
        recyclerView.setAdapter(adapter);

        updateUI(isNotificationEnabled);

        return view;
    }

    private void updateUI(boolean isNotificationEnabled) {
        if (!isNotificationEnabled) {
            bellIcon.setImageResource(R.drawable.notificationoff); // تأكد من وجود هذا المورد في مجلد drawable
            noNotificationsText.setText("Notifications are disabled.");
            additionalMessageText.setText("Enable notifications in settings to stay updated.");
        }

        if (notifications.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            bellIcon.setVisibility(View.VISIBLE);
            noNotificationsText.setVisibility(View.VISIBLE);
            additionalMessageText.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            bellIcon.setVisibility(View.GONE);
            noNotificationsText.setVisibility(View.GONE);
            additionalMessageText.setVisibility(View.GONE);
        }
    }

    private static class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
        private List<String> notifications;

        NotificationAdapter(List<String> notifications) {
            this.notifications = notifications;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String notification = notifications.get(position);
            holder.notificationTextView.setText(notification);
        }

        @Override
        public int getItemCount() {
            return notifications.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView notificationTextView;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                notificationTextView = itemView.findViewById(R.id.notificationTextView);
            }
        }
    }
}

