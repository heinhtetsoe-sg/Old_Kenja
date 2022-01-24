// kanji=漢字
/*
 * $Id: 32cf38744620679da5468e1823e4cd35e9542f03 $
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
import java.util.HashMap;
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
 * @version $Id: 32cf38744620679da5468e1823e4cd35e9542f03 $
 */
public class KNJX_C031K extends AbstractXls {

    private static final Log log = LogFactory.getLog("KNJX_C031K.class");

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
        retList.add("学級");
        retList.add("No.");
        retList.add("氏名");
        retList.add("※年度");
        retList.add("※対象月");
        retList.add("※学期");
        retList.add("※学籍番号");
        retList.add("締め日");
        retList.add("授業日数");
        retList.add("休学日数");
        retList.add("留学日数");
        retList.add("公欠日数");
        retList.add(StringUtils.defaultString((String) _param._getTitleNamesMap.get("2")));
        if ("true".equals(_param._useKoudome)) {
            retList.add(StringUtils.defaultString((String) _param._getTitleNamesMap.get("25")));
        }
        if ("true".equals(_param._useVirus)) {
            retList.add(StringUtils.defaultString((String) _param._getTitleNamesMap.get("19")));
        }
        retList.add("忌引");
        retList.add(_param._titleNames[0]);
        retList.add(_param._titleNames[1]);
        retList.add(_param._titleNames[2]);
        retList.add("遅刻");
        retList.add("早退");
        return retList;
    }

    protected String[] getCols() {
        final List colsList = new ArrayList();
        colsList.add("HR_NAME");
        colsList.add("ATTENDNO");
        colsList.add("NAME");
        colsList.add("YEAR");
        colsList.add("MONTH");
        colsList.add("SEMESTER");
        colsList.add("SCHREGNO");
        colsList.add("APPOINTED_DAY");
        colsList.add("LESSON");
        colsList.add("OFFDAYS");
        colsList.add("ABROAD");
        colsList.add("ABSENT");
        colsList.add("SUSPEND");
        if ("true".equals(_param._useKoudome)) {
            colsList.add("KOUDOME");
        }
        if ("true".equals(_param._useVirus)) {
            colsList.add("VIRUS");
        }
        colsList.add("MOURNING");
        colsList.add("SICK");
        colsList.add("NOTICE");
        colsList.add("NONOTICE");
        colsList.add("LATE");
        colsList.add("EARLY");
        final String[] cols = (String[]) colsList.toArray(new String[colsList.size()]);
        return cols;
    }

    protected String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     S2.HR_NAME, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     S1.NAME, ");
        stb.append("     T1.YEAR, ");
        stb.append("     S3.MONTH, ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     S4.APPOINTED_DAY, ");
        stb.append("     S3.LESSON, ");
        stb.append("     S3.OFFDAYS, ");
        stb.append("     S3.ABROAD, ");
        stb.append("     S3.ABSENT, ");
        stb.append("     S3.SUSPEND, ");
        if ("true".equals(_param._useKoudome)) {
            stb.append("     S3.KOUDOME, ");
        }
        if ("true".equals(_param._useVirus)) {
            stb.append("     S3.VIRUS, ");
        }
        stb.append("     S3.MOURNING, ");
        stb.append("     S3.SICK, ");
        stb.append("     S3.NOTICE, ");
        stb.append("     S3.NONOTICE, ");
        stb.append("     S3.LATE, ");
        stb.append("     S3.EARLY ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST S1 ON T1.SCHREGNO = S1.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT S2 ON T1.YEAR = S2.YEAR ");
        stb.append("                                  AND T1.SEMESTER = S2.SEMESTER ");
        stb.append("                                  AND T1.GRADE = S2.GRADE ");
        stb.append("                                  AND T1.HR_CLASS = S2.HR_CLASS ");
        stb.append("     LEFT JOIN ATTEND_SEMES_DAT S3 ON T1.YEAR = S3.YEAR ");
        stb.append("                                  AND T1.SCHREGNO = S3.SCHREGNO ");
        stb.append("                                  AND S3.MONTH || '-' || S3.SEMESTER  = '" + _param._monthSem + "' ");
        stb.append("     LEFT JOIN APPOINTED_DAY_MST S4 ON T1.YEAR = S4.YEAR ");
        stb.append("                                  AND S3.MONTH || '-' || S3.SEMESTER = S4.MONTH || '-' || S4.SEMESTER ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR || T1.SEMESTER = '" + _param._yearSem + "' AND ");
        stb.append("     T1.GRADE || T1.HR_CLASS = '" + _param._gradeHrClass + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.SCHREGNO ");
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
        private final String _monthSem;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final boolean _header;
        private final String _templatePath;
        private final String[] _titleNames;
        private final Map _getTitleNamesMap;
        private final String _useVirus;
        private final String _useKoudome;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _yearSem = request.getParameter("YEAR");
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            _monthSem = request.getParameter("MONTH");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _header = request.getParameter("HEADER") == null ? false : true;
            _templatePath = request.getParameter("TEMPLATE_PATH");
            _titleNames = getTitleNames();
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
            _getTitleNamesMap = getTitleNamesMap();
        }

        private String[] getTitleNames() throws SQLException {
            String[] titleNames = {"","",""};

            final String sql = getTitleNamesSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = _db2.prepareStatement(sql);
                rs = ps.executeQuery();
                int cnt = 0;
                while (rs.next()) {
                    titleNames[cnt] = rs.getString("NAME1");
                    cnt++;
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
            stb.append("     NAME1 ");
            stb.append(" FROM ");
            stb.append("     NAME_MST ");
            stb.append(" WHERE ");
            stb.append("         NAMECD1 = 'C001' ");
            stb.append("     AND NAMECD2 IN ('4', '5', '6') ");
            stb.append(" ORDER BY ");
            stb.append("     NAMECD2 ");
            return stb.toString();
        }
        

        private Map getTitleNamesMap() throws SQLException {
            final Map titleNames = new HashMap();

            final String sql = getTitleNamesSql1();
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

        private String getTitleNamesSql1() {
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
