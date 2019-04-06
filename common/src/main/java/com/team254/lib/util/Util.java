package com.team254.lib.util;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Util {

    public static final double kEpsilon = 1.0E-12D;

    private Util() {
    }

    public static double limit(double v, double maxMagnitude) {
        return limit(v, -maxMagnitude, maxMagnitude);
    }

    public static double limit(double v, double min, double max) {
        return Math.min(max, Math.max(min, v));
    }

    public static double interpolate(double a, double b, double x) {
        x = limit(x, 0.0D, 1.0D);
        return a + (b - a) * x;
    }

    public static String joinStrings(String delim, List<?> strings) {
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < strings.size(); ++i) {
            sb.append(strings.get(i).toString());
            if (i < strings.size() - 1) {
                sb.append(delim);
            }
        }

        return sb.toString();
    }

    public static boolean epsilonEquals(double a, double b, double epsilon) {
        return a - epsilon <= b && a + epsilon >= b;
    }

    public static boolean epsilonEquals(double a, double b) {
        return epsilonEquals(a, b, 1.0E-12D);
    }

    public static boolean epsilonEquals(int a, int b, int epsilon) {
        return a - epsilon <= b && a + epsilon >= b;
    }

    public static boolean allCloseTo(List<Double> list, double value, double epsilon) {
        boolean result = true;

        Double value_in;
        for(Iterator var6 = list.iterator(); var6.hasNext(); result &= epsilonEquals(value_in, value, epsilon)) {
            value_in = (Double)var6.next();
        }

        return result;
    }

    /**
     *
     * @param map
     * @param <K>
     * @param <V>
     * @return The entry with the smallest value in the map.
     */
    public static <K, V extends Comparable<V>> Map.Entry<K, V> getEntryWithSmallestValue(Map<K, V> map) {
        Map.Entry<K, V> smallest = null;

        for(Map.Entry<K, V> current : map.entrySet()) {
            if(smallest == null) {
                smallest = current;
                // TODO Check if compareTo() is correct
            } else if(smallest.getValue().compareTo(current.getValue()) > 0) {
                smallest = current;
            }
        }

        return smallest;
    }

}
