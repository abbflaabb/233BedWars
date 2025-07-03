package cn.serendipityr._233bedwars.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MathUtil {
    public static String intToRoman(int num) {
        String[] thousands = {"", "M", "MM", "MMM"};
        String[] hundreds = {"", "C", "CC", "CCC", "CD", "D",
                "DC", "DCC", "DCCC", "CM"};
        String[] tens = {"", "X", "XX", "XXX", "XL", "L",
                "LX", "LXX", "LXXX", "XC"};
        String[] ones = {"", "I", "II", "III", "IV", "V",
                "VI", "VII", "VIII", "IX"};

        return thousands[num / 1000] +
                hundreds[(num % 1000) / 100] +
                tens[(num % 100) / 10] +
                ones[num % 10];
    }

    public static Double roundDouble(double num, int scale) {
        double rounded = new BigDecimal(num).setScale(scale, RoundingMode.HALF_UP).doubleValue();
        if (num > 0 && rounded == 0) {
            return 0.1D;
        } else {
            return rounded;
        }
    }
}
