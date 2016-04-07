package graph;

import java.lang.reflect.Array;

/**
 * Created by duke on 07/04/16.
 */
public class GenTest {

    public static void main(String[] args) {
        hello(new Toto(){});
    }

    public static <A extends Toto> A[] hello(A h) {
        //A[] array = (A[]) new Object[0];
       // A[] array = (A[]) Array.newInstance(h.getClass(),3);
        //return array;

        return (A[]) new Toto[0];

    }

    static class Toto {

    }

}
