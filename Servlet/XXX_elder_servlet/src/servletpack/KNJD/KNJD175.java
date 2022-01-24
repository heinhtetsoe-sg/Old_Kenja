// kanji=漢字
/*
 * 学校教育システム 賢者 [成績管理]  成績通知票  千代田区中学様式
 * $Id: 358a61926448635b02550748376271ec45412a23 $
 * 作成日: 2005/12/24
 * 作成者: yamashiro
 * Copyright(C) 2005-2007 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJC.KNJC053_BASE;
import servletpack.KNJC.KNJDivideAttendDate;
import servletpack.KNJZ.detail.KNJDefineCode;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJEditString;
import servletpack.KNJZ.detail.KNJObjectAbs;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 358a61926448635b02550748376271ec45412a23 $
 */
public class KNJD175 {
    private static final Log log = LogFactory.getLog(KNJD175.class);
    private static final String SUBJECT_D = "01";  //教科コード
    private static final String SUBJECT_U = "89";  //教科コード
    private static final String SUBJECT_T = "90";  //総合的な学習の時間
    private Param _param;
    private KNJObjectAbs _knjobj = new KNJEditString();                     //編集用クラス

    private KNJServletpacksvfANDdb2 _sd = new KNJServletpacksvfANDdb2();    //帳票におけるＳＶＦおよびＤＢ２の設定

    /**
     * KNJD.classから呼ばれる処理
     * @param request リクエスト
     * @param response レスポンス
     * @throws Exception I/O例外
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        final Vrw32alp svf = new Vrw32alp();    // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      // Databaseクラスを継承したクラス
        boolean nonedata = false;
        try {
            _sd.setSvfInit(request, response, svf); // print svf設定
            db2 = _sd.setDb(request);
            if (_sd.openDb(db2)) {
                log.error("db open error! ");
                return;
            }
            _param = createParam(db2, request);
            nonedata = printSvf(db2, svf);
        } finally {
            _sd.closeSvf(svf, nonedata);
            _sd.closeDb(db2);
        }
    }

    private boolean printSvf(final DB2UDB db2, final Vrw32alp svf) throws Exception {
        boolean nonedata = false;
        final Form form = setForm(db2);
        if (form.printMain(db2, svf)) {
            nonedata = true;  //SVF-FORM出力処理
        }
        return nonedata;
    }

    private Form setForm(final DB2UDB db2) throws Exception {
        if (_param._isjunior) {
            return new JuniorForm(db2);
        } else {
            return new HighSchoolForm(db2);
        }
    }

    private void printSvfHyoushiOut(
            final Vrw32alp svf,
            final ResultSet rs
    ) throws Exception {
        svf.VrSetForm("KNJD175_1.frm", 1);  //表紙
        svf.VrsOut("NENDO",        _param._gengou);           //年度
        svf.VrsOut("STAFFNAME1",   _param._principalName);    //校長名
        for (int i = 1 ; i <= _param._arrstaffname.size() ; i++) {
            svf.VrsOut("STAFFNAME" + (i + 1),   (String) _param._arrstaffname.get(i - 1)); //担任名
        }
        final String logoPass = "/usr/local/development/src/image/school_logo.bmp";
        final File logof = new File(logoPass);
        if (logof.exists()) {
            svf.VrsOut("LOGO", logoPass);
        }
        svf.VrsOut("SCHOOLNAME",   rs.getString("SCHOOL_NAME"));    //学校名
        svf.VrsOut("HR_NAME",      rs.getString("HR_NAME"));        //組名称
        svf.VrsOut("ATTENDNO",     rs.getString("ATTENDNO"));       //出席番号
        svf.VrsOut("NAME",         rs.getString("NAME"));           //生徒氏名

        printCertifData(svf, rs);
    }

    private void printCertifData(
            final Vrw32alp svf,
            final ResultSet rs
    ) throws SQLException {
        if (!_param.isPrintCertif(_param._semester, _param.getGrade())) { return; }
        final int len = KNJ_EditEdit.ret_byte_2(rs.getString("NAME"), 26);
        if (20 < len) {
            svf.VrsOut("NAME2", rs.getString("NAME"));
        } else {
            svf.VrsOut("NAME1", rs.getString("NAME"));
        }
        svf.VrsOut("BIRTHDAY", KNJ_EditDate.h_format_JP_Bth(rs.getString("BIRTHDAY")));
        final StringBuffer stb = new StringBuffer();
        if (null != rs.getString("COURSENAME")) {
            stb.append(rs.getString("COURSENAME"));
        }
        stb.append("課程");
        svf.VrsOut("COURSE", stb.toString());
        svf.VrsOut("GRADE", _param._certifGrade + "学年");
        svf.VrsOut("DATE", _param._certifDate);
        svf.VrsOut("SCHOOLNAME1", _param._schoolName);
        svf.VrsOut("JOBNAME", _param._jobName);
        svf.VrsOut("STAFFNAME", _param._principalName);
    }

    private int printItemTitle(final Vrw32alp svf, final int lineNo, final String field, final String printData) {
        svf.VrsOut(field, printData);
        return endRecordLineAdd(svf, lineNo);
    }

    /** VrEndRecordして、line+1を返す */
    private int endRecordLineAdd(final Vrw32alp svf, final int lineNo) {
        svf.VrEndRecord();
        return lineNo + 1;
    }

    private String printKantenBreak(final Vrw32alp svf, final ResultSet rs, final String div) throws SQLException {
        String viewcd = null;
        if (div.equalsIgnoreCase("1")) {
            svf.VrsOut("CLASS", rs.getString("CLASSNAME"));
            svf.VrsOut("VIEW", rs.getString("VIEWNAME"));
            viewcd = rs.getString("VIEWCD");
        }
        svf.VrsOut("SITUATION" + rs.getString("SEMESTER"), rs.getString("STATUS"));
        svf.VrsOut("ASSESS" + rs.getString("SEMESTER"), rs.getString("GRAD_VALUE"));
        return viewcd;
    }

    private void umejiPrint(final Vrw32alp svf, final int lineNo) {
        for (int i = 1; i <= 60 - lineNo; i++) {
            svf.VrAttribute("CLASS", "Meido=100");
            svf.VrsOut("CLASS", String.valueOf(i));
            svf.VrEndRecord();
        }
    }

    String prestatementRightData() {
        final StringBuffer stb = new StringBuffer();

        stb.append("SELECT * ");
        stb.append(" FROM HREPORTREMARK_DAT");
        stb.append(" WHERE YEAR = '" + _param._year + "'");
        stb.append("       AND (SEMESTER <= '" + _param._semester + "' OR SEMESTER = '9')");
        stb.append("       AND SCHREGNO = ?");
        stb.append(" ORDER BY SEMESTER");

        return stb.toString();
    }

    private String prestatementSubclassJ() {
        final StringBuffer stb = new StringBuffer();

        stb.append("WITH ");
        stb.append("SCHNO AS(");    //学籍の表
        stb.append("SELECT  SCHREGNO ");
        stb.append("FROM    SCHREG_REGD_DAT W1 ");
        stb.append("WHERE   YEAR = '" + _param._year + "' ");
        stb.append("    AND SCHREGNO = ? ");
        stb.append("    AND GRADE||HR_CLASS = '" + _param._gradeHrclass + "' ");
        stb.append("    AND SEMESTER = (SELECT  MAX(SEMESTER) ");
        stb.append("                    FROM    SCHREG_REGD_DAT W2 ");
        stb.append("                    WHERE   YEAR = '" + _param._year + "' ");
        stb.append("                        AND SEMESTER <= '" + _param._semester + "' ");
        stb.append("                        AND W2.SCHREGNO = W1.SCHREGNO) ");
        stb.append(") ");
        stb.append(",CHAIR_CLASS AS("); //受講教科の表
        stb.append("SELECT  W1.SEMESTER, SUBSTR(W2.SUBCLASSCD,1,2) AS CLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(", W2.SCHOOL_KIND ");
            stb.append(", W2.CURRICULUM_CD ");
        }
        stb.append("FROM    CHAIR_STD_DAT W1, ");
        stb.append("        CHAIR_DAT W2 ");
        stb.append("WHERE   W1.YEAR = '" + _param._year + "' ");
        stb.append("    AND W1.SEMESTER <= '" + _param._semester + "' ");
        stb.append("    AND W2.YEAR  = '" + _param._year + "' ");
        stb.append("    AND W2.SEMESTER <= '" + _param._semester + "' ");
        stb.append("    AND W2.SEMESTER = W1.SEMESTER ");
        stb.append("    AND W2.CHAIRCD = W1.CHAIRCD ");
        stb.append("    AND EXISTS(SELECT 'X' FROM SCHNO W3 WHERE W3.SCHREGNO = W1.SCHREGNO) ");
        stb.append("    AND (SUBSTR(SUBCLASSCD,1,2) BETWEEN '" + KNJDefineSchool.subject_D + "' ");
        stb.append("         AND '" + KNJDefineSchool.subject_U + "' OR SUBSTR(SUBCLASSCD,1,2) = '" + KNJDefineSchool.subject_T + "') ");
        stb.append("GROUP BY W1.SEMESTER, SUBSTR(W2.SUBCLASSCD,1,2) ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(", W2.SCHOOL_KIND ");
            stb.append(", W2.CURRICULUM_CD ");
        }
        stb.append(") ");
        stb.append(",SCH_VIEW AS(");    //観点状況の表
        stb.append("SELECT  CASE WHEN SEMESTER = '9' THEN '2' ELSE SEMESTER END AS SEMESTER,");
        stb.append("        SUBSTR(VIEWCD,1,2) AS CLASSCD, VIEWCD, STATUS ");
        stb.append("FROM    JVIEWSTAT_DAT W1 ");
        stb.append("WHERE   YEAR = '" + _param._year + "' ");
        stb.append("    AND EXISTS(SELECT 'X' FROM SCHNO W2 WHERE W2.SCHREGNO = W1.SCHREGNO) ");
        stb.append("    AND SEMESTER <= '" + _param._jviewStatSem + "' ");
        stb.append("    AND SUBSTR(VIEWCD,3,2) <> '99' ");
        stb.append(") ");
        stb.append(",SCH_VALUE AS(");   //評定の表
        stb.append("SELECT  CASE WHEN SEMESTER = '9' THEN '2' ELSE SEMESTER END AS SEMESTER,");
        stb.append("        SUBSTR(VIEWCD,1,2) AS CLASSCD, STATUS AS GRAD_VALUE ");
        stb.append("FROM    JVIEWSTAT_DAT W1 ");
        stb.append("WHERE   YEAR = '" + _param._year + "' ");
        stb.append("    AND EXISTS(SELECT 'X' FROM SCHNO W2 WHERE W2.SCHREGNO = W1.SCHREGNO) ");
        stb.append("    AND SEMESTER <= '" + _param._jviewStatSem + "' ");
        stb.append("    AND SUBSTR(VIEWCD,3,2) = '99' ");
        stb.append("), ");
        stb.append("SELECT_SUB AS(");   //メイン表
        stb.append("SELECT  T3.ELECTDIV, T5.SHOWORDER, T3.CLASSCD, T3.CLASSNAME, T4.VIEWCD, T5.VIEWNAME, T1.SEMESTER, T2.STATUS, T6.GRAD_VALUE ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(", T3.SCHOOL_KIND ");
        }
        stb.append("FROM    CHAIR_CLASS T1 ");
        stb.append("INNER   JOIN CLASS_MST T3 ON T3.CLASSCD = T1.CLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(" AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
        }
        stb.append("LEFT    JOIN JVIEWNAME_YDAT T4 ON T4.YEAR = '" + _param._year + "' AND SUBSTR(T4.VIEWCD,1,2) = T1.CLASSCD ");
        stb.append("LEFT    JOIN JVIEWNAME_MST T5 ON T5.VIEWCD = T4.VIEWCD ");
        stb.append("LEFT    JOIN SCH_VIEW T2 ON T2.SEMESTER = T1.SEMESTER AND T2.VIEWCD = T4.VIEWCD ");
        stb.append("LEFT    JOIN SCH_VALUE T6 ON T6.SEMESTER = T1.SEMESTER AND T6.CLASSCD = T1.CLASSCD ");
        stb.append("ORDER BY T3.ELECTDIV, T5.SHOWORDER, T3.CLASSCD, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(" T3.SCHOOL_KIND, ");
        }
        stb.append("T4.VIEWCD, T1.SEMESTER ");
        stb.append(") ");
        stb.append("SELECT  * ");
        stb.append("FROM    SELECT_SUB ");
        stb.append("WHERE   VIEWCD IS NOT NULL AND VIEWCD != ''  ");
        stb.append("    AND VALUE(ELECTDIV,'0') != '1' ");
        stb.append("ORDER BY ELECTDIV, SHOWORDER, CLASSCD, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(" SCHOOL_KIND, ");
        }
        stb.append("VIEWCD, SEMESTER ");

        return stb.toString();
    }

