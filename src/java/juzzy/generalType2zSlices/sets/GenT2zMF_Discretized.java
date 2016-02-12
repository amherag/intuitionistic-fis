/*
 * GenT2zMF_Discretized.java
 *
 * Author: Christian Wagner
 * Copyright 2006 Christian Wagner All Rights Reserved.
 */

package generalType2zSlices.sets;

import type1.sets.T1MF_Discretized;
import type1.sets.T1MF_Interface;

/**
 *
 * @author Christian Wagner
 */
public class GenT2zMF_Discretized extends GenT2zMF_Prototype
{
    private double[][] set;
    private double[] xDiscretizationValues, yDiscretizationValues;
    private final double precision = 0.000001;
    private int xDiscretizationLevel;
    T1MF_Interface[] vSlices;
    
    private final boolean DEBUG = false;
    
    /** Creates a new instance of GenT2zMF_Discretized */
    public GenT2zMF_Discretized(GenT2zMF_Interface gt2set, int primaryDiscretizationLevel)
    {
        super("GenT2zMF_Discretized");  //name is not important;
        support = gt2set.getSupport().clone();
        this.xDiscretizationLevel = primaryDiscretizationLevel;
        this.name = "Discretized version of "+gt2set.getName();
        xDiscretizationValues = new double[primaryDiscretizationLevel];
        double xStep = gt2set.getSupport().getLeft();
        double stepsize = (gt2set.getSupport().getRight()-gt2set.getSupport().getLeft())/(primaryDiscretizationLevel-1);
        vSlices = new T1MF_Discretized[primaryDiscretizationLevel];
        
        for(int i=0;i<primaryDiscretizationLevel;i++)
        {
            xDiscretizationValues[i] = xStep;
            vSlices[i] =  gt2set.getFS(xStep);
            if(DEBUG)
            {
                if(vSlices[i] != null)
                    System.out.println("vSlice number: "+i+" = \n"+vSlices[i].toString());
                else
                    System.out.println("vSlice number: "+i+" = null");
            }
            xStep+=stepsize;
        }
    }
    
    /** 
     * Creates a new instance of GenT2Discretized by setting up a new two-dimensional array using the dimensions provided
     * and "filling" with a discretized version of the set provided.
     *@param primaryDiscretizationLevel The level/number of discretisations performed on the primary/x axis.
     *@param secondaryDiscretizationLevel The level/number of discretisations performed on the secondary/y axis.
     */
    public GenT2zMF_Discretized(GenT2zMF_Interface gt2set, int primaryDiscretizationLevel, int secondaryDiscretizationLevel)
    {    
        super("GenT2zMF_Discretized");
        this.name = gt2set.getName();
        support = gt2set.getSupport().clone();
        set = new double[primaryDiscretizationLevel][secondaryDiscretizationLevel];
        xDiscretizationValues = new double[primaryDiscretizationLevel];
        yDiscretizationValues = new double[secondaryDiscretizationLevel];   
        
        double primStepsize = (this.getSupport().getRight()-this.getSupport().getLeft())/(primaryDiscretizationLevel-1);
        double secStepsize = 1.0/(secondaryDiscretizationLevel-1);
        double xStep=getSupport().getLeft(),yStep=0; //the discretization values
        T1MF_Interface t1set_temp;
        

        
        for(int i=0;i<primaryDiscretizationLevel;i++)
        {
            yStep = 0;
            xDiscretizationValues[i] = xStep;
            if(DEBUG)System.out.println("In iteration "+i+" xStep = "+xStep);
            t1set_temp = gt2set.getFS(xStep);
            //System.out.println(t1set_temp);
            if(t1set_temp!=null)
            for(int j=0; j<secondaryDiscretizationLevel; j++)
            {
                yDiscretizationValues[j] = yStep;
                set[i][j] = t1set_temp.getFS(yStep);
                yStep += secStepsize;
            }
            xStep += primStepsize;
        }
         
    }    

    
    public int getPrimaryDiscretizationLevel()
    {
        return xDiscretizationLevel;
    }

    /**
     *Returns third dimension membership for given array coordinates. (Use getDiscX() and getDiscY() to get discretization level at pointer location.)
     *A filter is applied which returns 0 for any values smaller than the specified precision within the class (usually 0.000001)
     *@param xPointer Points to the position in the array along the x axis.
     *@param yPointer Points to the position in the array along the y axis.
     */
    public double getSetDataAt(int xPointer, int yPointer)
    {
        if(set[xPointer][yPointer]>precision)
            return set[xPointer][yPointer];
        else
            return 0;
    }

    /**
     *Returns discretization value at the specified level on the x Axis.
     *@param xPointer Determines which x discretization value is returned (From 0 to N).
     */
    public double getDiscX(int xPointer)
    {
        return xDiscretizationValues[xPointer];
    }    
    /**
     *Returns discretization value at the specified level on the y Axis.
     *@param yPointer Determines which y discretization value is returned (From 0 to N).
     */
    public double getDiscY(int yPointer)
    {
        return yDiscretizationValues[yPointer];
    }   
    
    
    public int getSecondaryDiscretizationLevel()
    {
        return yDiscretizationValues.length;
    }
 
    public double[] getPrimaryDiscretizationValues()
    {
        return xDiscretizationValues;
    }

    public double[] getSecondaryDiscretizationValues()
    {
        return yDiscretizationValues;
    }           
    
}
