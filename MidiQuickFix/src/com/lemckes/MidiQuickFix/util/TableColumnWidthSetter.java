/**************************************************************
 *
 *   MidiQuickFix - A Simple Midi file editor and player
 *
 *   Copyright (C) 2004-2005 John Lemcke
 *   jostle@users.sourceforge.net
 *
 *   This program is free software; you can redistribute it
 *   and/or modify it under the terms of the Artistic License
 *   as published by Larry Wall, either version 2.0,
 *   or (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *   See the Artistic License for more details.
 *
 *   You should have received a copy of the Artistic License with this Kit,
 *   in the file named "Artistic.clarified".
 *   If not, I'll be glad to provide one.
 *
 **************************************************************/
package com.lemckes.MidiQuickFix.util;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

/**
 * A utility to set the widths of a JTable's columns.
 * @version $Id$
 */
public class TableColumnWidthSetter {
    /** This method picks good column sizes.
     * If all column heads are wider than the column's cells'
     * contents, then you can just use column.sizeWidthToFit().
     * @param table The table for which to set column widths.
     * @param longValues An array of objects that represent the longest
     * content of each column. If <CODE>null</CODE> then
     * only the header contents will be used to determine the width.
     * The array may contain <CODE>null</CODE> entries in which case
     * the header contents are used instead. If the array is shorter
     * than the number of columns then the remaining columns are sized
     * by using the header contents.
     */
    static public void setColumnWidths(JTable table, Object[] longValues) {
        TableModel model = table.getModel();
        TableColumn column = null;
        java.awt.Component comp = null;
        int headerWidth = 0;
        int cellWidth = 0;

        TableCellRenderer defaultHeaderRenderer =
            table.getTableHeader().getDefaultRenderer();
        int i = 0;
        java.util.Enumeration e = table.getColumnModel().getColumns();
        while (e.hasMoreElements()) {
            column = (TableColumn)e.nextElement();

            TableCellRenderer headerRenderer = column.getHeaderRenderer();
            if (headerRenderer == null) {
                headerRenderer = defaultHeaderRenderer;
            }
            comp = headerRenderer.getTableCellRendererComponent(
                table, column.getHeaderValue(),
                false, false, 0, 0);
            java.awt.Dimension d = comp.getPreferredSize();
            headerWidth = d.width;

            if (longValues != null && i < longValues.length && longValues[i] != null) {
                comp = table.getDefaultRenderer(model.getColumnClass(i)).
                    getTableCellRendererComponent(
                    table, longValues[i],
                    false, false, 0, i);
                cellWidth = comp.getPreferredSize().width;
            } else {
                cellWidth = 0;
            }

            // The MultiLineHeader seems to have no right margin
            // so we add a little room to stop the text from
            // touching the edge of the header cell.
            int fudgeFactor = 3;
            int w = Math.max(headerWidth, cellWidth) + fudgeFactor;
            column.setPreferredWidth(w);
            ++i;
        }
    }
}
