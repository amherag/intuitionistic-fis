/*
 * Input.java
 *
 * Created on 16 May 2007, 11:25
 * updated on 13 August 2014
 * Author: Christian Wagner
 * Copyright 2006 Christian Wagner All Rights Reserved.
 */

package generic;

import generalType2zSlices.sets.GenT2zMF_Gaussian;
import generalType2zSlices.sets.GenT2zMF_Interface;
import generalType2zSlices.sets.GenT2zMF_Trapezoidal;
import generalType2zSlices.sets.GenT2zMF_Triangular;
import intervalType2.sets.IntervalT2MF_Gauangle;
import intervalType2.sets.IntervalT2MF_Gaussian;
import intervalType2.sets.IntervalT2MF_Interface;
import intervalType2.sets.IntervalT2MF_Trapezoidal;
import intervalType2.sets.IntervalT2MF_Triangular;

import java.io.Serializable;
import java.util.ArrayList;

import type1.sets.T1MF_Gauangle;
import type1.sets.T1MF_Gaussian;
import type1.sets.T1MF_Interface;
import type1.sets.T1MF_Singleton;
import type1.sets.T1MF_Trapezoidal;
import type1.sets.T1MF_Triangular;

/**
 * The Input class allows the management and updating of one input, for example
 * as part of fuzzy membership functions.
 * @author Christian Wagner
 * @author Amandine Pailloux
 */
public class Input implements Serializable
{
    protected double x;
    private String name;
    private Tuple domain;
    private MF_Interface inputMF;
    

    public Input(String name, Tuple domain)
    {
            this.name = name;
            this.domain = domain;
            this.x = 0;     
            this.inputMF = new T1MF_Singleton(x);
    }	
    public Input(String name, Tuple domain, double x)
    {
            this.name = name;
            this.domain = domain;        
            this.x=x;
            this.inputMF = new T1MF_Singleton(x); //If there is any precision in input the inputMF is by default a singleton
    }

    public Input(String name, Tuple domain, T1MF_Interface inputMF)
    {
            this.name = name;
            this.domain = domain; 
            this.inputMF = inputMF;
            this.x = inputMF.getPeak();
            
    }
    
    public Input(String name, Tuple domain, IntervalT2MF_Interface inputMF)
    {
            this.name = name;
            this.domain = domain; 
            this.inputMF = inputMF;
            this.x = inputMF.getPeak();
            
    }
    
    public Input(String name, Tuple domain, GenT2zMF_Interface inputMF)
    {
            this.name = name;
            this.domain = domain; 
            this.inputMF = inputMF;
            this.x = inputMF.getPeak();
            
    }
    
    public Input(){}   //no args constructor for serialization

    public String getName()
    {
        return name;
    }
    public void setName(String name) 
    {
        this.name = name;
    }   
    public double getInput()
    {
        return x;
    }
    public Tuple getDomain()
    {
        return domain;
    }
    public void setDomain(Tuple domain) 
    {
        this.domain = domain;
    }

