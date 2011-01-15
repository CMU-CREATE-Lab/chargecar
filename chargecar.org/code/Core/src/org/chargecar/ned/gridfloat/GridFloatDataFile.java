package org.chargecar.ned.gridfloat;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.chargecar.ned.ElevationDataFile;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class GridFloatDataFile implements ElevationDataFile
   {
   private static final Logger LOG = Logger.getLogger(GridFloatDataFile.class);

   public static final int BYTES_PER_FLOAT = Float.SIZE / 8;

   private static final String HEADER_FILE_EXTENSION = ".hdr";
   private static final String DATA_FILE_EXTENSION = ".flt";

   private static final FileFilter IS_VALID_FILE_FILTER =
         new FileFilter()
         {
         public boolean accept(final File file)
            {
            return (file != null && file.isFile());
            }
         };

   public static final FileFilter HEADER_FILE_FILTER =
         new FileFilter()
         {
         public boolean accept(final File file)
            {
            return (IS_VALID_FILE_FILTER.accept(file) && file.getName().endsWith(HEADER_FILE_EXTENSION));
            }
         };

   private static final FileFilter DATA_FILE_WITH_EXTENSION_FILTER =
         new FileFilter()
         {
         public boolean accept(final File file)
            {
            return (IS_VALID_FILE_FILTER.accept(file) && file.getName().endsWith(DATA_FILE_EXTENSION));
            }
         };

   private static final FileFilter DATA_FILE_WITHOUT_EXTENSION_FILTER =
         new FileFilter()
         {
         public boolean accept(final File file)
            {
            if (IS_VALID_FILE_FILTER.accept(file))
               {
               final String filename = file.getName();
               if (filename.lastIndexOf('.') < 0)
                  {
                  final File headerFile = new File(file.getParentFile(), filename + HEADER_FILE_EXTENSION);
                  return HEADER_FILE_FILTER.accept(headerFile);
                  }
               }
            return false;
            }
         };

   public static final FileFilter DATA_FILE_FILTER =
         new FileFilter()
         {
         public boolean accept(final File file)
            {
            return DATA_FILE_WITH_EXTENSION_FILTER.accept(file) || DATA_FILE_WITHOUT_EXTENSION_FILTER.accept(file);
            }
         };

   private static final String PROPERTY_NUM_COLUMNS = "ncols";
   private static final String PROPERTY_NUM_ROWS = "nrows";
   private static final String PROPERTY_LOWER_LEFT_CORNER_LONGITUDE = "xllcorner";
   private static final String PROPERTY_LOWER_LEFT_CORNER_LATITUDE = "yllcorner";
   private static final String PROPERTY_CELL_SIZE = "cellsize";
   private static final String PROPERTY_NO_DATA_VALUE = "NODATA_value";
   private static final String PROPERTY_BYTE_ORDER = "byteorder";
   private static final String PROPERTY_VALUE_BIG_ENDIAN = "MSBFIRST";
   private static final String PROPERTY_VALUE_LITTLE_ENDIAN = "LSBFIRST";

   private static final Set<String> PROPERTY_KEYS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(PROPERTY_NUM_COLUMNS,
                                                                                                                  PROPERTY_NUM_ROWS,
                                                                                                                  PROPERTY_LOWER_LEFT_CORNER_LONGITUDE,
                                                                                                                  PROPERTY_LOWER_LEFT_CORNER_LATITUDE,
                                                                                                                  PROPERTY_CELL_SIZE,
                                                                                                                  PROPERTY_NO_DATA_VALUE,
                                                                                                                  PROPERTY_BYTE_ORDER)));
   private static final Set<String> VALID_BYTE_ORDER_VALUES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(PROPERTY_VALUE_BIG_ENDIAN, PROPERTY_VALUE_LITTLE_ENDIAN)));

   private static File getDataFileFromHeaderFile(final File headerFile)
      {
      final File directory = headerFile.getParentFile();
      final String headerFileName = headerFile.getName();
      final String baseFileName = headerFileName.substring(0, headerFileName.lastIndexOf('.'));

      File dataFile = new File(directory, baseFileName);
      if (DATA_FILE_WITHOUT_EXTENSION_FILTER.accept(dataFile))
         {
         return dataFile;
         }
      dataFile = new File(directory, baseFileName + ".flt");
      if (IS_VALID_FILE_FILTER.accept(dataFile))
         {
         return dataFile;
         }

      return null;
      }

   private static File getHeaderFileFromDataFile(final File dataFile)
      {
      if (DATA_FILE_WITHOUT_EXTENSION_FILTER.accept(dataFile))
         {
         return new File(dataFile.getParentFile(), dataFile.getName() + HEADER_FILE_EXTENSION);
         }
      if (DATA_FILE_WITH_EXTENSION_FILTER.accept(dataFile))
         {
         final String dataFileName = dataFile.getName();
         final String baseFileName = dataFileName.substring(0, dataFileName.lastIndexOf('.'));
         final File headerFile = new File(dataFile.getParentFile(), baseFileName + HEADER_FILE_EXTENSION);
         if (IS_VALID_FILE_FILTER.accept(headerFile))
            {
            return headerFile;
            }
         }

      return null;
      }

   /**
    * Constructs a <code>GridFloatDataFile</code> using the given {@link File} which may denote either a GridFloat data
    * file (*.flt or no extension) or a header file (*.hdr).
    */
   public static GridFloatDataFile create(final File file) throws IOException
      {
      final File headerFile;
      if (DATA_FILE_FILTER.accept(file))
         {
         headerFile = getHeaderFileFromDataFile(file);
         }
      else
         {
         headerFile = file;
         }

      if (HEADER_FILE_FILTER.accept(headerFile))
         {
         try
            {
            final List lines = FileUtils.readLines(headerFile);
            final Pattern whitespacePattern = Pattern.compile("\\s+");
            final Map<String, String> propertyMap = new HashMap<String, String>(lines.size());
            for (final Object line : lines)
               {
               final String[] keyAndValue = whitespacePattern.split((String)line, 2);
               if (keyAndValue != null && keyAndValue.length == 2 && PROPERTY_KEYS.contains(keyAndValue[0]))
                  {
                  propertyMap.put(keyAndValue[0], keyAndValue[1]);
                  }
               }

            if (propertyMap.size() == PROPERTY_KEYS.size())
               {
               try
                  {
                  final String byteOrder = propertyMap.get(PROPERTY_BYTE_ORDER);
                  if (!VALID_BYTE_ORDER_VALUES.contains(byteOrder))
                     {
                     throw new Exception("Invalid byte order value [" + byteOrder + "]");
                     }

                  return new GridFloatDataFile(headerFile,
                                               Double.parseDouble(propertyMap.get(PROPERTY_LOWER_LEFT_CORNER_LONGITUDE)),
                                               Double.parseDouble(propertyMap.get(PROPERTY_LOWER_LEFT_CORNER_LATITUDE)),
                                               Double.parseDouble(propertyMap.get(PROPERTY_CELL_SIZE)),
                                               Double.parseDouble(propertyMap.get(PROPERTY_NO_DATA_VALUE)),
                                               Integer.parseInt(propertyMap.get(PROPERTY_NUM_COLUMNS)),
                                               Integer.parseInt(propertyMap.get(PROPERTY_NUM_ROWS)),
                                               byteOrder.equals(PROPERTY_VALUE_BIG_ENDIAN));
                  }
               catch (Exception e)
                  {
                  LOG.error("Exception while trying to construct GridFloatDataFile instance, returning null.", e);
                  }
               }
            else
               {
               LOG.error("Incomplete GridFloatDataFile specification (missing parameters), returning null.");
               }
            return null;
            }
         catch (IOException e)
            {
            if (LOG.isEnabledFor(Level.ERROR))
               {
               LOG.error("IOException while trying to read the header file [" + headerFile + "]", e);
               }
            throw e;
            }
         }
      else
         {
         throw new IOException("The file [" + headerFile + "] does not appear to be a GridFloatDataFile.");
         }
      }

   public static double convertGridFloatToElevation(final byte[] floatBytes, final boolean isBigEndian)
      {
      final ByteBuffer byteBuffer = ByteBuffer.wrap(floatBytes);
      byteBuffer.order(isBigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
      return byteBuffer.getFloat();
      }

   private final File headerFile;
   private final File dataFile;
   private final int numColumns;
   private final int numRows;
   private final int maxCol;
   private final int maxRow;
   private final double lowerLeftCornerLongitude;
   private final double lowerLeftCornerLatitude;
   private final double upperRightCornerLongitude;
   private final double upperRightCornerLatitude;
   private final double cellSize;
   private final double noDataValue;
   private final boolean isBigEndian;

   private RandomAccessFile randomAccessFile;

   private GridFloatDataFile(final File headerFile,
                             final double lowerLeftCornerLongitude,
                             final double lowerLeftCornerLatitude,
                             final double cellSize,
                             final double noDataValue,
                             final int numColumns,
                             final int numRows,
                             final boolean isBigEndian)
      {
      this.headerFile = headerFile;
      this.dataFile = getDataFileFromHeaderFile(headerFile);
      if (dataFile == null)
         {
         throw new IllegalArgumentException("Couldn't find grid float file for header file [" + headerFile + "]");
         }
      this.numColumns = numColumns;
      this.numRows = numRows;
      this.maxCol = numColumns - 1;
      this.maxRow = numRows - 1;
      this.cellSize = cellSize;
      this.lowerLeftCornerLongitude = lowerLeftCornerLongitude;
      this.lowerLeftCornerLatitude = lowerLeftCornerLatitude;
      this.upperRightCornerLongitude = lowerLeftCornerLongitude + (maxCol) * cellSize;
      this.upperRightCornerLatitude = lowerLeftCornerLatitude + (maxRow) * cellSize;
      this.noDataValue = noDataValue;
      this.isBigEndian = isBigEndian;
      }

   public String getId()
      {
      return headerFile.getAbsolutePath();
      }

   public File getHeaderFile()
      {
      return headerFile;
      }

   public File getDataFile()
      {
      return dataFile;
      }

   public double getLowerLeftCornerLongitude()
      {
      return lowerLeftCornerLongitude;
      }

   public double getLowerLeftCornerLatitude()
      {
      return lowerLeftCornerLatitude;
      }

   public double getUpperRightCornerLongitude()
      {
      return upperRightCornerLongitude;
      }

   public double getUpperRightCornerLatitude()
      {
      return upperRightCornerLatitude;
      }

   public double getCellSize()
      {
      return cellSize;
      }

   public double getNoDataValue()
      {
      return noDataValue;
      }

   public int getNumColumns()
      {
      return numColumns;
      }

   public int getNumRows()
      {
      return numRows;
      }

   public boolean isBigEndian()
      {
      return isBigEndian;
      }

   public boolean contains(final Double longitude, final Double latitude)
      {
      return (Double.compare(lowerLeftCornerLongitude, longitude) <= 0 &&
              Double.compare(longitude, upperRightCornerLongitude) <= 0 &&
              Double.compare(lowerLeftCornerLatitude, latitude) <= 0 &&
              Double.compare(latitude, upperRightCornerLatitude) <= 0);
      }

   public void open() throws FileNotFoundException
      {
      if (randomAccessFile == null)
         {
         randomAccessFile = new RandomAccessFile(dataFile, "r");
         }
      }

   // BilinearInterpolation

   public Double getElevation(final double longitude, final double latitude)
      {
      if (randomAccessFile != null && contains(longitude, latitude))
         {
         // estimate the elevation using bilinear interpolation (using the algorithm described at: http://en.wikipedia.org/wiki/Bilinear_interpolation)
         final double llLongitudeOffset = longitude - lowerLeftCornerLongitude;
         final double llLatitudeOffset = latitude - lowerLeftCornerLatitude;

         final double llLongitudeOffsetDividedByCellSizeFloored = Math.floor(llLongitudeOffset / cellSize);
         final double llLatitudeOffsetDividedByCellSizeFloored = Math.floor(llLatitudeOffset / cellSize);

         final int llCol = validateColumn((int)llLongitudeOffsetDividedByCellSizeFloored);
         final int llRow = maxRow - validateRow((int)llLatitudeOffsetDividedByCellSizeFloored);
         final int urCol = validateColumn(llCol + 1);
         final int urRow = validateRow(llRow - 1);  // yes this is correct--the gridfloat data file is flipped vertically

         final Double f_Q11 = getElevation(llCol, llRow);
         final Double f_Q21 = getElevation(urCol, llRow);
         final Double f_Q12 = getElevation(llCol, urRow);
         final Double f_Q22 = getElevation(urCol, urRow);
         if (!isNoDataElevation(f_Q11) && !isNoDataElevation(f_Q21) && !isNoDataElevation(f_Q12) && !isNoDataElevation(f_Q22))
            {
            final double x = longitude;
            final double y = latitude;
            final double x1 = llLongitudeOffsetDividedByCellSizeFloored * cellSize + lowerLeftCornerLongitude;
            final double y1 = llLatitudeOffsetDividedByCellSizeFloored * cellSize + lowerLeftCornerLatitude;
            final double x2 = x1 + cellSize;
            final double y2 = y1 + cellSize;
            final double denominator = (x2 - x1) * (y2 - y1);
            final double piece1 = f_Q11 / denominator * (x2 - x) * (y2 - y);
            final double piece2 = f_Q21 / denominator * (x - x1) * (y2 - y);
            final double piece3 = f_Q12 / denominator * (x2 - x) * (y - y1);
            final double piece4 = f_Q22 / denominator * (x - x1) * (y - y1);
            return piece1 + piece2 + piece3 + piece4;
            }
         }
      return null;
      }

   private int validateColumn(final int column)
      {
      return Math.min(column, maxCol);
      }

   private int validateRow(final int row)
      {
      return Math.min(row, maxRow);
      }

   // NearestNeighbor

   public Double getElevationNearestNeighbor(final double longitude, final double latitude)
      {
      if (randomAccessFile != null && contains(longitude, latitude))
         {
         final int col = convertLongitudeToColumn(longitude);
         final int row = convertLatitudeToRow(latitude);

         return getElevation(col, row);
         }
      return null;
      }

   private int convertLongitudeToColumn(final double longitude)
      {
      final double offsetFromLowerLeft = longitude - lowerLeftCornerLongitude;
      final int column = (int)Math.round(offsetFromLowerLeft / cellSize);
      return validateColumn(column);
      }

   private int convertLatitudeToRow(final double latitude)
      {
      final double offsetFromLowerLeft = latitude - lowerLeftCornerLatitude;
      final int row = (int)Math.round(offsetFromLowerLeft / cellSize);
      return maxRow - validateRow(row);
      }

   private Double getElevation(final int col, final int row)
      {
      final long floatIndex = (long)row * (long)numColumns + (long)col;
      final long seekPosition = floatIndex * BYTES_PER_FLOAT;
      try
         {
         randomAccessFile.seek(seekPosition);
         final byte[] buffer = new byte[BYTES_PER_FLOAT];
         final int bytesRead = randomAccessFile.read(buffer);
         if (bytesRead == BYTES_PER_FLOAT)
            {
            return GridFloatDataFile.convertGridFloatToElevation(buffer, isBigEndian);
            }
         else
            {
            if (LOG.isEnabledFor(Level.ERROR))
               {
               LOG.error("Invalid number of bytes read: actual = [" + bytesRead + "], expected [" + BYTES_PER_FLOAT + "]");
               }
            }
         }
      catch (IOException e)
         {
         if (LOG.isEnabledFor(Level.ERROR))
            {
            LOG.error("IOException while seeking to or reading from position [" + seekPosition + "][" + floatIndex + "] for col [" + col + "] and row [" + row + "]", e);
            }
         }
      return null;
      }

   public boolean isNoDataElevation(final Double elevation)
      {
      return elevation == null || Double.compare(noDataValue, elevation) == 0;
      }

   public void close()
      {
      if (randomAccessFile != null)
         {
         try
            {
            randomAccessFile.close();
            }
         catch (IOException e)
            {
            LOG.error("IOException while trying to close the RandomAccessFile", e);
            }
         randomAccessFile = null;
         }
      }

   @Override
   public boolean equals(final Object o)
      {
      if (this == o)
         {
         return true;
         }
      if (o == null || getClass() != o.getClass())
         {
         return false;
         }

      final GridFloatDataFile that = (GridFloatDataFile)o;

      if (Double.compare(that.cellSize, cellSize) != 0)
         {
         return false;
         }
      if (isBigEndian != that.isBigEndian)
         {
         return false;
         }
      if (Double.compare(that.lowerLeftCornerLatitude, lowerLeftCornerLatitude) != 0)
         {
         return false;
         }
      if (Double.compare(that.lowerLeftCornerLongitude, lowerLeftCornerLongitude) != 0)
         {
         return false;
         }
      if (Double.compare(that.noDataValue, noDataValue) != 0)
         {
         return false;
         }
      if (numColumns != that.numColumns)
         {
         return false;
         }
      if (numRows != that.numRows)
         {
         return false;
         }
      if (Double.compare(that.upperRightCornerLatitude, upperRightCornerLatitude) != 0)
         {
         return false;
         }
      if (Double.compare(that.upperRightCornerLongitude, upperRightCornerLongitude) != 0)
         {
         return false;
         }
      if (dataFile != null ? !dataFile.equals(that.dataFile) : that.dataFile != null)
         {
         return false;
         }
      if (headerFile != null ? !headerFile.equals(that.headerFile) : that.headerFile != null)
         {
         return false;
         }

      return true;
      }

   @Override
   public int hashCode()
      {
      int result;
      long temp;
      result = headerFile != null ? headerFile.hashCode() : 0;
      result = 31 * result + (dataFile != null ? dataFile.hashCode() : 0);
      result = 31 * result + numColumns;
      result = 31 * result + numRows;
      temp = lowerLeftCornerLongitude != +0.0d ? Double.doubleToLongBits(lowerLeftCornerLongitude) : 0L;
      result = 31 * result + (int)(temp ^ (temp >>> 32));
      temp = lowerLeftCornerLatitude != +0.0d ? Double.doubleToLongBits(lowerLeftCornerLatitude) : 0L;
      result = 31 * result + (int)(temp ^ (temp >>> 32));
      temp = upperRightCornerLongitude != +0.0d ? Double.doubleToLongBits(upperRightCornerLongitude) : 0L;
      result = 31 * result + (int)(temp ^ (temp >>> 32));
      temp = upperRightCornerLatitude != +0.0d ? Double.doubleToLongBits(upperRightCornerLatitude) : 0L;
      result = 31 * result + (int)(temp ^ (temp >>> 32));
      temp = cellSize != +0.0d ? Double.doubleToLongBits(cellSize) : 0L;
      result = 31 * result + (int)(temp ^ (temp >>> 32));
      temp = noDataValue != +0.0d ? Double.doubleToLongBits(noDataValue) : 0L;
      result = 31 * result + (int)(temp ^ (temp >>> 32));
      result = 31 * result + (isBigEndian ? 1 : 0);
      return result;
      }

   @Override
   public String toString()
      {
      final StringBuilder sb = new StringBuilder();
      sb.append("GridFloatDataFile");
      sb.append("{headerFile=").append(headerFile);
      sb.append(", dataFile=").append(dataFile);
      sb.append(", numColumns=").append(numColumns);
      sb.append(", numRows=").append(numRows);
      sb.append(", lowerLeftCornerLongitude=").append(lowerLeftCornerLongitude);
      sb.append(", lowerLeftCornerLatitude=").append(lowerLeftCornerLatitude);
      sb.append(", upperRightCornerLongitude=").append(upperRightCornerLongitude);
      sb.append(", upperRightCornerLatitude=").append(upperRightCornerLatitude);
      sb.append(", cellSize=").append(cellSize);
      sb.append(", noDataValue=").append(noDataValue);
      sb.append(", isBigEndian=").append(isBigEndian);
      sb.append('}');
      return sb.toString();
      }
   }
