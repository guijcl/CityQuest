package com.example.cityquest.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cityquest.Prevalent.Prevalent;
import com.example.cityquest.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import io.paperdb.Paper;

public class SignIn extends AppCompatActivity {

    private EditText signinEmail;
    private EditText signinPassword;
    private Button btnSignIn;
    private CheckBox chkBoxRemenberMe;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        signinEmail = findViewById(R.id.username_signin);
        signinPassword = findViewById(R.id.password_signin);
        btnSignIn = findViewById(R.id.btnSignIn_Login);
        chkBoxRemenberMe = findViewById(R.id.checkbox_siginin);
        Paper.init(this);

        mAuth = FirebaseAuth.getInstance();
        btnSignIn.setOnClickListener(v -> {
            signInUser();
        });
    }

    public void onClick(View v) {
        startActivity(new Intent(SignIn.this, SignUp.class));
    }

    private void signInUser() {
        String email = signinEmail.getText().toString();
        String password = signinPassword.getText().toString();
        Paper.book().write(Prevalent.UserEmailKey, email);
        Paper.book().write(Prevalent.UserPasswordKey, password);

        if(chkBoxRemenberMe.isChecked())
            Paper.book().write(Prevalent.chkBox, "1");

        if(TextUtils.isEmpty(email)){
            signinEmail.setError("E-mail cannot be empty");
            signinEmail.requestFocus();
        } else if(TextUtils.isEmpty(password)){
            signinPassword.setError("Password cannot be empty");
            signinPassword.requestFocus();
        } else {
            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(SignIn.this, "User logged in successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SignIn.this, MainActivity.class));
                    } else {
                        Toast.makeText(SignIn.this, "Log in Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}