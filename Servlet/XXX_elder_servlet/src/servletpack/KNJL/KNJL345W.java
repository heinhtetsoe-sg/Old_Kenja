/*
 * $Id: 4c677b8872334d9e2b3bb75d562fb9522ac1d70a $
 *
 * 作成日: 2017/11/20
 * 作成者: yamashiro
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL345W {

    private static final Log log = LogFactory.getLog(KNJL345W.class);

    private boolean _hasData;

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
        final List printList = getList(db2);
        if (printList.size() > 0) {
            svf.VrSetForm("KNJL345W.frm", 4);
        } else {
            svf.VrSetForm("KNJL345W.frm", 1);
        }
        setTitle(db2, svf);
        int printCnt = 0;
        int boy = 0;
        int girl = 0;
        int total = 0;
        int pass = 0;
        int fail = 0;
        int unExam = 0;
        int ent = 0;
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final PrintData printData = (PrintData) iterator.next();
            svf.VrsOut("RELEVANCE", "該当あり");
            final String nameField = KNJ_EditEdit.getMS932ByteLength(printData._name) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(printData._name) > 20 ? "2" : "1";
            svf.VrsOut("NAME" + nameField, printData._name);
            svf.VrsOut("SEX", printData._sexName);
            svf.VrsOut("FORM", printData._gaitouYoushiki);
            final String fsField = KNJ_EditEdit.getMS932ByteLength(printData._finschoolName) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(printData._finschoolName) > 20 ? "2" : "1";
            svf.VrsOut("FINSCHOOL_NAME" + fsField, printData._finschoolName);
            final String prefField = KNJ_EditEdit.getMS932ByteLength(printData._prefName) > 8 ? "2" : "";
            svf.VrsOut("PREF" + prefField, printData._prefName);
            final String gakkaField = KNJ_EditEdit.getMS932ByteLength(printData._sucMajorName) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(printData._sucMajorName) > 20 ? "2" : "1";
            svf.VrsOut("DEPARTMENT_NAME" + gakkaField, printData._sucMajorName);
            final String senbatuField = KNJ_EditEdit.getMS932ByteLength(printData._testdivName) > 24 ? "3" : KNJ_EditEdit.getMS932ByteLength(printData._testdivName) > 16 ? "2" : "1";
            svf.VrsOut("EXAM_DIV" + senbatuField, printData._testdivName);
            svf.VrsOut("SECOND_HOPE", printData._dai2Shibou);
            svf.VrsOut("JUDGE", printData._judge);
            svf.VrsOut("ENT", printData._entdiv);
            svf.VrsOut("MOVE_DATE", printData._moveDate);
            final String mvpField = KNJ_EditEdit.getMS932ByteLength(printData._movePlace) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(printData._movePlace) > 20 ? "2" : "1";
            svf.VrsOut("MOVE_PLACE" + mvpField, printData._movePlace);

            total++;
            if ("1".equals(printData._sex)) {
                boy++;
            } else {
                girl++;
            }
            if ("○".equals(printData._judge)) {
                pass++;
            }
            if ("×".equals(printData._judge)) {
                fail++;
            }
            if ("－".equals(printData._judge)) {
                unExam++;
            }
            if ("○".equals(printData._entdiv)) {
                ent++;
            }
            printCnt++;
            if (printCnt >= printList.size()) {
                setGoukei(svf, boy, girl, total, pass, fail, unExam, ent);
            }
            svf.VrEndRecord();
            _hasData = true;
        }
        if (!_hasData) {
            svf.VrsOut("RELEVANCE", "該当なし");
            svf.VrEndPage();
            _hasData = true;
        }
    }

    private void setGoukei(final Vrw32alp svf, int boy, int girl, int total, int pass, int fail, int unExam, int ent) {
        svf.VrsOut("TOTAL_NUM", String.valueOf(total));
        svf.VrsOut("TOTAL_M_NUM", String.valueOf(boy));
        svf.VrsOut("TOTAL_F_NUM", String.valueOf(girl));
        svf.VrsOut("PASS_NUM", String.valueOf(pass));
        svf.VrsOut("NOT_PASS_NUM", String.valueOf(fail));
        svf.VrsOut("NOT_EXAM_NUM", String.valueOf(unExam));
        svf.VrsOut("ENT_NUM", String.valueOf(ent));
    }

    private void setTitle(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrsOut("TITLE", KNJ_EditDate.h_format_JP_N(db2, _param._entexamYear + "/04/01") + "度　志願学区外高等学校入学志願許可者の受検・入学者の状況調べ");
        svf.VrsOut("SUBTITLE", "[" + KNJ_EditDate.h_format_JP(db2, _param._ctrlDate) + "]現在");
        svf.VrsOut("AREA", _param._schoolData._distName);
        svf.VrsOut("SCHOOL_NAME", _param._schoolData._schoolName);
        svf.VrsOut("COURSE", _param._schoolData._courseName);
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String examno = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String sex = rs.getString("SEX");
                final String sexName = rs.getString("SEX_NAME");
                final String fsCd = rs.getString("FS_CD");
                final String finschoolName = rs.getString("FINSCHOOL_NAME");
                final String prefName = StringUtils.defaultString(rs.getString("PREF_NAME"));
                final String sucMajor = rs.getString("SUC_MAJOR");
                final String sucMajorName = rs.getString("SUC_MAJOR_NAME");
                final String testdiv = rs.getString("TESTDIV");
                final String testdivName = rs.getString("TESTDIV_NAME");
                final String dai2Shibou = rs.getString("DAI2_SHIBOU");
                final String judge = rs.getString("JUDGE");
                final String entdiv = rs.getString("ENTDIV");
                final String moveDate = rs.getString("MOVE_DATE");
                final String movePlace = rs.getString("MOVE_PLACE");
                String gaitouYoushiki = "";
                if ("1".equals(rs.getString("HOSHOUNIN_TODOKE"))) {
                    gaitouYoushiki = "15";
                } else if ("1".equals(rs.getString("KENGAI_CHUUGAKKOU_SHUSSHIN"))) {
                    gaitouYoushiki = "14";
                } else if ("1".equals(rs.getString("NYUUGAKU_SIGAN_KYOKA"))) {
                    gaitouYoushiki = "13-2";
//                } else if ("1".equals(rs.getString("KENGAI_HOSHOUNIN_ZAIJUU")) && "1".equals(rs.getString("KENGAI_ZAIJUU"))) { // 指示画面から「KENGAI_HOSHOUNIN_ZAIJUU」はカットになった
//                    gaitouYoushiki = "13-1(受保)";
//                } else if ("1".equals(rs.getString("KENGAI_HOSHOUNIN_ZAIJUU"))) {
//                    gaitouYoushiki = "13-1(保)";
                } else if ("1".equals(rs.getString("KENGAI_ZAIJUU"))) {
                    gaitouYoushiki = "13-1";
                } else if ("1".equals(rs.getString("TUUGAKU_KUIKIGAI_KYOKA"))) {
                    gaitouYoushiki = "12";
                }

                final PrintData printData = new PrintData(examno, name, sex, sexName, fsCd, finschoolName, prefName, sucMajor, sucMajorName, testdiv, testdivName, dai2Shibou, judge, entdiv, moveDate, movePlace, gaitouYoushiki);
                retList.add(printData);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     T1.SEX, ");
        stb.append("     N1.ABBV1 AS SEX_NAME, ");
        stb.append("     T1.FS_CD, ");
        stb.append("     FIN.FINSCHOOL_NAME, ");
        stb.append("     CASE WHEN ZIP.PREF = '三重県' THEN ZIP.CITY ELSE ZIP.PREF END AS PREF_NAME, ");
        stb.append("     T1.SUC_COURSECD || T1.SUC_MAJORCD AS SUC_MAJOR, ");
        stb.append("     L1.COURSENAME || L2.MAJORNAME AS SUC_MAJOR_NAME, ");
        stb.append("     T1.TESTDIV, ");
        stb.append("     N2.NAME1 AS TESTDIV_NAME, ");
        stb.append("     CASE WHEN T1.LAST_DAI2_COURSECD || T1.LAST_DAI2_MAJORCD || T1.LAST_DAI2_COURSECODE = T1.SUC_COURSECD || T1.SUC_MAJORCD || T1.SUC_COURSECODE THEN '○' ELSE '' END AS DAI2_SHIBOU, ");
        stb.append("     CASE WHEN N3.NAMESPARE1 = '1' THEN '○' WHEN T1.JUDGEMENT='2' THEN '×' WHEN T1.JUDGEMENT='4' THEN '－' ELSE '' END AS JUDGE, ");
        stb.append("     CASE WHEN N3.NAMESPARE1 = '1' AND T1.ENTDIV='1' THEN '○' ELSE '×' END AS ENTDIV, ");
        stb.append("     BD032.REMARK2 AS MOVE_DATE, ");
        stb.append("     BD032.REMARK3 AS MOVE_PLACE, ");
        stb.append("     T1.HOSHOUNIN_TODOKE, ");
        stb.append("     T1.KENGAI_CHUUGAKKOU_SHUSSHIN, ");
        stb.append("     T1.NYUUGAKU_SIGAN_KYOKA, ");
//        stb.append("     T1.KENGAI_HOSHOUNIN_ZAIJUU, ");
        stb.append("     T1.KENGAI_ZAIJUU, ");
        stb.append("     T1.TUUGAKU_KUIKIGAI_KYOKA ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT L3 ON T1.ENTEXAMYEAR = L3.ENTEXAMYEAR AND T1.APPLICANTDIV = L3.APPLICANTDIV AND T1.EXAMNO = L3.EXAMNO ");
        stb.append("     LEFT JOIN ZIPCD_MST ZIP ON L3.GZIPCD = ZIP.NEW_ZIPCD ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FIN ON T1.FS_CD = FIN.FINSCHOOLCD ");
        stb.append("     LEFT JOIN COURSE_MST L1 ON T1.SUC_COURSECD = L1.COURSECD ");
        stb.append("     LEFT JOIN MAJOR_MST L2 ON T1.SUC_COURSECD = L2.COURSECD AND T1.SUC_MAJORCD = L2.MAJORCD ");
        stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z002' AND T1.SEX = N1.NAMECD2 ");
        stb.append("     LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'L004' AND T1.TESTDIV = N2.NAMECD2 ");
        stb.append("     LEFT JOIN NAME_MST N3 ON N3.NAMECD1 = 'L013' AND T1.JUDGEMENT = N3.NAMECD2 ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD032 ");
        stb.append("          ON BD032.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND BD032.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND BD032.EXAMNO = T1.EXAMNO ");
        stb.append("         AND BD032.SEQ = '032' ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR  = '" + _param._entexamYear + "' AND ");
        stb.append("     T1.JUDGEMENT   != '5' AND ");
        stb.append("     (T1.TUUGAKU_KUIKIGAI_KYOKA = '1' ");
        stb.append("      OR T1.KENGAI_ZAIJUU = '1' ");
//        stb.append("      OR T1.KENGAI_HOSHOUNIN_ZAIJUU = '1' ");
        stb.append("      OR T1.NYUUGAKU_SIGAN_KYOKA = '1' ");
        stb.append("      OR T1.KENGAI_CHUUGAKKOU_SHUSSHIN = '1' ");
        stb.append("      OR T1.HOSHOUNIN_TODOKE = '1' ");
        stb.append("      ) ");
        stb.append(" ORDER BY ");
        stb.append("     T1.EXAMNO ");

        return stb.toString();
    }

    private class PrintData {
        final String _examno;
        final String _name;
        final String _sex;
        final String _sexName;
        final String _fsCd;
        final String _finschoolName;
        final String _prefName;
        final String _sucMajor;
        final String _sucMajorName;
        final String _testdiv;
        final String _testdivName;
        final String _dai2Shibou;
        final String _judge;
        final String _entdiv;
        final String _moveDate;
        final String _movePlace;
        final String _gaitouYoushiki;
        public PrintData(
                final String examno,
                final String name,
                final String sex,
                final String sexName,
                final String fsCd,
                final String finschoolName,
                final String prefName,
                final String sucMajor,
                final String sucMajorName,
                final String testdiv,
                final String testdivName,
                final String dai2Shibou,
                final String judge,
                final String entdiv,
                final String moveDate,
                final String movePlace,
                final String gaitouYoushiki
        ) {
            _examno = examno;
            _name = name;
            _sex = sex;
            _sexName = sexName;
            _fsCd = fsCd;
            _finschoolName = finschoolName;
            _prefName = prefName;
            _sucMajor = sucMajor;
            _sucMajorName = sucMajorName;
            _testdiv = testdiv;
            _testdivName = testdivName;
            _dai2Shibou = dai2Shibou;
            _judge = judge;
            _entdiv = entdiv;
            _moveDate = moveDate;
            _movePlace = movePlace;
            _gaitouYoushiki = gaitouYoushiki;
        }
    }

    private class SchoolData {
        final String _schoolCd;
        final String _schoolName;
        final String _courseCd;
        final String _courseName;
        final String _distName;
        public SchoolData(
                final String schoolCd,
                final String schoolName,
                final String courseCd,
                final String courseName,
                final String distName
        ) {
            _schoolCd = schoolCd;
            _schoolName = schoolName;
            _courseCd = courseCd;
            _courseName = courseName;
            _distName = distName;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 65610 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _entexamYear;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _useSchoolKindField;
        private final String _schoolkind;
        private final String _schoolCd;
        private final SchoolData _schoolData;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamYear = request.getParameter("ENTEXAMYEAR");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _useSchoolKindField = StringUtils.defaultString(request.getParameter("useSchool_KindField"));
            _schoolkind = StringUtils.defaultString(request.getParameter("SCHOOLKIND"));
            _schoolCd = StringUtils.defaultString(request.getParameter("SCHOOLCD"));
            _schoolData = getSchoolInfo(db2);
        }

        private SchoolData getSchoolInfo(final DB2UDB db2) throws SQLException {
            SchoolData schoolData = null;
            final String sql = getSchoolInfoSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schoolCd = rs.getString("KYOUIKU_IINKAI_SCHOOLCD");
                    final String schoolName = rs.getString("FINSCHOOL_NAME");
                    final String courseCd = rs.getString("COURSECD");
                    final String courseName = rs.getString("COURSENAME");
                    final String distName = rs.getString("DIST_NAME");
                    schoolData = new SchoolData(schoolCd, schoolName, courseCd, courseName, distName);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return schoolData;
        }

        private String getSchoolInfoSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH COURSE AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.* ");
            stb.append("     FROM ");
            stb.append("         COURSE_MST T1, ");
            stb.append("         (SELECT ");
            stb.append("              MIN(COURSECD) AS COURSECD ");
            stb.append("          FROM ");
            stb.append("              V_COURSE_MST ");
            stb.append("          WHERE ");
            stb.append("              YEAR = '" + _ctrlYear + "' ");
            stb.append("         ) T2 ");
            stb.append("     WHERE ");
            stb.append("         T1.COURSECD = T2.COURSECD ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.KYOUIKU_IINKAI_SCHOOLCD, ");
            stb.append("     T2.FINSCHOOL_NAME, ");
            stb.append("     COURSE.COURSECD, ");
            stb.append("     COURSE.COURSENAME, ");
            stb.append("     L1.NAME1 AS DIST_NAME ");
            stb.append(" FROM ");
            stb.append("     V_SCHOOL_MST T1, ");
            stb.append("     FINSCHOOL_MST T2 ");
            stb.append("     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'Z015' AND T2.FINSCHOOL_DISTCD2 = L1.NAMECD2, ");
            stb.append("     COURSE ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _ctrlYear + "' AND ");
            stb.append("     T1.KYOUIKU_IINKAI_SCHOOLCD = T2.FINSCHOOLCD ");
            if ("1".equals(_useSchoolKindField) && !"".equals(_schoolkind)) {
                stb.append(" AND T1.SCHOOLCD     = '" + _schoolCd + "' ");
                stb.append(" AND T1.SCHOOL_KIND  = '" + _schoolkind + "' ");
            }

            return stb.toString();
        }
    }
}

// eof
