package com.example.cityquest.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.example.cityquest.Activities.MainActivity;
import com.example.cityquest.Listeners.SensorListener;
import com.example.cityquest.R;
import com.example.cityquest.bottomSheet.BottomSheetItem;
import com.example.cityquest.bottomSheet.BottomSheetItemAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class MapFragment extends Fragment implements GoogleMap.OnMarkerClickListener {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private Geocoder geocoder;
    private Vibrator vibrator;
    private SensorListener sensorListener;

    private String currentUser;
    private MainActivity mainActivity;
    private HashMap<String, HashMap> user_loc_quests;
    private HashMap<String, HashMap> user_elaborate_quests;

    private GoogleMap googleMap;
    private HashMap<String, Marker> hashMapMarker = new HashMap<>();

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest mLocationRequest;

    private BottomSheetBehavior bottomSheetBehavior;

    private Location last_loc = null;

    public MapFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
            } else {
            }
        });
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION);
        requestPermissionLauncher.launch(Manifest.permission.INTERNET);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //return;
        }

        mainActivity = (MainActivity) getActivity();
        user_loc_quests = mainActivity.getLocQuests();
        user_elaborate_quests = mainActivity.getElaborateQuests();

        fusedLocationClient.requestLocationUpdates(mLocationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location location = locationResult.getLastLocation();

                if (last_loc != null) {
                    double distance = last_loc.distanceTo(location);
                    for (String id : user_elaborate_quests.keySet()) {
                        HashMap quest = user_elaborate_quests.get(id);
                        if (quest.containsKey("meters") && quest.get("meters") != null) {
                            if (Double.parseDouble(String.valueOf(quest.get("meters_traveled"))) < Double.parseDouble(String.valueOf(quest.get("meters")))) {
                                double current_meters = Double.parseDouble(String.valueOf(quest.get("meters_traveled")));
                                current_meters += distance;
                                user_elaborate_quests.get(id).put("meters_traveled", current_meters);

                                if (Double.parseDouble(String.valueOf(quest.get("meters_traveled"))) >= Double.parseDouble(String.valueOf(quest.get("meters")))) {
                                    ((MainActivity) getActivity()).updateElaborateQuest(id);
                                    removeElaboratedQuest(id);
                                }
                            }
                        }
                    }
                }
                last_loc = location;

                if (googleMap != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    if(hashMapMarker.containsKey("user_marker")) {
                        Marker temp_marker = hashMapMarker.get("user_marker");
                        temp_marker.setPosition(new LatLng(latitude, longitude));
                        hashMapMarker.put("user_marker", temp_marker);
                    }
                }
            }
        }, null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_map, container, false);

        currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();

        geocoder = new Geocoder(this.requireContext());
        vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
        sensorListener = new SensorListener(getActivity().getApplicationContext());
        sensorListener.setMap(this);

        SupportMapFragment supportMapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.google_map);

        assert supportMapFragment != null : "OBJECT DOESN'T EXIST";
        supportMapFragment.getMapAsync(googleMap -> {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //return TODO;
            }

            googleMap.getUiSettings().setCompassEnabled(false);
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(requireActivity(), location -> {
                        if (location != null) {
                            MarkerOptions userMarker = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.user_marker_icon ))
                                    .position(new LatLng(location.getLatitude(), location.getLongitude()));

                            CameraPosition cameraPosition = new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .zoom(17)
                                    .bearing(90)
                                    .tilt(40)
                                    .build();
                            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                            Marker marker = googleMap.addMarker(userMarker);
                            hashMapMarker.put("user_marker", marker);
                        }
                    });

            db.collection("loc_quests").get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    HashMap data = (HashMap) document.getData();

                                    LatLng latLng = new LatLng(Double.parseDouble(String.valueOf(data.get("latitude"))),
                                            Double.parseDouble(String.valueOf(data.get("longitude"))));

                                    HashMap<String, Boolean> quests_from_elaborate_quests = new HashMap<>();
                                    for(String quest_id : user_elaborate_quests.keySet()) {
                                        for(Object loc_quests : ((HashMap) user_elaborate_quests.get(quest_id).get("quests")).keySet()){
                                            Boolean b = (Boolean) ((HashMap) ((HashMap) (user_elaborate_quests.get(quest_id)).get("quests"))
                                                    .get(loc_quests)).get("done");
                                            quests_from_elaborate_quests.put((String) loc_quests, b);
                                        }
                                    }

                                    MarkerOptions markerOptions;
                                    if(quests_from_elaborate_quests.containsKey(document.getId())) {
                                        if(!quests_from_elaborate_quests.get(document.getId())) {
                                            markerOptions = new MarkerOptions().
                                                    icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)).position(latLng);
                                        } else {
                                            markerOptions = new MarkerOptions().
                                                    icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)).position(latLng);
                                        }
                                    } else if(user_loc_quests.containsKey(document.getId())) {
                                        markerOptions = new MarkerOptions().
                                            icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).position(latLng);
                                    } else {
                                        markerOptions = new MarkerOptions().position(latLng);
                                    }
                                    Marker marker = googleMap.addMarker(markerOptions);
                                    hashMapMarker.put(document.getId(), marker);
                                }
                            } else {
                                Log.w("ERROR", "Error getting documents.", task.getException());
                            }
                        }
                    });

            googleMap.setOnMarkerClickListener(this);
            googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(@NonNull LatLng latLng) {
                    List<Address> addressList = null;
                    try {
                        addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (addressList != null) {
                        if (addressList.size() > 0) {
                            String str = getOnTapLocationString(addressList);

                            boolean in_loc_quests = false;
                            // ============= LOC QUEST =============
                            for(String key : user_loc_quests.keySet()) {
                                HashMap<String, Object> map = user_loc_quests.get(key);

                                List<String> tmp_s = Arrays.asList(((String) map.get("name")).split(","));
                                boolean check = true;
                                for(String s : tmp_s) {
                                    if(!str.contains(s))
                                        check = false;
                                }
                                if(check) {
                                    in_loc_quests = true;

                                    Marker temp_marker = hashMapMarker.get(key);
                                    temp_marker.setIcon(BitmapDescriptorFactory.defaultMarker());
                                    hashMapMarker.put(key, temp_marker);

                                    db.collection("users").document(currentUser)
                                            .collection("user_loc_quests").document(key).delete();

                                    db.collection("users").document(currentUser).collection("completed_loc_quests")
                                            .document(key).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();
                                                if (document.exists()) {
                                                    db.collection("users").document(currentUser).collection("completed_loc_quests")
                                                            .document(key).update("completed_num", (long) document.get("completed_num") + 1);
                                                } else {
                                                    HashMap<String, Integer> temp = new HashMap<>();
                                                    temp.put("completed_num", 1);
                                                    db.collection("users").document(currentUser).collection("completed_loc_quests")
                                                            .document(key).set(temp);
                                                }
                                            } else {
                                                Log.d("ERROR", "Failed with: ", task.getException());
                                            }
                                        }
                                    });

                                    db.collection("users").document(currentUser).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();
                                                if (document.getData().containsKey("ranking") && document.getData().containsKey("experience")) {
                                                    db.collection("users").document(currentUser)
                                                            .update("experience", String.valueOf( Double.parseDouble((String) document.get("experience"))
                                                                    + Double.parseDouble(((String) user_loc_quests.get(key).get("experience"))) )).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            user_loc_quests.remove(key);
                                                        }
                                                    });
                                                    if(Double.parseDouble((String) document.get("experience")) >= nextLevel(Integer.parseInt((String) document.get("ranking"))))
                                                        db.collection("users").document(currentUser).update("ranking", String.valueOf(Integer.parseInt((String) document.get("ranking")) + 1));
                                                }
                                            } else {
                                                Log.d("ERROR", "Failed with: ", task.getException());
                                            }
                                        }
                                    });

                                    updateLocQuestPopularity(key);
                                }
                            }
                            // =====================================

                            // ===== ELABORATE QUEST LOCATION CHECK =====
                            if(!in_loc_quests) {
                                for (String key : user_elaborate_quests.keySet()) {
                                    for (Object quest : ((HashMap) user_elaborate_quests.get(key).get("quests")).keySet()) {
                                        HashMap<String, Object> map = (HashMap<String, Object>) ((HashMap<String, HashMap>) user_elaborate_quests
                                                .get(key)
                                                .get("quests"))
                                                .get(quest);

                                        List<String> tmp_s = Arrays.asList(((String) map.get("name")).split(","));
                                        boolean check = true;
                                        for (String s : tmp_s) {
                                            if (!str.contains(s))
                                                check = false;
                                        }
                                        if (check) {
                                            Marker temp_marker = hashMapMarker.get(quest);
                                            temp_marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                                            hashMapMarker.put(key, temp_marker);

                                            ((HashMap<String, Object>) ((HashMap<String, HashMap>) user_elaborate_quests
                                                    .get(key).get("quests")).get(quest)).put("done", true);

                                            updateLocQuestPopularity((String) quest);
                                            removeElaboratedQuest(key);
                                        }
                                    }
                                }
                            }
                            // ==========================================
                        }
                    }
                }
            });

            setMap(googleMap);
        });

        return view;
    }

    public void checkLocation() {
        vibrator.vibrate(400);

        List<Address> addressList = null;
        try {
            addressList = geocoder.getFromLocation(last_loc.getLatitude(), last_loc.getLongitude(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addressList != null) {
            if (addressList.size() > 0) {
                String str = getOnTapLocationString(addressList);

                boolean in_loc_quests = false;
                // ============= LOC QUEST =============
                for(String key : user_loc_quests.keySet()) {
                    HashMap<String, Object> map = user_loc_quests.get(key);

                    List<String> tmp_s = Arrays.asList(((String) map.get("name")).split(","));
                    boolean check = true;
                    for(String s : tmp_s) {
                        if(!str.contains(s))
                            check = false;
                    }
                    if(check) {
                        in_loc_quests = true;

                        if(hashMapMarker.containsKey(key)) {
                            Marker temp_marker = hashMapMarker.get(key);
                            temp_marker.setIcon(BitmapDescriptorFactory.defaultMarker());
                            hashMapMarker.put(key, temp_marker);
                        }

                        db.collection("users").document(currentUser)
                                .collection("user_loc_quests").document(key).delete();

                        db.collection("users").document(currentUser).collection("completed_loc_quests")
                                .document(key).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        db.collection("users").document(currentUser).collection("completed_loc_quests")
                                                .document(key).update("completed_num", (long) document.get("completed_num") + 1);
                                    } else {
                                        HashMap<String, Integer> temp = new HashMap<>();
                                        temp.put("completed_num", 1);
                                        db.collection("users").document(currentUser).collection("completed_loc_quests")
                                                .document(key).set(temp);
                                    }
                                } else {
                                    Log.d("ERROR", "Failed with: ", task.getException());
                                }
                            }
                        });

                        db.collection("users").document(currentUser).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.getData().containsKey("ranking") && document.getData().containsKey("experience")) {
                                        db.collection("users").document(currentUser)
                                                .update("experience", String.valueOf( Double.parseDouble((String) document.get("experience"))
                                                        + Double.parseDouble(((String) user_loc_quests.get(key).get("experience"))) )).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                user_loc_quests.remove(key);
                                            }
                                        });
                                        if(Double.parseDouble((String) document.get("experience")) >= nextLevel(Integer.parseInt((String) document.get("ranking"))))
                                            db.collection("users").document(currentUser).update("ranking", String.valueOf(Integer.parseInt((String) document.get("ranking")) + 1));
                                    }
                                } else {
                                    Log.d("ERROR", "Failed with: ", task.getException());
                                }
                            }
                        });

                        updateLocQuestPopularity(key);
                    }
                }
                // =====================================

                // ===== ELABORATE QUEST LOCATION CHECK =====
                if(!in_loc_quests) {
                    for (String key : user_elaborate_quests.keySet()) {
                        for (Object quest : ((HashMap) user_elaborate_quests.get(key).get("quests")).keySet()) {
                            HashMap<String, Object> map = (HashMap<String, Object>) ((HashMap<String, HashMap>) user_elaborate_quests
                                    .get(key)
                                    .get("quests"))
                                    .get(quest);

                            List<String> tmp_s = Arrays.asList(((String) map.get("name")).split(","));
                            boolean check = true;
                            for (String s : tmp_s) {
                                if (!str.contains(s))
                                    check = false;
                            }
                            if (check) {
                                Marker temp_marker = hashMapMarker.get(quest);
                                temp_marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                                hashMapMarker.put(key, temp_marker);

                                ((HashMap<String, Object>) ((HashMap<String, HashMap>) user_elaborate_quests
                                        .get(key).get("quests")).get(quest)).put("done", true);

                                updateLocQuestPopularity((String) quest);
                                removeElaboratedQuest(key);
                            }
                        }
                    }
                }
                // ==========================================
            }
        }
    }

    private void removeElaboratedQuest(String id) {
        ((MainActivity) getActivity()).updateElaborateQuest(id);

        if(elaborateQuestCompletedCheck(id) || elaborateQuestTimeLimitExceeded(id)) {
            if(elaborateQuestCompletedCheck(id)) {
                db.collection("elaborate_quests").document(id).update("popularity",
                        String.valueOf((Integer.parseInt((String) user_elaborate_quests.get(id).get("popularity"))) + 1));

                for(Object id_quest : ((HashMap) user_elaborate_quests.get(id).get("quests")).keySet()) {
                    Marker temp_marker = hashMapMarker.get(id_quest);
                    temp_marker.setIcon(BitmapDescriptorFactory.defaultMarker());
                    hashMapMarker.put((String) id_quest, temp_marker);
                }

                db.collection("users").document(currentUser).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();

                            db.collection("elaborate_quests").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if(task.isSuccessful()) {
                                        if(task.getResult().exists()) {
                                            HashMap data = (HashMap) task.getResult().getData();

                                            if (document.getData().containsKey("ranking") && document.getData().containsKey("experience")) {
                                                double experience = Double.parseDouble((String) document.get("experience"))
                                                        + Double.parseDouble(((String) data.get("experience")));
                                                int current_level = Integer.parseInt((String) document.get("ranking"));
                                                db.collection("users").document(currentUser).update("experience", String.valueOf(experience));
                                                if(experience >= nextLevel(current_level))
                                                    db.collection("users").document(currentUser).update("ranking", String.valueOf(current_level + 1));
                                            }
                                        }
                                    }
                                }
                            });
                        } else {
                            Log.d("ERROR", "Failed with: ", task.getException());
                        }
                    }
                });

                db.collection("users").document(currentUser).collection("completed_elaborate_quests")
                        .document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();

                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd:HH:mm");
                            String currentDateandTime = sdf.format(new Date());

                            Date date = null;
                            try {
                                date = sdf.parse(currentDateandTime);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(date);

                            db.collection("elaborate_quests").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if(task.isSuccessful()) {
                                        if(task.getResult().exists()) {
                                            HashMap data = (HashMap) task.getResult().getData();
                                            calendar.add(Calendar.HOUR, Integer.parseInt((String) data.get("cooldown")));

                                            if (document.exists()) {
                                                HashMap<String, Object> temp = new HashMap<>();
                                                temp.put("completed_num", (long) document.get("completed_num") + 1);
                                                temp.put("cooldown_until", calendar.getTime());
                                                db.collection("users").document(currentUser).collection("completed_elaborate_quests")
                                                        .document(id).update(temp);
                                            } else {
                                                HashMap<String, Object> temp = new HashMap<>();
                                                temp.put("completed_num", 1);
                                                temp.put("cooldown_until", calendar.getTime());
                                                db.collection("users").document(currentUser).collection("completed_elaborate_quests")
                                                        .document(id).set(temp);
                                            }
                                        }
                                    }
                                }
                            });
                        } else {
                            Log.d("ERROR", "Failed with: ", task.getException());
                        }
                    }
                });
            }

            user_elaborate_quests.remove(id);

            db.collection("users").document(currentUser).collection("user_elaborate_quests")
                    .document(id).delete();
        }
    }

    private boolean elaborateQuestCompletedCheck(String id) {
        HashMap elaborate_quest = user_elaborate_quests.get(id);
        for(Object quest_id : ((HashMap) elaborate_quest.get("quests")).keySet()) {
            HashMap quest = (HashMap) ((HashMap) user_elaborate_quests.get(id).get("quests")).get(quest_id);
            if(! (boolean) quest.get("done"))
                return false;
        }
        if (elaborate_quest.containsKey("meters") && elaborate_quest.get("meters") != null) {
            if(Double.parseDouble(String.valueOf(elaborate_quest.get("meters_traveled"))) < Double.parseDouble(String.valueOf(elaborate_quest.get("meters"))))
                return false;
        }
        return true;
    }

    private boolean elaborateQuestTimeLimitExceeded(String id) {
        HashMap elaborate_quest = user_elaborate_quests.get(id);
        Date currentTime = Calendar.getInstance().getTime();
        Date limit_date;
        if(elaborate_quest.get("time") instanceof Timestamp)
            limit_date = ((Timestamp) elaborate_quest.get("time")).toDate();
        else
            limit_date = (Date) elaborate_quest.get("time");
        return currentTime.after(limit_date);
    }

    private void updateLocQuestPopularity(String id) {
        db.collection("loc_quests").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    if(task.getResult().exists()) {
                        HashMap data = (HashMap) task.getResult().getData();
                        db.collection("loc_quests").document(id)
                                .update("popularity", String.valueOf(Integer.parseInt((String) data.get("popularity")) + 1));
                    }
                }
            }
        });
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        boolean allowed = true;
        for(String id : hashMapMarker.keySet()) {
            if(hashMapMarker.get(id).equals(marker)) {
                for(String id_elaborate : user_elaborate_quests.keySet()) {
                    if(((HashMap) user_elaborate_quests.get(id_elaborate).get("quests")).keySet().contains(id)) {
                        Toast.makeText(requireContext(), "BELONGS TO AN ELABORATE QUESTS", Toast.LENGTH_LONG).show();
                        allowed = false;
                        break;
                    }
                }
                if(allowed)
                    ((MainActivity) getActivity()).showLocQuestPopup(id, hashMapMarker, null, null);
                break;
            }
        }
        return false;
    }

    private int nextLevel(int current_level) {
        return (int) (250 * Math.pow(current_level + 1, 2) - (250 * (current_level + 1)));
    }

    private String getOnTapLocationString(List<Address> addressList) {
        List<Address> addressList1 = null, addressList2 = null, addressList3 = null, addressList4 = null;
        String str = "";
        try {
            Address address = addressList.get(0);
            str = addressToString(address);
            str = str.replaceAll("null, ", "");
            str = deDup(str);

            addressList1 = geocoder.getFromLocationName(str, 1);
            if(address.getAdminArea() != null) addressList2 = geocoder.getFromLocationName(address.getAdminArea(), 1);
            if(address.getSubAdminArea() != null) addressList3 = geocoder.getFromLocationName(address.getSubAdminArea(), 1);
            if(address.getLocality() != null) addressList4 = geocoder.getFromLocationName(address.getLocality(), 1);

        } catch (IOException e) {
            e.printStackTrace();
        }

        str = "";
        if (addressList1 != null) {
            if (addressList1.size() > 0) {
                Address address1 = addressList1.get(0);
                if (address1 != null) {
                    str += addressToString(address1) + ", ";
                }
            }
        }

        if (addressList2 != null) {
            if (addressList2.size() > 0) {
                Address address1 = addressList2.get(0);
                if (address1 != null) {
                    str += addressToString(address1) + ", ";
                }
            }
        }

        if (addressList3 != null) {
            if (addressList3.size() > 0) {
                Address address1 = addressList3.get(0);
                if (address1 != null) {
                    str += addressToString(address1) + ", ";
                }
            }
        }

        if (addressList4 != null) {
            if (addressList4.size() > 0) {
                Address address1 = addressList4.get(0);
                if (address1 != null) {
                    str += addressToString(address1) + ", ";
                }
            }
        }

        str = str.replaceAll("null, ", "");

        return " " + deDup(str);
    }

    public String addressToString(Address address) {
        return address.getFeatureName() + ", " + address.getAdminArea() + ", " +
                address.getSubAdminArea() + ", " + address.getLocality() + ", " + address.getThoroughfare() +
                ", " + address.getCountryName();
    }

    private String deDup(String s) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Arrays.stream(s.split(", ")).distinct().collect(Collectors.joining(", "));
        }
        return s;
    }

    public void setMap(GoogleMap m) {
        googleMap = m;
    }
}