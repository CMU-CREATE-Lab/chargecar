package org.chargecar.ned;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class ElevationDatasetException extends Exception
   {
   public ElevationDatasetException()
      {
      }

   public ElevationDatasetException(final String message)
      {
      super(message);
      }

   public ElevationDatasetException(final String message, final Throwable cause)
      {
      super(message, cause);
      }

   public ElevationDatasetException(final Throwable cause)
      {
      super(cause);
      }
   }
