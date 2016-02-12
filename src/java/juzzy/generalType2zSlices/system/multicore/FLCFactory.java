/*
 * FLCFactory.java
 *
 * Created on 09 July 2008, 15:03
 *
 * Copyright 2008 Christian Wagner All Rights Reserved.
 */

package generalType2zSlices.system.multicore;

import generalType2zSlices.system.GenT2z_Rulebase;
import generic.Output;
import generic.Tuple;
import intervalType2.system.IT2_Rulebase;
import java.util.Iterator;
import java.util.TreeMap;


/**
 * FLCFactory is the top level structure for the execution of multi zSlice FLCs. An FLCFactory controls a series of FLCPlants each of
 * which is responsible for the processing of a single zSlice.
 * @author Christian Wagner
 */
public class FLCFactory {

    private int numberOfThreads;
    private Thread[] threads;
    private FLCPlant[] plants;
    private TreeMap<Output, Object[]> rawResults;
    private double[] zLevels;
    private long timer;
    private double result, weight = 0.0;
    private IT2_Rulebase[] rulebases;
    
    private final boolean DEBUG = false;
    private final boolean SHOWCONTEXT = false;
    
    /** Creates a new instance of FLCFactory 
     *@param 
     */
    public FLCFactory(IT2_Rulebase[] rulebases)
    {
        this.rulebases = rulebases;
        this.rawResults = new TreeMap();
        int numberOfCores = Runtime.getRuntime().availableProcessors();
        if(SHOWCONTEXT)System.out.println("The system has identified "+numberOfCores+" available processing cores.");        
        
//        //if you have a multiple of cores in respect to the number of slices
//        if(numberOfCores > rulebases.length && numberOfCores*1.0%rulebases.length==0.0)
//        {
//            
//        }
        numberOfThreads = rulebases.length;
        
        threads = new Thread[numberOfThreads];
        plants = new FLCPlant[numberOfThreads];
        Iterator<Output> it = rulebases[0].getOutputIterator();
        while(it.hasNext())
        {
            rawResults.put(it.next(), new Object[]{new Tuple[numberOfThreads], new double[numberOfThreads]});
        }        
        zLevels = new double[numberOfThreads];
        
        if(DEBUG)System.out.println("Creating 1 thread per zSlice: total of "+numberOfThreads+" threads.");
        for(int i=0;i<numberOfThreads;i++)
        {            
            zLevels[i] = (i+1.0) / rulebases.length;
            weight += zLevels[i];    //for later weighted average calculation - suffices to calculate weight once...
        }
    }
    
    public TreeMap<Output, Double> runFactory(int typeReductionType)
    {
        TreeMap<Output, Double> returnValue = new TreeMap();
        //timer = System.currentTimeMillis();
        
        for(int i=0;i<numberOfThreads;i++)
        {            
            plants[i] = new FLCPlant(rulebases[i], rawResults, i, typeReductionType); //this could be moved to constructor for efficiency / less flexibility
            threads[i] = new Thread(plants[i], "Thread_"+i);

            if(DEBUG)System.out.println("Running Factory (thread) number "+i);
            threads[i].start();

        }
        
        for(int i=0;i<numberOfThreads;i++)
        {
            try {
                threads[i].join();
            } catch (InterruptedException ex) 
            {
                if(DEBUG)System.out.println("Thread "+i+" finished before it was joined with the main thread.");
                ex.printStackTrace();
            } 
        }
        
        Iterator<Output> it;
        //result = 0.0;
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
        TreeMap<Output, Object[]> returnValue = new TreeMap();
        //timer = System.currentTimeMillis();
        
        for(int i=0;i<numberOfThreads;i++)
        {            
            plants[i] = new FLCPlant(rulebases[i], rawResults, i, typeReductionType);
            threads[i] = new Thread(plants[i], "Thread_"+i);

            if(DEBUG)System.out.println("Running Factory (thread) number "+i);
            threads[i].start();

        }
        
        for(int i=0;i<numberOfThreads;i++)
        {
            try {
                threads[i].join();
            } catch (InterruptedException ex) 
            {
                if(DEBUG)System.out.println("Thread "+i+" finished before it was joined with the main thread.");
                ex.printStackTrace();
            } 
        }
        
        Iterator<Output> it;

        Output o;
        for(int i=0;i<numberOfThreads;i++)
        {
            //result += results[i]*zLevels[i];
            it = rulebases[0].getOutputIterator();
            while(it.hasNext())
            {
                o = it.next();
                //returnValue.put(o, returnValue.get(o) + results.get(o)[i]*zLevels[i]);
                ((double[])rawResults.get(o)[1])[i] = zLevels[i];   //set the zLevels
            }
        }
        return rawResults;
    }    
    
}
