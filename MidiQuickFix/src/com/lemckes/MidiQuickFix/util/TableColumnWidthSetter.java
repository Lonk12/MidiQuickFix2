/**************************************************************
 *
 *   MidiQuickFix - A Simple Midi file editor and player
 *
 *   Copyright (C) 2004-2021 John Lemcke
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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

/**
 * A utility to set the widths of a JTable's columns.
 */
public class TableColumnWidthSetter {
    /** This method picks good column sizes.
     * If all column heads are wider than the column's cells'
     * contents, then you can just use column.sizeWidthToFit().
     * calls { @link setTableColumnWidths(JTable, Object[], boolean)}
     * with a value of false.
     * @param table The table for which to set column widths.
     * @param longValues An array of objects which represent the longest
     * content of each column. If <CODE>null</CODE> then
     * only the header contents will be used to determine the width.
     * The array may contain <CODE>null</CODE> entries in which case
     * the header contents are used instead. If the array is shorter
     * than the number of columns then the remaining columns are sized
     * by using the header contents.
     */
    static public void setColumnWidths(JTable table, Object[] longValues) {
        setColumnWidths(table, longValues, false);
    }

    /** This method picks good column sizes.
     * If all column heads are wider than the column's cells'
     * contents, then you can just use column.sizeWidthToFit().
     * @param table The table for which to set column widths.
     * @param longValues An array of objects which represent the longest
     * content of each column. If <CODE>null</CODE> then
     * only the header contents will be used to determine the width.
     * The array may contain <CODE>null</CODE> entries in which case
     * the header contents are used instead. If the array is shorter
     * than the number of columns then the remaining columns are sized
     * by using the header contents.
     * @param maxLayout If set to true and the table has a parent,
     * then increase column widths to fill the parent if needed.
     */
    static public void setColumnWidths(JTable table, Object[] longValues,
                                       boolean maxLayout) {
        TableModel model = table.getModel();
        Component comp = null;
        int totalWidthOfColumns = 0;

        for (int i = 0; i < table.getColumnModel().getColumnCount(); ++i) {
            int headerWidth = 0;
            int cellWidth = 0;
            int editorWidth = 0;
            TableColumn column = table.getColumnModel().getColumn(i);
            ////////////////////////////////////////////////////
            // Find the width of the column header.
            //
            TableCellRenderer headerRenderer = column.getHeaderRenderer();
            if (headerRenderer == null) {
                headerRenderer = table.getTableHeader().getDefaultRenderer();
            }
            comp = headerRenderer.getTableCellRendererComponent(
                table, column.getHeaderValue(),
                false, false, -1, 0);
            Dimension d = comp.getPreferredSize();
            headerWidth = d.width;

            ////////////////////////////////////////////////////
            // Find the width of the column's cell renderer.
            //
            if (longValues != null &&
                i < longValues.length &&
                longValues[i] != null) {
                TableCellRenderer cellRenderer = column.getCellRenderer();
                if (cellRenderer == null) {
                    cellRenderer =
                        table.getDefaultRenderer(model.getColumnClass(i));
                }
                comp = cellRenderer.getTableCellRendererComponent(
                    table, longValues[i],
                    false, false, 0, i);
                cellWidth = comp.getPreferredSize().width;
            } else {
                cellWidth = 0;
            }

            ////////////////////////////////////////////////////
            // Find the width of the column's cell editor.
            //
            if (longValues != null &&
                i < longValues.length &&
                longValues[i] != null) {
                TableCellEditor cellEditor = column.getCellEditor();
                if (cellEditor == null) {
                    cellEditor =
                        table.getDefaultEditor(model.getColumnClass(i));
                }
                comp = cellEditor.getTableCellEditorComponent(
                    table, longValues[i],
                    false, 0, i);
                if (comp != null) {
                    editorWidth = comp.getPreferredSize().width;
                }
            } else {
                editorWidth = 0;
            }

            // The MultiLineHeader seems to have no right margin
            // so we add a little room to stop the text from
            // touching the edge of the header cell.
            int fudgeFactor = 3;

            ////////////////////////////////////////////////////
            // Set the width to the maximum of the 3 widths.
            //
            int colWidth =
                Math.max(Math.max(headerWidth, cellWidth), editorWidth) +
                fudgeFactor;
            column.setPreferredWidth(colWidth);
            column.setWidth(colWidth);
            totalWidthOfColumns += column.getPreferredWidth();
        }

        Container parent = table.getParent();
        if (maxLayout && parent != null) {
            final int columnCount = table.getColumnCount();
            int insetwidth =
                parent.getInsets().left + parent.getInsets().right;
            // There is an intercell space between each column and
            // before the first column and after the last column.
            // Hence (columnCount + 1)
            int requiredWidth =
                (columnCount + 1) * table.getIntercellSpacing().width +
                totalWidthOfColumns + insetwidth;

            if (parent.getWidth() > requiredWidth) {
                // WHY '+ columnCount + 1'??
                int remainingWidth =
                    parent.getWidth() - requiredWidth + columnCount + 1;

                for (int i = 0; i < columnCount; ++i) {
                    int give = remainingWidth / (columnCount - i);
                    remainingWidth -= give;
                    TableColumn col = table.getColumnModel().getColumn(i);
                    col.setPreferredWidth(col.getPreferredWidth() + give);
                }
                if (columnCount > 0) {
                    assert remainingWidth == 0 : remainingWidth;
                }
            }
        }
    }

    /**Set the tables column widths. Reads all cells of the table that return
     * the column class <code>String.class</code>,
     * and picks the longest string to pass to
     * {@link #setColumnWidths(JTable, Object[], boolean) }
     * @param table The table for which to set column widths.
     * @param maxValue If set to true and the table has a parent, then increase column widths
     * to fill the parent if needed.
     */
    static public void setColumnWidths(JTable table, boolean maxValue) {
        TableModel model = table.getModel();
        Object[] longestVals = new Object[model.getColumnCount()];
        for (int col = 0; col < longestVals.length; ++col) {
            Object longest = null;
            int maxWidth = 0;
            for (int row = 0; row < table.getRowCount(); ++row) {
                Object val = model.getValueAt(row, col);
                Component renderComp =
                    table.getDefaultRenderer(model.getColumnClass(col)).
                    getTableCellRendererComponent(
                    table, val,
                    false, false, 0, col);
                int cellWidth = renderComp.getPreferredSize().width;

                Component editorComp =
                    table.getDefaultEditor(model.getColumnClass(col)).
                    getTableCellEditorComponent(
                    table, val,
                    false, 0, col);
                int editorWidth = editorComp.getPreferredSize().width;

                // Pick the widest
                cellWidth = Math.max(cellWidth, editorWidth);
                if (cellWidth > maxWidth) {
                    maxWidth = cellWidth;
                    longest = val;
                }
            }
            longestVals[col] = longest;
        }
        setColumnWidths(table, longestVals, maxValue);
    }
}
