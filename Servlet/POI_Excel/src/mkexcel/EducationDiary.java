package mkexcel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import util.Alp_Properties;
import util.XSSF_Tool;

//教務日誌
public class EducationDiary {
	Log log = LogFactory.getLog(EducationDiary.class);
	// メンバー変数
	private Workbook book;
	private Alp_Properties Prop;
	private String errorMessage = "";
	private String schoolId = "";
	private String targetmonth = "";
	private String year = "";
	final static int MAX_YEAR = 4;
	final static int MAX_CLASS = 11;
	final static int MAX_ITEM = 9;
	final static int MAX_POSITION = 6;
	// 書込みデータ
	// 日付と天気、学校名
	String SchoolName = "";
	String date = "";
	String weather = "";
	String week = "";
	int weekCount = 0;
	String remark = "";
	//決裁職名
	String[] decision = new String[MAX_POSITION];
	int decisionCount = 0;
	//授業日数
	String[][] lessons = new String[MAX_YEAR][2]; //学年名・授業有無
	int[] lessontotalday = new int[MAX_YEAR];	//授業日数
	//在籍生徒数
	String existStudent = "";
	String[] gradeName = new String[MAX_YEAR];
	//クラスごとの在籍数
	String[][] className = new String[MAX_YEAR][MAX_CLASS];
	int[][][] y1n = new int[MAX_YEAR][MAX_CLASS][MAX_ITEM];
	int[] maxClass = new int[MAX_YEAR];
	//行事
	String event = "";
	//今日の連絡
	String liaison = "";
	//旅行
	String trip_ = "";
	//休暇
	String rest_ = "";

