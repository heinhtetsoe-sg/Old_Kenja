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

public class BusinessTripLodgeAsk {
	Log log = LogFactory.getLog(BusinessTripLodgeAsk.class);
	// メンバー変数
	private Workbook book;
	private Alp_Properties Prop;
	private String errorMessage = "";
	private String schoolId = "";
	private String tripID = "";
	private String[] Stamp = new String[6];
	private String Transportation = "";
	private String jobName = "";
	private String staffName = "";
	private String business = "";
	private String organizer = "";
	private String tripTerm = "";
	private String s_time = "";
	private String e_time = "";
	private String[] businessTime = new String[5];
	private String[] businessArea = new String[5];
	private String[] venue = new String[5];
	private String privateCar = "";
	private int maleCoverage, femaleCoverage, totalCoverage;
	private String budgetType = "";
	private String transfer = "";
	private String classTransfer = "";
	private String classTreatment = "";
	private String comment = "";

	public BusinessTripLodgeAsk(Workbook book, String information, Alp_Properties prop) {
		// TODO 自動生成されたコンストラクター・スタブ
		this.book = book;
		this.Prop = prop;
		String[] information1s = information.split(",");
		if (information1s.length < 2) {
			errorMessage = String.format("パラメータエラー{%s}", information);
		}
		else {
			schoolId = information1s[0];
			tripID = information1s[1];
			log.info("BusinessTripLodgeAskコンストラクタ{" + information + "}");
		}
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
		//役職
		String sqlJ = "SELECT JOBNAME FROM SAF_SANCTION_JOB_DAT WHERE SCHOOLID = '" + schoolId + "' ORDER BY ORDERNO";
		//交通機関
		String sqlT = "SELECT t3.NAME1 FROM SAF_TRIP_DAT t1"
				+ " left join SAF_TRIP_TRANSPORTATION_DAT t2 on t1.ID = t2.TRIP_ID"
				+ " left join SAF_NAME_MST t3 on t2.TRANSPORTATION = t3.NAMECD3 and t3.NAMECD1 = 'SAFRD' and t3.NAMECD2= '01'"
				+ " WHERE t1.ID = '" + tripID + "' AND t1.SCHOOLID = '" + schoolId + "' ORDER BY t2.TRANSPORTATION";
		//その他タイトル
		String sql0 = "SELECT t6.JOBNAME,"
				+ " t3.STAFFNAME,"
				+ " t2.BUSSINESS_REASON,"
				+ " t2.TRIP_SPONSOR,"
				+ " t1.START_DATE,"
				+ " t1.END_DATE,"
				+ " case when t1.START_TIME is not null then t1.START_TIME else t7.AM_START_TIME end as START_TIME,"
				+ " case when t1.END_TIME is not null then t1.END_TIME else t7.PM_END_TIME end as END_TIME,"
				+ " t1.REMARK,"
				+ " t2.PRIVATE_CAR_REASON,"
				+ " t2.MALE_STUDENT,"
				+ " t2.FEMALE_STUDENT,"
				+ " value(t2.FEMALE_STUDENT, 0) + value(t2.MALE_STUDENT, 0) as total_student,"
				+ " t2.BUDGET_TYPE,"
				+ " t2.CLASS_DEAL,"
				+ " t4.NAME1 as TRANSFER_FLG_NAME,"
				+ " t5.NAME1 as CLASS_TRANSFER_NAME"
				+ " FROM SAF_TRIP_DAT t1"
				+ " left join SAF_TRIP_EXTRA_D_DAT t2 on t1.ID = t2.TRIP_ID"
				+ " left join STAFF_MST t3 on t1.STAFFCD = t3.STAFFCD"
				+ " left join SAF_NAME_MST t4 on t2.TRANSFER_FLG = t4.NAMECD3 and t4.NAMECD1 = 'SAFRD' and t4.NAMECD2 ='02'"
				+ " left join SAF_NAME_MST t5 on t2.CLASS_TRANSFER = t5.NAMECD3 and t5.NAMECD1 = 'SAFRD' and t5.NAMECD2 = '02'"
				+ " left join JOB_D_MST t6 on t2.JOBCD = t6.JOBCD and t1.SCHOOLID = t6.SCHOOLID"
				+ " left join SAF_SCHOOL_MST t7 on t1.SCHOOLID = t7.SCHOOLID"
				+ " WHERE t1.ID = '" + tripID + "' AND t1.SCHOOLID = '" + schoolId + "'";
		//log.info(sql0);
		//用務
		String sqlB = "SELECT t1.ID, t1.SCHOOLID, t2.ORDER, t2.FROM_DATE, t2.FROM_TIME, t2.TO_DATE, t2.TO_TIME, t2.DEST_ADDR, t2.DEST_NAME, t2.REGISTERCD, t2.UPDATED"
				+ " FROM SAF_TRIP_DAT t1 left join SAF_TRIP_REASON_DAT t2 on t1.ID = t2.TRIP_ID"
				+ " WHERE t1.ID = '" + tripID + "' AND t1.SCHOOLID = '" + schoolId + "' ORDER BY t2.ORDER"
				+ " fetch first 5 rows only";
		try {
			//役職印影
			rs = dao.query(sqlJ);
			int count = 0;
			while (rs.next()) {
				Stamp[count++] = rs.getString("JOBNAME");
				if (count >= 6) {
					break;
				}
			}
			for(int i=count; i < 6; i++) {
				Stamp[i] = "";
			}
			dao.rsClose();
			//交通機関
			rs = dao.query(sqlT);
			while(rs.next()) {
				if (Transportation.length() > 0) {
					Transportation += "　";
				}
				Transportation += rs.getString("NAME1");
			}
			dao.rsClose();
			//その他タイトル
			rs = dao.query(sql0);
			if (rs.next() != false) {
				jobName = rs.getString("JOBNAME");	//職名
				staffName = rs.getString("STAFFNAME");	//氏名
				business = rs.getString("BUSSINESS_REASON");//用務
				organizer = rs.getString("TRIP_SPONSOR");	//主催者
				tripTerm = "";
				//出張期間
				java.sql.Date date = rs.getDate("START_DATE");
				if (date != null) {
					Calendar s_cl = Calendar.getInstance();
					s_cl.setTime(date);
					tripTerm = Prop.getJDayString(s_cl) + " ～ ";
				}
				date = rs.getDate("END_DATE");
				if (date != null) {
					Calendar e_cl = Calendar.getInstance();
					e_cl.setTime(date);
					String endterm = Prop.getJDayString(e_cl);
					tripTerm += endterm;
				}
				s_time = rs.getString("START_TIME");	//出発時間
				e_time = rs.getString("END_TIME");	//帰着時間
				//自家用車使用理由
				privateCar = rs.getString("PRIVATE_CAR_REASON");
				//引率
				maleCoverage = rs.getInt("MALE_STUDENT");
				femaleCoverage = rs.getInt("FEMALE_STUDENT");
				totalCoverage = rs.getInt("TOTAL_STUDENT");
				//予算科目
				budgetType = rs.getString("BUDGET_TYPE");
				//勤務の振替
				transfer = rs.getString("TRANSFER_FLG_NAME");
				//授業の振替
				classTransfer = rs.getString("CLASS_TRANSFER_NAME");
				//授業等振替処置
				classTreatment = rs.getString("CLASS_DEAL");
				//備考
				comment = rs.getString("REMARK");
			}
			dao.rsClose();
			//用務
			rs = dao.query(sqlB);
			int idx = 0;
			while(rs.next()) {
				//用務時間
				java.sql.Date date = rs.getDate("FROM_DATE");
				long from = date.getTime();
				long to = from;
				date = rs.getDate("TO_DATE");
				if (date != null) {
					to = date.getTime();
				}
				String stime = rs.getString("FROM_TIME");
				if (stime != null) {
					String[] T = stime.split(":");
					if (T.length >= 2) {
						int ms = Integer.valueOf(T[0]) * 60 * 6000 + Integer.valueOf(T[1]) * 6000;
						from += ms;
					}
				}
				String etime = rs.getString("TO_TIME");
				if (etime != null) {
					String[] T = etime.split(":");
					if (T.length >= 2) {
						int ms = Integer.valueOf(T[0]) * 60 * 6000 + Integer.valueOf(T[1]) * 6000;
						to += ms;
					}
				}
				int sa = (int) ((to - from) / 6000);
				if (sa >= 60) {
					if ((sa % 60) > 0) {
						businessTime[idx] = String.format("%d時間%d分", sa / 60, sa % 60);
					}
					else {
						businessTime[idx] = String.format("%d時間", sa / 60);
					}
				}
				else {
					businessTime[idx] = String.format("%d分", sa);
				}
				//用務地
				businessArea[idx] = rs.getString("DEST_ADDR");
				//会場
				venue[idx] = rs.getString("DEST_NAME");
				if (++idx >=5)
					break;
			}
			for(;idx < 5; idx++) {
				businessTime[idx] = businessArea[idx] = venue[idx] = "";
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
		Sheet sheet = book.getSheet("出張伺_宿泊有");
		if (sheet == null) {
			errorMessage = "対象のシート見つからず";
			return false;
		}
		SetColumn set = new SetColumn();
		set.setSheet(sheet);
		//役職印影
		int s;
		row = sheet.getRow(0);
		for(s = 0; s < 6; s ++) {
			cel = row.getCell(s * 2 + 6);
			if (cel != null) {
				cel.setCellValue(Stamp[s]);
			}
		}
		row = sheet.getRow(55);
		for(s = 0; s < 6; s ++) {
			cel = row.getCell(s * 2 + 6);
			if (cel != null) {
				cel.setCellValue(Stamp[s]);
			}
		}
		set.setValueString(7, "C", jobName);//職名
		set.setValueString(64, "C", jobName);
		set.setValueString(7, "J", staffName);	//氏名
		set.setValueString(64, "J", staffName);
		set.setValueString(8, "C", business);	//用務
		set.setValueString(9, "C", organizer);	//主催者
		set.setValueString(10, "C", tripTerm);	//出張期間
		set.setValueString(11, "C", s_time);	//出発時間
		set.setValueString(11, "L", e_time);	//帰着時間
		//自家用車使用理由
		set.setValueString(39, "E", privateCar);
		//引率
		set.setValueInt(41, "D", maleCoverage);
		set.setValueInt(41, "G", femaleCoverage);
		set.setValueInt(41, "J", totalCoverage);
		//予算科目
		set.setValueString(42, "C", budgetType);
		//勤務の振替
		set.setValueString(43, "C", transfer);
		//授業の振替
		set.setValueString(44, "C", classTransfer);
		//授業等振替処置
		set.setValueString(45, "C", classTreatment);
		//備考
		set.setValueString(48, "C", comment);
		for(int idx = 0; idx < 5; idx++) {
			//用務時間
			set.setValueString(12 + idx * 4, "C", businessTime[idx]);
			//用務地
			set.setValueString(13 + idx * 4, "C", businessArea[idx]);
			//会場
			set.setValueString(15 + idx * 4, "C", venue[idx]);
		}

		//交通機関
		set.setValueString(38, "C", Transportation);

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
