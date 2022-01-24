package mkexcel;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import util.Alp_Properties;
import util.SetColumn;
import beans.AttendCheckListLine;

//研修講座申込者チェックリスト
//	Excel出力
public class Tr_AttendCheckList {
	Log log = LogFactory.getLog(Tr_AttendCheckList.class);
	// メンバー変数
	private Workbook book;
	private Alp_Properties Prop;
	private String errorMessage = "";
	final static int MAX_LINE = 20;
	// 書込みデータ
	String capacity;	//定員
	String aplication;	//申込
	String attending;	//受講
	String lessonName;	//講座名
	String lessonNo;	//講座番号
	String lessonDate;	//実施日
	String createDate;	//作成日
	List<AttendCheckListLine> lines = new ArrayList<>();

	public Tr_AttendCheckList(Workbook book, String information, Alp_Properties prop) {
		// TODO 自動生成されたコンストラクター・スタブ
		this.book = book;
		String[] Information = information.split(",");
		this.Prop = prop;
	}

	// エラーメッセージ取出し
	public String getErrorMessage() {
		return errorMessage;
	}

	// Excelシートヘ書込み
	public Boolean excel() {
		log.info("Excel出力{" + this.getClass().getName() + "}");
		// 戻り値初期値はfalse
		boolean returnCode = false;
		//データベースリソース
		final String JDBC_LOOKUP = Prop.getProperty("jdbc.lookup");
		// フィールドデータをデータベースから取出す
		Statement stmt = null;
		ResultSet rs = null;
		//タイトル
		capacity = "40人";
		aplication = "18人";
		attending = "18人";
		createDate = "2018/10/12 09:35作成";
		lessonName = "小学校音楽科Ⅱ講座【授業】";
		lessonNo = "2151";
		lessonDate = "2018年10月25日";
		//行データ
		int no = 0;
		for(int i = 0; i < 24; i++) {
			AttendCheckListLine line = new AttendCheckListLine("○", "東部", ++no, "赤松小", "教諭", "赤井　繁", "あかい　しげる", "男", "今回初めて");
			lines.add(line);
		}
		//
		returnCode = setColumn();
		return returnCode;
	}

	public Boolean setColumn() {
		Row row;
		Cell cel;
		// 書込みシートを開く
		Sheet sheet = book.getSheet("申込者チェックリスト");
		if (sheet == null) {
			errorMessage = "対象のシート見つからず";
			return false;
		}
		SetColumn set = new SetColumn();
		set.setSheet(sheet);
		//定員
		set.setValueString(1, "C", capacity);
		//申込
		set.setValueString(2, "C", aplication);
		//受講
		set.setValueString(3, "C", attending);
		//作成日
		set.setValueString(4, "K", createDate);
		//講座名
		set.setValueString(5, "D", lessonName);
		//講座番号
		set.setValueString(6, "D", lessonNo);
		//実施日
		set.setValueString(7, "D", lessonDate);
		int count = lines.size();
		if (count > MAX_LINE) {
			CellStyle[] styles = new CellStyle[11];
			CellType[] types = new CellType[11];
			Row orgRow = sheet.getRow(8);
			if (orgRow != null) {
				for(int c = 0; c < 11; c++) {
					styles[c] = book.createCellStyle();
					styles[c].cloneStyleFrom(orgRow.getCell(c).getCellStyle());
					types[c] = orgRow.getCell(c).getCellType();
				}
			}
			else {
				errorMessage = "行の追加エラー";
				return false;
			}
			int c, start = sheet.getLastRowNum();
			//log.info("開始行:" + sheet.getLastRowNum());
			for(c = start; c < (count + 7); c++) {
				Row row_ = sheet.createRow(c + 1);
				row_.setHeightInPoints(30);
				Cell[] cels = new Cell[11];
				for(int x = 0; x < 11; x++) {
					cels[x] = row_.createCell(x, types[x]);
					cels[x].setCellStyle(styles[x]);
				}
				//log.info("行 "+ c);
				sheet.addMergedRegion(new CellRangeAddress(c + 1, c + 1, 8, 10));
			}
			//log.info("行追加 " + sheet.getLastRowNum());
		}
		for(int idx = 0; idx < count ; idx++) {
			AttendCheckListLine line = lines.get(idx);
			row = sheet.getRow(idx + 8);
			if (row != null) {
				for(int c = 0; c < 9; c++) {
					cel = row.getCell(c);
					if (cel != null) {
						switch( c ) {
						case 0:
							cel.setCellValue(line.getLesson());
							break;
						case 1:
							cel.setCellValue(line.getDistrict());
							break;
						case 2:
							cel.setCellValue(line.getNoumber());
							break;
						case 3:
							cel.setCellValue(line.getSchoolName());
							break;
						case 4:
							cel.setCellValue(line.getPosition());
							break;
						case 5:
							cel.setCellValue(line.getName());
							break;
						case 6:
							cel.setCellValue(line.getPhonetic());
							break;
						case 7:
							cel.setCellValue(line.getGender());
							break;
						case 8:
							cel.setCellValue(line.getComment());;
							break;
						}
					}
				}
			}
		}

		//
		//算出セルを実行
		int rowStart = sheet.getFirstRowNum();
		int rowEnd = sheet.getLastRowNum();
		for(int i = rowStart; i <= rowEnd; i++) {
			row = sheet.getRow(i);
			int cellStart = row.getFirstCellNum() >= 0 ? row.getFirstCellNum() : 0;
			int cellEnd = row.getLastCellNum();
			//log.info(i + "開始[" + row.getFirstCellNum() + "] 終了[" + row.getLastCellNum() + "]");
			for(int l = cellStart; l <= cellEnd; l++) {
				cel = row.getCell(l);
                if (cel != null) {
                	if (cel.getCellType() == CellType.FORMULA) {
                		String str = cel.getCellFormula();
                		//log.info("FORMULA：" + str);
                		cel.setCellFormula(str);
                	}
                }
			}
		}

		return true;
	}
}
