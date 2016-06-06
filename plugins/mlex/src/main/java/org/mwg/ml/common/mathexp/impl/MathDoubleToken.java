package org.mwg.ml.common.mathexp.impl;

class MathDoubleToken implements MathToken {

    private final double _content;

    MathDoubleToken(double _content) {
        this._content = _content;
    }

    @Override
    public int type() {
        return 2;
    }

    double content() {
        return this._content;
    }

}
