/*
 * Copyright (c) 2010, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.ext.mysql.data;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.mysql.model.MySQLTableColumn;
import org.jkiss.dbeaver.model.data.DBDValueController;
import org.jkiss.dbeaver.model.struct.DBSTypedObject;
import org.jkiss.dbeaver.model.struct.DBSColumnBase;
import org.jkiss.dbeaver.model.struct.DBSTableColumn;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.impl.jdbc.data.JDBCAbstractValueHandler;
import org.jkiss.dbeaver.model.dbc.DBCColumnMetaData;
import org.jkiss.dbeaver.ui.dialogs.data.TextViewDialog;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * JDBC string value handler
 */
public class MySQLEnumValueHandler extends JDBCAbstractValueHandler {

    public static final MySQLEnumValueHandler INSTANCE = new MySQLEnumValueHandler();

    public String getValueDisplayString(DBSTypedObject column, Object value) {
        String strValue = ((MySQLTypeEnum) value).getValue();
        return strValue == null ? NULL_VALUE_LABEL : strValue;
    }

    protected Object getColumnValue(
        DBRProgressMonitor monitor,
        ResultSet resultSet,
        DBSColumnBase column,
        int columnIndex)
        throws SQLException
    {
        DBSTableColumn tableColumn = null;
        if (column instanceof DBSTableColumn) {
            tableColumn = (DBSTableColumn)column;
        } else if (column instanceof DBCColumnMetaData) {
            try {
                tableColumn = ((DBCColumnMetaData)column).getTableColumn(monitor);
            }
            catch (DBException e) {
                throw new SQLException(e);
            }
        }
        if (tableColumn == null) {
            throw new SQLException("Could not find table column for column '" + column.getName() + "'");
        }
        MySQLTableColumn enumColumn;
        if (tableColumn instanceof MySQLTableColumn) {
            enumColumn = (MySQLTableColumn)tableColumn;
        } else {
            throw new SQLException("Bad column type: " + tableColumn.getClass().getName());
        }
        return new MySQLTypeEnum(enumColumn, resultSet.getString(columnIndex));
    }

    @Override
    public void bindParameter(DBRProgressMonitor monitor, PreparedStatement statement, DBSTypedObject paramType, int paramIndex, Object value)
        throws SQLException
    {
        MySQLTypeEnum e = (MySQLTypeEnum)value;
        if (e == null || e.isNull()) {
            statement.setNull(paramIndex, paramType.getValueType());
        } else {
            statement.setString(paramIndex, e.getValue());
        }
    }

    public boolean editValue(final DBDValueController controller)
        throws DBException
    {
        if (controller.isInlineEdit()) {
            final MySQLTypeEnum value = (MySQLTypeEnum)controller.getValue();

            Combo editor = new Combo(controller.getInlinePlaceholder(), SWT.READ_ONLY);
            List<String> enumValues = value.getColumn().getEnumValues();
            editor.add("");
            if (enumValues != null) {
                for (String enumValue : enumValues) {
                    editor.add(enumValue);
                }
            }
            editor.setText(value.isNull() ? "" : value.getValue());
            if (editor.getSelectionIndex() < 0) {
                editor.select(0);
            }
            editor.setFocus();
            initInlineControl(controller, editor, new ValueExtractor<Combo>() {
                public Object getValueFromControl(Combo control)
                {
                    int selIndex = control.getSelectionIndex();
                    if (selIndex < 0) {
                        return new MySQLTypeEnum(value.getColumn(), null);
                    } else if (selIndex == 0) {
                        return new MySQLTypeEnum(value.getColumn(), null);
                    } else {
                        return new MySQLTypeEnum(value.getColumn(), control.getItem(selIndex));
                    }
                }
            });
            return true;
        } else {
            TextViewDialog dialog = new TextViewDialog(controller);
            dialog.open();
            return true;
        }
    }

    public Object copyValueObject(Object value)
    {
        // String are immutable
        MySQLTypeEnum e = (MySQLTypeEnum)value;
        return new MySQLTypeEnum(e.getColumn(), e.getValue());
    }

/*
    public void fillProperties(PropertySourceAbstract propertySource, DBDValueController controller)
    {
        propertySource.addProperty(
            "max_length",
            "Max Length",
            controller.getColumnMetaData().getDisplaySize());
    }
*/

}