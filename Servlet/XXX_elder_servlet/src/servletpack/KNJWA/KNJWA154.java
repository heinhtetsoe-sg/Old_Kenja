// kanji=漢字
/*
 * $Id: 4393f5df0c58bdddb919627efaa3a1aff5c0ac19 $
 *
 * 作成日: 2010/06/04 15:31:27 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2010 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJWA;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 4393f5df0c58bdddb919627efaa3a1aff5c0ac19 $
 */
public class KNJWA154 {

    private static final Log log = LogFactory.getLog("KNJWA154.class");

    private static final String FORM_NAME = "KNJWA154.frm";
    private static final int MAX_LINE = 50;
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

        final int printAllCnt = printData.size();
        int pageTotal = printAllCnt / MAX_LINE;
        pageTotal += printAllCnt % MAX_LINE > 0 ? 1 : 0;

        svf.VrSetForm(FORM_NAME, 1);
        int page = 1;
        setHead(svf, pageTotal, page);
        int gyo = 1;
        for (final Iterator iter = printData.iterator(); iter.hasNext();) {
            if (gyo > MAX_LINE) {
                svf.VrEndPage();
                page++;
                gyo = 1;
                setHead(svf, pageTotal, page);
            }
            final Student student = (Student) iter.next();
            final String fieldName = student._belongingName.length() > 20 ? "2" : "1";
            svf.VrsOutn("BELONGING_NAME" + fieldName, gyo, student._belongingName);
            svf.VrsOutn("SCHREGSTART", gyo, KNJ_EditDate.h_format_JP(student._schregDate));
            svf.VrsOutn("APPLICANTNO", gyo, student._applicantNo);
            svf.VrsOutn("SCHREGNO", gyo, student._schregNo);
            svf.VrsOutn("NAME", gyo, student._name);
            gyo++;
            _hasData = true;
        }
        if (gyo > 1) {
            svf.VrEndPage();
        }
    }

    private void setHead(final Vrw32alp svf, final int pageTotal, final int page) {
        final String setDate = KNJ_EditDate.h_format_JP(_param._fromDate) + "\uFF5E" + KNJ_EditDate.h_format_JP(_param._toDate);
        svf.VrsOut("DATE1", setDate);
        svf.VrsOut("DATE2", KNJ_EditDate.getNowDateWa(true));
        svf.VrsOut("PAGE", String.valueOf(page));
        svf.VrsOut("TOTALPAGE", String.valueOf(pageTotal));
    }

    private List getPrintData(final DB2UDB db2) throws SQLException {
        List retList = new ArrayList();
        final String printSql = getPrintSql();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            db2.query(printSql);
            ps = db2.prepareStatement(printSql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String belongingName = rs.getString("SCHOOLNAME1");
                final String schregDate = rs.getString("SCHREG_DATE");
                final String applicantNo = rs.getString("APPLICANTNO");
                final String schregNo = rs.getString("SCHREGNO");
                final String name = rs.getString("NAME");
                final String grade = rs.getString("GRADE");

                final Student student = new Student(belongingName, schregDate, applicantNo, schregNo, name, grade);
                retList.add(student);
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getPrintSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     L2.SCHOOLNAME1, ");
        stb.append("     T1.SCHREG_DATE, ");
        stb.append("     T1.APPLICANTNO, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     L1.GRADE ");
        stb.append(" FROM ");
        stb.append("     SCHREG_BASE_MST T1 ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT L1 ON T1.SCHREGNO = L1.SCHREGNO ");
        stb.append("          AND L1.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("          AND L1.SEMESTER = '" + _param._ctrlSemester + "' ");
        if (null != _param._belongingDiv && !"".equals(_param._belongingDiv)) {
            stb.append("          AND L1.GRADE = '" + _param._belongingDiv + "' ");
        } else {
            if (null != _param._belonging && !"".equals(_param._belonging)) {
                stb.append("          AND L1.GRADE = '" + _param._belonging + "' ");
            }
        }
        stb.append("     LEFT JOIN BELONGING_MST L2 ON L1.GRADE = L2.BELONGING_DIV ");
        stb.append(" WHERE ");
        stb.append("     T1.SCHREG_DATE BETWEEN '" + _param._fromDate.replace('/', '-') + "' AND '" + _param._toDate.replace('/', '-') + "' ");
        stb.append("     AND EXISTS( ");
        stb.append("         SELECT ");
        stb.append("             'x' ");
        stb.append("         FROM ");
        stb.append("             CLAIM_DAT E1 ");
        stb.append("         WHERE ");
        stb.append("             T1.APPLICANTNO = E1.APPLICANTNO ");
        stb.append("             AND VALUE(E1.CANCEL_FLG, '0') = '0' ");
        stb.append("             AND MANNER_PAYMENT IN ('2', '3') ");
        stb.append("             AND COMP_ENT_FLG = '1' ");
        stb.append("     ) ");
        stb.append(" ORDER BY ");
        stb.append("     L1.GRADE, ");
        stb.append("     T1.SCHREG_DATE, ");
        stb.append("     T1.SCHREGNO ");

        return stb.toString();
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    private class Student {
        final String _belongingName;
        final String _schregDate;
        final String _applicantNo;
        final String _schregNo;
        final String _name;
        final String _grade;

        public Student(
                final String belongingName,
                final String schregDate,
                final String applicantNo,
                final String schregNo,
                final String name,
                final String grade
        ) {
            _belongingName = belongingName;
            _schregDate = schregDate;
            _applicantNo = applicantNo;
            _schregNo = schregNo;
            _name = name;
            _grade = grade;
        }
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
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _belongingDiv;
        private final String _fromDate;
        private final String _toDate;
        private final String _belonging;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _belongingDiv = request.getParameter("BELONGING_DIV");
            _fromDate = request.getParameter("FROM_DATE");
            _toDate = request.getParameter("TO_DATE");
            _belonging = request.getParameter("BELONGING");
        }

    }
}

// eof