    /**
     * Set the numeric input value x for this input and change its membership function
     * @param x The numeric value
     */
    public void setInput(double x)
    {
        if(domain.contains(x)) {
            this.x = x;
            MF_Interface inMF = this.inputMF;
            String nameMF = inMF.getName();
            if (inMF instanceof T1MF_Interface) {
	            if (inMF instanceof T1MF_Singleton) {
	            	this.inputMF = new T1MF_Singleton(x);
	            } else if (inMF instanceof T1MF_Gaussian) {
	            	double spread = ((T1MF_Gaussian) inMF).getSpread();
	            	this.inputMF = new T1MF_Gaussian(nameMF,x,spread);
	            } else if (inMF instanceof T1MF_Gauangle) {
	            	double start = ((T1MF_Gauangle) inMF).getStart();
	            	double end = ((T1MF_Gauangle) inMF).getEnd();
	            	double mean = ((T1MF_Gauangle) inMF).getMean();
	            	this.inputMF = new T1MF_Gauangle(nameMF,start+(x-mean),x,end+(x-mean));
	            } else if (inMF instanceof T1MF_Triangular) {
	            	double start = ((T1MF_Triangular) inMF).getStart();
	            	double end = ((T1MF_Triangular) inMF).getEnd();
	            	double mean = ((T1MF_Triangular) inMF).getPeak();
	            	this.inputMF = new T1MF_Triangular(nameMF,start+(x-mean),x,end+(x-mean));
	            } else if (inMF instanceof T1MF_Trapezoidal) {
	            	double[] params = new double[4];
	            	params[0] = ((T1MF_Trapezoidal) inMF).getA();
	            	params[1] = ((T1MF_Trapezoidal) inMF).getB();
	            	params[2] = ((T1MF_Trapezoidal) inMF).getC();
	            	params[3] = ((T1MF_Trapezoidal) inMF).getD();
	            	double mid = (params[1]+params[2])/2;
	            	double d = x-mid;
	            	params[0] = params[0] + d;
	            	params[1] = params[1] + d;
	            	params[2] = params[2] + d;
	            	params[3] = params[3] + d;
	            	this.inputMF = new T1MF_Trapezoidal(nameMF,params);
	            }
            } else if (inMF instanceof IntervalT2MF_Interface) {
            	if (inMF instanceof IntervalT2MF_Gaussian) {
            		T1MF_Gaussian lmf = ((IntervalT2MF_Gaussian) inMF).getLMF();
            		String namel = lmf.getName();
            		double spreadl = lmf.getSpread();
            		T1MF_Gaussian umf = ((IntervalT2MF_Gaussian) inMF).getUMF();
            		String nameu = umf.getName();
            		double spreadu = umf.getSpread();
	            	this.inputMF = new IntervalT2MF_Gaussian(nameMF,new T1MF_Gaussian(nameu,x,spreadu),new T1MF_Gaussian(namel,x,spreadl));
	            } else if (inMF instanceof IntervalT2MF_Gauangle) {
	            	T1MF_Gauangle lmf = (T1MF_Gauangle) ((IntervalT2MF_Gauangle) inMF).getLMF();
            		String namel = lmf.getName();
	            	double startl = lmf.getStart();
	            	double endl = lmf.getEnd();
	            	double meanl = lmf.getMean();
	            	T1MF_Gauangle umf = (T1MF_Gauangle) ((IntervalT2MF_Gauangle) inMF).getUMF();
            		String nameu = umf.getName();
	            	double startu = umf.getStart();
	            	double endu = umf.getEnd();
	            	double meanu = umf.getMean();
	            	this.inputMF = new IntervalT2MF_Gauangle(nameMF,new T1MF_Gauangle(nameu,startu+(x-meanu),x,endu+(x-meanu)),new T1MF_Gauangle(namel,startl+(x-meanl),x,endl+(x-meanl)));
	            } else if (inMF instanceof IntervalT2MF_Triangular) {
	            	T1MF_Triangular lmf = (T1MF_Triangular) ((IntervalT2MF_Triangular) inMF).getLMF();
            		String namel = lmf.getName();
	            	double startl = lmf.getStart();
	            	double endl = lmf.getEnd();
	            	double meanl = lmf.getPeak();
	            	T1MF_Triangular umf = (T1MF_Triangular) ((IntervalT2MF_Triangular) inMF).getUMF();
            		String nameu = umf.getName();
	            	double startu = umf.getStart();
	            	double endu = umf.getEnd();
	            	double meanu = umf.getPeak();
	            	this.inputMF = new IntervalT2MF_Triangular(nameMF,new T1MF_Triangular(nameu,startu+(x-meanu),x,endu+(x-meanu)),new T1MF_Triangular(namel,startl+(x-meanl),x,endl+(x-meanl)));
	            } else if (inMF instanceof IntervalT2MF_Trapezoidal) {
	            	double[] params = new double[4];
	            	T1MF_Trapezoidal lmf = (T1MF_Trapezoidal) ((IntervalT2MF_Interface) inMF).getLMF();
	            	params[0] = lmf.getA();
	            	params[1] = lmf.getB();
	            	params[2] = lmf.getC();
	            	params[3] = lmf.getD();
	            	double mid = (params[1]+params[2])/2;
	            	double d = x-mid;
	            	params[0] = params[0] + d;
	            	params[1] = params[1] + d;
	            	params[2] = params[2] + d;
	            	params[3] = params[3] + d;
	            	T1MF_Trapezoidal LMF = new T1MF_Trapezoidal(lmf.getName(),params);
	            	T1MF_Trapezoidal umf = (T1MF_Trapezoidal) ((IntervalT2MF_Interface) inMF).getUMF();
	            	params[0] = umf.getA();
	            	params[1] = umf.getB();
	            	params[2] = umf.getC();
	            	params[3] = umf.getD();
	            	mid = (params[1]+params[2])/2;
	            	d = x-mid;
	            	params[0] = params[0] + d;
	            	params[1] = params[1] + d;
	            	params[2] = params[2] + d;
	            	params[3] = params[3] + d;
	            	T1MF_Trapezoidal UMF = new T1MF_Trapezoidal(umf.getName(),params);
	            	this.inputMF = new IntervalT2MF_Trapezoidal(nameMF,UMF,LMF);
	            }
            } else if (inMF instanceof GenT2zMF_Interface) {
            	int nZ=((GenT2zMF_Interface) inMF).getNumberOfSlices();
            	if (inMF instanceof GenT2zMF_Gaussian) {
            		
            		IntervalT2MF_Gaussian[] it2s = new IntervalT2MF_Gaussian[nZ];
            		for (int i =0;i<nZ;i++) {
            			IntervalT2MF_Gaussian temp = (IntervalT2MF_Gaussian) ((GenT2zMF_Interface) inMF).getZSlice(i);
            			T1MF_Gaussian lmf = temp.getLMF();
                		String namel = lmf.getName();
                		double spreadl = lmf.getSpread();
                		T1MF_Gaussian umf = temp.getUMF();
                		String nameu = umf.getName();
                		double spreadu = umf.getSpread();
                		temp = new IntervalT2MF_Gaussian(nameMF,new T1MF_Gaussian(nameu,x,spreadu),new T1MF_Gaussian(namel,x,spreadl));
            			it2s[i]=temp;
            		}
	            	this.inputMF = new GenT2zMF_Gaussian(nameMF,it2s);
            	} else if (inMF instanceof GenT2zMF_Triangular) {
            		IntervalT2MF_Triangular[] it2s = new IntervalT2MF_Triangular[nZ];
            		for (int i =0;i<nZ;i++) {
            			IntervalT2MF_Triangular temp = (IntervalT2MF_Triangular) ((GenT2zMF_Interface) inMF).getZSlice(i);
            			T1MF_Triangular lmf = temp.getLMF();
                		String namel = lmf.getName();
    	            	double startl = lmf.getStart();
    	            	double endl = lmf.getEnd();
    	            	double meanl = lmf.getPeak();
    	            	T1MF_Triangular umf = temp.getUMF();
                		String nameu = umf.getName();
    	            	double startu = umf.getStart();
    	            	double endu = umf.getEnd();
    	            	double meanu = umf.getPeak();
    	            	temp = new IntervalT2MF_Triangular(nameMF,new T1MF_Triangular(nameu,startu+(x-meanu),x,endu+(x-meanu)),new T1MF_Triangular(namel,startl+(x-meanl),x,endl+(x-meanl)));            			it2s[i]=temp;
    	            	it2s[i]=temp;
            		}
            		this.inputMF = new GenT2zMF_Triangular(nameMF,it2s);
            	} else if (inMF instanceof GenT2zMF_Trapezoidal) {
            		IntervalT2MF_Trapezoidal[] it2s = new IntervalT2MF_Trapezoidal[nZ];
            		for (int i =0;i<nZ;i++) {
            			IntervalT2MF_Trapezoidal temp = (IntervalT2MF_Trapezoidal) ((GenT2zMF_Interface) inMF).getZSlice(i);
            			double[] params = new double[4];
    	            	T1MF_Trapezoidal lmf = (T1MF_Trapezoidal) temp.getLMF();
    	            	params[0] = lmf.getA();
    	            	params[1] = lmf.getB();
    	            	params[2] = lmf.getC();
    	            	params[3] = lmf.getD();
    	            	double mid = (params[1]+params[2])/2;
    	            	double d = x-mid;
    	            	params[0] = params[0] + d;
    	            	params[1] = params[1] + d;
    	            	params[2] = params[2] + d;
    	            	params[3] = params[3] + d;
    	            	T1MF_Trapezoidal LMF = new T1MF_Trapezoidal(lmf.getName(),params);
    	            	T1MF_Trapezoidal umf = (T1MF_Trapezoidal) temp.getUMF();
    	            	params[0] = umf.getA();
    	            	params[1] = umf.getB();
    	            	params[2] = umf.getC();
    	            	params[3] = umf.getD();
    	            	mid = (params[1]+params[2])/2;
    	            	d = x-mid;
    	            	params[0] = params[0] + d;
    	            	params[1] = params[1] + d;
    	            	params[2] = params[2] + d;
    	            	params[3] = params[3] + d;
    	            	T1MF_Trapezoidal UMF = new T1MF_Trapezoidal(umf.getName(),params);
    	            	temp = new IntervalT2MF_Trapezoidal(nameMF,UMF,LMF);
    	            	it2s[i] = temp;
            		}
            		this.inputMF = new GenT2zMF_Trapezoidal(nameMF,it2s);
	            }
        	
        } else {
            throw new BadParameterException("The input value "+x+" was rejected "
                    + "as it is outside of the domain for this input: "
                    + "["+domain.getLeft()+", "+domain.getRight()+"].");
        }
        }
    }    
    
    
    public MF_Interface getInputMF() {
		return inputMF;
	}
    
