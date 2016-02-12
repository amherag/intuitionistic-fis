/*
 * GenT2zMF_Trapezoidal.java
 *
 * Created on 26 July 2008, 16:28
 *
 * Copyright 2008 Christian Wagner All Rights Reserved.
 */

package generalType2zSlices.sets;

import generic.Tuple;
import intervalType2.sets.IntervalT2MF_Trapezoidal;
import type1.sets.T1MF_Trapezoidal;


/**
 *
 * @author Christian Wagner
 */
public class GenT2zMF_Trapezoidal extends GenT2zMF_Prototype
{
    private IntervalT2MF_Trapezoidal primer;   
    
    private final boolean DEBUG = false;
    
    
    /** Creates a new instance of GenT2zMF_Trapezoidal */
    public GenT2zMF_Trapezoidal(String name, IntervalT2MF_Trapezoidal primer, int numberOfzLevels)
    {
        super(name);
        //double left_stepsize, right_stepsize;
        double[] stepsize = new double[4];    //array to hold stepsize for all 4 trap. parameters
        this.numberOfzLevels = numberOfzLevels;
        this.support = primer.getSupport();
        this.primer = primer;
        slices_fs = new Tuple[numberOfzLevels];
        slices_zValues = new double[numberOfzLevels];
        
        //setup z value stepSize
        z_stepSize = 1.0/(numberOfzLevels);
        zSlices = new IntervalT2MF_Trapezoidal[numberOfzLevels];
        stepsize[0] = (  ((T1MF_Trapezoidal)primer.getLMF()).getA() - ((T1MF_Trapezoidal)primer.getUMF()).getA()  )/(numberOfzLevels-1)/2.0;
        stepsize[1] = (  ((T1MF_Trapezoidal)primer.getLMF()).getB() - ((T1MF_Trapezoidal)primer.getUMF()).getB()  )/(numberOfzLevels-1)/2.0;
        stepsize[2] = (  ((T1MF_Trapezoidal)primer.getUMF()).getC() - ((T1MF_Trapezoidal)primer.getLMF()).getC()  )/(numberOfzLevels-1)/2.0;
        stepsize[3] = (  ((T1MF_Trapezoidal)primer.getUMF()).getD() - ((T1MF_Trapezoidal)primer.getLMF()).getD()  )/(numberOfzLevels-1)/2.0;
    

        //if(DEBUG)System.out.println("left_stepsize = "+left_stepsize+"   right_stepsize = "+right_stepsize);
        //current_z = 0;
        double[] inner = new double[4];
        double[] outer = new double[4];
        System.arraycopy (((T1MF_Trapezoidal)primer.getLMF()).getParameters(),0,inner,0,4);
        System.arraycopy (((T1MF_Trapezoidal)primer.getUMF()).getParameters(),0,outer,0,4);
        
        //add primer
        zSlices[0] = new IntervalT2MF_Trapezoidal("Slice 0",(T1MF_Trapezoidal)primer.getUMF(),(T1MF_Trapezoidal)primer.getLMF());
        
        
        
        //slices_zValues[0] = 0.0;
        slices_zValues[0] = z_stepSize;
        if(DEBUG)System.out.println(zSlices[0].toString()+"  Z-Value = "+slices_zValues[0]);


        for(int i=1; i<numberOfzLevels;i++)
        {
            slices_zValues[i] = slices_zValues[i-1]+z_stepSize; 
            inner[0]-=stepsize[0]; inner[1]-=stepsize[1];  inner[2]+=stepsize[2]; inner[3]+=stepsize[3];
            outer[0]+=stepsize[0]; outer[1]+=stepsize[1]; outer[2]-=stepsize[2]; outer[3]-=stepsize[3];
            
            //check for floating point inaccuracies
            if(inner[0]<outer[0]) inner[0] = outer[0];
            if(inner[1]<outer[1]) inner[1] = outer[1];
            if(inner[2]>outer[2]) inner[2] = outer[2];
            if(inner[3]>outer[3]) inner[3] = outer[3];
            
            zSlices[i] = new IntervalT2MF_Trapezoidal("Slice "+i, 
                    new T1MF_Trapezoidal("upper_slice "+i,outer),
                    new T1MF_Trapezoidal("lower_slice "+i,inner));
            if(DEBUG)System.out.println(zSlices[i].toString()+"  Z-Value = "+slices_zValues[i]);
        }        
    }
    
