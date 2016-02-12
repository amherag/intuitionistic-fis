/*
 * GenT2zEngine_Defuzzification.java
 *
 * Author: Christian Wagner
 * Copyright 2007 Christian Wagner All Rights Reserved.
 */

package generalType2zSlices.system;

import generalType2zSlices.sets.GenT2zMF_Discretized;
import generalType2zSlices.sets.GenT2zMF_Interface;
import generic.Tuple;
import intervalType2.sets.IntervalT2Engine_Centroid;
import type1.sets.T1MF_Discretized;


/**
 *
 * @author Christian Wagner
 */
public class GenT2zEngine_Defuzzification
{
    private T1MF_Discretized tRSet;
    private GenT2zMF_Discretized dset;    
    private double[][] dPoints;    //points of the dscretized set
    private Object[] dPoints_real;  //only the points which actually belong to the set for every vertical slice are included here, one array of doubles per vertical slice.
    private double crisp_output;    

    //testing switchable tnorms
    private final byte MINIMUM = 0;
    private final byte PRODUCT = 1;
    private final byte tnorm = MINIMUM;    
    
    
    IntervalT2Engine_Centroid IEC;
    private final boolean DEBUG = false;
    private final boolean DEBUG_S = false;
    
    /** Creates a new instance of GenT2zEngine_Defuzzification */
    public GenT2zEngine_Defuzzification(int primaryDiscretizationLevel)
    {
        IEC = new IntervalT2Engine_Centroid(primaryDiscretizationLevel);
        //IEC = new IntervalT2Engine_Centroid();
    }
    
