/*
 * IT2_Rulebase.java
 *
 * Copyright 2007 Christian Wagner All Rights Reserved.
 */
package intervalType2.system;

import generic.BadParameterException;
import generic.Input;
import generic.Output;
import generic.Tuple;
import intervalType2.sets.*;
import java.util.*;

public class IT2_Rulebase
{
    private Vector<IT2_Rule> rules;
    private ArrayList<Output> outputs;  //keeps track of all outputs - in any rules
    private final boolean DEBUG = false;
    private final boolean showContext =  false;
    private IT2_Rule temp;

    private final double nan = Double.NaN;
    private final byte CENTEROFSETS = 0;
    private final byte CENTROID = 1;
//    private byte inferenceMethod = 0;  //not used ATM
    private byte implicationMethod = 1;
    
    private final byte PRODUCT = 0;
    private final byte MINIMUM = 1;        

    public IT2_Rulebase()
    {
            rules = new Vector();
            outputs = new ArrayList();
    }
    public IT2_Rulebase(int initialNumberOfRules)
    {
            rules = new Vector(initialNumberOfRules);
            outputs = new ArrayList();
    }

    /**
        * This method assumes all rules use the same (and all) inputs. The first rule is queried to identify the inputs and return them.
        * @return An array of the inputs used in the rulebase (retrieved from the actecedents of the firts rule in the rulebase!).
        */
    public Input[] getInputs()
    {
        return rules.elementAt(0).getInputs();
    }

    /**
        * Returns the type of fuzzy logic that is employed.
        * @return 0: type-1, 1: interval type-2, 2: zSlices based general type-2
        */
    public int getFuzzyLogicType()
    {
        return 1;
    }

    public void addRule(IT2_Rule r)
    {
        rules.addElement(r);
        
        //check if any new outputs occur
        Iterator<IT2_Consequent> it = r.getConsequentsIterator();
        Output o;
        while(it.hasNext())
        {
            o=it.next().getOutput();
            if(!outputs.contains(o))
                outputs.add(o);
        }
    }
    public void addRules(IT2_Rule[] r)
    {
        for(int i=0;i<r.length;i++)
        {
            rules.add(r[i]);
            //check if any new outputs occur
            Iterator<IT2_Consequent> it = r[i].getConsequentsIterator();
            Output o;
            while(it.hasNext())
            {
                o=it.next().getOutput();
                if(!outputs.contains(o))
                    outputs.add(o);
            }                
        }
    }

    public Vector getRules()
    {
        return rules;
    }

    public int getNumberOfRules()
    {
        return rules.size();
    }

    public Iterator<Output> getOutputIterator()
    {
        return outputs.iterator();
    }
    
    /**
     * Returns the output of the FLS after type-reduction, i.e. the centroid.
     * @param typeReductionType
     * @return A TreeMap where Output is used as key and the value is an Object[]
     *  where Object[0] is a Tuple (the centroid) and Object[1] is a Double holding
     * the associated yValue for the centroid. If not rule fired for the given input(s),
     * then null is returned as an Object[].
     */
    public synchronized TreeMap<Output, Object[]> evaluateGetCentroid(int typeReductionType)
    {
        TreeMap<Output, Object[]> returnValue = new TreeMap();
        TreeMap<Output, Tuple> typeReductionOutput = null;
        
        if(typeReductionType == CENTEROFSETS)
            typeReductionOutput = doCOSTypeReduction();
        else if (typeReductionType == CENTROID)
            typeReductionOutput = doReductionCentroid();  
        
        Iterator<Output> it = outputs.iterator();
        Output o = null;
        while(it.hasNext())
        {
            o = it.next();
            if(typeReductionOutput.get(o) == null) 
                returnValue.put(o, new Object[]{null, 1.0});    //by convention, return null if no rule was fired
            else
                returnValue.put(o, new Object[]{typeReductionOutput.get(o), 1.0});
        }  
        
        return returnValue;  
    }
    
