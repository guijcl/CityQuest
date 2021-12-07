package com.example.cityquest.Objects;

import com.example.cityquest.Fragments.Quest;

import java.util.HashMap;

public class ElaborateQuest extends Quest {

    private HashMap<String, String> tasks;

    public ElaborateQuest(String name, String desc, HashMap<String, String> tasks) {
        super(name, desc);
        this.tasks = tasks;
    }

    public HashMap<String, String> getTasks() {
        return tasks;
    }
}
