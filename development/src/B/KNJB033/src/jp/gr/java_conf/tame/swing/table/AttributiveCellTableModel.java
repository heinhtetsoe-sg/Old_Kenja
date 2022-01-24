/*
 * AttributiveCellTableModel.java
 * $Id: AttributiveCellTableModel.java,v 1.1 2002/10/18 14:22:18 tamura Exp $
 */

package jp.gr.java_conf.tame.swing.table;

import java.awt.Dimension;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;


/**
 * AttributiveCellTableModel.
 * @author Nobuo Tamemasa
 * @version 1.0 11/22/98
 */

public class AttributiveCellTableModel extends DefaultTableModel {

	/** セル属性? */
	protected CellAttribute cellAtt;

	/**
	 * デフォルト・コンストラクタ
	 */
	public AttributiveCellTableModel() {
		this((Vector)null, 0);
	}

	/**
	 * コンストラクタ
	 * @param	numRows		行数?
	 * @param	numColumns	カラム数?
	 */
	public AttributiveCellTableModel(int numRows, int numColumns) {
		Vector names = new Vector(numColumns);
		names.setSize(numColumns);
		setColumnIdentifiers(names);
		dataVector = new Vector();
		setNumRows(numRows);
		cellAtt = new DefaultCellAttribute(numRows,numColumns);
	}

	/**
	 * コンストラクタ
	 * @param	columnNames	カラム名?
	 * @param	numRows		行数?
	 */
	public AttributiveCellTableModel(Vector columnNames, int numRows) {
		setColumnIdentifiers(columnNames);
		dataVector = new Vector();
		setNumRows(numRows);
		cellAtt = new DefaultCellAttribute(numRows,columnNames.size());
	}

	/**
	 * コンストラクタ
	 * @param	columnNames	カラム名?
	 * @param	numRows		行数?
	 */
	public AttributiveCellTableModel(Object[] columnNames, int numRows) {
		this(convertToVector(columnNames), numRows);
	}

	/**
	 * コンストラクタ
	 * @param	data		データ?
	 * @param	columnNames	カラム名?
	 */
	public AttributiveCellTableModel(Vector data, Vector columnNames) {
		setDataVector(data, columnNames);
	}

	/**
	 * コンストラクタ
	 * @param	data		データ?
	 * @param	columnNames	カラム名?
	 */
	public AttributiveCellTableModel(Object[][] data, Object[] columnNames) {
		setDataVector(data, columnNames);
	}

	/**
	 * データ設定?
	 * @param	newData		新しいデータ?
	 * @param	columnNames	カラム名?
	 * @throws	IllegalArgumentException	newDataがnull
	 */
	public void setDataVector(Vector newData, Vector columnNames) {
		if (newData == null) {
			throw new IllegalArgumentException("setDataVector() - Null parameter");
		}
		dataVector = new Vector(0);
		setColumnIdentifiers(columnNames);
		dataVector = newData;

		//
		cellAtt = new DefaultCellAttribute(dataVector.size(), columnIdentifiers.size());

		newRowsAdded(new TableModelEvent(this, 0, getRowCount()-1,
		TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
	}

	/**
	 * カラムの追加?
	 * @param	columnName	追加するカラム名?
	 * @param	columnData	追加するカラムのデータ?
	 * @throws	IllegalArgumentException	columnNameがnull
	 */
	public void addColumn(Object columnName, Vector columnData) {
		if (columnName == null) {
			throw new IllegalArgumentException("addColumn() - null parameter");
		}
		columnIdentifiers.addElement(columnName);
		int index = 0;
		Enumeration enumeration = dataVector.elements();
		while (enumeration.hasMoreElements()) {
			Object value;
			if ((columnData != null) && (index < columnData.size())) {
				value = columnData.elementAt(index);
			} else {
				value = null;
			}
			((Vector)enumeration.nextElement()).addElement(value);
			index++;
		}

		//
		cellAtt.addColumn();

		fireTableStructureChanged();
	}

	/**
	 * 行を追加?
	 * @param	rowData		追加する行のデータ
	 */
	public void addRow(Vector rowData) {
		Vector newData = null;
		if (rowData == null) {
			newData = new Vector(getColumnCount());
		} else {
			rowData.setSize(getColumnCount());
		}
		dataVector.addElement(newData);

		//
		cellAtt.addRow();

		newRowsAdded(new TableModelEvent(this, getRowCount()-1, getRowCount()-1, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
	}

	/**
	 * 行の挿入?
	 * @param	row			追加する行の位置?
	 * @param	rowData		追加する行のデータ?
	 */
	public void insertRow(int row, Vector rowData) {
		if (rowData == null) {
			rowData = new Vector(getColumnCount());
		} else {
			rowData.setSize(getColumnCount());
		}

		dataVector.insertElementAt(rowData, row);

		//
		cellAtt.insertRow(row);

		newRowsAdded(new TableModelEvent(this, row, row, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
	}

	/**
	 * セル属性の取得?
	 * @return	セル属性?
	 */
	public CellAttribute getCellAttribute() {
		return cellAtt;
	}

	/**
	 * セル属性の設定?
	 * @param	newCellAtt	セル属性?
	 */
	public void setCellAttribute(CellAttribute newCellAtt) {
		int numColumns = getColumnCount();
		int numRows    = getRowCount();
		if ((newCellAtt.getSize().width  != numColumns) ||
				(newCellAtt.getSize().height != numRows)) {
			newCellAtt.setSize(new Dimension(numRows, numColumns));
		}
		cellAtt = newCellAtt;
		fireTableDataChanged();
	}

	/*
	public void changeCellAttribute(int row, int column, Object command) {
		cellAtt.changeAttribute(row, column, command);
	}

	public void changeCellAttribute(int[] rows, int[] columns, Object command) {
		cellAtt.changeAttribute(rows, columns, command);
	}
	*/
}

