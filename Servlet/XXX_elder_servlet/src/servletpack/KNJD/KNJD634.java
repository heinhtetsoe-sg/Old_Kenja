// kanji=漢字
/*
 * $Id: f71b58d024ee9ba5af60a854984f2ad85f87e198 $
 *
 * 作成日: 2007/09/10
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2007 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;

import servletpack.KNJC.KNJC053_BASE;
import servletpack.KNJC.KNJDivideAttendDate;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJEditString;
import servletpack.KNJZ.detail.KNJObjectAbs;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * 通知票（自修館）
 * @author nakamoto
 * @version $Id: f71b58d024ee9ba5af60a854984f2ad85f87e198 $
 */
public class KNJD634 {

    private static final Log log = LogFactory.getLog(KNJD634.class);

    /** 通知票。中学 */
    public String FORM_FILE1 = "KNJD634_2.frm";

    /** 通知票。高校 */
    public String FORM_FILE2 = "KNJD634_1.frm";

    Param _param;

    private KNJDefineSchool definecode;

    private KNJObjectAbs knjobj;            //編集用クラス
    
    private KNJSchoolMst _knjSchoolMst;

    /**
     * KNJD.classから呼ばれる処理。
     * @param request リクエスト
     * @param response レスポンス
     * @throws Exception I/O例外
     */
    public void svf_out(
        final HttpServletRequest request,
        final HttpServletResponse response
    ) throws Exception {
        dumpParam(request);
        _param = createParam(request);

        final Vrw32alp svf = new Vrw32alp();    //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        try {
            init(response, svf);

            // DB接続
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            definecode = new KNJDefineSchool();
            definecode.defineCode(db2, _param._year);
            if (log.isDebugEnabled()) {
                log.debug("schoolmark=" + definecode.schoolmark + " *** semesdiv=" + definecode.semesdiv + " *** absent_cov=" + definecode.absent_cov + " *** absent_cov_late=" + definecode.absent_cov_late);
            }

            _param.loadConstant(db2);
            _knjSchoolMst = new KNJSchoolMst(db2, _param._year);

            // 印字メイン
            boolean hasData = false;   // 該当データ無しフラグ
            hasData = printMain(db2, svf);
            if (!hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } finally {
            close(svf, db2);
        }
    }

    /** 印刷処理メイン */
    private boolean printMain(
            final DB2UDB db2,
            final Vrw32alp svf
    ) throws Exception {
        boolean rtnflg = false;

        final List students = createStudents(db2);
        log.debug("生徒数=" + students.size());

        final String svfFileName = _param.getForms();
        svf.VrSetForm(svfFileName, 1);
        log.debug("印刷するフォーム:" + svfFileName);

        for (final Iterator it = students.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();

            // ヘッダー出力
            printHeader(svf, student);

            // 学校生活の記録
            if (_param.isJunior()) printBehavior(db2, svf, student);

            // 検定試験等の結果
            printQualified(db2, svf, student);

            // 学習の記録
            printScore(db2, svf, student);

            // 通信欄
            String totalStudyTime = getTotalStudyTime(db2, svf, student);
            printTotalStudyTime(svf, student, totalStudyTime);

            // 出欠席の記録
            printAttend(db2, svf, student);
            
            svf.VrEndPage();

            rtnflg = true;
        }

        return rtnflg;
    }

