package mkexcel;

//import java.sql.PreparedStatement;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
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

//
//専門研修の年度･校種･事務所別申込率
//
public class Tr_Sagcr_07_8 {
	Log log = LogFactory.getLog(Tr_Sagcr_07_8.class);
	// メンバー変数
	private Workbook book;
	private final static String USE_SHEETNAME = "専門研修の年度･校種･事務所別申込率";
	private Alp_Properties Prop;
	private String errorMessage = "";
	final static int D_LINE = 3;
	private PrintData[][] primarySchool = new PrintData[5][D_LINE];
	private PrintData[][] juniorhighSchool = new PrintData[5][D_LINE];
	private PrintData[][] highSchool = new PrintData[5][D_LINE];
	// 書込みデータ
	String _year = "";
	String _syear = "";

	public Tr_Sagcr_07_8(Workbook book, String information, Alp_Properties prop) {
		this.book = book;
		this.Prop = prop;
		String[] Information = information.split(",");
		if (Information.length < 1) {
			errorMessage = String.format("パラメータエラー{%s}", information);
		} else {
			_year = Information[0];  //★yearは上位からパラメータでわたってくる。
			_syear = String.valueOf(Integer.parseInt(_year) - 4); //★年度-4
			//名前を付けて保存ファイル名を変更
			Calendar cl = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
			String fileName = Prop.getOutputName() + "_" + String.valueOf(_year) + "年度_" + sdf.format(cl.getTime());
			Prop.setOutputName(fileName);
		}
		log.info("Tr_Sagcr_07_8 コンストラクタ パラメータ{" + information + "}");
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
		Statement db2pt = null;

		// データベース操作オブジェクト生成
		util.DaoUtil db2 = new util.DaoUtil(Prop.getProperty("sqllogging")
				.equalsIgnoreCase("true"), log);
		db2pt = db2.open(JDBC_LOOKUP);
		if (db2pt == null) {
			// オープン失敗
			errorMessage = "データベース接続失敗";
			log.error("dao.open() error");
			return false;
		}

		//メイン処理
		try {
			ResultSet rs = null;
			int maxcnt;
			//小学校・中学校
			String[] type = {"01","02"};
			int start_Year = Integer.parseInt(_syear);
			int stop_Year = Integer.parseInt(_year);
			for(int t=0; t< 2; t++) {
				for(int y=start_Year; y <= stop_Year; y++) {
					String sqlA = getCountDataSqlA(String.valueOf(y), type[t]);
					//log.info("小学校・中学校:" + sqlA);
					log.info("小学校・中学校:");
					rs = db2.query(sqlA);
					maxcnt = 0;
					int yp = y - start_Year;
					while(rs.next()) {
						int staffCount = rs.getInt("STAFF_COUNT");
						int total = rs.getInt("MOSHIKOMI_COUNT");
						BigDecimal percent = rs.getBigDecimal("MOSHIKOMI_RITSU");
						if (t == 0) {	//小学校
							primarySchool[yp][maxcnt] = new PrintData(staffCount, total, percent);
						}
						else {	//中学校
							juniorhighSchool[yp][maxcnt] = new PrintData(staffCount, total, percent);
						}
						if (++maxcnt >= D_LINE) break;
					}
					rs.close();
				}
			}
			//pstmt.close();
			//県立学校
			for(int y=start_Year; y <= stop_Year; y++) {
				String sqlB = getCountDataSqlB(String.valueOf(y));
				//log.info("県立学校:" + sqlB);
				log.info("県立学校:");
				rs = db2.query(sqlB);
				maxcnt = 0;
				int yp = y - start_Year;
				while(rs.next()) {
					int staffCount = rs.getInt("STAFF_COUNT");
					int total = rs.getInt("MOSHIKOMI_COUNT");
					BigDecimal percent = rs.getBigDecimal("MOSHIKOMI_RITSU");
					highSchool[yp][maxcnt] = new PrintData(staffCount, total, percent);
					if (++maxcnt >= 3) break;
				}
				rs.close();
			}
			//pstmt.close();
			returnCode = setRowsInfo();
		} catch (Exception e) {
			errorMessage = e.toString();
			db2.StackTraceLogging(e);
		}finally {
			//クローズ
			db2.close();
		}
		return returnCode;
	}

