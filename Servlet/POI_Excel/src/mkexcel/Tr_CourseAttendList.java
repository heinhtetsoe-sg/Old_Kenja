package mkexcel;

import java.sql.ResultSet;
import java.sql.SQLException;
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
import beans.CourseAttendListLine;

////受講履歴一覧
public class Tr_CourseAttendList {
	Log log = LogFactory.getLog(Tr_CourseAttendList.class);
	// メンバー変数
	private Workbook book;
	private Alp_Properties Prop;
	private String errorMessage = "";
	final static int MAX_LINE = 20;
	// 書込みデータ
	String schoolId = "";
	String schoolName = "";	//学校名
	String staffCode = "";	//職員コード
	String staffName = "";	//職員名
	String position = "";	//職名
	List<CourseAttendListLine> lines = new ArrayList<>();

	public Tr_CourseAttendList(Workbook book, String information, Alp_Properties prop) {
		// TODO 自動生成されたコンストラクター・スタブ
		this.book = book;
		String[] Information = information.split(",");
		if (Information.length < 2) {
			errorMessage = String.format("パラメータエラー{%s}", information);
		} else {
			schoolId = Information[0];
			staffCode = Information[1];
		}
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
		/*schoolName = "麹町高等学校";
		//staffCode = "00999999";
		staffName = "首都宇　千代";
		position = "事務職";
		for(int i = 0; i < 24; i++) {
			CourseAttendListLine line = new CourseAttendListLine("平成25年", "簿記研修", "仕分け", 6, "2014年12月3日、2014年12月4日", "認定");
			lines.add(line);
		}*/
		// データベース操作オブジェクト生成
		util.DaoUtil dao = new util.DaoUtil(Prop.getProperty("sqllogging")
				.equalsIgnoreCase("true"), log);
		stmt = dao.open(JDBC_LOOKUP);
		if (stmt == null) {
			// オープン失敗
			errorMessage = "データベース接続失敗";
			log.error("dao.open() error");
			return false;
		}
		//
		try {
			String sqlS = "SELECT FORMAL_SCHOOL_NAME FROM V_SCHOOL_BASIS WHERE SCHOOLID='" + schoolId + "' ORDER BY START_DATE DESC LIMIT 1";
			String sqlN = "SELECT S.STAFFNAME, J.JOBNAME FROM STAFF_MST S LEFT JOIN JOB_MST J ON J.JOBCD = S.JOBCD WHERE S.STAFFCD='" + staffCode + "'";
			String sqlL = "SELECT TA.YEAR, TI.TRAINING_NAME, TI.ENFORCEMENT_DAYS, TT.TRAINING_TYPE_NAME, CM.ITEM1 AS CERTIFICATION_TYPE, TI.ENFORCEMENT_DATE_1, TI.ENFORCEMENT_DATE_2, TI.ENFORCEMENT_DATE_3, TI.ENFORCEMENT_DATE_4, TI.ENFORCEMENT_DATE_5, TI.ENFORCEMENT_DATE_6, TI.ENFORCEMENT_DATE_7, TI.ENFORCEMENT_DATE_8, TI.ENFORCEMENT_DATE_9, TI.ENFORCEMENT_DATE_10, TI.ENFORCEMENT_DATE_11, TI.ENFORCEMENT_DATE_12, TI.ENFORCEMENT_DATE_13, TI.ENFORCEMENT_DATE_14, TI.ENFORCEMENT_DATE_15, TI.ENFORCEMENT_DATE_16, TI.ENFORCEMENT_DATE_17, TI.ENFORCEMENT_DATE_18, TI.ENFORCEMENT_DATE_19, TI.ENFORCEMENT_DATE_20    FROM SAF_TRAINING_APPLICATION TA, SAF_TRAINING_INFOMATION TI, SAF_TRAINING_TYPE TT, SAF_ATTENDANCE_CERTIFICATION AC, SAF_CODE_MASTER CM"
					 + " WHERE TI.YEAR = TA.YEAR AND TI.TRAINING_ID = TA.TRAINING_ID AND TI.TRAINING_TYPE_CODE = TA.TRAINING_TYPE_CODE AND TT.YEAR = TI.YEAR AND TT.TRAINING_TYPE_CODE = TI.TRAINING_TYPE_CODE AND AC.YEAR = TA.YEAR AND AC.TRAINING_TYPE_CODE = TA.TRAINING_TYPE_CODE AND AC.TRAINING_ID = TA.TRAINING_ID AND AC.ID  = TA.ID AND AC.STAFFCD = TA.STAFFCD AND CM.CODE_ID = '0063' AND CM.CODE = AC.CERTIFICATION_FLAG AND TA.STAFFCD = '" + staffCode + "'";
			//学校名取出し
			rs = dao.query(sqlS);
			if (errorMessage.length() > 0) { // sql例外発生
				return false;
			}
			if (rs.next() == true) {
				schoolName = rs.getString("FORMAL_SCHOOL_NAME");
			}
			//職員の氏名と職名取出し
			rs = dao.query(sqlN);
			if (errorMessage.length() > 0) { // sql例外発生
				return false;
			}
			if (rs.next() == true) {
				staffName = rs.getString("STAFFNAME");
				position = rs.getString("JOBNAME");
			}
			//受講履歴取出し
			rs = dao.query(sqlL);
			//log.info(sql);
			errorMessage = dao.getErrTrace();
			if (errorMessage.length() > 0) { // sql例外発生
				return false;
			}
			while (rs.next()) {
				String nen = rs.getString("YEAR") + "年";
				String Type = rs.getString("TRAINING_TYPE_NAME");
				String className = rs.getString("TRAINING_NAME");
				int dayCount = rs.getInt("ENFORCEMENT_DAYS");
				String certficate = rs.getString("CERTIFICATION_TYPE");
				String enforcement = "";
				for(int i = 0; i <20; i++) {
					String item = String.format("ENFORCEMENT_DATE_%d", i + 1);
					String str = rs.getString(item);
					if ((str != null) && (str.length() > 0)) {
						String addstr = Enforcement_date(str);
						if (enforcement.length() > 0) {
							enforcement += ("," + addstr);
						}
						else {
							enforcement = addstr;
						}
					}
				}
				CourseAttendListLine line = new CourseAttendListLine(nen, Type, className, dayCount, enforcement, certficate);
				lines.add(line);
			}
			dao.rsClose();
			//クローズ
			dao.close();
			returnCode = setColumn();
		} catch (SQLException e) {
			errorMessage = e.toString();
			dao.StackTraceLogging(e);
		}
		return returnCode;
	}
	private String Enforcement_date(String str) {
		String enforcement = "";
		if (str != null) {
			String[] D = str.split("/");
			if (D.length >= 3) {
				enforcement = String.format("%s年%s月%s日", D[0], D[1], D[2]);
			}
		}
		return enforcement;
	}

