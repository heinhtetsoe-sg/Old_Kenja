package mkexcel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import util.Alp_Properties;
import util.SetColumn;

public class Tr_Sagcr_07_1 {
	Log log = LogFactory.getLog(Sample.class);
	// メンバー変数
	private Workbook book;
	private Alp_Properties Prop;
	private String errorMessage = "";
	private String _year;

	public Tr_Sagcr_07_1(Workbook book, String information, Alp_Properties prop) {
		// TODO 自動生成されたコンストラクター・スタブ
		this.book = book;
		this.Prop = prop;

		String[] Information = information.split(",");
		if (Information.length < 1) {
			errorMessage = String.format("パラメータエラー{%s}", information);
		} else {
			_year = Information[0];  //★yearは上位からパラメータでわたってくる。
			//_year = "2018"; //★デバッグで仮設定
		}
		this.Prop = prop;

		log.warn("$Revision: 67799 $");
	}

	public Boolean excel() {

		//データベースリソース
		final String JDBC_LOOKUP = Prop.getProperty("jdbc.lookup");

		// フィールドデータをデータベースから取出す
		Statement stmt = null;
		// データベース操作オブジェクト生成
		util.DaoUtil dao = new util.DaoUtil(Prop.getProperty("sqllogging").equalsIgnoreCase("true"), log);
		// データベースオープン
		stmt = dao.open(JDBC_LOOKUP);
		if (stmt == null) {
			// オープン失敗
			errorMessage = "データベース接続失敗";
			log.error("dao.open() error");
			return null;
		}



		//DBからデータ取得
		String sqlT = getSqlString();

		System.out.println(sqlT);

		List<TrAttendanceRecord> recordlist=getRecordList(sqlT);


//		String[] propcols = Information.split(",", 0);
		org.apache.poi.ss.usermodel.Sheet sheet = book.getSheetAt(0);
//		int lineCount = 0;
//		int propcolslength =  propcols.length;



		SetColumn set = new SetColumn();
		set.setSheet(sheet);

		//"研修の受講状況【2018年度】";
		final String strWk;
		strWk = "研修の受講状況【" + _year + "年度】";
		set.setValueString(1, "A", strWk);

		//"出力日：";
		Date date = new Date();
		SimpleDateFormat fmt1 = new SimpleDateFormat("yyyy/MM/dd");
		String dstrWk = "出力日：" + fmt1.format(date);
		set.setValueString(1, "K", dstrWk);


		int currentRowNum=2;
		for(TrAttendanceRecord tr_record: recordlist) {	//この方法ではrowがnullで抜ける
			int maxlengthWk = 0;
			//Row row=sheet.createRow(currentRowNum);
			Row row = sheet.getRow(currentRowNum);





			//1.研修種別
			Cell tr_type_name_cell =row.getCell(0);
			tr_type_name_cell.setCellValue(tr_record.getTRAINING_TYPE_NAME());
			maxlengthWk = setMaxLengthChk(maxlengthWk,getCellRows(tr_record.getTRAINING_TYPE_NAME(), 11 * 2));

			//2.対象校種
			Cell schl_type_name_cell=row.getCell(1);
			schl_type_name_cell.setCellValue(tr_record.getSCHOOL_TYPE());
			maxlengthWk = setMaxLengthChk(maxlengthWk,getCellRows(tr_record.getSCHOOL_TYPE(), 16 * 2));

			//3.研修番号
			Cell tr_no_cell=row.getCell(2);
			tr_no_cell.setCellValue(tr_record.getTRAINING_NO());

			//4.研修名
			Cell tr_name_cell=row.getCell(3);
			tr_name_cell.setCellValue(tr_record.getTRAINING_NAME());
			maxlengthWk = setMaxLengthChk(maxlengthWk,getCellRows(tr_record.getTRAINING_NAME(), 31 * 2));

			//5.日数
			Cell enforcement_days_cell=row.getCell(4);
			enforcement_days_cell.setCellValue(tr_record.getENFORCEMENT_DAYS());

			//6.定員
			Cell capacity_cell=row.getCell(5);
			capacity_cell.setCellValue(tr_record.getCAPACITY());

			//7.申込者数
			Cell applicant_num_cell=row.getCell(6);
			applicant_num_cell.setCellValue(tr_record.getAPPLICANT_NUM());

			//8.採用者数
			Cell saiyou_num_cell=row.getCell(7);
			saiyou_num_cell.setCellValue(tr_record.getSAIYOU_NUM());

			//9.欠席者延人数
			Cell absentee_num_cell=row.getCell(8);
			absentee_num_cell.setCellValue(tr_record.getABSENTEE_NUM());

			//10.受講者延人数
			Cell student_num_cell=row.getCell(9);
			student_num_cell.setCellValue(tr_record.getSTUDENT_NUM());

			//11.受講率
			Cell attendance_rate=row.getCell(10);
			attendance_rate.setCellValue(tr_record.getATTENDANCE_RATE());
			//row.setHeightInPoints((float)15.0 * (float)maxlengthWk);
			CellStyle style =  book.createCellStyle();
            style.setWrapText(true);
            row.setRowStyle(style);
            row.getCell(0).setCellStyle(style);

			currentRowNum++;

		}
		return true;
	}


