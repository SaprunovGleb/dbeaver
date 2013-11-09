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
package org.jkiss.dbeaver.model;

/**
 * General model constants
 */
public class DBConstants {

    public static final int METADATA_FETCH_SIZE = 1000;
    public static final long DISCONNECT_TIMEOUT = 2000;

    public static final String DATA_SOURCE_PROPERTY_USER = "user"; //NON-NLS-1
    public static final String DATA_SOURCE_PROPERTY_PASSWORD = "password"; //NON-NLS-1

    public static final String NULL_VALUE_LABEL = "[NULL]"; //NON-NLS-1

    public static final String PROP_ID_NAME = "name"; //NON-NLS-1
    public static final String PROP_ID_DESCRIPTION = "description"; //NON-NLS-1
    public static final String PROP_ID_TYPE_NAME = "typeName"; //NON-NLS-1
    public static final String PROP_ID_MAX_LENGTH = "maxLength"; //NON-NLS-1
    public static final String PROP_ID_NOT_NULL = "notNull"; //NON-NLS-1

    public static final String INTERNAL_PROP_PREFIX = "@dbeaver-"; //NON-NLS-1
    public static final String DEFAULT_DRIVER_PROP_PREFIX = INTERNAL_PROP_PREFIX + "default-"; //NON-NLS-1

    public static final String[] DEFAULT_DATATYPE_NAMES = {
        "varchar", //NON-NLS-1
        "varchar2", //NON-NLS-1
        "string",  //NON-NLS-1
        "char", //NON-NLS-1
        "integer", //NON-NLS-1
        "number" //NON-NLS-1
    };

}
