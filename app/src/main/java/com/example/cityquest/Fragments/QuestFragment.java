package com.example.cityquest.Fragments;

import android.animation.LayoutTransition;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.cityquest.R;

public class QuestFragment extends Fragment {

    private final String name;
    private final String desc;

    public QuestFragment(String name, String desc) {
        this.name = name;
        this.desc = desc;
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

        LinearLayout clickable_layout = view.findViewById(R.id.clickable_layout);
        clickable_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View card_v = view.findViewById(R.id.extra_card);

                if(card_v.getVisibility() == View.VISIBLE)
                    card_v.setVisibility(View.GONE);
                else if(card_v.getVisibility() == View.GONE)
                    card_v.setVisibility(View.VISIBLE);

            }
        });

        return view;
    }
}