    private int getByteLength(final String str) {
        return str.getBytes(Charset.forName("MS932")).length;
    }
    private int setMaxLengthChk(final int chkDatA, final int chkDatB) {
    	return chkDatA > chkDatB ? chkDatA : chkDatB;
    }
    private int getCellRows(final String str, int maxLineByte) {
    	return (int)Math.ceil((float)getByteLength(str) / (float)maxLineByte);
    }

	private boolean connectTest() throws Exception {

		String sqlT = getSqlString();

		return true;
	}



	private String getSqlString() {
		StringBuffer stb = new StringBuffer();
		stb.append("WITH SAF_TRAINING_SCHOOL_TYPE AS (");
		stb.append("");
		stb.append(" SELECT ");
		stb.append("	A.YEAR,");
		stb.append("	A.TRAINING_TYPE_CODE,");
		stb.append("	A.TRAINING_ID,");
		stb.append("	COUNT(*) AS SCHOOL_TYPE_COUNT,");
		stb.append("	MAX(CASE WHEN A.SCHOOL_TYPE = '01' THEN A.SCHOOL_TYPE END) AS SCHOOL_TYPE_P,");
		stb.append("	MAX(CASE WHEN A.SCHOOL_TYPE = '01' THEN B.ITEM1 END) AS SCHOOL_TYPE_NAME_P,");
		stb.append("	MAX(CASE WHEN A.SCHOOL_TYPE = '01' THEN B.ITEM2 END) AS SCHOOL_TYPE_DISPLAY_ORDER_P,");
		stb.append("	MAX(CASE WHEN A.SCHOOL_TYPE = '02' THEN A.SCHOOL_TYPE END) AS SCHOOL_TYPE_J,");
		stb.append("	MAX(CASE WHEN A.SCHOOL_TYPE = '02' THEN B.ITEM1 END) AS SCHOOL_TYPE_NAME_J,");
		stb.append("	MAX(CASE WHEN A.SCHOOL_TYPE = '02' THEN B.ITEM2 END) AS SCHOOL_TYPE_DISPLAY_ORDER_J,");
		stb.append("	MAX(CASE WHEN A.SCHOOL_TYPE = '03' THEN A.SCHOOL_TYPE END) AS SCHOOL_TYPE_H,");
		stb.append("	MAX(CASE WHEN A.SCHOOL_TYPE = '03' THEN B.ITEM1 END) AS SCHOOL_TYPE_NAME_H,");
		stb.append("	MAX(CASE WHEN A.SCHOOL_TYPE = '03' THEN B.ITEM2 END) AS SCHOOL_TYPE_DISPLAY_ORDER_H,");
		stb.append("	MAX(CASE WHEN A.SCHOOL_TYPE = '04' THEN A.SCHOOL_TYPE END) AS SCHOOL_TYPE_S,");
		stb.append("	MAX(CASE WHEN A.SCHOOL_TYPE = '04' THEN B.ITEM1 END) AS SCHOOL_TYPE_NAME_S,");
		stb.append("	MAX(CASE WHEN A.SCHOOL_TYPE = '04' THEN B.ITEM2 END) AS SCHOOL_TYPE_DISPLAY_ORDER_S,");
		stb.append("	MAX(CASE WHEN A.SCHOOL_TYPE = '05' THEN A.SCHOOL_TYPE END) AS SCHOOL_TYPE_K,");
		stb.append("	MAX(CASE WHEN A.SCHOOL_TYPE = '05' THEN B.ITEM1 END) AS SCHOOL_TYPE_NAME_K,");
		stb.append("	MAX(CASE WHEN A.SCHOOL_TYPE = '05' THEN B.ITEM2 END) AS SCHOOL_TYPE_DISPLAY_ORDER_K");
		stb.append(" FROM ");
		stb.append("	SAF_LECTURE_SCHOOL_TYPE A");
		stb.append("	INNER JOIN ");
		stb.append("	SAF_CODE_MASTER B ");
		stb.append("		ON B.CODE_ID = '0002'");
		stb.append("		AND B.CODE = A.SCHOOL_TYPE");
		stb.append("	");
		stb.append(" WHERE ");
		stb.append("	YEAR = '" + _year + "' ");               //画面からの入力年度を設定
		stb.append(" GROUP BY ");
		stb.append("	A.YEAR, ");
		stb.append("	A.TRAINING_TYPE_CODE, ");
		stb.append("	A.TRAINING_ID");
		stb.append("	");
		stb.append(")");
		stb.append("");


		stb.append(" SELECT ");
		stb.append("	B.TRAINING_TYPE_NAME,");
		stb.append("	C.SCHOOL_TYPE_NAME_P,");
		stb.append("	C.SCHOOL_TYPE_NAME_J,");
		stb.append("	C.SCHOOL_TYPE_NAME_H,");
		stb.append("	C.SCHOOL_TYPE_NAME_S,");
		stb.append("	C.SCHOOL_TYPE_NAME_K,");
		stb.append("	A.TRAINING_NO,");
		stb.append("	A.TRAINING_NAME,");
		stb.append("	A.ENFORCEMENT_DAYS,");
		stb.append("	A.CAPACITY,");
		stb.append("	COUNT(D.STAFFCD) AS APPLICANT_NUM,");
		stb.append("	COUNT(CASE WHEN D.ATTENDING_LECTURE_FLAG = '1' THEN 1 END) AS SAIYOU_NUM,");
		stb.append("	SUM(CASE WHEN E.DATE_1_ATTENDANCE = 0 THEN 1 ELSE 0 END)");
		stb.append("	+SUM(CASE WHEN E.DATE_2_ATTENDANCE = 0 THEN 1 ELSE 0 END)");
		stb.append("	+SUM(CASE WHEN E.DATE_3_ATTENDANCE = 0 THEN 1 ELSE 0 END)");
		stb.append("	+SUM(CASE WHEN E.DATE_4_ATTENDANCE = 0 THEN 1 ELSE 0 END)");
		stb.append("	+SUM(CASE WHEN E.DATE_5_ATTENDANCE = 0 THEN 1 ELSE 0 END)");
		stb.append("	+SUM(CASE WHEN E.DATE_6_ATTENDANCE = 0 THEN 1 ELSE 0 END)");
		stb.append("	+SUM(CASE WHEN E.DATE_7_ATTENDANCE = 0 THEN 1 ELSE 0 END)");
		stb.append("	+SUM(CASE WHEN E.DATE_8_ATTENDANCE = 0 THEN 1 ELSE 0 END)");
		stb.append("	+SUM(CASE WHEN E.DATE_9_ATTENDANCE = 0 THEN 1 ELSE 0 END)");
		stb.append("	+SUM(CASE WHEN E.DATE_10_ATTENDANCE = 0 THEN 1 ELSE 0 END)");
		stb.append("	+SUM(CASE WHEN E.DATE_11_ATTENDANCE = 0 THEN 1 ELSE 0 END)");
		stb.append("	+SUM(CASE WHEN E.DATE_12_ATTENDANCE = 0 THEN 1 ELSE 0 END)");
		stb.append("	+SUM(CASE WHEN E.DATE_13_ATTENDANCE = 0 THEN 1 ELSE 0 END)");
		stb.append("	+SUM(CASE WHEN E.DATE_14_ATTENDANCE = 0 THEN 1 ELSE 0 END)");
		stb.append("	+SUM(CASE WHEN E.DATE_15_ATTENDANCE = 0 THEN 1 ELSE 0 END)");
		stb.append("	+SUM(CASE WHEN E.DATE_16_ATTENDANCE = 0 THEN 1 ELSE 0 END)");
		stb.append("	+SUM(CASE WHEN E.DATE_17_ATTENDANCE = 0 THEN 1 ELSE 0 END)");
		stb.append("	+SUM(CASE WHEN E.DATE_18_ATTENDANCE = 0 THEN 1 ELSE 0 END)");
		stb.append("	+SUM(CASE WHEN E.DATE_19_ATTENDANCE = 0 THEN 1 ELSE 0 END)");
		stb.append("	+SUM(CASE WHEN E.DATE_20_ATTENDANCE = 0 THEN 1 ELSE 0 END) AS ABSENTEE_NUM,");
		stb.append("	SUM(CASE WHEN E.DATE_1_ATTENDANCE = 1 THEN 1 ELSE 0 END)");
		stb.append("	+SUM(CASE WHEN E.DATE_2_ATTENDANCE = 1 THEN 1 ELSE 0 END)");
		stb.append("	+SUM(CASE WHEN E.DATE_3_ATTENDANCE = 1 THEN 1 ELSE 0 END)");
		stb.append("	+SUM(CASE WHEN E.DATE_4_ATTENDANCE = 1 THEN 1 ELSE 0 END)");
		stb.append("	+SUM(CASE WHEN E.DATE_5_ATTENDANCE = 1 THEN 1 ELSE 0 END)");
		stb.append("	+SUM(CASE WHEN E.DATE_6_ATTENDANCE = 1 THEN 1 ELSE 0 END)");
		stb.append("	+SUM(CASE WHEN E.DATE_7_ATTENDANCE = 1 THEN 1 ELSE 0 END)");
		stb.append("	+SUM(CASE WHEN E.DATE_8_ATTENDANCE = 1 THEN 1 ELSE 0 END)");
		stb.append("	+SUM(CASE WHEN E.DATE_9_ATTENDANCE = 1 THEN 1 ELSE 0 END)");
		stb.append("	+SUM(CASE WHEN E.DATE_10_ATTENDANCE = 1 THEN 1 ELSE 0 END)");
		stb.append("	+SUM(CASE WHEN E.DATE_11_ATTENDANCE = 1 THEN 1 ELSE 0 END)");
		stb.append("	+SUM(CASE WHEN E.DATE_12_ATTENDANCE = 1 THEN 1 ELSE 0 END)");
		stb.append("	+SUM(CASE WHEN E.DATE_13_ATTENDANCE = 1 THEN 1 ELSE 0 END)");
		stb.append("	+SUM(CASE WHEN E.DATE_14_ATTENDANCE = 1 THEN 1 ELSE 0 END)");
		stb.append("	+SUM(CASE WHEN E.DATE_15_ATTENDANCE = 1 THEN 1 ELSE 0 END)");
		stb.append("	+SUM(CASE WHEN E.DATE_16_ATTENDANCE = 1 THEN 1 ELSE 0 END)");
		stb.append("	+SUM(CASE WHEN E.DATE_17_ATTENDANCE = 1 THEN 1 ELSE 0 END)");
		stb.append("	+SUM(CASE WHEN E.DATE_18_ATTENDANCE = 1 THEN 1 ELSE 0 END)");
		stb.append("	+SUM(CASE WHEN E.DATE_19_ATTENDANCE = 1 THEN 1 ELSE 0 END)");
		stb.append("	+SUM(CASE WHEN E.DATE_20_ATTENDANCE = 1 THEN 1 ELSE 0 END) AS STUDENT_NUM");
		stb.append(" FROM ");
		stb.append("	SAF_TRAINING_INFOMATION A ");
		stb.append("	INNER JOIN");
		stb.append("	SAF_TRAINING_TYPE B");
		stb.append("		ON B.YEAR = A.YEAR");
		stb.append("		AND B.TRAINING_TYPE_CODE = A.TRAINING_TYPE_CODE");
		stb.append("	INNER JOIN");
		stb.append("	SAF_TRAINING_SCHOOL_TYPE C");
		stb.append("		ON C.YEAR = A.YEAR");
		stb.append("		AND C.TRAINING_TYPE_CODE=A.TRAINING_TYPE_CODE");
		stb.append("		AND C.TRAINING_ID = A.TRAINING_ID");
		stb.append("	LEFT JOIN");
		stb.append("	SAF_TRAINING_APPLICATION D");
		stb.append("		ON D.YEAR = A.YEAR");
		stb.append("		AND D.TRAINING_TYPE_CODE = A.TRAINING_TYPE_CODE");
		stb.append("		AND D.TRAINING_ID = A.TRAINING_ID");
		stb.append("	LEFT JOIN");
		stb.append("	SAF_ATTENDANCE_CERTIFICATION E");
		stb.append("		ON E.YEAR = D.YEAR");
		stb.append("		AND E.TRAINING_TYPE_CODE = D.TRAINING_TYPE_CODE");
		stb.append("		AND E.TRAINING_ID = D.TRAINING_ID");
		stb.append("		AND E.ID = D.ID");
		stb.append("	INNER JOIN ");
		stb.append("	SAF_CODE_MASTER F");
		stb.append("		ON F.CODE_ID = '0106' ");
		stb.append("		AND F.CODE = A.TRAINING_TYPE_CODE");
		stb.append(" WHERE ");
		stb.append("	A.YEAR = '" + _year + "'");    //画面からの入力年度
		stb.append(" GROUP BY ");
		stb.append("	B.TRAINING_TYPE_NAME,");
		stb.append("	C.SCHOOL_TYPE_NAME_P,");
		stb.append("	C.SCHOOL_TYPE_NAME_J,");
		stb.append("	C.SCHOOL_TYPE_NAME_H,");
		stb.append("	C.SCHOOL_TYPE_NAME_S,");
		stb.append("	C.SCHOOL_TYPE_NAME_K,");
		stb.append("	F.DISPLAY_ORDER,");
		stb.append("	A.TERM,");
		stb.append("	A.TRAINING_NO,");
		stb.append("	A.TRAINING_NAME,");
		stb.append("	A.ENFORCEMENT_DAYS,");
		stb.append("	A.CAPACITY");
		stb.append(" ORDER BY ");
		stb.append("	F.DISPLAY_ORDER, ");
		stb.append("	A.TERM, ");
		stb.append("	A.TRAINING_NO ");
		return stb.toString();
	}

