// kanji=漢字
/*
 * $Id: 10b34dd784c3ff0bedd3d665994c9ccebc0b7be2 $
 *
 * 作成日: 2011/03/03 11:12:49 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;

import servletpack.KNJZ.detail.AbstractXls;
import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: 10b34dd784c3ff0bedd3d665994c9ccebc0b7be2 $
 */
public class KNJE152 extends AbstractXls {

    private static final Log log = LogFactory.getLog("KNJE152.class");
    
    private boolean _hasData;
    
    private static String SEISEKIDATA = "1";
    private static String KAMOKUSEISEKI = "1";
    
    private int _maxRecord = 0;

    Param _param;

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void xls_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        _param = createParam(_db2, request);

        //テンプレートの読込&初期化
        setIniTmpBook(_param._templatePath);

        //出力データ取得
        _dataList = getXlsDataList();

        //ヘッダデータ取得
        _headList = getHeadData();

        outPutXls(response, _param._header);
    }

    /** XLSデータ出力 */
    protected void outPutXls(final HttpServletResponse response, final boolean header) throws IOException {
        //出力用のシート
        HSSFSheet outPutSheet = _tmpBook.getSheetAt(1);
        HSSFRow setRow;

        //ヘッダの行の書式を使用する為
        HSSFRow headerRow = outPutSheet.getRow(0);
        setRow = outPutSheet.getRow(0);
        int hedCol = 0;
        for (final Iterator itHead = _headList.iterator(); itHead.hasNext();) {
            final String setXlsHedData = (String) itHead.next();
            setCellData(setRow, headerRow, hedCol++, setXlsHedData);
        }

        //最初の行の書式を使用する為
        HSSFRow firstRow = null;
        int line = 0;
        for (final Iterator iter = _dataList.iterator(); iter.hasNext();) {
            final List xlsData = (List) iter.next();
            final int rowLine = header ? line + 1 : line;
            setRow = outPutSheet.getRow(rowLine);
            if (line == 0) {
                final int firstrowLine = header ? rowLine + 1 : rowLine;
                firstRow = outPutSheet.getRow(firstrowLine);
                if (null == firstRow) {
                    firstRow = outPutSheet.createRow(firstrowLine);
                }
                if (firstRow.getCell((short) 0) == null) {
                    firstRow.createCell((short) 0);
                }
            }
            if (setRow == null) {
                setRow = outPutSheet.createRow(rowLine);
            }
            int col = 0;
            for (final Iterator itXlsData = xlsData.iterator(); itXlsData.hasNext();) {
                final String setXlsData = (String) itXlsData.next();
                setCellData(setRow, firstRow, col++, setXlsData);
            }
            line++;
        }
        //送信
        response.setHeader("Content-Disposition", "inline;filename=noufu_0.xls");
        response.setContentType("application/vnd.ms-excel");
        _tmpBook.write(response.getOutputStream());
    }

    protected List getXlsDataList() throws SQLException {
        final String sql = getSql();
        PreparedStatement psXls = null;
        ResultSet rsXls = null;
        final Map setdataMap = new HashMap();
        final List studentList = new ArrayList();
        try {
            psXls = _db2.prepareStatement(sql);
            rsXls = psXls.executeQuery();
            while (rsXls.next()) {
                final String schregno = rsXls.getString("SCHREGNO");
                Student student = null;
                //科目別成績詳細情報で1人複数行の時以外
                if (SEISEKIDATA.equals(_param._excelkind) || KAMOKUSEISEKI.equals(_param._output)) {
                	if (0 != studentList.size()) {
	                	for (final ListIterator it = studentList.listIterator(studentList.size()); it.hasPrevious();) {
	                    	final Student s = (Student) it.previous();
	                        if (null != s._schregno && s._schregno.equals(schregno)) {
	                            student = s;
	                            break;
	                        }
	                    }
	                }
	            }
                if (null == student) {
                    student = new Student(schregno);
                    studentList.add(student);
                }
    	        
                final Map dataMap = new HashMap(); 
                final ResultSetMetaData meta = rsXls.getMetaData();
                String flg = "";
                String abroad_year = "";
                String abroad_credits = "";
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                	String column = meta.getColumnName(i);
                    String value = rsXls.getString(column);
                    //科目別成績詳細情報で1人複数行の時
                    if (!SEISEKIDATA.equals(_param._excelkind) && !KAMOKUSEISEKI.equals(_param._output)) {
                    	if (column.equals("YEAR") && "9999".equals(value)) {
                    		value = "";
                    		flg = "1";
                    	}
                    	if (column.equals("VALUATION") && flg.equals("1")) {
                			value = "";
                    	}
                		if (column.equals("ADD_CREDIT") && flg.equals("1")) {
                			value = "";
                		}
                	//科目別成績詳細情報で1人1行の時
                    } else if (!SEISEKIDATA.equals(_param._excelkind) && KAMOKUSEISEKI.equals(_param._output)) {
                    	if (column.equals("YEAR") && "9999".equals(value)) {
                    		abroad_year = value;
                    		flg = "1";
                    	}
                		if (column.equals("GET_CREDIT") && flg.equals("1")) {
                			abroad_credits = value;
                		}
                    }
                    dataMap.put(column, value);
                }
                //科目別成績詳細情報で1人1行でYEAR='9999'の時、最後に留学単位数をセットするのみ
                if (!SEISEKIDATA.equals(_param._excelkind) && KAMOKUSEISEKI.equals(_param._output) && "9999".equals(abroad_year)) {
                	final String setschregno = rsXls.getString("SCHREGNO");
                	setdataMap.put("SCHREGNO", setschregno);
                	setdataMap.put("ABROAD_CREDITS", abroad_credits);
                } else {
                	student._record.add(dataMap);
                }
            }
        } catch (Exception e) {
            log.error("Exception!", e);
        } finally {
            DbUtils.closeQuietly(null, psXls, rsXls);
            _db2.commit();
        }
        
        _maxRecord = 0;
        final List dataList = new ArrayList();
        try {
            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                final List xlsData = new ArrayList();
                _maxRecord = Math.max(_maxRecord, student._record.size());
                
                final String[] cols1 = getCols1();
                final Map dataMap0 = (Map) student._record.get(0);
                for (int i = 0; i < cols1.length; i++) {
                    final String val = (String) dataMap0.get(cols1[i]);
                    xlsData.add(val);
                }
                final String[] cols2 = getCols2();
                for (int i = 0; i < student._record.size(); i++) {
                    final Map dataMap = (Map) student._record.get(i);
                    for (int j = 0; j < cols2.length; j++) {
                        final String val = (String) dataMap.get(cols2[j]);
                        xlsData.add(val);
                    }
                }
                //科目別成績詳細情報で1人1行の時、留学単位数追加
                if (!SEISEKIDATA.equals(_param._excelkind) && KAMOKUSEISEKI.equals(_param._output)) {
                	if(student._schregno.equals(setdataMap.get("SCHREGNO"))) {
                		xlsData.add(setdataMap.get("ABROAD_CREDITS"));
                	}
                }
                dataList.add(xlsData);
            }
        } catch (Exception e) {
            log.error("Exception!", e);
        }
        return dataList;
    }

    protected List getHeadData() {
        final List retList = new ArrayList();
        retList.add("学年");
        retList.add("組");
        retList.add("出席番号");
        retList.add("氏名");
        retList.add("学籍番号");
        //1.基本情報
        if (SEISEKIDATA.equals(_param._excelkind)) {
        	//ヘッダー情報をSQLで取得
        	final String headsql = getHeaderSql();
            PreparedStatement psXls = null;
            ResultSet rsXls = null;
            try {
                psXls = _db2.prepareStatement(headsql);
                rsXls = psXls.executeQuery();
                while (rsXls.next())
	            retList.add(rsXls.getString("CLASSNAME"));
            } catch (Exception e) {
                log.error("Exception!", e);
            } finally {
                DbUtils.closeQuietly(null, psXls, rsXls);
                _db2.commit();
            }
        	retList.add("全体");
        //2.科目別成績詳細
	    } else {
	    	//一人一行
	        if (KAMOKUSEISEKI.equals(_param._output)) {
	            for (int i = 0; i < _maxRecord; i++) {
	                retList.add("年度");
	                retList.add("科目名");
	                retList.add("評定");
	                retList.add("修得単位数");
	                retList.add("増加単位数");
	                retList.add("履修単位数");
	            }
	            retList.add("留学単位数");
	        //一人複数行
	        } else {
		        retList.add("年度");
		        if ("1".equals(_param._useCurriculumcd)) {	
	                retList.add("教科コード");
	                retList.add("学校校種");
	                retList.add("教育課程コード");
		        }
	            retList.add("科目コード");
	            retList.add("科目名");
	            retList.add("評定");
	            retList.add("修得単位数");
	            retList.add("増加単位数");
	            retList.add("履修単位数");
	        }	    	
	    }
        return retList;
    }

    protected String[] getCols() {
        return null;
    }

    protected String[] getCols1() {
        final String[] cols1 = new String[]{
                "GRADE",
                "HR_CLASS",
                "ATTENDNO",
                "NAME_SHOW",
                "SCHREGNO",
        };
        return cols1;
    }

    protected String[] getCols2() {
        final String[] cols2;
        //1.基本情報
        if (SEISEKIDATA.equals(_param._excelkind)) {
        	cols2 = new String[]{
        			"VALUATION",};        	
        //2.科目別成績詳細
	    } else {
	    	//一人一行
	        if (KAMOKUSEISEKI.equals(_param._output)) {
	        	cols2 = new String[]{
	        			"YEAR",
	        			"SUBCLASSNAME",
	        			"VALUATION",
	        			"GET_CREDIT",
	        			"ADD_CREDIT",
	        			"COMP_CREDIT"};
	        			//"ABROAD_CREDITS"};
	        //一人複数行
	        } else {
	        	if ("1".equals(_param._useCurriculumcd)) {
		            cols2 = new String[]{
		                    "YEAR",
		                    "CLASSCD",
		                    "SCHOOL_KIND",
		                    "CURRICULUM_CD",
		                    "SUBCLASSCD",
		                    "SUBCLASSNAME",
		                    "VALUATION",
		                    "GET_CREDIT",
		                    "ADD_CREDIT",
		                    "COMP_CREDIT",};
	        	} else {
		            cols2 = new String[]{
		                    "YEAR",
		                    "SUBCLASSCD",
		                    "SUBCLASSNAME",
		                    "VALUATION",
		                    "GET_CREDIT",
		                    "ADD_CREDIT",
		                    "COMP_CREDIT",};
	        	}
	        }
        }
        return cols2;
    }
    
    protected String getSql() {
        final String sql;
        //1.基本情報
        if (SEISEKIDATA.equals(_param._excelkind)) {
        	sql = getStudyrecSql();
        //2.科目別成績詳細
        } else {
	        sql = getStudyrec2Sql();
        }
        return sql;
    }
    
    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }
    
    private class Student {
        final String _schregno;
        final List _record = new ArrayList();
        
        public Student(final String schregno) {
            _schregno = schregno;
        }
    }

    /** パラメータクラス */
    private class Param {
    	final String _grade;
        final String _year;
        final String _gakki;
        final boolean _header;
        final String _templatePath;
        final String _excelkind;//1:基本情報　2:科目別成績詳細情報
        final String _useCurriculumcd;
        final String _output;//_excelkind=2の時のみ　1:1人1行、2:1人複数行

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _grade = request.getParameter("GRADE");
        	_year = request.getParameter("YEAR");
            _gakki = request.getParameter("CTRL_SEMESTER");
            _header = true;
            _templatePath = request.getParameter("TEMPLATE_PATH");
            _excelkind = request.getParameter("EXCEL_KIND");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _output = request.getParameter("OUTPUT");
        }
    }
    
    //基本情報取得時の教科情報（ヘッダー）取得
    private String getHeaderSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT DISTINCT ");
        stb.append("     T1.CLASSCD, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T1.SCHOOL_KIND, ");
        }
        stb.append("     CASE WHEN T2.CLASSORDERNAME1 IS NOT NULL THEN T2.CLASSORDERNAME1 ");
        stb.append("          WHEN T2.CLASSNAME IS NOT NULL THEN T2.CLASSNAME ");
        stb.append("          WHEN T3.CLASSORDERNAME1 IS NOT NULL THEN T3.CLASSORDERNAME1 ");
        stb.append("          ELSE T3.CLASSNAME END AS CLASSNAME, ");
        stb.append("     T2.SHOWORDER2 ");
        stb.append(" FROM ");
        stb.append("    (SELECT ");
        stb.append("         CLASSCD ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("        ,SCHOOL_KIND ");
        }
        stb.append("     FROM ");
        stb.append("         CLASS_MST ");
        stb.append("     UNION ");
        stb.append("     SELECT ");
        stb.append("         CLASSCD ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("        ,SCHOOL_KIND ");
        }
        stb.append("     FROM ");
        stb.append("         ANOTHER_CLASS_MST) T1 ");
        stb.append("     LEFT JOIN CLASS_MST T2 ON T1.CLASSCD = T2.CLASSCD ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("                           AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ");
        }
        stb.append("     LEFT JOIN ANOTHER_CLASS_MST T3 ON T1.CLASSCD = T3.CLASSCD ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("                                   AND T1.SCHOOL_KIND = T3.SCHOOL_KIND ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T2.SHOWORDER2, ");
        stb.append("     T1.CLASSCD ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     ,T1.SCHOOL_KIND ");
        }
        return stb.toString();
	}    
    
    //基本情報
    private String getStudyrecSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append("");
        stb.append(" WITH BASE AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T1.GRADE, ");
        stb.append("         T1.HR_CLASS, ");
        stb.append("         T1.ATTENDNO, ");
        stb.append("         T2.NAME_SHOW, ");
        stb.append("         T3.SCHOOLCD, ");
        stb.append("         T3.YEAR, ");
        stb.append("         T3.CLASSCD, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         T3.SCHOOL_KIND, ");
            stb.append("         T3.CURRICULUM_CD, ");
        }
        stb.append("         CASE WHEN L1.SUBCLASSCD2 IS NULL THEN T3.SUBCLASSCD ");
        stb.append("              ELSE L1.SUBCLASSCD2 END AS SUBCLASSCD, ");
        stb.append("         T3.VALUATION, ");
        stb.append("         CASE WHEN T3.GET_CREDIT = 0 THEN (CASE WHEN T3.COMP_CREDIT <> 0 THEN T3.COMP_CREDIT ");
        stb.append("                                                ELSE L3.CREDITS END) ");
        stb.append("              ELSE T3.GET_CREDIT END AS GET_CREDIT, ");
        stb.append("         T3.ADD_CREDIT, ");
        stb.append("         L2.GVAL_CALC ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT T1 ");
        stb.append("         INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("         LEFT JOIN SCHREG_STUDYREC_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("         LEFT JOIN SUBCLASS_MST L1 ON L1.SUBCLASSCD      = T3.SUBCLASSCD ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("                                  AND L1.CLASSCD         = T3.CLASSCD ");
            stb.append("                                  AND L1.SCHOOL_KIND     = T3.SCHOOL_KIND ");
            stb.append("                                  AND L1.CURRICULUM_CD   = T3.CURRICULUM_CD ");
        }
        stb.append("         LEFT JOIN SCHOOL_MST L2 ON L2.YEAR = T3.YEAR ");
        stb.append("         LEFT JOIN CREDIT_MST L3 ON L3.YEAR          = T3.YEAR ");
        stb.append("                                AND L3.COURSECD      = T1.COURSECD ");
        stb.append("                                AND L3.MAJORCD       = T1.MAJORCD ");
        stb.append("                                AND L3.GRADE         = T1.GRADE ");
        stb.append("                                AND L3.COURSECODE    = T1.COURSECODE ");
        stb.append("                                AND L3.SUBCLASSCD    = T3.SUBCLASSCD ");
        stb.append("                                AND L3.CLASSCD       = T3.CLASSCD ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("                                AND L3.SCHOOL_KIND   = T3.SCHOOL_KIND ");
            stb.append("                                AND L3.CURRICULUM_CD = T3.CURRICULUM_CD ");
        }
        stb.append("     WHERE ");
        stb.append("         T1.YEAR     = '" + _param._year + "' AND ");
        stb.append("         T1.SEMESTER = '" + _param._gakki + "' AND ");
        stb.append("         T1.GRADE    = '" + _param._grade + "' ");
        stb.append(" ), GVAL_CALC0 AS ( ");
        stb.append("     SELECT ");
        stb.append("         SCHREGNO, ");
        stb.append("         GRADE, ");
        stb.append("         HR_CLASS, ");
        stb.append("         ATTENDNO, ");
        stb.append("         NAME_SHOW, ");
        stb.append("         SCHOOLCD, ");
        stb.append("         YEAR, ");
        stb.append("         CLASSCD, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         SCHOOL_KIND, ");
            stb.append("         CURRICULUM_CD, ");
        }
        stb.append("         SUBCLASSCD, ");
        stb.append("         DECIMAL(ROUND(DECIMAL(AVG(FLOAT(VALUATION)),5,1),0),5,0) AS VALUATION ");
        stb.append("     FROM ");
        stb.append("         BASE ");
        stb.append("     WHERE ");
        stb.append("         SCHOOLCD    = '0' AND ");
        stb.append("         GVAL_CALC   = '0' ");
        stb.append("     GROUP BY ");
        stb.append("         SCHREGNO, ");
        stb.append("         GRADE, ");
        stb.append("         HR_CLASS, ");
        stb.append("         ATTENDNO, ");
        stb.append("         NAME_SHOW, ");
        stb.append("         SCHOOLCD, ");
        stb.append("         YEAR, ");
        stb.append("         CLASSCD, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         SCHOOL_KIND, ");
            stb.append("         CURRICULUM_CD, ");
        }
        stb.append("         SUBCLASSCD ");
        stb.append(" ), GVAL_CALC1 AS ( ");
        stb.append("     SELECT ");
        stb.append("         SCHREGNO, ");
        stb.append("         GRADE, ");
        stb.append("         HR_CLASS, ");
        stb.append("         ATTENDNO, ");
        stb.append("         NAME_SHOW, ");
        stb.append("         SCHOOLCD, ");
        stb.append("         YEAR, ");
        stb.append("         CLASSCD, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         SCHOOL_KIND, ");
            stb.append("         CURRICULUM_CD, ");
        }
        stb.append("         SUBCLASSCD, ");
        stb.append("         INTEGER(ROUND(DECIMAL(FLOAT(SUM(VALUATION * (CASE WHEN ADD_CREDIT IS NOT NULL THEN GET_CREDIT + ADD_CREDIT ELSE GET_CREDIT END))) / SUM((CASE WHEN ADD_CREDIT IS NOT NULL THEN GET_CREDIT + ADD_CREDIT ELSE GET_CREDIT END)),5,1),0)) AS VALUATION ");
        stb.append("     FROM ");
        stb.append("         BASE ");
        stb.append("     WHERE ");
        stb.append("         SCHOOLCD    = '0' AND ");
        stb.append("         GVAL_CALC   = '1' ");
        stb.append("     GROUP BY ");
        stb.append("         SCHREGNO, ");
        stb.append("         GRADE, ");
        stb.append("         HR_CLASS, ");
        stb.append("         ATTENDNO, ");
        stb.append("         NAME_SHOW, ");
        stb.append("         SCHOOLCD, ");
        stb.append("         YEAR, ");
        stb.append("         CLASSCD, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         SCHOOL_KIND, ");
            stb.append("         CURRICULUM_CD, ");
        }
        stb.append("         SUBCLASSCD ");
        stb.append(" ), GVAL_CALC2 AS ( ");
        stb.append("     SELECT ");
        stb.append("         SCHREGNO, ");
        stb.append("         GRADE, ");
        stb.append("         HR_CLASS, ");
        stb.append("         ATTENDNO, ");
        stb.append("         NAME_SHOW, ");
        stb.append("         SCHOOLCD, ");
        stb.append("         YEAR, ");
        stb.append("         CLASSCD, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         SCHOOL_KIND, ");
            stb.append("         CURRICULUM_CD, ");
        }
        stb.append("         SUBCLASSCD, ");
        stb.append("         MAX(VALUATION) AS VALUATION ");
        stb.append("     FROM ");
        stb.append("         BASE ");
        stb.append("     WHERE ");
        stb.append("         SCHOOLCD    = '0' AND ");
        stb.append("         GVAL_CALC   = '2' ");
        stb.append("     GROUP BY ");
        stb.append("         SCHREGNO, ");
        stb.append("         GRADE, ");
        stb.append("         HR_CLASS, ");
        stb.append("         ATTENDNO, ");
        stb.append("         NAME_SHOW, ");
        stb.append("         SCHOOLCD, ");
        stb.append("         YEAR, ");
        stb.append("         CLASSCD, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         SCHOOL_KIND, ");
            stb.append("         CURRICULUM_CD, ");
        }
        stb.append("         SUBCLASSCD ");
        //前籍校をGVAL_CALCごとにカウント
        stb.append(" ), ZENSEKI_CNT AS ( ");
        stb.append("     SELECT ");
        stb.append("         SCHREGNO, ");
        stb.append("         CLASSCD, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         SCHOOL_KIND, ");
            stb.append("         CURRICULUM_CD, ");
        }
        stb.append("         SUBCLASSCD, ");
        stb.append("         COUNT(*) AS CNT ");
        stb.append("     FROM ");
        stb.append("         (SELECT DISTINCT ");
        stb.append("             SCHREGNO, ");
        stb.append("             CLASSCD, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("             SCHOOL_KIND, ");
            stb.append("             CURRICULUM_CD, ");
        }
        stb.append("             SUBCLASSCD, ");
        stb.append("             GVAL_CALC ");
        stb.append("         FROM ");
        stb.append("             BASE ");
        stb.append("         WHERE ");
        stb.append("             SCHOOLCD = '1' ");
        stb.append("         ) T1 ");
        stb.append("     GROUP BY ");
        stb.append("         SCHREGNO, ");
        stb.append("         CLASSCD, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         SCHOOL_KIND, ");
            stb.append("         CURRICULUM_CD, ");
        }
        stb.append("         SUBCLASSCD ");
        //前籍校成績
        stb.append(" ), ZENSEKI AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T1.GRADE, ");
        stb.append("         T1.HR_CLASS, ");
        stb.append("         T1.ATTENDNO, ");
        stb.append("         T1.NAME_SHOW, ");
        stb.append("         T1.CLASSCD, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         T1.SCHOOL_KIND, ");
            stb.append("         T1.CURRICULUM_CD, ");
        }
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         DECIMAL(ROUND(DECIMAL(AVG(FLOAT(T1.VALUATION)),5,1),0),5,0) AS VALUATION ");
        stb.append("     FROM ");
        stb.append("         BASE T1 ");
        stb.append("         LEFT JOIN ZENSEKI_CNT T2 ON T1.SCHREGNO     = T2.SCHREGNO ");
        stb.append("                                 AND T1.CLASSCD      = T2.CLASSCD ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("                                 AND T1.SCHOOL_KIND  = T2.SCHOOL_KIND ");
            stb.append("                                 AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ");
        }
        stb.append("                                 AND T1.SUBCLASSCD   = T2.SUBCLASSCD ");
        stb.append("                                 AND T1.SCHOOLCD     = '1' ");
        stb.append("     WHERE ");
        stb.append("         T1.SCHOOLCD = '1' AND ");
        stb.append("         T2.CNT = 1 AND ");
        stb.append("         T1.GVAL_CALC = '0' ");
        stb.append("     GROUP BY ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T1.GRADE, ");
        stb.append("         T1.HR_CLASS, ");
        stb.append("         T1.ATTENDNO, ");
        stb.append("         T1.NAME_SHOW, ");
        stb.append("         T1.CLASSCD, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         T1.SCHOOL_KIND, ");
            stb.append("         T1.CURRICULUM_CD, ");
        }
        stb.append("         T1.SUBCLASSCD ");
        stb.append("     UNION ");
        stb.append("     SELECT ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T1.GRADE, ");
        stb.append("         T1.HR_CLASS, ");
        stb.append("         T1.ATTENDNO, ");
        stb.append("         T1.NAME_SHOW, ");
        stb.append("         T1.CLASSCD, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         T1.SCHOOL_KIND, ");
            stb.append("         T1.CURRICULUM_CD, ");
        }
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         INTEGER(ROUND(DECIMAL(FLOAT(SUM(T1.VALUATION * (CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN T1.GET_CREDIT + T1.ADD_CREDIT ELSE T1.GET_CREDIT END))) / SUM((CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN T1.GET_CREDIT + T1.ADD_CREDIT ELSE T1.GET_CREDIT END)),5,1),0)) AS VALUATION ");
        stb.append("     FROM ");
        stb.append("         BASE T1 ");
        stb.append("         LEFT JOIN ZENSEKI_CNT T2 ON T1.SCHREGNO     = T2.SCHREGNO ");
        stb.append("                                 AND T1.CLASSCD      = T2.CLASSCD ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("                                 AND T1.SCHOOL_KIND  = T2.SCHOOL_KIND ");
            stb.append("                                 AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ");
        }
        stb.append("                                 AND T1.SUBCLASSCD   = T2.SUBCLASSCD ");
        stb.append("                                 AND T1.SCHOOLCD     = '1' ");
        stb.append("     WHERE ");
        stb.append("         T1.SCHOOLCD = '1' AND ");
        stb.append("         T2.CNT = 1 AND ");
        stb.append("         T1.GVAL_CALC = '1' ");
        stb.append("     GROUP BY ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T1.GRADE, ");
        stb.append("         T1.HR_CLASS, ");
        stb.append("         T1.ATTENDNO, ");
        stb.append("         T1.NAME_SHOW, ");
        stb.append("         T1.CLASSCD, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         T1.SCHOOL_KIND, ");
            stb.append("         T1.CURRICULUM_CD, ");
        }
        stb.append("         T1.SUBCLASSCD ");
        stb.append("     UNION ");
        stb.append("     SELECT ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T1.GRADE, ");
        stb.append("         T1.HR_CLASS, ");
        stb.append("         T1.ATTENDNO, ");
        stb.append("         T1.NAME_SHOW, ");
        stb.append("         T1.CLASSCD, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         T1.SCHOOL_KIND, ");
            stb.append("         T1.CURRICULUM_CD, ");
        }
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         MAX(T1.VALUATION) AS VALUATION ");
        stb.append("     FROM ");
        stb.append("         BASE T1 ");
        stb.append("         LEFT JOIN ZENSEKI_CNT T2 ON T1.SCHREGNO     = T2.SCHREGNO ");
        stb.append("                                 AND T1.CLASSCD      = T2.CLASSCD ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("                                 AND T1.SCHOOL_KIND  = T2.SCHOOL_KIND ");
            stb.append("                                 AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ");
        }
        stb.append("                                 AND T1.SUBCLASSCD   = T2.SUBCLASSCD ");
        stb.append("                                 AND T1.SCHOOLCD     = '1' ");
        stb.append("     WHERE ");
        stb.append("         T1.SCHOOLCD = '1' AND ");
        stb.append("         T2.CNT = 1 AND ");
        stb.append("         T1.GVAL_CALC = '2' ");
        stb.append("     GROUP BY ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T1.GRADE, ");
        stb.append("         T1.HR_CLASS, ");
        stb.append("         T1.ATTENDNO, ");
        stb.append("         T1.NAME_SHOW, ");
        stb.append("         T1.CLASSCD, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         T1.SCHOOL_KIND, ");
            stb.append("         T1.CURRICULUM_CD, ");
        }
        stb.append("         T1.SUBCLASSCD ");
        stb.append("     UNION ");
        stb.append("     SELECT ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T1.GRADE, ");
        stb.append("         T1.HR_CLASS, ");
        stb.append("         T1.ATTENDNO, ");
        stb.append("         T1.NAME_SHOW, ");
        stb.append("         T1.CLASSCD, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         T1.SCHOOL_KIND, ");
            stb.append("         T1.CURRICULUM_CD, ");
        }
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         T1.VALUATION ");
        stb.append("     FROM ");
        stb.append("         BASE T1 ");
        stb.append("         LEFT JOIN ZENSEKI_CNT T2 ON T1.SCHREGNO     = T2.SCHREGNO ");
        stb.append("                                 AND T1.CLASSCD      = T2.CLASSCD ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("                                 AND T1.SCHOOL_KIND  = T2.SCHOOL_KIND ");
            stb.append("                                 AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ");
        }
        stb.append("                                 AND T1.SUBCLASSCD   = T2.SUBCLASSCD ");
        stb.append("                                 AND T1.SCHOOLCD     = '1' ");
        stb.append("     WHERE ");
        stb.append("         T1.SCHOOLCD = '1' AND ");
        stb.append("         T2.CNT > 1 ");
        stb.append(" ), HYOTEI AS ( ");
        stb.append("     SELECT ");
        stb.append("         * ");
        stb.append("     FROM ");
        stb.append("         GVAL_CALC0 ");
        stb.append("     UNION ");
        stb.append("     SELECT ");
        stb.append("         * ");
        stb.append("     FROM ");
        stb.append("         GVAL_CALC1 ");
        stb.append("     UNION ");
        stb.append("     SELECT ");
        stb.append("         * ");
        stb.append("     FROM ");
        stb.append("         GVAL_CALC2 ");
        stb.append("     UNION ");
        stb.append("     SELECT ");
        stb.append("         SCHREGNO, ");
        stb.append("         GRADE, ");
        stb.append("         HR_CLASS, ");
        stb.append("         ATTENDNO, ");
        stb.append("         NAME_SHOW, ");
        stb.append("         '1' AS SCHOOLCD, ");
        stb.append("         '入学前' AS YEAR, ");
        stb.append("         CLASSCD, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         SCHOOL_KIND, ");
            stb.append("         CURRICULUM_CD, ");
        }
        stb.append("         SUBCLASSCD, ");
        stb.append("         VALUATION ");
        stb.append("     FROM ");
        stb.append("         ZENSEKI ");
        stb.append(" ), HYOTEI_AVG AS ( ");
        stb.append("     SELECT ");
        stb.append("         SCHREGNO, ");
        stb.append("         GRADE, ");
        stb.append("         HR_CLASS, ");
        stb.append("         ATTENDNO, ");
        stb.append("         NAME_SHOW, ");
        stb.append("         CLASSCD, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         SCHOOL_KIND, ");
        }
        stb.append("         DECIMAL(ROUND(DECIMAL(AVG(FLOAT(VALUATION)),5,2),1),5,1) AS VALUATION ");
        stb.append("     FROM ");
        stb.append("         HYOTEI ");
        stb.append("     GROUP BY ");
        stb.append("         SCHREGNO, ");
        stb.append("         GRADE, ");
        stb.append("         HR_CLASS, ");
        stb.append("         ATTENDNO, ");
        stb.append("         NAME_SHOW, ");
        stb.append("         CLASSCD ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("        ,SCHOOL_KIND ");
        }
        stb.append(" ), HYOTEI_TOTAL_AVG AS ( ");
        stb.append("     SELECT ");
        stb.append("         SCHREGNO, ");
        stb.append("         GRADE, ");
        stb.append("         HR_CLASS, ");
        stb.append("         ATTENDNO, ");
        stb.append("         NAME_SHOW, ");
        stb.append("         DECIMAL(ROUND(DECIMAL(AVG(FLOAT(VALUATION)),5,2),1),5,1) AS VALUATION ");
        stb.append("     FROM ");
        stb.append("         HYOTEI ");
        stb.append("     GROUP BY ");
        stb.append("         SCHREGNO, ");
        stb.append("         GRADE, ");
        stb.append("         HR_CLASS, ");
        stb.append("         ATTENDNO, ");
        stb.append("         NAME_SHOW ");
        stb.append(" ), SCH_CLASS AS ( ");
        stb.append("     SELECT DISTINCT ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T1.GRADE, ");
        stb.append("         T1.HR_CLASS, ");
        stb.append("         T1.ATTENDNO, ");
        stb.append("         T2.NAME_SHOW, ");
        stb.append("         T3.CLASSCD ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("        ,T3.SCHOOL_KIND ");
        }
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT T1, ");
        stb.append("         SCHREG_BASE_MST T2, ");
        stb.append("        (SELECT ");
        stb.append("             CLASSCD ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("            ,SCHOOL_KIND ");
        }
        stb.append("         FROM ");
        stb.append("             CLASS_MST ");
        stb.append("         UNION   ");
        stb.append("         SELECT ");
        stb.append("             CLASSCD ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("            ,SCHOOL_KIND ");
        }
        stb.append("         FROM ");
        stb.append("             ANOTHER_CLASS_MST) T3 ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR     = '" + _param._year + "' AND ");
        stb.append("         T1.SEMESTER = '" + _param._gakki + "' AND ");
        stb.append("         T1.GRADE    = '" + _param._grade + "' AND ");
        stb.append("         T1.SCHREGNO = T2.SCHREGNO ");
        stb.append(" ), MAIN AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.*, ");
        stb.append("         T2.VALUATION ");
        stb.append("     FROM ");
        stb.append("         SCH_CLASS T1 ");
        stb.append("         LEFT JOIN HYOTEI_AVG T2 ON T1.SCHREGNO      = T2.SCHREGNO ");
        stb.append("                                AND T1.CLASSCD       = T2.CLASSCD ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("                                AND T1.SCHOOL_KIND   = T2.SCHOOL_KIND ");
        }
        stb.append("     UNION  ");
        stb.append("     SELECT ");
        stb.append("         SCHREGNO, ");
        stb.append("         GRADE, ");
        stb.append("         HR_CLASS, ");
        stb.append("         ATTENDNO, ");
        stb.append("         NAME_SHOW, ");
        stb.append("         '999' AS CLASSCD, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         'Z' AS SCHOOL_KIND, ");
        }
        stb.append("         VALUATION ");
        stb.append("     FROM ");
        stb.append("         HYOTEI_TOTAL_AVG ");
        stb.append(" ) ");

        stb.append(" SELECT DISTINCT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.NAME_SHOW, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND AS CLASSCD, ");
        } else {
            stb.append("     T1.CLASSCD AS CLASSCD, ");
        }
        stb.append("     T1.VALUATION, ");
        stb.append("     CASE WHEN L1.SHOWORDER2 IS NOT NULL THEN L1.SHOWORDER2 ");
        stb.append("          ELSE 100 END AS CLASS_ORDER ");
        stb.append(" FROM ");
        stb.append("     MAIN T1 ");
        stb.append("     LEFT JOIN CLASS_MST L1 ON T1.CLASSCD        = L1.CLASSCD ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("                           AND T1.SCHOOL_KIND    = L1.SCHOOL_KIND ");
        }
        stb.append(" WHERE ");
        stb.append("     T1.CLASSCD != '999' ");
        stb.append(" UNION ");
        stb.append(" SELECT DISTINCT ");
        stb.append("     SCHREGNO, ");
        stb.append("     GRADE, ");
        stb.append("     HR_CLASS, ");
        stb.append("     ATTENDNO, ");
        stb.append("     NAME_SHOW, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     CLASSCD || '-' || SCHOOL_KIND AS CLASSCD, ");
        } else {
            stb.append("     CLASSCD AS CLASSCD, ");
        }
        stb.append("     VALUATION, ");
        stb.append("     999 AS CLASS_ORDER ");
        stb.append(" FROM ");
        stb.append("     MAIN ");
        stb.append(" WHERE ");
        stb.append("     CLASSCD = '999' ");
        stb.append(" ORDER BY ");
        stb.append("     GRADE, ");
        stb.append("     HR_CLASS, ");
        stb.append("     ATTENDNO, ");
        stb.append("     CLASS_ORDER, ");
        stb.append("     CLASSCD ");
        return stb.toString();
    }
    
    //科目別成績詳細情報
    private String getStudyrec2Sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH BASE AS ( ");
        stb.append("     SELECT DISTINCT ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T1.GRADE, ");
        stb.append("         T1.HR_CLASS, ");
        stb.append("         T1.ATTENDNO, ");
        stb.append("         T2.NAME_SHOW, ");
        stb.append("         T3.SCHOOLCD, ");
        stb.append("         T3.YEAR, ");
        stb.append("         T3.CLASSCD, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         T3.SCHOOL_KIND, ");
            stb.append("         T3.CURRICULUM_CD, ");
        }
        stb.append("         CASE WHEN L1.SUBCLASSCD2 IS NULL THEN T3.SUBCLASSCD ");
        stb.append("              ELSE L1.SUBCLASSCD2 END AS SUBCLASSCD, ");
        stb.append("         T3.SUBCLASSNAME, ");
        stb.append("         T3.VALUATION, ");
        stb.append("         CASE WHEN T3.GET_CREDIT = 0 THEN (CASE WHEN T3.COMP_CREDIT <> 0 THEN T3.COMP_CREDIT ");
        stb.append("                                                ELSE L3.CREDITS END) ");
        stb.append("              ELSE T3.GET_CREDIT END AS CREDIT, ");
        stb.append("         T3.GET_CREDIT, ");
        stb.append("         T3.ADD_CREDIT, ");
        stb.append("         T3.COMP_CREDIT, ");
        stb.append("         L2.GVAL_CALC ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT T1 ");
        stb.append("         INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("         LEFT JOIN SCHREG_STUDYREC_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("         LEFT JOIN SUBCLASS_MST L1 ON L1.SUBCLASSCD      = T3.SUBCLASSCD ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("                                  AND L1.CLASSCD         = T3.CLASSCD ");
            stb.append("                                  AND L1.SCHOOL_KIND     = T3.SCHOOL_KIND ");
            stb.append("                                  AND L1.CURRICULUM_CD   = T3.CURRICULUM_CD ");
        }
        stb.append("         LEFT JOIN SCHOOL_MST L2 ON L2.YEAR = T3.YEAR ");
        stb.append("         LEFT JOIN CREDIT_MST L3 ON L3.YEAR          = T3.YEAR ");
        stb.append("                                AND L3.COURSECD      = T1.COURSECD ");
        stb.append("                                AND L3.MAJORCD       = T1.MAJORCD ");
        stb.append("                                AND L3.GRADE         = T1.GRADE ");
        stb.append("                                AND L3.COURSECODE    = T1.COURSECODE ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR     = '" + _param._year + "' AND ");
        stb.append("         T1.SEMESTER = '" + _param._gakki + "' AND ");
        stb.append("         T1.GRADE    = '" + _param._grade + "' ");
        stb.append(" ), GVAL_CALC0 AS ( ");
        stb.append("     SELECT ");
        stb.append("         SCHREGNO, ");
        stb.append("         GRADE, ");
        stb.append("         HR_CLASS, ");
        stb.append("         ATTENDNO, ");
        stb.append("         NAME_SHOW, ");
        stb.append("         SCHOOLCD, ");
        stb.append("         YEAR, ");
        stb.append("         CLASSCD, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         SCHOOL_KIND, ");
            stb.append("         CURRICULUM_CD, ");
        }
        stb.append("         SUBCLASSCD, ");
        stb.append("         SUBCLASSNAME, ");
        stb.append("         DECIMAL(ROUND(DECIMAL(AVG(FLOAT(VALUATION)),5,1),0),5,0) AS VALUATION, ");
        stb.append("         SUM(GET_CREDIT) AS GET_CREDIT, ");
        stb.append("         SUM(ADD_CREDIT) AS ADD_CREDIT, ");
        stb.append("         SUM(COMP_CREDIT) AS COMP_CREDIT ");
        stb.append("     FROM ");
        stb.append("         BASE ");
        stb.append("     WHERE ");
        stb.append("         SCHOOLCD    = '0' AND ");
        stb.append("         GVAL_CALC   = '0' ");
        stb.append("     GROUP BY ");
        stb.append("         SCHREGNO, ");
        stb.append("         GRADE, ");
        stb.append("         HR_CLASS, ");
        stb.append("         ATTENDNO, ");
        stb.append("         NAME_SHOW, ");
        stb.append("         SCHOOLCD, ");
        stb.append("         YEAR, ");
        stb.append("         CLASSCD, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         SCHOOL_KIND, ");
            stb.append("         CURRICULUM_CD, ");
        }
        stb.append("         SUBCLASSCD, ");
        stb.append("         SUBCLASSNAME ");
        stb.append(" ), GVAL_CALC1 AS ( ");
        stb.append("     SELECT ");
        stb.append("         SCHREGNO, ");
        stb.append("         GRADE, ");
        stb.append("         HR_CLASS, ");
        stb.append("         ATTENDNO, ");
        stb.append("         NAME_SHOW, ");
        stb.append("         SCHOOLCD, ");
        stb.append("         YEAR, ");
        stb.append("         CLASSCD, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         SCHOOL_KIND, ");
            stb.append("         CURRICULUM_CD, ");
        }
        stb.append("         SUBCLASSCD, ");
        stb.append("         SUBCLASSNAME, ");
        stb.append("         INTEGER(ROUND(DECIMAL(FLOAT(SUM(VALUATION * (CASE WHEN ADD_CREDIT IS NOT NULL THEN CREDIT + ADD_CREDIT ELSE CREDIT END))) / SUM((CASE WHEN ADD_CREDIT IS NOT NULL THEN CREDIT + ADD_CREDIT ELSE CREDIT END)),5,1),0)) AS VALUATION, ");
        stb.append("         SUM(GET_CREDIT) AS GET_CREDIT, ");
        stb.append("         SUM(ADD_CREDIT) AS ADD_CREDIT, ");
        stb.append("         SUM(COMP_CREDIT) AS COMP_CREDIT ");
        stb.append("     FROM ");
        stb.append("         BASE ");
        stb.append("     WHERE ");
        stb.append("         SCHOOLCD    = '0' AND ");
        stb.append("         GVAL_CALC   = '1' ");
        stb.append("     GROUP BY ");
        stb.append("         SCHREGNO, ");
        stb.append("         GRADE, ");
        stb.append("         HR_CLASS, ");
        stb.append("         ATTENDNO, ");
        stb.append("         NAME_SHOW, ");
        stb.append("         SCHOOLCD, ");
        stb.append("         YEAR, ");
        stb.append("         CLASSCD, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         SCHOOL_KIND, ");
            stb.append("         CURRICULUM_CD, ");
        }
        stb.append("         SUBCLASSCD, ");
        stb.append("         SUBCLASSNAME ");
        stb.append(" ), GVAL_CALC2 AS ( ");
        stb.append("     SELECT ");
        stb.append("         SCHREGNO, ");
        stb.append("         GRADE, ");
        stb.append("         HR_CLASS, ");
        stb.append("         ATTENDNO, ");
        stb.append("         NAME_SHOW, ");
        stb.append("         SCHOOLCD, ");
        stb.append("         YEAR, ");
        stb.append("         CLASSCD, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         SCHOOL_KIND, ");
            stb.append("         CURRICULUM_CD, ");
        }
        stb.append("         SUBCLASSCD, ");
        stb.append("         SUBCLASSNAME, ");
        stb.append("         MAX(VALUATION) AS VALUATION, ");
        stb.append("         SUM(GET_CREDIT) AS GET_CREDIT, ");
        stb.append("         SUM(ADD_CREDIT) AS ADD_CREDIT, ");
        stb.append("         SUM(COMP_CREDIT) AS COMP_CREDIT ");
        stb.append("     FROM ");
        stb.append("         BASE ");
        stb.append("     WHERE ");
        stb.append("         SCHOOLCD    = '0' AND ");
        stb.append("         GVAL_CALC   = '2' ");
        stb.append("     GROUP BY ");
        stb.append("         SCHREGNO, ");
        stb.append("         GRADE, ");
        stb.append("         HR_CLASS, ");
        stb.append("         ATTENDNO, ");
        stb.append("         NAME_SHOW, ");
        stb.append("         SCHOOLCD, ");
        stb.append("         YEAR, ");
        stb.append("         CLASSCD, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         SCHOOL_KIND, ");
            stb.append("         CURRICULUM_CD, ");
        }
        stb.append("         SUBCLASSCD, ");
        stb.append("         SUBCLASSNAME ");
        //前籍校をGVAL_CALCごとにカウント
        stb.append(" ), ZENSEKI_CNT AS ( ");
        stb.append("     SELECT ");
        stb.append("         SCHREGNO, ");
        stb.append("         CLASSCD, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         SCHOOL_KIND, ");
            stb.append("         CURRICULUM_CD, ");
        }
        stb.append("         SUBCLASSCD, ");
        stb.append("         COUNT(*) AS CNT ");
        stb.append("     FROM ");
        stb.append("         (SELECT DISTINCT ");
        stb.append("             SCHREGNO, ");
        stb.append("             CLASSCD, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("             SCHOOL_KIND, ");
            stb.append("             CURRICULUM_CD, ");
        }
        stb.append("             SUBCLASSCD, ");
        stb.append("             GVAL_CALC ");
        stb.append("         FROM ");
        stb.append("             BASE ");
        stb.append("         WHERE ");
        stb.append("             SCHOOLCD = '1' ");
        stb.append("         ) T1 ");
        stb.append("     GROUP BY ");
        stb.append("         SCHREGNO, ");
        stb.append("         CLASSCD, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         SCHOOL_KIND, ");
            stb.append("         CURRICULUM_CD, ");
        }
        stb.append("         SUBCLASSCD ");
        //前籍校成績
        stb.append(" ), ZENSEKI AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T1.GRADE, ");
        stb.append("         T1.HR_CLASS, ");
        stb.append("         T1.ATTENDNO, ");
        stb.append("         T1.NAME_SHOW, ");
        stb.append("         T1.CLASSCD, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         T1.SCHOOL_KIND, ");
            stb.append("         T1.CURRICULUM_CD, ");
        }
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         T1.SUBCLASSNAME, ");
        stb.append("         DECIMAL(ROUND(DECIMAL(AVG(FLOAT(T1.VALUATION)),5,1),0),5,0) AS VALUATION, ");
        stb.append("         SUM(T1.GET_CREDIT) AS GET_CREDIT, ");
        stb.append("         SUM(T1.ADD_CREDIT) AS ADD_CREDIT, ");
        stb.append("         SUM(T1.COMP_CREDIT) AS COMP_CREDIT ");
        stb.append("     FROM ");
        stb.append("         BASE T1 ");
        stb.append("         LEFT JOIN ZENSEKI_CNT T2 ON T1.SCHREGNO     = T2.SCHREGNO ");
        stb.append("                                 AND T1.CLASSCD      = T2.CLASSCD ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("                                 AND T1.SCHOOL_KIND  = T2.SCHOOL_KIND ");
            stb.append("                                 AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ");
        }
        stb.append("                                 AND T1.SUBCLASSCD   = T2.SUBCLASSCD ");
        stb.append("                                 AND T1.SCHOOLCD     = '1' ");
        stb.append("     WHERE ");
        stb.append("         T1.SCHOOLCD = '1' AND ");
        stb.append("         T2.CNT = 1 AND ");
        stb.append("         T1.GVAL_CALC = '0' ");
        stb.append("     GROUP BY ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T1.GRADE, ");
        stb.append("         T1.HR_CLASS, ");
        stb.append("         T1.ATTENDNO, ");
        stb.append("         T1.NAME_SHOW, ");
        stb.append("         T1.CLASSCD, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         T1.SCHOOL_KIND, ");
            stb.append("         T1.CURRICULUM_CD, ");
        }
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         T1.SUBCLASSNAME ");
        stb.append("     UNION ");
        stb.append("     SELECT ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T1.GRADE, ");
        stb.append("         T1.HR_CLASS, ");
        stb.append("         T1.ATTENDNO, ");
        stb.append("         T1.NAME_SHOW, ");
        stb.append("         T1.CLASSCD, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         T1.SCHOOL_KIND, ");
            stb.append("         T1.CURRICULUM_CD, ");
        }
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         T1.SUBCLASSNAME, ");
        stb.append("         INTEGER(ROUND(DECIMAL(FLOAT(SUM(T1.VALUATION * (CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN T1.CREDIT + T1.ADD_CREDIT ELSE T1.CREDIT END))) / SUM((CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN T1.CREDIT + T1.ADD_CREDIT ELSE T1.CREDIT END)),5,1),0)) AS VALUATION, ");
        stb.append("         SUM(T1.GET_CREDIT) AS GET_CREDIT, ");
        stb.append("         SUM(T1.ADD_CREDIT) AS ADD_CREDIT, ");
        stb.append("         SUM(T1.COMP_CREDIT) AS COMP_CREDIT ");
        stb.append("     FROM ");
        stb.append("         BASE T1 ");
        stb.append("         LEFT JOIN ZENSEKI_CNT T2 ON T1.SCHREGNO     = T2.SCHREGNO ");
        stb.append("                                 AND T1.CLASSCD      = T2.CLASSCD ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("                                 AND T1.SCHOOL_KIND  = T2.SCHOOL_KIND ");
            stb.append("                                 AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ");
        }
        stb.append("                                 AND T1.SUBCLASSCD   = T2.SUBCLASSCD ");
        stb.append("                                 AND T1.SCHOOLCD     = '1' ");
        stb.append("     WHERE ");
        stb.append("         T1.SCHOOLCD = '1' AND ");
        stb.append("         T2.CNT = 1 AND ");
        stb.append("         T1.GVAL_CALC = '1' ");
        stb.append("     GROUP BY ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T1.GRADE, ");
        stb.append("         T1.HR_CLASS, ");
        stb.append("         T1.ATTENDNO, ");
        stb.append("         T1.NAME_SHOW, ");
        stb.append("         T1.CLASSCD, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         T1.SCHOOL_KIND, ");
            stb.append("         T1.CURRICULUM_CD, ");
        }
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         T1.SUBCLASSNAME ");
        stb.append("     UNION ");
        stb.append("     SELECT ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T1.GRADE, ");
        stb.append("         T1.HR_CLASS, ");
        stb.append("         T1.ATTENDNO, ");
        stb.append("         T1.NAME_SHOW, ");
        stb.append("         T1.CLASSCD, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         T1.SCHOOL_KIND, ");
            stb.append("         T1.CURRICULUM_CD, ");
        }
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         T1.SUBCLASSNAME, ");
        stb.append("         MAX(T1.VALUATION) AS VALUATION, ");
        stb.append("         SUM(T1.GET_CREDIT) AS GET_CREDIT, ");
        stb.append("         SUM(T1.ADD_CREDIT) AS ADD_CREDIT, ");
        stb.append("         SUM(T1.COMP_CREDIT) AS COMP_CREDIT ");
        stb.append("     FROM ");
        stb.append("         BASE T1 ");
        stb.append("         LEFT JOIN ZENSEKI_CNT T2 ON T1.SCHREGNO     = T2.SCHREGNO ");
        stb.append("                                 AND T1.CLASSCD      = T2.CLASSCD ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("                                 AND T1.SCHOOL_KIND  = T2.SCHOOL_KIND ");
            stb.append("                                 AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ");
        }
        stb.append("                                 AND T1.SUBCLASSCD   = T2.SUBCLASSCD ");
        stb.append("                                 AND T1.SCHOOLCD     = '1' ");
        stb.append("     WHERE ");
        stb.append("         T1.SCHOOLCD = '1' AND ");
        stb.append("         T2.CNT = 1 AND ");
        stb.append("         T1.GVAL_CALC = '2' ");
        stb.append("     GROUP BY ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T1.GRADE, ");
        stb.append("         T1.HR_CLASS, ");
        stb.append("         T1.ATTENDNO, ");
        stb.append("         T1.NAME_SHOW, ");
        stb.append("         T1.CLASSCD, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         T1.SCHOOL_KIND, ");
            stb.append("         T1.CURRICULUM_CD, ");
        }
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         T1.SUBCLASSNAME ");
        stb.append("     UNION ");
        stb.append("     SELECT ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T1.GRADE, ");
        stb.append("         T1.HR_CLASS, ");
        stb.append("         T1.ATTENDNO, ");
        stb.append("         T1.NAME_SHOW, ");
        stb.append("         T1.CLASSCD, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         T1.SCHOOL_KIND, ");
            stb.append("         T1.CURRICULUM_CD, ");
        }
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         T1.SUBCLASSNAME, ");
        stb.append("         MAX(T1.VALUATION) AS VALUATION, ");
        stb.append("         T2.GET_CREDIT, ");
        stb.append("         T2.ADD_CREDIT, ");
        stb.append("         T2.COMP_CREDIT ");
        stb.append("     FROM ");
        stb.append("         BASE T1, ");
        stb.append("         (SELECT ");
        stb.append("             T1.SCHREGNO, ");
        stb.append("             T1.CLASSCD, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("             T1.SCHOOL_KIND, ");
            stb.append("             T1.CURRICULUM_CD, ");
        }
        stb.append("             T1.SUBCLASSCD, ");
        stb.append("             MAX(T1.GET_CREDIT) AS MAX_CREDIT, ");
        stb.append("             SUM(T1.GET_CREDIT) AS GET_CREDIT, ");
        stb.append("             SUM(T1.ADD_CREDIT) AS ADD_CREDIT, ");
        stb.append("             SUM(T1.COMP_CREDIT) AS COMP_CREDIT ");
        stb.append("         FROM ");
        stb.append("             BASE T1 ");
        stb.append("             LEFT JOIN ZENSEKI_CNT T2 ON T1.SCHREGNO     = T2.SCHREGNO ");
        stb.append("                                     AND T1.CLASSCD      = T2.CLASSCD ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("                                     AND T1.SCHOOL_KIND  = T2.SCHOOL_KIND ");
            stb.append("                                     AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ");
        }
        stb.append("                                     AND T1.SUBCLASSCD   = T2.SUBCLASSCD ");
        stb.append("                                     AND T1.SCHOOLCD     = '1' ");
        stb.append("         WHERE ");
        stb.append("             T1.SCHOOLCD = '1' AND ");
        stb.append("             T2.CNT > 1 ");
        stb.append("         GROUP BY ");
        stb.append("             T1.SCHREGNO, ");
        stb.append("             T1.CLASSCD, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("             T1.SCHOOL_KIND, ");
            stb.append("             T1.CURRICULUM_CD, ");
        }
        stb.append("             T1.SUBCLASSCD ");
        stb.append("         ) T2 ");
        stb.append("     WHERE ");
        stb.append("         T1.SCHREGNO         = T2.SCHREGNO AND ");
        stb.append("         T1.CLASSCD          = T2.CLASSCD AND ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         T1.SCHOOL_KIND      = T2.SCHOOL_KIND AND ");
            stb.append("         T1.CURRICULUM_CD    = T2.CURRICULUM_CD AND ");
        }
        stb.append("         T1.SUBCLASSCD       = T2.SUBCLASSCD AND ");
        stb.append("         T1.GET_CREDIT       = T2.MAX_CREDIT ");
        stb.append("     GROUP BY ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T1.GRADE, ");
        stb.append("         T1.HR_CLASS, ");
        stb.append("         T1.ATTENDNO, ");
        stb.append("         T1.NAME_SHOW, ");
        stb.append("         T1.CLASSCD, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         T1.SCHOOL_KIND, ");
            stb.append("         T1.CURRICULUM_CD, ");
        }
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         T1.SUBCLASSNAME, ");
        stb.append("         T2.GET_CREDIT, ");
        stb.append("         T2.ADD_CREDIT, ");
        stb.append("         T2.COMP_CREDIT ");
        //留学単位数
        stb.append(" ), TRANSFER AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T1.GRADE, ");
        stb.append("         T1.HR_CLASS, ");
        stb.append("         T1.ATTENDNO, ");
        stb.append("         T1.NAME_SHOW, ");
        stb.append("         '' AS SCHOOLCD, ");
        stb.append("         '9999' AS YEAR, ");
        stb.append("         '' AS CLASSCD, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         '' AS SCHOOL_KIND, ");
            stb.append("         '' AS CURRICULUM_CD, ");
        }
        stb.append("         '' AS SUBCLASSCD, ");
        stb.append("         '留学単位数' AS SUBCLASSNAME, ");
        stb.append("         0 AS VALUATION, ");
        stb.append("         T2.ABROAD_CREDITS AS GET_CREDIT, ");
        stb.append("         0 AS ADD_CREDIT, ");
        stb.append("         T2.ABROAD_CREDITS AS COMP_CREDIT ");
        stb.append("     FROM ");
        stb.append("         BASE T1, ");
        stb.append("         (SELECT ");
        stb.append("             SCHREGNO, ");
        stb.append("             SUM(ABROAD_CREDITS) AS ABROAD_CREDITS ");
        stb.append("         FROM ");
        stb.append("             SCHREG_TRANSFER_DAT ");
        stb.append("         WHERE ");
        stb.append("             TRANSFERCD = '1' ");     //留学
        stb.append("         GROUP BY ");
        stb.append("             SCHREGNO ");
        stb.append("         ) T2 ");
        stb.append("     WHERE ");
        stb.append("         T1.SCHREGNO = T2.SCHREGNO ");
        stb.append(" ), MAIN AS ( ");
        stb.append("     SELECT ");
        stb.append("         * ");
        stb.append("     FROM ");
        stb.append("         GVAL_CALC0 ");
        stb.append("     UNION  ");
        stb.append("     SELECT ");
        stb.append("         * ");
        stb.append("     FROM ");
        stb.append("         GVAL_CALC1 ");
        stb.append("     UNION  ");
        stb.append("     SELECT ");
        stb.append("         * ");
        stb.append("     FROM ");
        stb.append("         GVAL_CALC2 ");
        stb.append("     UNION  ");
        stb.append("     SELECT ");
        stb.append("         SCHREGNO, ");
        stb.append("         GRADE, ");
        stb.append("         HR_CLASS, ");
        stb.append("         ATTENDNO, ");
        stb.append("         NAME_SHOW, ");
        stb.append("         '1' AS SCHOOLCD, ");
        stb.append("         '入学前' AS YEAR, ");
        stb.append("         CLASSCD, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         SCHOOL_KIND, ");
            stb.append("         CURRICULUM_CD, ");
        }
        stb.append("         SUBCLASSCD, ");
        stb.append("         SUBCLASSNAME, ");
        stb.append("         VALUATION, ");
        stb.append("         GET_CREDIT, ");
        stb.append("         ADD_CREDIT, ");
        stb.append("         COMP_CREDIT ");
        stb.append("     FROM ");
        stb.append("         ZENSEKI ");
        stb.append("     UNION  ");
        stb.append("     SELECT ");
        stb.append("         * ");
        stb.append("     FROM ");
        stb.append("         TRANSFER ");
        stb.append(" ) ");

        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.NAME_SHOW, ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.CLASSCD, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
        }
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     CASE WHEN T1.SUBCLASSNAME IS NOT NULL THEN T1.SUBCLASSNAME ");
        stb.append("          WHEN T1.SCHOOLCD='0' AND L2.SUBCLASSORDERNAME1 IS NOT NULL THEN L2.SUBCLASSORDERNAME1 ");
        stb.append("          WHEN T1.SCHOOLCD='1' AND L3.SUBCLASSORDERNAME1 IS NOT NULL THEN L3.SUBCLASSORDERNAME1 ");
        stb.append("          WHEN T1.SCHOOLCD='1' THEN L3.SUBCLASSNAME ");
        stb.append("          ELSE L2.SUBCLASSNAME END AS SUBCLASSNAME, ");
        stb.append("     T1.VALUATION, ");
        stb.append("     T1.GET_CREDIT, ");
        stb.append("     T1.ADD_CREDIT, ");
        stb.append("     T1.COMP_CREDIT, ");
        stb.append("     L1.SHOWORDER2 AS CLASS_ORDER, ");
        stb.append("     L2.SHOWORDER2 AS SUBCLASS_ORDER ");
        stb.append(" FROM ");
        stb.append("     MAIN T1 ");
        stb.append("     LEFT JOIN CLASS_MST L1 ON T1.CLASSCD        = L1.CLASSCD ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("                           AND T1.SCHOOL_KIND    = L1.SCHOOL_KIND ");
        }
        stb.append("     LEFT JOIN SUBCLASS_MST L2 ON T1.SUBCLASSCD      = L2.SUBCLASSCD ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("                              AND T1.CLASSCD         = L2.CLASSCD ");
            stb.append("                              AND T1.SCHOOL_KIND     = L2.SCHOOL_KIND ");
            stb.append("                              AND T1.CURRICULUM_CD   = L2.CURRICULUM_CD ");
        }
        stb.append("     LEFT JOIN ANOTHER_SUBCLASS_MST L3 ON T1.SUBCLASSCD  = L3.SUBCLASSCD ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("                              AND T1.CLASSCD         = L3.CLASSCD ");
            stb.append("                              AND T1.SCHOOL_KIND     = L3.SCHOOL_KIND ");
            stb.append("                              AND T1.CURRICULUM_CD   = L3.CURRICULUM_CD ");
            stb.append("                              AND T1.SCHOOLCD        = '1' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     GRADE, ");
        stb.append("     HR_CLASS, ");
        stb.append("     ATTENDNO, ");
        stb.append("     CLASS_ORDER, ");
        stb.append("     CLASSCD, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     SCHOOL_KIND, ");
            stb.append("     CURRICULUM_CD, ");
        }
        stb.append("     SUBCLASS_ORDER, ");
        stb.append("     SUBCLASSCD, ");
        stb.append("     YEAR ");
        return stb.toString();
    }
}

// eof
