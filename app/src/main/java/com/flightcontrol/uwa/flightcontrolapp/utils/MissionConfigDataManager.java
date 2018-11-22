package com.flightcontrol.uwa.flightcontrolapp.utils;


import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.widget.Toast;

import com.flightcontrol.uwa.flightcontrolapp.MainActivity;


import java.util.ArrayList;
import java.util.List;

import dji.common.error.DJIError;
import dji.common.util.CommonCallbacks;
import dji.sdk.flightcontroller.FlightController;

import static com.flightcontrol.uwa.flightcontrolapp.utils.DataUtil.DoubleToBytes;
import static com.flightcontrol.uwa.flightcontrolapp.utils.DataUtil.FloatToBytes;


//TODO change array size so you can send orientation and mission_end in just bytes

public class MissionConfigDataManager {

    private static final String TAG = MissionConfigDataManager.class.getName();
//    private  final Handler mHandler;
//    private final HandlerThread mThread;


    private List<byte[]>ConfigDataList = new ArrayList<>();


    private static final int ARRAY_SIZE = 28;
    private static final int COMMAND_POSITION = 0;
    private static final int WAYPOINT_LATITUDE = 1;
    private static final int WAYPOINT_LONGITUDE = 9;
    private static final int WAYPOINT_ALTITUDE = 17;
    private static final int WAYPOINT_ORIENTATION = 21;
    private static final int WAYPOINT_SPEED = 22;
    private static final int MISSION_END = 26;
    private static final int NULL_POSITION = 27;


    private  double mLatitude;
    private  double mLongitude;
    private  float mAltitude;
    private  byte mOrientation;
    private  byte mMissionEnd;
    private  float mSpeed;
    private boolean mReady;
    byte[] dataToSend;




    public MissionConfigDataManager()
    {

        mLatitude = 0L;
        mLongitude = 0L;
        mAltitude = 0L;
        mOrientation = 0x0;
        mSpeed = 0L;
        mMissionEnd = 0x0;

    }

    public byte [] getConfigData()
    {
        byte[] configData = new byte [ARRAY_SIZE];
        configData[COMMAND_POSITION] = (byte)0x2f;
        DoubleToBytes(configData, WAYPOINT_LATITUDE, mLatitude);
        DoubleToBytes(configData, WAYPOINT_LONGITUDE, mLongitude);
        FloatToBytes(configData, WAYPOINT_ALTITUDE, mAltitude);
        configData[WAYPOINT_ORIENTATION] = mOrientation;
        FloatToBytes(configData, WAYPOINT_SPEED, mSpeed);
        configData[MISSION_END] = mMissionEnd;
        configData[NULL_POSITION] = (byte)0x0;

        ConfigDataList.add(configData);

        return configData;
    }



    public void sendMissionData(byte [] data,  FlightController djiFC)
    {
        dataToSend = data;
        final Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if(djiFC != null)
                {
                    if(ConfigDataList.size() > 0)
                    {

                        for(int i = 0; i < ConfigDataList.size(); i++)
                        {

                            dataToSend = ConfigDataList.get(i);

                            djiFC.sendDataToOnboardSDKDevice(data, new CommonCallbacks.CompletionCallback() {
                                @Override
                                public void onResult(DJIError djiError) {

                                    if (djiError == null) {
                                        Log.d(TAG, "Sent Data to Onboard Device");
                                    } else {
                                        Log.e(TAG, djiError.getDescription());
                                        Toast.makeText(MainActivity.getInstance().getApplicationContext(), djiError.getDescription(), Toast.LENGTH_SHORT).show();

                                    }

                                }
                            });


                        }


                    }


                }

                else
                {
                    Toast.makeText(MainActivity.getInstance().getApplicationContext(), "NO FC", Toast.LENGTH_LONG).show();
                }


            }
        }, 1000);



    }

    public void sendCommand(byte []data , FlightController djiFC)
    {
        if(djiFC!= null)
        {
            djiFC.sendDataToOnboardSDKDevice(data, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if(djiError == null)
                    {

                    }

                    else
                    {
                        Toast.makeText(MainActivity.getInstance().getApplicationContext(), djiError.getDescription(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    public String toString()
    {
        return "Latitude: " + getLatitude() + "\nLongitude: " + getLongitude()
                +"\nAltitude: " + getAltitude() + "\nSpeed: " + getSpeed()
                +"\nOrientation: " + getOrientation() + "\nMissionEnd: "+ getMissionEnd();

    }


    public String getMissionEnd()
    {

        if(mMissionEnd == 1)
        {
            return "HOVER";
        }
        if(mMissionEnd == 2)
        {
            return  "FIRST WAYPOINT";
        }

        if(mMissionEnd == 3)
        {
            return  "RETURN HOME";
        }

        if(mMissionEnd == 4)
        {
            return "AUTO LAND";
        }

        return "No Mission End Behaviour";
    }
    public double getLatitude() {
        return mLatitude;
    }

    public  double getLongitude()
    {
        return mLongitude;
    }

    public  float getAltitude()
    {
        return mAltitude;
    }

    public float getSpeed()
    {
        return mSpeed;
    }

    public String getOrientation()
    {
        if (mOrientation == 1)
        {
            return "AUTO";
        }

        if(mOrientation == 2)
        {
            return "INITIAL";
        }

        if(mOrientation == 3)
        {
            return "RC";
        }

        if(mOrientation == 4)
        {
            return "WAYPOINT";
        }

        return "NO ORIENTATION";
    }

    public void setLatitude(double latitude)
    {
        this.mLatitude = latitude;
    }

    public void setLongitude(double longitude)
    {
        this.mLongitude = longitude;
    }

    public void setSpeed(float speed)
    {
        this.mSpeed = speed;
    }
    public void setAltitude(float altitude)
    {
        this.mAltitude = altitude;
    }

    public void setCourseLock(byte orientation)
    {
        this.mOrientation = orientation;
    }

    public void setMissionEnd(byte missionEndVal)
    {
        this.mMissionEnd = missionEndVal;
    }

    public void clearAllPoints()
    {
        ConfigDataList.clear();
    }






}