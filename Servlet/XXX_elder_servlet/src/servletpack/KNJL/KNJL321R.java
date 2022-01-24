/*
 * $Id: 1fbab5d72ed3e349809032bf43bd559ad808d861 $
 *
 * 作成日: 2013/10/10
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
 *                  ＜ＫＮＪＬ３２１Ｒ＞  合否判定資料
 **/
public class KNJL321R {

    private static final Log log = LogFactory.getLog(KNJL321R.class);

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
        final String form = "KNJL321R.frm";
        final int maxLine = 50;

        for (final Iterator itc = _param._examCourseList.iterator(); itc.hasNext();) {
            final Map examCourseMap = (Map) itc.next();
            final String examcourseCd = (String) examCourseMap.get("EXAMCOURSECD");
            final String examcourseMark = (String) examCourseMap.get("EXAMCOURSE_MARK");

            final List list = ReceptDat.load(db2, _param, examcourseCd);

            final List pageList = getPageList(list, maxLine);
            for (int pi = 0; pi < pageList.size(); pi++) {
                final List receptDatList = (List) pageList.get(pi);

                svf.VrSetForm(form, 1);
                svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "年度　" + _param._testdivName + "合否判定資料（" + ("1".equals(_param._output) ? "受験番号順" : "2".equals(_param._output) ? "成績順" : "") + "）"); // タイトル
                svf.VrsOut("HOPE_COURSE", examcourseMark); // タイトル
                svf.VrsOut("PAGE1", String.valueOf(pi + 1)); // ページ
                svf.VrsOut("PAGE2", String.valueOf(pageList.size())); // ページ

                int classi = 1;
                for (final Iterator it = _param._subclassNameMstMapList.iterator(); it.hasNext();) {
                    final Map map = (Map) it.next();
                    svf.VrsOut("CLASS" + classi, (String) map.get("NAME1")); // 教科名
                    classi++;
                }
                svf.VrsOut("DATE", _param._dateStr); // 印刷日
                final int startGyo = maxLine * pi;

                for (int j = 0; j < receptDatList.size(); j++) {
                    final ReceptDat recept = (ReceptDat) receptDatList.get(j);
                    final int line = j + 1;
                    svf.VrsOutn("NO", line, String.valueOf(startGyo + line)); // NO
                    svf.VrsOutn("EXAM_NO", line, recept._examno); // 受験番号
                    svf.VrsOutn("COURSE1", line, recept._examCoursemark1); // コース
                    svf.VrsOutn("COURSE2", line, recept._examCoursemark2); // コース
                    svf.VrsOutn("COURSE3", line, recept._examCoursemark3); // コース
                    svf.VrsOutn("COURSE4", line, recept._examCoursemark4); // コース
                    svf.VrsOutn("JH_SCORE", line, recept._kasantenAll); // 内申90点満点
                    for (int i = 0; i < Math.min(_param._subclassNameMstMapList.size(), 6); i++) {
                        final Map map = (Map) _param._subclassNameMstMapList.get(i);
                        final String testsubclasscd = (String) map.get("NAMECD2");
                        final String score = (String) recept._testSubclassMap.get(testsubclasscd);
                        svf.VrsOutn("SCORE" + (i + 1), line, score); // 教科得点
                    }
                    svf.VrsOutn("SCORE6", line, recept._total4); // 教科合計点
                    svf.VrsOutn("JUDGE", line, recept._sucCoursemark); // 合否
                    svf.VrsOutn("JUDGE_MARK", line, NumberUtils.isDigits(recept._sucCoursecode) ? String.valueOf(Integer.parseInt(recept._sucCoursecode)) : recept._sucCoursecode); // 合否記号
                    svf.VrsOutn("CONSENT_COURSE1", line, recept._naidakuCoursemark); // 内諾コース
                    svf.VrsOutn("CONSENT_COURSE2", line, recept._sub); // 内諾コースサブ
                    svf.VrsOutn("INTERVIEW", line, recept._interviewValue); // 面接
                    svf.VrsOutn("RANK", line, recept._totalRank4); // 順位
                    svf.VrsOutn("EXAM_EXIST", line, recept._juken); // 受験有無
                    svf.VrsOutn("NAME1", line, recept._name); // 名前
                    svf.VrsOutn("SEX", line, recept._sexName); // 性別
                    svf.VrsOutn("JH_CODE", line, recept._fsCd); // 中学校コード
                    svf.VrsOutn("JH_NAME1", line, recept._finschoolName); // 中学校名

                    svf.VrsOutn("REMARK", line, recept._scholarshipName); // 備考
                }
                svf.VrEndPage();
                _hasData = true;
            }
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

