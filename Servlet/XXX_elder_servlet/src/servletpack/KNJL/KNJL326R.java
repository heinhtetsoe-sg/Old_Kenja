/*
 * $Id: fbb6347b2ebd791ae4238a5b1419c06555948d31 $
 *
 * 作成日: 2013/10/10
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.io.File;
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
 *                  ＜ＫＮＪＬ３２６Ｒ＞  各種帳票（学校宛）
 **/
public class KNJL326R {

    private static final Log log = LogFactory.getLog(KNJL326R.class);

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

        log.debug(" form = " + _param._form);
        if ("1".equals(_param._form)) {
            log.debug(" print 1");
            print1(db2, svf);
		} else if ("2".equals(_param._form)) {
            log.debug(" print 2");
            print2(db2, svf);
		} else if ("3".equals(_param._form)) {
            log.debug(" print 3");
		    print3(db2, svf);
		}
    }

    public void print2(final DB2UDB db2, final Vrw32alp svf) {
        final String form = "KNJL326R_2.frm";

        final List fsList = Finschool.load(db2, _param);
        for (final Iterator it = fsList.iterator(); it.hasNext();) {
            final Finschool finschool = (Finschool) it.next();

            final List pageList = getPageList(finschool._applicantList, 20);
            for (int i = 0; i < pageList.size(); i++) {
                final List applicantList = (List) pageList.get(i);

                svf.VrSetForm(form, 1);
                svf.VrsOut("JH_CODE1", finschool._finschoolcd); // 中学校コード
                svf.VrsOut("JH_NAME", StringUtils.isBlank(finschool._finschoolName) ? null : finschool._finschoolName + "中学校"); // 中学校名
                svf.VrsOut("TITLE", _param._entexamyear + "（" + KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "）年度　" + _param._testdivName1 + "合否結果通知書"); // タイトル
                svf.VrsOut("DATE", _param._noticedateStr); // 印刷日

                for (int j = 0; j < applicantList.size(); j++) {
                    final Applicant appl = (Applicant) applicantList.get(j);
                    final int line = j + 1;
                    svf.VrsOutn("JH_CODE2", line, finschool._finschoolcd); // 中学校コード
                    svf.VrsOutn("EXAM_NO", line, appl._examno); // 受験番号
                    svf.VrsOutn("NAME", line, appl._name); // 名前
                    svf.VrsOutn("SEX1", line, appl._sexName); // 性別
                    svf.VrsOutn("JUDGE", line, appl._judgementName); // 判定
                    svf.VrsOutn("SUBJECT", line, appl._sucMajorname); // 学科
                    svf.VrsOutn("PASS_COURSE", line, appl._sucCourseName); // 合格コース
                    svf.VrsOutn("FAILE1", line, appl._examCoursename1); // 不合格コース
                    svf.VrsOutn("FAILE2", line, appl._examCoursename2); // 不合格コース
                    svf.VrsOutn("FAILE3", line, appl._examCoursename3); // 不合格コース
                    svf.VrsOutn("FAILE4", line, appl._examCoursename4); // 不合格コース
                    if (null != appl._judgeKind) {
                        svf.VrsOutn("REMARK", line, "奨学生（" + StringUtils.defaultString(appl._judgeKindName) + "）"); // 備考
                    }
                }
                svf.VrsOut("SCHOOL_NAME", _param._schoolName); // 学校名
                svf.VrEndPage();
                _hasData = true;
            }
        }
    }

    public void print3(final DB2UDB db2, final Vrw32alp svf) {
        final String form = "KNJL326R_3.frm";

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(getFinschoolSql(_param));
            rs = ps.executeQuery();
            while (rs.next()) {
                svf.VrSetForm(form, 1);
                svf.VrsOut("FIELD1", StringUtils.replace(rs.getString("FINSCHOOL_ZIPCD"), "-", "")); // 郵便番号
                svf.VrsOut("ADDR1", rs.getString("FINSCHOOL_ADDR1")); // 住所
                svf.VrsOut("ADDR2", rs.getString("FINSCHOOL_ADDR2")); // 住所
                svf.VrsOut("SCHOOL_NAME", StringUtils.isBlank(rs.getString("FINSCHOOL_NAME")) ? null : rs.getString("FINSCHOOL_NAME") + "中学校"); // 学校名
                svf.VrsOut("PRINCIPAL_NAME", "学校長　様"); // 校長名
                svf.VrsOut("JH_CODE1", rs.getString("FINSCHOOLCD")); // 中学校コード
                svf.VrEndPage();
                _hasData = true;
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    public void print1(final DB2UDB db2, final Vrw32alp svf) {
        final String form = "KNJL326R_1.frm";

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String finschoolSql = getFinschoolSql(_param);
            log.debug(" sql = " + finschoolSql);
            ps = db2.prepareStatement(finschoolSql);
            rs = ps.executeQuery();
            while (rs.next()) {
                svf.VrSetForm(form, 1);
                svf.VrsOut("FIELD2", StringUtils.isBlank(rs.getString("FINSCHOOL_NAME")) ? null : rs.getString("FINSCHOOL_NAME") + "中学校"); // 宛先中学校
                svf.VrsOut("FIELD3", "学校長　様"); // 宛先校長名
                svf.VrsOut("SCHOOL_NAME", _param._schoolName); // 学校名
                svf.VrsOut("PRINCIPAL_NAME", _param._jobName + "　　" + _param._principalName); // 校長名
                svf.VrsOut("TITLE", _param._testdivAbbv1); // タイトル
                svf.VrsOut("DATE", _param._noticedateStr); // 日付
                if (null != _param._imageFile) {
                    svf.VrsOut("STAMP", _param._imageFile.toString());
                }
                svf.VrEndPage();
                _hasData = true;
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private String getFinschoolSql(final Param param) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT DISTINCT ");
        stb.append("     T2.FINSCHOOLCD,  ");
        stb.append("     T2.FINSCHOOL_NAME, ");
        stb.append("     T2.FINSCHOOL_ZIPCD,  ");
        stb.append("     T2.FINSCHOOL_ADDR1,  ");
        stb.append("     T2.FINSCHOOL_ADDR2,  ");
        stb.append("     T2.PRINCNAME ");
        stb.append(" FROM ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append(" LEFT JOIN V_FINSCHOOL_MST T2 ON T2.YEAR = T1.ENTEXAMYEAR ");
        stb.append("     AND T2.FINSCHOOLCD = T1.FS_CD ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + param._entexamyear + "' ");
        stb.append("     AND T1.APPLICANTDIV = '" + param._applicantdiv + "' ");
        stb.append("     AND T1.TESTDIV = '" + param._testdiv + "' ");
        if ("2".equals(param._output)) {
            stb.append("     AND T1.FS_CD = '" + param._schoolcd + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T2.FINSCHOOLCD ");
        return stb.toString();
    }



    private static class Finschool {
        final String _finschoolcd;
        final String _finschoolName;
        final List _applicantList;
        Finschool(
            final String finschoolcd,
            final String finschoolName
        ) {
            _finschoolcd = finschoolcd;
            _finschoolName = finschoolName;
            _applicantList = new ArrayList();
        }

        private static Finschool getFinschool(final List list, final String finschoolcd) {
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final Finschool finschool = (Finschool) it.next();
                if (finschool._finschoolcd.equals(finschoolcd)) {
                    return finschool;
                }
            }
            return null;
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
                    final String finschoolcd = rs.getString("FINSCHOOLCD");
                    if (null == getFinschool(list, finschoolcd)) {
                        final String finschoolName = rs.getString("FINSCHOOL_NAME");
                        list.add(new Finschool(finschoolcd, finschoolName));
                    }
                    final Finschool fs = getFinschool(list, finschoolcd);
                    final String examno = rs.getString("EXAMNO");
                    final String name = rs.getString("NAME");
                    final String sex = rs.getString("SEX");
                    final String sexName = rs.getString("SEX_NAME");
                    final String judgement = rs.getString("JUDGEMENT");
                    final String judgementName = rs.getString("JUDGEMENT_NAME");
                    final String sucMajorname = rs.getString("SUC_MAJORNAME");
                    final String sucCourseName = rs.getString("SUC_COURSE_NAME");
                    final String examCoursename1 = rs.getString("EXAM_COURSENAME1");
                    final String examCoursename2 = rs.getString("EXAM_COURSENAME2");
                    final String examCoursename3 = rs.getString("EXAM_COURSENAME3");
                    final String examCoursename4 = rs.getString("EXAM_COURSENAME4");
                    final String judgeKind = rs.getString("JUDGE_KIND");
                    final String judgeKindName = rs.getString("JUDGE_KIND_NAME");
                    final Applicant applicant = new Applicant(examno, name, sex, sexName, judgement, judgementName, sucMajorname, sucCourseName, examCoursename1, examCoursename2, examCoursename3, examCoursename4, judgeKind, judgeKindName);
                    fs._applicantList.add(applicant);
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
            stb.append(" SELECT  ");
            stb.append("     T2.FINSCHOOLCD,  ");
            stb.append("     T2.FINSCHOOL_NAME, ");
            stb.append("     BASE.EXAMNO, ");
            stb.append("     BASE.NAME, ");
            stb.append("     BASE.SEX, ");
            stb.append("     NMZ002.NAME2 AS SEX_NAME, ");
            stb.append("     BASE.JUDGEMENT, ");
            stb.append("     CASE WHEN BASE.JUDGEMENT IS NOT NULL THEN ");
            stb.append("         CASE WHEN BASE.JUDGEMENT = '3' THEN '欠席' ");
            stb.append("              WHEN NML013.NAMESPARE1 = '1' THEN '合格' ELSE '不合格' END ");
            stb.append("     END AS JUDGEMENT_NAME, ");
            stb.append("     CASE WHEN NML013.NAMESPARE1 = '1' THEN T6.MAJORNAME END AS SUC_MAJORNAME, ");
            stb.append("     CASE WHEN NML013.NAMESPARE1 = '1' THEN T7.EXAMCOURSE_NAME END AS SUC_COURSE_NAME, ");
            stb.append("     CASE WHEN BASE.JUDGEMENT IS NOT NULL AND VALUE(NML013.NAMESPARE1, '') <> '1' THEN T3C.EXAMCOURSE_NAME END AS EXAM_COURSENAME1, ");
            stb.append("     CASE WHEN BASE.JUDGEMENT IS NOT NULL AND VALUE(NML013.NAMESPARE1, '') <> '1' THEN T4C.EXAMCOURSE_NAME END AS EXAM_COURSENAME2, ");
            stb.append("     CASE WHEN BASE.JUDGEMENT IS NOT NULL AND VALUE(NML013.NAMESPARE1, '') <> '1' THEN T5C.EXAMCOURSE_NAME END AS EXAM_COURSENAME3, ");
            stb.append("     CASE WHEN BASE.JUDGEMENT IS NOT NULL AND VALUE(NML013.NAMESPARE1, '') <> '1' THEN C4.EXAMCOURSE_NAME END AS EXAM_COURSENAME4, ");
            stb.append("     BASE.JUDGE_KIND, ");
            stb.append("     NML031.NAME1 AS JUDGE_KIND_NAME ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
            stb.append("     LEFT JOIN V_FINSCHOOL_MST T2 ON T2.YEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND T2.FINSCHOOLCD = BASE.FS_CD ");
            stb.append("     LEFT JOIN ENTEXAM_WISHDIV_MST T3 ON T3.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
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
            stb.append("     LEFT JOIN ENTEXAM_WISHDIV_MST W4 ON W4.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND W4.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("         AND W4.TESTDIV = BASE.TESTDIV ");
            stb.append("         AND W4.DESIREDIV = BASE.DESIREDIV ");
            stb.append("         AND W4.WISHNO = '4' ");
            stb.append("     LEFT JOIN ENTEXAM_COURSE_MST C4 ON C4.ENTEXAMYEAR = W4.ENTEXAMYEAR ");
            stb.append("         AND C4.APPLICANTDIV = W4.APPLICANTDIV ");
            stb.append("         AND C4.TESTDIV = W4.TESTDIV ");
            stb.append("         AND C4.COURSECD = W4.COURSECD ");
            stb.append("         AND C4.MAJORCD = W4.MAJORCD ");
            stb.append("         AND C4.EXAMCOURSECD = W4.EXAMCOURSECD ");
            stb.append("     LEFT JOIN MAJOR_MST T6 ON T6.COURSECD = BASE.SUC_COURSECD ");
            stb.append("         AND T6.MAJORCD = BASE.SUC_MAJORCD ");
            stb.append("     LEFT JOIN ENTEXAM_COURSE_MST T7 ON T7.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND T7.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("         AND T7.TESTDIV = BASE.TESTDIV ");
            stb.append("         AND T7.COURSECD = BASE.SUC_COURSECD ");
            stb.append("         AND T7.MAJORCD = BASE.SUC_MAJORCD ");
            stb.append("         AND T7.EXAMCOURSECD = BASE.SUC_COURSECODE ");
            stb.append("     LEFT JOIN NAME_MST NMZ002 ON NMZ002.NAMECD1 = 'Z002' ");
            stb.append("         AND NMZ002.NAMECD2 = BASE.SEX ");
            stb.append("     LEFT JOIN NAME_MST NML013 ON NML013.NAMECD1 = 'L013' ");
            stb.append("         AND NML013.NAMECD2 = BASE.JUDGEMENT ");
            stb.append("     LEFT JOIN NAME_MST NML031 ON NML031.NAMECD1 = 'L031' ");
            stb.append("         AND NML031.NAMECD2 = BASE.JUDGE_KIND ");
            stb.append(" WHERE ");
            stb.append("     BASE.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("     AND BASE.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("     AND BASE.TESTDIV = '" + param._testdiv + "' ");
            if ("2".equals(param._output)) {
                stb.append("     AND BASE.FS_CD = '" + param._schoolcd + "' ");
            }
            stb.append("     AND BASE.JUDGEMENT NOT IN ('4') ");
            stb.append(" ORDER BY ");
            stb.append("     BASE.FS_CD, ");
            stb.append("     BASE.EXAMNO ");
            return stb.toString();
        }
    }

    private static class Applicant {
        final String _examno;
        final String _name;
        final String _sex;
        final String _sexName;
        final String _judgement;
        final String _judgementName;
        final String _sucMajorname;
        final String _sucCourseName;
        final String _examCoursename1;
        final String _examCoursename2;
        final String _examCoursename3;
        final String _examCoursename4;
        final String _judgeKind;
        final String _judgeKindName;

        Applicant(
            final String examno,
            final String name,
            final String sex,
            final String sexName,
            final String judgement,
            final String judgementName,
            final String sucMajorname,
            final String sucCourseName,
            final String examCoursename1,
            final String examCoursename2,
            final String examCoursename3,
            final String examCoursename4,
            final String judgeKind,
            final String judgeKindName
        ) {
            _examno = examno;
            _name = name;
            _sex = sex;
            _sexName = sexName;
            _judgement = judgement;
            _judgementName = judgementName;
            _sucMajorname = sucMajorname;
            _sucCourseName = sucCourseName;
            _examCoursename1 = examCoursename1;
            _examCoursename2 = examCoursename2;
            _examCoursename3 = examCoursename3;
            _examCoursename4 = examCoursename4;
            _judgeKind = judgeKind;
            _judgeKindName = judgeKindName;
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

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 72070 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _noticedate;
        final String _form; // 出力順 1:お礼状 2:合否決通知書 3:封筒
        final String _output; // 出力順 1:全校 2:指定
        final String _schoolcd; // 指定学校
        final String _documentroot;
        final File _imageFile;

        final String _applicantdivName;
        final String _testdivName1;
        final String _testdivAbbv1;
        final String _noticedateStr;
        final String _schoolName;
        final String _jobName;
        final String _principalName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _noticedate = request.getParameter("NOTICEDATE").replace('/', '-');
            _noticedateStr = getDateStr(db2, _noticedate);
            _form = request.getParameter("FORM");
            _output = request.getParameter("OUTPUT");
            _schoolcd = request.getParameter("SCHOOLCD");
            _documentroot = request.getParameter("DOCUMENTROOT");
            _imageFile = getImageFile(db2);

            _applicantdivName = getNameMst(db2, "NAME1", "L003", _applicantdiv);
            _testdivName1 = getNameMst(db2, "NAME1", "L004", _testdiv);
            _testdivAbbv1 = getNameMst(db2, "ABBV1", "L004", _testdiv);

            _schoolName = getCertifSchoolDat(db2, "SCHOOL_NAME", "");
            _jobName = getCertifSchoolDat(db2, "JOB_NAME", "　　");
            _principalName = getCertifSchoolDat(db2, "PRINCIPAL_NAME", "");
        }

        private String getDateStr(final DB2UDB db2, final String date) {
            if (null == date) {
                return null;
            }
            return KNJ_EditDate.h_format_JP(db2, date);
        }

        private String getExamCourseName(final DB2UDB db2, final String field, final String examcoursecd) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                StringBuffer sql = new StringBuffer();
                sql.append(" SELECT " + field + " ");
                sql.append(" FROM ENTEXAM_COURSE_MST ");
                sql.append(" WHERE ENTEXAMYEAR = '" + _entexamyear + "' ");
                sql.append("   AND APPLICANTDIV = '" + _applicantdiv + "' ");
                sql.append("   AND TESTDIV = '" + _testdiv + "' ");
                sql.append("   AND EXAMCOURSECD = '" + examcoursecd + "' ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(field);
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

        private File getImageFile(final DB2UDB db2) {
            if (null == _documentroot) {
                return null;
            }
            String imagepath = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT IMAGEPATH FROM CONTROL_MST WHERE CTRL_NO = '01' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    imagepath = rs.getString("IMAGEPATH");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            if (null == imagepath) {
                return null;
            }
            final File file = new File(_documentroot + "/" + imagepath + "/SCHOOLSTAMP.bmp");
            log.fatal(" file = " + file.getAbsolutePath() + ", exists? = " + file.exists());
            if (!file.exists()) {
                return null;
            }
            return file;
        }

        private String getCertifSchoolDat(final DB2UDB db2, final String field, final String blank) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                StringBuffer sql = new StringBuffer();
                sql.append(" SELECT " + field + " ");
                sql.append(" FROM CERTIF_SCHOOL_DAT ");
                sql.append(" WHERE YEAR = '" + _entexamyear + "' ");
                sql.append("   AND CERTIF_KINDCD = '106' ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            if (null == rtn) {
                rtn = blank;
            } else {
                int start = 0;
                for (int i = 0; i < rtn.length(); i++) {
                    if (rtn.charAt(i) != ' ' && rtn.charAt(i) != '　') {
                        break;
                    }
                    start = i + 1;
                }
                rtn = rtn.substring(start);
            }
            return rtn;
        }
    }
}

// eof

