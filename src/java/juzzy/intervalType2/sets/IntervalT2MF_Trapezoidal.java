/*
 * IntervalT2MF_Trapezoidal.java
 *
 * Created on 26 July 2008, 16:06
 *
 * Copyright 2008 Christian Wagner All Rights Reserved.
 */

package intervalType2.sets;

import generic.BadParameterException;
import type1.sets.T1MF_Trapezoidal;

/**
 * Class for Trapezoidal Interval Type-2 Fuzzy Sets
 * @author Christian Wagner
 */
public class IntervalT2MF_Trapezoidal extends IntervalT2MF_Prototype
{
    private final boolean DEBUG = false;
    
    public IntervalT2MF_Trapezoidal(String name, T1MF_Trapezoidal upper, T1MF_Trapezoidal lower)
    {
        super(name);
        if(upper.getA()>lower.getA() || upper.getB()>lower.getB() ||
                upper.getC()<lower.getC() || upper.getD()<lower.getD())
            throw new BadParameterException("The upper membership function needs to be higher than the lower membership function.");
        
        this.lMF = lower;
        this.uMF = upper;
        if(DEBUG) System.out.println("Setting the support for the interval type-2 trapezoidal set: "+name);
        this.support = upper.getSupport();
    }
    
    @Override
    public String toString()
    {
        return "IntervalT2MF_Trapezoidal: "+this.getName()+
                ",\nlower MF: "+lMF+"\nupper MF: "+uMF;
    }
}
