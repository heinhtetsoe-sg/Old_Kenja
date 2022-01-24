// kanji=漢字
/*
 * $Id: 6c6c65fa74571bb7a4c776156904aef457a7c9af $
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
 * @version $Id: 6c6c65fa74571bb7a4c776156904aef457a7c9af $
 */
public class KNJE150 extends AbstractXls {

    private static final Log log = LogFactory.getLog("KNJE150.class");
    
    private boolean _hasData;
    
    private static String SYUKKETU = "2";
    
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
                if (0 != studentList.size()) {
                    for (final ListIterator it = studentList.listIterator(studentList.size()); it.hasPrevious();) {
                        final Student s = (Student) it.previous();
                        if (null != s._schregno && s._schregno.equals(schregno)) {
                            student = s;
                            break;
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
                    final String column = meta.getColumnName(i);
                    final String value = rsXls.getString(column);
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
        if (SYUKKETU.equals(_param._outDiv)) {
            for (int i = 0; i < _maxRecord; i++) {
                retList.add("年次");
                retList.add("授業日数");
                retList.add("出席停止・忌引等の日数");
                retList.add("留学中の授業日数");
                retList.add("出席しなければならない日数");
                retList.add("出席日数");
                retList.add("欠席日数");
                retList.add("遅刻");
                retList.add("早退");
            }
        } else {
            for (int i = 0; i < _maxRecord; i++) {
                retList.add("年次");
                retList.add("科目名");
                retList.add("評定");
                retList.add("修得単位数");
            }
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
        if (SYUKKETU.equals(_param._outDiv)) {
            cols2 = new String[]{
                    "ANNUAL",
                    "LESSON",
                    "SUSPEND_MOURNING",
                    "ABROAD",
                    "REQUIREPRESENT",
                    "PRESENT",
                    "ABSENT",
                    "LATE",
                    "EARLY",};
        } else {
            cols2 = new String[]{
                    "ANNUAL",
                    "SUBCLASSNAME",
                    "VALUATION",
                    "GET_CREDIT",};
        }
        return cols2;
    }

    protected String getSql() {
        final String sql;
        if (SYUKKETU.equals(_param._outDiv)) {
            sql = getAttendrecSql();
        } else {
            sql = getStudyrecSql();
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
        final String _year;
        final String _gakki;
        final boolean _header;
        final String _templatePath;
        final String _outDiv;
        final String _useCurriculumcd;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _gakki = request.getParameter("GAKKI");
            _header = true;
            _templatePath = request.getParameter("TEMPLATE_PATH");
            _outDiv = request.getParameter("OUT_DIV");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
        }
    }
    
    //出欠の記録
    private String getAttendrecSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T2.NAME_SHOW, ");
        stb.append("     T3.ANNUAL, ");
        stb.append("     VALUE(T3.CLASSDAYS,0) - VALUE(T3.OFFDAYS,0) - VALUE(T3.ABROAD,0) AS LESSON, ");
        stb.append("     VALUE(T3.SUSPEND,0) + VALUE(T3.MOURNING,0) AS SUSPEND_MOURNING, ");
        stb.append("     VALUE(T3.ABROAD,0) AS ABROAD, ");
        stb.append("     VALUE(T3.REQUIREPRESENT,0) AS REQUIREPRESENT, ");
        stb.append("     VALUE(T3.PRESENT,0) AS PRESENT, ");
        stb.append("     VALUE(T3.SICK,0) + VALUE(T3.ACCIDENTNOTICE,0) + VALUE(T3.NOACCIDENTNOTICE,0) AS ABSENT, ");
        stb.append("     '' AS LATE, ");
        stb.append("     '' AS EARLY ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_ATTENDREC_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' AND ");
        stb.append("     T1.SEMESTER = '" + _param._gakki + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T3.ANNUAL ");
        return stb.toString();
    }
    
    //学習の記録
    private String getStudyrecSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append("");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T2.NAME_SHOW, ");
        stb.append("     T3.ANNUAL, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T3.CLASSCD, ");
            stb.append("     T3.SCHOOL_KIND, ");
            stb.append("     T3.CURRICULUM_CD, ");
        }
        stb.append("     T3.SUBCLASSCD, ");
        stb.append("     L1.SUBCLASSNAME, ");
        stb.append("     T3.VALUATION, ");
        stb.append("     T3.GET_CREDIT ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_STUDYREC_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN SUBCLASS_MST L1 ON L1.SUBCLASSCD = T3.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("      AND L1.CLASSCD = T3.CLASSCD ");
            stb.append("      AND L1.SCHOOL_KIND = T3.SCHOOL_KIND ");
            stb.append("      AND L1.CURRICULUM_CD = T3.CURRICULUM_CD ");
        }
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' AND ");
        stb.append("     T1.SEMESTER = '" + _param._gakki + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T3.ANNUAL, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T3.CLASSCD, ");
            stb.append("     T3.SCHOOL_KIND, ");
            stb.append("     T3.CURRICULUM_CD, ");
        }
        stb.append("     T3.SUBCLASSCD ");
        return stb.toString();
    }
}

// eof
