/*
 * Copyright (c) 2010, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.ext.generic;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.generic.model.GenericDataSource;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCDataSourceProvider;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSDataSourceContainer;

public class GenericDataSourceProvider extends JDBCDataSourceProvider {

    public GenericDataSourceProvider()
    {
    }

    public long getFeatures()
    {
        return FEATURE_CATALOGS | FEATURE_SCHEMAS;
    }

    public DBPDataSource openDataSource(
        DBRProgressMonitor monitor,
        DBSDataSourceContainer container)
        throws DBException
    {
        return new GenericDataSource(container);
    }

}
