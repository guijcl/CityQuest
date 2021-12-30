package com.example.cityquest.Objects;

import com.example.cityquest.Fragments.Quest;

import java.util.HashMap;

public class ElaborateQuest extends Quest {

    private HashMap<String, String> quests;
    private String meters;
    private String time;

    public ElaborateQuest(String name, String desc, HashMap<String, String> quests, String time) {
        super(name, desc);
        this.quests = quests;
        this.time = time;
    }

    public ElaborateQuest(String name, String desc, String meters, String time) {
        super(name, desc);
        this.meters = meters;
        this.time = time;
    }

    public ElaborateQuest(String name, String desc, HashMap<String, String> quests, String meters, String time) {
        super(name, desc);
        this.quests = quests;
        this.meters = meters;
        this.time = time;
    }

    public HashMap<String, String> getQuests() {
        return quests;
    }

    public String getMeters() {
        return meters;
    }

    public String getTime() {
        return time;
    }
}
