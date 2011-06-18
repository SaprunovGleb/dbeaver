/*
 * Copyright (c) 2011, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.ui.export.scripts;

import org.jkiss.utils.CommonUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Text;
import org.jkiss.dbeaver.core.DBeaverCore;
import org.jkiss.dbeaver.model.navigator.DBNResource;
import org.jkiss.dbeaver.runtime.RuntimeUtils;
import org.jkiss.dbeaver.ui.DBIcon;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.views.navigator.database.DatabaseNavigatorTree;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

class ScriptsExportWizardPage extends WizardPage {

    static final Log log = LogFactory.getLog(ScriptsExportWizardPage.class);

    static final String PREF_SCRIPTS_EXPORT_OUT_DIR = "export.scripts.out.dir";

    private Button overwriteCheck;
    private Text directoryText;
    private DatabaseNavigatorTree scriptsNavigator;
    private final List<DBNResource> selectedResources = new ArrayList<DBNResource>();

    protected ScriptsExportWizardPage(String pageName)
    {
        super(pageName);
        setTitle("Export scripts(s)");
    }

    @Override
    public boolean isPageComplete()
    {
        if (directoryText == null || directoryText.isDisposed() || scriptsNavigator == null || scriptsNavigator.isDisposed()) {
            return false;
        }
        if (CommonUtils.isEmpty(directoryText.getText())) {
            setMessage("Output directory is not specified.", IMessageProvider.ERROR);
            return false;
        }
        selectedResources.clear();
        CheckboxTreeViewer viewer = (CheckboxTreeViewer) scriptsNavigator.getViewer();
        for (Object obj : viewer.getCheckedElements()) {
            if (obj instanceof DBNResource) {
                selectedResources.add((DBNResource) obj);
            }
        }
        if (selectedResources.isEmpty()) {
            setMessage("Check script(s) to export.", IMessageProvider.ERROR);
            return false;
        } else {
            setMessage("Configure script(s) export settings.", IMessageProvider.NONE);
            return true;
        }
    }

    public void createControl(Composite parent)
    {
        String outDir = DBeaverCore.getInstance().getGlobalPreferenceStore().getString(PREF_SCRIPTS_EXPORT_OUT_DIR);
        if (CommonUtils.isEmpty(outDir)) {
            outDir = RuntimeUtils.getUserHomeDir().getAbsolutePath();
        }

        Composite placeholder = UIUtils.createPlaceholder(parent, 1);
        placeholder.setLayout(new GridLayout(1, false));

        // Project list
        scriptsNavigator = new DatabaseNavigatorTree(placeholder, ScriptsExportUtils.getScriptsNode(), SWT.BORDER | SWT.CHECK);
        GridData gd = new GridData(GridData.FILL_BOTH);
        scriptsNavigator.setLayoutData(gd);
        CheckboxTreeViewer viewer = (CheckboxTreeViewer) scriptsNavigator.getViewer();
        viewer.addCheckStateListener(new ICheckStateListener() {
            public void checkStateChanged(CheckStateChangedEvent event)
            {
                updateState();
            }
        });

        // Output folder
        Composite generalSettings = UIUtils.createPlaceholder(placeholder, 3);
        generalSettings.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        {
            overwriteCheck = UIUtils.createCheckbox(generalSettings, "Overwrite existing files", false);
            gd = new GridData(GridData.BEGINNING);
            gd.horizontalSpan = 3;
            overwriteCheck.setLayoutData(gd);
            UIUtils.createControlLabel(generalSettings, "Directory");
            directoryText = new Text(generalSettings, SWT.BORDER);
            directoryText.setText(outDir);
            directoryText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            directoryText.addModifyListener(new ModifyListener() {
                public void modifyText(ModifyEvent e)
                {
                    updateState();
                }
            });

            Button openFolder = new Button(generalSettings, SWT.PUSH);
            openFolder.setImage(DBIcon.TREE_FOLDER.getImage());
            openFolder.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e)
                {
                    DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.NONE);
                    dialog.setMessage("Choose directory to place exported files");
                    dialog.setText("Export directory");
                    String directory = directoryText.getText();
                    if (!CommonUtils.isEmpty(directory)) {
                        dialog.setFilterPath(directory);
                    }
                    directory = dialog.open();
                    if (directory != null) {
                        directoryText.setText(directory);
                    }
                }
            });
        }

        setControl(placeholder);

        updateState();
    }

    private void updateState()
    {
        getContainer().updateButtons();
    }

    public ScriptsExportData getExportData()
    {
        Set<IResource> result = new LinkedHashSet<IResource>();
        // Add folders
        for (DBNResource resourceNode : selectedResources) {
            final IResource resource = resourceNode.getResource();
            if (resource instanceof IFolder) {
                addResourceToSet(result, resource);
            }
        }
        // Add files
        for (DBNResource resourceNode : selectedResources) {
            final IResource resource = resourceNode.getResource();
            addResourceToSet(result, resource);
        }

        final String outputDir = directoryText.getText();
        DBeaverCore.getInstance().getGlobalPreferenceStore().setValue(PREF_SCRIPTS_EXPORT_OUT_DIR, outputDir);
        return new ScriptsExportData(result, overwriteCheck.getSelection(), new File(outputDir));
    }

    private void addResourceToSet(Set<IResource> result, IResource resource)
    {
        boolean skip = false;
        for (IResource parent = resource.getParent(); parent != null; parent = parent.getParent()) {
            if (result.contains(parent)) {
                skip = true;
                break;
            }
        }
        if (!skip) {
            result.add(resource);
        }
    }

}
