/*
 * Consequent.java
 *
 * Created on 19 November 2008, 11:38
 *
 * Copyright 2008 Christian Wagner All Rights Reserved.
 */

package type1.system;

import generic.Output;
import java.io.Serializable;
import type1.sets.T1MF_Interface;

/**
 * Consequent for a fuzzy rule of a Type-1 Fuzzy System.
 * @author chwagn
 */
public class T1_Consequent implements Serializable
{
    private String name;
    private T1MF_Interface mF;
    private Output output;
    
    public T1_Consequent(T1MF_Interface mF, Output output)
    {
        this.name = mF.getName();
        this.mF = mF;
        this.output = output;
    }
    public T1_Consequent(String name, T1MF_Interface mF, Output output)
    {
        this.name = name;
        this.mF = mF;
        this.output = output;
    }    
    
    /**
     *Allows changing the membership function defining the consequent.
     */
    public void setMF(T1MF_Interface mF)
    {
        this.mF = mF;
    }
    
    /**
     *Returns the membership function defining the consequent.
     */
    public T1MF_Interface getMF()
    {
        return mF;
    }

    public Output getOutput() {
        return output;
    }

    public void setOutput(Output output) {
        this.output = output;
    }
    
    public String getName()
    {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
   
    @Override
    public String toString()
    {
        return "Consequent with MF: "+ mF.toString();
    }    
}
