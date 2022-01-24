/*
 * $Id: ce2863f7017905cca4858f54d4422ee034b0c279 $
 *
 * 作成日: 2019/06/11
 * 作成者: yamashiro
 *
 * Copyright(C) 2019-2012 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

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
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 学校教育システム 賢者 [成績管理] 中学通知表
 */

public class KNJD185M {

    private static final Log log = LogFactory.getLog(KNJD185M.class);
    private static final String SEME1 = "1";
    private static final String SEME2 = "2";
    private static final String SEME3 = "3";
    private static final String SEMEALL = "9";

    private static final String CLASSCD_TOTALSTDY = "90";
    private static final String CLASSCD_MORAL = "28";
    private boolean _hasData;

    private Param _param;

    private static final String STATUS = "STATUS_NAME1";

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

        final List studentList = Student.getStudentList(db2, _param);

        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();

            log.info(" student = " + student._schregno);

            if (_param._hyoushiPrint) {
                printHyoushi(db2, svf, student);
                printUraHyoushi(db2, svf, student);
            } else {
                svf.VrSetForm("KNJD185M_2_1.frm", 4);
                printStudent(db2, svf, student, "SEISEKI");

                svf.VrSetForm("KNJD185M_2_2.frm", 1);
                printStudent(db2, svf, student, "SYOKEN");
            }
        }
    }

    private void printHyoushi(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        final String form = "KNJD185M_1_1.frm";
        svf.VrSetForm(form, 1);

        svf.VrsOut("NENDO", _param._year + "年度");

        final String putHrName = student.getHrname();
        svf.VrsOut("HR_NAME", putHrName);
        final String nameField = KNJ_EditEdit.getMS932ByteLength(student._name) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(student._name) > 20 ? "2" : "1";
        svf.VrsOut("NAME" + nameField, student._name);
        svf.VrsOut("STAFF_NAME", student._staffname);

        svf.VrsOut("SCHOOL_LOGO", _param._schoolLogoImagePath);
        svf.VrsOut("SCHOOL_NAME", _param._certifSchoolSchoolName);

        svf.VrsOut("LINE_UL", _param.getImageFilePath("SEISEN_LINE_UL.jpg"));
        svf.VrsOut("LINE_UR", _param.getImageFilePath("SEISEN_LINE_UR.jpg"));
        svf.VrsOut("LINE_DL", _param.getImageFilePath("SEISEN_LINE_DL.jpg"));
        svf.VrsOut("LINE_DR", _param.getImageFilePath("SEISEN_LINE_DR.jpg"));
        for (int i = 1; i <= 5; i++) {
            svf.VrsOutn("LINE_V1", i, _param.getImageFilePath("SEISEN_LINE_V.jpg"));
            svf.VrsOutn("LINE_V2", i, _param.getImageFilePath("SEISEN_LINE_V.jpg"));
        }
        for (int i = 1; i <= 3; i++) {
            svf.VrsOutn("LINE_S1", i, _param.getImageFilePath("SEISEN_LINE_S.jpg"));
            svf.VrsOutn("LINE_S2", i, _param.getImageFilePath("SEISEN_LINE_S.jpg"));
        }
        svf.VrEndPage();
        _hasData = true;
    }

    private void printUraHyoushi(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        final String form = "KNJD185M_1_2.frm";
        svf.VrSetForm(form, 1);

        svf.VrsOut("NENDO", _param._year + "年度");

        final String putHrName = student.getHrname();
        svf.VrsOut("HR_NAME", putHrName);
        final String nameField = KNJ_EditEdit.getMS932ByteLength(student._name) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(student._name) > 20 ? "2" : "1";
        svf.VrsOut("NAME" + nameField, student._name);
        String printGrade = String.valueOf(Integer.parseInt(_param._gradeCdStr));
        svf.VrsOut("GRADE", "第" + printGrade + "学年");
        final String printDate = (Integer.parseInt(_param._year) + 1) + "-03-31";
        svf.VrsOut("DATE", KNJ_EditDate.h_format_SeirekiJP(printDate));
        svf.VrsOut("SCHOOL_NAME", _param._certifSchoolSchoolName + "　校長　" + _param._certifSchoolPrincipalName + "　　印");

        svf.VrEndPage();
        _hasData = true;
    }

    private void printStudent(final DB2UDB db2, final Vrw32alp svf, final Student student, final String PrintDiv) {
        final Semester semester = (Semester) _param._semesterMap.get(_param._semester);
        svf.VrsOut("TITLE", "成績通知表　(" + _param._year + "年度　" + semester._semesterName + ")");

        svf.VrsOut("SCHREGNO", student._schregno);
        final String putHrName = student.getHrname();
        svf.VrsOut("HR_NAME", putHrName);
        final String nameField = KNJ_EditEdit.getMS932ByteLength(student._name) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(student._name) > 20 ? "2" : "1";
        svf.VrsOut("NAME" + nameField, student._name);

        if ("SEISEKI".equals(PrintDiv)) {
            printSeiseki(svf, student);
        }
        if ("SYOKEN".equals(PrintDiv)) {
            printShoken(svf, student);
            printAttendance(svf, student);
            svf.VrEndPage();
        }

        _hasData = true;
    }

    private static Map getMappedMap(final Map map, final String key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap());
        }
        return (Map) map.get(key1);
    }

    private void printSeiseki(final Vrw32alp svf, final Student student) {
        //1.学習の記録
        boolean printLineFlg = false;
        for (int i = 0; i < Math.min(student._viewClassList.size(), 10); i++) {
            final ViewClass vc = (ViewClass) student._viewClassList.get(i);
            if (CLASSCD_TOTALSTDY.equals(vc._classcd)) {
                continue;
            }
            int nclsnamelen = KNJ_EditEdit.getMS932ByteLength(vc._classname);
            final String[] token = KNJ_EditEdit.get_token(vc._classname, 2, (int)Math.ceil((double)nclsnamelen / 2.0));
            int vi = 0;
            for (vi = 0; vi <  vc.getViewSize(); vi++) {
                svf.VrsOut("GRPCD", String.valueOf(i));
                if (null != token) {
                    if (vi < token.length) {
                        svf.VrsOut("CLASS_NAME", token[vi]); // 教科名
                    }
                }
                final String viewcd = vc.getViewCd(vi);
                svf.VrsOut("VIEW_NAME", vc.getViewName(vi)); // 観点
                final Map stat1 = getMappedMap(getMappedMap(vc._viewcdSemesterStatDatMap, viewcd), "1");
                if (stat1 != null) {
                    svf.VrsOut("EVA1", StringUtils.defaultString(KnjDbUtils.getString(stat1, STATUS), "")); // 評価
                }
                final Map stat2 = getMappedMap(getMappedMap(vc._viewcdSemesterStatDatMap, viewcd), "2");
                if (stat2 != null) {
                    svf.VrsOut("EVA2", StringUtils.defaultString(KnjDbUtils.getString(stat2, STATUS), "")); // 評価
                }
                final Map stat9 = getMappedMap(getMappedMap(vc._viewcdSemesterStatDatMap, viewcd), "9");
                if (stat9 != null) {
                    svf.VrsOut("EVA9", StringUtils.defaultString(KnjDbUtils.getString(stat9, STATUS), "")); // 評価
                }
                svf.VrsOut("VAL1", StringUtils.defaultString((String)vc._semesterScoreMap.get("1"), "")); // 評定
                svf.VrsOut("VAL2", StringUtils.defaultString((String)vc._semesterScoreMap.get("2"), "")); // 評定
                svf.VrsOut("VAL9", StringUtils.defaultString((String)vc._semesterScoreMap.get("9"), "")); // 評定
                svf.VrEndRecord();
            }
            if (vi < token.length) {
                for (;vi < token.length;vi++) {
                    svf.VrsOut("GRPCD", String.valueOf(i));
                    svf.VrsOut("CLASS_NAME", token[vi]); // 教科名
                    svf.VrEndRecord();
                }
            }
            //svf.VrEndRecord();
            printLineFlg = true;
        }
        //表の出力が無い場合でも他の出力があれば帳票出力しないといけないので、設定。
        if (!printLineFlg) {
            svf.VrEndRecord();
        }
    }

    private void printAttendance(final Vrw32alp svf, final Student student) {
        final String[] seme = {SEME1, SEME2, SEME3, SEMEALL};
        for (int semei = 0; semei < seme.length; semei++) {
            final int line = semei + 1;
            if (!_param._isLastSemester && Integer.parseInt(seme[semei]) > Integer.parseInt(_param._semester)) {
                continue;
            }

            //出欠
            final AttendSemesDat att = (AttendSemesDat) student._attendSemesDatMap.get(seme[semei]);
            if (null != att) {
                svf.VrsOutn("LESSON", line, String.valueOf(att._lesson)); // 授業日数
                svf.VrsOutn("SUSPEND", line, String.valueOf(att._suspend + att._mourning + att._virus + att._koudome)); // 出停・忌引
                svf.VrsOutn("ABROAD", line, String.valueOf(att._transferDate)); // 留学中
                svf.VrsOutn("MUST", line, String.valueOf(att._mlesson)); // 出席しなければならない日数
                svf.VrsOutn("ABSENT", line, String.valueOf(att._sick)); // 欠席日数
                svf.VrsOutn("LATE", line, String.valueOf(att._late)); // 遅刻
                svf.VrsOutn("EARLY", line, String.valueOf(att._early)); // 早退
            }
            //出欠備考
            final AttendReasonCollection attReason = (AttendReasonCollection) student._attendReasonCollectionMap.get(seme[semei]);
            if (null != attReason) {
                final List attReasonList = KNJ_EditKinsoku.getTokenList(attReason._attendRemark, 54);
                int reasonLine = 1;
                for (Iterator itAttReason = attReasonList.iterator(); itAttReason.hasNext();) {
                    final String reason = (String) itAttReason.next();
                    svf.VrsOutn("ATTEND_REMARK" + line, reasonLine, reason);
                    reasonLine++;
                }
            }
        }
    }

    private void printShoken(final Vrw32alp svf, final Student student) {
        //統合的な学習観点
    	int outcnt = 0;
    	int outcntmax = 4;
        for (int i = 0; i < student._viewClassList.size(); i++) {
            final ViewClass vc = (ViewClass) student._viewClassList.get(i);
            if (!CLASSCD_TOTALSTDY.equals(vc._classcd)) {
                continue;
            }
            int vi = 0;
            for (vi = 0; vi <  vc.getViewSize(); vi++) {
                final String viewcd = vc.getViewCd(vi);
                svf.VrsOutn("VIEW_NAME", vi + 1, vc.getViewName(vi)); // 観点
                final Map stat1 = getMappedMap(getMappedMap(vc._viewcdSemesterStatDatMap, viewcd), "1");
                if (stat1 != null) {
                    svf.VrsOutn("EVA1", vi + 1, StringUtils.defaultString(KnjDbUtils.getString(stat1, STATUS), "")); // 評価
                }
                final Map stat2 = getMappedMap(getMappedMap(vc._viewcdSemesterStatDatMap, viewcd), "2");
                if (stat2 != null) {
                    svf.VrsOutn("EVA2", vi + 1, StringUtils.defaultString(KnjDbUtils.getString(stat2, STATUS), "")); // 評価
                }
                final Map stat9 = getMappedMap(getMappedMap(vc._viewcdSemesterStatDatMap, viewcd), "9");
                if (stat9 != null) {
                    svf.VrsOutn("EVA9", vi + 1, StringUtils.defaultString(KnjDbUtils.getString(stat9, STATUS), "")); // 評価
                }
                outcnt++;
            }
            if (outcnt >= outcntmax) {
            	break;
            }
        }
        //統合的な学習所見
        for (Iterator itRemark = student._recordTotalStudyTimeMap.keySet().iterator(); itRemark.hasNext();) {
            final String semester = (String) itRemark.next();
            final RecordTotalStudy recordTotalStudy = (RecordTotalStudy) student._recordTotalStudyTimeMap.get(semester);

            svf.VrsOut("TOTAL_ACT" + semester + "_1", recordTotalStudy._ta1_totalstudyact);
        }
        //特別活動
        for (Iterator itRemark = student._hReportRemarkDatMap.keySet().iterator(); itRemark.hasNext();) {
            final String semester = (String) itRemark.next();
            final HReportRemarkDat reportRemarkDat = (HReportRemarkDat) student._hReportRemarkDatMap.get(semester);

            if (semester.equals(_param._semester)) {
                printHreportText(svf, reportRemarkDat._detail01r1, "SP_ACT1");
                printHreportText(svf, reportRemarkDat._detail02r1, "SP_ACT2");
                printHreportText(svf, reportRemarkDat._detail04r1, "SP_ACT3");
                printHreportText(svf, reportRemarkDat._detail03r1, "SP_ACT4");
                printHreportText(svf, reportRemarkDat._detail05r1, "SP_ACT5");
            }
            if (Integer.parseInt(semester) <= Integer.parseInt(_param._semester)) {
                svf.VrsOut("ACTION" + semester + "_1", reportRemarkDat._communication);
            }
        }
    }

    private void printHreportText(final Vrw32alp svf, String setText, String textField) {
        final List printList = KNJ_EditKinsoku.getTokenList(setText, 20);
        int textCnt = 1;
        for (Iterator itText = printList.iterator(); itText.hasNext();) {
            final String printText = (String) itText.next();
            svf.VrsOutn(textField, textCnt, printText);
            textCnt++;
        }
    }

    private static class Student {
        final String _schregno;
        final String _name;
        final String _gradeCd;
        final String _hrName;
        final String _hrClassName1;
        final String _attendno;
        final String _staffname;
        final String _staffname2;
        final List _viewClassList = new ArrayList(); //観点情報

        Map _attendSemesDatMap = Collections.EMPTY_MAP; // 出欠の記録
        Map _attendReasonCollectionMap = Collections.EMPTY_MAP; // 出欠の備考
        Map _recordTotalStudyTimeMap = Collections.EMPTY_MAP; // 総合学習所見
        Map _hReportRemarkDatMap = Collections.EMPTY_MAP; // 通知表所見

        public Student(final String schregno, final String name, final String gradeCd, final String hrName, final String hrClassName1, final String attendno, final String staffname, final String staffname2) {
            _schregno = schregno;
            _name = name;
            _gradeCd = gradeCd;
            _hrName = hrName;
            _hrClassName1 = hrClassName1;
            _attendno = attendno;
            _staffname = staffname;
            _staffname2 = staffname2;
        }

        public String getHrname() {
            final String gradeCd = null == _gradeCd ? "" : String.valueOf(Integer.parseInt(_gradeCd));
            final String attendno = null == _attendno ? "" : String.valueOf(Integer.parseInt(_attendno));
            final String retStr = gradeCd + "年 " + StringUtils.defaultString(_hrClassName1) + "組 " + attendno + "番";
            return retStr;
        }

        private static List getStudentList(final DB2UDB db2, final Param param) {
            final List studentList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getStudentSql(param);
                log.info(" regd sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("SCHREGNO");

                    final String name = "1".equals(rs.getString("USE_REAL_NAME")) ? rs.getString("REAL_NAME") : rs.getString("NAME");
                    final String gradeCd = rs.getString("GRADE_CD");
                    final String hrName = rs.getString("HR_NAME");
                    final String hrClassName1 = rs.getString("HR_CLASS_NAME1");
                    final String attendno = rs.getString("ATTENDNO");
                    final String staffname = rs.getString("STAFFNAME");
                    final String staffname2 = rs.getString("STAFFNAME2");
                    final Student student = new Student(schregno, name, gradeCd, hrName, hrClassName1, attendno, staffname, staffname2);
                    studentList.add(student);
                }

            } catch (SQLException e) {
                log.error("exception!!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            AttendSemesDat.setAttendSemesDatList(db2, param, studentList);
            AttendReasonCollection.setAttendReasonCollectionMap(db2, param, studentList);
            ViewClass.setViewClassList(db2, param, studentList);
            RecordTotalStudy.setRecordTotalStudy(db2, param, studentList);
            HReportRemarkDat.setHreportData(db2, param, studentList);
            return studentList;
        }

        private static String getStudentSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            // 異動者を除外した学籍の表 => 組平均において異動者の除外に使用
            stb.append("WITH SCHNO_A AS(");
            stb.append("    SELECT ");
            stb.append("        T1.SCHREGNO, ");
            stb.append("        T1.GRADE, ");
            stb.append("        T1.HR_CLASS, ");
            stb.append("        T1.ATTENDNO, ");
            stb.append("        T1.SEMESTER, ");
            stb.append("        T1.COURSECD, ");
            stb.append("        T1.MAJORCD, ");
            stb.append("        T1.COURSECODE ");
            stb.append("    FROM ");
            stb.append("        SCHREG_REGD_DAT T1, ");
            stb.append("        V_SEMESTER_GRADE_MST T2 ");
            stb.append("    WHERE ");
            stb.append("        T1.YEAR = '" + param._year + "' ");
            stb.append("        AND T1.SEMESTER = '"+ param._semester +"' ");
            stb.append("        AND T1.YEAR = T2.YEAR ");
            stb.append("        AND T1.GRADE = T2.GRADE ");
            stb.append("        AND T1.SEMESTER = T2.SEMESTER ");
            stb.append("        AND T1.GRADE || T1.HR_CLASS = '" + param._gradeHrclass + "' ");
            stb.append("        AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            stb.append("    ) ");
            //メイン表
            stb.append("SELECT  SCHNO_A.SCHREGNO, ");
            stb.append("        GDAT.GRADE_CD, ");
            stb.append("        REGDH.HR_NAME, ");
            stb.append("        REGDH.HR_CLASS_NAME1, ");
            stb.append("        SCHNO_A.ATTENDNO, ");
            stb.append("        BASE.NAME, ");
            stb.append("        BASE.REAL_NAME, ");
            stb.append("        CASE WHEN NMSETUP.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS USE_REAL_NAME, ");
            stb.append("        STAFF1.STAFFNAME, ");
            stb.append("        STAFF2.STAFFNAME AS STAFFNAME2 ");
            stb.append("FROM    SCHNO_A ");
            stb.append("        INNER JOIN SCHREG_BASE_MST BASE ON SCHNO_A.SCHREGNO = BASE.SCHREGNO ");
            stb.append("        LEFT JOIN SCHREG_NAME_SETUP_DAT NMSETUP ON NMSETUP.SCHREGNO = SCHNO_A.SCHREGNO ");
            stb.append("             AND NMSETUP.DIV = '03' ");
            stb.append("        LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = '" + param._year + "' ");
            stb.append("             AND REGDH.SEMESTER = SCHNO_A.SEMESTER ");
            stb.append("             AND REGDH.GRADE    = SCHNO_A.GRADE ");
            stb.append("             AND REGDH.HR_CLASS = SCHNO_A.HR_CLASS ");
            stb.append("        LEFT JOIN SCHREG_REGD_GDAT GDAT ON REGDH.YEAR = GDAT.YEAR ");
            stb.append("             AND REGDH.GRADE = GDAT.GRADE ");
            stb.append("        LEFT JOIN STAFF_MST STAFF1 ON STAFF1.STAFFCD = REGDH.TR_CD1 ");
            stb.append("        LEFT JOIN STAFF_MST STAFF2 ON STAFF2.STAFFCD = REGDH.TR_CD2 ");
            stb.append("ORDER BY ATTENDNO");
            return stb.toString();
        }
    }

    /**
     * 観点の教科
     */
    private static class ViewClass {
        final String _classcd;
        final String _subclasscd;
        final String _electDiv;
        final String _classname;
        final String _subclassname;
        final List _viewList;
        final Map _semesterScoreMap = new HashMap();
        final Map _viewcdSemesterStatDatMap = new HashMap();

        ViewClass(
                final String classcd,
                final String subclasscd,
                final String electDiv,
                final String classname,
                final String subclassname) {
            _classcd = classcd;
            _subclasscd = subclasscd;
            _electDiv = electDiv;
            _classname = classname;
            _subclassname = subclassname;
            _viewList = new ArrayList();
        }

        public void addView(final String viewcd, final String viewname) {
            _viewList.add(new String[]{viewcd, viewname});
        }

        public String getViewCd(final int i) {
            return ((String[]) _viewList.get(i))[0];
        }

        public String getViewName(final int i) {
            return ((String[]) _viewList.get(i))[1];
        }

        public int getViewSize() {
            return _viewList.size();
        }

        public String toString() {
            return "ViewClass(" + _subclasscd + ", " + _classname + ")";
        }

        public static void setViewClassList(final DB2UDB db2, final Param param, final List studentList) {
            final String sql = getViewClassSql(param);
            if (param._isOutputDebug) {
                log.info(" view class sql = " + sql);
            }

            PreparedStatement ps = null;
            try {
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    student._viewClassList.clear();

                    for (final Iterator rit = KnjDbUtils.query(db2, ps, new Object[] {student._schregno, student._schregno, student._schregno, student._schregno}).iterator(); rit.hasNext();) {
                        final Map row = (Map) rit.next();

                        final String classcd = KnjDbUtils.getString(row, "CLASSCD");
                        final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                        final String viewcd = KnjDbUtils.getString(row, "VIEWCD");

                        ViewClass viewClass = null;
                        for (final Iterator vit = student._viewClassList.iterator(); vit.hasNext();) {
                            final ViewClass viewClass0 = (ViewClass) vit.next();
                            if (viewClass0._subclasscd.equals(subclasscd)) {
                                viewClass = viewClass0;
                                break;
                            }
                        }

                        if (null == viewClass) {
                            final String electDiv = KnjDbUtils.getString(row, "ELECTDIV");
                            final String classname = KnjDbUtils.getString(row, "CLASSNAME");
                            final String subclassname = KnjDbUtils.getString(row, "SUBCLASSNAME");

                            viewClass = new ViewClass(classcd, subclasscd, electDiv, classname, subclassname);
                            student._viewClassList.add(viewClass);
                        }

                        final String viewname = KnjDbUtils.getString(row, "VIEWNAME");
                        if (viewname != null && !"".equals(viewname)) {
                            boolean findflg = false;
                            for (int ii = 0;ii < viewClass.getViewSize();ii++) {
                                if (viewcd.equals(viewClass.getViewCd(ii))) {
                                    findflg = true;
                                }
                            }
                            if (!findflg) {
                                viewClass.addView(viewcd, viewname);
                            }
                        }

                        final String semester = KnjDbUtils.getString(row, "SEMESTER");
                        if (null == semester) {
                            continue;
                        }
                        if (!param._isLastSemester && Integer.parseInt(semester) > Integer.parseInt(param._semester)) {
                            continue;
                        }
                        viewClass._semesterScoreMap.put(semester, KnjDbUtils.getString(row, "SCORE"));

                        final Map stat = getMappedMap(getMappedMap(viewClass._viewcdSemesterStatDatMap, viewcd), semester);
                        stat.put(STATUS, KnjDbUtils.getString(row, STATUS));
                    }
                }

            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps);
            }
        }

        private static String getViewClassSql(final Param param) {

            final StringBuffer stb = new StringBuffer();

            stb.append(" WITH FILTSEMS AS ( ");
            stb.append(" SELECT DISTINCT ");
            stb.append("  T1.YEAR, ");
            stb.append("  T1.SEMESTER ");
            stb.append(" FROM ");
            stb.append("  SEMESTER_MST T1 ");
            stb.append(" WHERE ");
            stb.append("  T1.YEAR = '" + param._year + "' ");
            stb.append("  AND T1.SEMESTER IN ( ");
            stb.append("      SELECT ");
            stb.append("        REC.SEMESTER ");
            stb.append("      FROM ");
            stb.append("      JVIEWNAME_GRADE_MST T1 ");
            stb.append("      INNER JOIN JVIEWNAME_GRADE_YDAT T2 ON T2.YEAR = '" + param._year + "' ");
            stb.append("          AND T2.GRADE = T1.GRADE ");
            stb.append("          AND T2.CLASSCD = T1.CLASSCD  ");
            stb.append("          AND T2.SCHOOL_KIND = T1.SCHOOL_KIND  ");
            stb.append("          AND T2.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            stb.append("          AND T2.SUBCLASSCD = T1.SUBCLASSCD  ");
            stb.append("          AND T2.VIEWCD = T1.VIEWCD ");
            stb.append("      INNER JOIN CLASS_MST CLM ON CLM.CLASSCD = T1.CLASSCD ");
            stb.append("          AND CLM.SCHOOL_KIND = T1.SCHOOL_KIND  ");
            stb.append("      LEFT JOIN JVIEWSTAT_RECORD_DAT REC ON REC.SUBCLASSCD = T2.SUBCLASSCD ");
            stb.append("          AND REC.CLASSCD = T2.CLASSCD  ");
            stb.append("          AND REC.SCHOOL_KIND = T2.SCHOOL_KIND  ");
            stb.append("          AND REC.CURRICULUM_CD = T2.CURRICULUM_CD  ");
            stb.append("          AND REC.VIEWCD = T2.VIEWCD ");
            stb.append("          AND REC.YEAR = T2.YEAR ");
            stb.append("          AND REC.SCHREGNO = ? ");
            stb.append("      WHERE ");
            stb.append("          T1.GRADE = '" + param._grade + "' ");
            stb.append("          AND T1.SCHOOL_KIND = '" + param._schoolkind + "' ");
            stb.append("          AND REC.SEMESTER IS NOT NULL ");
            stb.append("      UNION ");
            stb.append("      SELECT ");
            stb.append("        T10.SEMESTER ");
            stb.append("      FROM ");
            stb.append("      JVIEWNAME_GRADE_MST T1 ");
            stb.append("      INNER JOIN JVIEWNAME_GRADE_YDAT T2 ON T2.YEAR = '" + param._year + "' ");
            stb.append("          AND T2.GRADE = T1.GRADE ");
            stb.append("          AND T2.CLASSCD = T1.CLASSCD  ");
            stb.append("          AND T2.SCHOOL_KIND = T1.SCHOOL_KIND  ");
            stb.append("          AND T2.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            stb.append("          AND T2.SUBCLASSCD = T1.SUBCLASSCD  ");
            stb.append("          AND T2.VIEWCD = T1.VIEWCD ");
            stb.append("      INNER JOIN CLASS_MST CLM ON CLM.CLASSCD = T1.CLASSCD ");
            stb.append("          AND CLM.SCHOOL_KIND = T1.SCHOOL_KIND  ");
            stb.append("      LEFT JOIN RECORD_SCORE_DAT T10 ON T10.YEAR = T2.YEAR ");
            stb.append("          AND T10.TESTKINDCD = '99' ");
            stb.append("          AND T10.TESTITEMCD = '00' ");
            stb.append("          AND T10.SCORE_DIV = '08' ");
            stb.append("          AND T10.CLASSCD = T1.CLASSCD ");
            stb.append("          AND T10.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("          AND T10.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("          AND T10.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("          AND T10.SCHREGNO = ? ");
            stb.append("      WHERE ");
            stb.append("          T1.GRADE = '" + param._grade + "' ");
            stb.append("          AND T1.SCHOOL_KIND = '" + param._schoolkind + "' ");
            stb.append("          AND T10.SEMESTER IS NOT NULL ");
            stb.append("     ) ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.GRADE ");
            stb.append("   , CLM.CLASSCD ");
            stb.append("   , VALUE(CLM.CLASSORDERNAME2, CLM.CLASSNAME) AS CLASSNAME ");
            stb.append("   , VALUE(SCLM.SUBCLASSORDERNAME2, SCLM.SUBCLASSNAME) AS SUBCLASSNAME ");
            stb.append("   , T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("   , VALUE(SCLM.ELECTDIV, '0') AS ELECTDIV ");
            stb.append("   , T1.VIEWCD ");
            stb.append("   , T1.VIEWNAME ");
            stb.append("   , CASE WHEN REC.SEMESTER IS NULL THEN T10.SEMESTER ELSE REC.SEMESTER END AS SEMESTER ");
            stb.append("   , REC.SCHREGNO ");
            stb.append("   , REC.STATUS ");
            stb.append("   , NM_D029.NAMESPARE1 AS STATUS_NAME1 ");
            stb.append("   , T10.SCORE ");
            stb.append(" FROM ");
            stb.append("     JVIEWNAME_GRADE_MST T1 ");
            stb.append("     INNER JOIN JVIEWNAME_GRADE_YDAT T2 ON T2.YEAR = '" + param._year + "' ");
            stb.append("         AND T2.GRADE = T1.GRADE ");
            stb.append("         AND T2.CLASSCD = T1.CLASSCD  ");
            stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND  ");
            stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD  ");
            stb.append("         AND T2.VIEWCD = T1.VIEWCD ");
            stb.append("     INNER JOIN CLASS_MST CLM ON CLM.CLASSCD = T1.CLASSCD ");
            stb.append("         AND CLM.SCHOOL_KIND = T1.SCHOOL_KIND  ");
            stb.append("     LEFT JOIN SUBCLASS_MST SCLM ON SCLM.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND SCLM.CLASSCD = T1.CLASSCD  ");
            stb.append("         AND SCLM.SCHOOL_KIND = T1.SCHOOL_KIND  ");
            stb.append("         AND SCLM.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            stb.append("     LEFT JOIN JVIEWSTAT_RECORD_DAT REC ON REC.SUBCLASSCD = T2.SUBCLASSCD ");
            stb.append("         AND REC.CLASSCD = T2.CLASSCD  ");
            stb.append("         AND REC.SCHOOL_KIND = T2.SCHOOL_KIND  ");
            stb.append("         AND REC.CURRICULUM_CD = T2.CURRICULUM_CD  ");
            stb.append("         AND REC.VIEWCD = T2.VIEWCD ");
            stb.append("         AND REC.YEAR = T2.YEAR ");
            stb.append("         AND REC.SCHREGNO = ? ");
            stb.append("     LEFT JOIN NAME_MST NM_D029 ON NM_D029.NAMECD1 = 'D029' ");
            stb.append("         AND NM_D029.ABBV1 = REC.STATUS ");
            stb.append("     LEFT JOIN RECORD_SCORE_DAT T10 ON T10.YEAR = T2.YEAR ");
            stb.append("         AND T10.SEMESTER = REC.SEMESTER ");
            stb.append("         AND T10.TESTKINDCD = '99' ");
            stb.append("         AND T10.TESTITEMCD = '00' ");
            stb.append("         AND T10.SCORE_DIV = '08' ");
            stb.append("         AND T10.CLASSCD = T1.CLASSCD ");
            stb.append("         AND T10.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("         AND T10.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("         AND T10.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND T10.SCHREGNO = ? ");
            stb.append(" WHERE ");
            stb.append("     T1.GRADE = '" + param._grade + "' ");
            stb.append("     AND T1.SCHOOL_KIND = '" + param._schoolkind + "' ");
            stb.append(" ORDER BY ");
            stb.append("     VALUE(SCLM.ELECTDIV, '0'), ");
            stb.append("     VALUE(CLM.SHOWORDER3, -1), ");
            stb.append("     CLM.CLASSCD, ");
            stb.append("     VALUE(SCLM.SHOWORDER3, -1), ");
            stb.append("     SCLM.CLASSCD, ");
            stb.append("     SCLM.SCHOOL_KIND, ");
            stb.append("     SCLM.CURRICULUM_CD, ");
            stb.append("     SCLM.SUBCLASSCD, ");
            stb.append("     VALUE(T1.SHOWORDER, -1), ");
            stb.append("     T1.VIEWCD ");
            return stb.toString();
        }
    }

    /**
     * 出欠の記録
     */
    private static class AttendSemesDat {

        final String _semester;
        int _lesson;
        int _suspend;
        int _mourning;
        int _mlesson;
        int _sick;
        int _absent;
        int _present;
        int _late;
        int _early;
        int _transferDate;
        int _offdays;
        int _kekkaJisu;
        int _virus;
        int _koudome;

        public AttendSemesDat(
                final String semester
        ) {
            _semester = semester;
        }

        public void add(
                final AttendSemesDat o
        ) {
            _lesson += o._lesson;
            _suspend += o._suspend;
            _mourning += o._mourning;
            _mlesson += o._mlesson;
            _sick += o._sick;
            _absent += o._absent;
            _present += o._present;
            _late += o._late;
            _early += o._early;
            _transferDate += o._transferDate;
            _offdays += o._offdays;
            _kekkaJisu += o._kekkaJisu;
            _virus += o._virus;
            _koudome += o._koudome;
        }

        private static void setAttendSemesDatList(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                param._attendParamMap.put("schregno", "?");
                final String sql = AttendAccumulate.getAttendSemesSql(
                        param._year,
                        param._semester,
                        null,
                        param._date,
                        param._attendParamMap
                );
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    student._attendSemesDatMap = new HashMap();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {

                        final String semester = rs.getString("SEMESTER");
                        final int lesson = rs.getInt("LESSON");
                        final int suspend = rs.getInt("SUSPEND");
                        final int mourning = rs.getInt("MOURNING");
                        final int mlesson = rs.getInt("MLESSON");
                        final int sick = rs.getInt("SICK");
                        final int absent = rs.getInt("ABSENT");
                        final int present = rs.getInt("PRESENT");
                        final int late = rs.getInt("LATE");
                        final int early = rs.getInt("EARLY");
                        final int transferDate = rs.getInt("TRANSFER_DATE");
                        final int offdays = rs.getInt("OFFDAYS");
                        final int kekkaJisu = rs.getInt("KEKKA_JISU");
                        final int virus = rs.getInt("VIRUS");
                        final int koudome = rs.getInt("KOUDOME");

                        final AttendSemesDat attendSemesDat = new AttendSemesDat(semester);
                        attendSemesDat._lesson = lesson;
                        attendSemesDat._suspend = suspend;
                        attendSemesDat._mourning = mourning;
                        attendSemesDat._mlesson = mlesson;
                        attendSemesDat._sick = sick;
                        attendSemesDat._absent = absent;
                        attendSemesDat._present = present;
                        attendSemesDat._late = late;
                        attendSemesDat._early = early;
                        attendSemesDat._transferDate = transferDate;
                        attendSemesDat._offdays = offdays;
                        attendSemesDat._kekkaJisu = kekkaJisu;
                        attendSemesDat._virus = virus;
                        attendSemesDat._koudome = koudome;

                        student._attendSemesDatMap.put(semester, attendSemesDat);
                    }
                    DbUtils.closeQuietly(rs);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

    }

    /**
     * 出欠の備考
     */
    private static class AttendReasonCollection {

        final String _semester;
        final String _attendRemark;

        public AttendReasonCollection(
                final String semester,
                final String attendRemark
        ) {
            _semester = semester;
            _attendRemark = attendRemark;
        }

        private static void setAttendReasonCollectionMap(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getAttendReasonCollectionSql(param);
                log.debug("attendReason sql = "+sql);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    student._attendReasonCollectionMap = new HashMap();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {

                        final String semester = rs.getString("SEMESTER");
                        final String attendRemark = StringUtils.defaultString(rs.getString("ATTEND_REMARK"));

                        final AttendReasonCollection attendReasonCollection = new AttendReasonCollection(semester, attendRemark);

                        student._attendReasonCollectionMap.put(semester, attendReasonCollection);
                    }
                    DbUtils.closeQuietly(rs);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private static String getAttendReasonCollectionSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH MIN_COL AS ( ");
            stb.append("     SELECT ");
            stb.append("         YEAR, ");
            stb.append("         SCHOOL_KIND, ");
            stb.append("         SEMESTER, ");
            stb.append("         MIN(COLLECTION_CD) AS COLLECTION_CD ");
            stb.append("     FROM ");
            stb.append("         ATTEND_REASON_COLLECTION_MST ");
            stb.append("     WHERE ");
            stb.append("         YEAR = '" + param._year + "' ");
            stb.append("         AND SCHOOL_KIND = '" + param._schoolkind + "' ");
            stb.append("     GROUP BY ");
            stb.append("         YEAR, ");
            stb.append("         SCHOOL_KIND, ");
            stb.append("         SEMESTER ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     ATCOL_D.ATTEND_REMARK, ");
            stb.append("     MIN_COL.SEMESTER ");
            stb.append(" FROM ");
            stb.append("     ATTEND_REASON_COLLECTION_DAT ATCOL_D ");
            stb.append("     INNER JOIN MIN_COL ON ATCOL_D.YEAR = MIN_COL.YEAR ");
            stb.append("           AND ATCOL_D.SCHOOL_KIND = MIN_COL.SCHOOL_KIND ");
            stb.append("           AND ATCOL_D.COLLECTION_CD = MIN_COL.COLLECTION_CD ");
            stb.append(" WHERE ");
            stb.append("     ATCOL_D.YEAR = '" + param._year + "' ");
            stb.append("     AND ATCOL_D.SCHREGNO = ? ");

            return stb.toString();
        }

    }

    /**
     * 通知表所見
     */
    private static class RecordTotalStudy {
        final String _semester;
        final String _ta1_totalstudytime;
        final String _ta1_totalstudyact;
        final String _ta1_remark1;
        final String _ta2_totalstudytime;
        final String _ta2_totalstudyact;

        public RecordTotalStudy(
                final String semester,
                final String ta1_totalstudytime,
                final String ta1_totalstudyact,
                final String ta1_remark1,
                final String ta2_totalstudytime,
                final String ta2_totalstudyact) {
            _semester = semester;
            _ta1_totalstudytime = ta1_totalstudytime;
            _ta1_totalstudyact = ta1_totalstudyact;
            _ta1_remark1 = ta1_remark1;
            _ta2_totalstudytime = ta2_totalstudytime;
            _ta2_totalstudyact = ta2_totalstudyact;
        }

        public static void setRecordTotalStudy(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getRecordTotalStudySql(param);
                log.debug("totalstudy sql = "+sql);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    student._recordTotalStudyTimeMap = new HashMap();

                    ps.setString(1, student._schregno);

                    rs = ps.executeQuery();
                    String semester = "";
                    String ta1_totalstudytime = "";
                    String ta1_remark1 = "";
                    String ta1_totalstudyact = "";
                    String ta2_totalstudytime = "";
                    String ta2_totalstudyact = "";
                    String sep = "\n";
                    while (rs.next()) {
                        if ("".equals(semester) || !semester.equals(rs.getString("SEMESTER"))) {
                            semester = rs.getString("SEMESTER");
                            ta1_totalstudytime = rs.getString("TA1_TOTALSTUDYTIME");
                            ta1_totalstudyact = rs.getString("TA1_TOTALSTUDYACT");
                            ta1_remark1 = rs.getString("TA1_REMARK1");
                            ta2_totalstudytime = rs.getString("TA2_TOTALSTUDYTIME");
                            ta2_totalstudyact = rs.getString("TA2_TOTALSTUDYACT");
                            if (!"".equals(semester)) {
                                final RecordTotalStudy recordTotalStudy = new RecordTotalStudy(semester, ta1_totalstudytime, ta1_totalstudyact, ta1_remark1, ta2_totalstudytime, ta2_totalstudyact);
                                student._recordTotalStudyTimeMap.put(semester, recordTotalStudy);
                            }
                        } else {
                            semester = semester + sep + rs.getString("SEMESTER");
                            ta1_totalstudytime = ta1_totalstudytime + sep + rs.getString("TA1_TOTALSTUDYTIME");
                            ta1_totalstudyact = ta1_totalstudyact + sep + rs.getString("TA1_TOTALSTUDYACT");
                            ta1_remark1 = ta1_remark1 + sep + rs.getString("TA1_REMARK1");
                            ta2_totalstudytime = ta2_totalstudytime + sep + rs.getString("TA2_TOTALSTUDYTIME");
                            ta2_totalstudyact = ta2_totalstudyact + sep + rs.getString("TA2_TOTALSTUDYACT");
                        }

                    }
                    final RecordTotalStudy recordTotalStudy = new RecordTotalStudy(semester, ta1_totalstudytime, ta1_totalstudyact, ta1_remark1, ta2_totalstudytime, ta2_totalstudyact);
                    student._recordTotalStudyTimeMap.put(semester, recordTotalStudy);

                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private static String getRecordTotalStudySql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            //RECORD_TOTALSTUDYTIME_DATで、紐づけたRECORD_TOTALSTUDYTIME_ITEM_MSTのREMARK1が'1'のデータだけが対象。
            stb.append(" WITH GETDOUTOKU as ( ");
            stb.append(" SELECT DISTINCT ");
            stb.append("  T4.* ");
            stb.append(" FROM ");
            stb.append("  RECORD_TOTALSTUDYTIME_DAT T4 ");
            stb.append("  LEFT JOIN RECORD_TOTALSTUDYTIME_ITEM_MST T3 ");
            stb.append("     ON T3.CLASSCD = T4.CLASSCD ");
            stb.append("    AND T3.SCHOOL_KIND = T4.SCHOOL_KIND ");
            stb.append(" WHERE ");
            stb.append("    T3.SHOW_FLG = '1' ");
            stb.append("    AND T3.REMARK2 = '1' ");
            stb.append(" ), COMMON as ( ");
            stb.append(" SELECT ");
            stb.append("  T4.SCHREGNO, ");
            stb.append("  T4.YEAR, ");
            stb.append("  T4.SEMESTER ");
            stb.append(" FROM ");
            stb.append("  GETDOUTOKU T4 ");
            stb.append("  UNION ");
            stb.append(" SELECT ");
            stb.append("  T4.SCHREGNO, ");
            stb.append("  T4.YEAR, ");
            stb.append("  T4.SEMESTER ");
            stb.append(" FROM ");
            stb.append("  RECORD_TOTALSTUDYTIME_DAT T4 ");
            stb.append("  LEFT JOIN RECORD_TOTALSTUDYTIME_ITEM_MST T3 ");
            stb.append("     ON T3.CLASSCD = T4.CLASSCD ");
            stb.append("    AND T3.SCHOOL_KIND = T4.SCHOOL_KIND ");
            stb.append(" WHERE ");
            stb.append("    T3.SHOW_FLG = '1' ");
            stb.append("    AND T3.REMARK2 = '1' ");
            stb.append(" ) ");
            //上記データのCLASSCD='28'とRECORD_TOTALSTUDYTIME_DATのCLASSCD='90'を対象とする。
            stb.append(" SELECT ");
            stb.append("  T1.SEMESTER, ");
            stb.append("  T3.TOTALSTUDYACT as TA1_TOTALSTUDYACT, ");
            stb.append("  T3.REMARK1 as TA1_REMARK1, ");
            stb.append("  T3.TOTALSTUDYTIME as TA1_TOTALSTUDYTIME, ");
            stb.append("  T2.TOTALSTUDYACT as TA2_TOTALSTUDYACT, ");
            stb.append("  T2.TOTALSTUDYTIME as TA2_TOTALSTUDYTIME ");
            stb.append(" FROM ");
            stb.append("  COMMON T1 ");
            stb.append("  LEFT JOIN RECORD_TOTALSTUDYTIME_DAT T3 ");
            stb.append("     ON T3.YEAR = T1.YEAR ");
            stb.append("    AND T3.SEMESTER = T1.SEMESTER ");
            stb.append("    AND T3.CLASSCD = '" + CLASSCD_TOTALSTDY + "' ");       //固定
            stb.append("    AND T3.SCHOOL_KIND = '" + param._schoolkind + "' ");
            stb.append("    AND T3.SCHREGNO = T1.SCHREGNO ");
            stb.append("  LEFT JOIN GETDOUTOKU T2 ");
            stb.append("     ON T2.YEAR = T1.YEAR ");
            stb.append("    AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("    AND T2.CLASSCD = '" + CLASSCD_MORAL + "' ");
            stb.append("    AND T2.SCHOOL_KIND = '" + param._schoolkind + "' ");
            stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("  T1.YEAR = '" + param._year + "' ");
            stb.append("  and T1.SCHREGNO = ? ");
            if (!param._isLastSemester) {
                stb.append("     AND (T1.SEMESTER <= '" + param._semester + "') ");
            }
            stb.append(" ORDER BY ");
            stb.append("  T1.SEMESTER ");

            return stb.toString();
        }

    }

    /**
     * 通知表所見
     */
    private static class HReportRemarkDat {
        final String _communication;
        final String _detail01r1;
        final String _detail02r1;
        final String _detail03r1;
        final String _detail04r1;
        final String _detail05r1;

        public HReportRemarkDat(
                final String communication,
                final String detail01r1,
                final String detail02r1,
                final String detail03r1,
                final String detail04r1,
                final String detail05r1
        ) {
            _communication = communication;
            _detail01r1 = detail01r1;
            _detail02r1 = detail02r1;
            _detail03r1 = detail03r1;
            _detail04r1 = detail04r1;
            _detail05r1 = detail05r1;
        }

        public static void setHreportData(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getHReportRemarkSql(param);
                log.debug("hreport sql = "+sql);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    student._hReportRemarkDatMap = new HashMap();

                    ps.setString(1, student._schregno);

                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String semester = rs.getString("SEMESTER");
                        final String communication = StringUtils.defaultString(rs.getString("COMMUNICATION"));
                        final String detail01r1 = StringUtils.defaultString(rs.getString("DETAIL01R1"));
                        final String detail02r1 = StringUtils.defaultString(rs.getString("DETAIL02R1"));
                        final String detail03r1 = StringUtils.defaultString(rs.getString("DETAIL03R1"));
                        final String detail04r1 = StringUtils.defaultString(rs.getString("DETAIL04R1"));
                        final String detail05r1 = StringUtils.defaultString(rs.getString("DETAIL05R1"));
                        final HReportRemarkDat hReportRemarkDat = new HReportRemarkDat(communication, detail01r1, detail02r1, detail03r1, detail04r1, detail05r1);
                        student._hReportRemarkDatMap.put(semester, hReportRemarkDat);
                    }
                    DbUtils.closeQuietly(rs);

                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private static String getHReportRemarkSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     REMARKD.SEMESTER, ");
            stb.append("     REMARKD.COMMUNICATION, ");
            stb.append("     DETAIL01.REMARK1 AS DETAIL01R1, ");
            stb.append("     DETAIL02.REMARK1 AS DETAIL02R1, ");
            stb.append("     DETAIL03.REMARK1 AS DETAIL03R1, ");
            stb.append("     DETAIL04.REMARK1 AS DETAIL04R1, ");
            stb.append("     DETAIL05.REMARK1 AS DETAIL05R1 ");
            stb.append(" FROM ");
            stb.append("     HREPORTREMARK_DAT REMARKD ");
            stb.append("     LEFT JOIN HREPORTREMARK_DETAIL_DAT DETAIL01 ON REMARKD.YEAR = DETAIL01.YEAR ");
            stb.append("          AND REMARKD.SEMESTER = DETAIL01.SEMESTER ");
            stb.append("          AND REMARKD.SCHREGNO = DETAIL01.SCHREGNO ");
            stb.append("          AND DETAIL01.DIV = '01' ");
            stb.append("          AND DETAIL01.CODE = '01' ");
            stb.append("     LEFT JOIN HREPORTREMARK_DETAIL_DAT DETAIL02 ON REMARKD.YEAR = DETAIL02.YEAR ");
            stb.append("          AND REMARKD.SEMESTER = DETAIL02.SEMESTER ");
            stb.append("          AND REMARKD.SCHREGNO = DETAIL02.SCHREGNO ");
            stb.append("          AND DETAIL02.DIV = '01' ");
            stb.append("          AND DETAIL02.CODE = '02' ");
            stb.append("     LEFT JOIN HREPORTREMARK_DETAIL_DAT DETAIL03 ON REMARKD.YEAR = DETAIL03.YEAR ");
            stb.append("          AND REMARKD.SEMESTER = DETAIL03.SEMESTER ");
            stb.append("          AND REMARKD.SCHREGNO = DETAIL03.SCHREGNO ");
            stb.append("          AND DETAIL03.DIV = '01' ");
            stb.append("          AND DETAIL03.CODE = '03' ");
            stb.append("     LEFT JOIN HREPORTREMARK_DETAIL_DAT DETAIL04 ON REMARKD.YEAR = DETAIL04.YEAR ");
            stb.append("          AND REMARKD.SEMESTER = DETAIL04.SEMESTER ");
            stb.append("          AND REMARKD.SCHREGNO = DETAIL04.SCHREGNO ");
            stb.append("          AND DETAIL04.DIV = '01' ");
            stb.append("          AND DETAIL04.CODE = '04' ");
            stb.append("     LEFT JOIN HREPORTREMARK_DETAIL_DAT DETAIL05 ON REMARKD.YEAR = DETAIL05.YEAR ");
            stb.append("          AND REMARKD.SEMESTER = DETAIL05.SEMESTER ");
            stb.append("          AND REMARKD.SCHREGNO = DETAIL05.SCHREGNO ");
            stb.append("          AND DETAIL05.DIV = '01' ");
            stb.append("          AND DETAIL05.CODE = '05' ");
            stb.append(" WHERE ");
            stb.append("     REMARKD.YEAR = '" + param._year + "' ");
            stb.append("     AND REMARKD.SCHREGNO = ? ");

            return stb.toString();
        }

    }

    private class Semester {
        final String _semester;
        final String _semesterName;

        public Semester(final String semester, final String semesterName) {
            _semester = semester;
            _semesterName = semesterName;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 68870 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _semester;
        final String _date;
        final boolean _hyoushiPrint;
        final String _gradeHrclass;
        final String _grade;
        final String[] _categorySelected;

        final String _gradeCdStr;
        final String _certifSchoolSchoolName;
        final String _certifSchoolRemark3;
        final String _certifSchoolPrincipalName;
        final String _gradeName;
        final String _d016Namespare1;
        private boolean _isLastSemester;

        final Map _attendParamMap;
        final Map _semesterMap;

        private String _schoolkind;

        final Map _totalStudyTime_DataSizeMap;
        final boolean _isOutputDebug;

        final String _documentroot;
        final String _imagepath;
        final String _schoolLogoImagePath;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _date = request.getParameter("DATE").replace('/', '-');
            _hyoushiPrint = "1".equals(request.getParameter("PRINT_HYOUSHI"));
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _grade = null != _gradeHrclass && _gradeHrclass.length() > 2 ? _gradeHrclass.substring(0, 2) : null;
            _categorySelected = request.getParameterValues("category_selected");

            _gradeCdStr = getGradeCdIntStr(db2, _grade);
            _certifSchoolSchoolName = getCertifSchoolDat(db2, "SCHOOL_NAME");
            _certifSchoolRemark3 = getCertifSchoolDat(db2, "REMARK3");
            _certifSchoolPrincipalName = getCertifSchoolDat(db2, "PRINCIPAL_NAME");
            _gradeName = "第" + StringUtils.defaultString(_gradeCdStr) + "学年";
            _d016Namespare1 = getNameMst(db2, _year, "D016", "01", "NAMESPARE1");

            _semesterMap = getSemester(db2);

            _schoolkind = getSchoolKind(db2);
            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);

            _totalStudyTime_DataSizeMap = getTotalStudyTime_DataSizeMap(db2);
            _isOutputDebug = "1".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD185M' AND NAME = 'outputDebug' ")));

            _documentroot = request.getParameter("DOCUMENTROOT");
            final String sqlControlMst = " SELECT IMAGEPATH FROM CONTROL_MST WHERE CTRL_NO = '01' ";
            _imagepath = KnjDbUtils.getOne(KnjDbUtils.query(db2, sqlControlMst));
            _schoolLogoImagePath = getImageFilePath("SCHOOLLOGO.jpg");
        }

        private Map getTotalStudyTime_DataSizeMap(final DB2UDB db2) {
        	StringBuffer stb = new StringBuffer();
        	stb.append(" SELECT CLASSCD || COLUMNNAME AS CODE, DATA_SIZE FROM RECORD_TOTALSTUDYTIME_ITEM_MST ");
        	stb.append(" WHERE ");
        	stb.append("   CLASSCD = '" + CLASSCD_TOTALSTDY + "' AND SCHOOL_KIND = '" + _schoolkind + "' ");
        	stb.append(" UNION ");
        	stb.append(" SELECT CLASSCD || COLUMNNAME AS CODE, DATA_SIZE FROM RECORD_TOTALSTUDYTIME_ITEM_MST ");
        	stb.append(" WHERE ");
        	stb.append("   CLASSCD = '" + CLASSCD_MORAL + "' AND SCHOOL_KIND = '" + _schoolkind + "' ");
        	return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, stb.toString()), "CODE", "DATA_SIZE");
        }

        private String getSchoolKind(final DB2UDB db2) {
        	return KnjDbUtils.getOne(KnjDbUtils.query(db2," SELECT GDAT.SCHOOL_KIND FROM SCHREG_REGD_GDAT GDAT WHERE GDAT.YEAR = '" + _year + "' AND GDAT.GRADE = '" + _grade + "' "));
        }

        private String getHrClassName1(final DB2UDB db2, final String year, final String semester, final String gradeHrclass) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT HR_CLASS_NAME1 FROM SCHREG_REGD_HDAT WHERE YEAR = '" + year + "' AND SEMESTER = '" + semester + "' AND GRADE || HR_CLASS = '" + gradeHrclass + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (null != rs.getString("HR_CLASS_NAME1")) {
                        rtn = rs.getString("HR_CLASS_NAME1");
                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            if (null == rtn) {
                try {
                    final String sql = " SELECT HR_CLASS FROM SCHREG_REGD_DAT WHERE YEAR = '" + year + "' AND SEMESTER = '" + semester + "' AND GRADE || HR_CLASS = '" + gradeHrclass + "' ";
                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();
                    if (rs.next()) {
                        if (null == rtn && null != rs.getString("HR_CLASS")) {
                            rtn = NumberUtils.isDigits(rs.getString("HR_CLASS")) ? String.valueOf(Integer.parseInt(rs.getString("HR_CLASS"))) : rs.getString("HR_CLASS");
                        }
                    }
                } catch (SQLException e) {
                    log.error("exception!", e);
                } finally {
                    DbUtils.closeQuietly(null, ps, rs);
                    db2.commit();
                }
            }
            if (null == rtn) {
                rtn = "";
            }
            return rtn;
        }

        private String getNameMst(final DB2UDB db2, final String year, final String namecd1, final String namecd2, final String field) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT ");
                sql.append("    " + field + " ");
                sql.append(" FROM NAME_MST T1 ");
                sql.append("    INNER JOIN NAME_YDAT T2 ON T2.YEAR = '" + year + "' AND T2.NAMECD1 = T1.NAMECD1 AND T2.NAMECD2 = T1.NAMECD2 ");
                sql.append(" WHERE ");
                sql.append("    T1.NAMECD1 = '" + namecd1 + "' ");
                sql.append("    AND T1.NAMECD2 = '" + namecd2 + "' ");
                sql.append("   ");
                ps = db2.prepareStatement(sql.toString());
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

        private String getCertifSchoolDat(final DB2UDB db2, final String field) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT " + field + " FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '103' ";
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

        private String getGradeCdIntStr(final DB2UDB db2, final String grade) {
            String gradeCd = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     * ");
                stb.append(" FROM ");
                stb.append("     SCHREG_REGD_GDAT T1 ");
                stb.append(" WHERE ");
                stb.append("     T1.SCHOOL_KIND = 'J' ");
                stb.append("     AND T1.YEAR = '" + _year + "' ");
                stb.append("     AND T1.GRADE = '" + grade + "' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    final String tmp = rs.getString("GRADE_CD");
                    if (NumberUtils.isDigits(tmp)) {
                        gradeCd = String.valueOf(Integer.parseInt(tmp));
                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return gradeCd;
        }

        private Map getSemester(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            String lastSemester = null;
            final Map retMap = new TreeMap();
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     * ");
                stb.append(" FROM ");
                stb.append("     SEMESTER_MST T1 ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + _year + "' ");
                stb.append(" ORDER BY ");
                stb.append("     T1.SEMESTER ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String semesterName = rs.getString("SEMESTERNAME");
                    final Semester semesterObj = new Semester(semester, semesterName);
                    retMap.put(semester, semesterObj);
                    if (!SEMEALL.equals(semester)) {
                        lastSemester = semester;
                    }
                }
                _isLastSemester = null != lastSemester && lastSemester.equals(_semester);
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        public String getImageFilePath(final String name) {
            final String path = _documentroot + "/" + (null == _imagepath || "".equals(_imagepath) ? "" : _imagepath + "/") + name;
            final boolean exists = new java.io.File(path).exists();
            log.warn(" path " + path + " exists: " + exists);
            if (exists) {
                return path;
            }
            return null;
        }
    }
}

// eof

