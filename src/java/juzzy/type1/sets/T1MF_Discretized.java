/*
 * T1MF_Discretized.java
 *
 * Created on 08 April 2007, 23:37
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package type1.sets;

import generic.Tuple;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

/**
 * The class allows the specification of a type-1 MF based on single points alone, 
 * i.e. in a discretised fashion. The points are specified using y-x coordinates.
 * All points are held in an ArrayList which requires resorting as points are 
 * added.
 * @author Christian Wagner
 */
public class T1MF_Discretized extends T1MF_Prototype
{
    protected ArrayList<Tuple> set;
    protected double peak;
    protected boolean sorted = false;
    protected int discretizationLevel;
    private boolean leftShoulder = false, rightShoulder = false;
    private double leftShoulderStart = 0.0, rightShoulderStart = 0.0;
    private double a;
    private double temp,temp2,left, right,min;
    private double temp3;

    private final boolean DEBUG = false;
    private int alphaCutDiscLevel = 60;
    private final double alphaCutPrcsisionLimit = 0.01;

    
    /** Creates a new instance of T1MF_Discretized */
    public T1MF_Discretized(String name, int initialSize) 
    {
        super(name);
        set = new ArrayList(initialSize);
        this.support = new Tuple();
    }
    public T1MF_Discretized(String name) 
    {
        super(name);
        set = new ArrayList();
        this.support = new Tuple();
    }    
    public T1MF_Discretized(String name, Tuple[] points)
    {
        super(name);
        this.support = new Tuple();
        set = new ArrayList(points.length);
        this.addPoints(points);
        sort();
    }
    
    /**
     * Adds a point to the discretized set - forces resorting.
     * (add array of points in one go to avoid sorting overhead)
     * @param p 2-D coordinates of the point in the order y,x.
     */
    public void addPoint(Tuple p)
    {
        set.add(p);
        sorted = false;
    }

    /**
     * Adds a series of points to the discretized set - forces resorting.
     * @param p 2-D coordinates of the points in the order y,x.
     */    
    public void addPoints(Tuple[] p)
    {
        set.addAll(Arrays.asList(p));
        sorted = false;
    } 
    
    public int getAlphaCutDisretizationLevel()
    {
        return alphaCutDiscLevel;
    }
    
    public void setAlphaCutDisretizationLevel(int alphaCutDiscLevel)
    {
        this.alphaCutDiscLevel = alphaCutDiscLevel;
    }
    
    public int getNumberOfPoints()
    {
        return set.size();
    }

    public double getFS(double x) 
    {
        if(set==null)
            return -1;    
        if (leftShoulder)
            if(x<leftShoulderStart)
                return 1.0;        
        if(rightShoulder)
            if(x>rightShoulderStart)
                return 1.0;
        if(x<this.getSupport().getLeft()||x>this.getSupport().getRight())
            return 0.0;
            
        //make sure set is sorted
        sort();

        //traverse set from left to right
        for(int i=0;i<set.size();i++)
        {
            if(set.get(i).getRight()>x)
            {
                if(DEBUG){
                    System.out.println("Element at "+x+" was not contained in discretized set - INTERPOLATING!");
                    System.out.println("Set[i-1].getRight = "+set.get(i-1).getRight() +"           Set[i].getRight = "+set.get(i).getRight() + "   i = "+i);
                }
                return interpolate(i-1, x, i);
            }
            else if(set.get(i).getRight()==x)
            {
                return set.get(i).getLeft();
            }
        }
        return Double.NaN;  //we never get here...
    }

    
  /**
     *Returns the x values where the alpha cut using the alpha (y) value provided "cuts" the function curve
     */
    public Tuple getAlphaCut(double alpha)
    {
        double left=0, right=0;
        
        if(alpha == 0.0)
        {
            return this.getSupport();
        }
        
        if(alpha == 1.0)
        {
            //rely on convexity and search from the outside inwards
            for(int i =0;i<set.size();i++)
            {
                if(set.get(i).getLeft()==1.0)
                    left = set.get(i).getRight();
            }
            for(int i=set.size()-1;i>=0;i--)
            {
                if(set.get(i).getLeft()==1.0)
                    right = set.get(i).getRight();
            }            
            return new Tuple(left, right);
        }
        
        //for other alphas between 0 and 1;
        double stepSize = (this.getSupport().getSize())/(alphaCutDiscLevel-1);
        double currentStep = this.getSupport().getLeft();
        Tuple alphaCut = null;        
        findLeft:
            for(int i=0;i<alphaCutDiscLevel;i++)
            {
                temp = this.getFS(currentStep)-alpha;
                if (temp>=0.0)
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
                temp = this.getFS(currentStep)-alpha;
                if (temp>0.0)
                {
                    right = currentStep;
                    break findRight;
                }
                currentStep-= stepSize;
            }                 

        alphaCut = new Tuple(left,right);
        
        //in some cases there might only be 1 single point (if set has 1 slope, say from lower left to top right)
        //account for this and fix problem where due to lack of precision the right would be < than the left.
        if(Math.abs(left-right)<alphaCutPrcsisionLimit) alphaCut.setRight(left);     
        return alphaCut;
    }       
    
