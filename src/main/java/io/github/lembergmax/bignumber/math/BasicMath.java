package io.github.lembergmax.bignumber.math;

import io.github.lembergmax.bignumber.BigNumber;

public class BasicMath {

    public static BigNumber add(
            // TODO: besseren namen ausdenken (anstatt zahlen)
            BigNumber addend1,
            BigNumber addend2
    ) {
        /*
        "[1][2][3][4][5][6]"
        "[6][5][4][3][2][1]"
        - Max String length = 2147483647
         */

        addend1 = MathTools.makeNumbersSameLength(addend2, addend1);
        addend2 = MathTools.makeNumbersSameLength(addend1, addend2);


    }

}
