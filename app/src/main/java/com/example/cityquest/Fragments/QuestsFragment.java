package com.example.cityquest.Fragments;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.cityquest.R;

public class QuestsFragment extends Fragment {

    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private EditText name, desc;
    private Button newquestpopup_cancel, newquestpopup_save;

    public QuestsFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quests, container, false);
        Button create_quest = view.findViewById(R.id.create_quest);
        create_quest.setOnClickListener(view1 -> {
            createNewQuestDialog();
        });

        return view;
    }

    public void createNewQuestDialog() {
        dialogBuilder = new AlertDialog.Builder(requireActivity());
        final View newQuestPopupView = getLayoutInflater().inflate(R.layout.new_quest_popup, null);
        name = newQuestPopupView.findViewById(R.id.name);
        desc = newQuestPopupView.findViewById(R.id.desc);

        newquestpopup_save = newQuestPopupView.findViewById(R.id.create);
        newquestpopup_cancel = newQuestPopupView.findViewById(R.id.cancel);

        dialogBuilder.setView(newQuestPopupView);
        dialog = dialogBuilder.create();
        dialog.show();

        newquestpopup_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        newquestpopup_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

    }
}