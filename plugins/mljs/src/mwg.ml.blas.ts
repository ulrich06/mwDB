/// <reference path="mwg.ml.d.ts" />

var emlapack;

module org {
    export module mwg {
        export module ml {
            export module common {
                export module matrix {
                    export module blassolver {
                        export module blas {
                            export class JSBlas implements org.mwg.ml.common.matrix.blassolver.blas.Blas {

                                dgemm(transA:org.mwg.ml.common.matrix.TransposeType, transB:org.mwg.ml.common.matrix.TransposeType, m:number, n:number, k:number, alpha:number, matA:Float64Array, offsetA:number, ldA:number, matB:Float64Array, offsetB:number, ldB:number, beta:number, matC:Float64Array, offsetC:number, ldC:number):void {
                                    var ptransa = emlapack._malloc(1),
                                        ptransb = emlapack._malloc(1),
                                        pm = emlapack._malloc(4),
                                        pn = emlapack._malloc(4),
                                        pk = emlapack._malloc(4),
                                        palpha = emlapack._malloc(8),
                                        pa = emlapack._malloc(8 * matA.length),
                                        plda = emlapack._malloc(4),
                                        pb = emlapack._malloc(8 * matB.length),
                                        pldb = emlapack._malloc(4),
                                        pbeta = emlapack._malloc(8),
                                        pc = emlapack._malloc(8 * matC.length),
                                        pldc = emlapack._malloc(4);

                                    emlapack.setValue(ptransa, org.mwg.ml.common.matrix.blassolver.blas.BlasHelper.transTypeToChar(transA).charCodeAt(0), 'i8');
                                    emlapack.setValue(ptransb, org.mwg.ml.common.matrix.blassolver.blas.BlasHelper.transTypeToChar(transB).charCodeAt(0), 'i8');
                                    emlapack.setValue(pm, m, 'i32');
                                    emlapack.setValue(pn, n, 'i32');
                                    emlapack.setValue(pk, k, 'i32');
                                    emlapack.setValue(palpha, alpha, 'double');
                                    emlapack.setValue(plda, ldA, 'i32');
                                    emlapack.setValue(pldb, ldB, 'i32');
                                    emlapack.setValue(pbeta, beta, 'double');
                                    emlapack.setValue(pldc, ldC, 'i32');

                                    var a = new Float64Array(emlapack.HEAPF64.buffer, pa, matA.length);
                                    var b = new Float64Array(emlapack.HEAPF64.buffer, pb, matB.length);
                                    var c = new Float64Array(emlapack.HEAPF64.buffer, pc, matC.length);
                                    a.set(matA);
                                    b.set(matB);
                                    c.set(matC);
                                    var dgemm = emlapack.cwrap('f2c_dgemm', null, ['number', 'number', 'number', 'number', 'number', 'number', 'number', 'number', 'number', 'number', 'number', 'number', 'number']);
                                    dgemm(ptransa, ptransb, pm, pn, pk, palpha, pa, plda, pb, pldb, pbeta, pc, pldc);
                                    matC.set(c);

                                }

                                dgetrs(transA:org.mwg.ml.common.matrix.TransposeType, dim:number, nrhs:number, matA:Float64Array, offsetA:number, ldA:number, ipiv:Int32Array, offsetIpiv:number, matB:Float64Array, offsetB:number, ldB:number, info:Int32Array):void {

                                }

                                dgetri(dim:number, matA:Float64Array, offsetA:number, ldA:number, ipiv:Int32Array, offsetIpiv:number, work:Float64Array, offsetWork:number, ldWork:number, info:Int32Array):void {

                                }

                                dgetrf(rows:number, columns:number, matA:Float64Array, offsetA:number, ldA:number, ipiv:Int32Array, offsetIpiv:number, info:Int32Array):void {

                                }

                                dorgqr(m:number, n:number, k:number, matA:Float64Array, offsetA:number, ldA:number, taw:Float64Array, offsetTaw:number, work:Float64Array, offsetWork:number, lWork:number, info:Int32Array):void {

                                }

                                dgeqrf(m:number, n:number, matA:Float64Array, offsetA:number, ldA:number, taw:Float64Array, offsetTaw:number, work:Float64Array, offsetwork:number, lWork:number, info:Int32Array):void {

                                }

                                dgesdd(jobz:string, m:number, n:number, data:Float64Array, lda:number, s:Float64Array, u:Float64Array, ldu:number, vt:Float64Array, ldvt:number, work:Float64Array, length:number, iwork:Int32Array, info:Int32Array):void {

                                }

                                connect():void {

                                }

                                disconnect():void {

                                }

                            }
                        }
                    }
                }
            }
        }
    }
}