/*
 * SimpleT1FLS.java
 *
 * Created on May 20th 2012
 *
 * Copyright 2012 Christian Wagner All Rights Reserved.
 */
package examples;

import generic.Input;
import generic.Output;
import generic.Tuple;
import java.util.TreeMap;
import tools.JMathPlotter;
import type1.sets.T1MF_Gauangle;
import type1.sets.T1MF_Gaussian;
import type1.sets.T1MF_Interface;
import type1.sets.T1MF_Triangular;
import type1.system.T1_Antecedent;
import type1.system.T1_Consequent;
import type1.system.T1_Rule;
import type1.system.T1_Rulebase;

/**
 * A simple example of a type-1 FLS based on the "How much to tip the waiter"
 *  scenario which has been augmented to showcase the use of two outputs.
 * We have two inputs: food quality and service level and as an output we would
 * like to generate the applicable tip as well as a recommended smile as a sign of satisfaction.
 * @author Christian Wagner
 */
public class SimpleT1FLS_twoOutputs 
{
    Input food, service;    //the inputs to the FLS
    Output tip, smile;      //the outputs of the FLS
    T1_Rulebase rulebase;   //the rulebase captures the entire FLS
    
    public SimpleT1FLS_twoOutputs()
    {
        //Define the inputs
        food = new Input("Food Quality", new Tuple(0,10));      //a rating given by a person between 0 and 10
        service = new Input("Service Level", new Tuple(0,10));  //a rating given by a person between 0 and 10
        tip = new Output("Tip", new Tuple(0,30));               //a percentage for the tip
        smile = new Output("Smile", new Tuple(0,1));            //a "smile" output between 0 and 1

        //Set up the membership functions (MFs) for each input and output
        T1MF_Triangular badFoodMF = new T1MF_Triangular("MF for bad food",0.0, 0.0, 10.0);
        T1MF_Triangular greatFoodMF = new T1MF_Triangular("MF for great food",0.0, 10.0, 10.0);

        T1MF_Gauangle unfriendlyServiceMF = new T1MF_Gauangle("MF for unfriendly service",0.0, 0.0, 6.0);
        T1MF_Gauangle okServiceMF = new T1MF_Gauangle("MF for ok service",2.5, 5.0, 7.5);
        T1MF_Gauangle friendlyServiceMF = new T1MF_Gauangle("MF for friendly service",4.0, 10.0, 10.0);

        T1MF_Gaussian lowTipMF = new T1MF_Gaussian("MF for Low tip", 0.0, 6.0);
        T1MF_Gaussian mediumTipMF = new T1MF_Gaussian("MF for Medium tip", 15.0, 6.0);
        T1MF_Gaussian highTipMF = new T1MF_Gaussian("MF for High tip", 30.0, 6.0);
        
        T1MF_Triangular smallSmileMF = new T1MF_Triangular("MF for Small Smile", 0.0, 0.0, 1.0);
        T1MF_Triangular bigSmileMF = new T1MF_Triangular("MF for Big Smile", 0.0, 1.0, 1.0);

        //Set up the antecedents and consequents - note how the inputs are associated...
        T1_Antecedent badFood = new T1_Antecedent("BadFood",badFoodMF, food);
        T1_Antecedent greatFood = new T1_Antecedent("GreatFood",greatFoodMF, food);

        T1_Antecedent unfriendlyService = new T1_Antecedent("UnfriendlyService",unfriendlyServiceMF, service);
        T1_Antecedent okService = new T1_Antecedent("OkService",okServiceMF, service);
        T1_Antecedent friendlyService = new T1_Antecedent("FriendlyService",friendlyServiceMF, service);

        T1_Consequent lowTip = new T1_Consequent("LowTip", lowTipMF, tip);
        T1_Consequent mediumTip = new T1_Consequent("MediumTip", mediumTipMF, tip);
        T1_Consequent highTip = new T1_Consequent("HighTip", highTipMF, tip);
        
        T1_Consequent smallSmile = new T1_Consequent("SmallSmile", smallSmileMF, smile);
        T1_Consequent bigSmile = new T1_Consequent("BigSmile", bigSmileMF, smile);

        //Set up the rulebase and add rules
        rulebase = new T1_Rulebase(6);
        rulebase.addRule(new T1_Rule(new T1_Antecedent[]{badFood, unfriendlyService}, new T1_Consequent[]{lowTip, smallSmile}));
        rulebase.addRule(new T1_Rule(new T1_Antecedent[]{badFood, okService}, new T1_Consequent[]{lowTip, smallSmile}));
        rulebase.addRule(new T1_Rule(new T1_Antecedent[]{badFood, friendlyService}, new T1_Consequent[]{mediumTip}));   //note - not each consequent needs to be part of every rule...
        rulebase.addRule(new T1_Rule(new T1_Antecedent[]{greatFood, unfriendlyService}, new T1_Consequent[]{lowTip}));
        rulebase.addRule(new T1_Rule(new T1_Antecedent[]{greatFood, okService}, new T1_Consequent[]{mediumTip, smallSmile}));
        rulebase.addRule(new T1_Rule(new T1_Antecedent[]{greatFood, friendlyService}, new T1_Consequent[]{highTip, bigSmile}));
        
        
        //just an example of setting the discretisation level of an output - the usual level is 100
        tip.setDiscretisationLevel(50);
        
        //get some outputs
        getOutput(7,8);
        getOutput(0,2.5);
        getOutput(10,1.0);
        System.out.println("--> Note that for smile the output is -Not a Number- (NaN). This is because no rule was defined for the given input combination and the -smile- output");
        
        //plot some sets, discretizing each input into 100 steps.
        plotMFs("Food Quality Membership Functions", new T1MF_Interface[]{badFoodMF, greatFoodMF}, food.getDomain(), 100); 
        plotMFs("Service Level Membership Functions", new T1MF_Interface[]{unfriendlyServiceMF, okServiceMF, friendlyServiceMF}, service.getDomain(), 100);
        plotMFs("Level of Tip Membership Functions", new T1MF_Interface[]{lowTipMF, mediumTipMF, highTipMF}, tip.getDomain(), 100);
        plotMFs("Satisfaction Smile Membership Functions", new T1MF_Interface[]{smallSmileMF, bigSmileMF}, smile.getDomain(), 100);
       
        //plot control surface
        //do either height defuzzification (false) or centroid d. (true)
        plotControlSurface(tip, true, 10, 10);
        plotControlSurface(smile, true, 10, 10);
        System.out.println("--> Note that in the control surfaces any areas which would result in NaN are replaced by 0 by convention.");
        
        //print out the rules
        System.out.println("\n"+rulebase);        
    }
    
