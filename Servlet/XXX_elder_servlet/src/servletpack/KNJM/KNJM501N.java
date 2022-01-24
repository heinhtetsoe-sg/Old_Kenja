/*
 * $Id: 0dcf9ac6e6819c0dde5acaab0cdc9882b440f280 $
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

public class KNJM501N {

    private static final Log log = LogFactory.getLog(KNJM501N.class);

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

            svf.VrSetForm("KNJM501N.frm", 4);

            // タイトル
            printTitle(db2, svf, student);

            //各科目の印字
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
                final String creditval = StringUtils.defaultString(rs.getString("CREDITVAL"), "0");

                final Student student = new Student(schregno, name, hrName, attendno, staffname, creditval);
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
        stb.append("        T5.CREDITVAL ");
        stb.append("FROM    SCHNO_A T1 ");
        stb.append("        INNER JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ");
        stb.append("        LEFT JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR = T1.YEAR AND T3.SEMESTER = T1.SEMESTER AND T3.GRADE = T1.GRADE AND T3.HR_CLASS = T1.HR_CLASS ");
        stb.append("        LEFT JOIN STAFF_MST T4 ON T4.STAFFCD = T3.TR_CD1 ");
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
        stb.append("                  ) T5 ");
        stb.append("               ON T5.YEAR     = T1.YEAR ");
        stb.append("              AND T5.SCHREGNO = T1.SCHREGNO ");
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

        final String date = KNJ_EditDate.h_format_JP(db2, _param._date);
        svf.VrsOut("DATE", date); //日付

        String schoolName = _param._certifSchoolSchoolName;
        svf.VrsOut("SCHOOL_NAME", schoolName); //学校名

        final String title = _param._semesName + "成績通知票";
        svf.VrsOut("TITLE", title); //タイトル

        svf.VrsOut("HR_NAME", student._hrName); //年組

        svf.VrsOut("SCHREGNO", student._schregno); //学籍番号

        final String nameField = getMS932Bytecount(student._name) > 34 ? "3" : getMS932Bytecount(student._name) > 26 ? "2" : "1";
        svf.VrsOut("NAME" + nameField, student._name); //氏名

        final String[] nendo = KNJ_EditDate.tate_format4(db2, _param._year + "-04-01");
        final String subtitle = nendo[0] + nendo[1] + _param._semesName;
        svf.VrsOut("NENDO", subtitle); //年度

    }

    /**
     * 各教科の情報を印刷する
     * @param svf
     * @param student
     */
    private void printSvfSubClassRecord(final DB2UDB db2, final Vrw32alp svf, final Student student) {
    	int cnt = 1;
    	final int maxCnt = 19;
    	final int totalCnt = 43;
    	boolean fotterFlg = false;

        for (final Iterator itv = student._subClassRecordList.iterator(); itv.hasNext();) {
            final SubClassRecord subClass = (SubClassRecord) itv.next();

            if(cnt > totalCnt) {
                cnt = 1;
                fotterFlg = false;
            }

            int reportNo = Integer.parseInt(subClass._report_no1);
            int repSeqAll = Integer.parseInt(subClass._rep_seq_all1);
            int schSeqMin = Integer.parseInt(subClass._sch_seq_min1);
            int schoolingSeq = Integer.parseInt(subClass._schooling_seq1);
            String value1 = subClass._value1;
            String value2 = "";
            if(Integer.parseInt(subClass._rep_seq_all1) > Integer.parseInt(subClass._report_no1)) {
                value1 = "×";
            }

            if("2".equals(_param._semester)) {
                reportNo = reportNo + Integer.parseInt(subClass._report_no2);
                repSeqAll = repSeqAll + Integer.parseInt(subClass._rep_seq_all2);
                schSeqMin = schSeqMin + Integer.parseInt(subClass._sch_seq_min2);
                schoolingSeq = schoolingSeq + Integer.parseInt(subClass._schooling_seq2);

                value2 = subClass._value2;
                if(Integer.parseInt(subClass._rep_seq_all2) > Integer.parseInt(subClass._report_no2)) {
                    value2 = "×";
                }
            }

            if(reportNo < 0) reportNo = 0;
            final String setReport = repSeqAll <= reportNo ? "完":String.valueOf(reportNo);


            final String nameField = getMS932Bytecount(subClass._subclassname) > 20 ? "_4" : getMS932Bytecount(subClass._subclassname) > 16 ? "_3" : getMS932Bytecount(subClass._subclassname) > 12 ? "_2" : "_1";
            svf.VrsOut("SUBCLASS_NAME1" + nameField, subClass._subclassname); // 科目名
            svf.VrsOut("VALUE1_1", value1); // 前期評価
            svf.VrsOut("VALUE1_2", value2); // 後期評価
            svf.VrsOut("REPORT_NO1", setReport); // レポート番号
            svf.VrsOut("SP_NUM1", String.valueOf(repSeqAll)); // 規定枚数
            svf.VrsOut("SC_ATTEND_NUM1", String.valueOf(schoolingSeq)); // スクーリング 出席回数
            svf.VrsOut("SC_NUM1", String.valueOf(schSeqMin)); // スクーリング 規定回数
            svf.VrEndRecord();

            if(cnt == maxCnt && fotterFlg == false) {
                // フッター
                printFooter(db2, svf, student);
                fotterFlg = true;
            }

            cnt++;
        }

        //空行出力
        while(cnt <= maxCnt) {
            svf.VrsOut("BLANK1", "BLANK"); //空行用
            svf.VrEndRecord();
            cnt++;
        }


        // フッター
        if(cnt < maxCnt || fotterFlg == false) printFooter(db2, svf, student);

    }


    /**
     * フッターを印刷する
     * @param db2
     * @param svf
     * @param student
     */
    private void printFooter(final DB2UDB db2, final Vrw32alp svf, final Student student) {

        //TODO 保留の為、科目名以外は空白。
        svf.VrsOut("SUBCLASS_NAME2_1", "つどい"); // 科目名
        svf.VrsOut("VALUE2_2", "");               // 評価
        svf.VrsOut("REPORT_NO2", "");             // レポート番号
        svf.VrsOut("SP_NUM2", "");                // 規定枚数
        svf.VrsOut("SC_ATTEND_NUM2", "");         // スクーリング 出席回数
        svf.VrsOut("SC_NUM2", "");                // スクーリング 規定回数
        svf.VrEndRecord();

        svf.VrsOut("SUBCLASS_NAME3_1", "生活文化の伝承A"); // 科目名
        svf.VrsOut("SP_NUM3", "");                         // 規定出席数
        svf.VrsOut("SC_ATTEND_NUM3", "");                  // スクーリング 出席回数
        svf.VrEndRecord();

        svf.VrsOut("SUBCLASS_NAME3_1", "生活文化の伝承B"); // 科目名
        svf.VrsOut("SP_NUM3", "");                         // 規定出席数
        svf.VrsOut("SC_ATTEND_NUM3", "");                  // スクーリング 出席回数
        svf.VrEndRecord();

        svf.VrsOut("SC_ATTEND_NUM4", student._creditval); //単位時間
        svf.VrEndRecord();

        svf.VrsOut("BLANK2", "BLANK"); //空行用
        svf.VrEndRecord();

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
        final String _creditval;

        List _subClassRecordList = Collections.EMPTY_LIST;

        public Student(final String schregno, final String name, final String hrName, final String attendno, final String hrStaffName, final String creditval) {
            _schregno = schregno;
            _name = name;
            _hrName = hrName;
            _attendno = attendno;
            _hrStaffName = hrStaffName;
            _creditval = creditval;
        }

        public void load(final DB2UDB db2, final Param param) {
        	_subClassRecordList = SubClassRecord.getSubClassRecordList(db2, param, _schregno);
        }

    }

    /**
     * 科目ごとの情報
     */
    private static class SubClassRecord {

        final String _schregno;
        final String _classcd;
        final String _school_kind;
        final String _curriculum_cd;
        final String _subclasscd;
        final String _classname;
        final String _subclassname;
        final String _value1;
        final String _value2;
        final String _rep_seq_all1;
        final String _rep_seq_all2;
        final String _report_no1;
        final String _report_no2;
        final String _sch_seq_min1;
        final String _sch_seq_min2;
        final String _schooling_seq1;
        final String _schooling_seq2;
        SubClassRecord(
                final String schregno,
                final String classcd,
                final String school_kind,
                final String curriculum_cd,
                final String subclasscd,
                final String classname,
                final String subclassname,
                final String value1,
                final String value2,
                final String rep_seq_all1,
                final String rep_seq_all2,
                final String report_no1,
                final String report_no2,
                final String sch_seq_min1,
                final String sch_seq_min2,
                final String schooling_seq1,
                final String schooling_seq2) {
            _schregno = schregno;
            _classcd = classcd;
            _school_kind = school_kind;
            _curriculum_cd = curriculum_cd;
            _subclasscd = subclasscd;
            _classname = classname;
            _subclassname = subclassname;
            _value1 = value1;
            _value2 = value2;
            _rep_seq_all1 = rep_seq_all1;
            _rep_seq_all2 = rep_seq_all2;
            _report_no1 = report_no1;
            _report_no2 = report_no2;
            _sch_seq_min1 = sch_seq_min1;
            _sch_seq_min2 = sch_seq_min2;
            _schooling_seq1 = schooling_seq1;
            _schooling_seq2 = schooling_seq2;
        }

        public static List getSubClassRecordList(final DB2UDB db2, final Param param, final String schregno) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getSubClassSql(param, schregno);
                log.debug(" view record sql = "+  sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String classcd = rs.getString("CLASSCD");
                    final String school_kind = rs.getString("SCHOOL_KIND");
                    final String curriculum_cd = rs.getString("CURRICULUM_CD");
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String classname = rs.getString("CLASSNAME");
                    final String subclassname = rs.getString("SUBCLASSNAME");
                    final String value1 = StringUtils.defaultString(rs.getString("VALUE1"), "0");
                    final String value2 = StringUtils.defaultString(rs.getString("VALUE2"), "0");
                    final String rep_seq_all1 = StringUtils.defaultString(rs.getString("REP_SEQ_ALL1"), "0");
                    final String rep_seq_all2 = StringUtils.defaultString(rs.getString("REP_SEQ_ALL2"), "0");
                    final String report_no1 = StringUtils.defaultString(rs.getString("REPORT_NO1"), "0");
                    final String report_no2 = StringUtils.defaultString(rs.getString("REPORT_NO2"), "0");
                    final String sch_seq_min1 = StringUtils.defaultString(rs.getString("SCH_SEQ_MIN1"), "0");
                    final String sch_seq_min2 = StringUtils.defaultString(rs.getString("SCH_SEQ_MIN2"), "0");
                    final String schooling_seq1 = StringUtils.defaultString(rs.getString("SCHOOLING_SEQ1"), "0");
                    final String schooling_seq2 = StringUtils.defaultString(rs.getString("SCHOOLING_SEQ2"), "0");
                    final SubClassRecord viewRecord = new SubClassRecord(schregno, classcd, school_kind, curriculum_cd, subclasscd, classname, subclassname, value1, value2, rep_seq_all1, rep_seq_all2, report_no1, report_no2, sch_seq_min1, sch_seq_min2, schooling_seq1, schooling_seq2);

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

        private static String getSubClassSql(final Param param, final String schregno) {
            final StringBuffer stb = new StringBuffer();

            //CHAIRCDと科目
            stb.append(" WITH CHAIR AS( ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.CHAIRCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T2.CLASSCD, ");
                stb.append("     T2.SCHOOL_KIND, ");
                stb.append("     T2.CURRICULUM_CD, ");
            }
            stb.append("     T2.SUBCLASSCD ");
            stb.append(" FROM ");
            stb.append("     CHAIR_STD_DAT T1 ");
            stb.append(" INNER JOIN CHAIR_DAT T2 ");
            stb.append("       ON T2.YEAR          = T1.YEAR ");
            stb.append("      AND T2.SEMESTER      = T1.SEMESTER ");
            stb.append("      AND T2.CHAIRCD       = T1.CHAIRCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
            //学期別レポート規定枚数、スクーリング規定数
            stb.append(" ), CORRES AS( ");
            stb.append(" SELECT ");
            stb.append("     T1.YEAR, ");
            stb.append("     T2.SEMESTER, ");
            stb.append("     T1.CHAIRCD, ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     T1.REP_SEQ_ALL, ");
            stb.append("     T1.SCH_SEQ_MIN ");
            stb.append(" FROM ");
            stb.append("     CHAIR_CORRES_DAT T1 ");
            stb.append("     INNER JOIN CHAIR T2 ");
            stb.append("        ON T2.YEAR     = T1.YEAR ");
            stb.append("       AND T2.CHAIRCD  = T1.CHAIRCD ");
            stb.append("       AND T2.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("       AND T2.CLASSCD       = T1.CLASSCD ");
            stb.append("       AND T2.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("       AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            //合格レポート番号集計
            stb.append(" ), REP AS( ");
            stb.append(" SELECT ");
            stb.append("     T1.YEAR, ");
            stb.append("     T2.SEMESTER, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.CHAIRCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T2.CLASSCD, ");
                stb.append("     T2.SCHOOL_KIND, ");
                stb.append("     T2.CURRICULUM_CD, ");
            }
            stb.append("     T2.SUBCLASSCD, ");
            stb.append("     MAX(T1.STANDARD_SEQ) AS STANDARD_SEQ, ");
            stb.append("     MAX(T1.REPRESENT_SEQ) AS REPRESENT_SEQ ");
            stb.append(" FROM ");
            stb.append("     REP_PRESENT_DAT T1 ");
            stb.append("     INNER JOIN CHAIR T2 ");
            stb.append("        ON T2.YEAR     = T1.YEAR ");
            stb.append("       AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
            stb.append(" GROUP BY ");
            stb.append("     T1.YEAR, ");
            stb.append("     T2.SEMESTER, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.CHAIRCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T2.CLASSCD, ");
                stb.append("     T2.SCHOOL_KIND, ");
                stb.append("     T2.CURRICULUM_CD, ");
            }
            stb.append("     T2.SUBCLASSCD ");
            //学期別スクーリング出席回数
            stb.append(" ), SCHOOLING AS( ");
            stb.append(" SELECT ");
            stb.append("     T1.YEAR, ");
            stb.append("     T2.SEMESTER, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.CHAIRCD, ");
            stb.append("     MAX(T1.SCHOOLING_SEQ) AS SCHOOLING_SEQ ");
            stb.append(" FROM ");
            stb.append("     SCH_ATTEND_DAT T1 ");
            stb.append("     INNER JOIN CHAIR T2 ");
            stb.append("        ON T2.YEAR     = T1.YEAR ");
            stb.append("       AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("       AND T2.CHAIRCD  = T1.CHAIRCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
            stb.append(" GROUP BY ");
            stb.append("     T1.YEAR, ");
            stb.append("     T2.SEMESTER, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.CHAIRCD ");
            //メイン
            stb.append(" )  ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     M1.CLASSCD, ");
            stb.append("     M1.SCHOOL_KIND, ");
            stb.append("     M1.CURRICULUM_CD, ");
            stb.append("     M2.CLASSNAME, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     M1.CLASSCD || M1.SCHOOL_KIND || M1.CURRICULUM_CD || M1.SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append("     M1.SUBCLASSCD, ");
            }
            stb.append("     M2.CLASSNAME, ");
            stb.append("     M3.SUBCLASSNAME, ");
            stb.append("     M4_1.VALUE AS VALUE1, ");
            stb.append("     M4_2.VALUE AS VALUE2, ");
            stb.append("     M5_1.REP_SEQ_ALL AS REP_SEQ_ALL1, ");
            stb.append("     M5_2.REP_SEQ_ALL AS REP_SEQ_ALL2, ");
            stb.append("     M6_1.STANDARD_SEQ - M6_1.REPRESENT_SEQ AS REPORT_NO1, ");
            stb.append("     M6_2.STANDARD_SEQ - M6_2.REPRESENT_SEQ AS REPORT_NO2, ");
            stb.append("     M5_1.SCH_SEQ_MIN AS SCH_SEQ_MIN1,  ");
            stb.append("     M5_2.SCH_SEQ_MIN AS SCH_SEQ_MIN2, ");
            stb.append("     M7_1.SCHOOLING_SEQ AS SCHOOLING_SEQ1, ");
            stb.append("     M7_2.SCHOOLING_SEQ AS SCHOOLING_SEQ2 ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1 ");
            stb.append("     INNER JOIN CHAIR M1 ");
            stb.append("       ON M1.YEAR     = T1.YEAR ");
            stb.append("      AND M1.SCHREGNO = T1.SCHREGNO ");
            stb.append("     LEFT JOIN CLASS_MST M2 ");
            stb.append("       ON M2.CLASSCD     = M1.CLASSCD ");
            stb.append("      AND M2.SCHOOL_KIND = M1.SCHOOL_KIND ");
            stb.append("     LEFT JOIN SUBCLASS_MST M3 ");
            stb.append("       ON M3.SUBCLASSCD    = M1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("      AND M3.CLASSCD       = M1.CLASSCD ");
                stb.append("      AND M3.SCHOOL_KIND   = M1.SCHOOL_KIND ");
                stb.append("      AND M3.CURRICULUM_CD = M1.CURRICULUM_CD ");
            }
            stb.append("     LEFT JOIN RECORD_SCORE_HIST_DAT M4_1 ");
            stb.append("       ON M4_1.YEAR          = M1.YEAR ");
            stb.append("      AND M4_1.SEMESTER      = '1' ");
            stb.append("      AND M4_1.TESTKINDCD    = '99' ");
            stb.append("      AND M4_1.TESTITEMCD    = '00' ");
            stb.append("      AND M4_1.SCORE_DIV     = '08' ");
            stb.append("      AND M4_1.SUBCLASSCD    = M1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("      AND M4_1.CLASSCD       = M1.CLASSCD ");
                stb.append("      AND M4_1.SCHOOL_KIND   = M1.SCHOOL_KIND ");
                stb.append("      AND M4_1.CURRICULUM_CD = M1.CURRICULUM_CD ");
            }
            stb.append("     LEFT JOIN RECORD_SCORE_HIST_DAT M4_2 ");
            stb.append("       ON M4_2.YEAR          = M1.YEAR ");
            stb.append("      AND M4_2.SEMESTER      = '2' ");
            stb.append("      AND M4_2.TESTKINDCD    = '99' ");
            stb.append("      AND M4_2.TESTITEMCD    = '00' ");
            stb.append("      AND M4_2.SCORE_DIV     = '08' ");
            stb.append("      AND M4_2.SUBCLASSCD    = M1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("      AND M4_2.CLASSCD       = M1.CLASSCD ");
                stb.append("      AND M4_2.SCHOOL_KIND   = M1.SCHOOL_KIND ");
                stb.append("      AND M4_2.CURRICULUM_CD = M1.CURRICULUM_CD ");
            }
            stb.append("     LEFT JOIN CORRES M5_1 ");
            stb.append("       ON M5_1.YEAR     = M1.YEAR ");
            stb.append("      AND M5_1.SEMESTER = '1' ");
            stb.append("      AND M5_1.CHAIRCD  = M1.CHAIRCD ");
            stb.append("      AND M5_1.SUBCLASSCD    = M1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("      AND M5_1.CLASSCD       = M1.CLASSCD ");
                stb.append("      AND M5_1.SCHOOL_KIND   = M1.SCHOOL_KIND ");
                stb.append("      AND M5_1.CURRICULUM_CD = M1.CURRICULUM_CD ");
            }
            stb.append("     LEFT JOIN CORRES M5_2 ");
            stb.append("       ON M5_2.YEAR     = M1.YEAR ");
            stb.append("      AND M5_2.SEMESTER = '2' ");
            stb.append("      AND M5_2.CHAIRCD  = M1.CHAIRCD ");
            stb.append("      AND M5_2.SUBCLASSCD    = M1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("      AND M5_2.CLASSCD       = M1.CLASSCD ");
                stb.append("      AND M5_2.SCHOOL_KIND   = M1.SCHOOL_KIND ");
                stb.append("      AND M5_2.CURRICULUM_CD = M1.CURRICULUM_CD ");
            }
            stb.append("     LEFT JOIN REP M6_1 ");
            stb.append("       ON M6_1.YEAR     = M1.YEAR ");
            stb.append("      AND M6_1.SEMESTER = '1' ");
            stb.append("      AND M6_1.SCHREGNO = M1.SCHREGNO ");
            stb.append("      AND M6_1.CHAIRCD  = M1.CHAIRCD ");
            stb.append("      AND M6_1.SUBCLASSCD    = M1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("      AND M6_1.CLASSCD       = M1.CLASSCD ");
                stb.append("      AND M6_1.SCHOOL_KIND   = M1.SCHOOL_KIND ");
                stb.append("      AND M6_1.CURRICULUM_CD = M1.CURRICULUM_CD ");
            }
            stb.append("     LEFT JOIN REP M6_2 ");
            stb.append("       ON M6_2.YEAR     = M1.YEAR ");
            stb.append("      AND M6_2.SEMESTER = '2' ");
            stb.append("      AND M6_2.SCHREGNO = M1.SCHREGNO ");
            stb.append("      AND M6_2.CHAIRCD  = M1.CHAIRCD ");
            stb.append("      AND M6_2.SUBCLASSCD    = M1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("      AND M6_2.CLASSCD       = M1.CLASSCD ");
                stb.append("      AND M6_2.SCHOOL_KIND   = M1.SCHOOL_KIND ");
                stb.append("      AND M6_2.CURRICULUM_CD = M1.CURRICULUM_CD ");
            }
            stb.append("     LEFT JOIN SCHOOLING M7_1 ");
            stb.append("       ON M7_1.YEAR     = M1.YEAR ");
            stb.append("      AND M7_1.SEMESTER = '1' ");
            stb.append("      AND M7_1.SCHREGNO = M1.SCHREGNO ");
            stb.append("      AND M7_1.CHAIRCD  = M1.CHAIRCD ");
            stb.append("     LEFT JOIN SCHOOLING M7_2 ");
            stb.append("       ON M7_2.YEAR     = M1.YEAR ");
            stb.append("      AND M7_2.SEMESTER = '2' ");
            stb.append("      AND M7_2.SCHREGNO = M1.SCHREGNO ");
            stb.append("      AND M7_2.CHAIRCD  = M1.CHAIRCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
            stb.append(" ORDER BY ");
            stb.append("     CLASSCD, ");
            stb.append("     SUBCLASSCD ");

            return stb.toString();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 67865 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _semester;
        final String _date;
        final String _gradeHrclass;
        final String[] _categorySelected;
        final String _certifSchoolSchoolName;
        final String _semesName;

        /** 教育課程コードを使用するか */
        final String _useCurriculumcd;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _date = request.getParameter("DATE").replace('/', '-');
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _categorySelected = request.getParameterValues("category_selected");
            _semesName = getSemesterMst(db2, "SEMESTERNAME", _semester);

            _certifSchoolSchoolName = getCertifSchoolDat(db2, "SCHOOL_NAME");

            _useCurriculumcd = request.getParameter("useCurriculumcd");

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

        private String getSemesterMst(final DB2UDB db2, final String field, final String semester) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + semester + "' ");
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

