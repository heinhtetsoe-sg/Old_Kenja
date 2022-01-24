/*
 * (swing1.1beta3)
 * $Id: GroupableTableHeaderUI.java,v 1.5 2002/10/15 15:47:14 tamura Exp $
 */

package jp.gr.java_conf.tame.swing.table;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicTableHeaderUI;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;




/**
 * GroupableTableHeaderUI
 * @author Nobuo Tamemasa
 */
public class GroupableTableHeaderUI extends BasicTableHeaderUI {

	/**
	 * @see javax.swing.plaf.ComponentUI#paint(Graphics, JComponent)
	 */
	public void paint(Graphics g, JComponent c) {
		Rectangle clipBounds = g.getClipBounds();
		if (header.getColumnModel() == null) { return; }
		((GroupableTableHeader) header).setColumnMargin();
		int column = 0;
		Dimension size = header.getSize();
		Rectangle cellRect  = new Rectangle(0, 0, size.width, size.height);
		Hashtable h = new Hashtable();
//		int columnMargin = header.getColumnModel().getColumnMargin();

		Enumeration enumeration = header.getColumnModel().getColumns();
		while (enumeration.hasMoreElements()) {
			cellRect.height = size.height;
			cellRect.y      = 0;
			TableColumn aColumn = (TableColumn) enumeration.nextElement();
			Enumeration cGroups = ((GroupableTableHeader) header).getColumnGroups(aColumn);
			if (cGroups != null) {
				int groupHeight = 0;
				while (cGroups.hasMoreElements()) {
					ColumnGroup cGroup = (ColumnGroup) cGroups.nextElement();
					Rectangle groupRect = (Rectangle) h.get(cGroup);
					if (groupRect == null) {
						groupRect = new Rectangle(cellRect);
						Dimension d = cGroup.getSize(header.getTable());
						groupRect.width  = d.width;
						groupRect.height = d.height;
						h.put(cGroup, groupRect);
					}
					paintCell(g, groupRect, cGroup);
					groupHeight += groupRect.height;
					cellRect.height = size.height - groupHeight;
					cellRect.y      = groupHeight;
				}
			}
//			cellRect.width = aColumn.getWidth() + columnMargin;
			cellRect.width = aColumn.getWidth() /*+ columnMargin*/;
			if (cellRect.intersects(clipBounds)) {
				paintCell(g, cellRect, column);
			}
			cellRect.x += cellRect.width;
			column++;
		}
	}

	/**
	 * Method paintCell.
	 * @param	g			�O���t�B�b�N?
	 * @param	cellRect	�Z����`?
	 * @param	columnIndex	��C���f�b�N�X?
	 */
	private void paintCell(Graphics g, Rectangle cellRect, int columnIndex) {
		TableColumn aColumn = header.getColumnModel().getColumn(columnIndex);
		TableCellRenderer renderer = aColumn.getHeaderRenderer();
		if (null == renderer) {						// �ǉ�
			renderer = header.getDefaultRenderer();		// �ǉ�
		}												// �ǉ�
		Component component = renderer.getTableCellRendererComponent(
				header.getTable(),
				aColumn.getHeaderValue(),
				false,
				false,
				-1,
				columnIndex);
		rendererPane.add(component);
		rendererPane.paintComponent(g, component, header, cellRect.x, cellRect.y,
		cellRect.width, cellRect.height, true);
	}

	/**
	 * Method paintCell.
	 * @param	g			�O���t�B�b�N?
	 * @param	cellRect	�Z����`?
	 * @param	cGroup		��O���[�v?
	 */
	private void paintCell(Graphics g, Rectangle cellRect, ColumnGroup cGroup) {
		TableCellRenderer renderer = cGroup.getHeaderRenderer();
		Component component = renderer.getTableCellRendererComponent(
				header.getTable(),
				cGroup.getHeaderValue(),
				false,
				false,
				-1,
				-1);
		rendererPane.add(component);
		rendererPane.paintComponent(g, component, header, cellRect.x, cellRect.y,
		cellRect.width, cellRect.height, true);
	}

	/**
	 * Method getHeaderHeight.
	 * @return int
	 */
	private int getHeaderHeight() {
		int height = 0;
		TableColumnModel columnModel = header.getColumnModel();
		for (int column = 0; column < columnModel.getColumnCount(); column++) {
			TableColumn aColumn = columnModel.getColumn(column);
			TableCellRenderer renderer = aColumn.getHeaderRenderer();
			if (null == renderer) {						// �ǉ�
				renderer = header.getDefaultRenderer();		// �ǉ�
			}												// �ǉ�
			Component comp = renderer.getTableCellRendererComponent(
					header.getTable(),
					aColumn.getHeaderValue(),
					false,
					false,
					-1,
					column);
			int cHeight = comp.getPreferredSize().height;
			Enumeration enum = ((GroupableTableHeader) header).getColumnGroups(aColumn);
			if (enum != null) {
				while (enum.hasMoreElements()) {
					ColumnGroup cGroup = (ColumnGroup) enum.nextElement();
					cHeight += cGroup.getSize(header.getTable()).height;
				}
			}
			height = Math.max(height, cHeight);
		}
		return height;
	}

	/**
	 * Method createHeaderSize.
	 * @param		width		��?
	 * @return		Dimension
	 */
	private Dimension createHeaderSize(long width) {
		TableColumnModel columnModel = header.getColumnModel();
		width += columnModel.getColumnMargin() * columnModel.getColumnCount();
		if (width > Integer.MAX_VALUE) {
			width = Integer.MAX_VALUE;
		}
		Dimension rtn = new Dimension((int) width, getHeaderHeight());
		return rtn;
	}

	/**
	 * @see javax.swing.plaf.ComponentUI#getPreferredSize(JComponent)
	 */
	public Dimension getPreferredSize(JComponent c) {
		long width = 0;
		Enumeration enumeration = header.getColumnModel().getColumns();
		while (enumeration.hasMoreElements()) {
			TableColumn aColumn = (TableColumn) enumeration.nextElement();
			width = width + aColumn.getPreferredWidth();
		}
		return createHeaderSize(width);
	}
}
