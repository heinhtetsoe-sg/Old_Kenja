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
public class Tr_Sagcr_07_2 {
	Log log = LogFactory.getLog(Tr_Sagcr_07_2.class);
	// メンバー変数
	private Workbook book;
	private final static String USE_SHEETNAME = "校種・事務所別の申込者数";
	private Alp_Properties Prop;
	private String errorMessage = "";
	final static int MAX_LINE = 20;
	// 書込みデータ
	String _year = "";
	List<CourseAttendListLine> lines = new ArrayList<>();

	public Tr_Sagcr_07_2(Workbook book, String information, Alp_Properties prop) {
		this.book = book;
		String[] Information = information.split(",");
		if (Information.length < 1) {
			errorMessage = String.format("パラメータエラー{%s}", information);
		} else {
			_year = Information[0];  //yearは上位からパラメータでわたってくる。
		}
		this.Prop = prop;
        log.warn("$Revision: 67777 $");
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
		Sheet sheet = book.getSheet(USE_SHEETNAME);
		if (sheet == null) {
			errorMessage = "対象のシート見つからず";
			return retFlg;
		}
		SetColumn set = new SetColumn();
		set.setSheet(sheet);

		//"校種・事務所別の申込者数【2018年度】";
		final String strWk;
		strWk = "校種・事務所別の申込者数【" + _year + "年度】";
		set.setValueString(1, "A", strWk);

		//"出力日：";
		Date date = new Date();
		SimpleDateFormat fmt1 = new SimpleDateFormat("yyyy/MM/dd");
		String dstrWk = "出力日：" + fmt1.format(date);
		set.setValueString(1, "X", dstrWk);

		int dataidx = 0;
		int strtrow = 3;  //出力開始行-1
		for (Iterator<PrintData> ite = DatList.iterator();ite.hasNext();) {
			PrintData outinfo = (PrintData)ite.next();
			row = sheet.getRow(strtrow + dataidx);
			int pos = 0; //出力開始位置(次行処理でリセット)
			int maxlengthWk = 0;

			setData(row.getCell(pos++), outinfo._Training_Type_Name); //研修種別
            maxlengthWk = setMaxLengthChk(maxlengthWk,getCellRows(outinfo._Training_Type_Name, 19));
			setData(row.getCell(pos++), outinfo._Training_No);        //研修番号
			setData(row.getCell(pos++), outinfo._Training_Name);      //研修名
            maxlengthWk = setMaxLengthChk(maxlengthWk,getCellRows(outinfo._Training_Name, 14));

			setData(row.getCell(pos++), outinfo._P_Sasiro_Cnt);   //小学校 旧佐城
			setData(row.getCell(pos++), outinfo._P_Mikami_Cnt);   //小学校 旧三神
			setData(row.getCell(pos++), outinfo._P_Toushou_Cnt);  //小学校 旧東松浦
			setData(row.getCell(pos++), outinfo._P_Kinenisi_Cnt); //小学校 旧杵西
			setData(row.getCell(pos++), outinfo._P_Fujitu_Cnt);   //小学校 旧藤津
			setData(row.getCell(pos++), outinfo._P_Local_Cnt);    //小学校 付属

			setData(row.getCell(pos++), outinfo._J_Sasiro_Cnt);   //中学校 旧佐城
			setData(row.getCell(pos++), outinfo._J_Mikami_Cnt);   //中学校 旧三神
			setData(row.getCell(pos++), outinfo._J_Toushou_Cnt);  //中学校 旧東松浦
			setData(row.getCell(pos++), outinfo._J_Kinenisi_Cnt); //中学校 旧杵西
			setData(row.getCell(pos++), outinfo._J_Fujitu_Cnt);   //中学校 旧藤津
			setData(row.getCell(pos++), outinfo._J_Local_Cnt);    //中学校 付属

			setData(row.getCell(pos++), outinfo._J_Prefecture_Cnt);               //県立 中学
			setData(row.getCell(pos++), outinfo._H_Prefecture_Normal_Cnt);        //県立 高校 普通
			pos++; //県立 高校 専攻は空列。
			setData(row.getCell(pos++), outinfo._H_Prefecture_Special_Cnt);       //特別支援学校
			setData(row.getCell(pos++), outinfo._H_Prefecture_Dormitory_Adv_Cnt); //寄宿舎指導員

			setData(row.getCell(pos++), outinfo._H_Local_Special_Cnt); //附属特別支援学校
			setData(row.getCell(pos++), outinfo._Other_Cnt);           //その他
            maxlengthWk = setMaxLengthChk(maxlengthWk,getCellRows(outinfo._Training_Name, 24));

			final String OtherInfo = getOtherInfo(db2, outinfo._Training_Id);
			if (!"".equals(OtherInfo)) {
                setData(row.getCell(pos), OtherInfo); //その他内訳
			}
			pos++; //※if文により出力しなくても、出力位置をずらす必要がある。

			setData(row.getCell(pos++), outinfo._Total_Cnt); //合計

			row.setHeightInPoints((float)15.0 * (float)maxlengthWk);
		    dataidx++;
		    retFlg = true;
		}
		if (retFlg) {
			//印刷範囲設定。
			//引数は、シートindex,開始列index,終了列index,開始行index,終了行index
			book.setPrintArea(0, 0, 23, 0, dataidx + strtrow - 1);
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
		try {
			rs = db2.query(sql);
			errorMessage = db2.getErrTrace();
			if (errorMessage.length() > 0) { // sql例外発生
				return retList;
			}
			while(rs.next()) {
				final String Training_Type_Name = rs.getString("TRAINING_TYPE_NAME");
				final String Display_Order = rs.getString("DISPLAY_ORDER");
				final String Training_Id = rs.getString("TRAINING_ID");
				final String Training_No = rs.getString("TRAINING_NO");
				final String Training_Name = rs.getString("TRAINING_NAME");
				final String P_Sasiro_Cnt = rs.getString("P_SASIRO_CNT");
				final String P_Mikami_Cnt = rs.getString("P_MIKAMI_CNT");
				final String P_Toushou_Cnt = rs.getString("P_TOUSHOU_CNT");
				final String P_Kinenisi_Cnt = rs.getString("P_KINENISI_CNT");
				final String P_Fujitu_Cnt = rs.getString("P_FUJITU_CNT");
				final String P_Local_Cnt = rs.getString("P_LOCAL_CNT");
				final String J_Sasiro_Cnt = rs.getString("J_SASIRO_CNT");
				final String J_Mikami_Cnt = rs.getString("J_MIKAMI_CNT");
				final String J_Toushou_Cnt = rs.getString("J_TOUSHOU_CNT");
				final String J_Kinenisi_Cnt = rs.getString("J_KINENISI_CNT");
				final String J_Fujitu_Cnt = rs.getString("J_FUJITU_CNT");
				final String J_Local_Cnt = rs.getString("J_LOCAL_CNT");
				final String J_Prefecture_Cnt = rs.getString("J_PREFECTURE_CNT");
				final String H_Prefecture_Normal_Cnt = rs.getString("H_PREFECTURE_NORMAL_CNT");
				final String H_Prefecture_Special_Cnt = rs.getString("H_PREFECTURE_SPECIAL_CNT");
				final String H_Prefecture_Dormitory_Adv_Cnt = rs.getString("H_PREFECTURE_DORMITORY_ADV_CNT");
				final String H_Local_Special_Cnt = rs.getString("H_LOCAL_SPECIAL_CNT");
				final String Other_Cnt = rs.getString("OTHER_CNT");
				final String Total_Cnt = rs.getString("TOTAL_CNT");
				PrintData addwk = new PrintData(Training_Type_Name, Display_Order, Training_Id, Training_No, Training_Name, P_Sasiro_Cnt, P_Mikami_Cnt, P_Toushou_Cnt, P_Kinenisi_Cnt, P_Fujitu_Cnt, P_Local_Cnt, J_Sasiro_Cnt, J_Mikami_Cnt, J_Toushou_Cnt, J_Kinenisi_Cnt, J_Fujitu_Cnt, J_Local_Cnt, J_Prefecture_Cnt, H_Prefecture_Normal_Cnt, H_Prefecture_Special_Cnt, H_Prefecture_Dormitory_Adv_Cnt, H_Local_Special_Cnt, Other_Cnt, Total_Cnt);
				retList.add(addwk);
			}
		} catch (SQLException e) {
			log.debug("getCountData Err.");
		} finally {
		}

		return retList;
	}

	private String getCountDataSql() {
		StringBuffer stb = new StringBuffer();
		stb.append(" WITH COLLECT_DAT_WKTBL AS (");
		stb.append(" SELECT ");
		stb.append("   T2.TRAINING_TYPE_NAME, ");
		stb.append("   T5.DISPLAY_ORDER, ");
		stb.append("   T1.TRAINING_ID, ");
		stb.append("   T1.TRAINING_NO, ");
		stb.append("   T1.TRAINING_NAME, ");
		stb.append("   COUNT(CASE WHEN T4.ESTABLISHED_SEGMENTS IN ('2','3') AND T4.SCHOOL_TYPE = '01' AND T4.OLD_DISTRICT = '01' THEN 1 ELSE 0 END) AS P_SASIRO_CNT, ");
		stb.append("   COUNT(CASE WHEN T4.ESTABLISHED_SEGMENTS IN ('2','3') AND T4.SCHOOL_TYPE = '01' AND T4.OLD_DISTRICT = '02' THEN 1 ELSE 0 END) AS P_MIKAMI_CNT, ");
		stb.append("   COUNT(CASE WHEN T4.ESTABLISHED_SEGMENTS IN ('2','3') AND T4.SCHOOL_TYPE = '01' AND T4.OLD_DISTRICT = '03' THEN 1 ELSE 0 END) AS P_TOUSHOU_CNT, ");
		stb.append("   COUNT(CASE WHEN T4.ESTABLISHED_SEGMENTS IN ('2','3') AND T4.SCHOOL_TYPE = '01' AND T4.OLD_DISTRICT = '04' THEN 1 ELSE 0 END) AS P_KINENISI_CNT, ");
		stb.append("   COUNT(CASE WHEN T4.ESTABLISHED_SEGMENTS IN ('2','3') AND T4.SCHOOL_TYPE = '01' AND T4.OLD_DISTRICT = '05' THEN 1 ELSE 0 END) AS P_FUJITU_CNT, ");
		stb.append("   COUNT(CASE WHEN T4.SCHOOLID IN (SELECT ITEM1 FROM SAF_CODE_MASTER WHERE CODE_ID = '0107' AND ITEM3 = '01') THEN 1 ELSE 0 END) AS P_LOCAL_CNT, ");
		stb.append("   COUNT(CASE WHEN T4.ESTABLISHED_SEGMENTS IN ('2','3') AND T4.SCHOOL_TYPE = '02' AND T4.OLD_DISTRICT = '01' THEN 1 ELSE 0 END) AS J_SASIRO_CNT, ");
		stb.append("   COUNT(CASE WHEN T4.ESTABLISHED_SEGMENTS IN ('2','3') AND T4.SCHOOL_TYPE = '02' AND T4.OLD_DISTRICT = '02' THEN 1 ELSE 0 END) AS J_MIKAMI_CNT, ");
		stb.append("   COUNT(CASE WHEN T4.ESTABLISHED_SEGMENTS IN ('2','3') AND T4.SCHOOL_TYPE = '02' AND T4.OLD_DISTRICT = '03' THEN 1 ELSE 0 END) AS J_TOUSHOU_CNT, ");
		stb.append("   COUNT(CASE WHEN T4.ESTABLISHED_SEGMENTS IN ('2','3') AND T4.SCHOOL_TYPE = '02' AND T4.OLD_DISTRICT = '04' THEN 1 ELSE 0 END) AS J_KINENISI_CNT, ");
		stb.append("   COUNT(CASE WHEN T4.ESTABLISHED_SEGMENTS IN ('2','3') AND T4.SCHOOL_TYPE = '02' AND T4.OLD_DISTRICT = '05' THEN 1 ELSE 0 END) AS J_FUJITU_CNT, ");
		stb.append("   COUNT(CASE WHEN T4.SCHOOLID IN (SELECT ITEM1 FROM SAF_CODE_MASTER WHERE CODE_ID = '0107' AND ITEM3 = '02') THEN 1 ELSE 0 END) AS J_LOCAL_CNT, ");
		stb.append("   COUNT(CASE WHEN T4.ESTABLISHED_SEGMENTS = '1' AND T4.SCHOOL_TYPE = '02' THEN 1 ELSE 0 END) AS J_PREFECTURE_CNT, ");
		stb.append("   COUNT(CASE WHEN T4.ESTABLISHED_SEGMENTS = '1' AND T4.SCHOOL_TYPE = '03' THEN 1 ELSE 0 END) AS H_PREFECTURE_NORMAL_CNT, ");
		stb.append("   COUNT(CASE WHEN T4.ESTABLISHED_SEGMENTS = '1' AND T4.SCHOOL_TYPE = '04' AND T3.RANK_CODE NOT IN (SELECT ITEM1 FROM SAF_CODE_MASTER WHERE CODE_ID = '0111') THEN 1 ELSE 0 END) AS H_PREFECTURE_SPECIAL_CNT, ");
		stb.append("   COUNT(CASE WHEN T4.ESTABLISHED_SEGMENTS = '1' AND T3.RANK_CODE IN (SELECT ITEM1 FROM SAF_CODE_MASTER WHERE CODE_ID = '0111') THEN 1 ELSE 0 END) AS H_PREFECTURE_DORMITORY_ADV_CNT, ");
		stb.append("   COUNT(CASE WHEN T4.SCHOOLID IN (SELECT ITEM1 FROM SAF_CODE_MASTER WHERE CODE_ID = '0107' AND ITEM3 = '04') THEN 1 ELSE 0 END) AS H_LOCAL_SPECIAL_CNT, ");
		stb.append("   COUNT(CASE WHEN T3.BELONG_SCHOOLID = '0' OR T4.SCHOOL_TYPE IN ('05','06','07','08','09','11') OR T4.SCHOOLID IN (SELECT ITEM1 FROM SAF_CODE_MASTER WHERE CODE_ID = '0107' AND ITEM3 = '05') THEN 1 ELSE 0 END) AS OTHER_CNT ");
		stb.append(" FROM ");
		stb.append("   SAF_TRAINING_INFOMATION T1 ");
		stb.append("   LEFT JOIN SAF_TRAINING_TYPE T2 ");
		stb.append("     ON T2.YEAR = T1.YEAR ");
		stb.append("     AND T2.TRAINING_TYPE_CODE = T1.TRAINING_TYPE_CODE ");
		stb.append("   FULL OUTER JOIN SAF_TRAINING_APPLICATION T3 ");
		stb.append("     ON T3.YEAR = T1.YEAR ");
		stb.append("     AND T3.TRAINING_TYPE_CODE = T1.TRAINING_TYPE_CODE ");
		stb.append("     AND T3.TRAINING_ID = T1.TRAINING_ID ");
		stb.append("   FULL OUTER JOIN ( " + getSchoolViewSql() + " ) T4 ");
		stb.append("     ON T4.SCHOOLID = T3.BELONG_SCHOOLID ");
		stb.append("   LEFT JOIN SAF_CODE_MASTER T5 ");
		stb.append("     ON T5.CODE_ID = '0106' ");
		stb.append("     AND T5.CODE = T2.TRAINING_TYPE_CODE ");
		stb.append(" WHERE ");
		stb.append("   T1.YEAR = '" + _year +"' ");
		stb.append(" GROUP BY ");
		stb.append("   T2.TRAINING_TYPE_NAME, ");
		stb.append("   T5.DISPLAY_ORDER, ");
		stb.append("   T1.TRAINING_ID, ");
		stb.append("   T1.TRAINING_NO, ");
		stb.append("   T1.TRAINING_NAME ");
		stb.append(" ) ");
		stb.append(" SELECT ");
		stb.append("   T1.*,");
		stb.append("   (P_SASIRO_CNT + P_MIKAMI_CNT + P_TOUSHOU_CNT + P_KINENISI_CNT + P_FUJITU_CNT + P_LOCAL_CNT ");
		stb.append("   + J_SASIRO_CNT + J_MIKAMI_CNT + J_TOUSHOU_CNT + J_KINENISI_CNT + J_FUJITU_CNT + J_LOCAL_CNT ");
		stb.append("   + J_PREFECTURE_CNT + H_PREFECTURE_NORMAL_CNT + H_PREFECTURE_SPECIAL_CNT + H_PREFECTURE_DORMITORY_ADV_CNT + H_LOCAL_SPECIAL_CNT + OTHER_CNT) AS TOTAL_CNT ");
		stb.append(" FROM ");
		stb.append("   COLLECT_DAT_WKTBL T1 ");
		stb.append(" ORDER BY ");
		stb.append("   T1.DISPLAY_ORDER, ");
		stb.append("   T1.TRAINING_NO ");
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
	    final String _Training_Id;
	    final String _Training_No;
	    final String _Training_Name;
	    final String _P_Sasiro_Cnt;
	    final String _P_Mikami_Cnt;
	    final String _P_Toushou_Cnt;
	    final String _P_Kinenisi_Cnt;
	    final String _P_Fujitu_Cnt;
	    final String _P_Local_Cnt;
	    final String _J_Sasiro_Cnt;
	    final String _J_Mikami_Cnt;
	    final String _J_Toushou_Cnt;
	    final String _J_Kinenisi_Cnt;
	    final String _J_Fujitu_Cnt;
	    final String _J_Local_Cnt;
	    final String _J_Prefecture_Cnt;
	    final String _H_Prefecture_Normal_Cnt;
	    final String _H_Prefecture_Special_Cnt;
	    final String _H_Prefecture_Dormitory_Adv_Cnt;
	    final String _H_Local_Special_Cnt;
	    final String _Other_Cnt;
	    final String _Total_Cnt;
	    public PrintData (final String Training_Type_Name, final String Display_Order, final String Training_Id, final String Training_No, final String Training_Name, final String P_Sasiro_Cnt, final String P_Mikami_Cnt, final String P_Toushou_Cnt, final String P_Kinenisi_Cnt, final String P_Fujitu_Cnt, final String P_Local_Cnt, final String J_Sasiro_Cnt, final String J_Mikami_Cnt, final String J_Toushou_Cnt, final String J_Kinenisi_Cnt, final String J_Fujitu_Cnt, final String J_Local_Cnt, final String J_Prefecture_Cnt, final String H_Prefecture_Normal_Cnt, final String H_Prefecture_Special_Cnt, final String H_Prefecture_Dormitory_Adv_Cnt, final String H_Local_Special_Cnt, final String Other_Cnt, final String Total_Cnt)
	    {
	        _Training_Type_Name = Training_Type_Name;
	        _Display_Order = Display_Order;
	        _Training_Id = Training_Id;
	        _Training_No = Training_No;
	        _Training_Name = Training_Name;
	        _P_Sasiro_Cnt = P_Sasiro_Cnt;
	        _P_Mikami_Cnt = P_Mikami_Cnt;
	        _P_Toushou_Cnt = P_Toushou_Cnt;
	        _P_Kinenisi_Cnt = P_Kinenisi_Cnt;
	        _P_Fujitu_Cnt = P_Fujitu_Cnt;
	        _P_Local_Cnt = P_Local_Cnt;
	        _J_Sasiro_Cnt = J_Sasiro_Cnt;
	        _J_Mikami_Cnt = J_Mikami_Cnt;
	        _J_Toushou_Cnt = J_Toushou_Cnt;
	        _J_Kinenisi_Cnt = J_Kinenisi_Cnt;
	        _J_Fujitu_Cnt = J_Fujitu_Cnt;
	        _J_Local_Cnt = J_Local_Cnt;
	        _J_Prefecture_Cnt = J_Prefecture_Cnt;
	        _H_Prefecture_Normal_Cnt = H_Prefecture_Normal_Cnt;
	        _H_Prefecture_Special_Cnt = H_Prefecture_Special_Cnt;
	        _H_Prefecture_Dormitory_Adv_Cnt = H_Prefecture_Dormitory_Adv_Cnt;
	        _H_Local_Special_Cnt = H_Local_Special_Cnt;
	        _Other_Cnt = Other_Cnt;
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