    /**
    * Returns typereduced & defuzzified result of evaluating all rules in the rulebase.
    * @param typeReductionType The type of type reducer to be used: 0-Center-Of-Sets, 
    * 1-Centroid.
    * @param discretizationLevel The discretization level to be employed (only
    * applies to centroid type reducer)
    * @return The type-reduced and defuzzified output.
    */
    public TreeMap<Output, Double> evaluate(int typeReductionType)
    {
        TreeMap<Output, Double> returnValue = new TreeMap();
        TreeMap<Output, Tuple> typeReductionOutput = null;
        
        if(typeReductionType == CENTEROFSETS)
            typeReductionOutput = doCOSTypeReduction();
        else if (typeReductionType == CENTROID)
            typeReductionOutput = doReductionCentroid();  
        
        Iterator<Output> it = outputs.iterator();
        Output o;
        while(it.hasNext())
        {
            o = it.next();
            if(typeReductionOutput.get(o) == null) 
                returnValue.put(o, 0.0);    //by convention, return 0 if no rule was fired
            else
                returnValue.put(o, typeReductionOutput.get(o).getAverage());
        }  
        return returnValue;      
    }
        
    
    public TreeMap<Output, Tuple> doCOSTypeReduction()
    {
        //get rule firing strengths
        TreeMap<Output, Tuple> returnValue = new TreeMap();
        Iterator<IT2_Rule> ruleIterator;
        
        TreeMap<Output, Object[]> data = getFiringIntervalsForCOS_TR();
        
        //for each output
        Iterator<Output> oIt = data.keySet().iterator();
        Object[] currentOutputData;
        Output o;

        if(data.firstEntry()==null || ((Vector<IT2_COSInferenceData>)data.firstEntry().getValue()[0]).size()==0)
        {    //no rules fired
            while(oIt.hasNext())
                returnValue.put(oIt.next(), null);
            return returnValue;
        }
        else
        while(oIt.hasNext())
        {            
            o = oIt.next();
            currentOutputData = data.get(o);

            IT2_COSInferenceData[] leftData = new IT2_COSInferenceData[((Vector)currentOutputData[0]).size()];
            ((Vector<IT2_COSInferenceData>)currentOutputData[0]).toArray(leftData);
            IT2_COSInferenceData[] rightData = new IT2_COSInferenceData[((Vector)currentOutputData[1]).size()];
            ((Vector<IT2_COSInferenceData>)currentOutputData[1]).toArray(rightData);
            ruleIterator = rules.iterator();

            
            {
                Arrays.sort(leftData);
                Arrays.sort(rightData);

                double[] fir = new double[leftData.length];
                double yr = 0.0, yl = 0.0;
                double yDash = 0.0;
                double yDashDash = 0.0;
                boolean stopFlag = false;
                int R = 0;
                int L = 0;            

                //DO RIGHT
                for(int i = 0; i<fir.length;i++) 
                {
                    fir[i] = rightData[i].getFStrength().getAverage();
                }

                yr = weightedSigma(fir, rightData);
                yDash = yr;

                compute_yr:
                while(!stopFlag)
                {
                    //Step2
                    for(int i=0; i<fir.length-1;i++)     //set to rulesFired-2
                    {
                        //System.out.println("yDash = "+yDash+" and rCCRight[i] = "+ rCCRight[i]);
                        if(rightData[i].getSelectedCentroidEndpoint()<=yDash && yDash<=rightData[i+1].getSelectedCentroidEndpoint())
                        {
                            R = i;
                            break;
                        }
                    }

                    //Step3
                    for(int i=0;i<=R;i++)
                    {
                        fir[i] = rightData[i].getFStrength().getLeft();
                    }
                    for(int i=R+1;i<fir.length;i++)
                    {
                        fir[i] = rightData[i].getFStrength().getRight();
                    }

                    //fix problem when only one rule fires and f_lower =0
                    if(fir.length == 1 & fir[0]==0) fir[0] = 0.00001; 

                    yr = weightedSigma(fir, rightData);

                    yDashDash = yr;

                    //Step4 if smaller than 1 billionth
                    if(Math.abs(yDash-yDashDash)<0.000000001)//watch out for problem with doubles  use difference of absolute value < 0.003...
                    {
                        stopFlag = true;
                        yDashDash = yr;
                    }
                    else
                    {
                        yDash = yDashDash;
                    }
                }

                //DO LEFT
                //reset stopFlag!
                stopFlag = false;
                for(int i = 0; i<fir.length;i++) 
                {
                    fir[i] = leftData[i].getFStrength().getAverage();
                }

                yl = weightedSigma(fir, leftData);
                yDash = yl;
                if(DEBUG)System.out.println("Intitial yDash for left = "+yDash);

                compute_yl:
                while(!stopFlag)
                {
                    //Step2
                    for(int i=0; i<=fir.length-2;i++)  //set to rulesFired-2
                    {
                        if(leftData[i].getSelectedCentroidEndpoint()<=yDash && yDash<=leftData[i+1].getSelectedCentroidEndpoint())
                        {
                            L = i;
                            break;
                        }
                    }

                    //Step3
                    for(int i=0;i<=L;i++)
                    {
                        fir[i] = leftData[i].getFStrength().getRight();
                    }
                    for(int i=L+1;i<fir.length;i++)
                    {
                        fir[i] = leftData[i].getFStrength().getLeft();
                    }

                    yl = weightedSigma(fir, leftData);
                    if(new Double(yl).equals(nan))
                    {
                        yl = 0;
                        break compute_yl;
                    }           

                    yDashDash = yl;

                    //Step4
                    if(DEBUG)System.out.println("yDash = "+yDash+" and yDashDash = "+yDashDash);

                    //difference smaller than 1 in a billion
                    if(Math.abs(yDash)-Math.abs(yDashDash)<0.000000001)//watch out for problem with doubles  use difference of absolute value < 0.003...
                    {
                        stopFlag = true;
                        yDashDash = yl;
                    }
                    else
                    {
                        yDash = yDashDash;
                    }
                }
                if(DEBUG)System.out.println("returning yl = "+yl+" and yr= "+yr);
                returnValue.put(o, new Tuple(yl, yr));             
            }
        }//output loop end
        return returnValue;
    }
    
