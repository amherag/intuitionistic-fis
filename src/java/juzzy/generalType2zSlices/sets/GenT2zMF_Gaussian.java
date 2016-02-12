/*
 * GenT2zMF_Gaussian.java
 *
 * Created on 07 May 2007, 13:10
 *
 * Author: Christian Wagner
 * Copyright 2006 Christian Wagner All Rights Reserved.
 */

package generalType2zSlices.sets;

import generic.BadParameterException;
import generic.Tuple;
import intervalType2.sets.IntervalT2MF_Gaussian;
import type1.sets.T1MF_Gaussian;


/**
 * zSlices based General Type-2 MF implementation for Gaussian Membership Functions.
 * @author Christian Wagner
 */
public class GenT2zMF_Gaussian extends GenT2zMF_Prototype
{
    private IntervalT2MF_Gaussian primer;
    
    
    private boolean DEBUG = false;
    
    /**
     * Creates a new instance of GenT2zMF_Gaussian by accepting an Interval Type-2
     * Fuzzy Set as primer. The specified number of zLevels are created evenly
     * with the Footprint of Uncertainty of the original IT2 set.
     * Note that the actual primer will be the first zSlice (at zLevel 
     * (1.0 / numberOfzLevels)) and that there will be no zSlice at zLevel 0.
     * @param name
     * @param primer
     * @param numberOfzLevels 
     */ 
    public GenT2zMF_Gaussian(String name, IntervalT2MF_Gaussian primer, int numberOfzLevels)
    {
        super(name);
        this.numberOfzLevels = numberOfzLevels;
        this.primer = primer;
        this.support = new Tuple(primer.getSupport().getLeft(), primer.getSupport().getRight());
        double stepsize_spread, stepsize_mean;        
        slices_fs = new Tuple[numberOfzLevels];         //setup arrays
        slices_zValues = new double[numberOfzLevels];
        
        //setup z value stepSize
        z_stepSize = 1.0/(numberOfzLevels);
        
        //setup actual set by creating zSlices (subsets of primer set)
        zSlices = new IntervalT2MF_Gaussian[numberOfzLevels];
        
        
        stepsize_spread = ((primer.getUMF().getSpread()-primer.getLMF().getSpread())/(numberOfzLevels-1))/2.0;
        stepsize_mean = ((primer.getUMF().getMean()-primer.getLMF().getMean())/(numberOfzLevels-1))/2.0;
        
        //current_z = 0;
        double[] inner = new double[2];
        double[] outer = new double[2];
        inner[0] = primer.getLMF().getMean();
        inner[1] = primer.getLMF().getSpread();
        outer[0] = primer.getUMF().getMean();
        outer[1] = primer.getUMF().getSpread();
        
        //add primer
        zSlices[0] = new IntervalT2MF_Gaussian(primer.getName()+"_zSlice_0",
                new T1MF_Gaussian(primer.getName()+"_zSlice_0"+"_UMF", outer[0], outer[1]),
                new T1MF_Gaussian(primer.getName()+"_zSlice_0"+"_LMF", inner[0], inner[1]));
        if(primer.isLeftShoulder()) zSlices[0].setLeftShoulder(true);
        if(primer.isRightShoulder()) zSlices[0].setRightShoulder(true);
        slices_zValues[0] = z_stepSize;
        
        if(DEBUG)System.out.println(zSlices[0].toString()+"  Z-Value = "+slices_zValues[0]);

        
        for(int i=1; i<numberOfzLevels;i++)
        {
            slices_zValues[i] = (i+1)*z_stepSize;
            inner[0] += stepsize_mean;
            outer[0] -= stepsize_mean;
            inner[1] += stepsize_spread;
            outer[1] -= stepsize_spread;
            if(outer[1]<inner[1]) inner[1] = outer[1];   //account for imprecision...
            if(outer[0]<inner[0]) inner[0] = outer[0];   //account for imprecision...
            zSlices[i] = new IntervalT2MF_Gaussian(primer.getName()+"_zSlice_"+i,
                    new T1MF_Gaussian(primer.getName()+"_zSlice_"+i+"_UMF", outer[0], outer[1]),
                    new T1MF_Gaussian(primer.getName()+"_zSlice_"+i+"_LMF", inner[0], inner[1]));            
            if(primer.isLeftShoulder()) zSlices[i].setLeftShoulder(true);
            if(primer.isRightShoulder()) zSlices[i].setRightShoulder(true);
            if(DEBUG)System.out.println("zSlice "+i+" is: "+zSlices[i].getName()+" its domain is: "+ zSlices[i].getSupport());
            if(DEBUG)System.out.println(zSlices[i].toString()+"  zValue = "+slices_zValues[i]);            
            //set same domain as primer
            zSlices[i].setSupport(primer.getSupport());
        }       
    }
    
    
    /** Creates a new instance of GenT2zMF_Gaussian by taking an array of interval type-2sets as inputs*/
    public GenT2zMF_Gaussian(String name, IntervalT2MF_Gaussian[] primers) 
    {
        super(name);
        this.numberOfzLevels = primers.length;
        this.support = primers[0].getSupport();
        slices_fs = new Tuple[numberOfzLevels];
        slices_zValues = new double[numberOfzLevels];
        
        zSlices = new IntervalT2MF_Gaussian[numberOfzLevels];
        z_stepSize = 1.0/(numberOfzLevels);
        
        slices_zValues[0] = z_stepSize;

        System.arraycopy(primers,0,zSlices,0,primers.length);
        for(int i=0; i<numberOfzLevels;i++)
        {
            slices_zValues[i] = z_stepSize*(i+1);
            if(DEBUG)System.out.println(zSlices[i].toString()+"  Z-Value = "+slices_zValues[i]);
        }        
    }        
    @Override
    public Object clone()
    {
        return new GenT2zMF_Gaussian(name,primer,numberOfzLevels);
    }
    
    @Override
    public IntervalT2MF_Gaussian getZSlice(int slice_number)
    {
        return (IntervalT2MF_Gaussian)zSlices[slice_number];
    }
    
    
    /**
     *Forces new support over which MF is evaluated
     */
    public void setSupport(Tuple support)
    {
        this.support = support;
        for(int i=1; i<numberOfzLevels;i++)
            zSlices[i].setSupport(this.getSupport());
    }
}