    private String prestatementSubclassH() {
        final StringBuffer stb = new StringBuffer();
        //学籍の表（クラス）
        stb.append("WITH HR_SCHNO AS(");
        stb.append("    SELECT  T2.SCHREGNO, T2.GRADE, T2.HR_CLASS, T2.ATTENDNO, ");
        stb.append("            T2.COURSECD, T2.MAJORCD, T2.COURSECODE ");
        stb.append("    FROM    SCHREG_REGD_DAT T2 ");
        stb.append("    WHERE   T2.YEAR = '" + _param._year + "' ");
        stb.append("        AND T2.GRADE = '" + _param.getGrade() + "' ");
        stb.append("        AND T2.HR_CLASS = '" + _param.getHrclass() + "' ");
        stb.append("        AND T2.SEMESTER = (SELECT  MAX(SEMESTER) ");
        stb.append("                           FROM    SCHREG_REGD_DAT W2 ");
        stb.append("                           WHERE   W2.YEAR = '" + _param._year + "' ");
        stb.append("                               AND W2.SEMESTER <= '" + _param._semester + "' ");
        stb.append("                               AND W2.SCHREGNO = T2.SCHREGNO) ");
        stb.append(") ");
        //学籍の表（生徒）
        stb.append(",SCHNO AS(");
        stb.append("    SELECT  T2.SCHREGNO, T2.GRADE, T2.HR_CLASS, T2.ATTENDNO, ");
        stb.append("            T2.COURSECD, T2.MAJORCD, T2.COURSECODE ");
        stb.append("    FROM    HR_SCHNO T2 ");
        stb.append("    WHERE   T2.SCHREGNO = ? ");
        stb.append(") ");
        //講座の表（生徒別・学期別） 講座単位ではなく科目単位に変更
        stb.append(", HR_CHAIR_A AS(");
        stb.append("    SELECT  S1.SCHREGNO,S2.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(" , S2.CLASSCD ");
            stb.append(" , S2.SCHOOL_KIND ");
            stb.append(" , S2.CURRICULUM_CD ");
        }
        stb.append("    FROM    CHAIR_STD_DAT S1, ");
        stb.append("            CHAIR_DAT S2 ");
        stb.append("    WHERE   S1.YEAR = '" + _param._year + "' ");
        stb.append("        AND S1.SEMESTER <= '" + _param._semester + "' ");
        stb.append("        AND S2.YEAR  = '" + _param._year + "' ");
        stb.append("        AND S2.SEMESTER <= '" + _param._semester + "' ");
        stb.append("        AND S2.SEMESTER = S1.SEMESTER ");
        stb.append("        AND S2.CHAIRCD = S1.CHAIRCD ");
        stb.append("        AND EXISTS( SELECT 'X' FROM HR_SCHNO S3 WHERE S3.SCHREGNO = S1.SCHREGNO) ");
        stb.append("        AND (SUBSTR(SUBCLASSCD,1,2) BETWEEN '" + SUBJECT_D + "' AND '" + SUBJECT_U + "' OR SUBSTR(SUBCLASSCD,1,2) = '" + SUBJECT_T + "') ");
        stb.append("    GROUP BY S1.SCHREGNO,S2.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(" , S2.CLASSCD ");
            stb.append(" , S2.SCHOOL_KIND ");
            stb.append(" , S2.CURRICULUM_CD ");
        }
        stb.append(") ");
        //講座の表（生徒）
        stb.append(", CHAIR_A AS(");
        stb.append("    SELECT  SCHREGNO,SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(" , CLASSCD ");
            stb.append(" , SCHOOL_KIND ");
            stb.append(" , CURRICULUM_CD ");
        }
        stb.append("    FROM    HR_CHAIR_A S1 ");
        stb.append("    WHERE   EXISTS( SELECT 'X' FROM SCHNO S3 WHERE S3.SCHREGNO = S1.SCHREGNO) ");
        stb.append(") ");
        //読替先科目の表（名簿無し）
        stb.append(", NOT_CHAIR_REPLACE AS(");
        stb.append("    SELECT  S1.GRADING_SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(" , S1.GRADING_CLASSCD ");
            stb.append(" , S1.GRADING_SCHOOL_KIND ");
            stb.append(" , S1.GRADING_CURRICULUM_CD ");
        }
        stb.append("    FROM    SUBCLASS_REPLACE_DAT S1 ");
        stb.append("    INNER JOIN CHAIR_DAT S2 ON S2.YEAR = '" + _param._year + "' ");
        stb.append("                           AND S2.SEMESTER <= '" + _param._semester + "' ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("                           AND S2.CLASSCD = S1.ATTEND_CLASSCD ");
            stb.append("                           AND S2.SCHOOL_KIND = S1.ATTEND_SCHOOL_KIND ");
            stb.append("                           AND S2.CURRICULUM_CD = S1.ATTEND_CURRICULUM_CD ");
        }
        stb.append("                           AND S2.SUBCLASSCD = S1.ATTEND_SUBCLASSCD ");
        stb.append("    WHERE   S1.YEAR = '" + _param._year + "' ");
        stb.append("        AND S1.ANNUAL = '" + _param.getGrade() + "' ");
        stb.append("        AND NOT EXISTS(SELECT  'X' FROM CHAIR_STD_DAT S3 ");
        stb.append("                       WHERE   S3.YEAR = '" + _param._year + "' ");
        stb.append("                           AND S3.SEMESTER = S2.SEMESTER ");
        stb.append("                           AND S3.CHAIRCD = S2.CHAIRCD) ");
        stb.append("    GROUP BY S1.GRADING_SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(" , S1.GRADING_CLASSCD ");
            stb.append(" , S1.GRADING_SCHOOL_KIND ");
            stb.append(" , S1.GRADING_CURRICULUM_CD ");
        }
        stb.append(") ");
        //評価読替科目（生徒）
        stb.append(", SCHREG_SUBCLASS_REPLACE AS(");
        stb.append("    SELECT  S1.GRADING_SUBCLASSCD, S1.ATTEND_SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(" , S1.GRADING_CLASSCD ");
            stb.append(" , S1.GRADING_SCHOOL_KIND ");
            stb.append(" , S1.GRADING_CURRICULUM_CD ");
            stb.append(" , S1.ATTEND_CLASSCD ");
            stb.append(" , S1.ATTEND_SCHOOL_KIND ");
            stb.append(" , S1.ATTEND_CURRICULUM_CD ");
        }
        stb.append("    FROM    SUBCLASS_REPLACE_DAT S1 ");
        stb.append("    WHERE   S1.YEAR = '" + _param._year + "' ");
        stb.append("        AND S1.ANNUAL = '" + _param.getGrade() + "' ");
        stb.append("        AND (EXISTS(SELECT  'X' FROM CHAIR_A S2 WHERE S2.SUBCLASSCD = S1.GRADING_SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("        AND S2.CLASSCD = S1.GRADING_CLASSCD ");
            stb.append("        AND S2.SCHOOL_KIND = S1.GRADING_SCHOOL_KIND ");
            stb.append("        AND S2.CURRICULUM_CD = S1.GRADING_CURRICULUM_CD ");
        }
        stb.append("                   ) ");
        stb.append("          OR EXISTS(SELECT  'X' FROM NOT_CHAIR_REPLACE S2 WHERE S2.GRADING_SUBCLASSCD = S1.GRADING_SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("        AND S2.GRADING_CLASSCD = S1.GRADING_CLASSCD ");
            stb.append("        AND S2.GRADING_SCHOOL_KIND = S1.GRADING_SCHOOL_KIND ");
            stb.append("        AND S2.GRADING_CURRICULUM_CD = S1.GRADING_CURRICULUM_CD ");
        }
        stb.append("                   ) ");
        stb.append("            ) ");
        stb.append(") ");
        //成績明細データの表
        stb.append(",RECORD AS(");
        stb.append("    SELECT  SCHREGNO, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("            CLASSCD, ");
            stb.append("            SCHOOL_KIND, ");
            stb.append("            CURRICULUM_CD, ");
        }
        stb.append("            SUBCLASSCD, ");
        stb.append("            CHAIRCD, ");
        stb.append("            COMP_CREDIT, GET_CREDIT, ADD_CREDIT, ");
        stb.append("            SEM1_INTR_SCORE, ");
        stb.append("            SEM1_INTR_VALUE, ");
        stb.append("            SEM1_TERM_SCORE, ");
        stb.append("            SEM1_TERM_VALUE, ");
        stb.append("            CASE WHEN SEM1_VALUE IS NOT NULL THEN RTRIM(CHAR(SEM1_VALUE)) ELSE SEM1_VALUE_DI END AS SEM1_VALUE, ");
        stb.append("            SEM2_INTR_SCORE, ");
        stb.append("            SEM2_INTR_VALUE, ");
        stb.append("            SEM2_TERM_SCORE, ");
        stb.append("            SEM2_TERM_VALUE, ");
        stb.append("            CASE WHEN SEM2_VALUE IS NOT NULL THEN RTRIM(CHAR(SEM2_VALUE)) ELSE SEM2_VALUE_DI END AS SEM2_VALUE, ");
        stb.append("            SEM3_INTR_SCORE, ");
        stb.append("            SEM3_INTR_VALUE, ");
        stb.append("            SEM3_TERM_SCORE, ");
        stb.append("            SEM3_TERM_VALUE, ");
        stb.append("            CASE WHEN SEM3_VALUE IS NOT NULL THEN RTRIM(CHAR(SEM3_VALUE)) ELSE SEM3_VALUE_DI END AS SEM3_VALUE, ");
        stb.append("            CASE WHEN GRAD_VALUE IS NOT NULL THEN RTRIM(CHAR(GRAD_VALUE)) ELSE GRAD_VALUE_DI END AS GRAD_VALUE ");
        stb.append("    FROM    RECORD_DAT T1 ");
        stb.append("    WHERE   YEAR = '" + _param._year + "' ");
        stb.append("        AND EXISTS(SELECT  'X' FROM SCHNO T2 WHERE T2.SCHREGNO = T1.SCHREGNO) ");
        stb.append("        AND (SUBSTR(SUBCLASSCD,1,2) BETWEEN '" + SUBJECT_D + "' AND '" + SUBJECT_U + "' OR SUBSTR(SUBCLASSCD,1,2) = '" + SUBJECT_T + "') ");
        stb.append(") ");
        // テスト項目マスタの集計フラグ
        stb.append(" , TEST_COUNTFLG AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.EXECUTEDATE, ");
        stb.append("         T1.PERIODCD, ");
        stb.append("         T1.CHAIRCD, ");
        stb.append("         '2' AS DATADIV ");
        stb.append("     FROM ");
        stb.append("         SCH_CHR_TEST T1, ");
        stb.append("         TESTITEM_MST_COUNTFLG_NEW T2 ");
        stb.append("     WHERE ");
        stb.append("         T2.YEAR       = T1.YEAR ");
        stb.append("         AND T2.SEMESTER   = T1.SEMESTER ");
        stb.append("         AND T2.TESTKINDCD = T1.TESTKINDCD ");
        stb.append("         AND T2.TESTITEMCD = T1.TESTITEMCD ");
        stb.append("         AND T2.COUNTFLG   = '0' ");
        stb.append(" ) ");
        // 時間割データの表
        stb.append(", T_SCH_CHR_DAT AS(");
        stb.append(    "SELECT  T0.SCHREGNO, T2.SUBCLASSCD, T2.SEMESTER, T1.EXECUTEDATE, T1.PERIODCD, T1.CHAIRCD, T1.DATADIV, ATDD.REP_DI_CD, ATDD.ATSUB_REPL_DI_CD, ATDD.MULTIPLY, (CASE WHEN T4.SCHREGNO IS NOT NULL THEN '1' ELSE '0' END) AS IS_OFFDAYS ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(", CLASSCD ");
            stb.append(", SCHOOL_KIND ");
            stb.append(", CURRICULUM_CD ");
        }
        stb.append(    "FROM    SCH_CHR_DAT T1 ");
        stb.append(    "INNER JOIN SEMESTER_MST T3 ON T3.YEAR = T1.YEAR ");
        stb.append(        "AND T3.SEMESTER <> '9' ");
        stb.append(        "AND T1.EXECUTEDATE BETWEEN T3.SDATE AND T3.EDATE ");
        stb.append(    "INNER JOIN CHAIR_DAT T2 ON T2.YEAR = T1.YEAR ");
        stb.append(        "AND T2.SEMESTER = T3.SEMESTER ");
        stb.append(        "AND T2.CHAIRCD = T1.CHAIRCD ");
        stb.append(    "INNER JOIN CHAIR_STD_DAT T0 ON T0.YEAR = T1.YEAR ");
        stb.append(        "AND T0.SEMESTER = T2.SEMESTER ");
        stb.append(        "AND T0.CHAIRCD = T2.CHAIRCD ");
        stb.append(        "AND T1.EXECUTEDATE BETWEEN T0.APPDATE AND T0.APPENDDATE ");
        stb.append(    "LEFT JOIN ATTEND_DAT T5 ON T5.SCHREGNO = T0.SCHREGNO ");
        stb.append(        "AND T1.EXECUTEDATE = T5.ATTENDDATE ");
        stb.append(        "AND T1.PERIODCD = T5.PERIODCD ");
        stb.append(        "AND T1.CHAIRCD = T5.CHAIRCD ");
        stb.append(    "LEFT JOIN SCHREG_TRANSFER_DAT T4 ON T4.SCHREGNO = T0.SCHREGNO ");
        stb.append(        "AND T1.EXECUTEDATE BETWEEN T4.TRANSFER_SDATE AND T4.TRANSFER_EDATE ");
        stb.append(        "AND T4.TRANSFERCD = '2' ");
        stb.append("    LEFT JOIN ATTEND_DI_CD_DAT ATDD ON ATDD.YEAR = '" + _param._year + "' ");
        stb.append("        AND ATDD.DI_CD = T5.DI_CD ");
        stb.append(    "WHERE   T1.YEAR = '" +  _param._year + "' ");
        stb.append(        "AND T1.EXECUTEDATE BETWEEN '" + _param._attendDate + "' AND '" + _param._date + "' ");
        stb.append(        "AND T3.SEMESTER <= '" + _param._semester + "' ");
        //                      学籍不在日を除外
        stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST T6 ");
        stb.append(                       "WHERE   T6.SCHREGNO = T0.SCHREGNO ");
        stb.append(                           "AND (( T6.ENT_DIV IN('4','5') AND T1.EXECUTEDATE < T6.ENT_DATE ) ");
        stb.append(                             "OR ( T6.GRD_DIV IN('2','3') AND T1.EXECUTEDATE > T6.GRD_DATE )) ) ");
        //                      留学日、休学日を除外
        stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT T7 ");
        stb.append(                       "WHERE   T7.SCHREGNO = T0.SCHREGNO ");
        stb.append(                           "AND T7.TRANSFERCD <> '2' AND T1.EXECUTEDATE BETWEEN T7.TRANSFER_SDATE AND T7.TRANSFER_EDATE ) ");
        // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
        stb.append("    AND NOT EXISTS(SELECT ");
        stb.append("                       'X' ");
        stb.append("                   FROM ");
        stb.append("                       ATTEND_DAT T4 ");
        stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT ATDD ON ATDD.YEAR = '" + _param._year + "' ");
        stb.append("                             AND ATDD.DI_CD = T4.DI_CD ");
        stb.append("                   WHERE ");
        stb.append("                       T4.SCHREGNO = T0.SCHREGNO ");
        stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
        stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
        stb.append("                       AND ATDD.REP_DI_CD = '27' ");
        stb.append("                  ) ");
        // 勤怠コード'28'は時間割にカウントしない
        stb.append("    AND NOT EXISTS(SELECT ");
        stb.append("                       'X' ");
        stb.append("                   FROM ");
        stb.append("                       ATTEND_DAT T4 ");
        stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT ATDD ON ATDD.YEAR = '" + _param._year + "' ");
        stb.append("                             AND ATDD.DI_CD = T4.DI_CD ");
        stb.append("                   WHERE ");
        stb.append("                       T4.SCHREGNO = T0.SCHREGNO ");
        stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
        stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
        stb.append("                       AND ATDD.REP_DI_CD = '28' ");
        stb.append("                  ) ");

        stb.append(") ");
        //生徒・科目・学期別欠課集計の表（出欠データと集計テーブルを合算）
        stb.append(", SCH_ATTEND_SUM AS(");
        stb.append("    SELECT  T1.SCHREGNO, T1.SUBCLASSCD, T1.SEMESTER ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(", T1.CLASSCD ");
            stb.append(", T1.SCHOOL_KIND ");
            stb.append(", T1.CURRICULUM_CD ");
        }
        stb.append(           ",SUM(CASE WHEN (CASE WHEN T1.REP_DI_CD IN ('29','30','31') THEN VALUE(T1.ATSUB_REPL_DI_CD, T1.REP_DI_CD) ELSE T1.REP_DI_CD END) IN('4','5','6','14','11','12','13'");
        if ("1".equals(_param._knjSchoolMst._subAbsent)) {
            stb.append(          ",'1','8'");
        }
        if ("1".equals(_param._knjSchoolMst._subSuspend)) {
            stb.append(          ",'2','9'");
        }
        if ("1".equals(_param._knjSchoolMst._subMourning)) {
            stb.append(          ",'3','10'");
        }
        if ("1".equals(_param._knjSchoolMst._subVirus)) {
            stb.append(          ",'19','20'");
        }
        stb.append(                ") ");
        if ("1".equals(_param._knjSchoolMst._subOffDays)) {
            stb.append(            "OR (IS_OFFDAYS = '1')");
        }
        stb.append(            " THEN 1 ELSE 0 END)AS ABSENT1 ");
        stb.append("           ,SUM(CASE WHEN T1.REP_DI_CD IN('15','16','23','24') THEN SMALLINT(VALUE(T1.MULTIPLY, '1')) ELSE 0 END)AS LATE_EARLY ");
        stb.append("    FROM T_SCH_CHR_DAT T1 ");
        stb.append("         , SCHNO T0 ");
        stb.append("    WHERE   T1.SCHREGNO = T0.SCHREGNO ");
        if (_param._defineCode.useschchrcountflg) {
            stb.append("    AND NOT EXISTS(SELECT  'X' FROM SCH_CHR_COUNTFLG T4 ");
            stb.append("                    WHERE   T4.EXECUTEDATE = T1.EXECUTEDATE ");
            stb.append("                        AND T4.PERIODCD = T1.PERIODCD ");
            stb.append("                        AND T4.CHAIRCD = T1.CHAIRCD ");
            stb.append("                        AND T1.DATADIV IN ('0', '1') ");
            stb.append("                        AND T4.GRADE = '" + _param.getGrade() + "' ");
            stb.append("                        AND T4.HR_CLASS = T0.HR_CLASS ");
            stb.append("                        AND T4.COUNTFLG = '0') ");
            stb.append("    AND NOT EXISTS(SELECT  'X' FROM TEST_COUNTFLG TEST ");
            stb.append("                       WHERE ");
            stb.append("                           TEST.EXECUTEDATE  = T1.EXECUTEDATE ");
            stb.append("                           AND TEST.PERIODCD = T1.PERIODCD ");
            stb.append("                           AND TEST.CHAIRCD  = T1.CHAIRCD ");
            stb.append("                           AND TEST.DATADIV  = T1.DATADIV) ");
        }
        stb.append("    GROUP BY T1.SCHREGNO, T1.SUBCLASSCD, T1.SEMESTER ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(", T1.CLASSCD ");
            stb.append(", T1.SCHOOL_KIND ");
            stb.append(", T1.CURRICULUM_CD ");
        }
        stb.append("    UNION ALL ");
        stb.append("    SELECT  T1.SCHREGNO, T1.SUBCLASSCD, SEMESTER ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(", T1.CLASSCD ");
            stb.append(", T1.SCHOOL_KIND ");
            stb.append(", T1.CURRICULUM_CD ");
        }
        stb.append(           ",SUM(VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) + VALUE(NURSEOFF,0)");
        if ("1".equals(_param._knjSchoolMst._subAbsent)) {
            stb.append(          "+ VALUE(ABSENT,0)");
        }
        if ("1".equals(_param._knjSchoolMst._subSuspend)) {
            stb.append(          "+ VALUE(SUSPEND,0)");
        }
        if ("1".equals(_param._knjSchoolMst._subMourning)) {
            stb.append(          "+ VALUE(MOURNING,0)");
        }
        if ("1".equals(_param._knjSchoolMst._subOffDays)) {
            stb.append(          "+ VALUE(OFFDAYS,0)");
        }
        if ("1".equals(_param._knjSchoolMst._subVirus)) {
            stb.append(          "+ VALUE(VIRUS,0)");
        }
        stb.append(               ") AS ABSENT1 ");
        stb.append("           ,SUM(VALUE(LATE,0) + VALUE(EARLY,0)) AS LATE_EARLY ");
        stb.append("    FROM    ATTEND_SUBCLASS_DAT T1 ");
        stb.append("    WHERE   YEAR = '" + _param._year + "' ");
        stb.append("        AND SEMESTER <= '" + _param._semester + "' ");
        stb.append("        AND (CASE WHEN INT(T1.MONTH) < 4 ");
        stb.append("                  THEN RTRIM(CHAR(INT(T1.SEMESTER) + 1 )) ");
        stb.append("                  ELSE T1.SEMESTER END )||T1.MONTH <= '" + KNJC053_BASE.retSemesterMonthValue(_param._attendMonth) + "' ");
        stb.append("        AND EXISTS(SELECT  'X' FROM SCHNO T2 ");
        stb.append("                   WHERE   T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("                   GROUP BY SCHREGNO) ");
        stb.append("    GROUP BY T1.SCHREGNO, T1.SEMESTER, T1.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(", T1.CLASSCD ");
            stb.append(", T1.SCHOOL_KIND ");
            stb.append(", T1.CURRICULUM_CD ");
        }
        stb.append(") ");
        //ペナルティー欠課を加味した生徒欠課集計の表（出欠データと集計テーブルを合算）
        if (_param._defineCode.absent_cov == 1 || _param._defineCode.absent_cov == 3) {
            //学期でペナルティ欠課を算出する場合
            stb.append(", ATTEND_B AS(");
            stb.append("       SELECT ");
            stb.append("           SCHREGNO, SUBCLASSCD, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(" CLASSCD, ");
                stb.append(" SCHOOL_KIND, ");
                stb.append(" CURRICULUM_CD, ");
            }
            stb.append("           VALUE(SUM(CASE WHEN SEMESTER = '1' THEN ABSENT ELSE NULL END),0) AS ABSENT_SEM1, ");
            stb.append("           VALUE(SUM(CASE WHEN SEMESTER = '2' THEN ABSENT ELSE NULL END),0) AS ABSENT_SEM2, ");
            stb.append("           VALUE(SUM(CASE WHEN SEMESTER = '3' THEN ABSENT ELSE NULL END),0) AS ABSENT_SEM3, ");
            stb.append("           VALUE(SUM(ABSENT),0) AS ABSENT_SEM9 ");
            stb.append("       FROM(   SELECT  SCHREGNO, SUBCLASSCD, SEMESTER ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(", CLASSCD ");
                stb.append(", SCHOOL_KIND ");
                stb.append(", CURRICULUM_CD ");
            }
            stb.append("                      ,VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / " + _param._defineCode.absent_cov_late + " AS ABSENT ");
            stb.append("               FROM    SCH_ATTEND_SUM T1 ");
            stb.append("               GROUP BY SCHREGNO, SUBCLASSCD, SEMESTER ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(", CLASSCD ");
                stb.append(", SCHOOL_KIND ");
                stb.append(", CURRICULUM_CD ");
            }
            stb.append("           )T1 ");
            stb.append("       GROUP BY SCHREGNO, SUBCLASSCD ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(", CLASSCD ");
                stb.append(", SCHOOL_KIND ");
                stb.append(", CURRICULUM_CD ");
            }
            stb.append(") ");
        } else if (_param._defineCode.absent_cov == 2 || _param._defineCode.absent_cov == 4) {
            //通年でペナルティ欠課を算出する場合
            //学期の欠課時数は学期別で換算したペナルティ欠課を加算、学年の欠課時数は年間で換算する
            stb.append(", ATTEND_B AS(");
            stb.append("       SELECT  T1.SCHREGNO, T1.SUBCLASSCD, T1.ABSENT_SEM9 ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(", T1.CLASSCD ");
                stb.append(", T1.SCHOOL_KIND ");
                stb.append(", T1.CURRICULUM_CD ");
            }
            stb.append("              ,T2.ABSENT_SEM1, T2.ABSENT_SEM2, T2.ABSENT_SEM3 ");
            stb.append("       FROM (");
            stb.append("            SELECT  SCHREGNO, SUBCLASSCD ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(", CLASSCD ");
                stb.append(", SCHOOL_KIND ");
                stb.append(", CURRICULUM_CD ");
            }
            stb.append("                   ,VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / " + _param._defineCode.absent_cov_late + " AS ABSENT_SEM9 ");
            stb.append("            FROM    SCH_ATTEND_SUM T1 ");
            stb.append("            GROUP BY SCHREGNO, SUBCLASSCD ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(", CLASSCD ");
                stb.append(", SCHOOL_KIND ");
                stb.append(", CURRICULUM_CD ");
            }
            stb.append("       )T1, (");
            stb.append("            SELECT  SCHREGNO, SUBCLASSCD ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(", CLASSCD ");
                stb.append(", SCHOOL_KIND ");
                stb.append(", CURRICULUM_CD ");
            }
            stb.append("                   ,VALUE(SUM(CASE WHEN SEMESTER = '1' THEN ABSENT ELSE NULL END),0) AS ABSENT_SEM1 ");
            stb.append("                   ,VALUE(SUM(CASE WHEN SEMESTER = '2' THEN ABSENT ELSE NULL END),0) AS ABSENT_SEM2 ");
            stb.append("                   ,VALUE(SUM(CASE WHEN SEMESTER = '3' THEN ABSENT ELSE NULL END),0) AS ABSENT_SEM3 ");
            stb.append("            FROM(   SELECT  SCHREGNO, SUBCLASSCD, SEMESTER ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(", CLASSCD ");
                stb.append(", SCHOOL_KIND ");
                stb.append(", CURRICULUM_CD ");
            }
            stb.append("                           ,VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / " + _param._defineCode.absent_cov_late + " AS ABSENT ");
            stb.append("                    FROM    SCH_ATTEND_SUM T1 ");
            stb.append("                    GROUP BY SCHREGNO, SUBCLASSCD, SEMESTER ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(", CLASSCD ");
                stb.append(", SCHOOL_KIND ");
                stb.append(", CURRICULUM_CD ");
            }
            stb.append("                )T1 ");
            stb.append("            GROUP BY SCHREGNO, SUBCLASSCD ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(", CLASSCD ");
                stb.append(", SCHOOL_KIND ");
                stb.append(", CURRICULUM_CD ");
            }
            stb.append("       )T2 ");
            stb.append("       WHERE T1.SCHREGNO = T2.SCHREGNO AND T1.SUBCLASSCD = T2.SUBCLASSCD ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(" AND T1.CLASSCD = T2.CLASSCD ");
                stb.append(" AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ");
                stb.append(" AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ");
            }
            stb.append(") ");
        } else {
            //ペナルティ欠課なしの場合
            stb.append(", ATTEND_B AS(");
            stb.append("       SELECT  SCHREGNO, SUBCLASSCD ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(", CLASSCD ");
                stb.append(", SCHOOL_KIND ");
                stb.append(", CURRICULUM_CD ");
            }
            stb.append("              ,VALUE(SUM(CASE WHEN SEMESTER = '1' THEN ABSENT1 ELSE NULL END),0) AS ABSENT_SEM1 ");
            stb.append("              ,VALUE(SUM(CASE WHEN SEMESTER = '2' THEN ABSENT1 ELSE NULL END),0) AS ABSENT_SEM2 ");
            stb.append("              ,VALUE(SUM(CASE WHEN SEMESTER = '3' THEN ABSENT1 ELSE NULL END),0) AS ABSENT_SEM3 ");
            stb.append("              ,VALUE(SUM(ABSENT1),0) AS ABSENT_SEM9 ");
            stb.append("       FROM    SCH_ATTEND_SUM T1 ");
            stb.append("       GROUP BY SCHREGNO, SUBCLASSCD ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(", CLASSCD ");
                stb.append(", SCHOOL_KIND ");
                stb.append(", CURRICULUM_CD ");
            }
            stb.append(") ");
        }
        //読替先のペナルティー欠課を加味した生徒欠課集計の表
        stb.append(", ATTEND_B_REPLACE AS(");
        stb.append("    SELECT  S2.SCHREGNO, S1.GRADING_SUBCLASSCD AS SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(", S1.GRADING_CLASSCD AS CLASSCD ");
            stb.append(", S1.GRADING_SCHOOL_KIND AS SCHOOL_KIND ");
            stb.append(", S1.GRADING_CURRICULUM_CD AS CURRICULUM_CD ");
        }
        stb.append("           ,SUM(S2.ABSENT_SEM1) AS ABSENT_SEM1 ");
        stb.append("           ,SUM(S2.ABSENT_SEM2) AS ABSENT_SEM2 ");
        stb.append("           ,SUM(S2.ABSENT_SEM3) AS ABSENT_SEM3 ");
        stb.append("           ,SUM(S2.ABSENT_SEM9) AS ABSENT_SEM9 ");
        stb.append("    FROM    SUBCLASS_REPLACE_DAT S1, ATTEND_B S2 ");
        stb.append("    WHERE   S1.ATTEND_SUBCLASSCD = S2.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("        AND S1.ATTEND_CLASSCD = S2.CLASSCD ");
            stb.append("        AND S1.ATTEND_SCHOOL_KIND = S2.SCHOOL_KIND ");
            stb.append("        AND S1.ATTEND_CURRICULUM_CD = S2.CURRICULUM_CD ");
        }
        stb.append("        AND S1.YEAR = '" + _param._year + "' ");
        stb.append("        AND S1.ANNUAL = '" + _param.getGrade() + "' ");
        stb.append("    GROUP BY S2.SCHREGNO, S1.GRADING_SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(", S1.GRADING_CLASSCD ");
            stb.append(", S1.GRADING_SCHOOL_KIND ");
            stb.append(", S1.GRADING_CURRICULUM_CD ");
        }
        stb.append(") ");
        //無条件履修修得フラグがオンの科目の表
        stb.append(",CREDITS_UNCONDITION AS(");
        stb.append("  SELECT  SCHREGNO, SUBCLASSCD, CREDITS");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(", CLASSCD ");
            stb.append(", SCHOOL_KIND ");
            stb.append(", CURRICULUM_CD ");
        }
        stb.append("  FROM    CREDIT_MST T1, SCHNO T2");
        stb.append("  WHERE   T1.YEAR = '" + _param._year + "'");
        stb.append("      AND T1.GRADE = T2.GRADE");
        stb.append("      AND T1.COURSECD = T2.COURSECD");
        stb.append("      AND T1.MAJORCD = T2.MAJORCD");
        stb.append("      AND T1.COURSECODE = T2.COURSECODE");
        stb.append("      AND VALUE(T1.COMP_UNCONDITION_FLG,'0') = '1'");
        stb.append("      AND NOT EXISTS(SELECT 'X' FROM RECORD T3 WHERE T3.SUBCLASSCD = T1.SUBCLASSCD AND T3.SCHREGNO = T2.SCHREGNO ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(" AND T3.CLASSCD = T1.CLASSCD ");
            stb.append(" AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append(" AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append(                    ") ");
        stb.append(") ");
        //科目数カウント
        stb.append(",SUBCLASSNUM AS(");
        stb.append("    SELECT  SCHREGNO ");
        stb.append("           ,SUM(CASE WHEN SUBSTR(SUBCLASSCD,1,2) = '" + SUBJECT_T + "' THEN 1 ELSE NULL END) AS NUM90 ");
        stb.append("           ,SUM(CASE WHEN SUBSTR(SUBCLASSCD,1,2) != '" + SUBJECT_T + "' THEN 1 ELSE NULL END) AS NUMTOTAL ");
        stb.append("    FROM (");
        stb.append("          SELECT  S1.SCHREGNO, S1.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(", S1.CLASSCD ");
            stb.append(", S1.SCHOOL_KIND ");
            stb.append(", S1.CURRICULUM_CD ");
        }
        stb.append("          FROM    CHAIR_A S1 ");
        stb.append("          WHERE   EXISTS(SELECT 'X' FROM SCHNO S3 WHERE S3.SCHREGNO = S1.SCHREGNO) ");
        stb.append("              AND EXISTS(SELECT 'X' FROM RECORD S2 WHERE S2.SCHREGNO = S1.SCHREGNO AND S2.SUBCLASSCD = S1.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(" AND S2.CLASSCD = S1.CLASSCD ");
            stb.append(" AND S2.SCHOOL_KIND = S1.SCHOOL_KIND ");
            stb.append(" AND S2.CURRICULUM_CD = S1.CURRICULUM_CD ");
        }
        stb.append(                    ") ");
        stb.append("          UNION ");
                              //受講名簿が１件もない場合の評価読替科目
        stb.append("          SELECT  SCHREGNO, GRADING_SUBCLASSCD AS SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(", GRADING_CLASSCD AS CLASSCD ");
            stb.append(", GRADING_SCHOOL_KIND AS SCHOOL_KIND ");
            stb.append(", GRADING_CURRICULUM_CD AS CURRICULUM_CD ");
        }
        stb.append("          FROM    NOT_CHAIR_REPLACE S1, SCHNO S2 ");
        //無条件履修取得の科目
        stb.append("      UNION");
        stb.append("      SELECT  SCHREGNO, SUBCLASSCD");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(", CLASSCD ");
            stb.append(", SCHOOL_KIND ");
            stb.append(", CURRICULUM_CD ");
        }
        stb.append("      FROM    CREDITS_UNCONDITION S1");
        stb.append("          )T1 ");
        stb.append("    GROUP BY SCHREGNO ");
        stb.append(") ");
        //メイン表
//        stb.append(", MAIN_T AS ( ");
        stb.append("SELECT  T2.ATTENDNO, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("        T5.CLASSCD || T5.SCHOOL_KIND || T5.CURRICULUM_CD || T5.SUBCLASSCD AS SUBCLASSCD, ");
        } else {
            stb.append("        T5.SUBCLASSCD, ");
        }
        stb.append("        T4.SUBCLASSNAME, T7.CLASSCD, T7.CLASSNAME, ");
        stb.append("        T6.CREDITS, T6_N.NAME1 AS REQUIRE, ");
        stb.append("        CASE WHEN VALUE(T6.COMP_UNCONDITION_FLG,'0') = '1' AND T1.COMP_CREDIT IS NULL ");
        stb.append("             THEN T6.CREDITS ");
        stb.append("             ELSE T1.COMP_CREDIT END AS COMP_CREDIT, ");
        stb.append("        CASE WHEN VALUE(T6.COMP_UNCONDITION_FLG,'0') = '1' AND T1.GET_CREDIT IS NULL ");
        stb.append("             THEN T6.CREDITS ");
        stb.append("             ELSE T1.GET_CREDIT END AS GET_CREDIT, ");
        stb.append("        T1.ADD_CREDIT, ");
        stb.append("        T1.COMP_CREDIT AS ON_RECORD_COMP, ");
        stb.append("        T1.GET_CREDIT AS ON_RECORD_GET, ");
        stb.append("        VALUE(T6.COMP_UNCONDITION_FLG,'0')AS COMP_UNCONDITION_FLG, ");  //値が'1'の場合無条件に単位を与える
        stb.append("        SEM1_INTR_SCORE, SEM1_INTR_VALUE, ");
        stb.append("        SEM1_TERM_SCORE, SEM1_TERM_VALUE, ");
        stb.append("        SEM1_VALUE, ");
        stb.append("        SEM2_INTR_SCORE, SEM2_INTR_VALUE, ");
        stb.append("        SEM2_TERM_SCORE, SEM2_TERM_VALUE, ");
        stb.append("        SEM2_VALUE, ");
        stb.append("        SEM3_INTR_SCORE, SEM3_INTR_VALUE, ");
        stb.append("        SEM3_TERM_SCORE, SEM3_TERM_VALUE, ");
        stb.append("        SEM3_VALUE, ");
        stb.append("        CASE WHEN SUBSTR(T1.SUBCLASSCD,1,2) <> '" + SUBJECT_T + "' THEN T1.GRAD_VALUE ");
        stb.append("             WHEN T1.GRAD_VALUE IN('-','=') THEN T1.GRAD_VALUE ELSE ");
        stb.append("             (SELECT  S1.ASSESSMARK FROM ASSESS_MST S1 ");
        stb.append("              WHERE   S1.ASSESSCD = '3' ");
        stb.append("                  AND S1.ASSESSLEVEL = INT(T1.GRAD_VALUE))END AS GRAD_VALUE, ");
        stb.append("        CASE WHEN T8.ABSENT_SEM1 IS NOT NULL THEN T8.ABSENT_SEM1 ELSE T3.ABSENT_SEM1 END ABSENT_SEM1, ");
        stb.append("        CASE WHEN T8.ABSENT_SEM2 IS NOT NULL THEN T8.ABSENT_SEM2 ELSE T3.ABSENT_SEM2 END ABSENT_SEM2, ");
        stb.append("        CASE WHEN T8.ABSENT_SEM3 IS NOT NULL THEN T8.ABSENT_SEM3 ELSE T3.ABSENT_SEM3 END ABSENT_SEM3, ");
        stb.append("        CASE WHEN T8.ABSENT_SEM9 IS NOT NULL THEN T8.ABSENT_SEM9 ELSE T3.ABSENT_SEM9 END ABSENT_SEM9, ");
        stb.append("        T3.ABSENT_SEM9 AS ABSENT_TOTAL ");
        stb.append("       ,T5.REPLACEFLG, T8.ABSENT_SEM9 AS REPLACE_ABSENT_TOTAL ");
        stb.append("       ,VALUE(T9.NUM90,0) AS NUM90, VALUE(T9.NUMTOTAL,0) AS NUMTOTAL ");
        stb.append("FROM   SCHNO T2 ");
        stb.append("LEFT JOIN (");
                        //任意の生徒が受講している科目
        stb.append("    SELECT  S1.SCHREGNO, S1.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("        , S1.CLASSCD ");
            stb.append("        , S1.SCHOOL_KIND ");
            stb.append("        , S1.CURRICULUM_CD ");
        }
        stb.append("           ,CASE WHEN S2.SUBCLASSCD IS NOT NULL THEN 9 ");
        stb.append("                 WHEN S4.SUBCLASSCD IS NOT NULL THEN 2 ");
        stb.append("                 WHEN S3.SUBCLASSCD IS NOT NULL THEN 1 ");
        stb.append("                 ELSE 0 END AS REPLACEFLG ");
        stb.append("    FROM    CHAIR_A S1 ");
        stb.append("    LEFT JOIN(");
                            //評価読替科目
        stb.append("        SELECT  S1.GRADING_SUBCLASSCD AS SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("        , S1.GRADING_CLASSCD AS CLASSCD ");
            stb.append("        , S1.GRADING_SCHOOL_KIND AS SCHOOL_KIND ");
            stb.append("        , S1.GRADING_CURRICULUM_CD AS CURRICULUM_CD ");
        }
        stb.append("        FROM    SCHREG_SUBCLASS_REPLACE S1 ");
        stb.append("        GROUP BY S1.GRADING_SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("        , S1.GRADING_CLASSCD ");
            stb.append("        , S1.GRADING_SCHOOL_KIND ");
            stb.append("        , S1.GRADING_CURRICULUM_CD ");
        }
        stb.append("    )S2 ON S2.SUBCLASSCD = S1.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("        AND S2.CLASSCD = S1.CLASSCD ");
            stb.append("        AND S2.SCHOOL_KIND = S1.SCHOOL_KIND ");
            stb.append("        AND S2.CURRICULUM_CD = S1.CURRICULUM_CD ");
        }
        stb.append("    LEFT JOIN(");
                            //評価読替元の科目
        stb.append("        SELECT  S1.ATTEND_SUBCLASSCD AS SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("        , S1.ATTEND_CLASSCD AS CLASSCD ");
            stb.append("        , S1.ATTEND_SCHOOL_KIND AS SCHOOL_KIND ");
            stb.append("        , S1.ATTEND_CURRICULUM_CD AS CURRICULUM_CD ");
        }
        stb.append("        FROM    SCHREG_SUBCLASS_REPLACE S1 ");
        stb.append("        GROUP BY S1.ATTEND_SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("        , S1.ATTEND_CLASSCD ");
            stb.append("        , S1.ATTEND_SCHOOL_KIND ");
            stb.append("        , S1.ATTEND_CURRICULUM_CD ");
        }
        stb.append("    )S3 ON S3.SUBCLASSCD = S1.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("        AND S3.CLASSCD = S1.CLASSCD ");
            stb.append("        AND S3.SCHOOL_KIND = S1.SCHOOL_KIND ");
            stb.append("        AND S3.CURRICULUM_CD = S1.CURRICULUM_CD ");
        }
        stb.append("    LEFT JOIN(");
                            //評価読替科目に学年評定がある評価読替元の科目 => 学年の値は非表示のため抽出
        stb.append("        SELECT  S1.ATTEND_SUBCLASSCD AS SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("        , S1.ATTEND_CLASSCD AS CLASSCD ");
            stb.append("        , S1.ATTEND_SCHOOL_KIND AS SCHOOL_KIND ");
            stb.append("        , S1.ATTEND_CURRICULUM_CD AS CURRICULUM_CD ");
        }
        stb.append("        FROM    SCHREG_SUBCLASS_REPLACE S1 ");
        stb.append("        WHERE   EXISTS(SELECT  'X' FROM RECORD_DAT S2 ");
        stb.append("                       WHERE   S2.SUBCLASSCD = S1.GRADING_SUBCLASSCD AND S2.GRAD_VALUE IS NOT NULL ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("        AND S2.CLASSCD = S1.GRADING_CLASSCD ");
            stb.append("        AND S2.SCHOOL_KIND = S1.GRADING_SCHOOL_KIND ");
            stb.append("        AND S2.CURRICULUM_CD = S1.GRADING_CURRICULUM_CD ");
        }
        stb.append("                       GROUP BY S2.SUBCLASSCD) ");
        stb.append("        GROUP BY S1.ATTEND_SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("        , S1.ATTEND_CLASSCD ");
            stb.append("        , S1.ATTEND_SCHOOL_KIND ");
            stb.append("        , S1.ATTEND_CURRICULUM_CD ");
        }
        stb.append("    )S4 ON S4.SUBCLASSCD = S1.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("        AND S4.CLASSCD = S1.CLASSCD ");
            stb.append("        AND S4.SCHOOL_KIND = S1.SCHOOL_KIND ");
            stb.append("        AND S4.CURRICULUM_CD = S1.CURRICULUM_CD ");
        }
        stb.append("    WHERE   EXISTS(SELECT 'X' FROM SCHNO S3 WHERE S3.SCHREGNO = S1.SCHREGNO) ");
        stb.append("        AND EXISTS(SELECT 'X' FROM RECORD S2 WHERE S2.SCHREGNO = S1.SCHREGNO AND S2.SUBCLASSCD = S1.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("        AND S2.CLASSCD = S1.CLASSCD ");
            stb.append("        AND S2.SCHOOL_KIND = S1.SCHOOL_KIND ");
            stb.append("        AND S2.CURRICULUM_CD = S1.CURRICULUM_CD ");
        }
        stb.append(                    ") ");
        stb.append("    UNION ");
                        //受講名簿が１件もない場合の評価読替科目
        stb.append("    SELECT  SCHREGNO, GRADING_SUBCLASSCD AS SUBCLASSCD, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("        GRADING_CLASSCD AS CLASSCD, ");
            stb.append("        GRADING_SCHOOL_KIND AS SCHOOL_KIND, ");
            stb.append("        GRADING_CURRICULUM_CD AS CURRICULUM_CD, ");
        }
        stb.append("        9 AS REPLACEFLG ");
        stb.append("    FROM    NOT_CHAIR_REPLACE S1, SCHNO S2 ");

        stb.append(" UNION");
        stb.append(" SELECT  SCHREGNO, SUBCLASSCD, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("        CLASSCD, ");
            stb.append("        SCHOOL_KIND, ");
            stb.append("        CURRICULUM_CD, ");
        }
        stb.append("        0 AS REPLACEFLG ");
        stb.append(" FROM    CREDITS_UNCONDITION S1");
        stb.append(")T5 ON T5.SCHREGNO = T2.SCHREGNO ");
        stb.append("LEFT JOIN RECORD T1 ON T1.SCHREGNO = T5.SCHREGNO AND T1.SUBCLASSCD = T5.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("        AND T1.CLASSCD = T5.CLASSCD ");
            stb.append("        AND T1.SCHOOL_KIND = T5.SCHOOL_KIND ");
            stb.append("        AND T1.CURRICULUM_CD = T5.CURRICULUM_CD ");
        }
        stb.append("LEFT JOIN ATTEND_B T3 ON T3.SCHREGNO = T5.SCHREGNO AND T3.SUBCLASSCD = T5.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("        AND T3.CLASSCD = T5.CLASSCD ");
            stb.append("        AND T3.SCHOOL_KIND = T5.SCHOOL_KIND ");
            stb.append("        AND T3.CURRICULUM_CD = T5.CURRICULUM_CD ");
        }
        stb.append("LEFT JOIN ATTEND_B_REPLACE T8 ON T8.SCHREGNO = T5.SCHREGNO AND T8.SUBCLASSCD = T5.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("        AND T8.CLASSCD = T5.CLASSCD ");
            stb.append("        AND T8.SCHOOL_KIND = T5.SCHOOL_KIND ");
            stb.append("        AND T8.CURRICULUM_CD = T5.CURRICULUM_CD ");
        }
        stb.append("LEFT JOIN SUBCLASS_MST T4 ON T4.SUBCLASSCD = T5.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("        AND T4.CLASSCD = T5.CLASSCD ");
            stb.append("        AND T4.SCHOOL_KIND = T5.SCHOOL_KIND ");
            stb.append("        AND T4.CURRICULUM_CD = T5.CURRICULUM_CD ");
        }
        stb.append("LEFT JOIN CLASS_MST T7 ON T7.CLASSCD = SUBSTR(T5.SUBCLASSCD,1,2) ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("        AND T7.SCHOOL_KIND = T5.SCHOOL_KIND ");
        }
        stb.append("LEFT JOIN CREDIT_MST T6 ON T6.YEAR = '" + _param._year + "' ");
        stb.append("                       AND T6.SUBCLASSCD = T5.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("        AND T6.CLASSCD = T5.CLASSCD ");
            stb.append("        AND T6.SCHOOL_KIND = T5.SCHOOL_KIND ");
            stb.append("        AND T6.CURRICULUM_CD = T5.CURRICULUM_CD ");
        }
        stb.append("                       AND T6.GRADE = T2.GRADE ");
        stb.append("                       AND T6.COURSECD = T2.COURSECD ");
        stb.append("                       AND T6.MAJORCD = T2.MAJORCD ");
        stb.append("                       AND T6.COURSECODE = T2.COURSECODE ");
        stb.append("LEFT JOIN NAME_MST T6_N ON T6_N.NAMECD1 = 'Z011' ");
        stb.append("                       AND T6_N.NAMECD2 = T6.REQUIRE_FLG ");
        stb.append("LEFT JOIN SUBCLASSNUM T9 ON T9.SCHREGNO = T5.SCHREGNO ");
        stb.append("WHERE ");
        stb.append("    T5.REPLACEFLG NOT IN (1, 2) ");
        stb.append("ORDER BY T2.ATTENDNO, T5.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("        , T5.CLASSCD ");
            stb.append("        , T5.SCHOOL_KIND ");
            stb.append("        , T5.CURRICULUM_CD ");
        }

        return stb.toString();
    }

