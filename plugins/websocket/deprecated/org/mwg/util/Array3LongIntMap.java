package org.mwg.maths;

/* From an original idea https://code.google.com/p/jdbm2/
 *
 * A very specific HashMap to store positive int
 * So, Array3LongIntMap.get(<key>) == -1 means "no value for key <key>"
 */

/**
 * @native ts
 * constructor(initalCapacity: number, loadFactor : number) { }
 * public clear():void { for(var p in this){ if(this.hasOwnProperty(p)){ delete this[p];} } }
 * public get(key:string):V { return this[key]; }
 * public put(key:string, pval : V):V { var previousVal = this[key];this[key] = pval;return previousVal;}
 * public contains(key:string):boolean { return this.hasOwnProperty(key);}
 * public remove(key:string):V { var tmp = this[key]; delete this[key]; return tmp; }
 * public size():number { return Object.keys(this).length; }
 * public each(callback: (p : string, p1 : V) => void): void { for(var p in this){ if(this.hasOwnProperty(p)){ callback(<string>p,this[p]); } } }
 */
public class Array3LongIntMap implements K3LongIntMap {

    protected int elementCount;

    protected Entry[] elementData;

    protected int threshold;

    private final int initalCapacity;

    private final float loadFactor;


    private HeadEntry[] heads;

    /**
     * @ignore ts
     */
    static final class Entry {
        Entry next; //linked list to mock the bucket

        //key
        long universe;
        long time;
        long uuid;

        int value;

        public Entry(long universe, long time, long uuid, int value) {
            this.value = value;
            this.universe = universe;
            this.time = time;
            this.uuid = uuid;
        }
    }

    /**
     *
     * @ignore ts
     */
    private static final class HeadEntry {
        long uuid;
        String key;

    }

    private int hash(long universe, long time, long uuid) {
        return  (int)(universe ^ time ^ uuid) & 0x7fffffff;
    }

    public Array3LongIntMap(int p_initalCapacity, float p_loadFactor) {
        this.initalCapacity = p_initalCapacity;
        this.loadFactor = p_loadFactor;
        elementCount = 0;
        elementData = new Entry[initalCapacity];
        heads = new HeadEntry[initalCapacity / 2];
        computeMaxSize();
    }

    public void clear() {
        if (elementCount > 0) {
            elementCount = 0;
            this.elementData = new Entry[initalCapacity];
        }
    }

    private void computeMaxSize() {
        threshold = (int) (elementData.length * loadFactor);
    }

    @Override
    public boolean contains(long universe, long time, long uuid) {
        if (elementData.length == 0) {
            return false;
        }

        int index = hash(universe,time,uuid) % elementData.length;
        return findNonNullKeyEntry(universe,time,uuid, index) != null;
    }

    @Override
    public int get(long universe, long time, long uuid) {
        if (elementData.length == 0) {
            return -1;
        }

        int index = hash(universe,time,uuid) % elementData.length;
        Entry m = findNonNullKeyEntry(universe,time,uuid, index);
        if (m != null) {
            return m.value;
        }
        return -1;
    }

    final Entry findNonNullKeyEntry(long universe, long time, long uuid, int index) {
        Entry m = elementData[index];
        while (m != null) {
            if(universe == m.universe && time == m.time && uuid == m.uuid) {
                return m;
            }
            m = m.next;
        }
        return null;
    }

    @Override
    public void put(long universe, long time, long uuid, int value) {
        Entry entry = null;
        int index = -1;
        int hash = hash(universe,time,uuid);
        if (elementData.length != 0) {
            index = hash % elementData.length;
            entry = findNonNullKeyEntry(universe,time,uuid, index);
        }
        if (entry == null) {
            if (++elementCount > threshold) {
                rehash();
                index = hash % elementData.length;
            }
            entry = createHashedEntry(universe,time,uuid, index);
        }
        entry.value = value;
    }

    @Override
    public void each(K3LongMapCallBack callback) {
        for (int i = 0; i < elementData.length; i++) {
            if (elementData[i] != null) {
                Entry current = elementData[i];
                callback.on(current.universe,current.time,current.uuid, current.value);
                while (current.next != null) {
                    current = current.next;
                    callback.on(current.universe,current.time,current.uuid, current.value);
                }
            }
        }
    }

    private Entry createHashedEntry(long universe, long time, long uuid, int index) {
        Entry entry = new Entry(universe,time,uuid, -1);
        entry.next = elementData[index];
        elementData[index] = entry;
        return entry;
    }

    private void rehash() {
        int length = (elementData.length == 0 ? 1 : elementData.length * 2);
        Entry[] newData = new Entry[length];
        for (Entry elmtData : elementData) {
            Entry entry = elmtData;
            while (entry != null) {
                int index = hash(entry.universe, entry.time, entry.uuid) % length;
                Entry next = entry.next;
                entry.next = newData[index];
                newData[index] = entry;
                entry = next;
            }
        }
        elementData = newData;
        computeMaxSize();
    }

    @Override
    public void remove(long universe, long time, long uuid) {
        if (elementData.length == 0) {
            return;
        }
        Entry entry;
        Entry last = null;
        int index = hash(universe,time,uuid)  % elementData.length;
        entry = elementData[index];
        while (entry != null && !(universe== entry.universe && time == entry.time && uuid==entry.uuid)) {
            last = entry;
            entry = entry.next;
        }
        if (entry == null) {
            return;
        }
        if (last == null) {
            elementData[index] = entry.next;
        } else {
            last.next = entry.next;
        }
        elementCount--;
    }


    public int size() {
        return elementCount;
    }

}



