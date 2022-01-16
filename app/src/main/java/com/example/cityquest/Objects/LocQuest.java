package com.example.cityquest.Objects;

import com.example.cityquest.Fragments.Quest;

public class LocQuest extends Quest {

    private double latitude;
    private double longitude;
    private int popularity;

    public LocQuest(String name, String desc, double latitude, double longitude, int popularity) {
        super(name, desc);

        this.latitude = latitude;
        this.longitude = longitude;
        this.popularity = popularity;
    }

    public double getLongitude() {
        return longitude;
    }
    public double getLatitude() {
        return latitude;
    }
    public double getPopularity() {
        return popularity;
    }
}
