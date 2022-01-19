package com.example.cityquest.Fragments;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cityquest.Activities.MainActivity;
import com.example.cityquest.R;

public class AboutFragment extends Fragment {

    private SensorManager sensorManager;
    private Sensor sensorMemberPhoto;
    private SensorEventListener selMemberPhoto;
    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;

    private TextView txtview;

    public AboutFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_about, container, false);

        txtview = v.findViewById(R.id.profilePhotoText);

        //----------------------------------SHAKE ACCELEROMETER SENSOR----------------------------------
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);

        mAccel = 10f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;

        sensorMemberPhoto = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if(sensorMemberPhoto == null)
            Toast.makeText(getActivity(), "No Accelerometer Sensor To See Group Members", Toast.LENGTH_SHORT).show();

        selMemberPhoto = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                float x = sensorEvent.values[0];
                float y = sensorEvent.values[1];
                float z = sensorEvent.values[2];
                mAccelLast = mAccelCurrent;
                mAccelCurrent = (float) Math.sqrt((double) (x * x + y * y + z * z));
                float delta = mAccelCurrent - mAccelLast;
                mAccel = mAccel * 0.9f + delta;
                if (mAccel > 12) {
                    txtview.setVisibility(View.GONE);

                    //ImageView Setup
                    ImageView imageView1 = v.findViewById(R.id.memberProfile1);
                    ImageView imageView2 = v.findViewById(R.id.memberProfile2);
                    TextView txtviewName = v.findViewById(R.id.profilePhotoTextName);
                    TextView txt = v.findViewById(R.id.profilePhotoText);

                    if(imageView1.getVisibility() == View.INVISIBLE && imageView2.getVisibility() == View.INVISIBLE) {
                        imageView1.setVisibility(View.VISIBLE);
                        txtviewName.setVisibility(View.VISIBLE);
                        txtviewName.setText("Guilherme Lopes - fc52761");
                    } else if(imageView1.getVisibility() == View.VISIBLE) {
                        imageView1.setVisibility(View.INVISIBLE);
                        imageView2.setVisibility(View.VISIBLE);
                        txtviewName.setText("Gonçalo Tristão - fc52743");
                    } else if(imageView2.getVisibility() == View.VISIBLE) {
                        imageView1.setVisibility(View.INVISIBLE);
                        imageView2.setVisibility(View.INVISIBLE);
                        txtviewName.setVisibility(View.INVISIBLE);
                        txt.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {}
        };
        sensorManager.registerListener(selMemberPhoto,sensorMemberPhoto,SensorManager.SENSOR_DELAY_NORMAL);


        return v;
    }

    public void setVisibility(int visibility) {
        txtview.setVisibility(visibility);
    }
}