package org.chargecar.lcddisplay.lcd;

import edu.cmu.ri.createlab.LCD;
import edu.cmu.ri.createlab.LCDConstants;
import edu.cmu.ri.createlab.LCDProxy;
import edu.cmu.ri.createlab.display.character.CharacterDisplay;
import org.apache.log4j.Logger;

import java.awt.*;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class LCDPanel implements CharacterDisplay {
    private static final Logger LOG = Logger.getLogger(LCDPanel.class);
    private static final String LCD_SCROLL_UP_ARROW = "^ ";
    private static final String LCD_SCROLL_DOWN_ARROW = "v ";
    private static final int LCD_WIDTH_OF_SCROLL_ARROW_AND_PADDING = Math.max(LCD_SCROLL_UP_ARROW.length(), LCD_SCROLL_DOWN_ARROW.length());
    private static final String LCD_PADDING_FOR_LINES_WITHOUT_ARROWS_WHEN_IN_SCROLLING_MODE = padRight("", LCD_WIDTH_OF_SCROLL_ARROW_AND_PADDING);

    private static final String FONT_NAME = "Monaco";
    private static final Font FONT_LARGE = new Font(FONT_NAME, 0, 24);

    // taken from http://stackoverflow.com/questions/388461/padding-strings-in-java
    private static String padRight(final String s, final int n) {
        return String.format("%1$-" + n + "s", s);
    }

    private final int numRows;
    private final int numColumns;
    private final int totalCharacterCount;
    private final int numColumnsWhenInScrollingMode;


    public LCDPanel(final int rows, final int columns) {
        numRows = rows;
        numColumns = columns;
        totalCharacterCount = numRows * numColumns;
        numColumnsWhenInScrollingMode = numColumns - LCD_WIDTH_OF_SCROLL_ARROW_AND_PADDING;
    }

    public int getRows() {
        return numRows;
    }

    public int getColumns() {
        return numColumns;
    }

    public void setText(final String text) {
        setText(text, true);
    }

    public void setText(final String text, final boolean willClearFirst) {
        if (text != null && text.length() > 0) {
            if (willClearFirst) {
                clear();
            }

            //wrap text
            for (int charIndex = 0, row = 0; charIndex < Math.min(text.length(), LCDConstants.NUM_ROWS * LCDConstants.NUM_COLS); charIndex += LCDConstants.NUM_COLS, row++) {
                setLine(row, text.substring(charIndex));
            }
        }
    }

    public void setTextWithScrollArrows(final String text) {
        if (text != null && text.length() > 0) {
            String theText = text;

            final StringBuilder textWithScrollArrows = new StringBuilder();
            for (int line = 0; line < numRows; line++) {
                // chop off at most the first numColumnsWhenInScrollingMode characters
                String lineText = theText.substring(0, Math.min(numColumnsWhenInScrollingMode, theText.length()));

                // update the remainder
                theText = theText.substring(lineText.length());

                if (theText.length() == 0) {
                    lineText = padRight(lineText, numColumnsWhenInScrollingMode);
                }

                if (line == 0) {
                    textWithScrollArrows.append(LCD_SCROLL_UP_ARROW).append(lineText);
                } else if (line == numRows - 1) {
                    textWithScrollArrows.append(LCD_SCROLL_DOWN_ARROW).append(lineText);
                } else {
                    textWithScrollArrows.append(LCD_PADDING_FOR_LINES_WITHOUT_ARROWS_WHEN_IN_SCROLLING_MODE).append(lineText);
                }
            }
            setText(textWithScrollArrows.toString());
        }
    }

    public void setLine(final int lineNumber, final String text) {
        setLine(lineNumber, text, true);
    }

    public void setLine(final int lineNumber, final String text, final boolean willClearLineFirst) {
        if (isValidRow(lineNumber)) {
            final LCD lcd = LCDProxy.getInstance();
            //if (willClearLineFirst) {
            //    lcd.setText(lineNumber, 0, LCDConstants.BLANK_LINE);
            //}
            if (text != null && text.length() > 0) {
                final int numChars = Math.min(text.length(), LCDConstants.NUM_COLS);
                String test = (numChars < numColumns) ? padRight(text.substring(0, numChars), numColumns - numChars) : text.substring(0, numChars);
                lcd.setText(lineNumber, 0, (numChars < numColumns) ? padRight(text.substring(0, numChars), numColumns) : text.substring(0, numChars));
            }

        }
    }

    public void setCharacter(final int row, final int col, final char character) {
        setCharacter(row, col, String.valueOf(character));
    }

    public void setCharacter(final int row, final int col, final String character) {
        if (isValidRow(row) && isValidColumn(col)) {
            final LCD lcd = LCDProxy.getInstance();
            lcd.setText(row, col, String.valueOf(character));
        }
    }

    public void clear() {
        for (int row = 0; row < numRows; row++) {
            clearLine(row);
        }
    }

    public void clearLine(final int lineNumber) {
        if (isValidRow(lineNumber)) {
            setLine(lineNumber, LCDConstants.BLANK_LINE);
        }
    }

    private boolean isValidColumn(final int col) {
        return (col >= 0 && col < numColumns);
    }

    private boolean isValidRow(final int row) {
        return (row >= 0 && row < numRows);
    }
}
