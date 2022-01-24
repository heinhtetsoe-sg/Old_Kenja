//kanji=漢字
/*
 * $Id: 936af28e92f8884808d855296f2d5ed3f8e80327 $
 *
 * 作成日: 2010/02/22 22:22:22 - JST
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJC;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *
 *   学校教育システム賢者 [出欠管理] 
 *
 *                   ＜ＫＮＪＣ０３１Ａ クラス別出欠情報＞
 */

public class KNJC031A {

    private static final Log log = LogFactory.getLog(KNJC031A.class);

    Param _param;

    /**
     * @param request
     * @param response
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) {
        
        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        try {
            response.setContentType("application/pdf");

            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            boolean hasData = printMain(db2, svf);

            if (!hasData) {
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

    private boolean printMain(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJC031A.frm", 4);
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean hasData = false;
        
        try {
            final String sql = selectAttendQuery();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                svf.VrsOut("HR_CLASS", _param._hrName);
                svf.VrsOut("NENDO", KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度");
                svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._ctrlDate));
                svf.VrsOut("SEMESTER", _param._semesterName);
                
                svf.VrsOut("ATTENDNO", Integer.valueOf(rs.getString("ATTENDNO")).toString());
                svf.VrsOut("NAME", rs.getString("NAME"));
                svf.VrsOut("APPOINTED_DAY", rs.getString("APPOINTED_DAY"));
                svf.VrsOut("LESSON", rs.getString("LESSON"));
                svf.VrsOut("OFFDAYS", rs.getString("OFFDAYS"));
                svf.VrsOut("ABROAD", rs.getString("ABROAD"));
                svf.VrsOut("ABSENT", rs.getString("ABSENT"));
                svf.VrsOut("SUSPEND", rs.getString("SUSPEND"));
                svf.VrsOut("VIRUS", rs.getString("VIRUS"));
                svf.VrsOut("MOURNING", rs.getString("MOURNING"));

                svf.VrsOut("PRESENT", rs.getString("CLASSDAYS2"));
                svf.VrsOut("ABSENCE", rs.getString("NONOTICE"));
                svf.VrsOut("ATTEND", rs.getString("CLASSDAYS3"));
                svf.VrsOut("LATE", rs.getString("LATEDETAIL"));
                svf.VrsOut("KEKKATIME", rs.getString("KEKKA_JISU"));
                svf.VrsOut("KEKKACNT", rs.getString("KEKKA"));

                svf.VrsOut("ACCUM_LESSON", rs.getString("SUM_CLASSDAYS"));
                svf.VrsOut("ACCUM_SUSPEND", rs.getString("SUM_SUSPEND"));
                svf.VrsOut("ACCUM_VIRUS", rs.getString("SUM_VIRUS"));
                svf.VrsOut("ACCUM_MOURNING", rs.getString("SUM_MOURNING"));
                svf.VrsOut("ACCUM_PRESENT", rs.getString("SUM_CLASSDAYS2"));
                svf.VrsOut("ACCUM_ABSENCE", rs.getString("SUM_SICK"));
                svf.VrsOut("ACCUM_ATTEND", rs.getString("SUM_CLASSDAYS3"));
                svf.VrsOut("ACCUM_LATE", rs.getString("SUM_LATEDETAIL"));
                svf.VrsOut("ACCUM_KEKKATIME", rs.getString("SUM_KEKKA_JISU"));
                svf.VrsOut("ACCUM_KEKKACNT", rs.getString("SUM_KEKKA"));
                
                svf.VrEndRecord();
                
                hasData = true;
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return hasData;
    }

    /**
     * php画面 KNJC031AQuery.incからコピー
     * @return
     */
    private String selectAttendQuery() {
        
        final Map rangeMonth = new HashMap();
        rangeMonth.put("04","'04'");
        rangeMonth.put("05","'04','05'");
        rangeMonth.put("06","'04','05','06'");
        rangeMonth.put("07","'04','05','06','07'");
        rangeMonth.put("08","'04','05','06','07','08'");
        rangeMonth.put("09","'04','05','06','07','08','09'");
        rangeMonth.put("10","'04','05','06','07','08','09','10'");
        rangeMonth.put("11","'04','05','06','07','08','09','10','11'");
        rangeMonth.put("12","'04','05','06','07','08','09','10','11','12'");
        rangeMonth.put("01","'04','05','06','07','08','09','10','11','12','01'");
        rangeMonth.put("02","'04','05','06','07','08','09','10','11','12','01','02'");
        rangeMonth.put("03","'04','05','06','07','08','09','10','11','12','01','02','03'");
        
        String[] monthSem = StringUtils.split(_param._month, "-");


        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     SD.SCHREGNO, ");          //学籍番号
        stb.append("     SD.ATTENDNO, ");
        stb.append("     SM.NAME, ");         //名前
        stb.append("     AM.APPOINTED_DAY, ");     //締め日
        stb.append("     AD.LESSON, ");            //授業数
        stb.append("     AD.OFFDAYS, ");           //休学数
        stb.append("     AD.ABROAD, ");            //留学数
        stb.append("     AD.ABSENT, ");            //公欠数
        stb.append("     AD.SUSPEND, ");           //出停
        stb.append("     AD.VIRUS, ");             //伝染病
        stb.append("     AD.MOURNING, ");          //忌引
        stb.append("     AD.LESSON - AD.SUSPEND - AD.MOURNING - AD.OFFDAYS - AD.ABROAD - AD.VIRUS ");
        if ("1".equals(_param._knjSchoolMst._semOffDays)) {
            stb.append("     + AD.OFFDAYS ");
        }
        stb.append("     AS CLASSDAYS2, "); //出席すべき数
        stb.append("     AD.NONOTICE, ");          //欠席
        stb.append("     ((AD.LESSON ");
        stb.append("      - (AD.SUSPEND + AD.MOURNING + AD.OFFDAYS + AD.ABROAD + AD.VIRUS)) ");
        stb.append("      - (AD.SICK + AD.NOTICE + AD.NONOTICE)) AS CLASSDAYS3, ");  //出席数
        stb.append("     AD.LATEDETAIL, ");        //遅刻詳細
        stb.append("     AD.KEKKA_JISU, ");        //早退回数
        stb.append("     AD.KEKKA, ");             //早退回数
        stb.append("     SUMAD.SUM_CLASSDAYS, ");  //累積・授業数
        stb.append("     SUMAD.SUM_SUSPEND, ");    //累積・出停
        stb.append("     SUMAD.SUM_VIRUS, ");      //累積・伝染病
        stb.append("     SUMAD.SUM_MOURNING, ");   //累積・忌引
        stb.append("     SUMAD.SUM_CLASSDAYS - SUMAD.SUM_SUSPEND - SUMAD.SUM_MOURNING - SUMAD.SUM_OFFDAYS - SUMAD.SUM_ABROAD - SUMAD.SUM_VIRUS ");
        if ("1".equals(_param._knjSchoolMst._subOffDays)) {
            stb.append("     + SUMAD.SUM_OFFDAYS ");
        }
        stb.append("     AS SUM_CLASSDAYS2, "); //累積・出席すべき数
        stb.append("     SUMAD.SUM_SICK + SUMAD.SUM_NOTICE + SUMAD.SUM_NONOTICE ");
        if ("1".equals(_param._knjSchoolMst._subOffDays)) {
            stb.append("     + SUMAD.SUM_OFFDAYS ");
        }
        stb.append("     AS SUM_SICK, ");  //累積・欠席数
        stb.append("     ((SUMAD.SUM_CLASSDAYS ");
        stb.append("      - (SUMAD.SUM_SUSPEND + SUMAD.SUM_MOURNING + SUMAD.SUM_OFFDAYS + SUMAD.SUM_ABROAD + SUMAD.SUM_VIRUS)) ");
        stb.append("      - (SUMAD.SUM_SICK + SUMAD.SUM_NOTICE + SUMAD.SUM_NONOTICE)) AS SUM_CLASSDAYS3, ");  //累積・出数
        stb.append("     SUMAD.SUM_LATEDETAIL, "); //累積・遅刻詳細

        stb.append("     SUMAD.SUM_KEKKA_JISU, "); //累積・欠課数
        stb.append("     SUMAD.SUM_KEKKA ");       //累積・早退回数
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT SD ");
        stb.append(" LEFT OUTER JOIN ");
        stb.append("     SCHREG_BASE_MST SM ");
        stb.append(" ON SD.SCHREGNO = SM.SCHREGNO ");
        stb.append(" LEFT OUTER JOIN ");
        stb.append("     (SELECT ");
        stb.append("         * ");
        stb.append("      FROM ");
        stb.append("         ATTEND_SEMES_DAT ");
        stb.append("      WHERE ");
        stb.append("         YEAR = '" + _param._year + "' ");
        stb.append("         AND MONTH = '" + monthSem[0] + "' ");
        stb.append("         AND SEMESTER = '" + monthSem[1] + "' ) AS AD ");
        stb.append(" ON AD.SCHREGNO = SD.SCHREGNO ");
                            
        stb.append(" LEFT OUTER JOIN ");
        stb.append("     APPOINTED_DAY_MST AM ");
        stb.append(" ON AD.YEAR = AM.YEAR ");
        stb.append("   AND AD.MONTH = AM.MONTH ");
        stb.append("   AND AD.SEMESTER = AM.SEMESTER ");
                            
        stb.append(" LEFT OUTER JOIN ");
        stb.append("     (SELECT ");
        stb.append("         SCHREGNO, ");
        stb.append("         SUM(LESSON) AS SUM_CLASSDAYS, ");
        stb.append("         SUM(OFFDAYS) AS SUM_OFFDAYS, ");
        stb.append("         SUM(ABROAD) AS SUM_ABROAD, ");
        stb.append("         SUM(SUSPEND) AS SUM_SUSPEND, ");
        stb.append("         SUM(VIRUS) AS SUM_VIRUS, ");
        stb.append("         SUM(MOURNING) AS SUM_MOURNING, ");
        stb.append("         SUM(SICK) AS SUM_SICK, ");
        stb.append("         SUM(NOTICE) AS SUM_NOTICE, ");
        stb.append("         SUM(NONOTICE) AS SUM_NONOTICE, ");
        stb.append("         SUM(LATEDETAIL) AS SUM_LATEDETAIL, ");
        stb.append("         SUM(KEKKA_JISU) AS SUM_KEKKA_JISU, ");
        stb.append("         SUM(KEKKA) AS SUM_KEKKA ");
        stb.append("      FROM ");
        stb.append("         ATTEND_SEMES_DAT ");
        stb.append("      WHERE ");
        stb.append("            YEAR = '" + _param._year + "' ");
                
        if(monthSem[0] != null && !"".equals(monthSem[0])){
            stb.append("        AND MONTH IN( " + (String) rangeMonth.get(monthSem[0]) + ")");
            stb.append("        AND SEMESTER <= '" + monthSem[1] + "' ");
        }

        stb.append("      GROUP BY ");
        stb.append("         SCHREGNO) AS SUMAD ");
        stb.append(" ON SUMAD.SCHREGNO = SD.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     SD.YEAR = '" + _param._year + "' AND ");
                
        // 2005/05/11 attend_semes_datにデータが存在しない場合でも、表示可能へ変更
        if(monthSem[0] != null && !"".equals(monthSem[0])){
            stb.append("     SD.SEMESTER = '" + monthSem[1] + "' AND");
        }else{
            stb.append("     SD.SEMESTER IS NULL  AND");
        }

        stb.append("     SD.GRADE = '" + _param._grade + "' AND ");
        stb.append("     SD.HR_CLASS = '" + _param._hrClass + "' ");
        stb.append(" ORDER BY ");
        stb.append("     SD.ATTENDNO ");
        
        return stb.toString();
    }

