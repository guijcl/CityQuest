package com.example.cityquest.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.cityquest.Prevalent.Prevalent;
import com.example.cityquest.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import io.paperdb.Paper;

public class ProfileFragment extends Fragment {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();

    private CircleImageView profileImage;
    private FloatingActionButton buttonAddImage;

    public ProfileFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        //VERIFICAR COM ID SO USER: FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").document(currentUser).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    HashMap data = (HashMap) document.getData();
                    String emailSaved = Paper.book().read(Prevalent.UserEmailKey);
                    TextView username = view.findViewById(R.id.username_profile);
                    TextView email = view.findViewById(R.id.email_profile);
                    CircleImageView profileImage = view.findViewById(R.id.profileImage);

                    username.setText(data.get("username").toString());
                    email.setText(data.get("email").toString());
                    //Picasso.get().load(data.get("profileImage").toString()).into(profileImage);
                }
            }
        });

        db.collection("users").document(currentUser).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.getResult().exists()) {
                    DocumentSnapshot document = task.getResult();
                    HashMap data = (HashMap) document.getData();
                    TextView ranking_number = view.findViewById(R.id.ranking_number);
                    ranking_number.setText((String) data.get("ranking"));
                }
            }
        });

        profileImage = view.findViewById(R.id.profileImage);
        buttonAddImage = view.findViewById(R.id.buttonAddImage);

        buttonAddImage.setOnClickListener(view1 -> {
            //Fazer upload da imagem de perfil
        });

        FragmentManager childFragMan = getChildFragmentManager();
        loadLocQuests(childFragMan);
        loadElaborateQuests(childFragMan);

        return view;
    }

    private void loadLocQuests(FragmentManager childFragMan) {
        db.collection("users").document(currentUser).collection("user_loc_quests").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            FragmentTransaction childFragTrans = childFragMan.beginTransaction();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                HashMap data = (HashMap) document.getData();
                                QuestFragment questFragment = new QuestFragment(document.getId(), (String) data.get("name"),
                                        (String) data.get("desc"), (double) data.get("latitude"), (double) data.get("longitude"),
                                        "loc_quest", null, null, null, (String) data.get("popularity"),
                                        (String) data.get("experience"), null, ((Timestamp) data.get("creationDate")).toDate()
                                        , "profile_quest_list");

                                childFragTrans.add(R.id.user_quests, questFragment, document.getId() + " active");
                            }
                            childFragTrans.commit();
                        } else {
                            Log.w("ERROR", "Error getting documents.", task.getException());
                        }
                    }
                });

        db.collection("users").document(currentUser).collection("completed_loc_quests").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                db.collection("loc_quests").document(document.getId()).get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if(task.getResult().exists()) {
                                                    FragmentTransaction childFragTrans = childFragMan.beginTransaction();
                                                    HashMap data = (HashMap) task.getResult().getData();
                                                    QuestFragment questFragment = new QuestFragment(document.getId(), (String) data.get("name"),
                                                            (String) data.get("desc"), (double) data.get("latitude"), (double) data.get("longitude"),
                                                            "loc_quest", null, null, null, (String) data.get("popularity"),
                                                            (String) data.get("experience"), null, ((Timestamp) data.get("creationDate")).toDate()
                                                            , "profile_quest_list");

                                                    childFragTrans.add(R.id.user_completed_quests, questFragment, document.getId() + " completed");

                                                    childFragTrans.commit();
                                                }
                                            }
                                        });
                            }
                        }
                    }
                });
    }

    private void loadElaborateQuests(FragmentManager childFragMan) {
        db.collection("users").document(currentUser).collection("user_elaborate_quests").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            FragmentTransaction childFragTrans = childFragMan.beginTransaction();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                db.collection("elaborate_quests").document(document.getId()).get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if(task.getResult().exists()) {
                                                    HashMap data = (HashMap) task.getResult().getData();
                                                    QuestFragment questFragment = new QuestFragment(document.getId(), (String) data.get("name"),
                                                            (String) data.get("desc"), 0, 0, "elaborate_quest",
                                                            (HashMap<String, HashMap>) data.get("quests"), (String) data.get("meters"),
                                                            (String) data.get("time"), (String) data.get("popularity"), (String) data.get("experience"),
                                                            (String) data.get("cooldown"), ((Timestamp) data.get("creationDate")).toDate(),
                                                            "profile_quest_list");
                                                    childFragTrans.add(R.id.user_quests, questFragment, document.getId() + " active");

                                                    childFragTrans.commit();
                                                }
                                            }
                                        });
                            }
                        } else {
                            Log.w("ERROR", "Error getting documents.", task.getException());
                        }
                    }
                });

        db.collection("users").document(currentUser).collection("completed_elaborate_quests").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                db.collection("elaborate_quests").document(document.getId()).get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if(task.getResult().exists()) {
                                                    FragmentTransaction childFragTrans = childFragMan.beginTransaction();
                                                    HashMap data = (HashMap) task.getResult().getData();
                                                    QuestFragment questFragment = new QuestFragment(document.getId(), (String) data.get("name"),
                                                            (String) data.get("desc"), 0, 0, "elaborate_quest",
                                                            (HashMap<String, HashMap>) data.get("quests"), (String) data.get("meters"),
                                                            (String) data.get("time"), (String) data.get("popularity"), (String) data.get("experience"),
                                                            (String) data.get("cooldown"), ((Timestamp) data.get("creationDate")).toDate(),
                                                            "profile_quest_list");

                                                    childFragTrans.add(R.id.user_completed_quests, questFragment, document.getId() + " completed");

                                                    childFragTrans.commit();
                                                }
                                            }
                                        });
                            }
                        }
                    }
                });
    }

}