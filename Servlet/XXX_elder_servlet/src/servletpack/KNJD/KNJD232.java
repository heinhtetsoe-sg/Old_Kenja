// kanji=漢字
/*
 * $Id: 5e21ed9ec8e430814296730f19433c44b5001949 $
 *
 * 作成日: 2005/08/08
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import servletpack.KNJC.KNJC053_BASE;
import servletpack.KNJC.KNJDivideAttendDate;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.KNJ_Semester;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.Vrw32alpWrap;

/**
 *  学校教育システム 賢者 [成績管理] 成績判定会議資料
 */

/*
 *  2005/08/08 Build
 *  2005/10/05 yamashiro 学期成績による出力を追加
 */

public class KNJD232 {
    private static final Log log = LogFactory.getLog(KNJD232.class);

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {
        Vrw32alpWrap svf = new Vrw32alpWrap();
        DB2UDB db2 = null;
        boolean hasData = false;
        KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();

        sd.setSvfInit(request, response, svf);

        db2 = sd.setDb(request);
        if (sd.openDb(db2)) {
            log.error("db open error");
            return;
        }

        log.fatal("$Revision: 69695 $ $Date: 2019-09-13 18:04:41 +0900 (金, 13 9 2019) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);

        Param _param = new Param(db2, request);

        hasData = printSvf(db2, svf, request, _param);

        sd.closeSvf(svf, hasData);
        sd.closeDb(db2);
    }

    /**
     *  印刷処理
     */
    private boolean printSvf(
            final DB2UDB db2,
            final Vrw32alpWrap svf,
            final HttpServletRequest request,
            final Param _param
    ) {
        boolean hasData = false;
        for (int i = 1; i <= 6; i++) {
            String str = "OUTPUT" + i;
            if (null == request.getParameter(str)) {
                continue;
            }
            final Base base;
            //1=成績優良者 2=成績不振者 3=皆勤者 4=出欠状況優良者 5=出欠状況不振者 6=異動者
            switch (i) {
            case 1:
                base = new GradesGood();
                base._param = _param;
                final boolean b1 = base.printSvf(db2, svf);
                if (b1) {
                    hasData = true;
                }
                log.debug(" output1 = " + b1);
                break;
            case 2:
                base = new GradesPoor();
                base._param = _param;
                final boolean b2 = base.printSvf(db2, svf);
                if (b2) {
                    hasData = true;
                }
                log.debug(" output2 = " + b2);
                break;
            case 3:
                base = new AttendPerfect();
                base._param = _param;
                final boolean b3 = base.printSvf(db2, svf);
                if (b3) {
                    hasData = true;
                }
                log.debug(" output3 = " + b3);
                break;
            case 4:
                base = new AttendGood();
                base._param = _param;
                final boolean b4 = base.printSvf(db2, svf);
                if (b4) {
                    hasData = true;
                }
                log.debug(" output4 = " + b4);
                break;
            case 5:
                base = new AttendPoor();
                base._param = _param;
                final boolean b5 = base.printSvf(db2, svf);
                if (b5) {
                    hasData = true;
                }
                log.debug(" output5 = " + b5);
                break;
            case 6:
                base = new Transfer();
                base._param = _param;
                final boolean b6 = base.printSvf(db2, svf);
                if (b6) {
                    hasData = true;
                }
                log.debug(" output6 = " + b6);
                break;
            default:
                continue;
            }
        }
        return hasData;
    }

    private static String getHrName(
            final Param _param,
            final String hr_class,
            final String attendno
    ) {
        if (null == _param._hrName) {
            return "";
        }
        StringBuffer stb = new StringBuffer();
        String str = KNJ_EditEdit.Ret_Num_Str(hr_class, _param._hrName);
        if (null != str) {
            stb.append(str);
        }
        stb.append("-");
        str = KNJ_EditEdit.editFlontBrank(String.valueOf(Integer.parseInt(attendno)), 2);
        if (null != str) {
            stb.append(str);
        }
        return stb.toString();
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     *  学校教育システム 賢者 [成績管理] 成績判定会議資料
     */

    /*
     *  2005/08/08 Build
     *  2005/10/07 yamashiro 学期名称を取り出すメソッドを追加
     */
    public static abstract class Base {
        Param _param;

        /**
         * 印刷。
         * @param db2
         * @param svf
         * @return
         */
        abstract boolean printSvf(
                final DB2UDB db2,
                final Vrw32alpWrap svf);

    }

    //--- 内部クラス -------------------------------------------------------
    /**
     *  学校教育システム 賢者 [成績管理] 成績判定会議資料 １．成績優良者
     */

    /*
     *  2005/08/08 Build
     *  2005/10/05 yamashiro 学期成績による出力を追加
     *  2005/10/07 yamashiro 学期名称をタイトルに追加
     */

    public class GradesGood extends Base {
        boolean printSvf(
                final DB2UDB db2,
                final Vrw32alpWrap svf
        ) {
            if (0 != setSvfformFormat(svf)) {
                return false;
            }
            final GradesDetail gradesDetail;
            if (null != _param._gradeGoodDetail) {
                gradesDetail = new GradesDetail(false, _param);
            } else {
                gradesDetail = null;
            }

            boolean hasData = false;
            if (printsvfMain(db2, svf, gradesDetail)) {
                hasData = true;
            }

            if (null != gradesDetail) {
                gradesDetail.printSvf(db2, svf, _param._definecode);
            }
            return hasData;
        }

        private boolean printsvfMain(
                final DB2UDB db2,
                final Vrw32alpWrap svf,
                final GradesDetail gradesDetail
        ) {
            boolean hasData = false;
            printsvfOutHead(svf);
            log.debug("★★★ 優良者 ★★★");
            try {
                PreparedStatement ps = db2.prepareStatement(prestatementRecordYuryo());
                ResultSet rs = ps.executeQuery();
                int num = 0;
                while (rs.next()) {
                    final String hrClass = rs.getString("HR_CLASS");
                    final String attendNo = rs.getString("ATTENDNO");
                    final String schregno = rs.getString("SCHREGNO");
                    final String name = rs.getString("NAME");
                    final float valuation = rs.getFloat("VALUATION");
                    printsvfOutDetail(svf, hrClass, attendNo, schregno, name, valuation, ++num);
                    log.debug(schregno + ", " + name);
                    svf.VrEndRecord();
                    if (null != gradesDetail) {
                        gradesDetail._targetSchregno.add(rs.getString("SCHREGNO"));
                    }
                    if (!hasData) {
                        hasData = true;
                    }
                }
                db2.commit();
                rs.close();
                ps.close();
            } catch (SQLException e) {
                 log.error("SQLException", e);
            } catch (Exception e) {
                 log.error("Exception", e);
            }
            return hasData;
        }

        /**
         *  帳票明細行印刷
         */
        private void printsvfOutDetail(
                final Vrw32alpWrap svf,
                final String hrClass,
                final String attendNo,
                final String schregno,
                final String name,
                final float valuation,
                final int num
        ) {
            svf.VrsOut("NUMBER", String.valueOf(num));
            svf.VrsOut("HR_ATTENDNO", getHrName(_param, hrClass, attendNo));
            svf.VrsOut("SCHREGNO", schregno);
            svf.VrsOut("NAME", name);
            svf.VrsOut( "RATING", String.valueOf(_param.df.format(valuation)));
        }

        private void printsvfOutHead(final Vrw32alpWrap svf) {
            svf.VrsOut("NENDO",     _param._nendo);
            svf.VrsOut("GRADE",     _param._printGrade);
            svf.VrsOut("CONDITION", _param._assess1.toString());
            svf.VrsOut("DATE",      _param._nowDateWa);
            svf.VrsOut("DATE2",     _param._printDate);

            svf.VrsOut("SEMESTER",  _param._semesterName );
        }

        int setSvfformFormat(final Vrw32alpWrap svf) {
            return svf.VrSetForm("KNJD232_1.frm", 4);
        }

        /**
         * @return SQL文を戻します。
         */
        private String prestatementRecordYuryo() {
            StringBuffer stb = new StringBuffer();
            stb.append("WITH ");
            //学籍の表
            stb.append("SCHNO AS(");
            stb.append(    "SELECT  T2.SCHREGNO, T3.NAME, T2.HR_CLASS, T2.ATTENDNO ");
            stb.append(    "FROM    SCHREG_REGD_DAT T2 ");
            stb.append(           " INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T2.SCHREGNO ");
            stb.append(    "WHERE   T2.YEAR = '" + _param._year + "' ");
            stb.append(        "AND T2.GRADE = '" + _param._grade + "' ");
            stb.append(        "AND T2.SEMESTER = '" + _param._gakki + "' ");
            stb.append(        "AND NOT (T3.GRD_DIV IS NOT NULL AND T3.GRD_DATE IS NOT NULL AND T3.GRD_DIV IN('2','3') AND T3.GRD_DATE < '" + _param._date + "') ");

            stb.append(     ") ");

            stb.append(_param.sqlJyogaiGappei());

            //成績明細データの表
            stb.append(",RECORD AS(");
            stb.append(    "SELECT  SCHREGNO, ");
            stb.append(            "ROUND(AVG(FLOAT(" + _param._gradeKinde + ")) * 10 ,0) / 10 AS VALUATION ");   //05/10/05Modify
            stb.append(    "FROM    RECORD_DAT T1 ");
            stb.append(    "WHERE   YEAR = '" + _param._year + "' ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(        "AND CLASSCD <= '" + KNJDefineSchool.subject_U + "' ");
            } else {
                stb.append(        "AND SUBSTR(SUBCLASSCD,1,2) <= '" + KNJDefineSchool.subject_U + "' ");
            }
            stb.append(        "AND EXISTS(SELECT  'X' FROM   SCHNO T2 ");
            stb.append(                   "WHERE   T2.SCHREGNO = T1.SCHREGNO) ");
            if (_param.isGakunenMatu()) {
                stb.append("AND NOT EXISTS(SELECT  'X' FROM   GAPPEI T4 ");
                stb.append("WHERE   T4.SUBCLASSCD = ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append(        " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                }
                stb.append("        T1.SUBCLASSCD) ");
            }
            stb.append(    "GROUP BY T1.SCHREGNO ");
            stb.append(    "HAVING " + ( _param._assess1 ).floatValue() + " <= ROUND(AVG(FLOAT(" + _param._gradeKinde + ")) * 10 ,0) / 10 ");  //05/10/05Modify
            stb.append(    ") ");

            //メイン表
            stb.append("SELECT  T2.HR_CLASS ");
            stb.append(       ",T2.ATTENDNO ");
            stb.append(       ",T2.NAME ");
            stb.append(       ",T1.VALUATION ");
            stb.append(       ",T2.SCHREGNO ");
            stb.append("FROM    RECORD T1 ");
            stb.append("INNER JOIN SCHNO T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("ORDER BY T1.VALUATION DESC, T2.HR_CLASS, T2.ATTENDNO ");

            return stb.toString();
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
    *
    *  学校教育システム 賢者 [成績管理] 成績判定会議資料 ２．成績不良者
    */

    /*
     *  2005/08/08 Build
     *  2005/10/02 yamashiro 評定１と未履修をＯＲ条件に変更
     *  2005/10/05 yamashiro 学期成績による出力を追加
     *  2005/10/07 yamashiro 学期名称をタイトルに追加
     *  2006/02/01 yamasihro・2学期制の場合、1月〜3月の出欠集計データのソート順による集計の不具合を修正 --
     *  2007/03/02 m-yama    NO001 出欠対象日付出力を追加した。
     */

    public class GradesPoor extends Base {
        boolean printSvf(
                final DB2UDB db2,
                final Vrw32alpWrap svf
        ) {
            if (0 != setSvfformFormat(svf)) {
                return false;
            }
            final GradesDetail gradesDetail;
            if (null != _param._gradePoorDetail) {
                gradesDetail = new GradesDetail(true, _param);
            } else {
                gradesDetail = null;
            }

            boolean hasData = false;
            if (printsvfMain(db2, svf, gradesDetail)) {
                hasData = true;
            }

            if (null != gradesDetail) {
                gradesDetail.printSvf(db2, svf, _param._definecode);
            }
            return hasData;
        }

        private boolean printsvfMain(
                final DB2UDB db2,
                final Vrw32alpWrap svf,
                final GradesDetail gradesDetail
        ) {
            boolean hasData = false;
            printsvfOutHead(svf);

            log.debug("★★★ 不振者 ★★★");
            String sql = null;
            try {
                sql = prestatementRecordFushin();
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                String schno = null;
                boolean disp = false;
                int num = 0;
                while (rs.next()) {
                    if (schno == null  ||  !rs.getString("SCHREGNO").equals(schno)) {
                        num++;
                        schno = rs.getString("SCHREGNO");
                        disp = true;
                        if (null != gradesDetail) {
                            gradesDetail._targetSchregno.add(rs.getString("SCHREGNO"));
                        }
                    }
                    printsvfOutDetail(svf, rs, num, disp);
                    svf.VrEndRecord();
                    if (!hasData) { hasData = true; }
                    disp = false;
                }
            } catch (SQLException e) {
                log.error("SQLException sql = " + sql, e);
            } catch (Exception e) {
                log.error("Exception", e);
            }

            return hasData;
        }

        /**
         *  帳票明細行印刷
         */
        private void printsvfOutDetail(final Vrw32alpWrap svf, final ResultSet rs, final int num, final boolean disp) {
            try {
                if (disp) {
                    final String hrClass = rs.getString("HR_CLASS");
                    final String attendNo = rs.getString("ATTENDNO");
                    final String schregno = rs.getString("SCHREGNO");
                    final String name = rs.getString("NAME");
                    final String getCredits = rs.getString("GETCREDITS");
                    log.debug(schregno + ", " + name);

                    svf.VrsOut("NUMBER",       String.valueOf(num));
                    svf.VrsOut("HR_ATTENDNO",  getHrName(_param, hrClass, attendNo));
                    svf.VrsOut("SCHREGNO",     schregno);
                    svf.VrsOut("NAME",         name);
                    if (hasGetCredits(getCredits)) {
                        svf.VrsOut("GET_CREDIT", getCredits);   // 修得単位数
                    }
                }

                final String subClassName = rs.getString("SUBCLASSNAME");
                final String credits = rs.getString("CREDITS");
                final String assess = rs.getString("ASSESS");
                final String unstudy = rs.getString("UNSTUDY");

                svf.VrsOut("FILE_SUBCLASS", subClassName);  // 欠点科目
                svf.VrsOut("CREDIT", credits);  // 単位数
                svf.VrsOut("EVALUATION", assess);   // 評価
                svf.VrsOut("NONSTUDY_MARK", unstudy);   // 未履修
            } catch( Exception ex ) {
                log.error("error! ",ex);
            }
        }

        /**
         *  帳票ヘッダー等印刷
         */
        private void printsvfOutHead(final Vrw32alpWrap svf) {
            svf.VrsOut("NENDO",     _param._nendo);
            svf.VrsOut("GRADE",     _param._printGrade);

            StringBuffer stb = new StringBuffer();
            stb.append("条件：評定");
            stb.append(null != _param._assess2 ? _param._assess2.toString() : " ");
            stb.append("以下が");
            stb.append(null != _param._count2 ? _param._count2.toString() : " ");
            stb.append("科目以上、または未履修科目が");
            stb.append(null != _param._unstudy2 ? _param._unstudy2.toString() : " ");
            stb.append("科目以上");

            svf.VrsOut( "CONDITION",  stb.toString());

            svf.VrsOut( "DATE",      _param._nowDateWa );
            svf.VrsOut( "DATE2",     _param._printDate);  //NO001

            svf.VrsOut( "SEMESTER",  _param._semesterName);
        }

        int setSvfformFormat(final Vrw32alpWrap svf) {
            return svf.VrSetForm("KNJD232_2_1.frm", 4);
        }

        /**
         * @param paramap
         * @return SQL文を戻します。
         */
        private String prestatementRecordFushin() {
            StringBuffer stb = new StringBuffer();
            stb.append("WITH ");
            //学籍の表
            stb.append("SCHNO AS(");
            stb.append(    "SELECT  T2.SCHREGNO, T3.NAME, T2.HR_CLASS, T2.ATTENDNO ");
            stb.append(           ",T2.COURSECD, T2.MAJORCD, T2.COURSECODE ");
            stb.append(    "FROM    SCHREG_REGD_DAT T2 ");
            stb.append(           " INNER JOIN SCHREG_BASE_MST T3 ON T2.SCHREGNO = T3.SCHREGNO ");
            stb.append(    "WHERE   T2.YEAR = '" + _param._year + "' ");
            stb.append(        "AND T2.GRADE = '" + _param._grade + "' ");
            stb.append(        "AND T2.SEMESTER = '" + _param._gakki + "' ");
            stb.append(        "AND NOT (T3.GRD_DIV IS NOT NULL AND T3.GRD_DATE IS NOT NULL AND T3.GRD_DIV IN('2','3') AND T3.GRD_DATE < '" + _param._date + "') ");

            stb.append(     ") ");

            // 時間割(留学・休学期間を含む)
            stb.append(", SCHREG_SCHEDULE_R AS(");
            stb.append(    "SELECT  T0.SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(        " T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
            }
            stb.append(           " T2.SUBCLASSCD AS SUBCLASSCD, T2.SEMESTER, T1.EXECUTEDATE, T1.PERIODCD, T1.CHAIRCD, ");
            stb.append("             ATTEND_DI.REP_DI_CD AS YOMIKAEMAE, ");
            stb.append("             ATTEND_DI.MULTIPLY, ");
            stb.append("             CASE WHEN ATTEND_DI.ATSUB_REPL_DI_CD IS NOT NULL THEN ATTEND_DI.ATSUB_REPL_DI_CD ELSE ATTEND_DI.REP_DI_CD END AS DI_CD, ");
            stb.append("             T1.DATADIV ");
            stb.append(           ",(SELECT 'X' FROM COURSE_MST T5 ");
            stb.append(             "WHERE  T5.COURSECD = T10.COURSECD ");
            stb.append(                "AND T1.PERIODCD BETWEEN T5.S_PERIODCD AND T5.E_PERIODCD)AS T_PERIOD ");
            stb.append(    "FROM    SCH_CHR_DAT T1 ");
            stb.append(    "INNER JOIN SEMESTER_MST T3 ON T3.YEAR = T1.YEAR ");
            stb.append(          "AND T3.SEMESTER <> '9' ");
            stb.append(          "AND T1.EXECUTEDATE BETWEEN T3.SDATE AND T3.EDATE ");
            stb.append(    "INNER JOIN CHAIR_DAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append(          "AND T2.SEMESTER = T3.SEMESTER ");
            stb.append(          "AND T2.CHAIRCD = T1.CHAIRCD ");
            stb.append(    "INNER JOIN CHAIR_STD_DAT T0 ON T0.YEAR = T1.YEAR ");
            stb.append(          "AND T0.SEMESTER = T2.SEMESTER ");
            stb.append(          "AND T0.CHAIRCD = T2.CHAIRCD ");
            stb.append(          "AND T1.EXECUTEDATE BETWEEN T0.APPDATE AND T0.APPENDDATE ");
            stb.append(    "INNER JOIN SCHNO T10 ON T0.SCHREGNO = T10.SCHREGNO ");
            stb.append(    "INNER JOIN NAME_YDAT T8 ON T8.YEAR = T1.YEAR ");
            stb.append(          "AND T8.NAMECD1 = 'B001' AND T1.PERIODCD = T8.NAMECD2 ");
            stb.append(    "LEFT JOIN ATTEND_DAT T5 ON T5.SCHREGNO = T0.SCHREGNO ");
            stb.append(          "AND T1.EXECUTEDATE = T5.ATTENDDATE ");
            stb.append(          "AND T1.PERIODCD = T5.PERIODCD ");
            stb.append(          "AND T1.CHAIRCD = T5.CHAIRCD ");
            stb.append("    LEFT JOIN ATTEND_DI_CD_DAT ATTEND_DI ON ATTEND_DI.YEAR = T5.YEAR AND ATTEND_DI.DI_CD = T5.DI_CD ");
            stb.append(    "WHERE   T1.YEAR = '" +  _param._year  + "' ");
            stb.append(          "AND T1.EXECUTEDATE BETWEEN '" + _param._attendFromDate + "' AND '" + _param._attendToDate + "' ");
            stb.append(          "AND T3.SEMESTER <= '" + _param._gakki + "' ");
            //                      学籍不在日を除外
            stb.append(          "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST T6 ");
            stb.append(                       "WHERE   T6.SCHREGNO = T0.SCHREGNO ");
            stb.append(                           "AND (( T6.ENT_DIV IN('4','5') AND T1.EXECUTEDATE < T6.ENT_DATE ) ");
            stb.append(                             "OR ( T6.GRD_DIV IN('2','3') AND T1.EXECUTEDATE > T6.GRD_DATE )) ) ");
            stb.append("    AND NOT EXISTS(SELECT ");
            stb.append("                       'X' ");
            stb.append("                   FROM ");
            stb.append("                       ATTEND_DAT T4 ");
            stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = T4.YEAR AND L1.DI_CD = T4.DI_CD ");
            stb.append("                   WHERE ");
            stb.append("                       T4.SCHREGNO = T0.SCHREGNO ");
            stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
            stb.append("                       AND L1.REP_DI_CD = '27' "); // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
            stb.append("                  ) ");
            // 勤怠コード'28'は時間割にカウントしない
            stb.append("    AND NOT EXISTS(SELECT ");
            stb.append("                       'X' ");
            stb.append("                   FROM ");
            stb.append("                       ATTEND_DAT T4 ");
            stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = T4.YEAR AND L1.DI_CD = T4.DI_CD ");
            stb.append("                   WHERE ");
            stb.append("                       T4.SCHREGNO = T0.SCHREGNO ");
            stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
            stb.append("                       AND L1.REP_DI_CD = '28' ");
            stb.append("                  ) ");
            stb.append(") ");

            // 時間割(留学・休学期間を含まない)
            stb.append(", T_ATTEND_DAT AS(");
            stb.append(    "SELECT  T1.SCHREGNO, T1.SUBCLASSCD, T1.SEMESTER, T1.EXECUTEDATE AS ATTENDDATE, T1.PERIODCD, T1.CHAIRCD, T1.YOMIKAEMAE, T1.MULTIPLY, T1.DI_CD, T1.T_PERIOD, T1.DATADIV ");
            stb.append(    "FROM    SCHREG_SCHEDULE_R T1 ");
            stb.append(    "WHERE   NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT T7 ");
            stb.append(                       "WHERE   T7.SCHREGNO = T1.SCHREGNO ");
            stb.append(                           "AND TRANSFERCD IN ('1','2') AND EXECUTEDATE BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ) ");
            stb.append(") ");

            // 休学時数
            stb.append(", SCHREG_OFFDAYS AS(");
            stb.append(    "SELECT  T1.SCHREGNO, T1.SUBCLASSCD, T1.SEMESTER, COUNT(*) AS OFFDAYS ");
            stb.append(    "FROM    SCHREG_SCHEDULE_R T1 ");
            stb.append(    "WHERE   EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT T7 ");
            stb.append(                       "WHERE   T7.SCHREGNO = T1.SCHREGNO ");
            stb.append(                           "AND TRANSFERCD = '2' AND EXECUTEDATE BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ) ");
            stb.append(    "GROUP BY T1.SCHREGNO, T1.SUBCLASSCD, T1.SEMESTER ");
            stb.append(") ");

            stb.append(", TEST_COUNTFLG AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.EXECUTEDATE, ");
            stb.append("         T1.PERIODCD, ");
            stb.append("         T1.CHAIRCD, ");
            stb.append("         '2' AS DATADIV ");
            stb.append("     FROM ");
            stb.append("         SCH_CHR_TEST T1, ");
            stb.append("         TESTITEM_MST_COUNTFLG_NEW T2 ");
            stb.append("     WHERE ");
            stb.append("         T2.YEAR           = T1.YEAR ");
            stb.append("         AND T2.SEMESTER   = T1.SEMESTER ");
            stb.append("         AND T2.TESTKINDCD = T1.TESTKINDCD ");
            stb.append("         AND T2.TESTITEMCD = T1.TESTITEMCD ");
            stb.append("         AND T2.COUNTFLG   = '0' ");
            stb.append(") ");

            //生徒・科目・学期別欠課明細の表（出欠データ）
            stb.append(", SCH_ATTEND_LOW AS(");
            stb.append(    "SELECT  T1.SCHREGNO, T1.SUBCLASSCD, T1.SEMESTER ");
            stb.append(           ",T1.ATTENDDATE, T1.PERIODCD, T1.YOMIKAEMAE, T1.MULTIPLY, T1.DI_CD ");
            stb.append(    "FROM    SCHNO T0, T_ATTEND_DAT T1 ");
            stb.append(    "WHERE   T0.SCHREGNO = T1.SCHREGNO ");
            if ((null == _param._kyoukaSougou1 && null == _param._kyoukaSougou2) || (null != _param._kyoukaSougou1 && null != _param._kyoukaSougou2)) {
                stb.append("         AND SUBSTR(T1.SUBCLASSCD,1,2) <= '" + KNJDefineSchool.subject_T + "' ");
            } else if (null != _param._kyoukaSougou1 && null == _param._kyoukaSougou2) {
                stb.append("         AND SUBSTR(T1.SUBCLASSCD,1,2) < '" + KNJDefineSchool.subject_T + "' ");
            } else if (null == _param._kyoukaSougou1 && null != _param._kyoukaSougou2) {
                stb.append("         AND SUBSTR(T1.SUBCLASSCD,1,2) = '" + KNJDefineSchool.subject_T + "' ");
            }
            if( _param._definecode.useschchrcountflg ) {
                stb.append(    "AND NOT EXISTS(SELECT  'X' FROM SCH_CHR_COUNTFLG T4 ");
                stb.append(                   "WHERE   T4.EXECUTEDATE = T1.ATTENDDATE ");
                stb.append(                       "AND T4.PERIODCD = T1.PERIODCD ");
                stb.append(                       "AND T4.CHAIRCD = T1.CHAIRCD ");
                stb.append(                       "AND T1.DATADIV IN ('0', '1') ");
                stb.append(                       "AND T4.GRADE = '" + _param._grade + "' ");
                stb.append(                       "AND T4.HR_CLASS = T0.HR_CLASS ");
                stb.append(                       "AND T4.COUNTFLG = '0') ");
                stb.append(    "AND NOT EXISTS(SELECT 'X' FROM TEST_COUNTFLG TEST ");
                stb.append(                   "WHERE TEST.EXECUTEDATE  = T1.ATTENDDATE ");
                stb.append(                       "AND TEST.PERIODCD = T1.PERIODCD ");
                stb.append(                       "AND TEST.CHAIRCD  = T1.CHAIRCD ");
                stb.append(                       "AND TEST.DATADIV  = T1.DATADIV) ");
            }
            stb.append(    ") ");

            //生徒・科目・学期別欠課集計の表（集計テーブル）
            stb.append(", ATTEND_CALC AS(");
            stb.append(    "SELECT  SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(        " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append(            "SUBCLASSCD AS SUBCLASSCD, SEMESTER ");
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
            if ("1".equals(_param._knjSchoolMst._subKoudome)) {
                stb.append(          "+ VALUE(KOUDOME,0)");
            }
            stb.append(           ") AS ABSENT1 ");
            stb.append(           ",SUM(VALUE(LATE,0) + VALUE(EARLY,0)) AS LATE_EARLY ");
            stb.append(    "FROM    ATTEND_SUBCLASS_DAT T1 ");
            stb.append(    "WHERE   YEAR = '" + _param._year + "' ");
            stb.append(        "AND SEMESTER <= '" + _param._gakki + "' ");
            stb.append(        "AND (CASE WHEN INT(T1.MONTH) < 4 THEN RTRIM(CHAR(INT(T1.SEMESTER) + 1 )) ELSE T1.SEMESTER END )||T1.MONTH <= '" + KNJC053_BASE.retSemesterMonthValue (_param._attendLastMonth) + "' ");
            if ((null == _param._kyoukaSougou1 && null == _param._kyoukaSougou2) || (null != _param._kyoukaSougou1 && null != _param._kyoukaSougou2)) {
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("         AND CLASSCD <= '" + KNJDefineSchool.subject_T + "'");
                } else {
                    stb.append("         AND SUBSTR(SUBCLASSCD,1,2) <= '" + KNJDefineSchool.subject_T + "' ");
                }
            } else if (null != _param._kyoukaSougou1 && null == _param._kyoukaSougou2) {
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("         AND CLASSCD < '" + KNJDefineSchool.subject_T + "'");
                } else {
                    stb.append("         AND SUBSTR(SUBCLASSCD,1,2) < '" + KNJDefineSchool.subject_T + "' ");
                }
            } else if (null == _param._kyoukaSougou1 && null != _param._kyoukaSougou2) {
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("         AND CLASSCD = '" + KNJDefineSchool.subject_T + "'");
                } else {
                    stb.append("         AND SUBSTR(SUBCLASSCD,1,2) = '" + KNJDefineSchool.subject_T + "' ");
                }
            }
            stb.append(        "AND EXISTS(SELECT  'X' FROM SCHNO T2 ");
            stb.append(                   "WHERE   T2.SCHREGNO = T1.SCHREGNO) ");
            stb.append(    "GROUP BY SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(        " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append(             "SUBCLASSCD, SEMESTER ");
            stb.append(    ") ");

            //生徒・科目・学期別欠課集計の表（出欠データと集計テーブルを合算）
            stb.append(", SCH_ATTEND_SUM AS(");
            stb.append(    "SELECT  T1.SCHREGNO, T1.SUBCLASSCD, T1.SEMESTER ");
            stb.append(           ",SUM(CASE WHEN DI_CD IN('4','5','6','14','11','12','13' ");
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
            if ("1".equals(_param._knjSchoolMst._subKoudome)) {
                stb.append(          ",'25','26'");
            }
            stb.append(            ") THEN 1 ELSE 0 END) AS ABSENT1 ");
            stb.append(           ",SUM(CASE WHEN DI_CD IN('15','16','23','24') THEN SMALLINT(VALUE(T1.MULTIPLY, '1')) ELSE 0 END)AS LATE_EARLY ");
            stb.append(    "FROM SCH_ATTEND_LOW T1 ");
            stb.append(    "GROUP BY T1.SCHREGNO, T1.SUBCLASSCD, T1.SEMESTER ");
            if ("1".equals(_param._knjSchoolMst._subOffDays)) {
                stb.append(    "UNION ALL ");
                stb.append(    "SELECT  T2.SCHREGNO, T2.SUBCLASSCD, T2.SEMESTER ");
                stb.append(           ",SUM(T2.OFFDAYS) AS ABSENT1 ");
                stb.append(           ",SUM(0) AS LATE_EARLY ");
                stb.append(    "FROM SCHREG_OFFDAYS T2 ");
                stb.append(    "GROUP BY T2.SCHREGNO, T2.SUBCLASSCD, T2.SEMESTER ");
            }
            stb.append(    "UNION ALL ");
            stb.append(    "SELECT  SCHREGNO, SUBCLASSCD, SEMESTER ");
            stb.append(           ",SUM(ABSENT1) AS ABSENT1 ");
            stb.append(           ",SUM(LATE_EARLY) AS LATE_EARLY ");
            stb.append(    "FROM    ATTEND_CALC T1 ");
            stb.append(    "GROUP BY SCHREGNO, SUBCLASSCD, SEMESTER ");
            stb.append(    ") ");

            //ペナルティー欠課を加味した生徒・科目別欠課集計の表（出欠データと集計テーブルを合算）
            stb.append(",SCH_ABSENT_SUM AS(");
            if( _param._definecode.absent_cov == 1 || _param._definecode.absent_cov == 3) {
                //学期でペナルティ欠課を算出する場合
                stb.append("SELECT  SCHREGNO, SUBCLASSCD ");
                stb.append(       ",VALUE(SUM(ABSENT),0) AS ABSENT ");
                stb.append("FROM   ( ");
                stb.append(        "SELECT  SCHREGNO, SUBCLASSCD ");
                if (1 == _param._definecode.absent_cov || _param.isGakunenMatu()) {
                    stb.append(              ", VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / " + _param._definecode.absent_cov_late + " AS ABSENT ");
                } else {
                    stb.append(              ", FLOAT(DECIMAL(VALUE(SUM(ABSENT1),0) + VALUE(SUM(FLOAT(LATE_EARLY)),0) / " + _param._definecode.absent_cov_late + ",5,1)) AS ABSENT ");
                }
                stb.append(         "FROM    SCH_ATTEND_SUM ");
                stb.append(         "GROUP BY SCHREGNO, SUBCLASSCD, SEMESTER ");
                stb.append(         ")T1 ");
                stb.append("GROUP BY T1.SCHREGNO, T1.SUBCLASSCD ");
                stb.append("HAVING 0 < VALUE(SUM(ABSENT),0) ");
            } else if( _param._definecode.absent_cov == 2 || _param._definecode.absent_cov == 4) {
                //通年でペナルティ欠課を算出する場合
                stb.append("SELECT  SCHREGNO, SUBCLASSCD ");
                if (_param._definecode.absent_cov == 2 || _param.isGakunenMatu()) {
                    stb.append(        ", VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / " + _param._definecode.absent_cov_late + " AS ABSENT ");
                } else {
                    stb.append(        ", FLOAT(DECIMAL(VALUE(SUM(ABSENT1),0) + VALUE(SUM(FLOAT(LATE_EARLY)),0) / " + _param._definecode.absent_cov_late + ",5,1)) AS ABSENT ");
                }
                stb.append("FROM    SCH_ATTEND_SUM T1 ");
                stb.append("GROUP BY T1.SCHREGNO, T1.SUBCLASSCD ");
                stb.append("HAVING 0 < VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / " + _param._definecode.absent_cov_late + " ");
            } else{
                //ペナルティ欠課なしの場合
                stb.append("SELECT  SCHREGNO, SUBCLASSCD ");
                stb.append(       ",VALUE(SUM(ABSENT1),0) AS ABSENT ");
                stb.append("FROM    SCH_ATTEND_SUM T1 ");
                stb.append("GROUP BY T1.SCHREGNO, T1.SUBCLASSCD ");
                stb.append("HAVING 0 < VALUE(SUM(ABSENT1),0) ");
            }
            stb.append(    ") ");

            //生徒別科目別欠課時数超過者の表
            stb.append(", SCH_SUBCLASS_ABSENTOVER AS(");
            stb.append("     SELECT T2.SCHREGNO, T2.SUBCLASSCD");
            stb.append("     FROM SCHNO T1");
            stb.append("     INNER JOIN SCH_ABSENT_SUM T2 ON T2.SCHREGNO = T1.SCHREGNO");
            if (_param._knjSchoolMst.isHoutei()) {
                stb.append("     LEFT JOIN CREDIT_MST T5 ON T5.GRADE = '" + _param._grade + "'");
                stb.append("                      AND T5.YEAR = '" + _param._year + "'");
                stb.append("                      AND T5.COURSECD = T1.COURSECD AND T5.MAJORCD = T1.MAJORCD AND T5.COURSECODE = T1.COURSECODE ");
                stb.append("                      AND ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append(        " T5.CLASSCD || '-' || T5.SCHOOL_KIND || '-' || T5.CURRICULUM_CD || '-' || ");
                }
                stb.append("                          T5.SUBCLASSCD = T2.SUBCLASSCD ");
                stb.append("     WHERE T5.ABSENCE_HIGH IS NOT NULL AND VALUE(T5.ABSENCE_HIGH,0) < T2.ABSENT");
            } else {
                stb.append("     LEFT JOIN SCHREG_ABSENCE_HIGH_DAT T5 ON T5.YEAR = '" + _param._year + "' ");
                stb.append("          AND T5.DIV = '" + _param._absenceDiv + "' ");
                stb.append("          AND T2.SUBCLASSCD = ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append(        " T5.CLASSCD || '-' || T5.SCHOOL_KIND || '-' || T5.CURRICULUM_CD || '-' || ");
                }
                stb.append("          T5.SUBCLASSCD ");
                stb.append("          AND T2.SCHREGNO = T5.SCHREGNO ");
                stb.append("     WHERE T5.COMP_ABSENCE_HIGH IS NOT NULL AND VALUE(T5.COMP_ABSENCE_HIGH,0) < T2.ABSENT");
            }
            stb.append(") ");

            //未履修科目数がＸ科目以上の生徒表
            stb.append(", SCH_ABSENT_SUBCLASS_NUM AS(");
            stb.append("     SELECT  SCHREGNO, COUNT(*)AS COUNT");
            stb.append("     FROM SCH_SUBCLASS_ABSENTOVER");
            stb.append("     GROUP BY SCHREGNO");
            if (null != _param._unstudy2) {
                stb.append(" HAVING " + _param._unstudy2 + " <= COUNT(*)");
            } else {
                stb.append(" HAVING 100 <= COUNT(*)");
            }
            stb.append(") ");

            //成績が評定１の明細表
            stb.append(", RECORD_1ASSESS AS(");
            stb.append("     SELECT  SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(        " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append("             SUBCLASSCD AS SUBCLASSCD,");
            stb.append(_param._gradeKinde);
            stb.append("               AS ASSESS");
            stb.append("     FROM    RECORD_DAT T1");
            stb.append("     WHERE   YEAR = '" + _param._year + "'");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("         AND CLASSCD <= '" + KNJDefineSchool.subject_U + "'");
            } else {
                stb.append("         AND SUBSTR(SUBCLASSCD,1,2) <= '" + KNJDefineSchool.subject_U + "'");
            }
            stb.append("         AND " + _param._gradeKinde + " <= " + (_param._assess2).intValue());
            stb.append("         AND EXISTS(SELECT  'X' FROM SCHNO T2 WHERE T2.SCHREGNO = T1.SCHREGNO)");
            if (_param.isGakunenMatu()) {
                stb.append("     UNION ALL ");
                stb.append("     SELECT  SCHREGNO, ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append(        " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                }
                stb.append("             SUBCLASSCD AS SUBCLASSCD,");
                stb.append(_param._gradeKinde);
                stb.append("               AS ASSESS");
                stb.append("     FROM    RECORD_DAT T1");
                stb.append("     WHERE   YEAR = '" + _param._year + "'");
                if ((null == _param._kyoukaSougou1 && null == _param._kyoukaSougou2) || (null != _param._kyoukaSougou1 && null != _param._kyoukaSougou2)) {
                    if ("1".equals(_param._useCurriculumcd)) {
                        stb.append("         AND CLASSCD <= '" + KNJDefineSchool.subject_T + "'");
                    } else {
                        stb.append("         AND SUBSTR(T1.SUBCLASSCD,1,2) <= '" + KNJDefineSchool.subject_T + "' ");
                    }
                } else if (null != _param._kyoukaSougou1 && null == _param._kyoukaSougou2) {
                    if ("1".equals(_param._useCurriculumcd)) {
                        stb.append("         AND CLASSCD < '" + KNJDefineSchool.subject_T + "'");
                    } else {
                        stb.append("         AND SUBSTR(T1.SUBCLASSCD,1,2) < '" + KNJDefineSchool.subject_T + "' ");
                    }
                } else if (null == _param._kyoukaSougou1 && null != _param._kyoukaSougou2) {
                    if ("1".equals(_param._useCurriculumcd)) {
                        stb.append("         AND CLASSCD = '" + KNJDefineSchool.subject_T + "'");
                    } else {
                        stb.append("         AND SUBSTR(T1.SUBCLASSCD,1,2) = '" + KNJDefineSchool.subject_T + "' ");
                    }
                }
                stb.append("         AND " + _param._gradeKinde + " IS NULL ");
                stb.append("         AND (GET_CREDIT = 0 OR (GET_CREDIT IS NULL AND COMP_CREDIT IS NOT NULL)) ");
                stb.append("         AND EXISTS(SELECT  'X' FROM SCHNO T2 WHERE T2.SCHREGNO = T1.SCHREGNO)");
            }
            stb.append(") ");

            //評定１科目数がＸ科目以上の生徒表
            stb.append(",  RECORD_1ASSESS_NUM AS(");
            stb.append("     SELECT  SCHREGNO, COUNT(*) AS COUNT");
            stb.append("     FROM    RECORD_1ASSESS T1");
            stb.append("     GROUP BY SCHREGNO");
            if (null != _param._count2) {
                stb.append(" HAVING " + _param._count2 + " <= COUNT(*)");
            } else {
                stb.append(" HAVING 100 <= COUNT(*)");
            }
            stb.append(") ");

            stb.append(_param.sqlJyogaiGappei());

            //生徒別修得単位の表
            stb.append(",  RECORD_GETCREDITS AS(");
            stb.append("     SELECT  T1.SCHREGNO, SUM(T1.GET_CREDIT)AS CREDITS");
            stb.append("     FROM    RECORD_DAT T1, SCHNO T2");
            stb.append("     WHERE   T1.YEAR = '" + _param._year + "'");
            if ("1".equals(_param._useCurriculumcd)) {
                if ("1".equals(_param._useClassDetailDat)) {
                    stb.append("         AND T1.CLASSCD || '-' || T1.SCHOOL_KIND NOT IN (SELECT CLASSCD || '-' || SCHOOL_KIND ");
                    stb.append("                                FROM CLASS_DETAIL_DAT ");
                    stb.append("                                WHERE YEAR = T1.YEAR AND CLASS_SEQ = '006') ");
                } else {
                    stb.append("         AND T1.CLASSCD NOT IN (SELECT NAMECD2 FROM V_NAME_MST WHERE YEAR = T1.YEAR AND NAMECD1 = 'D049') ");
                }
            } else {
                stb.append("         AND SUBSTR(T1.SUBCLASSCD,1,2) NOT IN (SELECT NAMECD2 FROM V_NAME_MST WHERE YEAR = T1.YEAR AND NAMECD1 = 'D049') ");
            }
            if ((null == _param._kyoukaSougou1 && null == _param._kyoukaSougou2) || (null != _param._kyoukaSougou1 && null != _param._kyoukaSougou2)) {
                stb.append(        " AND SUBSTR(T1.SUBCLASSCD,1,2) <= '" + KNJDefineSchool.subject_T + "' ");
            } else if (null != _param._kyoukaSougou1 && null == _param._kyoukaSougou2) {
                stb.append(        " AND SUBSTR(T1.SUBCLASSCD,1,2) < '" + KNJDefineSchool.subject_T + "' ");
            } else if (null == _param._kyoukaSougou1 && null != _param._kyoukaSougou2) {
                stb.append(        " AND SUBSTR(T1.SUBCLASSCD,1,2) = '" + KNJDefineSchool.subject_T + "' ");
            }
            stb.append("         AND T2.SCHREGNO = T1.SCHREGNO");
            if (_param.isGakunenMatu()) {
                stb.append("         AND NOT EXISTS(SELECT 'X' FROM GAPPEI T3");
                stb.append("                        WHERE T3.SUBCLASSCD = ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append(        " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                }
                stb.append("                              T1.SUBCLASSCD)");
            }
            stb.append("     GROUP BY T1.SCHREGNO");
            stb.append(") ");

            //メイン表
            stb.append("SELECT  T1.SCHREGNO, T1.NAME, T1.HR_CLASS, T1.ATTENDNO, ");
            stb.append(        "T4.SUBCLASSCD, T4.UNSTUDY, T5.CREDITS, T6.SUBCLASSABBV AS SUBCLASSNAME, ");
            stb.append(        "T7.CREDITS AS GETCREDITS ");
            stb.append("       ,T4.ASSESS ");
            stb.append("FROM  ( SELECT  VALUE(T2.SCHREGNO,T3.SCHREGNO) AS SCHREGNO ");
            stb.append(        "FROM    RECORD_1ASSESS_NUM T2 ");
            stb.append(        "FULL JOIN SCH_ABSENT_SUBCLASS_NUM T3 ON T2.SCHREGNO = T3.SCHREGNO ");
            stb.append(      ") T0 ");
            stb.append("LEFT JOIN SCHNO T1 ON T1.SCHREGNO = T0.SCHREGNO ");
            stb.append("INNER JOIN(");
            stb.append(           "SELECT  VALUE(T2.SCHREGNO,T3.SCHREGNO) AS SCHREGNO, ");
            stb.append(             "      VALUE(T2.SUBCLASSCD,T3.SUBCLASSCD) AS SUBCLASSCD ");
            stb.append(              "    ,CASE WHEN T3.SCHREGNO IS NOT NULL THEN '*' ELSE NULL END AS UNSTUDY  ");
            stb.append("                  ,T2.ASSESS ");
            stb.append(           "FROM    RECORD_1ASSESS T2 ");
            stb.append(           "FULL JOIN SCH_SUBCLASS_ABSENTOVER T3 ON T2.SCHREGNO = T3.SCHREGNO AND T2.SUBCLASSCD = T3.SUBCLASSCD ");
            stb.append(          ") T4 ON T4.SCHREGNO = T1.SCHREGNO ");
            stb.append("LEFT JOIN CREDIT_MST T5 ON T5.GRADE  = '" + _param._grade + "' ");
            stb.append(                       "AND T5.YEAR = '" + _param._year + "' ");
            stb.append(                       "AND T5.COURSECD = T1.COURSECD AND T5.MAJORCD = T1.MAJORCD AND T5.COURSECODE = T1.COURSECODE ");
            stb.append(                       "AND ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(        " T5.CLASSCD || '-' || T5.SCHOOL_KIND || '-' || T5.CURRICULUM_CD || '-' || ");
            }
            stb.append(                       " T5.SUBCLASSCD = T4.SUBCLASSCD ");
            stb.append("LEFT JOIN SUBCLASS_MST T6 ON ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(        " T6.CLASSCD || '-' || T6.SCHOOL_KIND || '-' || T6.CURRICULUM_CD || '-' || ");
            }
            stb.append(                       " T6.SUBCLASSCD = T4.SUBCLASSCD ");
            stb.append("LEFT JOIN RECORD_GETCREDITS T7 ON T7.SCHREGNO = T1.SCHREGNO ");
            if ((null == _param._kyoukaSougou1 && null == _param._kyoukaSougou2) || (null != _param._kyoukaSougou1 && null != _param._kyoukaSougou2)) {
                stb.append(        " WHERE SUBSTR(T4.SUBCLASSCD,1,2) <= '" + KNJDefineSchool.subject_T + "' ");
            } else if (null != _param._kyoukaSougou1 && null == _param._kyoukaSougou2) {
                stb.append(        " WHERE SUBSTR(T4.SUBCLASSCD,1,2) < '" + KNJDefineSchool.subject_T + "' ");
            } else if (null == _param._kyoukaSougou1 && null != _param._kyoukaSougou2) {
                stb.append(        " WHERE SUBSTR(T4.SUBCLASSCD,1,2) = '" + KNJDefineSchool.subject_T + "' ");
            }
            stb.append("ORDER BY T1.HR_CLASS, T1.ATTENDNO, T4.SUBCLASSCD");

            return stb.toString();
        }

        /**
         * @param getcredits
         * @return 修得単位を表記する場合Trueを戻します。
         */
        private boolean hasGetCredits(final String getcredits) {
            if (!_param.isGakunenMatu()) {
                return false;
            }
            if (null == getcredits) {
                return false;
            }
            return true;
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     *  学校教育システム 賢者 [成績管理] 成績判定会議資料 ２．成績不良者詳細リスト
     */

    /*
     *  2005/08/08 Build
     *  2005/10/02 yamashiro 評定１と未履修をＯＲ条件に変更
     *  2005/10/02 yamashiro 授業時数の不具合を修正
     *  2005/10/05 yamashiro 学期成績による出力を追加
     *  2006/02/01 yamasihro・2学期制の場合、1月〜3月の出欠集計データのソート順による集計の不具合を修正 --
     *  2007/03/02 m-yama    NO001 出欠対象日付出力を追加した。
     */

    public class GradesDetail {
        final List _targetSchregno = new LinkedList();

        String _title;
        String _condition;
        final boolean _hasAmikake;
        final Param _param;

        GradesDetail(final boolean hasAmikake, final Param param) {
            _hasAmikake = hasAmikake;
            _param = param;

            if (_hasAmikake) {
                _title = "    成績判定会議資料・成績不振者詳細リスト";

                final String a2 = (null != _param._assess2) ?  _param._assess2.toString() : " ";
                final String c2 = (null != _param._count2) ?   _param._count2.toString() : " ";
                final String u2 = (null != _param._unstudy2) ? _param._unstudy2.toString() : " ";

                _condition = "条件：評定" + a2 + "以下が" + c2 + "科目以上、または未履修科目が" + u2 + "科目以上";
            } else {
                _title = "    成績判定会議資料・成績優良者詳細リスト";

                final String a1 = (null != _param._assess1) ? _param._assess1.toString() : " ";
                _condition = "条件：評定" + a1 + "が1科目以上";
            }
        }

        void printSvf(
                final DB2UDB db2,
                final Vrw32alpWrap svf,
                final KNJDefineSchool definecode
        ) {
            final String sql = prestatementRecordSeiseki(definecode);

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                for (Iterator it = _targetSchregno.iterator(); it.hasNext();) {
                    final String schno = (String) it.next();
                    ps.setString(1, schno);
                    rs = ps.executeQuery();

                    svf.VrSetForm("KNJD232_2_2.frm", 4);
                    int num = 0;
                    while (rs.next()) {
                       if (0 == num) {
                           final String hrName = rs.getString("HR_NAME");
                           final String attendNoStr = rs.getString("ATTENDNO");
                           final String name = rs.getString("NAME");

                           printsvfOutHead(svf, hrName, attendNoStr, name);
                       }
                       final Double gradValue = KNJServletUtils.getDouble(rs, "GRAD_VALUE");
                       final Double absenceHigh = KNJServletUtils.getDouble(rs, "ABSENCE_HIGH");
                       final Double absent = KNJServletUtils.getDouble(rs, "ABSENT");
                       final String subClassName = rs.getString("SUBCLASSNAME");
                       final String credits = rs.getString("CREDITS");
                       final String jisu = rs.getString("JISU");

                       printsvfOutDetail(svf, gradValue, absenceHigh, absent, subClassName, credits, jisu, ++num);
                       if (0 == num % 8) {
                           svf.VrEndRecord();
                       }
                    }
                    if (0 < num) {
                        svf.VrEndRecord();
                        log.debug("detail: schregno = " + schno);
                    } else {
                        log.debug("no detail: schregno = " + schno);
                    }
                }
            } catch (SQLException e) {
                log.error("SQLException", e);
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        /**
         *  帳票明細行印刷
         */
        private void printsvfOutDetail(
                final Vrw32alpWrap svf,
                final Double gradValue,
                final Double absenceHigh,
                final Double absent,
                final String subClassName,
                final String credits,
                final String jisu,
                final int num
        ) {
            int i = ( 0 < num % 8 )? num % 8 : 8;       // SVF-FORM 1行あたり8列

            // 科目名
            boolean amikake = false;
            if (_hasAmikake) {
                if (null != _param._assess2 && null != gradValue) {
                    if (gradValue.intValue() <= (_param._assess2).intValue()) {
                        amikake = true;
                    }
                }
                if (!amikake && null != absenceHigh && null != absent) {
                    if (absent.intValue() > absenceHigh.doubleValue()) {
                        amikake = true;
                    }
                }
            }
            if (amikake) {
                svf.VrAttribute("SUBCLASS" + i, "Paint=(2,70,2),Bold=1");
            }
            svf.VrsOut("SUBCLASS" + i, subClassName);
            if (amikake) {
                svf.VrAttribute("SUBCLASS" + i, "Paint=(0,0,0),Bold=0");
            }

            // 欠課数
            if (null != absent) {
                svf.VrsOut("KEKKA" + i, _param._absentFmt.format(absent));
            }

            // 成績
            svf.VrsOut("RATING" + i, (null == gradValue) ? null : String.valueOf(gradValue.intValue()));

            // 単位数
            svf.VrsOut("CREDIT" + i, credits);

            // 総時数
            svf.VrsOut("LESSON" + i, jisu);
        }

        /**
         *  帳票ヘッダー等印刷
         */
        private void printsvfOutHead(
                final Vrw32alpWrap svf,
                final String hrName,
                final String attendNoStr,
                final String name
        ) {
            svf.VrsOut("NENDO", _param._nendo);
            svf.VrsOut("GRADE", _param._printGrade );

            svf.VrsOut("TITLE", _title);
            svf.VrsOut("CONDITION", _condition);

            svf.VrsOut("DATE", _param._nowDateWa);
            svf.VrsOut("DATE2", _param._printDate);  //NO001
            svf.VrsOut("HR_NAME", hrName);

            final String attendNo = String.valueOf(Integer.parseInt(attendNoStr));
            svf.VrsOut("ATTENDNO",  KNJ_EditEdit.editFlontBrank(attendNo, 2));

            svf.VrsOut("NAME", name);
        }

        /**
         *  SQLStatement作成 成績データ
         */
        private String prestatementRecordSeiseki(final KNJDefineSchool definecode) {
            StringBuffer stb = new StringBuffer();
            stb.append("WITH ");
            //学籍の表
            stb.append("SCHNO AS(");
            stb.append(    "SELECT  T2.SCHREGNO, T3.NAME, T2.HR_CLASS, T2.ATTENDNO, T4.HR_NAME ");
            stb.append(           ",T2.COURSECD, T2.MAJORCD, T2.COURSECODE ");
            stb.append(    "FROM    SCHREG_REGD_DAT T2 ");
            stb.append(           " INNER JOIN SCHREG_BASE_MST T3 ON T2.SCHREGNO = T3.SCHREGNO ");
            stb.append(           " INNER JOIN SCHREG_REGD_HDAT T4 ON T4.YEAR = '" + _param._year + "' ");
            stb.append(        "AND T4.GRADE = '" + _param._grade + "' ");
            stb.append(        "AND T4.SEMESTER = '" + _param._gakki + "' ");
            stb.append(        "AND T4.HR_CLASS = T2.HR_CLASS ");

            stb.append(    "WHERE   T2.YEAR = '" + _param._year + "' ");
            stb.append(        "AND T2.GRADE = '" + _param._grade + "' ");
            stb.append(        "AND T2.SEMESTER = '" + _param._gakki + "' ");
            stb.append(        "AND T2.SCHREGNO = ? ");

            stb.append(     ") ");

            //生徒・科目・学期別欠課集計の表（集計テーブル）
            stb.append(", ATTEND_CALC AS(");
            stb.append(    "SELECT  SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(        " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append(            "SUBCLASSCD AS SUBCLASSCD, SEMESTER ");
            stb.append(           ",SUM(VALUE(LESSON,0)) AS LESSON ");
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
            if ("1".equals(_param._knjSchoolMst._subKoudome)) {
                stb.append(          "+ VALUE(KOUDOME,0)");
            }
            stb.append(           ") AS ABSENT1 ");
            stb.append(           ",SUM(VALUE(LATE,0) + VALUE(EARLY,0)) AS LATE_EARLY ");
            stb.append(    "FROM ATTEND_SUBCLASS_DAT T1 ");
            stb.append(    "WHERE   YEAR = '" + _param._year + "' ");
            stb.append(        "AND SEMESTER <= '" + _param._gakki + "' ");
            stb.append(        "AND (CASE WHEN INT(T1.MONTH) < 4 THEN RTRIM(CHAR(INT(T1.SEMESTER) + 1 )) ELSE T1.SEMESTER END )||T1.MONTH <= '" + KNJC053_BASE.retSemesterMonthValue (_param._attendLastMonth) + "' ");   //--NO004
            if (_hasAmikake) {
                if ((null == _param._kyoukaSougou1 && null == _param._kyoukaSougou2) || (null != _param._kyoukaSougou1 && null != _param._kyoukaSougou2)) {
                    if ("1".equals(_param._useCurriculumcd)) {
                        stb.append("         AND CLASSCD <= '" + KNJDefineSchool.subject_T + "'");
                    } else {
                        stb.append("         AND SUBSTR(SUBCLASSCD,1,2) <= '" + KNJDefineSchool.subject_T + "' ");
                    }
                } else if (null != _param._kyoukaSougou1 && null == _param._kyoukaSougou2) {
                    if ("1".equals(_param._useCurriculumcd)) {
                        stb.append("         AND CLASSCD < '" + KNJDefineSchool.subject_T + "'");
                    } else {
                        stb.append("         AND SUBSTR(SUBCLASSCD,1,2) < '" + KNJDefineSchool.subject_T + "' ");
                    }
                } else if (null == _param._kyoukaSougou1 && null != _param._kyoukaSougou2) {
                    if ("1".equals(_param._useCurriculumcd)) {
                        stb.append("         AND CLASSCD = '" + KNJDefineSchool.subject_T + "'");
                    } else {
                        stb.append("         AND SUBSTR(SUBCLASSCD,1,2) = '" + KNJDefineSchool.subject_T + "' ");
                    }
                }
            } else {
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append(        "AND CLASSCD <= '" + KNJDefineSchool.subject_U + "' ");
                } else {
                    stb.append(        "AND SUBSTR(SUBCLASSCD,1,2) <= '" + KNJDefineSchool.subject_U + "' ");
                }
            }
            stb.append(        "AND EXISTS(SELECT  'X' FROM SCHNO T2 ");
            stb.append(                   "WHERE   T2.SCHREGNO = T1.SCHREGNO) ");
            stb.append(    "GROUP BY SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(        " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append(             "SUBCLASSCD, SEMESTER ");
            stb.append(    ") ");

            // 留学数・休学数を含む時間割
            stb.append(", SCHREG_SCHEDULE_R AS(");
            stb.append(    "SELECT  T0.SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(            "T2.CLASSCD, ");
                stb.append(            "T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
            }
            stb.append(            "T2.SUBCLASSCD AS SUBCLASSCD, T2.SEMESTER, T1.EXECUTEDATE, T1.PERIODCD, T1.CHAIRCD, T1.DATADIV, ");
            stb.append("             ATTEND_DI.REP_DI_CD AS YOMIKAEMAE, ");
            stb.append("             ATTEND_DI.MULTIPLY, ");
            stb.append("             CASE WHEN ATTEND_DI.ATSUB_REPL_DI_CD IS NOT NULL THEN ATTEND_DI.ATSUB_REPL_DI_CD ELSE ATTEND_DI.REP_DI_CD END AS DI_CD ");
            stb.append(           ",(SELECT 'X' FROM COURSE_MST T5 ");
            stb.append(             "WHERE  T5.COURSECD = T10.COURSECD ");
            stb.append(                "AND T1.PERIODCD BETWEEN T5.S_PERIODCD AND T5.E_PERIODCD)AS T_PERIOD ");
            stb.append(    "FROM    SCH_CHR_DAT T1 ");
            stb.append(    "INNER JOIN SEMESTER_MST T3 ON T3.YEAR = T1.YEAR ");
            stb.append(          "AND T3.SEMESTER <> '9' ");
            stb.append(          "AND T1.EXECUTEDATE BETWEEN T3.SDATE AND T3.EDATE ");
            stb.append(    "INNER JOIN CHAIR_DAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append(          "AND T2.SEMESTER = T3.SEMESTER ");
            stb.append(          "AND T2.CHAIRCD = T1.CHAIRCD ");
            stb.append(    "INNER JOIN CHAIR_STD_DAT T0 ON T0.YEAR = T1.YEAR ");
            stb.append(          "AND T0.SEMESTER = T2.SEMESTER ");
            stb.append(          "AND T0.CHAIRCD = T2.CHAIRCD ");
            stb.append(          "AND T1.EXECUTEDATE BETWEEN T0.APPDATE AND T0.APPENDDATE ");
            stb.append(    "INNER JOIN SCHNO T10 ON T0.SCHREGNO = T10.SCHREGNO ");
            stb.append(    "INNER JOIN NAME_YDAT T8 ON T8.YEAR = T1.YEAR ");
            stb.append(          "AND T8.NAMECD1 = 'B001' AND T1.PERIODCD = T8.NAMECD2 ");
            stb.append(    "LEFT JOIN ATTEND_DAT T5 ON T5.SCHREGNO = T0.SCHREGNO ");
            stb.append(          "AND T1.EXECUTEDATE = T5.ATTENDDATE ");
            stb.append(          "AND T1.PERIODCD = T5.PERIODCD ");
            stb.append(          "AND T1.CHAIRCD = T5.CHAIRCD ");
            stb.append("    LEFT JOIN ATTEND_DI_CD_DAT ATTEND_DI ON ATTEND_DI.YEAR = T5.YEAR AND ATTEND_DI.DI_CD = T5.DI_CD ");
            stb.append(    "WHERE   T1.YEAR = '" +  _param._year  + "' ");
            stb.append(          "AND T1.EXECUTEDATE BETWEEN '" + _param._attendFromDate + "' AND '" + _param._attendToDate + "' ");
            stb.append(          "AND T3.SEMESTER <= '" + _param._gakki + "' ");
            //                      学籍不在日を除外
            stb.append(          "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST T6 ");
            stb.append(                       "WHERE   T6.SCHREGNO = T0.SCHREGNO ");
            stb.append(                           "AND (( T6.ENT_DIV IN('4','5') AND T1.EXECUTEDATE < T6.ENT_DATE ) ");
            stb.append(                             "OR ( T6.GRD_DIV IN('2','3') AND T1.EXECUTEDATE > T6.GRD_DATE )) ) ");
            stb.append("    AND NOT EXISTS(SELECT ");
            stb.append("                       'X' ");
            stb.append("                   FROM ");
            stb.append("                       ATTEND_DAT T4 ");
            stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = T4.YEAR AND L1.DI_CD = T4.DI_CD ");
            stb.append("                   WHERE ");
            stb.append("                       T4.SCHREGNO = T0.SCHREGNO ");
            stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
            stb.append("                       AND L1.REP_DI_CD = '27' "); // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
            stb.append("                  ) ");
            // 勤怠コード'28'は時間割にカウントしない
            stb.append("    AND NOT EXISTS(SELECT ");
            stb.append("                       'X' ");
            stb.append("                   FROM ");
            stb.append("                       ATTEND_DAT T4 ");
            stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = T4.YEAR AND L1.DI_CD = T4.DI_CD ");
            stb.append("                   WHERE ");
            stb.append("                       T4.SCHREGNO = T0.SCHREGNO ");
            stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
            stb.append("                       AND L1.REP_DI_CD = '28' ");
            stb.append("                  ) ");
            stb.append(") ");

            // 留学時数・休学時数を含まない時間割
            stb.append(", T_ATTEND_DAT AS(");
            stb.append(    "SELECT  T1.SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(            "T1.CLASSCD, ");
            }
            stb.append(            "T1.SUBCLASSCD, T1.SEMESTER, T1.EXECUTEDATE AS ATTENDDATE, T1.PERIODCD, T1.CHAIRCD, T1.YOMIKAEMAE, T1.MULTIPLY, T1.DI_CD, T1.T_PERIOD, T1.DATADIV ");
            stb.append(    "FROM    SCHREG_SCHEDULE_R T1 ");
            stb.append(    "WHERE   NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT T7 ");
            stb.append(                       "WHERE   T7.SCHREGNO = T1.SCHREGNO ");
            stb.append(                           "AND TRANSFERCD IN ('1','2') AND EXECUTEDATE BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ) ");
            stb.append(") ");

            // 休学時数
            stb.append(", SCHREG_OFFDAYS AS(");
            stb.append(    "SELECT  T1.SCHREGNO, T1.SUBCLASSCD, T1.SEMESTER, COUNT(*) AS OFFDAYS ");
            stb.append(    "FROM    SCHREG_SCHEDULE_R T1 ");
            stb.append(    "WHERE   EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT T7 ");
            stb.append(                       "WHERE   T7.SCHREGNO = T1.SCHREGNO ");
            stb.append(                           "AND TRANSFERCD = '2' AND EXECUTEDATE BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ) ");
            stb.append(    "GROUP BY T1.SCHREGNO, T1.SUBCLASSCD, T1.SEMESTER ");
            stb.append(") ");

            stb.append(", TEST_COUNTFLG AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.EXECUTEDATE, ");
            stb.append("         T1.PERIODCD, ");
            stb.append("         T1.CHAIRCD, ");
            stb.append("         '2' AS DATADIV ");
            stb.append("     FROM ");
            stb.append("         SCH_CHR_TEST T1, ");
            stb.append("         TESTITEM_MST_COUNTFLG_NEW T2 ");
            stb.append("     WHERE ");
            stb.append("         T2.YEAR           = T1.YEAR ");
            stb.append("         AND T2.SEMESTER   = T1.SEMESTER ");
            stb.append("         AND T2.TESTKINDCD = T1.TESTKINDCD ");
            stb.append("         AND T2.TESTITEMCD = T1.TESTITEMCD ");
            stb.append("         AND T2.COUNTFLG   = '0' ");
            stb.append(") ");
            //生徒・科目・学期別欠課明細の表（出欠データ）
            stb.append(", SCH_ATTEND_LOW AS(");
            stb.append(    "SELECT  T1.SCHREGNO, T1.SUBCLASSCD, T1.SEMESTER ");
            stb.append(           ",T1.ATTENDDATE, T1.PERIODCD, T1.YOMIKAEMAE, T1.MULTIPLY, T1.DI_CD ");
            stb.append(    "FROM    SCHNO T0, T_ATTEND_DAT T1 ");
            stb.append(    " WHERE T0.SCHREGNO = T1.SCHREGNO ");
            if (_hasAmikake) {
                if ((null == _param._kyoukaSougou1 && null == _param._kyoukaSougou2) || (null != _param._kyoukaSougou1 && null != _param._kyoukaSougou2)) {
                    if ("1".equals(_param._useCurriculumcd)) {
                        stb.append(" AND T1.CLASSCD <= '" + KNJDefineSchool.subject_T + "' ");
                    } else {
                        stb.append(" AND SUBSTR(T1.SUBCLASSCD,1,2) <= '" + KNJDefineSchool.subject_T + "' ");
                    }
                } else if (null != _param._kyoukaSougou1 && null == _param._kyoukaSougou2) {
                    if ("1".equals(_param._useCurriculumcd)) {
                        stb.append(" AND T1.CLASSCD < '" + KNJDefineSchool.subject_T + "' ");
                    } else {
                        stb.append(" AND SUBSTR(T1.SUBCLASSCD,1,2) < '" + KNJDefineSchool.subject_T + "' ");
                    }
                } else if (null == _param._kyoukaSougou1 && null != _param._kyoukaSougou2) {
                    if ("1".equals(_param._useCurriculumcd)) {
                        stb.append(" AND T1.CLASSCD = '" + KNJDefineSchool.subject_T + "' ");
                    } else {
                        stb.append(" AND SUBSTR(T1.SUBCLASSCD,1,2) = '" + KNJDefineSchool.subject_T + "' ");
                    }
                }
            } else {
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("    AND T1.CLASSCD <= '" + KNJDefineSchool.subject_U + "' ");
                } else {
                    stb.append("    AND SUBSTR(T1.SUBCLASSCD,1,2) <= '" + KNJDefineSchool.subject_U + "' ");
                }
            }
            if( _param._definecode.useschchrcountflg ) {
                stb.append(    "AND NOT EXISTS(SELECT  'X' FROM SCH_CHR_COUNTFLG T4 ");
                stb.append(                   "WHERE   T4.EXECUTEDATE = T1.ATTENDDATE ");
                stb.append(                       "AND T4.PERIODCD = T1.PERIODCD ");
                stb.append(                       "AND T4.CHAIRCD = T1.CHAIRCD ");
                stb.append(                       "AND T1.DATADIV IN ('0', '1') ");
                stb.append(                       "AND T4.GRADE = '" + _param._grade + "' ");
                stb.append(                       "AND T4.HR_CLASS = T0.HR_CLASS ");
                stb.append(                       "AND T4.COUNTFLG = '0') ");
                stb.append(    "AND NOT EXISTS(SELECT 'X' FROM TEST_COUNTFLG TEST ");
                stb.append(                   "WHERE TEST.EXECUTEDATE  = T1.ATTENDDATE ");
                stb.append(                       "AND TEST.PERIODCD = T1.PERIODCD ");
                stb.append(                       "AND TEST.CHAIRCD  = T1.CHAIRCD ");
                stb.append(                       "AND TEST.DATADIV  = T1.DATADIV) ");
            }
            stb.append(    ") ");

            //生徒・科目・学期別欠課集計の表（出欠データと集計テーブルを合算）
            stb.append(", SCH_ATTEND_SUM AS(");
            stb.append(    "SELECT  T1.SCHREGNO, T1.SUBCLASSCD, T1.SEMESTER ");
            stb.append(           ",SUM(CASE WHEN DI_CD IN('4','5','6','14','11','12','13' ");
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
            if ("1".equals(_param._knjSchoolMst._subKoudome)) {
                stb.append(          ",'25','26'");
            }
            stb.append(            ") THEN 1 ELSE 0 END) AS ABSENT1 ");
            stb.append(           ",SUM(CASE WHEN DI_CD IN('15','16','23','24') THEN SMALLINT(VALUE(T1.MULTIPLY, '1')) ELSE 0 END)AS LATE_EARLY ");
            stb.append(    "FROM SCH_ATTEND_LOW T1 ");
            stb.append(    "GROUP BY T1.SCHREGNO, T1.SUBCLASSCD, T1.SEMESTER ");
            if ("1".equals(_param._knjSchoolMst._subOffDays)) {
                stb.append(    "UNION ALL ");
                stb.append(    "SELECT  T2.SCHREGNO, T2.SUBCLASSCD, T2.SEMESTER ");
                stb.append(           ",SUM(T2.OFFDAYS) AS ABSENT1 ");
                stb.append(           ",SUM(0) AS LATE_EARLY ");
                stb.append(    "FROM SCHREG_OFFDAYS T2 ");
                stb.append(    "GROUP BY T2.SCHREGNO, T2.SUBCLASSCD, T2.SEMESTER ");
            }
            stb.append(    "UNION ALL ");
            stb.append(    "SELECT  SCHREGNO, SUBCLASSCD, SEMESTER ");
            stb.append(           ",SUM(ABSENT1) AS ABSENT1 ");
            stb.append(           ",SUM(LATE_EARLY) AS LATE_EARLY ");
            stb.append(    "FROM    ATTEND_CALC T1 ");
            stb.append(    "GROUP BY SCHREGNO, SUBCLASSCD, SEMESTER ");
            stb.append(    ") ");

            //ペナルティー欠課を加味した生徒・科目別欠課集計の表（出欠データと集計テーブルを合算）
            stb.append(",SCH_ABSENT_SUM AS(");
            stb.append("          SELECT  SCHREGNO, SUBCLASSCD ");
            if (1 == definecode.absent_cov || 3 == definecode.absent_cov) {
                //遅刻・早退を学期で欠課換算する
                stb.append("            , SUM(ABSENT)AS ABSENT ");
                stb.append("      FROM (");
                stb.append("            SELECT  SCHREGNO, SUBCLASSCD");
                if (1 == definecode.absent_cov || _param.isGakunenMatu()) {
                    stb.append("              , VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / " + definecode.absent_cov_late + " AS ABSENT ");
                } else {
                    stb.append("              , FLOAT(DECIMAL(VALUE(SUM(ABSENT1),0) + VALUE(SUM(FLOAT(LATE_EARLY)),0) / " + definecode.absent_cov_late + ",5,1)) AS ABSENT ");
                }
                stb.append("            FROM    SCH_ATTEND_SUM ");
                stb.append("            GROUP BY SCHREGNO, SUBCLASSCD, SEMESTER ");
                stb.append("      )W1 ");
                stb.append("      GROUP BY SCHREGNO, SUBCLASSCD ");
            } else if (2 == definecode.absent_cov || 4 == definecode.absent_cov) {
                //遅刻・早退を年間で欠課換算する
                if (definecode.absent_cov == 2 || _param.isGakunenMatu()) {
                    stb.append("        , VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / " + definecode.absent_cov_late + " AS ABSENT ");
                } else {
                    stb.append("        , FLOAT(DECIMAL(VALUE(SUM(ABSENT1),0) + VALUE(SUM(FLOAT(LATE_EARLY)),0) / " + definecode.absent_cov_late + ",5,1)) AS ABSENT ");
                }
                stb.append("      FROM    SCH_ATTEND_SUM ");
                stb.append("      GROUP BY SCHREGNO, SUBCLASSCD ");
            } else {
                //遅刻・早退を欠課換算しない
                stb.append("            , SUM(ABSENT1)AS ABSENT ");
                stb.append("      FROM    SCH_ATTEND_SUM ");
                stb.append("      GROUP BY SCHREGNO, SUBCLASSCD ");
            }
            stb.append(") ");

            //科目別時数の表 05/10/02Modify
            stb.append(", TOTAL_JISU_NUM AS(");
            stb.append(    "SELECT  SCHREGNO,SUBCLASSCD,SUM(JISU) AS JISU ");
            stb.append(    "FROM (");
            stb.append(       "SELECT  T2.SCHREGNO,");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(        " T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || ");
            }
            stb.append(                "SUBCLASSCD AS SUBCLASSCD,COUNT(*) AS JISU ");
            stb.append(       "FROM    SCH_CHR_DAT T1, CHAIR_STD_DAT T2,CHAIR_DAT T3 ");
            stb.append(       "WHERE   T1.YEAR = '" + _param._year + "' ");
            stb.append(           "AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ");
            stb.append(           "AND T1.EXECUTEDATE BETWEEN '" + _param._attendFromDate + "' AND '" + _param._attendToDate + "' ");
            stb.append(           "AND T1.YEAR = T2.YEAR ");
            stb.append(           "AND T1.SEMESTER = T2.SEMESTER ");
            stb.append(           "AND T1.CHAIRCD = T2.CHAIRCD ");
            stb.append(           "AND T2.SCHREGNO IN(SELECT SCHREGNO FROM SCHNO) ");
            stb.append(           "AND T3.YEAR='" + _param._year + "' ");
            stb.append(           "AND T3.SEMESTER = T1.SEMESTER ");
            stb.append(           "AND T3.CHAIRCD = T1.CHAIRCD ");
            stb.append(       "GROUP BY T2.SCHREGNO,");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(        " T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || ");
            }
            stb.append(                "SUBCLASSCD ");
                            //05/10/02Modify
            stb.append(       "UNION ");
            stb.append(       "SELECT SCHREGNO,SUBCLASSCD,SUM(LESSON) AS JISU ");
            stb.append(       "FROM ATTEND_CALC ");
            stb.append(       "WHERE  SCHREGNO IN(SELECT SCHREGNO FROM SCHNO) ");
            stb.append(       "GROUP BY SCHREGNO,SUBCLASSCD ");
            stb.append(       ")T1 ");
            stb.append(    "GROUP BY SCHREGNO,SUBCLASSCD ");
            stb.append(    ") ");

            //成績評定の明細表
            stb.append(", RECORD_ASSESS AS(");
            stb.append(    "SELECT  SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(        " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append(            "SUBCLASSCD AS SUBCLASSCD, " + _param._gradeKinde + " AS GRAD_VALUE ");   //05/10/05Modify
            stb.append(    "FROM    RECORD_DAT T1 ");
            stb.append(    "WHERE   YEAR = '" + _param._year + "' ");
            if (_hasAmikake) {
                if ((null == _param._kyoukaSougou1 && null == _param._kyoukaSougou2) || (null != _param._kyoukaSougou1 && null != _param._kyoukaSougou2)) {
                    if ("1".equals(_param._useCurriculumcd)) {
                        stb.append(" AND T1.CLASSCD <= '" + KNJDefineSchool.subject_T + "' ");
                    } else {
                        stb.append(" AND SUBSTR(T1.SUBCLASSCD,1,2) <= '" + KNJDefineSchool.subject_T + "' ");
                    }
                } else if (null != _param._kyoukaSougou1 && null == _param._kyoukaSougou2) {
                    if ("1".equals(_param._useCurriculumcd)) {
                        stb.append(" AND T1.CLASSCD < '" + KNJDefineSchool.subject_T + "' ");
                    } else {
                        stb.append(" AND SUBSTR(T1.SUBCLASSCD,1,2) < '" + KNJDefineSchool.subject_T + "' ");
                    }
                } else if (null == _param._kyoukaSougou1 && null != _param._kyoukaSougou2) {
                    if ("1".equals(_param._useCurriculumcd)) {
                        stb.append(" AND T1.CLASSCD = '" + KNJDefineSchool.subject_T + "' ");
                    } else {
                        stb.append(" AND SUBSTR(T1.SUBCLASSCD,1,2) = '" + KNJDefineSchool.subject_T + "' ");
                    }
                }
            } else {
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append(        "AND CLASSCD <= '" + KNJDefineSchool.subject_U + "' ");
                } else {
                    stb.append(        "AND SUBSTR(SUBCLASSCD,1,2) <= '" + KNJDefineSchool.subject_U + "' ");
                }
            }
            stb.append(        "AND " + _param._gradeKinde + " IS NOT NULL ");  //05/10/05Modify
            stb.append(        "AND EXISTS(SELECT  'X' FROM   SCHNO T2 ");
            stb.append(                   "WHERE   T2.SCHREGNO = T1.SCHREGNO) ");
            stb.append(    ") ");

            //メイン表
            stb.append("SELECT  T1.SCHREGNO, T1.NAME, T1.HR_CLASS, T1.ATTENDNO, T1.HR_NAME ");
            stb.append(       ",T4.SUBCLASSCD, T5.CREDITS, T6.SUBCLASSABBV AS SUBCLASSNAME ");
            stb.append(       ",T4.GRAD_VALUE, T4.ABSENT, T7.JISU ");
            if (_param._knjSchoolMst.isHoutei()) {
                stb.append("       ,T5.ABSENCE_HIGH ");
            } else {
                stb.append("       ,L1.COMP_ABSENCE_HIGH AS ABSENCE_HIGH ");
            }
            stb.append("FROM    SCHNO T1 ");
            stb.append("INNER JOIN(");
            stb.append(           "SELECT  VALUE(T2.SCHREGNO,T3.SCHREGNO) AS SCHREGNO ");
            stb.append(                  ",VALUE(T2.SUBCLASSCD,T3.SUBCLASSCD) AS SUBCLASSCD ");
            stb.append(                  ",GRAD_VALUE, ABSENT ");
            stb.append(           "FROM    RECORD_ASSESS T2 ");
            stb.append(           "FULL JOIN(SELECT SCHREGNO,SUBCLASSCD,ABSENT FROM SCH_ABSENT_SUM) T3 ON T2.SCHREGNO = T3.SCHREGNO AND T2.SUBCLASSCD = T3.SUBCLASSCD");
            stb.append(          ") T4 ON T4.SCHREGNO = T1.SCHREGNO ");
            stb.append("LEFT JOIN CREDIT_MST T5 ON T5.GRADE  = '" + _param._grade + "' ");
            stb.append(                       "AND T5.YEAR = '" + _param._year + "' ");
            stb.append(                       "AND T5.COURSECD = T1.COURSECD AND T5.MAJORCD = T1.MAJORCD AND T5.COURSECODE = T1.COURSECODE ");
            stb.append(                       "AND ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(        " T5.CLASSCD || '-' || T5.SCHOOL_KIND || '-' || T5.CURRICULUM_CD || '-' || ");
            }
            stb.append(                       " T5.SUBCLASSCD = T4.SUBCLASSCD ");

            if (_param._knjSchoolMst.isJitu()) {
                stb.append("     LEFT JOIN SCHREG_ABSENCE_HIGH_DAT L1 ON L1.YEAR = '" + _param._year + "' ");
                stb.append("          AND L1.DIV = '" + _param._absenceDiv + "' ");
                stb.append("          AND T4.SUBCLASSCD = ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append(        " L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || ");
                }
                stb.append("              L1.SUBCLASSCD ");
                stb.append("          AND T4.SCHREGNO = L1.SCHREGNO ");
            }
            stb.append("LEFT JOIN SUBCLASS_MST T6 ON ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(        " T6.CLASSCD || '-' || T6.SCHOOL_KIND || '-' || T6.CURRICULUM_CD || '-' || ");
            }
            stb.append(                            " T6.SUBCLASSCD = T4.SUBCLASSCD ");
            stb.append("LEFT JOIN TOTAL_JISU_NUM T7 ON T7.SCHREGNO = T4.SCHREGNO AND T7.SUBCLASSCD = T4.SUBCLASSCD ");
            if (_hasAmikake) {
                if ((null == _param._kyoukaSougou1 && null == _param._kyoukaSougou2) || (null != _param._kyoukaSougou1 && null != _param._kyoukaSougou2)) {
                    stb.append(        " WHERE SUBSTR(T4.SUBCLASSCD,1,2) <= '" + KNJDefineSchool.subject_T + "' ");
                } else if (null != _param._kyoukaSougou1 && null == _param._kyoukaSougou2) {
                    stb.append(        " WHERE SUBSTR(T4.SUBCLASSCD,1,2) < '" + KNJDefineSchool.subject_T + "' ");
                } else if (null == _param._kyoukaSougou1 && null != _param._kyoukaSougou2) {
                    stb.append(        " WHERE SUBSTR(T4.SUBCLASSCD,1,2) = '" + KNJDefineSchool.subject_T + "' ");
                }
            }
            stb.append("ORDER BY T4.SUBCLASSCD");

            return stb.toString();
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     *  学校教育システム 賢者 [成績管理] 成績判定会議資料 ３．皆勤者
     */

    /*
     *  2005/08/08 Build
     *  2006/02/01 yamasihro・2学期制の場合、1月〜3月の出欠集計データのソート順による集計の不具合を修正 --
     */

    private static class AttendPerfect extends Base {

        List _AttendList = new ArrayList();

        boolean printSvf(
                final DB2UDB db2,
                final Vrw32alpWrap svf
        ) {
            if (0 != setSvfformFormat(svf)) {
                return false;
            }
            return printsvfMain(svf);
        }

        /**
         *  印刷処理
         */
        boolean printsvfMain(final Vrw32alpWrap svf) {
            boolean hasData = false;                               //該当データなしフラグ

            List attendList = _param._attendList;
            if (null == attendList) { return false; }
            for (Iterator lt = attendList.iterator(); lt.hasNext(); ) {
                AttendData attendData = (AttendData) lt.next();
                if (hasElected(attendData)) {
                    _AttendList.add(attendData);
                }
            }

            doSort();

            final List pageList = new ArrayList();
            List currentList = null;
            for (Iterator lt = _AttendList.iterator(); lt.hasNext(); ) {
                Object o = lt.next();
                if (null == currentList || currentList.size() >= 50) {
                    currentList = new ArrayList();
                    pageList.add(currentList);
                }
                currentList.add(o);
            }

            int num = 0;
            for (Iterator lt = pageList.iterator(); lt.hasNext(); ) {
                printsvfOutHead(svf);                    //SVF-FORM印刷
                final List atList = (List) lt.next();
                for (int i = 0; i < atList.size(); i++) {
                    AttendData ad = (AttendData) atList.get(i);
                    printsvfOutDetail(svf, ad, ++num, i + 1);
                    hasData = true;
                }
                svf.VrEndPage();
            }
            return hasData;
        }

        /**
         * 出欠データをソートします。
         */
        void doSort() {
            Collections.sort(_AttendList, new SortOrderHrclassCompare());
        }

        /**
         * 皆勤なら true.
         * @param attendData
         * @return
         */
        boolean hasElected(final AttendData attendData) {
            boolean b = true;
            if (null != attendData._absent && 0 < attendData._absent.intValue()) {
                b = false;
            }
            if (b && null != attendData._late && 0 < attendData._late.intValue()) {
                b = false;
            }
            if (b && null != attendData._early && 0 < attendData._early.intValue()) {
                b = false;
            }
            if (b && null != attendData._subclassAbsentInt && 0 < attendData._subclassAbsentInt.intValue()) {
                b = false;
            }
            if (b && null != attendData._subclassAbsentDouble && 0 < attendData._subclassAbsentDouble.doubleValue()) {
                b = false;
            }
            if (_param._isAttendPerfectSubclassLateEarly) {
                if (b && null != attendData._subclassLateEarly && 0 < attendData._subclassLateEarly.intValue()) {
                    b = false;
                }
            }
            return b;
        }

        /**
         *  帳票明細行印刷
         */
        void printsvfOutDetail(
                final Vrw32alpWrap svf,
                final AttendData ad,
                final int num,
                final int gyo
        ) {
            try {
                svf.VrsOutn( "NUMBER",       gyo, String.valueOf( num ) );
                svf.VrsOutn( "HR_ATTENDNO",  gyo, getHrName(_param, ad._hrClass, ad._attendno));
                svf.VrsOutn( "SCHREGNO",     gyo, ad._schregno);
                svf.VrsOutn( "NAME",         gyo, ad._name);

            } catch( Exception ex ) {
                log.error("error! ",ex);
            }
        }

        /**
         *  帳票ヘッダー等印刷
         */
        void printsvfOutHead(final Vrw32alpWrap svf) {
            svf.VrsOut( "NENDO",     _param._nendo);
            svf.VrsOut( "GRADE",     _param._printGrade );
            svf.VrsOut( "CONDITION", _param._printDate);
            svf.VrsOut( "DATE",      _param._nowDateWa );
        }

        int setSvfformFormat(final Vrw32alpWrap svf) {
            return svf.VrSetForm("KNJD232_3.frm", 1);
        }

        /**
         * @param paramap
         * @return SQL文を戻します。
         */
        private String prestatementRecordAttend() {
            StringBuffer stb = new StringBuffer();
            stb.append("WITH ");
            //学籍の表
            stb.append("SCHNO AS(");
            stb.append(    "SELECT  W1.SCHREGNO, W1.ATTENDNO, W1.HR_CLASS, W1.COURSECD, W4.NAME ");
            stb.append(           ",CASE WHEN W2.SCHREGNO IS NOT NULL THEN 1 WHEN W3.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS LEAVE ");
            stb.append(    "FROM    SCHREG_REGD_DAT W1 ");
            stb.append(            "INNER JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = W1.SCHREGNO ");
            stb.append(            "LEFT JOIN(");
            stb.append(                "SELECT  SCHREGNO ");
            stb.append(                "FROM    SCHREG_BASE_MST ");
            stb.append(                "WHERE   GRD_DIV IN ('2','3') ");
            stb.append(                    "AND '" + _param._year + "-04-01" + "' <= GRD_DATE ");
            stb.append(                    "AND GRD_DATE < '" + _param._attendToDate + "' ");
            stb.append(                "GROUP BY SCHREGNO ");
            stb.append(            ") W2 ON W2.SCHREGNO = W1.SCHREGNO ");
            stb.append(            "LEFT JOIN(");
            stb.append(                "SELECT  SCHREGNO ");
            stb.append(                "FROM    SCHREG_TRANSFER_DAT ");
            stb.append(                "WHERE   TRANSFERCD IN ('4') AND '" + _param._attendToDate + "' < TRANSFER_SDATE ");
            stb.append(                "GROUP BY SCHREGNO ");
            stb.append(            ") W3 ON W3.SCHREGNO = W1.SCHREGNO ");

            stb.append(           " LEFT JOIN SCHREG_TRANSFER_DAT T4 ON T4.SCHREGNO = W1.SCHREGNO ");
            stb.append(     "        AND T4.TRANSFERCD IN ('1', '2') ");
            stb.append(     "        AND (T4.TRANSFER_SDATE BETWEEN '" + _param._transFrom + "' AND '" + _param._transTo + "' ");
            stb.append(          "OR T4.TRANSFER_EDATE BETWEEN '" + _param._transFrom + "' AND '" + _param._transTo + "' ");
            stb.append(          "OR T4.TRANSFER_SDATE <= '" + _param._transFrom + "' AND T4.TRANSFER_EDATE >= '" + _param._transTo + "' ");
            stb.append(     "        ) ");

            stb.append(    "WHERE   W1.YEAR = '" + _param._year + "' ");
            stb.append(        "AND W1.SEMESTER = '" + _param._gakki + "' ");
            stb.append(        "AND W1.GRADE = '" + _param._grade + "' ");
            stb.append(        "AND NOT (W4.GRD_DIV IS NOT NULL AND W4.GRD_DATE IS NOT NULL AND W4.GRD_DIV IN('2','3') AND W4.GRD_DATE < '" + _param._date + "') ");
            stb.append(        "AND T4.SCHREGNO IS NULL");
            stb.append(") ");

            stb.append(", TEST_COUNTFLG AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.EXECUTEDATE, ");
            stb.append("         T1.PERIODCD, ");
            stb.append("         T1.CHAIRCD, ");
            stb.append("         '2' AS DATADIV ");
            stb.append("     FROM ");
            stb.append("         SCH_CHR_TEST T1, ");
            stb.append("         TESTITEM_MST_COUNTFLG_NEW T2 ");
            stb.append("     WHERE ");
            stb.append("         T2.YEAR           = T1.YEAR ");
            stb.append("         AND T2.SEMESTER   = T1.SEMESTER ");
            stb.append("         AND T2.TESTKINDCD = T1.TESTKINDCD ");
            stb.append("         AND T2.TESTITEMCD = T1.TESTITEMCD ");
            stb.append("         AND T2.COUNTFLG   = '0' ");
            stb.append(") ");

            // 時間割(留学・休学期間を含む)
            stb.append(", SCHREG_SCHEDULE_R AS(");
            stb.append(    "SELECT  T0.SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(        " T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
            }
            stb.append(            "T2.SUBCLASSCD AS SUBCLASSCD, T2.SEMESTER, T1.EXECUTEDATE, T1.PERIODCD, T1.CHAIRCD, T1.DATADIV, ");
            stb.append("             ATTEND_DI.REP_DI_CD AS YOMIKAEMAE, ");
            stb.append("             ATTEND_DI.MULTIPLY, ");
            stb.append("             CASE WHEN ATTEND_DI.ATSUB_REPL_DI_CD IS NOT NULL THEN ATTEND_DI.ATSUB_REPL_DI_CD ELSE ATTEND_DI.REP_DI_CD END AS DI_CD ");
            stb.append(           ",(SELECT 'X' FROM COURSE_MST T5 ");
            stb.append(             "WHERE  T5.COURSECD = T10.COURSECD ");
            stb.append(                "AND T1.PERIODCD BETWEEN T5.S_PERIODCD AND T5.E_PERIODCD)AS T_PERIOD ");
            stb.append(    "FROM    SCH_CHR_DAT T1 ");
            stb.append(    "INNER JOIN SEMESTER_MST T3 ON T3.YEAR = T1.YEAR ");
            stb.append(          "AND T3.SEMESTER <> '9' ");
            stb.append(          "AND T1.EXECUTEDATE BETWEEN T3.SDATE AND T3.EDATE ");
            stb.append(    "INNER JOIN CHAIR_DAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append(          "AND T2.SEMESTER = T3.SEMESTER ");
            stb.append(          "AND T2.CHAIRCD = T1.CHAIRCD ");
            stb.append(    "INNER JOIN CHAIR_STD_DAT T0 ON T0.YEAR = T1.YEAR ");
            stb.append(          "AND T0.SEMESTER = T2.SEMESTER ");
            stb.append(          "AND T0.CHAIRCD = T2.CHAIRCD ");
            stb.append(          "AND T1.EXECUTEDATE BETWEEN T0.APPDATE AND T0.APPENDDATE ");
            stb.append(    "INNER JOIN SCHNO T10 ON T0.SCHREGNO = T10.SCHREGNO ");
            stb.append(    "INNER JOIN NAME_YDAT T8 ON T8.YEAR = T1.YEAR ");
            stb.append(          "AND T8.NAMECD1 = 'B001' AND T1.PERIODCD = T8.NAMECD2 ");
            stb.append(    "LEFT JOIN ATTEND_DAT T5 ON T5.SCHREGNO = T0.SCHREGNO ");
            stb.append(          "AND T1.EXECUTEDATE = T5.ATTENDDATE ");
            stb.append(          "AND T1.PERIODCD = T5.PERIODCD ");
            stb.append(          "AND T1.CHAIRCD = T5.CHAIRCD ");
            stb.append("    LEFT JOIN ATTEND_DI_CD_DAT ATTEND_DI ON ATTEND_DI.YEAR = T5.YEAR AND ATTEND_DI.DI_CD = T5.DI_CD ");
            stb.append(    "WHERE   T1.YEAR = '" +  _param._year  + "' ");
            stb.append(          "AND T1.EXECUTEDATE BETWEEN '" + _param._attendFromDate + "' AND '" + _param._attendToDate + "' ");
            stb.append(          "AND T3.SEMESTER <= '" + _param._gakki + "' ");
            //                      学籍不在日を除外
            stb.append(          "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST T6 ");
            stb.append(                       "WHERE   T6.SCHREGNO = T0.SCHREGNO ");
            stb.append(                           "AND (( T6.ENT_DIV IN('4','5') AND T1.EXECUTEDATE < T6.ENT_DATE ) ");
            stb.append(                             "OR ( T6.GRD_DIV IN('2','3') AND T1.EXECUTEDATE > T6.GRD_DATE )) ) ");
            stb.append("    AND NOT EXISTS(SELECT ");
            stb.append("                       'X' ");
            stb.append("                   FROM ");
            stb.append("                       ATTEND_DAT T4 ");
            stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = T4.YEAR AND L1.DI_CD = T4.DI_CD ");
            stb.append("                   WHERE ");
            stb.append("                       T4.SCHREGNO = T0.SCHREGNO ");
            stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append("                       AND L1.REP_DI_CD = '27' "); // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
            stb.append("                  ) ");
            // 勤怠コード'28'は時間割にカウントしない
            stb.append("    AND NOT EXISTS(SELECT ");
            stb.append("                       'X' ");
            stb.append("                   FROM ");
            stb.append("                       ATTEND_DAT T4 ");
            stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = T4.YEAR AND L1.DI_CD = T4.DI_CD ");
            stb.append("                   WHERE ");
            stb.append("                       T4.SCHREGNO = T0.SCHREGNO ");
            stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
            stb.append("                       AND L1.REP_DI_CD = '28' ");
            stb.append("                  ) ");
            stb.append(") ");

            stb.append(", SCHREG_SCHEDULE_R_FOR_SUBCLASS AS(");
            stb.append(    "SELECT  T1.SCHREGNO, ");
            stb.append(            "T1.SUBCLASSCD, T1.SEMESTER, T1.EXECUTEDATE, T1.PERIODCD, T1.CHAIRCD, T1.DATADIV, T1.YOMIKAEMAE, T1.MULTIPLY, T1.DI_CD ");
            stb.append(           ",T1.T_PERIOD ");
            stb.append(    "FROM    SCHREG_SCHEDULE_R T1 ");
            stb.append(    "INNER JOIN SCHNO T10 ON T1.SCHREGNO = T10.SCHREGNO ");
            if( _param._definecode.useschchrcountflg ) {
                stb.append(    " WHERE ");
                stb.append(      " NOT EXISTS(SELECT  'X' FROM SCH_CHR_COUNTFLG T4 ");
                stb.append(                    "WHERE   T4.EXECUTEDATE = T1.EXECUTEDATE ");
                stb.append(                        "AND T4.PERIODCD = T1.PERIODCD ");
                stb.append(                        "AND T4.CHAIRCD = T1.CHAIRCD ");
                stb.append(                        "AND T1.DATADIV IN ('0', '1') ");
                stb.append(                        "AND T4.GRADE = '" + _param._grade + "' ");
                stb.append(                        "AND T4.HR_CLASS = T10.HR_CLASS ");
                stb.append(                        "AND T4.COUNTFLG = '0') ");
                stb.append(      "AND NOT EXISTS(SELECT 'X' FROM TEST_COUNTFLG TEST ");
                stb.append(                   "WHERE TEST.EXECUTEDATE  = T1.EXECUTEDATE ");
                stb.append(                       "AND TEST.PERIODCD = T1.PERIODCD ");
                stb.append(                       "AND TEST.CHAIRCD  = T1.CHAIRCD ");
                stb.append(                       "AND TEST.DATADIV  = T1.DATADIV) ");
            }
            stb.append(") ");

            // 時間割(留学・休学期間を含まない)
            stb.append(", T_ATTEND_DAT AS(");
            stb.append(    "SELECT  T1.SCHREGNO, T1.SUBCLASSCD, T1.SEMESTER, T1.EXECUTEDATE AS ATTENDDATE, T1.PERIODCD, T1.CHAIRCD, T1.YOMIKAEMAE, T1.DI_CD, T1.T_PERIOD ");
            stb.append(    "FROM    SCHREG_SCHEDULE_R T1 ");
            stb.append(    "WHERE   NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT T7 ");
            stb.append(                       "WHERE   T7.SCHREGNO = T1.SCHREGNO ");
            stb.append(                           "AND TRANSFERCD IN ('1','2') AND EXECUTEDATE BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ) ");
            stb.append(") ");

            // 時間割(留学・休学期間を含まない)
            stb.append(", T_ATTEND_DAT_FOR_SUBCLASS AS(");
            stb.append(    "SELECT  T1.SCHREGNO, T1.SUBCLASSCD, T1.SEMESTER, T1.EXECUTEDATE AS ATTENDDATE, T1.PERIODCD, T1.CHAIRCD, T1.YOMIKAEMAE, T1.MULTIPLY, T1.DI_CD, T1.T_PERIOD ");
            stb.append(    "FROM    SCHREG_SCHEDULE_R_FOR_SUBCLASS T1 ");
            stb.append(    "WHERE   NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT T7 ");
            stb.append(                       "WHERE   T7.SCHREGNO = T1.SCHREGNO ");
            stb.append(                           "AND TRANSFERCD IN ('1','2') AND EXECUTEDATE BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ) ");
            stb.append(") ");

            // 対象生徒の出欠データ（忌引・出停した日）
            stb.append(", T_ATTEND_DAT_B AS(");
            stb.append(    "SELECT  T1.SCHREGNO, T1.ATTENDDATE, ");
            stb.append(            "MIN(T1.PERIODCD) AS FIRST_PERIOD, ");
            stb.append(            "COUNT(DISTINCT T1.PERIODCD) AS PERIOD_CNT ");
            stb.append(    "FROM   T_ATTEND_DAT T1 ");
            stb.append(    "WHERE  T1.DI_CD IN('2','3','9','10') ");
            stb.append(    "GROUP BY T1.SCHREGNO, T1.ATTENDDATE ");
            stb.append(") ");

            // 休学数
            stb.append(", SCHREG_SUBCLASS_OFFDAYS AS(");
            stb.append(    "SELECT  T1.SCHREGNO, T1.SUBCLASSCD, T1.SEMESTER, COUNT(*) AS OFFDAYS ");
            stb.append(    "FROM    SCHREG_SCHEDULE_R_FOR_SUBCLASS T1 ");
            stb.append(    "WHERE   EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT T7 ");
            stb.append(                       "WHERE   T7.SCHREGNO = T1.SCHREGNO ");
            stb.append(                           "AND TRANSFERCD = '2' AND EXECUTEDATE BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ) ");
            stb.append(    "GROUP BY T1.SCHREGNO, T1.SUBCLASSCD, T1.SEMESTER ");
            stb.append(") ");

            //生徒・受講日別の最小校時・最大校時の表（時間割データから）
            stb.append(", T_PERIOD_CNT AS(");
            stb.append(    "SELECT  T1.SCHREGNO, T1.ATTENDDATE AS EXECUTEDATE, ");
            stb.append(            "MIN(T1.PERIODCD) AS FIRST_PERIOD, ");
            stb.append(            "MAX(T1.PERIODCD) AS LAST_PERIOD, ");
            stb.append(            "COUNT(DISTINCT T1.PERIODCD) AS PERIOD_CNT ");
            stb.append(    "FROM   T_ATTEND_DAT T1 ");
            stb.append("    WHERE NOT EXISTS(SELECT ");
            stb.append("                       'X' ");
            stb.append("                   FROM ");
            stb.append("                       ATTEND_DAT T4 ");
            stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = T4.YEAR AND L1.DI_CD = T4.DI_CD ");
            stb.append("                   WHERE ");
            stb.append("                       T4.SCHREGNO = T1.SCHREGNO ");
            stb.append("                       AND T4.ATTENDDATE = T1.ATTENDDATE ");
            stb.append("                       AND L1.REP_DI_CD = '27' "); // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
            stb.append("                  ) ");
            // 勤怠コード'28'は時間割にカウントしない
            stb.append("    AND NOT EXISTS(SELECT ");
            stb.append("                       'X' ");
            stb.append("                   FROM ");
            stb.append("                       ATTEND_DAT T4 ");
            stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = T4.YEAR AND L1.DI_CD = T4.DI_CD ");
            stb.append("                   WHERE ");
            stb.append("                       T4.SCHREGNO = T1.SCHREGNO ");
            stb.append("                       AND T4.ATTENDDATE = T1.ATTENDDATE ");
            stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
            stb.append("                       AND L1.REP_DI_CD = '28' ");
            stb.append("                  ) ");
            stb.append(    "GROUP BY T1.SCHREGNO, T1.ATTENDDATE ");
            stb.append(") ");

            if ("1".equals(_param._knjSchoolMst._syukessekiHanteiHou)) {
                //対象生徒の日単位のデータ（忌引・出停した日）
                stb.append(" , T_PERIOD_SUSPEND_MOURNING AS( ");
                stb.append(" SELECT ");
                stb.append("    T0.SCHREGNO, ");
                stb.append("    T0.EXECUTEDATE ");
                stb.append(" FROM ");
                stb.append("    T_PERIOD_CNT T0, ");
                stb.append("    T_ATTEND_DAT_B T1 ");
                stb.append(" WHERE ");
                stb.append("        T0.SCHREGNO = T1.SCHREGNO ");
                stb.append("    AND T0.EXECUTEDATE = T1.ATTENDDATE ");
                stb.append("    AND T0.FIRST_PERIOD = T1.FIRST_PERIOD ");
                stb.append("    AND T0.PERIOD_CNT = T1.PERIOD_CNT ");
                stb.append(" ) ");
            }

            // 休学日数
            stb.append(" , SCHREG_SEM_OFFDAYS AS(");
            stb.append(    "SELECT  T1.SCHREGNO, T1.SEMESTER, COUNT(DISTINCT T1.EXECUTEDATE) AS OFFDAYS ");
            stb.append(    "FROM    SCHREG_SCHEDULE_R T1 ");
            stb.append(    "WHERE   EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT T7 ");
            stb.append(                       "WHERE   T7.SCHREGNO = T1.SCHREGNO ");
            stb.append(                           "AND TRANSFERCD = '2' AND EXECUTEDATE BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ) ");
            stb.append(    "GROUP BY T1.SCHREGNO, T1.SEMESTER ");
            stb.append(") ");

            //生徒・科目・学期別欠課集計の表（出欠データと集計テーブルを合算）
            stb.append(", SCH_ATTEND_SUM AS(");
            stb.append(    "SELECT  T1.SCHREGNO, T1.SUBCLASSCD, T1.SEMESTER ");
            stb.append(           ",SUM(CASE WHEN DI_CD IN('4','5','6','14','11','12','13' ");
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
            if ("1".equals(_param._knjSchoolMst._subKoudome)) {
                stb.append(          ",'25','26'");
            }
            stb.append(            ") THEN 1 ELSE 0 END) AS ABSENT1 ");
            stb.append(           ",SUM(CASE WHEN DI_CD IN('15','16','23','24') THEN SMALLINT(VALUE(T1.MULTIPLY, '1')) ELSE 0 END)AS LATE_EARLY ");
            stb.append(    "FROM T_ATTEND_DAT_FOR_SUBCLASS T1 ");
            stb.append(    "GROUP BY T1.SCHREGNO, T1.SUBCLASSCD, T1.SEMESTER ");
            if ("1".equals(_param._knjSchoolMst._subOffDays)) {
                stb.append(    "UNION ALL ");
                stb.append(    "SELECT  T2.SCHREGNO, T2.SUBCLASSCD, T2.SEMESTER ");
                stb.append(           ",SUM(T2.OFFDAYS) AS ABSENT1 ");
                stb.append(           ",SUM(0) AS LATE_EARLY ");
                stb.append(    "FROM SCHREG_SUBCLASS_OFFDAYS T2 ");
                stb.append(    "GROUP BY T2.SCHREGNO, T2.SUBCLASSCD, T2.SEMESTER ");
            }
            stb.append(    "UNION ALL ");
            stb.append(    "SELECT  T1.SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(        " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append(            "T1.SUBCLASSCD AS SUBCLASSCD, SEMESTER ");
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
            if ("1".equals(_param._knjSchoolMst._subKoudome)) {
                stb.append(          "+ VALUE(KOUDOME,0)");
            }
            stb.append(           ") AS ABSENT1 ");
            stb.append(           ",SUM(VALUE(LATE,0) + VALUE(EARLY,0)) AS LATE_EARLY ");
            stb.append(    "FROM    ATTEND_SUBCLASS_DAT T1 ");
            stb.append(    "WHERE   YEAR = '" + _param._year + "' ");
            stb.append(        "AND SEMESTER <= '" + _param._gakki + "' ");
            stb.append(        "AND (CASE WHEN INT(T1.MONTH) < 4 THEN RTRIM(CHAR(INT(T1.SEMESTER) + 1 )) ELSE T1.SEMESTER END )||T1.MONTH <= '" + KNJC053_BASE.retSemesterMonthValue ( _param._attendLastMonth ) + "' ");   //--NO004
            stb.append(        "AND EXISTS(SELECT  'X' FROM SCHNO T2 ");
            stb.append(                   "WHERE   T2.SCHREGNO = T1.SCHREGNO ");
            stb.append(                   "GROUP BY SCHREGNO) ");
            stb.append(    "GROUP BY T1.SCHREGNO, T1.SEMESTER, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(        " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append(             "T1.SUBCLASSCD ");
            stb.append(") ");

            //ペナルティー欠課を加味した生徒欠課集計の表（出欠データと集計テーブルを合算）
            stb.append(", SCH_ABSENT_SUM AS(");
            stb.append("    SELECT SCHREGNO,SUM(ABSENT) AS ABSENT ");
            stb.append("    FROM ( ");
            stb.append("          SELECT  SCHREGNO, SUBCLASSCD ");
            if (1 == _param._definecode.absent_cov || 3 == _param._definecode.absent_cov) {
                //遅刻・早退を学期で欠課換算する
                stb.append("            , SUM(ABSENT)AS ABSENT ");
                stb.append("      FROM (");
                stb.append("            SELECT  SCHREGNO, SUBCLASSCD");
                if (1 == _param._definecode.absent_cov || _param.isGakunenMatu()) {
                    stb.append("              , VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / " + _param._definecode.absent_cov_late + " AS ABSENT ");
                } else {
                    stb.append("              , FLOAT(DECIMAL(VALUE(SUM(ABSENT1),0) + VALUE(SUM(FLOAT(LATE_EARLY)),0) / " + _param._definecode.absent_cov_late + ",5,1)) AS ABSENT ");
                }
                stb.append("            FROM    SCH_ATTEND_SUM ");
                stb.append("            GROUP BY SCHREGNO, SUBCLASSCD, SEMESTER ");
                stb.append("      )W1 ");
                stb.append("      GROUP BY SCHREGNO, SUBCLASSCD ");
            } else if (2 == _param._definecode.absent_cov || 4 == _param._definecode.absent_cov) {
                //遅刻・早退を年間で欠課換算する
                if (_param._definecode.absent_cov == 2 || _param.isGakunenMatu()) {
                    stb.append("        , VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / " + _param._definecode.absent_cov_late + " AS ABSENT ");
                } else {
                    stb.append("        , FLOAT(DECIMAL(VALUE(SUM(ABSENT1),0) + VALUE(SUM(FLOAT(LATE_EARLY)),0) / " + _param._definecode.absent_cov_late + ",5,1)) AS ABSENT ");
                }
                stb.append("      FROM    SCH_ATTEND_SUM ");
                stb.append("      GROUP BY SCHREGNO, SUBCLASSCD ");
            } else {
                //遅刻・早退を欠課換算しない
                stb.append("            , SUM(ABSENT1)AS ABSENT ");
                stb.append("      FROM    SCH_ATTEND_SUM ");
                stb.append("      GROUP BY SCHREGNO, SUBCLASSCD ");
            }
            stb.append("    )T1 ");
            stb.append("    GROUP BY SCHREGNO ");
            stb.append("    HAVING 0 < SUM(ABSENT) ");
            stb.append(") ");

            //生徒遅刻・早退集計の表（出欠データと集計テーブルを合算）
            stb.append(", SCH_LATE_EARLY_SUM AS(");
            stb.append("    SELECT SCHREGNO,SUM(LATE_EARLY) AS LATE_EARLY ");
            stb.append("    FROM SCH_ATTEND_SUM ");
            stb.append("    GROUP BY SCHREGNO ");
            stb.append("    HAVING 0 < SUM(LATE_EARLY) ");
            stb.append(") ");

            //メイン表
            stb.append("SELECT  TT0.SCHREGNO, tt0.hr_class, tt0.attendno, tt0.name ");
            stb.append(       ",TT0.LEAVE ");
            stb.append(       ",VALUE(TT5.SICK,0) + VALUE(TT5.NOTICE,0) + VALUE(TT5.NONOTICE,0) + VALUE(TT7.ABSENT,0)");
            if ("1".equals(_param._knjSchoolMst._semOffDays)) {
                stb.append(   " + VALUE(TT9.OFFDAYS,0) ");
            }
            stb.append(       " AS ABSENT ");
            stb.append(       ",VALUE(TT6.LATE,0) + VALUE(TT10.LATE,0) + VALUE(TT7.LATE,0) AS LATE ");
            stb.append(       ",VALUE(TT6.EARLY,0) + VALUE(TT11.EARLY,0) + VALUE(TT7.EARLY,0) AS EARLY ");
            stb.append(       ",VALUE(TT8.ABSENT,0) AS SUBCLASS_ABSENT ");
            stb.append(       ",VALUE(TT88.LATE_EARLY,0) AS SUBCLASS_LATE_EARLY ");
            stb.append("FROM    SCHNO TT0 ");
            //個人別授業日数
            stb.append(   "LEFT OUTER JOIN(");
            stb.append(      "SELECT  SCHREGNO, COUNT(DISTINCT EXECUTEDATE) AS LESSON ");
            stb.append(      "FROM    T_PERIOD_CNT ");
            stb.append(      "GROUP BY SCHREGNO ");
            stb.append(   ") TT1 ON TT0.SCHREGNO = TT1.SCHREGNO ");
            //個人別出停日数
            stb.append(   "LEFT OUTER JOIN(");
            stb.append(      "SELECT W1.SCHREGNO, COUNT(DISTINCT W1.ATTENDDATE) AS SUSPEND ");
            stb.append(      "FROM   T_ATTEND_DAT W1 ");
            stb.append(      "WHERE  W1.DI_CD IN ('2','9') ");
            stb.append(         "AND W1.T_PERIOD IS NOT NULL ");
            if ("1".equals(_param._knjSchoolMst._syukessekiHanteiHou)) {
                stb.append(     "AND (W1.SCHREGNO, W1.ATTENDDATE) IN (SELECT T0.SCHREGNO, T0.EXECUTEDATE FROM T_PERIOD_SUSPEND_MOURNING T0) ");
            }
            stb.append(      "GROUP BY W1.SCHREGNO ");
            stb.append(   ") TT3 ON TT0.SCHREGNO = TT3.SCHREGNO ");
            //個人別忌引日数
            stb.append(   "LEFT OUTER JOIN(");
            stb.append(      "SELECT W1.SCHREGNO, COUNT(DISTINCT W1.ATTENDDATE) AS MOURNING ");
            stb.append(      "FROM   T_ATTEND_DAT W1 ");
            stb.append(      "WHERE  W1.DI_CD IN ('3','10') ");
            stb.append(         "AND W1.T_PERIOD IS NOT NULL ");
            if ("1".equals(_param._knjSchoolMst._syukessekiHanteiHou)) {
                stb.append(     "AND (W1.SCHREGNO, W1.ATTENDDATE) IN (SELECT T0.SCHREGNO, T0.EXECUTEDATE FROM T_PERIOD_SUSPEND_MOURNING T0) ");
            }
            stb.append(      "GROUP BY W1.SCHREGNO ");
            stb.append(   ") TT4 ON TT0.SCHREGNO = TT4.SCHREGNO ");
            //個人別欠席日数
            stb.append(   "LEFT OUTER JOIN(");
            stb.append(      "SELECT  W0.SCHREGNO, ");
            stb.append(              "SUM(CASE ATTEND_DI.REP_DI_CD WHEN '4' THEN 1 WHEN '11' THEN 1 ELSE 0 END) AS SICK, ");
            stb.append(              "SUM(CASE ATTEND_DI.REP_DI_CD WHEN '5' THEN 1 WHEN '12' THEN 1 ELSE 0 END) AS NOTICE, ");
            stb.append(              "SUM(CASE ATTEND_DI.REP_DI_CD WHEN '6' THEN 1 WHEN '13' THEN 1 ELSE 0 END) AS NONOTICE ");
            stb.append(      "FROM    ATTEND_DAT W0 ");
            stb.append("              LEFT JOIN ATTEND_DI_CD_DAT ATTEND_DI ON ATTEND_DI.YEAR = W0.YEAR AND ATTEND_DI.DI_CD = W0.DI_CD, ");
            stb.append(         "(");
            stb.append(         "SELECT  T0.SCHREGNO, T0.EXECUTEDATE ");
            if ("1".equals(_param._knjSchoolMst._syukessekiHanteiHou)) {
                stb.append(            ",T2.FIRST_PERIOD ");
            } else {
                stb.append(            ",T0.FIRST_PERIOD ");
            }
            stb.append(         "FROM    T_PERIOD_CNT T0, ");
            stb.append(            "(");
            stb.append(            "SELECT  W1.SCHREGNO, W1.ATTENDDATE, ");
            stb.append(                    "MIN(W1.PERIODCD) AS FIRST_PERIOD, ");
            stb.append(                    "COUNT(W1.PERIODCD) AS PERIOD_CNT ");
            stb.append(            "FROM    T_ATTEND_DAT W1 ");
            stb.append(            "WHERE   W1.YOMIKAEMAE IN ('4','5','6','11','12','13' ");
            if ("1".equals(_param._knjSchoolMst._syukessekiHanteiHou)) {
                stb.append("                            ,'2','9','3','10' ");
            }
            stb.append(                                ") ");
            stb.append(                "AND W1.T_PERIOD IS NOT NULL ");
            stb.append(            "GROUP BY W1.SCHREGNO, W1.ATTENDDATE ");
            stb.append(            ") T1 ");
            if ("1".equals(_param._knjSchoolMst._syukessekiHanteiHou)) {
                stb.append("         INNER JOIN ( ");
                stb.append(            "SELECT  W1.SCHREGNO, W1.ATTENDDATE, ");
                stb.append(                    "MIN(W1.PERIODCD) AS FIRST_PERIOD, ");
                stb.append(                    "COUNT(W1.PERIODCD) AS PERIOD_CNT ");
                stb.append(            "FROM    T_ATTEND_DAT W1 ");
                stb.append(            "WHERE   W1.YOMIKAEMAE IN ('4','5','6','11','12','13') ");
                stb.append(                "AND W1.T_PERIOD IS NOT NULL ");
                stb.append(            "GROUP BY W1.SCHREGNO, W1.ATTENDDATE ");
                stb.append("             ) T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.ATTENDDATE = T1.ATTENDDATE ");
            }
            stb.append(         "WHERE   T0.SCHREGNO = T1.SCHREGNO AND ");
            stb.append(                 "T0.EXECUTEDATE = T1.ATTENDDATE AND ");
            stb.append(                 "T0.FIRST_PERIOD = T1.FIRST_PERIOD AND ");
            stb.append(                 "T0.PERIOD_CNT = T1.PERIOD_CNT ");
            stb.append(         ") W1 ");
            stb.append(      "WHERE   W0.SCHREGNO = W1.SCHREGNO AND ");
            stb.append(              "W0.ATTENDDATE = W1.EXECUTEDATE AND ");
            stb.append(              "W0.PERIODCD = W1.FIRST_PERIOD ");
            if ("1".equals(_param._knjSchoolMst._syukessekiHanteiHou)) {
                stb.append(     "AND (W1.SCHREGNO, W1.EXECUTEDATE) NOT IN (SELECT T0.SCHREGNO, T0.EXECUTEDATE FROM T_PERIOD_SUSPEND_MOURNING T0) ");
            }
            stb.append(      "GROUP BY W0.SCHREGNO ");
            stb.append(   ")TT5 ON TT0.SCHREGNO = TT5.SCHREGNO ");
            //個人別遅刻・早退回数
            stb.append(   "LEFT OUTER JOIN(");
            stb.append(      "SELECT  T0.SCHREGNO, COUNT(T2.ATTENDDATE) AS LATE, COUNT(T3.ATTENDDATE) AS EARLY ");
            stb.append(      "FROM    T_PERIOD_CNT T0 ");
            stb.append(      "INNER JOIN(");
            stb.append(         "SELECT  W1.SCHREGNO, W1.ATTENDDATE, COUNT(W1.PERIODCD) AS PERIOD_CNT ");
            stb.append(         "FROM    T_ATTEND_DAT W1 ");
            stb.append(         "WHERE   W1.YOMIKAEMAE NOT IN ('0','14','15','16','23','24','29','30','31','32') ");
            stb.append(             "AND T_PERIOD IS NOT NULL ");
            if (!"1".equals(_param._knjSchoolMst._syukessekiHanteiHou)) {
                stb.append(             "AND NOT EXISTS(SELECT 'X' FROM T_ATTEND_DAT_B W2 ");
                stb.append(                            "WHERE W2.SCHREGNO = W1.SCHREGNO AND W2.ATTENDDATE = W1.ATTENDDATE) ");
            }
            stb.append(         "GROUP BY W1.SCHREGNO, W1.ATTENDDATE ");
            stb.append(         ")T1 ON T0.SCHREGNO = T1.SCHREGNO AND T0.EXECUTEDATE = T1.ATTENDDATE AND ");
            stb.append(                                              "T0.PERIOD_CNT != T1.PERIOD_CNT ");
            stb.append(      "LEFT OUTER JOIN(");
            stb.append(         "SELECT  SCHREGNO ,ATTENDDATE ,PERIODCD ");
            stb.append(         "FROM    T_ATTEND_DAT ");
            stb.append(         "WHERE   YOMIKAEMAE IN ('4','5','6','11','12','13') ");
            stb.append(             "AND T_PERIOD IS NOT NULL ");
            stb.append(         ")T2 ON T0.SCHREGNO = T2.SCHREGNO AND T0.EXECUTEDATE  = T2.ATTENDDATE AND ");
            stb.append(                                              "T0.FIRST_PERIOD = T2.PERIODCD ");
            stb.append(      "LEFT OUTER JOIN(");
            stb.append(         "SELECT  SCHREGNO ,ATTENDDATE ,PERIODCD ");
            stb.append(         "FROM    T_ATTEND_DAT ");
            stb.append(         "WHERE   YOMIKAEMAE IN ('4','5','6') ");
            stb.append(             "AND T_PERIOD IS NOT NULL ");
            stb.append(         ")T3 ON T0.SCHREGNO= T3.SCHREGNO AND T0.EXECUTEDATE = T3.ATTENDDATE AND ");
            stb.append(                                             "T0.LAST_PERIOD = T3.PERIODCD ");
            stb.append(      "GROUP BY T0.SCHREGNO ");
            stb.append(   ")TT6 ON TT0.SCHREGNO = TT6.SCHREGNO ");

            //個人別遅刻回数 05/03/04
            stb.append(   "LEFT OUTER JOIN(");
            stb.append(      "SELECT  T0.SCHREGNO, COUNT(T2.ATTENDDATE) AS LATE ");
            stb.append(      "FROM    T_PERIOD_CNT T0 ");
            stb.append(      "INNER JOIN(");
            stb.append(         "SELECT  W1.SCHREGNO ,W1.ATTENDDATE ,W1.PERIODCD ");
            stb.append(         "FROM    T_ATTEND_DAT W1 ");
            stb.append(         "WHERE   W1.YOMIKAEMAE IN ('15','23','24','29','31','32') ");
            stb.append(             "AND W1.T_PERIOD IS NOT NULL ");
            if (!"1".equals(_param._knjSchoolMst._syukessekiHanteiHou)) {
                stb.append(             "AND NOT EXISTS(SELECT 'X' FROM T_ATTEND_DAT_B W2 ");
                stb.append(                            "WHERE W2.SCHREGNO = W1.SCHREGNO AND W2.ATTENDDATE = W1.ATTENDDATE) ");
            }
            stb.append(         ")T2 ON T0.SCHREGNO = T2.SCHREGNO AND T0.EXECUTEDATE  = T2.ATTENDDATE AND ");
            stb.append(                                              "T0.FIRST_PERIOD = T2.PERIODCD ");
            stb.append(      "GROUP BY T0.SCHREGNO ");
            stb.append(   ")TT10 ON TT0.SCHREGNO = TT10.SCHREGNO ");

            //個人別早退回数 05/03/04
            stb.append(   "LEFT OUTER JOIN(");
            stb.append(      "SELECT  T0.SCHREGNO, COUNT(T2.ATTENDDATE) AS EARLY ");
            stb.append(      "FROM    T_PERIOD_CNT T0 ");
            stb.append(      "INNER JOIN(");
            stb.append(         "SELECT  W1.SCHREGNO ,W1.ATTENDDATE ,W1.PERIODCD ");
            stb.append(         "FROM    T_ATTEND_DAT W1 ");
            stb.append(         "WHERE   W1.YOMIKAEMAE IN ('16', '30', '31', '32') ");
            stb.append(             "AND W1.T_PERIOD IS NOT NULL ");
            if (!"1".equals(_param._knjSchoolMst._syukessekiHanteiHou)) {
                stb.append(             "AND NOT EXISTS(SELECT 'X' FROM T_ATTEND_DAT_B W2 ");
                stb.append(                            "WHERE W2.SCHREGNO = W1.SCHREGNO AND W2.ATTENDDATE = W1.ATTENDDATE) ");
            }
            stb.append(         ")T2 ON T0.SCHREGNO = T2.SCHREGNO AND T0.EXECUTEDATE  = T2.ATTENDDATE AND ");
            stb.append(                                              "T0.LAST_PERIOD = T2.PERIODCD ");
            stb.append(      "GROUP BY T0.SCHREGNO ");
            stb.append(   ")TT11 ON TT0.SCHREGNO = TT11.SCHREGNO ");

            //月別集計データから集計した表
            stb.append(   "LEFT JOIN(");
            stb.append(      "SELECT SCHREGNO, ");
            stb.append(                   "SUM(LESSON) AS LESSON, ");
            stb.append(                   "SUM(MOURNING) AS MOURNING, ");
            stb.append(                   "SUM(SUSPEND) AS SUSPEND, ");
            stb.append(                   "SUM( VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0)");
            if ("1".equals(_param._knjSchoolMst._semOffDays)) {
                stb.append(               " + VALUE(OFFDAYS,0) ");
            }
            stb.append(                   ") AS ABSENT, ");
            stb.append(                   "SUM(LATE) AS LATE, ");
            stb.append(                   "SUM(EARLY) AS EARLY ");
            stb.append(            "FROM   ATTEND_SEMES_DAT W1 ");
            stb.append(            "WHERE  YEAR = '" + _param._year + "' AND ");
            stb.append(                   "(CASE WHEN INT(W1.MONTH) < 4 THEN RTRIM(CHAR(INT(W1.SEMESTER) + 1 )) ELSE W1.SEMESTER END )||W1.MONTH <= '" + KNJC053_BASE.retSemesterMonthValue ( _param._attendLastMonth ) + "' AND ");   //--NO004
            stb.append(                   "EXISTS(");
            stb.append(                       "SELECT  'X' ");
            stb.append(                       "FROM    SCHNO W2 ");
            stb.append(                       "WHERE   W1.SCHREGNO = W2.SCHREGNO)");
            stb.append(            "GROUP BY SCHREGNO ");
            stb.append(   ")TT7 ON TT0.SCHREGNO = TT7.SCHREGNO ");

            //休学日数の表
            stb.append("LEFT JOIN SCHREG_SEM_OFFDAYS TT9 ON TT0.SCHREGNO = TT9.SCHREGNO ");

            //欠課の表
            stb.append("LEFT JOIN SCH_ABSENT_SUM TT8 ON TT0.SCHREGNO = TT8.SCHREGNO ");

            //遅刻・早退の表
            stb.append("LEFT JOIN SCH_LATE_EARLY_SUM TT88 ON TT0.SCHREGNO = TT88.SCHREGNO ");

            return stb.toString();
        }

        /**
         * 出欠データのリストを作成します。<br>
         * また、Map paramap に "ATTEND_LIST"をキーとした出欠データのリストを追加します。
         * @param db2
         */
        void createAttendData(final DB2UDB db2) {
            final List list = new ArrayList();
            try {
                final boolean isNotInt = isDoubleValue();
                final String attendSql = prestatementRecordAttend();
                PreparedStatement ps = db2.prepareStatement(attendSql);
                log.debug("attendSql = " + attendSql);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    list.add(new AttendData(rs, isNotInt));
                }
                db2.commit();
                rs.close();
                ps.close();
            } catch (SQLException e) {
                log.error("SQLException", e);
            } catch (Exception e) {
                log.error("Exception", e);
            }

            if (0 < list.size()) {
                _param._attendList = list;
            }
        }

        /**
         * 欠課数の算出が小数点と判別すれば、Trueを戻します。<br>
         * 小数点は、欠課数換算コード KNJDefineSchool.absent_cov が '3' または '4' の場合です。<br>
         * 但し、'3'で"学年末"指定の場合は除外します。
         * @return
         */
        boolean isDoubleValue() {
            if (3 == _param._definecode.absent_cov && !_param.isGakunenMatu()) {
                return true;
            }
            if (4 == _param._definecode.absent_cov && !_param.isGakunenMatu()) {
                return true;
            }
            return false;
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     *  学校教育システム 賢者 [成績管理] 成績判定会議資料 ４．出欠状況（優良）
     */

    /*
     *  2005/08/08 Build
     *  2007/03/02 m-yama   NO001 出欠対象日付出力を追加した。
     *
     */

    public class AttendGood extends AttendPerfect {

        void doSort(final Map paramap) {
            Collections.sort(_AttendList, new SortOrderHrclassCompare());
        }


        boolean hasElected(
                final AttendData attendData
        ) {
            boolean b = true;
            Integer con = _param._absent4;
            Integer dat = attendData._absent;
            if (null != con && null != dat && con.intValue() < dat.intValue()) { b = false; }

            con = _param._late4;
            dat = attendData._late;
            if (null != con && null != dat && con.intValue() < dat.intValue()) { b = false; }

            con = _param._early4;
            dat = attendData._early;
            if (null != con && null != dat && con.intValue() < dat.intValue()) { b = false; }

            con = _param._subClassAbsent4;
            dat = attendData._subclassAbsentInt;
            if (null != con && null != dat && con.intValue() < dat.intValue()) { b = false; }
            Double notintdat = attendData._subclassAbsentDouble;
            if (null != con && null != notintdat && con.doubleValue() < notintdat.doubleValue()) { b = false; }

            return b;
        }

        /**
         *  帳票明細行印刷
         *
         */
        void printsvfOutDetail(
                final Vrw32alpWrap svf,
                final AttendData ad,
                final int num,
                final int gyo
        ) {
            try {
                svf.VrsOutn( "NUMBER",       gyo, String.valueOf( num ) );
                svf.VrsOutn( "HR_ATTENDNO",  gyo, getHrName(_param, ad._hrClass, ad._attendno));
                svf.VrsOutn( "SCHREGNO",     gyo, ad._schregno);
                svf.VrsOutn( "NAME",         gyo, ad._name);
                if (null != ad._late) { svf.VrsOutn( "LATE", gyo, ad._late.toString()); }
                if (null != ad._early) { svf.VrsOutn( "LEAVE", gyo, ad._early.toString()); }
                if (null != ad._absent) { svf.VrsOutn( "ABSENT", gyo, ad._absent.toString()); }
                if (null != ad._subclassAbsentDouble) { svf.VrsOutn( "KEKKA", gyo, _param._absentFmt.format(ad._subclassAbsentDouble)); }
                if (null != ad._subclassAbsentInt) { svf.VrsOutn( "KEKKA", gyo, _param._absentFmt.format(ad._subclassAbsentInt)); }
            } catch( Exception ex ) {
                log.error("error! ",ex);
            }
        }

        /**
         *  帳票ヘッダー等印刷
         */
        void printsvfOutHead(
                final Vrw32alpWrap svf
        ) {
            svf.VrsOut( "NENDO",      _param._nendo);
            svf.VrsOut( "GRADE",      _param._printGrade );
            svf.VrsOut( "DATE",       _param._nowDateWa );
            svf.VrsOut( "DATE2",      _param._printDate);  //NO001

            String str[] = new String[4];
            if( null != _param._late4            )str[0] = "遅刻が" + ( _param._late4 ).toString()            + "回以下";
            if( null != _param._early4           )str[1] = "早退が" + ( _param._early4 ).toString()           + "回以下";
            if( null != _param._absent4          )str[2] = "欠席が" + ( _param._absent4 ).toString()          + "回以下";
            if( null != _param._subClassAbsent4 )str[3] = "欠課が" + ( _param._subClassAbsent4 ).toString() + "回以下";

            StringBuffer stb = new StringBuffer();
            int count = 0;
            int j = 0;
            for( int i = 0 ; i < str.length ; i++ ) {
                if( str[i] == null )continue;
                count++;
                if( count == 2 )stb.append("、");
                stb.append( str[i] );
                if( count % 2 == 0 ) {
                    svf.VrsOut( "CONDITION" + ( ++j ) ,   stb.toString() );
                    count = 0;
                    stb = new StringBuffer();
                }
            }
            if( 0 < count ) svf.VrsOut( "CONDITION" + ( ++j ) ,   stb.toString() );
        }

        int setSvfformFormat(final Vrw32alpWrap svf) {
            return svf.VrSetForm("KNJD232_4.frm", 1);
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     *  学校教育システム 賢者 [成績管理] 成績判定会議資料  ５．出欠状況（不良）
     */

    /*
     *  2005/08/08 Build
     *  2005/08/25 Modify 出欠状況抽出条件を変更 => 回数が指定されたものだけを条件とする
     *  2007/03/02 m-yama   NO001 出欠対象日付出力を追加した。
     */

    public class AttendPoor extends AttendPerfect {

        List _order;

        void doSort(final Map paramap) {
            _order = createOrder(paramap);
            if (null == _order) {
                Collections.sort(_AttendList, new SortOrderHrclassCompare());
            } else {
                Collections.sort(_AttendList, new SortOrderAttendCompare(_order));
            }
        }

        boolean hasElected(
                final AttendData attendData
        ) {
            boolean b = false;
            Integer con = _param._absent5;
            Integer dat = attendData._absent;
            if (null != con && null != dat && con.intValue() <= dat.intValue()) { b = true; }

            con = _param._late5;
            dat = attendData._late;
            if (null != con && null != dat && con.intValue() <= dat.intValue()) { b = true; }

            con = _param._early5;
            dat = attendData._early;
            if (null != con && null != dat && con.intValue() <= dat.intValue()) { b = true; }

            con = _param._subClassAbsent5;
            dat = attendData._subclassAbsentInt;
            if (null != con && null != dat && con.intValue() <= dat.intValue()) { b = true; }
            Double notintdat = attendData._subclassAbsentDouble;
            if (null != con && null != notintdat && con.doubleValue() <= notintdat.doubleValue()) { b = true; }

            return b;
        }

        /**
         *  帳票明細行印刷
         */
        void printsvfOutDetail(
                final Vrw32alpWrap svf,
                final AttendData ad,
                final int num,
                final int gyo
        ) {
            try {
                svf.VrsOutn("NUMBER", gyo, String.valueOf(num));
                svf.VrsOutn("HR_ATTENDNO", gyo, getHrName(_param, ad._hrClass, ad._attendno));
                svf.VrsOutn("SCHREGNO", gyo, ad._schregno);
                svf.VrsOutn("NAME", gyo, ad._name);

                if (null != ad._late) {
                    svf.VrsOutn( "LATE", gyo, ad._late.toString());
                }
                if (null != ad._early) {
                    svf.VrsOutn( "LEAVE", gyo, ad._early.toString());
                }
                if (null != ad._absent) {
                    svf.VrsOutn( "ABSENT", gyo, ad._absent.toString());
                }
                if (null != ad._subclassAbsentDouble) {
                    svf.VrsOutn( "KEKKA", gyo, _param._absentFmt.format(ad._subclassAbsentDouble));
                }
                if (null != ad._subclassAbsentInt) {
                    svf.VrsOutn( "KEKKA", gyo, _param._absentFmt.format(ad._subclassAbsentInt));
                }
            } catch( Exception ex ) {
                log.error("error! ",ex);
            }
        }

        /**
         *  帳票ヘッダー等印刷
         */
        void printsvfOutHead(
                final Vrw32alpWrap svf
        ) {
            svf.VrsOut( "NENDO",      _param._nendo);
            svf.VrsOut( "GRADE",      _param._printGrade );
            svf.VrsOut( "DATE",       _param._nowDateWa );
            svf.VrsOut( "DATE2",      _param._printDate);

            String str[] = new String[4];
            if( null != _param._late5            )str[0] = "遅刻が" + _param._late5.toString()            + "回以上";
            if( null != _param._early5           )str[1] = "早退が" + _param._early5.toString()           + "回以上";
            if( null != _param._absent5          )str[2] = "欠席が" + _param._absent5.toString()          + "回以上";
            if( null != _param._subClassAbsent5 )str[3] = "欠課が" + _param._subClassAbsent5.toString() + "回以上";

            StringBuffer stb = new StringBuffer();
            int count = 0;
            int j = 0;
            for( int i = 0 ; i < str.length ; i++ ) {
                if( str[i] == null )continue;
                count++;
                if( count == 2 )stb.append("、");
                stb.append( str[i] );
                if( count % 2 == 0 ) {
                    svf.VrsOut( "CONDITION" + ( ++j ) ,   stb.toString() );
                    count = 0;
                    stb = new StringBuffer();
                }
            }
            if (0 < count) {
                svf.VrsOut( "CONDITION" + ( ++j ) ,   stb.toString() );
            }
        }

        int setSvfformFormat(final Vrw32alpWrap svf) {
            return svf.VrSetForm("KNJD232_5.frm", 1);
        }

        /**
         * 出欠種別(遅刻・早退・欠席・欠課)をソート順（頻度順）に並べたリストを戻します。<br>
         * 順番が入力されていない場合、またはゼロの場合は null を戻します。
         * @param paramap
         * @return
         */
        List createOrder (final Map paramap) {
            int[] number = {1, 2, 3, 4};

            boolean has = false;
            for (int i = 0; i < number.length; i++) {
                Integer order = (Integer) _param._order[i - 1];
                if (null != order && 0 < order.intValue()) {
                    has = true;
                    break;
                }
            }
            if (!has) { return null; }

            String[] kindString = {"LATE","EARLY","ABSENT","SUBCLASS_ABSENT"};
            List listOrder = new ArrayList();
            for (int i = 0; i < number.length; i++) {
                Integer order = _param._order[i -1];
                int order2 = (null == order || 0 == order.intValue()) ? 5 * number[i] : order.intValue();
                listOrder.add(new AttendHindOrder(new Integer(order2), new Integer(number[i]), kindString[i]));
            }
            Collections.sort(listOrder, new AttendHindOrder());
            return listOrder;
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     *  学校教育システム 賢者 [成績管理] 成績判定会議資料 ６．異動に関する情報
     */

    /*
     *  2005/08/08 Build
     *  2006/11/20 m-yama   NO001 フォーム変更に伴う修正。（異動種別のグループサプレス化に対応）
     *  2007/03/02 m-yama   NO002 出欠対象日付出力を追加した。
     */

    public class Transfer extends Base {
        boolean printSvf(
                final DB2UDB db2,
                final Vrw32alpWrap svf
        ) {
            if (0 != setSvfformFormat(svf)) {
                return false;
            }
            return printsvfMain(db2, svf);
        }

        /**
         *  印刷処理
         */
        private boolean printsvfMain(
                final DB2UDB db2,
                final Vrw32alpWrap svf
        ) {
            boolean hasData = false;
            printsvfOutHead(svf);
            try {
                PreparedStatement ps = db2.prepareStatement(prestatementRecordTransfer());
                ResultSet rs = ps.executeQuery();
                String transcd = null;
                int num = 0;
                while (rs.next()) {
                    if (null == transcd || !transcd.equals(rs.getString("TRANSFERCD"))) {
                        num++;
                        transcd = rs.getString("TRANSFERCD");
                    }
                    printsvfOutDetail(svf, rs, num);
                    svf.VrEndRecord();
                    if (!hasData) { hasData = true; }
                }
                db2.commit();
                rs.close();
                ps.close();
            } catch (SQLException e) {
                log.error("SQLException", e);
            } catch (Exception e) {
                log.error("Exception", e);
            }
            return hasData;
        }

        /**
         *  帳票明細行印刷
         */
        private void printsvfOutDetail(
                final Vrw32alpWrap svf,
                final ResultSet rs,
                final int num
        ) {
            try {
                svf.VrsOut( "TRANSFER",     rs.getString("TRANSFERNAME") );
                svf.VrsOut( "HR_ATTENDNO",  getHrName(_param, rs.getString("HR_CLASS"), rs.getString("ATTENDNO")));
                svf.VrsOut( "SCHREGNO",     rs.getString("SCHREGNO") );
                svf.VrsOut( "NAME",         rs.getString("NAME") );

                StringBuffer stb = new StringBuffer();
                stb.append(  KNJ_EditDate.h_format_thi( rs.getString("TRANSFER_SDATE"), 0 ) );
                if( rs.getString("TRANSFER_EDATE") != null )stb.append( " \uFF5E " + KNJ_EditDate.h_format_thi( rs.getString("TRANSFER_EDATE"), 0 ) );
                svf.VrsOut( "TRANSFER_DATE",  stb.toString() );

                svf.VrsOut("REASON", rs.getString("TRANSFERREASON"));

                StringBuffer stb2 = new StringBuffer();
                if( rs.getString("TRANSFERPLACE") != null )stb2.append( rs.getString("TRANSFERPLACE") );
                if( rs.getString("TRANSFERADDR") != null )stb2.append( rs.getString("TRANSFERADDR") );
                if( stb2 != null )svf.VrsOut( "ADDRESS",         stb2.toString() );

            } catch( Exception ex ) {
                log.error("error! ",ex);
            }
        }

        /**
         *  帳票ヘッダー等印刷
         */
        private void printsvfOutHead(
               final Vrw32alpWrap svf
        ) {
            svf.VrsOut( "NENDO",     _param._nendo);
            svf.VrsOut( "GRADE",     _param._printGrade );
            svf.VrsOut( "DATE",      _param._nowDateWa );
            svf.VrsOut( "DATE2",     _param._printTransTo);  //NO002
        }

        int setSvfformFormat(final Vrw32alpWrap svf) {
            return svf.VrSetForm("KNJD232_6.frm", 4);
        }

        /**
         * @param paramap
         * @return SQL文を戻します。
         */
        private String prestatementRecordTransfer() {
            StringBuffer stb = new StringBuffer();
            stb.append("WITH ");
            //異動データの表
            stb.append("TRANS_DATA AS(");
            //留学・休学等
            stb.append(     "SELECT  SCHREGNO ");
            stb.append(            ",CASE WHEN TRANSFERCD IN('1','2','3','4','5','6','7','8','9') THEN 1 + INT(TRANSFERCD) ELSE NULL END AS TRANSFERCD ");
            stb.append(            ",TRANSFER_SDATE,TRANSFER_EDATE ");
            stb.append(            ",TRANSFERPLACE,TRANSFERADDR,TRANSFERREASON ");
            stb.append(            ",NAME1 ");
            stb.append(     "FROM    SCHREG_TRANSFER_DAT T1 ");
            stb.append(     "LEFT JOIN NAME_MST T2 ON T1.TRANSFERCD = T2.NAMECD2 AND T2.NAMECD1 = 'A004' ");
            stb.append(     "WHERE   TRANSFER_SDATE BETWEEN '" + _param._transFrom + "' AND '" + _param._transTo + "' ");
            stb.append(          "OR TRANSFER_EDATE BETWEEN '" + _param._transFrom + "' AND '" + _param._transTo + "' ");
            stb.append(          "OR TRANSFER_SDATE <= '" + _param._transFrom + "' AND TRANSFER_EDATE >= '" + _param._transTo + "' ");
            //転学・退学
            stb.append(     "UNION ");
            stb.append(     "SELECT  SCHREGNO ");
            stb.append(            ",CASE GRD_DIV WHEN '2' THEN 21 WHEN '3' THEN 22 ELSE NULL END AS TRANSFERCD ");
            stb.append(            ",GRD_DATE AS TRANSFER_SDATE ");
            stb.append(            ",CASE WHEN SCHREGNO IS NOT NULL THEN NULL ELSE '' END AS TRANSFER_EDATE ");
            stb.append(            ",CASE WHEN SCHREGNO IS NOT NULL THEN NULL ELSE '' END AS TRANSFERPLACE ");
            stb.append(            ",CASE WHEN SCHREGNO IS NOT NULL THEN NULL ELSE '' END AS TRANSFERADDR ");
            stb.append(            ",GRD_REASON AS TRANSFERREASON ");
            stb.append(            ",NAME1 ");
            stb.append(     "FROM    SCHREG_BASE_MST T1 ");
            stb.append(     "LEFT JOIN NAME_MST T2 ON T1.GRD_DIV = T2.NAMECD2 AND T2.NAMECD1 = 'A003' ");
            stb.append(     "WHERE   GRD_DIV    IN  ('2','3') ");
            stb.append(         "AND GRD_DATE BETWEEN '" + _param._transFrom + "' AND '" + _param._transTo + "' ");
            //転入・編入
            stb.append(     "UNION ");
            stb.append(     "SELECT  SCHREGNO ");
            stb.append(            ",CASE ENT_DIV WHEN '4' THEN 1 WHEN '5' THEN 2 ELSE NULL END AS TRANSFERCD ");
            stb.append(            ",ENT_DATE AS TRANSFER_SDATE ");
            stb.append(            ",CASE WHEN SCHREGNO IS NOT NULL THEN NULL ELSE '' END AS TRANSFER_EDATE ");
            stb.append(            ",CASE WHEN SCHREGNO IS NOT NULL THEN NULL ELSE '' END AS TRANSFERPLACE ");
            stb.append(            ",CASE WHEN SCHREGNO IS NOT NULL THEN NULL ELSE '' END AS TRANSFERADDR ");
            stb.append(            ",ENT_REASON AS TRANSFERREASON ");
            stb.append(            ",NAME1 ");
            stb.append(     "FROM    SCHREG_BASE_MST T1 ");
            stb.append(     "LEFT JOIN NAME_MST T2 ON T1.ENT_DIV = T2.NAMECD2 AND T2.NAMECD1 = 'A002' ");
            stb.append(     "WHERE   ENT_DIV    IN  ('4','5') ");
            stb.append(         "AND ENT_DATE BETWEEN '" + _param._transFrom + "' AND '" + _param._transTo + "' ");
            stb.append(") ");

            //メイン表
            stb.append("SELECT  T3.SCHREGNO, T1.HR_CLASS, T1.ATTENDNO, T2.NAME ");
            stb.append(       ",T3.TRANSFERCD, T3.NAME1 AS TRANSFERNAME ");
            stb.append(       ",T3.TRANSFER_SDATE, T3.TRANSFER_EDATE ");
            stb.append(       ",T3.TRANSFERPLACE, T3.TRANSFERADDR, T3.TRANSFERREASON ");
            stb.append("FROM    TRANS_DATA T3 ");
            stb.append("INNER JOIN SCHREG_BASE_MST   T2 ON T2.SCHREGNO = T3.SCHREGNO ");
            stb.append("INNER JOIN (SELECT  SCHREGNO,HR_CLASS,ATTENDNO ");
            stb.append(            "FROM    SCHREG_REGD_DAT S1 ");
            stb.append(            "WHERE   S1.YEAR = '" + _param._year + "' ");
            stb.append(                "AND S1.GRADE = '" + _param._grade + "' ");
            stb.append(                "AND S1.SEMESTER =(SELECT  MAX(SEMESTER) ");
            stb.append(                                  "FROM    SCHREG_REGD_DAT S2 ");
            stb.append(                                  "WHERE   S2.YEAR = '" + _param._year + "' ");
            stb.append(                                      "AND S2.SCHREGNO = S1.SCHREGNO ");
            stb.append(                                  "GROUP BY SCHREGNO) ");
            stb.append(                "AND EXISTS(SELECT 'X' FROM TRANS_DATA S2 WHERE S1.SCHREGNO = S2.SCHREGNO) ");
            stb.append(      ")T1 ON T1.SCHREGNO = T3.SCHREGNO ");
            stb.append("ORDER BY T3.TRANSFERCD, T1.HR_CLASS,T1.ATTENDNO ");

            return stb.toString();
        }
    }

    /**
     * 出欠種別（遅刻・早退・欠席・欠課）をオーダー順に並び替えるクラス。
     * @author yamasiro
     * @version $Id: 5e21ed9ec8e430814296730f19433c44b5001949 $
     */
    private static class AttendHindOrder implements Comparator {
        Integer _order;
        Integer _number;
        String _kind;

        /**
         * コンストラクタ。
         */
        AttendHindOrder() {}

        /**
         * コンストラクタ。
         * @param order
         * @param number
         * @param kind
         */
        AttendHindOrder(
                Integer order,
                Integer number,
                String kind
        ) {
            _order = order;
            _number = number;
            _kind = kind;
        }

        /**
         * {@inheritDoc}
         */
        public int compare(Object o1, Object o2) {
            if (!(o1 instanceof AttendHindOrder)) return -1;
            if (!(o2 instanceof AttendHindOrder)) return -1;
            final AttendHindOrder g1 = (AttendHindOrder) o1;
            final AttendHindOrder g2 = (AttendHindOrder) o2;
            int rtn = g1._order.compareTo(g2._order);
            if (0 != rtn) return rtn;
            rtn = g1._number.compareTo(g2._number);
            return rtn;
        }
    }

    /**
     * 出欠データのクラス。
     */
    private static class AttendData {
        String _schregno;
        String _hrClass;
        String _attendno;
        String _name;
        Integer _leave;
        Integer _absent;
        Integer _late;
        Integer _early;
        Integer _subclassAbsentInt;
        Double _subclassAbsentDouble;
        Integer _subclassLateEarly;

        /**
         * コンストラクタ。
         */
        AttendData(ResultSet rs, boolean isNotInt) {
            try {
                _schregno = (String) rs.getObject("SCHREGNO");
                _hrClass = (String) rs.getObject("HR_CLASS");
                _attendno = (String) rs.getObject("ATTENDNO");
                _name = (String) rs.getObject("NAME");
                _leave = (Integer) rs.getObject("LEAVE");
                _absent = (Integer) rs.getObject("ABSENT");
                _late = (Integer) rs.getObject("LATE");
                _early = (Integer) rs.getObject("EARLY");

                if (isNotInt) {
                    _subclassAbsentDouble = (Double) rs.getObject ("SUBCLASS_ABSENT");
                } else {
                    _subclassAbsentInt = (Integer) rs.getObject ("SUBCLASS_ABSENT");
                }
                _subclassLateEarly = (Integer) rs.getObject("SUBCLASS_LATE_EARLY");
            } catch (SQLException e) {
                log.error("SQLException", e);
            }
        }

        /**
         * Integer型のオブジェクトを null の場合は 0 として戻します。
         * @param val
         * @return
         */
        Integer getintValue(final Integer val) {
            if (null == val) { return new Integer(0); }
            return val;
        }

        /**
         * Double型のオブジェクトを null の場合は 0 として戻します。
         * @param val
         * @return
         */
        Double getdoubleValue(final Double val) {
            if (null == val) { return new Double(0); }
            return val;
        }

        public String toString() {
            return _schregno;
        }
    }

    /**
     * 出欠データのリストを頻度順にソートするクラス。
     * @author yamasiro
     * @version $Id: 5e21ed9ec8e430814296730f19433c44b5001949 $
     */
    private static class SortOrderAttendCompare implements Comparator{
        List _order;

        /**
         * コンストラクタ。
         * @param order
         */
        SortOrderAttendCompare(final List order) {
            _order = order;
        }

        public int compare(Object o1, Object o2) {
            if (!(o1 instanceof AttendData)) return -1;
            if (!(o2 instanceof AttendData)) return -1;
            final AttendData g1 = (AttendData) o1;
            final AttendData g2 = (AttendData) o2;

            for (Iterator it = _order.iterator(); it.hasNext();) {
                AttendHindOrder order = (AttendHindOrder) it.next();
                if ("LATE".equals(order._kind)) {
                    int rtn = g1._late.compareTo(g2._late);
                    if (0 != rtn) { return -rtn; }
                }
                if ("EARLY".equals(order._kind)) {
                    int rtn = g1._early.compareTo(g2._early);
                    if (0 != rtn) { return -rtn; }
                }
                if ("ABSENT".equals(order._kind)) {
                    int rtn = g1._absent.compareTo(g2._absent);
                    if (0 != rtn) { return -rtn; }
                }
                if ("SUBCLASS_ABSENT".equals(order._kind)) {
                    if (null != g1._subclassAbsentInt || null != g2._subclassAbsentInt) {
                        int rtn = g1.getintValue(g1._subclassAbsentInt).compareTo(g2.getintValue(g2._subclassAbsentInt));
                        if (0 != rtn) { return -rtn; }
                    }
                    if (null != g1._subclassAbsentDouble || null != g2._subclassAbsentDouble) {
                        int rtn = g1.getdoubleValue(g1._subclassAbsentDouble).compareTo(g2.getdoubleValue(g2._subclassAbsentDouble));
                        if (0 != rtn) { return -rtn; }
                    }
                }
            }

            int rtn = g1._hrClass.compareTo(g2._hrClass);
            if (0 != rtn) {
                return rtn;
            }
            rtn = g1._attendno.compareTo(g2._attendno);
            return rtn;
        }
    }

    /**
     * 出欠データのリストを組・出席番号にソートするクラス。
     * @author yamasiro
     * @version $Id: 5e21ed9ec8e430814296730f19433c44b5001949 $
     */
    private static class SortOrderHrclassCompare implements Comparator {
        public int compare(Object o1, Object o2) {
            if (!(o1 instanceof AttendData)) return -1;
            if (!(o2 instanceof AttendData)) return -1;
            final AttendData g1 = (AttendData) o1;
            final AttendData g2 = (AttendData) o2;
            int rtn = g1._hrClass.compareTo(g2._hrClass);
            if (0 != rtn) {
                return rtn;
            }
            rtn = g1._attendno.compareTo(g2._attendno);
            return rtn;
        }
    }

    private static class Param {
        private String _semesterName;
        private List _attendList;
        private boolean _isAttendPerfectSubclassLateEarly;
        private String _transFrom;
        private String _attendToDate;
        private String _attendLastMonth;
        private String _attendFromDate;
        final String _gradeKinde;
        private Map _hrName;
        private String _transTo;
        private String _printTransTo;
        private String _date;
        private String _printDate;
        private String _nowDateWa;
        private Integer _subClassAbsent5;
        private Integer _absent5;
        private Integer _early5;
        private Integer _late5;
        private Integer _subClassAbsent4;
        private Integer _absent4;
        private Integer _early4;
        private Integer _late4;
        private Integer _unstudy2;
        private Integer _count2;
        private Integer _assess2;
        final Float _assess1;
        private Integer[] _order;
        final String _year;
        final String _grade;
        final String _printGrade;
        /** コンボで選択された値 */
        final String _gakki2;
        final String _gakki;
        final String _nendo;

        /** 成績優良者 */
        final String _output1;
        /** 成績不振者 */
        final String _output2;
        /** 皆勤者 */
        final String _output3;
        /** 出欠状況優良者 */
        final String _output4;
        /** 出欠状況不振者 */
        final String _output5;
        /** 異動者 */
        final String _output6;
        /** 成績不振者 詳細出力 */
        private String _gradePoorDetail;    // output7
        /** 成績優良者 詳細出力 */
        private String _gradeGoodDetail;    // output8

        /** 科目マスタ */
        private Map _subClasses;    // TODO: 優良者・不振者の明細でこのフィールドを使う予定

        /** １：年間、２：随時 */
        private String _absenceDiv = "1";

        final String _useCurriculumcd;
        final String _useVirus;
        final String _useKoudome;
        final String _useClassDetailDat;
        final String _kyoukaSougou1;
        final String _kyoukaSougou2;
        private KNJSchoolMst _knjSchoolMst;

        private KNJDefineSchool _definecode;       // 各学校における定数等設定
        private DecimalFormat _absentFmt;
        private DecimalFormat df = new DecimalFormat("0.0");

        Param(final DB2UDB db2, final HttpServletRequest request) {

            _year = request.getParameter("YEAR");
            _grade = request.getParameter("GRADE");
            String gradeCd = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT GRADE_CD FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' "));
			_printGrade = NumberUtils.isDigits(gradeCd) ? String.valueOf(Integer.parseInt(gradeCd)) : StringUtils.defaultString(gradeCd, " ");
					;
            
            _gakki2 = request.getParameter("GAKKI2");    // コンボで選択された値
            if (isGakunenMatu()) {
                _gakki = request.getParameter("GAKKI");  // 今学期
            } else {
                _gakki = _gakki2;
            }
            _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_year)) + "年度";

            // 成績優良者 評定平均がＸ以上
            String str = request.getParameter("ASSESS1");
            if (hasParameter(str,true)) {
                _assess1 = new Float(str);
            } else {
                _assess1 = new Float(0.0);
            }

            // 成績不振者 評定ＹがＸ科目以上
            str = request.getParameter("ASSESS2");
            if (hasParameter(str,true)) {
                _assess2 = new Integer(str);
            }
            str = request.getParameter("COUNT2");
            if (hasParameter(str,true)) {
                _count2 = new Integer(str);
            }

            // 成績不振者 未履修がＸ科目以上
            str = request.getParameter("UNSTUDY2");
            if (hasParameter(str,true)) {
                _unstudy2 = new Integer(str);
            }

            // 出欠状況優良者 遅刻がＸ回以下
            str = request.getParameter("LATE4");
            if (hasParameter(str,true)) {
                _late4 = new Integer(str);
            }
            // 出欠状況優良者 早退がＸ回以下
            str = request.getParameter("EARLY4");
            if (hasParameter(str,true)) {
                _early4 = new Integer(str);
            }
            // 出欠状況優良者 欠席がＸ回以下
            str = request.getParameter("ABSENT4");
            if (hasParameter(str,true)) {
                _absent4 = new Integer(str);
            }
            // 出欠状況優良者 欠課がＸ回以下
            str = request.getParameter("SUBCLASS_ABSENT4");
            if (hasParameter(str,true)) {
                _subClassAbsent4 = new Integer(str);
            }

            // 出欠状況不振者 遅刻がＸ回以下
            str = request.getParameter("LATE5");
            if (hasParameter(str,true)) {
                _late5 = new Integer(str);
            }

            // 出欠状況不振者 早退がＸ回以下
            str = request.getParameter("EARLY5");
            if (hasParameter(str,true)) {
                _early5 = new Integer(str);
            }

            // 出欠状況不振者 欠席がＸ回以下
            str = request.getParameter("ABSENT5");
            if (hasParameter(str,true)) {
                _absent5 = new Integer(str);
            }

            // 出欠状況不振者 欠課がＸ回以下
            str = request.getParameter("SUBCLASS_ABSENT5");
            if (hasParameter(str,true)) {
                _subClassAbsent5 = new Integer(str);
            }

            // 出欠状況不振者・頻度順
            _order = new Integer[5];
            for (int i = 1; i < 5; i++) {
                if (null != request.getParameter("ORDER" + i)) {
                    str = request.getParameter("ORDER" + i);
                    if (hasParameter(str,true)) {
                        _order[i - 1] = new Integer(str);
                    }
                }
            }

            // 出欠対象日付・異動対象日付
            str = request.getParameter("DATE");
            if (hasParameter(str, false)) {
                _date = str;
                _transTo = str;
                _printDate = KNJ_EditDate.h_format_JP(db2, _date);
                _printTransTo = _printDate;
            }
            _nowDateWa = KNJ_EditDate.getNowDateWa(db2, false);

            // 成績不振者 詳細出力
            final String output7 = request.getParameter("OUTPUT7");
            if (null != output7) {
                _gradePoorDetail = output7;
            }

            // 成績優良者 詳細出力
            final String output8 = request.getParameter("OUTPUT8");
            if (null != output8) {
                _gradeGoodDetail = output8;
            }

            _output1 = request.getParameter("OUTPUT1");
            _output2 = request.getParameter("OUTPUT2");
            _output3 = request.getParameter("OUTPUT3");
            _output4 = request.getParameter("OUTPUT4");
            _output5 = request.getParameter("OUTPUT5");
            _output6 = request.getParameter("OUTPUT6");

            // 対象となるRECORD_DATのFIELD
            if (isGakunenMatu()) {
                _gradeKinde = "GRAD_VALUE";
            } else {
                _gradeKinde = "SEM" + _gakki + "_VALUE";
            }

            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
            _useClassDetailDat = request.getParameter("useClassDetailDat");
            _kyoukaSougou1 = request.getParameter("KYOUKA_SOUGOU1");
            _kyoukaSougou2 = request.getParameter("KYOUKA_SOUGOU2");

            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (SQLException ex) {
                log.debug("KNJSchoolMst exception!", ex);
            }

            setHrName(db2);
            setParam3(db2);
            setParam4(db2);

            _definecode = new KNJDefineSchool();
            _definecode.defineCode(db2, _year);

            setAbsentFmt();
            setNameMstD050(db2);
            createAttendData(db2);
            load(db2);
        }

        /**
         * 表示用組名称マップを作成し、汎用マップparamapに追加します。
         * @param db2
         */
        private void setHrName(final DB2UDB db2) {
            final Map hmap = KNJ_Get_Info.getMapForHrclassName(db2);
            if (null != hmap){
                _hrName = hmap;
            } else {
                _hrName = new HashMap();
            }
        }

        private void createAttendData(final DB2UDB db2) {
            if (null == _output3 && null == _output4 && null == _output5) {
                return;
            }
            final AttendPerfect obj = new AttendPerfect();
            obj._param = this;
            obj.createAttendData(db2);
        }

        /**
         * 出欠関連テーブル読み込みのパラメータをMapに追加します。<br>
         * Mapのキーは
         *      "ATTEND_DATE"  :attend_semes_datの最終集計日の翌日をセット<br>
         *      "ATTEND_MONTH" :attend_semes_datの最終集計学期＋月をセット
         * @param db2
         */
        void setParam3(final DB2UDB db2) {
            final KNJDivideAttendDate obj = new KNJDivideAttendDate();
            obj.getDivideAttendDate(db2, _year, _gakki, _date);
            if (null != obj.date) {
                _attendFromDate = obj.date;
            }
            if (null != obj.month) {
                _attendLastMonth = obj.month;
            }
            _attendToDate = _date;
        }

        /**
         *  異動データ取得範囲日付パラメータをセット
         */
        void setParam4(final DB2UDB db2) {
//            if (null == _output6) {
//                return;
//            }

            final KNJ_Semester obj = new KNJ_Semester();          //学期情報取得のクラス
            KNJ_Semester.ReturnVal returnval = null;        //各情報を返すためのクラス

            // 期間開始日に年度開始日をセット
            returnval = obj.Semester(db2, _year, "9");
            if (null != returnval.val2) {
                _transFrom = returnval.val2;
            }

            // 期間終了日が渡されていない場合は学期終了日をセット
            if (null == _transTo) {
                returnval = obj.Semester(db2, _year, _gakki);
                if (null != returnval.val2) {
                    _transTo = returnval.val3;
                }
            }
        }

        /**
         * 欠課数印字のフォーマットを設定します。
         */
        void setAbsentFmt() {
            switch (_definecode.absent_cov) {
            case 0:
                _absentFmt = new DecimalFormat("0");
                break;
            case 1:
                _absentFmt = new DecimalFormat("0");
                break;
            case 2:
                _absentFmt = new DecimalFormat("0");
                break;
            case 3:
                if (isGakunenMatu()) {
                    _absentFmt = new DecimalFormat("0");
                } else {
                    _absentFmt = new DecimalFormat("0.0");
                }
                break;
            case 4:
                _absentFmt = new DecimalFormat("0.0");
                break;
            default:
                _absentFmt = new DecimalFormat("0");
            }
        }

        /**
         * @param str
         * @return String strに値があればTrueを戻します。
         */
        private boolean hasParameter(
                final String str,
                boolean isCheckDigit
        ) {
            if (null == str || 0 == str.length()) { return false; }
            if (!isCheckDigit) {
                return true;
            }

            char schar[] = str.toCharArray();
            boolean hasDigit = true;
            for (int i = 0; i < schar.length; i++) {
                if (!Character.isDigit(schar[i]) && schar[i] != '.') {
                    hasDigit = false;
                    break;
                }
            }

            try {
                double i = Double.parseDouble(str);
                if (0 == i) {
                    i = 0;
                }
            } catch (NumberFormatException e) {
                hasDigit = false;
                log.error("＊＊＊　この値は数字になっていません。value=[" + str +"]");
            }

            return hasDigit;
        }

        public boolean isGakunenMatu() {
            return "9".equals(_gakki2);
        }

        public void load(final DB2UDB db2) {
            try {
                _subClasses = setSubClasses(db2);
            } catch (final SQLException e) {
                log.error("科目の取り込みに失敗", e);
                return;
            }

            try {
                setCombinedOnSubClass(db2);
            } catch (final SQLException e) {
                log.error("合併情報の取り込みに失敗", e);
                return;
            }

            setSemesterName(db2);
        }

        private void setSemesterName(final DB2UDB db2) {
            ResultSet rs = null;
            try {
                db2.query("SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _gakki2 + "' ");
                rs = db2.getResultSet();
                if (rs.next()) {
                    _semesterName = rs.getString("SEMESTERNAME");
                }
            } catch (final Exception e) {
                log.warn("学期名称の取得失敗", e);
            } finally{
                db2.commit();
                DbUtils.closeQuietly(null, null, rs);
            }
        }

        /**
         * 「皆勤者」の判定基準
         * NAMESPARE1・・・Y:皆勤者かどうかを判断する際に「授業の遅刻・早退」もチェックする
         */
        private void setNameMstD050(final DB2UDB db2) {
            _isAttendPerfectSubclassLateEarly = false;
            ResultSet rs = null;
            try {
                db2.query("SELECT NAMESPARE1,NAMESPARE2 FROM NAME_MST WHERE NAMECD1 = 'D050' AND NAMECD2 = '01'");
                rs = db2.getResultSet();
                if (rs.next()) {
                    _isAttendPerfectSubclassLateEarly = "Y".equals(rs.getString("NAMESPARE1"));
                }
            } catch (final Exception e) {
                log.warn("「皆勤者」の判定基準の取得失敗", e);
            } finally{
                db2.commit();
                DbUtils.closeQuietly(null, null, rs);
            }
        }

        private Map setSubClasses(final DB2UDB db2) throws SQLException {
            final Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sqlSubClasses());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String code = rs.getString("SUBCLASSCD");
                    final String name = rs.getString("SUBCLASSNAME");
                    final String abbv = rs.getString("SUBCLASSABBV");
                    rtn.put(code, new SubClass(code, name, abbv));
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.debug("科目マスタ総数=" + rtn.size());
            return rtn;
        }

        public String sqlSubClasses() {
            String sql = "select";
                if ("1".equals(_useCurriculumcd)) {
                    sql +="    CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ";
                }
                sql +="    SUBCLASSCD AS SUBCLASSCD,"
                    + "    SUBCLASSNAME,"
                    + "    SUBCLASSABBV"
                    + "  from V_SUBCLASS_MST"
                    + "  where"
                    + "    YEAR = '" + _year + "'"
                    + "  order by";
                if ("1".equals(_useCurriculumcd)) {
                    sql +="    CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ";
                }
                sql += "    SUBCLASSCD";
                ;
            return sql;
        }

        private void setCombinedOnSubClass(final DB2UDB db2) throws SQLException {
            final Set sakiSet = new HashSet();
            final Set motoSet = new HashSet();

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sqlCombined());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String combined = rs.getString("COMBINED_SUBCLASSCD");
                    final String attend = rs.getString("ATTEND_SUBCLASSCD");
                    sakiSet.add(combined);
                    motoSet.add(attend);
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            // 合併先
            for (final Iterator it = sakiSet.iterator(); it.hasNext();) {
                final String saki = (String) it.next();
                final SubClass subClass = (SubClass) _subClasses.get(saki);
                if (null != subClass) {
                    subClass.setSaki();
                }
            }
            // 合併元
            for (final Iterator it = motoSet.iterator(); it.hasNext();) {
                final String moto = (String) it.next();
                final SubClass subClass = (SubClass) _subClasses.get(moto);
                if (null != subClass) {
                    subClass.setMoto();
                }
            }
        }

        public String sqlCombined() {
            String sql = "select distinct";
                  if ("1".equals(_useCurriculumcd)) {
                      sql += "  COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || ";
                  }
                  sql += "  COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD,";
                  if ("1".equals(_useCurriculumcd)) {
                      sql += "  ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ";
                  }
                  sql += "  ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD ";
                  sql +=  " from SUBCLASS_REPLACE_COMBINED_DAT"
                    + " where"
                    + "  YEAR = '" + _year + "'"
                ;
            return sql;
        }

        /**
         * 除外する合併科目のSQL。
         * @return SQL文
         */
        public String sqlJyogaiGappei() {
            final String field;
            if ("1".equals(_useCurriculumcd)) {
                field = isGakunenMatu() ? "ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD" : " COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD";
            } else {
                field = isGakunenMatu() ? "ATTEND_SUBCLASSCD" : "COMBINED_SUBCLASSCD";
            }
            final String sql = ",GAPPEI AS(SELECT DISTINCT " + field+ " AS SUBCLASSCD FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR='" + _year + "')";
            return sql;
        }
    }

    private static class SubClass implements Comparable {
        private final String _code;
        private final String _name;
        private final String _abbv;

        /** 合併情報を持っているか */
        private boolean _hasCombined;
        /** 合併先か? */
        private boolean _isSaki;
        /** 合併元か? */
        private boolean _isMoto;

        public SubClass(final String code, final String name, final String abbv) {
            _code = code;
            _name = name;
            _abbv = abbv;
        }

        public void setSaki() {
            _hasCombined = true;
            _isSaki = true;
        }

        public void setMoto() {
            _hasCombined = true;
            _isMoto = true;
        }

        public String toString() {
            return _code + ":" + _abbv;
        }

        public int compareTo(final Object o) {
            if (!(o instanceof SubClass)) {
                return -1;
            }
            final SubClass that = (SubClass) o;
            return this._code.compareTo(that._code);
        }
    }
}
