/*
 * SimplezGT2FLSNSIT2.java
 *
 * Created on August 6th 2014
 *
 * Copyright 2012 Christian Wagner All Rights Reserved.
 */
package examples;

import generalType2zSlices.sets.GenT2zMF_Gaussian;
import generalType2zSlices.sets.GenT2zMF_Interface;
import generalType2zSlices.sets.GenT2zMF_Triangular;
import generalType2zSlices.system.*;
import generic.Input;
import generic.Output;
import generic.Tuple;
import intervalType2.sets.IntervalT2MF_Gaussian;
import intervalType2.sets.IntervalT2MF_Triangular;
import java.util.TreeMap;
import tools.JMathPlotter;
import type1.sets.T1MF_Gaussian;
import type1.sets.T1MF_Triangular;

/**
 * A simple example of a zSlices based general Type-2 FLS based on the "How much 
 * to tip the waiter" scenario.
 * The example is an extension of the Interval Type-2 FLS example where we extend the MFs
 * and use zSlices based General Type-2 Fuzzy System classes.
 * It is the same example as SimpleGT2FLS with a non singleton input IT2.
 * We have two inputs: food quality (non singleton) and service level and as an output we would
 * like to generate the applicable tip.
 * @author Amandine Pailloux
 */
public class SimplezGT2FLSNSIT2
{
    Input food, service;    //the inputs to the FLS
    Output tip;             //the output of the FLS
    GenT2z_Rulebase rulebase;   //the rulebase captures the entire FLS
    
