/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import java.text.DecimalFormat;

/**
 * Formats things as Strings
 *
 * @version $Revision: 1.1 $, $Date: 2003/03/05 18:42:33 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.8
 */
public class FormatHelper {

    private static DecimalFormat smallNumberFormat = new DecimalFormat("00");

    private static DecimalFormat mediumNumberFormat = new DecimalFormat("0000");

    private static DecimalFormat bigCountFormat = new DecimalFormat("###000000");

    /**
     * Format like 00
     * @param value to format
     * @return formatted value
     */
    public static String formatSmallNumber(long value) {
        return smallNumberFormat.format(value);
    }

    /**
     * Format like 0000
     * @param value to format
     * @return formatted value
     */
    public static String formatMediumNumber(long value) {
        return mediumNumberFormat.format(value);
    }

    /**
     * Format like ###000000
     * @param value to format
     * @return formatted value
     */
    public static String formatBigNumber(long value) {
        return bigCountFormat.format(value);
    }
}


/*
 Revision history:
 $Log: FormatHelper.java,v $
 Revision 1.1  2003/03/05 18:42:33  billhorsman
 big refactor of prototyping and house keeping to
 drastically reduce the number of threads when using
 many pools

 */