    /**
     * 
     * @return 
     */
    private synchronized TreeMap<Output, Object[]> getFiringIntervalsForCOS_TR()
    {
        TreeMap<Output, Object[]> returnValue = new TreeMap();
        if(DEBUG)System.out.println("Number of rules in rulebase: "+rules.size());
        //create temp variables - optimize
        Tuple firingIntervals[] = new Tuple[rules.size()];
        Tuple ruleCentroids[] = new Tuple[rules.size()];
        int rulesFired=0;
        int i=0;
        //Tuple firingStrength;
        Object[] fIntervals = new Object[2];

        int ruleCounter=0;
        Iterator it = rules.iterator();
        IT2_Consequent cons;
        while(it.hasNext())
        {            
            temp = (IT2_Rule)it.next();
            Iterator<IT2_Consequent> ruleCons = temp.getConsequentsIterator();
            Tuple firingStrength = temp.getFStrength(implicationMethod);
            if(firingStrength.getRight()>0.0)    //check if rule fired
            {
                while(ruleCons.hasNext())
                {
                    cons = ruleCons.next();
                    if(!returnValue.containsKey(cons.getOutput()))
                    {
                        returnValue.put(cons.getOutput(), new Object[]{new Vector<IT2_COSInferenceData>(), new Vector<IT2_COSInferenceData>()});    //for left and right consequentData object - needed for separate sorting
                    }
                    ((Vector)returnValue.get(cons.getOutput())[0]).add(new IT2_COSInferenceData(firingStrength, temp.getConsequentCentroid(cons.getOutput()).getLeft()));
                    ((Vector)returnValue.get(cons.getOutput())[1]).add(new IT2_COSInferenceData(firingStrength, temp.getConsequentCentroid(cons.getOutput()).getRight()));
                }
            }
            ruleCounter++;
        }       
        return returnValue;


    }