    final int numberOfzLevels = 4;
    final int typeReduction = 0;    //0: Center Of Sets, 1: Centroid
    final int xDiscs = 50;          //discretizations on xAxis
    final int yDiscs = 10;          //discretizations on yAxis
    
    
    public SimplezGT2FLSNSIT2()
    {
        //Define the inputs
    	T1MF_Gaussian inputumf = new T1MF_Gaussian("inputumf",7,3);
    	T1MF_Gaussian inputlmf = new T1MF_Gaussian("inputlmf",7,2);
    	IntervalT2MF_Gaussian inputMf = new IntervalT2MF_Gaussian("inputmf",inputumf,inputlmf);
        food = new Input("Food Quality", new Tuple(0,10),inputMf);
        service = new Input("Service Level", new Tuple(0,10));
        tip = new Output("Tip", new Tuple(0,30));               //a percentage for the tip

        //Set up the lower and upper membership functions (MFs) making up the 
        //overall Interval Type-2 Fuzzy Sets for each input and output
        T1MF_Triangular badFoodUMF = new T1MF_Triangular("Upper MF for bad food",0.0, 0.0, 10.0);
        T1MF_Triangular badFoodLMF = new T1MF_Triangular("Lower MF for bad food",0.0, 0.0, 8.0);
        IntervalT2MF_Triangular badFoodIT2MF = new IntervalT2MF_Triangular("IT2MF for bad food",badFoodUMF,badFoodLMF);
        //now spawn a basic zSlices-based set with 4 zLevels
        GenT2zMF_Triangular badFoodMF = new GenT2zMF_Triangular("zGT2MF for bad food", badFoodIT2MF, numberOfzLevels);
        
        T1MF_Triangular greatFoodUMF = new T1MF_Triangular("Upper MF for great food",0.0, 10.0, 10.0);
        T1MF_Triangular greatFoodLMF = new T1MF_Triangular("Lower MF for great food",2.0, 10.0, 10.0);
        IntervalT2MF_Triangular greatFoodIT2MF = new IntervalT2MF_Triangular("IT2MF for great food",greatFoodUMF,greatFoodLMF);
        //now spawn a basic zSlices-based set with 4 zLevels
        GenT2zMF_Triangular greatFoodMF = new GenT2zMF_Triangular("zGT2MF for great food", greatFoodIT2MF, numberOfzLevels);
        
        
        //We have changed these to triangular MFs - we leave it to the reader to
        //follow the examples and implement a zSlices based general type-2 
        //GenT2zMF_Gauangle class.
        T1MF_Triangular unfriendlyServiceUMF = new T1MF_Triangular("Upper MF for unfriendly service",0.0, 0.0, 8.0);
        T1MF_Triangular unfriendlyServiceLMF = new T1MF_Triangular("Lower MF for unfriendly service",0.0, 0.0, 6.0);
        IntervalT2MF_Triangular unfriendlyServiceIT2MF = new IntervalT2MF_Triangular("IT2MF for unfriendly service",unfriendlyServiceUMF,unfriendlyServiceLMF);
        //now spawn a basic zSlices-based set with 4 zLevels
        GenT2zMF_Triangular unfriendlyServiceMF = new GenT2zMF_Triangular("zGT2MF for unfriendly service", unfriendlyServiceIT2MF, numberOfzLevels);        

        T1MF_Triangular friendlyServiceUMF = new T1MF_Triangular("Upper MF for friendly service",2.0, 10.0, 10.0);
        T1MF_Triangular friendlyServiceLMF = new T1MF_Triangular("Lower MF for friendly service",4.0, 10.0, 10.0);
        IntervalT2MF_Triangular friendlyServiceIT2MF = new IntervalT2MF_Triangular("IT2MF for friendly service",friendlyServiceUMF,friendlyServiceLMF);
        GenT2zMF_Triangular friendlyServiceMF = new GenT2zMF_Triangular("zGT2MF for friendly service", friendlyServiceIT2MF, numberOfzLevels); 

        
        T1MF_Gaussian lowTipUMF = new T1MF_Gaussian("Upper MF Low tip", 0.0, 6.0);
        T1MF_Gaussian lowTipLMF = new T1MF_Gaussian("Lower MF Low tip", 0.0, 4.0);
        IntervalT2MF_Gaussian lowTipIT2MF = new IntervalT2MF_Gaussian("IT2MF for Low tip",lowTipUMF,lowTipLMF);
        GenT2zMF_Gaussian lowTipMF = new GenT2zMF_Gaussian("zGT2MF for Low tip", lowTipIT2MF, numberOfzLevels);

        T1MF_Gaussian mediumTipUMF = new T1MF_Gaussian("Upper MF Medium tip", 15.0, 6.0);
        T1MF_Gaussian mediumTipLMF = new T1MF_Gaussian("Lower MF Medium tip", 15.0, 4.0);
        IntervalT2MF_Gaussian mediumTipIT2MF = new IntervalT2MF_Gaussian("IT2MF for Medium tip",mediumTipUMF,mediumTipLMF);
        GenT2zMF_Gaussian mediumTipMF = new GenT2zMF_Gaussian("zGT2MF for Medium tip", mediumTipIT2MF, numberOfzLevels);

        T1MF_Gaussian highTipUMF = new T1MF_Gaussian("Upper MF High tip", 30.0, 6.0);
        T1MF_Gaussian highTipLMF = new T1MF_Gaussian("Lower MF High tip", 30.0, 4.0);
        IntervalT2MF_Gaussian highTipIT2MF = new IntervalT2MF_Gaussian("IT2MF for High tip",highTipUMF,highTipLMF);
        GenT2zMF_Gaussian highTipMF = new GenT2zMF_Gaussian("zGT2MF for High tip", highTipIT2MF, numberOfzLevels);

        //Set up the antecedents and consequents - note how the inputs are associated...
        GenT2z_Antecedent badFood = new GenT2z_Antecedent("BadFood", badFoodMF, food);
        GenT2z_Antecedent greatFood = new GenT2z_Antecedent("GreatFood", greatFoodMF, food);

        GenT2z_Antecedent unfriendlyService = new GenT2z_Antecedent("UnfriendlyService", unfriendlyServiceMF, service);
        GenT2z_Antecedent friendlyService = new GenT2z_Antecedent("FriendlyService", friendlyServiceMF, service);

        //set up a defuzzification engine here to pass to consequents and set the discretizaiton level to 100.
        GenT2zEngine_Defuzzification gT2zED = new GenT2zEngine_Defuzzification(100);
        GenT2z_Consequent lowTip = new GenT2z_Consequent("LowTip", lowTipMF, tip, gT2zED);
        GenT2z_Consequent mediumTip = new GenT2z_Consequent("MediumTip", mediumTipMF, tip, gT2zED);
        GenT2z_Consequent highTip = new GenT2z_Consequent("HighTip", highTipMF, tip, gT2zED);

        //Set up the rulebase and add rules
        rulebase = new GenT2z_Rulebase(6); 
        rulebase.addRule(new GenT2z_Rule(new GenT2z_Antecedent[]{badFood, unfriendlyService}, lowTip));
        //rulebase.addRule(new IT2_Rule(new IT2_Antecedent[]{badFood, okService}, lowTip));
        rulebase.addRule(new GenT2z_Rule(new GenT2z_Antecedent[]{badFood, friendlyService}, mediumTip));
        rulebase.addRule(new GenT2z_Rule(new GenT2z_Antecedent[]{greatFood, unfriendlyService}, lowTip));
        //rulebase.addRule(new IT2_Rule(new IT2_Antecedent[]{greatFood, okService}, mediumTip));
        rulebase.addRule(new GenT2z_Rule(new GenT2z_Antecedent[]{greatFood, friendlyService}, highTip));
        
        //get some outputs
        getTip(7,8);
        getTip(0.0,0.0);
        
        //plot some sets, discretizing each input into 100 steps.
        plotMFs("Food Quality Membership Functions", new GenT2zMF_Interface[]{badFoodMF, greatFoodMF}, new Tuple(0.0,10.0), 100, true, true); 
        plotMFs("Service Level Membership Functions", new GenT2zMF_Interface[]{unfriendlyServiceMF, /**okServiceMF,**/ friendlyServiceMF}, new Tuple(0.0, 10.0), 100, true, true);
        plotMFs("Level of Tip Membership Functions", new GenT2zMF_Interface[]{lowTipMF, mediumTipMF, highTipMF}, new Tuple(0.0, 30.0), 100, true, true);     
        
        //plot control surface
        //do either height defuzzification (false) or centroid d. (true)
        System.out.println("Processing at "+xDiscs+" discretizations on the xAxis "
                + "and "+yDiscs+" discretizations on the yAxis, please wait...");  
        long time=System.currentTimeMillis();
        plotControlSurface(tip, false, xDiscs, yDiscs);
        time = System.currentTimeMillis()-time;
        System.out.println("Processing (& plotting) was completed in "+time/1000+" seconds.");
        
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
        System.out.println("The food was: "+food.getInput()+" (gaussian IT2 with a spread of : "+((IntervalT2MF_Gaussian) food.getInputMF()).getUMF().getSpread()+" for UMF and " + ((IntervalT2MF_Gaussian) food.getInputMF()).getLMF().getSpread()+" for LMF)");
        System.out.println("The service was: "+service.getInput());
        System.out.println("Using height center of sets type reduction, the zSlices based general type-2 FLS recommends a "
                + "tip of: "+rulebase.evaluate(0).get(tip));  
        System.out.println("Using centroid type reduction, the zSlices based general type-2 FLS recommends a "
                + "tip of: "+rulebase.evaluate(1).get(tip));
        
        //show the output of the raw centroids
        System.out.println("Centroid of the output for TIP (based on centroid type reduction):");
        TreeMap<Output, Object[]> centroid = rulebase.evaluateGetCentroid(1);
        Object[] centroidTip = centroid.get(tip);
        Tuple[] centroidTipXValues = (Tuple[])centroidTip[0];
        double[] centroidTipYValues = (double[])centroidTip[1];
        for(int zLevel=0;zLevel<centroidTipXValues.length;zLevel++)
            System.out.println(centroidTipXValues[zLevel]+" at y= "+centroidTipYValues[zLevel]);        
    }
    
