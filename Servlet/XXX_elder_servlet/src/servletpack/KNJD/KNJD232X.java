// kanji=漢字
/*
 * $Id: 422be2ce198011584110a4b9192652c6be3da508 $
 *
 * 作成日: 2011/03/03 11:12:49 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.AbstractXls;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;

import jp.co.alp.kenja.common.dao.SQLUtils;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: 422be2ce198011584110a4b9192652c6be3da508 $
 */
public class KNJD232X extends AbstractXls {

    private static final Log log = LogFactory.getLog("KNJD232X.class");

    private static final String SYUKKETUJOKYO = "3";

    private boolean _hasData;

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

    protected List getXlsDataList() throws SQLException {
        final String sql = getSql();
        PreparedStatement psXls = null;
        ResultSet rsXls = null;
        final List studentList = new ArrayList();
        try {
            psXls = _db2.prepareStatement(sql);
            rsXls = psXls.executeQuery();
            int no = 1;
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
                if (SYUKKETUJOKYO.equals(_param._excelKind)) {
                    dataMap.put("NUMBER", String.valueOf(no));
                    no += 1;
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
                if (!SYUKKETUJOKYO.equals(_param._excelKind)) {
                    final String[] cols2 = getCols2();
                    for (int i = 0; i < student._record.size(); i++) {
                        final Map dataMap = (Map) student._record.get(i);
                        for (int j = 0; j < cols2.length; j++) {
                            final String val = (String) dataMap.get(cols2[j]);
                            xlsData.add(val);
                        }
                    }
                }
                dataList.add(xlsData);
            }
        } catch (Exception e) {
            log.error("Exception!", e);
        }
        return dataList;
    }


    /** XLSデータ出力 */
    protected void outPutXls(final HttpServletResponse response, final boolean header) throws IOException {
        //出力用のシート
        HSSFSheet outPutSheet = _tmpBook.getSheetAt(1);
        HSSFRow setRow;

        //ヘッダの行の書式を使用する為
        HSSFRow headerRow;
        int hedCol = 0;
        headerRow = outPutSheet.getRow(0);
        setRow = outPutSheet.getRow(1);
        for (final Iterator itHead = _headList.iterator(); itHead.hasNext();) {
            final String setXlsHedData = (String) itHead.next();
            setCellData(setRow, headerRow, hedCol++, setXlsHedData);
        }

        HSSFCell cell;
        hedCol = 0;
        setRow = outPutSheet.getRow(0);
        cell = setRow.getCell((short) hedCol);
        if (cell == null) {
            cell = setRow.createCell((short) hedCol);
        }
        HSSFCell cell1 = setRow.getCell((short) (hedCol + 1));
        if (cell1 == null) {
            cell1 = setRow.createCell((short) (hedCol + 1));
        }
        cell.setCellStyle(cell1.getCellStyle());
        cell.setCellValue(_param._contents);

        //最初の行の書式を使用する為
        HSSFRow firstRow = outPutSheet.getRow(2);
        if (firstRow == null) {
            firstRow = outPutSheet.createRow(2);
        }
        int line = 1;
        for (final Iterator iter = _dataList.iterator(); iter.hasNext();) {
            final List xlsData = (List) iter.next();
            final int rowLine = header ? line + 1 : line;
            setRow = outPutSheet.getRow(rowLine);
            // firstRow = line == 1 ? outPutSheet.getRow(line + 1) : firstRow;
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

    protected List getHeadData() {
        final List retList = new ArrayList();
        if (SYUKKETUJOKYO.equals(_param._excelKind)) {
            retList.add("No.");
            retList.add("組");
            retList.add("出席番号");
            retList.add("学籍番号");
            retList.add("氏名");
            retList.add("遅刻");
            retList.add("早退");
            retList.add("欠席");
            retList.add("欠課数");
        } else {
            retList.add("学籍番号");
            retList.add("学年");
            retList.add("組");
            retList.add("出席番号");
            retList.add("氏名");
            retList.add("履修単位数累計");
            retList.add("修得単位数累計");
            retList.add("評定平均");
            for (int i = 0; i < _maxRecord; i++) {
                retList.add("科目名");
                retList.add("評定");
                retList.add("見込単位数");
                retList.add("履修単位数");
                retList.add("修得単位数");
            }
        }
        return retList;
    }

    protected String[] getCols() {
        return null;
    }

    protected String[] getCols1() {
        final String[] cols1;
        if (SYUKKETUJOKYO.equals(_param._excelKind)) {
            cols1 = new String[]{
                    "NUMBER",
                    "HR_CLASS",
                    "ATTENDNO",
                    "SCHREGNO",
                    "NAME",
                    "LATE",
                    "EARLY",
                    "NOTICE",
                    "NOTICE_LATE",
            };
        } else {
            cols1 = new String[]{
                    "SCHREGNO",
                    "GRADE",
                    "HR_CLASS",
                    "ATTENDNO",
                    "NAME",
                    "ST_COMP",
                    "ST_GET_ADD",
                    "VALUATION",
            };
        }
        return cols1;
    }

    protected String[] getCols2() {
        final String[] cols2 = new String[]{
                "SUBCLASSNAME",
                "VALUE",
                "CREDITS",
                "COMP_CREDIT",
                "GET_CREDIT",};
        return cols2;
    }

    protected String getSql() {
        if ("1".equals(_param._excelKind)) {
            return select1();
        } else if ("2".equals(_param._excelKind)) {
            return select2();
        }
        final String[] month = (String[]) _param.attend_month.toArray(new String[0]);
        return select3(_param.attend_seme, month, _param.attend_sdate, _param._knjSchoolMst);
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
        final String _gakki;
        final String _year;
        final String _ctrlSemester;
        final boolean _header;
        final String _templatePath;
        final String _mode;
        final String _excelKind;
        final String _contents;

        final String _gakki2;
        final String _grade;
        final String _date;
        final String _assess1;
        final String _assess2;
        final String _count2;
        final String _unstudy2;
        final String _assessAve2;
        final int _late5;
        final int _early5;
        final int _absent5;
        final int _subclassAbsent5;
        final String _useCurriculumcd;
        final String _useVirus;
        final String _useKoudome;

        String attend_sdate = "";
        String attend_seme = "";
        List attend_month = new ArrayList();
        KNJSchoolMst _knjSchoolMst;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _gakki = request.getParameter("GAKKI");
            _ctrlSemester = _gakki;
            _header = true; // request.getParameter("HEADER") == null ? false : true;
            _templatePath = request.getParameter("TEMPLATE_PATH");
            _mode = request.getParameter("XLS_MODE");
            _excelKind = request.getParameter("EXCEL_KIND");

            final String title;
            if ("1".equals(_excelKind)) {
                title = "成績判定会議資料・成績優良者詳細リスト";
            } else if ("2".equals(_excelKind)) {
                title = "成績判定会議資料・成績不振者詳細リスト";
            } else {
                title = "成績判定会議資料・出欠状況不振者";
            }
            _contents = KenjaProperties.gengou(Integer.parseInt(_year)) + "年度　" + title;

            _gakki2 =  request.getParameter("GAKKI2");
            _grade =  request.getParameter("GRADE");
            _date =  request.getParameter("DATE").replace('/', '-');

            _assess1 = request.getParameter("ASSESS1");

            _assess2 = request.getParameter("ASSESS2");
            _count2 = request.getParameter("COUNT2");
            _unstudy2 = request.getParameter("UNSTUDY2");
            _assessAve2 = request.getParameter("ASSESS_AVE2");

            _late5 = null == request.getParameter("LATE5") || "".equals(request.getParameter("LATE5")) ? 99999 : Integer.parseInt(request.getParameter("LATE5"));
            _early5 = null == request.getParameter("EARLY5") || "".equals(request.getParameter("EARLY5")) ? 99999 : Integer.parseInt(request.getParameter("YEAR"));
            _absent5 = null == request.getParameter("ABSENT5") || "".equals(request.getParameter("ABSENT5")) ? 99999 : Integer.parseInt(request.getParameter("ABSENT5"));
            _subclassAbsent5 = null == request.getParameter("SUBCLASS_ABSENT5") || "".equals(request.getParameter("SUBCLASS_ABSENT5")) ? 99999 : Integer.parseInt(request.getParameter("SUBCLASS_ABSENT5"));
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");

            if (SYUKKETUJOKYO.equals(_excelKind)) {
                setAttendDate(db2);
            }
        }

        public void setAttendDate(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(getAttendDate());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String tmp_attend_sdate = rs.getString("MAX_YEAR") + "-" + rs.getString("MONTH") +"-" + rs.getString("MAX_APP");
                    if (_date.compareTo(tmp_attend_sdate) < 0) break;
                    attend_month.add(rs.getString("MONTH"));
                    attend_sdate = tmp_attend_sdate;
                    attend_seme = rs.getString("SEMESTER");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            if ("".equals(attend_sdate)) {
                final String sql = "SELECT SDATE FROM SEMESTER_MST WHERE YEAR='" + _year + "' AND SEMESTER='1' ";
                attend_sdate = getOne(db2, sql);    //学期開始日
            } else {
                final String sql = "VALUES Add_days(date('" + attend_sdate + "'), 1) ";
                attend_sdate = getOne(db2, sql);    //次の日
            }

            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }

        private String getOne(final DB2UDB db2, final String sql) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String s = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    s = rs.getString(1);
                }
            } catch (Exception e) {
                log.error(e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return s;
        }

        //出欠集計開始日付などを取得
        private String getAttendDate() {
            final StringBuffer stb = new StringBuffer();
            stb.append("SELECT SEMESTER ");
            stb.append("      ,MAX(CASE WHEN MONTH BETWEEN '01' AND '03' THEN RTRIM(CHAR(INT(YEAR)+1)) ELSE YEAR END) AS MAX_YEAR ");
            stb.append("      ,MONTH ");
            stb.append("      ,MAX(APPOINTED_DAY) AS MAX_APP ");
            stb.append("FROM   ATTEND_SEMES_DAT ");
            stb.append("WHERE  YEAR='" + _year + "' ");
            if ("9".equals(_gakki2)) {
                stb.append("   AND SEMESTER != '" + _gakki2 + "' ");
            } else {
                stb.append("   AND SEMESTER <= '" + _gakki2 + "' ");
            }
            stb.append("GROUP BY SEMESTER,MONTH ");
            stb.append("ORDER BY 1,2,3 ");
            return stb.toString();
        }
    }


