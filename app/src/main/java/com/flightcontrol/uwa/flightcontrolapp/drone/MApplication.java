package com.flightcontrol.uwa.flightcontrolapp.drone;

import android.app.Application;
import android.content.Context;

import com.secneo.sdk.Helper;

public class MApplication extends Application {

    private  Registration registration;

    @Override
    protected void attachBaseContext(Context paramContext) {
        super.attachBaseContext(paramContext);
        Helper.install(MApplication.this);

        if(registration == null)
        {
            registration = new Registration();
            registration.setContext(this);
        }
    }

}