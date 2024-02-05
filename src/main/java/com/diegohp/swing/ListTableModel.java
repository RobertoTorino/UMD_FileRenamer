package com.diegohp.swing;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * This class defines a {@link javax.swing.table.TableModel} based on a {@link java.util.List} as container.
 *
 * @param <O> Class that defines the type of the container list.
 * @author diegohp (Diego Hernandez Perez) - <a href="mailto:hp.diego@gmail.com">hp.diego@gmail.com></a>
 * @version 1.0
 */
public abstract class ListTableModel<O> extends AbstractTableModel {

    /**
     * The Column names.
     */
    protected List<String> columnNames;
    /**
     * The Objects.
     */
    protected List<O> objects;

    /**
     * Instantiates a new List table model.
     */
    public ListTableModel() {
        this.columnNames = new ArrayList<>();
        this.objects = new ArrayList<>();
    }

    /**
     * Add object list.
     *
     * @param objects the objects
     */
    public void addObjectList(List<O> objects) {
        this.objects.addAll(objects);
        this.fireTableDataChanged();
    }

    /**
     * Gets object at.
     *
     * @param row the row
     * @return the object at
     */
    public O getObjectAt(int row) {
        return this.objects.get(row);
    }

    /**
     * Remove all objects.
     */
    public void removeAllObjects() {
        this.objects.clear();
        this.fireTableDataChanged();
    }

    /** {@inheritDoc} */
    @Override
    public int getRowCount() {
        return this.objects.size();
    }

    /** {@inheritDoc} */
    @Override
    public int getColumnCount() {
        return this.columnNames.size();
    }

    /** {@inheritDoc} */
    @Override
    public String getColumnName(int col) {
        return columnNames.get(col);
    }

    /**
     * Sets column names.
     *
     * @param columnNames the column names
     */
    public void setColumnNames(List<String> columnNames) {
        this.columnNames.addAll(columnNames);
    }

    /**
     * Gets objects.
     *
     * @return the objects
     */
    public List<O> getObjects() {
        return this.objects;
    }

    /** {@inheritDoc} */
    @Override
    public abstract Object getValueAt(int row, int col);
}
