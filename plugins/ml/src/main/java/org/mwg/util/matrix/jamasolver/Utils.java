package org.mwg.util.matrix.jamasolver;

import org.mwg.util.matrix.KMatrix;

public class Utils {

   /** sqrt(a^2 + b^2) without under/overflow. **/

   public static double hypot(double a, double b) {
      double r;
      if (Math.abs(a) > Math.abs(b)) {
         r = b/a;
         r = Math.abs(a)*Math.sqrt(1+r*r);
      } else if (b != 0) {
         r = a/b;
         r = Math.abs(b)*Math.sqrt(1+r*r);
      } else {
         r = 0.0;
      }
      return r;
   }

   public static double[][] convertToArray(KMatrix mat){
      double[][] res= new double[mat.rows()][mat.columns()];
      for(int i=0;i<mat.rows();i++){
         for(int j=0;j<mat.columns();j++){
            res[i][j]=mat.get(i,j);
         }
      }
      return res;
   }

   public static KMatrix convertToMatrix(double[][] matrix){

      KMatrix mat=new KMatrix(null,matrix.length,matrix[0].length);

      for(int i=0;i<mat.rows();i++){
         for(int j=0;j<mat.columns();j++){
            mat.set(i,j,matrix[i][j]);
         }
      }
      return mat;
   }

   public static KMatrix convertToMatrixArg(double[][] matrix, int m, int n) {
      KMatrix mat=new KMatrix(null,m,n);

      for(int i=0;i<mat.rows();i++){
         for(int j=0;j<mat.columns();j++){
            mat.set(i,j,matrix[i][j]);
         }
      }
      return mat;
   }
}
