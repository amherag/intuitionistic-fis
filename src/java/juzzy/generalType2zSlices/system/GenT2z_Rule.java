/*
 * GenT2z_Rule.java
 *
 * Author: Christian Wagner
 * Copyright 2006 Christian Wagner All Rights Reserved.
 */

package generalType2zSlices.system;

import generalType2zSlices.sets.GenT2zMF_CylExtension;
import generalType2zSlices.sets.GenT2zMF_Interface;
import generalType2zSlices.sets.GenT2zMF_Intersection;
import generalType2zSlices.sets.GenT2zMF_Prototype;
import generic.Input;
import generic.Output;
import generic.Tuple;
import intervalType2.sets.IntervalT2MF_Interface;
import intervalType2.system.IT2_Antecedent;
import intervalType2.system.IT2_Consequent;
import intervalType2.system.IT2_Rule;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;

import type1.sets.T1MF_Discretized;
import type1.sets.T1MF_Interface;
import type1.sets.T1MF_Meet;
import type1.sets.T1MF_Singleton;


/**
 *
 * @author Christian Wagner
 */
public class GenT2z_Rule
{
    private GenT2z_Antecedent[] antecedents;
    private HashMap<Output, GenT2z_Consequent> consequents;
    //private T1MF_DiscretizedIntersection fs;
    private T1MF_Meet fs;
    private GenT2zMF_CylExtension cylExt;
    private GenT2zEngine_Intersection gzEI;
    
    private final boolean DEBUG = false;
    
    /** 
     * Creates a new instance of GenT2_Rule with a single consequent.
     * Currently only "AND" is supported as logical connective.
     */
    public GenT2z_Rule(GenT2z_Antecedent[] antecedents, GenT2z_Consequent consequent)
    {
        this.antecedents = antecedents;
        this.consequents  = new HashMap(1);
        this.consequents.put(consequent.getOutput(), consequent);
        if(DEBUG)
        {
            System.out.println("Rule antecedent's inputs:");
            for(int i=0;i<this.antecedents.length;i++)
            {
                System.out.println("input: "+antecedents[i].getInput());
                System.out.println(this.antecedents[i].getInput());
            }
            System.out.println("");
        }
        this.gzEI = new GenT2zEngine_Intersection();    //inefficient
    }
    
    /** 
     * Creates a new instance of GenT2_Rule with one or more consequent(s).
     * Currently only "AND" is supported as logical connective.
     */
    public GenT2z_Rule(GenT2z_Antecedent[] antecedents, GenT2z_Consequent[] consequents)
    {
        this.antecedents = antecedents;
        this.consequents  = new HashMap();
        for(int i=0;i<consequents.length;i++)
            this.consequents.put(consequents[i].getOutput(), consequents[i]);
        if(DEBUG)
        {
            System.out.println("Rule antecedent's inputs:");
            for(int i=0;i<this.antecedents.length;i++)
            {
                System.out.println("input: "+antecedents[i].getInput());
                System.out.println(this.antecedents[i].getInput());
            }
            System.out.println("");
        }
        this.gzEI = new GenT2zEngine_Intersection();    //inefficient
    }    
    
    /**********************************************
     *
     *      BELOW: OR WOULD IT BE MORE SUITABLE TO CALCULATE THE INTERSECTION OF ALL ANTECEDENTS AT THE BEGINNING AND THEN 
     *      SAMPLE THE RESULTING GEN T2 INTERSECTION SET???
     */
    
    /**
     * 
     * @return An iterator over the consequents included in this rule.
     */
    public Iterator<GenT2z_Consequent> getConsequentsIterator()
    {
        return consequents.values().iterator();
    }        
    
