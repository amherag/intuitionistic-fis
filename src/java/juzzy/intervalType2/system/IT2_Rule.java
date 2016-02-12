/*
 * IT2_Rule.java
 *
 * Copyright 2007-2013 Christian Wagner All Rights Reserved.
 */
package intervalType2.system;

import generic.Input;
import generic.MF_Interface;
import generic.Output;
import generic.Tuple;
import intervalType2.sets.IntervalT2MF_Interface;

import java.util.HashMap;
import java.util.Iterator;

import type1.sets.T1MF_Interface;
import type1.sets.T1MF_Singleton;

/**
 * Rule class for Interval Type-2 FLSs. Note that currently only a single
 * consequent per rule is supported.
 * @author chwagn
 */
public class IT2_Rule
{
    private IT2_Antecedent[] antecedents;
    private HashMap<Output, IT2_Consequent> consequents;
    
    private final byte PRODUCT = 0;
    private final byte MINIMUM = 1;    

    /**
    * Standard constructor for a single consequent.
    * @param antecedents Array of antecedents.
    * @param consequent The consequent.
    */
    public IT2_Rule(IT2_Antecedent[] antecedents, IT2_Consequent consequent)
    {
        this.antecedents = antecedents;
        this.consequents  = new HashMap(1);
        this.consequents.put(consequent.getOutput(), consequent);
    }
    
    /**
    * Standard constructor.
    * @param antecedents Array of antecedents.
    * @param consequents Array of consequents.
    */
    public IT2_Rule(IT2_Antecedent[] antecedents, IT2_Consequent[] consequents)
    {
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
        //return consequents.length;
        return 1;
    }
    public IT2_Antecedent[] getAntecedents()
    {
        return antecedents;
    }
    public IT2_Consequent[] getConsequents()
    {
        IT2_Consequent[] cons = new IT2_Consequent[consequents.size()];
        consequents.values().toArray(cons);
        return cons;
    }
    
    /**
     * 
     * @return An iterator over the consequents included in this rule.
     */
    public Iterator<IT2_Consequent> getConsequentsIterator()
    {
        return consequents.values().iterator();
    }    

    /**
     * Returns the rule's firing strength. The method relies on the transparent 
     * updating of the inputs of the fuzzy system through the Input classes 
     * attached to the antecedents.
     * @param tNorm Either product (0) or minimum (1) is currently supported.
     * @return The firing strength as a (lower, upper) tuple.
     */
    public Tuple getFStrength(byte tNorm)
    {
        Tuple fStrength = new Tuple(1.0,1.0);	//initialize for multiplication
        
        if(tNorm==PRODUCT)
        {
            //mutliply antecedents (left and right)
            for(int i = 0;i<antecedents.length;i++)
            {
            	if (antecedents[i].getInput().getInputMF() instanceof T1MF_Singleton) {
                    fStrength.setLeft(fStrength.getLeft()*antecedents[i].getFS().getLeft());
                    fStrength.setRight(fStrength.getRight()*antecedents[i].getFS().getRight());
            	} else if (antecedents[i].getInput().getInputMF() instanceof T1MF_Interface) {
            		Tuple xmax = antecedents[i].getMax(PRODUCT);
            		fStrength.setLeft(fStrength.getLeft()*antecedents[i].getMF().getLMF().getFS(xmax.getLeft())*((T1MF_Interface) antecedents[i].getInput().getInputMF()).getFS(xmax.getLeft()));
                    fStrength.setRight(fStrength.getRight()*antecedents[i].getMF().getUMF().getFS(xmax.getRight())*((T1MF_Interface) antecedents[i].getInput().getInputMF()).getFS(xmax.getRight()));
            	} else {//IntervalT2MF
            		Tuple xmax = antecedents[i].getMax(PRODUCT);
            		fStrength.setLeft(fStrength.getLeft()*antecedents[i].getMF().getLMF().getFS(xmax.getLeft())*((IntervalT2MF_Interface) antecedents[i].getInput().getInputMF()).getLMF().getFS(xmax.getLeft()));
                    fStrength.setRight(fStrength.getRight()*antecedents[i].getMF().getUMF().getFS(xmax.getRight())*((IntervalT2MF_Interface) antecedents[i].getInput().getInputMF()).getUMF().getFS(xmax.getRight()));           	
            	}
            }
        }
        else    //use minimum
        {
        	for(int i = 0;i<antecedents.length;i++)
            {
            	if (antecedents[i].getInput().getInputMF() instanceof T1MF_Singleton) {
                    fStrength.setLeft(Math.min(fStrength.getLeft(),antecedents[i].getFS().getLeft()));
                    fStrength.setRight(Math.min(fStrength.getRight(),antecedents[i].getFS().getRight()));
            	} else if (antecedents[i].getInput().getInputMF() instanceof T1MF_Interface) {
            		Tuple xmax = antecedents[i].getMax(MINIMUM);
            		fStrength.setLeft(Math.min(fStrength.getLeft(),Math.min(antecedents[i].getMF().getLMF().getFS(xmax.getLeft()),((T1MF_Interface) antecedents[i].getInput().getInputMF()).getFS(xmax.getLeft()))));
                    fStrength.setRight(Math.min(fStrength.getRight(),Math.min(antecedents[i].getMF().getUMF().getFS(xmax.getRight()),((T1MF_Interface) antecedents[i].getInput().getInputMF()).getFS(xmax.getRight()))));
            	} else {//IntervalT2MF
            		Tuple xmax = antecedents[i].getMax(MINIMUM);
            		fStrength.setLeft(Math.min(fStrength.getLeft(),Math.min(antecedents[i].getMF().getLMF().getFS(xmax.getLeft()),((IntervalT2MF_Interface) antecedents[i].getInput().getInputMF()).getLMF().getFS(xmax.getLeft()))));
                    fStrength.setRight(Math.min(fStrength.getRight(),Math.min(antecedents[i].getMF().getUMF().getFS(xmax.getRight()),((IntervalT2MF_Interface) antecedents[i].getInput().getInputMF()).getUMF().getFS(xmax.getRight()))));           	
            	}
            }
        }
        
        return fStrength;
    }

    /**
     * Returns the inputs of the antecedents used in the current rule.
     * @return 
     */
    public Input[] getInputs()
    {
        Input[] inputs = new Input[this.getNumberOfAntecedents()];
        for (int i=0;i<this.getNumberOfAntecedents();i++)
            inputs[i] = this.getAntecedents()[i].getInput();

        return inputs;
    }

    
    public Tuple getConsequentCentroid(Output o)
    {
        return consequents.get(o).getCentroid();
    }

    /**
     *Performs a comparison operation by comparing the rule objects solely based on they antecedents.
     */
    public boolean compareBasedOnAntecedents(IT2_Rule r)
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
        else 
            return false;
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