	//エラーメッセージ取出し
	public String getErrorMessage() {
		return errorMessage;
	}


	private class TrAttendanceRecord{

		final String TRAINING_TYPE_NAME;
		final String SCHOOL_TYPE_P;
		final String SCHOOL_TYPE_J;
		final String SCHOOL_TYPE_H;
		final String SCHOOL_TYPE_S;
		final String SCHOOL_TYPE_K;
		final int TRAINING_NO;
		final String TRAINING_NAME;
		final int ENFORCEMENT_DAYS;
		final int CAPACITY;
		final int APPLICANT_NUM;
		final int SAIYOU_NUM;
		final int ABSENTEE_NUM;
		final int STUDENT_NUM;


		TrAttendanceRecord(final String TRAINING_TYPE_NAME,final String SCHOOL_TYPE_P,final String SCHOOL_TYPE_J,final String SCHOOL_TYPE_H,final String SCHOOL_TYPE_S, final String SCHOOL_TYPE_K, final int TRAINING_NO, final String TRAINING_NAME, final int ENFORCEMENT_DAYS, final int CAPACITY, final int APPLICANT_NUM, final int SAIYOU_NUM, final int ABSENTEE_NUM, final int STUDENT_NUM){
			this.TRAINING_TYPE_NAME=TRAINING_TYPE_NAME;
			this.SCHOOL_TYPE_P=SCHOOL_TYPE_P;
			this.SCHOOL_TYPE_J=SCHOOL_TYPE_J;
			this.SCHOOL_TYPE_H=SCHOOL_TYPE_H;
			this.SCHOOL_TYPE_S=SCHOOL_TYPE_S;
			this.SCHOOL_TYPE_K=SCHOOL_TYPE_K;
			this.TRAINING_NO=TRAINING_NO;
			this.TRAINING_NAME=TRAINING_NAME;
			this.ENFORCEMENT_DAYS=ENFORCEMENT_DAYS;
			this.CAPACITY=CAPACITY;
			this.APPLICANT_NUM=APPLICANT_NUM;
			this.SAIYOU_NUM=SAIYOU_NUM;
			this.ABSENTEE_NUM=ABSENTEE_NUM;
			this.STUDENT_NUM=STUDENT_NUM;
		}

