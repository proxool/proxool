/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.monitor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.logicalcobwebs.proxool.ConnectionInfoIF;
import org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF;
import org.logicalcobwebs.proxool.ProxoolException;
import org.logicalcobwebs.proxool.ProxoolFacade;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.Color;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

/**
 * Use this to monitor each pool within Proxool. It acts like a normal
 * servlet., so just configure it within your web app as you see fit.
 * For example, within web.xml:
 *
 * <pre>
 *   &lt;servlet&gt;
 *       &lt;servlet-name&gt;Monitor&lt;/servlet-name&gt;
 *       &lt;servlet-class&gt;org.logicalcobwebs.proxool.monitor.MonitorServlet&lt;/servlet-class&gt;
 *   &lt;/servlet&gt;
 *
 *   &lt;servlet-mapping&gt;
 *       &lt;servlet-name&gt;Monitor&lt;/servlet-name&gt;
 *       &lt;url-pattern&gt;/monitor&lt;/url-pattern&gt;
 *   &lt;/servlet-mapping&gt;
 * </pre>
 *
 * @version $Revision: 1.5 $, $Date: 2003/02/05 15:06:16 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.7
 */
public class MonitorServlet extends HttpServlet {

    private static final Log LOG = LogFactory.getLog(MonitorServlet.class);

    private int width = 300;

    private int height = 5;

    protected static final String ACTION_LIST = "list";
    private static final String ACTION_STATS = "stats";
    protected static final String ACTION_CHART = "chart";
    protected static final String TYPE = "type";
    protected static final String TYPE_CONNECTIONS = "1";
    protected static final String TYPE_ACTIVITY_LEVEL = "2";
    private static final String STYLE_CAPTION = "text-align: right; color: #333333;";
    private static final String STYLE_DATA = "background: white;";
    private static final String STYLE_NO_DATA = "color: #666666;";
    private static final int DATE_OFFSET = 3600000;
    private static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("DD-MMM-yyyy HH:mm:ss");
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");
    private static final Color COLOR_ACTIVE = Color.red;
    private static final Color COLOR_AVAILABLE = Color.green;
    private static final Color COLOR_SPARE = Color.decode("#eeeeee");
    private static final String LEVEL = "level";
    private static final String LEVEL_MORE = "more";
    private static final String LEVEL_LESS = "less";
    private static final String ACTION = "action";
    private static final String ALIAS = "alias";

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setHeader("Pragma", "no-cache");
        String link = request.getRequestURI();

        // Check the action, and default to stats
        String action = request.getParameter(ACTION);
        if (action == null) {
            action = ACTION_STATS;
        }
        String level = request.getParameter(LEVEL);

        // Check the alias and if not defined and there is only one
        // then use that. Otherwise show the list.
        String alias = request.getParameter(ALIAS);
        String[] aliases = ProxoolFacade.getAliases();
        if (alias == null) {
            if (aliases.length == 1) {
                alias = aliases[0];
            } else {
                action = ACTION_LIST;
            }
        }

        // Check we can find the pool. If not, show the list
        if (alias != null) {
            try {
                ProxoolFacade.getConnectionPoolDefinition(alias);
            } catch (ProxoolException e) {
                action = ACTION_LIST;
            }
        }


