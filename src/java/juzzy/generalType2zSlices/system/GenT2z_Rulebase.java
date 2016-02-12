/*
 * GenT2_Rulebase.java
 *
 * Author: Christian Wagner
 * Copyright 2006 Christian Wagner All Rights Reserved.
 */

package generalType2zSlices.system;

import generalType2zSlices.sets.GenT2zMF_Interface;
import generalType2zSlices.sets.GenT2zMF_Intersection;
import generic.BadParameterException;
import generic.Output;
import generic.Tuple;
import intervalType2.system.IT2_Rulebase;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.TreeMap;

/**
 *
 * @author Christian Wagner
 */
public class GenT2z_Rulebase
{
    private ArrayList<GenT2z_Rule> rules;
    private static GenT2zEngine_Union gzEU;
    private static GenT2zEngine_Intersection gzEI;
    private ArrayList<Output> outputs;
    private GenT2zMF_Interface output=null;
    private GenT2zMF_Intersection temp1, temp2;
    private final byte CENTEROFSETS = 0;
    private final byte CENTROID = 1;
//    private byte inferenceMethod = 0;   //nopt used ATM
    private byte implicationMethod = 1;    
    private final byte PRODUCT = 0;
    private final byte MINIMUM = 1;        
    
    private final boolean DEBUG = false;
    private final boolean showContext = false;
    
    /** Creates a new instance of GenT2_Rulebase */
    public GenT2z_Rulebase(int initialNumberOfRules)
    {
        this.rules = new ArrayList<GenT2z_Rule>(initialNumberOfRules);
        gzEU = new GenT2zEngine_Union();
        gzEI = new GenT2zEngine_Intersection();
        outputs = new ArrayList();
        rules = new ArrayList();
    }

    public void addRule(GenT2z_Rule rule)
    {
        rules.add(rule);
        //check if any new outputs occur
        Iterator<GenT2z_Consequent> it = rule.getConsequentsIterator(); 
        Output o;
        while(it.hasNext())
        {
            o=it.next().getOutput();
            if(!outputs.contains(o))
                outputs.add(o);
        }        
    }
    
    public void addRules(GenT2z_Rule[] r)
    {
        for(int i=0;i<r.length;i++)
        {
            rules.add(r[i]);
            //check if any new outputs occur
            Iterator<GenT2z_Consequent> it = r[i].getConsequentsIterator();
            Output o;
            while(it.hasNext())
            {
                o=it.next().getOutput();
                if(!outputs.contains(o))
                    outputs.add(o);
            }                
        }
    }    
    
    public ArrayList<GenT2z_Rule> getRules()
    {
        return rules;
    }

    /**
     * Returns the type of fuzzy logic that is employed.
     * @return 0: type-1, 1: interval type-2, 2: zSlices based general type-2
     */
    public int getFuzzyLogicType()
    {
        return 2;
    }
    
    public GenT2zEngine_Intersection get_GenT2zEngine_Intersection()
    {
        return gzEI;
    }
    public GenT2zEngine_Union get_GenT2zEngine_Union()
    {
        return gzEU;
    }
    
    public TreeMap<Output, GenT2zMF_Interface> getOverallOutput()
    {
        TreeMap<Output, GenT2zMF_Interface> returnValue = new TreeMap();
        TreeMap<Output, GenT2zMF_Intersection> temp;
        
        //if(rules.size()==1)
        for(int r=0;r<rules.size();r++)
        {
            
            temp = rules.get(r).getRawOutput(); 
            Iterator<Output> it = outputs.iterator();
            Output o;
            while(it.hasNext())
            {
                o = it.next();
                if(r==0)
                {
                    returnValue.put(o, temp.get(o));
                }
                else
                {
                    returnValue.put(o, gzEU.getUnion(returnValue.get(o),temp.get(o)));
                }
            }
        }
        return returnValue;
    }

    /**
     * Returns the output of the FLS after type-reduction, i.e. the centroid.
     * @param typeReductionType
     * @return A TreeMap where Output is used as key and the value is an Object[]
     *  where Object[0] is a Tuple[] (the centroids, one per zLevel) and Object[1] is a Double holding
     * the associated yValues for the centroids. If not rule fired for the given input(s),
     * then null is returned as an Object[].
     */
    public TreeMap<Output, Object[]> evaluateGetCentroid(int typeReductionType)
    {
        TreeMap<Output, Object[]> returnValue = new TreeMap();
        IT2_Rulebase[] rbsIT2 = getIT2Rulebases();
        TreeMap<Output, Tuple> typeReductionOutput = null;
        double[] zValues = rules.get(0).getAntecedents()[0].getSet().getZValues();
        
        //compute the outputs of each zLevel - later the zLevel weigh will be applied to these outputs.
        ArrayList<TreeMap<Output, Object[]>> rawOutputValues = new ArrayList(rbsIT2.length);
        TreeMap<Output, Object[]> temp;
        for(int i=0;i<rbsIT2.length;i++)    //for each zLevel
        {
            temp = rbsIT2[i].evaluateGetCentroid(typeReductionType);
            
            //add to output
            Output o;
            Iterator<Output> it = temp.keySet().iterator();
            {
                while(it.hasNext())
                {
                    o = it.next();
                    //for the first IT2 rulebase - add the outputs
                    if(i==0)
                    {
                        returnValue.put(o, new Object[]{new Tuple[rbsIT2.length], new double[rbsIT2.length]});
                    }
                    ((Tuple[])returnValue.get(o)[0])[i] = (Tuple)temp.get(o)[0];    //the centroid
                    ((double[])returnValue.get(o)[1])[i] = zValues[i];  //the yValue for the centroid
                }
            }
        }
        return returnValue;  
    }    
    
