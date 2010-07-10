/*
 * Copyright (c) 2010, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.ext.mysql;

import org.eclipse.swt.graphics.Image;
import org.jkiss.dbeaver.ext.mysql.data.MySQLEnumValueHandler;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.data.DBDDataTypeProvider;
import org.jkiss.dbeaver.model.data.DBDValueHandler;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.struct.DBSTypedObject;

/**
 * MySQL data types provider
 */
public class MySQLDataTypeProvider implements DBDDataTypeProvider {

    public Image getTypeImage(DBSTypedObject type)
    {
        return JDBCUtils.getDataIcon(type).getImage();
    }

    public DBDValueHandler getHandler(DBPDataSource dataSource, DBSTypedObject type)
    {
        String typeName = type.getTypeName();
        if (MySQLConstants.TYPE_NAME_ENUM.equalsIgnoreCase(typeName)) {
            return MySQLEnumValueHandler.INSTANCE;
        } else if (MySQLConstants.TYPE_NAME_SET.equalsIgnoreCase(typeName)) {
            return MySQLEnumValueHandler.INSTANCE;
        } else {
            return null;
        }
    }

}