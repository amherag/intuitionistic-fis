/*
 * GenT2MFz_Triangular.java
 *
 * Created on 07 April 2007, 05:20
 *
 * Author: Christian Wagner
 * Copyright 2007 Christian Wagner All Rights Reserved.
 */

package generalType2zSlices.sets;

import generic.Tuple;
import intervalType2.sets.IntervalT2MF_Triangular;
import type1.sets.T1MF_Discretized;
import type1.sets.T1MF_Triangular;


/**
 * zSlices based General Type-2 MF implementation for Triangular Membership Functions.
 * @author Christian Wagner
 */
public class GenT2zMF_Triangular extends GenT2zMF_Prototype
{
    private IntervalT2MF_Triangular primer;
    
    private final boolean DEBUG = false;
    
    
    /**
     * Creates a new instance of GenT2MFz_Triangular by accepting an Interval Type-2
     * Fuzzy Set as primer. The specified number of zLevels are created evenly
     * with the Footprint of Uncertainty of the original IT2 set.
     * Note that the actual primer will be the first zSlice (at zLevel 
     * (1.0 / numberOfzLevels)) and that there will be no zSlice at zLevel 0.
     * @param name
     * @param primer
     * @param numberOfzLevels 
     */
    public GenT2zMF_Triangular(String name, IntervalT2MF_Triangular primer, int numberOfzLevels)
    {
        super(name);
        double left_stepsize, right_stepsize;
        
        this.numberOfzLevels = numberOfzLevels;
        this.support = primer.getSupport();
        this.primer = primer;
        slices_fs = new Tuple[numberOfzLevels];
        slices_zValues = new double[numberOfzLevels];
        
        //setup z value stepSize
        z_stepSize = 1.0/(numberOfzLevels);

        //setup actual set by creating zSlices (subsets of primer set)
        zSlices = new IntervalT2MF_Triangular[numberOfzLevels];
        left_stepsize = ((primer.getLMF().getStart()-primer.getUMF().getStart())/(numberOfzLevels-1))/2.0;
        right_stepsize = ((primer.getUMF().getEnd()-primer.getLMF().getEnd())/(numberOfzLevels-1))/2.0;
        
        //add primer
        zSlices[0] = new IntervalT2MF_Triangular("Slice 0", primer.getUMF(), primer.getLMF());
        double[] inner = new double[]{primer.getLMF().getStart(), primer.getLMF().getPeak(), primer.getLMF().getEnd()};
        double[] outer = new double[]{primer.getUMF().getStart(), primer.getUMF().getPeak(), primer.getUMF().getEnd()};

        slices_zValues[0] = z_stepSize;
        if(DEBUG)System.out.println(zSlices[0].toString()+"  zValue = "+slices_zValues[0]);

        for(int i=1; i<numberOfzLevels;i++)
        {
            slices_zValues[i] = (i+1)*z_stepSize;
            inner[0]-=left_stepsize; inner[2]+=right_stepsize;
            outer[0]+=left_stepsize; outer[2]-=right_stepsize;  
            
            //check for floating point inaccuracies
            if(Math.abs(inner[0]-outer[0])<0.000001)
                outer[0] = inner[0];
            if(Math.abs(inner[2]-outer[2])<0.000001)
                outer[2] = inner[2];            
            
            zSlices[i] = new IntervalT2MF_Triangular("Slice_"+i,
                    new T1MF_Triangular("Slice_"+i+"_UMF", outer[0], outer[1], outer[2]),
                    new T1MF_Triangular("Slice_"+i+"_LMF", inner[0], inner[1], inner[2]));
                    
            if(DEBUG)System.out.println(zSlices[i].toString()+"  zValue = "+slices_zValues[i]);
        }        
    }
    
