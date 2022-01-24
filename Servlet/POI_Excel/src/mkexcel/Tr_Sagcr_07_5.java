package mkexcel;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import util.Alp_Properties;
import util.SetColumn;

//
//専門研修の申込回数
// Excel
//
public class Tr_Sagcr_07_5 {
	Log log = LogFactory.getLog(Tr_Sagcr_07_5.class);
	// メンバー変数
	private Workbook book;
	private final static String USE_SHEETNAME = "専門研修の申込回数";
	private Alp_Properties Prop;
	private String errorMessage = "";
	final static int MAX_CELL_COUNT = 16;
	final static int START_ROW = 2;
	final static int START_COL = 0;

	final static int TEMP_MAX_ROW = 44;

	// 書込みデータ
	String _year = "";
	String _nyear = "";
	List<PrintData> DatList = new ArrayList<PrintData>();

	public Tr_Sagcr_07_5(Workbook book, String information, Alp_Properties prop) {
		this.book = book;
		this.Prop = prop;
		String[] Information = information.split(",");
		if (Information.length < 1) {
			errorMessage = String.format("パラメータエラー{%s}", information);
		} else {
			_year = Information[0];  //yearは上位からパラメータでわたってくる。
			int y = Integer.parseInt(_year);
			_nyear = String.valueOf(y + 1);
			//名前を付けて保存ファイル名を変更
			Calendar cl = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
			String fileName = Prop.getOutputName() + "_" + String.valueOf(_year) + "年度_" + sdf.format(cl.getTime());
			Prop.setOutputName(fileName);
		}
		log.info("Tr_Sagcr_07_5 コンストラクタ パラメータ{" + information + "}");
		//log.warn("$Revision: 72418 $");
	}

	// エラーメッセージ取出し
	public String getErrorMessage() {
		return errorMessage;
	}

	// データ読込み
	public Boolean excel() {
		log.info("Excel出力{" + this.getClass().getName() + "}");
		// 戻り値初期値はfalse
		boolean returnCode = false;
		//データベースリソース
		String JDBC_LOOKUP = Prop.getProperty("jdbc.lookup");
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
			return false;
		}