    private static class ReceptDat {
        final String _receptno;
        final String _examno;
        final String _examCoursemark1;
        final String _examCoursemark2;
        final String _examCoursemark3;
        final String _examCoursemark4;
        final String _name;
        final String _sex;
        final String _sexName;
        final String _fsCd;
        final String _finschoolName;
        final String _sucCoursecode;
        final String _sucCoursemark;
        final String _naidakuCoursemark;
        final String _sub;
        final String _kasantenAll;
        final String _total4;
        final String _totalRank4;
        final String _juken;
        final String _interviewValue;
        final String _scholarship;
        final String _scholarshipName;
        final Map _testSubclassMap;

        ReceptDat(
            final String receptno,
            final String examno,
            final String examCoursemark1,
            final String examCoursemark2,
            final String examCoursemark3,
            final String examCoursemark4,
            final String name,
            final String sex,
            final String sexName,
            final String fsCd,
            final String finschoolName,
            final String sucCoursecode,
            final String sucCoursemark,
            final String naidakuCoursemark,
            final String sub,
            final String kasantenAll,
            final String total4,
            final String totalRank4,
            final String juken,
            final String interviewValue,
            final String scholarship,
            final String scholarshipName
        ) {
            _receptno = receptno;
            _examno = examno;
            _examCoursemark1 = examCoursemark1;
            _examCoursemark2 = examCoursemark2;
            _examCoursemark3 = examCoursemark3;
            _examCoursemark4 = examCoursemark4;
            _name = name;
            _sex = sex;
            _sexName = sexName;
            _fsCd = fsCd;
            _finschoolName = finschoolName;
            _sucCoursecode = sucCoursecode;
            _sucCoursemark = sucCoursemark;
            _naidakuCoursemark = naidakuCoursemark;
            _sub = sub;
            _kasantenAll = kasantenAll;
            _total4 = total4;
            _totalRank4 = totalRank4;
            _juken = juken;
            _interviewValue = interviewValue;
            _testSubclassMap = new HashMap();
            _scholarship = scholarship;
            _scholarshipName = scholarshipName;
        }

        private static ReceptDat getReceptDat(final List list, final String examno) {
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final ReceptDat recept = (ReceptDat) it.next();
                if (recept._examno.equals(examno)) {
                    return recept;
                }
            }
            return null;
        }

