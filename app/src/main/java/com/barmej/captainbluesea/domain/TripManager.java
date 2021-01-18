package com.barmej.captainbluesea.domain;


import androidx.annotation.NonNull;

import com.barmej.captainbluesea.callback.StatusCallBack;
import com.barmej.captainbluesea.domain.entity.Trip;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.barmej.captainbluesea.AddTripActivity.TRIP_REF_PATH;

public class TripManager {
    private FirebaseDatabase database;
    private static TripManager instance;
    private Trip trip;
    private ValueEventListener tripListener;

    private StatusCallBack statusCallBack;

    public TripManager() {
        database = FirebaseDatabase.getInstance();
    }

    public static TripManager getInstance() {
        if (instance == null) {
            instance = new TripManager();
        }
        return instance;
    }

    public void startListeningForStatus(StatusCallBack statusCallBack, String id) {
        this.statusCallBack = statusCallBack;
        tripListener = database.getReference(TRIP_REF_PATH).child(id)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        trip = dataSnapshot.getValue(Trip.class);
                        if (trip != null) {
                            notifyListener(trip);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void notifyListener(Trip trip) {
        if (statusCallBack != null) {
            statusCallBack.onUpdate(trip);
        }
    }

    public void updateTrip() {
        trip.setStatus(Trip.Status.START_TRIP.name());
        database.getReference(TRIP_REF_PATH).child(trip.getId()).setValue(trip);
        notifyListener(trip);
    }

    public void updateTripToArrived() {
        trip.setStatus(Trip.Status.ARRIVED.name());
        database.getReference(TRIP_REF_PATH).child(trip.getId()).setValue(trip);
        notifyListener(trip);
    }

    public void updateCurrentLocation(double lat, double lng) {
        trip.setCurrentLat(lat);
        trip.setCurrentLng(lng);
        database.getReference(TRIP_REF_PATH).child(trip.getId()).setValue(trip);
    }

    public void stopListeningToStatus() {
        if (tripListener != null) {
            database.getReference().child(trip.getId()).removeEventListener(tripListener);
        }
        statusCallBack = null;
    }

}
