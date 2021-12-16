package com.example.cityquest.Objects;

import com.example.cityquest.Fragments.Quest;

public class LocQuest extends Quest {

    private double latitude;
    private double longitude;

    public LocQuest(String name, String desc, double latitude, double longitude) {
        super(name, desc);

        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLongitude() {
        return longitude;
    }
    public double getLatitude() {
        return latitude;
    }
}
