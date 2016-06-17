package org.mwg.ml.common;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.Type;
import org.mwg.plugin.Enforcer;

/**
 * Created by assaad on 17/06/16.
 */
public class EnforcerTest {

    @Test
    public void enforce(){
        Enforcer enforcer=new Enforcer().asPositiveLong("plong").asPositiveInt("pint").asPositiveDouble("pdouble").asBool("bool").asIntWithin("int1-10",1,10);
        enforcer.check("plong", Type.LONG, 1);
        enforcer.check("pint", Type.INT, 1);
        enforcer.check("pdouble", Type.DOUBLE, 1);
        enforcer.check("int1-10", Type.INT, 1);

        boolean catched=false;
        try{
            enforcer.check("plong", Type.LONG, 0);
        }
        catch (Exception ex){
            catched=true;
        }
        Assert.assertTrue(catched);
        catched=false;

        try{
            enforcer.check("pint", Type.INT, 0);
        }
        catch (Exception ex){
            catched=true;
        }
        Assert.assertTrue(catched);
        catched=false;

        try{
            enforcer.check("pdouble", Type.DOUBLE, 0);
        }
        catch (Exception ex){
            catched=true;
        }
        Assert.assertTrue(catched);
        catched=false;

        try{
            enforcer.check("int1-10", Type.INT, 0);
        }
        catch (Exception ex){
            catched=true;
        }
        Assert.assertTrue(catched);

        catched=false;
        try{
            enforcer.check("int1-10", Type.INT, 100);
        }
        catch (Exception ex){
            catched=true;
        }
        Assert.assertTrue(catched);
    }
}
