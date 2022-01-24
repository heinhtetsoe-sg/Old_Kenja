// kanji=漢字
/*
 * $Id: 186930bdd2c4c20adc23ad50458f566f8335399f $
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

import javax.servlet.ServletException;
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
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * 学校教育システム 賢者 [成績管理]  武蔵野東 就職模擬試験結果表
 */
public class KNJD675F {
    private static final Log log = LogFactory.getLog(KNJD675F.class);

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
            Param param = createParam(request, db2);

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
    
    private void printMain(
            final DB2UDB db2,
            final Vrw32alp svf,
            final Param param
    ) {
        final List allHrList = HR.getHRList(db2, param);
        final List avgMapList = getProficiencyAvgList(db2, param);
        if (allHrList.size() == 0 || avgMapList.size() == 0) {
            return;
        }

        final String form = "KNJD675F.frm";
        svf.VrSetForm(form, 4);
        
        svf.VrsOut("TITLE", KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(param._year)) + "年度　"+ StringUtils.defaultString(param._testitemname) + "結果表"); // タイトル
        svf.VrsOut("DATE", KNJ_EditDate.getAutoFormatDate(db2, param._loginDate)); // 日付
        svf.VrsOut("SCHOOL_NAME", param._schoolName); // 学校名

        for (int i = 0; i < avgMapList.size(); i++) {
            final Map m = (Map) avgMapList.get(i);
            final String classname = (String) m.get("CLASSNAME");
            svf.VrsOut("CLASS_NAME" + String.valueOf(i + 1), classname); // 教科名
        }
        
        for (int hri = 0; hri < allHrList.size(); hri++) {
            final HR hr = (HR) allHrList.get(hri);
        
            svf.VrsOut("HR_NAME", hr._hrNameabbv); // クラス名
            
            for (int i = 0; i < avgMapList.size(); i++) {
                final Map m = (Map) avgMapList.get(i);
                svf.VrsOut("HEALTHY_AVE1_" + String.valueOf(i + 1), sishaGonyu(m, getDataKey(hr._grade, hr._hrClass, "AVG"))); // 健常児平均
                getMappedList(m, "SCORELIST").add(m.get(getDataKey(hr._grade, hr._hrClass, "SCORE")));
                getMappedList(m, "COUNTLIST").add(m.get(getDataKey(hr._grade, hr._hrClass, "COUNT")));
            }
            
            svf.VrEndRecord();
        }
    