        try {
            if (action.equals(ACTION_LIST)) {
                response.setContentType("text/html");
                openHtml(response.getOutputStream());
                doList(response.getOutputStream(), alias, link, level);
                closeHtml(response.getOutputStream());
            } else if (action.equals(ACTION_STATS)) {
                response.setContentType("text/html");
                doStats(response.getOutputStream(), alias, link, level);
            } else {
                LOG.error("Unrecognised action '" + action + "'");
            }
        } catch (ProxoolException e) {
            LOG.error("Problem", e);
        }


    }

    private void doStats(ServletOutputStream out, String alias, String link, String level) throws ProxoolException, IOException {
        openHtml(out);
        doList(out, alias, link, level);
        doDefinition(out, alias, link);
        doSnapshot(out, alias, link, level);
        doStatistics(out, alias, link);
        closeHtml(out);
    }

    private void doStatistics(ServletOutputStream out, String alias, String link) throws ProxoolException, IOException {
        StatisticsIF[] statisticsArray = ProxoolFacade.getStatistics(alias);
        SnapshotIF snapshot = ProxoolFacade.getSnapshot(alias, false);
        ConnectionPoolDefinitionIF cpd = ProxoolFacade.getConnectionPoolDefinition(alias);

        for (int i = 0; i < statisticsArray.length; i++) {
            StatisticsIF statistics = statisticsArray[i];
            out.print("<b>Statistics</b> from ");
            out.print(TIME_FORMAT.format(statistics.getStartDate()));
            out.print(" to ");
            out.print(TIME_FORMAT.format(statistics.getStopDate()));

            openTable(out);

            // Served
            printDefintionEntry(out, "Served", statistics.getServedCount() + " (" + DECIMAL_FORMAT.format(statistics.getServedPerSecond()) + "/s)");

            // Refused
            printDefintionEntry(out, "Refused", statistics.getRefusedCount() + " (" + DECIMAL_FORMAT.format(statistics.getRefusedPerSecond()) + "/s)");

            // averageActiveTime
            printDefintionEntry(out, "Average active time", DECIMAL_FORMAT.format(statistics.getAverageActiveTime()) + "s");

            // activityLevel
            StringBuffer activityLevelBuffer = new StringBuffer();
            int activityLevel = (int) (100 * statistics.getAverageActiveCount() / cpd.getMaximumConnectionCount());
            activityLevelBuffer.append(activityLevel);
            activityLevelBuffer.append("%<br/>");
            String[] colours = {"0000ff", "eeeeee"};
            int[] lengths = {activityLevel, 100 - activityLevel};
            drawBarChart(activityLevelBuffer, colours, lengths);
            printDefintionEntry(out, "Activity level", activityLevelBuffer.toString());

            closeTable(out);
        }
    }

    private void drawBarChart(StringBuffer out, String[] colours, int[] lengths) throws IOException {
        out.append("<table style=\"margin: 8px;\" width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\"><tr>");

        // Calculate total length
        int totalLength = 0;
        for (int i = 0; i < colours.length; i++) {
            totalLength += lengths[i];
        }

        // Draw segments
        for (int j = 0; j < colours.length; j++) {
            String colour = colours[j];
            int length = lengths[j];
            if (length > 0) {
                out.append("<td style=\"font-size: 50%;\" bgcolor=\"#");
                out.append(colour);
                out.append("\" width=\"");
                out.append(100 * length / totalLength);
                out.append("%\">&nbsp;</td>");
            }
        }
        out.append("</tr></table>");
    }

    private void doDefinition(ServletOutputStream out, String alias, String link) throws ProxoolException, IOException {
        ConnectionPoolDefinitionIF cpd = ProxoolFacade.getConnectionPoolDefinition(alias);

        out.print("<b>Defintition</b> for ");
        out.println(alias);
        openTable(out);

        // url
        printDefintionEntry(out, "URL", cpd.getUrl());

        // driver
        printDefintionEntry(out, "Driver", cpd.getDriver());

        // minimumConnectionCount and maximumConnectionCount
        printDefintionEntry(out, "Connections", cpd.getMinimumConnectionCount() + " (min), " + cpd.getMaximumConnectionCount() + " (max)");

        // prototypeCount
        printDefintionEntry(out, "Prototyping", cpd.getPrototypeCount() > 0 ? String.valueOf(cpd.getPrototypeCount()) : null);

        // maximumConnectionLifetime
        printDefintionEntry(out, "Connection Lifetime", TIME_FORMAT.format(new Date(cpd.getMaximumConnectionLifetime() - DATE_OFFSET)));

        // maximumActiveTime
        printDefintionEntry(out, "Maximum active time", TIME_FORMAT.format(new Date(cpd.getMaximumActiveTime() - DATE_OFFSET)));
        printDefintionEntry(out, "House keeping sleep time", (cpd.getHouseKeepingSleepTime() / 1000) + "s");

        // houseKeepingTestSql
        printDefintionEntry(out, "House keeping test SQL", cpd.getHouseKeepingTestSql());

        // fatalSqlExceptions
        String fatalSqlExceptions = null;
        if (cpd.getFatalSqlExceptions() != null && cpd.getFatalSqlExceptions().size() > 0) {
            StringBuffer fatalSqlExceptionsBuffer = new StringBuffer();
            Iterator i = cpd.getFatalSqlExceptions().iterator();
            while (i.hasNext()) {
                String s = (String) i.next();
                fatalSqlExceptionsBuffer.append(s);
                fatalSqlExceptionsBuffer.append(i.hasNext() ? ", " : "");
            }
            fatalSqlExceptions = fatalSqlExceptionsBuffer.toString();
        }
        printDefintionEntry(out, "Fatal SQL exceptions", fatalSqlExceptions);

        // statistics
        printDefintionEntry(out, "Statistics", cpd.getStatistics());

        closeTable(out);

    }

    private void doSnapshot(ServletOutputStream out, String alias, String link, String level) throws IOException, ProxoolException {
        boolean detail = (level != null && level.equals(LEVEL_MORE));
        SnapshotIF snapshot = ProxoolFacade.getSnapshot(alias, detail);
        ConnectionPoolDefinitionIF cpd = ProxoolFacade.getConnectionPoolDefinition(alias);

        if (snapshot != null) {

            out.print("<b>Snapshot</b> at ");
            out.println(TIME_FORMAT.format(snapshot.getSnapshotDate()));
            openTable(out);

            // dateStarted
            printDefintionEntry(out, "Start date", DATE_FORMAT.format(snapshot.getDateStarted()));

            // connections
            StringBuffer connectionsBuffer = new StringBuffer();
            connectionsBuffer.append(snapshot.getActiveConnectionCount());
            connectionsBuffer.append(" (<font color=\"");
            connectionsBuffer.append(COLOR_ACTIVE);
            connectionsBuffer.append("\">active</font>), ");
            connectionsBuffer.append(snapshot.getAvailableConnectionCount());
            connectionsBuffer.append(" (<font color=\"");
            connectionsBuffer.append(COLOR_AVAILABLE);
            connectionsBuffer.append("\">available</font>), ");
            if (snapshot.getOfflineConnectionCount() > 0) {
                connectionsBuffer.append(snapshot.getOfflineConnectionCount());
                connectionsBuffer.append(" (offline), ");
            }
            connectionsBuffer.append(snapshot.getMaximumConnectionCount());
            connectionsBuffer.append(" (<font color=\"");
            connectionsBuffer.append(COLOR_SPARE);
            connectionsBuffer.append("\">max</font>)<br/>");
            String[] colours = {"ff0000", "00ff00", "eeeeee"};
            int[] lengths = {snapshot.getActiveConnectionCount(), snapshot.getAvailableConnectionCount(),
                             snapshot.getMaximumConnectionCount() - snapshot.getActiveConnectionCount() - snapshot.getAvailableConnectionCount()};
            drawBarChart(connectionsBuffer, colours, lengths);
            printDefintionEntry(out, "Connections", connectionsBuffer.toString());

            // servedCount
            printDefintionEntry(out, "Served", String.valueOf(snapshot.getServedCount()));

            // refusedCount
            printDefintionEntry(out, "Refused", String.valueOf(snapshot.getRefusedCount()));

            if (!detail) {
                out.println("    <tr>");
                out.print("<td colspan=\"2\" align=\"right\"><a href=\"");
                out.print(link);
                out.print("?");
                out.print(ALIAS);
                out.print("=");
                out.print(alias);
                out.print("&");
                out.print(LEVEL);
                out.print("=");
                out.print(LEVEL_MORE);
                out.println("\">more information</a></td>");
                out.println("    </tr>");
            } else {

                out.println("    <tr>");
                out.print("      <td width=\"200\" valign=\"top\" style=\"" + STYLE_CAPTION + "\">");
                out.print("Details");
                out.println("</td>");
                out.print("      <td style=\"" + STYLE_NO_DATA + "\">");

                doSnapshotDetails(out, snapshot, link);

                out.println("</td>");
                out.println("    </tr>");

                out.println("    <tr>");
                out.print("<td colspan=\"2\" align=\"right\"><a href=\"");
                out.print(link);
                out.print("?");
                out.print(ALIAS);
                out.print("=");
                out.print(alias);
                out.print("&");
                out.print(LEVEL);
                out.print("=");
                out.print(LEVEL_LESS);
                out.println("\">less information</a></td>");
                out.println("    </tr>");
            }

            closeTable(out);
        }
    }

    private void doSnapshotDetails(ServletOutputStream out, SnapshotIF snapshot, String link) throws IOException {

        if (snapshot.getConnectionInfos() != null && snapshot.getConnectionInfos().length > 0) {
            out.println("<table cellpadding=\"2\" border=\"0\">");
            out.println("  <tbody>");

            out.print("<tr>");
            out.print("<td style=\"font-size: 90%\">#</td>");
            out.print("<td style=\"font-size: 90%\" align=\"center\">born</td>");
            out.print("<td style=\"font-size: 90%\" align=\"center\">last<br>start</td>");
            out.print("<td style=\"font-size: 90%\" align=\"center\">lap<br>(ms)</td>");
            out.print("<td style=\"font-size: 90%\" width=\"90%\">&nbsp;thread</td>");
            out.print("</tr>");

            ConnectionInfoIF[] connectionInfos = snapshot.getConnectionInfos();
            for (int i = 0; i < connectionInfos.length; i++) {
                ConnectionInfoIF connectionInfo = connectionInfos[i];

                if (connectionInfo.getStatus() != ConnectionInfoIF.STATUS_NULL) {

                    out.print("<tr>");

                    // id
                    out.print("<td bgcolor=\"#");
                    if (connectionInfo.getStatus() == ConnectionInfoIF.STATUS_ACTIVE) {
                        out.print("ffcccc");
                    } else if (connectionInfo.getStatus() == ConnectionInfoIF.STATUS_AVAILABLE) {
                        out.print("ccffcc");
                    } else if (connectionInfo.getStatus() == ConnectionInfoIF.STATUS_OFFLINE) {
                        out.print("ccccff");
                    }
                    out.print("\">");
                    out.print(connectionInfo.getId());
                    out.print("</td>");

                    // birth
                    out.print("<td>&nbsp;");
                    out.print(TIME_FORMAT.format(connectionInfo.getBirthDate()));
                    out.print("</td>");

                    // started
                    out.print("<td>&nbsp;");
                    out.print(connectionInfo.getTimeLastStartActive() > 0 ? TIME_FORMAT.format(new Date(connectionInfo.getTimeLastStartActive())) : "-");
                    out.print("</td>");

                    // active
                    out.print("<td align=\"right\">");
                    if (connectionInfo.getTimeLastStopActive() > 0) {
                        out.print((int) (connectionInfo.getTimeLastStopActive() - connectionInfo.getTimeLastStartActive()));
                    } else if (connectionInfo.getTimeLastStartActive() > 0) {
                        out.print("<font color=\"red\">");
                        out.print((int) (snapshot.getSnapshotDate().getTime() - connectionInfo.getTimeLastStartActive()));
                        out.print("</font>");
                    } else {
                        out.print("&nbsp;");
                    }
                    out.print("&nbsp;&nbsp;</td>");

                    // requester
                    out.print("<td>&nbsp;");
                    out.print(connectionInfo.getRequester() != null ? connectionInfo.getRequester() : "-");
                    out.print("</td>");

                    out.println("</tr>");
                }
            }
            out.println("  </tbody>");
            out.println("</table>");

        } else {
            out.println("No connections yet");
        }
    }

    private void openHtml(ServletOutputStream out) throws IOException {
        out.println("<html><header><title>Proxool Monitor</title></header><body BGCOLOR=\"#eeeeee\">");
    }

    private void closeHtml(ServletOutputStream out) throws IOException {
        out.println("</body></html>");
    }

    private void openTable(ServletOutputStream out) throws IOException {
        out.println("<table width=\"550\" cellpadding=\"2\" cellspacing=\"2\" border=\"0\" bgcolor=\"#EEEEEE\" style=\"border: 1px solid black\">");
        out.println("  <tbody>");
    }

    private void closeTable(ServletOutputStream out) throws IOException {
        out.println("  </tbody>");
        out.println("</table>");
        out.println("<br/>");
    }


    private void printDefintionEntry(ServletOutputStream out, String name, String value) throws IOException {
        out.println("    <tr>");
        out.print("      <td width=\"200\" valign=\"top\" style=\"" + STYLE_CAPTION + "\">");
        out.print(name);
        out.println("</td>");
        if (value != null) {
            out.print("      <td style=\"" + STYLE_DATA + "\">");
            out.print(value);
        } else {
            out.print("      <td style=\"" + STYLE_NO_DATA + "\">off");
        }
        out.print("</td>");
        out.println("    </tr>");
    }

    private void doList(ServletOutputStream out, String alias, String link, String level) throws IOException, ProxoolException {

        out.print("<b>Pools</b>");
        openTable(out);

        String[] aliases = ProxoolFacade.getAliases();
        for (int i = 0; i < aliases.length; i++) {
            String a = aliases[i];
            String style = "";
            if (a.equals(alias)) {
                style = "background: white;";
            }
            ConnectionPoolDefinitionIF cpd = ProxoolFacade.getConnectionPoolDefinition(a);
            out.println("    <tr style=\"" + style + "\">");

            out.print("      <td width=\"200\" style=\"" + STYLE_CAPTION + "\">");
            out.print(a.equals(alias) ? ">" : "&nbsp;");
            out.println("</td>");

            out.print("      <td><a href=\"" + link + "?" + ALIAS + "=" + a + "&" + LEVEL + "=" + level + "\">");
            out.print(a);
            out.println("</a> -> ");
            out.print(cpd.getUrl());
            out.println("</td>");
            out.println("    </tr>");
        }

        closeTable(out);

    }
}


/*
 Revision history:
 $Log: MonitorServlet.java,v $
 Revision 1.5  2003/02/05 15:06:16  billhorsman
 removed dependency on JDK1.4 imaging.

 Revision 1.4  2003/01/31 16:53:21  billhorsman
 checkstyle

 Revision 1.3  2003/01/31 16:38:52  billhorsman
 doc (and removing public modifier for classes where possible)

 Revision 1.2  2003/01/31 11:35:57  billhorsman
 improvements to servlet (including connection details)

 Revision 1.1  2003/01/31 00:38:22  billhorsman
 *** empty log message ***

 */