    /** パラメータ取得処?*/
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _grade;
        private final String _hrClass;
        private final String _month;
        private final String _semesterName;
        private final String _ctrlDate;
        
        private final KNJSchoolMst _knjSchoolMst;
        private final String _hrName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("CTRL_SEMESTER");
            String[] gradeHrclass = StringUtils.split(request.getParameter("HR_CLASS"), "-");
            _grade = gradeHrclass[0];
            _hrClass = gradeHrclass[1];
            _month = request.getParameter("MONTH");
            _semesterName = getSemesterName(db2, StringUtils.split(_month, "-")[0], StringUtils.split(_month, "-")[1]);
            _ctrlDate = request.getParameter("CTRL_DATE");
            _knjSchoolMst = new KNJSchoolMst(db2, _year);
            _hrName = getHrName(db2);
        }

        private String getSemesterName(DB2UDB db2, String month, String semester) throws SQLException {
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT DISTINCT ");
            sql.append("     t1.namecd2, t1.name1, t1.namespare1, t2.semestername ");
            sql.append(" FROM ");
            sql.append("     name_mst t1, semester_mst t2");
            sql.append(" WHERE ");
            sql.append("     t1.namecd1 = 'Z005' ");
            sql.append("     AND t1.namecd2 = '" + month + "' ");
            sql.append("     AND t2.year  = '" + _year + "' ");
            sql.append("     AND t2.semester  = '" + semester + "' ");
            sql.append(" ORDER BY ");
            sql.append("     namespare1 ");
            
            String semesterName = null;
            log.debug(" semester sql = " + sql.toString());
            PreparedStatement ps = db2.prepareStatement(sql.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String monthName = rs.getString("NAME1");
                semesterName = rs.getString("SEMESTERNAME");
                if (semesterName != null) {
                    semesterName = semesterName + " " + monthName;
                } else {
                    semesterName = monthName;
                }
            }
            return semesterName;
        }

        private String getHrName(DB2UDB db2) throws SQLException {
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT HR_NAME ");
            sql.append(" FROM SCHREG_REGD_HDAT ");
            sql.append(" WHERE YEAR = '" + _year + "' ");
            sql.append("   AND SEMESTER = '" + _semester + "' ");
            sql.append("   AND GRADE = '" + _grade + "' ");
            sql.append("   AND HR_CLASS = '" + _hrClass + "' ");
            
            String hrName = null;
            PreparedStatement ps = db2.prepareStatement(sql.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                hrName = rs.getString("HR_NAME");
            }
            return hrName;
        }
    }
}

// eof

