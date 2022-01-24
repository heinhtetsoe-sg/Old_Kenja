package util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class SetColumn {
	private Sheet sheet;

	public SetColumn() {
		// TODO 自動生成されたコンストラクター・スタブ
	}

	public Sheet getSheet() {
		return sheet;
	}

	public void setSheet(Sheet sheet) {
		this.sheet = sheet;
	}

	//セルへ文字列格納
	public Boolean setValueString(int line, String column, String value) {
		Row row;
		Cell cel;
		row = sheet.getRow(line - 1);
		if (row != null) {
			cel = row.getCell(XSSF_Tool.CellNo(column));	//対象セル取出し
			if (cel != null) {
				cel.setCellValue(value);
				return true;
			}
		}
		return false;
	}

	//セルへ数値格納
	public Boolean setValueInt(int line, String column, int value) {
		Row row;
		Cell cel;
		row = sheet.getRow(line - 1);
		if (row != null) {
			cel = row.getCell(XSSF_Tool.CellNo(column));	//対象セル取出し
			if (cel != null) {
				cel.setCellValue(value);
				return true;
			}
		}
		return false;
	}
}
