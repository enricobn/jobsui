package org.bef.core.utils;

/**
 * Created by enrico on 11/2/15.
 */
public class Tuple4<F,S,T,FO> {
    public final F first;
    public final S second;
    public final T third;
    public final FO fourth;

    public Tuple4(F first, S second, T third, FO fourth) {
        this.first = first;
        this.second = second;
        this.third = third;
        this.fourth = fourth;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tuple4<?, ?, ?, ?> tuple4 = (Tuple4<?, ?, ?, ?>) o;

        if (first != null ? !first.equals(tuple4.first) : tuple4.first != null) return false;
        if (second != null ? !second.equals(tuple4.second) : tuple4.second != null) return false;
        if (third != null ? !third.equals(tuple4.third) : tuple4.third != null) return false;
        return !(fourth != null ? !fourth.equals(tuple4.fourth) : tuple4.fourth != null);

    }

    public boolean hasNulls() {
        return first == null || second == null || third == null || fourth == null;
    }

    @Override
    public int hashCode() {
        int result = first != null ? first.hashCode() : 0;
        result = 31 * result + (second != null ? second.hashCode() : 0);
        result = 31 * result + (third != null ? third.hashCode() : 0);
        result = 31 * result + (fourth != null ? fourth.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Tuple4(" +
                first +
                "," + second +
                "," + third +
                "," + fourth +
                ')';
    }
}
