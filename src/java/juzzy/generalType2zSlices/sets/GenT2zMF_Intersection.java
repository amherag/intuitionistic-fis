/*
 * GenT2zMF_Intersection.java
 *
 * Created on 01 May 2007, 15:41
 *
 * Author: Christian Wagner
 * Copyright 2006 Christian Wagner All Rights Reserved.
 */

package generalType2zSlices.sets;

import generic.Tuple;
import intervalType2.sets.IntervalT2MF_Interface;
import type1.sets.T1MF_Discretized;


/**
 *
 * @author Christian Wagner
 */
public class GenT2zMF_Intersection extends GenT2zMF_Prototype 
{
    private final boolean DEBUG = false;
    
    /** Creates a new instance of GenT2zMF_Intersection */
    public GenT2zMF_Intersection(String name, int numberOfzLevels, double[] slices_zValues, IntervalT2MF_Interface[] zSlices)
    {
        super(name);
        this.numberOfzLevels = numberOfzLevels;
        this.slices_zValues = slices_zValues;
        this.zSlices = zSlices;
        support = zSlices[0].getSupport().clone();
        
        if(DEBUG)
        {
            System.out.println("");
            System.out.println("GenT2zMF_Intersection:");
            for(int i=0;i<8;i++)
            {
                System.out.println("For x = "+i*10+" :  "+zSlices[0].getFS(i*10));
            }
            System.out.println("");
        }
    }
    
    public Object clone()
    {
        return new GenT2zMF_Intersection(name,numberOfzLevels, slices_zValues,zSlices);
    }
    
}
