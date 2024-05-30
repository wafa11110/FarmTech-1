package com.example.farmtech;

public class Farm {
    private String id;
    private String farmName;
    private String location;
    private String cropType;
    private String imageUrl;

    public Farm() {
        // Required empty public constructor
    }

    public Farm(String id, String farmName, String location, String cropType, String imageUrl) {
        this.id = id;
        this.farmName = farmName;
        this.location = location;
        this.cropType = cropType;
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    public String getFarmName() {
        return farmName;
    }

    public void setFarmName(String farmName) {
        this.farmName = farmName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCropType() {
        return cropType;
    }

    public void setCropType(String cropType) {
        this.cropType = cropType;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
