/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;


import generalType2zSlices.sets.GenT2zMF_Interface;
import generalType2zSlices.sets.GenT2zMF_Trapezoidal;
import generalType2zSlices.sets.GenT2zMF_Triangular;
import generic.Tuple;
import intervalType2.sets.IntervalT2MF_Interface;
import intervalType2.sets.IntervalT2MF_Trapezoidal;
import intervalType2.sets.IntervalT2MF_Triangular;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import org.math.plot.Plot2DPanel;
import org.math.plot.Plot3DPanel;
import type1.sets.T1MF_Interface;
import type1.sets.T1MF_Trapezoidal;


/**
 * Implementation based on JMathPlotter: http://code.google.com/p/jmathplot
 * First use the plotMF function to specify the content to be plotted.
 * Then use show() to actually plot the content and show it in a frame.
 * @author chwagn
 */
public class JMathPlotter {
    Container plot = null;
    String title = "";
    private Font legendFont, axisFont, axisLightFont;   //axisLightFont refers to subdivisions on axis
    
    public JMathPlotter()
    {
        legendFont = new Font("SansSerif",1,17);
        axisFont = new Font("SansSerif",1,17);
        axisLightFont = new Font("SansSerif",1,15);
    }
    
    public JMathPlotter(int legendFontSize, int axisFontSize, int axisLightFontSize)
    {
        legendFont = new Font("SansSerif",1,legendFontSize);
        axisFont = new Font("SansSerif",1,axisFontSize);
        axisLightFont = new Font("SansSerif",1,axisLightFontSize);
    }
    
    /**
     * 2D plotting for T1 sets based on JMathPlotter
     * @param plotName
     * @param set
     * @param xDisc
     * @param yAxisRange
     * @param addExtraEndpoints If true, two extra points with coordinates (leftEndpoint, 0) and (rightEndpoint, 0) are added to the line plot.
     * This ensures that vertical edges are plotted correctly for example for interval type-1 sets.
     */
    public void plotMF(String plotName, T1MF_Interface set, int xDisc, Tuple xAxisRange, Tuple yAxisRange, boolean addExtraEndpoints)
    {
        double[] x = discretize(set.getSupport(),xDisc);
        double[] y = new double[xDisc];
        for(int i=0;i<xDisc;i++)
            y[i] = set.getFS(x[i]);
        
        if(addExtraEndpoints)
        {
            double[] x2 = new double[x.length+2];
            double[] y2 = new double[y.length+2];
            x2[0] = set.getSupport().getLeft();
            x2[x2.length-1] = set.getSupport().getRight();
            y2[0] = 0.0;
            y2[y2.length-1] = 0.0;
            for(int i=0;i<x.length;i++)
            {
                x2[i+1] = x[i];
                y2[i+1] = y[i];
            }
            x=x2;
            y=y2;
        }
        
        // create your PlotPanel if it is not yet instantiated (to allow for multi-plots)
        if(plot==null)
        {
            plot = new Plot2DPanel();
            ((Plot2DPanel)plot).setFont(legendFont);
            ((Plot2DPanel)plot).getAxis(0).setLabelFont(axisFont);
            ((Plot2DPanel)plot).getAxis(0).setLightLabelFont(axisLightFont);
            ((Plot2DPanel)plot).getAxis(1).setLabelFont(axisFont);
            ((Plot2DPanel)plot).getAxis(1).setLightLabelFont(axisLightFont);
            // define the legend position
            ((Plot2DPanel)plot).addLegend("SOUTH");            
        }

        // add a line plot to the PlotPanel
        ((Plot2DPanel)plot).addLinePlot(plotName, x, y);
        ((Plot2DPanel)plot).setFixedBounds(1,yAxisRange.getLeft(), yAxisRange.getRight());
        ((Plot2DPanel)plot).setFixedBounds(0,xAxisRange.getLeft(), xAxisRange.getRight());
        
        title = "Type-1 Fuzzy set plot of set: "+set.getName();
        
        
             
    }
    
