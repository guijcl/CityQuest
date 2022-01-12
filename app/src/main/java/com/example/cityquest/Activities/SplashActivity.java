package com.example.cityquest.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.cityquest.Prevalent.Prevalent;
import com.example.cityquest.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import io.paperdb.Paper;

public class SplashActivity extends AppCompatActivity {

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        Paper.init(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        if(getSupportActionBar() != null) getSupportActionBar().hide();
        new CountDownTimer(1000, 1000)  { //maybe change back to 2000 or 3000, 1000 is for testing purposes
            @Override
            public void onTick(long l) {}

            @Override
            public void onFinish() {
                String UserEmailKey = Paper.book().read(Prevalent.UserEmailKey);
                String UserPasswordKey = Paper.book().read(Prevalent.UserPasswordKey);
                String chkBox = Paper.book().read(Prevalent.chkBox);

                if(UserEmailKey != null && UserPasswordKey != null && chkBox != null){
                    if(!TextUtils.isEmpty(UserEmailKey) && !TextUtils.isEmpty(UserPasswordKey) && chkBox.equals("1")) {
                        mAuth = FirebaseAuth.getInstance();
                        AllowAccess(UserEmailKey, UserPasswordKey);
                    }
                } else {
                    startActivity(new Intent(getApplicationContext(), SignIn.class));
                    finish();
                }
            }
        }.start();
    }

    private void AllowAccess(String userEmailKey, String userPasswordKey) {
        mAuth.signInWithEmailAndPassword(userEmailKey,userPasswordKey).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(SplashActivity.this, "User logged in successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                }
            }
        });
    }
}