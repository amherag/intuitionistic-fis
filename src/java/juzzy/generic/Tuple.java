package generic;

import java.io.Serializable;
/*
 * Range.java
 *
 * Created on 07 February 2006, 12:35
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

/**
 * Stores 2 values which are usually related in some way, such as a range,...
 * @author Unreal
 */
public class Tuple implements Comparable, Serializable
{
    private double left,right;
    
    /** Creates a new instance of Centroid */
    public Tuple() 
    {
        left = 0.0;
        right = 0.0;
    }
    
    public Tuple(double l, double r)
    {
        left = l;
        right = r;
    }
    
    public Tuple clone()
    {
        return new Tuple(left, right);
    }
    
    public void setLeft(double l)
    {
        left = l;
    }
    
    public void setRight(double r)
    {
        right = r;
    }

    /**
     * Sets the left and right points of this tuple to that of tuple t.
     * @param t
     */
    public void setEqual(Tuple t)
    {
        this.left = t.getLeft();
        this.right = t.getRight();
    }
    
    public double getLeft()
    {
        return left;
    }
    
    public double getRight()
    {
        return right;
    }

    public double getAverage()
    {
        return (left+right)/2.0;
    }
    
    /**
     * Returns true if the parameter falls within the interval defined by the Tuple.
     * @param x
     * @return 
     */
    public boolean contains(double x)
    {
        return (x>=left && x<=right);
    }

    public double getSize()
    {
        return right-left;
    }
    
    public String toString()
    {
        return("left = "+left+" and right = "+right);
    }
    
    /**
     *Adds an existing tuple to the current tuple by adding their left and right members respectively.
     */
    public Tuple add(Tuple x)
    {
        return new Tuple(this.getLeft()+x.getLeft(),this.getRight()+x.getRight());
    }
    

    /**
     *Comparison is done using right member of tuple!.
     */
    public int compareTo(Object o)
    {
//        if(this ==null) System.out.println("This is null");
//        if(o ==null) return 1;
//        Tuple t = (Tuple)o;
        if (this.getRight() < ((Tuple)o).getRight()) return -1;
        else if (this.getRight() > ((Tuple)o).getRight()) return 1;
                //if rights are the same, compare lefts
        else if(this.getLeft()<((Tuple)o).getLeft()) return -1;
        else if(this.getLeft()>((Tuple)o).getLeft()) return 1;
        else return 0;
    }    
}
