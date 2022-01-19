package com.example.cityquest.Fragments;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.BitmapCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cityquest.Activities.MainActivity;
import com.example.cityquest.Prevalent.Prevalent;
import com.example.cityquest.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.auth.FirebaseAuth;
import com.yarolegovich.slidingrootnav.SlidingRootNav;
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import io.paperdb.Paper;

public class ProfileFragment extends Fragment {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();

    private ImageView profilePicture;
    private FloatingActionButton buttonAddImage;

    public ProfileFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view =inflater.inflate(R.layout.fragment_profile, container, false);

        //--------------------------SET USERNAME, EMAIL AND PHOTO ON TOP----------------------------
        TextView username = view.findViewById(R.id.username_profile);
        TextView email = view.findViewById(R.id.email_profile);
        profilePicture = view.findViewById(R.id.profileImage);
        TextView followers = view.findViewById(R.id.followers_number);
        TextView following = view.findViewById(R.id.following_number);
        TextView ranking = view.findViewById(R.id.ranking_number);

        username.setText(Paper.book().read(Prevalent.UserUsernameKey));
        email.setText(Paper.book().read(Prevalent.UserEmailKey));
        decodeImage(profilePicture, Paper.book().read(Prevalent.ProfileImageKey));
        followers.setText(Paper.book().read(Prevalent.followers));
        following.setText(Paper.book().read(Prevalent.following));
        ranking.setText(Paper.book().read(Prevalent.ranking));

        System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        System.out.println((String) Paper.book().read(Prevalent.chkBox));
        //------------------------------------------------------------------------------------------

        buttonAddImage = view.findViewById(R.id.buttonAddImage);
        buttonAddImage.setOnClickListener(view1 -> {
            chooseProfilePicture();
        });

