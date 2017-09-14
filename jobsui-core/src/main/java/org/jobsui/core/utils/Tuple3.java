package org.jobsui.core.utils;

/**
 * Created by enrico on 11/2/15.
 */
public class Tuple3<F,S,T> {
    public final F first;
    public final S second;
    public final T third;

    public Tuple3(F first, S second, T third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tuple3<?, ?, ?> tuple3 = (Tuple3<?, ?, ?>) o;

        if (first != null ? !first.equals(tuple3.first) : tuple3.first != null) return false;
        if (second != null ? !second.equals(tuple3.second) : tuple3.second != null) return false;
        return !(third != null ? !third.equals(tuple3.third) : tuple3.third != null);
    }

    public boolean hasNulls() {
        return first == null || second == null || third == null;
    }


    @Override
    public int hashCode() {
        int result = first != null ? first.hashCode() : 0;
        result = 31 * result + (second != null ? second.hashCode() : 0);
        result = 31 * result + (third != null ? third.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Tuple3(" +
                first +
                "," + second +
                "," + third +
                ')';
    }
}