    /**
     *Calcuates f(s) for input x through interpolation.
     *@param x_0 Identifier pointing to correct x_0 in set array.
     *@param x_1 Actual x input.
     *@param x_2 Identifier pointing to correct x_2 in set array.
     */
    private double interpolate(int x_0, double x_1, int x_2)
    {
        a = (set.get(x_2).getRight() - set.get(x_0).getRight()) / (x_1 - set.get(x_0).getRight());
        return set.get(x_0).getLeft()-((set.get(x_0).getLeft()-set.get(x_2).getLeft())/a);
    }
    
    /**
     *Returns all points in the set
     */
    public ArrayList getPoints()
    {
        sort();
        return set;
    }
    
    /**
     *Returns point at i
     */
    public Tuple getPointAt(int i)
    {
        sort();
        return set.get(i);
    }    

    /**
     * Returns the xCoordinate of the peak value. If the set has a "flat top",
     * i.e. similar to a trapezoidal MF, then the center of this flat top is
     * returned.
     * Assumes convexity.
     * @return The peak of the set.
     */
    @Override
    public double getPeak() 
    {
        sort();
        double xCoordinateofPeak, yValueAtCurrentPeak, secondX=0.0; //two x's if the set has a flat top
        yValueAtCurrentPeak = this.getPointAt(0).getLeft();
        xCoordinateofPeak = this.getPointAt(0).getRight();
        loop:
        for(int i=1;i<this.getNumberOfPoints();i++)
        {
            if(this.getPointAt(i).getLeft()>yValueAtCurrentPeak)
            {System.out.println("in the loop... currentPeak = "+yValueAtCurrentPeak+"   new point: "+this.getPointAt(i).getLeft());
                yValueAtCurrentPeak = this.getPointAt(i).getLeft();
                xCoordinateofPeak = this.getPointAt(i).getRight();
            }
            else
            {
                if(this.getPointAt(i).getLeft()==yValueAtCurrentPeak)
                {
                    while(this.getPointAt(i).getLeft()==yValueAtCurrentPeak)
                    {
                        secondX = this.getPointAt(i).getRight();
                        i++;
                    }
                    return (xCoordinateofPeak+secondX)/2.0;
                }
                //otherwise stop the loop as the degree of mem. is decreasing
                break loop;
            }
        }
        return xCoordinateofPeak;
    }


    @Override
    public Tuple getSupport()
    {
        if(set==null)
            return null;
        sort();       
        if(leftShoulder)
            support = new Tuple(Double.NEGATIVE_INFINITY,set.get(set.size()-1).getRight());
            else if (rightShoulder)
                support = new Tuple(set.get(0).getRight(),Double.POSITIVE_INFINITY);
                else
                    support = new Tuple(set.get(0).getRight(),set.get(set.size()-1).getRight());
        return support;
    }
    
    public String toString()
    {
        sort(); 
        
        String s="";
        for(int i = 0;i<set.size();i++)
        {
            s+=set.get(i).getLeft()+" / "+set.get(i).getRight()+"\n";
        }
        return s;
    }
    
