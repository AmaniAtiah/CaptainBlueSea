package com.barmej.captainbluesea.callback;


import com.barmej.captainbluesea.domain.entity.Trip;

public interface StatusCallBack {
    void onUpdate(Trip trip);
}
