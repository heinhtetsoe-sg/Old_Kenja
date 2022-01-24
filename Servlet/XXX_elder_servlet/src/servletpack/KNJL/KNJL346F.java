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

public class KNJL346F {

    private static final Log log = LogFactory.getLog(KNJL346F.class);

    private boolean _hasData;
    private Param _param;

    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

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

        final List dataListAll = getDataList(db2);
        final String title = KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "年度 " + StringUtils.defaultString(_param._applicantdivname) + "入試 重複受験者情報一覧表";
        final List pageList = getPageList(dataListAll, 50);
        final String form = "KNJL346F.frm";

        for (int pi = 0; pi < pageList.size(); pi++) {
            svf.VrSetForm(form, 1);
            svf.VrsOut("TITLE", title); // タイトル
            svf.VrsOut("EXAM_DIV", null); // 入試区分
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._loginDate)); // 日付
            svf.VrsOut("PAGE", String.valueOf(pi + 1) + "頁"); // ページ

            final List dataList = (List) pageList.get(pi);
            for (int j = 0; j < dataList.size(); j++) {
                final Map m = (Map) dataList.get(j);
                final int line = j + 1;
                svf.VrsOutn("NO", line, String.valueOf(line)); // 番号
                svf.VrsOutn("KANA", line, getString(m, "NAME_KANA")); // かな
                svf.VrsOutn("NAME", line, getString(m, "NAME")); // 氏名
                svf.VrsOutn("BIRTHDAY", line, KNJ_EditDate.h_format_JP(db2, getString(m, "BIRTHDAY", true))); // 生年月日
                svf.VrsOutn("SCHOOL_NAME", line, getString(m, "FINSCHOOL_NAME")); // 小学校名

                String[] tdivStr = {"1", "16", "2", "3", "5", "17", "18"};
                for (int idx = 0; idx < tdivStr.length; idx++) {
                    if (null != m.get("RECEPTNO" + tdivStr[idx])) {
                        svf.VrsOutn("EXAM_NO" + (idx + 1), line, getString(m, "RECEPTNO" + tdivStr[idx])); // 受験番号
                        svf.VrsOutn("JUDGE" + (idx + 1), line, getString(m, "JUDGEDIV_NAME" + tdivStr[idx])); // 合否
                    }
                }
                for (int ii = 0; ii < 6; ii++) {
                    final String testdiv = String.valueOf(ii + 9);
                    if (null != m.get("RECEPTNO" + testdiv)) {
                        svf.VrsOutn("EXAM_NO" + testdiv, line, getString(m, "RECEPTNO" + testdiv)); // 受験番号
                        svf.VrsOutn("JUDGE" + testdiv, line, getString(m, "JUDGEDIV_NAME" + testdiv)); // 合否
                    }

                }

                //svf.VrsOutn("PASS_COURSE", line, null); // 合格コース
            }
            svf.VrEndPage();
            _hasData = true;
        }
    }

    private static String getString(final Map m, final String field) {
    	return getString(m, field, false);
    }
    private static String getString(final Map m, final String field, final boolean bretNull) {
        if (null == m) {
            return bretNull ? null : "";
        }
        if (!m.containsKey(field)) {
            log.error("not defined: " + field + " in " + m.keySet());
            return bretNull ? null : "";
        }
        return (String) m.get(field);
    }

    private static List getPageList(final List list, final int max) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Map m = (Map) it.next();
            if (null == current || current.size() >= max) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(m);
        }
        return rtn;
    }

    private List getDataList(final DB2UDB db2) {
        final List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        ResultSetMetaData meta;
        final Map examnoDataMap = new HashMap();
        try {
            final String sql = getDataSql(_param);
            log.debug(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            meta = rs.getMetaData();
            while (rs.next()) {
                final String examno = rs.getString("EXAMNO");
                if (null == examnoDataMap.get(examno)) {
                    final Map m = new HashMap();
                    for (int i = 1; i<= meta.getColumnCount(); i++) {
                        final String columnName = meta.getColumnLabel(i);
                        if ("TESTDIV".equals(columnName) || "JUDGEDIV_NAME".equals(columnName)) {
                            continue;
                        }
                        final String data = rs.getString(columnName);
                        m.put(columnName, data);
                    }
                    list.add(m);
                    examnoDataMap.put(examno, m);
                }
                if (null != rs.getString("RECEPTNO")) {
                    final Map examnoData = (Map) examnoDataMap.get(examno);
                    final String testdiv = rs.getString("TESTDIV");
                    examnoData.put("RECEPTNO" + testdiv, rs.getString("RECEPTNO"));
                    examnoData.put("JUDGEDIV" + testdiv, rs.getString("JUDGEDIV"));
                    examnoData.put("JUDGEDIV_NAME" + testdiv, rs.getString("JUDGEDIV_NAME"));
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

//    private static String sum(final List list) {
//        Integer rtn = null;
//        for (final Iterator it = list.iterator(); it.hasNext();) {
//            final String v = (String) it.next();
//            if (NumberUtils.isDigits(v)) {
//                if (null == rtn) {
//                    rtn = new Integer(0);
//                }
//                rtn = new Integer(rtn.intValue() + Integer.parseInt(v));
//            }
//        }
//        if (null == rtn) {
//            return null;
//        }
//        return rtn.toString();
//    }

//    private static int getMS932Bytecount(String str) {
//        int count = 0;
//        if (null != str) {
//            try {
//                count = str.getBytes("MS932").length;
//            } catch (Exception e) {
//                log.error(e);
//            }
//        }
//        return count;
//    }

//    private static List getMappedList(final Map map, final Object key1) {
//        if (!map.containsKey(key1)) {
//            map.put(key1, new ArrayList());
//        }
//        return (List) map.get(key1);
//    }
//
//
//    private static Map getMappedHashMap(final Map map, final Object key1) {
//        if (!map.containsKey(key1)) {
//            map.put(key1, new HashMap());
//        }
//        return (Map) map.get(key1);
//    }

    private String getDataSql(final Param param) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     APPBASE.EXAMNO ");
        stb.append("     ,APPBASE.NAME ");
        stb.append("     ,APPBASE.NAME_KANA ");
        stb.append("     ,APPBASE.BIRTHDAY ");
        stb.append("     ,L4.FINSCHOOL_NAME ");
        stb.append("     ,VEATD.TESTDIV ");
        stb.append("     ,VEATD.RECEPTNO ");
        stb.append("     ,TREC.JUDGEDIV ");
        stb.append("     ,NML013.NAME1 AS JUDGEDIV_NAME ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT APPBASE ");
        stb.append("     INNER JOIN V_ENTEXAM_APPLICANTBASE_TESTDIV_DAT VEATD ON APPBASE.ENTEXAMYEAR = VEATD.ENTEXAMYEAR ");
        stb.append("                                    AND APPBASE.APPLICANTDIV = VEATD.APPLICANTDIV ");
        stb.append("                                    AND APPBASE.EXAMNO = VEATD.EXAMNO ");
        stb.append("     LEFT JOIN V_ENTEXAM_RECEPT_DAT TREC ON TREC.ENTEXAMYEAR = VEATD.ENTEXAMYEAR ");
        stb.append("                                    AND TREC.APPLICANTDIV = VEATD.APPLICANTDIV ");
        stb.append("                                    AND TREC.TESTDIV = VEATD.TESTDIV ");
        stb.append("                                    AND TREC.EXAM_TYPE = '1' ");
        stb.append("                                    AND TREC.RECEPTNO = VEATD.RECEPTNO ");
        stb.append("     LEFT JOIN FINSCHOOL_MST L4 ON L4.FINSCHOOLCD = APPBASE.FS_CD ");
        stb.append("     LEFT JOIN NAME_MST NML013 ON NML013.NAMECD1 = 'L013' AND NML013.NAMECD2 = TREC.JUDGEDIV ");
        stb.append(" WHERE ");
        stb.append("         APPBASE.ENTEXAMYEAR = '" + param._entexamyear + "' ");
        stb.append("     AND APPBASE.APPLICANTDIV = '" + param._applicantdiv + "' ");
        stb.append("     AND APPBASE.TESTDIV = '2' "); // 1:帰国生 2:一般
        stb.append(" ORDER BY int(VEATD.TESTDIV), APPBASE.EXAMNO ");
        return stb.toString();
    }

    private static class Param {
        final String _entexamyear;
        final String _loginDate;
        final String _applicantdiv;

        final String _applicantdivname;

        private boolean _seirekiFlg;

        Param(
                final DB2UDB db2,
                final HttpServletRequest request
        ) {
            _entexamyear  = request.getParameter("ENTEXAMYEAR");
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

//        private String gethiduke(final String inputDate) {
//            // 西暦か和暦はフラグで判断
//            String date;
//            if (null != inputDate) {
//                if (_seirekiFlg) {
//                    date = inputDate.toString().substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(inputDate);
//                } else {
//                    date = KNJ_EditDate.h_format_JP(inputDate);
//                }
//                return date;
//            }
//            return null;
//        }
    }
}//クラスの括り
