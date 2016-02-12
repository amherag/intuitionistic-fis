/*
 * BadParameterException.java
 *
 * Created on 14 March 2007, 18:21
 *
 * Author: Christian Wagner
 * Copyright 2006 Christian Wagner All Rights Reserved.
 */

package generic;

/**
 * Exception used when Fuzzy Objects (such as Membership Functions) get used with bad parameters.
 * @author Christian Wagner
 */
public class BadParameterException extends RuntimeException
{

    /** Creates a new instance of BadParameterException */
    public BadParameterException(String message)
    {
        super(message);
    }
    
}
