package com.example.cityquest.Objects;

import com.example.cityquest.Fragments.Quest;

import java.util.HashMap;

public class ElaborateQuest extends Quest {

    private HashMap<String, HashMap> quests;
    private String meters;
    private String time;
    private int popularity;

    public ElaborateQuest(String name, String desc, HashMap<String, HashMap> quests, String time, int popularity) {
        super(name, desc);
        this.quests = quests;
        this.time = time;
        this.popularity = popularity;
    }

    public ElaborateQuest(String name, String desc, String meters, String time, int popularity) {
        super(name, desc);
        this.meters = meters;
        this.time = time;
        this.popularity = popularity;
    }

    public ElaborateQuest(String name, String desc, HashMap<String, HashMap> quests, String meters, String time, int popularity) {
        super(name, desc);
        this.quests = quests;
        this.meters = meters;
        this.time = time;
        this.popularity = popularity;
    }

    public HashMap<String, HashMap> getQuests() {
        return quests;
    }

    public String getMeters() {
        return meters;
    }

    public String getTime() {
        return time;
    }

    public int getPopularity() { return popularity; }
}
