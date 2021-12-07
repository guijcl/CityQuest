package com.example.cityquest.Fragments;

import android.animation.LayoutTransition;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.cityquest.R;

import org.w3c.dom.Text;

import java.util.HashMap;

public class QuestFragment extends Fragment {

    private final String name;
    private final String desc;
    private final String type;
    private final HashMap<String, String> tasks;

    public QuestFragment(String name, String desc, String type, HashMap<String, String> tasks) {
        this.name = name;
        this.desc = desc;
        this.type = type;
        this.tasks = tasks;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quest, container, false);

        ((TextView) view.findViewById(R.id.quest_name)).setText(name);
        ((TextView) view.findViewById(R.id.quest_desc)).setText(desc);

        if(type.equals("elaborate_quest")) {
            ((TextView) view.findViewById(R.id.task_1)).setText(tasks.get("Task_1"));
            ((TextView) view.findViewById(R.id.task_2)).setText(tasks.get("Task_2"));
            ((TextView) view.findViewById(R.id.task_3)).setText(tasks.get("Task_3"));
            ((TextView) view.findViewById(R.id.task_4)).setText(tasks.get("Task_4"));
        }

        LinearLayout clickable_layout = view.findViewById(R.id.clickable_layout);
        clickable_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout card_v = null;
                if(type.equals("loc_quest"))
                    card_v = view.findViewById(R.id.extra_card_loc);
                else if(type.equals("elaborate_quest"))
                    card_v = view.findViewById(R.id.extra_card_elaborate);

                if(card_v != null) {
                    if (card_v.getVisibility() == View.VISIBLE)
                        card_v.setVisibility(View.GONE);
                    else if (card_v.getVisibility() == View.GONE)
                        card_v.setVisibility(View.VISIBLE);
                }

            }
        });

        return view;
    }
}