package com.example.cityquest.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cityquest.Prevalent.Prevalent;
import com.example.cityquest.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import io.paperdb.Paper;

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
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    final boolean[] userExists = {false};
                    db.collection("users").get().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task1.getResult()) {
                                HashMap data = (HashMap) document.getData();
                                if(data.get("username").equals(username)) {
                                    System.out.println(data.get("username"));
                                    System.out.println(username);
                                    userExists[0] = true;
                                }
                            }
                        }
                    });
                    if(userExists[0]){
                        usernameSignUp.setError("Username already exists! It must be unique");
                        usernameSignUp.requestFocus();
                    } else {
                        Toast.makeText(SignUp.this, "User registered successfully", Toast.LENGTH_SHORT).show();

                        userID = mAuth.getCurrentUser().getUid();
                        DocumentReference documentReference = db.collection("users").document(userID);
                        Map<String, Object> user = new HashMap<>();
                        user.put("username", username);
                        user.put("email", email);

                        //Storage default profile Image
                        /*Bitmap bitmap = BitmapFactory.decodeFile("app/src/main/res/drawable/profile_image.png");
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, byteArrayOutputStream);
                        byte[] byteArray = byteArrayOutputStream.toByteArray();
                        user.put("profileImage", Base64.encodeToString(byteArray, Base64.DEFAULT));*/

                        documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.d("SUCCESS", "User added to DB");
                            }
                        });

                        startActivity(new Intent(SignUp.this, SignIn.class));
                    }
                } else {
                    Toast.makeText(SignUp.this, "Registration Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}