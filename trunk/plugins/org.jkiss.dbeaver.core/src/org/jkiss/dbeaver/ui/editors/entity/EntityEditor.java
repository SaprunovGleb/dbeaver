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
package org.jkiss.dbeaver.ui.editors.entity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.*;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.core.CoreMessages;
import org.jkiss.dbeaver.core.DBeaverCore;
import org.jkiss.dbeaver.ext.IDatabasePersistAction;
import org.jkiss.dbeaver.ext.IProgressControlProvider;
import org.jkiss.dbeaver.ext.IPropertyChangeReflector;
import org.jkiss.dbeaver.ext.ui.IFolderListener;
import org.jkiss.dbeaver.ext.ui.IFolderedPart;
import org.jkiss.dbeaver.ext.ui.INavigatorModelView;
import org.jkiss.dbeaver.ext.ui.IRefreshablePart;
import org.jkiss.dbeaver.model.DBPObject;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.edit.DBECommand;
import org.jkiss.dbeaver.model.edit.DBECommandContext;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.impl.edit.DBECommandAdapter;
import org.jkiss.dbeaver.model.navigator.DBNDataSource;
import org.jkiss.dbeaver.model.navigator.DBNDatabaseFolder;
import org.jkiss.dbeaver.model.navigator.DBNDatabaseNode;
import org.jkiss.dbeaver.model.navigator.DBNNode;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.runtime.DBRRunnableWithProgress;
import org.jkiss.dbeaver.model.struct.DBSDataSourceContainer;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.model.struct.DBSObjectStateful;
import org.jkiss.dbeaver.registry.EntityEditorDescriptor;
import org.jkiss.dbeaver.registry.EntityEditorsRegistry;
import org.jkiss.dbeaver.registry.tree.DBXTreeItem;
import org.jkiss.dbeaver.registry.tree.DBXTreeNode;
import org.jkiss.dbeaver.runtime.DefaultProgressMonitor;
import org.jkiss.dbeaver.runtime.VoidProgressMonitor;
import org.jkiss.dbeaver.ui.DBIcon;
import org.jkiss.dbeaver.ui.IHelpContextIds;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.controls.ProgressPageControl;
import org.jkiss.dbeaver.ui.dialogs.ConfirmationDialog;
import org.jkiss.dbeaver.ui.dialogs.ViewSQLDialog;
import org.jkiss.dbeaver.ui.editors.MultiPageDatabaseEditor;
import org.jkiss.dbeaver.ui.preferences.PrefConstants;
import org.jkiss.dbeaver.utils.ContentUtils;
import org.jkiss.utils.CommonUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * EntityEditor
 */
