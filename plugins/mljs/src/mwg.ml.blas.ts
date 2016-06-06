declare var Module;

module org {
    export module mwg {
        export module ml {
            export module common {
                export module matrix {
                    export module blassolver {
                        export module blas {
                            export class JSBlas implements org.mwg.ml.common.matrix.blassolver.blas.Blas {
                                private netlib;

                                constructor() {
                                    this.netlib = Module;
                                }

                                dgemm(transA:org.mwg.ml.common.matrix.TransposeType, transB:org.mwg.ml.common.matrix.TransposeType, m:number, n:number, k:number, alpha:number, matA:Float64Array, offsetA:number, ldA:number, matB:Float64Array, offsetB:number, ldB:number, beta:number, matC:Float64Array, offsetC:number, ldC:number):void {
                                    var ptransa = this.netlib._malloc(1),
                                        ptransb = this.netlib._malloc(1),
                                        pm = this.netlib._malloc(4),
                                        pn = this.netlib._malloc(4),
                                        pk = this.netlib._malloc(4),
                                        palpha = this.netlib._malloc(8),
                                        pa = this.netlib._malloc(8 * matA.length),
                                        plda = this.netlib._malloc(4),
                                        pb = this.netlib._malloc(8 * matB.length),
                                        pldb = this.netlib._malloc(4),
                                        pbeta = this.netlib._malloc(8),
                                        pc = this.netlib._malloc(8 * matC.length),
                                        pldc = this.netlib._malloc(4);

                                    this.netlib.setValue(ptransa, org.mwg.ml.common.matrix.blassolver.blas.BlasHelper.transTypeToChar(transA).charCodeAt(0), 'i8');
                                    this.netlib.setValue(ptransb, org.mwg.ml.common.matrix.blassolver.blas.BlasHelper.transTypeToChar(transB).charCodeAt(0), 'i8');
                                    this.netlib.setValue(pm, m, 'i32');
                                    this.netlib.setValue(pn, n, 'i32');
                                    this.netlib.setValue(pk, k, 'i32');
                                    this.netlib.setValue(palpha, alpha, 'double');
                                    this.netlib.setValue(plda, ldA, 'i32');
                                    this.netlib.setValue(pldb, ldB, 'i32');
                                    this.netlib.setValue(pbeta, beta, 'double');
                                    this.netlib.setValue(pldc, ldC, 'i32');

                                    var a = new Float64Array(this.netlib.HEAPF64.buffer, pa, matA.length);
                                    var b = new Float64Array(this.netlib.HEAPF64.buffer, pb, matB.length);
                                    var c = new Float64Array(this.netlib.HEAPF64.buffer, pc, matC.length);
                                    a.set(matA);
                                    b.set(matB);
                                    c.set(matC);
                                    var dgemm = this.netlib.cwrap('f2c_dgemm', null, ['number', 'number', 'number', 'number', 'number', 'number', 'number', 'number', 'number', 'number', 'number', 'number', 'number']);
                                    dgemm(ptransa, ptransb, pm, pn, pk, palpha, pa, plda, pb, pldb, pbeta, pc, pldc);
                                    matC.set(c);

                                    this.netlib._free(ptransa);
                                    this.netlib._free(ptransb);
                                    this.netlib._free(pm);
                                    this.netlib._free(pn);
                                    this.netlib._free(pk);
                                    this.netlib._free(palpha);
                                    this.netlib._free(pa);
                                    this.netlib._free(plda);
                                    this.netlib._free(pb);
                                    this.netlib._free(pldb);
                                    this.netlib._free(pbeta);
                                    this.netlib._free(pc);
                                    this.netlib._free(pldc);
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