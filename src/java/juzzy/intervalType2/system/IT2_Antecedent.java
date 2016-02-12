/*
 * Antecedent.java
 *
 * Copyright 2007 Christian Wagner All Rights Reserved.
 */

package intervalType2.system;

import type1.sets.T1MF_Gaussian;
import type1.sets.T1MF_Interface;
import generic.Input;
import generic.Tuple;
import intervalType2.sets.IntervalT2MF_Gauangle;
import intervalType2.sets.IntervalT2MF_Interface;
import intervalType2.sets.IntervalT2MF_Triangular;

/**
 * Antecedent class for Interval Type-2 FLSs
 * @author Christian Wagner
 */
public class IT2_Antecedent
{
    private String name;
    private IntervalT2MF_Interface mF;
    private Input input;
    private final boolean debug = false;

    public IT2_Antecedent(IntervalT2MF_Interface m, Input i)
    {
        mF = m;
        this.name = mF.getName();
        input = i;
    }
    public IT2_Antecedent(String name, IntervalT2MF_Interface m, Input i)
    {
        this.name = name;
        mF = m;
        input = i;
    }    
    
    public IntervalT2MF_Interface getMF()
    {
        return mF;
    }

    public Tuple getFS()
    {
        if(debug) System.out.println("Input = "+input.getInput());
        if(debug) System.out.println("MF is:  "+mF.getName());
        if(debug) System.out.println("Result is: "+mF.getFS(input.getInput()).toString());
        return mF.getFS(input.getInput());
    }

    public void setInput(Input input)
    {
        this.input = input;
    }

    public Input getInput()
    {
        return input;
    }       

    public IntervalT2MF_Interface getSet()
    {
        return mF;
    }
    
    public String getName()
    {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Returns the arg sup of the t-norm between the membership function of the antecedent and the 
     * membership function of the input (in case of NSF)
     * @param tnorm the t-norm used : product or minimum
     * @return the arg sups
     */
    public Tuple getMax(int tNorm) {
    	Tuple xmax = new Tuple(0.0,0.0);
		double valxmaxl = 0;
		double valxmaxu = 0;
		double domain = this.input.getDomain().getRight() - this.input.getDomain().getLeft();
		double incr = 1.0/50.0;
		double x = 0;
		double templ = 0;
		double tempu = 0;
    	//Maximisation with discretisation
    	if (this.input.getInputMF() instanceof T1MF_Interface) {
    		for (int i=0;i<=domain*50;i++) {
    			if (tNorm ==0) {//PRODUCT
    				templ = ((T1MF_Interface) this.input.getInputMF()).getFS(x)*this.getMF().getLMF().getFS(x);
    				tempu = ((T1MF_Interface) this.input.getInputMF()).getFS(x)*this.getMF().getUMF().getFS(x);
    			} else { //MINIMUM
    				templ = Math.min(((T1MF_Interface) this.input.getInputMF()).getFS(x),this.getMF().getLMF().getFS(x));
    				tempu = Math.min(((T1MF_Interface) this.input.getInputMF()).getFS(x),this.getMF().getUMF().getFS(x));      			
    			}
    			if (templ >= valxmaxl){
    				valxmaxl = templ;
    				xmax.setLeft(x);
    			}
    			if (tempu >= valxmaxu){
    				valxmaxu = tempu;
    				xmax.setRight(x);
    			}
    			x = x + incr;		
    		}
    	} else if (this.input.getInputMF() instanceof IntervalT2MF_Interface){//IT2 input
    			for (int i=0;i<=domain*50;i++) {
        			if (tNorm ==0) {//PRODUCT
        				templ = ((IntervalT2MF_Interface) this.input.getInputMF()).getFS(x).getLeft()*this.getMF().getFS(x).getLeft();
        				tempu = ((IntervalT2MF_Interface) this.input.getInputMF()).getFS(x).getRight()*this.getMF().getFS(x).getRight();
        			} else { //MINIMUM
        				templ = Math.min(((IntervalT2MF_Interface) this.input.getInputMF()).getFS(x).getLeft(),this.getMF().getFS(x).getLeft());
        				tempu = Math.min(((IntervalT2MF_Interface) this.input.getInputMF()).getFS(x).getRight(),this.getMF().getFS(x).getRight());      			
        			}
        			if (templ >= valxmaxl){
        				valxmaxl = templ;
        				xmax.setLeft(x);
        			}
        			if (tempu >= valxmaxu){
        				valxmaxu = tempu;
        				xmax.setRight(x);
        			}
        			x = x + incr;
    		}   		
    	}   	
    	return xmax;
    }
    
    @Override
    public String toString()
    {
        return "IT2 Antecedent (current input is:"+this.getInput().getInput()+"), with MF: "+ mF;
    }

    public int compareTo(Object o) throws ClassCastException
    {
        if (!(((IT2_Antecedent)o).getMF() instanceof IntervalT2MF_Interface))
        throw new ClassCastException("A Membership function (inplementing T1MF_Interface) object is expected.");

        if(mF instanceof IntervalT2MF_Triangular && ((IT2_Antecedent)o).getMF() instanceof IntervalT2MF_Triangular)
        {
            return ((IntervalT2MF_Triangular)mF).compareTo(((IT2_Antecedent)o).getMF());
        }
        else if(mF instanceof IntervalT2MF_Gauangle && ((IT2_Antecedent)o).getMF() instanceof IntervalT2MF_Gauangle)
        {
            return ((IntervalT2MF_Gauangle)mF).compareTo(((IT2_Antecedent)o).getMF());
        }
        else
            throw new ClassCastException("Antecedent - compareTo has only been implemented for two T1MF_Triangular and T1MF_Gauangle sets.");
    }
}
