// kanji=漢字
/*
 * $Id: 2658a345a4c62a42af486bb1a4cbd8070a2b6def $
 *
 * 作成日: 2011/04/01 16:46:18 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJX;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.AbstractXls;
import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: 2658a345a4c62a42af486bb1a4cbd8070a2b6def $
 */
public class KNJX230 extends AbstractXls {

    private static final Log log = LogFactory.getLog("KNJX230.class");

    private boolean _hasData;

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

        //ヘッダデータ取得
        _headList = getHeadData();

        //出力データ取得
        _dataList = getXlsDataList();

        outPutXls(response, _param._header);
    }

    protected List getHeadData() {
        final List retList = new ArrayList();
        retList.add("学年");
        retList.add("クラス");
        retList.add("出席番号");
        retList.add("氏名");
        retList.add("※複写区分");
        retList.add("※年度");
        retList.add("※対象月");
        retList.add("※学期");
        retList.add("※学籍番号");
        for (final Iterator it = _param._fieldNames.iterator(); it.hasNext();) {
            final String fieldName = (String) it.next();
            retList.add(_param._headMap.get(fieldName));
        }
        return retList;
    }

    protected String[] getCols() {
        final List retCols = new ArrayList();
        retCols.add("GRADE");
        retCols.add("HR_CLASS");
        retCols.add("ATTENDNO");
        retCols.add("NAME");
        retCols.add("COPYCD");
        retCols.add("YEAR");
        retCols.add("MONTH");
        retCols.add("SEMESTER");
        retCols.add("SCHREGNO");
        for (final Iterator it = _param._fieldNames.iterator(); it.hasNext();) {
            final String fieldName = (String) it.next();
            retCols.add(fieldName);
        }
        final String[] cols = (String[]) retCols.toArray(new String[0]);
        return cols;
    }

    protected String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T2.NAME, ");
        stb.append("     T3.COPYCD, ");
        stb.append("     T3.YEAR, ");
        stb.append("     T3.MONTH, ");
        stb.append("     T3.SEMESTER, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T3.APPOINTED_DAY, ");
        stb.append("     T3.LESSON, ");
        stb.append("     T3.OFFDAYS, ");
        stb.append("     T3.ABROAD, ");
        stb.append("     T3.ABSENT, ");
        stb.append("     T3.SUSPEND, ");
        if ("true".equals(_param._useKoudome)) {
            stb.append("     T3.KOUDOME, ");
        }
        if ("true".equals(_param._useVirus)) {
            stb.append("     T3.VIRUS, ");
        }
        stb.append("     T3.MOURNING, ");
        stb.append("     T3.SICK, ");
        stb.append("     T3.NOTICE, ");
        stb.append("     T3.NONOTICE, ");
        stb.append("     T3.LATE, ");
        stb.append("     T3.EARLY, ");
        stb.append("     'DUMMY' AS DUMMY ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN ATTEND_SEMES_DAT T3 ON T3.YEAR = T1.YEAR ");
        stb.append("     AND T3.SEMESTER = T1.SEMESTER ");
        stb.append("     AND T3.SCHREGNO = T1.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR || T1.SEMESTER = '" + _param._yearSem + "' ");
        stb.append("     AND T3.SCHREGNO = T1.SCHREGNO ");
        if (!StringUtils.isEmpty(_param._gradeHrClass)) {
            stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + _param._gradeHrClass + "' ");
        }
        if (!StringUtils.isEmpty(_param._student)) {
            stb.append("     AND T1.SCHREGNO = '" + _param._student + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T3.MONTH ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _yearSem;
        private final String _gradeHrClass;
        private final String _student;
        private final String _monthSem;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final boolean _header;
        private final String _templatePath;
        private final List _fieldNames;
        private final Map _titleNames;
        private final Map _headMap;
        private final String _useVirus;
        private final String _useKoudome;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _yearSem = request.getParameter("YEAR");
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            _student = request.getParameter("STUDENT");
            _monthSem = request.getParameter("MONTH");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _header = request.getParameter("HEADER") == null ? false : true;
            _templatePath = request.getParameter("TEMPLATE_PATH");
            _fieldNames = Arrays.asList(request.getParameterValues("XLS_FIELDNAME"));
            _titleNames = getTitleNames();
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
            
            _headMap = new HashMap();
            _headMap.put("APPOINTED_DAY", "指定日");
            _headMap.put("LESSON",   "授業日数");
            _headMap.put("OFFDAYS",  "休学日数");
            _headMap.put("ABROAD",   "留学日数");
            _headMap.put("ABSENT",   "公欠日数");
            _headMap.put("SUSPEND",  StringUtils.defaultString((String) _titleNames.get("2")) + "日数");
            _headMap.put("VIRUS",    StringUtils.defaultString((String) _titleNames.get("19")) + "日数");
            _headMap.put("KOUDOME",  StringUtils.defaultString((String) _titleNames.get("25")) + "日数");
            _headMap.put("MOURNING", "忌引");
            _headMap.put("SICK",     _titleNames.get("4"));
            _headMap.put("NOTICE",   _titleNames.get("5"));
            _headMap.put("NONOTICE", _titleNames.get("6"));
            _headMap.put("LATE",    "遅刻");
            _headMap.put("EARLY",   "早退");
        }

        private Map getTitleNames() throws SQLException {
            final Map titleNames = new HashMap();

            final String sql = getTitleNamesSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = _db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    titleNames.put(rs.getString("NAMECD2"), rs.getString("NAME1"));
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                _db2.commit();
            }

            return titleNames;
        }

        private String getTitleNamesSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     NAMECD2, NAME1 ");
            stb.append(" FROM ");
            stb.append("     NAME_MST ");
            stb.append(" WHERE ");
            stb.append("         NAMECD1 = 'C001' ");
            stb.append("     AND NAMECD2 IN ('2', '4', '5', '6', '14', '19', '25') ");
            stb.append(" ORDER BY ");
            stb.append("     NAMECD2 ");
            return stb.toString();
        }
    }
}

// eof
