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
package org.jkiss.dbeaver.ui.controls.resultset;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.widgets.Shell;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.virtual.DBVConstants;
import org.jkiss.dbeaver.model.virtual.DBVEntity;
import org.jkiss.dbeaver.runtime.VoidProgressMonitor;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.utils.CommonUtils;

/**
 * Confirm virtual key usage dialog
 */
class ConfirmVirtualKeyUsageDialog extends MessageDialogWithToggle {

    private ResultSetViewer viewer;

    protected ConfirmVirtualKeyUsageDialog(ResultSetViewer viewer)
    {
        super(
            viewer.getControl().getShell(),
            "Confirm usage of virtual key",
            null,
            "You are about to persist changes. Virtual (set by user) unique key is used for the table '" +
                viewer.getVirtualEntityIdentifier().getReferrer().getParentObject().getName() +
                "'.\nIncorrect virtual key configuration may cause table data corruption.\nAre you sure you want to proceed?",
            WARNING,
            new String[]{IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL, "Edit Virtual Key"},
            0,
            "Do not ask again for '" + viewer.getVirtualEntityIdentifier().getReferrer().getParentObject().getName() + "'",
            false);
        this.viewer = viewer;
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
    }

    @Override
    protected void buttonPressed(int buttonId)
    {
        switch (buttonId)
        {
            case IDialogConstants.OK_ID:
            case IDialogConstants.CANCEL_ID:
                if (getToggleState()) {
                    // Save toggle state
                    DBVEntity entity = (DBVEntity) viewer.getVirtualEntityIdentifier().getReferrer().getParentObject();
                    entity.setProperty(DBVConstants.PROPERTY_USE_VIRTUAL_KEY_QUIET, Boolean.TRUE.toString());
                }
                super.buttonPressed(buttonId);
                break;
            default:
                try {
                    viewer.editEntityIdentifier(VoidProgressMonitor.INSTANCE);
                } catch (DBException e) {
                    UIUtils.showErrorDialog(getShell(), "Virtual key edit", "Error editing virtual key", e);
                }
                break;
        }
    }

    @Override
    public int open()
    {
        DBVEntity entity = (DBVEntity) viewer.getVirtualEntityIdentifier().getReferrer().getParentObject();
        if (CommonUtils.getBoolean(entity.getProperty(DBVConstants.PROPERTY_USE_VIRTUAL_KEY_QUIET))) {
            return IDialogConstants.OK_ID;
        }
        return super.open();
    }
}
