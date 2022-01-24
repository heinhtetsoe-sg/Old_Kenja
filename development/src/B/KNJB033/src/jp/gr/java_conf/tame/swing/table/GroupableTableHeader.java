/*
 * GroupableTableHeader.java
 * (swing1.1beta3)
 * $Id: GroupableTableHeader.java,v 1.5 2002/10/18 14:20:33 tamura Exp $
 */

package jp.gr.java_conf.tame.swing.table;

import java.util.Enumeration;
import java.util.Vector;

import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;



/**
 * GroupableTableHeader
 *
 * @author Nobuo Tamemasa
 * @version 1.0 10/20/98
 */

public class GroupableTableHeader extends JTableHeader {
	/** uiClassID? */
//	private static final String UI_CLASS_ID = "GroupableTableHeaderUI";

	/** カラム・グループ? */
	protected Vector _columnGroups = null;

	/**
	 * @see javax.swing.table.JTableHeader#JTableHeader(TableColumnModel)
	 */
	public GroupableTableHeader(TableColumnModel model) {
		super(model);
		setUI(new GroupableTableHeaderUI());
		setReorderingAllowed(false);
	}

	/**
	 * @see javax.swing.table.JTableHeader#setReorderingAllowed(boolean)
	 */
	public void setReorderingAllowed(boolean b) {
		reorderingAllowed = false;
	}

	/**
	 * Method addColumnGroup.
	 * @param		g		カラム・グループ?
	 */
	public void addColumnGroup(ColumnGroup g) {
		if (_columnGroups == null) {
			_columnGroups = new Vector();
		}
		_columnGroups.addElement(g);
	}

	/**
	 * Method getColumnGroups.
	 * @param	col			カラム?
	 * @return Enumeration
	 */
	public Enumeration getColumnGroups(TableColumn col) {
		if (_columnGroups == null) { return null; }
		Enumeration enum = _columnGroups.elements();
		while (enum.hasMoreElements()) {
			ColumnGroup cGroup = (ColumnGroup) enum.nextElement();
			Vector vRet = (Vector) cGroup.getColumnGroups(col, new Vector());
			if (vRet != null) {
				return vRet.elements();
			}
		}
		return null;
	}

	/**
	 * Method setColumnMargin.
	 */
	public void setColumnMargin() {
		if (_columnGroups == null) { return; }
		int columnMargin = getColumnModel().getColumnMargin();
		Enumeration enum = _columnGroups.elements();
		while (enum.hasMoreElements()) {
			ColumnGroup cGroup = (ColumnGroup) enum.nextElement();
			cGroup.setColumnMargin(columnMargin);
		}
	}
}
