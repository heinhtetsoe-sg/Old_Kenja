// kanji=漢字
/*
 * $Id: c7e5a51be37bb8dee984ee724825bb135ecf0bdf $
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
 * @version $Id: c7e5a51be37bb8dee984ee724825bb135ecf0bdf $
 */
public class KNJE153 extends AbstractXls {

    private static final Log log = LogFactory.getLog("KNJE153.class");
    
    private boolean _hasData;
    
    private static String OUTPUTDATA = "1";//1.まとめて集計　2.年度ごと
    
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
        final List studentList = new ArrayList();
        try {
            psXls = _db2.prepareStatement(sql);
            rsXls = psXls.executeQuery();
            while (rsXls.next()) {
                final String schregno = rsXls.getString("SCHREGNO");
                Student student = null;
                if (OUTPUTDATA.equals(_param._output)) {
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
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                	String column = meta.getColumnName(i);
                    String value = rsXls.getString(column);
                    dataMap.put(column, value);
                }
                student._record.add(dataMap);
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
                        //2.年度ごとのとき
                        if (!OUTPUTDATA.equals(_param._output)) {
                        	if (dataMap.get("SCHOOLCD").equals("1")) {
                        		dataMap.put("SCHOOLCD", "*");
                        	} else if (dataMap.get("SCHOOLCD").equals("0")) {
                        		dataMap.put("SCHOOLCD", "");
                        	}	
                        }
                        xlsData.add(val);
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
        retList.add("学籍番号");
        retList.add("学年");
        retList.add("組");
        retList.add("出席番号");
        retList.add("生徒氏名");
        //2.年度ごと
        if (!OUTPUTDATA.equals(_param._output)) {
            retList.add("年度");
        }
        retList.add("授業日数");
        retList.add("休学日数");
        retList.add("公欠日数");
        retList.add("出停日数");
        retList.add("忌引日数");
        retList.add("留学日数");
        retList.add("欠席日数");
        retList.add("遅刻日数");
        retList.add("早退日数");       
        //2.年度ごと
        if (!OUTPUTDATA.equals(_param._output)) {
            retList.add("前籍校");
        }
        return retList;
    }

    protected String[] getCols() {
        return null;
    }

    protected String[] getCols1() {
        final String[] cols1 = new String[]{
                "SCHREGNO",
                "GRADE",
                "HR_CLASS",
                "ATTENDNO",
                "NAME_SHOW",
        };
        return cols1;
    }

    protected String[] getCols2() {
        final String[] cols2;
        //1.まとめて集計
        if (OUTPUTDATA.equals(_param._output)) {
        	cols2 = new String[]{
        			"LESSON",
                    "OFFDAYS",
                    "ABSENT",
                    "SUSPEND",
                    "MOURNING",
                    "ABROAD",
                    "SICK",
                    "LATE",
                    "EARLY",};        	
        //2.年度ごと
	    } else {
            cols2 = new String[]{
                    "YEAR",
        			"LESSON",
                    "OFFDAYS",
                    "ABSENT",
                    "SUSPEND",
                    "MOURNING",
                    "ABROAD",
                    "SICK",
                    "LATE",
                    "EARLY",
                    "SCHOOLCD",};        
        }
        return cols2;
    }
    
    protected String getSql() {
        final String sql;
    	sql = getAttendDataSql();
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
        final String _useCurriculumcd;
        final String _output;//1:まとめて集計、2:年度ごと

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _grade = request.getParameter("GRADE");
        	_year = request.getParameter("YEAR");
            _gakki = request.getParameter("CTRL_SEMESTER");
            _header = true;
            _templatePath = request.getParameter("TEMPLATE_PATH");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _output = request.getParameter("OUTPUT");
        }
    }
    