	//Excelへ値を格納
	private boolean setRowsInfo() {

		Row row;
		// 書込みシートを開く
		Sheet sheet = book.getSheet(USE_SHEETNAME);
		if (sheet == null) {
			errorMessage = "対象のシート見つからず";
			return false;
		}
		SetColumn set = new SetColumn();
		set.setSheet(sheet);

		String strWk = "専門研修の年度･校種･事務所別申込率";
		set.setValueString(1, "A", strWk);

		//"出力日：";
		java.util.Date date = new java.util.Date();
		SimpleDateFormat fmt1 = new SimpleDateFormat("yyyy/MM/dd");
		String dstrWk = "出力日：" + fmt1.format(date);
		set.setValueString(1, "P", dstrWk);

		//年度タイトル
		int strtrowh = 3; //出力開始行-1
		int posh = 1; //出力開始位置
		for (int y = Integer.parseInt(_syear); y <= Integer.parseInt(_year); y++) {
			String stry = String.valueOf(y) + "年度";
			row = sheet.getRow(strtrowh);
			setData(row.getCell(posh), stry);
			posh += 3;
		}
		   //20200127_中学校の年度表示位置(X軸変更(12->10)
		strtrowh = 10; //出力開始行-1
		posh = 1; //出力開始位置
		for (int y = Integer.parseInt(_syear); y <= Integer.parseInt(_year); y++) {
			String stry = String.valueOf(y) + "年度";
			row = sheet.getRow(strtrowh);
			setData(row.getCell(posh), stry);
			posh += 3;
		}
		   //20200127_県立学校の年度表示位置(X軸変更(21->17)
		strtrowh = 17; //出力開始行-1
		posh = 1; //出力開始位置
		for (int y = Integer.parseInt(_syear); y <= Integer.parseInt(_year); y++) {
			String stry = String.valueOf(y) + "年度";
			row = sheet.getRow(strtrowh);
			setData(row.getCell(posh), stry);
			posh += 3;
		}

		//地区・教職員数・申込者数・申込率
		int start_Year = Integer.parseInt(_syear);
		int stop_Year = Integer.parseInt(_year);
		log.info("strat:"+start_Year+" stop:"+stop_Year);
		//小学校
		strtrowh = 5; //出力開始行-1
		for(int l=0; l < D_LINE; l++) {
			row = sheet.getRow(strtrowh + l);
			//setData(row.getCell(0), oldDistrict[l]);
			int pos = 1; //出力開始位置
			for(int y=start_Year; y <= stop_Year; y++) {
				int yp = y - start_Year;
				if ( primarySchool[yp][l] == null) {
					log.info("null l:"+l + " yp:" + yp + " pos:" + pos);
					continue;
				}
				setDataI(row.getCell(pos++), primarySchool[yp][l].getStaffCount());
				setDataI(row.getCell(pos++), primarySchool[yp][l].getTotalCount());
				setDecimal(row.getCell(pos++), primarySchool[yp][l].getPercent());
			}
		}
		//中学校
		strtrowh = 12; //出力開始行-1
		for(int l=0; l < D_LINE; l++) {
			row = sheet.getRow(strtrowh + l);
			//setData(row.getCell(0), oldDistrict[l]);
			int pos = 1; //出力開始位置
			for(int y=start_Year; y <= stop_Year; y++) {
				int yp = y - start_Year;
				if ( juniorhighSchool[yp][l] == null) {
					log.info("null l:"+l + " yp:" + yp + " pos:" + pos);
					continue;
				}
				setDataI(row.getCell(pos++), juniorhighSchool[yp][l].getStaffCount());
				setDataI(row.getCell(pos++), juniorhighSchool[yp][l].getTotalCount());
				setDecimal(row.getCell(pos++), juniorhighSchool[yp][l].getPercent());
			}
		}
		//県立学校
		strtrowh = 19; //出力開始行-1
		for(int l=0; l < D_LINE; l++) {
			row = sheet.getRow(strtrowh + l);
			int pos = 1; //出力開始位置
			for(int y=start_Year; y <= stop_Year; y++) {
				int yp = y - start_Year;
				if ( highSchool[yp][l] == null) {
					log.info("null l:"+l + " yp:" + yp + " pos:" + pos);
					continue;
				}
				setDataI(row.getCell(pos++), highSchool[yp][l].getStaffCount());
				setDataI(row.getCell(pos++), highSchool[yp][l].getTotalCount());
				setDecimal(row.getCell(pos++), highSchool[yp][l].getPercent());
			}
		}

		//算出セルを実行
		int rowStart = sheet.getFirstRowNum();
		int rowEnd = sheet.getLastRowNum();
		for(int i = rowStart; i <= rowEnd; i++) {
			row = sheet.getRow(i);
			int cellStart = row.getFirstCellNum() >= 0 ? row.getFirstCellNum() : 0;
			int cellEnd = row.getLastCellNum();
			//log.info(i + "開始[" + row.getFirstCellNum() + "] 終了[" + row.getLastCellNum() + "]");
			for(int l = cellStart; l <= cellEnd; l++) {
				Cell cel = row.getCell(l);
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

	private void setData(final Cell cel, final String vals) {
		if (cel != null) {
			cel.setCellValue(vals);
		}
	}
	private void setDataI(final Cell cel, final int vals) {
		if (cel != null) {
			cel.setCellValue(vals);
		}
	}

	private void setDecimal(final Cell cel, BigDecimal vals) {
		if (cel != null) {
			double d = Double.parseDouble(vals.toString());
			cel.setCellValue(d);
		}
	}

	//データ保持クラス
	private class PrintData {
		int staffCount;
		int totalCount;
		BigDecimal percent;
		public PrintData() {
			this.staffCount = 0;
			this.totalCount = 0;
			this.percent = BigDecimal.valueOf(0.0);
		}
		public PrintData(int staffCount, int totalCount, BigDecimal percent) {
			this.staffCount = staffCount;
			this.totalCount = totalCount;
			this.percent = percent;
		}
		public int getStaffCount() {
			return staffCount;
		}
		public int getTotalCount() {
			return totalCount;
		}
		public BigDecimal getPercent() {
			return percent;
		}
	}

	//小･中学校
	private String getCountDataSqlA(String year, String type) {
		String sql = "SELECT"
		+ " P01.DISTRICT"
		+ " , P01.ITEM1 AS PRIMARY_SCHOOL"
		+ " , P01.STAFF_COUNT"
		+ " , NVL(P02.TOTAL, 0) AS MOSHIKOMI_COUNT"
		+ " , CASE"
		+ " WHEN P01.STAFF_COUNT = 0"
		+ " THEN 0.0"
		+ " ELSE CAST("
		+ " ROUND("
		+ " DEC (NVL(P02.TOTAL, 0)) / DEC (P01.STAFF_COUNT) * 100"
		+ " , 1"
		+ " ) AS DECIMAL (10, 1)"
		+ " )"
		+ " END AS MOSHIKOMI_RITSU"
		+ " FROM"
		+ " ("
		+ " SELECT"
		+ " C.DISTRICT"
		+ " , COUNT(DISTINCT B.STAFFCD) AS STAFF_COUNT"
		+ " , E.ITEM1"
		+ " FROM"
		+ " ( SELECT * "
		+ " FROM SAF_STAFF_JOB_G_DAT"
		+ " WHERE"
        + "      FROM_DATE <= '" + year + "-05-01'"
        + " AND (TO_DATE IS NULL"
        + " OR '" + year + "-05-01' <= TO_DATE)"
        + " ) B "
		+ " LEFT JOIN V_SCHOOL_BASIS C"
		+ " ON C.SCHOOLID = B.FROM_SCHOOLCD"
		+ " LEFT JOIN SAF_SCHOOL_EXTRA_G_DAT D"
		+ " ON D.SCHOOLID = B.FROM_SCHOOLCD"
		+ " LEFT JOIN SAF_CODE_MASTER E"
		+ " ON C.DISTRICT = E.CODE"
		+ " AND E.CODE_ID = '0014'"
		+ " WHERE"
        + "     B.BONUS_WORKER_FLAG = '0'"
        + " AND B.PRIORITY= '1'"
        + " AND B.BONUS_WORKER_FLAG = '0'"
		+ " AND B.PRIORITY = '1'"
		+ " AND B.FROM_SCHOOLCD NOT IN ("
		+ " SELECT"
		+ " CODE"
		+ " FROM"
		+ " SAF_CODE_MASTER"
		+ " WHERE"
		+ " CODE_ID = '0102'"
		+ " )"
		+ " AND C.ESTABLISHED_SEGMENTS IN ('2', '3')"
		+ " AND C.SCHOOL_TYPE = '" + type + "'"
		+ " AND D.FACULTY_FLAG = '0'"
		+ " GROUP BY"
		+ " C.DISTRICT"
		+ " , E.ITEM1"
		+ " ) P01"
		+ " LEFT JOIN ("
		+ " SELECT"
		+ " A.YEAR"
		+ " , B.DISTRICT"
		+ " , D.ITEM1"
		+ " , VALUE(COUNT(DISTINCT CASE WHEN A.STAFFCD <> '0' THEN A.STAFFCD END), 0) + SUM(CASE WHEN A.STAFFCD = '0' THEN 1 ELSE 0 END)"
		+ "  AS TOTAL"
		+ " FROM"
		+ " SAF_TRAINING_APPLICATION A"
		+ " LEFT JOIN V_SCHOOL_BASIS B"
		+ " ON B.SCHOOLID = A.BELONG_SCHOOLID"
		+ " LEFT JOIN SAF_SCHOOL_EXTRA_G_DAT C"
		+ " ON C.SCHOOLID = A.BELONG_SCHOOLID"
		+ " LEFT JOIN SAF_CODE_MASTER D"
		+ " ON B.DISTRICT = D.CODE"
		+ " AND D.CODE_ID = '0014'"
		+ " WHERE"
		+ " A.YEAR = '" + year + "'"
		+ " AND A.TRAINING_TYPE_CODE IN ("
		+ " SELECT"
		+ " TRAINING_TYPE_CODE"
		+ " FROM"
		+ " SAF_TRAINING_TYPE"
		+ " WHERE"
		+ " TRAINING_TYPE IN ('2', '3')"
		+ " )"
		+ " AND A.DELETER_OFFICE = '0'"
		+ " AND B.ESTABLISHED_SEGMENTS IN ('2', '3')"
		+ " AND B.SCHOOL_TYPE = '" + type + "'"
		+ " AND C.FACULTY_FLAG = '0'"
		+ " GROUP BY"
		+ " A.YEAR"
		+ " , B.DISTRICT"
		+ " , D.ITEM1"
		+ " ) P02"
		+ " ON P01.DISTRICT = P02.DISTRICT";
		return sql;
	}

	//県立学校
	private String getCountDataSqlB(String year) {
		String sql = "SELECT"
		+ " P01.SCHOOL_TYPE"
		+ " , P01.ITEM1 AS PREFECTURAL_SCHOOL"
		+ " , P01.STAFF_COUNT"
		+ " , NVL(P02.TOTAL, 0) AS MOSHIKOMI_COUNT"
		+ " , CASE"
		+ " WHEN P01.STAFF_COUNT = 0"
		+ " THEN 0.0"
		+ " ELSE CAST("
		+ " ROUND("
		+ " DEC (NVL(P02.TOTAL, 0)) / DEC (P01.STAFF_COUNT) * 100"
		+ " , 1) AS DECIMAL (10, 1)"
		+ " )"
		+ " END AS MOSHIKOMI_RITSU"
		+ " FROM ("
		+ " SELECT"
		+ " C.SCHOOL_TYPE"
		+ " , COUNT(DISTINCT B.STAFFCD) AS STAFF_COUNT"
		+ " , E.ITEM1"
		+ " FROM"
		+ " ( SELECT * "
		+ " FROM SAF_STAFF_JOB_G_DAT"
		+ " WHERE"
        + "      FROM_DATE <= '" + year + "-05-01'"
        + " AND (TO_DATE IS NULL"
        + " OR '" + year + "-05-01' <= TO_DATE)"
        + " ) B "
		+ " LEFT JOIN V_SCHOOL_BASIS C"
		+ " ON C.SCHOOLID = B.FROM_SCHOOLCD"
		+ " LEFT JOIN SAF_SCHOOL_EXTRA_G_DAT D"
		+ " ON D.SCHOOLID = B.FROM_SCHOOLCD"
		+ " LEFT JOIN SAF_CODE_MASTER E"
		+ " ON C.SCHOOL_TYPE = E.CODE"
		+ " AND E.CODE_ID = '0002'"
		+ " WHERE"
        + "     B.BONUS_WORKER_FLAG = '0'"
        + " AND B.PRIORITY= '1'"
        + " AND B.JOBCD <> '812'"
		+ " AND B.BONUS_WORKER_FLAG = '0'"
		+ " AND B.PRIORITY = '1'"
		+ " AND B.FROM_SCHOOLCD NOT IN ("
		+ " SELECT"
		+ " CODE"
		+ " FROM"
		+ " SAF_CODE_MASTER"
		+ " WHERE"
		+ " CODE_ID = '0102'"
		+ " )"
		+ " AND C.ESTABLISHED_SEGMENTS = '1'"
		+ " AND ("
		+ " C.SCHOOL_TYPE IN ('02', '03')"
		+ " AND D.FACULTY_FLAG = '0'"
		+ " OR C.SCHOOL_TYPE = '04'"
		+ " OR D.FACULTY_FLAG = '1'"
		+ " )"
		+ " GROUP BY"
		+ " C.SCHOOL_TYPE"
		+ " , E.ITEM1"
		+ " ) P01"
		+ " LEFT JOIN ("
		+ " SELECT"
		+ " A.YEAR"
		+ " , B.SCHOOL_TYPE"
		+ " , D.ITEM1"
		+ " , VALUE(COUNT(DISTINCT CASE WHEN A.STAFFCD <> '0' THEN A.STAFFCD END), 0) + SUM(CASE WHEN A.STAFFCD = '0' THEN 1 ELSE 0 END) AS"
		+ " TOTAL"
		+ " FROM"
		+ " SAF_TRAINING_APPLICATION A"
		+ " LEFT JOIN V_SCHOOL_BASIS B"
		+ " ON B.SCHOOLID = A.BELONG_SCHOOLID"
		+ " LEFT JOIN SAF_SCHOOL_EXTRA_G_DAT C"
		+ " ON C.SCHOOLID = A.BELONG_SCHOOLID"
		+ " LEFT JOIN SAF_CODE_MASTER D"
		+ " ON B.SCHOOL_TYPE = D.CODE"
		+ " AND D.CODE_ID = '0002'"
		+ " WHERE"
		+ " A.YEAR = '" + year + "'"
		+ " AND A.TRAINING_TYPE_CODE IN ("
		+ " SELECT"
		+ " TRAINING_TYPE_CODE"
		+ " FROM"
		+ " SAF_TRAINING_TYPE"
		+ " WHERE"
		+ " TRAINING_TYPE IN ('2', '3')"
		+ " )"
		+ " AND A.DELETER_OFFICE = '0'"
		+ " AND B.ESTABLISHED_SEGMENTS = '1'"
		+ " AND ("
		+ " B.SCHOOL_TYPE IN ('02', '03')"
		+ " AND C.FACULTY_FLAG = '0'"
		+ " OR B.SCHOOL_TYPE = '04'"
		+ " OR C.FACULTY_FLAG = '1'"
		+ " )"
		+ " GROUP BY"
		+ " A.YEAR"
		+ " , B.SCHOOL_TYPE"
		+ " , D.ITEM1"
		+ " ) P02"
		+ " ON P01.SCHOOL_TYPE = P02.SCHOOL_TYPE";
		return sql;
	}
}
