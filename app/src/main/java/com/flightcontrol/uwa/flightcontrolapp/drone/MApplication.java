package com.flightcontrol.uwa.flightcontrolapp.drone;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

public class MApplication extends Application {

    private Registration registration;

    @Override
    protected void attachBaseContext(Context paramContext) {
        super.attachBaseContext(paramContext);
        MultiDex.install(this);
        com.secneo.sdk.Helper.install(MApplication.this);

        if(registration == null)
        {
            registration = new Registration();
            registration.setContext(this);
        }
    }

}