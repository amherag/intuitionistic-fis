/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package type1.sets;

import generic.BadParameterException;
import generic.Tuple;

/**
 * The T1MF_Cylinder class is mainly a support class for the IT2 class. It serves
 * to implement the cylindrical extension of a firing strength. In terms of a 
 * type-1 MF, a cylinder is in fact just a singleton over the whole universe of 
 * discourse.
 * @author Christian Wagner
 */
public class T1MF_Cylinder extends T1MF_Prototype
{
    private double membershipDegree;
    
    /**
     * Creates a new instance.
     * @param name
     * @param membershipDegree The value for the membership degree of the "cylinder" (for all x).
     */
    public T1MF_Cylinder(String name, double membershipDegree)
    {   
        super(name);
        if(this.membershipDegree<0.0 || this.membershipDegree>1.0)
            throw new BadParameterException("The membership degree should be between 0 and 1."); 
        this.membershipDegree = membershipDegree;
        this.support = new Tuple(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }


    public double getFS(double x) 
    {
        return membershipDegree;
    }

    
    @Override
    public Tuple getAlphaCut(double alpha) 
    {
        if(alpha<=membershipDegree)
            return new Tuple(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        else
            return null;
    }
    
    @Override
    public String toString()
    {
        return this.name+" - Cylindrical extension at :"+this.membershipDegree;
    }    
    
    @Override
    public int compareTo(Object o) 
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }    

    @Override
    public double getPeak() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
