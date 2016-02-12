/*
 * IntervalT2MF_Prototype.java
 *
 * Copyright 2012 Christian Wagner All Rights Reserved.
 */

package intervalType2.sets;

import generic.Tuple;
import java.io.Serializable;
import type1.sets.T1MF_Interface;

/**
 * Prototype class for interval type-2 fuzzy sets. This class should
 * not be instantiated directly but extended by the specific fuzzy set classes
 * such as triangular, Gaussian, etc.
 * @author Christian Wagner 2010
 */
public abstract class IntervalT2MF_Prototype implements IntervalT2MF_Interface, Serializable
{
    protected String name;
    protected boolean isLeftShoulder = false;
    protected boolean isRightShoulder = false;    
    protected T1MF_Interface uMF, lMF;
    protected Tuple support;
    
    private final boolean DEBUG = false;

    public IntervalT2MF_Prototype(String name)
    {
        this.name = name;
    }

    public IntervalT2MF_Prototype(String name, T1MF_Interface uMF, T1MF_Interface lMF)
    {
        this.name = name;
        this.uMF = uMF;
        this.lMF = lMF;
        
        //adjust support - important for uncertain means
        this.support = new Tuple(Math.min(uMF.getSupport().getLeft(), lMF.getSupport().getLeft()), 
                Math.max(uMF.getSupport().getRight(), lMF.getSupport().getRight()));
        this.uMF.setSupport(this.support);
        this.lMF.setSupport(this.support);
    }    

    /**
     * Returns the interval membership degree for the given x.
     * @param x
     * @return Tuple where the left value is the degree of membership of the 
     * lower MF and the right value is the degree of membership of the higher MF.
     */
    public Tuple getFS(double x)
    {
        return(new Tuple(lMF.getFS(x), uMF.getFS(x)));
    }

    /**
     * Returns the average of the lower and upper membership values as a single value.
     * @param x
     * @return The average of the tuple returned by getFS(double x)
     */
    public double getFSAverage(double x)
    {
        return this.getFS(x).getAverage();
    }

    public void setSupport(Tuple d)
    {
        this.support = d;
    }

    public Tuple getSupport()
    {
        return support;
    }

    public void setName(String name)
    {
        this.name = name;
    }
    
    public String getName()
    {
        return name;
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
     * Returns the degree of membership at x for the Lower Membership Function.
     * @param x
     * @return 
     */
    public double getLowerBound(double x)
    {
        return lMF.getFS(x);
    }

    /**
     * Returns the degree of membership at x for the Higher Membership Function.
     * @param x
     * @return 
     */    
    public double getUpperBound(double x)
    {
        return uMF.getFS(x);
    }

    public T1MF_Interface getLMF() {
        return lMF;
    }

    public T1MF_Interface getUMF() {
        return uMF;
    }

    @Override
    public double getPeak() 
    {
        if(uMF.getPeak()==lMF.getPeak())
            return uMF.getPeak();
        else
            return (uMF.getPeak()+lMF.getPeak())/2.0;   //return average
    }    

    /**
     * Computes the centroid of the set using the IntervalT2Engine_Centroid class.
     * @param primaryDiscretizationLevel The discretization level to be employed.
     * @return 
     */
    public Tuple getCentroid(int primaryDiscretizationLevel)
    {
        IntervalT2Engine_Centroid iec = new IntervalT2Engine_Centroid(primaryDiscretizationLevel);
        return iec.getCentroid(this);
    }
    
    /**
     * Each implementation of the prototype should override this method.
     * @return 
     */
    public String toString()
    {
        return new String("Interval Type-2 MF with:\nName: "+name+"\nlMF: "+lMF+"\nuMF: "+uMF+"\nSupport: "+support);
    }
}
