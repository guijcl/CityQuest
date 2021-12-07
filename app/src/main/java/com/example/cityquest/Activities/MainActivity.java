package com.example.cityquest.Activities;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;

import com.example.cityquest.R;
import com.example.cityquest.Fragments.AboutFragment;
import com.example.cityquest.Fragments.CompetitiveFragment;
import com.example.cityquest.Fragments.MapFragment;
import com.example.cityquest.Fragments.ProfileFragment;
import com.example.cityquest.Fragments.QuestsFragment;
import com.example.cityquest.Fragments.SettingsFragment;
import com.example.cityquest.Fragments.SignupFragment;
import com.example.cityquest.Fragments.SocialFragment;
import com.example.cityquest.menu.DrawerAdapter;
import com.example.cityquest.menu.DrawerItem;
import com.example.cityquest.menu.SimpleItem;
import com.example.cityquest.menu.SpaceItem;
import com.yarolegovich.slidingrootnav.SlidingRootNav;
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements DrawerAdapter.OnItemSelectedListener, FragmentManager.OnBackStackChangedListener {

    private static final int POS_CLOSE = 0;
    private static final int POS_MAIN_MAP = 1;
    private static final int POS_PROFILE = 2;
    private static final int POS_QUESTS = 3;
    private static final int POS_COMPETITIVE = 4;
    private static final int POS_SOCIAL = 5;
    private static final int POS_SETTINGS = 6;
    private static final int POS_ABOUT = 7;
    private static final int POS_SIGNUP = 8;

    private String[] screenTitles;
    private Drawable[] screenIcons;

    private SlidingRootNav slidingRootNav;

    private FragmentManager fragmentManager;

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

        //----------------------------------------SIDE MENU-----------------------------------------

        slidingRootNav = new SlidingRootNavBuilder(this)
                .withDragDistance(180)
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
                createItemFor(POS_SIGNUP)
        ));
        adapter.setListener(this);

        RecyclerView list = findViewById(R.id.drawer_list);
        list.setNestedScrollingEnabled(false);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        adapter.setSelected(POS_MAIN_MAP);

        Toolbar menuIcon = findViewById(R.id.main_toolbar);
        menuIcon.setOnClickListener(v -> slidingRootNav.openMenu());


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

    public void onBackPressed() {
        finish();
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
            case POS_SIGNUP:
                SignupFragment signup = new SignupFragment();
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, signup, "signup")
                        .addToBackStack(null)
                        .commit();
                break;
        }

        slidingRootNav.closeMenu();
    }

    @Override
    public void onBackStackChanged() {
        if(fragmentManager.getBackStackEntryCount() == 0)
            finish();
    }
}