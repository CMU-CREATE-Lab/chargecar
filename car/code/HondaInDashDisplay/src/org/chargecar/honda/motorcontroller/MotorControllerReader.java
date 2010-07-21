package org.chargecar.honda.motorcontroller;

import java.util.Date;
import edu.cmu.ri.createlab.serial.config.BaudRate;
import edu.cmu.ri.createlab.serial.config.CharacterSize;
import edu.cmu.ri.createlab.serial.config.FlowControl;
import edu.cmu.ri.createlab.serial.config.Parity;
import edu.cmu.ri.createlab.serial.config.SerialIOConfiguration;
import edu.cmu.ri.createlab.serial.config.StopBits;
import edu.cmu.ri.createlab.util.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.chargecar.serial.streaming.DefaultSerialIOManager;
import org.chargecar.serial.streaming.SerialIOManager;
import org.chargecar.serial.streaming.StreamingSerialPortReader;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class MotorControllerReader extends StreamingSerialPortReader<MotorControllerEvent>
   {
   private static final Log LOG = LogFactory.getLog(MotorControllerReader.class);
   private static final char SENTENCE_DELIMETER = '\f';
   private static final String SENTENCE_PREFIX = ":SRPM";
   private static final String SENTENCE_PREFIX_REGEX = ".:SRPM";
   private static final String ERROR_PREFIX = ":SCode";

   public MotorControllerReader(final String serialPortName)
      {
      this(new DefaultSerialIOManager(MotorControllerReader.class.getClass().getSimpleName(),
                                      new SerialIOConfiguration(serialPortName,
                                                                BaudRate.BAUD_9600,
                                                                CharacterSize.EIGHT,
                                                                Parity.NONE,
                                                                StopBits.ONE,
                                                                FlowControl.NONE)));
      }

   public MotorControllerReader(final SerialIOManager serialIOManager)
      {
      super(SENTENCE_DELIMETER, serialIOManager);
      }

   protected void processSentence(final Date timestamp, final String sentence)
      {
      if (LOG.isDebugEnabled())
         {
         //LOG.debug("MotorControllerReader.processSentence(" + sentence + ")");
         }

      if (sentence != null)
         {
         final int errorPos = sentence.indexOf(ERROR_PREFIX);
         if (errorPos > 0)
            {
            final String errorCodeStr = sentence.substring(errorPos + ERROR_PREFIX.length());
            final Integer errorCode = StringUtils.convertStringToInteger(errorCodeStr.trim());

            if (LOG.isErrorEnabled())
               {
               if (errorCode == null)
                  {
                  LOG.error("MotorControllerReader.processSentence(): error condition detected, but could not parse sentence [" + sentence + "]");
                  }
               else
                  {
                  LOG.error("MotorControllerReader.processSentence(): error condition detected, code [" + errorCode + "]");
                  }
               }

            publishEvent(MotorControllerEvent.createErrorEvent(timestamp, errorCode == null ? 0 : errorCode));
            }
         else
            {
            final int pos = sentence.indexOf(SENTENCE_PREFIX);
            if (pos > 0 && (sentence.length() > (pos + SENTENCE_PREFIX.length())))
               {
               final String[] rpms = sentence.substring(pos + SENTENCE_PREFIX.length()).split(SENTENCE_PREFIX_REGEX);

               for (final String rpmStr : rpms)
                  {
                  final Integer rpm = StringUtils.convertStringToInteger(rpmStr.trim());

                  if (rpm != null)
                     {
                     publishEvent(MotorControllerEvent.createNormalEvent(timestamp, rpm));
                     }
                  }
               }
            else
               {
               if (LOG.isErrorEnabled())
                  {
                  LOG.error("MotorControllerReader.processSentence(): Unexpected sentence [" + sentence + "]");
                  }
               }
            }
         }
      }
   }
