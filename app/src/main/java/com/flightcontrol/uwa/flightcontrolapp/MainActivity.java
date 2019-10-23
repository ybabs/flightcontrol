package com.flightcontrol.uwa.flightcontrolapp;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.flightcontrol.uwa.flightcontrolapp.drone.Registration;
import com.flightcontrol.uwa.flightcontrolapp.utils.DataUtil;
import  com.flightcontrol.uwa.flightcontrolapp.utils.MathUtil;
import  com.flightcontrol.uwa.flightcontrolapp.utils.MissionConfigDataManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import dji.common.battery.BatteryState;
import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.model.LocationCoordinate2D;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKInitEvent;
import dji.sdk.sdkmanager.DJISDKManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, GoogleMap.OnMapClickListener, OnMapReadyCallback{

    private static MainActivity mainActivityInstance;

    private static final String[] REQUIRED_PERMISSION_LIST = new String[]{
            Manifest.permission.VIBRATE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
    };

    private AtomicBoolean isRegistrationInProgress = new AtomicBoolean(false);
    private static final int REQUEST_PERMISSION_CODE = 12345;
    private List<String> missingPermission = new ArrayList<>();
    public static final String FLAG_CONNECTION_CHANGE = "dji_sdk_connection_change";

    private GoogleMap googleMap;
    private Handler mHandler;
    ArrayList<Marker> markerArrayList = new ArrayList<>();

    private float mAltitude = 10.0f;
    private float mSpeed = 2.0f;
    private  float mSampleTime = 0.0f;
    private Marker droneMarker = null;

    private LinearLayout takeoff, land, goHome;
    private Button start, abort;
    private LinearLayout newMission;

    //Buttons on the mission config page
    private Button missionOk, missionCancel;

    private Button playPause;

    // Load preloaded missions button
    private Button preloadedMission;

    // Seekbars in flight config page
    private SeekBar speedSeekbar;

    private RelativeLayout flightConfigPanel;

    // radioGroups on Flight config page

    private RadioGroup missionEndRadioGroup;


    private Button missionSettingsBtn;

    //Telemetry Status
    private ImageView batteryStatusImageView;



    private TextView droneStatusTextView;
    private TextView satelliteCountTextView;
    private TextView batteryLevelTextView;
    private TextView latitudeTextView;
    private TextView longitudeTextView;
    private  TextView altitudeTextView;
    private TextView homeDistanceTextView;
    private TextView verticalspeedtextView;
    private TextView horizontalspeedtextView;

    private double droneLocationLatitude;
    private double droneLocationLongitude;
    private double droneLocationAltitude;
    private double droneDistanceToHome;
    private double droneVerticalSpeed;
    private double droneHorizontalSpeed;

    private TextView speedSeekbarTextView;
    private TextView waypointAltitudeTextView;
    private TextView sampleTimeTextView;

    private RelativeLayout mainActivityLayout;

    private LinearLayout waypointSettings;

    boolean isPlay = false; // set drone pause and play missions

    private int batteryVoltage;
    private float batteryTemp;
    private int batteryChargeRemaining;
    private int batteryChargeInPercent;
    private int satelliteCount = -1;
    private int droneHeight;

    byte[] data = {0};

    byte parseCheckedResultInt = 0;
    byte[] param_data = {0};


      double [] fixedLat  = {52.756217, 52.756109, 52.756109, 52.755813, 52.755708, 52.755711, 52.755704, 52.755876, 52.756151, 52.756183 };
      double [] fixedLon = {-1.246959,-1.247070,-1.247093,-1.247105, -1.246833,-1.246443, -1.245985,-1.245634,-1.245655, -1.246334};
      double [] fixedAlt = {10,10,10,10,10,10,10,10,10,10};

      private int i;

    //Lists to store Mission Waypoints
   // double [] missionWaypointsArray = {fixedLat, fixedLon, fixedAlt};


    // dialog checkbox
    private CheckBox samplingCheckbox;


    private FusedLocationProviderClient mFusedLocationClient;
    SupportMapFragment mapFragment;

    private FlightController mFlightController;


    private static final String TAG = MainActivity.class.getName();

    MissionConfigDataManager missionConfigDataManager;

    private LatLng homeLatLng;

    public static MainActivity getInstance()
    {
        return mainActivityInstance;
    }

    public MainActivity()
    {
        if (mainActivityInstance == null)
        {
            mainActivityInstance = this;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        missionConfigDataManager = new MissionConfigDataManager();
        super.onCreate(savedInstanceState);


        setWindowAttributes();



        // When the compile and target version is higher than 22, please request the following permission at runtime to ensure the SDK works well.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkAndRequestPermissions();
        }

        setContentView(R.layout.activity_main);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Registration.FLAG_CONNECTION_CHANGE);
        registerReceiver(mReceiver, filter);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapview);
        mapFragment.getMapAsync(this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mHandler = new Handler(Looper.getMainLooper());


        initFlightController();
        initUI();
        initOnClickListener();
        setHomeLocation();

    }

    private void initFlightController()
    {

        BaseProduct product = Registration.getProductInstance();
        if (product != null && product.isConnected()) {
            if (product instanceof Aircraft) {
                mFlightController = ((Aircraft) product).getFlightController();
            }
        }

        if (mFlightController != null) {

            updateFlightData();
            addCallback();
            updateBatteryStatus();
        }


    }



    private void addCallback() {
        mFlightController.setOnboardSDKDeviceDataCallback(new FlightController.OnboardSDKDeviceDataCallback() {
            @Override
            public void onReceive(byte[] bytes) {

                //showToast("Data Received: " + bytes[0]);


                if(bytes[0] == 0x02)
                {
                    showToast("Error Uploading Waypoints");
                    //Toast.makeText(getApplicationContext(), "Error Uploading Waypoints", Toast.LENGTH_SHORT).show();
                }

                if(bytes[0] == 0x01)
                {
                    showToast("Waypoints Uploaded");
                    //Toast.makeText(getApplicationContext(), "Waypoints Uploaded", Toast.LENGTH_SHORT).show();

                }

                if(bytes[0] == 0x01)
                {
                    showToast("Mission Started");
                    //Toast.makeText(getApplicationContext(), "Mission Started", Toast.LENGTH_SHORT).show();

                }

            }
        });
    }

    private void setHomeLocation() {
        if (mFlightController != null) {

            mFlightController.setHomeLocation(new LocationCoordinate2D(homeLatLng.latitude, homeLatLng.longitude), new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    String Text1 = Double.toString(homeLatLng.latitude);
                    String Text2 = Double.toString(homeLatLng.longitude);
                    showToast("Home Location set: " + Text1 + " ," + Text2 + (djiError == null ? " Successfully" : djiError.getDescription()));
                }
            });
        }
    }


    private void updateBatteryStatus()
    {
        final Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run()
            {
                BaseProduct product = Registration.getProductInstance();
                if (product != null && product.isConnected()) {
                    if (product instanceof Aircraft) {
                        product.getBattery().setStateCallback(new BatteryState.Callback() {
                            @Override
                            public void onUpdate(BatteryState batteryState) {
                                batteryChargeInPercent = batteryState.getChargeRemainingInPercent();
                                batteryChargeRemaining = batteryState.getChargeRemaining();
                                batteryVoltage = batteryState.getVoltage();

                                batteryTemp = batteryState.getTemperature();

                                updateBatteryImageView();

                            }
                        });
                    }
                }
                mHandler.postDelayed(this, 1000);

            }
        }, 1000);

    }

    // Decided not to use the data from the onboard SDK for this bit.
    private void updateFlightData()
    {
        mFlightController.setStateCallback(new FlightControllerState.Callback() {
            @Override
            public void onUpdate(@NonNull FlightControllerState flightControllerState) {
                droneLocationLatitude = flightControllerState.getAircraftLocation().getLatitude();
                droneLocationLongitude = flightControllerState.getAircraftLocation().getLongitude();
                droneLocationAltitude = flightControllerState.getAircraftLocation().getAltitude();
                droneDistanceToHome = MathUtil.CoordinateToDistanceConverter(flightControllerState.getHomeLocation().getLatitude(),
                        flightControllerState.getHomeLocation().getLongitude(), flightControllerState.getAircraftLocation().getLatitude(),
                        flightControllerState.getAircraftLocation().getLongitude());


                //showToast("Distance = " + droneDistanceToHome + " vs" + secondValue);
                droneVerticalSpeed = (int) (flightControllerState.getVelocityZ() * 10) == 0 ? 0.0000f : (-1.0) * flightControllerState.getVelocityZ();
                droneHorizontalSpeed = MathUtil.computeScalarVelocity(flightControllerState.getVelocityX(), flightControllerState.getVelocityY());
                updateDroneLocation();
                satelliteCount = flightControllerState.getSatelliteCount();




                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        homeDistanceTextView.setText(String.format("%.2f", droneDistanceToHome) + " m");
                        horizontalspeedtextView.setText(String.format("%.2f",droneHorizontalSpeed) + " m/s");
                        verticalspeedtextView.setText(String.format("%.2f",droneVerticalSpeed) + " m/s");
                        latitudeTextView.setText(String.format("%.6f",droneLocationLatitude));
                        longitudeTextView.setText(String.format("%.6f",droneLocationLongitude));
                        altitudeTextView.setText(String.format("%.1f",droneLocationAltitude) + " m");
                        satelliteCountTextView.setText(String.valueOf(satelliteCount));
                    }
                });



            }
        });


    }

    private void initOnClickListener()
    {
        newMission.setClickable(true);
        land.setClickable(true);
        takeoff.setClickable(true);
        goHome.setClickable(true);


        newMission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                flightConfigPanel.setVisibility(view.VISIBLE);

            }
        });


        land.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                byte [] LAND_CMD = {0x03};
                missionConfigDataManager.sendCommand(LAND_CMD, mFlightController);

            }
        });

        takeoff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte [] TAKEOFF_CMD = {0x01};
                missionConfigDataManager.sendCommand(TAKEOFF_CMD, mFlightController);
            }
        });

        goHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte [] GOHOME_CMD = {0x02};
                missionConfigDataManager.sendCommand(GOHOME_CMD, mFlightController);

            }
        });
    }

    @Override
    public void onResume() {
        Log.e(TAG, "onResume");

        mapFragment.onResume();
        initFlightController();
        setHomeLocation();
        super.onResume();


    }

    @Override
    public void onPause() {
        Log.e(TAG, "onPause");

        mapFragment.onPause();
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.e(TAG, "onStop");
        super.onStop();
    }

    public void onReturn(View view){
        Log.e(TAG, "onReturn");
        this.finish();
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy");
        unregisterReceiver(mReceiver);
        mapFragment.onDestroy();
        super.onDestroy();
    }

    private void setWindowAttributes()
    {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                |View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                |View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                |View.SYSTEM_UI_FLAG_FULLSCREEN
                |View.SYSTEM_UI_FLAG_IMMERSIVE
                |View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        getWindow().getAttributes().systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE;
    }

    private void checkAndRequestPermissions() {
        // Check for permissions
        for (String eachPermission : REQUIRED_PERMISSION_LIST) {
            if (ContextCompat.checkSelfPermission(this, eachPermission) != PackageManager.PERMISSION_GRANTED) {
                missingPermission.add(eachPermission);
            }
        }
        // Request for missing permissions
        if (missingPermission.isEmpty()) {
            startSDKRegistration();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            showToast("Need to grant the permissions!");
            ActivityCompat.requestPermissions(this,
                    missingPermission.toArray(new String[missingPermission.size()]),
                    REQUEST_PERMISSION_CODE);
        }

    }

    /**
     * Result of runtime permission request
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Check for granted permission and remove from missing list
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = grantResults.length - 1; i >= 0; i--) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    missingPermission.remove(permissions[i]);
                }
            }
        }
        // If there is enough permission, we will start the registration
        if (missingPermission.isEmpty()) {
            startSDKRegistration();
        } else {
            showToast("Missing permissions!!!");
        }
    }

    public void startSDKRegistration()
    {
        if (isRegistrationInProgress.compareAndSet(false, true)) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    showToast("registering, pls wait...");
                    DJISDKManager.getInstance().registerApp(getApplicationContext(), new DJISDKManager.SDKManagerCallback() {
                        @Override
                        public void onRegister(DJIError djiError) {
                            if (djiError == DJISDKError.REGISTRATION_SUCCESS) {
                                showToast("App successfully registered");
                                DJISDKManager.getInstance().startConnectionToProduct();
                            } else {
                                showToast("Register sdk fails, please check the bundle id and network connection!");
                            }
                            Log.v(TAG, djiError.getDescription());
                        }

                        @Override
                        public  void onProductDisconnect()
                        {
                            Log.d(TAG, "OnProductDisconnect");
                            showToast("Aircraft is Disconnected");
                            notifyStatusChange();
                        }

                        @Override
                        public void onProductConnect(BaseProduct baseProduct)
                        {
                            Log.d(TAG, String.format("onProductConnect newProduct:%s", baseProduct));
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showToast("The Aircraft is connected");
                                    droneStatusTextView.setText(Registration.getProductInstance().getModel().toString());

                                }
                            });

                            notifyStatusChange();



                        }

                        @Override
                        public  void onComponentChange(BaseProduct.ComponentKey componentKey, BaseComponent oldComponent,
                                                       BaseComponent newComponent)
                        {

                            if(newComponent!=null)
                            {
                                newComponent.setComponentListener(new BaseComponent.ComponentListener() {
                                    @Override
                                    public void onConnectivityChange(boolean isConnected) {
                                        Log.d(TAG, "onComponentConnectivityChanged" + isConnected);

                                        if(isConnected)
                                        {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    droneStatusTextView.setText(Registration.getProductInstance().getModel().toString());

                                                }
                                            });
                                            showToast("The Aircraft is connected");

                                        }

                                        else
                                        {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    showToast("The Aircraft has been disconnected");
                                                    droneStatusTextView.setText("Aircraft is not connected");
                                                    satelliteCountTextView.setText("N/A");
                                                    latitudeTextView.setText("N/A");
                                                    longitudeTextView.setText("N/A");
                                                    altitudeTextView.setText("N/A");
                                                    homeDistanceTextView.setText("N/A");
                                                    verticalspeedtextView.setText("N/A");
                                                    horizontalspeedtextView.setText("N/A");
                                                    batteryLevelTextView.setText("N/A");

                                                }
                                            });
                                        }

                                        notifyStatusChange();
                                    }
                                });
                            }

                            Log.d(TAG, String.format("onComponentChange key: %s , oldComponent:%s, newComponent:%s",
                                    componentKey, oldComponent, newComponent));

                        }

                        @Override
                        public void onInitProcess(DJISDKInitEvent djisdkInitEvent, int i) {

                        }

                        @Override
                        public void onDatabaseDownloadProgress(long l, long l1) {

                        }


                    });
                }
            });
        }
    }

    private void notifyStatusChange() {
        mHandler.removeCallbacks(updateRunnable);
        mHandler.postDelayed(updateRunnable, 500);
    }

    private Runnable updateRunnable = new Runnable() {

        @Override
        public void run() {
            Intent intent = new Intent(FLAG_CONNECTION_CHANGE);
            getApplicationContext().sendBroadcast(intent);
        }
    };

    private void showToast(final String toastMsg) {

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void initUI()
    {

        takeoff = (LinearLayout) findViewById(R.id.takeoff);
        land = (LinearLayout) findViewById(R.id.land);
        goHome = (LinearLayout) findViewById(R.id.go_home);
        newMission = (LinearLayout)findViewById(R.id.new_mission);


        preloadedMission = (Button) findViewById(R.id.waypoint_button);
        start = (Button) findViewById(R.id.start_btn);
        abort = (Button) findViewById(R.id.abort_btn);
        newMission = (LinearLayout) findViewById(R.id.new_mission);
        playPause = (Button) findViewById(R.id.play_pause_button);

        mainActivityLayout = (RelativeLayout) findViewById(R.id.main_relativelayout);
        flightConfigPanel = (RelativeLayout) LayoutInflater.from(MainActivity.this).inflate(R.layout.mission_configuration, null);
         waypointSettings = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_mission_settings, null);
        RelativeLayout.LayoutParams missionParams = new RelativeLayout.LayoutParams(500, ViewGroup.LayoutParams.MATCH_PARENT);

        waypointAltitudeTextView = (TextView)waypointSettings.findViewById(R.id.altitude_editText);
        sampleTimeTextView = (TextView)waypointSettings.findViewById(R.id.sampleTime_editText);
        missionParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        mainActivityLayout.addView(flightConfigPanel, missionParams);
        flightConfigPanel.setVisibility(View.GONE);

        // heightSeekbar = (SeekBar) flightConfigPanel.findViewById(R.id.height_param_seekbar);
        speedSeekbar = (SeekBar) flightConfigPanel.findViewById(R.id.speed_param_seekbar);
        missionOk = (Button) flightConfigPanel.findViewById(R.id.config_ok_btn);
        missionCancel= (Button)flightConfigPanel.findViewById(R.id.config_cancel_btn);

        missionEndRadioGroup = (RadioGroup)flightConfigPanel.findViewById(R.id.mission_end_action_rg);

        batteryStatusImageView = (ImageView)findViewById(R.id.battery_status);
        batteryLevelTextView = (TextView)findViewById(R.id.battery_level_text);



        droneStatusTextView = (TextView)findViewById(R.id.drone_Status);
        satelliteCountTextView = (TextView)findViewById(R.id.satellite_count_text);
        latitudeTextView = (TextView)findViewById(R.id.lat_text);
        longitudeTextView = (TextView) findViewById(R.id.lon_text);
        altitudeTextView = (TextView) findViewById(R.id.altitude_text);
        homeDistanceTextView = (TextView)findViewById(R.id.home_distance_text);
        verticalspeedtextView = (TextView)findViewById(R.id.v_speed_text);
        horizontalspeedtextView = (TextView) findViewById(R.id.h_speed_text);

        speedSeekbarTextView = (TextView)flightConfigPanel.findViewById(R.id.speed_param_text);




        preloadedMission.setOnClickListener(this);
        start.setOnClickListener(this);
        playPause.setOnClickListener(this);
        abort.setOnClickListener(this);
        missionOk.setOnClickListener(this);
        missionCancel.setOnClickListener(this);

        samplingCheckbox = (CheckBox) waypointSettings.findViewById(R.id.task_checkBox);

        samplingCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked)
                {

                    parseCheckedResultInt = 1;
                    showToast("Sampling enabled at this waypoint");

                }

                else
                {
                    parseCheckedResultInt = 0;
                }

            }
        });


        speedSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                speedSeekbarTextView.setText(String.format(Locale.UK, "Speed: %d m/s", progress));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });




    }

    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {


            onProductConnectionChange();

        }
    };

    private void onProductConnectionChange()
    {
        initFlightController();

    }



    private void setUpMap()
    {
        googleMap.setOnMapClickListener(this);
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);


        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener()
        {
            @Override
            public void onMapLongClick(LatLng waypoint)
            {

                final LatLng point = waypoint;
                markWayPoint(point);

                if (point != null)
                {

                    AlertDialog.Builder waypointDialogbuilder = new AlertDialog.Builder(MainActivity.this);
                    waypointDialogbuilder.setTitle("");
                    if (waypointSettings.getParent() != null) {
                        ((ViewGroup) waypointSettings.getParent()).removeView(waypointSettings);

                    }
                    waypointDialogbuilder.setView(waypointSettings);


                    waypointDialogbuilder.setPositiveButton("Okay", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            String altitudeString = waypointAltitudeTextView.getText().toString();
                            String sampleTimeString = sampleTimeTextView.getText().toString();
                            mAltitude = Integer.parseInt(DataUtil.nullToIntegerDefault(altitudeString));
                            mSampleTime = Integer.parseInt(DataUtil.nullToIntegerDefault(sampleTimeString));
                            if(mSampleTime < 0)
                            {
                                mSampleTime = 0;
                                showToast("Sampling time can't be lower than 0 seconds, setting to 0");
                            }
                            if(mAltitude > 100)
                            {
                                mAltitude = 100;
                                showToast("Max Altitude exceeded. Setting to 100m");
                            }

                            if(mAltitude < 0)
                            {
                                mAltitude = 1;
                                showToast("Min Altitude exceeded. Setting to 1m");
                            }
                            missionConfigDataManager.setLatitude(point.latitude);
                            missionConfigDataManager.setLongitude(point.longitude);
                            missionConfigDataManager.setAltitude(mAltitude);
                            missionConfigDataManager.setSpeed(mSpeed);
                            missionConfigDataManager.setSampling(parseCheckedResultInt);
                            missionConfigDataManager.setSampleTime(mSampleTime);

                            data = missionConfigDataManager.getConfigData();

                            missionConfigDataManager.sendMissionData(data, mFlightController);

                            dialogInterface.dismiss();
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    googleMap.clear();
                                }

                            });
                            markHomePoint(homeLatLng);
                            dialogInterface.cancel();
                        }


                    }).create().show();

                }

            }

        });
    }



    private void updateBatteryImageView()
    {
        final int temp [] = new int[1];

        if(batteryChargeInPercent >= 90 && batteryChargeInPercent <= 100)
        {
            temp[0] = R.mipmap.battery10;
        }

        else if (batteryChargeInPercent >= 80 && batteryChargeInPercent < 90)
        {
            temp[0] = R.mipmap.battery9;
        }

        else if (batteryChargeInPercent >= 70 && batteryChargeInPercent < 80)
        {
            temp[0] = R.mipmap.battery8;
        }

        else if (batteryChargeInPercent >= 60 && batteryChargeInPercent < 70)
        {
            temp[0] = R.mipmap.battery7;
        }

        else if (batteryChargeInPercent >= 50 && batteryChargeInPercent < 60)
        {
            temp[0] = R.mipmap.battery6;
        }

        else if (batteryChargeInPercent >= 40 && batteryChargeInPercent < 50)
        {
            temp[0] = R.mipmap.battery5;
        }

        else if (batteryChargeInPercent >= 30 && batteryChargeInPercent < 40)
        {
            temp[0] = R.mipmap.battery4;
        }
        else if (batteryChargeInPercent >= 20 && batteryChargeInPercent < 30)
        {
            temp[0] = R.mipmap.battery3;
        }

        else if (batteryChargeInPercent >= 10 && batteryChargeInPercent < 20)
        {
            temp[0] = R.mipmap.battery2;
        }

        else if (batteryChargeInPercent < 10)
        {
            temp[0] = R.mipmap.battery1;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                batteryStatusImageView.setImageDrawable(MainActivity.this.getDrawable(temp[0]));
                batteryLevelTextView.setText(String.valueOf(batteryChargeInPercent) + "%");
            }
        });

    }

    // Updates Map with and marks current location of device in use
    //Make sure you check permissions if you're using the new Google Fused Location API
    // Older method for google Locations(FusedLocationProviderApi) has been deprecated.
    @Override
    public void onMapReady(GoogleMap gMap)
    {
        if(googleMap == null)
        {
            googleMap = gMap;
            setUpMap();
        }

        getLastLocation();
    }

    private void getLastLocation()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            }
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            homeLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(homeLatLng, 19);
                            googleMap.animateCamera(update);
                            markHomePoint(homeLatLng);
                        } else if (location == null) {
                            Toast.makeText(getApplicationContext(), "Can't get current location", Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }

    // Marks the current homepoint on the map
    private void markHomePoint(LatLng point) {
        //Create MarkerOptions object
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(point);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        googleMap.addMarker(markerOptions);

    }

    @Override
    public void onMapClick(LatLng point)
    {
        flightConfigPanel.setVisibility(View.GONE);

    }


    private void markWayPoint(LatLng point)
    {
        //Create MarkerOptions object
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(point);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        Marker marker = googleMap.addMarker(markerOptions);
        markerArrayList.add(marker);
    }


    private void markCodedWaypoint(LatLng point)
    {
        //Create MarkerOptions object
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(point);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        Marker marker = googleMap.addMarker(markerOptions);
        markerArrayList.add(marker);
    }

    private void updateDroneLocation() {

        LatLng pos = new LatLng(droneLocationLatitude, droneLocationLongitude);
        //Create MarkerOptions object
        final MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(pos);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.drone));

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (droneMarker != null) {
                    droneMarker.remove();
                }

                if (MathUtil.checkGpsCoordinates(droneLocationLatitude, droneLocationLongitude)) {
                    droneMarker = googleMap.addMarker(markerOptions);

                    latitudeTextView.setText(String.format("%.6f", droneLocationLatitude));
                    longitudeTextView.setText(String.format("%.6f", droneLocationLongitude));

                }
            }
        });
    }

    @Override
    public void onClick(View v)
    {

        switch (v.getId()) {

            case R.id.config_ok_btn:
            {

                //  mAltitude = heightSeekbar.getProgress();
                mSpeed = speedSeekbar.getProgress();
                byte missionEndCommand;


                switch (missionEndRadioGroup.getCheckedRadioButtonId()) {
                    //TODO Document Mission END commands properly
                    case R.id.hover_radio_button:
                        missionEndCommand = 1;
                        missionConfigDataManager.setMissionEnd(missionEndCommand);
                        break;

                    case R.id.returnhome_radio_button:
                        missionEndCommand = 2;
                        missionConfigDataManager.setMissionEnd(missionEndCommand);
                        break;

                    case R.id.autoland_radio_button:
                        missionEndCommand = 3;
                        missionConfigDataManager.setMissionEnd(missionEndCommand);
                        break;
                }


                if (markerArrayList.size() > 0)
                {

                    missionConfigDataManager.clearAllPoints();
                    missionConfigDataManager.setSpeed(mSpeed);

                    param_data = missionConfigDataManager.getParams();
                    missionConfigDataManager.sendMissionParam(param_data, mFlightController);


                }

                flightConfigPanel.setVisibility(v.GONE);
                break;

            }

            case R.id.config_cancel_btn: {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        googleMap.clear();
                    }

                });


                flightConfigPanel.setVisibility(v.GONE);
                updateDroneLocation();
                markHomePoint(homeLatLng);
                byte[] CANCELMISSION_CMD = {0x3F};
                missionConfigDataManager.sendCommand(CANCELMISSION_CMD, mFlightController);
                missionConfigDataManager.clearAllPoints();

                break;
            }

            //When Abort button is pressed, aircraft goes home.
            case R.id.abort_btn: {
                byte[] GOHOME_CMD = {0x02};
                missionConfigDataManager.sendCommand(GOHOME_CMD, mFlightController);
                break;
            }
            case R.id.start_btn: {
                byte[] STARTMISSION_CMD = {0x1A};
                missionConfigDataManager.sendCommand(STARTMISSION_CMD, mFlightController);
                break;
            }

            case R.id.waypoint_button:
            {
                i = 0;
                mSpeed = 1.0f;
                mSampleTime = 30;
                parseCheckedResultInt = 1;

                Handler handler = new Handler();
                Runnable runnable = new Runnable()
                {
                    @Override
                    public void run()
                    {
                       if (i < fixedLat.length)
                       {
                           double latitude = fixedLat[i];
                           double longitude = fixedLon[i];
                           double altitude =  fixedAlt[i];
                           LatLng waypoint = new LatLng(latitude, longitude);
                           markWayPoint(waypoint);
                           missionConfigDataManager.setLatitude(latitude);
                           missionConfigDataManager.setLongitude(longitude);
                           missionConfigDataManager.setAltitude((float) altitude);
                           missionConfigDataManager.setSpeed(mSpeed);
                           missionConfigDataManager.setSampleTime(mSampleTime);
                           missionConfigDataManager.setSampling(parseCheckedResultInt);

                           data = missionConfigDataManager.getConfigData();

                           missionConfigDataManager.sendMissionData(data, mFlightController);

                           i++;
                       }

                       else
                       {
                           handler.removeCallbacks(this);
                       }

                       handler.postDelayed(this, 50);
                    }
                };

                handler.postDelayed(runnable, 50);


                break;
            }

            case R.id.play_pause_button:
            {
                if(isPlay)
                {
                    playPause.setBackgroundColor(Color.RED);
                    playPause.setText("Pause");
                    byte [] PLAYMISSION_CMD = {0x5d};
                    missionConfigDataManager.sendCommand(PLAYMISSION_CMD, mFlightController);
                    showToast("Mission continuing");

                }

                else
                {
                    playPause.setBackgroundColor(Color.GREEN);
                    playPause.setText("Play");
                    //playPause.setBackgroundResource(R.mipmap.play);
                    byte [] PAUSEMISSION_CMD = {0x5f};
                    missionConfigDataManager.sendCommand(PAUSEMISSION_CMD, mFlightController);
                    showToast("Mission paused");
                }

                isPlay = !isPlay; // switch boolean


                break;
            }

            default:
                break;

        }


    }












}
