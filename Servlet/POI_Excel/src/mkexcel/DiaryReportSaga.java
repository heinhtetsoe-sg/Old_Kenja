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

public class DiaryReportSaga {
	Log log = LogFactory.getLog(DiaryReportSaga.class);
	// メンバー変数
	private Workbook book;
	private Alp_Properties Prop;
	private String errorMessage = "";
	private String schoolId = "";
	private String targetDate = "";
	private String LF;	//Excel改行
	// 書込みデータ
	private String schoolName = "";
	private String bangou = "";
	private String JDate; // 日付
	private String TDate; // 本日
	private String NDate; // 翌日
	//今日の日付
	java.sql.Date todayDate = null;
	//今日の連絡
	private String liaison = "";
	//出張
	private String[] Tr_staff  = new String[10];
	private String[] Tr_dest   = new String[10];
	private String[] Tr_time = new String[10];
	int t_count = 0;
	//行事予定
	private String[] Schedule = new String[14];
	private int[] days = new int[14];
	private String[] weeks = new String[14];

	// コンストラクタ
	// パラメータ
	// pdf：PDF作成オブジェクト
	// information：PDF出力のパラメータ(スタッフCD、学校コード、年-月)
	// prop：定義ファイル読取オブジェクト
	public DiaryReportSaga(Workbook book, String information, Alp_Properties prop) {
		// TODO 自動生成されたコンストラクター・スタブ
		this.book = book;
		this.Prop = prop;
		String[] Information = information.split(",");
		if (Information.length < 2) {
			errorMessage = String.format("パラメータエラー{%s}", information);
		} else {
			schoolId = Information[0];
			targetDate = Information[1];
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
			String sql2 = "SELECT t2.SCHOOLNAME, t1.REC_DATE, t1.WEATHER, t1.REMARK, t3.VISITOR, t3.VISITOR_REASON, t3.PRINTNO FROM SAF_SCHOOL_JOURNAL_DAT t1 left join SAF_SCHOOL_MST t2 on t1.SCHOOLID = t2.SCHOOLID left join SAF_SCHOOL_JOURNAL_EXTRA_D_DAT t3 on t1.ID = t3.JOURNAL_ID"
					+ " WHERE t1.REC_DATE = '" + targetDate + "' AND t1.SCHOOLID = '" + schoolId + "'";
			rs = dao.query(sql2);
			if (errorMessage.length() > 0) { // sql例外発生
				return false;
			}
			if (rs.next() != false) {
				schoolName = rs.getString("SCHOOLNAME");
				bangou = rs.getString("PRINTNO") + "号";
				todayDate = rs.getDate("REC_DATE");
				log.info("REMARK " + rs.getString("REMARK"));//テスト
				//一旦閉じる
				dao.rsClose();
				Calendar title_cl = Calendar.getInstance();
				title_cl.setTime(todayDate);
				String week = Prop.getWeek(title_cl);
				String day = Prop.getJDayString0(title_cl);
				JDate = String.format("%s  (%s)", day, week);
				TDate = "本日の予定　" + String.format("%2d月%2d日  (%s)", title_cl.get(Calendar.MONTH) + 1, title_cl.get(Calendar.DATE), week);
				title_cl.add(Calendar.DATE, 1);
				week = Prop.getWeek(title_cl);
				NDate = "翌日の予定　" + String.format("%2d月%2d日  (%s)", title_cl.get(Calendar.MONTH) + 1, title_cl.get(Calendar.DATE), week);
			}
			dao.rsClose();
			//今日の連絡
			String sql3 = "SELECT LIAISON FROM SAF_LIAISON_DAT WHERE"
					+ " SCHOOLID = '" + schoolId + "' AND LIAISON_DATE = '" + targetDate + "' AND DELETE_FLG = '0' ORDER BY UPDATED";
			//log.info(sql3);
			rs = dao.query(sql3);
			if (errorMessage.length() > 0) { // sql例外発生
				return false;
			}
			if (rs.next() != false) {
				liaison = rs.getString("LIAISON") != null ? rs.getString("LIAISON") : "";
			}
			dao.rsClose();
			//出張
			String sql4 = "SELECT t1.STAFFCD, t3.STAFFNAME_SHOW, t2.TRIP_REASON, t2.TRIP_DEST, t2.START_DATE, case when t2.END_DATE is null then t2.START_DATE else t2.END_DATE end as END_DATE, t2.START_TIME, t2.END_TIME FROM SAF_TRIP_DATE_DAT t1  left join SAF_TRIP_DAT t2 on t1.TRIP_ID = t2.ID  left join STAFF_MST t3 on t1.STAFFCD = t3.STAFFCD"
					+ " WHERE t1.SCHOOLID = '" + schoolId + "' AND t1.TRIP_DATE = '" + targetDate + "' AND t2.DELETE_FLG = '0' ORDER BY t1.STAFFCD";
			rs = dao.query(sql4);
			errorMessage = dao.getErrTrace();
			if (errorMessage.length() > 0) { // sql例外発生
				return false;
			}
			t_count = 0;
			while (rs.next()) {
				Tr_staff[t_count] = rs.getString("STAFFNAME_SHOW") != null ? rs.getString("STAFFNAME_SHOW") : "";
				Tr_dest[t_count] = rs.getString("TRIP_DEST") != null ? rs.getString("TRIP_DEST") : "";
				String start = rs.getString("START_TIME");
				String end = rs.getString("END_TIME");
				Tr_time[t_count] = String.format("%s～%s", start, end);
				if (++t_count >= 10) {
					break;
				}
			}
			dao.rsClose();
			//行事
			Calendar cl = Calendar.getInstance();
			cl.setTime(todayDate);
			for(int idx = 0; idx < 14; idx++) {
				Schedule[idx] = "";
				weeks[idx] = Prop.getWeek(cl);
				days[idx] = cl.get(Calendar.DATE);
				String selectdate = String.format("%d-%02d-%02d", cl.get(Calendar.YEAR), cl.get(Calendar.MONTH) + 1, days[idx]);
				String sql5 = "SELECT EVENT_NAME FROM SAF_EVENT_DAT WHERE"
						+ " SCHOOLID = '" + schoolId + "' AND EVENT_DATE = '" + selectdate + "' ORDER BY START_TIME";
				rs = dao.query(sql5);
				errorMessage = dao.getErrTrace();
				if (errorMessage.length() > 0) { // sql例外発生
					return false;
				}
				int s_count = 0;
				while (rs.next()) {
					if (s_count++ > 0) {
						Schedule[idx] += LF;
					}
					Schedule[idx] += rs.getString("EVENT_NAME");
				}
				cl.add(Calendar.DATE, 1);
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
		//
		return returnCode;
	}

	public Boolean setColumn() {
		Row row;
		Cell cel;
		int L;
		// 書込みシートを開く
		Sheet sheet = book.getSheet("日報");
		if (sheet == null) {
			errorMessage = "対象のシート見つからず";
			return false;
		}
		SetColumn set = new SetColumn();
		set.setSheet(sheet);
		//タイトル
		// 日付
		char lf = 0x0A;
		String LF = String.valueOf(lf);
		set.setValueString(1, "X", JDate);
		set.setValueString(2, "X", schoolName);
		set.setValueString(1, "AX", bangou);
		set.setValueString(3, "A", TDate);
		set.setValueString(3, "AC", NDate);
		set.setValueString(5, "A", Schedule[0]);
		set.setValueString(5, "AC", Schedule[1]);
		//お知らせ
		set.setValueString(21, "A", liaison);
		//出張
		int k0 = XSSF_Tool.CellNo("K");
		int r0 = XSSF_Tool.CellNo("V");
		int n1 = XSSF_Tool.CellNo("AC");
		int k1 = XSSF_Tool.CellNo("AM");
		int r1 = XSSF_Tool.CellNo("AX");
		row = sheet.getRow(33);
		String traveller = "";
		String destnation = "";
		String worktime = "";
		for(L = 0; L < t_count && L < 5 ; L++) {
			if (L > 0) {
				 traveller += LF;
				 destnation += LF;
				 worktime += LF;
			}
			traveller += Tr_staff[L];
			destnation += Tr_dest[L];
			worktime += Tr_time[L];
		}
		cel = row.getCell(0);
		if (cel != null) {
			cel.setCellValue(traveller);
		}
		cel = row.getCell(k0);
		if (cel != null) {
			cel.setCellValue(destnation);
		}
		cel = row.getCell(r0);
		if (cel != null) {
			cel.setCellValue(worktime);
		}
		String traveller2 = "";
		String destnation2 = "";
		String worktime2 = "";
		for(;L < t_count; L++) {
			if (L > 5) {
				 traveller2 += LF;
				 destnation2 += LF;
				 worktime2 += LF;
			}
			traveller2 += Tr_staff[L];
			destnation2 += Tr_dest[L];
			worktime2 += Tr_time[L];
		}
		cel = row.getCell(n1);
		if (cel != null) {
			cel.setCellValue(traveller2);
		}
		cel = row.getCell(k1);
		if (cel != null) {
			cel.setCellValue(destnation2);
		}
		cel = row.getCell(r1);
		if (cel != null) {
			cel.setCellValue(worktime2);
		}
		//行事
		int w0 = XSSF_Tool.CellNo("C");
		int s0 =  XSSF_Tool.CellNo("E");
		int d1 = XSSF_Tool.CellNo("AC");
		int w1 = XSSF_Tool.CellNo("AE");
		int s1 = XSSF_Tool.CellNo("AG");
		int line = 40;
		for(L = 0; L < 7; L++) {
			row = sheet.getRow(line);
			line += 3;
			cel = row.getCell(0);
			if (cel != null) {
				cel.setCellValue(days[L]);
			}
			cel = row.getCell(w0);
			if (cel != null) {
				cel.setCellValue(weeks[L]);
			}
			cel = row.getCell(s0);
			if (cel != null) {
				cel.setCellValue(Schedule[L]);
			}
			cel = row.getCell(d1);
			if (cel != null) {
				cel.setCellValue(days[L+7]);
			}
			cel = row.getCell(w1);
			if (cel != null) {
				cel.setCellValue(weeks[L+7]);
			}
			cel = row.getCell(s1);
			if (cel != null) {
				cel.setCellValue(Schedule[L+7]);
			}
		}
		//
		//算出セルを実行
		int rowStart = sheet.getFirstRowNum();
		int rowEnd = sheet.getLastRowNum();
		//log.info("[" + rowStart + "][" + rowEnd + "]");
		for(int i = rowStart; i <= rowEnd; i++) {
			row = sheet.getRow(i);
			if (row == null) {
				//log.info("row No.[" + i + "]");
				continue;
			}
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
