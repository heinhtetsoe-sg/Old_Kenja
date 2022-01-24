package servletpack.KNJL;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
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
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;


/**
 *
 *    学校教育システム 賢者 [入試管理]
 *
 **/

public class KNJL337G {

    private static final Log log = LogFactory.getLog(KNJL337G.class);

    private boolean _hasData;
    private Param _param;

    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意

        Vrw32alp svf     = new Vrw32alp();   //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2       = null;             //Databaseクラスを継承したクラス

        //print設定
        PrintWriter outstrm = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");

        //svf設定
        svf.VrInit();    //クラスの初期化
        svf.VrSetSpoolFileStream(response.getOutputStream()); //PDFファイル名の設定

        //ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch (Exception ex) {
            log.error("DB2 open error!", ex);
            return;
        }

        try {
            KNJServletUtils.debugParam(request, log);
            _param = new Param(db2, request);

            //SVF出力
            printMain(db2, svf); //帳票出力のメソッド

        } catch (Exception ex) {
            log.error("exception!", ex);
        } finally {
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }

            //終了処理
            svf.VrQuit();
            db2.commit();
            db2.close();       //DBを閉じる
            outstrm.close();   //ストリームを閉じる
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

    private static int getMS932Bytecount(String str) {
        int count = 0;
        if (null != str) {
            try {
                count = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error(e);
            }
        }
        return count;
    }
    
    private static String percentage(final String bunshi, final String bunbo) {
        if (!NumberUtils.isNumber(bunshi) || !NumberUtils.isNumber(bunbo) || Double.parseDouble(bunbo) == 0.0) {
            return null;
        }
        return new BigDecimal(bunshi).multiply(new BigDecimal(100)).divide(new BigDecimal(bunbo), 1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private static String add(final Object o1, final Object o2) {
        final String s1 = (String) o1;
        final String s2 = (String) o2;
        if (!NumberUtils.isDigits(s1)) { return s2; }
        if (!NumberUtils.isDigits(s2)) { return s1; }
        return String.valueOf(Integer.parseInt(s1) + Integer.parseInt(s2));
    }
    
    private static List getList(final DB2UDB db2, final String sql) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List list = new ArrayList();
        try {
            log.info(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                final Map m = new HashMap();
                list.add(m);
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    m.put(meta.getColumnName(i), rs.getString(i));
                }
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;
    }
    
    private static String getString(final Map m, final String field) {
        if (null == m) {
            return null;
        }
        try {
            if (!m.containsKey(field)) {
                throw new IllegalArgumentException("not defined: " + field + " in " + m.keySet());
            }
        } catch (Exception e) {
            log.fatal("exception!", e);
        }
        return (String) m.get(field);
    }

    private static String tokeiKey( final String entexamyear, final String applicantdiv, final Object testdiv, final Object shdiv, final Object sex) {
        return entexamyear + "-" + applicantdiv + "-" + testdiv + "-" + (null == shdiv ? "" : shdiv.toString()) + "-" + sex;
    }
    
    private String tokeiValue(final Map tokeiMap, final String key, final String field) {
        if (null == tokeiMap.get(key)) {
            return null;
        }
        final Map tokei = (Map) tokeiMap.get(key);
        return (String) tokei.get(field);
    }
    
    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        
        final Map tokeiMap = Tokei.getTokeiMap(db2, _param);
        
        Tokei.updateDB(db2, _param, tokeiMap);
        
        for (final Iterator it = tokeiMap.entrySet().iterator(); it.hasNext();) {
            log.debug(" data = " + it.next());
        }

        final String form = "KNJL337G.frm";
        svf.VrSetForm(form, 4);
        
        svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "年度　入学考査　査定資料"); // タイトル
        svf.VrsOut("SUBTITLE", "過去５年間の入学状況"); // サブタイトル
//        svf.VrsOut("PAGE", null); // ページ
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate)); // 
        
        for (int i = 4; i >= 0; i -= 1) {
            final String entexamyear = String.valueOf(Integer.parseInt(_param._entexamyear) - i);
            
            svf.VrsOut("NENDO", KenjaProperties.gengou(Integer.parseInt(entexamyear)) + "年度"); // 年度
            
            String juTotal1sen = null;
            String juTotal1hei = null;
            String juTotal2 = null;
            String juTotal3 = null;
            String goTotal1sen = null;
            String goTotal1hei = null;
            String goTotal2 = null;
            String goTotal3 = null;
            String nyuTotal1sen = null;
            String nyuTotal1hei = null;
            String nyuTotal2 = null;
            String nyuTotal3 = null;
            for (int si = 1; si <= 3; si++) {
                final String sex = String.valueOf(si);
                
                final String keyTestdiv1sen = tokeiKey(entexamyear, _param._applicantdiv, "1", "1", sex);
                final String keyTestdiv1hei = tokeiKey(entexamyear, _param._applicantdiv, "1", "2", sex);
                final String keyTestdiv2 = tokeiKey(entexamyear, _param._applicantdiv, "2", "", sex);
                final String keyTestdiv3 = tokeiKey(entexamyear, _param._applicantdiv, "3", "", sex);
                String field;
                
                field = "REMARK4"; // 受験者数
                final String juCount1sen;
                final String juCount1hei;
                final String juCount2;
                final String juCount3;
                if (si == 3) {
                    juCount1sen = juTotal1sen;
                    juCount1hei = juTotal1hei;
                    juCount2 = juTotal2;
                    juCount3 = juTotal3;
                } else {
                    juCount1sen = tokeiValue(tokeiMap, keyTestdiv1sen, field);
                    juCount1hei = tokeiValue(tokeiMap, keyTestdiv1hei, field);
                    juCount2 = tokeiValue(tokeiMap, keyTestdiv2, field);
                    juCount3 = tokeiValue(tokeiMap, keyTestdiv3, field);
                    juTotal1sen = add(juTotal1sen, juCount1sen);
                    juTotal1hei = add(juTotal1hei, juCount1hei);
                    juTotal2 = add(juTotal2, juCount2);
                    juTotal3 = add(juTotal3, juCount3);
                }
                svf.VrsOut("EXAM1_1_" + sex, add(juCount1sen, juCount1hei)); // 受験1次計
                svf.VrsOut("EXAM1_2_" + sex, juCount1sen); // 受験1次専願
                svf.VrsOut("EXAM1_3_" + sex, juCount1hei); // 受験1次併願
                svf.VrsOut("EXAM2_" + sex, juCount2); // 受験1.5次
                svf.VrsOut("EXAM3_" + sex, juCount3); // 受験2次
                svf.VrsOut("EXAM_ALL" + sex, add(add(juCount1sen, juCount1hei), add(juCount2, juCount3))); // 受験総数
                
                field = "REMARK5"; // 合格者数
                final String goCount1sen;
                final String goCount1hei;
                final String goCount2;
                final String goCount3;
                if (si == 3) {
                    goCount1sen = goTotal1sen;
                    goCount1hei = goTotal1hei;
                    goCount2 = goTotal2;
                    goCount3 = goTotal3;
                } else {
                    goCount1sen = tokeiValue(tokeiMap, keyTestdiv1sen, field);
                    goCount1hei = tokeiValue(tokeiMap, keyTestdiv1hei, field);
                    goCount2 = tokeiValue(tokeiMap, keyTestdiv2, field);
                    goCount3 = tokeiValue(tokeiMap, keyTestdiv3, field);
                    goTotal1sen = add(goTotal1sen, goCount1sen);
                    goTotal1hei = add(goTotal1hei, goCount1hei);
                    goTotal2 = add(goTotal2, goCount2);
                    goTotal3 = add(goTotal3, goCount3);
                }
                svf.VrsOut("PASS1_1_" + sex, add(goCount1sen, goCount1hei)); // 合格1次計
                svf.VrsOut("PASS1_2_" + sex, goCount1sen); // 合格1次専願
                svf.VrsOut("PASS1_3_" + sex, goCount1hei); // 合格1次併願
                svf.VrsOut("PASS2_" + sex, goCount2); // 合格1.5次
                svf.VrsOut("PASS3_" + sex, goCount3); // 合格2次
                svf.VrsOut("PASS_ALL" + sex, add(add(goCount1sen, goCount1hei), add(goCount2, goCount3))); // 合格総数
                
                field = "REMARK6"; // 入学者数
                final String nyuCount1sen;
                final String nyuCount1hei;
                final String nyuCount2;
                final String nyuCount3;
                if (si == 3) {
                    nyuCount1sen = nyuTotal1sen;
                    nyuCount1hei = nyuTotal1hei;
                    nyuCount2 = nyuTotal2;
                    nyuCount3 = nyuTotal3;
                } else {
                    nyuCount1sen = tokeiValue(tokeiMap, keyTestdiv1sen, field);
                    nyuCount1hei = tokeiValue(tokeiMap, keyTestdiv1hei, field);
                    nyuCount2 = tokeiValue(tokeiMap, keyTestdiv2, field);
                    nyuCount3 = tokeiValue(tokeiMap, keyTestdiv3, field);
                    nyuTotal1sen = add(nyuTotal1sen, nyuCount1sen);
                    nyuTotal1hei = add(nyuTotal1hei, nyuCount1hei);
                    nyuTotal2 = add(nyuTotal2, nyuCount2);
                    nyuTotal3 = add(nyuTotal3, nyuCount3);
                }
                svf.VrsOut("ENT1_1_" + sex, add(nyuCount1sen, nyuCount1hei)); // 入学1次計
                svf.VrsOut("ENT1_2_" + sex, nyuCount1sen); // 入学1次専願
                svf.VrsOut("ENT1_3_" + sex, nyuCount1hei); // 入学1次併願
                svf.VrsOut("ENT1_4_" + sex, percentage(nyuCount1hei, goCount1hei)); // 入学1次併願戻 ... 併願合格者 / 併願入学者のパーセンテージ
                svf.VrsOut("ENT2_" + sex, nyuCount2); // 入学1.5次
                svf.VrsOut("ENT3_" + sex, nyuCount3); // 入学2次
                svf.VrsOut("ENT_ALL" + sex, add(add(nyuCount1sen, nyuCount1hei), add(nyuCount2, nyuCount3))); // 入学総数
            }

            svf.VrEndRecord();
            _hasData = true;
        }
    }

    private static class Tokei {

        private static List getTableColumnInfo(final DB2UDB db2, final String tabname) {
            return getList(db2, " SELECT COLNAME, TYPENAME, NULLS FROM SYSCAT.COLUMNS WHERE TABNAME = '" + tabname + "' ORDER BY COLNO ");
        }
        
        private static String quote(final String s) {
            if (null == s) {
                return null;
            }
            return "'" + s + "'";
        }

        private static String toSqlValues(final List columnInfoList, final Param param, final Map data) {
            String sqlc = "(";
            String sqlv = " VALUES(";
            String sqlccm = "";
            String sqlvcm = "";
            for (final Iterator tit = columnInfoList.iterator(); tit.hasNext();) {
                final Map colInfo = (Map) tit.next();
                final String colName = getString(colInfo, "COLNAME");
                final boolean isNotNull = "N".equals(getString(colInfo, "NULLS"));
                final String typename = getString(colInfo, "TYPENAME");

                String v = null;
                if ("APPLICANTDIV".equals(colName)) {
                    v = getString(data, colName);
                } else if ("SHDIV".equals(colName)) {
                    v = StringUtils.defaultString((String) data.get(colName), "0");
                } else if ("SEX".equals(colName)) {
                    v = getString(data, colName);
                } else if ("ENTEXAMYEAR".equals(colName)) {
                    v = getString(data, colName);
                } else if ("KIND_CD".equals(colName)) {
                    v = getString(data, colName);
                } else if (colName.indexOf("REMARK") == 0) {
                    v = (String) data.get(colName);
                } else if ("TESTDIV".equals(colName)) {
                    v = getString(data, colName);
                } else if ("REGISTERCD".equals(colName)) {
                    v = "alp";
                } else if ("UPDATED".equals(colName)) {
                    v = "CURRENT TIMESTAMP";
                } else {
                    log.warn("unknown field:" + colName);
                    continue;
                }
                if (null == v) {
                    if (isNotNull) {
                        throw new IllegalStateException("null at not null field:" + colName);
                    }
                    continue;
                }
                if ("CHARACTER".equals(typename) || "LONG VARCHAR".equals(typename) || "VARCHAR".equals(typename)) {
                    v = quote(v);
                }
                sqlv += sqlvcm + v;
                sqlvcm = ", ";
                sqlc += sqlccm + colName;
                sqlccm = ", ";
            }
            sqlc += ")";
            sqlv += ")";
            return sqlc + sqlv;
        }

        private static void updateDB(final DB2UDB db2, final Param param, final Map tokeiMap) {
            PreparedStatement ps = null;
            Map debugTokei = null;
            try {
                String delsql;
                delsql  = " DELETE FROM ENTEXAM_TOKEI_HISTORY_DAT ";
                delsql += " WHERE ENTEXAMYEAR = '" + param._entexamyear + "' ";
                delsql += "   AND APPLICANTDIV = '" + param._applicantdiv + "' ";

                ps = db2.prepareStatement(delsql);
                ps.executeUpdate();
                DbUtils.closeQuietly(ps);
                
                final List columnInfoList = getTableColumnInfo(db2, "ENTEXAM_TOKEI_HISTORY_DAT");
                
                for (final Iterator it = tokeiMap.keySet().iterator(); it.hasNext();) {
                    final String tokeiKey = (String) it.next();
                    final Map data = (Map) tokeiMap.get(tokeiKey);
                    final String dataEntexamyear = getString(data, "ENTEXAMYEAR");
                    
                    if (!param._entexamyear.equals(dataEntexamyear)) {
                        continue;
                    }
                    
                    debugTokei = data;

                    String sql;
                    sql  = " INSERT INTO ENTEXAM_TOKEI_HISTORY_DAT ";
                    final String sqlValues = toSqlValues(columnInfoList, param, data);
                    sql += sqlValues;

                    log.debug("sqlValues = " + sqlValues);
                    ps = db2.prepareStatement(sql);
                    ps.executeUpdate();
                    DbUtils.closeQuietly(ps);
                }
                
                db2.commit();

            } catch (Exception e) {
                log.fatal("exception!:" + debugTokei, e);
                db2.rollback();
            }
        }

        private static Map getTokeiMap(final DB2UDB db2, final Param param) {
            final Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sqlLast = sqlLastYear(param);
                ps = db2.prepareStatement(sqlLast);
                rs = ps.executeQuery();
                final ResultSetMetaData meta = rs.getMetaData();
                while (rs.next()) {
                    final String shdiv = "0".equals(rs.getString("SHDIV")) ? "" : rs.getString("SHDIV");
                    final Map m = new HashMap();
                    for (int c = 1; c <= meta.getColumnCount(); c++) {
                        m.put(meta.getColumnName(c), rs.getString(c));
                    }
                    rtn.put(tokeiKey(rs.getString("ENTEXAMYEAR"), rs.getString("APPLICANTDIV"), rs.getString("TESTDIV"), shdiv, rs.getString("SEX")), m);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            try {
                final String sql = sql(param);
                log.debug(" sql year = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                final Map baseMap = new HashMap();
                baseMap.put("ENTEXAMYEAR",  param._entexamyear);
                baseMap.put("APPLICANTDIV",  param._applicantdiv);
                baseMap.put("KIND_CD", "4");
                while (rs.next()) {
                    baseMap.put("SEX", rs.getString("SEX"));
                    
                    final Map m11 = new HashMap(baseMap);
                    m11.put("TESTDIV", "1");
                    m11.put("SHDIV", "1");
                    m11.put("REMARK4", rs.getString("SHUTUGAN1_SENGAN"));
                    m11.put("REMARK5", rs.getString("GOKAKU1_SENGAN"));
                    m11.put("REMARK6", rs.getString("NYUGAKU1_SENGAN"));
                    rtn.put(tokeiKey(param._entexamyear, param._applicantdiv, m11.get("TESTDIV"), m11.get("SHDIV"), m11.get("SEX")), m11);
                    
                    final Map m12 = new HashMap(baseMap);
                    m12.put("TESTDIV", "1");
                    m12.put("SHDIV", "2");
                    m12.put("REMARK4", rs.getString("SHUTUGAN1_HEIGAN"));
                    m12.put("REMARK5", rs.getString("GOKAKU1_HEIGAN"));
                    m12.put("REMARK6", rs.getString("NYUGAKU1_HEIGAN"));
                    rtn.put(tokeiKey(param._entexamyear, param._applicantdiv, m12.get("TESTDIV"), m12.get("SHDIV"), m12.get("SEX")), m12);
                    
                    final Map m2 = new HashMap(baseMap);
                    m2.put("TESTDIV", "2");
                    m2.put("REMARK4", rs.getString("SHUTUGAN2"));
                    m2.put("REMARK5", rs.getString("GOKAKU2"));
                    m2.put("REMARK6", rs.getString("NYUGAKU2"));
                    rtn.put(tokeiKey(param._entexamyear, param._applicantdiv, m2.get("TESTDIV"), "", m2.get("SEX")), m2);
                    
                    final Map m3 = new HashMap(baseMap);
                    m3.put("TESTDIV", "3");
                    m3.put("REMARK4", rs.getString("SHUTUGAN3"));
                    m3.put("REMARK5", rs.getString("GOKAKU3"));
                    m3.put("REMARK6", rs.getString("NYUGAKU3"));
                    rtn.put(tokeiKey(param._entexamyear, param._applicantdiv, m3.get("TESTDIV"), "", m3.get("SEX")), m3);
                }

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        public static String sqlLastYear(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT  ");
            stb.append("   ENTEXAMYEAR, ");
            stb.append("   APPLICANTDIV, ");
            stb.append("   TESTDIV, ");
            stb.append("   SHDIV, ");
            stb.append("   SEX, ");
            stb.append("   KIND_CD, ");
            stb.append("   REMARK4, ");
            stb.append("   REMARK5, ");
            stb.append("   REMARK6 ");
            stb.append(" FROM  ");
            stb.append("   ENTEXAM_TOKEI_HISTORY_DAT  ");
            stb.append(" WHERE  ");
            stb.append("   ENTEXAMYEAR BETWEEN '" + String.valueOf(Integer.parseInt(param._entexamyear) - 4) + "' AND '" + String.valueOf(Integer.parseInt(param._entexamyear) - 1) + "'  ");
            stb.append("   AND KIND_CD = '4' ");
            return stb.toString();
        }

        private static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH TMP AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.EXAMNO ");
            stb.append("   , T1.TESTDIV ");
            stb.append("   , T1.SHDIV ");
            stb.append("   , T1.SEX ");
            stb.append("   , T1.JUDGEMENT ");
            stb.append("   , T1.PROCEDUREDIV ");
            stb.append("   , T1.ENTDIV ");
            stb.append(" FROM V_ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append(" INNER JOIN FINSCHOOL_MST T2 ON T2.FINSCHOOLCD = T1.FS_CD ");
            stb.append(" INNER JOIN NAME_MST NML001 ON NML001.NAMECD1 = 'L001' ");
            stb.append("     AND NML001.NAMECD2 = T2.FINSCHOOL_DISTCD ");
            stb.append(" LEFT JOIN PREF_MST T3 ON T3.PREF_CD =  SUBSTR(T2.FINSCHOOL_DISTCD, 1,2)  ");
            stb.append(" WHERE ");
            stb.append("     T1.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("     AND T1.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("   T1.SEX ");
            stb.append("   , SUM(CASE WHEN T1.TESTDIV = '1' AND T1.SHDIV = '1' THEN 1 END) AS SHUTUGAN1_SENGAN ");
            stb.append("   , SUM(CASE WHEN T1.TESTDIV = '1' AND T1.SHDIV = '2' THEN 1 END) AS SHUTUGAN1_HEIGAN ");
            stb.append("   , SUM(CASE WHEN T1.TESTDIV = '2' THEN 1 END) AS SHUTUGAN2 ");
            stb.append("   , SUM(CASE WHEN T1.TESTDIV = '3' THEN 1 END) AS SHUTUGAN3 ");
            stb.append("   , SUM(CASE WHEN T1.JUDGEMENT = '1' AND T1.TESTDIV = '1' AND T1.SHDIV = '1' THEN 1 END) AS GOKAKU1_SENGAN ");
            stb.append("   , SUM(CASE WHEN T1.JUDGEMENT = '1' AND T1.TESTDIV = '1' AND T1.SHDIV = '2' THEN 1 END) AS GOKAKU1_HEIGAN ");
            stb.append("   , SUM(CASE WHEN T1.JUDGEMENT = '1' AND T1.TESTDIV = '2' THEN 1 END) AS GOKAKU2 ");
            stb.append("   , SUM(CASE WHEN T1.JUDGEMENT = '1' AND T1.TESTDIV = '3' THEN 1 END) AS GOKAKU3 ");
            stb.append("   , SUM(CASE WHEN T1.JUDGEMENT = '1' AND T1.ENTDIV = '1' AND T1.TESTDIV = '1' AND T1.SHDIV = '1' THEN 1 END) AS NYUGAKU1_SENGAN ");
            stb.append("   , SUM(CASE WHEN T1.JUDGEMENT = '1' AND T1.ENTDIV = '1' AND T1.TESTDIV = '1' AND T1.SHDIV = '2' THEN 1 END) AS NYUGAKU1_HEIGAN ");
            stb.append("   , SUM(CASE WHEN T1.JUDGEMENT = '1' AND T1.ENTDIV = '1' AND T1.TESTDIV = '2' THEN 1 END) AS NYUGAKU2 ");
            stb.append("   , SUM(CASE WHEN T1.JUDGEMENT = '1' AND T1.ENTDIV = '1' AND T1.TESTDIV = '3' THEN 1 END) AS NYUGAKU3 ");
            stb.append(" FROM TMP T1 ");
            stb.append(" GROUP BY ");
            stb.append("     T1.SEX ");
            stb.append(" ORDER BY ");
            stb.append("   T1.SEX ");
            return stb.toString();
        }
    }

    private static class Param {
        final String _entexamyear;
        final String _loginDate;
        final String _applicantdiv;

        final String _applicantdivname;
        final  boolean _seirekiFlg;

        Param(
                final DB2UDB db2,
                final HttpServletRequest request
        ) {
            _entexamyear  = request.getParameter("YEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _loginDate    = request.getParameter("LOGIN_DATE");

            _seirekiFlg = getSeirekiFlg(db2);
            _applicantdivname = getApplicantdivName(db2);
        }

        private String getApplicantdivName(DB2UDB db2) {
            String schoolName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'L003' AND NAMECD2 = '" + _applicantdiv + "'");
                rs = ps.executeQuery();
                if (rs.next() && null != rs.getString("NAME1")) {
                  schoolName = rs.getString("NAME1");
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return schoolName;
        }

        /* 西暦表示にするのかのフラグ  */
        private boolean getSeirekiFlg(final DB2UDB db2) {
            final String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
            boolean seirekiFlg = false;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (rs.getString("NAME1").equals("2")) seirekiFlg = true; //西暦
                }
            } catch (Exception e) {
                log.error("getSeirekiFlg Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return seirekiFlg;
        }

        private String gethiduke(final String inputDate) {
            // 西暦か和暦はフラグで判断
            String date;
            if (null != inputDate) {
                if (_seirekiFlg) {
                    date = inputDate.toString().substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(inputDate);
                } else {
                    date = KNJ_EditDate.h_format_JP(inputDate);
                }
                return date;
            }
            return null;
        }
    }
}//クラスの括り
