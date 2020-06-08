package com.destroflyer.nordicworld.shared;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 *
 * @author carl
 */
public class Util {

    public static <T> T createObjectByClassName(String className) {
        try {
            return (T) Class.forName(className).newInstance();
        } catch (Exception ex) {
            System.err.println("Error while creating object of class '" + className + "':");
            ex.printStackTrace();
        }
        return null;
    }

    public static float[] parseToFloatArray(String[] array) {
        float[] floatArray = new float[array.length];
        for(int i = 0; i < floatArray.length; i++) {
            floatArray[i] = Float.parseFloat(array[i]);
        }
        return floatArray;
    }

    public static float round(float value, int decimals){
        return new BigDecimal(value).setScale(decimals, RoundingMode.HALF_UP).floatValue();
    }
}
