//    KriolOS POS
//    Copyright (c) 2019-2023 KriolOS
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.

package com.openbravo.data.user;

import com.openbravo.basic.BasicException;
import com.openbravo.pos.forms.AppLocal;
import java.util.*;
import javax.swing.ListModel;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 *
 * @author JG uniCenta
 */
public class BrowsableData<E> implements ListModel<E> {
    
    /**
     *
     */
    protected EventListenerList listeners = new EventListenerList();
    private boolean m_bIsAdjusting;
    
    private ListProvider<E> m_dataprov;      
    private SaveProvider<E> m_saveprov;  
    
    private List<E> m_aData; // List<Object>
    
    private Comparator<E> m_comparer;
    
    /** Creates a new instance of BrowsableData
     * @param dataprov
     * @param saveprov
     * @param c */
    public BrowsableData(ListProvider<E> dataprov, SaveProvider<E> saveprov, Comparator<E> c) {
        m_dataprov = dataprov;
        m_saveprov = saveprov;
        m_comparer = c;
        m_bIsAdjusting = false;
        
        m_aData = new ArrayList<>();
    }

    /**
     *
     * @param dataprov
     * @param saveprov
     */
    public BrowsableData(ListProvider<E> dataprov, SaveProvider<E> saveprov) {
        this(dataprov, saveprov, null);
    }

    /**
     *
     * @param dataprov
     */
    public BrowsableData(ListProvider<E> dataprov) {
        this(dataprov, null, null);
    }    

    @Override
    public final void addListDataListener(ListDataListener l) {
        listeners.add(ListDataListener.class, l);
    }
    @Override
    public final void removeListDataListener(ListDataListener l) {
        listeners.remove(ListDataListener.class, l);
    }

    // Metodos de acceso
    @Override
    public final E getElementAt(int index) {
        return m_aData.get(index);
    }        

    @Override
    public final int getSize() {
        return m_aData.size();
    }   

    /**
     *
     * @return
     */
    public final boolean isAdjusting() {
        return m_bIsAdjusting;
    }
    