    public TreeMap<Output, Tuple> doReductionCentroid()
    {
        Tuple fStrength;
        boolean firstFiredSet = true;
        TreeMap<Output, IntervalT2MF_Interface> overallOutputSet = new TreeMap();
        TreeMap<Output, Boolean> firstFiredForOutput = new TreeMap();
        Iterator<Output> iO = outputs.iterator();
        while(iO.hasNext())
        {
            firstFiredForOutput.put(iO.next(), true);
        }

        Iterator<IT2_Rule> itR = rules.iterator();
        Iterator<IT2_Consequent> itC;
        IT2_Consequent c;
        Output o;
        while(itR.hasNext())
        {
            temp = (IT2_Rule)itR.next();
            fStrength = temp.getFStrength(implicationMethod);
            
            if(fStrength.getRight()>0.0)
            {
                //for each consequent of the rule
                itC = temp.getConsequentsIterator();
                while(itC.hasNext())
                {
                    c = itC.next();
                    o = c.getOutput();

                    if(firstFiredForOutput.get(o))
                    {
                        overallOutputSet.put(o, new IntervalT2MF_Intersection(
                                new IntervalT2MF_Cylinder("FiringInterval",fStrength),
                                c.getMembershipFunction()));
                        if(!((IntervalT2MF_Intersection)overallOutputSet.get(o)).intersectionExists()) 
                        {
                            System.out.println("PUTTING NULL");
                            overallOutputSet.put(o,null);
                        }
                        firstFiredForOutput.put(o, false);                   
                    }
                    else
                    {
                        if(overallOutputSet.get(o)==null)
                        {
                            overallOutputSet.put(o, new IntervalT2MF_Intersection(
                                new IntervalT2MF_Cylinder("FiringInterval",fStrength),
                                c.getMembershipFunction()));
                            if(!((IntervalT2MF_Intersection)overallOutputSet.get(o)).intersectionExists()) 
                            {
                                System.out.println("PUTTING NULL");
                                overallOutputSet.put(o,null);
                            }
                        }
                        else
                        {
                            overallOutputSet.put(o, new IntervalT2MF_Union(
                            new IntervalT2MF_Intersection(
                            new IntervalT2MF_Cylinder("FiringInterval",fStrength),
                            c.getMembershipFunction()),
                            overallOutputSet.get(o)));                            
                        }
                    }
                }
            }
        }
        
        
        //Create Centroid Engine and get centroid of overall set for each output:
        IntervalT2Engine_Centroid iT2EC = new IntervalT2Engine_Centroid();
        TreeMap<Output, Tuple> returnValue = new TreeMap();
        iO = outputs.iterator();
        while(iO.hasNext())
        {
            o = iO.next();
            iT2EC.setPrimaryDiscretizationLevel(o.getDiscretisationLevel());
            returnValue.put(o, iT2EC.getCentroid(overallOutputSet.get(o)));
        }
        return returnValue;
    }    
    
//    public Tuple doReductionCentroid(int discLevel)
//    {
//        Tuple firingIntervals[] = new Tuple[rules.size()];
//        int i = 0;
//        IntervalT2MF_Interface overallOutputSet = null;
//        boolean firstFiredSet = true;
//
//        Iterator it = rules.iterator();
//        while(it.hasNext())
//        {
//            temp = (IT2_Rule)it.next();
//            firingIntervals[i] = temp.getFStrength();
//
//            if(firingIntervals[i].getRight()!=0)
//            {
//                if(firstFiredSet)
//                {
//                    overallOutputSet = new IntervalT2MF_Intersection(
//                            new IntervalT2MF_Cylinder("FiringInterval_"+i,firingIntervals[i]),
//                            temp.getConsequent().getMembershipFunction());
//                    if(!((IntervalT2MF_Intersection)overallOutputSet).intersectionExists()) 
//                        overallOutputSet = null;
//                    firstFiredSet = false;
//                }
//                else
//                {
//                    if(overallOutputSet == null)
//                    {
//                        overallOutputSet = new IntervalT2MF_Intersection(
//                            new IntervalT2MF_Cylinder("FiringInterval_"+i,firingIntervals[i]),
//                            temp.getConsequent().getMembershipFunction());
//                        if(!((IntervalT2MF_Intersection)overallOutputSet).intersectionExists()) 
//                        overallOutputSet = null;
//                    }
//                    else
//                    overallOutputSet = new IntervalT2MF_Union(
//                            new IntervalT2MF_Intersection(
//                            new IntervalT2MF_Cylinder("FiringInterval_"+i,firingIntervals[i]),
//                            temp.getConsequent().getMembershipFunction()),
//                            overallOutputSet);
//                    
//                }
//            }
//            i++;
//        }
//
//        //Create Centroid Engine and get centroid of overall set:
//        IntervalT2Engine_Centroid iT2EC = new IntervalT2Engine_Centroid();
//        iT2EC.setPrimaryDiscretizationLevel(discLevel);
//
//        iT2EC.getCentroid(overallOutputSet);
//        return iT2EC.getCentroid(overallOutputSet);
//    }


