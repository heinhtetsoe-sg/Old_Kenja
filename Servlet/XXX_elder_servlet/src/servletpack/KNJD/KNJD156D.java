// kanji=漢字
/*
 * $Id$
 *
 * 作成日: 2007/05/14
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;


import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 成績一覧表（成績判定会議用）を印刷します。
 * 成績処理改訂版。
 * 成績判定会議用をBASEとし、従来の成績一覧表の成績処理改訂版の印刷処理を行います。
 * @author nakamoto
 * @version $Id$
 */
public class KNJD156D {
    private static final Log log = LogFactory.getLog(KNJD156D.class);

    private static final DecimalFormat DEC_FMT1 = new DecimalFormat("0.0");
    private static final DecimalFormat DEC_FMT2 = new DecimalFormat("0");
    private static final String SEMEALL = "9";

    static final String PATTERN1 = "1"; // 科目固定型
    static final String PATTERN2 = "2"; // 科目変動型
    static final String PATTERN3 = "3"; // 科目固定型（仮評定付）
    static final String PATTERN4 = "4"; // 成績の記録
    static final String PATTERN5 = "5"; // 欠課時数と出欠の記録

    private static final String OUTPUT_KJUN2 = "2";
    private static final String OUTPUT_KJUN3 = "3";
    private static final String OUTPUT_RANK1 = "1";
    private static final String OUTPUT_RANK2 = "2";
    private static final String OUTPUT_RANK3 = "3";
    private static final String OUTPUT_RANK4 = "4";

    private static final String SIDOU_INPUT_INF_MARK = "1";
    private static final String SIDOU_INPUT_INF_SCORE = "2";

    private static final String SCORE_DIV_01 = "01";
    private static final String SCORE_DIV_02 = "02";
    private static final String SCORE_DIV_08 = "08";
    private static final String SCORE_DIV_09 = "09";

    private static final String ATTRIBUTE_KETTEN = "Paint=(1,90,1),Bold=1";
    private static final String ATTRIBUTE_KEKKAOVER = "Paint=(1,90,1),Bold=1";
    private static final String ATTRIBUTE_ELECTDIV = "Paint=(2,90,2),Bold=1";
    private static final String ATTRIBUTE_NORMAL = "Paint=(0,0,0),Bold=0";

    private static final String csv = "csv";

    private final static String DATA_SELECT_ALL = "0";
    private final static String DATA_SELECT_RYOUSEI = "1";
    private final static String DATA_SELECT_SUISEN = "2";
    private final static String DATA_SELECT_KAIGAI = "3";
    private final static String DATA_SELECT_IB = "4";
    private final static String DATA_SELECT_A_HOUSHIKI = "6";

    private final static String PRGID_KNJD615H = "KNJD615H";
    private final static String SUISEN = "4";
    private final static String KAIGAI = "3";
    private final static String A_HOUSHIKI = "('01','05','08')";
    private final static String IB_COURSECODE = "0002";
    private final static String PRGID_KNJD615P = "KNJD615P";

    private static final String ALL9 = "999999";
    protected boolean _hasData = false;
    private Param _param;

    public void svf_out (
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {

        final Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）

        log.debug(" $Id$ ");
        KNJServletUtils.debugParam(request, log);

        try {
            response.setContentType("application/pdf");
            // ＤＢ接続
            DB2UDB db2 = null;
            try {
                db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);    //Databaseクラスを継承したクラス
                db2.open();
            } catch (Exception ex) {
                log.error("db2 open error!", ex);
                return;
            }
            _param = new Param(request, db2);

            svf.VrInit();                             //クラスの初期化
            svf.VrSetSpoolFileStream(response.getOutputStream());         //PDFファイル名の設定
            // 印刷処理
            printMain(svf, db2);

        } catch (Exception ex) {
            log.error("exception!", ex);
        } finally {
            // 終了処理
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            svf.VrQuit();

        }
    }