	public Boolean setColumn() {
		Row row;
		Cell cel;
		// 書込みシートを開く
		Sheet sheet = book.getSheet("受講履歴一覧表");
		if (sheet == null) {
			errorMessage = "対象のシート見つからず";
			return false;
		}
		SetColumn set = new SetColumn();
		set.setSheet(sheet);
		//学校名
		set.setValueString(3, "D", schoolName);
		//職員コード
		set.setValueString(4, "D", staffCode);
		//職員名
		set.setValueString(5, "D", staffName);
		//職名
		set.setValueString(6, "D", position);
		int count = lines.size();
		if (count > MAX_LINE) {
			CellStyle[] styles = new CellStyle[27];
			CellType[] types = new CellType[27];
			Row orgRow = sheet.getRow(8);
			if (orgRow != null) {
				for(int c = 0; c < 27; c++) {
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
			for(c = start; c < (count + 6); c++) {
				Row row_ = sheet.createRow(c + 1);
				row_.setHeightInPoints(57);
				Cell[] cels = new Cell[27];
				for(int x = 0; x < 27; x++) {
					cels[x] = row_.createCell(x, types[x]);
					cels[x].setCellStyle(styles[x]);
				}
				//log.info("行 "+ c);
				sheet.addMergedRegion(new CellRangeAddress(c + 1, c + 1, 0, 1));
				sheet.addMergedRegion(new CellRangeAddress(c + 1, c + 1, 2, 4));
				sheet.addMergedRegion(new CellRangeAddress(c + 1, c + 1, 5, 11));
				sheet.addMergedRegion(new CellRangeAddress(c + 1, c + 1, 12, 13));
				sheet.addMergedRegion(new CellRangeAddress(c + 1, c + 1, 14, 25));
			}
			int end = sheet.getLastRowNum();
			book.setPrintArea(0, 0, 26, 0, end);
		}
		int[] pos = {0, 2, 5, 12, 14, 26};
		for(int idx = 0; idx < count ; idx++) {
			CourseAttendListLine line = lines.get(idx);
			row = sheet.getRow(idx + 7);
			if (row != null) {
				for(int c = 0; c < pos.length; c++) {
					cel = row.getCell(pos[c]);
					if (cel != null) {
						if (pos[c] == pos[0]) {
							cel.setCellValue(line.getYear());
						}
						else if (pos[c] == pos[1]) {
							cel.setCellValue(line.getLessonType());
						}
						else if (pos[c] == pos[2]) {
							cel.setCellValue(line.getLessonName());
						}
						else if (pos[c] == pos[3]) {
							cel.setCellValue(line.getDayCount());
						}
						else if (pos[c] == pos[4]) {
							cel.setCellValue(line.getLessonDate());
						}
						else if (pos[c] == pos[5]) {
							cel.setCellValue(line.getAccredit());
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
