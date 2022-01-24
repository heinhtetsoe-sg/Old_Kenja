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
public class Tr_Sagcr_07_6 {
	Log log = LogFactory.getLog(Tr_Sagcr_07_6.class);
	// メンバー変数
	private Workbook book;
	private final static String USE_SHEETNAME = "その他学校の申込回数";
	private Alp_Properties Prop;
	private String errorMessage = "";
	final static int MAX_LINE = 20;
	// 書込みデータ
	String _year = "";
	List<CourseAttendListLine> lines = new ArrayList<>();

	public Tr_Sagcr_07_6(Workbook book, String information, Alp_Properties prop) {
		this.book = book;
		String[] Information = information.split(",");
		if (Information.length < 2) {
			errorMessage = String.format("パラメータエラー{%s}", information);
		} else {
			_year = Information[0];  //yearは上位からパラメータでわたってくる。
		}
		this.Prop = prop;
        log.warn("$Revision: 67804 $");
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
		strWk = USE_SHEETNAME + "【" + _year + "年度】";
		set.setValueString(1, "A", strWk);

		//"出力日：";
		Date date = new Date();
		SimpleDateFormat fmt1 = new SimpleDateFormat("yyyy/MM/dd");
		String dstrWk = "出力日：" + fmt1.format(date);
		set.setValueString(1, "F", dstrWk);

		int dataidx = 0;
		int strtrow = 2;  //出力開始行-1
		for (Iterator<PrintData> ite = DatList.iterator();ite.hasNext();) {
			PrintData outinfo = (PrintData)ite.next();
			row = sheet.getRow(strtrow + dataidx);
			int pos = 0; //出力開始位置(次行処理でリセット)
			int maxlengthWk = 0;

			setData(row.getCell(pos++), outinfo._school_Type); //校種
            maxlengthWk = setMaxLengthChk(maxlengthWk,getCellRows(outinfo._school_Type, 22));
			setData(row.getCell(pos++), outinfo._schoolname_Short);        //学校名
            maxlengthWk = setMaxLengthChk(maxlengthWk,getCellRows(outinfo._schoolname_Short, 40));
			setData(row.getCell(pos++), outinfo._count);      //申込数
			setData(row.getCell(pos++), outinfo._lecture_Count);   //採用者数
			setData(row.getCell(pos++), outinfo._training_No);   //研修番号
			setData(row.getCell(pos++), outinfo._training_Name);  //研修名
            maxlengthWk = setMaxLengthChk(maxlengthWk,getCellRows(outinfo._training_Name, 80));

			row.setHeightInPoints((float)15.0 * (float)maxlengthWk);
		    dataidx++;
		    retFlg = true;
		}
		if (retFlg) {
			//印刷範囲設定。
			//引数は、シートindex,開始列index,終了列index,開始行index,終了行index
			book.setPrintArea(0, 0, 5, 0, dataidx + strtrow - 1);
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
				final String belong_Schoolid = StringUtils.defaultString(rs.getString("BELONG_SCHOOLID"), "");
				final String schoolname_Short = StringUtils.defaultString(rs.getString("SCHOOLNAME_SHORT"), "");
				final String training_No = StringUtils.defaultString(rs.getString("TRAINING_NO"), "");
				final String training_Name = StringUtils.defaultString(rs.getString("TRAINING_NAME"), "");
				final String school_Type = StringUtils.defaultString(rs.getString("SCHOOL_TYPE"), "");
				final String count = StringUtils.defaultString(rs.getString("COUNT"), "");
				final String lecture_Count = StringUtils.defaultString(rs.getString("LECTURE_COUNT"), "");
				PrintData addwk = new PrintData(belong_Schoolid, schoolname_Short, training_No, training_Name, school_Type, count, lecture_Count);
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
		stb.append(" SELECT ");
		stb.append("   A.BELONG_SCHOOLID, ");
		stb.append("   C.SCHOOLNAME_SHORT, ");
		stb.append("   B.TRAINING_NO, ");
		stb.append("   B.TRAINING_NAME, ");
		stb.append("   D.ITEM1 AS SCHOOL_TYPE, ");
		stb.append("   COUNT(A.ID) AS COUNT, ");
		stb.append("   COUNT(CASE WHEN A.ATTENDING_LECTURE_FLAG = 1 THEN 1 ELSE 0 END) AS LECTURE_COUNT ");
		stb.append(" FROM ");
		stb.append("   SAF_TRAINING_APPLICATION A ");
		stb.append("   INNER JOIN SAF_TRAINING_INFOMATION B ");
		stb.append("           ON B.YEAR               = A.YEAR ");
		stb.append("          AND B.TRAINING_TYPE_CODE = A.TRAINING_TYPE_CODE ");
		stb.append("          AND B.TRAINING_ID        = A.TRAINING_ID ");
		stb.append("   LEFT JOIN ( " + getSchoolViewSql() + " ) C ");
		stb.append("          ON C.SCHOOLID = A.BELONG_SCHOOLID ");
		stb.append("   LEFT JOIN SAF_CODE_MASTER D ");
		stb.append("          ON D.CODE_ID = C.SCHOOL_TYPE ");
		stb.append("         AND D.CODE_ID = '0002' ");
		stb.append(" WHERE ");
		stb.append("   A.YEAR = '" + _year + "' ");
		stb.append("   AND ( A.BELONG_SCHOOLID = '0'  ");
		stb.append("         OR ( A.BELONG_SCHOOLID != '0'  ");
		stb.append("              AND C.SCHOOL_TYPE NOT IN ('01','02','03','04') ");
		stb.append("         )  ");
		stb.append("   ) ");
		stb.append(" GROUP BY ");
		stb.append("   A.BELONG_SCHOOLID, ");
		stb.append("   C.SCHOOLID, ");
		stb.append("   C.SCHOOLNAME_SHORT, ");
		stb.append("   B.TRAINING_NO, ");
		stb.append("   B.TRAINING_NAME, ");
		stb.append("   D.DISPLAY_ORDER, ");
		stb.append("   D.ITEM1 ");
		stb.append(" ORDER BY ");
		stb.append("   D.DISPLAY_ORDER, ");
		stb.append("   C.SCHOOLID, ");
		stb.append("   C.SCHOOLNAME_SHORT, ");
		stb.append("   B.TRAINING_NO ");
		return stb.toString();
	}

    private String getSchoolViewSql() {
        final StringBuffer stb = new StringBuffer();

        //学校VIEW
        stb.append(" SELECT ");
        stb.append("   AA.*  ");
        stb.append(" FROM  ");
        stb.append("   V_SCHOOL_BASIS AA ");
        stb.append("   INNER JOIN (SELECT SCHOOLID, MAX(START_DATE) AS START_DATE ");
        stb.append("                FROM V_SCHOOL_BASIS ");
        stb.append("               WHERE START_DATE <= '" + _year  + "-03-31' ");
        stb.append("                 AND END_DATE   >= '" + _year  + "-04-01' ");
        stb.append("            GROUP BY SCHOOLID ");
        stb.append("             ) AB  ");
        stb.append("          ON AB.SCHOOLID = AA.SCHOOLID ");
        stb.append("         AND AB.START_DATE = AA.START_DATE, ");
        stb.append("   SAF_CODE_MASTER AC ");
        stb.append(" WHERE ");
        stb.append("   AA.SCHOOLID NOT IN AC.CODE ");
        stb.append("   AND AC.CODE_ID = '0102' ");

        return stb.toString();
    }

    private class PrintData {
        final String _belong_Schoolid;
        final String _schoolname_Short;
        final String _count;
        final String _lecture_Count;
        final String _training_No;
        final String _training_Name;
        final String _school_Type;
        public PrintData (final String belong_Schoolid, final String schoolname_Short, final String training_No, final String training_Name, final String school_Type, final String count, final String lecture_Count)
        {
            _belong_Schoolid = belong_Schoolid;
            _schoolname_Short = schoolname_Short;
            _training_No = training_No;
            _training_Name = training_Name;
            _school_Type = school_Type;
            _count = count;
            _lecture_Count = lecture_Count;
	    }
	}

}