    private String getTotalStudyTime(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        log.debug("---------- 通信欄設定値取得 schno=" + student._schregno + " ----------");
        
        String retTotalStudyTime = "";
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        final String sql = sqlTotalStudyTime(student);
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
            	retTotalStudyTime = rs.getString("TOTALSTUDYTIME");
            }
            if(retTotalStudyTime == null){
            	retTotalStudyTime = "";
            }
        } catch (final Exception ex) {
            log.error("通信欄設定値取得でエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retTotalStudyTime;
    }
    
    private void printTotalStudyTime(final Vrw32alp svf, final Student student, String totalStudyTime) {
        log.debug("---------- 通信欄編集 schno=" + student._schregno + " ----------");

        //定義
        knjobj = new KNJEditString();
        ArrayList arrlist = knjobj.retDividString( totalStudyTime, 30, 8 );
        if( arrlist != null ){
            for( int i = 0 ; i < arrlist.size() ; i++ ){
                svf.VrsOut("SCHOOL_NOTE"+ ( i+1 ),  (String)arrlist.get(i) );         //総合的な学習
            }
        }
        
    }

    private void printAttend(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        log.debug("---------- 出欠席の記録 schno=" + student._schregno + " ----------");
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = sqlAttend(student);
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                int seme = rs.getInt("SEMESTER");
                if (!"2".equals(_param._semester) && (seme == 3 || seme == 4)) {
                    continue;
                }
                svf.VrsOutn("LESSON",   seme, rs.getString("LESSON"));
                svf.VrsOutn("SUSPEND",  seme, rs.getString("SUSPEND"));
                svf.VrsOutn("PRESENT",  seme, rs.getString("PRESENT"));
                svf.VrsOutn("ABSENCE",  seme, rs.getString("ABSENCE"));
                svf.VrsOutn("ATTEND",   seme, rs.getString("ATTEND"));
                svf.VrsOutn("LATE",     seme, rs.getString("LATE"));
                svf.VrsOutn("LEAVE",    seme, rs.getString("LEAVE"));
                log.debug("seme=" + seme);
            }
        } catch (final Exception ex) {
            log.error("出欠席の記録のロードでエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private void printScore(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        log.debug("---------- 学習の記録 schno=" + student._schregno + " ----------");
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = sqlScore(student);
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            int gyono = 0;
            final int strlen = _param.isJunior() ? 10 : 16;
            while (rs.next()) {
                // 中学で科目コード頭1桁が’2’の科目は対象外とする。
                if (_param.isJunior() && rs.getString("SUBCLASSCD").startsWith("2")) {
                    continue;
                }
                gyono++;
                String fieldno = strlen < rs.getString("SUBCLASSNAME").length() ? "2_1" : "";
                svf.VrsOutn("SUBCLASSNAME" + fieldno, gyono, rs.getString("SUBCLASSNAME"));
                svf.VrsOutn("VALUE1", gyono, rs.getString("VALUE1") == null ? "-" : rs.getString("VALUE1"));
                if (rs.getString("VALUE1") != null && ("1".equals(rs.getString("VALUE1")) || "2".equals(rs.getString("VALUE1")) || "3".equals(rs.getString("VALUE1")))) { // 評点が1,2,3なら赤字
                    svf.VrAttributen("VALUE1", gyono, "Palette=9");
                }
                if ("2".equals(_param._semester)) {
                    svf.VrsOutn("VALUE2", gyono, rs.getString("VALUE2") == null ? "-" : rs.getString("VALUE2"));
                    if (rs.getString("VALUE2") != null && ("1".equals(rs.getString("VALUE2")) || "2".equals(rs.getString("VALUE2")) || "3".equals(rs.getString("VALUE2")))) { // 評点が1,2,3なら赤字
                        svf.VrAttributen("VALUE2", gyono, "Palette=9");
                    }
                }
                if (_param.isJunior()) {
                    if (rs.getString("VALUE9") != null && "1".equals(rs.getString("VALUE9"))) { // 評定が1なら赤字
                        svf.VrAttributen("VALUE9", gyono, "Palette=9");
                    }
                    svf.VrsOutn("VALUE9", gyono, rs.getString("VALUE9"));
                } else {
                    if (_param._year.equals("2007") && rs.getString("SUBCLASSCD").equals("280202")) {
                        svf.VrsOutn("VALUE9", gyono, "-");
                    } else {
                        if (rs.getString("VALUE9") != null && "1".equals(rs.getString("VALUE9"))) { // 評定が1なら赤字
                            svf.VrAttributen("VALUE9", gyono, "Palette=9");
                        }
                        svf.VrsOutn("VALUE9", gyono, rs.getString("VALUE9"));
                    }
                }
                if (!_param.isJunior()) {
                    String lesson1 = rs.getString("LESSON1");
                    String lesson2 = rs.getString("LESSON2");
                    if ("0".equals(lesson1)) svf.VrsOutn("VALUE1", gyono, "-");
                    svf.VrsOutn("NOTICE1", gyono, "0".equals(lesson1) ? "" : rs.getString("NOTICE1"));
                    svf.VrsOutn("LESSON1", gyono, "0".equals(lesson1) ? "" : rs.getString("LESSON1"));
                    if ("2".equals(_param._semester)) {
                        if ("0".equals(lesson2)) svf.VrsOutn("VALUE2", gyono, "-");
                        svf.VrsOutn("NOTICE2", gyono, "0".equals(lesson2) ? "" : rs.getString("NOTICE2"));
                        svf.VrsOutn("LESSON2", gyono, "0".equals(lesson2) ? "" : rs.getString("LESSON2"));
                    }
                    svf.VrsOutn("GET_CREDIT", gyono, rs.getString("GET_CREDIT"));
                }
                log.debug("subclasscd=" + rs.getString("SUBCLASSCD") + "、subclassname=" + rs.getString("SUBCLASSNAME"));
            }
        } catch (final Exception ex) {
            log.error("学習の記録のロードでエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private void printQualified(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        log.debug("---------- 検定試験等の結果 schno=" + student._schregno + " ----------");
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = sqlQualified(student);
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                String fieldno = rs.getString("CONTENTS");
                svf.VrsOut("REMARK"   + fieldno, rs.getString("REMARK"));
                svf.VrsOut("REGDDATE" + fieldno, KNJ_EditDate.h_format_JP(rs.getString("REGDDATE")) + " 取得");
                log.debug("contents=" + fieldno + "、date=" + rs.getString("REGDDATE"));
            }
        } catch (final Exception ex) {
            log.error("検定試験のロードでエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private void printBehavior(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        log.debug("---------- 学校生活の記録 schno=" + student._schregno + " ----------");
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = sqlBehavior(student);
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                svf.VrsOutn("RECORD", rs.getInt("CODE"), rs.getString("RECORD"));
                log.debug("code=" + rs.getInt("CODE"));
            }
        } catch (final Exception ex) {
            log.error("学校生活の記録のロードでエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private void printHeader(final Vrw32alp svf, final Student student) {
        svf.VrsOut("NENDO",     _param._nendo);
        svf.VrsOut("SEMESTER",  _param._semeName);
        svf.VrsOut("STAFFNAME", _param._staffName);
        svf.VrsOut("HR_NAME",   student._hrname);
        svf.VrsOut("NAME",      student._name);
        if (_param._stamp.exists()) {
            svf.VrsOut("SCHOOLLOGO", _param._stamp.toString());
        }
    }

    private List createStudents(final DB2UDB db2) throws SQLException {
        final List rtn = new ArrayList();
        final String sql = sqlStudents();

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("schregno");
                final String grade = rs.getString("grade");
                final String hrclass = rs.getString("hrclass");
                final String attendno = rs.getString("attendno");
                final String hrname = rs.getString("hrname");
                final String name = rs.getString("name");

                final Student student = new Student(
                        schregno,
                        grade,
                        hrclass,
                        attendno,
                        hrname,
                        name
                );

                rtn.add(student);
            }
        } catch (final Exception ex) {
            log.error("生徒のロードでエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return rtn;
    }

    private Param createParam(final HttpServletRequest request) {
        final String year = request.getParameter("YEAR");
        final String semester = request.getParameter("SEMESTER");
        final String gradeHrClass = request.getParameter("GRADE_HR_CLASS");
        final String staffcd = request.getParameter("TR_CD1");
        final String documentRoot = request.getParameter("DOCUMENTROOT");
        final String imagePath = request.getParameter("IMAGEPATH");
        final String date = request.getParameter("DATE");
        final String[] schregno = request.getParameterValues("category_selected");

        return new Param(
                year,
                semester,
                gradeHrClass,
                staffcd,
                documentRoot + '/' + imagePath + '/',
                date,
                schregno);
    }

    private void dumpParam(final HttpServletRequest request) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
    }

    private void init(final HttpServletResponse response, final Vrw32alp svf) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private void close(final Vrw32alp svf, final DB2UDB db2) {
        if (null != svf) {
            svf.VrQuit();
        }
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    private String sqlStudents() {
        final String students = SQLUtils.whereIn(true, _param._schregno);
        return " select"
                + "    T1.SCHREGNO as schregno,"
                + "    T1.GRADE as grade,"
                + "    T1.HR_CLASS as hrclass,"
                + "    T1.ATTENDNO as attendno,"
                + "    T0.HR_NAME as hrname,"
                + "    T2.NAME_SHOW as name"
                + " from"
                + "    SCHREG_REGD_HDAT T0,"
                + "    SCHREG_REGD_DAT T1,"
                + "    SCHREG_BASE_MST T2"
                + " where"
                + "    T0.YEAR = T1.YEAR and"
                + "    T0.SEMESTER = T1.SEMESTER and"
                + "    T0.GRADE = T1.GRADE and"
                + "    T0.HR_CLASS = T1.HR_CLASS and"
                + "    T1.SCHREGNO = T2.SCHREGNO and"
                + "    T1.YEAR = '" + _param._year + "' and"
                + "    T1.SEMESTER = '" + _param._semester + "' and"
                + "    T1.SCHREGNO in " + students
                + " order by"
                + "    T1.GRADE,"
                + "    T1.HR_CLASS,"
                + "    T1.ATTENDNO";
    }

    private String sqlBehavior(final Student student) {
        return "SELECT "
                + "    SCHREGNO, "
                + "    CODE, "
                + "    RECORD "
                + "FROM "
                + "    BEHAVIOR_SEMES_DAT "
                + "WHERE "
                + "    YEAR='" + _param._year + "' AND "
                + "    SEMESTER='" + _param._semester + "' AND "
                + "    SCHREGNO='" + student._schregno + "' "
                + "ORDER BY "
                + "    CODE ";
    }

    private String sqlQualified(final Student student) {
        return "SELECT "
                + "    T1.SCHREGNO, "
                + "    T1.REGDDATE, "
                + "    T1.CONDITION_DIV, "
                + "    T1.REMARK, "
                + "    T1.CONTENTS "
                + "FROM "
                + "    SCHREG_QUALIFIED_DAT T1 "
                + "WHERE "
                + "    T1.CONDITION_DIV='1' AND "
                + "    T1.SCHREGNO='" + student._schregno + "' "
                + "ORDER BY "
                + "    T1.CONTENTS, "
                + "    T1.REGDDATE ";
    }

    private String sqlScore(final Student student) {
        StringBuffer stb = new StringBuffer();
        stb.append("WITH T_RECORD AS ( ");
        stb.append("    SELECT  T1.SUBCLASSCD, ");
        stb.append("            T1.SCHREGNO, ");
        stb.append("            sum(case when T1.SEMESTER = '1' then T1.VALUE else NULL end) as VALUE1, ");
        stb.append("            sum(case when T1.SEMESTER = '2' then T1.VALUE else NULL end) as VALUE2, ");
        stb.append("            sum(case when T1.SEMESTER = '9' then T1.VALUE else NULL end) as VALUE9, ");
        stb.append("            sum(case when T1.SEMESTER = '9' then T1.GET_CREDIT else NULL end) as GET_CREDIT ");
        stb.append("    FROM    RECORD_SCORE_DAT T1 ");
        stb.append("    WHERE   T1.YEAR='" + _param._year + "' AND ");
        if ("1".equals(_param._semester)) {
            stb.append("        T1.SEMESTER not in ('2') AND ");
        }
        stb.append("            T1.TESTKINDCD='99' AND ");
        stb.append("            T1.TESTITEMCD='00' AND ");
        stb.append("            T1.SCORE_DIV='00' AND ");
        stb.append("            T1.SCHREGNO='" + student._schregno + "' ");
        stb.append("    GROUP BY T1.SUBCLASSCD, T1.SCHREGNO ");
        stb.append("    ) ");
        stb.append(", T_SUBCLASS AS ( ");
        stb.append("    SELECT  T2.SUBCLASSCD, T1.SCHREGNO ");
        stb.append("    FROM    CHAIR_STD_DAT T1, ");
        stb.append("            CHAIR_DAT T2 ");
        stb.append("    WHERE   T1.YEAR='" + _param._year + "' AND ");
        stb.append("            T1.SEMESTER<='" + _param._semester + "' AND ");
        stb.append("            T1.SCHREGNO='" + student._schregno + "' AND ");
        stb.append("            T2.SUBCLASSCD NOT LIKE '9%' AND ");
        stb.append("            T2.SUBCLASSCD NOT LIKE '10%' AND ");
        stb.append("            T2.SUBCLASSCD NOT LIKE '11%' AND ");
        stb.append("            T2.YEAR=T1.YEAR AND ");
        stb.append("            T2.SEMESTER=T1.SEMESTER AND ");
        stb.append("            T2.CHAIRCD=T1.CHAIRCD ");
        stb.append("    GROUP BY T2.SUBCLASSCD, T1.SCHREGNO ");
        stb.append("    ) ");
        // テスト項目マスタの集計フラグ
        stb.append(" ,TEST_COUNTFLG AS ( ");
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
        stb.append(", T_REPLACE AS ( ");
        stb.append("    SELECT  COMBINED_SUBCLASSCD, ATTEND_SUBCLASSCD ");
        stb.append("    FROM    SUBCLASS_REPLACE_COMBINED_DAT ");
        stb.append("    WHERE   REPLACECD='1' AND YEAR='" + _param._year + "' ");
        stb.append("    ) ");
        if (!_param.isJunior()) {
            // 時間割(休学・留学時数を含む)
            stb.append(", SCHEDULE_SCHREG AS(");
            stb.append("    SELECT  T2.SCHREGNO, T5.SUBCLASSCD, T0.SEMESTER, T1.EXECUTEDATE, T1.PERIODCD, T4.DI_CD, T1.CHAIRCD ");
            stb.append("    FROM    SCH_CHR_DAT T1 ");
            stb.append("    INNER JOIN CHAIR_DAT T0 ON T1.YEAR = T0.YEAR ");
            stb.append("        AND T1.SEMESTER = T0.SEMESTER ");
            stb.append("        AND T1.CHAIRCD = T0.CHAIRCD ");
            stb.append("    INNER JOIN CHAIR_STD_DAT T2 ON T1.YEAR = T2.YEAR ");
            stb.append("        AND T1.SEMESTER = T2.SEMESTER ");
            stb.append("        AND T1.CHAIRCD = T2.CHAIRCD ");
            stb.append("        AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ");
            stb.append("    INNER JOIN CHAIR_DAT T5 ON T5.CHAIRCD = T0.CHAIRCD ");
            stb.append("        AND T5.YEAR = T1.YEAR ");
            stb.append("        AND T5.SEMESTER = T1.SEMESTER ");
            stb.append("    LEFT JOIN SCHREG_TRANSFER_DAT T3 ON T3.SCHREGNO = T2.SCHREGNO ");
            stb.append("        AND T3.TRANSFERCD = '2' ");
            stb.append("        AND T1.EXECUTEDATE BETWEEN T3.TRANSFER_SDATE AND T3.TRANSFER_EDATE ");
            stb.append("    LEFT JOIN ATTEND_DAT T4 ON T4.SCHREGNO = T2.SCHREGNO ");
            stb.append("        AND T4.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append("        AND T4.PERIODCD = T1.PERIODCD ");
            stb.append("        AND T4.CHAIRCD = T1.CHAIRCD ");
            stb.append("    WHERE   T1.YEAR = '" + _param._year + "' ");
            stb.append("        AND T1.SEMESTER <= '" + _param._semester + "' ");
            stb.append("        AND T1.EXECUTEDATE BETWEEN '" + _param._attendDate + "' AND '" + _param._date + "' ");
            stb.append("        AND T2.SCHREGNO='" + student._schregno + "' ");
            stb.append("        AND NOT EXISTS(SELECT  'X' FROM SCH_CHR_COUNTFLG T4 ");
            stb.append("                        WHERE   T4.EXECUTEDATE = T1.EXECUTEDATE ");
            stb.append("                            AND T4.PERIODCD = T1.PERIODCD ");
            stb.append("                            AND T4.CHAIRCD = T1.CHAIRCD ");
            stb.append("                            AND T1.DATADIV IN ('0', '1') ");
            stb.append("                            AND T4.GRADE||T4.HR_CLASS = '" + _param._gradeHrClass + "' ");
            stb.append("                            AND T4.COUNTFLG = '0') ");
            stb.append("        AND NOT EXISTS(SELECT  'X' FROM TEST_COUNTFLG TEST ");
            stb.append("                        WHERE ");
            stb.append("                            TEST.EXECUTEDATE  = T1.EXECUTEDATE ");
            stb.append("                            AND TEST.PERIODCD = T1.PERIODCD ");
            stb.append("                            AND TEST.CHAIRCD  = T1.CHAIRCD ");
            stb.append("                            AND TEST.DATADIV  = T1.DATADIV) ");
            stb.append("        AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST T3 ");    //05/10/07Build NOT EXISTS
            stb.append("                       WHERE   T3.SCHREGNO = T2.SCHREGNO ");
            stb.append("                           AND (( ENT_DIV IN('4','5') AND EXECUTEDATE < ENT_DATE ) ");
            stb.append("                             OR ( GRD_DIV IN('2','3') AND EXECUTEDATE > GRD_DATE )) ) ");
            stb.append(") ");
            // 時間割(休学・留学時数を含まない)
            stb.append(", T_ATTEND_DAT AS(");
            stb.append("    SELECT  T1.SCHREGNO, T1.SUBCLASSCD, T1.SEMESTER, T1.EXECUTEDATE, T1.PERIODCD, T1.DI_CD, T1.CHAIRCD ");
            stb.append("    FROM    SCHEDULE_SCHREG T1 ");
            stb.append("    WHERE   NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT T5 ");    //05/10/07Build NOT EXISTS
            stb.append("                       WHERE   T5.SCHREGNO = T1.SCHREGNO ");
            stb.append("                           AND T5.TRANSFERCD IN ('1','2') ");
            stb.append("                           AND T1.EXECUTEDATE BETWEEN T5.TRANSFER_SDATE AND T5.TRANSFER_EDATE ) ");
            stb.append(") ");
            // 休学時数
            stb.append(", SCHEDULE_OFFDAYS AS(");
            stb.append("    SELECT  T1.SCHREGNO, T1.SUBCLASSCD, T1.SEMESTER, COUNT(*) AS OFFDAYS ");
            stb.append("    FROM    SCHEDULE_SCHREG T1 ");
            stb.append("    WHERE   EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT T5 ");    //05/10/07Build NOT EXISTS
            stb.append("                       WHERE   T5.SCHREGNO = T1.SCHREGNO ");
            stb.append("                           AND T5.TRANSFERCD = '2' ");
            stb.append("                           AND T1.EXECUTEDATE BETWEEN T5.TRANSFER_SDATE AND T5.TRANSFER_EDATE ) ");
            stb.append("    GROUP BY T1.SCHREGNO, T1.SUBCLASSCD, T1.SEMESTER ");
            stb.append(") ");
            // 総時間数(休学・留学時数を含む)
            stb.append(", SCH_CHAIR_SUM AS( ");
            stb.append("    SELECT  T1.SUBCLASSCD, T1.SEMESTER ");
            stb.append("           ,COUNT(*) ");
            stb.append("            AS LESSON ");
            stb.append("    FROM    SCHEDULE_SCHREG T1 ");
            stb.append("    GROUP BY T1.SEMESTER, T1.SUBCLASSCD ");
            stb.append("    UNION ALL ");
            stb.append("    SELECT  T1.SUBCLASSCD, T1.SEMESTER ");
            stb.append("           ,SUM(LESSON) AS LESSON ");
            stb.append("    FROM    ATTEND_SUBCLASS_DAT T1 ");
            stb.append("    WHERE   T1.SCHREGNO = '" + student._schregno + "' ");
            stb.append("        AND T1.YEAR = '" + _param._year + "' ");
            stb.append("        AND T1.SEMESTER <= '" + _param._semester + "' ");
            stb.append("        AND (CASE WHEN INT(T1.MONTH) < 4 THEN RTRIM(CHAR(INT(T1.SEMESTER) + 1 )) ELSE T1.SEMESTER END )||T1.MONTH <= '" + KNJC053_BASE.retSemesterMonthValue (_param._attendMonth) + "' ");
            stb.append("    GROUP BY T1.SEMESTER, T1.SUBCLASSCD ");
            stb.append(") ");
            stb.append(", SCH_ATTEND_SUM AS( ");
            stb.append("    SELECT  T1.SUBCLASSCD, T1.SEMESTER ");
            stb.append("           ,SUM(CASE WHEN DI_CD IN('4','5','6','14','11','12','13'");
            if ("1".equals(_knjSchoolMst._subAbsent)) {
                stb.append("            ,'1','8'");
            }
            if ("1".equals(_knjSchoolMst._subSuspend)) {
                stb.append("            ,'2','9'");
            }
            if ("1".equals(_knjSchoolMst._subMourning)) {
                stb.append("            ,'3','10'");
            }
            if ("1".equals(_knjSchoolMst._subVirus)) {
                stb.append("            ,'19','20'");
            }
            stb.append("            ) THEN 1 ELSE 0 END ");
            if ("1".equals(_knjSchoolMst._subOffDays)) {
                stb.append("            + T2.OFFDAYS ");
            }
            stb.append("             ) AS ABSENT ");
            stb.append("           ,SUM(CASE WHEN DI_CD IN('15','16','23','24') THEN SMALLINT(VALUE(L1.ABBV2, '1')) ELSE 0 END)AS LATE_EARLY ");
            stb.append("    FROM    T_ATTEND_DAT T1 ");
            stb.append("            LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'C001' AND L1.NAMECD2 = T1.DI_CD ");
            stb.append("    LEFT JOIN SCHEDULE_OFFDAYS T2 ON T1.SCHREGNO = T2.SCHREGNO ");
            stb.append("        AND T1.SEMESTER = T2.SEMESTER ");
            stb.append("        AND T1.SUBCLASSCD = T2.SUBCLASSCD ");
            stb.append("    GROUP BY T1.SEMESTER, T1.SUBCLASSCD ");
            stb.append("    UNION ALL ");
            stb.append("    SELECT  T1.SUBCLASSCD, T1.SEMESTER ");
            stb.append("           ,SUM(VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) + VALUE(NURSEOFF,0) ");
            if ("1".equals(_knjSchoolMst._subAbsent)) {
                stb.append(       "+ VALUE(ABSENT,0) ");
            }
            if ("1".equals(_knjSchoolMst._subSuspend)) {
                stb.append(       "+ VALUE(SUSPEND,0) ");
            }
            if ("1".equals(_knjSchoolMst._subMourning)) {
                stb.append(       "+ VALUE(MOURNING,0) ");
            }
            if ("1".equals(_knjSchoolMst._subOffDays)) {
                stb.append(       "+ VALUE(OFFDAYS,0) ");
            }
            if ("1".equals(_knjSchoolMst._subVirus)) {
                stb.append(       "+ VALUE(VIRUS,0) ");
            }
            stb.append("            ) AS ABSENT ");
            stb.append("           ,SUM(VALUE(LATE,0) + VALUE(EARLY,0)) AS LATE_EARLY ");
            stb.append("    FROM    ATTEND_SUBCLASS_DAT T1 ");
            stb.append("    WHERE   SCHREGNO = '" + student._schregno + "' ");
            stb.append("        AND YEAR = '" + _param._year + "' ");
            stb.append("        AND T1.SEMESTER <= '" + _param._semester + "' ");
            stb.append("        AND (CASE WHEN INT(T1.MONTH) < 4 THEN RTRIM(CHAR(INT(T1.SEMESTER) + 1 )) ELSE T1.SEMESTER END )||T1.MONTH <= '" + KNJC053_BASE.retSemesterMonthValue (_param._attendMonth) + "' ");
            stb.append("    GROUP BY T1.SEMESTER, T1.SUBCLASSCD ");
            stb.append(") ");
            stb.append(", CHAIR_B AS( ");
            stb.append("       SELECT  SUBCLASSCD ");
            stb.append("              ,VALUE(SUM(CASE WHEN SEMESTER = '1' THEN LESSON ELSE NULL END),0) AS LESSON1 ");
            stb.append("              ,VALUE(SUM(CASE WHEN SEMESTER = '2' THEN LESSON ELSE NULL END),0) AS LESSON2 ");
            stb.append("       FROM    SCH_CHAIR_SUM T1 ");
            stb.append("       GROUP BY SUBCLASSCD ");
            stb.append(") ");
            // ペナルティー欠課を加味した欠課集計の表（出欠データと集計テーブルを合算）
            stb.append(", ATTEND_B AS( ");
            stb.append("       SELECT  SUBCLASSCD ");
            stb.append("              ,VALUE(SUM(CASE WHEN SEMESTER = '1' THEN NOTICE ELSE NULL END),0) AS NOTICE1 ");
            stb.append("              ,VALUE(SUM(CASE WHEN SEMESTER = '2' THEN NOTICE ELSE NULL END),0) AS NOTICE2 ");
            stb.append("       FROM(   SELECT  SUBCLASSCD, SEMESTER ");
            if (definecode.absent_cov == 1 || definecode.absent_cov == 3) {
                //学期でペナルティ欠課を算出する場合
                stb.append("                  ,VALUE(SUM(ABSENT),0) + VALUE(SUM(LATE_EARLY),0) / " + definecode.absent_cov_late + " AS NOTICE ");
            } else if (definecode.absent_cov == 2 || definecode.absent_cov == 4) {
                //通年でペナルティ欠課を算出する場合 
                //学期の欠課時数は学期別で換算したペナルティ欠課を加算、学年の欠課時数は年間で換算する
                stb.append("                  ,VALUE(SUM(ABSENT),0) + VALUE(SUM(LATE_EARLY),0) / " + definecode.absent_cov_late + " AS NOTICE ");
            } else{
                //ペナルティ欠課なしの場合
                stb.append("                  ,VALUE(SUM(ABSENT),0) AS NOTICE ");
            }
            stb.append("               FROM    SCH_ATTEND_SUM T1 ");
            stb.append("               GROUP BY SUBCLASSCD, SEMESTER ");
            stb.append("           )T1 ");
            stb.append("       GROUP BY SUBCLASSCD ");
            stb.append(") ");
            //合併先科目の欠課時数・総時間数は、元科目の合計値を印刷する。
            stb.append(", CHAIR_C AS( ");
            stb.append("       SELECT  T1.SUBCLASSCD ");
            stb.append("              ,T1.LESSON1 ");
            stb.append("              ,T1.LESSON2 ");
            stb.append("       FROM    CHAIR_B T1 ");
            stb.append("       WHERE   NOT EXISTS(SELECT 'X' FROM T_REPLACE R1 WHERE R1.COMBINED_SUBCLASSCD=T1.SUBCLASSCD) ");
            stb.append("       UNION ALL ");
            stb.append("       SELECT  R1.COMBINED_SUBCLASSCD AS SUBCLASSCD ");
            stb.append("              ,VALUE(SUM(T1.LESSON1),0) AS LESSON1 ");
            stb.append("              ,VALUE(SUM(T1.LESSON2),0) AS LESSON2 ");
            stb.append("       FROM    CHAIR_B T1 ");
            stb.append("               INNER JOIN T_REPLACE R1 ON R1.ATTEND_SUBCLASSCD=T1.SUBCLASSCD ");
            stb.append("       GROUP BY R1.COMBINED_SUBCLASSCD ");
            stb.append(") ");
            stb.append(", ATTEND_C AS( ");
            stb.append("       SELECT  T1.SUBCLASSCD ");
            stb.append("              ,T1.NOTICE1 ");
            stb.append("              ,T1.NOTICE2 ");
            stb.append("       FROM    ATTEND_B T1 ");
            stb.append("       WHERE   NOT EXISTS(SELECT 'X' FROM T_REPLACE R1 WHERE R1.COMBINED_SUBCLASSCD=T1.SUBCLASSCD) ");
            stb.append("       UNION ALL ");
            stb.append("       SELECT  R1.COMBINED_SUBCLASSCD AS SUBCLASSCD ");
            stb.append("              ,VALUE(SUM(T1.NOTICE1),0) AS NOTICE1 ");
            stb.append("              ,VALUE(SUM(T1.NOTICE2),0) AS NOTICE2 ");
            stb.append("       FROM    ATTEND_B T1 ");
            stb.append("               INNER JOIN T_REPLACE R1 ON R1.ATTEND_SUBCLASSCD=T1.SUBCLASSCD ");
            stb.append("       GROUP BY R1.COMBINED_SUBCLASSCD ");
            stb.append(") ");
        }

        stb.append("SELECT  T0.SUBCLASSCD, ");
        stb.append("        CASE WHEN T2.SUBCLASSORDERNAME2 IS NOT NULL THEN T2.SUBCLASSORDERNAME2 ");
        stb.append("             ELSE T2.SUBCLASSNAME END AS SUBCLASSNAME, ");
        stb.append("        T0.SCHREGNO, ");
        stb.append("        T1.VALUE1, ");
        stb.append("        T1.VALUE2, ");
        if (!_param.isJunior()) {
            stb.append("    VALUE(T3.NOTICE1,0) as NOTICE1, ");
            stb.append("    VALUE(T3.NOTICE2,0) as NOTICE2, ");
            stb.append("    VALUE(T4.LESSON1,0) as LESSON1, ");
            stb.append("    VALUE(T4.LESSON2,0) as LESSON2, ");
        }
        stb.append("        T1.VALUE9, ");
        stb.append("        T1.GET_CREDIT ");
        stb.append("FROM    T_SUBCLASS T0 ");
        stb.append("        LEFT JOIN T_RECORD T1 ON T1.SUBCLASSCD=T0.SUBCLASSCD ");
        stb.append("        LEFT JOIN SUBCLASS_MST T2 ON T2.SUBCLASSCD=T0.SUBCLASSCD ");
        if (!_param.isJunior()) {
            stb.append("    LEFT JOIN ATTEND_C T3 ON T3.SUBCLASSCD=T0.SUBCLASSCD ");
            stb.append("    LEFT JOIN CHAIR_C T4 ON T4.SUBCLASSCD=T0.SUBCLASSCD ");
        }
        stb.append("WHERE   NOT EXISTS(SELECT 'X' FROM T_REPLACE R1 WHERE R1.ATTEND_SUBCLASSCD=T0.SUBCLASSCD) ");
        if (_param.isJunior()) {
            stb.append("AND NOT EXISTS(SELECT 'X' FROM SUBCLASS_MST S1 WHERE S1.SUBCLASSCD=T0.SUBCLASSCD AND S1.ELECTDIV='1') ");
        }
        stb.append("ORDER BY T0.SUBCLASSCD ");
        return stb.toString();
    }
    
    private String sqlTotalStudyTime(final Student student) {
        
        return "select "
                 + "    TOTALSTUDYTIME "
                 + "    from  HREPORTREMARK_DAT "
                 + "    where "
                 + "    YEAR = '" + _param._year + "' and"
                 + "    SEMESTER = '" + _param._semester + "' and"
                 + "    SCHREGNO = '" + student._schregno + "'"
                 ;
    }
    
    
    
    private String sqlAttend(final Student student) {
        StringBuffer stb = new StringBuffer();
        stb.append("WITH SCHNO (SCHREGNO, SEMESTER) AS(");
        stb.append(    "VALUES( cast('" + student._schregno + "' as varchar(8) ), '1') ");
        stb.append(    "UNION  VALUES( cast('" + student._schregno + "' as varchar(8) ), '2')  ");
        stb.append(    "UNION  VALUES( cast('" + student._schregno + "' as varchar(8) ), '3')  ");
        stb.append(    "UNION  VALUES( cast('" + student._schregno + "' as varchar(8) ), '4')  ");
        stb.append(    "UNION  VALUES( cast('" + student._schregno + "' as varchar(8) ), '5')  ");
        stb.append(" ) ");

        // 対象生徒の時間割データ
        stb.append(", SCHEDULE_SCHREG_R AS(");
        stb.append("     SELECT  max(D1.SEMESTER_DETAIL) AS SEMESTER,  ");
        stb.append(             "T1.EXECUTEDATE, T1.PERIODCD ");
        stb.append(     "FROM    SCH_CHR_DAT T1, CHAIR_STD_DAT T2, SEMESTER_DETAIL_MST D1 ");
        stb.append(     "WHERE   T1.YEAR = '" + _param._year + "' ");
        stb.append(         "AND T1.SEMESTER <= '" + _param._semester + "' ");
//      stb.append(         "AND T1.EXECUTEDATE BETWEEN '" + _param._attendDate + "' AND '" + _param._date + "' ");
        stb.append(         "AND T1.EXECUTEDATE <= '" + _param._date + "' "); // TODO:集計テーブルが準備できるまでの対応。
        stb.append(         "AND T1.YEAR = D1.YEAR ");
        stb.append(         "AND T1.SEMESTER = D1.SEMESTER ");
        stb.append(         "AND T1.EXECUTEDATE BETWEEN D1.SDATE AND D1.EDATE ");
        stb.append(         "AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ");
        stb.append(         "AND T1.YEAR = T2.YEAR ");
        stb.append(         "AND T1.SEMESTER = T2.SEMESTER ");
        stb.append(         "AND T1.CHAIRCD = T2.CHAIRCD ");
        stb.append(         "AND EXISTS (SELECT 'X' FROM SCHNO T3 WHERE T3.SCHREGNO = T2.SCHREGNO) ");
            //                   １日出欠集計対象校時を条件に追加
            stb.append(     "AND EXISTS(SELECT 'X' FROM COURSE_MST T3, SCHREG_REGD_DAT T4 ");
            stb.append(                "WHERE T4.YEAR = '" + _param._year + "' ");
            stb.append(                  "AND T4.SCHREGNO = T2.SCHREGNO ");
            stb.append(                  "AND T4.SEMESTER = T2.SEMESTER ");
            stb.append(                  "AND T4.COURSECD = T3.COURSECD ");
            stb.append(                  "AND T1.PERIODCD BETWEEN T3.S_PERIODCD AND T3.E_PERIODCD) ");
        //                        転学・退学・転入・編入による不在期間はココで除外する
        stb.append(         "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST T3 ");
        stb.append(                        "WHERE   T3.SCHREGNO = T2.SCHREGNO ");
        stb.append(                            "AND (( ENT_DIV IN('4','5') AND EXECUTEDATE < ENT_DATE ) ");
        stb.append(                              "OR ( GRD_DIV IN('2','3') AND EXECUTEDATE > GRD_DATE )) ) ");
        stb.append(     "GROUP BY T1.EXECUTEDATE, T1.PERIODCD ");
        stb.append(" ) ");

        // 対象生徒の時間割データ(さらに留学・休学期間を除外)
        stb.append(", SCHEDULE_SCHREG AS(");
        stb.append("    SELECT  T1.SEMESTER, T1.EXECUTEDATE, T1.PERIODCD ");
        stb.append("    FROM    SCHEDULE_SCHREG_R T1 ");
        stb.append("    WHERE   NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT T4 ");
        stb.append("                       WHERE   EXISTS (SELECT 'X' FROM SCHNO T3 WHERE T3.SCHREGNO = T4.SCHREGNO) ");
        stb.append("                           AND T4.TRANSFERCD IN('1','2') AND T1.EXECUTEDATE BETWEEN T4.TRANSFER_SDATE AND T4.TRANSFER_EDATE ) ");
        stb.append(" ) ");

        // 対象生徒の出欠データ
        stb.append(",T_ATTEND_DAT AS(");
        stb.append(     "SELECT  T1.SEMESTER, T2.ATTENDDATE, T2.PERIODCD, T2.DI_CD ");
        stb.append(     "FROM    SCHEDULE_SCHREG T1, ATTEND_DAT T2 ");
        stb.append(     "WHERE   T2.YEAR = '" + _param._year + "' ");
//        stb.append(         "AND T2.ATTENDDATE BETWEEN '" + _param._attendDate + "' AND '" + _param._date + "' ");
        stb.append(         "AND T2.ATTENDDATE <= '" + _param._date + "' "); // TODO:集計テーブルが準備できるまでの対応。
        stb.append(         "AND EXISTS (SELECT 'X' FROM SCHNO T3 WHERE T3.SCHREGNO = T2.SCHREGNO) ");
        stb.append(         "AND T2.ATTENDDATE = T1.EXECUTEDATE ");
        stb.append(         "AND T2.PERIODCD = T1.PERIODCD ");
        stb.append(" ) ");

        // 対象生徒の出欠データ（忌引・出停した日）
        stb.append(", T_ATTEND_DAT_B AS(");
        stb.append(     "SELECT  ATTENDDATE, ");
        stb.append(     "MIN(PERIODCD) AS FIRST_PERIOD, ");
        stb.append(     "COUNT(DISTINCT PERIODCD) AS PERIOD_CNT ");
        stb.append(     "FROM    T_ATTEND_DAT ");
        stb.append(     "WHERE   DI_CD IN('2','3','9','10') ");
        stb.append(     "GROUP BY ATTENDDATE ");
        stb.append(" ) ");

        // 対象生徒の日単位の最小校時・最大校時・校時数
        stb.append(",T_PERIOD_CNT AS(");
        stb.append(     "SELECT  T1.SEMESTER, T1.EXECUTEDATE, ");
        stb.append(             "MIN(T1.PERIODCD) AS FIRST_PERIOD, ");
        stb.append(             "MAX(T1.PERIODCD) AS LAST_PERIOD, ");
        stb.append(             "COUNT(DISTINCT T1.PERIODCD) AS PERIOD_CNT ");
        stb.append(     "FROM    SCHEDULE_SCHREG T1 ");
        stb.append(     "GROUP BY T1.SEMESTER, T1.EXECUTEDATE ");
        stb.append(" ) ");

        if ("1".equals(_knjSchoolMst._syukessekiHanteiHou)) {
            //対象生徒の日単位のデータ（忌引・出停した日）
            stb.append(" , T_PERIOD_SUSPEND_MOURNING AS( ");
            stb.append(" SELECT ");
            stb.append("    T0.EXECUTEDATE ");
            stb.append(" FROM ");
            stb.append("    T_PERIOD_CNT T0, ");
            stb.append("    T_ATTEND_DAT_B T1 ");
            stb.append(" WHERE ");
            stb.append("        T0.EXECUTEDATE = T1.ATTENDDATE ");
            stb.append("    AND T0.FIRST_PERIOD = T1.FIRST_PERIOD ");
            stb.append("    AND T0.PERIOD_CNT = T1.PERIOD_CNT ");
            stb.append(" ) ");
        }

        // 留学日数を算出
        stb.append(",TRANSFER_SCHREG AS(");
        stb.append(     "SELECT  VALUE(T1.SEMESTER,'5')AS SEMESTER,COUNT(DISTINCT T1.EXECUTEDATE)AS TRANSFER_DATE ");
        stb.append(     "FROM    SCHEDULE_SCHREG_R T1, SCHREG_TRANSFER_DAT T2 ");
        stb.append(     "WHERE   EXISTS (SELECT 'X' FROM SCHNO T3 WHERE T3.SCHREGNO = T2.SCHREGNO) ");
        stb.append(         "AND T2.TRANSFERCD IN('1') ");
        stb.append(         "AND T1.EXECUTEDATE BETWEEN T2.TRANSFER_SDATE AND T2.TRANSFER_EDATE ");
        stb.append(     "GROUP BY GROUPING SETS(T1.SEMESTER,()) ");
        stb.append(" ) ");

        // 休学日数を算出
        stb.append(",OFFDAYS_SCHREG AS(");
        stb.append(     "SELECT  VALUE(T1.SEMESTER,'5')AS SEMESTER,COUNT(DISTINCT T1.EXECUTEDATE)AS OFFDAYS ");
        stb.append(     "FROM    SCHEDULE_SCHREG_R T1, SCHREG_TRANSFER_DAT T2 ");
        stb.append(     "WHERE   EXISTS (SELECT 'X' FROM SCHNO T3 WHERE T3.SCHREGNO = T2.SCHREGNO) ");
        stb.append(         "AND T2.TRANSFERCD IN('2') ");
        stb.append(         "AND T1.EXECUTEDATE BETWEEN T2.TRANSFER_SDATE AND T2.TRANSFER_EDATE ");
        stb.append(     "GROUP BY GROUPING SETS(T1.SEMESTER,()) ");
        stb.append(" ) ");

        // メイン表 // TODO:集計テーブルが準備できるまでの対応。
        stb.append(   "SELECT  TT0.SEMESTER, ");
//        //                     授業日数
//        stb.append(           "VALUE(TT1.LESSON,0) + VALUE(TT7.LESSON,0) AS LESSON, ");
//        //                     出停・忌引日数
//        stb.append(           "VALUE(TT3.SUSPEND,0) + VALUE(TT4.MOURNING,0) ");
//        stb.append(           " + VALUE(TT7.SUSPEND,0) + VALUE(TT7.MOURNING,0) AS SUSPEND, ");
//        //                     出席しなければならない日数
//        stb.append(           "VALUE(TT1.LESSON,0) - VALUE(TT3.SUSPEND,0) - VALUE(TT4.MOURNING,0) ");
//        stb.append(           " + VALUE(TT7.LESSON,0) - VALUE(TT7.SUSPEND,0) - VALUE(TT7.MOURNING,0) AS PRESENT, ");
//        //                     欠席日数
//        stb.append(           "VALUE(TT5.SICK,0) + VALUE(TT5.NOTICE,0) + VALUE(TT5.NONOTICE,0) ");
//        stb.append(           " + VALUE(TT7.ABSENT,0) AS ABSENCE, ");
//        //                     出席日数
//        stb.append(           "VALUE(TT1.LESSON,0) - VALUE(TT3.SUSPEND,0) - VALUE(TT4.MOURNING,0) ");
//        stb.append(           " + VALUE(TT7.LESSON,0) - VALUE(TT7.SUSPEND,0) - VALUE(TT7.MOURNING,0) ");
//        stb.append(           " - VALUE(TT5.SICK,0) - VALUE(TT5.NOTICE,0) - VALUE(TT5.NONOTICE,0) ");
//        stb.append(           " - VALUE(TT7.ABSENT,0) AS ATTEND, ");
//        //                     遅刻・早退回数
//        stb.append(           "VALUE(TT6.LATE,0) + VALUE(TT10.LATE,0) + VALUE(TT7.LATE,0) AS LATE, ");
//        stb.append(           "VALUE(TT6.EARLY,0) + VALUE(TT11.EARLY,0) + VALUE(TT7.EARLY,0) AS LEAVE ");
        //                     授業日数
        stb.append(           "VALUE(TT1.LESSON,0) ");
        if ("1".equals(_knjSchoolMst._semOffDays)) {
            stb.append(       "+ VALUE(TT13.OFFDAYS,0) ");
        }
        stb.append(           "AS LESSON, ");
        //                     出停・忌引日数
        stb.append(           "VALUE(TT3.SUSPEND,0) + VALUE(TT4.MOURNING,0) AS SUSPEND, ");
        //                     出席しなければならない日数
        stb.append(           "VALUE(TT1.LESSON,0) ");
        if ("1".equals(_knjSchoolMst._semOffDays)) {
            stb.append(       "+ VALUE(TT13.OFFDAYS,0) ");
        }
        stb.append(           " - VALUE(TT3.SUSPEND,0) - VALUE(TT4.MOURNING,0) AS PRESENT, ");
        //                     欠席日数
        stb.append(           "VALUE(TT5.SICK,0) + VALUE(TT5.NOTICE,0) + VALUE(TT5.NONOTICE,0) ");
        if ("1".equals(_knjSchoolMst._semOffDays)) {
            stb.append(       "+ VALUE(TT13.OFFDAYS,0) ");
        }
        stb.append(           " AS ABSENCE, ");
        //                     出席日数
        stb.append(           "VALUE(TT1.LESSON,0) - VALUE(TT3.SUSPEND,0) - VALUE(TT4.MOURNING,0) ");
        stb.append(           " - VALUE(TT5.SICK,0) - VALUE(TT5.NOTICE,0) - VALUE(TT5.NONOTICE,0) AS ATTEND, ");
        //                     留学・休学回数
        stb.append(           "VALUE(TT12.TRANSFER_DATE,0) AS ABROAD, ");
        stb.append(           "VALUE(TT13.OFFDAYS,0) AS OFFDAYS, ");
        //                     遅刻・早退回数
        stb.append(           "VALUE(TT6.LATE,0) + VALUE(TT10.LATE,0) AS LATE, ");
        stb.append(           "VALUE(TT6.EARLY,0) + VALUE(TT11.EARLY,0) AS LEAVE ");
        stb.append(   "FROM    SCHNO TT0 ");

        // 授業日数
        stb.append(   "LEFT JOIN(");
        stb.append(      "SELECT  VALUE(SEMESTER,'5')AS SEMESTER, COUNT(DISTINCT EXECUTEDATE) AS LESSON ");
        stb.append(      "FROM    T_PERIOD_CNT ");
        stb.append(      "GROUP BY GROUPING SETS(SEMESTER,()) ");
        stb.append(      ") TT1 ON TT0.SEMESTER = TT1.SEMESTER ");

        // 出停日数
        stb.append(   "LEFT JOIN(");
        stb.append(      "SELECT VALUE(W1.SEMESTER,'5')AS SEMESTER, COUNT(DISTINCT W1.ATTENDDATE) AS SUSPEND ");
        stb.append(      "FROM   T_ATTEND_DAT W1 ");
        stb.append(      "WHERE  W1.DI_CD IN ('2','9') ");
        if ("1".equals(_knjSchoolMst._syukessekiHanteiHou)) {
            stb.append("    AND  W1.ATTENDDATE IN (SELECT T0.EXECUTEDATE FROM T_PERIOD_SUSPEND_MOURNING T0) ");
        }
        stb.append(      "GROUP BY GROUPING SETS(W1.SEMESTER,()) ");
        stb.append(      ") TT3 ON TT0.SEMESTER = TT3.SEMESTER ");

        // 忌引日数
        stb.append(   "LEFT JOIN(");
        stb.append(      "SELECT VALUE(W1.SEMESTER,'5')AS SEMESTER, COUNT(DISTINCT W1.ATTENDDATE) AS MOURNING ");
        stb.append(      "FROM   T_ATTEND_DAT W1 ");
        stb.append(      "WHERE  W1.DI_CD IN ('3','10') ");
        if ("1".equals(_knjSchoolMst._syukessekiHanteiHou)) {
            stb.append("    AND  W1.ATTENDDATE IN (SELECT T0.EXECUTEDATE FROM T_PERIOD_SUSPEND_MOURNING T0) ");
        }
        stb.append(      "GROUP BY GROUPING SETS(W1.SEMESTER,()) ");
        stb.append(      ") TT4 ON TT0.SEMESTER = TT4.SEMESTER ");

        // 欠席日数
        stb.append("   LEFT JOIN(");
        stb.append("     SELECT  VALUE(T2.SEMESTER,'5')AS SEMESTER, ");
        stb.append("             SUM(CASE T1.DI_CD WHEN '4' THEN 1 WHEN '11' THEN 1 ELSE 0 END) AS SICK, ");
        stb.append("             SUM(CASE T1.DI_CD WHEN '5' THEN 1 WHEN '12' THEN 1 ELSE 0 END) AS NOTICE, ");
        stb.append("             SUM(CASE T1.DI_CD WHEN '6' THEN 1 WHEN '13' THEN 1 ELSE 0 END) AS NONOTICE ");
        stb.append("     FROM    ATTEND_DAT T1, ");
        stb.append("         (SELECT  S2.SEMESTER, S1.EXECUTEDATE ");
        if ("1".equals(_knjSchoolMst._syukessekiHanteiHou)) {
            stb.append("             ,S3.FIRST_PERIOD ");
        } else {
            stb.append("             ,S1.FIRST_PERIOD ");
        }
        stb.append("          FROM    T_PERIOD_CNT S1, ");
        stb.append("              (SELECT  SEMESTER, ATTENDDATE, ");
        stb.append("                       MIN(PERIODCD) AS FIRST_PERIOD, ");
        stb.append("                       COUNT(PERIODCD) AS PERIOD_CNT ");
        stb.append("               FROM    T_ATTEND_DAT ");
        stb.append("               WHERE   DI_CD IN ('4','5','6','11','12','13' ");
        if ("1".equals(_knjSchoolMst._syukessekiHanteiHou)) {
            stb.append("                            ,'2','9','3','10' ");
        }
        stb.append("                                ) ");
        stb.append("               GROUP BY SEMESTER, ATTENDDATE ");
        stb.append("              ) S2 ");
        if ("1".equals(_knjSchoolMst._syukessekiHanteiHou)) {
            stb.append("         INNER JOIN ( ");
            stb.append("               SELECT  SEMESTER, ATTENDDATE, ");
            stb.append("                       MIN(PERIODCD) AS FIRST_PERIOD, ");
            stb.append("                       COUNT(PERIODCD) AS PERIOD_CNT ");
            stb.append("               FROM    T_ATTEND_DAT ");
            stb.append("               WHERE   DI_CD IN ('4','5','6','11','12','13') ");
            stb.append("               GROUP BY SEMESTER, ATTENDDATE ");
            stb.append("             ) S3 ON S3.ATTENDDATE = S2.ATTENDDATE ");
        }
        stb.append("          WHERE   S1.EXECUTEDATE = S2.ATTENDDATE ");
        stb.append("              AND S1.FIRST_PERIOD = S2.FIRST_PERIOD ");
        stb.append("              AND S1.PERIOD_CNT = S2.PERIOD_CNT ");
        stb.append("         ) T2 ");
        stb.append("     WHERE   EXISTS (SELECT 'X' FROM SCHNO T3 WHERE T3.SCHREGNO = T1.SCHREGNO) ");
        stb.append("         AND T1.ATTENDDATE = T2.EXECUTEDATE ");
        stb.append("         AND T1.PERIODCD = T2.FIRST_PERIOD ");
        if ("1".equals(_knjSchoolMst._syukessekiHanteiHou)) {
            stb.append("    AND  T2.EXECUTEDATE NOT IN (SELECT T0.EXECUTEDATE FROM T_PERIOD_SUSPEND_MOURNING T0) ");
        }
        stb.append("     GROUP BY GROUPING SETS(T2.SEMESTER,()) ");
        stb.append("   )TT5 ON  TT0.SEMESTER = TT5.SEMESTER ");

        // 遅刻・早退回数
        stb.append(   "LEFT JOIN(");
        stb.append(      "SELECT  COUNT(T2.ATTENDDATE) AS LATE, COUNT(T3.ATTENDDATE) AS EARLY ");
        stb.append(             ",VALUE(T0.SEMESTER,'5')AS SEMESTER ");
        stb.append(      "FROM    T_PERIOD_CNT T0 ");
        stb.append(      "INNER JOIN(");
        stb.append(         "SELECT  W1.ATTENDDATE, COUNT(W1.PERIODCD) AS PERIOD_CNT ");
        stb.append(         "FROM    T_ATTEND_DAT W1 ");
        stb.append(         "WHERE   W1.DI_CD NOT IN ('0','14','15','16','23','24') ");
        if (!"1".equals(_knjSchoolMst._syukessekiHanteiHou)) {
            stb.append(             "AND NOT EXISTS(SELECT 'X' FROM T_ATTEND_DAT_B W2 ");
            stb.append(                            "WHERE W2.ATTENDDATE = W1.ATTENDDATE) ");
        }
        stb.append(         "GROUP BY W1.ATTENDDATE ");
        stb.append(         ")T1 ON T0.EXECUTEDATE = T1.ATTENDDATE AND T0.PERIOD_CNT != T1.PERIOD_CNT ");
        stb.append(      "LEFT JOIN(");
        stb.append(         "SELECT  ATTENDDATE ,PERIODCD ");
        stb.append(         "FROM    T_ATTEND_DAT ");
        stb.append(         "WHERE   DI_CD IN ('4','5','6','11','12','13') ");
        stb.append(         ")T2 ON T0.EXECUTEDATE  = T2.ATTENDDATE AND T0.FIRST_PERIOD = T2.PERIODCD ");
        stb.append(      "LEFT JOIN(");
        stb.append(         "SELECT  ATTENDDATE ,PERIODCD ");
        stb.append(         "FROM    T_ATTEND_DAT ");
        stb.append(         "WHERE   DI_CD IN ('4','5','6') ");
        stb.append(         ")T3 ON T0.EXECUTEDATE = T3.ATTENDDATE AND T0.LAST_PERIOD = T3.PERIODCD ");
        stb.append(      "GROUP BY GROUPING SETS(T0.SEMESTER,()) ");
        stb.append(      ")TT6 ON TT0.SEMESTER = TT6.SEMESTER ");

        // 遅刻回数
        stb.append(   "LEFT JOIN(");
        stb.append(      "SELECT  COUNT(T2.ATTENDDATE) AS LATE ");
        stb.append(             ",VALUE(T0.SEMESTER,'5')AS SEMESTER ");
        stb.append(      "FROM    T_PERIOD_CNT T0 ");
        stb.append(      "INNER JOIN(");
        stb.append(         "SELECT  ATTENDDATE ,PERIODCD ");
        stb.append(         "FROM    T_ATTEND_DAT W1 ");
        stb.append(         "WHERE   DI_CD IN ('15','23','24') ");
        if (!"1".equals(_knjSchoolMst._syukessekiHanteiHou)) {
            stb.append(             "AND NOT EXISTS(SELECT 'X' FROM T_ATTEND_DAT_B W2 ");
            stb.append(                            "WHERE W2.ATTENDDATE = W1.ATTENDDATE) ");
        }
        stb.append(         ")T2 ON T0.EXECUTEDATE  = T2.ATTENDDATE AND T0.FIRST_PERIOD = T2.PERIODCD ");
        stb.append(      "GROUP BY GROUPING SETS(T0.SEMESTER,()) ");
        stb.append(      ")TT10 ON TT0.SEMESTER = TT10.SEMESTER ");

        // 早退回数
        stb.append(   "LEFT JOIN(");
        stb.append(      "SELECT  COUNT(T2.ATTENDDATE) AS EARLY ");
        stb.append(             ",VALUE(T0.SEMESTER,'5')AS SEMESTER ");
        stb.append(      "FROM    T_PERIOD_CNT T0 ");
        stb.append(      "INNER JOIN(");
        stb.append(         "SELECT  ATTENDDATE ,PERIODCD ");
        stb.append(         "FROM    T_ATTEND_DAT W1 ");
        stb.append(         "WHERE   DI_CD IN ('16') ");
        if (!"1".equals(_knjSchoolMst._syukessekiHanteiHou)) {
            stb.append(             "AND NOT EXISTS(SELECT 'X' FROM T_ATTEND_DAT_B W2 ");
            stb.append(                            "WHERE W2.ATTENDDATE = W1.ATTENDDATE) ");
        }
        stb.append(         ")T2 ON T0.EXECUTEDATE  = T2.ATTENDDATE AND T0.LAST_PERIOD = T2.PERIODCD ");
        stb.append(      "GROUP BY GROUPING SETS(T0.SEMESTER,()) ");
        stb.append(      ")TT11 ON TT0.SEMESTER = TT11.SEMESTER ");

//        // 月別集計データから集計した表 // TODO:集計テーブルが準備できるまでの対応。
//        stb.append(   "LEFT JOIN(");
//        stb.append(      "SELECT  VALUE(SEMESTER,'5')AS SEMESTER, ");
//        stb.append(              "SUM(LESSON) AS LESSON,  ");
//        stb.append(              "SUM(MOURNING) AS MOURNING,  ");
//        stb.append(              "SUM(SUSPEND) AS SUSPEND,  ");
//        stb.append(              "SUM(ABSENT) AS ABSENT,  ");
//        stb.append(              "SUM(LATE) AS LATE,  ");
//        stb.append(              "SUM(EARLY) AS EARLY,  ");
//        stb.append(              "SUM(ABROAD) AS ABROAD,  ");
//        stb.append(              "SUM(OFFDAYS) AS OFFDAYS  ");
//        stb.append(     " FROM ( ");
//        stb.append(          "SELECT  CASE WHEN MONTH IN ('04','05','06') THEN '1' ");
//        stb.append(                       "WHEN MONTH IN ('07','08','09') THEN '2' ");
//        stb.append(                       "WHEN MONTH IN ('10','11','12') THEN '3' ");
//        stb.append(                       "WHEN MONTH IN ('01','02','03') THEN '4' ");
//        stb.append(                       "END AS SEMESTER,  ");
//        stb.append(                  "VALUE(SUM(LESSON),0) - VALUE(SUM(OFFDAYS),0) - VALUE(SUM(ABROAD),0) AS LESSON, ");
//        stb.append(                  "SUM(MOURNING) AS MOURNING, ");
//        stb.append(                  "SUM(SUSPEND) AS SUSPEND, ");
//        stb.append(                  "SUM( VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) ) AS ABSENT, ");
//        stb.append(                  "SUM(LATE) AS LATE, ");
//        stb.append(                  "SUM(EARLY) AS EARLY, ");
//        stb.append(                  "SUM(ABROAD) AS ABROAD, ");
//        stb.append(                  "SUM(OFFDAYS) AS OFFDAYS ");
//        stb.append(          "FROM    ATTEND_SEMES_DAT W1 ");
//        stb.append(          "WHERE   YEAR = '" + _param._year + "' AND ");
//        stb.append(                  "SEMESTER <= '" + _param._semester + "' AND ");
//        stb.append(                  "(CASE WHEN INT(W1.MONTH) < 4 THEN RTRIM(CHAR(INT(W1.SEMESTER) + 1 )) ELSE W1.SEMESTER END )||W1.MONTH <= '" + KNJC053_BASE.retSemesterMonthValue (_param._attendMonth) + "' AND ");
//        stb.append(                  "EXISTS (SELECT 'X' FROM SCHNO W2 WHERE W1.SCHREGNO = W2.SCHREGNO) ");
//        stb.append(          "GROUP BY MONTH ) T1 ");
//        stb.append(      "GROUP BY GROUPING SETS(SEMESTER,()) ");
//        stb.append(      ")TT7 ON TT0.SEMESTER = TT7.SEMESTER ");

        // 留学中の授業日数の表
        stb.append(" LEFT JOIN TRANSFER_SCHREG TT12 ON TT0.SEMESTER = TT12.SEMESTER ");
        // 休学中の授業日数の表
        stb.append(" LEFT JOIN OFFDAYS_SCHREG TT13 ON TT0.SEMESTER = TT13.SEMESTER ");

        stb.append(" ORDER BY TT0.SEMESTER");
        return stb.toString();
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * パラメータクラス
     */
    private class Param {
        private final String _year;
        private final String _nendo;
        private final String _semester;
        private final String _semeName;
        private final String _gradeHrClass;
        private final String _grade;
        private final String _staffcd;
        private final String _fullImagePath;
        private final String _date;
        private final String[] _schregno;

        private String _staffName;
        private String _attendDate;
        private String _attendMonth;
        private File _stamp;

        Param(
                final String year,
                final String semester,
                final String gradeHrClass,
                final String staffcd,
                final String fullImagePath,
                final String date,
                final String[] schregno
        ) {
            _year = year;
            _nendo = nao_package.KenjaProperties.gengou(Integer.parseInt(year)) + "年度";
            _semester = semester;
            _semeName = getSemeName(semester);
            _gradeHrClass = gradeHrClass;
            _grade = gradeHrClass.substring(0, 2);
            _staffcd = staffcd;
            _fullImagePath = fullImagePath;
            _date = KNJ_EditDate.H_Format_Haifun(date);
            _schregno = schregno;
        }

        public String getSemeName(final String semester) {
            return "1".equals(semester) ? "前　期" : "後　期";
        }

        public void loadConstant(final DB2UDB db2) {
            _staffName = createStaffName(db2);
            _stamp = createStamp();
            loadAttendDate(db2);
        }

        /**
         *  出欠集計端数処理用の日 _attendDate と月 _attendMonth をセットします。
         */
        public void loadAttendDate(final DB2UDB db2) {
            KNJDivideAttendDate obj = new KNJDivideAttendDate();
            obj.getDivideAttendDate(db2, _year, _semester, _date);
            _attendDate = obj.date;
            _attendMonth = obj.month;
        }

        public String createStaffName(final DB2UDB db2) {
            String staffName = null;
            PreparedStatement ps = null;
            ResultSet rs = null;

            final String sql = "SELECT STAFFNAME FROM STAFF_MST WHERE STAFFCD='" + _staffcd + "'";
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    staffName = rs.getString("STAFFNAME");
                }
            } catch (final Exception ex) {
                log.error("担任名のロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            log.debug("担任名:" + staffName);
            return staffName;
        }

        public File createStamp() {
            final String fileName = _fullImagePath + getStampName();

            final File f = new File(fileName);
            log.debug("学校ロゴ:" + f);

            return f;
        }

        private String getStampName() {
            return "SCHOOLLOGO.jpg";
        }

        public int getGrade() {
            return Integer.parseInt(_grade);
        }

        public boolean isJunior() {
            return getGrade() < 4;
        }

        public String getForms() {
            return isJunior() ? FORM_FILE1 : FORM_FILE2;
        }
    }

    private class Student {
        private final String _schregno;
        private final String _grade;
        private final String _hrclass;
        private final String _attendno;
        private final String _hrname;
        private final String _name;

        Student(
                final String schregno,
                final String grade,
                final String hrclass,
                final String attendno,
                final String hrname,
                final String name
        ) {
            _schregno = schregno;
            _grade = grade;
            _hrclass = hrclass;
            _attendno = attendno;
            _hrname = hrname + String.valueOf(getAttendno()) + "番";
            _name = name;
        }

        public int getGrade() {
            return Integer.parseInt(_grade);
        }

        public int getHrclass() {
            return Integer.parseInt(_hrclass);
        }

        public int getAttendno() {
            return Integer.parseInt(_attendno);
        }

        public boolean isJunior() {
            return getGrade() < 4;
        }

        public String toString() {
            return _schregno + ":" + _name;
        }
    }
}
