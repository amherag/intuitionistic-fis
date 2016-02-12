/*
 * GenT2zEngine_Intersection.java

 * Author: Christian Wagner
 * Copyright 2006 Christian Wagner All Rights Reserved.
 */

package generalType2zSlices.system;

import generalType2zSlices.sets.GenT2zMF_Interface;
import generalType2zSlices.sets.GenT2zMF_Intersection;
import generic.BadParameterException;
import intervalType2.sets.IntervalT2MF_Intersection;

/**
 *
 * @author Christian Wagner
 */
public class GenT2zEngine_Intersection
{
    private final short TRADITIONAL = 0;
    private final short GEOMETRIC = 1;
    
    private final short intersection_method = TRADITIONAL;
    
    private GenT2zMF_Intersection intersection;
    
    
    /** Creates a new instance of GenT2zEngine_Intersection */
    public GenT2zEngine_Intersection()
    {
    }
    
    public GenT2zMF_Intersection getIntersection(GenT2zMF_Interface set_a, GenT2zMF_Interface set_b)
    {
        if(set_a==null || set_b == null)
            return null;

        //first do some checks, for example only allow sets with same z-disc. level to be intersected
        if(set_a.getNumberOfSlices() != set_b.getNumberOfSlices()) 
        {
            throw new BadParameterException("Both sets need to have the same number of slices to calculate their intersection!\n" +
                    "Here, set A ("+set_a.getName()+") has "+set_a.getNumberOfSlices()+" slices and set B ("+set_b.getName()+") has : "+set_b.getNumberOfSlices());
        }
        
        switch (intersection_method)
        {
            case TRADITIONAL:
            {
                IntervalT2MF_Intersection[] zSlices = new IntervalT2MF_Intersection[set_a.getNumberOfSlices()];
                for(int i=0 ; i<set_a.getNumberOfSlices();i++)
                {
                    zSlices[i] = new IntervalT2MF_Intersection(set_a.getZSlice(i),set_b.getZSlice(i));
                    if(!zSlices[i].intersectionExists()) zSlices[i] = null;
                }
                intersection = new GenT2zMF_Intersection("Intersection of "+set_a.getName()+" and "+set_b.getName(),set_a.getNumberOfSlices(),set_a.getZValues(),zSlices);
                break;
            }
        } 
        
        return intersection;
    }
    
    
    
}