    /**
     *
     * @param index0
     * @param index1
     */
    protected void fireDataIntervalAdded(int index0, int index1) {
        m_bIsAdjusting = true;
        EventListener[] l = listeners.getListeners(ListDataListener.class);
        ListDataEvent e = null;
        for (EventListener l1 : l) {
            if (e == null) {
                e = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, index0, index1);
            }
            ((ListDataListener) l1).intervalAdded(e);	       
        }
        m_bIsAdjusting = false;
    }

    /**
     *
     * @param index0
     * @param index1
     */
    protected void fireDataContentsChanged(int index0, int index1) {
        m_bIsAdjusting = true;
        EventListener[] l = listeners.getListeners(ListDataListener.class);
        ListDataEvent e = null;
        for (EventListener l1 : l) {
            if (e == null) {
                e = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, index0, index1);
            }
            ((ListDataListener) l1).contentsChanged(e);	       
        }
        m_bIsAdjusting = false;
    }

    /**
     *
     * @param index0
     * @param index1
     */
    protected void fireDataIntervalRemoved(int index0, int index1) {
        m_bIsAdjusting = true;
        EventListener[] l = listeners.getListeners(ListDataListener.class);
        ListDataEvent e = null;
        for (EventListener l1 : l) {
            if (e == null) {
                e = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, index0, index1);
            }
            ((ListDataListener) l1).intervalRemoved(e);	       
        }
        m_bIsAdjusting = false;
    }
    
    /**
     *
     * @throws BasicException
     */
    public void refreshData() throws BasicException {
        
        putNewData(m_dataprov == null ? null  : m_dataprov.refreshData());
    }    

    /**
     *
     * @throws BasicException
     */
    public void loadData() throws BasicException {
        
        putNewData(m_dataprov == null ? null  : m_dataprov.loadData());
    }

    /**
     *
     * @throws BasicException
     */
    public void unloadData() throws BasicException {
        putNewData(null);
    }    

    /**
     *
     * @param l
     */
    public void loadList(List<E> l) {
        putNewData(l);
    }
    
    /**
     *
     * @param c
     * @throws BasicException
     */
    public void sort(Comparator<E> c) throws BasicException {
        
        Collections.sort(m_aData, c);
        putNewData(m_aData);
    }
    
    /**
     *
     * @return
     */
    public final boolean canLoadData() {
        return m_dataprov != null;
    }

    /**
     *
     * @return
     */
    public boolean canInsertData() {
        return m_saveprov != null && m_saveprov.canInsert();          
    }
    
    /**
     *
     * @return
     */
    public boolean canDeleteData() {
        return m_saveprov != null && m_saveprov.canDelete();      
    }
    
    /**
     *
     * @return
     */
    public boolean canUpdateData() {
        return m_saveprov != null && m_saveprov.canUpdate();      
    }
    
    /**
     *
     * @param index
     * @param f
     * @return
     * @throws BasicException
     */
    public final int findNext(int index, Finder f) throws BasicException {
        int i = index + 1;
        
        // search up to the end of the recordset
        while (i < m_aData.size()) {
            if (f.match(this.getElementAt(i))) {
                return i;
            }
            i++;
        }    
        // search from the begining
        i = 0;
        while (i < index) {
            if (f.match(this.getElementAt(i))) {
                return i;
            }
            i++;
        }    
        
        // No se ha encontrado
        return -1;
    }

    /**
     *
     * @param index
     * @return
     * @throws BasicException
     */
    public final int removeRecord(int index) throws BasicException {
        if (canDeleteData() && index >= 0 && index < m_aData.size()) {
            if (m_saveprov.deleteData(getElementAt(index)) > 0) { 
                // borramos el elemento indicado
                m_aData.remove(index);
                // disparamos los eventos
                fireDataIntervalRemoved(index, index);
                
                int newindex;
                if (index < m_aData.size()) {
                    newindex = index;
                } else {
                    newindex = m_aData.size() - 1;
                }
                return newindex;
            } else {
                throw new BasicException(AppLocal.getIntString("exception.nodelete"));
            }     
        } else {
            // indice no valido
            throw new BasicException(AppLocal.getIntString("exception.nodelete"));
        }
    }
    
    /**
     *
     * @param index
     * @param value
     * @return
     * @throws BasicException
     */
    public final int updateRecord(int index, E value) throws BasicException {
                
        if (canUpdateData() && index >= 0 && index < m_aData.size()) {
            if (m_saveprov.updateData(value) > 0) { 
                // Modificamos el elemento indicado
                int newindex;
                if (m_comparer == null) {
                    newindex = index;
                    m_aData.set(newindex, value);
                } else {
                    // lo movemos
                    newindex = insertionPoint(value);
                    if (newindex == index + 1) {
                        newindex = index;
                        m_aData.set(newindex, value);
                    } else if (newindex > index + 1) {
                        m_aData.remove(index);
                        newindex --;
                        m_aData.add(newindex, value);
                    } else {
                        m_aData.remove(index);
                        m_aData.add(newindex, value);                        
                    }
                }

                if (newindex >= index) {
                    fireDataContentsChanged(index, newindex);
                } else {
                    fireDataContentsChanged(newindex, index);
                }                        
                return newindex;
            } else {
                // fallo la actualizacion
                throw new BasicException(AppLocal.getIntString("exception.noupdate"));
            }
        } else {
            // registro invalido
            throw new BasicException(AppLocal.getIntString("exception.noupdate"));
        }
    }

    /**
     *
     * @param value
     * @return
     * @throws BasicException
     */
    public final int insertRecord(E value) throws BasicException {   
        
        if (canInsertData() && m_saveprov.insertData(value) > 0) { 
            int newindex;
            if (m_comparer == null) {
                // Anadimos el elemento indicado al final...
                newindex = m_aData.size();       
             } else {
                 // lo insertamos en el lugar adecuado
                newindex = insertionPoint(value);
             }
             m_aData.add(newindex, value);
             
            // Disparamos la inserccion
            fireDataIntervalAdded(newindex, newindex);
            return newindex;
        } else {
            throw new BasicException(AppLocal.getIntString("exception.noinsert"));
        }       
    }
    
    private void putNewData(List<E> aData) {
        
        int oldSize = m_aData.size();        
        m_aData = (aData == null) ? new ArrayList<>() : aData;
        int newSize = m_aData.size();
        
        // Ordeno si es un Browsabledata ordenado
        if (m_comparer != null) {
            Collections.sort(m_aData, m_comparer);
        }
        
        fireDataContentsChanged(0, newSize - 1);
        if (oldSize > newSize) {
            fireDataIntervalRemoved(newSize, oldSize - 1);
        } else if (oldSize < newSize) {
            fireDataIntervalAdded(oldSize, newSize - 1);
        }    
    }        
    
    private int insertionPoint(E value) {
        
	int low = 0;
	int high = m_aData.size() - 1;

	while (low <= high) {
	    int mid = (low + high) >> 1;
	    E midVal = m_aData.get(mid);
	    int cmp = m_comparer.compare(midVal, value);

	    if (cmp <= 0) {
		low = mid + 1;
            } else {
		high = mid - 1;
            }
	}
	return low;   
    }
}