    /**
     * This function enables to plot a graph with input values in xaxis and the corresponding outputs in y axis
     * @param plotName
     * @param results
     * @param xAxisRange
     * @param yAxisRange
     */
    public void plotInputOutput(String plotName, double[] results, Tuple xAxisRange, Tuple yAxisRange)
    {
        double[] x = new double[results.length];
        for(int i=0;i<results.length;i++)
        	x[i] = i;
    
        
        // create your PlotPanel if it is not yet instantiated (to allow for multi-plots)
        if(plot==null)
        {
            plot = new Plot2DPanel();
            ((Plot2DPanel)plot).setFont(legendFont);
            ((Plot2DPanel)plot).getAxis(0).setLabelFont(axisFont);
            ((Plot2DPanel)plot).getAxis(0).setLightLabelFont(axisLightFont);
            ((Plot2DPanel)plot).getAxis(1).setLabelFont(axisFont);
            ((Plot2DPanel)plot).getAxis(1).setLightLabelFont(axisLightFont);
            // define the legend position
            ((Plot2DPanel)plot).addLegend("SOUTH");            
        }

        // add a line plot to the PlotPanel
        ((Plot2DPanel)plot).addLinePlot(plotName, x, results);
        ((Plot2DPanel)plot).setFixedBounds(1,yAxisRange.getLeft(), yAxisRange.getRight());
        ((Plot2DPanel)plot).setFixedBounds(0,xAxisRange.getLeft(), xAxisRange.getRight());
        
        title = "Output function of Input : " + plotName;
        
        
             
    }
    public void toGraphicFile( File f) throws IOException {
        Plot2DPanel p = ((Plot2DPanel)plot);
        //p.toGraphicFile(f);
        // otherwise toolbar appears
        p.plotToolBar.setVisible(false);

        Image image = p.createImage(p.getWidth(), p.getHeight());
        //Image image = p.createImage(2000, p.getHeight());
        //System.out.println(p.getWidth()+"  "+p.getHeight());
        p.paint(image.getGraphics());
        image = new ImageIcon(image).getImage();

        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
        //BufferedImage bufferedImage = new BufferedImage(p.getWidth(), p.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics g = bufferedImage.createGraphics();
        g.drawImage(image, 0, 0, Color.WHITE, null);
        g.dispose();

        // make it reappear
        p.plotToolBar.setVisible(true);

        try {
                ImageIO.write((RenderedImage) bufferedImage, "PNG", f);
        } catch (IllegalArgumentException ex) {
        }
    }
    
    public void plotMF(String plotName, IntervalT2MF_Interface set, int xDisc, JFrame frame, boolean addExtraEndpoints)
    {
        double[] x = discretize(set.getSupport(),xDisc);
        double[] y1 = new double[xDisc];
        double[] y2 = new double[xDisc];
        Tuple temp;
        
        for(int i=0;i<xDisc;i++)
        {
            temp = set.getFS(x[i]);
            y1[i] = temp.getRight(); //upper
            y2[i] = temp.getLeft(); //lower
        }

        if(addExtraEndpoints)
        {
            double[] x2 = new double[x.length+2];
            double[] y1b = new double[y1.length+2];
            double[] y2b = new double[y2.length+2];
            x2[0] = set.getSupport().getLeft();
            x2[x2.length-1] = set.getSupport().getRight();
            y1b[0] = 0.0;
            y1b[y1.length-1] = 0.0;
            y2b[0] = 0.0;
            y2b[y2.length-1] = 0.0;            
            for(int i=0;i<x.length;i++)
            {
                x2[i+1] = x[i];
                y1b[i+1] = y1[i];
                y2b[i+1] = y2[i];
            }
            x=x2;
            y1=y1b;
            y2=y2b;
        }        
        
        // create your PlotPanel if it is not yet instantiated (to allow for multi-plots)
        if(plot==null)
        {
            plot = new Plot2DPanel();
            ((Plot2DPanel)plot).setFont(legendFont);
            // define the legend position
            ((Plot2DPanel)plot).addLegend("SOUTH");            
        }

        // add set to panel - maintain color for upper and lower
        int upper = ((Plot2DPanel)plot).addLinePlot(set.getName()+"_upper", x, y1);
        int lower = ((Plot2DPanel)plot).addLinePlot(set.getName()+"_lower",((Plot2DPanel)plot).getPlot(upper).getColor(), x, y2);
        
        //set title for later (for use in show() method).
        title = "Interval-Type-2 Fuzzy Set plot of set: "+set.getName();
    }
    