	public EducationDiary(Workbook book, String information, Alp_Properties prop) {
		// TODO 自動生成されたコンストラクター・スタブ
		this.book = book;
		String[] Information = information.split(",");
		if (Information.length < 2) {
			errorMessage = String.format("パラメータエラー{%s}", information);
		} else {
			schoolId = Information[0];
			targetmonth = Information[1];
			String[] dates = targetmonth.split("-");
			if (dates.length != 3) {
				errorMessage = String.format("パラメータエラー{%s}", information);
			}
			else {
				if (Information.length >= 3) {
					year = Information[2];
				}
				else {
					year = dates[0];
				}
			}
		}
		this.Prop = prop;
		log.info("EducationDiaryコンストラクタ{" + information + "}");
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
		// データベース操作オブジェクト生成
		util.DaoUtil dao = new util.DaoUtil(Prop.getProperty("sqllogging")
				.equalsIgnoreCase("true"), log);
		// データベースオープン
		stmt = dao.open(JDBC_LOOKUP);
		if (stmt == null) {
			// オープン失敗
			errorMessage = "データベース接続失敗";
			log.error("dao.open() error");
			return false;
		}
		try {
			// 日付と天気、学校名
			String sqlT = "SELECT t2.SCHOOLNAME, t1.REC_DATE, t1.WEATHER, t1.WEEKNO, t1.REMARK FROM SAF_SCHOOL_JOURNAL_DAT t1 left join SAF_SCHOOL_MST t2 on t1.SCHOOLID = t2.SCHOOLID WHERE t1.REC_DATE = '"
					+ targetmonth + "' AND t1.SCHOOLID = '" + schoolId + "' ";
			rs = dao.query(sqlT);
			errorMessage = dao.getErrTrace();
			if (errorMessage.length() > 0) { // sql例外発生
				return false;
			}
			if (rs.next() == false) {
				dao.close();
				return false;
			}
			SchoolName = rs.getString("SCHOOLNAME");
			java.sql.Date qDate = rs.getDate("REC_DATE");
			Calendar s_cl = Calendar.getInstance();
			s_cl.setTime(qDate);
			date = Prop.getJDayString(s_cl);
			weather = rs.getString("WEATHER");
			remark = rs.getString("REMARK");
			weekCount = rs.getInt("WEEKNO");
			dao.rsClose();
			//決裁職名取出し
			String SqlD = "SELECT JOBNAME FROM SAF_SANCTION_JOB_C_DAT WHERE SCHOOLID = '" + schoolId + "' ORDER BY ORDERNO";
			rs = dao.query(SqlD);
			errorMessage = dao.getErrTrace();
			if (errorMessage.length() > 0) { // sql例外発生
				return false;
			}
			decisionCount = 0;
			while (dao.Next()) {
				decision[decisionCount++] = rs.getString("JOBNAME");
				if (decisionCount >= MAX_POSITION) {
					break;
				}
			}
			for(int Count = decisionCount; Count < MAX_POSITION; Count++) {
				decision[Count] = "";
			}
			dao.rsClose();
			//授業日数取得
			String sqlDay = "SELECT a2.GRADE, a2.GRADE_NAME1,a2.SCHOOLDAY, VALUE(a3.TOTAL_SCHOOLDAY, '0') as TOTAL_SCHOOLDAY"
					+ " FROM  SAF_SCHOOL_JOURNAL_DAT a1 left join SAF_SCHOOL_JOURNAL_GRADE_DAT a2 on a1.ID = a2.JOURNAL_ID"
					+ " left join (SELECT t2.GRADE, COUNT(SCHOOLDAY) as TOTAL_SCHOOLDAY FROM SAF_SCHOOL_JOURNAL_DAT t1 left join SAF_SCHOOL_JOURNAL_GRADE_DAT t2 on t1.ID = t2.JOURNAL_ID"
					+ " WHERE SCHOOLID  = '" + schoolId + "' AND REC_DATE between '" + year + "-04-01' AND '" + targetmonth + "' AND SCHOOLDAY = '1' GROUP BY t2.GRADE) a3 on a2.GRADE = a3.GRADE"
					+ " WHERE a1.REC_DATE = '" + targetmonth + "'";
			rs = dao.query(sqlDay);
			for(int Count = 0; Count < MAX_YEAR; Count++) {	//初期化
				lessons[Count][0] = lessons[Count][1] = "";
				lessontotalday[Count] = 0;
			}
			while( rs.next() ) {
				String grade = rs.getString("GRADE");
				int gradeid = grade == null ? -1 : Integer.parseInt(grade) - 1;
				//log.info("GRADE{"+grade+"}["+gradeid+"]");
				if ((gradeid >=0) && (gradeid < MAX_YEAR)) {
					lessons[gradeid][0] = rs.getString("GRADE_NAME1");
					lessons[gradeid][1] = rs.getInt("SCHOOLDAY") == 1 ? "有" : "無" ;
					lessontotalday[gradeid] = rs.getInt("TOTAL_SCHOOLDAY");
				}
			}
			dao.rsClose();
			//在籍生徒数
			String sqlS = "SELECT a1.GRADE, a2.GRADE_NAME1, a1.MALE_CNT, a1.FEMALE_CNT FROM (SELECT JOURNAL_ID, GRADE, SUM(MALE_CNT) as MALE_CNT, SUM(FEMALE_CNT) as FEMALE_CNT FROM SAF_SCHOOL_JOURNAL_CLASS_DAT WHERE JOURNAL_ID = (SELECT ID FROM SAF_SCHOOL_JOURNAL_DAT WHERE SCHOOLID = '"
					+ schoolId + "' AND REC_DATE = '"+ targetmonth + "' ) GROUP BY JOURNAL_ID, GRADE ) a1 left join SAF_SCHOOL_JOURNAL_GRADE_DAT a2 on a1.JOURNAL_ID = a2.JOURNAL_ID and a1.GRADE = a2.GRADE ORDER BY a1.GRADE ";
			rs = dao.query(sqlS);
			errorMessage = dao.getErrTrace();
			if (errorMessage.length() > 0) { // sql例外発生
				return false;
			}
			int[] male = new int[MAX_YEAR];
			int[] female = new int[MAX_YEAR];
			int[] grade = new int[MAX_YEAR];
			int total = 0;
			int count = 0;
			while( rs.next() ) {
				grade[count] = Integer.valueOf(rs.getString("GRADE"));
				male[count] = rs.getInt("MALE_CNT");
				total += male[count];
				female[count] = rs.getInt("FEMALE_CNT");
				total += female[count];
				gradeName[count] = grade[count] + "学年";
				if (++count >= MAX_YEAR) {
					break;
				}
			}
			if (count <= 3) {
				existStudent = String.format("在籍生徒数　%d学年　男%d　女%d　%d学年　男%d　女%d　%d学年　男%d　女%d　　総計%d"
						, grade[0], male[0], female[0], grade[1], male[1], female[1], grade[2], male[2], female[2], total);
			}
			else {
				existStudent = String.format("在籍生徒数　%d学年 男%d 女%d　%d学年 男%d 女%d　%d学年 男%d 女%d　%d学年 男%d 女%d 　総計%d"
						, grade[0], male[0], female[0], grade[1], male[1], female[1], grade[2], male[2], female[2], grade[3], male[3], female[3], total);
			}
			dao.rsClose();
			//クラスごとの在籍数
			String sqlN = "SELECT GRADE, HR_CLASS, HR_NAMEABBV, MALE_CNT + FEMALE_CNT as ALL_CNT, LEAVE_CNT, ABROAD_CNT, AUTHORIZED_CNT, ABSENCE_CNT, MOURNING_CNT, SUSPEND_CNT, LATE_CNT, EARLY_CNT FROM SAF_SCHOOL_JOURNAL_CLASS_DAT WHERE JOURNAL_ID = (SELECT ID FROM SAF_SCHOOL_JOURNAL_DAT WHERE SCHOOLID = '"
					+ schoolId + "' AND REC_DATE = '"+ targetmonth + "' ) ORDER BY GRADE, HR_CLASS";
			rs = dao.query(sqlN);
			errorMessage = dao.getErrTrace();
			if (errorMessage.length() > 0) { // sql例外発生
				return false;
			}
			maxClass[0] = maxClass[1] = maxClass[2] = maxClass[3] = 0;
			while( rs.next() ) {
				int grade_ = Integer.valueOf(rs.getString("GRADE"));
				int class_ = Integer.valueOf(rs.getString("HR_CLASS"));
				if ((grade_ > 0) && (grade_ <= MAX_YEAR) && (class_ > 0) && (class_ <= MAX_CLASS)) {
					className[grade_ - 1][class_ - 1] = rs.getString("HR_NAMEABBV") != null ? rs.getString("HR_NAMEABBV") : "";
					y1n[grade_ - 1][class_ - 1][0] = rs.getInt("ALL_CNT");
					y1n[grade_ - 1][class_ - 1][1] = rs.getInt("LEAVE_CNT");
					y1n[grade_ - 1][class_ - 1][2] = rs.getInt("ABROAD_CNT");
					y1n[grade_ - 1][class_ - 1][3] = rs.getInt("AUTHORIZED_CNT");
					y1n[grade_ - 1][class_ - 1][4] = rs.getInt("ABSENCE_CNT");
					y1n[grade_ - 1][class_ - 1][5] = rs.getInt("MOURNING_CNT");
					y1n[grade_ - 1][class_ - 1][6] = rs.getInt("SUSPEND_CNT");
					y1n[grade_ - 1][class_ - 1][7] = rs.getInt("LATE_CNT");
					y1n[grade_ - 1][class_ - 1][8] = rs.getInt("EARLY_CNT");
					if (maxClass[grade_ - 1] < class_) {
						maxClass[grade_ - 1] = class_;
					}
				}
			}
			dao.rsClose();
			//行事
			String sql1 = "SELECT EVENT_NAME FROM SAF_EVENT_DAT WHERE SCHOOLID = '" + schoolId + "' AND EVENT_DATE = '"+ targetmonth + "' ORDER BY START_TIME";
			rs = dao.query(sql1);
			errorMessage = dao.getErrTrace();
			if (errorMessage.length() > 0) { // sql例外発生
				return false;
			}
			while (rs.next()) {
				if (event.length() == 0) {
					event = rs.getString("EVENT_NAME");
				}
				else {
					event += ("\n" + rs.getString("EVENT_NAME"));
				}
			}
			dao.rsClose();
			//今日の連絡
			String sql2 = "SELECT LIAISON FROM SAF_LIAISON_DAT WHERE SCHOOLID = '" + schoolId + "' AND LIAISON_DATE = '"+ targetmonth + "' AND DELETE_FLG = '0' ORDER BY UPDATED";
			rs = dao.query(sql2);
			errorMessage = dao.getErrTrace();
			if (errorMessage.length() > 0) { // sql例外発生
				return false;
			}
			while (rs.next()) {
				if (liaison.length() == 0) {
					liaison = rs.getString("LIAISON");
				}
				else {
					liaison += ("\n" + rs.getString("LIAISON"));
				}
			}
			dao.rsClose();
			//旅行
			String sql3 = "SELECT DISTINCT t1.STAFFCD, t3.STAFFNAME_SHOW,  case when t2.OPEN_FLG = '0' then '旅行' else t2.TRIP_REASON end as TRIP_REASON, case when t2.OPEN_FLG = '0' then NULL else t2.TRIP_DEST end as TRIP_DEST, t2.START_DATE, case when t2.END_DATE is null then t2.START_DATE else t2.END_DATE end as END_DATE "
					+ " FROM SAF_TRIP_DATE_DAT t1  left join SAF_TRIP_DAT t2 on t1.TRIP_ID = t2.ID  left join STAFF_MST t3 on t1.STAFFCD = t3.STAFFCD WHERE t1.SCHOOLID = '"
					+ schoolId + "' AND t1.TRIP_DATE = '" + targetmonth + "' AND t2.DELETE_FLG = '0' ORDER BY t1.STAFFCD";
			rs = dao.query(sql3);
			errorMessage = dao.getErrTrace();
			if (errorMessage.length() > 0) { // sql例外発生
				return false;
			}
			int tripCount = 0;
			trip_ = "・旅行";
			while (rs.next()) {
				String tripReason = rs.getString("TRIP_REASON") != null ? rs.getString("TRIP_REASON") : "";
				String tripDest = rs.getString("TRIP_DEST") != null ? rs.getString("TRIP_DEST") : "";
				String tripTerm = getTerm(rs.getString("START_DATE"), rs.getString("END_DATE"));
				if (tripReason.length() > 0) {
					trip_ += ("\n" + tripReason);
				}
				if (tripDest.length() > 0) {
					trip_ += (" " + tripDest);
				}
				trip_ += (" " + rs.getString("STAFFNAME_SHOW"));
				trip_ += tripTerm;
				tripCount++;
			}
			if (tripCount == 0) {
				trip_ += "なし\n";
			}
			else {
				trip_ += "\n";
			}
			dao.rsClose();
			//休暇
			String sql4 = "SELECT DISTINCT t1.STAFFCD, t3.STAFFNAME_SHOW FROM SAF_VACATION_DATE_DAT t1 left join SAF_VACATION_DAT t2 on t1.VACATION_ID = t2.ID left join STAFF_MST t3 on t1.STAFFCD = t3.STAFFCD WHERE t1.VACATION_DATE = '"
					+ targetmonth + "' AND t1.SCHOOLID = '" + schoolId + "' AND t2.DELETE_FLG = '0' ORDER BY t1.STAFFCD";
			rs = dao.query(sql4);
			errorMessage = dao.getErrTrace();
			if (errorMessage.length() > 0) { // sql例外発生
				return false;
			}
			int restCount = 0;
			rest_ = "\n・休暇";
			while (rs.next()) {
				rest_ += ("\n[休暇]" + rs.getString("STAFFNAME_SHOW"));
				restCount++;
			}
			if (restCount == 0) {
				rest_ += "なし";
			}
			dao.rsClose();
			//クローズ
			dao.close();
			returnCode = setDBData();
		} catch (SQLException e) {
			errorMessage = e.toString();
			dao.StackTraceLogging(e);
		}
		return returnCode;
	}

