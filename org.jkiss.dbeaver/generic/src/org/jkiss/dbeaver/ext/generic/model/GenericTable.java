package org.jkiss.dbeaver.ext.generic.model;

import net.sf.jkiss.utils.CommonUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.anno.Property;
import org.jkiss.dbeaver.model.dbc.DBCException;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCConstants;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.impl.meta.AbstractTable;
import org.jkiss.dbeaver.model.jdbc.JDBCPreparedStatement;
import org.jkiss.dbeaver.model.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.jdbc.JDBCDatabaseMetaData;
import org.jkiss.dbeaver.model.jdbc.JDBCExecutionContext;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSConstraintCascade;
import org.jkiss.dbeaver.model.struct.DBSConstraintDefferability;
import org.jkiss.dbeaver.model.struct.DBSConstraintType;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.model.struct.DBSUtils;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GenericTable
 */
public class GenericTable extends AbstractTable<GenericDataSource, GenericStructureContainer>
{
    static Log log = LogFactory.getLog(GenericTable.class);

    private boolean isView;
    private boolean isSystem;

    private String typeName;
    private GenericCatalog typeCatalog;
    private GenericSchema typeSchema;

    private List<GenericTableColumn> columns;
    private List<GenericIndex> indexes;
    private List<GenericConstraint> constraints;
    private List<GenericForeignKey> foreignKeys;
    private Long rowCount;

    /*
        private final ILoadService<Long> rowCountLoader = new JDBCLoadService<Long>("Load row count", this, true) {
            public Long evaluateQuery(Statement statement)
                throws InvocationTargetException, InterruptedException, DBException, SQLException
            {
                ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM " + getFullQualifiedName());
                try {
                    resultSet.next();
                    return resultSet.getLong(1);
                }
                finally {
                    resultSet.close();
                }
            }
        };

    */
    public GenericTable(
        GenericStructureContainer container,
        String tableName,
        String tableType,
        String remarks,
        String typeName,
        GenericCatalog typeCatalog,
        GenericSchema typeSchema)
    {
        super(container, tableName, tableType, remarks);
        this.typeName = typeName;
        this.typeCatalog = typeCatalog;
        this.typeSchema = typeSchema;

        if (!CommonUtils.isEmpty(this.getTableType())) {
            this.isView = (this.getTableType().toUpperCase().indexOf("VIEW") != -1);
            this.isSystem = (this.getTableType().toUpperCase().indexOf("SYSTEM") != -1);
        }
    }

    public DBSObject getParentObject()
    {
        return getContainer().getObject();
    }

    public String getFullQualifiedName()
    {
        return DBSUtils.getFullTableName(getDataSource(),
            getCatalog() == null ? null : getCatalog().getName(),
            getSchema() == null ? null : getSchema().getName(),
            getName());
    }

    public boolean isView()
    {
        return this.isView;
    }

    public boolean isSystem()
    {
        return this.isSystem;
    }

    @Property(name = "Catalog", viewable = true, order = 3)
    public GenericCatalog getCatalog()
    {
        return getContainer().getCatalog();
    }

    @Property(name = "Schema", viewable = true, order = 4)
    public GenericSchema getSchema()
    {
        return getContainer().getSchema();
    }

    public List<GenericTableColumn> getColumns(DBRProgressMonitor monitor)
        throws DBException
    {
        if (columns == null) {
            // Read columns using container
            this.getContainer().getTableCache().loadChildren(monitor, this);
        }
        return columns;
    }

    public GenericTableColumn getColumn(DBRProgressMonitor monitor, String columnName)
        throws DBException
    {
        return DBSUtils.findObject(getColumns(monitor), columnName);
    }

    boolean isColumnsCached()
    {
        return this.columns != null;
    }

    void setColumns(List<GenericTableColumn> columns)
    {
        this.columns = columns;
    }

    public List<GenericIndex> getIndexes(DBRProgressMonitor monitor)
        throws DBException
    {
        if (indexes == null) {
            // Read indexes using cache
            this.getContainer().getIndexCache().getObjects(monitor, this);
        }
        return indexes;
    }

    public GenericIndex getIndex(DBRProgressMonitor monitor, String indexName)
        throws DBException
    {
        return DBSUtils.findObject(getIndexes(monitor), indexName);
    }

