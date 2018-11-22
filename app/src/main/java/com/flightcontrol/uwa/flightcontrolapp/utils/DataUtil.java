package com.flightcontrol.uwa.flightcontrolapp.utils;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/*
  Formatting data to be sent to the drone
  Adapted from https://github.com/MRASL/mrasl_android/blob/master/app/src/main/java/ca/polymtl/mrasl/shared/PayloadUtil.java
 */

public class DataUtil {

    private static final int FLOAT_SIZE = 4 ;
    private static final int DOUBLE_SIZE = 8;
    private static final int BITS_IN_BYTE = 8;

    /* Convert a floating point number to a byte array
        @param buffer the buffer to put the converted float
        @param pos position to put the converted float
        @param input floating point number to convert

    */
    public static void FloatToBytes(byte [] buffer, int pos, float input)
    {
        //  Integer representation for bitwise operations
        int value = Float.floatToIntBits(input);

        // Convert the float to array of bytes
        for(int i = pos; i < pos + FLOAT_SIZE; i++)
        {
            buffer[i] = (byte) (value >> (((FLOAT_SIZE -1) -(i - pos)) * BITS_IN_BYTE));
        }

    }


    /* Convert a double precision floating point number to a byte array

    @param buffer the buffer to put the converted double
    @param pos position to put the converted double
    @param input double to convert

    */
    public static  void DoubleToBytes(byte [] buffer, int pos, double input)
    {
        long value = Double.doubleToLongBits(input);

        // Convert the double to array of bytes
        for(int i = pos; i < pos + DOUBLE_SIZE; i++)
        {
            buffer[i] = (byte) (value >> (((DOUBLE_SIZE -1) -(i - pos)) * BITS_IN_BYTE));
        }


    }


}