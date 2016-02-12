/*
 * IntervalT2MF_Cylinder.java
 *
 * Created on 04 June 2007, 12:08
 *
 * Author: Christian Wagner
 * Copyright 2006 Christian Wagner All Rights Reserved.
 */

package intervalType2.sets;

import generic.BadParameterException;
import generic.Tuple;
import type1.sets.T1MF_Cylinder;
import type1.sets.T1MF_Discretized;

/**
 * The class IntervalT2MF_Cylinder represents a membership function which is 
 * defined by a single Tuple and can be seen as a cylinder from left to right.
 * It does not vary in height/firing strength but its getFS() method will return
 * the same FS for all x. (In the literature referred to as cylindrical extension
 * of a FS.
 * @author Christian Wagner
 */
public class IntervalT2MF_Cylinder extends IntervalT2MF_Prototype
{
    
    /** Creates a new instance of IntervalT2MF_Cylinder */
    public IntervalT2MF_Cylinder(String name, Tuple primer)
    {
        super(name);
        if(primer == null) 
            throw new BadParameterException("IntervalT2MD_Cylinder primer is NULL!");
        if(primer.getLeft() > primer.getRight())
        {
            if(primer.getLeft()-primer.getRight()<0.000001) //account for floating point errors
            {
                primer.setLeft(primer.getRight());
            }
            else
                throw new BadParameterException("Lower firing strength ("+primer.getLeft()+") should not be higher than Upper firing strength ("+primer.getRight()+").");             
        }
        
        this.uMF = new T1MF_Cylinder(name+"_uMF", primer.getRight());
        this.lMF = new T1MF_Cylinder(name+"_lMF", primer.getLeft());
        support = new Tuple(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }
    
    public IntervalT2MF_Cylinder(String name, T1MF_Cylinder uMF, T1MF_Cylinder lMF)
    {
        super(name, uMF, lMF);
    }    

    
    @Override
    public String toString()
    {
        return ("Interval Type-2 Cylindrical Extension of FS: ["+lMF.getFS(0)+","+uMF.getFS(0)+"]");
    }
    
}
