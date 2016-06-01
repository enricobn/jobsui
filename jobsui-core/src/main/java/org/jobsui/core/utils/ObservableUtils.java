package org.jobsui.core.utils;

import rx.Observable;
import rx.functions.Func2;
import rx.functions.Func3;
import rx.functions.Func4;
import rx.functions.Func5;

/**
 * Created by enrico on 2/20/16.
 */
public abstract class ObservableUtils {

    public static <T1,T2> Observable<Tuple2<T1,T2>> combineLatest(
            Observable<T1> o1, Observable<T2> o2)
    {
        return Observable.combineLatest(o1, o2,
                new Func2<T1, T2, Tuple2<T1, T2>>() {
                    @Override
                    public Tuple2<T1, T2> call(T1 v1, T2 v2) {
                        return new Tuple2<>(v1, v2);
                    }
                });
    }

    public static <T1,T2,T3> Observable<Tuple3<T1,T2,T3>> combineLatest(
            Observable<T1> o1, Observable<T2> o2, Observable<T3> o3)
    {
        return Observable.combineLatest(o1, o2, o3,
                new Func3<T1, T2, T3, Tuple3<T1, T2, T3>>() {
                    @Override
                    public Tuple3<T1, T2, T3> call(T1 v1, T2 v2, T3 v3) {
                        return new Tuple3<>(v1, v2, v3);
                    }
                });
    }

    public static <T1,T2,T3,T4> Observable<Tuple4<T1,T2,T3,T4>> combineLatest(
            Observable<T1> o1, Observable<T2> o2, Observable<T3> o3, Observable<T4> o4)
    {
        return Observable.combineLatest(o1, o2, o3, o4,
                new Func4<T1, T2, T3, T4, Tuple4<T1, T2, T3, T4>>() {
                    @Override
                    public Tuple4<T1, T2, T3, T4> call(T1 v1, T2 v2, T3 v3, T4 v4) {
                        return new Tuple4<>(v1, v2, v3, v4);
                    }
                });
    }

    public static <T1,T2,T3,T4,T5> Observable<Tuple5<T1,T2,T3,T4,T5>> combineLatest(
            Observable<T1> o1, Observable<T2> o2, Observable<T3> o3, Observable<T4> o4, Observable<T5> o5)
    {
        return Observable.combineLatest(o1, o2, o3, o4, o5,
                new Func5<T1, T2, T3, T4, T5, Tuple5<T1, T2, T3, T4, T5>>() {
                    @Override
                    public Tuple5<T1, T2, T3, T4, T5> call(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5) {
                        return new Tuple5<>(v1, v2, v3, v4, v5);
                    }
                });
    }

//    public static <T1,T2> Observable<Tuple2<T1,T2>> combineLatest(
//            UIChoice<T1> c1, UIChoice<T2> c2) {
//        return combineLatest(c1.getObservable(), c2.getObservable());
//    }

//    public static <T1,T2,T3> Observable<Tuple3<T1,T2,T3>> combineLatest(
//            UIChoice<T1> c1, UIChoice<T2> c2, UIChoice<T3> c3) {
//        return combineLatest(c1.getObservable(), c2.getObservable(), c3.getObservable());
//    }
//
//    public static <T1,T2,T3,T4> Observable<Tuple4<T1,T2,T3,T4>> combineLatest(
//            UIChoice<T1> c1, UIChoice<T2> c2, UIChoice<T3> c3, UIChoice<T4> c4) {
//        return combineLatest(c1.getObservable(), c2.getObservable(), c3.getObservable(), c4.getObservable());
//    }
//
//    public static <T1,T2,T3,T4,T5> Observable<Tuple5<T1,T2,T3,T4,T5>> combineLatest(
//            UIChoice<T1> c1, UIChoice<T2> c2, UIChoice<T3> c3, UIChoice<T4> c4, UIChoice<T5> c5) {
//        return combineLatest(c1.getObservable(), c2.getObservable(), c3.getObservable(), c4.getObservable(), c5.getObservable());
//    }


}
