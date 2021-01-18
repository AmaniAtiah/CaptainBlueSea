package com.barmej.captainbluesea.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.barmej.captainbluesea.R;
import com.barmej.captainbluesea.callback.CaptainActionDelegates;
import com.barmej.captainbluesea.domain.TripManager;
import com.barmej.captainbluesea.domain.entity.Trip;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;


public class TripDetailsFragment extends Fragment implements OnMapReadyCallback {
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    public static final String TRIP_DATA = "trip_data";
    private TextView mDateTextView;
    private TextView mFromCountryTextView;
    private TextView mToCountryTextView;
    private TextView mAvailableSeatTextView;
    private TextView mReservedSeatTextView;
    private MapView mMapView;
    private GoogleMap mMap;
    private Button startButton;
    private Button arrivedButton;
    private CaptainActionDelegates captainActionDelegates;
    private Trip trip;
    private TextView mFinishTripTextView;
    private Marker currentMarker;
    private Marker destinationMarker;
    private Marker pickUpMarker;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient location;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        trip = getArguments().getParcelable(TRIP_DATA);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,@Nullable ViewGroup container,@Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trip_details,container,false);

    }

    @Override
    public void onViewCreated(@NonNull View view,@Nullable Bundle savedInstanceState) {
        super.onViewCreated(view,savedInstanceState);
        mMapView = view.findViewById(R.id.map_view);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);

        mDateTextView = view.findViewById(R.id.text_view_trip_date);
        mFromCountryTextView = view.findViewById(R.id.text_view_from_country);
        mToCountryTextView = view.findViewById(R.id.text_view_to_country);
        mAvailableSeatTextView = view.findViewById(R.id.text_view_available_seat);
        mReservedSeatTextView = view.findViewById(R.id.text_view_reserved_seat);
        startButton = view.findViewById(R.id.start_trip_button);
        arrivedButton = view.findViewById(R.id.arrived_button);
        mFinishTripTextView = view.findViewById(R.id.text_view_finish_trip);

        if (trip != null) {
            mDateTextView.setText(trip.getFormattedDate());
            mFromCountryTextView.setText(trip.getFromCountry());
            mToCountryTextView.setText(trip.getToCountry());
            mAvailableSeatTextView.setText(String.valueOf(trip.getAvailableSeats()));
            mReservedSeatTextView.setText(String.valueOf(trip.getReservedSeats()));
        }

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captainActionDelegates.startTrip();
            }
        });

        arrivedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captainActionDelegates.arrivedTrip();

            }
        });

    }

    public void showArrivedScreen(Trip trip) {
        reset();
        updateWithStatus(trip);
    }

    public void showOnTripView(Trip trip) {
        setCurrentMarker(new LatLng(trip.getCurrentLat(), trip.getCurrentLng()));
        setDestinationMarker(new LatLng(trip.getDestinationLat(), trip.getDestinationLng()));
        setPickUpMarker(new LatLng(trip.getPickupLat(), trip.getPickupLng()));
        updateWithStatus(trip);
    }

    public void updateWithStatus(Trip trip) {
        String tripStatus = trip.getStatus();
        if (tripStatus.equals(Trip.Status.AVAILABLE.name())) {
            hideAllViews();
            startButton.setVisibility(View.VISIBLE);

        }else if (tripStatus.equals(Trip.Status.START_TRIP.name())) {
            hideAllViews();
            arrivedButton.setVisibility(View.VISIBLE);

        } else if (tripStatus.equals(Trip.Status.ARRIVED.name())) {
            hideAllViews();
            mFinishTripTextView.setVisibility(View.VISIBLE);
        }
        mAvailableSeatTextView.setText(String.valueOf(trip.getAvailableSeats()));
        mReservedSeatTextView.setText(String.valueOf(trip.getReservedSeats()));
    }

    private void hideAllViews() {
        startButton.setVisibility(View.GONE);
        arrivedButton.setVisibility(View.GONE);
        mFinishTripTextView.setVisibility(View.GONE);
        mFinishTripTextView.setVisibility(View.GONE);
    }

    public void setCaptainActionDelegates(CaptainActionDelegates captainActionDelegates) {
        this.captainActionDelegates = captainActionDelegates;
    }

    public void checkLocationPermissionAndSetUpUserLocation() {
        if (ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            setUpUserLocation();
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.ACCESS_FINE_LOCATION}
                    , REQUEST_LOCATION_PERMISSION);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        checkLocationPermissionAndSetUpUserLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,@NonNull String[] permissions,@NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (permissions.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setUpUserLocation();
            } else {
                Toast.makeText(getActivity(),R.string.location_permission_needed,Toast.LENGTH_SHORT).show();

            }
        } else {
            super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        }
    }

    @SuppressLint("MissingPermission")
    private void setUpUserLocation() {
        if (mMap == null)
            return;
        mMap.setMyLocationEnabled(true);

        FusedLocationProviderClient locationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        locationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    CameraUpdate update = CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f);
                    mMap.moveCamera(update);

                }
            }
        });

        trackAndSendLocationUpdates();
    }

    @SuppressLint("MissingPermission")
    private void trackAndSendLocationUpdates() {

        if (locationCallback == null) {
            location = LocationServices.getFusedLocationProviderClient(getContext());

            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(1000);

            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    Location lastLocation = locationResult.getLastLocation();
                    TripManager.getInstance().updateCurrentLocation(lastLocation.getLatitude(), lastLocation.getLongitude());
                }
            };

            location.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    public void setCurrentMarker(LatLng target) {
        if (mMap == null)
            return;
        if (currentMarker == null) {
            BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(R.drawable.boat);
            MarkerOptions options = new MarkerOptions();
            options.icon(descriptor);
            options.position(target);
            currentMarker = mMap.addMarker(options);

        } else {
            currentMarker.setPosition(target);
        }
    }

    public void setPickUpMarker(LatLng target) {
        if (mMap == null) return;

        if (pickUpMarker == null) {
            BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(R.drawable.position);
            MarkerOptions options = new MarkerOptions();
            options.icon(descriptor);
            options.position(target);

            pickUpMarker = mMap.addMarker(options);

        } else {
            pickUpMarker.setPosition(target);
        }
    }

    public void setDestinationMarker(LatLng target) {
        if (mMap == null) return;

        if (destinationMarker == null) {
            BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(R.drawable.destination);
            MarkerOptions options = new MarkerOptions();
            options.icon(descriptor);
            options.position(target);

            destinationMarker = mMap.addMarker(options);

        } else {
            destinationMarker.setPosition(target);
        }
    }

    public void stopLocationUpdates() {
        if (location != null && locationCallback != null) {
            location.removeLocationUpdates(locationCallback);
            locationCallback = null;
        }
    }

    public void reset() {
        if (mMap == null) return;
        mMap.clear();
        currentMarker = null;
        pickUpMarker = null;
        destinationMarker = null;
        setUpUserLocation();
    }
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }
}
