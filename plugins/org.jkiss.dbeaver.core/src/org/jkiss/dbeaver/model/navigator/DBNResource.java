/*
 * Copyright (c) 2011, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.model.navigator;

import org.jkiss.utils.CommonUtils;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.graphics.Image;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.core.DBeaverCore;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.project.DBPResourceHandler;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.ui.DBIcon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * DBNResource
 */
public class DBNResource extends DBNNode
{
    private IResource resource;
    private DBPResourceHandler handler;
    private List<DBNNode> children;
    private Image resourceImage;

    public DBNResource(DBNNode parentNode, IResource resource, DBPResourceHandler handler)
    {
        super(parentNode);
        this.resource = resource;
        this.handler = handler;
    }

    protected void dispose(boolean reflect)
    {
        this.resource = null;
        this.handler = null;
        if (children != null) {
            for (DBNNode child : children) {
                child.dispose(reflect);
            }
            children = null;
        }
        if (reflect && this.getModel() != null) {
            getModel().fireNodeEvent(new DBNEvent(this, DBNEvent.Action.REMOVE, this));
        }
        super.dispose(reflect);
    }

    public int getFeatures()
    {
        return handler.getFeatures(resource);
    }

    @Override
    public String getNodeType()
    {
        return handler.getTypeName(resource);
    }

    @Property(name = "Name", viewable = true, order = 1)
    public String getNodeName()
    {
        if (resource == null) {
            return null;
        }
        if (resource instanceof IFile) {
            return resource.getFullPath().removeFileExtension().lastSegment();
        }
        return resource.getFullPath().lastSegment();
    }

    public String getNodeDescription()
    {
        return handler.getResourceDescription(getResource());
    }

    @Override
    public Image getNodeIcon()
    {
        if (resourceImage != null) {
            return resourceImage;
        }
/*
        if (resource instanceof IFile) {
            final IContentDescription contentDescription = ((IFile) resource).getContentDescription();
            contentDescription.getContentType().
        }
*/
        if (resource == null) {
            return null;
        }
        switch (resource.getType()) {
            case IResource.FOLDER: return DBIcon.TREE_FOLDER.getImage();
            case IResource.PROJECT: return DBIcon.PROJECT.getImage();
            default: return DBIcon.TREE_PAGE.getImage();
        }
    }

    @Override
    public boolean allowsChildren()
    {
        return resource instanceof IContainer;
    }

    @Override
    public boolean allowsNavigableChildren()
    {
        return allowsChildren();
    }

    @Override
    public List<? extends DBNNode> getChildren(DBRProgressMonitor monitor) throws DBException
    {
        if (children == null) {
            if (resource instanceof IContainer) {
                //final ProjectRegistry projectRegistry = DBeaverCore.getInstance().getProjectRegistry();
                List<DBNNode> result = new ArrayList<DBNNode>();
                try {
                    IResource[] members = ((IContainer) resource).members(IContainer.INCLUDE_HIDDEN);
                    for (IResource member : members) {
                        DBNNode newChild = makeNode(member);
                        if (newChild != null) {
                            result.add(newChild);
                        }
                    }
                } catch (CoreException e) {
                    throw new DBException(e);
                }
                filterChildren(result);
                sortChildren(result);
                this.children = result;
            }
        }
        return children;
    }

    DBNResource getChild(IResource resource)
    {
        if (children == null) {
            return null;
        }
        for (DBNNode child : children) {
            if (child instanceof DBNResource && ((DBNResource)child).getResource().equals(resource)) {
                return (DBNResource) child;
            }
        }
        return null;
    }

    private DBNNode makeNode(IResource resource)
    {
        try {
            if (resource instanceof IFolder && resource.getParent() instanceof IFolder) {
                // Sub folder
                return handler.makeNavigatorNode(this, resource);
            }
            DBPResourceHandler resourceHandler = DBeaverCore.getInstance().getProjectRegistry().getResourceHandler(resource);
            if (resourceHandler == null) {
                log.debug("Skip resource '" + resource.getName() + "'");
                return null;
            }

            return resourceHandler.makeNavigatorNode(this, resource);
        } catch (Exception e) {
            log.error("Error creating navigator node for resource '" + resource.getName() + "'", e);
            return null;
        }
    }

    public boolean isManagable()
    {
        return true;
    }

    @Override
    public DBNNode refreshNode(DBRProgressMonitor monitor, Object source) throws DBException
    {
        try {
            resource.refreshLocal(IResource.DEPTH_INFINITE, monitor.getNestedMonitor());
        } catch (CoreException e) {
            throw new DBException(e);
        }
        return this;
    }

    @Override
    public boolean supportsRename()
    {
        return (getFeatures() & DBPResourceHandler.FEATURE_RENAME) != 0;
    }

