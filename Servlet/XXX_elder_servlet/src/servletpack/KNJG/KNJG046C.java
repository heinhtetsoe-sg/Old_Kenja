/*
 * $Id: 07957ccf6ed94e7e7880764cc9fce45504c037f6 $
 *
 * 作成日: 2018/06/07
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJG;


import java.util.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/*
 *  学校教育システム 賢者 [事務管理] 証明書
 */
public class KNJG046C {

    private static final Log log = LogFactory.getLog(KNJG046C.class);

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

	//日付の差(日数)を算出
    public static int differenceDays(String strOldDate1,String strNewDate2)
        throws ParseException {
    	DateFormat d1 = DateFormat.getDateInstance();
        Date date1 = d1.parse(strOldDate1.replace('-', '/'));
        Date date2 = d1.parse(strNewDate2.replace('-', '/'));
        return differenceDays(date2,date1);
    }
    public static int differenceDays(Date date1,Date date2) {
        long datetime1 = date1.getTime();
        long datetime2 = date2.getTime();
        long one_date_time = 1000 * 60 * 60 * 24;
        long diffDays = (datetime1 - datetime2) / one_date_time;
        return (int)diffDays;
    }

    private void printMain(final Vrw32alp svf, final DB2UDB db2) throws ParseException {
    	//日付で日々ループさせる
        final Map dateMap = getRemarkInfo(db2, _param._sdate, _param._edate);

        int datemaxcnt = differenceDays(_param._sdate, _param._edate);

        Date strtDate = DateFormat.getDateInstance().parse(_param._sdate.replace('-', '/'));
        Calendar cal = Calendar.getInstance();
        cal.setTime(strtDate);
        for (int datecnt = 0;datecnt <= datemaxcnt;datecnt++) {
            final String strChkDate = new SimpleDateFormat("yyyy/MM/dd",Locale.JAPAN).format(cal.getTime()).replace('/', '-');
            final RemarkInfo date = (RemarkInfo)dateMap.get(strChkDate);
            printDate(svf, db2, strChkDate, date);
            cal.add(Calendar.DAY_OF_MONTH, 1);//翌日にする
        }
//        for (final Iterator it = dateList.iterator(); it.hasNext();) {
//            final RemarkInfo date = (RemarkInfo) it.next();
//            log.debug(" date = " + date._datestr);
//            printDate(svf, db2, date);
//        }
    }

    private void printDate(final Vrw32alp svf, final DB2UDB db2, final String chkdate, final RemarkInfo date) throws ParseException {

        final String form = "KNJG046C.frm";
        svf.VrSetForm(form, 4);

        final int maxsublinenum = 4;
        final int maxlinenum = 6 * maxsublinenum;
        int line = 1;
        int total_enroll1 = 0;
        int total_enroll2 = 0;
        int total_enroll3 = 0;
        int total_attend1 = 0;
        int total_attend2 = 0;
        int total_attend3 = 0;
        int total_notice1 = 0;
        int total_notice2 = 0;
        int total_notice3 = 0;
        int total_ent1 = 0;
        int total_ent2 = 0;
        int total_out1 = 0;
        int total_out2 = 0;

        String beforegradecd = "";
        int sublinecnt = 1;

        List attendlist = null;
        attendlist = getAttendDat(db2, chkdate, _param._SCHOOLKIND);

        if (date != null || attendlist != null && attendlist.size() > 0) {
            setTitle(svf, db2, chkdate, date);
            setRemark(svf, db2, date);
        }

        //なにがしか、データがあれば出力する。
        if (attendlist != null && attendlist.size() > 0) {
            for (final Iterator it = attendlist.iterator(); it.hasNext();) {
                final AttendDatInfo attenddat = (AttendDatInfo) it.next();
                if (!"".equals(beforegradecd) && !beforegradecd.equals(attenddat._gradecd)) {
                    while (sublinecnt<=maxsublinenum) {
                        svf.VrsOut("GRADE", String.valueOf(Integer.parseInt(beforegradecd)));
                        svf.VrEndRecord();
                        line++;
                        sublinecnt++;
                    }
                	sublinecnt = 1;
                }
log.debug("line:"+line+", GRADE:"+attenddat._gradecd);
                svf.VrsOut("GRADE", String.valueOf(Integer.parseInt(attenddat._gradecd)));
                svf.VrsOut("HR_NAME", attenddat._hrname);
                svf.VrsOut("ENROLL1", attenddat._zaseki_man);
                total_enroll1 += Integer.parseInt(attenddat._zaseki_man);
                svf.VrsOut("ENROLL2", attenddat._zaseki_woman);
                total_enroll2 += Integer.parseInt(attenddat._zaseki_woman);
                svf.VrsOut("ENROLL3", attenddat._zaseki_all);
                total_enroll3 += Integer.parseInt(attenddat._zaseki_all);
                svf.VrsOut("ATTEND1", attenddat._syuukei_man);
                total_attend1 += Integer.parseInt(attenddat._syuukei_man);
                svf.VrsOut("ATTEND2", attenddat._syuukei_woman);
                total_attend2 += Integer.parseInt(attenddat._syuukei_woman);
                svf.VrsOut("ATTEND3", attenddat._syusseki_all);
                total_attend3 += Integer.parseInt(attenddat._syusseki_all);
                svf.VrsOut("NOTICE1", attenddat._kesseki_man);
                total_notice1 += Integer.parseInt(attenddat._kesseki_man);
                svf.VrsOut("NOTICE2", attenddat._kesseki_woman);
                total_notice2 += Integer.parseInt(attenddat._kesseki_woman);
                svf.VrsOut("NOTICE3", attenddat._kesseki_all);
                total_notice3 += Integer.parseInt(attenddat._kesseki_all);
                svf.VrsOut("ENT1", attenddat._nyuugaku_man);
                total_ent1 += Integer.parseInt(attenddat._nyuugaku_man);
                svf.VrsOut("ENT2", attenddat._nyuugaku_woman);
                total_ent2 += Integer.parseInt(attenddat._nyuugaku_woman);
                svf.VrsOut("OUT1", attenddat._tentaigaku_man);
                total_out1 += Integer.parseInt(attenddat._tentaigaku_man);
                svf.VrsOut("OUT2", attenddat._tentaigaku_woman);
                total_out2 += Integer.parseInt(attenddat._tentaigaku_woman);

                beforegradecd = attenddat._gradecd;
                line++;
                sublinecnt++;
                svf.VrEndRecord();
            }
            while (sublinecnt<=maxsublinenum) {
                svf.VrsOut("GRADE", String.valueOf(Integer.parseInt(beforegradecd)));
                svf.VrEndRecord();
                line++;
                sublinecnt++;
            }
            //最終行に合計を出力するため、最終行までの余白を埋めておく。
            while (line<=maxlinenum) {
                svf.VrsOut("BLANK", "X");
                svf.VrEndRecord();
                line++;
            }

            svf.VrsOut("TOTAL_NAME", "合計");
            svf.VrsOut("TOTAL_ENROLL1", String.valueOf(total_enroll1));
            svf.VrsOut("TOTAL_ENROLL2", String.valueOf(total_enroll2));
            svf.VrsOut("TOTAL_ENROLL3", String.valueOf(total_enroll3));
            svf.VrsOut("TOTAL_ATTEND1", String.valueOf(total_attend1));
            svf.VrsOut("TOTAL_ATTEND2", String.valueOf(total_attend2));
            svf.VrsOut("TOTAL_ATTEND3", String.valueOf(total_attend3));
            svf.VrsOut("TOTAL_NOTICE1", String.valueOf(total_notice1));
            svf.VrsOut("TOTAL_NOTICE2", String.valueOf(total_notice2));
            svf.VrsOut("TOTAL_NOTICE3", String.valueOf(total_notice3));
            svf.VrsOut("TOTAL_ENT1", String.valueOf(total_ent1));
            svf.VrsOut("TOTAL_ENT2", String.valueOf(total_ent2));
            svf.VrsOut("TOTAL_OUT1", String.valueOf(total_out1));
            svf.VrsOut("TOTAL_OUT2", String.valueOf(total_out2));
            svf.VrEndRecord();

        }
        if (date != null || attendlist != null && attendlist.size() > 0) {
            svf.VrEndPage();
            _hasData = true;
        }
    }

