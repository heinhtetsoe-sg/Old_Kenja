/*
 * $Id: f45e233d898d803ab2558f571056389f9d87e438 $
 *
 * 作成日: 2018/07/03
 * 作成者: tawada
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

public class KNJD155G {

    private static final Log log = LogFactory.getLog(KNJD155G.class);

    private boolean _hasData;

    private static final String SEMESTER1    = "1";
    private static final String SEMESTER2    = "2";
    private static final String SEMESTER3    = "3";
    private static final String SEMESTER9    = "9";
    private static final String CHUKAN_TEST  = "010101";
    private static final String CHUKAN_TEST2  = "010201";
    private static final String KIMATSU_TEST = "020101";
    private static final String GAKUNENMATSU = "020101";
    private static final String[] TESTKINDS = new String[] {SEMESTER1 + CHUKAN_TEST  + ":１学期:中間",
                                                                 SEMESTER1 + KIMATSU_TEST + ":１学期:期末",
                                                                 SEMESTER2 + CHUKAN_TEST2  + ":２学期:中間",
                                                                 SEMESTER2 + KIMATSU_TEST + ":２学期:期末",
                                                                 SEMESTER3 + GAKUNENMATSU + ":３学期:学年末"
                                                                 };
    private static final String TOTAL5  = "555555";
    private static final String TOTAL9  = "999999";

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
        final int maxLine = 22;

        final List students = Student.getStudents(db2, _param);
        for (Iterator iterator = students.iterator(); iterator.hasNext();) {
            svf.VrSetForm("KNJD155G.frm", 4);

            final Student student = (Student) iterator.next();

            final String title = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._ctrlYear)) + "年度　個人成績表";
            svf.VrsOut("TITLE", title); // タイトル
            svf.VrsOut("SCHOOL_NAME", _param._certifScholDatSchoolName); // 学校名
            final String hrName = student._hrName + " " + String.valueOf(Integer.parseInt(student._attendNo)) + "番";
            svf.VrsOut("HR_NAME", hrName);     // 年組番
            svf.VrsOut("NAME", student._name); // 氏名
            svf.VrsOut("TEACHER_NAME", student._trStaff); // 担任

            // 考査名称
            for (int i = 0; i < TESTKINDS.length; i++) {
                final String sem = TESTKINDS[i].substring(0, 1);
                final String fieldSem = "3".equals(sem) ? "9" : sem;

                final String semesterName = StringUtils.split(TESTKINDS[i], ":")[1]; //学期名
                svf.VrsOut("SEMESTER" + fieldSem, semesterName);

                final String fieldNum = "3".equals(sem) ? "": "_" + TESTKINDS[i].substring(2, 3);
                final String testName = StringUtils.split(TESTKINDS[i], ":")[2]; //考査名称
                svf.VrsOut("TEST_ITEM_NAME"+ fieldSem + fieldNum, testName);
            }

            // ５教科、３教科、出席情報
            setAttendanceInfo(svf, student);

            // 得点、序列
            final int lineCnt = setScoreInfo(svf, student);

            for (int i = lineCnt; i < maxLine; i++) {
                svf.VrEndRecord();
            }

            _hasData = true;
        }
    }

    /**
     * 得点・序列を印刷
     * @param svf
     * @param student
     * @return
     */
    private int setScoreInfo(final Vrw32alp svf, final Student student) {
        int lineCnt = 0;

        //教科毎の科目数をカウント
        final Map classSubclassMap = new TreeMap();
        int subClassCnt = 1;
        String befClassCd = "";
        for (Iterator it1 = student._subClassMap.keySet().iterator(); it1.hasNext();) {
            final String subclassCd = (String) it1.next();
            final SubclassInfo subInfo = (SubclassInfo) student._subClassMap.get(subclassCd);

            if (!"".equals(befClassCd) && !befClassCd.equals(subInfo._classCd)) {
                subClassCnt = 1;
            }

            classSubclassMap.put(subInfo._classCd, String.valueOf(subClassCnt));
            subClassCnt++;
            befClassCd = subInfo._classCd;
        }
        befClassCd = "";
        int subClassOrder = 1;

        for (Iterator it1 = student._subClassMap.keySet().iterator(); it1.hasNext();) {
            final String subclassCd = (String) it1.next();
            final SubclassInfo subInfo = (SubclassInfo) student._subClassMap.get(subclassCd);

            if (!"".equals(befClassCd) && !befClassCd.equals(subInfo._classCd)) {
                subClassOrder = 1;
            }

            svf.VrsOut("GRPCD", subInfo._classCd);
            final int abbvLen = KNJ_EditEdit.getMS932ByteLength(subInfo._className);
            final int abbvStrCnt = subInfo._className.length();
            final ClassAbbvFieldSet abbvFieldSet = setClassAbbv(Integer.parseInt((String)classSubclassMap.get(subInfo._classCd)), subClassOrder, subInfo._className, abbvLen, abbvStrCnt);
            svf.VrsOut("CLASS_NAME" + abbvFieldSet._fieldNumer, abbvFieldSet._setAbbv); // 教科名
            final String setSubField = KNJ_EditEdit.getMS932ByteLength(subInfo._subClassName) > 20 ? "3": KNJ_EditEdit.getMS932ByteLength(subInfo._subClassName) > 16 ? "2": "1";
            svf.VrsOut("SUBCLASS_NAME" + setSubField, subInfo._subClassName); // 科目名

            for (int i = 0; i < TESTKINDS.length; i++) {
                final String sem = TESTKINDS[i].substring(0, 1);
                final String fieldSem = "3".equals(sem) ? "9" : sem;
                final String fieldNum = "3".equals(sem) ? "": "_" + TESTKINDS[i].substring(2, 3);
                final String semTestKindCd = StringUtils.split(TESTKINDS[i], ":")[0];

                if (semTestKindCd.compareTo(_param._testKindCd) > 0) break;

                final Map scoreMap = (Map) student._scoreData.get(semTestKindCd);
                if (null == scoreMap) continue;
                final ScoreRank soreRank = (ScoreRank) scoreMap.get(subInfo._subclassCd);

                if (null != soreRank) {
                    svf.VrsOut("SCORE" + fieldSem + fieldNum, nullToAlt(String.valueOf(soreRank._score), ""));    // 得点
                    svf.VrsOut("RANK"  + fieldSem + fieldNum, nullToAlt(String.valueOf(soreRank._classRank), ""));// HR順位
                }
            }
            lineCnt++;
            subClassOrder++;
            befClassCd = subInfo._classCd;
            svf.VrEndRecord();
        }
        return lineCnt;
    }

    /**
     * 合計情報、出席情報を印刷
     * @param svf
     * @param student
     */
    private void setAttendanceInfo(final Vrw32alp svf, final Student student) {
    	Attendance sum = new Attendance();
        for (int i = 0; i < TESTKINDS.length; i++) {
            final String sem = TESTKINDS[i].substring(0, 1);
            final String fieldSem = "3".equals(sem) ? "9" : sem;
            final String fieldNum = "3".equals(sem) ? "": "_" + TESTKINDS[i].substring(2, 3);
            final String semTestKindCd = StringUtils.split(TESTKINDS[i], ":")[0];

            if (semTestKindCd.compareTo(_param._testKindCd) > 0) break;

            final Map scoreMap = (Map) student._scoreData.get(semTestKindCd);
            if (null != scoreMap) {
                // ５教科
                final ScoreRank soreRank9 = (ScoreRank) scoreMap.get(student._useTotal5 ? TOTAL5 : TOTAL9);
                if (null != soreRank9) {
                    svf.VrsOut("TOTAL_SCORE" + fieldSem + fieldNum, String.valueOf(soreRank9._score));    // 合計
                    svf.VrsOut("TOTAL_AVE"   + fieldSem + fieldNum, sishagonyu(soreRank9._avg)); // 平均
                    svf.VrsOut("TOTAL_RANK"  + fieldSem + fieldNum, String.valueOf(soreRank9._classRank));// HR順位
                }
            }

            // 出欠情報
            final Attendance att = (Attendance) student._attendMap.get(semTestKindCd);
            if (null != att) {
            	sum = sum.add(att);
                svf.VrsOut("LESSON"  + fieldSem + fieldNum, String.valueOf(sum._lesson));                 // 授業日数
                svf.VrsOut("SUSPEND" + fieldSem + fieldNum, String.valueOf(sum._suspend + sum._mourning));// 出停・忌引
                svf.VrsOut("MUST"    + fieldSem + fieldNum, String.valueOf(sum._mlesson));                // 出席すべき日数
                svf.VrsOut("PRESENT" + fieldSem + fieldNum, String.valueOf(sum._attend));                 // 出席日数
                svf.VrsOut("NOTICE"  + fieldSem + fieldNum, String.valueOf(sum._absence));                // 欠席
                svf.VrsOut("LATE"    + fieldSem + fieldNum, String.valueOf(sum._late));                   // 遅刻
                svf.VrsOut("EARLY"   + fieldSem + fieldNum, String.valueOf(sum._leave));                  // 早退
            }
        }
    }

    private static String sishagonyu(final String s) {
    	if (!NumberUtils.isNumber(s)) {
    		return null;
    	}
    	return new BigDecimal(s).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private static class Student {
        final String _schregno;
        public String _name;
        public String _cmcCd;
        public String _grade;
        public String _gradeName;
        public String _hrClass;
        public String _hrName;
        public String _attendNo;
        public String _trStaff;
        public boolean _useTotal5;

        private final Map _subClassMap = new TreeMap();
        private final Map _scoreData = new TreeMap();
        final Map _attendMap = new TreeMap();
//        private AttendInfo _attendInfo = new AttendInfo(0, 0, 0, 0, 0, 0, 0, 0, 0);

        public Student(final String schregno) {
            _schregno   = schregno;
        }

        /**
         * 生徒情報をセット
         * @param db2
         * @param param
         * @return
         */
        private static List getStudents(final DB2UDB db2, final Param param) {
            final List retList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getRegdDatSql(param);
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final Student student = new Student(rs.getString("SCHREGNO"));
                    retList.add(student);

                    final StringBuffer staffname = new StringBuffer();
                    final String[] staffnameArray = {rs.getString("STAFFNAME1"), rs.getString("STAFFNAME2"), rs.getString("STAFFNAME3")};
                    String comma = "";
                    for (int i = 0; i < staffnameArray.length; i++) {
                    	if (StringUtils.isBlank(staffnameArray[i])) {
                    		continue;
                    	}
                    	staffname.append(comma).append(staffnameArray[i]);
                    	comma = " / ";
                    }

                    student._name       = rs.getString("NAME");
                    student._cmcCd      = rs.getString("CMC_CD");
                    student._grade      = rs.getString("GRADE");
                    student._gradeName  = rs.getString("GRADE_NAME1");
                    student._hrName     = rs.getString("HR_NAME");
                    student._attendNo   = rs.getString("ATTENDNO");
                    student._trStaff    = staffname.toString();
                    student._useTotal5  = "1".equals(rs.getString("USE_TOTAL5"));
                }

            } catch (SQLException ex) {
                log.error("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            //科目取得
            setSubclass(db2, retList, param);

            //成績・序列
            setScoreValue(db2, retList, param);

            //出欠情報
            loadAttend(db2, retList, param);

            return retList;
        }

        /** 生徒取得SQL */
        private static String getRegdDatSql(final Param param) {
            final String semester = SEMESTER9.equals(param._semester) ? param._ctrlSemester: param._semester;

            final StringBuffer stb = new StringBuffer();

            stb.append(" WITH TOTAL5 AS ( SELECT ");
            stb.append("       T1.GRADE, ");
            stb.append("       T1.COURSECD, ");
            stb.append("       T1.MAJORCD, ");
            stb.append("       T1.COURSECODE ");
            stb.append("     FROM REC_SUBCLASS_GROUP_DAT T1 ");
            stb.append("     WHERE YEAR = '" + param._ctrlYear + "' ");
            stb.append("       AND GROUP_DIV = '5' ");
            stb.append("     GROUP BY ");
            stb.append("       T1.GRADE, ");
            stb.append("       T1.COURSECD, ");
            stb.append("       T1.MAJORCD, ");
            stb.append("       T1.COURSECODE ");
            stb.append("     HAVING ");
            stb.append("       COUNT(*) > 0 ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     REGD.SCHREGNO, ");
            stb.append("     BASE.NAME, ");
            stb.append("     REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE AS CMC_CD, ");
            stb.append("     REGD.GRADE, ");
            stb.append("     GDAT.GRADE_NAME1, ");
            stb.append("     HDAT.HR_NAME, ");
            stb.append("     REGD.ATTENDNO, ");
            stb.append("     STF1.STAFFNAME AS STAFFNAME1, ");
            stb.append("     STF2.STAFFNAME AS STAFFNAME2, ");
            stb.append("     STF3.STAFFNAME AS STAFFNAME3, ");
            stb.append("     CASE WHEN TOTAL5.GRADE IS NOT NULL THEN 1 END AS USE_TOTAL5 ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT REGD ");
            stb.append("     INNER JOIN  SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR     = REGD.YEAR ");
            stb.append("                                    AND HDAT.SEMESTER = REGD.SEMESTER ");
            stb.append("                                    AND HDAT.GRADE    = REGD.GRADE ");
            stb.append("                                    AND HDAT.HR_CLASS = REGD.HR_CLASS ");
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR  = REGD.YEAR ");
            stb.append("                                    AND GDAT.GRADE = REGD.GRADE ");
            stb.append("     LEFT JOIN STAFF_MST STF1 ON STF1.STAFFCD = HDAT.TR_CD1 ");
            stb.append("     LEFT JOIN STAFF_MST STF2 ON STF2.STAFFCD = HDAT.TR_CD2 ");
            stb.append("     LEFT JOIN STAFF_MST STF3 ON STF3.STAFFCD = HDAT.TR_CD3 ");
            stb.append("     LEFT JOIN TOTAL5 ON TOTAL5.GRADE  = REGD.GRADE ");
            stb.append("                     AND TOTAL5.COURSECD = REGD.COURSECD ");
            stb.append("                     AND TOTAL5.MAJORCD = REGD.MAJORCD ");
            stb.append("                     AND TOTAL5.COURSECODE = REGD.COURSECODE ");
            stb.append(" WHERE ");
            stb.append("         REGD.YEAR     = '" + param._ctrlYear + "' ");
            stb.append("     AND REGD.SEMESTER = '" + semester + "' ");
            stb.append("     AND REGD.GRADE    = '" + param._grade + "' ");
            if ("1".equals(param._isClass)) {
                stb.append("     AND REGD.HR_CLASS IN " + SQLUtils.whereIn(true, param._categorySelected));
            } else {
                stb.append("     AND REGD.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected));
            }
            stb.append(" ORDER BY ");
            stb.append("     REGD.GRADE || REGD.HR_CLASS || REGD.ATTENDNO ");

            return stb.toString();
        }

        /**
         * 受講科目をセット
         * @param db2
         * @param student
         * @param param
         */
        private static void setSubclass(final DB2UDB db2, final List students, final Param param) {
            final String prestatementSybclass = sqlSubclass(param);
            log.debug("setSubClass sql = " + prestatementSybclass);

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(prestatementSybclass);

                for (final Iterator it = students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    ps.setString(1, student._schregno);

                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String classCd        = rs.getString("CLASSCD");
                        final String subclassCd     = rs.getString("SUBCLASSCD");
                        final String className      = rs.getString("CLASSABBV");
                        final String subClassName   = rs.getString("SUBCLASSABBV");
                        final SubclassInfo subInfo = new SubclassInfo(className, subClassName, classCd, subclassCd);

                        student._subClassMap.put(subclassCd, subInfo);
                    }
                    DbUtils.closeQuietly(rs);

                    // 表示しない科目を除く
                    for (final Iterator rit = param._d046List.iterator(); rit.hasNext();) {
                    	final String subclasscd = (String) rit.next();
                    	if (student._subClassMap.containsKey(subclasscd)) {
                    		student._subClassMap.remove(subclasscd);
                    	}
                    }
                }
            } catch (SQLException e) {
                log.error("get subclassSQL exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        /** 受講科目取得SQL */
        private static String sqlSubclass(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     CHIR.CLASSCD, ");
            stb.append("     CHIR.CLASSCD || '-' || CHIR.SCHOOL_KIND || '-' || CHIR.CURRICULUM_CD || '-' || CHIR.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     CLAS.CLASSABBV, ");
            stb.append("     SUBC.SUBCLASSABBV ");
            stb.append(" FROM ");
            stb.append("     CHAIR_STD_DAT STDD ");
            stb.append("     INNER JOIN CHAIR_DAT CHIR ON CHIR.YEAR     = STDD.YEAR ");
            stb.append("                              AND CHIR.SEMESTER = STDD.SEMESTER ");
            stb.append("                              AND CHIR.CHAIRCD  = STDD.CHAIRCD ");
            stb.append("     LEFT JOIN CLASS_MST CLAS ON CLAS.CLASSCD     = CHIR.CLASSCD ");
            stb.append("                             AND CLAS.SCHOOL_KIND = CHIR.SCHOOL_KIND ");
            stb.append("     LEFT JOIN SUBCLASS_MST SUBC ON SUBC.CLASSCD       = CHIR.CLASSCD ");
            stb.append("                                AND SUBC.SCHOOL_KIND   = CHIR.SCHOOL_KIND ");
            stb.append("                                AND SUBC.CURRICULUM_CD = CHIR.CURRICULUM_CD ");
            stb.append("                                AND SUBC.SUBCLASSCD    = CHIR.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("         STDD.YEAR     = '" + param._ctrlYear + "' ");
            stb.append("     AND STDD.SCHREGNO = ? ");
            stb.append(" ORDER BY ");
            stb.append("     SUBCLASSCD ");

            return stb.toString();
        }

        /**
         * 成績・序列をセット
         * @param db2
         * @param student
         * @param param
         */
        private static void setScoreValue(final DB2UDB db2, final List students, final Param param) {
            final String prestatementRecordScore = sqlRecordScore(param);
            log.debug("setScoreValue sql = " + prestatementRecordScore);

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(prestatementRecordScore);

                for (final Iterator it = students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    ps.setString(1, student._schregno);

                    Map subClassMap = new TreeMap();
                    String befKindCd = "";
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String semTestKindCd  = rs.getString("SEMESTER") + rs.getString("TESTKINDCD");
                        if (!"".equals(befKindCd) && !befKindCd.equals(semTestKindCd)) {
                            subClassMap = new TreeMap();
                        }
                        final String subclassCd     = rs.getString("SUBCLASSCD");
                        final String miniSubCd      = rs.getString("MINI_SUBCLASSCD");

                        final String score      = rs.getString("SCORE");
                        final String classRank  = rs.getString("CLASS_RANK");
                        final String avg   = rs.getString("AVG");
                        final ScoreRank scorRank = new ScoreRank(score, classRank, avg);

                        if (TOTAL9.equals(miniSubCd) || TOTAL5.equals(miniSubCd)) {
                            subClassMap.put(miniSubCd, scorRank);
                        } else {
                            subClassMap.put(subclassCd, scorRank);
                        }
                        student._scoreData.put(semTestKindCd, subClassMap);
                        befKindCd = semTestKindCd;
                        // log.debug(" scoreRank = " + student + " , semTestKindCd = " + testKindCd + " , tableDiv = " + tableDiv + " "+ sr);
                    }
                    DbUtils.closeQuietly(rs);
                }
            } catch (SQLException e) {
                log.error("recordRankSQL exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        /** 得点・序列取得SQL */
        private static String sqlRecordScore(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SDIV.SEMESTER, ");
            stb.append("     SDIV.TESTKINDCD || SDIV.TESTITEMCD || SDIV.SCORE_DIV AS TESTKINDCD, ");
            stb.append("     SDIV.CLASSCD || '-' || SDIV.SCHOOL_KIND || '-' || SDIV.CURRICULUM_CD || '-' || SDIV.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     SDIV.SUBCLASSCD AS MINI_SUBCLASSCD, ");
            stb.append("     SDIV.SCORE, ");
            stb.append("     DECIMAL(ROUND(FLOAT(SDIV.AVG)*10,0)/10,5,1) AS AVG, ");
            if ("1".equals(param._outputKijun)) {
            	stb.append("     SDIV.CLASS_RANK ");
            } else {
            	stb.append("     SDIV.CLASS_AVG_RANK AS CLASS_RANK ");
            }
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_SDIV_DAT SDIV ");
            stb.append(" WHERE ");
            stb.append("         SDIV.YEAR     = '" + param._ctrlYear + "' ");
            stb.append("     AND SDIV.SCHREGNO = ? ");
            stb.append(" ORDER BY ");
            stb.append("     SDIV.SEMESTER, ");
            stb.append("     SDIV.TESTKINDCD || SDIV.TESTITEMCD || SDIV.SCORE_DIV ");

            return stb.toString();
        }

        /** 出欠情報 */
        private static void loadAttend(final DB2UDB db2, final List students, final Param param) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            param._attendParamMap.put("schregno", "?");

            try {
                for (int i = 0; i < TESTKINDS.length; i++) {
                    final String semTestKindCd = StringUtils.split(TESTKINDS[i], ":")[0];
                    if (semTestKindCd.compareTo(param._testKindCd) > 0) break;
                    final String setSem = semTestKindCd.substring(0, 1);
                    final SemeDtailData semD = (SemeDtailData) param._semDetailMap.get(semTestKindCd);
                    if (null == semD || null == semD._sDate || null == semD._eDate) {
                    	continue;
                    }
                    String sql;
                    sql = AttendAccumulate.getAttendSemesSql(
                            param._ctrlYear,
                            setSem,
                            semD._sDate,
                            semD._eDate,
                            param._attendParamMap
                    );

                    //log.debug(" attend semes sql = " + sql);
                    ps = db2.prepareStatement(sql);

                    for (final Iterator stit = students.iterator(); stit.hasNext();) {
                        final Student student = (Student) stit.next();

                        ps.setString(1, student._schregno);
                        rs = ps.executeQuery();
                        while (rs.next()) {
                            final int lesson    = rs.getInt("LESSON");
                            final int mourning  = rs.getInt("MOURNING");
                            final int suspend   = rs.getInt("SUSPEND");
                            final int koudome   = rs.getInt("KOUDOME");
                            final int virus     = rs.getInt("VIRUS");
                            final int abroad    = rs.getInt("TRANSFER_DATE");
                            final int mlesson   = rs.getInt("MLESSON");
                            final int absence   = rs.getInt("SICK");
                            final int attend    = rs.getInt("PRESENT");
                            final int late      = rs.getInt("LATE");
                            final int early     = rs.getInt("EARLY");

                            final Attendance attendance = new Attendance(lesson, mourning, suspend, koudome, virus, abroad, mlesson, absence, attend, late, early);
                            // log.debug("   schregno = " + student._schregno + " , attendance = " + attendance);
                            student.setAttendance(semTestKindCd, attendance);
                        }
                        DbUtils.closeQuietly(rs);
                    }
                }

            } catch (SQLException e) {
                log.error("sql exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        public void setAttendance(final String semTestCd, final Attendance a) {
            _attendMap.put(semTestCd, a);
        }
    }

    private static String nullToAlt(final String str, final String alt) {
        return StringUtils.defaultString(str, alt);
    }

    /**
     * 教科名
     * @param subclassCnt 　科目数
     * @param subclassOrder 科目順序
     * @param abbvName 　 教科名
     * @param abbvLen 　　 教科名(バイト数)
     * @param abbvStrCnt 　教科名(文字数)
     * @return
     */
    private static ClassAbbvFieldSet setClassAbbv(final int subclassCnt, final int subclassOrder, final String abbvName, final int abbvLen, final int abbvStrCnt) {
        String fieldNum  = "";
        String setString = "";
        ClassAbbvFieldSet retData = new ClassAbbvFieldSet(fieldNum, setString);
        try {
            if (0 != subclassCnt && 0 != subclassOrder && null != abbvName && 0 != abbvLen && 0 != abbvStrCnt) {
                if (1 == subclassCnt) {
                    fieldNum  = (abbvLen > 2) ? "2": "1";
                    setString = abbvName;
                } else if (2 == subclassCnt) {
                    if (abbvStrCnt > 2) {
                        fieldNum = "3";
                        setString = (1 == subclassOrder) ? abbvName.substring(0, 2): abbvName.substring(2) + "　";
                    } else {
                        fieldNum = "1";
                        setString = (1 == subclassOrder) ? abbvName.substring(0, 1): (2 == abbvStrCnt) ? abbvName.substring(1): "" + "　";
                    }
                } else if (3 == subclassCnt) {
                    if (abbvStrCnt > 3) {
                        fieldNum = "3";
                        if (5 == abbvStrCnt) {
                            setString = (1 == subclassOrder) ? abbvName.substring(0, 2): (2 == subclassOrder) ? abbvName.substring(2, 4): abbvName.substring(4) + "　";
                        } else {
                            setString = (1 == subclassOrder) ? "　" + abbvName.substring(0, 1): (2 == subclassOrder) ? abbvName.substring(1, 3): abbvName.substring(3) + "　";
                        }
                    } else {
                        fieldNum = "1";
                        if (1 == abbvStrCnt) {
                            setString = (2 == subclassOrder) ? abbvName.substring(0, 1): "";
                        } else {
                            if (1 == subclassOrder) {
                                setString = abbvName.substring(0, 1);
                            } else if (2 == subclassOrder) {
                                setString = abbvName.substring(1, 2);
                            } else {
                                setString = (3 == abbvStrCnt) ? abbvName.substring(2): "";
                            }
                        }
                    }
                } else if (4 == subclassCnt) {
                    if (abbvStrCnt > 4) {
                        fieldNum = "3";
                        setString = (1 == subclassOrder) ? "　" + abbvName.substring(0, 1): (2 == subclassOrder) ? abbvName.substring(1, 3): (3 == subclassOrder) ? abbvName.substring(3): "";
                    } else {
                        fieldNum = "1";
                        if (1 == abbvStrCnt) {
                            setString = (2 == subclassOrder) ? abbvName.substring(0, 1): "";
                        } else if (2 == abbvStrCnt) {
                            setString = (2 == subclassOrder) ? abbvName.substring(0, 1): (3 == subclassOrder) ? abbvName.substring(1): "";
                        } else {
                            if (1 == subclassOrder) {
                                setString = abbvName.substring(0, 1);
                            } else if (2 == subclassOrder) {
                                setString = abbvName.substring(1, 2);
                            } else if (3 == subclassOrder) {
                                setString = abbvName.substring(2, 3);
                            } else {
                                setString = (4 == abbvStrCnt) ? abbvName.substring(3): "";
                            }
                        }
                    }
                }
                if (subclassCnt > 4) {
                    fieldNum = "1";
                    final boolean oddNumber = subclassCnt % 2 == 1;
                    final int halfNo        = subclassCnt / 2;
                    int textStartNo;
                    int textFinishNo;

                    //科目数が奇数の時
                    if (oddNumber) {
                        if (5 == abbvStrCnt) {
                            textStartNo  = halfNo - 1;
                            textFinishNo = halfNo + 3;
                        } else if (4 == abbvStrCnt) {
                            textStartNo  = halfNo - 1;
                            textFinishNo = halfNo + 2;
                        } else if (3 == abbvStrCnt) {
                            textStartNo  = halfNo;
                            textFinishNo = halfNo + 2;
                        } else if (2 == abbvStrCnt) {
                            textStartNo  = halfNo;
                            textFinishNo = halfNo + 1;
                        } else {
                            textStartNo  = halfNo + 1;
                            textFinishNo = halfNo + 1;
                        }

                    // 科目数が偶数の時
                    } else {
                        if (5 == abbvStrCnt) {
                            textStartNo  = halfNo - 2;
                            textFinishNo = halfNo + 2;
                        } else if (4 == abbvStrCnt) {
                            textStartNo  = halfNo - 1;
                            textFinishNo = halfNo + 2;
                        } else if (3 == abbvStrCnt) {
                            textStartNo  = halfNo - 1;
                            textFinishNo = halfNo + 1;
                        } else if (2 == abbvStrCnt) {
                            textStartNo  = halfNo;
                            textFinishNo = halfNo + 1;
                        } else {
                            textStartNo  = halfNo;
                            textFinishNo = halfNo;
                        }
                    }

                    if (subclassOrder < textStartNo || textFinishNo < subclassOrder) {
                        setString = "";
                    } else {
                        for (int i = 0; i < abbvStrCnt; i++) {
                            final int targetNo = i + textStartNo;
                            if (targetNo != subclassOrder) continue;
                            setString = abbvName.substring(i, i + 1);
                        }
                    }
                }
            }
            retData = new ClassAbbvFieldSet(fieldNum, setString);
        } catch (final Exception ex) {
            log.error("classAbbvSetError!!", ex);
        }

        return retData;
    }

    private static class ClassAbbvFieldSet {
        final String _fieldNumer;
        final String _setAbbv;
        public ClassAbbvFieldSet(
                final String fieldNumer,
                final String setAbbv
        ) {
            _fieldNumer = fieldNumer;
            _setAbbv = setAbbv;
        }
    }

    /**
     * 1日出欠データ
     */
    private static class Attendance {
        final int _lesson;
        /** 忌引 */
        final int _mourning;
        /** 出停 */
        final int _suspend;
        /** 交止 */
        final int _koudome;
        /** 出停伝染病 */
        final int _virus;
        /** 留学 */
        final int _abroad;
        /** 出席すべき日数 */
        final int _mlesson;
        /** 欠席 */
        final int _absence;
        /** 出席 */
        final int _attend;
        /** 遅刻 */
        final int _late;
        /** 早退 */
        final int _leave;

        public Attendance() {
            this(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        }

        public Attendance(
                final int lesson,
                final int mourning,
                final int suspend,
                final int koudome,
                final int virus,
                final int abroad,
                final int mlesson,
                final int absence,
                final int attend,
                final int late,
                final int leave
        ) {
            _lesson = lesson;
            _mourning = mourning;
            _suspend = suspend;
            _koudome = koudome;
            _virus = virus;
            _abroad = abroad;
            _mlesson = mlesson;
            _absence = absence;
            _attend = attend;
            _late = late;
            _leave = leave;
        }

        public Attendance add(final Attendance att) {
        	return new Attendance(
        			_lesson + att._lesson,
        			_mourning + att._mourning,
        			_suspend + att._suspend,
        			_koudome + att._koudome,
        			_virus + att._virus,
        			_abroad + att._abroad,
        			_mlesson + att._mlesson,
        			_absence + att._absence,
        			_attend + att._attend,
        			_late + att._late,
        			_leave + att._leave
        			);
        }
    }

    private static class ScoreRank {
        final String _score;
        final String _classRank;
        final String _avg;

        public ScoreRank(
                final String score,
                final String classRank,
                final String avg
                ) {
            _score      = score;
            _classRank  = classRank;
            _avg   = avg;
        }
    }

    private static class SubclassInfo {
        private final String _className;
        private final String _subClassName;
        private final String _classCd;
        private final String _subclassCd;
        public SubclassInfo(
                final String className,
                final String subClassName,
                final String classCd,
                final String subclassCd
                ) {
            _className    = className;
            _subClassName = subClassName;
            _classCd      = classCd;
            _subclassCd   = subclassCd;
        }
    }

    private class SemeDtailData {
        private final String _sDate;
        private final String _eDate;
        public SemeDtailData(
                final String sDate,
                final String eDate
                ) {
            _sDate    = sDate;
            _eDate    = eDate;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 71572 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _semester;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _isClass;
        private final String _grade;
        private final String _testKindCd;
        private final String _outputKijun;
        private final String _useCurriculumcd;
        private final String _useClassDetailDat;
        private final String _useVirus;
        private final String _useKoudome;

        private Map _semDetailMap = new TreeMap();

        final String[] _categorySelected;

        final String _certifScholDatSchoolName;
        final List _d046List;

        final Map _attendParamMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _semester           = request.getParameter("SEMESTER");
            _ctrlYear           = request.getParameter("CTRL_YEAR");
            _ctrlSemester       = request.getParameter("CTRL_SEMESTER");
            _ctrlDate           = request.getParameter("CTRL_DATE");
            _isClass            = request.getParameter("CATEGORY_IS_CLASS");
            _grade              = request.getParameter("GRADE");
            _testKindCd         = request.getParameter("TESTKIND_CD");
            _outputKijun        = request.getParameter("OUTPUT_KIJUN");
            _useCurriculumcd    = request.getParameter("useCurriculumcd");
            _useClassDetailDat  = request.getParameter("useClassDetailDat");
            _useVirus           = request.getParameter("useVirus");
            _useKoudome         = request.getParameter("useKoudome");

            _semDetailMap       = getSemesterDetailMst(db2);

            _categorySelected   = request.getParameterValues("CATEGORY_SELECTED");

            _certifScholDatSchoolName = getCertifSchoolDat(db2, "SCHOOL_NAME");
            _d046List = loadNameMstD046(db2);

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("useCurriculumcd", _useCurriculumcd);
            _attendParamMap.put("useVirus", _useVirus);
            _attendParamMap.put("useKoudome", _useKoudome);
        }

        private Map getSemesterDetailMst(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT DISTINCT ");
            sql.append("     SDIV.SEMESTER, ");
            sql.append("     SDIV.TESTKINDCD || SDIV.TESTITEMCD || SDIV.SCORE_DIV AS TESTKINDCD, ");
            sql.append("     SEME.SDATE, ");
            sql.append("     SEME.EDATE ");
            sql.append(" FROM ");
            sql.append("     TESTITEM_MST_COUNTFLG_NEW_SDIV SDIV ");
            sql.append("     LEFT JOIN SEMESTER_DETAIL_MST SEME ON SDIV.YEAR     = SEME.YEAR ");
            sql.append("                                       AND SDIV.SEMESTER = SEME.SEMESTER ");
            sql.append("                                       AND SDIV.SEMESTER_DETAIL = SEME.SEMESTER_DETAIL ");
            sql.append(" WHERE ");
            sql.append("     SDIV.YEAR = '" + _ctrlYear + "' ");

            log.debug("TESTITEM_MST_COUNTFLG_NEW_SDIV sql = " + sql.toString());
            PreparedStatement ps = null;
            ResultSet rs = null;
            Map rtList = new TreeMap();
            String[] semTestList = new String[6];
            for (int i = 0; i < TESTKINDS.length; i++) {
                final String semTestKindCd = StringUtils.split(TESTKINDS[i], ":")[0];
                semTestList[i] = semTestKindCd;
            }
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester   = rs.getString("SEMESTER");
                    final String testKindCd = rs.getString("TESTKINDCD");
                    final String sDate      = rs.getString("SDATE");
                    final String eDate      = rs.getString("EDATE");

                    if (ArrayUtils.contains(semTestList, semester + testKindCd)){
                        final SemeDtailData sdd = new SemeDtailData(sDate, eDate);
                        rtList.put(semester + testKindCd, sdd);
                    }
                }
            } catch (Exception ex) {
                log.error("error:testitem_mst_countflg_new_sdivSQL!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtList;
        }

        private String getCertifSchoolDat(final DB2UDB db2, final String field) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT " + field + " FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _ctrlYear + "' AND CERTIF_KINDCD = '109' ");
            //log.debug("certif_school_dat sql = " + sql.toString());
            PreparedStatement ps = null;
            ResultSet rs = null;
            String rtn = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private List loadNameMstD046(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            final String field = SEMESTER9.equals(_semester) ? "NAMESPARE1" : "ABBV" + _semester;
            sql.append(" SELECT NAME1 AS SUBCLASSCD FROM V_NAME_MST ");
            sql.append(" WHERE YEAR = '" + _ctrlYear + "' AND NAMECD1 = 'D046' AND " + field + " = '1'  ");
            return KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, sql.toString()), "SUBCLASSCD");
        }
    }
}

// eof
