/*
 * RowHeaderRenderer.java
 * $Id: RowHeaderRenderer.java,v 1.5 2002/10/18 14:20:33 tamura Exp $ 
 */
package jp.gr.java_conf.tame.swing.table;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

/**
 * RowHeaderRenderer
 * @author Nobuo Tamemasa
 * @version 1.0 11/09/98
 */
public class RowHeaderRenderer extends JLabel implements ListCellRenderer, TableCellRenderer {
	/**
	 * コンストラクタ.
	 * @param	table				テーブル
	 */
	public RowHeaderRenderer(JTable table) {
		this(table, CENTER);
	}

	/**
	 * コンストラクタ.
	 * @param	table				テーブル
	 * @param	horizontalAlignment	見出し文字列のアライメント
	 */
	public RowHeaderRenderer(JTable table, int horizontalAlignment) {
		JTableHeader header = table.getTableHeader();
		setOpaque(true);
		setBorder(UIManager.getBorder("TableHeader.cellBorder"));
		setHorizontalAlignment(horizontalAlignment);
		setForeground(header.getForeground());
		setBackground(header.getBackground());
		setFont(header.getFont());
	}

	/**
	 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(
	 *			JTable, Object, boolean, boolean, int, int)
	 */
	public Component getTableCellRendererComponent(
			JTable table,
			Object value,
			boolean isSelected,
			boolean hasFocus,
			int row,
			int column) {
		setText((value == null) ? "" : value.toString());
		return this;
	}

	/**
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(
	 *			JList, Object, int, boolean, boolean)
	 */
	public Component getListCellRendererComponent(
			JList list, 
			Object value,
			int index,
			boolean isSelected,
			boolean cellHasFocus) {
		setText((value == null) ? "" : value.toString());
		return this;
	}
}