		public String getTRAINING_TYPE_NAME() { //研修種別　（出力用)
			return TRAINING_TYPE_NAME;
		}

		private String getSCHOOL_TYPE_P() {  //校種1　小学校
			return SCHOOL_TYPE_P;
		}

		private String getSCHOOL_TYPE_J() {  //校種2　中学校
			return SCHOOL_TYPE_J;
		}

		private String getSCHOOL_TYPE_H() {  //校種3　高校
			return SCHOOL_TYPE_H;
		}

		private String getSCHOOL_TYPE_S() {  //校種4　特別支援学校
			return SCHOOL_TYPE_S;
		}

		private String getSCHOOL_TYPE_K() {  //校種5　幼稚園
			return SCHOOL_TYPE_K;
		}

		public String getSCHOOL_TYPE() { //校種1～5 カンマ結合　（出力用）
			String type1=getSCHOOL_TYPE_P();
			String type2=getSCHOOL_TYPE_J();
			String type3=getSCHOOL_TYPE_H();
			String type4=getSCHOOL_TYPE_S();
			String type5=getSCHOOL_TYPE_K();
			String str = "";
			if ((type1 != null)&&(type1.length()>0)) {
				str = type1;
			}
			if ((type2 != null)&&(type2.length()>0)) {
				if (str.length() > 0) {
					str += ("," + type2);
				}
				else {
					str = type2;
				}
			}
			if ((type3 != null)&&(type3.length()>0)) {
				if (str.length() > 0) {
					str += ("," + type3);
				}
				else {
					str = type3;
				}
			}
			if ((type4 != null)&&(type4.length()>0)) {
				if (str.length() > 0) {
					str += ("," + type4);
				}
				else {
					str = type4;
				}
			}
			if ((type5 != null)&&(type5.length()>0)) {
				if (str.length() > 0) {
					str += ("," + type5);
				}
				else {
					str = type5;
				}
			}
			return str;
			/*return Stream.of(type1, type2, type3, type4, type5)
					      .filter(s -> null != s)
					      .collect(Collectors.joining(","));*/
		}


