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
import util.SetColumn;
import util.XSSF_Tool;

public class EducationDiarySaga {
	Log log = LogFactory.getLog(EducationDiarySaga.class);
	// メンバー変数
	private Workbook book;
	private Alp_Properties Prop;
	private String errorMessage = "";
	private String schoolId = "";
	private String targetDate = "";
	private String LF;	//Excel改行
	// 書込みデータ
	String JYear; // 年度
	String Visitor = "";
	String VisitorReason = "";
	String Remark = "";
	//スタンプ
	String[] stamp = new String[4];
	String dateWeather = ""; // 日付天気学校名
	String Report = ""; // 記事
	//出張
	String[] Tr_staff  = new String[15];
	String[] Tr_dest   = new String[15];
	String[] Tr_reason = new String[15];
	int t_count = 0;
	//休暇
	String[] r_remark = new String[10];
	String[] r_staffName = new String[10];
	String[] r_kind = new String[10];
	String[] r_time = new String[10];
	int r_count = 0;


	// コンストラクタ
	// パラメータ
	// pdf：PDF作成オブジェクト
	// information：PDF出力のパラメータ(スタッフCD、学校コード、年-月)
	// prop：定義ファイル読取オブジェクト
	public EducationDiarySaga(Workbook book, String information,
			Alp_Properties prop) {
		// TODO 自動生成されたコンストラクター・スタブ
		this.book = book;
		this.Prop = prop;
		String[] Information = information.split(",");
		if (Information.length < 2) {
			errorMessage = String.format("パラメータエラー{%s}", information);
		} else {
			schoolId = Information[0];
			targetDate = Information[1];
			String[] start = targetDate.toString().split("-");
			if (start.length >= 3) {
				String[] str = this.Prop.getJapanYear0(Integer.valueOf(start[0]), Integer.valueOf(start[1]), Integer.valueOf(start[2]));
				JYear = String.format("%s %s 年", str[0], str[1]);
			}
		}
		char lf = 0x0A;
		LF = String.valueOf(lf);
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
		// データベースリソース
		final String JDBC_LOOKUP = Prop.getProperty("jdbc.lookup");
		// フィールドデータをデータベースから取出す
		Statement stmt = null;
		ResultSet rs = null;
		int count;
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
		//
		try {
			// 決裁印職名
			String sql1 = "SELECT JOBNAME FROM SAF_SANCTION_JOB_DAT WHERE SCHOOLID ='"
					+ schoolId + "' ORDER BY ORDERNO";
			rs = dao.query(sql1);
			errorMessage = dao.getErrTrace();
			if (errorMessage.length() > 0) { // sql例外発生
				return false;
			}
			count = 0;
			while (rs.next()) {
				stamp[count] = rs.getString("JOBNAME") != null ? rs.getString("JOBNAME") : "";
				if (++count >= 4) {
					break;
				}
			}
			for(; count <4; count++) {
				stamp[count] = "";
			}
			dao.rsClose();
			//天気等
			String sql2 = "SELECT t2.SCHOOLNAME, t1.REC_DATE, t1.WEATHER, t1.REMARK, t3.VISITOR, t3.VISITOR_REASON, t3.PRINTNO FROM SAF_SCHOOL_JOURNAL_DAT t1 left join SAF_SCHOOL_MST t2 on t1.SCHOOLID = t2.SCHOOLID left join SAF_SCHOOL_JOURNAL_EXTRA_D_DAT t3 on t1.ID = t3.JOURNAL_ID"
					+ " WHERE t1.REC_DATE = '" + targetDate + "' AND t1.SCHOOLID = '" + schoolId + "'";
			rs = dao.query(sql2);
			//log.info("天気等 " + sql2);
			if (errorMessage.length() > 0) { // sql例外発生
				return false;
			}
			if (rs.next() != false) {
				java.sql.Date date = rs.getDate("REC_DATE");
				Calendar title_cl = Calendar.getInstance();
				title_cl.setTime(date);
				String week = Prop.getWeek(title_cl);
				String schoolName = rs.getString("SCHOOLNAME");
				String weather = rs.getString("WEATHER");
				dateWeather = String.format("%02d月 %02d日 %s曜  %s %s", title_cl.get(Calendar.MONTH) + 1, title_cl.get(Calendar.DATE), week, weather, schoolName);
				Visitor = rs.getString("VISITOR");
				VisitorReason = rs.getString("VISITOR_REASON");
				Remark = rs.getString("REMARK");;
			}
			dao.rsClose();
			//行事
			String sql3 = "SELECT EVENT_NAME FROM SAF_EVENT_DAT WHERE"
					+ " SCHOOLID = '" + schoolId + "' AND EVENT_DATE = '" + targetDate + "' ORDER BY START_TIME";
			rs = dao.query(sql3);
			errorMessage = dao.getErrTrace();
			if (errorMessage.length() > 0) { // sql例外発生
				return false;
			}
			int s_count = 0;
			while (rs.next()) {
				if (s_count++ > 0) {
					Report += LF;
				}
				String line = rs.getString("EVENT_NAME") != null ? rs.getString("EVENT_NAME") : "";
				Report += line;
			}
			dao.rsClose();
			//今日の連絡
			String sql4 = "SELECT LIAISON FROM SAF_LIAISON_DAT WHERE"
					+ " SCHOOLID = '" + schoolId + "' AND LIAISON_DATE = '" + targetDate + "' AND DELETE_FLG = '0' ORDER BY UPDATED";
			rs = dao.query(sql4);
			if (errorMessage.length() > 0) { // sql例外発生
				return false;
			}
			while (rs.next()) {
				if (s_count++ > 0) {
					Report += LF;
				}
				String line = rs.getString("LIAISON") != null ? rs.getString("LIAISON") : "";
				Report += line;
			}
			dao.rsClose();
			//出張
			String sql5 = "SELECT t1.STAFFCD, t3.STAFFNAME_SHOW, t2.TRIP_REASON, t2.TRIP_DEST, t2.START_DATE, case when t2.END_DATE is null then t2.START_DATE else t2.END_DATE end as END_DATE, t2.START_TIME, t2.END_TIME FROM SAF_TRIP_DATE_DAT t1  left join SAF_TRIP_DAT t2 on t1.TRIP_ID = t2.ID  left join STAFF_MST t3 on t1.STAFFCD = t3.STAFFCD"
					+ " WHERE t1.SCHOOLID = '" + schoolId + "' AND t1.TRIP_DATE = '" + targetDate + "' AND t2.DELETE_FLG = '0' ORDER BY t1.STAFFCD";
			rs = dao.query(sql5);
			errorMessage = dao.getErrTrace();
			if (errorMessage.length() > 0) { // sql例外発生
				return false;
			}
			t_count = 0;
			while (rs.next()) {
				Tr_staff[t_count] = rs.getString("STAFFNAME_SHOW") != null ? rs.getString("STAFFNAME_SHOW") : "";
				Tr_dest[t_count] = rs.getString("TRIP_DEST") != null ? rs.getString("TRIP_DEST") : "";
				Tr_reason[t_count] = rs.getString("TRIP_REASON") != null ? rs.getString("TRIP_REASON") : "";
				if (++t_count >= 15) {
					break;
				}
			}
			dao.rsClose();
			//休暇
			String sql6 ="SELECT t1.STAFFCD, t3.STAFFNAME_SHOW, t2.REMARK, t4.NAME1, t2.SHINSEI_DAY, t2.SHINSEI_HOUR, t2.SHINSEI_MIN FROM"
					+ " SAF_VACATION_DATE_DAT t1  left join SAF_VACATION_DAT t2 on t1.VACATION_ID = t2.ID  left join STAFF_MST t3 on t1.STAFFCD = t3.STAFFCD left join SAF_NAME_MST t4 on t2.VACATION_KIND = t4.NAMECD3 AND t4.NAMECD1 = 'SAFVD' AND t4.NAMECD2 = '01'"
					+ " WHERE t1.VACATION_DATE = '" + targetDate + "' AND t1.SCHOOLID = '" + schoolId + "' AND t2.DELETE_FLG = '0' ORDER BY t1.STAFFCD";
			rs = dao.query(sql6);
			errorMessage = dao.getErrTrace();
			if (errorMessage.length() > 0) { // sql例外発生
				return false;
			}
			r_count = 0;
			while (rs.next()) {
				r_staffName[r_count] = rs.getString("STAFFNAME_SHOW");
				r_kind[r_count] = rs.getString("NAME1");
				r_remark[r_count] = rs.getString("REMARK") != null ? rs.getString("REMARK") : "";
				int D, H, M;
				D = rs.getInt("SHINSEI_DAY");
				H = rs.getInt("SHINSEI_HOUR");
				M = rs.getInt("SHINSEI_MIN");
				r_time[r_count] = "";
				if (D > 0) {
					r_time[r_count] += (String.valueOf(D) + "D");
				}
				if (H > 0) {
					r_time[r_count] += (String.valueOf(H) + "H");
				}
				if (M > 0) {
					r_time[r_count] += (String.valueOf(M) + "M");
				}
				if (++r_count >= 4) {
					break;
				}
			}
			dao.rsClose();
			//クローズ
			dao.close();
			//
			returnCode = setColumn();
		} catch (SQLException e) {
			errorMessage = e.toString();
			dao.StackTraceLogging(e);
		}
		return returnCode;
	}

