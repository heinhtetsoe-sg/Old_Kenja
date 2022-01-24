//kanji=漢字
/*
 * $Id: 9f417f63f6a2ca7fb44b9521a29bfb1d1daf7887 $
 *
 * 作成日: 2010/05/21 22:22:22 - JST
 * 作成者: nakamoto
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


public class KNJC031K {

    private static final Log log = LogFactory.getLog(KNJC031K.class);

    Param _param;

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
        final String form;
        if ("true".equals(_param._useVirus) && "true".equals(_param._useKoudome)) {
            form = "KNJC031K_3.frm";
        } else if ("true".equals(_param._useVirus) || "true".equals(_param._useKoudome)) {
            form = "KNJC031K_2.frm";
        } else {
            form = "KNJC031K.frm";
        }
        svf.VrSetForm(form, 4);

        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean hasData = false;

        try {
            svf.VrsOut("HR_CLASS", _param._hrName);
            svf.VrsOut("NENDO", KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度");
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._ctrlDate));
            svf.VrsOut("SEMESTER", _param._semesterName);
            svf.VrsOut("ITEM1", _param._item1);
            svf.VrsOut("ITEM2", _param._item2);
            svf.VrsOut("ITEM3", _param._item3);
            if ("true".equals(_param._useVirus) && "true".equals(_param._useKoudome)) {
                svf.VrsOut("ITEM_SUS1", "法止");
                svf.VrsOut("ITEM_SUS2", "交止");
                svf.VrsOut("ITEM_SUS3", "伝染");
                svf.VrsOut("ACCUM_ITEM_SUS1", "法止");
                svf.VrsOut("ACCUM_ITEM_SUS2", "交止");
                svf.VrsOut("ACCUM_ITEM_SUS3", "伝染");
            } else if ("true".equals(_param._useKoudome)) {
                svf.VrsOut("ITEM_SUS1", "法止");
                svf.VrsOut("ITEM_SUS2", "交止");
                svf.VrsOut("ACCUM_ITEM_SUS1", "法止");
                svf.VrsOut("ACCUM_ITEM_SUS2", "交止");
            } else if ("true".equals(_param._useVirus)) {
                svf.VrsOut("ITEM_SUS1", "出停");
                svf.VrsOut("ITEM_SUS2", "伝染");
                svf.VrsOut("ACCUM_ITEM_SUS1", "出停");
                svf.VrsOut("ACCUM_ITEM_SUS2", "伝染");
            }

            final String sql = selectAttendQuery();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                svf.VrsOut("ATTENDNO", Integer.valueOf(rs.getString("ATTENDNO")).toString());
                svf.VrsOut("NAME", rs.getString("NAME"));
                svf.VrsOut("APPOINTED_DAY", getAttendZeroHyoji(rs.getString("APPOINTED_DAY")));
                svf.VrsOut("LESSON", getAttendZeroHyoji(rs.getString("LESSON")));
                svf.VrsOut("OFFDAYS", getAttendZeroHyoji(rs.getString("OFFDAYS")));
                svf.VrsOut("ABROAD", getAttendZeroHyoji(rs.getString("ABROAD")));
                svf.VrsOut("ABSENT", getAttendZeroHyoji(rs.getString("ABSENT")));
                if ("true".equals(_param._useVirus) && "true".equals(_param._useKoudome)) {
                    svf.VrsOut("SUS1", getAttendZeroHyoji(rs.getString("SUSPEND")));
                    svf.VrsOut("SUS2", getAttendZeroHyoji(rs.getString("KOUDOME")));
                    svf.VrsOut("SUS3", getAttendZeroHyoji(rs.getString("VIRUS")));
                } else if ("true".equals(_param._useKoudome)) {
                    svf.VrsOut("SUS1", getAttendZeroHyoji(rs.getString("SUSPEND")));
                    svf.VrsOut("SUS2", getAttendZeroHyoji(rs.getString("KOUDOME")));
                } else if ("true".equals(_param._useVirus)) {
                    svf.VrsOut("SUS1", getAttendZeroHyoji(rs.getString("SUSPEND")));
                    svf.VrsOut("SUS2", getAttendZeroHyoji(rs.getString("VIRUS")));
                } else {
                    svf.VrsOut("SUSPEND", getAttendZeroHyoji(rs.getString("SUSPEND")));
                }
                svf.VrsOut("MOURNING", getAttendZeroHyoji(rs.getString("MOURNING")));

                svf.VrsOut("PRESENT", getAttendZeroHyoji(rs.getString("CLASSDAYS2")));
                svf.VrsOut("ABSENCE1", getAttendZeroHyoji(rs.getString("SICK")));
                svf.VrsOut("ABSENCE2", getAttendZeroHyoji(rs.getString("NOTICE")));
                svf.VrsOut("ABSENCE3", getAttendZeroHyoji(rs.getString("NONOTICE")));
                svf.VrsOut("ATTEND", getAttendZeroHyoji(rs.getString("CLASSDAYS3")));
                svf.VrsOut("LATE", getAttendZeroHyoji(rs.getString("LATE")));
                svf.VrsOut("EARLY", getAttendZeroHyoji(rs.getString("EARLY")));

                svf.VrsOut("ACCUM_LESSON", rs.getString("SUM_CLASSDAYS"));
                if ("true".equals(_param._useVirus) && "true".equals(_param._useKoudome)) {
                    svf.VrsOut("ACCUM_SUS1", rs.getString("SUM_SUSPEND"));
                    svf.VrsOut("ACCUM_SUS2", rs.getString("SUM_KOUDOME"));
                    svf.VrsOut("ACCUM_SUS3", rs.getString("SUM_VIRUS"));
                } else if ("true".equals(_param._useKoudome)) {
                    svf.VrsOut("ACCUM_SUS1", rs.getString("SUM_SUSPEND"));
                    svf.VrsOut("ACCUM_SUS2", rs.getString("SUM_KOUDOME"));
                } else if ("true".equals(_param._useVirus)) {
                    svf.VrsOut("ACCUM_SUS1", rs.getString("SUM_SUSPEND"));
                    svf.VrsOut("ACCUM_SUS2", rs.getString("SUM_VIRUS"));
                } else {
                    svf.VrsOut("ACCUM_SUSPEND", rs.getString("SUM_SUSPEND"));
                }
                svf.VrsOut("ACCUM_MOURNING", rs.getString("SUM_MOURNING"));
                svf.VrsOut("ACCUM_PRESENT", rs.getString("SUM_CLASSDAYS2"));
                svf.VrsOut("ACCUM_ABSENCE1", rs.getString("SUM_SICK_ONLY"));
                svf.VrsOut("ACCUM_ABSENCE2", rs.getString("SUM_NOTICE_ONLY"));
                svf.VrsOut("ACCUM_ABSENCE3", rs.getString("SUM_NONOTICE_ONLY"));
                svf.VrsOut("ACCUM_ABSENCE", rs.getString("SUM_SICK"));
                svf.VrsOut("ACCUM_ATTEND", rs.getString("SUM_CLASSDAYS3"));
                svf.VrsOut("ACCUM_LATE", rs.getString("SUM_LATE"));
                svf.VrsOut("ACCUM_EARLY", rs.getString("SUM_EARLY"));

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

    //プロパティ「use_Attend_zero_hyoji」= '1'のとき、データの通りにゼロ、NULLを表示
    //それ以外のとき、ゼロは表示しない
    private String getAttendZeroHyoji(final String val) {
        if ("1".equals(_param._use_Attend_zero_hyoji)) return val;
        if ("0".equals(val) || "0.0".equals(val)) return "";
        return val;
    }

    /**
     * php画面 KNJC031KQuery.incからコピー
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
        if ("true".equals(_param._useKoudome)) {
            stb.append("     AD.KOUDOME, ");
        }
        if ("true".equals(_param._useVirus)) {
            stb.append("     AD.VIRUS, ");
        }
        stb.append("     AD.MOURNING, ");          //忌引
        stb.append("     VALUE(AD.LESSON, 0) - VALUE(AD.SUSPEND, 0) - VALUE(AD.MOURNING, 0) - VALUE(AD.OFFDAYS, 0) - VALUE(AD.ABROAD, 0) ");
        if ("true".equals(_param._useKoudome)) {
            stb.append("     - VALUE(AD.KOUDOME, 0) ");
        }
        if ("true".equals(_param._useVirus)) {
            stb.append("     - VALUE(AD.VIRUS, 0) ");
        }
        if ("1".equals(_param._knjSchoolMst._semOffDays)) {
            stb.append("     + VALUE(AD.OFFDAYS, 0) ");
        }
        stb.append("     AS CLASSDAYS2, "); //出席すべき数
        stb.append("     AD.SICK, ");              //欠席
        stb.append("     AD.NOTICE, ");            //欠席
        stb.append("     AD.NONOTICE, ");          //欠席
        stb.append("     VALUE(AD.LESSON, 0) - VALUE(AD.SUSPEND, 0) - VALUE(AD.MOURNING, 0) - VALUE(AD.OFFDAYS, 0) - VALUE(AD.ABROAD, 0) ");
        if ("true".equals(_param._useKoudome)) {
            stb.append("     - VALUE(AD.KOUDOME, 0) ");
        }
        if ("true".equals(_param._useVirus)) {
            stb.append("     - VALUE(AD.VIRUS, 0) ");
        }
        stb.append("               - VALUE(AD.SICK, 0) - VALUE(AD.NOTICE, 0) - VALUE(AD.NONOTICE, 0) ");
        stb.append("     AS CLASSDAYS3, "); //出席数
        stb.append("     AD.LATE, ");              //遅刻
        stb.append("     AD.EARLY, ");             //早退
        stb.append("     SUMAD.SUM_CLASSDAYS, ");  //累積・授業数
        stb.append("     SUMAD.SUM_SUSPEND, ");    //累積・出停
        if ("true".equals(_param._useKoudome)) {
            stb.append("     SUMAD.SUM_KOUDOME, ");
        }
        if ("true".equals(_param._useVirus)) {
            stb.append("     SUMAD.SUM_VIRUS, ");
        }
        stb.append("     SUMAD.SUM_MOURNING, ");   //累積・忌引
        stb.append("     VALUE(SUMAD.SUM_CLASSDAYS, 0) - VALUE(SUMAD.SUM_SUSPEND, 0) - VALUE(SUMAD.SUM_MOURNING, 0) - VALUE(SUMAD.SUM_OFFDAYS, 0) - VALUE(SUMAD.SUM_ABROAD, 0) ");
        if ("true".equals(_param._useKoudome)) {
            stb.append("     - VALUE(SUMAD.SUM_KOUDOME, 0) ");
        }
        if ("true".equals(_param._useVirus)) {
            stb.append("     - VALUE(SUMAD.SUM_VIRUS, 0) ");
        }
        if ("1".equals(_param._knjSchoolMst._semOffDays)) {
            stb.append("     + VALUE(SUMAD.SUM_OFFDAYS, 0) ");
        }
        stb.append("     AS SUM_CLASSDAYS2, "); //累積・出席すべき数
        stb.append("     SUMAD.SUM_SICK AS SUM_SICK_ONLY, ");
        stb.append("     SUMAD.SUM_NOTICE AS SUM_NOTICE_ONLY, ");
        stb.append("     SUMAD.SUM_NONOTICE AS SUM_NONOTICE_ONLY, ");
        stb.append("     VALUE(SUMAD.SUM_SICK, 0) + VALUE(SUMAD.SUM_NOTICE, 0) + VALUE(SUMAD.SUM_NONOTICE, 0) ");
        if ("1".equals(_param._knjSchoolMst._semOffDays)) {
            stb.append("     + VALUE(SUMAD.SUM_OFFDAYS, 0) ");
        }
        stb.append("     AS SUM_SICK, ");  //累積・欠席数
        stb.append("     ((VALUE(SUMAD.SUM_CLASSDAYS, 0) ");
        stb.append("      - (VALUE(SUMAD.SUM_SUSPEND, 0) ");
        if ("true".equals(_param._useKoudome)) {
            stb.append("     + VALUE(SUMAD.SUM_KOUDOME, 0) ");
        }
        if ("true".equals(_param._useVirus)) {
            stb.append("     + VALUE(SUMAD.SUM_VIRUS, 0) ");
        }
        stb.append("         + VALUE(SUMAD.SUM_MOURNING, 0) + VALUE(SUMAD.SUM_OFFDAYS, 0) + VALUE(SUMAD.SUM_ABROAD, 0))) ");
        stb.append("      - (VALUE(SUMAD.SUM_SICK, 0) + VALUE(SUMAD.SUM_NOTICE, 0) + VALUE(SUMAD.SUM_NONOTICE, 0))) AS SUM_CLASSDAYS3, ");  //累積・出席数

        stb.append("     SUMAD.SUM_LATE, "); //累積・遅刻
        stb.append("     SUMAD.SUM_EARLY "); //累積・早退
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
        if ("1".equals(_param._useSchool_KindField)) {
	        stb.append("     SCHREG_REGD_GDAT SGD ");
	        stb.append(" ON SD.YEAR = SGD.YEAR ");
	        stb.append("   AND SD.GRADE = SGD.GRADE ");
	        stb.append(" LEFT OUTER JOIN ");
	    }
        stb.append("     APPOINTED_DAY_MST AM ");
        stb.append(" ON AD.YEAR = AM.YEAR ");
        stb.append("   AND AD.MONTH = AM.MONTH ");
        stb.append("   AND AD.SEMESTER = AM.SEMESTER ");
        if ("1".equals(_param._useSchool_KindField)) {
        	stb.append("   AND SGD.SCHOOL_KIND = AM.SCHOOL_KIND ");
        }

        stb.append(" LEFT OUTER JOIN ");
        stb.append("     (SELECT ");
        stb.append("         SCHREGNO, ");
        stb.append("         SUM(LESSON) AS SUM_CLASSDAYS, ");
        stb.append("         SUM(OFFDAYS) AS SUM_OFFDAYS, ");
        stb.append("         SUM(ABROAD) AS SUM_ABROAD, ");
        stb.append("         SUM(SUSPEND) AS SUM_SUSPEND, ");
        if ("true".equals(_param._useKoudome)) {
            stb.append("         SUM(KOUDOME) AS SUM_KOUDOME, ");
        }
        if ("true".equals(_param._useVirus)) {
            stb.append("         SUM(VIRUS) AS SUM_VIRUS, ");
        }
        stb.append("         SUM(MOURNING) AS SUM_MOURNING, ");
        stb.append("         SUM(SICK) AS SUM_SICK, ");
        stb.append("         SUM(NOTICE) AS SUM_NOTICE, ");
        stb.append("         SUM(NONOTICE) AS SUM_NONOTICE, ");
        stb.append("         SUM(LATE) AS SUM_LATE, ");
        stb.append("         SUM(EARLY) AS SUM_EARLY ");
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

        // attend_semes_datにデータが存在しない場合でも、表示可能へ変更
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

    /** パラメータ取得 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 69939 $");
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
        private final String _useVirus;
        private final String _useSchool_KindField;
        private final String _useKoudome;
        private final String _use_Attend_zero_hyoji;

        private final KNJSchoolMst _knjSchoolMst;
        private final String _hrName;
        private String _item1;
        private String _item2;
        private String _item3;

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
            _useVirus = request.getParameter("useVirus");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _useKoudome = request.getParameter("useKoudome");
            setSickDiv(db2);
            _use_Attend_zero_hyoji = request.getParameter("use_Attend_zero_hyoji");
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

        private void setSickDiv(DB2UDB db2) throws SQLException {
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT ");
            sql.append("     NAMECD2, ");
            sql.append("     NAME1 ");
            sql.append(" FROM ");
            sql.append("     V_NAME_MST ");
            sql.append(" WHERE ");
            sql.append("     YEAR = '" + _year + "' ");
            sql.append("     AND NAMECD1 = 'C001' ");
            sql.append("     AND NAMECD2 IN ('4', '5', '6') ");
            sql.append(" ORDER BY ");
            sql.append("     NAMECD2 ");

            _item1 = "";
            _item2 = "";
            _item3 = "";
            PreparedStatement ps = db2.prepareStatement(sql.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String cd = rs.getString("NAMECD2");
                if ("4".equals(cd)) _item1 = rs.getString("NAME1");
                if ("5".equals(cd)) _item2 = rs.getString("NAME1");
                if ("6".equals(cd)) _item3 = rs.getString("NAME1");
            }
        }
    }
}

// eof

