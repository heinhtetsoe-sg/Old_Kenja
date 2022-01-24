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

	/** �Z������? */
	protected CellAttribute cellAtt;

	/**
	 * �f�t�H���g�E�R���X�g���N�^
	 */
	public AttributiveCellTableModel() {
		this((Vector)null, 0);
	}

	/**
	 * �R���X�g���N�^
	 * @param	numRows		�s��?
	 * @param	numColumns	�J������?
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
	 * �R���X�g���N�^
	 * @param	columnNames	�J������?
	 * @param	numRows		�s��?
	 */
	public AttributiveCellTableModel(Vector columnNames, int numRows) {
		setColumnIdentifiers(columnNames);
		dataVector = new Vector();
		setNumRows(numRows);
		cellAtt = new DefaultCellAttribute(numRows,columnNames.size());
	}

	/**
	 * �R���X�g���N�^
	 * @param	columnNames	�J������?
	 * @param	numRows		�s��?
	 */
	public AttributiveCellTableModel(Object[] columnNames, int numRows) {
		this(convertToVector(columnNames), numRows);
	}

	/**
	 * �R���X�g���N�^
	 * @param	data		�f�[�^?
	 * @param	columnNames	�J������?
	 */
	public AttributiveCellTableModel(Vector data, Vector columnNames) {
		setDataVector(data, columnNames);
	}

	/**
	 * �R���X�g���N�^
	 * @param	data		�f�[�^?
	 * @param	columnNames	�J������?
	 */
	public AttributiveCellTableModel(Object[][] data, Object[] columnNames) {
		setDataVector(data, columnNames);
	}

	/**
	 * �f�[�^�ݒ�?
	 * @param	newData		�V�����f�[�^?
	 * @param	columnNames	�J������?
	 * @throws	IllegalArgumentException	newData��null
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
	 * �J�����̒ǉ�?
	 * @param	columnName	�ǉ�����J������?
	 * @param	columnData	�ǉ�����J�����̃f�[�^?
	 * @throws	IllegalArgumentException	columnName��null
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
	 * �s��ǉ�?
	 * @param	rowData		�ǉ�����s�̃f�[�^
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
	 * �s�̑}��?
	 * @param	row			�ǉ�����s�̈ʒu?
	 * @param	rowData		�ǉ�����s�̃f�[�^?
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
	 * �Z�������̎擾?
	 * @return	�Z������?
	 */
	public CellAttribute getCellAttribute() {
		return cellAtt;
	}

	/**
	 * �Z�������̐ݒ�?
	 * @param	newCellAtt	�Z������?
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