    private String getRisyuSyutokuCredit() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCH_R AS ( ");
        stb.append(" SELECT ");
        stb.append("     SCHREGNO, ");
        stb.append("     GRADE, ");
        stb.append("     MAX(YEAR) AS YEAR ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT ");
        stb.append(" WHERE ");
        stb.append("     SCHREGNO = ? ");
        if (_param._semester.equals("1")) {
            stb.append("     AND YEAR < '" + _param._year + "' ");
        }
        stb.append("     AND GRADE > '03' ");
        stb.append(" GROUP BY ");
        stb.append("     SCHREGNO, ");
        stb.append("     GRADE ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.YEAR, ");
        stb.append("     SUM(VALUE(L1.GET_CREDIT, 0)) AS GET_CREDIT ");
        stb.append(" FROM ");
        stb.append("     SCH_R T1 ");
        stb.append("     LEFT JOIN SCHREG_STUDYREC_DAT L1 ON L1.YEAR = T1.YEAR ");
        stb.append("          AND L1.SCHREGNO = T1.SCHREGNO ");
        stb.append(" GROUP BY ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.YEAR ");
        return stb.toString();
    }

    private void prestatementClose(final PreparedStatement[] arrps) throws Exception {
        for (int i = 0; i < arrps.length; i++) {
            DbUtils.closeQuietly(arrps[i]);
        }
    }