    /**
     * Provides a basic line-style vizualisation of the zSlices based general Type-2
     * Fuzzy Sets.
     * @param plotName
     * @param set
     * @param xDisc Discretization level of the xAxis.
     */
    public void plotMFasLines(String plotName, GenT2zMF_Interface set, int xDisc)
    {
        double[] x = discretize(set.getSupport(),xDisc);
        double[][] y1 = new double[set.getNumberOfSlices()][xDisc];
        double[][] y2 = new double[set.getNumberOfSlices()][xDisc];
        double[][] z1 = new double[set.getNumberOfSlices()][xDisc];
        double[][] z2 = new double[set.getNumberOfSlices()][xDisc];
        
        Tuple temp;
        
        for(int zLevel = 0 ; zLevel<set.getNumberOfSlices();zLevel++)
        {
            for(int i=0;i<xDisc;i++)
            {
                temp = set.getZSlice(zLevel).getFS(x[i]);
                y1[zLevel][i] = temp.getRight(); //upper
                y2[zLevel][i] = temp.getLeft(); //lower
                if(zLevel==0)
                    z1[zLevel][i] = 0.0;
                else
                    z1[zLevel][i] = set.getZValue(zLevel-1);
                z2[zLevel][i] = set.getZValue(zLevel);
            }
        }        

        // create your PlotPanel if it is not yet instantiated (to allow for multi-plots)
        if(plot==null)
        {
            plot = new Plot3DPanel();
            ((Plot3DPanel)plot).setFont(legendFont);
            // define the legend position
            ((Plot3DPanel)plot).addLegend("SOUTH");            
        }      
        
        
        int index;
        //plot all zLevels separately
        for(int zLevel =0; zLevel<set.getNumberOfSlices();zLevel++)
        {        
            ((Plot3DPanel)plot).addLinePlot(set.getName()+"_upper", ((Plot3DPanel)plot).COLORLIST[zLevel%8], x, y1[zLevel], z1[zLevel]);
            ((Plot3DPanel)plot).addLinePlot(set.getName()+"_lower", ((Plot3DPanel)plot).COLORLIST[zLevel%8], x, y2[zLevel], z1[zLevel]);
            ((Plot3DPanel)plot).addLinePlot(set.getName()+"_upper", ((Plot3DPanel)plot).COLORLIST[zLevel%8], x, y1[zLevel], z2[zLevel]);
            ((Plot3DPanel)plot).addLinePlot(set.getName()+"_lower", ((Plot3DPanel)plot).COLORLIST[zLevel%8], x, y2[zLevel], z2[zLevel]);
        }
        //System.out.println(plot3D.getComponentOrientation());
        ((Plot3DPanel)plot).plotLegend.removeAll();
        
        title = "zSlices based general type-2 type-2 fuzzy set plot of set: "+set.getName();    
    }
    