        svf.VrsOut("GRADE_AVE_NAME", "全体"); // 全体平均名称
        for (int i = 0; i < avgMapList.size(); i++) {
            final Map m = (Map) avgMapList.get(i);
 
            // 全体の平均がDBにないので帳票で計算する
            final BigDecimal sum = sum(getMappedList(m, "SCORELIST")); // 「クラスごとの合計」の合計
            final BigDecimal count = sum(getMappedList(m, "COUNTLIST")); // 「クラスごとの人数」の合計
            if (null != sum && null != count) {
                final BigDecimal dAvg = sum.divide(count, 1, BigDecimal.ROUND_HALF_UP);
                final String tuishiLine = dAvg.divide(new BigDecimal(2), 1, BigDecimal.ROUND_DOWN).toString(); // 平均点 / 2
                
                svf.VrsOut("AVE" + String.valueOf(i + 1), dAvg.toString()); // 平均
                svf.VrsOut("MAKEUP" + String.valueOf(i + 1), tuishiLine); // 追試ライン
            }
        }
        svf.VrEndRecord();
        _hasData = true;
    }

    private static BigDecimal sum(final List scoreList) {
        BigDecimal d = new BigDecimal(0);
        boolean add = false;
        for (final Iterator it = scoreList.iterator(); it.hasNext();) {
            final BigDecimal score = (BigDecimal) it.next();
            if (null == score) {
                continue;
            }
            d = d.add(score);
            add = true;
        }
        if (!add) {
            return null;
        }
        return d;
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

    private static List getMappedList(final Map map, final String key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList());
        }
        return (List) map.get(key1);
    }

    private List getProficiencyAvgList(final DB2UDB db2, final Param param) {
        final List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getProficiencyAvgSql(param);
            log.debug(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final Map getDataMap = getListedMap(list, "CLASSKEY", rs.getString("CLASSKEY"));
                getDataMap.put(getDataKey(rs.getString("GRADE"), rs.getString("HR_CLASS"), "AVG"), rs.getBigDecimal("AVG"));
                getDataMap.put(getDataKey(rs.getString("GRADE"), rs.getString("HR_CLASS"), "SCORE"), rs.getBigDecimal("SCORE"));
                getDataMap.put(getDataKey(rs.getString("GRADE"), rs.getString("HR_CLASS"), "COUNT"), rs.getBigDecimal("COUNT"));
                getDataMap.put("CLASSNAME", rs.getString("CLASSNAME"));
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;
    }

    private static String getDataKey(final String grade, final String hrClass, final String kubun) {
        return grade + "-" + hrClass + "-" + kubun;
    }
    
    private static String getProficiencyAvgSql(final Param param) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SUBCLASS_CONV AS ( ");
        stb.append(" SELECT DISTINCT ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.PROFICIENCY_SUBCLASS_CD, ");
        stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND AS CLASSKEY, ");
        stb.append("     T3.CLASSNAME ");
        stb.append(" FROM PROFICIENCY_SUBCLASS_YDAT T1 ");
        stb.append(" INNER JOIN SCHREG_REGD_DAT T2 ON T2.YEAR = T1.YEAR ");
        stb.append("     AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("     AND T2.GRADE = T1.GRADE ");
        stb.append("     AND T2.COURSECD = T1.COURSECD ");
        stb.append("     AND T2.MAJORCD = T1.MAJORCD ");
        stb.append("     AND T2.COURSECODE = T1.COURSECODE ");
        stb.append(" INNER JOIN CLASS_MST T3 ON T3.CLASSCD = T1.CLASSCD ");
        stb.append("     AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append(" WHERE ");
        stb.append("   T1.DIV = '03' "); // コース
        stb.append("   AND T1.YEAR = '" + param._year + "' ");
        stb.append("   AND T1.SEMESTER = '" + param._semester + "' ");
        stb.append("   AND T1.PROFICIENCYDIV = '" + param._proficiencydiv + "' ");
        stb.append("   AND T1.PROFICIENCYCD = '" + param._proficiencycd + "' ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT DISTINCT ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.PROFICIENCY_SUBCLASS_CD, ");
        stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND AS CLASSKEY, ");
        stb.append("     T3.CLASSNAME ");
        stb.append(" FROM PROFICIENCY_SUBCLASS_YDAT T1 ");
        stb.append(" INNER JOIN SCHREG_REGD_DAT T2 ON T2.YEAR = T1.YEAR ");
        stb.append("     AND T2.SEMESTER = T1.SEMESTER ");
        stb.append(" INNER JOIN COURSE_GROUP_CD_DAT T3 ON T3.YEAR = T2.YEAR ");
        stb.append("     AND T3.GRADE = T2.GRADE ");
        stb.append("     AND T3.COURSECD = T2.COURSECD ");
        stb.append("     AND T3.MAJORCD = T2.MAJORCD ");
        stb.append("     AND T3.COURSECODE = T2.COURSECODE ");
        stb.append("     AND T3.GROUP_CD = T1.MAJORCD ");
        stb.append(" INNER JOIN CLASS_MST T3 ON T3.CLASSCD = T1.CLASSCD ");
        stb.append("     AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append(" WHERE ");
        stb.append("   T1.DIV = '04' "); // コースグループ
        stb.append("   AND T1.COURSECD = '0' ");
        stb.append("   AND T1.COURSECODE = '0000' ");
        stb.append("   AND T1.YEAR = '" + param._year + "' ");
        stb.append("   AND T1.SEMESTER = '" + param._semester + "' ");
        stb.append("   AND T1.PROFICIENCYDIV = '" + param._proficiencydiv + "' ");
        stb.append("   AND T1.PROFICIENCYCD = '" + param._proficiencycd + "' ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("   T1.GRADE, ");
        stb.append("   T1.HR_CLASS, ");
        stb.append("   T1.SCORE, ");
        stb.append("   T1.COUNT, ");
        stb.append("   T1.AVG, ");
        stb.append("   T3.CLASSKEY, ");
        stb.append("   T3.CLASSNAME ");
        stb.append(" FROM PROFICIENCY_AVERAGE_DAT T1 ");
        stb.append(" INNER JOIN SUBCLASS_CONV T3 ON T3.GRADE = T1.GRADE AND T3.PROFICIENCY_SUBCLASS_CD = T1.PROFICIENCY_SUBCLASS_CD ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '" + param._year + "' ");
        stb.append("   AND T1.SEMESTER = '" + param._semester + "' ");
        stb.append("   AND T1.PROFICIENCYDIV = '" + param._proficiencydiv + "' ");
        stb.append("   AND T1.PROFICIENCYCD = '" + param._proficiencycd + "' ");
        stb.append("   AND T1.DATA_DIV = '1' ");
        stb.append("   AND T1.AVG_DIV = '02' "); // HR
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
        log.fatal("$Revision: 67344 $ $Date: 2019-05-07 18:06:28 +0900 (火, 07 5 2019) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(request, db2);
        return param;
    }

    private static class Param {
        final String _year;
        final String _semester;
        final String _proficiencydiv;
        final String _proficiencycd;
        final String _loginDate;
        final String _testitemname;
        final String _schoolName;

        Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _proficiencydiv = request.getParameter("PROFICIENCYDIV");
            _proficiencycd = request.getParameter("PROFICIENCYCD");
            _loginDate = request.getParameter("LOGIN_DATE");
            _testitemname = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT PROFICIENCYNAME1 FROM PROFICIENCY_MST T1 WHERE T1.PROFICIENCYDIV = '" + _proficiencydiv + "'  AND T1.PROFICIENCYCD = '" + _proficiencycd + "' "));
            _schoolName = StringUtils.defaultString(KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SCHOOLNAME2 FROM SCHOOL_MST WHERE YEAR = '" + _year + "' ")));
        }
    }
}
