/*
 * Rulebase.java
 *
 * Created on 19 November 2008, 18:25
 *
 * Copyright 2008 Christian Wagner All Rights Reserved.
 */

package type1.system;

import generic.BadParameterException;
import generic.Input;
import generic.Output;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;


 /**
 * Class Capturing an entire Type-1 FLS through its rules.
 * @author Christian Wagner
 */
public class T1_Rulebase implements Serializable
{
    private Vector<T1_Rule> rules;
    private double[] fStrengths;
    private double denominator, numerator;
    private byte inferenceMethod = 1;
    private byte implicationMethod = 1;
    private HashMap<Output, double[]> outputSetBuffers; //a buffer for the discretised output sets (yLevels) of each output
    private TreeMap<Output, Double> outputBuffers;  //buffers the actual outputs of the rulebase (one per output)
    
    private final byte PRODUCT = 0;
    private final byte MINIMUM = 1;    
    private final boolean DEBUG = false;
    
    /**
     * Creates a new instance of T1_Rulebase
     * @param initialNumberOfRules Initialises the rules vector of the object.
     */
    public T1_Rulebase(int initialNumberOfRules)
    {
        rules = new Vector(initialNumberOfRules);
        outputSetBuffers = new HashMap();
        outputBuffers = new TreeMap();
    }

    public T1_Rulebase()
    {
        rules = new Vector();
        outputSetBuffers = new HashMap();
        outputBuffers = new TreeMap();
    }
    
    public void addRule(T1_Rule r)
    {
        rules.addElement(r);
        Iterator<T1_Consequent> it = r.getConsequentsIterator();
        T1_Consequent c;
        while(it.hasNext())
        {   
            c = it.next();
            if (!outputSetBuffers.containsKey(c.getOutput()))
            {
                outputSetBuffers.put(c.getOutput(), new double[c.getOutput().getDiscretisationLevel()]);
                outputBuffers.put(c.getOutput(), null);
            }
        }
    }

    public int getNumberOfOutputs() {
        return outputSetBuffers.size();
    }
    
    public int getNumberOfRules()
    {
        return rules.size();
    }

    
    /**
     * Returns the current Inference Method as used for all rules.
     * @return 0 (product) or 1 (minimum)
     */    
    public String getInferenceMethod() 
    {
        if(inferenceMethod==PRODUCT)
            return "product";
        else
            return "minimum";
    }