		String sql = getDataSqlN();
		//log.info(sql);
		try {
			ResultSet rs = dao.query(sql);
			errorMessage = dao.getErrTrace();
			if (errorMessage.length() > 0) { // sql例外発生
				return false;
			}
			while(rs.next()) {
				String str;
				String established_segments_name = StringUtils.defaultString(rs.getString("ESTABLISHED_SEGMENTS_NAME"), "");
				String district_name = StringUtils.defaultString(rs.getString("DISTRICT_NAME"), "");
				String school_type_name = StringUtils.defaultString(rs.getString("SCHOOL_TYPE_NAME"), "");
				String cities_towns_name = StringUtils.defaultString(rs.getString("CITIES_TOWNS_NAME"), "");
				String formal_school_name = StringUtils.defaultString(rs.getString(/*"SCHOOLNAME_SHORT"*/"FORMAL_SCHOOL_NAME"), "");
				//String staff_count = StringUtils.defaultString(rs.getString("STAFF_ID_COUNT"), "0");
				//String str = rs.getString("STAFF_ID_COUNT") != null ? rs.getString("STAFF_ID_COUNT") : "";
				//int staff_count = Integer.parseInt(str);
				int staff_count = rs.getInt("STAFF_ID_COUNT");
				int total = rs.getInt("TOTAL");
				int count = rs.getInt("COUNT");
				str = rs.getString("COUNT1") != null ? rs.getString("COUNT1") : "";
				int count1 = Integer.parseInt(str);
				str = rs.getString("COUNT2") != null ? rs.getString("COUNT2") : "";
				int count2 = Integer.parseInt(str);
				str = rs.getString("COUNT3") != null ? rs.getString("COUNT3") : "";
				int count3 = Integer.parseInt(str);
				str = rs.getString("COUNT4") != null ? rs.getString("COUNT4") : "";
				int count4 = Integer.parseInt(str);
				BigDecimal moushikomi_total_ritu = rs.getBigDecimal("MOUSHIKOMI_TOTAL_RITU");
				BigDecimal moushikomi_real_ritu = rs.getBigDecimal("MOUSHIKOMI_REAL_RITU");
				BigDecimal count0_ritsu = rs.getBigDecimal("COUNT0_RITU");
				String major = rs.getString("MAJOR_CD") != null ? rs.getString("MAJOR_CD").trim() : "";
				String courl = rs.getString("MAJOR_COURCE") != null ? rs.getString("MAJOR_COURCE").trim() : "";
				String courlName = rs.getString("MAJOR") != null ? rs.getString("MAJOR") : "";
				//log.info(formal_school_name + ":majer[" + major + "] courl[" + courl + "] coriName[" + courlName + "]");

				//log.info("[" + moushikomi_total_ritu + "][" + moushikomi_real_ritu + "][" + count0_ritsu + "]");
				PrintData addwk = new PrintData(established_segments_name, district_name, school_type_name, cities_towns_name, formal_school_name, staff_count, total, count
						//, count1, count2, count3, count4, major, courl, ""
						, count1, count2, count3, count4, major, courl, courlName
						, moushikomi_total_ritu, moushikomi_real_ritu, count0_ritsu);
				DatList.add(addwk);
			}
			dao.rsClose();
			//普通/専門を取出す。
			log.info("行数:"+DatList.size());

		} catch (SQLException e) {
			errorMessage = e.toString();
			dao.StackTraceLogging(e);
		} finally {
			dao.close();
		}
		returnCode = setRowsInfo();
		return returnCode;
	}

    private int getByteLength(String str) {
        return str.getBytes(Charset.forName("MS932")).length;
    }
    private int setMaxLengthChk(final int chkDatA, final int chkDatB) {
    	return chkDatA > chkDatB ? chkDatA : chkDatB;
    }
    private int getCellRows(String str, int maxLineByte) {
    	return (int)Math.ceil((float)getByteLength(str) / (float)maxLineByte);
    }

	// Excelシートヘ書込み
	private boolean setRowsInfo() {
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

		//"校種・事務所別の申込者数【yyyy年度】";
		String strWk;
		strWk = "専門研修の申込回数【" + _year + "年度】";
		if (set.setValueString(1, "A", strWk) == false) {
			log.info("空セル " + strWk);
		}
		//"出力日：";
		Date date = new Date();
		SimpleDateFormat fmt1 = new SimpleDateFormat("yyyy/MM/dd");
		String dstrWk = "出力日：" + fmt1.format(date);
		if (set.setValueString(1, "P", dstrWk) == false) {
			log.info("空セル " + dstrWk);
		}

		//不足行を追加
		int count = DatList.size();
		int MAX_LINE = sheet.getLastRowNum() - START_ROW + 1;
		int NEXT_LINE = MAX_LINE + START_ROW;
		log.info("行数:" + count + "[" + MAX_LINE + "]");
		if (count > MAX_LINE) {
			CellStyle[] styles = new CellStyle[MAX_CELL_COUNT];
			CellType[] types = new CellType[MAX_CELL_COUNT];
			Row orgRow = sheet.getRow(START_ROW + 1);
			if (orgRow != null) {
				for(int c = 0; c < MAX_CELL_COUNT; c++) {
					styles[c] = book.createCellStyle();
					styles[c].cloneStyleFrom(orgRow.getCell(c).getCellStyle());
					types[c] = orgRow.getCell(c).getCellType();
				}
			}
			else {
				errorMessage = "行の追加エラー";
				return false;
			}
			for(int L = NEXT_LINE; L < (count + START_ROW); L++) {
				Row row_ = sheet.createRow(L);
				Cell[] cels = new Cell[MAX_CELL_COUNT];
				for(int c = 0; c < MAX_CELL_COUNT; c++) {
					cels[c] = row_.createCell(c, types[c]);
					cels[c].setCellStyle(styles[c]);
				}
			}
		}
		//行データ出力
		int dataidx = 0;
		int strtrow = START_ROW;  //出力開始行
		for (PrintData outinfo : DatList) {
			if(strtrow < 0) {
				log.warn("strtrow:" + strtrow);
			}
			if(dataidx < 0) {
				log.warn("dataidx:" + dataidx);
			}

			row = sheet.getRow(strtrow + dataidx);
			int pos = 0; //出力開始位置(次行処理でリセット)
			int maxlengthWk = 0;
			if(row == null) {
				log.warn("row:" + row);
				log.warn("引数1:" + strtrow);
				log.warn("引数2:" + dataidx);
			}

    		if(outinfo._district_name == null) {
				log.warn("地区名称:" + outinfo._district_name);
			}

			setData(row.getCell(pos++), outinfo._district_name); //地区
            maxlengthWk = setMaxLengthChk(maxlengthWk,getCellRows(outinfo._district_name, 8));
			setData(row.getCell(pos++), outinfo._established_segments_name); //設立区分
			setData(row.getCell(pos++), outinfo._school_type_name); //学校種別

			setData(row.getCell(pos++), outinfo._cities_towns_name); //市町
			setData(row.getCell(pos++), outinfo._courlName);	//普通/専門
			setData(row.getCell(pos++), outinfo._formal_school_name); //学校名
            maxlengthWk = setMaxLengthChk(maxlengthWk,getCellRows(outinfo._formal_school_name, 40));
			setInt(row.getCell(pos++), outinfo._staff_count); //教員数
			setInt(row.getCell(pos++), outinfo._total);  //申込数(延べ)
			setInt(row.getCell(pos++), outinfo._count);  //申込数(実数)

			final int staffCnt = outinfo._staff_count;

			setDecimal(row.getCell(pos++), outinfo._moushikomi_total_ritu);  //申込率(実数)
			setDecimal(row.getCell(pos++), outinfo._moushikomi_real_ritu);  //申込率(延べ)
			setDecimal(row.getCell(pos++), outinfo._count0_ritsu);  //0回率*/


			final int cnt1 = outinfo._count1;
			final int cnt2 = outinfo._count2;
			final int cnt3 = outinfo._count3;
			final int cnt4 = outinfo._count4;
			final int cnt0 =  staffCnt - (cnt1 + cnt2 + cnt3 + cnt4);

			setInt(row.getCell(pos++), cnt0); //0回
			setInt(row.getCell(pos++), cnt1); //1回
			setInt(row.getCell(pos++), cnt2); //2回
			setInt(row.getCell(pos++), cnt3); //3回
			setInt(row.getCell(pos++), cnt4); //4回以上

			row.setHeightInPoints((float)15.0 * (float)maxlengthWk);
		    dataidx++;
		    retFlg = true;
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
		if (retFlg) {
			//印刷範囲設定。
			//引数は、シートindex,開始列index,終了列index,開始行index,終了行index
			//final int endRow = dataidx + strtrow - 1 > TEMP_MAX_ROW ? dataidx + strtrow - 1 : TEMP_MAX_ROW;
			final int endRow = dataidx + strtrow - 1;
			book.setPrintArea(0, 0, 16, 0, endRow);
		}
        return retFlg;
	}

	private void setData(final Cell cel, String vals) {
		if (cel != null) {
			cel.setCellValue(vals);
		}

	}

	private void setInt(final Cell cel, final int vals) {
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

	private class PrintData {
		String _established_segments_name;
		String _district_name;
		String _school_type_name;
		String _cities_towns_name;
		String _formal_school_name;
		int _staff_count;
		int _total;
		int _count;
		int _count1;
		int _count2;
		int _count3;
		int _count4;
		BigDecimal _moushikomi_total_ritu;
		BigDecimal _moushikomi_real_ritu;
		BigDecimal _count0_ritsu;
		String _major;
		String _courl;
		String _courlName;

	    public PrintData (String established_segments_name, String district_name, String school_type_name, String cities_towns_name
	    		, String formal_school_name, int staff_count, int total, int count, int count1, int count2,int count3
	    		, int count4, String major, String courl, String courlName
	    		, BigDecimal moushikomi_total_ritu, BigDecimal moushikomi_real_ritu, BigDecimal count0_ritsu)
	    {
	    	_established_segments_name = established_segments_name;
	    	_district_name = district_name;
	    	_school_type_name = school_type_name;
	    	_cities_towns_name = cities_towns_name;
	    	_formal_school_name = formal_school_name;
	    	_staff_count = staff_count;
	    	_total = total;
	    	_count = count;
	    	_count1 = count1;
	    	_count2 = count2;
	    	_count3 = count3;
	    	_count4 = count4;
	    	_moushikomi_total_ritu = moushikomi_total_ritu;
	    	_moushikomi_real_ritu = moushikomi_real_ritu;
	    	_count0_ritsu = count0_ritsu;
	    	_major = major;
	    	_courl = courl;
	    	_courlName = courlName;
	    }
	}

	//行データ取出しsql
	private String getDataSqlN() {
		StringBuffer stb = new StringBuffer();
		Prop.Append(stb, " SELECT ");
        /*Prop.Append(stb, "  P01.DISTRICT_NAME");
        Prop.Append(stb, "  , P01.ESTABLISHED_SEGMENTS_NAME");
        Prop.Append(stb, "  , P01.SCHOOL_TYPE_NAME");
        Prop.Append(stb, "  , P01.CITIES_TOWNS_NAME");
        Prop.Append(stb, "  , P01.MAJOR");
        Prop.Append(stb, "  , P01.FORMAL_SCHOOL_NAME");
        Prop.Append(stb, "  , P01.STAFF_ID_COUNT");
        Prop.Append(stb, "  , P01.TOTAL");
        Prop.Append(stb, "  , P01.COUNT");
        Prop.Append(stb, "  , CASE WHEN P01.STAFF_ID_COUNT > 0 ");
        Prop.Append(stb, "      THEN CAST( ROUND((DEC (P01.TOTAL) * 100) / DEC (P01.STAFF_ID_COUNT), 1) AS DECIMAL(10, 1)) ");
        Prop.Append(stb, "      ELSE 0.0 ");
        Prop.Append(stb, "      END AS MOUSHIKOMI_TOTAL_RITU");
        Prop.Append(stb, "  , CASE WHEN P01.STAFF_ID_COUNT > 0");
        Prop.Append(stb, "      THEN CAST( ROUND((DEC (P01.COUNT) * 100) / DEC (P01.STAFF_ID_COUNT), 1) AS DECIMAL(10, 1)) ");
        Prop.Append(stb, "      ELSE 0.0 ");
        Prop.Append(stb, "      END AS MOUSHIKOMI_REAL_RITU");
        Prop.Append(stb, "  , CASE ");
        Prop.Append(stb, "    WHEN P01.STAFF_ID_COUNT > 0");
        Prop.Append(stb, "      THEN CAST( ROUND((DEC (P01.STAFF_ID_COUNT - (P01.COUNT1 + P01.COUNT2 + P01.COUNT3 + P01.COUNT4)) * 100) / DEC (P01.STAFF_ID_COUNT), 1) AS DECIMAL(10, 1)) ");
        Prop.Append(stb, "      ELSE 0.0 END AS COUNT0_RITU");
        Prop.Append(stb, "  , P01.STAFF_ID_COUNT - (P01.COUNT1 + P01.COUNT2 + P01.COUNT3 + P01.COUNT4) AS COUNT0");
        Prop.Append(stb, "  , P01.COUNT1");
        Prop.Append(stb, "  , P01.COUNT2");
        Prop.Append(stb, "  , P01.COUNT3");
        Prop.Append(stb, "  , P01.COUNT4 ");
        Prop.Append(stb, "  , P01.MAJOR_COURCE ");
        Prop.Append(stb, "  , P01.MAJOR_CD");
        Prop.Append(stb, "  , P01.SCHOOLID");
        Prop.Append(stb, "FROM");
        Prop.Append(stb, "  ( SELECT");
        Prop.Append(stb, "      F.ITEM1 AS DISTRICT_NAME");
        Prop.Append(stb, "      , E.ITEM1 AS ESTABLISHED_SEGMENTS_NAME");
        Prop.Append(stb, "      , G.ITEM1 AS SCHOOL_TYPE_NAME");
        Prop.Append(stb, "      , H.ITEM1 AS CITIES_TOWNS_NAME");
        Prop.Append(stb, "      , D.MAJOR_COURCE AS MAJOR_COURCE");
        Prop.Append(stb, "      , D.MAJOR AS MAJOR_CD");
        Prop.Append(stb, "      , A.FORMAL_SCHOOL_NAME");
        Prop.Append(stb, "      , A.SCHOOLID");
        Prop.Append(stb, "      , CASE WHEN J.ITEM1 IS NULL THEN I.ITEM1 ELSE J.ITEM1 END AS MAJOR");
        //Prop.Append(stb, "      , CASE WHEN B.STAFF_COUNT > 0 THEN B.STAFF_COUNT ELSE 0 END AS STAFF_ID_COUNT");
		Prop.Append(stb, "      , B.STAFF_ID_CNT AS STAFF_ID_COUNT ");
        Prop.Append(stb, "      , SUM( CASE WHEN C.MOUSHIKOMI_COUNT > 0 THEN C.MOUSHIKOMI_COUNT ELSE 0 END ) AS TOTAL");
        Prop.Append(stb, "      , SUM( CASE WHEN C.MOUSHIKOMI_COUNT > 0 THEN 1 ELSE 0 END ) AS COUNT");
        Prop.Append(stb, "      , SUM( CASE WHEN C.MOUSHIKOMI_COUNT = 1 THEN 1 ELSE 0 END ) AS COUNT1");
        Prop.Append(stb, "      , SUM( CASE WHEN C.MOUSHIKOMI_COUNT = 2 THEN 1 ELSE 0 END ) AS COUNT2");
        Prop.Append(stb, "      , SUM( CASE WHEN C.MOUSHIKOMI_COUNT = 3 THEN 1 ELSE 0 END ) AS COUNT3");
        Prop.Append(stb, "      , SUM( CASE WHEN C.MOUSHIKOMI_COUNT >= 4 THEN 1 ELSE 0 END ) AS COUNT4 ");
        Prop.Append(stb, "    FROM");
        Prop.Append(stb, "      V_SCHOOL_BASIS A ");
        Prop.Append(stb, " LEFT JOIN ( SELECT FROM_SCHOOLCD, ");
		Prop.Append(stb, "      COUNT(DISTINCT STAFFCD) AS STAFF_ID_CNT ");
		Prop.Append(stb, " FROM SAF_STAFF_JOB_G_DAT");
		Prop.Append(stb, " WHERE");
        Prop.Append(stb, "      FROM_DATE <= '" + _year + "-05-01'");
        Prop.Append(stb, " AND (TO_DATE IS NULL");
        Prop.Append(stb, " OR '" + _year + "-05-01' <= TO_DATE)");
        Prop.Append(stb, " AND BONUS_WORKER_FLAG = '0'");
        Prop.Append(stb, " AND PRIORITY= '1'");
        Prop.Append(stb, " AND JOBCD <> '812'");
        Prop.Append(stb, "    GROUP BY");
        Prop.Append(stb, "    FROM_SCHOOLCD");	//2020/02/13
        Prop.Append(stb, " ) B ON B.FROM_SCHOOLCD = A.SCHOOLID ");
        Prop.Append(stb, "      LEFT JOIN V_TRAINING_APPLICATION C ON C.SCHOOLID = A.SCHOOLID AND C.YEAR = '" + _year  + "'");
        Prop.Append(stb, "      LEFT JOIN SAF_SCHOOL_EXTRA_G_DAT D ON D.SCHOOLID = A.SCHOOLID ");
        Prop.Append(stb, "      LEFT JOIN SAF_CODE_MASTER E ON E.CODE_ID = '0006' AND E.CODE = A.ESTABLISHED_SEGMENTS ");
        Prop.Append(stb, "      LEFT JOIN SAF_CODE_MASTER F ON F.CODE_ID = '0014' AND F.CODE = A.DISTRICT ");
        Prop.Append(stb, "      LEFT JOIN SAF_CODE_MASTER G ON G.CODE_ID = '0002' AND G.CODE = A.SCHOOL_TYPE ");
        Prop.Append(stb, "      LEFT JOIN SAF_CODE_MASTER H ON H.CODE_ID = '0008' AND H.CODE = A.CITIES_TOWNS ");
        Prop.Append(stb, "      LEFT JOIN SAF_CODE_MASTER I ON I.CODE_ID = '0109' AND I.CODE = D.MAJOR AND D.MAJOR != '2'");
        Prop.Append(stb, "      LEFT JOIN SAF_CODE_MASTER J ON J.CODE_ID = '0110' AND J.CODE = D.MAJOR_COURCE AND D.MAJOR = '2'");
        Prop.Append(stb, "    WHERE");
        Prop.Append(stb, "      A.SCHOOL_TYPE IN ('01', '02', '03', '04') ");
        Prop.Append(stb, "      AND D.FACULTY_FLAG = '0' ");
        Prop.Append(stb, "    GROUP BY");
        Prop.Append(stb, "      C.YEAR");
        Prop.Append(stb, "      , A.SCHOOLID");
        Prop.Append(stb, "      , A.ESTABLISHED_SEGMENTS");
        Prop.Append(stb, "      , E.ITEM1");
        Prop.Append(stb, "      , A.DISTRICT");
        Prop.Append(stb, "      , F.ITEM1");
        Prop.Append(stb, "      , A.SCHOOL_TYPE");
        Prop.Append(stb, "      , G.ITEM1");
        Prop.Append(stb, "      , A.CITIES_TOWNS");
        Prop.Append(stb, "      , H.ITEM1");
        Prop.Append(stb, "      , D.MAJOR");
        Prop.Append(stb, "      , I.ITEM1");
        Prop.Append(stb, "      , D.MAJOR_COURCE");
        Prop.Append(stb, "      , J.ITEM1");
        Prop.Append(stb, "      , A.FORMAL_SCHOOL_NAME");
        //Prop.Append(stb, "      , B.STAFF_COUNT");
        Prop.Append(stb, "      , B.STAFF_ID_CNT");
        Prop.Append(stb, "       , F.DISPLAY_ORDER");
        Prop.Append(stb, "       , G.ITEM2");
        Prop.Append(stb, "       , H.DISPLAY_ORDER");
        Prop.Append(stb, "       , I.DISPLAY_ORDER");
        Prop.Append(stb, "       , J.DISPLAY_ORDER");
        Prop.Append(stb, "       , A.SCHOOL_CODE");
		Prop.Append(stb, "    ORDER BY");
		Prop.Append(stb, "      F.DISPLAY_ORDER");
		Prop.Append(stb, "      , G.ITEM2");
		Prop.Append(stb, "      , H.DISPLAY_ORDER");
		Prop.Append(stb, "      , I.DISPLAY_ORDER");
		Prop.Append(stb, "      , J.DISPLAY_ORDER");
		Prop.Append(stb, "      , A.SCHOOL_CODE");
        Prop.Append(stb, "  ) P01");*/
		Prop.Append(stb, "    P01.DISTRICT_NAME,");
		Prop.Append(stb, "    P01.ESTABLISHED_SEGMENTS_NAME,");
		Prop.Append(stb, "    P01.SCHOOL_TYPE_NAME,");
		Prop.Append(stb, "    P01.CITIES_TOWNS_NAME,");
		Prop.Append(stb, "    P01.MAJOR,");
		Prop.Append(stb, "    P01.FORMAL_SCHOOL_NAME,");
		Prop.Append(stb, "    P01.STAFF_ID_COUNT,");
		Prop.Append(stb, "    P01.TOTAL,");
		Prop.Append(stb, "    P01.COUNT,");
		Prop.Append(stb, "    CASE WHEN P01.STAFF_ID_COUNT > 0 THEN CAST(ROUND((DEC(P01.TOTAL) * 100) / DEC (P01.STAFF_ID_COUNT),1) AS DECIMAL(10, 1)) ELSE 0.0 END AS MOUSHIKOMI_TOTAL_RITU,");
		Prop.Append(stb, "    CASE WHEN P01.STAFF_ID_COUNT > 0 THEN CAST(ROUND((DEC(P01.COUNT) * 100) / DEC (P01.STAFF_ID_COUNT), 1) AS DECIMAL(10, 1)) ELSE 0.0 END AS MOUSHIKOMI_REAL_RITU,");
		Prop.Append(stb, "    CASE WHEN P01.STAFF_ID_COUNT > 0 THEN CAST(ROUND((DEC(P01.STAFF_ID_COUNT - (P01.COUNT1 + P01.COUNT2 + P01.COUNT3 + P01.COUNT4)) * 100) / DEC (P01.STAFF_ID_COUNT), 1) AS DECIMAL(10, 1)) ELSE 0.0 END AS COUNT0_RITU,");
		Prop.Append(stb, "    P01.STAFF_ID_COUNT - (P01.COUNT1 + P01.COUNT2 + P01.COUNT3 + P01.COUNT4) AS COUNT0,");
		Prop.Append(stb, "    P01.COUNT1,");
		Prop.Append(stb, "    P01.COUNT2,");
		Prop.Append(stb, "    P01.COUNT3,");
		Prop.Append(stb, "    P01.COUNT4,");
		Prop.Append(stb, "    P01.MAJOR_COURCE,");
		Prop.Append(stb, "    P01.MAJOR_CD");
		Prop.Append(stb, " FROM");
		Prop.Append(stb, "    (");
		Prop.Append(stb, "    SELECT");
		Prop.Append(stb, "        case when A.ESTABLISHED_SEGMENTS in ('2','3') then A.DISTRICT");
		Prop.Append(stb, "             when A.ESTABLISHED_SEGMENTS = '1' then '10'");
		Prop.Append(stb, "             when A.ESTABLISHED_SEGMENTS = '4' then '20'");
		Prop.Append(stb, "             else '30' end as DISTRICT_ORDER,");
		Prop.Append(stb, "        A.SCHOOLKIND,");
		Prop.Append(stb, "        H.DISPLAY_ORDER as CITY_ORDER,");
		Prop.Append(stb, "        case when A.ESTABLISHED_SEGMENTS in ('2','3') then F.ITEM1 else NULL end as DISTRICT_NAME,");
		Prop.Append(stb, "        E.ITEM1        AS ESTABLISHED_SEGMENTS_NAME,");
		Prop.Append(stb, "        G.ITEM1        AS SCHOOL_TYPE_NAME,");
		Prop.Append(stb, "        H.ITEM1        AS CITIES_TOWNS_NAME,");
		Prop.Append(stb, "        D.MAJOR_COURCE AS MAJOR_COURCE,");
		Prop.Append(stb, "        D.MAJOR        AS MAJOR_CD,");
		Prop.Append(stb, "        A.SCHOOL_NAME as FORMAL_SCHOOL_NAME,");
		Prop.Append(stb, "        CASE WHEN J.ITEM1 IS NULL THEN I.ITEM1 ELSE J.ITEM1 END AS MAJOR,");
		Prop.Append(stb, "        B.STAFF_ID_CNT AS STAFF_ID_COUNT,");
		Prop.Append(stb, "        SUM(CASE WHEN C.MOUSHIKOMI_COUNT > 0  THEN C.MOUSHIKOMI_COUNT ELSE 0 END) AS TOTAL,");
		Prop.Append(stb, "        SUM(CASE WHEN C.MOUSHIKOMI_COUNT > 0  THEN 1 ELSE 0 END) AS COUNT,");
		Prop.Append(stb, "        SUM(CASE WHEN C.MOUSHIKOMI_COUNT = 1  THEN 1 ELSE 0 END) AS COUNT1,");
		Prop.Append(stb, "        SUM(CASE WHEN C.MOUSHIKOMI_COUNT = 2  THEN 1 ELSE 0 END) AS COUNT2,");
		Prop.Append(stb, "        SUM(CASE WHEN C.MOUSHIKOMI_COUNT = 3  THEN 1 ELSE 0 END) AS COUNT3,");
		Prop.Append(stb, "        SUM(CASE WHEN C.MOUSHIKOMI_COUNT >= 4 THEN 1 ELSE 0 END) AS COUNT4");
		Prop.Append(stb, "    FROM");
		Prop.Append(stb, "        (");
		Prop.Append(stb, "        SELECT");
		Prop.Append(stb, "            *");
		Prop.Append(stb, "        FROM");
		Prop.Append(stb, "            SAF_SCHOOLHIST_G_DAT");
		Prop.Append(stb, "        WHERE");
		Prop.Append(stb, "            (SCHOOLID, SCHSTART) in (SELECT");
		Prop.Append(stb, "                                        SCHOOLID,");
		Prop.Append(stb, "                                        MAX(SCHSTART) AS SCHSTART");
		Prop.Append(stb, "                                    FROM");
		Prop.Append(stb, "                                        SAF_SCHOOLHIST_G_DAT");
		Prop.Append(stb, "                                    WHERE");
		Prop.Append(stb, "                                        SCHSTART <= '" + _nyear + "-03-31' AND");
		Prop.Append(stb, "                                        value(SCHEND, '9999-03-31') >= '" + _year + "-04-01'");
		Prop.Append(stb, "                                    GROUP BY");
		Prop.Append(stb, "                                        SCHOOLID");
		Prop.Append(stb, "                                    )");
		Prop.Append(stb, "        ) A");
		Prop.Append(stb, "        LEFT JOIN (SELECT");
		Prop.Append(stb, "                        FROM_SCHOOLCD,");
		Prop.Append(stb, "                        COUNT(DISTINCT STAFFCD) AS STAFF_ID_CNT");
		Prop.Append(stb, "                    FROM");
		Prop.Append(stb, "                        SAF_STAFF_JOB_G_DAT");
		Prop.Append(stb, "                    WHERE");
		Prop.Append(stb, "                        FROM_DATE <= '" + _year + "-05-01' AND");
		Prop.Append(stb, "                        (TO_DATE IS NULL OR '" + _year + "-05-01' <= TO_DATE) AND");
		Prop.Append(stb, "                        BONUS_WORKER_FLAG = '0' AND");
		Prop.Append(stb, "                        PRIORITY = '1' AND");
		Prop.Append(stb, "                        JOBCD <> '812'");
		Prop.Append(stb, "                    GROUP BY");
		Prop.Append(stb, "                        FROM_SCHOOLCD");
		Prop.Append(stb, "                    ) B ON B.FROM_SCHOOLCD = A.SCHOOLID ");
		Prop.Append(stb, "        LEFT JOIN V_TRAINING_APPLICATION C ON C.SCHOOLID = A.SCHOOLID AND C.YEAR = '" + _year + "' ");
		Prop.Append(stb, "        LEFT JOIN SAF_SCHOOL_EXTRA_G_DAT D ON D.SCHOOLID = A.SCHOOLID ");
		Prop.Append(stb, "        LEFT JOIN SAF_CODE_MASTER E ON E.CODE_ID = '0006' AND E.CODE = A.ESTABLISHED_SEGMENTS ");
		Prop.Append(stb, "        LEFT JOIN SAF_CODE_MASTER F ON F.CODE_ID = '0014' AND F.CODE = A.DISTRICT ");
		Prop.Append(stb, "        LEFT JOIN SAF_CODE_MASTER G ON G.CODE_ID = '0002' AND G.CODE = A.SCHOOLKIND ");
		Prop.Append(stb, "        LEFT JOIN SAF_CODE_MASTER H ON H.CODE_ID = '0008' AND H.CODE = A.CITIES_TOWNS ");
		Prop.Append(stb, "        LEFT JOIN SAF_CODE_MASTER I ON I.CODE_ID = '0109' AND I.CODE = D.MAJOR AND D.MAJOR != '2' ");
		Prop.Append(stb, "        LEFT JOIN SAF_CODE_MASTER J ON J.CODE_ID = '0110' AND J.CODE = D.MAJOR_COURCE AND D.MAJOR = '2' ");
		Prop.Append(stb, "    WHERE");
		Prop.Append(stb, "        A.SCHOOLKIND IN ('01', '02', '03', '04') AND");
		Prop.Append(stb, "        A.SCHOOLID not in (SELECT CODE FROM SAF_CODE_MASTER WHERE CODE_ID = '0102') AND");
		Prop.Append(stb, "        D.FACULTY_FLAG = '0' ");
		Prop.Append(stb, "    GROUP BY");
		Prop.Append(stb, "        C.YEAR, ");
		Prop.Append(stb, "        A.SCHOOLID, ");
		Prop.Append(stb, "        A.ESTABLISHED_SEGMENTS, ");
		Prop.Append(stb, "        E.ITEM1, ");
		Prop.Append(stb, "        A.DISTRICT, ");
		Prop.Append(stb, "        F.ITEM1, ");
		Prop.Append(stb, "        A.SCHOOLKIND, ");
		Prop.Append(stb, "        G.ITEM1, ");
		Prop.Append(stb, "        A.CITIES_TOWNS, ");
		Prop.Append(stb, "        H.ITEM1, ");
		Prop.Append(stb, "        D.MAJOR, ");
		Prop.Append(stb, "        I.ITEM1, ");
		Prop.Append(stb, "        D.MAJOR_COURCE, ");
		Prop.Append(stb, "        J.ITEM1, ");
		Prop.Append(stb, "        A.SCHOOL_NAME, ");
		Prop.Append(stb, "        B.STAFF_ID_CNT, ");
		Prop.Append(stb, "        F.DISPLAY_ORDER, ");
		Prop.Append(stb, "        G.ITEM2, ");
		Prop.Append(stb, "        H.DISPLAY_ORDER, ");
		Prop.Append(stb, "        I.DISPLAY_ORDER, ");
		Prop.Append(stb, "        J.DISPLAY_ORDER, ");
		Prop.Append(stb, "        A.SCHOOLID ");
		Prop.Append(stb, "    ORDER BY");
		Prop.Append(stb, "        F.DISPLAY_ORDER, ");
		Prop.Append(stb, "        G.ITEM2, ");
		Prop.Append(stb, "        H.DISPLAY_ORDER, ");
		Prop.Append(stb, "        I.DISPLAY_ORDER, ");
		Prop.Append(stb, "        J.DISPLAY_ORDER, ");
		Prop.Append(stb, "        A.SCHOOLID");
		Prop.Append(stb, "    ) P01");
		Prop.Append(stb, " ORDER BY");
		Prop.Append(stb, "    P01.DISTRICT_ORDER,");
		Prop.Append(stb, "    P01.SCHOOLKIND,");
		Prop.Append(stb, "    P01.CITY_ORDER");

        return stb.toString();
	}
}
