/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Connection;

/**
 * Implementation of DatabaseMetaData hard coded for JDK 1.2
 * @version $Revision: 1.1 $, $Date: 2003/01/31 14:33:19 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.7
 */
public class ProxyDatabaseMetaData extends AbstractDatabaseMetaData implements DatabaseMetaData {

    public ProxyDatabaseMetaData(Connection connection, ProxyConnectionIF proxyConnection) throws SQLException {
        super(connection, proxyConnection);
    }

    public boolean allProceduresAreCallable() throws SQLException {
        return getDatabaseMetaData().allProceduresAreCallable();
    }

    public boolean allTablesAreSelectable() throws SQLException {
        return getDatabaseMetaData().allTablesAreSelectable();
    }

    public String getURL() throws SQLException {
        return getDatabaseMetaData().getURL();
    }

    public String getUserName() throws SQLException {
        return getDatabaseMetaData().getUserName();
    }

    public boolean isReadOnly() throws SQLException {
        return getDatabaseMetaData().isReadOnly();
    }

    public boolean nullsAreSortedHigh() throws SQLException {
        return getDatabaseMetaData().nullsAreSortedHigh();
    }

    public boolean nullsAreSortedLow() throws SQLException {
        return getDatabaseMetaData().nullsAreSortedLow();
    }

    public boolean nullsAreSortedAtStart() throws SQLException {
        return getDatabaseMetaData().nullsAreSortedAtStart();
    }

    public boolean nullsAreSortedAtEnd() throws SQLException {
        return getDatabaseMetaData().nullsAreSortedAtEnd();
    }

    public String getDatabaseProductName() throws SQLException {
        return getDatabaseMetaData().getDatabaseProductName();
    }

    public String getDatabaseProductVersion() throws SQLException {
        return getDatabaseMetaData().getDatabaseProductVersion();
    }

    public String getDriverName() throws SQLException {
        return getDatabaseMetaData().getDriverName();
    }

    public String getDriverVersion() throws SQLException {
        return getDatabaseMetaData().getDriverVersion();
    }

    public int getDriverMajorVersion() {
        return getDatabaseMetaData().getDriverMajorVersion();
    }

    public int getDriverMinorVersion() {
        return getDatabaseMetaData().getDriverMinorVersion();
    }

    public boolean usesLocalFiles() throws SQLException {
        return getDatabaseMetaData().usesLocalFiles();
    }

    public boolean usesLocalFilePerTable() throws SQLException {
        return getDatabaseMetaData().usesLocalFilePerTable();
    }

    public boolean supportsMixedCaseIdentifiers() throws SQLException {
        return getDatabaseMetaData().supportsMixedCaseIdentifiers();
    }

    public boolean storesUpperCaseIdentifiers() throws SQLException {
        return getDatabaseMetaData().storesUpperCaseIdentifiers();
    }

    public boolean storesLowerCaseIdentifiers() throws SQLException {
        return getDatabaseMetaData().storesLowerCaseIdentifiers();
    }

    public boolean storesMixedCaseIdentifiers() throws SQLException {
        return getDatabaseMetaData().storesMixedCaseIdentifiers();
    }

    public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {
        return getDatabaseMetaData().storesMixedCaseQuotedIdentifiers();
    }

    public boolean storesUpperCaseQuotedIdentifiers() throws SQLException {
        return getDatabaseMetaData().storesUpperCaseQuotedIdentifiers();
    }

