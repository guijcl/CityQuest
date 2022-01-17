package com.example.cityquest.Objects;

import com.example.cityquest.Fragments.Quest;

import java.util.HashMap;

public class ElaborateQuest extends Quest {

    private HashMap<String, HashMap> quests;
    private String meters;
    private String time;
    private String popularity;
    private String experience;

    public ElaborateQuest(String name, String desc, HashMap<String, HashMap> quests, String time, String popularity, String experience) {
        super(name, desc);
        this.quests = quests;
        this.time = time;
        this.popularity = popularity;
        this.experience = experience;
    }

    public ElaborateQuest(String name, String desc, String meters, String time, String popularity, String experience) {
        super(name, desc);
        this.meters = meters;
        this.time = time;
        this.popularity = popularity;
        this.experience = experience;
    }

    public ElaborateQuest(String name, String desc, HashMap<String, HashMap> quests, String meters, String time, String popularity, String experience) {
        super(name, desc);
        this.quests = quests;
        this.meters = meters;
        this.time = time;
        this.popularity = popularity;
        this.experience = experience;
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

    public String getPopularity() { return popularity; }

    public String getExperience() {
        return experience;
    }
}
