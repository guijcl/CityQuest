package com.example.cityquest.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.cityquest.Activities.MainActivity;
import com.example.cityquest.Objects.ElaborateQuest;
import com.example.cityquest.Objects.LocQuest;
import com.example.cityquest.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class QuestFragment extends Fragment {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private MainActivity mainActivity;
    private HashMap<String, HashMap> user_loc_quests;
    private HashMap<String, HashMap> user_elaborate_quests;

    private View questView;

    private String currentUser;

    private final String id;
    private final String name;
    private final String desc;
    private final double latitude;
    private final double longitude;
    private final String type;
    private final HashMap<String, HashMap> quests;
    private final String meters;
    private final String time;
    private final String popularity;
    private final String experience;
    private final String cooldown;
    private final Date creation_date;

    private final String fragment_type;

    private ImageView v1;
    private ImageView v2;

    public QuestFragment(String id, String name, String desc, double latitude, double longitude,
                         String type, HashMap<String, HashMap> quests, String meters, String time,
                         String popularity, String experience, String cooldown, Date creation_date, String fragment_type) {
        this.id = id;
        this.name = name;
        this.desc = desc;
        this.latitude = latitude;
        this.longitude = longitude;
        this.type = type;
        this.quests = quests;
        this.meters = meters;
        this.time = time;
        this.popularity = popularity;
        this.experience = experience;
        this.cooldown = cooldown;
        this.creation_date = creation_date;

        this.fragment_type = fragment_type;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quest, container, false);
        this.questView = view;

        v1 = view.findViewById(R.id.type_quest1);
        v2 = view.findViewById(R.id.type_quest2);

        mainActivity = (MainActivity) getActivity();
        user_loc_quests = mainActivity.getLocQuests();
        user_elaborate_quests = mainActivity.getElaborateQuests();

        currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if(fragment_type.equals("profile_quest_list")) {
            if (type.equals("loc_quest")) {
                v2.setImageResource(R.drawable.quests_local);
            } else if (type.equals("elaborate_quest")) {
                v2.setImageResource(R.drawable.quests_elaborate);
            }

            LinearLayout profile_quest = view.findViewById(R.id.profile_quest_layout);
            if(view.findViewById(R.id.list_quest_layout).getVisibility() == View.VISIBLE)
                view.findViewById(R.id.list_quest_layout).setVisibility(View.GONE);
            profile_quest.setVisibility(View.VISIBLE);

            profile_quest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(type.equals("loc_quest"))
                        ((MainActivity) getActivity()).showLocQuestPopup(id, null, questView, "profile_quest_list");
                    else ((MainActivity) getActivity()).showElaborateQuestPopup(id, questView, "profile_quest_list");
                }
            });

            ((TextView) view.findViewById(R.id.p_quest_name)).setText(name);
        }

        if(fragment_type.equals("quests_list")) {
            if(view.findViewById(R.id.profile_quest_layout).getVisibility() == View.VISIBLE) {
                view.findViewById(R.id.profile_quest_layout).setVisibility(View.GONE);
            }
            view.findViewById(R.id.list_quest_layout).setVisibility(View.VISIBLE);

            ((TextView) view.findViewById(R.id.quest_name)).setText(name);
            ((TextView) view.findViewById(R.id.quest_desc)).setText(desc);

            Button loc_button = view.findViewById(R.id.start_local_quest);
            Button loc_button_details = view.findViewById(R.id.loc_quest_details);
            Button elaborate_button = view.findViewById(R.id.start_elaborate_quest);
            Button elaborate_button_details = view.findViewById(R.id.elaborate_quest_details);

            if (type.equals("loc_quest")) {
                v1.setImageResource(R.drawable.quests_local);
                db.collection("users").document(currentUser).
                        collection("user_loc_quests").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().exists()) {
                                loc_button.setEnabled(false);
                            } else {
                                loc_button.setOnClickListener(view12 -> {
                                    HashMap<String, String> quest = new HashMap<>();
                                    quest.put("name", name);
                                    quest.put("desc", desc);
                                    quest.put("latitude", String.valueOf(latitude));
                                    quest.put("longitude", String.valueOf(longitude));
                                    quest.put("popularity", popularity);
                                    quest.put("experience", experience);
                                    ((MainActivity) getActivity()).addLocQuest(id, quest);

                                    db.collection("users")
                                            .document(currentUser).collection("user_loc_quests").document(id)
                                            .set(new LocQuest(name, desc, latitude, longitude, popularity, experience, creation_date));
                                    loc_button.setEnabled(false);
                                });
                                for(String id_temp : user_elaborate_quests.keySet()) {
                                    if(user_elaborate_quests.get(id_temp).containsKey(id))
                                        loc_button.setEnabled(false);
                                }
                            }

                            loc_button_details.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    ((MainActivity) getActivity()).showLocQuestPopup(id, null, questView, "quests_list");
                                }
                            });
                        }
                    }
                });
            } else if (type.equals("elaborate_quest")) {
                TextView desc_layout = view.findViewById(R.id.desc);
                v1.setImageResource(R.drawable.quests_elaborate);
                if(!quests.isEmpty()) {
                    int count = 1;
                    for (String id_quest : quests.keySet()) {
                        desc_layout.setText(desc_layout.getText() + "Location " + count + " > " + (String) quests.get(id_quest).get("name") + "\n");
                        count++;
                    }
                }

                if(meters != null)
                    desc_layout.setText(desc_layout.getText() + "Meters > " + meters + "\n");
                desc_layout.setText("\n" + desc_layout.getText() + "Time to complete > " + time + " hours" + "\n");

                db.collection("users").document(currentUser).
                        collection("user_elaborate_quests").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().exists()) {
                                elaborate_button.setEnabled(false);
                            } else {
                                elaborate_button.setOnClickListener(view1 -> {
                                    HashMap<String, Object> quest = new HashMap<>();
                                    quest.put("name", name);
                                    quest.put("desc", desc);
                                    quest.put("quests", quests);
                                    quest.put("meters", meters);

                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd:HH:mm");
                                    String currentDateandTime = sdf.format(new Date());

                                    Date date = null;
                                    try {
                                        date = sdf.parse(currentDateandTime);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    Calendar calendar = Calendar.getInstance();
                                    calendar.setTime(date);
                                    calendar.add(Calendar.HOUR, Integer.parseInt(time));

                                    quest.put("time", calendar.getTime());
                                    quest.put("popularity", popularity);
                                    quest.put("experience", experience);
                                    quest.put("cooldown", cooldown);

                                    if(quests != null && meters != null) {
                                        quest.put("meters_traveled", "0");
                                        db.collection("users")
                                                .document(currentUser).collection("user_elaborate_quests").document(id)
                                                .set(new ElaborateQuest(name, desc, quests, meters, calendar.getTime().toString(), popularity, experience, cooldown, creation_date));
                                    } else if(quests != null && meters == null) {
                                        db.collection("users")
                                                .document(currentUser).collection("user_elaborate_quests").document(id)
                                                .set(new ElaborateQuest(name, desc, quests, calendar.getTime().toString(), popularity, experience, cooldown, creation_date));
                                    } else if(quests == null && meters != null) {
                                        quest.put("meters_traveled", "0");
                                        db.collection("users")
                                                .document(currentUser).collection("user_elaborate_quests").document(id)
                                                .set(new ElaborateQuest(name, desc, meters, calendar.getTime().toString(), popularity, experience, cooldown, creation_date));
                                    }

                                    ((MainActivity) getActivity()).addElaborateQuest(id, quest);

                                    if(quests != null)
                                        ((QuestsFragment) getParentFragment()).disable_loc_quest_buttons(quests);

                                    elaborate_button.setEnabled(false);

                                    ((MainActivity) getActivity()).updateElaborateQuests();
                                });
                            }
                            elaborate_button_details.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    ((MainActivity) getActivity()).showElaborateQuestPopup(id, questView, "quests_list");
                                }
                            });
                        }
                    }
                });

                db.collection("users").document(currentUser).collection("completed_elaborate_quests").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()) {
                            if(task.getResult().exists()) {
                                Date current_time = Calendar.getInstance().getTime();
                                Date cooldown_until = ((Timestamp) task.getResult().getData().get("cooldown_until")).toDate();
                                if(current_time.before(cooldown_until))
                                    elaborate_button.setEnabled(false);
                            }
                        }
                    }
                });
            }

            LinearLayout clickable_layout = view.findViewById(R.id.list_quest_layout);
            clickable_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinearLayout card_v = null;
                    if (type.equals("loc_quest"))
                        card_v = view.findViewById(R.id.extra_card_loc);
                    else if (type.equals("elaborate_quest"))
                        card_v = view.findViewById(R.id.extra_card_elaborate);

                    if (card_v != null) {
                        if (card_v.getVisibility() == View.VISIBLE) {
                            clickable_layout.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.alert_dialog_profilepic_shape));
                            card_v.setVisibility(View.GONE);
                        } else if (card_v.getVisibility() == View.GONE){
                            card_v.setVisibility(View.VISIBLE);
                            clickable_layout.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.quests_frag_shape));
                        }
                    }
                }
            });


        }

        return view;
    }

    public void disableQuestButton() {
        Button loc_button = questView.findViewById(R.id.start_local_quest);
        Button elaborate_button = questView.findViewById(R.id.start_elaborate_quest);
        loc_button.setEnabled(false);
        elaborate_button.setEnabled(false);
    }
}