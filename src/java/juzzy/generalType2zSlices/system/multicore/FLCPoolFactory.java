/*
 * FLCPoolFactory.java
 *
 * Created on 15 July 2008, 10:58
 *
 * Copyright 2008 Christian Wagner All Rights Reserved.
 */

package generalType2zSlices.system.multicore;

import generic.Output;
import generic.Tuple;
import intervalType2.system.IT2_Rulebase;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Christian Wagner
 */
public class FLCPoolFactory {
 
    private int numberOfThreads;
    private ThreadPool pool;
    private FLCPlant[] plants;
    private TreeMap<Output, Object[]> rawResults;
    private double[] zLevels;
    private long timer;
    private double result, weight = 0.0;   
    private int defaultTypeReduction = 1;   //default set to 1 -centroid
    private IT2_Rulebase[] rulebases;
    
    /** Creates a new instance of FLCPoolFactory */
    public FLCPoolFactory(IT2_Rulebase[] rulebases)
    {
        this.rulebases = rulebases;
        numberOfThreads = rulebases.length;
        plants = new FLCPlant[numberOfThreads];
        pool = new ThreadPool(numberOfThreads);
        zLevels = new double[rulebases.length];

        
        for(int i=0;i<numberOfThreads;i++)
        {
            zLevels[i] = (i+1.0) / rulebases.length;
            weight += zLevels[i];    //for later weighted average calculation - suffices to calculate weight once...
        }            

        this.rawResults = new TreeMap();
        Iterator<Output> it = rulebases[0].getOutputIterator();
        while(it.hasNext())
        {
            rawResults.put(it.next(), new Object[]{new Tuple[numberOfThreads], new double[numberOfThreads]});
        }     
        for(int i=0;i<numberOfThreads;i++)
            plants[i] = new FLCPlant(rulebases[i], rawResults, i,defaultTypeReduction);    
    }
    
    public TreeMap<Output, Double> runFactory(int typeReductionType)
    {
        TreeMap<Output, Double> returnValue = new TreeMap();
        
         Iterator<Output> it = rulebases[0].getOutputIterator();
        Object[] objs;
        while(it.hasNext())
        {
            objs = rawResults.get(it.next());
            objs[0] = new Tuple[numberOfThreads];
            objs[1] = new double[numberOfThreads];
        }     
        
        if(typeReductionType!=defaultTypeReduction)
        {
            for(FLCPlant p : plants)
                p.setTypeReductionType(typeReductionType);
            this.defaultTypeReduction = typeReductionType;
        }
        
        for(int i=0;i<numberOfThreads;i++)
        {            
            try {
                pool.execute(plants[i]);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        
        while(pool.getNumberOfBusyThreads()>0)                
        {
            //System.out.println("waiting : "+ pool.getNumberOfBusyThreads());
            Thread.currentThread().yield();
        }
        

        
//        Iterator<Output> it;
        //set all to 0
        it = rulebases[0].getOutputIterator();
            while(it.hasNext())
                returnValue.put(it.next(), 0.0);

        Output o;
        for(int i=0;i<numberOfThreads;i++)
        {
            //result += results[i]*zLevels[i];
            it = rulebases[0].getOutputIterator();
            while(it.hasNext())
            {
                o = it.next();
                //by convention, if the output is null, i.e. no rule has fired, return 0 (i.e. do not add anything...)
                if(((Tuple[])rawResults.get(o)[0])[i]!= null)
                    returnValue.put(o, returnValue.get(o) + ((Tuple[])rawResults.get(o)[0])[i].getAverage()*zLevels[i]);
            }
        }
        
        it = rulebases[0].getOutputIterator();
        while(it.hasNext())
        {
            o = it.next();
            returnValue.put(o, (returnValue.get(o) / weight));
        }        
        //result /= weight;
        //System.out.println("The overall result is: "+result+" and was computed in "+(System.currentTimeMillis()-timer)+" ms");
//        pool.stopRequestAllWorkers();
        return returnValue;
    }

    /**
     * Returns the output of the FLS after type-reduction, i.e. the centroid.
     * @param typeReductionType
     * @return A TreeMap where Output is used as key and the value is an Object[]
     *  where Object[0] is a Tuple[] (the centroids, one per zLevel) and Object[1] is a Double holding
     * the associated yValues for the centroids. If not rule fired for the given input(s),
     * then null is returned as an Object[].
     */        
    public TreeMap<Output, Object[]> runFactoryGetCentroid(int typeReductionType)
    {   
        Iterator<Output> it = rulebases[0].getOutputIterator();
        Object[] objs;
        while(it.hasNext())
        {
            objs = rawResults.get(it.next());
            objs[0] = new Tuple[numberOfThreads];
            objs[1] = new double[numberOfThreads];
        }            

        
        if(typeReductionType!=defaultTypeReduction)
        {
            for(FLCPlant p : plants)
                p.setTypeReductionType(typeReductionType);
            this.defaultTypeReduction = typeReductionType;
        }        
        
        for(int i=0;i<numberOfThreads;i++)
        {            
            try {
                pool.execute(plants[i]);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        while(pool.getNumberOfBusyThreads()>0)                
        {
            //System.out.println("waiting : "+ pool.getNumberOfBusyThreads());
            Thread.currentThread().yield();
        }        

        //Iterator<Output> it;
        Output o;
        for(int i=0;i<numberOfThreads;i++)
        {
            //result += results[i]*zLevels[i];
            it = rulebases[0].getOutputIterator();
            while(it.hasNext())
            {
                o = it.next();
                synchronized(o)
                {
                //returnValue.put(o, returnValue.get(o) + results.get(o)[i]*zLevels[i]);
                 ((double[])rawResults.get(o)[1])[i] = zLevels[i];   //set the zLevels
                }
            }
        }
        return rawResults;
    }    
    
//    public TreeMap<Output, Double> runFactory()
//    {
//        TreeMap<Output, Double> returnValue = new TreeMap();
//        //timer = System.currentTimeMillis();
//        
//        for(int i=0;i<numberOfThreads;i++)
//        {            
//            try {
//                pool.execute(plants[i]);
//            } catch (InterruptedException ex) {
//                ex.printStackTrace();
//            }
//        }
//        
//       Iterator<Output> it;
//        //result = 0.0;
//        //set all to 0
//        it = rulebases[0].getOutputIterator();
//            while(it.hasNext())
//                returnValue.put(it.next(), 0.0);
//
//        Output o;
//        for(int i=0;i<numberOfThreads;i++)
//        {
//            //result += results[i]*zLevels[i];
//            it = rulebases[0].getOutputIterator();
//            while(it.hasNext())
//            {
//                o = it.next();
//                returnValue.put(o, returnValue.get(o) + results.get(o)[i]*zLevels[i]);
//            }
//        }
//        
//        it = rulebases[0].getOutputIterator();
//        while(it.hasNext())
//        {
//            o = it.next();
//            returnValue.put(o, (returnValue.get(o) / weight));
//        }        
//        //result /= weight;
//        //System.out.println("The overall result is: "+result+" and was computed in "+(System.currentTimeMillis()-timer)+" ms");
//        return returnValue;
//    }    
}