    void setIndexes(List<GenericIndex> indexes)
    {
        this.indexes = indexes;
    }

    boolean isIndexesCached()
    {
        return this.indexes != null;
    }

    public List<? extends GenericConstraint> getConstraints(DBRProgressMonitor monitor)
        throws DBException
    {
        if (constraints == null) {
            constraints = loadConstraints(monitor);
        }
        return constraints;
    }

    public List<GenericForeignKey> getReferences(DBRProgressMonitor monitor)
        throws DBException
    {
        return loadForeignKeys(monitor, true);
    }

    public List<GenericForeignKey> getForeignKeys(DBRProgressMonitor monitor)
        throws DBException
    {
        if (foreignKeys == null) {
            foreignKeys = loadForeignKeys(monitor, false);
        }
        return foreignKeys;
    }

    public List<GenericTable> getSubTables()
    {
        return null;
    }

    public boolean refreshObject(DBRProgressMonitor monitor)
        throws DBException
    {
        return false;
    }

    public String getTypeName()
    {
        return typeName;
    }

    public GenericCatalog getTypeCatalog()
    {
        return typeCatalog;
    }

    public GenericSchema getTypeSchema()
    {
        return typeSchema;
    }

    @Property(name = "Row Count", viewable = true, order = 5)
    public long getRowCount(DBRProgressMonitor monitor)
        throws DBCException
    {
        if (rowCount != null) {
            return rowCount;
        }

        JDBCExecutionContext context = getDataSource().openContext(monitor);
        try {
            JDBCPreparedStatement dbStat = context.prepareStatement(
                "SELECT COUNT(*) FROM " + getFullQualifiedName());
            dbStat.setDescription("Select table '" + getName() + "' row count");
            try {
                JDBCResultSet resultSet = dbStat.executeQuery();
                try {
                    resultSet.next();
                    rowCount = resultSet.getLong(1);
                }
                finally {
                    resultSet.close();
                }
            }
            finally {
                dbStat.close();
            }
        }
        catch (SQLException e) {
            throw new DBCException(e);
        }
        finally {
            context.close();
        }

        return rowCount;
    }

    private List<GenericConstraint> loadConstraints(DBRProgressMonitor monitor)
        throws DBException
    {
        monitor.beginTask("Loading contraints", 1);
        JDBCExecutionContext context = getDataSource().openContext(monitor);
        try {
            List<GenericConstraint> pkList = new ArrayList<GenericConstraint>();
            Map<String, GenericConstraint> pkMap = new HashMap<String, GenericConstraint>();
            JDBCDatabaseMetaData metaData = context.getMetaData();
            // Load indexes
            JDBCResultSet dbResult = metaData.getPrimaryKeys(
                getCatalog() == null ? null : getCatalog().getName(),
                getSchema() == null ? null : getSchema().getName(),
                getName());
            try {
                while (dbResult.next()) {
                    String columnName = JDBCUtils.safeGetString(dbResult, JDBCConstants.COLUMN_NAME);
                    int keySeq = JDBCUtils.safeGetInt(dbResult, JDBCConstants.KEY_SEQ);
                    String pkName = JDBCUtils.safeGetString(dbResult, JDBCConstants.PK_NAME);
                    GenericConstraint pk = pkMap.get(pkName);
                    if (pk == null) {
                        pk = new GenericConstraint(
                            DBSConstraintType.PRIMARY_KEY,
                            this,
                            pkName,
                            null);
                        pkList.add(pk);
                        pkMap.put(pkName, pk);
                    }
                    if (CommonUtils.isEmpty(columnName)) {
                        // Bad index - can't evaluate it
                        continue;
                    }
                    GenericTableColumn tableColumn = this.getColumn(monitor, columnName);
                    if (tableColumn == null) {
                        log.warn("Column '" + columnName + "' not found in table '" + this.getName() + "' for PK");
                        continue;
                    }
                    pk.addColumn(
                        new GenericConstraintColumn(
                            pk,
                            tableColumn,
                            keySeq));
                }
            }
            finally {
                dbResult.close();
            }
            return pkList;
        } catch (SQLException ex) {
            throw new DBException(ex);
        } finally {
            context.close();
            monitor.done();
        }
    }

