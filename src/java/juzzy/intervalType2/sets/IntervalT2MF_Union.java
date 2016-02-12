/*
 * IntervalT2MF_Union.java
 *
 * Created on 21 May 2007, 18:56
 *
 * Author: Christian Wagner
 * Copyright 2006 Christian Wagner All Rights Reserved.
 */

package intervalType2.sets;

import generic.Tuple;
import java.util.HashSet;
import java.util.Iterator;
import type1.sets.T1MF_Union;

/**
 * Union operation for interval type 2
 * @author Christian Wagner
 */
public class IntervalT2MF_Union extends IntervalT2MF_Prototype
{
    //private IntervalT2MF_Interface left, right;
    private HashSet<IntervalT2MF_Interface> sets;

    private final boolean DEBUG = false;
    private boolean isNull = false;
    
    /**
     * THIS SHOULD BE REMOVED AND ALL UNION AMD INTERSECTION SHOULD BE DONE THROUGH TYPE-1 CASES
     * Creates a new instance of IntervalT2MF_Union 
     */
    public IntervalT2MF_Union(IntervalT2MF_Interface a, IntervalT2MF_Interface b)
    {   
        super("Union of ("+a.getName()+" and "+b.getName()+")");
        this.uMF = new T1MF_Union(a.getUMF(), b.getUMF());
        this.lMF = new T1MF_Union(a.getLMF(), b.getLMF());
              
                support = new Tuple(Math.min(a.getSupport().getLeft(),b.getSupport().getLeft()),
                    Math.max(a.getSupport().getRight(),b.getSupport().getRight()));                
    }


    public HashSet getSets()
    {
        return sets;
    }

    
    public boolean isNull()
    {
        return isNull;
    }

    @Override
    public double getPeak() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