    /**
     * The current evaluate function is functional but inefficient. It creates an IT2
     * version of all the rules in the rulebase and computes each IT2 rule separately...
     * @param typeReductionType 0: Center Of Sets, 1: Centroid
     * @param discretizationLevel The discretization level on the xAxis
     * @return
     */
    public TreeMap<Output, Double> evaluate(int typeReductionType)
    {
        TreeMap<Output, Double> returnValue = new TreeMap();
        IT2_Rulebase[] rbsIT2 = getIT2Rulebases();

        //compute the outputs of each zLevel - later the zLevel weigh will be applied to these outputs.
        ArrayList<TreeMap<Output, Double>> rawOutputValues = new ArrayList(rbsIT2.length);

        for(int i=0;i<rbsIT2.length;i++)
        {
            rawOutputValues.add(rbsIT2[i].evaluate(typeReductionType));            
        }

        //apply zLevel
        double numerator;
        double denominator;
        double[] zValues = rules.get(0).getAntecedents()[0].getSet().getZValues();
        Iterator<TreeMap<Output, Double>> it;
        TreeMap <Output, Double> outputValue;
        Output o;
        int i;  //zLevel
        Iterator<Output> itO = outputs.iterator();
        while(itO.hasNext())
        {
            o = itO.next();
            i=0;
            numerator = 0.0;
            denominator = 0.0;
            //for all zLevels:
            it = rawOutputValues.iterator();
            while(it.hasNext())
            {
                outputValue = it.next();
                numerator += outputValue.get(o) * zValues[i];
                denominator += zValues[i];
                i++;
            }
            returnValue.put(o, numerator/denominator);
        }
        return returnValue;
    }
    
    /**
     * Returns the whole zSlices based rulebase as a series of interval type-2
     * rule bases (one per zLevel) which can then be computed in parallel.
     * @param typeReductionMethod The type-reduction method to be used at the IT2 level 
     * 0: Center Of Sets,  1: Centroid.
     * @param discretizationLevelXAxis The number of discretizations to be used at the IT2 level.
     * @return 
     */
    public IT2_Rulebase[] getIT2Rulebases()
    {
        IT2_Rulebase[] rbs = new IT2_Rulebase[rules.get(0).getAntecedents()[0].getSet().getNumberOfSlices()];   //retrieve the number of zLevels.
        for(int i=0;i<rbs.length;i++)
        {
            rbs[i] = new IT2_Rulebase(this.getNumberOfRules());
            for(int currentRule=0;currentRule<this.getNumberOfRules();currentRule++)
            {
                rbs[i].addRule(rules.get(currentRule).getRuleasIT2Rules()[i]);
            }
            rbs[i].setImplicationMethod(implicationMethod);
        }
        
        return rbs;
    }

    public GenT2z_Rule getRule(int number) 
    {
        return rules.get(number);
    }

    public void changeRule(int ruleToBeChanged, GenT2z_Rule newRule) 
    {
        rules.set(ruleToBeChanged, newRule);
    }

    public void removeRule(int ruleNumber) {
        rules.remove(ruleNumber);
    }
    
    public int getNumberOfRules()
    {
        return this.rules.size();
    }

    public boolean containsRule(GenT2z_Rule rule)
    {
        return rules.contains(rule);
    }

    /**
     * Returns all rules with a matching (i.e. equal) set of antecedents.
     * @param antecedents
     * @return
     */
    public ArrayList<GenT2z_Rule> getRulesWithAntecedents(GenT2z_Antecedent[] antecedents)
    {
        ArrayList<GenT2z_Rule> matches = new ArrayList();
        for(int i=0; i<this.rules.size();i++)
        {
            if(Arrays.equals(rules.get(i).getAntecedents(),antecedents))
                matches.add(rules.get(i));
        }
        return matches;
    }

    /**
     * Returns the current Implication Method as used for all rules.
     * @return 0 (product) or 1 (minimum)
     */
    public String getImplicationMethod() 
    {
        if(implicationMethod==PRODUCT)
            return "product";
        else
            return "minimum";
    }

    /**
     * Sets the implication method, where by implication, we mean the implementation
     * of the AND logical connective between parts of the antecedent.
     * The desired implication method is applied for all rules.
     * @param implicationMethod Product (0) or Minimum (1) are supported.
     */
    public void setImplicationMethod(byte implicationMethod) 
    {
        if(implicationMethod == PRODUCT)
            this.implicationMethod = PRODUCT;
        else if(implicationMethod == MINIMUM)
            this.implicationMethod = MINIMUM;
        else
            throw new BadParameterException("Only product (0) and minimum (1) implication is currentlyt supported.");
    }       
    public String toString()
    {
        String s="";
        for(int i=0;i<this.getNumberOfRules();i++)
        {
            s+=this.getRules().get(i);
            if(i<this.getNumberOfRules()-1)
                s+="\n";
        }
        return s;
    }
}
