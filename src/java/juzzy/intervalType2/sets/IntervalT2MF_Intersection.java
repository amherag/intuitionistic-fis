/*
 * IntervalT2MF_Intersection.java
 *
 * Created on 30 April 2007, 10:19
 *
 * Author: Christian Wagner
 * Copyright 2006 Christian Wagner All Rights Reserved.
 */

package intervalType2.sets;

import generic.Tuple;
import java.util.HashSet;
import java.util.Iterator;
import type1.sets.T1MF_Intersection;

/**
 * Class that manages the intersection of two interval type-2 MFs.
 * The class supports the intersection of other intersections.
 * @author Christian Wagner
 */
public class IntervalT2MF_Intersection extends IntervalT2MF_Prototype
{
    private HashSet<IntervalT2MF_Interface> sets; //an intersection can be based on an arbitrary number of sets
    private boolean intersectionExists = false;  //if set to false there is no actual intersection between the given sets
    
    /**
     * Creates a new instance of IntervalT2MF_Intersection by intersecting a and b.
     * @param a
     * @param b 
     */
    public IntervalT2MF_Intersection(IntervalT2MF_Interface a, IntervalT2MF_Interface b)
    {
        super("dummy-intersection"); //will be updated at the end of constructor

        //check if there is actually an intersection
        checkForIntersection:
        {
            //first - check if we are dealing with a cylinder in which case there will always be an intersection:
            if(a instanceof IntervalT2MF_Cylinder || b instanceof IntervalT2MF_Cylinder)
            {
                //verify that the cylindrical extensions are not "empty"
                if(a instanceof IntervalT2MF_Cylinder && ((IntervalT2MF_Cylinder)a).getUpperBound(0)==0.0)
                {
                    break checkForIntersection;
                }
                else if(b instanceof IntervalT2MF_Cylinder && ((IntervalT2MF_Cylinder)b).getUpperBound(0)==0.0)
                {
                    break checkForIntersection;
                }
                else                
                intersectionExists = true; break checkForIntersection;
            }
            else
            if(a.getSupport().getLeft()==b.getSupport().getLeft())
                {intersectionExists = true; break checkForIntersection;}
            else
            if(a.getSupport().getLeft()<b.getSupport().getLeft())
            {
                if(a.getSupport().getRight()>=b.getSupport().getLeft())
                    {intersectionExists = true; break checkForIntersection;}
            } 
            else
            if(a.getSupport().getLeft()<=b.getSupport().getRight())
                {intersectionExists = true; break checkForIntersection;}
        }

        if(intersectionExists)
        {
            sets = new HashSet();
            
            //deal with special case when the sets are themselves intersections
            if(a instanceof IntervalT2MF_Intersection)
            {
                sets.addAll(((IntervalT2MF_Intersection)a).getSets());
            } else sets.add(a);
            if(b instanceof IntervalT2MF_Intersection)
            {
                sets.addAll(((IntervalT2MF_Intersection)b).getSets());
            } else sets.add(b);

            //DOING THE WORK DOUBLE, REMOVE THE TOP (DIRECT) APPROACH!
            this.uMF = new T1MF_Intersection("uMF of Intersection of ("+a.getName()+","+b.getName()+")", a.getUMF(),b.getUMF());
            this.lMF = new T1MF_Intersection("lMF of Intersection of ("+a.getName()+","+b.getName()+")", a.getLMF(),b.getLMF());            
  
            //find domain and set up set name at the same time
            IntervalT2MF_Interface set;
            Iterator<IntervalT2MF_Interface> it = sets.iterator();
            set = it.next();
        
            if(set.getSupport()!=null && !(set instanceof IntervalT2MF_Cylinder))
            support = new Tuple(set.getSupport().getLeft(), set.getSupport().getRight());
            name = "Intersection of ("+set.getName();
            while(it.hasNext())
            {
                set = it.next();
                //avoid cylinderical extensions:
                if(!(set instanceof IntervalT2MF_Cylinder))
                {
                    if (support==null) support = set.getSupport();
                    else
                    {
                        support.setLeft(Math.min(support.getLeft(), set.getSupport().getLeft()));
                        support.setRight(Math.max(support.getRight(), set.getSupport().getRight()));
                    }
                }
                    name+=" and "+set.getName();
            } name+=")";
            this.setName(name);
        }
        else    
            this.support = null;
    }

    /**
     * Returns the intersection's constituting sets, i.e. all sets which are
     * intersected to give rise to this set.
     * @return
     */
    public HashSet getSets()
    {
        return sets;
    }

    /**
     * Returns true if the set specified is part of this intersection set.
     * @param set
     * @return 
     */
    public boolean containsSet(IntervalT2MF_Interface set)
    {
        return sets.contains(set);
    }

    @Override
    public Tuple getFS(double x)
    {
        if(!intersectionExists)
            return null;
        else
        {
            Tuple returnValue = new Tuple(1.0,1.0);
            Tuple setFS;    //for temp storarge to avoid double computation
            for(IntervalT2MF_Interface set : sets)
            {
                setFS = set.getFS(x);
                returnValue.setLeft(Math.min(returnValue.getLeft(),setFS.getLeft()));
                returnValue.setRight(Math.min(returnValue.getRight(),setFS.getRight()));
            }
            return returnValue;
        }
    }


    public boolean intersectionExists()
    {
        return intersectionExists;
    }

}
