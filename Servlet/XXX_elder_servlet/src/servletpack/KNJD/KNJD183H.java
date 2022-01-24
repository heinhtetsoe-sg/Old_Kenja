/*
 * $Id: 8ec189807add4ef591ec8cb6c98877d2ea81ad18 $
 *
 * 作成日: 2011/05/16
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;


import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 学校教育システム 賢者 [成績管理]  共愛学園・高校成績通知票
 */

public class KNJD183H {

    private static final Log log = LogFactory.getLog(KNJD183H.class);
    
    private static String SEMES9 = "9";
    
    private static String SEMES3 = "3";
    
    private static String CLASSCD90 = "90";
    
    private static String CLASSCD92_CHOHAI = "92";
    
    private static String SUBCLASSCD999999 = "999999";
    private static String SUBCLASSCD555555 = "555555";
    private static String SUBCLASSCD333333 = "333333";
    
    private static Clazz SUBCLASS999999CLAZZ = new Clazz(SUBCLASSCD999999, "", 999999);
    
    private static Subclass SUBCLASS999999 = new Subclass(SUBCLASS999999CLAZZ, SUBCLASSCD999999, "", 999999);
    
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
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
            svf.VrQuit();
        }
    }

    private void printHyosi(final Vrw32alp svf, final Student student) {
        svf.VrSetForm("KNJD183H_1.frm", 1);
        
        svf.VrsOut(getMS932ByteLength(student._name) < 20 ? "NAME" : "NAME2", student._name);
        svf.VrsOut(getMS932ByteLength(student._name) < 20 ? "NAME3" : "NAME4", student._name);
        svf.VrsOut(getMS932ByteLength(student._staffname) < 20 ? "STAFFNAME2_1" : "STAFFNAME2_2", student._staffname);
        svf.VrsOut("STAFFNAME1_1", _param._principalname);
        svf.VrsOut("SCHOOLNAME", _param._schoolname);
        
        svf.VrsOut("HR_NAME", _param._gradename + " " + _param._hrname + " ");
        svf.VrsOut("ATTENDNO", student._attendno);
        
        if (null != _param.getFilePath()) {
            svf.VrsOut("LOGO", _param.getFilePath());
        }
        
        svf.VrEndPage();
    }
    
    private void printStudentMain(final Vrw32alp svf, final Student student, final List<YearGrade> printGrade, final String pageMaxYear) {
        
        svf.VrSetForm("KNJD183H_2.frm", 4);
        
        printHeader(svf, student, printGrade);
        
        printSogotekinaGakushu(svf, student, printGrade);
        
        printShukketsu(svf, student, printGrade);
        
        printTokubetsukatsudo(svf, student, printGrade);
        
        printTanninran(svf, student);
        
        printSeiseki(svf, student, printGrade, pageMaxYear);
    }
    
    private void printTanninran(final Vrw32alp svf, final Student student) {
        final String tgtSemester = "9".equals(_param._semester) ? _param._ctrlSemester : _param._semester;
        HreportremarkDat hreportremarkDat = null;
        for (final HreportremarkDat hd : student.getHreportremarkDat(_param._year)) {
            if (null == hreportremarkDat && tgtSemester.equals(hd._semester)) {
                hreportremarkDat = hd;
            }
        }
        if (null != hreportremarkDat) {
            int i = 1;
            for (final String text : getToken(hreportremarkDat._communication, 100)) {
                svf.VrsOut("TEACHER_COMMENT" + i, text);
                i += 1;
            }
        }
    }

    private void printHeader(final Vrw32alp svf, final Student student, final List<YearGrade> printGrade) {
        svf.VrsOutn("GRADE_NAME1", 1, "第1学年");
        svf.VrsOutn("GRADE_NAME1", 2, "第2学年");
        svf.VrsOutn("GRADE_NAME1", 3, "第1学年");
        svf.VrsOutn("GRADE_NAME1", 4, "第2学年");
        svf.VrsOut(getMS932ByteLength(student._staffname) < 20 ? "STAFFNAME4_1" : "STAFFNAME4_2", student._staffname);
        svf.VrsOut("PREGRADE", _param._gradename);
        svf.VrsOut("PRECLASS", _param._hrname + "組");
        svf.VrsOut("PRENO",  student._attendno + "番");
        svf.VrsOut("NAME", student._name);
        svf.VrsOut("COURSE", student._majorname);
        
        for (final Map map : student._regdMapList) {
            if (!contains((String) map.get("YEAR"), (String) map.get("GRADE_CD"), printGrade)) {
                continue;
            }
            final String gradeCd = (String) map.get("GRADE_CD");
            final String hrclassName1 = (String) map.get("HR_CLASS_NAME1");
            final String attendno = (String) map.get("ATTENDNO");
            final String staffname = (String) map.get("STAFFNAME");
            final int i = NumberUtils.isDigits(gradeCd) ? Integer.parseInt(gradeCd) : -1;
            svf.VrsOutn("GRADE", i, NumberUtils.isDigits(gradeCd) ? String.valueOf(Integer.parseInt(gradeCd)) : gradeCd);
            svf.VrsOutn("HR_CLASS", i, hrclassName1);
            svf.VrsOutn("NO", i, NumberUtils.isDigits(attendno) ? String.valueOf(Integer.parseInt(attendno)) : attendno);
            svf.VrsOutn(getMS932ByteLength(staffname) < 20 ? "STAFFNAME1_1" : "STAFFNAME1_2", i, staffname);
        }
        
        svf.VrsOut("SOGO1", getSogoSubclassname(student));
    }
    
    public String getSogoSubclassname(final Student student) {
		final int tankyuStartYear = 2019;
		boolean isTankyu = false;
		if (NumberUtils.isDigits(student._curriculumYear)) {
			isTankyu = Integer.parseInt(student._curriculumYear) >= tankyuStartYear;
		} else {
			final int year = NumberUtils.isDigits(_param._year) ? Integer.parseInt(_param._year) : 9999;
			final int gradeCdInt = NumberUtils.isDigits(_param._gradeCd) ? Integer.parseInt(_param._gradeCd) : 99;
			if (year == tankyuStartYear     && gradeCdInt <= 1
			 || year == tankyuStartYear + 1 && gradeCdInt <= 2
			 || year == tankyuStartYear + 2 && gradeCdInt <= 3
			 || year >= tankyuStartYear + 3
					) {
				isTankyu = true;
			}
		}
		return isTankyu ? "総合的な探究の時間" : "総合的な学習の時間";
	}

    private boolean contains(final String year, final String gradeCd, final List printGrade) {
        for (final Iterator it = printGrade.iterator(); it.hasNext();) {
            final YearGrade yg = (YearGrade) it.next();
            if (yg._year.equals(year) && yg._gradeCd.equals(gradeCd)) {
                return true;
            }
        }
        return false;
    }

    private Credits getClass90Credits(final Map<Clazz, List<RecordSubclass>> clazzRecordSubclassmap, final List<YearGrade> printGrade) {
        Clazz clazz90 = null;
        for (final Clazz clazz : clazzRecordSubclassmap.keySet()) {
            if (clazz._clazzcd.equals(CLASSCD90)) {
                clazz90 = clazz;
                break;
            }
        }
        final Credits class90Credits = new Credits();
        if (null != clazz90) {
            final List<RecordSubclass> recordSubclassList = clazzRecordSubclassmap.get(clazz90);
            
            // 総合的な学習の時間は表示する
            for (final RecordSubclass recSubclass : recordSubclassList) {
                final Credits credits = new Credits();
                credits.addSubclass(recSubclass, printGrade);
                class90Credits.add(credits);
            }
        }
        return class90Credits;
    }

    private void printSeiseki(final Vrw32alp svf, final Student student, final List<YearGrade> printGrade, final String pageMaxYear) {
        final int MAX_LINE = 21;
        final Map<Clazz, List<RecordSubclass>> clazzRecordSubclassmap = student.getClazzRecordSubclassMap();
        
        if (_param._isOutputDebug) {
        	for (Clazz clazz : clazzRecordSubclassmap.keySet()) {
        		log.info(" " + student._schregno + " " + clazz + " " + clazzRecordSubclassmap.get(clazz));
        	}
        }
        
        final Credits totalCredits = new Credits();
        
        // 総合的な学習の時間（教科コード90）
        final Credits class90Credits = getClass90Credits(clazzRecordSubclassmap, student._printGrade);
        printCreditsKotei(svf, class90Credits, "TOTALACT_CREDIT", "TOTALACT_SUBTOTAL_CREDIT", printGrade, pageMaxYear); // 総合的な学習の時間
        
        totalCredits.add(class90Credits);
        printCreditsKotei(svf, totalCredits, "SUBTOTAL_CREDIT", "TOTAL_CREDIT", printGrade, pageMaxYear); // 科目ごと修得単位数計表示
        
        // 評定平均・クラス順位
        printRankAvg(svf, _param, clazzRecordSubclassmap.get(SUBCLASS999999CLAZZ), student._isPrintLetterGrade, printGrade);
        
        int line = 0;
        int classnameindex;
        boolean isUpper = true;
        for (final Clazz clazz : clazzRecordSubclassmap.keySet()) { // 教科
            if (clazz._clazzcd.compareTo(CLASSCD90) >= 0) { // 教科コード90以上は以下では表示しない
                continue; 
            }
            
            final List<RecordSubclass> recordSubclassList = clazzRecordSubclassmap.get(clazz);
            final String tmpclazzname = null == clazz._clazzname ? "" : clazz._clazzname;
            final int clazznamelen = tmpclazzname.length() + (tmpclazzname.length() % 2 != 0 ? 1 : 0);
            final int recordlen = recordSubclassList.size() + (recordSubclassList.size() % 2 != 0 ? 1 : 0);
            final int subclasslinelength = (clazznamelen >= recordlen * 2) ? clazznamelen / 2 + (clazznamelen % 2 != 0 ? 1 : 0) : recordlen;
            
            final List classnameList = getClassnameArrayList(clazz._clazzname, subclasslinelength);
            classnameindex = 0;
            
            for (int subclassline = 0; subclassline < subclasslinelength; subclassline++) { // 科目
                final String[] classnameArray = classnameindex >= classnameList.size() ? new String[]{""} : (String[]) classnameList.get(classnameindex);
                final RecordSubclass recSubclass = subclassline >= recordSubclassList.size() ? null : (RecordSubclass) recordSubclassList.get(subclassline);
                final String suf = isUpper ? "1" : "2"; 
                
                printSubclassLine(svf, clazz._clazzcd, classnameArray, recSubclass, student._isPrintLetterGrade, suf, printGrade); // 科目の1行
                
                final Credits credits = new Credits();
                credits.addSubclass(recSubclass, student._printGrade);

                printCreditsRecord(svf, credits, "CREDIT", "SUBCLASS_SUBTOTAL_CREDIT", suf, printGrade, pageMaxYear); // 修得単位数表示
                totalCredits.add(credits);
                printCreditsKotei(svf, totalCredits, "SUBTOTAL_CREDIT", "TOTAL_CREDIT", printGrade, pageMaxYear); // 科目ごと修得単位数計表示
                
                isUpper = !isUpper;
                if (isUpper) {
                    line += 1;
                    svf.VrEndRecord();
                }
                classnameindex += 1;
            }
            if (!isUpper) {
                isUpper = !isUpper;
                line += 1;
                svf.VrEndRecord();
            }
        }
        if (!isUpper) {
            isUpper = !isUpper;
            line += 1;
            svf.VrEndRecord();
        }
        for (int i = line % MAX_LINE + (line % MAX_LINE == 0 ? MAX_LINE : 0); i < MAX_LINE; i++) {
            printSubclassLine(svf, String.valueOf(line), null, null, student._isPrintLetterGrade, null, printGrade);
            svf.VrEndRecord();
        }
    }
    
    private static String getSubclasscd(final ResultSet rs, final Param param) throws SQLException {
        final String subclassCd = rs.getString("SUBCLASSCD");
        if (SUBCLASSCD999999.equals(subclassCd) || SUBCLASSCD333333.equals(subclassCd) || SUBCLASSCD555555.equals(subclassCd)) {
            return subclassCd;
        }
        if ("1".equals(param._useCurriculumcd)) {
            return rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-" + rs.getString("CURRICULUM_CD") + "-" + subclassCd;
        }
        return subclassCd;
    }
    
    private void printRankAvg(final Vrw32alp svf, final Param param, final List recSubclass999999List, final boolean isPrintLetterGrade, final List printGrade) {
        if (null == recSubclass999999List || recSubclass999999List.isEmpty()) {
            return;
        }
        
        final RecordSubclass recSubclass999999 = (RecordSubclass) recSubclass999999List.get(0);
        // 年度ごとに表示
        for (final Iterator ityg = printGrade.iterator(); ityg.hasNext();) {
            
            final YearGrade yg = (YearGrade) ityg.next();
            final int gi = (Integer.parseInt(yg._gradeCd) - 1) * 3;
            
            // 学期ごとに表示
            for (final Iterator its = yg._semesters.values().iterator(); its.hasNext();) {
                
                final Semester semester = (Semester) its.next();
                final int idx = gi + (SEMES9.equals(semester._semes) ? 3 : Integer.parseInt(semester._semes));
                
                final RecordScore recScore = recSubclass999999.getRecordScore(semester);
                if (null != recScore) {
                    final String avg;
                    final String hrRank;
                    final String hrCount;
                    if (isPrintLetterGrade && yg.isPrintLetterGrade()) {
                        avg = recScore._vAvg;
                        hrRank = recScore._vHrRank;
                        hrCount = recScore._vHrCount;
                    } else {
                        avg = recScore._avg;
                        hrRank = recScore._hrRank;
                        hrCount = recScore._hrCount;
                    }
                    
                    if (null != avg) {
                        svf.VrsOut("AVERAGE_RATE" + idx, new BigDecimal(avg).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    }
                    svf.VrsOut("RANK" + idx, hrRank);
                    svf.VrsOut("DENOMI" + idx, hrCount);
                }
            }
        }
    }
    
    private void printCreditsRecord(final Vrw32alp svf, final Credits credits,
            final String field1, final String field2, final String suf, final List printGrade, final String pageMaxYear) {
        
        int totalCredit = -1;
        for (final Iterator ityg = credits._ygCredits.keySet().iterator(); ityg.hasNext();) {
            
            final YearGrade yg = (YearGrade) ityg.next();
            final Integer credit = (Integer) credits._ygCredits.get(yg);
            if (printGrade.contains(yg)) {
                svf.VrsOut(field1 + (Integer.parseInt(yg._gradeCd)) + "_" + suf, String.valueOf(credit));
            }
            if (!yg._isRyunen && null != pageMaxYear && yg._year.compareTo(pageMaxYear) <= 0) {
                totalCredit = (-1 == totalCredit ? 0 : totalCredit) + credit.intValue();
            }
        }
        if (-1 != totalCredit) {
            svf.VrsOut(field2 + suf, String.valueOf(totalCredit)); // 年度ごと修得単位数計
        }
    }
    
    private void printCreditsKotei(final Vrw32alp svf, final Credits credits,
            final String field1, final String field2, final List<YearGrade> printGrade, final String pageMaxYear) {
        
        int totalCredit = -1;
        for (final YearGrade yg : credits._ygCredits.keySet()) {
            
            final Integer credit = (Integer) credits._ygCredits.get(yg);
            if (printGrade.contains(yg)) {
                svf.VrsOut(field1 + (Integer.parseInt(yg._gradeCd)), String.valueOf(credit));
            }
            if (!yg._isRyunen && null != pageMaxYear && yg._year.compareTo(pageMaxYear) <= 0) {
                totalCredit = (-1 == totalCredit ? 0 : totalCredit) + credit.intValue();
            }
        }
        if (-1 != totalCredit) {
            svf.VrsOut(field2, String.valueOf(totalCredit));
        }
    }
    
    private void printSubclassLine(final Vrw32alp svf, final String clazzcd, final String[] clazznames,
            final RecordSubclass recSubclass, final boolean isPrintLetterGrade, final String suf, final List<YearGrade> printGrade) {
        svf.VrsOut("SUBJECTGRP", clazzcd);
        if (null == clazznames) {
            svf.VrsOut("CLASSNAME1", "DUMMY");
            svf.VrAttribute("CLASSNAME1", "X=10000");
            return;
        }
        if (clazznames.length > 1) {
            svf.VrsOut("CLASSNAME2_" + suf + "_1", clazznames[0]);
            svf.VrsOut("CLASSNAME2_" + suf + "_2", clazznames[1]);
        } else {
            svf.VrsOut("CLASSNAME1_" + suf, clazznames[0]);
        }
        
        if (null != recSubclass) {
            svf.VrsOut("SUBCLASS_NAME" + suf + (getMS932ByteLength(recSubclass._subclass._subclassname) > 20 ? "_2" : ""), recSubclass._subclass._subclassname); // 科目名
            
            // 学年ごとに表示
            for (final YearGrade yg : printGrade) {
                
                final int gi = (Integer.parseInt(yg._gradeCd) - 1) * 3;
                
                // 学期ごとに表示
                for (final Semester semester : yg._semesters.values()) {
                    
                    if (SEMES3.equals(semester._semes)) { // 3学期は表示なし
                        continue;
                    }
                    
                    final int idx = gi + (SEMES9.equals(semester._semes) ? 3 : Integer.parseInt(semester._semes));
                    
                    final RecordScore recScore = recSubclass.getRecordScore(semester);
                    if (null != recScore) {
                        svf.VrsOut("RATE" + idx + "_" + suf, isPrintLetterGrade && yg.isPrintLetterGrade() ? recScore._letterGradeMark : recScore._score); // 評定
                    }
                    
                    final SubclassAttendance subAtt = recSubclass.getAttendance(semester);
                    if (null != subAtt) {
                        svf.VrsOut("ABSENCE" + idx + "_" + suf, String.valueOf(subAtt._kekka)); // 欠課
                    }
                }
            }
        }
    }

    private void printTokubetsukatsudo(final Vrw32alp svf, final Student student, final List<YearGrade> printGrade) {
        for (final YearGrade yg : printGrade) {
            final int st1 = Integer.parseInt(yg._gradeCd);
            
            int i = 1;
            for (final Iterator itp = student.specialActremark(yg._year, 32).iterator(); itp.hasNext(); i++) {
                final String s = (String) itp.next();
                svf.VrsOutn("SPECIAL_ACT" + st1, i, s);
            }
        }
    }

    private void printShukketsu(final Vrw32alp svf, final Student student, final List<YearGrade> printGrade) {
        for (final Iterator ityg = printGrade.iterator(); ityg.hasNext();) {
            final YearGrade yg = (YearGrade) ityg.next();
            
            final String[] semess = {"1", "2", SEMES3};
            final Attendance dummy = new Attendance(null);
            final Attendance total = new Attendance(null);
            for (int i = 0; i < semess.length; i++) {
                final Semester semester = (Semester) yg._semesters.get(semess[i]);
                if (null == semester) {
                    continue;
                }
                
                final Attendance attendance = null == student._attendances.get(semester) ? dummy : (Attendance) student._attendances.get(semester);
                total.add(attendance);

                if (!SEMES3.equals(semess[i])) {
                    final int idx = ((Integer.parseInt(yg._gradeCd) - 1) * 3) + (i + 1);
                    svf.VrsOutn("LESSON",        idx, String.valueOf(attendance._lesson));
                    svf.VrsOutn("ABSENT",        idx, String.valueOf(attendance._kesseki));
                    svf.VrsOutn("PRAY_ABSENT",   idx, String.valueOf(attendance._reihaiKekka));
                    svf.VrsOutn("LESSON_ABSENT", idx, String.valueOf(attendance._kekkaJisu));
                    svf.VrsOutn("PRAY_LATE",     idx, String.valueOf(attendance._reihaiTikoku));
                    svf.VrsOutn("LESSON_LATE",   idx, String.valueOf(attendance._jugyouTikoku));
                    svf.VrsOutn("MOURNING",      idx, String.valueOf(attendance._suspendMourning));
                    svf.VrsOutn("EARLY",         idx, String.valueOf(attendance._early));
                }
            }
            
            final Semester semester = student.getSemester(yg._year, SEMES9);
            if (null != semester) {
                final int idx = ((Integer.parseInt(yg._gradeCd) - 1) * 3) + 3;
                svf.VrsOutn("LESSON",        idx, String.valueOf(total._lesson));
                svf.VrsOutn("ABSENT",        idx, String.valueOf(total._kesseki));
                svf.VrsOutn("PRAY_ABSENT",   idx, String.valueOf(total._reihaiKekka));
                svf.VrsOutn("LESSON_ABSENT", idx, String.valueOf(total._kekkaJisu));
                svf.VrsOutn("PRAY_LATE",     idx, String.valueOf(total._reihaiTikoku));
                svf.VrsOutn("LESSON_LATE",   idx, String.valueOf(total._jugyouTikoku));
                svf.VrsOutn("MOURNING",      idx, String.valueOf(total._suspendMourning));
                svf.VrsOutn("EARLY",         idx, String.valueOf(total._early));
            }
            
            final List attendrecRemark = student.attendrecRemark(yg._year, 24);
            int i = 1;
            for (final Iterator it = attendrecRemark.iterator(); it.hasNext(); i += 1) {
                final String remark = (String) it.next();
                svf.VrsOutn("REMARK" + i, Integer.parseInt(yg._gradeCd), remark);
            }
        }
    }

    private void printSogotekinaGakushu(final Vrw32alp svf, final Student student, final List<YearGrade> printGrade) {
        final int MAX_LINE = 3;
        for (final YearGrade yg : printGrade) {
            if (Integer.parseInt(yg._gradeCd) >= 3) {
                continue; // 表示する学年は１学年、２学年のみ
            }
            final int st = Integer.parseInt(yg._gradeCd);
            
            final List totalstudytime = student.totalstudytime(yg._year, 120);
            int line1 = 1;
            for (final Iterator it = totalstudytime.iterator(); it.hasNext() && line1 <= MAX_LINE; line1++) {
                final String s = (String) it.next();
                svf.VrsOutn("TOTAL_ACT1_" + line1, 0 + st, s);
            }
            final List remark2 = student.remark1(yg._year, 120);
            int line2 = 1;
            for (final Iterator it = remark2.iterator(); it.hasNext() && line2 <= MAX_LINE; line2++) {
                final String s = (String) it.next();
                svf.VrsOutn("TOTAL_ACT1_" + line2, 2 + st, s);
            }
        }
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        
        final List<Student> studentList = Student.getStudentList(db2, _param);

        for (final Student student : studentList) {
            
            if (_param._isOutputDebug) {
            	log.info(" schregno = " + student._schregno);
            }
            student._printGrade = getPrintGrade(db2, student._schregno);
            
            Integer page = new Integer(1);
            final TreeMap<Integer, List<YearGrade>> pageMap = new TreeMap();
            for (final YearGrade yg : student._printGrade) {
                if (yg._isRyunenNext) {
                    page = new Integer(page.intValue() + 1);
                }
                if (null == pageMap.get(page)) {
                    pageMap.put(page, new ArrayList());
                }
                pageMap.get(page).add(yg);
            }

            for (final Integer p : pageMap.keySet()) {
                final List<YearGrade> printGrade = pageMap.get(p);
                final String pageMaxYear = (printGrade.get(printGrade.size() - 1))._year;
                
                if (_param._isOutputDebug) {
                	log.info(" page = " + p + ", pageMaxYear = " +  pageMaxYear + ", printGrade = " + printGrade);
                }
                
                load(db2, student, printGrade);
                
                printHyosi(svf, student);
                
                printStudentMain(svf, student, printGrade, pageMaxYear);
            }
            _hasData = true;
        }
    }
    
    private void load(final DB2UDB db2, final Student student, final List<YearGrade> printGrade) {
        
        student._recordSubclasses = RecordSubclass.load(db2, _param, student, student._printGrade);
        student._hreportremarkDatList = HreportremarkDat.load(db2, _param, student._schregno);
        student._hreportremarkDetailDatList = HreportremarkDetailDat.load(db2, _param, student._schregno);
        student._attendances = Attendance.load(db2, _param, student);
        student._regdMapList = getRegdGradeList(db2, _param, student._schregno);
        SubclassAttendance.setSubclassAttendance(db2, _param, student._schregno, student._recordSubclasses, printGrade);
    }
    
    private List<YearGrade> getPrintGrade(final DB2UDB db2, final String schregno) {
        final List printGrades = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = sqlPrintGrade(_param, schregno);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String year = rs.getString("YEAR");
                final String grade = rs.getString("GRADE");
                final boolean isRyunen = "1".equals(rs.getString("IS_RYUNEN"));
                final boolean isRyunenNext = "1".equals(rs.getString("IS_RYUNEN_NEXT"));
                final Map semesterMap = getSemesters(db2, year, _param._year, _param._grade, _param._semester, _param._date);
                printGrades.add(new YearGrade(year, grade, isRyunen, isRyunenNext, semesterMap));
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        
        Collections.sort(printGrades);
        return printGrades;
    }
    
    private String sqlPrintGrade(final Param param, final String schregno) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH REGD AS ( ");
        stb.append("  SELECT DISTINCT GRADE, YEAR ");
        stb.append("   FROM SCHREG_REGD_DAT T1 ");
        stb.append("   WHERE ");
        stb.append("   T1.SCHREGNO = '" + schregno + "' ");
        stb.append(" ) ");
        stb.append("  SELECT  ");
        stb.append("   T1.YEAR, ");
        stb.append("   T2.GRADE_CD AS GRADE, ");
        stb.append("   CASE WHEN  (T1.YEAR, T1.GRADE) NOT IN (SELECT MAX(YEAR), GRADE FROM REGD GROUP BY GRADE) THEN 1 ELSE 0 END AS IS_RYUNEN, ");
        stb.append("   CASE WHEN  (T1.YEAR, T1.GRADE) NOT IN (SELECT MIN(YEAR), GRADE FROM REGD GROUP BY GRADE) THEN 1 ELSE 0 END AS IS_RYUNEN_NEXT ");
        stb.append("  FROM REGD T1 ");
        stb.append("  INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE AND T2.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' ");
        stb.append("  WHERE T1.YEAR <= '" + _param._year + "' ");
        stb.append("  ORDER BY ");
        stb.append("   T1.YEAR ");
        return stb.toString();
    }
    
    private Map<String, Semester> getSemesters(final DB2UDB db2, final String year, final String paramYear, final String grade, final String paramSemester, String paramDate) {
        final Map semesters = new TreeMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String semesterMax = paramYear.equals(year) ? paramSemester : SEMES9;
            ps = db2.prepareStatement("SELECT * FROM V_SEMESTER_GRADE_MST WHERE YEAR = '" + year + "' AND SEMESTER <= '" + semesterMax + "' AND GRADE = '" + grade + "' ORDER BY SEMESTER ");
            rs = ps.executeQuery();
            while (rs.next()) {
                final String ssemes = rs.getString("SEMESTER");
                final String sdate = rs.getString("SDATE");
                final String edate = (paramYear.equals(year) && paramSemester.equals(ssemes)) ? paramDate : rs.getString("EDATE");
                final Semester semester = new Semester(year, ssemes, sdate, edate);
                semesters.put(ssemes, semester);
            }
        } catch (SQLException ex) {
            log.error("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return semesters;
    }
    
    private List<Map<String, String>> getRegdGradeList(final DB2UDB db2, final Param param, final String schregno) {
        final StringBuffer sql = new StringBuffer();
        sql.append(" WITH REGD_MAX_SEMESTER AS ( ");
        sql.append(" SELECT ");
        sql.append("     T1.SCHREGNO, T1.YEAR, MAX(SEMESTER) AS SEMESTER, T1.GRADE ");
        sql.append(" FROM ");
        sql.append("     SCHREG_REGD_DAT T1 ");
        sql.append(" WHERE ");
        sql.append("     T1.SCHREGNO = '" + schregno + "' ");
        sql.append("     AND (T1.YEAR < '" + param._year + "' ");
        sql.append("          OR (T1.YEAR = '" + param._year + "' ");
        if (SEMES9.equals(param._semester)) {
            sql.append("               AND T1.SEMESTER <= '" + param._ctrlSemester + "'");
        } else {
            sql.append("               AND T1.SEMESTER <= '" + param._semester + "'");
        }
        sql.append("          )) ");
        sql.append(" GROUP BY ");
        sql.append("     T1.SCHREGNO, T1.YEAR, T1.GRADE ");
        sql.append(" ) ");
        sql.append(" SELECT ");
        sql.append("     T1.YEAR, ");
        sql.append("     T1.GRADE, ");
        sql.append("     T2.GRADE_CD, ");
        sql.append("     T4.HR_CLASS_NAME1, ");
        sql.append("     T3.ATTENDNO, ");
        sql.append("     VALUE(T5.STAFFNAME, VALUE(T6.STAFFNAME, T7.STAFFNAME)) AS STAFFNAME ");
        sql.append(" FROM ");
        sql.append("     REGD_MAX_SEMESTER T1 ");
        sql.append("     INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR ");
        sql.append("         AND T2.GRADE = T1.GRADE ");
        sql.append("     INNER JOIN SCHREG_REGD_DAT T3 ON T3.YEAR = T1.YEAR ");
        sql.append("         AND T3.SEMESTER = T1.SEMESTER ");
        sql.append("         AND T3.GRADE = T1.GRADE ");
        sql.append("         AND T3.SCHREGNO = T1.SCHREGNO ");
        sql.append("     LEFT JOIN SCHREG_REGD_HDAT T4 ON T4.YEAR = T3.YEAR ");
        sql.append("         AND T4.SEMESTER = T3.SEMESTER ");
        sql.append("         AND T4.GRADE = T3.GRADE ");
        sql.append("         AND T4.HR_CLASS = T3.HR_CLASS ");
        sql.append("     LEFT JOIN STAFF_MST T5 ON T5.STAFFCD = T4.TR_CD1 ");
        sql.append("     LEFT JOIN STAFF_MST T6 ON T6.STAFFCD = T4.TR_CD2 ");
        sql.append("     LEFT JOIN STAFF_MST T7 ON T7.STAFFCD = T4.TR_CD3 ");
        sql.append(" ORDER BY ");
        sql.append("     T1.YEAR ");
        
        return KnjDbUtils.query(db2, sql.toString());
    }
    
    private int getMS932ByteLength(final String str) {
    	return KNJ_EditEdit.getMS932ByteLength(str);
    }

    public static List<String> getToken(final String str, final int len) {
        return KNJ_EditKinsoku.getTokenList(str, len, 30);
    }

    /**
     * 教科名を表示用に配列のリストに分割する。
     * @param clazzname 教科名
     * @param subclasslinesize 科目の列の長さ
     * @return 教科名を表示用に分割した配列のリスト
     */
    public static List getClassnameArrayList(final String clazzname, final int subclasslinesize) {
        final int charPerBlock = 1;
        final int block = subclasslinesize / charPerBlock + (subclasslinesize % charPerBlock != 0 ? 1 : 0);
        final int totalLine = block * charPerBlock;
        final List list = new ArrayList();
        if (null != clazzname) {
            if (clazzname.length() <= totalLine) {
                String tmp = StringUtils.center(clazzname, totalLine);
                while (tmp.length() > charPerBlock) {
                    list.add(new String[]{tmp.substring(0, charPerBlock)});
                    tmp = tmp.substring(charPerBlock);
                }
                if (tmp.length() != 0) {
                    list.add(new String[]{tmp});
                }
            } else {
                String tmp1 = clazzname.substring(0, totalLine);
                String tmp2 = clazzname.substring(totalLine);
                
                while (tmp1.length() > charPerBlock) {
                    final int tmp2CharPerBlock = Math.min(tmp2.length(), charPerBlock);
                    list.add(new String[]{tmp1.substring(0, charPerBlock), tmp2.substring(0, tmp2CharPerBlock)});
                    tmp1 = tmp1.substring(charPerBlock);
                    tmp2 = tmp2.substring(tmp2CharPerBlock);
                }
                if (tmp1.length() != 0) {
                    list.add(new String[]{tmp1, tmp2});
                }
            }
        }
        return list;
    }
    
    private static class Student {
        final String _schregno;
        String _name;
        String _staffname;
        String _attendno;
        String _majorname;
        String _curriculumYear;
        boolean _isPrintLetterGrade;
        Map<Subclass, RecordSubclass> _recordSubclasses; // Subclass と RecordSubclass のマップ
        List<HreportremarkDat> _hreportremarkDatList;
        List<HreportremarkDetailDat> _hreportremarkDetailDatList;
        Map<Semester, Attendance> _attendances;
        List<Map<String, String>> _regdMapList;
        List<YearGrade> _printGrade;
        Student(final String schregno) {
            _schregno = schregno;
            _recordSubclasses = Collections.EMPTY_MAP;
            _hreportremarkDatList = Collections.EMPTY_LIST;
            _hreportremarkDetailDatList = Collections.EMPTY_LIST;
            _attendances = Collections.EMPTY_MAP;
            _regdMapList = Collections.EMPTY_LIST;
            _printGrade = Collections.EMPTY_LIST;
        }
        
        public Semester getSemester(final String year, final String semes) {
            Semester semester = null;
            search:
            for (final YearGrade yg : _printGrade) {
                for (final Semester s : yg._semesters.values()) {
                    if (null != semes && semes.equals(s._semes) && null != year && year.equals(s._year)) {
                        semester = s;
                        break search;
                    }
                }
            }
            return semester;
        }

        public List<RecordSubclass> getRecordSubclassList(final Map<Clazz, List<RecordSubclass>> clazzRecordSubclassMap, final Clazz clazz) {
            if (!clazzRecordSubclassMap.containsKey(clazz)) {
                clazzRecordSubclassMap.put(clazz, new ArrayList());
            }
            return clazzRecordSubclassMap.get(clazz);
        }
        
        /**
         * @return ClazzとRecordSubclassのListのマップ
         */
        public Map<Clazz, List<RecordSubclass>> getClazzRecordSubclassMap() {
            final Map<Clazz, List<RecordSubclass>> m = new TreeMap();
            for (final RecordSubclass rs : _recordSubclasses.values()) {
                getRecordSubclassList(m, rs._subclass._clazz).add(rs);
            }
            return m;
        }
        
        private List<HreportremarkDat> getHreportremarkDat(final String year) {
            final List list = new ArrayList();
            for (final HreportremarkDat hreportremarkDat : _hreportremarkDatList) {
                if (hreportremarkDat._year.equals(year)) {
                    list.add(hreportremarkDat);
                }
            }
            return list;
        }
        
        private List getHreportremarkDetailDat(final String year, final String div, final String code) {
            final List list = new ArrayList();
            for (final Iterator iter = _hreportremarkDetailDatList.iterator(); iter.hasNext();) {
                final HreportremarkDetailDat hdd = (HreportremarkDetailDat) iter.next();
                if (hdd._year.equals(year) && hdd._div != null && hdd._div.equals(div) && hdd._code != null && hdd._code.equals(code)) {
                    list.add(hdd);
                }
            }
            return list;
        }
        
        public List specialActremark(final String year, final int keta) {
            final List list = new ArrayList();
            for (final Iterator it = getHreportremarkDetailDat(year, "03", "02").iterator(); it.hasNext();) {
                final HreportremarkDetailDat hd = (HreportremarkDetailDat) it.next();
                if (SEMES9.equals(hd._semester)) {
                    list.addAll(getToken(hd._remark1, keta));
                }
            }
            return list;
        }
        
        public List remark1(final String year, final int keta) {
            final List list = new ArrayList();
            for (final Iterator it = getHreportremarkDetailDat(year, "03", "01").iterator(); it.hasNext();) {
                final HreportremarkDetailDat hd = (HreportremarkDetailDat) it.next();
                if (SEMES9.equals(hd._semester)) {
                    list.addAll(getToken(hd._remark1, keta));
                }
            }
            return list;
        }
        
        public List totalstudytime(final String year, final int keta) {
            final List list = new ArrayList();
            for (final Iterator it = getHreportremarkDat(year).iterator(); it.hasNext();) {
                final HreportremarkDat hd = (HreportremarkDat) it.next();
                if (SEMES9.equals(hd._semester)) {
                    list.addAll(getToken(hd._totalstudytime, keta));
                }
            }
            return list;
        }
        
        public List attendrecRemark(final String year, final int keta) {
            final List list = new ArrayList();
            for (final Iterator it = getHreportremarkDat(year).iterator(); it.hasNext();) {
                final HreportremarkDat hd = (HreportremarkDat) it.next();
                if (!SEMES9.equals(hd._semester)) { // 9学期以外を連結して表示
                    list.addAll(getToken(hd._attendrecRemark, keta));
                }
            }
            return list;
        }
        
        public static List getStudentList(final DB2UDB db2, final Param param) {
            final List students = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            
            String sql = null;
            try {
                sql = prestatementRegd(param);
//                 log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                
                while (rs.next()) {
                    final Student student = new Student(rs.getString("SCHREGNO"));
                    students.add(student);
                    student._name = "1".equals(rs.getString("USE_REAL_NAME")) ? rs.getString("REAL_NAME") : rs.getString("NAME");
                    student._attendno = NumberUtils.isNumber(rs.getString("ATTENDNO")) ? String.valueOf(Integer.parseInt(rs.getString("ATTENDNO"))) : rs.getString("ATTENDNO");
                    student._staffname = rs.getString("STAFFNAME");
                    student._majorname = rs.getString("MAJORNAME");
                    student._isPrintLetterGrade = "1".equals(rs.getString("PRINT_LETTER_GRADE"));
                    student._curriculumYear = rs.getString("CURRICULUM_YEAR");
                }
                
            } catch (SQLException ex) {
                log.fatal("Exception: sql = " + sql, ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            
            return students;
        }
        /** 
         * <pre>
         * 生徒の学籍等の情報および総合的な学習の時間の所見・通信欄を取得するＳＱＬ文を戻します。
         * ・指定された生徒全員を対象とします。
         * </pre>
         */
        public static String prestatementRegd(final Param param) {
            final StringBuffer stb = new StringBuffer();
            // 異動者を除外した学籍の表 => 組平均において異動者の除外に使用
            stb.append("WITH SCHNO_A AS(");
            stb.append(    "SELECT  T1.YEAR, T1.SCHREGNO, T1.GRADE, T1.ATTENDNO, T1.SEMESTER, T1.COURSECD, T1.MAJORCD, T1.COURSECODE ");
            stb.append(    "FROM    SCHREG_REGD_DAT T1, V_SEMESTER_GRADE_MST T2 ");
            stb.append(    "WHERE   T1.YEAR = '" + param._year + "' ");
            if (SEMES9.equals(param._semester)) {
                stb.append(        "AND T1.SEMESTER = '"+ param._ctrlSemester +"' ");
            } else {
                stb.append(        "AND T1.SEMESTER = '"+ param._semester +"' ");
            }
            stb.append(        "AND T1.YEAR = T2.YEAR ");
            stb.append(        "AND T1.SEMESTER = T2.SEMESTER ");
            stb.append(        "AND T1.GRADE = T2.GRADE ");
            stb.append(        "AND T1.GRADE||T1.HR_CLASS = '" + param._gradeHrclass + "' ");
            stb.append(        "AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            //                      在籍チェック:転学(2)・退学(3)者は除外 但し異動日が学期終了日または異動基準日より小さい場合
            //                                   転入(4)・編入(5)者は除外 但し異動日が学期終了日または異動基準日より大きい場合 
            stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
            stb.append(                       "WHERE   S1.SCHREGNO = T1.SCHREGNO ");
            stb.append(                           "AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END) ");
            stb.append(                             "OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END)) ) ");
            //                      異動者チェック：留学(1)・休学(2)者は除外 但し学期終了日または基準日が異動開始日と終了日内にある場合
            stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
            stb.append(                       "WHERE   S1.SCHREGNO = T1.SCHREGNO ");
            stb.append(                           "AND S1.TRANSFERCD IN ('1','2') AND CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
            stb.append(    ") ");
            
            //メイン表
            stb.append("SELECT  T1.SCHREGNO, T1.ATTENDNO, T2.HR_NAME, ");
            stb.append(        "T5.NAME, T5.REAL_NAME, CASE WHEN T6.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS USE_REAL_NAME, ");
            stb.append(        "T3.COURSENAME, T4.MAJORNAME, ");
            stb.append(        "T1.GRADE, T1.COURSECD, T1.MAJORCD, T1.COURSECODE, ");
            stb.append(        "VALUE(T7.STAFFNAME, VALUE(T8.STAFFNAME, T9.STAFFNAME)) AS STAFFNAME, ");
            stb.append(        "CASE WHEN T10.NAMECD1 IS NOT NULL THEN 1 ELSE 0 END AS PRINT_LETTER_GRADE, ");
            stb.append(        "EGHIST.CURRICULUM_YEAR ");
            stb.append("FROM    SCHNO_A T1 ");
            stb.append(        "INNER JOIN SCHREG_BASE_MST T5 ON T1.SCHREGNO = T5.SCHREGNO ");
            stb.append(        "INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = '" + param._year + "' AND ");
            stb.append(                                          "T2.SEMESTER = T1.SEMESTER AND ");
            stb.append(                                          "T2.GRADE || T2.HR_CLASS = '" + param._gradeHrclass + "' ");
            stb.append(        "LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND ");
            stb.append(                                          "GDAT.GRADE = T1.GRADE ");
            stb.append(        "LEFT JOIN SCHREG_ENT_GRD_HIST_DAT EGHIST ON EGHIST.SCHREGNO = T1.SCHREGNO AND ");
            stb.append(                                          "EGHIST.SCHOOL_KIND = GDAT.SCHOOL_KIND ");
            stb.append(        "LEFT JOIN COURSE_MST T3 ON T3.COURSECD = T1.COURSECD ");
            stb.append(        "LEFT JOIN MAJOR_MST T4 ON T4.COURSECD = T1.COURSECD AND T4.MAJORCD = T1.MAJORCD ");
            stb.append(        "LEFT JOIN SCHREG_NAME_SETUP_DAT T6 ON T6.SCHREGNO = T1.SCHREGNO AND T6.DIV = '03' ");
            stb.append(        "LEFT JOIN STAFF_MST T7 ON T7.STAFFCD = T2.TR_CD1 ");
            stb.append(        "LEFT JOIN STAFF_MST T8 ON T8.STAFFCD = T2.TR_CD2 ");
            stb.append(        "LEFT JOIN STAFF_MST T9 ON T9.STAFFCD = T2.TR_CD3 ");
            stb.append(        "LEFT JOIN NAME_MST T10 ON T10.NAMECD1 = 'D033' ");
            stb.append(             "AND T10.NAME1 = T1.COURSECD ");
            stb.append(             "AND T10.NAME2 = T1.MAJORCD ");
            stb.append(             "AND T10.NAME3 = T1.COURSECODE ");
            stb.append("ORDER BY ATTENDNO");
            return stb.toString();
        }
    }
    
    private static class Credits {
        
        final List _subclassesDebug;
        final Map<YearGrade, Integer> _ygCredits;
        
        Credits() {
            _subclassesDebug = new ArrayList();
            _ygCredits = new HashMap();
        }

        private void addSubclass(final RecordSubclass recSubclass, final List printGrade) {
            if (null != recSubclass) {
                _subclassesDebug.add(recSubclass._subclass);
                createYearGradeCreditsMap(_ygCredits, recSubclass, printGrade);
            }
        }
        
        /**
         * YearGradeと単位(Integer)のマップをセットする
         */
        private static void createYearGradeCreditsMap(final Map rtn, final RecordSubclass recSubclass, final List printGrade) {
            for (final Iterator itg = printGrade.iterator(); itg.hasNext();) {
                final YearGrade yg = (YearGrade) itg.next();
                
                final Semester semester9 = (Semester) yg._semesters.get(SEMES9);
                if (null != semester9) {
                    final RecordScore recScore = (RecordScore) recSubclass._scores.get(semester9);
                    if (null != recScore) {
                        if (NumberUtils.isDigits(recScore._getCredit) || NumberUtils.isDigits(recScore._addCredit)) {
                            final int getCredit = (NumberUtils.isDigits(recScore._getCredit) ? Integer.parseInt(recScore._getCredit) : 0);
                            final int addCredit = (NumberUtils.isDigits(recScore._addCredit) ? Integer.parseInt(recScore._addCredit) : 0);
                            final int credits = getCredit + addCredit;
                            rtn.put(yg, new Integer(credits));
                        }
                    }
                }
            }
        }
        
        /**
         * 年度ごとに単位を加算する
         */
        private void add(final Credits credits) {
            _subclassesDebug.addAll(credits._subclassesDebug);
            
            final Set allYearGrades = new HashSet();
            allYearGrades.addAll(_ygCredits.keySet());
            allYearGrades.addAll(credits._ygCredits.keySet());
            
            final Map newYgCredits = new HashMap();
            for (final Iterator it = allYearGrades.iterator(); it.hasNext();) {
                final YearGrade yg = (YearGrade) it.next();
                final Integer credit1 = (Integer) _ygCredits.get(yg);
                final Integer credit2 = (Integer) credits._ygCredits.get(yg);
                final Integer sum = new Integer(((null == credit1) ? 0 : credit1.intValue()) + ((null == credit2) ? 0 : credit2.intValue()));
                newYgCredits.put(yg, sum);
            }
            
            _ygCredits.clear();
            _ygCredits.putAll(newYgCredits);
        }
    }
    
    private static class Clazz implements Comparable<Clazz> {
        final String _clazzcd;
        final String _clazzname;
        final int _showorderclass;
        Clazz(final String clazzcd, final String clazzname, final int showorderclass) {
            _clazzcd = clazzcd;
            _clazzname = clazzname;
            _showorderclass = showorderclass;
        }
        public int compareTo(final Clazz clazz) {
            int ret = _showorderclass - clazz._showorderclass;
            if (0 == ret) {
                ret = _clazzcd.compareTo(clazz._clazzcd);
            }
            return ret;
        }
        public int hashCode() {
            return _clazzcd.hashCode() * _clazzname.hashCode();
        }
        public String toString() {
        	return "Clazz(" + _clazzcd + ", " + _clazzname + ")";
        }
    }
    
    private static class Subclass implements Comparable<Subclass> {
        final Clazz _clazz;
        final String _subclasscd;
        final String _subclassname;
        final int _showordersubclass;
        Subclass(final Clazz clazz, final String subclasscd, final String subclassname, final int showordersubclass) {
            _clazz = clazz;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _showordersubclass = showordersubclass;
        }
        public int hashCode() {
            return ("SUBCLASS" + _subclasscd).hashCode();
        }
        public int compareTo(final Subclass other) {
            int ret = _clazz.compareTo(other._clazz);
            if (0 == ret) {
                ret = _showordersubclass - other._showordersubclass;
            }
            if (0 == ret) {
                ret = _subclasscd.compareTo(other._subclasscd);
            }
            return ret;
        }
        public String toString() {
            return "(" + _subclasscd + ":" + _subclassname + ")";
        }
    }
    
    private static class RecordSubclass implements Comparable<RecordSubclass> {
        final Subclass _subclass;
        final Map<Semester, RecordScore> _scores;
        final Map<Semester, SubclassAttendance> _attendances;
        RecordSubclass(final Subclass subclass) {
            _subclass = subclass;
            _scores = new HashMap();
            _attendances = new HashMap();
        }
        public static Map<Subclass, RecordSubclass> load(final DB2UDB db2, final Param param, final Student student, final List printGrade) {
            final Map<Subclass, RecordSubclass> recordSubclassMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param, student._schregno, printGrade);
                if (param._isOutputDebug) {
                	log.info(" record score sql = " + sql);
                }
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String year = rs.getString("YEAR");
                    final String semes = rs.getString("SEMESTER");
                    
                    final Semester semester = student.getSemester(year, semes);
                    
                    final String subclasscd = getSubclasscd(rs, param);
                    final Subclass subclass = param.getSubclass(subclasscd);
                    if (null == subclass) {
                        if (param._isOutputDebug) {
                        	log.warn(" 科目無し : " + subclasscd);
                        }
                        continue;
                    }
                    if (param._isOutputDebug) {
                    	log.info(" subclass = " + subclass);
                    }
                    final String score = rs.getString("SCORE");
                    final String avg = rs.getString("AVG");
                    final String hrRank0 = rs.getString("CLASS_RANK");
                    final String hrAvgRank = rs.getString("CLASS_AVG_RANK");
                    final String hrRank = "1".equals(param._outputKijun) ? hrRank0 : hrAvgRank;
                    final String hrCount = rs.getString("CLASS_COUNT");
                    final String vScore = rs.getString("V_SCORE");
                    final String vAvg = rs.getString("V_AVG");
                    final String vHrRank0 = rs.getString("V_CLASS_RANK");
                    final String vHrAvgRank = rs.getString("V_CLASS_AVG_RANK");
                    final String vHrRank =  "1".equals(param._outputKijun) ? vHrRank0 : vHrAvgRank;
                    final String vHrCount = rs.getString("V_CLASS_COUNT");

                    final String getCredit = rs.getString("GET_CREDIT");
                    final String addCredit = rs.getString("ADD_CREDIT");
                    final String assessMark = rs.getString("ASSESSMARK");
                    
                    final RecordSubclass recSubclass = createRecordSubclass(param, "record", recordSubclassMap, subclass);
                    
                    final RecordScore recordScore = new RecordScore(semester,
                            score, avg, hrRank, hrCount,
                            vScore, vAvg, vHrRank, vHrCount,
                            getCredit, addCredit, assessMark);
                    
                    recSubclass._scores.put(semester, recordScore);
                }
                
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return recordSubclassMap;
        }
        
        public RecordScore getRecordScore(final Semester semester) {
            return _scores.get(semester);
        }
        
        public SubclassAttendance getAttendance(final Semester semester) {
            return _attendances.get(semester);
        }
        
        public static String sql(final Param param, final String schregno, final List<YearGrade> printGrade) {
            
            final StringBuffer stbys = new StringBuffer();
            String union = "";
            for (final YearGrade yg : printGrade) {
                
                for (final Semester semester : yg._semesters.values()) {
                    stbys.append(union).append(" VALUES('" ).append(schregno).append("', '").append(semester._year).append("', '").append(semester._semes).append("') ");
                    union = " UNION ";
                }
            }
            
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH YEAR_SEMESTER (SCHREGNO, YEAR, SEMESTER) AS ( ");
            stb.append(stbys);
            stb.append(" ), SUBCLASSES AS ( ");
            stb.append(" SELECT DISTINCT  ");
            stb.append("     T1.SCHREGNO, T1.YEAR, T1.SEMESTER, T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("  ,T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD ");
            }
            stb.append(" FROM  ");
            stb.append("     RECORD_RANK_DAT T1 ");
            stb.append("     INNER JOIN YEAR_SEMESTER T4 ON T4.SCHREGNO = T1.SCHREGNO ");
            stb.append("         AND T4.YEAR = T1.YEAR ");
            stb.append("         AND T4.SEMESTER = T1.SEMESTER ");
            stb.append(" WHERE ");
            stb.append("      T1.TESTKINDCD = '99' AND T1.TESTITEMCD = '00' ");
            stb.append("   OR T1.TESTKINDCD = '99' AND T1.TESTITEMCD = '01' ");
            stb.append(" UNION ");
            stb.append(" SELECT DISTINCT  ");
            stb.append("     T1.SCHREGNO, T1.YEAR, T1.SEMESTER, T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("  ,T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD ");
            }
            stb.append(" FROM  ");
            stb.append("     RECORD_RANK_V_DAT T1 ");
            stb.append("     INNER JOIN YEAR_SEMESTER T4 ON T4.SCHREGNO = T1.SCHREGNO ");
            stb.append("         AND T4.YEAR = T1.YEAR ");
            stb.append("         AND T4.SEMESTER = T1.SEMESTER ");
            stb.append(" WHERE ");
            stb.append("      T1.TESTKINDCD = '99' AND T1.TESTITEMCD = '00' ");
            stb.append("   OR T1.TESTKINDCD = '99' AND T1.TESTITEMCD = '01' ");
            stb.append(" UNION ");
            stb.append(" SELECT DISTINCT  ");
            stb.append("     T1.SCHREGNO, T1.YEAR, T1.SEMESTER, T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("  ,T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD ");
            }
            stb.append(" FROM ");
            stb.append("     RECORD_SCORE_DAT T1 ");
            stb.append("     INNER JOIN YEAR_SEMESTER T4 ON T4.SCHREGNO = T1.SCHREGNO ");
            stb.append("         AND T4.YEAR = T1.YEAR ");
            stb.append("         AND T4.SEMESTER = T1.SEMESTER ");
            stb.append(" WHERE ");
            stb.append("      T1.TESTKINDCD = '99' AND T1.TESTITEMCD = '00' ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     AND T1.CLASSCD = '90' ");
            } else {
                stb.append("     AND SUBSTR(T1.SUBCLASSCD, 1, 2) = '90' ");
            }
            stb.append(" UNION ");
            stb.append(" SELECT DISTINCT  ");
            stb.append("     T1.SCHREGNO, T1.YEAR, T1.SEMESTER, T2.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("  ,T2.CLASSCD, T2.SCHOOL_KIND, T2.CURRICULUM_CD ");
            }
            stb.append(" FROM ");
            stb.append("     CHAIR_STD_DAT T1 ");
            stb.append("     INNER JOIN CHAIR_DAT T2 ON T2.CHAIRCD = T1.CHAIRCD ");
            stb.append("         AND T2.YEAR = T1.YEAR ");
            stb.append("         AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("     INNER JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD = T2.SUBCLASSCD ");
            stb.append("     INNER JOIN YEAR_SEMESTER T4 ON T4.YEAR = T1.YEAR ");
            stb.append("         AND T4.SEMESTER = T1.SEMESTER ");
            stb.append("         AND T4.SCHREGNO = T1.SCHREGNO ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T0.YEAR, ");
            stb.append("     T0.SEMESTER, ");
            stb.append("     T0.SUBCLASSCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("  T0.CLASSCD, ");
                stb.append("  T0.SCHOOL_KIND, ");
                stb.append("  T0.CURRICULUM_CD, ");
            }
            stb.append("     T1.SCORE, ");
            stb.append("     T1.AVG, ");
            stb.append("     T1.CLASS_RANK AS CLASS_RANK, ");
            stb.append("     T1.CLASS_AVG_RANK AS CLASS_AVG_RANK, ");
            stb.append("     T4.COUNT AS CLASS_COUNT, ");
            stb.append("     T2.SCORE AS V_SCORE, ");
            stb.append("     T2.AVG AS V_AVG, ");
            stb.append("     T2.CLASS_RANK AS V_CLASS_RANK, ");
            stb.append("     T2.CLASS_AVG_RANK AS V_CLASS_AVG_RANK, ");
            stb.append("     T5.COUNT AS V_CLASS_COUNT, ");
            stb.append("     T6.GET_CREDIT, ");
            stb.append("     T6.ADD_CREDIT, ");
            stb.append("     T8.ASSESSMARK ");
            stb.append(" FROM ");
            stb.append("     SUBCLASSES T0 ");
            stb.append("     LEFT JOIN RECORD_RANK_DAT T1 ON T1.SCHREGNO = T0.SCHREGNO ");
            stb.append("         AND T1.YEAR = T0.YEAR ");
            stb.append("         AND T1.SEMESTER = T0.SEMESTER ");
            stb.append("         AND (T0.SEMESTER = '9' ");
            stb.append("          AND T1.TESTKINDCD = '99' AND T1.TESTITEMCD = '01' ");
            stb.append("          OR  T0.SEMESTER <> '9' ");
            stb.append("          AND T1.TESTKINDCD = '99' AND T1.TESTITEMCD = '00' ");
            stb.append("             ) ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("  AND T1.CLASSCD = T0.CLASSCD ");
                stb.append("  AND T1.SCHOOL_KIND = T0.SCHOOL_KIND ");
                stb.append("  AND T1.CURRICULUM_CD = T0.CURRICULUM_CD ");
            }
            stb.append("         AND T1.SUBCLASSCD = T0.SUBCLASSCD ");
            stb.append("     LEFT JOIN RECORD_RANK_V_DAT T2 ON T2.SCHREGNO = T0.SCHREGNO ");
            stb.append("         AND T2.YEAR = T0.YEAR ");
            stb.append("         AND T2.SEMESTER = T0.SEMESTER ");
            stb.append("         AND (T0.SEMESTER = '9' ");
            stb.append("          AND T2.TESTKINDCD = '99' AND T2.TESTITEMCD = '01' ");
            stb.append("          OR  T0.SEMESTER <> '9' ");
            stb.append("          AND T2.TESTKINDCD = '99' AND T2.TESTITEMCD = '00' ");
            stb.append("             ) ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("  AND T2.CLASSCD = T0.CLASSCD ");
                stb.append("  AND T2.SCHOOL_KIND = T0.SCHOOL_KIND ");
                stb.append("  AND T2.CURRICULUM_CD = T0.CURRICULUM_CD ");
            }
            stb.append("         AND T2.SUBCLASSCD = T0.SUBCLASSCD ");
            stb.append("     LEFT JOIN SCHREG_REGD_DAT T3 ON T3.SCHREGNO = T0.SCHREGNO ");
            stb.append("         AND T3.YEAR = T0.YEAR ");
            stb.append("         AND T3.SEMESTER = (CASE WHEN T0.SEMESTER = '9' THEN '3' ELSE T0.SEMESTER END) ");
            stb.append("     LEFT JOIN RECORD_AVERAGE_DAT T4 ON T4.YEAR = T0.YEAR ");
            stb.append("         AND T4.SEMESTER = T0.SEMESTER ");
            stb.append("         AND T4.TESTKINDCD = T1.TESTKINDCD ");
            stb.append("         AND T4.TESTITEMCD = T1.TESTITEMCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("  AND T4.CLASSCD = T0.CLASSCD ");
                stb.append("  AND T4.SCHOOL_KIND = T0.SCHOOL_KIND ");
                stb.append("  AND T4.CURRICULUM_CD = T0.CURRICULUM_CD ");
            }
            stb.append("         AND T4.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND T4.AVG_DIV = '2' ");
            stb.append("         AND T4.GRADE = T3.GRADE ");
            stb.append("         AND T4.HR_CLASS = T3.HR_CLASS ");
            stb.append("         AND T4.COURSECD || T4.MAJORCD || T4.COURSECODE = '00000000' ");
            stb.append("     LEFT JOIN RECORD_AVERAGE_V_DAT T5 ON T5.YEAR = T0.YEAR ");
            stb.append("         AND T5.SEMESTER = T0.SEMESTER ");
            stb.append("         AND T5.TESTKINDCD = T2.TESTKINDCD ");
            stb.append("         AND T5.TESTITEMCD = T2.TESTITEMCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("  AND T5.CLASSCD = T0.CLASSCD ");
                stb.append("  AND T5.SCHOOL_KIND = T0.SCHOOL_KIND ");
                stb.append("  AND T5.CURRICULUM_CD = T0.CURRICULUM_CD ");
            }
            stb.append("         AND T5.SUBCLASSCD = T2.SUBCLASSCD ");
            stb.append("         AND T5.AVG_DIV = '2' ");
            stb.append("         AND T5.GRADE = T3.GRADE ");
            stb.append("         AND T5.HR_CLASS = T3.HR_CLASS ");
            stb.append("         AND T5.COURSECD || T5.MAJORCD || T5.COURSECODE = '00000000' ");
            stb.append("     LEFT JOIN RECORD_SCORE_DAT T6 ON T6.SCHREGNO = T0.SCHREGNO ");
            stb.append("         AND T6.YEAR = T0.YEAR ");
            stb.append("         AND T6.SEMESTER = T0.SEMESTER ");
            stb.append("         AND T6.TESTKINDCD = '99' AND T6.TESTITEMCD = '00' ");
            stb.append("         AND T6.SCORE_DIV = '00' ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("  AND T6.CLASSCD = T0.CLASSCD ");
                stb.append("  AND T6.SCHOOL_KIND = T0.SCHOOL_KIND ");
                stb.append("  AND T6.CURRICULUM_CD = T0.CURRICULUM_CD ");
            }
            stb.append("         AND T6.SUBCLASSCD = T0.SUBCLASSCD ");
            stb.append("     LEFT JOIN ASSESS_COURSE_MST T8 ON T8.ASSESSCD = '5' ");
            stb.append("         AND T8.COURSECD = T3.COURSECD ");
            stb.append("         AND T8.MAJORCD = T3.MAJORCD ");
            stb.append("         AND T8.COURSECODE = T3.COURSECODE ");
            stb.append("         AND T1.SCORE BETWEEN T8.ASSESSLOW AND T8.ASSESSHIGH ");
            stb.append(" ORDER BY ");
            stb.append("     T0.YEAR, ");
            stb.append("     T0.SEMESTER, ");
            stb.append("     T0.SUBCLASSCD ");
            return stb.toString();
        }
        
        public static RecordSubclass createRecordSubclass(final Param param, final String comment, final Map<Subclass, RecordSubclass> recordSubclassMap, final Subclass subclass) {
            if (!recordSubclassMap.containsKey(subclass)) {
                if (param._isOutputDebug) {
                	log.info(" add " + comment + " subclass " + subclass);
                }
                recordSubclassMap.put(subclass, new RecordSubclass(subclass));
            }
            return recordSubclassMap.get(subclass);
        }
        public SubclassAttendance createSubclassAttendance(final Semester semester) {
            if (!_attendances.containsKey(semester)) {
                _attendances.put(semester, new SubclassAttendance(this._subclass, semester));
            }
            return _attendances.get(semester);
        }
        public int hashCode() {
            return "RecordSubclass".hashCode() + _subclass.hashCode();
        }
        public int compareTo(final RecordSubclass other) {
            return _subclass.compareTo(other._subclass);
        }
        public String toString() {
            return "RecordSubclass" + _subclass.toString();
        }
    }
    
    private static class RecordScore extends SemesterData {
        final Semester _semester;
        final String _score;    // RECORD_RANK_DAT.SCORE
        final String _avg;      // RECORD_RANK_DAT.AVG
        final String _hrRank;   // RECORD_RANK_DAT.CLASS_RANK
        final String _hrCount;  // RECORD_RANK_AVERAGE_DAT.COUNT
        final String _vScore;   // RECORD_RANK_V_DAT.SCORE
        final String _vAvg;     // RECORD_RANK_V_DAT.AVG
        final String _vHrRank;  // RECORD_RANK_V_DAT.CLASS_RANK
        final String _vHrCount; // RECORD_RANK_AVERAGE_V_DAT.COUNT
        final String _getCredit;
        final String _addCredit;
        final String _letterGradeMark;

        RecordScore(final Semester semester,
                final String score,
                final String avg,
                final String hrRank,
                final String hrCount,
                final String vScore,
                final String vAvg,
                final String vHrRank,
                final String vHrCount,
                final String getCredit,
                final String addCredit,
                final String letterGradeMark) {
            _semester = semester;
            _score =score;
            _avg = avg;
            _hrRank = hrRank;
            _hrCount = hrCount;
            _vScore = vScore;
            _vAvg = vAvg;
            _vHrRank = vHrRank;
            _vHrCount = vHrCount;
            _getCredit = getCredit;
            _addCredit = addCredit;
            _letterGradeMark = letterGradeMark;
        }
        public Semester getSemester() {
            return _semester;
        }
        public String toString() {
            return "RecordScore(" + _semester + ":" + _score + ":" + _getCredit + " + " + _addCredit + ")";
        }
    }
    
    /**
     * 出欠
     */
    private static class Attendance extends SemesterData {
        final Semester _semester;
        int _lesson;
        int _kesseki;
        int _reihaiKekka;
        int _kekkaJisu;
        int _reihaiTikoku;
        int _jugyouTikoku;
        int _early;
        int _suspendMourning;
        public Attendance(
                final Semester semester
        ) {
            _semester = semester;
        }
        public Semester getSemester() {
            return _semester;
        }
        public void add(final Attendance att) {
            _lesson += att._lesson;
            _kesseki += att._kesseki;
            _reihaiKekka += att._reihaiKekka;
            _kekkaJisu += att._kekkaJisu;
            _reihaiTikoku += att._reihaiTikoku;
            _jugyouTikoku += att._jugyouTikoku;
            _early += att._early;
            _suspendMourning += att._suspendMourning;
        }
        public static Map<Semester, Attendance> load(final DB2UDB db2, final Param param, final Student student) {
            Map<Semester, Attendance> attendances = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final Map hasuuMap = AttendAccumulate.getHasuuMap(db2, param._year, param._year + "-04-01", param._date);
                final String attendSemesInState = (String) hasuuMap.get("attendSemesInState");

                final KNJSchoolMst knjSchoolmst = param._knjSchoolMst;
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     YEAR, ");
                stb.append("     SEMESTER, ");
                stb.append("     SUM(LESSON - ABROAD ");
                if (!"1".equals(knjSchoolmst._semOffDays)) {
                    stb.append("     - OFFDAYS ");
                }
                stb.append("     ) AS LESSON, ");
                stb.append("     SUM(ABSENT) AS ABSENT, ");
                stb.append("     SUM(SUSPEND) AS SUSPEND, ");
                if ("true".equals(param._useVirus)) {
                    stb.append("     SUM(VIRUS) AS VIRUS, ");
                } else {
                    stb.append("     0 AS VIRUS, ");
                }
                if ("true".equals(param._useKoudome)) {
                    stb.append("     SUM(KOUDOME) AS KOUDOME, ");
                } else {
                    stb.append("     0 AS KOUDOME, ");
                }
                stb.append("     SUM(MOURNING) AS MOURNING, ");
                stb.append("     SUM(SICK + NOTICE + NONOTICE) AS SICK, ");
                stb.append("     SUM(EARLY) AS EARLY, ");
                stb.append("     SUM(VALUE(REIHAI_KEKKA, 0)) AS REIHAI_KEKKA, ");
                stb.append("     SUM(VALUE(KEKKA_JISU, 0)) AS KEKKA_JISU, ");
                stb.append("     SUM(VALUE(REIHAI_TIKOKU, 0)) AS REIHAI_TIKOKU, ");
                stb.append("     SUM(VALUE(JYUGYOU_TIKOKU, 0)) AS JYUGYOU_TIKOKU ");
                stb.append(" FROM ");
                stb.append("     V_ATTEND_SEMES_DAT T1 ");
                stb.append(" WHERE ");
                stb.append("     T1.SCHREGNO = '" + student._schregno + "' ");
                stb.append("     AND (T1.YEAR < '" + param._year + "' OR ");
                stb.append("            T1.YEAR = '" + param._year + "' AND T1.SEMESTER || T1.MONTH IN " + attendSemesInState + ") ");
                stb.append(" GROUP BY ");
                stb.append("     T1.YEAR, T1.SEMESTER ");
                
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    
                    final Semester semester = student.getSemester(rs.getString("YEAR"), rs.getString("SEMESTER"));
                    if (null == semester) {
                        continue;
                    }
                    final Attendance att = createAttendance(attendances, semester);
                    att._lesson += rs.getInt("LESSON");
                    att._suspendMourning += rs.getInt("SUSPEND") + rs.getInt("MOURNING") + ("true".equals(param._useVirus) ? rs.getInt("VIRUS") : 0) + ("true".equals(param._useKoudome) ? rs.getInt("KOUDOME") : 0);
                    att._kesseki += rs.getInt("SICK");
                    att._early += rs.getInt("EARLY");
                    att._reihaiKekka += rs.getInt("REIHAI_KEKKA");
                    att._kekkaJisu += rs.getInt("KEKKA_JISU");
                    att._reihaiTikoku += rs.getInt("REIHAI_TIKOKU");
                    att._jugyouTikoku += rs.getInt("JYUGYOU_TIKOKU");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return attendances;
        }
        
        private static Attendance createAttendance(final Map<Semester, Attendance> semesterAttendanceMap, final Semester semester) {
            if (!semesterAttendanceMap.containsKey(semester)) {
                semesterAttendanceMap.put(semester, new Attendance(semester));
            }
            return semesterAttendanceMap.get(semester);
        }
    }
    
    /**
     * 出欠
     */
    private static class SubclassAttendance extends SemesterData {
        final Subclass _subclass;
        final Semester _semester;
        int _kekka;
        public SubclassAttendance(
                final Subclass subclass,
                final Semester semester
        ) {
            _subclass = subclass;
            _semester = semester;
        }
        public Semester getSemester() {
            return _semester;
        }
        
        /**
         * 名称マスタ NAMECD1='Z010' NAMECD2='00'読込
         */
        private static String setZ010Name1(DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' "));
        }

        public static void setSubclassAttendance(final DB2UDB db2, final Param param, final String schregno, final Map<Subclass, RecordSubclass> recordSubclasses, final List<YearGrade> printGrade) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String SSEMESTER = "1";
                
                for (final YearGrade yg : printGrade) {
                    
                    for (final Semester semester : yg._semesters.values()) {
                        
                        final String ssemester = SEMES9.equals(semester._semes) ? SSEMESTER : semester._semes;

                        if (param._isOutputDebug) {
                        	log.info(" subatt semes = " + semester._semes + " , " + semester._sdate + " : " + semester._edate);
                        }
                        
                        param._attendParamMap.put("schregno", schregno);
                        param._attendParamMap.put("sSemester", ssemester);
                        final String sqlAttendSubclass = AttendAccumulate.getAttendSubclassSql(
                                yg._year,
                                semester._semes,
                                semester._sdate,
                                semester._edate,
                                param._attendParamMap
                                );
                        
                        try {
                            ps = db2.prepareStatement(sqlAttendSubclass);
                            rs = ps.executeQuery();
                            while (rs.next()) {
                                
                                if (!SEMES9.equals(rs.getString("SEMESTER"))) {
                                    continue;
                                }
                                
                                final String subclasscd = rs.getString("SUBCLASSCD");
                                
                                final int sick = rs.getInt("SICK2");
                                
                                final Subclass subclass = param.getSubclass(subclasscd);
                                if (null == subclass) {
                                    log.warn(" 科目無し : " + subclasscd);
                                    continue;
                                }
                                
                                final RecordSubclass recSubclass = RecordSubclass.createRecordSubclass(param, "subatt", recordSubclasses, subclass);
                                
                                final SubclassAttendance sa = recSubclass.createSubclassAttendance(semester);
                                
                                sa._kekka += sick;
                            }
                            
                        } catch (Exception e) {
                            log.error("exception!", e);
                        } finally {
                            DbUtils.closeQuietly(null, ps, rs);
                            db2.commit();
                        }
                    }
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
    }
    
    private static class HreportremarkDat {
        final String _year;
        final String _semester;
        final String _totalstudytime;
        final String _communication;
        final String _attendrecRemark;
        public HreportremarkDat(
                final String year,
                final String semester,
                final String totalstudytime,
                final String communication,
                final String attendrecRemark) {
            _year = year;
            _semester = semester;
            _totalstudytime = totalstudytime;
            _communication = communication;
            _attendrecRemark = attendrecRemark;
        }
        public static List load(final DB2UDB db2, final Param param, final String schregno) {
            List hreportremarkDatList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param, schregno);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String totalstudytime = rs.getString("TOTALSTUDYTIME");
                    final String communication = rs.getString("COMMUNICATION");
                    final String attendrecRemark = rs.getString("ATTENDREC_REMARK");
                    final String year = rs.getString("YEAR");
                    final String semester = rs.getString("SEMESTER");
                    
                    
                    final HreportremarkDat hd = new HreportremarkDat(year, semester, totalstudytime, communication, attendrecRemark);
                    hreportremarkDatList.add(hd);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return hreportremarkDatList;
        }
        
        public static String sql(final Param param, final String schregno) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.TOTALSTUDYTIME, ");
            stb.append("     T1.COMMUNICATION, ");
            stb.append("     T1.ATTENDREC_REMARK ");
            stb.append(" FROM ");
            stb.append("     HREPORTREMARK_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     SCHREGNO = '" + schregno + "' ");
            stb.append("     AND (YEAR < '" + param._year + "' OR ");
            stb.append("       YEAR = '" + param._year + "' AND");
            stb.append("         (SEMESTER = '" + SEMES9 + "' OR SEMESTER <= '" + param._semester + "')) ");
            stb.append(" ORDER BY ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SEMESTER ");
            return stb.toString();
        }
    }
    
    private static class HreportremarkDetailDat {
        final String _year;
        final String _semester;
        final String _div;
        final String _code;
        final String _remark1;
        public HreportremarkDetailDat(
                final String year,
                final String semester,
                final String div,
                final String code,
                final String remark1) {
            _year = year;
            _semester = semester;
            _div = div;
            _code = code;
            _remark1 = remark1;
        }
        public static List load(final DB2UDB db2, final Param param, final String schregno) {
            List hreportremarkDetailDatList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param, schregno);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String year = rs.getString("YEAR");
                    final String semester = rs.getString("SEMESTER");
                    final String div = rs.getString("DIV");
                    final String code = rs.getString("CODE");
                    final String remark1 = rs.getString("REMARK1");
                    
                    final HreportremarkDetailDat hd = new HreportremarkDetailDat(year, semester, div, code, remark1);
                    hreportremarkDetailDatList.add(hd);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return hreportremarkDetailDatList;
        }
        
        public static String sql(final Param param, final String schregno) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.DIV, ");
            stb.append("     T1.CODE, ");
            stb.append("     T1.REMARK1 ");
            stb.append(" FROM ");
            stb.append("     HREPORTREMARK_DETAIL_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     SCHREGNO = '" + schregno + "' ");
            stb.append("     AND (YEAR < '" + param._year + "' OR ");
            stb.append("       YEAR = '" + param._year + "' AND");
            stb.append("         (SEMESTER = '" + SEMES9 + "' OR SEMESTER <= '" + param._semester + "')) ");
            stb.append(" ORDER BY ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SEMESTER ");
            return stb.toString();
        }
    }

    private static class YearGrade implements Comparable<YearGrade> {
        final String _year;
        final String _gradeCd;
        final boolean _isRyunen;
        final boolean _isRyunenNext;
        final Map<?, Semester> _semesters;
        YearGrade(final String year, final String gradeCd, final boolean isRyunen, final boolean isRyunenNext, final Map semesters) {
            _year = year;
            _gradeCd = gradeCd;
            _isRyunen = isRyunen;
            _isRyunenNext = isRyunenNext;
            _semesters = semesters;
        }
        public int compareTo(final YearGrade other) {
            return _year.compareTo(other._year);
        }
        public boolean isPrintLetterGrade() {
            final int y = Integer.parseInt(_year);
            final int g = Integer.parseInt(_gradeCd);
            if (g == 1 && y < 2012) {
                return true;
            }
            if (g == 2 && y < 2013) {
                return true;
            }
            if (g == 3 && y < 2014) {
                return true;
            }
            return false;
        }
        public String toString() {
            return "YearGrade(" + _year + ", " + _gradeCd + ", ryunen? " + _isRyunen + ", ryunenNext?" + _isRyunenNext + ")";
        }
    }
    
    private static abstract class SemesterData implements Comparable<SemesterData> {
        public abstract Semester getSemester();
        public int compareTo(SemesterData o) {
            return getSemester().compareTo(o.getSemester());
        }
    }
    
    private static class Semester implements Comparable<Semester> {
        final String _year;
        final String _semes;
        final String _sdate;
        final String _edate;
        Semester(final String year, final String semes, final String sdate, final String edate) {
            _year = year;
            _semes = semes;
            _sdate = sdate;
            _edate = edate;
        }
        public int hashCode() {
            return _year.hashCode() + _semes.hashCode();
        }
        public int compareTo(final Semester other) {
            int ret = _year.compareTo(other._year);
            if (0 == ret) {
                _semes.compareTo(other._semes);
            }
            return ret;
        }
        public String toString() {
            return "Semester(" + _year + "," + _semes + ")";
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 72933 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        
        static final String SCHOOL_KIND = "H";
        
        final boolean _isOutputDebug;
        final String _year;
        final String _semester;
        final String _ctrlSemester;
        final String _date;
        final String _gradeHrclass;
        final String _grade;
        final String[] _categorySelected;
        final String _gradeCd;
        final String _gradename;
        final String _hrname;
        
        final String _documentroot;
        String _imagepath;
        String _extension;

        final String _outputKijun;
        
        final String _principalname;
        final String _schoolname;
        final String _jobname;
        
        final KNJSchoolMst _knjSchoolMst;
        
        final Map<String, Subclass> _subclassMap;
        
        /** 教育課程コードを使用するか */
        private final String _useCurriculumcd;
        private final String _useVirus;
        private final String _useKoudome;
        final Map _attendParamMap;
        
        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
        	_isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _date = request.getParameter("DATE").replace('/', '-');
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _grade = _gradeHrclass.substring(0, 2);
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            
            _documentroot = request.getParameter("DOCUMENTROOT");
            _outputKijun = request.getParameter("OUTPUT_KIJUN");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
            setImagePath(db2);
            
            KNJSchoolMst knjSchoolMst = null;
            try {
                knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }
            _knjSchoolMst = knjSchoolMst;
            
            _subclassMap = getAllSubclasses(db2);
            
            final Map certifSchoolDat = getCertifSchoolDat(db2, _year);
            _principalname = (String) certifSchoolDat.get("PRINCIPAL_NAME");
            _schoolname = (String) certifSchoolDat.get("SCHOOL_NAME");
            _jobname = (String) certifSchoolDat.get("JOB_NAME");
            
            _gradeCd = getGradeCd(db2, _grade);
            _gradename = "第" + _gradeCd + "学年";
            _hrname = getHrClassName1(db2, _year, SEMES9.equals(_semester) ? _ctrlSemester : _semester, _gradeHrclass) + "組";
            
            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
        }
        
        private void setImagePath(DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT IMAGEPATH, EXTENSION FROM CONTROL_MST ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    _imagepath = rs.getString("IMAGEPATH");
                    _extension = rs.getString("EXTENSION");
                }
            } catch (SQLException ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        
        private Subclass getSubclass(final String subclasscd) {
            return _subclassMap.get(subclasscd);
        }
        
        private Map getAllSubclasses(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map clazzMap = new TreeMap();
            final Map subclassMap = new TreeMap();
            try {
                final String sql = sqlAllSubclass();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    
                    final String clazzcd = rs.getString("CLASSCD");
                    final String subclasscd = getSubclasscd(rs, this);
                    final String clazzname = rs.getString("CLASSNAME");
                    final String subclassname = rs.getString("SUBCLASSNAME");
                    final int showorderclazz = rs.getInt("SHOWORDERCLASS");
                    final int showordersubclass = rs.getInt("SHOWORDERSUBCLASS");
                    
                    if (!clazzMap.containsKey(clazzcd)) {
                        clazzMap.put(clazzcd, new Clazz(clazzcd, clazzname, showorderclazz));
                    }
                    final Clazz clazz = (Clazz) clazzMap.get(clazzcd);
                    
                    final Subclass subclass = new Subclass(clazz, subclasscd, subclassname, showordersubclass);
                    subclassMap.put(subclasscd, subclass);
                }
                
                subclassMap.put(SUBCLASSCD999999, SUBCLASS999999);
                
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return subclassMap;
        }
        
        private String sqlAllSubclass() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("  T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, ");
            } else {
                stb.append("  T2.CLASSCD, ");
            }
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     T2.CLASSNAME, ");
            stb.append("     T1.SUBCLASSNAME,  ");
            stb.append("     VALUE(T2.SHOWORDER3, 9999) AS SHOWORDERCLASS, ");
            stb.append("     VALUE(T1.SHOWORDER3, 9999) AS SHOWORDERSUBCLASS ");
            stb.append(" FROM ");
            stb.append("     SUBCLASS_MST T1 ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("     LEFT JOIN CLASS_MST T2 ON T2.CLASSCD || '-' ||T2.SCHOOL_KIND = ");
                stb.append("                               T1.CLASSCD || '-' ||T1.SCHOOL_KIND ");
            } else {
                stb.append("     LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = SUBSTR(T1.SUBCLASSCD, 1, 2) ");
            }
            stb.append(" ORDER BY ");
            stb.append("     SHOWORDERCLASS, ");
            stb.append("     T2.CLASSCD, ");
            stb.append("     SHOWORDERSUBCLASS, ");
            stb.append("     T1.SUBCLASSCD ");
            return stb.toString();
        }
        
        private Map getCertifSchoolDat(final DB2UDB db2, final String year) {
            final Map certifSchoolDat = KnjDbUtils.firstRow(KnjDbUtils.query(db2, " SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + year + "' AND CERTIF_KINDCD = '104' "));
            return certifSchoolDat;
        }
        
        private String getHrClassName1(final DB2UDB db2, final String year, final String semester, final String gradeHrclass) {
            String rtn = null;
            rtn = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT HR_CLASS_NAME1 FROM SCHREG_REGD_HDAT WHERE YEAR = '" + year + "' AND SEMESTER = '" + semester + "' AND GRADE || HR_CLASS = '" + gradeHrclass + "' "));
            if (null == rtn) {
            	final String hrClass = " SELECT HR_CLASS FROM SCHREG_REGD_DAT WHERE YEAR = '" + year + "' AND SEMESTER = '" + semester + "' AND GRADE || HR_CLASS = '" + gradeHrclass + "' ";
                rtn = NumberUtils.isDigits(hrClass) ? String.valueOf(Integer.parseInt(hrClass)) : hrClass;
            }
            if (null == rtn) {
                rtn = "";
            }
            return rtn;
        }
        
        private String getGradeCd(final DB2UDB db2, final String grade) {
            String gradeCd = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT * FROM SCHREG_REGD_GDAT T1 WHERE T1.SCHOOL_KIND = '" + SCHOOL_KIND + "' AND T1.YEAR = '" + _year + "' AND T1.GRADE = '" + grade + "' ");
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
        
        public String getFilePath() {
            final String path = _documentroot + "/" + (null == _imagepath ? "" : (_imagepath + "/")) + "SCHOOLLOGO." + _extension;
            if (!new java.io.File(path).exists()) {
                return null;
            }
            return path;
        }

		private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD183H' AND NAME = '" + propName + "' "));
        }
    }
}

// eof
