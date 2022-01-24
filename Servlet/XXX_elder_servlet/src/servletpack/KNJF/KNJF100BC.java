/*
 * $Id: b69551f6a828dda2f8dd731743bad39e9e179035 $
 *
 * 作成日: 2015/08/25
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJF;


import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
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

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJF.detail.MedexamDetDat;
import servletpack.KNJF.detail.MedexamToothDat;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_Schoolinfo;

public class KNJF100BC {

    private static final Log log = LogFactory.getLog(KNJF100BC.class);

    private static String PRGID_KNJF100B = "KNJF100B";
    private static String PRGID_KNJF100C = "KNJF100C";
    private static String PRGID_KNJF100D = "KNJF100D";
    private static String PRGID_KNJF100E = "KNJF100E";

    private static final String FROM_TO_MARK = "\uFF5E";

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

    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str)
    {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;                      //byte数を取得
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final Map physAvgMap;
        if (_param._isKumamoto) {
            physAvgMap = new TreeMap();
        } else {
            physAvgMap = HexamPhysicalAvgDat.getHexamPhysicalAvgMap(db2, _param);
        }

        final int max1 = 7;
        final int max2 = 10;

		final List list = Student.getStudentList(db2, _param, Arrays.asList(_param._schregnos));
		int page = 0;
		for (int line = 0; line < list.size(); line++) {
			final Student student = (Student) list.get(line);

			page = printNurseoff(db2, svf, max1, max2, page, student);

			page = printStudent2(db2, svf, page, student, physAvgMap);
		}
    }

    public int printNurseoff(final DB2UDB db2, final Vrw32alp svf, final int max1, final int max2, int page, final Student student) {
        final List nurseoffPageList = getNurseoffPageList(student._nurseoffList, max1, max2);
        for (int pi = 0; pi < nurseoffPageList.size(); pi++) {
            final List nurseoffList = (List) nurseoffPageList.get(pi);

            final String form1Name = !"".equals(_param._useFormNameF100BC_1) ? _param._useFormNameF100BC_1 + ".frm" : "KNJF100BC_1.frm";
            final String form = pi == 0 ? form1Name : "KNJF100BC_2.frm"; // 2ページ以降は10行のフォーム
            final int max = pi == 0 ? max1 : max2;
            svf.VrSetForm(form, 4);
            page += 1;
            svf.VrsOut("PAGE", String.valueOf(page) + "頁"); // 学科名
            svf.VrsOut("DATE1", KNJ_EditDate.h_format_JP(db2, _param._ctrlDate)); // 作成日

            svf.VrsOut("MAJOR_NAME", student._courseName); // 学科名
            svf.VrsOut("HR_NAME", student._hrName); // ＨＲクラス（マスク）
            final int len1 = getMS932ByteLength(student._trCd1Staffname);
            svf.VrsOut("TEACHER_NAME1_" + (len1 > 30 ? "3" : len1 > 20 ? "2" : "1"), student._trCd1Staffname); // 担任名
            final int len2 = getMS932ByteLength(_param._yougoStaffname);
            svf.VrsOut("TEACHER_NAME2_" + (len2 > 30 ? "3" : len2 > 20 ? "2" : "1"), _param._yougoStaffname); // 担任名

            svf.VrsOut("ATTENDNO", NumberUtils.isDigits(student._attendno) ? String.valueOf(Integer.parseInt(student._attendno)) : StringUtils.defaultString(student._attendno)); // 出席番号（マスク）
//              svf.VrsOut("COURSE_NAME", null); // コース名
//              svf.VrsOut("HR_CLASS", null); // 学年・組・番号
            svf.VrsOut("NAME", student._name); // 氏名

            if (pi == 0) {
                svf.VrsOut("MEDICAL_HISTORY1", student._medicalHistory1Name); // 既往症
                svf.VrsOut("MEDICAL_HISTORY2", student._medicalHistory2Name); // 既往症
                svf.VrsOut("MEDICAL_HISTORY3", student._medicalHistory3Name); // 既往症
                svfVrsOut(svf, new String[] {"DIAGNOSIS_NAME", "DIAGNOSIS_NAME2"}, KNJ_EditEdit.get_token(student._diagnosisName, 50, 2)); // 診断名
                svf.VrsOut("GUIDE_DIV", student._guideDivName); // 指導区分

                svfVrsOut(svf, new String[] {"ALLERGY1_1", "ALLERGY1_2"}, KNJ_EditEdit.get_token(student._allergyMedicine, 40, 2)); // 薬アレルギー
                svfVrsOut(svf, new String[] {"ALLERGY2_1", "ALLERGY2_2"}, KNJ_EditEdit.get_token(student._allergyFood, 40, 2)); // 食品アレルギー
                svfVrsOut(svf, new String[] {"ALLERGY3_1", "ALLERGY3_2"}, KNJ_EditEdit.get_token(student._allergyOther, 40, 2)); // その他アレルギー

                svf.VrsOut("PERIOD", "利用期間:" + periodDateString(_param._date1) + "〜" + periodDateString(_param._date2)); // 利用期間
            }

            for (int ni = 0; ni < nurseoffList.size(); ni++) {
                final Nurseoff nurseoff = (Nurseoff) nurseoffList.get(ni);

                final String startTime = timeString(nurseoff._visitHour, nurseoff._visitMinute);
                final String endTime = timeString(nurseoff._leaveHour, nurseoff._leaveMinute);
                final String timeRange = StringUtils.defaultString(startTime, "　　　") + (null == startTime && null == endTime ? "" : FROM_TO_MARK) + StringUtils.defaultString(endTime, "　　　");
                svf.VrsOut("VISIT_DATE", StringUtils.defaultString(KNJ_EditDate.h_format_JP(db2, nurseoff._visitDate)) + "　" + StringUtils.defaultString(timeRange)); // 来室日付
                svf.VrsOut("FIELD1", Nurseoff.typeName(nurseoff._type)); // 来室
                if (getMS932ByteLength(nurseoff._visitReason1Name) > 14) {
                    svf.VrsOut("VISIT_REASON1_1", nurseoff._visitReason1Name); // 来室理由
                } else {
                    svf.VrsOut("VISIT_REASON1", nurseoff._visitReason1Name); // 来室理由
                }
                svf.VrsOut("OCCUR_DATE", StringUtils.defaultString(_param.dateString(nurseoff._occurDate)) + "　" + StringUtils.defaultString(timeString(nurseoff._occurHour, nurseoff._occurMinute))); // 症状発生日
                svf.VrsOut("OCCUR_PLACE", nurseoff._occurPlaceName); // 症状発生場所
                svf.VrsOut("SLEEPTIME", nurseoff._sleeptime); // 睡眠時間
                svf.VrsOut("CONDITION1", Nurseoff.condition1Name(nurseoff._condition1)); // 睡眠状況1
                svf.VrsOut("CONDITION2", Nurseoff.condition2Name(nurseoff._condition2)); // 睡眠状況2
                svf.VrsOut("CONDITION3", Nurseoff.condition4Name(nurseoff._condition4)); // 朝食状況
                svf.VrsOut("TREATMENT", nurseoff._treatment1Name); // 保健室での処置
                svf.VrsOut("RESTTIME", null == nurseoff._resttimeName ? "" :  "休養時間:" + nurseoff._resttimeName); // 休養時間
                svf.VrEndRecord();
            }

            for (int li = nurseoffList.size(); li < max; li++) {
                svf.VrEndRecord();
            }

            _hasData = true;
        }
        return page;
    }

    private String periodDateString(final String date) {
        return StringUtils.replace(date, "-", "/") + "(" + KNJ_EditDate.h_format_W(date) + ")";
    }

    private String timeString(final String hour, final String minute) {
        if (!NumberUtils.isDigits(hour) || !NumberUtils.isDigits(minute)) {
            return null;
        }
        final DecimalFormat df = new DecimalFormat("00");
        return df.format(Integer.parseInt(hour)) + ":" + df.format(Integer.parseInt(minute));
    }

    private void svfVrsOut(final Vrw32alp svf, final String[] fields, final String[] token) {
        if (null != token) {
            for (int i = 0; i < Math.min(fields.length, token.length); i++) {
                svf.VrsOut(fields[i], token[i]);
            }
        }
    }

    private List getNurseoffPageList(final List nurseoffList, final int max1, final int max2) {
        final List pageList = new ArrayList();
        int page = 0;
        List currentPage = null;
        for (final Iterator it = nurseoffList.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == currentPage || page == 1 && currentPage.size() >= max1 || page > 1 && currentPage.size() >= max2) {
                currentPage = new ArrayList();
                pageList.add(currentPage);
                page += 1;
            }
            currentPage.add(o);
        }
        if (null == currentPage) {
            pageList.add(new ArrayList()); // ダミーで1ページ空リスト追加
        }
        return pageList;
    }

    private static class Student {
        final String _grade;
        final String _hrClass;
        final String _attendno;
        final String _schregno;
        final String _name;
        final String _course;
        final String _courseName;
        final String _hrName;
        final String _trCd1Staffname;
        final String _allergyMedicine;
        final String _allergyFood;
        final String _allergyOther;
        final String _medicalHistory1;
        final String _medicalHistory1Name;
        final String _medicalHistory2;
        final String _medicalHistory2Name;
        final String _medicalHistory3;
        final String _medicalHistory3Name;
        final String _diagnosisName;
        final String _guideDiv;
        final String _guideDivName;
        final String _joiningSportsClub;
        final String _joiningSportsClubName;
        final List _nurseoffList;

        String _annual;
        String _sexCd;
        String _sexName;
        String _birthDay;
        String _coursecd;
        String _majorcd;
        String _coursecode;
        String _coursename;
        String _majorname;
        String _coursecodename;
        String _schoolKind;

        MedexamDetDat _medexamDetDat = null;
        MedexamToothDat _medexamToothDat = null;

        Student(
            final String grade,
            final String hrClass,
            final String attendno,
            final String schregno,
            final String name,
            final String course,
            final String courseName,
            final String hrName,
            final String trCd1Staffname,
            final String allergyMedicine,
            final String allergyFood,
            final String allergyOther,
            final String medicalHistory1,
            final String medicalHistory1Name,
            final String medicalHistory2,
            final String medicalHistory2Name,
            final String medicalHistory3,
            final String medicalHistory3Name,
            final String diagnosisName,
            final String guideDiv,
            final String guideDivName,
            final String joiningSportsClub,
            final String joiningSportsClubName
        ) {
            _grade = grade;
            _hrClass = hrClass;
            _attendno = attendno;
            _schregno = schregno;
            _name = name;
            _course = course;
            _courseName = courseName;
            _hrName = hrName;
            _trCd1Staffname = trCd1Staffname;
            _allergyMedicine = allergyMedicine;
            _allergyFood = allergyFood;
            _allergyOther = allergyOther;
            _medicalHistory1 = medicalHistory1;
            _medicalHistory1Name = medicalHistory1Name;
            _medicalHistory2 = medicalHistory2;
            _medicalHistory2Name = medicalHistory2Name;
            _medicalHistory3 = medicalHistory3;
            _medicalHistory3Name = medicalHistory3Name;
            _diagnosisName = diagnosisName;
            _guideDiv = guideDiv;
            _guideDivName = guideDivName;
            _joiningSportsClub = joiningSportsClub;
            _joiningSportsClubName = joiningSportsClubName;
            _nurseoffList = new ArrayList();
        }

        public static List getStudentList(final DB2UDB db2, final Param param, final List schregnoList) {
            final List list = new ArrayList();
            final Map schregMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String sql = sql(param);
            log.debug(" sql = " + sql);
            try {
                ps = db2.prepareStatement(sql);

                for (final Iterator it = schregnoList.iterator(); it.hasNext();) {
                    final String schregno = (String) it.next();

                    ps.setString(1, schregno);
                    rs = ps.executeQuery();

                    while (rs.next()) {
                        if (null == schregMap.get(schregno)) {
                            final String grade = rs.getString("GRADE");
                            final String hrClass = rs.getString("HR_CLASS");
                            final String attendno = rs.getString("ATTENDNO");
                            final String name = rs.getString("NAME");
                            final String course = rs.getString("COURSE");
                            final String courseName = rs.getString("COURSE_NAME");
                            final String hrName = rs.getString("HR_NAME");
                            final String trCd1Staffname = rs.getString("TR_CD1_STAFFNAME");
                            final String allergyMedicine = rs.getString("ALLERGY_MEDICINE");
                            final String allergyFood = rs.getString("ALLERGY_FOOD");
                            final String allergyOther = rs.getString("ALLERGY_OTHER");
                            final String medicalHistory1 = rs.getString("MEDICAL_HISTORY1");
                            final String medicalHistory1Name = rs.getString("MEDICAL_HISTORY1_NAME");
                            final String medicalHistory2 = rs.getString("MEDICAL_HISTORY2");
                            final String medicalHistory2Name = rs.getString("MEDICAL_HISTORY2_NAME");
                            final String medicalHistory3 = rs.getString("MEDICAL_HISTORY3");
                            final String medicalHistory3Name = rs.getString("MEDICAL_HISTORY3_NAME");
                            final String diagnosisName = rs.getString("DIAGNOSIS_NAME");
                            final String guideDiv = rs.getString("GUIDE_DIV");
                            final String guideDivName = rs.getString("GUIDE_DIV_NAME");
                            final String joiningSportsClub = rs.getString("JOINING_SPORTS_CLUB");
                            final String joiningSportsClubName = rs.getString("JOINING_SPORTS_CLUB_NAME");
                            final Student student = new Student(grade, hrClass, attendno, schregno, name, course, courseName, hrName, trCd1Staffname, allergyMedicine, allergyFood, allergyOther, medicalHistory1, medicalHistory1Name, medicalHistory2, medicalHistory2Name, medicalHistory3, medicalHistory3Name, diagnosisName, guideDiv, guideDivName, joiningSportsClub, joiningSportsClubName);
                            list.add(student);
                            schregMap.put(schregno, student);
                        }

                        final Student student = (Student) schregMap.get(schregno);

                        final String visitDate = rs.getString("VISIT_DATE");
                        final String visitHour = rs.getString("VISIT_HOUR");
                        final String visitMinute = rs.getString("VISIT_MINUTE");
                        final String leaveHour = rs.getString("LEAVE_HOUR");
                        final String leaveMinute = rs.getString("LEAVE_MINUTE");
                        final String visitReason1 = rs.getString("VISIT_REASON1");
                        final String visitReason1Name = rs.getString("VISIT_REASON1_NAME");
                        final String type = rs.getString("TYPE");
                        final String occurDate = rs.getString("OCCUR_DATE");
                        final String occurHour = rs.getString("OCCUR_HOUR");
                        final String occurMinute = rs.getString("OCCUR_MINUTE");
                        final String occurPlace = rs.getString("OCCUR_PLACE");
                        final String occurPlaceName = rs.getString("OCCUR_PLACE_NAME");
                        final String sleeptime = rs.getString("SLEEPTIME");
                        final String condition1 = rs.getString("CONDITION1");
                        final String condition2 = rs.getString("CONDITION2");
                        final String condition4 = rs.getString("CONDITION4");
                        final String treatment1 = rs.getString("TREATMENT1");
                        final String treatment1Name = rs.getString("TREATMENT1_NAME");
                        final String resttime = rs.getString("RESTTIME");
                        final String resttimeName = rs.getString("RESTTIME_NAME");
                        final Nurseoff nurseoff = new Nurseoff(visitDate, visitHour, visitMinute, leaveHour, leaveMinute, visitReason1, visitReason1Name, type, occurDate, occurHour, occurMinute, occurPlace, occurPlaceName, sleeptime, condition1, condition2, condition4, treatment1, treatment1Name, resttime, resttimeName);
                        student._nurseoffList.add(nurseoff);
                    }

                    DbUtils.closeQuietly(rs);
                }

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            try {
                final StringBuffer stb = new StringBuffer();

                stb.append(" SELECT ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     T1.ANNUAL, ");
                stb.append("     BASE.SEX AS SEX_CD, ");
                stb.append("     N1.NAME2 AS SEX, ");
                stb.append("     BASE.BIRTHDAY, ");
                stb.append("     T1.COURSECD, ");
                stb.append("     T1.MAJORCD, ");
                stb.append("     T1.COURSECODE, ");
                stb.append("     COURSE.COURSENAME, ");
                stb.append("     MAJOR.MAJORNAME, ");
                stb.append("     COURSEC.COURSECODENAME, ");
                stb.append("     GDAT.SCHOOL_KIND ");
                stb.append(" FROM ");
                stb.append("     SCHREG_REGD_DAT T1 ");
                stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON T1.YEAR = GDAT.YEAR ");
                stb.append("          AND T1.GRADE = GDAT.GRADE ");
                stb.append("     LEFT JOIN SCHREG_BASE_MST AS BASE ON T1.SCHREGNO = BASE.SCHREGNO ");
                stb.append("     LEFT JOIN NAME_MST AS N1 ON N1.NAMECD1 = 'Z002' ");
                stb.append("          AND BASE.SEX = N1.NAMECD2 ");
                stb.append("     LEFT JOIN COURSECODE_MST AS COURSEC ON T1.COURSECODE = COURSEC.COURSECODE ");
                stb.append("     LEFT JOIN MAJOR_MST AS MAJOR ON T1.COURSECD = MAJOR.COURSECD ");
                stb.append("          AND T1.MAJORCD = MAJOR.MAJORCD ");
                stb.append("     LEFT JOIN COURSE_MST AS COURSE ON T1.COURSECD = COURSE.COURSECD ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + param._year + "' ");
                stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
                stb.append("     AND T1.SCHREGNO = ? ");

                ps = db2.prepareStatement(stb.toString());
                for (final Iterator it = list.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();

                    while (rs.next()) {
                        student._annual = rs.getString("ANNUAL");
                        student._sexCd = rs.getString("SEX_CD");
                        student._sexName = rs.getString("SEX");
                        student._birthDay = rs.getString("BIRTHDAY");
                        student._coursecd = rs.getString("COURSECD");
                        student._majorcd = rs.getString("MAJORCD");
                        student._coursecode = rs.getString("COURSECODE");
                        student._coursename = rs.getString("COURSENAME");
                        student._majorname = rs.getString("MAJORNAME");
                        student._coursecodename = rs.getString("COURSECODENAME");
                        student._schoolKind = rs.getString("SCHOOL_KIND");

                    }
                    DbUtils.closeQuietly(rs);

                    student._medexamDetDat = new MedexamDetDat(db2, param._year, student._schregno, param._printKenkouSindanIppan);

                    student._medexamToothDat = new MedexamToothDat(db2, param._year, student._schregno);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            return list;
        }

        public static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   REGD.GRADE, ");
            stb.append("   REGD.HR_CLASS, ");
            stb.append("   REGD.ATTENDNO, ");
            stb.append("   REGD.SCHREGNO, ");
            stb.append("   BASE.NAME, ");
            stb.append("   REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE AS COURSE, ");
            stb.append("   VALUE(MAJM.MAJORNAME, '') || VALUE(CRSCDM.COURSECODENAME, '') AS COURSE_NAME, ");
            stb.append("   REGDH.HR_NAME, ");
            stb.append("   STFM.STAFFNAME AS TR_CD1_STAFFNAME, ");
            stb.append("   T2.ALLERGY_MEDICINE, ");
            stb.append("   T2.ALLERGY_FOOD, ");
            stb.append("   T2.ALLERGY_OTHER, ");
            stb.append("   MED.MEDICAL_HISTORY1, ");
            stb.append("   NMF143_1.NAME1 AS MEDICAL_HISTORY1_NAME, ");
            stb.append("   MED.MEDICAL_HISTORY2, ");
            stb.append("   NMF143_2.NAME1 AS MEDICAL_HISTORY2_NAME, ");
            stb.append("   MED.MEDICAL_HISTORY3, ");
            stb.append("   NMF143_3.NAME1 AS MEDICAL_HISTORY3_NAME, ");
            stb.append("   MED.DIAGNOSIS_NAME, ");
            stb.append("   MED.GUIDE_DIV, ");
            stb.append("   NMF141.NAME1 AS GUIDE_DIV_NAME, ");
            stb.append("   MED.JOINING_SPORTS_CLUB, ");
            stb.append("   NMF142.NAME1 AS JOINING_SPORTS_CLUB_NAME, ");
            stb.append("   NRSVR.VISIT_DATE, ");
            stb.append("   NRSVR.VISIT_HOUR, ");
            stb.append("   NRSVR.VISIT_MINUTE, ");
            stb.append("   NRSVR.LEAVE_HOUR, ");
            stb.append("   NRSVR.LEAVE_MINUTE, ");
            stb.append("   NRSVR.VISIT_REASON1, ");
            stb.append("   CASE NRSVR.TYPE WHEN '1' THEN NMF200.NAME1 ");
            stb.append("                   WHEN '2' THEN NMF201.NAME1 ");
            stb.append("                   WHEN '5' THEN NMF219.NAME1 ");
            stb.append("                   WHEN '3' THEN NMF202.NAME1 ");
            stb.append("                   WHEN '4' THEN NMF203.NAME1 ");
            stb.append("   END AS VISIT_REASON1_NAME, ");
            stb.append("   NRSVR.TYPE, ");
            stb.append("   NRSVR.OCCUR_DATE, ");
            stb.append("   NRSVR.OCCUR_HOUR, ");
            stb.append("   NRSVR.OCCUR_MINUTE, ");
            stb.append("   NRSVR.OCCUR_PLACE, ");
            stb.append("   NMF206.NAME1 AS OCCUR_PLACE_NAME, ");
            stb.append("   NRSVR.SLEEPTIME, ");
            stb.append("   NRSVR.CONDITION1, ");
            stb.append("   NRSVR.CONDITION2, ");
            stb.append("   NRSVR.CONDITION4, ");
            stb.append("   NRSVR.TREATMENT1, ");
            stb.append("   CASE NRSVR.TYPE WHEN '1' THEN NMF208.NAME1 ");
            stb.append("                   WHEN '2' THEN NMF209.NAME1 ");
            stb.append("                   WHEN '5' THEN NMF220.NAME1 ");
            stb.append("                   WHEN '3' THEN NMF210.NAME1 ");
            stb.append("                   WHEN '4' THEN NMF210.NAME1 ");
            stb.append("   END AS TREATMENT1_NAME, ");
            stb.append("   NRSVR.RESTTIME, ");
            stb.append("   NMF212.NAME1 AS RESTTIME_NAME ");
            stb.append(" FROM SCHREG_REGD_DAT REGD ");
            stb.append(" INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append(" LEFT JOIN MAJOR_MST MAJM ON MAJM.COURSECD = REGD.COURSECD AND MAJM.MAJORCD = REGD.MAJORCD ");
            stb.append(" LEFT JOIN COURSECODE_MST CRSCDM ON CRSCDM.COURSECODE = REGD.COURSECODE ");
            stb.append(" LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR ");
            stb.append("     AND REGDH.SEMESTER = REGD.SEMESTER ");
            stb.append("     AND REGDH.GRADE = REGD.GRADE ");
            stb.append("     AND REGDH.HR_CLASS = REGD.HR_CLASS ");
            stb.append(" LEFT JOIN STAFF_MST STFM ON STFM.STAFFCD = REGDH.TR_CD1 ");
            stb.append(" LEFT JOIN HEALTH_INVEST_OTHER_DAT T2 ON T2.SCHREGNO = REGD.SCHREGNO ");
            stb.append(" LEFT JOIN MEDEXAM_DET_DAT MED ON MED.YEAR = REGD.YEAR ");
            stb.append("     AND MED.SCHREGNO = REGD.SCHREGNO ");
            stb.append(" LEFT JOIN NAME_MST NMF143_1 ON NMF143_1.NAMECD1 = 'F143' ");
            stb.append("    AND NMF143_1.NAMECD2 = MED.MEDICAL_HISTORY1 ");
            stb.append(" LEFT JOIN NAME_MST NMF143_2 ON NMF143_2.NAMECD1 = 'F143' ");
            stb.append("    AND NMF143_2.NAMECD2 = MED.MEDICAL_HISTORY2 ");
            stb.append(" LEFT JOIN NAME_MST NMF143_3 ON NMF143_3.NAMECD1 = 'F143' ");
            stb.append("    AND NMF143_3.NAMECD2 = MED.MEDICAL_HISTORY3 ");
            stb.append(" LEFT JOIN NAME_MST NMF141 ON NMF141.NAMECD1 = 'F141' ");
            stb.append("   AND NMF141.NAMECD2 = MED.GUIDE_DIV ");
            stb.append(" LEFT JOIN NAME_MST NMF142 ON NMF142.NAMECD1 = 'F142' ");
            stb.append("   AND NMF142.NAMECD2 = MED.JOINING_SPORTS_CLUB ");
            stb.append(" LEFT JOIN NURSEOFF_VISITREC_DAT NRSVR ON NRSVR.SCHREGNO = REGD.SCHREGNO ");
            stb.append("   AND VISIT_DATE BETWEEN '" + param._date1 + "' AND '" + param._date2 + "' ");
            stb.append(" LEFT JOIN NAME_MST NMF200 ON NMF200.NAMECD1 = 'F200' ");
            stb.append("   AND NMF200.NAMECD2 = NRSVR.VISIT_REASON1 ");
            stb.append(" LEFT JOIN NAME_MST NMF201 ON NMF201.NAMECD1 = 'F201' ");
            stb.append("   AND NMF201.NAMECD2 = NRSVR.VISIT_REASON1 ");
            stb.append(" LEFT JOIN NAME_MST NMF219 ON NMF219.NAMECD1 = 'F219' ");
            stb.append("   AND NMF219.NAMECD2 = NRSVR.VISIT_REASON1 ");
            stb.append(" LEFT JOIN NAME_MST NMF202 ON NMF202.NAMECD1 = 'F202' ");
            stb.append("   AND NMF202.NAMECD2 = NRSVR.VISIT_REASON1 ");
            stb.append(" LEFT JOIN NAME_MST NMF203 ON NMF203.NAMECD1 = 'F203' ");
            stb.append("   AND NMF203.NAMECD2 = NRSVR.VISIT_REASON1 ");
            stb.append(" LEFT JOIN NAME_MST NMF206 ON NMF206.NAMECD1 = 'F206' ");
            stb.append("   AND NMF206.NAMECD2 = NRSVR.OCCUR_PLACE ");
            stb.append(" LEFT JOIN NAME_MST NMF208 ON NMF208.NAMECD1 = 'F208' ");
            stb.append("   AND NMF208.NAMECD2 = NRSVR.TREATMENT1 ");
            stb.append(" LEFT JOIN NAME_MST NMF209 ON NMF209.NAMECD1 = 'F209' ");
            stb.append("   AND NMF209.NAMECD2 = NRSVR.TREATMENT1 ");
            stb.append(" LEFT JOIN NAME_MST NMF220 ON NMF220.NAMECD1 = 'F220' ");
            stb.append("   AND NMF220.NAMECD2 = NRSVR.TREATMENT1 ");
            stb.append(" LEFT JOIN NAME_MST NMF210 ON NMF210.NAMECD1 = 'F210' ");
            stb.append("   AND NMF210.NAMECD2 = NRSVR.TREATMENT1 ");
            stb.append(" LEFT JOIN NAME_MST NMF212 ON NMF212.NAMECD1 = 'F212' ");
            stb.append("   AND NMF212.NAMECD2 = NRSVR.RESTTIME ");
            stb.append(" WHERE ");
            stb.append(" REGD.YEAR = '" + param._year + "' ");
            stb.append(" AND REGD.SEMESTER = '" + param._semester + "' ");
            stb.append(" AND REGD.SCHREGNO = ? ");
            stb.append(" ORDER BY ");
            stb.append("   NRSVR.VISIT_DATE, ");
            stb.append("   NRSVR.VISIT_HOUR, ");
            stb.append("   NRSVR.VISIT_MINUTE, ");
            stb.append("   NRSVR.LEAVE_HOUR, ");
            stb.append("   NRSVR.LEAVE_MINUTE ");
            return stb.toString();
        }
    }

    private static class Nurseoff {
        final String _visitDate;
        final String _visitHour;
        final String _visitMinute;
        final String _leaveHour;
        final String _leaveMinute;
        final String _visitReason1;
        final String _visitReason1Name;
        final String _type;
        final String _occurDate;
        final String _occurHour;
        final String _occurMinute;
        final String _occurPlace;
        final String _occurPlaceName;
        final String _sleeptime;
        final String _condition1;
        final String _condition2;
        final String _condition4;
        final String _treatment1;
        final String _treatment1Name;
        final String _resttime;
        final String _resttimeName;

        Nurseoff(
            final String visitDate,
            final String visitHour,
            final String visitMinute,
            final String leaveHour,
            final String leaveMinute,
            final String visitReason1,
            final String visitReason1Name,
            final String type,
            final String occurDate,
            final String occurHour,
            final String occurMinute,
            final String occurPlace,
            final String occurPlaceName,
            final String sleeptime,
            final String condition1,
            final String condition2,
            final String condition4,
            final String treatment1,
            final String treatment1Name,
            final String resttime,
            final String resttimeName
        ) {
            _visitDate = visitDate;
            _visitHour = visitHour;
            _visitMinute = visitMinute;
            _leaveHour = leaveHour;
            _leaveMinute = leaveMinute;
            _visitReason1 = visitReason1;
            _visitReason1Name = visitReason1Name;
            _type = type;
            _occurDate = occurDate;
            _occurHour = occurHour;
            _occurMinute = occurMinute;
            _occurPlace = occurPlace;
            _occurPlaceName = occurPlaceName;
            _sleeptime = sleeptime;
            _condition1 = condition1;
            _condition2 = condition2;
            _condition4 = condition4;
            _treatment1 = treatment1;
            _treatment1Name = treatment1Name;
            _resttime = resttime;
            _resttimeName = resttimeName;
        }

        public static String condition1Name(final String condition1) {
            String rtn = null;
            if ("1".equals(condition1)) {
                rtn = "はい";
            } else if ("2".equals(condition1)) {
                rtn = "いいえ";
            }
            return rtn;
        }

        public static String condition2Name(final String condition2) {
            String rtn = null;
            if ("1".equals(condition2)) {
                rtn = "いつもよく眠れる";
            } else if ("2".equals(condition2)) {
                rtn = "余り眠れない";
            }
            return rtn;
        }

        public static String condition4Name(final String condition4) {
            String rtn = null;
            if ("1".equals(condition4)) {
                rtn = "食べた";
            } else if ("2".equals(condition4)) {
                rtn = "食べていない";
            } else if ("3".equals(condition4)) {
                rtn = "いつも食べない";
            }
            return rtn;
        }

        public static String typeName(final String type) {
            String rtn = null;
            if ("1".equals(type)) {
                rtn = "内科";
            } else if ("2".equals(type)) {
                rtn = "外科";
            } else if ("5".equals(type)) {
                rtn = "健康相談";
            } else if ("3".equals(type)) {
                rtn = "その他";
            } else if ("4".equals(type)) {
                rtn = "生徒以外";
            }
            return rtn;
        }
    }

    /**
     * 文面マスタ情報 文面マスタからタイトルと本文を取得
     */
    private String statementTitle(final String documentcd) {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT TITLE, TEXT FROM DOCUMENT_MST WHERE DOCUMENTCD = '" + documentcd + "'");
        } catch (Exception e) {
            log.warn("statementTitle error!", e);
        }
        return stb.toString();
    }

    private int printStudent2(final DB2UDB db2, final Vrw32alp svf, int page, final Student student, final Map physAvgMap) {
        // KNJF030CTeikiKenshin
        page += 1;
        final String form = !_param._isKumamoto ? "KNJF100BC_3_2.frm" : "KNJF100BC_3.frm";
        svf.VrSetForm(form, 1);
        svf.VrsOut("DATE", _param.changePrintDate(db2, _param._ctrlDate));
//        svf.VrsOut("SCHOOLNAME1", _param.schoolInfoVal(student._schoolKind, Param.SCHOOL_NAME1));
//        svf.VrsOut("PRINCIPAL_NAME", _param.schoolInfoVal(student._schoolKind, Param.PRINCIPAL_NAME));
//        svf.VrsOut("PRINCIPAL_JOBNAME", _param.schoolInfoVal(student._schoolKind, Param.PRINCIPAL_JOBNAME));
        svf.VrsOut("TITLE", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度　定期健康診断結果一覧");
        svf.VrsOut("HR_NAME", student._hrName);
        svf.VrsOut("ATTENDNO", String.valueOf(Integer.parseInt(student._attendno)) + "番");
        svf.VrsOut("MAJOR", student._majorname);
        final int namelen = getMS932ByteLength(student._name);
        svf.VrsOut("NAME" + ((34 < namelen) ? "3" : (20 < namelen) ? "2" : ""), student._name);

        if (null != student._medexamDetDat) {
            final String detMonth = null != student._medexamDetDat._detMonth ? student._medexamDetDat._detMonth + "月" : "";
            final String detDate = _param.changePrintYear(db2, student._medexamDetDat._detYear) + detMonth;
            svf.VrsOut("DET_DATE", detDate);
            svf.VrsOut("HEIGHT", student._medexamDetDat._height);
            svf.VrsOut("WEIGHT", student._medexamDetDat._weight);
            svf.VrsOut("SITHEIGHT", student._medexamDetDat._sitheight);
            svf.VrsOut("R_BAREVISION_MARK", student._medexamDetDat._rBarevisionMark);
            svf.VrsOut("R_VISION_MARK", student._medexamDetDat._rVisionMark);
            svf.VrsOut("L_BAREVISION_MARK", student._medexamDetDat._lBarevisionMark);
            svf.VrsOut("L_VISION_MARK", student._medexamDetDat._lVisionMark);
            if (!_param._isKumamoto) {
                svf.VrsOut("BMI", calcHimando(student, physAvgMap, _param));
            } else {
                svf.VrsOut("BMI", student._medexamDetDat._bmi);
            }
            if (_param._tbPrint) {
                setName(db2, svf, "TB_X_RAY", "F100", student._medexamDetDat._tbRemarkcd, "NAME1", 0, "", "");
                svf.VrsOut("TB_X_RAY_REMARK", null != student._medexamDetDat._tbXRay ? "(" + student._medexamDetDat._tbXRay + ")" : "");
            }
            if ("2".equals(_param._printKenkouSindanIppan)) {
                final String setRyear = "99".equals(student._medexamDetDat._rEar) ? "(" + student._medexamDetDat._rEarDb + ")" : "";
                final String setLyear = "99".equals(student._medexamDetDat._lEar) ? "(" + student._medexamDetDat._lEarDb + ")" : "";
                setName(db2, svf, "R_EAR", "F010", student._medexamDetDat._rEar, "NAME1", 0, setRyear, "");
                setName(db2, svf, "L_EAR", "F010", student._medexamDetDat._lEar, "NAME1", 0, setLyear, "");
            } else {
                setName(db2, svf, "R_EAR", "F010", student._medexamDetDat._rEar, "NAME1", 0, "", "");
                setName(db2, svf, "L_EAR", "F010", student._medexamDetDat._lEar, "NAME1", 0, "", "");
            }

            setName(db2, svf, "ALBUMINURIA1", "F020", student._medexamDetDat._albuminuria1cd, "NAME1", 0, "", "");
            setName(db2, svf, "URICSUGAR1", "F019", student._medexamDetDat._uricsugar1cd, "NAME1", 0, "", "");
            setName(db2, svf, "URICBLEED1", "F018", student._medexamDetDat._uricbleed1cd, "NAME1", 0, "", "");
            setName(db2, svf, "ALBUMINURIA2", "F020", student._medexamDetDat._albuminuria2cd, "NAME1", 0, "", "");
            setName(db2, svf, "URICSUGAR2", "F019", student._medexamDetDat._uricsugar2cd, "NAME1", 0, "", "");
            setName(db2, svf, "URICBLEED2", "F018", student._medexamDetDat._uricbleed2cd, "NAME1", 0, "", "");
            if (_param._heartMedexamPrint) {
                String name = getName(db2, "F080", student._medexamDetDat._heartMedexam, "NAME1");
                String remark = null != student._medexamDetDat._managementRemark ? "(" + student._medexamDetDat._managementRemark + ")" : "";
                svf.VrsOut("HEART_MEDEXAM", name + remark);
            }
            setName(db2, svf, "OTHERDISEASE", "F144", student._medexamDetDat._docCd, "NAME1", 0, "", "");
            final String setRemark = student._medexamDetDat._docRemark;
            svf.VrsOut("REMARK", null != setRemark ? "(" + setRemark + ")": "");
            setName(db2, svf, "EYEDISEASE", "F050", student._medexamDetDat._eyediseasecd, "NAME1", 0, "", "");
            final String setEyeTestResult = student._medexamDetDat._eyeTestResult;
            svf.VrsOut("EYE_TEST_RESULT", null != setEyeTestResult ? "(" + setEyeTestResult + ")" : "");
        }
        if (null != student._medexamToothDat) {
            final int reBaby = null != student._medexamToothDat._remainbabytooth ? Integer.parseInt(student._medexamToothDat._remainbabytooth) : 0;
            final int reAdult = null != student._medexamToothDat._remainadulttooth ? Integer.parseInt(student._medexamToothDat._remainadulttooth) : 0;
            final boolean remainToothNull = null == student._medexamToothDat._remainbabytooth && null == student._medexamToothDat._remainadulttooth ? true : false;
            svf.VrsOut("REMAIN_TOOTH", remainToothNull ? "" : reBaby + reAdult > 0 ? "あり" : "なし");
            final int brackB = null != student._medexamToothDat._brackBabytooth ? Integer.parseInt(student._medexamToothDat._brackBabytooth) : 0;
            final boolean brackBabyNull = null == student._medexamToothDat._brackBabytooth ? true : false;
            svf.VrsOut("BRACK_BABYTOOTH", brackBabyNull ? "" : brackB > 0 ? "あり" : "なし");
            final int brackA = null != student._medexamToothDat._brackAdulttooth ? Integer.parseInt(student._medexamToothDat._brackAdulttooth) : 0;
            final boolean brackAdultNull = null == student._medexamToothDat._brackAdulttooth ? true : false;
            svf.VrsOut("BRACK_ADULTTOOTH", brackAdultNull ? "" : brackA > 0 ? "あり" : "なし");
            final int lostA = null != student._medexamToothDat._lostadulttooth ? Integer.parseInt(student._medexamToothDat._lostadulttooth) : 0;
            final boolean lostAdultNull = null == student._medexamToothDat._lostadulttooth ? true : false;
            svf.VrsOut("LOSTADULTTOOTH", lostAdultNull ? "" : lostA > 0 ? "あり" : "なし");
            final int chkA = null != student._medexamToothDat._checkAdulttooth ? Integer.parseInt(student._medexamToothDat._checkAdulttooth) : 0;
            final boolean thoroughNull = null == student._medexamToothDat._checkAdulttooth ? true : false;
            svf.VrsOut("THOROUGH_TOOTH", thoroughNull ? "" : chkA > 0 ? "あり" : "なし");

            setName(db2, svf, "JAWS_JOINT1", "F510", student._medexamToothDat._jawsJointcd, "NAME1", 10, "", "_1");
            setName(db2, svf, "JAWS_JOINT2", "F511", student._medexamToothDat._jawsJointcd2, "NAME1", 10, "", "_1");
            setName(db2, svf, "PLAQUE", "F520", student._medexamToothDat._plaquecd, "NAME1", 10, "", "1_1");
            setName(db2, svf, "GUM", "F513", student._medexamToothDat._gumcd, "NAME1", 10, "", "1_1");
            setName(db2, svf, "TARTAR", "F521", student._medexamToothDat._calculuscd, "NAME1", 10, "", "1_1");
        }
        svf.VrsOut("NOTICE", "注3）聴力検査は、1年生と3年生実施。");
        svf.VrEndPage();
        return page;
    }

    // 肥満度計算
    //  肥満度（過体重度）= 100 × (測定された体重 - 標準体重) / 標準体重
    private String calcHimando(final Student student, final Map physAvgMap, final Param param) {
        if (null == student._medexamDetDat._weight) {
            log.debug(" " + student._schregno + ", " + param._year + " 体重がnull");
            return null;
        }
        BigDecimal weightAvg = null;
        final boolean isUseMethod2 = true;
        if (isUseMethod2) {
            // final BigDecimal weightAvg1 = getWeightAvgMethod1(student, mdnd, param);
            final BigDecimal weightAvg2 = getWeightAvgMethod2(student, physAvgMap, param);
            // log.fatal(" (schregno, attendno, weight1, weight2) = (" + student._schregno + ", " + student._attendno + ", " + weightAvg1 + ", " + weightAvg2 + ")");
            log.fatal(" (schregno, attendno, weight2) = (" + student._schregno + ", " + student._attendno + ", " + weightAvg2 + ")");
            weightAvg = weightAvg2;
        } else {
            // weightAvg = null; getWeightAvgMethod0(student, mdnd, physAvgMap);
        }
        if (null == weightAvg) {
            return null;
        }
        final BigDecimal himando = new BigDecimal(100).multiply(new BigDecimal(Double.parseDouble(student._medexamDetDat._weight)).subtract(weightAvg)).divide(weightAvg, 1, BigDecimal.ROUND_HALF_UP);
        log.fatal(" himando = 100 * (" + student._medexamDetDat._weight + " - " + weightAvg + ") / " + weightAvg + " = " + himando);
        return himando.toString();
    }

    private BigDecimal getWeightAvgMethod2(final Student student, final Map physAvgMap, final Param param) {
        if (null == student._medexamDetDat._height) {
            log.debug(" " + student._schregno + ", " + param._year + " 身長がnull");
            return null;
        }
        if (null == student._birthDay) {
            log.debug(" " + student._schregno + ", " + param._year + " 生年月日がnull");
            return null;
        }
        // 日本小児内分泌学会 (http://jspe.umin.jp/)
        // http://jspe.umin.jp/ipp_taikaku.htm ２．肥満度 ２）性別・年齢別・身長別標準体重（５歳以降）のデータによる
        // ａ＝ HEXAM_PHYSICAL_AVG_DAT.STD_WEIGHT_KEISU_A
        // ｂ＝ HEXAM_PHYSICAL_AVG_DAT.STD_WEIGHT_KEISU_B　
        // 標準体重＝ａ×身長（cm）- ｂ 　 　
        final BigDecimal height = new BigDecimal(student._medexamDetDat._height);
        final String kihonDate = param._year + "-04-01";
        final int iNenrei = (int) getNenrei(student, kihonDate, param._year, param._year);
//        final int iNenrei = (int) getNenrei2(student, param._year, param._year);
        final HexamPhysicalAvgDat hpad = getPhysicalAvgDatNenrei(iNenrei, (List) physAvgMap.get(student._sexCd));
        if (null == hpad || null == hpad._stdWeightKeisuA || null == hpad._stdWeightKeisuB) {
            return null;
        }
        final BigDecimal a = hpad._stdWeightKeisuA;
        final BigDecimal b = hpad._stdWeightKeisuB;
        final BigDecimal avgWeight = a.multiply(height).subtract(b);
        log.fatal(" method2 avgWeight = " + a + " * " + height + " - " + b + " = " + avgWeight);
        return avgWeight;
    }

    // 学年から年齢を計算する
    private double getNenrei2(final Student student, final String year1, final String year2) {
        return 5.0 + Integer.parseInt(student._grade) - (StringUtils.isNumeric(year1) && StringUtils.isNumeric(year2) ? Integer.parseInt(year1) - Integer.parseInt(year2) : 0); // 1年生:6才、2年生:7才、...6年生:11才
    }

    // 生年月日と対象日付から年齢を計算する
    private double getNenrei(final Student student, final String date, final String year1, final String year2) {
        if (null == student._birthDay) {
            return getNenrei2(student, year1, year2);
        }
        final Calendar calBirthDate = Calendar.getInstance();
        calBirthDate.setTime(java.sql.Date.valueOf(student._birthDay));
        final int birthYear = calBirthDate.get(Calendar.YEAR);
        final int birthDayOfYear = calBirthDate.get(Calendar.DAY_OF_YEAR);

        final Calendar calTestDate = Calendar.getInstance();
        calTestDate.setTime(java.sql.Date.valueOf(date));
        final int testYear = calTestDate.get(Calendar.YEAR);
        final int testDayOfYear = calTestDate.get(Calendar.DAY_OF_YEAR);

        int nenreiYear = testYear - birthYear + (testDayOfYear - birthDayOfYear < 0 ? -1 : 0);
        final int nenreiDateOfYear = testDayOfYear - birthDayOfYear + (testDayOfYear - birthDayOfYear < 0 ? 365 : 0);
        final double nenrei = nenreiYear + nenreiDateOfYear / 365.0;
        return nenrei;
    }

    // 年齢の平均データを得る
    private HexamPhysicalAvgDat getPhysicalAvgDatNenrei(final int nenrei, final List physAvgList) {
        HexamPhysicalAvgDat tgt = null;
        for (final Iterator it = physAvgList.iterator(); it.hasNext();) {
            final HexamPhysicalAvgDat hpad = (HexamPhysicalAvgDat) it.next();
            if (hpad._nenrei <= nenrei) {
                tgt = hpad;
                if (hpad._nenreiYear == nenrei) {
                    break;
                }
            }
        }
        return tgt;
    }

    protected String getName(final DB2UDB _db2, final String nameCd1, final String nameCd2, final String useFieldName) {
        final int checkNameCd2 = null != nameCd2 ? Integer.parseInt(nameCd2) : 0;
        final String sql = "SELECT VALUE(" + useFieldName + ", '') AS LABEL FROM NAME_MST WHERE NAMECD1 = '" + nameCd1 + "' AND NAMECD2 = '" + nameCd2 + "'";
        ResultSet rs = null;
        PreparedStatement ps = null;
        String label = "";
        try {
            ps = _db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                label = checkNameCd2 == 0 ? "" : rs.getString("LABEL");
            }
        } catch (Exception ex) {
            log.error("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            _db2.commit();
        }
        return label;
    }

    private static void setName(
            final DB2UDB _db2,
            final Vrw32alp _svf,
            final String fieldName,
            final String nameCd1,
            final String nameCd2,
            final String useFieldName,
            final int lineLength,
            final String plusString,
            final String fieldNameOver
    ) {
        final int checkNameCd2 = null != nameCd2 ? Integer.parseInt(nameCd2) : -1;
        final String sql = "SELECT VALUE(" + useFieldName + ", '') AS LABEL FROM NAME_MST WHERE NAMECD1 = '" + nameCd1 + "' AND NAMECD2 = '" + nameCd2 + "'";
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            ps = _db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String setName = checkNameCd2 == -1 ? "" + plusString : rs.getString("LABEL") + plusString;
                if (lineLength > 0 && setName.length() > lineLength) {
                    _svf.VrsOut(fieldName + fieldNameOver, setName);
                } else {
                    _svf.VrsOut(fieldName, setName);
                }
            }
        } catch (Exception ex) {
            log.error("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            _db2.commit();
        }
    }

    static class HexamPhysicalAvgDat {
        final String _sex;
        final int _nenreiYear;
        final int _nenreiMonth;
        final double _nenrei;
        final BigDecimal _heightAvg;
        final BigDecimal _heightSd;
        final BigDecimal _weightAvg;
        final BigDecimal _weightSd;
        final BigDecimal _stdWeightKeisuA;
        final BigDecimal _stdWeightKeisuB;

        HexamPhysicalAvgDat(
            final String sex,
            final int nenreiYear,
            final int nenreiMonth,
            final BigDecimal heightAvg,
            final BigDecimal heightSd,
            final BigDecimal weightAvg,
            final BigDecimal weightSd,
            final BigDecimal stdWeightKeisuA,
            final BigDecimal stdWeightKeisuB
        ) {
            _sex = sex;
            _nenreiYear = nenreiYear;
            _nenreiMonth = nenreiMonth;
            _nenrei = _nenreiYear + (_nenreiMonth / 12.0);
            _heightAvg = heightAvg;
            _heightSd = heightSd;
            _weightAvg = weightAvg;
            _weightSd = weightSd;
            _stdWeightKeisuA = stdWeightKeisuA;
            _stdWeightKeisuB = stdWeightKeisuB;
        }

        public static Map getHexamPhysicalAvgMap(final DB2UDB db2, final Param param) {
            final Map m = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql(param));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String sex = rs.getString("SEX");
                    final int nenreiYear = rs.getInt("NENREI_YEAR");
                    final int nenreiMonth = rs.getInt("NENREI_MONTH");
                    // if (ageMonth % 3 != 0) { continue; }
                    final BigDecimal heightAvg = rs.getBigDecimal("HEIGHT_AVG");
                    final BigDecimal heightSd = rs.getBigDecimal("HEIGHT_SD");
                    final BigDecimal weightAvg = rs.getBigDecimal("WEIGHT_AVG");
                    final BigDecimal weightSd = rs.getBigDecimal("WEIGHT_SD");
                    final BigDecimal stdWeightKeisuA = rs.getBigDecimal("STD_WEIGHT_KEISU_A");
                    final BigDecimal stdWeightKeisuB = rs.getBigDecimal("STD_WEIGHT_KEISU_B");
                    final HexamPhysicalAvgDat testheightweight = new HexamPhysicalAvgDat(sex, nenreiYear, nenreiMonth, heightAvg, heightSd, weightAvg, weightSd, stdWeightKeisuA, stdWeightKeisuB);
                    if (null == m.get(rs.getString("SEX"))) {
                        m.put(rs.getString("SEX"), new ArrayList());
                    }
                    final List list = (List) m.get(rs.getString("SEX"));
                    list.add(testheightweight);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return m;
        }

        private static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" WITH MAX_YEAR AS ( ");
            stb.append("   SELECT ");
            stb.append("       MAX(YEAR) AS YEAR ");
            stb.append("   FROM ");
            stb.append("       HEXAM_PHYSICAL_AVG_DAT T1 ");
            stb.append("   WHERE ");
            stb.append("       T1.YEAR <= '" + param._year + "' ");
            stb.append(" ), MIN_YEAR AS ( ");
            stb.append("   SELECT ");
            stb.append("       MIN(YEAR) AS YEAR ");
            stb.append("   FROM ");
            stb.append("       HEXAM_PHYSICAL_AVG_DAT T1 ");
            stb.append("   WHERE ");
            stb.append("       T1.YEAR >= '" + param._year + "' ");
            stb.append(" ), MAX_MIN_YEAR AS ( ");
            stb.append("   SELECT ");
            stb.append("       MIN(T1.YEAR) AS YEAR ");
            stb.append("   FROM ( ");
            stb.append("       SELECT YEAR FROM MAX_YEAR T1 ");
            stb.append("       UNION ");
            stb.append("       SELECT YEAR FROM MIN_YEAR T1 ");
            stb.append("   ) T1 ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.SEX, ");
            stb.append("     T1.NENREI_YEAR, ");
            stb.append("     T1.NENREI_MONTH, ");
            stb.append("     T1.HEIGHT_AVG, ");
            stb.append("     T1.HEIGHT_SD, ");
            stb.append("     T1.WEIGHT_AVG, ");
            stb.append("     T1.WEIGHT_SD, ");
            stb.append("     T1.STD_WEIGHT_KEISU_A, ");
            stb.append("     T1.STD_WEIGHT_KEISU_B ");
            stb.append(" FROM ");
            stb.append("    HEXAM_PHYSICAL_AVG_DAT T1 ");
            stb.append("    INNER JOIN MAX_MIN_YEAR T2 ON T2.YEAR = T1.YEAR ");
            stb.append(" ORDER BY ");
            stb.append("     T1.SEX, T1.NENREI_YEAR, T1.NENREI_MONTH ");
            return stb.toString();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 60601 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private class Param {
//        static final String SCHOOL_NAME1 = "SCHOOL_NAME1";
//        static final String SCHOOL_NAME2 = "SCHOOL_NAME2";
//        static final String PRINCIPAL_NAME = "PRINCIPAL_NAME";
//        static final String PRINCIPAL_JOBNAME = "PRINCIPAL_JOBNAME";

        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _prgid;
        final String _date1;
        final String _date2;
        final String _yougoStaffname;
        final String _useFormNameF100BC_1;
        final List _nameMstL007;
        String _year;
        String _semester;

        // KNJF100B, KNJF100C
        String[] _schregnos; // 生徒

        final boolean _heartMedexamPrint = true;
        final boolean _tbPrint = true;
        final KNJ_Schoolinfo _schoolinfo;
        private final KNJ_Schoolinfo.ReturnVal _schoolInfoVal;
        private String _certifSchoolDatSchoolName;
        private String _certifSchoolDatJobName;
        private String _certifSchoolDatPrincipalName;
        private String _certifSchoolDatRemark1;
        private String _certifSchoolDatRemark2;
        private String _certifSchoolDatRemark3;
        private String _certifSchoolDatRemark4;
        private String _certifSchoolDatRemark5;
        private String _certifSchoolDatRemark6;
        final String _schoolJudge;
        private boolean _seirekiFlg;
        private String _printKenkouSindanIppan = "2";
        final boolean _isKumamoto;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _prgid = request.getParameter("PRGID");
            _date1 = null == request.getParameter("DATE1") ? request.getParameter("DATE1") : request.getParameter("DATE1").replace('/', '-');
            _date2 = null == request.getParameter("DATE2") ? request.getParameter("DATE2") : request.getParameter("DATE2").replace('/', '-');
            if (PRGID_KNJF100B.equals(_prgid)) {
                _year = request.getParameter("YEAR");
                _semester = request.getParameter("SEMESTER");
            } else { // if (PRGID_KNJF100C.equals(_prgid) || PRGID_KNJF100D.equals(_prgid) || PRGID_KNJF100E.equals(_prgid)) {
                _year = _ctrlYear;
                _semester = _ctrlSemester;
            }
            final String[] studentSelected = request.getParameterValues("STUDENT_SELECTED");
            _schregnos = new String[studentSelected.length];
            for (int i = 0; i < studentSelected.length; i++) {
                _schregnos[i] = StringUtils.split(studentSelected[i], "-")[1];
            }
            _yougoStaffname = getYougoStaffname(db2);
            _useFormNameF100BC_1 = StringUtils.defaultString(request.getParameter("useFormNameF100BC_1"));
            _nameMstL007 = getNameMstL007(db2);
            _isKumamoto = "kumamoto".equals(getNamemstZ010(db2));

            _schoolJudge = request.getParameter("SCHOOL_JUDGE");
            try {
                _schoolinfo = new KNJ_Schoolinfo(_year); //取得クラスのインスタンス作成
                _schoolInfoVal = _schoolinfo.get_info(db2);
            } finally {
                db2.commit();
            }
//            setSeirekiFlg(db2);

            setCertifSchoolDat(db2);
        }

        private String getYougoStaffname(final DB2UDB db2) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = "";
                sql += " SELECT T2.STAFFNAME FROM CERTIF_SCHOOL_DAT T1 ";
                sql += " INNER JOIN STAFF_MST T2 ON T2.STAFFCD = T1.REMARK4 ";
                sql += " WHERE T1.YEAR = '" + _year + "' AND T1.CERTIF_KINDCD = '124' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    rtn = rs.getString("STAFFNAME");
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private List getNameMstL007(final DB2UDB db2) {
            final List list = new ArrayList();
            final String sql = " SELECT * FROM NAME_MST WHERE NAMECD1 = 'L007' ORDER BY NAMECD2 DESC ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                ResultSetMetaData meta = rs.getMetaData();

                while (rs.next()) {
                    final Map m = new HashMap();
                    for (int i = 1; i <= meta.getColumnCount(); i++) {
                        final String columnName = meta.getColumnName(i);
                        m.put(columnName, rs.getString(columnName));
                    }
                    list.add(m);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private String getNamemstZ010(final DB2UDB db2) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z010' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    rtn = rs.getString("NAME1");
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        public String dateString(final String date) {
            if (null != date) {
                final DateFormat df = new SimpleDateFormat("yyyy-mm-dd");
                try {
                    final Date d = java.sql.Date.valueOf(date);
                    final Calendar cal = Calendar.getInstance();
                    cal.setTime(d);
                    int nen = -1;
                    final int tuki = cal.get(Calendar.MONTH) + 1;
                    final int hi = cal.get(Calendar.DAY_OF_MONTH);

                    String nengoAlphabet = "";
                    for (final Iterator it = _nameMstL007.iterator(); it.hasNext();) {
                        final Map m = (Map) it.next();
                        final String namespare2 = (String) m.get("NAMESPARE2");
                        if (null != namespare2) {
                            final Calendar dcal = Calendar.getInstance();
                            dcal.setTime(df.parse(namespare2.replace('/', '-')));
                            if (dcal.before(cal)) {
                                nengoAlphabet = StringUtils.defaultString((String) m.get("ABBV1"), "　　");
                                nen = cal.get(Calendar.YEAR) - dcal.get(Calendar.YEAR) + 1;
                                break;
                            }
                        }
                    }
                    final DecimalFormat decfmt = new DecimalFormat("00");
                    return nengoAlphabet + decfmt.format(nen) + "." + decfmt.format(tuki) + "." + decfmt.format(hi);
                } catch (Exception e) {
                    log.error("format exception! date = " + date, e);
                }
            }
            return null;
        }

//        String schoolInfoVal(final String schoolKind, final String field) {
//            final Map map = new HashMap();
//            if ("H".equals(schoolKind)) {
//                map.put(SCHOOL_NAME1, _certifSchoolDatSchoolName); //学校名１
//                map.put(SCHOOL_NAME2, _certifSchoolDatSchoolName); //学校名２
//                map.put(PRINCIPAL_NAME, _certifSchoolDatPrincipalName); //校長名
//                map.put(PRINCIPAL_JOBNAME, _certifSchoolDatJobName);
//            } else if ("J".equals(schoolKind)) {
//                map.put(SCHOOL_NAME1, _certifSchoolDatRemark1); //学校名１
//                map.put(SCHOOL_NAME2, _certifSchoolDatRemark1); //学校名２
//                map.put(PRINCIPAL_NAME, _certifSchoolDatRemark2); //校長名
//                map.put(PRINCIPAL_JOBNAME, _certifSchoolDatRemark3);
//            } else if ("P".equals(schoolKind)) {
//                map.put(SCHOOL_NAME1, _certifSchoolDatRemark4); //学校名１
//                map.put(SCHOOL_NAME2, _certifSchoolDatRemark4); //学校名２
//                map.put(PRINCIPAL_NAME, _certifSchoolDatRemark5); //校長名
//                map.put(PRINCIPAL_JOBNAME, _certifSchoolDatRemark6);
//            }
//            if (null == map.get(SCHOOL_NAME1)) map.put(SCHOOL_NAME1, _schoolInfoVal.SCHOOL_NAME1); //学校名１
//            if (null == map.get(SCHOOL_NAME2)) map.put(SCHOOL_NAME2, _schoolInfoVal.SCHOOL_NAME2); //学校名２
//            if (null == map.get(PRINCIPAL_NAME)) map.put(PRINCIPAL_NAME, _schoolInfoVal.PRINCIPAL_NAME); //校長名
//            if (null == map.get(PRINCIPAL_JOBNAME)) map.put(PRINCIPAL_JOBNAME, _schoolInfoVal.PRINCIPAL_JOBNAME);
//            return (String) map.get(field);
//        }

        private void setSeirekiFlg(final DB2UDB db2) {
            try {
                _seirekiFlg = false;
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                while( rs.next() ){
                    if (rs.getString("NAME1").equals("2")) _seirekiFlg = true; //西暦
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
            }
        }

        public String changePrintDate(final DB2UDB db2, final String date) {
            if (null != date) {
                if (_seirekiFlg) {
                    return date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date);
                } else {
                    return KNJ_EditDate.h_format_JP(db2, date);
                }
            } else {
                return "";
            }
        }

        public String changePrintYear(final DB2UDB db2, final String year) {
            if (null == year) {
                return "";
            }
            if (_seirekiFlg) {
                return year + "年";
            } else {
                return KNJ_EditDate.gengou(db2, Integer.parseInt(year)) + "年";
            }
        }

        private void setCertifSchoolDat(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT * ");
                stb.append(" FROM CERTIF_SCHOOL_DAT T1 ");
                stb.append(" WHERE CERTIF_KINDCD = '125' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    _certifSchoolDatSchoolName = rs.getString("SCHOOL_NAME");
                    _certifSchoolDatJobName = rs.getString("JOB_NAME");
                    _certifSchoolDatPrincipalName = rs.getString("PRINCIPAL_NAME");
                    _certifSchoolDatRemark1 = rs.getString("REMARK1");
                    _certifSchoolDatRemark2 = rs.getString("REMARK2");
                    _certifSchoolDatRemark3 = rs.getString("REMARK3");
                    _certifSchoolDatRemark4 = rs.getString("REMARK4");
                    _certifSchoolDatRemark5 = rs.getString("REMARK5");
                    _certifSchoolDatRemark6 = rs.getString("REMARK6");
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

// eof

