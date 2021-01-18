package com.barmej.captainbluesea;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.barmej.captainbluesea.callback.AddPointCommunicationInterface;
import com.barmej.captainbluesea.fragment.AddPointOnMapFragment;
import com.barmej.captainbluesea.fragment.MapFragment;
import com.google.android.gms.maps.model.LatLng;

import static com.barmej.captainbluesea.AddTripActivity.REQUEST_TYPE;

public class MapsActivity extends AppCompatActivity {
    public static final String PICKUP = "pickup";
    public static final String DESTINATION = "destination";
    private MapFragment mapsFragment;
    private AddPointOnMapFragment addPointOnMapFragment;
    private AddPointCommunicationInterface pointCommunicationInterface;

    private LatLng pickUpLatLng;
    private LatLng destinationLatLng;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        FragmentManager manager = getSupportFragmentManager();

        mapsFragment = (MapFragment) manager.findFragmentById(R.id.map_container_fragment);
        addPointOnMapFragment = (AddPointOnMapFragment) manager.findFragmentById(R.id.add_point);

        addPointOnMapFragment.setRequestType(getIntent().getIntExtra(REQUEST_TYPE, 0));
        initListenerDelegates();
        addPointOnMapFragment.setActionDelegates(pointCommunicationInterface);
    }

    private void initListenerDelegates() {
        pointCommunicationInterface = new AddPointCommunicationInterface() {
            @Override
            public boolean setPickup() {
                pickUpLatLng = mapsFragment.captureCenter();
                if (pickUpLatLng != null) {
                    mapsFragment.setPickUpMarker(pickUpLatLng);
                    return true;
                }
                return false;
            }

            @Override
            public boolean setDestination() {
                destinationLatLng = mapsFragment.captureCenter();
                if (destinationLatLng != null) {
                    mapsFragment.setDestinationMarker(destinationLatLng);
                    return true;
                }
                return false;
            }

            @Override
            public void addPickup() {
                Intent intent = new Intent();
                intent.putExtra(PICKUP,pickUpLatLng);
                setResult(RESULT_OK, intent);
                finish();
            }

            @Override
            public void addDestination() {
                Intent intent = new Intent();
                intent.putExtra(DESTINATION,destinationLatLng);
                setResult(RESULT_OK, intent);
                finish();

            }
        };
    }

}