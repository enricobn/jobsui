package org.bef.core.utils;

/**
 * Created by enrico on 11/2/15.
 */
public class Tuple5<F,S,T,FO,FI> {
    public final F first;
    public final S second;
    public final T third;
    public final FO fourth;
    public final FI fifth;

    public Tuple5(F first, S second, T third, FO fourth, FI fifth) {
        this.first = first;
        this.second = second;
        this.third = third;
        this.fourth = fourth;
        this.fifth = fifth;
    }

    public boolean hasNulls() {
        return first == null || second == null || third == null || fourth == null || fifth == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tuple5<?, ?, ?, ?, ?> tuple5 = (Tuple5<?, ?, ?, ?, ?>) o;

        if (first != null ? !first.equals(tuple5.first) : tuple5.first != null) return false;
        if (second != null ? !second.equals(tuple5.second) : tuple5.second != null) return false;
        if (third != null ? !third.equals(tuple5.third) : tuple5.third != null) return false;
        if (fourth != null ? !fourth.equals(tuple5.fourth) : tuple5.fourth != null) return false;
        return !(fifth != null ? !fifth.equals(tuple5.fifth) : tuple5.fifth != null);
    }

    @Override
    public int hashCode() {
        int result = first != null ? first.hashCode() : 0;
        result = 31 * result + (second != null ? second.hashCode() : 0);
        result = 31 * result + (third != null ? third.hashCode() : 0);
        result = 31 * result + (fourth != null ? fourth.hashCode() : 0);
        result = 31 * result + (fifth != null ? fifth.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Tuple5(" +
                first +
                "," + second +
                "," + third +
                "," + fourth +
                "," + fifth +
                ')';
    }
}