		public int getTRAINING_NO() { //研修番号　（出力用)
			return TRAINING_NO;
		}

		public String getTRAINING_NAME() { //研修名　（出力用)
			return TRAINING_NAME;
		}

		public int getENFORCEMENT_DAYS() { //日数　（出力用)
			return ENFORCEMENT_DAYS;
		}

		public int getCAPACITY() { //定員 （出力用)
			return CAPACITY;
		}

		public int getAPPLICANT_NUM() { //申込者数 （出力用)
			return APPLICANT_NUM;
		}

		public int getSAIYOU_NUM() { //採用者数　（出力用)
			return SAIYOU_NUM;
		}

		public int getABSENTEE_NUM() { //欠席者延人数　（出力用)
			return ABSENTEE_NUM;
		}

		public int getSTUDENT_NUM() { //受講者延人数 （出力用)
			return STUDENT_NUM;
		}

		public double getATTENDANCE_RATE() {
			final double CAPACTITY=getCAPACITY();
			final int APPLICANT_NUM = getSAIYOU_NUM();

			if(CAPACITY>0) {

				double rate=(APPLICANT_NUM/(double)CAPACITY)*100;

				//受講率の小数点第二位での四捨五入
				BigDecimal bi =BigDecimal.valueOf(rate);
				double result_rate=bi.setScale(1,RoundingMode.HALF_UP).doubleValue();

				return result_rate;
			}

			return 0;
		}
	}

