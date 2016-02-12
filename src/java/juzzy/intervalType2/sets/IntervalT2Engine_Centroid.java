/*
 * IntervalT2Engine_Centroid.java
 *
 * Created on 29 April 2007, 17:19
 *
 * Author: Christian Wagner
 * Copyright 2006 Christian Wagner All Rights Reserved.
 */

package intervalType2.sets;

import generic.Tuple;

/**
 * Implements Centroid calculation using the Karnik Mendel and Enhanced Karnik Mendel Iterative Procedures.
 * @author Christian Wagner
 */
public class IntervalT2Engine_Centroid
{
    private Tuple centroid;
    private int primaryDiscretizationLevel = 100;
    
    private final short KARNIKMENDEL = 0;
    private final short ENHANCEDKARNIKMENDEL = 1;
        private final double ENHANCEDKARNIKMENDEL_L0 = 2.4;
        private final double ENHANCEDKARNIKMENDEL_R0 = 1.7;
            
    private final short centroid_algorithm_selector = KARNIKMENDEL;
    private final boolean log = false; //log for comparison
    private final boolean DEBUG = false;
    
    /** Creates a new instance of GenT2Engine_Centroid */
    public IntervalT2Engine_Centroid()
    {
        
    }

    public IntervalT2Engine_Centroid(int primaryDiscretizationLevel)
    {
        this.primaryDiscretizationLevel = primaryDiscretizationLevel;
    }    
    
    public int getPrimaryDiscretizationLevel()
    {
        return primaryDiscretizationLevel;
    } 
    
    public void setPrimaryDiscretizationLevel(int primaryDiscretizationLevel)
    {
        this.primaryDiscretizationLevel = primaryDiscretizationLevel;
    }     
    
    public Tuple getCentroid(IntervalT2MF_Interface mf)
    {
        //System.out.println("Change back to KM!!!!!!");
        
        switch(centroid_algorithm_selector)
        {
            case KARNIKMENDEL:
            {
                centroid = getCentroidKM(mf);
                break;
            }
            
            case ENHANCEDKARNIKMENDEL:
            {
                centroid = getCentroid_enhanced(mf,ENHANCEDKARNIKMENDEL_L0,ENHANCEDKARNIKMENDEL_R0);
                break;
            }            
        }
        
        return centroid;
    }
    
