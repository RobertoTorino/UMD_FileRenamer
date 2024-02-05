package com.diegohp.swing;

import javax.swing.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class implements a {@link javax.swing.ComboBoxModel} based on a {@link java.util.List} as container.
 *
 * @param <O> Class that defines the type of the container list.
 * @author diegohp (Diego Hernandez Perez) - <a href="mailto:hp.diego@gmail.com">hp.diego@gmail.com></a>
 * @version 1.0
 */
public final class ListComboBoxModel<O> extends AbstractListModel<O> implements MutableComboBoxModel<O>, Serializable {

    private final List<O> objects;
    private O selectedObject;

    /**
     * Constructs an empty DefaultComboBoxModel object.
     */
    public ListComboBoxModel() {
        this.objects = new ArrayList<>();
    }

    /** {@inheritDoc} */
    @Override
    public Object getSelectedItem() {
        return selectedObject;
    }

    /**
     * {@inheritDoc}
     *
     * Set the value of the selected item. The selected item may be null.
     */
    @Override
    public void setSelectedItem(Object anObject) {
        if (anObject == null || anObject.getClass().isAssignableFrom(objects.get(0).getClass())) {
            @SuppressWarnings("unchecked")
            O castedObject = (O) anObject;

            if ((selectedObject != null && !selectedObject.equals(castedObject)) ||
                    selectedObject == null && castedObject != null) {
                selectedObject = castedObject;
                fireContentsChanged(this, -1, -1);
            }
        } else {
            throw new IllegalArgumentException("Invalid type for anObject");
        }
    }

    /** {@inheritDoc} */
    @Override
    public int getSize() {
        return objects.size();
    }

    /** {@inheritDoc} */
    @Override
    public O getElementAt(int index) {
        if (index >= 0 && index < objects.size())
            return objects.get(index);
        else
            return null;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public void addElement(Object anObject) {
        objects.add((O) anObject);
        fireIntervalAdded(this, objects.size() - 1, objects.size() - 1);
        if (objects.size() == 1 && selectedObject == null && anObject != null) {
            setSelectedItem(anObject);
        }
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public void insertElementAt(Object anObject, int index) {
        objects.add(index, (O) anObject);
        fireIntervalAdded(this, index, index);
    }

    /** {@inheritDoc} */
    @Override
    public void removeElementAt(int index) {
        if (getElementAt(index) == selectedObject) {
            if (index == 0) {
                setSelectedItem(getSize() == 1 ? null : getElementAt(index + 1));
            } else {
                setSelectedItem(getElementAt(index - 1));
            }
        }

        objects.remove(index);

        fireIntervalRemoved(this, index, index);
    }

    /** {@inheritDoc} */
    @Override
    public void removeElement(Object anObject) {
        for (int i = 0; i < objects.size(); i++) {
            Object listObject = objects.get(i);

            if ((listObject == null && anObject == null) || (listObject != null && listObject.equals(anObject))) {
                removeElementAt(i);
                return;
            }
        }
    }
}