        public static List load(final DB2UDB db2, final Param param, final String examCourseCd) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param, examCourseCd);
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String examno = rs.getString("EXAMNO");
                    if (null == getReceptDat(list, examno)) {
                        final String receptno = rs.getString("RECEPTNO");
                        final String examCoursemark1 = rs.getString("EXAM_COURSEMARK1");
                        final String examCoursemark2 = rs.getString("EXAM_COURSEMARK2");
                        final String examCoursemark3 = rs.getString("EXAM_COURSEMARK3");
                        final String examCoursemark4 = rs.getString("EXAM_COURSEMARK4");
                        final String name = rs.getString("NAME");
                        final String sex = rs.getString("SEX");
                        final String sexName = rs.getString("SEX_NAME");
                        final String fsCd = rs.getString("FS_CD");
                        final String finschoolName = rs.getString("FINSCHOOL_NAME");
                        final String sucCoursecode = rs.getString("SUC_COURSECODE");
                        final String sucCoursemark = rs.getString("SUC_COURSEMARK");
                        final String naidakuCoursemark = rs.getString("NAIDAKU_COURSEMARK");
                        final String sub = rs.getString("SUB");
                        final String kasantenAll = rs.getString("KASANTEN_ALL");
                        final String total4 = rs.getString("TOTAL4");
                        final String totalRank4 = rs.getString("TOTAL_RANK4");
                        final String juken = rs.getString("JUKEN");
                        final String interviewValue = rs.getString("INTERVIEW_VALUE");
                        final String scholarship = rs.getString("SCHOLARSHIP");
                        final String scholarshipName = rs.getString("SCHOLARSHIP_NAME");
                        final ReceptDat receptdat = new ReceptDat(receptno, examno, examCoursemark1, examCoursemark2, examCoursemark3, examCoursemark4, name, sex, sexName, fsCd, finschoolName,
                                sucCoursecode, sucCoursemark, naidakuCoursemark, sub, kasantenAll, total4, totalRank4, juken, interviewValue, scholarship, scholarshipName);
                        list.add(receptdat);
                    }

                    if (null != rs.getString("TESTSUBCLASSCD")) {
                        final ReceptDat recept = getReceptDat(list, examno);
                        recept._testSubclassMap.put(rs.getString("TESTSUBCLASSCD"), rs.getString("SCORE"));
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

        public static String sql(final Param param, final String examCourseCd) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.RECEPTNO, ");
            stb.append("     T1.EXAMNO, ");
            stb.append("     T3C.EXAMCOURSE_MARK AS EXAM_COURSEMARK1, ");
            stb.append("     T4C.EXAMCOURSE_MARK AS EXAM_COURSEMARK2, ");
            stb.append("     T5C.EXAMCOURSE_MARK AS EXAM_COURSEMARK3, ");
            stb.append("     T6C.EXAMCOURSE_MARK AS EXAM_COURSEMARK4, ");
            stb.append("     T7.KASANTEN_ALL, ");
            stb.append("     T1.TOTAL4, ");
            stb.append("     BASE.SUC_COURSECODE, ");
            stb.append("     T15.EXAMCOURSE_MARK AS SUC_COURSEMARK, ");
            stb.append("     T14.EXAMCOURSE_MARK AS NAIDAKU_COURSEMARK, ");
            stb.append("     CASE WHEN T13.BEFORE_EXAMCOURSECD IS NOT NULL THEN '" + param._testdivAbbv3 + "' END AS SUB, ");
            stb.append("     CASE WHEN T9.INTERVIEW_VALUE = 'C' THEN 'C' END AS INTERVIEW_VALUE, ");
            stb.append("     T1.TOTAL_RANK4, ");
            stb.append("     CASE WHEN T1.TOTAL4 IS NOT NULL THEN '○' ELSE '×' END AS JUKEN, ");
            stb.append("     BASE.NAME, ");
            stb.append("     BASE.SEX, ");
            stb.append("     NMZ002.ABBV1 AS SEX_NAME, ");
            stb.append("     BASE.FS_CD, ");
            stb.append("     FINS.FINSCHOOL_NAME, ");
            stb.append("     T8.TESTSUBCLASSCD, ");
            stb.append("     T13.SCHOLARSHIP, ");
            stb.append("     NML031.NAME1 AS SCHOLARSHIP_NAME, ");
            stb.append("     T8.SCORE ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_RECEPT_DAT T1 ");
            stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON BASE.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            stb.append("         AND BASE.APPLICANTDIV = T1.APPLICANTDIV ");
            stb.append("         AND BASE.EXAMNO       = T1.EXAMNO ");
            stb.append("     INNER JOIN ENTEXAM_WISHDIV_MST T3 ON T3.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND T3.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("         AND T3.TESTDIV = BASE.TESTDIV ");
            stb.append("         AND T3.DESIREDIV = BASE.DESIREDIV ");
            stb.append("         AND T3.WISHNO = '1' ");
            stb.append("     LEFT JOIN ENTEXAM_COURSE_MST T3C ON T3C.ENTEXAMYEAR = T3.ENTEXAMYEAR ");
            stb.append("         AND T3C.APPLICANTDIV = T3.APPLICANTDIV ");
            stb.append("         AND T3C.TESTDIV = T3.TESTDIV ");
            stb.append("         AND T3C.COURSECD = T3.COURSECD ");
            stb.append("         AND T3C.MAJORCD = T3.MAJORCD ");
            stb.append("         AND T3C.EXAMCOURSECD = T3.EXAMCOURSECD ");
            stb.append("     LEFT JOIN ENTEXAM_WISHDIV_MST T4 ON T4.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND T4.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("         AND T4.TESTDIV = BASE.TESTDIV ");
            stb.append("         AND T4.DESIREDIV = BASE.DESIREDIV ");
            stb.append("         AND T4.WISHNO = '2' ");
            stb.append("     LEFT JOIN ENTEXAM_COURSE_MST T4C ON T4C.ENTEXAMYEAR = T4.ENTEXAMYEAR ");
            stb.append("         AND T4C.APPLICANTDIV = T4.APPLICANTDIV ");
            stb.append("         AND T4C.TESTDIV = T4.TESTDIV ");
            stb.append("         AND T4C.COURSECD = T4.COURSECD ");
            stb.append("         AND T4C.MAJORCD = T4.MAJORCD ");
            stb.append("         AND T4C.EXAMCOURSECD = T4.EXAMCOURSECD ");
            stb.append("     LEFT JOIN ENTEXAM_WISHDIV_MST T5 ON T5.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND T5.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("         AND T5.TESTDIV = BASE.TESTDIV ");
            stb.append("         AND T5.DESIREDIV = BASE.DESIREDIV ");
            stb.append("         AND T5.WISHNO = '3' ");
            stb.append("     LEFT JOIN ENTEXAM_COURSE_MST T5C ON T5C.ENTEXAMYEAR = T5.ENTEXAMYEAR ");
            stb.append("         AND T5C.APPLICANTDIV = T5.APPLICANTDIV ");
            stb.append("         AND T5C.TESTDIV = T5.TESTDIV ");
            stb.append("         AND T5C.COURSECD = T5.COURSECD ");
            stb.append("         AND T5C.MAJORCD = T5.MAJORCD ");
            stb.append("         AND T5C.EXAMCOURSECD = T5.EXAMCOURSECD ");            
            stb.append("     LEFT JOIN ENTEXAM_WISHDIV_MST T6 ON T6.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND T6.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("         AND T6.TESTDIV = BASE.TESTDIV ");
            stb.append("         AND T6.DESIREDIV = BASE.DESIREDIV ");
            stb.append("         AND T6.WISHNO = '4' ");
            stb.append("     LEFT JOIN ENTEXAM_COURSE_MST T6C ON T6C.ENTEXAMYEAR = T6.ENTEXAMYEAR ");
            stb.append("         AND T6C.APPLICANTDIV = T6.APPLICANTDIV ");
            stb.append("         AND T6C.TESTDIV = T6.TESTDIV ");
            stb.append("         AND T6C.COURSECD = T6.COURSECD ");
            stb.append("         AND T6C.MAJORCD = T6.MAJORCD ");
            stb.append("         AND T6C.EXAMCOURSECD = T6.EXAMCOURSECD ");
            stb.append("     LEFT JOIN V_FINSCHOOL_MST FINS ON FINS.YEAR = T1.ENTEXAMYEAR ");
            stb.append("        AND FINS.FINSCHOOLCD = BASE.FS_CD ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T2 ON T2.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND T2.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("         AND T2.EXAMNO       = BASE.EXAMNO ");
            stb.append("         AND T2.SEQ          = '002' ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANT_BEFORE_DAT T13 ON T13.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND T13.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("         AND T13.TESTDIV = BASE.TESTDIV ");
            stb.append("         AND T13.BEFORE_PAGE = T2.REMARK1 ");
            stb.append("         AND T13.BEFORE_SEQ = T2.REMARK2 ");
            stb.append("     LEFT JOIN ENTEXAM_COURSE_MST T14 ON T14.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND T14.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("         AND T14.TESTDIV = BASE.TESTDIV ");
            stb.append("         AND T14.COURSECD = T13.BEFORE_COURSECD ");
            stb.append("         AND T14.MAJORCD = T13.BEFORE_MAJORCD ");
            stb.append("         AND T14.EXAMCOURSECD = T13.BEFORE_EXAMCOURSECD ");
            stb.append("     LEFT JOIN ENTEXAM_COURSE_MST T15 ON T15.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND T15.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("         AND T15.TESTDIV = BASE.TESTDIV ");
            stb.append("         AND T15.COURSECD = BASE.SUC_COURSECD ");
            stb.append("         AND T15.MAJORCD = BASE.SUC_MAJORCD ");
            stb.append("         AND T15.EXAMCOURSECD = BASE.SUC_COURSECODE ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT T7 ON T7.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND T7.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("         AND T7.EXAMNO       = BASE.EXAMNO ");
            stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT T8 ON T8.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            stb.append("         AND T8.APPLICANTDIV = T1.APPLICANTDIV ");
            stb.append("         AND T8.TESTDIV = T1.TESTDIV ");
            stb.append("         AND T8.EXAM_TYPE = T1.EXAM_TYPE ");
            stb.append("         AND T8.RECEPTNO = T1.RECEPTNO ");
            stb.append("     LEFT JOIN ENTEXAM_INTERVIEW_DAT T9 ON T9.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            stb.append("         AND T9.APPLICANTDIV = T1.APPLICANTDIV ");
            stb.append("         AND T9.TESTDIV = T1.TESTDIV ");
            stb.append("         AND T9.EXAMNO = T1.EXAMNO ");
            stb.append("     LEFT JOIN NAME_MST NMZ002 ON NMZ002.NAMECD1 = 'Z002' ");
            stb.append("         AND NMZ002.NAMECD2 = BASE.SEX ");
            stb.append("     LEFT JOIN NAME_MST NML031 ON NML031.NAMECD1 = 'L031' ");
            stb.append("         AND NML031.NAMECD2 = T13.SCHOLARSHIP ");
            stb.append(" WHERE ");
            stb.append("     T1.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("     AND T1.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("     AND T1.TESTDIV = '" + param._testdiv + "' ");
            stb.append("     AND T1.EXAM_TYPE = '1' ");
            stb.append("     AND T3.EXAMCOURSECD = '" + examCourseCd + "' ");
            stb.append(" ORDER BY ");
            if ("2".equals(param._output)) {
                stb.append("     T1.TOTAL_RANK4, ");
            }
            stb.append("     T1.EXAMNO ");
            return stb.toString();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 71531 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _date;
        final String _output; // 出力順 1:受験番号 2:成績順

        final String _applicantdivName;
        final String _testdivName;
        final String _testdivAbbv3;
        final String _dateStr;
        final List _examCourseList;
        final List _subclassNameMstMapList;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _date = request.getParameter("CTRL_DATE");
            _output = request.getParameter("OUTPUT");
            _dateStr = getDateStr(_date);
            _examCourseList = getExamCourseList(db2);

            _applicantdivName = getNameMst(db2, "NAME1", "L003", _applicantdiv);
            _testdivName = getNameMst(db2, "NAME1", "L004", _testdiv);
            _testdivAbbv3 = getNameMst(db2, "ABBV3", "L004", _testdiv);
            final String subclassWhere = "1".equals(_testdiv) ? " NAMESPARE2 = '1' " : " NAMESPARE3 = '1' ";
            _subclassNameMstMapList = getSubclassNameMstList(db2, subclassWhere);
        }

        private String getDateStr(final String date) {
            final Calendar cal = Calendar.getInstance();
            final DecimalFormat df = new DecimalFormat("00");
            final int hour = cal.get(Calendar.HOUR_OF_DAY);
            final int minute = cal.get(Calendar.MINUTE);
            return KNJ_EditDate.h_format_JP(date) + "　" + df.format(hour) + "時" + df.format(minute) + "分現在";
        }

        private List getExamCourseList(final DB2UDB db2) {
            final List rtn = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                StringBuffer sql = new StringBuffer();
                sql.append(" SELECT EXAMCOURSECD, EXAMCOURSE_MARK ");
                sql.append(" FROM ENTEXAM_COURSE_MST ");
                sql.append(" WHERE ENTEXAMYEAR = '" + _entexamyear + "' ");
                sql.append("   AND APPLICANTDIV = '" + _applicantdiv + "' ");
                sql.append("   AND TESTDIV = '" + _testdiv + "' ");
                sql.append(" ORDER BY ");
                sql.append("   EXAMCOURSECD ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Map m = new HashMap();
                    m.put("EXAMCOURSECD", rs.getString("EXAMCOURSECD"));
                    m.put("EXAMCOURSE_MARK", rs.getString("EXAMCOURSE_MARK"));
                    rtn.add(m);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
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

        private List getSubclassNameMstList(final DB2UDB db2, final String where) {
            final List rtn = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT NAMECD2, NAME1 FROM V_NAME_MST ");
                sql.append(" WHERE YEAR = '" + _entexamyear + "' AND NAMECD1 = 'L009' ");
                if (!StringUtils.isBlank(where)) {
                    sql.append(" AND " + where);
                }
                sql.append(" ORDER BY NAMECD2 ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Map m = new HashMap();
                    m.put("NAMECD2", rs.getString("NAMECD2"));
                    m.put("NAME1", rs.getString("NAME1"));
                    rtn.add(m);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }
    }
}

// eof

