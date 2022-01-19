package com.example.cityquest.Activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cityquest.Fragments.AboutFragment;
import com.example.cityquest.Fragments.CompetitiveFragment;
import com.example.cityquest.Fragments.MapFragment;
import com.example.cityquest.Fragments.ProfileFragment;
import com.example.cityquest.Fragments.QuestFragment;
import com.example.cityquest.Fragments.QuestsFragment;
import com.example.cityquest.Fragments.SettingsFragment;
import com.example.cityquest.Fragments.SocialFragment;
import com.example.cityquest.Objects.LocQuest;
import com.example.cityquest.Prevalent.Prevalent;
import com.example.cityquest.R;
import com.example.cityquest.menu.DrawerAdapter;
import com.example.cityquest.menu.DrawerItem;
import com.example.cityquest.menu.SimpleItem;
import com.example.cityquest.menu.SpaceItem;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.yarolegovich.slidingrootnav.SlidingRootNav;
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity implements DrawerAdapter.OnItemSelectedListener, FragmentManager.OnBackStackChangedListener {

    private static final int POS_CLOSE = 0;
    private static final int POS_MAIN_MAP = 1;
    private static final int POS_PROFILE = 2;
    private static final int POS_QUESTS = 3;
    private static final int POS_COMPETITIVE = 4;
    private static final int POS_SOCIAL = 5;
    private static final int POS_SETTINGS = 6;
    private static final int POS_ABOUT = 7;
    private static final int POS_LOG_OUT = 8;

    private String[] screenTitles;
    private Drawable[] screenIcons;

    private SlidingRootNav slidingRootNav;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private FragmentManager fragmentManager;
    Fragment currentFragment;

    private String currentUser;

    HashMap<String, HashMap> loc_quests = new HashMap<>();
    HashMap<String, HashMap> elaborate_quests = new HashMap<>();

    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //--------------------------------------FIRST SETTINGS--------------------------------------
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //FULLSCREEN IN NOTCH DEVICES
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        //------------------------------AUTHENTICATION AND GET DATA---------------------------------

        mAuth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();
        db.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        HashMap data = (HashMap) document.getData();
                        String emailSaved = Paper.book().read(Prevalent.UserEmailKey);
                        if(data.get("email").equals(emailSaved)) {
                            TextView username = slidingRootNav.getLayout().findViewById(R.id.username_sideMenu);
                            TextView email = slidingRootNav.getLayout().findViewById(R.id.email_sideMenu);
                            ImageView profileImage = slidingRootNav.getLayout().findViewById(R.id.profileImageMenu);

                            Paper.book().write(Prevalent.UserUsernameKey, data.get("username").toString());
                            Paper.book().write(Prevalent.ProfileImageKey, data.get("profileImage").toString());
                            Paper.book().write(Prevalent.followers, data.get("followers").toString());
                            Paper.book().write(Prevalent.following, data.get("following").toString());
                            Paper.book().write(Prevalent.ranking, data.get("ranking").toString());


                            username.setText(data.get("username").toString());
                            email.setText(data.get("email").toString());
                            decodeImage(profileImage, data.get("profileImage").toString());
                        }
                    }
                }
            }
        });

        Toolbar menuIcon = findViewById(R.id.main_toolbar);
        menuIcon.setOnClickListener(v -> {
            db.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            HashMap data = (HashMap) document.getData();
                            String emailSaved = Paper.book().read(Prevalent.UserEmailKey);
                            if(data.get("email").equals(emailSaved)) {
                                ImageView profileImage = slidingRootNav.getLayout().findViewById(R.id.profileImageMenu);
                                decodeImage(profileImage, data.get("profileImage").toString());
                            }
                        }
                    }
                }
            });
            slidingRootNav.openMenu();
        });

        //----------------------------------------SIDE MENU-----------------------------------------

        slidingRootNav = new SlidingRootNavBuilder(this)
                .withDragDistance(200)
                .withRootViewScale(0.75f)
                .withRootViewElevation(25)
                .withMenuOpened(false)
                .withContentClickableWhenMenuOpened(false)
                .withSavedState(savedInstanceState)
                .withMenuLayout(R.layout.drawer_menu)
                .inject();

        screenIcons = loadScreenIcons();
        screenTitles = loadScreenTitles();

        DrawerAdapter adapter_close = new DrawerAdapter(Arrays.asList(
                createItemFor(POS_CLOSE)
        ));
        adapter_close.setListener(this);

        RecyclerView list_close = findViewById(R.id.drawer_close);
        list_close.setNestedScrollingEnabled(false);
        list_close.setLayoutManager(new LinearLayoutManager(this));
        list_close.setAdapter(adapter_close);

        DrawerAdapter adapter = new DrawerAdapter(Arrays.asList(
                createItemFor(POS_MAIN_MAP).setChecked(true),
                createItemFor(POS_PROFILE),
                createItemFor(POS_QUESTS),
                createItemFor(POS_COMPETITIVE),
                createItemFor(POS_SOCIAL),
                createItemFor(POS_SETTINGS),
                createItemFor(POS_ABOUT),
                new SpaceItem(40),
                createItemFor(POS_LOG_OUT)
        ));
        adapter.setListener(this);

        RecyclerView list = findViewById(R.id.drawer_list);
        list.setNestedScrollingEnabled(false);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        adapter.setSelected(POS_MAIN_MAP);

        //------------------------------------------------------------------------------------------

        if(getSupportActionBar() != null) getSupportActionBar().hide();

        fragmentManager = getSupportFragmentManager();
        fragmentManager.addOnBackStackChangedListener(this);

        //Add Map to Main Activity
        Fragment fragment = new MapFragment();

        fragmentManager.beginTransaction()
                .replace(R.id.frame_layout, fragment, "map")
                .addToBackStack(null)
                .commit();

        System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        System.out.println((String)Paper.book().read(Prevalent.chkBox));
    }

    private void decodeImage(ImageView profileImageDecoded, String profileImage) {
        byte[] decodedString = Base64.decode(profileImage, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        profileImageDecoded.setImageBitmap(decodedByte);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user == null) {
            startActivity(new Intent(MainActivity.this, SignIn.class));
        }
    }

    public void onPause() {
        super.onPause();
        if (isFinishing() && Paper.book().read(Prevalent.chkBox).equals("0")) {
            mAuth.signOut();
            Paper.book().destroy();
        }
    }

    private DrawerItem createItemFor(int position) {
        return new SimpleItem(screenIcons[position], screenTitles[position])
                .withIconTint(color(R.color.white))
                .withTextTint(color(R.color.white))
                .withSelectedIconTint(color(R.color.white))
                .withSelectedTextTint(color(R.color.white));
    }

    @ColorInt
    private int color(@ColorRes int res) {
        return ContextCompat.getColor(this, res);
    }

    private String[] loadScreenTitles() {
        return getResources().getStringArray(R.array.id_activityScreenTitles);
    }

    private Drawable[] loadScreenIcons() {
        TypedArray ta = getResources().obtainTypedArray(R.array.id_activityScreenIcons);
        Drawable[] icons = new Drawable[ta.length()];
        for (int i = 0; i < ta.length(); i++) {
            int id = ta.getResourceId(i, 0);
            if (id != 0) {
                icons[i] = ContextCompat.getDrawable(this, id);
            }
        }
        ta.recycle();
        return icons;
    }

    @Override
    public void onItemSelected(int position) {
        fragmentManager = getSupportFragmentManager();
        switch (position) {
            case POS_MAIN_MAP:
                MapFragment map = new MapFragment();
                currentFragment = map;
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, map, "map")
                        .addToBackStack(null)
                        .commit();
                break;
            case POS_PROFILE:
                ProfileFragment profile = new ProfileFragment();
                currentFragment = profile;
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, profile, "profile")
                        .addToBackStack(null)
                        .commit();
                break;
            case POS_QUESTS:
                QuestsFragment quests = new QuestsFragment();
                currentFragment = quests;
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, quests, "quests")
                        .addToBackStack(null)
                        .commit();
                break;
            case POS_COMPETITIVE:
                CompetitiveFragment competitive = new CompetitiveFragment();
                currentFragment = competitive;
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, competitive, "competitive")
                        .addToBackStack(null)
                        .commit();
                break;
            case POS_SOCIAL:
                SocialFragment social = new SocialFragment();
                currentFragment = social;
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, social, "social")
                        .addToBackStack(null)
                        .commit();
                break;
            case POS_SETTINGS:
                SettingsFragment settings = new SettingsFragment();
                currentFragment = settings;
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, settings, "settings")
                        .addToBackStack(null)
                        .commit();
                break;
            case POS_ABOUT:
                AboutFragment about = new AboutFragment();
                currentFragment = about;
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, about, "about")
                        .addToBackStack(null)
                        .commit();
                break;
            case POS_LOG_OUT:
                currentFragment = null;
                mAuth.signOut();
                Paper.book().destroy();
                startActivity(new Intent(MainActivity.this, SignIn.class));
                finish();
                break;
        }

        slidingRootNav.closeMenu();
    }

    @Override
    public void onBackStackChanged() {
        if(fragmentManager.getBackStackEntryCount() == 0) {
            finish();
        }
    }

    public HashMap<String, HashMap> getLocQuests() {
        return loc_quests;
    }

    public void addLocQuest(String id, HashMap temp) {
        loc_quests.put(id, temp);
    }

    public void deleteLocQuest(String id) { loc_quests.remove(id); }

    public HashMap<String, HashMap> getElaborateQuests() {
        return elaborate_quests;
    }

    public void addElaborateQuest(String id, HashMap temp) {
        elaborate_quests.put(id, temp);
    }

    public void updateElaborateQuests() {
        for(String id : elaborate_quests.keySet()) {
            db.collection("users").document(currentUser).
                    collection("user_elaborate_quests").document(id).update(elaborate_quests.get(id));
        }
    }

    public void updateElaborateQuest(String id) {
        db.collection("users").document(currentUser).
                collection("user_elaborate_quests").document(id).update(elaborate_quests.get(id));
    }

    public void showLocQuestPopup(String id, HashMap<String, Marker> hashMapMarker, View quest, String fragmentType) {
        dialogBuilder = new AlertDialog.Builder(this);
        final View locQuestPopupView = getLayoutInflater().inflate(R.layout.loc_quest_popup, null);

        TextView name = locQuestPopupView.findViewById(R.id.quest_name);
        TextView latitude = locQuestPopupView.findViewById(R.id.latitude);
        TextView longitude = locQuestPopupView.findViewById(R.id.longitude);
        TextView value = locQuestPopupView.findViewById(R.id.value);
        TextView times_completed = locQuestPopupView.findViewById(R.id.times_completed);

        Button start_quest = locQuestPopupView.findViewById(R.id.start_quest);
        Button cancel_quest = locQuestPopupView.findViewById(R.id.cancel_quest);
        Button close_popup = locQuestPopupView.findViewById(R.id.close_popup);

        cancel_quest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.collection("users").document(currentUser).collection("user_loc_quests").document(id).delete();
                loc_quests.remove(id);

                if (hashMapMarker != null) {
                    Marker temp_marker = hashMapMarker.get(id);
                    temp_marker.setIcon(BitmapDescriptorFactory.defaultMarker());
                    hashMapMarker.put((String) id, temp_marker);

                    cancel_quest.setVisibility(View.GONE);
                    start_quest.setVisibility(View.VISIBLE);
                } else {
                    cancel_quest.setEnabled(false);
                    if(quest != null) {
                        Button loc_button = quest.findViewById(R.id.start_local_quest);
                        loc_button.setEnabled(true);
                    }
                }

                if(fragmentType.equals("profile_quest_list")) {
                    Fragment profile = fragmentManager.findFragmentByTag("profile");
                    profile.getChildFragmentManager().beginTransaction().remove(profile.getChildFragmentManager().findFragmentByTag(id + " active")).commit();
                }
            }
        });

        db.collection("loc_quests").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    HashMap data = (HashMap) task.getResult().getData();
                    String name_txt = (String) data.get("name");
                    double latitude_txt = (double) data.get("latitude");
                    double longitude_txt = (double) data.get("longitude");
                    String value_txt = (String) data.get("experience");
                    name.setText("Local > " + name_txt);
                    latitude.setText("Latitude > " + latitude_txt);
                    longitude.setText("Longitude > " + longitude_txt);
                    value.setText("Value > " + value_txt + "xp");

                    start_quest.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            HashMap<String, String> quest = new HashMap<>();
                            quest.put("name", name_txt);
                            quest.put("desc", (String) data.get("desc"));
                            quest.put("latitude", String.valueOf(latitude_txt));
                            quest.put("longitude", String.valueOf(longitude_txt));
                            quest.put("popularity", (String) data.get("popularity"));
                            quest.put("experience", value_txt);
                            addLocQuest(id, quest);

                            db.collection("users")
                                    .document(currentUser).collection("user_loc_quests").document(id)
                                    .set(new LocQuest(name_txt, (String) data.get("desc"), latitude_txt, longitude_txt, (String) data.get("popularity"), value_txt, ((Timestamp) data.get("creationDate")).toDate()));

                            if (hashMapMarker != null) {
                                Marker temp_marker = hashMapMarker.get(id);
                                temp_marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                                hashMapMarker.put(id, temp_marker);
                            }

                            cancel_quest.setVisibility(View.VISIBLE);
                            start_quest.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });

        db.collection("users").document(currentUser).collection("completed_loc_quests")
                .document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists())
                        times_completed.setText("Times you've completed this Quest > " + task.getResult().getData().get("completed_num"));
                    else times_completed.setText("Times you've completed this Quest > 0");
                }
            }
        });

        db.collection("users").document(currentUser).collection("user_loc_quests").document(id)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        start_quest.setVisibility(View.GONE);
                        cancel_quest.setVisibility(View.VISIBLE);
                        if (hashMapMarker == null)
                            cancel_quest.setEnabled(true);
                    } else {
                        if (hashMapMarker == null) {
                            cancel_quest.setVisibility(View.VISIBLE);
                            cancel_quest.setEnabled(false);
                            start_quest.setVisibility(View.GONE);
                        } else {
                            cancel_quest.setVisibility(View.GONE);
                            start_quest.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
        });

        close_popup.setOnClickListener(view -> {
            dialog.dismiss();
        });

        dialogBuilder.setView(locQuestPopupView);
        dialog = dialogBuilder.create();
        dialog.show();
    }

    public void showElaborateQuestPopup(String id, View quest, String fragmentType){
        dialogBuilder = new AlertDialog.Builder(this);
        final View elaborateQuestPopupView = getLayoutInflater().inflate(R.layout.elaborate_quest_popup, null);

        TextView name = elaborateQuestPopupView.findViewById(R.id.quest_name);
        TextView desc = elaborateQuestPopupView.findViewById(R.id.desc);
        TextView tasks = elaborateQuestPopupView.findViewById(R.id.tasks);
        TextView value = elaborateQuestPopupView.findViewById(R.id.value);
        TextView cooldown_time = elaborateQuestPopupView.findViewById(R.id.cooldown_time);
        TextView in_cooldown_until = elaborateQuestPopupView.findViewById(R.id.in_cooldown_until);
        TextView time_to_complete = elaborateQuestPopupView.findViewById(R.id.time_to_complete);
        TextView complete_until = elaborateQuestPopupView.findViewById(R.id.complete_until);
        TextView times_completed = elaborateQuestPopupView.findViewById(R.id.times_completed);

        Button cancel_quest = elaborateQuestPopupView.findViewById(R.id.cancel_quest);
        Button close_popup = elaborateQuestPopupView.findViewById(R.id.close_popup);

        cancel_quest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.collection("users").document(currentUser).collection("user_elaborate_quests").document(id).delete();
                elaborate_quests.remove(id);

                cancel_quest.setEnabled(false);
                if(quest != null) {
                    in_cooldown_until.setVisibility(View.GONE);
                    complete_until.setVisibility(View.GONE);
                    Button elaborate_button = quest.findViewById(R.id.start_elaborate_quest);
                    elaborate_button.setEnabled(true);
                }

                if(fragmentType.equals("profile_quest_list")) {
                    Fragment profile = fragmentManager.findFragmentByTag("profile");
                    profile.getChildFragmentManager().beginTransaction().remove(profile.getChildFragmentManager().findFragmentByTag(id + " active")).commit();
                }
            }
        });

        db.collection("elaborate_quests").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    HashMap data = (HashMap) task.getResult().getData();
                    String name_txt = (String) data.get("name");
                    String desc_txt = (String) data.get("desc");
                    String value_txt = (String) data.get("experience");
                    String cooldown_time_txt = (String) data.get("cooldown");
                    String time_to_complete_txt = (String) data.get("time");

                    name.setText("Name > " + name_txt);
                    desc.setText("Description > " + desc_txt);
                    value.setText("Value > " + value_txt + "xp");
                    cooldown_time.setText("Cooldown Time > " + cooldown_time_txt);
                    time_to_complete.setText("Time to Complete > " + time_to_complete_txt);

                    HashMap quests = (HashMap) data.get("quests");
                    String meters = (String) data.get("meters");
                    if(!quests.isEmpty()) {
                        int count = 1;
                        for (Object id_quest : quests.keySet()) {
                            tasks.setText(tasks.getText() + "Location " + count + " > " + (String) ((HashMap) quests.get(id_quest)).get("name") + "\n");
                            count++;
                        }
                    }

                    if(meters != null)
                        tasks.setText(tasks.getText() + "Meters > " + meters + "\n");

                    tasks.setText("\n" + tasks.getText() + "Time to complete > " + (String) data.get("time") + " hours" + "\n");
                }
            }
        });

        db.collection("users").document(currentUser).collection("completed_elaborate_quests")
                .document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        times_completed.setText("Times you've completed this Quest > " + task.getResult().getData().get("completed_num"));

                        Date current_time = Calendar.getInstance().getTime();
                        if(current_time.after(((Timestamp)task.getResult().getData().get("cooldown_until")).toDate()))
                            in_cooldown_until.setVisibility(View.GONE);
                        else {
                            in_cooldown_until.setText("In Cooldown until > " + ((Timestamp)task.getResult().getData().get("cooldown_until")).toDate().toString());
                            in_cooldown_until.setVisibility(View.VISIBLE);
                        }
                    } else {
                        times_completed.setText("Times you've completed this Quest > 0");
                        in_cooldown_until.setVisibility(View.GONE);
                    }
                }
            }
        });

        db.collection("users").document(currentUser).collection("user_elaborate_quests").document(id)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        cancel_quest.setEnabled(true);

                        HashMap data = (HashMap) task.getResult().getData();

                        Date limit_date;
                        if(elaborate_quests.get(id).get("time") instanceof Timestamp)
                            limit_date = ((Timestamp) elaborate_quests.get(id).get("time")).toDate();
                        else
                            limit_date = (Date) elaborate_quests.get(id).get("time");

                        complete_until.setText("Complete Until > " + limit_date);
                        complete_until.setVisibility(View.VISIBLE);
                    } else
                        cancel_quest.setEnabled(false);
                }
            }
        });

        close_popup.setOnClickListener(view -> {
            dialog.dismiss();
        });

        dialogBuilder.setView(elaborateQuestPopupView);
        dialog = dialogBuilder.create();
        dialog.show();
    }

}