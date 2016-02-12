/*
 * T1MF_Gauangle.java
 *
 * Created on 21 November 2008, 18:41
 *
 * Copyright 2008 Christian Wagner All Rights Reserved.
 */

package type1.sets;

import generic.Tuple;


/**
 * Class for Gauangle Type-1 Fuzzy Membership Functions.
 * The Gauangle MF combines the smooth "peak" of Gaussian MFs with the linearly
 * decreasing "sides" of triangular MFs.
 * @author Christian Wagner
 */
public class T1MF_Gauangle extends T1MF_Prototype
{
    private double spreadForLeft, spreadForRight;
    private double start, center, end;
    private double transitionPointLeft, transitionPointRight;   //the points at which the set changes from triangular to gaussian and back again
    private double leftCalculationPoint, rightCalculationPoint; //used in computing the MF values for the triangular bits of the MG.

    private final double similarToGaussian = 0.5; //Gauangle MF paramter

    private final boolean DEBUG = false;


    /**
     * Creates a new instance of T1MF_Gauangle.
     * @param name Name of the set
     * @param start Start as for triangular MF.
     * @param center Center as for triangular MF
     * @param end End as for triangular MF.
     */ 
    public T1MF_Gauangle(String name, double start, double center, double end)
    {
        super(name);

        this.center = center;
        this.start = start;
        this.end = end;

        //make some checks to avoid 0 values...
        if (start==center) 
            this.isLeftShoulder=true;
        if(center==end) 
            this.isRightShoulder=true;
        

        this.spreadForLeft = (center-start)*(1.0-similarToGaussian);
        this.spreadForRight = (end-center)*(1.0-similarToGaussian);


        support = new Tuple(start,end);                

        //set up the transition points between gaussian and triangular MFs...
        transitionPointLeft = center-((center-start)*similarToGaussian);
        double[] ab = getLineEquationParameters(new Tuple(start,0.0),
                new Tuple(transitionPointLeft, Math.exp(-0.5*Math.pow(
                ((transitionPointLeft-center)/spreadForLeft),2))));
        leftCalculationPoint = getXForYOnLine(1.0, ab);

        transitionPointRight = center+((end-center)*similarToGaussian);
        
        ab = getLineEquationParameters(new Tuple(transitionPointRight, 
                Math.exp(-0.5*Math.pow(((transitionPointRight-center)/spreadForRight),2))),
                new Tuple(end,0.0));
        rightCalculationPoint = getXForYOnLine(1.0, ab);

        if(DEBUG)System.out.println("Transition points between triangular and gaussian functions are "+transitionPointLeft+" and "+ transitionPointRight+".");

    }
    
    
    @Override
    public double getFS(double x) 
    {
        if(this.support.contains(x))
        {         
            //first, check for shoulder sets
            if(isLeftShoulder && x<= center) return 1.0;
            if(isRightShoulder && x>=center) return 1.0;

            //if not...
            if(x<=transitionPointLeft)
            {
                return (x-start)/(leftCalculationPoint-start);
            }
            else if (x<=transitionPointRight)
            {
                if(x<=center)
                    return Math.exp(-0.5*Math.pow(((x-center)/spreadForLeft),2));
                else
                    return Math.exp(-0.5*Math.pow(((x-center)/spreadForRight),2));
            }
            else
            {
                return (end-x)/(end-rightCalculationPoint);
            }
        }
        else
            return 0.0;
    }

    @Override
    public double getPeak()
    {
        return this.getMean();
    }
    public double getMean()
    {
        return center;
    }

    public double getStart()
    {
        return start;
    }
    
    public double getEnd()
    {
        return end;
    }

    
    @Override
    public String toString() 
    {
        String s;
        s = this.name+" interiorSet "+start+" "+center+" "+end;
        if(isLeftShoulder) s+=" (LeftShoulder)";
        if(isRightShoulder) s+=" (RightShoulder)";        
        return s;
        
    }
    
    /**
     *Returns the line equation parameters a and be (line equation = ax*b) for a line passing through the points defined by the tuples x and y.
     *@params x The first point, the Tuple consists of the x and y coordinates of the point in this order.
     *@params y The second point, the Tuple consists of the x and y coordinates of the point in this order.
     */ 
    private double[] getLineEquationParameters(Tuple x, Tuple y)
    {
        double[] ab = new double[2];
        if(DEBUG)System.out.println("x = "+x+"   y = "+y);
        ab[0] = (y.getRight()-x.getRight()) / (y.getLeft()-x.getLeft());
        ab[1] = x.getRight()-ab[0]*x.getLeft();
        if(DEBUG)System.out.println("Line equation: "+ab[0]+" * x + "+ab[1]);
        return ab;
    }
    
    /**
     *Returns the x coordinate for a specified y coordinate when considering the given line equation.
     */
    private double getXForYOnLine(double y, double[] ab)
    {
        return (y-ab[1])/ab[0];
    }


    @Override
    public int compareTo(Object o)
    {
        if (!(o instanceof T1MF_Gauangle))
        throw new ClassCastException("A T1MF_Gauangle object is expected for comparison with another T1MF_Triangular object.");

        if(this.isLeftShoulder())
        {
            if(this.getEnd() ==((T1MF_Gauangle)o).getEnd() && ((T1MF_Gauangle)o).isLeftShoulder() && this.getPeak() == ((T1MF_Gauangle)o).getPeak())
                return 0;
            if(this.getEnd() <=((T1MF_Gauangle)o).getEnd() && ((T1MF_Gauangle)o).isLeftShoulder() && this.getPeak() <= ((T1MF_Gauangle)o).getPeak())
                return -1;
            return 1;
        }
        else if (this.isRightShoulder())
        {
            if(((T1MF_Gauangle)o).isRightShoulder() && this.getStart() == ((T1MF_Gauangle)o).getStart() && this.getPeak() == ((T1MF_Gauangle)o).getPeak())
                return 0;
            if(((T1MF_Gauangle)o).isRightShoulder() && this.getStart() <= ((T1MF_Gauangle)o).getStart() && this.getPeak() <= ((T1MF_Gauangle)o).getPeak())
                return -1;
            return 1;
        }
        else
        {
            if(this.getEnd() ==((T1MF_Gauangle)o).getEnd() && this.getStart() == ((T1MF_Gauangle)o).getStart() && this.getPeak() == ((T1MF_Gauangle)o).getPeak())
                return 0;
            if(this.getEnd() <=((T1MF_Gauangle)o).getEnd() && this.getStart() <= ((T1MF_Gauangle)o).getStart() && this.getPeak() <= ((T1MF_Gauangle)o).getPeak())
                return -1;
            return 1;
        }
    }

    @Override
    public Tuple getAlphaCut(double alpha) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
