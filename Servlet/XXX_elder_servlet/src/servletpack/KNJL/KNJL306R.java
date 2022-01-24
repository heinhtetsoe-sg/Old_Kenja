/*
 * $Id: 03204d1bc8c62359364848bddc831461b4c74c17 $
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
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３０６Ｒ＞  願書と専願台帳照会チェックリスト
 **/
public class KNJL306R {

    private static final Log log = LogFactory.getLog(KNJL306R.class);

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
        final String form = "1".equals(_param._testdiv) ? "KNJL305R_1.frm" : "KNJL305R_2.frm";

        final List pageList = getPageList(ApplicantCheck.load(db2, _param), 50);

        for (int pi = 0; pi < pageList.size(); pi++) {
            final List checkList = (List) pageList.get(pi);

            svf.VrSetForm(form, 1);

            svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "年度　" + _param._testdivAbbv1 + "入試願書-" + _param._testdivAbbv2 + "照会リスト"); // タイトル
            svf.VrsOut("HOPE_KIND", _param._testdivAbbv2); // 専願区分
            svf.VrsOut("DATE", _param._dateStr); // 印刷日
            svf.VrsOut("PAGE1", String.valueOf(pi + 1)); // ページ
            svf.VrsOut("PAGE2", String.valueOf(pageList.size())); // ページ