    private void printMain(Vrw32alp svf, final DB2UDB db2) {

        List<Student> studentList = getStudentList(db2);

        final List semeTestList = new ArrayList();
        semeTestList.add("1-010108");
        semeTestList.add("1-020108");
        semeTestList.add("2-010108");
        semeTestList.add("2-020108");
        semeTestList.add("3-020108");

        String befGhrClass = "";
        Map attendSubMap = null;
        Map attendSemeMap = null;
        for(Student student : studentList) {

            //出欠情報取得
            if (!befGhrClass.equals(student._grade + student._hrClass)) {
                //クラスが切り替わった段階で取得
                for (final Iterator rit = _param._attendRanges.values().iterator(); rit.hasNext();) {
                    final DateRange range = (DateRange) rit.next();
                    attendSubMap  = getAttendSubMap(db2, range, attendSubMap);
                    attendSemeMap = getAttendSemeMap(db2, range, attendSemeMap); //TODO
                }
            }

            svf.VrSetForm("KNJD156D.frm", 1);

             svf.VrsOut("NENDO", _param._year + "年度");
            svf.VrsOut("DATE", _param._ctrlDate2);
            svf.VrsOut("SCHOOL_NAME", _param._knjSchoolMst._schoolName1);

            svf.VrsOut("SEMESTER1", "1学期");
            svf.VrsOut("TEST_NAME1_1", "中間");
            svf.VrsOut("TEST_NAME1_2", "期末");
            svf.VrsOut("SEMESTER2", "2学期");
            svf.VrsOut("TEST_NAME2_1", "中間");
            svf.VrsOut("TEST_NAME2_2", (student._grade != "03") ? "期末" : "学年末");
            svf.VrsOut("SEMESTER3", "3学期");
            svf.VrsOut("TEST_NAME3_1", (student._grade != "03") ? "学年末" : "");

            svf.VrsOut("NAME", student._name);
            final String attendNo = (student._attendNo != null) ? student._attendNo.replaceFirst("^0+", "") : "";
            svf.VrsOut("HR_NAME", student._hrName + attendNo + "番");
            svf.VrsOut("COURSE_NAME", "（" + student._courseName + "）");

            //学期毎の出欠累計
            SemeAttendData attendData = null;
            int[] semesArray = {1, 2, 3, 9};
            for (int semesKey : semesArray) {
                final String attendKey = student._schregno + "-" + semesKey;
                if (!attendSemeMap.containsKey(attendKey)) {
                    continue;
                }
                attendData = (SemeAttendData)attendSemeMap.get(attendKey);
                if (attendData != null) {
                    final int semesLine = semesKey != 9 ? semesKey : 4;
                    final String semesname = semesKey == 1 ? "1学期" : semesKey == 2 ? "2学期" : "3学期" ;
                    svf.VrsOut("SEMESTER" + semesLine + "_2", semesname);
                    svf.VrsOutn("LESSON", semesLine, attendData._lesson);
                    svf.VrsOutn("MOURNING", semesLine, attendData._kibikiNado);
                    svf.VrsOutn("MUST", semesLine, attendData._mlesson);
                    svf.VrsOutn("ABSENT", semesLine, attendData._sick);
                    svf.VrsOutn("PRESENT", semesLine, attendData._present);
                    svf.VrsOutn("LATE", semesLine, attendData._late);
                    svf.VrsOutn("EARLY", semesLine, attendData._early);
                }
            }

            //科目毎の成績
            int subLine = 0;
            for (final Iterator it = student._subclassMap.keySet().iterator(); it.hasNext();) {
                final String subclasscd = (String) it.next();
                final Subclass subclass = (Subclass)student._subclassMap.get(subclasscd);
                subLine++;
                final int subnameByte = KNJ_EditEdit.getMS932ByteLength(subclass._subclassName);
                final String fieldSuffix = subnameByte <= 16 ? "1" : subnameByte <= 24 ? "2" : "3";
                svf.VrsOutn("SUBCLASS_NAME" + fieldSuffix, subLine, subclass._subclassName);

                for (final Iterator it2 = semeTestList.iterator(); it2.hasNext();) {
                    final String semeTestKey = (String) it2.next();
                    final String[] keyElements = semeTestKey.split("-");
                    final String semester = keyElements[0];
                    final String testcd   = keyElements[1];

                    final String fieldNo1 = semester;
                    final String fieldNo2 = "3".equals(semester) ? "1" : "010108".equals(testcd) ? "1" : "2";
                    final String fieldNo3 = fieldNo1 + "_" + fieldNo2;

                    final String scoreKey = subclasscd + "-" + semester + "-" + testcd;
                    final SubScoreData scoreData = (SubScoreData)student._subScoreMap.get(scoreKey);
                    if (scoreData != null) {
                        svf.VrsOutn("SCORE" + fieldNo3  , subLine, scoreData._score);
                        svf.VrsOutn("AVE" + fieldNo3, subLine, scoreData._avg);
                        svf.VrsOutn("RANK" + fieldNo3, subLine, scoreData._rank + "/" + scoreData._count);
                    }

                    if (!"010108".equals(testcd)) {
                        final String attendSubKey = student._schregno + "-" + subclasscd + "-" + semester;
                        BigDecimal sick = (BigDecimal)attendSubMap.get(attendSubKey);
                        if(sick != null) {
                            svf.VrsOutn("KEKKA" + fieldNo1, subLine, sick.toString());
                        }
                    }
                }
            }
            // 個人/クラス 個人/コース
            if (subLine > 0) {
                final String subclasscd = "999999";
                for (final Iterator it2 = semeTestList.iterator(); it2.hasNext();) {
                    final String semeTestKey = (String) it2.next();
                    final String[] keyElements = semeTestKey.split("-");
                    final String semester = keyElements[0];
                    final String testcd   = keyElements[1];

                    final String fieldNo1 = semester;
                    final String fieldNo2 = "3".equals(semester) ? "1" : "010108".equals(testcd) ? "1" : "2";
                    final String fieldNo3 = fieldNo1 + "_" + fieldNo2;

                    final String scoreKey = subclasscd + "-" + semester + "-" + testcd;
                    final SubAllScoreData scoreData = (SubAllScoreData)student._subAllScoreMap.get(scoreKey);
                    if (scoreData != null) {
                        //クラス
                        svf.VrsOutn("TOTAL_AVE" + fieldNo3, 1, scoreData._all9Avg + "/" + scoreData._classAvg);
                        svf.VrsOutn("TOTAL_RANK" + fieldNo3, 1, scoreData._classRank + "/" + scoreData._classCount);
                        //コース
                        svf.VrsOutn("TOTAL_AVE" + fieldNo3, 2, scoreData._all9Avg + "/" + scoreData._courseAvg);
                        svf.VrsOutn("TOTAL_RANK" + fieldNo3, 2, scoreData._courseRank + "/" + scoreData._courseCount);
                    }
                }
            }
            befGhrClass = student._grade + student._hrClass;
            _hasData = true;
            svf.VrEndPage();
        }
    }

