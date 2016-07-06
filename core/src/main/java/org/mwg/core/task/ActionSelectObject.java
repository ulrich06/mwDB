package org.mwg.core.task;

import org.mwg.core.utility.GenericIterable;
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
           context.setUnsafeResult(filter(context.result(),context));
        } else {
            context.setUnsafeResult(null);
        }
    }

   private Object filter(Object current, TaskContext context) {
       GenericIterable iterable = new GenericIterable(current);
       if(!iterable.isArray()) {
           if(_filter.select(current)) {
               return current;
           }
           context.cleanObj(current);
           return null;
       }

       Object[] result;
       if(iterable.estimate() == -1) {
           result = new Object[16];
       } else {
           result = new Object[iterable.estimate()];
       }
       Object loop = iterable.next();
       int index = 0;
       while(loop != null) {

           if(index >= result.length) {
               Object[] tmp = new Object[result.length * 2];
               System.arraycopy(result,0,tmp,0,result.length);
               result = tmp;
           }

           Object filtered = filter(loop,context);
           if(filtered != null) {
               if(filtered instanceof Object[]) {
                   if(((Object[]) filtered).length > 0) {
                       result[index] = filtered;
                       index++;
                   }
               } else {
                   result[index] = filtered;
                   index++;
               }
           }

           loop = iterable.next();
       }

       if(index < result.length) {
           Object[] shrinkedResult = new Object[index];
           System.arraycopy(result,0,shrinkedResult,0,index);
           return shrinkedResult;
       }
       return result;
   }

    @Override
    public String toString() {
        return "selectObject()";
    }
}
