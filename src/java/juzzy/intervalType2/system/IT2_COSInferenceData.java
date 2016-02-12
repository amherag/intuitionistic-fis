/*
 * ConsequentData.java
 *
 * Copyright 2007 Christian Wagner All Rights Reserved.
 */

package intervalType2.system;

import generic.Tuple;

/**
 * Stores Consequent Data in IT2 systems - supports sorting.
 * @author Christian Wagner
 */
public class IT2_COSInferenceData implements Comparable
{
    
    private Tuple firingStrength;
    private double centroidValue;   //left or right
    
    /**
     * Creates a new instance of ConsequentData
     * @param f The firing strength
     * @param c The value of the centroid
     */
    public IT2_COSInferenceData(Tuple f, double c)  
    {
        firingStrength = f;
        centroidValue = c;
    }
    
    public Tuple getFStrength()
    {
        return firingStrength;
    }
    
    public double getSelectedCentroidEndpoint()
    {
        return centroidValue;
    }
    
    public int compareTo(Object o)
    {
        IT2_COSInferenceData cd = (IT2_COSInferenceData)o;
        if (this.getSelectedCentroidEndpoint() < cd.getSelectedCentroidEndpoint()) return -1;
        else if (this.getSelectedCentroidEndpoint() > cd.getSelectedCentroidEndpoint()) return 1;
        else return 0;
    }
    
    public String toString()
    {
        return ("FiringStrength = ["+firingStrength.getLeft()+","+firingStrength.getRight()+"   centroidValue = "+centroidValue);
    }
}
