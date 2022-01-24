/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id$
 *
 * 作成日: 2019/08/06
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJA113A {

    private static final Log log = LogFactory.getLog(KNJA113A.class);

    private boolean _hasData;
    private final int MAX_LINE = 50;

    private Param _param;

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        try {
            response.setContentType("application/pdf");

            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            printMain(db2, svf);
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }

            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }

    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJA113A.frm", 1);
        final List printList = getList(db2);
        int lineCnt = 1;
        String befStdDiv = "";
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final PrintDataStudent printDataStudent = (PrintDataStudent) iterator.next();
            if (!befStdDiv.equals("") && !befStdDiv.equals(printDataStudent._scholarship)) {
                svf.VrEndPage();
                lineCnt = 1;
            }
            if (lineCnt > MAX_LINE) {
                svf.VrEndPage();
                lineCnt = 1;
            }
            final String nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year));
            final String setTitle = nendo + "年度　減免対象者出力";
            svf.VrsOut("TITLE", setTitle);
            if (_param._reductionMap.containsKey(printDataStudent._scholarship)) {
                svf.VrsOut("DIV", (String) _param._reductionMap.get(printDataStudent._scholarship));
            }
            String hrName = printDataStudent._hrName + " " + printDataStudent._attendNo + "番";
            final String hrNameField = KNJ_EditEdit.getMS932ByteLength(hrName) > 15 ? "2" : "1";
            svf.VrsOutn("HR_NAME" + hrNameField, lineCnt, hrName);
            svf.VrsOutn("SCHREGNO", lineCnt, printDataStudent._schregNo);
            final String nameField = KNJ_EditEdit.getMS932ByteLength(printDataStudent._name) > 60 ? "2" : "1";
            svf.VrsOutn("NAME" + nameField, lineCnt, printDataStudent._name);

            lineCnt++;
            befStdDiv = printDataStudent._scholarship;
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getStudentSql();
            log.fatal(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String scholarship = rs.getString("SCHOLARSHIP");
                final String schregNo = rs.getString("SCHREGNO");
                final String grade = rs.getString("GRADE");
                final String hrClass = rs.getString("HR_CLASS");
                final String attendNo = rs.getString("ATTENDNO");
                final String name = rs.getString("NAME");
                final String hrName = rs.getString("HR_NAME");
                final String fromDate = rs.getString("FROM_DATE");
                final String toDate = rs.getString("TO_DATE");
                final String remark = rs.getString("REMARK");

                final PrintDataStudent printDataStudent = new PrintDataStudent(scholarship, schregNo, grade, hrClass, attendNo, name, hrName, fromDate, toDate, remark);
                retList.add(printDataStudent);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCH_DATA AS ( ");
        if ("1".equals(_param._stdDiv)) {
            stb.append("     SELECT DISTINCT ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.GRADE, ");
            stb.append("         VALUE(T1.HR_CLASS, '000') AS HR_CLASS, ");
            stb.append("         VALUE(T1.ATTENDNO, '000') AS ATTENDNO, ");
            stb.append("         T1.NAME, ");
            stb.append("         '新入生' || T1.GRADE || '-' || VALUE(T1.HR_CLASS, '000') AS HR_NAME ");
            stb.append("     FROM ");
            stb.append("         FRESHMAN_DAT T1 ");
            stb.append("         INNER JOIN SCHREG_REGD_GDAT T3 ON T1.ENTERYEAR = T3.YEAR AND T1.GRADE = T3.GRADE AND T3.SCHOOL_KIND = '" + _param._schoolKind + "' ");
            stb.append("     WHERE ");
            stb.append("         T1.ENTERYEAR = '" + _param._year + "' ");
        } else {
            stb.append("     SELECT DISTINCT ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.GRADE, ");
            stb.append("         T1.HR_CLASS, ");
            stb.append("         T1.ATTENDNO, ");
            stb.append("         T2.NAME, ");
            stb.append("         T4.HR_NAME ");
            stb.append("     FROM ");
            stb.append("         SCHREG_REGD_DAT T1 ");
            stb.append("         INNER JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ");
            stb.append("         INNER JOIN SCHREG_REGD_GDAT T3 ON T1.YEAR = T3.YEAR AND T1.GRADE = T3.GRADE AND T3.SCHOOL_KIND = '" + _param._schoolKind + "' ");
            stb.append("         INNER JOIN SCHREG_REGD_HDAT T4 ON T1.YEAR = T4.YEAR AND T1.SEMESTER = T4.SEMESTER AND T1.GRADE = T4.GRADE AND T1.HR_CLASS = T4.HR_CLASS ");
            stb.append("     WHERE ");
            stb.append("         T1.YEAR     = '" + _param._year + "' AND ");
            stb.append("         T1.SEMESTER = '" + _param._ctrlSemester + "' ");
        }
        stb.append(" ) ");

        stb.append(" SELECT ");
        stb.append("     T1.SCHOLARSHIP, ");
        stb.append("     T2.SCHREGNO, ");
        stb.append("     T2.GRADE, ");
        stb.append("     T2.HR_CLASS, ");
        stb.append("     T2.ATTENDNO, ");
        stb.append("     T2.NAME, ");
        stb.append("     T2.HR_NAME, ");
        stb.append("     T1.FROM_DATE, ");
        stb.append("     T1.TO_DATE, ");
        stb.append("     T1.REMARK ");
        stb.append(" FROM ");
        stb.append("     SCHREG_SCHOLARSHIP_HIST_DAT T1, ");
        stb.append("     SCH_DATA T2 ");
        stb.append(" WHERE ");
        stb.append("     T1.SCHOOLCD    = '" + _param._schoolCd + "' ");
        stb.append("     AND T1.SCHOOL_KIND = '" + _param._schoolKind + "' ");
        if (!"00".equals(_param._scholarship)) {
            stb.append("     AND T1.SCHOLARSHIP = '" + _param._scholarship + "' ");
        }
        stb.append("     AND T1.SCHREGNO    = T2.SCHREGNO ");
        stb.append("     AND T2.GRADE IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
        stb.append("     AND NOT EXISTS ( ");
        stb.append("         SELECT ");
        stb.append("             'X' ");
        stb.append("         FROM ");
        stb.append("             SEMESTER_MST T3 ");
        stb.append("         WHERE ");
        stb.append("                 T3.YEAR = '" + _param._year + "' ");
        stb.append("             AND T3.SEMESTER = '9' ");
        stb.append("             AND (T3.SDATE > VALUE(T1.TO_DATE, '9999-12-31') OR T3.EDATE < T1.FROM_DATE) ");
        stb.append("     ) ");
        stb.append(" ORDER BY ");
        stb.append("     T1.SCHOLARSHIP, ");
        stb.append("     T2.GRADE, ");
        stb.append("     T2.HR_CLASS, ");
        stb.append("     T2.ATTENDNO, ");
        stb.append("     T1.FROM_DATE ");

        return stb.toString();
    }

    private class PrintDataStudent {
        final String _scholarship;
        final String _schregNo;
        final String _grade;
        final String _hrClass;
        final String _attendNo;
        final String _name;
        final String _hrName;
        final String _fromDate;
        final String _toDate;
        final String _remark;
        public PrintDataStudent(
                final String scholarship,
                final String schregNo,
                final String grade,
                final String hrClass,
                final String attendNo,
                final String name,
                final String hrName,
                final String fromDate,
                final String toDate,
                final String remark
        ) {
            _scholarship = scholarship;
            _schregNo = schregNo;
            _grade = grade;
            _hrClass = hrClass;
            _attendNo = attendNo;
            _name = name;
            _hrName = hrName;
            _fromDate = fromDate;
            _toDate = toDate;
            _remark = remark;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 69100 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String[] _categorySelected;
        final String _ctrlSemester;
        final String _year;
        final String _scholarship;
        final String _schoolCd;
        final String _schoolKind;
        final String _stdDiv;
        final Map _reductionMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _year = request.getParameter("CTRL_YEAR");
            _scholarship = request.getParameter("SCHOLARSHIP");
            _schoolCd = request.getParameter("SCHOOLCD");
            _schoolKind = request.getParameter("SCHOOL_KIND");
            _stdDiv = request.getParameter("STD_DIV");
            _reductionMap = getReductionMst(db2);
        }

        private Map getReductionMst(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("    REDUCTION_DIV_NAME AS LABEL, ");
            stb.append("    REDUCTION_DIV_CD AS VALUE ");
            stb.append(" FROM ");
            stb.append("     REDUCTION_SCHOOL_MST ");
            stb.append(" WHERE ");
            stb.append("     SCHOOLCD    = '" + _schoolCd + "' AND ");
            stb.append("     SCHOOL_KIND = '" + _schoolKind + "' AND ");
            stb.append("     YEAR        = '" + _year + "' ");
            stb.append(" ORDER BY ");
            stb.append("     VALUE ");

            final Map retMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    retMap.put(rs.getString("VALUE"), rs.getString("LABEL"));
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

    }
}

// eof
