package util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class XSSF_Tool {

	//シート内の数式を実行
	public static void DoFormura_X(Sheet sheet)
	{
		// 数式を反映
		int Row_top = sheet.getFirstRowNum();
		int Row_last = sheet.getLastRowNum();
		for (int L = Row_top; L <= Row_last; L++) {
			Row row = sheet.getRow(L);
			if (row == null)
				continue;
			int Col_top = row.getFirstCellNum();
			int Col_last = row.getLastCellNum();
			if (Col_top<0)
				continue;
			for (int C = Col_top; C <= Col_last; C++) {
				Cell cell = row.getCell(C);
				if (cell == null)
					continue;
				if (cell.getCellType() == CellType.FORMULA) {
					String str=cell.getCellFormula();
					cell.setCellFormula(str);
				}
			}
		}
	}

	public static int CellNo(String numb) {
		//２桁までサポート
		int len = numb.length() <= 2 ? numb.length() : 2;
		int No = 0;
		for(int i=0; i<len; i++) {
			No = No > 0 ? No * 26 : 0;
			char ch = numb.charAt(i);
			if ((ch >= 'A') && (ch <= 'Z')) {
				No += (ch - 64);
			}
			else if ((ch >= 'a') && (ch <= 'z')) {
				No += (ch - 96);
			}
			else {
				break;
			}
		}
		return No > 0 ? No - 1 : 0;
	}
}
