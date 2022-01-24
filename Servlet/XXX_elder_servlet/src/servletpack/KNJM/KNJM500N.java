/*
 * $Id: 97458094c953291330ffa2b6181cc2f55f02da99 $
 *
 * 作成日: 2019/06/05
 * 作成者: matsushima
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJM;


import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJEditString;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * 学校教育システム 賢者
 */

public class KNJM500N {

    private static final Log log = LogFactory.getLog(KNJM500N.class);

    private boolean _hasData;

    private Param _param;

    KNJEditString knjobj = new KNJEditString();

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        final List studentList = getStudentList(db2);

        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            student.load(db2, _param);

            svf.VrSetForm("KNJM500N.frm", 4);

            // タイトル
            printTitle(db2, svf, student);

            // フッター
            printFooter(db2, svf, student);

            //各教科の印字
            printSvfSubClassRecord(db2, svf, student);

            svf.VrEndPage();

            _hasData = true;
        }
    }

    private List getStudentList(final DB2UDB db2) {
        final List studentList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getStudentSql(_param);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String name = rs.getString("NAME");
                final String hrName = rs.getString("HR_NAME");
                final String attendno = rs.getString("ATTENDNO");
                final String staffname = rs.getString("STAFFNAME");
                final String studytime = rs.getString("STUDYTIME");
                final String totalCredit = rs.getString("TOTALCREDIT");
                final String creditval = rs.getString("CREDITVAL");
                final String coursename = StringUtils.defaultString(rs.getString("COURSENAME"));
                final Student student = new Student(schregno, name, hrName, attendno, staffname, studytime, totalCredit, creditval, coursename);
                studentList.add(student);
            }

        } catch (SQLException e) {
            log.error("exception!!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return studentList;
    }

    private String getStudentSql(final Param param) {
        final StringBuffer stb = new StringBuffer();
        stb.append("WITH SCHNO_A AS(");
        stb.append("    SELECT  T1.YEAR, T1.SCHREGNO, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T1.SEMESTER, T1.COURSECD, T1.MAJORCD, T1.COURSECODE ");
        stb.append("    FROM    SCHREG_REGD_DAT T1 ");
        stb.append("            , V_SEMESTER_GRADE_MST T2 ");
        stb.append("    WHERE   T1.YEAR = '" + param._year + "' ");
        stb.append("        AND T1.SEMESTER = '"+ param._semester +"' ");
        stb.append("        AND T1.YEAR = T2.YEAR ");
        stb.append("        AND T1.GRADE = T2.GRADE ");
        stb.append("        AND T1.SEMESTER = T2.SEMESTER ");
        stb.append("        AND T1.GRADE||T1.HR_CLASS = '" + param._gradeHrclass + "' ");
        stb.append("        AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
        stb.append("    ) ");
        //メイン表
        stb.append("SELECT  T1.SCHREGNO, ");
        stb.append("        T2.NAME, ");
        stb.append("        T3.HR_NAME, ");
        stb.append("        T1.ATTENDNO, ");
        stb.append("        T4.STAFFNAME, ");
        stb.append("        CASE WHEN T5.TOTALSTUDYTIME_FLG = '1' THEN '合' ELSE '否' END AS STUDYTIME, ");
        stb.append("        T6.TOTALCREDIT, ");
        stb.append("        T7.CREDITVAL, ");
        stb.append("        T8.COURSENAME ");
        stb.append("FROM    SCHNO_A T1 ");
        stb.append("        INNER JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ");
        stb.append("        LEFT JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR = T1.YEAR AND T3.SEMESTER = T1.SEMESTER AND T3.GRADE = T1.GRADE AND T3.HR_CLASS = T1.HR_CLASS ");
        stb.append("        LEFT JOIN STAFF_MST T4 ON T4.STAFFCD = T3.TR_CD1 ");
        stb.append("        LEFT JOIN RECORD_TOTALSTUDYTIME_DAT T5 ");
        stb.append("               ON T5.YEAR     = T1.YEAR ");
        stb.append("              AND T5.SEMESTER = '9' ");
        stb.append("              AND T5.SCHREGNO = T1.SCHREGNO ");
        stb.append("        LEFT JOIN ( ");
        stb.append("                    SELECT ");
        stb.append("                     SUB.YEAR, ");
        stb.append("                     SUB.SCHREGNO, ");
        stb.append("                     SUM(SUB.GET_CREDIT) AS TOTALCREDIT ");
        stb.append("                    FROM ( ");
        stb.append(SubClassRecord.getSubClassSql(param, "", "")); //各教科の各科目
        stb.append("                         ) SUB ");
        stb.append("                    GROUP BY ");
        stb.append("                     SUB.YEAR, ");
        stb.append("                     SUB.SCHREGNO ");
        stb.append("                  ) T6 ");
        stb.append("               ON T6.YEAR     = T1.YEAR ");
        stb.append("              AND T6.SCHREGNO = T1.SCHREGNO ");
        stb.append("        LEFT JOIN ( ");
        stb.append("                    SELECT ");
        stb.append("                     T1.YEAR, ");
        stb.append("                     T1.SCHREGNO, ");
        stb.append("                     SUM(T1.CREDIT_TIME) AS CREDITVAL ");
        stb.append("                    FROM ");
        stb.append("                     SPECIALACT_ATTEND_DAT T1 ");
        stb.append("                     INNER JOIN V_NAME_MST M1 ");
        stb.append("                       ON M1.YEAR    = T1.YEAR ");
        stb.append("                      AND M1.NAMECD1 = 'M026' ");
        stb.append("                      AND M1.NAME1   = T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ");
        stb.append("                    GROUP BY ");
        stb.append("                     T1.YEAR, ");
        stb.append("                     T1.SCHREGNO ");
        stb.append("                  ) T7 ");
        stb.append("               ON T7.YEAR     = T1.YEAR ");
        stb.append("              AND T7.SCHREGNO = T1.SCHREGNO ");
        stb.append("        LEFT JOIN COURSE_MST T8 ");
        stb.append("               ON T8.COURSECD = T1.COURSECD ");
        stb.append("ORDER BY ");
        stb.append("        T1.GRADE, ");
        stb.append("        T1.HR_CLASS, ");
        stb.append("        T1.ATTENDNO ");
        return stb.toString();
    }

    /**
     * タイトルを印刷する
     * @param db2
     * @param svf
     * @param student
     */
    private void printTitle(final DB2UDB db2, final Vrw32alp svf, final Student student) {

        final String[] nendo = KNJ_EditDate.tate_format4(db2, _param._year + "-04-01");
        final String title = nendo[0] + nendo[1] + "年度" + "成績通知票";
        svf.VrsOut("TITLE", title); //タイトル

        String schoolName = _param._certifSchoolSchoolName;
        schoolName = (!"".equals(student._coursename)) ? schoolName + student._coursename + "課程" : "";
        final String schoonNameField = getMS932Bytecount(schoolName) > 26 ? "2" : "1";
        svf.VrsOut("SCHOOL_NAME" + schoonNameField, schoolName); //学校名

        final String logoCheck = _param._imagePass + "/SCHOOLLOGO.jpg";
        if (isFileExists(logoCheck)) {
            svf.VrsOut("SCHOOL_LOGO", logoCheck );//学校ロゴ
        }

        svf.VrsOut("HR_NAME", student._hrName); //年組

        svf.VrsOut("SCHREGNO", student._schregno); //学籍番号

        final String nameField = getMS932Bytecount(student._name) > 34 ? "3" : getMS932Bytecount(student._name) > 26 ? "2" : "1";
        svf.VrsOut("NAME" + nameField, student._name); //氏名

    }

    /**
     * フッターを印刷する
     * @param db2
     * @param svf
     * @param student
     */
    private void printFooter(final DB2UDB db2, final Vrw32alp svf, final Student student) {

        svf.VrsOut("JUDGE", student._studytime); //合否

        svf.VrsOut("TOTAL_ACT_GET_CREDIT", ""); //総学修得単位数

        svf.VrsOut("TOTAL_GET_CREDIT", student._totalCredit); //修得単位数合計

        String[] nendo = KNJ_EditDate.tate_format4(db2, _param._year + "-04-01");
        svf.VrsOut("NENDO", nendo[1]); //年度

        svf.VrsOut("SP_ACT_TIME", student._creditval); //特別活動時数

        String staffField = getMS932Bytecount(student._hrStaffName) > 30 ? "3" : getMS932Bytecount(student._hrStaffName) > 26 ? "2" : "1";
        svf.VrsOut("TR_NAME" + staffField, student._hrStaffName); //担任氏名

    }

    /**
     * 各教科の情報を印刷する
     * @param svf
     * @param student
     */
    private void printSvfSubClassRecord(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        int grp = 0;
        String className = "";

        //各教科
        for (final Iterator it = student._classRecordList.iterator(); it.hasNext();) {
            final ClassRecord classRecord = (ClassRecord) it.next();
            classRecord.load(db2, _param);
            int idx = 0;

            className = classRecord._classname;

            //教科ごとの科目
            for (final Iterator itv = classRecord._subClassRecordList.iterator(); itv.hasNext();) {
            	final SubClassRecord subClass = (SubClassRecord) itv.next();

                final String setName = className.length() > idx ? className.substring(idx,idx+1) : "";
                svf.VrsOut("CLASS_NAME1", setName); // 教科名
                svf.VrsOut("SUBCLASS_NAME1", subClass._subclassname); // 科目名
                svf.VrsOut("CREDIT", subClass._credits); // 単位数
                svf.VrsOut("VALUE", subClass._value); // 評価
                svf.VrsOut("GET_CREDIT", subClass._get_credit); // 修得単位数
                String remarkField = getMS932Bytecount(subClass._remark) > 30 ? "3" : getMS932Bytecount(subClass._remark) > 20 ? "2" : "1";
                svf.VrsOut("REMARK" + remarkField, subClass._remark); // 備考

                svf.VrsOut("GRPCD", String.valueOf(grp)); // グループ

                svf.VrEndRecord();
                idx++;
            }

            while (className.length() > idx) {
                //教科名を最終文字まで出力
            	final String setName = className.length() > idx ? className.substring(idx,idx+1) : "";
                svf.VrsOut("CLASS_NAME1", setName); // 教科名
                svf.VrsOut("GRPCD", String.valueOf(grp)); // グループ
                idx++;
                svf.VrEndRecord();
            }
            grp++;
        }

    }

    private static int getMS932Bytecount(String str) {
        int count = 0;
        if (null != str) {
            try {
                count = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error(e);
            }
        }
        return count;
    }

    private boolean isFileExists(final String fileName) {
        File fileCheck = new File(fileName);
        return fileCheck.exists();
    }

    /**
     * 生徒情報
     */
    private static class Student {
        final String _schregno;
        final String _name;
        final String _hrName;
        final String _attendno;
        final String _hrStaffName;
        final String _studytime;
        final String _totalCredit;
        final String _creditval;
        final String _coursename;

        List _classRecordList = Collections.EMPTY_LIST;

        public Student(final String schregno, final String name, final String hrName, final String attendno, final String hrStaffName, final String studytime, final String totalCredit, final String creditval, final String coursename) {
            _schregno = schregno;
            _name = name;
            _hrName = hrName;
            _attendno = attendno;
            _hrStaffName = hrStaffName;
            _studytime = studytime;
            _totalCredit = totalCredit;
            _creditval = creditval;
            _coursename = coursename;
        }

        public void load(final DB2UDB db2, final Param param) {
            _classRecordList = ClassRecord.getClassRecordList(db2, param, _schregno);
        }

    }


    /**
     * 各教科
     */
    private static class ClassRecord {

        final String _schregno;
        final String _classcd;
        final String _school_kind;
        final String _curriculum_cd;
        final String _classname;
        List _subClassRecordList = Collections.EMPTY_LIST;
        ClassRecord(
                final String schregno,
                final String classcd,
                final String school_kind,
                final String curriculum_cd,
                final String classname) {
            _schregno = schregno;
            _classcd = classcd;
            _school_kind = school_kind;
            _curriculum_cd = curriculum_cd;
            _classname = classname;

        }

        public void load(final DB2UDB db2, final Param param) {
            _subClassRecordList = SubClassRecord.getSubClassRecordList(db2, param, _schregno, _classcd);
        }

        public static List getClassRecordList(final DB2UDB db2, final Param param, final String schregno) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getClassSql(param, schregno);
                log.debug(" view record sql = "+  sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String classcd = rs.getString("CLASSCD");
                    final String school_kind = rs.getString("SCHOOL_KIND");
                    final String curriculum_cd = rs.getString("CURRICULUM_CD");
                    final String classname = rs.getString("CLASSNAME");

                    final ClassRecord viewRecord = new ClassRecord(schregno, classcd, school_kind, curriculum_cd, classname);

                    list.add(viewRecord);

                }

            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private static String getClassSql(final Param param, final String schregno) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     M2.CLASSCD, ");
            stb.append("     M2.SCHOOL_KIND, ");
            stb.append("     M2.CURRICULUM_CD, ");
            stb.append("     M3.CLASSNAME ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1 ");
            stb.append("     INNER JOIN CHAIR_STD_DAT M1 ");
            stb.append("       ON M1.YEAR     = T1.YEAR ");
            stb.append("      AND M1.SEMESTER = T1.SEMESTER ");
            stb.append("      AND M1.SCHREGNO = T1.SCHREGNO ");
            stb.append("     INNER JOIN CHAIR_DAT M2 ");
            stb.append("       ON M2.YEAR     = M1.YEAR ");
            stb.append("      AND M2.SEMESTER = M1.SEMESTER ");
            stb.append("      AND M2.CHAIRCD  = M1.CHAIRCD ");
            stb.append("     LEFT JOIN CLASS_MST M3 ");
            stb.append("       ON M3.CLASSCD     = M2.CLASSCD ");
            stb.append("      AND M3.SCHOOL_KIND = M2.SCHOOL_KIND ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
            stb.append(" ORDER BY ");
            stb.append("     CLASSCD ");

            return stb.toString();
        }
    }


    /**
     * 各教科の科目ごとの情報
     */
    private static class SubClassRecord {

        final String _schregno;
        final String _classcd;
        final String _school_kind;
        final String _curriculum_cd;
        final String _subclasscd;
        final String _subclassname;
        final String _credits;
        final String _value;
        final String _get_credit;
        final String _remark;
        SubClassRecord(
                final String schregno,
                final String classcd,
                final String school_kind,
                final String curriculum_cd,
                final String subclasscd,
                final String subclassname,
                final String credits,
                final String value,
                final String get_credit,
                final String remark) {
            _schregno = schregno;
            _classcd = classcd;
            _school_kind = school_kind;
            _curriculum_cd = curriculum_cd;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _credits = credits;
            _value = value;
            _get_credit = get_credit;
            _remark = remark;

        }

        public static List getSubClassRecordList(final DB2UDB db2, final Param param, final String schregno , final String classCd) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getSubClassSql(param, schregno, classCd);
                log.debug(" view record sql = "+  sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String classcd = rs.getString("CLASSCD");
                    final String school_kind = rs.getString("SCHOOL_KIND");
                    final String curriculum_cd = rs.getString("CURRICULUM_CD");
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String subclassname = rs.getString("SUBCLASSNAME");
                    final String credits = rs.getString("CREDITS");
                    final String value = rs.getString("VALUE");
                    final String get_credit = rs.getString("GET_CREDIT");
                    final String remark = rs.getString("REMARK");

                    final SubClassRecord viewRecord = new SubClassRecord(schregno, classcd, school_kind, curriculum_cd, subclasscd, subclassname, credits, value, get_credit, remark);

                    list.add(viewRecord);

                }

            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private static String getSubClassSql(final Param param, final String schregno, final String classCd) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     M2.CLASSCD, ");
            stb.append("     M2.SCHOOL_KIND, ");
            stb.append("     M2.CURRICULUM_CD, ");
            stb.append("     M3.CLASSNAME, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     M2.CLASSCD || M2.SCHOOL_KIND || M2.CURRICULUM_CD || M2.SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append("     M2.SUBCLASSCD, ");
            }
            stb.append("     M4.SUBCLASSNAME, ");
            stb.append("     M5.CREDITS, ");
            stb.append("     M6.VALUE, ");
            stb.append("     M6.GET_CREDIT, ");
            stb.append("     M7.REMARK ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1 ");
            stb.append("     INNER JOIN CHAIR_STD_DAT M1 ");
            stb.append("       ON M1.YEAR     = T1.YEAR ");
            stb.append("      AND M1.SEMESTER = T1.SEMESTER ");
            stb.append("      AND M1.SCHREGNO = T1.SCHREGNO ");
            stb.append("     INNER JOIN CHAIR_DAT M2 ");
            stb.append("       ON M2.YEAR     = M1.YEAR ");
            stb.append("      AND M2.SEMESTER = M1.SEMESTER ");
            stb.append("      AND M2.CHAIRCD  = M1.CHAIRCD ");
            if(!"".equals(classCd)) {
                stb.append("      AND M2.CLASSCD  = '" + classCd + "' ");
            }
            stb.append("     LEFT JOIN CLASS_MST M3 ");
            stb.append("       ON M3.CLASSCD     = M2.CLASSCD ");
            stb.append("      AND M3.SCHOOL_KIND = M2.SCHOOL_KIND ");
            stb.append("     LEFT JOIN SUBCLASS_MST M4 ");
            stb.append("       ON M4.SUBCLASSCD    = M2.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("      AND M4.CLASSCD       = M2.CLASSCD ");
                stb.append("      AND M4.SCHOOL_KIND   = M2.SCHOOL_KIND ");
                stb.append("      AND M4.CURRICULUM_CD = M2.CURRICULUM_CD ");
            }
            stb.append("     LEFT JOIN CREDIT_MST M5 ");
            stb.append("       ON M5.YEAR          = T1.YEAR ");
            stb.append("      AND M5.COURSECD      = T1.COURSECD ");
            stb.append("      AND M5.MAJORCD       = T1.MAJORCD ");
            stb.append("      AND M5.GRADE         = T1.GRADE ");
            stb.append("      AND M5.COURSECODE    = T1.COURSECODE ");
            stb.append("      AND M5.SUBCLASSCD    = M2.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("      AND M5.CLASSCD       = M2.CLASSCD ");
                stb.append("      AND M5.SCHOOL_KIND   = M2.SCHOOL_KIND ");
                stb.append("      AND M5.CURRICULUM_CD = M2.CURRICULUM_CD ");
            }
            stb.append("     LEFT JOIN RECORD_SCORE_HIST_DAT M6 ");
            stb.append("       ON M6.YEAR          = M2.YEAR ");
            stb.append("      AND M6.SEMESTER      = '9' ");
            stb.append("      AND M6.TESTKINDCD    = '99' ");
            stb.append("      AND M6.TESTITEMCD    = '00' ");
            stb.append("      AND M6.SCORE_DIV     = '08' ");
            stb.append("      AND M6.SUBCLASSCD    = M2.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("      AND M6.CLASSCD       = M2.CLASSCD ");
                stb.append("      AND M6.SCHOOL_KIND   = M2.SCHOOL_KIND ");
                stb.append("      AND M6.CURRICULUM_CD = M2.CURRICULUM_CD ");
            }
            stb.append("      LEFT JOIN RECORD_REMARK_DAT M7 ");
            stb.append("       ON M7.YEAR     = M1.YEAR ");
            stb.append("      AND M7.SUBCLASSCD    = M2.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("      AND M7.CLASSCD       = M2.CLASSCD ");
                stb.append("      AND M7.SCHOOL_KIND   = M2.SCHOOL_KIND ");
                stb.append("      AND M7.CURRICULUM_CD = M2.CURRICULUM_CD ");
            }
            stb.append("      AND M7.SCHREGNO      = T1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            if(!"".equals(schregno)) {
            	stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
            }
            stb.append(" ORDER BY ");
            stb.append("     CLASSCD, ");
            stb.append("     SUBCLASSCD ");

            return stb.toString();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 67863 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _semester;
        final String _gradeHrclass;
        final String[] _categorySelected;
        final String _certifSchoolSchoolName;

        /** 教育課程コードを使用するか */
        final String _useCurriculumcd;

        final String _imagePass;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _categorySelected = request.getParameterValues("category_selected");

            _certifSchoolSchoolName = getCertifSchoolDat(db2, "SCHOOL_NAME");

            _useCurriculumcd = request.getParameter("useCurriculumcd");

            String rootPass = request.getParameter("DOCUMENTROOT"); // '/usr/local/development/src'
            _imagePass = rootPass + "/image/";

        }

        private String getCertifSchoolDat(final DB2UDB db2, final String field) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT " + field + " FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '104' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (null != rs.getString(field)) {
                        rtn = rs.getString(field);
                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }
    }
}

// eof

