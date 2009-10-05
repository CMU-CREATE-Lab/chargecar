package org.chargecar.ned;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
@SuppressWarnings({"UseOfSystemOutOrSystemErr"})
public final class ElevationMapRenderingFilter implements ElevationDataFilter
   {
   private static final Log LOG = LogFactory.getLog(ElevationMapRenderingFilter.class);

   private static final int MAX_GREY_VALUE = 255;
   private static final int[] COLORS = new int[MAX_GREY_VALUE + 1];

   static
      {
      for (int i = 0; i < COLORS.length; i++)
         {
         COLORS[i] = new Color(i, i, i).getRGB();
         }
      }

   private final double minElevation;
   private final double elevationRange;
   private final File outputFile;
   private final BufferedImage bimage;

   public ElevationMapRenderingFilter(final int numCols,
                                      final int numRows,
                                      final double minElevation,
                                      final double elevationRange,
                                      final File outputFile)
      {
      this.minElevation = minElevation;
      this.elevationRange = elevationRange;
      this.outputFile = outputFile;

      // Create buffered image that does not support transparency
      bimage = new BufferedImage(numCols, numRows, BufferedImage.TYPE_INT_RGB);
      }

   public void onBeforeProcessing()
      {
      // do nothing
      }

   public void processElevation(final int col, final int row, final double elevationInMeters)
      {
      // convert elevation to color
      final double numerator = elevationInMeters - minElevation;
      final int color = (int)(numerator / elevationRange * MAX_GREY_VALUE);

      // Draw on the image
      bimage.setRGB(col, row, COLORS[color]);
      }

   public void onAfterProcessing(final long elapsedTimeInMilliseconds)
      {
      try
         {
         ImageIO.write(bimage, "png", outputFile);
         }
      catch (IOException e)
         {
         if (LOG.isErrorEnabled())
            {
            LOG.error("Failed to write image to output file [" + outputFile + "]: " + e);
            }
         }
      if (LOG.isDebugEnabled())
         {
         LOG.debug("Elapsed time to generate map (ms) = " + elapsedTimeInMilliseconds);
         }
      }
   }