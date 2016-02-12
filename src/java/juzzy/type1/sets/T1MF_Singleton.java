/*
 * T1MF_Singleton.java
 *
 * Created on 18 March 2007, 15:13
 *
 * Author: Christian Wagner
 * Copyright 2006 Christian Wagner All Rights Reserved.
 */

package type1.sets;

import generic.Tuple;

/**
 * Membership Function represented by a single double value - for example heavily used in TSK/Anfis for consequents.
 * @author Christian Wagner
 */
public class T1MF_Singleton extends T1MF_Prototype
{
    private double value;
    
    /**
     * Creates a new instance.
     * @param name
     * @param value The value where the singleton exists.
     */
    public T1MF_Singleton(String name, double value)
    {   
        super(name);
        this.value = value;
        this.support = new Tuple(value,value);
    }
    
    /** 
     * Creates a new instance with empty name.
     * @param value The value where the singleton exists.
     */
    public T1MF_Singleton(double value)
    {   
        super("");
        this.value = value;
    }    
    
    public double getValue()
    {
        return value;
    }

    public double getFS(double x) 
    {
        if (x==value) return 1.0;
                else return 0.0;
    }


    @Override
    public double getPeak() {
        return this.getValue();
    }
    
    @Override
    public Tuple getAlphaCut(double alpha) 
    {
        return new Tuple(value,value);
    }
    
    @Override
    public String toString()
    {
        return this.name+" - Singleton at :"+this.value;
    }

    @Override
    public int compareTo(Object o)
    {
        if(o instanceof T1MF_Singleton)
        {
            if(this.getValue() ==((T1MF_Singleton)o).getValue())
                return 0;
            if(this.getValue() <((T1MF_Singleton)o).getValue())
                return -1;
            return 1;
        }
        else
        if(o instanceof T1MF_Triangular)
        {
            //if(this.getValue() ==((T1MF_Triangular)o).getPeak())
            //    return 0;                 --> they are never equal
            if(this.getValue() <((T1MF_Triangular)o).getPeak())
                return -1;
            return 1;
        }
        else
            throw new ClassCastException("A T1MF_Triangular object is expected for comparison with another T1MF_Triangular ot T1MF_Singleton object.");

    }
}
