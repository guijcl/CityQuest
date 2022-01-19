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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import io.paperdb.Paper;

public class ProfileForUsersFragment extends Fragment {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();

    private CircleImageView profileImage;
    private FloatingActionButton buttonAddImage;

    public ProfileForUsersFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view =inflater.inflate(R.layout.fragment_profile, container, false);


        //--------------------------SET USERNAME, EMAIL AND PHOTO ON TOP----------------------------
        TextView username = view.findViewById(R.id.username_profile);
        TextView email = view.findViewById(R.id.email_profile);
        //CircleImageView profileImage = view.findViewById(R.id.profileImage);

        username.setText(Paper.book().read(Prevalent.UserUsernameKey));
        email.setText(Paper.book().read(Prevalent.UserEmailKey));
        //Picasso.get().load(data.get("profileImage").toString()).into(profileImage);
        //------------------------------------------------------------------------------------------

        profileImage = view.findViewById(R.id.profileImage);
        buttonAddImage = view.findViewById(R.id.buttonAddImage);

        buttonAddImage.setOnClickListener(view1 -> {
            //Fazer upload da imagem de perfil
        });

        /*
        FragmentManager childFragMan = getChildFragmentManager();
        db.collection("users").document(currentUser).collection("user_quests").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            FragmentTransaction childFragTrans = childFragMan.beginTransaction();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                HashMap data = (HashMap) document.getData();
                                /*QuestFragment questFragment = new QuestFragment(document.getId(), (String) data.get("name"), (String) data.get("desc"),
                                        (double) data.get("latitude"), (double) data.get("longitude"), "loc_quest", null, null, null, "profile_quest_list");
                                childFragTrans.add(R.id.user_quests, questFragment);*/
                            }
                            childFragTrans.commit();
                        } else {
                            Log.w("ERROR", "Error getting documents.", task.getException());
                        }
                    }
                });

        db.collection("users").document(currentUser).collection("user_completed_quests").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            FragmentTransaction childFragTrans = childFragMan.beginTransaction();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                HashMap data = (HashMap) document.getData();
                                /*QuestFragment questFragment = new QuestFragment(document.getId(), (String) data.get("name"), (String) data.get("desc"),
                                        (double) data.get("latitude"), (double) data.get("longitude"), "loc_quest", null, null, null, "profile_quest_list");
                                childFragTrans.add(R.id.user_completed_quests, questFragment);*/
                            }
                            childFragTrans.commit();
                        } else {
                            Log.w("ERROR", "Error getting documents.", task.getException());
                        }
                    }
                });
        */
        return view;
    }

}