    /**
     *Creates a new instance of GenT2zMF_Trapezoidal by taking two interval type 2 sets as first and last slice as inputs.
     */
    public GenT2zMF_Trapezoidal(String name, IntervalT2MF_Trapezoidal primer0, IntervalT2MF_Trapezoidal primer1, int numberOfzLevels)
    {
        super(name);
        if(DEBUG)System.out.println("Number of zLevels: "+numberOfzLevels);
        if(DEBUG)System.out.println("Check if trapezoidal with two primers is correct!!");
        
        double lsu; //left stepSize (left wing of set), upper MFs (ie upper MFs of primer 0 and primer 1)
        double lsl; //left stepSize lower
        double rsu, rsl; //right....
        
        //check inputs
        //to be included - throw badParameterException
        
        this.numberOfzLevels = numberOfzLevels;
        this.support = primer0.getSupport();
        slices_fs = new Tuple[numberOfzLevels];
        slices_zValues = new double[numberOfzLevels];
        zSlices = new IntervalT2MF_Trapezoidal[numberOfzLevels];
        
        zSlices[0] = primer0;
        ((IntervalT2MF_Trapezoidal)zSlices[0]).setName(this.getName()+"_Slice_0");
        zSlices[zSlices.length-1] = primer1;

        z_stepSize = 1.0/(numberOfzLevels);
        slices_zValues[0] = z_stepSize;
        slices_zValues[zSlices.length-1] = 1.0;
        
        lsu = ((((T1MF_Trapezoidal)primer1.getUMF()).getParameters()[0]-((T1MF_Trapezoidal)primer0.getUMF()).getParameters()[0])/(numberOfzLevels-1));
        lsl = ((((T1MF_Trapezoidal)primer0.getLMF()).getParameters()[0]-((T1MF_Trapezoidal)primer1.getLMF()).getParameters()[0])/(numberOfzLevels-1));
        
        //fill here for middle points
        
        rsu = ((((T1MF_Trapezoidal)primer0.getUMF()).getParameters()[3]-((T1MF_Trapezoidal)primer1.getUMF()).getParameters()[3])/(numberOfzLevels-1));
        rsl = ((((T1MF_Trapezoidal)primer1.getLMF()).getParameters()[3]-((T1MF_Trapezoidal)primer0.getLMF()).getParameters()[3])/(numberOfzLevels-1));
        
        if(DEBUG)System.out.println("lsu = "+lsu+"  lsl = "+lsl+"  rsu = "+rsu+"  rsl = "+rsl);
        
        double[] inner = new double[4];
        double[] outer = new double[4];

        System.arraycopy(((T1MF_Trapezoidal)primer0.getLMF()).getParameters(),0,inner,0,3);
        System.arraycopy(((T1MF_Trapezoidal)primer0.getUMF()).getParameters(),0,outer,0,3);
        
        for(int i=1; i<numberOfzLevels-1;i++)
        {
            slices_zValues[i] = slices_zValues[i-1]+z_stepSize;
            inner[0]-=lsl; /*inner[1]+=stepSize;*/ inner[3]+=rsl;
            outer[0]+=lsu; /*outer[1]-=stepSize;*/ outer[3]-=rsu;            
            if(DEBUG)System.out.println("Slice "+i+" , inner: "+inner[0]+"  "+inner[1]+"  "+inner[2]+"   outer: "+outer[0]+"  "+outer[1]+"  "+outer[2]);
            zSlices[i] = new IntervalT2MF_Trapezoidal(this.getName()+"_Slice_"+i,
                    new T1MF_Trapezoidal("upper_slice "+i,outer),
                    new T1MF_Trapezoidal("lower_slice "+i,inner));
            if(DEBUG)System.out.println(zSlices[i].toString()+"  Z-Value = "+slices_zValues[i]);
            //current_z+=stepSize;
        }            
        
    }    
    
    /** Creates a new instance of GenT2zMF_Trapezoidal by taking an array of interval sets as inputs*/
    public GenT2zMF_Trapezoidal(String name, IntervalT2MF_Trapezoidal[] primers) 
    {
        super(name);
        this.numberOfzLevels = primers.length;
        this.support = primers[0].getSupport();
        //this.primer = primer;
        slices_fs = new Tuple[numberOfzLevels];
        slices_zValues = new double[numberOfzLevels];
        
        zSlices = new IntervalT2MF_Trapezoidal[numberOfzLevels];
        z_stepSize = 1.0/(numberOfzLevels);
        
        //slices_zValues[0] = 0.0;
        slices_zValues[0] = z_stepSize;

        System.arraycopy(primers,0,zSlices,0,primers.length);
        for(int i=0; i<numberOfzLevels;i++)
        {
            slices_zValues[i] = z_stepSize*(i+1);
            if(DEBUG)System.out.println(zSlices[i].toString()+"  Z-Value = "+slices_zValues[i]);
        }        
    }    
    
    public Object clone()
    {
        //return new GenT2zMF_Trapezoidal(name,primer,numberOfzLevels);
        System.out.println("Cloning for GenT2zMF_Trapezoidal needs to be re-implemented.");
        return null;
    }

    
    public IntervalT2MF_Trapezoidal getZSlice(int slice_number)
    {
        if(DEBUG)System.out.println("Returning xSlice number: "+slice_number);
        return (IntervalT2MF_Trapezoidal)zSlices[slice_number];
    }



    public double getLeftShoulderStart() {
        System.out.println("Shoulder methods not implemented!");
        return Double.NaN;        
    }

    public double getRightShoulderStart() {
        System.out.println("Shoulder methods not implemented!");
        return Double.NaN;         
    }    
}