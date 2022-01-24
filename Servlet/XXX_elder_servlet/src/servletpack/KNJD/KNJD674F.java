// kanji=漢字
/*
 * $Id: 3c99383b7ec40004397981b5b5be16429306ae36 $
 */
package servletpack.KNJD;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * 学校教育システム 賢者 [成績管理]  武蔵野東 定期考査用平均点一覧 
 */
public class KNJD674F {
    private static final Log log = LogFactory.getLog(KNJD674F.class);

    private final String HR_CLASS_ALL = "999";
    private final String HANDICAP_ALL = "---";

    private boolean _hasData;
    
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {
        Vrw32alp svf = null;
        DB2UDB db2 = null;
        try {
            svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            response.setContentType("application/pdf");

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            // パラメータの取得
            final Param param = createParam(request, db2);

            printMain(db2, svf, param);

        } catch (final Exception ex) {
            log.error("error! ", ex);
        } finally {
            try {
                if (null != db2) {
                    db2.commit();
                    db2.close();
                }
            } catch (Exception ex) {
                log.error("db close error!", ex);
            }

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            svf.VrQuit();
        }
    }
    
    private void printMain(final DB2UDB db2, final Vrw32alp svf, final Param param) {
        
        final List allHrList = HR.getHRList(db2, param);
        final Map avgMap = getRecorScoreSubclassAvgMap(db2, param);
        if (allHrList.size() == 0 || avgMap.size() == 0) {
            return;
        }
        
        final String form = "KNJD674F.frm";
        svf.VrSetForm(form, 4);

        svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(param._year)) + "年度　"+ StringUtils.defaultString(param._testitemname) + "試験結果表"); // タイトル
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(param._loginDate)); // 日付
        svf.VrsOut("SCHOOL_NAME", param._schoolName); // 学校名
        
        for (int i = 0; i < param._classKeyMapList.size(); i++) {
            final Map m = (Map) param._classKeyMapList.get(i);
            final String classname = (String) m.get("CLASSNAME");
            svf.VrsOut("CLASS_NAME" + String.valueOf(i + 1), classname); // 教科名
        }

        final List gradeList = groupByGrade(allHrList);
        for (int gi = 0; gi < gradeList.size(); gi++) {
            final List hrList = (List) gradeList.get(gi);

            String grade = null;
            for (int hri = 0; hri < hrList.size(); hri++) {
                final HR hr = (HR) hrList.get(hri);
                grade = hr._grade;
            
                svf.VrsOut("HR_NAME", hr._hrNameabbv); // クラス名
                
                for (int i = 0; i < param._classKeyMapList.size(); i++) {
                    final Map m = (Map) param._classKeyMapList.get(i);
                    final Map avgm = (Map) avgMap.get(m.get("CLASSKEY"));
                    if (null == avgm) {
                        continue;
                    }
                    svf.VrsOut("HEALTHY_AVE1_" + String.valueOf(i + 1), sishaGonyu(avgm, getAverageKey(grade, hr._hrClass, "001"))); // 健常児平均
                    svf.VrsOut("TOTAL_AVE1_" + String.valueOf(i + 1), sishaGonyu(avgm, getAverageKey(grade, hr._hrClass, HANDICAP_ALL))); // 全体の平均
                }
                
                svf.VrEndRecord();
            }
        
            svf.VrsOut("GRADE_AVE_NAME", "学年平均"); // 学年平均名称
            for (int i = 0; i < param._classKeyMapList.size(); i++) {
                final Map m = (Map) param._classKeyMapList.get(i);
                final Map avgm = (Map) avgMap.get(m.get("CLASSKEY"));
                if (null == avgm) {
                    continue;
                }
                svf.VrsOut("HEALTHY_AVE2_" + String.valueOf(i + 1), sishaGonyu(avgm, getAverageKey(grade, HR_CLASS_ALL, "001"))); // 健常児平均
                svf.VrsOut("TOTAL_AVE2_" + String.valueOf(i + 1), sishaGonyu(avgm, getAverageKey(grade, HR_CLASS_ALL, HANDICAP_ALL))); // 全体の平均
            }
            svf.VrEndRecord();
            _hasData = true;
        }
    }
    
    private static String sishaGonyu(final Map m, final String key) {
        final BigDecimal bd = (BigDecimal) m.get(key); 
        return null == bd ? null : bd.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }
    
    private static Map getListedMap(final List list, final String key, final String value) {
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Map m = (Map) it.next();
            final Object mapValue = m.get(key);
            if (null != mapValue && mapValue.equals(value)) {
                return m;
            }
        }
        
        final Map m = new HashMap();
        m.put(key, value);
        list.add(m);
        return m;
    }

    private List groupByGrade(final List hrList) {
        final List rtn = new ArrayList();
        List current = null;
        String grade = null;
        for (final Iterator it = hrList.iterator(); it.hasNext();) {
            final HR hr = (HR) it.next();
            if (null == current || !hr._grade.equals(grade)) {
                current = new ArrayList();
                rtn.add(current);
                grade = hr._grade;
            }
            current.add(hr);
        }
        return rtn;
    }

    private static Map getMappedMap(final Map map, final String key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap());
        }
        return (Map) map.get(key1);
    }

    private Map getRecorScoreSubclassAvgMap(final DB2UDB db2, final Param param) {
        final Map map = new HashMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getRecorScoreSubclassAvg(param);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                getMappedMap(map, rs.getString("CLASSKEY")).put(getAverageKey(rs.getString("GRADE"), rs.getString("HR_CLASS"), rs.getString("HANDICAP")), rs.getBigDecimal("AVG"));
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return map;
    }

    private static String getAverageKey(final String grade, final String hrClass, final String handicap) {
        return grade + "-" + hrClass + "-" + handicap;
    }

    private String getRecorScoreSubclassAvg(final Param param) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH ALL_SCORE AS ( ");
        stb.append(" SELECT T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     VALUE(T2.HANDICAP, '001') AS HANDICAP, ");
        stb.append("     T3.CLASSCD || '-' || T3.SCHOOL_KIND AS CLASSKEY, ");
        stb.append("     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("     T4.CLASSNAME, ");
        stb.append("     T3.SCORE ");
        stb.append(" FROM RECORD_SCORE_DAT T3 ");
        stb.append(" INNER JOIN SCHREG_REGD_DAT T1 ON T1.YEAR = T3.YEAR ");
        if ("9".equals(param._semester)) {
            stb.append("     AND T1.SEMESTER = '" + param._ctrlSeme + "' ");
        } else {
            stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
        }
        stb.append("     AND T1.SCHREGNO = T3.SCHREGNO ");
        stb.append(" INNER JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = T1.YEAR AND REGDG.GRADE = T1.GRADE AND REGDG.SCHOOL_KIND = 'H' ");
        stb.append(" INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append(" INNER JOIN CLASS_MST T4 ON T4.CLASSCD = T3.CLASSCD AND T4.SCHOOL_KIND = T3.SCHOOL_KIND ");
        stb.append(" WHERE ");
        stb.append("   T3.YEAR = '" + param._year + "' ");
        stb.append("   AND T3.SEMESTER = '" + param._semester + "' ");
        stb.append("   AND T3.TESTKINDCD || T3.TESTITEMCD || T3.SCORE_DIV = '" + param._testcd + "' ");
        stb.append("   AND T3.SCORE IS NOT NULL ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.GRADE ");
        stb.append("     , VALUE(T1.HR_CLASS, '" + HR_CLASS_ALL + "') AS HR_CLASS ");
        stb.append("     , VALUE(T1.HANDICAP, '" + HANDICAP_ALL + "') AS HANDICAP ");
        stb.append("     , T1.CLASSKEY ");
        stb.append("     , T1.SUBCLASSCD ");
        stb.append("     , T1.CLASSNAME ");
        stb.append("     , SUM(T1.SCORE) AS SUM ");
        stb.append("     , COUNT(T1.SCORE) AS COUNT ");
        stb.append("     , AVG(T1.SCORE * 1.0) AS AVG ");
        stb.append(" FROM ALL_SCORE T1 ");
        stb.append(" INNER JOIN (SELECT DISTINCT GRADE, HR_CLASS FROM ALL_SCORE WHERE HANDICAP = '001') T2 ON T2.GRADE = T1.GRADE AND T2.HR_CLASS = T1.HR_CLASS ");
        stb.append(" GROUP BY ");
        stb.append("     T1.CLASSKEY ");
        stb.append("     , T1.SUBCLASSCD ");
        stb.append("     , T1.CLASSNAME ");
        stb.append("     , T1.GRADE ");
        stb.append("     , GROUPING SETS( ");
        stb.append("         (T1.HR_CLASS, T1.HANDICAP) ");
        stb.append("       , (T1.HR_CLASS) ");
        stb.append("       , (T1.HANDICAP) ");
        stb.append("       , () ");
        stb.append("       ) ");
        stb.append(" ORDER BY ");
        stb.append("     T1.CLASSKEY ");
        stb.append("     , T1.GRADE");
        stb.append("     , VALUE(T1.HR_CLASS, '" + HR_CLASS_ALL + "') ");
        return stb.toString();
    }

    private static class HR {
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _hrNameabbv;

        HR(
            final String grade,
            final String hrClass,
            final String hrName,
            final String hrNameabbv
        ) {
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _hrNameabbv = hrNameabbv;
        }

        public static List getHRList(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String grade = rs.getString("GRADE");
                    final String hrClass = rs.getString("HR_CLASS");
                    final String hrName = rs.getString("HR_NAME");
                    final String hrNameabbv = rs.getString("HR_NAMEABBV");
                    final HR hr = new HR(grade, hrClass, hrName, hrNameabbv);
                    list.add(hr);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.GRADE ");
            stb.append("     , T1.HR_CLASS ");
            stb.append("     , T1.HR_NAME ");
            stb.append("     , T1.HR_NAMEABBV ");
            stb.append(" FROM SCHREG_REGD_HDAT T1 ");
            stb.append(" INNER JOIN SCHREG_REGD_DAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("     AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("     AND T2.GRADE = T1.GRADE ");
            stb.append("     AND T2.HR_CLASS = T1.HR_CLASS ");
            stb.append(" INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T2.SCHREGNO AND VALUE(T3.HANDICAP, '001') = '001' ");
            stb.append(" INNER JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = T1.YEAR AND REGDG.GRADE = T1.GRADE ");
            stb.append("     AND REGDG.SCHOOL_KIND = 'H' ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS ");
            return stb.toString();
        }
    }

    private Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(request, db2);
        return param;
    }

    private static class Param {
        final String _year;
        final String _semester;
        final String _ctrlSeme;
        final String _loginDate;
        final String _testcd;
        final String _testitemname;
        final String _schoolName;
        final List _classKeyMapList;
        
        Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSeme = request.getParameter("CTRL_SEME");
            _loginDate = request.getParameter("LOGIN_DATE");
            _testcd = request.getParameter("TESTCD");
            _testitemname = getTestitemname(db2);
            _schoolName = getSchoolName(db2);
            _classKeyMapList = getPrintClassKeyMapList(db2);
        }
        
        private String getSchoolName(final DB2UDB db2) {
            String sql = " SELECT SCHOOLNAME2 FROM SCHOOL_MST WHERE YEAR = '" + _year + "' ";
            log.debug(" schoolMst sql = " + sql.toString());
            PreparedStatement ps = null;
            ResultSet rs = null;
            String schoolname = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    schoolname = rs.getString("SCHOOLNAME2");
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return StringUtils.defaultString(schoolname);
        }
        
        private List getPrintClassKeyMapList(
                final DB2UDB db2
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            List classKeyMapList = new ArrayList();
            try {
                final String sql = "SELECT NAME1, T2.CLASSNAME FROM V_NAME_MST T1 "
                                  + "INNER JOIN CLASS_MST T2 ON T1.NAME1 = T2.CLASSCD || '-' || T2.SCHOOL_KIND "
                                  + "WHERE T1.YEAR = '" + _year + "' AND T1.NAMECD1 = 'D066' AND T1.NAME1 IS NOT NULL "
                                  + "ORDER BY T1.NAMECD2 "
                                 ;
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Map m = new HashMap();
                    m.put("CLASSKEY", rs.getString("NAME1"));
                    m.put("CLASSNAME", rs.getString("CLASSNAME"));
                    classKeyMapList.add(m);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return classKeyMapList;
        }
        
        private String getTestitemname(
                final DB2UDB db2
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String testitemname = null;
            try {
                final String sql = "SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW_SDIV T1 WHERE T1.YEAR = '" + _year + "' "
                                 +  "  AND T1.SEMESTER = '" + _semester + "' "
                                 +  "  AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + _testcd + "' "
                                 ;
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    testitemname = rs.getString("TESTITEMNAME");
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return testitemname;
        }
    }
}