    /**
     *Returns the firing strength of the rule
     */
    private T1MF_Interface getFS()
    {
        if(DEBUG) for(int i=0;i<this.antecedents.length;i++) System.out.println(antecedents[i].getInput());
        
        if(antecedents.length==1)
        {//System.out.println("--------- Rule FS = "+antecedents[0].getFS());
        	return antecedents[0].getFS();
            //return new T1MF_DiscretizedIntersection(antecedents[0].getFS(), antecedents[0].getFS());
        }
        else
        {
            fs = new T1MF_Meet(antecedents[0].getFS(), antecedents[1].getFS());
            if(!fs.intersectionExists()) 
            {
                fs = null;
            }
            else
            {
    //            fs = new T1MF_DiscretizedIntersection(antecedents[0].getFS(), antecedents[1].getFS());
                //System.out.println("antecedents[0].getFS() = "+antecedents[0].getFS() +"      antecedents[1].getFS() = "+antecedents[1].getFS());
                for(int i = 2;i<antecedents.length;i++)
                {
    //                fs = new T1MF_DiscretizedIntersection(fs,antecedents[i].getFS());
                    fs = new T1MF_Meet(fs,antecedents[i].getFS());
                }            
                //System.out.println("zSlice firing domain: "+fs.getSupport());
            
            }
            if(!(fs == null) &&fs.intersectionExists())
                return fs;
            else
                return null;
                 
        }
        
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
    
     /**
     * Returns the output associated with the (currently single) consequent.
     * @return 
     */
    public Output getOutput()
    {
        return this.getConsequents()[0].getOutput();
    }
    
    public TreeMap<Output,GenT2zMF_Intersection> getRawOutput()
    {
        TreeMap<Output,GenT2zMF_Intersection> returnValue = new TreeMap();
        Iterator<GenT2z_Consequent> it = consequents.values().iterator();
        GenT2z_Consequent con;
        Output o;
        T1MF_Interface baseSet = this.getFS();        
        while(it.hasNext())
        {
            con = it.next();
            o = con.getOutput();
            {
                if(baseSet!=null)
                {            
                    cylExt = new GenT2zMF_CylExtension(baseSet,antecedents[0].getSet().getNumberOfSlices());

                    returnValue.put(o, gzEI.getIntersection(cylExt,con.getSet()));
                }
                else
                    returnValue.put(o, gzEI.getIntersection(cylExt,null));
            }          
        }
        return returnValue;
    }

    /**
     * Produces a series of interval type-2 rules where each rule represents one
     * zLevel of the underlying zSlices rule.
     * CURRENTLY ONLY ONE CONSEQUENT IS SUPPORTED!
     * @return
     */
    public IT2_Rule[] getRuleasIT2Rules()
    {
        IT2_Rule[] rs = new IT2_Rule[this.getAntecedents()[0].getSet().getNumberOfSlices()];    //use the first set to check how many slices we have
        Iterator<GenT2z_Consequent> it;
        int c;

        for(int i=0;i<rs.length;i++)
        {
            IT2_Antecedent[] as = new IT2_Antecedent[antecedents.length];
            IT2_Consequent[] cs = new IT2_Consequent[consequents.size()];
            for(int a=0;a<as.length;a++)
            {
                as[a] = this.getAntecedents()[a].getAntecedentasIT2Sets()[i];
                if (this.getAntecedents()[a].getInput().getInputMF() instanceof GenT2zMF_Interface) {
                	Input temp = as[a].getInput();
                	Tuple domain = temp.getDomain();
                	String nameInput = temp.getName();
                	IntervalT2MF_Interface mf = ((GenT2zMF_Prototype) temp.getInputMF()).getZSlice(i);               	
                	as[a].setInput(new Input(nameInput, domain, mf));
                }
            }
            it = consequents.values().iterator();
            c=0;
            while(it.hasNext())
            {
                cs[c] = it.next().getConsequentsIT2Sets()[i];
                c++;
            }
            rs[i] = new IT2_Rule(as,cs);
        }

        return rs;
    }

    public GenT2z_Antecedent[] getAntecedents() {
        return antecedents;
    }

    public GenT2z_Consequent[] getConsequents() {
        GenT2z_Consequent[] cons = new GenT2z_Consequent[consequents.size()];
        consequents.values().toArray(cons);
        return cons;
    }
    
    public Iterator<GenT2z_Consequent> getConsequentIterator()
    {
        return consequents.values().iterator();
    }

    public int getNumberOfAntecedents()
    {
        return antecedents.length;
    }
    public int getNumberOfConsequents()
    {
        return consequents.size();
    }    

    public boolean equals(Object rule)
    {
        if ( this == rule ) return true;
        if ( !(rule instanceof GenT2z_Rule) ) return false;
        GenT2z_Rule myRule = (GenT2z_Rule)rule;

        boolean isEqual = true;
        boolean temp;
        for(int ants=0;ants<this.getAntecedents().length;ants++)
        {
            temp = false;
            for(int i=0;i<myRule.getAntecedents().length;i++)
            {
                if(this.getAntecedents()[ants].equals(myRule.getAntecedents()[i]))
                {
                    temp = true;
                }
            }
            isEqual&=temp;
        }
        for(int cons=0;cons<this.getConsequents().length;cons++)
        {
            temp = false;
            for(int i=0;i<myRule.getConsequents().length;i++)
            {
                if(this.getConsequents()[cons].equals(myRule.getConsequents()[i]))
                {
                    temp = true;
                }
            }
            isEqual&=temp;
        }
        return isEqual;
    }

    public String toString()
    {
        String s="IF ";
        for(int i=0;i<getAntecedents().length;i++)
        {
            s+=getAntecedents()[i].getName(); //use the built in toString method
            if(i<getAntecedents().length-1)
                s+=" AND ";
            else
                s+=" THEN ";
        }
        for(int i=0;i<getConsequents().length;i++)
        {
            s+=getConsequents()[i].getName(); //use the built in toString method
            if(i<getAntecedents().length-1)
                s+=" ";
        }
        return s;
    }
    
}