    //（成績優良者）
    private String select1() {
        final String value = _param._gakki2.equals("9") ? "GRAD_VALUE" : "SEM" + _param._gakki2 + "_VALUE";
        final String semester = (_param._gakki2.equals("9")) ? _param._ctrlSemester : _param._gakki2;

        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH ");
        stb.append(" SCHNO AS (SELECT ");
        stb.append("             T2.SCHREGNO, ");
        stb.append("             T3.NAME, ");
        stb.append("             T2.GRADE, ");
        stb.append("             T2.HR_CLASS, ");
        stb.append("             T2.ATTENDNO,T2.COURSECD,T2.MAJORCD,T2.COURSECODE ");
        stb.append("         FROM ");
        stb.append("             SCHREG_REGD_DAT T2, ");
        stb.append("             SCHREG_BASE_MST T3 ");
        stb.append("         WHERE ");
        stb.append("             T2.YEAR     = '" + _param._year + "' AND ");
        stb.append("             T2.GRADE    = '" + _param._grade + "' AND ");
        stb.append("             T2.SEMESTER = '" + semester + "' AND ");
        stb.append("             T2.SCHREGNO = T3.SCHREGNO AND ");
        stb.append("             NOT (T3.GRD_DIV  IS NOT NULL AND ");
        stb.append("                  T3.GRD_DATE IS NOT NULL AND ");
        stb.append("                  T3.GRD_DIV IN ('2','3') AND ");
        stb.append("                  T3.GRD_DATE < '" + _param._date + "') ");
        stb.append(" ) ");

        stb.append(" ,RECORD AS (SELECT ");
        stb.append("                 T1.SCHREGNO, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("                 T1.CLASSCD, ");
            stb.append("                 T1.SCHOOL_KIND, ");
            stb.append("                 T1.CURRICULUM_CD, ");
        }
        stb.append("                 T1.SUBCLASSCD, ");
        stb.append("                 T2.SUBCLASSNAME, ");
        stb.append("                 " + value + " AS VALUE, ");
        stb.append("                 T4.CREDITS, ");
        stb.append("                 T1.COMP_CREDIT, ");
        stb.append("                 T1.GET_CREDIT, ");
        stb.append("                 T3.VALUATION ");
        stb.append("             FROM ");
        stb.append("                 RECORD_DAT T1 ");
        stb.append("             LEFT OUTER JOIN SUBCLASS_MST T2 ON T1.SUBCLASSCD=T2.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("                 AND T1.CLASSCD=T2.CLASSCD ");
            stb.append("                 AND T1.SCHOOL_KIND=T2.SCHOOL_KIND ");
            stb.append("                 AND T1.CURRICULUM_CD=T2.CURRICULUM_CD ");
        }
        stb.append("             LEFT OUTER JOIN (SELECT ");
        stb.append("                                 T2.SCHREGNO, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("                                 T1.CLASSCD , ");
            stb.append("                                 T1.SCHOOL_KIND , ");
            stb.append("                                 T1.CURRICULUM_CD , ");
        }
        stb.append("                                 T1.SUBCLASSCD , ");
        stb.append("                                 T1.CREDITS ");
        stb.append("                              FROM ");
        stb.append("                                 CREDIT_MST T1, ");
        stb.append("                                 SCHNO T2 ");
        stb.append("                              WHERE ");
        stb.append("                                 T1.YEAR         = '" + _param._year + "' AND ");
        stb.append("                                 T1.GRADE        = T2.GRADE       AND ");
        stb.append("                                 T1.COURSECD     = T2.COURSECD    AND ");
        stb.append("                                 T1.MAJORCD      = T2.MAJORCD     AND ");
        stb.append("                                 T1.COURSECODE   = T2.COURSECODE ");
        stb.append("                             ) T4 ON  T1.SCHREGNO   = T4.SCHREGNO ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("                                  AND T1.CLASSCD = T4.CLASSCD, ");
            stb.append("                                  AND T1.SCHOOL_KIND = T4.SCHOOL_KIND, ");
            stb.append("                                  AND T1.CURRICULUM_CD = T4.CURRICULUM_CD, ");
        }
        stb.append("                                  AND T1.SUBCLASSCD = T4.SUBCLASSCD, ");
        stb.append("             (SELECT ");
        stb.append("                 SCHREGNO, ");
        stb.append("                 DECIMAL(ROUND(DECIMAL(AVG(FLOAT(" + value + ")),5,2),1),5,1) AS VALUATION ");
        stb.append("             FROM ");
        stb.append("                 RECORD_DAT T1 ");
        stb.append("             WHERE ");
        stb.append("                 YEAR = '" + _param._year + "'  AND ");
        stb.append("                 SUBSTR(SUBCLASSCD,1,2) <= '89'  AND ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("                 CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD NOT IN (SELECT ");
        } else {
            stb.append("                 SUBCLASSCD NOT IN (SELECT ");
        }
        if(_param._gakki2.equals("9")) {
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("                                     ATTEND_CLASSCD || ATTEND_SCHOOL_KIND || ATTEND_CURRICULUM_CD || ATTEND_SUBCLASSCD ");
            } else {
                stb.append("                                     ATTEND_SUBCLASSCD ");
            }
        } else {
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("                                     COMBINED_CLASSCD || COMBINED_SCHOOL_KIND || COMBINED_CURRICULUM_CD || COMBINED_SUBCLASSCD ");
            } else {
                stb.append("                                     COMBINED_SUBCLASSCD ");
            }
        }
        stb.append("                                   FROM ");
        stb.append("                                         SUBCLASS_REPLACE_COMBINED_DAT ");
        stb.append("                                   WHERE ");
        stb.append("                                         YEAR = '" + _param._year + "' ");
        stb.append("                                  ) AND ");
        stb.append("                 EXISTS(SELECT ");
        stb.append("                             'X' ");
        stb.append("                         FROM ");
        stb.append("                             SCHNO T2 ");
        stb.append("                         WHERE ");
        stb.append("                             T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("                         ) ");
        stb.append("             GROUP BY ");
        stb.append("                 T1.SCHREGNO ");
        stb.append("             HAVING ");
        stb.append("                 " + _param._assess1 + " <= ROUND(AVG(FLOAT(" + value + ")) * 10 ,0) / 10) T3 ");
        stb.append("             WHERE ");
        stb.append("                 T1.YEAR = '" + _param._year + "' AND ");
        stb.append("                 SUBSTR(T1.SUBCLASSCD,1,2) <= '89' AND ");
        stb.append("                 T1.SCHREGNO = T3.SCHREGNO AND ");
        stb.append("                 EXISTS(SELECT ");
        stb.append("                             'X' ");
        stb.append("                         FROM ");
        stb.append("                             SCHNO T3 ");
        stb.append("                         WHERE ");
        stb.append("                             T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("                         ) ");
        stb.append(" ) ");

