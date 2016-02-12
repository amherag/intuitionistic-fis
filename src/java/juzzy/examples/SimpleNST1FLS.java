package examples;

import intervalType2.sets.IntervalT2MF_Gaussian;
import generic.Input;
import generic.Output;
import generic.Tuple;
import tools.JMathPlotter;
import type1.sets.T1MF_Gauangle;
import type1.sets.T1MF_Gaussian;
import type1.sets.T1MF_Interface;
import type1.sets.T1MF_Singleton;
import type1.sets.T1MF_Trapezoidal;
import type1.sets.T1MF_Triangular;
import type1.system.T1_Antecedent;
import type1.system.T1_Consequent;
import type1.system.T1_Rule;
import type1.system.T1_Rulebase;


/**
 * A simple example of a Non Singleton type-1 FLS based on the "How much to tip the waiter"
 *  scenario.
 * We have two inputs: food quality and service level and as an output we would
 * like to generate the applicable tip.
 * @author Amandine Pailloux
 */

public class SimpleNST1FLS {


	    Input food, service;    //the inputs to the FLS
	    Output tip;             //the output of the FLS
	    T1_Rulebase rulebase;   //the rulebase captures the entire FLS
	    
	    public SimpleNST1FLS()
	    {
	        //Define the inputs
	    	T1MF_Gaussian inputmf = new T1MF_Gaussian("inputmf",7,2);
	        food = new Input("Food Quality", new Tuple(0,10),inputmf);      //a rating given by a person between 0 and 10
	        service = new Input("Service Level", new Tuple(0,10));  //a rating given by a person between 0 and 10
	        tip = new Output("Tip", new Tuple(0,30));               //a percentage for the tip

	        //Set up the membership functions (MFs) for each input and output
	        T1MF_Triangular badFoodMF = new T1MF_Triangular("MF for bad food",0.0, 0.0, 10.0);
	        T1MF_Triangular greatFoodMF = new T1MF_Triangular("MF for great food",0.0, 10.0, 10.0);

	        T1MF_Gauangle unfriendlyServiceMF = new T1MF_Gauangle("MF for unfriendly service",0.0, 0.0, 6.0);
	        T1MF_Gauangle okServiceMF = new T1MF_Gauangle("MF for ok service",2.5, 5.0, 7.5);
	        T1MF_Gauangle friendlyServiceMF = new T1MF_Gauangle("MF for friendly service",4.0, 10.0, 10.0);

	        T1MF_Gaussian lowTipMF = new T1MF_Gaussian("Low tip", 0.0, 6.0);
	        T1MF_Gaussian mediumTipMF = new T1MF_Gaussian("Medium tip", 15.0, 6.0);
	        T1MF_Gaussian highTipMF = new T1MF_Gaussian("High tip", 30.0, 6.0);

	        //Set up the antecedents and consequents - note how the inputs are associated...
	        T1_Antecedent badFood = new T1_Antecedent("BadFood",badFoodMF, food);
	        T1_Antecedent greatFood = new T1_Antecedent("GreatFood",greatFoodMF, food);

	        T1_Antecedent unfriendlyService = new T1_Antecedent("UnfriendlyService",unfriendlyServiceMF, service);
	        T1_Antecedent okService = new T1_Antecedent("OkService",okServiceMF, service);
	        T1_Antecedent friendlyService = new T1_Antecedent("FriendlyService",friendlyServiceMF, service);

	        T1_Consequent lowTip = new T1_Consequent("LowTip", lowTipMF, tip);
	        T1_Consequent mediumTip = new T1_Consequent("MediumTip", mediumTipMF, tip);
	        T1_Consequent highTip = new T1_Consequent("HighTip", highTipMF, tip);

	        //Set up the rulebase and add rules
	        rulebase = new T1_Rulebase(6);
	        rulebase.addRule(new T1_Rule(new T1_Antecedent[]{badFood, unfriendlyService}, lowTip));
	        rulebase.addRule(new T1_Rule(new T1_Antecedent[]{badFood, okService}, lowTip));
	        rulebase.addRule(new T1_Rule(new T1_Antecedent[]{badFood, friendlyService}, mediumTip));
	        rulebase.addRule(new T1_Rule(new T1_Antecedent[]{greatFood, unfriendlyService}, lowTip));
	        rulebase.addRule(new T1_Rule(new T1_Antecedent[]{greatFood, okService}, mediumTip));
	        rulebase.addRule(new T1_Rule(new T1_Antecedent[]{greatFood, friendlyService}, highTip));
	        
	        //Use minimum
	        rulebase.setImplicationMethod((byte) 0);
	        rulebase.setInferenceMethod((byte) 0);
	        
	        //just an example of setting the discretisation level of an output - the usual level is 100
	        tip.setDiscretisationLevel(50);        
	        
	        //get some outputs
	        getTip(7,8);
	        getTip(0,2.5);
	        
	        //plot some sets, discretizing each input into 100 steps.
	        plotMFs("Food Quality Membership Functions", new T1MF_Interface[]{badFoodMF, greatFoodMF}, food.getDomain(), 100); 
	        plotMFs("Service Level Membership Functions", new T1MF_Interface[]{unfriendlyServiceMF, okServiceMF, friendlyServiceMF}, service.getDomain(), 100);
	        plotMFs("Level of Tip Membership Functions", new T1MF_Interface[]{lowTipMF, mediumTipMF, highTipMF}, tip.getDomain(), 100);
	       
	        //plot control surface
	        //do either height defuzzification (false) or centroid d. (true)
	        plotControlSurface(true, 100, 100);
	        
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
	        System.out.println("The food was: "+food.getInput()+" (gaussian with a spread of : "+((T1MF_Gaussian) food.getInputMF()).getSpread()+")");
	        System.out.println("The service was: "+service.getInput());
	        System.out.println("Using height defuzzification, the FLS recommends a tip of"
	                + "tip of: "+rulebase.evaluate(0).get(tip)); 
	        System.out.println("Using centroid defuzzification, the FLS recommends a tip of"
	                + "tip of: "+rulebase.evaluate(1).get(tip));     
	    }
	    
	    private void plotMFs(String name, T1MF_Interface[] sets, Tuple xAxisRange, int discretizationLevel)
	    {
	        JMathPlotter plotter = new JMathPlotter(20,20,20);
	        for (int i=0;i<sets.length;i++)
	        {
	            plotter.plotMF(sets[i].getName(), sets[i], discretizationLevel, xAxisRange, new Tuple(0.0,1.0), false);
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
	            {
	                service.setInput(y[currentY]);
	                if(useCentroidDefuzzification)
	                    output = rulebase.evaluate(1).get(tip);
	                else
	                    output = rulebase.evaluate(0).get(tip);
	                z[currentY][currentX] = output;
	            }    
	        }
	        
	        //now do the plotting
	        JMathPlotter plotter = new JMathPlotter(17, 17, 14);
	        plotter.plotControlSurface("Control Surface",
	                new String[]{food.getName(), service.getName(), "Tip"}, x, y, z, new Tuple(0.0,30.0), true);   
	       plotter.show("Type-1 Fuzzy Logic System Control Surface for Tipping Example");
	    }
	    
	    public static void main (String args[])
	    {
	        new SimpleNST1FLS();
	    }
	


}

