package intervalType2.system;

import generic.Output;
import generic.Tuple;
import intervalType2.sets.IntervalT2Engine_Centroid;
import intervalType2.sets.IntervalT2MF_Interface;

public class IT2_Consequent
{
    private String name;
    private IntervalT2MF_Interface mF;
    private Tuple centroid;
    private Output output;
    private IntervalT2Engine_Centroid IEC;

    private final boolean DEBUG = false;        

    public IT2_Consequent(IntervalT2MF_Interface m, Output output)
    {
            mF = m;
            name = mF.getName();    //adopt name from MF
            this.output = output;
            
            //ensure the domain of the MF is constrained to the domain of the overall system output
            mF.setSupport(new Tuple(
                    Math.max(mF.getSupport().getLeft(), this.output.getDomain().getLeft()), 
                    Math.min(mF.getSupport().getRight(), this.output.getDomain().getRight())));
            
            IEC = new IntervalT2Engine_Centroid();
            centroid = IEC.getCentroid(m);
            if(DEBUG) System.out.println("Centroid values of interval consequent "+mF.getName()+" are: "+centroid.toString());
    }

    public IT2_Consequent(String name, IntervalT2MF_Interface m, Output output)
    {
            mF = m;
            this.name = name;
            this.output = output;
            
            //ensure the domain of the MF is constrained to the domain of the overall system output
            mF.setSupport(new Tuple(
                    Math.max(mF.getSupport().getLeft(), this.output.getDomain().getLeft()), 
                    Math.min(mF.getSupport().getRight(), this.output.getDomain().getRight())));            
            IEC = new IntervalT2Engine_Centroid();
            centroid = IEC.getCentroid(m);
            if(DEBUG) System.out.println("Centroid values of interval consequent "+mF.getName()+" are: "+centroid.toString());
    }    

    /**
        * Sets a consequent directly as a centroid, without using a membership function.
        * @param centroid
        */
    public IT2_Consequent(Tuple centroid)
    {
        this.centroid = centroid;
    }

    public String getName()
    {
            return name;
    }

    public Output getOutput() {
        return output;
    }

    public void setOutput(Output output) {
        this.output = output;
    }
    
    public void setName(String name) 
    {
        this.name = name;
    }
    
    public IntervalT2MF_Interface getMembershipFunction()
    {
            return mF;
    }

    public Tuple getCentroid()
    {
            return centroid;
    }
    @Override
    public String toString()
    {
        return "Consequent with MF: "+ mF.toString();
    }

}
