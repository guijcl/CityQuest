package com.example.cityquest.Objects;

import com.example.cityquest.Fragments.Quest;

public class LocQuest extends Quest {

    private double latitude;
    private double longitude;
    private String popularity;
    private String experience;

    public LocQuest(String name, String desc, double latitude, double longitude, String popularity, String experience) {
        super(name, desc);

        this.latitude = latitude;
        this.longitude = longitude;
        this.popularity = popularity;
        this.experience = experience;
    }

    public double getLongitude() {
        return longitude;
    }
    public double getLatitude() {
        return latitude;
    }
    public String getPopularity() {
        return popularity;
    }
    public String getExperience() {
        return experience;
    }

}