	public Boolean setColumn() {
		Row row;
		Cell cel;
		// 書込みシートを開く
		Sheet sheet = book.getSheet("校務日誌");
		if (sheet == null) {
			errorMessage = "対象のシート見つからず";
			return false;
		}
		SetColumn set = new SetColumn();
		set.setSheet(sheet);
		//年度
		set.setValueString(2, "A", JYear);
		//スタンプ
		set.setValueString(2, "AA", stamp[0]);
		set.setValueString(2, "AG", stamp[1]);
		set.setValueString(2, "AM", stamp[2]);
		set.setValueString(2, "AS", stamp[3]);
		//日付天気学校名
		set.setValueString(3, "A", dateWeather);
		// 記事
		set.setValueString(8, "A", Report);
		//出張
		int d = XSSF_Tool.CellNo("N");
		int r = XSSF_Tool.CellNo("AA");
		for(int L = 0; L < t_count ; L++) {
			row = sheet.getRow(L + 24);
			cel = row.getCell(0);
			if (cel != null) {
				cel.setCellValue(Tr_staff[L]);
			}
			cel = row.getCell(d);
			if (cel != null) {
				cel.setCellValue(Tr_dest[L]);
			}
			cel = row.getCell(r);
			if (cel != null) {
				cel.setCellValue(Tr_reason[L]);
			}
		}
		//休暇
		int k0 = XSSF_Tool.CellNo("L");
		int r0 = XSSF_Tool.CellNo("O");
		int t0 = XSSF_Tool.CellNo("Z");
		int n1 = XSSF_Tool.CellNo("AC");
		int k1 = XSSF_Tool.CellNo("AN");
		int r1 = XSSF_Tool.CellNo("AQ");
		int t1 = XSSF_Tool.CellNo("BB");
		int LL;
		for(LL = 0; LL < r_count && LL < 5; LL++) {
			row = sheet.getRow(LL + 41);
			cel = row.getCell(0);
			if (cel != null) {
				cel.setCellValue(r_staffName[LL]);
			}
			cel = row.getCell(k0);
			if (cel != null) {
				cel.setCellValue(r_kind[LL]);
			}
			cel = row.getCell(r0);
			if (cel != null) {
				cel.setCellValue(r_remark[LL]);
			}
			cel = row.getCell(t0);
			if (cel != null) {
				cel.setCellValue(r_time[LL]);
			}
		}
		for(; LL < r_count ; LL++) {
			row = sheet.getRow(LL + 41 - 5);
			cel = row.getCell(n1);
			if (cel != null) {
				cel.setCellValue(r_staffName[LL]);
			}
			cel = row.getCell(k1);
			if (cel != null) {
				cel.setCellValue(r_kind[LL]);
			}
			cel = row.getCell(r1);
			if (cel != null) {
				cel.setCellValue(r_remark[LL]);
			}
			cel = row.getCell(t1);
			if (cel != null) {
				cel.setCellValue(r_time[LL]);
			}
		}
		//来校者
		set.setValueString(48, "A", Visitor);
		set.setValueString(48, "AC", VisitorReason);
		//その他
		set.setValueString(52, "A",Remark);
		// 算出セルを実行
		int rowStart = sheet.getFirstRowNum();
		int rowEnd = sheet.getLastRowNum();
		for (int i = rowStart; i <= rowEnd; i++) {
			row = sheet.getRow(i);
			if (row == null) {
				// log.info("row No.[" + i + "]");
				continue;
			}
			int cellStart = row.getFirstCellNum() >= 0 ? row.getFirstCellNum()
					: 0;
			int cellEnd = row.getLastCellNum();
			// log.info(i + "開始[" + row.getFirstCellNum() + "] 終了[" +
			// row.getLastCellNum() + "]");
			for (int l = cellStart; l <= cellEnd; l++) {
				cel = row.getCell(l);
				if (cel != null) {
					if (cel.getCellType() == CellType.FORMULA) {
						String str = cel.getCellFormula();
						// log.info("FORMULA：" + str);
						cel.setCellFormula(str);
					}
				}
			}
		}
		return true;
	}
}
