package org.mwg.core.scheduler;

import org.mwg.plugin.Job;

class JobQueue {

    private JobQueueElem first = null;
    private JobQueueElem last = null;

    void add(final Job item) {
        final JobQueueElem elem = new JobQueueElem(item, null);
        if (first == null) {
            first = elem;
            last = elem;
        } else {
            last._next = elem;
            last = elem;
        }
    }

    Job poll() {
        final JobQueueElem value = first;
        first = first._next;
        return value._ptr;
    }

    private class JobQueueElem {
        final Job _ptr;
        JobQueueElem _next;

        private JobQueueElem(final Job ptr, final JobQueueElem next) {
            this._ptr = ptr;
            this._next = next;
        }
    }

}