    public boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
        return getDatabaseMetaData().storesLowerCaseQuotedIdentifiers();
    }

    public boolean storesMixedCaseQuotedIdentifiers() throws SQLException {
        return getDatabaseMetaData().storesMixedCaseQuotedIdentifiers();
    }

    public String getIdentifierQuoteString() throws SQLException {
        return getDatabaseMetaData().getIdentifierQuoteString();
    }

    public String getSQLKeywords() throws SQLException {
        return getDatabaseMetaData().getSQLKeywords();
    }

    public String getNumericFunctions() throws SQLException {
        return getDatabaseMetaData().getNumericFunctions();
    }

    public String getStringFunctions() throws SQLException {
        return getDatabaseMetaData().getStringFunctions();
    }

    public String getSystemFunctions() throws SQLException {
        return getDatabaseMetaData().getSystemFunctions();
    }

    public String getTimeDateFunctions() throws SQLException {
        return getDatabaseMetaData().getTimeDateFunctions();
    }

    public String getSearchStringEscape() throws SQLException {
        return getDatabaseMetaData().getSearchStringEscape();
    }

    public String getExtraNameCharacters() throws SQLException {
        return getDatabaseMetaData().getExtraNameCharacters();
    }

    public boolean supportsAlterTableWithAddColumn() throws SQLException {
        return getDatabaseMetaData().supportsAlterTableWithAddColumn();
    }

    public boolean supportsAlterTableWithDropColumn() throws SQLException {
        return getDatabaseMetaData().supportsAlterTableWithDropColumn();
    }

    public boolean supportsColumnAliasing() throws SQLException {
        return getDatabaseMetaData().supportsColumnAliasing();
    }

    public boolean nullPlusNonNullIsNull() throws SQLException {
        return getDatabaseMetaData().nullPlusNonNullIsNull();
    }

    public boolean supportsConvert() throws SQLException {
        return getDatabaseMetaData().supportsConvert();
    }

    public boolean supportsConvert(int fromType, int toType) throws SQLException {
        return getDatabaseMetaData().supportsConvert(fromType, toType);
    }

    public boolean supportsTableCorrelationNames() throws SQLException {
        return getDatabaseMetaData().supportsTableCorrelationNames();
    }

    public boolean supportsDifferentTableCorrelationNames() throws SQLException {
        return getDatabaseMetaData().supportsDifferentTableCorrelationNames();
    }

    public boolean supportsExpressionsInOrderBy() throws SQLException {
        return getDatabaseMetaData().supportsExpressionsInOrderBy();
    }

    public boolean supportsOrderByUnrelated() throws SQLException {
        return getDatabaseMetaData().supportsOrderByUnrelated();
    }

    public boolean supportsGroupBy() throws SQLException {
        return getDatabaseMetaData().supportsGroupBy();
    }

    public boolean supportsGroupByUnrelated() throws SQLException {
        return getDatabaseMetaData().supportsGroupByUnrelated();
    }

    public boolean supportsGroupByBeyondSelect() throws SQLException {
        return getDatabaseMetaData().supportsGroupByBeyondSelect();
    }

    public boolean supportsLikeEscapeClause() throws SQLException {
        return getDatabaseMetaData().supportsLikeEscapeClause();
    }

    public boolean supportsMultipleResultSets() throws SQLException {
        return getDatabaseMetaData().supportsMultipleResultSets();
    }

    public boolean supportsMultipleTransactions() throws SQLException {
        return getDatabaseMetaData().supportsMultipleTransactions();
    }

    public boolean supportsNonNullableColumns() throws SQLException {
        return getDatabaseMetaData().supportsNonNullableColumns();
    }

    public boolean supportsMinimumSQLGrammar() throws SQLException {
        return getDatabaseMetaData().supportsMinimumSQLGrammar();
    }

    public boolean supportsCoreSQLGrammar() throws SQLException {
        return getDatabaseMetaData().supportsCoreSQLGrammar();
    }

    public boolean supportsExtendedSQLGrammar() throws SQLException {
        return getDatabaseMetaData().supportsExtendedSQLGrammar();
    }

    public boolean supportsANSI92EntryLevelSQL() throws SQLException {
        return getDatabaseMetaData().supportsANSI92EntryLevelSQL();
    }

    public boolean supportsANSI92IntermediateSQL() throws SQLException {
        return getDatabaseMetaData().supportsANSI92IntermediateSQL();
    }

    public boolean supportsANSI92FullSQL() throws SQLException {
        return getDatabaseMetaData().supportsANSI92FullSQL();
    }

    public boolean supportsIntegrityEnhancementFacility() throws SQLException {
        return getDatabaseMetaData().supportsIntegrityEnhancementFacility();
    }

    public boolean supportsOuterJoins() throws SQLException {
        return getDatabaseMetaData().supportsOuterJoins();
    }

    public boolean supportsFullOuterJoins() throws SQLException {
        return getDatabaseMetaData().supportsFullOuterJoins();
    }

    public boolean supportsLimitedOuterJoins() throws SQLException {
        return getDatabaseMetaData().supportsLimitedOuterJoins();
    }

    public String getSchemaTerm() throws SQLException {
        return getDatabaseMetaData().getSchemaTerm();
    }

    public String getProcedureTerm() throws SQLException {
        return getDatabaseMetaData().getProcedureTerm();
    }

    public String getCatalogTerm() throws SQLException {
        return getDatabaseMetaData().getCatalogTerm();
    }

    public boolean isCatalogAtStart() throws SQLException {
        return getDatabaseMetaData().isCatalogAtStart();
    }

    public String getCatalogSeparator() throws SQLException {
        return getDatabaseMetaData().getCatalogSeparator();
    }

    public boolean supportsSchemasInDataManipulation() throws SQLException {
        return getDatabaseMetaData().supportsSchemasInDataManipulation();
    }

    public boolean supportsSchemasInProcedureCalls() throws SQLException {
        return getDatabaseMetaData().supportsSchemasInProcedureCalls();
    }

    public boolean supportsSchemasInTableDefinitions() throws SQLException {
        return getDatabaseMetaData().supportsSchemasInTableDefinitions();
    }

    public boolean supportsSchemasInIndexDefinitions() throws SQLException {
        return getDatabaseMetaData().supportsSchemasInIndexDefinitions();
    }

    public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException {
        return getDatabaseMetaData().supportsSchemasInPrivilegeDefinitions();
    }

    public boolean supportsCatalogsInDataManipulation() throws SQLException {
        return getDatabaseMetaData().supportsCatalogsInDataManipulation();
    }

    public boolean supportsCatalogsInProcedureCalls() throws SQLException {
        return getDatabaseMetaData().supportsCatalogsInProcedureCalls();
    }

    public boolean supportsCatalogsInTableDefinitions() throws SQLException {
        return getDatabaseMetaData().supportsCatalogsInTableDefinitions();
    }

    public boolean supportsCatalogsInIndexDefinitions() throws SQLException {
        return getDatabaseMetaData().supportsCatalogsInIndexDefinitions();
    }

    public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException {
        return getDatabaseMetaData().supportsCatalogsInPrivilegeDefinitions();
    }

    public boolean supportsPositionedDelete() throws SQLException {
        return getDatabaseMetaData().supportsPositionedDelete();
    }

    public boolean supportsPositionedUpdate() throws SQLException {
        return getDatabaseMetaData().supportsPositionedUpdate();
    }

    public boolean supportsSelectForUpdate() throws SQLException {
        return getDatabaseMetaData().supportsSelectForUpdate();
    }

    public boolean supportsStoredProcedures() throws SQLException {
        return getDatabaseMetaData().supportsStoredProcedures();
    }

    public boolean supportsSubqueriesInComparisons() throws SQLException {
        return getDatabaseMetaData().supportsSubqueriesInComparisons();
    }

    public boolean supportsSubqueriesInExists() throws SQLException {
        return getDatabaseMetaData().supportsSubqueriesInExists();
    }

    public boolean supportsSubqueriesInIns() throws SQLException {
        return getDatabaseMetaData().supportsSubqueriesInIns();
    }

    public boolean supportsSubqueriesInQuantifieds() throws SQLException {
        return getDatabaseMetaData().supportsSubqueriesInQuantifieds();
    }

    public boolean supportsCorrelatedSubqueries() throws SQLException {
        return getDatabaseMetaData().supportsCorrelatedSubqueries();
    }

    public boolean supportsUnion() throws SQLException {
        return getDatabaseMetaData().supportsUnion();
    }

    public boolean supportsUnionAll() throws SQLException {
        return getDatabaseMetaData().supportsUnionAll();
    }

    public boolean supportsOpenCursorsAcrossCommit() throws SQLException {
        return getDatabaseMetaData().supportsOpenCursorsAcrossCommit();
    }

    public boolean supportsOpenCursorsAcrossRollback() throws SQLException {
        return getDatabaseMetaData().supportsOpenCursorsAcrossRollback();
    }

    public boolean supportsOpenStatementsAcrossCommit() throws SQLException {
        return getDatabaseMetaData().supportsOpenStatementsAcrossCommit();
    }

    public boolean supportsOpenStatementsAcrossRollback() throws SQLException {
        return getDatabaseMetaData().supportsOpenStatementsAcrossRollback();
    }

    public int getMaxBinaryLiteralLength() throws SQLException {
        return getDatabaseMetaData().getMaxBinaryLiteralLength();
    }

    public int getMaxCharLiteralLength() throws SQLException {
        return getDatabaseMetaData().getMaxCharLiteralLength();
    }

    public int getMaxColumnNameLength() throws SQLException {
        return getDatabaseMetaData().getMaxColumnNameLength();
    }

    public int getMaxColumnsInGroupBy() throws SQLException {
        return getDatabaseMetaData().getMaxColumnsInGroupBy();
    }

    public int getMaxColumnsInIndex() throws SQLException {
        return getDatabaseMetaData().getMaxColumnsInIndex();
    }

    public int getMaxColumnsInOrderBy() throws SQLException {
        return getDatabaseMetaData().getMaxColumnsInOrderBy();
    }

    public int getMaxColumnsInSelect() throws SQLException {
        return getDatabaseMetaData().getMaxColumnsInSelect();
    }

    public int getMaxColumnsInTable() throws SQLException {
        return getDatabaseMetaData().getMaxColumnsInTable();
    }

    public int getMaxConnections() throws SQLException {
        return getDatabaseMetaData().getMaxConnections();
    }

    public int getMaxCursorNameLength() throws SQLException {
        return getDatabaseMetaData().getMaxCursorNameLength();
    }

    public int getMaxIndexLength() throws SQLException {
        return getDatabaseMetaData().getMaxIndexLength();
    }

    public int getMaxSchemaNameLength() throws SQLException {
        return getDatabaseMetaData().getMaxSchemaNameLength();
    }

    public int getMaxProcedureNameLength() throws SQLException {
        return getDatabaseMetaData().getMaxProcedureNameLength();
    }

    public int getMaxCatalogNameLength() throws SQLException {
        return getDatabaseMetaData().getMaxCatalogNameLength();
    }

    public int getMaxRowSize() throws SQLException {
        return getDatabaseMetaData().getMaxRowSize();
    }

    public boolean doesMaxRowSizeIncludeBlobs() throws SQLException {
        return getDatabaseMetaData().doesMaxRowSizeIncludeBlobs();
    }

    public int getMaxStatementLength() throws SQLException {
        return getDatabaseMetaData().getMaxStatementLength();
    }

    public int getMaxStatements() throws SQLException {
        return getDatabaseMetaData().getMaxStatements();
    }

    public int getMaxTableNameLength() throws SQLException {
        return getDatabaseMetaData().getMaxTableNameLength();
    }

    public int getMaxTablesInSelect() throws SQLException {
        return getDatabaseMetaData().getMaxTablesInSelect();
    }

    public int getMaxUserNameLength() throws SQLException {
        return getDatabaseMetaData().getMaxTablesInSelect();
    }

    public int getDefaultTransactionIsolation() throws SQLException {
        return getDatabaseMetaData().getDefaultTransactionIsolation();
    }

    public boolean supportsTransactions() throws SQLException {
        return getDatabaseMetaData().supportsTransactions();
    }

    public boolean supportsTransactionIsolationLevel(int level)
            throws SQLException {
        return getDatabaseMetaData().supportsTransactionIsolationLevel(level);
    }

    public boolean supportsDataDefinitionAndDataManipulationTransactions()
            throws SQLException {
        return getDatabaseMetaData().supportsDataDefinitionAndDataManipulationTransactions();
    }

    public boolean supportsDataManipulationTransactionsOnly()
            throws SQLException {
        return getDatabaseMetaData().supportsDataManipulationTransactionsOnly();
    }

    public boolean dataDefinitionCausesTransactionCommit()
            throws SQLException {
        return getDatabaseMetaData().dataDefinitionCausesTransactionCommit();
    }

    public boolean dataDefinitionIgnoredInTransactions()
            throws SQLException {
        return getDatabaseMetaData().dataDefinitionIgnoredInTransactions();
    }

    public ResultSet getProcedures(String catalog, String schemaPattern,
                                   String procedureNamePattern) throws SQLException {
        return getDatabaseMetaData().getProcedures(catalog, schemaPattern,
                                   procedureNamePattern);
    }

    public ResultSet getProcedureColumns(String catalog,
                                         String schemaPattern,
                                         String procedureNamePattern,
                                         String columnNamePattern) throws SQLException {
        return getDatabaseMetaData().getProcedureColumns(catalog,
                                         schemaPattern,
                                         procedureNamePattern,
                                         columnNamePattern);
    }

    public ResultSet getTables(String catalog, String schemaPattern,
                               String tableNamePattern, String types[]) throws SQLException {
        return getDatabaseMetaData().getTables(catalog, schemaPattern,
                               tableNamePattern, types);
    }

    public ResultSet getSchemas() throws SQLException {
        return getDatabaseMetaData().getSchemas();
    }

    public ResultSet getCatalogs() throws SQLException {
        return getDatabaseMetaData().getCatalogs();
    }

    public ResultSet getTableTypes() throws SQLException {
        return getDatabaseMetaData().getTableTypes();
    }

    public ResultSet getColumns(String catalog, String schemaPattern,
                                String tableNamePattern, String columnNamePattern)
            throws SQLException {
        return getDatabaseMetaData().getColumns(catalog, schemaPattern,
                                tableNamePattern, columnNamePattern);
    }

    public ResultSet getColumnPrivileges(String catalog, String schema,
                                         String table, String columnNamePattern) throws SQLException {
        return getDatabaseMetaData().getColumnPrivileges(catalog, schema,
                                         table, columnNamePattern);
    }

    public ResultSet getTablePrivileges(String catalog, String schemaPattern,
                                        String tableNamePattern) throws SQLException {
        return getDatabaseMetaData().getTablePrivileges(catalog, schemaPattern,
                                        tableNamePattern);
    }

    public ResultSet getBestRowIdentifier(String catalog, String schema,
                                          String table, int scope, boolean nullable) throws SQLException {
        return getDatabaseMetaData().getBestRowIdentifier(catalog, schema,
                                          table, scope, nullable);
    }

    public ResultSet getVersionColumns(String catalog, String schema,
                                       String table) throws SQLException {
        return getDatabaseMetaData().getVersionColumns(catalog, schema,
                                       table);
    }

    public ResultSet getPrimaryKeys(String catalog, String schema,
                                    String table) throws SQLException {
        return getDatabaseMetaData().getPrimaryKeys(catalog, schema,
                                    table);
    }

    public ResultSet getImportedKeys(String catalog, String schema,
                                     String table) throws SQLException {
        return getDatabaseMetaData().getImportedKeys(catalog, schema,
                                     table);
    }

    public ResultSet getExportedKeys(String catalog, String schema,
                                     String table) throws SQLException {
        return getDatabaseMetaData().getExportedKeys(catalog, schema,
                                     table);
    }

    public ResultSet getCrossReference(
            String primaryCatalog, String primarySchema, String primaryTable,
            String foreignCatalog, String foreignSchema, String foreignTable
            ) throws SQLException {
        return getDatabaseMetaData().getCrossReference(
            primaryCatalog, primarySchema, primaryTable,
            foreignCatalog, foreignSchema, foreignTable
            );
    }

    public ResultSet getTypeInfo() throws SQLException {
        return getDatabaseMetaData().getTypeInfo();
    }

    public ResultSet getIndexInfo(String catalog, String schema, String table,
                                  boolean unique, boolean approximate)
            throws SQLException {
        return getDatabaseMetaData().getIndexInfo(catalog, schema, table,
                                  unique, approximate);
    }

    public boolean supportsResultSetType(int type) throws SQLException {
        return getDatabaseMetaData().supportsResultSetType(type);
    }

    public boolean supportsResultSetConcurrency(int type, int concurrency)
            throws SQLException {
        return getDatabaseMetaData().supportsResultSetConcurrency(type, concurrency);
    }

    public boolean ownUpdatesAreVisible(int type) throws SQLException {
        return getDatabaseMetaData().ownUpdatesAreVisible(type);
    }

    public boolean ownDeletesAreVisible(int type) throws SQLException {
        return getDatabaseMetaData().ownDeletesAreVisible(type);
    }

    public boolean ownInsertsAreVisible(int type) throws SQLException {
        return getDatabaseMetaData().ownInsertsAreVisible(type);
    }

    public boolean othersUpdatesAreVisible(int type) throws SQLException {
        return getDatabaseMetaData().othersUpdatesAreVisible(type);
    }

    public boolean othersDeletesAreVisible(int type) throws SQLException {
        return getDatabaseMetaData().othersDeletesAreVisible(type);
    }

    public boolean othersInsertsAreVisible(int type) throws SQLException {
        return getDatabaseMetaData().othersInsertsAreVisible(type);
    }

    public boolean updatesAreDetected(int type) throws SQLException {
        return getDatabaseMetaData().updatesAreDetected(type);
    }

    public boolean deletesAreDetected(int type) throws SQLException {
        return getDatabaseMetaData().deletesAreDetected(type);
    }

    public boolean insertsAreDetected(int type) throws SQLException {
        return getDatabaseMetaData().insertsAreDetected(type);
    }

    public boolean supportsBatchUpdates() throws SQLException {
        return getDatabaseMetaData().supportsBatchUpdates();
    }

    public ResultSet getUDTs(String catalog, String schemaPattern,
                             String typeNamePattern, int[] types)
            throws SQLException {
        return getDatabaseMetaData().getUDTs(catalog, schemaPattern,
                             typeNamePattern, types);
    }

}


/*
 Revision history:
 $Log: ProxyDatabaseMetaData.java,v $
 Revision 1.1  2003/01/31 14:33:19  billhorsman
 fix for DatabaseMetaData

 */