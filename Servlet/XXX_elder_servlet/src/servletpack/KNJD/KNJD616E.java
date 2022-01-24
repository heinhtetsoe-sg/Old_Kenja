/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: 83b8784fa5c38689af01ba356eeb171644a382ef $
 *
 * 作成日: 2019/08/27
 * 作成者: yamashiro
 *
 * Copyright(C) 2019-2021 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJD616E {

    private static final Log log = LogFactory.getLog(KNJD616E.class);

    private boolean _hasData;

    private final String SUBCLASS_ALL = "999999";
    private final String GOUKEI = "1";
    private final String HEIKIN = "2";

    private final String AVG_CLASS = "2";

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
        final List hrClassList = getHrList(db2);
        final String subclassAll = "99-" + _param._schoolKind + "-99-" + SUBCLASS_ALL;
        for (Iterator itHr = hrClassList.iterator(); itHr.hasNext();) {
            svf.VrSetForm("KNJD616E.frm", 4);
            final HrClass hrClass = (HrClass) itHr.next();
            svf.VrsOut("year2", KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_param._ctrlYear)) + "年度");
            svf.VrsOut("TITLE", _param._semesterName + "　" + _param._tesName + "成績一覧表");
            svf.VrsOut("ymd1", KNJ_EditDate.getAutoFormatDate(db2, _param._ctrlDate));
            final String staffField = KNJ_EditEdit.getMS932ByteLength(hrClass._staffName) > 30 ? "2" : "";
            svf.VrsOut("HR_TEACHER" + staffField, hrClass._staffName);
            svf.VrsOut("HR_NAME", hrClass._hrName);
            int stdCnt = 1;
            BigDecimal totalAvg = new BigDecimal(0);
            int avgCnt = 0;
            for (Iterator itStudent = hrClass._studentList.iterator(); itStudent.hasNext();) {
                final Student student = (Student) itStudent.next();

                svf.VrsOutn("NUMBER", stdCnt, student._attendno);
                svf.VrsOutn("name1", stdCnt, student._name);

                if (student._recordRank.containsKey(subclassAll)) {
                    final RecordRank recordRank = (RecordRank) student._recordRank.get(subclassAll);
                    BigDecimal setAvg = new BigDecimal(recordRank._avg).setScale(1, BigDecimal.ROUND_HALF_UP);
                    totalAvg = totalAvg.add(setAvg);
                    avgCnt++;
                    svf.VrsOutn("AVERAGE1", stdCnt, setAvg.toString());
                    svf.VrsOutn("TOTAL1", stdCnt, recordRank._score);
                    if (GOUKEI.equals(_param._rankBaseScore)) {
                        svf.VrsOutn("CLASS_RANK1", stdCnt, recordRank._courseGroupClRank);
                        svf.VrsOutn("RANK1", stdCnt, recordRank._courseGroupRank);
                    } else {
                        svf.VrsOutn("CLASS_RANK1", stdCnt, recordRank._courseGroupClAvgRank);
                        svf.VrsOutn("RANK1", stdCnt, recordRank._courseGroupAvgRank);
                    }
                }
                stdCnt++;
            }
            //平均の最終
            if (hrClass._recordAvgSdivMap.containsKey(subclassAll)) {
                final RecordAvgSdiv recordAvgSdiv = (RecordAvgSdiv) hrClass._recordAvgSdivMap.get(subclassAll);
                if (null != recordAvgSdiv._highScoreAvg) {
                	BigDecimal setHighAvg = new BigDecimal(recordAvgSdiv._highScoreAvg).setScale(1, BigDecimal.ROUND_HALF_UP);
                	svf.VrsOutn("AVERAGE1", 46, setHighAvg.toString());
                }
                if (null != recordAvgSdiv._lowScoreAvg) {
                	BigDecimal setLowAvg = new BigDecimal(recordAvgSdiv._lowScoreAvg).setScale(1, BigDecimal.ROUND_HALF_UP);
                	svf.VrsOutn("AVERAGE1", 47, setLowAvg.toString());
                }
                if (0 != avgCnt) {
                	final BigDecimal setVal = totalAvg.divide(new BigDecimal(avgCnt), 1, BigDecimal.ROUND_HALF_UP);
                	svf.VrsOutn("AVERAGE1", 48, setVal.toString());
                }
            }

            final List printSubclassList = new ArrayList(hrClass._subclassMap.values());
            Collections.sort(printSubclassList);
            final Map printClassCdMap = new HashMap<String, String>();
            for (final Iterator it = printSubclassList.iterator(); it.hasNext();) {
                final SubclassMst subclass = (SubclassMst) it.next();
                int classAbbvLen = KNJ_EditEdit.getMS932ByteLength(subclass._classAbbv);
                final String classField = classAbbvLen > 6 ? "3" : classAbbvLen > 4 ? "2" : "1";
                svf.VrsOut("GRPCD", subclass._classcd);
                if (!printClassCdMap.containsKey(subclass._classcd)) {
                    svf.VrsOut("course" + classField, subclass._classAbbv);
                }
                if (subclass._subclassname.length() > 9) {
                    final String[] split = KNJ_EditEdit.splitByLength(subclass._subclassname, 10);
                    final String[] subNameField = {"SUBCLASS_1", "SUBCLASS_2"};
                    for (int subNameCnt = 0; subNameCnt < Math.min(split.length, subNameField.length); subNameCnt++) {
                        svf.VrsOut(subNameField[subNameCnt], split[subNameCnt]);
                    }
                } else {
                    svf.VrsOut("SUBCLASS", subclass._subclassname);
                }
                svf.VrsOut("credit1", subclass._dispCredit);
                int gyo = 1;
                for (Iterator itstudent = hrClass._studentList.iterator(); itstudent.hasNext();) {
                    final Student student = (Student) itstudent.next();
                    if (student._recordRank.containsKey(subclass._joinSubclassCd)) {
                        final RecordRank recordRank = (RecordRank) student._recordRank.get(subclass._joinSubclassCd);
                        svf.VrsOut("SCORE" + gyo, recordRank._score);
                    }
                    gyo++;
                }
                if (hrClass._recordAvgSdivMap.containsKey(subclass._joinSubclassCd)) {
                    final RecordAvgSdiv recordAvgSdiv = (RecordAvgSdiv) hrClass._recordAvgSdivMap.get(subclass._joinSubclassCd);
                    svf.VrsOut("MAX", recordAvgSdiv._highScore);
                    svf.VrsOut("MIN", recordAvgSdiv._lowScore);
                    svf.VrsOut("AVE", recordAvgSdiv._avg);
                }
                svf.VrEndRecord();
                printClassCdMap.put(subclass._classcd, "1");
            }
            for (int i = printSubclassList.size(); i < 55; i++) {
                svf.VrsOut("GRPCD", "a");
                svf.VrEndRecord();
            }
            _hasData = true;
        }
    }

    private List getHrList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String hrClassSql = getHrClassSql();
            log.debug(" sql =" + hrClassSql);
            ps = db2.prepareStatement(hrClassSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String grade = rs.getString("GRADE");
                final String hrClass = rs.getString("HR_CLASS");
                final String hrName = rs.getString("HR_NAME");
                final String staffName = rs.getString("STAFFNAME");

                final HrClass hrClassObj = new HrClass(grade, hrClass, hrName, staffName);
                hrClassObj.setStudent(db2);
                hrClassObj.setSubclassMst(db2);
                hrClassObj.setRecordAvgSdiv(db2);
                retList.add(hrClassObj);
            }

        } catch (SQLException ex) {
            log.fatal("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getHrClassSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     REGDH.GRADE, ");
        stb.append("     REGDH.HR_CLASS, ");
        stb.append("     REGDH.HR_NAME, ");
        stb.append("     STAFF.STAFFNAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_HDAT REGDH ");
        stb.append("     LEFT JOIN STAFF_MST STAFF ON REGDH.TR_CD1 = STAFF.STAFFCD ");
        stb.append(" WHERE ");
        stb.append("     REGDH.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND REGDH.SEMESTER = '" + _param._regdSeme + "' ");
        stb.append("     AND REGDH.GRADE || '-' || REGDH.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
        stb.append(" ORDER BY ");
        stb.append("     REGDH.GRADE, ");
        stb.append("     REGDH.HR_CLASS ");

        return stb.toString();
    }

    private class HrClass {
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _staffName;
        final List _studentList;
        final Map _subclassMap;
        final Map _recordAvgSdivMap;
        public HrClass(
                final String grade,
                final String hrClass,
                final String hrName,
                final String staffName
        ) {
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _staffName = staffName;
            _studentList = new ArrayList<Student>();
            _subclassMap = new TreeMap<String, SubclassMst>();
            _recordAvgSdivMap = new TreeMap<String, RecordAvgSdiv>();
        }

        private void setStudent(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String studentSql = getStudentSql();
                log.debug(" sql =" + studentSql);
                ps = db2.prepareStatement(studentSql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String schregno = rs.getString("SCHREGNO");
                    final String attendno = rs.getString("ATTENDNO");
                    final String name = rs.getString("NAME");
                    final String major = rs.getString("MAJOR");
                    final String joinCourse = rs.getString("JOIN_COURSE");
                    final String courseCode = rs.getString("COURSECODE");
                    final String groupCd = rs.getString("GROUP_CD");

                    final Student student = new Student(schregno, attendno, name, major, joinCourse, courseCode, groupCd);
                    student.setRecordRank(db2);
                    _studentList.add(student);
                }

            } catch (SQLException ex) {
                log.fatal("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getStudentSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     REGD.SCHREGNO, ");
            stb.append("     REGD.ATTENDNO, ");
            stb.append("     BASE.NAME, ");
            stb.append("     REGD.COURSECD || REGD.MAJORCD AS MAJOR, ");
            stb.append("     REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE AS JOIN_COURSE, ");
            stb.append("     REGD.COURSECODE, ");
            stb.append("     G1.GROUP_CD ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT REGD ");
            stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     LEFT JOIN COURSE_GROUP_CD_DAT G1 ON G1.YEAR = REGD.YEAR ");
            stb.append("          AND G1.GRADE = REGD.GRADE ");
            stb.append("          AND G1.COURSECD = REGD.COURSECD ");
            stb.append("          AND G1.MAJORCD = REGD.MAJORCD ");
            stb.append("          AND G1.COURSECODE = REGD.COURSECODE ");
            stb.append(" WHERE ");
            stb.append("     REGD.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND REGD.SEMESTER = '" + _param._regdSeme + "' ");
            stb.append("     AND REGD.GRADE = '" + _grade + "' ");
            stb.append("     AND REGD.HR_CLASS = " + _hrClass + " ");
            stb.append(" ORDER BY ");
            stb.append("     REGD.GRADE, ");
            stb.append("     REGD.HR_CLASS, ");
            stb.append("     REGD.ATTENDNO ");

            return stb.toString();
        }

        private void setSubclassMst(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCH_T AS ( ");
            stb.append(getStudentSql());
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     REC_RANK.CLASSCD, ");
            stb.append("     CLASS_M.CLASSABBV, ");
            stb.append("     REC_RANK.SCHOOL_KIND, ");
            stb.append("     REC_RANK.CURRICULUM_CD, ");
            stb.append("     REC_RANK.SUBCLASSCD, ");
            stb.append("     REC_RANK.CLASSCD || '-' || REC_RANK.SCHOOL_KIND || '-' || REC_RANK.CURRICULUM_CD || '-' || REC_RANK.SUBCLASSCD AS JOIN_SUBCLASSCD, ");
            stb.append("     VALUE(SUB_M.ELECTDIV, '0') AS ELECTDIV, ");
            stb.append("     SUB_M.SUBCLASSNAME, ");
            stb.append("     MAX(VALUE(CREDIT.CREDITS, 0)) AS MAX_CREDITS, ");
            stb.append("     MIN(VALUE(CREDIT.CREDITS, 0)) AS MIN_CREDITS ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_SDIV_DAT REC_RANK ");
            stb.append("     INNER JOIN CLASS_MST CLASS_M ON REC_RANK.CLASSCD = CLASS_M.CLASSCD ");
            stb.append("           AND REC_RANK.SCHOOL_KIND = CLASS_M.SCHOOL_KIND ");
            stb.append("     INNER JOIN SUBCLASS_MST SUB_M ON REC_RANK.CLASSCD = SUB_M.CLASSCD ");
            stb.append("           AND REC_RANK.SCHOOL_KIND = SUB_M.SCHOOL_KIND ");
            stb.append("           AND REC_RANK.CURRICULUM_CD = SUB_M.CURRICULUM_CD ");
            stb.append("           AND REC_RANK.SUBCLASSCD = SUB_M.SUBCLASSCD ");
            stb.append("     INNER JOIN SCH_T ON REC_RANK.SCHREGNO = SCH_T.SCHREGNO ");
            stb.append("     LEFT JOIN CREDIT_MST CREDIT ON REC_RANK.YEAR = CREDIT.YEAR ");
            stb.append("          AND SCH_T.JOIN_COURSE = CREDIT.COURSECD || CREDIT.MAJORCD || CREDIT.COURSECODE ");
            stb.append("          AND CREDIT.GRADE = '" + _grade + "' ");
            stb.append("          AND REC_RANK.CLASSCD = CREDIT.CLASSCD ");
            stb.append("          AND REC_RANK.SCHOOL_KIND = CREDIT.SCHOOL_KIND ");
            stb.append("          AND REC_RANK.CURRICULUM_CD = CREDIT.CURRICULUM_CD ");
            stb.append("          AND REC_RANK.SUBCLASSCD = CREDIT.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     REC_RANK.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND REC_RANK.SEMESTER = '" + _param._semester + "' ");
            stb.append("     AND REC_RANK.TESTKINDCD || REC_RANK.TESTITEMCD || REC_RANK.SCORE_DIV = '" + _param._testkindcd + "' ");
            stb.append("     AND REC_RANK.SCHOOL_KIND = '" + _param._schoolKind + "' ");
            stb.append(" GROUP BY ");
            stb.append("     REC_RANK.CLASSCD, ");
            stb.append("     CLASS_M.CLASSABBV, ");
            stb.append("     REC_RANK.SCHOOL_KIND, ");
            stb.append("     REC_RANK.CURRICULUM_CD, ");
            stb.append("     REC_RANK.SUBCLASSCD, ");
            stb.append("     SUB_M.ELECTDIV, ");
            stb.append("     SUB_M.SUBCLASSNAME ");
            stb.append(" ORDER BY ");
            stb.append("     VALUE(SUB_M.ELECTDIV, '0'), ");
            stb.append("     REC_RANK.CLASSCD, ");
            stb.append("     REC_RANK.SCHOOL_KIND, ");
            stb.append("     REC_RANK.CURRICULUM_CD, ");
            stb.append("     REC_RANK.SUBCLASSCD ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String classcd = rs.getString("CLASSCD");
                    final String classAbbv = rs.getString("CLASSABBV");
                    final String schoolKind = rs.getString("SCHOOL_KIND");
                    final String curriculumCd = rs.getString("CURRICULUM_CD");
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final String joinSubclassCd = rs.getString("JOIN_SUBCLASSCD");
                    final String electdiv = rs.getString("ELECTDIV");
                    final String subclassname = rs.getString("SUBCLASSNAME");
                    final String maxCredit = StringUtils.defaultString(rs.getString("MAX_CREDITS"));
                    final String minCredit = StringUtils.defaultString(rs.getString("MIN_CREDITS"));
                    final SubclassMst subclassMst = new SubclassMst(classcd, classAbbv, schoolKind, curriculumCd, subclassCd, joinSubclassCd, electdiv, subclassname, maxCredit, minCredit);
                    _subclassMap.put(joinSubclassCd, subclassMst);
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private void setRecordAvgSdiv(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS JOIN_SUBCLASSCD, ");
            stb.append("     SCORE, ");
            stb.append("     HIGHSCORE, ");
            stb.append("     LOWSCORE, ");
            stb.append("     COUNT, ");
            stb.append("     AVG, ");
            stb.append("     STDDEV, ");
            stb.append("     HIGHSCORE_AVG, ");
            stb.append("     LOWSCORE_AVG ");
            stb.append(" FROM ");
            stb.append("     RECORD_AVERAGE_SDIV_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND SEMESTER = '" + _param._semester + "' ");
            stb.append("     AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + _param._testkindcd + "' ");
            stb.append("     AND AVG_DIV = '" + AVG_CLASS + "' ");
            stb.append("     AND GRADE = '" + _grade + "' ");
            stb.append("     AND HR_CLASS = '" + _hrClass + "' ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String joinSubclassCd = rs.getString("JOIN_SUBCLASSCD");
                    final String score = rs.getString("SCORE");
                    final String highScore = rs.getString("HIGHSCORE");
                    final String lowScore = rs.getString("LOWSCORE");
                    final String count = rs.getString("COUNT");
                    final String avg = rs.getString("AVG");
                    final String stddev = rs.getString("STDDEV");
                    final String highScoreAvg = rs.getString("HIGHSCORE_AVG");
                    final String lowScoreAvg = rs.getString("LOWSCORE_AVG");
                    final RecordAvgSdiv recordAvgSdiv = new RecordAvgSdiv(joinSubclassCd, score, highScore, lowScore, count, avg, stddev, highScoreAvg, lowScoreAvg);
                    _recordAvgSdivMap.put(joinSubclassCd, recordAvgSdiv);
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
    }

    private class Student {
        final String _schregno;
        final String _attendno;
        final String _name;
        final String _major;
        final String _joinCourse;
        final String _courseCode;
        final String _groupCd;
        final Map _recordRank;
        public Student(
                final String schregno,
                final String attendno,
                final String name,
                final String major,
                final String joinCourse,
                final String courseCode,
                final String groupCd
        ) {
            _schregno = schregno;
            _attendno = attendno;
            _name = name;
            _major = major;
            _joinCourse = joinCourse;
            _courseCode = courseCode;
            _groupCd = groupCd;
            _recordRank = new HashMap<String, RecordRank>();
        }

        private void setRecordRank(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS JOIN_SUBCLASSCD, ");
            stb.append("     T1.SCORE, ");
            stb.append("     T1.AVG, ");
            stb.append("     T1.GRADE_RANK, ");
            stb.append("     T1.GRADE_AVG_RANK, ");
            stb.append("     T1.GRADE_DEVIATION, ");
            stb.append("     T1.GRADE_DEVIATION_RANK, ");
            stb.append("     T1.CLASS_RANK, ");
            stb.append("     T1.CLASS_AVG_RANK, ");
            stb.append("     T1.CLASS_DEVIATION, ");
            stb.append("     T1.CLASS_DEVIATION_RANK, ");
            stb.append("     T1.COURSE_RANK, ");
            stb.append("     T1.COURSE_AVG_RANK, ");
            stb.append("     T1.COURSE_DEVIATION, ");
            stb.append("     T1.COURSE_DEVIATION_RANK, ");
            stb.append("     T1.MAJOR_RANK, ");
            stb.append("     T1.MAJOR_AVG_RANK, ");
            stb.append("     T1.MAJOR_DEVIATION, ");
            stb.append("     T1.MAJOR_DEVIATION_RANK, ");
            stb.append("     T1.COURSE_GROUP_RANK, ");
            stb.append("     T1.COURSE_GROUP_AVG_RANK, ");
            stb.append("     T1.COURSE_GROUP_DEVIATION, ");
            stb.append("     T1.COURSE_GROUP_DEVIATION_RANK, ");
            stb.append("     T1.CHAIR_GROUP_RANK, ");
            stb.append("     T1.CHAIR_GROUP_AVG_RANK, ");
            stb.append("     T1.CHAIR_GROUP_DEVIATION, ");
            stb.append("     T1.CHAIR_GROUP_DEVIATION_RANK, ");
            stb.append("     T2.COURSE_GROUP_CL_RANK, ");
            stb.append("     T2.COURSE_GROUP_CL_AVG_RANK, ");
            stb.append("     T2.COURSE_GROUP_CL_DEVIATION, ");
            stb.append("     T2.COURSE_GROUP_CL_DEVIATION_RANK ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_SDIV_DAT T1 ");
            stb.append("     LEFT JOIN V_RECORD_RANK_SDIV_DAT T2 ");
            stb.append("            ON T2.YEAR          = T1.YEAR ");
            stb.append("           AND T2.SEMESTER      = T1.SEMESTER ");
            stb.append("           AND T2.TESTKINDCD    = T1.TESTKINDCD ");
            stb.append("           AND T2.TESTITEMCD    = T1.TESTITEMCD ");
            stb.append("           AND T2.SCORE_DIV     = T1.SCORE_DIV ");
            stb.append("           AND T2.CLASSCD       = T1.CLASSCD ");
            stb.append("           AND T2.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("           AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("           AND T2.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("           AND T2.SCHREGNO      = T1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
            stb.append("     AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + _param._testkindcd + "' ");
            stb.append("     AND T1.SCHREGNO = '" + _schregno + "' ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String joinSubclassCd             = StringUtils.defaultString(rs.getString("JOIN_SUBCLASSCD"));
                    final String score                      = StringUtils.defaultString(rs.getString("SCORE"));
                    final String avg                        = StringUtils.defaultString(rs.getString("AVG"));
                    final String gradeRank                  = StringUtils.defaultString(rs.getString("GRADE_RANK"));
                    final String gradeAvgRank               = StringUtils.defaultString(rs.getString("GRADE_AVG_RANK"));
                    final String gradeDeviation             = StringUtils.defaultString(rs.getString("GRADE_DEVIATION"));
                    final String gradeDeviationRank         = StringUtils.defaultString(rs.getString("GRADE_DEVIATION_RANK"));
                    final String classRank                  = StringUtils.defaultString(rs.getString("CLASS_RANK"));
                    final String classAvgRank               = StringUtils.defaultString(rs.getString("CLASS_AVG_RANK"));
                    final String classDeviation             = StringUtils.defaultString(rs.getString("CLASS_DEVIATION"));
                    final String classDeviationRank         = StringUtils.defaultString(rs.getString("CLASS_DEVIATION_RANK"));
                    final String courseRank                 = StringUtils.defaultString(rs.getString("COURSE_RANK"));
                    final String courseAvgRank              = StringUtils.defaultString(rs.getString("COURSE_AVG_RANK"));
                    final String courseDeviation            = StringUtils.defaultString(rs.getString("COURSE_DEVIATION"));
                    final String courseDeviationRank        = StringUtils.defaultString(rs.getString("COURSE_DEVIATION_RANK"));
                    final String majorRank                  = StringUtils.defaultString(rs.getString("MAJOR_RANK"));
                    final String majorAvgRank               = StringUtils.defaultString(rs.getString("MAJOR_AVG_RANK"));
                    final String majorDeviation             = StringUtils.defaultString(rs.getString("MAJOR_DEVIATION"));
                    final String majorDeviationRank         = StringUtils.defaultString(rs.getString("MAJOR_DEVIATION_RANK"));
                    final String courseGroupRank            = StringUtils.defaultString(rs.getString("COURSE_GROUP_RANK"));
                    final String courseGroupAvgRank         = StringUtils.defaultString(rs.getString("COURSE_GROUP_AVG_RANK"));
                    final String courseGroupDeviation       = StringUtils.defaultString(rs.getString("COURSE_GROUP_DEVIATION"));
                    final String courseGroupDeviationRank   = StringUtils.defaultString(rs.getString("COURSE_GROUP_DEVIATION_RANK"));
                    final String chairGroupRank             = StringUtils.defaultString(rs.getString("CHAIR_GROUP_RANK"));
                    final String chairGroupAvgRank          = StringUtils.defaultString(rs.getString("CHAIR_GROUP_AVG_RANK"));
                    final String chairGroupDeviation        = StringUtils.defaultString(rs.getString("CHAIR_GROUP_DEVIATION"));
                    final String chairGroupDeviationRank    = StringUtils.defaultString(rs.getString("CHAIR_GROUP_DEVIATION_RANK"));
                    final String courseGroupClRank          = StringUtils.defaultString(rs.getString("COURSE_GROUP_CL_RANK"));
                    final String courseGroupClAvgRank       = StringUtils.defaultString(rs.getString("COURSE_GROUP_CL_AVG_RANK"));
                    final String courseGroupClDeviation     = StringUtils.defaultString(rs.getString("COURSE_GROUP_CL_DEVIATION"));
                    final String courseGroupClDeviationRank = StringUtils.defaultString(rs.getString("COURSE_GROUP_CL_DEVIATION_RANK"));
                    final RecordRank recordRank = new RecordRank(joinSubclassCd, score, avg,
                            gradeRank, gradeAvgRank, gradeDeviation, gradeDeviationRank,
                            classRank, classAvgRank, classDeviation, classDeviationRank,
                            courseRank, courseAvgRank, courseDeviation, courseDeviationRank,
                            majorRank, majorAvgRank, majorDeviation, majorDeviationRank,
                            courseGroupRank, courseGroupAvgRank, courseGroupDeviation, courseGroupDeviationRank,
                            chairGroupRank, chairGroupAvgRank, chairGroupDeviation, chairGroupDeviationRank,
                            courseGroupClRank, courseGroupClAvgRank, courseGroupClDeviation, courseGroupClDeviationRank);
                    _recordRank.put(joinSubclassCd, recordRank);
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
    }

    private class SubclassMst implements Comparable<SubclassMst> {
        final String _classcd;
        final String _classAbbv;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclasscd;
        final String _joinSubclassCd;
        final String _electdiv;
        final String _subclassname;
        final String _maxCredit;
        final String _minCredit;
        final String _dispCredit;
        public SubclassMst(
                final String classcd,
                final String classAbbv,
                final String schoolKind,
                final String curriculumCd,
                final String subclasscd,
                final String joinSubclassCd,
                final String electdiv,
                final String subclassname,
                final String maxCredit,
                final String minCredit
        ) {
            _classcd = classcd;
            _classAbbv = classAbbv;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclasscd = subclasscd;
            _joinSubclassCd = joinSubclassCd;
            _electdiv = electdiv;
            _subclassname = subclassname;
            _maxCredit = maxCredit;
            _minCredit = minCredit;
            if ("0".equals(_maxCredit) && "0".equals(_minCredit)) {
                _dispCredit = "";
            } else if (_maxCredit.equals(minCredit)) {
                _dispCredit = _maxCredit;
            } else {
                _dispCredit = _minCredit + "\uFF5E" + _maxCredit;
            }
        }
        public int compareTo(final SubclassMst sub) {
            int cmp;
            cmp = _electdiv.compareTo(sub._electdiv);
            if (cmp != 0) {
                return cmp;
            }
            cmp = _joinSubclassCd.compareTo(sub._joinSubclassCd);
            return cmp;
        }
        public String toString() {
            return _joinSubclassCd + ":" + _subclassname;
        }
    }

    private class RecordRank {
        final String _joinSubclassCd;
        final String _score;
        final String _avg;
        final String _gradeRank;
        final String _gradeAvgRank;
        final String _gradeDeviation;
        final String _gradeDeviationRank;
        final String _classRank;
        final String _classAvgRank;
        final String _classDeviation;
        final String _classDeviationRank;
        final String _courseRank;
        final String _courseAvgRank;
        final String _courseDeviation;
        final String _courseDeviationRank;
        final String _majorRank;
        final String _majorAvgRank;
        final String _majorDeviation;
        final String _majorDeviationRank;
        final String _courseGroupRank;
        final String _courseGroupAvgRank;
        final String _courseGroupDeviation;
        final String _courseGroupDeviationRank;
        final String _chairGroupRank;
        final String _chairGroupAvgRank;
        final String _chairGroupDeviation;
        final String _chairGroupDeviationRank;
        final String _courseGroupClRank;
        final String _courseGroupClAvgRank;
        final String _courseGroupClDeviation;
        final String _courseGroupClDeviationRank;
        public RecordRank(
                final String joinSubclassCd,
                final String score,
                final String avg,
                final String gradeRank,
                final String gradeAvgRank,
                final String gradeDeviation,
                final String gradeDeviationRank,
                final String classRank,
                final String classAvgRank,
                final String classDeviation,
                final String classDeviationRank,
                final String courseRank,
                final String courseAvgRank,
                final String courseDeviation,
                final String courseDeviationRank,
                final String majorRank,
                final String majorAvgRank,
                final String majorDeviation,
                final String majorDeviationRank,
                final String courseGroupRank,
                final String courseGroupAvgRank,
                final String courseGroupDeviation,
                final String courseGroupDeviationRank,
                final String chairGroupRank,
                final String chairGroupAvgRank,
                final String chairGroupDeviation,
                final String chairGroupDeviationRank,
                final String courseGroupClRank,
                final String courseGroupClAvgRank,
                final String courseGroupClDeviation,
                final String courseGroupClDeviationRank
        ) {
            _joinSubclassCd = joinSubclassCd;
            _score = score;
            _avg = avg;
            _gradeRank = gradeRank;
            _gradeAvgRank = gradeAvgRank;
            _gradeDeviation = gradeDeviation;
            _gradeDeviationRank = gradeDeviationRank;
            _classRank = classRank;
            _classAvgRank = classAvgRank;
            _classDeviation = classDeviation;
            _classDeviationRank = classDeviationRank;
            _courseRank = courseRank;
            _courseAvgRank = courseAvgRank;
            _courseDeviation = courseDeviation;
            _courseDeviationRank = courseDeviationRank;
            _majorRank = majorRank;
            _majorAvgRank = majorAvgRank;
            _majorDeviation = majorDeviation;
            _majorDeviationRank = majorDeviationRank;
            _courseGroupRank = courseGroupRank;
            _courseGroupAvgRank = courseGroupAvgRank;
            _courseGroupDeviation = courseGroupDeviation;
            _courseGroupDeviationRank = courseGroupDeviationRank;
            _chairGroupRank = chairGroupRank;
            _chairGroupAvgRank = chairGroupAvgRank;
            _chairGroupDeviation = chairGroupDeviation;
            _chairGroupDeviationRank = chairGroupDeviationRank;
            _courseGroupClRank = courseGroupClRank;
            _courseGroupClAvgRank = courseGroupClAvgRank;
            _courseGroupClDeviation = courseGroupClDeviation;
            _courseGroupClDeviationRank = courseGroupClDeviationRank;
        }
    }

    private class RecordAvgSdiv {
        final String _joinSubclassCd;
        final String _score;
        final String _highScore;
        final String _lowScore;
        final String _count;
        final String _avg;
        final String _stddev;
        final String _highScoreAvg;
        final String _lowScoreAvg;
        public RecordAvgSdiv(
                final String joinSubclassCd,
                final String score,
                final String highScore,
                final String lowScore,
                final String count,
                final String avg,
                final String stddev,
                final String highScoreAvg,
                final String lowScoreAvg
        ) {
            _joinSubclassCd = joinSubclassCd;
            _score = score;
            _highScore = highScore;
            _lowScore = lowScore;
            _count = count;
            _avg = avg;
            _stddev = stddev;
            _highScoreAvg = highScoreAvg;
            _lowScoreAvg = lowScoreAvg;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 74544 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _ctrlDate;
        final String _ctrlSemester;
        final String _ctrlYear;
        final String _grade;
        final String _schoolKind;
        final String _printLogStaffcd;
        final String _rankBaseScore;
        final String _semester;
        final String _semesterName;
        final String _regdSeme;
        final String _testkindcd;
        final String _tesName;
        final String[] _categorySelected;
        final String _checkkettendiv;
        final String _usecurriculumcd;
        final String _usetestcountflg;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlDate = request.getParameter("CTRL_DATE");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _grade = request.getParameter("GRADE");
            _schoolKind = request.getParameter("SCHOOL_KIND");
            _printLogStaffcd = request.getParameter("PRINT_LOG_STAFFCD");
            _rankBaseScore = request.getParameter("RANK_BASE_SCORE");
            _semester = request.getParameter("SEMESTER");
            _semesterName = getSemesterName(db2);
            _regdSeme = "9".equals(_semester) ? _ctrlSemester : _semester;
            _testkindcd = request.getParameter("TESTKINDCD");
            _tesName = getTestName(db2);
            _categorySelected = request.getParameterValues("category_selected");
            _checkkettendiv = request.getParameter("checkKettenDiv");
            _usecurriculumcd = request.getParameter("useCurriculumcd");
            _usetestcountflg = request.getParameter("useTestCountflg");
        }

        private String getSemesterName(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SEMESTERNAME ");
            stb.append(" FROM ");
            stb.append("     SEMESTER_MST ");
            stb.append(" WHERE ");
            stb.append("         YEAR     = '" + _ctrlYear + "' ");
            stb.append("     AND SEMESTER = '" + _semester + "' ");

            String retStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    retStr = rs.getString("SEMESTERNAME");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retStr;
        }

        private String getTestName(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     TESTITEMNAME ");
            stb.append(" FROM ");
            stb.append("     TESTITEM_MST_COUNTFLG_NEW_SDIV ");
            stb.append(" WHERE ");
            stb.append("         YEAR          = '" + _ctrlYear + "' ");
            stb.append("     AND SEMESTER      = '" + _semester + "' ");
            stb.append("     AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + _testkindcd + "' ");

            String retStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    retStr = rs.getString("TESTITEMNAME");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retStr;
        }

    }
}

// eof