            for (int j = 0; j < checkList.size(); j++) {
                final ApplicantCheck appl = (ApplicantCheck) checkList.get(j);
                final int gyo = j + 1;
                svf.VrsOutn("EXAM_NO", gyo, appl._baseExamno); // 受験番号
                svf.VrsOutn("KANA1_1", gyo, appl.split(appl._baseNameKana)[0]); // 名前
                svf.VrsOutn("KANA1_2", gyo, appl.split(appl._baseNameKana)[1]); // 名前
                svf.VrsOutn("SEX1", gyo, appl._baseSexName); // 性別
                svf.VrsOutn("JH_CODE1", gyo, appl._baseFsCd); // 中学校コード
                svf.VrsOutn("JH_NAME1", gyo, appl._baseFinschoolName); // 中学校名
                svf.VrsOutn("COURSE1", gyo, appl._baseExamCoursemark1); // コース
                svf.VrsOutn("COURSE2", gyo, appl._baseExamCoursemark2); // コース
                svf.VrsOutn("COURSE3", gyo, appl._baseExamCoursemark3); // コース
                svf.VrsOutn("COURSE4", gyo, appl._baseExamCoursemark4); // コース
                if ("2".equals(_param._testdiv)) {
                    svf.VrsOutn("EXAM_EXIST", gyo, appl._baseJudgementName); // 受験有無
                }
                svf.VrsOutn("PAGE", gyo, appl._beforePage); // ページ
                svf.VrsOutn("NUMBER", gyo, NumberUtils.isDigits(appl._beforeSeq) ? String.valueOf(Integer.parseInt(appl._beforeSeq)) : appl._beforeSeq); // 番号
                svf.VrsOutn("KANA2_1", gyo, appl.split(appl._befNameKana)[0]); // 名前
                svf.VrsOutn("KANA2_2", gyo, appl.split(appl._befNameKana)[1]); // 名前
                svf.VrsOutn("SEX2", gyo, appl._befSexName); // 性別
                svf.VrsOutn("CONSENT_COURSE1", gyo, appl._befCourseMark); // 内諾コース
                svf.VrsOutn("CONSENT_COURSE2", gyo, appl._befSub); // サブ
                final String nankanFlg = "1".equals(appl._nankanFlg) ? "有" : "";
                svf.VrsOutn("DIF_COURSE", gyo, nankanFlg); // 難関コース希望
                svf.VrsOutn("REMARK", gyo, appl._befRemark); // 備考
                svf.VrsOutn("JH_CODE2", gyo, appl._befFsCd); // 中学校コード
                svf.VrsOutn("JH_NAME2", gyo, appl._befFinschoolName); // 中学校名
                if ("2".equals(_param._testdiv)) {
                    if (null != appl._befSenbatu1School) {
                        String pass = appl._befSenbatu1School;
                        if ("1".equals(appl._befSenbatu1)) {
                            pass += "（合格）";
                        }
                        svf.VrsOutn("SELECT_PASS", gyo, pass); // 選抜
                    }
                }
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
        final String _baseSex;
        final String _baseSexName;
        final String _baseFsCd;
        final String _baseFinschoolName;
        final String _baseExamCoursemark1;
        final String _baseExamCoursemark2;
        final String _baseExamCoursemark3;
        final String _baseExamCoursemark4;
        final String _baseJudgementName;
        final String _beforePage;
        final String _beforeSeq;
        final String _befNameKana;
        final String _befSex;
        final String _befSexName;
        final String _befCourseMark;
        final String _befSub;
        final String _befFsCd;
        final String _befFinschoolName;
        final String _nankanFlg;
        final String _befRemark;
        final String _befSenbatu1School;
        final String _befSenbatu1;

        ApplicantCheck(
            final String baseExamno,
            final String baseNameKana,
            final String baseSex,
            final String baseSexName,
            final String baseFsCd,
            final String baseFinschoolName,
            final String baseExamCoursemark1,
            final String baseExamCoursemark2,
            final String baseExamCoursemark3,
            final String baseExamCoursemark4,
            final String baseJudgementName,
            final String beforePage,
            final String beforeSeq,
            final String befNameKana,
            final String befSex,
            final String befSexName,
            final String befCourseMark,
            final String befSub,
            final String befFsCd,
            final String befFinschoolName,
            final String nankanFlg,
            final String befRemark,
            final String befSenbatu1School,
            final String befSenbatu1
        ) {
            _baseExamno = baseExamno;
            _baseNameKana = baseNameKana;
            _baseSex = baseSex;
            _baseSexName = baseSexName;
            _baseFsCd = baseFsCd;
            _baseFinschoolName = baseFinschoolName;
            _baseExamCoursemark1 = baseExamCoursemark1;
            _baseExamCoursemark2 = baseExamCoursemark2;
            _baseExamCoursemark3 = baseExamCoursemark3;
            _baseExamCoursemark4 = baseExamCoursemark4;
            _baseJudgementName = baseJudgementName;
            _beforePage = beforePage;
            _beforeSeq = beforeSeq;
            _befNameKana = befNameKana;
            _befSex = befSex;
            _befSexName = befSexName;
            _befCourseMark = befCourseMark;
            _befSub = befSub;
            _befFsCd = befFsCd;
            _befFinschoolName = befFinschoolName;
            _nankanFlg = nankanFlg;
            _befRemark = befRemark;
            _befSenbatu1School = befSenbatu1School;
            _befSenbatu1 = befSenbatu1;
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
                final String sql = sql(param);
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String baseExamno = rs.getString("BASE_EXAMNO");
                    final String baseNameKana = rs.getString("BASE_NAME_KANA");
                    final String baseSex = rs.getString("BASE_SEX");
                    final String baseSexName = rs.getString("BASE_SEX_NAME");
                    final String baseFsCd = rs.getString("BASE_FS_CD");
                    final String baseFinschoolName = rs.getString("BASE_FINSCHOOL_NAME");
                    final String baseExamCoursemark1 = rs.getString("BASE_EXAM_COURSEMARK1");
                    final String baseExamCoursemark2 = rs.getString("BASE_EXAM_COURSEMARK2");
                    final String baseExamCoursemark3 = rs.getString("BASE_EXAM_COURSEMARK3");
                    final String baseExamCoursemark4 = rs.getString("BASE_EXAM_COURSEMARK4");
                    final String baseJudgementName = rs.getString("BASE_JUDGEMENT_NAME");
                    final String beforePage = rs.getString("BEFORE_PAGE");
                    final String beforeSeq = rs.getString("BEFORE_SEQ");
                    final String befNameKana = rs.getString("BEF_NAME_KANA");
                    final String befSex = rs.getString("BEF_SEX");
                    final String befSexName = rs.getString("BEF_SEX_NAME");
                    final String befCourseMark = rs.getString("BEF_COURSEMARK");
                    final String befSub = rs.getString("BEF_SUB");
                    final String befFsCd = rs.getString("BEF_FS_CD");
                    final String befFinschoolName = rs.getString("BEF_FINSCHOOL_NAME");
                    final String befRecomFlgName = rs.getString("BEF_RECOM_FLG_NAME");
                    final String befRecomRemark = null == rs.getString("BEF_RECOM_FLG") || null == rs.getString("BEF_RECOM_REMARK") ? "" : "（" + rs.getString("BEF_RECOM_REMARK") + "）" ;
                    final String nankanFlg = rs.getString("BEF_NANKAN_FLG");
                    final String rsBefRemark = rs.getString("BEF_REMARK");
                    final String befRemark = StringUtils.defaultString(befRecomFlgName) + StringUtils.defaultString(befRecomRemark) + StringUtils.defaultString(rsBefRemark);
                    final String befSenbatu1School = rs.getString("BEF_SENBATU1_SCHOOL");
                    final String befSenbatu1 = rs.getString("BEF_SENBATU1");

                    final ApplicantCheck applicantcheck = new ApplicantCheck(baseExamno, baseNameKana, baseSex, baseSexName, baseFsCd, baseFinschoolName, baseExamCoursemark1, baseExamCoursemark2, baseExamCoursemark3, baseExamCoursemark4, baseJudgementName, beforePage, beforeSeq, befNameKana, befSex, befSexName, befCourseMark, befSub, befFsCd, befFinschoolName, nankanFlg, befRemark, befSenbatu1School, befSenbatu1);
                    list.add(applicantcheck);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        public static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH BASE AS ( ");
            stb.append("     SELECT ");
            stb.append("         BASE.EXAMNO, ");
            stb.append("         BASE.NAME_KANA, ");
            stb.append("         BASE.SEX, ");
            stb.append("         NMZ002.ABBV1 AS SEX_NAME, ");
            stb.append("         BASE.FS_CD, ");
            stb.append("         T7.FINSCHOOL_NAME, ");
            stb.append("         BASE.DESIREDIV, ");
            stb.append("         T3C.COURSECD || T3C.MAJORCD || T3C.EXAMCOURSECD AS EXAM_COURSE1, ");
            stb.append("         T4C.COURSECD || T4C.MAJORCD || T4C.EXAMCOURSECD AS EXAM_COURSE2, ");
            stb.append("         T5C.COURSECD || T5C.MAJORCD || T5C.EXAMCOURSECD AS EXAM_COURSE3, ");
            stb.append("         T6C.COURSECD || T6C.MAJORCD || T6C.EXAMCOURSECD AS EXAM_COURSE4, ");
            stb.append("         T3C.EXAMCOURSE_MARK AS EXAM_COURSEMARK1, ");
            stb.append("         T4C.EXAMCOURSE_MARK AS EXAM_COURSEMARK2, ");
            stb.append("         T5C.EXAMCOURSE_MARK AS EXAM_COURSEMARK3, ");
            stb.append("         T6C.EXAMCOURSE_MARK AS EXAM_COURSEMARK4, ");
            stb.append("         CASE WHEN RECEPT.TOTAL4 IS NOT NULL THEN '○' ELSE '×' END AS JUDGEMENT_NAME ");
            stb.append("     FROM ");
            stb.append("         ENTEXAM_APPLICANTBASE_DAT BASE ");
            stb.append("         LEFT JOIN ENTEXAM_RECEPT_DAT RECEPT ON RECEPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("             AND RECEPT.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("             AND RECEPT.TESTDIV = BASE.TESTDIV ");
            stb.append("             AND RECEPT.EXAM_TYPE = '1' ");
            stb.append("             AND RECEPT.EXAMNO = BASE.EXAMNO ");
            stb.append("         LEFT JOIN ENTEXAM_WISHDIV_MST T3 ON T3.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("             AND T3.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("             AND T3.TESTDIV = BASE.TESTDIV ");
            stb.append("             AND T3.DESIREDIV = BASE.DESIREDIV ");
            stb.append("             AND T3.WISHNO = '1' ");
            stb.append("         LEFT JOIN ENTEXAM_COURSE_MST T3C ON T3C.ENTEXAMYEAR = T3.ENTEXAMYEAR ");
            stb.append("             AND T3C.APPLICANTDIV = T3.APPLICANTDIV ");
            stb.append("             AND T3C.TESTDIV = T3.TESTDIV ");
            stb.append("             AND T3C.COURSECD = T3.COURSECD ");
            stb.append("             AND T3C.MAJORCD = T3.MAJORCD ");
            stb.append("             AND T3C.EXAMCOURSECD = T3.EXAMCOURSECD ");
            stb.append("         LEFT JOIN ENTEXAM_WISHDIV_MST T4 ON T4.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("             AND T4.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("             AND T4.TESTDIV = BASE.TESTDIV ");
            stb.append("             AND T4.DESIREDIV = BASE.DESIREDIV ");
            stb.append("             AND T4.WISHNO = '2' ");
            stb.append("         LEFT JOIN ENTEXAM_COURSE_MST T4C ON T4C.ENTEXAMYEAR = T4.ENTEXAMYEAR ");
            stb.append("             AND T4C.APPLICANTDIV = T4.APPLICANTDIV ");
            stb.append("             AND T4C.TESTDIV = T4.TESTDIV ");
            stb.append("             AND T4C.COURSECD = T4.COURSECD ");
            stb.append("             AND T4C.MAJORCD = T4.MAJORCD ");
            stb.append("             AND T4C.EXAMCOURSECD = T4.EXAMCOURSECD ");
            stb.append("         LEFT JOIN ENTEXAM_WISHDIV_MST T5 ON T5.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("             AND T5.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("             AND T5.TESTDIV = BASE.TESTDIV ");
            stb.append("             AND T5.DESIREDIV = BASE.DESIREDIV ");
            stb.append("             AND T5.WISHNO = '3' ");
            stb.append("         LEFT JOIN ENTEXAM_COURSE_MST T5C ON T5C.ENTEXAMYEAR = T5.ENTEXAMYEAR ");
            stb.append("             AND T5C.APPLICANTDIV = T5.APPLICANTDIV ");
            stb.append("             AND T5C.TESTDIV = T5.TESTDIV ");
            stb.append("             AND T5C.COURSECD = T5.COURSECD ");
            stb.append("             AND T5C.MAJORCD = T5.MAJORCD ");
            stb.append("             AND T5C.EXAMCOURSECD = T5.EXAMCOURSECD ");
            stb.append("         LEFT JOIN ENTEXAM_WISHDIV_MST T6 ON T6.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("             AND T6.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("             AND T6.TESTDIV = BASE.TESTDIV ");
            stb.append("             AND T6.DESIREDIV = BASE.DESIREDIV ");
            stb.append("             AND T6.WISHNO = '4' ");
            stb.append("         LEFT JOIN ENTEXAM_COURSE_MST T6C ON T6C.ENTEXAMYEAR = T6.ENTEXAMYEAR ");
            stb.append("             AND T6C.APPLICANTDIV = T6.APPLICANTDIV ");
            stb.append("             AND T6C.TESTDIV = T6.TESTDIV ");
            stb.append("             AND T6C.COURSECD = T6.COURSECD ");
            stb.append("             AND T6C.MAJORCD = T6.MAJORCD ");
            stb.append("             AND T6C.EXAMCOURSECD = T6.EXAMCOURSECD ");
            stb.append("         LEFT JOIN FINSCHOOL_MST T7 ON T7.FINSCHOOLCD = BASE.FS_CD ");
            stb.append("         LEFT JOIN NAME_MST NMZ002 ON NMZ002.NAMECD1 = 'Z002' ");
            stb.append("             AND NMZ002.NAMECD2 = BASE.SEX ");
            stb.append("     WHERE ");
            stb.append("         BASE.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("         AND BASE.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("         AND BASE.TESTDIV = '" + param._testdiv + "' ");
            stb.append(" ), APPLICANT_BEFORE AS ( ");
            stb.append("     SELECT ");
            stb.append("         BEF.BEFORE_PAGE, ");
            stb.append("         BEF.BEFORE_SEQ, ");
            stb.append("         BEF.NAME_KANA, ");
            stb.append("         BEF.SEX, ");
            stb.append("         NMZ002.NAME2 AS SEX_NAME, ");
            stb.append("         T3C.COURSECD || T3C.MAJORCD || T3C.EXAMCOURSECD AS BEF_COURSE, ");
            stb.append("         T3C.EXAMCOURSECD AS BEF_EXAMCOURSECD, ");
            stb.append("         T3C.EXAMCOURSE_MARK AS BEF_COURSEMARK, ");
            stb.append("         '" + param._testdivAbbv3 + "' AS SUB, ");
            stb.append("         BEF.FS_CD, ");
            stb.append("         T6.FINSCHOOL_NAME, ");
            stb.append("         BEF.NANKAN_FLG, ");            
            stb.append("         BEF.RECOM_FLG, ");
            stb.append("         NML032.NAME2 AS RECOM_FLG_NAME, ");
            stb.append("         BEF.RECOM_REMARK, ");
            stb.append("         BEF.REMARK, ");
            stb.append("         BEF.SENBATU1_SCHOOL, ");
            stb.append("         T4C.REMARK1 AS SENBATU1 ");
            stb.append("     FROM ");
            stb.append("         ENTEXAM_APPLICANT_BEFORE_DAT BEF ");
            stb.append("         LEFT JOIN NAME_MST NMZ002 ON NMZ002.NAMECD1 = 'Z002' ");
            stb.append("             AND NMZ002.NAMECD2 = BEF.SEX ");
            stb.append("         LEFT JOIN NAME_MST NML032 ON NML032.NAMECD1 = 'L032' ");
            stb.append("             AND NML032.NAMECD2 = BEF.RECOM_FLG ");
            stb.append("         LEFT JOIN V_FINSCHOOL_MST T6 ON T6.YEAR = BEF.ENTEXAMYEAR ");
            stb.append("            AND T6.FINSCHOOLCD = BEF.FS_CD ");
            stb.append("         LEFT JOIN ENTEXAM_COURSE_MST T3C ON T3C.ENTEXAMYEAR = BEF.ENTEXAMYEAR ");
            stb.append("             AND T3C.APPLICANTDIV = BEF.APPLICANTDIV ");
            stb.append("             AND T3C.TESTDIV = BEF.TESTDIV ");
            stb.append("             AND T3C.COURSECD = BEF.BEFORE_COURSECD ");
            stb.append("             AND T3C.MAJORCD = BEF.BEFORE_MAJORCD ");
            stb.append("             AND T3C.EXAMCOURSECD = BEF.BEFORE_EXAMCOURSECD ");
            stb.append("         LEFT JOIN (SELECT T4.* ");
            stb.append("                 FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT T4 ");
            stb.append("                 INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON ");
            stb.append("                      BASE.ENTEXAMYEAR  = T4.ENTEXAMYEAR ");
            stb.append("                  AND BASE.APPLICANTDIV = T4.APPLICANTDIV ");
            stb.append("                  AND BASE.TESTDIV      = '" + param._testdiv + "' ");
            stb.append("                  AND BASE.EXAMNO       = T4.EXAMNO ");
            stb.append("                 WHERE T4.ENTEXAMYEAR = '" + param._entexamyear + "' AND T4.APPLICANTDIV = '" + param._applicantdiv + "' AND T4.SEQ = '002' ");
            stb.append("              )  T4 ON T4.REMARK1 = BEF.BEFORE_PAGE AND T4.REMARK2 = BEF.BEFORE_SEQ ");
            stb.append("         LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T4C ON T4C.ENTEXAMYEAR = T4.ENTEXAMYEAR ");
            stb.append("             AND T4C.APPLICANTDIV = T4.APPLICANTDIV ");
            stb.append("             AND T4C.EXAMNO = T4.EXAMNO ");
            stb.append("             AND T4C.SEQ = '003' ");
            stb.append("     WHERE ");
            stb.append("         BEF.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("         AND BEF.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("         AND BEF.TESTDIV = '" + param._testdiv + "' ");
            stb.append(" ), COMMON AS ( ");
            stb.append("     SELECT T4.EXAMNO, T4.REMARK1, T4.REMARK2 ");
            stb.append("     FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT T4 ");
            stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON ");
            stb.append("             BASE.ENTEXAMYEAR  = T4.ENTEXAMYEAR ");
            stb.append("         AND BASE.APPLICANTDIV = T4.APPLICANTDIV ");
            stb.append("         AND BASE.TESTDIV      = '" + param._testdiv + "' ");
            stb.append("         AND BASE.EXAMNO       = T4.EXAMNO ");
            stb.append("     WHERE ");
            stb.append("             T4.ENTEXAMYEAR  = '" + param._entexamyear + "' ");
            stb.append("         AND T4.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("         AND T4.SEQ          = '002' ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.EXAMNO AS BASE_EXAMNO, ");
            stb.append("     T1.NAME_KANA AS BASE_NAME_KANA, ");
            stb.append("     T1.SEX AS BASE_SEX, ");
            stb.append("     T1.SEX_NAME AS BASE_SEX_NAME, ");
            stb.append("     T1.FS_CD AS BASE_FS_CD, ");
            stb.append("     T1.FINSCHOOL_NAME AS BASE_FINSCHOOL_NAME, ");
            stb.append("     T1.DESIREDIV, ");
            stb.append("     T1.EXAM_COURSE1 AS BASE_EXAM_COURSE1, ");
            stb.append("     T1.EXAM_COURSE2 AS BASE_EXAM_COURSE2, ");
            stb.append("     T1.EXAM_COURSE3 AS BASE_EXAM_COURSE3, ");
            stb.append("     T1.EXAM_COURSE4 AS BASE_EXAM_COURSE4, ");
            stb.append("     T1.EXAM_COURSEMARK1 AS BASE_EXAM_COURSEMARK1, ");
            stb.append("     T1.EXAM_COURSEMARK2 AS BASE_EXAM_COURSEMARK2, ");
            stb.append("     T1.EXAM_COURSEMARK3 AS BASE_EXAM_COURSEMARK3, ");
            stb.append("     T1.EXAM_COURSEMARK4 AS BASE_EXAM_COURSEMARK4, ");
            stb.append("     T1.JUDGEMENT_NAME AS BASE_JUDGEMENT_NAME, ");
            stb.append("     T2.BEFORE_PAGE, ");
            stb.append("     T2.BEFORE_SEQ, ");
            stb.append("     T2.NAME_KANA AS BEF_NAME_KANA, ");
            stb.append("     T2.SEX AS BEF_SEX, ");
            stb.append("     T2.SEX_NAME AS BEF_SEX_NAME, ");
            stb.append("     T2.BEF_COURSE, ");
            stb.append("     T2.BEF_EXAMCOURSECD, ");
            stb.append("     T2.BEF_COURSEMARK, ");
            stb.append("     T2.SUB AS BEF_SUB, ");
            stb.append("     T2.FS_CD AS BEF_FS_CD, ");
            stb.append("     T2.FINSCHOOL_NAME AS BEF_FINSCHOOL_NAME, ");
            stb.append("     T2.NANKAN_FLG AS BEF_NANKAN_FLG, ");
            stb.append("     T2.RECOM_FLG AS BEF_RECOM_FLG, ");
            stb.append("     T2.RECOM_FLG_NAME AS BEF_RECOM_FLG_NAME, ");
            stb.append("     T2.RECOM_REMARK AS BEF_RECOM_REMARK, ");
            stb.append("     T2.REMARK AS BEF_REMARK, ");
            stb.append("     T2.SENBATU1_SCHOOL AS BEF_SENBATU1_SCHOOL, ");
            stb.append("     T2.SENBATU1 AS BEF_SENBATU1 ");
            stb.append(" FROM COMMON T0 ");
            stb.append(" INNER JOIN BASE T1 ON T1.EXAMNO = T0.EXAMNO ");
            stb.append(" INNER JOIN APPLICANT_BEFORE T2 ON T2.BEFORE_PAGE = T0.REMARK1 AND T2.BEFORE_SEQ = T0.REMARK2 ");
            stb.append(" WHERE ");
            stb.append("  BEF_EXAMCOURSECD = '0002' AND NOT (T1.DESIREDIV = '3' OR T1.DESIREDIV = '6') ");
            stb.append("  OR ");
            stb.append("  BEF_EXAMCOURSECD = '0003' AND NOT (T1.DESIREDIV = '2' OR T1.DESIREDIV = '5' OR T1.DESIREDIV = '8') ");
            stb.append("  OR ");
            stb.append("  BEF_EXAMCOURSECD = '0004' AND NOT (T1.DESIREDIV = '1' OR T1.DESIREDIV = '4' OR T1.DESIREDIV = '7' OR T1.DESIREDIV = '9') ");
            stb.append(" ORDER BY ");
            if ("2".equals(param._output)) {
                stb.append("     BEFORE_PAGE, ");
                stb.append("     BEFORE_SEQ, ");
                stb.append("     BASE_EXAMNO ");
            } else { // if ("1".equals(param._output)) {
                stb.append("     BASE_EXAMNO, ");
                stb.append("     BEFORE_PAGE, ");
                stb.append("     BEFORE_SEQ ");
            }
            return stb.toString();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 71517 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _date;
        final String _output; // 表示順 1:受験番号 2:事前番号

        final String _applicantdivName;
        final String _testdivAbbv1;
        final String _testdivAbbv2;
        final String _testdivAbbv3;
        final String _dateStr;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _date = request.getParameter("CTRL_DATE");
            _output = request.getParameter("OUTPUT");

            _applicantdivName = getNameMst(db2, "NAME1", "L003", _applicantdiv);
            _testdivAbbv1 = StringUtils.defaultString(getNameMst(db2, "ABBV1", "L004", _testdiv));
            _testdivAbbv2 = StringUtils.defaultString(getNameMst(db2, "ABBV2", "L004", _testdiv));
            _testdivAbbv3 = StringUtils.defaultString(getNameMst(db2, "ABBV3", "L004", _testdiv));
            _dateStr = getDateStr(_date);
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
    }
}

// eof

