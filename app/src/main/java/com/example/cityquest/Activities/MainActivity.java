package com.example.cityquest.Activities;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cityquest.Fragments.AboutFragment;
import com.example.cityquest.Fragments.CompetitiveFragment;
import com.example.cityquest.Fragments.MapFragment;
import com.example.cityquest.Fragments.ProfileFragment;
import com.example.cityquest.Fragments.QuestsFragment;
import com.example.cityquest.Fragments.SettingsFragment;
import com.example.cityquest.Fragments.SocialFragment;
import com.example.cityquest.Prevalent.Prevalent;
import com.example.cityquest.R;
import com.example.cityquest.menu.DrawerAdapter;
import com.example.cityquest.menu.DrawerItem;
import com.example.cityquest.menu.SimpleItem;
import com.example.cityquest.menu.SpaceItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;
import com.yarolegovich.slidingrootnav.SlidingRootNav;
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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

    private FragmentManager fragmentManager;

    FirebaseAuth mAuth;

    HashMap<String, HashMap> loc_quests;
    HashMap<String, HashMap> elaborate_quests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //--------------------------------------FIRST SETTINGS--------------------------------------
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Paper.init(this);

        //FULLSCREEN IN NOTCH DEVICES
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        //-------------------------------------AUTHENTICATION---------------------------------------

        mAuth = FirebaseAuth.getInstance();

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

        //VERIFICAR COM ID SO USER: FirebaseAuth.getInstance().getCurrentUser().getUid();
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
                           CircleImageView profileImage = slidingRootNav.getLayout().findViewById(R.id.profileImageMenu);
                           username.setText(data.get("username").toString());
                           email.setText(data.get("email").toString());
                           //Picasso.get().load(data.get("profileImage").toString()).into(profileImage);
                       }
                   }
               }
           }
        });

        Toolbar menuIcon = findViewById(R.id.main_toolbar);
        menuIcon.setOnClickListener(v -> slidingRootNav.openMenu());


        //------------------------------------------------------------------------------------------

        loc_quests = new HashMap<>();
        elaborate_quests = new HashMap<>();

        if(getSupportActionBar() != null) getSupportActionBar().hide();

        fragmentManager = getSupportFragmentManager();
        fragmentManager.addOnBackStackChangedListener(this);

        //Add Map to Main Activity
        Fragment fragment = new MapFragment();

        fragmentManager.beginTransaction()
                .replace(R.id.frame_layout, fragment, "map")
                .addToBackStack(null)
                .commit();
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
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, map, "map")
                        .addToBackStack(null)
                        .commit();
                break;
            case POS_PROFILE:
                ProfileFragment profile = new ProfileFragment();
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, profile, "profile")
                        .addToBackStack(null)
                        .commit();
                break;
            case POS_QUESTS:
                QuestsFragment quests = new QuestsFragment();
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, quests, "quests")
                        .addToBackStack(null)
                        .commit();
                break;
            case POS_COMPETITIVE:
                CompetitiveFragment competitive = new CompetitiveFragment();
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, competitive, "competitive")
                        .addToBackStack(null)
                        .commit();
                break;
            case POS_SOCIAL:
                SocialFragment social = new SocialFragment();
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, social, "social")
                        .addToBackStack(null)
                        .commit();
                break;
            case POS_SETTINGS:
                SettingsFragment settings = new SettingsFragment();
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, settings, "settings")
                        .addToBackStack(null)
                        .commit();
                break;
            case POS_ABOUT:
                AboutFragment about = new AboutFragment();
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, about, "about")
                        .addToBackStack(null)
                        .commit();
                break;
            case POS_LOG_OUT:
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
        if(fragmentManager.getBackStackEntryCount() == 0)
            finish();
    }

    public HashMap<String, HashMap> getLocQuests() {
        return loc_quests;
    }

    public void addLocQuest(String id, HashMap temp) {
        loc_quests.put(id, temp);
    }

    public HashMap<String, HashMap> getElaborateQuests() {
        return elaborate_quests;
    }

    public void addElaborateQuest(String id, HashMap temp) {
        elaborate_quests.put(id, temp);
    }
}