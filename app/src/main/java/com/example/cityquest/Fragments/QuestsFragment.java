package com.example.cityquest.Fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cityquest.Activities.MainActivity;
import com.example.cityquest.Activities.SignIn;
import com.example.cityquest.Objects.ElaborateQuest;
import com.example.cityquest.Objects.LocQuest;
import com.example.cityquest.Prevalent.Prevalent;
import com.example.cityquest.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class QuestsFragment extends Fragment {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String currentUser;

    private Geocoder geocoder;

    private MainActivity mainActivity;

    private final String[] categoryItems = {"All Quests", "Local Quests", "Elaborate Quests"};
    private String lastCategorySelected = null;

    private final String[] orderByItems = {"Latest Quests", "Oldest Quests", "Most Popular Quests"};
    private String lastOrderSelected = null;

    boolean countdownTimerRunning;

    private HashMap<String, HashMap> all_loc_quests;

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
        View fragmentView = inflater.inflate(R.layout.fragment_quests, container, false);

        currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mainActivity = (MainActivity) getActivity();

        geocoder = new Geocoder(this.requireContext());

        all_loc_quests = new HashMap<>();

        FragmentManager childFragMan = getChildFragmentManager();

        loadLocQuests(childFragMan);
        loadElaborateQuests(childFragMan);

        TextView search_bar = fragmentView.findViewById(R.id.search_bar);

        countdownTimerRunning = false;
        CountDownTimer timer = new CountDownTimer(2000, 1000)  {
            @Override
            public void onTick(long l) {}

            @Override
            public void onFinish() {
                LinearLayout linearLayout = fragmentView.findViewById(R.id.all_quests);
                linearLayout.removeAllViews();
                searchLocQuests(childFragMan, search_bar.getText().toString());
                searchElaborateQuests(childFragMan, search_bar.getText().toString());
            }
        };

        search_bar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(countdownTimerRunning) {
                    timer.cancel();
                    countdownTimerRunning = false;
                }
                timer.start();
                countdownTimerRunning = true;
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        AutoCompleteTextView autoCompleteTextCat = fragmentView.findViewById(R.id.autoCompleteOne);
        ArrayAdapter<String> adapterItemsCat = new ArrayAdapter<>(getActivity(),R.layout.list_item_questsfrag, categoryItems);
        autoCompleteTextCat.setAdapter(adapterItemsCat);
        autoCompleteTextCat.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                LinearLayout linearLayout = fragmentView.findViewById(R.id.all_quests);
                linearLayout.removeAllViews();

                String item = adapterView.getItemAtPosition(i).toString();
                if(lastOrderSelected != null) {
                    if (item.equals("Local Quests")) {
                        loadLocQuests(childFragMan, lastOrderSelected);
                    } else if(item.equals("Elaborate Quests")){
                        loadElaborateQuests(childFragMan, lastOrderSelected);
                    } else {
                        loadLocQuests(childFragMan, lastOrderSelected);
                        loadElaborateQuests(childFragMan, lastOrderSelected);
                    }
                } else {
                    if (item.equals("Local Quests")) {
                        loadLocQuests(childFragMan);
                    } else if(item.equals("Elaborate Quests")){
                        loadElaborateQuests(childFragMan);
                    } else {
                        loadLocQuests(childFragMan);
                        loadElaborateQuests(childFragMan);
                    }
                }
                lastCategorySelected = item;
            }
        });

        AutoCompleteTextView autoCompleteTextOrder = fragmentView.findViewById(R.id.autoCompleteTwo);
        ArrayAdapter<String> adapterItemsOrder = new ArrayAdapter<>(getActivity(),R.layout.list_item_questsfrag, orderByItems);
        autoCompleteTextOrder.setAdapter(adapterItemsOrder);
        autoCompleteTextOrder.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String item = adapterView.getItemAtPosition(i).toString();
                LinearLayout linearLayout = fragmentView.findViewById(R.id.all_quests);
                linearLayout.removeAllViews();
                if(lastCategorySelected.equals("Local Quests"))
                    loadLocQuests(childFragMan, item);
                else loadElaborateQuests(childFragMan, item);
                lastOrderSelected = item;
            }
        });

        Button create_local_quest = fragmentView.findViewById(R.id.local_quest);
        create_local_quest.setOnClickListener(view1 -> {
            createNewLocalQuestDialog();
        });

        Button create_elaborate_quest = fragmentView.findViewById(R.id.elaborate_quest);
        create_elaborate_quest.setOnClickListener(view1 -> {
            createNewElaborateQuestDialog();
        });

        return fragmentView;
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
                            coords.put("latitude", address.getLatitude());
                            coords.put("longitude", address.getLongitude());
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
                                        Date creation_date = Calendar.getInstance().getTime();
                                        LocQuest n_lq = new LocQuest(selected_item, desc.getText().toString(), selected_item_latitude, selected_item_longitude, "0", "50", creation_date);
                                        db.collection("loc_quests")
                                                .add(n_lq)
                                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                    @Override
                                                    public void onSuccess(DocumentReference documentReference) {
                                                        QuestFragment questFragment = new QuestFragment(documentReference.getId(),
                                                                n_lq.getName(), n_lq.getDesc(), selected_item_latitude, selected_item_longitude,
                                                                "loc_quest",null, null, null, "0", "50", null, creation_date, "quests_list");

                                                        HashMap temp_hash = new HashMap();
                                                        temp_hash.put("id", documentReference.getId());
                                                        temp_hash.put("latitude", selected_item_latitude);
                                                        temp_hash.put("longitude", selected_item_longitude);
                                                        all_loc_quests.put((String) n_lq.getName(), temp_hash);

                                                        childFragTrans.add(R.id.all_quests, questFragment, documentReference.getId());
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

        List<String> quest_names = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_expandable_list_item_1, quest_names);
        db.collection("loc_quests").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        HashMap data = (HashMap) document.getData();
                        quest_names.add((String) data.get("name"));
                    }
                }
            }
        });

        List<Boolean> check1 = new ArrayList<>();
        check1.add(false);
        check1.add(false);

        List<Boolean> check2 = new ArrayList<>();
        check2.add(false);
        check2.add(false);
        check2.add(false);
        check2.add(false);

        EditText name = newQuestPopupView.findViewById(R.id.name);
        EditText desc = newQuestPopupView.findViewById(R.id.desc);

        Button newquestpopup_save = newQuestPopupView.findViewById(R.id.create);
        Button newquestpopup_cancel = newQuestPopupView.findViewById(R.id.cancel);

        LinearLayout task1 = newQuestPopupView.findViewById(R.id.task_1);
        Button add_quest = newQuestPopupView.findViewById(R.id.add_quest);

        CheckBox checkBox1 = newQuestPopupView.findViewById(R.id.checkbox_1);
        CheckBox checkBox2 = newQuestPopupView.findViewById(R.id.checkbox_2);

        AutoCompleteTextView quest1 = newQuestPopupView.findViewById(R.id.quest_1);
        EditText meters = newQuestPopupView.findViewById(R.id.meters);
        EditText time = newQuestPopupView.findViewById(R.id.time_to_complete);
        EditText cooldown = newQuestPopupView.findViewById(R.id.cooldown_time);

        List<Boolean> check_quests = new ArrayList<>();
        check_quests.add(false);

        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String s = charSequence.toString();
                if(s.length() > 0) {
                    if (!check1.get(0)) {
                        check1.set(0, true);
                        check_elaborate_quest(newquestpopup_save, add_quest, meters, check1, check2);
                    }
                } else {
                    if (check1.get(0)) {
                        check1.set(0, false);
                        check_elaborate_quest(newquestpopup_save, add_quest, meters, check1, check2);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        desc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String s = charSequence.toString();
                if(s.length() > 0) {
                    if (!check1.get(1)) {
                        check1.set(1, true);
                        check_elaborate_quest(newquestpopup_save, add_quest, meters, check1, check2);
                    }
                } else {
                    if (check1.get(1)) {
                        check1.set(1, false);
                        check_elaborate_quest(newquestpopup_save, add_quest, meters, check1, check2);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        checkBox1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(add_quest.isEnabled()) {
                    add_quest.setEnabled(false);
                    for(int i = 0; i < task1.getChildCount(); i++) {
                        if(i != 0)
                            task1.getChildAt(i).setEnabled(false);
                    }
                } else {
                    add_quest.setEnabled(true);
                    for(int i = 0; i < task1.getChildCount(); i++) {
                        if(i != 0)
                            task1.getChildAt(i).setEnabled(true);
                    }
                }

                check_elaborate_quest(newquestpopup_save, add_quest, meters, check1, check2);
            }
        });

        quest1.setAdapter(adapter);
        quest1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                check_quests.set(0, false);
                if(newquestpopup_save.isEnabled())
                    newquestpopup_save.setEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        quest1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                check_quests.set(0, true);
                if(!check_quests.contains(false)) {
                    check2.set(0, true);

                    check_elaborate_quest(newquestpopup_save, add_quest, meters, check1, check2);
                }
            }
        });

        List<String> used_quest_names = new ArrayList<>();
        add_quest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(task1.getChildCount() <= 5) {
                    int id_num = task1.getChildCount();

                    AutoCompleteTextView new_quest = new AutoCompleteTextView(newQuestPopupView.getContext());
                    new_quest.setHint("Quest " + id_num);
                    check_quests.add(false);

                    check2.set(0, false);
                    if(newquestpopup_save.isEnabled())
                        newquestpopup_save.setEnabled(false);

                    new_quest.setAdapter(adapter);
                    new_quest.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            check_quests.set(id_num - 1, false);
                            if(newquestpopup_save.isEnabled())
                                newquestpopup_save.setEnabled(false);

                            if(used_quest_names.size() >= id_num - 1)
                                used_quest_names.remove(id_num - 2);
                        }

                        @Override
                        public void afterTextChanged(Editable editable) {}
                    });

                    new_quest.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            String q_name = new_quest.getText().toString();
                            if(!used_quest_names.contains(q_name) && !q_name.equals(quest1.getText().toString())) {
                                if(used_quest_names.size() >= id_num - 1)
                                    used_quest_names.set(id_num - 2, q_name);
                                else used_quest_names.add(q_name);

                                check_quests.set(id_num - 1, true);
                                if (!check_quests.contains(false)) {
                                    check2.set(0, true);

                                    check_elaborate_quest(newquestpopup_save, add_quest, meters, check1, check2);
                                }
                            } else {
                                new_quest.setText("");
                                Toast.makeText(requireContext(), "QUEST HAS ALREADY BEEN CHOSEN", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    task1.addView(new_quest);
                } else {
                    Toast.makeText(newQuestPopupView.getContext(), "TOO MANY QUESTS", Toast.LENGTH_SHORT);
                }
            }
        });

        checkBox2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(meters.isEnabled())
                    meters.setEnabled(false);
                else
                    meters.setEnabled(true);

                check_elaborate_quest(newquestpopup_save, add_quest, meters, check1, check2);
            }
        });

        meters.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String str = charSequence.toString();
                if(str.length() > 0 && str.matches("[0-9]+"))
                    check2.set(1, true);
                else {
                    check2.set(1, false);
                    if(newquestpopup_save.isEnabled())
                        newquestpopup_save.setEnabled(false);
                }

                check_elaborate_quest(newquestpopup_save, add_quest, meters, check1, check2);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        time.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String str = charSequence.toString();
                if(str.length() > 0 && str.matches("[0-9]+"))
                    check2.set(2, true);
                else {
                    check2.set(2, false);
                    if(newquestpopup_save.isEnabled())
                        newquestpopup_save.setEnabled(false);
                }

                check_elaborate_quest(newquestpopup_save, add_quest, meters, check1, check2);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        cooldown.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String str = charSequence.toString();
                if(str.length() > 0 && str.matches("[0-9]+"))
                    check2.set(3, true);
                else {
                    check2.set(3, false);
                    if(newquestpopup_save.isEnabled())
                        newquestpopup_save.setEnabled(false);
                }

                check_elaborate_quest(newquestpopup_save, add_quest, meters, check1, check2);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        dialogBuilder.setView(newQuestPopupView);
        dialog = dialogBuilder.create();
        dialog.show();

        newquestpopup_save.setOnClickListener(view -> {
            HashMap<String, HashMap> quests = new HashMap<>();
            for(int i = 0; i < task1.getChildCount(); i++) {
                if(i != 0) {
                    if(all_loc_quests.containsKey(((AutoCompleteTextView) task1.getChildAt(i)).getText().toString())) {
                        HashMap<String, Object> info = new HashMap<>();
                        info.put("name", ((AutoCompleteTextView) task1.getChildAt(i)).getText().toString());
                        info.put("latitude", String.valueOf(all_loc_quests.get(((AutoCompleteTextView) task1.getChildAt(i)).getText().toString()).get("latitude")));
                        info.put("longitude", String.valueOf(all_loc_quests.get(((AutoCompleteTextView) task1.getChildAt(i)).getText().toString()).get("longitude")));
                        info.put("done", false);
                        quests.put((String) all_loc_quests.get(((AutoCompleteTextView) task1.getChildAt(i)).getText().toString()).get("id"), info);
                    }
                }
            }

            String temp_meters;
            if(meters.getText().toString().equals(""))
                temp_meters = null;
            else temp_meters = meters.getText().toString();
            db.collection("elaborate_quests").whereEqualTo("quests", quests).whereEqualTo("meters", temp_meters)
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()) {
                        if(task.getResult().isEmpty()) {
                            ElaborateQuest n_eq = null;
                            int experience = 0;
                            Date creation_date = Calendar.getInstance().getTime();
                            if(check2.get(0) && check2.get(1)) {
                                experience += 50 * quests.size();
                                experience += Integer.parseInt(meters.getText().toString()) * 0.1;
                                n_eq = new ElaborateQuest(name.getText().toString(), desc.getText().toString(), quests,
                                        meters.getText().toString(), time.getText().toString(), "0",
                                        String.valueOf(experience), cooldown.getText().toString(), creation_date);
                            } else if(check2.get(0)) {
                                experience += 50 * quests.size();
                                n_eq = new ElaborateQuest(name.getText().toString(), desc.getText().toString(), quests,
                                        time.getText().toString(), "0", String.valueOf(experience),
                                        cooldown.getText().toString(), creation_date);
                            } else if(check2.get(1)) {
                                experience += Long.parseLong(meters.getText().toString()) * 0.1;
                                n_eq = new ElaborateQuest(name.getText().toString(), desc.getText().toString(),
                                        meters.getText().toString(), time.getText().toString(), "0",
                                        String.valueOf(experience), cooldown.getText().toString(), creation_date);
                            }

                            int finalExperience = experience;
                            db.collection("elaborate_quests")
                                    .add(n_eq)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            String temp_meters = null;
                                            if(!meters.getText().toString().equals(""))
                                                temp_meters = meters.getText().toString();

                                            QuestFragment questFragment = new QuestFragment(documentReference.getId(), name.getText().toString(),
                                                    desc.getText().toString(), 0, 0, "elaborate_quest", quests,
                                                    temp_meters, time.getText().toString(), "0", String.valueOf(finalExperience),
                                                    cooldown.getText().toString(), creation_date, "quests_list");
                                            childFragTrans.add(R.id.all_quests, questFragment, documentReference.getId());
                                            childFragTrans.commit();
                                        }
                                    })
                                    .addOnFailureListener(e -> { });

                            dialog.dismiss();
                        } else {
                            Toast.makeText(requireContext(), "THIS QUEST ALREADY EXISTS", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });

        });

        newquestpopup_cancel.setOnClickListener(view -> {
            dialog.dismiss();
        });
    }

    private void loadLocQuests(FragmentManager childFragMan) {
        db.collection(  "loc_quests").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            FragmentTransaction childFragTrans = childFragMan.beginTransaction();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                HashMap data = (HashMap) document.getData();
                                QuestFragment questFragment = new QuestFragment(document.getId(), (String) data.get("name"), (String) data.get("desc"),
                                        (double) data.get("latitude"), (double) data.get("longitude"), "loc_quest", null, null, null,
                                        (String) data.get("popularity"), (String) data.get("experience"), null, ((Timestamp) data.get("creationDate")).toDate(), "quests_list");

                                childFragTrans.add(R.id.all_quests, questFragment, document.getId());

                                HashMap temp_hash = new HashMap();
                                temp_hash.put("id", document.getId());
                                temp_hash.put("latitude", data.get("latitude"));
                                temp_hash.put("longitude", data.get("longitude"));
                                all_loc_quests.put((String) data.get("name"), temp_hash);
                            }
                            childFragTrans.commit();
                        } else {
                            Log.w("ERROR", "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    private void loadElaborateQuests(FragmentManager childFragMan) {
        db.collection("elaborate_quests").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            FragmentTransaction childFragTrans = childFragMan.beginTransaction();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                HashMap data = (HashMap) document.getData();
                                QuestFragment questFragment = new QuestFragment(document.getId(), (String) data.get("name"), (String) data.get("desc"), 0, 0,
                                        "elaborate_quest", (HashMap<String, HashMap>) data.get("quests"), (String) data.get("meters"), (String) data.get("time"),
                                        (String) data.get("popularity"), (String) data.get("experience"), (String) data.get("cooldown"), ((Timestamp) data.get("creationDate")).toDate(), "quests_list");
                                childFragTrans.add(R.id.all_quests, questFragment, document.getId());
                            }
                            childFragTrans.commit();
                        } else {
                            Log.w("ERROR", "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    private void loadLocQuests(FragmentManager childFragMan, String orderBy) {
        if(orderBy.equals("Latest Quests") || orderBy.equals("Oldest Quests")) {
            Query.Direction direction;
            if(orderBy.equals("Latest Quests")) direction = Query.Direction.DESCENDING;
            else direction = Query.Direction.ASCENDING;
            db.collection("loc_quests").orderBy("creationDate", direction).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        FragmentTransaction childFragTrans = childFragMan.beginTransaction();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            HashMap data = (HashMap) document.getData();
                            QuestFragment questFragment = new QuestFragment(document.getId(), (String) data.get("name"), (String) data.get("desc"),
                                    (double) data.get("latitude"), (double) data.get("longitude"), "loc_quest", null, null, null,
                                    (String) data.get("popularity"), (String) data.get("experience"), null, ((Timestamp) data.get("creationDate")).toDate(), "quests_list");

                            childFragTrans.add(R.id.all_quests, questFragment, document.getId());

                            HashMap temp_hash = new HashMap();
                            temp_hash.put("id", document.getId());
                            temp_hash.put("latitude", data.get("latitude"));
                            temp_hash.put("longitude", data.get("longitude"));
                            all_loc_quests.put((String) data.get("name"), temp_hash);
                        }
                        childFragTrans.commit();
                    } else {
                        Log.w("ERROR", "Error getting documents.", task.getException());
                    }
                }
            });
        } else if(orderBy.equals("Most Popular Quests")) {
            db.collection("loc_quests").orderBy("popularity", Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        FragmentTransaction childFragTrans = childFragMan.beginTransaction();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            HashMap data = (HashMap) document.getData();
                            QuestFragment questFragment = new QuestFragment(document.getId(), (String) data.get("name"), (String) data.get("desc"),
                                    (double) data.get("latitude"), (double) data.get("longitude"), "loc_quest", null, null, null,
                                    (String) data.get("popularity"), (String) data.get("experience"), null, ((Timestamp) data.get("creationDate")).toDate(), "quests_list");

                            childFragTrans.add(R.id.all_quests, questFragment, document.getId());

                            HashMap temp_hash = new HashMap();
                            temp_hash.put("id", document.getId());
                            temp_hash.put("latitude", data.get("latitude"));
                            temp_hash.put("longitude", data.get("longitude"));
                            all_loc_quests.put((String) data.get("name"), temp_hash);
                        }
                        childFragTrans.commit();
                    } else {
                        Log.w("ERROR", "Error getting documents.", task.getException());
                    }
                }
            });
        }
    }

    private void loadElaborateQuests(FragmentManager childFragMan, String orderBy) {
        if(orderBy.equals("Latest Quests") || orderBy.equals("Oldest Quests")) {
            Query.Direction direction;
            if(orderBy.equals("Latest Quests")) direction = Query.Direction.DESCENDING;
            else direction = Query.Direction.ASCENDING;
            db.collection("elaborate_quests").orderBy("creationDate", direction).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        FragmentTransaction childFragTrans = childFragMan.beginTransaction();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            HashMap data = (HashMap) document.getData();
                            QuestFragment questFragment = new QuestFragment(document.getId(), (String) data.get("name"),
                                    (String) data.get("desc"), 0, 0, "elaborate_quest", (HashMap<String, HashMap>) data.get("quests"),
                                    (String) data.get("meters"), (String) data.get("time"), (String) data.get("popularity"), (String) data.get("experience"),
                                    (String) data.get("cooldown"), ((Timestamp) data.get("creationDate")).toDate(), "quests_list");
                            childFragTrans.add(R.id.all_quests, questFragment, document.getId());
                        }
                        childFragTrans.commit();
                    } else {
                        Log.w("ERROR", "Error getting documents.", task.getException());
                    }
                }
            });
        } else if(orderBy.equals("Most Popular Quests")) {
            db.collection("elaborate_quests").orderBy("popularity", Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        FragmentTransaction childFragTrans = childFragMan.beginTransaction();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            HashMap data = (HashMap) document.getData();
                            QuestFragment questFragment = new QuestFragment(document.getId(), (String) data.get("name"),
                                    (String) data.get("desc"), 0, 0, "elaborate_quest",
                                    (HashMap<String, HashMap>) data.get("quests"), (String) data.get("meters"), (String) data.get("time"),
                                    (String) data.get("popularity"), (String) data.get("experience"), (String) data.get("cooldown"),
                                    ((Timestamp) data.get("creationDate")).toDate(), "quests_list");
                            childFragTrans.add(R.id.all_quests, questFragment, document.getId());
                        }
                        childFragTrans.commit();
                    } else {
                        Log.w("ERROR", "Error getting documents.", task.getException());
                    }
                }
            });
        }
    }

    private void searchLocQuests(FragmentManager childFragMan, String substring) {
        db.collection(  "loc_quests").whereGreaterThanOrEqualTo("name", substring)
                .whereLessThanOrEqualTo("name", substring + "\uF7FF").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            FragmentTransaction childFragTrans = childFragMan.beginTransaction();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                HashMap data = (HashMap) document.getData();
                                QuestFragment questFragment = new QuestFragment(document.getId(), (String) data.get("name"), (String) data.get("desc"),
                                        (double) data.get("latitude"), (double) data.get("longitude"), "loc_quest", null, null, null,
                                        (String) data.get("popularity"), (String) data.get("experience"), null, ((Timestamp) data.get("creationDate")).toDate(), "quests_list");

                                childFragTrans.add(R.id.all_quests, questFragment, document.getId());

                                HashMap temp_hash = new HashMap();
                                temp_hash.put("id", document.getId());
                                temp_hash.put("latitude", data.get("latitude"));
                                temp_hash.put("longitude", data.get("longitude"));
                                all_loc_quests.put((String) data.get("name"), temp_hash);
                            }
                            childFragTrans.commit();
                        } else {
                            Log.w("ERROR", "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    private void searchElaborateQuests(FragmentManager childFragMan, String substring) {
        db.collection(  "elaborate_quests").whereGreaterThanOrEqualTo("name", substring)
                .whereLessThanOrEqualTo("name", substring + "\uF7FF").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            FragmentTransaction childFragTrans = childFragMan.beginTransaction();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                HashMap data = (HashMap) document.getData();
                                QuestFragment questFragment = new QuestFragment(document.getId(), (String) data.get("name"), (String) data.get("desc"), 0, 0,
                                        "elaborate_quest", (HashMap<String, HashMap>) data.get("quests"), (String) data.get("meters"), (String) data.get("time"),
                                        (String) data.get("popularity"), (String) data.get("experience"), (String) data.get("cooldown"), ((Timestamp) data.get("creationDate")).toDate(), "quests_list");
                                childFragTrans.add(R.id.all_quests, questFragment, document.getId());
                            }
                            childFragTrans.commit();
                        } else {
                            Log.w("ERROR", "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    private void check_elaborate_quest(View newquestpopup_save, View add_quest, View meters, List<Boolean> check1, List<Boolean> check2) {
        if(check1.contains(false)) {
            if(newquestpopup_save.isEnabled())
                newquestpopup_save.setEnabled(false);
            return;
        }
        if(add_quest.isEnabled() && meters.isEnabled()) {
            if (!check2.contains(false))
                newquestpopup_save.setEnabled(true);
            else newquestpopup_save.setEnabled(false);
        } else if(add_quest.isEnabled() && !meters.isEnabled()) {
            if(check2.get(0) && check2.get(2) && check2.get(3))
                newquestpopup_save.setEnabled(true);
            else newquestpopup_save.setEnabled(false);
        } else if(!add_quest.isEnabled() && meters.isEnabled()) {
            if(check2.get(1) && check2.get(2) && check2.get(3))
                newquestpopup_save.setEnabled(true);
            else newquestpopup_save.setEnabled(false);
        } else {
            if(newquestpopup_save.isEnabled())
                newquestpopup_save.setEnabled(false);
        }
    }

    public void disable_loc_quest_buttons(HashMap<String, HashMap> list) {
        for(String id : list.keySet()) {
            ((QuestFragment) getChildFragmentManager().findFragmentByTag(id)).disableQuestButton();
            mainActivity.deleteLocQuest(id);
            db.collection("users").document(currentUser).
                    collection("user_loc_quests").document(id).delete();
        }
    }

    private String getLocationsString(Address address) {
        String str = "";
        if(address != null) {
            str = address.getFeatureName() + ", " + address.getAdminArea() + ", " +
                    address.getSubAdminArea() + ", " + address.getLocality() + ", " + address.getThoroughfare() +
                    ", " + address.getCountryName();
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