/*
 * $Id: 6d173c9a6e4b6fb0cf30dfa75de9604590f62593 $
 *
 * 作成日: 2017/11/21
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
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

public class KNJL366W {

    private static final Log log = LogFactory.getLog(KNJL366W.class);

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

            for (Iterator itSchool = _param._schoolList.iterator(); itSchool.hasNext();) {
                final SchoolData schoolData = (SchoolData) itSchool.next();
                printMain(db2, svf, schoolData);
            }
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

    private void printMain(final DB2UDB db2, final Vrw32alp svf, final SchoolData schoolData) {
        final List printList = getList(db2, schoolData);

        //ヘッダー部
        svf.VrSetForm("KNJL366W.frm", 4);

//        final String yousiki = _param._isSigan ? "別紙様式9-1" : "別紙様式9-2";
        svf.VrsOut("TITLE", KNJ_EditDate.h_format_JP_N(db2, _param._entexamYear + "/04/01") + "度　" + "スポーツ特別枠選抜の状況調べ");
        svf.VrsOut("SCHOOL_NAME", schoolData._schoolName);
        svf.VrsOut("AREA", schoolData._distName);

        String grp = "";
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final PrintData printData = (PrintData) iterator.next();

            //データ部
            if (!"ALL".equals(printData._majorCd)) {
                svf.VrsOut("GRP", printData._sportCd);
                if (!grp.equals(printData._sportCd)) {
                    grp = printData._sportCd;
                    final String sportField = KNJ_EditEdit.getMS932ByteLength(printData._sportName) <= 26 ? "1" : KNJ_EditEdit.getMS932ByteLength(printData._sportName) <= 32 ? "2" : "3";
                    svf.VrsOut("CLUB_NAME" + sportField, printData._sportName);
                }
                final String majorField = KNJ_EditEdit.getMS932ByteLength(printData._majorName) <= 20 ? "1" : KNJ_EditEdit.getMS932ByteLength(printData._majorName) <= 30 ? "2" : "3";
                svf.VrsOut("DEPARTMENT_NAME" + majorField, printData._majorName);
//                svf.VrsOut("RECRUIT", printData._boshuuCnt);
                svf.VrsOut("APPLI", printData._siganCnt);
                if (!_param._isSigan) {
                    svf.VrsOut("INFORM1", printData._goukakuCnt);
                    svf.VrsOut("INFORM2", printData._zenkiGoukakuCnt);
                    svf.VrsOut("NOT_PASS", printData._fugoukakuCnt);
                }
            }

            //男子・女子・合計
            if ("ALL".equals(printData._majorCd)) {
                svf.VrsOut("TOTAL_NAME", printData._majorName);
//                svf.VrsOut("TOTAL_RECRUIT", printData._boshuuCnt);
                svf.VrsOut("TOTAL_APPLI", printData._siganCnt);
                if (!_param._isSigan) {
                    svf.VrsOut("TOTAL_INFORM1", printData._goukakuCnt);
                    svf.VrsOut("TOTAL_INFORM2", printData._zenkiGoukakuCnt);
                    svf.VrsOut("TOTAL_NOT_PASS", printData._fugoukakuCnt);
                }
            }

            svf.VrEndRecord();
            _hasData = true;
        }
    }

    private List getList(final DB2UDB db2, final SchoolData schoolData) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getListSql(schoolData._schoolCd);
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String sportCd = rs.getString("BOSHUU_KYOUGI");
                final String majorCd = rs.getString("MAJOR");
                final String sportName = rs.getString("BOSHUU_SPORT");
                final String majorName = rs.getString("BOSHUU_MAJOR");
                final String boshuuCnt = rs.getString("BOSHUU_CNT");
                final String siganCnt = rs.getString("SIGAN_CNT");
                final String goukakuCnt = rs.getString("GOUKAKU_CNT");
                final String zenkiGoukakuCnt = rs.getString("ZENKI_GOUKAKU_CNT");
                final String fugoukakuCnt = rs.getString("FUGOUKAKU_CNT");

                final PrintData printData = new PrintData(sportCd, majorCd, sportName, majorName, boshuuCnt, siganCnt, goukakuCnt, zenkiGoukakuCnt, fugoukakuCnt);
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

    private String getListSql(final String schoolCd) {
        final StringBuffer stb = new StringBuffer();
        //募集競技、募集学科
        stb.append(" WITH T_SPORT_MAJOR AS ( ");
        stb.append("     SELECT ");
        stb.append("         BOSHUU_KYOUGI, ");
        stb.append("         LAST_DAI1_COURSECD || LAST_DAI1_MAJORCD AS MAJOR ");
        stb.append("     FROM ");
        stb.append("         V_EDBOARD_ENTEXAM_APPLICANTBASE_DAT ");
        stb.append("     WHERE ");
        stb.append("         EDBOARD_SCHOOLCD = '" + schoolCd + "' ");
        stb.append("         AND ENTEXAMYEAR = '" + _param._entexamYear + "' ");
        stb.append("         AND TESTDIV = '4' ");
        stb.append("         AND VALUE(JUDGEMENT,'0') != '5' ");
        stb.append("         AND BOSHUU_KYOUGI IS NOT NULL ");
        stb.append("     UNION ");
        stb.append("     SELECT ");
        stb.append("         BOSHUU_KYOUGI, ");
        stb.append("         SUC_COURSECD || SUC_MAJORCD AS MAJOR ");
        stb.append("     FROM ");
        stb.append("         V_EDBOARD_ENTEXAM_APPLICANTBASE_DAT ");
        stb.append("     WHERE ");
        stb.append("         EDBOARD_SCHOOLCD = '" + schoolCd + "' ");
        stb.append("         AND ENTEXAMYEAR = '" + _param._entexamYear + "' ");
        stb.append("         AND TESTDIV = '4' ");
        stb.append("         AND VALUE(JUDGEMENT,'0') != '5' ");
        stb.append("         AND BOSHUU_KYOUGI IS NOT NULL ");
        stb.append("         AND SUC_MAJORCD IS NOT NULL ");
        stb.append(" ) ");
        stb.append(" , T_SPORT_MAJOR2 AS ( ");
        stb.append("     SELECT ");
        stb.append("         BOSHUU_KYOUGI, ");
        stb.append("         LAST_DAI1_COURSECD || LAST_DAI1_MAJORCD || LAST_DAI1_COURSECODE AS COURSECODE ");
        stb.append("     FROM ");
        stb.append("         V_EDBOARD_ENTEXAM_APPLICANTBASE_DAT ");
        stb.append("     WHERE ");
        stb.append("         EDBOARD_SCHOOLCD = '" + schoolCd + "' ");
        stb.append("         AND ENTEXAMYEAR = '" + _param._entexamYear + "' ");
        stb.append("         AND TESTDIV = '4' ");
        stb.append("         AND VALUE(JUDGEMENT,'0') != '5' ");
        stb.append("         AND BOSHUU_KYOUGI IS NOT NULL ");
        stb.append("     UNION ");
        stb.append("     SELECT ");
        stb.append("         BOSHUU_KYOUGI, ");
        stb.append("         SUC_COURSECD || SUC_MAJORCD || SUC_COURSECODE AS COURSECODE ");
        stb.append("     FROM ");
        stb.append("         V_EDBOARD_ENTEXAM_APPLICANTBASE_DAT ");
        stb.append("     WHERE ");
        stb.append("         EDBOARD_SCHOOLCD = '" + schoolCd + "' ");
        stb.append("         AND ENTEXAMYEAR = '" + _param._entexamYear + "' ");
        stb.append("         AND TESTDIV = '4' ");
        stb.append("         AND VALUE(JUDGEMENT,'0') != '5' ");
        stb.append("         AND BOSHUU_KYOUGI IS NOT NULL ");
        stb.append("         AND SUC_MAJORCD IS NOT NULL ");
        stb.append(" ) ");
        //志願者数、不合格者数
        stb.append(" , T_SIGAN AS ( ");
        stb.append("     SELECT ");
        stb.append("         BOSHUU_KYOUGI, ");
        stb.append("         LAST_DAI1_COURSECD || LAST_DAI1_MAJORCD AS MAJOR, ");
        stb.append("         COUNT(*) AS SIGAN_CNT, ");
        stb.append("         SUM(CASE WHEN SEX = '1' THEN 1 ELSE 0 END) AS SIGAN_CNT1, ");
        stb.append("         SUM(CASE WHEN SEX = '2' THEN 1 ELSE 0 END) AS SIGAN_CNT2, ");
        stb.append("         SUM(CASE WHEN JUDGEMENT = '2' THEN 1 ELSE 0 END) AS FUGOUKAKU_CNT, ");
        stb.append("         SUM(CASE WHEN SEX = '1' AND JUDGEMENT = '2' THEN 1 ELSE 0 END) AS FUGOUKAKU_CNT1, ");
        stb.append("         SUM(CASE WHEN SEX = '2' AND JUDGEMENT = '2' THEN 1 ELSE 0 END) AS FUGOUKAKU_CNT2 ");
        stb.append("     FROM ");
        stb.append("         V_EDBOARD_ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("     WHERE ");
        stb.append("         T1.EDBOARD_SCHOOLCD = '" + schoolCd + "' ");
        stb.append("         AND ENTEXAMYEAR = '" + _param._entexamYear + "' ");
        stb.append("         AND TESTDIV = '4' ");
        stb.append("         AND VALUE(JUDGEMENT,'0') != '5' ");
        stb.append("         AND BOSHUU_KYOUGI IS NOT NULL ");
        stb.append("     GROUP BY ");
        stb.append("         BOSHUU_KYOUGI, ");
        stb.append("         LAST_DAI1_COURSECD, ");
        stb.append("         LAST_DAI1_MAJORCD ");
        stb.append(" ) ");
        //合格内定者数、前期合格内定者数
        stb.append(" , T_GOUKAKU AS ( ");
        stb.append("     SELECT ");
        stb.append("         BOSHUU_KYOUGI, ");
        stb.append("         SUC_COURSECD || SUC_MAJORCD AS MAJOR, ");
        stb.append("         SUM(CASE WHEN JUDGEMENT = '1' THEN 1 ELSE 0 END) AS GOUKAKU_CNT, ");
        stb.append("         SUM(CASE WHEN JUDGEMENT = '3' THEN 1 ELSE 0 END) AS ZENKI_GOUKAKU_CNT, ");
        stb.append("         SUM(CASE WHEN SEX = '1' AND JUDGEMENT = '1' THEN 1 ELSE 0 END) AS GOUKAKU_CNT1, ");
        stb.append("         SUM(CASE WHEN SEX = '1' AND JUDGEMENT = '3' THEN 1 ELSE 0 END) AS ZENKI_GOUKAKU_CNT1, ");
        stb.append("         SUM(CASE WHEN SEX = '2' AND JUDGEMENT = '1' THEN 1 ELSE 0 END) AS GOUKAKU_CNT2, ");
        stb.append("         SUM(CASE WHEN SEX = '2' AND JUDGEMENT = '3' THEN 1 ELSE 0 END) AS ZENKI_GOUKAKU_CNT2 ");
        stb.append("     FROM ");
        stb.append("         V_EDBOARD_ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("     WHERE ");
        stb.append("         T1.EDBOARD_SCHOOLCD = '" + schoolCd + "' ");
        stb.append("         AND ENTEXAMYEAR = '" + _param._entexamYear + "' ");
        stb.append("         AND TESTDIV = '4' ");
        stb.append("         AND VALUE(JUDGEMENT,'0') != '5' ");
        stb.append("         AND BOSHUU_KYOUGI IS NOT NULL ");
        stb.append("         AND SUC_MAJORCD IS NOT NULL ");
        stb.append("     GROUP BY ");
        stb.append("         BOSHUU_KYOUGI, ");
        stb.append("         SUC_COURSECD, ");
        stb.append("         SUC_MAJORCD ");
        stb.append(" ) ");
        //募集人数(募集競技・募集学科)
        stb.append(" , T_BOSHUU_CNT AS ( ");
        stb.append("     SELECT ");
        stb.append("         T_SPORT.BOSHUU_KYOUGI, ");
        stb.append("         SUM(CAPACITY2) AS BOSHUU_CNT ");
        stb.append("     FROM ");
        stb.append("         T_SPORT_MAJOR2 T_SPORT ");
        stb.append("         LEFT JOIN EDBOARD_ENTEXAM_COURSE_MST COURSE_M ON COURSE_M.EDBOARD_SCHOOLCD = '" + schoolCd + "' ");
        stb.append("              AND COURSE_M.ENTEXAMYEAR = '" + _param._entexamYear + "' ");
        stb.append("              AND COURSE_M.TESTDIV = '4' ");
        stb.append("              AND T_SPORT.COURSECODE = COURSE_M.COURSECD || COURSE_M.MAJORCD || COURSE_M.EXAMCOURSECD ");
        stb.append("     GROUP BY ");
        stb.append("         T_SPORT.BOSHUU_KYOUGI ");
        stb.append(" ) ");
        //募集人数(合計)
        stb.append(" , T_BOSHUU_ALL AS ( ");
        stb.append("     SELECT ");
        stb.append("         'ALL' AS MAJOR, ");
        stb.append("         SUM(CAPACITY2) AS BOSHUU_CNT ");
        stb.append("     FROM ");
        stb.append("         EDBOARD_ENTEXAM_COURSE_MST ");
        stb.append("     WHERE ");
        stb.append("         EDBOARD_SCHOOLCD = '" + schoolCd + "' ");
        stb.append("         AND ENTEXAMYEAR = '" + _param._entexamYear + "' ");
        stb.append("         AND TESTDIV = '4' ");
        stb.append(" ) ");

        //メイン
        //募集競技・募集学科
        stb.append(" SELECT ");
        stb.append("     T1.BOSHUU_KYOUGI, ");
        stb.append("     T1.MAJOR, ");
        stb.append("     L1.SPORT_NAME AS BOSHUU_SPORT, ");
        stb.append("     L2.MAJORNAME AS BOSHUU_MAJOR, ");
        stb.append("     S0.BOSHUU_CNT, ");
        stb.append("     S1.SIGAN_CNT, ");
        stb.append("     S2.GOUKAKU_CNT, ");
        stb.append("     S2.ZENKI_GOUKAKU_CNT, ");
        stb.append("     S1.FUGOUKAKU_CNT ");
        stb.append(" FROM ");
        stb.append("     T_SPORT_MAJOR T1 ");
        stb.append("     LEFT JOIN T_SIGAN S1 ON S1.BOSHUU_KYOUGI = T1.BOSHUU_KYOUGI AND S1.MAJOR = T1.MAJOR ");
        stb.append("     LEFT JOIN T_GOUKAKU S2 ON S2.BOSHUU_KYOUGI = T1.BOSHUU_KYOUGI AND S2.MAJOR = T1.MAJOR ");
        stb.append("     LEFT JOIN EDBOARD_ENTEXAM_SPORT_MST L1 ON L1.EDBOARD_SCHOOLCD = '" + schoolCd + "' AND L1.ENTEXAMYEAR = '" + _param._entexamYear + "' AND L1.SPORT_CD = T1.BOSHUU_KYOUGI ");
        stb.append("     LEFT JOIN EDBOARD_MAJOR_MST L2 ON L2.EDBOARD_SCHOOLCD = '" + schoolCd + "' AND L2.COURSECD || L2.MAJORCD = T1.MAJOR ");
        stb.append("     LEFT JOIN T_BOSHUU_CNT S0 ON S0.BOSHUU_KYOUGI = T1.BOSHUU_KYOUGI ");
        //男子
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     'BOY' AS BOSHUU_KYOUGI, ");
        stb.append("     'ALL' AS MAJOR, ");
        stb.append("     '' AS BOSHUU_SPORT, ");
        stb.append("     '男子' AS BOSHUU_MAJOR, ");
        stb.append("     0 AS BOSHUU_CNT, ");
        stb.append("     SUM(S1.SIGAN_CNT1) AS SIGAN_CNT, ");
        stb.append("     SUM(S2.GOUKAKU_CNT1) AS GOUKAKU_CNT, ");
        stb.append("     SUM(S2.ZENKI_GOUKAKU_CNT1) AS ZENKI_GOUKAKU_CNT, ");
        stb.append("     SUM(S1.FUGOUKAKU_CNT1) AS FUGOUKAKU_CNT ");
        stb.append(" FROM ");
        stb.append("     T_SPORT_MAJOR T1 ");
        stb.append("     LEFT JOIN T_SIGAN S1 ON S1.BOSHUU_KYOUGI = T1.BOSHUU_KYOUGI AND S1.MAJOR = T1.MAJOR ");
        stb.append("     LEFT JOIN T_GOUKAKU S2 ON S2.BOSHUU_KYOUGI = T1.BOSHUU_KYOUGI AND S2.MAJOR = T1.MAJOR ");
        //女子
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     'GIRL' AS BOSHUU_KYOUGI, ");
        stb.append("     'ALL' AS MAJOR, ");
        stb.append("     '' AS BOSHUU_SPORT, ");
        stb.append("     '女子' AS BOSHUU_MAJOR, ");
        stb.append("     0 AS BOSHUU_CNT, ");
        stb.append("     SUM(S1.SIGAN_CNT2) AS SIGAN_CNT, ");
        stb.append("     SUM(S2.GOUKAKU_CNT2) AS GOUKAKU_CNT, ");
        stb.append("     SUM(S2.ZENKI_GOUKAKU_CNT2) AS ZENKI_GOUKAKU_CNT, ");
        stb.append("     SUM(S1.FUGOUKAKU_CNT2) AS FUGOUKAKU_CNT ");
        stb.append(" FROM ");
        stb.append("     T_SPORT_MAJOR T1 ");
        stb.append("     LEFT JOIN T_SIGAN S1 ON S1.BOSHUU_KYOUGI = T1.BOSHUU_KYOUGI AND S1.MAJOR = T1.MAJOR ");
        stb.append("     LEFT JOIN T_GOUKAKU S2 ON S2.BOSHUU_KYOUGI = T1.BOSHUU_KYOUGI AND S2.MAJOR = T1.MAJOR ");
        //合計
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     'TOTAL' AS BOSHUU_KYOUGI, ");
        stb.append("     'ALL' AS MAJOR, ");
        stb.append("     '' AS BOSHUU_SPORT, ");
        stb.append("     '合計' AS BOSHUU_MAJOR, ");
        stb.append("     MAX(S0.BOSHUU_CNT) AS BOSHUU_CNT, ");
        stb.append("     SUM(S1.SIGAN_CNT) AS SIGAN_CNT, ");
        stb.append("     SUM(S2.GOUKAKU_CNT) AS GOUKAKU_CNT, ");
        stb.append("     SUM(S2.ZENKI_GOUKAKU_CNT) AS ZENKI_GOUKAKU_CNT, ");
        stb.append("     SUM(S1.FUGOUKAKU_CNT) AS FUGOUKAKU_CNT ");
        stb.append(" FROM ");
        stb.append("     T_SPORT_MAJOR T1 ");
        stb.append("     LEFT JOIN T_SIGAN S1 ON S1.BOSHUU_KYOUGI = T1.BOSHUU_KYOUGI AND S1.MAJOR = T1.MAJOR ");
        stb.append("     LEFT JOIN T_GOUKAKU S2 ON S2.BOSHUU_KYOUGI = T1.BOSHUU_KYOUGI AND S2.MAJOR = T1.MAJOR ");
        stb.append("     LEFT JOIN T_BOSHUU_ALL S0 ON S0.MAJOR = 'ALL' ");
        stb.append(" ORDER BY ");
        stb.append("     BOSHUU_KYOUGI, ");
        stb.append("     MAJOR ");
        return stb.toString();
    }

    private class PrintData {
        final String _sportCd;
        final String _majorCd;
        final String _sportName;
        final String _majorName;
        final String _boshuuCnt;
        final String _siganCnt;
        final String _goukakuCnt;
        final String _zenkiGoukakuCnt;
        final String _fugoukakuCnt;
        public PrintData(
                final String sportCd,
                final String majorCd,
                final String sportName,
                final String majorName,
                final String boshuuCnt,
                final String siganCnt,
                final String goukakuCnt,
                final String zenkiGoukakuCnt,
                final String fugoukakuCnt
        ) {
            _sportCd = sportCd;
            _majorCd = majorCd;
            _sportName = sportName;
            _majorName = majorName;
            _boshuuCnt = boshuuCnt;
            _siganCnt = siganCnt;
            _goukakuCnt = goukakuCnt;
            _zenkiGoukakuCnt = zenkiGoukakuCnt;
            _fugoukakuCnt = fugoukakuCnt;
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
        log.fatal("$Revision: 65600 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final boolean _isSigan;
        private final String _entexamYear;
        private final String[] _selected;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _useSchoolKindField;
        private final String _schoolkind;
        private final String _schoolCd;
        private final List _schoolList;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException, ParseException {
            _isSigan = "1".equals(request.getParameter("CSVDIV"));
            _entexamYear = request.getParameter("ENTEXAMYEAR");
            _selected = request.getParameterValues("CATEGORY_SELECTED");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _useSchoolKindField = StringUtils.defaultString(request.getParameter("useSchool_KindField"));
            _schoolkind = StringUtils.defaultString(request.getParameter("SCHOOLKIND"));
            _schoolCd = StringUtils.defaultString(request.getParameter("SCHOOLCD"));
            _schoolList = getSchoolList(db2);
        }

        private List getSchoolList(final DB2UDB db2) throws SQLException, ParseException {
            final List retList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            for (int i = 0; i < _selected.length; i++) {
                final String schoolCd = _selected[i];
                try {
                    final String titleSql = getSchoolSql(schoolCd);
                    ps = db2.prepareStatement(titleSql);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String schoolName = rs.getString("FINSCHOOL_NAME");
                        final String courseCd = rs.getString("COURSECD");
                        final String courseName = rs.getString("COURSENAME");
                        final String distName = rs.getString("DIST_NAME");
                        final SchoolData schoolData = new SchoolData(schoolCd, schoolName, courseCd, courseName, distName);
                        retList.add(schoolData);
                    }
                } finally {
                    DbUtils.closeQuietly(null, ps, rs);
                }
            }
            return retList;
        }

        private String getSchoolSql(final String schoolCd) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH COURSE AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.* ");
            stb.append("     FROM ");
            stb.append("         EDBOARD_COURSE_MST T1, ");
            stb.append("         (SELECT ");
            stb.append("              MIN(COURSECD) AS COURSECD ");
            stb.append("          FROM ");
            stb.append("              EDBOARD_COURSE_MST ");
            stb.append("          WHERE ");
            stb.append("              EDBOARD_SCHOOLCD = '" + schoolCd + "' ");
            stb.append("         ) T2 ");
            stb.append("     WHERE ");
            stb.append("         T1.EDBOARD_SCHOOLCD = '" + schoolCd + "' ");
            stb.append("         AND T1.COURSECD = T2.COURSECD ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.EDBOARD_SCHOOLCD AS KYOUIKU_IINKAI_SCHOOLCD, ");
            stb.append("     T2.FINSCHOOL_NAME, ");
            stb.append("     COURSE.COURSECD, ");
            stb.append("     COURSE.COURSENAME, ");
            stb.append("     L1.NAME1 AS DIST_NAME ");
            stb.append(" FROM ");
            stb.append("     EDBOARD_SCHOOL_MST T1, ");
            stb.append("     FINSCHOOL_MST T2 ");
            stb.append("     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'Z015' AND T2.FINSCHOOL_DISTCD2 = L1.NAMECD2, ");
            stb.append("     COURSE ");
            stb.append(" WHERE ");
            stb.append("     T1.EDBOARD_SCHOOLCD = '" + schoolCd + "' ");
            stb.append("     AND T1.EDBOARD_SCHOOLCD = T2.FINSCHOOLCD ");

            return stb.toString();
        }
    }
}

// eof