        FragmentManager childFragMan = getChildFragmentManager();
        db.collection("users").document(currentUser).collection("user_quests").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            FragmentTransaction childFragTrans = childFragMan.beginTransaction();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                HashMap data = (HashMap) document.getData();
                                QuestFragment questFragment = new QuestFragment(document.getId(), (String) data.get("name"), (String) data.get("desc"),
                                        (double) data.get("latitude"), (double) data.get("longitude"), "loc_quest", null, null, null, "profile_quest_list");
                                childFragTrans.add(R.id.user_quests, questFragment);
                            }
                            childFragTrans.commit();
                        } else {
                            Log.w("ERROR", "Error getting documents.", task.getException());
                        }
                    }
                });

        /* TALVEZ ADICIONAR AQUI O MESMO CÓDIGO PARA ELABORATE QUESTS */

        db.collection("users").document(currentUser).collection("user_completed_quests").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            FragmentTransaction childFragTrans = childFragMan.beginTransaction();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                HashMap data = (HashMap) document.getData();
                                QuestFragment questFragment = new QuestFragment(document.getId(), (String) data.get("name"), (String) data.get("desc"),
                                        (double) data.get("latitude"), (double) data.get("longitude"), "loc_quest", null, null, null, "profile_quest_list");
                                childFragTrans.add(R.id.user_completed_quests, questFragment);
                            }
                            childFragTrans.commit();
                        } else {
                            Log.w("ERROR", "Error getting documents.", task.getException());
                        }
                    }
                });
        return view;
    }

    private static String encodeImage(Bitmap bitmap) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,bos);
        byte[] bb = bos.toByteArray();
        return Base64.encodeToString(bb, Base64.DEFAULT);
    }

    private void decodeImage(ImageView profileImageDecoded, String profileImage) {
        byte[] decodedString = Base64.decode(profileImage, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        profileImageDecoded.setImageBitmap(decodedByte);
    }

    public void chooseProfilePicture(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MyDialogTheme);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView =  inflater.inflate(R.layout.alert_dialog_profile_picture, null);
        builder.setCancelable(false);
        builder.setView(dialogView);

        ImageView addPhotoCamera = dialogView.findViewById(R.id.ADDPCamera);
        ImageView addPhotoGallery = dialogView.findViewById(R.id.ADDPGallery);


        AlertDialog alertDialogProfilePicture = builder.create();
        alertDialogProfilePicture.setCanceledOnTouchOutside(true);
        alertDialogProfilePicture.show();

        addPhotoCamera.setOnClickListener(view -> {
            if(checkAndRequestPermissions()){
                takePictureFromCamera();
                alertDialogProfilePicture.cancel();
            }
        });

        addPhotoGallery.setOnClickListener(view -> {
            takePictureFromGallery();
            alertDialogProfilePicture.cancel();
        });
    }

    private void takePictureFromGallery(){
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, 1);
    }

    private void takePictureFromCamera(){
        Intent takePhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePhoto.resolveActivity(getActivity().getPackageManager()) != null){
            startActivityForResult(takePhoto, 2);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        DocumentReference documentReference = db.collection("users").document(currentUser);
        switch(requestCode){
            case 1:
                if(resultCode == RESULT_OK){
                    Uri selectedImageUri = data.getData();
                    try {
                        Bitmap bitmapRounded = getSquareRoundedMaxCroppedBitmap(MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImageUri));
                        //Not sure for this limit of size
                        if(BitmapCompat.getAllocationByteCount(bitmapRounded) > 1440000) {
                            Toast.makeText(getActivity(), "The image must be less than 300kb in size!", Toast.LENGTH_SHORT).show();
                        } else {
                            profilePicture.setImageBitmap(bitmapRounded);

                            //Update Prevalent
                            Paper.book().write(Prevalent.ProfileImageKey, encodeImage(bitmapRounded));

                            //Atualizar a DB
                            Map<String, Object> user = new HashMap<>();
                            user.put("username", Paper.book().read(Prevalent.UserUsernameKey));
                            user.put("email", Paper.book().read(Prevalent.UserEmailKey));
                            user.put("profileImage", Paper.book().read(Prevalent.ProfileImageKey));
                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Log.d("SUCCESS", "Profile Picture was updated successfully!");
                                }
                            });
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case 2:
                if(resultCode == RESULT_OK){
                    Bundle bundle =  data.getExtras();
                    Bitmap bitmapImage = getSquareRoundedMaxCroppedBitmap((Bitmap) bundle.get("data"));
                    //Not sure for this limit of size
                    if(BitmapCompat.getAllocationByteCount(bitmapImage) > 1440000) {
                        Toast.makeText(getActivity(), "The image must be less than 300kb in size!", Toast.LENGTH_SHORT).show();
                    } else {
                        profilePicture.setImageBitmap(bitmapImage);

                        //Update Prevalent
                        try {
                            Paper.book().write(Prevalent.ProfileImageKey, encodeImage(bitmapImage));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        //Atualizar a DB
                        Map<String, Object> user = new HashMap<>();
                        user.put("username", Paper.book().read(Prevalent.UserUsernameKey));
                        user.put("email", Paper.book().read(Prevalent.UserEmailKey));
                        user.put("profileImage", Paper.book().read(Prevalent.ProfileImageKey));
                        documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.d("SUCCESS", "Profile Picture was updated successfully!");
                            }
                        });
                    }
                }
                break;
        }
    }

    private boolean checkAndRequestPermissions(){
        if(Build.VERSION.SDK_INT >= 23){
            int cameraPermission = ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA);
            if(cameraPermission == PackageManager.PERMISSION_DENIED){
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, 20);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 20 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            takePictureFromCamera();
        } else {
             Toast.makeText(getActivity(), "Permission not Granted", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap getSquareRoundedMaxCroppedBitmap(Bitmap bitmap) {
        //Crop as square
        int width  = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newWidth = (height > width) ? width : height;
        int newHeight = (height > width)? height - ( height - width) : height;
        int cropW = (width - height) / 2;
        cropW = (cropW < 0)? 0: cropW;
        int cropH = (height - width) / 2;
        cropH = (cropH < 0)? 0: cropH;
        Bitmap cropSquareImg = Bitmap.createBitmap(bitmap, cropW, cropH, newWidth, newHeight);

        //Crop as circle
        int widthLight = cropSquareImg.getWidth();
        int heightLight = cropSquareImg.getHeight();

        Bitmap output = Bitmap.createBitmap(cropSquareImg.getWidth(), cropSquareImg.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(output);
        Paint paintColor = new Paint();
        paintColor.setFlags(Paint.ANTI_ALIAS_FLAG);

        RectF rectF = new RectF(new Rect(0, 0, widthLight, heightLight));

        canvas.drawRoundRect(rectF, widthLight / 2, heightLight / 2, paintColor);

        Paint paintImage = new Paint();
        paintImage.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
        canvas.drawBitmap(bitmap, 0, 0, paintImage);

        return output;
    }
}