// kanji=漢字
/*
 * $Id: 5ae3ea8ecfda3a8675ddee874d991b699d0f73fc $
 *
 * 作成日: 2006/05/08 21:10:29 - JST
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJD;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJD.detail.KNJ_Testname;
import servletpack.KNJZ.detail.KNJDefineCode;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 学校教育システム 賢者 [成績管理] 成績一覧 (単位制　東京都版)
 *  2006/05/02 yamashiro 東京都版を複写して作成
 *  @version $Id: 5ae3ea8ecfda3a8675ddee874d991b699d0f73fc $
 */
public class KNJD061T {

    /** 0 : パラメータ学年/学期成績 */
    private static final String PARAM_GRAD_SEM_KIND = "0";
    /** 9900 : 学年/学期成績 */
    private static final String GRAD_SEM_KIND = "9900";

    private static final Log log = LogFactory.getLog(KNJD061T.class);

    private static final int MAX_COLUMN = 19;
    private static final int MAX_LINE = 25;

    private static final String SUBJECT_U = "89";  // 教科コード
    private static final String SUBJECT_T = "90";  // 総合的な学習の時間

    private static final String FORM_FILE = "KNJD061_2.frm";
    private static final String FROM_TO_MARK = "\uFF5E";
    private static final DecimalFormat TOTAL_DEC_FMT = new DecimalFormat("0.0");

    private Common _common;  //成績別処理のクラス
    private Manager _manager;

    private Param _param;
    private Form _form;

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {

        _form = new Form();
        try {
            _form.svfInit(response, request);
        } catch (final IOException e) {
            log.fatal("SVFの初期化に失敗!");
            return;
        }

        boolean hasData = false;
        DB2UDB db2 = null;
        try {
            db2 = openDb(request.getParameter("DBNAME"));

            _param = createParam(request, db2);

            _common = createCommon(db2);
            _manager = new Manager(_common);

            _form.printHeader();
            if (_common instanceof CommonGrade) {
                _form.printFooterMark();
            }

            for (int i = 0; i < _param._hrClasses.length; i++) {
                final HRInfo hrInfo = new HRInfo(_manager, _param._hrClasses[i]);
                hrInfo.loadHRClassStaff(db2);
                hrInfo.loadStudents(db2);
                hrInfo.loadScoreDetail(db2);
                if (_common instanceof CommonGrade) {
                    hrInfo.loadRecordDat(db2);
                }

                hrInfo._ranking = hrInfo.createRanking();
                log.debug("RANK:" + hrInfo._ranking);

                if (_common instanceof CommonGrade) {
                    hrInfo.setGradeAttendCheckMark();
                }

                boolean hasDataWrk = false;
                _form.setHead1(hrInfo._staffName, hrInfo._hrName);

                int line = 0;
                for (final Iterator it = hrInfo._students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    final int used = student.print(line);
                    _form.VrEndRecord();
                    line += used;
                    if (0 < used) {
                        hasDataWrk = true;
                    }
                }

                if (hasDataWrk) {
                    if (0 != line % MAX_LINE) {
                        // ページの途中で、年組が終わった場合。
                        final int n = MAX_LINE - (line % MAX_LINE);

                        // ページ末までの、残り行数分を、改行(つまり、改ページ)
                        for (int i1 = 0; i1 < n; i1++) {
                            _form.VrEndRecord();
                        }
                    }
                    hasData = true;
                }
            }
        } catch (final Exception e) {
            log.error("メイン処理でエラー", e);
        } finally {
            closeDb(db2);
            _form.closeSvf(hasData);
        }
    }

    private DB2UDB openDb(final String dbName)
            throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException
    {
        DB2UDB db2;
        db2 = new DB2UDB(dbName, "db2inst1", "db2inst1", DB2UDB.TYPE2);
        db2.open();
        return db2;
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    private Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        log.fatal("$Revision: 56595 $ $Date"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);

        final Param param = new Param(request, db2);
        return param;
    }

    /*
     * 科目別平均の表
     */
    private String sqlSubclassAverage(final String fieldChaircd, final String fieldName) {
        final String rtn;

        rtn = " SELECT "
            + ("1".equals(_param._useCurriculumcd) ? " CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD " : " SUBCLASSCD ")
            + "        ," + fieldChaircd + " AS CHAIRCD"
            + "        ,ROUND(AVG(FLOAT(" + fieldName + "))*10,0)/10 AS AVG_SCORE"
            + " FROM RECORD_DAT W1"
            + " WHERE YEAR = '" + _param._year + "'"
            + " GROUP BY "
            + ("1".equals(_param._useCurriculumcd) ? " CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD " : " SUBCLASSCD ")
            + "," + fieldChaircd
            + " HAVING " + fieldChaircd + " IS NOT NULL"
            ;

        return rtn;
    }

    /**
     *  欠課・遅刻・早退データの表
     */
    private String sqlAttendSubclassInfo(final String hrClass) {

        String semesInState = (String) _param._hasuuMap.get("attendSemesInState");
        String befDayFrom   = (String) _param._hasuuMap.get("befDayFrom");
        String befDayTo     = (String) _param._hasuuMap.get("befDayTo");
        String aftDayFrom   = (String) _param._hasuuMap.get("aftDayFrom");
        String aftDayTo     = (String) _param._hasuuMap.get("aftDayTo");

        final StringBuffer stb = new StringBuffer();

        stb.append("WITH ");

        //対象生徒の表 クラスの生徒
        stb.append("SCHNO_A AS(");
        stb.append(     "SELECT  W1.SCHREGNO,W1.SEMESTER ");
        stb.append(            ",W1.GRADE,W1.COURSECD, W1.MAJORCD, W1.COURSECODE ");
        stb.append(     "FROM    SCHREG_REGD_DAT W1 ");
        if (_param.semesterGakunenMatu()) {
            stb.append(     "WHERE   W1.YEAR = '" + _param._year + "' ");
            stb.append(     "AND W1.SEMESTER = '" + _param._loginSemes + "' ");
        } else {
            stb.append(     "INNER   JOIN SEMESTER_MST W2 ON W2.YEAR = '" + _param._year + "' AND W2.SEMESTER = '" + _param._semester + "' ");
            stb.append(     "WHERE   W1.YEAR = '" + _param._year + "' ");
            stb.append(     "AND W1.SEMESTER = '" + _param._semester + "' ");
            stb.append(     "AND NOT EXISTS(SELECT 'X'  FROM  SCHREG_BASE_MST S1 ");
            stb.append(                     "WHERE S1.SCHREGNO = W1.SCHREGNO AND S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < W2.SDATE) ");
        }
        stb.append(         "AND W1.SCHREGNO = ? ");
        stb.append(     ") ");

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

        //対象講座の表
        stb.append(",CHAIR_A AS(");
        stb.append(     "SELECT W1.SCHREGNO, W2.CHAIRCD, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(        "W2.CLASSCD || '-' || W2.SCHOOL_KIND || '-' || W2.CURRICULUM_CD || '-' || ");
        }
        stb.append(            "W2.SUBCLASSCD AS SUBCLASSCD, W2.SEMESTER, W1.APPDATE, W1.APPENDDATE ");
        stb.append(     "FROM   CHAIR_STD_DAT W1, CHAIR_DAT W2 ");
        stb.append(     "WHERE  W1.YEAR = '" + _param._year + "' ");
        stb.append(        "AND W2.YEAR = '" + _param._year + "' ");
        stb.append(        "AND W1.SEMESTER = W2.SEMESTER ");
        stb.append(        "AND W1.SEMESTER <= '" + _param._semester + "' ");
        stb.append(        "AND W2.SEMESTER <= '" + _param._semester + "' ");
        stb.append(        "AND W1.CHAIRCD = W2.CHAIRCD ");
        if (!_param._isAttendPerfectSubclass90over) {
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(        "AND (W2.CLASSCD <= '" + SUBJECT_U + "' OR W2.CLASSCD = '" + SUBJECT_T + "') ");
            } else {
                stb.append(        "AND (SUBSTR(W2.SUBCLASSCD,1,2) <= '" + SUBJECT_U + "' OR SUBSTR(W2.SUBCLASSCD,1,2) = '" + SUBJECT_T + "') ");
            }
        }
        stb.append(        "AND EXISTS(SELECT 'X' FROM SCHNO_A W3 WHERE W3.SCHREGNO = W1.SCHREGNO)");
        stb.append(     ")");

        if (befDayFrom != null || aftDayFrom != null) {
            // 時間割(休学・留学を含む)
            stb.append(", SCHEDULE_SCHREG_R AS( ");
            stb.append(    "SELECT T2.SCHREGNO, T2.SUBCLASSCD, T1.SEMESTER, EXECUTEDATE, PERIODCD ");
            stb.append(    "FROM   SCH_CHR_DAT T1, CHAIR_A T2 ");
            stb.append(    "WHERE  T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ");
            if (befDayFrom != null && aftDayFrom != null) {
                stb.append("    AND (T1.EXECUTEDATE BETWEEN '" + befDayFrom + "' AND '" + befDayTo + "' ");
                stb.append("      OR T1.EXECUTEDATE BETWEEN '" + aftDayFrom + "' AND '" + aftDayTo + "') ");
            } else if (befDayFrom != null) {
                stb.append("    AND  T1.EXECUTEDATE BETWEEN '" + befDayFrom + "' AND '" + befDayTo + "' ");
            } else if (aftDayFrom != null) {
                stb.append("    AND  T1.EXECUTEDATE BETWEEN '" + aftDayFrom + "' AND '" + aftDayTo + "' ");
            }
            stb.append(        "AND T1.YEAR = '" + _param._year + "' ");
            stb.append(        "AND T1.SEMESTER <= '" + _param._semester + "' ");
            stb.append(        "AND T1.SEMESTER = T2.SEMESTER ");
            stb.append(        "AND T1.CHAIRCD = T2.CHAIRCD ");
            stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST T4 ");
            stb.append(                        "WHERE   T4.SCHREGNO = T2.SCHREGNO ");
            stb.append(                         "AND (( ENT_DIV IN('4','5') AND EXECUTEDATE < ENT_DATE ) ");
            stb.append(                           "OR ( GRD_DIV IN('2','3') AND EXECUTEDATE > GRD_DATE )) ) ");
            stb.append("    AND NOT EXISTS(SELECT ");
            stb.append("                       'X' ");
            stb.append("                   FROM ");
            stb.append("                       ATTEND_DAT T4 ");
            stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = T4.YEAR AND L1.DI_CD = T4.DI_CD ");
            stb.append("                   WHERE ");
            stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
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
            stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
            stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
            stb.append("                       AND L1.REP_DI_CD = '28' ");
            stb.append("                  ) ");
            if (_param._definecode.useschchrcountflg) {
                stb.append(                "AND NOT EXISTS(SELECT  'X' FROM SCH_CHR_COUNTFLG T4 ");
                stb.append(                               "WHERE   T4.EXECUTEDATE = T1.EXECUTEDATE AND ");
                stb.append(                                       "T4.PERIODCD = T1.PERIODCD AND ");
                stb.append(                                       "T4.CHAIRCD = T1.CHAIRCD AND ");
                stb.append(                                       "T1.DATADIV IN ('0', '1') AND ");
                stb.append(                                       "T4.GRADE||T4.HR_CLASS = '" + hrClass + "' AND ");
                stb.append(                                       "T4.COUNTFLG = '0') ");
                stb.append(                "AND NOT EXISTS(SELECT  'X' FROM TEST_COUNTFLG TEST ");
                stb.append(                               "WHERE ");
                stb.append(                                       "TEST.EXECUTEDATE  = T1.EXECUTEDATE ");
                stb.append(                                       "AND TEST.PERIODCD = T1.PERIODCD ");
                stb.append(                                       "AND TEST.CHAIRCD  = T1.CHAIRCD ");
                stb.append(                                       "AND TEST.DATADIV  = T1.DATADIV) ");
            }
            if (_param._hasSchChrDatExecutediv) {
                stb.append("        AND VALUE(T1.EXECUTEDIV, '0') <> '2' "); // 休講は対象外
            }
            stb.append("GROUP BY T2.SCHREGNO, T2.SUBCLASSCD, T1.SEMESTER, EXECUTEDATE, PERIODCD ");
            stb.append(")");

            // 時間割(休学・留学を含まない)
            stb.append(", SCHEDULE_SCHREG AS( ");
            stb.append(    "SELECT T1.SCHREGNO, SUBCLASSCD, T1.SEMESTER, EXECUTEDATE, PERIODCD ");
            stb.append(    "FROM SCHEDULE_SCHREG_R T1 ");
            stb.append(    "WHERE NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT T4 ");
            stb.append(                       "WHERE   T4.SCHREGNO = T1.SCHREGNO ");
            stb.append(                         "AND TRANSFERCD IN('1','2') AND EXECUTEDATE BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ) ");
            stb.append("  GROUP BY T1.SCHREGNO, SUBCLASSCD, T1.SEMESTER, EXECUTEDATE, PERIODCD ");
            stb.append(")");

            // 休学時数
            stb.append(", OFFDAYS_SCHREG AS( ");
            stb.append(    "SELECT T1.SCHREGNO, SUBCLASSCD, T1.SEMESTER, COUNT(*) AS OFFDAYS ");
            stb.append(    "FROM SCHEDULE_SCHREG_R T1 ");
            stb.append(    "WHERE EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT T4 ");
            stb.append(                       "WHERE   T4.SCHREGNO = T1.SCHREGNO ");
            stb.append(                         "AND TRANSFERCD = '2' AND EXECUTEDATE BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ) ");
            stb.append("  GROUP BY T1.SCHREGNO, SUBCLASSCD, T1.SEMESTER ");
            stb.append(")");
        }

        //欠課数の表
        stb.append(",ATTEND_A AS(");
        //出欠データより集計
        if (befDayFrom != null || aftDayFrom != null) {
            stb.append(          "SELECT S1.SCHREGNO, SUBCLASSCD, SEMESTER,");
            stb.append(                 "COUNT(*) AS JISU, ");
            stb.append(                 "SUM(CASE WHEN L1.REP_DI_CD IN('4','5','6','14','11','12','13'");
            if ("1".equals(_param._knjSchoolMst._subAbsent)) {
                stb.append(                                   ",'1','8'");
            }
            if ("1".equals(_param._knjSchoolMst._subSuspend)) {
                stb.append(                                   ",'2','9'");
            }
            if ("1".equals(_param._knjSchoolMst._subMourning)) {
                stb.append(                                   ",'3','10'");
            }
            if ("true".equals(_param._useVirus)) {
                if ("1".equals(_param._knjSchoolMst._subVirus)) {
                    stb.append(                                   ",'19','20'");
                }
            }
            if ("true".equals(_param._useKoudome)) {
                if ("1".equals(_param._knjSchoolMst._subKoudome)) {
                    stb.append(                                   ",'25','26'");
                }
            }
            stb.append(                                        ") THEN 1 ELSE 0 END)AS ABSENT1, ");
            stb.append(                 "SUM(CASE WHEN L1.REP_DI_CD IN('15','16','23','24') THEN SMALLINT(VALUE(L1.MULTIPLY, '1')) ELSE 0 END)AS LATE_EARLY ");
            stb.append(          "FROM SCHEDULE_SCHREG S1 "); // 休学時数、留学時数を含まない
            stb.append(               "LEFT JOIN ATTEND_DAT S2 ON S2.YEAR = '" + _param._year + "' AND ");
            stb.append(                                      "S2.ATTENDDATE = S1.EXECUTEDATE AND ");
            stb.append(                                      "S2.PERIODCD = S1.PERIODCD AND ");
            stb.append(                                      "S1.SCHREGNO = S2.SCHREGNO ");
            stb.append(               "LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = S2.YEAR AND L1.DI_CD = S2.DI_CD ");
            stb.append(          "GROUP BY S1.SCHREGNO, SUBCLASSCD, SEMESTER ");
            stb.append(      "UNION ALL ");
            stb.append(      "SELECT  T2.SCHREGNO, T2.SUBCLASSCD, T2.SEMESTER ");
            if ("1".equals(_param._knjSchoolMst._subOffDays)) {
                stb.append(           ",SUM(T2.OFFDAYS) AS JISU ");   // 授業時数に休学時数を減算する
                stb.append(           ",SUM(T2.OFFDAYS) AS ABSENT1 "); // 欠課時数に休学時数を加算する
            } else {
                stb.append(           ",SUM(0) AS JISU ");
                stb.append(           ",SUM(0) AS ABSENT1 ");
            }
            stb.append(           ",SUM(0) AS LATE_EARLY ");
            stb.append(      "FROM OFFDAYS_SCHREG T2 ");
            stb.append(      "GROUP BY T2.SCHREGNO, T2.SUBCLASSCD, T2.SEMESTER ");
        }
        //UNION ALL
        if ((befDayFrom != null || aftDayFrom != null) && _param._semesFlg) {
            stb.append(          "UNION ALL ");
        }
        //月別科目別出欠集計データより欠課を取得
        if (_param._semesFlg) {
            stb.append(          "SELECT  W1.SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(        "W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
            }
            stb.append(                  "W1.SUBCLASSCD AS SUBCLASSCD, W1.SEMESTER, ");
            stb.append(                  "SUM(VALUE(LESSON,0) ");
            if (!"1".equals(_param._knjSchoolMst._subOffDays)) {
                stb.append(                  " - VALUE(OFFDAYS,0) ");
            }
            stb.append(                  " - VALUE(ABROAD,0) ) AS JISU, ");
            stb.append(                  "SUM(VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) + VALUE(NURSEOFF,0) ");
            if ("1".equals(_param._knjSchoolMst._subAbsent))  {
                stb.append(                   "+ VALUE(ABSENT,0) ");
            }
            if ("1".equals(_param._knjSchoolMst._subSuspend)) {
                stb.append(                   "+ VALUE(SUSPEND,0) ");
            }
            if ("1".equals(_param._knjSchoolMst._subMourning)) {
                stb.append(                   "+ VALUE(MOURNING,0) ");
            }
            if ("1".equals(_param._knjSchoolMst._subOffDays)) {
                stb.append(                   "+ VALUE(OFFDAYS,0) ");
            }
            if ("true".equals(_param._useVirus)) {
                if ("1".equals(_param._knjSchoolMst._subVirus)) {
                    stb.append(                   "+ VALUE(VIRUS,0) ");
                }
            }
            if ("true".equals(_param._useKoudome)) {
                if ("1".equals(_param._knjSchoolMst._subKoudome)) {
                    stb.append(                   "+ VALUE(KOUDOME,0) ");
                }
            }
            stb.append(                    "   ) AS ABSENT1, ");
            stb.append(                  "SUM(VALUE(LATE,0) + VALUE(EARLY,0)) AS LATE_EARLY ");
            stb.append(          "FROM    ATTEND_SUBCLASS_DAT W1, SCHNO_A W2 ");
            stb.append(          "WHERE   W1.YEAR = '" + _param._year + "' AND ");
            stb.append(                  "W1.SEMESTER <= '" + _param._semester + "' AND ");
            stb.append(                  "W1.SEMESTER || W1.MONTH IN " + semesInState + " AND ");
            stb.append(                  "W1.SCHREGNO = W2.SCHREGNO ");
            stb.append(          "GROUP BY W1.SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(        "W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
            }
            stb.append(                   "W1.SUBCLASSCD, W1.SEMESTER ");
        }