	//旅行期間取出し
	//旅行期間有効なら先頭に改行
	private String getTerm(String start, String end) {
		String _start = "";
		String _end = "";
		if ((start != null) && (start.length() > 0)) {
			String[] sdt = start.split("-");
			if (sdt.length == 3) {
				_start = Integer.parseInt(sdt[1]) + "/" + Integer.parseInt(sdt[2]);
				if ((end != null) && (end.length() > 0)) {
					String[] edt = end.split("-");
					if (edt.length == 3) {
						_end = Integer.parseInt(edt[1]) + "/" + Integer.parseInt(edt[2]);
						if (!_start.equals(_end)) {
							return " (" + _start + "～" + _end + ")";
						}
					}
				}
				return " (" + _start + ")";
			}
		}
		return "";
	}

	//Excelのセルへ取出したデータを格納
	private Boolean setDBData() {
		Row row;
		Cell cel;
		int col, i, l;
		// 戻り値初期値はfalse
		boolean returnCode = false;
		// 書込みシートを開く
		Sheet sheet = book.getSheet("教務日誌");
		if (sheet == null) {
			errorMessage = "対象のシート見つからず";
				return false;
		}
		row = sheet.getRow(1);
		if (row != null) {
			cel = row.getCell(0);	//学校名称
			if (cel != null) {
				cel.setCellValue(SchoolName);
			}
		}
		//決裁職名
		if (decisionCount > 0) {
			row = sheet.getRow(2);
			for(i = 0; i < MAX_POSITION; i++) {
				cel = row.getCell(i * 8);
				if (i < decisionCount) {
					cel.setCellValue(decision[i]);
				}
				else {
					cel.setCellValue("");
				}
			}
		}
		row = sheet.getRow(6);
		if (row != null) {
			cel = row.getCell(0);	//日付
			if (cel != null) {
				cel.setCellValue(date);
			}
			cel = row.getCell(23);	//天候
			if (cel != null) {
				cel.setCellValue(weather);
			}
		}
		//授業日数学年名
		row = sheet.getRow(7);
		for(i = 0; i < MAX_YEAR; i++) {
			if (lessons[i][0].length() > 0) {
				cel = row.getCell(12 + i * 5);
				cel.setCellValue(lessons[i][0]);
			}
		}
		row = sheet.getRow(8);
		cel = row.getCell(0);	//通算集
		cel.setCellValue(weekCount);
		//授業の有無
		for(i = 0; i < MAX_YEAR; i++) {
			if (lessons[i][0].length() > 0) {
				cel = row.getCell(12 + i * 5);
				cel.setCellValue(lessons[i][1]);
			}
		}
		cel = row.getCell(32);	//備考
		cel.setCellValue(remark);
		//授業日数
		row = sheet.getRow(9);
		for(i = 0; i < MAX_YEAR; i++) {
			if (lessons[i][0].length() > 0) {
				cel = row.getCell(12 + i * 5);
				cel.setCellValue(lessontotalday[i]);
			}
		}
		row = sheet.getRow(11);
		if (row != null) {
			cel = row.getCell(0);	//在籍生徒数
			if (cel != null) {
				cel.setCellValue(existStudent);;
			}
		}
		row = sheet.getRow(12);
		if (row != null) {
			cel = row.getCell(3);	//１年
			if (cel != null) {
				cel.setCellValue(gradeName[0]);
			}
			cel = row.getCell(28);	//２年
			if (cel != null) {
				cel.setCellValue(gradeName[1]);
			}
		}
		row = sheet.getRow(13);
		if (row != null) {
			col = 3;	//１年クラス名称
			for(i = 0; i < maxClass[0]; i++) {
				cel = row.getCell(col);
				if ((cel != null) && (className[0][i] != null)) {
					cel.setCellValue(className[0][i]);
				}
				col += 2;
			}
			col = 28;	//２年クラス名称
			for(i = 0; i < maxClass[1]; i++) {
				cel = row.getCell(col);
				if ((cel != null) && (className[1][i] != null)) {
					cel.setCellValue(className[1][i]);
				}
				col += 2;
			}
		}
		for(l = 0; l < MAX_ITEM; l++) {
			row = sheet.getRow(l + 16);
			if (row != null) {
				col = 3;	//１年クラス
				for(i = 0; i < maxClass[0]; i++) {
					cel = row.getCell(col);
					if (cel != null) {
						cel.setCellValue(y1n[0][i][l]);
					}
					col += 2;
				}
				col = 28;	//２年クラス
				for(i = 0; i < maxClass[1]; i++) {
					cel = row.getCell(col);
					if (cel != null) {
						cel.setCellValue(y1n[1][i][l]);
					}
					col += 2;
				}
			}
		}
		row = sheet.getRow(25);
		if (row != null) {
			cel = row.getCell(3);	//３年
			if (cel != null) {
				cel.setCellValue(gradeName[2]);
			}
			cel = row.getCell(28);	//４年
			if (cel != null) {
				cel.setCellValue(gradeName[3]);
			}
		}
		row = sheet.getRow(26);
		if (row != null) {
			col =3;	//３年クラス名称
			for(i = 0; i < maxClass[2]; i++) {
				cel = row.getCell(col);
				if ((cel != null) && (className[2][i] != null)) {
					cel.setCellValue(className[2][i]);
				}
				col += 2;
			}
			col = 28;	//４年クラス名称
			for(i = 0; i < maxClass[3]; i++) {
				cel = row.getCell(col);
				if ((cel != null) && (className[3][i] != null)) {
					cel.setCellValue(className[3][i]);
				}
				col += 2;
			}
			if (maxClass[3] > 0) {
				cel = row.getCell(XSSF_Tool.CellNo("AY"));
				if (cel != null) {
					cel.setCellValue("合計");
				}
			}
		}
		for(l = 0; l < MAX_ITEM; l++) {
			row = sheet.getRow(l + 29);
			if (row != null) {
				col = 3;	//３年クラス
				for(i = 0; i < maxClass[2]; i++) {
					cel = row.getCell(col);
					if (cel != null) {
						cel.setCellValue(y1n[2][i][l]);
					}
					col += 2;
				}
				col = 28;	//４年クラス
				for(i = 0; i < maxClass[3]; i++) {
					cel = row.getCell(col);
					if (cel != null) {
						cel.setCellValue(y1n[3][i][l]);
					}
					col += 2;
				}
				if (maxClass[3] <= 0) {
					col = XSSF_Tool.CellNo("AY");
					cel = row.getCell(col);
					cel.setCellType(CellType.BLANK);
					//cel.setCellValue("");
				}
			}
		}
		//行事
		if ((event != null) && (event.length() > 0)) {
			row = sheet.getRow(38);
			if (row != null) {
				cel = row.getCell(3);
				if (cel != null) {
					cel.setCellValue(event);
				}
			}
		}
		//今日の連絡
		if ((liaison != null) && (liaison.length() > 0)) {
			row = sheet.getRow(49);
			if (row != null) {
				cel = row.getCell(3);
				if (cel != null) {
					cel.setCellValue(liaison);
				}
			}
		}
		//旅行･休暇
		row = sheet.getRow(40);
		if (row != null) {
			cel = row.getCell(39);
			if (cel != null) {
				cel.setCellValue(trip_ + rest_);
			}
		}
		//算出セルを実行
		int rowStart = sheet.getFirstRowNum();
		int rowEnd = sheet.getLastRowNum();
		for(i = rowStart; i <= rowEnd; i++) {
			row = sheet.getRow(i);
			int cellStart = row.getFirstCellNum();
			int cellEnd = row.getLastCellNum();
			for(l = cellStart; l <= cellEnd; l++) {
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
		returnCode = true;
		return returnCode;
	}

}
