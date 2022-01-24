/*
 * (swing1.1beta3)
 * $Id: ColumnGroup.java,v 1.4 2002/10/15 13:23:01 tamura Exp $
 */
package jp.gr.java_conf.tame.swing.table;

import java.awt.Component;
import java.awt.Dimension;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;


/**
 * ColumnGroup
 *
 * @version 1.0 10/20/98
 * @author Nobuo Tamemasa
 */
public class ColumnGroup {
	/** セル・レンダラー? */
	protected TableCellRenderer _renderer;
	/** ベクター? */
	protected Vector _v;
	/** テキスト? */
	protected String _text;
	/** マージン? */
	protected int _margin = 0;

	/**
	 * コンストラクタ
	 * @param	text		カラム名
	 */
	public ColumnGroup(String text) {
		this(null, text);
	}

	/**
	 * コンストラクタ
	 * @param	renderer	レンダラー
	 * @param	text		カラム名
	 */
	public ColumnGroup(TableCellRenderer renderer, String text) {
		if (renderer == null) {
			this._renderer = new DefaultTableCellRenderer() {
				public Component getTableCellRendererComponent(
							JTable table,
							Object value,
							boolean isSelected,
							boolean hasFocus,
							int row,
							int column) {
					JTableHeader header = table.getTableHeader();
					if (header != null) {
						setForeground(header.getForeground());
						setBackground(header.getBackground());
						setFont(header.getFont());
					}
					setHorizontalAlignment(JLabel.CENTER);
					setText((value == null) ? "" : value.toString());
					setBorder(UIManager.getBorder("TableHeader.cellBorder"));
					return this;
				}
			};
		} else {
			this._renderer = renderer;
		}

		this._text = text;
		_v = new Vector();
	}


	/**
	 * @param obj    TableColumn or ColumnGroup
	 */
	public void add(Object obj) {
		if (obj == null) { return; }
		_v.addElement(obj);
	}


	/**
	 * Method getColumnGroups.
	 * @param c	TableColumn
	 * @param g    ColumnGroups
	 * @return Vector
	 */
	public Vector getColumnGroups(TableColumn c, Vector g) {
		g.addElement(this);
		if (_v.contains(c)) { return g; }
		Enumeration enum = _v.elements();
		while (enum.hasMoreElements()) {
			Object obj = enum.nextElement();
			if (obj instanceof ColumnGroup) {
				Vector groups = (Vector) ((ColumnGroup) obj).getColumnGroups(c, (Vector) g.clone());
				if (groups != null) { return groups; }
			}
		}
		return null;
	}

	/**
	 * Method getHeaderRenderer.
	 * @return TableCellRenderer
	 */
	public TableCellRenderer getHeaderRenderer() {
		return _renderer;
	}

	/**
	 * Method setHeaderRenderer.
	 * @param renderer	セル・レンダラー?
	 */
	public void setHeaderRenderer(TableCellRenderer renderer) {
		if (renderer != null) {
			this._renderer = renderer;
		}
	}

	/**
	 * Method getHeaderValue.
	 * @return Object
	 */
	public Object getHeaderValue() {
		return _text;
	}

	/**
	 * Method getSize.
	 * @param table		テーブル?
	 * @return Dimension
	 */
	public Dimension getSize(JTable table) {
		Component comp = _renderer.getTableCellRendererComponent(
					table,
					getHeaderValue(),
					false,
					false,
					-1,
					-1);
		int height = comp.getPreferredSize().height;
		int width  = 0;
		Enumeration enum = _v.elements();
		while (enum.hasMoreElements()) {
			Object obj = enum.nextElement();
			if (obj instanceof TableColumn) {
				TableColumn aColumn = (TableColumn) obj;
				width += aColumn.getWidth();
				width += _margin;
			} else {
				width += ((ColumnGroup) obj).getSize(table).width;
			}
		}
		return new Dimension(width, height);
	}

	/**
	 * Method setColumnMargin.
	 * @param margin	マージン?
	 */
	public void setColumnMargin(int margin) {
		this._margin = margin;
		Enumeration enum = _v.elements();
		while (enum.hasMoreElements()) {
			Object obj = enum.nextElement();
			if (obj instanceof ColumnGroup) {
				((ColumnGroup) obj).setColumnMargin(margin);
			}
		}
	}
}
