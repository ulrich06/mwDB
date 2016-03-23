package math;

import org.junit.Assert;
import org.junit.Test;
import org.mwdb.math.matrix.blas.KBlas;
import org.mwdb.math.matrix.blas.NetlibBlas;
import org.mwdb.math.matrix.solver.PolynomialFitBlas;

/**
 * Created by assaad on 23/03/16.
 */
public class PolynomialSolveTest {
    @Test
    public void polytest(){
        double eps=1e-7;
        double[] coef={5,-4,1,7};
        double[] t={0,1,2,3,4,5,6,7,8,9};
        double[] res=new double[t.length];
        KBlas blas = new NetlibBlas();

        for(int i=0;i<t.length;i++){
            res[i]= PolynomialFitBlas.extrapolate(t[i],coef);
        }


        PolynomialFitBlas pf = new PolynomialFitBlas(coef.length-1, blas);
        long timestart, timeend;

        timestart = System.nanoTime();
        pf.fit(t,res);
        double[] blasCoef=pf.getCoef();
        timeend = System.nanoTime();
        System.out.println("Polynomial solving done in: " + ((double) (timeend - timestart)) / 1000000 + " ms");



        for(int i=0;i<coef.length;i++){
            Assert.assertTrue(Math.abs(blasCoef[i]-coef[i])< eps);
        }
    }
}