    /**
     * Provides a surface-style visualisation of the sets. For some Membership 
     * Functions, purpose-built plotting has been included, e.g. GenT2zMF_Triangular.
     * @param plotName
     * @param set
     * @param xAxisRange the range of the x axis to be sampled
     * @param xDisc The level of discretization. This parameter may be disregarded
     * for sets which have purpose-built plotting has been included.
     */
    public void plotMFasSurface(String plotName, GenT2zMF_Interface set, Tuple xAxisRange, int xDisc, boolean addExtraEndpoints)
    {
        Tuple temp;
        
        
        
        // create your PlotPanel if it is not yet instantiated (to allow for multi-plots)
        if(plot==null)
        {
            plot = new Plot3DPanel();
            ((Plot3DPanel)plot).setFont(legendFont);
            // define the legend position
            ((Plot3DPanel)plot).addLegend("SOUTH");            
        }          
        
        if(set instanceof GenT2zMF_Triangular)
        {
            //first upper, then lower
            for(int zLevel=0;zLevel<set.getNumberOfSlices();zLevel++)
            {
                double[] x_upper = new double[] {((IntervalT2MF_Triangular)set.getZSlice(zLevel)).getUMF().getStart(),
                                                ((IntervalT2MF_Triangular)set.getZSlice(zLevel)).getUMF().getPeak(),
                                                ((IntervalT2MF_Triangular)set.getZSlice(zLevel)).getUMF().getEnd()};
                double[] z_upper = null;//discretize(new Tuple(0.0,1.0),yDisc);;
                double[][] y_upper = new double[2][xDisc];

                if(zLevel==0)
                    z_upper = new double[]{0.0,set.getZValue(zLevel)};
                else
                    z_upper = new double[]{set.getZValue(zLevel-1),set.getZValue(zLevel)};

                
                for(int xD = 0 ; xD<3;xD++)
                {              
                    y_upper[0][xD] = set.getZSlice(zLevel).getFS(x_upper[xD]).getRight();
                    y_upper[1][xD] = y_upper[0][xD];
                } 

                double[] x_Lower = new double[] {((IntervalT2MF_Triangular)set.getZSlice(zLevel)).getLMF().getStart(),
                                                ((IntervalT2MF_Triangular)set.getZSlice(zLevel)).getLMF().getPeak(),
                                                ((IntervalT2MF_Triangular)set.getZSlice(zLevel)).getLMF().getEnd()};
                double[] z_Lower = null;//discretize(new Tuple(0.0,1.0),yDisc);;
                double[][] y_Lower = new double[2][xDisc];

                if(zLevel==0)
                    z_Lower = new double[]{0.0,set.getZValue(zLevel)};
                else
                    z_Lower = new double[]{set.getZValue(zLevel-1),set.getZValue(zLevel)};

                for(int xD = 0 ; xD<3;xD++)
                {               
                    y_Lower[0][xD] = set.getZSlice(zLevel).getFS(x_Lower[xD]).getLeft();
                    y_Lower[1][xD] = y_Lower[0][xD];
                }             
                ((Plot3DPanel)plot).addGridPlot("zSlice",((Plot3DPanel)plot).COLORLIST[zLevel%8], x_upper, z_upper, y_upper);
                ((Plot3DPanel)plot).addGridPlot("zSlice",((Plot3DPanel)plot).COLORLIST[zLevel%8], x_Lower, z_Lower, y_Lower);

            }            
        }
        
        else if(set instanceof GenT2zMF_Trapezoidal)
        {
            //first upper, then lower
            for(int zLevel=0;zLevel<set.getNumberOfSlices();zLevel++)
            {
                double[] x_upper = new double[] {((T1MF_Trapezoidal)((IntervalT2MF_Trapezoidal)set.getZSlice(zLevel)).getUMF()).getA(),
                                                ((T1MF_Trapezoidal)((IntervalT2MF_Trapezoidal)set.getZSlice(zLevel)).getUMF()).getB(),
                                                ((T1MF_Trapezoidal)((IntervalT2MF_Trapezoidal)set.getZSlice(zLevel)).getUMF()).getC(),
                                                ((T1MF_Trapezoidal)((IntervalT2MF_Trapezoidal)set.getZSlice(zLevel)).getUMF()).getD()};
                double[] z_upper = null;//discretize(new Tuple(0.0,1.0),yDisc);;
                double[][] y_upper = new double[2][xDisc];

                if(zLevel==0)
                    z_upper = new double[]{0.0,set.getZValue(zLevel)};
                else
                    z_upper = new double[]{set.getZValue(zLevel-1),set.getZValue(zLevel)};

                
                for(int xD = 0 ; xD<4;xD++)
                {              
                    y_upper[0][xD] = set.getZSlice(zLevel).getFS(x_upper[xD]).getRight();
                    y_upper[1][xD] = y_upper[0][xD];
                } 

                double[] x_Lower = new double[] {((T1MF_Trapezoidal)((IntervalT2MF_Trapezoidal)set.getZSlice(zLevel)).getLMF()).getA(),
                                                ((T1MF_Trapezoidal)((IntervalT2MF_Trapezoidal)set.getZSlice(zLevel)).getLMF()).getB(),
                                                ((T1MF_Trapezoidal)((IntervalT2MF_Trapezoidal)set.getZSlice(zLevel)).getLMF()).getC(),
                                                ((T1MF_Trapezoidal)((IntervalT2MF_Trapezoidal)set.getZSlice(zLevel)).getLMF()).getD()};
                double[] z_Lower = null;//discretize(new Tuple(0.0,1.0),yDisc);;
                double[][] y_Lower = new double[2][xDisc];

                if(zLevel==0)
                    z_Lower = new double[]{0.0,set.getZValue(zLevel)};
                else
                    z_Lower = new double[]{set.getZValue(zLevel-1),set.getZValue(zLevel)};

                for(int xD = 0 ; xD<4;xD++)
                {               
                    y_Lower[0][xD] = set.getZSlice(zLevel).getFS(x_Lower[xD]).getLeft();
                    y_Lower[1][xD] = y_Lower[0][xD];
                }             
                ((Plot3DPanel)plot).addGridPlot("zSlice",((Plot3DPanel)plot).COLORLIST[zLevel%8], x_upper, z_upper, y_upper);
                ((Plot3DPanel)plot).addGridPlot("zSlice",((Plot3DPanel)plot).COLORLIST[zLevel%8], x_Lower, z_Lower, y_Lower);

            }            
        }        
        //else if (set instanceof ...)
        
        else
            genericplot:
            {
                //double xStep = set.getSupport().getSize()/(xDisc-1.0);
                double xStep = xAxisRange.getSize()/(xDisc-1.0);
                
                //first upper, then lower
                for(int zLevel=0;zLevel<set.getNumberOfSlices();zLevel++)
                {
                    //double[] x_upper = discretize(set.getSupport(),xDisc);
                    double[] x_upper = discretize(xAxisRange,xDisc);
                    double[] z_upper = null;//discretize(new Tuple(0.0,1.0),yDisc);;
                    double[][] y_upper = new double[2][xDisc];

                    if(zLevel==0)
                        z_upper = new double[]{0.0,set.getZValue(zLevel)};
                    else
                        z_upper = new double[]{set.getZValue(zLevel-1),set.getZValue(zLevel)};
                        //z_upper = new double[]{0.0,set.getZValue(zLevel)};

                    for(int xD = 0 ; xD<xDisc;xD++)
                    {
                        //x_upper[xD] = xStep * xD;                
                        y_upper[0][xD] = set.getZSlice(zLevel).getFS(x_upper[xD]).getRight();
                        y_upper[1][xD] = y_upper[0][xD];
                    } 

                    //double[] x_Lower = discretize(set.getSupport(),xDisc);
                    double[] x_Lower = discretize(xAxisRange,xDisc);
                    double[] z_Lower = null;//discretize(new Tuple(0.0,1.0),yDisc);;
                    double[][] y_Lower = new double[2][xDisc];

                    if(zLevel==0)
                        z_Lower = new double[]{0.0,set.getZValue(zLevel)};
                    else
                        z_Lower = new double[]{set.getZValue(zLevel-1),set.getZValue(zLevel)};

                    for(int xD = 0 ; xD<xDisc;xD++)
                    {
                        //x_Lower[xD] = xStep * xD;                
                        y_Lower[0][xD] = set.getZSlice(zLevel).getFS(x_Lower[xD]).getLeft();
                        y_Lower[1][xD] = y_Lower[0][xD];
                    }  
                    
        if(addExtraEndpoints)
        {
            double[] x_upper2 = new double[x_upper.length+2];
            double[][] y_upper2 = new double[2][y_upper[0].length+2];
//            double[] z_upper2 = new double[z_upper.length+2];
            double[] x_Lower2 = new double[x_Lower.length+2];
            double[][] y_Lower2 = new double[2][y_Lower[0].length+2];
            
//            double[] z_Lower2 = new double[z_Lower.length+2];            
            x_upper2[0] = set.getSupport().getLeft();
            x_upper2[x_upper2.length-1] = set.getSupport().getRight();
            x_Lower2[0]=x_upper2[0];
            x_Lower2[x_Lower2.length-1] = x_upper2[x_upper2.length-1];
            y_upper2[0][0] = 0.0;
            y_upper2[0][y_upper2[0].length-1] = 0.0;
            y_Lower2[0][0]=0.0;
            y_Lower2[0][y_Lower2[0].length-1] = 0.0 ;  
            y_upper2[1][0] = 0.0;
            y_upper2[1][y_upper2[1].length-1] = 0.0;
            y_Lower2[1][0]=0.0;
            y_Lower2[1][y_Lower2[1].length-1] = 0.0 ;              
//            z_upper2[0] = 0.0;
//            z_upper2[z_upper2.length-1] = 0.0;
//            z_Lower2[0]=0.0;
//            z_Lower2[z_Lower.length-1] = 0.0 ;              
            
            for(int i=0;i<x_upper.length;i++)
            {
                x_upper2[i+1] = x_upper[i];
                x_Lower2[i+1] = x_Lower[i];
                y_upper2[0][i+1] = y_upper[0][i];
                y_Lower2[0][i+1] = y_Lower[0][i];                
                y_upper2[1][i+1] = y_upper[1][i];
                y_Lower2[1][i+1] = y_Lower[1][i];
            }
            x_upper=x_upper2; x_Lower = x_Lower2;
            y_upper=y_upper2; y_Lower = y_Lower2;
            
        }                          
                    
                    
                    
                    int index = ((Plot3DPanel)plot).addGridPlot("zSlice", x_upper, z_upper, y_upper); 
                    ((Plot3DPanel)plot).addGridPlot("zSlice",((Plot3DPanel)plot).getPlot(index).getColor(), x_Lower, z_Lower, y_Lower);
                    
                }
            }
        
        //setPlotAxisProperties();
        
        ((Plot3DPanel)plot).setAxisLabel(0, "X-Axis");
        ((Plot3DPanel)plot).setAxisLabel(1, "Z-Axis");
        ((Plot3DPanel)plot).setAxisLabel(2, "Y-Axis");
        ((Plot3DPanel)plot).plotLegend.removeAll();
       
        title = "zSlices based general type-2 type-2 fuzzy set plot of set: "+set.getName();    
    }    
    
