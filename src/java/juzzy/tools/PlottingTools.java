
package tools;

import generalType2zSlices.sets.GenT2zMF_Interface;
import generic.Tuple;
import java.util.Arrays;

/**
 *
 * @author 
 */
public class PlottingTools 
{
    /**
     * Returns point cloud data for the given sets for the specified ranges and levels of discretisation.
     * @param sets The zSlice based general type-2 fuzzy sets.
     * @param xD The interval on x over which to discretise.
     * @param xDisc The number of discretisations for the interval on x.
     * @param yD The interval on y over which to discretise.
     * @param yDisc The number of discretisations for the interval on y.
     * @return An object array with three elements, where object[0] is a double array with the discretisation of the xAxis,
     *  object[1] is the same for the yAxis and result[2] is a 2-dimensional double array (i.e.: object[xDisc][yDisc]) which holds the z values.
     */
    static public Object[] getPointCloud(GenT2zMF_Interface[] sets, Tuple xD, int xDisc, Tuple yD, int yDisc) 
    {
        Object[] result = new Object[3];
        result[0] = new double[xDisc]; // x[]
        result[1] = new double[yDisc]; // y[]
        result[2] = new double[xDisc][yDisc]; // z[][]
        double xStep = (xD.getRight() - xD.getLeft()) / (xDisc-1.0);
        double yStep = (yD.getRight() - yD.getLeft()) / (yDisc-1.0);
        
        // fill x[]
        double xDStart = xD.getLeft();
        ((double[]) result[0])[0] = xDStart;
        for (int i = 1; i < xDisc-1; i++) {
                ((double[]) result[0])[i] = xDStart + i*xStep;
        }
        ((double[]) result[0])[xDisc-1] = xD.getRight();
        
        // fill y[]
        double yDStart = yD.getLeft();
        ((double[]) result[1])[0] = yDStart;
        for (int j = 1; j < yDisc-1; j++) {
                ((double[]) result[1])[j] = yDStart + j*yStep;
        }
        ((double[]) result[1])[yDisc-1] = yD.getRight();
        
        // first, fill z[][] with zeros
        for (int i = 0; i < xDisc; i++) 
        {
            Arrays.fill(((double[][]) result[2])[i],0.0);
        }
        // then, go through every slice of the set and update z[][]
        int nZ = sets[0].getNumberOfSlices(); // every set should have the same number of slices
        double zValue;
        Tuple temp;
        for (int s = 0; s < sets.length; s++) 
        {
            for (int k = 0; k < nZ; k++) 
            {
                zValue = sets[s].getZValue(k);
                for (int i = 0; i < xDisc; i++) 
                {
                    temp = sets[s].getZSlice(k).getFS(((double[]) result[0])[i]);
                    for (int j = 0; j < yDisc; j++) 
                    {
                        if (((double[]) result[1])[j] >= temp.getLeft() && ((double[]) result[1])[j] <= temp.getRight()) 
                        {
                            ((double[][]) result[2])[i][j] = Math.max(zValue, ((double[][]) result[2])[i][j]);
                        }
                    }
                }
            }
        }
        return result;
    }
    
}
