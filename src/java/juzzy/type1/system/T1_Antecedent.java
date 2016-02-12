/*
 * Antecedent.java
 *
 * Created on 19 November 2008, 11:49
 *
 * Copyright 2008 Christian Wagner All Rights Reserved.
 */

package type1.system;

import generic.Input;

import java.io.Serializable;

import type1.sets.T1MF_Gauangle;
import type1.sets.T1MF_Gaussian;
import type1.sets.T1MF_Interface;
import type1.sets.T1MF_Singleton;
import type1.sets.T1MF_Triangular;

/**
 * Antecedent for a fuzzy rule of a Type-1 Fuzzy System.
 * @author Christian Wagner
 */
public class T1_Antecedent implements Comparable, Serializable
{
    private String name;
    private Input input;
    private T1MF_Interface mF;  //the membership function consituting this antecedent
    
    /**
     * Creates a new instance of Antecedent 
     * @param mF The actual membership function to associated with this antecedent.
     * @param input The input to associate with this antecedent.
     */
    public T1_Antecedent(T1MF_Interface mF, Input input)
    {
        this.name = mF.getName();   //adopt name form MF
        this.input = input;
        this.mF = mF;
    }
    /**
     * Creates a new instance of Antecedent 
     * @param name Name of the antecedent.
     * @param mF The actual membership function to associated with this antecedent.
     * @param input The input to associate with this antecedent.
     */
    public T1_Antecedent(String name, T1MF_Interface mF, Input input)
    {
        this.name = name;
        this.input = input;
        this.mF = mF;
    }    

    /**
     *Allows changing the membership function defining the antecedent
     * @param mF The new membership function.
     */
    public void setMF(T1MF_Interface mF)
    {
        this.mF = mF;
    }    
    
    /**
     *Returns the membership function defining the antecedent.
     */
    public T1MF_Interface getMF()
    {
        return mF;
    }

    public Input getInput()
    {
        return input;
    }

    public String getName()
    {
        return name;
    }    

    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Returns the firing strength for the given antecedent using the current input supplied.
     * @return Firing Strength
     */
    public double getFS()
    {
        return mF.getFS(input.getInput());
    }
    
    /**
     * Returns the arg sup of the t-norm between the membership function of the antecedent and the 
     * membership function of the input (in case of NSF)
     * @param tnorm the t-norm used : product or minimum
     * @return the sup
     */
    public double getMax(int tNorm) {
    	double xmax = 0.0;

        if ((this.input.getInputMF() instanceof T1MF_Gaussian) & (this.getMF() instanceof T1MF_Gaussian)) {
    			//xmax = arg sup muXm(xm)*muFlm(xm)
    			T1MF_Gaussian gaussian = (T1MF_Gaussian) this.input.getInputMF();
    			double sigmaX = gaussian.getSpread();
    			double meanX = gaussian.getMean();
    			T1MF_Interface antecedentMF = this.mF;
    			//Case where antecedentMF is also Gaussian (general case later)
    			double sigmaF = ((T1MF_Gaussian) antecedentMF).getSpread();
    			double meanF = ((T1MF_Gaussian) antecedentMF).getMean();
    			if (tNorm==0) { //PRODUCT
    				xmax = (sigmaX*sigmaX*meanF + sigmaF*sigmaF*meanX)/(sigmaX*sigmaX + sigmaF*sigmaF);
    			} else { //MINIMUM
    				xmax = (sigmaX*meanF + sigmaF*meanX)/(sigmaX + sigmaF);
    			}
    			
    	} else { //Maximisation with discretisation
    	
    		double valxmax = 0;
    		double domain = this.input.getDomain().getRight() - this.input.getDomain().getLeft();
    		double incr = domain/(domain*50);
    		double x = 0;
    		double temp = 0;
    		for (int i=0;i<=domain*50;i++) {
    			if (tNorm ==0) {//PRODUCT
    				temp = ((T1MF_Interface) this.input.getInputMF()).getFS(x)*this.getMF().getFS(x);
    			} else { //MINIMUM
    				temp = Math.min(((T1MF_Interface) this.input.getInputMF()).getFS(x),this.getMF().getFS(x));
    			}
    			if (temp >= valxmax){
    				valxmax = temp;
    				xmax = x;
    			}
    			x = x + incr;

    		}
    	}
    	
    	
    	
    	return xmax;
    }
    
    /**
     * Allows the comparison of two antecedents, based on their membership functions.
     * @param o The other antecedent
     * @return The returned value directly depends on the compareTo method implemented
     * for the respected membership functions.
     * @throws ClassCastException 
     */
    public int compareTo(Object o) throws ClassCastException
    {          
        if (!(((T1_Antecedent)o).getMF() instanceof T1MF_Interface))
        throw new ClassCastException("A Membership function (inplementing T1MF_Interface) object is expected.");
        
        if(mF instanceof T1MF_Triangular && ((T1_Antecedent)o).getMF() instanceof T1MF_Triangular)
        {
            return ((T1MF_Triangular)mF).compareTo(((T1_Antecedent)o).getMF());
        }
        else if(mF instanceof T1MF_Gauangle && ((T1_Antecedent)o).getMF() instanceof T1MF_Gauangle)
        {
            return ((T1MF_Gauangle)mF).compareTo(((T1_Antecedent)o).getMF());
        }
        //singletons
        else if(mF instanceof T1MF_Singleton && ((T1_Antecedent)o).getMF() instanceof T1MF_Singleton)
        {
            return ((T1MF_Singleton)mF).compareTo(((T1_Antecedent)o).getMF());
        }
        //singleton with triangular
        else if(mF instanceof T1MF_Singleton && ((T1_Antecedent)o).getMF() instanceof T1MF_Triangular)
        {
            return ((T1MF_Singleton)mF).compareTo(((T1_Antecedent)o).getMF());
        }
        //triangular with singleton
        else if(mF instanceof T1MF_Triangular && ((T1_Antecedent)o).getMF() instanceof T1MF_Singleton)
        {
            return ((T1MF_Triangular)mF).compareTo(((T1_Antecedent)o).getMF());
        }
        else
            throw new ClassCastException("Antecedent - compareTo has only not "
                    + "been implemented for the provided combination of sets.");
    }
    
    @Override
    public String toString()
    {
        return "Antecedent (current input is:"+this.getInput().getInput()+"), with MF: "+ mF.toString();
    }
}
