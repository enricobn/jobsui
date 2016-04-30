package org.bef.core.utils;

/**
 * Created by enrico on 11/2/15.
 */
public class Tuple2<F,S> {
    public final F first;
    public final S second;

    public Tuple2(F first, S second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tuple2<?, ?> tuple = (Tuple2<?, ?>) o;

        if (first != null ? !first.equals(tuple.first) : tuple.first != null) return false;
        return !(second != null ? !second.equals(tuple.second) : tuple.second != null);

    }

    public boolean hasNulls() {
        return first == null || second == null;
    }

    @Override
    public int hashCode() {
        int result = first != null ? first.hashCode() : 0;
        result = 31 * result + (second != null ? second.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Tuple2(" +
                first +
                "," + second +
                ')';
    }
}