    /**
     * Sorts the ArrayList holding all points defining the set.
     * No sort is performed if the set is already sorted.
     */
    private void sort()
    {
        //sort and prune (i.e. remove points with more than one degree of
        //membership) if necessary.
        if(!sorted && set!=null)
        {
            //sort
            Collections.sort(set);
            support.setLeft(set.get(0).getRight());
            support.setRight(set.get(set.size()-1).getRight());
            sorted = true;
            
            //prune
            if(set.size()>1);
            double lastX = set.get(0).getRight();
            for(int i=1;i<set.size();i++)
            {
                if(set.get(i).getRight()==lastX)
                {
                    set.get(i-1).setLeft(Math.max(set.get(i-1).getLeft(), set.get(i).getLeft()));
                    set.remove(i);
                    i--;
                }
                else
                    lastX = set.get(i).getRight();
            }
        }
    }
    
    public String writeToFile(String filename)
    { 
        sort();
        try
        {
            //BufferedWriter out = new BufferedWriter(new FileWriter("C:\\GeneralType2_VisualisationTestData.txt"));
            BufferedWriter out = new BufferedWriter(new FileWriter(filename));

            for(int i=0; i<set.size();i++)
            {
                out.write(set.get(i).getRight()+","+set.get(i).getLeft());// x y (where y =  third dimension(actual z))
                out.newLine();
            }

            out.flush();
            out.close();
            return ("Discretized set '"+this.getName()+"' was successfully written to "+filename+".");
        }
        catch (IOException e) 
        {return ("Error while setting up or writing to output file "+filename+".");}      
    }     
    
    /**
     *Uses interpolation to supply high-res visualisation of the set.
     *@param filename The path/name for the output file.
     *@param resolution The number of discretizations to be performed.
     */
    public String writeToFileHighRes(String filename, int resolution)
    { 
        sort();
        try
        {
            //BufferedWriter out = new BufferedWriter(new FileWriter("C:\\GeneralType2_VisualisationTestData.txt"));
            BufferedWriter out = new BufferedWriter(new FileWriter(filename));
            
            double stepSize = (this.getSupport().getRight()-this.getSupport().getLeft())/(resolution-1);
            double currentStep = this.getSupport().getLeft();
            for(int i=0; i<resolution;i++)
            {
                out.write(currentStep+","+this.getFS(currentStep));
                out.newLine();
                currentStep += stepSize;
            }

            out.flush();
            out.close();
            return ("Discretized set '"+this.getName()+"' was successfully written to "+filename+".");
        }
        catch (IOException e) 
        {return ("Error while setting up or writing to output file "+filename+".");}      
    }    
    
    public void setLeftShoulderSet(double shoulderStart)
    {
        leftShoulder = true;
        leftShoulderStart = shoulderStart;
        support.setLeft(Double.NEGATIVE_INFINITY);
    }
    
    public void setRightShoulderSet(double shoulderStart)
    {
        rightShoulder = true;
        rightShoulderStart = shoulderStart;
        support.setRight(Double.POSITIVE_INFINITY);
    }


    /**
     * Returns the defuzzified value of this set computed using the centroid algorithm.
     * @param numberOfDiscretizations The number of discretizations to be employed.
     * The number of discretizations is not used - instead the centroid is
     * computed on all the discrete values in the set.
     */
    @Override
    public double getDefuzzifiedCentroid(int numberOfDiscretizations)
    {
        double stepSize=
                this.getSupport().getAverage()/(numberOfDiscretizations-1);
        double currentStep = this.getSupport().getLeft();

        double numerator = 0.0, denominator = 0.0, fs = 0.0;
        if(DEBUG)System.out.println("number of points: "+this.getPoints().size());

        Iterator<Tuple> i = this.getPoints().iterator();
        Tuple point;
        while(i.hasNext())
        {
            point = i.next();
            numerator += point.getRight() * point.getLeft();
            denominator += point.getLeft();
        }
        if(denominator==0.0) return 0.0;
        else
        return numerator / denominator;
    }

    @Override
    public int compareTo(Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
