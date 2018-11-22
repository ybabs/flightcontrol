package com.flightcontrol.uwa.flightcontrolapp.utils;

public  class MathUtil {



    private static final double R = 6371;  // Radius of the earth in km
    private static final double RCM = 6370996.81;  // Radius of the earth in km

    public static double CoordinateToDistanceConverter(double lat1, double lon1, double lat2, double lon2 )
    {
        double distance;

        double dLat = DEG2RAD(lat2- lat1);
        double dLon = DEG2RAD(lon2-lon1);

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(DEG2RAD(lat1)) * Math.cos(DEG2RAD(lat2)) *
                Math.sin(dLon/2) * Math.sin(dLon/2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        distance = R * c * 1000; // value in Metres.

        return  distance;

    }

    static double DEG2RAD(double degree)
    {
        return degree * Math.PI/ 180;

    }

    public static double LL2Distance(double lat1, double lng1, double lat2, double lng2) {
        return RCM * Math.acos(Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) * Math.cos(lng1 * Math.PI / 180 - lng2 * Math.PI / 180) +
                Math.sin(lat1 * Math.PI / 180) * Math.sin(lat2 * Math.PI / 180));
    }

    public static double computeScalarVelocity(double x, double y)
    {
        double velocity;


        // square root(x^2 + y^2)
        double x_squared = Math.pow(x, 2);
        double y_squared = Math.pow(y, 2);

        velocity = Math.sqrt(x_squared + y_squared);

        return velocity;
    }

    public static boolean checkGpsCoordinates(double latitude, double longitude) {
        return (latitude > -90 && latitude < 90 && longitude > -180 && longitude < 180) && (latitude != 0f && longitude != 0f);
    }

}
