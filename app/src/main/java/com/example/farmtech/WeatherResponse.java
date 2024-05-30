package com.example.farmtech;

import java.util.List;

public class WeatherResponse {
    private Main main;
    private Wind wind;
    private List<Weather> weather; // قائمة لحالات الطقس
    private String name; // اسم الموقع

    public class Main {
        private float temp;
        private int humidity;

        public float getTemp() {
            return temp;
        }

        public int getHumidity() {
            return humidity;
        }
    }

    public class Wind {
        private float speed;

        public float getSpeed() {
            return speed;
        }
    }

    public class Weather {
        private int id;
        private String main;
        private String description;
        private String icon;

        public int getId() {
            return id;
        }

        public String getMain() {
            return main;
        }

        public String getDescription() {
            return description;
        }

        public String getIcon() {
            return icon;
        }
    }

    public String getName() {
        return name;
    }

    public Main getMain() {
        return main;
    }

    public Wind getWind() {
        return wind;
    }

    public List<Weather> getWeather() {
        return weather;
    }
}