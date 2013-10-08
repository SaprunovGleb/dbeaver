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

package org.jkiss.dbeaver.model.struct;

import org.jkiss.dbeaver.model.data.DBDDataReceiver;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.exec.DBCSession;
import org.jkiss.dbeaver.model.exec.DBCStatistics;

/**
 * Data manipulator.
 * Extends data container and provides additional methods to manipulate underlying data.
 */
public interface DBSDataManipulator extends DBSDataContainer {

    public static final int DATA_INSERT         = 4;
    public static final int DATA_UPDATE         = 8;
    public static final int DATA_DELETE         = 16;

    interface ExecuteBatch {
        void add(Object[] attributeValues) throws DBCException;

        DBCStatistics execute() throws DBCException;

        void close();
    }

    ExecuteBatch insertData(
        DBCSession session,
        DBSEntityAttribute[] attributes,
        DBDDataReceiver keysReceiver)
        throws DBCException;

    ExecuteBatch updateData(
        DBCSession session,
        DBSEntityAttribute[] updateAttributes,
        DBSEntityAttribute[] keyAttributes,
        DBDDataReceiver keysReceiver)
        throws DBCException;

    ExecuteBatch deleteData(
        DBCSession session,
        DBSEntityAttribute[] keyAttributes)
        throws DBCException;

}
