package mkexcel;

import java.nio.charset.Charset;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import beans.CourseAttendListLine;
import util.Alp_Properties;
import util.SetColumn;

////受講履歴一覧
public class Tr_Sagcr_07_3 {
	Log log = LogFactory.getLog(Tr_Sagcr_07_3.class);
	// メンバー変数
	private Workbook book;
	private final static String USE_SHEETNAME1 = "事務所別の申込者数";
	private final static String USE_SHEETNAME2 = "事務所別の受講者数";
	private final static String OUT_TYPE_0 = "0";  //申込者数
	private final static String OUT_TYPE_1 = "1";  //受講者数
	private Alp_Properties Prop;
	private String errorMessage = "";
	final static int MAX_LINE = 20;
	private String _outputType = "";
	private String _useSheetName = "";
	// 書込みデータ
	String _year = "";
	List<CourseAttendListLine> lines = new ArrayList<>();

	public Tr_Sagcr_07_3(Workbook book, String information, Alp_Properties prop) {
		this.book = book;
		String[] Information = information.split(",");
		if (Information.length < 2) {
			errorMessage = String.format("パラメータエラー{%s}", information);
		} else {
			_year = Information[0];  //yearは上位からパラメータでわたってくる。
			_outputType = Information[1];
			_useSheetName = _outputType.equals(OUT_TYPE_1) ? USE_SHEETNAME2 : USE_SHEETNAME1;
		}
		this.Prop = prop;
        log.warn("$Revision: 67753 $");
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
			List<PrintData> DatList = getCountData(db2);
			returnCode = setRowsInfo(db2, DatList);
		} catch (Exception e) {
			errorMessage = e.toString();
			db2.StackTraceLogging(e);
		}finally {
			//クローズ
			if (db2pt != null) {
			    db2.close();
			}
		}
		return returnCode;
	}

	//年月日変換関数
	private String Enforcement_date(String str) {
		String enforcement = "";
		if (str != null) {
			String[] dCutStr = str.split("/");
			if (dCutStr.length >= 3) {
				enforcement = String.format("%s年%s月%s日", dCutStr[0], dCutStr[1], dCutStr[2]);
			}
		}
		return enforcement;
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

	private boolean setRowsInfo(final util.DaoUtil db2, final List<PrintData> DatList) throws SQLException {
		boolean retFlg = false;
		Row row;
		// 書込みシートを開く
		Sheet sheet = book.getSheet(_useSheetName);
		if (sheet == null) {
			errorMessage = "対象のシート見つからず";
			return retFlg;
		}
		SetColumn set = new SetColumn();
		set.setSheet(sheet);

		//"校種・事務所別の申込者数【2018年度】";
		final String strWk;
		strWk = _useSheetName + "【" + _year + "年度】";
		set.setValueString(1, "A", strWk);

		//"出力日：";
		Date date = new Date();
		SimpleDateFormat fmt1 = new SimpleDateFormat("yyyy/MM/dd");
		String dstrWk = "出力日：" + fmt1.format(date);
		set.setValueString(2, "S", dstrWk);


		int dataidx = 0;
		int strtrow = 4;  //出力開始行-1
		for (Iterator<PrintData> ite = DatList.iterator();ite.hasNext();) {
			PrintData outinfo = (PrintData)ite.next();
			row = sheet.getRow(strtrow + dataidx);
			int pos = 0; //出力開始位置(次行処理でリセット)
			int maxlengthWk = 0;

			setData(row.getCell(pos++), outinfo._Training_Type_Name); //研修種別
            maxlengthWk = setMaxLengthChk(maxlengthWk,getCellRows(outinfo._Training_Type_Name, 17));
			setData(row.getCell(pos++), outinfo._Schkind_Nm);      //対象校種
            maxlengthWk = setMaxLengthChk(maxlengthWk,getCellRows(outinfo._Schkind_Nm, 20));
			setData(row.getCell(pos++), outinfo._Training_No);      //研修番号
            maxlengthWk = setMaxLengthChk(maxlengthWk,getCellRows(outinfo._Training_No, 7));
			setData(row.getCell(pos++), outinfo._Training_Name);      //研修名
            maxlengthWk = setMaxLengthChk(maxlengthWk,getCellRows(outinfo._Training_Name, 33));
			setData(row.getCell(pos++), outinfo._Enforcement_Days);      //日数
            maxlengthWk = setMaxLengthChk(maxlengthWk,getCellRows(outinfo._Enforcement_Days, 4));
			setData(row.getCell(pos++), outinfo._Enforcement_Date);      //期日
            maxlengthWk = setMaxLengthChk(maxlengthWk,getCellRows(outinfo._Enforcement_Date, 26));
			setData(row.getCell(pos++), outinfo._Capacity);      //定員
            maxlengthWk = setMaxLengthChk(maxlengthWk,getCellRows(outinfo._Capacity, 5));

			setData(row.getCell(pos++), outinfo._Sasiro_Cnt);   //旧佐城
			setData(row.getCell(pos++), outinfo._Mikami_Cnt);   //旧三神
			setData(row.getCell(pos++), outinfo._Tousyou_Cnt);  //旧東松浦
			setData(row.getCell(pos++), outinfo._Kinenisi_Cnt); //旧杵西
			setData(row.getCell(pos++), outinfo._Fujitu_Cnt);   //旧藤津

			setData(row.getCell(pos++), outinfo._J_Prefecture_Cnt);          //県立 中学
			setData(row.getCell(pos++), outinfo._H_Prefecture_Normal_Cnt);   //高等学校
			setData(row.getCell(pos++), outinfo._H_Prefecture_Special_Cnt);  //特別支援学校
			setData(row.getCell(pos++), outinfo._College_Cnt);               //付属
			setData(row.getCell(pos++), outinfo._Other);                     //その他

			final String OtherInfo = getOtherInfo(db2, outinfo._Training_Id);
			if (!"".equals(OtherInfo)) {
                setData(row.getCell(pos), OtherInfo); //その他内訳
			}
			pos++; //※if文により出力しなくても、出力位置をずらす必要がある。
            maxlengthWk = setMaxLengthChk(maxlengthWk,getCellRows(OtherInfo, 20));

			setData(row.getCell(pos++), outinfo._Total_Cnt); //合計

			row.setHeightInPoints((float)15.0 * (float)maxlengthWk);
		    dataidx++;
		    retFlg = true;
		}
		if (retFlg) {
			//印刷範囲設定。
			//引数は、シートindex,開始列index,終了列index,開始行index,終了行index
			book.setPrintArea(0, 0, 18, 0, dataidx + strtrow - 1);
		}
        return retFlg;
	}

	private void setData(final Cell cel, final String vals) {
		if (cel != null) {
			cel.setCellValue(vals);
		}

	}

	private List<PrintData> getCountData(final util.DaoUtil db2) {
		List<PrintData> retList = new ArrayList<>();
		ResultSet rs = null;

		String sql = getCountDataSql();
		log.warn(" sql = " + sql);
		try {
			rs = db2.query(sql);
			errorMessage = db2.getErrTrace();
			if (errorMessage.length() > 0) { // sql例外発生
				return retList;
			}
			while(rs.next()) {
				final String Training_Type_Name = rs.getString("TRAINING_TYPE_NAME");
				final String Display_Order = rs.getString("DISPLAY_ORDER");
				final String Term = rs.getString("TERM");
				final String Training_Id = rs.getString("TRAINING_ID");
				final String Training_No = rs.getString("TRAINING_NO");
				final String Training_Name = rs.getString("TRAINING_NAME");
				final String Enforcement_Days = rs.getString("ENFORCEMENT_DAYS");
				final String Capacity = rs.getString("CAPACITY");

				String edOutWk = "";
				edOutWk = concatStrWk(edOutWk, rs.getString("ENFORCEMENT_DATE_1"));
				edOutWk = concatStrWk(edOutWk, rs.getString("ENFORCEMENT_DATE_2"));
				edOutWk = concatStrWk(edOutWk, rs.getString("ENFORCEMENT_DATE_3"));
				edOutWk = concatStrWk(edOutWk, rs.getString("ENFORCEMENT_DATE_4"));
				edOutWk = concatStrWk(edOutWk, rs.getString("ENFORCEMENT_DATE_5"));
				edOutWk = concatStrWk(edOutWk, rs.getString("ENFORCEMENT_DATE_6"));
				edOutWk = concatStrWk(edOutWk, rs.getString("ENFORCEMENT_DATE_7"));
				edOutWk = concatStrWk(edOutWk, rs.getString("ENFORCEMENT_DATE_8"));
				edOutWk = concatStrWk(edOutWk, rs.getString("ENFORCEMENT_DATE_9"));
				edOutWk = concatStrWk(edOutWk, rs.getString("ENFORCEMENT_DATE_10"));
				edOutWk = concatStrWk(edOutWk, rs.getString("ENFORCEMENT_DATE_11"));
				edOutWk = concatStrWk(edOutWk, rs.getString("ENFORCEMENT_DATE_12"));
				edOutWk = concatStrWk(edOutWk, rs.getString("ENFORCEMENT_DATE_13"));
				edOutWk = concatStrWk(edOutWk, rs.getString("ENFORCEMENT_DATE_14"));
				edOutWk = concatStrWk(edOutWk, rs.getString("ENFORCEMENT_DATE_15"));
				edOutWk = concatStrWk(edOutWk, rs.getString("ENFORCEMENT_DATE_16"));
				edOutWk = concatStrWk(edOutWk, rs.getString("ENFORCEMENT_DATE_17"));
				edOutWk = concatStrWk(edOutWk, rs.getString("ENFORCEMENT_DATE_18"));
				edOutWk = concatStrWk(edOutWk, rs.getString("ENFORCEMENT_DATE_19"));
				edOutWk = concatStrWk(edOutWk, rs.getString("ENFORCEMENT_DATE_20"));

				final String Enforcement_Date = edOutWk;

				String schKindStr = "";
				schKindStr = concatStrWk(schKindStr, rs.getString("SCHKIND_NM1"));
				schKindStr = concatStrWk(schKindStr, rs.getString("SCHKIND_NM2"));
				schKindStr = concatStrWk(schKindStr, rs.getString("SCHKIND_NM3"));
				schKindStr = concatStrWk(schKindStr, rs.getString("SCHKIND_NM4"));
				schKindStr = concatStrWk(schKindStr, rs.getString("SCHKIND_NM5"));
				final String Schkind_Nm = schKindStr;

				final String Sasiro_Cnt = rs.getString("SASIRO_CNT");
				final String Mikami_Cnt = rs.getString("MIKAMI_CNT");
				final String Tousyou_Cnt = rs.getString("TOUSYOU_CNT");
				final String Kinenisi_Cnt = rs.getString("KINENISI_CNT");
				final String Fujitu_Cnt = rs.getString("FUJITU_CNT");
				final String J_Prefecture_Cnt = rs.getString("J_PREFECTURE_CNT");
				final String H_Prefecture_Normal_Cnt = rs.getString("H_PREFECTURE_NORMAL_CNT");
				final String H_Prefecture_Special_Cnt = rs.getString("H_PREFECTURE_SPECIAL_CNT");
				final String College_Cnt = rs.getString("COLLEGE_CNT");
				final String Other = rs.getString("OTHER");
				final String Total_Cnt = rs.getString("TOTAL_CNT");
				PrintData addwk = new PrintData(Training_Type_Name, Display_Order, Term, Training_Id, Training_No, Training_Name, Enforcement_Days, Capacity, Enforcement_Date, Schkind_Nm, Sasiro_Cnt, Mikami_Cnt, Tousyou_Cnt, Kinenisi_Cnt, Fujitu_Cnt, J_Prefecture_Cnt, H_Prefecture_Normal_Cnt, H_Prefecture_Special_Cnt, College_Cnt, Other, Total_Cnt);
				retList.add(addwk);
			}
		} catch (SQLException e) {
			log.debug("getCountData Err.");
		} finally {
		}

		return retList;
	}

	private String concatStrWk(final String edOutWk, final String setStr) {
		String retStr = "";
		final String edStr = StringUtils.defaultString(setStr, "");
		retStr = edOutWk + (!"".equals(edOutWk) && !"".equals(edStr) ? "、" : "") + ("".equals(edStr) ? "" : edStr);

		return retStr;
	}

	private String getCountDataSql() {
		StringBuffer stb = new StringBuffer();
		stb.append(" WITH LECTURE_VIEW_TBL AS (SELECT ");
		stb.append("     TV1.YEAR, ");
		stb.append("     TV1.TRAINING_TYPE_CODE, ");
		stb.append("     TV1.TRAINING_ID, ");
		stb.append("     MAX(CASE WHEN TV1.SCHOOL_TYPE = '01' THEN TV2.ITEM1 ELSE NULL END) AS SCHKIND_NM1, ");
		stb.append("     MAX(CASE WHEN TV1.SCHOOL_TYPE = '02' THEN TV2.ITEM1 ELSE NULL END) AS SCHKIND_NM2, ");
		stb.append("     MAX(CASE WHEN TV1.SCHOOL_TYPE = '03' THEN TV2.ITEM1 ELSE NULL END) AS SCHKIND_NM3, ");
		stb.append("     MAX(CASE WHEN TV1.SCHOOL_TYPE = '04' THEN TV2.ITEM1 ELSE NULL END) AS SCHKIND_NM4, ");
		stb.append("     MAX(CASE WHEN TV1.SCHOOL_TYPE = '05' THEN TV2.ITEM1 ELSE NULL END) AS SCHKIND_NM5 ");
		stb.append(" FROM ");
		stb.append("     SAF_LECTURE_SCHOOL_TYPE TV1 ");
		stb.append("     LEFT JOIN SAF_CODE_MASTER TV2 ");
		stb.append("       ON TV2.CODE_ID = '0002' ");
		stb.append("      AND TV2.CODE = TV1.SCHOOL_TYPE ");
		stb.append(" WHERE ");
		stb.append("     TV1.YEAR = '" + _year + "' ");
		stb.append(" GROUP BY ");
		stb.append("     TV1.YEAR, ");
		stb.append("     TV1.TRAINING_TYPE_CODE,TV1.TRAINING_ID ");
		stb.append(" ), COLLECT_DAT_WKTBL AS ( ");
		stb.append(" SELECT ");
		stb.append("   T2.TRAINING_TYPE_NAME, ");
		stb.append("   T5.DISPLAY_ORDER, ");
		stb.append("   T1.TERM, ");
		stb.append("   T1.TRAINING_ID, ");
		stb.append("   T1.TRAINING_NO, ");
		stb.append("   T1.TRAINING_NAME, ");
		stb.append("   T1.ENFORCEMENT_DAYS, ");
		stb.append("   T1.CAPACITY, ");
		stb.append("   T1.ENFORCEMENT_DATE_1, ");
		stb.append("   T1.ENFORCEMENT_DATE_2, ");
		stb.append("   T1.ENFORCEMENT_DATE_3, ");
		stb.append("   T1.ENFORCEMENT_DATE_4, ");
		stb.append("   T1.ENFORCEMENT_DATE_5, ");
		stb.append("   T1.ENFORCEMENT_DATE_6, ");
		stb.append("   T1.ENFORCEMENT_DATE_7, ");
		stb.append("   T1.ENFORCEMENT_DATE_8, ");
		stb.append("   T1.ENFORCEMENT_DATE_9, ");
		stb.append("   T1.ENFORCEMENT_DATE_10, ");
		stb.append("   T1.ENFORCEMENT_DATE_11, ");
		stb.append("   T1.ENFORCEMENT_DATE_12, ");
		stb.append("   T1.ENFORCEMENT_DATE_13, ");
		stb.append("   T1.ENFORCEMENT_DATE_14, ");
		stb.append("   T1.ENFORCEMENT_DATE_15, ");
		stb.append("   T1.ENFORCEMENT_DATE_16, ");
		stb.append("   T1.ENFORCEMENT_DATE_17, ");
		stb.append("   T1.ENFORCEMENT_DATE_18, ");
		stb.append("   T1.ENFORCEMENT_DATE_19, ");
		stb.append("   T1.ENFORCEMENT_DATE_20, ");
		stb.append("   T6.SCHKIND_NM1, ");
		stb.append("   T6.SCHKIND_NM2, ");
		stb.append("   T6.SCHKIND_NM3, ");
		stb.append("   T6.SCHKIND_NM4, ");
		stb.append("   T6.SCHKIND_NM5, ");
		stb.append("   COUNT(CASE WHEN T4.ESTABLISHED_SEGMENTS IN ('2','3') AND T4.OLD_DISTRICT = '01' THEN 1 ELSE 0 END) AS SASIRO_CNT, ");
		stb.append("   COUNT(CASE WHEN T4.ESTABLISHED_SEGMENTS IN ('2','3') AND T4.OLD_DISTRICT = '02' THEN 1 ELSE 0 END) AS MIKAMI_CNT, ");
		stb.append("   COUNT(CASE WHEN T4.ESTABLISHED_SEGMENTS IN ('2','3') AND T4.OLD_DISTRICT = '03' THEN 1 ELSE 0 END) AS TOUSYOU_CNT, ");
		stb.append("   COUNT(CASE WHEN T4.ESTABLISHED_SEGMENTS IN ('2','3') AND T4.OLD_DISTRICT = '04' THEN 1 ELSE 0 END) AS KINENISI_CNT, ");
		stb.append("   COUNT(CASE WHEN T4.ESTABLISHED_SEGMENTS IN ('2','3') AND T4.OLD_DISTRICT = '05' THEN 1 ELSE 0 END) AS FUJITU_CNT, ");
		stb.append("   COUNT(CASE WHEN T4.ESTABLISHED_SEGMENTS = '1' AND T4.OLD_DISTRICT = '02' THEN 1 ELSE 0 END) AS J_PREFECTURE_CNT, ");
		stb.append("   COUNT(CASE WHEN T4.ESTABLISHED_SEGMENTS = '1' AND T4.OLD_DISTRICT = '03' THEN 1 ELSE 0 END) AS H_PREFECTURE_NORMAL_CNT, ");
		stb.append("   COUNT(CASE WHEN T4.ESTABLISHED_SEGMENTS = '1' AND T4.OLD_DISTRICT = '04' THEN 1 ELSE 0 END) AS H_PREFECTURE_SPECIAL_CNT, ");
		stb.append("   COUNT(CASE WHEN T4.OLD_DISTRICT = '10' THEN 1 ELSE 0 END) AS COLLEGE_CNT, ");
		stb.append("   COUNT(CASE WHEN T3.BELONG_SCHOOLID = '0' OR T4.OLD_DISTRICT NOT IN ('01','02','03','04','10')  THEN 1 ELSE 0 END) AS OTHER ");
		stb.append(" FROM ");
		stb.append("   SAF_TRAINING_INFOMATION T1 ");
		stb.append("   LEFT JOIN SAF_TRAINING_TYPE T2 ");
		stb.append("     ON T2.YEAR = T1.YEAR ");
		stb.append("    AND T2.TRAINING_TYPE_CODE = T1.TRAINING_TYPE_CODE ");
		stb.append("   FULL OUTER JOIN SAF_TRAINING_APPLICATION T3 ");
		stb.append("     ON T3.YEAR = T1.YEAR ");
		stb.append("    AND T3.TRAINING_TYPE_CODE = T1.TRAINING_TYPE_CODE ");
		stb.append("    AND T3.TRAINING_ID = T1.TRAINING_ID ");
		if (OUT_TYPE_1.equals(_outputType)) {
			stb.append("    AND EXISTS ( SELECT 'X'  ");
			stb.append("                  FROM SAF_ATTENDANCE_CERTIFICATION C2 ");
			stb.append("                  WHERE ");
			stb.append("                    C2.YEAR = T3.YEAR ");
			stb.append("                    AND C2.TRAINING_TYPE_CODE = T3.TRAINING_TYPE_CODE ");
			stb.append("                    AND C2.TRAINING_ID = T3.TRAINING_ID ");
			stb.append("                    AND C2.ID = T3.ID ");
			stb.append("                    AND (C2.date_1_attendance = '1' OR C2.date_2_attendance = '1' OR C2.date_3_attendance = '1' OR C2.date_4_attendance = '1' OR C2.date_5_attendance = '1' OR C2.date_6_attendance = '1' OR C2.date_7_attendance = '1' OR C2.date_8_attendance = '1' OR C2.date_9_attendance = '1' OR C2.date_10_attendance = '1' OR C2.date_11_attendance = '1' OR C2.date_12_attendance = '1' OR C2.date_13_attendance = '1' OR C2.date_14_attendance = '1' OR C2.date_15_attendance = '1' OR C2.date_16_attendance = '1' OR C2.date_17_attendance = '1' OR C2.date_18_attendance = '1' OR C2.date_19_attendance = '1' OR C2.date_20_attendance = '1') ");
			stb.append("                ) ");
		}
		stb.append("   FULL OUTER JOIN ( " + getSchoolViewSql() + " ) T4 ");
		stb.append("     ON T4.SCHOOLID = T3.BELONG_SCHOOLID ");
		stb.append("   LEFT JOIN SAF_CODE_MASTER T5 ");
		stb.append("     ON T5.CODE_ID = '0106' ");
		stb.append("    AND T5.CODE = T2.TRAINING_TYPE_CODE ");
		stb.append("   LEFT JOIN LECTURE_VIEW_TBL T6 ");
		stb.append("     ON T6.YEAR = T1.YEAR ");
		stb.append("    AND T6.TRAINING_TYPE_CODE = T1.TRAINING_TYPE_CODE ");
		stb.append("    AND T6.TRAINING_ID = T1.TRAINING_ID ");
		stb.append(" WHERE ");
		stb.append("   T1.YEAR = '" + _year + "' ");
		stb.append(" GROUP BY ");
		stb.append("   T2.TRAINING_TYPE_NAME,T5.DISPLAY_ORDER,T1.TERM,T1.TRAINING_ID,T1.TRAINING_NO,T1.TRAINING_NAME, ");
		stb.append("   T1.ENFORCEMENT_DAYS,T1.CAPACITY, ");
		stb.append("   T1.enforcement_date_1,T1.enforcement_date_2,T1.enforcement_date_3,T1.enforcement_date_4, ");
		stb.append("   T1.enforcement_date_5,T1.enforcement_date_6,T1.enforcement_date_7,T1.enforcement_date_8, ");
		stb.append("   T1.enforcement_date_9,T1.enforcement_date_10,T1.enforcement_date_11,T1.enforcement_date_12, ");
		stb.append("   T1.enforcement_date_13,T1.enforcement_date_14,T1.enforcement_date_15,T1.enforcement_date_16, ");
		stb.append("   T1.enforcement_date_17,T1.enforcement_date_18,T1.enforcement_date_19,T1.enforcement_date_20, ");
		stb.append("   T6.SCHKIND_NM1,T6.SCHKIND_NM2,T6.SCHKIND_NM3,T6.SCHKIND_NM4,T6.SCHKIND_NM5 ");
		stb.append(" ) ");
		stb.append(" SELECT ");
		stb.append("   T1.*, ");
		stb.append("   (T1.SASIRO_CNT + T1.MIKAMI_CNT + T1.TOUSYOU_CNT + T1.KINENISI_CNT + T1.FUJITU_CNT + T1.J_PREFECTURE_CNT ");
		stb.append("    + T1.H_PREFECTURE_NORMAL_CNT + T1.H_PREFECTURE_SPECIAL_CNT + T1.COLLEGE_CNT + T1.OTHER) AS TOTAL_CNT ");
		stb.append(" FROM ");
		stb.append("   COLLECT_DAT_WKTBL T1 ");
		stb.append(" ORDER BY ");
		stb.append("   T1.DISPLAY_ORDER,T1.TERM,T1.TRAINING_NO ");
		return stb.toString();
	}

    private String getSchoolViewSql() {
        final StringBuffer stb = new StringBuffer();

        //学校VIEW
        stb.append(" SELECT ");
        stb.append("   A.*  ");
        stb.append(" FROM  ");
        stb.append("   V_SCHOOL_BASIS A ");
        stb.append("   INNER JOIN (SELECT SCHOOLID, MAX(START_DATE) AS START_DATE ");
        stb.append("                FROM V_SCHOOL_BASIS ");
        stb.append("               WHERE START_DATE <= '" + _year  + "-03-31' ");
        stb.append("                 AND END_DATE   >= '" + _year  + "-04-01' ");
        stb.append("            GROUP BY SCHOOLID ");
        stb.append("             ) B  ");
        stb.append("          ON B.SCHOOLID = A.SCHOOLID ");
        stb.append("         AND B.START_DATE = A.START_DATE, ");
        stb.append("   SAF_CODE_MASTER C ");
        stb.append(" WHERE ");
        stb.append("   A.SCHOOLID NOT IN C.CODE ");
        stb.append("   AND C.CODE_ID = '0102' ");

        return stb.toString();
    }

	private class PrintData {
	    final String _Training_Type_Name;
	    final String _Display_Order;
	    final String _Term;
	    final String _Training_Id;
	    final String _Training_No;
	    final String _Training_Name;
	    final String _Enforcement_Days;
	    final String _Capacity;
	    final String _Enforcement_Date;
	    final String _Schkind_Nm;
	    final String _Sasiro_Cnt;
	    final String _Mikami_Cnt;
	    final String _Tousyou_Cnt;
	    final String _Kinenisi_Cnt;
	    final String _Fujitu_Cnt;
	    final String _J_Prefecture_Cnt;
	    final String _H_Prefecture_Normal_Cnt;
	    final String _H_Prefecture_Special_Cnt;
	    final String _College_Cnt;
	    final String _Other;
	    final String _Total_Cnt;
	    public PrintData (final String Training_Type_Name, final String Display_Order, final String Term, final String Training_Id, final String Training_No, final String Training_Name, final String Enforcement_Days, final String Capacity, final String Enforcement_Date, final String Schkind_Nm, final String Sasiro_Cnt, final String Mikami_Cnt, final String Tousyou_Cnt, final String Kinenisi_Cnt, final String Fujitu_Cnt, final String J_Prefecture_Cnt, final String H_Prefecture_Normal_Cnt, final String H_Prefecture_Special_Cnt, final String College_Cnt, final String Other, final String Total_Cnt)
	    {
	        _Training_Type_Name = Training_Type_Name;
	        _Display_Order = Display_Order;
	        _Term = Term;
	        _Training_Id = Training_Id;
	        _Training_No = Training_No;
	        _Training_Name = Training_Name;
	        _Enforcement_Days = Enforcement_Days;
	        _Capacity = Capacity;
	        _Enforcement_Date = Enforcement_Date;
	        _Schkind_Nm = Schkind_Nm;
	        _Sasiro_Cnt = Sasiro_Cnt;
	        _Mikami_Cnt = Mikami_Cnt;
	        _Tousyou_Cnt = Tousyou_Cnt;
	        _Kinenisi_Cnt = Kinenisi_Cnt;
	        _Fujitu_Cnt = Fujitu_Cnt;
	        _J_Prefecture_Cnt = J_Prefecture_Cnt;
	        _H_Prefecture_Normal_Cnt = H_Prefecture_Normal_Cnt;
	        _H_Prefecture_Special_Cnt = H_Prefecture_Special_Cnt;
	        _College_Cnt = College_Cnt;
	        _Other = Other;
	        _Total_Cnt = Total_Cnt;
	    }
	}

	private String getOtherInfo(final util.DaoUtil db2, final String training_Id) {
		String retStr = "";
		ResultSet rs = null;
		String delimWk = "";

		final String sql = getOtherInfoSql(training_Id);

		try {
			rs = db2.query(sql);
			errorMessage = db2.getErrTrace();
			if (errorMessage.length() > 0) { // sql例外発生
				return retStr;
			}
			while(rs.next()) {
				final String SchoolNameShort = rs.getString("SCHOOLNAME_SHORT");
				final String IdCnt = rs.getString("ID_CNT");
				retStr += delimWk + SchoolNameShort + ":" + IdCnt;
				delimWk = "、";
			}
		} catch (SQLException e) {
			log.debug("getOtherInfo Err.");
		}
		return retStr;
	}

	private String getOtherInfoSql(final String training_Id) {
		StringBuffer stb = new StringBuffer();
		stb.append(" SELECT ");
		stb.append("     TW1.TRAINING_ID, ");
		stb.append("     TW3.SCHOOLNAME_SHORT, ");
		stb.append("     COUNT(TW2.ID) AS ID_CNT ");
		stb.append(" FROM ");
		stb.append("     SAF_TRAINING_INFOMATION TW1 ");
		stb.append("     LEFT JOIN  SAF_TRAINING_APPLICATION TW2 ");
		stb.append("             ON TW2.YEAR = TW1.YEAR ");
		stb.append("             AND TW2.TRAINING_TYPE_CODE = TW1.TRAINING_TYPE_CODE ");
		stb.append("             AND TW2.TRAINING_ID = TW1.TRAINING_ID ");
		stb.append("     FULL OUTER JOIN ( " + getSchoolViewSql() + " ) TW3 ");
		stb.append("             ON TW2.BELONG_SCHOOLID = TW3.SCHOOLID ");
		stb.append(" WHERE ");
		stb.append("     TW1.YEAR = '" + _year + "' ");
		stb.append("     AND ( ");
		stb.append("          TW2.BELONG_SCHOOLID = '0' ");
		stb.append("          OR ");
		stb.append("          TW3.SCHOOL_TYPE NOT IN ('01','02','03','04','10') ");
		stb.append("         ) ");
		stb.append("     AND TW1.TRAINING_ID = '" + training_Id + "' ");
		stb.append(" GROUP BY ");
		stb.append("     TW1.TRAINING_ID, ");
		stb.append("     TW3.SCHOOLNAME_SHORT ");
		stb.append(" ORDER BY ");
		stb.append("     TW1.TRAINING_ID ");
		return stb.toString();
	}
}