    /**
     * Sets the inference method, where by inference, we mean the implementation
     * of applying the rule's firing strength to the consequent.
     * The desired inference method is applied for all rules.
     * @param inferenceMethod Product (0) or Minimum (1) are supported.
     */    
    public void setInferenceMethod(byte inferenceMethod) 
    {
        if(inferenceMethod == PRODUCT)
            this.inferenceMethod = PRODUCT;
        else if(inferenceMethod == MINIMUM)
            this.inferenceMethod = MINIMUM;
        else
            throw new BadParameterException("Only product (0) and minimum (1) inference is currentlyt supported.");
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

    
    
    public T1_Rule getRule(int ruleNumber)
    {
        return rules.elementAt(ruleNumber);
    }

    /**
     * This method assumes all rules use the same (and all) inputs. 
     * The first rule is queried to identify the inputs and return them.
     * @return An array of the inputs used in the rulebase (retrieved from the 
     * antecedents of the first rule in the rulebase!).
     */
    public Input[] getInputs()
    {
        return rules.elementAt(0).getInputs();
    }
    
    /**
     * Returns the outputSetBuffers
     */
    public HashMap<Output, double[]> getOutputSetBuffers() {
    	return this.outputSetBuffers;
    }

    /**
     * Returns defuzzified result of evaluating all rules in the rulebase.
     * @param defuzzificationType The type of defuzzifier to be used: 0-Height 
     * Defuzzification, 1-Centroid Defuzzification.
     * @param discretizationLevel The discretization level to be employed (only
     * applies to centroid defuzzification)
     */
    public TreeMap<Output, Double> evaluate(int defuzzificationType)
    {
        if(defuzzificationType==0)
            return heightDefuzzification();
        else if (defuzzificationType==1)
            return centroidDefuzzification(); 
        else
            throw new BadParameterException("The T1 evaluate() method only supports height defuzzification (0) and centroid defuzzification (1).");
    }

    /**
     * Inference and Centroid Defuzzification
     * @return TreeMap with outputs as keys, holding one Double per output based on centroid defuzzifier
     */
    public TreeMap<Output, Double> centroidDefuzzification()
    {
        //clear the output buffer
        Iterator<Output> it = outputSetBuffers.keySet().iterator();
        Output o;
        while(it.hasNext())
        {
            o = it.next();
            if(outputSetBuffers.get(o).length==o.getDiscretisationLevel())
                Arrays.fill(outputSetBuffers.get(o),0.0);
            else
                outputSetBuffers.put(o,new double[o.getDiscretisationLevel()]);
        }
        
        fStrengths = new double[rules.size()];
        for (int i=0;i<rules.size();i++)
        {
                fStrengths[i] = ((T1_Rule)rules.elementAt(i)).getFStrength(implicationMethod);
                if(DEBUG)System.out.println("fStrength of rule "+i+" is: "+fStrengths[i]);
        }
        
        Iterator<T1_Consequent> consequentIterator;
        T1_Consequent c;
        //for each rule
        for(int r=0;r<rules.size();r++)
        {
            if(DEBUG)System.out.println("Rule: "+r+"\n"+rules.get(r));
            //for each output (i.e. consequent) in this rule...
            consequentIterator = rules.get(r).getConsequentsIterator();
            while(consequentIterator.hasNext())
            {
                c = consequentIterator.next();
                o = c.getOutput();
                for(int i=0; i<o.getDiscretisationLevel();i++)
                {
                    if(inferenceMethod==PRODUCT)
                    {
                        if(DEBUG)System.out.println("output = "+o.getName() + "  outputSetBuffers.get(o)[i]= "+outputSetBuffers.get(o)[i]+"  fStrengths[r]="+fStrengths[r]+"   c.getMF().getFS(o.getDisc[i])="+c.getMF().getFS(o.getDiscretizations()[i])+"  o.getDisc[i]="+o.getDiscretizations()[i]+"     result: "+fStrengths[r] * c.getMF().getFS(o.getDiscretizations()[i]));
                        outputSetBuffers.get(o)[i] = Math.max(outputSetBuffers.get(o)[i], fStrengths[r] * c.getMF().getFS(o.getDiscretizations()[i]));
                    }
                    else
                    {
                        outputSetBuffers.get(o)[i] = Math.max(outputSetBuffers.get(o)[i], Math.min(fStrengths[r], c.getMF().getFS(o.getDiscretizations()[i])));
                    }
                }
            }
        }
        
        //calculate center of gravity for each output
        numerator = 0.0; denominator = 0.0;
        Iterator<Output> outputs = outputBuffers.keySet().iterator();
        while(outputs.hasNext())
        {
            o = outputs.next();
            numerator = 0.0; denominator = 0.0;
            for(int i=0 ; i<o.getDiscretisationLevel();i++)
            {
                numerator += o.getDiscretizations()[i] * outputSetBuffers.get(o)[i];
                denominator += outputSetBuffers.get(o)[i];
            }
            outputBuffers.put(o, numerator/denominator);
        }
        return outputBuffers;
    }
    
    /**
     * Inference and Height  Defuzzification
     * @return TreeMap with outputs as keys, holding one Double per output based on height defuzzifier
     */
    public TreeMap<Output, Double> heightDefuzzification()
    {        
        //clear the output buffer
        Iterator<Output> it = outputSetBuffers.keySet().iterator();
        Output o;
        while(it.hasNext())
        {
            o = it.next();
            if(outputSetBuffers.get(o).length==2)
                Arrays.fill(outputSetBuffers.get(o),0.0);
            else
                outputSetBuffers.put(o,new double[2]);
        }        

        fStrengths = new double[rules.size()];
        for (int i=0;i<rules.size();i++)
        {
                fStrengths[i] = ((T1_Rule)rules.elementAt(i)).getFStrength(implicationMethod);
                if(DEBUG)System.out.println("fStrength of rule "+i+" is: "+fStrengths[i]);
        }

        //for each rule
        T1_Consequent c;
        Iterator<T1_Consequent> consequentIterator;
        for(int r=0;r<rules.size();r++)
        {
            //for each output (i.e. consequent) in this rule...
            consequentIterator = rules.get(r).getConsequentsIterator();
            while(consequentIterator.hasNext())
            {
                c = consequentIterator.next();
                o = c.getOutput();
                outputSetBuffers.get(o)[0] = outputSetBuffers.get(o)[0] + fStrengths[r] * c.getMF().getPeak();  //numerator
                outputSetBuffers.get(o)[1] = outputSetBuffers.get(o)[1] + fStrengths[r];    //denominator
            }
        }
        
        //prep outputs
        Iterator<Output> outputs = outputBuffers.keySet().iterator();
        while(outputs.hasNext())
        {
            o = outputs.next();
            outputBuffers.put(o, outputSetBuffers.get(o)[0] / outputSetBuffers.get(o)[1]);
        }
        return outputBuffers;
    }

    public Vector<T1_Rule> getRules() {
        return rules;
    }

    public void changeRule(int ruleToBeChanged, T1_Rule newRule) {
        rules.setElementAt(newRule, ruleToBeChanged);
    }

    public void removeRule(int ruleNumber) {
        rules.remove(ruleNumber);
    }

    
    @Override
    public String toString()
    {
        String s="Type-1 Fuzzy Logic System with "+this.getNumberOfRules()+" rules:\n";
        for(int i=0;i<this.getNumberOfRules();i++)
            s+=rules.get(i)+"\n";
        return s;
    }
}