        stb.append(     ") ");

        //欠課数の表
        stb.append(",ATTEND_B AS(");
        stb.append(         "SELECT  SCHREGNO, SUBCLASSCD ");

        if (_param._definecode.absent_cov == 1 || _param._definecode.absent_cov == 3) {
            //遅刻・早退を学期で欠課換算する
            stb.append(           ", SUM(ABSENT1)AS ABSENT1 ");
            stb.append(     "      , SUM(JISU)AS JISU ");
            stb.append(           ", SUM(LATE_EARLY)AS LATE_EARLY ");
            stb.append(     "FROM   (SELECT  SCHREGNO, SUBCLASSCD, SUM(JISU)AS JISU, ");
            if (_param._definecode.absent_cov == 1 || _param.semesterGakunenMatu()) {
                stb.append(                 "VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / " + _param._definecode.absent_cov_late + " AS ABSENT1 ");
            } else {
                stb.append(                 "FLOAT(DECIMAL(VALUE(SUM(ABSENT1),0) + VALUE(SUM(FLOAT(LATE_EARLY)),0) / " + _param._definecode.absent_cov_late + ",5,1)) AS ABSENT1 ");
            }
            stb.append(                   ", SUM(LATE_EARLY)AS LATE_EARLY ");
            stb.append(             "FROM    ATTEND_A ");
            stb.append(             "GROUP BY SCHREGNO, SUBCLASSCD, SEMESTER ");
            stb.append(             ")W1 ");
            stb.append(     "GROUP BY SCHREGNO, SUBCLASSCD ");
        } else if (_param._definecode.absent_cov == 2 || _param._definecode.absent_cov == 4) {
            //遅刻・早退を年間で欠課換算する
            if (_param._definecode.absent_cov == 2) {
                stb.append(       ", VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / " + _param._definecode.absent_cov_late + " AS ABSENT1 ");
            } else {
                stb.append(       ", FLOAT(DECIMAL(VALUE(SUM(ABSENT1),0) + VALUE(SUM(FLOAT(LATE_EARLY)),0) / " + _param._definecode.absent_cov_late + ",5,1)) AS ABSENT1 ");
            }
            stb.append(     "      , SUM(JISU)AS JISU ");
            stb.append(           ", SUM(LATE_EARLY)AS LATE_EARLY ");
            stb.append(     "FROM    ATTEND_A ");
            stb.append(     "GROUP BY SCHREGNO, SUBCLASSCD ");
        } else {
            //遅刻・早退を欠課換算しない
            stb.append(     "      , SUM(ABSENT1)AS ABSENT1 ");
            stb.append(           ", SUM(JISU)AS JISU ");
            stb.append(           ", SUM(LATE_EARLY)AS LATE_EARLY ");
            stb.append(     "FROM    ATTEND_A W1 ");
            stb.append(     "GROUP BY SCHREGNO, SUBCLASSCD ");
        }
        stb.append(     ") ");

        //メイン表
        stb.append(" SELECT  T1.SCHREGNO ");
        stb.append(        ",FLOAT(SUM(T5.ABSENT1)) AS ABSENT1 ");
        stb.append(        ",SUM(T5.JISU) AS JISU ");
        stb.append(        ",SUM(T5.LATE_EARLY) AS LATE_EARLY ");
        //対象生徒・講座の表
        stb.append("FROM(");
        stb.append(     "SELECT  W1.SCHREGNO,W2.SUBCLASSCD ");
        stb.append(     "FROM    CHAIR_STD_DAT W1, CHAIR_A W2, SCHNO_A W3 ");
        stb.append(     "WHERE   W1.YEAR = '" + _param._year + "' AND ");
        stb.append(             "W1.CHAIRCD = W2.CHAIRCD AND ");
        stb.append(             "W1.SEMESTER = W2.SEMESTER AND ");
        stb.append(             "W1.SCHREGNO = W3.SCHREGNO ");
        if (!_param.semesterGakunenMatu()) {
            stb.append(     "AND W2.SEMESTER = '" + _param._semester + "' ");
        }
        stb.append(     "GROUP BY W1.SCHREGNO,W2.SUBCLASSCD ");
        stb.append(")T1 ");
        //欠課数の表
        stb.append(  "LEFT JOIN(");
        stb.append(         "SELECT  SCHREGNO, SUBCLASSCD, ABSENT1, JISU, LATE_EARLY ");
        stb.append(         "FROM    ATTEND_B W1 ");
        stb.append(  ")T5 ON T5.SUBCLASSCD = T1.SUBCLASSCD AND T5.SCHREGNO = T1.SCHREGNO ");
        stb.append("GROUP BY T1.SCHREGNO");