    private Tuple getCentroidKM(IntervalT2MF_Interface mf)
    {
        if(mf==null) return new Tuple(Double.NaN, Double.NaN);

        if(mf instanceof IntervalT2MF_Intersection && !((IntervalT2MF_Intersection)mf).intersectionExists())  return new Tuple(Double.NaN, Double.NaN);
        Tuple[] w;
        double[] x;
        double[] weights;
        //double[] weightsLeft;       //used to keep weights array intact for calculation of y_r
        double y, yDash, y_l=0, y_r=0;
        double domainSize;  //stores the size of the domain over which the function is evaluated
        double temp;    //stores various bits for efficiency purposes (avoid recalculation
        int k;          //switch point
        boolean stopFlag = false;   //set to true if correct yDash is found.
        boolean stopFlagRight = false;   //set to true if correct yDash is found for right.

        int iterationCounterLeft = 0, iterationCounterRight=0;
        

        //get domain size
        if(DEBUG)System.out.println("MF name = "+mf.getName());
        if(mf instanceof IntervalT2MF_Union &&((IntervalT2MF_Union)mf).isNull())
            return null;
        if(DEBUG)System.out.println("domain left point is: "+mf.getSupport().getLeft()+"  and right point is: "+mf.getSupport().getRight() );
        if(mf.getSupport().getRight()==mf.getSupport().getLeft()) 
            return mf.getSupport().clone();  //domain is one point - return this point
        
        domainSize = mf.getSupport().getRight()-mf.getSupport().getLeft();
//System.out.println("domain: "+mf.getSupport());
        //setup arrays for primaryDiscretizationLevel
        w = new Tuple[primaryDiscretizationLevel];
        x = new double[primaryDiscretizationLevel];
        weights = new double[primaryDiscretizationLevel];
        //weightsLeft = new double[primaryDiscretizationLevel+1];
//System.out.println("prim disc in KM= "+primaryDiscretizationLevel);
        //compute lower and upper membership value w at x over primaryDiscretizationLevel (discretize domain primaryDiscretizationLevel times)
        temp = domainSize/(primaryDiscretizationLevel-1);
        if(DEBUG)System.out.println("Domainsize = "+domainSize+",  discLevel = "+primaryDiscretizationLevel+",  stepSize = "+temp);
        for (int i=0; i<primaryDiscretizationLevel;i++)
        {
            x[i] = i*temp+mf.getSupport().getLeft(); //discretize and shift - starting from domain starting point
            w[i] = mf.getFS(x[i]);
            //if(i==primaryDiscretizationLevel)System.out.println("x = "+x[i]+" and y = "+w[i]);
            weights[i] = (w[i].getLeft()+w[i].getRight()) / 2;      //(8)
            
            if(DEBUG)System.out.println("KM x disc. number: "+i+"  = "+x[i]+ "  y(w[i]) = "+w[i]+"   weights[i] = "+weights[i]);
        }        

        for(byte runs=0;runs<2;runs++) //run once for left, once for right
        {//System.out.println("runs = "+runs);
            stopFlag = false;

        for (int i=0; i<primaryDiscretizationLevel;i++)
        {
            //x[i] = i*temp+mf.getSupport().getLeft(); //discretize and shift - starting from domain starting point
            w[i] = mf.getFS(x[i]); 
            weights[i] = (w[i].getLeft()+w[i].getRight()) / 2;      //(8)
        
        }            
            
            y = getWeightedSum(x,weights);                                  //(9)
            if(DEBUG)System.out.println("y = "+y);

            while(!stopFlag)
            {
                if(log)
                {
                    if(runs==0)iterationCounterLeft++;
                    else iterationCounterRight++;
                }
                for(k=0;k<primaryDiscretizationLevel-1;k++)
                //for(k=1;k<primaryDiscretizationLevel;k++)
                {if(DEBUG)System.out.println("k = "+k+"  y = "+y+"  x[k] = "+x[k]+"  x[k+1] = " +x[k+1]);
                    if(x[k]<=y && y<=x[k+1])                                //10
                        break;
                 if(k==(primaryDiscretizationLevel-2))
                     System.out.println("###################################  NO k WAS  FOUND! ###################### for: "+mf.getName()+"\n"+mf);
                }

                //reset weights according to k                              //11
                if(runs==0)
                {
                    if(DEBUG)System.out.println("Doing left   k = "+k+"   and primaryDiscretizationLevel = "+primaryDiscretizationLevel);
                    for(int i=0;i<=k;i++)
                        //weightsLeft[i] = w[i].getRight();
                        weights[i] = w[i].getRight();
                    for(int i=k+1;i<primaryDiscretizationLevel;i++)
                        //weightsLeft[i] = w[i].getLeft();
                        weights[i] = w[i].getLeft();
                }
                else
                {
                    if(DEBUG)System.out.println("Doing right   k = "+k+"   and primaryDiscretizationLevel = "+primaryDiscretizationLevel);
                    for(int i=0;i<=k;i++)
                        weights[i] = w[i].getLeft();
                    for(int i=k+1;i<primaryDiscretizationLevel;i++)
                        weights[i] = w[i].getRight();                    
                }

                yDash = getWeightedSum(x, weights);
                if(DEBUG)System.out.println("yDash = "+yDash+"   and y = "+y+ "   y_l="+y_l+"  y_r="+y_r);
                //if(yDash == y)
                if(Double.isNaN(yDash))
                {
                    if(DEBUG)System.out.println("Is using NAN in KM the right thing to avoid divide by zero? "+mf);
                    double step = mf.getSupport().getSize()/9;
                    double value;
                    for (int i=0;i<10;i++)
                    {
                        value = i*step+mf.getSupport().getLeft();
                        if(DEBUG)System.out.println("FS for set at "+value+" is: "+mf.getFS(value));
                    }
                    yDash = y;
                }

                if(Math.abs(yDash-y)<0.001)
                {if(DEBUG)System.out.println("SUCCESS! - y = "+y);
                    stopFlag = true;
                    if(runs==0)
                    {
                        y_l = yDash;
                    }
                    else
                    {
                        y_r = yDash;
                    }
                }
                else
                {
                    //if(yDash ==0)System.out.println("yDash is 0");
                    y = yDash;
                }

            }       
        }
        //if(log)System.out.println("Iterations for y_l: "+iterationCounterLeft+"   iterations for y_r: "+iterationCounterRight);
        //return new Range(y_l,y_r);	
                    //test

        return new Tuple(y_l,y_r);
        //return new Tuple[]{new Tuple(y_l,y_r),new Tuple(iterationCounterLeft,iterationCounterRight)};
        
     
    }    

