/*
 * T1MF_Trapezoidal.java
 *
 * Created on 12 January 2009, 10:36
 *
 * Copyright 2008 Christian Wagner All Rights Reserved.
 */

package type1.sets;

import generic.Tuple;

/**
 * Class for Trapezoidal Type-1 Fuzzy Membership Functions.
 * The parameters of the MF are defined from left to right as a,b,c and d.
 * The MF supports both normal MFs where the membership between b and c is 1.0,
 * as well as non-normal MFs where this membership can be specified freely.
 * @author Christian Wagner
 */
public class T1MF_Trapezoidal extends T1MF_Prototype
{
    private double a, b, c, d;
    private double lS = Double.NaN, rS = Double.NaN;    //left and right "leg" slope
    private double lI = Double.NaN, rI = Double.NaN;    //left and right "leg" intercept    
    private double output;
    private double peak = Double.NaN;    //peak is generally defined as the avg of b and c,
    //however as part of some applications this may be changed using the setPeak() method.

    //For normal trap. MFs this is 1.0 but it can be different.
    //the ylevels specify the y value of points b and c of the trapezoid
    private double[] yLevels = new double[]{1.0,1.0};

    /**
     * Standard Constructor for normal trapezoidal MF.
     * @param name
     * @param parameters Parameters from left to right.
     */
    public T1MF_Trapezoidal(String name, double[] parameters)
    {
        super(name);
        this.a = parameters[0];
        this.b = parameters[1];
        this.c = parameters[2];
        this.d = parameters[3];
        this.support = new Tuple(a,d);
    }   

    /**
     * Constructor for non-normal trapezoidal MF.
     * @param name
     * @param parameters Parameters from left to right.
     * @param yLevels The specific yLevels for the second and third point of
     * the trapezoid. Normally, both values are equal.
     */
    public T1MF_Trapezoidal(String name, double[] parameters, double[] yLevels)
    {
        super(name);
        this.yLevels[0] = yLevels[0];
        this.yLevels[1] = yLevels[1];
        this.a = parameters[0];
        this.b = parameters[1];
        this.c = parameters[2];
        this.d = parameters[3];
        this.support = new Tuple(a,d);
    }
       

    @Override
    public double getFS(double x) 
    {
        if(isLeftShoulder && x<=c) return 1.0;
        if(isRightShoulder && x>=b) return 1.0;
        
        if(x<b && x>a)
        {
            output = yLevels[0]*(x-a)/(b-a);
        }
        else if(x>=b && x<=c)
        { 
            if(yLevels[0]==yLevels[1])
                output = yLevels[0];
            else if(yLevels[0]<yLevels[1])
                output = (yLevels[1]*x-yLevels[0]*x-yLevels[1]*b+yLevels[0]*b)/(c-b)+yLevels[0];
            else
                output = (yLevels[1]*x-yLevels[0]*x-yLevels[1]*b+yLevels[0]*b)/(c-b)+yLevels[0];

            if(output<0) output=0;
        }
        else if(x>c && x<d)
        {
            output = yLevels[1]*(d-x)/(d-c);
        }
        else
        { output=0.0;}

        //fix calculation errors because of imprecision
        if(Math.abs(1-output)<0.000001) output = 1.0;
        if(Math.abs(output)<0.000001) output = 0.0;
        
        return output;
    }	

    public double getA()
    {
            return a;
    }

    public double getB()
    {
            return b;
    }
    public double getC()
    {
            return c;
    }        
    public double getD()
    {
            return d;
    }    
    /**
     * Returns the MFs parameters
     * @return An array of type double, holding all 4 parameters in order from
     * left to right.
     */
    public double[] getParameters()
    {
        return new double[]{a,b,c,d};
    }

    /**
     * As standard, the peak is generally defined as the average of b and c,
     * however it may be changed using the setPeak() method if desired.
     * @return The peak of either as average of b and c or as specified.
     */
    public double getPeak() 
    {
        if(Double.isNaN(peak))
            peak = (b+c)/2.0;        
        return peak;
    }
    public void setPeak(double peak)
    {
        this.peak = peak;
    }

    /**
     * Retrieves the yLevels of the second and third parameters (points B and C)
     * This is useful for non-normal MFs.
     * @return The degrees of membership of the inner parameters of the MF.
     */
    public double[] getyLevels() {
        return yLevels;
    }

    /**
     * Returns the degrees of membership of the inner parameters of the MF.
     * @param yLevels 
     */
    public void setyLevels(double[] yLevels) {
        this.yLevels = yLevels;
    }
    

    @Override
    public int compareTo(Object o) 
    {
        if (!(o instanceof T1MF_Trapezoidal))
        throw new ClassCastException("A T1MF_Trapezoidal object is expected for comparison with another T1MF_Trapezoidal object.");        
        
        if(this.getA() ==((T1MF_Trapezoidal)o).getA() && this.getB() == ((T1MF_Trapezoidal)o).getB() && this.getC() == ((T1MF_Trapezoidal)o).getC()&& this.getD() == ((T1MF_Trapezoidal)o).getD())
            return 0;
        if(this.getA() <=((T1MF_Trapezoidal)o).getA() && this.getB() <= ((T1MF_Trapezoidal)o).getB() && this.getC() <= ((T1MF_Trapezoidal)o).getC()&& this.getD() <= ((T1MF_Trapezoidal)o).getD())
            return -1;
        //else
        return 1;
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
        lS = 1.0 / (b-a);
        lI = 0 - lS * a;
        
        //right leg
        rS = -1.0 / (d-c);
        rI = 0 - rS * d;   
    }
    
    public String toString()
    {
        String s = "T1MF_Trapezoidal: "+name+"  -  "+a+"  "+b+" (y="+yLevels[0]+")  "+c+" (y="+yLevels[1]+")  "+d;
        if(isLeftShoulder) s+=" (LeftShoulder)";
        if(isRightShoulder) s+=" (RightShoulder)";
        return s;
    }    

}





















