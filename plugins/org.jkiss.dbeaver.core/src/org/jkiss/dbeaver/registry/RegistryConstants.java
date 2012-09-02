/*
 * Copyright (C) 2010-2012 Serge Rieder
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
package org.jkiss.dbeaver.registry;

/**
 * Data source descriptors constants
 */
public class RegistryConstants {

    public static final String DRIVERS_FILE_NAME = "drivers.xml"; //$NON-NLS-1$

    public static final String TAG_DRIVERS = "drivers"; //$NON-NLS-1$
    public static final String TAG_DRIVER = "driver"; //$NON-NLS-1$
    public static final String TAG_PROVIDER = "provider"; //$NON-NLS-1$
    public static final String TAG_PARAMETER = "parameter"; //$NON-NLS-1$
    public static final String TAG_PROPERTY = "property"; //$NON-NLS-1$
    public static final String TAG_FILE = "file"; //$NON-NLS-1$
    public static final String TAG_LIBRARY = "library"; // [LEGACY: from DBeaver 1.1.0]  //$NON-NLS-1$
    public static final String TAG_PATH = "path"; //$NON-NLS-1$
    public static final String TAG_REPLACE = "replace"; //$NON-NLS-1$
    public static final String TAG_CLIENT_HOME = "clientHome"; //$NON-NLS-1$

    public static final String TAG_OBJECT_TYPE = "objectType"; //$NON-NLS-1$
    public static final String TAG_PROFILES = "profiles"; //$NON-NLS-1$
    public static final String TAG_PROFILE = "profile"; //$NON-NLS-1$

    public static final String TAG_TYPE = "type"; //$NON-NLS-1$
    public static final String TAG_DATASOURCE = "datasource"; //$NON-NLS-1$
    public static final String TAG_OS = "os"; //NON-NLS-1

    public static final String ATTR_ID = "id"; //$NON-NLS-1$
    public static final String ATTR_CATEGORY = "category"; //$NON-NLS-1$
    public static final String ATTR_DISABLED = "disabled"; //$NON-NLS-1$
    public static final String ATTR_CUSTOM = "custom"; //$NON-NLS-1$
    public static final String ATTR_NAME = "name"; //$NON-NLS-1$
    public static final String ATTR_VALUE = "value"; //$NON-NLS-1$
    public static final String ATTR_CLASS = "class"; //$NON-NLS-1$
    public static final String ATTR_URL = "url"; //$NON-NLS-1$
    public static final String ATTR_PORT = "port"; //$NON-NLS-1$
    public static final String ATTR_DESCRIPTION = "description"; //$NON-NLS-1$
    public static final String ATTR_PATH = "path"; //$NON-NLS-1$
    public static final String ATTR_PROVIDER = "provider"; //$NON-NLS-1$
    public static final String ATTR_COMMENT = "comment"; //$NON-NLS-1$
    public static final String ATTR_ORDER = "order"; //$NON-NLS-1$
    public static final String ATTR_ENABLED = "enabled"; //$NON-NLS-1$
    public static final String ATTR_DRIVER = "driver"; //$NON-NLS-1$

    public static final String ATTR_LABEL = "label"; //$NON-NLS-1$
    public static final String ATTR_DEFAULT_PORT = "defaultPort"; //$NON-NLS-1$
    public static final String ATTR_SAMPLE_URL = "sampleURL"; //$NON-NLS-1$
    public static final String ATTR_WEB_URL = "webURL"; //$NON-NLS-1$
    public static final String ATTR_SUPPORTS_DRIVER_PROPERTIES = "supportsDriverProperties"; //$NON-NLS-1$
    public static final String ATTR_CLIENT_REQUIRED = "clientRequired"; //$NON-NLS-1$
    public static final String ATTR_ANONYMOUS = "anonymous"; //$NON-NLS-1$
    public static final String ATTR_CUSTOM_DRIVER_LOADER = "customDriverLoader"; //$NON-NLS-1$

    public static final String ATTR_ICON = "icon"; //$NON-NLS-1$
    public static final String ATTR_ICON_ID = "iconId"; //$NON-NLS-1$
    public static final String ATTR_STANDARD = "standard"; //$NON-NLS-1$

    public static final String ATTR_TARGET_ID = "targetID"; //$NON-NLS-1$
    public static final String ATTR_TYPE = "type"; //$NON-NLS-1$
    public static final String ATTR_OS = "os"; //$NON-NLS-1$
    public static final String ATTR_ARCH = "arch"; //$NON-NLS-1$
    public static final String ATTR_MAIN = "main"; //$NON-NLS-1$
    public static final String ATTR_POSITION = "position"; //$NON-NLS-1$
    public static final String ATTR_OBJECT_TYPE = "objectType"; //$NON-NLS-1$
    public static final String ATTR_EXTENSIONS = "extensions"; //$NON-NLS-1$
    public static final String ATTR_EXTENSION = "extension"; //$NON-NLS-1$
    public static final String ATTR_SAMPLE_CLASS = "sampleClass"; //$NON-NLS-1$
    public static final String ATTR_SOURCE_TYPE = "sourceType"; //$NON-NLS-1$
    public static final String TAG_FOLDER = "folder"; //$NON-NLS-1$
    public static final String TAG_ITEMS = "items"; //$NON-NLS-1$
    public static final String TAG_OBJECT = "object"; //$NON-NLS-1$

