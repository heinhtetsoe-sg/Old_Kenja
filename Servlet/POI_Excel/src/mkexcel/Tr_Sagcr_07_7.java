package mkexcel;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
//年度別の本数・日数・受講者数
//
public class Tr_Sagcr_07_7 {
	Log log = LogFactory.getLog(Tr_Sagcr_07_7.class);
	// メンバー変数
	private Workbook book;
	private final static String USE_SHEETNAME = "年度別の本数・日数・受講者数";
	private Alp_Properties Prop;
	private String errorMessage = "";
	final static int MAX_LINE = 20;
	// 書込みデータ
	String _year = "";
	String _syear = "";

	public Tr_Sagcr_07_7(Workbook book, String information, Alp_Properties prop) {
		this.book = book;
		this.Prop = prop;
		String[] Information = information.split(",");
		if (Information.length < 1) {
			errorMessage = String.format("パラメータエラー{%s}", information);
		} else {
			_year = Information[0];  //★yearは上位からパラメータでわたってくる。
//			_year = "2019"; //★デバッグで仮設定
			_syear = String.valueOf(Integer.parseInt(_year) - 4); //★年度-4
			//名前を付けて保存ファイル名を変更
			Calendar cl = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
			String fileName = Prop.getOutputName() + "_" + String.valueOf(_year) + "年度_" + sdf.format(cl.getTime());
			Prop.setOutputName(fileName);
		}
		log.info("Tr_Sagcr_07_7 コンストラクタ パラメータ{" + information + "}");
		//log.warn("$Revision: 72437 $");
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
		stmt = dao.open(JDBC_LOOKUP);
		if (stmt == null) {
			// オープン失敗
			errorMessage = "データベース接続失敗";
			log.error("dao.open() error");
			return false;
		}
		//メイン処理
		String sql = getCountDataSql();
		try {
			log.info("SQL:" + sql);
			List<PrintData> DatList = new ArrayList<PrintData>();
			rs = dao.query(sql);
			errorMessage = dao.getErrTrace();
			if (errorMessage.length() > 0) { // sql例外発生
				return false;
			}
			while(rs.next()) {
				final String Year = rs.getString("YEAR");
				final int Hon_Kihon = rs.getInt("HON_KIHON");
				final int Hon_Senmon = rs.getInt("HON_SENMON");
				final int Hon_Tokubetu = rs.getInt("HON_TOKUBETU");
				final int Day_Kihon = rs.getInt("DAY_KIHON");
				final int Day_Senmon = rs.getInt("DAY_SENMON");
				final int Day_Tokubetu = rs.getInt("DAY_TOKUBETU");
				final int Jukou_Kihon = rs.getInt("JUKOU_KIHON");
				final int Jukou_Senmon = rs.getInt("JUKOU_SENMON");
				final int Jukou_Tokubetu = rs.getInt("JUKOU_TOKUBETU");
				final int Jukou_Nobe_Kihon = rs.getInt("JUKOU_NOBE_KIHON");
				final int Jukou_Nobe_Senmon = rs.getInt("JUKOU_NOBE_SENMON");
				final int Jukou_Nobe_Tokubetu = rs.getInt("JUKOU_NOBE_TOKUBETU");
				final BigDecimal Jukou_Ritu_Kihon = rs.getBigDecimal("JUKOU_RITU_KIHON");
				final BigDecimal Jukou_Ritu_Senmon = rs.getBigDecimal("JUKOU_RITU_SENMON");
				final BigDecimal Jukou_Ritu_Tokubetu = rs.getBigDecimal("JUKOU_RITU_TOKUBETU");
				PrintData addwk = new PrintData(Year,Hon_Kihon,Hon_Senmon,Hon_Tokubetu,Day_Kihon,Day_Senmon,Day_Tokubetu,Jukou_Kihon,Jukou_Senmon,Jukou_Tokubetu,Jukou_Nobe_Kihon,Jukou_Nobe_Senmon,Jukou_Nobe_Tokubetu,Jukou_Ritu_Kihon,Jukou_Ritu_Senmon,Jukou_Ritu_Tokubetu);
				DatList.add(addwk);
				log.info("Year:" + Year + " count:" + rs.getInt("STAFF_CNT"));
			}
			returnCode = setRowsInfo(DatList);
		} catch (Exception e) {
			errorMessage = e.toString();
			dao.StackTraceLogging(e);
		} finally {
			//クローズ
			dao.close();
		}
		return returnCode;
	}
	private boolean setRowsInfo(final List<PrintData> DatList) {
		boolean retFlg = false;

		// 書込みシートを開く
		Sheet sheet = book.getSheet(USE_SHEETNAME);
		if (sheet == null) {
			errorMessage = "対象のシート見つからず";
			return retFlg;
		}
		SetColumn set = new SetColumn();
		set.setSheet(sheet);

		//"出力日：";
		Date date = new Date();
		SimpleDateFormat fmt1 = new SimpleDateFormat("yyyy/MM/dd");
		String dstrWk = "出力日：" + fmt1.format(date);
		set.setValueString(1, "F", dstrWk);

		//本数
		int dataidx = 0;
		int strtrow = 3;  //出力開始行-1(次列処理でリセット)
		int pos = 1; //出力開始位置
		for (PrintData outinfo : DatList) {

			//本数
			strtrow = 3;
			setDataRow(sheet.getRow(strtrow++), pos + dataidx, outinfo._Year);
			setIntRow(sheet.getRow(strtrow++), pos + dataidx, outinfo._Hon_Kihon);
			setIntRow(sheet.getRow(strtrow++), pos + dataidx, outinfo._Hon_Senmon);
			setIntRow(sheet.getRow(strtrow++), pos + dataidx, outinfo._Hon_Tokubetu);
			//日数
			strtrow = 10;
			setDataRow(sheet.getRow(strtrow++), pos + dataidx, outinfo._Year);
			setIntRow(sheet.getRow(strtrow++), pos + dataidx, outinfo._Day_Kihon);
			setIntRow(sheet.getRow(strtrow++), pos + dataidx, outinfo._Day_Senmon);
			setIntRow(sheet.getRow(strtrow++), pos + dataidx, outinfo._Day_Tokubetu);
			//受講者人数
			strtrow = 17;
			setDataRow(sheet.getRow(strtrow++), pos + dataidx, outinfo._Year);
			setIntRow(sheet.getRow(strtrow++), pos + dataidx, outinfo._Jukou_Kihon);
			setIntRow(sheet.getRow(strtrow++), pos + dataidx, outinfo._Jukou_Senmon);
			setIntRow(sheet.getRow(strtrow++), pos + dataidx, outinfo._Jukou_Tokubetu);
			//受講者延べ人数
			strtrow = 24;
			setDataRow(sheet.getRow(strtrow++), pos + dataidx, outinfo._Year);
			setIntRow(sheet.getRow(strtrow++), pos + dataidx, outinfo._Jukou_Nobe_Kihon);
			setIntRow(sheet.getRow(strtrow++), pos + dataidx, outinfo._Jukou_Nobe_Senmon);
			setIntRow(sheet.getRow(strtrow++), pos + dataidx, outinfo._Jukou_Nobe_Tokubetu);
			//受講率
			strtrow = 31;
			setDataRow(sheet.getRow(strtrow++), pos + dataidx, outinfo._Year);
			setDecimalRow(sheet.getRow(strtrow++), pos + dataidx, outinfo._Jukou_Ritu_Kihon);
			setDecimalRow(sheet.getRow(strtrow++), pos + dataidx, outinfo._Jukou_Ritu_Senmon);
			setDecimalRow(sheet.getRow(strtrow++), pos + dataidx, outinfo._Jukou_Ritu_Tokubetu);

		    dataidx++;
		    retFlg = true;
		}
		//算出セルを実行
		int rowStart = sheet.getFirstRowNum();
		int rowEnd = sheet.getLastRowNum();
		for(int i = rowStart; i <= rowEnd; i++) {
			Row row = sheet.getRow(i);
			if (row != null) {
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
		}
        return retFlg;
	}

	private void setDataRow(final Row row, final int pos, final String vals) {
		setData(row.getCell(pos), vals);
	}

	private void setData(final Cell cel, final String vals) {
		if (cel != null) {
			cel.setCellValue(vals);
		}
	}

	private void setIntRow(final Row row, final int pos, final int vals) {
		setInt(row.getCell(pos), vals);
	}

	private void setInt(final Cell cel, final int vals) {
		if (cel != null) {
			cel.setCellValue(vals);
		}
	}

	private void setDecimalRow(final Row row, final int pos, final BigDecimal vals) {
		setDecimal(row.getCell(pos), vals);
	}

	private void setDecimal(final Cell cel, BigDecimal vals) {
		if (cel != null) {
			double d = Double.parseDouble(vals.toString());
			cel.setCellValue(d);
		}
	}

	private String getCountDataSql() {
		StringBuffer stb = new StringBuffer();
		//5年分の年度、基準日
		Prop.Append(stb, " WITH T_YEAR (YEAR, BASE_DATE, SDATE, EDATE) AS ( ");
		for (int y = Integer.parseInt(_syear); y <= Integer.parseInt(_year); y++) {
			if (y > Integer.parseInt(_syear)) {
				Prop.Append(stb, "     UNION ALL ");
			}
			Prop.Append(stb, "     VALUES('" + String.valueOf(y) + "', '" + String.valueOf(y) + "-05-01', '" + String.valueOf(y) + "-04-01', '" + String.valueOf(y + 1) + "-03-31') ");
		}
		Prop.Append(stb, " ) ");
		//本数・日数
		Prop.Append(stb, " , T_HONDAY_CNT AS ( ");
		Prop.Append(stb, "     SELECT ");
		Prop.Append(stb, "         TI.YEAR, ");
		Prop.Append(stb, "         SUM(CASE WHEN CM.TRAINING_TYPE = '1' THEN 1 ELSE 0 END) AS HON_KIHON, ");
		Prop.Append(stb, "         SUM(CASE WHEN CM.TRAINING_TYPE IN ('2','3') THEN 1 ELSE 0 END) AS HON_SENMON, ");
		Prop.Append(stb, "         SUM(CASE WHEN CM.TRAINING_TYPE = '7' THEN 1 ELSE 0 END) AS HON_TOKUBETU, ");
		Prop.Append(stb, "         SUM(CASE WHEN CM.TRAINING_TYPE = '1' THEN TI.ENFORCEMENT_DAYS ELSE 0 END) AS DAY_KIHON, ");
		Prop.Append(stb, "         SUM(CASE WHEN CM.TRAINING_TYPE IN ('2','3') THEN TI.ENFORCEMENT_DAYS ELSE 0 END)  AS DAY_SENMON, ");
		Prop.Append(stb, "         SUM(CASE WHEN CM.TRAINING_TYPE = '7' THEN TI.ENFORCEMENT_DAYS ELSE 0 END) AS DAY_TOKUBETU ");
		Prop.Append(stb, "     FROM ");
		Prop.Append(stb, "         SAF_TRAINING_INFOMATION TI ");
		Prop.Append(stb, "         INNER JOIN SAF_TRAINING_TYPE CM ");
		Prop.Append(stb, "              ON CM.TRAINING_TYPE IN ('1', '2', '3', '7') ");
		Prop.Append(stb, "             AND CM.TRAINING_TYPE_CODE = TI.TRAINING_TYPE_CODE ");
		Prop.Append(stb, "             AND CM.YEAR = TI.YEAR ");
		Prop.Append(stb, "     WHERE ");
		Prop.Append(stb, "             TI.YEAR BETWEEN '" + _syear + "' AND '" + _year + "' ");
		Prop.Append(stb, "         AND TI.SPONSOR_SCHOOLID NOT IN (SELECT CODE FROM SAF_CODE_MASTER WHERE CODE_ID = '0102') ");
		Prop.Append(stb, "     GROUP BY ");
		Prop.Append(stb, "         TI.YEAR ");
		Prop.Append(stb, " ) ");
		//受講者人数・受講者延べ人数
		Prop.Append(stb, " , T_JUKOU_CNT AS ( ");
		Prop.Append(stb, "     SELECT ");
		Prop.Append(stb, "         TI.YEAR, ");
		Prop.Append(stb, "         COUNT(DISTINCT(CASE WHEN CM.TRAINING_TYPE = '1' AND AC.STAFFCD != 0 THEN AC.STAFFCD ELSE NULL END)) AS JUKOU_KIHON1, ");
		Prop.Append(stb, "         COUNT(CASE WHEN CM.TRAINING_TYPE = '1' AND AC.STAFFCD = 0 THEN AC.STAFFCD ELSE NULL END) AS JUKOU_KIHON2, ");
		Prop.Append(stb, "         COUNT(DISTINCT(CASE WHEN CM.TRAINING_TYPE IN ('2','3') AND AC.STAFFCD != 0 THEN AC.STAFFCD ELSE NULL END)) AS JUKOU_SENMON1, ");
		Prop.Append(stb, "         COUNT(CASE WHEN CM.TRAINING_TYPE IN ('2','3') AND AC.STAFFCD = 0 THEN AC.STAFFCD ELSE NULL END) AS JUKOU_SENMON2, ");
		Prop.Append(stb, "         COUNT(DISTINCT(CASE WHEN CM.TRAINING_TYPE = '7' AND AC.STAFFCD != 0 THEN AC.STAFFCD ELSE NULL END)) AS JUKOU_TOKUBETU1, ");
		Prop.Append(stb, "         COUNT(CASE WHEN CM.TRAINING_TYPE = '7' AND AC.STAFFCD = 0 THEN AC.STAFFCD ELSE NULL END) AS JUKOU_TOKUBETU2, ");
		Prop.Append(stb, "         SUM(CASE WHEN CM.TRAINING_TYPE = '1'  ");
		Prop.Append(stb, "                  THEN INT(VALUE(AC.DATE_1_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_2_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_3_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_4_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_5_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_6_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_7_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_8_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_9_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_10_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_11_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_12_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_13_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_14_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_15_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_16_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_17_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_18_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_19_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_20_ATTENDANCE,0)) ");
		Prop.Append(stb, "                  ELSE 0 END) AS JUKOU_NOBE_KIHON, ");
		Prop.Append(stb, "         SUM(CASE WHEN CM.TRAINING_TYPE IN ('2','3')  ");
		Prop.Append(stb, "                  THEN INT(VALUE(AC.DATE_1_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_2_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_3_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_4_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_5_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_6_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_7_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_8_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_9_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_10_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_11_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_12_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_13_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_14_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_15_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_16_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_17_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_18_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_19_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_20_ATTENDANCE,0)) ");
		Prop.Append(stb, "                  ELSE 0 END) AS JUKOU_NOBE_SENMON, ");
		Prop.Append(stb, "         SUM(CASE WHEN CM.TRAINING_TYPE = '7'  ");
		Prop.Append(stb, "                  THEN INT(VALUE(AC.DATE_1_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_2_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_3_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_4_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_5_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_6_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_7_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_8_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_9_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_10_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_11_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_12_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_13_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_14_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_15_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_16_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_17_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_18_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_19_ATTENDANCE,0)) +  ");
		Prop.Append(stb, "                       INT(VALUE(AC.DATE_20_ATTENDANCE,0)) ");
		Prop.Append(stb, "                  ELSE 0 END) AS JUKOU_NOBE_TOKUBETU ");
		Prop.Append(stb, "     FROM ");
		Prop.Append(stb, "         SAF_TRAINING_INFOMATION TI ");
		Prop.Append(stb, "         INNER JOIN SAF_TRAINING_TYPE CM ");
		Prop.Append(stb, "              ON CM.TRAINING_TYPE IN ('1', '2', '3', '7') ");
		Prop.Append(stb, "             AND CM.TRAINING_TYPE_CODE = TI.TRAINING_TYPE_CODE ");
		Prop.Append(stb, "             AND CM.YEAR = TI.YEAR ");
		Prop.Append(stb, "         INNER JOIN SAF_TRAINING_APPLICATION TA ");
		Prop.Append(stb, "              ON TA.YEAR = TI.YEAR ");
		Prop.Append(stb, "             AND TA.TRAINING_TYPE_CODE = TI.TRAINING_TYPE_CODE ");
		Prop.Append(stb, "             AND TA.TRAINING_ID = TI.TRAINING_ID ");
		Prop.Append(stb, "             AND TA.ATTENDING_LECTURE_FLAG = '1' ");
		Prop.Append(stb, "         INNER JOIN SAF_ATTENDANCE_CERTIFICATION AC ");
		Prop.Append(stb, "              ON AC.YEAR = TA.YEAR ");
		Prop.Append(stb, "             AND AC.TRAINING_TYPE_CODE = TA.TRAINING_TYPE_CODE ");
		Prop.Append(stb, "             AND AC.TRAINING_ID = TA.TRAINING_ID ");
		Prop.Append(stb, "             AND AC.ID = TA.ID ");
		Prop.Append(stb, "     WHERE ");
		Prop.Append(stb, "             TI.YEAR BETWEEN '" + _syear + "' AND '" + _year + "' ");
		Prop.Append(stb, "         AND TI.SPONSOR_SCHOOLID NOT IN (SELECT CODE FROM SAF_CODE_MASTER WHERE CODE_ID = '0102') ");
		Prop.Append(stb, "         AND TA.BELONG_SCHOOLID NOT IN (SELECT CODE FROM SAF_CODE_MASTER WHERE CODE_ID = '0102') ");
		Prop.Append(stb, "         AND (AC.DATE_1_ATTENDANCE = '1' OR AC.DATE_6_ATTENDANCE = '1' OR AC.DATE_11_ATTENDANCE = '1' OR AC.DATE_16_ATTENDANCE = '1' OR ");
		Prop.Append(stb, "              AC.DATE_2_ATTENDANCE = '1' OR AC.DATE_7_ATTENDANCE = '1' OR AC.DATE_12_ATTENDANCE = '1' OR AC.DATE_17_ATTENDANCE = '1' OR ");
		Prop.Append(stb, "              AC.DATE_3_ATTENDANCE = '1' OR AC.DATE_8_ATTENDANCE = '1' OR AC.DATE_13_ATTENDANCE = '1' OR AC.DATE_18_ATTENDANCE = '1' OR ");
		Prop.Append(stb, "              AC.DATE_4_ATTENDANCE = '1' OR AC.DATE_9_ATTENDANCE = '1' OR AC.DATE_14_ATTENDANCE = '1' OR AC.DATE_19_ATTENDANCE = '1' OR ");
		Prop.Append(stb, "              AC.DATE_5_ATTENDANCE = '1' OR AC.DATE_10_ATTENDANCE= '1' OR AC.DATE_15_ATTENDANCE = '1' OR AC.DATE_20_ATTENDANCE = '1') ");
		Prop.Append(stb, "     GROUP BY ");
		Prop.Append(stb, "         TI.YEAR ");
		Prop.Append(stb, " ) ");
		//教職員数
		Prop.Append(stb, " , T_STAFF_CNT AS ( ");
		/*Prop.Append(stb, "     SELECT ");
		Prop.Append(stb, "         A.YEAR, ");
		Prop.Append(stb, "         COUNT(DISTINCT SA.STAFFCD) AS STAFF_CNT ");
		Prop.Append(stb, "     FROM ");
		Prop.Append(stb, "         T_YEAR A, ");
		Prop.Append(stb, "         STAFF_WORK_HIST_DAT SA ");
		//					2020/02/10
		Prop.Append(stb, "     INNER JOIN V_STAFF SB ON SB.STAFFCD = SA.STAFFCD AND ((A.BASE_DATE > SA.FROM_DATE AND SA.TO_DATE IS NULL)");
		Prop.Append(stb, "         OR A.BASE_DATE BETWEEN SA.FROM_DATE AND SA.TO_DATE)");
		//Prop.Append(stb, "     INNER JOIN SAF_STAFF_EXTRA_G_DAT S ON S.STAFFCD = SA.STAFFCD ");
		Prop.Append(stb, "     WHERE ");
		//Prop.Append(stb, "         S.BONUS_WORKER_FLAG='0' AND SB.ADDITIONAL_POST_FLAG='1' AND SB.RANK_CODE <> '812'");
		Prop.Append(stb, "         SB.ADDITIONAL_POST_FLAG='1' AND SB.RANK_CODE <> '812'");*/
		Prop.Append(stb, "     SELECT ");
		Prop.Append(stb, "         A.YEAR, ");
		Prop.Append(stb, "         COUNT(DISTINCT SA.STAFFCD) AS STAFF_CNT ");
		Prop.Append(stb, "     FROM ");
		Prop.Append(stb, "         T_YEAR A, ");
		Prop.Append(stb, "         SAF_STAFF_JOB_G_DAT SA, ");
		Prop.Append(stb, "        (");
		Prop.Append(stb, "        SELECT DISTINCT ");
		Prop.Append(stb, "            GD.SCHOOLID ");
		Prop.Append(stb, "          , TT.YEAR ");
		Prop.Append(stb, "        FROM");
		Prop.Append(stb, "            SAF_SCHOOLHIST_G_DAT GD");
		Prop.Append(stb, "            INNER JOIN (SELECT");
		Prop.Append(stb, "                                        T1.SCHOOLID,");
		Prop.Append(stb, "                                        MAX(T1.SCHSTART) AS SCHSTART, ");
		Prop.Append(stb, "                                        Y2.YEAR ");
		Prop.Append(stb, "                                    FROM");
		Prop.Append(stb, "                                        SAF_SCHOOLHIST_G_DAT T1");
		Prop.Append(stb, "                                     ,  T_YEAR Y2 ");
		Prop.Append(stb, "                                    WHERE");
		Prop.Append(stb, "                                        T1.SCHSTART <= Y2.EDATE AND");
		Prop.Append(stb, "                                        value(T1.SCHEND, '9999-03-31') >= Y2.SDATE ");
		Prop.Append(stb, "                                    GROUP BY");
		Prop.Append(stb, "                                        T1.SCHOOLID,");
		Prop.Append(stb, "                                        Y2.YEAR");
		Prop.Append(stb, "                                    ) TT ON ");
		Prop.Append(stb, "           TT.SCHOOLID = GD.SCHOOLID ");
		Prop.Append(stb, "       AND TT.SCHSTART = GD.SCHSTART ");
		Prop.Append(stb, "            LEFT JOIN SAF_SCHOOL_EXTRA_G_DAT D ON D.SCHOOLID = GD.SCHOOLID ");
		Prop.Append(stb, "        WHERE ");
		Prop.Append(stb, "           GD.SCHOOLID NOT IN (SELECT CODE FROM SAF_CODE_MASTER WHERE CODE_ID = '0102') ");
		Prop.Append(stb, "       AND D.FACULTY_FLAG = '0' ");
		Prop.Append(stb, "        ) B");
		Prop.Append(stb, "     WHERE ");
        Prop.Append(stb, "         A.BASE_DATE >= SA.FROM_DATE");
        Prop.Append(stb, "         AND (SA.TO_DATE IS NULL");
        Prop.Append(stb, "         OR A.BASE_DATE <= SA.TO_DATE)");
        Prop.Append(stb, "         AND SA.BONUS_WORKER_FLAG = '0'");
        Prop.Append(stb, "         AND SA.PRIORITY= '1'");
        Prop.Append(stb, "         AND SA.JOBCD <> '812'");
        Prop.Append(stb, "         AND B.SCHOOLID = SA.FROM_SCHOOLCD ");
        Prop.Append(stb, "         AND B.YEAR = A.YEAR ");
		Prop.Append(stb, "     GROUP BY ");
		Prop.Append(stb, "         A.YEAR ");
		Prop.Append(stb, " ) ");
		//メイン
		Prop.Append(stb, " , T_MAIN AS ( ");
		Prop.Append(stb, "     SELECT ");
		Prop.Append(stb, "         T0.YEAR, ");
		Prop.Append(stb, "         VALUE(T1.HON_KIHON, 0) AS HON_KIHON, ");
		Prop.Append(stb, "         VALUE(T1.HON_SENMON, 0) AS HON_SENMON, ");
		Prop.Append(stb, "         VALUE(T1.HON_TOKUBETU, 0) AS HON_TOKUBETU, ");
		Prop.Append(stb, "         VALUE(T1.DAY_KIHON, 0) AS DAY_KIHON, ");
		Prop.Append(stb, "         VALUE(T1.DAY_SENMON, 0) AS DAY_SENMON, ");
		Prop.Append(stb, "         VALUE(T1.DAY_TOKUBETU, 0) AS DAY_TOKUBETU, ");
		Prop.Append(stb, "         VALUE(T2.JUKOU_KIHON1, 0) + VALUE(T2.JUKOU_KIHON2, 0) AS JUKOU_KIHON, ");
		Prop.Append(stb, "         VALUE(T2.JUKOU_SENMON1, 0) + VALUE(T2.JUKOU_SENMON2, 0) AS JUKOU_SENMON, ");
		Prop.Append(stb, "         VALUE(T2.JUKOU_TOKUBETU1, 0) + VALUE(T2.JUKOU_TOKUBETU2, 0) AS JUKOU_TOKUBETU, ");
		Prop.Append(stb, "         VALUE(T2.JUKOU_NOBE_KIHON, 0) AS JUKOU_NOBE_KIHON, ");
		Prop.Append(stb, "         VALUE(T2.JUKOU_NOBE_SENMON, 0) AS JUKOU_NOBE_SENMON, ");
		Prop.Append(stb, "         VALUE(T2.JUKOU_NOBE_TOKUBETU, 0) AS JUKOU_NOBE_TOKUBETU, ");
		Prop.Append(stb, "         VALUE(T3.STAFF_CNT, 0) AS STAFF_CNT ");
		Prop.Append(stb, "     FROM ");
		Prop.Append(stb, "         T_YEAR T0 ");
		Prop.Append(stb, "         LEFT JOIN T_HONDAY_CNT T1 ON T1.YEAR = T0.YEAR ");
		Prop.Append(stb, "         LEFT JOIN T_JUKOU_CNT T2 ON T2.YEAR = T0.YEAR ");
		Prop.Append(stb, "         LEFT JOIN T_STAFF_CNT T3 ON T3.YEAR = T0.YEAR ");
		Prop.Append(stb, "     ORDER BY ");
		Prop.Append(stb, "         T0.YEAR ");
		Prop.Append(stb, " ) ");

		Prop.Append(stb, " SELECT ");
		Prop.Append(stb, "     TM.STAFF_CNT, ");
		Prop.Append(stb, "     TM.YEAR, ");
		Prop.Append(stb, "     TM.HON_KIHON, ");
		Prop.Append(stb, "     TM.HON_SENMON, ");
		Prop.Append(stb, "     TM.HON_TOKUBETU, ");
		Prop.Append(stb, "     TM.DAY_KIHON, ");
		Prop.Append(stb, "     TM.DAY_SENMON, ");
		Prop.Append(stb, "     TM.DAY_TOKUBETU, ");
		Prop.Append(stb, "     TM.JUKOU_KIHON, ");
		Prop.Append(stb, "     TM.JUKOU_SENMON, ");
		Prop.Append(stb, "     TM.JUKOU_TOKUBETU, ");
		Prop.Append(stb, "     TM.JUKOU_NOBE_KIHON, ");
		Prop.Append(stb, "     TM.JUKOU_NOBE_SENMON, ");
		Prop.Append(stb, "     TM.JUKOU_NOBE_TOKUBETU, ");
		//受講率
		Prop.Append(stb, "     CASE WHEN TM.STAFF_CNT > 0 ");
		Prop.Append(stb, "          THEN DECIMAL(ROUND(FLOAT(TM.JUKOU_KIHON)*100/TM.STAFF_CNT*10,0)/10,6,1) ");
		Prop.Append(stb, "          ELSE 0.0 END AS JUKOU_RITU_KIHON, ");
		Prop.Append(stb, "     CASE WHEN TM.STAFF_CNT > 0 ");
		Prop.Append(stb, "          THEN DECIMAL(ROUND(FLOAT(TM.JUKOU_SENMON)*100/TM.STAFF_CNT*10,0)/10,6,1) ");
		Prop.Append(stb, "          ELSE 0.0 END AS JUKOU_RITU_SENMON, ");
		Prop.Append(stb, "     CASE WHEN TM.STAFF_CNT > 0 ");
		Prop.Append(stb, "          THEN DECIMAL(ROUND(FLOAT(TM.JUKOU_TOKUBETU)*100/TM.STAFF_CNT*10,0)/10,6,1) ");
		Prop.Append(stb, "          ELSE 0.0 END AS JUKOU_RITU_TOKUBETU ");
		Prop.Append(stb, " FROM ");
		Prop.Append(stb, "     T_MAIN TM ");
		Prop.Append(stb, " ORDER BY ");
		Prop.Append(stb, "     TM.YEAR ");
		return stb.toString();
	}

	private class PrintData {
		final String _Year;
		final int _Hon_Kihon;
		final int _Hon_Senmon;
		final int _Hon_Tokubetu;
		final int _Day_Kihon;
		final int _Day_Senmon;
		final int _Day_Tokubetu;
		final int _Jukou_Kihon;
		final int _Jukou_Senmon;
		final int _Jukou_Tokubetu;
		final int _Jukou_Nobe_Kihon;
		final int _Jukou_Nobe_Senmon;
		final int _Jukou_Nobe_Tokubetu;
		final BigDecimal _Jukou_Ritu_Kihon;
		final BigDecimal _Jukou_Ritu_Senmon;
		final BigDecimal _Jukou_Ritu_Tokubetu;
	    public PrintData (final String Year,final int Hon_Kihon,final int Hon_Senmon,final int Hon_Tokubetu,final int Day_Kihon,final int Day_Senmon,final int Day_Tokubetu,final int Jukou_Kihon,final int Jukou_Senmon,final int Jukou_Tokubetu,final int Jukou_Nobe_Kihon,final int Jukou_Nobe_Senmon,final int Jukou_Nobe_Tokubetu,final BigDecimal Jukou_Ritu_Kihon,final BigDecimal Jukou_Ritu_Senmon,final BigDecimal Jukou_Ritu_Tokubetu)
	    {
	    	_Year = Year;
	    	_Hon_Kihon = Hon_Kihon;
	    	_Hon_Senmon = Hon_Senmon;
	    	_Hon_Tokubetu = Hon_Tokubetu;
	    	_Day_Kihon = Day_Kihon;
	    	_Day_Senmon = Day_Senmon;
	    	_Day_Tokubetu = Day_Tokubetu;
	    	_Jukou_Kihon = Jukou_Kihon;
	    	_Jukou_Senmon = Jukou_Senmon;
	    	_Jukou_Tokubetu = Jukou_Tokubetu;
	    	_Jukou_Nobe_Kihon = Jukou_Nobe_Kihon;
	    	_Jukou_Nobe_Senmon = Jukou_Nobe_Senmon;
	    	_Jukou_Nobe_Tokubetu = Jukou_Nobe_Tokubetu;
	    	_Jukou_Ritu_Kihon = Jukou_Ritu_Kihon;
	    	_Jukou_Ritu_Senmon = Jukou_Ritu_Senmon;
	    	_Jukou_Ritu_Tokubetu = Jukou_Ritu_Tokubetu;
	    }
	}
}
