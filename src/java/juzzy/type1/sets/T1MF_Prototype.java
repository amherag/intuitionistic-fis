/*
 * T1MF_Prototype.java
 *
 * Author: Christian Wagner
 * Copyright 2012 Christian Wagner All Rights Reserved.
 */

package type1.sets;

import generic.Tuple;
import java.io.Serializable;

/**
 * Prototype class for Type-1 Fuzzy Membership Functions.
 * This class should be extended by new Type-1 Membership Function classes.
 * @author Christian Wagner
 */
public abstract class T1MF_Prototype implements T1MF_Interface, Comparable, Serializable
{
    protected boolean isLeftShoulder = false;
    protected boolean isRightShoulder = false;
    protected String name;
    protected Tuple support;  
    private final boolean DEBUG = false;

    public T1MF_Prototype(String name)
    {
        this.name = name;
    }

    @Override
    public Tuple getSupport()
    {
        return support;
    }
    
    @Override
    public void setSupport(Tuple support)
    {
        this.support = support;
    }    

    @Override
    public String getName()
    {
        return name;
    }    
    
    @Override
    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public boolean isLeftShoulder() 
    {
        return isLeftShoulder;
    }

    @Override
    public boolean isRightShoulder() 
    {
        return isRightShoulder;
    }
    
    @Override
    public void setLeftShoulder(boolean value) 
    {
        isLeftShoulder=value;
    }

    @Override
    public void setRightShoulder(boolean value) 
    {
        isRightShoulder = value;
    }	    

    /**
     * Returns the defuzzified value of this set computed using the centroid algorithm.
     * @param numberOfDiscretizations The number of discretizations to be employed.
     */
    @Override
    public double getDefuzzifiedCentroid(int numberOfDiscretizations)
    {
        if(DEBUG)System.out.println(this.getSupport());
        double stepSize=this.getSupport().getSize()/(numberOfDiscretizations-1.0);
        double currentStep = this.getSupport().getLeft();

        double numerator = 0.0, denominator = 0.0, fs = 0.0;
        for(int i=0;i<numberOfDiscretizations;i++)
        {
            if(DEBUG)System.out.println("currentStep = "+currentStep+"   FS = "+fs);
            fs = this.getFS(currentStep);
            numerator += currentStep * fs;
            denominator += fs;
            currentStep+=stepSize;
        }
        if(denominator==0.0) return 0.0;
        else
        return numerator / denominator;
    }

    /**
     * Returns the Center of this Set
     */
    public double getDefuzzifiedCOS()
    {
        return this.getPeak();
    }

}