    private Tuple getCentroid_enhanced(IntervalT2MF_Interface mf, double divisor_left, double divisor_right)    //parameter to divide primaryDiscretizationLevel, usually 2.4
    {
        Tuple[] w;
        double[] x;
        double[] weights;
        //double[] weightsLeft;       //used to keep weights array intact for calculation of y_r
        double y=0, yDash=0, y_l=0, y_r=0;
        double domainSize;  //stores the size of the domain over which the function is evaluated
        double temp;    //stores various bits for efficiency purposes (avoid recalculation
        int k, kDash;          //switch points
        double a=0,b=0,aDash=0, bDash=0, s=0;
        boolean stopFlag = false;   //set to true if correct yDash is found.
        boolean stopFlagRight = false;   //set to true if correct yDash is found for right.

        int iterationCounterLeft = 0, iterationCounterRight=0;
        boolean log = true; //log for comparison

        //get domain size
        domainSize = mf.getSupport().getRight()-mf.getSupport().getLeft();

        //setup arrays for primaryDiscretizationLevel
        w = new Tuple[primaryDiscretizationLevel+1];
        x = new double[primaryDiscretizationLevel+1];
        weights = new double[primaryDiscretizationLevel+1];
        //weightsLeft = new double[primaryDiscretizationLevel+1];

        //compute lower and upper membership value w at x over primaryDiscretizationLevel (discretize domain primaryDiscretizationLevel times)
        temp = domainSize/primaryDiscretizationLevel;

        //set x and calculate weights (membership values for left/right (bottom/top)
        for (int i=0; i<=primaryDiscretizationLevel;i++)
        {
                x[i] = i*temp+mf.getSupport().getLeft(); //discretize and shift - starting from domain starting point
                //x[i] = i*temp;    //use silly discretization
                //x[i] = i/100.0;    //use very silly discretization
                //System.out.println(x[i]);

                w[i] = mf.getFS(x[i]);                
        }


        //left
        {
            stopFlag = false;           

            //calculate k;
            k = (int)Math.round(primaryDiscretizationLevel/divisor_left);

            //System.out.println("k = "+k);
            a=0;b=0;
            //System.out.println("Domainsize = "+domainSize+"   domain left = "+domain.getLeft()+"    domain right = "+domain.getRight());
            for (int i=0; i<=k;i++)
            {


                a += x[i]*w[i].getRight();
                b += w[i].getRight();
            }
            for (int i=k+1; i<=primaryDiscretizationLevel;i++)
            {
                a += x[i]*w[i].getLeft();
                b += w[i].getLeft();
            }            

                y = a/b;                                  //(23)





            //step 3
            while(!stopFlag)
            {
                //System.out.println("y = "+y);
                if(log)iterationCounterLeft++;

                for(kDash=0;kDash<primaryDiscretizationLevel;kDash++)
                {//System.out.println("y = "+y+"     and x[kDash]= "+x[kDash]);
                    if(x[kDash]<=y && y<=x[kDash+1])                                //10
                    //if((x[kDash]-y<0.0005) && (y-x[kDash+1]<0.0005))                                //10
                        break;
                }
                System.out.println("kDash = "+kDash+"   k = "+k);
                if(kDash == k)
                {
                    stopFlag = true;

                    y_l = y;

                }
                else
                {
                    s = kDash-k;
                    if (s<0) s=-1; else s=1;

                    //System.out.println("s = "+s);

                    for(int i=(Math.min(k,kDash)+1);i<=Math.max(k,kDash);i++)
                    {
                        aDash += x[i]*(w[i].getRight()-w[i].getLeft());
                        bDash += (w[i].getRight()-w[i].getLeft());
                    }
                    aDash = a + s * aDash;
                    bDash = b + s * bDash;

                    yDash = aDash/bDash;

                    y = yDash;
                    a = aDash;
                    b = bDash;
                    k = kDash;

                    aDash=0; bDash=0;
                }

            }       
        }//left complete

        //right
        {
            stopFlag = false;           

            //calculate k;
            k = (int)Math.round(primaryDiscretizationLevel/divisor_right);

            //System.out.println("k = "+k);
            a=0;b=0;
            //System.out.println("Domainsize = "+domainSize+"   domain left = "+domain.getLeft()+"    domain right = "+domain.getRight());
            for (int i=0; i<=k;i++)
            {
                a += x[i]*w[i].getLeft();
                b += w[i].getLeft();
            }
            for (int i=k+1; i<=primaryDiscretizationLevel;i++)
            {
                a += x[i]*w[i].getRight();
                b += w[i].getRight();
            }            

                y = a/b;                                  //(23)





            //step 3
            while(!stopFlag)
            {
                //System.out.println("y = "+y);
                if(log)iterationCounterRight++;


                for(kDash=0;kDash<primaryDiscretizationLevel;kDash++)
                {//System.out.println("y = "+y+"     and x[kDash]= "+x[kDash]);
                    if(x[kDash]<=y && y<=x[kDash+1])                                //10
                    //if((x[kDash]-y<0.0005) && (y-x[kDash+1]<0.0005))                                //10
                        break;
                }
                //System.out.println("kDash = "+kDash);
                if(kDash == k)
                {
                    stopFlag = true;

                    y_r = y;

                }
                else
                {
                    s = kDash-k;
                    if (s<0) s=-1; else s=1;

                    //System.out.println("s = "+s);

                    for(int i=(Math.min(k,kDash)+1);i<=Math.max(k,kDash);i++)
                    {
                        aDash += x[i]*(w[i].getRight()-w[i].getLeft());
                        bDash += (w[i].getRight()-w[i].getLeft());
                    }
                    aDash = a - s * aDash;
                    bDash = b - s * bDash;

                    yDash = aDash/bDash;

                    y = yDash;
                    a = aDash;
                    b = bDash;
                    k = kDash;

                    aDash=0; bDash=0;
                }

            }                       
        }
        //if(log)System.out.println("Iterations for y_l: "+iterationCounterLeft+"   iterations for y_r: "+iterationCounterRight);
        
//        return new Tuple[]{new Tuple(y_l,y_r),new Tuple(iterationCounterLeft,iterationCounterRight)};
        return new Tuple(y_l,y_r);
    }      
    
    private double getWeightedSum(double x[], double w[])
    {
        double temp=0.0, temp2=0.0;

        for (int i=0;i<x.length;i++)
        {//System.out.println("x = "+x[i]+ "  w = "+w[i]);
            temp += x[i]*w[i];
            temp2 += w[i];
        }//System.out.println("temp = "+temp+"   temp2 = "+temp2);
        if(temp2!=0)
            return temp/temp2;
        else
        {

            return Double.NaN;
            //return 0;
            //return temp;
        }
    }            
}
