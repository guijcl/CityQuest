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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.cityquest.Objects.ElaborateQuest;
import com.example.cityquest.Objects.LocQuest;
import com.example.cityquest.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class QuestsFragment extends Fragment {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Geocoder geocoder;

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

        geocoder = new Geocoder(this.requireContext());

        Button create_local_quest = view.findViewById(R.id.local_quest);
        create_local_quest.setOnClickListener(view1 -> {
            createNewLocalQuestDialog();
        });

        Button create_elaborate_quest = view.findViewById(R.id.elaborate_quest);
        create_elaborate_quest.setOnClickListener(view1 -> {
            createNewElaborateQuestDialog();
        });

        FragmentManager childFragMan = getChildFragmentManager();

        db.collection("loc_quests").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            FragmentTransaction childFragTrans = childFragMan.beginTransaction();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                HashMap data = (HashMap) document.getData();
                                QuestFragment questFragment = new QuestFragment(document.getId(), (String) data.get("name"), (String) data.get("desc"), "loc_quest", null);
                                childFragTrans.add(R.id.all_quests, questFragment);
                            }
                            childFragTrans.commit();
                        } else {
                            Log.w("ERROR", "Error getting documents.", task.getException());
                        }
                    }
                });

        db.collection("elaborate_quests").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            FragmentTransaction childFragTrans = childFragMan.beginTransaction();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                HashMap data = (HashMap) document.getData();
                                QuestFragment questFragment = new QuestFragment(document.getId(), (String) data.get("name"), (String) data.get("desc"),
                                        "elaborate_quest", (HashMap<String, String>) data.get("tasks"));
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
            //CODIGO PARA NOME CORRETO
            List<Address> addressList = null;
            try {
                addressList = geocoder.getFromLocationName(name.getText().toString(), 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(addressList != null) {
                if(addressList.size() > 0) {
                    Address address = addressList.get(0);

                    String tmp_name = "";
                    if(address.getFeatureName().equals(address.getLocality()))
                        tmp_name = address.getFeatureName() + ", " + address.getAdminArea() + ", " + address.getCountryName();
                    else
                        tmp_name = address.getFeatureName() + ", " + address.getLocality() + ", " + address.getAdminArea() + ", " + address.getCountryName();

                    LocQuest n_lq = new LocQuest(tmp_name, desc.getText().toString());
                    db.collection("loc_quests")
                            .add(n_lq)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    QuestFragment questFragment = new QuestFragment(documentReference.getId(), n_lq.getName(), n_lq.getDesc(), "loc_quest", null);
                                    childFragTrans.add(R.id.all_quests, questFragment);
                                    childFragTrans.commit();
                                }
                            })
                            .addOnFailureListener(e -> { });
                    dialog.dismiss();
                }
            }

        });

        newquestpopup_cancel.setOnClickListener(view -> {
            dialog.dismiss();
        });
    }

    public void createNewElaborateQuestDialog() {
        FragmentManager childFragMan = getChildFragmentManager();
        FragmentTransaction childFragTrans = childFragMan.beginTransaction();

        dialogBuilder = new AlertDialog.Builder(requireActivity());
        final View newQuestPopupView = getLayoutInflater().inflate(R.layout.new_elaboratequest_popup, null);

        EditText name = newQuestPopupView.findViewById(R.id.name);
        EditText desc = newQuestPopupView.findViewById(R.id.desc);

        EditText task1 = newQuestPopupView.findViewById(R.id.task_1);
        EditText task2 = newQuestPopupView.findViewById(R.id.task_2);
        EditText task3 = newQuestPopupView.findViewById(R.id.task_3);
        EditText task4 = newQuestPopupView.findViewById(R.id.task_4);

        Button newquestpopup_save = newQuestPopupView.findViewById(R.id.create);
        Button newquestpopup_cancel = newQuestPopupView.findViewById(R.id.cancel);

        dialogBuilder.setView(newQuestPopupView);
        dialog = dialogBuilder.create();
        dialog.show();

        newquestpopup_save.setOnClickListener(view -> {
            HashMap<String, String> tasks = new HashMap<String, String>() {{
                put("Task_1", task1.getText().toString());
                put("Task_2", task2.getText().toString());
                put("Task_3", task3.getText().toString());
                put("Task_4", task4.getText().toString());
            }};

            ElaborateQuest n_eq = new ElaborateQuest(name.getText().toString(), desc.getText().toString(), tasks);
            db.collection("elaborate_quests")
                    .add(n_eq)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            QuestFragment questFragment = new QuestFragment(documentReference.getId(), name.getText().toString(), desc.getText().toString(), "elaborate_quest", tasks);
                            childFragTrans.add(R.id.all_quests, questFragment);
                            childFragTrans.commit();
                        }
                    })
                    .addOnFailureListener(e -> { });
            dialog.dismiss();
        });

        newquestpopup_cancel.setOnClickListener(view -> {
            dialog.dismiss();
        });
    }
}