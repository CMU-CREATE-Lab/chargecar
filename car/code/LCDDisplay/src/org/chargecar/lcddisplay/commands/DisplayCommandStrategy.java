package org.chargecar.lcddisplay.commands;

import edu.cmu.ri.createlab.serial.CreateLabSerialDeviceNoReturnValueCommandStrategy;
import edu.cmu.ri.createlab.util.ByteUtils;
import org.chargecar.lcddisplay.LCDProxy;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */

public final class DisplayCommandStrategy extends CreateLabSerialDeviceNoReturnValueCommandStrategy {
    private final byte[] command;
    private static final byte COMMAND_PREFIX = 'D';
    private static final Charset ASCII_CHARSET = Charset.forName("US-ASCII");

    public DisplayCommandStrategy(final int row, final int column, final String displayString) {
        final byte[] displayStringByteArray = displayString.getBytes(ASCII_CHARSET);
        final byte[] tmp = new byte[]{COMMAND_PREFIX,
                ByteUtils.intToUnsignedByte(row),
                ByteUtils.intToUnsignedByte(column),
                ByteUtils.intToUnsignedByte(displayString.length())};
        final ByteBuffer bb = ByteBuffer.allocate(displayStringByteArray.length + tmp.length + 1);
        bb.put(tmp);
        bb.put(displayStringByteArray);
        bb.put(ByteUtils.intToUnsignedByte(LCDProxy.SEQUENCE_NUMBER.next()));
        this.command = bb.array();
    }

    public DisplayCommandStrategy(final int row, final int column, final String displayString, final boolean doAscii) {
        final byte[] displayStringByteArray = displayString.getBytes();
        final byte[] tmp = new byte[]{COMMAND_PREFIX,
                ByteUtils.intToUnsignedByte(row),
                ByteUtils.intToUnsignedByte(column),
                ByteUtils.intToUnsignedByte(displayString.length())};
        final ByteBuffer bb = ByteBuffer.allocate(displayStringByteArray.length + tmp.length + 1);
        bb.put(tmp);
        bb.put(displayStringByteArray);
        bb.put(ByteUtils.intToUnsignedByte(LCDProxy.SEQUENCE_NUMBER.next()));
        this.command = bb.array();
    }

    protected byte[] getCommand() {
        return command.clone();
    }

}


