/*
 * IntervalT2MF_Interface.java
 *
 * Author: Christian Wagner
 * Copyright 2006 Christian Wagner All Rights Reserved.
 */
package intervalType2.sets;

import generic.MF_Interface;
import generic.Tuple;
import java.io.Serializable;
import type1.sets.T1MF_Interface;

public interface IntervalT2MF_Interface extends Serializable, MF_Interface
{
    public Tuple getFS(double x);
    public double getFSAverage(double x);
    public void setSupport(Tuple d);
    public Tuple getSupport();
    public String getName();
    public void setName(String name);
    public double getPeak();
    public void setLeftShoulder(boolean value);
    public void setRightShoulder(boolean value);        
    public boolean isLeftShoulder();
    public boolean isRightShoulder();    
    public T1MF_Interface getUMF();
    public T1MF_Interface getLMF();
    public double getLowerBound(double x);
    public double getUpperBound(double x);
    public Tuple getCentroid(int primaryDiscretizationLevel);
}
