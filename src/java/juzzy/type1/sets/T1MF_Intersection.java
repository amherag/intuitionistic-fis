/*  
 * Created on 10 October 2012, 15:57
 *
 * Author: Christian Wagner
 * Copyright 2012 Christian Wagner All Rights Reserved.
 */
package type1.sets;

import generic.Tuple;

/**
 *
 * @author Christian Wagner
 */
public class T1MF_Intersection extends T1MF_Prototype
{
    private T1MF_Interface setA, setB;
    
    public T1MF_Intersection(String name, T1MF_Interface setA, T1MF_Interface setB)
    {
        super("Intersection: "+setA.getName()+"_"+setB.getName());
        this.support = new Tuple(Math.max(setA.getSupport().getLeft(),setB.getSupport().getLeft()),
                Math.min(setA.getSupport().getRight(),setB.getSupport().getRight()));        
        this.setA = setA;
        this.setB = setB;
    }

    @Override
    public double getFS(double x) {
        return Math.min(setA.getFS(x), setB.getFS(x));
    }

    @Override
    public Tuple getAlphaCut(double alpha) 
    {
        throw new UnsupportedOperationException("Not supported yet.");        
    }

    @Override
    public double getPeak() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int compareTo(Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