    public List getStudentList(final DB2UDB db2) {
        final List studentList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getStudentSql();
            log.debug(sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno       = rs.getString("SCHREGNO");
                final String name           = rs.getString("NAME");
                final String grade          = rs.getString("GRADE");
                final String hrClass        = rs.getString("HR_CLASS");
                final String hrName         = rs.getString("HR_NAME");
                final String attendno       = rs.getString("ATTENDNO");
                final String courseCd       = rs.getString("COURSECD");
                final String majorCd        = rs.getString("MAJORCD");
                final String courseCode     = rs.getString("COURSECODE");
                final String courseName     = rs.getString("COURSECODENAME");

                final Student student = new Student(schregno, name, grade, hrClass, hrName, attendno, courseCd, majorCd, courseCode, courseName);
                student.setSubclassMap(db2);
                student.setSubScoreMap(db2);
                studentList.add(student);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return studentList;
    }

    public String getStudentSql() {
        StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T2.NAME, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T3.HR_NAME, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.COURSECD, ");
        stb.append("     T1.MAJORCD, ");
        stb.append("     T1.COURSECODE, ");
        stb.append("     T4.COURSECODENAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     INNER JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR = T1.YEAR ");
        stb.append("         AND T3.SEMESTER = T1.SEMESTER ");
        stb.append("         AND T3.GRADE = T1.GRADE ");
        stb.append("         AND T3.HR_CLASS = T1.HR_CLASS ");
        stb.append("     LEFT JOIN COURSECODE_MST T4 ON T4.COURSECODE = T1.COURSECODE ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        if ("9".equals(_param._selectSemester)) {
            stb.append("     AND T1.SEMESTER = '" + _param._ctrlSemester + "' ");
        } else {
            stb.append("     AND T1.SEMESTER = '" + _param._selectSemester + "' ");
        }
        if ("1".equals(_param._disp)) {
            stb.append("     AND T1.GRADE || T1.HR_CLASS IN " + _param._selectedIn  + " ");
        } else {
            stb.append("     AND T1.SCHREGNO IN " + _param._selectedIn  + " ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO ");
        return stb.toString();
    }


    private Map getAttendSemeMap(DB2UDB db2, final DateRange dateRange, final Map map) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        final String sql = AttendAccumulate.getAttendSemesSql(
                _param._year,
                _param._selectSemester,
                dateRange._sdate,
                dateRange._edate,
                _param._attendParamMap
                );
        final Map attendSemeMap;
        if(map != null) {
            attendSemeMap = map;
        } else {
            attendSemeMap = new LinkedHashMap();
        }

        try {
            log.debug(sql);
            ps = db2.prepareStatement(sql);

            for (final Map row : KnjDbUtils.query(db2, ps, new Object[] {})) {

                final String schregno  		= KnjDbUtils.getString(row, "SCHREGNO");
//	            final String semester  		= KnjDbUtils.getString(row, "SEMESTER");

                final String lesson  		= KnjDbUtils.getString(row, "LESSON");
                final String suspend  		= KnjDbUtils.getString(row, "SUSPEND");
                final String virus  		= KnjDbUtils.getString(row, "VIRUS");
                final String koudome  		= KnjDbUtils.getString(row, "KOUDOME");
                final String mourning  		= KnjDbUtils.getString(row, "MOURNING");
                final String mlesson  		= KnjDbUtils.getString(row, "MLESSON");
                final String sick  			= KnjDbUtils.getString(row, "SICK");
                final String present  		= KnjDbUtils.getString(row, "PRESENT");
                final String late  			= KnjDbUtils.getString(row, "LATE");
                final String early  		= KnjDbUtils.getString(row, "EARLY");


                final int supendInt			= (!StringUtils.isEmpty(suspend)) ? Integer.parseInt(suspend) : 0;
                final int virusInt				= (!StringUtils.isEmpty(virus)) ? Integer.parseInt(virus) : 0;
                final int koudomeInt			= (!StringUtils.isEmpty(koudome)) ? Integer.parseInt(koudome) : 0;
                final int mourningInt			= (!StringUtils.isEmpty(mourning)) ? Integer.parseInt(mourning) : 0;
                final int kibikiNadoInt	    = supendInt + virusInt + koudomeInt + mourningInt;

                final String kibikiNado			= String.valueOf(kibikiNadoInt);
                SemeAttendData semeData 		= new SemeAttendData(lesson, kibikiNado, mlesson, sick, present, late, early);
//	            attendSemeMap.put(schregno + "-" + semester, semeData);
                attendSemeMap.put(schregno + "-" + dateRange._key, semeData);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return attendSemeMap;
    }

    private Map getAttendSubMap(DB2UDB db2, final DateRange dateRange, final Map map) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        final String sql = AttendAccumulate.getAttendSubclassSql(
                _param._year,
                _param._selectSemester,
                dateRange._sdate,
                dateRange._edate,
                _param._attendParamMap
                );
        final Map attendSubMap;
        if(map != null) {
            attendSubMap = map;
        } else {
            attendSubMap = new LinkedHashMap();
        }
        try {
            log.debug(sql);
            ps = db2.prepareStatement(sql);

            for (final Map row : KnjDbUtils.query(db2, ps, new Object[] {})) {

                final String schregno 		= KnjDbUtils.getString(row, "SCHREGNO");
                final String subclascd 		= KnjDbUtils.getString(row, "SUBCLASSCD");

                final SubclassMst mst = (SubclassMst) _param._subclassMstMap.get(subclascd);
                if (null == mst) {
                    log.warn("no subclass : " + subclascd);
                    continue;
                }

//	            final String semester  		= KnjDbUtils.getString(row, "SEMESTER");
                final BigDecimal sick = KnjDbUtils.getBigDecimal(row, "SICK2", null);
                final BigDecimal replacedSick = KnjDbUtils.getBigDecimal(row, "REPLACED_SICK", null);
                final BigDecimal sick2 = mst.isSaki() ? replacedSick : sick;
//	            final String sick2 			= KnjDbUtils.getString(row, "SICK2");	//換算済み欠課時数

                attendSubMap.put(schregno + "-" + subclascd + "-" + dateRange._key, sick2);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return attendSubMap;
    }

    //科目毎の成績データ
    private static class SubScoreData {
        final String _subclasscd;
        final String _score;
        final String _avg;
        final String _rank;
        final String _count;

        public SubScoreData(
                final String subclasscd,
                final String score,
                final String avg,
                final String rank,
                final String count
        ) {
            _subclasscd      = subclasscd;
            _score           = score;
            _avg             = avg;
            _rank            = rank;
            _count           = count;
        }
    }

    //合計科目の成績データ
    private static class SubAllScoreData {
        final String _subclasscd;
        final String _all9Avg;
        final String _classAvg;
        final String _courseAvg;
        final String _classRank;
        final String _courseRank;
        final String _classCount;
        final String _courseCount;

        public SubAllScoreData(
                final String subclasscd,
                final String all9Avg,
                final String classAvg,
                final String courseAvg,
                final String classRank,
                final String courseRank,
                final String classCount,
                final String courseCount
        ) {
            _subclasscd      = subclasscd;
            _all9Avg         = all9Avg;
            _classAvg        = classAvg;
            _courseAvg       = courseAvg;
            _classRank       = classRank;
            _courseRank      = courseRank;
            _classCount      = classCount;
            _courseCount     = courseCount;
        }
    }

    //学期毎の出欠データ
    private static class SemeAttendData {
        final String _lesson;
        final String _kibikiNado;
        final String _mlesson;
        final String _sick;
        final String _present;
        final String _late;
        final String _early;

        public SemeAttendData(
                final String lesson,
                final String kibikiNado,
                final String mlesson,
                final String sick,
                final String present,
                final String late,
                final String early
        ) {
            _lesson      = lesson;
            _kibikiNado  = kibikiNado;
            _mlesson     = mlesson;
            _sick        = sick;
            _present     = present;
            _late        = late;
            _early       = early;
        }
    }

    //生徒クラス
    private class Student {
        final String _schregno;
        final String _name;
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _attendNo;
        final String _courseCd;
        final String _majorCd;
        final String _courseCode;
        final String _courseName;
        final Map _subclassMap;
        final Map _subScoreMap;
        final Map _subAllScoreMap;

        public Student(
                final String schregno,
                final String name,
                final String grade,
                final String hrClass,
                final String hrName,
                final String attendNo,
                final String courseCd,
                final String majorCd,
                final String courseCode,
                final String courseName
        ) {
            _schregno       = schregno;
            _name           = name;
            _grade          = grade;
            _hrClass        = hrClass;
            _hrName         = hrName;
            _attendNo       = attendNo;
            _courseCd       = courseCd;
            _majorCd        = majorCd;
            _courseCode     = courseCode;
            _courseName     = courseName;
            _subclassMap    = new LinkedHashMap();
            _subScoreMap    = new LinkedHashMap();
            _subAllScoreMap = new LinkedHashMap();
        }

        private void setSubclassMap(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getSubclassSql();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String classCd = rs.getString("CLASSCD");
                    final String schoolKind = rs.getString("SCHOOL_KIND");
                    final String curriculumCd = rs.getString("CURRICULUM_CD");
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final String subclassName = rs.getString("SUBCLASSNAME");

                    final String key = classCd + '-' + schoolKind + '-' + curriculumCd + '-' + subclassCd;
                    final Subclass subclass = new Subclass(classCd, schoolKind, curriculumCd, subclassCd, subclassName);
                    _subclassMap.put(key, subclass);
                }
            } catch (SQLException ex) {
                log.error("setSubclassMap exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getSubclassSql() {
            StringBuffer stb = new StringBuffer();
            stb.append(" WITH SUBCLASS_T AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.YEAR, ");
            stb.append("         T2.CLASSCD, ");
            stb.append("         T2.SCHOOL_KIND, ");
            stb.append("         T2.CURRICULUM_CD, ");
            stb.append("         T2.SUBCLASSCD ");
            stb.append("     FROM ");
            stb.append("         CHAIR_STD_DAT T1 ");
            stb.append("         INNER JOIN CHAIR_DAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("             AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("             AND T2.CHAIRCD = T1.CHAIRCD ");
            stb.append("     WHERE ");
            stb.append("         T1.YEAR = '" + _param._year + "' ");
            stb.append("         AND T1.SEMESTER <= '" + _param._selectSemester + "' ");
            stb.append("         AND T1.SCHREGNO = '" + _schregno + "' ");
            stb.append("         AND T2.CLASSCD <= '90' ");
            stb.append("     GROUP BY ");
            stb.append("         T1.YEAR, ");
            stb.append("         T2.CLASSCD, ");
            stb.append("         T2.SCHOOL_KIND, ");
            stb.append("         T2.CURRICULUM_CD, ");
            stb.append("         T2.SUBCLASSCD ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T2.CLASSCD, ");
            stb.append("     T2.SCHOOL_KIND, ");
            stb.append("     T2.CURRICULUM_CD, ");
            stb.append("     T2.SUBCLASSCD, ");
            stb.append("     VALUE(T2.SUBCLASSORDERNAME2, T2.SUBCLASSNAME) AS SUBCLASSNAME ");
            stb.append(" FROM ");
            stb.append("     SUBCLASS_T T1 ");
            stb.append("     INNER JOIN V_SUBCLASS_MST T2 ON T2.YEAR = T1.YEAR ");
            stb.append("         AND T2.CLASSCD = T1.CLASSCD ");
            stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
//            stb.append("         AND VALUE(T2.ELECTDIV, '0') <> '1' ");
            stb.append(" ORDER BY ");
            stb.append("     T2.CLASSCD, ");
            stb.append("     T2.SCHOOL_KIND, ");
            stb.append("     VALUE(T2.SHOWORDER3, '99'), ");
            stb.append("     T2.CURRICULUM_CD, ");
            stb.append("     T2.SUBCLASSCD ");
            return stb.toString();
        }

        private void setSubScoreMap(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getRecordSql();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclasscd     = rs.getString("SUBCLASSCD");
                    final String subclasscdOnly = rs.getString("SUBCLASSCD_ONLY");
                    final String semester       = rs.getString("SEMESTER");
                    final String testcd         = rs.getString("TESTCD");
                    final String score          = rs.getString("SCORE");
                    final String all9Avg        = StringUtils.defaultString(rs.getString("ALL9_AVG"));
                    final String courseAvg      = StringUtils.defaultString(rs.getString("COURSE_AVG"));
                    final String classAvg       = StringUtils.defaultString(rs.getString("CLASS_AVG"));
                    final String courseRank     = StringUtils.defaultString(rs.getString("COURSE_RANK"));
                    final String classRank      = StringUtils.defaultString(rs.getString("CLASS_RANK"));
                    final String courseCount    = StringUtils.defaultString(rs.getString("COURSE_COUNT"));
                    final String classCount     = StringUtils.defaultString(rs.getString("CLASS_COUNT"));

                    if (!"999999".equals(subclasscdOnly)) {
                        SubScoreData subData = new SubScoreData(subclasscd, score, courseAvg, courseRank, courseCount); //科目ごとの得点・平均(コース)・順位(コース)・人数(コース)
                        _subScoreMap.put(subclasscd + "-" + semester + "-" + testcd, subData);
                    } else {
                        SubAllScoreData subAllData = new SubAllScoreData(subclasscdOnly, all9Avg, classAvg, courseAvg, classRank, courseRank, classCount, courseCount); //全科目の平均とクラス/コースごとの順位・人数
                        _subAllScoreMap.put(subclasscdOnly + "-" + semester + "-" + testcd, subAllData);
                    }
                }
            } catch (SQLException ex) {
                log.debug("setSubScoreMap Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        public String getRecordSql() {
            StringBuffer stb = new StringBuffer();
            stb.append("   WITH REGD_DATA AS ( ");
            stb.append("     SELECT ");
            stb.append("       T1.YEAR, ");
            stb.append("       T1.SCHREGNO, ");
            stb.append("       T1.GRADE, ");
            stb.append("       T1.HR_CLASS, ");
            stb.append("       T1.ATTENDNO, ");
            stb.append("       T1.COURSECD, ");
            stb.append("       T1.MAJORCD, ");
            stb.append("       T1.COURSECODE ");
            stb.append("     FROM ");
            stb.append("       SCHREG_REGD_DAT T1 ");
            stb.append("     WHERE ");
            stb.append("       T1.YEAR = '" + _param._year + "' ");
            if ("9".equals(_param._selectSemester)) {
                stb.append("     AND T1.SEMESTER = '" + _param._ctrlSemester + "' ");
            } else {
                stb.append("     AND T1.SEMESTER = '" + _param._selectSemester + "' ");
            }
            stb.append("         AND T1.SCHREGNO = '" + _schregno + "' ");
            stb.append("   ), CLASS_COURSE_AVG AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.YEAR, ");
            stb.append("         T1.SEMESTER, ");
            stb.append("         T1.TESTKINDCD, ");
            stb.append("         T1.TESTITEMCD, ");
            stb.append("         T1.SCORE_DIV, ");
            stb.append("         '99' AS CLASSCD, ");
            stb.append("         MAX(SCHOOL_KIND) AS SCHOOL_KIND, ");
            stb.append("         '99' AS CURRICULUM_CD, ");
            stb.append("         '999999' AS SUBCLASSCD, ");
            stb.append("         '2' AS AVG_DIV, ");
            stb.append("         T2.GRADE, ");
            stb.append("         T2.HR_CLASS, ");
            stb.append("         '0' AS COURSECD, ");
            stb.append("         '000' AS MAJORCD, ");
            stb.append("         '0000' AS COURSECODE, ");
            stb.append("         AVG(AVG) AS AVG ");
            stb.append("     FROM ");
            stb.append("         RECORD_RANK_SDIV_DAT T1 ");
            stb.append("         INNER JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("             AND T2.YEAR = T1.YEAR ");
            if ("9".equals(_param._selectSemester)) {
                stb.append("             AND T2.SEMESTER = '" + _param._ctrlSemester + "' ");
            } else {
                stb.append("             AND T2.SEMESTER = '" + _param._selectSemester + "' ");
            }
            stb.append("             AND T2.GRADE = '" + _grade + "' ");
            stb.append("             AND T2.HR_CLASS = '" + _hrClass + "' ");
            stb.append("     WHERE ");
            stb.append("         T1.YEAR = '" + _param._year + "' ");
            stb.append("         AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV IN ('1010108','1020108','2010108','2020108','3020108') ");
            stb.append("         AND T1.SUBCLASSCD IN ('999999') ");
            stb.append("     GROUP BY ");
            stb.append("         T1.YEAR, ");
            stb.append("         T1.SEMESTER, ");
            stb.append("         T1.TESTKINDCD, ");
            stb.append("         T1.TESTITEMCD, ");
            stb.append("         T1.SCORE_DIV, ");
            stb.append("         T2.GRADE, ");
            stb.append("         T2.HR_CLASS ");
            stb.append("     UNION ALL ");
            stb.append("     SELECT ");
            stb.append("         T1.YEAR, ");
            stb.append("         T1.SEMESTER, ");
            stb.append("         T1.TESTKINDCD, ");
            stb.append("         T1.TESTITEMCD, ");
            stb.append("         T1.SCORE_DIV, ");
            stb.append("         '99' AS CLASSCD, ");
            stb.append("         MAX(SCHOOL_KIND) AS SCHOOL_KIND, ");
            stb.append("         '99' AS CURRICULUM_CD, ");
            stb.append("         '999999' AS SUBCLASSCD, ");
            stb.append("         '3' AS AVG_DIV, ");
            stb.append("         T2.GRADE, ");
            stb.append("         '000' AS HR_CLASS, ");
            stb.append("         T2.COURSECD, ");
            stb.append("         T2.MAJORCD, ");
            stb.append("         T2.COURSECODE, ");
            stb.append("         AVG(AVG) AS AVG ");
            stb.append("     FROM ");
            stb.append("         RECORD_RANK_SDIV_DAT T1 ");
            stb.append("         INNER JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("             AND T2.YEAR = T1.YEAR ");
            if ("9".equals(_param._selectSemester)) {
                stb.append("             AND T2.SEMESTER = '" + _param._ctrlSemester + "' ");
            } else {
                stb.append("             AND T2.SEMESTER = '" + _param._selectSemester + "' ");
            }
            stb.append("             AND T2.GRADE = '" + _grade + "' ");
            stb.append("             AND T2.COURSECD = '" + _courseCd + "' ");
            stb.append("             AND T2.MAJORCD = '" + _majorCd + "' ");
            stb.append("             AND T2.COURSECODE = '" + _courseCode + "' ");
            stb.append("     WHERE ");
            stb.append("         T1.YEAR = '" + _param._year + "' ");
            stb.append("         AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV IN ('1010108','1020108','2010108','2020108','3020108') ");
            stb.append("         AND T1.SUBCLASSCD IN ('999999') ");
            stb.append("     GROUP BY ");
            stb.append("         T1.YEAR, ");
            stb.append("         T1.SEMESTER, ");
            stb.append("         T1.TESTKINDCD, ");
            stb.append("         T1.TESTITEMCD, ");
            stb.append("         T1.SCORE_DIV, ");
            stb.append("         T2.GRADE, ");
            stb.append("         T2.COURSECD, ");
            stb.append("         T2.MAJORCD, ");
            stb.append("         T2.COURSECODE ");
            stb.append("   ), RECORD_BASE AS ( ");
            stb.append("     SELECT ");
            stb.append("       T1.YEAR, ");
            stb.append("       T1.SEMESTER, ");
            stb.append("       T2.GRADE, ");
            stb.append("       T2.HR_CLASS, ");
            stb.append("       T2.ATTENDNO, ");
            stb.append("       T1.TESTKINDCD, ");
            stb.append("       T1.TESTITEMCD, ");
            stb.append("       T1.SCORE_DIV, ");
            stb.append("       T1.CLASSCD, ");
            stb.append("       T1.SCHOOL_KIND, ");
            stb.append("       T1.CURRICULUM_CD, ");
            stb.append("       T1.SUBCLASSCD, ");
            stb.append("       T1.SCHREGNO, ");
            stb.append("       T2.COURSECD, ");
            stb.append("       T2.MAJORCD, ");
            stb.append("       T2.COURSECODE, ");
            stb.append("       T1.SCORE, ");
            stb.append("       CAST(ROUND(T1.AVG, 1) AS FLOAT) AS ALL9_AVG, ");
            stb.append("       CASE WHEN T1.SUBCLASSCD = '999999' THEN CAST(ROUND(T5.AVG, 1) AS FLOAT) ELSE CAST(ROUND(T3.AVG, 1) AS FLOAT) END AS COURSE_AVG, ");
            stb.append("       CASE WHEN T1.SUBCLASSCD = '999999' THEN CAST(ROUND(T6.AVG, 1) AS FLOAT) ELSE CAST(ROUND(T4.AVG, 1) AS FLOAT) END AS CLASS_AVG, ");
            stb.append("       T1.COURSE_RANK, ");
            stb.append("       T1.CLASS_RANK, ");
            stb.append("       T3.COUNT AS COURSE_COUNT, ");
            stb.append("       T4.COUNT AS CLASS_COUNT ");
            stb.append("     FROM ");
            stb.append("       RECORD_RANK_SDIV_DAT T1 ");
            stb.append("       INNER JOIN REGD_DATA T2 ");
            stb.append("         ON T2.YEAR = T1.YEAR ");
            stb.append("         AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("       LEFT JOIN RECORD_AVERAGE_SDIV_DAT T3 ");
            stb.append("         ON T3.YEAR = T1.YEAR ");
            stb.append("         AND T3.SEMESTER = T1.SEMESTER ");
            stb.append("         AND T3.TESTKINDCD = T1.TESTKINDCD ");
            stb.append("         AND T3.TESTITEMCD = T1.TESTITEMCD ");
            stb.append("         AND T3.SCORE_DIV = T1.SCORE_DIV ");
            stb.append("         AND T3.CLASSCD = T1.CLASSCD ");
            stb.append("         AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("         AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("         AND T3.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND T3.AVG_DIV = '3' ");
            stb.append("         AND T3.GRADE = T2.GRADE ");
            stb.append("         AND T3.HR_CLASS = '000' ");
            stb.append("         AND T3.COURSECD = T2.COURSECD ");
            stb.append("         AND T3.MAJORCD = T2.MAJORCD ");
            stb.append("         AND T3.COURSECODE = T2.COURSECODE ");
            stb.append("       LEFT JOIN RECORD_AVERAGE_SDIV_DAT T4 ");
            stb.append("         ON T4.YEAR = T1.YEAR ");
            stb.append("         AND T4.SEMESTER = T1.SEMESTER ");
            stb.append("         AND T4.TESTKINDCD = T1.TESTKINDCD ");
            stb.append("         AND T4.TESTITEMCD = T1.TESTITEMCD ");
            stb.append("         AND T4.SCORE_DIV = T1.SCORE_DIV ");
            stb.append("         AND T4.CLASSCD = T1.CLASSCD ");
            stb.append("         AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("         AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("         AND T4.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND T4.AVG_DIV = '2' ");
            stb.append("         AND T4.GRADE = T2.GRADE ");
            stb.append("         AND T4.HR_CLASS = T2.HR_CLASS ");
            stb.append("         AND T4.COURSECD = '0' ");
            stb.append("         AND T4.MAJORCD = '000' ");
            stb.append("         AND T4.COURSECODE = '0000' ");
            stb.append("       LEFT JOIN CLASS_COURSE_AVG T5 ");
            stb.append("         ON T5.YEAR = T1.YEAR ");
            stb.append("         AND T5.SEMESTER = T1.SEMESTER ");
            stb.append("         AND T5.TESTKINDCD = T1.TESTKINDCD ");
            stb.append("         AND T5.TESTITEMCD = T1.TESTITEMCD ");
            stb.append("         AND T5.SCORE_DIV = T1.SCORE_DIV ");
            stb.append("         AND T5.CLASSCD = T1.CLASSCD ");
            stb.append("         AND T5.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("         AND T5.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("         AND T5.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND T5.AVG_DIV = '3' ");
            stb.append("         AND T5.GRADE = T2.GRADE ");
            stb.append("         AND T5.HR_CLASS = '000' ");
            stb.append("         AND T5.COURSECD = T2.COURSECD ");
            stb.append("         AND T5.MAJORCD = T2.MAJORCD ");
            stb.append("         AND T5.COURSECODE = T2.COURSECODE ");
            stb.append("       LEFT JOIN CLASS_COURSE_AVG T6 ");
            stb.append("         ON T6.YEAR = T1.YEAR ");
            stb.append("         AND T6.SEMESTER = T1.SEMESTER ");
            stb.append("         AND T6.TESTKINDCD = T1.TESTKINDCD ");
            stb.append("         AND T6.TESTITEMCD = T1.TESTITEMCD ");
            stb.append("         AND T6.SCORE_DIV = T1.SCORE_DIV ");
            stb.append("         AND T6.CLASSCD = T1.CLASSCD ");
            stb.append("         AND T6.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("         AND T6.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("         AND T6.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND T6.AVG_DIV = '2' ");
            stb.append("         AND T6.GRADE = T2.GRADE ");
            stb.append("         AND T6.HR_CLASS = T2.HR_CLASS ");
            stb.append("         AND T6.COURSECD = '0' ");
            stb.append("         AND T6.MAJORCD = '000' ");
            stb.append("         AND T6.COURSECODE = '0000' ");
            stb.append("     WHERE ");
            stb.append("       T1.YEAR = '" + _param._year + "' ");
            stb.append("       AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV IN ( ");
            stb.append("         '1010108', ");
            stb.append("         '1020108', ");
            stb.append("         '2010108', ");
            stb.append("         '2020108', ");
            stb.append("         '3020108' ");
            stb.append("       ) ");
            stb.append("       AND T1.SUBCLASSCD NOT IN ( ");
            stb.append("         '333333', ");
            stb.append("         '555555', ");
            stb.append("         '777777', ");
            stb.append("         '99999A', ");
            stb.append("         '99999B' ");
            stb.append("       ) ");
            stb.append("   ) ");
            stb.append("   SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     T1.SUBCLASSCD AS SUBCLASSCD_ONLY, ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS TESTCD, ");
            stb.append("     T1.SCORE, ");
            stb.append("     T1.ALL9_AVG, ");
            stb.append("     T1.COURSE_AVG, ");
            stb.append("     T1.CLASS_AVG, ");
            stb.append("     T1.COURSE_RANK, ");
            stb.append("     T1.CLASS_RANK, ");
            stb.append("     T1.COURSE_COUNT, ");
            stb.append("     T1.CLASS_COUNT ");
            stb.append("   FROM ");
            stb.append("     RECORD_BASE T1 ");
            stb.append("   WHERE ");
            stb.append("     T1.SEMESTER <= '" + _param._selectSemester + "' ");
            stb.append("   ORDER BY ");
            stb.append("     SUBCLASSCD, ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     TESTCD ");

            return stb.toString();
        }

    }

    //科目クラス
    private static class Subclass {
        final String _classCd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclassCd;
        final String _subclassName;

        public Subclass(
                final String classCd,
                final String schoolKind,
                final String curriculumCd,
                final String subclassCd,
                final String subclassName
        ) {
            _classCd = classCd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclassCd = subclassCd;
            _subclassName = subclassName;
        }
    }

    private static class SemesterDetail implements Comparable<SemesterDetail> {
        final Semester _semester;
        final String _cdSemesterDetail;
        final String _semestername;
        final String _sdate;
        final String _edate;
        public SemesterDetail(Semester semester, String cdSemesterDetail, String semestername, final String sdate, final String edate) {
            _semester = semester;
            _cdSemesterDetail = cdSemesterDetail;
            _semestername = StringUtils.defaultString(semestername);
            _sdate = sdate;
            _edate = edate;
        }
        public int compareTo(final SemesterDetail sd) {
            int rtn;
            rtn = _semester.compareTo(sd._semester);
            if (rtn != 0) {
                return rtn;
            }
            rtn = _cdSemesterDetail.compareTo(sd._cdSemesterDetail);
            return rtn;
        }
        public String toString() {
            return "SemesterDetail(" + _semester._semester + ", " + _cdSemesterDetail + ", " + _semestername + ")";
        }
    }

    private static class TestItem {
        final String _year;
        final Semester _semester;
        final String _testkindcd;
        final String _testitemcd;
        final String _scoreDiv;
        final String _testitemname;
        final String _testitemabbv1;
        final String _sidouInput;
        final String _sidouInputInf;
        final SemesterDetail _semesterDetail;
        boolean _isGakunenKariHyotei;
        int _printKettenFlg; // -1: 表示しない（仮評定）、1: 値が1をカウント（学年評定）、2:換算した値が1をカウント（評価等）
        public TestItem(final String year, final Semester semester, final String testkindcd, final String testitemcd, final String scoreDiv,
                final String testitemname, final String testitemabbv1, final String sidouInput, final String sidouInputInf,
                final SemesterDetail semesterDetail) {
            _year = year;
            _semester = semester;
            _testkindcd = testkindcd;
            _testitemcd = testitemcd;
            _scoreDiv = scoreDiv;
            _testitemname = testitemname;
            _testitemabbv1 = testitemabbv1;
            _sidouInput = sidouInput;
            _sidouInputInf = sidouInputInf;
            _semesterDetail = semesterDetail;
        }
        public String getTestcd() {
            return _semester._semester +_testkindcd +_testitemcd + _scoreDiv;
        }
        public String toString() {
            return "TestItem(" + _semester._semester + _testkindcd + _testitemcd + "(" + _scoreDiv + "))";
        }
    }

    private static class Semester {
        final String _semester;
        final String _semestername;
        final DateRange _dateRange;
        final List _testItemList;
        final List _semesterDetailList;
        public Semester(final String semester, final String semestername, final String sdate, final String edate) {
            _semester = semester;
            _semestername = semestername;
            _dateRange = new DateRange(_semester, _semestername, sdate, edate);
            _testItemList = new ArrayList();
            _semesterDetailList = new ArrayList();
        }
        public int getTestItemIdx(final TestItem testItem) {
            return _testItemList.indexOf(testItem);
        }
        public int getSemesterDetailIdx(final SemesterDetail semesterDetail) {
//          log.debug(" semesterDetail = " + semesterDetail + " , " + _semesterDetailList.indexOf(semesterDetail));
          return _semesterDetailList.indexOf(semesterDetail);
        }

        public int compareTo(final Object o) {
            if (!(o instanceof Semester)) {
                return 0;
            }
            Semester s = (Semester) o;
            return _semester.compareTo(s._semester);
        }
    }

    private static class DateRange {
        final String _key;
        final String _name;
        final String _sdate;
        final String _edate;
        public DateRange(final String key, final String name, final String sdate, final String edate) {
            _key = key;
            _name = name;
            _sdate = sdate;
            _edate = edate;
        }
        public String toString() {
            return "DateRange(" + _key + ", " + _sdate + ", " + _edate + ")";
        }
    }

    private static class SubclassMst implements Comparable<SubclassMst> {
        final String _specialDiv;
        final String _classcd;
        final String _subclasscd;
        final String _classabbv;
        final String _classname;
        final String _subclassname;
        final Integer _classShoworder3;
        final Integer _subclassShoworder3;
        final String _calculateCreditFlg;
        String _combined = null;
        List<String> _attendSubclassList = new ArrayList();
        public SubclassMst(final String specialDiv, final String classcd, final String subclasscd, final String classabbv, final String classname, final String subclassabbv, final String subclassname,
                final Integer classShoworder3,
                final Integer subclassShoworder3,
                final String calculateCreditFlg) {
            _specialDiv = specialDiv;
            _classcd = classcd;
            _subclasscd = subclasscd;
            _classabbv = classabbv;
            _classname = classname;
            _subclassname = subclassname;
            _classShoworder3 = classShoworder3;
            _subclassShoworder3 = subclassShoworder3;
            _calculateCreditFlg = calculateCreditFlg;
        }
        public boolean isMoto() {
            return null != _combined;
        }
        public boolean isSaki() {
            return !_attendSubclassList.isEmpty();
        }
        public int compareTo(final SubclassMst mst) {
            int rtn;
            rtn = _classShoworder3.compareTo(mst._classShoworder3);
            if (0 != rtn) { return rtn; }
            if (null == _classcd && null == mst._classcd) {
                return 0;
            } else if (null == _classcd) {
                return 1;
            } else if (null == mst._classcd) {
                return -1;
            }
            rtn = _classcd.compareTo(mst._classcd);
            if (0 != rtn) { return rtn; }
            rtn = _subclassShoworder3.compareTo(mst._subclassShoworder3);
            if (0 != rtn) { return rtn; }
            if (null == _subclasscd && null == mst._subclasscd) {
                return 0;
            } else if (null == _subclasscd) {
                return 1;
            } else if (null == mst._subclasscd) {
                return -1;
            }
            return _subclasscd.compareTo(mst._subclasscd);
        }
        public String toString() {
            return "SubclassMst(subclasscd = " + _subclasscd + ")";
        }
    }



    protected Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        log.fatal("$Revision: 75852 $ $Date: 2020-08-05 14:26:42 +0900 (水, 05 8 2020) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        Param param = new Param(request, db2);
        return param;
    }

    private static class Param {
        private final String _year;
        private final String _ctrlSemester;
        private final String _selectSemester;
        private final String _ctrlDate;
        String _ctrlDate2;
        private final String _useVirus;
        private final String _useKoudome;
        private final String _gradeHrClass;
        private final String _grade;
        private final String _attendSdate;
        private final String _attendEdate;
        private final String _edate;
        private final String _disp;

        private String _selectedIn = "";

        private KNJSchoolMst _knjSchoolMst;

        /** 端数計算共通メソッド引数 */
        private final Map _attendParamMap;

        private Map _semesterMap;
        private final List _semesterList;
        private final Map _semesterDetailMap;
        private Map _attendRanges;
        private Map<String, SubclassMst> _subclassMstMap;

        Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("YEAR");
            _ctrlSemester  = request.getParameter("CTRL_SEMESTER");
            _selectSemester  = request.getParameter("SEMESTER");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
            _disp = request.getParameter("DISP");
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            if ("1".equals(_disp)) {
                _grade = request.getParameter("GRADE");
            } else {
                _grade = _gradeHrClass.substring(0, 2);
            }

            _attendSdate = null == request.getParameter("ATTEND_SDATE") ? null : request.getParameter("ATTEND_SDATE").replace('/', '-');
            _attendEdate = null == request.getParameter("ATTEND_EDATE") ? null : request.getParameter("ATTEND_EDATE").replace('/', '-');
            _ctrlDate = null == request.getParameter("CTRL_DATE") ? null : request.getParameter("CTRL_DATE").replace('/', '-');

            _edate = null == request.getParameter("EDATE") ? null : request.getParameter("EDATE").replace('/', '-');

            try {
                SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date date = sdFormat.parse(_ctrlDate);
                sdFormat.applyPattern("yyyy年M月d日");
                _ctrlDate2 = sdFormat.format(date);
            } catch(ParseException e) {
                e.printStackTrace();
            }
            //_attendEdate = _ctrlDate;
            final String[] categorySelected = request.getParameterValues("CATEGORY_SELECTED"); // 学年＋組
            _selectedIn = "(";
            for (int i = 0; i < categorySelected.length; i++) {
                if (categorySelected[i] == null)
                    break;
                if (i > 0)
                    _selectedIn = _selectedIn + ",";
                _selectedIn = _selectedIn + "'" + categorySelected[i] + "'";
            }
            _selectedIn = _selectedIn + ")";


            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (Exception ex) {
                log.error("Param load exception!", ex);
            }

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _grade);
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
            _attendParamMap.put("useCurriculumcd", "1");

            _semesterList = getSemesterList(db2);
            _semesterMap = loadSemester(db2);
            _semesterDetailMap = new HashMap();
            _attendRanges = new HashMap();
            for (final Iterator it = _semesterMap.keySet().iterator(); it.hasNext();) {
                final String semester = (String) it.next();
                final Semester oSemester = (Semester) _semesterMap.get(semester);
                _attendRanges.put(semester, oSemester._dateRange);
            }

            setSubclassMst(db2);
        }

        private List getSemesterList(DB2UDB db2) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT * FROM SEMESTER_MST WHERE YEAR = '" + _year + "' ORDER BY SEMESTER ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String semestername = rs.getString("SEMESTERNAME");
                    final String sdate = rs.getString("SDATE");
                    final String edate = rs.getString("EDATE");
                    Semester semes = new Semester(semester, semestername, sdate, edate);
                    list.add(semes);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        /**
         * 年度の開始日を取得する
         */
        private Map loadSemester(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map map = new TreeMap();
            try {
                final String sql = "select"
                        + "   SEMESTER,"
                        + "   SEMESTERNAME,"
                        + "   SDATE,"
                        + "   EDATE"
                        + " from"
                        + "   V_SEMESTER_GRADE_MST"
                        + " where"
                        + "   YEAR='" + _year + "'"
                        + "   AND GRADE='" + _grade + "'"
                        + " order by SEMESTER"
                    ;

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    map.put(rs.getString("SEMESTER"), new Semester(rs.getString("SEMESTER"), rs.getString("SEMESTERNAME"), rs.getString("SDATE"), rs.getString("EDATE")));
                }
            } catch (final Exception ex) {
                log.error("テスト項目のロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return map;
        }


        private void setSubclassMst(
                final DB2UDB db2
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _subclassMstMap = new HashMap();
            try {
                String sql = "";
                sql += " SELECT ";
                sql += " VALUE(T2.SPECIALDIV, '0') AS SPECIALDIV, ";
                sql += " T1.CLASSCD, ";
                sql += " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
                sql += " T2.CLASSABBV, VALUE(T2.CLASSORDERNAME2, T2.CLASSNAME) AS CLASSNAME, T1.SUBCLASSABBV, VALUE(T1.SUBCLASSORDERNAME2, T1.SUBCLASSNAME) AS SUBCLASSNAME, ";
                sql += " COMB1.CALCULATE_CREDIT_FLG, ";
                sql += " VALUE(T2.SHOWORDER3, 999) AS CLASS_SHOWORDER3, ";
                sql += " VALUE(T1.SHOWORDER3, 999) AS SUBCLASS_SHOWORDER3, ";
                sql += " ATT1.COMBINED_CLASSCD || '-' || ATT1.COMBINED_SCHOOL_KIND || '-' || ATT1.COMBINED_CURRICULUM_CD || '-' || ATT1.COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD, ";
                sql += " COMB1.ATTEND_CLASSCD || '-' || COMB1.ATTEND_SCHOOL_KIND || '-' || COMB1.ATTEND_CURRICULUM_CD || '-' || COMB1.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD ";
                sql += " FROM SUBCLASS_MST T1 ";
                sql += " LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
                sql += " LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT ATT1 ON ATT1.YEAR = '" + _year + "' AND ATT1.ATTEND_CLASSCD = T1.CLASSCD AND ATT1.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND AND ATT1.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD AND ATT1.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ";
                sql += " LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT COMB1 ON COMB1.YEAR = '" + _year + "' AND COMB1.COMBINED_CLASSCD = T1.CLASSCD AND COMB1.COMBINED_SCHOOL_KIND = T1.SCHOOL_KIND AND COMB1.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD AND COMB1.COMBINED_SUBCLASSCD = T1.SUBCLASSCD ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    if (!_subclassMstMap.containsKey(rs.getString("SUBCLASSCD"))) {
                        final SubclassMst mst = new SubclassMst(rs.getString("SPECIALDIV"), rs.getString("CLASSCD"), rs.getString("SUBCLASSCD"), rs.getString("CLASSABBV"), rs.getString("CLASSNAME"), rs.getString("SUBCLASSABBV"), rs.getString("SUBCLASSNAME"), new Integer(rs.getInt("CLASS_SHOWORDER3")), new Integer(rs.getInt("SUBCLASS_SHOWORDER3")), rs.getString("CALCULATE_CREDIT_FLG"));
                        _subclassMstMap.put(rs.getString("SUBCLASSCD"), mst);
                    }
                    final SubclassMst mst = _subclassMstMap.get(rs.getString("SUBCLASSCD"));
                    if(rs.getString("COMBINED_SUBCLASSCD") != null) {
                        mst._combined = rs.getString("COMBINED_SUBCLASSCD");
                    }
                    if(rs.getString("ATTEND_SUBCLASSCD") != null) {
                        mst._attendSubclassList.add(rs.getString("ATTEND_SUBCLASSCD"));
                    }

                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
    }

}
