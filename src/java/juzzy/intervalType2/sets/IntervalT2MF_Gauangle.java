/*
 * IntervalT2MF_Gauangle.java
 *
 * Author: Christian Wagner
 * Copyright 2007 Christian Wagner All Rights Reserved.
 */

package intervalType2.sets;

import generic.BadParameterException;
import generic.Tuple;
import type1.sets.T1MF_Gauangle;


/**
 *
 * @author Christian Wagner
 */
public class IntervalT2MF_Gauangle extends IntervalT2MF_Prototype
{
    private boolean leftShoulder=false, rightShoulder=false;

    /**
     * Creates a new instance of the IT2 Gauangle set
     * @param name
     * @param uMF The upper membership function
     * @param lMF  The lower membership function
     */
    public IntervalT2MF_Gauangle(String name, T1MF_Gauangle uMF, T1MF_Gauangle lMF)
    {
        super(name);
        this.uMF = uMF;
        this.lMF = lMF;
        support = new Tuple(Math.min(uMF.getSupport().getLeft(), lMF.getSupport().getLeft()), Math.max(uMF.getSupport().getRight(), lMF.getSupport().getRight()));
    }

    @Override
    public String toString()
    {
        String s = this.getName()+" - IT2 Gauangle with UMF:\n"+this.getUMF()+" and LMF:\n"+this.getLMF();
        if(isLeftShoulder) s+="\n (LeftShoulder)";
        if(isRightShoulder) s+="\n (RightShoulder)";
        return s;        
    }
    
    @Override
    public Tuple getFS(double x) 
    {
        double l = lMF.getFS(x);
        double u = uMF.getFS(x);      
        
        if(lMF.getPeak()==uMF.getPeak())
            return new Tuple(Math.min(l, u), Math.max(l, u));
        else
        {
            if(x<=Math.max(lMF.getPeak(),uMF.getPeak()) && x>=Math.min(lMF.getPeak(),uMF.getPeak()))
                return new Tuple(Math.min(l, u), 1.0);
            else
                return new Tuple(Math.min(l, u), Math.max(l, u));
        }
    }    

    public int compareTo(Object o)
    {
        if (!(o instanceof IntervalT2MF_Gauangle))
        throw new ClassCastException("An IntervalT2MF_Gauangle object is expected for comparison with another IntervalT2MF_Gauangle object.");
        else
        {
            if(((T1MF_Gauangle)this.uMF).compareTo(((IntervalT2MF_Gauangle)o).getUMF())==0 && ((T1MF_Gauangle)this.lMF).compareTo(((IntervalT2MF_Gauangle)o).getLMF())==0)
                return 0;
            if(((T1MF_Gauangle)this.uMF).compareTo(((IntervalT2MF_Gauangle)o).getUMF())<0 && ((T1MF_Gauangle)this.lMF).compareTo(((IntervalT2MF_Gauangle)o).getLMF())<0)
                return -1;
            //else
            return 1;
        }
    }
}
