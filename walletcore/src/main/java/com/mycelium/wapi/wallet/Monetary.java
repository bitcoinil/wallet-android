package com.mycelium.wapi.wallet;

public interface Monetary {
    /*
     * Returns the absolute value of exponent of the value of a "smallest unit" in scientific notation. For Bitcoin, a
     * satoshi is worth 1E-8 so this would be 8.
     */

    int smallestUnitExponent();

    /**
     * Returns the number of "smallest units" of this monetary value. For Bitcoin, this would be the number of satoshis.
     */
    long getValue();

    int signum();
}
