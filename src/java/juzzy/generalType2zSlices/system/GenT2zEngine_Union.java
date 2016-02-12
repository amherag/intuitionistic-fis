/*
 * GenT2zEngine_Union.java
 *
 * Created on 21 May 2007, 18:52
 *
 * Author: Christian Wagner
 * Copyright 2006 Christian Wagner All Rights Reserved.
 */

package generalType2zSlices.system;

import generalType2zSlices.sets.GenT2zMF_Interface;
import generalType2zSlices.sets.GenT2zMF_Union;
import generic.BadParameterException;
import intervalType2.sets.IntervalT2MF_Union;


/**
 *
 * @author Christian Wagner
 */
public class GenT2zEngine_Union
{
    private final short TRADITIONAL = 0;
    private final short GEOMETRIC = 1;
    
    private final short union_method = TRADITIONAL;
    private GenT2zMF_Union union;
    
    
    /** Creates a new instance of GenT2zEngine_Union */
    public GenT2zEngine_Union()
    {
        
    }
   
    public GenT2zMF_Interface getUnion(GenT2zMF_Interface set_a, GenT2zMF_Interface set_b)
    {
        //first check that both sets exist
        if(set_a==null)
            return set_b;
        if(set_b==null)
            return set_a;
        
        //first do some checks, for example only allow sets with same z-disc. level to be intersected
        if(set_a.getNumberOfSlices() != set_b.getNumberOfSlices()) 
            throw new BadParameterException("Both sets need to have the same number of slices to caluclate their union!");
                    
        
        switch (union_method)
        {
            case TRADITIONAL:
            {
                if(set_a==null && set_b==null) return null;
                else
                    if(set_a==null) return set_b;
                    else
                        if(set_b==null) return set_a;
                        else
                        {
                            IntervalT2MF_Union[] zSlices = new IntervalT2MF_Union[set_a.getNumberOfSlices()];
                            for(int i=0 ; i<set_a.getNumberOfSlices();i++)
                            {
                                zSlices[i] = new IntervalT2MF_Union(set_a.getZSlice(i),set_b.getZSlice(i));
                            }
                            union = new GenT2zMF_Union("Union of "+set_a.getName()+" and "+set_b.getName(),set_a.getNumberOfSlices(),set_a.getZValues(),zSlices);
                        }
                break;
            }
            
            case GEOMETRIC:
            {
                System.out.println("GEOMETRIC UNION IS NOT YET IMPLEMENTED!!!");
            }
        } 
        
        return union;
    }    
}
