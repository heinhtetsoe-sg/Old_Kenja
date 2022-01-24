// kanji=漢字
/*
 * $Id: e23bda74f7af5ee5808d4bc944a8895cc3a8d57c $
 *
 */
package servletpack.KNJC;

import java.io.IOException;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/*
 *  学校教育システム 賢者 [出欠管理] 欠課状況集計表
 */

public class KNJC165 {

    private static final Log log = LogFactory.getLog(KNJC165.class);

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
            log.debug(" $Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $ ");
            KNJServletUtils.debugParam(request, log);
            param = new Param(request, db2);

            // 印刷処理
            printMain(db2, param, svf);
            
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
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
    private void printMain(
            final DB2UDB db2,
            final Param param,
            final Vrw32alp svf
    ) {
        final List grades = new ArrayList(); // 学年のリスト
        final Map gradeClassMap = new TreeMap(); // 学年ごとのHRクラスのリストマップ
        
        //学籍のSQL
        setHrClasses(db2, param, grades, gradeClassMap);
        
        final String form = "KNJC165.frm";

        final List hrClassesAll = new ArrayList();
        for(final Iterator it = grades.iterator(); it.hasNext();) {
            final String grade = (String) it.next();
            final List hrClasses = (List) gradeClassMap.get(grade);
            hrClassesAll.addAll(hrClasses);
        }
        
        final List pageList = getPageList(hrClassesAll, 50);
        for (final Iterator it = pageList.iterator(); it.hasNext();) {
            final List hrClasses = (List) it.next();
            svf.VrSetForm(form, 4);
            
            svf.VrsOut("NENDO", KenjaProperties.gengou(Integer.parseInt(param._year)) + "年度"); // 年度
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(param._loginDate)); // 作成日
            
            for (final Iterator hrit = hrClasses.iterator(); hrit.hasNext();) {
                final HrClass hrClass = (HrClass) hrit.next();

                svf.VrsOut("HR_NAME", hrClass._hrname); // クラス名
                for (final Iterator mit = param._monthList.iterator(); mit.hasNext();) {
                    final String month = (String) mit.next();
                    final String i = String.valueOf(Integer.parseInt(month));
                    final AttendanceCount a = (AttendanceCount) hrClass._hrClassAttendMonthMap.get(month);
                    if (null == a) {
                        continue;
                    }
                            
                    svf.VrsOut("KEKKA" + i, String.valueOf(a._sick)); // 欠席
                }
                
                svf.VrEndRecord();
                _hasdata = true;
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
            stb.append("     HR_CLASS, ");
            stb.append("     HR_NAME ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_HDAT T1 ");
            stb.append("     INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ");
            stb.append("       AND T2.SCHOOL_KIND = '" + param._schoolKind + "' ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND SEMESTER = '" + param._semester + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.GRADE, ");
            stb.append("     HR_CLASS ");
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
                
                final HrClass hrClass = new HrClass(rs.getString("GRADE"), rs.getString("HR_CLASS"), rs.getString("HR_NAME"));
                
                hrClasses.add(hrClass);
                gradeHrClassMap.put(rs.getString("GRADE") + rs.getString("HR_CLASS"), hrClass);
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        }
        
        loadMonths(db2, param, grades, gradeClassMap);
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
                
                final String sql = AttendAccumulate.getAttendSubclassSql(
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
                        hrClass._hrClassAttendMonthMap.put(month, getHrClassAttend(param, ps, hrClass));
                    }
                }
                DbUtils.closeQuietly(ps);
            }
            
        } catch (Exception ex) {
            log.error("svfPrintGrade exception!", ex);
        } finally {
            DbUtils.closeQuietly(ps);
            db2.commit();
        }
    }

    private static AttendanceCount getHrClassAttend(final Param param, PreparedStatement ps, final HrClass hrClass) throws SQLException {
        ResultSet rs = null;
        final AttendanceCount total = new AttendanceCount();
        try {
            rs = ps.executeQuery();
            while(rs.next()) {
                if (!"9".equals(rs.getString("SEMESTER"))) {
                    continue;
                }
                total.addAttend(rs);
            }
        } finally {
            DbUtils.closeQuietly(rs);
        }
        return total;
    }

    private static class Param {
        final String _year;
        final String _semester;
        final String _date;
        final String _loginDate;
        final String _schoolKind;
        final String _month;
        final List _monthList = new ArrayList();
        final String _z010;

        final Map _attendParamMapMonth;
        
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
            _z010 = getZ010(db2, _year);
            
            final DecimalFormat df2 = new DecimalFormat("00");
            _attendParamMapMonth = new HashMap();
            _attendParamMapMonth.put("DB2UDB", db2);
            _attendParamMapMonth.put("HttpServletRequest", request);
            _attendParamMapMonth.put("grade", "?");
            _attendParamMapMonth.put("hrClass", "?");
            
            for (int m = 4; m <= month + (month <= 3 ? 12 : 0); m++) {
                _monthList.add(df2.format(m - (m > 12 ? 12 : 0)));
            }
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
        /** 表示する授業時数(各クラスごとの生徒の授業時数の最大値) */
        private int _displayLesson;
        /** 授業時数 */
        private int _lesson;
        /** 出席すべき時数 */
        private int _mlesson;
        /** 出席時数 */
        private int _attend;
        /** 欠席時数 */
        private int _sick;
        /** 遅刻時数 */
        private int _late;
        /** 早退時数 */
        private int _early;
        /** 忌引時数 */
        private int _mourning;
        /** 出停時数 */
        private int _suspend;
        /** 公欠時数 */
        private int _absent;
        /** 休学時数 */
        private int _offdays;
        
        public void addAttend(ResultSet rs) throws SQLException {
            int lesson = rs.getInt("LESSON"); // 授業時数
            int offdays = rs.getInt("OFFDAYS"); // 休学時数
            int sick = rs.getInt("SICK2"); // 欠席時数
            int suspend = rs.getInt("SUSPEND");
            suspend += rs.getInt("VIRUS");
            suspend += rs.getInt("KOUDOME");
            int special = rs.getInt("MOURNING") + suspend; // 特別欠席
            int mlesson = lesson - special; // 出席すべき時数
            _displayLesson = Math.max(lesson, _displayLesson);
            _lesson += lesson;
            _mlesson += mlesson;
            _sick += sick;
            _attend += mlesson - sick; // 出席時数 = 出席すべき時数 - 欠席時数
            _late += rs.getInt("LATE");
            _early += rs.getInt("EARLY");
            _mourning += rs.getInt("MOURNING");
            _suspend += suspend;
            _absent += rs.getInt("ABSENT");
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
    }
    
    private static class HrClass {
        final String _grade;
        final String _hrClass;
        final String _hrname;
        Map _hrClassAttendMonthMap = new HashMap();
        HrClass(final String grade,
                final String hrClass,
                final String hrname
                ) {
            _grade = grade;
            _hrClass = hrClass;
            _hrname = hrname;
        }
    }
}
