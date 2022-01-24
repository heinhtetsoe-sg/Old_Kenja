package mkexcel;

import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import util.Alp_Properties;

public class Sample {
	Log log = LogFactory.getLog(Sample.class);
	// メンバー変数
	private Workbook book;
	private String Information;
	private Alp_Properties Prop;
	private String errorMessage = "";

	public Sample(Workbook book, String information, Alp_Properties prop) {
		// TODO 自動生成されたコンストラクター・スタブ
		this.book = book;
		this.Information = information;
		this.Prop = prop;
	}

	public Boolean excel() {
		try {
			connectTest();
		} catch (Exception e) {
			
		}
		String[] propcols = Information.split(",", 0);
		org.apache.poi.ss.usermodel.Sheet sheet = book.getSheetAt(0);
		int lineCount = 0;
		int propcolslength =  propcols.length;
		for(Row row : sheet) {	//この方法ではrowがnullで抜ける
			if ((lineCount > 0) && (lineCount <= propcolslength)) {
				Cell cel = row.getCell(3);
				cel.setCellValue(propcols[lineCount - 1]);
			}
			lineCount++;
			if ((lineCount >= 2) && (lineCount <= 8)) {
				Cell cell = row.getCell(4);
                if (cell != null) {
                	if (cell.getCellType() == CellType.FORMULA) {
                		String str=cell.getCellFormula();
                		log.info("FORMULA：" + str);
                		cell.setCellFormula(str);
                	}
                }
			}
		}
		log.info("行数：" + lineCount);
		if (propcolslength >= 7) {
			Row row = sheet.getRow(9);
			Cell cel = row.getCell(4);
			log.info("今の値{" + cel.getStringCellValue() + "} スタイル：" + cel.getCellType());
			row.getCell(4).setCellValue(propcols[6]);
		}
		return true;
	}

	private boolean connectTest() throws Exception {
		//データベースリソース
		final String JDBC_LOOKUP = Prop.getProperty("jdbc.lookup");

		// フィールドデータをデータベースから取出す
		Statement stmt = null;
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
		// 日付と天気、学校名
		String sqlT = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'A002' ";
		try (ResultSet rs = dao.query(sqlT)) {
			errorMessage = dao.getErrTrace();
			if (errorMessage.length() > 0) { // sql例外発生
				return false;
			}
			while (rs.next()) {
				final String name1 = rs.getString("NAME1");
				log.info(" name1 = " + name1);
			}
		}
		dao.close();
		return true;
	}

	//エラーメッセージ取出し
	public String getErrorMessage() {
		return errorMessage;
	}

}
