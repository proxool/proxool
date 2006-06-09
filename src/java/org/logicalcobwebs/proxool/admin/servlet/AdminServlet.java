/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.admin.servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.logicalcobwebs.proxool.*;
import org.logicalcobwebs.proxool.admin.SnapshotIF;
import org.logicalcobwebs.proxool.admin.StatisticsIF;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;

/**
 * Use this to admin each pool within Proxool. It acts like a normal
 * servlet., so just configure it within your web app as you see fit.
 * For example, within web.xml:
 * <pre>
 *   &lt;servlet&gt;
 *       &lt;servlet-name&gt;Admin&lt;/servlet-name&gt;
 *       &lt;servlet-class&gt;org.logicalcobwebs.proxool.admin.servlet.AdminServlet&lt;/servlet-class&gt;
 *       &lt;init-param&gt;
 *            &lt;param-name&gt;output&lt;/param-name&gt;
 *            &lt;param-value&gt;full|simple&lt;/param-value&gt;
 *        &lt;/init-param&gt;
 *        &lt;init-param&gt;
 *            &lt;param-name&gt;cssFile&lt;/param-name&gt;
 *            &lt;param-value&gt;/my_path/my.css&lt;/param-value&gt;
 *        &lt;/init-param&gt;
 *   &lt;/servlet&gt;
 *   &lt;servlet-mapping&gt;
 *       &lt;servlet-name&gt;Admin&lt;/servlet-name&gt;
 *       &lt;url-pattern&gt;/proxool&lt;/url-pattern&gt;
 *   &lt;/servlet-mapping&gt;
 * </pre>
 *
 * Options:
 * <ul>
 *   <li>output: full|simple. "full" means with HTML header and body tags "simple" means
 *   just the HTML. Choose "simple" if you are including this servlet within your own
 *   web page. Note that if you choose simple output then you're probably going to want
 *   to consider supplying some CSS to make it look nice.</li>
 *   <li>cssFile: If you choose full output (see above) then some CSS is included, inline,
 *   in the HTML header. If you specify a URL here then that file is also linked to. It is
 *   linked after the inline CSS so you only have to override thos styles you want to be
 *   different.</li>
 * </ul>
 *
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @version $Revision: 1.14 $, $Date: 2006/06/09 17:32:54 $
 * @since Proxool 0.7
 */
public class AdminServlet extends HttpServlet {

    private static final Log LOG = LogFactory.getLog(AdminServlet.class);

    /**
     * The CSS class for a connection in different states:
     * <ul>
     *   <li>null</li>
     *   <li>available</li>
     *   <li>active</li>
     *   <li>offline</li>
     * </ul>
     */
    private static final String[] STATUS_CLASSES = {"null", "available", "active", "offline"};

    /**
     * OUtput full HTML including &lt;HTML&gt;, &lt;HEAD&gt; and &lt;BODY&gt; tags.
     * @see #output
     * @see AdminServlet configuration
     */
    public static final String OUTPUT_FULL = "full";

    /**
     * OUtput simple HTML <em>excluding</em> &lt;HTML&gt;, &lt;HEAD&gt; and &lt;BODY&gt; tags.
     * @see #output
     * @see AdminServlet configuration
     */
    public static final String OUTPUT_SIMPLE = "simple";

    /**
     * Either {@link #OUTPUT_FULL} (default) or {@link #OUTPUT_SIMPLE}
     * @see AdminServlet configuration
     */
    private String output;

    /**
     * A valid URLL that can be linked to to override default, inline CSS.
     * @see AdminServlet configuration
     */

    private String cssFile;

    /**
     * Used as part of the CSS class
     */
    private static final String STATISTIC = "statistic";

    /**
     * Used as part of the CSS class
     */
    private static final String CORE_PROPERTY = "core-property";

    /**
     * Used as part of the CSS class
     */
    private static final String STANDARD_PROPERTY = "standard-property";

    /**
     * Used as part of the CSS class
     */
    private static final String DELEGATED_PROPERTY = "delegated-property";

