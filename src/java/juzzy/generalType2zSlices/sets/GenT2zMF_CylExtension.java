/*
 * GenT2zMF_CylExtension.java
 * 
 * Author: Christian Wagner
 * Copyright 2006 Christian Wagner All Rights Reserved.
 */

package generalType2zSlices.sets;

import generic.Tuple;
import intervalType2.sets.IntervalT2MF_Cylinder;
import type1.sets.T1MF_Interface;


/**
 *
 * @author Christian Wagner
 */
public class GenT2zMF_CylExtension extends GenT2zMF_Prototype
{
    private T1MF_Interface baseSet;
    private int zDiscretizationlevel;
    private double zSpacing;

    private final boolean DEBUG = false;


    public GenT2zMF_CylExtension(T1MF_Interface baseSet, int zDiscLevel)
    {
        super("GenT2zCyl-ext-of-"+baseSet.getName());

        this.baseSet = baseSet;
        zDiscretizationlevel = zDiscLevel;
        zSpacing = 1.0/(zDiscretizationlevel);
        zSlices = new IntervalT2MF_Cylinder[zDiscretizationlevel];
        support = new Tuple(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        slices_zValues = new double[zDiscretizationlevel];  
        
        if(DEBUG)System.out.println("Cylindric extension baseset:");
        if(DEBUG)System.out.println(baseSet);
        for(int i=0;i<zDiscretizationlevel;i++)
        {
            slices_zValues[i] = (i+1)*zSpacing;
            zSlices[i] = new IntervalT2MF_Cylinder("Cyl-ext-at-"+slices_zValues[i],baseSet.getAlphaCut(slices_zValues[i]));
        }
        this.numberOfzLevels = zDiscretizationlevel;
    }
    
    public Object clone()
    {
        return new GenT2zMF_CylExtension(baseSet, zDiscretizationlevel);
    }
    
}
