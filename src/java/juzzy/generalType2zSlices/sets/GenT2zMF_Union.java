/*
 * GenT2zMF_Union.java
 *
 * Created on 21 May 2007, 19:16
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
public class GenT2zMF_Union extends GenT2zMF_Prototype
{
    
    /** Creates a new instance of GenT2zMF_Union */
    public GenT2zMF_Union(String name, int numberOfzLevels, double[] slices_zValues, IntervalT2MF_Interface[] zSlices)
    {
        super(name);
        this.numberOfzLevels = numberOfzLevels;
        this.slices_zValues = new double[slices_zValues.length];
        System.arraycopy(slices_zValues,0,this.slices_zValues,0,slices_zValues.length);
        this.zSlices = zSlices; //change to arraycopy...
        //this.zSlices = new IntervalT2MF_Interface[numberOfzLevels];
        support = new Tuple(zSlices[0].getSupport().getLeft(),zSlices[0].getSupport().getRight());    //first slice being the "largest"
    }

    public Object clone()
    {
        return new GenT2zMF_Union(name,numberOfzLevels,slices_zValues,zSlices);
    }

    public T1MF_Discretized getFS(double x) 
    {
        T1MF_Discretized slice = new T1MF_Discretized("VSlice", zSlices.length);
        Tuple temp;
        
        //build up vertical slice
        buildup:
        for(int i=0;i<zSlices.length;i++)
        {
            temp = this.getZSlice(i).getFS(x);
            if(temp.getRight()==0) //if outer on interval is 0 - its not part of the set...
            {
                if( i == 0) slice =null;
                
                break buildup;
            }
            else
            {
//                slice.addPoint(new Tuple(temp.getLeft(),this.getZValue(i)));
//                slice.addPoint(new Tuple(temp.getRight(),this.getZValue(i)));                
                slice.addPoint(new Tuple(this.getZValue(i),temp.getLeft()));
                slice.addPoint(new Tuple(this.getZValue(i),temp.getRight()));
            }
        }
        return slice;
    }

    public boolean isLeftShoulder() {
        System.out.println("Shoulder methods not implemented!");
        return false;
    }

    public boolean isRightShoulder() {
        System.out.println("Shoulder methods not implemented!");
        return false;
    }

    public double getLeftShoulderStart() {
        System.out.println("Shoulder methods not implemented!");
        return Double.NaN;        
    }

    public double getRightShoulderStart() {
        System.out.println("Shoulder methods not implemented!");
        return Double.NaN;         
    }    
}