	public void setInputMF(T1MF_Interface inputMF) {
		if(domain.contains(inputMF.getPeak())) {
            this.x = inputMF.getPeak();
            this.inputMF = inputMF;
        	
        } else {
            throw new BadParameterException("The inputMF was rejected "
                    + "as it is outside of the domain for this input: "
                    + "["+domain.getLeft()+", "+domain.getRight()+"].");
        }
	}	
	
	public void setInputMF(IntervalT2MF_Interface inputMF) {
		if(domain.contains(inputMF.getPeak())) {
            this.x = inputMF.getPeak();
            this.inputMF = inputMF;
        	
        } else {
            throw new BadParameterException("The inputMF was rejected "
                    + "as it is outside of the domain for this input: "
                    + "["+domain.getLeft()+", "+domain.getRight()+"].");
        }
	}
	
	public void setInputMF(GenT2zMF_Interface inputMF) {
		if(domain.contains(inputMF.getPeak())) {
            this.x = inputMF.getPeak();
            this.inputMF = inputMF;
        	
        } else {
            throw new BadParameterException("The inputMF was rejected "
                    + "as it is outside of the domain for this input: "
                    + "["+domain.getLeft()+", "+domain.getRight()+"].");
        }
	}
	
	public String toString()
    {
        return "Input: '"+name+"' with value: "+x;
    }
	
}
