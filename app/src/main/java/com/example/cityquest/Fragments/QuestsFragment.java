package com.example.cityquest.Fragments;

import android.app.AlertDialog;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.cityquest.Objects.ElaborateQuest;
import com.example.cityquest.Objects.LocQuest;
import com.example.cityquest.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class QuestsFragment extends Fragment {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Geocoder geocoder;

    String[] categoryItems = {"Elaborate Quest", "Local Quest"};
    AutoCompleteTextView autoCompleteTextCat;
    ArrayAdapter<String> adapterItemsCat;

    String[] orderByItems = {"Latest Quests", "Oldest Quests", "Nearest Quests", "Most Distant Quests", "Most Popular Quests"};
    AutoCompleteTextView autoCompleteTextOrder;
    ArrayAdapter<String> adapterItemsOrder;

    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;

    private List<HashMap<String, Double>> loc_quests_suggestions_coords = new ArrayList<>();
    private String selected_item = "";
    private double selected_item_latitude = 0.0;
    private double selected_item_longitude = 0.0;

    public QuestsFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quests, container, false);

        autoCompleteTextCat = view.findViewById(R.id.autoCompleteOne);
        adapterItemsCat = new ArrayAdapter<String>(getActivity(),R.layout.list_item_questsfrag, categoryItems);
        autoCompleteTextCat.setAdapter(adapterItemsCat);
        autoCompleteTextCat.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String item = adapterView.getItemAtPosition(i).toString();
                //filter by category
            }
        });

        autoCompleteTextOrder = view.findViewById(R.id.autoCompleteTwo);
        adapterItemsOrder = new ArrayAdapter<String>(getActivity(),R.layout.list_item_questsfrag, orderByItems);
        autoCompleteTextOrder.setAdapter(adapterItemsOrder);
        autoCompleteTextOrder.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String item = adapterView.getItemAtPosition(i).toString();
                //filter by category
            }
        });


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
                                QuestFragment questFragment = new QuestFragment(document.getId(), (String) data.get("name"), (String) data.get("desc"),
                                        (double) data.get("latitude"), (double) data.get("longitude"), "loc_quest", null, "quests_list");
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
                                        0, 0,"elaborate_quest", (HashMap<String, String>) data.get("tasks"), "quests_list");
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

        AutoCompleteTextView name = newQuestPopupView.findViewById(R.id.name);
        EditText desc = newQuestPopupView.findViewById(R.id.desc);

        Button search_button = newQuestPopupView.findViewById(R.id.search);
        Spinner dropdown = newQuestPopupView.findViewById(R.id.dropdown_menu);

        Button newquestpopup_save = newQuestPopupView.findViewById(R.id.create);
        Button newquestpopup_cancel = newQuestPopupView.findViewById(R.id.cancel);

        List<String> items = new ArrayList<>();
        items.add("Choose a location");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_expandable_list_item_1, items);
        dropdown.setAdapter(adapter);

        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.clear();
                loc_quests_suggestions_coords.clear();

                List<String> loc_quests_suggestions = new ArrayList<>();
                loc_quests_suggestions.add("Choose a location");
                List<Address> addressList = null;
                try {
                    addressList = geocoder.getFromLocationName(String.valueOf(name.getText()), 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(addressList != null) {
                    if (addressList.size() > 0) {
                        for(Address address : addressList) {
                            loc_quests_suggestions.add(getLocationsString(address));
                            HashMap<String, Double> coords = new HashMap<>();
                            coords.put("latitude", (double) address.getLatitude());
                            coords.put("longitude", (double) address.getLongitude());
                            loc_quests_suggestions_coords.add(coords);
                        }
                    }
                }
                adapter.addAll(loc_quests_suggestions);
            }
        });

        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i > 0) {
                    newquestpopup_save.setEnabled(true);
                    selected_item = (String) adapterView.getItemAtPosition(i);

                    HashMap<String, Double> coords = loc_quests_suggestions_coords.get(i - 1);
                    selected_item_latitude = coords.get("latitude");
                    selected_item_longitude = coords.get("longitude");
                } else {
                    if(newquestpopup_save.isEnabled())
                        newquestpopup_save.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(newquestpopup_save.isEnabled())
                    newquestpopup_save.setEnabled(false);
                if(adapter.getCount() > 1) {
                    adapter.clear();
                    adapter.add("Choose a location");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        newquestpopup_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.collection("loc_quests")
                        .whereEqualTo("name", selected_item)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    if(task.getResult().size() == 0) {
                                        //IF DOESNT EXIST, CREATE
                                        LocQuest n_lq = new LocQuest(selected_item, desc.getText().toString(), selected_item_latitude, selected_item_longitude);
                                        db.collection("loc_quests")
                                                .add(n_lq)
                                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                    @Override
                                                    public void onSuccess(DocumentReference documentReference) {
                                                        QuestFragment questFragment = new QuestFragment(documentReference.getId(),
                                                                n_lq.getName(), n_lq.getDesc(), selected_item_latitude, selected_item_longitude, "loc_quest",null, "quests_list");
                                                        childFragTrans.add(R.id.all_quests, questFragment);
                                                        childFragTrans.commit();
                                                    }
                                                })
                                                .addOnFailureListener(e -> { });
                                        dialog.dismiss();
                                    } else {
                                        Toast.makeText(requireContext(), "THIS QUEST ALREADY EXISTS", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });
            }
        });

        newquestpopup_cancel.setOnClickListener(view -> {
            dialog.dismiss();
        });

        dialogBuilder.setView(newQuestPopupView);
        dialog = dialogBuilder.create();
        dialog.show();
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
                            QuestFragment questFragment = new QuestFragment(documentReference.getId(), name.getText().toString(), desc.getText().toString(),
                                    0, 0, "elaborate_quest", tasks, "quests_list");
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

    private String getLocationsString(Address address) {
        List<String> res = new ArrayList<>();

        String str = "";
        if(address != null) {
            str = address.getFeatureName() + ", " + address.getAdminArea() + ", " +
                    address.getSubAdminArea() + ", " + address.getLocality() + ", " + address.getThoroughfare() +
                    ", " + address.getCountryName();
            /*str = address.getFeatureName() + ", " + address.getAdminArea() +
                    ", " + address.getCountryName();*/
            str = str.replaceAll("null, ", "");
            str = deDup(str);
        }

        return str;
    }

    private String deDup(String s) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Arrays.stream(s.split(", ")).distinct().collect(Collectors.joining(", "));
        }
        return s;
    }
}