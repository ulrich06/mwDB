package org.mwg.core.task;

import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskFunctionSelectObject;

class ActionSelectObject implements TaskAction {

    private final TaskFunctionSelectObject _filter;

    ActionSelectObject(TaskFunctionSelectObject filterFunction) {
        _filter = filterFunction;
    }

    @Override
    public void eval(TaskContext context) {
        final Object previousResult = context.result();
        if (previousResult != null) {
            if (previousResult instanceof Object[]) {
                context.setUnsafeResult(filterArray((Object[]) previousResult, context));//fixme put unsafe
            } else if(previousResult instanceof int[]) {
                int[] intResult = (int[]) previousResult;
                Integer[] nextResult = new Integer[intResult.length];
                int cursor = 0;
                for(int i=0;i<intResult.length;i++) {
                    if(_filter.select(intResult[i])) {
                        nextResult[cursor] = intResult[i];
                        cursor++;
                    }
                }
                if(cursor == intResult.length) {
                    context.setUnsafeResult(nextResult);
                } else {
                    Integer[] shrinkedResult = new Integer[cursor];
                    System.arraycopy(nextResult,0,shrinkedResult,0,cursor);
                    context.setUnsafeResult(shrinkedResult);
                }
            } else if(previousResult instanceof double[]) {
                double[] doubleResult = (double[]) previousResult;
                Double[] nextResult = new Double[doubleResult.length];
                int cursor = 0;
                for(int i=0;i<doubleResult.length;i++) {
                    if(_filter.select(doubleResult[i])) {
                        nextResult[cursor] = doubleResult[i];
                        cursor++;
                    }
                }
                if(cursor == doubleResult.length) {
                    context.setUnsafeResult(nextResult);
                } else {
                    Double[] shrinkedResult = new Double[cursor];
                    System.arraycopy(nextResult,0,shrinkedResult,0,cursor);
                    context.setUnsafeResult(shrinkedResult);
                }
            } else if(previousResult instanceof float[]) {
                float[] floatResult = (float[]) previousResult;
                Float[] nextResult = new Float[floatResult.length];
                int cursor = 0;
                for(int i=0;i<floatResult.length;i++) {
                    if(_filter.select(floatResult[i])) {
                        nextResult[cursor] = floatResult[i];
                        cursor++;
                    }
                }
                if(cursor == floatResult.length) {
                    context.setUnsafeResult(nextResult);
                } else {
                    Float[] shrinkedResult = new Float[cursor];
                    System.arraycopy(nextResult,0,shrinkedResult,0,cursor);
                    context.setUnsafeResult(shrinkedResult);
                }
            } /*else if(previousResult instanceof byte[]) {
                Byte[] byteResult = (Byte[]) previousResult;
                Byte[] nextResult = new Byte[byteResult.length];
                int cursor = 0;
                for(int i=0;i<byteResult.length;i++) {
                    if(_filter.select(byteResult[i])) {
                        nextResult[cursor] = byteResult[i];
                        cursor++;
                    }
                }
                if(cursor == byteResult.length) {
                    context.setUnsafeResult(nextResult);
                } else {
                    Byte[] shrinkedResult = new Byte[cursor];
                    System.arraycopy(nextResult,0,shrinkedResult,0,cursor);
                    context.setUnsafeResult(shrinkedResult);
                }
            } else if(previousResult instanceof boolean[]) {
                Boolean[] booleanResult = (Boolean[]) previousResult;
                Boolean[] nextResult = new Boolean[booleanResult.length];
                int cursor = 0;
                for(int i=0;i<booleanResult.length;i++) {
                    if(_filter.select(booleanResult[i])) {
                        nextResult[cursor] = booleanResult[i];
                        cursor++;
                    }
                }
                if(cursor == booleanResult.length) {
                    context.setUnsafeResult(nextResult);
                } else {
                    boolean[] shrinkedResult = new boolean[cursor];
                    System.arraycopy(nextResult,0,shrinkedResult,0,cursor);
                    context.setUnsafeResult(shrinkedResult);
                }
            } else if(previousResult instanceof short[]) {
                short[] shortResult = (short[]) previousResult;
                Short[] nextResult = new Short[shortResult.length];
                int cursor = 0;
                for(int i=0;i<shortResult.length;i++) {
                    if(_filter.select(shortResult[i])) {
                        nextResult[cursor] = shortResult[i];
                        cursor++;
                    }
                }
                if(cursor == shortResult.length) {
                    context.setUnsafeResult(nextResult);
                } else {
                    Short[] shrinkedResult = new Short[cursor];
                    System.arraycopy(nextResult,0,shrinkedResult,0,cursor);
                    context.setUnsafeResult(shrinkedResult);
                }
            } else if(previousResult instanceof char[]) {
                char[] charResult = (char[]) previousResult;
                Character[] nextResult = new Character[charResult.length];
                int cursor = 0;
                for(int i=0;i<charResult.length;i++) {
                    if(_filter.select(charResult[i])) {
                        nextResult[cursor] = charResult[i];
                        cursor++;
                    }
                }
                if(cursor == charResult.length) {
                    context.setUnsafeResult(nextResult);
                } else {
                    Character[] shrinkedResult = new Character[cursor];
                    System.arraycopy(nextResult,0,shrinkedResult,0,cursor);
                    context.setUnsafeResult(shrinkedResult);
                }
            } else if(previousResult instanceof long[]) {
                long[] longResult = (long[]) previousResult;
                Long[] nextResult = new Long[longResult.length];
                int cursor = 0;
                for(int i=0;i<longResult.length;i++) {
                    if(_filter.select(longResult[i])) {
                        nextResult[cursor] = longResult[i];
                        cursor++;
                    }
                }
                if(cursor == longResult.length) {
                    context.setUnsafeResult(nextResult);
                } else {
                    Long[] shrinkedResult = new Long[cursor];
                    System.arraycopy(nextResult,0,shrinkedResult,0,cursor);
                    context.setUnsafeResult(shrinkedResult);
                }
            }*/ else {
                if(_filter.select(previousResult)) {
                    context.setUnsafeResult(previousResult);//fixme put unsafe
                } else {
                    context.cleanObj(previousResult);
                    context.setUnsafeResult(null);
                }
            }
        } else {
            context.setUnsafeResult(null);
        }
    }

    private Object[] filterArray(Object[] current, TaskContext context) {
        Object[] filteredResult = new Object[current.length];
        int cursor = 0;
        for (int i = 0; i < current.length; i++) {
            if (current[i] instanceof Object[]) {
                Object[] filtered = filterArray((Object[]) current[i],context);
                if (filtered != null && filtered.length > 0) {
                    filteredResult[cursor] = filtered;
                    cursor++;
                }
            } else {
                if(_filter.select(current[i])) {
                    filteredResult[cursor] = current[i];
                    cursor++;
                } else {
                    context.cleanObj(current[i]);
                }
            }
        }

        if (cursor == filteredResult.length) {
            return filteredResult;
        } else {
            Object[] shrinkedResult = new Object[cursor];
            System.arraycopy(filteredResult, 0, shrinkedResult, 0, cursor);
            return shrinkedResult;
        }

    }
}
