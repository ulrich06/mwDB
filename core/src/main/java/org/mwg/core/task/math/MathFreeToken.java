package org.mwg.core.task.math;

class MathFreeToken implements MathToken {

    private String _content;

    MathFreeToken(String content) {
        this._content = content;
    }

    String content() {
        return this._content;
    }

    @Override
    public int type() {
        return 3;
    }

}