    private double weightedSigma(double[] w, double[] y)
    {
        double numerator = 0.0, denominator = 0.0;
        for (int i=0;i<w.length;i++) numerator+=(w[i]*y[i]);
        for (int i=0;i<w.length;i++) denominator+=w[i];

        if(denominator==0.0) 
            return 0.0;
        else
            return (numerator / denominator);
    }
    
    /**
     * Facilitates weighted average calculation in COS type reduction
     * @param w
     * @param y
     * @return 
     */
    private double weightedSigma(double[]w, IT2_COSInferenceData[] y)
    {
        double numerator = 0.0, denominator = 0.0;
        for (int i=0;i<w.length;i++) numerator+=(w[i]*y[i].getSelectedCentroidEndpoint());
        for (int i=0;i<w.length;i++) denominator+=w[i];

        if(denominator==0.0) 
            return 0.0;
        else
            return (numerator / denominator);
    }


    public void removeRule(int ruleNumber) 
    {
        rules.remove(ruleNumber);
    }


//    /**
//     * Returns the current Inference Method as used for all rules.
//     * @return 0 (product) or 1 (minimum)
//     */    
//    public String getInferenceMethod() 
//    {
//        if(inferenceMethod==PRODUCT)
//            return "product";
//        else
//            return "minimum";
//    }
//
//    /**
//     * Sets the inference method, where by inference, we mean the implementation
//     * of applying the rule's firing strength to the consequent.
//     * The desired inference method is applied for all rules.
//     * @param inferenceMethod Product (0) or Minimum (1) are supported.
//     */    
//    public void setInferenceMethod(byte inferenceMethod) 
//    {
//        if(inferenceMethod == PRODUCT)
//            this.inferenceMethod = PRODUCT;
//        else if(inferenceMethod == MINIMUM)
//            this.inferenceMethod = MINIMUM;
//        else
//            throw new BadParameterException("Only product (0) and minimum (1) inference is currentlyt supported.");
//    }

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
    
    @Override
    public String toString()
    {
        String s="Interval Type-2 Fuzzy Logic System with "+this.getNumberOfRules()+" rules:\n";
        for(int i=0;i<this.getNumberOfRules();i++)
            s+=rules.get(i)+"\n";
        return s;
    }

}
