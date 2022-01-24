// kanji=漢字
/*
 * $Id: 3d7ad224bebb302ea548108ea8f3903cfb1e8623 $
 *
 */
package servletpack.KNJC;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/*
 *  学校教育システム 賢者 [出欠管理] 出欠集計表
 */

public class KNJC161 {

    private static final Log log = LogFactory.getLog(KNJC161.class);

    private boolean _hasdata = false;

    public void svf_out (
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {

        Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）

        try {
            response.setContentType("application/pdf");
            svf.VrInit();                             //クラスの初期化
            svf.VrSetSpoolFileStream(response.getOutputStream());         //PDFファイル名の設定
        } catch (java.io.IOException ex) {
            log.error("svf instancing exception! ", ex);
        }

        // ＤＢ接続
        DB2UDB db2 = null;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);    //Databaseクラスを継承したクラス
            db2.open();
        } catch (Exception ex) {
            log.error("db2 instancing exception! ", ex);
            return;
        }

        _hasdata = false;
        Param param = null;
        try {
            log.debug(" $Revision: 71434 $ $Date: 2019-12-25 11:14:22 +0900 (水, 25 12 2019) $ ");
            KNJServletUtils.debugParam(request, log);
            param = new Param(request, db2);

            // 印刷処理
            svfPrint(db2, param, svf);

        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            if (null != param) {
                DbUtils.closeQuietly(param._sqlAttendance);
            }
            // 終了処理
            if (!_hasdata) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            svf.VrQuit();
        }
    }

    /**
     *  印刷処理
     */
    private void svfPrint(
            final DB2UDB db2,
            final Param param,
            final Vrw32alp svf
    ) {
        final List grades = new ArrayList(); // 学年のリスト
        final Map gradeClassMap = new TreeMap(); // 学年ごとのHRクラスのリストマップ

        //学籍のSQL
        setHrClasses(db2, param, grades, gradeClassMap);


        if ("3".equals(param._outputSelect)) {
            final String form = "KNJC161_3.frm";

            final List hrClassesAll = new ArrayList();
            for(final Iterator it = grades.iterator(); it.hasNext();) {
                final String grade = (String) it.next();
                final List hrClasses = (List) gradeClassMap.get(grade);
                hrClassesAll.addAll(hrClasses);
            }

            final List pageList = getPageList(hrClassesAll, 30);
            for (final Iterator it = pageList.iterator(); it.hasNext();) {
                final List hrClasses = (List) it.next();
                svf.VrSetForm(form, 4);

                svf.VrsOut("NENDO", KNJ_EditDate.gengou(db2, Integer.parseInt(param._year)) + "年度"); // 年度
                svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, param._loginDate)); // 作成日

                for (final Iterator hrit = hrClasses.iterator(); hrit.hasNext();) {
                    final HrClass hrClass = (HrClass) hrit.next();

                    svf.VrsOut("HR_NAME", hrClass._hrname); // クラス名
                    final AttendanceCount total = new AttendanceCount();
                    for (final Iterator mit = param._monthList.iterator(); mit.hasNext();) {
                        final String month = (String) mit.next();
                        final String i = String.valueOf(Integer.parseInt(month));
                        final AttendanceCount a = (AttendanceCount) hrClass._hrClassAttendMonthMap.get(month);
                        if (null == a) {
                            continue;
                        }

                        svf.VrsOut("SICK" + i, String.valueOf(a._sick)); // 欠席
                        svf.VrsOut("LATE" + i, String.valueOf(a._late)); // 遅刻
                        svf.VrsOut("EARLY" + i, String.valueOf(a._early)); // 早退
                        total.addAttend(a);
                    }

                    svf.VrsOut("TOTAL_SICK", String.valueOf(total._sick)); // 欠席
                    svf.VrsOut("TOTAL_LATE", String.valueOf(total._late)); // 遅刻
                    svf.VrsOut("TOTAL_EARLY", String.valueOf(total._early)); // 早退

                    svf.VrEndRecord();
                    _hasdata = true;
                }
            }

        } else if ("2".equals(param._outputSelect)) {
            final String form = "KNJC161_2.frm";

            for(final Iterator it = grades.iterator(); it.hasNext();) {
                final String grade = (String) it.next();
                final List hrClasses = (List) gradeClassMap.get(grade);
                for (final Iterator hrit = hrClasses.iterator(); hrit.hasNext();) {
                    final HrClass hrClass = (HrClass) hrit.next();

                    svf.VrSetForm(form, 1);
                    svf.VrsOut("NENDO", KNJ_EditDate.gengou(db2, Integer.parseInt(param._year)) + "年度");
                    svf.VrsOut("MONTH", param._month);
                    svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, param._loginDate));
                    svf.VrsOut("TOTAL_DATE", KNJ_EditDate.h_format_JP(db2, param._date));
                    svf.VrsOut("HR_NAME", hrClass._hrname);

                    for (int i = 0; i < hrClass._studentList.size(); i++) {
                        final Map schregMap = (Map) hrClass._studentList.get(i);
                        final int line = i + 1;

                        final String attendno = (String) schregMap.get("ATTENDNO");
                        final String name = (String) schregMap.get("NAME");
                        final AttendanceCount hrClassAttend = (AttendanceCount) schregMap.get("HR_CLASS_ATTEND");
                        final AttendanceCount hrClassAttendMonth = (AttendanceCount) schregMap.get("HR_CLASS_ATTEND_MONTH");

                        svf.VrsOutn("ATTENDNO", line, NumberUtils.isDigits(attendno) ? String.valueOf(Integer.parseInt(attendno)) : attendno);
                        svf.VrsOutn("NAME", line, name);

                        printAttendanceCount(svf, param, true, hrClassAttend, line);
                        printAttendanceCount(svf, param, false, hrClassAttendMonth, line);

                    }
                    _hasdata = true;
                    svf.VrEndPage();
                }
            }

        } else {
            final String form = "KNJC161.frm";
            svf.VrSetForm(form, 4);
            svf.VrsOut("NENDO", KNJ_EditDate.gengou(db2, Integer.parseInt(param._year)) + "年度");
            svf.VrsOut("MONTH", param._month);
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, param._loginDate));
            svf.VrsOut("TOTAL_DATE", KNJ_EditDate.h_format_JP(db2, param._date));

            final AttendanceCount totalAttend = new AttendanceCount(true);
            final AttendanceCount totalAttendMonth = new AttendanceCount(true);

            for(final Iterator it = grades.iterator(); it.hasNext();) {
                final String grade = (String) it.next();
                final List hrClasses = (List) gradeClassMap.get(grade);
                svfPrintGrade(svf, param, grade, hrClasses, totalAttend, totalAttendMonth);
                svf.VrsOut("HR_NAME", "\n");
                svf.VrEndRecord();
                _hasdata = true;
            }

            if (_hasdata) {
                svf.VrsOut("HR_NAME", "総合計");
                printAttendanceCount(svf, param, true, totalAttend, 0);
                printAttendanceCount(svf, param, false, totalAttendMonth, 0);
                svf.VrEndRecord();
            }
        }
    }

    private static List getPageList(final List list, final int max) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= max) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }

    private static void setHrClasses(final DB2UDB db2, final Param param, final List grades, final Map gradeClassMap) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        final Map gradeHrClassMap = new HashMap();
        try {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.GRADE, ");
            stb.append("     GD.GRADE_NAME3 AS GRADENAME, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.HR_NAME ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_HDAT T1 ");
            stb.append("     INNER JOIN SCHREG_REGD_GDAT GD ON GD.YEAR = T1.YEAR ");
            stb.append("           AND GD.GRADE = T1.GRADE ");
            stb.append("           AND GD.SCHOOL_KIND = '" + param._schoolKind + "' ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS ");
            final String sqlRegdH = stb.toString();

            ps = db2.prepareStatement(sqlRegdH);
            rs = ps.executeQuery();

            while(rs.next()) {
                final String grade = rs.getString("GRADE");
                List hrClasses = null;
                for(final Iterator it = grades.iterator(); it.hasNext(); ) {
                    final String grade1 = (String) it.next();
                    if (grade1.equals(grade)) {
                        hrClasses = (List) gradeClassMap.get(grade1);
                        break;
                    }
                }
                if (hrClasses == null) {
                    grades.add(grade);
                    hrClasses = new ArrayList();
                    gradeClassMap.put(grade, hrClasses);
                }

                final HrClass hrClass = new HrClass(rs.getString("GRADE"), rs.getString("GRADENAME"), rs.getString("HR_CLASS"), rs.getString("HR_NAME"));

                hrClasses.add(hrClass);
                gradeHrClassMap.put(rs.getString("GRADE") + rs.getString("HR_CLASS"), hrClass);
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        }

        if ("2".equals(param._outputSelect) || (param._isS_Sakae && "1".equals(param._outputSelect))) {

            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.ATTENDNO, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.COURSECD || T1.MAJORCD AS COURSEMAJOR, ");
            stb.append("     MM.MAJORNAME, ");
            stb.append("     T2.NAME ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1 ");
            stb.append("     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("     INNER JOIN SCHREG_REGD_GDAT GD ON GD.YEAR = T1.YEAR ");
            stb.append("           AND GD.GRADE = T1.GRADE ");
            stb.append("           AND GD.SCHOOL_KIND = '" + param._schoolKind + "' ");
            stb.append("     LEFT JOIN MAJOR_MST MM ON MM.COURSECD = T1.COURSECD AND MM.MAJORCD = T1.MAJORCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.ATTENDNO ");
            final String sql = stb.toString();

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while(rs.next()) {
                    final HrClass hrClass = (HrClass) gradeHrClassMap.get(rs.getString("GRADE") + rs.getString("HR_CLASS"));
                    if (null == hrClass) {
                        continue;
                    }
                    final Map schregnoMap = new HashMap();
                    schregnoMap.put("SCHREGNO", rs.getString("SCHREGNO"));
                    schregnoMap.put("ATTENDNO", rs.getString("ATTENDNO"));
                    schregnoMap.put("NAME", rs.getString("NAME"));
                    schregnoMap.put("COURSEMAJOR", rs.getString("COURSEMAJOR"));
                    schregnoMap.put("MAJORNAME", rs.getString("MAJORNAME"));
                    hrClass._studentList.add(schregnoMap);
                    hrClass._studentMap.put(rs.getString("SCHREGNO"), schregnoMap);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            }
        }

        if ("3".equals(param._outputSelect)) {

            loadMonths(db2, param, grades, gradeClassMap);

        } else {
            for(final Iterator it = grades.iterator(); it.hasNext();) {
                final String grade = (String) it.next();
                final List hrClasses = (List) gradeClassMap.get(grade);
                load(db2, param, grade, hrClasses);
            }
        }
    }

    /**
     *  印刷処理 メイン出力
     *
     */
    private void svfPrintGrade(final Vrw32alp svf, final Param param, final String grade, final List hrClasses, final AttendanceCount totalAttend, final AttendanceCount totalAttendMonth) {

    	String gradeName = "";
        for (final Iterator it = hrClasses.iterator(); it.hasNext();) {
            final HrClass hrClass = (HrClass) it.next();
            if (param._isS_Sakae) {
                gradeName = StringUtils.defaultString(hrClass._gradeName, "");
            }

            svf.VrsOut("HR_NAME", hrClass._hrname);
            // 年間 //
            printAttendanceCount(svf, param, true, hrClass._hrClassAttend, 0);
            log.debug(grade + hrClass._hrClass + " (年), " + hrClass._hrClassAttend);

            printAttendanceCount(svf, param, false, hrClass._hrClassAttendMonth, 0);
            log.debug(grade + hrClass._hrClass + " (月), " + hrClass._hrClassAttendMonth);

            // 改行
            svf.VrEndRecord();
        }

        if (param._isS_Sakae) {
            svf.VrsOut("HR_NAME", gradeName + "計");
        } else {
            final String gradestr = StringUtils.isNumeric(grade) ? Integer.valueOf(grade).toString() : grade;
            svf.VrsOut("HR_NAME", gradestr + "年計");
        }

        final AttendanceCount gradeTotalAttend = new AttendanceCount(true);
        final AttendanceCount gradeTotalAttendMonth = new AttendanceCount(true);

        final Map majorTotalAttend = new TreeMap();
        final Map majorTotalAttendMonth = new TreeMap();

        for (final Iterator it = hrClasses.iterator(); it.hasNext();) {
            final HrClass hrClass = (HrClass) it.next();
            // 年間 //
            gradeTotalAttend.addAttend(hrClass._hrClassAttend);
            gradeTotalAttendMonth.addAttend(hrClass._hrClassAttendMonth);

            if (param._isS_Sakae) {
                //学科別集計
	            for (int i = 0; i < hrClass._studentList.size(); i++) {
	                final Map schregMap = (Map) hrClass._studentList.get(i);

	                final String courseMajor = (String) schregMap.get("COURSEMAJOR");
	                final String MajorName = StringUtils.defaultString((String)schregMap.get("MAJORNAME"), "");
	                final AttendanceCount hrClassAttend = (AttendanceCount) schregMap.get("HR_CLASS_ATTEND");
	                final AttendanceCount hrClassAttendMonth = (AttendanceCount) schregMap.get("HR_CLASS_ATTEND_MONTH");
	                AttendanceCount majorTotalAtt = null;
	                AttendanceCount majorTotalAttMonth = null;
	                if (!majorTotalAttend.containsKey(courseMajor)) {
	                    majorTotalAtt = new AttendanceCount(true);
	                    majorTotalAtt.setMajorName(MajorName);
	                    majorTotalAttend.put(courseMajor, majorTotalAtt);
	                    majorTotalAttMonth = new AttendanceCount(true);
	                    majorTotalAttMonth.setMajorName(MajorName);
	                    majorTotalAttendMonth.put(courseMajor, majorTotalAttMonth);
	                } else {
	                    majorTotalAtt = (AttendanceCount)majorTotalAttend.get(courseMajor);
	                    majorTotalAttMonth = (AttendanceCount)majorTotalAttendMonth.get(courseMajor);
	                }
	                majorTotalAtt.addAttend(hrClassAttend);
	                majorTotalAttMonth.addAttend(hrClassAttendMonth);
	            }
            }
        }

        printAttendanceCount(svf, param, true, gradeTotalAttend, 0);
        log.debug(grade + "学年 total      = " + gradeTotalAttend);

        printAttendanceCount(svf, param, false, gradeTotalAttendMonth, 0);
        log.debug(grade + "学年 totalMonth = " + gradeTotalAttendMonth);

        svf.VrEndRecord();

        if (param._isS_Sakae && majorTotalAttend.size() > 1) {
        	for (Iterator ite = majorTotalAttend.keySet().iterator();ite.hasNext();) {
        		String kStr = (String)ite.next();
        		AttendanceCount outwk1 = (AttendanceCount)majorTotalAttend.get(kStr);
        		AttendanceCount outwk2 = (AttendanceCount)majorTotalAttendMonth.get(kStr);
                final String outStr = (!"".equals(outwk1._majorName) ? gradeName + outwk1._majorName : "") + "計";
                final int olen = KNJ_EditEdit.getMS932ByteLength(outStr);
                final String ofiled = olen > 10 ? "2" : "";
     		    svf.VrsOut("HR_NAME" + ofiled, outStr);
                printAttendanceCount(svf, param, true, outwk1, 0);
                printAttendanceCount(svf, param, false, outwk2, 0);
                svf.VrEndRecord();
        	}
        }
        totalAttend.addAttend(gradeTotalAttend);
        totalAttendMonth.addAttend(gradeTotalAttendMonth);
    }

    private static void load(final DB2UDB db2, final Param param, final String grade, final List hrClasses) {
        PreparedStatement ps = null;
        try {
            final String gradeSdate = (String) param._sDateMap.get(grade);
            param._attendParamMap2.put("grade", grade);
            final String sql = AttendAccumulate.getAttendSemesSql(
                    param._year,
                    param._semester,
                    null != gradeSdate && param._sDateMonth.compareTo(gradeSdate) < 0 ? gradeSdate : param._sDateMonth,
                    param._date,
                    param._attendParamMap2
            );
            ps = db2.prepareStatement(sql);

            for (final Iterator it = hrClasses.iterator(); it.hasNext();) {
                final HrClass hrClass = (HrClass) it.next();

                int pi;
                pi = 0;
                param._sqlAttendance.setString(++pi, grade);
                param._sqlAttendance.setString(++pi, hrClass._hrClass);
                hrClass._hrClassAttend = getHrClassAttend(param, param._sqlAttendance, hrClass, "HR_CLASS_ATTEND");
                pi = 0;
                ps.setString(++pi, hrClass._hrClass);
                hrClass._hrClassAttendMonth = getHrClassAttend(param, ps, hrClass, "HR_CLASS_ATTEND_MONTH");
            }

        } catch (Exception ex) {
            log.error("svfPrintGrade exception!", ex);
        } finally {
            DbUtils.closeQuietly(ps);
            db2.commit();
        }
    }


    private static void loadMonths(final DB2UDB db2, final Param param, final List grades, final Map gradeClassMap) {
        PreparedStatement ps = null;
        try {
            final Calendar dcal = Calendar.getInstance();
            dcal.setTime(Date.valueOf(param._date));
            final DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            final boolean isKindai = "KINDAI".equals(param._z010);

            for (final Iterator itm = param._monthList.iterator(); itm.hasNext();) {
                final String month = (String) itm.next();

                final String nen = String.valueOf(Integer.parseInt(param._year) + (Integer.parseInt(month) <= 3 ? 1 : 0));
                final String monthStartDate = nen + "-" + month + (isKindai ? "-02" : "-01");

                final Calendar endCal = Calendar.getInstance();
                endCal.setTime(Date.valueOf(monthStartDate));
                endCal.add(Calendar.MONTH, 1);
                endCal.add(Calendar.DAY_OF_MONTH, -1);

                final String endDate = sdf.format((dcal.before(endCal) ? dcal : endCal).getTime());
                log.debug(" month = " + month + ", " + monthStartDate + " - " + endDate);

                final String sql = AttendAccumulate.getAttendSemesSql(
                        param._year,
                        param._semester,
                        monthStartDate,
                        endDate,
                        param._attendParamMapMonth
                        );
                ps = db2.prepareStatement(sql);

                for(final Iterator it = grades.iterator(); it.hasNext();) {
                    final String grade = (String) it.next();
                    final List hrClasses = (List) gradeClassMap.get(grade);

                    for (final Iterator hrit = hrClasses.iterator(); hrit.hasNext();) {
                        final HrClass hrClass = (HrClass) hrit.next();

                        int pi = 0;
                        ps.setString(++pi, grade);
                        ps.setString(++pi, hrClass._hrClass);
                        hrClass._hrClassAttendMonthMap.put(month, getHrClassAttend(param, ps, hrClass, "HR_CLASS_ATTEND_MONTH"));
                    }
                }
            }

        } catch (Exception ex) {
            log.error("svfPrintGrade exception!", ex);
        } finally {
            DbUtils.closeQuietly(ps);
            db2.commit();
        }
    }

    private static AttendanceCount getHrClassAttend(final Param param, PreparedStatement ps, final HrClass hrClass, final String key) throws SQLException {
        ResultSet rs = null;
        final AttendanceCount total = new AttendanceCount(false);
        try {
            rs = ps.executeQuery();
            while(rs.next()) {
                if (!"9".equals(rs.getString("SEMESTER"))) {
                    continue;
                }
                if ("2".equals(param._outputSelect) || (param._isS_Sakae && "1".equals(param._outputSelect))) {
                    final Map schregMap = (Map) hrClass._studentMap.get(rs.getString("SCHREGNO"));
                    if (null == schregMap) {
                        log.info(" not in " + hrClass._hrClass + ", schregno = " + rs.getString("SCHREGNO"));
                        continue;
                    }
                    final AttendanceCount attendanceStudent = new AttendanceCount(false);
                    attendanceStudent.addAttend(rs);
                    schregMap.put(key, attendanceStudent);
                }
                total.addAttend(rs);
            }
        } finally {
            DbUtils.closeQuietly(rs);
        }
        return total;
    }

    /** svfへ出力する
     * @param svf svf
     * @param isYear 年計か
     */
    private void printAttendanceCount(final Vrw32alp svf, final Param param, final boolean isYear, final AttendanceCount a, final int line) {
        // 出席率 = 100.0 * 出席日数 / 授業日数
        if (null == a) {
            return;
        }
        final String percentage;
        if (a._mlesson == 0) {
            percentage = "0.0";
        } else {
            percentage = new BigDecimal(100 * a._attend).divide(new BigDecimal(a._mlesson), 1, BigDecimal.ROUND_HALF_UP).toString();
        }
        a._attendancePercentage = percentage;

        String totalStr = isYear ? "TOTAL_" : "";
        if ("2".equals(param._outputSelect)) {
            if (!a._isTotal) {
                svf.VrsOutn(totalStr + "LESSON", line, String.valueOf(a._displayLesson));
            }
            svf.VrsOutn(totalStr + "SUSPEND", line, String.valueOf(a._suspend + a._mourning));
            svf.VrsOutn(totalStr + "ABROAD", line, String.valueOf(a._abroad));
            svf.VrsOutn(totalStr + "REQ_ATTEND", line, String.valueOf(a._mlesson));
            svf.VrsOutn(totalStr + "SICK", line, String.valueOf(a._sick));
            svf.VrsOutn(totalStr + "ATTEND", line, String.valueOf(a._attend));
            svf.VrsOutn(totalStr + "LATE", line, String.valueOf(a._late));
            svf.VrsOutn(totalStr + "EARLY", line, String.valueOf(a._early));
        } else {
            if (!a._isTotal) {
                svf.VrsOut(totalStr + "LESSON", String.valueOf(a._displayLesson));
            }
            svf.VrsOut(totalStr + "MOURNING", String.valueOf(a._mourning));
            svf.VrsOut(totalStr + "SUSPEND", String.valueOf(a._suspend));
            svf.VrsOut(totalStr + "ATTEND", String.valueOf(a._attend));
            svf.VrsOut(totalStr + "SICK", String.valueOf(a._sick));
            svf.VrsOut(totalStr + "LATE", String.valueOf(a._late));
            svf.VrsOut(totalStr + "EARLY", String.valueOf(a._early));
            svf.VrsOut(totalStr + "ABSENT", String.valueOf(a._absent));
            svf.VrsOut(totalStr + "PERCENTAGE", a._attendancePercentage);
        }
    }

    private static class Param {
        final String _year;
        final String _semester;
        String _sDateMonth;
        final String _date;
        final String _loginDate;
        final String _schoolKind;
        final String _month;
        final String _outputSelect;
        final List _monthList = new ArrayList();
        final String _z010;
        final boolean _isS_Sakae;

        private Map _sDateMap;

        private PreparedStatement _sqlAttendance;

        private Map _attendParamMap;
        private Map _attendParamMap2;
        private Map _attendParamMapMonth;

        Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("YEAR");
            _semester  = request.getParameter("SEMESTER");
            _date = StringUtils.replace(request.getParameter("DATE"), "/", "-");
            _loginDate = request.getParameter("LOGIN_DATE");
            _schoolKind = request.getParameter("SCHOOL_KIND");

            Calendar cal = Calendar.getInstance();
            cal.setTime(Date.valueOf(_date));
            final int month = cal.get(Calendar.MONTH) + 1;
            _month = String.valueOf(month);
            _outputSelect = request.getParameter("OUTPUT_SELECT");
            _z010 = getZ010(db2, _year);
            _isS_Sakae = "sakae".equals(_z010) ? true : false;

            final DecimalFormat df2 = new DecimalFormat("00");
            if ("3".equals(_outputSelect)) {
                _attendParamMapMonth = new HashMap();
                _attendParamMapMonth.put("DB2UDB", db2);
                _attendParamMapMonth.put("HttpServletRequest", request);
                _attendParamMapMonth.put("grade", "?");
                _attendParamMapMonth.put("hrClass", "?");

                for (int m = 4; m <= month + (month <= 3 ? 12 : 0); m++) {
                    _monthList.add(df2.format(m - (m > 12 ? 12 : 0)));
                }
            } else {
                final int year = cal.get(Calendar.YEAR);
                _sDateMonth = StringUtils.replace(request.getParameter("STRT_DATE"), "/", "-");

                _attendParamMap = new HashMap();
                _attendParamMap.put("DB2UDB", db2);
                _attendParamMap.put("HttpServletRequest", request);
                _attendParamMap.put("grade", "?");
                _attendParamMap.put("hrClass", "?");

                _attendParamMap2 = new HashMap();
                _attendParamMap2.put("DB2UDB", db2);
                _attendParamMap2.put("HttpServletRequest", request);
                _attendParamMap2.put("hrClass", "?");

                try {
                    _sDateMap = getYearStartDateMap(db2);
                    String sql;

                    // 年間
                    sql = AttendAccumulate.getAttendSemesSql(
                            _year,
                            _semester,
                            null,
                            _date,
                            _attendParamMap
                            );
                    _sqlAttendance = db2.prepareStatement(sql);

                } catch (SQLException ex) {
                    log.error("parameter load exception!", ex);
                }
            }
        }

        private Map getYearStartDateMap(DB2UDB db2) {
            final Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            String sql = "SELECT GRADE, SDATE FROM V_SEMESTER_GRADE_MST WHERE SEMESTER = '9' AND YEAR = '" + _year + "' ";
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn.put(rs.getString("GRADE"), rs.getString("SDATE"));
                }
            } catch (SQLException ex) {
                log.error("getYearStartDate exception!" , ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }


        private String getZ010(final DB2UDB db2, final String year) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String val = null;
            try {
                ps = db2.prepareStatement(sqlNameMst(year, "Z010", "00"));
                rs = ps.executeQuery();
                if (rs.next()) {
                    val = rs.getString("NAME1");
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return val;
        }

        private String sqlNameMst(final String year, final String namecd1, final String namecd2) {
            final String sql = " SELECT "
                + "     * "
                + " FROM "
                + "     V_NAME_MST "
                + " WHERE "
                + "         YEAR = '" + year + "' "
                + "     AND NAMECD1 = '" + namecd1 + "' "
                + "     AND NAMECD2 = '" + namecd2 + "'";
            return sql;
        }
    }

    /** 出欠カウント */
    private static class AttendanceCount {
        /** 表示する授業日数(各クラスごとの生徒の授業日数の最大値) */
        private int _displayLesson;
        /** 授業日数 */
        private int _lesson;
        /** 出席すべき日数 */
        private int _mlesson;
        /** 出席日数 */
        private int _attend;
        /** 欠席日数 */
        private int _sick;
        /** 遅刻日数 */
        private int _late;
        /** 早退日数 */
        private int _early;
        /** 忌引日数 */
        private int _mourning;
        /** 出停日数 */
        private int _suspend;
        /** 公欠日数 */
        private int _absent;
        /** 留学日数 */
        private int _abroad;
        /** 休学日数 */
        private int _offdays;
        /** 出欠率 */
        private String _attendancePercentage;
        /** 学年計・総合計か */
        private boolean _isTotal;
        /** 学科名称 */
        private String _majorName;  //学科毎に集計する際のみ、設定/利用

        /**
         * コンストラクタ
         * @param isTotal 学年計・総合計か
         */
        public AttendanceCount(boolean isTotal) {
            _isTotal = isTotal;
        }

        public AttendanceCount() {
        }

        public String toString() {
            DecimalFormat df5 = new DecimalFormat("00000");
            return
            "LESSON=" + df5.format(_lesson)
            + ", MLESSON=" + df5.format(_mlesson)
            + ", ATTEND=" + df5.format(_attend)
            + ", SICK=" + df5.format(_sick)
            + ", LATE=" + df5.format(_late )
            + ", EARLY=" + df5.format(_early)
            + ", MOURNING=" + df5.format(_mourning)
            + ", SUSPEND=" + df5.format(_suspend)
            + ", ABSENT=" + df5.format(_absent)
            + ", ABROAD=" + df5.format(_abroad)
            + ", OFFDAYS=" + df5.format(_offdays)
            + ", percentage=" + _attendancePercentage;
        }

        public void addAttend(ResultSet rs) throws SQLException {
            int lesson = rs.getInt("LESSON"); // 授業日数
            int offdays = rs.getInt("OFFDAYS"); // 休学日数
            int sick = rs.getInt("SICK"); // 欠席日数
            int suspend = rs.getInt("SUSPEND");
            suspend += rs.getInt("VIRUS");
            suspend += rs.getInt("KOUDOME");
            int special = rs.getInt("MOURNING") + suspend; // 特別欠席
            int mlesson = lesson - special; // 出席すべき日数
            _displayLesson = Math.max(lesson, _displayLesson);
            _lesson += lesson;
            _mlesson += mlesson;
            _sick += sick;
            _attend += mlesson - sick; // 出席日数 = 出席すべき日数 - 欠席日数
            _late += rs.getInt("LATE");
            _early += rs.getInt("EARLY");
            _mourning += rs.getInt("MOURNING");
            _suspend += suspend;
            _absent += rs.getInt("ABSENT");
            _abroad += rs.getInt("TRANSFER_DATE");
            _offdays += offdays;
        }

        /** 出欠カウントを追加する */
        public void addAttend(AttendanceCount ac) {
            if (null == ac) {
                return;
            }
            _lesson += ac._lesson;
            _mlesson += ac._mlesson;
            _attend += ac._attend;
            _sick += ac._sick;
            _late += ac._late;
            _early += ac._early;
            _mourning += ac._mourning;
            _suspend += ac._suspend;
            _absent += ac._absent;
            _offdays += ac._offdays;
        }
        public void setMajorName(final String majorName) {
        	_majorName = majorName;
        }
    }

    private static class HrClass {
        final String _grade;
        final String _gradeName;
        final String _hrClass;
        final String _hrname;
        final List _studentList = new ArrayList();
        final Map _studentMap = new HashMap();
        AttendanceCount _hrClassAttend = null;
        AttendanceCount _hrClassAttendMonth = null;
        Map _hrClassAttendMonthMap = new HashMap();
        HrClass(final String grade,
        		final String gradeName,
                final String hrClass,
                final String hrname
                ) {
            _grade = grade;
            _gradeName = gradeName;
            _hrClass = hrClass;
            _hrname = hrname;
        }
    }
}