    /**
     * abstractクラス。
     * 中学、高校で処理が異なる。
     * @author m-yama
     * @version $Id: 358a61926448635b02550748376271ec45412a23 $
     */
    private abstract class Form {
        final PreparedStatement[] _arrps = new PreparedStatement[6];
        boolean _nonedata;
        String prestatementRegd() {
            final StringBuffer stb = new StringBuffer();
            //異動者を除外した学籍の表 => 組平均において異動者の除外に使用
            stb.append("WITH SCHNO_A AS(");
            stb.append("SELECT  T1.SCHREGNO, T1.ATTENDNO, T1.SEMESTER, T1.COURSECD, T1.MAJORCD ");
            stb.append("FROM    SCHREG_REGD_DAT T1,SEMESTER_MST T2 ");
            stb.append("WHERE   T1.YEAR = '" + _param._year + "' ");
            stb.append("   AND T1.SEMESTER = '" + _param._semester + "' ");
            stb.append("   AND T1.YEAR = T2.YEAR ");
            stb.append("   AND T1.SEMESTER = T2.SEMESTER ");
            stb.append("   AND T1.GRADE||T1.HR_CLASS = '" + _param._gradeHrclass + "' ");
            stb.append("   AND T1.SCHREGNO IN" + _param._schnoInState + " ");
            //在籍チェック:転学(2)・退学(3)者は除外 但し異動日が学期終了日または異動基準日より小さい場合
            //転入(4)・編入(5)者は除外 但し異動日が学期終了日または異動基準日より大きい場合
            stb.append("   AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
            stb.append("                  WHERE ");
            stb.append("                       S1.SCHREGNO = T1.SCHREGNO ");
            stb.append("                       AND ((S1.GRD_DIV IN('2','3') AND ");
            stb.append("                             S1.GRD_DATE < CASE WHEN T2.EDATE < '" + _param._date + "' ");
            stb.append("                                                THEN T2.EDATE ");
            stb.append("                                                ELSE '" + _param._date + "' END) ");
            stb.append("                            OR (S1.ENT_DIV IN('4','5') ");
            stb.append("                                AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + _param._date + "' ");
            stb.append("                                                       THEN T2.EDATE ");
            stb.append("                                                       ELSE '" + _param._date + "' END)");
            stb.append("                            )) ");
            //異動者チェック：留学(1)・休学(2)者は除外 但し学期終了日または基準日が異動開始日と終了日内にある場合
            stb.append("   AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
            stb.append("                  WHERE ");
            stb.append("                      S1.SCHREGNO = T1.SCHREGNO ");
            stb.append("                      AND S1.TRANSFERCD IN ('1','2') ");
            stb.append("                      AND CASE WHEN T2.EDATE < '" + _param._date + "' ");
            stb.append("                               THEN T2.EDATE ");
            stb.append("                               ELSE '" + _param._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
            stb.append("   ) ");
            stb.append(getRegdMainSql());   //メイン表

            return stb.toString();
        }

        void printSvfAttend(
                final Vrw32alp svf,
                final PreparedStatement ps,
                final String schregno,
                final int sem
        ) throws Exception {
            ResultSet rs = null;
            try {
                int pp = 1;
                ps.setString(pp++, schregno);   //生徒番号
                rs = ps.executeQuery();
                while (rs.next()) {
                    printSvfAttendOut(svf, rs);
                }
            } finally {
                DbUtils.closeQuietly(rs);
            }
        }

        void printSvfSchInfo(final Vrw32alp svf, final ResultSet rs, final String formName) throws SQLException {
            svf.VrSetForm(formName, 4);  //通知表
            svf.VrsOut("NENDO",        _param._gengou);  //年度
            svf.VrsOut("HR_NAME",      rs.getString("HR_NAME"));        //組名称
            svf.VrsOut("ATTENDNO",     rs.getString("ATTENDNO"));       //出席番号
            svf.VrsOut("NAME",         rs.getString("NAME"));           //生徒氏名
        }
        abstract boolean printMain(final DB2UDB db2, final Vrw32alp svf) throws Exception;
        abstract String getRegdMainSql();
        abstract void printSvfAttendOut(final Vrw32alp svf, final ResultSet rs) throws Exception;
    }

    /** 中学用 */
    private class JuniorForm extends Form {

        public JuniorForm(final DB2UDB db2) throws Exception {
            _arrps[0] = db2.prepareStatement(prestatementRegd());         //学籍データ
            _arrps[1] = db2.prepareStatement(prestatementRightData());    //帳票右側データ
            _arrps[2] = db2.prepareStatement(prestatementSubclassJ());    //成績明細データ
            
            String prestatementAttendSemes = AttendAccumulate.getAttendSemesSql(
                    _param._semesFlg,
                    _param._defineCode,
                    _param._knjSchoolMst,
                    _param._year,
                    _param.SSEMESTER,
                    _param._semester,
                    (String) _param._hasuuMap.get("attendSemesInState"),
                    _param._periodInState,
                    (String) _param._hasuuMap.get("befDayFrom"),
                    (String) _param._hasuuMap.get("befDayTo"),
                    (String) _param._hasuuMap.get("aftDayFrom"),
                    (String) _param._hasuuMap.get("aftDayTo"),
                    _param._gradeHrclass.substring(0, 2),
                    _param._gradeHrclass.substring(2, 5),
                    "?",
                    "SEMESTER",
                    _param._useCurriculumcd,
                    _param._useVirus,
                    _param._useKoudome
            );
            log.debug("presatementAttendSemes sql=" + prestatementAttendSemes);
            _arrps[3] = db2.prepareStatement(prestatementAttendSemes);       //出欠データ
        }

        boolean printMain(final DB2UDB db2, final Vrw32alp svf) throws Exception {
            ResultSet rs1 = null;
            try {
                rs1 = _arrps[0].executeQuery();          //学籍データ
                while (rs1.next()) {
                    if (!_param._printDiv.equals("Back")) {
                        printSvfHyoushiOut(svf, rs1);   //表紙を出力
                        svf.VrEndPage();
                    }
                    if (!_param._printDiv.equals("Front")) {
                        printSvfRegdOut(db2, svf, rs1, _arrps[1]);  //学籍情報等を出力
                        printSvfAttend(svf, _arrps[3], rs1.getString("SCHREGNO"), Integer.parseInt(_param._semester));  //出欠データを出力
                        //成績明細データを出力(SVF-FORMへ出力）
                        if (printSvfRecDetail(svf, _arrps[2], rs1.getString("SCHREGNO"))) {
                            _nonedata = true;
                        }
                    }
                    _nonedata = true;
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(rs1);
                prestatementClose(_arrps); //preparestatementを閉じる
            }
            return _nonedata;
        }

        String getRegdMainSql() {
            final StringBuffer stb = new StringBuffer();

            stb.append("SELECT  T1.SCHREGNO, T1.ATTENDNO, T2.HR_NAME, ");
            stb.append("        T5.NAME, T3.COURSENAME, T4.MAJORNAME, ");
            stb.append("        T6.GUARD_NAME, T6.GUARD_ADDR1, T6.GUARD_ADDR2, T6.GUARD_ZIPCD, ");
            stb.append("        T7.TOTALSTUDYACT, T7.TOTALSTUDYVAL, T7.ATTENDREC_REMARK, T7.VIEWREMARK, ");
            stb.append("        (select ");
            stb.append("             SCHOOL_NAME ");
            stb.append("         from ");
            stb.append("             CERTIF_SCHOOL_DAT ");
            stb.append("         where ");
            stb.append("             YEAR = '").append(_param._year).append("' and CERTIF_KINDCD = '103' ");
            stb.append("        ) AS SCHOOL_NAME, ");
            stb.append("        T5.BIRTHDAY ");
            stb.append("FROM    SCHNO_A T1 ");
            stb.append("        INNER JOIN SCHREG_BASE_MST T5 ON T1.SCHREGNO = T5.SCHREGNO ");
            stb.append("        INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = '" + _param._year + "' ");
            stb.append("              AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("              AND T2.GRADE || T2.HR_CLASS = '" + _param._gradeHrclass + "' ");
            stb.append("        LEFT JOIN COURSE_MST T3 ON T3.COURSECD = T1.COURSECD ");
            stb.append("        LEFT JOIN MAJOR_MST T4 ON T4.COURSECD = T1.COURSECD AND T4.MAJORCD = T1.MAJORCD ");    //05/09/01Modify
            stb.append("        LEFT JOIN GUARDIAN_DAT T6 ON T6.SCHREGNO = T1.SCHREGNO ");
            stb.append("        LEFT JOIN HTRAINREMARK_DAT T7 ON T7.YEAR = '" + _param._year + "' ");
            stb.append("             AND T7.SCHREGNO = T1.SCHREGNO ");
            stb.append("ORDER BY ATTENDNO");

            return stb.toString();
        }

        private void printSvfRegdOut(
                final DB2UDB db2,
                final Vrw32alp svf,
                final ResultSet rs,
                final PreparedStatement ps
        ) throws Exception {
            printSvfSchInfo(svf, rs, "KNJD175_2.frm");
            svf.VrsOut("STAFFNAME1",    (String) _param._arrstaffname.get(0));  //担任名1
            svf.VrsOut("IN1",    "印");  //印1
            if (_param._arrstaffname.size() >= 2) {
                svf.VrsOut("STAFFNAME2",    (String) _param._arrstaffname.get(1));  //担任名2
                svf.VrsOut("IN2",    "印");  //印2
            }

            ArrayList arrlist = null;
            //総合的な学習の時間
            arrlist = _knjobj.retDividString(rs.getString("TOTALSTUDYACT"), 40, 3);  //学習活動
            printsvfOutComment(svf, arrlist, "TOTAL_ACT" , 0);
            arrlist = _knjobj.retDividString(rs.getString("VIEWREMARK"), 40, 3);     //観点
            printsvfOutComment(svf, arrlist, "TOTAL_VIEW", 0);
            arrlist = _knjobj.retDividString(rs.getString("TOTALSTUDYVAL"), 88, 3);  //評価
            printsvfOutComment(svf, arrlist, "TOTAL_VALUE", 0);
            if (_param._semester.equals("1")) {
                svf.VrsOut("NOTE", "※総合的な学習の時間の評定は、後期終了後に行います。");
            }
            //出欠の記録備考
            arrlist = _knjobj.retDividConectString(rs.getString("ATTENDREC_REMARK"), 80, 1);
            printsvfOutComment(svf, arrlist, "REMARK", 1);

            int pp = 1;
            ps.setString(pp++, rs.getString("SCHREGNO"));
            ResultSet rs2 = null;
            try {
                rs2 = ps.executeQuery();
                while (rs2.next()) {
                    //特別活動の記録
                    arrlist = _knjobj.retDividString(rs2.getString("SPECIALACTREMARK"), 42, 2);  //前期 生徒会・委員会・係活動
                    printsvfOutComment(svf, arrlist, "COMMITTEE" + rs2.getString("SEMESTER"), 0);
                    arrlist = _knjobj.retDividString(rs2.getString("TOTALSTUDYTIME"), 42, 4);    //部活動および諸活動
                    printsvfOutComment(svf, arrlist, "CLUB", 0);
                    //通信欄
                    arrlist = _knjobj.retDividString(rs2.getString("COMMUNICATION"), 42, 4);  //前期
                    printsvfOutComment(svf, arrlist, "MESSAGE" + rs2.getString("SEMESTER"), 0);
                }
            } finally {
                DbUtils.closeQuietly(rs2);
            }
        }

        private void printsvfOutComment(final Vrw32alp svf, final ArrayList arrlist, final String svffieldname, final int vrout) {
            if (arrlist != null) {
                for (int i = 0; i < arrlist.size(); i++) {
                    if (vrout == 1) {
                        svf.VrsOut(svffieldname,  (String) arrlist.get(i));
                    } else {
                        svf.VrsOutn(svffieldname, i + 1, (String) arrlist.get(i));
                    }
                }
            }
        }

        void printSvfAttendOut(final Vrw32alp svf, final ResultSet rs) throws Exception {
            int i = Integer.parseInt(rs.getString("SEMESTER"));
            if (i == 9) {
                i = _param._defineCode.semesdiv + 1;
            }
            if (0 <= Integer.parseInt(rs.getString("LESSON"))) {
                svf.VrsOutn("LESSON",     i,   rs.getString("LESSON"));        //授業日数
            }
            svf.VrsOutn("SUSPEND",    i,   String.valueOf(rs.getInt("MOURNING")+rs.getInt("SUSPEND") + ("true".equals(_param._useVirus) ? rs.getInt("VIRUS") : 0)  + ("true".equals(_param._useKoudome) ? rs.getInt("KOUDOME") : 0)));  //出停・忌引日数
            svf.VrsOutn("ABROAD",     i,   rs.getString("TRANSFER_DATE"));     //留学中の授業日数
            svf.VrsOutn("PRESENT",    i,   rs.getString("MLESSON"));           //出席しなければならない日数
            svf.VrsOutn("ABSENCE",    i,   rs.getString("SICK"));            //欠席日数
            svf.VrsOutn("ATTEND",     i,   rs.getString("PRESENT"));           //出席日数
            svf.VrsOutn("LATE",       i,   rs.getString("LATE"));              //遅刻回数
            svf.VrsOutn("LEAVE",      i,   rs.getString("EARLY"));             //早退回数
        }

        private boolean printSvfRecDetail(
                final Vrw32alp svf,
                final PreparedStatement ps,
                final String schregno
        ) throws Exception {
            ResultSet rs = null;
            try {
                int pp = 1;
                int lineNo = 0;
                ps.setString(pp++, schregno);
                rs = ps.executeQuery();
                boolean classFlg = true;
                boolean selectClassFlg = true;
                String viewcd = null;
                while (rs.next()) {
                    if (lineNo == 60) {
                        break;
                    }
                    if (selectClassFlg && !rs.getString("ELECTDIV").equals("0")) {
                        lineNo = printItemTitle(svf, lineNo, "KARA", "空");
                        if (lineNo == 60) {
                            break;
                        }
                        lineNo = printItemTitle(svf, lineNo, "ITEM", "選択教科");
                        selectClassFlg = false;
                    } else if (classFlg) {
                        lineNo = printItemTitle(svf, lineNo, "ITEM", "教科");
                        classFlg = false;
                    }
                    if (lineNo == 60) {
                        break;
                    }
                    if (null == rs.getString("VIEWCD")) {
                        continue;
                    }
                    //観点のブレイク
                    if (viewcd == null || !rs.getString("VIEWCD").equals(viewcd)) {
                        viewcd = printKantenBreak(svf, rs, "1");
                        if (_param._semester.equals("1")) {
                            lineNo = endRecordLineAdd(svf, lineNo);
                        }
                    } else {
                        printKantenBreak(svf, rs, "2");
                        lineNo = endRecordLineAdd(svf, lineNo);
                    }
                }
                umejiPrint(svf, lineNo);
            } finally {
                DbUtils.closeQuietly(rs);
            }
            return false;
        }
    }

    /** 高校用 */
    private class HighSchoolForm extends Form {

        public HighSchoolForm(final DB2UDB db2) throws Exception {
            _arrps[0] = db2.prepareStatement(prestatementRegd());         //学籍データ
            _arrps[1] = db2.prepareStatement(prestatementRightData());    //帳票右側データ
            _arrps[2] = db2.prepareStatement(prestatementSubclassH());    //成績明細データ
            
            String prestatementAttendSemes = AttendAccumulate.getAttendSemesSql(
                    _param._semesFlg,
                    _param._defineCode,
                    _param._knjSchoolMst,
                    _param._year,
                    _param.SSEMESTER,
                    _param._semester,
                    (String) _param._hasuuMap.get("attendSemesInState"),
                    _param._periodInState,
                    (String) _param._hasuuMap.get("befDayFrom"),
                    (String) _param._hasuuMap.get("befDayTo"),
                    (String) _param._hasuuMap.get("aftDayFrom"),
                    (String) _param._hasuuMap.get("aftDayTo"),
                    _param._gradeHrclass.substring(0, 2),
                    _param._gradeHrclass.substring(2, 5),
                    "?",
                    "SEMESTER",
                    _param._useCurriculumcd,
                    _param._useVirus,
                    _param._useKoudome
            );
            log.debug("presatementAttendSemes sql=" + prestatementAttendSemes);
            _arrps[3] = db2.prepareStatement(prestatementAttendSemes);       //出欠データ            
            
            _arrps[4] = db2.prepareStatement(getRisyuSyutokuCredit());    //履修・修得単位
        }

        boolean printMain(final DB2UDB db2, final Vrw32alp svf) throws Exception {
            ResultSet rs1 = null;
            try {
                rs1 = _arrps[0].executeQuery();          //学籍データ
                while (rs1.next()) {
                    printSvfRegdOut(db2, svf, rs1, _arrps[1]);  //学籍情報等を出力
                    printSvfAttend(svf, _arrps[3], rs1.getString("SCHREGNO"), Integer.parseInt(_param._semester));  //出欠データを出力
                    printSvfRisyuSyutoku(svf, _arrps[4], rs1.getString("SCHREGNO"));    //履修・修得単位を出力
                    //成績明細データを出力(SVF-FORMへ出力）
                    if (printSvfRecDetail(svf, _arrps[2], rs1.getString("SCHREGNO"))) {
                        _nonedata = true;
                    }
                    _nonedata = true;
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(rs1);
                prestatementClose(_arrps);
            }
            return _nonedata;
        }

        String getRegdMainSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append("SELECT  T1.SCHREGNO, ");
            stb.append("        T1.ATTENDNO, ");
            stb.append("        SCH_RH.HR_NAME, ");
            stb.append("        SCH_B.NAME, ");
            stb.append("        CERTIF.SCHOOL_NAME, ");
            stb.append("        CERTIF.PRINCIPAL_NAME, ");
            stb.append("        CERTIF.REMARK1, ");
            stb.append("        SCH_B.BIRTHDAY ");
            stb.append("FROM    SCHNO_A T1 ");
            stb.append("        INNER JOIN SCHREG_BASE_MST SCH_B ON T1.SCHREGNO = SCH_B.SCHREGNO ");
            stb.append("        INNER JOIN SCHREG_REGD_HDAT SCH_RH ON SCH_RH.YEAR = '" + _param._year + "' ");
            stb.append("              AND SCH_RH.SEMESTER = T1.SEMESTER ");
            stb.append("              AND SCH_RH.GRADE || SCH_RH.HR_CLASS = '" + _param._gradeHrclass + "', ");
            stb.append("        (select ");
            stb.append("             SCHOOL_NAME, ");
            stb.append("             PRINCIPAL_NAME, ");
            stb.append("             REMARK1 ");
            stb.append("         from ");
            stb.append("             CERTIF_SCHOOL_DAT ");
            stb.append("         where ");
            stb.append("             YEAR = '").append(_param._year).append("' and CERTIF_KINDCD = '104' ");
            stb.append("        ) CERTIF ");
            stb.append("ORDER BY ATTENDNO");
            return stb.toString();
        }

        private void printSvfRegdOut(
                final DB2UDB db2,
                final Vrw32alp svf,
                final ResultSet rs,
                final PreparedStatement ps
        ) throws Exception {
            printSvfSchInfo(svf, rs, "KNJD175_3.frm");
            printSvfRegdInfo(svf, rs);

            ArrayList arrlist = null;

            int pp = 1;
            ps.setString(pp++, rs.getString("SCHREGNO"));
            ResultSet rs2 = null;
            try {
                rs2 = ps.executeQuery();
                while (rs2.next()) {
                    if (rs2.getString("SEMESTER").equals("9")) {
                        //総合的な学習の記録
                        arrlist = _knjobj.retDividString(rs2.getString("TOTALSTUDYTIME"), 42, 5);  //前期 生徒会・委員会・係活動
                        printsvfOutComment(svf, arrlist, "TOTALSTUDYTIME", 0);
                        arrlist = _knjobj.retDividString(rs2.getString("SPECIALACTREMARK"), 42, 5);  //前期 生徒会・委員会・係活動
                        printsvfOutComment(svf, arrlist, "SPECIALACTREMARK", 0);
                    } else {
                        //特別活動の記録
                        svf.VrsOut("COMMITTEE" + rs2.getString("SEMESTER") + "_1", rs2.getString("SPECIALACTREMARK"));
                        svf.VrsOut("CLUB" + rs2.getString("SEMESTER") + "_1", rs2.getString("TOTALSTUDYTIME"));
                        //通信欄
                        arrlist = _knjobj.retDividString(rs2.getString("COMMUNICATION"), 42, 4);  //前期
                        printsvfOutComment(svf, arrlist, "COMMUNICATION" + rs2.getString("SEMESTER"), 1);
                    }
                }
            } finally {
                DbUtils.closeQuietly(rs2);
            }
        }

        private void printSvfRegdInfo(final Vrw32alp svf, final ResultSet rs) throws SQLException {
            int i = 1;
            for (final Iterator it = _param._arrstaffname.iterator(); it.hasNext();) {
                final String staffName = (String) it.next();
                svf.VrsOut("STAFFNAME" + "2_" + i, staffName);
                i++;
            }
            svf.VrsOut("SCHOOLNAME", rs.getString("SCHOOL_NAME"));
            svf.VrsOut("STAFFNAME1", rs.getString("PRINCIPAL_NAME"));
            svf.VrsOut("CERTIF_REMARK1", rs.getString("REMARK1"));
            final String logoPass = "/usr/local/development/src/image/school_logo.bmp";
            final File logof = new File(logoPass);
            if (logof.exists()) {
                svf.VrsOut("SCHOOL_LOGO", logoPass);
            }
        }

        private void printsvfOutComment(final Vrw32alp svf, final ArrayList arrlist, final String svffieldname, final int vrout) {
            final String fieldSep = vrout == 0 ? "" : "_";
            if (arrlist != null) {
                for (int i = 0; i < arrlist.size(); i++) {
                    svf.VrsOut(svffieldname + fieldSep + (i + 1),  (String) arrlist.get(i));
                }
            }
        }

        void printSvfAttendOut(final Vrw32alp svf, final ResultSet rs) throws Exception {
            svf.VrsOut("LESSON" + rs.getString("SEMESTER"),     rs.getString("LESSON"));           //授業日数
            svf.VrsOut("SUSPEND" + rs.getString("SEMESTER"),    String.valueOf(rs.getInt("MOURNING")+rs.getInt("SUSPEND") + ("true".equals(_param._useVirus) ? rs.getInt("VIRUS") : 0) + ("true".equals(_param._useKoudome) ? rs.getInt("KOUDOME") : 0))); //出停・忌引日数
            svf.VrsOut("ABROAD" + rs.getString("SEMESTER"),     rs.getString("TRANSFER_DATE"));    //留学中の授業日数
            svf.VrsOut("PRESENT" + rs.getString("SEMESTER"),    rs.getString("MLESSON"));          //出席しなければならない日数
            svf.VrsOut("ABSENT" + rs.getString("SEMESTER"),    rs.getString("SICK"));            //欠席日数
            svf.VrsOut("ATTEND" + rs.getString("SEMESTER"),     rs.getString("PRESENT"));          //出席日数
            svf.VrsOut("LATE" + rs.getString("SEMESTER"),       rs.getString("LATE"));             //遅刻回数
            svf.VrsOut("LEAVE" + rs.getString("SEMESTER"),      rs.getString("EARLY"));            //早退回数
        }

        private void printSvfRisyuSyutoku(
                final Vrw32alp svf,
                final PreparedStatement ps,
                final String schregno
        ) throws Exception {
            int totalGetCredit = 0;
            int pp = 1;
            ps.setString(pp++, schregno);
            ResultSet rs = null;
            try {
                rs = ps.executeQuery();
                while (rs.next()) {
                    svf.VrsOut("GET_CREDIT" + rs.getInt("GRADE"), rs.getString("GET_CREDIT"));
                    totalGetCredit += rs.getInt("GET_CREDIT");
                }
                svf.VrsOut("TOTAL_GET_CREDIT2", String.valueOf(totalGetCredit));
            } finally {
                DbUtils.closeQuietly(rs);
            }
        }

        private boolean printSvfRecDetail(
                final Vrw32alp svf,
                final PreparedStatement ps,
                final String schregno
        ) throws Exception {
            ResultSet rs = null;
            boolean nonedata = false;
            try {
                String classCd = "";
                int totalGetCredit = 0;
                int pp = 1;
                ps.setString(pp++, schregno);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String compCredits = _param._isjunior ? rs.getString("COMP_CREDIT") : rs.getString("CREDITS");
                    printResult(svf,
                                rs.getString("CLASSCD"),
                                rs.getString("SUBCLASSCD"),
                                rs.getString("CLASSNAME"),
                                rs.getString("SUBCLASSNAME"),
                                rs.getString("REQUIRE"),
                                compCredits,
                                rs.getString("SEM1_VALUE"),
                                rs.getString("ABSENT_SEM1"),
                                rs.getString("GRAD_VALUE"),
                                rs.getString("ABSENT_SEM9"),
                                rs.getString("GET_CREDIT"));
                    classCd = rs.getString("CLASSCD");
                    totalGetCredit += rs.getInt("GET_CREDIT");
                    nonedata = true;
                }
                if (SUBJECT_T.equals(classCd)) {
                    printResult(svf,
                            "*",
                            "*",
                            "奉仕",
                            "奉仕",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "");
                }
                if (!_param._semester.equals("1")) {
                    svf.VrsOut("TOTAL_GET_CREDIT",    String.valueOf(totalGetCredit));
                }
                svf.VrEndRecord();
            } finally {
                DbUtils.closeQuietly(rs);
            }
            return nonedata;
        }

        private void printResult(
                final Vrw32alp svf,
                final String classCd,
                final String subclassCd,
                final String className,
                final String subclassName,
                final String require,
                final String compCredit,
                final String value1,
                final String kekka1,
                final String value2,
                final String kekka2,
                final String getCredit
        ) throws SQLException {
            svf.VrsOut("CLASSNAME",     className);
            svf.VrsOut("SUBCLASSNAME",  subclassName);
            svf.VrsOut("REQUIRE",       require);
            svf.VrsOut("COMP_CREDIT",   compCredit);
            svf.VrsOut("VALUE1",        value1);
            svf.VrsOut("KEKKA1",        kekka1);
            if (!_param._semester.equals("1")) {
                svf.VrsOut("VALUE2", value2);
                svf.VrsOut("KEKKA2", kekka2);
                svf.VrsOut("GET_CREDIT", getCredit);
            }

            final String setMask = SUBJECT_T.equals(classCd) ? "*" : subclassCd;
            svf.VrsOut("COMP_CREDIT_MASK",   setMask);
            svf.VrsOut("VALUE1_MASK",        setMask);
            svf.VrsOut("KEKKA1_MASK",        setMask);
            svf.VrsOut("VALUE2_MASK",        setMask);
            svf.VrsOut("KEKKA2_MASK",        setMask);
            svf.VrsOut("GET_CREDIT_MASK",    setMask);
            svf.VrEndRecord();
        }
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        dumpParam(request, param);
        param.loadAttendSemesArgument(db2);
        return param;
    }

    /** パラメータダンプ */
    private void dumpParam(final HttpServletRequest request, final Param param) {
        log.fatal("$Revision: 56595 $"); // CVSキーワードの取り扱いに注意
        if (log.isDebugEnabled()) {
            final Enumeration enums = request.getParameterNames();
            while (enums.hasMoreElements()) {
                final String name = (String) enums.nextElement();
                final String[] values = request.getParameterValues(name);
                log.debug("parameter:name=" + name + ", value=[" + StringUtils.join(values, ',') + "]");
            }
        }
    }

    /** パラメータクラス */
    private static class Param {
        private final String _year;
        private final String _semester;
        private final String _gradeHrclass;
        private final String _schnoInState;
        private final String _date;
        private final String _printDiv;
        private final String _jviewStatSem;
        private final String _certifDate;
        private final String _gengou;
        private String _schoolName;
        private String _principalName;
        private String _jobName = "";
        private final List _arrstaffname;
        private final String _attendTargetPeriod;
        private final String _certifGrade;
        private final String _attendDate;
        private final String _attendMonth;
        private final boolean _isjunior;
        /** 教育課程コードを使用するか */
        private final String _useCurriculumcd;
        private final String _useVirus;
        private final String _useKoudome;

        private final KNJDefineSchool _defineCode = new KNJDefineSchool();      //各学校における定数等設定
        private KNJSchoolMst _knjSchoolMst;
        
        private String _periodInState;
        private Map _attendSemesMap;
        private Map _hasuuMap;
        private boolean _semesFlg;
        private String _sDate;
        final private String SSEMESTER = "1";

        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("GAKKI");
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _date = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE"));
            _printDiv = request.getParameter("OUTPUT");
            _jviewStatSem = _semester.equals("2") ? "9" : _semester; //学期
                // 修了証記載日付
            _certifDate = null != request.getParameter("CERTIF_DATE") ? KNJ_EditDate.h_format_JP(request.getParameter("CERTIF_DATE")) : "";
            _gengou = nao_package.KenjaProperties.gengou(Integer.parseInt(_year)) + "年度";
            _schnoInState = setSchno(request.getParameterValues("category_selected"));  //学籍番号の編集

            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = null;
            returnval = getinfo.getSchoolName(db2, _year);  //学校名、校長名を取得
            _schoolName = returnval.val1; //学校名
            _principalName = returnval.val2; //校長名
            _arrstaffname = getinfo.Staff_name(db2, _year, _semester, _gradeHrclass);   //学級担任名を取得
            _defineCode.defineCode(db2, _year);      //各学校における定数等設定
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }
                //１日出欠集計対象校時を取得
            returnval = getinfo.getTargetPeriod(db2, _year, _semester, _gradeHrclass, _defineCode.usefromtoperiod);
            _attendTargetPeriod = returnval.val1;    //１日出欠集計対象校時
            getinfo = null;
            returnval = null;

            setCertifData(db2);
            _certifGrade = getCertifGrade(Integer.parseInt(getGrade()));

            final KNJDivideAttendDate obj = new KNJDivideAttendDate();
            obj.getDivideAttendDate(db2, _year, _semester, _date);
            _attendDate = obj.date;
            _attendMonth = obj.month;
            _isjunior = Integer.parseInt(getGrade()) < 4 ? true : false;
        }

        private void setCertifData(final DB2UDB db2) throws Exception {
            final String certifDataSql = getCertifDataSql(_year, _semester);
            if (!certifDataSql.equalsIgnoreCase("")) {
                db2.query(certifDataSql);
                final ResultSet rs = db2.getResultSet();
                if (rs.next()) {
                    _schoolName = rs.getString("SCHOOL_NAME");
                    _jobName = rs.getString("JOB_NAME");
                    _principalName = rs.getString("PRINCIPAL_NAME");
                }
            }
        }
        
        private boolean isPrintCertif(final String semester, final String grade) {
            if (_defineCode.semesdiv != Integer.parseInt(semester)) {
                return false;
            }
            if ("01".equals(grade) || "02".equals(grade)) {
                return true;
            }
            return false;
        }

        private String getCertifDataSql(final String year, final String semester) throws Exception {
            final StringBuffer stb = new StringBuffer();
            if (!isPrintCertif(semester, getGrade())) {
                return "";
            }
            stb.append(" SELECT  SCHOOL_NAME,JOB_NAME, PRINCIPAL_NAME");
            stb.append(" FROM    CERTIF_SCHOOL_DAT T1");
            stb.append(" WHERE   T1.YEAR = '" + year + "'");
            stb.append("     AND T1.CERTIF_KINDCD = '015' ");
            return stb.toString();
        }

        private String getCertifGrade(final int c) {
            if (c >= 0 && c <= 9) {
                final char gradeZenkaku = (char) (c + 0xff10); //全角に変換する。0xff10 = '０' 〜 0xff19 = '９'
                return String.valueOf(gradeZenkaku);
            } else {
                return "";
            }
        }

        private String setSchno(final String[] schno) {
            final StringBuffer stb = new StringBuffer();
            String sep = "";
            stb.append("(");
            for (int ia = 0; ia < schno.length; ia++) {
                stb.append(sep + "'" + schno[ia] + "'");
                sep = ",";
            }
            stb.append(")");

            return stb.toString();
        }

        private String getGrade() {
            return _gradeHrclass.substring(0, 2);  //学年の抽出03001 → 3
        }

        private String getHrclass() {
            return _gradeHrclass.substring(2);     //クラスの抽出03001 → 001
        }

        private KNJDefineCode setClasscode0(final DB2UDB db2) {
            KNJDefineCode definecode = null;
            try {
                definecode = new KNJDefineCode();
                definecode.defineCode(db2, _year);         //各学校における定数等設定
            } catch (Exception ex) {
                log.warn("semesterdiv-get error!", ex);
            }
            return definecode;
        }

        /**
         * 名称マスタ NAMECD1='Z010' NAMECD2='00'読込
         */
        private String setZ010Name1(DB2UDB db2) {
            String name1 = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    name1 = rs.getString("NAME1");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return name1;
        }
        
        /**
         * 出欠状況計算のパラメータをロードする
         */
        private void loadAttendSemesArgument(DB2UDB db2) {
            try {
                loadSemester(db2);
                // 出欠の情報
                final KNJDefineCode definecode0 = setClasscode0(db2);
                final String z010Name1 = setZ010Name1(db2);
                _periodInState = AttendAccumulate.getPeiodValue(db2, definecode0, _year, SSEMESTER, _semester);
                _attendSemesMap = AttendAccumulate.getAttendSemesMap(db2, z010Name1, _year);
                _hasuuMap = AttendAccumulate.getHasuuMap(_attendSemesMap, _sDate, _date); // _sDate: 年度開始日, _date: LOGIN_DATE
                _semesFlg = ((Boolean) _hasuuMap.get("semesFlg")).booleanValue();
            } catch (Exception e) {
                log.debug("loadAttendSemesArgument exception", e);
            }
        }

        /**
         * 年度の開始日を取得する 
         */
        private void loadSemester(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map map = new HashMap();
            final List list = new ArrayList();
            try {
                ps = db2.prepareStatement(sqlSemester());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String name = rs.getString("SEMESTERNAME");
                    map.put(semester, name);

                    final String sDate = rs.getString("SDATE");
                    list.add(sDate);
                }
            } catch (final Exception ex) {
                log.error("テスト項目のロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            if (!list.isEmpty()) {
                _sDate = (String) list.get(0);
            }
            log.debug("年度の開始日=" + _sDate);
        }
        
        private String sqlSemester() {
            final String sql;
            sql = "select"
                + "   SEMESTER,"
                + "   SEMESTERNAME,"
                + "   SDATE"
                + " from"
                + "   SEMESTER_MST"
                + " where"
                + "   YEAR='" + _year + "'"
                + " order by SEMESTER"
            ;
            return sql;
        }
        
        private static boolean setTableColumnCheck(final DB2UDB db2, final String tabname, final String colname) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT 1 FROM ");
            if (StringUtils.isBlank(colname)) {
                stb.append("SYSCAT.TABLES");
            } else {
                stb.append("SYSCAT.COLUMNS");
            }
            stb.append(" WHERE TABNAME = '" + tabname + "' ");
            if (!StringUtils.isBlank(colname)) {
                stb.append(" AND COLNAME = '" + colname + "' ");
            }
            
            PreparedStatement ps = null;
            ResultSet rs = null;
            boolean hasTableColumn = false;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    hasTableColumn = true;
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            log.fatal(" hasTableColumn " + tabname + (null == colname ? "" :  "." + colname) + " = " + hasTableColumn);
            return hasTableColumn;
        }
    }
}
