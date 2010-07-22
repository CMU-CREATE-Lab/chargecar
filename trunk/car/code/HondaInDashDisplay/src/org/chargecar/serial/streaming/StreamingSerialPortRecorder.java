package org.chargecar.serial.streaming;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import edu.cmu.ri.createlab.serial.SerialPortEnumerator;
import edu.cmu.ri.createlab.serial.SerialPortException;
import edu.cmu.ri.createlab.serial.SerialPortIOHelper;
import edu.cmu.ri.createlab.serial.config.BaudRate;
import edu.cmu.ri.createlab.serial.config.CharacterSize;
import edu.cmu.ri.createlab.serial.config.FlowControl;
import edu.cmu.ri.createlab.serial.config.Parity;
import edu.cmu.ri.createlab.serial.config.SerialIOConfiguration;
import edu.cmu.ri.createlab.serial.config.StopBits;
import edu.cmu.ri.createlab.util.commandline.BaseCommandLineApplication;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
@SuppressWarnings({"UseOfSystemOutOrSystemErr"})
public final class StreamingSerialPortRecorder extends BaseCommandLineApplication
   {
   private static final Log LOG = LogFactory.getLog(StreamingSerialPortRecorder.class);

   public static void main(final String[] args)
      {
      final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

      new StreamingSerialPortRecorder(in).run();
      }

   private StreamingSerialPortRecorder(final BufferedReader in)
      {
      super(in);

      registerAction("r", recordAction);
      registerAction(QUIT_COMMAND, quitAction);
      }

   @Override
   protected void menu()
      {
      println("COMMANDS -----------------------------------");
      println("");
      println("r         Record data from a serial port");
      println("");
      println("q         Quit");
      println("");
      println("--------------------------------------------");
      }

   private final Runnable recordAction =
         new Runnable()
         {
         public void run()
            {
            try
               {
               // prompt user for port to connect to
               final String serialPortName = getSerialPortName();

               if (serialPortName == null)
                  {
                  return;
                  }

               // prompt the user for the baud rate
               final String baudStr = readString("Baud: ");
               final BaudRate baudRate = BaudRate.findByName(baudStr);

               if (baudRate == null)
                  {
                  println("Invalid baud rate");
                  return;
                  }

               // prompt the user for the number of seconds to record
               final Integer numSecondsToRead = readInteger("Num seconds to record: ");
               if (numSecondsToRead == null || numSecondsToRead <= 0)
                  {
                  println("Invalid duration");
                  return;
                  }

               // prompt the user for the number of seconds to record
               final String outputFilename = readString("Output filename: ");
               if (outputFilename == null || outputFilename.length() <= 0)
                  {
                  println("Invalid output filename");
                  return;
                  }

               // prompt the user for the output filename
               final File outputFile = new File(outputFilename);
               if (outputFile.exists())
                  {
                  println("Output file already exists or does not denote a valid file");
                  return;
                  }

               // set up the serial port config
               final SerialIOConfiguration config = new SerialIOConfiguration(serialPortName,
                                                                              baudRate,
                                                                              CharacterSize.EIGHT,
                                                                              Parity.NONE,
                                                                              StopBits.ONE,
                                                                              FlowControl.NONE);

               // connect to the serial port
               final SerialIOManager serialIOManager = new DefaultSerialIOManager(StreamingSerialPortRecorder.class.getSimpleName(), config);
               if (serialIOManager.connect())
                  {
                  // create an output file stream
                  final DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(outputFile));

                  // read from the serial port
                  final SerialPortIOHelper ioHelper = serialIOManager.getSerialPortIoHelper();
                  final long endTime = System.currentTimeMillis() + numSecondsToRead * 1000;
                  while (System.currentTimeMillis() < endTime)
                     {
                     if (ioHelper.isDataAvailable())
                        {
                        final int b = ioHelper.read();
                        if (b >= 0)
                           {
                           System.out.println((byte)b + (b >= 32 ? " = [" + (char)b + "]" : ""));
                           outputStream.writeByte(b);
                           }
                        else
                           {
                           LOG.error("End of stream.");
                           break;
                           }
                        }
                     }
                  serialIOManager.disconnect();
                  outputStream.close();
                  }
               else
                  {
                  println("Failed to connect to the serial port");
                  }
               }

            catch (SerialPortException e)
               {
               LOG.error("SerialPortException while trying to connect to the serial port", e);
               }
            catch (FileNotFoundException e)
               {
               LOG.error("FileNotFoundException while trying to create the FileOutputStream", e);
               }
            catch (IOException e)
               {
               LOG.error("IOException while trying to connect to or read from the serial port", e);
               }
            }
         };

   private String getSerialPortName()
      {
      // get the list of serial ports
      final SortedMap<Integer, String> portMap = enumeratePorts();

      // prompt user for port to connect to
      if (!portMap.isEmpty())
         {
         final Integer index = readInteger("Connect to port number: ");

         if (index == null)
            {
            println("Invalid port");
            }
         else
            {
            return portMap.get(index);
            }
         }

      return null;
      }

   private final Runnable quitAction =
         new Runnable()
         {
         public void run()
            {
            println("Bye!");
            }
         };

   @SuppressWarnings({"UseOfSystemOutOrSystemErr"})
   private final SortedMap<Integer, String> enumeratePorts()
      {
      final SortedSet<String> availableSerialPorts = SerialPortEnumerator.getAvailableSerialPorts();
      final SortedMap<Integer, String> portMap = new TreeMap<Integer, String>();

      if ((availableSerialPorts != null) && (!availableSerialPorts.isEmpty()))
         {
         println("Available serial port(s):");
         int i = 1;
         for (final String portName : availableSerialPorts)
            {
            System.out.printf("%6d:  %s\n", i, portName);
            portMap.put(i++, portName);
            }
         }
      else
         {
         println("No available serial ports.");
         }

      return portMap;
      }
   }
