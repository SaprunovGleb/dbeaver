/*
 * Copyright (c) 2011, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.ext.oracle.data;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jkiss.dbeaver.model.data.DBDContentStorage;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCExecutionContext;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCPreparedStatement;
import org.jkiss.dbeaver.model.impl.jdbc.data.JDBCContentLOB;
import org.jkiss.dbeaver.model.impl.jdbc.data.MimeTypes;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSTypedObject;
import org.jkiss.dbeaver.utils.ContentUtils;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * OracleContentOpaque
 *
 * @author Serge Rider
 */
public abstract class OracleContentOpaque<OPAQUE_TYPE extends Object> extends JDBCContentLOB {

    static final Log log = LogFactory.getLog(OracleContentOpaque.class);

    private OPAQUE_TYPE opaque;
    private InputStream tmpStream;

    public OracleContentOpaque(OPAQUE_TYPE opaque) {
        this.opaque = opaque;
    }

    public long getLOBLength() throws DBCException {
        return 0;//opaque.getLength();
    }

    public String getContentType()
    {
        return MimeTypes.TEXT_XML;
    }

    public DBDContentStorage getContents(DBRProgressMonitor monitor)
        throws DBCException
    {
        if (storage == null && opaque != null) {
            storage = makeStorageFromOpaque(monitor, opaque);
            opaque = null;
        }
        return storage;
    }

    public void release()
    {
        if (tmpStream != null) {
            ContentUtils.close(tmpStream);
            tmpStream = null;
        }
        super.release();
    }

    public void bindParameter(
        JDBCExecutionContext context,
        JDBCPreparedStatement preparedStatement,
        DBSTypedObject columnType,
        int paramIndex)
        throws DBCException
    {
        try {
            if (storage != null) {
                preparedStatement.setObject(paramIndex, createNewOracleObject(context.getOriginal()));
            } else if (opaque != null) {
                preparedStatement.setObject(paramIndex, opaque);
            } else {
                preparedStatement.setNull(paramIndex + 1, java.sql.Types.SQLXML);
            }
        }
        catch (IOException e) {
            throw new DBCException(e);
        }
        catch (SQLException e) {
            throw new DBCException(e);
        }
    }

    public boolean isNull()
    {
        return opaque == null && storage == null;
    }

    @Override
    public String toString() {
        return opaque == null && storage == null ? null : "[" + getOpaqueType() + "]";
    }

    protected abstract String getOpaqueType();

    protected abstract OracleContentOpaque createNewContent();

    protected abstract OPAQUE_TYPE createNewOracleObject(Connection connection)
        throws DBCException, IOException, SQLException;

    protected abstract DBDContentStorage makeStorageFromOpaque(DBRProgressMonitor monitor, OPAQUE_TYPE opaque) throws DBCException;

}
