package com.barmej.captainbluesea.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.barmej.captainbluesea.AddTripActivity;
import com.barmej.captainbluesea.R;
import com.barmej.captainbluesea.TripDetailsActivity;
import com.barmej.captainbluesea.TripListAdapter;
import com.barmej.captainbluesea.domain.entity.Trip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.barmej.captainbluesea.AddTripActivity.TRIP_REF_PATH;

public class TripListFragment extends Fragment implements TripListAdapter.OnTripClickListener {
    private RecyclerView mRecyclerViewTrip;
    private TripListAdapter mTripsListAdapter;
    private ArrayList<Trip> mTrips;
    private Button mAddTripButton;
    private TextView youDidntAddAnyTrip;
    private Trip trip;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,@Nullable ViewGroup container,@Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trips_list,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view,@Nullable Bundle savedInstanceState) {
        super.onViewCreated(view,savedInstanceState);

        mRecyclerViewTrip = view.findViewById(R.id.recycler_view_trip);
        mAddTripButton = view.findViewById(R.id.add_trip_button);
        mRecyclerViewTrip.setLayoutManager(new LinearLayoutManager(getContext()));
        youDidntAddAnyTrip = view.findViewById(R.id.you_didnt_add_any_trip);

        mTrips = new ArrayList<>();
        mTripsListAdapter = new TripListAdapter(mTrips, TripListFragment.this);
        mRecyclerViewTrip.setAdapter(mTripsListAdapter);

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseDatabase.getReference(TRIP_REF_PATH).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mTrips.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                     trip = dataSnapshot.getValue(Trip.class);
                    String tripStatus = trip.getStatus();
                    if (trip != null) {
                        if (trip.getId().startsWith(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            if (tripStatus.equals(Trip.Status.AVAILABLE.name()) ||
                                    tripStatus.equals(Trip.Status.START_TRIP.name())) {
                                mTrips.add(trip);
                                youDidntAddAnyTrip.setVisibility(View.GONE);
                                mRecyclerViewTrip.setVisibility(View.VISIBLE);
                            } else if (tripStatus.equals(Trip.Status.ARRIVED.name())) {
                                mRecyclerViewTrip.setVisibility(View.GONE);
                            }
                        }
                    }
                }
                if (trip == null) {
                    youDidntAddAnyTrip.setVisibility(View.VISIBLE);
                }
                mTripsListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mAddTripButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), AddTripActivity.class));

            }
        });
    }

    @Override
    public void onTripClick(Trip trip) {
        Intent intent = new Intent(getContext(), TripDetailsActivity.class);
        intent.putExtra(TripDetailsFragment.TRIP_DATA, trip);
        startActivity(intent);
    }
}
