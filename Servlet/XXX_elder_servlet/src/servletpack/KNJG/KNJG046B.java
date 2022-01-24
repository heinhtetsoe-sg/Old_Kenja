/*
 * $Id: 91ca584540fcba7ad004522859f634d8133b20a8 $
 *
 * 作成日: 2018/04/10
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJG;


import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/*
 *  学校教育システム 賢者 [事務管理] 証明書
 */
public class KNJG046B {

    private static final Log log = LogFactory.getLog(KNJG046B.class);

    private static final String SEMEALL = "9";

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

            printMain(svf, db2);
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
    private static int getMS932ByteLength(final String str) {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
    }

    private void printMain(final Vrw32alp svf, final DB2UDB db2)  throws ParseException, SQLException {
        final List dateList = getDateList(db2, _param._sdate, _param._edate);
        for (final Iterator it = dateList.iterator(); it.hasNext();) {
            final String date = (String) it.next();
            log.debug(" date = " + date);
            printDate(svf, db2, date);
        }
    }

    private List getDateList(final DB2UDB db2, final String sdate, final String edate) {
        final List allClassHolidayList = getAllClassHolidayList(db2, sdate, edate);
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        final List dateList = new ArrayList();
        final Calendar cal = Calendar.getInstance();
        cal.setTime(Date.valueOf(sdate));
        final Calendar cale = Calendar.getInstance();
        cale.setTime(Date.valueOf(edate));
        while (cal.before(cale) || cal.equals(cale)) {
            final String date = df.format(cal.getTime());
            if (allClassHolidayList.contains(date)) {
                // すべてのHRが休日の場合、その日付を出力しない
                log.info(" 休日:" + date);
            } else {
                dateList.add(date);
            }
            cal.add(Calendar.DATE, 1);
        }
        return dateList;
    }