    /**
     * Basic method that prints the output for a given set of inputs.
     * @param foodQuality
     * @param serviceLevel 
     */
    private void getOutput(double foodQuality, double serviceLevel)
    {
        //first, set the inputs
        food.setInput(foodQuality);
        service.setInput(serviceLevel);
        //now execute the FLS and print output
        System.out.println("The food was: "+food.getInput());
        System.out.println("The service was: "+service.getInput());
        TreeMap<Output, Double> output;
        output = rulebase.evaluate(0);
        System.out.println("Using height defuzzification, the FLS recommends a tip of: "
                +output.get(tip)+" and a smile of: "+output.get(smile)); 
        output = rulebase.evaluate(1);
        System.out.println("Using centroid defuzzification, the FLS recommends a tip of: "
                +output.get(tip)+" and a smile of: "+output.get(smile));      
    }
    
    private void plotMFs(String name, T1MF_Interface[] sets, Tuple xAxisRange, int discretizationLevel)
    {
        JMathPlotter plotter = new JMathPlotter(17,17,15);
        for (int i=0;i<sets.length;i++)
        {
            plotter.plotMF(sets[i].getName(), sets[i], discretizationLevel, xAxisRange, new Tuple(0.0,1.0), false);
        }
        plotter.show(name);
    }

    private void plotControlSurface(Output o, boolean useCentroidDefuzzification, int input1Discs, int input2Discs)
    {
        double output;  //variable for the numeric output
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
            {
                service.setInput(y[currentY]);
                if(useCentroidDefuzzification)
                    output = rulebase.evaluate(1).get(o);
                else
                    output = rulebase.evaluate(0).get(o);
                z[currentY][currentX] = output;
            }    
        }
        
        //now do the plotting
        JMathPlotter plotter = new JMathPlotter(17, 17, 14);
        plotter.plotControlSurface("Control Surface",
                new String[]{food.getName(), service.getName(), o.getName()}, x, y, z, o.getDomain(), true);   
       plotter.show("Type-1 Fuzzy Logic System Control Surface for Tipping Example");
    }
    
    public static void main (String args[])
    {
        new SimpleT1FLS_twoOutputs();
    }
}
