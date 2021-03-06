declare module java {
    module lang {
        class System {
            static gc(): void;
            static arraycopy(src: any[] | Float64Array | Int32Array, srcPos: number, dest: any[] | Float64Array | Int32Array, destPos: number, numElements: number): void;
        }
        class StringBuilder {
            private _buffer;
            length: number;
            append(val: any): StringBuilder;
            insert(position: number, val: any): StringBuilder;
            toString(): string;
        }
        class String {
            static valueOf(data: any, offset?: number, count?: number): string;
            static hashCode(str: string): number;
            static isEmpty(str: string): boolean;
            static join(delimiter: string, ...elements: string[]): string;
        }
        class Thread {
            static sleep(time: number): void;
        }
    }
    namespace util {
        namespace concurrent {
            namespace atomic {
                class AtomicIntegerArray {
                    _internal: Int32Array;
                    constructor(p: Int32Array);
                    set(index: number, newVal: number): void;
                    get(index: number): number;
                    getAndSet(index: number, newVal: number): number;
                    compareAndSet(index: number, expect: number, update: number): boolean;
                }
                class AtomicReference<A> {
                    _internal: A;
                    compareAndSet(expect: A, update: A): boolean;
                    get(): A;
                    set(newRef: A): void;
                    getAndSet(newVal: A): A;
                }
                class AtomicLong {
                    _internal: number;
                    constructor(init: number);
                    compareAndSet(expect: number, update: number): boolean;
                    get(): number;
                    incrementAndGet(): number;
                    decrementAndGet(): number;
                }
                class AtomicBoolean {
                    _internal: boolean;
                    constructor(init: boolean);
                    compareAndSet(expect: boolean, update: boolean): boolean;
                    get(): boolean;
                    set(newVal: boolean): void;
                }
                class AtomicInteger {
                    _internal: number;
                    constructor(init: number);
                    compareAndSet(expect: number, update: number): boolean;
                    get(): number;
                    set(newVal: number): void;
                    getAndSet(newVal: number): number;
                    incrementAndGet(): number;
                    decrementAndGet(): number;
                    getAndIncrement(): number;
                    getAndDecrement(): number;
                }
            }
            namespace locks {
                class ReentrantLock {
                    lock(): void;
                    unlock(): void;
                }
            }
        }
        class Random {
            nextInt(max?: number): number;
            nextDouble(): number;
            nextBoolean(): boolean;
        }
        interface Iterator<E> {
            hasNext(): boolean;
            next(): E;
        }
        class Arrays {
            static fill(data: any, begin: number, nbElem: number, param: number): void;
            static copyOf<T>(original: any[], newLength: number, ignore?: any): T[];
        }
        class Collections {
            static swap(list: List<any>, i: number, j: number): void;
        }
        interface Collection<E> {
            add(val: E): void;
            addAll(vals: Collection<E>): void;
            get(index: number): E;
            remove(o: any): any;
            clear(): void;
            isEmpty(): boolean;
            size(): number;
            contains(o: E): boolean;
            toArray<E>(a: Array<E>): E[];
            iterator(): Iterator<E>;
            containsAll(c: Collection<any>): boolean;
            addAll(c: Collection<any>): boolean;
            removeAll(c: Collection<any>): boolean;
        }
        interface List<E> extends Collection<E> {
            add(elem: E): void;
            add(index: number, elem: E): void;
            poll(): E;
            addAll(c: Collection<E>): boolean;
            addAll(index: number, c: Collection<E>): boolean;
            get(index: number): E;
            set(index: number, element: E): E;
            indexOf(o: E): number;
            lastIndexOf(o: E): number;
            remove(index: number): E;
        }
        interface Set<E> extends Collection<E> {
            forEach(f: (e: any) => void): void;
        }
        class Itr<E> implements Iterator<E> {
            cursor: number;
            lastRet: number;
            protected list: Collection<E>;
            constructor(list: Collection<E>);
            hasNext(): boolean;
            next(): E;
        }
        class HashSet<E> implements Set<E> {
            private content;
            add(val: E): void;
            clear(): void;
            contains(val: E): boolean;
            containsAll(elems: Collection<E>): boolean;
            addAll(vals: Collection<E>): boolean;
            remove(val: E): boolean;
            removeAll(): boolean;
            size(): number;
            isEmpty(): boolean;
            toArray<E>(a: Array<E>): E[];
            iterator(): Iterator<E>;
            forEach(f: (e: any) => void): void;
            get(index: number): E;
        }
        class AbstractList<E> implements List<E> {
            private content;
            addAll(index: any, vals?: any): boolean;
            clear(): void;
            poll(): E;
            remove(indexOrElem: any): any;
            removeAll(): boolean;
            toArray(a: Array<E>): E[];
            size(): number;
            add(index: any, elem?: E): void;
            get(index: number): E;
            contains(val: E): boolean;
            containsAll(elems: Collection<E>): boolean;
            isEmpty(): boolean;
            set(index: number, element: E): E;
            indexOf(element: E): number;
            lastIndexOf(element: E): number;
            iterator(): Iterator<E>;
        }
        class LinkedList<E> extends AbstractList<E> {
        }
        class ArrayList<E> extends AbstractList<E> {
        }
        class Stack<E> {
            content: any[];
            pop(): E;
            push(t: E): void;
            isEmpty(): boolean;
            peek(): E;
        }
        interface Map<K, V> {
            get(key: K): V;
            put(key: K, value: V): V;
            containsKey(key: K): boolean;
            remove(key: K): V;
            keySet(): Set<K>;
            isEmpty(): boolean;
            values(): Set<V>;
            clear(): void;
            size(): number;
        }
        class HashMap<K, V> implements Map<K, V> {
            get(key: K): V;
            put(key: K, value: V): V;
            containsKey(key: K): boolean;
            remove(key: K): V;
            keySet(): Set<K>;
            isEmpty(): boolean;
            values(): Set<V>;
            clear(): void;
            size(): number;
        }
        class ConcurrentHashMap<K, V> extends HashMap<K, V> {
        }
    }
}
declare function arrayInstanceOf(arr: any, arg: Function): boolean;
declare class Long {
    private high;
    private low;
    private unsigned;
    private static INT_CACHE;
    private static UINT_CACHE;
    private static pow_dbl;
    private static TWO_PWR_16_DBL;
    private static TWO_PWR_24_DBL;
    private static TWO_PWR_32_DBL;
    private static TWO_PWR_64_DBL;
    private static TWO_PWR_63_DBL;
    private static TWO_PWR_24;
    static ZERO: Long;
    static UZERO: Long;
    static ONE: Long;
    static UONE: Long;
    static NEG_ONE: Long;
    static MAX_VALUE: Long;
    static MAX_UNSIGNED_VALUE: Long;
    static MIN_VALUE: Long;
    constructor(low?: number, high?: number, unsigned?: boolean);
    static isLong(obj: any): boolean;
    static fromInt(value: number, unsigned?: boolean): Long;
    static fromNumber(value: number, unsigned?: boolean): Long;
    static fromBits(lowBits?: number, highBits?: number, unsigned?: boolean): Long;
    static fromString(str: string, radix?: number, unsigned?: boolean): Long;
    static fromValue(val: any): Long;
    toInt(): number;
    toNumber(): number;
    toString(radix: number): string;
    getHighBits(): number;
    getHighBitsUnsigned(): number;
    getLowBits(): number;
    getLowBitsUnsigned(): number;
    getNumBitsAbs(): number;
    isZero(): boolean;
    isNegative(): boolean;
    isPositive(): boolean;
    isOdd(): boolean;
    isEven(): boolean;
    equals(other: any): boolean;
    eq: (other: any) => boolean;
    notEquals(other: any): boolean;
    neq: (other: any) => boolean;
    lessThan(other: any): boolean;
    lt: (other: any) => boolean;
    lessThanOrEqual(other: any): boolean;
    lte: (other: any) => boolean;
    greaterThan(other: any): boolean;
    gt: (other: any) => boolean;
    greaterThanOrEqual(other: any): boolean;
    gte: (other: any) => boolean;
    compare(other: any): number;
    comp: (other: any) => number;
    negate(): Long;
    neg: () => Long;
    add(addend: any): Long;
    subtract(subtrahend: any): Long;
    sub: (subtrahend: any) => Long;
    multiply(multiplier: any): Long;
    mul: (multiplier: any) => Long;
    divide(divisor: any): Long;
    div: (divisor: any) => Long;
    modulo(divisor: any): Long;
    mod: (divisor: any) => Long;
    not(): Long;
    and(other: any): Long;
    or(other: any): Long;
    xor(other: any): Long;
    shiftLeft(numBits: any): Long;
    shl: (numBits: any) => Long;
    shiftRight(numBits: any): Long;
    shr: (numBits: any) => Long;
    shiftRightUnsigned(numBits: any): Long;
    shru: (numBits: any) => Long;
    toSigned(): Long;
    toUnsigned(): Long;
}
declare module org {
    module mwg {
        interface Callback<A> {
            (result: A): void;
        }
        class Constants {
            static LONG_SIZE: number;
            static PREFIX_SIZE: number;
            static BEGINNING_OF_TIME: number;
            static END_OF_TIME: number;
            static NULL_LONG: number;
            static KEY_PREFIX_MASK: number;
            static CACHE_MISS_ERROR: string;
            static QUERY_SEP: string;
            static QUERY_KV_SEP: string;
            static TASK_SEP: string;
            static TASK_PARAM_OPEN: string;
            static TASK_PARAM_CLOSE: string;
            static BUFFER_SEP: number;
            static KEY_SEP: number;
            static isDefined(param: any): boolean;
            static equals(src: string, other: string): boolean;
            static longArrayEquals(src: Float64Array, other: Float64Array): boolean;
        }
        interface DeferCounter {
            count(): void;
            then(callback: org.mwg.Callback<any>): void;
        }
        interface Graph {
            newNode(world: number, time: number): org.mwg.Node;
            newTypedNode(world: number, time: number, nodeType: string): org.mwg.Node;
            cloneNode(origin: org.mwg.Node): org.mwg.Node;
            lookup<A extends org.mwg.Node>(world: number, time: number, id: number, callback: org.mwg.Callback<A>): void;
            diverge(world: number): number;
            save(callback: org.mwg.Callback<boolean>): void;
            connect(callback: org.mwg.Callback<boolean>): void;
            disconnect(callback: org.mwg.Callback<boolean>): void;
            index(indexName: string, nodeToIndex: org.mwg.Node, flatKeyAttributes: string, callback: org.mwg.Callback<boolean>): void;
            unindex(indexName: string, nodeToIndex: org.mwg.Node, flatKeyAttributes: string, callback: org.mwg.Callback<boolean>): void;
            find(world: number, time: number, indexName: string, query: string, callback: org.mwg.Callback<org.mwg.Node[]>): void;
            findQuery(query: org.mwg.Query, callback: org.mwg.Callback<org.mwg.Node[]>): void;
            all(world: number, time: number, indexName: string, callback: org.mwg.Callback<org.mwg.Node[]>): void;
            namedIndex(world: number, time: number, indexName: string, callback: org.mwg.Callback<org.mwg.Node>): void;
            counter(expectedEventsCount: number): org.mwg.DeferCounter;
            resolver(): org.mwg.plugin.Resolver;
            scheduler(): org.mwg.plugin.Scheduler;
            newBuffer(): org.mwg.struct.Buffer;
            newQuery(): org.mwg.Query;
            newTask(): org.mwg.task.Task;
            space(): org.mwg.plugin.ChunkSpace;
            storage(): org.mwg.plugin.Storage;
            actions(): org.mwg.task.TaskActionRegistry;
        }
        class GraphBuilder {
            private _storage;
            private _scheduler;
            private _factories;
            private _offHeap;
            private _gc;
            private _memorySize;
            private _saveBatchSize;
            private _readOnly;
            private static internalBuilder;
            static builder(): org.mwg.GraphBuilder;
            constructor();
            withStorage(p_storage: org.mwg.plugin.Storage): org.mwg.GraphBuilder;
            readOnly(): org.mwg.GraphBuilder;
            withScheduler(p_scheduler: org.mwg.plugin.Scheduler): org.mwg.GraphBuilder;
            withFactory(p_factory: org.mwg.plugin.NodeFactory): org.mwg.GraphBuilder;
            withGC(): org.mwg.GraphBuilder;
            withOffHeapMemory(): org.mwg.GraphBuilder;
            withMemorySize(size: number): org.mwg.GraphBuilder;
            withAutoSave(batchSize: number): org.mwg.GraphBuilder;
            build(): org.mwg.Graph;
        }
        module GraphBuilder {
            interface InternalBuilder {
                newGraph(storage: org.mwg.plugin.Storage, readOnly: boolean, scheduler: org.mwg.plugin.Scheduler, factories: org.mwg.plugin.NodeFactory[], usingGC: boolean, usingOffHeapMemory: boolean, memorySize: number, autoSaveSize: number): org.mwg.Graph;
            }
        }
        interface Node {
            world(): number;
            time(): number;
            id(): number;
            get(propertyName: string): any;
            type(propertyName: string): number;
            set(propertyName: string, propertyValue: any): void;
            setProperty(propertyName: string, propertyType: number, propertyValue: any): void;
            map(propertyName: string, propertyType: number): org.mwg.struct.Map;
            removeProperty(propertyName: string): void;
            rel(relationName: string, callback: org.mwg.Callback<org.mwg.Node[]>): void;
            add(relationName: string, relatedNode: org.mwg.Node): void;
            remove(relationName: string, relatedNode: org.mwg.Node): void;
            index(indexName: string, nodeToIndex: org.mwg.Node, flatKeyAttributes: string, callback: org.mwg.Callback<boolean>): void;
            unindex(indexName: string, nodeToIndex: org.mwg.Node, flatKeyAttributes: string, callback: org.mwg.Callback<boolean>): void;
            find(indexName: string, query: string, callback: org.mwg.Callback<org.mwg.Node[]>): void;
            findQuery(query: org.mwg.Query, callback: org.mwg.Callback<org.mwg.Node[]>): void;
            all(indexName: string, callback: org.mwg.Callback<org.mwg.Node[]>): void;
            allAt(world: number, time: number, indexName: string, callback: org.mwg.Callback<org.mwg.Node[]>): void;
            timeDephasing(): number;
            forcePhase(): void;
            timepoints(beginningOfSearch: number, endOfSearch: number, callback: org.mwg.Callback<Float64Array>): void;
            free(): void;
            graph(): org.mwg.Graph;
            jump<A extends org.mwg.Node>(targetTime: number, callback: org.mwg.Callback<A>): void;
        }
        module plugin {
            abstract class AbstractNode implements org.mwg.Node {
                private _world;
                private _time;
                private _id;
                private _graph;
                _resolver: org.mwg.plugin.Resolver;
                _previousResolveds: java.util.concurrent.atomic.AtomicReference<Float64Array>;
                constructor(p_world: number, p_time: number, p_id: number, p_graph: org.mwg.Graph, currentResolution: Float64Array);
                unphasedState(): org.mwg.plugin.NodeState;
                phasedState(): org.mwg.plugin.NodeState;
                newState(time: number): org.mwg.plugin.NodeState;
                graph(): org.mwg.Graph;
                world(): number;
                time(): number;
                id(): number;
                get(propertyName: string): any;
                set(propertyName: string, propertyValue: any): void;
                setProperty(propertyName: string, propertyType: number, propertyValue: any): void;
                map(propertyName: string, propertyType: number): org.mwg.struct.Map;
                type(propertyName: string): number;
                removeProperty(attributeName: string): void;
                rel(relationName: string, callback: org.mwg.Callback<org.mwg.Node[]>): void;
                add(relationName: string, relatedNode: org.mwg.Node): void;
                remove(relationName: string, relatedNode: org.mwg.Node): void;
                free(): void;
                timeDephasing(): number;
                forcePhase(): void;
                timepoints(beginningOfSearch: number, endOfSearch: number, callback: org.mwg.Callback<Float64Array>): void;
                jump<A extends org.mwg.Node>(targetTime: number, callback: org.mwg.Callback<A>): void;
                findQuery(query: org.mwg.Query, callback: org.mwg.Callback<org.mwg.Node[]>): void;
                find(indexName: string, query: string, callback: org.mwg.Callback<org.mwg.Node[]>): void;
                allAt(world: number, time: number, indexName: string, callback: org.mwg.Callback<org.mwg.Node[]>): void;
                all(indexName: string, callback: org.mwg.Callback<org.mwg.Node[]>): void;
                index(indexName: string, nodeToIndex: org.mwg.Node, flatKeyAttributes: string, callback: org.mwg.Callback<boolean>): void;
                unindex(indexName: string, nodeToIndex: org.mwg.Node, flatKeyAttributes: string, callback: org.mwg.Callback<boolean>): void;
                private isNaN(toTest);
                toString(): string;
                setPropertyWithType(propertyName: string, propertyType: number, propertyValue: any, propertyTargetType: number): void;
            }
            class Base64 {
                private static dictionary;
                private static powTwo;
                private static longIndexes;
                static encodeLongToBuffer(l: number, buffer: org.mwg.struct.Buffer): void;
                static encodeIntToBuffer(l: number, buffer: org.mwg.struct.Buffer): void;
                static decodeToLong(s: org.mwg.struct.Buffer): number;
                static decodeToLongWithBounds(s: org.mwg.struct.Buffer, offsetBegin: number, offsetEnd: number): number;
                static decodeToInt(s: org.mwg.struct.Buffer): number;
                static decodeToIntWithBounds(s: org.mwg.struct.Buffer, offsetBegin: number, offsetEnd: number): number;
                static encodeDoubleToBuffer(d: number, buffer: org.mwg.struct.Buffer): void;
                static decodeToDouble(s: org.mwg.struct.Buffer): number;
                static decodeToDoubleWithBounds(s: org.mwg.struct.Buffer, offsetBegin: number, offsetEnd: number): number;
                static encodeBoolArrayToBuffer(boolArr: Array<boolean>, buffer: org.mwg.struct.Buffer): void;
                static decodeBoolArray(s: org.mwg.struct.Buffer, arraySize: number): any[];
                static decodeToBoolArrayWithBounds(s: org.mwg.struct.Buffer, offsetBegin: number, offsetEnd: number, arraySize: number): any[];
                static encodeStringToBuffer(s: string, buffer: org.mwg.struct.Buffer): void;
                static decodeString(s: org.mwg.struct.Buffer): string;
                static decodeToStringWithBounds(s: org.mwg.struct.Buffer, offsetBegin: number, offsetEnd: number): string;
            }
            interface Chunk {
                world(): number;
                time(): number;
                id(): number;
                chunkType(): number;
                marks(): number;
                flags(): number;
                save(buffer: org.mwg.struct.Buffer): void;
                merge(buffer: org.mwg.struct.Buffer): void;
            }
            interface ChunkIterator {
                hasNext(): boolean;
                next(): org.mwg.plugin.Chunk;
                size(): number;
                free(): void;
            }
            interface ChunkSpace {
                create(type: number, world: number, time: number, id: number, initialPayload: org.mwg.struct.Buffer, origin: org.mwg.plugin.Chunk): org.mwg.plugin.Chunk;
                getAndMark(type: number, world: number, time: number, id: number): org.mwg.plugin.Chunk;
                putAndMark(elem: org.mwg.plugin.Chunk): org.mwg.plugin.Chunk;
                getOrLoadAndMark(type: number, world: number, time: number, id: number, callback: org.mwg.Callback<org.mwg.plugin.Chunk>): void;
                unmark(type: number, world: number, time: number, id: number): void;
                unmarkChunk(chunk: org.mwg.plugin.Chunk): void;
                freeChunk(chunk: org.mwg.plugin.Chunk): void;
                declareDirty(elem: org.mwg.plugin.Chunk): void;
                declareClean(elem: org.mwg.plugin.Chunk): void;
                setGraph(graph: org.mwg.Graph): void;
                graph(): org.mwg.Graph;
                clear(): void;
                free(): void;
                size(): number;
                available(): number;
                detachDirties(): org.mwg.plugin.ChunkIterator;
            }
            class ChunkType {
                static STATE_CHUNK: number;
                static TIME_TREE_CHUNK: number;
                static WORLD_ORDER_CHUNK: number;
                static GEN_CHUNK: number;
            }
            interface Job {
                (): void;
            }
            interface NodeFactory {
                name(): string;
                create(world: number, time: number, id: number, graph: org.mwg.Graph, initialResolution: Float64Array): org.mwg.Node;
            }
            interface NodeState {
                world(): number;
                time(): number;
                set(index: number, elemType: number, elem: any): void;
                setFromKey(key: string, elemType: number, elem: any): void;
                get(index: number): any;
                getFromKey(key: string): any;
                getFromKeyWithDefault<A>(key: string, defaultValue: A): A;
                getOrCreate(index: number, elemType: number): any;
                getOrCreateFromKey(key: string, elemType: number): any;
                getType(index: number): number;
                getTypeFromKey(key: string): number;
                each(callBack: org.mwg.plugin.NodeStateCallback): void;
            }
            interface NodeStateCallback {
                (attributeKey: number, elemType: number, elem: any): void;
            }
            interface Resolver {
                init(graph: org.mwg.Graph): void;
                initNode(node: org.mwg.Node, typeCode: number): void;
                markNodeAndGetType(node: org.mwg.Node): number;
                initWorld(parentWorld: number, childWorld: number): void;
                freeNode(node: org.mwg.Node): void;
                lookupJob<A extends org.mwg.Node>(world: number, time: number, id: number, callback: org.mwg.Callback<A>): org.mwg.plugin.Job;
                lookup<A extends org.mwg.Node>(world: number, time: number, id: number, callback: org.mwg.Callback<A>): void;
                resolveState(node: org.mwg.Node, allowDephasing: boolean): org.mwg.plugin.NodeState;
                newState(node: org.mwg.Node, world: number, time: number): org.mwg.plugin.NodeState;
                resolveTimepoints(node: org.mwg.Node, beginningOfSearch: number, endOfSearch: number, callback: org.mwg.Callback<Float64Array>): void;
                stringToHash(name: string, insertIfNotExists: boolean): number;
                hashToString(key: number): string;
            }
            interface Scheduler {
                dispatch(job: org.mwg.plugin.Job): void;
                start(): void;
                stop(): void;
            }
            interface Storage {
                get(keys: org.mwg.struct.Buffer, callback: org.mwg.Callback<org.mwg.struct.Buffer>): void;
                put(stream: org.mwg.struct.Buffer, callback: org.mwg.Callback<boolean>): void;
                remove(keys: org.mwg.struct.Buffer, callback: org.mwg.Callback<boolean>): void;
                connect(graph: org.mwg.Graph, callback: org.mwg.Callback<boolean>): void;
                lock(callback: org.mwg.Callback<org.mwg.struct.Buffer>): void;
                unlock(previousLock: org.mwg.struct.Buffer, callback: org.mwg.Callback<boolean>): void;
                disconnect(callback: org.mwg.Callback<boolean>): void;
            }
        }
        interface Query {
            parseString(flatQuery: string): org.mwg.Query;
            add(attributeName: string, value: any): org.mwg.Query;
            setWorld(initialWorld: number): org.mwg.Query;
            world(): number;
            setTime(initialTime: number): org.mwg.Query;
            time(): number;
            setIndexName(indexName: string): org.mwg.Query;
            indexName(): string;
            hash(): number;
            attributes(): Float64Array;
            values(): any[];
        }
        module struct {
            interface Buffer {
                write(b: number): void;
                writeAll(bytes: Int8Array): void;
                read(position: number): number;
                data(): Int8Array;
                size(): number;
                free(): void;
                iterator(): org.mwg.struct.BufferIterator;
                removeLast(): void;
            }
            interface BufferIterator {
                hasNext(): boolean;
                next(): org.mwg.struct.Buffer;
            }
            interface LongLongArrayMap extends org.mwg.struct.Map {
                get(key: number): Float64Array;
                put(key: number, value: number): void;
                remove(key: number, value: number): void;
                each(callback: org.mwg.struct.LongLongArrayMapCallBack): void;
            }
            interface LongLongArrayMapCallBack {
                (key: number, value: number): void;
            }
            interface LongLongMap extends org.mwg.struct.Map {
                get(key: number): number;
                put(key: number, value: number): void;
                remove(key: number): void;
                each(callback: org.mwg.struct.LongLongMapCallBack): void;
            }
            interface LongLongMapCallBack {
                (key: number, value: number): void;
            }
            interface Map {
                size(): number;
            }
            interface StringLongMap extends org.mwg.struct.Map {
                getValue(key: string): number;
                getByHash(index: number): string;
                containsHash(index: number): boolean;
                put(key: string, value: number): void;
                remove(key: string): void;
                each(callback: org.mwg.struct.StringLongMapCallBack): void;
            }
            interface StringLongMapCallBack {
                (key: string, value: number): void;
            }
        }
        module task {
            interface Action {
                (context: org.mwg.task.TaskContext): void;
            }
            interface Task {
                world(world: number): org.mwg.task.Task;
                time(time: number): org.mwg.task.Task;
                asVar(variableName: string): org.mwg.task.Task;
                fromVar(variableName: string): org.mwg.task.Task;
                from(inputValue: any): org.mwg.task.Task;
                fromIndex(indexName: string, query: string): org.mwg.task.Task;
                fromIndexAll(indexName: string): org.mwg.task.Task;
                selectWith(name: string, pattern: string): org.mwg.task.Task;
                selectWithout(name: string, pattern: string): org.mwg.task.Task;
                select(filterFunction: org.mwg.task.TaskFunctionSelect): org.mwg.task.Task;
                selectWhere(subTask: org.mwg.task.Task): org.mwg.task.Task;
                traverse(relationName: string): org.mwg.task.Task;
                traverseOrKeep(relationName: string): org.mwg.task.Task;
                traverseIndex(indexName: string, query: string): org.mwg.task.Task;
                traverseIndexAll(indexName: string): org.mwg.task.Task;
                map(mapFunction: org.mwg.task.TaskFunctionMap): org.mwg.task.Task;
                flatMap(flatMapFunction: org.mwg.task.TaskFunctionFlatMap): org.mwg.task.Task;
                group(groupFunction: org.mwg.task.TaskFunctionGroup): org.mwg.task.Task;
                groupWhere(groupSubTask: org.mwg.task.Task): org.mwg.task.Task;
                foreach(subTask: org.mwg.task.Task): org.mwg.task.Task;
                foreachPar(subTask: org.mwg.task.Task): org.mwg.task.Task;
                foreachThen<T>(action: org.mwg.Callback<T>): org.mwg.task.Task;
                wait(subTask: org.mwg.task.Task): org.mwg.task.Task;
                ifThen(cond: org.mwg.task.TaskFunctionConditional, then: org.mwg.task.Task): org.mwg.task.Task;
                whileDo(cond: org.mwg.task.TaskFunctionConditional, then: org.mwg.task.Task): org.mwg.task.Task;
                then(action: org.mwg.task.Action): org.mwg.task.Task;
                thenAsync(action: org.mwg.task.Action): org.mwg.task.Task;
                save(): org.mwg.task.Task;
                execute(): void;
                executeThen(action: org.mwg.task.Action): void;
                executeThenAsync(parentContext: org.mwg.task.TaskContext, initialResult: any, finalAction: org.mwg.task.Action): void;
                parse(flat: string): org.mwg.task.Task;
                action(name: string, params: string): org.mwg.task.Task;
            }
            interface TaskAction {
                eval(context: org.mwg.task.TaskContext): void;
            }
            interface TaskActionFactory {
                (params: string[]): org.mwg.task.TaskAction;
            }
            interface TaskActionRegistry {
                add(name: string, action: org.mwg.task.TaskActionFactory): void;
                remove(name: string): void;
                get(name: string): org.mwg.task.TaskActionFactory;
            }
            interface TaskContext {
                graph(): org.mwg.Graph;
                getWorld(): number;
                setWorld(world: number): void;
                getTime(): number;
                setTime(time: number): void;
                getVariable(name: string): any;
                getVariablesKeys(): string[];
                setVariable(name: string, value: any): void;
                addToVariable(name: string, value: any): void;
                getPreviousResult(): any;
                setResult(actionResult: any): void;
                next(): void;
                clean(): void;
            }
            interface TaskFunctionConditional {
                (context: org.mwg.task.TaskContext): boolean;
            }
            interface TaskFunctionFlatMap {
                (nodes: org.mwg.Node[]): any;
            }
            interface TaskFunctionGroup {
                (nodes: org.mwg.Node): number;
            }
            interface TaskFunctionMap {
                (node: org.mwg.Node): any;
            }
            interface TaskFunctionSelect {
                (node: org.mwg.Node): boolean;
            }
        }
        class Type {
            static BOOL: number;
            static STRING: number;
            static LONG: number;
            static INT: number;
            static DOUBLE: number;
            static DOUBLE_ARRAY: number;
            static LONG_ARRAY: number;
            static INT_ARRAY: number;
            static LONG_LONG_MAP: number;
            static LONG_LONG_ARRAY_MAP: number;
            static STRING_LONG_MAP: number;
            static typeName(p_type: number): string;
        }
    }
}
declare module org {
    module mwg {
        module core {
            class BlackHoleStorage implements org.mwg.plugin.Storage {
                private _graph;
                private prefix;
                get(keys: org.mwg.struct.Buffer, callback: org.mwg.Callback<org.mwg.struct.Buffer>): void;
                put(stream: org.mwg.struct.Buffer, callback: org.mwg.Callback<boolean>): void;
                remove(keys: org.mwg.struct.Buffer, callback: org.mwg.Callback<boolean>): void;
                connect(graph: org.mwg.Graph, callback: org.mwg.Callback<boolean>): void;
                lock(callback: org.mwg.Callback<org.mwg.struct.Buffer>): void;
                unlock(previousLock: org.mwg.struct.Buffer, callback: org.mwg.Callback<boolean>): void;
                disconnect(callback: org.mwg.Callback<boolean>): void;
            }
            class Builder implements org.mwg.GraphBuilder.InternalBuilder {
                newGraph(p_storage: org.mwg.plugin.Storage, p_readOnly: boolean, p_scheduler: org.mwg.plugin.Scheduler, p_factories: org.mwg.plugin.NodeFactory[], p_usingGC: boolean, p_usingOffHeapMemory: boolean, p_memorySize: number, p_autoSaveSize: number): org.mwg.Graph;
                private createSpace(usingOffHeapMemory, memorySize, autoSaveSize);
            }
            module chunk {
                interface ChunkListener {
                    declareDirty(chunk: org.mwg.plugin.Chunk): void;
                    graph(): org.mwg.Graph;
                }
                interface GenChunk extends org.mwg.plugin.Chunk {
                    newKey(): number;
                }
                module heap {
                    class ArrayLongLongArrayMap implements org.mwg.struct.LongLongArrayMap {
                        private state;
                        private aligned;
                        private _listener;
                        constructor(p_listener: org.mwg.core.chunk.ChunkListener, initialCapacity: number, p_origin: org.mwg.core.chunk.heap.ArrayLongLongArrayMap);
                        get(key: number): Float64Array;
                        each(callback: org.mwg.struct.LongLongArrayMapCallBack): void;
                        size(): number;
                        put(key: number, value: number): void;
                        private internal_modify_map(key, value, toInsert);
                        remove(key: number, value: number): void;
                    }
                    module ArrayLongLongArrayMap {
                        class InternalState {
                            _stateSize: number;
                            _elementK: Float64Array;
                            _elementV: Float64Array;
                            _elementNext: Int32Array;
                            _elementHash: Int32Array;
                            _threshold: number;
                            _elementCount: number;
                            _nextAvailableSlot: number;
                            constructor(p_stateSize: number, p_elementK: Float64Array, p_elementV: Float64Array, p_elementNext: Int32Array, p_elementHash: Int32Array, p_elementCount: number, p_nextAvailableSlot: number);
                            clone(): org.mwg.core.chunk.heap.ArrayLongLongArrayMap.InternalState;
                        }
                    }
                    class ArrayLongLongMap implements org.mwg.struct.LongLongMap {
                        private state;
                        private aligned;
                        private _listener;
                        constructor(p_listener: org.mwg.core.chunk.ChunkListener, initialCapacity: number, p_origin: org.mwg.core.chunk.heap.ArrayLongLongMap);
                        get(key: number): number;
                        each(callback: org.mwg.struct.LongLongMapCallBack): void;
                        size(): number;
                        put(key: number, value: number): void;
                        private internal_modify_map(key, value);
                        remove(key: number): void;
                    }
                    module ArrayLongLongMap {
                        class InternalState {
                            _stateSize: number;
                            _elementK: Float64Array;
                            _elementV: Float64Array;
                            _elementNext: Int32Array;
                            _elementHash: Int32Array;
                            _threshold: number;
                            _elementCount: number;
                            constructor(p_stateSize: number, p_elementK: Float64Array, p_elementV: Float64Array, p_elementNext: Int32Array, p_elementHash: Int32Array, p_elementCount: number);
                            clone(): org.mwg.core.chunk.heap.ArrayLongLongMap.InternalState;
                        }
                    }
                    class ArrayStringLongMap implements org.mwg.struct.StringLongMap {
                        private state;
                        private aligned;
                        private _listener;
                        constructor(p_listener: org.mwg.core.chunk.ChunkListener, initialCapacity: number, p_origin: org.mwg.core.chunk.heap.ArrayStringLongMap);
                        getValue(key: string): number;
                        getByHash(keyHash: number): string;
                        containsHash(keyHash: number): boolean;
                        each(callback: org.mwg.struct.StringLongMapCallBack): void;
                        size(): number;
                        remove(key: string): void;
                        put(key: string, value: number): void;
                        private internal_modify_map(key, value);
                    }
                    module ArrayStringLongMap {
                        class InternalState {
                            _stateSize: number;
                            _elementK: string[];
                            _elementKH: Float64Array;
                            _elementV: Float64Array;
                            _elementNext: Int32Array;
                            _elementHash: Int32Array;
                            _threshold: number;
                            _elementCount: number;
                            constructor(p_stateSize: number, p_elementK: string[], p_elementKH: Float64Array, p_elementV: Float64Array, p_elementNext: Int32Array, p_elementHash: Int32Array, p_elementCount: number);
                            clone(): org.mwg.core.chunk.heap.ArrayStringLongMap.InternalState;
                        }
                    }
                    class FixedStack implements org.mwg.core.chunk.Stack {
                        private _first;
                        private _last;
                        private _next;
                        private _prev;
                        private _count;
                        private lock;
                        private _capacity;
                        constructor(capacity: number);
                        enqueue(index: number): boolean;
                        dequeueTail(): number;
                        dequeue(index: number): boolean;
                        free(): void;
                        size(): number;
                    }
                    interface HeapChunk extends org.mwg.plugin.Chunk {
                        mark(): number;
                        unmark(): number;
                        setFlags(bitsToEnable: number, bitsToDisable: number): boolean;
                    }
                    class HeapChunkSpace implements org.mwg.plugin.ChunkSpace, org.mwg.core.chunk.ChunkListener {
                        private _maxEntries;
                        private _saveBatchSize;
                        private _elementCount;
                        private _lru;
                        private _graph;
                        private _elementNext;
                        private _elementHash;
                        private _values;
                        private _elementHashLock;
                        private _dirtyState;
                        setGraph(p_graph: org.mwg.Graph): void;
                        graph(): org.mwg.Graph;
                        getValues(): org.mwg.plugin.Chunk[];
                        constructor(initialCapacity: number, saveBatchSize: number);
                        getAndMark(type: number, world: number, time: number, id: number): org.mwg.plugin.Chunk;
                        getOrLoadAndMark(type: number, world: number, time: number, id: number, callback: org.mwg.Callback<org.mwg.plugin.Chunk>): void;
                        unmark(type: number, world: number, time: number, id: number): void;
                        unmarkChunk(chunk: org.mwg.plugin.Chunk): void;
                        freeChunk(chunk: org.mwg.plugin.Chunk): void;
                        create(p_type: number, p_world: number, p_time: number, p_id: number, p_initialPayload: org.mwg.struct.Buffer, origin: org.mwg.plugin.Chunk): org.mwg.plugin.Chunk;
                        putAndMark(p_elem: org.mwg.plugin.Chunk): org.mwg.plugin.Chunk;
                        detachDirties(): org.mwg.plugin.ChunkIterator;
                        declareDirty(dirtyChunk: org.mwg.plugin.Chunk): void;
                        declareClean(cleanChunk: org.mwg.plugin.Chunk): void;
                        clear(): void;
                        free(): void;
                        size(): number;
                        available(): number;
                        printMarked(): void;
                    }
                    module HeapChunkSpace {
                        class InternalDirtyStateList implements org.mwg.plugin.ChunkIterator {
                            private _nextCounter;
                            private _dirtyElements;
                            private _max;
                            private _iterationCounter;
                            private _parent;
                            constructor(maxSize: number, p_parent: org.mwg.core.chunk.heap.HeapChunkSpace);
                            hasNext(): boolean;
                            next(): org.mwg.plugin.Chunk;
                            declareDirty(dirtyIndex: number): boolean;
                            size(): number;
                            free(): void;
                        }
                    }
                    class HeapGenChunk implements org.mwg.core.chunk.GenChunk, org.mwg.core.chunk.heap.HeapChunk {
                        private _world;
                        private _time;
                        private _id;
                        private _space;
                        private _flags;
                        private _marks;
                        private _prefix;
                        private _currentIndex;
                        constructor(p_world: number, p_time: number, p_id: number, p_space: org.mwg.core.chunk.ChunkListener, initialPayload: org.mwg.struct.Buffer);
                        private load(payload);
                        save(buffer: org.mwg.struct.Buffer): void;
                        merge(buffer: org.mwg.struct.Buffer): void;
                        newKey(): number;
                        world(): number;
                        time(): number;
                        id(): number;
                        chunkType(): number;
                        marks(): number;
                        mark(): number;
                        unmark(): number;
                        flags(): number;
                        setFlags(bitsToEnable: number, bitsToDisable: number): boolean;
                        private internal_set_dirty();
                    }
                    class HeapStateChunk implements org.mwg.core.chunk.heap.HeapChunk, org.mwg.core.chunk.StateChunk, org.mwg.core.chunk.ChunkListener {
                        private _world;
                        private _time;
                        private _id;
                        private state;
                        private _flags;
                        private _marks;
                        private _space;
                        private inLoadMode;
                        declareDirty(chunk: org.mwg.plugin.Chunk): void;
                        graph(): org.mwg.Graph;
                        constructor(p_world: number, p_time: number, p_id: number, p_space: org.mwg.core.chunk.ChunkListener, initialPayload: org.mwg.struct.Buffer, origin: org.mwg.plugin.Chunk);
                        world(): number;
                        time(): number;
                        id(): number;
                        chunkType(): number;
                        marks(): number;
                        mark(): number;
                        unmark(): number;
                        set(p_elementIndex: number, p_elemType: number, p_unsafe_elem: any): void;
                        setFromKey(key: string, p_elemType: number, p_unsafe_elem: any): void;
                        private internal_set(p_elementIndex, p_elemType, p_unsafe_elem, replaceIfPresent);
                        get(p_elementIndex: number): any;
                        getFromKey(key: string): any;
                        getFromKeyWithDefault<A>(key: string, defaultValue: A): A;
                        getType(p_elementIndex: number): number;
                        getTypeFromKey(key: string): number;
                        getOrCreate(p_elementIndex: number, elemType: number): any;
                        getOrCreateFromKey(key: string, elemType: number): any;
                        each(callBack: org.mwg.plugin.NodeStateCallback): void;
                        merge(buffer: org.mwg.struct.Buffer): void;
                        private load(payload, isMerge);
                        save(buffer: org.mwg.struct.Buffer): void;
                        private internal_set_dirty();
                        flags(): number;
                        setFlags(bitsToEnable: number, bitsToDisable: number): boolean;
                    }
                    module HeapStateChunk {
                        class InternalState {
                            _elementDataSize: number;
                            _elementK: Float64Array;
                            _elementV: any[];
                            _elementNext: Int32Array;
                            _elementHash: Int32Array;
                            _elementType: Int8Array;
                            threshold: number;
                            _elementCount: number;
                            hashReadOnly: boolean;
                            constructor(elementDataSize: number, p_elementK: Float64Array, p_elementV: any[], p_elementNext: Int32Array, p_elementHash: Int32Array, p_elementType: Int8Array, p_elementCount: number, p_hashReadOnly: boolean);
                            deepClone(): org.mwg.core.chunk.heap.HeapStateChunk.InternalState;
                            softClone(): org.mwg.core.chunk.heap.HeapStateChunk.InternalState;
                        }
                    }
                    class HeapTimeTreeChunk implements org.mwg.core.chunk.TimeTreeChunk, org.mwg.core.chunk.heap.HeapChunk {
                        private static META_SIZE;
                        private _world;
                        private _time;
                        private _id;
                        private _listener;
                        private _threshold;
                        private _root_index;
                        private _size;
                        private _back_meta;
                        private _back_k;
                        private _back_colors;
                        private _lock;
                        private _flags;
                        private _marks;
                        private _magic;
                        constructor(p_world: number, p_time: number, p_obj: number, p_listener: org.mwg.core.chunk.ChunkListener, initialPayload: org.mwg.struct.Buffer);
                        private lock();
                        private unlock();
                        marks(): number;
                        mark(): number;
                        unmark(): number;
                        world(): number;
                        time(): number;
                        id(): number;
                        flags(): number;
                        setFlags(bitsToEnable: number, bitsToDisable: number): boolean;
                        size(): number;
                        range(startKey: number, endKey: number, maxElements: number, walker: org.mwg.core.chunk.TreeWalker): void;
                        save(buffer: org.mwg.struct.Buffer): void;
                        private load(buffer);
                        merge(buffer: org.mwg.struct.Buffer): void;
                        previousOrEqual(key: number): number;
                        magic(): number;
                        insert(p_key: number): void;
                        unsafe_insert(p_key: number): void;
                        chunkType(): number;
                        clearAt(max: number): void;
                        private allocate(capacity);
                        private reallocate(newCapacity);
                        private key(p_currentIndex);
                        private setKey(p_currentIndex, p_paramIndex);
                        value(p_currentIndex: number): number;
                        private setValue(p_currentIndex, p_paramIndex);
                        private left(p_currentIndex);
                        private setLeft(p_currentIndex, p_paramIndex);
                        private right(p_currentIndex);
                        private setRight(p_currentIndex, p_paramIndex);
                        private parent(p_currentIndex);
                        private setParent(p_currentIndex, p_paramIndex);
                        private color(p_currentIndex);
                        private setColor(p_currentIndex, p_paramIndex);
                        private grandParent(p_currentIndex);
                        private sibling(p_currentIndex);
                        private uncle(p_currentIndex);
                        private previous(p_index);
                        private internal_previousOrEqual_index(p_key);
                        private rotateLeft(n);
                        private rotateRight(n);
                        private replaceNode(oldn, newn);
                        private insertCase1(n);
                        private insertCase2(n);
                        private insertCase3(n);
                        private insertCase4(n_n);
                        private insertCase5(n);
                        private internal_insert(p_key);
                        private internal_set_dirty();
                    }
                    class HeapWorldOrderChunk implements org.mwg.core.chunk.WorldOrderChunk, org.mwg.core.chunk.heap.HeapChunk {
                        private _world;
                        private _time;
                        private _id;
                        private _listener;
                        private _lock;
                        private _marks;
                        private _magic;
                        private _flags;
                        private _extra;
                        private state;
                        world(): number;
                        time(): number;
                        id(): number;
                        extra(): number;
                        setExtra(extraValue: number): void;
                        constructor(p_universe: number, p_time: number, p_obj: number, p_listener: org.mwg.core.chunk.ChunkListener, initialPayload: org.mwg.struct.Buffer);
                        lock(): void;
                        unlock(): void;
                        marks(): number;
                        mark(): number;
                        unmark(): number;
                        magic(): number;
                        private rehashCapacity(capacity, previousState);
                        each(callback: org.mwg.struct.LongLongMapCallBack): void;
                        get(key: number): number;
                        put(key: number, value: number): void;
                        merge(buffer: org.mwg.struct.Buffer): void;
                        private findNonNullKeyEntry(key, index, internalState);
                        remove(key: number): void;
                        size(): number;
                        private load(buffer);
                        save(buffer: org.mwg.struct.Buffer): void;
                        chunkType(): number;
                        private internal_set_dirty();
                        flags(): number;
                        setFlags(bitsToEnable: number, bitsToDisable: number): boolean;
                    }
                    module HeapWorldOrderChunk {
                        class InternalState {
                            threshold: number;
                            elementDataSize: number;
                            elementKV: Float64Array;
                            elementNext: Int32Array;
                            elementHash: Int32Array;
                            elementCount: number;
                            constructor(elementDataSize: number, elementKV: Float64Array, elementNext: Int32Array, elementHash: Int32Array, elemCount: number);
                        }
                    }
                }
                interface LongTree {
                    insert(key: number): void;
                    unsafe_insert(key: number): void;
                    previousOrEqual(key: number): number;
                    clearAt(max: number): void;
                    range(startKey: number, endKey: number, maxElements: number, walker: org.mwg.core.chunk.TreeWalker): void;
                    magic(): number;
                    size(): number;
                }
                module offheap {
                }
                interface Stack {
                    enqueue(index: number): boolean;
                    dequeueTail(): number;
                    dequeue(index: number): boolean;
                    free(): void;
                    size(): number;
                }
                interface StateChunk extends org.mwg.plugin.Chunk, org.mwg.plugin.NodeState {
                }
                interface TimeTreeChunk extends org.mwg.core.chunk.LongTree, org.mwg.plugin.Chunk {
                }
                interface TreeWalker {
                    (t: number): void;
                }
                interface WorldOrderChunk extends org.mwg.plugin.Chunk, org.mwg.struct.LongLongMap {
                    magic(): number;
                    lock(): void;
                    unlock(): void;
                    extra(): number;
                    setExtra(extraValue: number): void;
                }
            }
            class CoreConstants extends org.mwg.Constants {
                static CHUNK_SEP: number;
                static CHUNK_SUB_SEP: number;
                static CHUNK_SUB_SUB_SEP: number;
                static CHUNK_SUB_SUB_SUB_SEP: number;
                static DIRTY_BIT: number;
                static PREVIOUS_RESOLVED_WORLD_INDEX: number;
                static PREVIOUS_RESOLVED_SUPER_TIME_INDEX: number;
                static PREVIOUS_RESOLVED_TIME_INDEX: number;
                static PREVIOUS_RESOLVED_WORLD_MAGIC: number;
                static PREVIOUS_RESOLVED_SUPER_TIME_MAGIC: number;
                static PREVIOUS_RESOLVED_TIME_MAGIC: number;
                static PREFIX_TO_SAVE_SIZE: number;
                static NULL_KEY: Float64Array;
                static GLOBAL_UNIVERSE_KEY: Float64Array;
                static GLOBAL_DICTIONARY_KEY: Float64Array;
                static GLOBAL_INDEX_KEY: Float64Array;
                static INDEX_ATTRIBUTE: string;
                static MAP_INITIAL_CAPACITY: number;
                static MAP_LOAD_FACTOR: number;
                static DISCONNECTED_ERROR: string;
                static OFFHEAP_NULL_PTR: number;
                static OFFHEAP_CHUNK_INDEX_WORLD: number;
                static OFFHEAP_CHUNK_INDEX_TIME: number;
                static OFFHEAP_CHUNK_INDEX_ID: number;
                static OFFHEAP_CHUNK_INDEX_TYPE: number;
                static OFFHEAP_CHUNK_INDEX_FLAGS: number;
                static OFFHEAP_CHUNK_INDEX_MARKS: number;
                static SCALE_1: number;
                static SCALE_2: number;
                static SCALE_3: number;
                static SCALE_4: number;
                static DEAD_NODE_ERROR: string;
                static BOOL_TRUE: number;
                static BOOL_FALSE: number;
            }
            class CoreGraph implements org.mwg.Graph {
                private _storage;
                private _space;
                private _scheduler;
                private _resolver;
                private _factories;
                private _factoryNames;
                offHeapBuffer: boolean;
                private _prefix;
                private _nodeKeyCalculator;
                private _worldKeyCalculator;
                private _isConnected;
                private _lock;
                private _registry;
                constructor(p_storage: org.mwg.plugin.Storage, p_space: org.mwg.plugin.ChunkSpace, p_scheduler: org.mwg.plugin.Scheduler, p_resolver: org.mwg.plugin.Resolver, p_factories: org.mwg.plugin.NodeFactory[]);
                diverge(world: number): number;
                newNode(world: number, time: number): org.mwg.Node;
                newTypedNode(world: number, time: number, nodeType: string): org.mwg.Node;
                cloneNode(origin: org.mwg.Node): org.mwg.Node;
                factoryByCode(code: number): org.mwg.plugin.NodeFactory;
                lookup<A extends org.mwg.Node>(world: number, time: number, id: number, callback: org.mwg.Callback<A>): void;
                save(callback: org.mwg.Callback<boolean>): void;
                connect(callback: org.mwg.Callback<boolean>): void;
                disconnect(callback: org.mwg.Callback<any>): void;
                newBuffer(): org.mwg.struct.Buffer;
                newTask(): org.mwg.task.Task;
                newQuery(): org.mwg.Query;
                private saveDirtyList(dirtyIterator, callback);
                index(indexName: string, toIndexNode: org.mwg.Node, flatKeyAttributes: string, callback: org.mwg.Callback<boolean>): void;
                unindex(indexName: string, toIndexNode: org.mwg.Node, flatKeyAttributes: string, callback: org.mwg.Callback<boolean>): void;
                find(world: number, time: number, indexName: string, query: string, callback: org.mwg.Callback<org.mwg.Node[]>): void;
                findQuery(query: org.mwg.Query, callback: org.mwg.Callback<org.mwg.Node[]>): void;
                all(world: number, time: number, indexName: string, callback: org.mwg.Callback<org.mwg.Node[]>): void;
                namedIndex(world: number, time: number, indexName: string, callback: org.mwg.Callback<org.mwg.Node>): void;
                private getIndexOrCreate(world, time, indexName, callback, createIfNull);
                counter(expectedCountCalls: number): org.mwg.DeferCounter;
                resolver(): org.mwg.plugin.Resolver;
                scheduler(): org.mwg.plugin.Scheduler;
                space(): org.mwg.plugin.ChunkSpace;
                actions(): org.mwg.task.TaskActionRegistry;
                storage(): org.mwg.plugin.Storage;
            }
            class CoreNode extends org.mwg.plugin.AbstractNode {
                constructor(p_world: number, p_time: number, p_id: number, p_graph: org.mwg.Graph, currentResolution: Float64Array);
            }
            class CoreQuery implements org.mwg.Query {
                private _resolver;
                private capacity;
                private _attributes;
                private _values;
                private size;
                private _hash;
                private _world;
                private _time;
                private _indexName;
                constructor(p_resolver: org.mwg.plugin.Resolver);
                parseString(flatQuery: string): org.mwg.Query;
                add(attributeName: string, value: any): org.mwg.Query;
                setWorld(initialWorld: number): org.mwg.Query;
                world(): number;
                setTime(initialTime: number): org.mwg.Query;
                time(): number;
                setIndexName(indexName: string): org.mwg.Query;
                indexName(): string;
                hash(): number;
                attributes(): Float64Array;
                values(): any[];
                private internal_add(att, val);
                private compute();
            }
            class MWGResolver implements org.mwg.plugin.Resolver {
                private _storage;
                private _space;
                private _tracker;
                private _scheduler;
                private _graph;
                private dictionary;
                private static KEY_SIZE;
                constructor(p_storage: org.mwg.plugin.Storage, p_space: org.mwg.plugin.ChunkSpace, p_tracker: org.mwg.core.NodeTracker, p_scheduler: org.mwg.plugin.Scheduler);
                init(graph: org.mwg.Graph): void;
                markNodeAndGetType(node: org.mwg.Node): number;
                initNode(node: org.mwg.Node, codeType: number): void;
                initWorld(parentWorld: number, childWorld: number): void;
                freeNode(node: org.mwg.Node): void;
                lookup<A extends org.mwg.Node>(world: number, time: number, id: number, callback: org.mwg.Callback<A>): void;
                lookupJob<A extends org.mwg.Node>(world: number, time: number, id: number, callback: org.mwg.Callback<A>): org.mwg.plugin.Job;
                private resolve_world(globalWorldOrder, nodeWorldOrder, timeToResolve, originWorld);
                private getOrLoadAndMark(type, world, time, id, callback);
                private getOrLoadAndMarkAll(types, keys, callback);
                newState(node: org.mwg.Node, world: number, time: number): org.mwg.plugin.NodeState;
                resolveState(node: org.mwg.Node, allowDephasing: boolean): org.mwg.plugin.NodeState;
                resolveTimepoints(node: org.mwg.Node, beginningOfSearch: number, endOfSearch: number, callback: org.mwg.Callback<Float64Array>): void;
                private resolveTimepointsFromWorlds(globalWorldOrder, objectWorldOrder, node, beginningOfSearch, endOfSearch, collectedWorlds, collectedWorldsSize, callback);
                private resolveTimepointsFromSuperTimes(globalWorldOrder, objectWorldOrder, node, beginningOfSearch, endOfSearch, collectedWorlds, collectedSuperTimes, collectedSize, callback);
                stringToHash(name: string, insertIfNotExists: boolean): number;
                hashToString(key: number): string;
            }
            interface NodeTracker {
                monitor(node: org.mwg.Node): void;
            }
            class NoopNodeTracker implements org.mwg.core.NodeTracker {
                monitor(node: org.mwg.Node): void;
            }
            module scheduler {
                class NoopScheduler implements org.mwg.plugin.Scheduler {
                    dispatch(job: org.mwg.plugin.Job): void;
                    start(): void;
                    stop(): void;
                }
            }
            module task {
                class ActionAsVar implements org.mwg.task.TaskAction {
                    private _name;
                    constructor(p_name: string);
                    eval(context: org.mwg.task.TaskContext): void;
                }
                class ActionForeach implements org.mwg.task.TaskAction {
                    private _subTask;
                    constructor(p_subTask: org.mwg.task.Task);
                    eval(context: org.mwg.task.TaskContext): void;
                    static convert(elem: any): any[];
                }
                class ActionForeachPar implements org.mwg.task.TaskAction {
                    private _subTask;
                    constructor(p_subTask: org.mwg.task.Task);
                    eval(context: org.mwg.task.TaskContext): void;
                }
                class ActionFrom implements org.mwg.task.TaskAction {
                    private _value;
                    constructor(value: any);
                    eval(context: org.mwg.task.TaskContext): void;
                }
                class ActionFromIndex implements org.mwg.task.TaskAction {
                    private _indexName;
                    private _query;
                    constructor(p_indexName: string, p_query: string);
                    eval(context: org.mwg.task.TaskContext): void;
                }
                class ActionFromIndexAll implements org.mwg.task.TaskAction {
                    private _indexName;
                    constructor(p_indexName: string);
                    eval(context: org.mwg.task.TaskContext): void;
                }
                class ActionFromVar implements org.mwg.task.TaskAction {
                    private _name;
                    constructor(p_name: string);
                    eval(context: org.mwg.task.TaskContext): void;
                }
                class ActionIfThen implements org.mwg.task.TaskAction {
                    private _condition;
                    private _action;
                    constructor(cond: org.mwg.task.TaskFunctionConditional, action: org.mwg.task.Task);
                    eval(context: org.mwg.task.TaskContext): void;
                }
                class ActionMap implements org.mwg.task.TaskAction {
                    private _map;
                    constructor(p_map: org.mwg.task.TaskFunctionMap);
                    eval(context: org.mwg.task.TaskContext): void;
                    private filterArray(current);
                }
                class ActionNoop implements org.mwg.task.TaskAction {
                    eval(context: org.mwg.task.TaskContext): void;
                }
                class ActionSave implements org.mwg.task.TaskAction {
                    eval(context: org.mwg.task.TaskContext): void;
                }
                class ActionSelect implements org.mwg.task.TaskAction {
                    private _filter;
                    constructor(p_filter: org.mwg.task.TaskFunctionSelect);
                    eval(context: org.mwg.task.TaskContext): void;
                    private filterArray(current);
                }
                class ActionTime implements org.mwg.task.TaskAction {
                    private _time;
                    constructor(p_time: number);
                    eval(context: org.mwg.task.TaskContext): void;
                }
                class ActionTraverse implements org.mwg.task.TaskAction {
                    private _name;
                    constructor(p_name: string);
                    eval(context: org.mwg.task.TaskContext): void;
                    private collectArray(current, toLoad);
                }
                class ActionTraverseIndex implements org.mwg.task.TaskAction {
                    private _indexName;
                    private _query;
                    constructor(indexName: string, query: string);
                    eval(context: org.mwg.task.TaskContext): void;
                    private getNodes(previousResult);
                    private countNbNodeToLoad(nodes);
                }
                class ActionTraverseOrKeep implements org.mwg.task.TaskAction {
                    private _name;
                    constructor(p_name: string);
                    eval(context: org.mwg.task.TaskContext): void;
                    private collectArray(current, toLoad);
                }
                class ActionTrigger implements org.mwg.task.TaskAction {
                    private _subTask;
                    constructor(p_subTask: org.mwg.task.Task);
                    eval(context: org.mwg.task.TaskContext): void;
                }
                class ActionWhileDo implements org.mwg.task.TaskAction {
                    private _cond;
                    private _then;
                    constructor(p_cond: org.mwg.task.TaskFunctionConditional, p_then: org.mwg.task.Task);
                    eval(context: org.mwg.task.TaskContext): void;
                }
                class ActionWith extends org.mwg.core.task.ActionSelect {
                    constructor(name: string, pattern: RegExp);
                }
                class ActionWithout extends org.mwg.core.task.ActionSelect {
                    constructor(name: string, pattern: RegExp);
                }
                class ActionWorld implements org.mwg.task.TaskAction {
                    private _world;
                    constructor(p_world: number);
                    eval(context: org.mwg.task.TaskContext): void;
                }
                class ActionWrapper implements org.mwg.task.TaskAction {
                    private _wrapped;
                    private _syncProtection;
                    constructor(p_wrapped: org.mwg.task.Action, p_syncProtection: boolean);
                    eval(context: org.mwg.task.TaskContext): void;
                }
                class CoreTask implements org.mwg.task.Task {
                    private _graph;
                    private _actions;
                    private _actionCursor;
                    constructor(p_graph: org.mwg.Graph);
                    private addAction(task);
                    world(world: number): org.mwg.task.Task;
                    time(time: number): org.mwg.task.Task;
                    fromIndex(indexName: string, query: string): org.mwg.task.Task;
                    fromIndexAll(indexName: string): org.mwg.task.Task;
                    selectWith(name: string, pattern: string): org.mwg.task.Task;
                    selectWithout(name: string, pattern: string): org.mwg.task.Task;
                    asVar(variableName: string): org.mwg.task.Task;
                    fromVar(variableName: string): org.mwg.task.Task;
                    select(filter: org.mwg.task.TaskFunctionSelect): org.mwg.task.Task;
                    selectWhere(subTask: org.mwg.task.Task): org.mwg.task.Task;
                    traverse(relationName: string): org.mwg.task.Task;
                    traverseOrKeep(relationName: string): org.mwg.task.Task;
                    traverseIndex(indexName: string, query: string): org.mwg.task.Task;
                    traverseIndexAll(indexName: string): org.mwg.task.Task;
                    map(mapFunction: org.mwg.task.TaskFunctionMap): org.mwg.task.Task;
                    flatMap(flatMapFunction: org.mwg.task.TaskFunctionFlatMap): org.mwg.task.Task;
                    group(groupFunction: org.mwg.task.TaskFunctionGroup): org.mwg.task.Task;
                    groupWhere(groupSubTask: org.mwg.task.Task): org.mwg.task.Task;
                    from(inputValue: any): org.mwg.task.Task;
                    wait(subTask: org.mwg.task.Task): org.mwg.task.Task;
                    ifThen(cond: org.mwg.task.TaskFunctionConditional, then: org.mwg.task.Task): org.mwg.task.Task;
                    whileDo(cond: org.mwg.task.TaskFunctionConditional, then: org.mwg.task.Task): org.mwg.task.Task;
                    then(p_action: org.mwg.task.Action): org.mwg.task.Task;
                    thenAsync(p_action: org.mwg.task.Action): org.mwg.task.Task;
                    foreachThen<T>(action: org.mwg.Callback<T>): org.mwg.task.Task;
                    foreach(subTask: org.mwg.task.Task): org.mwg.task.Task;
                    foreachPar(subTask: org.mwg.task.Task): org.mwg.task.Task;
                    save(): org.mwg.task.Task;
                    execute(): void;
                    executeThen(p_action: org.mwg.task.Action): void;
                    executeThenAsync(parent: org.mwg.task.TaskContext, initialResult: any, p_finalAction: org.mwg.task.Action): void;
                    action(name: string, flatParams: string): org.mwg.task.Task;
                    parse(flat: string): org.mwg.task.Task;
                    private protect(input);
                    private protectIterable(input);
                }
                class CoreTaskActionRegistry implements org.mwg.task.TaskActionRegistry {
                    private _factory;
                    constructor();
                    add(name: string, action: org.mwg.task.TaskActionFactory): void;
                    remove(name: string): void;
                    get(name: string): org.mwg.task.TaskActionFactory;
                }
                class CoreTaskContext implements org.mwg.task.TaskContext {
                    private _variables;
                    private _results;
                    private _graph;
                    private _actions;
                    private _currentTaskId;
                    private _parentContext;
                    private _initialResult;
                    private _world;
                    private _time;
                    constructor(p_parentContext: org.mwg.task.TaskContext, p_initialResult: any, p_graph: org.mwg.Graph, p_actions: org.mwg.task.TaskAction[]);
                    graph(): org.mwg.Graph;
                    getWorld(): number;
                    setWorld(p_world: number): void;
                    getTime(): number;
                    setTime(p_time: number): void;
                    getVariable(name: string): any;
                    getVariablesKeys(): string[];
                    addToVariable(name: string, value: any): void;
                    setVariable(name: string, value: any): void;
                    getPreviousResult(): any;
                    setResult(actionResult: any): void;
                    private mergeVariables(actionResult);
                    next(): void;
                    clean(): void;
                    private cleanObj(o);
                }
                class TaskContextWrapper implements org.mwg.task.TaskContext {
                    private _wrapped;
                    constructor(p_wrapped: org.mwg.task.TaskContext);
                    graph(): org.mwg.Graph;
                    getWorld(): number;
                    setWorld(world: number): void;
                    getTime(): number;
                    setTime(time: number): void;
                    getVariable(name: string): any;
                    getVariablesKeys(): string[];
                    setVariable(name: string, value: any): void;
                    addToVariable(name: string, value: any): void;
                    getPreviousResult(): any;
                    setResult(actionResult: any): void;
                    next(): void;
                    clean(): void;
                    toString(): string;
                }
            }
            module utility {
                abstract class AbstractBuffer implements org.mwg.struct.Buffer {
                    abstract slice(initPos: number, endPos: number): Int8Array;
                    iterator(): org.mwg.struct.BufferIterator;
                    abstract read(position: number): number;
                    abstract size(): number;
                    abstract write(b: number): void;
                    abstract writeAll(bytes: Int8Array): void;
                    abstract data(): Int8Array;
                    abstract free(): void;
                    abstract removeLast(): void;
                }
                class BufferBuilder {
                    constructor();
                    static keyToBuffer(buffer: org.mwg.struct.Buffer, chunkType: number, world: number, time: number, id: number): void;
                    static getNewSize(old: number, target: number): number;
                    static newOffHeapBuffer(): org.mwg.struct.Buffer;
                    static newHeapBuffer(): org.mwg.struct.Buffer;
                }
                class BufferView implements org.mwg.struct.Buffer {
                    private _origin;
                    private _initPos;
                    private _endPos;
                    constructor(p_origin: org.mwg.core.utility.AbstractBuffer, p_initPos: number, p_endPos: number);
                    write(b: number): void;
                    writeAll(bytes: Int8Array): void;
                    read(position: number): number;
                    data(): Int8Array;
                    size(): number;
                    free(): void;
                    iterator(): org.mwg.struct.BufferIterator;
                    removeLast(): void;
                }
                class CoreBufferIterator implements org.mwg.struct.BufferIterator {
                    private _origin;
                    private _originSize;
                    private _cursor;
                    constructor(p_origin: org.mwg.core.utility.AbstractBuffer);
                    hasNext(): boolean;
                    next(): org.mwg.struct.Buffer;
                }
                class CoreDeferCounter implements org.mwg.DeferCounter {
                    private _nb_down;
                    private _counter;
                    private _end;
                    constructor(nb: number);
                    count(): void;
                    then(p_callback: org.mwg.Callback<any>): void;
                }
                class DataHasher {
                    private static byteTable;
                    private static HSTART;
                    private static HMULT;
                    static hash(data: string): number;
                    static hashBytes(data: Int8Array): number;
                }
                class HeapBuffer extends org.mwg.core.utility.AbstractBuffer {
                    private buffer;
                    private writeCursor;
                    slice(initPos: number, endPos: number): Int8Array;
                    write(b: number): void;
                    writeAll(bytes: Int8Array): void;
                    read(position: number): number;
                    data(): Int8Array;
                    size(): number;
                    free(): void;
                    removeLast(): void;
                }
                class PrimitiveHelper {
                    static PRIME1: Long;
                    static PRIME2: Long;
                    static PRIME3: Long;
                    static PRIME4: Long;
                    static PRIME5: Long;
                    private static len;
                    static longHash(number: number, max: number): number;
                    static tripleHash(p0: number, p1: number, p2: number, p3: number, max: number): number;
                    static rand(): number;
                    static equals(src: string, other: string): boolean;
                    static DOUBLE_MIN_VALUE(): number;
                    static DOUBLE_MAX_VALUE(): number;
                    static isDefined(param: any): boolean;
                    static iterate(elem: any, callback: org.mwg.Callback<any>): boolean;
                }
                class ReadOnlyStorage implements org.mwg.plugin.Storage {
                    private wrapped;
                    constructor(toWrap: org.mwg.plugin.Storage);
                    get(keys: org.mwg.struct.Buffer, callback: org.mwg.Callback<org.mwg.struct.Buffer>): void;
                    put(stream: org.mwg.struct.Buffer, callback: org.mwg.Callback<boolean>): void;
                    remove(keys: org.mwg.struct.Buffer, callback: org.mwg.Callback<boolean>): void;
                    connect(graph: org.mwg.Graph, callback: org.mwg.Callback<boolean>): void;
                    disconnect(callback: org.mwg.Callback<boolean>): void;
                    lock(callback: org.mwg.Callback<org.mwg.struct.Buffer>): void;
                    unlock(previousLock: org.mwg.struct.Buffer, callback: org.mwg.Callback<boolean>): void;
                }
            }
        }
    }
}