    /**
     * Used as part of the CSS class
     */
    private static final String SNAPSHOT = "snapshot";

    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);

        // Get output parameter. Default to OUTPUT_FULL.
        output = servletConfig.getInitParameter("output");
        if (output != null) {
            if (output.equalsIgnoreCase(OUTPUT_FULL)) {
                output = OUTPUT_FULL;
            } else if (output.equalsIgnoreCase(OUTPUT_SIMPLE)) {
                output = OUTPUT_SIMPLE;
            } else {
                LOG.warn("Unrecognised output parameter for " + this.getClass().getName() + ". Expected: " + OUTPUT_FULL + " or " + OUTPUT_SIMPLE);
                output = null;
            }
        }
        if (output == null) {
            output = OUTPUT_FULL;
        }

        cssFile = servletConfig.getInitParameter("cssFile");

    }

    /**
     * HH:mm:ss
     * @see #formatMilliseconds
     */
    private static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");

    /**
     * dd-MMM-yyyy HH:mm:ss
     */
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");

    private static final String DETAIL = "detail";
    private static final String DETAIL_MORE = "more";
    private static final String DETAIL_LESS = "less";

    /**
     * The request parameter name that defines:
     * <ol>
     *   <li>{@link #TAB_DEFINITION} (default)</li>
     *   <li>{@link #TAB_SNAPSHOT}</li>
     *   <li>{@link #TAB_STATISTICS}</li>
     *  </ol>
     */
    private static final String TAB = "tab";

    /**
     * @see #TAB
     */
    private static final String TAB_DEFINITION = "definition";

    /**
     * @see #TAB
     */
    private static final String TAB_SNAPSHOT = "snapshot";

    /**
     * @see #TAB
     */
    private static final String TAB_STATISTICS = "statistics";

    /**
     * The request parameter name that defines the pool
     */
    private static final String ALIAS = "alias";

    /**
     * If we are drilling down into a connection (on the {@link #TAB_SNAPSHOT snapshot} tab then
     * this points to the {@link org.logicalcobwebs.proxool.ProxyConnection#getId() ID} we are
     * getting detailed information for.
     */
    private static final String CONNECTION_ID = "id";

    /**
     * Delegate to {@link #doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    /**
     * Show the details for a pool.
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setHeader("Pragma", "no-cache");
        String link = request.getRequestURI();

        // Check the alias and if not defined and there is only one
        // then use that.
        String alias = request.getParameter(ALIAS);
        // Check we can find the pool.
        ConnectionPoolDefinitionIF def = null;
        if (alias != null) {
            try {
                def = ProxoolFacade.getConnectionPoolDefinition(alias);
            } catch (ProxoolException e) {
                alias = null;
            }
        }
        String[] aliases = ProxoolFacade.getAliases();
        if (alias == null) {
            if (aliases.length > 0) {
                alias = aliases[0];
            }
        }
        if (def == null && alias != null) {
            try {
                def = ProxoolFacade.getConnectionPoolDefinition(alias);
            } catch (ProxoolException e) {
                throw new ServletException("Couldn't find pool with alias " + alias);
            }
        }

        String tab = request.getParameter(TAB);
        if (tab == null) {
            tab = TAB_DEFINITION;
        }

        // If we are showing the snapshot, are we showing it in detail or not?
        String snapshotDetail = request.getParameter(DETAIL);

        // If we are showing the snapshot, are we drilling down into a connection?
        String snapshotConnectionId = request.getParameter(CONNECTION_ID);

        try {
            if (output.equals(OUTPUT_FULL)) {
                response.setContentType("text/html");
                openHtml(response.getOutputStream());
            }
            response.getOutputStream().println("<div class=\"version\">Proxool " + Version.getVersion() + "</div>");
            doList(response.getOutputStream(), alias, tab, link);
            // Skip everything if there aren't any pools
            if (aliases != null && aliases.length > 0) {
                StatisticsIF[] statisticsArray = ProxoolFacade.getStatistics(alias);
                final boolean statisticsAvailable = (statisticsArray != null && statisticsArray.length > 0);
                final boolean statisticsComingSoon = def.getStatistics() != null;
                // We can't be on the statistics tab if there are no statistics
                if (!statisticsComingSoon && tab.equals(TAB_STATISTICS)) {
                    tab = TAB_DEFINITION;
                }
                doTabs(response.getOutputStream(), alias, link, tab, statisticsAvailable, statisticsComingSoon);
                if (tab.equals(TAB_DEFINITION)) {
                    doDefinition(response.getOutputStream(), def);
                } else if (tab.equals(TAB_SNAPSHOT)) {
                    doSnapshot(response.getOutputStream(), def, link, snapshotDetail, snapshotConnectionId);
                } else if (tab.equals(TAB_STATISTICS)) {
                    doStatistics(response.getOutputStream(), statisticsArray, def);
                } else {
                    throw new ServletException("Unrecognised tab '" + tab + "'");
                }
            }
        } catch (ProxoolException e) {
            throw new ServletException("Problem serving Proxool Admin", e);
        }

        if (output.equals(OUTPUT_FULL)) {
            closeHtml(response.getOutputStream());
        }

    }

    /**
     * Output the tabs that we are showing at the top of the page
     * @param out where to write the HTNL to
     * @param alias the current pool
     * @param link the URL to get back to this servlet
     * @param tab the active tab
     * @param statisticsAvailable whether statistics are available (true if configured and ready)
     * @param statisticsComingSoon whether statistics will be available (true if configured but not ready yet)
     */
    private void doTabs(ServletOutputStream out, String alias, String link, String tab, boolean statisticsAvailable, boolean statisticsComingSoon) throws IOException {
        out.println("<ul>");
        out.println("<li class=\"" + (tab.equals(TAB_DEFINITION) ? "active" : "inactive") + "\"><a class=\"quiet\" href=\"" + link + "?alias=" + alias + "&tab=" + TAB_DEFINITION + "\">Definition</a></li>");
        out.println("<li class=\"" + (tab.equals(TAB_SNAPSHOT) ? "active" : "inactive") + "\"><a class=\"quiet\" href=\"" + link + "?alias=" + alias + "&tab=" + TAB_SNAPSHOT + "\">Snapshot</a></li>");
        if (statisticsAvailable) {
            out.println("<li class=\"" + (tab.equals(TAB_STATISTICS) ? "active" : "inactive") + "\"><a class=\"quiet\" href=\"" + link + "?alias=" + alias + "&tab=" + TAB_STATISTICS + "\">Statistics</a></li>");
        } else if (statisticsComingSoon) {
            out.println("<li class=\"disabled\">Statistics</li>");
        }
        out.println("</ul>");
    }

    /**
     * Output the statistics. If there are more than one set of statistics then show them all.
     * @param out where to write HTML to
     * @param statisticsArray the statistics we have ready to see
     * @param cpd defines the connection
     */
    private void doStatistics(ServletOutputStream out, StatisticsIF[] statisticsArray, ConnectionPoolDefinitionIF cpd) throws IOException {

        for (int i = 0; i < statisticsArray.length; i++) {
            StatisticsIF statistics = statisticsArray[i];

            openDataTable(out);

            printDefinitionEntry(out, ProxoolConstants.ALIAS, cpd.getAlias(), CORE_PROPERTY);

            // Period
            printDefinitionEntry(out, "Period", TIME_FORMAT.format(statistics.getStartDate()) + " to " + TIME_FORMAT.format(statistics.getStopDate()), STATISTIC);

            // Served
            printDefinitionEntry(out, "Served", statistics.getServedCount() + " (" + DECIMAL_FORMAT.format(statistics.getServedPerSecond()) + "/s)", STATISTIC);

            // Refused
            printDefinitionEntry(out, "Refused", statistics.getRefusedCount() + " (" + DECIMAL_FORMAT.format(statistics.getRefusedPerSecond()) + "/s)", STATISTIC);

            // averageActiveTime
            printDefinitionEntry(out, "Average active time", DECIMAL_FORMAT.format(statistics.getAverageActiveTime() / 1000) + "s", STATISTIC);

            // activityLevel
            StringBuffer activityLevelBuffer = new StringBuffer();
            int activityLevel = (int) (100 * statistics.getAverageActiveCount() / cpd.getMaximumConnectionCount());
            activityLevelBuffer.append(activityLevel);
            activityLevelBuffer.append("%<br/>");
            String[] colours = {"0000ff", "eeeeee"};
            int[] lengths = {activityLevel, 100 - activityLevel};
            drawBarChart(activityLevelBuffer, colours, lengths);
            printDefinitionEntry(out, "Activity level", activityLevelBuffer.toString(), STATISTIC);

            closeTable(out);
        }
    }

    /**
     * We can draw a bar chart simply enough. The two arrays passed as parameters must be of equal length
     * @param out where to write the HTML
     * @param colours the colur (CSS valid string) for each segment
     * @param lengths the length of each segment. Can be any size since the chart just takes up as much room
     * as possible as uses the relative length of each segment.
     */
    private void drawBarChart(StringBuffer out, String[] colours, int[] lengths) {
        out.append("<table style=\"margin: 8px; font-size: 50%;\" width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\"><tr>");

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
                out.append("<td style=\"background-color: #");
                out.append(colour);
                out.append("\" width=\"");
                out.append(100 * length / totalLength);
                out.append("%\">&nbsp;</td>");
            }
        }
        out.append("</tr></table>");
    }

    /**
     * Output the {@link ConnectionPoolDefinitionIF definition}
     * @param out where to write the HTML
     * @param cpd the definition
     */
    private void doDefinition(ServletOutputStream out, ConnectionPoolDefinitionIF cpd) throws IOException {
        openDataTable(out);

        /*
            TODO: it would be nice to have meta-data in the definition so that this is much easier.
         */
        printDefinitionEntry(out, ProxoolConstants.ALIAS, cpd.getAlias(), CORE_PROPERTY);
        printDefinitionEntry(out, ProxoolConstants.DRIVER_URL, cpd.getUrl(), CORE_PROPERTY);
        printDefinitionEntry(out, ProxoolConstants.DRIVER_CLASS, cpd.getDriver(), CORE_PROPERTY);
        printDefinitionEntry(out, ProxoolConstants.MINIMUM_CONNECTION_COUNT, String.valueOf(cpd.getMinimumConnectionCount()), STANDARD_PROPERTY);
        printDefinitionEntry(out, ProxoolConstants.MAXIMUM_CONNECTION_COUNT, String.valueOf(cpd.getMaximumConnectionCount()), STANDARD_PROPERTY);
        printDefinitionEntry(out, ProxoolConstants.PROTOTYPE_COUNT, cpd.getPrototypeCount() > 0 ? String.valueOf(cpd.getPrototypeCount()) : null, STANDARD_PROPERTY);
        printDefinitionEntry(out, ProxoolConstants.SIMULTANEOUS_BUILD_THROTTLE, String.valueOf(cpd.getSimultaneousBuildThrottle()), STANDARD_PROPERTY);
        printDefinitionEntry(out, ProxoolConstants.MAXIMUM_CONNECTION_LIFETIME, formatMilliseconds(cpd.getMaximumConnectionLifetime()), STANDARD_PROPERTY);
        printDefinitionEntry(out, ProxoolConstants.MAXIMUM_ACTIVE_TIME, formatMilliseconds(cpd.getMaximumActiveTime()), STANDARD_PROPERTY);
        printDefinitionEntry(out, ProxoolConstants.HOUSE_KEEPING_SLEEP_TIME, (cpd.getHouseKeepingSleepTime() / 1000) + "s", STANDARD_PROPERTY);
        printDefinitionEntry(out, ProxoolConstants.HOUSE_KEEPING_TEST_SQL, cpd.getHouseKeepingTestSql(), STANDARD_PROPERTY);
        printDefinitionEntry(out, ProxoolConstants.TEST_BEFORE_USE, String.valueOf(cpd.isTestBeforeUse()), STANDARD_PROPERTY);
        printDefinitionEntry(out, ProxoolConstants.TEST_AFTER_USE, String.valueOf(cpd.isTestAfterUse()), STANDARD_PROPERTY);
        printDefinitionEntry(out, ProxoolConstants.RECENTLY_STARTED_THRESHOLD, formatMilliseconds(cpd.getRecentlyStartedThreshold()), STANDARD_PROPERTY);
        printDefinitionEntry(out, ProxoolConstants.OVERLOAD_WITHOUT_REFUSAL_LIFETIME, formatMilliseconds(cpd.getOverloadWithoutRefusalLifetime()), STANDARD_PROPERTY);
        printDefinitionEntry(out, ProxoolConstants.INJECTABLE_CONNECTION_INTERFACE_NAME, String.valueOf(cpd.getInjectableConnectionInterface()), STANDARD_PROPERTY);
        printDefinitionEntry(out, ProxoolConstants.INJECTABLE_STATEMENT_INTERFACE_NAME, String.valueOf(cpd.getInjectableStatementInterface()), STANDARD_PROPERTY);
        printDefinitionEntry(out, ProxoolConstants.INJECTABLE_CALLABLE_STATEMENT_INTERFACE_NAME, String.valueOf(cpd.getInjectableCallableStatementInterface()), STANDARD_PROPERTY);
        printDefinitionEntry(out, ProxoolConstants.INJECTABLE_PREPARED_STATEMENT_INTERFACE_NAME, String.valueOf(cpd.getInjectablePreparedStatementInterface()), STANDARD_PROPERTY);

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
        printDefinitionEntry(out, ProxoolConstants.FATAL_SQL_EXCEPTION, fatalSqlExceptions, STANDARD_PROPERTY);
        printDefinitionEntry(out, ProxoolConstants.FATAL_SQL_EXCEPTION_WRAPPER_CLASS, cpd.getFatalSqlExceptionWrapper(), STANDARD_PROPERTY);
        printDefinitionEntry(out, ProxoolConstants.STATISTICS, cpd.getStatistics(), STANDARD_PROPERTY);
        printDefinitionEntry(out, ProxoolConstants.STATISTICS_LOG_LEVEL, cpd.getStatisticsLogLevel(), STANDARD_PROPERTY);
        printDefinitionEntry(out, ProxoolConstants.VERBOSE, String.valueOf(cpd.isVerbose()), STANDARD_PROPERTY);
        printDefinitionEntry(out, ProxoolConstants.TRACE, String.valueOf(cpd.isTrace()), STANDARD_PROPERTY);
        // Now all the properties that are forwarded to the delegate driver.
        Properties p = cpd.getDelegateProperties();
        Iterator i = p.keySet().iterator();
        while (i.hasNext()) {
            String name = (String) i.next();
            String value = p.getProperty(name);
            // Better hide the password!
            if (name.toLowerCase().indexOf("password") > -1 || name.toLowerCase().indexOf("passwd") > -1) {
                value = "******";
            }
            printDefinitionEntry(out, name + " (delegated)", value, DELEGATED_PROPERTY);
        }

        closeTable(out);

    }

    /**
     * Output a {@link SnapshotIF snapshot} of the pool.
     * @param out where to write the HTML
     * @param cpd defines the pool
     * @param link the URL back to this servlet
     * @param level either {@link #DETAIL_LESS} or {@link #DETAIL_MORE}
     * @param connectionId the connection we want to drill into (optional)
     */
    private void doSnapshot(ServletOutputStream out, ConnectionPoolDefinitionIF cpd, String link, String level, String connectionId) throws IOException, ProxoolException {
        boolean detail = (level != null && level.equals(DETAIL_MORE));
        SnapshotIF snapshot = ProxoolFacade.getSnapshot(cpd.getAlias(), detail);

        if (snapshot != null) {

            openDataTable(out);

            printDefinitionEntry(out, ProxoolConstants.ALIAS, cpd.getAlias(), CORE_PROPERTY);

            // dateStarted
            printDefinitionEntry(out, "Start date", DATE_FORMAT.format(snapshot.getDateStarted()), SNAPSHOT);

            // snapshot date
            printDefinitionEntry(out, "Snapshot", TIME_FORMAT.format(snapshot.getSnapshotDate()), SNAPSHOT);

            // connections
            StringBuffer connectionsBuffer = new StringBuffer();
            connectionsBuffer.append(snapshot.getActiveConnectionCount());
            connectionsBuffer.append(" (active), ");
            connectionsBuffer.append(snapshot.getAvailableConnectionCount());
            connectionsBuffer.append(" (available), ");
            if (snapshot.getOfflineConnectionCount() > 0) {
                connectionsBuffer.append(snapshot.getOfflineConnectionCount());
                connectionsBuffer.append(" (offline), ");
            }
            connectionsBuffer.append(snapshot.getMaximumConnectionCount());
            connectionsBuffer.append(" (max)<br/>");
            String[] colours = {"ff9999", "66cc66", "cccccc"};
            int[] lengths = {snapshot.getActiveConnectionCount(), snapshot.getAvailableConnectionCount(),
                    snapshot.getMaximumConnectionCount() - snapshot.getActiveConnectionCount() - snapshot.getAvailableConnectionCount()};
            drawBarChart(connectionsBuffer, colours, lengths);
            printDefinitionEntry(out, "Connections", connectionsBuffer.toString(), SNAPSHOT);

            // servedCount
            printDefinitionEntry(out, "Served", String.valueOf(snapshot.getServedCount()), SNAPSHOT);

            // refusedCount
            printDefinitionEntry(out, "Refused", String.valueOf(snapshot.getRefusedCount()), SNAPSHOT);

            if (!detail) {
                out.println("    <tr>");
                out.print("        <td colspan=\"2\" align=\"right\"><form action=\"" + link + "\" method=\"GET\">");
                out.print("<input type=\"hidden\" name=\"" + ALIAS + "\" value=\"" + cpd.getAlias() + "\">");
                out.print("<input type=\"hidden\" name=\"" + TAB + "\" value=\"" + TAB_SNAPSHOT + "\">");
                out.print("<input type=\"hidden\" name=\"" + DETAIL + "\" value=\"" + DETAIL_MORE + "\">");
                out.print("<input type=\"submit\" value=\"More information&gt;\">");
                out.println("</form></td>");
                out.println("    </tr>");
            } else {

                out.println("    <tr>");
                out.print("      <th width=\"200\" valign=\"top\">");
                out.print("Details:<br>(click ID to drill down)");
                out.println("</th>");
                out.print("      <td>");

                doSnapshotDetails(out, cpd, snapshot, link, connectionId);

                out.println("</td>");
                out.println("    </tr>");

                long drillDownConnectionId;
                if (connectionId != null) {
                    drillDownConnectionId = Long.valueOf(connectionId).longValue();
                    ConnectionInfoIF drillDownConnection = snapshot.getConnectionInfo(drillDownConnectionId);
                    if (drillDownConnection != null) {
                        out.println("    <tr>");
                        out.print("      <th valign=\"top\">");
                        out.print("Connection #" + connectionId);
                        out.println("</td>");
                        out.print("      <td>");

                        doDrillDownConnection(out, drillDownConnection);

                        out.println("</td>");
                        out.println("    </tr>");
                    }
                }

                out.println("    <tr>");
                out.print("        <td colspan=\"2\" align=\"right\"><form action=\"" + link + "\" method=\"GET\">");
                out.print("<input type=\"hidden\" name=\"" + ALIAS + "\" value=\"" + cpd.getAlias() + "\">");
                out.print("<input type=\"hidden\" name=\"" + TAB + "\" value=\"" + TAB_SNAPSHOT + "\">");
                out.print("<input type=\"hidden\" name=\"" + DETAIL + "\" value=\"" + DETAIL_LESS + "\">");
                out.print("<input type=\"submit\" value=\"&lt; Less information\">");
                out.println("</form></td>");
                out.println("    </tr>");
            }

            closeTable(out);
        }
    }

    /**
     * If we want a {@link #DETAIL_MORE more} detailed {@link SnapshotIF snapshot} then {@link #doSnapshot(javax.servlet.ServletOutputStream, org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF, String, String, String)}
     * calls this too
     * @param out where to write the HTML
     * @param cpd defines the pool
     * @param snapshot snapshot
     * @param link the URL back to this servlet
     * @param connectionId the connection we want to drill into (optional)
     * @param connectionId
     * @throws IOException
     */
    private void doSnapshotDetails(ServletOutputStream out, ConnectionPoolDefinitionIF cpd, SnapshotIF snapshot, String link, String connectionId) throws IOException {

        long drillDownConnectionId = 0;
        if (connectionId != null) {
            drillDownConnectionId = Long.valueOf(connectionId).longValue();
        }

        if (snapshot.getConnectionInfos() != null && snapshot.getConnectionInfos().length > 0) {
            out.println("<table cellpadding=\"2\" cellspacing=\"0\" border=\"0\">");
            out.println("  <tbody>");

            out.print("<tr>");
            out.print("<td>#</td>");
            out.print("<td align=\"center\">born</td>");
            out.print("<td align=\"center\">last<br>start</td>");
            out.print("<td align=\"center\">lap<br>(ms)</td>");
            out.print("<td>&nbsp;thread</td>");
            out.print("</tr>");

            ConnectionInfoIF[] connectionInfos = snapshot.getConnectionInfos();
            for (int i = 0; i < connectionInfos.length; i++) {
                ConnectionInfoIF connectionInfo = connectionInfos[i];

                if (connectionInfo.getStatus() != ConnectionInfoIF.STATUS_NULL) {

                    out.print("<tr>");

                    // drillDownConnectionId
                    out.print("<td style=\"background-color: #");
                    if (connectionInfo.getStatus() == ConnectionInfoIF.STATUS_ACTIVE) {
                        out.print("ffcccc");
                    } else if (connectionInfo.getStatus() == ConnectionInfoIF.STATUS_AVAILABLE) {
                        out.print("ccffcc");
                    } else if (connectionInfo.getStatus() == ConnectionInfoIF.STATUS_OFFLINE) {
                        out.print("ccccff");
                    }
                    out.print("\" style=\"");

                    if (drillDownConnectionId == connectionInfo.getId()) {
                        out.print("border: 1px solid black;");
                        out.print("\">");
                        out.print(connectionInfo.getId());
                    } else {
                        out.print("border: 1px solid transparent;");
                        out.print("\"><a href=\"");
                        out.print(link);
                        out.print("?");
                        out.print(ALIAS);
                        out.print("=");
                        out.print(cpd.getAlias());
                        out.print("&");
                        out.print(TAB);
                        out.print("=");
                        out.print(TAB_SNAPSHOT);
                        out.print("&");
                        out.print(DETAIL);
                        out.print("=");
                        out.print(DETAIL_MORE);
                        out.print("&");
                        out.print(CONNECTION_ID);
                        out.print("=");
                        out.print(connectionInfo.getId());
                        out.print("\">");
                        out.print(connectionInfo.getId());
                        out.print("</a>");
                    }
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
                    out.print("<td align=\"right\" class=\"");
                    out.print(getStatusClass(connectionInfo));
                    out.print("\">");
                    String active = "&nbsp;";
                    if (connectionInfo.getTimeLastStopActive() > 0) {
                        active = String.valueOf((int) (connectionInfo.getTimeLastStopActive() - connectionInfo.getTimeLastStartActive()));
                    } else if (connectionInfo.getTimeLastStartActive() > 0) {
                        active = String.valueOf((int) (snapshot.getSnapshotDate().getTime() - connectionInfo.getTimeLastStartActive()));
                    }
                    out.print(active);
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

    /**
     * What CSS class to use for a particular connection.
     * @param info so we know the {@link org.logicalcobwebs.proxool.ConnectionInfoIF#getStatus()} status
     * @return the CSS class
     * @see #STATUS_CLASSES
     */
    private static String getStatusClass(ConnectionInfoIF info) {
        try {
            return STATUS_CLASSES[info.getStatus()];
        } catch (ArrayIndexOutOfBoundsException e) {
            LOG.warn("Unknown status: " + info.getStatus());
            return "unknown-" + info.getStatus();
        }
    }

    private void doDrillDownConnection(ServletOutputStream out, ConnectionInfoIF drillDownConnection) throws IOException {

        // sql calls
        String[] sqlCalls = drillDownConnection.getSqlCalls();
        for (int i = 0; sqlCalls != null && i < sqlCalls.length; i++) {
            String sqlCall = sqlCalls[i];
            out.print("<div class=\"drill-down\">");
            out.print("sql = ");
            out.print(sqlCall);
            out.print("</div>");
        }

        // proxy
        out.print("<div class=\"drill-down\">");
        out.print("proxy = ");
        out.print(drillDownConnection.getProxyHashcode());
        out.print("</div>");

        // delegate
        out.print("<div class=\"drill-down\">");
        out.print("delegate = ");
        out.print(drillDownConnection.getDelegateHashcode());
        out.print("</div>");

        // url
        out.print("<div class=\"drill-down\">");
        out.print("url = ");
        out.print(drillDownConnection.getDelegateUrl());
        out.print("</div>");

    }

    private void openHtml(ServletOutputStream out) throws IOException {
        out.println("<html><header><title>Proxool Admin</title>");
        out.println("<style media=\"screen\">");
        out.println("body {background-color: #93bde6;}\n" +
                "div.version {font-weight: bold; font-size: 100%; margin-bottom: 8px;}\n" +
                "h1 {font-weight: bold; font-size: 100%}\n" +
                "option {padding: 2px 24px 2px 4px;}\n" +
                "input {margin: 0px 0px 4px 12px;}\n" +
                "table.data {font-size: 90%; border-collapse: collapse; border: 1px solid black;}\n" +
                "table.data th {background: #bddeff; width: 25em; text-align: left; padding-right: 8px; font-weight: normal; border: 1px solid black;}\n" +
                "table.data td {background: #ffffff; vertical-align: top; padding: 0px 2px 0px 2px; border: 1px solid black;}\n" +
                "td.null {background: yellow;}\n" +
                "td.available {color: black;}\n" +
                "td.active {color: red;}\n" +
                "td.offline {color: blue;}\n" +
                "div.drill-down {}\n" +
                "ul {list-style: none; padding: 0px; margin: 0px; position: relative; font-size: 90%;}\n" +
                "li {padding: 0px; margin: 0px 4px 0px 0px; display: inline; border: 1px solid black; border-width: 1px 1px 0px 1px;}\n" +
                "li.active {background: #bddeff;}\n" +
                "li.inactive {background: #eeeeee;}\n" +
                "li.disabled {background: #dddddd; color: #999999; padding: 0px 4px 0px 4px;}\n" +
                "a.quiet {color: black; text-decoration: none; padding: 0px 4px 0px 4px; }\n" +
                "a.quiet:hover {background: white;}\n");
        out.println("</style>");
        // If we have configured a cssFile then that will override what we have above
        if (cssFile != null) {
            out.println("<link rel=\"stylesheet\" media=\"screen\" type=\"text/css\" href=\"" + cssFile + "\"></script>");
        }
        out.println("</header><body>");
    }

    private void closeHtml(ServletOutputStream out) throws IOException {
        out.println("</body></html>");
    }

    private void openDataTable(ServletOutputStream out) throws IOException {
        out.println("<table cellpadding=\"2\" cellspacing=\"0\" border=\"1\" class=\"data\">");
        out.println("  <tbody>");
    }

    private void closeTable(ServletOutputStream out) throws IOException {
        out.println("  </tbody>");
        out.println("</table>");
        out.println("<br/>");
    }

    private void printDefinitionEntry(ServletOutputStream out, String name, String value, String type) throws IOException {
        out.println("    <tr>");
        out.print("      <th valign=\"top\">");
        out.print(name);
        out.println(":</th>");
        out.print("      <td class=\"" + type + "\"nowrap>");
        if (value != null && !value.equals("null")) {
            out.print(value);
        } else {
            out.print("-");
        }
        out.print("</td>");
        out.println("    </tr>");
    }

    /**
     * Output the list of available connection pools. If there are none then display a message saying that.
     * If there is only one then just display nothing (and the pool will displayed by default)
     * @param out where to write the HTML
     * @param alias identifies the current pool
     * @param tab identifies the tab we are on so that changing pools doesn't change the tab
     * @param link the URL back to this servlet
     */
    private void doList(ServletOutputStream out, String alias, String tab, String link) throws IOException {

        String[] aliases = ProxoolFacade.getAliases();

        if (aliases.length == 0) {
            out.println("<p>No pools have been registered.</p>");
        } else if (aliases.length == 1) {
            // Don't bother listing. Just show it.
        } else {
            out.println("<form action=\"" + link + "\" method=\"GET\" name=\"alias\">");
            out.println("<select name=\"alias\" size=\"" + Math.min(aliases.length, 5) + "\">");
            for (int i = 0; i < aliases.length; i++) {
                out.print("  <option value=\"");
                out.print(aliases[i]);
                out.print("\"");
                out.print(aliases[i].equals(alias) ? " selected" : "");
                out.print(">");
                out.print(aliases[i]);
                out.println("</option>");
            }
            out.println("</select>");
            out.println("<input name=\"" + TAB + "\" value=\"" + tab + "\" type=\"hidden\">");
            out.println("<input value=\"Show\" type=\"submit\">");
            out.println("</form>");
        }
    }

    /**
     * Express time in an easy to read HH:mm:ss format
     *
     * @param time in milliseconds
     * @return time (e.g. 180000 = 00:30:00)
     * @see #TIME_FORMAT
     */
    private String formatMilliseconds(int time) {
        Calendar c = Calendar.getInstance();
        c.clear();
        c.add(Calendar.MILLISECOND, time);
        return TIME_FORMAT.format(c.getTime());
    }
}

/*
Revision history:
$Log: AdminServlet.java,v $
Revision 1.14  2006/06/09 17:32:54  billhorsman
Fix closing tag for select. Credit to Paolo Di Tommaso.

Revision 1.13  2006/01/18 14:39:56  billhorsman
Unbundled Jakarta's Commons Logging.

Revision 1.12  2005/10/07 08:23:10  billhorsman
Doc

Revision 1.11  2005/10/02 09:45:49  billhorsman
Layout

Revision 1.10  2005/09/26 21:47:46  billhorsman
no message

Revision 1.9  2005/09/26 13:31:14  billhorsman
Smartened up AdminServlet

Revision 1.8  2003/09/29 17:49:19  billhorsman
Includes new fatal-sql-exception-wrapper-class in display

Revision 1.7  2003/08/06 20:08:58  billhorsman
fix timezone display of time (for millisecond based properties)

Revision 1.6  2003/03/10 23:43:14  billhorsman
reapplied checkstyle that i'd inadvertently let
IntelliJ change...

Revision 1.5  2003/03/10 15:26:51  billhorsman
refactoringn of concurrency stuff (and some import
optimisation)

Revision 1.4  2003/03/03 11:12:00  billhorsman
fixed licence

Revision 1.3  2003/02/26 16:59:18  billhorsman
fixed spelling error in method name

Revision 1.2  2003/02/26 16:51:12  billhorsman
fixed units for average active time. now displays
properly in seconds

Revision 1.1  2003/02/24 10:19:44  billhorsman
moved AdminServlet into servlet package

Revision 1.1  2003/02/19 23:36:51  billhorsman
renamed monitor package to admin

Revision 1.10  2003/02/12 12:28:27  billhorsman
added url, proxyHashcode and delegateHashcode to
ConnectionInfoIF

Revision 1.9  2003/02/11 00:30:28  billhorsman
add version

Revision 1.8  2003/02/06 17:41:05  billhorsman
now uses imported logging

Revision 1.7  2003/02/06 15:42:21  billhorsman
display changes

Revision 1.6  2003/02/05 17:04:02  billhorsman
fixed date format

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