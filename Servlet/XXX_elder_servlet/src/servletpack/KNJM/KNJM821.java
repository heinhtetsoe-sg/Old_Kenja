/*
 * $Id: f3dcb92ff81d6ec87bd307b4620d3a37abfbb741 $
 *
 * 作成日: 2012/11/27
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJM;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * 出席簿
 */
public class KNJM821 {

    private static final Log log = LogFactory.getLog(KNJM821.class);

    private boolean _hasData;

    private List _semesterlist;
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

            _semesterlist = null;
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

        final List<Hrclass> hrclassList = getHrclassList(db2);
        _semesterlist = getSemesterName(db2);

        for (final Hrclass hrclass : hrclassList) {

            if (_param._shuukei != null) {
                printShukei(hrclass, svf);
            }
            if (_param._meisai != null) {
                printMeisai(db2, hrclass, svf);
            }
        }
    }

    private List getSemesterName(final DB2UDB db2) {
    	List retlist = new ArrayList();

    	PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = sqlSemesterName();
//log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
            	retlist.add(rs.getString("SEMESTERNAME"));
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    	return retlist;
    }

    private List<Hrclass> getHrclassList(final DB2UDB db2) {
        final List<Hrclass> hrclassList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = sql();
            if (_param._isOutputDebug) {
            	log.info(" sql =" + sql);
            }
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {

                Hrclass hrclass = getHrclass(hrclassList, rs.getString("GRADE_HR_CLASS"));
                if (null == hrclass) {
                    hrclass = new Hrclass(rs.getString("GRADE_HR_CLASS"), rs.getString("HR_NAME"), rs.getString("STAFFNAME"));
                    hrclassList.add(hrclass);
                }
                Student student = getStudent(hrclass._studentList, rs.getString("SCHREGNO"));
                if (null == student) {
                    student = new Student(rs.getString("SCHREGNO"), rs.getString("ATTENDNO"), rs.getString("NAME"));
                    hrclass._studentList.add(student);
                }
                
                final AttendInfo attend = new AttendInfo(
                        rs.getString("EXECUTEDATE"),
                        rs.getString("SEMESTER"),
                        rs.getString("PERIODF"),
                        rs.getString("PERIODT"),
                        rs.getString("CLASSCD"),
                        rs.getString("CHAIRCD"),
                        rs.getString("SUBCLASSABBV"),
                        rs.getString("CREDIT_TIME"),
                        rs.getString("SCHOOLINGKINDCD"),
                        rs.getString("NAMESPARE1"),
                        rs.getString("M026_NAMESPARE1"),
                        rs.getString("M026_NAMESPARE2"),
                        rs.getString("M027_NAME1")
                );
                final String tablediv = rs.getString("TABLEDIV");
				if ("SCHOOLING".equals(tablediv)) {
                    student._schAttendDatList.add(attend);
                } else if ("SPECIAL".equals(tablediv)) {
                    student._specialactAttendDatList.add(attend);
                } else if ("TEST".equals(tablediv)) {
                    student._testAttendDatList.add(attend);
                }
            }

        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return hrclassList;
    }

    private static boolean between(final boolean nullequal, final String data, final String before, final String after) {
        return (nullequal && before == null && after == null) || before.compareTo(data) <= 0 && after.compareTo(data) >= 0;
    }

    private void printMeisai(final DB2UDB db2, final Hrclass hrclass, final Vrw32alp svf) {

        final TreeSet<String> adates = new TreeSet(); // 表示対象日付母集団
        for (int i = 0; i < hrclass._studentList.size(); i++) {
            final Student student = hrclass._studentList.get(i);

            for (final AttendInfo m : student._schAttendDatList) {
                if ("1".equals(m._namespare1) && null != m._executedate && between(false, m._executedate, _param._outputsdate, _param._outputedate)) {
                	if (!"1".equals(m._m026Namespare1)) {
                		adates.add(m._executedate);
                	}
                }
            }
            for (final AttendInfo m : student._specialactAttendDatList) {
                if (null != m._executedate && between(false, m._executedate, _param._outputsdate, _param._outputedate)) {
                	if (!"1".equals(m._m026Namespare1)) {
                		if (_param._isSagaken) {
                			if (null != m._m027Name1) {
                                adates.add(m._executedate);
                			}
                		} else {
                            adates.add(m._executedate);
                		}
                	}
                }
            }
            for (final AttendInfo m : student._testAttendDatList) {
                if (null != m._executedate && between(false, m._executedate, _param._outputsdate, _param._outputedate)) {
                	if (!"1".equals(m._m026Namespare1)) {
                		adates.add(m._executedate);
                	}
                }
            }
        }

        final TreeMap<Integer, TreeSet<String>> pageDates = new TreeMap();
        int ipage = 0;
        TreeSet current = null;
        for (final String date : adates) {
            if (null == current || current.size() >= 5) {
                ipage += 1;
                current = new TreeSet();
                pageDates.put(new Integer(ipage), current);
            }
            current.add(date);
        }

        final int periodPerDay = 8;
        if (pageDates.isEmpty()) {
            return;
        }

        svf.VrSetForm("KNJM821_2.frm", 1);
        final List<List<Student>> studentPageList = hrclass.getStudentPageList(50);
        final String totalPage = String.valueOf(pageDates.lastKey().intValue() * studentPageList.size());

        for (final Integer page : pageDates.keySet()) {
            final TreeSet<String> dates = pageDates.get(page);
            final boolean islastPage = pageDates.lastKey().equals(page); // 最後のページ

            for (int i = 0; i < studentPageList.size(); i++) {
                final List<Student> studentPage = studentPageList.get(i);

                int datec = 0;
                for (final String date : dates) {
                    svf.VrsOut("hymd" + (datec + 1), KNJ_EditDate.h_format_JP_MD(date) + "(" + KNJ_EditDate.h_format_W(date) + ")"); // 月日
                    svf.VrsOut("nendo", _param._nendo); // 年度
                    svf.VrsOut("PRINT", KNJ_EditDate.h_format_JP(db2, dates.first().toString()) + "〜" + KNJ_EditDate.h_format_JP(db2, dates.last().toString())); // 印刷範囲
                    svf.VrsOut("ymd1", _param._loginDateString); // 作成日
                    svf.VrsOut("TOTAL_PAGE", totalPage); // 総ページ数
                    svf.VrsOut("HR_NAME", hrclass._hrname); // 組氏名
                    svf.VrsOut("teacher", hrclass._staffname); // 担任

                    final Set<String> printedPeriodcd = new TreeSet<String>();
                    for (final String periodcd : _param._nameMstB001.keySet()) {
                    	printedPeriodcd.add(periodcd);
                        if (printedPeriodcd.size() > periodPerDay) {
                            continue;
                        }
                        final int periodline = datec * periodPerDay + printedPeriodcd.size();
                        svf.VrsOutn("KOUJI", periodline, _param._nameMstB001.get(periodcd)); // 校時名

                        for (final Student student : studentPage) {

                            final int iattendno = Integer.parseInt(student._attendno);
                            final int line = iattendno % 50 == 0 && iattendno > 0 ? 50 : iattendno % 50;
                            svf.VrsOutn("NUMBER", line, String.valueOf(iattendno)); // 番号
                            svf.VrsOutn("SCHREGNO", line, student._schregno); // 学籍番号
                            svf.VrsOutn("name", line, student._name); // 生徒氏名
                            svf.VrsOutn("SUBJECT" + periodline, line, student.getAttendName(date, periodcd)); // 出席
                            if (islastPage) {
                                svf.VrsOutn("TOTAL_OP_SCHOOL", line, student.getShukkouCount(_param, null, _param._outputsdate, _param._outputedate)); // 出校日数
                                svf.VrsOutn("TOTAL_SP_HR", line, student.getHrCount(_param, null, _param._outputsdate, _param._outputedate)); // HR
                                svf.VrsOutn("TOTAL_SP_EVENT", line, student.getGyoujiCount(_param, null, _param._outputsdate, _param._outputedate)); // 行事
                                svf.VrsOutn("TOTAL_SP", line, student.getTokkatsuCount(_param, null, _param._outputsdate, _param._outputedate)); // 計
                                svf.VrsOutn("NOTE", line, null); // 備考
                            }
                        }
                    }
                    final String periodcd = new DecimalFormat("00").format(periodPerDay);
                    final int periodline = datec * periodPerDay + Integer.parseInt(periodcd);
                    for (final Student student : studentPage) {

                        final int iattendno = Integer.parseInt(student._attendno);
                        final int line = iattendno % 50 == 0 && iattendno > 0 ? 50 : iattendno % 50;
                        // テストは校時がないため表示しない。代わりに８校時の欄に「考査」を表示する
                        if (student.isKousa(date)) {
                            svf.VrsOutn("SUBJECT" + periodline, line, "考査");
                        }
                    }
                    datec += 1;
                }
                svf.VrEndPage();
            }
            _hasData = true;
        }
    }

    private void printShukei(final Hrclass hrclass, final Vrw32alp svf) {
        if (_param._hirokofrmflg) {
    	    printShukei2(hrclass, svf);
        } else {
    	    printShukei1(hrclass, svf);
        }

    }

    private void printShukei1(final Hrclass hrclass, final Vrw32alp svf) {
	    svf.VrSetForm("KNJM821_1.frm", 1);
        final String[] semesters = new String[]{"1", "2"};
        final Map totalCount = new HashMap();
        final List<List<Student>> studentPageList = hrclass.getStudentPageList(50);
        for (int i = 0; i < studentPageList.size(); i++) {
            final List<Student> studentPage = studentPageList.get(i);

            svf.VrsOut("SEMESTER1", "1学期"); // 学期
            svf.VrsOut("SEMESTER2", "2学期"); // 学期
            svf.VrsOut("SEMESTER3", "1〜2学期累計"); // 学期累計

            svf.VrsOut("nendo", _param._nendo); // 年度
            svf.VrsOut("HR_NAME", hrclass._hrname); // 組氏名
            svf.VrsOut("teacher", hrclass._staffname); // 担任
            svf.VrsOut("ymd1", _param._loginDateString); // 作成日

            for (final Student student : studentPage) {

                final int iattendno = Integer.parseInt(student._attendno);
                final int line = iattendno % 50 == 0 && iattendno > 0 ? 50 : iattendno % 50;
                svf.VrsOutn("NUMBER", line, String.valueOf(iattendno)); // 番号
                svf.VrsOutn("SCHREGNO", line, student._schregno); // 学籍番号
                svf.VrsOutn("name", line, student._name); // 生徒氏名

                String sumShukkou = null;
                String sumHr = null;
                String sumGyouji = null;
                String sumTokkatsu = null;
                for (int j = 0; j < semesters.length; j++) {
                    final String semester = semesters[j];
                    final String shukkouCount = student.getShukkouCount(_param, semester, null, null);
                    final String hrCount = student.getHrCount(_param, semester, null, null);
                    final String gyoujiCount = student.getGyoujiCount(_param, semester, null, null);
                    final String tokkatsuCount = student.getTokkatsuCount(_param, semester, null, null);
                    svf.VrsOutn("TOTAL_OP_SCHOOL" + semester, line, shukkouCount); // 出校日数合計
                    svf.VrsOutn("TOTAL_HR" + semester, line, hrCount); // HR合計
                    svf.VrsOutn("TOTAL_SP_EVENT" + semester, line, gyoujiCount); // 行事合計
                    svf.VrsOutn("TOTAL_SP" + semester, line, tokkatsuCount); // 特活合計
                    sumShukkou = add(sumShukkou, shukkouCount);
                    sumHr = add(sumHr, hrCount);
                    sumGyouji = add(sumGyouji, gyoujiCount);
                    sumTokkatsu = add(sumTokkatsu, tokkatsuCount);
                    totalCount.put("TOTAL_OP_SCHOOL" + semester, add(totalCount.get("TOTAL_OP_SCHOOL" + semester), shukkouCount));
                    totalCount.put("TOTAL_HR" + semester, add(totalCount.get("TOTAL_HR" + semester), hrCount));
                    totalCount.put("TOTAL_SP_EVENT" + semester, add(totalCount.get("TOTAL_SP_EVENT" + semester), gyoujiCount));
                    totalCount.put("TOTAL_SP" + semester, add(totalCount.get("TOTAL_SP" + semester), tokkatsuCount));
                }

                svf.VrsOutn("TOTAL_OP_SCHOOL3", line, sumShukkou); // 出校日数合計
                svf.VrsOutn("TOTAL_SP_HR3", line, sumHr); // HR合計
                svf.VrsOutn("TOTAL_SP_EVENT3", line, sumGyouji); // 行事合計
                svf.VrsOutn("TOTAL_SP3", line, sumTokkatsu); // 特活合計

                // svf.VrsOutn("REMARK", line, null); // 備考
            }

            if (studentPageList.size() - 1 == i) { // 最後のページ
                final int line = 51;
                String sumShukkou = null;
                String sumHr = null;
                String sumGyouji = null;
                String sumTokkatsu = null;
                for (int j = 0; j < semesters.length; j++) {
                    final String semester = semesters[j];
                    final String shukkouCount = (String) totalCount.get("TOTAL_OP_SCHOOL" + semester);
                    final String hrCount = (String) totalCount.get("TOTAL_HR" + semester);
                    final String gyoujiCount = (String) totalCount.get("TOTAL_SP_EVENT" + semester);
                    final String tokkatsuCount = (String) totalCount.get("TOTAL_SP" + semester);
                    svf.VrsOutn("TOTAL_OP_SCHOOL" + semester, line, shukkouCount); // 出校日数合計
                    svf.VrsOutn("TOTAL_HR" + semester, line, hrCount); // HR合計
                    svf.VrsOutn("TOTAL_SP_EVENT" + semester, line, gyoujiCount); // 行事合計
                    svf.VrsOutn("TOTAL_SP" + semester, line, tokkatsuCount); // 特活合計
                    sumShukkou = add(sumShukkou, shukkouCount);
                    sumHr = add(sumHr, hrCount);
                    sumGyouji = add(sumGyouji, gyoujiCount);
                    sumTokkatsu = add(sumTokkatsu, tokkatsuCount);
                }

                svf.VrsOutn("TOTAL_OP_SCHOOL3", line, sumShukkou); // 出校日数合計
                svf.VrsOutn("TOTAL_SP_HR3", line, sumHr); // HR合計
                svf.VrsOutn("TOTAL_SP_EVENT3", line, sumGyouji); // 行事合計
                svf.VrsOutn("TOTAL_SP3", line, sumTokkatsu); // 特活合計
            }
            svf.VrEndPage();
        }
        _hasData = true;
    }

    private void printShukei2(final Hrclass hrclass, final Vrw32alp svf) {
	    svf.VrSetForm("KNJM821_1_2.frm", 1);
        final String[] semesters = new String[]{"1", "2", "3", "4"};
        final Map totalCount = new HashMap();
        final List<List<Student>> studentPageList = hrclass.getStudentPageList(50);
        for (int i = 0; i < studentPageList.size(); i++) {
            final List<Student> studentPage = studentPageList.get(i);

            for (int cnt = 0;cnt < _semesterlist.size() && cnt < 4;cnt++) {
                svf.VrsOut("SEMESTER"+(cnt + 1), (String)_semesterlist.get(cnt)); // 学期
            }
            svf.VrsOut("SEMESTER5", "学期累計"); // 学期累計

            svf.VrsOut("nendo", _param._nendo); // 年度
            svf.VrsOut("HR_NAME", hrclass._hrname); // 組氏名
            svf.VrsOut("teacher", hrclass._staffname); // 担任
            svf.VrsOut("ymd1", _param._loginDateString); // 作成日

            for (final Student student : studentPage) {

                final int iattendno = Integer.parseInt(student._attendno);
                final int line = iattendno % 50 == 0 && iattendno > 0 ? 50 : iattendno % 50;
                svf.VrsOutn("NUMBER", line, String.valueOf(iattendno)); // 番号
                svf.VrsOutn("SCHREGNO", line, student._schregno); // 学籍番号
                svf.VrsOutn("name", line, student._name); // 生徒氏名

                String sumShukkou = null;
                String sumHr = null;
                String sumGyouji = null;
                String sumTokkatsu = null;
                for (int j = 0; j < semesters.length; j++) {
                    final String semester = semesters[j];
                    final String shukkouCount = student.getShukkouCount(_param, semester, null, null);
                    final String hrCount = student.getHrCount(_param, semester, null, null);
                    final String gyoujiCount = student.getGyoujiCount(_param, semester, null, null);
                    final String tokkatsuCount = student.getTokkatsuCount(_param, semester, null, null);
                    svf.VrsOutn("TOTAL_OP_SCHOOL" + semester, line, shukkouCount); // 出校日数合計
                    svf.VrsOutn("TOTAL_HR" + semester, line, hrCount); // HR合計
                    svf.VrsOutn("TOTAL_SP_EVENT" + semester, line, gyoujiCount); // 行事合計
                    svf.VrsOutn("TOTAL_SP" + semester, line, tokkatsuCount); // 特活合計
                    sumShukkou = add(sumShukkou, shukkouCount);
                    sumHr = add(sumHr, hrCount);
                    sumGyouji = add(sumGyouji, gyoujiCount);
                    sumTokkatsu = add(sumTokkatsu, tokkatsuCount);
                    totalCount.put("TOTAL_OP_SCHOOL" + semester, add(totalCount.get("TOTAL_OP_SCHOOL" + semester), shukkouCount));
                    totalCount.put("TOTAL_HR" + semester, add(totalCount.get("TOTAL_HR" + semester), hrCount));
                    totalCount.put("TOTAL_SP_EVENT" + semester, add(totalCount.get("TOTAL_SP_EVENT" + semester), gyoujiCount));
                    totalCount.put("TOTAL_SP" + semester, add(totalCount.get("TOTAL_SP" + semester), tokkatsuCount));
                }

                svf.VrsOutn("TOTAL_OP_SCHOOL5", line, sumShukkou); // 出校日数合計
                svf.VrsOutn("TOTAL_SP_HR5", line, sumHr); // HR合計
                svf.VrsOutn("TOTAL_SP_EVENT5", line, sumGyouji); // 行事合計
                svf.VrsOutn("TOTAL_SP5", line, sumTokkatsu); // 特活合計

                // svf.VrsOutn("REMARK", line, null); // 備考
            }

            if (studentPageList.size() - 1 == i) { // 最後のページ
                final int line = 51;
                String sumShukkou = null;
                String sumHr = null;
                String sumGyouji = null;
                String sumTokkatsu = null;
                for (int j = 0; j < semesters.length; j++) {
                    final String semester = semesters[j];
                    final String shukkouCount = (String) totalCount.get("TOTAL_OP_SCHOOL" + semester);
                    final String hrCount = (String) totalCount.get("TOTAL_HR" + semester);
                    final String gyoujiCount = (String) totalCount.get("TOTAL_SP_EVENT" + semester);
                    final String tokkatsuCount = (String) totalCount.get("TOTAL_SP" + semester);
                    svf.VrsOutn("TOTAL_OP_SCHOOL" + semester, line, shukkouCount); // 出校日数合計
                    svf.VrsOutn("TOTAL_HR" + semester, line, hrCount); // HR合計
                    svf.VrsOutn("TOTAL_SP_EVENT" + semester, line, gyoujiCount); // 行事合計
                    svf.VrsOutn("TOTAL_SP" + semester, line, tokkatsuCount); // 特活合計
                    sumShukkou = add(sumShukkou, shukkouCount);
                    sumHr = add(sumHr, hrCount);
                    sumGyouji = add(sumGyouji, gyoujiCount);
                    sumTokkatsu = add(sumTokkatsu, tokkatsuCount);
                }

                svf.VrsOutn("TOTAL_OP_SCHOOL5", line, sumShukkou); // 出校日数合計
                svf.VrsOutn("TOTAL_SP_HR5", line, sumHr); // HR合計
                svf.VrsOutn("TOTAL_SP_EVENT5", line, sumGyouji); // 行事合計
                svf.VrsOutn("TOTAL_SP5", line, sumTokkatsu); // 特活合計
            }
            svf.VrEndPage();
        }
        _hasData = true;
    }

    private static String add(final Object count1, final Object count2) {
        if (!NumberUtils.isNumber((String) count1) && !NumberUtils.isNumber((String) count2)) {
            return null;
        }
        final BigDecimal bd1 = NumberUtils.isNumber((String) count1) ? new BigDecimal((String) count1) : BigDecimal.valueOf(0);
        final BigDecimal bd2 = NumberUtils.isNumber((String) count2) ? new BigDecimal((String) count2) : BigDecimal.valueOf(0);
        return bd1.add(bd2).toString();
    }

    private String z010(final String name) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" select NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAME1 = '" + name + "'");
        return stb.toString();
    }

    private String sqlSemesterName() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" select SEMESTER, SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _param._year + "' AND SEMESTER <> '9' ORDER BY SEMESTER ");
        return stb.toString();
    }

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.GRADE || T1.HR_CLASS AS GRADE_HR_CLASS, ");
        stb.append("     T7.HR_NAME, ");
        stb.append("     T8.STAFFNAME, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T4.NAME, ");
        stb.append("     CASE WHEN T2.SCHREGNO IS NOT NULL THEN 'SCHOOLING' END AS TABLEDIV, ");
        stb.append("     T2.EXECUTEDATE, ");
        stb.append("     T5.SEMESTER, ");
        stb.append("     T2.PERIODCD AS PERIODF, ");
        stb.append("     T2.PERIODCD AS PERIODT, ");
        stb.append("     T3.CLASSCD, ");
        stb.append("     T2.CHAIRCD, ");
        stb.append("     T6.SUBCLASSABBV, ");
        stb.append("     T2.CREDIT_TIME, ");
        stb.append("     T2.SCHOOLINGKINDCD, ");
        stb.append("     T9.NAMESPARE1 ");
        stb.append("   , NM_M026.NAMESPARE1 AS M026_NAMESPARE1 ");
        stb.append("   , NM_M026.NAMESPARE2 AS M026_NAMESPARE2 ");
        stb.append("   , NM_M027.NAME1 AS M027_NAME1");
        stb.append(" FROM SCHREG_REGD_DAT T1 ");
        stb.append(" LEFT JOIN SCH_ATTEND_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     AND T2.YEAR = T1.YEAR ");
        stb.append(" LEFT JOIN SEMESTER_MST T5 ON T5.YEAR = T2.YEAR ");
        stb.append("     AND T5.SEMESTER <> '9' ");
        stb.append("     AND T2.EXECUTEDATE BETWEEN T5.SDATE AND T5.EDATE ");
        stb.append(" LEFT JOIN CHAIR_DAT T3 ON T3.CHAIRCD = T2.CHAIRCD ");
        stb.append("     AND T3.YEAR = T1.YEAR ");
        stb.append("     AND T3.SEMESTER = T5.SEMESTER ");
        stb.append(" INNER JOIN SCHREG_BASE_MST T4 ON T4.SCHREGNO = T1.SCHREGNO ");
        stb.append(" LEFT JOIN SUBCLASS_MST T6 ON T6.CLASSCD = T3.CLASSCD ");
        stb.append("     AND T6.SCHOOL_KIND = T3.SCHOOL_KIND ");
        stb.append("     AND T6.CURRICULUM_CD = T3.CURRICULUM_CD ");
        stb.append("     AND T6.SUBCLASSCD = T3.SUBCLASSCD ");
        stb.append(" LEFT JOIN SCHREG_REGD_HDAT T7 ON T7.YEAR = T1.YEAR ");
        stb.append("     AND T7.SEMESTER = T1.SEMESTER ");
        stb.append("     AND T7.GRADE = T1.GRADE ");
        stb.append("     AND T7.HR_CLASS = T1.HR_CLASS ");
        stb.append(" LEFT JOIN STAFF_MST T8 ON T8.STAFFCD = T7.TR_CD1 ");
        stb.append(" LEFT JOIN NAME_MST T9 ON T9.NAMECD1 = 'M001' ");
        stb.append("     AND T9.NAMECD2 = T2.SCHOOLINGKINDCD ");
        stb.append(" LEFT JOIN V_NAME_MST NM_M026 ON NM_M026.YEAR = T2.YEAR ");
        stb.append("     AND NM_M026.NAMECD1 = 'M026' ");
        stb.append("     AND NM_M026.NAME1 = T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD ");
        stb.append(" LEFT JOIN V_NAME_MST NM_M027 ON NM_M027.YEAR = T2.YEAR ");
        stb.append("     AND NM_M027.NAMECD1 = 'M027' ");
        stb.append("     AND NM_M027.NAME1 = T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.GRADE || T1.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categoryName) + " ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     T1.GRADE || T1.HR_CLASS AS GRADE_HR_CLASS, ");
        stb.append("     T7.HR_NAME, ");
        stb.append("     T8.STAFFNAME, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T4.NAME, ");
        stb.append("     CASE WHEN T2.SCHREGNO IS NOT NULL THEN 'SPECIAL' END AS TABLEDIV, ");
        stb.append("     T2.ATTENDDATE AS EXECUTEDATE, ");
        stb.append("     T2.SEMESTER, ");
        stb.append("     T2.PERIODF, ");
        stb.append("     T2.PERIODT, ");
        stb.append("     T2.CLASSCD, ");
        stb.append("     T2.CHAIRCD, ");
        stb.append("     T6.SUBCLASSABBV, ");
        stb.append("     T2.CREDIT_TIME, ");
        stb.append("     CAST(NULL AS VARCHAR(1)) AS SCHOOLINGKINDCD, ");
        stb.append("     CAST(NULL AS VARCHAR(1)) AS NAMESPARE1 ");
        stb.append("   , NM_M026.NAMESPARE1 AS M026_NAMESPARE1 ");
        stb.append("   , NM_M026.NAMESPARE2 AS M026_NAMESPARE2 ");
        stb.append("   , NM_M027.NAME1 AS M027_NAME1");
        stb.append(" FROM SCHREG_REGD_DAT T1 ");
        stb.append(" LEFT JOIN SPECIALACT_ATTEND_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     AND T2.YEAR = T1.YEAR ");
        stb.append(" INNER JOIN SCHREG_BASE_MST T4 ON T4.SCHREGNO = T1.SCHREGNO ");
        stb.append(" LEFT JOIN SUBCLASS_MST T6 ON T6.CLASSCD = T2.CLASSCD ");
        stb.append("     AND T6.SCHOOL_KIND = T2.SCHOOL_KIND ");
        stb.append("     AND T6.CURRICULUM_CD = T2.CURRICULUM_CD ");
        stb.append("     AND T6.SUBCLASSCD = T2.SUBCLASSCD ");
        stb.append(" LEFT JOIN SCHREG_REGD_HDAT T7 ON T7.YEAR = T1.YEAR ");
        stb.append("     AND T7.SEMESTER = T1.SEMESTER ");
        stb.append("     AND T7.GRADE = T1.GRADE ");
        stb.append("     AND T7.HR_CLASS = T1.HR_CLASS ");
        stb.append(" LEFT JOIN STAFF_MST T8 ON T8.STAFFCD = T7.TR_CD1 ");
        stb.append(" LEFT JOIN V_NAME_MST NM_M026 ON NM_M026.YEAR = T2.YEAR ");
        stb.append("     AND NM_M026.NAMECD1 = 'M026' ");
        stb.append("     AND NM_M026.NAME1 = T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD ");
        stb.append(" LEFT JOIN V_NAME_MST NM_M027 ON NM_M027.YEAR = T2.YEAR ");
        stb.append("     AND NM_M027.NAMECD1 = 'M027' ");
        stb.append("     AND NM_M027.NAME1 = T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.GRADE || T1.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categoryName) + " ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     T1.GRADE || T1.HR_CLASS AS GRADE_HR_CLASS, ");
        stb.append("     T7.HR_NAME, ");
        stb.append("     T8.STAFFNAME, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T4.NAME, ");
        stb.append("     CASE WHEN T2.SCHREGNO IS NOT NULL THEN 'TEST' END AS TABLEDIV, ");
        stb.append("     T2.INPUT_DATE AS EXECUTEDATE, ");
        stb.append("     T2.SEMESTER, ");
        stb.append("     CAST(NULL AS VARCHAR(1)) AS PERIODF, ");
        stb.append("     CAST(NULL AS VARCHAR(1)) AS PERIODT, ");
        stb.append("     T2.CLASSCD, ");
        stb.append("     CAST(NULL AS VARCHAR(7)) AS CHAIRCD, ");
        stb.append("     T6.SUBCLASSABBV, ");
        stb.append("     CAST(NULL AS DECIMAL(5, 1)) AS CREDIT_TIME, ");
        stb.append("     CAST(NULL AS VARCHAR(1)) AS SCHOOLINGKINDCD, ");
        stb.append("     CAST(NULL AS VARCHAR(1)) AS NAMESPARE1 ");
        stb.append("   , NM_M026.NAMESPARE1 AS M026_NAMESPARE1 ");
        stb.append("   , CAST(NULL AS VARCHAR(1)) AS M026_NAMESPARE2 ");
        stb.append("   , CAST(NULL AS VARCHAR(1)) AS M027_NAME1");
        stb.append(" FROM SCHREG_REGD_DAT T1 ");
        stb.append(" LEFT JOIN TEST_ATTEND_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     AND T2.YEAR = T1.YEAR ");
        stb.append(" INNER JOIN SCHREG_BASE_MST T4 ON T4.SCHREGNO = T1.SCHREGNO ");
        stb.append(" LEFT JOIN SUBCLASS_MST T6 ON T6.CLASSCD = T2.CLASSCD ");
        stb.append("     AND T6.SCHOOL_KIND = T2.SCHOOL_KIND ");
        stb.append("     AND T6.CURRICULUM_CD = T2.CURRICULUM_CD ");
        stb.append("     AND T6.SUBCLASSCD = T2.SUBCLASSCD ");
        stb.append(" LEFT JOIN SCHREG_REGD_HDAT T7 ON T7.YEAR = T1.YEAR ");
        stb.append("     AND T7.SEMESTER = T1.SEMESTER ");
        stb.append("     AND T7.GRADE = T1.GRADE ");
        stb.append("     AND T7.HR_CLASS = T1.HR_CLASS ");
        stb.append(" LEFT JOIN STAFF_MST T8 ON T8.STAFFCD = T7.TR_CD1 ");
        stb.append(" LEFT JOIN V_NAME_MST NM_M026 ON NM_M026.YEAR = T2.YEAR ");
        stb.append("     AND NM_M026.NAMECD1 = 'M026' ");
        stb.append("     AND NM_M026.NAME1 = T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD ");
        stb.append(" LEFT JOIN V_NAME_MST NM_M027 ON NM_M027.YEAR = T2.YEAR ");
        stb.append("     AND NM_M027.NAMECD1 = 'M027' ");
        stb.append("     AND NM_M027.NAME1 = T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.GRADE || T1.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categoryName) + " ");
        stb.append(" ORDER BY GRADE_HR_CLASS, ATTENDNO ");
        return stb.toString();
    }

    private Hrclass getHrclass(final List<Hrclass> hrclassList, final String gradeHrclass) {
        for (final Hrclass hrclass : hrclassList) {
            if (hrclass._gradeHrclass.equals(gradeHrclass)) {
                return hrclass;
            }
        }
        return null;
    }

    private Student getStudent(final List<Student> studentList, final String schregno) {
        for (final Student student : studentList) {
            if (student._schregno.equals(schregno)) {
                return student;
            }
        }
        return null;
    }

    private static class Hrclass {
        final String _gradeHrclass;
        final String _hrname;
        final String _staffname;
        final List<Student> _studentList = new ArrayList();

        Hrclass(
                final String gradeHrclass,
                final String hrname,
                final String staffname
        ) {
            _gradeHrclass = gradeHrclass;
            _hrname = hrname;
            _staffname = staffname;
        }

        List<List<Student>> getStudentPageList(final int count) {
            final List<List<Student>> list = new ArrayList();
            List<Student> current = null;
            for (final Student student : _studentList) {
                if (null == current || current.size() >= count) {
                    current = new ArrayList();
                    list.add(current);
                }
                current.add(student);
            }
            return list;
        }
    }

    private static class Student {
        final String _schregno;
        final String _attendno;
        final String _name;
        final List<AttendInfo> _schAttendDatList = new ArrayList();
        final List<AttendInfo> _specialactAttendDatList = new ArrayList();
        final List<AttendInfo> _testAttendDatList = new ArrayList();

        Student(
                final String schregno,
                final String attendno,
                final String name
        ) {
            _schregno = schregno;
            _attendno = attendno;
            _name = name;
        }

        public String getTokkatsuCount(final Param param, final String semester, final String sdate, final String edate) {
            return add(getHrCount(param, semester, sdate, edate), getGyoujiCount(param, semester, sdate, edate));
        }

        public String getGyoujiCount(final Param param, final String semester, final String sdate, final String edate) {
            return getSpecialactCount(param, "94", semester, sdate, edate);
        }

        public String getHrCount(final Param param, final String semester, final String sdate, final String edate) {
            return getSpecialactCount(param, "93", semester, sdate, edate);
        }

        public String getShukkouCount(final Param param, final String semester, final String sdate, final String edate) {
            final Set<String> set = new TreeSet<String>();
            // スクーリング
            for (final AttendInfo m : _schAttendDatList) {
                // スクーリング種別 = '2' (放送)は出校から除く
                if ((null == semester || semester.equals(m._semester)) && null != m._executedate && "1".equals(m._namespare1)
                        && between(true, m._executedate, sdate, edate)) {
                	if (!"1".equals(m._m026Namespare1)) {
                        set.add(m._executedate);
                	}
                }
            }
            // 特別活動
            for (final AttendInfo m : _specialactAttendDatList) {
                if ((null == semester || semester.equals(m._semester)) && between(true, m._executedate, sdate, edate)) {
                	if (!"1".equals(m._m026Namespare1)) {
                		if (param._isSagaken) {
                			if (null != m._m027Name1) {
                    			set.add(m._executedate);
                			}
                		} else {
                			set.add(m._executedate);
                		}
                	}
                }
            }
            // テスト
            for (final AttendInfo m : _testAttendDatList) {
                if ((null == semester || semester.equals(m._semester)) && between(true, m._executedate, sdate, edate)) {
                	if (!"1".equals(m._m026Namespare1)) {
                        set.add(m._executedate);
                	}
                }
            }
            return set.isEmpty() ? null : String.valueOf(set.size());
        }

        public String getSpecialactCount(final Param param, final String classcd, final String semester, final String sdate, final String edate) {
            String sum = null;
            // 特別活動
            for (final AttendInfo m : _specialactAttendDatList) {
                if ((null == semester || semester.equals(m._semester)) && null != m._creditTime && classcd.equals(m._classcd) &&
                        between(true, m._executedate, sdate, edate)) {
            		if (param._isSagaken) {
            			if (null != m._m027Name1) {
                			sum = add(sum, m._creditTime);
            			}
            		} else {
            			sum = add(sum, m._creditTime);
            		}
                }
            }
            return sum;
        }

        public String getAttendName(final String date, final String periodcd) {
            if (null == date || null == periodcd) {
                return null;
            }
            // スクーリング
            for (final AttendInfo m : _schAttendDatList) {
                if (date.equals(m._executedate) && between(false, periodcd, m._periodf, m._periodt)) {
                    final String attendName;
                    if ("2".equals(m._schoolingkindcd)) {
                        continue;
                    } else if ("3".equals(m._schoolingkindcd)) { // その他
                        attendName = m._subclassabbv;
                        // attendName = StringUtils.defaultString(m._subclassabbv) + "(" + StringUtils.defaultString(m._creditTime) + ")";
                    } else { // "1"equals(m._schoolingkindcd)  // 登校スクーリング
                        attendName = m._subclassabbv;
                    }
                    return attendName;
                }
            }
            // 特別活動
            for (final AttendInfo m : _specialactAttendDatList) {
                if (date.equals(m._executedate) && between(false, periodcd, m._periodf, m._periodt)) {
                    return m._subclassabbv;
                }
            }
            // テストは校時がないため表示しない。代わりに８校時の欄に「考査」を表示する
            return null;
        }

        public boolean isKousa(final String date) {
            for (final AttendInfo m : _testAttendDatList) {
                if (date.equals(m._executedate)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static class AttendInfo {
        final String _executedate;
        final String _semester;
        final String _periodf;
        final String _periodt;
        final String _classcd;
        final String _chaircd;
        final String _subclassabbv;
        final String _creditTime;
        final String _schoolingkindcd;
        final String _namespare1;
        final String _m026Namespare1;
        final String _m026Namespare2;
        final String _m027Name1;
        public AttendInfo(
                final String executedate,
                final String semester,
                final String periodf,
                final String periott,
                final String classcd,
                final String chaircd,
                final String subclassabbv,
                final String creditTime,
                final String schoolingkindcd,
                final String namespare1,
                final String m026Namespare1,
                final String m026Namespare2,
                final String m027Name1
        ) {
            _executedate = executedate;
            _semester = semester;
            _periodf = periodf;
            _periodt = periott;
            _classcd = classcd;
            _chaircd = chaircd;
            _subclassabbv = subclassabbv;
            _creditTime = creditTime;
            _schoolingkindcd = schoolingkindcd;
            _namespare1 = namespare1;
            _m026Namespare1 = m026Namespare1;
            _m026Namespare2 = m026Namespare2;
            _m027Name1 = m027Name1;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 74381 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _outputsdate;
        private final String _outputedate;
        private final String[] _categoryName;
        private final String _shuukei;
        private final String _meisai;
        private final String _loginDate;
        private final boolean _hirokofrmflg;
        private final boolean _isSagaken;

        private final String _nendo;
        private final String _loginDateString;
        private final Map<String, String> _nameMstB001;
        final boolean _isOutputDebug;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _outputsdate = request.getParameter("OUTPUTSDATE").replace('/', '-');
            _outputedate = request.getParameter("OUTPUTEDATE").replace('/', '-');
            _categoryName = request.getParameterValues("CATEGORY_NAME");
            _shuukei = request.getParameter("SHUUKEI");
            _meisai = request.getParameter("MEISAI");
            _loginDate = request.getParameter("LOGIN_DATE");
            _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_year)) + "年度";
            _loginDateString = KNJ_EditDate.h_format_JP(db2, _loginDate);
            _nameMstB001 = getNameMstB001Map(db2);
            
            final String name1 = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00'"));
        	_hirokofrmflg = "hirokoudai".equals(name1);
        	_isSagaken = "sagaken".equals(name1);
        	_isOutputDebug = "1".equals(KnjDbUtils.getDbPrginfoProperties(db2, "KNJM821", "outputDebug"));
        }

        private Map getNameMstB001Map(final DB2UDB db2) {
            final Map m = new TreeMap();
            final String sql = " SELECT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1 = 'B001' ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    m.put(rs.getString("NAMECD2"), rs.getString("NAME1"));
                }

            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return m;
        }
    }
}

// eof