	private List<TrAttendanceRecord> getRecordList(String sqlT){

		/*
		 *DB接続処理
		 */

		//データベースリソース
		final String JDBC_LOOKUP = Prop.getProperty("jdbc.lookup");
		// フィールドデータをデータベースから取出す
		Statement stmt = null;
		// データベース操作オブジェクト生成
		util.DaoUtil dao = new util.DaoUtil(Prop.getProperty("sqllogging").equalsIgnoreCase("true"), log);
		// データベースオープン
		stmt = dao.open(JDBC_LOOKUP);
		if (stmt == null) {
			// オープン失敗
			errorMessage = "データベース接続失敗";
			log.error("dao.open() error");
			return null;
		}

		log.info("データベース接続成功");


		/*
		 * DBデータ取得処理
		 */

		List<TrAttendanceRecord> recordlist=new ArrayList<TrAttendanceRecord>();


		try (ResultSet rs = dao.query(sqlT)) {
			log.info("-------------SQL 開始------------");
			errorMessage = dao.getErrTrace();
			if (errorMessage.length() > 0) { // sql例外発生
				return null;
			}



			while (rs.next()) {
				log.info("sql成功"+rs.getString("SCHOOL_TYPE_NAME_P"));

				final String TRAINING_TYPE_NAME=rs.getString("TRAINING_TYPE_NAME");
				final String SCHOOL_TYPE_P=rs.getString("SCHOOL_TYPE_NAME_P");
				final String SCHOOL_TYPE_J=rs.getString("SCHOOL_TYPE_NAME_J");
				final String SCHOOL_TYPE_H=rs.getString("SCHOOL_TYPE_NAME_H");
				final String SCHOOL_TYPE_S=rs.getString("SCHOOL_TYPE_NAME_S");
				final String SCHOOL_TYPE_K=rs.getString("SCHOOL_TYPE_NAME_K");
				final int TRAINING_NO=rs.getInt("TRAINING_NO");
				final String TRAINING_NAME=rs.getString("TRAINING_NAME");
				final int ENFORCEMENT_DAYS=rs.getInt("ENFORCEMENT_DAYS");
				final int CAPACITY=rs.getInt("CAPACITY");
				final int APPLICANT_NUM=rs.getInt("APPLICANT_NUM");
				final int SAIYOU_NUM=rs.getInt("SAIYOU_NUM");
				final int ABSENTEE_NUM=rs.getInt("ABSENTEE_NUM");
				final int STUDENT_NUM=rs.getInt("STUDENT_NUM");

				TrAttendanceRecord record=new TrAttendanceRecord(TRAINING_TYPE_NAME,SCHOOL_TYPE_P,SCHOOL_TYPE_J,SCHOOL_TYPE_H,SCHOOL_TYPE_S,SCHOOL_TYPE_K,TRAINING_NO,TRAINING_NAME,ENFORCEMENT_DAYS,CAPACITY,APPLICANT_NUM,SAIYOU_NUM,ABSENTEE_NUM,STUDENT_NUM);

				recordlist.add(record);
			}

		}catch(SQLException e) {
			log.info("DBからデータの取り出し失敗", e);
			return null;
		}

		dao.close();

		log.info("データ取得成功");

		return recordlist;

	}

}


