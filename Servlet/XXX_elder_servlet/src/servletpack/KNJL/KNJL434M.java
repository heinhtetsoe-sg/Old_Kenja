/*
 * 作成日: 2021/03/26 作成者: miwa
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJL434M {

    private static final Log log = LogFactory.getLog(KNJL434M.class);

    private boolean _hasData;

    private Param _param;

    private static final String ENTERING = "1";
    private static final int MAX_ROW = 10;
    private static final int MAX_COL = 10;

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws Exception {

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
        svf.VrSetForm("KNJL434M.frm", 1);

        final Map<String, PrintData> printDataMap = getPrintDataMap(db2);

        for (Iterator itPrintData = printDataMap.keySet().iterator(); itPrintData.hasNext();) {
            final String printKey = (String) itPrintData.next();
            final PrintData printData = (PrintData) printDataMap.get(printKey);

            setHeader(svf, printData);

            int examCol = 1;
            int examRow = 1;
            for (Iterator itRecept = printData._receptNoList.iterator(); itRecept.hasNext();) {
                final String receptNo = (String) itRecept.next();
                if (examRow > MAX_ROW) {
                    examRow = 1;
                    examCol++;
                }
                if (examCol > MAX_COL) {
                    examRow = 1;
                    examCol = 1;
                    svf.VrEndPage();
                    setHeader(svf, printData);
                }
                svf.VrsOutn("EXAM_NO" + examCol, examRow, receptNo);
                log.debug(receptNo);
                examRow++;
            }

            _hasData = true;
            svf.VrEndPage();
        }
    }


    private void setHeader(final Vrw32alp svf, final PrintData printData) {
        svf.VrsOut("EXAM_NAME", printData._examName);
        svf.VrsOut("DATE", printData._dateNote);
        svf.VrsOut("SCHOOL_NAME", _param._schoolName);
    }

    private Map getPrintDataMap(final DB2UDB db2) {
        final Map<String, PrintData> retMap = new TreeMap();
        for (String examKey : _param._examSelect) {
            // 試験情報取得
            PreparedStatement ps = null;
            ResultSet rs = null;
            String sql = null;

            try {
                String _examSchoolKind = examKey.substring(0, 1);
                String _applicantDiv = examKey.substring(1, 3);
                String _courseDiv = examKey.substring(3, 7);
                String _frequency = examKey.substring(7);
                sql = getEntExamSql(_examSchoolKind, _applicantDiv, _courseDiv, _frequency);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                rs.next();
                String examName = rs.getString("EXAM_NAME");
                String dateNote = KNJ_EditDate.h_format_JP_MD(rs.getString("EXAM_DATE")) + rs.getString("AM_PM");
                final PrintData printData = new PrintData(examName, dateNote);

                ps = null;
                rs = null;
                sql = null;
                sql = sqlReceptno(_examSchoolKind, _applicantDiv, _courseDiv, _frequency);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                log.debug(" sql =" + sql);

                while (rs.next()) {
                    printData._receptNoList.add(rs.getString("RECEPTNO"));
                }

                retMap.put(examKey, printData);
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        return retMap;
    }

    private class PrintData {
        private final String _examName;
        private final String _dateNote;
        private final List<String> _receptNoList;

        public PrintData(final String examName, final String dateNote) {
            _examName = examName;
            _dateNote = dateNote;
            _receptNoList = new ArrayList<String>();
        }
    }

    private String getEntExamSql(final String examSchoolKind, final String applicantDiv, final String courseDiv, final String frequency) {

        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT  EXAM_NAME, EXAM_DATE, ");
        stb.append("         CASE ");
        stb.append("              WHEN AM_PM = '1' THEN '午前' ");
        stb.append("              WHEN AM_PM = '2' THEN '午後' ");
        stb.append("         END AS AM_PM ");
        stb.append(" FROM    ENTEXAM_STD_MST ");
        stb.append(" WHERE   YEAR = '" + _param._examyear + "' AND ");
        stb.append("         EXAM_SCHOOL_KIND = '" + examSchoolKind + "' AND ");
        stb.append("         APPLICANT_DIV = '" + applicantDiv + "' AND ");
        stb.append("         COURSE_DIV = '" + courseDiv + "' AND ");
        stb.append("         FREQUENCY = '" + frequency + "' ");
        return stb.toString();
    }

    private String sqlReceptno(final String examSchoolKind, final String applicantDiv, final String courseDiv, final String frequency) {

        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT  RECEPTNO ");
        stb.append(" FROM    ENTEXAM_STD_RECEPT_DAT ");
        stb.append(" WHERE   YEAR = '" + _param._examyear + "' AND ");
        stb.append("         EXAM_SCHOOL_KIND = '" + examSchoolKind + "' AND ");
        stb.append("         APPLICANT_DIV = '" + applicantDiv + "' AND ");
        stb.append("         COURSE_DIV = '" + courseDiv + "' AND ");
        stb.append("         FREQUENCY = '" + frequency + "' AND ");
        stb.append("         JUDGEMENT in ('1', '2') "); // 合格、繰り上げ合格
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        private final String _examyear;
        private final String[] _examSelect;
        private final String _schoolKind;
        private final String _schoolName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _examyear = request.getParameter("EXAM_YEAR");
            _examSelect = request.getParameterValues("EXAM_SELECTED");
            _schoolKind = request.getParameter("SCHOOL_KIND_DIV");
            _schoolName = getSchoolName(db2);
        }

        private String getSchoolName(final DB2UDB db2) {
            String rtn = "";

            final String setCertifKind = "H".equals(_schoolKind) ? "104" : "J".equals(_schoolKind) ? "105" : "";
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT  * ");
            stb.append(" FROM    CERTIF_SCHOOL_DAT ");
            stb.append(" WHERE   YEAR = '" + _examyear + "' AND ");
            stb.append("         CERTIF_KINDCD = '" + setCertifKind + "'");

            final String sql = stb.toString();
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    rtn = rs.getString("SCHOOL_NAME");
                }
            } catch (SQLException e) {
                log.error("Exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            return rtn;
        }

    }

}

// eof

