/// <reference path="mwg.d.ts" />
declare module org {
    module mwg {
        module ml {
            abstract class AbstractMLNode extends org.mwg.plugin.AbstractNode {
                static FROM_SEPARATOR: string;
                static FROM: string;
                constructor(p_world: number, p_time: number, p_id: number, p_graph: org.mwg.Graph, currentResolution: Float64Array);
                get(propertyName: string): any;
                setTrainingVector(vec: Float64Array): void;
                extractFeatures(callback: org.mwg.Callback<Float64Array>): void;
                parseDouble(payload: string): number;
            }
            module algorithm {
                interface ClassificationNode extends org.mwg.Node {
                    learn(expectedClass: number, callback: org.mwg.Callback<boolean>): void;
                    classify(callback: org.mwg.Callback<number>): void;
                }
                interface ClusteringNode {
                    learn(callback: org.mwg.Callback<boolean>): void;
                    inferCluster(callback: org.mwg.Callback<number>): void;
                }
                module profiling {
                    class GaussianSlotProfilingNode extends org.mwg.ml.AbstractMLNode implements org.mwg.ml.algorithm.ProfilingNode {
                        static NAME: string;
                        static SLOTS_NUMBER: string;
                        static SLOTS_NUMBER_DEF: number;
                        static PERIOD_SIZE: string;
                        static PERIOD_SIZE_DEF: number;
                        private static INTERNAL_FEATURES_NUMBER;
                        private static INTERNAL_TOTAL_KEY;
                        private static INTERNAL_MIN_KEY;
                        private static INTERNAL_MAX_KEY;
                        private static INTERNAL_SUM_KEY;
                        private static INTERNAL_SUMSQUARE_KEY;
                        learn(callback: org.mwg.Callback<boolean>): void;
                        learnArray(values: Float64Array): void;
                        predict(callback: org.mwg.Callback<Float64Array>): void;
                        setProperty(propertyName: string, propertyType: number, propertyValue: any): void;
                        get(attributeName: string): any;
                        constructor(p_world: number, p_time: number, p_id: number, p_graph: org.mwg.Graph, currentResolution: Float64Array);
                        static getIntTime(time: number, numOfSlot: number, periodSize: number): number;
                        private update(total, min, max, sum, sumSquare, values, slot, features, index, indexSquare);
                        getMin(): Float64Array;
                        getMax(): Float64Array;
                        getSum(): Float64Array;
                        getSumSquare(): Float64Array;
                        getTotal(): Int32Array;
                        getAvg(): Float64Array;
                    }
                    module GaussianSlotProfilingNode {
                        class Factory implements org.mwg.plugin.NodeFactory {
                            name(): string;
                            create(world: number, time: number, id: number, graph: org.mwg.Graph, initialResolution: Float64Array): org.mwg.Node;
                        }
                    }
                    interface ProgressReporter {
                        updateProgress(value: number): void;
                        isCancelled(): boolean;
                        updateGraphInfo(info: string): void;
                    }
                }
                interface ProfilingNode {
                    learn(callback: org.mwg.Callback<boolean>): void;
                    predict(callback: org.mwg.Callback<Float64Array>): void;
                }
                module regression {
                    class PolynomialNode extends org.mwg.ml.AbstractMLNode implements org.mwg.ml.algorithm.RegressionNode {
                        static NAME: string;
                        static PRECISION_KEY: string;
                        static PRECISION_DEF: number;
                        static FEATURES_KEY: string;
                        private static INTERNAL_WEIGHT_KEY;
                        private static INTERNAL_STEP_KEY;
                        private static INTERNAL_NB_PAST_KEY;
                        private static INTERNAL_LAST_TIME_KEY;
                        private static _maxDegree;
                        learn(value: number, callback: org.mwg.Callback<boolean>): void;
                        extrapolate(callback: org.mwg.Callback<number>): void;
                        constructor(p_world: number, p_time: number, p_id: number, p_graph: org.mwg.Graph, currentResolution: Float64Array);
                        setProperty(propertyName: string, propertyType: number, propertyValue: any): void;
                        getPrecision(): number;
                        getWeight(): Float64Array;
                        private maxErr(precision, degree);
                        private tempError(computedWeights, times, values);
                        getDegree(): number;
                        toString(): string;
                    }
                    module PolynomialNode {
                        class Factory implements org.mwg.plugin.NodeFactory {
                            name(): string;
                            create(world: number, time: number, id: number, graph: org.mwg.Graph, initialResolution: Float64Array): org.mwg.Node;
                        }
                    }
                }
                interface RegressionNode {
                    learn(output: number, callback: org.mwg.Callback<boolean>): void;
                    extrapolate(callback: org.mwg.Callback<number>): void;
                }
            }
            module common {
                module mathexp {
                    module impl {
                        class MathDoubleToken implements org.mwg.ml.common.mathexp.impl.MathToken {
                            private _content;
                            constructor(_content: number);
                            type(): number;
                            content(): number;
                        }
                        class MathEntities {
                            private static INSTANCE;
                            operators: java.util.HashMap<string, org.mwg.ml.common.mathexp.impl.MathOperation>;
                            functions: java.util.HashMap<string, org.mwg.ml.common.mathexp.impl.MathFunction>;
                            static getINSTANCE(): org.mwg.ml.common.mathexp.impl.MathEntities;
                            constructor();
                        }
                        class MathExpressionEngine implements org.mwg.ml.common.mathexp.MathExpressionEngine {
                            static decimalSeparator: string;
                            static minusSign: string;
                            private _cacheAST;
                            constructor(expression: string);
                            static parse(p_expression: string): org.mwg.ml.common.mathexp.MathExpressionEngine;
                            static isNumber(st: string): boolean;
                            static isDigit(c: string): boolean;
                            static isLetter(c: string): boolean;
                            static isWhitespace(c: string): boolean;
                            private shuntingYard(expression);
                            eval(context: org.mwg.Node, variables: java.util.Map<string, number>): number;
                            private buildAST(rpn);
                            private parseDouble(val);
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
                        class MathFreeToken implements org.mwg.ml.common.mathexp.impl.MathToken {
                            private _content;
                            constructor(content: string);
                            content(): string;
                            type(): number;
                        }
                        class MathFunction implements org.mwg.ml.common.mathexp.impl.MathToken {
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
                        class MathOperation implements org.mwg.ml.common.mathexp.impl.MathToken {
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
                        class PrimitiveHelper {
                            static equals(src: string, other: string): boolean;
                        }
                    }
                    interface MathExpressionEngine {
                        eval(context: org.mwg.Node, variables: java.util.Map<string, number>): number;
                    }
                }
                module matrix {
                    module blassolver {
                        module blas {
                            interface Blas {
                                dgemm(transA: org.mwg.ml.common.matrix.TransposeType, transB: org.mwg.ml.common.matrix.TransposeType, m: number, n: number, k: number, alpha: number, matA: Float64Array, offsetA: number, ldA: number, matB: Float64Array, offsetB: number, ldB: number, beta: number, matC: Float64Array, offsetC: number, ldC: number): void;
                                dgetrs(transA: org.mwg.ml.common.matrix.TransposeType, dim: number, nrhs: number, matA: Float64Array, offsetA: number, ldA: number, ipiv: Int32Array, offsetIpiv: number, matB: Float64Array, offsetB: number, ldB: number, info: Int32Array): void;
                                dgetri(dim: number, matA: Float64Array, offsetA: number, ldA: number, ipiv: Int32Array, offsetIpiv: number, work: Float64Array, offsetWork: number, ldWork: number, info: Int32Array): void;
                                dgetrf(rows: number, columns: number, matA: Float64Array, offsetA: number, ldA: number, ipiv: Int32Array, offsetIpiv: number, info: Int32Array): void;
                                dorgqr(m: number, n: number, k: number, matA: Float64Array, offsetA: number, ldA: number, taw: Float64Array, offsetTaw: number, work: Float64Array, offsetWork: number, lWork: number, info: Int32Array): void;
                                dgeqrf(m: number, n: number, matA: Float64Array, offsetA: number, ldA: number, taw: Float64Array, offsetTaw: number, work: Float64Array, offsetwork: number, lWork: number, info: Int32Array): void;
                                dgesdd(jobz: string, m: number, n: number, data: Float64Array, lda: number, s: Float64Array, u: Float64Array, ldu: number, vt: Float64Array, ldvt: number, work: Float64Array, length: number, iwork: Int32Array, info: Int32Array): void;
                                connect(): void;
                                disconnect(): void;
                            }
                        }
                    }
                    module jamasolver {
                        class JamaMatrixEngine implements org.mwg.ml.common.matrix.MatrixEngine {
                            multiplyTransposeAlphaBeta(transA: org.mwg.ml.common.matrix.TransposeType, alpha: number, matA: org.mwg.ml.common.matrix.Matrix, transB: org.mwg.ml.common.matrix.TransposeType, beta: number, matB: org.mwg.ml.common.matrix.Matrix): org.mwg.ml.common.matrix.Matrix;
                            invert(mat: org.mwg.ml.common.matrix.Matrix, invertInPlace: boolean): org.mwg.ml.common.matrix.Matrix;
                            pinv(mat: org.mwg.ml.common.matrix.Matrix, invertInPlace: boolean): org.mwg.ml.common.matrix.Matrix;
                            solveLU(matA: org.mwg.ml.common.matrix.Matrix, matB: org.mwg.ml.common.matrix.Matrix, workInPlace: boolean, transB: org.mwg.ml.common.matrix.TransposeType): org.mwg.ml.common.matrix.Matrix;
                            solveQR(matA: org.mwg.ml.common.matrix.Matrix, matB: org.mwg.ml.common.matrix.Matrix, workInPlace: boolean, transB: org.mwg.ml.common.matrix.TransposeType): org.mwg.ml.common.matrix.Matrix;
                            decomposeSVD(matA: org.mwg.ml.common.matrix.Matrix, workInPlace: boolean): org.mwg.ml.common.matrix.SVDDecompose;
                            static solve(A: org.mwg.ml.common.matrix.Matrix, B: org.mwg.ml.common.matrix.Matrix): org.mwg.ml.common.matrix.Matrix;
                        }
                        class LU {
                            private LU;
                            private m;
                            private n;
                            private pivsign;
                            private piv;
                            constructor(A: org.mwg.ml.common.matrix.Matrix);
                            isNonsingular(): boolean;
                            getL(): org.mwg.ml.common.matrix.Matrix;
                            getU(): org.mwg.ml.common.matrix.Matrix;
                            getPivot(): Int32Array;
                            getDoublePivot(): Float64Array;
                            det(): number;
                            solve(B: org.mwg.ml.common.matrix.Matrix): org.mwg.ml.common.matrix.Matrix;
                            private getMatrix(A, r, j0, j1);
                        }
                        class QR {
                            private QR;
                            private m;
                            private n;
                            private Rdiag;
                            constructor(A: org.mwg.ml.common.matrix.Matrix);
                            isFullRank(): boolean;
                            getH(): org.mwg.ml.common.matrix.Matrix;
                            getR(): org.mwg.ml.common.matrix.Matrix;
                            getQ(): org.mwg.ml.common.matrix.Matrix;
                            solve(B: org.mwg.ml.common.matrix.Matrix): org.mwg.ml.common.matrix.Matrix;
                            private static getMatrix(B, i0, i1, j0, j1);
                        }
                        class SVD implements org.mwg.ml.common.matrix.SVDDecompose {
                            private U;
                            private V;
                            private s;
                            private m;
                            private n;
                            private static serialVersionUID;
                            constructor(Arg: org.mwg.ml.common.matrix.Matrix);
                            factor(A: org.mwg.ml.common.matrix.Matrix, workInPlace: boolean): org.mwg.ml.common.matrix.jamasolver.SVD;
                            getU(): org.mwg.ml.common.matrix.Matrix;
                            getVt(): org.mwg.ml.common.matrix.Matrix;
                            getV(): org.mwg.ml.common.matrix.Matrix;
                            getSingularValues(): Float64Array;
                            getSMatrix(): org.mwg.ml.common.matrix.Matrix;
                            getS(): Float64Array;
                            norm2(): number;
                            cond(): number;
                            rank(): number;
                        }
                        class Utils {
                            static hypot(a: number, b: number): number;
                        }
                    }
                    class Matrix {
                        private _data;
                        private _nbRows;
                        private _nbColumns;
                        private static _defaultEngine;
                        constructor(backend: Float64Array, p_nbRows: number, p_nbColumns: number);
                        static compare(a: Float64Array, b: Float64Array, eps: number): boolean;
                        static compareArray(a: Float64Array[], b: Float64Array[], eps: number): boolean;
                        data(): Float64Array;
                        exportRowMatrix(): Float64Array;
                        importRowMatrix(rowdata: Float64Array, rows: number, columns: number): org.mwg.ml.common.matrix.Matrix;
                        setData(data: Float64Array): void;
                        rows(): number;
                        columns(): number;
                        get(rowIndex: number, columnIndex: number): number;
                        set(rowIndex: number, columnIndex: number, value: number): number;
                        add(rowIndex: number, columnIndex: number, value: number): number;
                        setAll(value: number): void;
                        getAtIndex(index: number): number;
                        setAtIndex(index: number, value: number): number;
                        addAtIndex(index: number, value: number): number;
                        clone(): org.mwg.ml.common.matrix.Matrix;
                        static defaultEngine(): org.mwg.ml.common.matrix.MatrixEngine;
                        static multiply(matA: org.mwg.ml.common.matrix.Matrix, matB: org.mwg.ml.common.matrix.Matrix): org.mwg.ml.common.matrix.Matrix;
                        static multiplyTransposeAlphaBeta(transA: org.mwg.ml.common.matrix.TransposeType, alpha: number, matA: org.mwg.ml.common.matrix.Matrix, transB: org.mwg.ml.common.matrix.TransposeType, beta: number, matB: org.mwg.ml.common.matrix.Matrix): org.mwg.ml.common.matrix.Matrix;
                        static invert(mat: org.mwg.ml.common.matrix.Matrix, invertInPlace: boolean): org.mwg.ml.common.matrix.Matrix;
                        static pinv(mat: org.mwg.ml.common.matrix.Matrix, invertInPlace: boolean): org.mwg.ml.common.matrix.Matrix;
                        static leadingDimension(matA: org.mwg.ml.common.matrix.Matrix): number;
                        static random(rows: number, columns: number, min: number, max: number): org.mwg.ml.common.matrix.Matrix;
                        static scale(alpha: number, matA: org.mwg.ml.common.matrix.Matrix): void;
                        static transpose(matA: org.mwg.ml.common.matrix.Matrix): org.mwg.ml.common.matrix.Matrix;
                        private static transposeSquare(matA, result);
                        private static transposeStandard(matA, result);
                        private static transposeBlock(matA, result);
                        saveToState(): Float64Array;
                        static loadFromState(o: any): org.mwg.ml.common.matrix.Matrix;
                        static createIdentity(rows: number, columns: number): org.mwg.ml.common.matrix.Matrix;
                        static compareMatrix(matA: org.mwg.ml.common.matrix.Matrix, matB: org.mwg.ml.common.matrix.Matrix): number;
                        static testDimensionsAB(transA: org.mwg.ml.common.matrix.TransposeType, transB: org.mwg.ml.common.matrix.TransposeType, matA: org.mwg.ml.common.matrix.Matrix, matB: org.mwg.ml.common.matrix.Matrix): boolean;
                        static identity(rows: number, columns: number): org.mwg.ml.common.matrix.Matrix;
                    }
                    interface MatrixEngine {
                        multiplyTransposeAlphaBeta(transA: org.mwg.ml.common.matrix.TransposeType, alpha: number, matA: org.mwg.ml.common.matrix.Matrix, transB: org.mwg.ml.common.matrix.TransposeType, beta: number, matB: org.mwg.ml.common.matrix.Matrix): org.mwg.ml.common.matrix.Matrix;
                        invert(mat: org.mwg.ml.common.matrix.Matrix, invertInPlace: boolean): org.mwg.ml.common.matrix.Matrix;
                        pinv(mat: org.mwg.ml.common.matrix.Matrix, invertInPlace: boolean): org.mwg.ml.common.matrix.Matrix;
                        solveLU(matA: org.mwg.ml.common.matrix.Matrix, matB: org.mwg.ml.common.matrix.Matrix, workInPlace: boolean, transB: org.mwg.ml.common.matrix.TransposeType): org.mwg.ml.common.matrix.Matrix;
                        solveQR(matA: org.mwg.ml.common.matrix.Matrix, matB: org.mwg.ml.common.matrix.Matrix, workInPlace: boolean, transB: org.mwg.ml.common.matrix.TransposeType): org.mwg.ml.common.matrix.Matrix;
                        decomposeSVD(matA: org.mwg.ml.common.matrix.Matrix, workInPlace: boolean): org.mwg.ml.common.matrix.SVDDecompose;
                    }
                    module operation {
                        class Gaussian1D {
                            static getCovariance(sum: number, sumSq: number, total: number): number;
                            static getDensity(sum: number, sumSq: number, total: number, feature: number): number;
                            static getDensityArray(sum: number, sumSq: number, total: number, feature: Float64Array): Float64Array;
                        }
                        class MultivariateNormalDistribution {
                            min: Float64Array;
                            max: Float64Array;
                            means: Float64Array;
                            covDiag: Float64Array;
                            inv: org.mwg.ml.common.matrix.Matrix;
                            covariance: org.mwg.ml.common.matrix.Matrix;
                            pinvsvd: org.mwg.ml.common.matrix.operation.PInvSVD;
                            rank: number;
                            det: number;
                            getMin(): Float64Array;
                            getMax(): Float64Array;
                            getAvg(): Float64Array;
                            getCovDiag(): Float64Array;
                            constructor(means: Float64Array, cov: org.mwg.ml.common.matrix.Matrix, allowSingular: boolean);
                            setMin(min: Float64Array): void;
                            setMax(max: Float64Array): void;
                            static getCovariance(sum: Float64Array, sumsquares: Float64Array, total: number): org.mwg.ml.common.matrix.Matrix;
                            static getDistribution(sum: Float64Array, sumsquares: Float64Array, total: number, allowSingular: boolean): org.mwg.ml.common.matrix.operation.MultivariateNormalDistribution;
                            density(features: Float64Array, normalizeOnAvg: boolean): number;
                            private getExponentTerm(features);
                            clone(avg: Float64Array): org.mwg.ml.common.matrix.operation.MultivariateNormalDistribution;
                        }
                        class PInvSVD {
                            private _svd;
                            private pinv;
                            private S;
                            private rank;
                            private det;
                            getRank(): number;
                            getDeterminant(): number;
                            constructor();
                            factor(A: org.mwg.ml.common.matrix.Matrix, invertInPlace: boolean): org.mwg.ml.common.matrix.operation.PInvSVD;
                            getSvd(): org.mwg.ml.common.matrix.SVDDecompose;
                            getInvDeterminant(): org.mwg.ml.common.matrix.Matrix;
                            getPInv(): org.mwg.ml.common.matrix.Matrix;
                        }
                        class PolynomialFit {
                            private coef;
                            private degree;
                            constructor(degree: number);
                            getCoef(): Float64Array;
                            fit(samplePoints: Float64Array, observations: Float64Array): void;
                            static extrapolate(time: number, weights: Float64Array): number;
                        }
                    }
                    interface SVDDecompose {
                        factor(A: org.mwg.ml.common.matrix.Matrix, workInPlace: boolean): org.mwg.ml.common.matrix.SVDDecompose;
                        getU(): org.mwg.ml.common.matrix.Matrix;
                        getVt(): org.mwg.ml.common.matrix.Matrix;
                        getS(): Float64Array;
                        getSMatrix(): org.mwg.ml.common.matrix.Matrix;
                    }
                    class TransposeType {
                        static NOTRANSPOSE: TransposeType;
                        static TRANSPOSE: TransposeType;
                        equals(other: any): boolean;
                        static _TransposeTypeVALUES: TransposeType[];
                        static values(): TransposeType[];
                    }
                }
                class NDimentionalArray {
                    constructor();
                    get(indices: Int32Array): number;
                    set(indices: Int32Array, value: number): void;
                    add(indices: Int32Array, value: number): void;
                }
            }
        }
    }
}
