// kanji=漢字
/*
 * $Id: a0b9942e84101579473a5ab905a619ad6a8a3380 $
 *
 * Copyright(C) 2005-2008 ALP Okinawa Co.,Ltd. All rights reserved.
 */

/**
 *
 *  学校教育システム 賢者 [出欠管理] 状況出席率一覧
 *
 */

package servletpack.KNJC;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;


public class KNJC171A {

    private static final Log log = LogFactory.getLog(KNJC171A.class);

    /**
     *  KNJC.classから最初に起動されるクラス
     *
     */
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;              // Databaseクラスを継承したクラス
        boolean nonedata = false;

        // print svf設定
        response.setContentType("application/pdf");
        svf.VrInit(); //クラスの初期化

        // ＤＢ接続
        try {
            svf.VrSetSpoolFileStream(response.getOutputStream()); //PDFファイル名の設定

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);    //Databaseクラスを継承したクラス
            db2.open();

            final Param _param = new Param(request, db2);

            nonedata = printSvfMain(db2, svf, _param); //データ出力のメソッド

            db2.commit();
            db2.close();
        } catch (Exception ex) {
            log.error("exception:", ex);
            if (db2 != null) {
                db2.close();
            }
        }


        // 終了処理
        if (!nonedata) {
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note", "note");
            svf.VrEndPage();
        }
        svf.VrQuit();
    }

    /**
     *  見出し項目等
     */
    private void setHead(final DB2UDB db2, final Vrw32alp svf, final Param param) {

        svf.VrsOut("NENDO",   KNJ_EditDate.gengou(db2, Integer.parseInt(param._year))+"年度");//年度

        //  作成日(現在処理日)の取得 05/05/19Rivive
        try {
            svf.VrsOut("GRADE", param._gradeName1);
            SimpleDateFormat format = new SimpleDateFormat("yyyy.M.d");
            svf.VrsOut("DATE", format.format(Date.valueOf(param._loginDate)));
            svf.VrsOut("T_DATE", format.format(java.sql.Date.valueOf(KNJ_EditDate.H_Format_Haifun(param._date))));
        } catch (Exception ex) {
            log.error("error! ", ex);
        }

    }//setHead()の括り

    /**
     *  印刷処理
     *    学年別月別に統計データを配列へ保存後、配列データを印刷する
     */
    private boolean printSvfMain(final DB2UDB db2, final Vrw32alp svf, final Param param)
    {
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean nonedata = false;
        final Map homeRooms = new TreeMap();
        try {
            final String sql1 = prestatementStudent(param._year, param._semester, param._grade);
            log.debug("学年クラス取得 sql = " + sql1);
            ps = db2.prepareStatement(sql1);

            rs = ps.executeQuery();
            while (rs.next()) {
                nonedata = true;
                final String hrClassStr = rs.getString("HR_CLASS");
                final String hrClassName = rs.getString("HR_NAME");
                HomeRoom homeRoom = (HomeRoom) homeRooms.get(hrClassStr);
                if (null == homeRoom) {
                    homeRoom = new HomeRoom(hrClassStr, hrClassName);
                    homeRooms.put(hrClassStr, homeRoom);
                }

                final String schregno = rs.getString("SCHREGNO");
                final String name = rs.getString("NAME");
                final String attendNo = rs.getString("ATTENDNO");
                final String mark = rs.getString("MARK");
                final boolean hasGrdDiv = null != rs.getString("GRD_DIV");

                homeRoom.addStudent(new Student(schregno, name, attendNo, mark, hasGrdDiv));
            }
            DbUtils.closeQuietly(rs);

            if (nonedata == false) {
                log.debug("データがありません。");
                return false;
            }

            log.debug(" absent_cov = " + param._knjSchoolMst._absentCov + ", absent_cov_late = " + param._knjSchoolMst._absentCovLate + ", amari_kuriage = " + param._knjSchoolMst._amariKuriage);

            for (final Iterator it = homeRooms.keySet().iterator(); it.hasNext();) {
                final String name = (String) it.next();
                final HomeRoom homeRoom = (HomeRoom) homeRooms.get(name);
                final String sql2 = getAttendanceSql(
                        param._semesFlg,
                        param._defineSchool,
                        param._year,
                        param.SSEMESTER,
                        param._semester,
                        (String) param._hasuuMap.get("attendSemesInState"),
                        param._periodInState,
                        (String) param._hasuuMap.get("befDayFrom"),
                        (String) param._hasuuMap.get("befDayTo"),
                        (String) param._hasuuMap.get("aftDayFrom"),
                        (String) param._hasuuMap.get("aftDayTo"),
                        param._grade,
                        homeRoom._hrClass,
                        param
                );
                final PreparedStatement ps2 = db2.prepareStatement(sql2);
                log.debug("出欠を取得する " + homeRoom._hrClass + " sql = " + sql2);

                rs = ps2.executeQuery();

                while (rs.next()) {
                    final String schregno = rs.getString("SCHREGNO");
                    final Student student = homeRoom.getStudent(schregno);
                    if (student == null) {
                        continue;
                    }
                    student.setAttendance(rs.getDouble("LESSON"), rs.getDouble("ABSENT"), rs.getDouble("SUSPEND_MOURNING"), rs.getDouble("ATTEND"));
                }
                DbUtils.closeQuietly(rs);
            }

        } catch (Exception ex) {
            log.error("error! ", ex);
        } finally {
            DbUtils.closeQuietly(ps);
            db2.commit();
        }

        svf.VrSetForm("KNJC171A.frm", 1);
        int column=0;
        final int MAX_COLUMNS = 5;
        final int MAX_LINES = 40;
        for (final Iterator it = homeRooms.keySet().iterator(); it.hasNext();) {
            setHead(db2, svf, param); //見出し出力のメソッド
            column++;
            final String name = (String) it.next();
            final HomeRoom hr = (HomeRoom) homeRooms.get(name);
            log.debug("HR名称 = " + hr._name);
            int line = 1;
            for (final Iterator it2 = hr._studentList.iterator(); it2.hasNext();) {

                if (line > MAX_LINES) {
                    line = 1;
                    column++;
                }

                if (column > MAX_COLUMNS) {
                    svf.VrEndPage();
                    setHead( db2, svf, param ); //見出し出力のメソッド
                    column = 1;
                }
                final String columnStr = String.valueOf(column);
                svf.VrsOut("HR_NAME" + columnStr , hr._name);

                final Student student = (Student) it2.next();

                svf.VrsOutn("ATTENDNO" + columnStr, line, student._attendNo);
                svf.VrsOutn("SEX" + columnStr, line, student._mark);
                svf.VrsOutn("NAME" + columnStr, line, student._name);
                svf.VrsOutn("PERCENTAGE" + columnStr, line, student.getAttendancePercentage());
                line++;
            }
        }
        if (column != 0) {
            svf.VrEndPage();
        }

        return nonedata;
    }

    /** 指定した学年とHRクラスに所属する各学生の出席時数と欠席時数を取得するSQLを返す */
    private String getAttendanceSql(
            final boolean semesFlg,
            final KNJDefineSchool defineSchoolCode,
            final String year,
            final String sSemester,
            final String eSemester,
            final String semesInState,
            final String periodInState,
            final String befDayFrom,
            final String befDayTo,
            final String aftDayFrom,
            final String aftDayTo,
            final String grade,
            final String hrClass,
            final Param param) {

        final StringBuffer stb = new StringBuffer();
        stb.append( " WITH SCHNO AS(SELECT ");
        stb.append(    " W1.SCHREGNO, ");
        stb.append(    " W1.GRADE, ");
        stb.append(    " W1.SEMESTER, ");
        stb.append(    " W1.HR_CLASS, ");
        stb.append(    " W1.COURSECD, ");
        stb.append(    " W1.MAJORCD, ");
        stb.append(    " W1.COURSECODE ");
        stb.append(" FROM ");
        stb.append(    " SCHREG_REGD_DAT W1 ");
        stb.append(" WHERE ");
        stb.append(    " W1.YEAR = '" + year + "'     AND ");
        stb.append(    " W1.SEMESTER BETWEEN '" + sSemester + "' AND '" + eSemester + "'     AND ");
        stb.append(    " W1.GRADE = '" + grade + "' AND ");
        stb.append(    " W1.HR_CLASS = '" + hrClass + "' ");

        //端数計算有無の判定
        if (befDayFrom != null || aftDayFrom != null) {
            //対象生徒の時間割データ

            stb.append(" ), SCHEDULE_SCHREG AS(SELECT ");
            stb.append(    " T1.YEAR, ");
            stb.append(    " T2.SCHREGNO, ");
            stb.append(    " T1.SEMESTER, ");
            stb.append(    " T1.EXECUTEDATE, ");
            stb.append(    " T1.PERIODCD, ");
            stb.append(    " T1.CHAIRCD, ");
            stb.append(    " T1.DATADIV, ");
            stb.append(    " MAX(CASE WHEN T1.EXECUTEDATE BETWEEN T3.TRANSFER_SDATE AND T3.TRANSFER_EDATE THEN 1 ELSE 0 END) AS IS_ABROAD, ");
            stb.append(    " MAX(CASE WHEN T1.EXECUTEDATE BETWEEN T4.TRANSFER_SDATE AND T4.TRANSFER_EDATE THEN 1 ELSE 0 END) AS IS_OFFDAYS ");
            stb.append(" FROM ");
            stb.append(    " SCH_CHR_DAT T1 ");
            stb.append(    " INNER JOIN CHAIR_STD_DAT T2 ON ");
            stb.append(       " T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE AND ");
            stb.append(       " T1.YEAR = T2.YEAR AND ");
            stb.append(       " T1.SEMESTER = T2.SEMESTER AND ");
            stb.append(       " T1.CHAIRCD = T2.CHAIRCD ");
            stb.append(    " LEFT JOIN SCHREG_TRANSFER_DAT T3 ON T3.SCHREGNO = T2.SCHREGNO AND T3.TRANSFERCD IN ('1') ");
            stb.append(    " LEFT JOIN SCHREG_TRANSFER_DAT T4 ON T4.SCHREGNO = T2.SCHREGNO AND T4.TRANSFERCD IN ('2') ");
            stb.append(" WHERE ");
            stb.append(    " T1.YEAR = '" + year + "'     AND ");
            stb.append(    " T1.SEMESTER BETWEEN '" + sSemester + "' AND '" + eSemester + "' AND ");
            if (befDayFrom != null && aftDayFrom != null) {
                stb.append("    (T1.EXECUTEDATE BETWEEN '" + befDayFrom + "' AND '" + befDayTo + "' ");
                stb.append("         OR T1.EXECUTEDATE BETWEEN '" + aftDayFrom + "' AND '" + aftDayTo + "') AND ");
            } else if (befDayFrom != null) {
                stb.append("    T1.EXECUTEDATE BETWEEN '" + befDayFrom + "' AND '" + befDayTo + "' AND ");
            } else if (aftDayFrom != null) {
                stb.append("    T1.EXECUTEDATE BETWEEN '" + aftDayFrom + "' AND '" + aftDayTo + "' AND ");
            }
            if (defineSchoolCode != null && defineSchoolCode.usefromtoperiod)
                stb.append(" T1.PERIODCD IN " + periodInState + " AND ");
            stb.append(    " T2.SCHREGNO IN(SELECT ");
            stb.append(                        " SCHREGNO ");
            stb.append(                    " FROM ");
            stb.append(                        " SCHNO ");
            stb.append(                    " GROUP BY ");
            stb.append(                        " SCHREGNO ");
            stb.append(                    " ) ");
            stb.append(    " AND NOT EXISTS(SELECT ");
            stb.append(                    " 'X' ");
            stb.append(                " FROM ");
            stb.append(                    " SCHREG_BASE_MST T3 ");
            stb.append(                " WHERE ");
            stb.append(                    " T3.SCHREGNO = T2.SCHREGNO AND ");
            stb.append(                    " ((ENT_DIV IN('4','5') AND EXECUTEDATE < ENT_DATE) ");
            stb.append(                              " OR (GRD_DIV IN('2','3') AND EXECUTEDATE > GRD_DATE)) ");
            stb.append(                " ) ");
            stb.append("    AND NOT EXISTS(SELECT ");
            stb.append("                       'X' ");
            stb.append("                   FROM ");
            stb.append("                       ATTEND_DAT T4 ");
            stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT ATDD ON ATDD.YEAR = '" + year + "' ");
            stb.append("                             AND ATDD.DI_CD = T4.DI_CD ");
            stb.append("                   WHERE ");
            stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
            stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
            stb.append("                       AND ATDD.REP_DI_CD = '27' "); // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
            stb.append("                  ) ");
            // 勤怠コード'28'は時間割にカウントしない
            stb.append("    AND NOT EXISTS(SELECT ");
            stb.append("                       'X' ");
            stb.append("                   FROM ");
            stb.append("                       ATTEND_DAT T4 ");
            stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT ATDD ON ATDD.YEAR = '" + year + "' ");
            stb.append("                             AND ATDD.DI_CD = T4.DI_CD ");
            stb.append("                   WHERE ");
            stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
            stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
            stb.append("                       AND ATDD.REP_DI_CD = '28' ");
            stb.append("                  ) ");
            if (param._hasSchChrDatExecutediv) {
                stb.append("        AND VALUE(T1.EXECUTEDIV, '0') <> '2' "); // 休講は対象外
            }
            stb.append(" GROUP BY ");
            stb.append(     " T1.YEAR, ");
            stb.append(    " T2.SCHREGNO, ");
            stb.append(    " T1.SEMESTER, ");
            stb.append(    " T1.EXECUTEDATE, ");
            stb.append(    " T1.PERIODCD, ");
            stb.append(    " T1.CHAIRCD, ");
            stb.append(    " T1.DATADIV ");

            stb.append(" ), TEST_COUNTFLG AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.EXECUTEDATE, ");
            stb.append("         T1.PERIODCD, ");
            stb.append("         T1.CHAIRCD, ");
            stb.append("         '2' AS DATADIV ");
            stb.append("     FROM ");
            stb.append("         SCH_CHR_TEST T1, ");
            if ("TESTITEM_MST_COUNTFLG".equals(param._useTestCountflg)) {
                stb.append("         TESTITEM_MST_COUNTFLG T2 ");
                stb.append("     WHERE ");
                stb.append("         T2.YEAR       = T1.YEAR ");
            } else if ("TESTITEM_MST_COUNTFLG_NEW_SDIV".equals(param._useTestCountflg)) {
                stb.append("         TESTITEM_MST_COUNTFLG_NEW_SDIV T2 ");
                stb.append("     WHERE ");
                stb.append("         T2.YEAR       = T1.YEAR ");
                stb.append("         AND T2.SEMESTER   = T1.SEMESTER ");
                stb.append("         AND T2.SCORE_DIV  = '01' ");
            } else {
                stb.append("         TESTITEM_MST_COUNTFLG_NEW T2 ");
                stb.append("     WHERE ");
                stb.append("         T2.YEAR       = T1.YEAR ");
                stb.append("         AND T2.SEMESTER   = T1.SEMESTER ");
            }
            stb.append("         AND T2.TESTKINDCD = T1.TESTKINDCD ");
            stb.append("         AND T2.TESTITEMCD = T1.TESTITEMCD ");
            stb.append("         AND T2.COUNTFLG   = '0' ");

            //端数計算有無の判定
            //対象生徒の出欠データ
            stb.append(" ), T_ATTEND_DAT AS (SELECT ");
            stb.append(    " T0.SCHREGNO, ");
            stb.append(    " T0.SEMESTER, ");
            stb.append(    " T0.EXECUTEDATE, ");
            stb.append(    " T0.PERIODCD, ");
            stb.append(    " T0.CHAIRCD, ");
            stb.append(    " T0.DATADIV, ");
            stb.append(    " T3.SUBCLASSCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(         " T3.CLASSCD, ");
                stb.append(         " T3.SCHOOL_KIND, ");
                stb.append(         " T3.CURRICULUM_CD, ");
            }
            stb.append(    " CASE WHEN T0.IS_ABROAD = 1 THEN 'X99' ");
            stb.append(    "      WHEN T0.IS_OFFDAYS = 1 THEN 'X98' ELSE VALUE(CASE WHEN ATDD.ATSUB_REPL_DI_CD IS NOT NULL THEN ATDD.ATSUB_REPL_DI_CD ELSE T1.DI_CD END, '0') END AS DI_CD "); // 留学中の授業時数
            stb.append(" FROM ");
            stb.append(    " SCHEDULE_SCHREG T0 ");
            stb.append(    " LEFT JOIN ATTEND_DAT T1 ON ");
            stb.append(         " T1.SCHREGNO = T0.SCHREGNO AND ");
            stb.append(         " T1.ATTENDDATE = T0.EXECUTEDATE AND ");
            stb.append(         " T1.PERIODCD = T0.PERIODCD AND ");
            stb.append(         " T1.CHAIRCD = T0.CHAIRCD ");
            stb.append(    " LEFT JOIN ATTEND_DI_CD_DAT ATDD ON ATDD.YEAR = '" + year + "' AND ATDD.DI_CD = T1.DI_CD ");
            stb.append(    " LEFT JOIN NAME_MST C001 ON C001.NAMECD1 = 'C001' AND T1.DI_CD = C001.NAMECD2 ");
            stb.append(    " LEFT JOIN CHAIR_DAT T2 ON ");
            stb.append(         " T2.YEAR = T0.YEAR AND ");
            stb.append(         " T2.SEMESTER = T0.SEMESTER AND ");
            stb.append(         " T2.CHAIRCD = T0.CHAIRCD ");
            stb.append(    " LEFT JOIN SUBCLASS_MST T3 ON ");
            stb.append(         " T3.SUBCLASSCD = T2.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(         " AND T3.CLASSCD = T2.CLASSCD ");
                stb.append(         " AND T3.SCHOOL_KIND = T2.SCHOOL_KIND ");
                stb.append(         " AND T3.CURRICULUM_CD = T2.CURRICULUM_CD ");
            }
            stb.append(" WHERE ");
            stb.append(    " T0.YEAR = '" + year + "' ");
            if (befDayFrom != null && aftDayFrom != null) {
                stb.append("    AND (T0.EXECUTEDATE BETWEEN '" + befDayFrom + "' AND '" + befDayTo + "' ");
                stb.append("         OR T0.EXECUTEDATE BETWEEN '" + aftDayFrom + "' AND '" + aftDayTo + "') ");
            } else if (befDayFrom != null) {
                stb.append("    AND T0.EXECUTEDATE BETWEEN '" + befDayFrom + "' AND '" + befDayTo + "' ");
            } else if (aftDayFrom != null) {
                stb.append("    AND T0.EXECUTEDATE BETWEEN '" + aftDayFrom + "' AND '" + aftDayTo + "' ");
            }
            //生徒・科目・学期別欠課集計の表（出欠データと集計テーブルを合算）
            stb.append("), SCH_ATTEND_SUM AS(");
            stb.append(" SELECT ");
            stb.append(    " T1.SCHREGNO, ");
            stb.append(    " T1.SUBCLASSCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(         " T1.CLASSCD, ");
                stb.append(         " T1.SCHOOL_KIND, ");
                stb.append(         " T1.CURRICULUM_CD, ");
            }
            stb.append(    " T1.SEMESTER, ");
            stb.append(    " COUNT(*) AS LESSON1, ");
            stb.append(    " COUNT(*) ");
            stb.append(    " - SUM(CASE WHEN T1.DI_CD IN ('X99') THEN 1 ELSE 0 END) "); // 留学中の授業時数
            if (!"1".equals(param._knjSchoolMst._subOffDays)) {
                stb.append(    " - SUM(CASE WHEN T1.DI_CD IN ('X98') THEN 1 ELSE 0 END) "); // 休学中の授業時数
            }
            stb.append(    " AS ATTEND1, ");
            stb.append(    " SUM(CASE WHEN ATDD.REP_DI_CD IN('2','9','3','10' "); // 出停・忌引の授業時数
            if ("1".equals(param._knjSchoolMst._subVirus)) {
                stb.append(    ",'19','20' "); // 出停(伝染病)の授業時数
            }
            if ("1".equals(param._knjSchoolMst._subKoudome)) {
                stb.append(    ",'25','26' "); // 出停(交止)の授業時数
            }
            stb.append(    ") THEN 1 ELSE 0 END) ");
            stb.append(    " AS SUSPEND_MOURNING, ");
            stb.append(    " SUM(CASE WHEN ATDD.REP_DI_CD IN('4','5','6','14','11','12','13' ");
            if ("1".equals(param._knjSchoolMst._subAbsent)) {
                stb.append(    ",'1','8' "); // 公欠の授業時数
            }
            if ("1".equals(param._knjSchoolMst._subSuspend)) {
                stb.append(    ",'2','9' "); // 出停の授業時数
            }
            if ("1".equals(param._knjSchoolMst._subMourning)) {
                stb.append(    ",'3','10' "); // 忌引の授業時数
            }
            if ("1".equals(param._knjSchoolMst._subOffDays)) {
                stb.append(    ",'98' "); // 休学中の授業時数
            }
            if ("1".equals(param._knjSchoolMst._subVirus)) {
                stb.append(    ",'19','20' "); // 出停(伝染病)の授業時数
            }
            if ("1".equals(param._knjSchoolMst._subKoudome)) {
                stb.append(    ",'25','26' "); // 出停(交止)の授業時数
            }
            stb.append(    ") THEN 1 ELSE 0 END) ");
            stb.append(    " AS ABSENT1, ");
            stb.append(    " SUM(CASE WHEN ATDD.REP_DI_CD IN('15','16','23','24') THEN SMALLINT(VALUE(ATDD.MULTIPLY,'1')) ELSE 0 END)AS LATE_EARLY ");
            stb.append(" FROM ");
            stb.append(    " T_ATTEND_DAT T1 ");
            stb.append("     LEFT JOIN ATTEND_DI_CD_DAT ATDD ON ATDD.YEAR = '" + year + "' AND ATDD.DI_CD = T1.DI_CD ");
            stb.append(    ", SCHNO T0 ");
            stb.append(" WHERE ");
            stb.append(    " T1.SCHREGNO = T0.SCHREGNO         AND ");
            stb.append(    " T1.SEMESTER = T0.SEMESTER         AND ");
            if (defineSchoolCode.useschchrcountflg) {
                stb.append(    " NOT EXISTS(SELECT ");
                stb.append(                    " 'X' ");
                stb.append(                " FROM ");
                stb.append(                    " SCH_CHR_COUNTFLG T4 ");
                stb.append(                " WHERE ");
                stb.append(                    " T4.EXECUTEDATE = T1.EXECUTEDATE AND ");
                stb.append(                    " T4.PERIODCD = T1.PERIODCD AND ");
                stb.append(                    " T4.CHAIRCD = T1.CHAIRCD AND ");
                stb.append(                    " T1.DATADIV IN ('0', '1') AND ");
                stb.append(                    " T4.GRADE = T0.GRADE AND ");
                stb.append(                    " T4.HR_CLASS = T0.HR_CLASS AND ");
                stb.append(                    " T4.COUNTFLG = '0' ");
                stb.append(                " ) ");
                stb.append(    " AND NOT EXISTS(SELECT ");
                stb.append(                    " 'X' ");
                stb.append(                " FROM ");
                stb.append(                    " TEST_COUNTFLG TEST ");
                stb.append(                " WHERE ");
                stb.append(                    " TEST.EXECUTEDATE  = T1.EXECUTEDATE ");
                stb.append(                    " AND TEST.PERIODCD = T1.PERIODCD ");
                stb.append(                    " AND TEST.CHAIRCD  = T1.CHAIRCD ");
                stb.append(                    " AND TEST.DATADIV  = T1.DATADIV) ");
            }
            stb.append(" GROUP BY ");
            stb.append(    " T1.SCHREGNO, ");
            stb.append(    " T1.SUBCLASSCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(         " T1.CLASSCD, ");
                stb.append(         " T1.SCHOOL_KIND, ");
                stb.append(         " T1.CURRICULUM_CD, ");
            }
            stb.append(    " T1.SEMESTER ");
            stb.append(" UNION ALL      ");
        }

        if (befDayFrom == null && aftDayFrom == null) {
            stb.append(" ), SCH_ATTEND_SUM AS (  ");
        }

        stb.append(" SELECT ");
        stb.append(    " T1.SCHREGNO, ");
        stb.append(    " T1.SUBCLASSCD, ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(         " T1.CLASSCD, ");
            stb.append(         " T1.SCHOOL_KIND, ");
            stb.append(         " T1.CURRICULUM_CD, ");
        }
        stb.append(    " SEMESTER, ");
        stb.append(    " SUM(VALUE(LESSON,0)) AS LESSON1, ");
        stb.append(    " SUM(VALUE(LESSON,0) - VALUE(ABROAD,0) ");
        if (!"1".equals(param._knjSchoolMst._subOffDays)) {
            stb.append(" - VALUE(OFFDAYS,0) ");
        }
        stb.append(    " ) AS ATTEND1, ");
        stb.append(    " SUM(VALUE(SUSPEND,0) + VALUE(MOURNING,0) ");
        if ("true".equals(param._useVirus)) {
            stb.append(    " + VALUE(VIRUS,0) ");
        }
        if ("true".equals(param._useKoudome)) {
            stb.append(    " + VALUE(KOUDOME,0) ");
        }
        stb.append(    " ) AS SUSPEND_MOURNING, ");
        stb.append(    " SUM(VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) ");
        if ("1".equals(param._knjSchoolMst._subOffDays)) {
            stb.append(" + VALUE(OFFDAYS,0) ");
        }
        if ("1".equals(param._knjSchoolMst._subAbsent)) {
            stb.append(" + VALUE(ABSENT,0) ");
        }
        if ("1".equals(param._knjSchoolMst._subMourning)) {
            stb.append(" + VALUE(MOURNING,0) ");
        }
        if ("1".equals(param._knjSchoolMst._subSuspend)) {
            stb.append(" + VALUE(SUSPEND,0) ");
        }
        if ("1".equals(param._knjSchoolMst._subVirus)) {
            stb.append(" + VALUE(VIRUS,0) ");
        }
        if ("1".equals(param._knjSchoolMst._subKoudome)) {
            stb.append(" + VALUE(KOUDOME,0) ");
        }
        stb.append(    " + VALUE(NURSEOFF,0)) ");
        stb.append(    " AS ABSENT1, ");
        stb.append(    " SUM(VALUE(LATE,0) + VALUE(EARLY,0)) AS LATE_EARLY ");
        stb.append(" FROM ");
        stb.append(    " ATTEND_SUBCLASS_DAT T1 ");
        stb.append(" WHERE ");
        stb.append(    " YEAR = '" + year+ "' AND ");
        stb.append(    " SEMESTER BETWEEN '" + sSemester + "' AND '" + eSemester + "' AND ");
        stb.append(    " T1.SEMESTER || T1.MONTH  IN " + semesInState + "  AND ");
        stb.append(    " EXISTS(SELECT ");
        stb.append(                " 'X' ");
        stb.append(            " FROM ");
        stb.append(                " SCHNO T2 ");
        stb.append(            " WHERE ");
        stb.append(                " T2.SCHREGNO = T1.SCHREGNO ");
        stb.append(            " GROUP BY ");
        stb.append(                " SCHREGNO ");
        stb.append(            " ) ");
        stb.append(" GROUP BY ");
        stb.append(    " T1.SCHREGNO, ");
        stb.append(    " T1.SEMESTER, ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(         " T1.CLASSCD, ");
            stb.append(         " T1.SCHOOL_KIND, ");
            stb.append(         " T1.CURRICULUM_CD, ");
        }
        stb.append(    " T1.SUBCLASSCD ");

        //ペナルティー欠課を加味した生徒欠課集計の表（出欠データと集計テーブルを合算）
        stb.append("), ATTEND_B AS(");
        // 学期の欠課時数は学期別で換算したペナルティ欠課を加算、学年の欠課時数は年間で換算する
        //absent_cov = 1 : 学期でペナルティ欠課を算出する場合
        //absent_cov = 2 : 通年でペナルティ欠課を算出する場合
        final int absent_cov = null == param._knjSchoolMst._absentCov ? 0 : Integer.parseInt(param._knjSchoolMst._absentCov);
        final int absent_cov_late = null == param._knjSchoolMst._absentCovLate ? 1 : Integer.parseInt(param._knjSchoolMst._absentCovLate);

        if (absent_cov != 0) {
            stb.append(" SELECT ");
            stb.append(    " SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(         " CLASSCD, ");
                stb.append(         " SCHOOL_KIND, ");
                stb.append(         " CURRICULUM_CD, ");
            }
            stb.append(    " SUBCLASSCD, ");
            stb.append(    " VALUE(SUM(LESSON1),0) AS LESSON_SEM9, ");
            stb.append(    " VALUE(SUM(ATTEND1),0) AS ATTEND_SEM9, ");
            stb.append(    " VALUE(SUM(SUSPEND_MOURNING),0) AS SUSPEND_MOURNING_SEM9, ");
            if (absent_cov == 5) {
                final int amariKuriage = null == param._knjSchoolMst._amariKuriage ? 1 : Integer.parseInt(param._knjSchoolMst._amariKuriage);

                stb.append(    " VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / " + absent_cov_late + " ");
                stb.append(    " +  (CASE WHEN MOD(VALUE(SUM(LATE_EARLY),0) , " + absent_cov_late + ") >= " + amariKuriage + " THEN 1 ELSE 0 END) AS ABSENT_SEM9 ");
            } else if (absent_cov == 3 || absent_cov == 4) {
                stb.append(    " VALUE(SUM(ABSENT1),0) + ROUND(DECIMAL(VALUE(SUM(LATE_EARLY),0)) / " + absent_cov_late + ", 1) AS ABSENT_SEM9 ");
            } else {
                stb.append(    " VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / " + absent_cov_late + " AS ABSENT_SEM9 ");
            }
            stb.append(" FROM ");
            stb.append(    " SCH_ATTEND_SUM T1 ");
            stb.append(" GROUP BY ");
            stb.append(    " SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(         " CLASSCD, ");
                stb.append(         " SCHOOL_KIND, ");
                stb.append(         " CURRICULUM_CD, ");
            }
            stb.append(    " SUBCLASSCD ");
            if (absent_cov == 1 || absent_cov == 3) {
                stb.append(    ", SEMESTER ");
            }
        } else {
            //ペナルティ欠課なしの場合
            stb.append("    SELECT ");
            stb.append("        SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(         " CLASSCD, ");
                stb.append(         " SCHOOL_KIND, ");
                stb.append(         " CURRICULUM_CD, ");
            }
            stb.append("        SUBCLASSCD, ");
            stb.append("        VALUE(SUM(LESSON1),0) AS LESSON_SEM9, ");
            stb.append("        VALUE(SUM(ATTEND1),0) AS ATTEND_SEM9, ");
            stb.append("        VALUE(SUM(SUSPEND_MOURNING),0) AS SUSPEND_MOURNING_SEM9, ");
            stb.append("        VALUE(SUM(ABSENT1),0) AS ABSENT_SEM9 ");
            stb.append("    FROM ");
            stb.append("        SCH_ATTEND_SUM T1 ");
            stb.append("    GROUP BY ");
            stb.append("        SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(         " CLASSCD, ");
                stb.append(         " SCHOOL_KIND, ");
                stb.append(         " CURRICULUM_CD, ");
            }
            stb.append("        SUBCLASSCD ");
        }

        // メイン表
        stb.append(") SELECT ");
        stb.append(    " T1.SCHREGNO, ");
        stb.append(    " SUM(T1.LESSON_SEM9) AS LESSON, ");
        stb.append(    " SUM(T1.ATTEND_SEM9) AS ATTEND, ");
        stb.append(    " SUM(T1.SUSPEND_MOURNING_SEM9) AS SUSPEND_MOURNING, ");
        stb.append(    " SUM(T1.ABSENT_SEM9) AS ABSENT ");
        stb.append(" FROM ");
        stb.append(    " ATTEND_B T1 ");
        stb.append(" GROUP BY ");
        stb.append(    " T1.SCHREGNO ");
        stb.append(" ORDER BY ");
        stb.append(    " T1.SCHREGNO ");

        return stb.toString();
    }

    // 学生のデータを取得するSQL
    private String prestatementStudent(final String year, final String semester, final String grade) {
        StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append(    " T1.GRADE, ");
        stb.append(    " T1.HR_CLASS, ");
        stb.append(    " T1.SCHREGNO, ");
        stb.append(    " T3.NAME, ");
        stb.append(    " CASE T3.SEX WHEN '2' THEN '○' ELSE NULL END AS MARK, ");
        stb.append(    " T1.ATTENDNO, ");
        stb.append(    " T2.HR_NAME, ");
        stb.append(    " T3.GRD_DIV ");
        stb.append(" FROM ");
        stb.append(    " SCHREG_REGD_DAT T1 ");
        stb.append(    " LEFT JOIN SCHREG_REGD_HDAT T2 ON ");
        stb.append(        " T1.YEAR = T2.YEAR AND ");
        stb.append(        " T1.SEMESTER = T2.SEMESTER AND ");
        stb.append(        " T1.GRADE = T2.GRADE AND ");
        stb.append(        " T1.HR_CLASS = T2.HR_CLASS ");
        stb.append(    " LEFT JOIN SCHREG_BASE_MST T3 ON ");
        stb.append(        " T1.SCHREGNO = T3.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append(    " T1.YEAR = '" + year + "' AND ");
        stb.append(    " T1.SEMESTER = '" + semester + "' AND ");
        stb.append(    " T1.GRADE = '" + grade + "' ");
        stb.append(" ORDER BY  ");
        stb.append(    " T1.HR_CLASS, ATTENDNO ");

        return stb.toString();
    }

    /** HRクラス */
    private class HomeRoom {
        final String _hrClass;
        final String _name;
        final List _studentList;
        public HomeRoom(
                final String hrClass,
                final String hrName) {
            _hrClass = hrClass;
            _name = hrName;
            _studentList = new ArrayList();
        }
        /** HRに学生を追加する */
        public void addStudent(final Student st) {
            _studentList.add(st);
        }
        /** 指定の学籍番号の学生を得る */
        public Student getStudent(final String schregno) {
            for (final Iterator it = _studentList.iterator(); it.hasNext();) {
                Student student = (Student) it.next();
                if (student._schregno.equals(schregno)) {
                    return student;
                }
            }
            return null;
        }
    }

    /** 学生クラス */
    private class Student {
        final String _schregno;
        final String _name;
        final String _attendNo;
        final String _mark;
        /** 除籍(卒業)区分があるか */
        final boolean _hasGrdDiv;
        /** 欠席時数 */
        private double _absent;
        /** 授業時数 */
        private double _attend;
        /** 出停忌引き時数 */
        private double _suspendmourning;
        /** LESSON */
        private double _lesson;
        public Student(
                final String schregno,
                final String name,
                final String attendNo,
                final String mark,
                final boolean hasGrdDiv) {
            _schregno = schregno;
            _name = name;
            _attendNo = attendNo;
            _mark = mark;
            _hasGrdDiv = hasGrdDiv;
        }
        /** 出欠をセットする
         * @param lesson 授業時数
         * @param absent 欠席時数
         * @param attend 出席時数
         */
        public void setAttendance(double lesson, double absent, double suspendmourning, double attend) {
            if (_hasGrdDiv) {
                log.debug(" [除籍区分有] " + _schregno + ", " + _name + "(" + _schregno + " , " + _name  +  " 全 = " + lesson + ", 欠 = " + absent + " , 出 = " + attend + ")");
                return;
            }

            log.debug(_schregno + " , " + _name  +  " 全 = " + lesson + ", 欠 = " + absent + ", 出停忌引き = " + suspendmourning + ", 出 = " + attend);
            _lesson = lesson;
            _absent = absent;
            _suspendmourning = suspendmourning;
            _attend = attend;
        }

        /** 出欠の百分率を得る
         *  (除籍区分がある、もしくはデータが無い なら空白)
         */
        public String getAttendancePercentage() {
            if (_hasGrdDiv) {
                return "";
            }
            if (_attend == 0 || _attend - _suspendmourning - _absent <= 0) {
                if (_attend != 0) {
                    // 授業時数があるならレコードはある
                    return "0.0";
                }
                log.debug("出席のレコードがありません。 学籍番号=" + _schregno + ", 名前=" + _name);
                return "";
            }
            BigDecimal percentage = new BigDecimal(100.0 * (_attend - _suspendmourning - _absent)).divide(new BigDecimal(_attend - _suspendmourning), 1, BigDecimal.ROUND_HALF_DOWN);
            return new DecimalFormat("0.0").format(percentage);
        }
    }

    /** パラメータ */
    private static class Param {
        final String _grade;
        final String _gradeName1;
        final String _date;
        final String _year;
        final String _semester;
        final String _loginDate;
        final String _dbname;
        final String _prgid;
        final String _chkSdate;
        final String _chkEdate;
        final String _useCurriculumcd;
        final String _useVirus;
        final String _useKoudome;
        private final String _useTestCountflg;
        private final boolean _hasSchChrDatExecutediv;

        private KNJSchoolMst _knjSchoolMst;

        // 出欠集計共通端数計算メソッド用引数
        private KNJDefineSchool _defineSchool;
        private String _periodInState;
        private Map _attendSemesMap;
        private Map _hasuuMap;
        private boolean _semesFlg;
        private String _sDate;
        private final String SSEMESTER = "1";

        Param(final HttpServletRequest request, final DB2UDB db2) {
            _grade = request.getParameter("GRADE");
            _date = request.getParameter("DATE");
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _gradeName1 = getGradename(db2);
            _loginDate = request.getParameter("LOGIN_DATE");
            _dbname = request.getParameter("DBNAME");
            _prgid = request.getParameter("PRGID");
            _chkSdate = request.getParameter("CHK_SDATE");
            _chkEdate = request.getParameter("CHK_EDATE");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
            _useTestCountflg = request.getParameter("useTestCountflg");
            _hasSchChrDatExecutediv = setTableColumnCheck(db2, "SCH_CHR_DAT", "EXECUTEDIV");
            loadAttendSemesArgument(db2);

            try {
                final Map smParamMap = new HashMap();
                if (KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND")) {
                	final String schoolKind = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' "));
					smParamMap.put("SCHOOL_KIND", schoolKind);
                }
                _knjSchoolMst = new KNJSchoolMst(db2, _year, smParamMap);
                log.debug(" 休学時数         = 欠課時数に" + ("1".equals(_knjSchoolMst._subOffDays) ? "含める" : "含めない"));
                log.debug(" 出停(伝染病)時数 = " + ("1".equals(_knjSchoolMst._subVirus) ? "使用する" : "使用しない"));
            } catch (Exception e) {
                log.fatal("exception!", e);
            }
        }


        private String getGradename(final DB2UDB db2) {
            String retStr = "";
            final String sql = "SELECT GRADE_NAME1 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ";

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    retStr = StringUtils.defaultString(rs.getString("GRADE_NAME1"));
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retStr;
        }


        private KNJDefineSchool setClasscode0(final DB2UDB db2) {
            KNJDefineSchool definecode = null;
            try {
                definecode = new KNJDefineSchool();
                definecode.defineCode(db2, _year);         //各学校における定数等設定
            } catch (Exception ex) {
                log.warn("semesterdiv-get error!", ex);
            }
            return definecode;
        }

        /**
         * 名称マスタ NAMECD1='Z010' NAMECD2='00'読込
         */
        private String setZ010Name1(final DB2UDB db2) {
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
         * 出欠端数計算に必要な引数をロードする
         * @param db2 DB
         */
        private void loadAttendSemesArgument(final DB2UDB db2) {
            // 出欠の情報
            _defineSchool = new KNJDefineSchool();
            _defineSchool.defineCode(db2, _year);
            final KNJDefineSchool definecode0 = setClasscode0(db2);
            final String z010Name1 = setZ010Name1(db2);
            try {
                _periodInState = AttendAccumulate.getPeiodValue(db2, definecode0, _year, SSEMESTER, _semester);
                _attendSemesMap = AttendAccumulate.getAttendSemesMap(db2, z010Name1, _year);
                _hasuuMap = AttendAccumulate.getHasuuMap(_attendSemesMap,_chkSdate, _date); // _sDate: 年度開始日, _date: LOGIN_DATE
                _semesFlg = ((Boolean) _hasuuMap.get("semesFlg")).booleanValue();
            } catch (Exception e) {
                log.fatal("exception!", e);
            }
            log.debug(" hasuuMap = " + _hasuuMap);
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

}//クラスの括り