    /**
     * Creates a new instance of GenT2MFz_Triangular by taking two interval 
     * type-2 sets as first and last slice as inputs. Depending on the numberOfzLevels
     * specified, additional zSlices will be created between both initial zSlices.
     * @param name
     * @param primer0 The first zSlice (to be situated at zLevel 1.0/numberOfzLevels)
     * @param primer1 The last zSlice (to be situated at zLevel 1.0)
     * @param numberOfzLevels 
     */
    public GenT2zMF_Triangular(String name, IntervalT2MF_Triangular primer0, IntervalT2MF_Triangular primer1, int numberOfzLevels)
    {
        super(name);
        double lsu; //left stepSize (left "wing" of set), upper MFs (ie upper MFs of primer 0 and primer 1)
        double lsl; //left stepSize, lower MFs
        double rsu, rsl; //right side...
        
        this.numberOfzLevels = numberOfzLevels;
        this.support = primer0.getSupport();
        slices_fs = new Tuple[numberOfzLevels];
        slices_zValues = new double[numberOfzLevels];
        zSlices = new IntervalT2MF_Triangular[numberOfzLevels];
        
        z_stepSize = 1.0/(numberOfzLevels);
        
        zSlices[0] = primer0;   //first zSlice
        slices_zValues[0] = z_stepSize;
        zSlices[zSlices.length-1] = primer1;    //last zSlice
        slices_zValues[zSlices.length-1] = 1.0;
        zSlices[zSlices.length-1].setSupport(zSlices[0].getSupport()); //ensure same "support" for all zSlices
        
        lsu = ((primer1.getUMF().getStart()-primer0.getUMF().getStart())/(numberOfzLevels-1.0));
        lsl = ((primer0.getLMF().getStart()-primer1.getLMF().getStart())/(numberOfzLevels-1.0));
        rsu = ((primer0.getUMF().getEnd()-primer1.getUMF().getEnd())/(numberOfzLevels-1.0));
        rsl = ((primer1.getLMF().getEnd()-primer0.getLMF().getEnd())/(numberOfzLevels-1.0));
        
        double[] inner = new double[]{primer0.getLMF().getStart(), primer0.getLMF().getPeak(), primer0.getLMF().getEnd()};
        double[] outer = new double[]{primer0.getUMF().getStart(), primer0.getUMF().getPeak(), primer0.getUMF().getEnd()};

        //now set up zSlices in between first and last...
        for(int i=1; i<numberOfzLevels-1;i++)
        {
            slices_zValues[i] = (i+1)*z_stepSize;
            inner[0]-=lsl; inner[2]+=rsl;
            outer[0]+=lsu; outer[2]-=rsu;            
            if(DEBUG)System.out.println(this.getName()+"_zSlice "+i+" , inner: "+inner[0]+"  "+inner[1]+"  "+inner[2]+"   outer: "+outer[0]+"  "+outer[1]+"  "+outer[2]);
            zSlices[i] = new IntervalT2MF_Triangular(this.getName()+"_zSlice_"+i, 
                    new T1MF_Triangular(this.getName()+"_zSlice_"+i+"_UMF", outer[0], outer[1], outer[2]),
                    new T1MF_Triangular(this.getName()+"_zSlice_"+i+"_LMF", inner[0], inner[1], inner[2]));
            zSlices[i].setSupport(zSlices[0].getSupport()); //ensure same "support" for all zSlices
        }            
        
    }    
    
    /**
     * Creates a new instance of GenT2MFz_Triangular by taking an array of
     * interval type-2 triangular membership functions as input.
     * The number of zLevels is specified by the length of the primers array.
     * @param name
     * @param primers The sets will be used in order, where the last set will be
     * associated with a zLevel of 1
     */
    public GenT2zMF_Triangular(String name, IntervalT2MF_Triangular[] primers) 
    {
        super(name);
        this.numberOfzLevels = primers.length;
        this.support = primers[0].getSupport();
        slices_fs = new Tuple[numberOfzLevels];
        slices_zValues = new double[numberOfzLevels];
        
        zSlices = new IntervalT2MF_Triangular[numberOfzLevels];
        z_stepSize = 1.0/(numberOfzLevels);

        System.arraycopy(primers,0,zSlices,0,primers.length);
        for(int i=0; i<numberOfzLevels;i++)
        {
            slices_zValues[i] = z_stepSize*(i+1);
            zSlices[i].setSupport(primers[0].getSupport()); //ensure same support for all zSlices
        }        
    }    

}
