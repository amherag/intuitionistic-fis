/*
 * SimpleT1FLS.java
 *
 * Created on May 21st 2012
 *
 * Copyright 2012 Christian Wagner All Rights Reserved.
 */
package examples;

import generic.Input;
import generic.Output;
import generic.Tuple;
import intervalType2.sets.IntervalT2MF_Gauangle;
import intervalType2.sets.IntervalT2MF_Gaussian;
import intervalType2.sets.IntervalT2MF_Interface;
import intervalType2.sets.IntervalT2MF_Triangular;
import intervalType2.system.IT2_Antecedent;
import intervalType2.system.IT2_Consequent;
import intervalType2.system.IT2_Rule;
import intervalType2.system.IT2_Rulebase;
import java.util.TreeMap;
import tools.JMathPlotter;
import type1.sets.T1MF_Gauangle;
import type1.sets.T1MF_Gaussian;
import type1.sets.T1MF_Triangular;

/**
 * A simple example of an interval Type-2 FLS based on the "How much to tip the waiter"
 *  scenario.
 * The example is an extension of the Type-1 FLS example where we extend the MFs
 * and use the Interval Type-2 System classes. Note that in contrast to the type-1
 * case, here only two sets are used to model the service quality.
 * We have two inputs: food quality and service level and as an output we would
 * like to generate the applicable tip.
 * @author Christian Wagner
 */
public class SimpleIT2FLS 
{
    Input food, service;    //the inputs to the FLS
    Output tip;             //the output of the FLS
    IT2_Rulebase rulebase;   //the rulebase captures the entire FLS
    
    public SimpleIT2FLS()
    {
        //Define the inputs
        food = new Input("Food Quality", new Tuple(0,10));
        service = new Input("Service Level", new Tuple(0,10));
        tip = new Output("Tip", new Tuple(0,30));               //a percentage for the tip

        //Set up the lower and upper membership functions (MFs) making up the 
        //overall Interval Type-2 Fuzzy Sets for each input and output
        T1MF_Triangular badFoodUMF = new T1MF_Triangular("Upper MF for bad food",0.0, 0.0, 10.0);
        T1MF_Triangular badFoodLMF = new T1MF_Triangular("Lower MF for bad food",0.0, 0.0, 8.0);
        IntervalT2MF_Triangular badFoodMF = new IntervalT2MF_Triangular("IT2MF for bad food",badFoodUMF,badFoodLMF);
        
        T1MF_Triangular greatFoodUMF = new T1MF_Triangular("Upper MF for great food",0.0, 10.0, 10.0);
        T1MF_Triangular greatFoodLMF = new T1MF_Triangular("Lower MF for great food",2.0, 10.0, 10.0);
        IntervalT2MF_Triangular greatFoodMF = new IntervalT2MF_Triangular("IT2MF for great food",greatFoodUMF,greatFoodLMF);
        
        T1MF_Gauangle unfriendlyServiceUMF = new T1MF_Gauangle("Upper MF for unfriendly service",0.0, 0.0, 8.0);
        T1MF_Gauangle unfriendlyServiceLMF = new T1MF_Gauangle("Lower MF for unfriendly service",0.0, 0.0, 6.0);
        IntervalT2MF_Gauangle unfriendlyServiceMF = new IntervalT2MF_Gauangle("IT2MF for unfriendly service",unfriendlyServiceUMF,unfriendlyServiceLMF);

//        T1MF_Gauangle okServiceUMF = new T1MF_Gauangle("Upper MF for ok service",2.5, 5.0, 7.5);
//        T1MF_Gauangle okServiceLMF = new T1MF_Gauangle("Lower MF for ok service",4.5, 5.0, 5.5);
//        IntervalT2MF_Gauangle okServiceMF = new IntervalT2MF_Gauangle("IT2MF for ok service",okServiceUMF,okServiceLMF);

        T1MF_Gauangle friendlyServiceUMF = new T1MF_Gauangle("Upper MF for friendly service",2.0, 10.0, 10.0);
        T1MF_Gauangle friendlyServiceLMF = new T1MF_Gauangle("Lower MF for friendly service",4.0, 10.0, 10.0);
        IntervalT2MF_Gauangle friendlyServiceMF = new IntervalT2MF_Gauangle("IT2MF for friendly service",friendlyServiceUMF,friendlyServiceLMF);

        T1MF_Gaussian lowTipUMF = new T1MF_Gaussian("Upper MF Low tip", 0.0, 6.0);
        T1MF_Gaussian lowTipLMF = new T1MF_Gaussian("Lower MF Low tip", 0.0, 4.0);
        IntervalT2MF_Gaussian lowTipMF = new IntervalT2MF_Gaussian("IT2MF for Low tip",lowTipUMF,lowTipLMF);

        T1MF_Gaussian mediumTipUMF = new T1MF_Gaussian("Upper MF Medium tip", 15.0, 6.0);
        T1MF_Gaussian mediumTipLMF = new T1MF_Gaussian("Lower MF Medium tip", 15.0, 4.0);
        IntervalT2MF_Gaussian mediumTipMF = new IntervalT2MF_Gaussian("IT2MF for Medium tip",mediumTipUMF,mediumTipLMF);

        T1MF_Gaussian highTipUMF = new T1MF_Gaussian("Upper MF High tip", 30.0, 6.0);
        T1MF_Gaussian highTipLMF = new T1MF_Gaussian("Lower MF High tip", 30.0, 4.0);
        IntervalT2MF_Gaussian highTipMF = new IntervalT2MF_Gaussian("IT2MF for High tip",highTipUMF,highTipLMF);

        //Set up the antecedents and consequents - note how the inputs are associated...
        IT2_Antecedent badFood = new IT2_Antecedent("BadFood", badFoodMF, food);
        IT2_Antecedent greatFood = new IT2_Antecedent("GreatFood", greatFoodMF, food);

        IT2_Antecedent unfriendlyService = new IT2_Antecedent("UnfriendlyService", unfriendlyServiceMF, service);
        //IT2_Antecedent okService = new IT2_Antecedent("OkService", okServiceMF, service);
        IT2_Antecedent friendlyService = new IT2_Antecedent("FriendlyService", friendlyServiceMF, service);

        IT2_Consequent lowTip = new IT2_Consequent("LowTip", lowTipMF, tip);
        IT2_Consequent mediumTip = new IT2_Consequent("MediumTip", mediumTipMF, tip);
        IT2_Consequent highTip = new IT2_Consequent("HighTip", highTipMF, tip);

        //Set up the rulebase and add rules
        rulebase = new IT2_Rulebase(4);
        rulebase.addRule(new IT2_Rule(new IT2_Antecedent[]{badFood, unfriendlyService}, lowTip));
        rulebase.addRule(new IT2_Rule(new IT2_Antecedent[]{badFood, friendlyService}, mediumTip));
        rulebase.addRule(new IT2_Rule(new IT2_Antecedent[]{greatFood, unfriendlyService}, lowTip));
        rulebase.addRule(new IT2_Rule(new IT2_Antecedent[]{greatFood, friendlyService}, highTip));
        
        //get some outputs
        getTip(7,8);
        getTip(0,0);
                
        //plot some sets, discretizing each input into 100 steps.
        plotMFs("Food Quality Membership Functions", new IntervalT2MF_Interface[]{badFoodMF, greatFoodMF}, 100); 
        plotMFs("Service Level Membership Functions", new IntervalT2MF_Interface[]{unfriendlyServiceMF, /**okServiceMF,**/ friendlyServiceMF}, 100);
        plotMFs("Level of Tip Membership Functions", new IntervalT2MF_Interface[]{lowTipMF, mediumTipMF, highTipMF}, 100);
        
        //plot control surface
        //do either height defuzzification (false) or centroid d. (true)
        plotControlSurface(false, 100, 100);
        
        //print out the rules
        System.out.println("\n"+rulebase);
    }
    