    //データ取得
    private String getAttendDataSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append("");
        stb.append("  WITH SCH_INFO AS ( ");
        stb.append("      SELECT ");
        stb.append("          T1.SCHREGNO, ");
        stb.append("          T1.GRADE, ");
        stb.append("          T1.HR_CLASS, ");
        stb.append("          T1.ATTENDNO, ");
        stb.append("          T2.NAME_SHOW ");
        stb.append("      FROM ");
        stb.append("          SCHREG_REGD_DAT T1, ");
        stb.append("          SCHREG_BASE_MST T2 ");
        stb.append("      WHERE ");
        stb.append("          T1.YEAR     = '" + _param._year + "' AND ");
        stb.append("          T1.SEMESTER = '" + _param._gakki + "' AND ");
        stb.append("          T1.SCHREGNO = T2.SCHREGNO AND ");
        stb.append("          T1.GRADE    = '" + _param._grade + "' ");
        //出欠データ取得（ATTEND_SEMES_DAT）
        stb.append("  ), ATTEND_SEM AS ( ");
        stb.append("      SELECT ");
        stb.append("          T1.SCHREGNO, ");
        stb.append("          T1.GRADE, ");
        stb.append("          T1.HR_CLASS, ");
        stb.append("          T1.ATTENDNO, ");
        stb.append("          T1.NAME_SHOW, ");
        if (!OUTPUTDATA.equals(_param._output)) {
            stb.append("          T2.YEAR, ");
            stb.append("          '0' AS SCHOOLCD, ");
        }
        stb.append("          SUM(T2.LESSON) AS LESSON, ");
        stb.append("          SUM(T2.OFFDAYS) AS OFFDAYS, ");
        stb.append("          SUM(T2.ABSENT) AS ABSENT, ");
        stb.append("          SUM(T2.SUSPEND) AS SUSPEND, ");
        stb.append("          SUM(T2.MOURNING) AS MOURNING, ");
        stb.append("          SUM(T2.ABROAD) AS ABROAD, ");
        stb.append("          SUM(T2.SICK+NOTICE+NONOTICE) AS SICK, ");
        stb.append("          SUM(T2.LATE) AS LATE, ");
        stb.append("          SUM(T2.EARLY) AS EARLY ");
        stb.append("      FROM ");
        stb.append("          SCH_INFO T1, ");
        stb.append("          ATTEND_SEMES_DAT T2 ");
        stb.append("      WHERE ");
        stb.append("          T1.SCHREGNO = T2.SCHREGNO ");
        stb.append("      GROUP BY ");
        stb.append("          T1.SCHREGNO, ");
        stb.append("          T1.GRADE, ");
        stb.append("          T1.HR_CLASS, ");
        stb.append("          T1.ATTENDNO, ");
        stb.append("          T1.NAME_SHOW ");
        if (!OUTPUTDATA.equals(_param._output)) {
            stb.append("         ,T2.YEAR ");
        }
        //前籍校出欠データ取得（SCHREG_ATTENDREC_DAT）
        stb.append("  ), ATTENDREC AS ( ");
        stb.append("      SELECT ");
        stb.append("          T1.SCHREGNO, ");
        stb.append("          T1.GRADE, ");
        stb.append("          T1.HR_CLASS, ");
        stb.append("          T1.ATTENDNO, ");
        stb.append("          T1.NAME_SHOW, ");
        if (!OUTPUTDATA.equals(_param._output)) {
            stb.append("          T2.YEAR, ");
            stb.append("          T2.SCHOOLCD, ");
        }
        stb.append("          SUM(T2.CLASSDAYS) AS LESSON, ");
        stb.append("          SUM(T2.OFFDAYS) AS OFFDAYS, ");
        stb.append("          SUM(T2.ABSENT) AS ABSENT, ");
        stb.append("          SUM(T2.SUSPEND) AS SUSPEND, ");
        stb.append("          SUM(T2.MOURNING) AS MOURNING, ");
        stb.append("          SUM(T2.ABROAD) AS ABROAD, ");
        stb.append("          SUM(T2.SICK+ACCIDENTNOTICE+NOACCIDENTNOTICE) AS SICK, ");
        stb.append("          0 AS LATE, ");
        stb.append("          0 AS EARLY ");
        stb.append("      FROM ");
        stb.append("          SCH_INFO T1, ");
        stb.append("          SCHREG_ATTENDREC_DAT T2 ");
        stb.append("      WHERE ");
        stb.append("          T1.SCHREGNO = T2.SCHREGNO AND ");
        stb.append("          T2.SCHOOLCD = '1' ");
        stb.append("      GROUP BY ");
        stb.append("          T1.SCHREGNO, ");
        stb.append("          T1.GRADE, ");
        stb.append("          T1.HR_CLASS, ");
        stb.append("          T1.ATTENDNO, ");
        stb.append("          T1.NAME_SHOW ");
        if (!OUTPUTDATA.equals(_param._output)) {
            stb.append("          ,T2.YEAR ");
            stb.append("          ,T2.SCHOOLCD ");
        }
        stb.append("  ) ");

        //メイン
        stb.append("  SELECT ");
        stb.append("      SCHREGNO, ");
        stb.append("      GRADE, ");
        stb.append("      HR_CLASS, ");
        stb.append("      ATTENDNO, ");
        stb.append("      NAME_SHOW, ");
        stb.append("      VALUE(SUM(LESSON),0) AS LESSON, ");
        stb.append("      VALUE(SUM(OFFDAYS),0) AS OFFDAYS, ");
        stb.append("      VALUE(SUM(ABSENT),0) AS ABSENT, ");
        stb.append("      VALUE(SUM(SUSPEND),0) AS SUSPEND, ");
        stb.append("      VALUE(SUM(MOURNING),0) AS MOURNING, ");
        stb.append("      VALUE(SUM(ABROAD),0) AS ABROAD, ");
        stb.append("      VALUE(SUM(SICK),0) AS SICK, ");
        stb.append("      VALUE(SUM(LATE),0) AS LATE, ");
        stb.append("      VALUE(SUM(EARLY),0) AS EARLY ");
        if (!OUTPUTDATA.equals(_param._output)) {
            stb.append("      ,YEAR ");
            stb.append("      ,SCHOOLCD ");
        }
        stb.append("  FROM ");
        stb.append("      (SELECT * FROM ATTEND_SEM UNION SELECT * FROM ATTENDREC) T1 ");
        stb.append("  GROUP BY ");
        stb.append("      SCHREGNO, ");
        stb.append("      GRADE, ");
        stb.append("      HR_CLASS, ");
        stb.append("      ATTENDNO, ");
        stb.append("      NAME_SHOW ");
        if (!OUTPUTDATA.equals(_param._output)) {
            stb.append("      ,YEAR ");
            stb.append("      ,SCHOOLCD ");
        }
        stb.append("  ORDER BY ");
        stb.append("      GRADE, ");
        stb.append("      HR_CLASS, ");
        stb.append("      ATTENDNO, ");
        stb.append("      SCHREGNO ");
        if (!OUTPUTDATA.equals(_param._output)) {
            stb.append("      ,YEAR ");
            stb.append("      ,SCHOOLCD DESC ");
        }
        return stb.toString();
    }
}

// eof
