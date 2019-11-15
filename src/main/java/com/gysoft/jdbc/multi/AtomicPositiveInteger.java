package com.gysoft.jdbc.multi;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * @author 周宁
 */
public class AtomicPositiveInteger extends Number {


    private static final long serialVersionUID = -3038533876489105940L;

    private static final AtomicIntegerFieldUpdater<AtomicPositiveInteger> indexUpdater =
            AtomicIntegerFieldUpdater.newUpdater(AtomicPositiveInteger.class, "index");

    private volatile int index = 0;

    public AtomicPositiveInteger() {
    }


    public final int getAndIncrement() {
        return indexUpdater.getAndIncrement(this) & Integer.MAX_VALUE;
    }

    public final int get() {
        return indexUpdater.get(this) & Integer.MAX_VALUE;
    }

    public final void set(int newValue) {
        if (newValue < 0) {
            throw new IllegalArgumentException("new value " + newValue + " < 0");
        }
        indexUpdater.set(this, newValue);
    }

    @Override
    public byte byteValue() {
        return (byte) get();
    }

    @Override
    public short shortValue() {
        return (short) get();
    }

    @Override
    public int intValue() {
        return get();
    }

    @Override
    public long longValue() {
        return (long) get();
    }

    @Override
    public float floatValue() {
        return (float) get();
    }

    @Override
    public double doubleValue() {
        return (double) get();
    }

    @Override
    public String toString() {
        return Integer.toString(get());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + get();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof AtomicPositiveInteger)) return false;
        AtomicPositiveInteger other = (AtomicPositiveInteger) obj;
        return intValue() == other.intValue();
    }
}
