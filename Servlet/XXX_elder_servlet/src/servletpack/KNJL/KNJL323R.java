/*
 * $Id: 7525b52983839eb03917a885ce6b4f76d40f1631 $
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３２３Ｒ＞  奨学生一覧表
 **/
public class KNJL323R {

    private static final Log log = LogFactory.getLog(KNJL323R.class);

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
        final String form = "KNJL323R.frm";
        final int maxLine = 50;

        final List list = Applicant.load(db2, _param);

        final List pageList = getPageList(list, maxLine);
        for (int pi = 0; pi < pageList.size(); pi++) {
            final List applList = (List) pageList.get(pi);

            svf.VrSetForm(form, 1);

            svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "年度　" + _param._testdivName + "奨学生一覧表"); // タイトル
            svf.VrsOut("DATE", _param._dateStr); // 印刷日

            for (int i = 0; i < applList.size(); i++) {
                final Applicant appl = (Applicant) applList.get(i);
                final int line = i + 1;
                svf.VrsOutn("EXAM_NO", line, appl._examno); // 受験番号
                svf.VrsOutn("HONOR", line, appl._judgeKindName); // 特待生
                final String remark1 = (null != appl._judgeKind && null != appl._scholarship) ? "専願" : "得点";
                svf.VrsOutn("REMARK1", line, remark1); // 備考
                // svf.VrsOutn("REMARK2", line, null); // 備考欄
                svf.VrsOutn("SCORE6", line, appl._total4); // 合計点
                svf.VrsOutn("JUDGE", line, appl._sucCoursemark); // 合否
                svf.VrsOutn("CONSENT_COURSE1", line, appl._befCoursemark); // 内諾コース
                svf.VrsOutn("CONSENT_COURSE2", line, appl._sub); // 内諾コース
                svf.VrsOutn("EXAM_EXIST", line, appl._juken); // 受験有無
                svf.VrsOutn("INTERVIEW", line, appl._interviewValue); // 面接
                svf.VrsOutn("NAME1", line, appl._name); // 名前
                svf.VrsOutn("JH_CODE", line, appl._fsCd); // 中学校コード
                svf.VrsOutn("JH_NAME1", line, appl._finschoolName); // 中学校名
                svf.VrsOutn("SEX", line, appl._sexName); // 性別
                svf.VrsOutn("COURSE1", line, appl._examCoursemark); // コース
                svf.VrsOutn("JH_SCORE", line, appl._kasantenAll); // 内申90点満点
            }
            svf.VrEndPage();
            _hasData = true;
        }

    }

    private static List getPageList(final List list, final int count) {
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

    private static class Applicant {
        final String _examno;
        final String _judgeKind;
        final String _judgeKindName;
        final String _scholarship;
        final String _total4;
        final String _sucCoursemark;
        final String _befCoursemark;
        final String _sub;
        final String _juken;
        final String _interviewValue;
        final String _name;
        final String _fsCd;
        final String _finschoolName;
        final String _sex;
        final String _sexName;
        final String _examCoursemark;
        final String _kasantenAll;

        Applicant(
            final String examno,
            final String judgeKind,
            final String judgeKindName,
            final String scholarship,
            final String total4,
            final String sucCoursemark,
            final String befCoursemark,
            final String sub,
            final String juken,
            final String interviewValue,
            final String name,
            final String fsCd,
            final String finschoolName,
            final String sex,
            final String sexName,
            final String examCoursemark,
            final String kasantenAll
        ) {
            _examno = examno;
            _judgeKind = judgeKind;
            _judgeKindName = judgeKindName;
            _scholarship = scholarship;
            _total4 = total4;
            _sucCoursemark = sucCoursemark;
            _befCoursemark = befCoursemark;
            _sub = sub;
            _juken = juken;
            _interviewValue = interviewValue;
            _name = name;
            _fsCd = fsCd;
            _finschoolName = finschoolName;
            _sex = sex;
            _sexName = sexName;
            _examCoursemark = examCoursemark;
            _kasantenAll = kasantenAll;
        }

        public static List load(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql(param));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String examno = rs.getString("EXAMNO");
                    final String judgeKind = rs.getString("JUDGE_KIND");
                    final String judgeKindName = rs.getString("JUDGE_KIND_NAME");
                    final String scholarship = rs.getString("SCHOLARSHIP");
                    final String total4 = rs.getString("TOTAL4");
                    final String sucCoursemark = rs.getString("SUC_COURSEMARK");
                    final String befCoursemark = rs.getString("BEF_COURSEMARK");
                    final String sub = rs.getString("SUB");
                    final String juken = rs.getString("JUKEN");
                    final String interviewValue = rs.getString("INTERVIEW_VALUE");
                    final String name = rs.getString("NAME");
                    final String fsCd = rs.getString("FS_CD");
                    final String finschoolName = rs.getString("FINSCHOOL_NAME");
                    final String sex = rs.getString("SEX");
                    final String sexName = rs.getString("SEX_NAME");
                    final String examCoursemark = rs.getString("EXAM_COURSEMARK");
                    final String kasantenAll = rs.getString("KASANTEN_ALL");
                    final Applicant applicant = new Applicant(examno, judgeKind, judgeKindName, scholarship, total4, sucCoursemark, befCoursemark, sub, juken, interviewValue, name, fsCd, finschoolName, sex, sexName, examCoursemark, kasantenAll);
                    list.add(applicant);
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
            stb.append(" SELECT ");
            stb.append("     BASE.EXAMNO, ");
            stb.append("     BASE.JUDGE_KIND, ");
            stb.append("     NML031.NAME1 AS JUDGE_KIND_NAME, ");
            stb.append("     BEF.SCHOLARSHIP, ");
            stb.append("     RECEPT.TOTAL4, ");
            stb.append("     SUCC.EXAMCOURSE_MARK AS SUC_COURSEMARK, ");
            stb.append("     BEFC.EXAMCOURSE_MARK AS BEF_COURSEMARK, ");
            stb.append("     CASE WHEN BEFC.EXAMCOURSECD IS NOT NULL THEN '" + param._testdivAbbv3 + "' END AS SUB, ");
            stb.append("     CASE WHEN RECEPT.TOTAL4 IS NOT NULL THEN '○' ELSE '×' END AS JUKEN, ");
            stb.append("     INTV.INTERVIEW_VALUE, ");
            stb.append("     BASE.NAME, ");
            stb.append("     BASE.FS_CD, ");
            stb.append("     FIN.FINSCHOOL_NAME, ");
            stb.append("     BASE.SEX, ");
            stb.append("     NMZ002.NAME2 AS SEX_NAME, ");
            stb.append("     W1C.EXAMCOURSE_MARK AS EXAM_COURSEMARK, ");
            stb.append("     CFRPT.KASANTEN_ALL ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T2 ON T2.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND T2.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("         AND T2.EXAMNO       = BASE.EXAMNO ");
            stb.append("         AND T2.SEQ          = '002' ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CFRPT ON CFRPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND CFRPT.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("         AND CFRPT.EXAMNO       = BASE.EXAMNO ");
            stb.append("     LEFT JOIN ENTEXAM_COURSE_MST SUCC ON SUCC.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND SUCC.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("         AND SUCC.TESTDIV = BASE.TESTDIV ");
            stb.append("         AND SUCC.COURSECD = BASE.SUC_COURSECD ");
            stb.append("         AND SUCC.MAJORCD = BASE.SUC_MAJORCD ");
            stb.append("         AND SUCC.EXAMCOURSECD = BASE.SUC_COURSECODE ");
            stb.append("     LEFT JOIN V_FINSCHOOL_MST FIN ON FIN.YEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND FIN.FINSCHOOLCD = BASE.FS_CD ");
            stb.append("     LEFT JOIN ENTEXAM_RECEPT_DAT RECEPT ON RECEPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND RECEPT.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("         AND RECEPT.TESTDIV = BASE.TESTDIV ");
            stb.append("         AND RECEPT.EXAM_TYPE = '1' ");
            stb.append("         AND RECEPT.EXAMNO = BASE.EXAMNO ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DETAIL ON DETAIL.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND DETAIL.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("         AND DETAIL.EXAMNO       = BASE.EXAMNO ");
            stb.append("         AND DETAIL.SEQ          = '002' ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANT_BEFORE_DAT BEF ON BEF.ENTEXAMYEAR = BASE.ENTEXAMYEAR  ");
            stb.append("         AND BEF.APPLICANTDIV = BASE.APPLICANTDIV  ");
            stb.append("         AND BEF.TESTDIV = BASE.TESTDIV  ");
            stb.append("         AND BEF.BEFORE_PAGE = DETAIL.REMARK1  ");
            stb.append("         AND BEF.BEFORE_SEQ = DETAIL.REMARK2  ");
            stb.append("     LEFT JOIN ENTEXAM_COURSE_MST BEFC ON BEFC.ENTEXAMYEAR = BASE.ENTEXAMYEAR  ");
            stb.append("         AND BEFC.APPLICANTDIV = BASE.APPLICANTDIV  ");
            stb.append("         AND BEFC.TESTDIV = BASE.TESTDIV  ");
            stb.append("         AND BEFC.COURSECD = BEF.BEFORE_COURSECD  ");
            stb.append("         AND BEFC.MAJORCD = BEF.BEFORE_MAJORCD  ");
            stb.append("         AND BEFC.EXAMCOURSECD = BEF.BEFORE_EXAMCOURSECD  ");
            stb.append("     LEFT JOIN ENTEXAM_WISHDIV_MST W1 ON W1.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND W1.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("         AND W1.TESTDIV = BASE.TESTDIV ");
            stb.append("         AND W1.DESIREDIV = BASE.DESIREDIV ");
            stb.append("         AND W1.WISHNO = '1' ");
            stb.append("     LEFT JOIN ENTEXAM_COURSE_MST W1C ON W1C.ENTEXAMYEAR = W1.ENTEXAMYEAR ");
            stb.append("         AND W1C.APPLICANTDIV = W1.APPLICANTDIV ");
            stb.append("         AND W1C.TESTDIV = W1.TESTDIV ");
            stb.append("         AND W1C.COURSECD = W1.COURSECD ");
            stb.append("         AND W1C.MAJORCD = W1.MAJORCD ");
            stb.append("         AND W1C.EXAMCOURSECD = W1.EXAMCOURSECD ");
            stb.append("     LEFT JOIN ENTEXAM_INTERVIEW_DAT INTV ON INTV.ENTEXAMYEAR = BASE.ENTEXAMYEAR  ");
            stb.append("         AND INTV.APPLICANTDIV = BASE.APPLICANTDIV  ");
            stb.append("         AND INTV.TESTDIV = BASE.TESTDIV  ");
            stb.append("         AND INTV.EXAMNO = BASE.EXAMNO ");
            stb.append("     LEFT JOIN NAME_MST NMZ002 ON NMZ002.NAMECD1 = 'Z002' ");
            stb.append("         AND NMZ002.NAMECD2 = BASE.SEX ");
            stb.append("     LEFT JOIN NAME_MST NML031 ON NML031.NAMECD1 = 'L031' ");
            stb.append("         AND NML031.NAMECD2 = BASE.JUDGE_KIND ");
            stb.append(" WHERE ");
            stb.append("     BASE.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("     AND BASE.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("     AND BASE.TESTDIV = '" + param._testdiv + "' ");
            stb.append("     AND BASE.JUDGE_KIND IS NOT NULL ");
            stb.append(" ORDER BY ");
            if ("2".equals(param._output)) {
                stb.append("     VALUE(RECEPT.TOTAL4, 0) DESC, ");
            }
            stb.append("     BASE.EXAMNO ");
            return stb.toString();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 63854 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _date;
        final String _output; // 出力順 1:受験番号 2:出身校 3:コース別

        final String _applicantdivName;
        final String _testdivName;
        final String _testdivAbbv3;
        final String _dateStr;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _date = request.getParameter("CTRL_DATE");
            _output = request.getParameter("OUTPUT");
            _dateStr = getDateStr(_date);

            _applicantdivName = getNameMst(db2, "NAME1", "L003", _applicantdiv);
            _testdivName = getNameMst(db2, "NAME1", "L004", _testdiv);
            _testdivAbbv3 = getNameMst(db2, "ABBV3", "L004", _testdiv);
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

