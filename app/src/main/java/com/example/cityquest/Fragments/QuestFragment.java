package com.example.cityquest.Fragments;

import android.animation.LayoutTransition;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.cityquest.Objects.ElaborateQuest;
import com.example.cityquest.Objects.LocQuest;
import com.example.cityquest.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class QuestFragment extends Fragment {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String currentUser;

    private final String id;
    private final String name;
    private final String desc;
    private final double latitude;
    private final double longitude;
    private final String type;
    private final HashMap<String, String> quests;
    private final String meters;
    private final String time;
    private final String fragment_type;

    public QuestFragment(String id, String name, String desc, double latitude, double longitude,
                         String type, HashMap<String, String> quests, String meters, String time, String fragment_type) {
        this.id = id;
        this.name = name;
        this.desc = desc;
        this.latitude = latitude;
        this.longitude = longitude;
        this.type = type;
        this.quests = quests;
        this.meters = meters;
        this.time = time;
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

        currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if(fragment_type.equals("profile_quest_list")) {
            if(view.findViewById(R.id.list_quest_layout).getVisibility() == View.VISIBLE)
                view.findViewById(R.id.list_quest_layout).setVisibility(View.GONE);
            view.findViewById(R.id.profile_quest_layout).setVisibility(View.VISIBLE);

            String[] strs = name.split(", ");
            String str_name = strs[0];
            ((TextView) view.findViewById(R.id.p_quest_name)).setText(str_name);
        }

        if(fragment_type.equals("quests_list")) {
            if(view.findViewById(R.id.profile_quest_layout).getVisibility() == View.VISIBLE)
                view.findViewById(R.id.profile_quest_layout).setVisibility(View.GONE);
            view.findViewById(R.id.list_quest_layout).setVisibility(View.VISIBLE);

            ((TextView) view.findViewById(R.id.quest_name)).setText(name);
            ((TextView) view.findViewById(R.id.quest_desc)).setText(desc);

            if (type.equals("elaborate_quest")) {
                /*((TextView) view.findViewById(R.id.task_1)).setText(quests.get("Task_1"));
                ((TextView) view.findViewById(R.id.task_2)).setText(quests.get("Task_2"));
                ((TextView) view.findViewById(R.id.task_3)).setText(quests.get("Task_3"));
                ((TextView) view.findViewById(R.id.task_4)).setText(quests.get("Task_4"));*/
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
                        if (card_v.getVisibility() == View.VISIBLE)
                            card_v.setVisibility(View.GONE);
                        else if (card_v.getVisibility() == View.GONE)
                            card_v.setVisibility(View.VISIBLE);
                    }

                }
            });

            Button loc_button = view.findViewById(R.id.start_local_quest);
            Button elaborate_button = view.findViewById(R.id.start_elaborate_quest);
            db.collection("users").document(currentUser).
                    collection("user_quests").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            loc_button.setEnabled(false);
                            elaborate_button.setEnabled(false);
                        } else {
                            loc_button.setOnClickListener(view12 -> {
                                db.collection("users")
                                        .document(currentUser).collection("user_loc_quests").document(id).set(new LocQuest(name, desc, latitude, longitude));
                                loc_button.setEnabled(false);
                                elaborate_button.setEnabled(false);
                            });

                            elaborate_button.setOnClickListener(view1 -> {
                                if(quests != null && meters != null)
                                    db.collection("users")
                                            .document(currentUser).collection("user_elaborate_quests").document(id).set(new ElaborateQuest(name, desc, quests, meters, time));
                                else if(quests != null && meters == null)
                                    db.collection("users")
                                            .document(currentUser).collection("user_elaborate_quests").document(id).set(new ElaborateQuest(name, desc, quests, time));
                                else if(quests == null && meters != null)
                                    db.collection("users")
                                            .document(currentUser).collection("user_elaborate_quests").document(id).set(new ElaborateQuest(name, desc, meters, time));
                                loc_button.setEnabled(false);
                                elaborate_button.setEnabled(false);
                            });
                        }
                    }
                }
            });
        }

        return view;
    }
}