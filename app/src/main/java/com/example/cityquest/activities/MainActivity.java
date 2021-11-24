package com.example.cityquest.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.WindowManager;

import com.example.cityquest.R;
import com.example.cityquest.fragments.AboutFragment;
import com.example.cityquest.fragments.CompetitiveFragment;
import com.example.cityquest.fragments.MapFragment;
import com.example.cityquest.fragments.ProfileFragment;
import com.example.cityquest.fragments.QuestsFragment;
import com.example.cityquest.fragments.SignupFragment;
import com.example.cityquest.fragments.SocialFragment;

public class MainActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener {

    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //FULLSCREEN IN NOTCH DEVICES
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        if(getSupportActionBar() != null) getSupportActionBar().hide();

        fragmentManager = getSupportFragmentManager();
        fragmentManager.addOnBackStackChangedListener(this);

        //Add Map to Main Activity
        Fragment fragment = new MapFragment();

        fragmentManager.beginTransaction()
                .replace(R.id.frame_layout, fragment, "map")
                .addToBackStack(null)
                .commit();

        /*Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawerLayout = findViewById(R.id.drawer);
        NavigationView navigationView = findViewById(R.id.nav_menu);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.open, R.string.close
        );

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);*/
    }

    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment;
        switch (item.getItemId()) {
            case R.id.signup:
                fragment = new SignupFragment();
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, fragment, "signup")
                        .addToBackStack(null)
                        .commit();
                return true;
            case R.id.profile:
                fragment = new ProfileFragment();
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, fragment, "profile")
                        .addToBackStack(null)
                        .commit();
                return true;
            case R.id.quests:
                fragment = new QuestsFragment();
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, fragment, "quests")
                        .addToBackStack(null)
                        .commit();
                return true;
            case R.id.competitive:
                fragment = new CompetitiveFragment();
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, fragment, "competitive")
                        .addToBackStack(null)
                        .commit();
                return true;
            case R.id.social:
                fragment = new SocialFragment();
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, fragment, "social")
                        .addToBackStack(null)
                        .commit();
                return true;
            case R.id.about:
                fragment = new AboutFragment();
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, fragment, "about")
                        .addToBackStack(null)
                        .commit();
                return true;
        }
        return false;
    }

    @Override
    public void onBackStackChanged() {
        if(fragmentManager.getBackStackEntryCount() == 0)
            finish();
    }
}