/*
 * DefaultCellAttribute.java
 * (swing1.1beta3)
 * $Id: DefaultCellAttribute.java,v 1.1 2002/10/18 14:22:18 tamura Exp $
 */

package jp.gr.java_conf.tame.swing.table;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

/**
 * DefaultCellAttribute.
 * @author Nobuo Tamemasa
 * @version 1.0 11/22/98
 */
public class DefaultCellAttribute
	implements CellAttribute, CellSpan, ColoredCell, CellFont {

	//
	// !!!! CAUTION !!!!!
	// these values must be synchronized to Table data
	//
	/** 行高さ? */
	protected int rowSize;
	/** カラム幅? */
	protected int columnSize;
	/** CellSpan */
	protected int[][][] span; // CellSpan
	/** ColoredCell */
	protected Color[][] foreground; // ColoredCell
	/** ColoredCell */
	protected Color[][] background; //
	/** CellFont */
	protected Font[][] font; // CellFont

	/**
	 * デフォルト・コンストラクタ
	 */
	public DefaultCellAttribute() {
		this(1, 1);
	}

	/**
	 * コンストラクタ
	 * @param numRows		行数?
	 * @param numColumns	カラム数?
	 */
	public DefaultCellAttribute(int numRows, int numColumns) {
		setSize(new Dimension(numColumns, numRows));
	}

	/**
	 * Method initValue.
	 */
	protected void initValue() {
		for (int i = 0; i < span.length; i++) {
			for (int j = 0; j < span[i].length; j++) {
				span[i][j][CellSpan.COLUMN] = 1;
				span[i][j][CellSpan.ROW] = 1;
			}
		}
	}

	//
	// CellSpan
	//
	/**
	 * @see jp.gr.java_conf.tame.swing.table.CellSpan#getSpan(int, int)
	 */
	public int[] getSpan(int row, int column) {
		if (isOutOfBounds(row, column)) {
			int[] retCode = { 1, 1 };
			return retCode;
		}
		return span[row][column];
	}

	/**
	 * @see jp.gr.java_conf.tame.swing.table.CellSpan#setSpan(int[], int, int)
	 */
	public void setSpan(int[] span, int row, int column) {
		if (isOutOfBounds(row, column)) { return; }
		this.span[row][column] = span;
	}

	/**
	 * @see jp.gr.java_conf.tame.swing.table.CellSpan#isVisible(int, int)
	 */
	public boolean isVisible(int row, int column) {
		if (isOutOfBounds(row, column)) { return false; }
		if ((span[row][column][CellSpan.COLUMN] < 1)
				|| (span[row][column][CellSpan.ROW] < 1)) {
			return false;
		}
		return true;
	}

	/**
	 * @see jp.gr.java_conf.tame.swing.table.CellSpan#combine(int[], int[])
	 */
	public void combine(int[] rows, int[] columns) {
		if (isOutOfBounds(rows, columns)) { return; }
		int rowSpan = rows.length;
		int columnSpan = columns.length;
		int startRow = rows[0];
		int startColumn = columns[0];
		for (int i = 0; i < rowSpan; i++) {
			for (int j = 0; j < columnSpan; j++) {
				if ((span[startRow + i][startColumn + j][CellSpan.COLUMN] != 1)
					|| (span[startRow + i][startColumn + j][CellSpan.ROW] != 1)) {
					//System.out.println("can't combine");
					return;
				}
			}
		}

		for (int i = 0, ii = 0; i < rowSpan; i++, ii--) {
			for (int j = 0, jj = 0; j < columnSpan; j++, jj--) {
				span[startRow + i][startColumn + j][CellSpan.COLUMN] = jj;
				span[startRow + i][startColumn + j][CellSpan.ROW] = ii;
				//System.out.println("r " +ii +"  c " +jj);
			}
		}

		span[startRow][startColumn][CellSpan.COLUMN] = columnSpan;
		span[startRow][startColumn][CellSpan.ROW] = rowSpan;
	}

	/**
	 * @see jp.gr.java_conf.tame.swing.table.CellSpan#split(int, int)
	 */
	public void split(int row, int column) {
		if (isOutOfBounds(row, column)) { return; }
		int columnSpan = span[row][column][CellSpan.COLUMN];
		int rowSpan = span[row][column][CellSpan.ROW];
		for (int i = 0; i < rowSpan; i++) {
			for (int j = 0; j < columnSpan; j++) {
				span[row + i][column + j][CellSpan.COLUMN] = 1;
				span[row + i][column + j][CellSpan.ROW] = 1;
			}
		}
	}

	//
	// ColoredCell
	//
	/**
	 * @see jp.gr.java_conf.tame.swing.table.ColoredCell#getForeground(int, int)
	 */
	public Color getForeground(int row, int column) {
		if (isOutOfBounds(row, column)) {
			return null;
		}
		return foreground[row][column];
	}

	/**
	 * @see jp.gr.java_conf.tame.swing.table.ColoredCell#setForeground(Color, int, int)
	 */
	public void setForeground(Color color, int row, int column) {
		if (isOutOfBounds(row, column)) {
			return;
		}
		foreground[row][column] = color;
	}

	/**
	 * @see jp.gr.java_conf.tame.swing.table.ColoredCell#setForeground(Color, int[], int[])
	 */
	public void setForeground(Color color, int[] rows, int[] columns) {
		if (isOutOfBounds(rows, columns)) {
			return;
		}
		setValues(foreground, color, rows, columns);
	}

	/**
	 * @see jp.gr.java_conf.tame.swing.table.ColoredCell#getBackground(int, int)
	 */
	public Color getBackground(int row, int column) {
		if (isOutOfBounds(row, column)) {
			return null;
		}
		return background[row][column];
	}

	/**
	 * @see jp.gr.java_conf.tame.swing.table.ColoredCell#setBackground(Color, int, int)
	 */
	public void setBackground(Color color, int row, int column) {
		if (isOutOfBounds(row, column)) {
			return;
		}
		background[row][column] = color;
	}

	/**
	 * @see jp.gr.java_conf.tame.swing.table.ColoredCell#setBackground(Color, int[], int[])
	 */
	public void setBackground(Color color, int[] rows, int[] columns) {
		if (isOutOfBounds(rows, columns)) {
			return;
		}
		setValues(background, color, rows, columns);
	}
	//

	//
	// CellFont
	//
	/**
	 * @see jp.gr.java_conf.tame.swing.table.CellFont#getFont(int, int)
	 */
	public Font getFont(int row, int column) {
		if (isOutOfBounds(row, column)) {
			return null;
		}
		return font[row][column];
	}

	/**
	 * @see jp.gr.java_conf.tame.swing.table.CellFont#setFont(Font, int, int)
	 */
	public void setFont(Font font, int row, int column) {
		if (isOutOfBounds(row, column)) {
			return;
		}
		this.font[row][column] = font;
	}

	/**
	 * @see jp.gr.java_conf.tame.swing.table.CellFont#setFont(Font, int[], int[])
	 */
	public void setFont(Font font, int[] rows, int[] columns) {
		if (isOutOfBounds(rows, columns)) {
			return;
		}
		setValues(this.font, font, rows, columns);
	}
	//

	//
	// CellAttribute
	//
	/**
	 * @see jp.gr.java_conf.tame.swing.table.CellAttribute#addColumn()
	 */
	public void addColumn() {
		int[][][] oldSpan = span;
		int numRows = oldSpan.length;
		int numColumns = oldSpan[0].length;
		span = new int[numRows][numColumns + 1][2];
		System.arraycopy(oldSpan, 0, span, 0, numRows);
		for (int i = 0; i < numRows; i++) {
			span[i][numColumns][CellSpan.COLUMN] = 1;
			span[i][numColumns][CellSpan.ROW] = 1;
		}
	}

	/**
	 * @see jp.gr.java_conf.tame.swing.table.CellAttribute#addRow()
	 */
	public void addRow() {
		int[][][] oldSpan = span;
		int numRows = oldSpan.length;
		int numColumns = oldSpan[0].length;
		span = new int[numRows + 1][numColumns][2];
		System.arraycopy(oldSpan, 0, span, 0, numRows);
		for (int i = 0; i < numColumns; i++) {
			span[numRows][i][CellSpan.COLUMN] = 1;
			span[numRows][i][CellSpan.ROW] = 1;
		}
	}

	/**
	 * @see jp.gr.java_conf.tame.swing.table.CellAttribute#insertRow(int)
	 */
	public void insertRow(int row) {
		int[][][] oldSpan = span;
		int numRows = oldSpan.length;
		int numColumns = oldSpan[0].length;
		span = new int[numRows + 1][numColumns][2];
		if (0 < row) {
			System.arraycopy(oldSpan, 0, span, 0, row - 1);
		}

		System.arraycopy(oldSpan, 0, span, row, numRows - row);

		for (int i = 0; i < numColumns; i++) {
			span[row][i][CellSpan.COLUMN] = 1;
			span[row][i][CellSpan.ROW] = 1;
		}
	}

	/**
	 * @see jp.gr.java_conf.tame.swing.table.CellAttribute#getSize()
	 */
	public Dimension getSize() {
		return new Dimension(rowSize, columnSize);
	}

	/**
	 * @see jp.gr.java_conf.tame.swing.table.CellAttribute#setSize(Dimension)
	 */
	public void setSize(Dimension size) {
		columnSize = size.width;
		rowSize = size.height;
		span = new int[rowSize][columnSize][2]; // 2: COLUMN,ROW
		foreground = new Color[rowSize][columnSize];
		background = new Color[rowSize][columnSize];
		font = new Font[rowSize][columnSize];
		initValue();
	}

	/*
	public void changeAttribute(int row, int column, Object command) {
	}
	
	public void changeAttribute(int[] rows, int[] columns, Object command) {
	}
	*/

	/**
	 * Method isOutOfBounds.
	 * @param row		行?
	 * @param column	カラム?
	 * @return	結果?
	 */
	protected boolean isOutOfBounds(int row, int column) {
		if ((row < 0)
			|| (rowSize <= row)
			|| (column < 0)
			|| (columnSize <= column)) {
			return true;
		}

		return false;
	}

	/**
	 * Method isOutOfBounds.
	 * @param rows		行?
	 * @param columns	カラム?
	 * @return	結果?
	 */
	protected boolean isOutOfBounds(int[] rows, int[] columns) {
		for (int i = 0; i < rows.length; i++) {
			if ((rows[i] < 0) || (rowSize <= rows[i])) {
				return true;
			}
		}

		for (int i = 0; i < columns.length; i++) {
			if ((columns[i] < 0) || (columnSize <= columns[i])) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Method setValues.
	 * @param target	ターゲット?
	 * @param value	新しい値?
	 * @param rows		行?
	 * @param columns	カラム?
	 */
	protected void setValues(
		Object[][] target,
		Object value,
		int[] rows,
		int[] columns) {
		for (int i = 0; i < rows.length; i++) {
			int row = rows[i];
			for (int j = 0; j < columns.length; j++) {
				int column = columns[j];
				target[row][column] = value;
			}
		}
	}
}