   private void setPlotAxisProperties()
   {
        ((Plot3DPanel)plot).setFixedBounds(0, 0, 100);
        ((Plot3DPanel)plot).setFixedBounds(1, 0, 1);
        ((Plot3DPanel)plot).setFixedBounds(2, 0, 1);
        ((Plot3DPanel)plot).getAxis(0).setLabelFont(axisFont);
        ((Plot3DPanel)plot).getAxis(1).setLabelFont(axisFont);
        ((Plot3DPanel)plot).getAxis(2).setLabelFont(axisFont);
        ((Plot3DPanel)plot).getAxis(0).setLightLabelFont(axisLightFont);
        ((Plot3DPanel)plot).getAxis(1).setLightLabelFont(axisLightFont);
        ((Plot3DPanel)plot).getAxis(2).setLightLabelFont(axisLightFont);       
   }
    
    /**
     * 
     * @param plotName The name of the plot
     * @param xyzLabels Labels for each axis in order
     * @param x The array of x discretisations
     * @param y The array of y discretisations
     * @param z The z values for the given number of x-y combinations
     * @param yAxisRange The desired range for the yAxis
     * @param plotNaNasZero If true, then the provided z array will be check for NaN values which will be replaced with 0 for plotting.
     * This is useful for example when a fuzzy inference system does not contain fuzzy sets for the whole input range, i.e. outputs are undefined
     *  for parts of the input space.
     */
    public void plotControlSurface(String plotName, String[] xyzLabels, double[] x, double[] y, double[][] z, Tuple yAxisRange, boolean plotNaNasZero)
    {
        plot = new Plot3DPanel("SOUTH");
        ((Plot3DPanel)plot).setFont(legendFont);   
        
        // add grid plot to the PlotPanel
        //spec legend
        int id = ((Plot3DPanel)plot).addGridPlot(plotName, x, y, z); 
        
        ((Plot3DPanel)plot).getAxis(0).setLabelFont(axisFont);
        ((Plot3DPanel)plot).getAxis(0).setLightLabelFont(axisLightFont);
        //((Plot3DPanel)plot).getAxis(0)
        ((Plot3DPanel)plot).getAxis(0).setLabelText(xyzLabels[0]);
        
        ((Plot3DPanel)plot).getAxis(1).setLabelFont(axisFont);
        ((Plot3DPanel)plot).getAxis(1).setLightLabelFont(axisLightFont);
        ((Plot3DPanel)plot).getAxis(1).setLabelText(xyzLabels[1]);
        
        ((Plot3DPanel)plot).getAxis(2).setLabelFont(axisFont);
        ((Plot3DPanel)plot).getAxis(2).setLightLabelFont(axisLightFont);
        ((Plot3DPanel)plot).getAxis(2).setLabelText(xyzLabels[2]);
        
        //check and fix the z values if desired:
        if(plotNaNasZero)
            for(int i=0;i<x.length;i++)
            {
                for (int j=0;j<y.length;j++)
                {
                    if(Double.isNaN(z[j][i]))
                        z[j][i] = 0.0;
                }
            }

        ((Plot3DPanel)plot).setFixedBounds(2,yAxisRange.getLeft(), yAxisRange.getRight());
        title = plotName;
                
    }

    
    /**
     * Discretizes a given interval and returns all discrete points.
     * @param support A tuple which defines the left and right endpoints of the interval.
     * @param discLevel The number of discretizations.
     * @return A double array with teh discretized values of length discLevel.
     */
    private double[] discretize(Tuple support, int discLevel)
    {
        double[] d = new double[discLevel];
        double stepSize = (support.getSize())/(discLevel-1.0);
        d[0] = support.getLeft();
        d[d.length-1] = support.getRight();
        for(int i=1;i<d.length-1;i++)
        {
            d[i] = support.getLeft()+i*stepSize;
        }
        return d;
    }
      
    public void show(String title)
    {
        this.title=title;
        show();
    }
    public void show()
    {
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600);
        frame.setContentPane(plot);
        frame.setVisible(true);           
    }    
}
