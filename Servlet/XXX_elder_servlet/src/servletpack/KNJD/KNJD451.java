/*
 * $Id: f4360a72aa330178eba75efe2713e1061f086c28 $
 *
 * 作成日: 2014/10/29
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;

public class KNJD451 {

    private static final Log log = LogFactory.getLog(KNJD451.class);

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        
        final List formList = new ArrayList();
        formList.add(new KNJD451_0(_param));
        
        final String studentSql = getStudentSql(_param);
        //log.debug(" studentSql = " + studentSql);
        final List studentList = KNJD451_0.getRowList(db2, studentSql);
        
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Map student = (Map) it.next();

            for (final Iterator fit = formList.iterator(); fit.hasNext();) {
                final KNJD451_0 form = (KNJD451_0) fit.next();
                form.printMain(svf, db2, student);
                _hasData = true;
            }
        }
        
    }

    private static class KNJD451_0 {

        final String FROM_TO_MARK = "\uFF5E";
        final String checkedBox = "■";
        final String noCheckedBox = "□";

        final Param _param;
        KNJD451_0(final Param param) {
            _param = param;
        }

        protected static String getString(final String field, final Map m) {
            if (m.isEmpty()) {
                return null;
            }
            if (!m.containsKey(field)) {
                throw new IllegalStateException("フィールドなし:" + field + ", " + m);
            }
            return (String) m.get(field);
        }
        
        protected static Map createRow() {
            return new HashMap();
        }

        protected static Map getFirstRow(final List list) {
            if (list.size() == 0) {
                return createRow();
            }
            return (Map) list.get(0);
        }
        
        protected static List withDummy(final List list) {
            if (list.isEmpty()) {
                list.add(createRow());
            }
            return list;
        }
        
        protected static Map getRowMap(final DB2UDB db2, final String sql, final String keyField) {
            final Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                ResultSetMetaData meta = rs.getMetaData();
                final Integer[] idxs = new Integer[meta.getColumnCount() + 1];
                for (int col = 1; col <= meta.getColumnCount(); col++) {
                    idxs[col] = new Integer(col);
                }
                while (rs.next()) {
                    final Map m = createRow();
                    for (int col = 1; col <= meta.getColumnCount(); col++) {
                        final String val = rs.getString(col);
                        m.put(meta.getColumnName(col), val);
                        m.put(idxs[col], val);
                    }
                    rtn.put(rs.getString(keyField), m);
                }
            } catch (SQLException e) {
                log.error("exception! sql = " + sql, e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }
        
        protected static List getRowList(final DB2UDB db2, final String sql) {
            final List rtn = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                ResultSetMetaData meta = rs.getMetaData();
                final Integer[] idxs = new Integer[meta.getColumnCount() + 1];
                for (int col = 1; col <= meta.getColumnCount(); col++) {
                    idxs[col] = new Integer(col);
                }
                while (rs.next()) {
                    final Map m = createRow();
                    for (int col = 1; col <= meta.getColumnCount(); col++) {
                        final String val = rs.getString(col);
                        m.put(meta.getColumnName(col), val);
                        m.put(idxs[col], val);
                    }
                    rtn.add(m);
                }
            } catch (SQLException e) {
                log.error("exception! sql = " + sql, e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        protected static Map createCondMap(final String[] keyval) {
            if (keyval.length % 2 != 0) {
                throw new IllegalArgumentException("引数の個数が奇数:" + ArrayUtils.toString(keyval));
            }
            final Map m = new HashMap();
            for (int i = 0; i < keyval.length; i += 2) {
                m.put(keyval[i], keyval[i + 1]);
            }
            return m;
        }
        
        protected static List filterList(final List rowList, final Map cond) {
            final List rtn = new ArrayList();
            for (final Iterator it = rowList.iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                boolean notMatched = false;
                for (final Iterator ci = cond.entrySet().iterator(); ci.hasNext();) {
                    final Map.Entry e = (Map.Entry) ci.next();
                    if (e.getValue().equals(getString((String) e.getKey(), row))) {
                    } else {
                        notMatched = true;
                    }
                }
                if (!notMatched) {
                    rtn.add(row);
                }
            }
            return rtn;
        }

        protected static String getOne(final DB2UDB db2, final String sql) {
            final Map row = getFirstRow(getRowList(db2, sql));
            if (row.isEmpty()) {
                return null;
            }
            return (String) row.get(new Integer(1));
        }
        
        protected static String formatNentuki(final String yearMonth) {
            if (null == yearMonth) {
                return "　　　年　月";
            }
            final String[] tateFormat = KNJ_EditDate.tate_format(KNJ_EditDate.h_format_JP(yearMonth + "-01"));
            return tateFormat[0] + tateFormat[1] + "年" + tateFormat[2] + "月";
        }

        protected static LinkedList singleton(final String s) {
            final LinkedList l = new LinkedList();
            l.add(s);
            return l;
        }

        protected static LinkedList asList(final String[] array) {
            final LinkedList l = new LinkedList();
            for (int i = 0; i < array.length; i++) {
                l.add(array[i]);
            }
            return l;
        }

        protected static List seq(final int startInclusive, final int endExcludive) {
            final List l = new ArrayList();
            for (int i = startInclusive; i < endExcludive; i++) {
                l.add(String.valueOf(i));
            }
            return l;
        }
        
        protected static List groupByCount(final List list, final int max) {
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
        
        protected List addLineIfLessThanCount(final int count, final List tokenList) {
            final List list = new ArrayList();
            if (null != tokenList) {
                list.addAll(tokenList);
            }
            for (int i = 0; i < count - tokenList.size(); i++) {
                list.add("");
            }
            return list;
        }
        
        private DecimalFormat df2 = new DecimalFormat("00");
        
        private static FieldDataGroup newFieldDataGroup(final List dataGroupList) {
            final FieldDataGroup fdg = new FieldDataGroup();
            dataGroupList.add(fdg);
            return fdg;
        }
        
        private List getAssessmentQMstList(final DB2UDB db2, final String assessDiv) {
            final String sql = " SELECT * FROM ASSESSMENT_Q_MST WHERE YEAR = '" + _param._year + "' AND ASSESS_DIV = '" + assessDiv + "' ORDER BY INT(ASSESS_CD) ";
            
            return getRowList(db2, sql);
        }

        private Map getAssessmentAnsDat(final DB2UDB db2, final String schregno, final String assessDiv) {
            final String sql = " SELECT * FROM ASSESSMENT_ANS_DAT WHERE YEAR = '" + _param._year + "' AND SCHREGNO = '" + schregno + "' AND ASSESS_DIV = '" + assessDiv + "' ";
            
            return getFirstRow(getRowList(db2, sql));
        }
        
        public void printMain(final Vrw32alp svf, final DB2UDB db2, final Map student) {
            
            final Form form = new Form(svf);
            form._form1 = "KNJD451.frm";
            form._recMax1 = 40;
            form.setForm1();
            
            final String schregno = getString("SCHREGNO", student);
            form.VrsOut("DATE", null); // 作成日
            form.VrsOut("SCHOOL_NAME", getOne(db2, "SELECT SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR = '" + _param._year + "' ")); // 学校名
            final int namelen = Util.getMS932ByteCount(getString("NAME", student));
            final int kanalen = Util.getMS932ByteCount(getString("NAME_KANA", student));
            final int gnamelen = Util.getMS932ByteCount(getString("GUARD_NAME", student));
            final int gkanalen = Util.getMS932ByteCount(getString("GUARD_KANA", student));
            form.VrsOut("NAME" + (namelen > 30 ? "3" : namelen > 20 ? "2" : "1"), getString("NAME", student)); // 氏名
            form.VrsOut("KANA" + (kanalen > 30 ? "2" : "1"), getString("NAME_KANA", student)); // かな
            form.VrsOut("GUARD_NAME" + (gnamelen > 30 ? "3" : gnamelen > 20 ? "2" : "1"), getString("GUARD_NAME", student)); // 保護者氏名
            form.VrsOut("GUARD_KANA" + (gkanalen > 30 ? "2" : "1"), getString("GUARD_KANA", student)); // 保護者かな
            form.VrsOut("BIRTHDAY", StringUtils.defaultString(KNJ_EditDate.h_format_JP(getString("BIRTHDAY", student))) + "（" + getString("AGE", student) + "才）"); // 生年月日
            
            final List dataGroupList2 = new ArrayList();
            
            for (int divi = 1; divi <= 4; divi++) {
                final String div = String.valueOf(divi);
                
                final FieldDataGroup dg = newFieldDataGroup(dataGroupList2);

                if (divi == 1) {
                    
                    //final String[][] columnTitle = {{"教科/科目", "(優先的に支援を", "要するもの)"}, {"授業中の様子"}, {"学習目標", "支援目標"}, {"具体的支援の方略"}, {"支援の評価"}};

                    final StringBuffer stb1 = new StringBuffer();
                    
                    stb1.append("  WITH MAX_DATE_SEMESTER AS ( ");
                    stb1.append("    SELECT T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.WRITING_DATE, MAX(SEMESTER) AS SEMESTER ");
                    stb1.append("    FROM EDUCATION_GUIDANCE_SCHREG_REMARK_DAT T1 ");
                    stb1.append("    WHERE ");
                    stb1.append("       T1.SCHREGNO = '" + schregno + "' ");
                    stb1.append("       AND T1.YEAR = '" + _param._outputYear + "' ");
                    stb1.append("       AND T1.DIV = '" + div + "' ");
                    stb1.append("    GROUP BY T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.WRITING_DATE ");
                    stb1.append("  ) ");

                    stb1.append("  , MAX_DATE AS ( ");
                    stb1.append(" SELECT ");
                    stb1.append("     T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.WRITING_DATE, MAX(T1.SEMESTER) AS SEMESTER ");
                    stb1.append(" FROM MAX_DATE_SEMESTER T1 ");
                    stb1.append(" WHERE T1.WRITING_DATE = (SELECT MAX(WRITING_DATE) ");
                    stb1.append("                          FROM MAX_DATE_SEMESTER ");
                    stb1.append("                          WHERE ");
                    stb1.append("                            CLASSCD = T1.CLASSCD ");
                    stb1.append("                            AND SCHOOL_KIND = T1.SCHOOL_KIND ");
                    stb1.append("                            AND CURRICULUM_CD = T1.CURRICULUM_CD ");
                    stb1.append("                            AND SUBCLASSCD = T1.SUBCLASSCD ");
                    stb1.append("                         ) ");
                    stb1.append(" GROUP BY ");
                    stb1.append("     T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.WRITING_DATE ");
                    stb1.append("  ) ");

                    stb1.append(" SELECT ");
                    stb1.append("     T1.SCHREGNO, ");
                    stb1.append("     T1.YEAR, ");
                    stb1.append("     T1.SEMESTER, ");
                    stb1.append("     T2.WRITING_DATE, ");
                    stb1.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
                    stb1.append("     T3.SUBCLASSNAME, ");
                    stb1.append("     T2.REMARK1, ");
                    stb1.append("     T2.REMARK2, ");
                    stb1.append("     T2.REMARK3, ");
                    stb1.append("     T2.REMARK4 ");
                    stb1.append(" FROM EDUCATION_GUIDANCE_SCHREG_SUBCLASS_DAT T1 ");
                    stb1.append(" LEFT JOIN (SELECT I1.* FROM EDUCATION_GUIDANCE_SCHREG_REMARK_DAT I1 ");
                    stb1.append("            INNER JOIN MAX_DATE I2 ON I2.WRITING_DATE = I1.WRITING_DATE AND I2.SEMESTER = I1.SEMESTER ");
                    stb1.append("            AND I2.CLASSCD = I1.CLASSCD ");
                    stb1.append("            AND I2.SCHOOL_KIND = I1.SCHOOL_KIND ");
                    stb1.append("            AND I2.CURRICULUM_CD = I1.CURRICULUM_CD ");
                    stb1.append("            AND I2.SUBCLASSCD = I1.SUBCLASSCD ");
                    stb1.append("           ) T2 ON T1.SCHREGNO = T2.SCHREGNO ");
                    stb1.append("     AND T1.SCHREGNO = T2.SCHREGNO ");
                    stb1.append("     AND T1.YEAR = T2.YEAR ");
                    stb1.append("     AND T1.CLASSCD = T2.CLASSCD ");
                    stb1.append("     AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ");
                    stb1.append("     AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ");
                    stb1.append("     AND T1.SUBCLASSCD = T2.SUBCLASSCD ");
                    stb1.append("     AND T2.DIV = '" + div + "' ");
                    stb1.append(" INNER JOIN SUBCLASS_MST T3 ON T1.CLASSCD = T3.CLASSCD ");
                    stb1.append("     AND T1.SCHOOL_KIND = T3.SCHOOL_KIND ");
                    stb1.append("     AND T1.CURRICULUM_CD = T3.CURRICULUM_CD ");
                    stb1.append("     AND T1.SUBCLASSCD = T3.SUBCLASSCD ");
                    stb1.append(" WHERE ");
                    stb1.append("     T1.SCHREGNO = '" + schregno + "' ");
                    stb1.append("     AND T1.YEAR = '" + _param._outputYear + "' ");
                    if (_param._year.equals(_param._outputYear)) {
                        stb1.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
                    } else {
                        stb1.append("     AND T1.SEMESTER = (SELECT MAX(SEMESTER) ");
                        stb1.append("                        FROM EDUCATION_GUIDANCE_SCHREG_SUBCLASS_DAT ");
                        stb1.append("                        WHERE YEAR = '" + _param._outputYear + "' ");
                        stb1.append("                          AND SCHREGNO = T1.SCHREGNO ");
                        stb1.append("                        ) ");
                    }
                    stb1.append("     AND NOT (T1.CLASSCD = '00' AND T1.SCHOOL_KIND = '00' AND T1.CURRICULUM_CD = '00' AND T1.SUBCLASSCD = '000000') ");
                    stb1.append(" ORDER BY ");
                    stb1.append("     T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD ");
                    
                    int subi = 0;
                    for (final Iterator it = getRowList(db2, stb1.toString()).iterator(); it.hasNext();) {
                        final Map row = (Map) it.next();
                        
                        FieldData d;
                        if (subi == 0) {
                            d = dg.newFieldData();
                            d.add("TITLE", singleton("□各教科"));
                            d = dg.newFieldData();
                            d.add("HEADER1", singleton("1"));
                        }
                        
                        subi += 1;
                        final String ssubi = String.valueOf(subi);
                        
                        //log.debug(" sub row = " + row);
                        
                        d = dg.newFieldData();
                        d.addAll("GRP1", ssubi); // グループコード
                        final String subclassname = getString("SUBCLASSNAME", row);
                        if (Util.getMS932ByteCount(subclassname) > 10) {
                            d.addCenter("FIELD2", Util.getTokenList(subclassname, 18)); // 
                        } else {
                            d.addCenter("FIELD1", Util.getTokenList(subclassname, 10)); // 
                        }
                        d.addAll("GRP2", ssubi); // グループコード
                        d.add("STATE", Util.getTokenList(getString("REMARK1", row), 30)); // 授業中の様子
                        d.addAll("GRP3", ssubi); // グループコード
                        d.add("TARGET", Util.getTokenList(getString("REMARK2", row), 22)); // 学習支援目標
                        
                        d.addAll("GRP4", ssubi); // グループコード
                        d.add("PLOT", Util.getTokenList(getString("REMARK3", row), 30)); // 方略
                        d.addAll("GRP5", ssubi); // グループコード
                        d.add("VALUE", Util.getTokenList(getString("REMARK4", row), 16)); // 評価
                    }
                    
                } else {

                    if (divi == 2) {
                        //final String[][] columnTitle = {{}, {"様子"}, {"支援目標"}, {"具体的支援の方略"}, {"支援の評価"}};
                        
                        FieldData d;
                        d = dg.newFieldData();
                        d.add("TITLE", singleton("□生活・行動面"));
                        d = dg.newFieldData();
                        d.add("HEADER2", singleton("1"));
                    }
                    
                    final String[] divtitle;
                    if (divi == 2) {
                        divtitle = new String[] {"対人関係", "・", "社会性"};
                    } else if (divi == 3) {
                        divtitle = new String[] {"コミュニケーション"};
                    } else if (divi == 4) {
                        divtitle = new String[] {"その他"};
                    } else {
                        divtitle = new String[] {};
                    }
                    int titleMaxKeta = 0;
                    for (int i = 0; i < divtitle.length; i++) {
                        titleMaxKeta = Math.max(titleMaxKeta, Util.getMS932ByteCount(divtitle[i]));
                    }
                    
                    final StringBuffer stb = new StringBuffer();
                    
                    stb.append("  WITH MAX_DATE_SEMESTER AS ( ");
                    stb.append("    SELECT T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.WRITING_DATE, MAX(SEMESTER) AS SEMESTER ");
                    stb.append("    FROM EDUCATION_GUIDANCE_SCHREG_REMARK_DAT T1 ");
                    stb.append("    WHERE ");
                    stb.append("       T1.SCHREGNO = '" + schregno + "' ");
                    stb.append("       AND T1.YEAR = '" + _param._outputYear + "' ");
                    stb.append("       AND T1.DIV = '" + div + "' ");
                    stb.append("    GROUP BY T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.WRITING_DATE ");
                    stb.append("  ) ");

                    stb.append("  , MAX_DATE AS ( ");
                    stb.append(" SELECT ");
                    stb.append("     T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.WRITING_DATE, MAX(T1.SEMESTER) AS SEMESTER ");
                    stb.append(" FROM MAX_DATE_SEMESTER T1 ");
                    stb.append(" WHERE T1.WRITING_DATE = (SELECT MAX(WRITING_DATE) ");
                    stb.append("                          FROM MAX_DATE_SEMESTER ");
                    stb.append("                          WHERE ");
                    stb.append("                            CLASSCD = T1.CLASSCD ");
                    stb.append("                            AND SCHOOL_KIND = T1.SCHOOL_KIND ");
                    stb.append("                            AND CURRICULUM_CD = T1.CURRICULUM_CD ");
                    stb.append("                            AND SUBCLASSCD = T1.SUBCLASSCD ");
                    stb.append("                         ) ");
                    stb.append(" GROUP BY ");
                    stb.append("     T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.WRITING_DATE ");
                    stb.append("  ) ");

                    stb.append(" SELECT ");
                    stb.append("     T1.SEMESTER, ");
                    stb.append("     T1.WRITING_DATE, ");
                    stb.append("     T1.REMARK1, ");
                    stb.append("     T1.REMARK2, ");
                    stb.append("     T1.REMARK3, ");
                    stb.append("     T1.REMARK4 ");
                    stb.append(" FROM EDUCATION_GUIDANCE_SCHREG_REMARK_DAT T1 ");
                    stb.append(" INNER JOIN MAX_DATE T2 ON T2.CLASSCD = T1.CLASSCD ");
                    stb.append("     AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                    stb.append("     AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
                    stb.append("     AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
                    stb.append("     AND T2.WRITING_DATE = T1.WRITING_DATE ");
                    stb.append("     AND T2.SEMESTER = T1.SEMESTER ");
                    stb.append(" WHERE ");
                    stb.append("     T1.SCHREGNO = '" + schregno + "' ");
                    stb.append("     AND T1.YEAR = '" + _param._outputYear + "' ");
                    stb.append("     AND T1.DIV = '" + div + "' ");
                    stb.append("     AND T1.CLASSCD = '00' AND T1.SCHOOL_KIND = '00' AND T1.CURRICULUM_CD = '00' AND T1.SUBCLASSCD = '000000' ");
                    
                    final Map row = getFirstRow(getRowList(db2, stb.toString()));
                    
                    final FieldData d = dg.newFieldData();

                    d.addAll("GRP1", div); // グループコード
                    d.addCenter("FIELD" + (titleMaxKeta > 10 ? "2" : "1"), asList(divtitle)); // 
                    d.addAll("GRP2", div); // グループコード
                    d.add("STATE", Util.getTokenList(getString("REMARK1", row), 30)); // 授業中の様子
                    d.addAll("GRP3", div); // グループコード
                    d.add("TARGET", Util.getTokenList(getString("REMARK2", row), 22)); // 学習支援目標
                    d.addAll("GRP4", div); // グループコード
                    d.add("PLOT", Util.getTokenList(getString("REMARK3", row), 30)); // 方略
                    d.addAll("GRP5", div); // グループコード
                    d.add("VALUE", Util.getTokenList(getString("REMARK4", row), 16)); // 評価
                }
                
                FieldDataGroup.svfPrintFieldDataGroup(dg, form);
            }
        }
    }
    
    private static class Util {
        private static List extendStringList(final List list, final int size, final boolean centering) {
            final LinkedList rtn = new LinkedList();
            final int msize = Math.max(list.size(), size);
            if (centering) {
                final int blankCount = (msize - list.size()) / 2;
                for (int i = 0; i < blankCount; i++) {
                    rtn.add(null);
                }
            }
            rtn.addAll(list);
            for (int i = rtn.size(); i < msize; i++) {
                rtn.add(null);
            }
            return rtn;
        }

        private static void setRecordFieldDataAll(final List printRecordList, final String fieldDivName, final String data) {
            setRecordFieldDataList(printRecordList, fieldDivName, repeat(data, printRecordList.size()));
        }
        
        private static void setRecordFieldDataList(final List printRecordList, final String fieldDivName, final List dataList) {
            for (int j = 0, max = printRecordList.size(); j < max; j++) {
                final Map record = (Map) printRecordList.get(j);
                final Map fieldDivNameMap = getMappedMap(record, FieldData.FIELD_DIV_NAME);
                record.put(StringUtils.defaultString((String) fieldDivNameMap.get(fieldDivName), fieldDivName), dataList.get(j));
            }
        }

        private static List charStringList(final String s) {
            final LinkedList rtn = new LinkedList();
            if (s == null) {
                return rtn;
            }
            for (int i = 0; i < s.length(); i++) {
                rtn.add(String.valueOf(s.charAt(i)));
            }
            return rtn;
        }
        
        private static Map getMappedMap(final Map map, final String key) {
            if (null == map.get(key)) {
                map.put(key, new TreeMap());
            }
            return (Map) map.get(key);
        }
        
        private static List getMappedList(final Map map, final String key) {
            if (null == map.get(key)) {
                map.put(key, new ArrayList());
            }
            return (List) map.get(key);
        }
        
        private static List repeat(final String data, final int count) {
            final List list = new ArrayList();
            for (int i = 0; i < count; i++) {
                list.add(data);
            }
            return list;
        }
        
        protected static int getMS932ByteCount(final String str) {
            int count = 0;
            if (null != str) {
                try {
                    count = str.getBytes("MS932").length;
                } catch (Exception e) {
                    log.error("EncodingException!", e);
                    count = str.length();
                }
            }
            return count;
        }
        
        /**
         * @param source 元文字列
         * @param bytePerLine 1行あたりのバイト数
         * @return bytePerLineのバイト数ごとの文字列リスト
         */
        protected static List getTokenList(final String source0, final int bytePerLine) {

            if (source0 == null || source0.length() == 0) {
                return Collections.EMPTY_LIST;
            }
            return KNJ_EditKinsoku.getTokenList(source0, bytePerLine);
        }
        
        protected static void svfVrListOut(
                final Form form,
                final String field,
                final List tokenList
        ) {
            svfVrListOutWithStart(form, field, 1, tokenList);
        }
        
        protected static void svfVrListOutWithStart(
                final Form form,
                final String field,
                final int start,
                final List tokenList
        ) {
            for (int j = 0; j < tokenList.size(); j++) {
                if (null == tokenList.get(j)) {
                    continue;
                }
                form.VrsOut(field + String.valueOf(start + j), (String) tokenList.get(j));
            }
        }

        protected static void svfVrListOutn(
                final Form form,
                final String field,
                final List tokenList
        ) {
            for (int j = 0; j < tokenList.size(); j++) {
                if (null == tokenList.get(j)) {
                    continue;
                }
                form.VrsOutn(field, j + 1, (String) tokenList.get(j));
            }
        }

        protected static void svfVrListOutn(
                final Form form,
                final String field,
                final int n,
                final List tokenList
        ) {
            for (int j = 0; j < tokenList.size(); j++) {
                if (null == tokenList.get(j)) {
                    continue;
                }
                form.VrsOutn(field + String.valueOf(j + 1), n, (String) tokenList.get(j));
            }
        }

        protected static void svfVrsOutWithSize(
                final Form form,
                final String fieldHead,
                final String[] field,
                final int[] byteSize,
                final String data
        ) {
            final int bsize = getMS932ByteCount(data);
            boolean out = false;
            for (int i = 0; i < field.length - 1; i++) {
                if (bsize <= byteSize[i]) {
                    form.VrsOut(fieldHead + field[i], data);
                    out = true;
                    break;
                }
                
            }
            if (!out) {
                form.VrsOut(fieldHead + field[field.length - 1], data);
            }
        }
        
        protected static void svfVrsOutnWithSize(
                final Form form,
                final String fieldHead,
                final String[] field,
                final int[] byteSize,
                final int n,
                final String data
        ) {
            final int bsize = getMS932ByteCount(data);
            boolean out = false;
            for (int i = 0; i < field.length - 1; i++) {
                if (bsize <= byteSize[i]) {
                    form.VrsOutn(fieldHead + field[i], n, data);
                    out = true;
                    break;
                }
                
            }
            if (!out) {
                form.VrsOutn(fieldHead + field[field.length - 1], n, data);
            }
        }
    }

    private static class Form {
        final Vrw32alp _svf;
        String _form1;
        int _recMax1 = Integer.MAX_VALUE;
        int _recMax;
        int recLine;
        
        private void VrsOut(final String field, final String data) {
            _svf.VrsOut(field, data);
        }
        
        private void VrsOutn(final String field, final int gyo, final String data) {
            _svf.VrsOutn(field, gyo, data);
        }
        
        private Form(final Vrw32alp svf) {
            _svf = svf;
        }

        private void setForm1() {
            _svf.VrSetForm(_form1, 4);
            _recMax = _recMax1;
            recLine = 0;
        }
        
        private void VrEndRecord() {
            _svf.VrEndRecord();
            recLine += 1;
        }
    }

    private static class FieldData {
        static final String FIELD_DIV_NAME = "FIELD_NAME";
        final Map _fieldDivNameMap = new HashMap();
        final List _recordDataList = new LinkedList();
        final Map _addAllMap = new HashMap();
        final Map _addCenterMap = new HashMap();
        
        private void create(final int min) {
            for (int i = _recordDataList.size(); i < min; i++) {
                _recordDataList.add(newRecord());
            }
        }
        
        public static void changeField(final Map record, final Map changeFieldMap) {
            final Map newContents = newRecord0();
            for (final Iterator it = record.entrySet().iterator(); it.hasNext();) {
                final Map.Entry e = (Map.Entry) it.next();
                if (FIELD_DIV_NAME.equals(e.getKey())) {
                    final Map fieldDivNameMap = (Map) e.getValue();
                    final Map newFieldDivNameMap = new HashMap();
                    for (final Iterator fdIt = fieldDivNameMap.entrySet().iterator(); fdIt.hasNext();) {
                        final Map.Entry divEntry = (Map.Entry) fdIt.next();
                        newFieldDivNameMap.put(divEntry.getKey(), changeFieldMap.get(divEntry.getValue()));
                    }
                    newContents.put(FIELD_DIV_NAME, newFieldDivNameMap);
                    continue;
                }
                if (null == changeFieldMap.get(e.getKey())) {
                    try {
                        throw new IllegalStateException("変換先無し! key = " + e.getKey());
                    } catch (Exception ex) {
                        log.warn("変換先無し! src = " + record + ", change = " + changeFieldMap, ex);
                    }
                    newContents.put(e.getKey(), e.getValue());
                } else {
                    newContents.put(changeFieldMap.get(e.getKey()), e.getValue());
                }
            }
            record.clear();
            record.putAll(newContents);
        }

        public void add(final String fieldname, final int gyo, final List dataLines) { // 文言等、dataLinesの各行のfieldnameにdataをセット
            add(fieldname + "," + String.valueOf(gyo), dataLines);
        }

        public void add(final String fieldname, final List dataLines) { // 文言等、dataLinesの各行のfieldnameにdataをセット
            create(dataLines.size());
            for (int i = 0; i < dataLines.size(); i++) {
                final Map record = (Map) _recordDataList.get(i);
                record.put(fieldname, dataLines.get(i));
            }
        }
        
        public List getPrintRecordList() {
            create(1);
            for (final Iterator cit = _addCenterMap.keySet().iterator(); cit.hasNext();) {
                final String fieldname = (String) cit.next();
                final List dataLines = (List) _addCenterMap.get(fieldname);
                add(fieldname, center(dataLines, _recordDataList.size()));
            }
            for (final Iterator cit = _addAllMap.keySet().iterator(); cit.hasNext();) {
                final String fieldname = (String) cit.next();
                final String data = (String) _addAllMap.get(fieldname);
                add(fieldname, repeat(data, _recordDataList.size()));
            }
            for (final Iterator rit = _recordDataList.iterator(); rit.hasNext();) {
                final Map record = (Map) rit.next();
                ((Map) record.get(FIELD_DIV_NAME)).putAll(_fieldDivNameMap);
            }
            //log.debug(" recordDataList = " + _recordDataList);
            return _recordDataList;
        }
        
        private Map getRecord(final List recordList, final int i) {
            return (Map) recordList.get(i);
        }

        public static void svfPrintRecordList(final List recordList, final Form form) {
//            form._svf.debug = true;
            for (final Iterator it = recordList.iterator(); it.hasNext();) {
                final Map record = (Map) it.next();
                svfPrintRecord(record, form);
                form.VrEndRecord();
            }
//            form._svf.debug = false;
        }
        
        private static void svfPrintRecord(final Map record, final Form form) {
            for (final Iterator fit = record.keySet().iterator(); fit.hasNext();) {
                final String field = (String) fit.next();
                final Object data = record.get(field);
                if (data instanceof String) {
                    if (null != field && field.indexOf(",") != -1) {
                        final String[] split = StringUtils.split(field, ",");
                        form.VrsOutn(split[0], Integer.parseInt(split[1]), data.toString());
                        
                    } else {
                        form.VrsOut(field, data.toString());
                    }
                }
            }
        }

        public void addAll(final String fieldname, final String data) { // DIV等、全ての行のfieldnameにdataをセット
            _addAllMap.put(fieldname, data);
        }
        public void addCenter(final String fieldname, final List dataLines) { // タイトル等、全ての行の中央にdataLinesをセット
            _addCenterMap.put(fieldname, dataLines);
        }
        
        public String toString() {
            return "FieldData(" + _recordDataList + ", all = " + _addAllMap + ", center = " + _addCenterMap + ")";
        }

        private static final Map newRecord0() {
            return new TreeMap();
        }
        protected final Map newRecord() {
            final Map r = newRecord0();
            r.put(FIELD_DIV_NAME, _fieldDivNameMap);
            return r;
        }
        
        static List center(final List s, final int size) { // lをsize行の中央行にセット
            final LinkedList l = new LinkedList(s);
            for (int i = 0, max = (size - l.size()) / 2; i < max; i++) {
                l.addFirst("");
            }
            for (int i = 0; i < size - l.size(); i++) {
                l.addLast("");
            }
            return l;
        }
        
        static List repeat(final String s, final int size) {
            final List l = new ArrayList();
            for (int i = 0; i < size; i++) {
                l.add(s);
            }
            return l;
        }
    }
    
    private static class FieldDataGroup extends FieldData {
        final List _fieldDataList = new ArrayList();
        final Map _centringHeaders = new HashMap();
        
        FieldDataGroup() {
            super();
        }

        public FieldData newFieldData() {
            final FieldData fieldData = new FieldData();
            _fieldDataList.add(fieldData);
            return fieldData;
        }

        public FieldDataGroup newFieldDataGroup() {
            final FieldDataGroup fieldData = new FieldDataGroup();
            _fieldDataList.add(fieldData);
            return fieldData;
        }
        
        private void mergeRecordDataList() {
            final List allChidrenDataList = new ArrayList();
            for (final Iterator fit = _fieldDataList.iterator(); fit.hasNext();) {
                final FieldData fd = (FieldData) fit.next();
                fd._fieldDivNameMap.putAll(_fieldDivNameMap);
                allChidrenDataList.addAll(fd.getPrintRecordList());
            }
            for (int i = 0, max = allChidrenDataList.size() - _recordDataList.size(); i < max; i++) {
                _recordDataList.add(newRecord());
            }
            for (int i = 0; i < allChidrenDataList.size(); i++) {
                ((Map) _recordDataList.get(i)).putAll((Map) allChidrenDataList.get(i));
            }
        }

        public List getPrintRecordList() {
            mergeRecordDataList();
            for (final Iterator cit = _addCenterMap.keySet().iterator(); cit.hasNext();) {
                final String fieldname = (String) cit.next();
                final List dataLines = (List) _addCenterMap.get(fieldname);
                add(fieldname, center(dataLines, _recordDataList.size()));
            }
            for (final Iterator cit = _addAllMap.keySet().iterator(); cit.hasNext();) {
                final String fieldname = (String) cit.next();
                final String data = (String) _addAllMap.get(fieldname);
                for (final Iterator fit = _recordDataList.iterator(); fit.hasNext();) {
                    final Map record = (Map) fit.next();
                    record.put(fieldname, data);
                }
            }
//            log.debug(" # grouped record dataList size = " + _recordDataList.size());
//            for (int i = 0; i < _recordDataList.size(); i++) {
//                log.debug("  # grouped record i = " + i + ", " + _recordDataList.get(i));
//            }

            return _recordDataList;
        }
        
        public void addHeader(final String field, final List headerTitle) {
            _centringHeaders.put(field, headerTitle);
        }

        public static void svfPrintFieldDataGroup(final FieldDataGroup fdg, final Form form) {
            final List recordList = fdg.getPrintRecordList();
            for (final Iterator it = fdg._centringHeaders.entrySet().iterator(); it.hasNext();) {
                final Map.Entry e = (Map.Entry) it.next();
                final String field = (String) e.getKey();
                final List headerTitle = (List) e.getValue();
                final boolean centering = true;
                Util.setRecordFieldDataList(recordList, field, Util.extendStringList(headerTitle, recordList.size(), centering));
            }
            for (final Iterator it = recordList.iterator(); it.hasNext();) {
                final Map record = (Map) it.next();
                FieldData.svfPrintRecord(record, form);
                form.VrEndRecord();
            }
      }
    }
    
    private static String getStudentSql(final Param param) {
        final StringBuffer stb = new StringBuffer();
        
        stb.append(" WITH SCHREGNOS(SCHREGNO) AS ( ");
        String unionAll = "";
        for (int i = 0; i < param._schregSelected.length; i++) {
            stb.append(unionAll).append(" VALUES('" + param._schregSelected[i] + "') ");
            unionAll = " UNION ALL ";
        }
        stb.append(" ), ADDRESS AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.ZIPCD, ");
        stb.append("     T1.ADDR1, ");
        stb.append("     T1.ADDR2 ");
        stb.append(" FROM  SCHREG_ADDRESS_DAT T1 ");
        stb.append(" INNER JOIN (SELECT T1.SCHREGNO, MAX(T1.ISSUEDATE) AS ISSUEDATE ");
        stb.append("             FROM SCHREG_ADDRESS_DAT T1 ");
        stb.append("             WHERE FISCALYEAR(T1.ISSUEDATE) <= '" + param._year + "' ");
        stb.append("             GROUP BY T1.SCHREGNO ");
        stb.append("            ) T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.ISSUEDATE = T1.ISSUEDATE ");
        stb.append(" WHERE T1.SCHREGNO IN (SELECT SCHREGNO FROM SCHREGNOS) ");
        stb.append(" ), GUARD_ADDRESS AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.GUARD_ZIPCD, ");
        stb.append("     T1.GUARD_ADDR1, ");
        stb.append("     T1.GUARD_ADDR2, ");
        stb.append("     T1.GUARD_TELNO, ");
        stb.append("     T1.GUARD_ADDR_FLG ");
        stb.append(" FROM  GUARDIAN_ADDRESS_DAT T1 ");
        stb.append(" INNER JOIN (SELECT T1.SCHREGNO, MAX(T1.ISSUEDATE) AS ISSUEDATE ");
        stb.append("             FROM GUARDIAN_ADDRESS_DAT T1 ");
        stb.append("             WHERE FISCALYEAR(T1.ISSUEDATE) <= '" + param._year + "' ");
        stb.append("             GROUP BY T1.SCHREGNO ");
        stb.append("            ) T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.ISSUEDATE = T1.ISSUEDATE ");
        stb.append(" WHERE T1.SCHREGNO IN (SELECT SCHREGNO FROM SCHREGNOS) ");
        stb.append(" ), REGD_INFO AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T3.HR_NAME, ");
        stb.append("     ST1.STAFFNAME, ");
        stb.append("     ROW_NUMBER() OVER(PARTITION BY T1.SCHREGNO ORDER BY T1.YEAR) AS ORDER  ");
        stb.append(" FROM  SCHREG_REGD_DAT T1 ");
        stb.append(" INNER JOIN (SELECT T1.SCHREGNO, T1.YEAR, MAX(T1.SEMESTER) AS SEMESTER ");
        stb.append("             FROM SCHREG_REGD_DAT T1 ");
        stb.append("             WHERE (T1.YEAR < '" + param._year + "' OR T1.YEAR = '" + param._year + "' AND T1.SEMESTER <= '" + param._semester + "') ");
        stb.append("             GROUP BY T1.SCHREGNO, T1.YEAR ");
        stb.append("            ) T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER ");
        stb.append(" INNER JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR = T1.YEAR ");
        stb.append("     AND T3.SEMESTER = T1.SEMESTER ");
        stb.append("     AND T3.GRADE = T1.GRADE ");
        stb.append("     AND T3.HR_CLASS = T1.HR_CLASS ");
        stb.append(" INNER JOIN STAFF_MST ST1 ON ST1.STAFFCD = T3.TR_CD1  ");
        stb.append(" WHERE T1.YEAR <= '" + param._year + "' ");
        stb.append("       AND T1.SCHREGNO IN (SELECT SCHREGNO FROM SCHREGNOS) ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T5.HR_NAME, ");
        stb.append("     STFHR.STAFFNAME AS HR_STAFFNAME, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     GD.GRADE_CD, ");
        stb.append("     T2.NAME, ");
        stb.append("     T2.NAME_KANA, ");
        stb.append("     Z002.NAME2 AS SEX_NAME, ");
        stb.append("     T2.BIRTHDAY, ");
        stb.append("     AD1.ZIPCD, ");
        stb.append("     AD1.ADDR1, ");
        stb.append("     AD1.ADDR2, ");
        stb.append("     GUARD.GUARD_NAME, ");
        stb.append("     GUARD.GUARD_KANA, ");
        stb.append("     GAD1.GUARD_ZIPCD, ");
        stb.append("     GAD1.GUARD_ADDR1, ");
        stb.append("     GAD1.GUARD_ADDR2, ");
        stb.append("     GAD1.GUARD_TELNO, ");
        stb.append("     GAD1.GUARD_ADDR_FLG, ");
        stb.append("     H201.NAME1 AS GUARD_RELATIONSHIP_NAME, ");
        stb.append("     T2.EMERGENCYCALL, ");
        stb.append("     T2.EMERGENCYTELNO, ");
        stb.append("     T2.EMERGENCYCALL2, ");
        stb.append("     T2.EMERGENCYTELNO2, ");
        stb.append("     T2.EMERGENCYCALL3, ");
        stb.append("     T2.EMERGENCYTELNO3, ");
        stb.append("     T1.COURSECD, ");
        stb.append("     T6.COURSENAME, ");
        stb.append("     T1.MAJORCD, ");
        stb.append("     T7.MAJORNAME, ");
        stb.append("     T1.COURSECODE, ");
        stb.append("     T8.COURSECODENAME, ");
        stb.append("     A023.ABBV1 AS SCHOOL_KIND_NAME, ");
        stb.append("     JFIN.FINSCHOOL_NAME AS JUNIOR_FINSCHOOL_NAME, ");
        stb.append("     T2.BLOODTYPE, ");
        stb.append("     RI1.HR_NAME AS REGD_HR_NAME1, RI1.STAFFNAME AS REGD_STAFFNAME1, ");
        stb.append("     RI2.HR_NAME AS REGD_HR_NAME2, RI2.STAFFNAME AS REGD_STAFFNAME2, ");
        stb.append("     RI3.HR_NAME AS REGD_HR_NAME3, RI3.STAFFNAME AS REGD_STAFFNAME3, ");
        stb.append("     RI4.HR_NAME AS REGD_HR_NAME4, RI4.STAFFNAME AS REGD_STAFFNAME4, ");
        stb.append("     CASE WHEN T2.BIRTHDAY IS NOT NULL THEN YEAR(DATE('" + param._year + "' || '-04-01') - T2.BIRTHDAY) END AS AGE ");
        stb.append(" FROM  SCHREG_REGD_DAT T1 ");
        stb.append(" INNER JOIN V_SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append(" LEFT JOIN SCHREG_REGD_HDAT T5 ON T5.YEAR = T1.YEAR ");
        stb.append("     AND T5.SEMESTER = T1.SEMESTER ");
        stb.append("     AND T5.GRADE = T1.GRADE ");
        stb.append("     AND T5.HR_CLASS = T1.HR_CLASS ");
        stb.append(" LEFT JOIN COURSE_MST T6 ON T6.COURSECD = T1.COURSECD ");
        stb.append(" LEFT JOIN MAJOR_MST T7 ON T7.COURSECD = T1.COURSECD ");
        stb.append("     AND T7.MAJORCD = T1.MAJORCD ");
        stb.append(" LEFT JOIN SCHREG_REGD_GDAT GD ON GD.YEAR = T1.YEAR ");
        stb.append("     AND GD.GRADE = T1.GRADE ");
        stb.append(" LEFT JOIN COURSECODE_MST T8 ON T8.COURSECODE = T1.COURSECODE ");
        stb.append(" LEFT JOIN GUARDIAN_DAT GUARD ON GUARD.SCHREGNO = T1.SCHREGNO ");
        stb.append(" LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' AND Z002.NAMECD2 = T2.SEX ");
        stb.append(" LEFT JOIN NAME_MST H201 ON H201.NAMECD1 = 'H201' AND H201.NAMECD2 = GUARD.RELATIONSHIP ");
        stb.append(" LEFT JOIN NAME_MST A023 ON A023.NAMECD1 = 'A023' AND A023.NAME1 = GD.SCHOOL_KIND ");
        stb.append(" LEFT JOIN ADDRESS AD1 ON AD1.SCHREGNO = T1.SCHREGNO ");
        stb.append(" LEFT JOIN GUARD_ADDRESS GAD1 ON GAD1.SCHREGNO = T1.SCHREGNO ");
        stb.append(" LEFT JOIN SCHREG_ENT_GRD_HIST_DAT ENTGRD_H ON ENTGRD_H.SCHREGNO = T1.SCHREGNO AND ENTGRD_H.SCHOOL_KIND = 'H' ");
        stb.append(" LEFT JOIN FINSCHOOL_MST JFIN ON JFIN.FINSCHOOLCD = ENTGRD_H.FINSCHOOLCD ");
        stb.append(" LEFT JOIN STAFF_MST STFHR ON STFHR.STAFFCD = T5.TR_CD1 ");
        stb.append(" LEFT JOIN REGD_INFO RI1 ON RI1.SCHREGNO = T1.SCHREGNO AND RI1.ORDER = 1 ");
        stb.append(" LEFT JOIN REGD_INFO RI2 ON RI2.SCHREGNO = T1.SCHREGNO AND RI2.ORDER = 2 ");
        stb.append(" LEFT JOIN REGD_INFO RI3 ON RI3.SCHREGNO = T1.SCHREGNO AND RI3.ORDER = 3 ");
        stb.append(" LEFT JOIN REGD_INFO RI4 ON RI4.SCHREGNO = T1.SCHREGNO AND RI4.ORDER = 4 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + param._semester + "'  ");
        stb.append("     AND T1.SCHREGNO IN (SELECT SCHREGNO FROM SCHREGNOS) ");
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _semester;
        final String[] _schregSelected;
        final String _outputYear;
        
        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            if ("KNJD450".equals(request.getParameter("PRGID"))) {
                _schregSelected = request.getParameterValues("SCHREGNO");
                _outputYear = _year;
            } else {
                _schregSelected = request.getParameterValues("SCHREG_SELECTED");
                _outputYear = request.getParameter("OUTPUT_YEAR");
            }
        }
    }
}

// eof

