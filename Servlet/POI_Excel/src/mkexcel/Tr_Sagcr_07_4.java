package mkexcel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import util.Alp_Properties;
import util.SetColumn;

////職種別の申込者数出力
public class Tr_Sagcr_07_4 {
	Log log = LogFactory.getLog(Tr_Sagcr_07_4.class);
	// メンバー変数
	private Workbook book;
	private Alp_Properties Prop;
	private String errorMessage = "";
	private final static String USE_SHEETNAME = "職種別の申込者数";
	private int lastCellPosition = 23;
	final static int MAX_COL_COUNT = 2;	//+HEADER_START_COLがヘッダー行の初期セル数

	final static int HEADER_START_ROW = 1;
	final static int HEADER_START_COL = 6;

	final static int BODY_START_ROW = 3;
	final static int BODY_START_COL = 0;
	private Map<String, Integer> cellMap = new HashMap<String, Integer>();;

	// 書込みデータ
	String _ctrlYear = "";	//年
	String _nyear = "";	//学期末年

	public Tr_Sagcr_07_4(Workbook book, String information, Alp_Properties prop) {

		this.book = book;
		this.Prop = prop;
		String[] Information = information.split(",");
		if (Information.length < 1) {
			errorMessage = String.format("パラメータエラー{%s}", information);
		} else {
			_ctrlYear = Information[0].trim();
			//名前を付けて保存ファイル名を変更
			Calendar cl = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
			String fileName = Prop.getOutputName() + "_" + _ctrlYear + "年度_" + sdf.format(cl.getTime());
			Prop.setOutputName(fileName);
			_nyear = String.valueOf(Integer.parseInt(_ctrlYear) + 1);
		}
		log.info("Tr_Sagcr_07_4 コンストラクタ パラメータ{" + information + "}");
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
		util.DaoUtil db2 = new util.DaoUtil(Prop.getProperty("sqllogging").equalsIgnoreCase("true"), log);
		db2pt = db2.open(JDBC_LOOKUP);
		if (db2pt == null) {
			// オープン失敗
			errorMessage = "データベース接続失敗";
			log.error("dao.open() error");
			return false;
		}

		//メイン処理
		try {

			// 書込みシートを開く
			Sheet sheet = book.getSheet(USE_SHEETNAME);
			if (sheet == null) {
				errorMessage = "対象のシート見つからず";
				return false;
			}

			//ヘッダー
			List<PrintDataHeader> HeaderList = getHeaderData(db2);
			if (HeaderList.size() > 0) {
				returnCode = setHeaderInfo(sheet, HeaderList);
			}
			else {
				log.error("ヘッダーとれず!!");
			}
			//明細
			if(returnCode) {
				List<PrintDataBody> lines = getBodyData(db2);
				returnCode = setBodyInfo(sheet, lines);
			}

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

	//ヘッダー部をセット
	private boolean setHeaderInfo(final Sheet sheet, final List<PrintDataHeader> DataList) {
		boolean retFlg = false;
		Row row;
		int maxCount = DataList.size();
		int maxCol = maxCount + HEADER_START_COL;

		//カラム不足なら補う
		if (maxCount > MAX_COL_COUNT) {
			int MAX_LINE = sheet.getLastRowNum() + 1;
			for(int l = 0; l < MAX_LINE; l++) {
				Row row_ = sheet.getRow(l);
				CellStyle style = book.createCellStyle();
				style.cloneStyleFrom(row_.getCell(HEADER_START_COL).getCellStyle());
				CellType type = row_.getCell(HEADER_START_COL).getCellType();
				for(int c = MAX_COL_COUNT + HEADER_START_COL; c < maxCol; c++) {
					Cell cel_ = row_.createCell(c, type);
					cel_.setCellStyle(style);
				}
			}
			int celLength = sheet.getColumnWidth(HEADER_START_COL);
			for(int c = MAX_COL_COUNT + HEADER_START_COL; c < maxCol; c++) {
				sheet.setColumnWidth(c, celLength);
			}
			//セルの右端位置を更新
			lastCellPosition = maxCol;
		}
		log.info("セルの右端位置:" + maxCol);

		SetColumn set = new SetColumn();
		set.setSheet(sheet);

		//"校種・事務所別の申込者数【yyyy年度】";
		final String strWk;
		strWk = "職種別の申込者数【" + _ctrlYear + "年度】";
		row = sheet.getRow(0);
		row.getCell(0).setCellValue(strWk);
		//set.setValueString(1, "A", strWk);

		//"出力日：";
		Date date = new Date();
		SimpleDateFormat fmt1 = new SimpleDateFormat("yyyy/MM/dd");
		String dstrWk = "出力日：" + fmt1.format(date);
		int lastCellPosition = maxCount + HEADER_START_COL - 1;
		row.getCell(lastCellPosition).setCellValue(dstrWk);
		//set.setValueString(1, "AB", dstrWk);

		int firstCol = HEADER_START_COL;
		int cellnum = HEADER_START_COL;
		//int maxlengthWk = 0;
		//int length = 5; // 1セル当たりの文字数
		//int wk = 1; //セル数
		String defSchoolType = "";
		//String lastSchoolTypeName = "";
		for (Iterator<PrintDataHeader> ite = DataList.iterator();ite.hasNext();) {
			PrintDataHeader outinfo = (PrintDataHeader)ite.next();
			int pos = HEADER_START_ROW;

			row = sheet.getRow(pos);
//log.info("TITLE:"+outinfo._schoolTypeName+","+outinfo._Jobname);
			setData(row.getCell(cellnum), outinfo._schoolTypeName); //学校種別名

            row = sheet.getRow(pos + 1);
			setData(row.getCell(cellnum), outinfo._Jobname);  //職種名

		    //wk = 1 + cellnum - firstCol; // セル数
			//if (wk > 1) length = 5 * wk + wk;  // 1セル当たりの文字数 * セル数 + セル数

			if(!"".equals(defSchoolType) && !defSchoolType.equals(outinfo._schoolType)) {
				//セル結合
				if(firstCol != cellnum-1) sheet.addMergedRegion(new CellRangeAddress(1, 1, firstCol, cellnum-1));
				firstCol = cellnum;

			}

			//学校種別、職名コードと一致するセルを保持
			final String key = outinfo._schoolType + outinfo._rankCode;
			cellMap.put(key, cellnum);

			defSchoolType = outinfo._schoolType;
			//lastSchoolTypeName = outinfo._schoolTypeName;
			cellnum++;
		    retFlg = true;
		}

		if(DataList.size() >= 1) {
			//セル結合
			if(firstCol < lastCellPosition) {
				sheet.addMergedRegion(new CellRangeAddress(1, 1, firstCol, lastCellPosition));
			}
		}

        return retFlg;
	}

	//データ行をセット
	private boolean setBodyInfo(final Sheet sheet, List<PrintDataBody> lines) {
		boolean retFlg = false;
		Row row;

		if(lines.size() < 1) return retFlg;

		SetColumn set = new SetColumn();
		set.setSheet(sheet);
		//不足行の追加情報取出し
		int MAX_LINE = sheet.getLastRowNum() - BODY_START_ROW + 1;
		CellStyle[] styles = new CellStyle[lastCellPosition];
		CellType[] types = new CellType[lastCellPosition];
		Row orgRow = sheet.getRow(BODY_START_ROW + 1);
		if (orgRow != null) {
			for(int c = 0; c < lastCellPosition; c++) {
				styles[c] = book.createCellStyle();
				styles[c].cloneStyleFrom(orgRow.getCell(c).getCellStyle());
				types[c] = orgRow.getCell(c).getCellType();
			}
		}
		else {
			errorMessage = "行の追加情報取出しエラー";
			return false;
		}
		//データ行セット
		int dataidx = 0;
		int strtrow = BODY_START_ROW;  //出力開始行
		row = sheet.getRow(strtrow);
		String cmptrainingNo = "";
		for (PrintDataBody outinfo : lines) {
			if (!cmptrainingNo.equals(outinfo._trainingNo)) {
				int pos = BODY_START_COL; //出力開始位置(次行処理でリセット)
				if (dataidx >= MAX_LINE) {	//行不足
					//行を追加
					row =  sheet.createRow(strtrow + dataidx);
					Cell[] cels = new Cell[lastCellPosition];
					for(int c = 0; c < lastCellPosition; c++) {
						cels[c] = row.createCell(c, types[c]);
						cels[c].setCellStyle(styles[c]);
					}
				}
				else {
					row = sheet.getRow(strtrow + dataidx);
				}
				cmptrainingNo = outinfo._trainingNo;
				setData(row.getCell(pos++), outinfo._trainingTypeName); //研修種別
				setData(row.getCell(pos++), outinfo._schoolName); //対象校種
				setData(row.getCell(pos++), outinfo._trainingNo); //研修番号

				setData(row.getCell(pos++), outinfo._trainingName); //研修名
				setInt(row.getCell(pos++), outinfo._capacity);   //定員
				setInt(row.getCell(pos++), outinfo._count);  //申込総数
				dataidx++;
				//行高さ調整
				Cell dummy = row.getCell(lastCellPosition);
				if (dummy == null) {
					dummy = row.createCell(lastCellPosition);
				}
				CellStyle style =  book.createCellStyle();
	            style.setWrapText(true);
	            row.setRowStyle(style);
	            dummy.setCellStyle(style);
			}

			if (outinfo._columnPos > 0) {
				if (outinfo._applicationCount > 0) {
					setInt(row.getCell(outinfo._columnPos), outinfo._applicationCount);
				}
			}
		    retFlg = true;
		}
		log.info("データ行数[" + dataidx + "] 項目数[" + cellMap.size() + "]");
		if (retFlg) {
			//印刷範囲設定。
			int endRow = dataidx + BODY_START_ROW - 1;
			int endCol = cellMap.size() + HEADER_START_COL - 1;
			book.setPrintArea(0, 0, endCol, 0, endRow);
		}
        return retFlg;
	}

	private void setData(final Cell cel, final String vals) {
		if (cel != null) {
			cel.setCellValue(vals);
		}
	}

	private void setInt(final Cell cel, final int vals) {
		if (cel != null) {
			cel.setCellValue(vals);
		}
	}

    private String getSchoolViewSql() {
        final StringBuffer stb = new StringBuffer();

        //学校VIEW
        Prop.Append(stb, " SELECT ");
        Prop.Append(stb, "   A.SCHOOLID, A.SCHOOL_TYPE ");
        Prop.Append(stb, " FROM  ");
        Prop.Append(stb, "   V_SCHOOL_BASIS A ");
        /*					//2020/02/10
        Prop.Append(stb, "   INNER JOIN (SELECT SCHOOLID, MAX(START_DATE) AS START_DATE ");
        Prop.Append(stb, "                FROM V_SCHOOL_BASIS ");
        Prop.Append(stb, "               WHERE START_DATE <= '" + _nyear  + "-03-31' ");
        Prop.Append(stb, "                 AND END_DATE   >= '" + _ctrlYear + "-04-01' ");
        Prop.Append(stb, "            GROUP BY SCHOOLID ");
        Prop.Append(stb, "             ) B  ");
        Prop.Append(stb, "          ON B.SCHOOLID = A.SCHOOLID ");
        Prop.Append(stb, "         AND B.START_DATE = A.START_DATE ");*/
        Prop.Append(stb, "   WHERE A.SCHOOLID not in (SELECT C.CODE FROM SAF_CODE_MASTER C ");
        Prop.Append(stb, "         WHERE C.CODE_ID = '0102') ");

        return stb.toString();
    }

    private String getLectureSchoolViewSql() {
        final StringBuffer stb = new StringBuffer();

        //研修校種VIEW
        Prop.Append(stb, " SELECT ");
        Prop.Append(stb, "   A.YEAR, ");
        Prop.Append(stb, "   A.TRAINING_TYPE_CODE, ");
        Prop.Append(stb, "   A.TRAINING_ID, ");
        Prop.Append(stb, "   COUNT(*) AS COUNT, ");
        Prop.Append(stb, "   MAX(CASE WHEN A.SCHOOL_TYPE = '01' THEN A.SCHOOL_TYPE ELSE NULL END) AS SCHOOL_TYPE1, ");
        Prop.Append(stb, "   MAX(CASE WHEN A.SCHOOL_TYPE = '01' THEN B.ITEM1 ELSE NULL END) AS SCHOOL_NAME1, ");
        Prop.Append(stb, "   MAX(CASE WHEN A.SCHOOL_TYPE = '01' THEN B.ITEM2 ELSE NULL END) AS SCHOOL_SORT1, ");
        Prop.Append(stb, "   MAX(CASE WHEN A.SCHOOL_TYPE = '02' THEN A.SCHOOL_TYPE ELSE NULL END) AS SCHOOL_TYPE2, ");
        Prop.Append(stb, "   MAX(CASE WHEN A.SCHOOL_TYPE = '02' THEN B.ITEM1 ELSE NULL END) AS SCHOOL_NAME2, ");
        Prop.Append(stb, "   MAX(CASE WHEN A.SCHOOL_TYPE = '02' THEN B.ITEM2 ELSE NULL END) AS SCHOOL_SORT2, ");
        Prop.Append(stb, "   MAX(CASE WHEN A.SCHOOL_TYPE = '03' THEN A.SCHOOL_TYPE ELSE NULL END) AS SCHOOL_TYPE3, ");
        Prop.Append(stb, "   MAX(CASE WHEN A.SCHOOL_TYPE = '03' THEN B.ITEM1 ELSE NULL END) AS SCHOOL_NAME3, ");
        Prop.Append(stb, "   MAX(CASE WHEN A.SCHOOL_TYPE = '03' THEN B.ITEM2 ELSE NULL END) AS SCHOOL_SORT3, ");
        Prop.Append(stb, "   MAX(CASE WHEN A.SCHOOL_TYPE = '04' THEN A.SCHOOL_TYPE ELSE NULL END) AS SCHOOL_TYPE4, ");
        Prop.Append(stb, "   MAX(CASE WHEN A.SCHOOL_TYPE = '04' THEN B.ITEM1 ELSE NULL END) AS SCHOOL_NAME4, ");
        Prop.Append(stb, "   MAX(CASE WHEN A.SCHOOL_TYPE = '04' THEN B.ITEM2 ELSE NULL END) AS SCHOOL_SORT4, ");
        Prop.Append(stb, "   MAX(CASE WHEN A.SCHOOL_TYPE = '05' THEN A.SCHOOL_TYPE ELSE NULL END) AS SCHOOL_TYPE5, ");
        Prop.Append(stb, "   MAX(CASE WHEN A.SCHOOL_TYPE = '05' THEN B.ITEM1 ELSE NULL END) AS SCHOOL_NAME5, ");
        Prop.Append(stb, "   MAX(CASE WHEN A.SCHOOL_TYPE = '05' THEN B.ITEM2 ELSE NULL END) AS SCHOOL_SORT5 ");
        Prop.Append(stb, " FROM  ");
        Prop.Append(stb, "   SAF_LECTURE_SCHOOL_TYPE A ");
        Prop.Append(stb, "   INNER JOIN SAF_CODE_MASTER B ");
        Prop.Append(stb, "          ON B.CODE    = A.SCHOOL_TYPE ");
        Prop.Append(stb, "         AND B.CODE_ID = '0002'    ");
        Prop.Append(stb, " WHERE ");
        Prop.Append(stb, "   A.YEAR = '" + _ctrlYear + "' ");
        Prop.Append(stb, " GROUP BY ");
        Prop.Append(stb, "   A.YEAR, ");
        Prop.Append(stb, "   A.TRAINING_TYPE_CODE, ");
        Prop.Append(stb, "   A.TRAINING_ID ");

        return stb.toString();
    }

    private String getTrainingApplicationViewSql() {
        final StringBuffer stb = new StringBuffer();

        //研修申込総数VIEW
        Prop.Append(stb, " SELECT ");
        Prop.Append(stb, "   A.YEAR, ");
        Prop.Append(stb, "   A.TRAINING_TYPE_CODE, ");
        Prop.Append(stb, "   B.TRAINING_TYPE_FULLNAME, ");
        Prop.Append(stb, "   B.DISPLAY_ORDER, ");
        Prop.Append(stb, "   A.TRAINING_ID, ");
        Prop.Append(stb, "   A.TRAINING_NO, ");
        Prop.Append(stb, "   A.TRAINING_NAME, ");
        Prop.Append(stb, "   A.CAPACITY, ");
        Prop.Append(stb, "   E.SCHOOL_NAME1, ");
        Prop.Append(stb, "   E.SCHOOL_NAME2, ");
        Prop.Append(stb, "   E.SCHOOL_NAME3, ");
        Prop.Append(stb, "   E.SCHOOL_NAME4, ");
        Prop.Append(stb, "   E.SCHOOL_NAME5, ");
        Prop.Append(stb, "   COUNT(C.ID) AS COUNT ");
        Prop.Append(stb, " FROM  ");
        Prop.Append(stb, "   SAF_TRAINING_INFOMATION A ");
        Prop.Append(stb, "   INNER JOIN SAF_TRAINING_TYPE B ");
        Prop.Append(stb, "           ON B.YEAR = A.YEAR ");
        Prop.Append(stb, "          AND B.TRAINING_TYPE_CODE = A.TRAINING_TYPE_CODE ");
        Prop.Append(stb, "   LEFT JOIN SAF_TRAINING_APPLICATION C ");
        Prop.Append(stb, "           ON C.YEAR = A.YEAR ");
        Prop.Append(stb, "          AND C.TRAINING_TYPE_CODE = A.TRAINING_TYPE_CODE ");
        Prop.Append(stb, "          AND C.TRAINING_ID = A.TRAINING_ID ");
        Prop.Append(stb, "          AND C.DELETER_OFFICE NOT IN ('1','2')");	//削除フラグ追加
/*        Prop.Append(stb, "   LEFT JOIN SAF_CODE_MASTER D ");	2020/01/15
        Prop.Append(stb, "          ON D.CODE    = B.TRAINING_TYPE_CODE ");
        Prop.Append(stb, "         AND D.CODE_ID = '0106'    ");*/
        Prop.Append(stb, "   LEFT JOIN ( ");
        Prop.Append(stb, getLectureSchoolViewSql()); //研修校種VIEW
        Prop.Append(stb, "             ) E ");
        Prop.Append(stb, "           ON E.YEAR = A.YEAR ");
        Prop.Append(stb, "          AND E.TRAINING_TYPE_CODE = A.TRAINING_TYPE_CODE ");
        Prop.Append(stb, "          AND E.TRAINING_ID = A.TRAINING_ID ");
        Prop.Append(stb, " WHERE ");
        Prop.Append(stb, "   A.YEAR = '" + _ctrlYear + "' ");
        Prop.Append(stb, " GROUP BY ");
        Prop.Append(stb, "   A.YEAR, ");
        Prop.Append(stb, "   A.TRAINING_TYPE_CODE, ");
        Prop.Append(stb, "   B.TRAINING_TYPE_FULLNAME, ");
        Prop.Append(stb, "   B.DISPLAY_ORDER, ");
        Prop.Append(stb, "   A.TRAINING_ID, ");
        Prop.Append(stb, "   A.TRAINING_NO, ");
        Prop.Append(stb, "   A.TRAINING_NAME, ");
        Prop.Append(stb, "   A.CAPACITY, ");
        Prop.Append(stb, "   E.SCHOOL_NAME1, ");
        Prop.Append(stb, "   E.SCHOOL_NAME2, ");
        Prop.Append(stb, "   E.SCHOOL_NAME3, ");
        Prop.Append(stb, "   E.SCHOOL_NAME4, ");
        Prop.Append(stb, "   E.SCHOOL_NAME5 ");

        return stb.toString();
    }

    //ヘッダー部取出し
	private List<PrintDataHeader> getHeaderData(final util.DaoUtil db2) {
		List<PrintDataHeader> retList = new ArrayList<>();
		ResultSet rs = null;

		String sql = getTrainingApplicationCntSql();
		try {
			rs = db2.query(sql);
			errorMessage = db2.getErrTrace();
			if (errorMessage.length() > 0) { // sql例外発生
				return retList;
			}
			String save_schoolType = "";
			String save_schoolTypeName = "";
			String save_rankCode = "";
			String save_jobname = "";
			while(rs.next()) {

				String schoolType = rs.getString("SCHOOL_TYPE");
				String schoolTypeName = rs.getString("SCHOOL_TYPE_NAME");
				String rankCode = rs.getString("RANK_CODE");
				String jobname = rs.getString("JOBNAME1");
				String jobCode = rs.getString("JOBCD");
//log.info(schoolType+","+schoolTypeName+","+rankCode+","+jobname+","+jobCode);
				int type = Integer.parseInt(schoolType);
				/*if (rankCode == null) {	//職種名称が取れなければランクコードを'9999'に統一
					rankCode = "9999";
					jobname = "";
				}
				if (type >= 5) {	//学校種別'01'～'04'以外
					schoolTypeName = "その他";	//学校種別名称は'その他'に統一
					schoolType = "99";	//学校種別は'99'に統一
				}*/
				if (type == 99) {
					schoolTypeName = "その他";	//学校種別名称は'その他'にする
				}
//log.info(schoolType+","+schoolTypeName+","+rankCode+","+jobname+","+jobCode);
				if ((save_schoolType != schoolType) || (save_schoolTypeName != schoolTypeName) || (save_rankCode != rankCode) || (save_jobname != jobname)) {
					PrintDataHeader addwk = new PrintDataHeader(schoolType, schoolTypeName, rankCode, jobname);
					retList.add(addwk);
				}
				save_schoolType = schoolType;
				save_schoolTypeName = schoolTypeName;
				save_rankCode = rankCode;
				save_jobname = jobname;
			}

		} catch (SQLException e) {
			errorMessage = e.toString();
			db2.StackTraceLogging(e);
		} finally {
		}

		return retList;
	}

	//ヘッダー部取出し
    private String getTrainingApplicationCntSql() {
        final StringBuffer stb = new StringBuffer();

        //職種別の申込者数
        Prop.Append(stb, " SELECT DISTINCT ");
        Prop.Append(stb, "   CASE WHEN C.SCHOOL_TYPE IS NOT NULL THEN C.SCHOOL_TYPE ELSE '99' END AS SCHOOL_TYPE, ");
        Prop.Append(stb, "   D.ITEM1 AS SCHOOL_TYPE_NAME, ");
        Prop.Append(stb, "   D.ITEM2, ");
        Prop.Append(stb, "   B.RANK_CODE, ");
        Prop.Append(stb, "   E.ORDERNO, "); // 2020/02/15
        Prop.Append(stb, "   E.JOBCD, ");
        Prop.Append(stb, "   E.JOBNAME1 ");
        Prop.Append(stb, " FROM  ");
        Prop.Append(stb, "   SAF_TRAINING_INFOMATION A ");
        Prop.Append(stb, "   INNER JOIN SAF_TRAINING_APPLICATION B ");
        Prop.Append(stb, "           ON B.YEAR = A.YEAR ");
        Prop.Append(stb, "          AND B.TRAINING_TYPE_CODE = A.TRAINING_TYPE_CODE ");
        Prop.Append(stb, "          AND B.TRAINING_ID = A.TRAINING_ID ");
        Prop.Append(stb, "          AND B.DELETER_OFFICE NOT IN ('1','2')");	//削除フラグ追加
        Prop.Append(stb, "   LEFT JOIN (SELECT T1.SCHOOLID, ");
        //Prop.Append(stb, "                      CASE WHEN T1.SCHOOL_TYPE NOT IN ('01','02','03','04','05') THEN '99' ");
        Prop.Append(stb, "                      CASE WHEN T1.SCHOOL_TYPE NOT IN ('01','02','03','04') THEN '99' ");
        Prop.Append(stb, "                           ELSE T1.SCHOOL_TYPE END AS SCHOOL_TYPE ");
        Prop.Append(stb, "                FROM ( ");
        Prop.Append(stb, getSchoolViewSql()); //学校VIEW
        Prop.Append(stb, "                     ) T1 ");
        Prop.Append(stb, "              ) C ON C.SCHOOLID = B.BELONG_SCHOOLID ");
        Prop.Append(stb, "   LEFT JOIN SAF_CODE_MASTER D ");
        Prop.Append(stb, "          ON D.CODE    = C.SCHOOL_TYPE ");
        Prop.Append(stb, "         AND D.CODE_ID = '0002'    ");
        Prop.Append(stb, "   LEFT JOIN SAF_JOB_MST E ");
        Prop.Append(stb, "          ON E.JOBCD = B.RANK_CODE ");
        Prop.Append(stb, " WHERE ");
        Prop.Append(stb, "   A.YEAR = '" + _ctrlYear + "' ");
        Prop.Append(stb, " ORDER BY ");
        Prop.Append(stb, "   ITEM2, ");
        Prop.Append(stb, "   E.ORDERNO, "); // 2020/02/15
        Prop.Append(stb, "   JOBCD ");

        return stb.toString();
    }

	private class PrintDataHeader {
		final String _schoolType;
		final String _schoolTypeName;
		final String _rankCode;
		final String _Jobname;

	    public PrintDataHeader (final String schoolType, final String schoolTypeName, final String rankCode, final String Jobname)
	    {
			_schoolType = schoolType;
			_schoolTypeName = schoolTypeName;
			_rankCode = rankCode;
			_Jobname = Jobname;
	    }
	}

	//private Map<String, PrintDataBody> getBodyData(final util.DaoUtil db2) {
	private List<PrintDataBody> getBodyData(final util.DaoUtil db2) {
		//Map<String, PrintDataBody> retMap = new HashMap<>();
		List <PrintDataBody> lines = new ArrayList<PrintDataBody>();

		ResultSet rs = null;

		String sql = getJobApplicationCntSql();
		try {
			rs = db2.query(sql);
			errorMessage = db2.getErrTrace();
			if (errorMessage.length() > 0) { // sql例外発生
				return lines;
			}
int no = 0;
			while(rs.next()) {
				String trainingTypeCode = rs.getString("TRAINING_TYPE_CODE");
				String trainingTypeName = rs.getString("TRAINING_TYPE_FULLNAME");
				String trainingId = rs.getString("TRAINING_ID");
				int nmb = rs.getInt("TRAINING_NO");
				String trainingNo = String.format("%04d",  nmb);
				String trainingName = rs.getString("TRAINING_NAME");
				int capacity = rs.getInt("CAPACITY");
				int count = rs.getInt("COUNT");

				String schoolType = rs.getString("SCHOOL_TYPE");
				String rankCode = rs.getString("RANK_CODE");
				int applicationCount = rs.getInt("APPLICATION_COUNT");
				String jobCode = rs.getString("JOBCD");

//log.info(++no + ":" + trainingNo + "[" + schoolType + "][" + rankCode + "][" + jobCode + "][" + count + "][" + applicationCount + "]");
				/*if (rankCode == null) {
					rankCode = "9999";
				}
				int type = schoolType != null ? Integer.parseInt(schoolType) : 99;
				if (type >= 5) {	//学校種別'01'～'04'以外
					schoolType = "99";	//学校種別は'99'に統一
				}*/
//log.info(no + ":" + trainingNo + "[" + schoolType + "][" + rankCode + "][" + jobCode + "][" + count + "][" + applicationCount + "]");
				String schoolName = "";
				String sep = "";
				for(int idx = 1; idx <= 5; idx++) {
					final String val = rs.getString("SCHOOL_NAME" + idx);
					if (!(val == null)) {
						schoolName = schoolName + sep + val;
						sep = "、";
					}
				}
				//ハッシュマップへ登録
				String Key = schoolType + rankCode;
				int pos = cellMap.get(Key) != null ? cellMap.get(Key) : 0;
				PrintDataBody addwk = new PrintDataBody(trainingTypeName, trainingNo, trainingName, capacity, schoolName, count, applicationCount, pos);
				lines.add(addwk);
			}
		} catch (SQLException e) {
			errorMessage = e.toString();
			db2.StackTraceLogging(e);
		} finally {
		}

		return lines;
	}

    private String getJobApplicationCntSql() {
        final StringBuffer stb = new StringBuffer();

        //職名ごとの申込人数
        Prop.Append(stb, " SELECT ");
        Prop.Append(stb, "   A.TRAINING_TYPE_CODE, ");
        Prop.Append(stb, "   A.TRAINING_TYPE_FULLNAME, ");
        Prop.Append(stb, "   A.DISPLAY_ORDER, ");
        Prop.Append(stb, "   A.TRAINING_ID, ");
        Prop.Append(stb, "   A.TRAINING_NO, ");
        Prop.Append(stb, "   A.TRAINING_NAME, ");
        Prop.Append(stb, "   A.CAPACITY, ");
        Prop.Append(stb, "   A.SCHOOL_NAME1, ");
        Prop.Append(stb, "   A.SCHOOL_NAME2, ");
        Prop.Append(stb, "   A.SCHOOL_NAME3, ");
        Prop.Append(stb, "   A.SCHOOL_NAME4, ");
        Prop.Append(stb, "   A.SCHOOL_NAME5, ");
        Prop.Append(stb, "   A.COUNT, ");
        Prop.Append(stb, "   E.JOBCD, ");	//2019/9/26
        Prop.Append(stb, "   CASE WHEN C.SCHOOL_TYPE IS NOT NULL THEN C.SCHOOL_TYPE ELSE '99' END AS SCHOOL_TYPE, ");
        Prop.Append(stb, "   B.RANK_CODE, ");
        Prop.Append(stb, "   COUNT(B.ID) AS APPLICATION_COUNT ");
        Prop.Append(stb, " FROM  ");
        Prop.Append(stb, "   ( ");
        Prop.Append(stb, getTrainingApplicationViewSql()); //研修申込者総数VIEW
        Prop.Append(stb, "   ) A ");
        Prop.Append(stb, "   LEFT JOIN SAF_TRAINING_APPLICATION B ");
        Prop.Append(stb, "          ON B.YEAR = A.YEAR ");
        Prop.Append(stb, "         AND B.TRAINING_TYPE_CODE = A.TRAINING_TYPE_CODE ");
        Prop.Append(stb, "         AND B.TRAINING_ID = A.TRAINING_ID ");
        Prop.Append(stb, "         AND B.DELETER_OFFICE NOT IN ('1','2')");	//削除フラグ追加
        Prop.Append(stb, "   LEFT JOIN (SELECT T1.SCHOOLID, ");
        //Prop.Append(stb, "                      CASE WHEN T1.SCHOOL_TYPE NOT IN ('01','02','03','04','05') THEN '99' ");
        Prop.Append(stb, "                      CASE WHEN T1.SCHOOL_TYPE NOT IN ('01','02','03','04') THEN '99' ");
        Prop.Append(stb, "                           ELSE T1.SCHOOL_TYPE END AS SCHOOL_TYPE ");
        Prop.Append(stb, "                FROM ( ");
        Prop.Append(stb, getSchoolViewSql()); //学校VIEW
        Prop.Append(stb, "                     ) T1 ");
        Prop.Append(stb, "              ) C ON C.SCHOOLID = B.BELONG_SCHOOLID ");
        Prop.Append(stb, "   LEFT JOIN SAF_CODE_MASTER D ");
        Prop.Append(stb, "          ON D.CODE    = C.SCHOOL_TYPE ");
        Prop.Append(stb, "         AND D.CODE_ID = '0002'    ");
        Prop.Append(stb, "   LEFT JOIN  SAF_JOB_MST E ");
        Prop.Append(stb, "          ON E.JOBCD = B.RANK_CODE ");
        Prop.Append(stb, " WHERE ");
        Prop.Append(stb, "   A.YEAR = '"+ _ctrlYear +"' ");
        Prop.Append(stb, " GROUP BY ");
        Prop.Append(stb, "   A.TRAINING_TYPE_CODE, ");
        Prop.Append(stb, "   A.TRAINING_TYPE_FULLNAME, ");
        Prop.Append(stb, "   A.DISPLAY_ORDER, ");
        Prop.Append(stb, "   A.TRAINING_ID, ");
        Prop.Append(stb, "   A.TRAINING_NO, ");
        Prop.Append(stb, "   A.TRAINING_NAME, ");
        Prop.Append(stb, "   A.CAPACITY, ");
        Prop.Append(stb, "   A.SCHOOL_NAME1, ");
        Prop.Append(stb, "   A.SCHOOL_NAME2, ");
        Prop.Append(stb, "   A.SCHOOL_NAME3, ");
        Prop.Append(stb, "   A.SCHOOL_NAME4, ");
        Prop.Append(stb, "   A.SCHOOL_NAME5, ");
        Prop.Append(stb, "   A.COUNT, ");
        Prop.Append(stb, "   C.SCHOOL_TYPE, ");
        Prop.Append(stb, "   B.RANK_CODE, ");
        Prop.Append(stb, "   D.ITEM2, ");
        Prop.Append(stb, "   E.JOBCD ");
        Prop.Append(stb, " ORDER BY ");
        Prop.Append(stb, "   A.DISPLAY_ORDER, ");
        Prop.Append(stb, "   A.TRAINING_NO, ");
        Prop.Append(stb, "   D.ITEM2, ");
        Prop.Append(stb, "   E.JOBCD ");

        return stb.toString();
    }

	private class PrintDataBody {
		final String _trainingTypeName;
		final String _trainingNo;
		final String _trainingName;
		final int _capacity;
		final String _schoolName;
		final int _count;
		final int _applicationCount;
		final int _columnPos;
		//final List<BodyDetail> _detail = new ArrayList<>();

	    public PrintDataBody (final String trainingTypeName, final String trainingNo, final String trainingName, final int capacity
	    		, final String schoolName, final int count, final int applicationCount, final int columnPos)
	    {
	    	_trainingTypeName = trainingTypeName;
			_trainingNo = trainingNo;
			_trainingName = trainingName;
			_capacity = capacity;
			_schoolName = schoolName;
			_count = count;
			_applicationCount = applicationCount;
			_columnPos = columnPos;
	    }
	}

	/*private class BodyDetail {
		final String _schoolType;
		final String _rankCode;
		final String _applicationCount;

	    public BodyDetail (final String schoolType, final String rankCode, final String applicationCount)
	    {
			_schoolType = schoolType;
			_rankCode = rankCode;
			_applicationCount = applicationCount;
	    }
	}*/

}

