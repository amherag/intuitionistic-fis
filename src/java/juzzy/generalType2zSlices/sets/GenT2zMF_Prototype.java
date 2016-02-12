/*
 * GenT2zMF_Prototype.java
 *
 * Author: Christian Wagner
 * Copyright 2012 Christian Wagner All Rights Reserved.
 */

package generalType2zSlices.sets;

import generic.BadParameterException;
import generic.Tuple;
import intervalType2.sets.IntervalT2MF_Interface;
import type1.sets.T1MF_Discretized;


/**
 * Prototype class for zSlices based general type-2 fuzzy sets. This class should
 * not be instantiated directly but extended by the specific fuzzy set classes
 * such as triangular, Gaussian, etc.
 * @author Christian Wagner 2010
 */
abstract public class GenT2zMF_Prototype implements GenT2zMF_Interface
{
    protected IntervalT2MF_Interface[] zSlices;
    protected Tuple support;
    protected String name;
    protected int numberOfzLevels;
    protected double z_stepSize;   //value of slice on the z axis
    protected double[] slices_zValues;
    protected Tuple[] slices_fs;
    protected boolean isLeftShoulder = false, isRightShoulder = false;

    private final boolean DEBUG = false;


    /** Creates a new instance of GenT2zMF_Trapezoidal */
    public GenT2zMF_Prototype(String name)
    {
        this.name = name;
    }

    public void setSupport(Tuple support) {
        this.support = support;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The number of zLevels. Note that this doe not include a zSlice at zLevel
     * 0. No slice is modelled at this level.
     * @return 
     */
    public int getNumberOfSlices()
    {
        return numberOfzLevels;
    }

    public IntervalT2MF_Interface getZSlice(int slice_number)
    {
        if(slice_number>=this.getNumberOfSlices())
            throw new BadParameterException("The zSlice reference "+slice_number+" is invalid as the set has only "+this.getNumberOfSlices()+" zSlices.");
        return zSlices[slice_number];
    }

    /**
     * Method to set or swap a specific zSlice. The method replaces a specific 
     * zSlice with the given zSlice respectively IT2 set. Note that currently
     * NO checks whether a 
     * provided zSlices results in the violation of the general type-2 fuzzy set
     *  restrictions are done - no exceptions are thrown!
     * @param zSlice
     * @param zLevel 
     * @throws BatParameterException If the zSlices is not contained within the previous 
     * zSlice and does not contain the next zSlice.
     */
    public void setZSlice(IntervalT2MF_Interface zSlice, int zLevel)
    {
        // for consistency checking - when this gets implemented...
//        if(zLevel == 0)
//        {
//            
//        }
//        else if(zLevel == numberOfzLevels)
//        {
//            
//        }
//        else
//        {
//            
//        }
        
        //simple replacement for now:
        this.zSlices[zLevel] = zSlice;
    }    
    public double getZValue(int slice_number)
    {
        if(slice_number>=this.getNumberOfSlices())
            throw new BadParameterException("The zSlice reference "+slice_number+" is invalid as the set has only "+this.getNumberOfSlices()+" zSlices.");
        if(slices_zValues==null)
            setZValues();
        return slices_zValues[slice_number];
    }

    public String getName()
    {
        return name;
    }

    private void setZValues()
    {
        double stepSize = 1.0 / this.getNumberOfSlices();
        double firstStep = stepSize;
        slices_zValues = new double[this.getNumberOfSlices()];
        for(int i=0;i<slices_zValues.length;i++)
        {
            slices_zValues[i] = firstStep+i*stepSize;
        }
    }

    /**
     * Returns the weighted average of the firing strength of the zSlices of this
     * set. Employed for example in order to compare the firing strength for a
     * given input of mutliple zSlices based general type-2 fuzzy sets.
     * @return
     */
    public double getFSWeightedAverage(double x)
    {
        double numerator = 0.0, denominator = 0.0;
        for(int i = 0;i<this.getNumberOfSlices();i++)
        {
            numerator += this.getZSlice(i).getFSAverage(x) * this.getZValue(i);
            denominator += this.getZValue(i);
        }
        return numerator / denominator;
    }

    public T1MF_Discretized getFS(double x) 
    {
        T1MF_Discretized slice = new T1MF_Discretized("VerticalSlice_at"+x+"_of_"+this.getName(), numberOfzLevels);
        Tuple temp;

        //build up vertical slice
        buildup:
        for(int i=0;i<numberOfzLevels;i++)
        {
            temp = this.getZSlice(i).getFS(x);
            if(DEBUG)System.out.println("On slice "+i+" with x = "+x+", getFS() returns: "+temp);           
            if(DEBUG)System.out.println("Adding Tuple: "+new Tuple(this.getZValue(i),temp.getLeft()));
            if(DEBUG)System.out.println("Adding Tuple: "+new Tuple(this.getZValue(i),temp.getRight()));
            slice.addPoint(new Tuple(this.getZValue(i),temp.getLeft()));
            slice.addPoint(new Tuple(this.getZValue(i),temp.getRight()));
        }
        if(slice.getNumberOfPoints()>0)
            return slice;
        else return null;     
    }      

    public double[] getZValues()
    {
        if(slices_zValues==null)
            setZValues();
        return slices_zValues;
    }

    public Tuple getSupport()
    {
        return support;
    }

    public boolean isLeftShoulder() 
    {
        return isLeftShoulder;
    }

    public boolean isRightShoulder() 
    {
        return isRightShoulder;
    }
    public void setLeftShoulder(boolean isLeftShoulder) 
    {
        this.isLeftShoulder = isLeftShoulder;
    }

    public void setRightShoulder(boolean isRightShoulder) 
    {
        this.isRightShoulder = isRightShoulder;
    }    


    public T1MF_Discretized getCentroid(int primaryDiscretizationLevel)
    {
        T1MF_Discretized slice = new T1MF_Discretized("Centroid_of_"+this.getName(), numberOfzLevels);
        Tuple temp;
                
        buildup:
        for(int i=0;i<numberOfzLevels;i++)
        {
            temp = this.getZSlice(i).getCentroid(primaryDiscretizationLevel);
            if(DEBUG)System.out.println("On slice number"+i+" ("+this.getZSlice(i).getName()+") with primaryDiscretizationLevel = "+
                    primaryDiscretizationLevel+" getCentroid() returns: "+temp);

            slice.addPoint(new Tuple(this.getZValue(i),temp.getLeft()));
            slice.addPoint(new Tuple(this.getZValue(i),temp.getRight()));
        }
        if(slice.getNumberOfPoints()>0)
            return slice;
        else return null;
    }
    
    /**
     * @return the average of the peaks of each slice
     */
    @Override
    public double getPeak() 
    {
    	double average = 0;
    	for(int i=0;i<this.getNumberOfSlices();i++)  {
    		average += this.getZSlice(i).getPeak();
    	}
    	average = average/this.getNumberOfSlices();
    	return average;

    }    
    
    public String toString()
    {
        String s = "zMF(noSlices:"+this.getNumberOfSlices()+"):[";
        for(int i=0;i<this.getNumberOfSlices();i++)
            s+=this.getZSlice(i);
        s+="]\n";
        return s;
    }
}
