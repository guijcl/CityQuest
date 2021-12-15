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
    private List<HashMap<String, String>> user_loc_quests;

    private GoogleMap googleMap;
    //private MarkerOptions markerOptions;
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

        //markerOptions = new MarkerOptions();

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
                                for(HashMap map : user_loc_quests) {
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

        user_loc_quests = new ArrayList<>();
        currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").document(currentUser).
                collection("user_quests").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        HashMap<String, String> q = new HashMap<>();
                        q.put("id", document.getId());
                        q.put("name", (String) document.getData().get("name"));
                        q.put("desc", (String) document.getData().get("desc"));
                        q.put("latitude", String.valueOf(document.getData().get("latitude")));
                        q.put("longitude", String.valueOf(document.getData().get("longitude")));
                        user_loc_quests.add(q);
                    }
                }
            }
        });

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

                                    HashMap<String, String> temp = new HashMap<>();
                                    temp.put("id", document.getId());
                                    temp.put("name", (String) data.get("name"));
                                    temp.put("desc", (String) data.get("desc"));
                                    temp.put("latitude", String.valueOf(data.get("latitude")));
                                    temp.put("longitude", String.valueOf(data.get("longitude")));

                                    LatLng latLng = new LatLng(Double.parseDouble(temp.get("latitude")),
                                            Double.parseDouble(temp.get("longitude")));

                                    MarkerOptions markerOptions;
                                    if(!user_loc_quests.contains(temp)) {
                                        markerOptions = new MarkerOptions().position(latLng).title((String) data.get("name"));
                                    } else {
                                        markerOptions = new MarkerOptions().
                                                icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).
                                                position(latLng).title((String) data.get("name"));
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
                        addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 3);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (addressList != null) {
                        if (addressList.size() > 0) {

                            List<String> listStrs = getOnTapThreeLocationsStrings(addressList);
                            String str1 = listStrs.get(0);
                            String str2 = listStrs.get(1);
                            String str3 = listStrs.get(2);

                            for(HashMap<String, String> map : user_loc_quests) {
                                /*DEBUG
                                Log.d("LOCALIZAÇÕES2.1", str1 + " ; " + (String) map.get("name"));
                                Log.d("LOCALIZAÇÕES2.2", str2 + " ; " + (String) map.get("name"));
                                Log.d("LOCALIZAÇÕES2.3", str3 + " ; " + (String) map.get("name"));*/

                                List<String> tmp_s = Arrays.asList(((String) map.get("name")).split(","));
                                boolean check = true;
                                for(String s : tmp_s) {
                                    if(!str1.contains(s) && !str2.contains(s) && !str3.contains(s))
                                        check = false;
                                }
                                if(check) {
                                    //Log.d("PASSED1", String.valueOf(check));
                                    LatLng latLng_marker = new LatLng(Double.parseDouble(map.get("latitude")),
                                            Double.parseDouble(map.get("longitude")));
                                    Marker to_rmv_marker = hashMapMarker.get(map.get("id"));
                                    MarkerOptions markerOptions = new MarkerOptions().position(latLng_marker).title(to_rmv_marker.getTitle());
                                    to_rmv_marker.remove();
                                    Marker marker = googleMap.addMarker(markerOptions);
                                    hashMapMarker.put(map.get("id"), marker);
                                    db.collection("users").document(currentUser)
                                            .collection("user_quests").document((String) map.get("id")).delete();

                                    LocQuest n_lq = new LocQuest(map.get("name"), map.get("desc"),
                                            Double.parseDouble(map.get("latitude")), Double.parseDouble(map.get("longitude")));
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

    private List<String> getOnTapThreeLocationsStrings(List<Address> addressList) {
        Address address1 = null, address2 = null, address3 = null;
        if(addressList.size() >= 1) address1 = addressList.get(0);
        if(addressList.size() >= 2) address2 = addressList.get(1);
        if(addressList.size() >= 3) address3 = addressList.get(2);

        List<String> res = new ArrayList<>();

        String str1 = "", str2 = "", str3 = "";
        if(address1 != null) {
            str1 = address1.getFeatureName() + ", " + address1.getAdminArea() + ", " +
                    address1.getSubAdminArea() + ", " + address1.getLocality() + ", " + address1.getThoroughfare() +
                    ", " + address1.getCountryName();
            str1 = str1.replaceAll("null, ", "");
            str1 = deDup(str1);
        }

        if(address2 != null) {
            str2 = address2.getFeatureName() + ", " + address2.getAdminArea() + ", " +
                    address2.getSubAdminArea() + ", " + address2.getLocality() + ", " + address2.getThoroughfare() +
                    ", " + address2.getCountryName();
            str2 = str2.replaceAll("null, ", "");
            str2 = deDup(str2);
        }

        if(address3 != null) {
            str3 = address3.getFeatureName() + ", " + address3.getAdminArea() + ", " +
                    address3.getSubAdminArea() + ", " + address3.getLocality() + ", " + address3.getThoroughfare() +
                    ", " + address3.getCountryName();
            str3 = str3.replaceAll("null, ", "");
            str3 = deDup(str3);
        }

        res.add(str1);
        res.add(str2);
        res.add(str3);

        return res;
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