public class EntityEditor extends MultiPageDatabaseEditor
    implements INavigatorModelView, IPropertyChangeReflector, IProgressControlProvider, ISaveablePart2, IFolderedPart
{
    static final Log log = LogFactory.getLog(EntityEditor.class);

    private static class EditorDefaults {
        String pageId;
        String folderId;

        private EditorDefaults(String pageId, String folderId)
        {
            this.pageId = pageId;
            this.folderId = folderId;
        }
    }

    private static final Map<String, EditorDefaults> defaultPageMap = new HashMap<String, EditorDefaults>();

    private final Map<String, IEditorPart> editorMap = new LinkedHashMap<String, IEditorPart>();
    private IEditorPart activeEditor;
    private DBECommandAdapter commandListener;
    private IFolderListener folderListener;
    private boolean hasPropertiesEditor;
    private Map<IEditorPart, IEditorActionBarContributor> actionContributors = new HashMap<IEditorPart, IEditorActionBarContributor>();

    public EntityEditor()
    {
        folderListener = new IFolderListener() {
            @Override
            public void folderSelected(String folderId)
            {
                IEditorPart editor = getActiveEditor();
                if (editor != null) {
                    String editorPageId = getEditorPageId(editor);
                    if (editorPageId != null) {
                        updateEditorDefaults(editorPageId, folderId);
                    }
                }
            }
        };
    }

    @Override
    public void handlePropertyChange(int propId)
    {
        super.handlePropertyChange(propId);
    }

    @Override
    public ProgressPageControl getProgressControl()
    {
        IEditorPart activeEditor = getActiveEditor();
        return activeEditor instanceof IProgressControlProvider ? ((IProgressControlProvider) activeEditor).getProgressControl() : null;
    }

    public DBSObject getDatabaseObject()
    {
        return getEditorInput().getDatabaseObject();
    }

    public DBECommandContext getCommandContext()
    {
        return getEditorInput().getCommandContext();
    }

    @Override
    public void dispose()
    {
        for (Map.Entry<IEditorPart, IEditorActionBarContributor> entry : actionContributors.entrySet()) {
            GlobalContributorManager.getInstance().removeContributor(entry.getValue(), entry.getKey());
        }
        actionContributors.clear();
        //final DBPDataSource dataSource = getDataSource();

//        if (getCommandContext() != null && getCommandContext().isDirty()) {
//            getCommandContext().resetChanges();
//        }
        if (commandListener != null && getCommandContext() != null) {
            getCommandContext().removeCommandListener(commandListener);
            commandListener = null;
        }
        super.dispose();

        if (getDatabaseObject() != null) {
            getCommandContext().resetChanges();
//            // Remove all non-persisted objects
//            for (DBPObject object : getCommandContext().getEditedObjects()) {
//                if (object instanceof DBPPersistedObject && !((DBPPersistedObject)object).isPersisted()) {
//                    dataSource.getContainer().fireEvent(new DBPEvent(DBPEvent.Action.OBJECT_REMOVE, (DBSObject) object));
//                }
//            }
        }
        this.editorMap.clear();
        this.activeEditor = null;
    }

    @Override
    public boolean isDirty()
    {
        final DBECommandContext commandContext = getCommandContext();
        if (commandContext != null && commandContext.isDirty()) {
            return true;
        }

        for (IEditorPart editor : editorMap.values()) {
            if (editor.isDirty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isSaveAsAllowed()
    {
        return this.activeEditor != null && this.activeEditor.isSaveAsAllowed();
    }

    @Override
    public void doSaveAs()
    {
        IEditorPart activeEditor = getActiveEditor();
        if (activeEditor != null && activeEditor.isSaveAsAllowed()) {
            activeEditor.doSaveAs();
        }
    }

    /**
     * Saves data in all nested editors
     * @param monitor progress monitor
     */
    @Override
    public void doSave(IProgressMonitor monitor)
    {
        if (!isDirty()) {
            return;
        }

        for (IEditorPart editor : editorMap.values()) {
            editor.doSave(monitor);
        }

        final DBECommandContext commandContext = getCommandContext();
        if (commandContext != null && commandContext.isDirty()) {
            saveCommandContext(monitor);
        }

        firePropertyChange(IEditorPart.PROP_DIRTY);
    }

    private void saveCommandContext(IProgressMonitor monitor)
    {
        monitor.beginTask(CoreMessages.editors_entity_monitor_preview_changes, 1);
        int previewResult = showChanges(true);
        monitor.done();

        final DefaultProgressMonitor monitorWrapper = new DefaultProgressMonitor(monitor);

        if (previewResult == IDialogConstants.PROCEED_ID) {
            Throwable error = null;
            try {
                getCommandContext().saveChanges(monitorWrapper);
            } catch (DBException e) {
                error = e;
            }
            if (getDatabaseObject() instanceof DBSObjectStateful) {
                try {
                    ((DBSObjectStateful) getDatabaseObject()).refreshObjectState(monitorWrapper);
                } catch (DBCException e) {
                    // Just report an error
                    log.error(e);
                }
            }

            if (error == null) {
                // Refresh underlying node
                // It'll refresh database object and all it's descendants
                // So we'll get actual data from database
                final DBNDatabaseNode treeNode = getEditorInput().getTreeNode();
                try {
                    DBeaverCore.getInstance().runInProgressService(new DBRRunnableWithProgress() {
                        @Override
                        public void run(DBRProgressMonitor monitor) throws InvocationTargetException, InterruptedException
                        {
                            try {
                                treeNode.refreshNode(monitor, getCommandContext());
                            } catch (DBException e) {
                                throw new InvocationTargetException(e);
                            }
                        }
                    });
                } catch (InvocationTargetException e) {
                    error = e.getTargetException();
                } catch (InterruptedException e) {
                    // ok
                }
            }
            if (error != null) {
                UIUtils.showErrorDialog(getSite().getShell(), "Could not save '" + getDatabaseObject().getName() + "'", null, error);
            }
        }
    }

    public void revertChanges()
    {
        if (isDirty()) {
            if (ConfirmationDialog.showConfirmDialog(
                null,
                PrefConstants.CONFIRM_ENTITY_REVERT,
                ConfirmationDialog.QUESTION,
                getDatabaseObject().getName()) != IDialogConstants.YES_ID)
            {
                return;
            }
            getCommandContext().resetChanges();
            firePropertyChange(IEditorPart.PROP_DIRTY);
        }
    }

    public void undoChanges()
    {
        if (getCommandContext() != null && getCommandContext().getUndoCommand() != null) {
            if (!getDatabaseObject().isPersisted() && getCommandContext().getUndoCommands().size() == 1) {
                //getSite().getPage().closeEditor(this, true);
                //return;
                // Undo of last command in command context will close editor
                // Let's ask user about it
                if (ConfirmationDialog.showConfirmDialog(
                    null,
                    PrefConstants.CONFIRM_ENTITY_REJECT,
                    ConfirmationDialog.QUESTION,
                    getDatabaseObject().getName()) != IDialogConstants.YES_ID)
                {
                    return;
                }
            }
            getCommandContext().undoCommand();
            firePropertyChange(IEditorPart.PROP_DIRTY);
        }
    }

    public void redoChanges()
    {
        if (getCommandContext() != null && getCommandContext().getRedoCommand() != null) {
            getCommandContext().redoCommand();
            firePropertyChange(IEditorPart.PROP_DIRTY);
        }
    }

    public int showChanges(boolean allowSave)
    {
        if (getCommandContext() == null) {
            return IDialogConstants.CANCEL_ID;
        }
        Collection<? extends DBECommand> commands = getCommandContext().getFinalCommands();
        StringBuilder script = new StringBuilder();
        for (DBECommand command : commands) {
            try {
                command.validateCommand();
            } catch (final DBException e) {
                UIUtils.runInUI(null, new Runnable() {
                    @Override
                    public void run()
                    {
                        UIUtils.showErrorDialog(getSite().getShell(), "Validation", e.getMessage());
                    }
                });
                return IDialogConstants.CANCEL_ID;
            }
            script.append(DBUtils.generateScript(
                getDataSource(),
                command.getPersistActions()));
        }

        ChangesPreviewer changesPreviewer = new ChangesPreviewer(script, allowSave);
        UIUtils.runInUI(getSite().getShell(), changesPreviewer);
        return changesPreviewer.getResult();
/*

        Shell shell = getSite().getShell();
        EditTextDialog dialog = new EditTextDialog(shell, "Script", script.toString());
        dialog.setTextWidth(0);
        dialog.setTextHeight(0);
        dialog.setImage(DBIcon.SQL_PREVIEW.getImage());
        dialog.open();
*/
    }

    @Override
    protected void createPages()
    {
/*
        {
            IBindingService bindingService = (IBindingService)getSite().getService(IBindingService.class);
            for (Binding binding : bindingService.getBindings()) {
                System.out.println("binding:" + binding);
            }
        }
        {
            ICommandService commandService = (ICommandService)getSite().getService(ICommandService.class);
            for (Command command : commandService.getDefinedCommands()) {
                System.out.println("command:" + command);
            }
        }
*/

        // Command listener
        commandListener = new DBECommandAdapter() {
            @Override
            public void onCommandChange(DBECommand command)
            {
                firePropertyChange(IEditorPart.PROP_DIRTY);
            }
        };
        getCommandContext().addCommandListener(commandListener);

        // Property listener
        addPropertyListener(new IPropertyListener() {
            @Override
            public void propertyChanged(Object source, int propId)
            {
                if (propId == IEditorPart.PROP_DIRTY) {
                    EntityEditorPropertyTester.firePropertyChange(EntityEditorPropertyTester.PROP_DIRTY);
                    EntityEditorPropertyTester.firePropertyChange(EntityEditorPropertyTester.PROP_CAN_UNDO);
                    EntityEditorPropertyTester.firePropertyChange(EntityEditorPropertyTester.PROP_CAN_REDO);
                }
            }
        });

        super.createPages();

        EditorDefaults editorDefaults;
        synchronized (defaultPageMap) {
            editorDefaults = defaultPageMap.get(getEditorInput().getDatabaseObject().getClass().getName());
        }

        EntityEditorsRegistry editorsRegistry = DBeaverCore.getInstance().getEditorsRegistry();
        DBSObject databaseObject = getEditorInput().getDatabaseObject();

        // Add object editor page
        EntityEditorDescriptor defaultEditor = editorsRegistry.getMainEntityEditor(databaseObject);
        hasPropertiesEditor = false;
        if (defaultEditor != null) {
            hasPropertiesEditor = addEditorTab(defaultEditor);
        }
        if (hasPropertiesEditor) {
            DBNNode node = getEditorInput().getTreeNode();
            setPageText(0, CoreMessages.editors_entity_properties_text);
            setPageToolTip(0, node.getNodeType() + CoreMessages.editors_entity_properties_tooltip_suffix);
            setPageImage(0, node.getNodeIconDefault());
        }
/*
        if (!mainAdded) {
            try {
                DBNNode node = getEditorInput().getTreeNode();
                int index = addPage(new ObjectPropertiesEditor(node), getEditorInput());
                setPageText(index, "Properties");
                if (node instanceof DBNDatabaseNode) {
                    setPageToolTip(index, ((DBNDatabaseNode)node).getMeta().getChildrenType() + " Properties");
                }
                setPageImage(index, node.getNodeIconDefault());
            } catch (PartInitException e) {
                log.error("Error creating object editor");
            }
        }
*/

        // Add contributed pages
        addContributions(EntityEditorDescriptor.POSITION_PROPS);
        addContributions(EntityEditorDescriptor.POSITION_START);
        addContributions(EntityEditorDescriptor.POSITION_MIDDLE);

        // Add navigator tabs
        //addNavigatorTabs();

        // Add contributed pages
        addContributions(EntityEditorDescriptor.POSITION_END);

        String defPageId = getEditorInput().getDefaultPageId();
        if (defPageId == null && editorDefaults != null) {
            defPageId = editorDefaults.pageId;
        }
        if (defPageId != null) {
            IEditorPart defEditorPage = editorMap.get(defPageId);
            if (defEditorPage != null) {
                setActiveEditor(defEditorPage);
            }
        }
        this.activeEditor = getActiveEditor();
        if (activeEditor instanceof IFolderedPart) {
            String defFolderId = getEditorInput().getDefaultFolderId();
            if (defFolderId == null && editorDefaults != null) {
                defFolderId = editorDefaults.folderId;
            }
            if (defFolderId != null) {
                ((IFolderedPart)activeEditor).switchFolder(defFolderId);
            }
        }

        UIUtils.setHelp(getContainer(), IHelpContextIds.CTX_ENTITY_EDITOR);
    }

    private void addNavigatorTabs()
    {
        // Collect tabs from navigator tree model
        final List<TabInfo> tabs = new ArrayList<TabInfo>();
        DBRRunnableWithProgress tabsCollector = new DBRRunnableWithProgress() {
            @Override
            public void run(DBRProgressMonitor monitor)
            {
                tabs.addAll(collectTabs(monitor));
            }
        };
        DBNDatabaseNode node = getEditorInput().getTreeNode();
        try {
            if (node.isLazyNode()) {
                DBeaverCore.getInstance().runInProgressService(tabsCollector);
            } else {
                tabsCollector.run(VoidProgressMonitor.INSTANCE);
            }
        } catch (InvocationTargetException e) {
            log.error(e.getTargetException());
        } catch (InterruptedException e) {
            // just go further
        }

        for (TabInfo tab : tabs) {
            addNodeTab(tab);
        }
    }

    @Override
    protected void pageChange(int newPageIndex) {
        super.pageChange(newPageIndex);

        activeEditor = getEditor(newPageIndex);

        for (Map.Entry<IEditorPart, IEditorActionBarContributor> entry : actionContributors.entrySet()) {
            if (entry.getKey() == activeEditor) {
                entry.getValue().setActiveEditor(activeEditor);
            } else {
                entry.getValue().setActiveEditor(null);
            }
        }

        String editorPageId = getEditorPageId(activeEditor);
        if (editorPageId != null) {
            updateEditorDefaults(editorPageId, null);
        }
        // Fire dirty flag refresh to re-enable Save-As command (which is enabled only for certain pages)
        firePropertyChange(IEditorPart.PROP_DIRTY);
    }

    private String getEditorPageId(IEditorPart editorPart)
    {
        synchronized (editorMap) {
            for (Map.Entry<String,IEditorPart> entry : editorMap.entrySet()) {
                if (entry.getValue() == editorPart) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    private void updateEditorDefaults(String pageId, String folderId)
    {
        DBSObject object = getEditorInput().getDatabaseObject();
        if (object != null) {
            synchronized (defaultPageMap) {
                EditorDefaults editorDefaults = defaultPageMap.get(object.getClass().getName());
                if (editorDefaults == null) {
                    editorDefaults = new EditorDefaults(pageId, folderId);
                    defaultPageMap.put(object.getClass().getName(), editorDefaults);
                } else {
                    if (pageId != null) {
                        editorDefaults.pageId = pageId;
                    }
                    if (folderId != null) {
                        editorDefaults.folderId = folderId;
                    }
                }
            }
        }
    }

    @Override
    public int promptToSaveOnClose()
    {
        final int result = ConfirmationDialog.showConfirmDialog(
            getSite().getShell(),
            PrefConstants.CONFIRM_ENTITY_EDIT_CLOSE,
            ConfirmationDialog.QUESTION_WITH_CANCEL,
            getEditorInput().getTreeNode().getNodeName());
        if (result == IDialogConstants.YES_ID) {
//            getWorkbenchPart().getSite().getPage().saveEditor(this, false);
            return ISaveablePart2.YES;
        } else if (result == IDialogConstants.NO_ID) {
            return ISaveablePart2.NO;
        } else {
            return ISaveablePart2.CANCEL;
        }
    }

    @Override
    public Object getActiveFolder()
    {
        if (getActiveEditor() instanceof IFolderedPart) {
            ((IFolderedPart)getActiveEditor()).getActiveFolder();
        }
        return null;
    }

    @Override
    public void switchFolder(String folderId)
    {
        for (IEditorPart editor : editorMap.values()) {
            if (editor instanceof IFolderedPart) {
                if (getActiveEditor() != editor) {
                    setActiveEditor(editor);
                }
                ((IFolderedPart)editor).switchFolder(folderId);
            }
        }
//        if (getActiveEditor() instanceof IFolderedPart) {
//            ((IFolderedPart)getActiveEditor()).switchFolder(folderId);
//        }
    }

    @Override
    public void addFolderListener(IFolderListener listener)
    {
    }

    @Override
    public void removeFolderListener(IFolderListener listener)
    {
    }

    private static class TabInfo {
        DBNDatabaseNode node;
        DBXTreeNode meta;
        private TabInfo(DBNDatabaseNode node)
        {
            this.node = node;
        }
        private TabInfo(DBNDatabaseNode node, DBXTreeNode meta)
        {
            this.node = node;
            this.meta = meta;
        }
        public String getName()
        {
            return meta == null ? node.getNodeName() : meta.getChildrenType(node.getObject().getDataSource());
        }
    }

    private List<TabInfo> collectTabs(DBRProgressMonitor monitor)
    {
        List<TabInfo> tabs = new ArrayList<TabInfo>();

        // Add all nested folders as tabs
        DBNNode node = getEditorInput().getTreeNode();
        if (node instanceof DBNDataSource && !((DBNDataSource)node).getDataSourceContainer().isConnected()) {
            // Do not add children tabs
        } else if (node != null) {
            try {
                List<? extends DBNNode> children = node.getChildren(monitor);
                if (children != null) {
                    for (DBNNode child : children) {
                        if (child instanceof DBNDatabaseFolder) {
                            monitor.subTask(CoreMessages.editors_entity_monitor_add_folder + child.getNodeName() + "'");
                            tabs.add(new TabInfo((DBNDatabaseFolder)child));
                        }
                    }
                }
            } catch (DBException e) {
                log.error("Error initializing entity editor", e); //$NON-NLS-1$
            }
            // Add itself as tab (if it has child items)
            if (node instanceof DBNDatabaseNode) {
                DBNDatabaseNode databaseNode = (DBNDatabaseNode)node;
                List<DBXTreeNode> subNodes = databaseNode.getMeta().getChildren(databaseNode);
                if (subNodes != null) {
                    for (DBXTreeNode child : subNodes) {
                        if (child instanceof DBXTreeItem) {
                            try {
                                if (!((DBXTreeItem)child).isOptional() || databaseNode.hasChildren(monitor, child)) {
                                    monitor.subTask(CoreMessages.editors_entity_monitor_add_node + node.getNodeName() + "'");
                                    tabs.add(new TabInfo((DBNDatabaseNode)node, child));
                                }
                            } catch (DBException e) {
                                log.debug("Can't add child items tab", e); //$NON-NLS-1$
                            }
                        }
                    }
                }
            }
        }
        return tabs;
    }

    private void addContributions(String position)
    {
        EntityEditorsRegistry editorsRegistry = DBeaverCore.getInstance().getEditorsRegistry();
        final DBSObject databaseObject = getEditorInput().getDatabaseObject();
        DBPObject object;
        if (databaseObject instanceof DBSDataSourceContainer && databaseObject.getDataSource() != null) {
            object = databaseObject.getDataSource();
        } else {
            object = databaseObject;
        }
        List<EntityEditorDescriptor> descriptors = editorsRegistry.getEntityEditors(
            object,
            position);
        for (EntityEditorDescriptor descriptor : descriptors) {
            if (descriptor.getType() == EntityEditorDescriptor.Type.editor) {
                addEditorTab(descriptor);
            }
        }
    }

    private boolean addEditorTab(EntityEditorDescriptor descriptor)
    {
        try {
            IEditorPart editor = descriptor.createEditor();
            if (editor == null) {
                return false;
            }
            IEditorInput nestedInput = descriptor.getNestedEditorInput(getEditorInput());
            final Class<? extends IEditorActionBarContributor> contributorClass = descriptor.getContributorClass();
            if (contributorClass != null) {
                addActionsContributor(editor, contributorClass);
            }
            int index = addPage(editor, nestedInput);
            setPageText(index, descriptor.getName());
            if (descriptor.getIcon() != null) {
                setPageImage(index, descriptor.getIcon());
            }
            if (!CommonUtils.isEmpty(descriptor.getDescription())) {
                setPageToolTip(index, descriptor.getDescription());
            }
            editorMap.put(descriptor.getId(), editor);

            if (editor instanceof IFolderedPart) {
                ((IFolderedPart) editor).addFolderListener(folderListener);
            }

            return true;
        } catch (Exception ex) {
            log.error("Error adding nested editor", ex); //$NON-NLS-1$
            return false;
        }
    }

    private void addNodeTab(TabInfo tabInfo)
    {
        try {
            EntityNodeEditor nodeEditor = new EntityNodeEditor(tabInfo.node, tabInfo.meta);
            int index = addPage(nodeEditor, getEditorInput());
            if (tabInfo.meta == null) {
                setPageText(index, tabInfo.node.getNodeName());
                setPageImage(index, tabInfo.node.getNodeIconDefault());
                setPageToolTip(index, getEditorInput().getTreeNode().getNodeType() + " " + tabInfo.node.getNodeName()); //$NON-NLS-1$
            } else {
                setPageText(index, tabInfo.meta.getChildrenType(getDataSource()));
                if (tabInfo.meta.getDefaultIcon() != null) {
                    setPageImage(index, tabInfo.meta.getDefaultIcon());
                } else {
                    setPageImage(index, DBIcon.TREE_FOLDER.getImage());
                }
                setPageToolTip(index, tabInfo.meta.getChildrenType(getDataSource()));
            }
            editorMap.put("node." + tabInfo.getName(), nodeEditor); //$NON-NLS-1$
        } catch (PartInitException ex) {
            log.error("Error adding nested editor", ex); //$NON-NLS-1$
        }
    }

    private void addActionsContributor(IEditorPart editor, Class<? extends IEditorActionBarContributor> contributorClass) throws InstantiationException, IllegalAccessException
    {
        GlobalContributorManager contributorManager = GlobalContributorManager.getInstance();
        IEditorActionBarContributor contributor = contributorManager.getContributor(contributorClass);
        if (contributor == null) {
            contributor = contributorClass.newInstance();
        }
        contributorManager.addContributor(contributor, editor);
        actionContributors.put(editor, contributor);
    }

    @Override
    public void refreshPart(final Object source, boolean force)
    {
        // TODO: make smart content refresh
        // Lists and commands should be refreshed only if we make real refresh from remote storage
        // Otherwise just update object's properties
/*
        getEditorInput().getCommandContext().resetChanges();
*/
        DBSObject databaseObject = getEditorInput().getDatabaseObject();
        if (databaseObject != null && databaseObject.isPersisted()) {
            // Refresh visual content in parts
            for (IEditorPart editor : editorMap.values()) {
                if (editor instanceof IRefreshablePart) {
                    ((IRefreshablePart)editor).refreshPart(source, force);
                }
            }
        }

        setPartName(getEditorInput().getName());
        setTitleImage(getEditorInput().getImageDescriptor());

        if (hasPropertiesEditor) {
            // Update main editor image
            setPageImage(0, getEditorInput().getTreeNode().getNodeIconDefault());
        }
    }

    @Override
    public DBNNode getRootNode() {
        return getEditorInput().getTreeNode();
    }

    @Override
    public Viewer getNavigatorViewer()
    {
        IWorkbenchPart activePart = getActiveEditor();
        if (activePart instanceof INavigatorModelView) {
            return ((INavigatorModelView)activePart).getNavigatorViewer();
        }
        return null;
    }

    @Override
    public Object getAdapter(Class adapter) {
        if (adapter == IPropertySheetPage.class) {
            //return new PropertyPageTabbed();
        }
        IEditorPart activeEditor = getActiveEditor();
        if (activeEditor != null) {
            if (adapter.isAssignableFrom(activeEditor.getClass())) {
                return activeEditor;
            }
            Object result = activeEditor.getAdapter(adapter);
            if (result != null) {
                return result;
            }
        }
        return super.getAdapter(adapter);
    }

    private class ChangesPreviewer implements Runnable {

        private final StringBuilder script;
        private final boolean allowSave;
        private int result;

        public ChangesPreviewer(StringBuilder script, boolean allowSave)
        {
            this.script = script;
            this.allowSave = allowSave;
        }

        @Override
        public void run()
        {
            ViewSQLDialog dialog = new ViewSQLDialog(
                getEditorSite(),
                getDataSource().getContainer(),
                allowSave ? CoreMessages.editors_entity_dialog_persist_title : CoreMessages.editors_entity_dialog_preview_title, 
                script.toString());
            dialog.setShowSaveButton(allowSave);
            dialog.setImage(DBIcon.SQL_PREVIEW.getImage());
            result = dialog.open();
        }

        public int getResult()
        {
            return result;
        }
    }

}
