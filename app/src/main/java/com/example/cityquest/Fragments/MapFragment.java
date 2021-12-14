package com.example.cityquest.Fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

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
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MapFragment extends Fragment implements GoogleMap.OnMarkerClickListener {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private Geocoder geocoder;

    private String currentUser;
    private List<HashMap<String, String>> user_loc_quests;

    private GoogleMap googleMap;
    private MarkerOptions markerOptions;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest mLocationRequest;

    private BottomSheetBehavior bottomSheetBehavior;

    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;

    public MapFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        markerOptions = new MarkerOptions();

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
                    //LatLng latLng = new LatLng(latitude, longitude);

                    if(geocoder != null) {
                        List<Address> addressList = null;
                        try {
                            addressList = geocoder.getFromLocation(latitude, longitude, 1);
                            //addressList = geocoder.getFromLocation(39.481254, -8.53529, 1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (addressList != null) {
                            if (addressList.size() > 0) {
                                Address address = addressList.get(0);
                                String str = address.getAddressLine(0);
                                for(HashMap map : user_loc_quests) {
                                    Log.d("LOCALIZAÇÕES", str + " ; " + (String) map.get("name"));
                                    if(str.contains((String) map.get("name"))) {
                                        Log.d("CHECK1", "LOCALIZAÇÃO IGUAL");
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

        /*Toolbar toolbar = view.findViewById(R.id.main_toolbar);
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
                            Address address = addressList.get(0);
                            Toast.makeText(requireContext(), address.getAddressLine(0), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

            googleMap.getUiSettings().setCompassEnabled(false);
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(requireActivity(), location -> {
                        if (location != null) {
                            markerOptions.position(new LatLng(location.getLatitude(), location.getLongitude()));
                            markerOptions.title(location.getLatitude() + " : " + location.getLongitude());

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
                                List<Address> addressList = null;
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    HashMap data = (HashMap) document.getData();

                                    HashMap<String, String> temp = new HashMap<>();
                                    temp.put("id", document.getId());
                                    temp.put("name", (String) data.get("name"));
                                    if(!user_loc_quests.contains(temp)) {
                                        try {
                                            addressList = geocoder.getFromLocationName((String) data.get("name"), 1);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        if (addressList != null) {
                                            if (addressList.size() > 0) {
                                                Address address = addressList.get(0);
                                                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                                                googleMap.addMarker(new MarkerOptions().position(latLng).title(address.getFeatureName()));
                                            }
                                        }
                                    }
                                }
                            } else {
                                Log.w("ERROR", "Error getting documents.", task.getException());
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

    /*@Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        boolean ret = ((MainActivity) requireActivity()).onNavigationItemSelected(item);
        if(ret) ((DrawerLayout) view.findViewById(R.id.drawer)).closeDrawer(GravityCompat.START);
        return ret;
    }*/
}