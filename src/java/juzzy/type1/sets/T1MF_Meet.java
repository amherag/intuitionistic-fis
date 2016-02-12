/*
 * T1MF_Meet.java
 *
 * Author: Christian Wagner
 * Copyright 2007 Christian Wagner All Rights Reserved.
 */

package type1.sets;

import generic.Tuple;


/**
 * Meet operation for 2 Type-1 sets, mostly used while computing general Type-2 FLSs.
 * @author Christian Wagner
 */
public class T1MF_Meet extends T1MF_Prototype
{
    private final boolean DEBUG = false;
    private T1MF_Interface f1, f2;
    private boolean intersectionExists = false;
    private double v1, v2, temp, temp2;

    
    private final int resolution = 30;
    private final int alphaCutDiscLevel = 10000;
    private final int max_resolution = 10000;
    
    /** Creates a new instance of T1MF_DiscretizedMeet */
    public T1MF_Meet(T1MF_Interface a, T1MF_Interface b) 
    {
        super("T1MF_Meet");
        if(a==null || b==null)
        {
            //set = null;
            intersectionExists = false;
        }
        else
        {
            name = a.getName()+" <meet> "+b.getName();
            intersectionExists = true;
            //find v1 and v2 (maximums of both sets/functions
            temp = findMax(a);
            temp2 = findMax(b);

            this.support = new Tuple(Math.min(a.getSupport().getLeft(),b.getSupport().getLeft()), 
                    Math.min(a.getSupport().getRight(),b.getSupport().getRight()));
            
            
            if(temp < temp2)
            {
                v1 = temp;
                v2 = temp2;
                f1 = a;
                f2 = b;
            }
            else
            {
                v1 = temp2;
                v2 = temp;
                f1 = b;
                f2 = a;
            }
            if(DEBUG)System.out.println("v1: "+v1+"  and : "+v2);
        }
    }
    
    public double getFS(double x) 
    {
        if(x<v1)
            return Math.max(f1.getFS(x),f2.getFS(x));
        else
            if(x<v2)
                return f1.getFS(x);
            else
            {
                return Math.min(f1.getFS(x),f2.getFS(x));
            }
    }
    
    public boolean intersectionExists()
    {
        return intersectionExists;
    }
    
    private double findMax(T1MF_Interface set)
    {
        double stepSize, currentStep = 0;
        
        //new try
        stepSize = (set.getSupport().getRight()-set.getSupport().getLeft())/(max_resolution-1);
        double currentMax = 0, temp, maxStep=0;
        currentStep = set.getSupport().getLeft();
        findMax:
        for(int i=0;i<max_resolution;i++)
        {
            temp = set.getFS(currentStep);
            
            if (temp == 1) return currentStep;
            if(temp>=currentMax)
            {
                currentMax = temp;
                maxStep = currentStep;
            }
            currentStep+= stepSize;
        }
        return maxStep;        
    }

    /**
     *Returns the x values where the alpha cut using the alpha (y) value provided "cuts" the function curve
     */
    public Tuple getAlphaCut(double alpha)
    {
        double stepSize = this.getSupport().getSize()/(alphaCutDiscLevel-1.0);
        double currentStep;
        Tuple alphaCut = null;
        double left=0, right=0;

        //compute alphaCut
        currentStep = this.getSupport().getLeft();
        findLeft:
            for(int i=0;i<alphaCutDiscLevel;i++)
            {
                temp = Math.abs(this.getFS(currentStep)-alpha);
                if (temp<0.001)
                {
                    left = currentStep;
                    break findLeft;
                }
                currentStep+= stepSize;
            }        
        
            currentStep = this.getSupport().getRight();
            
        findRight:
            for(int i=0;i<alphaCutDiscLevel;i++)
            {
                temp = Math.abs(this.getFS(currentStep)-alpha);
                if (temp<0.001)
                {
                    right = currentStep;
                    break findRight;
                }
                currentStep-= stepSize;
            }                 

        alphaCut = new Tuple(left,right);
        return alphaCut;
    }

    @Override
    public double getPeak() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int compareTo(Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
