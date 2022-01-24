/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: 54da28047e02bef24e9599a3f7f731cdab3652df $
 *
 * 作成日: 2019/06/20
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

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

public class KNJD185O {

    private static final Log log = LogFactory.getLog(KNJD185O.class);

    private static final String SEMEALL = "9";
    private static final String SELECT_CLASSCD_UNDER = "90";
    private static final String SUBCLASSCD999999 = "999999";

    private static final String SCORE010101 = "010101";
    private static final String SCORE990008 = "990008";
    private static final String SCORE990009 = "990009";

    private static final String ALL3 = "333333";
    private static final String ALL5 = "555555";
    private static final String ALL9 = "999999";

    private boolean _hasData;

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
        final List studentList = Student.getList(db2, _param);
    	
        final Semester semester = (Semester) _param._semesterMap.get(_param._semester);
        for (Iterator iterator = studentList.iterator(); iterator.hasNext();) {
            final Student student = (Student) iterator.next();

            printHyoushi(db2, svf, student);

            svf.VrSetForm("KNJD185O_2.frm", 4);
            if (!_param._isLastSemester) {
                svf.VrsOut("TITLE", "成績通知表　(" + _param._year + "年度　" + semester._semestername + ")");
            }

            svf.VrsOut("SCHREGNO", student._schregno);
            svf.VrsOut("HR_NAME", student.getHrname());
            final String nameField = KNJ_EditEdit.getMS932ByteLength(student._name) > 44 ? "3" : KNJ_EditEdit.getMS932ByteLength(student._name) > 30 ? "2" : "1";
            svf.VrsOut("NAME" + nameField, student._name);

            //出欠記録
            printAttend(svf, student);

            //通知表所見
            printHreport(svf, student);

            //評定平均
            printHyouteiAvg(svf, student);

            final List subclassList = subclassListRemoveD026();
            Collections.sort(subclassList);

            //クラス順位
            String hyouteiAvg = "";
            String classRank = "";
            for (Iterator itSubclass = student._printSubclassMap.keySet().iterator(); itSubclass.hasNext();) {
                final String subclassCd = (String) itSubclass.next();
                final PrintSubclass printSubclass =  (PrintSubclass) student._printSubclassMap.get(subclassCd);

                final String[] subclassArray = StringUtils.split(subclassCd, "-");
                if (ALL9.equals(subclassArray[3])) {
                	final String semeKey = _param._isLastSemester ? SEMEALL : _param._semester;
                    if (printSubclass._semesScoreMap.containsKey(semeKey)) {
                        final Map scoreMap = (Map) printSubclass._semesScoreMap.get(semeKey);
                        if (null != scoreMap) {
                        	final ScoreData scoreData = (ScoreData) scoreMap.get(SCORE990008);
                        	if (null != scoreData) {
                        		if (!StringUtils.isEmpty(scoreData._classRank)) {
                        			classRank = scoreData._classRank;
                        		}
                        		if (!StringUtils.isEmpty(scoreData._avg)) {
                        			final BigDecimal bd = new BigDecimal(scoreData._avg).setScale(2, BigDecimal.ROUND_HALF_UP);
                        			hyouteiAvg = bd.toString();
                        		}
                        	}
                        }
                    }
                }
            }
            svf.VrsOutn("COMM1", 1, semester._semestername + "　科目評定平均　" + hyouteiAvg + " クラス内順位　" + classRank);

            final int maxCnt = 17;
            int subclassCnt = 0;
            String wkClassName = "";
            int wkCurriculumCd = 0;
            String wkSubClassCd = "";
            String wkSubClassName = "";
            String wkCredit = "";
            for (Iterator itSubclass = subclassList.iterator(); itSubclass.hasNext();) {
                final SubclassMst subclassMst = (SubclassMst) itSubclass.next();
                final String subclassCd = subclassMst._subclasscd;
                if (!student._printSubclassMap.containsKey(subclassCd)) {
                    continue;
                }
                final PrintSubclass printSubclass =  (PrintSubclass) student._printSubclassMap.get(subclassCd);

                final String[] subclass = StringUtils.split(printSubclass._subclassCd, "-");
                if("900100".equals(subclass[3])) {
                    if(wkCurriculumCd < Integer.parseInt(subclass[2])) {
                        //科目「900100」の情報を保持
                        // *複数レコード存在する場合、CURRICULUM_CDが最も大きいレコードを保持
                        wkClassName = subclassMst._classname;
                        wkCurriculumCd = Integer.parseInt(subclass[2]);
                        wkSubClassCd = subclass[3];
                        wkSubClassName = subclassMst._subclassname;
                        wkCredit = printSubclass. _credit;
                    }
                    continue;
                }

                final String classField = KNJ_EditEdit.getMS932ByteLength(subclassMst._classname) > 6 ? "2" : "1";
                svf.VrsOut("CLASS_NAME" + classField, subclassMst._classname);
                final String subNameField = KNJ_EditEdit.getMS932ByteLength(subclassMst._subclassname) > 24 ? "2" : "1";
                svf.VrsOut("SUBCLASS_NAME" + subNameField, subclassMst._subclassname);
                svf.VrsOut("CREDIT", printSubclass._credit);

                if (student._attendSubClassMap.containsKey(subclassCd)) {
                    final Map atSubSemeMap = (Map) student._attendSubClassMap.get(subclassCd);
                    if (atSubSemeMap.containsKey(SEMEALL)) {
                        final SubclassAttendance attendance = (SubclassAttendance) atSubSemeMap.get(SEMEALL);
                        svf.VrsOut("LESSON_TIME", attendance._lesson.toString());
                        svf.VrsOut("ABSENT_TIME", attendance._sick.toString());
                    }
                }

                for (int semei = 1; semei <= 9; semei++) {
                    if (!_param._isLastSemester && semei > Integer.parseInt(_param._semester)) {
                        continue;
                    }
                    final String scoreSemester = String.valueOf(semei);

                    if (printSubclass._semesScoreMap.containsKey(scoreSemester)) {
                        final Map scoreMap = (Map) printSubclass._semesScoreMap.get(scoreSemester);
                        if (scoreMap.containsKey(SCORE990008)) {
                            final ScoreData scoreData = (ScoreData) scoreMap.get(SCORE990008);

                            String score = "";
                            score = scoreData._score;
//                            if (student._attendSubClassMap.containsKey(subclassCd)) {
//                                final Map atSubSemeMap = (Map) student._attendSubClassMap.get(subclassCd);
//                                if (atSubSemeMap.containsKey(scoreSemester)) {
//                                    final SubclassAttendance attendance = (SubclassAttendance) atSubSemeMap.get(scoreSemester);
//                                    if(attendance._isOver) {
//                                        score = "(" + score + ")";
//                                    }
//                                }
//                            }
                            svf.VrsOut("EVA" + scoreSemester, score);
                        }
                    }
                }
                if(maxCnt < subclassCnt) subclassCnt = 0;
                subclassCnt++;
                svf.VrEndRecord();
            }

            if(!"".equals(wkSubClassCd) && subclassCnt != 0) {
                for (int idx = subclassCnt; idx <= maxCnt; idx++) {
                    if(idx == maxCnt) {
                        //最終行に科目「900100」を印字
                        final String classField = KNJ_EditEdit.getMS932ByteLength(wkClassName) > 10 ? "3_1" : KNJ_EditEdit.getMS932ByteLength(wkClassName) > 6 ? "2" : "1";
                        svf.VrsOut("CLASS_NAME" + classField, wkClassName);
                        final String subNameField = KNJ_EditEdit.getMS932ByteLength(wkSubClassName) > 24 ? "2" : "1";
                        svf.VrsOut("SUBCLASS_NAME" + subNameField, wkSubClassName);
                        svf.VrsOut("CREDIT", wkCredit);
                    }
                    svf.VrEndRecord();
                }
            }

            _hasData = true;
            if (subclassCnt == 0) {
                svf.VrEndRecord();
            }
        }
    }

    private void printHyoushi(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        final String form = "KNJD185O_1.frm";
        svf.VrSetForm(form, 1);

        svf.VrsOut("NENDO", _param._year + "年度");

        if (!_param._isLastSemester) {
            final Semester semester = (Semester) _param._semesterMap.get(_param._semester);
            svf.VrsOut("SEMESTER", semester._semestername);
        }

        final String putHrName = student.getHrname();
        svf.VrsOut("HR_NAME", putHrName);
        final String nameField = KNJ_EditEdit.getMS932ByteLength(student._name) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(student._name) > 20 ? "2" : "1";
        svf.VrsOut("NAME1_" + nameField, student._name);
        final String staffNameField = KNJ_EditEdit.getMS932ByteLength(student._staffname) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(student._staffname) > 20 ? "2" : "1";
        svf.VrsOut("STAFF_NAME" + staffNameField, student._staffname);

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
        printUraHyoushi(db2, svf, student);
        svf.VrEndPage();
        _hasData = true;
    }

    private void printUraHyoushi(final DB2UDB db2, final Vrw32alp svf, final Student student) {
    	
        if (_param._isLastSemester) {
            svf.VrPage("10");
        	
            svf.VrsOut("NENDO", _param._year + "年度");

            final String putHrName = student.getHrname();
            svf.VrsOut("HR_NAME", putHrName);
            final String nameField = KNJ_EditEdit.getMS932ByteLength(student._name) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(student._name) > 20 ? "2" : "1";
            svf.VrsOut("NAME2_" + nameField, student._name);
            svf.VrsOut("BIRTHDAY", KNJ_EditDate.h_format_SeirekiJP(student._birthday));

            svf.VrsOut("NENDO2", _param._year + "年度");
            svf.VrsOut("DATE", KNJ_EditDate.h_format_SeirekiJP(_param._ninteiDate));

            svf.VrsOut("SCHOOL_NAME2", _param._certifSchoolSchoolName);

            svf.VrsOut("PRINCIPAL_TITLE", "校長");
            svf.VrsOut("PRINCIPAL_NAME", _param._certifSchoolPrincipalName);

            for (int i = 1; i <= 28; i++) {
                svf.VrsOutn("CREDIT_LINE_V1", i, _param.getImageFilePath("SEISEN_LINE_LV.jpg"));
                svf.VrsOutn("CREDIT_LINE_V2", i, _param.getImageFilePath("SEISEN_LINE_LV.jpg"));
            }
            for (int i = 1; i <= 19; i++) {
                svf.VrsOutn("CREDIT_LINE_S1", i, _param.getImageFilePath("SEISEN_LINE_LS.jpg"));
                svf.VrsOutn("CREDIT_LINE_S2", i, _param.getImageFilePath("SEISEN_LINE_LS.jpg"));
            }
            _hasData = true;

        } else {
            svf.VrPage("01");
        }
    }

    private List subclassListRemoveD026() {
        final List retList = new ArrayList(_param._subclassMstMap.values());
        for (final Iterator it = retList.iterator(); it.hasNext();) {
            final SubclassMst subclassMst = (SubclassMst) it.next();
            if (_param._d026List.contains(subclassMst._subclasscd)) {
                it.remove();
            }
            if (_param._isNoPrintMoto &&  subclassMst._isMoto || !_param._isPrintSakiKamoku &&  subclassMst._isSaki) {
                it.remove();
            }
        }
        return retList;
    }

    // 出欠記録
    private void printAttend(final Vrw32alp svf, final Student student) {
        for (final Iterator it = _param._semesterMap.keySet().iterator(); it.hasNext();) {
            final String semester = (String) it.next();
            final int line = getSemeLine(semester);
            final Attendance att = (Attendance) student._attendMap.get(semester);
            if (null != att) {
                svf.VrsOutn("LESSON", line, String.valueOf(att._lesson));   // 授業日数
                svf.VrsOutn("SUSPEND", line, String.valueOf(att._suspend + att._mourning)); // 忌引出停日数
                svf.VrsOutn("ABROAD", line, String.valueOf(att._abroad));   // 留学日数
                svf.VrsOutn("MUST", line, String.valueOf(att._mLesson));    // 出席しなければならない日数
                svf.VrsOutn("ABSENT", line, String.valueOf(att._sick ));    // 欠席日数
                svf.VrsOutn("LATE", line, String.valueOf(att._late));       // 遅刻
                svf.VrsOutn("EARLY", line, String.valueOf(att._early));     // 早退
            }
        }
    }

    // 通知表所見
    private void printHreport(final Vrw32alp svf, final Student student) {

        for (final Iterator it = _param._semesterMap.keySet().iterator(); it.hasNext();) {
            final String semester = (String) it.next();
            final HReportRemarkDat remarkDat = (HReportRemarkDat) student._hReportRemarkDatMap.get(semester);
            //出欠備考
            if (Integer.parseInt(semester) <= Integer.parseInt(_param._semester)) {
                if (null != remarkDat) {
                    final List atRemarkList = KNJ_EditKinsoku.getTokenList(remarkDat._attendrecRemark, 20);
                    int atRemarkField = 1;
                    for (Iterator itatRemark = atRemarkList.iterator(); itatRemark.hasNext();) {
                        final String atRemark = (String) itatRemark.next();
                        svf.VrsOutn("ATTEND_REMARK" + atRemarkField, Integer.parseInt(semester), atRemark);
                        atRemarkField++;
                    }
                }
            }
            //特別活動
            if (null != remarkDat && semester.equals(_param._semester)) {
                printHreportText(svf, remarkDat._detail01r1, "SP_ACT1", 20);
                printHreportText(svf, remarkDat._detail03r1, "SP_ACT2", 40);
                printHreportText(svf, remarkDat._detail05r1, "SP_ACT3", 20);
                printHreportText(svf, remarkDat._detail06r1, "SP_ACT4", 40);
            }
        }
        //所見
        final HReportRemarkDat remarkDat = (HReportRemarkDat) student._hReportRemarkDatMap.get(_param._semester);
        if (null != remarkDat) {
            final List commList = KNJ_EditKinsoku.getTokenList(remarkDat._communication, 80);
            int commLine = 1;
            for (Iterator itComm = commList.iterator(); itComm.hasNext();) {
                final String communication = (String) itComm.next();
                svf.VrsOutn("COMM2", commLine, communication);
                commLine++;
            }
        }
    }

    private void printHreportText(final Vrw32alp svf, String setText, String textField, final int keta) {
        final List printList = KNJ_EditKinsoku.getTokenList(setText, keta);
        int textCnt = 1;
        for (Iterator itText = printList.iterator(); itText.hasNext();) {
            final String printText = (String) itText.next();
            svf.VrsOutn(textField, textCnt, printText);
            textCnt++;
        }
    }

    private void printHyouteiAvg(final Vrw32alp svf, final Student student) {
        int colLine = 1;
        int rowLine = 1;
        for (Iterator itComm = student._printHyouteiMap.keySet().iterator(); itComm.hasNext();) {
            if (colLine > 7) {
                colLine = 1;
                rowLine++;
            }
            final String classCd = (String) itComm.next();
            final PrintHyoutei printHyoutei = (PrintHyoutei) student._printHyouteiMap.get(classCd);
            svf.VrsOutn("AVE_SUBCLASS_NAME" + colLine, rowLine, printHyoutei._className);
            svf.VrsOutn("AVE_VAL" + colLine, rowLine, printHyoutei._val);
            colLine++;
            svf.VrsOut("AVE_VAL_TOTAL", printHyoutei._allVal);
        }
    }

    private int getSemeLine(final String semester) {
        final int line;
        if (SEMEALL.equals(semester)) {
            line = 4;
        } else {
            line = Integer.parseInt(semester);
        }
        return line;
    }

    private static String sishaGonyu(final String val) {
        if (!NumberUtils.isNumber(val)) {
            return null;
        }
        return new BigDecimal(val).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private static class Student {
        String _schregno;
        String _name;
        String _birthday;
        String _hrname;
        String _staffname;
        String _staffname2;
        String _attendno;
        String _gradeCd;
        String _grade;
        String _coursecd;
        String _majorcd;
        String _course;
        String _majorname;
        String _hrClassName1;
        String _entyear;
        String _communication;
        String _totalstudytime;
        String _specialactremark;
        final Map _attendMap = new TreeMap();
        final Map _printSubclassMap = new TreeMap();
        final Map _printHyouteiMap = new TreeMap();
        final Map _attendSubClassMap = new TreeMap();
        final Map _jviewGradeMap = new TreeMap();
        Map _hReportRemarkDatMap = Collections.EMPTY_MAP; // 通知表所見

        private void setRankSdiv(final DB2UDB db2, final Param param) {
            final String scoreSql = getRankSdivSql(param);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
            	if (param._isOutputDebug) {
            		log.info(" score sql = " + scoreSql);
            	}
                ps = db2.prepareStatement(scoreSql);
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final String semester = rs.getString("SEMESTER");
                    final String testkindCd = rs.getString("TESTKINDCD");
                    final String testitemCd = rs.getString("TESTITEMCD");
                    final String scoreDiv = rs.getString("SCORE_DIV");
                    final String credit = rs.getString("CREDITS");
                    final String score = rs.getString("SCORE");
                    final String avg = rs.getString("AVG");
                    final String classRank = rs.getString("CLASS_AVG_RANK");
                    if (!_printSubclassMap.containsKey(subclassCd)) {
                        _printSubclassMap.put(subclassCd, new PrintSubclass(subclassCd, credit));
                    }
                    final PrintSubclass printSubclass = (PrintSubclass) _printSubclassMap.get(subclassCd);

                    if (null != semester) {
                    	if (!printSubclass._semesScoreMap.containsKey(semester)) {
                    		printSubclass._semesScoreMap.put(semester, new TreeMap());
                    	}
                    	final Map scoreMap = (Map) printSubclass._semesScoreMap.get(semester);
                    	final String testKey = testkindCd + testitemCd + scoreDiv;
                    	if (!scoreMap.containsKey(testKey)) {
                    		scoreMap.put(testKey, new ScoreData(score, avg, classRank));
                    	}
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }

        }

        private String getRankSdivSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH CHAIR AS( ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     S1.YEAR, ");
            stb.append("     S1.SCHREGNO, ");
            stb.append("     S2.CLASSCD, ");
            stb.append("     S2.SCHOOL_KIND, ");
            stb.append("     S2.CURRICULUM_CD, ");
            stb.append("     S2.SUBCLASSCD ");
            stb.append(" FROM ");
            stb.append("     CHAIR_STD_DAT S1, ");
            stb.append("     CHAIR_DAT S2 ");
            stb.append(" WHERE ");
            stb.append("     S1.YEAR         = '" + param._year + "' ");
            stb.append("     AND S2.YEAR     = S1.YEAR ");
            stb.append("     AND S2.SEMESTER = S1.SEMESTER ");
            stb.append("     AND S2.CHAIRCD  = S1.CHAIRCD ");
            stb.append("     AND S1.SCHREGNO = '" + _schregno + "' ");
            stb.append("     AND S2.CLASSCD <= '" + SELECT_CLASSCD_UNDER + "' ");
            stb.append("     AND S2.SUBCLASSCD NOT LIKE '50%' ");
            stb.append(" )  ");
            stb.append(" SELECT ");
            stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     RANK_SDIV.SEMESTER, ");
            stb.append("     RANK_SDIV.TESTKINDCD, ");
            stb.append("     RANK_SDIV.TESTITEMCD, ");
            stb.append("     RANK_SDIV.SCORE_DIV, ");
            stb.append("     CRE.CREDITS, ");
            stb.append("     RANK_SDIV.SCORE, ");
            stb.append("     RANK_SDIV.AVG, ");
            stb.append("     RANK_SDIV.CLASS_AVG_RANK ");
            stb.append(" FROM ");
            stb.append("     CHAIR T1 ");
            stb.append("     LEFT JOIN CREDIT_MST CRE ON T1.YEAR = CRE.YEAR ");
            stb.append("          AND CRE.COURSECD || CRE.MAJORCD || CRE.COURSECODE = '" + _course + "' ");
            stb.append("          AND CRE.GRADE = '" + _grade + "' ");
            stb.append("          AND T1.CLASSCD = CRE.CLASSCD AND T1.SCHOOL_KIND = CRE.SCHOOL_KIND AND T1.CURRICULUM_CD = CRE.CURRICULUM_CD AND T1.SUBCLASSCD = CRE.SUBCLASSCD ");
            stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT RANK_SDIV ");
            stb.append("            ON RANK_SDIV.YEAR          = T1.YEAR ");
            stb.append("           AND RANK_SDIV.SCHREGNO      = T1.SCHREGNO ");
            stb.append("           AND RANK_SDIV.CLASSCD       = T1.CLASSCD ");
            stb.append("           AND RANK_SDIV.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("           AND RANK_SDIV.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("           AND RANK_SDIV.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("           AND RANK_SDIV.TESTKINDCD || RANK_SDIV.TESTITEMCD || RANK_SDIV.SCORE_DIV = '" + SCORE990008 + "' ");
            stb.append("           AND RANK_SDIV.CLASSCD <= '" + SELECT_CLASSCD_UNDER + "' ");
            stb.append("           AND RANK_SDIV.SUBCLASSCD NOT IN ('" + ALL3 + "', '" + ALL5 + "') ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     RANK_SDIV.CLASSCD || '-' || RANK_SDIV.SCHOOL_KIND || '-' || RANK_SDIV.CURRICULUM_CD || '-' || RANK_SDIV.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     RANK_SDIV.SEMESTER, ");
            stb.append("     RANK_SDIV.TESTKINDCD, ");
            stb.append("     RANK_SDIV.TESTITEMCD, ");
            stb.append("     RANK_SDIV.SCORE_DIV, ");
            stb.append("     CAST(NULL AS SMALLINT) AS CREDITS, ");
            stb.append("     RANK_SDIV.SCORE, ");
            stb.append("     RANK_SDIV.AVG, ");
            stb.append("     RANK_SDIV.CLASS_AVG_RANK ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_SDIV_DAT RANK_SDIV ");
            stb.append(" WHERE ");
            stb.append("         RANK_SDIV.YEAR          = '" + param._year + "' ");
            stb.append("     AND RANK_SDIV.SCHREGNO      = '" + _schregno +"' ");
            stb.append("     AND RANK_SDIV.TESTKINDCD || RANK_SDIV.TESTITEMCD || RANK_SDIV.SCORE_DIV = '" + SCORE990008 + "' ");
            stb.append("     AND RANK_SDIV.SUBCLASSCD = '" + ALL9 + "' ");
            stb.append(" ORDER BY ");
            stb.append("     SUBCLASSCD, ");
            stb.append("     SEMESTER, ");
            stb.append("     TESTKINDCD, ");
            stb.append("     TESTITEMCD, ");
            stb.append("     SCORE_DIV ");

            return stb.toString();
        }

        private void setHyouteiAvg(final DB2UDB db2, final Param param) {
            final String hyouteiAvgSqlSql = getHyouteiAvgSql(param);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(hyouteiAvgSqlSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String classCd = rs.getString("CLASSCD");
                    final String className = rs.getString("CLASSNAME");
                    final String val = rs.getString("VAL");
                    final String allVal = rs.getString("ALL_VAL");
                    PrintHyoutei printHyoutei = new PrintHyoutei(classCd, className, val, allVal);
                    _printHyouteiMap.put(classCd, printHyoutei);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }

        }

        private String getHyouteiAvgSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCHNO AS ( ");
            stb.append(" SELECT ");
            stb.append("     SCHREGNO, ");
            stb.append("     GRADE, ");
            stb.append("     HR_CLASS, ");
            stb.append("     ATTENDNO, ");
            stb.append("     ANNUAL, ");
            stb.append("     COURSECD, ");
            stb.append("     MAJORCD, ");
            stb.append("     COURSECODE ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + param._year + "' ");
            stb.append("     AND SEMESTER = '" + param._semester + "' ");
            stb.append("     AND SCHREGNO = '" + _schregno + "' ");
            stb.append(" ), STUDYREC0 AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.YEAR,");
            stb.append("     T1.ANNUAL,");
            stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND AS CLASSCD, ");
            stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || VALUE(T2.SUBCLASSCD2, T1.SUBCLASSCD) AS SUBCLASSCD, ");
            stb.append("     T1.VALUATION as VALUATION, ");
            stb.append("     CASE WHEN ADD_CREDIT IS NOT NULL OR GET_CREDIT IS NOT NULL THEN VALUE(ADD_CREDIT, 0) + VALUE(GET_CREDIT, 0) END AS CREDIT ");
            stb.append(" FROM ");
            stb.append("     SCHREG_STUDYREC_DAT T1 ");
            stb.append("     LEFT JOIN SUBCLASS_MST T2 ON T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("          AND T2.CLASSCD = T1.CLASSCD ");
            stb.append("          AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("          AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append(" WHERE ");
            stb.append("     T1.CLASSCD < '90' ");
            stb.append("     AND 0 < VALUATION ");
            stb.append("     AND T1.YEAR <= '" + param._year + "' ");
            stb.append("     AND NOT EXISTS (SELECT 'X' FROM NAME_MST N1 WHERE N1.NAMECD1 = 'A023' AND N1.NAME1 = 'J' AND T1.ANNUAL BETWEEN N1.NAME2 AND N1.NAME3) ");
            if (param._isGakunensei) {
                stb.append("     AND (T1.SCHREGNO, T1.YEAR) IN ( ");
                stb.append("            SELECT T2.SCHREGNO, MAX(T2.YEAR) AS YEAR");
                stb.append("            FROM SCHREG_STUDYREC_DAT T2  ");
                stb.append("            WHERE T2.SCHREGNO IN (SELECT SCHREGNO FROM SCHNO) AND ANNUAL <> '00'");
                stb.append("            GROUP BY T2.SCHREGNO, ANNUAL ");
                stb.append("         UNION ");
                stb.append("            SELECT T2.SCHREGNO, T2.YEAR ");
                stb.append("            FROM SCHREG_STUDYREC_DAT T2  ");
                stb.append("            WHERE T2.SCHREGNO IN (SELECT SCHREGNO FROM SCHNO) AND ANNUAL = '00'");
                stb.append("        ) ");
            } else {
                stb.append("     AND T1.SCHREGNO IN (SELECT SCHREGNO FROM SCHNO) ");
            }
            stb.append("     AND T1.SCHOOL_KIND = '" + param._schoolkind + "' ");
            stb.append(" ), STUDYREC AS ( ");
            stb.append(" SELECT ");
            stb.append("     SCHREGNO, ");
            stb.append("     CASE WHEN ANNUAL = '00' THEN T1.YEAR ELSE ANNUAL END AS ANNUAL, ");
            stb.append("     CLASSCD, ");
            stb.append("     CASE WHEN COUNT(*) = 1 THEN MAX(VALUATION) ");
            stb.append("          WHEN GVAL_CALC = '0' THEN ROUND(AVG(FLOAT(CASE WHEN 0 < VALUATION THEN VALUATION END)),0) ");
            stb.append("          WHEN GVAL_CALC = '1' AND 0 < SUM(CASE WHEN 0 < VALUATION THEN CREDIT END) THEN ROUND(FLOAT(SUM((CASE WHEN 0 < VALUATION THEN VALUATION END)*CREDIT))/SUM(CASE WHEN 0 < VALUATION THEN CREDIT END),0) ");
            stb.append("          ELSE MAX(VALUATION) END AS VALUATION ");
            stb.append(" FROM ");
            stb.append("     STUDYREC0 T1 ");
            stb.append("     LEFT JOIN SCHOOL_MST T2 ON T2.YEAR = T1.YEAR ");
            stb.append("          AND T2.SCHOOL_KIND = '" + param._schoolkind + "' ");
            stb.append(" GROUP BY ");
            stb.append("     SCHREGNO, ");
            stb.append("     CASE WHEN ANNUAL = '00' THEN T1.YEAR ELSE ANNUAL END, ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     GVAL_CALC ");
            stb.append(" ), ASSESS_CLASS AS ( ");
            //教科毎評定平均値
            stb.append(" SELECT ");
            stb.append("     DECIMAL(ROUND(AVG(FLOAT(VALUATION))*10,0)/10,4,1) AS VAL, ");
            stb.append("     SCHREGNO, ");
            stb.append("     CLASSCD ");
            stb.append(" FROM ");
            stb.append("     STUDYREC ");
            stb.append(" GROUP BY ");
            stb.append("     SCHREGNO, ");
            stb.append("     CLASSCD ");
            stb.append(" ), ASSESS_ALL AS ( ");
            //全体の評定平均値
            stb.append(" SELECT ");
            stb.append("     DECIMAL(ROUND(AVG(FLOAT(VALUATION))*10,0)/10,4,1) AS VAL, ");
            stb.append("     SCHREGNO, ");
            stb.append("     MAX(ANNUAL) AS GRADE_MAX ");
            stb.append(" FROM ");
            stb.append("     STUDYREC ");
            stb.append(" GROUP BY ");
            stb.append("     SCHREGNO ");
            stb.append(" ), ASSESS_ALL2 AS ( ");
            //全体の評定平均値・段階
            stb.append("     SELECT ");
            stb.append("        W1.SCHREGNO, ");
            stb.append("        W1.VAL, ");
            stb.append("        W2.ASSESSMARK ");
            stb.append("     FROM ");
            stb.append("        ASSESS_ALL W1 ");
            stb.append("        LEFT JOIN SCHNO L1 ON L1.SCHREGNO = W1.SCHREGNO ");
            if ("1".equals(param._useAssessCourseMst)) {
                stb.append("            LEFT JOIN ASSESS_COURSE_MST W2 ON W2.ASSESSCD = '4' ");
                stb.append("                                       AND W2.COURSECD = L1.COURSECD ");
                stb.append("                                       AND W2.MAJORCD = L1.MAJORCD ");
                stb.append("                                       AND W2.COURSECODE = L1.COURSECODE ");
                stb.append("                                       AND W1.VAL BETWEEN ASSESSLOW AND ASSESSHIGH ");
            } else {
                stb.append("            LEFT JOIN ASSESS_MST W2 ON W2.ASSESSCD = '4' ");
                stb.append("                                       AND W1.VAL BETWEEN ASSESSLOW AND ASSESSHIGH ");
            }
            stb.append("    ) ");
            //メイン
            stb.append(" SELECT ");
            stb.append("     ASSESS_CLASS.CLASSCD,");
            stb.append("     CLASS_MST.CLASSNAME,");
            stb.append("     ASSESS_CLASS.VAL, ");
            stb.append("     ASSESS_ALL2.VAL AS ALL_VAL");
            stb.append(" FROM ");
            stb.append("     SCHNO ");
            stb.append("     LEFT JOIN ASSESS_CLASS ON ASSESS_CLASS.SCHREGNO = SCHNO.SCHREGNO ");
            stb.append("     LEFT JOIN ASSESS_ALL2 ASSESS_ALL2 ON ASSESS_ALL2.SCHREGNO = SCHNO.SCHREGNO ");
            stb.append("     LEFT JOIN CLASS_MST ON ASSESS_CLASS.CLASSCD = CLASS_MST.CLASSCD || '-' || CLASS_MST.SCHOOL_KIND ");
            stb.append(" ORDER BY ");
            stb.append("     ASSESS_CLASS.CLASSCD");

            return stb.toString();
        }

        public String getHrname() {
            final String gradeCd = null == _gradeCd ? "" : String.valueOf(Integer.parseInt(_gradeCd));
            final String attendno = null == _attendno ? "" : String.valueOf(Integer.parseInt(_attendno));
            final String retStr = gradeCd + "年 " + StringUtils.defaultString(_hrClassName1) + "組 " + attendno + "番";
            return retStr;
        }

        private static List getList(final DB2UDB db2, final Param param) {
            final List retList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getStudentSql(param);
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final Student student = new Student();
                    student._schregno = rs.getString("SCHREGNO");
                    student._name = rs.getString("NAME");
                    student._birthday = StringUtils.defaultString(rs.getString("BIRTHDAY"));
                    student._hrname = rs.getString("HR_NAME");
                    student._staffname = StringUtils.defaultString(rs.getString("STAFFNAME"));
                    student._staffname2 = StringUtils.defaultString(rs.getString("STAFFNAME2"));
                    student._attendno = rs.getString("ATTENDNO");
                    student._gradeCd = rs.getString("GRADE_CD");
                    student._grade = rs.getString("GRADE");
                    student._coursecd = rs.getString("COURSECD");
                    student._majorcd = rs.getString("MAJORCD");
                    student._course = rs.getString("COURSE");
                    student._majorname = rs.getString("MAJORNAME");
                    student._hrClassName1 = rs.getString("HR_CLASS_NAME1");
                    student._entyear = rs.getString("ENT_YEAR");
                    retList.add(student);
                }
            } catch (SQLException ex) {
                log.error("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            for (final Iterator it = retList.iterator(); it.hasNext();) {
            	final Student student = (Student) it.next();
                student.setRankSdiv(db2, param);
                student.setHyouteiAvg(db2, param);
            }
            
            HReportRemarkDat.setHreportData(db2, param, retList);

            for (final Iterator rit = param._attendRanges.values().iterator(); rit.hasNext();) {
                final DateRange range = (DateRange) rit.next();
                //下段の出欠
                Attendance.load(db2, param, retList, range);
                //欠課
                SubclassAttendance.load(db2, param, retList, range);
            }

            return retList;
        }

        private static String getStudentSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append("     SELECT  REGD.SCHREGNO");
            stb.append("            ,GDAT.GRADE_CD ");
            stb.append("            ,REGD.SEMESTER ");
            stb.append("            ,BASE.NAME ");
            stb.append("            ,BASE.BIRTHDAY ");
            stb.append("            ,REGDH.HR_NAME ");
            stb.append("            ,STF1.STAFFNAME ");
            stb.append("            ,STF2.STAFFNAME AS STAFFNAME2 ");
            stb.append("            ,REGD.ATTENDNO ");
            stb.append("            ,REGD.GRADE ");
            stb.append("            ,REGD.COURSECD ");
            stb.append("            ,REGD.MAJORCD ");
            stb.append("            ,REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE AS COURSE");
            stb.append("            ,MAJOR.MAJORNAME ");
            stb.append("            ,REGDH.HR_CLASS_NAME1 ");
            stb.append("            ,FISCALYEAR(BASE.ENT_DATE) AS ENT_YEAR ");
            stb.append("     FROM    SCHREG_REGD_DAT REGD ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR ");
            stb.append("                  AND REGDH.SEMESTER = REGD.SEMESTER ");
            stb.append("                  AND REGDH.GRADE = REGD.GRADE ");
            stb.append("                  AND REGDH.HR_CLASS = REGD.HR_CLASS ");
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON REGDH.YEAR = GDAT.YEAR ");
            stb.append("          AND REGDH.GRADE = GDAT.GRADE ");
            stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     LEFT JOIN STAFF_MST STF1 ON STF1.STAFFCD = REGDH.TR_CD1 ");
            stb.append("     LEFT JOIN MAJOR_MST MAJOR ON MAJOR.COURSECD = REGD.COURSECD ");
            stb.append("                  AND MAJOR.MAJORCD = REGD.MAJORCD ");
            stb.append("     LEFT JOIN STAFF_MST STF2 ON STF2.STAFFCD = REGDH.TR_CD2 ");
            stb.append("     WHERE   REGD.YEAR = '" + param._year + "' ");
            stb.append("     AND REGD.SEMESTER = '" + param._semester + "' ");
            stb.append("         AND REGD.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected));
            stb.append("     ORDER BY ");
            stb.append("         REGD.GRADE, ");
            stb.append("         REGD.HR_CLASS, ");
            stb.append("         REGD.ATTENDNO ");
            final String sql = stb.toString();
            log.debug(" student sql = " + sql);

            return stb.toString();
        }
    }

    private static class PrintSubclass {
        final String _subclassCd;
        final String _credit;
        final Map _semesHyoukaMap;
        /**
         * _semesScoreMap[学期]scoreMap[テストコード]ScoreData
         */
        final Map _semesScoreMap;
        private PrintSubclass(
                final String subclassCd,
                final String credit
        ) {
            _subclassCd = subclassCd;
            _credit = credit;
            _semesHyoukaMap = new TreeMap();
            _semesScoreMap = new TreeMap();
        }
    }

    private static class PrintHyoutei {
        final String _classCd;
        final String _className;
        final String _val;
        final String _allVal;

        private PrintHyoutei(
                final String classCd,
                final String className,
                final String val,
                final String allVal
        ) {
            _classCd = classCd;
            _className = className;
            _val = val;
            _allVal = allVal;
        }
    }

    /**
     * 通知表所見
     */
    private static class HReportRemarkDat {
        final String _communication;
        final String _attendrecRemark;
        final String _detail01r1;
        final String _detail03r1;
        final String _detail05r1;
        final String _detail06r1;

        public HReportRemarkDat(
                final String communication,
                final String attendrecRemark,
                final String detail01r1,
                final String detail03r1,
                final String detail05r1,
                final String detail06r1
        ) {
            _communication = communication;
            _attendrecRemark = attendrecRemark;
            _detail01r1 = detail01r1;
            _detail03r1 = detail03r1;
            _detail05r1 = detail05r1;
            _detail06r1 = detail06r1;
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
                        final String attendrecRemark = StringUtils.defaultString(rs.getString("ATTENDREC_REMARK"));
                        final String detail01r1 = StringUtils.defaultString(rs.getString("DETAIL01R1"));
                        final String detail03r1 = StringUtils.defaultString(rs.getString("DETAIL03R1"));
                        final String detail05r1 = StringUtils.defaultString(rs.getString("DETAIL05R1"));
                        final String detail06r1 = StringUtils.defaultString(rs.getString("DETAIL06R1"));
                        final HReportRemarkDat hReportRemarkDat = new HReportRemarkDat(communication, attendrecRemark, detail01r1, detail03r1, detail05r1, detail06r1);
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
            stb.append("     REMARKD.ATTENDREC_REMARK, ");
            stb.append("     DETAIL01.REMARK1 AS DETAIL01R1, ");
            stb.append("     DETAIL03.REMARK1 AS DETAIL03R1, ");
            stb.append("     DETAIL05.REMARK1 AS DETAIL05R1, ");
            stb.append("     DETAIL06.REMARK1 AS DETAIL06R1 ");
            stb.append(" FROM ");
            stb.append("     HREPORTREMARK_DAT REMARKD ");
            stb.append("     LEFT JOIN HREPORTREMARK_DETAIL_DAT DETAIL01 ON REMARKD.YEAR = DETAIL01.YEAR ");
            stb.append("          AND REMARKD.SEMESTER = DETAIL01.SEMESTER ");
            stb.append("          AND REMARKD.SCHREGNO = DETAIL01.SCHREGNO ");
            stb.append("          AND DETAIL01.DIV = '01' ");
            stb.append("          AND DETAIL01.CODE = '01' ");
            stb.append("     LEFT JOIN HREPORTREMARK_DETAIL_DAT DETAIL03 ON REMARKD.YEAR = DETAIL03.YEAR ");
            stb.append("          AND REMARKD.SEMESTER = DETAIL03.SEMESTER ");
            stb.append("          AND REMARKD.SCHREGNO = DETAIL03.SCHREGNO ");
            stb.append("          AND DETAIL03.DIV = '01' ");
            stb.append("          AND DETAIL03.CODE = '03' ");
            stb.append("     LEFT JOIN HREPORTREMARK_DETAIL_DAT DETAIL05 ON REMARKD.YEAR = DETAIL05.YEAR ");
            stb.append("          AND REMARKD.SEMESTER = DETAIL05.SEMESTER ");
            stb.append("          AND REMARKD.SCHREGNO = DETAIL05.SCHREGNO ");
            stb.append("          AND DETAIL05.DIV = '01' ");
            stb.append("          AND DETAIL05.CODE = '05' ");
            stb.append("     LEFT JOIN HREPORTREMARK_DETAIL_DAT DETAIL06 ON REMARKD.YEAR = DETAIL06.YEAR ");
            stb.append("          AND REMARKD.SEMESTER = DETAIL06.SEMESTER ");
            stb.append("          AND REMARKD.SCHREGNO = DETAIL06.SCHREGNO ");
            stb.append("          AND DETAIL06.DIV = '01' ");
            stb.append("          AND DETAIL06.CODE = '06' ");
            stb.append(" WHERE ");
            stb.append("     REMARKD.YEAR = '" + param._year + "' ");
            stb.append("     AND REMARKD.SCHREGNO = ? ");

            return stb.toString();
        }

    }

    private static class ScoreData {
        final String _score;
        final String _avg;
        final String _classRank;
        private ScoreData(
                final String score,
                final String avg,
                final String classRank
        ) {
            _score = score;
            _avg = avg;
            _classRank = classRank;
        }
    }

    private static class Attendance {
        final int _lesson;
        final int _mLesson;
        final int _suspend;
        final int _mourning;
        final int _sick;
        final int _present;
        final int _late;
        final int _early;
        final int _abroad;
        Attendance(
                final int lesson,
                final int mLesson,
                final int suspend,
                final int mourning,
                final int sick,
                final int present,
                final int late,
                final int early,
                final int abroad
        ) {
            _lesson = lesson;
            _mLesson = mLesson;
            _suspend = suspend;
            _mourning = mourning;
            _sick = sick;
            _present = present;
            _late = late;
            _early = early;
            _abroad = abroad;
        }

        private static void load(
                final DB2UDB db2,
                final Param param,
                final List studentList,
                final DateRange dateRange
        ) {
            log.info(" attendance = " + dateRange);
            if (null == dateRange || null == dateRange._sdate || null == dateRange._edate || dateRange._sdate.compareTo(param._date) > 0) {
                return;
            }
            final String edate = dateRange._edate.compareTo(param._date) > 0 ? param._date : dateRange._edate;
            PreparedStatement psAtSeme = null;
            ResultSet rsAtSeme = null;
            try {
                param._attendParamMap.put("schregno", "?");

                final String sql = AttendAccumulate.getAttendSemesSql(
                        param._year,
                        param._semester,
                        dateRange._sdate,
                        edate,
                        param._attendParamMap
                );
                log.debug(" attend sql = " + sql);
                psAtSeme = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    psAtSeme.setString(1, student._schregno);
                    rsAtSeme = psAtSeme.executeQuery();

                    while (rsAtSeme.next()) {
                        if (!SEMEALL.equals(rsAtSeme.getString("SEMESTER"))) {
                            continue;
                        }

                        final Attendance attendance = new Attendance(
                                rsAtSeme.getInt("LESSON"),
                                rsAtSeme.getInt("MLESSON"),
                                rsAtSeme.getInt("SUSPEND"),
                                rsAtSeme.getInt("MOURNING"),
                                rsAtSeme.getInt("SICK"),
                                rsAtSeme.getInt("PRESENT"),
                                rsAtSeme.getInt("LATE"),
                                rsAtSeme.getInt("EARLY"),
                                rsAtSeme.getInt("TRANSFER_DATE")
                        );
                        student._attendMap.put(dateRange._key, attendance);
                    }
                    DbUtils.closeQuietly(rsAtSeme);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(psAtSeme);
                db2.commit();
            }
        }
    }

    private static class SubclassAttendance {
        final BigDecimal _lesson;
        final BigDecimal _attend;
        final BigDecimal _sick;
        final BigDecimal _late;
        final BigDecimal _early;
        boolean _isOver;

        public SubclassAttendance(final BigDecimal lesson, final BigDecimal attend, final BigDecimal sick, final BigDecimal late, final BigDecimal early) {
            _lesson = lesson;
            _attend = attend;
            _sick = sick;
            _late = late;
            _early = early;
        }

        public String toString() {
            return "SubclassAttendance(" + _sick == null ? null : sishaGonyu(_sick.toString())  + "/" + _lesson + ")";
        }

        private static void load(final DB2UDB db2,
                final Param param,
                final List studentList,
                final DateRange dateRange) {
            log.info(" subclass attendance dateRange = " + dateRange);
            if (null == dateRange || null == dateRange._sdate || null == dateRange._edate || dateRange._sdate.compareTo(param._date) > 0) {
                return;
            }
            final String edate = dateRange._edate.compareTo(param._date) > 0 ? param._date : dateRange._edate;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                param._attendParamMap.put("schregno", "?");

                final String sql = AttendAccumulate.getAttendSubclassSql(
                        param._year,
                        SEMEALL,
                        dateRange._sdate,
                        edate,
                        param._attendParamMap
                );

                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();

                    while (rs.next()) {
                        if (!SEMEALL.equals(rs.getString("SEMESTER"))) {
                            continue;
                        }
                        final String subclasscd = rs.getString("SUBCLASSCD");

                        final SubclassMst mst = (SubclassMst) param._subclassMstMap.get(subclasscd);
                        if (null == mst) {
                            log.warn("no subclass : " + subclasscd);
                            continue;
                        }
                        final int iclasscd = Integer.parseInt(subclasscd.substring(0, 2));
                        if ((Integer.parseInt(KNJDefineSchool.subject_D) <= iclasscd && iclasscd <= Integer.parseInt(KNJDefineSchool.subject_U) || iclasscd == Integer.parseInt(KNJDefineSchool.subject_T))) {
                            if (null == student._printSubclassMap.get(subclasscd)) {
                                continue;
                            }

                            final BigDecimal lesson = rs.getBigDecimal("MLESSON");
                            final BigDecimal rawSick = rs.getBigDecimal("SICK1");
                            final BigDecimal sick = rs.getBigDecimal("SICK2");
                            final BigDecimal rawReplacedSick = rs.getBigDecimal("RAW_REPLACED_SICK");
                            final BigDecimal replacedSick = rs.getBigDecimal("REPLACED_SICK");
                            final BigDecimal late = rs.getBigDecimal("LATE");
                            final BigDecimal early = rs.getBigDecimal("EARLY");

                            final BigDecimal sick1 = mst._isSaki ? rawReplacedSick : rawSick;
                            final BigDecimal attend = lesson.subtract(null == sick1 ? BigDecimal.valueOf(0) : sick1);
                            final BigDecimal sick2 = mst._isSaki ? replacedSick : sick;

                            final BigDecimal absenceHigh = rs.getBigDecimal("ABSENCE_HIGH");

                            final SubclassAttendance subclassAttendance = new SubclassAttendance(lesson, attend, sick2, late, early);

                            //欠課時数上限
                            final Double absent = Double.valueOf(mst._isSaki ? rs.getString("REPLACED_SICK"): rs.getString("SICK2"));
                            subclassAttendance._isOver = subclassAttendance.judgeOver(absent, absenceHigh);

                            Map setSubAttendMap = null;
                            if (student._attendSubClassMap.containsKey(subclasscd)) {
                                setSubAttendMap = (Map) student._attendSubClassMap.get(subclasscd);
                            } else {
                                setSubAttendMap = new TreeMap();
                            }
                            setSubAttendMap.put(dateRange._key, subclassAttendance);

                            student._attendSubClassMap.put(subclasscd, setSubAttendMap);
                        }

                    }

                    DbUtils.closeQuietly(rs);
                }

            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        /**
         * 欠課時数超過ならTrueを戻します。
         * @param absent 欠課時数
         * @param absenceHigh 超過対象欠課時数（CREDIT_MST）
         * @return
         */
        private boolean judgeOver(final Double absent, final BigDecimal absenceHigh) {
            if (null == absent || null == absenceHigh) {
                return false;
            }
            if (0.1 > absent.floatValue() || 0.0 == absenceHigh.doubleValue()) {
                return false;
            }
            if (absenceHigh.doubleValue() < absent.doubleValue()) {
                return true;
            }
            return false;
        }
    }

    private static class Semester {
        final String _semester;
        final String _semestername;
        final DateRange _dateRange;
        public Semester(final String semester, final String semestername, final String sdate, final String edate) {
            _semester = semester;
            _semestername = semestername;
            _dateRange = new DateRange(_semester, _semestername, sdate, edate);
        }
    }

    private static class SubclassMst implements Comparable {
        final String _specialDiv;
        final String _classcd;
        final String _subclasscd;
        final String _classname;
        final String _subclassname;
        final Integer _classShoworder3;
        final Integer _subclassShoworder3;
        final boolean _isSaki;
        final boolean _isMoto;
        final String _calculateCreditFlg;
        public SubclassMst(final String specialDiv, final String classcd, final String subclasscd, final String classname, final String subclassname,
                final Integer classShoworder3,
                final Integer subclassShoworder3,
                final boolean isSaki, final boolean isMoto, final String calculateCreditFlg) {
            _specialDiv = specialDiv;
            _classcd = classcd;
            _subclasscd = subclasscd;
            _classname = classname;
            _subclassname = subclassname;
            _classShoworder3 = classShoworder3;
            _subclassShoworder3 = subclassShoworder3;
            _isSaki = isSaki;
            _isMoto = isMoto;
            _calculateCreditFlg = calculateCreditFlg;
        }
        public int compareTo(final Object o) {
            final SubclassMst mst = (SubclassMst) o;
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

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
    	log.fatal("$Revision: 72656 $");
    	KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _semester;
        final String _gradeHrClass;
        final String _grade;
        final String[] _categorySelected;
        final String _date;
        final String _ninteiDate;
        private boolean _isLastSemester;
        private String _schoolkind;
        boolean _isGakunensei = false;
        final String _useAssessCourseMst;

        final Map _d082;

        private String _certifSchoolSchoolName;
        private String _certifSchoolJobName;
        private String _certifSchoolPrincipalName;

        /** 端数計算共通メソッド引数 */
        private final Map _attendParamMap;

        private Map _semesterMap;
        private Map _subclassMstMap;
        private List _d026List = new ArrayList();
        Map _attendRanges;

        final String _documentroot;
        final String _imagepath;
        final String _schoolLogoImagePath;

        private boolean _isNoPrintMoto;
        private boolean _isPrintSakiKamoku;
        private final boolean _isOutputDebug;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _date = request.getParameter("DATE").replace('/', '-');
            _ninteiDate = request.getParameter("NINTEI_DATE").replace('/', '-');
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            _grade = null != _gradeHrClass && _gradeHrClass.length() > 2 ? _gradeHrClass.substring(0, 2) : null;
            _categorySelected = request.getParameterValues("category_selected");
            _useAssessCourseMst = request.getParameter("useAssessCourseMst");
            _schoolkind = getSchoolKind(db2);
            _isGakunensei = "0".equals(getSchoolDiv(db2));
            
            try {
            	final Map paramMap = new HashMap();
            	if (KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND")) {
            		paramMap.put("SCHOOL_KIND", KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ")));
            	}
            	final KNJSchoolMst knjSchoolMst = new KNJSchoolMst(db2, _year, paramMap);
            	_isLastSemester = null != knjSchoolMst && _semester.equals(knjSchoolMst._semesterDiv);
            } catch (Exception e) {
            	log.fatal("exception!", e);
            }

            _d082 = getD082(db2);
            loadNameMstD026(db2);
            loadNameMstD016(db2);
            setPrintSakiKamoku(db2);

            setCertifSchoolDat(db2);

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _grade);
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
            _attendParamMap.put("useCurriculumcd", "1");

            _semesterMap = loadSemester(db2);
            _attendRanges = new HashMap();
            for (final Iterator it = _semesterMap.keySet().iterator(); it.hasNext();) {
                final String semester = (String) it.next();
                final Semester oSemester = (Semester) _semesterMap.get(semester);
                _attendRanges.put(semester, oSemester._dateRange);
            }
            setSubclassMst(db2);

            _documentroot = request.getParameter("DOCUMENTROOT");
            final String sqlControlMst = " SELECT IMAGEPATH FROM CONTROL_MST WHERE CTRL_NO = '01' ";
            _imagepath = KnjDbUtils.getOne(KnjDbUtils.query(db2, sqlControlMst));
            _schoolLogoImagePath = getImageFilePath("SCHOOLLOGO.jpg");
            
            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));
        }
        
        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD185O' AND NAME = '" + propName + "' "));
        }

        private Map getD082(
                final DB2UDB db2
        ) {
            final Map retMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     * ");
                stb.append(" FROM ");
                stb.append("     NAME_MST D082 ");
                stb.append(" WHERE ");
                stb.append("     NAMECD1 = 'D082' ");
                ps = db2.prepareStatement(stb.toString());

                rs = ps.executeQuery();
                while (rs.next()) {
                    retMap.put(rs.getString("NAME1"), rs.getString("NAME1"));
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        /**
         * 年度の開始日を取得する
         */
        private Map loadSemester(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map map = new HashMap();
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
                String lastSemester = null;
                while (rs.next()) {
                    map.put(rs.getString("SEMESTER"), new Semester(rs.getString("SEMESTER"), rs.getString("SEMESTERNAME"), rs.getString("SDATE"), rs.getString("EDATE")));
                    if (!SEMEALL.equals(rs.getString("SEMESTER"))) {
                        lastSemester = rs.getString("SEMESTER");
                    }
                }
                _isLastSemester = null != lastSemester && lastSemester.equals(_semester);
            } catch (final Exception ex) {
                log.error("テスト項目のロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return map;
        }

        private String getSchoolKind(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2," SELECT GDAT.SCHOOL_KIND FROM SCHREG_REGD_GDAT GDAT WHERE GDAT.YEAR = '" + _year + "' AND GDAT.GRADE = '" + _grade + "' "));
        }

        private void setSubclassMst(
                final DB2UDB db2
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _subclassMstMap = new LinkedMap();
            try {
                String sql = "";
                sql += " WITH REPL AS ( ";
                sql += " SELECT '1' AS DIV, COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD AS SUBCLASSCD, CAST(NULL AS VARCHAR(1)) AS CALCULATE_CREDIT_FLG FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _year + "' GROUP BY COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD ";
                sql += " UNION ";
                sql += " SELECT '2' AS DIV, ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD AS SUBCLASSCD, MAX(CALCULATE_CREDIT_FLG) AS CALCULATE_CREDIT_FLG FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _year + "' GROUP BY ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD ";
                sql += " ) ";
                sql += " SELECT ";
                sql += " VALUE(T2.SPECIALDIV, '0') AS SPECIALDIV, ";
                sql += " T1.CLASSCD, ";
                sql += " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
                sql += " VALUE(T2.CLASSORDERNAME2, T2.CLASSNAME) AS CLASSNAME, VALUE(T1.SUBCLASSORDERNAME2, T1.SUBCLASSNAME) AS SUBCLASSNAME, ";
                sql += " CASE WHEN L1.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_SAKI, ";
                sql += " CASE WHEN L2.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_MOTO, ";
                sql += " L2.CALCULATE_CREDIT_FLG, ";
                sql += " VALUE(T2.SHOWORDER3, 999) AS CLASS_SHOWORDER3, ";
                sql += " VALUE(T1.SHOWORDER3, 999) AS SUBCLASS_SHOWORDER3 ";
                sql += " FROM SUBCLASS_MST T1 ";
                sql += " LEFT JOIN REPL L1 ON L1.DIV = '1' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L1.SUBCLASSCD ";
                sql += " LEFT JOIN REPL L2 ON L2.DIV = '2' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L2.SUBCLASSCD ";
                sql += " LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final boolean isSaki = "1".equals(rs.getString("IS_SAKI"));
                    final boolean isMoto = "1".equals(rs.getString("IS_MOTO"));
                    final SubclassMst mst = new SubclassMst(rs.getString("SPECIALDIV"), rs.getString("CLASSCD"), rs.getString("SUBCLASSCD"), rs.getString("CLASSNAME"), rs.getString("SUBCLASSNAME"), new Integer(rs.getInt("CLASS_SHOWORDER3")), new Integer(rs.getInt("SUBCLASS_SHOWORDER3")), isSaki, isMoto, rs.getString("CALCULATE_CREDIT_FLG"));
                    _subclassMstMap.put(rs.getString("SUBCLASSCD"), mst);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private void setCertifSchoolDat(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK2, REMARK4, REMARK5 FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '104' ");
            log.debug("certif_school_dat sql = " + sql.toString());

            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql.toString()));

            _certifSchoolSchoolName = StringUtils.defaultString(KnjDbUtils.getString(row, "SCHOOL_NAME"));
            _certifSchoolJobName = StringUtils.defaultString(KnjDbUtils.getString(row, "JOB_NAME"), "校長");
            _certifSchoolPrincipalName = StringUtils.defaultString(KnjDbUtils.getString(row, "PRINCIPAL_NAME"));
        }

        public String getImageFilePath(final String name) {
            final String path = _documentroot + "/" + (null == _imagepath || "".equals(_imagepath) ? "" : _imagepath + "/") + name;
            final boolean exists = new java.io.File(path).exists();
            if (exists) {
                return path;
            }
            log.warn(" path " + path + " exists: " + exists);
            return null;
        }

        private void loadNameMstD016(final DB2UDB db2) {
            _isNoPrintMoto = false;
            final String sql = "SELECT NAMECD2, NAMESPARE1, NAMESPARE2 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D016' AND NAMECD2 = '01' ";
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql));
            if ("Y".equals(KnjDbUtils.getString(row, "NAMESPARE1"))) _isNoPrintMoto = true;
            log.info("(名称マスタD016):元科目を表示しない = " + _isNoPrintMoto);
        }

        /**
         * 合併先科目を印刷するか
         */
        private void setPrintSakiKamoku(final DB2UDB db2) {
            // 初期値：印刷する
            _isPrintSakiKamoku = true;
            // 名称マスタ「D021」「01」から取得する
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, "SELECT NAMESPARE3 FROM V_NAME_MST WHERE YEAR='" + _year+ "' AND NAMECD1 = 'D021' AND NAMECD2 = '01' "));
            if ("Y".equals(KnjDbUtils.getString(row, "NAMESPARE3"))) {
                _isPrintSakiKamoku = false;
            }
            log.debug("合併先科目を印刷するか：" + _isPrintSakiKamoku);
        }

        private void loadNameMstD026(final DB2UDB db2) {

            final StringBuffer sql = new StringBuffer();
                final String field = SEMEALL.equals(_semester) ? "NAMESPARE1" : "ABBV" + _semester;
                sql.append(" SELECT NAME1 AS SUBCLASSCD FROM V_NAME_MST ");
                sql.append(" WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D026' AND " + field + " = '1'  ");

            _d026List.clear();
            _d026List.addAll(KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, sql.toString()), "SUBCLASSCD"));
            log.info("非表示科目:" + _d026List);
        }

        private String getSchoolDiv(final DB2UDB db2) {
            String schoolDiv = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = " SELECT SCHOOLDIV FROM SCHOOL_MST WHERE YEAR = '" + _year + "' AND SCHOOL_KIND = '" + _schoolkind + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    schoolDiv = rs.getString("SCHOOLDIV");
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return schoolDiv;
        }

    }
}

// eof