    public Tuple typeReduce(GenT2zMF_Interface set)
    {
        if(set==null) 
        {
            if(DEBUG)System.out.println("Set is null at defuzzification stage!");
            return null;
        }
        
        double dividend_left = 0.0, divisor_left = 0.0;
        double dividend_right = 0.0, divisor_right = 0.0;
        Tuple centroid;
        
        for(int i=0;i<set.getNumberOfSlices();i++)
        {
            if(set.getZSlice(i)==null) 
            {
                if(DEBUG)System.out.println("��������������������slice is null "+i);
            }
            else
            {
                if(DEBUG)System.out.println("Computing centroid of slice: "+i+" of "+set.getNumberOfSlices());
                centroid = IEC.getCentroid(set.getZSlice(i));
                if(DEBUG)System.out.println("Centroid Computation complete: "+centroid);
                //if(DEBUG)System.out.println("For slice: "+i+" centroid = "+centroid+"   set.getZValue(i)= "+set.getZValue(i));
                if(centroid !=null)
                {
                    dividend_left += centroid.getLeft() * set.getZValue(i);
                    dividend_right += centroid.getRight() * set.getZValue(i);

                    divisor_left += set.getZValue(i);
                    divisor_right += set.getZValue(i);
                }
            }

        }
        //System.out.println("dividend_left = "+dividend_left+"    divisor_left = "+divisor_left);
        //System.out.println("dividend_left = "+dividend_right+"    divisor_left = "+divisor_right);
        return new Tuple(dividend_left/divisor_left,dividend_right/divisor_right);
        
    }

    
    /**
     *Returns a typereduced and defuzzified set using the standard general type-2 wavy slice centroid method.
     *@param set The Type 2 set to type reduce.
     *@param xResolution Determines how fine the type 2 set should be discretised along the x-axis.
     *@param yResolution Determines how fine the type 2 set should be discretised along the y-axis.
     */
    public double typeReduce_standard(GenT2zMF_Interface set, int xResolution, int yResolution)
    {
        //get discretized version of set
        dset = new GenT2zMF_Discretized(set, xResolution,yResolution);

        
        dPoints_real = new Object[xResolution];
//        double[] yDiscretizationLevels = dset.getSecondaryDiscretizationValues();
        
        int counter;

        
        //find out how many yvalues belong to each vertical slice and sift them out.
        int i,j;
        Tuple[] temp = new Tuple[yResolution];
        int[] yScliceCount = new int[xResolution];
        for(i=0;i<xResolution;i++)
        {
            counter = 0;
            for(j = 0;j<yResolution;j++)
            {
                if(dset.getSetDataAt(i,j)>0) //if the third dimension > 0
                {
                    //fill into dPoints_real;
                    //temp[counter] = new Tuple(dset.getSetDataAt(i,j),dset.getSecondaryDiscretizationValues()[j]);
                    temp[counter] = new Tuple(dset.getSetDataAt(i,j),dset.getDiscY(j));
                    //if(i==3)System.out.println("temp[counter] = "+temp[counter].toString());
                    counter++;
                }
            }
            //dPoints_real[i] = new double[counter+1];
            dPoints_real[i] = new Tuple[counter];       //+1
            System.arraycopy(temp,0,dPoints_real[i],0,counter);
        }
        
        if(DEBUG_S)
        {
            System.out.println("Number of vertical slices: " +dPoints_real.length);
            System.out.println("Vertical Slice Positions on x-Axis: ");
            for(i=0;i<xResolution;i++)
            {
                System.out.println("Slice "+i+" is at x = "+dset.getPrimaryDiscretizationValues()[i]);
            }

            System.out.println("Actual Slices:");
            printSlices(dPoints_real);
        }
        
        
        //get two-dimensional superarray containing all permutations...
        //how many rows?
        long number_of_rows=0;
        for(i=0;i<dPoints_real.length;i++)  //for every x disc
        {
            if(((Tuple[])dPoints_real[i]).length!=0)    //number of points on vertical slice
            {
                if(number_of_rows==0) number_of_rows = ((Tuple[])dPoints_real[i]).length;
                else number_of_rows*=((Tuple[])dPoints_real[i]).length;
            }
        }
        
//        long number_of_rows=((Tuple[])dPoints_real[0]).length;
//        for(i=1;i<dPoints_real.length;i++)
//        {
//            number_of_rows*=((Tuple[])dPoints_real[i]).length;
//        }        
        
        
        if(DEBUG_S)
        {
            if(DEBUG_S)System.out.println("Final array contains (long) "+ number_of_rows+" rows!");
            if(DEBUG_S)System.out.println("Final array contains (int) "+ (int)number_of_rows+" rows!");            
        }
        
        if(number_of_rows!=(int)number_of_rows) System.out.println("precision too great, integer overflow - array length not supported!");
        
        Tuple[][] wavySlices = new Tuple[(int)number_of_rows][xResolution];
        
        //retrieve permutations and put them in wavyslices array
        for(i=0;i<xResolution;i++)
        {
            counter=0;
            for(int k=0; k<wavySlices.length;k++)
            {
                if (((Tuple[])dPoints_real[i]).length!=0)       //if some vertical slices are empty (resolution too low...)
                    wavySlices[k][i] = ((Tuple[])dPoints_real[i])[counter];
                else 
                {
                    System.out.println("Setting wavy slice to null!");
                    wavySlices[k][i] = null;
                }
                
                counter++;
                if(counter==(((Tuple[])dPoints_real[i]).length)) 
                    counter=0;
            }
        }
        
        if(DEBUG_S)
        {
            System.out.println("Wavy Slices:");
            printSlices(wavySlices);
        }
        
        //now compute centroids of wavy slices using x and y
        double[] wavycentroids = new double[(int)number_of_rows];
        double dividend, divisor;
        for(i=0;i<number_of_rows;i++)
        {
            dividend = 0;
            divisor = 0;
            for(j=0;j<xResolution;j++)
            {
                if(wavySlices[i][j]==null) 
                {
                    if(DEBUG_S)
                    System.out.println("Skipping wavy slice "+i+" as its not defined at "+j);
                }
                else
                {
                    dividend += (dset.getPrimaryDiscretizationValues()[j]*wavySlices[i][j].getRight());
                    divisor += wavySlices[i][j].getRight();
                }
            }
            if(DEBUG_S)System.out.println("wavySlices - Dividend: "+dividend+"  Divisior: "+divisor);
            wavycentroids[i] = dividend/divisor;
            if(DEBUG_S) System.out.println("Centroid of wavyslice "+i+" is: "+wavycentroids[i]);
        }
        
        //get type reduced set
        if(DEBUG_S) System.out.println("Final type-reduced tuples:");
        double min = 1.0;
        Tuple[] reduced = new Tuple[(int)number_of_rows];
        

        for(i=0;i<number_of_rows;i++)
        {
            if(tnorm==MINIMUM)
            {
                min = 1.0;
                for(j=0;j<xResolution;j++)
                { 
                    if(wavySlices[i][j]!=null)
                    {//System.out.println("At "+i+" "+j+"  = "+wavySlices[i][j].getLeft());
                        min = Math.min(min,wavySlices[i][j].getLeft());
                    }
                }
            }
            else if(tnorm==PRODUCT)
            {
                min = 1.0;
                for(j=0;j<xResolution;j++)
                { 
                    if(wavySlices[i][j]!=null)
                    {//System.out.println("At "+i+" "+j+"  = "+wavySlices[i][j].getLeft());
                        min *= wavySlices[i][j].getLeft();
                    }
                }                
            }
            //tRSet.addPoint(new Tuple(min,wavycentroids[i]));
            reduced[i] = new Tuple(min,wavycentroids[i]);
            if(DEBUG_S) System.out.println(reduced[i]);
            System.out.println(reduced[i].getRight()+","+reduced[i].getLeft());
        }
        
        //add to output set
        tRSet = new T1MF_Discretized("output",reduced.length);
        tRSet.addPoints(reduced);
        
        //get crisp output:
        dividend = 0; divisor = 0;
        for(i=0;i<reduced.length;i++)
        {
            //if(reduced[i].getRight()==Double.NaN) System.out.println("************");
            //System.out.println("reduced[i].getRight() = "+reduced[i].getRight());
            dividend += reduced[i].getLeft()*reduced[i].getRight();
            divisor += reduced[i].getLeft();
        }
        if(DEBUG_S)System.out.println("Dividend: "+dividend+"  Divisior: "+divisor);
        crisp_output = dividend/divisor;
        
        //System.out.println("Crisp output = "+crisp_output);

        
        
        
        return crisp_output;
       // return tRSet;
    }    
    
    private void printSlices(Object[] o)
    {
        for(int i = 0;i<o.length;i++)
        {
            /*if(DEBUG)*/System.out.println("Slice "+i+" , with a length of: "+((Tuple[])o[i]).length);
            for(int j = 0;j<((Tuple[])o[i]).length;j++)
            {
                //System.out.println("((Tuple[])o[i]).length = "+((Tuple[])o[i]).length);
                //System.out.println(((Tuple[])o[i])[0].toString());
                if(((Tuple[])o[i])[j]!=null)System.out.print("Point "+j+": "+((Tuple[])o[i])[j].getLeft()+"/"+((Tuple[])o[i])[j].getRight()+" ");
                else System.out.print("NULL ");
            }
            System.out.println("");
        }
    }    
}
