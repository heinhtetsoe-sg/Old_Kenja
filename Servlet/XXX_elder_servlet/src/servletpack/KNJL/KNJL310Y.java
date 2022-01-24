/*
 * $Id: 57687b45d6ece225fd05ef8e8ac22ec85935f290 $
 *
 * 作成日: 2010/11/01
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;

/**
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３１０Ｙ＞  志願者データチェックリスト
 **/
public class KNJL310Y {

    private static final Log log = LogFactory.getLog(KNJL310Y.class);

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

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }
    }

    /**
     * 文字列をMS932でエンコードしたバイト数を得る
     * @param str 文字列
     * @return バイト数
     */
    private int keta(String str) {
        int count = 0;
        try {
            count = str.getBytes("MS932").length;
        } catch (Exception e) {
        }
        return count;
    }

    /**
     * listを最大数ごとにグループ化したリストを得る
     * @param list
     * @param max 最大数
     * @return listを最大数ごとにグループ化したリスト
     */
    private static List getPageList(final List list, final int max) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= max) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        final String formName;
        final String formName2;
        final int linePerPage;
        final String title;
        String title2 = "";
        if ("1".equals(_param._applicantDiv)) {
            formName = "KNJL310Y_1.frm";
            formName2 = null;
            linePerPage = 20;
            title = StringUtils.defaultString(_param._title);
        } else {
            formName = "KNJL310Y_2.frm";
            formName2 = "KNJL310Y_2_2.frm";
            linePerPage = 50;
            title = StringUtils.defaultString(_param._title) + "入学試験志願者データチェックリスト";
            title2 = StringUtils.defaultString(_param._title) + "入学試験志願者データチェックリスト（その２）";
        }

        svf.VrSetForm(formName, 4);

        PreparedStatement ps = null;
        ResultSet rs = null;

        final List courses = getCourses(db2, ps, rs);
        for (Iterator itC = courses.iterator(); itC.hasNext(); ) {
            final Course course = (Course) itC.next();

            final List pageList = getPageList(course._applicants, linePerPage);

            for (int pi = 0; pi < pageList.size(); pi++) {
                final List applicants = (List) pageList.get(pi);
                final int page;
                final int pageMax;
                if ("1".equals(_param._applicantDiv)) {
                    page = pi + 1;
                    pageMax = pageList.size();
                } else {
                    page = 2 * pi + 1;
                    pageMax = 2 * pageList.size();
                }
                svf.VrSetForm(formName, 4);

                for (int ai = 0; ai < applicants.size(); ai++) {
                    final Applicant applicant = (Applicant) applicants.get(ai);

                    svf.VrsOut("NENDO",            _param._year + "年度　");
                    svf.VrsOut("PAGE1",            String.valueOf(page));
                    svf.VrsOut("PAGE2",            String.valueOf(pageMax));
                    svf.VrsOut("DATE",             _param._dateString);
                    svf.VrsOut("TITLE",            title);
                    svf.VrsOut("SUBTITLE",         _param._subTitle);
                    svf.VrsOut("COURSE",           applicant._examCourseName);
                    svf.VrsOut("RECOMMEND_KIND",   applicant._recomKindName);
                    svf.VrsOut("RECOMMEND",        applicant._recomDiv);

                    final int nameKeta     = keta(applicant._name);
                    final int nameKanaKeta = keta(applicant._nameKana);
                    final int gNameKeta    = keta(applicant._gname);
                    final int gKanaKeta    = keta(applicant._gkana);
                    final String nameField     = "NAME" + (30 < nameKeta ? "3" : (20 < nameKeta ? "2" : "1"));
                    final String nameKanaField = "NAME_KANA" + (50 < nameKanaKeta ? "3" : (20 < nameKanaKeta ? "2" : "1"));
                    final String gNameField    = "GNAME" + (30 < gNameKeta ? "3" : (20 < gNameKeta ? "2" : "1"));
                    final String gKanaField    = "GKANA" + (50 < gKanaKeta ? "3" : (20 < gKanaKeta ? "2" : "1"));

                    final String addr = applicant._addr1 + applicant._addr2;
                    final int addrKeta = keta(addr);

                    svf.VrsOut("BACK",             applicant._interviewAttendFlg);
                    svf.VrsOut("EXAMNO",           applicant._examno);
                    svf.VrsOut(nameField,          applicant._name);
                    svf.VrsOut(nameKanaField,      applicant._nameKana);
                    svf.VrsOut("SEX",              applicant._sex);
                    svf.VrsOut("BIRTHDAY",         applicant._birthday);
                    svf.VrsOut("FINSCHOOL_ABBV",   applicant._finschoolNameAbbv);
                    svf.VrsOut("GRD_ABBV",         applicant._fsGrddivName);
                    svf.VrsOut("EXAMCOURSE_NAME1", applicant._examCourseName);
                    svf.VrsOut("EXAMCOURSE_NAME2", applicant._examCourseName2);
                    svf.VrsOut("TELNO",            applicant._telno);
                    svf.VrsOut("ZIPCD",            applicant._zipcd);
                    if (50 < addrKeta) {
                        svf.VrsOut("ADDRESS2_1",   applicant._addr1);
                        svf.VrsOut("ADDRESS2_2",   applicant._addr2);
                    } else {
                        final String addressField = "ADDRESS1_" + (40 < addrKeta ? "2" : "1");
                        svf.VrsOut(addressField,   addr);
                    }
                    svf.VrsOut(gNameField,         applicant._gname);
                    svf.VrsOut(gKanaField,         applicant._gkana);
                    svf.VrsOut("RELASHINSHIP",     applicant._relationshipName);
                    svf.VrsOut("GENERAL",          applicant._generalFlg);
                    svf.VrsOut("SPORTS",           applicant._sportsFlg);
                    svf.VrsOut("DORMITORY",        applicant._dormFlg);
                    svf.VrsOut("SH",               applicant._shDivName);
                    svf.VrsOut("SH_SCHOOL",        applicant._shSchoolName);

                    final String[] remark1 = KNJ_EditEdit.get_token(applicant._remark1, 40, 4);
                    if (null != remark1) {
                        for (int i = 0; i < remark1.length; i++) {
                            svf.VrsOut("REMARK1_" + (i + 1), remark1[i]);
                        }
                    }
                    final String[] remark2 = KNJ_EditEdit.get_token(applicant._remark2, 40, 2);
                    if (null != remark2) {
                        for (int i = 0; i < remark2.length; i++) {
                            svf.VrsOut("REMARK2_" + (i + 1), remark2[i]);
                        }
                    }
                    svf.VrsOut("CONFRPT", applicant._averageAll);
                    svf.VrsOut("SP_JUDGE",         applicant._shiftDesireFlg);
                    svf.VrEndRecord();

                    _hasData = true;
                }

                if (!"1".equals(_param._applicantDiv)) {
                    svf.VrSetForm(formName2, 4);

                    for (int ai = 0; ai < applicants.size(); ai++) {
                        final Applicant applicant = (Applicant) applicants.get(ai);

                        svf.VrsOut("NENDO",            _param._year + "年度　");
                        svf.VrsOut("PAGE1",            String.valueOf(page + 1));
                        svf.VrsOut("PAGE2",            String.valueOf(pageMax));
                        svf.VrsOut("DATE",             _param._dateString);
                        svf.VrsOut("TITLE",            title2);
                        svf.VrsOut("SUBTITLE",         _param._subTitle);
                        svf.VrsOut("COURSE",           applicant._examCourseName);
                        svf.VrsOut("RECOMMEND_KIND",   applicant._recomKindName);

                        final int nameKeta     = keta(applicant._name);
                        final int nameKanaKeta = keta(applicant._nameKana);
                        final String nameField     = "NAME" + (30 < nameKeta ? "3" : (20 < nameKeta ? "2" : "1"));
                        final String nameKanaField = "NAME_KANA" + (50 < nameKanaKeta ? "3" : (20 < nameKanaKeta ? "2" : "1"));

                        svf.VrsOut("BACK",             applicant._interviewAttendFlg);
                        svf.VrsOut("EXAMNO",           applicant._examno);
                        svf.VrsOut(nameField,          applicant._name);
                        svf.VrsOut(nameKanaField,      applicant._nameKana);
                        svf.VrsOut("SEX",              applicant._sex);
                        svf.VrsOut("ABSENCE_DAYS1",    applicant._absenceDays1);
                        svf.VrsOut("ABSENCE_DAYS2",    applicant._absenceDays2);
                        svf.VrsOut("ABSENCE_DAYS3",    applicant._absenceDays3);

                        final int ketaRemark1 = KNJ_EditKinsoku.getMS932ByteCount(applicant._remark1);
                        svf.VrsOut("REMARK1" + (ketaRemark1 <= 123 ? "" : "_2"), applicant._remark1);

                        final int ketaRemark2 = KNJ_EditKinsoku.getMS932ByteCount(applicant._remark2);
                        svf.VrsOut("REMARK2" + (ketaRemark2 <= 54 ? "" : "_2"), applicant._remark2);

                        svf.VrEndRecord();

                        _hasData = true;
                    }
                }
            }
        }
    }

    private List getCourses(final DB2UDB db2, PreparedStatement ps, ResultSet rs) {
        final List courses = new ArrayList();
        try {
            final String sql = getApplicantSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String examno = rs.getString("EXAMNO");
                final String examCourseCd = null == rs.getString("EXAMCOURSECD") ? "" : rs.getString("EXAMCOURSECD");
                final String recomKind = null == rs.getString("RECOM_KIND") ? "" : rs.getString("RECOM_KIND");
                final String examCourseName = rs.getString("EXAMCOURSE_NAME");
                final String recomKindName = rs.getString("RECOM_KINDNAME");
                final String name     = rs.getString("NAME");
                final String nameKana = rs.getString("NAME_KANA");
                final String gname    = rs.getString("GNAME");
                final String gkana    = rs.getString("GKANA");
                final String addr1 = null == rs.getString("ADDRESS1") ? "" : rs.getString("ADDRESS1");
                final String addr2 = null == rs.getString("ADDRESS2") ? "" : rs.getString("ADDRESS2");
                final String interviewAttendFlg = "1".equals(rs.getString("INTERVIEW_ATTEND_FLG")) ? "帰" : "";
                final String shiftDesireFlg = "1".equals(rs.getString("SHIFT_DESIRE_FLG")) ? "有" : "";
                final String generalFlg = "1".equals(rs.getString("GENERAL_FLG")) ? "有" : "";
                final String sportsFlg = "1".equals(rs.getString("SPORTS_FLG")) ? "有" : "";
                final String dormFlg = "1".equals(rs.getString("DORMITORY_FLG")) ? "有" : "";
                final String relationshipName = rs.getString("RELATIONSHIP_NAME");
                final String shDivName = rs.getString("SH_DIVNAME");
                final String shSchoolName = rs.getString("SH_SCHOOL_NAME");
                final String sremark1 = rs.getString("REMARK1");
                final String sremark2 = rs.getString("REMARK2");
                final String sex = rs.getString("SEX");
                final String birthday = null == rs.getString("BIRTHDAY") ? "" : rs.getString("BIRTHDAY").replace('-', '.');
                final String finschoolNameAbbv = rs.getString("FINSCHOOL_NAME_ABBV");
                final String fsGrddivName = rs.getString("FS_GRDDIVNAME");
                final String examCourseName2 = rs.getString("EXAMCOURSE_NAME2");
                final String telno = rs.getString("TELNO");
                final String zipcd = rs.getString("ZIPCD");
                final StringBuffer recomDivStb = new StringBuffer();
                String comma = "";

                Course course = null;
                for (Iterator it = courses.iterator(); it.hasNext(); ) {
                    final Course course0 = (Course) it.next();
                    if (course0._examCourseCd.equals(examCourseCd) && course0._recomKind.equals(recomKind)) {
                        course = course0;
                        break;
                    }
                }
                if (null == course) {
                    course = new Course(examCourseCd, recomKind);
                    courses.add(course);
                }
                for (int i = 1; i <= 4; i++) {
                    final String recomItem = rs.getString("RECOM_ITEM" + String.valueOf(i));
                    recomDivStb.append(comma).append(recomItem == null ? " " : String.valueOf(i));
                    comma = ",";
                }
                final String average5 = rs.getString("AVERAGE5");
                final String averageAll = NumberUtils.isNumber(rs.getString("AVERAGE_ALL")) ? String.valueOf(new BigDecimal(rs.getString("AVERAGE_ALL")).setScale(0)) : null;
                final String absenceDays1 = rs.getString("ABSENCE_DAYS1");
                final String absenceDays2 = rs.getString("ABSENCE_DAYS2");
                final String absenceDays3 = rs.getString("ABSENCE_DAYS3");
                final Applicant applicant = new Applicant(examno, examCourseCd, recomKind, examCourseName, recomKindName,
                        name, nameKana, gname, gkana, addr1, addr2,
                        interviewAttendFlg, shiftDesireFlg, generalFlg, sportsFlg, dormFlg, relationshipName,
                        shDivName, shSchoolName, sremark1, sremark2, sex, birthday,
                        finschoolNameAbbv, fsGrddivName, examCourseName2, telno, zipcd, recomDivStb.toString(), average5, averageAll,
                        absenceDays1, absenceDays2, absenceDays3);
                course._applicants.add(applicant);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return courses;
    }

    private class Course {
        final String _examCourseCd;
        final String _recomKind;
        final List _applicants = new ArrayList();
        public Course(String examCourseCd, String recomKind) {
            _examCourseCd = examCourseCd;
            _recomKind = recomKind;
        }
        public String toString() {
            return "Course(" + _examCourseCd + ":" + _recomKind + ")";
        }
    }

    private class Applicant {
        final String _examno;
        final String _examCourseCd;
        final String _recomKind;
        final String _examCourseName;
        final String _recomKindName;
        final String _name;
        final String _nameKana;
        final String _gname;
        final String _gkana;
        final String _addr1;
        final String _addr2;
        final String _interviewAttendFlg;
        final String _shiftDesireFlg;
        final String _generalFlg;
        final String _sportsFlg;
        final String _dormFlg;
        final String _relationshipName;
        final String _shDivName;
        final String _shSchoolName;
        final String _remark1;
        final String _remark2;
        final String _sex;
        final String _birthday;
        final String _finschoolNameAbbv;
        final String _fsGrddivName;
        final String _examCourseName2;
        final String _telno;
        final String _zipcd;
        final String _recomDiv;
        final String _average5;
        final String _averageAll;
        final String _absenceDays1;
        final String _absenceDays2;
        final String _absenceDays3;
        public Applicant(
                final String examno,
                final String examCourseCd,
                final String recomKind,
                final String examCourseName,
                final String recomKindName,
                final String name,
                final String nameKana,
                final String gname,
                final String gkana,
                final String addr1,
                final String addr2,
                final String interviewAttendFlg,
                final String shiftDesireFlg,
                final String generalFlg,
                final String sportsFlg,
                final String dormFlg,
                final String relationshipName,
                final String shDivName,
                final String shSchoolName,
                final String remark1,
                final String remark2,
                final String sex,
                final String birthday,
                final String finschoolNameAbbv,
                final String fsGrddivName,
                final String examCourseName2,
                final String telno,
                final String zipcd,
                final String recomDiv,
                final String average5,
                final String averageAll,
                final String absenceDays1,
                final String absenceDays2,
                final String absenceDays3) {
            _examno = examno;
            _examCourseCd = examCourseCd;
            _recomKind = recomKind;
            _examCourseName = examCourseName;
            _recomKindName = recomKindName;
            _name = name;
            _nameKana = nameKana;
            _gname = gname;
            _gkana = gkana;
            _addr1 = addr1;
            _addr2 = addr2;
            _interviewAttendFlg = interviewAttendFlg;
            _shiftDesireFlg = shiftDesireFlg;
            _generalFlg = generalFlg;
            _sportsFlg = sportsFlg;
            _dormFlg = dormFlg;
            _relationshipName = relationshipName;
            _shDivName = shDivName;
            _shSchoolName = shSchoolName;
            _remark1 = remark1;
            _remark2 = remark2;
            _sex = sex;
            _birthday = birthday;
            _finschoolNameAbbv = finschoolNameAbbv;
            _fsGrddivName = fsGrddivName;
            _examCourseName2 = examCourseName2;
            _telno = telno;
            _zipcd = zipcd;
            _recomDiv = recomDiv;
            _average5 = average5;
            _averageAll = averageAll;
            _absenceDays1 = absenceDays1;
            _absenceDays2 = absenceDays2;
            _absenceDays3 = absenceDays3;
        }
    }

    private String getApplicantSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T4.EXAMCOURSECD, ");
        stb.append("     T6.EXAMCOURSE_NAME, ");
        stb.append("     T1.RECOM_KIND, ");
        stb.append("     NM5.NAME1 AS RECOM_KINDNAME, ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T5.NAME, ");
        stb.append("     T5.NAME_KANA, ");
        stb.append("     NM1.NAME2 AS SEX, ");
        stb.append("     T5.BIRTHDAY, ");
        stb.append("     T3.FINSCHOOL_NAME_ABBV, ");
        stb.append("     NM3.ABBV1 AS FS_GRDDIVNAME, ");
        stb.append("     (CASE WHEN T5.TESTDIV = '" + _param._testDiv + "' AND VALUE(T5.SLIDE_FLG, '') = '1' THEN T8.EXAMCOURSE_NAME ELSE '' END) AS EXAMCOURSE_NAME2, ");
        stb.append("     T2.TELNO, ");
        stb.append("     T2.ZIPCD, ");
        stb.append("     T2.ADDRESS1, ");
        stb.append("     T2.ADDRESS2, ");
        stb.append("     T2.GNAME, ");
        stb.append("     T2.GKANA, ");
        stb.append("     T2.RELATIONSHIP, ");
        stb.append("     NM2.NAME1 AS RELATIONSHIP_NAME, ");
        stb.append("     T5.INTERVIEW_ATTEND_FLG, ");
        stb.append("     T5.SHIFT_DESIRE_FLG, ");
        stb.append("     T11.REMARK1 AS GENERAL_FLG, ");
        stb.append("     T5.SPORTS_FLG, ");
        stb.append("     T5.DORMITORY_FLG, ");
        stb.append("     T1.SHDIV, ");
        stb.append("     NM4.NAME1 AS SH_DIVNAME, ");
        stb.append("     T9.FINSCHOOL_NAME AS SH_SCHOOL_NAME, ");
        stb.append("     T5.REMARK1, ");
        stb.append("     T5.REMARK2, ");
        stb.append("     T5.RECOM_ITEM1, ");
        stb.append("     T5.RECOM_ITEM2, ");
        stb.append("     T5.RECOM_ITEM3, ");
        stb.append("     T5.RECOM_ITEM4, ");
        stb.append("     T10.AVERAGE5, ");
        stb.append("     T10.AVERAGE_ALL, ");
        stb.append("     T10.ABSENCE_DAYS AS ABSENCE_DAYS1, ");
        stb.append("     T10.ABSENCE_DAYS2, ");
        stb.append("     T10.ABSENCE_DAYS3 ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTDESIRE_DAT T1 ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T5 ON T5.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND T5.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND T5.EXAMNO = T1.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT T2 ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND T2.EXAMNO = T1.EXAMNO ");
        stb.append("     LEFT JOIN FINSCHOOL_MST T3 ON T3.FINSCHOOLCD = T5.FS_CD ");
        stb.append("     LEFT JOIN ENTEXAM_WISHDIV_MST T4 ON T4.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND T4.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND T4.TESTDIV = T1.TESTDIV ");
        stb.append("         AND T4.DESIREDIV = T1.DESIREDIV ");
        stb.append("         AND T4.WISHNO = '1' ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST T6 ON T6.ENTEXAMYEAR = T4.ENTEXAMYEAR ");
        stb.append("         AND T6.APPLICANTDIV = T4.APPLICANTDIV ");
        stb.append("         AND T6.TESTDIV = T4.TESTDIV ");
        stb.append("         AND T6.COURSECD = T4.COURSECD ");
        stb.append("         AND T6.MAJORCD = T4.MAJORCD ");
        stb.append("         AND T6.EXAMCOURSECD = T4.EXAMCOURSECD ");
        stb.append("     LEFT JOIN ENTEXAM_WISHDIV_MST T7 ON T7.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND T7.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND T7.TESTDIV = T1.TESTDIV ");
        stb.append("         AND T7.DESIREDIV = T1.DESIREDIV ");
        stb.append("         AND T7.WISHNO = '2' ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST T8 ON T8.ENTEXAMYEAR = T7.ENTEXAMYEAR ");
        stb.append("         AND T8.APPLICANTDIV = T7.APPLICANTDIV ");
        stb.append("         AND T8.TESTDIV = T7.TESTDIV ");
        stb.append("         AND T8.COURSECD = T7.COURSECD ");
        stb.append("         AND T8.MAJORCD = T7.MAJORCD ");
        stb.append("         AND T8.EXAMCOURSECD = T7.EXAMCOURSECD ");
        stb.append("     LEFT JOIN FINHIGHSCHOOL_MST T9 ON T9.FINSCHOOLCD = T5.SH_SCHOOLCD ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT T10 ON T10.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND T10.EXAMNO = T1.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T11 ");
        stb.append("            ON T11.ENTEXAMYEAR  = T5.ENTEXAMYEAR ");
        // stb.append("           AND T11.APPLICANTDIV = T5.APPLICANTDIV ");
        stb.append("           AND T11.EXAMNO       = T5.EXAMNO ");
        stb.append("           AND T11.SEQ          = '005' ");
        stb.append("     LEFT JOIN NAME_MST NM1 ON NM1.NAMECD1 = 'Z002' AND NM1.NAMECD2 = T5.SEX ");
        stb.append("     LEFT JOIN NAME_MST NM2 ON NM2.NAMECD1 = 'H201' AND NM2.NAMECD2 = T2.RELATIONSHIP ");
        stb.append("     LEFT JOIN NAME_MST NM3 ON NM3.NAMECD1 = 'L016' AND NM3.NAMECD2 = T5.FS_GRDDIV ");
        stb.append("     LEFT JOIN NAME_MST NM4 ON NM4.NAMECD1 = 'L006' AND NM4.NAMECD2 = T1.SHDIV ");
        stb.append("     LEFT JOIN NAME_MST NM5 ON NM5.NAMECD1 = 'L023' AND NM5.NAMECD2 = T1.RECOM_KIND ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._year + "' ");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND T1.TESTDIV = '" + _param._testDiv + "' ");
        stb.append("     AND NOT EXISTS ( SELECT ");
        stb.append("            'X' ");
        stb.append("         FROM ");
        stb.append("            ENTEXAM_RECEPT_DAT L1 ");
        stb.append("            INNER JOIN ENTEXAM_APPLICANTBASE_DAT B2 ");
        stb.append("                ON  B2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
        stb.append("                AND B2.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("                AND B2.EXAMNO       = T1.EXAMNO ");
        stb.append("                AND VALUE(B2.SELECT_SUBCLASS_DIV,'0') <> '1' "); //1:特進チャレンジ受験者は対象外
        stb.append("            LEFT JOIN NAME_MST L2 ON L2.NAMECD1 = 'L013' AND L2.NAMECD2 = L1.JUDGEDIV ");
        stb.append("         WHERE ");
        stb.append("            L1.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("            AND L1.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("            AND L1.TESTDIV < T1.TESTDIV  ");
        stb.append("            AND L1.EXAMNO = T1.EXAMNO  ");
        stb.append("            AND VALUE(L2.NAMESPARE1, '') = '1') ");
        if (!"9".equals(_param._desireDiv)) {
            stb.append("     AND T1.DESIREDIV = '" + _param._desireDiv + "' ");
        }
        if ("1".equals(_param._outputC)) {
            stb.append("     AND T5.SELECT_SUBCLASS_DIV = '1' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T4.COURSECD, T4.MAJORCD,T4.EXAMCOURSECD, T1.RECOM_KIND, T1.EXAMNO ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 71287 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        private final String _year;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _title;
        private final String _dateString;
        private final String _subTitle;
        private final String _desireDiv;
        private final String _outputC;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _title = getSchoolName(db2);
            _subTitle = getTestDivName(db2);

            final Calendar cal = Calendar.getInstance();
            _dateString = new SimpleDateFormat("yyyy年M月d日").format(Date.valueOf(request.getParameter("LOGIN_DATE"))) + cal.get(Calendar.HOUR_OF_DAY) + "時" + cal.get(Calendar.MINUTE) + "分";
            _desireDiv = request.getParameter("DESIREDIV");
            _outputC = request.getParameter("OUTPUT_C");
        }

        private String getSchoolName(DB2UDB db2) {
            String schoolName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'L003' AND NAMECD2 = '" + _applicantDiv + "'");
                rs = ps.executeQuery();
                if (rs.next() && null != rs.getString("NAME1")) {
                  schoolName = rs.getString("NAME1");
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return schoolName;
        }

        private String getTestDivName(DB2UDB db2) {
            String testDivName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String namecd1 = "1".equals(_applicantDiv) ? "L024" : "L004";
            try {
                ps = db2.prepareStatement(" SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + _testDiv + "'");
                rs = ps.executeQuery();
                if (rs.next() && null != rs.getString("NAME1")) {
                  testDivName = rs.getString("NAME1");
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return testDivName;
        }
    }
}

// eof