    @Override
    public void rename(DBRProgressMonitor monitor, String newName) throws DBException
    {
        try {
            if (resource instanceof IFile) {
                String ext = resource.getFileExtension();
                if (!CommonUtils.isEmpty(ext)) {
                    newName += "." + ext;
                }
            }
            if (!newName.equals(resource.getName())) {
                resource.move(resource.getParent().getFullPath().append(newName), true, monitor.getNestedMonitor());
            }
        } catch (CoreException e) {
            throw new DBException(e);
        }
    }

    public boolean supportsDrop(DBNNode otherNode)
    {
        if (!(resource instanceof IFolder) || (getFeatures() & DBPResourceHandler.FEATURE_MOVE_INTO) == 0) {
            return false;
        }
        if (otherNode == null) {
            // Potentially any other node could be dropped in the folder
            return true;
        }

        // Drop supported only if both nodes are resource with the same handler and DROP feature is supported
        return otherNode instanceof DBNResource
            && otherNode != this
            && otherNode.getParentNode() != this
            && !this.isChildOf(otherNode)
            && ((DBNResource)otherNode).handler == this.handler;
    }

    public void dropNode(DBNNode otherNode) throws DBException
    {
        DBNResource resourceNode = (DBNResource)otherNode;
        try {
            IResource otherResource = resourceNode.getResource();
            otherResource.move(
                resource.getFullPath().append(otherResource.getName()),
                true,
                new NullProgressMonitor());
        } catch (CoreException e) {
            throw new DBException(e);
        }
    }

    public IResource getResource()
    {
        return resource;
    }

    protected void filterChildren(List<DBNNode> list)
    {

    }

    protected void sortChildren(List<DBNNode> list)
    {
        Collections.sort(list, new Comparator<DBNNode>() {
            public int compare(DBNNode o1, DBNNode o2)
            {
                if (o1 instanceof DBNProjectDatabases) {
                    return -1;
                } else if (o2 instanceof DBNProjectDatabases) {
                    return 1;
                } else {
                    if (o1 instanceof DBNResource && o2 instanceof DBNResource) {
                        IResource res1 = ((DBNResource)o1).getResource();
                        IResource res2 = ((DBNResource)o2).getResource();
                        if (res1 instanceof IFolder && !(res2 instanceof IFolder)) {
                            return -1;
                        } else if (res2 instanceof IFolder && !(res1 instanceof IFolder)) {
                            return 1;
                        }
                    }
                    return o1.getNodeName().compareTo(o2.getNodeName());
                }
            }
        });
    }

    public void setResourceImage(Image resourceImage)
    {
        this.resourceImage = resourceImage;
    }

    public void createNewFolder(String folderName)
        throws DBException
    {
        if (resource instanceof IFolder) {
            IFolder newFolder = ((IFolder) resource).getFolder(folderName);
            if (newFolder.exists()) {
                throw new DBException("Folder '" + folderName + "' already exists in '" + resource.getFullPath().toString() + "'");
            }
            try {
                newFolder.create(true, true, new NullProgressMonitor());
            } catch (CoreException e) {
                throw new DBException(e);
            }
        }
    }

    void handleResourceChange(IResourceDelta delta)
    {
        if (delta.getKind() == IResourceDelta.CHANGED) {
            // Update this node in navigator
            getModel().fireNodeEvent(new DBNEvent(delta, DBNEvent.Action.UPDATE, this));
        }
        if (children == null) {
            // Child nodes are not yet read so nothing to change here - just return
            return;
        }
        for (IResourceDelta childDelta : delta.getAffectedChildren()) {
            DBNResource childResource = getChild(childDelta.getResource());
            if (childResource == null) {
                if (childDelta.getKind() == IResourceDelta.ADDED || childDelta.getKind() == IResourceDelta.CHANGED) {
                    // New child or new "grand-child"
                    DBNNode newChild = makeNode(childDelta.getResource());
                    if (newChild != null) {
                        children.add(newChild);
                        sortChildren(children);
                        getModel().fireNodeEvent(new DBNEvent(childDelta, DBNEvent.Action.ADD, newChild));

                        if (childDelta.getKind() == IResourceDelta.CHANGED) {
                            // Notify just created resource
                            // This may happen (e.g.) when first script created in just created script folder
                            childResource = getChild(childDelta.getResource());
                            if (childResource != null) {
                                childResource.handleResourceChange(childDelta);
                            }
                        }
                    }
                } else {
                    //log.warn("Can't find resource '" + childDelta.getResource().getName() + "' in navigator model");
                }
            } else {
                if (childDelta.getKind() == IResourceDelta.REMOVED) {
                    // Node deleted
                    children.remove(childResource);
                    childResource.dispose(true);
                } else {
                    // Node changed - handle it recursive
                    childResource.handleResourceChange(childDelta);
                }
            }
        }
    }

}
