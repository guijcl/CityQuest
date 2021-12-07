package com.example.cityquest.Fragments;

import android.app.AlertDialog;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cityquest.Objects.LocQuest;
import com.example.cityquest.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QuestsFragment extends Fragment {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;

    public QuestsFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quests, container, false);

        Button create_local_quest = view.findViewById(R.id.local_quest);
        create_local_quest.setOnClickListener(view1 -> {
            createNewLocalQuestDialog();
        });

        Button create_elaborate_quest = view.findViewById(R.id.elaborate_quest);
        create_elaborate_quest.setOnClickListener(view1 -> {
            createNewElaborateQuestDialog();
        });

        FragmentManager childFragMan = getChildFragmentManager();
        FragmentTransaction childFragTrans = childFragMan.beginTransaction();

        db.collection("loc_quests").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                HashMap data = (HashMap) document.getData();
                                QuestFragment questFragment = new QuestFragment((String) data.get("name"), (String) data.get("desc"));
                                childFragTrans.add(R.id.all_quests, questFragment);
                            }
                            childFragTrans.commit();
                        } else {
                            Log.w("ERROR", "Error getting documents.", task.getException());
                        }
                    }
                });
        return view;
    }

    public void createNewLocalQuestDialog() {
        FragmentManager childFragMan = getChildFragmentManager();
        FragmentTransaction childFragTrans = childFragMan.beginTransaction();

        dialogBuilder = new AlertDialog.Builder(requireActivity());
        final View newQuestPopupView = getLayoutInflater().inflate(R.layout.new_locquest_popup, null);

        EditText name = newQuestPopupView.findViewById(R.id.name);
        EditText desc = newQuestPopupView.findViewById(R.id.desc);

        Button newquestpopup_save = newQuestPopupView.findViewById(R.id.create);
        Button newquestpopup_cancel = newQuestPopupView.findViewById(R.id.cancel);

        dialogBuilder.setView(newQuestPopupView);
        dialog = dialogBuilder.create();
        dialog.show();

        newquestpopup_save.setOnClickListener(view -> {
            LocQuest n_lq = new LocQuest(name.getText().toString(), desc.getText().toString());
            db.collection("loc_quests")
                    .add(n_lq)
                    .addOnSuccessListener(documentReference -> {
                        QuestFragment questFragment = new QuestFragment(name.getText().toString(), desc.getText().toString());
                        childFragTrans.add(R.id.all_quests, questFragment);
                        childFragTrans.commit();
                    })
                    .addOnFailureListener(e -> { });
            dialog.dismiss();
        });

        newquestpopup_cancel.setOnClickListener(view -> {
            dialog.dismiss();
        });
    }

    public void createNewElaborateQuestDialog() {
        dialogBuilder = new AlertDialog.Builder(requireActivity());
        final View newQuestPopupView = getLayoutInflater().inflate(R.layout.new_elaboratequest_popup, null);

        EditText name = newQuestPopupView.findViewById(R.id.name);
        EditText desc = newQuestPopupView.findViewById(R.id.desc);;

        Button newquestpopup_save = newQuestPopupView.findViewById(R.id.create);
        Button newquestpopup_cancel = newQuestPopupView.findViewById(R.id.cancel);

        dialogBuilder.setView(newQuestPopupView);
        dialog = dialogBuilder.create();
        dialog.show();

        newquestpopup_save.setOnClickListener(view -> {

        });

        newquestpopup_cancel.setOnClickListener(view -> {
            dialog.dismiss();
        });
    }
}