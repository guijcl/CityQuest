package com.example.cityquest.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import com.example.cityquest.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.ByteArrayOutputStream;
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
            final boolean[] userExists = {false};
            db.collection("users").get().addOnCompleteListener(task1 -> {
                if (task1.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task1.getResult()) {
                        HashMap data = (HashMap) document.getData();
                        if(data.get("username").equals(username)) {
                            userExists[0] = true;
                        }
                    }
                }

                if(userExists[0]) {
                    usernameSignUp.setError("Username already exists! It must be unique");
                    usernameSignUp.requestFocus();
                } else {
                    Toast.makeText(SignUp.this, "User registered successfully", Toast.LENGTH_SHORT).show();

                    mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
                        if(task.isSuccessful()){

                            userID = mAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = db.collection("users").document(userID);

                            //Storage default username, email and Profile Image
                            Map<String, Object> user = new HashMap<>();
                            //USERNAME
                            user.put("username", username);
                            //EMAIL
                            user.put("email", email);
                            //PROFILE_IMAGE-DEFAULT_IMAGE
                            ImageView lblPic = new ImageView(this);
                            String uri = "@drawable/profile_image";
                            int imageResource = getResources().getIdentifier(uri, null, getPackageName());
                            Drawable res = getResources().getDrawable(imageResource);
                            lblPic.setImageDrawable(res);

                            try {
                                user.put("profileImage", encodeImage(lblPic));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            //FOLLOWERS
                            user.put("followers", "0");
                            //FOLLOWING
                            user.put("following", "0");
                            //RANKING
                            user.put("ranking", "0");
                            user.put("experience", "1");
                            //STATUS
                            user.put("status", "offline");

                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Log.d("SUCCESS", "User added to DB");
                                }
                            });

                        } else {
                            Toast.makeText(SignUp.this, "Registration Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    startActivity(new Intent(SignUp.this, SignIn.class));
                }

            });
        }
    }

    private static String encodeImage(ImageView profImg) throws Exception {
        BitmapDrawable drawable = (BitmapDrawable) profImg.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,bos);
        byte[] bb = bos.toByteArray();
        return Base64.encodeToString(bb, Base64.DEFAULT);
    }

}