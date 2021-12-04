package com.example.cityquest.Objects;

public class Quest {

    private String name;
    private String desc;

    public Quest(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public String getName() {
        return name;
    }
}
