package com.example.cityquest.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cityquest.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUp extends AppCompatActivity {

    EditText usernameSignUp;
    EditText signupEmail;
    EditText signupPassword;
    EditText signupPasswordConfirm;
    Button btnSignUp;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String userID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        db = FirebaseFirestore.getInstance();

        usernameSignUp = findViewById(R.id.username_signup);
        signupEmail = findViewById(R.id.email_signup);
        signupPassword = findViewById(R.id.password_signup);
        signupPasswordConfirm = findViewById(R.id.confirm_password_signup);
        btnSignUp = findViewById(R.id.btnsignup);

        mAuth = FirebaseAuth.getInstance();
        btnSignUp.setOnClickListener(v -> {
            createUser();
        });

    }

    public void onClick(View v) {
        startActivity(new Intent(SignUp.this, SignIn.class));
    }

    private void createUser() {
        String username = usernameSignUp.getText().toString();
        String email = signupEmail.getText().toString();
        String password = signupPassword.getText().toString();
        String passwordConfirm = signupPasswordConfirm.getText().toString();

        if(TextUtils.isEmpty(username)){
            usernameSignUp.setError("Username cannot be empty");
            usernameSignUp.requestFocus();
        } else if(TextUtils.isEmpty(email)){
            signupEmail.setError("E-mail cannot be empty");
            signupEmail.requestFocus();
        } else if(TextUtils.isEmpty(password)){
            signupPassword.setError("Password cannot be empty");
            signupPassword.requestFocus();
        } else if(TextUtils.isEmpty(passwordConfirm)){
            signupPasswordConfirm.setError("Confirm Password cannot be empty");
            signupPasswordConfirm.requestFocus();
        } else if(!password.equals(passwordConfirm)){
            signupPasswordConfirm.setError("Passwords do not match");
            signupPasswordConfirm.requestFocus();
        } else {
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(SignUp.this, "User registered successfully", Toast.LENGTH_SHORT).show();

                        userID = mAuth.getCurrentUser().getUid();
                        DocumentReference documentReference = db.collection("users").document(userID);
                        Map<String, Object> user = new HashMap<>();
                        user.put("username", username);
                        user.put("email", email);
                        documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.d("SUCCESS", "User added to DB");
                            }
                        });

                        startActivity(new Intent(SignUp.this, SignIn.class));
                    } else {
                        Toast.makeText(SignUp.this, "Registration Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}