        stb.append(" , STUDYREC AS (SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     SUM(VALUE(T1.COMP_CREDIT, 0)) AS ST_COMP, ");
        stb.append("     SUM(VALUE(T1.GET_CREDIT, 0)) + SUM(VALUE(T1.ADD_CREDIT, 0)) AS ST_GET_ADD ");
        stb.append(" FROM ");
        stb.append("     SCHREG_STUDYREC_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR <= '" + _param._year + "' ");
        stb.append(" GROUP BY ");
        stb.append("     T1.SCHREGNO ");
        stb.append(" ) ");

        stb.append(" SELECT ");
        stb.append("     T2.SCHREGNO, ");
        stb.append("     T2.GRADE, ");
        stb.append("     T2.HR_CLASS, ");
        stb.append("     T2.ATTENDNO, ");
        stb.append("     T2.NAME, ");
        stb.append("     T1.SUBCLASSNAME, ");
        stb.append("     T1.VALUE, ");
        stb.append("     T1.CREDITS, ");
        stb.append("     T1.COMP_CREDIT, ");
        stb.append("     T1.GET_CREDIT, ");
        stb.append("     T1.VALUATION, ");
        stb.append("     T3.ST_COMP, ");
        stb.append("     T3.ST_GET_ADD ");
        stb.append(" FROM ");
        stb.append("     RECORD T1 ");
        stb.append(" INNER JOIN ");
        stb.append("     SCHNO T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append(" LEFT JOIN ");
        stb.append("     STUDYREC T3 ON T3.SCHREGNO = T1.SCHREGNO ");
        stb.append(" WHERE ");

        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD NOT IN (SELECT ");
        } else {
            stb.append("     T1.SUBCLASSCD NOT IN (SELECT ");
        }
        if (_param._gakki2.equals("9")) {
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("                                 ATTEND_CLASSCD || ATTEND_SCHOOL_KIND || ATTEND_CURRICULUM_CD || ATTEND_SUBCLASSCD ");
            } else {
                stb.append("                                 ATTEND_SUBCLASSCD ");
            }
        } else {
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("                                 COMBINED_CLASSCD || COMBINED_SCHOOL_KIND || COMBINED_CURRICULUM_CD || COMBINED_SUBCLASSCD ");
            } else {
                stb.append("                                 COMBINED_SUBCLASSCD ");
            }
        }
        stb.append("                           FROM ");
        stb.append("                                 SUBCLASS_REPLACE_COMBINED_DAT ");
        stb.append("                           WHERE ");
        stb.append("                                 YEAR = '" + _param._year + "' ");
        stb.append("                          ) ");
        stb.append(" ORDER BY ");
        stb.append("     T1.VALUATION DESC, ");
        stb.append("     T2.HR_CLASS, ");
        stb.append("     T2.ATTENDNO, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
        }
        stb.append("     T1.SUBCLASSCD ");
        return stb.toString();
    }

    //(成績不振者)
    private String select2() {
        final String value = (_param._gakki2.equals("9")) ? "GRAD_VALUE" : "SEM" + _param._gakki2 + "_VALUE";
        final String semester = (_param._gakki2.equals("9")) ? _param._ctrlSemester : _param._gakki2;

        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH ");
        stb.append(" SCHNO AS(SELECT ");
        stb.append("             T2.SCHREGNO, ");
        stb.append("             T3.NAME, ");
        stb.append("             T2.GRADE, ");
        stb.append("             T2.HR_CLASS, ");
        stb.append("             T2.ATTENDNO, ");
        stb.append("             T2.COURSECD, ");
        stb.append("             T2.MAJORCD, ");
        stb.append("             T2.COURSECODE ");
        stb.append("         FROM ");
        stb.append("             SCHREG_REGD_DAT T2, ");
        stb.append("             SCHREG_BASE_MST T3 ");
        stb.append("         WHERE ");
        stb.append("            T2.YEAR      = '" + _param._year + "' AND ");
        stb.append("            T2.GRADE     = '" + _param._grade + "' AND ");
        stb.append("            T2.SEMESTER  = '" + semester + "' AND ");
        stb.append("            T2.SCHREGNO  = T3.SCHREGNO AND ");
        stb.append("            NOT (T3.GRD_DIV IS NOT NULL  AND ");
        stb.append("                 T3.GRD_DATE IS NOT NULL AND ");
        stb.append("                 T3.GRD_DIV IN('2','3')  AND ");
        stb.append("                 T3.GRD_DATE < '" + _param._date + "') ");
        stb.append(" ) ");

        stb.append(" , BAD_SCHNO AS(SELECT ");
        stb.append("                     SCHREGNO ");
        stb.append("                 FROM ");
        stb.append("                     RECORD_DAT ");
        stb.append("                 WHERE ");
        stb.append("                     YEAR = '" + _param._year + "' AND ");
        stb.append("                     SUBSTR(SUBCLASSCD,1,2) <= '89'  AND ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("                CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD NOT IN (SELECT ");
        } else {
            stb.append("                SUBCLASSCD NOT IN (SELECT ");
        }
        if (_param._gakki2.equals("9")) {
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("                                     ATTEND_CLASSCD || ATTEND_SCHOOL_KIND || ATTEND_CURRICULUM_CD || ATTEND_SUBCLASSCD ");
            } else {
                stb.append("                                     ATTEND_SUBCLASSCD ");
            }
        } else {
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("                                     COMBINED_CLASSCD || COMBINED_SCHOOL_KIND || COMBINED_CURRICULUM_CD || COMBINED_SUBCLASSCD ");
            } else {
                stb.append("                                     COMBINED_SUBCLASSCD ");
            }
        }
        stb.append("                                   FROM ");
        stb.append("                                         SUBCLASS_REPLACE_COMBINED_DAT ");
        stb.append("                                   WHERE ");
        stb.append("                                         YEAR = '" + _param._year + "' ");
        stb.append("                                  ) AND ");
        stb.append("                     (" + value + " <= " + _param._assess2 + " OR " + value + " IS NULL) ");
        stb.append("                 GROUP BY ");
        stb.append("                     SCHREGNO ");
        stb.append("                 HAVING ");
        stb.append("                     " + _param._count2 + " <= COUNT(SUBCLASSCD) ");
        stb.append("                 UNION ");
        stb.append("                 SELECT ");
        stb.append("                     SCHREGNO ");
        stb.append("                 FROM ");
        stb.append("                     RECORD_DAT ");
        stb.append("                 WHERE ");
        stb.append("                     YEAR = '" + _param._year + "' AND ");
        stb.append("                     SUBSTR(SUBCLASSCD,1,2) <= '89'  AND ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("                                CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD NOT IN (SELECT ");
        } else {
            stb.append("                                SUBCLASSCD NOT IN (SELECT ");
        }
        if (_param._gakki2.equals("9")) {
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("                                                     ATTEND_CLASSCD || ATTEND_SCHOOL_KIND || ATTEND_CURRICULUM_CD || ATTEND_SUBCLASSCD ");
            } else {
                stb.append("                                                     ATTEND_SUBCLASSCD ");
            }
        } else {
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("                                                     COMBINED_CLASSCD || COMBINED_SCHOOL_KIND || COMBINED_CURRICULUM_CD || COMBINED_SUBCLASSCD ");
            } else {
                stb.append("                                                     COMBINED_SUBCLASSCD ");
            }
        }
        stb.append("                                                   FROM ");
        stb.append("                                                         SUBCLASS_REPLACE_COMBINED_DAT ");
        stb.append("                                                   WHERE ");
        stb.append("                                                         YEAR = '" + _param._year + "' ");
        stb.append("                                                  ) ");
        if (_param._gakki2.equals("9")) {
            stb.append("                     AND (COMP_CREDIT IS NULL OR COMP_CREDIT = 0) ");
            stb.append("                     AND (GET_CREDIT IS NULL OR GET_CREDIT = 0) ");
        } else {
            stb.append("                     AND (" + value + " IS NULL OR " + value + " = 0 ) ");
        }
        stb.append("                 GROUP BY ");
        stb.append("                     SCHREGNO ");
        stb.append("                 HAVING ");
        stb.append("                     " + _param._unstudy2 + " <= COUNT(SUBCLASSCD) ");
        stb.append("                 UNION ");
        stb.append("                 SELECT ");
        stb.append("                     SCHREGNO ");
        stb.append("                 FROM ");
        stb.append("                     RECORD_DAT ");
        stb.append("                 WHERE ");
        stb.append("                     YEAR = '" + _param._year + "' AND ");
        stb.append("                     SUBSTR(SUBCLASSCD,1,2) <= '89' AND ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("                                CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD NOT IN (SELECT ");
        } else {
            stb.append("                                SUBCLASSCD NOT IN (SELECT ");
        }
        if (_param._gakki2.equals("9")) {
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("                                                     ATTEND_CLASSCD || ATTEND_SCHOOL_KIND || ATTEND_CURRICULUM_CD || ATTEND_SUBCLASSCD ");
            } else {
                stb.append("                                                     ATTEND_SUBCLASSCD ");
            }
        } else {
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("                                                     COMBINED_CLASSCD || COMBINED_SCHOOL_KIND || COMBINED_CURRICULUM_CD || COMBINED_SUBCLASSCD ");
            } else {
                stb.append("                                                     COMBINED_SUBCLASSCD ");
            }
        }
        stb.append("                                                   FROM ");
        stb.append("                                                         SUBCLASS_REPLACE_COMBINED_DAT ");
        stb.append("                                                   WHERE ");
        stb.append("                                                         YEAR = '" + _param._year + "' ");
        stb.append("                                                  ) ");
        stb.append("                 GROUP BY ");
        stb.append("                     SCHREGNO ");
        stb.append("                 HAVING ");
        stb.append("                    " + _param._assessAve2 + " >= ROUND(AVG(FLOAT(" + value + ")) * 10, 0) / 10 ");
        stb.append(" ) ");

        stb.append(" , RECORD AS (SELECT ");
        stb.append("                 T1.SCHREGNO, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("                 T1.CLASSCD, ");
            stb.append("                 T1.SCHOOL_KIND, ");
            stb.append("                 T1.CURRICULUM_CD, ");
        }
        stb.append("                 T1.SUBCLASSCD, ");
        stb.append("                 T3.SUBCLASSNAME, ");
        stb.append("                 T1." + value + " AS VALUE, ");
        stb.append("                 T4.CREDITS, ");
        stb.append("                 T1.COMP_CREDIT, ");
        stb.append("                 T1.GET_CREDIT, ");
        stb.append("                 T5.VALUATION ");
        stb.append("             FROM ");
        stb.append("                 RECORD_DAT T1 ");
        stb.append("                 INNER JOIN BAD_SCHNO T2 ON T1.SCHREGNO = T2.SCHREGNO ");
        stb.append("                 LEFT JOIN SUBCLASS_MST T3 ON T1.SUBCLASSCD = T3.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("                 AND T1.CLASSCD = T3.CLASSCD ");
            stb.append("                 AND T1.SCHOOL_KIND = T3.SCHOOL_KIND ");
            stb.append("                 AND T1.CURRICULUM_CD = T3.CURRICULUM_CD ");
        }
        stb.append("                 LEFT JOIN (SELECT ");
        stb.append("                               T2.SCHREGNO, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("                               T1.CLASSCD , ");
            stb.append("                               T1.SCHOOL_KIND , ");
            stb.append("                               T1.CURRICULUM_CD , ");
        }
        stb.append("                               T1.SUBCLASSCD , ");
        stb.append("                               T1.CREDITS ");
        stb.append("                            FROM ");
        stb.append("                               CREDIT_MST T1, ");
        stb.append("                               SCHNO T2 ");
        stb.append("                            WHERE ");
        stb.append("                               T1.YEAR          = '" + _param._year + "' AND ");
        stb.append("                               T1.GRADE         = T2.GRADE AND ");
        stb.append("                               T1.COURSECD      = T2.COURSECD AND ");
        stb.append("                               T1.MAJORCD       = T2.MAJORCD AND ");
        stb.append("                               T1.COURSECODE    = T2.COURSECODE ");
        stb.append("                            ) T4 ON  T1.SCHREGNO   = T4.SCHREGNO ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("                                 AND T1.CLASSCD = T4.CLASSCD ");
            stb.append("                                 AND T1.SCHOOL_KIND = T4.SCHOOL_KIND ");
            stb.append("                                 AND T1.CURRICULUM_CD = T4.CURRICULUM_CD ");
        }
        stb.append("                                 AND T1.SUBCLASSCD = T4.SUBCLASSCD ");
        stb.append("                 LEFT JOIN (SELECT ");
        stb.append("                                SCHREGNO, ");
        stb.append("                                DECIMAL(ROUND(DECIMAL(AVG(FLOAT(" + value + ")),5,2),1),5,1) AS VALUATION ");
        stb.append("                            FROM ");
        stb.append("                                RECORD_DAT T1 ");
        stb.append("                            WHERE ");
        stb.append("                                YEAR = '" + _param._year + "'  AND ");
        stb.append("                                SUBSTR(SUBCLASSCD, 1, 2) <= '89'  AND ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("                                CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD NOT IN (SELECT ");
        } else {
            stb.append("                                SUBCLASSCD NOT IN (SELECT ");
        }
        if (_param._gakki2.equals("9")) {
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("                                                   ATTEND_CLASSCD || ATTEND_SCHOOL_KIND || ATTEND_CURRICULUM_CD || ATTEND_SUBCLASSCD ");
            } else {
                stb.append("                                                   ATTEND_SUBCLASSCD ");
            }
        } else {
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("                                                   COMBINED_CLASSCD || COMBINED_SCHOOL_KIND || COMBINED_CURRICULUM_CD || COMBINED_SUBCLASSCD ");
            } else {
                stb.append("                                                   COMBINED_SUBCLASSCD ");
            }
        }
        stb.append("                                                   FROM ");
        stb.append("                                                         SUBCLASS_REPLACE_COMBINED_DAT ");
        stb.append("                                                   WHERE ");
        stb.append("                                                         YEAR = '" + _param._year + "' ");
        stb.append("                                                  ) AND ");
        stb.append("                                EXISTS(SELECT ");
        stb.append("                                            'X' ");
        stb.append("                                        FROM ");
        stb.append("                                            SCHNO T2 ");
        stb.append("                                        WHERE ");
        stb.append("                                            T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("                                        ) ");
        stb.append("                            GROUP BY ");
        stb.append("                                T1.SCHREGNO ");
        stb.append("                            ) T5 ON T1.SCHREGNO = T5.SCHREGNO ");
        stb.append("             WHERE ");
        stb.append("                 T1.YEAR = '" + _param._year + "' AND ");
        stb.append("                 SUBSTR(T1.SUBCLASSCD,1,2) <= '89' AND ");
        stb.append("                 EXISTS(SELECT ");
        stb.append("                             'X' ");
        stb.append("                         FROM ");
        stb.append("                             SCHNO T3 ");
        stb.append("                         WHERE ");
        stb.append("                             T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("                         ) ");
        stb.append(" ) ");

        stb.append(" , STUDYREC AS (SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     SUM(VALUE(T1.COMP_CREDIT, 0)) AS ST_COMP, ");
        stb.append("     SUM(VALUE(T1.GET_CREDIT, 0)) + SUM(VALUE(T1.ADD_CREDIT, 0)) AS ST_GET_ADD ");
        stb.append(" FROM ");
        stb.append("     SCHREG_STUDYREC_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR <= '" + _param._year + "' ");
        stb.append(" GROUP BY ");
        stb.append("     T1.SCHREGNO ");
        stb.append(" ) ");

        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T2.GRADE, ");
        stb.append("     T2.HR_CLASS, ");
        stb.append("     T2.ATTENDNO, ");
        stb.append("     T2.NAME, ");
        stb.append("     T1.SUBCLASSNAME, ");
        stb.append("     T1.VALUE, ");
        stb.append("     T1.CREDITS, ");
        stb.append("     T1.COMP_CREDIT, ");
        stb.append("     T1.GET_CREDIT, ");
        stb.append("     T1.VALUATION, ");
        stb.append("     T3.ST_COMP, ");
        stb.append("     T3.ST_GET_ADD ");
        stb.append(" FROM ");
        stb.append("     RECORD T1 ");
        stb.append(" INNER JOIN SCHNO    T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append(" LEFT  JOIN STUDYREC T3 ON T3.SCHREGNO = T1.SCHREGNO ");
        stb.append(" WHERE ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD NOT IN (SELECT ");
        } else {
            stb.append("     T1.SUBCLASSCD NOT IN (SELECT ");
        }
        if (_param._gakki2.equals("9")) {
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("                                 ATTEND_CLASSCD || ATTEND_SCHOOL_KIND || ATTEND_CURRICULUM_CD || ATTEND_SUBCLASSCD ");
            } else {
                stb.append("                                 ATTEND_SUBCLASSCD ");
            }
        } else {
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("                                 COMBINED_CLASSCD || COMBINED_SCHOOL_KIND || COMBINED_CURRICULUM_CD || COMBINED_SUBCLASSCD ");
            } else {
                stb.append("                                 COMBINED_SUBCLASSCD ");
            }
        }
        stb.append("                           FROM ");
        stb.append("                                 SUBCLASS_REPLACE_COMBINED_DAT ");
        stb.append("                           WHERE ");
        stb.append("                                 YEAR = '" + _param._year + "' ");
        stb.append("                          ) ");
        stb.append(" ORDER BY ");
        stb.append("     T1.VALUATION DESC, ");
        stb.append("     T2.HR_CLASS, ");
        stb.append("     T2.ATTENDNO, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
        }
        stb.append("     T1.SUBCLASSCD ");
        return stb.toString();
    }

    //出欠不振者ＣＳＶ
    //(科目別:対象学期までの累計)
    private String select3(final String attend_seme, final String[] month, final String attend_sdate, final KNJSchoolMst knjSchoolMst) {
        final String semester = (_param._gakki2.equals("9")) ? _param._ctrlSemester : _param._gakki2;
        final String absent_cov = null == knjSchoolMst._absentCov ? "" : knjSchoolMst._absentCov;
        final String absent_cov_late = null == knjSchoolMst._absentCovLate ? "" : knjSchoolMst._absentCovLate;

        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCHNO AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         COURSECD, ");
        stb.append("         MAJORCD, ");
        stb.append("         COURSECODE, ");
        stb.append("         GRADE, ");
        stb.append("         HR_CLASS, ");
        stb.append("         ATTENDNO, ");
        stb.append("         NAME ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT T1");
        stb.append("     LEFT JOIN ");
        stb.append("         SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ");
        stb.append("     WHERE ");
        stb.append("             YEAR     = '" + _param._year + "' ");
        stb.append("         AND SEMESTER = '" + semester + "' ");
        stb.append("         AND GRADE    = '" + _param._grade + "' ");
        stb.append(" ) ");

        stb.append(" ,ATTEND_SUBCLASS AS ( ");
        stb.append("     SELECT ");
        stb.append("         * ");
        stb.append("     FROM ");
        stb.append("         ATTEND_SUBCLASS_DAT ");
        stb.append("     WHERE ");
        stb.append("             YEAR     = '" + _param._year + "' ");
        stb.append("         AND SEMESTER = '" + attend_seme + "' ");
        stb.append("         AND MONTH IN " + SQLUtils.whereIn(true, month) + " ");
        stb.append(" ) ");

        stb.append(" ,SCHEDULE AS ( ");
        stb.append("     SELECT ");
        stb.append("         EXECUTEDATE, ");
        stb.append("         PERIODCD, ");
        stb.append("         CHAIRCD, ");
        stb.append("         DATADIV ");
        stb.append("     FROM ");
        stb.append("         SCH_CHR_DAT ");
        stb.append("     WHERE ");
        stb.append("             EXECUTEDATE BETWEEN '" + attend_sdate + "' AND '" + _param._date + "' ");
        stb.append("         AND PERIODCD != '0' ");
        stb.append(" ) ");

        stb.append(" ,T_attend_dat AS ( ");
        stb.append("     SELECT ");
        stb.append("         W1.SCHREGNO, ");
        stb.append("         W1.ATTENDDATE, ");
        stb.append("         W1.PERIODCD, ");
        stb.append("         CASE WHEN L1.ATSUB_REPL_DI_CD IS NOT NULL THEN L1.ATSUB_REPL_DI_CD ELSE L1.REP_DI_CD END AS DI_CD, ");
        stb.append("         L1.MULTIPLY ");
        stb.append("     FROM ");
        stb.append("         ATTEND_DAT W1 ");
        stb.append("            LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = W1.YEAR AND L1.DI_CD = W1.DI_CD ");
        stb.append("     WHERE ");
        stb.append("             W1.ATTENDDATE BETWEEN '" + attend_sdate + "' AND '" + _param._date + "' ");
        stb.append("         AND NOT EXISTS( SELECT ");
        stb.append("                             'X' ");
        stb.append("                         FROM ");
        stb.append("                             SCHREG_TRANSFER_DAT T7 ");
        stb.append("                         WHERE ");
        stb.append("                                 T7.SCHREGNO = W1.SCHREGNO ");
        stb.append("                             AND T7.TRANSFERCD IN('1','2') ");
        stb.append("                             AND W1.ATTENDDATE BETWEEN T7.TRANSFER_SDATE AND T7.TRANSFER_EDATE ) ");
        stb.append(" ) ");

        //テスト項目マスタの集計フラグの表
        stb.append(" , TEST_COUNTFLG AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.EXECUTEDATE, ");
        stb.append("         T1.PERIODCD, ");
        stb.append("         T1.CHAIRCD, ");
        stb.append("         '2' AS DATADIV ");
        stb.append("     FROM ");
        stb.append("         SCH_CHR_TEST T1, ");
        stb.append("         TESTITEM_MST_COUNTFLG_NEW T2 ");
        stb.append("     WHERE ");
        stb.append("             T2.YEAR       = T1.YEAR ");
        stb.append("         AND T2.SEMESTER   = T1.SEMESTER ");
        stb.append("         AND T2.TESTKINDCD = T1.TESTKINDCD ");
        stb.append("         AND T2.TESTITEMCD = T1.TESTITEMCD ");
        stb.append("         AND T2.COUNTFLG   = '0' "); //0：集計しない 0以外：集計する
        stb.append("     ) ");

        stb.append(" ,T_attend AS ( ");
        stb.append("     SELECT ");
        stb.append("         S1.SCHREGNO, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         S1.CLASSCD, ");
            stb.append("         S1.SCHOOL_KIND, ");
            stb.append("         S1.CURRICULUM_CD, ");
        }
        stb.append("         S1.SUBCLASSCD, ");
        stb.append("         S1.SEMESTER, ");
        stb.append("         COUNT(S3.SCHREGNO)  AS OFFDAYS, ");
        stb.append("         SUM(CASE S2.DI_CD WHEN  '1' THEN 1 WHEN '8'  THEN 1 ELSE 0 END)  AS  ABSENT, ");
        stb.append("         SUM(CASE S2.DI_CD WHEN  '2' THEN 1 WHEN '9'  THEN 1 ELSE 0 END)  AS  SUSPEND, ");
        stb.append("         SUM(CASE S2.DI_CD WHEN  '3' THEN 1 WHEN '10' THEN 1 ELSE 0 END)  AS  MOURNING, ");
        stb.append("         SUM(CASE S2.DI_CD WHEN  '4' THEN 1 WHEN '11' THEN 1 ELSE 0 END)  AS  SICK, ");
        stb.append("         SUM(CASE S2.DI_CD WHEN  '5' THEN 1 WHEN '12' THEN 1 ELSE 0 END)  AS  NOTICE, ");
        stb.append("         SUM(CASE S2.DI_CD WHEN  '6' THEN 1 WHEN '13' THEN 1 ELSE 0 END)  AS  NONOTICE, ");
        stb.append("         SUM(CASE S2.DI_CD WHEN '14' THEN 1 ELSE 0 END)  AS  NURSEOFF, ");
        stb.append("         SUM(CASE WHEN S2.DI_CD IN('15','23','24') THEN SMALLINT(VALUE(S2.MULTIPLY, '1')) ELSE 0 END)  AS  LATE,  ");
        stb.append("         SUM(CASE S2.DI_CD WHEN '16' THEN 1 ELSE 0 END)  AS  EARLY, ");
        stb.append("         SUM(CASE S2.DI_CD WHEN '19' THEN 1 WHEN '20' THEN 1 ELSE 0 END)  AS  VIRUS, ");
        stb.append("         SUM(CASE S2.DI_CD WHEN '25' THEN 1 WHEN '26' THEN 1 ELSE 0 END)  AS  KOUDOME ");
        stb.append("     FROM ");
        stb.append("         (SELECT ");
        stb.append("             T2.SCHREGNO, ");
        stb.append("             T1.EXECUTEDATE, ");
        stb.append("             T1.PERIODCD, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("             CLASSCD, ");
            stb.append("             SCHOOL_KIND, ");
            stb.append("             CURRICULUM_CD, ");
        }
        stb.append("             SUBCLASSCD, ");
        stb.append("             T2.SEMESTER ");
        stb.append("         FROM ");
        stb.append("             SCHEDULE T1, ");
        stb.append("             CHAIR_STD_DAT T2, ");
        stb.append("             CHAIR_DAT T3, ");
        stb.append("             SCHNO T4 ");
        stb.append("         WHERE ");
        stb.append("                 T1.CHAIRCD  = T3.CHAIRCD ");
        stb.append("             AND T3.YEAR     = '" + _param._year + "' ");
        stb.append("             AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ");
        stb.append("             AND T2.YEAR     = '" + _param._year + "' ");
        stb.append("             AND T2.SEMESTER = T3.SEMESTER ");
        stb.append("             AND T2.CHAIRCD  = T1.CHAIRCD ");
        stb.append("             AND T4.SCHREGNO = T2.SCHREGNO ");
        stb.append("             AND NOT EXISTS( SELECT ");
        stb.append("                                 'X' ");
        stb.append("                             FROM ");
        stb.append("                                 SCH_CHR_COUNTFLG T5 ");
        stb.append("                             WHERE ");
        stb.append("                                 T5.EXECUTEDATE  = T1.EXECUTEDATE AND ");
        stb.append("                                 T5.PERIODCD     = T1.PERIODCD AND ");
        stb.append("                                 T5.CHAIRCD      = T1.CHAIRCD AND ");
        stb.append("                                 T5.GRADE        = T4.GRADE AND ");
        stb.append("                                 T5.HR_CLASS     = T4.HR_CLASS AND ");
        stb.append("                                 T1.DATADIV      IN ('0','1') AND "); //テスト(DATADIV=2)以外
        stb.append("                                 T5.COUNTFLG     = '0' ");
        stb.append("                           ) ");
        stb.append("             AND NOT EXISTS(SELECT 'X' FROM TEST_COUNTFLG TEST ");
        stb.append("                             WHERE TEST.EXECUTEDATE = T1.EXECUTEDATE ");
        stb.append("                               AND TEST.PERIODCD    = T1.PERIODCD ");
        stb.append("                               AND TEST.CHAIRCD     = T1.CHAIRCD ");
        stb.append("                               AND TEST.DATADIV     = T1.DATADIV) "); //テスト(DATADIV=2)
        stb.append("             AND NOT EXISTS( SELECT ");
        stb.append("                                 'X' ");
        stb.append("                             FROM ");
        stb.append("                                 SCHREG_BASE_MST T6 ");
        stb.append("                             WHERE ");
        stb.append("                                 T6.SCHREGNO = T4.SCHREGNO AND ");
        stb.append("                                 ((T6.GRD_DIV IN('1','2','3') AND T6.GRD_DATE < T1.EXECUTEDATE) OR ");
        stb.append("                                  (T6.ENT_DIV IN('4','5')     AND T6.ENT_DATE > T1.EXECUTEDATE)) ");
        stb.append("                           ) ");
        stb.append("    AND NOT EXISTS(SELECT ");
        stb.append("                       'X' ");
        stb.append("                   FROM ");
        stb.append("                       ATTEND_DAT T4 ");
        stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = T4.YEAR AND L1.DI_CD = T4.DI_CD ");
        stb.append("                   WHERE ");
        stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
        stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
        stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
        stb.append("                       AND L1.REP_DI_CD = '27' "); // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
        stb.append("                  ) ");
        // 勤怠コード'28'は時間割にカウントしない
        stb.append("    AND NOT EXISTS(SELECT ");
        stb.append("                       'X' ");
        stb.append("                   FROM ");
        stb.append("                       ATTEND_DAT T4 ");
        stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = T4.YEAR AND L1.DI_CD = T4.DI_CD ");
        stb.append("                   WHERE ");
        stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
        stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
        stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
        stb.append("                       AND L1.REP_DI_CD = '28' ");
        stb.append("                  ) ");
        stb.append("     GROUP BY ");
        stb.append("         T2.SCHREGNO, ");
        stb.append("         T1.EXECUTEDATE, ");
        stb.append("         T1.PERIODCD, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         CLASSCD, ");
            stb.append("         SCHOOL_KIND, ");
            stb.append("         CURRICULUM_CD, ");
        }
        stb.append("         SUBCLASSCD, ");
        stb.append("         T2.SEMESTER)S1 ");
        stb.append("     LEFT JOIN T_attend_dat        S2 ON  S2.SCHREGNO   = S1.SCHREGNO ");
        stb.append("                                      AND S2.ATTENDDATE = S1.EXECUTEDATE ");
        stb.append("                                      AND S2.PERIODCD   = S1.PERIODCD ");
        stb.append("     LEFT JOIN SCHREG_TRANSFER_DAT S3 ON  S3.SCHREGNO   = S1.SCHREGNO ");
        stb.append("                                      AND S3.TRANSFERCD = '2' ");
        stb.append("                                      AND S1.EXECUTEDATE BETWEEN S3.TRANSFER_SDATE AND S3.TRANSFER_EDATE ");
        stb.append("     LEFT JOIN SCHREG_TRANSFER_DAT S4 ON  S4.SCHREGNO   = S1.SCHREGNO ");
        stb.append("                                      AND S4.TRANSFERCD = '1' ");
        stb.append("                                      AND S1.EXECUTEDATE BETWEEN S4.TRANSFER_SDATE AND S4.TRANSFER_EDATE ");
        stb.append("     GROUP BY ");
        stb.append("         S1.SCHREGNO, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         S1.CLASSCD, ");
            stb.append("         S1.SCHOOL_KIND, ");
            stb.append("         S1.CURRICULUM_CD, ");
        }
        stb.append("         S1.SUBCLASSCD, ");
        stb.append("         S1.SEMESTER ");
        stb.append(" ) ");

        stb.append(" ,ATTEND_SUM AS ( ");
        stb.append("     SELECT ");
        stb.append("         W1.SCHREGNO, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         W1.CLASSCD, ");
            stb.append("         W1.SCHOOL_KIND, ");
            stb.append("         W1.CURRICULUM_CD, ");
        }
        stb.append("         W1.SUBCLASSCD, ");
        stb.append("         W1.SEMESTER, ");
        stb.append("         SUM(VALUE(W1.SICK,0) + VALUE(W1.NURSEOFF,0) ");
        if ("1".equals(knjSchoolMst._subAbsent)) {
            stb.append(          "+ VALUE(W1.ABSENT,0)");
        }
        if ("1".equals(knjSchoolMst._subSuspend)) {
            stb.append(          "+ VALUE(W1.SUSPEND,0)");
        }
        if ("1".equals(knjSchoolMst._subMourning)) {
            stb.append(          "+ VALUE(W1.MOURNING,0)");
        }
        if ("1".equals(knjSchoolMst._subOffDays)) {
            stb.append(          "+ VALUE(W1.OFFDAYS,0)");
        }
        if ("1".equals(knjSchoolMst._subVirus)) {
            stb.append(          "+ VALUE(W1.VIRUS,0)");
        }
        if ("true".equals(_param._useKoudome)) {
            if ("1".equals(knjSchoolMst._subKoudome)) {
                stb.append(          "+ VALUE(W1.KOUDOME,0)");
            }
        }
        stb.append("            ) AS SICK, ");
        stb.append("         SUM(VALUE(W1.NOTICE,0))     NOTICE, ");
        stb.append("         SUM(VALUE(W1.NONOTICE,0))   NONOTICE, ");
        stb.append("         SUM(VALUE(W1.LATE,0))       LATE, ");
        stb.append("         SUM(VALUE(W1.EARLY,0))      EARLY ");
        stb.append("     FROM ");
        stb.append("         ATTEND_SUBCLASS W1, ");
        stb.append("         SCHNO W0 ");
        stb.append("     WHERE ");
        stb.append("         W1.SCHREGNO = W0.SCHREGNO ");
        stb.append("     GROUP BY ");
        stb.append("         W1.SCHREGNO, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         W1.CLASSCD, ");
            stb.append("         W1.SCHOOL_KIND, ");
            stb.append("         W1.CURRICULUM_CD, ");
        }
        stb.append("         W1.SUBCLASSCD, ");
        stb.append("         W1.SEMESTER ");
        stb.append("     UNION ALL ");
        stb.append("     SELECT ");
        stb.append("         W2.SCHREGNO, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         W2.CLASSCD, ");
            stb.append("         W2.SCHOOL_KIND, ");
            stb.append("         W2.CURRICULUM_CD, ");
        }
        stb.append("         W2.SUBCLASSCD, ");
        stb.append("         W2.SEMESTER, ");
        stb.append("         VALUE(W2.SICK,0) + VALUE(W2.NURSEOFF,0) ");

        if ("1".equals(knjSchoolMst._subAbsent)) {
            stb.append(          "+ VALUE(W2.ABSENT,0)");
        }
        if ("1".equals(knjSchoolMst._subSuspend)) {
            stb.append(          "+ VALUE(W2.SUSPEND,0)");
        }
        if ("1".equals(knjSchoolMst._subMourning)) {
            stb.append(          "+ VALUE(W2.MOURNING,0)");
        }
        if ("1".equals(knjSchoolMst._subOffDays)) {
            stb.append(          "+ VALUE(W2.OFFDAYS,0)");
        }
        if ("1".equals(knjSchoolMst._subVirus)) {
            stb.append(          "+ VALUE(W2.VIRUS,0)");
        }
        if ("true".equals(_param._useKoudome)) {
            if ("1".equals(knjSchoolMst._subKoudome)) {
                stb.append(          "+ VALUE(W2.KOUDOME,0)");
            }
        }
        stb.append("                AS SICK, ");
        stb.append("         VALUE(W2.NOTICE,0)      NOTICE, ");
        stb.append("         VALUE(W2.NONOTICE,0)    NONOTICE, ");
        stb.append("         VALUE(W2.LATE,0)        LATE, ");
        stb.append("         VALUE(W2.EARLY,0)       EARLY ");
        stb.append("     FROM ");
        stb.append("         T_attend W2 ");
        stb.append(" ) ");

        //学期毎に清算
        stb.append(" ,ATTEND_SUM2 AS ( ");
        stb.append("     SELECT ");
        stb.append("         SCHREGNO, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         CLASSCD, ");
            stb.append("         SCHOOL_KIND, ");
            stb.append("         CURRICULUM_CD, ");
        }
        stb.append("         SUBCLASSCD, ");
        stb.append("         SEMESTER, ");
        stb.append("         SUM(SICK)       SICK, ");
        stb.append("         SUM(NOTICE)     NOTICE, ");
        stb.append("         SUM(NONOTICE)   NONOTICE, ");
        stb.append("         SUM(LATE)       LATE, ");
        stb.append("         SUM(EARLY)      EARLY ");
        if (absent_cov.equals("3") && StringUtils.isNumeric(absent_cov_late) && 0 != Integer.parseInt(absent_cov_late)) {
            stb.append("      ,decimal((float(sum(LATE) + sum(EARLY)) / " + Integer.parseInt(absent_cov_late) + ") + (sum(NOTICE) + sum(NONOTICE) + sum(SICK)),4,1) as NOTICE_LATE ");
        } else if (absent_cov.equals("1") && StringUtils.isNumeric(absent_cov_late) && 0 != Integer.parseInt(absent_cov_late)) {
            stb.append("      ,((sum(LATE) + sum(EARLY)) / " + Integer.parseInt(absent_cov_late) + ") + (sum(NOTICE) + sum(NONOTICE) + sum(SICK)) as NOTICE_LATE ");
        } else {
            stb.append("      ,sum(NOTICE) + sum(NONOTICE) + sum(SICK) as NOTICE_LATE ");
        }
        stb.append("     FROM ");
        stb.append("         ATTEND_SUM ");
        stb.append("     GROUP BY ");
        stb.append("         SCHREGNO, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         CLASSCD, ");
            stb.append("         SCHOOL_KIND, ");
            stb.append("         CURRICULUM_CD, ");
        }
        stb.append("         SUBCLASSCD, ");
        stb.append("         SEMESTER ");
        stb.append(" ) ");

        //年間で清算
        stb.append(" ,ATTEND_SUM3 AS ( ");
        stb.append("     SELECT ");
        stb.append("         SCHREGNO, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         CLASSCD, ");
            stb.append("         SCHOOL_KIND, ");
            stb.append("         CURRICULUM_CD, ");
        }
        stb.append("         SUBCLASSCD, ");
        stb.append("         SUM(SICK)       SICK, ");
        stb.append("         SUM(NOTICE)     NOTICE, ");
        stb.append("         SUM(NONOTICE)   NONOTICE, ");
        stb.append("         SUM(LATE)       LATE, ");
        stb.append("         SUM(EARLY)      EARLY, ");
        stb.append("         SUM(NOTICE_LATE) NOTICE_LATE1 ");
        if (absent_cov.equals("4") && StringUtils.isNumeric(absent_cov_late) && 0 != Integer.parseInt(absent_cov_late)) {
            stb.append("      ,DECIMAL((FLOAT(SUM(LATE) + SUM(EARLY)) / " + Integer.parseInt(absent_cov_late) + ") + (SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK)),4,1) AS NOTICE_LATE2 ");
        } else if (absent_cov.equals("2") && StringUtils.isNumeric(absent_cov_late) && 0 != Integer.parseInt(absent_cov_late)) {
            stb.append("      ,((SUM(LATE) + SUM(EARLY)) / " + Integer.parseInt(absent_cov_late) + ") + (SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK)) AS NOTICE_LATE2 ");
        } else {
            stb.append("      ,SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK) AS NOTICE_LATE2 ");
        }
        stb.append("     FROM ");
        stb.append("         ATTEND_SUM2 ");
        stb.append("     GROUP BY ");
        stb.append("         SCHREGNO, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         CLASSCD, ");
            stb.append("         SCHOOL_KIND, ");
            stb.append("         CURRICULUM_CD, ");
        }
        stb.append("         SUBCLASSCD ");
        stb.append(" ) ");

        //学籍番号でグルーピング
        stb.append(",ATTEND_SUM4 AS ( ");
        stb.append("     SELECT ");
        stb.append("         SCHREGNO, ");
        stb.append("         0 AS LATE, ");
        stb.append("         0 AS EARLY, ");
        stb.append("         0 AS NOTICE, ");
        if ((absent_cov.equals("1") || absent_cov.equals("3")) && StringUtils.isNumeric(absent_cov_late) && 0 != Integer.parseInt(absent_cov_late)) {
            stb.append("  SUM(NOTICE_LATE1) AS NOTICE_LATE ");
        } else {
            stb.append("  SUM(NOTICE_LATE2) AS NOTICE_LATE ");
        }
        stb.append("     FROM ");
        stb.append("         ATTEND_SUM3 ");
        stb.append("     GROUP BY ");
        stb.append("         SCHREGNO ");
        stb.append(" ) ");

        //遅刻・早退・欠席取得
        stb.append(" ,ATTEND_SEMES AS ( ");
        stb.append("     SELECT ");
        stb.append("         SCHREGNO, ");
        stb.append("         SUM( VALUE(LATE,0)) LATE,  ");
        stb.append("         SUM( VALUE(EARLY,0)) EARLY,  ");
        stb.append("         SUM( VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) ");
        if ("1".equals(knjSchoolMst._semOffDays)) {
            stb.append("        + VALUE(OFFDAYS,0) ");
        }
        stb.append("         ) AS NOTICE, ");
        stb.append("         0 AS NOTICE_LATE ");
        stb.append("     FROM ");
        stb.append("         ATTEND_SEMES_DAT ");
        stb.append("     WHERE ");
        stb.append("         YEAR = '" + _param._year + "' AND ");
        stb.append("         SEMESTER <= '" + _param._ctrlSemester + "' AND ");
        stb.append("         MONTH IN " + SQLUtils.whereIn(true, month) + " ");
        stb.append("     GROUP BY ");
        stb.append("         SCHREGNO ");
        stb.append(" ) ");

        //メイン
        stb.append(" , MAIN AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     SUM(T2.LATE) AS LATE, ");
        stb.append("     SUM(T2.EARLY) AS EARLY, ");
        stb.append("     SUM(T2.NOTICE) AS NOTICE, ");
        stb.append("     SUM(T2.NOTICE_LATE) AS NOTICE_LATE ");
        stb.append(" FROM ");
        stb.append("     SCHNO T1, ");
        stb.append("     (SELECT * FROM ATTEND_SUM4 UNION ALL SELECT * FROM ATTEND_SEMES) T2 ");
        stb.append(" WHERE ");
        stb.append("     T1.SCHREGNO = T2.SCHREGNO ");
        stb.append(" GROUP BY ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.NAME ");
        stb.append(" ) ");

        stb.append(" SELECT ");
        stb.append("     * ");
        stb.append(" FROM ");
        stb.append("     MAIN ");
        stb.append(" WHERE ");
        stb.append("     (LATE   >= " + _param._late5 + "  OR ");
        stb.append("      EARLY  >= " + _param._early5 + " OR ");
        stb.append("      NOTICE >= " + _param._absent5 + " OR ");
        stb.append("      NOTICE_LATE >= " + _param._subclassAbsent5 + ") ");
        stb.append(" ORDER BY ");
        stb.append("     5 DESC, ");
        stb.append("     6 DESC, ");
        stb.append("     7 DESC, ");
        stb.append("     8 DESC, ");
        stb.append("     1, ");
        stb.append("     2 ");

        return stb.toString();
    }
}

// eof
