/*
 * GenT2z_Antecedent.java
 *
 * Created on 16 May 2007, 11:21
 *
 * Author: Christian Wagner
 * Copyright 2006 Christian Wagner All Rights Reserved.
 */

package generalType2zSlices.system;

import generalType2zSlices.sets.GenT2zMF_Interface;
import generic.Input;
import intervalType2.system.IT2_Antecedent;
import type1.sets.T1MF_Interface;


/**
 *
 * @author Christian Wagner
 */
public class GenT2z_Antecedent
{
    private Input input;
    private String name;
    private GenT2zMF_Interface set;
    private final boolean DEBUG = false;
    
    /** 
     * Creates a new instance of Antecedent which uses an Input object.
     * 
     */
    public GenT2z_Antecedent(String name, GenT2zMF_Interface set, Input input)
    {
        this.name = name;
        this.input = input;
        this.set = set;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public T1MF_Interface getFS()
    {
        if(set.getSupport().contains(input.getInput()))
        {
            return set.getFS(input.getInput());
        }
        else
        {
            return null;
        }
    }
    
    public GenT2zMF_Interface getSet()
    {
        return set;
    }

    /**
     * Returns this antecedent as a series of antecedents (each based on a single
     * zSlice) for interval type-2 FLSs.
     * @return 
     */
    public IT2_Antecedent[] getAntecedentasIT2Sets()
    {
        IT2_Antecedent[] ants = new IT2_Antecedent[this.getSet().getNumberOfSlices()];
        for(int i=0;i<ants.length;i++)
        {
            ants[i] = new IT2_Antecedent(this.getName()+"_zSlices:"+i,this.getSet().getZSlice(i),this.getInput());
        }
        return ants;
    }
    
    public Input getInput()
    {
        return input;
    }

    public boolean equals(Object antecedent)
    {
        if ( this == antecedent ) return true;
        if ( !(antecedent instanceof GenT2z_Antecedent) ) return false;
        GenT2z_Antecedent myAntecedent = (GenT2z_Antecedent)antecedent;
        return this.getSet()==myAntecedent.getSet();
    }

    public String toString()
    {
        return "Antecedent_for:"+this.getSet().getName();
    }
    
}
