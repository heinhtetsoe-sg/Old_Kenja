package servletpack.KNJL;

import java.io.IOException;
import java.io.PrintWriter;
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

public class KNJL337F {

    private static final Log log = LogFactory.getLog(KNJL337F.class);

    private boolean _hasData;
    private Param _param;

    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        Vrw32alp svf     = new Vrw32alp();   //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2       = null;             //Databaseクラスを継承したクラス

        log.fatal("$Revision: 63590 $ $Date: 2018-11-26 15:10:11 +0900 (月, 26 11 2018) $"); // CVSキーワードの取り扱いに注意

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
            setSvfMain(db2, svf); //帳票出力のメソッド

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

    /**
     * フォームの出力
     * @param db2
     * @param svf
     * @return
     */
    private void setSvfMain(final DB2UDB db2, final Vrw32alp svf) {
        final List dataListAll = getList(db2);

        String title0 = KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "年度 " + StringUtils.defaultString(_param._applicantdivname) + "入試 " + StringUtils.defaultString(_param._testdivname);
        if ("2".equals(_param._applicantdiv) && null != _param._applicantdiv2Test2nd) {
            title0 += "(" + StringUtils.defaultString(_param._testdiv0name) + ")";
        }

        int maxLine = 0;
        String form = "";
        String title1 = "不合格者";
        form = "KNJL337F_H.frm";
        maxLine = 50;
        final List pageList = getPageList(dataListAll, maxLine, false);
        svf.VrSetForm(form, 4);
        for (int pi = 0; pi < pageList.size(); pi++) {
            final List dataList = (List) pageList.get(pi);

            svf.VrsOut("TITLE", title0 + " " + title1 + "一覧表"); // タイトル
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate)); // 日付
            svf.VrsOut("PAGE", String.valueOf(pi + 1) + "頁"); // ページ
            for (int i = 0; i < dataList.size(); i++) {

                final Map m = (Map) dataList.get(i);
                svf.VrsOut("NO", String.valueOf(maxLine * pi + i + 1)); // NO
                svf.VrsOut("EXAM_NO", getString(m, "RECEPTNO")); // 受験番号
                svf.VrsOut("NAME", getString(m, "NAME")); // 氏名
                svf.VrsOut("CITY_NAME", getString(m, "FINSCHOOL_DISTCD_NAME")); // 市区町村
                svf.VrsOut("SCHOOL_DUV", getString(m, "FINSCHOOL_DIV_NAME")); // 設立
                svf.VrsOut("SCHOOL_NAME", getString(m, "FINSCHOOL_NAME")); // 中学校名
                svf.VrsOut("REMARK", getString(m, "HONORDIV_NAME")); // 備考
                svf.VrEndRecord();
            }
            for (int i = dataList.size(); i < maxLine; i++) {
                svf.VrsOut("NO", String.valueOf(maxLine * pi + i + 1)); // NO
                svf.VrAttribute("NO", "X=10000");
                svf.VrEndRecord();
            }
            _hasData = true;
        }
    }

    private String formatBirthday(final String date) {
        if (null == date) {
            return null;
        }
        final StringBuffer stb = new StringBuffer();
        try {
            final String[] split = StringUtils.split(date, "-");
            stb.append(KenjaProperties.gengou(Integer.parseInt(split[0])));
            stb.append(".");
            if (NumberUtils.isDigits(split[1])) {
                final int m = Integer.parseInt(split[1]);
                if (m < 10) {
                    stb.append(" ");
                }
                stb.append(m);
            }
            stb.append(".");
            if (NumberUtils.isDigits(split[2])) {
                final int d = Integer.parseInt(split[2]);
                if (d < 10) {
                    stb.append(" ");
                }
                stb.append(d);
            }
        } catch (Exception e) {
            log.error("formatBirthday exception:" + date, e);
        }
        return stb.toString();
    }

    private static String getString(final Map m, final String field) {
        if (null == m) {
            return null;
        }
        if (!m.containsKey(field)) {
            log.error("not defined: " + field + " in " + m.keySet());
        }
        return (String) m.get(field);
    }

    private static List getPageList(final List list, final int max, final boolean checkTyohuku) {
        final List listTyofuku = new ArrayList();
        for (int j = 0; j < list.size(); j++) {
            final Map m = (Map) list.get(j);
            listTyofuku.add(m);
            if (null == m) {
                continue;
            }
            m.put("NO", String.valueOf(j + 1));
            if (checkTyohuku) {
                Map tyofukuMap = m;
                for (int i = 0; i < 7; i++) {
                    final String otherReceptno = getString(m, "RECEPTNO" + String.valueOf(i + 1));
                    if (null != otherReceptno && !otherReceptno.equals(getString(m, "RECEPTNO"))) {
                        if (getMappedList(tyofukuMap, "OTHER_RECEPTNO").size() >= 5) {
                            tyofukuMap = new HashMap();
                            tyofukuMap.put("TYOFUKU", "1");
                            listTyofuku.add(tyofukuMap);
                        }
                        getMappedList(tyofukuMap, "OTHER_RECEPTNO").add(otherReceptno);
                    }
                }
            }
        }

        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = listTyofuku.iterator(); it.hasNext();) {
            final Map m = (Map) it.next();
            if (null == current || current.size() >= max) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(m);
        }
        return rtn;
    }

    private List getList(final DB2UDB db2) {
        final List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        ResultSetMetaData meta;
        try {
            final String sql = sql(_param);
            log.debug(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            meta = rs.getMetaData();
            while (rs.next()) {
                final Map m = new HashMap();
                for (int i = 1; i<= meta.getColumnCount(); i++) {
                    final String columnName = meta.getColumnLabel(i);
                    final String data = rs.getString(columnName);
                    m.put(columnName, data);
                }
                list.add(m);
            }
        } catch (Exception e) {
            log.debug("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        try {
            final String sql = scoreSql(_param);
            log.debug(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final Map m = (Map) it.next();
                ps.setString(1, getString(m, "TESTDIV"));
                ps.setString(2, getString(m, "RECEPTNO"));
                rs = ps.executeQuery();
                final Map locScore = new HashMap();
                while (rs.next()) {
                    if (null == rs.getString("SCORE")) {
                        continue;
                    }
                    final String testsubclasscd = rs.getString("TESTSUBCLASSCD");
                    if ("1".equals(testsubclasscd) || "2".equals(testsubclasscd)) {
                        getMappedList(locScore, testsubclasscd).add(rs.getString("SCORE"));
                    } else if (null != getString(m, "SCORE1_SUBCLASSCD") && getString(m, "SCORE1_SUBCLASSCD").equals(testsubclasscd) ||
                                null != getString(m, "SCORE2_SUBCLASSCD") && getString(m, "SCORE2_SUBCLASSCD").equals(testsubclasscd)
                               ) {
                        final String loc = (String) _param._subclassnamespare1Map.get(testsubclasscd);
                        if (null != loc) {
                            getMappedList(locScore, loc).add(rs.getString("SCORE"));
                        }
                    }
                }
                DbUtils.closeQuietly(rs);

                final Map scoreMap = getMappedHashMap(m, "SET_SUBCLASS_SCORE");
                for (final Iterator its = locScore.keySet().iterator(); its.hasNext();) {
                    final String loc = (String) its.next();
                    scoreMap.put(loc, sum(getMappedList(locScore, loc)));
                }
            }
        } catch (Exception e) {
            log.debug("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        return list;
    }

    private static String sum(final List list) {
        Integer rtn = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final String v = (String) it.next();
            if (NumberUtils.isDigits(v)) {
                if (null == rtn) {
                    rtn = new Integer(0);
                }
                rtn = new Integer(rtn.intValue() + Integer.parseInt(v));
            }
        }
        if (null == rtn) {
            return null;
        }
        return rtn.toString();
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

    private static List getMappedList(final Map map, final Object key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList());
        }
        return (List) map.get(key1);
    }


    private static Map getMappedHashMap(final Map map, final Object key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new HashMap());
        }
        return (Map) map.get(key1);
    }


    private static String scoreSql(final Param param) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     TREC.TESTSUBCLASSCD ");
        stb.append("   , TREC.SCORE ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_SCORE_DAT TREC ");
        stb.append(" WHERE ");
        stb.append("     TREC.ENTEXAMYEAR = '" + param._entexamyear + "' ");
        stb.append("     AND TREC.APPLICANTDIV = '" + param._applicantdiv + "' ");
        stb.append("     AND TREC.TESTDIV = ? ");
        stb.append("     AND TREC.EXAM_TYPE = '1' ");
        stb.append("     AND TREC.RECEPTNO = ? ");
        return stb.toString();
    }

    private String sql(final Param param) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     TREC.TESTDIV ");
        stb.append("     ,T2.NAME ");
        stb.append("     ,TREC.EXAMNO ");
        stb.append("     ,T2.NAME_KANA ");
        stb.append("     ,L3.ABBV1 AS SEX_NAME ");
        stb.append("     ,T2.BIRTHDAY ");
        stb.append("     ,L4.FINSCHOOL_NAME ");
        stb.append("     ,TREC.RECEPTNO ");
        stb.append("     ,TREC.TOTAL2 ");
        stb.append("     ,TREC.EXAM_TYPE ");
        stb.append("     ,NML013.NAME1 AS JUDGEDIV_NAME ");
        if ("1".equals(param._applicantdiv)) {
            stb.append("     ,NML025.NAME1 AS HONORDIV_NAME ");
        } else {
            stb.append("     ,NML025.NAME2 AS HONORDIV_NAME ");
        }
        stb.append("     ,NML001.NAME1 AS FINSCHOOL_DISTCD_NAME ");
        stb.append("     ,NML015.NAME1 AS FINSCHOOL_DIV_NAME ");
        stb.append("   , TRDET004.REMARK1 AS SCORE1_SUBCLASSCD ");
        stb.append("   , TRDET004.REMARK2 AS SCORE2_SUBCLASSCD ");
        stb.append("     ,TD2.REMARK1 AS RECEPTNO1 ");
        stb.append("     ,TD2.REMARK2 AS RECEPTNO2 ");
        stb.append("     ,TD2.REMARK3 AS RECEPTNO3 ");
        stb.append("     ,TD2.REMARK4 AS RECEPTNO4 ");
        stb.append("     ,TD2.REMARK5 AS RECEPTNO5 ");
        stb.append("     ,TD2.REMARK6 AS RECEPTNO6 ");
        stb.append("     ,TD2.REMARK7 AS RECEPTNO7 ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_RECEPT_DAT TREC ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ON T2.ENTEXAMYEAR = TREC.ENTEXAMYEAR ");
        stb.append("                                    AND T2.APPLICANTDIV = TREC.APPLICANTDIV ");
        stb.append("                                    AND T2.EXAMNO       = TREC.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT TD2 ON TD2.ENTEXAMYEAR = TREC.ENTEXAMYEAR ");
        stb.append("                                    AND TD2.EXAMNO       = TREC.EXAMNO ");
        stb.append("                                    AND TD2.SEQ          = '012' ");
        stb.append("     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT TRDET003 ON TRDET003.ENTEXAMYEAR = TREC.ENTEXAMYEAR ");
        stb.append("         AND TRDET003.APPLICANTDIV = TREC.APPLICANTDIV ");
        stb.append("         AND TRDET003.TESTDIV = TREC.TESTDIV ");
        stb.append("         AND TRDET003.EXAM_TYPE = TREC.EXAM_TYPE ");
        stb.append("         AND TRDET003.RECEPTNO = TREC.RECEPTNO ");
        stb.append("         AND TRDET003.SEQ = '003' ");
        stb.append("     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT TRDET004 ON TRDET004.ENTEXAMYEAR = TREC.ENTEXAMYEAR ");
        stb.append("         AND TRDET004.APPLICANTDIV = TREC.APPLICANTDIV ");
        stb.append("         AND TRDET004.TESTDIV = TREC.TESTDIV ");
        stb.append("         AND TRDET004.EXAM_TYPE = TREC.EXAM_TYPE ");
        stb.append("         AND TRDET004.RECEPTNO = TREC.RECEPTNO ");
        stb.append("         AND TRDET004.SEQ = '004' ");
        stb.append("     LEFT JOIN NAME_MST L3 ON L3.NAMECD1 = 'Z002' AND L3.NAMECD2 = T2.SEX ");
        stb.append("     LEFT JOIN FINSCHOOL_MST L4 ON L4.FINSCHOOLCD = T2.FS_CD ");
        stb.append("     LEFT JOIN NAME_MST NML013 ON NML013.NAMECD1 = 'L013' AND NML013.NAMECD2 = TREC.JUDGEDIV ");
        stb.append("     LEFT JOIN NAME_MST NML025 ON NML025.NAMECD1 = 'L025' AND NML025.NAMECD2 = TREC.HONORDIV ");
        stb.append("     LEFT JOIN NAME_MST NML001 ON NML001.NAMECD1 = 'L001' AND NML001.NAMECD2 = L4.FINSCHOOL_DISTCD ");
        stb.append("     LEFT JOIN NAME_MST NML015 ON NML015.NAMECD1 = 'L015' AND NML015.NAMECD2 = L4.FINSCHOOL_DIV ");
        stb.append(" WHERE ");
        stb.append("         TREC.ENTEXAMYEAR = '" + param._entexamyear + "' ");
        stb.append("     AND TREC.APPLICANTDIV = '" + param._applicantdiv + "' ");
        if (!"ALL".equals(_param._testdiv)) {
            stb.append("     AND TREC.TESTDIV = '" + param._testdiv + "' ");
            if ("2".equals(param._applicantdiv)) {
                stb.append("     AND TRDET003.REMARK1 = '" + param._testdiv0 + "' ");
            }
        }
        stb.append("     AND (TREC.TESTDIV <> '3' OR TREC.TESTDIV = '3' AND VALUE(T2.GENERAL_FLG, '') <> '1') ");
        stb.append("     AND TREC.EXAM_TYPE = '1' ");
        stb.append("     AND TREC.JUDGEDIV = '2' ");
        stb.append(" ORDER BY TREC.TESTDIV ");
        if ("2".equals(_param._applicantdiv)) {
            stb.append(" , T2.TESTDIV0 ");
        }
        stb.append(" , TREC.RECEPTNO ");
        return stb.toString();
    }

    private static class Param {
        final String _entexamyear;
        final String _loginDate;
        final String _applicantdiv;
        final String _testdiv;
        final String _testdiv0; // 入試回数

        final String _z010Name1;
        final String _applicantdivname;
        final String _testdivname;
        final String _applicantdiv2Test2nd;
        final String _testdiv0name;
        final Map _subclassnameMap;
        final Map _subclassnamespare1Map;

        private boolean _seirekiFlg;
        private String _principalName = "";
        private String _jobName = "";
        private String _schoolName = "";

        Param(
                final DB2UDB db2,
                final HttpServletRequest request
        ) {
            _entexamyear  = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv      = request.getParameter("TESTDIV");
            _testdiv0     = request.getParameter("TESTDIV0");
            _loginDate    = request.getParameter("LOGIN_DATE");
            _z010Name1 = getSchoolName(db2);

            setCertifSchoolDat(db2);
            _seirekiFlg = getSeirekiFlg(db2);
            _applicantdivname = getApplicantdivName(db2);
            _testdivname = getTestDivName(db2, "NAME1");
            _applicantdiv2Test2nd = getTestDivName(db2, "NAME3");
            _testdiv0name = getTestdiv0Name(db2);
            _subclassnameMap = getTestsubclasNameMap(db2);
            _subclassnamespare1Map = getTestsubclasNamespare1Map(db2);
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

        private String getTestDivName(DB2UDB db2, final String field) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String namecd1 = "1".equals(_applicantdiv) ? "L024" : "L004";
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + _testdiv + "'");
                rs = ps.executeQuery();
                if (rs.next() && null != rs.getString(field)) {
                  rtn = rs.getString(field);
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private void setCertifSchoolDat(final DB2UDB db2) {
            final String certifKindCd;
            if (_applicantdiv.equals("1")) {
                certifKindCd = "105";
            } else {
                certifKindCd = "106";
            }

            final String sql = "SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _entexamyear + "' AND CERTIF_KINDCD = '" + certifKindCd + "' ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _principalName = rs.getString("PRINCIPAL_NAME");
                    _jobName = rs.getString("JOB_NAME");
                    _schoolName = rs.getString("SCHOOL_NAME");
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
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

        private String getSchoolName(DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   NAME1 ");
            stb.append(" FROM ");
            stb.append("   NAME_MST T1 ");
            stb.append(" WHERE ");
            stb.append("   T1.NAMECD1 = 'Z010' ");
            stb.append("   AND T1.NAMECD2 = '00' ");

            String name1 = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    name1 = rs.getString("NAME1");
                }
            } catch (Exception e) {
                log.error("getSchoolName Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return name1;
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

        private String getTestdiv0Name(final DB2UDB db2) {
            final String namecd1 = "L034";
            String testDiv0Name = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + _testdiv0 + "'");
                rs = ps.executeQuery();
                if (rs.next() && null != rs.getString("NAME1")) {
                  testDiv0Name = rs.getString("NAME1");
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return testDiv0Name;
        }

        private Map getTestsubclasNameMap(final DB2UDB db2) {
            final String namecd1 = "L009";
            final String field = "1".equals(_applicantdiv) ? "NAME1" : "NAME2";
            Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT NAMECD2, " + field + " FROM V_NAME_MST WHERE YEAR = '" + _entexamyear + "' AND NAMECD1 = '" + namecd1 + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn.put(rs.getString("NAMECD2"), rs.getString(field));
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private Map getTestsubclasNamespare1Map(final DB2UDB db2) {
            final String namecd1 = "L009";
            Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT NAMECD2, NAMESPARE1 FROM V_NAME_MST WHERE YEAR = '" + _entexamyear + "' AND NAMECD1 = '" + namecd1 + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn.put(rs.getString("NAMECD2"), rs.getString("NAMESPARE1"));
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

    }
}//クラスの括り
