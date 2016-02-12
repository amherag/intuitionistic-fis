/*
 * Rule.java
 *
 * Created on 19 November 2008, 11:37
 *
 * Copyright 2008 Christian Wagner All Rights Reserved.
 */

package type1.system;

import generic.Input;
import generic.Output;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

import type1.sets.T1MF_Gaussian;
import type1.sets.T1MF_Interface;
import type1.sets.T1MF_Singleton;

/**
 * Rule for a Type-1 Fuzzy System.
 * @author Christian Wagner
 */
public class T1_Rule implements Serializable
{

    private T1_Antecedent[] antecedents;
    private HashMap<Output, T1_Consequent> consequents;
    
    private final boolean DEBUG = false;
    private final byte PRODUCT = 0;
    private final byte MINIMUM = 1;
    
    
    /**
     * Creates a new instance of Rule with a single consequent
     * @param antecedents The array of antecedents
     * @param consequent The consequent (only a single consequent is supported here)
     */
    public T1_Rule(T1_Antecedent[] antecedents, T1_Consequent consequent) {
        this.antecedents = antecedents;
        this.consequents  = new HashMap(1);
        this.consequents.put(consequent.getOutput(), consequent);
    }
    
    /**
     * Creates a new instance of Rule
     * @param antecedents The array of antecedents
     * @param consequents The array of consequents 
     */
    public T1_Rule(T1_Antecedent[] antecedents, T1_Consequent[] consequents) {
        this.antecedents = antecedents;
        this.consequents  = new HashMap(consequents.length);
        for(int i=0;i<consequents.length;i++)
            this.consequents.put(consequents[i].getOutput(), consequents[i]);        
    }    
    
    public int getNumberOfAntecedents()
    {
        return antecedents.length;
    }
    public int getNumberOfConsequents()
    {
        return consequents.size();
    }
    public T1_Antecedent[] getAntecedents()
    {
        return antecedents;
    }
    
    public T1_Consequent[] getConsequents()
    {
        T1_Consequent[] cons = new T1_Consequent[consequents.size()];
        consequents.values().toArray(cons);
        return cons;
    }

    
    /**
     * 
     * @return An iterator over the consequents included in this rule.
     */
    public Iterator<T1_Consequent> getConsequentsIterator()
    {
        return consequents.values().iterator();
    }
    
    /**
     * Returns the inputs of the antecedents used in the current rule.
     */
    public Input[] getInputs()
    {
       Input[] inputs = new Input[this.getNumberOfAntecedents()];
       for (int i=0;i<this.getNumberOfAntecedents();i++)
           inputs[i] = this.getAntecedents()[i].getInput();

       return inputs;
    }
    
    
    /**
     *Performs a comparison operation by comparing the rule objects solely based 
     * on their antecedents. The method returns true of the antecedents of both
     * rules are the same.
     */
    public boolean compareBasedOnAntecedents(T1_Rule r)
    {
        if(this.getNumberOfAntecedents()==r.getNumberOfAntecedents())
        {
            for (int i=0;i<this.getNumberOfAntecedents();i++)
            {             
                if(this.getAntecedents()[i].compareTo(r.getAntecedents()[i])!=0)
                    return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Returns the rule's firing strength. The method relies on the transparent 
     * updating of the inputs of the fuzzy system through the Input classes 
     * attached to the antecedents.
     * @param tNorm Either product (0) or minimum (1) is currently supported.
     * @return The firing strength.
     */
    public double getFStrength(byte tNorm)
    {
        double fStrength = 1.0;	//initialize for multiplication

        if(tNorm==PRODUCT)
        {

            for(int i = 0;i<antecedents.length;i++)
            {
            	if (antecedents[i].getInput().getInputMF() instanceof T1MF_Singleton) {
            		if(DEBUG)System.out.println("Antecedent "+i+" gives a FS of: "+antecedents[i].getFS()+" with an input of: "+antecedents[i].getInput().getInput());
            		fStrength*=antecedents[i].getFS();
            	} else {
            		double xmax = antecedents[i].getMax(0);
            		fStrength *= ((T1MF_Interface) antecedents[i].getInput().getInputMF()).getFS(xmax)*antecedents[i].getMF().getFS(xmax);          
            	}
            }
        }
        else    //use minimum
        {
            for(int i = 0;i<antecedents.length;i++)
            {
            	if (antecedents[i].getInput().getInputMF() instanceof T1MF_Singleton) {
            		if(DEBUG)System.out.println("Antecedent "+i+" gives a FS of: "+antecedents[i].getFS()+" with an input of: "+antecedents[i].getInput().getInput());
            		fStrength=Math.min(fStrength,antecedents[i].getFS());
            	} else {
            		double xmax = antecedents[i].getMax(1);
            		fStrength = Math.min(fStrength,Math.min(((T1MF_Interface) antecedents[i].getInput().getInputMF()).getFS(xmax),antecedents[i].getMF().getFS(xmax)));
            	}
            }
        } 
        
        return fStrength;
    }


    @Override
    public String toString()
    {
        String s;
        s="IF ";
        for (int i=0;i<this.getNumberOfAntecedents();i++)
        {
            s+=this.getAntecedents()[i].getName()+" ";
            if((i+1)<this.getNumberOfAntecedents())
                s+="AND ";
            else
                s+="THEN ";
        }
        for (int i=0;i<this.getNumberOfConsequents();i++)
        {
            s+= this.getConsequents()[i].getName()+" ";
            if((i+1)<this.getNumberOfConsequents())
                s+="AND ";
        }
        return s;
    }
}