    private void setTitle(final Vrw32alp svf, final DB2UDB db2, final String chkdate, final RemarkInfo date) {
    	//TITLE
    	svf.VrsOut("TITLE", getfooterInfo(db2));
    	//DATE
    	svf.VrsOut("DATE", hiduke(chkdate));
    	//WEATHER
    	if (date != null) {
    	    final String setweatherstr = StringUtils.defaultString(date._weather1, "")
    	    		                       + ((date._weather2 == null || date._weather2 == "") ? "" : "/")
    	    		                       + StringUtils.defaultString(date._weather2, "");
    	    svf.VrsOut("WEATHER", setweatherstr);
    	}
    }

    private void setRemark(final Vrw32alp svf, final DB2UDB db2, final RemarkInfo date) {
    	if (date != null) {
        	//REMARK1_1
            setTokenString(svf, date._remark1_1, 12, 5, "REMARK1_1");
        	//REMARK1_2
            setTokenString(svf, date._remark1_2, 36, 5, "REMARK1_2");
        	//REMARK1_3
            setTokenString(svf, date._remark1_3, 12, 5, "REMARK1_3");
        	//REMARK1_4
            setTokenString(svf, date._remark1_4, 12, 5, "REMARK1_4");
        	//REMARK1_5
            setTokenString(svf, date._remark1_5, 22, 5, "REMARK1_5");
            //REMARK2_1/2/3
            svf.VrsOut("NOON1", date._remark2_1);
            svf.VrsOut("REMARK2_1_1", date._remark2_2);
            svf.VrsOut("REMARK2_1_2", date._remark2_3);
            //REMARK2_4/5/6
            svf.VrsOut("NOON2", date._remark2_4);
            svf.VrsOut("REMARK2_2_1", date._remark2_5);
            svf.VrsOut("REMARK2_2_2", date._remark2_6);
            //REMARK2_7
            setTokenString(svf, date._remark2_7, 38, 4, "REMARK2_3");
            //REMARK2_8
            setTokenString(svf, date._remark2_8, 42, 3, "REMARK2_4");
            //REMARK3_1
            setTokenString(svf, date._remark3_1, 98, 4, "REMARK3_1");
    	}
    }

    private void setTokenString(final Vrw32alp svf, final String setTokenStr, final int tokenlen, final int tokenrow, final String svfidstr) {
        final String[] token = KNJ_EditEdit.get_token(setTokenStr, tokenlen, tokenrow);
        if (null != token) {
            for (int i = 0; i < token.length; i++) {
                if (null != token[i]) {
                    svf.VrsOutn(svfidstr, i+1, token[i]);
                }
            }
        }
    }

