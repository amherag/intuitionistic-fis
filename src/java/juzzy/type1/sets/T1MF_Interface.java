/*
 * T1MF_Interface.java
 *
 * Author: Christian Wagner
 * Copyright 2012 Christian Wagner All Rights Reserved.
 */

package type1.sets;

import generic.MF_Interface;
import generic.Tuple;
import java.io.Serializable;

/**
 * Interface for Type-1 Fuzzy Membership Functions.
 * @author Christian Wagner
 */
public interface T1MF_Interface extends Serializable, MF_Interface//extends Antecedent,Consequent
{
    public double getFS(double x);
    public Tuple getAlphaCut(double alpha);
    public double getPeak();      //the point where the function peaks
    public String getName();
    public void setName(String name);
    public Tuple getSupport();
    public void setSupport(Tuple support);
    public void setLeftShoulder(boolean value);
    public void setRightShoulder(boolean value);        
    public boolean isLeftShoulder();
    public boolean isRightShoulder();
    public double getDefuzzifiedCentroid(int numberOfDiscretizations);
    public double getDefuzzifiedCOS();
}