    private void plotMFs(String name, GenT2zMF_Interface[] sets, Tuple xAxisRange, int discretizationLevel, boolean plotAsLines, boolean plotAsSurface)
    {
        if(plotAsLines)
        {
            JMathPlotter plotter = new JMathPlotter();
            plotter.plotMFasLines(sets[0].getName(), sets[0], discretizationLevel);

            for (int i=1;i<sets.length;i++)
            {
                plotter.plotMFasLines(sets[i].getName(), sets[i], discretizationLevel);
            }
            plotter.show(name);
        }
        if(plotAsSurface)
        {
            JMathPlotter plotterSurf = new JMathPlotter();
            plotterSurf.plotMFasSurface(sets[0].getName(), sets[0], xAxisRange, discretizationLevel, false);

            for (int i=1;i<sets.length;i++)
            {
                plotterSurf.plotMFasSurface(sets[i].getName(), sets[i], xAxisRange, discretizationLevel, false);
            }
            plotterSurf.show(name);
        }        
    }

    private void plotControlSurface(Output o, boolean useCentroidDefuzzification, int input1Discs, int input2Discs)
    {
        double output;
        double[] x = new double[input1Discs];
        double[] y = new double[input2Discs];
        //double[][] z = new double[x.length][y.length];
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
                
                //System.out.println("Current x = "+currentX+"  current y = "+currentY+"  output = "+output);
                z[currentY][currentX] = output;
            }    
        }
        
        //now do the plotting
        JMathPlotter plotter = new JMathPlotter();
        plotter.plotControlSurface("Control Surface",
                new String[]{food.getName(), service.getName(), "Tip"}, x, y, z, new Tuple(0.0,30.0), true); 
        plotter.show("zSlices based General Type-2 Fuzzy Logic System Control Surface for Tipping Example");
    }
    
    public static void main(String args[])
    {
        new SimplezGT2FLSNSIT2();
    }
}


