package com.example.cityquest.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.cityquest.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignIn extends AppCompatActivity {

    EditText signinEmail;
    EditText signinPassword;
    Button btnSignIn;
    Button btnSignUp;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        signinEmail = findViewById(R.id.username_signin);
        signinPassword = findViewById(R.id.password_signin);
        btnSignIn = findViewById(R.id.btnSignIn_Login);
        btnSignUp = findViewById(R.id.btnToSignUp_login);

        mAuth = FirebaseAuth.getInstance();
        btnSignIn.setOnClickListener(v -> {
            signInUser();
        });

        btnSignUp.setOnClickListener(v -> {
            startActivity(new Intent(SignIn.this, SignUp.class));
        });
    }

    private void signInUser() {
        String email = signinEmail.getText().toString();
        String password = signinPassword.getText().toString();

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