    private List getAttendDat(final DB2UDB db2, final String date, final String schkind) {
    	List retlist = new ArrayList();
        final String sql = getAttendDatsql(date, schkind);
log.debug("sql:"+sql);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
            	final String grade = rs.getString("GRADE");
            	final String gradecd = rs.getString("GRADE_CD");
            	final String hrclass = rs.getString("HR_CLASS");
            	final String hrname = rs.getString("HR_NAME");
            	final String zaseki_man = rs.getString("ZAISEKI_MAN");
            	final String zaseki_woman = rs.getString("ZAISEKI_WONAM");
            	final String zaseki_all = rs.getString("ZAISEKI_ALL");
            	final String syuukei_man = rs.getString("SYUSSEKI_MAN");
            	final String syuukei_woman = rs.getString("SYUSSEKI_WONAM");
            	final String syusseki_all = rs.getString("SYUSSEKI_ALL");
            	final String kesseki_man = rs.getString("KESSEKI_MAN");
            	final String kesseki_woman = rs.getString("KESSEKI_WONAM");
            	final String kesseki_all = rs.getString("KESSEKI_ALL");
            	final String nyuugaku_man = rs.getString("TENTAIGAKU_MAN");
            	final String nyuugaku_woman = rs.getString("TENTAIGAKU_WOMAN");
            	final String tentaigaku_man = rs.getString("TENTAIGAKU_MAN");
            	final String tentaigaku_woman = rs.getString("TENTAIGAKU_WOMAN");
            	AttendDatInfo addwk = new AttendDatInfo(grade, gradecd, hrclass, hrname, zaseki_man, zaseki_woman, zaseki_all,
            			                                syuukei_man, syuukei_woman, syusseki_all, kesseki_man, kesseki_woman, kesseki_all,
            			                                nyuugaku_man, nyuugaku_woman, tentaigaku_man, tentaigaku_woman);
            	retlist.add(addwk);
            }
        } catch (Exception ex) {
            log.error("CERTIF_SCHOOL_DAT read error! ", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    	return retlist;
    }

    //引数schkindについては、日付取得時の校種を利用するため、_paramからは取得していないことに注意。
    private String getAttendDatsql(final String date, final String schkind) {
        final StringBuffer stb = new StringBuffer();
        //在籍/欠席件数(計)
        stb.append(" WITH SCHBASE AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     count(T1.SCHREGNO) AS CNT1, ");
        stb.append("     count(T2.SCHREGNO) AS CNT2 ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST M1 ");
        stb.append("       ON M1.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN SEMESTER_MST M2 ");
        stb.append("       ON M2.YEAR = T1.YEAR ");
        stb.append("       AND M2.SEMESTER = T1.SEMESTER ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT M3 ");
        stb.append("       ON M3.YEAR = T1.YEAR ");
        stb.append("       AND M3.GRADE = T1.GRADE ");
        stb.append("     LEFT JOIN ATTEND_DAY_DAT T2 ");
        stb.append("       ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("       AND T2.ATTENDDATE = '"+date+"' ");
        stb.append("       AND T2.DI_CD IN ('4','5','6') ");
        stb.append(" WHERE ");
        stb.append("     M3.SCHOOL_KIND = '"+schkind+"' AND ");
        stb.append("     T1.YEAR = '"+_param._year+"'   AND ");
        stb.append("     M2.SDATE <= '"+date+"' AND M2.EDATE >= '"+date+"' AND ");
        stb.append("     NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
        stb.append("                    WHERE   S1.SCHREGNO = T1.SCHREGNO ");
        stb.append("                            AND ");
        stb.append("                           ( ");
        stb.append("                            (S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN M2.EDATE < '"+date+"' THEN M2.EDATE ELSE '"+date+"' END) ");
        stb.append("                            OR ");
        stb.append("                            (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN M2.EDATE < '"+date+"' THEN M2.EDATE ELSE '"+date+"' END) ");
        stb.append("                           ) ");
        stb.append("                   ) ");
        stb.append(" GROUP BY ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS ");
        //在籍/欠席件数(男)
        stb.append(" ), SCHBASE_MAN AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     M1.SEX, ");
        stb.append("     count(T1.SCHREGNO) AS CNT1, ");
        stb.append("     count(T2.SCHREGNO) AS CNT2 ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST M1 ");
        stb.append("       ON M1.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN SEMESTER_MST M2 ");
        stb.append("       ON M2.YEAR = T1.YEAR ");
        stb.append("       AND M2.SEMESTER = T1.SEMESTER ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT M3 ");
        stb.append("       ON M3.YEAR = T1.YEAR ");
        stb.append("       AND M3.GRADE = T1.GRADE ");
        stb.append("     LEFT JOIN ATTEND_DAY_DAT T2 ");
        stb.append("       ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("       AND T2.ATTENDDATE = '"+date+"' ");
        stb.append("       AND T2.DI_CD IN ('4','5','6') ");
        stb.append(" WHERE ");
        stb.append("     M3.SCHOOL_KIND = '"+schkind+"' AND ");
        stb.append("     T1.YEAR = '"+_param._year+"'   AND ");
        stb.append("     M2.SDATE <= '"+date+"' AND M2.EDATE >= '"+date+"' AND ");
        stb.append("     M1.SEX = '1' AND ");
        stb.append("     NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
        stb.append("                    WHERE   S1.SCHREGNO = T1.SCHREGNO ");
        stb.append("                            AND ");
        stb.append("                           ( ");
        stb.append("                            (S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN M2.EDATE < '"+date+"' THEN M2.EDATE ELSE '"+date+"' END) ");
        stb.append("                            OR ");
        stb.append("                            (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN M2.EDATE < '"+date+"' THEN M2.EDATE ELSE '"+date+"' END) ");
        stb.append("                           ) ");
        stb.append("                   ) ");
        stb.append(" GROUP BY ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     M1.SEX ");
        //在籍/欠席件数(女)
        stb.append(" ), SCHBASE_WOMAN AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     M1.SEX, ");
        stb.append("     count(T1.SCHREGNO) AS CNT1, ");
        stb.append("     count(T2.SCHREGNO) AS CNT2 ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST M1 ");
        stb.append("       ON M1.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN SEMESTER_MST M2 ");
        stb.append("       ON M2.YEAR = T1.YEAR ");
        stb.append("       AND M2.SEMESTER = T1.SEMESTER ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT M3 ");
        stb.append("       ON M3.YEAR = T1.YEAR ");
        stb.append("       AND M3.GRADE = T1.GRADE ");
        stb.append("     LEFT JOIN ATTEND_DAY_DAT T2 ");
        stb.append("       ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("       AND T2.ATTENDDATE = '"+date+"' ");
        stb.append("       AND T2.DI_CD IN ('4','5','6') ");
        stb.append(" WHERE ");
        stb.append("     M3.SCHOOL_KIND = '"+schkind+"' AND ");
        stb.append("     T1.YEAR = '"+_param._year+"'   AND ");
        stb.append("     M2.SDATE <= '"+date+"' AND M2.EDATE >= '"+date+"' AND ");
        stb.append("     M1.SEX = '2' AND ");
        stb.append("     NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
        stb.append("                    WHERE   S1.SCHREGNO = T1.SCHREGNO ");
        stb.append("                            AND ");
        stb.append("                           ( ");
        stb.append("                            (S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN M2.EDATE < '"+date+"' THEN M2.EDATE ELSE '"+date+"' END) ");
        stb.append("                            OR ");
        stb.append("                            (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN M2.EDATE < '"+date+"' THEN M2.EDATE ELSE '"+date+"' END) ");
        stb.append("                           ) ");
        stb.append("                   ) ");
        stb.append(" GROUP BY ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     M1.SEX ");
        //転退学(男)
        stb.append(" ), SCHGRD_MAN AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     count(T1.SCHREGNO) AS CNT3 ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST M1 ");
        stb.append("       ON M1.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN SEMESTER_MST M2 ");
        stb.append("       ON M2.YEAR = T1.YEAR ");
        stb.append("       AND M2.SEMESTER = T1.SEMESTER ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT M3 ");
        stb.append("       ON M3.YEAR = T1.YEAR ");
        stb.append("       AND M3.GRADE = T1.GRADE ");
        stb.append(" WHERE ");
        stb.append("     M3.SCHOOL_KIND = '"+schkind+"' AND ");
        stb.append("     T1.YEAR = '"+_param._year+"'   AND ");
        stb.append("     M1.SEX = '1' AND ");
        stb.append("     M2.SDATE <= '"+date+"' AND M2.EDATE >= '"+date+"' AND ");
        stb.append("     NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
        stb.append("                    WHERE   S1.SCHREGNO = T1.SCHREGNO ");
        stb.append("                            AND ");
        stb.append("                           ( ");
        stb.append("                            (S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN M2.EDATE < '"+date+"' THEN M2.EDATE ELSE '"+date+"' END) ");
        stb.append("                            OR ");
        stb.append("                            (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN M2.EDATE < '"+date+"' THEN M2.EDATE ELSE '"+date+"' END) ");
        stb.append("                           ) ");
        stb.append("                   ) ");
        stb.append("     AND ");
        stb.append("     (M1.GRD_DIV IN('2','3') AND M1.GRD_DATE = '"+date+"') ");
        stb.append("      ");
        stb.append(" GROUP BY ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS ");
        //転退学(女)
        stb.append(" ), SCHGRD_WOMAN AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     count(T1.SCHREGNO) AS CNT3 ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST M1 ");
        stb.append("       ON M1.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN SEMESTER_MST M2 ");
        stb.append("       ON M2.YEAR = T1.YEAR ");
        stb.append("       AND M2.SEMESTER = T1.SEMESTER ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT M3 ");
        stb.append("       ON M3.YEAR = T1.YEAR ");
        stb.append("       AND M3.GRADE = T1.GRADE ");
        stb.append(" WHERE ");
        stb.append("     M3.SCHOOL_KIND = '"+schkind+"' AND ");
        stb.append("     T1.YEAR = '"+_param._year+"'   AND ");
        stb.append("     M1.SEX = '2' AND ");
        stb.append("     M2.SDATE <= '"+date+"' AND M2.EDATE >= '"+date+"' AND ");
        stb.append("     NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
        stb.append("                    WHERE   S1.SCHREGNO = T1.SCHREGNO ");
        stb.append("                            AND ");
        stb.append("                           ( ");
        stb.append("                            (S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN M2.EDATE < '"+date+"' THEN M2.EDATE ELSE '"+date+"' END) ");
        stb.append("                            OR ");
        stb.append("                            (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN M2.EDATE < '"+date+"' THEN M2.EDATE ELSE '"+date+"' END) ");
        stb.append("                           ) ");
        stb.append("                   ) ");
        stb.append("     AND ");
        stb.append("     (M1.GRD_DIV IN('2','3') AND M1.GRD_DATE = '"+date+"') ");
        stb.append("      ");
        stb.append(" GROUP BY ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS ");
        //入学(男)
        stb.append(" ), SCHENT_MAN AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     count(T1.SCHREGNO) AS CNT4 ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST M1 ");
        stb.append("       ON M1.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN SEMESTER_MST M2 ");
        stb.append("       ON M2.YEAR = T1.YEAR ");
        stb.append("       AND M2.SEMESTER = T1.SEMESTER ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT M3 ");
        stb.append("       ON M3.YEAR = T1.YEAR ");
        stb.append("       AND M3.GRADE = T1.GRADE ");
        stb.append(" WHERE ");
        stb.append("     M3.SCHOOL_KIND = '"+schkind+"' AND ");
        stb.append("     T1.YEAR = '"+_param._year+"'   AND ");
        stb.append("     M1.SEX = '1' AND ");
        stb.append("     M2.SDATE <= '"+date+"' AND M2.EDATE >= '"+date+"' AND ");
        stb.append("     NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
        stb.append("                    WHERE   S1.SCHREGNO = T1.SCHREGNO ");
        stb.append("                            AND ");
        stb.append("                           ( ");
        stb.append("                            (S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN M2.EDATE < '"+date+"' THEN M2.EDATE ELSE '"+date+"' END) ");
        stb.append("                            OR ");
        stb.append("                            (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN M2.EDATE < '"+date+"' THEN M2.EDATE ELSE '"+date+"' END) ");
        stb.append("                           ) ");
        stb.append("                   ) ");
        stb.append("     AND ");
        stb.append("     (M1.ENT_DIV IS NOT NULL AND M1.ENT_DATE = '"+date+"') ");
        stb.append("      ");
        stb.append(" GROUP BY ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS ");
        //入学(女)
        stb.append(" ), SCHENT_WOMAN AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     count(T1.SCHREGNO) AS CNT4 ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST M1 ");
        stb.append("       ON M1.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN SEMESTER_MST M2 ");
        stb.append("       ON M2.YEAR = T1.YEAR ");
        stb.append("       AND M2.SEMESTER = T1.SEMESTER ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT M3 ");
        stb.append("       ON M3.YEAR = T1.YEAR ");
        stb.append("       AND M3.GRADE = T1.GRADE ");
        stb.append(" WHERE ");
        stb.append("     M3.SCHOOL_KIND = '"+schkind+"' AND ");
        stb.append("     T1.YEAR = '"+_param._year+"'   AND ");
        stb.append("     M1.SEX = '2' AND ");
        stb.append("     M2.SDATE <= '"+date+"' AND M2.EDATE >= '"+date+"' AND ");
        stb.append("     NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
        stb.append("                    WHERE   S1.SCHREGNO = T1.SCHREGNO ");
        stb.append("                            AND ");
        stb.append("                           ( ");
        stb.append("                            (S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN M2.EDATE < '"+date+"' THEN M2.EDATE ELSE '"+date+"' END) ");
        stb.append("                            OR ");
        stb.append("                            (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN M2.EDATE < '"+date+"' THEN M2.EDATE ELSE '"+date+"' END) ");
        stb.append("                           ) ");
        stb.append("                   ) ");
        stb.append("     AND ");
        stb.append("     (M1.ENT_DIV IS NOT NULL AND M1.ENT_DATE = '"+date+"') ");
        stb.append("      ");
        stb.append(" GROUP BY ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS ");
        stb.append(" ) ");
        //上記個別データをまとめて名称を紐づけ
        stb.append(" SELECT ");
        stb.append("     '"+date+"' AS FIXSTR, ");
        stb.append("     TX0.YEAR, ");
        stb.append("     TX0.SEMESTER, ");
        stb.append("     TX0.GRADE, ");
        stb.append("     TM3.GRADE_CD, ");
        stb.append("     TX0.HR_CLASS, ");
        stb.append("     TM4.HR_NAME, ");
        stb.append("     value(TX1.CNT1, 0) AS ZAISEKI_MAN, ");
        stb.append("     value(TX2.CNT1, 0) AS ZAISEKI_WONAM, ");
        stb.append("     value(TX0.CNT1, 0) AS ZAISEKI_ALL, ");
        stb.append("     value(TX1.CNT1, 0) - value(TX1.CNT2, 0) AS SYUSSEKI_MAN, ");
        stb.append("     value(TX2.CNT1, 0) - value(TX2.CNT2, 0) AS SYUSSEKI_WONAM, ");
        stb.append("     value(TX0.CNT1, 0) - value(TX0.CNT2, 0) AS SYUSSEKI_ALL, ");
        stb.append("     value(TX1.CNT2, 0) AS KESSEKI_MAN, ");
        stb.append("     value(TX2.CNT2, 0) AS KESSEKI_WONAM, ");
        stb.append("     value(TX0.CNT2, 0) AS KESSEKI_ALL, ");
        stb.append("     value(TE1.CNT4, 0) AS NYUUGAKU_MAN, ");
        stb.append("     value(TE2.CNT4, 0) AS NYUUGAKU_WOMAN, ");
        stb.append("     value(TG1.CNT3, 0) AS TENTAIGAKU_MAN, ");
        stb.append("     value(TG2.CNT3, 0) AS TENTAIGAKU_WOMAN ");
        stb.append(" FROM ");
        stb.append("   SCHBASE TX0 ");
        stb.append("   LEFT JOIN SCHBASE_MAN TX1 ");
        stb.append("     ON TX1.YEAR = TX0.YEAR ");
        stb.append("     AND TX1.GRADE = TX0.GRADE ");
        stb.append("     AND TX1.HR_CLASS = TX0.HR_CLASS ");
        stb.append("   LEFT JOIN SCHBASE_WOMAN TX2 ");
        stb.append("     ON TX2.YEAR = TX0.YEAR ");
        stb.append("     AND TX2.GRADE = TX0.GRADE ");
        stb.append("     AND TX2.HR_CLASS = TX0.HR_CLASS ");
        stb.append("   LEFT JOIN SCHGRD_MAN TG1 ");
        stb.append("     ON TG1.YEAR = TX0.YEAR ");
        stb.append("     AND TG1.GRADE = TX0.GRADE ");
        stb.append("     AND TG1.HR_CLASS = TX0.HR_CLASS ");
        stb.append("   LEFT JOIN SCHGRD_MAN TG2 ");
        stb.append("     ON TG2.YEAR = TX0.YEAR ");
        stb.append("     AND TG2.GRADE = TX0.GRADE ");
        stb.append("     AND TG2.HR_CLASS = TX0.HR_CLASS ");
        stb.append("   LEFT JOIN SCHENT_MAN TE1 ");
        stb.append("     ON TE1.YEAR = TX0.YEAR ");
        stb.append("     AND TE1.GRADE = TX0.GRADE ");
        stb.append("     AND TE1.HR_CLASS = TX0.HR_CLASS ");
        stb.append("   LEFT JOIN SCHENT_MAN TE2 ");
        stb.append("     ON TE2.YEAR = TX0.YEAR ");
        stb.append("     AND TE2.GRADE = TX0.GRADE ");
        stb.append("     AND TE2.HR_CLASS = TX0.HR_CLASS ");
        stb.append("   LEFT JOIN SCHREG_REGD_GDAT TM3 ");
        stb.append("     ON TM3.YEAR = TX0.YEAR ");
        stb.append("     AND TM3.GRADE = TX0.GRADE ");
        stb.append("   LEFT JOIN SCHREG_REGD_HDAT TM4 ");
        stb.append("     ON TM4.YEAR = TX0.YEAR ");
        stb.append("     AND TM4.SEMESTER = TX0.SEMESTER ");
        stb.append("     AND TM4.GRADE = TX0.GRADE ");
        stb.append("     AND TM4.HR_CLASS = TX0.HR_CLASS ");
        stb.append(" ORDER BY ");
        stb.append("   TX0.YEAR, ");
        stb.append("   TX0.SEMESTER, ");
        stb.append("   TX0.GRADE DESC, ");
        stb.append("   TM3.GRADE_CD DESC, ");
        stb.append("   TX0.HR_CLASS ASC ");
        return stb.toString();
    }

    private String getfooterInfo(final DB2UDB db2) {
    	String retstr = "";
        final String sql = getfooterInfosql();
log.debug("sql:"+sql);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
            	retstr = rs.getString("SCHOOL_NAME");
            }
        } catch (Exception ex) {
            log.error("CERTIF_SCHOOL_DAT read error! ", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    	return retstr;
    }

    private String getfooterInfosql() {
        final StringBuffer stb = new StringBuffer();
    	stb.append(" SELECT ");
    	stb.append("  SCHOOL_NAME ");
    	stb.append(" FROM ");
    	stb.append("  CERTIF_SCHOOL_DAT ");
    	stb.append(" WHERE ");
    	stb.append("  YEAR = '"+_param._year+"' ");
    	stb.append("  AND CERTIF_KINDCD = '144' ");
    	stb.append(" ORDER BY ");
    	stb.append("  YEAR, ");
    	stb.append("  CERTIF_KINDCD ");
        return stb.toString();
    }

    private Map getRemarkInfo(final DB2UDB db2, final String sdate, final String edate) {
        Map retmap = new LinkedMap();
        final String sql = getRemarkInfosql(sdate, edate);
log.debug("sql:"+sql);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schoolkind = rs.getString("SCHOOL_KIND");
                final String datestr = rs.getString("DATESTR");
                final String weather1 = StringUtils.defaultString(rs.getString("WEATHER1"));
                final String weather2 = StringUtils.defaultString(rs.getString("WEATHER2"));
                final String remark1_1 = StringUtils.defaultString(rs.getString("REMARK1_1"));
                final String remark1_2 = StringUtils.defaultString(rs.getString("REMARK1_2"));
                final String remark1_3 = StringUtils.defaultString(rs.getString("REMARK1_3"));
                final String remark1_4 = StringUtils.defaultString(rs.getString("REMARK1_4"));
                final String remark1_5 = StringUtils.defaultString(rs.getString("REMARK1_5"));
                final String remark2_1 = StringUtils.defaultString(rs.getString("REMARK2_1"));
                final String remark2_2 = StringUtils.defaultString(rs.getString("REMARK2_2"));
                final String remark2_3 = StringUtils.defaultString(rs.getString("REMARK2_3"));
                final String remark2_4 = StringUtils.defaultString(rs.getString("REMARK2_4"));
                final String remark2_5 = StringUtils.defaultString(rs.getString("REMARK2_5"));
                final String remark2_6 = StringUtils.defaultString(rs.getString("REMARK2_6"));
                final String remark2_7 = StringUtils.defaultString(rs.getString("REMARK2_7"));
                final String remark2_8 = StringUtils.defaultString(rs.getString("REMARK2_8"));
                final String remark3_1 = StringUtils.defaultString(rs.getString("REMARK3_1"));
                RemarkInfo listaddwk = new RemarkInfo(schoolkind, datestr, weather1, weather2, remark1_1, remark1_2, remark1_3, remark1_4,
                                                       remark1_5, remark2_1, remark2_2, remark2_3, remark2_4, remark2_5,
                                                       remark2_6, remark2_7, remark2_8, remark3_1);
                retmap.put(datestr, listaddwk);
            }
        } catch (Exception ex) {
            log.error("printSvfMain read error! ", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retmap;
    }

    private String getRemarkInfosql(final String sdate, final String edate) {
        final StringBuffer stb = new StringBuffer();
    	stb.append(" SELECT ");
    	stb.append("  D0.SCHOOL_KIND, ");
    	stb.append("  D0.DIARY_DATE AS DATESTR, ");
    	stb.append("  N1.NAME1 AS WEATHER1, ");
    	stb.append("  N2.NAME1 AS WEATHER2, ");
    	stb.append("  D001.REMARK1 AS REMARK1_1, ");
    	stb.append("  D001.REMARK2 AS REMARK1_2, ");
    	stb.append("  D001.REMARK3 AS REMARK1_3, ");
    	stb.append("  D001.REMARK4 AS REMARK1_4, ");
    	stb.append("  D001.REMARK5 AS REMARK1_5, ");
    	stb.append("  D002.REMARK1 AS REMARK2_1, ");
    	stb.append("  D002.REMARK2 AS REMARK2_2, ");
    	stb.append("  D002.REMARK3 AS REMARK2_3, ");
    	stb.append("  D002.REMARK4 AS REMARK2_4, ");
    	stb.append("  D002.REMARK5 AS REMARK2_5, ");
    	stb.append("  D002.REMARK6 AS REMARK2_6, ");
    	stb.append("  D002.REMARK7 AS REMARK2_7, ");
    	stb.append("  D002.REMARK8 AS REMARK2_8, ");
    	stb.append("  D003.REMARK1 AS REMARK3_1 ");
    	stb.append(" FROM ");
    	stb.append("  SCHOOL_DIARY_DAT D0 ");
    	stb.append("  LEFT JOIN SCHOOL_DIARY_DETAIL_SEQ_DAT D001 ");
    	stb.append("     ON D001.SCHOOLCD = D0.SCHOOLCD ");
    	stb.append("     AND D001.SCHOOL_KIND = D0.SCHOOL_KIND ");
    	stb.append("     AND D001.DIARY_DATE = D0.DIARY_DATE ");
    	stb.append("     AND D001.SEQ = '001' ");
    	stb.append("  LEFT JOIN SCHOOL_DIARY_DETAIL_SEQ_DAT D002 ");
    	stb.append("     ON D002.SCHOOLCD = D0.SCHOOLCD ");
    	stb.append("     AND D002.SCHOOL_KIND = D0.SCHOOL_KIND ");
    	stb.append("     AND D002.DIARY_DATE = D0.DIARY_DATE ");
    	stb.append("     AND D002.SEQ = '002' ");
    	stb.append("  LEFT JOIN SCHOOL_DIARY_DETAIL_SEQ_DAT D003 ");
    	stb.append("     ON D003.SCHOOLCD = D0.SCHOOLCD ");
    	stb.append("     AND D003.SCHOOL_KIND = D0.SCHOOL_KIND ");
    	stb.append("     AND D003.DIARY_DATE = D0.DIARY_DATE ");
    	stb.append("     AND D003.SEQ = '003' ");
    	stb.append("  LEFT JOIN NAME_MST N1 ");
    	stb.append("     ON N1.NAMECD2 = D0.WEATHER ");
    	stb.append("     AND NAMECD1 = 'A006' ");
    	stb.append("  LEFT JOIN NAME_MST N2 ");
        stb.append("     ON N2.NAMECD2 = D0.WEATHER2 ");
        stb.append("     AND N2.NAMECD1 = 'A006' ");
    	stb.append(" WHERE ");
    	stb.append("   D0.SCHOOLCD = '"+_param._schoolcd+"' ");

    	//※SCHOOL_DIARY_DATのSCHOOL_KINDを元に検索を行っていたが、
    	//  SCHOOL_DIARY_DATをベースとするのはNGとなった(日付ベースで、何かデータがあれば出力)ため、
    	//  指示画面で指定された校種のデータのみを対象とする。
    	//  また、出欠データでは、こちらのSCHOOL_KINDが利用できないため、指示画面のSCHOOL_KINDを利用する事としたので、
    	//  出欠データと紐づける際に不要なデータを取ることになる面も加味して、この対応としている。
//        if ("1".equals(_param._use_prg_schoolkind)) {
//            if (!"".equals(_param._schoolKindInState)) {
//                stb.append("   AND D0.SCHOOL_KIND IN (" + _param._schoolKindInState + ") ");
//            }
//        } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOLKIND)) {
            stb.append("  AND D0.SCHOOL_KIND = '" + _param._SCHOOLKIND + "' ");
//        }
    	stb.append("   AND D0.DIARY_DATE BETWEEN '"+sdate+"' AND '"+edate+"' ");
    	stb.append(" ORDER BY ");
    	stb.append("   SCHOOL_KIND, ");
    	stb.append("   D0.DIARY_DATE ");
        return stb.toString();
    }

    private static class AttendDatInfo {
    	final String _grade;
    	final String _gradecd;
    	final String _hrclass;
    	final String _hrname;
    	final String _zaseki_man;
    	final String _zaseki_woman;
    	final String _zaseki_all;
    	final String _syuukei_man;
    	final String _syuukei_woman;
    	final String _syusseki_all;
    	final String _kesseki_man;
    	final String _kesseki_woman;
    	final String _kesseki_all;
    	final String _nyuugaku_man;
    	final String _nyuugaku_woman;
    	final String _tentaigaku_man;
    	final String _tentaigaku_woman;

    	public AttendDatInfo(
        		final String grade,
        		final String gradecd,
        		final String hrclass,
        		final String hrname,
        		final String zaseki_man,
        		final String zaseki_woman,
        		final String zaseki_all,
        		final String syuukei_man,
        		final String syuukei_woman,
        		final String syusseki_all,
        		final String kesseki_man,
        		final String kesseki_woman,
        		final String kesseki_all,
        		final String nyuugaku_man,
        		final String nyuugaku_woman,
        		final String tentaigaku_man,
        		final String tentaigaku_woman
        		) {
        	_grade = grade;
        	_gradecd = gradecd;
        	_hrclass = hrclass;
        	_hrname = hrname;
        	_zaseki_man = zaseki_man;
        	_zaseki_woman = zaseki_woman;
        	_zaseki_all = zaseki_all;
        	_syuukei_man = syuukei_man;
        	_syuukei_woman = syuukei_woman;
        	_syusseki_all = syusseki_all;
        	_kesseki_man = kesseki_man;
        	_kesseki_woman = kesseki_woman;
        	_kesseki_all = kesseki_all;
        	_nyuugaku_man = nyuugaku_man;
        	_nyuugaku_woman = nyuugaku_woman;
        	_tentaigaku_man = tentaigaku_man;
        	_tentaigaku_woman = tentaigaku_woman;
        }
    }

    private static class RemarkInfo {
        final String _schoolkind;
        final String _datestr;
        final String _weather1;
        final String _weather2;
        final String _remark1_1;
        final String _remark1_2;
        final String _remark1_3;
        final String _remark1_4;
        final String _remark1_5;
        final String _remark2_1;
        final String _remark2_2;
        final String _remark2_3;
        final String _remark2_4;
        final String _remark2_5;
        final String _remark2_6;
        final String _remark2_7;
        final String _remark2_8;
        final String _remark3_1;

        public RemarkInfo(
                final String schoolkind,
                final String datestr,
                final String weather1,
                final String weather2,
                final String remark1_1,
                final String remark1_2,
                final String remark1_3,
                final String remark1_4,
                final String remark1_5,
                final String remark2_1,
                final String remark2_2,
                final String remark2_3,
                final String remark2_4,
                final String remark2_5,
                final String remark2_6,
                final String remark2_7,
                final String remark2_8,
                final String remark3_1
        ) {
        	_schoolkind = schoolkind;
        	_datestr = datestr;
        	_weather1 = weather1;
        	_weather2 = weather2;
        	_remark1_1 = remark1_1;
        	_remark1_2 = remark1_2;
        	_remark1_3 = remark1_3;
        	_remark1_4 = remark1_4;
        	_remark1_5 = remark1_5;
        	_remark2_1 = remark2_1;
        	_remark2_2 = remark2_2;
        	_remark2_3 = remark2_3;
        	_remark2_4 = remark2_4;
        	_remark2_5 = remark2_5;
        	_remark2_6 = remark2_6;
        	_remark2_7 = remark2_7;
        	_remark2_8 = remark2_8;
        	_remark3_1 = remark3_1;
        }
    }

    private String hiduke(final String date) {
        if (null == date) {
            return date;
        }
        final Calendar cal = Calendar.getInstance();
        cal.setTime(java.sql.Date.valueOf(date));
        final String youbi = "(" + new String[] {null, "日", "月", "火", "水", "木", "金", "土"} [cal.get(Calendar.DAY_OF_WEEK)] + ")";
log.debug("date:" + KNJ_EditDate.h_format_JP_MD(date) + youbi);
        return KNJ_EditDate.h_format_JP_MD(date) + youbi;
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(request, db2);
        log.fatal("$Revision: 61858 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /**
     * パラメータクラス
     */
    private static class Param {

        final String _year;
        final String _loginDate;
        final String _prgId;
        final String _sdate;
        final String _edate;

        final String _schoolcd;
        final String _SCHOOLKIND;
        final String _schoolKindInState;
        final String _useSchool_KindField;
        final String _use_prg_schoolkind;
        final String _selectSchoolKind;

        public Param(final HttpServletRequest request, final DB2UDB db2) {
            _prgId = request.getParameter("PRGID");
            _year = request.getParameter("YEAR");
            _edate = request.getParameter("DATE_TO").replace('/', '-');
            _sdate = request.getParameter("DATE_FROM").replace('/', '-');
            _loginDate = request.getParameter("LOGIN_DATE").replace('/', '-');
            _schoolcd = request.getParameter("SCHOOLCD");

            _use_prg_schoolkind = request.getParameter("use_prg_schoolkind");
            _schoolKindInState = getSchoolKindInState();
            _selectSchoolKind = request.getParameter("selectSchoolKind");

            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLKIND = request.getParameter("SCHOOL_KIND");
        }

        private String getSchoolKindInState() {
            String retStr = "";
            if (!"1".equals(_use_prg_schoolkind)) {
                return retStr;
            }
            if (null == _selectSchoolKind || "".equals(_selectSchoolKind)) {
                return retStr;
            }
            final String[] strSplit = StringUtils.split(_selectSchoolKind, ":");
            String sep = "";
            for (int i = 0; i < strSplit.length; i++) {
                retStr += sep + "'" + strSplit[i] + "'";
                sep = ",";
            }
            return retStr;
        }


    }
}

// eof

