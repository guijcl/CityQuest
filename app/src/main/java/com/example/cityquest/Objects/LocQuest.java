package com.example.cityquest.Objects;

import android.graphics.Point;

public class LocQuest extends Quest {

    private Point coord;

    public LocQuest(String name, String desc, Point coord) {
        super(name, desc);
        this.coord = coord;
    }

}