    private List<GenericForeignKey> loadForeignKeys(DBRProgressMonitor monitor, boolean references)
        throws DBException
    {
        JDBCExecutionContext context = getDataSource().openContext(monitor);
        try {
            List<GenericForeignKey> fkList = new ArrayList<GenericForeignKey>();
            Map<String, GenericForeignKey> fkMap = new HashMap<String, GenericForeignKey>();
            Map<String, GenericConstraint> pkMap = new HashMap<String, GenericConstraint>();
            JDBCDatabaseMetaData metaData = context.getMetaData();
            // Load indexes
            JDBCResultSet dbResult;
            if (references) {
                dbResult = metaData.getExportedKeys(
                    getCatalog() == null ? null : getCatalog().getName(),
                    getSchema() == null ? null : getSchema().getName(),
                    getName());
            } else {
                dbResult = metaData.getImportedKeys(
                    getCatalog() == null ? null : getCatalog().getName(),
                    getSchema() == null ? null : getSchema().getName(),
                    getName());
            }
            try {
                while (dbResult.next()) {
                    String pkTableCatalog = JDBCUtils.safeGetString(dbResult, JDBCConstants.PKTABLE_CAT);
                    String pkTableSchema = JDBCUtils.safeGetString(dbResult, JDBCConstants.PKTABLE_SCHEM);
                    String pkTableName = JDBCUtils.safeGetString(dbResult, JDBCConstants.PKTABLE_NAME);
                    String pkColumnName = JDBCUtils.safeGetString(dbResult, JDBCConstants.PKCOLUMN_NAME);
                    String fkTableCatalog = JDBCUtils.safeGetString(dbResult, JDBCConstants.FKTABLE_CAT);
                    String fkTableSchema = JDBCUtils.safeGetString(dbResult, JDBCConstants.FKTABLE_SCHEM);
                    String fkTableName = JDBCUtils.safeGetString(dbResult, JDBCConstants.FKTABLE_NAME);
                    String fkColumnName = JDBCUtils.safeGetString(dbResult, JDBCConstants.FKCOLUMN_NAME);
                    int keySeq = JDBCUtils.safeGetInt(dbResult, JDBCConstants.KEY_SEQ);
                    int updateRuleNum = JDBCUtils.safeGetInt(dbResult, JDBCConstants.UPDATE_RULE);
                    int deleteRuleNum = JDBCUtils.safeGetInt(dbResult, JDBCConstants.DELETE_RULE);
                    String fkName = JDBCUtils.safeGetString(dbResult, JDBCConstants.FK_NAME);
                    String pkName = JDBCUtils.safeGetString(dbResult, JDBCConstants.PK_NAME);
                    int defferabilityNum = JDBCUtils.safeGetInt(dbResult, JDBCConstants.DEFERRABILITY);

                    DBSConstraintCascade deleteRule = getCascadeFromNum(deleteRuleNum);
                    DBSConstraintCascade updateRule = getCascadeFromNum(updateRuleNum);
                    DBSConstraintDefferability defferability;
                    switch (defferabilityNum) {
                        case DatabaseMetaData.importedKeyInitiallyDeferred: defferability = DBSConstraintDefferability.INITIALLY_DEFERRED; break;
                        case DatabaseMetaData.importedKeyInitiallyImmediate: defferability = DBSConstraintDefferability.INITIALLY_IMMEDIATE; break;
                        case DatabaseMetaData.importedKeyNotDeferrable: defferability = DBSConstraintDefferability.NOT_DEFERRABLE; break;
                        default: defferability = DBSConstraintDefferability.UNKNOWN; break;
                    }

                    String pkTableFullName = DBSUtils.getFullTableName(getDataSource(), pkTableCatalog, pkTableSchema, pkTableName);
                    GenericTable pkTable = getDataSource().findTable(monitor, pkTableCatalog, pkTableSchema, pkTableName);
                    if (pkTable == null) {
                        log.warn("Can't find PK table " + pkTableFullName);
                        continue;
                    }
                    String fkTableFullName = DBSUtils.getFullTableName(getDataSource(), fkTableCatalog, fkTableSchema, fkTableName);
                    GenericTable fkTable = getDataSource().findTable(monitor, fkTableCatalog, fkTableSchema, fkTableName);
                    if (fkTable == null) {
                        log.warn("Can't find FK table " + fkTableFullName);
                        continue;
                    }
                    GenericTableColumn pkColumn = pkTable.getColumn(monitor, pkColumnName);
                    if (pkColumn == null) {
                        log.warn("Can't find PK table " + DBSUtils.getFullTableName(getDataSource(), pkTableCatalog, pkTableSchema, pkTableName) + " column " + pkColumnName);
                        continue;
                    }
                    GenericTableColumn fkColumn = fkTable.getColumn(monitor, fkColumnName);
                    if (fkColumn == null) {
                        log.warn("Can't find FK table " + DBSUtils.getFullTableName(getDataSource(), fkTableCatalog, fkTableSchema, fkTableName) + " column " + fkColumnName);
                        continue;
                    }

                    // Find PK
                    GenericConstraint pk = null;
                    if (pkName != null) {
                        pk = DBSUtils.findObject(pkTable.getConstraints(monitor), pkName);
                        if (pk == null) {
                            log.warn("Unique key '" + pkName + "' not found in table " + pkTable.getFullQualifiedName());
                        }
                    }
                    if (pk == null) {
                        for (GenericConstraint pkConstraint : pkTable.getConstraints(monitor)) {
                            if ((pkConstraint.getConstraintType() == DBSConstraintType.PRIMARY_KEY || pkConstraint.getConstraintType() == DBSConstraintType.UNIQUE_KEY)
                                && pkConstraint.getColumn(monitor, pkColumn) != null)
                            {
                                pk = pkConstraint;
                                break;
                            }
                        }
                    }
                    if (pk == null) {
                        log.warn("Could not find primary key for table " + pkTable.getFullQualifiedName());
                        // Too bad. But we have to create new fake PK for this FK
                        String pkFullName = pkTableFullName + "." + pkName;
                        pk = pkMap.get(pkFullName);
                        if (pk == null) {
                            pk = new GenericConstraint(DBSConstraintType.PRIMARY_KEY, pkTable, pkName, null);
                            pk.addColumn(new GenericConstraintColumn(pk, pkColumn, keySeq));
                            pkMap.put(pkFullName, pk);
                        }
                    }

                    // Find (or create) FK
                    GenericForeignKey fk = null;
                    if (references) {
                        fk = DBSUtils.findObject(fkTable.getForeignKeys(monitor), fkName);
                        if (fk == null) {
                            log.warn("Could not find foreign key '" + fkName + "' for table " + fkTable.getFullQualifiedName());
                            // No choice, we have to create fake foreign key :(
                        } else {
                            if (!fkList.contains(fk)) {
                                fkList.add(fk);
                            }
                        }
                    }

                    if (fk == null) {
                        fk = fkMap.get(fkName);
                        if (fk == null) {
                            fk = new GenericForeignKey(fkTable, fkName, null, pk, deleteRule, updateRule, defferability);
                            fkMap.put(fkName, fk);
                            fkList.add(fk);
                        }
                        GenericForeignKeyColumn fkColumnInfo = new GenericForeignKeyColumn(fk, fkColumn, keySeq, pkColumn);
                        fk.addColumn(fkColumnInfo);
                    }
                }
            }
            finally {
                dbResult.close();
            }
            return fkList;
        } catch (SQLException ex) {
            throw new DBException(ex);
        }
        finally {
            context.close();
        }
    }

    private static DBSConstraintCascade getCascadeFromNum(int num)
    {
        switch (num) {
            case DatabaseMetaData.importedKeyNoAction: return DBSConstraintCascade.NO_ACTION;
            case DatabaseMetaData.importedKeyCascade: return DBSConstraintCascade.CASCADE;
            case DatabaseMetaData.importedKeySetNull: return DBSConstraintCascade.SET_NULL;
            case DatabaseMetaData.importedKeySetDefault: return DBSConstraintCascade.SET_DEFAULT;
            case DatabaseMetaData.importedKeyRestrict: return DBSConstraintCascade.RESTRICT;
            default: return DBSConstraintCascade.UNKNOWN;
        }
    }

    public Class<? extends DBSObject> getChildType(DBRProgressMonitor monitor)
        throws DBException
    {
        return GenericTableColumn.class;
    }

    public void cacheStructure(DBRProgressMonitor monitor, int scope)
        throws DBException
    {
        getColumns(monitor);
    }

}
