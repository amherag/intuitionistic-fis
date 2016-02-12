/*
 * Consequent.java
 *
 * Created on 16 May 2007, 11:21
 *
 * Author: Christian Wagner
 * Copyright 2006 Christian Wagner All Rights Reserved.
 */

package generalType2zSlices.system;

import generalType2zSlices.sets.GenT2zMF_Interface;
import generic.Output;
import generic.Tuple;
import intervalType2.system.IT2_Consequent;

/**
 *
 * @author Christian Wagner
 */
public class GenT2z_Consequent
{
    private GenT2zMF_Interface set;
    private String name;
    private Output output;
  //  private Tuple setTR;    //type reduced set
    private final boolean cacheTypeReducedCentroid = false;

    private final boolean DEBUG = false;
    
    /** Creates a new instance of Consequent */
    public GenT2z_Consequent(String name, GenT2zMF_Interface set, Output output, GenT2zEngine_Defuzzification GT2zED)
    {
        this.set = set;
        this.name = name;
        this.output = output;
        
        //ensure the domain of the MF is constrained to the domain of the overall system output
        set.setSupport(new Tuple(
                Math.max(set.getSupport().getLeft(), this.output.getDomain().getLeft()), 
                Math.min(set.getSupport().getRight(), this.output.getDomain().getRight()))); 

        
        //bit wasteful here share one instance accross all consequents...?
        //GenT2zEngine_Defuzzification GTZED = new GenT2zEngine_Defuzzification();
    }
    
    public GenT2zMF_Interface getSet()
    {
        return set;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Output getOutput() {
        return output;
    }

    public void setOutput(Output output) {
        this.output = output;
    }
  

    /**
     * Returns this antecedent as a series of consequents (each based on a single
     * zSlice) for interval type-2 FLSs.
     * @return 
     */    
    public IT2_Consequent[] getConsequentsIT2Sets()
    {
        IT2_Consequent[] cons = new IT2_Consequent[this.getSet().getNumberOfSlices()];
        for(int i=0;i<cons.length;i++)
        {
            if(DEBUG)System.out.println("Retrieving consequent slice number: "+i+" of "+cons.length);
            cons[i] = new IT2_Consequent(this.name+"_zSlices:"+i,this.getSet().getZSlice(i), this.getOutput());
        }

        return cons;
    }

    public boolean equals(Object consequent)
    {
        if ( this == consequent ) return true;
        if ( !(consequent instanceof GenT2z_Antecedent) ) return false;
        GenT2z_Antecedent myConsequent = (GenT2z_Antecedent)consequent;

        return this.getSet()==myConsequent.getSet();
    }

    public String toString()
    {
        return this.getSet().getName();
    }
}
