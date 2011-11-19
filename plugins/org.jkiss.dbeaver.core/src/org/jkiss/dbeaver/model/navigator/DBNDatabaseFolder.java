/*
 * Copyright (c) 2011, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.model.navigator;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSFolder;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.model.struct.DBSWrapper;
import org.jkiss.dbeaver.registry.tree.DBXTreeFolder;
import org.jkiss.dbeaver.registry.tree.DBXTreeNode;
import org.jkiss.utils.CommonUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * DBNDatabaseFolder
 */
public class DBNDatabaseFolder extends DBNDatabaseNode implements DBNContainer, DBSFolder
{
    private DBXTreeFolder meta;

    DBNDatabaseFolder(DBNNode parent, DBXTreeFolder meta)
    {
        super(parent);
        this.meta = meta;
        if (this.getModel() != null) {
            this.getModel().addNode(this);
        }
    }

    protected void dispose(boolean reflect)
    {
        if (this.getModel() != null) {
            this.getModel().removeNode(this, reflect);
        }
        super.dispose(reflect);
    }

    public DBXTreeFolder getMeta()
    {
        return meta;
    }

    @Override
    protected void reloadObject(DBRProgressMonitor monitor, DBSObject object) {
        // do nothing
    }

    public DBSObject getObject()
    {
        return this;
    }

    public Object getValueObject()
    {
        return getParentNode() instanceof DBNDatabaseNode ? ((DBNDatabaseNode)getParentNode()).getValueObject() : null;
    }

    public String getChildrenType()
    {
        final List<DBXTreeNode> metaChildren = meta.getChildren(this);
        if (CommonUtils.isEmpty(metaChildren)) {
            return "?";
        } else {
            return metaChildren.get(0).getChildrenType(getDataSource());
        }
    }

    @Property(name = "Name", viewable = true)
    public String getName()
    {
        return meta.getChildrenType(getDataSource());
    }

    public String getDescription()
    {
        return meta.getDescription();
    }

    public DBSObject getParentObject()
    {
        return getParentNode() instanceof DBSWrapper ? ((DBSWrapper)getParentNode()).getObject() : null;
    }

    public DBPDataSource getDataSource()
    {
        return getParentObject() == null ? null : getParentObject().getDataSource();
    }

    public boolean isPersisted()
    {
        return getParentNode() != null && getParentNode().isPersisted();
    }

    public Class<? extends DBSObject> getChildrenClass()
    {
        String itemsType = CommonUtils.toString(meta.getType());
        if (CommonUtils.isEmpty(itemsType)) {
            return null;
        }
        Class<DBSObject> aClass = meta.getSource().getObjectClass(itemsType, DBSObject.class);
        if (aClass == null) {
            log.error("Items class '" + itemsType + "' not found");
            return null;
        }
        if (!DBSObject.class.isAssignableFrom(aClass)) {
            log.error("Class '" + aClass.getName() + "' doesn't extend DBSObject");
            return null;
        }
        return aClass ;
    }

    public Collection<DBSObject> getChildrenObjects(DBRProgressMonitor monitor) throws DBException
    {
        List<DBNDatabaseNode> children = getChildren(monitor);
        List<DBSObject> childObjects = new ArrayList<DBSObject>();
        if (!CommonUtils.isEmpty(children)) {
            for (DBNDatabaseNode child : children) {
                childObjects.add(child.getObject());
            }
        }
        return childObjects;
    }
}