    /**
     * Basic method that prints the output for a given set of inputs.
     * @param foodQuality
     * @param serviceLevel 
     */
    private void getTip(double foodQuality, double serviceLevel)
    {
        //first, set the inputs
        food.setInput(foodQuality);
        service.setInput(serviceLevel);
        
        //now execute the FLS and print output
        System.out.println("The food was: "+food.getInput());
        System.out.println("The service was: "+service.getInput());
        System.out.println("Using center of sets type reduction, the IT2 FLS recommends a "
                + "tip of: "+rulebase.evaluate(0).get(tip));  
        System.out.println("Using centroid type reduction, the IT2 FLS recommends a "
                + "tip of: "+rulebase.evaluate(1).get(tip));
        
        
        //show the output of the raw centroids
        System.out.println("Centroid of the output for TIP (based on centroid type reduction):");
        TreeMap<Output, Object[]> centroid = rulebase.evaluateGetCentroid(1);
        Object[] centroidTip = centroid.get(tip);
        Tuple centroidTipXValues = (Tuple)centroidTip[0];
        double centroidTipYValues = ((Double)centroidTip[1]);
            System.out.println(centroidTipXValues+" at y= "+centroidTipYValues);        
    }
    
    private void plotMFs(String name, IntervalT2MF_Interface[] sets, int discretizationLevel)
    {
        JMathPlotter plotter = new JMathPlotter();
        plotter.plotMF(sets[0].getName(), sets[0], discretizationLevel, null, false);
       
        for (int i=1;i<sets.length;i++)
        {
            plotter.plotMF(sets[i].getName(), sets[i], discretizationLevel, null, false);
        }
        plotter.show(name);
    }

    private void plotControlSurface(boolean useCentroidDefuzzification, int input1Discs, int input2Discs)
    {
        double output;
        double[] x = new double[input1Discs];
        double[] y = new double[input2Discs];
        double[][] z = new double[y.length][x.length];
        double incrX, incrY;
        incrX = food.getDomain().getSize()/(input1Discs-1.0);
        incrY = service.getDomain().getSize()/(input2Discs-1.0);

        //first, get the values
        for(int currentX=0; currentX<input1Discs; currentX++)
        {
            x[currentX] = currentX * incrX;        
        }
        for(int currentY=0; currentY<input2Discs; currentY++)
        {
            y[currentY] = currentY * incrY;
        }
        
        for(int currentX=0; currentX<input1Discs; currentX++)
        {
            food.setInput(x[currentX]);
            for(int currentY=0; currentY<input2Discs; currentY++)
            {//System.out.println("Current x = "+currentX+"  current y = "+currentY);
                service.setInput(y[currentY]);
                if(useCentroidDefuzzification)
                    output = rulebase.evaluate(1).get(tip);
                else
                    output = rulebase.evaluate(0).get(tip);
                z[currentY][currentX] = output;
            }    
        }
        
        //now do the plotting
        JMathPlotter plotter = new JMathPlotter();
        plotter.plotControlSurface("Control Surface",
                new String[]{food.getName(), service.getName(), "Tip"}, x, y, z, new Tuple(0.0,30.0), true); 
        plotter.show("Interval Type-2 Fuzzy Logic System Control Surface for Tipping Example");
    }
    
    public static void main(String args[])
    {
        new SimpleIT2FLS();
    }
}
