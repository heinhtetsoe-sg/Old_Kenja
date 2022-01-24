/*
 * $Id: c618b9258005eadc20ed401a09c06e4334ec28ab $
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

import jp.co.alp.kenja.common.dao.SQLUtils;
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
 *                  ＜ＫＮＪＬ３０９Ｒ＞  点呼名簿
 **/
public class KNJL309R {

    private static final Log log = LogFactory.getLog(KNJL309R.class);

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
        final String form = "1".equals(_param._applicantdiv) ? "KNJL309R.frm" : "KNJL309R_J.frm" ;
        final int maxLine = 50;
        String[] nendo = KNJ_EditDate.tate_format4(db2,_param._entexamyear + "-04-01");

        final List hallList = ExamHall.load(db2, _param);
        int totalPage = 0;
        for (final Iterator it = hallList.iterator(); it.hasNext();) {
            final ExamHall hall = (ExamHall) it.next();
            final List pageList = getPageList(hall._applicantList, maxLine);
            totalPage += pageList.size();
        }
        int page = 0;
        for (final Iterator it = hallList.iterator(); it.hasNext();) {
            final ExamHall hall = (ExamHall) it.next();

            final List pageList = getPageList(hall._applicantList, maxLine);
            for (int pi = 0; pi < pageList.size(); pi++) {
                final List applList = (List) pageList.get(pi);

                svf.VrSetForm(form, 1);
                svf.VrsOut("PAGE1", String.valueOf(page + pi + 1)); // ページ
                svf.VrsOut("PAGE2", String.valueOf(totalPage)); // ページ

                svf.VrsOut("TITLE", nendo[0] + nendo[1] + "年度　" + _param._testdivName + "点呼表"); // タイトル
                svf.VrsOut("DATE", _param._dateStr); // 印刷日
                svf.VrsOut("PLACE", hall._examhallcd + " " + hall._examhallName); // ページ

                for (int j = 0; j < applList.size(); j++) {
                    final Applicant appl = (Applicant) applList.get(j);
                    final int line = j + 1;
                    svf.VrsOutn("NO", line, String.valueOf(pi * maxLine + line)); // 番号
                    // svf.VrsOutn("ABSENCE", line, null); // 出欠
                    svf.VrsOutn("EXAM_NO", line, appl._examno); // 受験番号
                    svf.VrsOutn("COURSE1", line, appl._examCoursemark1); // 受験コース
                    svf.VrsOutn("NAME", line, appl._name); // 名前
                    svf.VrsOutn("KANA", line, appl._nameKana); // フリガナ
                    svf.VrsOutn("SEX", line, appl._sexName); // 性別
                    svf.VrsOutn("JH_NAME", line, appl._finschoolName); // 中学校名
                    svf.VrsOutn("COURSE2", line, appl._befCoursemark); // 専願コース
                    svf.VrsOutn("COURSE3", line, appl._sub); // サブ
                    svf.VrsOutn("SCORE", line, appl._totalAll); // 135点満点
                    svf.VrsOutn("ABSENT1_4", line, appl._absenceDays3); // 欠席日数
                    // svf.VrsOutn("INTERVIEW", line, null); // 面接
                    // svf.VrsOutn("REMARK", line, null); // 備考
                }
                svf.VrEndPage();
                _hasData = true;
            }
            page += pageList.size();
        }
    }

    private static class ExamHall {
        final String _examType;
        final String _examhallcd;
        final String _examhallName;
        final List _applicantList;

        ExamHall(
            final String examType,
            final String examhallcd,
            final String examhallName
        ) {
            _examType = examType;
            _examhallcd = examhallcd;
            _examhallName = examhallName;
            _applicantList = new ArrayList();
        }

        private static ExamHall getExamHall(final List list, final String examhallcd) {
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final ExamHall hall = (ExamHall) it.next();
                if (hall._examhallcd.equals(examhallcd)) {
                    return hall;
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
                    final String examhallcd = rs.getString("EXAMHALLCD");
                    if (null == getExamHall(list, examhallcd)) {
                        final String examType = rs.getString("EXAM_TYPE");
                        final String examhallName = rs.getString("EXAMHALL_NAME");
                        final ExamHall hall = new ExamHall(examType, examhallcd, examhallName);
                        list.add(hall);
                    }
                    final ExamHall hall = getExamHall(list, examhallcd);
                    final String receptno = rs.getString("RECEPTNO");
                    final String examno = rs.getString("EXAMNO");
                    final String examCoursemark1 = rs.getString("EXAM_COURSEMARK1");
                    final String name = rs.getString("NAME");
                    final String nameKana = rs.getString("NAME_KANA");
                    final String sex = rs.getString("SEX");
                    final String sexName = rs.getString("SEX_NAME");
                    final String fsCd = rs.getString("FS_CD");
                    final String befCoursemark = rs.getString("BEF_COURSEMARK");
                    final String sub = rs.getString("SUB");
                    final String finschoolName = rs.getString("FINSCHOOL_NAME");
                    final String totalAll = rs.getString("TOTAL_ALL");
                    final String absenceDays3 = rs.getString("ABSENCE_DAYS3");
                    final Applicant appl = new Applicant(receptno, examno, examCoursemark1, name, nameKana, sex, sexName, fsCd, befCoursemark, sub, finschoolName, totalAll, absenceDays3);
                    hall._applicantList.add(appl);
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
            stb.append("     T1.EXAM_TYPE, ");
            stb.append("     T1.EXAMHALLCD, ");
            stb.append("     T1.EXAMHALL_NAME, ");
            stb.append("     T2.RECEPTNO, ");
            stb.append("     T2.EXAMNO, ");
            stb.append("     BASE.NAME, ");
            stb.append("     BASE.NAME_KANA, ");
            stb.append("     BASE.SEX, ");
            stb.append("     NMZ002.ABBV1 AS SEX_NAME, ");
            stb.append("     BASE.FS_CD, ");
            stb.append("     FIN.FINSCHOOL_NAME, ");
            if("2".equals(param._applicantdiv)) {
                stb.append("     '' AS EXAM_COURSEMARK1, ");
                stb.append("     NML006.NAME2 || NML064.NAME1 AS BEF_COURSEMARK, ");
                stb.append("     '' AS SUB, ");
                stb.append("     '' AS TOTAL_ALL, ");
                stb.append("     VALUE(INTEGER(BD1_006.REMARK5),0) + VALUE(INTEGER(BD1_006.REMARK6),0) AS ABSENCE_DAYS3 ");
            }else {
            	stb.append("     T3C.EXAMCOURSE_MARK AS EXAM_COURSEMARK1, ");
                stb.append("     T14.EXAMCOURSE_MARK AS BEF_COURSEMARK, ");
                stb.append("     CASE WHEN T14.EXAMCOURSECD IS NOT NULL THEN '" + param._testdivAbbv3 + "' END AS SUB, ");
                stb.append("     T4.TOTAL_ALL AS TOTAL_ALL, ");
                stb.append("     T4.ABSENCE_DAYS3 AS ABSENCE_DAYS3 ");
            }
            stb.append(" FROM ");
            stb.append("     ENTEXAM_HALL_YDAT T1 ");
            stb.append("     INNER JOIN ENTEXAM_RECEPT_DAT T2 ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            stb.append("         AND T2.APPLICANTDIV = T1.APPLICANTDIV ");
            stb.append("         AND T2.TESTDIV = T1.TESTDIV ");
            stb.append("         AND T2.RECEPTNO BETWEEN T1.S_RECEPTNO AND T1.E_RECEPTNO ");
            stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON BASE.ENTEXAMYEAR = T2.ENTEXAMYEAR ");
            stb.append("         AND BASE.APPLICANTDIV = T2.APPLICANTDIV ");
            stb.append("         AND BASE.EXAMNO       = T2.EXAMNO ");
            stb.append("     LEFT JOIN V_FINSCHOOL_MST FIN ON FIN.YEAR = T1.ENTEXAMYEAR ");
            stb.append("         AND FIN.FINSCHOOLCD = BASE.FS_CD ");
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
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DETAIL ON DETAIL.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND DETAIL.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("         AND DETAIL.EXAMNO       = BASE.EXAMNO ");
            stb.append("         AND DETAIL.SEQ          = '002' ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANT_BEFORE_DAT T13 ON T13.ENTEXAMYEAR = BASE.ENTEXAMYEAR  ");
            stb.append("         AND T13.APPLICANTDIV = BASE.APPLICANTDIV  ");
            stb.append("         AND T13.TESTDIV = BASE.TESTDIV  ");
            stb.append("         AND T13.BEFORE_PAGE = DETAIL.REMARK1  ");
            stb.append("         AND T13.BEFORE_SEQ = DETAIL.REMARK2  ");
            stb.append("     LEFT JOIN ENTEXAM_COURSE_MST T14 ON T14.ENTEXAMYEAR = BASE.ENTEXAMYEAR  ");
            stb.append("         AND T14.APPLICANTDIV = BASE.APPLICANTDIV  ");
            stb.append("         AND T14.TESTDIV = BASE.TESTDIV  ");
            stb.append("         AND T14.COURSECD = T13.BEFORE_COURSECD  ");
            stb.append("         AND T14.MAJORCD = T13.BEFORE_MAJORCD  ");
            stb.append("         AND T14.EXAMCOURSECD = T13.BEFORE_EXAMCOURSECD  ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT T4 ON T4.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND T4.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("         AND T4.EXAMNO       = BASE.EXAMNO ");
            stb.append("     LEFT JOIN NAME_MST NMZ002 ON NMZ002.NAMECD1 = 'Z002' ");
            stb.append("         AND NMZ002.NAMECD2 = BASE.SEX ");
            if("2".equals(param._applicantdiv)) {
                stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD1_006 ON BD1_006.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
                stb.append("         AND BD1_006.APPLICANTDIV = BASE.APPLICANTDIV ");
                stb.append("         AND BD1_006.EXAMNO       = BASE.EXAMNO ");
                stb.append("         AND BD1_006.SEQ          = '006' ");
                stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BD2_013 ON BD2_013.ENTEXAMYEAR = BASE.ENTEXAMYEAR  ");
                stb.append("         AND BD2_013.APPLICANTDIV = BASE.APPLICANTDIV  ");
                stb.append("         AND BD2_013.EXAMNO       = BASE.EXAMNO  ");
                stb.append("         AND BD2_013.SEQ          = '013'  ");
                stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BD2_014 ON BD2_014.ENTEXAMYEAR = BASE.ENTEXAMYEAR  ");
                stb.append("         AND BD2_014.APPLICANTDIV = BASE.APPLICANTDIV  ");
                stb.append("         AND BD2_014.EXAMNO       = BASE.EXAMNO  ");
                stb.append("         AND BD2_014.SEQ          = '014'  ");
                if("1".equals(param._testdiv)) {
                    stb.append("     LEFT JOIN NAME_MST NML006  ");
                    stb.append("          ON NML006.NAMECD2 = BD2_013.REMARK1 ");
                    stb.append("         AND NML006.NAMECD1 = 'L006'  ");
                    stb.append("     LEFT JOIN NAME_MST NML064  ");
                    stb.append("          ON NML064.NAMECD2 = BD2_014.REMARK1 ");
                    stb.append("         AND NML064.NAMECD1 = 'L064'  ");
                }else{
                    stb.append("     LEFT JOIN NAME_MST NML006  ");
                    stb.append("          ON NML006.NAMECD2 = BD2_013.REMARK2 ");
                    stb.append("         AND NML006.NAMECD1 = 'L006'  ");
                    stb.append("     LEFT JOIN NAME_MST NML064  ");
                    stb.append("          ON NML064.NAMECD2 = BD2_014.REMARK2 ");
                    stb.append("         AND NML064.NAMECD1 = 'L064'  ");
                }
            }
            stb.append(" WHERE ");
            stb.append("     T1.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("     AND T1.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("     AND T1.TESTDIV = '" + param._testdiv + "' ");
            stb.append("     AND T1.EXAMHALLCD IN " + SQLUtils.whereIn(true, param._categoryName) + " ");
            stb.append(" ORDER BY ");
            stb.append("     T1.EXAMHALLCD, ");
            stb.append("     BASE.EXAMNO ");
            return stb.toString();
        }
    }

    private static class Applicant {
        final String _receptno;
        final String _examno;
        final String _examCoursemark1;
        final String _name;
        final String _nameKana;
        final String _sex;
        final String _sexName;
        final String _fsCd;
        final String _befCoursemark;
        final String _sub;
        final String _finschoolName;
        final String _totalAll;
        final String _absenceDays3;

        Applicant(
            final String receptno,
            final String examno,
            final String examCoursemark1,
            final String name,
            final String nameKana,
            final String sex,
            final String sexName,
            final String fsCd,
            final String befCoursemark,
            final String sub,
            final String finschoolName,
            final String totalAll,
            final String absenceDays3
        ) {
            _receptno = receptno;
            _examno = examno;
            _examCoursemark1 = examCoursemark1;
            _name = name;
            _nameKana = nameKana;
            _sex = sex;
            _sexName = sexName;
            _fsCd = fsCd;
            _befCoursemark = befCoursemark;
            _sub = sub;
            _finschoolName = finschoolName;
            _totalAll = totalAll;
            _absenceDays3 = absenceDays3;
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
        log.fatal("$Revision: 72367 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _date;
        final String[] _categoryName;

        final String _applicantdivName;
        final String _testdivName;
        final String _testdivAbbv3;
        final String _dateStr;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _date = request.getParameter("CTRL_DATE");
            _dateStr = getDateStr(db2, _date);
            _categoryName = request.getParameterValues("category_name");
            for (int i = 0; i < _categoryName.length; i++) {
                _categoryName[i] = StringUtils.split(_categoryName[i], "-")[0];
            }
            _applicantdivName = getNameMst(db2, "NAME1", "L003", _applicantdiv);
            if("1".equals(_applicantdiv)) {
                _testdivName = getNameMst(db2, "NAME1", "L004", _testdiv);
            }else {
                _testdivName = getNameMst(db2, "NAME1", "L024", _testdiv);
            }
            _testdivAbbv3 = getNameMst(db2, "ABBV3", "L004", _testdiv);
        }

        private String getDateStr(final DB2UDB db2, final String date) {
            if (null == date) {
                return null;
            }
            return KNJ_EditDate.h_format_JP(db2, date);
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

