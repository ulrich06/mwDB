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
        class Double {
            static MAX_VALUE: number;
            static POSITIVE_INFINITY: number;
            static NEGATIVE_INFINITY: number;
            static NaN: number;
        }
        class Long {
            static parseLong(d: any): number;
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
            private content;
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
            then(job: org.mwg.plugin.Job): void;
            wrap(): org.mwg.Callback<any>;
        }
        interface DeferCounterSync extends org.mwg.DeferCounter {
            waitResult(): any;
        }
        interface Graph {
            newNode(world: number, time: number): org.mwg.Node;
            newTypedNode(world: number, time: number, nodeType: string): org.mwg.Node;
            cloneNode(origin: org.mwg.Node): org.mwg.Node;
            lookup<A extends org.mwg.Node>(world: number, time: number, id: number, callback: org.mwg.Callback<A>): void;
            fork(world: number): number;
            save(callback: org.mwg.Callback<boolean>): void;
            connect(callback: org.mwg.Callback<boolean>): void;
            disconnect(callback: org.mwg.Callback<boolean>): void;
            index(indexName: string, nodeToIndex: org.mwg.Node, flatKeyAttributes: string, callback: org.mwg.Callback<boolean>): void;
            unindex(indexName: string, nodeToUnindex: org.mwg.Node, flatKeyAttributes: string, callback: org.mwg.Callback<boolean>): void;
            indexes(world: number, time: number, callback: org.mwg.Callback<string[]>): void;
            find(world: number, time: number, indexName: string, query: string, callback: org.mwg.Callback<org.mwg.Node[]>): void;
            findByQuery(query: org.mwg.Query, callback: org.mwg.Callback<org.mwg.Node[]>): void;
            findAll(world: number, time: number, indexName: string, callback: org.mwg.Callback<org.mwg.Node[]>): void;
            getIndexNode(world: number, time: number, indexName: string, callback: org.mwg.Callback<org.mwg.Node>): void;
            newCounter(expectedEventsCount: number): org.mwg.DeferCounter;
            newSyncCounter(expectedEventsCount: number): org.mwg.DeferCounterSync;
            resolver(): org.mwg.plugin.Resolver;
            scheduler(): org.mwg.plugin.Scheduler;
            space(): org.mwg.plugin.ChunkSpace;
            storage(): org.mwg.plugin.Storage;
            newBuffer(): org.mwg.struct.Buffer;
            newQuery(): org.mwg.Query;
            freeNodes(nodes: org.mwg.Node[]): void;
            taskAction(name: string): org.mwg.task.TaskActionFactory;
        }
        class GraphBuilder {
            private _storage;
            private _scheduler;
            private _plugins;
            private _offHeap;
            private _gc;
            private _memorySize;
            private _saveBatchSize;
            private _readOnly;
            private static _internalBuilder;
            withOffHeapMemory(): org.mwg.GraphBuilder;
            withStorage(storage: org.mwg.plugin.Storage): org.mwg.GraphBuilder;
            withReadOnlyStorage(storage: org.mwg.plugin.Storage): org.mwg.GraphBuilder;
            withMemorySize(numberOfElements: number): org.mwg.GraphBuilder;
            saveEvery(numberOfElements: number): org.mwg.GraphBuilder;
            withScheduler(scheduler: org.mwg.plugin.Scheduler): org.mwg.GraphBuilder;
            withPlugin(plugin: org.mwg.plugin.Plugin): org.mwg.GraphBuilder;
            withGC(): org.mwg.GraphBuilder;
            build(): org.mwg.Graph;
        }
        module GraphBuilder {
            interface InternalBuilder {
                newGraph(storage: org.mwg.plugin.Storage, readOnly: boolean, scheduler: org.mwg.plugin.Scheduler, plugins: org.mwg.plugin.Plugin[], usingGC: boolean, usingOffHeapMemory: boolean, memorySize: number, autoSaveSize: number): org.mwg.Graph;
                newTask(): org.mwg.task.Task;
            }
        }
        interface Node {
            world(): number;
            time(): number;
            id(): number;
            get(propertyName: string): any;
            type(propertyName: string): number;
            nodeTypeName(): string;
            set(propertyName: string, propertyValue: any): void;
            setProperty(propertyName: string, propertyType: number, propertyValue: any): void;
            getOrCreateMap(propertyName: string, propertyType: number): org.mwg.struct.Map;
            removeProperty(propertyName: string): void;
            rel(relationName: string, callback: org.mwg.Callback<org.mwg.Node[]>): void;
            add(relationName: string, relatedNode: org.mwg.Node): void;
            remove(relationName: string, relatedNode: org.mwg.Node): void;
            index(indexName: string, nodeToIndex: org.mwg.Node, flatKeyAttributes: string, callback: org.mwg.Callback<boolean>): void;
            unindex(indexName: string, nodeToIndex: org.mwg.Node, flatKeyAttributes: string, callback: org.mwg.Callback<boolean>): void;
            find(indexName: string, query: string, callback: org.mwg.Callback<org.mwg.Node[]>): void;
            findByQuery(query: org.mwg.Query, callback: org.mwg.Callback<org.mwg.Node[]>): void;
            findAll(indexName: string, callback: org.mwg.Callback<org.mwg.Node[]>): void;
            timeDephasing(): number;
            rephase(): void;
            timepoints(beginningOfSearch: number, endOfSearch: number, callback: org.mwg.Callback<Float64Array>): void;
            free(): void;
            graph(): org.mwg.Graph;
            jump<A extends org.mwg.Node>(targetTime: number, callback: org.mwg.Callback<A>): void;
        }
        module plugin {
            abstract class AbstractIterable {
                abstract next(): any;
                abstract close(): void;
                abstract estimate(): number;
            }
            abstract class AbstractNode implements org.mwg.Node {
                private _world;
                private _time;
                private _id;
                private _graph;
                _resolver: org.mwg.plugin.Resolver;
                _previousResolveds: java.util.concurrent.atomic.AtomicReference<Float64Array>;
                constructor(p_world: number, p_time: number, p_id: number, p_graph: org.mwg.Graph, currentResolution: Float64Array);
                init(): void;
                nodeTypeName(): string;
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
                getOrCreateMap(propertyName: string, propertyType: number): org.mwg.struct.Map;
                type(propertyName: string): number;
                removeProperty(attributeName: string): void;
                rel(relationName: string, callback: org.mwg.Callback<org.mwg.Node[]>): void;
                add(relationName: string, relatedNode: org.mwg.Node): void;
                remove(relationName: string, relatedNode: org.mwg.Node): void;
                free(): void;
                timeDephasing(): number;
                rephase(): void;
                timepoints(beginningOfSearch: number, endOfSearch: number, callback: org.mwg.Callback<Float64Array>): void;
                jump<A extends org.mwg.Node>(targetTime: number, callback: org.mwg.Callback<A>): void;
                findByQuery(query: org.mwg.Query, callback: org.mwg.Callback<org.mwg.Node[]>): void;
                find(indexName: string, query: string, callback: org.mwg.Callback<org.mwg.Node[]>): void;
                findAll(indexName: string, callback: org.mwg.Callback<org.mwg.Node[]>): void;
                index(indexName: string, nodeToIndex: org.mwg.Node, flatKeyAttributes: string, callback: org.mwg.Callback<boolean>): void;
                unindex(indexName: string, nodeToIndex: org.mwg.Node, flatKeyAttributes: string, callback: org.mwg.Callback<boolean>): void;
                private isNaN(toTest);
                toString(): string;
            }
            class AbstractPlugin implements org.mwg.plugin.Plugin {
                private _nodeTypes;
                private _taskActions;
                declareNodeType(name: string, factory: org.mwg.plugin.NodeFactory): org.mwg.plugin.Plugin;
                declareTaskAction(name: string, factory: org.mwg.task.TaskActionFactory): org.mwg.plugin.Plugin;
                nodeTypes(): string[];
                nodeType(nodeTypeName: string): org.mwg.plugin.NodeFactory;
                taskActionTypes(): string[];
                taskActionType(taskTypeName: string): org.mwg.task.TaskActionFactory;
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
            class Enforcer {
                private checkers;
                asBool(propertyName: string): org.mwg.plugin.Enforcer;
                asString(propertyName: string): org.mwg.plugin.Enforcer;
                asLong(propertyName: string): org.mwg.plugin.Enforcer;
                asLongWithin(propertyName: string, min: number, max: number): org.mwg.plugin.Enforcer;
                asDouble(propertyName: string): org.mwg.plugin.Enforcer;
                asDoubleWithin(propertyName: string, min: number, max: number): org.mwg.plugin.Enforcer;
                asInt(propertyName: string): org.mwg.plugin.Enforcer;
                asIntWithin(propertyName: string, min: number, max: number): org.mwg.plugin.Enforcer;
                asIntGreaterOrEquals(propertyName: string, min: number): org.mwg.plugin.Enforcer;
                asDoubleArray(propertyName: string): org.mwg.plugin.Enforcer;
                asPositiveInt(propertyName: string): org.mwg.plugin.Enforcer;
                asNonNegativeDouble(propertyName: string): org.mwg.plugin.Enforcer;
                asPositiveDouble(propertyName: string): org.mwg.plugin.Enforcer;
                asNonNegativeOrNanDouble(propertyName: string): org.mwg.plugin.Enforcer;
                asPositiveLong(propertyName: string): org.mwg.plugin.Enforcer;
                declare(propertyName: string, checker: org.mwg.plugin.EnforcerChecker): org.mwg.plugin.Enforcer;
                check(propertyName: string, propertyType: number, propertyValue: any): void;
            }
            interface EnforcerChecker {
                check(inputType: number, input: any): void;
            }
            interface Job {
                (): void;
            }
            interface NodeFactory {
                (world: number, time: number, id: number, graph: org.mwg.Graph, initialResolution: Float64Array): org.mwg.Node;
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
            interface Plugin {
                declareNodeType(name: string, factory: org.mwg.plugin.NodeFactory): org.mwg.plugin.Plugin;
                declareTaskAction(name: string, factory: org.mwg.task.TaskActionFactory): org.mwg.plugin.Plugin;
                nodeTypes(): string[];
                nodeType(nodeTypeName: string): org.mwg.plugin.NodeFactory;
                taskActionTypes(): string[];
                taskActionType(taskTypeName: string): org.mwg.task.TaskActionFactory;
            }
            interface Resolver {
                init(graph: org.mwg.Graph): void;
                initNode(node: org.mwg.Node, typeCode: number): void;
                markNodeAndGetType(node: org.mwg.Node): number;
                initWorld(parentWorld: number, childWorld: number): void;
                freeNode(node: org.mwg.Node): void;
                typeName(node: org.mwg.Node): string;
                typeCode(node: org.mwg.Node): number;
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
            parse(flatQuery: string): org.mwg.Query;
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
                length(): number;
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
            class Actions {
                private static _internalBuilder;
                static newTask(): org.mwg.task.Task;
                static setWorld(variableName: string): org.mwg.task.Task;
                static setTime(variableName: string): org.mwg.task.Task;
                static then(action: org.mwg.task.Action): org.mwg.task.Task;
                static inject(input: any): org.mwg.task.Task;
                static fromVar(variableName: string): org.mwg.task.Task;
                static fromIndexAll(indexName: string): org.mwg.task.Task;
                static fromIndex(indexName: string, query: string): org.mwg.task.Task;
                static parse(flatTask: string): org.mwg.task.Task;
                static asVar(variableName: string): org.mwg.task.Task;
                static setVar(variableName: string, inputValue: any): org.mwg.task.Task;
                static map(mapFunction: org.mwg.task.TaskFunctionMap): org.mwg.task.Task;
                static selectWith(name: string, pattern: string): org.mwg.task.Task;
                static selectWithout(name: string, pattern: string): org.mwg.task.Task;
                static select(filterFunction: org.mwg.task.TaskFunctionSelect): org.mwg.task.Task;
                static selectObject(filterFunction: org.mwg.task.TaskFunctionSelectObject): org.mwg.task.Task;
                static traverse(relationName: string): org.mwg.task.Task;
                static get(name: string): org.mwg.task.Task;
                static traverseIndex(indexName: string, query: string): org.mwg.task.Task;
                static traverseOrKeep(relationName: string): org.mwg.task.Task;
                static traverseIndexAll(indexName: string): org.mwg.task.Task;
                static repeat(repetition: number, subTask: org.mwg.task.Task): org.mwg.task.Task;
                static repeatPar(repetition: number, subTask: org.mwg.task.Task): org.mwg.task.Task;
                static print(name: string): org.mwg.task.Task;
                static setProperty(propertyName: string, propertyType: number, variableNameToSet: string): org.mwg.task.Task;
                static selectWhere(subTask: org.mwg.task.Task): org.mwg.task.Task;
                static foreach(subTask: org.mwg.task.Task): org.mwg.task.Task;
                static foreachPar(subTask: org.mwg.task.Task): org.mwg.task.Task;
                static math(expression: string): org.mwg.task.Task;
                static action(name: string, params: string): org.mwg.task.Task;
                static remove(relationName: string, variableNameToRemove: string): org.mwg.task.Task;
                static add(relationName: string, variableNameToAdd: string): org.mwg.task.Task;
                static removeProperty(propertyName: string): org.mwg.task.Task;
                static newNode(): org.mwg.task.Task;
                static newTypedNode(nodeType: string): org.mwg.task.Task;
                static save(): org.mwg.task.Task;
                static ifThen(cond: org.mwg.task.TaskFunctionConditional, then: org.mwg.task.Task): org.mwg.task.Task;
                static split(splitPattern: string): org.mwg.task.Task;
                static lookup(world: string, time: string, id: string): org.mwg.task.Task;
            }
            interface Task {
                setWorld(template: string): org.mwg.task.Task;
                setTime(template: string): org.mwg.task.Task;
                asVar(variableName: string): org.mwg.task.Task;
                fromVar(variableName: string): org.mwg.task.Task;
                setVar(variableName: string, inputValue: any): org.mwg.task.Task;
                inject(inputValue: any): org.mwg.task.Task;
                fromIndex(indexName: string, query: string): org.mwg.task.Task;
                fromIndexAll(indexName: string): org.mwg.task.Task;
                selectWith(name: string, pattern: string): org.mwg.task.Task;
                selectWithout(name: string, pattern: string): org.mwg.task.Task;
                select(filterFunction: org.mwg.task.TaskFunctionSelect): org.mwg.task.Task;
                selectObject(filterFunction: org.mwg.task.TaskFunctionSelectObject): org.mwg.task.Task;
                selectWhere(subTask: org.mwg.task.Task): org.mwg.task.Task;
                traverse(relationName: string): org.mwg.task.Task;
                get(name: string): org.mwg.task.Task;
                traverseOrKeep(relationName: string): org.mwg.task.Task;
                traverseIndex(indexName: string, query: string): org.mwg.task.Task;
                traverseIndexAll(indexName: string): org.mwg.task.Task;
                map(mapFunction: org.mwg.task.TaskFunctionMap): org.mwg.task.Task;
                flatMap(flatMapFunction: org.mwg.task.TaskFunctionFlatMap): org.mwg.task.Task;
                group(groupFunction: org.mwg.task.TaskFunctionGroup): org.mwg.task.Task;
                groupWhere(groupSubTask: org.mwg.task.Task): org.mwg.task.Task;
                foreach(subTask: org.mwg.task.Task): org.mwg.task.Task;
                foreachPar(subTask: org.mwg.task.Task): org.mwg.task.Task;
                executeSubTask(subTask: org.mwg.task.Task): org.mwg.task.Task;
                ifThen(cond: org.mwg.task.TaskFunctionConditional, then: org.mwg.task.Task): org.mwg.task.Task;
                whileDo(cond: org.mwg.task.TaskFunctionConditional, then: org.mwg.task.Task): org.mwg.task.Task;
                then(action: org.mwg.task.Action): org.mwg.task.Task;
                save(): org.mwg.task.Task;
                newNode(): org.mwg.task.Task;
                newTypedNode(typeNode: string): org.mwg.task.Task;
                setProperty(propertyName: string, propertyType: number, variableNameToSet: string): org.mwg.task.Task;
                removeProperty(propertyName: string): org.mwg.task.Task;
                add(relationName: string, variableNameToAdd: string): org.mwg.task.Task;
                remove(relationName: string, variableNameToRemove: string): org.mwg.task.Task;
                parse(flat: string): org.mwg.task.Task;
                action(name: string, params: string): org.mwg.task.Task;
                split(splitPattern: string): org.mwg.task.Task;
                lookup(world: string, time: string, id: string): org.mwg.task.Task;
                math(expression: string): org.mwg.task.Task;
                repeat(repetition: number, subTask: org.mwg.task.Task): org.mwg.task.Task;
                repeatPar(repetition: number, subTask: org.mwg.task.Task): org.mwg.task.Task;
                print(name: string): org.mwg.task.Task;
                execute(graph: org.mwg.Graph, result: org.mwg.Callback<any>): void;
                executeWith(graph: org.mwg.Graph, variables: java.util.Map<string, any>, initialResult: any, isVerbose: boolean, result: org.mwg.Callback<any>): void;
                executeFrom(parent: org.mwg.task.TaskContext, initialResult: any, result: org.mwg.Callback<any>): void;
                executeFromPar(parent: org.mwg.task.TaskContext, initialResult: any, result: org.mwg.Callback<any>): void;
            }
            interface TaskAction {
                eval(context: org.mwg.task.TaskContext): void;
            }
            interface TaskActionFactory {
                (params: string[]): org.mwg.task.TaskAction;
            }
            interface TaskContext {
                graph(): org.mwg.Graph;
                world(): number;
                setWorld(world: number): void;
                time(): number;
                setTime(time: number): void;
                variable(name: string): any;
                setVariable(name: string, value: any): void;
                addToVariable(name: string, value: any): void;
                variables(): java.util.Map<string, any>;
                result(): any;
                resultAsObjectArray(): any[];
                resultAsString(): string;
                resultAsStringArray(): string[];
                resultAsNode(): org.mwg.Node;
                resultAsNodeArray(): org.mwg.Node[];
                setUnsafeResult(actionResult: any): void;
                setResult(actionResult: any): void;
                cleanObj(o: any): void;
                template(input: string): string;
                isVerbose(): boolean;
                ident(): number;
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
                (node: org.mwg.Node, context: org.mwg.task.TaskContext): boolean;
            }
            interface TaskFunctionSelectObject {
                (object: any): boolean;
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
            static LONG_TO_LONG_MAP: number;
            static LONG_TO_LONG_ARRAY_MAP: number;
            static STRING_TO_LONG_MAP: number;
            static RELATION: number;
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
                newGraph(p_storage: org.mwg.plugin.Storage, p_readOnly: boolean, p_scheduler: org.mwg.plugin.Scheduler, p_plugins: org.mwg.plugin.Plugin[], p_usingGC: boolean, p_usingOffHeapMemory: boolean, p_memorySize: number, p_autoSaveSize: number): org.mwg.Graph;
                newTask(): org.mwg.task.Task;
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
                private _nodeTypes;
                private _taskActions;
                offHeapBuffer: boolean;
                private _prefix;
                private _nodeKeyCalculator;
                private _worldKeyCalculator;
                private _isConnected;
                private _lock;
                private _plugins;
                constructor(p_storage: org.mwg.plugin.Storage, p_space: org.mwg.plugin.ChunkSpace, p_scheduler: org.mwg.plugin.Scheduler, p_resolver: org.mwg.plugin.Resolver, p_plugins: org.mwg.plugin.Plugin[]);
                fork(world: number): number;
                newNode(world: number, time: number): org.mwg.Node;
                newTypedNode(world: number, time: number, nodeType: string): org.mwg.Node;
                cloneNode(origin: org.mwg.Node): org.mwg.Node;
                factoryByCode(code: number): org.mwg.plugin.NodeFactory;
                taskAction(taskActionName: string): org.mwg.task.TaskActionFactory;
                lookup<A extends org.mwg.Node>(world: number, time: number, id: number, callback: org.mwg.Callback<A>): void;
                save(callback: org.mwg.Callback<boolean>): void;
                connect(callback: org.mwg.Callback<boolean>): void;
                disconnect(callback: org.mwg.Callback<any>): void;
                newBuffer(): org.mwg.struct.Buffer;
                newQuery(): org.mwg.Query;
                private saveDirtyList(dirtyIterator, callback);
                index(indexName: string, toIndexNode: org.mwg.Node, flatKeyAttributes: string, callback: org.mwg.Callback<boolean>): void;
                unindex(indexName: string, nodeToUnindex: org.mwg.Node, flatKeyAttributes: string, callback: org.mwg.Callback<boolean>): void;
                indexes(world: number, time: number, callback: org.mwg.Callback<string[]>): void;
                find(world: number, time: number, indexName: string, query: string, callback: org.mwg.Callback<org.mwg.Node[]>): void;
                findByQuery(query: org.mwg.Query, callback: org.mwg.Callback<org.mwg.Node[]>): void;
                findAll(world: number, time: number, indexName: string, callback: org.mwg.Callback<org.mwg.Node[]>): void;
                getIndexNode(world: number, time: number, indexName: string, callback: org.mwg.Callback<org.mwg.Node>): void;
                private getIndexOrCreate(world, time, indexName, callback, createIfNull);
                newCounter(expectedCountCalls: number): org.mwg.DeferCounter;
                newSyncCounter(expectedCountCalls: number): org.mwg.DeferCounterSync;
                resolver(): org.mwg.plugin.Resolver;
                scheduler(): org.mwg.plugin.Scheduler;
                space(): org.mwg.plugin.ChunkSpace;
                storage(): org.mwg.plugin.Storage;
                freeNodes(nodes: org.mwg.Node[]): void;
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
                parse(flatQuery: string): org.mwg.Query;
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
                typeName(node: org.mwg.Node): string;
                typeCode(node: org.mwg.Node): number;
                markNodeAndGetType(node: org.mwg.Node): number;
                initNode(node: org.mwg.Node, codeType: number): void;
                initWorld(parentWorld: number, childWorld: number): void;
                freeNode(node: org.mwg.Node): void;
                lookup<A extends org.mwg.Node>(world: number, time: number, id: number, callback: org.mwg.Callback<A>): void;
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
                class ActionAdd implements org.mwg.task.TaskAction {
                    private _relationName;
                    private _variableNameToAdd;
                    constructor(relationName: string, variableNameToAdd: string);
                    eval(context: org.mwg.task.TaskContext): void;
                    private addFromArray(objs, relName, toRemove);
                    toString(): string;
                }
                class ActionAsVar implements org.mwg.task.TaskAction {
                    private _name;
                    constructor(p_name: string);
                    eval(context: org.mwg.task.TaskContext): void;
                    toString(): string;
                }
                class ActionForeach implements org.mwg.task.TaskAction {
                    private _subTask;
                    constructor(p_subTask: org.mwg.task.Task);
                    eval(context: org.mwg.task.TaskContext): void;
                    toString(): string;
                }
                class ActionForeachPar implements org.mwg.task.TaskAction {
                    private _subTask;
                    constructor(p_subTask: org.mwg.task.Task);
                    eval(context: org.mwg.task.TaskContext): void;
                    toString(): string;
                }
                class ActionFromIndex implements org.mwg.task.TaskAction {
                    private _indexName;
                    private _query;
                    constructor(p_indexName: string, p_query: string);
                    eval(context: org.mwg.task.TaskContext): void;
                    toString(): string;
                }
                class ActionFromIndexAll implements org.mwg.task.TaskAction {
                    private _indexName;
                    constructor(p_indexName: string);
                    eval(context: org.mwg.task.TaskContext): void;
                    toString(): string;
                }
                class ActionFromVar implements org.mwg.task.TaskAction {
                    private _name;
                    constructor(p_name: string);
                    eval(context: org.mwg.task.TaskContext): void;
                    toString(): string;
                }
                class ActionGet implements org.mwg.task.TaskAction {
                    private _name;
                    constructor(p_name: string);
                    eval(context: org.mwg.task.TaskContext): void;
                    private collectArray(current, toLoad, leafs, flatName);
                    toString(): string;
                }
                class ActionIfThen implements org.mwg.task.TaskAction {
                    private _condition;
                    private _action;
                    constructor(cond: org.mwg.task.TaskFunctionConditional, action: org.mwg.task.Task);
                    eval(context: org.mwg.task.TaskContext): void;
                    toString(): string;
                }
                class ActionInject implements org.mwg.task.TaskAction {
                    private _value;
                    constructor(value: any);
                    eval(context: org.mwg.task.TaskContext): void;
                    toString(): string;
                }
                class ActionLookup implements org.mwg.task.TaskAction {
                    private _world;
                    private _time;
                    private _id;
                    constructor(p_world: string, p_time: string, p_id: string);
                    eval(context: org.mwg.task.TaskContext): void;
                    private parse(flat);
                    toString(): string;
                }
                class ActionMap implements org.mwg.task.TaskAction {
                    private _map;
                    constructor(p_map: org.mwg.task.TaskFunctionMap);
                    eval(context: org.mwg.task.TaskContext): void;
                    private filterArray(current);
                    toString(): string;
                }
                class ActionMath implements org.mwg.task.TaskAction {
                    _engine: org.mwg.core.task.math.MathExpressionEngine;
                    _expression: string;
                    constructor(mathExpression: string);
                    eval(context: org.mwg.task.TaskContext): void;
                    arrayEval(objs: any[], result: java.util.List<number>, context: org.mwg.task.TaskContext): void;
                    toString(): string;
                }
                class ActionNewNode implements org.mwg.task.TaskAction {
                    private typeNode;
                    constructor(typeNode: string);
                    eval(context: org.mwg.task.TaskContext): void;
                    toString(): string;
                }
                class ActionPlugin implements org.mwg.task.TaskAction {
                    private _actionName;
                    private _flatParams;
                    private initilized;
                    private subAction;
                    constructor(actionName: string, flatParams: string);
                    eval(context: org.mwg.task.TaskContext): void;
                    toString(): string;
                }
                class ActionPrint implements org.mwg.task.TaskAction {
                    private _name;
                    constructor(p_name: string);
                    eval(context: org.mwg.task.TaskContext): void;
                    toString(): string;
                }
                class ActionRemove implements org.mwg.task.TaskAction {
                    private _relationName;
                    private _variableNameToRemove;
                    constructor(relationName: string, variableNameToRemove: string);
                    eval(context: org.mwg.task.TaskContext): void;
                    private removeFromArray(objs, relName, toRemove);
                    toString(): string;
                }
                class ActionRemoveProperty implements org.mwg.task.TaskAction {
                    private _propertyName;
                    constructor(propertyName: string);
                    eval(context: org.mwg.task.TaskContext): void;
                    private removePropertyFromArray(objs, templatedName);
                    toString(): string;
                }
                class ActionRepeat implements org.mwg.task.TaskAction {
                    private _subTask;
                    private _iteration;
                    constructor(p_iteration: number, p_subTask: org.mwg.task.Task);
                    eval(context: org.mwg.task.TaskContext): void;
                    toString(): string;
                }
                class ActionRepeatPar implements org.mwg.task.TaskAction {
                    private _subTask;
                    private _iteration;
                    constructor(p_iteration: number, p_subTask: org.mwg.task.Task);
                    eval(context: org.mwg.task.TaskContext): void;
                    toString(): string;
                }
                class ActionSave implements org.mwg.task.TaskAction {
                    eval(context: org.mwg.task.TaskContext): void;
                    toString(): string;
                }
                class ActionSelect implements org.mwg.task.TaskAction {
                    private _filter;
                    constructor(p_filter: org.mwg.task.TaskFunctionSelect);
                    eval(context: org.mwg.task.TaskContext): void;
                    private filterArray(current, context);
                    toString(): string;
                }
                class ActionSelectObject implements org.mwg.task.TaskAction {
                    private _filter;
                    constructor(filterFunction: org.mwg.task.TaskFunctionSelectObject);
                    eval(context: org.mwg.task.TaskContext): void;
                    private filter(current, context);
                    toString(): string;
                }
                class ActionSetProperty implements org.mwg.task.TaskAction {
                    private _relationName;
                    private _variableNameToSet;
                    private _propertyType;
                    constructor(relationName: string, propertyType: number, variableNameToSet: string);
                    eval(context: org.mwg.task.TaskContext): void;
                    private parseInt(payload);
                    private parseLong(payload);
                    private setFromArray(objs, relName, toSet);
                    toString(): string;
                }
                class ActionSetVar implements org.mwg.task.TaskAction {
                    private _name;
                    private _value;
                    constructor(name: string, value: any);
                    eval(context: org.mwg.task.TaskContext): void;
                    toString(): string;
                }
                class ActionSplit implements org.mwg.task.TaskAction {
                    private _splitPattern;
                    constructor(p_splitPattern: string);
                    eval(context: org.mwg.task.TaskContext): void;
                    toString(): string;
                }
                class ActionTime implements org.mwg.task.TaskAction {
                    private _varName;
                    constructor(p_varName: string);
                    eval(context: org.mwg.task.TaskContext): void;
                    private parse(flat);
                    toString(): string;
                }
                class ActionTraverse implements org.mwg.task.TaskAction {
                    private _name;
                    constructor(p_name: string);
                    eval(context: org.mwg.task.TaskContext): void;
                    private collectArray(current, toLoad, flatName);
                    toString(): string;
                }
                class ActionTraverseIndex implements org.mwg.task.TaskAction {
                    private _indexName;
                    private _query;
                    constructor(indexName: string, query: string);
                    eval(context: org.mwg.task.TaskContext): void;
                    private getNodes(previousResult);
                    private countNbNodeToLoad(nodes, flatIndexName);
                    toString(): string;
                }
                class ActionTraverseOrKeep implements org.mwg.task.TaskAction {
                    private _name;
                    constructor(p_name: string);
                    eval(context: org.mwg.task.TaskContext): void;
                    private collectArray(current, toLoad, flatName);
                    toString(): string;
                }
                class ActionTrigger implements org.mwg.task.TaskAction {
                    private _subTask;
                    constructor(p_subTask: org.mwg.task.Task);
                    eval(context: org.mwg.task.TaskContext): void;
                    toString(): string;
                }
                class ActionWhileDo implements org.mwg.task.TaskAction {
                    private _cond;
                    private _then;
                    constructor(p_cond: org.mwg.task.TaskFunctionConditional, p_then: org.mwg.task.Task);
                    eval(context: org.mwg.task.TaskContext): void;
                }
                class ActionWith extends org.mwg.core.task.ActionSelect {
                    constructor(name: string, stringPattern: string);
                    toString(): string;
                }
                class ActionWithout extends org.mwg.core.task.ActionSelect {
                    constructor(name: string, stringPattern: string);
                    toString(): string;
                }
                class ActionWorld implements org.mwg.task.TaskAction {
                    private _varName;
                    constructor(p_varName: string);
                    eval(context: org.mwg.task.TaskContext): void;
                    private parse(flat);
                    toString(): string;
                }
                class ActionWrapper implements org.mwg.task.TaskAction {
                    private _wrapped;
                    constructor(p_wrapped: org.mwg.task.Action);
                    eval(context: org.mwg.task.TaskContext): void;
                    toString(): string;
                }
                class CoreTask implements org.mwg.task.Task {
                    private _actions;
                    private _actionCursor;
                    private addAction(task);
                    setWorld(template: string): org.mwg.task.Task;
                    setTime(template: string): org.mwg.task.Task;
                    fromIndex(indexName: string, query: string): org.mwg.task.Task;
                    fromIndexAll(indexName: string): org.mwg.task.Task;
                    selectWith(name: string, pattern: string): org.mwg.task.Task;
                    selectWithout(name: string, pattern: string): org.mwg.task.Task;
                    asVar(variableName: string): org.mwg.task.Task;
                    fromVar(variableName: string): org.mwg.task.Task;
                    setVar(variableName: string, inputValue: any): org.mwg.task.Task;
                    select(filter: org.mwg.task.TaskFunctionSelect): org.mwg.task.Task;
                    selectObject(filterFunction: org.mwg.task.TaskFunctionSelectObject): org.mwg.task.Task;
                    selectWhere(subTask: org.mwg.task.Task): org.mwg.task.Task;
                    get(name: string): org.mwg.task.Task;
                    traverse(relationName: string): org.mwg.task.Task;
                    traverseOrKeep(relationName: string): org.mwg.task.Task;
                    traverseIndex(indexName: string, query: string): org.mwg.task.Task;
                    traverseIndexAll(indexName: string): org.mwg.task.Task;
                    map(mapFunction: org.mwg.task.TaskFunctionMap): org.mwg.task.Task;
                    flatMap(flatMapFunction: org.mwg.task.TaskFunctionFlatMap): org.mwg.task.Task;
                    group(groupFunction: org.mwg.task.TaskFunctionGroup): org.mwg.task.Task;
                    groupWhere(groupSubTask: org.mwg.task.Task): org.mwg.task.Task;
                    inject(inputValue: any): org.mwg.task.Task;
                    executeSubTask(subTask: org.mwg.task.Task): org.mwg.task.Task;
                    ifThen(cond: org.mwg.task.TaskFunctionConditional, then: org.mwg.task.Task): org.mwg.task.Task;
                    whileDo(cond: org.mwg.task.TaskFunctionConditional, then: org.mwg.task.Task): org.mwg.task.Task;
                    then(p_action: org.mwg.task.Action): org.mwg.task.Task;
                    foreach(subTask: org.mwg.task.Task): org.mwg.task.Task;
                    foreachPar(subTask: org.mwg.task.Task): org.mwg.task.Task;
                    save(): org.mwg.task.Task;
                    lookup(world: string, time: string, id: string): org.mwg.task.Task;
                    executeWith(graph: org.mwg.Graph, variables: java.util.Map<string, any>, initialResult: any, isVerbose: boolean, result: org.mwg.Callback<any>): void;
                    executeFrom(parent: org.mwg.task.TaskContext, initialResult: any, result: org.mwg.Callback<any>): void;
                    executeFromPar(parent: org.mwg.task.TaskContext, initialResult: any, result: org.mwg.Callback<any>): void;
                    execute(graph: org.mwg.Graph, result: org.mwg.Callback<any>): void;
                    action(name: string, flatParams: string): org.mwg.task.Task;
                    parse(flat: string): org.mwg.task.Task;
                    static protect(graph: org.mwg.Graph, input: any): any;
                    private static protectIterable(input);
                    newNode(): org.mwg.task.Task;
                    newTypedNode(typeNode: string): org.mwg.task.Task;
                    setProperty(propertyName: string, propertyType: number, variableNameToSet: string): org.mwg.task.Task;
                    removeProperty(propertyName: string): org.mwg.task.Task;
                    add(relationName: string, variableNameToAdd: string): org.mwg.task.Task;
                    remove(relationName: string, variableNameToRemove: string): org.mwg.task.Task;
                    math(expression: string): org.mwg.task.Task;
                    split(splitPattern: string): org.mwg.task.Task;
                    repeat(repetition: number, subTask: org.mwg.task.Task): org.mwg.task.Task;
                    repeatPar(repetition: number, subTask: org.mwg.task.Task): org.mwg.task.Task;
                    print(name: string): org.mwg.task.Task;
                    static fillDefault(registry: java.util.Map<string, org.mwg.task.TaskActionFactory>): void;
                }
                class CoreTaskContext implements org.mwg.task.TaskContext {
                    private _variables;
                    private shouldFreeVar;
                    private _graph;
                    private _actions;
                    private _actionCursor;
                    private _currentTaskId;
                    private _callback;
                    private verbose;
                    private _ident;
                    private _result;
                    private _world;
                    private _time;
                    constructor(p_variables: java.util.Map<string, any>, initial: any, p_graph: org.mwg.Graph, p_actions: org.mwg.task.TaskAction[], p_actionCursor: number, isVerbose: boolean, p_ident: number, p_callback: org.mwg.Callback<any>);
                    ident(): number;
                    graph(): org.mwg.Graph;
                    world(): number;
                    setWorld(p_world: number): void;
                    time(): number;
                    setTime(p_time: number): void;
                    variable(name: string): any;
                    setVariable(name: string, value: any): void;
                    addToVariable(name: string, value: any): void;
                    variables(): java.util.Map<string, any>;
                    result(): any;
                    resultAsString(): string;
                    resultAsStringArray(): string[];
                    resultAsNode(): org.mwg.Node;
                    resultAsNodeArray(): org.mwg.Node[];
                    resultAsObjectArray(): any[];
                    setUnsafeResult(actionResult: any): void;
                    setResult(actionResult: any): void;
                    private internal_setResult(actionResult, safe);
                    private printDebug(t);
                    executeFirst(graph: org.mwg.Graph): void;
                    cleanObj(o: any): void;
                    template(input: string): string;
                    private parseInt(s);
                    isVerbose(): boolean;
                }
                module math {
                    class CoreMathExpressionEngine implements org.mwg.core.task.math.MathExpressionEngine {
                        static decimalSeparator: string;
                        static minusSign: string;
                        private _cacheAST;
                        constructor(expression: string);
                        static parse(p_expression: string): org.mwg.core.task.math.MathExpressionEngine;
                        static isNumber(st: string): boolean;
                        static isDigit(c: string): boolean;
                        static isLetter(c: string): boolean;
                        static isWhitespace(c: string): boolean;
                        private shuntingYard(expression);
                        eval(context: org.mwg.Node, taskContext: org.mwg.task.TaskContext, variables: java.util.Map<string, number>): number;
                        private buildAST(rpn);
                        private parseDouble(val);
                        private parseInt(val);
                    }
                    class MathDoubleToken implements org.mwg.core.task.math.MathToken {
                        private _content;
                        constructor(_content: number);
                        type(): number;
                        content(): number;
                    }
                    class MathEntities {
                        private static INSTANCE;
                        operators: java.util.HashMap<string, org.mwg.core.task.math.MathOperation>;
                        functions: java.util.HashMap<string, org.mwg.core.task.math.MathFunction>;
                        static getINSTANCE(): org.mwg.core.task.math.MathEntities;
                        constructor();
                    }
                    interface MathExpressionEngine {
                        eval(context: org.mwg.Node, taskContext: org.mwg.task.TaskContext, variables: java.util.Map<string, number>): number;
                    }
                    class MathExpressionTokenizer {
                        private pos;
                        private input;
                        private previousToken;
                        constructor(input: string);
                        hasNext(): boolean;
                        private peekNextChar();
                        next(): string;
                        getPos(): number;
                    }
                    class MathFreeToken implements org.mwg.core.task.math.MathToken {
                        private _content;
                        constructor(content: string);
                        content(): string;
                        type(): number;
                    }
                    class MathFunction implements org.mwg.core.task.math.MathToken {
                        private name;
                        private numParams;
                        constructor(name: string, numParams: number);
                        getName(): string;
                        getNumParams(): number;
                        eval(p: Float64Array): number;
                        private date_to_seconds(value);
                        private date_to_minutes(value);
                        private date_to_hours(value);
                        private date_to_days(value);
                        private date_to_months(value);
                        private date_to_year(value);
                        private date_to_dayofweek(value);
                        type(): number;
                    }
                    class MathOperation implements org.mwg.core.task.math.MathToken {
                        private oper;
                        private precedence;
                        private leftAssoc;
                        constructor(oper: string, precedence: number, leftAssoc: boolean);
                        getOper(): string;
                        getPrecedence(): number;
                        isLeftAssoc(): boolean;
                        eval(v1: number, v2: number): number;
                        type(): number;
                    }
                    interface MathToken {
                        type(): number;
                    }
                }
            }
            module utility {
                abstract class AbstractBuffer implements org.mwg.struct.Buffer {
                    abstract slice(initPos: number, endPos: number): Int8Array;
                    iterator(): org.mwg.struct.BufferIterator;
                    abstract read(position: number): number;
                    abstract length(): number;
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
                    length(): number;
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
                    then(p_callback: org.mwg.plugin.Job): void;
                    wrap(): org.mwg.Callback<any>;
                }
                class CoreDeferCounterSync implements org.mwg.DeferCounterSync {
                    private _nb_down;
                    private _counter;
                    private _end;
                    private _result;
                    constructor(nb: number);
                    count(): void;
                    then(p_callback: org.mwg.plugin.Job): void;
                    wrap(): org.mwg.Callback<any>;
                    waitResult(): any;
                }
                class DataHasher {
                    private static byteTable;
                    private static HSTART;
                    private static HMULT;
                    static hash(data: string): number;
                    static hashBytes(data: Int8Array): number;
                }
                class GenericIterable extends org.mwg.plugin.AbstractIterable {
                    index: number;
                    array: boolean;
                    input: any;
                    max: number;
                    constructor(elem: any);
                    next(): any;
                    close(): void;
                    estimate(): number;
                    isArray(): boolean;
                }
                class HeapBuffer extends org.mwg.core.utility.AbstractBuffer {
                    private buffer;
                    private writeCursor;
                    slice(initPos: number, endPos: number): Int8Array;
                    write(b: number): void;
                    writeAll(bytes: Int8Array): void;
                    read(position: number): number;
                    data(): Int8Array;
                    length(): number;
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
