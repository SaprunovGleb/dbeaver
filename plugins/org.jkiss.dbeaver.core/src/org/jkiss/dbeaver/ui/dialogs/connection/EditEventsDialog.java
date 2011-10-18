/*
 * Copyright (c) 2011, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.ui.dialogs.connection;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.jkiss.dbeaver.model.DBPConnectionInfo;
import org.jkiss.dbeaver.model.runtime.DBRShellCommand;
import org.jkiss.dbeaver.ui.DBIcon;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.dialogs.HelpEnabledDialog;
import org.jkiss.dbeaver.ui.help.IHelpContextIds;
import org.jkiss.utils.CommonUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Events edit dialog
 */
public class EditEventsDialog extends HelpEnabledDialog {

    private DBPConnectionInfo connectionInfo;
    private Text commandText;
    private Button enabledCheck;
    private Button showProcessCheck;
    private Button terminateCheck;
    private Button waitFinishCheck;
    private Table eventTypeTable;

    private final Map<DBPConnectionInfo.EventType, DBRShellCommand> eventsCache = new HashMap<DBPConnectionInfo.EventType, DBRShellCommand>();

    protected EditEventsDialog(Shell shell, DBPConnectionInfo connectionInfo)
    {
        super(shell, IHelpContextIds.CTX_EDIT_CONNECTION_EVENTS);
        this.connectionInfo = connectionInfo;
        for (DBPConnectionInfo.EventType eventType : DBPConnectionInfo.EventType.values()) {
            DBRShellCommand command = connectionInfo.getEvent(eventType);
            eventsCache.put(eventType, command == null ? null : new DBRShellCommand(command));
        }
    }

    @Override
    protected Control createDialogArea(Composite parent)
    {
        getShell().setText("Edit connection's events");
        getShell().setImage(DBIcon.EVENT.getImage());

        Composite composite = (Composite) super.createDialogArea(parent);

        Composite group = UIUtils.createPlaceholder(composite, 2);
        group.setLayoutData(new GridData(GridData.FILL_BOTH));

        {
            Composite eventGroup = new Composite(group, SWT.NONE);
            eventGroup.setLayout(new GridLayout(1, false));
            eventGroup.setLayoutData(new GridData(GridData.FILL_VERTICAL));

            UIUtils.createControlLabel(eventGroup, "Event");
            eventTypeTable = new Table(eventGroup, SWT.BORDER | SWT.CHECK | SWT.SINGLE | SWT.FULL_SELECTION);
            eventTypeTable.setLayoutData(new GridData(GridData.FILL_VERTICAL));

            for (DBPConnectionInfo.EventType eventType : DBPConnectionInfo.EventType.values()) {
                DBRShellCommand command = eventsCache.get(eventType);
                TableItem item = new TableItem(eventTypeTable, SWT.NONE);
                item.setData(eventType);
                item.setText(eventType.getTitle());
                item.setImage(DBIcon.EVENT.getImage());
                item.setChecked(command != null && command.isEnabled());
            }

            eventTypeTable.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e)
                {
                    selectEventType(getSelectedEventType());
                }
            });
        }
        {
            Composite detailsGroup = new Composite(group, SWT.NONE);
            detailsGroup.setLayout(new GridLayout(1, false));
            detailsGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
                //UIUtils.createControlGroup(group, "Event", 1, GridData.FILL_BOTH | GridData.HORIZONTAL_ALIGN_BEGINNING, 0);

            UIUtils.createControlLabel(detailsGroup, "Command");
            commandText = new Text(detailsGroup, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
            commandText.addModifyListener(new ModifyListener() {
                public void modifyText(ModifyEvent e)
                {
                    updateEvent(true);
                }
            });
            GridData gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.heightHint = 60;
            gd.widthHint = 300;
            commandText.setLayoutData(gd);

            SelectionAdapter eventEditAdapter = new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e)
                {
                    updateEvent(false);
                }
            };

            enabledCheck = UIUtils.createCheckbox(detailsGroup, "Enabled", false);
            enabledCheck.addSelectionListener(eventEditAdapter);
            showProcessCheck = UIUtils.createCheckbox(detailsGroup, "Show Process Panel", false);
            showProcessCheck.addSelectionListener(eventEditAdapter);
            waitFinishCheck = UIUtils.createCheckbox(detailsGroup, "Wait Process Finish", false);
            waitFinishCheck.addSelectionListener(eventEditAdapter);
            terminateCheck = UIUtils.createCheckbox(detailsGroup, "Terminate At Disconnect", false);
            terminateCheck.addSelectionListener(eventEditAdapter);
        }

        selectEventType(null);

        return composite;
    }

    private DBPConnectionInfo.EventType getSelectedEventType()
    {
        TableItem[] selection = eventTypeTable.getSelection();
        return CommonUtils.isEmpty(selection) ? null : (DBPConnectionInfo.EventType) selection[0].getData();
    }

    private void updateEvent(boolean commandChange)
    {
        DBPConnectionInfo.EventType eventType = getSelectedEventType();
        if (eventType != null) {
            DBRShellCommand command = eventsCache.get(eventType);
            if (command == null) {
                command = new DBRShellCommand("");
                eventsCache.put(eventType, command);
            }
            boolean prevEnabled = command.isEnabled();
            if (commandChange) {
                command.setCommand(commandText.getText());
            } else {
                command.setEnabled(enabledCheck.getSelection());
                command.setShowProcessPanel(showProcessCheck.getSelection());
                command.setWaitProcessFinish(waitFinishCheck.getSelection());
                command.setTerminateAtDisconnect(terminateCheck.getSelection());
                if (prevEnabled != command.isEnabled()) {
                    selectEventType(eventType);
                }
            }
        } else if (!commandChange) {
            selectEventType(null);
        }
    }

    private void selectEventType(DBPConnectionInfo.EventType eventType)
    {
        enabledCheck.setEnabled(eventType != null);
        DBRShellCommand command = eventType == null ? null : eventsCache.get(eventType);
        commandText.setEnabled(command != null && command.isEnabled());
        showProcessCheck.setEnabled(command != null && command.isEnabled());
        waitFinishCheck.setEnabled(command != null && command.isEnabled());
        terminateCheck.setEnabled(command != null && command.isEnabled());

        if (command != null) {
            commandText.setText(CommonUtils.toString(command.getCommand()));
            enabledCheck.setSelection(command.isEnabled());
            showProcessCheck.setSelection(command.isShowProcessPanel());
            waitFinishCheck.setSelection(command.isWaitProcessFinish());
            terminateCheck.setSelection(command.isTerminateAtDisconnect());
        } else {
            commandText.setText("");
            enabledCheck.setSelection(false);
            showProcessCheck.setSelection(false);
            waitFinishCheck.setSelection(false);
            terminateCheck.setSelection(false);
        }
    }

    @Override
    protected void okPressed()
    {
        for (Map.Entry<DBPConnectionInfo.EventType, DBRShellCommand> entry : eventsCache.entrySet()) {
            connectionInfo.setEvent(entry.getKey(), entry.getValue());
        }
        super.okPressed();
    }

    @Override
    protected void cancelPressed()
    {
        super.cancelPressed();
    }
}