    static final String TAG_TREE = "tree"; //$NON-NLS-1$
    static final String TAG_DRIVER_PROPERTIES = "driver-properties"; //$NON-NLS-1$
    static final String TAG_VIEWS = "views"; //$NON-NLS-1$
    static final String TAG_VIEW = "view"; //$NON-NLS-1$
    static final String TAG_TOOLS = "tools"; //$NON-NLS-1$
    static final String TAG_TOOL = "tool"; //$NON-NLS-1$

    public static final String ATTR_REF = "ref"; //$NON-NLS-1$
    public static final String ATTR_VISIBLE_IF = "visibleIf"; //$NON-NLS-1$
    public static final String ATTR_NAVIGABLE = "navigable"; //$NON-NLS-1$
    public static final String ATTR_ITEM_LABEL = "itemLabel"; //$NON-NLS-1$
    public static final String ATTR_PROPERTY = "property"; //$NON-NLS-1$
    public static final String ATTR_OPTIONAL = "optional"; //$NON-NLS-1$
    public static final String ATTR_VIRTUAL = "virtual"; //$NON-NLS-1$
    public static final String ATTR_INLINE = "inline"; //$NON-NLS-1$
    public static final String ATTR_EDITOR = "editor"; //$NON-NLS-1$
    public static final String ATTR_IF = "if"; //$NON-NLS-1$
    public static final String ATTR_DEFAULT = "default"; //$NON-NLS-1$
    public static final String ATTR_MANAGABLE = "managable"; //$NON-NLS-1$
    public static final String ATTR_CONTRIBUTOR = "contributor"; //$NON-NLS-1$
    public static final String ATTR_INPUT_FACTORY = "inputFactory"; //$NON-NLS-1$

    public static final String ATTR_HANDLER_CLASS = "handlerClass"; //$NON-NLS-1$
    public static final String ATTR_UI_CLASS = "uiClass"; //$NON-NLS-1$
    public static final String ATTR_SECURED = "secured"; //$NON-NLS-1$

    public static final String TAG_DATA_SOURCE = "data-source"; //$NON-NLS-1$
    public static final String TAG_EVENT = "event"; //$NON-NLS-1$
    public static final String TAG_NETWORK_HANDLER = "network-handler"; //$NON-NLS-1$
    public static final String TAG_CUSTOM_PROPERTY = "custom-property"; //$NON-NLS-1$
    public static final String TAG_DESCRIPTION = "description"; //$NON-NLS-1$
    public static final String TAG_CONNECTION = "connection"; //$NON-NLS-1$

    public static final String ATTR_CREATE_DATE = "create-date"; //$NON-NLS-1$
    public static final String ATTR_UPDATE_DATE = "update-date"; //$NON-NLS-1$
    public static final String ATTR_LOGIN_DATE = "login-date"; //$NON-NLS-1$
    public static final String ATTR_SAVE_PASSWORD = "save-password"; //$NON-NLS-1$
    public static final String ATTR_SHOW_SYSTEM_OBJECTS = "show-system-objects"; //$NON-NLS-1$
    public static final String ATTR_READ_ONLY = "read-only"; //$NON-NLS-1$
    public static final String ATTR_FILTER_CATALOG = "filter-catalog"; //$NON-NLS-1$
    public static final String ATTR_FILTER_SCHEMA = "filter-schema"; //$NON-NLS-1$
    public static final String ATTR_HOST = "host"; //$NON-NLS-1$
    public static final String ATTR_SERVER = "server"; //$NON-NLS-1$
    public static final String ATTR_DATABASE = "database"; //$NON-NLS-1$
    public static final String ATTR_USER = "user"; //$NON-NLS-1$
    public static final String ATTR_PASSWORD = "password"; //$NON-NLS-1$
    public static final String ATTR_HOME = "home"; //$NON-NLS-1$
    public static final String ATTR_SHOW_PANEL = "show-panel"; //$NON-NLS-1$
    public static final String ATTR_WAIT_PROCESS = "wait-process"; //$NON-NLS-1$
    public static final String ATTR_TERMINATE_AT_DISCONNECT = "terminate-at-disconnect"; //$NON-NLS-1$
    public static final String TAG_FILTERS = "filters"; //$NON-NLS-1$
    public static final String TAG_FILTER = "filter"; //$NON-NLS-1$
    public static final String TAG_INCLUDE = "include"; //$NON-NLS-1$
    public static final String TAG_EXCLUDE = "exclude"; //$NON-NLS-1$

    public static final String TAG_VIRTUAL_META_DATA = "virtual-meta-data"; //$NON-NLS-1$
    public static final String TAG_MODEL = "model"; //$NON-NLS-1$
    public static final String TAG_CONTAINER = "container"; //$NON-NLS-1$
    public static final String TAG_ENTITY = "entity"; //$NON-NLS-1$
    public static final String TAG_CONSTRAINT = "constraint"; //$NON-NLS-1$
    public static final String TAG_ATTRIBUTE = "attribute"; //$NON-NLS-1$

}
