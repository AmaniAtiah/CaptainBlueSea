package com.barmej.captainbluesea;

import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.barmej.captainbluesea.callback.CaptainActionDelegates;
import com.barmej.captainbluesea.callback.StatusCallBack;
import com.barmej.captainbluesea.domain.TripManager;
import com.barmej.captainbluesea.domain.entity.Trip;
import com.barmej.captainbluesea.fragment.TripDetailsFragment;

import static com.barmej.captainbluesea.fragment.TripDetailsFragment.TRIP_DATA;

public class TripDetailsActivity extends AppCompatActivity {

    private FrameLayout frameLayout;
    private CaptainActionDelegates captainActionDelegates;
    private TripDetailsFragment tripDetailsFragment;
    private Trip trip;
    private StatusCallBack statusCallBack = getStatusCallBack();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);

        if (getIntent() != null && getIntent().getExtras() != null) {
            trip = getIntent().getExtras().getParcelable(TRIP_DATA);
        } else {
            throw new RuntimeException("You must pass trip object for this Activity");
        }

        frameLayout = findViewById(R.id.frame_layout);
        tripDetailsFragment = new TripDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(TRIP_DATA, trip);
        tripDetailsFragment.setArguments(bundle);
        setFragment(tripDetailsFragment);

        getCaptainActionDelegates();
        tripDetailsFragment.setCaptainActionDelegates(captainActionDelegates);
    }

    private StatusCallBack getStatusCallBack() {
        return new StatusCallBack() {
            @Override
            public void onUpdate(Trip trip) {
                String tripStatus = trip.getStatus();
                if (tripStatus.equals(Trip.Status.AVAILABLE.name())) {
                    tripDetailsFragment.updateWithStatus(trip);
                } else if (tripStatus.equals(Trip.Status.START_TRIP.name())){
                    tripDetailsFragment.showOnTripView(trip);
                    tripDetailsFragment.checkLocationPermissionAndSetUpUserLocation();
                } else if (tripStatus.equals(Trip.Status.ARRIVED.name())) {
                    tripDetailsFragment.showArrivedScreen(trip);
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        TripManager.getInstance().startListeningForStatus(statusCallBack, trip.getId());
    }

    private void getCaptainActionDelegates() {
        captainActionDelegates = new CaptainActionDelegates() {
            @Override
            public void startTrip() {
                TripManager.getInstance().updateTrip();
            }

            @Override
            public void arrivedTrip() {
                TripManager.getInstance().updateTripToArrived();
            }

        };
    }

    public void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(frameLayout.getId(), fragment);
        fragmentTransaction.commit();
    }

    @Override
    protected void onStop() {
        super.onStop();
        TripManager.getInstance().stopListeningToStatus();
        tripDetailsFragment.stopLocationUpdates();
    }

}