    /**
     * 指定校種のクラスが全て休日の日付リスト
     * @param db2
     * @param sdate 範囲開始日付
     * @param edate 範囲終了日付
     * @return　指定校種のクラスが全て休日の日付リスト
     */
    private List getAllClassHolidayList(final DB2UDB db2, final String sdate, final String edate) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        List allClassHolidayList = new ArrayList();
        try {
            final StringBuffer stb = new StringBuffer();
            // HRのカウント
            stb.append(" WITH T_HR AS (  ");
            stb.append("    SELECT T1.SEMESTER, COUNT(T1.HR_CLASS) AS HR_COUNT ");
            stb.append("    FROM SCHREG_REGD_GDAT T3, SCHREG_REGD_HDAT T1 ");
            stb.append("    WHERE T1.YEAR = '" + _param._year + "' ");
            stb.append("        AND T3.YEAR = T1.YEAR ");
            stb.append("        AND T3.GRADE = T1.GRADE ");
            stb.append("        AND T3.SCHOOL_KIND IN ('J','H') ");
            stb.append("    GROUP BY T1.SEMESTER ");
            stb.append(" ) ");
            stb.append(" , T_SEMESTER AS ( ");
            stb.append("    SELECT * ");
            stb.append("    FROM SEMESTER_MST ");
            stb.append("    WHERE SEMESTER <> '9' ");
            stb.append(" ) ");
            // 開始日付: 最初の学期ならその学期の開始日付の一日、それ以外は開始日付
            // 終了日付: 最後の学期ならその学期の終了日付の月の最後の日付、それ以外はMAX(ひとつあとの学期の開始日付の前日, その学期の終了日付)
            stb.append(" , T_SEMESTER1 AS ( ");
            stb.append("    SELECT ");
            stb.append("        T1.SEMESTER, ");
            stb.append("        CASE WHEN T1.SEMESTER = T3.MIN_SEMESTER THEN DATE(RTRIM(CHAR(YEAR(T1.SDATE))) || '-' || RTRIM(CHAR(MONTH(T1.SDATE))) || '-01') ");
            stb.append("             ELSE T1.SDATE ");
            stb.append("        END AS SDATE, ");
            stb.append("        CASE WHEN T1.SEMESTER = T2.MAX_SEMESTER THEN LAST_DAY(T1.EDATE) ");
            stb.append("             WHEN AFT.SDATE - 1 DAY > T1.EDATE THEN AFT.SDATE - 1 DAY  ");
            stb.append("             ELSE T1.EDATE ");
            stb.append("        END AS EDATE ");
            stb.append("    FROM T_SEMESTER T1 ");
            stb.append("    LEFT JOIN (SELECT YEAR, MAX(SEMESTER) AS MAX_SEMESTER ");
            stb.append("               FROM T_SEMESTER ");
            stb.append("               GROUP BY YEAR) T2 ON T2.YEAR = T1.YEAR  ");
            stb.append("    LEFT JOIN (SELECT YEAR, MIN(SEMESTER) AS MIN_SEMESTER ");
            stb.append("               FROM T_SEMESTER ");
            stb.append("               GROUP BY YEAR) T3 ON T3.YEAR = T1.YEAR  ");
            stb.append("    LEFT JOIN T_SEMESTER AFT ON AFT.YEAR = T1.YEAR ");
            stb.append("        AND INT(AFT.SEMESTER) = INT(T1.SEMESTER) + 1  ");
            stb.append("    WHERE T1.YEAR = '" + _param._year + "' ");
            stb.append(" ), EVENT_DATE AS ( ");
            stb.append("    SELECT T2.SEMESTER, T1.EXECUTEDATE, COUNT(T1.HR_CLASS) AS HR_COUNT ");
            stb.append("    FROM SCHREG_REGD_GDAT T3, EVENT_DAT T1 ");
            stb.append("    LEFT JOIN T_SEMESTER1 T2 ON T1.EXECUTEDATE BETWEEN T2.SDATE AND T2.EDATE ");
            stb.append("    WHERE ");
            stb.append("        T1.EXECUTEDATE BETWEEN '" + sdate + "' AND '" + edate + "' ");
            stb.append("        AND T1.HOLIDAY_FLG = '1' ");
            stb.append("        AND T3.YEAR = FISCALYEAR(T1.EXECUTEDATE) ");
            stb.append("        AND T3.GRADE = T1.GRADE ");
            stb.append("        AND T3.SCHOOL_KIND IN ('J','H') ");
            stb.append("    GROUP BY T2.SEMESTER, T1.EXECUTEDATE ");
            stb.append(" ) ");
            stb.append(" SELECT T1.EXECUTEDATE  ");
            stb.append(" FROM EVENT_DATE T1 ");
            stb.append(" INNER JOIN T_HR T3 ON T3.SEMESTER = T1.SEMESTER ");
            stb.append(" WHERE ");
            stb.append("    T1.HR_COUNT = T3.HR_COUNT ");

            final String sql = stb.toString();
            //log.debug("sqlHoliday:"+sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                allClassHolidayList.add(rs.getString("EXECUTEDATE"));
            }
        } catch (Exception ex) {
            log.error("printSvfMain read error! ", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return allClassHolidayList;
    }

    private void printDate(final Vrw32alp svf, final DB2UDB db2, final String date) throws ParseException, SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        String semester = _param._semester;
        try {
            final String sql = "SELECT SEMESTER FROM SEMESTER_MST WHERE YEAR = '" + _param._year + "' AND SEMESTER <> '" + SEMEALL + "' AND '" + date + "' BETWEEN SDATE AND EDATE ORDER BY SEMESTER ";
            //log.debug("sqlsemester:"+sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            if (rs.next()) {
                semester = rs.getString("SEMESTER");
            }
        } catch (Exception ex) {
            log.error("printSvfMain read error! ", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        final int maxlineHrclass = 20;//学年毎に20組まで(後で制限がかかるため、ここでは暫定値を設定)
        final List hrClassList = new ArrayList();
        try {
            final String sql = sqlRegdHdat(semester);
            //log.debug("regdsql:"+sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            String oldgrade = "";
            int gradeCnt = 0;
            int printline = 0;
            while (rs.next()) {
                final String grade = rs.getString("GRADE");
                final String hrclass = rs.getString("HR_CLASS");
                final String hrname = rs.getString("HR_NAMEABBV");
                final String staffname = rs.getString("STAFFNAME");
                final String school_kind = rs.getString("SCHOOL_KIND");
                final String grade_cd = rs.getString("GRADE_CD");

                if (!"J".equals(school_kind) && !"H".equals(school_kind)) {
                    continue;
                }
                //校種毎に３学年まで
                if (!"01".equals(grade_cd) && !"02".equals(grade_cd) && !"03".equals(grade_cd)) {
                    continue;
                }
                if (!oldgrade.equals(grade)) {
                    oldgrade = grade;
                    printline = 0;
                    gradeCnt++;
                }
                //最大３学年のみ
                if (gradeCnt > 3) {
                    break;
                }
                printline++;
                //学年毎に20組まで
                if (maxlineHrclass < printline) {
                    continue;
                }
                final HrClass hrClasses = new HrClass(grade, hrclass, hrname, school_kind, gradeCnt, printline, staffname);
                hrClassList.add(hrClasses);
            }
        } catch (Exception ex) {
            log.error("printSvfMain read error! ", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        final String form;
        form = "KNJG046B.frm";
        svf.VrSetForm(form, 4);

        final String[] attends = new String[] {"ENROLL", "NOTICE", "LATE", "EARLY", "MOURNING"};
        final int[] total01 = new int[attends.length];
        final int[] total02 = new int[attends.length];
        final int[] total03 = new int[attends.length];
        final int[] total = new int[attends.length];
        for (int i = 0; i < attends.length; i++) {
            total01[i] = 0;
            total02[i] = 0;
            total03[i] = 0;
            total[i] = 0;
        }

        boolean outputttlflg = false;
        int newlinecnt = 0;
        String outgradebk = "";
        final Map schregHrclassMap = new HashMap();
        for (final Iterator it = hrClassList.iterator(); it.hasNext();) {
            final HrClass hrClasses = (HrClass) it.next();

            if (!outgradebk.equals(hrClasses._grade)) {
                newlinecnt = 0;
            }
            try {
                final String sql = sqlRegdDatsql(date, hrClasses._gradehrclass, semester);
                //log.debug("regddatsql:"+sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                int zaiCnt = 0;
                while (rs.next()) {
                    final String sex = rs.getString("SEX");
                    zaiCnt++;
                    hrClasses._schregMap.put(rs.getString("SCHREGNO"), sex);
                    schregHrclassMap.put(rs.getString("SCHREGNO"), hrClasses);
                }
                hrClasses._zaiCnt = zaiCnt;
                //クラスとして存在するけど、'102'対象外のクラスは除外するため、上で設定したline出力を再調整する。
                if (zaiCnt > 0) {
                    newlinecnt++;
                    hrClasses._printline = newlinecnt;
                } else {
                	hrClasses._printline = 0;
                }
                setTotalCnt(hrClasses, hrClasses._zaiCnt, 0, total01, total02, total03);
                if (!outputttlflg && zaiCnt > 0) {
                	outputttlflg = true;
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            outgradebk = hrClasses._grade;
        }

        //在籍人数が全て0、またはデータ自体が無い場合は、後続処理は不要。
        if (!outputttlflg) {
        	return;
        }

        HeadInfo hinfo = getHeadInfo(db2);
        if (hinfo != null) {
            svf.VrsOut("ADDRESSEE", StringUtils.defaultString(hinfo._address) + "　宛"); // 宛名
            svf.VrsOut("SCHOOL_NAME", "所属　" + StringUtils.defaultString(hinfo._schoolname)); // 学校名
            svf.VrsOut("PRINCIPAL_NAME", "氏名　" + StringUtils.defaultString(hinfo._principalname)); // 校長名
        }
        svf.VrsOut("DATE", hiduke(_param._outputDate) + "　提出"); // 出力対象日
        svf.VrsOut("ATTEND_DATE", hiduke(date)); // 提出日
        _hasData = true;

        _param._attendParamMap.put("semesFlg", new Boolean(false));
        _param._attendParamMap.put("befDayFrom", date);
        _param._attendParamMap.put("befDayTo", date);
        _param._attendParamMap.put("groupByDiv", "SCHREGNO");
        _param._attendParamMap.put("outputDebug", "1");

        //ATTEND_SEMES
        try {
            final String attendSql = AttendAccumulate.getAttendDatOneDaySql(
                    _param._year,
                    semester,
                    date,
                    _param._attendParamMap
                    );
            //log.debug("attendSql2:"+attendSql);
            ps = db2.prepareStatement(attendSql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final HrClass hrClasses = (HrClass) schregHrclassMap.get(rs.getString("SCHREGNO"));
                if (null == hrClasses) {
                    //log.warn("no hrclass: schregno = " + rs.getString("SCHREGNO"));
                    continue;
                }
                hrClasses._sickCnt += rs.getInt("SICK");
                setTotalCnt(hrClasses, rs.getInt("SICK"), 1, total01, total02, total03);
                hrClasses._lateCnt += rs.getInt("LATE");
                setTotalCnt(hrClasses, rs.getInt("LATE"), 2, total01, total02, total03);
                hrClasses._earlyCnt += rs.getInt("EARLY");
                setTotalCnt(hrClasses, rs.getInt("EARLY"), 3, total01, total02, total03);
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        outgradebk = "";
        int putmaxline = 6; //出力制限数
        for (final Iterator it = hrClassList.iterator(); it.hasNext();) {
            final HrClass hrClasses = (HrClass) it.next();
            if (!outgradebk.equals(hrClasses._grade)) {
            	newlinecnt = 0;
            }
            if (hrClasses._zaiCnt == 0) continue;
            if (hrClasses._printline > 0 && newlinecnt <= putmaxline) {
                svf.VrsOutn("HR_NAME"+hrClasses._printGradeField, hrClasses._printline, hrClasses._hrname);
                svf.VrsOutn("TEACHER_NAME"+hrClasses._printGradeField, hrClasses._printline, getmyouji(hrClasses._staffname));
                svf.VrsOutn("ENROLL"+hrClasses._printGradeField, hrClasses._printline, String.valueOf(hrClasses._zaiCnt));
                svf.VrsOutn("NOTICE"+hrClasses._printGradeField, hrClasses._printline, String.valueOf(hrClasses._sickCnt));
                svf.VrsOutn("LATE"+hrClasses._printGradeField, hrClasses._printline, String.valueOf(hrClasses._lateCnt));
                svf.VrsOutn("EARLY"+hrClasses._printGradeField, hrClasses._printline, String.valueOf(hrClasses._earlyCnt));
                svf.VrsOutn("ATTEND"+hrClasses._printGradeField, hrClasses._printline, String.valueOf((hrClasses._zaiCnt - hrClasses._sickCnt)*100/hrClasses._zaiCnt) + "%");
                if (hrClasses._printGradeField == 3) {
                    svf.VrsOutn("HR_NAME"+(hrClasses._printGradeField+1), hrClasses._printline, hrClasses._hrname);
                    svf.VrsOutn("TEACHER_NAME"+(hrClasses._printGradeField+1), hrClasses._printline, getmyouji(hrClasses._staffname));
                    svf.VrsOutn("ENROLL"+(hrClasses._printGradeField+1), hrClasses._printline, String.valueOf(hrClasses._zaiCnt));
                }
                newlinecnt++;
            }
            outgradebk = hrClasses._grade;
        }

        //1年合計
        svf.VrsOut("GRADE1", "平日1年");
        svf.VrsOutn("ENROLL1", putmaxline+1, String.valueOf(total01[0]));
        svf.VrsOutn("NOTICE1", putmaxline+1, String.valueOf(total01[1]));
        svf.VrsOutn("LATE1", putmaxline+1, String.valueOf(total01[2]));
        svf.VrsOutn("EARLY1", putmaxline+1, String.valueOf(total01[3]));
        if (total01[0] != 0) {
        	svf.VrsOutn("ATTEND1", putmaxline+1, String.valueOf((total01[0] - total01[1])*100/total01[0]) + "%");
        }

        //2年合計
        svf.VrsOut("GRADE2", "平日2年");
        svf.VrsOutn("ENROLL2", putmaxline+1, String.valueOf(total02[0]));
        svf.VrsOutn("NOTICE2", putmaxline+1, String.valueOf(total02[1]));
        svf.VrsOutn("LATE2", putmaxline+1, String.valueOf(total02[2]));
        svf.VrsOutn("EARLY2", putmaxline+1, String.valueOf(total02[3]));
        if (total02[0] != 0) {
        	svf.VrsOutn("ATTEND2", putmaxline+1, String.valueOf((total02[0] - total02[1])*100/total02[0]) + "%");
        }

        //3年合計
        svf.VrsOut("GRADE3", "平日3年");
        svf.VrsOutn("ENROLL3", putmaxline+1, String.valueOf(total03[0]));
        svf.VrsOutn("NOTICE3", putmaxline+1, String.valueOf(total03[1]));
        svf.VrsOutn("LATE3", putmaxline+1, String.valueOf(total03[2]));
        svf.VrsOutn("EARLY3", putmaxline+1, String.valueOf(total03[3]));
        if (total03[0] != 0) {
        	svf.VrsOutn("ATTEND3", putmaxline+1, String.valueOf((total03[0] - total03[1])*100/total03[0]) + "%");
        }

        //3年(進路)合計
        svf.VrsOut("GRADE4", "平日3年");
        svf.VrsOutn("ENROLL4", putmaxline+1, String.valueOf(total03[0]));
        svf.VrsOutn("COLLEGE", putmaxline+1, String.valueOf(0));
        svf.VrsOutn("SPECIAL", putmaxline+1, String.valueOf(0));
        svf.VrsOutn("JOB", putmaxline+1, String.valueOf(0));
        svf.VrsOutn("UNCERT", putmaxline+1, String.valueOf(0));

        //その他項目
        int grpline = 1;
        int[] remainsize = {25};
        try {
            final String sql = getSchoolDiaryDetailsql(date);
            //log.debug("diarysql:"+sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String eventetc = rs.getString("REMARK1");
                final String passage = rs.getString("REMARK2");
                if (remainsize[0] > 0) {
                    setEventAffair(svf, eventetc, passage, grpline, remainsize);
                    grpline++;
                }
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        int noticeboxmaxline = remainsize[0];
        for (int cnt = 0;cnt < noticeboxmaxline;cnt++) {
        	svf.VrsOut("GRP1", String.valueOf(grpline));
        	svf.VrsOut("GRP2", String.valueOf(grpline));
        	svf.VrsOut("AFFAIR", "");
        	svf.VrsOut("HANDLE", "");
        	svf.VrEndRecord();
        }

        svf.VrEndPage();
        _hasData = true;
    }

    private String getmyouji(final String cutstaffname) {
        String[] staffnamecut;
        if (cutstaffname.indexOf("　") >= 0) {
            staffnamecut = StringUtils.split(cutstaffname, "　");
        } else {
            staffnamecut = StringUtils.split(cutstaffname, " ");
        }
        return staffnamecut[0];
    }

    // ※hrclassにzaiCntの値が設定された状態で利用すること。
    private void setTotalCnt(final HrClass hrclass, final int val, final int idx, final int[] total01, final int[] total02, final int[] total03) {
    	//在籍人数が0なら除外。
    	if (hrclass._zaiCnt > 0) {
    	    if ("01".equals(hrclass._grade)) {
    		    total01[idx] += val;
    	    } else if ("02".equals(hrclass._grade)) {
    		    total02[idx] += val;
    	    } else if ("03".equals(hrclass._grade)) {
    		    total03[idx] += val;
    	    }
    	}
    	return;
    }

    private class HeadInfo {
    	final String _address;
    	final String _schoolname;
    	final String _principalname;
    	HeadInfo(final String address, final String schoolname, final String principalname) {
    		_address = address;
    		_schoolname = schoolname;
    		_principalname = principalname;
    	}
    }

    HeadInfo getHeadInfo(final DB2UDB db2) {
    	HeadInfo retheadinfo = null; //new HeadInfo();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String attendSql = sqlHeadInfo();
            //log.debug("attendsql:"+attendSql);
            ps = db2.prepareStatement(attendSql);
            rs = ps.executeQuery();
            while (rs.next()) {
            	final String address = rs.getString("REMARK1");
            	final String schoolname = rs.getString("SCHOOL_NAME");
            	final String principalname = rs.getString("PRINCIPAL_NAME");
            	retheadinfo = new HeadInfo(address, schoolname, principalname);
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    	return retheadinfo;
    }

    private void setEventAffair(final Vrw32alp svf, final String eventetc, final String passage, final int grpline, int remainsize[]) {

        Vector v_edit1,v_edit2;
        Enumeration e_edit1,e_edit2;
        int maxlinenum1 = 0;
        int maxlinenum2 = 0;
        int remainsize1 = remainsize[0];
        int remainsize2 = remainsize[0];
        int maxoutline = 0;
        KNJ_EditEdit edit;

        edit = new KNJ_EditEdit(eventetc);
        v_edit1 = edit.get_token(40 ,remainsize1);
        maxlinenum1 = v_edit1.size();
        remainsize1 -= maxlinenum1;
        e_edit1 = v_edit1.elements();

        edit = new KNJ_EditEdit(passage);
        v_edit2 = edit.get_token(60 ,remainsize2);
        maxlinenum2 = v_edit2.size();
        remainsize2 -= maxlinenum2;
        e_edit2 = v_edit2.elements();

        maxoutline = maxlinenum1 < maxlinenum2 ? maxlinenum2 : maxlinenum1;
        int line = 0;
        while (line < maxoutline) {
            //出力
            svf.VrsOut("GRP1", String.valueOf(grpline));
            svf.VrsOut("GRP2", String.valueOf(grpline));
            if (e_edit1.hasMoreElements()) {
                svf.VrsOut("AFFAIR", e_edit1.nextElement().toString());
            } else {
                svf.VrsOut("AFFAIR", "");
            }
            if (e_edit2.hasMoreElements()) {
                svf.VrsOut("HANDLE", e_edit2.nextElement().toString());
            } else {
                svf.VrsOut("HANDLE", "");
            }
            //改行
            svf.VrEndRecord();
            line++;
        }
        remainsize[0] -= maxoutline;
    }

    private String sqlHeadInfo() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT REMARK1, SCHOOL_NAME, PRINCIPAL_NAME ");
        stb.append(" FROM CERTIF_SCHOOL_DAT ");
        stb.append(" WHERE ");
        stb.append("   YEAR = '" + _param._year + "' ");
        stb.append("   AND CERTIF_KINDCD = '144' ");
        return stb.toString();
    }

    private String sqlRegdHdat(final String semester) {
        final StringBuffer stb = new StringBuffer();
        stb.append("    SELECT  T1.GRADE, T1.HR_CLASS, T1.HR_NAMEABBV, T2.SCHOOL_KIND, T2.GRADE_CD, SM.STAFFNAME ");
        stb.append("    FROM    SCHREG_REGD_HDAT T1 ");
        stb.append("            LEFT JOIN STAFF_MST SM ON T1.TR_CD1 = SM.STAFFCD ");
        stb.append("            ,SCHREG_REGD_GDAT T2 ");
        stb.append("    WHERE   T1.YEAR = '" + _param._year + "' ");
        stb.append("        AND T1.SEMESTER = '"+ semester +"' ");
        stb.append("        AND T2.YEAR = T1.YEAR ");
        stb.append("        AND T2.GRADE = T1.GRADE ");
        stb.append("        AND T2.SCHOOL_KIND IN ('J','H') ");
        stb.append("    ORDER BY T1.GRADE, T1.HR_CLASS ");
        return stb.toString();
    }

    private String sqlRegdDatsql(final String date, final String gradeHrclass, final String semester) {
        final StringBuffer stb = new StringBuffer();
        stb.append("WITH SCHNO_A AS( ");
        stb.append("    SELECT  T1.SCHREGNO, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO ");
        stb.append("    FROM    SCHREG_REGD_DAT T1,SEMESTER_MST T2  ");
        stb.append("    WHERE   T1.YEAR = '" + _param._year + "'  ");
        stb.append("        AND T1.SEMESTER = '" + semester + "'  ");
        stb.append("        AND T1.GRADE || T1.HR_CLASS = '" + gradeHrclass + "'  ");
        stb.append("        AND T1.YEAR = T2.YEAR  ");
        stb.append("        AND T1.SEMESTER = T2.SEMESTER  ");
        stb.append("        AND T1.MAJORCD = '102' ");
        stb.append("        AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1  ");
        stb.append("           WHERE   S1.SCHREGNO = T1.SCHREGNO  ");
        stb.append("               AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '" + date + "' THEN T2.EDATE ELSE '" + date + "' END)  ");
        stb.append("                 OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + date + "' THEN T2.EDATE ELSE '" + date + "' END)) )  ");
        stb.append("    )  ");
        stb.append(", SCHNO_B AS( ");
        stb.append("    SELECT  T1.SCHREGNO, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO ");
        stb.append("    FROM    SCHREG_REGD_DAT T1,SEMESTER_MST T2  ");
        stb.append("    WHERE   T1.YEAR = '" + _param._year + "'  ");
        stb.append("        AND T1.SEMESTER = '" + semester + "'  ");
        stb.append("        AND T1.GRADE || T1.HR_CLASS = '" + gradeHrclass + "'  ");
        stb.append("        AND T1.YEAR = T2.YEAR  ");
        stb.append("        AND T1.SEMESTER = T2.SEMESTER  ");
        stb.append("        AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1  ");
        stb.append("           WHERE   S1.SCHREGNO = T1.SCHREGNO  ");
        stb.append("               AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '" + date + "' THEN T2.EDATE ELSE '" + date + "' END)  ");
        stb.append("                 OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + date + "' THEN T2.EDATE ELSE '" + date + "' END)) )  ");
        stb.append("        AND EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1  ");
        stb.append("           WHERE   S1.SCHREGNO = T1.SCHREGNO  ");
        stb.append("               AND S1.TRANSFERCD IN ('1','2') AND CASE WHEN T2.EDATE < '" + date + "' THEN T2.EDATE ELSE '" + date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE)  ");
        stb.append("    )  ");
        stb.append("SELECT  T1.GRADE, T1.HR_CLASS, T1.SCHREGNO, T5.SEX ");
        stb.append("FROM    SCHNO_A T1  ");
        stb.append("        INNER JOIN SCHREG_BASE_MST T5 ON T1.SCHREGNO = T5.SCHREGNO  ");
        stb.append("        LEFT JOIN SCHNO_B T2 ON T1.SCHREGNO = T2.SCHREGNO  ");
        stb.append("WHERE T1.SCHREGNO IS NOT NULL ");
        return stb.toString();
    }

    private String getAttendDayDatsql(final String date, final String gradeHrclass, final String semester) {
        final StringBuffer stb = new StringBuffer();
        stb.append("SELECT ");
        stb.append("    T1.DI_CD, ");
        stb.append("    COUNT(*) AS CNT ");
        stb.append("FROM ");
        stb.append("    ATTEND_DAY_DAT T1, ");
        stb.append("    SCHREG_REGD_DAT T2 ");
        stb.append("WHERE ");
        stb.append("    T1.ATTENDDATE = '" + date + "' ");
        stb.append("    AND T1.YEAR = '" + _param._year + "' ");
        stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("    AND T2.YEAR = T1.YEAR ");
        stb.append("    AND T2.SEMESTER = '" + semester + "' ");
        stb.append("    AND T2.GRADE || T2.HR_CLASS = '" + gradeHrclass + "' ");
        stb.append("GROUP BY ");
        stb.append("    T1.DI_CD ");
        stb.append("ORDER BY ");
        stb.append("    T1.DI_CD ");
        return stb.toString();
    }

    private String hiduke(final String date) {
        if (null == date) {
            return date;
        }
        final Calendar cal = Calendar.getInstance();
        cal.setTime(Date.valueOf(date));
        final String youbi = "(" + new String[] {null, "日", "月", "火", "水", "木", "金", "土"} [cal.get(Calendar.DAY_OF_WEEK)] + ")";
        String setYear = KNJ_EditDate.h_format_JP_N(date);
        return setYear + KNJ_EditDate.h_format_JP_MD(date) + youbi;
    }

    private static class HrClass {

        final String _gradehrclass;
        final String _grade;
        final String _hrclass;
        final String _hrname;
        final String _school_kind;
        final int _printGradeField;
        int _printline;
        final Map _schregMap;

        final String _staffname;

        int _zaiCnt = 0;
        int _sickCnt = 0;
        int _lateCnt = 0;
        int _earlyCnt = 0;

        public HrClass(final String grade,
                       final String hrclass,
                       final String hrname,
                       final String school_kind,
                       final int gradeCnt,
                       final int printline,
                       final String staffname
        ) {
            _grade = grade;
            _hrclass = hrclass;
            _gradehrclass = grade + hrclass;
            _school_kind = school_kind;
            _printGradeField = gradeCnt;
            _printline = printline;
            _hrname = hrname;
            _schregMap = new HashMap();
            _staffname = staffname;
        }

        public String toString() {
            return _gradehrclass;
        }
    }

    private String getSchoolDiaryDetailsql(final String date) {
        final StringBuffer stb = new StringBuffer();
        stb.append("SELECT ");
        stb.append("    T1.* ");
        stb.append("FROM ");
        stb.append("    SCHOOL_DIARY_DETAIL_SEQ_DAT T1 ");
        stb.append("WHERE ");
        stb.append("   T1.SCHOOLCD = '" + _param._schoolcd + "'");
        stb.append("   AND T1.SCHOOL_KIND = '" + _param._schoolkind + "'");
        stb.append("   AND T1.DIARY_DATE = '" + date + "' ");
        stb.append("ORDER BY ");
        stb.append("    T1.SEQ ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(request, db2);
        log.fatal("$Revision: 66665 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /**
     * パラメータクラス
     */
    private static class Param {

        final String _year;
        final String _semester;
        final String _loginDate;
        final String _prgId;

        final String _sdate;
        final String _edate;
        final String _schoolcd;
        final String _schoolkind;
        final String _outputDate;

        /** 教育課程コードを使用するか */
        private Map _attendParamMap;

        public Param(final HttpServletRequest request, final DB2UDB db2) {
            _prgId = request.getParameter("PRGID");
            if ("KNJG046B".equals(_prgId)) {
                _year = request.getParameter("YEAR");
                _semester = request.getParameter("SEMESTER");
                _edate = request.getParameter("DATE_TO").replace('/', '-');
                _sdate = request.getParameter("DATE_FROM").replace('/', '-');
            } else {
                _year = request.getParameter("CTRL_YEAR");
                _semester = request.getParameter("CTRL_SEMESTER");
                _edate = request.getParameter("DIARY_DATE").replace('/', '-');
                _sdate = _edate;
            }
            _loginDate = request.getParameter("LOGIN_DATE").replace('/', '-');
            _outputDate = request.getParameter("OUTPUT_DATE").replace('/', '-');
            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _schoolcd = request.getParameter("SCHOOLCD");
            _schoolkind = request.getParameter("SCHOOLKIND");
        }

    }
}

// eof

