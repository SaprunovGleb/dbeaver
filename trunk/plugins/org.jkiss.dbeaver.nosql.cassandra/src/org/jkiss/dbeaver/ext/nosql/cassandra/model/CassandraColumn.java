/*
 * Copyright (C) 2010-2013 Serge Rieder
 * serge@jkiss.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.jkiss.dbeaver.ext.nosql.cassandra.model;

import org.jkiss.dbeaver.model.impl.jdbc.JDBCConstants;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.impl.jdbc.struct.JDBCColumnKeyType;
import org.jkiss.dbeaver.model.impl.jdbc.struct.JDBCTableColumn;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.struct.rdb.DBSTableColumn;
import org.jkiss.utils.CommonUtils;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

/**
 * CassandraColumnFamily
 */
public class CassandraColumn extends JDBCTableColumn<CassandraColumnFamily> implements DBSTableColumn
{

    private final String indexName;
    private final String indexType;
    private final Object indexOptions;

    public static enum KeyType implements JDBCColumnKeyType {
        PRIMARY,
        SECONDARY;

        @Override
        public boolean isInUniqueKey()
        {
            return this == PRIMARY;
        }

        @Override
        public boolean isInReferenceKey()
        {
            return false;
        }
    }

    private KeyType keyType;

    public CassandraColumn(
        CassandraColumnFamily table,
        ResultSet dbResult)
    {
        super(table,
            true,
            JDBCUtils.safeGetStringTrimmed(dbResult, JDBCConstants.COLUMN_NAME),
            JDBCUtils.safeGetStringTrimmed(dbResult, JDBCConstants.TYPE_NAME),
            JDBCUtils.safeGetInt(dbResult, JDBCConstants.DATA_TYPE),
            JDBCUtils.safeGetInt(dbResult, JDBCConstants.ORDINAL_POSITION),
            JDBCUtils.safeGetLong(dbResult, JDBCConstants.COLUMN_SIZE),
            JDBCUtils.safeGetInt(dbResult, JDBCConstants.DECIMAL_DIGITS),
            0,
            JDBCUtils.safeGetInt(dbResult, JDBCConstants.NULLABLE) == DatabaseMetaData.columnNoNulls,
            null);

        if (getName().equals(table.getKeyAlias())) {
            keyType = CassandraColumn.KeyType.PRIMARY;
        }
        indexName = JDBCUtils.safeGetStringTrimmed(dbResult, JDBCConstants.INDEX_NAME);
        indexType = JDBCUtils.safeGetStringTrimmed(dbResult, "INDEX_TYPE");
        indexOptions = JDBCUtils.safeGetStringTrimmed(dbResult, "INDEX_OPTIONS");
        if (!CommonUtils.isEmpty(indexName)) {
            this.keyType = KeyType.SECONDARY;
        }
    }

    @Override
    public CassandraDataSource getDataSource()
    {
        return getTable().getDataSource();
    }

    @Override
    public boolean isSequence()
    {
        return false;
    }

    @Override
    @Property(viewable = true, order = 50)
    public KeyType getKeyType()
    {
        return keyType;
    }

    public String getIndexName()
    {
        return indexName;
    }

    @Property(viewable = false, order = 100)
    public String getIndexType()
    {
        return indexType;
    }

    @Property(viewable = false, order = 101)
    public Object getIndexOptions()
    {
        return indexOptions;
    }

    @Override
    public String getDescription()
    {
        return null;
    }

    // Override to hide property
    @Override
    public long getMaxLength()
    {
        return super.getMaxLength();
    }

    // Override to hide property
    @Override
    public int getScale()
    {
        return super.getScale();
    }

    // Override to hide property
    @Override
    public int getPrecision()
    {
        return super.getPrecision();
    }

    // Override to hide property
    @Override
    public String getDefaultValue()
    {
        return super.getDefaultValue();
    }

    @Override
    public String toString()
    {
        return getTable().getFullQualifiedName() + "." + getName();
    }

}
