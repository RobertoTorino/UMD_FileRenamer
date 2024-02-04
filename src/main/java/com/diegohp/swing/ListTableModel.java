
package com.diegohp.swing;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * This class defines a {@link TableModel} based on a {@link List} as container.
 * @param <O> Class that defines the type of the container list.
 * @author diegohp (Diego Hernandez Perez) - <a href="mailto:hp.diego@gmail.com">hp.diego@gmail.com>
 * @version 1.0
 */
public abstract class ListTableModel<O> extends AbstractTableModel {

    protected List<String> columnNames;
    protected List<O> objects;

    public ListTableModel() {
        this.columnNames = new ArrayList<>();
        this.objects = new ArrayList<>();
    }

    public void addObjectList(List<O> objects) {
        this.objects.addAll(objects);
        this.fireTableDataChanged();
    }

    public O getObjectAt(int row) {
        return this.objects.get(row);
    }

    public void removeAllObjects() {
        this.objects.clear();
        this.fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return this.objects.size();
    }

    @Override
    public int getColumnCount() {
        return this.columnNames.size();
    }

    @Override
    public String getColumnName(int col) {
        return columnNames.get(col);
    }

    public void setColumnNames(List<String> columnNames){
        this.columnNames.addAll(columnNames);
    }

    public List<O> getObjects(){
        return this.objects;
    }

    @Override
    public abstract Object getValueAt(int row, int col);
}
