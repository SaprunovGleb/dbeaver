/*
 * Copyright (c) 2011, Serge Rieder and others. All Rights Reserved.
 */

/*
 * Created on Jul 13, 2004
 */
package org.jkiss.dbeaver.ext.erd.model;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.model.struct.DBSTable;

import java.util.*;

/**
 * Represents a Schema in the model. Note that this class also includes
 * diagram specific information (layoutManualDesired and layoutManualAllowed fields)
 * although ideally these should be in a separate model hierarchy
 * @author Serge Rieder
 */
public class EntityDiagram extends ERDObject<DBSObject>
{

	private String name;
	private List<ERDTable> tables = new ArrayList<ERDTable>();
	private boolean layoutManualDesired = true;
	private boolean layoutManualAllowed = false;
    private Map<DBSTable, ERDTable> tableMap = new IdentityHashMap<DBSTable, ERDTable>();
    private Map<ERDObject, Rectangle> initBounds = new IdentityHashMap<ERDObject, Rectangle>();
    private List<ERDNote> notes = new ArrayList<ERDNote>();
    private boolean needsAutoLayout;

    public EntityDiagram(DBSObject container, String name)
	{
		super(container);
		if (name == null)
			throw new NullPointerException("Name cannot be null");
		this.name = name;
	}

	public synchronized void addTable(ERDTable table, boolean reflect)
	{
        addTable(table, -1, reflect);
	}

	public synchronized void addTable(ERDTable table, int i, boolean reflect)
	{
        if (i < 0) {
            tables.add(table);
        } else {
		    tables.add(i, table);
        }
        tableMap.put(table.getObject(), table);

        if (reflect) {
		    firePropertyChange(CHILD, null, table);
/*
            for (ERDAssociation rel : table.getPrimaryKeyRelationships()) {
                table.firePropertyChange(INPUT, null, rel);
            }
            for (ERDAssociation rel : table.getForeignKeyRelationships()) {
                table.firePropertyChange(OUTPUT, null, rel);
            }
*/
        }

        resolveRelations(reflect);

        if (reflect) {
            for (ERDAssociation rel : table.getPrimaryKeyRelationships()) {
                rel.getForeignKeyTable().firePropertyChange(OUTPUT, null, rel);
            }
        }
	}

    private void resolveRelations(boolean reflect)
    {
        // Resolve incomplete relations
        for (ERDTable erdTable : getTables()) {
            erdTable.resolveRelations(tableMap, reflect);
        }
    }

	public synchronized void removeTable(ERDTable table, boolean reflect)
	{
        tableMap.remove(table.getObject());
		tables.remove(table);
        if (reflect) {
		    firePropertyChange(CHILD, table, null);
        }
	}

    /**
	 * @return the Tables for the current schema
	 */
	public synchronized List<ERDTable> getTables()
	{
		return tables;
	}

    public synchronized List<ERDNote> getNotes()
    {
        return notes;
    }

    public synchronized void addNote(ERDNote note, boolean reflect)
    {
        notes.add(note);

        if (reflect) {
            firePropertyChange(CHILD, null, note);
        }
    }

    public synchronized void removeNote(ERDNote note, boolean reflect)
    {
        notes.remove(note);

        if (reflect) {
            firePropertyChange(CHILD, note, null);
        }
    }

    /**
	 * @return the name of the schema
	 */
	public String getName()
	{
		return name;
	}

    public void setName(String name)
    {
        this.name = name;
    }

	/**
	 * @param layoutManualAllowed
	 *            The layoutManualAllowed to set.
	 */
	public void setLayoutManualAllowed(boolean layoutManualAllowed)
	{
		this.layoutManualAllowed = layoutManualAllowed;
	}

	/**
	 * @return Returns the layoutManualDesired.
	 */
	public boolean isLayoutManualDesired()
	{
		return layoutManualDesired;
	}

	/**
	 * @param layoutManualDesired
	 *            The layoutManualDesired to set.
	 */
	public void setLayoutManualDesired(boolean layoutManualDesired)
	{
		this.layoutManualDesired = layoutManualDesired;
	}

	/**
	 * @return Returns whether we can lay out individual tables manually using the XYLayout
	 */
	public boolean isLayoutManualAllowed()
	{
		return layoutManualAllowed;
	}

    public int getEntityCount() {
        return tables.size();
    }

    public EntityDiagram copy()
    {
        EntityDiagram copy = new EntityDiagram(getObject(), getName());
        copy.tables.addAll(this.tables);
        copy.tableMap.putAll(this.tableMap);
        copy.layoutManualDesired = this.layoutManualDesired;
        copy.layoutManualAllowed = this.layoutManualAllowed;
        copy.initBounds = initBounds;
        return copy;
    }

    public void fillTables(DBRProgressMonitor monitor, Collection<DBSTable> tables, DBSObject dbObject)
    {
        // Load entities
        monitor.beginTask("Load tables metadata", tables.size());
        for (DBSTable table : tables) {
            if (monitor.isCanceled()) {
                break;
            }
            monitor.subTask("Load " + table.getName());
            ERDTable erdTable = ERDTable.fromObject(monitor, table);
            erdTable.setPrimary(table == dbObject);

            addTable(erdTable, false);
            tableMap.put(table, erdTable);

            monitor.worked(1);
        }

        monitor.done();

        // Load relations
        monitor.beginTask("Load tables' relations", tables.size());
        for (DBSTable table : tables) {
            if (monitor.isCanceled()) {
                break;
            }
            monitor.subTask("Load " + table.getName());
            final ERDTable erdTable = tableMap.get(table);
            if (erdTable != null) {
                erdTable.addRelations(monitor, tableMap, false);
            }
            monitor.worked(1);
        }
        monitor.done();
    }

    public boolean containsTable(DBSTable table)
    {
        for (ERDTable erdTable : tables) {
            if (erdTable.getObject() == table) {
                return true;
            }
        }
        return false;
    }

    public Map<DBSTable,ERDTable> getTableMap()
    {
        return tableMap;
    }

    public ERDTable getERDTable(DBSTable table)
    {
        return tableMap.get(table);
    }

    public void clear()
    {
        this.tables.clear();
        this.tableMap.clear();
        this.initBounds.clear();
    }

    public Rectangle getInitBounds(ERDObject erdObject)
    {
        return initBounds.get(erdObject);
    }

    public void addInitBounds(ERDObject erdTable, Rectangle bounds)
    {
        initBounds.put(erdTable, bounds);
    }

    public boolean isNeedsAutoLayout()
    {
        return needsAutoLayout;
    }

    public void setNeedsAutoLayout(boolean needsAutoLayout)
    {
        this.needsAutoLayout = needsAutoLayout;
    }

    public void addInitRelationBends(ERDTable sourceTable, ERDTable targetTable, String relName, List<Point> bends)
    {
        for (ERDAssociation rel : sourceTable.getPrimaryKeyRelationships()) {
            if (rel.getForeignKeyTable() == targetTable && relName.equals(rel.getObject().getName())) {
                rel.setInitBends(bends);
            }
        }
    }

    public List<?> getContents()
    {
        List<Object> children = new ArrayList<Object>(tables.size() + notes.size());
        children.addAll(tables);
        children.addAll(notes);
        return children;
    }

}