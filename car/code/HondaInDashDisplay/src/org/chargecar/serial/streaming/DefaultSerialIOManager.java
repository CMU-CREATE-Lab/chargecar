package org.chargecar.serial.streaming;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import edu.cmu.ri.createlab.serial.DefaultSerialPortIOHelper;
import edu.cmu.ri.createlab.serial.SerialPortEnumerator;
import edu.cmu.ri.createlab.serial.SerialPortException;
import edu.cmu.ri.createlab.serial.SerialPortIOHelper;
import edu.cmu.ri.createlab.serial.config.FlowControl;
import edu.cmu.ri.createlab.serial.config.Parity;
import edu.cmu.ri.createlab.serial.config.SerialIOConfiguration;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import org.apache.log4j.Logger;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class DefaultSerialIOManager implements SerialIOManager
   {
   private static final Logger LOG = Logger.getLogger(DefaultSerialIOManager.class);

   private static final int OPEN_PORT_TIMEOUT_MILLIS = 1000;
   private static final int RECEIVE_TIMEOUT_MILLIS = 1000;

   private static int convertParity(final Parity parity)
      {
      switch (parity)
         {
         case NONE:
            return SerialPort.PARITY_NONE;
         case EVEN:
            return SerialPort.PARITY_EVEN;
         case ODD:
            return SerialPort.PARITY_ODD;

         default:
            throw new IllegalArgumentException("Unexpected Parity [" + parity + "]");
         }
      }

   private SerialPort port;
   private SerialPortIOHelper ioHelper;
   private final String applicationName;
   private final SerialIOConfiguration config;

   public DefaultSerialIOManager(final String applicationName, final SerialIOConfiguration config)
      {
      this.applicationName = applicationName;
      this.config = config;
      }

   public boolean connect() throws SerialPortException, IOException
      {
      if (LOG.isDebugEnabled())
         {
         LOG.debug("StreamingSerialPortReader.connect(" + config.getPortDeviceName() + "): Calling SerialPortEnumerator.getSerialPortIdentifier(" + config.getPortDeviceName() + ")");
         }

      final CommPortIdentifier portIdentifier = SerialPortEnumerator.getSerialPortIdentifier(config.getPortDeviceName());

      if (LOG.isDebugEnabled())
         {
         LOG.debug("StreamingSerialPortReader.connect(" + config.getPortDeviceName() + "): Done calling SerialPortEnumerator.getSerialPortIdentifier(" + config.getPortDeviceName() + "), portIdentifier = " + portIdentifier);
         }

      if (portIdentifier != null)
         {
         port = null;

         try
            {
            // try to open the port
            LOG.debug("StreamingSerialPortReader.connect(): opening serial port");
            port = (SerialPort)portIdentifier.open(applicationName, OPEN_PORT_TIMEOUT_MILLIS);
            if (LOG.isDebugEnabled())
               {
               LOG.debug("StreamingSerialPortReader.connect(): done opening serial port = " + port);
               }

            // now configure the port
            if (port != null)
               {
               port.setSerialPortParams(config.getBaudRate().getValue(),
                                        config.getCharacterSize().getValue(),
                                        config.getStopBits().getValue(),
                                        convertParity(config.getParity()));

               // set the flow control
               if (FlowControl.HARDWARE.equals(config.getFlowControl()))
                  {
                  port.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN);
                  port.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_OUT);
                  }
               else if (FlowControl.SOFTWARE.equals(config.getFlowControl()))
                  {
                  port.setFlowControlMode(SerialPort.FLOWCONTROL_XONXOFF_IN);
                  port.setFlowControlMode(SerialPort.FLOWCONTROL_XONXOFF_OUT);
                  }
               else
                  {
                  port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
                  }

               // try to set the receive timeout
               if (LOG.isDebugEnabled())
                  {
                  LOG.debug("StreamingSerialPortReader.connect(): Setting serial port receive timeout to " + RECEIVE_TIMEOUT_MILLIS + "...");
                  }
               port.enableReceiveTimeout(RECEIVE_TIMEOUT_MILLIS);
               if (LOG.isDebugEnabled())
                  {
                  LOG.debug("StreamingSerialPortReader.connect(): Check whether setting serial port receive timeout worked: (is enabled=" + port.isReceiveTimeoutEnabled() + ",timeout=" + port.getReceiveTimeout() + ")");
                  }

               ioHelper = new DefaultSerialPortIOHelper(new BufferedInputStream(port.getInputStream()),
                                                        new BufferedOutputStream(port.getOutputStream()));

               return true;
               }
            }
         catch (PortInUseException e)
            {
            throw new SerialPortException("Failed to open serial port [" + config.getPortDeviceName() + "] because it is already in use", e);
            }
         catch (UnsupportedCommOperationException e)
            {
            port.close();
            throw new SerialPortException("Failed to configure serial port [" + config.getPortDeviceName() + "]", e);
            }
         }
      else
         {
         throw new SerialPortException("Failed to obtain the serial port [" + config.getPortDeviceName() + "].  Make sure that it exists and is not in use by another process.");
         }

      return false;
      }

   public SerialPortIOHelper getSerialPortIoHelper()
      {
      return ioHelper;
      }

   public void disconnect()
      {
      // shut down the serial port
      try
         {
         LOG.debug("DefaultSerialIOManager.disconnect(): Now attempting to close the serial port...");
         port.close();
         port = null;
         ioHelper = null;
         LOG.debug("DefaultSerialIOManager.disconnect(): Serial port closed successfully.");
         }
      catch (Exception e)
         {
         LOG.error("DefaultSerialIOManager.disconnect(): Exception while trying to close the serial port", e);
         }
      }
   }