        return stb.toString();
    }

    /**
     *  PrepareStatement作成 --> 成績・評定・欠課データの表
     *  2005/06/20 Modify ペナルティ欠課の算出式を修正
     */
    private static String sqlStdSubclassDetail(final Param _param, final Common _common, final KNJSchoolMst knjSchoolMst) {

        String semesInState = (String) _param._hasuuMap.get("attendSemesInState");
        String befDayFrom   = (String) _param._hasuuMap.get("befDayFrom");
        String befDayTo     = (String) _param._hasuuMap.get("befDayTo");
        String aftDayFrom   = (String) _param._hasuuMap.get("aftDayFrom");
        String aftDayTo     = (String) _param._hasuuMap.get("aftDayTo");

        final StringBuffer stb = new StringBuffer();

        stb.append("WITH ");

        //対象生徒の表 クラスの生徒
        stb.append("SCHNO_A AS(");
        stb.append(     "SELECT  W1.SCHREGNO,W1.SEMESTER ");
        stb.append(            ",W1.GRADE,W1.COURSECD, W1.MAJORCD, W1.COURSECODE ");  //NO010
        stb.append(     "FROM    SCHREG_REGD_DAT W1 ");
        if (_param.semesterGakunenMatu()) {
            stb.append(     "WHERE   W1.YEAR = '" + _param._year + "' ");
            stb.append(     "AND W1.SEMESTER = '" + _param._loginSemes + "' ");
        } else {
            stb.append(     "INNER   JOIN SEMESTER_MST W2 ON W2.YEAR = '" + _param._year + "' AND W2.SEMESTER = '" + _param._semester + "' ");
            stb.append(     "WHERE   W1.YEAR = '" + _param._year + "' ");
            stb.append(     "AND W1.SEMESTER = '" + _param._semester + "' ");
            stb.append(     "AND NOT EXISTS(SELECT 'X'  FROM  SCHREG_BASE_MST S1");
            stb.append(     "     WHERE S1.SCHREGNO = W1.SCHREGNO AND S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < W2.SDATE) ");
        }
        stb.append(         "AND W1.SCHREGNO = ? ");
        stb.append(     ") ");

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

        //対象講座の表
        stb.append(",CHAIR_A AS(");
        stb.append(     "SELECT W1.SCHREGNO, W2.CHAIRCD, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(           " W2.CLASSCD || '-' || W2.SCHOOL_KIND || '-' || W2.CURRICULUM_CD || '-' || ");
        }
        stb.append(           " W2.SUBCLASSCD AS SUBCLASSCD, W2.SEMESTER, W1.APPDATE, W1.APPENDDATE ");
        stb.append(     "FROM   CHAIR_STD_DAT W1, CHAIR_DAT W2 ");
        stb.append(     "WHERE  W1.YEAR = '" + _param._year + "' ");
        stb.append(        "AND W2.YEAR = '" + _param._year + "' ");
        stb.append(        "AND W1.SEMESTER = W2.SEMESTER ");

        stb.append(    "AND W1.SEMESTER <= '" + _param._semester + "' ");
        stb.append(    "AND W2.SEMESTER <= '" + _param._semester + "' ");

        stb.append(        "AND W1.CHAIRCD = W2.CHAIRCD ");
        stb.append(        "AND (SUBSTR(W2.SUBCLASSCD,1,2) <= '" + SUBJECT_U + "' OR SUBSTR(W2.SUBCLASSCD,1,2) = '" + SUBJECT_T + "') ");
        stb.append(        "AND EXISTS(SELECT 'X' FROM SCHNO_A W3 WHERE W3.SCHREGNO = W1.SCHREGNO)");
        stb.append(     ")");

        if (befDayFrom != null || aftDayFrom != null) {
            // 時間割(休学・留学を含む)
            stb.append(", SCHEDULE_SCHREG_R AS( ");
            stb.append(    "SELECT T2.SCHREGNO, SUBCLASSCD, T1.SEMESTER, EXECUTEDATE, PERIODCD ");
            stb.append(    "FROM   SCH_CHR_DAT T1, CHAIR_A T2 ");
            stb.append(    "WHERE  T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ");
            if (befDayFrom != null && aftDayFrom != null) {
                stb.append("    AND (T1.EXECUTEDATE BETWEEN '" + befDayFrom + "' AND '" + befDayTo + "' ");
                stb.append("      OR T1.EXECUTEDATE BETWEEN '" + aftDayFrom + "' AND '" + aftDayTo + "') ");
            } else if (befDayFrom != null) {
                stb.append("    AND  T1.EXECUTEDATE BETWEEN '" + befDayFrom + "' AND '" + befDayTo + "' ");
            } else if (aftDayFrom != null) {
                stb.append("    AND  T1.EXECUTEDATE BETWEEN '" + aftDayFrom + "' AND '" + aftDayTo + "' ");
            }
            stb.append(        "AND T1.YEAR = '" + _param._year + "' ");
            stb.append(        "AND T1.SEMESTER <= '" + _param._semester + "' ");
            stb.append(        "AND T1.SEMESTER = T2.SEMESTER ");
            stb.append(        "AND T1.CHAIRCD = T2.CHAIRCD ");
            stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST T4 ");  //NO025
            stb.append(                        "WHERE   T4.SCHREGNO = T2.SCHREGNO ");  //NO025
            stb.append(                         "AND (( ENT_DIV IN('4','5') AND EXECUTEDATE < ENT_DATE ) ");  //NO025
            stb.append(                           "OR ( GRD_DIV IN('2','3') AND EXECUTEDATE > GRD_DATE )) ) ");  //NO025
            stb.append("    AND NOT EXISTS(SELECT ");
            stb.append("                       'X' ");
            stb.append("                   FROM ");
            stb.append("                       ATTEND_DAT T4 ");
            stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = T4.YEAR AND L1.DI_CD = T4.DI_CD ");
            stb.append("                   WHERE ");
            stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
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
            stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
            stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
            stb.append("                       AND L1.REP_DI_CD = '28' ");
            stb.append("                  ) ");
            if (_param._definecode.useschchrcountflg) {
                stb.append(                "AND NOT EXISTS(SELECT  'X' FROM SCH_CHR_COUNTFLG T4 ");
                stb.append(                               "WHERE   T4.EXECUTEDATE = T1.EXECUTEDATE AND ");
                stb.append(                                       "T4.PERIODCD = T1.PERIODCD AND ");
                stb.append(                                       "T4.CHAIRCD = T1.CHAIRCD AND ");
                stb.append(                                       "T1.DATADIV IN ('0', '1') AND ");
                stb.append(                                       "T4.GRADE||T4.HR_CLASS = ? AND ");
                stb.append(                                       "T4.COUNTFLG = '0') ");
                stb.append(                "AND NOT EXISTS(SELECT  'X' FROM TEST_COUNTFLG TEST ");
                stb.append(                               "WHERE ");
                stb.append(                                       "TEST.EXECUTEDATE  = T1.EXECUTEDATE ");
                stb.append(                                       "AND TEST.PERIODCD = T1.PERIODCD ");
                stb.append(                                       "AND TEST.CHAIRCD  = T1.CHAIRCD ");
                stb.append(                                       "AND TEST.DATADIV  = T1.DATADIV) ");
            }
            stb.append("GROUP BY T2.SCHREGNO, SUBCLASSCD, T1.SEMESTER, EXECUTEDATE, PERIODCD ");
            stb.append(")");

            // 時間割(休学・留学を含まない)
            stb.append(", SCHEDULE_SCHREG AS( ");
            stb.append(    "SELECT T1.SCHREGNO, SUBCLASSCD, T1.SEMESTER, EXECUTEDATE, PERIODCD ");
            stb.append(    "FROM SCHEDULE_SCHREG_R T1 ");
            stb.append(    "WHERE NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT T4 ");
            stb.append(                       "WHERE   T4.SCHREGNO = T1.SCHREGNO ");
            stb.append(                         "AND TRANSFERCD IN('1','2') AND EXECUTEDATE BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ) ");
            stb.append("  GROUP BY T1.SCHREGNO, SUBCLASSCD, T1.SEMESTER, EXECUTEDATE, PERIODCD ");
            stb.append(")");

            // 休学時数
            stb.append(", OFFDAYS_SCHREG AS( ");
            stb.append(    "SELECT T1.SCHREGNO, SUBCLASSCD, T1.SEMESTER, COUNT(*) AS OFFDAYS ");
            stb.append(    "FROM SCHEDULE_SCHREG_R T1 ");
            stb.append(    "WHERE EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT T4 ");
            stb.append(                       "WHERE   T4.SCHREGNO = T1.SCHREGNO ");
            stb.append(                         "AND TRANSFERCD = '2' AND EXECUTEDATE BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ) ");
            stb.append("  GROUP BY T1.SCHREGNO, SUBCLASSCD, T1.SEMESTER ");
            stb.append(")");
        }

        //欠課数の表
        stb.append(",ATTEND_A AS(");
        //出欠データより集計
        if (befDayFrom != null || aftDayFrom != null) {
            stb.append(          "SELECT S1.SCHREGNO, SUBCLASSCD, SEMESTER,");
            stb.append(                 "COUNT(*) AS JISU, ");
            stb.append(                 "SUM(CASE WHEN L1.REP_DI_CD IN('4','5','6','14','11','12','13'");
            if ("1".equals(_param._knjSchoolMst._subAbsent)) {
                stb.append(                                   ",'1','8'");
            }
            if ("1".equals(_param._knjSchoolMst._subSuspend)) {
                stb.append(                                   ",'2','9'");
            }
            if ("1".equals(_param._knjSchoolMst._subMourning)) {
                stb.append(                                   ",'3','10'");
            }
            if ("true".equals(_param._useVirus)) {
                if ("1".equals(_param._knjSchoolMst._subVirus)) {
                    stb.append(                                   ",'19','20'");
                }
            }
            if ("true".equals(_param._useKoudome)) {
                if ("1".equals(_param._knjSchoolMst._subKoudome)) {
                    stb.append(                                   ",'25','26'");
                }
            }
            stb.append(                                        ") THEN 1 ELSE 0 END)AS ABSENT1, ");
            stb.append(                 "SUM(CASE WHEN L1.REP_DI_CD IN('15','16','23','24') THEN SMALLINT(VALUE(L1.MULTIPLY, '1')) ELSE 0 END)AS LATE_EARLY ");
            stb.append(          "FROM SCHEDULE_SCHREG S1 "); // 休学時数、留学時数を含まない
            stb.append(               "LEFT JOIN ATTEND_DAT S2 ON S2.YEAR = '" + _param._year + "' AND ");
            stb.append(                                      "S2.ATTENDDATE = S1.EXECUTEDATE AND ");
            stb.append(                                      "S2.PERIODCD = S1.PERIODCD AND ");
            stb.append(                                      "S1.SCHREGNO = S2.SCHREGNO ");
            stb.append(               "LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = S2.YEAR AND L1.DI_CD = S2.DI_CD ");
            stb.append(          "GROUP BY S1.SCHREGNO, SUBCLASSCD, SEMESTER ");
            stb.append(      "UNION ALL ");
            stb.append(      "SELECT  T2.SCHREGNO, T2.SUBCLASSCD, T2.SEMESTER ");
            if ("1".equals(_param._knjSchoolMst._subOffDays)) {
                stb.append(           ",SUM(T2.OFFDAYS) AS JISU ");   // 授業時数に休学時数を減算する
                stb.append(           ",SUM(T2.OFFDAYS) AS ABSENT1 "); // 欠課時数に休学時数を加算する
            } else {
                stb.append(           ",SUM(0) AS JISU ");
                stb.append(           ",SUM(0) AS ABSENT1 ");
            }
            stb.append(           ",SUM(0) AS LATE_EARLY ");
            stb.append(      "FROM OFFDAYS_SCHREG T2 ");
            stb.append(      "GROUP BY T2.SCHREGNO, T2.SUBCLASSCD, T2.SEMESTER ");
        }
        //UNION ALL
        if ((befDayFrom != null || aftDayFrom != null) && _param._semesFlg) {
            stb.append(          "UNION ALL ");
        }
        //月別科目別出欠集計データより欠課を取得
        if (_param._semesFlg) {
            stb.append(          "SELECT  W1.SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(           " W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
            }
            stb.append(                  "W1.SUBCLASSCD AS SUBCLASSCD, W1.SEMESTER, ");
            stb.append(                  "SUM(VALUE(LESSON,0) ");
            if (!"1".equals(_param._knjSchoolMst._subOffDays)) {
                stb.append(                  " - VALUE(OFFDAYS,0) ");
            }
            stb.append(                  " - VALUE(ABROAD,0) ) AS JISU, ");
            stb.append(                  "SUM(VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) + VALUE(NURSEOFF,0) ");
            if ("1".equals(_param._knjSchoolMst._subAbsent))  {
                stb.append(                   "+ VALUE(ABSENT,0) ");
            }
            if ("1".equals(_param._knjSchoolMst._subSuspend)) {
                stb.append(                   "+ VALUE(SUSPEND,0) ");
            }
            if ("1".equals(_param._knjSchoolMst._subMourning)) {
                stb.append(                   "+ VALUE(MOURNING,0) ");
            }
            if ("1".equals(_param._knjSchoolMst._subOffDays)) {
                stb.append(                   "+ VALUE(OFFDAYS,0) ");
            }
            if ("true".equals(_param._useVirus)) {
                if ("1".equals(_param._knjSchoolMst._subVirus)) {
                    stb.append(                   "+ VALUE(VIRUS,0) ");
                }
            }
            if ("true".equals(_param._useKoudome)) {
                if ("1".equals(_param._knjSchoolMst._subKoudome)) {
                    stb.append(                   "+ VALUE(KOUDOME,0) ");
                }
            }
            stb.append(                    "   ) AS ABSENT1, ");
            stb.append(                  "SUM(VALUE(LATE,0) + VALUE(EARLY,0)) AS LATE_EARLY ");
            stb.append(          "FROM    ATTEND_SUBCLASS_DAT W1, SCHNO_A W2 ");
            stb.append(          "WHERE   W1.YEAR = '" + _param._year + "' AND ");
            stb.append(                  "W1.SEMESTER <= '" + _param._semester + "' AND ");
            stb.append(                  "W1.SEMESTER || W1.MONTH IN " + semesInState + " AND ");
            stb.append(                  "W1.SCHREGNO = W2.SCHREGNO ");
            stb.append(          "GROUP BY W1.SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(           " W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
            }
            stb.append(                   "W1.SUBCLASSCD, W1.SEMESTER ");
        }

        stb.append(     ") ");

        //欠課数の表
        stb.append(",ATTEND_B AS(");
        stb.append(         "SELECT  SCHREGNO, SUBCLASSCD ");

        if (_param._definecode.absent_cov == 1 || _param._definecode.absent_cov == 3) {
            //遅刻・早退を学期で欠課換算する
            stb.append(           ", SUM(ABSENT1)AS ABSENT1 ");
            stb.append(     "      , SUM(JISU)AS JISU ");
            stb.append(     "FROM   (SELECT  SCHREGNO, SUBCLASSCD, SUM(JISU)AS JISU, ");
            if (_param._definecode.absent_cov == 1 || _param.semesterGakunenMatu()) {
                stb.append(                 "VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / " + _param._definecode.absent_cov_late + " AS ABSENT1 ");   //05/06/20Modify
            } else {
                stb.append(                 "FLOAT(DECIMAL(VALUE(SUM(ABSENT1),0) + VALUE(SUM(FLOAT(LATE_EARLY)),0) / " + _param._definecode.absent_cov_late + ",5,1)) AS ABSENT1 ");   //05/06/20Modify
            }
            stb.append(             "FROM    ATTEND_A ");
            stb.append(             "GROUP BY SCHREGNO, SUBCLASSCD, SEMESTER ");
            stb.append(             ")W1 ");
            stb.append(     "GROUP BY SCHREGNO, SUBCLASSCD ");
        } else if (_param._definecode.absent_cov == 2 || _param._definecode.absent_cov == 4) {
            //遅刻・早退を年間で欠課換算する
            if (_param._definecode.absent_cov == 2) {
                stb.append(       ", VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / " + _param._definecode.absent_cov_late + " AS ABSENT1 ");   //05/06/20Modify
            } else {
                stb.append(       ", FLOAT(DECIMAL(VALUE(SUM(ABSENT1),0) + VALUE(SUM(FLOAT(LATE_EARLY)),0) / " + _param._definecode.absent_cov_late + ",5,1)) AS ABSENT1 ");   //05/06/20Modify
            }
            stb.append(     "      , SUM(JISU)AS JISU ");
            stb.append(     "FROM    ATTEND_A ");
            stb.append(     "GROUP BY SCHREGNO, SUBCLASSCD ");
        } else {
            //遅刻・早退を欠課換算しない
            stb.append(     "      , SUM(ABSENT1)AS ABSENT1 ");
            stb.append(           ", SUM(JISU)AS JISU ");
            stb.append(     "FROM    ATTEND_A W1 ");
            stb.append(     "GROUP BY SCHREGNO, SUBCLASSCD ");
        }
        stb.append(     ") ");

        //NO010
        if (knjSchoolMst.isHoutei()) {
            stb.append(",CREDITS_A AS(");
        } else {
            stb.append(",CREDITS_A_SUB AS(");
        }
        stb.append(    "SELECT  SCHREGNO, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(           " CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
        }
        stb.append(            "SUBCLASSCD AS SUBCLASSCD, CREDITS, COMP_UNCONDITION_FLG, ABSENCE_HIGH ");
        stb.append(    "FROM    CREDIT_MST T1, SCHNO_A T2 ");
        stb.append(    "WHERE   T1.YEAR = '" + _param._year + "' ");
        stb.append(        "AND T1.GRADE = T2.GRADE ");
        stb.append(        "AND T1.COURSECD = T2.COURSECD ");
        stb.append(        "AND T1.MAJORCD = T2.MAJORCD ");
        stb.append(        "AND T1.COURSECODE = T2.COURSECODE ");
        if (knjSchoolMst.isJitu()) {
            stb.append(") ");
            stb.append(",CREDITS_A AS(");
            stb.append("    SELECT ");
            stb.append("        T1.SCHREGNO, ");
            stb.append("        T1.SUBCLASSCD, ");
            stb.append("        T1.CREDITS, ");
            stb.append("        T1.COMP_UNCONDITION_FLG, ");
            stb.append("        L1.COMP_ABSENCE_HIGH AS ABSENCE_HIGH ");
            stb.append("    FROM ");
            stb.append("        CREDITS_A_SUB T1 ");
            stb.append("        LEFT JOIN SCHREG_ABSENCE_HIGH_DAT L1 ON L1.YEAR = '" + _param._year + "' ");
            stb.append("             AND L1.DIV = '" + _param._absenceDiv + "' ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("             AND T1.SUBCLASSCD = L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || L1.SUBCLASSCD");
            } else {
                stb.append("             AND T1.SUBCLASSCD = L1.SUBCLASSCD ");
            }
            stb.append("             AND T1.SUBCLASSCD = L1.SUBCLASSCD ");
            stb.append("             AND T1.SCHREGNO = L1.SCHREGNO ");
        }
        stb.append(") ");

        //成績データの表（通常科目）
        stb.append(",RECORD_REC AS(");
        stb.append(    "SELECT  W3.SCHREGNO, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(           " W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
        }
        stb.append(            "W3.SUBCLASSCD AS SUBCLASSCD");
        stb.append("," + _common._fieldChaircd + " AS CHAIRCD");
        if (!_param._testKindCd.equals(GRAD_SEM_KIND)) {
            //中間・期末成績  NO024 Modify
            // fieldname:SEM?_XXXX_SCORE / fieldname2:SEM?_XXXX
            stb.append(       ",CASE WHEN " + _common._fieldName + " IS NOT NULL THEN RTRIM(CHAR(" + _common._fieldName + ")) ");
            stb.append(             "ELSE NULL END AS SCORE ");
            stb.append(       ",CASE WHEN " + _common._fieldNameValue + " IS NOT NULL THEN RTRIM(CHAR(" + _common._fieldNameValue + ")) ");
            stb.append(             "ELSE NULL END AS VALUE ");
            stb.append(       ",CASE WHEN " + _common._fieldName2 + "_VALUE IS NOT NULL THEN RTRIM(CHAR(" + _common._fieldName2 + "_VALUE)) ");
            stb.append(             "WHEN " + _common._fieldName2 + "_VALUE_DI IS NOT NULL THEN " + _common._fieldName2 + "_VALUE_DI ");
            stb.append(             "ELSE NULL END AS PATTERN_ASSESS ");
            stb.append(        ",W3.COMP_CREDIT, W3.GET_CREDIT ");  //NO010
        } else {
            // 学年成績/学期成績
            stb.append(       ",'' AS SCORE ");
            stb.append(       ",'' AS VALUE ");
            stb.append(       ",CASE WHEN " + _common._fieldName + " IS NOT NULL THEN RTRIM(CHAR(" + _common._fieldName + ")) ");
            stb.append(             "WHEN " + _common._fieldName + "_DI IS NOT NULL THEN " + _common._fieldName + "_DI ");
            stb.append(             "ELSE NULL END AS PATTERN_ASSESS ");
            stb.append(        ",W3.COMP_CREDIT, W3.GET_CREDIT ");  //NO010
        }
        stb.append(    "FROM    RECORD_DAT W3 ");
        stb.append(    "WHERE   W3.YEAR = '" + _param._year + "' AND ");
        stb.append(            "EXISTS(SELECT  'X' FROM SCHNO_A W1 WHERE W3.SCHREGNO = W1.SCHREGNO) ");
        stb.append(     ") ");

        //NO010
        stb.append(",CREDITS_UNCONDITION AS(");
        stb.append(    "SELECT  SCHREGNO, SUBCLASSCD, CREDITS ");
        stb.append(    "FROM    CREDITS_A T1 ");
        stb.append(    "WHERE   VALUE(T1.COMP_UNCONDITION_FLG,'0') = '1' ");
        stb.append(        "AND NOT EXISTS(SELECT 'X' FROM RECORD_REC T3 WHERE T3.SUBCLASSCD = T1.SUBCLASSCD AND T3.SCHREGNO = T1.SCHREGNO) ");
        stb.append(") ");

        //評価読替前科目の表 NO008 Build
        stb.append(",REPLACE_REC_MOTO AS(");
        stb.append(        "SELECT  ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(                 " ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD AS SUBCLASSCD ");
        } else {
            stb.append(                 "ATTEND_SUBCLASSCD AS SUBCLASSCD ");
        }
        stb.append(        "FROM    SUBCLASS_REPLACE_COMBINED_DAT T1 ");
        stb.append(        "WHERE   YEAR ='" + _param._year + "' AND REPLACECD = '1' ");
        stb.append(            "AND EXISTS(SELECT 'X' FROM CHAIR_A T2 WHERE ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(                 " T2.SUBCLASSCD = T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || T1.ATTEND_SUBCLASSCD ");
        } else {
            stb.append(                 " T2.SUBCLASSCD = T1.ATTEND_SUBCLASSCD ");  //NO0017
        }

        if (!_param.semesterGakunenMatu()) {
            stb.append("AND T2.SEMESTER = '" + _param._semester + "' ");
        }
        stb.append(")");
        stb.append(        "GROUP BY ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(                 " T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' ||  ");
        }
        stb.append(                  "ATTEND_SUBCLASSCD ");
        stb.append(     ") ");
        //評価読替後科目の表 NO008 Build
        stb.append(",REPLACE_REC_SAKI AS(");
        stb.append(        "SELECT  ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(                 " COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD AS SUBCLASSCD ");
        } else {
            stb.append(                 "COMBINED_SUBCLASSCD AS SUBCLASSCD ");
        }
        stb.append(        "FROM    SUBCLASS_REPLACE_COMBINED_DAT T1 ");
        stb.append(        "WHERE   YEAR ='" + _param._year + "' AND REPLACECD = '1' ");
        stb.append(            "AND EXISTS(SELECT 'X' FROM CHAIR_A T2 WHERE ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(                 " T2.SUBCLASSCD = T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || T1.ATTEND_SUBCLASSCD ");
        } else {
            stb.append(                 " T2.SUBCLASSCD = T1.ATTEND_SUBCLASSCD ");  //NO0017
        }

        if (!_param.semesterGakunenMatu()) {
            stb.append("AND T2.SEMESTER = '" + _param._semester + "' ");
        }
        stb.append(")");
        stb.append(        "GROUP BY ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(                 " T1.COMBINED_CLASSCD || '-' || T1.COMBINED_SCHOOL_KIND || '-' || T1.COMBINED_CURRICULUM_CD || '-' ||  ");
        }
        stb.append(                  "COMBINED_SUBCLASSCD ");
        stb.append(     ") ");

        //評定読替え科目評定の表
        if (_param.semesterGakunenMatu()) {
            stb.append(",REPLACE_REC AS(");
            stb.append(     "SELECT SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(            "W2.COMBINED_CLASSCD || '-' || W2.COMBINED_SCHOOL_KIND || '-' || W2.COMBINED_CURRICULUM_CD || '-' || W2.COMBINED_SUBCLASSCD AS SUBCLASSCD, ");
                stb.append(            "W2.ATTEND_CLASSCD || '-' || W2.ATTEND_SCHOOL_KIND || '-' || W2.ATTEND_CURRICULUM_CD || '-' || W2.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD, ");
            } else {
                stb.append(            "W2.COMBINED_SUBCLASSCD AS SUBCLASSCD, ");
                stb.append(            "W2.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD, ");
            }
            stb.append(            "SCORE, ");
            stb.append(            "PATTERN_ASSESS ");
            stb.append(            ",COMP_CREDIT,GET_CREDIT ");  //NO010
            stb.append(     "FROM   RECORD_REC W1, SUBCLASS_REPLACE_COMBINED_DAT W2 ");
            stb.append(     "WHERE  ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(                 " W1.SUBCLASSCD = W2.ATTEND_CLASSCD || '-' || W2.ATTEND_SCHOOL_KIND || '-' || W2.ATTEND_CURRICULUM_CD || '-' || W2.ATTEND_SUBCLASSCD AND ");
            } else {
                stb.append(                 " W1.SUBCLASSCD = W2.ATTEND_SUBCLASSCD AND ");
            }
            stb.append(            "W2.YEAR='" + _param._year + "' AND W2.REPLACECD='1' ");  //05/05/22
            stb.append(     ") ");

            stb.append(",REPLACE_REC_ATTEND AS(");
            stb.append(     "SELECT SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(            "W2.COMBINED_CLASSCD || '-' || W2.COMBINED_SCHOOL_KIND || '-' || W2.COMBINED_CURRICULUM_CD || '-' || W2.COMBINED_SUBCLASSCD AS SUBCLASSCD, ");
                stb.append(            "W2.ATTEND_CLASSCD || '-' || W2.ATTEND_SCHOOL_KIND || '-' || W2.ATTEND_CURRICULUM_CD || '-' || W2.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD ");
            } else {
                stb.append(            "W2.COMBINED_SUBCLASSCD AS SUBCLASSCD, ");
                stb.append(            "W2.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD ");
            }
            stb.append(     "FROM   RECORD_DAT W1, SUBCLASS_REPLACE_COMBINED_DAT W2 ");
            stb.append(     "WHERE  W1.YEAR = '" + _param._year + "' AND ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(            " W1.SUBCLASSCD = W2.ATTEND_SUBCLASSCD AND ");
                stb.append(            " W1.CLASSCD = W2.ATTEND_CLASSCD AND ");
                stb.append(            " W1.SCHOOL_KIND = W2.ATTEND_SCHOOL_KIND AND ");
                stb.append(            " W1.CURRICULUM_CD = W2.ATTEND_CURRICULUM_CD AND ");
            } else {
                stb.append(            " W1.SUBCLASSCD = W2.ATTEND_SUBCLASSCD AND ");
            }
            stb.append(            "EXISTS(SELECT 'X' FROM SCHNO_A W3 WHERE W3.SCHREGNO = W1.SCHREGNO) AND ");
            stb.append(            "W2.YEAR='" + _param._year + "' AND W2.REPLACECD='1' ");
            stb.append(     ") ");
         }

        //メイン表
        stb.append(" SELECT  T1.SUBCLASSCD,T1.SCHREGNO ");
        stb.append(        ",FLOAT(T5.ABSENT1) AS ABSENT1 ");
        stb.append(        ",T5.JISU ");
        stb.append(        ",T3.SCORE ");
        stb.append(        ",T3.VALUE ");
                                //教科コード'90'も同様に評定をそのまま出力 NO015
        stb.append(        ",T3.PATTERN_ASSESS ");
        stb.append(        ",REPLACEMOTO ");
        stb.append(        ",CASE WHEN VALUE(T6.COMP_UNCONDITION_FLG,'0') = '1' AND T3.COMP_CREDIT IS NULL THEN T6.CREDITS ELSE T3.COMP_CREDIT END AS COMP_CREDIT ");  //NO0015  NO0018
        stb.append(        ",CASE WHEN VALUE(T6.COMP_UNCONDITION_FLG,'0') = '1' AND T3.GET_CREDIT IS NULL THEN T6.CREDITS ELSE T3.GET_CREDIT END AS GET_CREDIT ");  //NO0015  NO0018
        stb.append(        ",VALUE(T6.COMP_UNCONDITION_FLG,'0')AS COMP_UNCONDITION_FLG ");  //NO015
        stb.append(        ",T3.COMP_CREDIT AS ON_RECORD_COMP "); //NO0015 NO0018
        stb.append(        ",T7.SUBCLASSABBV AS SUBCLASSNAME ");
        stb.append(        ",T6.ABSENCE_HIGH ");
        stb.append(        ",T6.CREDITS ");
        stb.append(        ",T3.CHAIRCD ");

        //対象生徒・講座の表
        stb.append("FROM(");
        stb.append(     "SELECT  W1.SCHREGNO,W2.SUBCLASSCD ");
        stb.append(     "FROM    CHAIR_STD_DAT W1, CHAIR_A W2, SCHNO_A W3 ");
        stb.append(     "WHERE   W1.YEAR = '" + _param._year + "' AND ");
        stb.append(             "W1.CHAIRCD = W2.CHAIRCD AND ");
        stb.append(             "W1.SEMESTER = W2.SEMESTER AND ");
        stb.append(             "W1.SCHREGNO = W3.SCHREGNO ");

        if (!_param.semesterGakunenMatu()) {
            stb.append(     "AND W2.SEMESTER = '" + _param._semester + "' ");
        }

        stb.append(     "GROUP BY W1.SCHREGNO,W2.SUBCLASSCD ");

        if (_param.semesterGakunenMatu()) {
            stb.append( "UNION   SELECT SCHREGNO,SUBCLASSCD ");
            stb.append( "FROM    REPLACE_REC_ATTEND ");
            stb.append( "GROUP BY SCHREGNO,SUBCLASSCD ");
        }
        //NO010
        stb.append(     "UNION ");
        stb.append(     "SELECT  SCHREGNO, SUBCLASSCD ");
        stb.append(     "FROM    CREDITS_UNCONDITION S1 ");

        stb.append(")T1 ");

        //成績の表
        stb.append(  "LEFT JOIN(");
        //成績の表（通常科目）
        if (_param.semesterGakunenMatu()) {
            stb.append(     "SELECT W3.SCHREGNO, W3.SUBCLASSCD, W3.SCORE, W3.VALUE, W3.PATTERN_ASSESS, ");  //NO010
            stb.append(            "(SELECT  COUNT(*) ");
            stb.append(             "FROM    REPLACE_REC S1 ");
            stb.append(             "WHERE   S1.SCHREGNO = W3.SCHREGNO AND ");
            stb.append(                     "S1.ATTEND_SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append(             "GROUP BY ATTEND_SUBCLASSCD) AS REPLACEMOTO ");
            stb.append(             ",COMP_CREDIT,GET_CREDIT ");  //NO010
            stb.append(             ",CHAIRCD ");
            stb.append(     "FROM   RECORD_REC W3 ");
            stb.append(     "WHERE  NOT EXISTS( SELECT 'X' FROM REPLACE_REC S2 WHERE W3.SUBCLASSCD = S2.SUBCLASSCD) ");
        } else {
            stb.append(     "SELECT W3.SCHREGNO, W3.SUBCLASSCD, W3.SCORE, W3.VALUE, W3.PATTERN_ASSESS, ");  //NO010
            stb.append(            "0 AS REPLACEMOTO ");
            stb.append(           ",COMP_CREDIT,GET_CREDIT ");  //NO010
            stb.append(           ",CHAIRCD ");
            stb.append(     "FROM   RECORD_REC W3 ");
        }
        if (_param.semesterGakunenMatu()) {
                            //評定読替科目 成績の表 NO006
            stb.append(     "UNION ALL ");
            stb.append(     "SELECT  W3.SCHREGNO, W3.SUBCLASSCD, ");
            stb.append(             "W3.SCORE AS SCORE, ");
            stb.append(             "W3.VALUE AS VALUE, ");
            stb.append(             "W3.PATTERN_ASSESS, ");
            stb.append(             "-1 AS REPLACEMOTO ");
            stb.append(             ",COMP_CREDIT,GET_CREDIT ");  //NO010
            stb.append(             ",CASE WHEN VALUE(CHAIRCD,'') = '' THEN NULL ELSE CHAIRCD END AS CHAIRCD ");
            stb.append(     "FROM   RECORD_REC W3 ");
            stb.append(     "WHERE  EXISTS( SELECT 'X' FROM REPLACE_REC S2 WHERE W3.SUBCLASSCD = S2.SUBCLASSCD) ");
        }
        stb.append(     ")T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD AND T3.SCHREGNO = T1.SCHREGNO ");

        //欠課数の表
        stb.append(  "LEFT JOIN(");
        stb.append(         "SELECT  SCHREGNO, SUBCLASSCD, ABSENT1, JISU ");
        stb.append(         "FROM    ATTEND_B W1 ");

        if (_param.semesterGakunenMatu()) {
                            //評定読替科目 欠課数の表
            stb.append(     "UNION ");
            stb.append(     "SELECT  W1.SCHREGNO,W1.SUBCLASSCD,SUM(ABSENT1)AS ABSENT1, SUM(JISU)AS JISU ");
            stb.append(     "FROM    REPLACE_REC_ATTEND W1, ATTEND_B W2 ");
            stb.append(     "WHERE   W1.SCHREGNO = W2.SCHREGNO AND W1.ATTEND_SUBCLASSCD = W2.SUBCLASSCD ");
            stb.append(         "AND NOT EXISTS(SELECT 'X' FROM ATTEND_B W3 WHERE W3.SUBCLASSCD = W1.SUBCLASSCD) ");  //NO0017
            stb.append(     "GROUP BY W1.SCHREGNO,W1.SUBCLASSCD ");
        }
        stb.append(  ")T5 ON T5.SUBCLASSCD = T1.SUBCLASSCD AND T5.SCHREGNO = T1.SCHREGNO ");

        stb.append("LEFT JOIN CREDITS_A T6 ON T6.SCHREGNO = T1.SCHREGNO AND T6.SUBCLASSCD = T1.SUBCLASSCD ");  //NO010

        stb.append("LEFT JOIN SUBCLASS_MST T7 ON ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(           " T7.CLASSCD || '-' || T7.SCHOOL_KIND || '-' || T7.CURRICULUM_CD || '-' || ");
        }
        stb.append(                       "T7.SUBCLASSCD = T1.SUBCLASSCD ");

        // 合併先科目を印刷しない
        if (!_param._isPrintSakiKamoku) {
            stb.append("WHERE T1.SUBCLASSCD NOT IN(");
            stb.append(     "SELECT ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(     " W2.COMBINED_CLASSCD || '-' || W2.COMBINED_SCHOOL_KIND || '-' || W2.COMBINED_CURRICULUM_CD || '-' || W2.COMBINED_SUBCLASSCD ");
            } else {
                stb.append(     " W2.COMBINED_SUBCLASSCD ");
            }
            stb.append(     "FROM   SUBCLASS_REPLACE_COMBINED_DAT W2 ");
            stb.append(     "WHERE  W2.YEAR = '" + _param._year + "' ");

            stb.append(     "GROUP BY ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(     "W2.COMBINED_CLASSCD || '-' || W2.COMBINED_SCHOOL_KIND || '-' || W2.COMBINED_CURRICULUM_CD || '-' || W2.COMBINED_SUBCLASSCD ");
            } else {
                stb.append(     "W2.COMBINED_SUBCLASSCD ");
            }
            stb.append(     ") ");
        }

        stb.append("ORDER BY T1.SUBCLASSCD, T1.SCHREGNO");

        return stb.toString();
    }

    private Common createCommon(final DB2UDB db2) {
        final boolean creditDrop = _param._creditDrop;
        final String testKindCd = _param._testKindCd;
        final String semester = _param._semester;
        final String semesterName = _param._semesterName;

        final Common common;
        if ("0101".equals(testKindCd)) {
            // 中間
            common = new CommonInter(creditDrop, testKindCd, semester, semesterName);
        } else if ("0201".equals(testKindCd) || "0202".equals(testKindCd)) {
            // 期末1 or 期末2
            common = new CommonTerm(creditDrop, testKindCd, semester, semesterName);
        } else if (_param.semesterGakunenMatu()) {
            // 学年
            common = new CommonGrade(creditDrop, testKindCd, semester, semesterName);
        } else {
            // 学期
            common = new CommonGakki(creditDrop, testKindCd, semester, semesterName);
        }
        log.fatal(common);

        common.loadAverage(db2);
        return common;
    }

    private static double round10(final int a, final int b) {
        return Math.round(a * 10.0 / b) / 10.0;
    }

    //--- 内部クラス -------------------------------------------------------
    private abstract class Common {
        String _fieldName;
        String _fieldNameValue;
        String _fieldName2;
        String _fieldChaircd;
        private final Map _averageMap;
        protected final boolean _creditDrop;

        protected final String _semesterName;
        protected final String _semester;
        protected final String _testKindCd;

        abstract void loadAverage(DB2UDB db2);
        abstract ScoreValue getScoreValue(ScoreDetail d);
        abstract boolean doPrintMark();

        public Common(
                final boolean creditDrop,
                final String testKindCd,
                final String semester,
                final String semesterName
        ) {
            _averageMap = new HashMap();
            _creditDrop = creditDrop;
            _testKindCd = testKindCd;
            _semester = semester;
            _semesterName = semesterName;
        }

        public void addAverage(
                final String chaircd,
                final Double avgScore
        ) {
            _averageMap.put(chaircd,avgScore);
        }

        public Double getAverage(final String chaircd) {
            return (Double)_averageMap.get(chaircd);
        }

        boolean hasCredit() {
            // 履修単位数/修得単位数なし
            return false;
        }

        /**
         * @return 学年末はTrueを戻します。
         */
        boolean isGakunenMatu() {
            return false;
        }

        abstract void setHead2(MyVrw32alp svf);

        /** {@inheritDoc} */
        public String toString() {
            return getClass().getName() + " : semesterName=" + _semesterName + ", testName=" + _param._testName;
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 中間試験成績の処理クラス
     */
    private class CommonInter extends Common {

        CommonInter(
                final boolean creditDrop,
                final String testKindCd,
                final String semester,
                final String semesterName
        ) {
            super(creditDrop, testKindCd, semester, semesterName);
            _fieldName  = "SEM" + _semester + "_INTR_SCORE";
            _fieldNameValue  = "SEM" + _semester + "_INTR_VALUE";
            _fieldName2 = "SEM" + _semester + "_INTR";
            _fieldChaircd = "SEM" + _semester + "_INTR_CHAIRCD";

        }

        /**
         *  中間試験成績 ページ見出し
         */
        void setHead2(final MyVrw32alp svf) {
            svf.VrsOut("TITLE", "（" + _param.getRecordTitleName()+"）");    // 成績名称
            svf.VrsOut("TEST", _param._testName);  // 成績名称

            // 詳細の凡例
            svf.VrsOut("DETAIL1_1", _param.getRecordTitleName());  // 科目名の下左
            svf.VrsOut("DETAIL1_2", _param.getDetailName()); // 科目名の下右

            // 合計欄
            svf.VrsOut("T_TOTAL", "総合点");
            svf.VrsOut("T_AVERAGE", "平均点");
        }

        String getFieldNameSelect() {
            if (_param._recordDiv == Param.RECORD_DIV_VALUE) {
                return _fieldNameValue;
            } else if (_param._recordDiv == Param.RECORD_DIV_SCORE){
                return _fieldName;
            }
            return null;
        }

        ScoreValue getScoreValue(final ScoreDetail d) {
            if (_param._recordDiv == Param.RECORD_DIV_VALUE) {
                return d._value;
            } else if (_param._recordDiv == Param.RECORD_DIV_SCORE){
                return d._score;
            }
            return null;
        }

        void loadAverage(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = sqlSubclassAverage(_fieldChaircd, getFieldNameSelect());
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {

                    if (null != rs.getString("SUBCLASSCD")) {
                        final String classcd = rs.getString("SUBCLASSCD").substring(0, 2);
                        final String classKey;
                        if ("1".equals(_param._useCurriculumcd) && "1".equals(_param._useClassDetailDat)) {
                            final String[] split = StringUtils.split(rs.getString("SUBCLASSCD"), "-");
                            classKey = split[0] + "-" + split[1];
                        } else {
                            classKey = classcd;
                        }
                        if (_common instanceof CommonGakki) {
                            final D008 d008 = (D008) _param._disableValueCd.get(classKey);
                            if (null != d008) {
                                if ("1".equals(d008._namespare1)) {
                                    ; //
                                } else {
                                    continue;
                                }
                            } else {
                                if (KNJDefineCode.subject_T.equals(classcd)) {
                                    continue; //
                                } else {
                                    ; //
                                }
                            }
                        } else if (_common instanceof CommonGrade) {
                            // 90
                            if (KNJDefineCode.subject_T.equals(classcd)) {
                                continue;
                            }
                            if (_param._disableValueCd.keySet().contains(classKey)) {
                                continue;
                            }
                        } else {
                            final D008 d008 = (D008) _param._disableValueCd.get(classKey);
                            if (null != d008) {
                                if ("1".equals(d008._namespare1)) {
                                    ; // 表示する
                                } else {
                                    continue;
                                }
                            } else {
                                if (KNJDefineCode.subject_T.equals(classcd)) {
                                    continue; // デフォルトでは表示しない
                                } else {
                                    ; // デフォルトでは表示する
                                }
                            }
                        }
                    }

                    final String chaircd = rs.getString("CHAIRCD");
                    final Double avgScore = (Double) rs.getObject("AVG_SCORE");

                    if (null != chaircd && null != avgScore) {
                        addAverage(chaircd, avgScore);
                    }
                }
            } catch (final Exception e) {
                log.error("平均の算出にてエラー", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        boolean doPrintMark() {
            return false;
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 期末試験成績の処理クラス
     */
    private class CommonTerm extends CommonInter {
        CommonTerm(
                final boolean creditDrop,
                final String testKindCd,
                final String semester,
                final String semesterName
        ) {
            super(creditDrop, testKindCd, semester, semesterName);
            if (_testKindCd.equals("0201")) {
                _fieldName  = "SEM" + _semester + "_TERM_SCORE";
                _fieldNameValue  = "SEM" + _semester + "_TERM_VALUE";
                _fieldName2 = "SEM" + _semester + "_TERM";
                _fieldChaircd = "SEM" + _semester + "_TERM_CHAIRCD";
            } else {
                _fieldName  = "SEM" + _semester + "_TERM2_SCORE";
                _fieldNameValue  = "SEM" + _semester + "_TERM2_VALUE";
                _fieldName2 = "SEM" + _semester + "_TERM2";
                _fieldChaircd = "SEM" + _semester + "_TERM2_CHAIRCD";
            }
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 学年成績の処理クラス
     */
    private class CommonGrade extends Common {
        public CommonGrade(
                final boolean creditDrop,
                final String testKindCd,
                final String semester,
                final String semesterName
        ) {
            super(creditDrop, testKindCd, semester, semesterName);
            _fieldName = "GRAD_VALUE";
            _fieldNameValue = "GRAD_VALUE";
            _fieldName2 = null;
            _fieldChaircd = "SEM1_INTR_CHAIRCD"; // ダミーでフィールドをセットしておく
        }

        boolean hasCredit() {
            // 履修単位数/修得単位数あり
            return true;
        }

        /**
         * @return 学年末はTrueを戻します。
         */
        boolean isGakunenMatu() {
            return true;
        }

        /**
         *  学年成績 ページ見出し
         */
        void setHead2(final MyVrw32alp svf) {
            svf.VrsOut("TITLE", "（評定）");    //成績名称

            // 詳細の凡例
            svf.VrsOut("DETAIL1_1", "評定");  // 科目名の下左
            svf.VrsOut("DETAIL1_2", "単位数"); // 科目名の下右

            // 合計欄
            svf.VrsOut("T_TOTAL", "評定合計");
            svf.VrsOut("T_AVERAGE", "評定平均");
        }

        ScoreValue getScoreValue(final ScoreDetail d) {
            return d._patternAssess;
        }

        void loadAverage(final DB2UDB db2) {}

        boolean doPrintMark() {
            return _creditDrop;
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 学期成績の処理クラス
     */
    private class CommonGakki extends CommonGrade {
        public CommonGakki(
                final boolean creditDrop,
                final String testKindCd,
                final String semester,
                final String semesterName
        ) {
            super(creditDrop, testKindCd, semester, semesterName);
            _fieldName = "SEM" + _semester + "_VALUE";
            _fieldNameValue = "SEM" + _semester + "_VALUE";
            _fieldName2 = null;
            _fieldChaircd = "SEM" + _semester + "_INTR_CHAIRCD"; // ダミーでフィールドをセットしておく
        }

        /**
         *  学期成績 ページ見出し
         */
        void setHead2(final MyVrw32alp svf) {
            svf.VrsOut("TITLE", ""); // 成績名称

            // 詳細の凡例
            svf.VrsOut("DETAIL1_1", "評価");  // 科目名の下左
            svf.VrsOut("DETAIL1_2", "単位数"); // 科目名の下右

            // 合計欄
            svf.VrsOut("T_TOTAL", "評価合計");
            svf.VrsOut("T_AVERAGE", "評価平均");
        }

        boolean hasCredit() {
            // 履修単位数/修得単位数なし
            return false;
        }

        /**
         * @return 学年末はTrueを戻します。
         */
        boolean isGakunenMatu() {
            return false;
        }
    }

    //--- 内部クラス -------------------------------------------------------
    private static class MyVrw32alp extends Vrw32alp {
        public int VrsOut(String field, String data) {
            if (null == field || null == data) {
                return 0;
            }
            return super.VrsOut(field, data);
        }

        public int VrsOutn(String field, int gyo, String data) {
            if (null == field || null == data) {
                return 0;
            }
            return super.VrsOutn(field, gyo, data);
        }

        public void doSvfOutNonZero(
                final String str1,
                final String str2
        ) {
            if (null == str1 || null == str2) {
                return;
            }
            if (str2.equals("0")) {
                return;
            }

            VrsOut(str1, str2);
        }

        public void doSvfOutNonZero(
                final String str,
                final int val
        ) {
            if (null == str || 0 == val) {
                return;
            }

            VrsOut(str, String.valueOf(val));
        }
    }

    //--- 内部クラス -------------------------------------------------------
    private class Manager {
        /** 帳票上に登場する全科目 */
        private final Map _subClasses = new HashMap();

        private final Common _common;

        Manager(final Common common) {
            _common = common;
        }

        SubClass getSubClass(final String code, final String abbv) {
            if (_subClasses.containsKey(code)) {
                return (SubClass) _subClasses.get(code);
            }
            final SubClass subClass = new SubClass(code, abbv);
            _subClasses.put(code, subClass);
            return subClass;
        }

        private ScoreValue createScoreValue(final String subClassCd, final String strScore) {
            if (null == strScore) {
                return null;
            }

            final String classcd = subClassCd.substring(0, 2);
            final String classKey;
            if ("1".equals(_param._useCurriculumcd) && "1".equals(_param._useClassDetailDat)) {
                final String[] split = StringUtils.split(subClassCd, "-");
                classKey = split[0] + "-" + split[1];
            } else {
                classKey = classcd;
            }
            if (_common instanceof CommonGakki) {
                final D008 d008 = (D008) _param._disableValueCd.get(classKey);
                if (null != d008) {
                    if ("1".equals(d008._namespare1)) {
                        ; //
                    } else {
                        return null;
                    }
                } else {
                    if (KNJDefineCode.subject_T.equals(classcd)) {
                        return null; //
                    } else {
                        ; //
                    }
                }
            } else if (_common instanceof CommonGrade) {
                // 90
                if (KNJDefineCode.subject_T.equals(classcd)) {
                    return null;
                }
                if (_param._disableValueCd.keySet().contains(classKey)) {
                    return null;
                }
            } else if (_common instanceof CommonInter && _param._recordDiv == Param.RECORD_DIV_VALUE) {
                final D008 d008 = (D008) _param._disableValueCd.get(classKey);
                if (null != d008) {
                    if ("1".equals(d008._namespare1)) {
                        ; // 表示する
                    } else {
                        return null;
                    }
                } else {
                    if (KNJDefineCode.subject_T.equals(classcd)) {
                        return null; // デフォルトでは表示しない
                    } else {
                        ; // デフォルトでは表示する
                    }
                }
            }
            return new ScoreValue(strScore);
        }
    }

    //--- 内部クラス -------------------------------------------------------
    private class HRInfo implements Comparable {
        private final Manager _manager;
        private final String _code;
        private String _staffName;
        private String _hrName;

        private final List _students = new LinkedList();

        private List _ranking;

        HRInfo(
                final Manager manager,
                final String code
        ) {
            _manager = manager;
            _code = code;
        }

        private void loadHRClassStaff(final DB2UDB db2) {
            final KNJ_Get_Info.ReturnVal returnval = _param._getinfo.Hrclass_Staff(
                    db2,
                    _param._year,
                    _param._semester,
                    _code,
                    ""
            );
            _staffName = returnval.val3;
            _hrName = returnval.val1;
        }

        private String sqlHrclassStdList() {
            final String sql;
            sql = "SELECT"
                + "  schregno,"
                + "  attendno,"
                + "  name,"
                + "  ent_date,"
                + "  ent_div,"
                + "  grd_date,"
                + "  grd_div"
                + " FROM v_schreg_info"
                + " WHERE year='" + _param._year + "'"
                + " AND semester='" + _param.specialSemester() + "'"
                + " AND grade || hr_class = ?"
                + " ORDER BY attendno"
                ;
            return sql;
        }

        private void loadStudents(final DB2UDB db2) throws Exception {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                ps = db2.prepareStatement(sqlHrclassStdList());
                ps.setString(1, _code);

                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("schregno");
                    final String attendno = rs.getString("attendno");
                    final String name = rs.getString("name");
                    final Date entDate = rs.getDate("ent_date");
                    final String entDiv = rs.getString("ent_div");
                    final Date grdDate = rs.getDate("grd_date");
                    final String grdDiv = rs.getString("grd_div");
                    final Student student = new Student(schregno, attendno, name, this, entDate, entDiv, grdDate, grdDiv);
                    _students.add(student);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }

            loadTransfer(db2);
//            loadStudentsInfo(db2);
            loadAttend(db2);
            loadAttendSubclassInfo(db2);
        }

        private void loadTransfer(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            for (final Iterator it = _students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                final String sql = sqlTrans(student);
                try {
                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String code = rs.getString("transfercd");
                        final String sDate = rs.getString("transfer_sdate");
                        if (null != code) {
                            student._trsDiv = code;
                            student._trsDate = sDate;
                        }
                    }
                } catch (final SQLException e) {
                    log.error("異動情報の取得でエラー", e);
                    throw e;
                }
            }
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        private String sqlTrans(final Student student) {
            final String sql;
            sql = "SELECT t1.transfercd, t1.transfer_sdate"
                + " FROM schreg_transfer_dat t1, semester_mst t2"
                + " WHERE t2.year='" + _param._year + "'"
                + " AND t2.semester='" + _param._semester + "'"
                + " AND t1.schregno='" + student._code + "'"
                + " AND t2.edate BETWEEN t1.transfer_sdate AND t1.transfer_edate"
                ;
            return sql;
        }

        /**
         * 任意の生徒の学籍情報を取得するSQL
         *   SEMESTER_MSTは'指示画面指定学期'で検索 => 学年末'9'有り
         *   SCHREG_REGD_DATは'指示画面指定学期'で検索 => 学年末はSCHREG_REGD_HDATの最大学期
         */
        private String sqlStdNameInfo() {
            final String sql;

            sql = "SELECT W6.HR_NAME, "
                +   "CASE WHEN W4.GRD_DATE IS NOT NULL THEN W4.GRD_DATE ELSE W4.ENT_DATE END AS KBN_DATE1, "
                +   "CASE WHEN W4.GRD_DATE IS NOT NULL THEN (SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1 = 'A003' AND S1.NAMECD2 = W4.GRD_DIV) "
                +     "ELSE (SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1 = 'A002' AND S1.NAMECD2 = W4.ENT_DIV) END AS KBN_NAME1, "
                +   "W5.TRANSFER_SDATE AS KBN_DATE2,"
                +   "(SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1='A004' AND S1.NAMECD2 = W5.TRANSFERCD) AS KBN_NAME2 "
                + "FROM    SCHREG_REGD_DAT W1 "
                + "INNER  JOIN SCHREG_REGD_HDAT W6 ON W6.YEAR = '" + _param._year + "' AND W6.SEMESTER = W1.SEMESTER AND W6.GRADE = W1.GRADE AND W6.HR_CLASS = W1.HR_CLASS "
                + "INNER  JOIN SEMESTER_MST     W2 ON W2.YEAR = '" + _param._year + "' AND W2.SEMESTER = '" + _param._semester + "' "
                + "INNER  JOIN SCHREG_BASE_MST  W3 ON W3.SCHREGNO = W1.SCHREGNO "
                + "LEFT   JOIN SCHREG_BASE_MST  W4 ON W4.SCHREGNO = W1.SCHREGNO "
                +                              "AND ((W4.GRD_DIV IN('2','3') AND W4.GRD_DATE < W2.EDATE) "
                +                               "OR (W4.ENT_DIV IN('4','5') AND W4.ENT_DATE > W2.SDATE)) "
                + "LEFT   JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = W1.SCHREGNO "
                +                                  "AND (W5.TRANSFERCD IN ('1','2') AND W2.EDATE BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE) "
                + "WHERE  W1.YEAR = '" + _param._year + "' "
                +    "AND W1.SCHREGNO = ? "
                +    "AND W1.SEMESTER = '" + _param.specialSemester() + "' "
                ;

            return sql;
        }

        /**
         * @deprecated
         * @param db2
         * @throws Exception
         */
        private void loadStudentsInfo(final DB2UDB db2) throws Exception {
            PreparedStatement ps = null;
            ResultSet rs = null;

            final String sql = sqlStdNameInfo();
            try {
                ps = db2.prepareStatement(sql);
                for (final Iterator it = _students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    ps.setString(1, student._code);
                    rs = ps.executeQuery();
                    if (rs.next()) {
                        final String d1 = rs.getString("KBN_DATE1");
                        final String n1 = rs.getString("KBN_NAME1");
                        final String d2 = rs.getString("KBN_DATE2");
                        final String n2 = rs.getString("KBN_NAME2");
                        final String transInfo;
                        if (null != d1) {
                            transInfo = KNJ_EditDate.h_format_JP(d1) + n1;
                        } else if (null != d2) {
                            transInfo = KNJ_EditDate.h_format_JP(d2) + n2;
                        } else {
                            transInfo = "";
                        }
                        student._transInfo = transInfo;
                    }
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private void loadAttend(final DB2UDB db2) throws Exception {
            ResultSet rs = null;

            try {
                String targetSemes = (_param._isRuikei) ? "9" : _param._semester;
                final String psKey = "ATTENDSEMES";
                if (null == _param._psMap.get(psKey)) {
                    final Map attendParamMap = new HashMap();
                    attendParamMap.put("useCurriculumcd", _param._useCurriculumcd);
                    attendParamMap.put("useVirus", _param._useVirus);
                    attendParamMap.put("useKoudome", _param._useKoudome);
                    attendParamMap.put("DB2UDB", db2);

                    String sql = AttendAccumulate.getAttendSemesSql(
                            _param._semesFlg,
                            _param._definecode,
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
                            _param._grade,
                            "?",
                            null,
                            "SEMESTER",
                            attendParamMap
                            );

                    PreparedStatement ps = db2.prepareStatement(sql);
                    _param._psMap.put(psKey, ps);
                }
                PreparedStatement ps = (PreparedStatement) _param._psMap.get(psKey);
                ps.setString(1, _code.substring(2));

                rs = ps.executeQuery();
                while (rs.next()) {
                    final Student student = (Student) getStudent(rs.getString("SCHREGNO"));
                    if (null == student) {
                        continue;
                    }
                    if (!targetSemes.equals(rs.getString("SEMESTER"))) {
                        continue;
                    }
                    final AttendInfo attendInfo = new AttendInfo(
                            rs.getInt("LESSON"),
                            rs.getInt("MLESSON"),
                            rs.getInt("SUSPEND"),
                            "true".equals(_param._useVirus) ? rs.getInt("VIRUS") : 0,
                            "true".equals(_param._useKoudome) ? rs.getInt("KOUDOME") : 0,
                            rs.getInt("MOURNING"),
                            rs.getInt("SICK"),
                            rs.getInt("PRESENT"),
                            rs.getInt("LATE"),
                            rs.getInt("EARLY"),
                            rs.getInt("TRANSFER_DATE")
                    );
                    student._attendInfo = attendInfo;
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(rs);
            }
        }

        private Student getStudent(final String schregno) {
            for (final Iterator it = _students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if (null != student._code && student._code.equals(schregno)) {
                    return student;
                }
            }
            return null;
        }

        private void loadAttendSubclassInfo(final DB2UDB db2) throws Exception {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = sqlAttendSubclassInfo(_code);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = _students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    int i = 1;
                    ps.setString(i++, student._code);

                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final AttendSubclassInfo attendSubclassInfo = new AttendSubclassInfo(
                                rs.getInt("JISU"),
                                rs.getInt("ABSENT1"),
                                rs.getInt("LATE_EARLY")
                        );
                        student._attendSubclassInfo = attendSubclassInfo;
                    }
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        public void loadRecordDat(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql;
                sql = "SELECT "
                    + ("1".equals(_param._useCurriculumcd) ? "classcd || '-' || school_kind || '-' || curriculum_cd || '-' || subclasscd as subclasscd, " : "subclasscd, ")
                    + "get_credit "
                    + " FROM record_dat"
                    + " WHERE year='" + _param._year + "'"
                    + " AND takesemes='0'"
                    + " AND schregno=?"
                    ;
                ps = db2.prepareStatement(sql);
                for (final Iterator it = _students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    int i = 1;
                    ps.setString(i++, student._code);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String subclassCd = rs.getString("subclasscd");
                        final String getCredit = rs.getString("get_credit");
                        student._recordDat.put(subclassCd, getCredit);
                        student.bindGetCredit(subclassCd, getCredit);
                    }
                    log.debug(student._code + ", " + student + ", RECORD_DATの数=" + student._recordDat.keySet().size());
                }
            } catch (final SQLException e) {
                log.error("RECORD_DATの読込みでエラー", e);
                throw e;
            }
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        private void loadScoreDetail(final DB2UDB db2) throws Exception {
            ResultSet rs = null;

            KNJSchoolMst knjSchoolMst = null;
            try {
                knjSchoolMst = new KNJSchoolMst(db2, _param._year);
            } catch (SQLException e) {
                log.warn("KNJSchoolMst error!", e);
            }

            try {
                final String psKey = "SCORE_DETAIL";
                if (null == _param._psMap.get(psKey)) {
                    final String sql = sqlStdSubclassDetail(_param, _common, knjSchoolMst);
                    log.info("明細抽出のSQL=" + sql);
                    PreparedStatement ps = db2.prepareStatement(sql);
                    _param._psMap.put(psKey, ps);
                }
                PreparedStatement ps = (PreparedStatement) _param._psMap.get(psKey);

                if ((String) _param._hasuuMap.get("befDayFrom") != null || (String) _param._hasuuMap.get("aftDayFrom") != null) {
                    if (_param._definecode.useschchrcountflg) {
                        ps.setString(2, _code);
                    }
                }


                for (final Iterator it = _students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    int i = 1;
                    ps.setString(i++, student._code);

                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final ScoreDetail detail = createScoreDetail(rs);
                        student.add(detail);
                    }
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(rs);
            }

            // 科目の合併情報を関連付ける
            _param.relateSubclass(_manager._subClasses);

            // 生徒の科目の合併情報を関連付ける
            for (final Iterator it = _students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                student.relateScoreDetails(_param._replaceCombined);
            }
        }

        private ScoreDetail createScoreDetail(final ResultSet rs) throws SQLException {
            // 科目
            final String subClassCd        = rs.getString("SUBCLASSCD");
            final SubClass subClass        = _manager.getSubClass(subClassCd, rs.getString("SUBCLASSNAME"));

            // 左下
            final ScoreValue score         = _manager.createScoreValue(subClassCd, rs.getString("SCORE"));
            final ScoreValue value         = _manager.createScoreValue(subClassCd, rs.getString("VALUE"));
            final ScoreValue patternAssess = _manager.createScoreValue(subClassCd, rs.getString("PATTERN_ASSESS"));

            // 欠課 - 右下
            final Double abnsent1          = (Double) rs.getObject("ABSENT1");
            final boolean isOver           = judgeOver(abnsent1, (BigDecimal) rs.getObject("ABSENCE_HIGH"));

            // 欠課時数(総時数) - 右上
            final Integer jisu             = (Integer) rs.getObject("JISU");

            // 履修単位
            final Integer compCredit       = (Integer) rs.getObject("COMP_CREDIT");
            // 修得単位
            final Integer getCredit        = (Integer) rs.getObject("GET_CREDIT");
            // [履修単位数/修得単位数]が有効か?
            final Integer onRecordComp     = (Integer) rs.getObject("ON_RECORD_COMP");
            final Integer replaceMoto      = (Integer) rs.getObject("REPLACEMOTO");
            final boolean compUncondition  = "1".equals(rs.getString("COMP_UNCONDITION_FLG"));
            final boolean enableCredit = enableCredit(onRecordComp, replaceMoto, isOver, compUncondition);

            // 単位数。CREDIT_MST.CREDITS。下段の真中
            final Integer credits          = (Integer) rs.getObject("CREDITS");

            final String chairCd           = rs.getString("CHAIRCD");

            final ScoreDetail detail = new ScoreDetail(
                    _manager,
                    subClass,
                    abnsent1,
                    isOver,
                    jisu,
                    score,
                    value,
                    patternAssess,
                    compCredit,
                    getCredit,
                    enableCredit,
                    credits,
                    chairCd
            );
            return detail;
        }

        // 成績不振者/成績優良者/皆勤者/欠課時数超過 設定
        private void setGradeAttendCheckMark() {
            for (final Iterator itS = _students.iterator(); itS.hasNext();) {
                final Student student = (Student) itS.next();
                student._isGradeGood = student.isGradeGood();
                student._isGradePoor = student.isGradePoor();
                student._isAttendPerfect = student.isAttendPerfect();
                student._isKekkaOver = student.isKekkaOver();
            }
        }

        /**
         * [履修単位数/修得単位数]が有効か?
         * @param onRecordComp
         * @param replaceMoto
         * @param isOver
         * @param compUnconditionFlg 単位マスタ.無条件フラグ。 true=無条件に単位マスタからの単位を表示する
         * @return [履修単位数/修得単位数]が有効ならtrue
         */
        private boolean enableCredit(final Integer onRecordComp, final Integer replaceMoto, final boolean isOver, final boolean compUnconditionFlg) {
            if (null == onRecordComp && compUnconditionFlg) {
                if (isOver) {
                    return false;
                }
            }

            if (null != replaceMoto && replaceMoto.intValue() >= 1) {
                return false;
            }
            return true;
        }

        private boolean judgeOver(final Double absent, final BigDecimal absenceHigh) {
            if (null == absent || null == absenceHigh) {
                return false;
            }
            if (0.1 > absent.floatValue() || 0 == absenceHigh.doubleValue()) {
                return false;
            }
            if (absenceHigh.doubleValue() < absent.floatValue()) {
                return true;
            }
            return false;
        }

        private List createRanking() {
            final List list = new LinkedList();
            for (final Iterator itS = _students.iterator(); itS.hasNext();) {
                final Student student = (Student) itS.next();
                student.compute();
                final Total total = student._total;
                if (0 < total.getCount()) {
                    list.add(total);
                }
            }

            Collections.sort(list);
            return list;
        }

        int rank(final Student student) {
            final Total total = student._total;
            if (0 >= total.getCount()) {
                return -1;
            }

            return 1 + _ranking.indexOf(total);
        }

        public int compareTo(final Object o) {
            if (!(o instanceof HRInfo)) return -1;
            final HRInfo that = (HRInfo) o;
            return _code.compareTo(that._code);
        }

        public String toString() {
            return _hrName + "[" + _staffName + "]";
        }
    }

    //--- 内部クラス -------------------------------------------------------
    private class Student implements Comparable {
        private final String _code;
        private final String _attendNo;
        private final String _name;
        private final HRInfo _hrInfo;

        private final Date _entDate;
        private final String _entDiv;

        private final Date _grdDate;
        private final String _grdDiv;

        private String _trsDate;
        private String _trsDiv;

        private boolean _isGradeGood;  // 成績優良者
        private boolean _isGradePoor;  // 成績不振者
        private boolean _isAttendPerfect;  // 皆勤者
        private boolean _isKekkaOver;  // 欠課時数超過が1科目でもある者

        /**
         * @deprecated
         */
        private String _transInfo;

        private AttendInfo _attendInfo;
        private AttendSubclassInfo _attendSubclassInfo;

        private final List _scoreDetails = new LinkedList();
        private Total _total;

        /**
         * 成績データ
         * @deprecated
         */
        private final Map _recordDat = new HashMap();

        Student(
                final String code,
                final String attendNo,
                final String name,
                final HRInfo hrInfo,
                final Date entDate,
                final String entDiv,
                final Date grdDate,
                final String grdDiv
        ) {
            _code = code;
            _attendNo = attendNo;
            _name = name;

            _entDate = entDate;
            _entDiv = entDiv;
            _grdDate = grdDate;
            _grdDiv = grdDiv;

            _hrInfo = hrInfo;
        }

        public void bindGetCredit(final String subclassCd, final String getCredit) {
            for (final Iterator it = _scoreDetails.iterator(); it.hasNext();) {
                final ScoreDetail sd = (ScoreDetail) it.next();
                if (!subclassCd.equals(sd._subClass._code)) {
                    continue;
                }
                if (null == getCredit) {
                    continue;
                }
                sd._creditsRecordDat = new Integer(getCredit);
            }
        }

        void add(final ScoreDetail scoreDetail) {
            _scoreDetails.add(scoreDetail);
        }

        void compute() {
            _total = new Total(this);
        }

        public int compareTo(final Object o) {
            if (!(o instanceof Student)) {
                return -1;
            }
            final Student that = (Student) o;
            int rtn;
            rtn = _hrInfo.compareTo(that._hrInfo);
            if (0 != rtn) {
                return rtn;
            }
            rtn = _attendNo.compareTo(that._attendNo);
            return rtn;
        }

        int rank() {
            return _hrInfo.rank(this);
        }

        public String toString() {
            return _attendNo + ":" + _name;
        }

        /**
         * @return 成績優良者（評定平均が4.3以上）は true を戻します。
         */
        boolean isGradeGood() {
            final BigDecimal avg = _total._avgBigDecimal;
            if (null == avg) { return false; }
            float float1 = _param._assess.floatValue();
            float float2 = avg.setScale(1,BigDecimal.ROUND_HALF_UP).floatValue();
            if (float1 <= float2) { return true; }
            return false;
        }

        /**
         * @return 成績不振者（評定１が1つでもある）は true を戻します。
         */
        boolean isGradePoor() {
            for (final Iterator itD = _scoreDetails.iterator(); itD.hasNext();) {
                final ScoreDetail detail = (ScoreDetail) itD.next();
                final ScoreValue val = detail._patternAssess;
                if (null == val) continue;
                if (!val.hasIntValue()) continue;
                if (val._val <= _param._hyotei){ return true; }
            }
            return false;
        }

        /**
         * @return 皆勤（欠席、遅刻、早退、欠課が０）なら true を戻します。
         */
        boolean isAttendPerfect() {
            if (null == _attendInfo || ! _attendInfo.isAttendPerfect()) {
                return false;
            }

            if (null == _attendSubclassInfo || ! _attendSubclassInfo.isAttendPerfect()) {
                return false;
            }
            return true;
        }

        /**
         * @return 欠課超過が1科目でもあるなら true を戻します。
         */
        boolean isKekkaOver() {
            for (final Iterator itD = _scoreDetails.iterator(); itD.hasNext();) {
                final ScoreDetail detail = (ScoreDetail) itD.next();
                if (true == detail._isOver){ return true; }
            }
            return false;
        }

        int print(final int line) {
            final int usingLines = _scoreDetails.isEmpty() ? 1 : (int) Math.ceil((double) _scoreDetails.size() / MAX_COLUMN);
            final int yLine = line % MAX_LINE;
            int white = 0;
            if (0 != yLine) {
                final int y = yLine + usingLines;
                if (MAX_LINE < y) {
                    final int n = MAX_LINE - yLine;
                    white = n;

                    // ページ末までの、残り行数分を、改行(つまり、改ページ)
                    for (int i = 0; i < n; i++) {
                        _form.VrEndRecord();
                    }
                }
            }

            final String transInfo = getTransInfo();
            _form.printStudent(_name, _attendNo, transInfo);

            final Common common = _manager._common;
            if (common instanceof CommonGrade) {
                _form.printMark(_isGradePoor, _isGradeGood, _isAttendPerfect, _isKekkaOver);
            }

            _form.printTotal(_total);
            _form.printAttendInfo(_attendInfo);

            int column = 0;
            for (final Iterator it = _scoreDetails.iterator(); it.hasNext();) {
                if (0 != column && 0 == column % MAX_COLUMN) {
                    // 一人で、二行以上を使う場合
                    _form.VrEndRecord();
                }
                final ScoreDetail detail = (ScoreDetail) it.next();
                _form.printDetail(detail, column);
                column++;
            }
            return white + usingLines;
        }

        private String getTransInfo() {
            final String rtn;
            if (enableGrd()) {
                final Map map = (Map) _param._map.get("A003");
                final String hoge = _grdDate.toString();
                rtn = KNJ_EditDate.h_format_JP(hoge) + map.get(_grdDiv);
            } else if (enableTrs()) {
                final Map map = (Map) _param._map.get("A004");
                rtn = KNJ_EditDate.h_format_JP(_trsDate) + map.get(_trsDiv);
            } else if (enableEnt()) {
                final Map map = (Map) _param._map.get("A002");
                final String hoge = _entDate.toString();
                rtn = KNJ_EditDate.h_format_JP(hoge) + map.get(_entDiv);
            } else {
                return null;
            }

            log.debug("備考に関する情報。日付(区分)" + toString());
            log.debug("\t卒業:" + _grdDate + "(" + _grdDiv + ")");
            log.debug("\t異動:" + _trsDate + "(" + _trsDiv + ")");
            log.debug("\t入学:" + _entDate + "(" + _entDiv + ")");
            return rtn;
        }

        /**
         * 入学データは有効か?
         * @return 有効ならtrue
         */
        private boolean enableEnt() {
            if (null == _entDate) {
                return false;
            }
            if (!"4".equals(_entDiv) && !"5".equals(_entDiv)) {
                return false;
            }

            final Semester semes = (Semester) _param._semesterMap.get(_param._semester);
            final Date aaa = semes._sDate;    // 指定学期開始日
            if (_entDate.compareTo(aaa) < 0) { // _entDate < aaa
                return false;
            }

            return true;
        }

        /**
         * 卒業データは有効か?
         * @return 有効ならtrue
         */
        private boolean enableGrd() {
            if (null == _grdDate) {
                return false;
            }
            if (!"2".equals(_grdDiv) && !"3".equals(_grdDiv)) {
                return false;
            }

            final Semester semes = (Semester) _param._semesterMap.get(_param._semester);
            final Date aaa = semes._eDate;    // 指定学期終了日
            if (_grdDate.compareTo(aaa) > 0) { // _grdDate > aaa
                return false;
            }

            return true;
        }

        /**
         * 異動データは有効か?
         * @return 有効ならtrue
         */
        private boolean enableTrs() {
            if (null == _trsDate) {
                return false;
            }
            if (!"1".equals(_trsDiv) && !"2".equals(_trsDiv)) {
                return false;
            }
            return true;
        }

        /**
         * 生徒の科目の合併情報を関連付ける。
         * @param replaceCombined 合併情報が関連付いた科目
         */
        void relateScoreDetails(final Set replaceCombined) {
            for (final Iterator it = replaceCombined.iterator(); it.hasNext();) {
                final ReplaceCombined aaa = (ReplaceCombined) it.next();

                // 先も元も存在するか？
                final ScoreDetail saki = find(aaa._combined);
                final ScoreDetail moto = find(aaa._attend);
                if (null == saki || null == moto) {
                    log.warn(toString() + ":合併先,元にnullがある:" + saki + ", " + moto + ",(" + aaa._combined + ", " + aaa._attend + ")");
                    continue;
                }

                // 合併先から見た元への関連付け
                saki._attendScoreDetails.add(moto);
                saki._fixed = aaa._fixed;

                // 合併元から見た先への関連付け
                moto._combined = saki;
            }
        }

        private ScoreDetail find(final String subClassCd) {
            for (final Iterator it = _scoreDetails.iterator(); it.hasNext();) {
                final ScoreDetail sd = (ScoreDetail) it.next();
                if (subClassCd.equals(sd._subClass._code)) {
                    return sd;
                }
            }
            return null;
        }
    }

    //--- 内部クラス -------------------------------------------------------
    private class AttendInfo {
        private final int _lesson;
        private final int _mLesson;
        private final int _suspend;
        private final int _virus;
        private final int _koudome;
        private final int _mourning;
        private final int _absent;
        private final int _present;
        private final int _late;
        private final int _early;
        private final int _transDays;

        private AttendInfo(
                final int lesson,
                final int mLesson,
                final int suspend,
                final int virus,
                final int koudome,
                final int mourning,
                final int absent,
                final int present,
                final int late,
                final int early,
                final int transDays
        ) {
            _lesson = lesson;
            _mLesson = mLesson;
            _suspend = suspend;
            _virus = virus;
            _koudome = koudome;
            _mourning = mourning;
            _absent = absent;
            _present = present;
            _late = late;
            _early = early;
            _transDays = transDays;
        }

        /**
         * @return 皆勤（欠席、遅刻、早退が０）なら true を戻す。
         */
        boolean isAttendPerfect() {
            if (_absent == 0 && _late == 0 && _early == 0) { return true; }
            return false;
        }
    }

    private class AttendSubclassInfo {
        private final int _lesson;
        private final int _absent;
        private final int _lateEarly;

        private AttendSubclassInfo(
                final int lesson,
                final int absent,
                final int lateEarly
        ) {
            _lesson = lesson;
            _absent = absent;
            _lateEarly = lateEarly;
        }

        /**
         * @return 皆勤（欠席、遅刻、早退が０）なら true を戻す。
         */
        boolean isAttendPerfect() {
            if (  _param._isAttendPerfectSubclassLateEarly && _absent == 0 && _lateEarly == 0) { return true; }
            if (! _param._isAttendPerfectSubclassLateEarly && _absent == 0) { return true; }
            return false;
        }
    }

    //--- 内部クラス -------------------------------------------------------
    private class SubClass {
        private final String _code;
        private final String _abbv;

        /** 合併先 */
        private SubClass _combined;
        /** 合併元 */
        private Set _attendSubClasses = new HashSet();
        /** 単位固定/加算フラグ(合併先科目の単位数取得方法)。<br>true:固定  false:加算 */
        private boolean _fixed; // 自信が合併先で合併元を持っている時、意味を成すフィールド

        SubClass(final String code, final String abbv) {
            _code = code;
            _abbv = abbv;
        }

        public String toString() {
            return _code + ":" + _abbv;
        }

        public boolean equals(final Object obj) {
            if (!(obj instanceof SubClass)) {
                return false;
            }
            final SubClass that = (SubClass) obj;
            return _code.equals(that._code);
        }

        public int hashCode() {
            return _code.hashCode();
        }

        String getCode() { return _code; }
        String getAbbv() { return _abbv; }

        /**
         * 合併先か?
         */
        boolean isRelateTo() {
            // 「合併元を持っている」という事は「先」である。
            return _attendSubClasses.size() > 0;
        }

        /**
         * 合併元か?
         */
        boolean isRelateFrom() {
            // 「合併先を持っている」という事は「元」である。
            return null != _combined;
        }
    }

    //--- 内部クラス -------------------------------------------------------
    private static class ScoreValue {
        private final String _strScore;
        private final boolean _isInt;
        private int _val;

        ScoreValue(final String strScore) {
            _strScore = strScore;
            _isInt = !StringUtils.isEmpty(_strScore) && StringUtils.isNumeric(_strScore);
            if (_isInt) {
                _val = Integer.parseInt(_strScore);
            }
        }

        static ScoreValue create(final String strScore) {
            if (null == strScore) {
                return null;
            }
            return new ScoreValue(strScore);
        }

        String getScore() { return _strScore; }
        boolean hasIntValue() { return _isInt; }
        int getScoreAsInt() { return _val; }
    }

    //--- 内部クラス -------------------------------------------------------
    private class ScoreDetail {
        private final Manager _manager;
        private final SubClass _subClass;
        /** 欠課 */
        private final Double _absent;
        /** 総時数 */
        private final Integer _jisu;

        /** 中間・期末の時の下左 */
        private final ScoreValue _score;
        /** 中間・期末の時の下左 */
        private final ScoreValue _value;
        /** 学年・学期の時の下左 */
        private final ScoreValue _patternAssess;

        /** 履修単位数 */
        private final Integer _compCredit;
        /** 修得単位数 */
        private final Integer _getCredit;
        /** [履修単位数/修得単位数]が有効か? */
        private final boolean _enableCredit;

        /** 単位数(単位マスタ) */
        private final Integer _credits;
        /** 単位数(成績データ) */
        private Integer _creditsRecordDat;

        /** 欠課時数越えか? */
        private final boolean _isOver;
        private final String _chaircd;

        /** 合併先 */
        private ScoreDetail _combined;
        /** 合併元 */
        private Set _attendScoreDetails = new HashSet();
        /** 単位固定/加算フラグ(合併先科目の単位数取得方法)。<br>true:固定  false:加算 */
        private boolean _fixed; // 自信が合併先で合併元を持っている時、意味を成すフィールド

        ScoreDetail(
                final Manager manager,
                final SubClass subClass,
                final Double absent,
                final boolean isOver,
                final Integer jisu,
                final ScoreValue score,
                final ScoreValue value,
                final ScoreValue patternAssess,
                final Integer compCredit,
                final Integer getCredit,
                final boolean enableCredit,
                final Integer credits,
                final String chaircd
        ) {
            _manager = manager;
            _subClass = subClass;
            _absent = absent;
            _isOver = isOver;
            _jisu = jisu;

            _score = score;
            _value = value;
            _patternAssess = patternAssess;

            _compCredit = compCredit;
            _getCredit = getCredit;
            _enableCredit = enableCredit;

            _credits = credits;
            _chaircd = chaircd;
        }

        Integer getCompCredit() {
            return _enableCredit ? _compCredit : null;
        }

        Integer getGetCredit() {
            return _enableCredit ? _getCredit : null;
        }

        /**
         * 合併先か?
         */
        boolean isRelateTo() {
            // 「合併元を持っている」という事は「先」である。
            return _attendScoreDetails.size() > 0;
        }

        /**
         * 合併元か?
         */
        boolean isRelateFrom() {
            // 「合併先を持っている」という事は「元」である。
            return null != _combined;
        }

        String printJisu() {
            if (isRelateTo()) {
                if (isTypeFix()) {
                    int total = 0;
                    for (final Iterator it = _attendScoreDetails.iterator(); it.hasNext();) {
                        final ScoreDetail sd = (ScoreDetail) it.next();
                        total += sd._jisu.intValue();
                    }
                    return String.valueOf(total);
                } else {
                    return "";
                }
            }

            return _jisu.toString();
        }

        String getCredits() {
            if (null == _credits) {
                return null;
            }

            if (isRelateFrom()) {
                return "(" + _credits.toString() + ")";
            }

            if (isRelateTo() && isTypeAdd()) {
                int total = 0;
                for (final Iterator it = _attendScoreDetails.iterator(); it.hasNext();) {
                    final ScoreDetail sd = (ScoreDetail) it.next();
                    total += sd._credits.intValue();
                }
                return String.valueOf(total);
            }

            return _credits.toString();
        }

        String getRecDatCredits() {
            if (null == _creditsRecordDat) {
                return null;
            }

            if (isRelateFrom()) {
                return "(" + _creditsRecordDat.toString() + ")";
            }

            return _creditsRecordDat.toString();
        }

        /**
         * 単位固定/加算フラグは固定タイプか?
         * @return 固定タイプなら true
         */
        public boolean isTypeFix() {
            return _fixed;
        }

        /**
         * 単位固定/加算フラグは加算タイプか?
         * @return 加算タイプなら true
         */
        public boolean isTypeAdd() {
            return !_fixed;
        }

        public String toString() {
            return _subClass.toString();
        }

        public boolean enableSubClass() {
            final String classcd = _subClass.getCode().substring(0, 2);
            return classcd.compareTo(SUBJECT_U) <= 0;
        }

        /**
         * 合併元全ての欠課数の合計値を得る。
         * @return 合併元全ての欠課数の合計値
         */
        public Double getAllAbsent() {
            double rtn = 0.0;
            for (final Iterator it = _attendScoreDetails.iterator(); it.hasNext();) {
                final ScoreDetail sd = (ScoreDetail) it.next();
                if (null == sd._absent) {
                    continue;
                }
                rtn += sd._absent.doubleValue();
            }
            return new Double(rtn);
        }
    }

    //--- 内部クラス -------------------------------------------------------
    private class Total implements Comparable {
        private final Student _stundet;

        private int _total;
        private int _count;
        private BigDecimal _avgBigDecimal;

        /** 履修単位数 */
        private int _compCredit;
        /** 修得単位数 */
        private int _getCredit;

        Total(final Student student) {
            _stundet = student;
            compute();
        }

        int getCount() { return _count; }

        private void compute() {
            final Common common = _stundet._hrInfo._manager._common;

            int total = 0;
            int count = 0;

            int compCredit = 0;
            int getCredit = 0;

            for (final Iterator it = _stundet._scoreDetails.iterator(); it.hasNext();) {
                final ScoreDetail detail = (ScoreDetail) it.next();

                if (common.isGakunenMatu() && detail.isRelateFrom() && null != detail._patternAssess) {
                    continue;
                }

                final ScoreValue scoreValue = common.getScoreValue(detail);
                if (enable(scoreValue) && detail.enableSubClass()) {
                    total += scoreValue.getScoreAsInt();
                    count++;
                }

                final Integer c = detail.getCompCredit();
                if (null != c) {
                    compCredit += c.intValue();
                }

                final Integer g = detail.getGetCredit();
                if (null != g) {
                    getCredit += g.intValue();
                }
            }

            _total = total;
            _count = count;
            if (0 < count) {
                final float avg = (float) round10(total, count);
                _avgBigDecimal = new BigDecimal(TOTAL_DEC_FMT.format(avg).toString());
            }
            _compCredit = compCredit;
            _getCredit = getCredit;
        }

        private boolean enable(final ScoreValue scoreValue) {
            return null != scoreValue && scoreValue.hasIntValue();
        }

        public int compareTo(final Object o) {
            if (!(o instanceof Total)) {
                return -1;
            }
            final Total that = (Total) o;

            return that._avgBigDecimal.compareTo(this._avgBigDecimal);
        }

        public boolean equals(final Object o) {
            if (!(o instanceof Total)) {
                return false;
            }
            final Total that = (Total) o;
            return that._avgBigDecimal.equals(this._avgBigDecimal);
        }

        public String toString() {
            return _avgBigDecimal.toString();
        }
    }

    private static class D008 {
        final String _namecd2;
        final String _namespare1;
        D008(final String namecd2, final String namespare1) {
            _namecd2 = namecd2;
            _namespare1 = namespare1;
        }
        public String toString() {
            return "D008(" + _namecd2 + ", " + _namespare1 + ")";
        }
    }

    private static class Param {
        private final String _year;

        /** 学期 */
        private final String _semester;
        /** LOG-IN時の学期（現在学期） */
        private final String _loginSemes;

        private final String _grade;
        private final String _testKindCd;
        /** テスト名称テーブル */
        private final String _countFlgTable;
        /** テスト名称 */
        private String _testName;

        /** 累計か */
        private final boolean _isRuikei;
        /** 出欠集計開始日付 */
        private final String _sDate;
        /** 出欠集計日付 */
        private final String _date;
        /** 単位保留マークを付けるか? */
        private final boolean _creditDrop;
        /** 年組 */
        private final String[] _hrClasses;

        private String _semesterName;
        private String _semesterDateS;
        private String _yearDateS;

        /** 出力区分 (素点/評価・評定)*/
        final int _recordDiv;
        final static int RECORD_DIV_SCORE = 1; // 素点
        final static int RECORD_DIV_VALUE = 2; // 評価

        /** 評定を無いものとして扱う教科コード */
        private Map _disableValueCd = new HashMap();

        /** 科目合併設定データ */
        private Set _replaceCombined = new HashSet();

        /** 名称マスタ */
        private Map _map = new HashMap();

        /** 学期 */
        private Map _semesterMap = new HashMap();   // TAKAESU: 整理せよ!

        /** 出欠状況取得引数 */
        private String _periodInState;
        private Map _attendSemesMap;
        private Map _hasuuMap;
        private boolean _semesFlg;
        private final String SSEMESTER = "1";

        /** １：年間、２：随時 */
        private String _absenceDiv = "1";

        /** 合併先科目を印刷するか */
        private boolean _isPrintSakiKamoku;

        private final Float _assess;
        private final int _hyotei;

        private KNJDefineSchool _definecode;  //各学校における定数等設定のクラス
        private KNJSchoolMst _knjSchoolMst;
        private KNJ_Get_Info _getinfo = new KNJ_Get_Info();

        private DecimalFormat _absentFmt;

        /** 「皆勤者」の判定基準 */
        private boolean _isAttendPerfectSubclassLateEarly;
        private boolean _isAttendPerfectSubclass90over;

        /** 教育課程コードを使用するか */
        private final String _useCurriculumcd;
        private final String _useClassDetailDat;
        private final String _useVirus;
        private final String _useKoudome;
        private final boolean _hasSchChrDatExecutediv;

        private Map _psMap = new HashMap();

        public Param(final HttpServletRequest request, final DB2UDB db2) {

            //成績優良者評定平均の基準値
            final Float assess;
            if (request.getParameter("ASSESS") != null && !"".equals(request.getParameter("ASSESS"))) {
                assess = new Float(request.getParameter("ASSESS"));
            } else {
                assess = new Float(4.3);
            }
            final int hyotei;
            if (NumberUtils.isDigits(request.getParameter("HYOTEI"))) {
                hyotei = Integer.parseInt(request.getParameter("HYOTEI"));
            } else {
                hyotei = 1;
            }

            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _loginSemes = request.getParameter("SEME_FLG");
            _grade = request.getParameter("GRADE");
            _testKindCd = PARAM_GRAD_SEM_KIND.equals(request.getParameter("TESTKINDCD")) ? GRAD_SEM_KIND : request.getParameter("TESTKINDCD");
            _countFlgTable = request.getParameter("COUNTFLG");
            _isRuikei = "1".equals(request.getParameter("ATTEND"));
            _sDate = KNJ_EditDate.H_Format_Haifun(request.getParameter("SDATE"));
            _date = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE"));
            _creditDrop = request.getParameter("OUTPUT4") != null;
            _hrClasses = request.getParameterValues("CLASS_SELECTED");
            _recordDiv = (request.getParameter("RECORD_DIV") != null) ?
                    Integer.parseInt(request.getParameter("RECORD_DIV")) : Param.RECORD_DIV_SCORE;
            _assess = assess;
            _hyotei = hyotei;
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useClassDetailDat = request.getParameter("useClassDetailDat");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
            _hasSchChrDatExecutediv = setTableColumnCheck(db2, "SCH_CHR_DAT", "EXECUTEDIV");

            _definecode = createDefineCode(db2);
            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }
            loadAttendSemesArgument(db2);

            setSemesters(db2);
            setTestName(db2);

            // 年度の開始日
            final KNJ_Get_Info.ReturnVal returnval1 = _getinfo.Semester(db2, _year, "9");
            _yearDateS = returnval1.val2;

            // 評定を無いものとして扱う教科コードを設定する
            setDisableValueCd(db2);

            // 科目合併設定データを読み込む
            setReplaceCombined(db2);

            setNameMst(db2);

            // 合併先科目を印刷するかを設定する
            setPrintSakiKamoku(db2);

            //「皆勤者」の判定基準
            setD050(db2);
        }

        public void closeQuietly() {
            for (final Iterator it = _psMap.values().iterator(); it.hasNext();) {
                final PreparedStatement ps = (PreparedStatement) it.next();
                DbUtils.closeQuietly(ps);
            }
        }

        public void setSemesters(final DB2UDB db2) {
            ResultSet rs = null;
            try {
                db2.query("SELECT * FROM semester_mst WHERE year='" + _year + "'");
                rs = db2.getResultSet();
                while (rs.next()) {
                    final String code = rs.getString("semester");
                    final String name = rs.getString("semestername");
                    final Date sDate = rs.getDate("sdate");
                    final Date eDate = rs.getDate("edate");

                    final Semester semester = new Semester(code, name, sDate, eDate);
                    _semesterMap.put(code, semester);

                    if (_semester.equals(code)) {
                        _semesterName = name;
                        _semesterDateS = sDate.toString();
                    }
                }
            } catch (final SQLException e) {
                log.error("学期マスタの読込みでエラー", e);
            }
            db2.commit();
            DbUtils.closeQuietly(null, null, rs);

            if (null == _semesterDateS) {
                _semesterDateS = _year + "-04-01";
            }
        }

        public void setTestName(final DB2UDB db2) {
            if (GRAD_SEM_KIND.equals(_testKindCd)) {
                _testName = semesterGakunenMatu() ? "(評定)" : "";
            } else {
                ResultSet rs = null;
                try {
                    final String sql = KNJ_Testname.getTestNameSql(_countFlgTable, _year, _semester, _testKindCd);
                    db2.query(sql);
                    rs = db2.getResultSet();
                    while (rs.next()) {
                        _testName = rs.getString("TESTITEMNAME");
                    }
                } catch (final SQLException e) {
                    log.error("テスト名称の読込みでエラー", e);
                } finally {
                    db2.commit();
                    DbUtils.closeQuietly(null, null, rs);
                }
            }
        }

        public void setNameMst(final DB2UDB db2) {
            final String[] namecd1 = {
                    "A002",
                    "A003",
                    "A004",
            };
            ResultSet rs = null;
            try {
                for (int i = 0; i < namecd1.length; i++) {
                    final Map map = new HashMap();

                    db2.query("SELECT namecd2, name1 FROM v_name_mst WHERE year='" + _year + "' AND namecd1='" + namecd1[i] + "'");
                    rs = db2.getResultSet();
                    while (rs.next()) {
                        final String namecd2 = rs.getString("namecd2");
                        final String name1 = rs.getString("name1");
                        map.put(namecd2, name1);
                    }

                    _map.put(namecd1[i], map);
                }
            } catch (final SQLException e) {
                log.error("名称マスタの読込みでエラー", e);
            }
            db2.commit();
            DbUtils.closeQuietly(null, null, rs);
        }

        /**
         * 「皆勤者」の判定基準
         * NAMESPARE1・・・Y:皆勤者かどうかを判断する際に「授業の遅刻・早退」もチェックする
         * NAMESPARE2・・・Y:皆勤者かどうかを判断する際に「教科コードが90より大きい科目」の出欠情報もチェックする
         */
        private void setD050(final DB2UDB db2) {
            _isAttendPerfectSubclassLateEarly = false;
            _isAttendPerfectSubclass90over = false;
            ResultSet rs = null;
            try {
                db2.query("SELECT NAMESPARE1,NAMESPARE2 FROM NAME_MST WHERE NAMECD1 = 'D050' AND NAMECD2 = '01'");
                rs = db2.getResultSet();
                if (rs.next()) {
                    _isAttendPerfectSubclassLateEarly = "Y".equals(rs.getString("NAMESPARE1"));
                    _isAttendPerfectSubclass90over = "Y".equals(rs.getString("NAMESPARE2"));
                }
                log.debug("皆勤者の判定基準：LateEarly = " + _isAttendPerfectSubclassLateEarly + ", 90over = " + _isAttendPerfectSubclass90over);
            } catch (final Exception e) {
                log.warn("「皆勤者」の判定基準の取得失敗", e);
            } finally{
                db2.commit();
                DbUtils.closeQuietly(null, null, rs);
            }
        }

        /**
         * 科目合併設定データを読み込む
         */
        public void setReplaceCombined(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sqlReplaceCombined();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String combined = rs.getString("combined_subclasscd");
                    final String attend = rs.getString("attend_subclasscd");
                    final String calcCreditFlg = rs.getString("calculate_credit_flg");
                    final boolean fixed = "1".equals(calcCreditFlg) ? true : false;

                    _replaceCombined.add(new ReplaceCombined(combined, attend, fixed));
                }
            } catch (SQLException e) {
                log.error("評定を無いものとして扱う教科コードの取得エラー", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.info("科目合併設定の件数=" + _replaceCombined.size());
        }

        private String sqlReplaceCombined() {
            final String sql;
            sql = "SELECT"
                + ("1".equals(_useCurriculumcd) ? " COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD AS combined_subclasscd, " : " combined_subclasscd,")
                + ("1".equals(_useCurriculumcd) ? " ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD AS attend_subclasscd, " : " attend_subclasscd,")
                + " calculate_credit_flg"
                + " FROM subclass_replace_combined_dat"
                + " WHERE YEAR='" + _year + "'"
                ;
            return sql;
        }

        /**
         * 科目の合併情報を関連付ける。
         * @param map 帳票上に登場する全科目
         */
        public void relateSubclass(final Map map) {
            final Set removeRC = new HashSet();

            for (final Iterator it = _replaceCombined.iterator(); it.hasNext();) {
                final ReplaceCombined aaa = (ReplaceCombined) it.next();

                // 先も元も存在するか？
                final SubClass saki = (SubClass) map.get(aaa._combined);
                final SubClass moto = (SubClass) map.get(aaa._attend);
                if (null == saki || null == moto) {
                    log.warn("合併先,元のいづれかがnullなので無効:" + saki + ", " + moto + ",(" + aaa._combined + ", " + aaa._attend + ")");
                    removeRC.add(aaa);
                    continue;
                }

                // 合併先から見た元への関連付け
                saki._attendSubClasses.add(moto);
                saki._fixed = aaa._fixed;

                // 合併元から見た先への関連付け
                moto._combined = saki;
            }
//            _replaceCombined.removeAll(removeRC);
        }

        public String getNendo() {
            final int year = Integer.parseInt(_year);
            return nao_package.KenjaProperties.gengou(year);
        }

        public boolean semesterGakunenMatu() {
            return _semester.equals("9");
        }

        public String specialSemester() {
            if (semesterGakunenMatu()) {
                return _loginSemes;
            } else {
                return _semester;
            }
        }

        /**
         * 評定を無いものとして扱う教科コードを設定する。
         */
        void setDisableValueCd(final DB2UDB db2) {
            // 名称マスタから取得する
            ResultSet rs = null;
            try {
                final String sql;
                if ("1".equals(_useCurriculumcd) && "1".equals(_useClassDetailDat)) {
                    sql = "SELECT classcd || '-' || school_kind as namecd2, class_remark7 as namespare1 FROM class_detail_dat WHERE year='" + _year + "' AND class_seq='003' ";
                } else {
                    sql = "SELECT namecd2, namespare1 FROM v_name_mst WHERE year='" + _year+ "' AND namecd1='D008' AND namecd2 IS NOT NULL";
                }
                db2.query(sql);
                rs = db2.getResultSet();
                while (rs.next()) {
                    _disableValueCd.put(rs.getString("namecd2"), new D008(rs.getString("namecd2"), rs.getString("namespare1")));
                }
            } catch (SQLException e) {
                log.error("評定を無いものとして扱う教科コードの取得エラー", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, null, rs);
            }

            log.debug("評定無し扱い教科コード=" + _disableValueCd);
        }

        /**
         * 合併先科目を印刷するか
         */
        void setPrintSakiKamoku(final DB2UDB db2) {
            // 初期値：印刷する
            _isPrintSakiKamoku = true;

            // 名称マスタ「D021」「01」から取得する
            ResultSet rs = null;
            try {
                final String sql = "SELECT NAMESPARE3 FROM V_NAME_MST WHERE YEAR='" + _year+ "' AND NAMECD1='D021' AND NAMECD2='01'";
                db2.query(sql);
                rs = db2.getResultSet();
                while (rs.next()) {
                    String str = rs.getString("NAMESPARE3");
                    if ("Y".equals(str)) _isPrintSakiKamoku = false;
                }
            } catch (SQLException e) {
                log.error("合併先科目を印刷するかフラグの取得エラー", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, null, rs);
            }

            log.debug("合併先科目を印刷するか：" + _isPrintSakiKamoku);
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

        public void loadAttendSemesArgument(DB2UDB db2) {
            try {
                loadSemester(db2, _year);
                // 出欠の情報
                final KNJDefineCode definecode0 = setClasscode0(db2);
                final String z010Name1 = setZ010Name1(db2);
                _periodInState = AttendAccumulate.getPeiodValue(db2, definecode0, _year, SSEMESTER, _semester);
                _attendSemesMap = AttendAccumulate.getAttendSemesMap(db2, z010Name1, _year); // _sDate: 年度開始日, _date: LOGIN_DATE
                _hasuuMap = AttendAccumulate.getHasuuMap(_attendSemesMap, _sDate, _date); // _sDate: 指定開始日, _date: LOGIN_DATE
                _semesFlg = ((Boolean) _hasuuMap.get("semesFlg")).booleanValue();

                log.info(" 出欠集計範囲：" + _sDate + FROM_TO_MARK + _date);
                log.info(" attendSemesMap = " + _attendSemesMap);
                log.info(" hasuuMap = " + _hasuuMap);
                log.info(" semesFlg = " + _semesFlg);
            } catch (Exception e) {
                log.error("loadAttendSemesArgument exception", e);
            }
        }

        /**
         * 年度の開始日を取得する
         */
        private void loadSemester(final DB2UDB db2, String year) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map map = new HashMap();
            final List list = new ArrayList();
            try {
                ps = db2.prepareStatement(sqlSemester(year));
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
        }

        private String sqlSemester(String year) {
            final String sql;
            sql = "select"
                + "   SEMESTER,"
                + "   SEMESTERNAME,"
                + "   SDATE"
                + " from"
                + "   SEMESTER_MST"
                + " where"
                + "   YEAR='" + year + "'"
                + " order by SEMESTER"
            ;
            return sql;
        }

        public String getRecordTitleName() {
            if (_recordDiv == RECORD_DIV_SCORE) {
                return "素点";
            } else if (_recordDiv == RECORD_DIV_VALUE){
                return "評価";
            }
            return null;
        }

        public String getDetailName() {
            if (_recordDiv == RECORD_DIV_SCORE) {
                return "平均点";
            } else if (_recordDiv == RECORD_DIV_VALUE){
                return "単位数";
            }
            return null;
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

        private KNJDefineSchool createDefineCode(final DB2UDB db2) {
            final KNJDefineSchool definecode = new KNJDefineSchool();

            // 各学校における定数等設定
            definecode.defineCode(db2, _year);
            log.debug("semesdiv=" + definecode.semesdiv + "   absent_cov=" + definecode.absent_cov + "   absent_cov_late=" + definecode.absent_cov_late);

            switch (definecode.absent_cov) {
                case 0:
                case 1:
                case 2:
                    _absentFmt = new DecimalFormat("0");
                    break;
                default:
                    _absentFmt = new DecimalFormat("0.0");
            }

            return definecode;
        }

    }

    private class Form {
        final MyVrw32alp svf = new MyVrw32alp();

        MyVrw32alp svfInit(
                final HttpServletResponse response,
                final HttpServletRequest request
        ) throws IOException {
            svf.VrInit();

            response.setContentType("application/pdf");
            svf.VrSetSpoolFileStream(response.getOutputStream());
            svf.VrSetForm(FORM_FILE, 4);

            return svf;
        }

        void setHead1(final String staffName, final String hrName) {
            svf.VrsOut("STAFFNAME", staffName);  // 担任名
            svf.VrsOut("CLASSNAME", hrName);  // 組名称
        }

        void printStudent(final String name, final String attendNo, final String remark) {
            svf.VrsOut("NAME",     name);    //氏名
            svf.VrsOut("ATTENDNO", attendNo);    //出席番号
            svf.VrsOut("REMARK",   remark); //備考
        }

        /**
         * 成績優良者、成績不振者、皆勤者、欠課時数超過有者のマークを印字
         */
        void printMark(
                final boolean isGradePoor,
                final boolean isGradeGood,
                final boolean isAttendPerfect,
                final boolean isKekkaOver
        ) {
            if (isGradePoor) {
                svf.VrsOut("CHECK1", "★");
            } else if (isGradeGood) {
                svf.VrsOut("CHECK1", "☆");
            } else {
                svf.VrsOut("CHECK1", "");
            }
            if (isAttendPerfect) {
                svf.VrsOut("CHECK2", "○");
            } else if (isKekkaOver) {
                svf.VrsOut("CHECK2", "●");
            } else {
                svf.VrsOut("CHECK2", "");
            }
        }

        void printFooterMark() {
            svf.VrsOut("MARK1_1", "☆");
            svf.VrsOut("MARK1_2", "　：成績優良者（評定平均" + _param._assess.toString() + "以上）");
            svf.VrsOut("MARK2_1", "○");
            svf.VrsOut("MARK2_2", "　：皆勤者");
            svf.VrsOut("MARK3_1", "★");
            svf.VrsOut("MARK3_2", "　：成績不振者（評定1を持つもの）");
            svf.VrsOut("MARK4_1", "●");
            svf.VrsOut("MARK4_2", "　：欠課時数超過が1科目でもあるもの");
        }

        void printHeader() {
            svf.VrsOut("NENDO", _param.getNendo() + "年度");
            svf.VrsOut("TERM", _common._semesterName); // 学期名称

            // 作成日(現在処理日)・出欠集計範囲の出力 05/05/22Modify
            try {
                final Date date = new Date();

                final SimpleDateFormat sdfY = new SimpleDateFormat("yyyy");
                final String nowNendo = nao_package.KenjaProperties.gengou(Integer.parseInt(sdfY.format(date)));

                final SimpleDateFormat sdf = new SimpleDateFormat("年M月d日H時m分");
                final String nowDate = sdf.format(date);

                svf.VrsOut("DATE", nowNendo + nowDate);
            } catch (final NumberFormatException e) {
                log.warn("処理日の算出にて変換エラー", e);
            }

            final String date_S = KNJ_EditDate.h_format_JP(_param._sDate);
            final String date_E = KNJ_EditDate.h_format_JP(_param._date);

            // 出欠集計範囲(欠課数の集計範囲)
            svf.VrsOut("DATE2", date_S + FROM_TO_MARK + date_E);

            // 一覧表枠外の文言
            svf.VrAttribute("NOTE1", "Paint=(1,70,1),Bold=1");
            svf.VrsOut("NOTE1", " ");
            svf.VrsOut("NOTE2", "：欠課時数超過者");

            // 「出欠の記録」の日付範囲
            svf.VrsOut("DATE3", date_S + FROM_TO_MARK + date_E);

            // 対象校時および名称取得  05/06/15
            _common.setHead2(svf);

            // 固定個所(詳細の凡例)
            svf.VrsOut("DETAIL1", "科目名");
            svf.VrsOut("DETAIL2", "総時数");
            svf.VrsOut("DETAIL2_1", "欠課");
        }

        void printTotal(final Total total) {
            if (0 < total._count) {
                svf.VrsOut("TOTAL", String.valueOf(total._total));  // 総合点
                svf.VrsOut("AVERAGE", total._avgBigDecimal.toString());  // 平均点
            }

            final int rank = total._stundet.rank();
            if (1 <= rank) {
                svf.VrsOut("RANK", String.valueOf(rank));  // 順位
            }

            final Common common = total._stundet._hrInfo._manager._common;
            if (common.hasCredit()) {
                svf.VrsOut("R_CREDIT", String.valueOf(total._compCredit));    // 履修単位数
                svf.VrsOut("C_CREDIT", String.valueOf(total._getCredit)); //修得単位数
            }
        }

        void printAttendInfo(final AttendInfo info) {
            svf.doSvfOutNonZero("LESSON",  info._lesson);       // 授業日数
            svf.doSvfOutNonZero("PRESENT", info._mLesson);      // 出席すべき日数
            svf.doSvfOutNonZero("SUSPEND", info._suspend + info._koudome + info._virus);      // 出席停止
            svf.doSvfOutNonZero("KIBIKI",  info._mourning);     // 忌引
            svf.doSvfOutNonZero("ABSENCE", info._absent);       // 欠席日数
            svf.doSvfOutNonZero("ATTEND",  info._present);      // 出席日数
            svf.doSvfOutNonZero("LATE",    info._late);         // 遅刻回数
            svf.doSvfOutNonZero("LEAVE",   info._early);        // 早退回数
            svf.doSvfOutNonZero("ABROAD",  info._transDays);    // 留学等実績
        }

        void printDetail(final ScoreDetail detail, final int column) {
            final int j = (column % MAX_COLUMN) + 1;

            // 科目名
            svf.VrsOut("SUBJECT" + j, detail._subClass.getAbbv());

            // 総時数
            if (null != detail._jisu && !detail.isRelateTo()) {
                svf.VrsOut("TOTALLESSON" + j, detail.printJisu());
            }

            final Common common = _manager._common;

            // 成績(素点/評定/評価)
            final ScoreValue grading = common.getScoreValue(detail);
            if (null != grading) {
                if (common.doPrintMark() && 1 == grading.getScoreAsInt()) {
                    svf.VrsOut("GRADING" + j, "*" + grading.getScore());
                } else {
                    svf.VrsOut("GRADING" + j, grading.getScore());
                }
            }

            if (common instanceof CommonInter) {
                if (_param._recordDiv == Param.RECORD_DIV_VALUE) {
                    // テスト評価は「単位マスタの単位数」
                    svf.doSvfOutNonZero("CREDIT" + j, detail.getCredits());
                } else {
                    // 中間・期末は「平均」
                    final Double avg = common.getAverage(detail._chaircd);
                    if (null != avg) {
                        svf.VrsOut("CREDIT" + j, avg.toString());
                    }
                }
            } else if (common instanceof CommonGakki) {
                // 学期は「単位マスタの単位数」
                svf.doSvfOutNonZero("CREDIT" + j, detail.getCredits());
            } else {
                // 学年は「成績データの単位数」
                svf.doSvfOutNonZero("CREDIT" + j, detail.getRecDatCredits());
            }

            // 欠課
            if (detail.isRelateTo() && detail.isTypeAdd()) {
                return;
            }

            final Double absent;
            if (detail.isRelateTo() && detail.isTypeFix()) {
                absent = detail.getAllAbsent();
            } else {
                absent = detail._absent;
            }

            if (null != absent) {
                final int value = (int) Math.round(absent.doubleValue() * 10.0);
                if (0 != value) {
                    if (detail._isOver) {
                        svf.VrAttribute("KEKKA" + j, "Paint=(2,70,1),Bold=1");
                    }
                    svf.VrsOut("KEKKA" + j, _param._absentFmt.format(absent.floatValue()));
                    if (detail._isOver) {
                        svf.VrAttribute("KEKKA" + j, "Paint=(0,0,0),Bold=0");   //網掛けクリア
                    }
                }
            }
        }

        void VrEndRecord() {
            svf.VrEndRecord();
        }

        void closeSvf(final boolean hasData) {
            if (null == svf) {
                return;
            }
            if (!hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }

            final int ret = svf.VrQuit();
            log.info("===> VrQuit():" + ret);
        }
    }

    private static class ReplaceCombined {
        /** 合併先科目コード */
        private final String _combined;

        /** 合併元科目コード */
        private final String _attend;

        /** 単位固定/加算フラグ(合併先科目の単位数取得方法)。<br>true:固定  false:加算 */
        private final boolean _fixed;

        /**
         * コンストラクタ。
         * @param combined 合併先科目コード
         * @param attend 合併元科目コード
         * @param fixed 単位固定/加算フラグ(合併先科目の単位数取得方法)。<br>true:固定  false:加算
         */
        public ReplaceCombined(final String combined, final String attend, final boolean fixed) {
            _combined = combined;
            _attend = attend;
            _fixed = fixed;
        }
    }

    private static class Semester {
        private final String _code;
        private final String _name;
        private final Date _sDate;
        private final Date _eDate;

        public Semester(
                final String code,
                final String name,
                final Date sDate,
                final Date eDate
        ) {
            _code = code;
            _name = name;
            _sDate = sDate;
            _eDate = eDate;
        }

        public String toString() {
            return _code + "/" + _name + "/" + _sDate + "/" + _eDate;
        }
    }
}
