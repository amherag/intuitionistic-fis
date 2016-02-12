/*
 * FLCPlant.java
 *
 * Created on 09 July 2008, 15:03
 *
 * Copyright 2008 Christian Wagner All Rights Reserved.
 */

package generalType2zSlices.system.multicore;

import generic.Output;
import generic.Tuple;
import intervalType2.system.IT2_Rulebase;
import java.util.Iterator;
import java.util.TreeMap;


/**
 *
 * @author Christian Wagner
 */
public class FLCPlant implements Runnable
{
    //private double[] results;
    private TreeMap<Output, Object[]> results;
    private int positionPointer, typeReductionType;
    private IT2_Rulebase rulebase;
    
    /** Creates a new instance of FLCPlant */
    public FLCPlant(IT2_Rulebase rulebase, TreeMap<Output, Object[]> results, int positionPointer, int typeReductionType)
    {
        this.rulebase = rulebase;
        this.results = results;
        this.typeReductionType = typeReductionType;
        this.positionPointer = positionPointer;
    }

    public int getTypeReductionType() {
        return typeReductionType;
    }

    public void setTypeReductionType(int typeReductionType) {
        this.typeReductionType = typeReductionType;
    }
    
    

    public void run() 
    {//System.out.println("tr: "+typeReductionType);
        //results[positionPointer] = rulebase.evaluate();
        Iterator<Output> it = rulebase.getOutputIterator();
        Output o;
        TreeMap<Output, Object[]> temp;
        temp = rulebase.evaluateGetCentroid(typeReductionType);
//            synchronized (rulebase) {temp = rulebase.evaluateGetCentroid(typeReductionType);}        
        while(it.hasNext())
        {
            o = it.next();
            synchronized(results){
            ((Tuple[])results.get(o)[0])[positionPointer] = (Tuple)temp.get(o)[0];
            }
        }
    }
    
}
