package com.rockwell.mes.myeig.utility;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class Testing {

    public static void main(String[] args) {
        String input = "  9.6299999999999997E+01";
        BigDecimal potency = new BigDecimal(input.trim());

        NumberFormat formatter = NumberFormat.getInstance(Locale.US);
        formatter.setMaximumFractionDigits(1);
        formatter.setMinimumFractionDigits(1);
        String strPotency = formatter.format(potency);

        System.out.print("valor: " + strPotency);

    }

}
