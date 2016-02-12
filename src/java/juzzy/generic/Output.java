/*
 * Output.java
 *
 * Created on 31 July 2013, 14:14
 *
 * Author: Christian Wagner
 * Copyright 2013 Christian Wagner All Rights Reserved.
 */
package generic;

/**
 * The output class enables the labelling of an output. It also captures the 
 * allowable domain for a given output.
 * @author Christian Wagner
 */
public class Output implements Comparable
{
    private String name;
    private Tuple domain;
    private int discretisationLevel = 100;  //the number of discretisations this output will be evaluated over
    private double[] discretisedDomain = null;
    
    public Output(String name, Tuple domain)
    {
        this.name = name;
        this.domain = domain;
    }
    
    public Output(String name, Tuple domain, int discretisationLevel)
    {
        this.name = name;
        this.domain = domain;
        this.discretisationLevel = discretisationLevel;
    }    

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDiscretisationLevel() {
        return discretisationLevel;
    }

    public void setDiscretisationLevel(int discretisationLevel) {
        this.discretisationLevel = discretisationLevel;
    }

    public Tuple getDomain() {
        return domain;
    }

    public void setDomain(Tuple domain) {
        this.domain = domain;
    }
    
    /**
     * Returns an array with discrete values over the domain of this output. This 
     * discrete array is buffered in the Output object, i.e. if the same discretisation 
     * is kept, it is efficient to use the array from the output object (e.g. in rule-based
     * inference).
     * @param numberOFDiscretizations
     * @return 
     */
    public double[] getDiscretizations()
    {
        if(discretisedDomain==null || discretisedDomain.length != discretisationLevel)
        {
            discretisedDomain = new double[discretisationLevel];
            double stepsize = domain.getSize()/(discretisationLevel-1.0);
            discretisedDomain[0] = domain.getLeft();
            discretisedDomain[discretisationLevel-1] = domain.getRight();
            for(int i=1;i<discretisationLevel-1;i++)
            {
                discretisedDomain[i] = domain.getLeft()+i*stepsize;
            }
            return discretisedDomain;
        }
        else
        return discretisedDomain;    
    }

    /**
     * Enables simple name-based ordering of outputs.
     * This method is solely used to maintain an ordering of outputs.
     * @param o
     * @return 
     */
    @Override
    public int compareTo(Object o) 
    {
            return this.name.compareTo(((Output)o).getName());
    }
    
}
