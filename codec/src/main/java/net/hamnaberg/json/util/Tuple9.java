
package net.hamnaberg.json.util;

import io.vavr.collection.List;

public final class Tuple9<A1, A2, A3, A4, A5, A6, A7, A8, A9> {
    public final A1 _1;
    public final A2 _2;
    public final A3 _3;
    public final A4 _4;
    public final A5 _5;
    public final A6 _6;
    public final A7 _7;
    public final A8 _8;
    public final A9 _9;

    public Tuple9(A1 _1, A2 _2, A3 _3, A4 _4, A5 _5, A6 _6, A7 _7, A8 _8, A9 _9) {
        this._1 = _1;
        this._2 = _2;
        this._3 = _3;
        this._4 = _4;
        this._5 = _5;
        this._6 = _6;
        this._7 = _7;
        this._8 = _8;
        this._9 = _9;
    }

    @Deprecated
    /**
    * @deprecated Use {@link #apply(F9) instead}
    */
    public <B> B transform(F9<A1, A2, A3, A4, A5, A6, A7, A8, A9, B> f) {
        return f.apply(_1, _2, _3, _4, _5, _6, _7, _8, _9);
    }

    public <B> B apply(F9<A1, A2, A3, A4, A5, A6, A7, A8, A9, B> f) {
        return f.apply(_1, _2, _3, _4, _5, _6, _7, _8, _9);
    }

    private List<?> toList() {
        return List.of(_1, _2, _3, _4, _5, _6, _7, _8, _9);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tuple9 tuple = (Tuple9) o;

        return !toList().equals(tuple.toList());
    }

    @Override
    public int hashCode() {
        return toList().hashCode();
    }
}
    