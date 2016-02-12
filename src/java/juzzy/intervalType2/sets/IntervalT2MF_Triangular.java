/*
 * IntervalT2MF_Triangular.java
 *
 * Created on 07 April 2007, 05:27
 *
 * Copyright 2007 Christian Wagner All Rights Reserved.
 */

package intervalType2.sets;

import generic.BadParameterException;
import generic.Tuple;
import type1.sets.T1MF_Triangular;


/**
 * Class for Triangular Interval Type-2 Fuzzy Sets
 * @author Christian Wagner
 */
public class IntervalT2MF_Triangular extends IntervalT2MF_Prototype
{
    public IntervalT2MF_Triangular(String name)
    {
        super(name);
    }    
    public IntervalT2MF_Triangular(String name, T1MF_Triangular uMF, T1MF_Triangular lMF)
    {
        super(name, uMF, lMF);
        
        if(uMF.getStart()>lMF.getStart() || uMF.getEnd()<lMF.getEnd())
            throw new BadParameterException("The upper membership function needs to be higher than the lower membership function.");
    }

    //Override for casting...
    @Override
    public T1MF_Triangular getLMF() {
        return (T1MF_Triangular)this.lMF;
    }
    @Override
    public T1MF_Triangular getUMF() {
        return (T1MF_Triangular)this.uMF;
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
        if (!(o instanceof IntervalT2MF_Triangular))
        throw new ClassCastException("A IntervalT2MF_Triangular object is expected for comparison with another IntervalT2MF_Triangular object.");
        IntervalT2MF_Triangular set = (IntervalT2MF_Triangular)o;
        if(     this.getLMF().getStart() == set.getLMF().getStart() &&
                this.getLMF().getPeak() == set.getLMF().getPeak() &&
                this.getLMF().getEnd() == set.getLMF().getEnd()
           &&   this.getUMF().getStart() == set.getUMF().getStart() &&
                this.getUMF().getPeak() == set.getUMF().getPeak() &&
                this.getUMF().getEnd() == set.getUMF().getEnd() )
            return 0;
        if(     this.getLMF().getStart() <= set.getLMF().getStart() &&
                this.getLMF().getPeak() <= set.getLMF().getPeak() &&
                this.getLMF().getEnd() <= set.getLMF().getEnd()
           &&   this.getUMF().getStart() <= set.getUMF().getStart() &&
                this.getUMF().getPeak() <= set.getUMF().getPeak() &&
                this.getUMF().getEnd() <= set.getUMF().getEnd() )
            return -1;

        return 1;
    }  
}
