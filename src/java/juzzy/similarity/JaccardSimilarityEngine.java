/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package similarity;

import generalType2zSlices.sets.GenT2zMF_Interface;
import generic.Tuple;
import intervalType2.sets.IntervalT2MF_Interface;
import type1.sets.T1MF_Interface;

/**
 * Implementation of Jaccard Similarity for Type-1, Type-2 and zGT2 Sets
 * @author Christian Wagner
 */
public class JaccardSimilarityEngine 
{
    private double numerator, denominator;
    
    public double getSimilarity(T1MF_Interface setA, T1MF_Interface setB, int numberOfDiscretisations)
    {
        double[] discValues = getDiscretisationValues(setA.getSupport(), setB.getSupport(), numberOfDiscretisations);
        numerator = 0.0; denominator = 0.0;
        for(int i=0;i<discValues.length;i++)
        {
            numerator += Math.min(setA.getFS(discValues[i]), setB.getFS(discValues[i]));
            denominator += Math.max(setA.getFS(discValues[i]), setB.getFS(discValues[i]));
        }
        return numerator/denominator;
    }
    
    public double getSimilarity(IntervalT2MF_Interface setA, IntervalT2MF_Interface setB,int numberOfDiscretisations)
    {
        double[] discValues = getDiscretisationValues(setA.getSupport(), setB.getSupport(), numberOfDiscretisations);
        numerator = 0.0; denominator = 0.0;
        for(int i=0;i<discValues.length;i++)
        {
            numerator += Math.min(setA.getUMF().getFS(discValues[i]), setB.getUMF().getFS(discValues[i])) + 
                    Math.min(setA.getLMF().getFS(discValues[i]), setB.getLMF().getFS(discValues[i]));
            denominator += Math.max(setA.getUMF().getFS(discValues[i]), setB.getUMF().getFS(discValues[i])) + 
                    Math.max(setA.getLMF().getFS(discValues[i]), setB.getLMF().getFS(discValues[i]));
        }
        return numerator/denominator;
    }
    
    public double getSimilarity(GenT2zMF_Interface setA, GenT2zMF_Interface setB, int numberOfDiscretisations)
    {
        double[] discValues = getDiscretisationValues(setA.getSupport(), setB.getSupport(), numberOfDiscretisations);
        numerator = 0.0; denominator = 0.0;
        double numeratorArray[] = new double[setA.getNumberOfSlices()];
        double denominatorArray[] = new double[setA.getNumberOfSlices()];
        
        for(int i=0;i<discValues.length;i++)
        {
            for(int z=0; z<setA.getNumberOfSlices(); z++)
            {
                numeratorArray[z] += Math.min(setA.getZSlice(z).getUMF().getFS(discValues[i]), setB.getZSlice(z).getUMF().getFS(discValues[i])) + 
                        Math.min(setA.getZSlice(z).getLMF().getFS(discValues[i]), setB.getZSlice(z).getLMF().getFS(discValues[i]));
                denominatorArray[z] += Math.max(setA.getZSlice(z).getUMF().getFS(discValues[i]), setB.getZSlice(z).getUMF().getFS(discValues[i])) + 
                        Math.max(setA.getZSlice(z).getLMF().getFS(discValues[i]), setB.getZSlice(z).getLMF().getFS(discValues[i]));                
            }
        }
        numerator = 0.0;
        denominator = 0.0;
        for(int z=0; z<setA.getNumberOfSlices(); z++)
        {
            numerator += numeratorArray[z] * setA.getZValue(z);
            denominator += denominatorArray[z] * setA.getZValue(z);
        }
        return numerator/denominator;
    }    
    
    private double[] getDiscretisationValues(Tuple domainSetA, Tuple domainSetB, int numberOfDiscretisations)
    {
        Tuple domain = new Tuple(Math.min(domainSetA.getLeft(), domainSetB.getLeft()), Math.max(domainSetA.getRight(), domainSetB.getRight()));
        double discStep = domain.getSize()/(numberOfDiscretisations-1);
        double[] discValues = new double[numberOfDiscretisations];
        for(int i=0;i<numberOfDiscretisations;i++)
        {
            discValues[i] = domain.getLeft() + i * discStep;
        }
        return discValues;
    }
}
