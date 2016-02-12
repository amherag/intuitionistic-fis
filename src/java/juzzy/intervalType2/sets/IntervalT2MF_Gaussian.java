/*
 * IntervalT2MF_Gaussian.java
 *
 * Copyright 2012 Christian Wagner All Rights Reserved.
 */

package intervalType2.sets;

import generic.BadParameterException;
import generic.Tuple;
import type1.sets.T1MF_Gaussian;


/**
 * Class for Gaussian Interval Type-2 Fuzzy Sets
 * @author Christian
 */
public class IntervalT2MF_Gaussian extends IntervalT2MF_Prototype
{
    private final boolean DEBUG = false;

    public IntervalT2MF_Gaussian(String name)
    {
        super(name);
    }    
    
    /**
     * Interval Type-2 Gaussian Membership Function. Note that uncertain mean is
     * supported, however, the mean of the upper MF should be larger than that 
     * of the lower MF (otherwise the LMF and UMF will be swapped internally).
     * @param name
     * @param uMF
     * @param lMF 
     */
    public IntervalT2MF_Gaussian(String name, T1MF_Gaussian uMF, T1MF_Gaussian lMF)
    {
        super(name, uMF, lMF);
        if(uMF.getMean()<lMF.getMean())
            throw new BadParameterException("By convention, the mean of the upper membership function should be larger than the mean of the lower membership function.");
        if(uMF.getSpread()<lMF.getSpread())
            throw new BadParameterException("By convention, the st. dev. (spread) of the upper membership function should be larger than the st. dev. of the lower membership function.");
        support = uMF.getSupport();
    }
    
    @Override
    public T1MF_Gaussian getUMF()
    {
        return (T1MF_Gaussian)uMF;
    }
    @Override
    public T1MF_Gaussian getLMF()
    {
        return (T1MF_Gaussian)lMF;
    }
    
    @Override
    public Tuple getFS(double x) 
    {
        double temp, temp2;
        if(x<support.getLeft())
        {
            return new Tuple(0.0,0.0);
        }
        if(x>support.getRight())
        {
            return new Tuple(0.0,0.0);
        }

        //if means are the same
        if (((T1MF_Gaussian)lMF).getMean() == ((T1MF_Gaussian)uMF).getMean())
        {
            return new Tuple(Math.exp(-0.5*Math.pow(((x-((T1MF_Gaussian)lMF).getMean())/((T1MF_Gaussian)lMF).getSpread()),2)),Math.exp(-0.5*Math.pow(((x-((T1MF_Gaussian)uMF).getMean())/((T1MF_Gaussian)uMF).getSpread()),2)));
        }
        else    //with uncertain mean things are a bit more complicated...rely on innerMean being <= outerMean!
        {
            //first find upper Membership value
            if(x<((T1MF_Gaussian)lMF).getMean())
            {
                temp = Math.exp(-0.5*Math.pow(((x-((T1MF_Gaussian)lMF).getMean())/((T1MF_Gaussian)lMF).getSpread()),2));
            }
            else if(x>((T1MF_Gaussian)uMF).getMean())
            {
                temp = Math.exp(-0.5*Math.pow(((x-((T1MF_Gaussian)uMF).getMean())/((T1MF_Gaussian)uMF).getSpread()),2));
            }
            else
            {
                temp = 1.0; 
            }

            //now for the lower Membership value
            if(x<(((T1MF_Gaussian)lMF).getMean()+((T1MF_Gaussian)uMF).getMean())/2)
            {
                temp2 = Math.exp(-0.5*Math.pow(((x-((T1MF_Gaussian)uMF).getMean())/((T1MF_Gaussian)uMF).getSpread()),2));
            }
            else
            {
                temp2 = Math.exp(-0.5*Math.pow(((x-((T1MF_Gaussian)lMF).getMean())/((T1MF_Gaussian)lMF).getSpread()),2));
            }
            return new Tuple(Math.min(temp, temp2), Math.max(temp, temp2));
        }
    }    
    
    @Override
    public String toString()
    {
        return ("Gaussian Interval Type-2 MF: "+this.name+"\nUMF: "+this.uMF+
                "\nLMF: "+this.lMF);
    }
}

