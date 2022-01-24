// kanji=漢字
/*
 * $Id: a701c50fd23105403870d80b5adab2d30e2a5695 $
 *
 * 作成日: 2009/10/21 8:52:18 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: a701c50fd23105403870d80b5adab2d30e2a5695 $
 */
public class KNJE372K {

    private static final Log log = LogFactory.getLog("KNJE372K.class");

    private boolean _hasData;

    Param _param;

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
            init(response, svf);

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            printMain(db2, svf);

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            closeDb(db2);
            svf.VrQuit();
        }

    }

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final List printData = getPrintData(db2);
        final int LEFT = 1;
        final int RIGHT = 2;
        int dataCnt = 0;
        int lrNo = 0;
        for (final Iterator it = printData.iterator(); it.hasNext();) {
            final PrintData data = (PrintData)it.next();

            dataCnt++;
            if (dataCnt % 4 == 1) {
                svf.VrSetForm("KNJE372K.frm", 4);
            }
            lrNo = dataCnt % 2 == 0 ? RIGHT : LEFT;
            final String lrField = String.valueOf(lrNo);

            //出力日付
            svf.VrsOut("DATE" + lrField, KNJ_EditDate.h_format_SeirekiJP(_param._printDate));
            //学校名
            final int schoolLen = KNJ_EditEdit.getMS932ByteLength(_param._schoolName);
            final String schoolField = schoolLen > 20 ? "_2" : "_1";
            svf.VrsOut("SCHOOL_NAME" + lrField + schoolField, _param._schoolName);
            //校長名
            final int principalLen = KNJ_EditEdit.getMS932ByteLength("校　長　" + _param._principalName);
            final String principalField = principalLen > 20 ? "_2" : "_1";
            svf.VrsOut("PRINCIPAL_NAME" + lrField + principalField, "校　長　" + _param._principalName);
            //年組番
            final String attendno = String.valueOf(Integer.parseInt(data._attendno)) + "番";
            svf.VrsOut("HR_NAME" + lrField, data._hrName + attendno);
            //氏名
            final int nameLen = KNJ_EditEdit.getMS932ByteLength(data._name);
            final String nameField = nameLen > 30 ? "_3" : nameLen > 20 ? "_2" : "_1";
            svf.VrsOut("NAME" + lrField + nameField, data._name);
            //学部、学科
            final int facultyLen = KNJ_EditEdit.getMS932ByteLength(data._facultyName + "　" + data._departmentName);
            final String facultyField = facultyLen > 28 ? "_2" : "_1";
            svf.VrsOut("FACULTY_NAME" + lrField + facultyField, data._facultyName + "　" + data._departmentName);

            if (lrNo == RIGHT) {
                svf.VrEndRecord();
                _hasData = true;
            }
        }
        if (lrNo == LEFT) {
            svf.VrEndRecord();
            _hasData = true;
        }
    }

    private List getPrintData(final DB2UDB db2) throws SQLException {
        final List rtnList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getPrintSql();
            log.debug(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = StringUtils.defaultString(rs.getString("SCHREGNO"));
                final String grade = StringUtils.defaultString(rs.getString("GRADE"));
                final String hrClass = StringUtils.defaultString(rs.getString("HR_CLASS"));
                final String attendno = StringUtils.defaultString(rs.getString("ATTENDNO"));
                final String name = StringUtils.defaultString(rs.getString("NAME"));
                final String hrName = StringUtils.defaultString(rs.getString("HR_NAME"));
                final String facultyName = StringUtils.defaultString(rs.getString("FACULTY_NAME"));
                final String departmentName = StringUtils.defaultString(rs.getString("DEPARTMENT_NAME"));

                final PrintData printData = new PrintData(schregno, grade, hrClass, attendno, name, hrName, facultyName, departmentName);
                rtnList.add(printData);
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtnList;
    }

    private String getPrintSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH REGD_T AS ( ");
        stb.append("     SELECT ");
        stb.append("         REGD.SCHREGNO, ");
        stb.append("         REGD.GRADE, ");
        stb.append("         REGD.HR_CLASS, ");
        stb.append("         REGD.ATTENDNO, ");
        stb.append("         BASE.NAME, ");
        stb.append("         REGD_H.HR_NAME ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT REGD ");
        stb.append("         INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
        stb.append("         INNER JOIN SCHREG_REGD_HDAT REGD_H ON REGD_H.YEAR = REGD.YEAR ");
        stb.append("             AND REGD_H.SEMESTER = REGD.SEMESTER ");
        stb.append("             AND REGD_H.GRADE = REGD.GRADE ");
        stb.append("             AND REGD_H.HR_CLASS = REGD.HR_CLASS ");
        stb.append("     WHERE ");
        stb.append("         REGD.YEAR = '" + _param._year + "' ");
        stb.append("         AND REGD.SEMESTER = '" + _param._ctrlSemester + "' ");
        if ("1".equals(_param._categoryIsClass)) {
            stb.append("         AND REGD.GRADE || '-' || REGD.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categorySelected));
        } else {
            stb.append("         AND REGD.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected));
        }
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     REGD.SCHREGNO, ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     REGD.NAME, ");
        stb.append("     REGD.HR_NAME, ");
        stb.append("     RECOM.FACULTY_NAME, ");
        stb.append("     RECOM.DEPARTMENT_NAME ");
        stb.append(" FROM ");
        stb.append("     AFT_SCHREG_CONVERT_SCORE_DAT CONV ");
        stb.append("     INNER JOIN REGD_T REGD ON REGD.SCHREGNO = CONV.SCHREGNO ");
        stb.append("     INNER JOIN AFT_RECOMMENDATION_LIMIT_MST RECOM ON RECOM.YEAR = CONV.YEAR ");
        stb.append("         AND (RECOM.DEPARTMENT_S = CONV.RECOMMENDATION_DEPARTMENT_CD OR RECOM.DEPARTMENT_H = CONV.RECOMMENDATION_DEPARTMENT_CD) ");
        stb.append(" WHERE ");
        stb.append("     CONV.YEAR = '" + _param._year + "' ");
        stb.append(" ORDER BY ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO ");

        return stb.toString();
    }

    private class PrintData {
        final String _schregno;
        final String _grade;
        final String _hrClass;
        final String _attendno;
        final String _name;
        final String _hrName;
        final String _facultyName;
        final String _departmentName;

        PrintData(final String schregno,
                final String grade,
                final String hrClass,
                final String attendno,
                final String name,
                final String hrName,
                final String facultyName,
                final String departmentName
        ) {
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _attendno = attendno;
            _name = name;
            _hrName = hrName;
            _facultyName = facultyName;
            _departmentName = departmentName;
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return _schregno + " = " + _name;
        }
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 76002 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _categoryIsClass;
        final String[] _categorySelected;
        final String _printDate;
        final String _schoolName;
        final String _principalName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _ctrlSemester = request.getParameter("LOGIN_SEMESTER");
            _ctrlDate = request.getParameter("LOGIN_DATE");
            _categoryIsClass = request.getParameter("CATEGORY_IS_CLASS");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _printDate = request.getParameter("PRINT_DATE");
            _schoolName = getSchoolName(db2);
            final String principalName = getPrincipalName(db2);
            _principalName = trimLeft(principalName);
        }
        private String getSchoolName(final DB2UDB db2) throws SQLException {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR = '" + _year + "' AND SCHOOL_KIND = 'H'"));
        }
        private String getPrincipalName(final DB2UDB db2) throws SQLException {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT PRINCIPAL_NAME FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '101'"));
        }

        // 左の半角スペース、全角スペースをカット
        private String trimLeft(final String s) {
            if (null == s) {
                return s;
            }
            int start = 0;
            int i = 0;
            while (i < s.length() && (s.charAt(i) == ' ' || s.charAt(i) == '　')) {
                i++;
                start = i;
            }
            if (start >= s.length()) {
                return "";
            }
            return s.substring(start);
        }
    }
}

// eof
