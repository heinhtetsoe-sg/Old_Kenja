/*
 * $Id: 7b0a34fd20156726ad24d4d25e3e00483ad71887 $
 *
 * 作成日: 2013/10/10
 * 作成者: maesiro
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

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３０５Ｒ＞  願書と専願台帳照会リスト
 **/
public class KNJL305F {

    private static final Log log = LogFactory.getLog(KNJL305F.class);

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

    public void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final List pageList = getPageList(ApplicantCheck.load(db2, _param), 50);
        for (int pi = 0; pi < pageList.size(); pi++) {
            final List checkList = (List) pageList.get(pi);

            svf.VrSetForm("KNJL305F.frm", 1);

            if ("1".equals(_param._applicantdiv)) {
                svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "年度　" + _param._applicantdivName + _param._testdivKotei + "入試願書-照会リスト"); // タイトル
            } else {
                svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "年度　" + _param._applicantdivName + _param._testdivAbbv1 + "入試願書-" + _param._testdivAbbv2 + "照会リスト"); // タイトル
            }
            svf.VrsOut("HOPE_KIND", _param._testdivAbbv2); // 専願区分
            svf.VrsOut("DATE", _param._dateStr); // 印刷日
            svf.VrsOut("PAGE1", String.valueOf(pi + 1)); // ページ
            svf.VrsOut("PAGE2", String.valueOf(pageList.size())); // ページ

            for (int j = 0; j < checkList.size(); j++) {
                final ApplicantCheck appl = (ApplicantCheck) checkList.get(j);
                final int gyo = j + 1;
                svf.VrsOutn("EXAM_NO", gyo, appl._baseExamno);
                svf.VrsOutn("KANA1_1", gyo, appl.split(appl._baseNameKana)[0]);
                svf.VrsOutn("KANA1_2", gyo, appl.split(appl._baseNameKana)[1]);
                svf.VrsOutn("JH_CODE1", gyo, appl._baseFsCd);
                svf.VrsOutn("JH_NAME1", gyo, appl._baseFinschoolName);
                svf.VrsOutn("COURSE1", gyo, appl._baseExamCoursemark1);
                svf.VrsOutn("PRE_NO", gyo, appl._befRecruitNo);
                svf.VrsOutn("KANA2_1", gyo, appl.split(appl._befNameKana)[0]);
                svf.VrsOutn("KANA2_2", gyo, appl.split(appl._befNameKana)[1]);
                svf.VrsOutn("JH_CODE2", gyo, appl._befFsCd);
                svf.VrsOutn("JH_NAME2", gyo, appl._befFinschoolName);
                svf.VrsOutn("SCHOLARSHIP", gyo, appl._befJudgeName);
            }
            svf.VrEndPage();
            _hasData = true;
        }
    }

    private List getPageList(final List list, final int count) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= count) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }

    private static class ApplicantCheck {
        final String _baseExamno;
        final String _baseNameKana;
        final String _baseFsCd;
        final String _baseFinschoolName;
        final String _baseExamCoursemark1;
        final String _befRecruitNo;
        final String _befNameKana;
        final String _befFsCd;
        final String _befFinschoolName;
        final String _befJudgeName;

        ApplicantCheck(
            final String baseExamno,
            final String baseNameKana,
            final String baseFsCd,
            final String baseFinschoolName,
            final String baseExamCoursemark1,
            final String befRecruitNo,
            final String befNameKana,
            final String befFsCd,
            final String befFinschoolName,
            final String befJudgeName
        ) {
            _baseExamno = baseExamno;
            _baseNameKana = baseNameKana;
            _baseFsCd = baseFsCd;
            _baseFinschoolName = baseFinschoolName;
            _baseExamCoursemark1 = baseExamCoursemark1;
            _befRecruitNo = befRecruitNo;
            _befNameKana = befNameKana;
            _befFsCd = befFsCd;
            _befFinschoolName = befFinschoolName;
            _befJudgeName = befJudgeName;
        }

        public String[] split(String s) {
            if (null == s) {
                return new String[] {null, null};
            }
            int idx = s.indexOf('　');
            if (-1 == idx) {
                return new String[] {s, null};
            }
            return new String[] {s.substring(0, idx), s.substring(idx + 1)};
        }

        public static List load(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                for (int i = 0; i < 3; i++) {
                    final String sql = sql(param, i);
                    log.debug(" sql = " + sql);
                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String baseExamno = rs.getString("BASE_EXAMNO");
                        final String baseNameKana = rs.getString("BASE_NAME_KANA");
                        final String baseFsCd = rs.getString("BASE_FS_CD");
                        final String baseFinschoolName = rs.getString("BASE_FINSCHOOL_NAME");
                        final String baseExamCoursemark1 = rs.getString("BASE_EXAM_COURSEMARK1");
                        final String befRecruitNo = rs.getString("RECRUIT_NO");
                        final String befNameKana = rs.getString("BEF_NAME_KANA");
                        final String befFsCd = rs.getString("BEF_FS_CD");
                        final String befFinschoolName = rs.getString("BEF_FINSCHOOL_NAME");
                        final String befJudgeName = rs.getString("BEF_JUDGENAME");

                        final ApplicantCheck applicantcheck = new ApplicantCheck(baseExamno, baseNameKana, baseFsCd, baseFinschoolName, baseExamCoursemark1, befRecruitNo, befNameKana, befFsCd, befFinschoolName, befJudgeName);
                        list.add(applicantcheck);
                    }
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        public static String sql(final Param param, final int i) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH BASE AS ( ");
            stb.append("     SELECT ");
            stb.append("         BASE.EXAMNO, ");
            stb.append("         BASE.NAME_KANA, ");
            stb.append("         BASE.FS_CD, ");
            stb.append("         T6.FINSCHOOL_NAME, ");
            stb.append("         ENT_COURSE.EXAMCOURSE_ABBV AS EXAM_COURSEMARK1 ");
            stb.append("     FROM ");
            stb.append("         ENTEXAM_APPLICANTBASE_DAT BASE ");
            stb.append("         LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DETAIL001 ON BASE.ENTEXAMYEAR = DETAIL001.ENTEXAMYEAR ");
            stb.append("             AND BASE.EXAMNO = DETAIL001.EXAMNO ");
            stb.append("             AND DETAIL001.SEQ = '001' ");
            stb.append("         LEFT JOIN ENTEXAM_COURSE_MST ENT_COURSE ON BASE.ENTEXAMYEAR = ENT_COURSE.ENTEXAMYEAR ");
            stb.append("             AND BASE.APPLICANTDIV = ENT_COURSE.APPLICANTDIV ");
            stb.append("             AND ENT_COURSE.TESTDIV = '1' ");
            stb.append("             AND DETAIL001.REMARK8 = ENT_COURSE.COURSECD ");
            stb.append("             AND DETAIL001.REMARK9 = ENT_COURSE.MAJORCD ");
            stb.append("             AND DETAIL001.REMARK10 = ENT_COURSE.EXAMCOURSECD ");
            stb.append("         LEFT JOIN FINSCHOOL_MST T6 ON T6.FINSCHOOLCD = BASE.FS_CD ");
            stb.append("     WHERE ");
            stb.append("         BASE.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("         AND BASE.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("         AND BASE.TESTDIV = '" + param._testdiv + "' ");
            if ("2".equals(param._applicantdiv)) {
                stb.append("         AND BASE.TESTDIV0 = '" + param._testdiv0 + "' ");
            }
            stb.append(" ), MAX_RECRUIT_VISIT AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.RECRUIT_NO, ");
            stb.append("     T1.JUDGE_KIND, ");
            if ("1".equals(param._applicantdiv)) {
                stb.append("     L1.NAME1 AS JUDGENAME ");
            } else {
                stb.append("     L1.NAME2 AS JUDGENAME ");
            }
            stb.append(" FROM ");
            stb.append("     RECRUIT_VISIT_DAT T1 ");
            stb.append("     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'L025' ");
            stb.append("          AND T1.JUDGE_KIND = L1.NAMECD2 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._entexamyear + "' ");
            stb.append(" ), APPLICANT_BEFORE AS ( ");
            stb.append("     SELECT ");
            stb.append("         BEF.RECRUIT_NO, ");
            stb.append("         BEF.NAME_KANA, ");
            stb.append("         BEF.FINSCHOOLCD AS FS_CD, ");
            stb.append("         T6.FINSCHOOL_NAME, ");
            stb.append("         RVISIT.JUDGENAME AS JUDGENAME ");
            stb.append("     FROM ");
            stb.append("         RECRUIT_DAT BEF ");
            stb.append("         LEFT JOIN V_FINSCHOOL_MST T6 ON T6.YEAR = BEF.YEAR ");
            stb.append("            AND T6.FINSCHOOLCD = BEF.FINSCHOOLCD ");
            stb.append("         LEFT JOIN MAX_RECRUIT_VISIT RVISIT ON BEF.RECRUIT_NO = RVISIT.RECRUIT_NO ");
            stb.append("     WHERE ");
            stb.append("         BEF.YEAR = '" + param._entexamyear + "' ");
            stb.append(" ), COMMON AS ( ");
            stb.append("     SELECT DISTINCT BASE.EXAMNO, BEF.RECRUIT_NO AS REMARK1 ");
            stb.append("     FROM ( ");
            stb.append("       SELECT NAME_KANA, FS_CD FROM BASE ");
            stb.append("       INTERSECT ");
            stb.append("       SELECT NAME_KANA, FS_CD FROM APPLICANT_BEFORE ");
            stb.append("     ) T1 ");
            stb.append("     INNER JOIN BASE ON BASE.NAME_KANA = T1.NAME_KANA ");
            stb.append("         AND BASE.FS_CD = T1.FS_CD ");
            stb.append("     INNER JOIN APPLICANT_BEFORE BEF ON BEF.NAME_KANA = T1.NAME_KANA ");
            stb.append("         AND BEF.FS_CD = T1.FS_CD ");
            stb.append(" ) ");
            if (0 == i) {
                stb.append(" SELECT ");
                stb.append("     T1.EXAMNO AS BASE_EXAMNO, ");
                stb.append("     T1.NAME_KANA AS BASE_NAME_KANA, ");
                stb.append("     T1.FS_CD AS BASE_FS_CD, ");
                stb.append("     T1.FINSCHOOL_NAME AS BASE_FINSCHOOL_NAME, ");
                stb.append("     T1.EXAM_COURSEMARK1 AS BASE_EXAM_COURSEMARK1, ");
                stb.append("     T2.RECRUIT_NO, ");
                stb.append("     T2.NAME_KANA AS BEF_NAME_KANA, ");
                stb.append("     T2.FS_CD AS BEF_FS_CD, ");
                stb.append("     T2.FINSCHOOL_NAME AS BEF_FINSCHOOL_NAME, ");
                stb.append("     T2.JUDGENAME AS BEF_JUDGENAME ");
                stb.append(" FROM COMMON T0 ");
                stb.append(" INNER JOIN BASE T1 ON T1.EXAMNO = T0.EXAMNO ");
                stb.append(" INNER JOIN APPLICANT_BEFORE T2 ON T2.RECRUIT_NO = T0.REMARK1 ");
            } else if (1 == i) {
                stb.append(" SELECT ");
                stb.append("     T1.EXAMNO AS BASE_EXAMNO, ");
                stb.append("     T1.NAME_KANA AS BASE_NAME_KANA, ");
                stb.append("     T1.FS_CD AS BASE_FS_CD, ");
                stb.append("     T1.FINSCHOOL_NAME AS BASE_FINSCHOOL_NAME, ");
                stb.append("     T1.EXAM_COURSEMARK1 AS BASE_EXAM_COURSEMARK1, ");
                stb.append("     CAST(NULL AS VARCHAR(1)) AS RECRUIT_NO, ");
                stb.append("     CAST(NULL AS VARCHAR(1)) AS BEF_NAME_KANA, ");
                stb.append("     CAST(NULL AS VARCHAR(1)) AS BEF_FS_CD, ");
                stb.append("     CAST(NULL AS VARCHAR(1)) AS BEF_FINSCHOOL_NAME, ");
                stb.append("     CAST(NULL AS VARCHAR(1)) AS BEF_JUDGENAME ");
                stb.append(" FROM BASE T1 ");
                stb.append(" WHERE NOT EXISTS (SELECT 'X' FROM COMMON T0 WHERE T1.EXAMNO = T0.EXAMNO) ");
            } else if (2 == i) {
                stb.append(" SELECT ");
                stb.append("     CAST(NULL AS VARCHAR(1)) AS BASE_EXAMNO, ");
                stb.append("     CAST(NULL AS VARCHAR(1)) AS BASE_NAME_KANA, ");
                stb.append("     CAST(NULL AS VARCHAR(1)) AS BASE_FS_CD, ");
                stb.append("     CAST(NULL AS VARCHAR(1)) AS BASE_FINSCHOOL_NAME, ");
                stb.append("     CAST(NULL AS VARCHAR(1)) AS BASE_EXAM_COURSEMARK1, ");
                stb.append("     T2.RECRUIT_NO, ");
                stb.append("     T2.NAME_KANA AS BEF_NAME_KANA, ");
                stb.append("     T2.FS_CD AS BEF_FS_CD, ");
                stb.append("     T2.FINSCHOOL_NAME AS BEF_FINSCHOOL_NAME, ");
                stb.append("     T2.JUDGENAME AS BEF_JUDGENAME ");
                stb.append(" FROM APPLICANT_BEFORE T2 ");
                stb.append(" WHERE NOT EXISTS (SELECT 'X' FROM COMMON T0 WHERE T2.RECRUIT_NO = T0.REMARK1) ");
            }
            stb.append(" ORDER BY ");
            if ("1".equals(param._output)) {
                stb.append("     BASE_EXAMNO ");
            } else {
                stb.append("     RECRUIT_NO, ");
                stb.append("     BASE_EXAMNO ");
            }
            return stb.toString();
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
    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _testdiv0;
        final String _date;
        final String _output; // 表示順 1:受験番号 2:事前番号

        final String _applicantdivName;
        final String _testdivAbbv1;
        final String _testdivAbbv2;
        final String _testdivAbbv3;
        final String _dateStr;
        final String _testdivKotei;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _testdiv0 = request.getParameter("TESTDIV0");
            _date = request.getParameter("CTRL_DATE");
            _output = request.getParameter("OUTPUT");

            _applicantdivName = getNameMst(db2, "NAME1", "L003", _applicantdiv);
            _testdivAbbv1 = StringUtils.defaultString(getNameMst(db2, "ABBV1", "L004", _testdiv));
            _testdivAbbv2 = StringUtils.defaultString(getNameMst(db2, "ABBV2", "L004", _testdiv));
            _testdivAbbv3 = StringUtils.defaultString(getNameMst(db2, "ABBV3", "L004", _testdiv));
            _dateStr = getDateStr(_date);
            _testdivKotei = getTestDivKotei(db2, _testdiv);
        }

        private String getDateStr(final String date) {
            if (null == date) {
                return null;
            }
            return KNJ_EditDate.h_format_JP(date);
        }

        private static String getNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + namecd2 + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }

        private static String getTestDivKotei(final DB2UDB db2, final String testdiv) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" WITH VAL_T(LABEL, VALUE) AS ( ");
                stb.append("     VALUES('帰国生', '1') ");
                stb.append("     UNION ");
                stb.append("     VALUES('一般', '2') ");
                stb.append(" ) ");
                stb.append(" SELECT ");
                stb.append("     LABEL ");
                stb.append(" FROM ");
                stb.append("     VAL_T ");
                stb.append(" WHERE ");
                stb.append("     VALUE = '" + testdiv + "' ");
                final String sql = stb.toString();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("LABEL");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }
    }
}

// eof

