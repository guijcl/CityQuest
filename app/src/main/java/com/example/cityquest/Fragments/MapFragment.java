package com.example.cityquest.Fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
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

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.cityquest.Activities.MainActivity;
import com.example.cityquest.Objects.LocQuest;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class MapFragment extends Fragment implements GoogleMap.OnMarkerClickListener {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private Geocoder geocoder;

    private String currentUser;
    //private List<HashMap<String, String>> user_loc_quests;
    private MainActivity mainActivity;
    private HashMap<String, HashMap> user_loc_quests;
    private HashMap<String, HashMap> user_elaborate_quests;

    private GoogleMap googleMap;
    private HashMap<String, Marker> hashMapMarker = new HashMap<String, Marker>();

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest mLocationRequest;

    private BottomSheetBehavior bottomSheetBehavior;

    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;

    public MapFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //PERMISSÕES - RESOLVER PROBLEMA DE SER NECESSÁRIO REINICIAR APP PARA FUNCIONAR
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
                //CODE FOR UPDATING USER LOCATION ON MAP (CLEARS ALL MARKERS, SO IGNORE FOR NOW)
                Location location = locationResult.getLastLocation();
                if (googleMap != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    if(geocoder != null) {
                        List<Address> addressList = null;
                        try {
                            addressList = geocoder.getFromLocation(latitude, longitude, 3);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        //COPIAR CÓDIGO DE ONMAPCLICK
                        if (addressList != null) {
                            if (addressList.size() > 0) {
                                Address address = addressList.get(0);
                                String str = address.getAddressLine(0);
                                for(String key : user_loc_quests.keySet()) {
                                    HashMap map = user_loc_quests.get(key);
                                    Log.d("LOCALIZAÇÕES", str + " ; " + (String) map.get("name"));
                                    if(str.contains((String) map.get("name"))) {
                                        //TODO
                                    }
                                }
                            }
                        }
                    }

                    //googleMap.clear();

                    //markerOptions.position(latLng);
                    //googleMap.addMarker(markerOptions);
                }

                /*for(HashMap map : user_loc_quests) {
                    if(geocoder != null) {
                        List<Address> addressList = null;
                        try {
                            double latitude = Double.parseDouble((String) map.get("latitude"));
                            double longitude = Double.parseDouble((String) map.get("longitude"));
                            addressList = geocoder.getFromLocation(latitude, longitude, 3);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        //COPIAR CÓDIGO DE ONMAPCLICK
                        if (addressList != null) {
                            if (addressList.size() > 0) {
                                List<String> listStrs = getOnTapThreeLocationsStrings(addressList);
                                String str1 = listStrs.get(0);
                                String str2 = listStrs.get(1);
                                String str3 = listStrs.get(2);

                                Log.d("LOCALIZAÇÕES1.1", str1 + " ; " + map.get("name"));
                                Log.d("LOCALIZAÇÕES1.2", str2 + " ; " + map.get("name"));
                                Log.d("LOCALIZAÇÕES1.3", str3 + " ; " + map.get("name"));
                            }
                        }
                    }
                }*/

            }
        }, null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_map, container, false);

        /*O QUE É ISTO?
        Toolbar toolbar = view.findViewById(R.id.main_toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);

        DrawerLayout drawerLayout = view.findViewById(R.id.drawer);
        NavigationView navigationView = view.findViewById(R.id.nav_menu);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                requireActivity(), drawerLayout, toolbar, R.string.open, R.string.close
        );

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);*/

        geocoder = new Geocoder(this.requireContext());

        currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if(user_loc_quests.isEmpty()) {
            db.collection("users").document(currentUser).
                    collection("user_loc_quests").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            HashMap<String, String> q = new HashMap<>();
                            q.put("name", (String) document.getData().get("name"));
                            q.put("desc", (String) document.getData().get("desc"));
                            q.put("latitude", String.valueOf(document.getData().get("latitude")));
                            q.put("longitude", String.valueOf(document.getData().get("longitude")));
                            mainActivity.addLocQuest(document.getId(), q);
                        }
                    }
                }
            });
        }

        if(user_elaborate_quests.isEmpty()) {
            db.collection("users").document(currentUser).
                    collection("user_elaborate_quests").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            HashMap<String, Object> q = new HashMap<>();
                            q.put("name", (String) document.getData().get("name"));
                            q.put("desc", (String) document.getData().get("desc"));
                            q.put("quests", (HashMap) document.getData().get("quests"));
                            q.put("meters", String.valueOf(document.getData().get("meters")));
                            q.put("time", String.valueOf(document.getData().get("time")));
                            mainActivity.addElaborateQuest(document.getId(), q);
                        }
                    }
                }
            });
        }

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
                            //markerOptions.position(new LatLng(location.getLatitude(), location.getLongitude()));
                            //markerOptions.title(location.getLatitude() + " : " + location.getLongitude());

                            CameraPosition cameraPosition = new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .zoom(15)
                                    .bearing(90)
                                    .tilt(40)
                                    .build();
                            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                            //googleMap.addMarker(markerOptions); //ADD USER CURRENT LOCATION MARKER
                        }
                    });


            //FREEZES MAP ANIMATION ON START (FIX LATER WITH USE OF CACHE WHICH MAKES ONLY THE FIRST ANIMATION FREEZE)
            db.collection("loc_quests").get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    HashMap data = (HashMap) document.getData();

                                    LatLng latLng = new LatLng(Double.parseDouble(String.valueOf(data.get("latitude"))),
                                            Double.parseDouble(String.valueOf(data.get("longitude"))));

                                    List<String> quests_from_elaborate_quests = new ArrayList<>();
                                    for(String quest_id : user_elaborate_quests.keySet()) {
                                        for(Object loc_quests : ((HashMap) ((HashMap) user_elaborate_quests.get(quest_id)).get("quests")).keySet() ) {
                                            quests_from_elaborate_quests.add((String) loc_quests);
                                        }
                                    }

                                    MarkerOptions markerOptions;
                                    if(quests_from_elaborate_quests.contains(document.getId())) {
                                        markerOptions = new MarkerOptions().
                                                icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)).
                                                position(latLng).title((String) data.get("name"));
                                    } else if(user_loc_quests.containsKey(document.getId())) {
                                        markerOptions = new MarkerOptions().
                                            icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).
                                            position(latLng).title((String) data.get("name"));
                                    } else {
                                        markerOptions = new MarkerOptions().position(latLng).title((String) data.get("name"));
                                    }
                                    Marker marker = googleMap.addMarker(markerOptions);
                                    hashMapMarker.put(document.getId(), marker);
                                }
                            } else {
                                Log.w("ERROR", "Error getting documents.", task.getException());
                            }
                        }
                    });

            //FOR TEST PURPOSES, COMPLETE QUESTS ONMAPCLICK
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
                            /*String str1 = listStrs.get(0), str2 = listStrs.get(1), str3 = listStrs.get(2),
                                    str4 = listStrs.get(3), str5 = listStrs.get(4);*/

                            for(String key : user_loc_quests.keySet()) {
                                //DEBUG
                                HashMap<String, Object> map = user_loc_quests.get(key);
                                Log.d("LOCALIZAÇÕES2.1", str + " ; " + (String) map.get("name"));

                                List<String> tmp_s = Arrays.asList(((String) map.get("name")).split(","));
                                boolean check = true;
                                for(String s : tmp_s) {
                                    Log.d("LOCALIZAÇÕES2.0", str.contains(s) + ";" + s);
                                    if(!str.contains(s))
                                        check = false;
                                }
                                if(check) {
                                    LatLng latLng_marker = new LatLng(Double.parseDouble((String) map.get("latitude")),
                                            Double.parseDouble((String) map.get("longitude")));
                                    Marker to_rmv_marker = hashMapMarker.get(map.get("id"));
                                    MarkerOptions markerOptions = new MarkerOptions().position(latLng_marker).title(to_rmv_marker.getTitle());
                                    to_rmv_marker.remove();
                                    Marker marker = googleMap.addMarker(markerOptions);
                                    hashMapMarker.put(key, marker);
                                    db.collection("users").document(currentUser)
                                            .collection("user_quests").document((String) map.get("id")).delete();

                                    LocQuest n_lq = new LocQuest((String) map.get("name"), (String) map.get("desc"),
                                            Double.parseDouble((String) map.get("latitude")), Double.parseDouble((String) map.get("longitude")));
                                    db.collection("users").document(currentUser).collection("user_completed_quests")
                                            .document((String) map.get("id")).set(n_lq);
                                }
                            }
                        }
                    }
                }
            });

            setMap(googleMap);
        });

        View bottomSheet = view.findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setPeekHeight(200);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        ListView listView = view.findViewById(R.id.listQuests);
        ArrayList<BottomSheetItem> arr = new ArrayList<>();
        while(arr.size() < 3) {
            arr.add(new BottomSheetItem());
        }
        BottomSheetItemAdapter adapter = new BottomSheetItemAdapter(getActivity(),0,arr);
        listView.setAdapter(adapter);

        return view;
    }

    public void setMap(GoogleMap m) {
        googleMap = m;
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        dialogBuilder = new AlertDialog.Builder(requireActivity());
        final View newQuestPopupView = getLayoutInflater().inflate(R.layout.marker_popup, null);

        dialogBuilder.setView(newQuestPopupView);
        dialog = dialogBuilder.create();
        dialog.show();

        return false;
    }

    private String getOnTapLocationString(List<Address> addressList) {
        List<Address> addressList1= null, addressList2 = null, addressList3 = null, addressList4 = null;
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

        String res = "";
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
        res = " " + deDup(str);

        return res;
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

    private static int compareStringsPercentage(String s1, String s2) {
        int percentage = 0;

        int total = 0;
        int fullMatch = 0;
        // Check for each character at same location
        total += charMatch(s1, s2);
        fullMatch += charMatch(s1, s1);

        // Calc percentage
        percentage = (int) Math.round(total / (fullMatch / 100.0));
        return percentage;
    }

    private static int charMatch(String s1, String s2) {
        char[] as1 = s1.toCharArray();
        char[] as2 = s2.toCharArray();
        int match = 0;
        for (int i = 0; i < as1.length; i++) {
            char c = as1[i];
            if (i < as2.length) {
                if (as2[i] == c) {
                    match++;
                }
            }

        }
        return match;
    }

    /*@Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        boolean ret = ((MainActivity) requireActivity()).onNavigationItemSelected(item);
        if(ret) ((DrawerLayout) view.findViewById(R.id.drawer)).closeDrawer(GravityCompat.START);
        return ret;
    }*/
}