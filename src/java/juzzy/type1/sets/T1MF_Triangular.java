/*
 * T1MF_Triangular.java
 *
 * Created on 18 March 2007, 14:23
 *
 * Author: Christian Wagner
 * Copyright 2006 Christian Wagner All Rights Reserved.
 */

package type1.sets;


import generic.Tuple;

public class T1MF_Triangular extends T1MF_Prototype
{
    private double start, peak, end;
    private double lS = Double.NaN, rS = Double.NaN;    //left and right "leg" slope
    private double lI = Double.NaN, rI = Double.NaN;    //left and right "leg" intercept
    private double output;

    public T1MF_Triangular(String n, double start, double peak, double end)
    {
        super(n);
        name = n;
        this.start = start;
        this.peak = peak;
        this.end = end;
        this.support = new Tuple(start, end);
    }       

    @Override
    public double getFS(double x) 
    {
        if(isLeftShoulder && x<=peak) return 1.0;
        if(isRightShoulder && x>=peak) return 1.0;
        
        if(x<peak && x>start)
        { output=(x-start)/(peak-start); }
        else if(x==peak)
        { output=1.0; }
        else if(x>peak && x<end)
        { output=(end-x)/(end-peak); }
        else
        { output=0.0;}

        return output;
    }	

    public double getStart()
    {
            return start;
    }
    @Override
    public double getPeak()
    {
            return peak;
    }
    public double getEnd()
    {
            return end;
    } 
    
    @Override
    public String toString()
    {
        String s =  name+"  -  "+start+"  "+peak+"  "+end;
        if(isLeftShoulder) s+=" (LeftShoulder)";
        if(isRightShoulder) s+=" (RightShoulder)";
        return s;
    }

    @Override
    public int compareTo(Object o) 
    {
        if (o instanceof T1MF_Triangular)
        {
            if(this.getEnd() ==((T1MF_Triangular)o).getEnd() && this.getStart() == 
                    ((T1MF_Triangular)o).getStart() && this.getPeak() == ((T1MF_Triangular)o).getPeak())
                return 0;
            if(this.getEnd() <=((T1MF_Triangular)o).getEnd() && this.getStart() <= 
                    ((T1MF_Triangular)o).getStart() && this.getPeak() <= ((T1MF_Triangular)o).getPeak())
                return -1;
            //else
            return 1;
        }
        else if(o instanceof T1MF_Singleton)
        {
            //never the same - never return 0
            if(this.getPeak() <((T1MF_Singleton)o).getValue())
                return -1;
            return 1;
        }
        else
            throw new ClassCastException("A T1MF_Triangular object or T1MF_Singleton is expected for comparison with another T1MF_Triangular object.");

    }

    @Override
    public Tuple getAlphaCut(double alpha) 
    {
        findLinearEquationParameters(); //make sure we have the basics
        Tuple cut = new Tuple(
                (alpha - lI)/lS,
                (alpha-rI)/rS);
        return cut;
    }
    
    /**
     * Finds the slopes and intercepts for the left and right "leg" of the membership function.
     * If the parameters for the given set have previously been computed, the method returns directly.
     */
    private void findLinearEquationParameters()
    {
        //check if values have already been populated
        if(!Double.isNaN(lS))
            return;
        
        //left leg
        lS = 1.0 / (peak-start);
        lI = 0 - lS * start;
        
        //right leg
        rS = -1.0 / (end-peak);
        rI = 0 - rS * end;        
    }
}





















