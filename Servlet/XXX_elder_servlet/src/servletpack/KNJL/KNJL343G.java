package servletpack.KNJL;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

public class KNJL343G {

    private static final Log log = LogFactory.getLog(KNJL343G.class);

    private boolean _hasData;
    private Param _param;

    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        log.fatal("$Revision: 57305 $ $Date: 2017-11-29 13:43:17 +0900 (水, 29 11 2017) $"); // CVSキーワードの取り扱いに注意

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final String form = "KNJL343G.frm";
        final int maxLine = 50;
        final List dataListAll = Tokei.getTokeiList(db2, _param);
        final List pageList = getPageList(dataListAll, maxLine);
        svf.VrSetForm(form, 1);
        
        for (int pi = 0; pi < pageList.size(); pi++) {
            svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "年度　" + "中学校別入試応募一覧表"); // 
            svf.VrsOut("SUBTITLE", "（" + _param._testdivName1 + "）");
            svf.VrsOut("PAGE", String.valueOf(pi + 1)); // ページ
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate)); // 
            
            final List dataList = (List) pageList.get(pi);
            for (int j = 0; j < dataList.size(); j++) {
                final int line = j + 1;
                final Tokei tokei = (Tokei) dataList.get(j);
                if (null == tokei._prefCd) {
                    // 総合計
                    svf.VrsOutn("CITY_NAME", line, "総合計"); // 市町村名
                } else if (null == tokei._finschoolDistcd) {
                    // 都道府県単位
                    svf.VrsOutn("CITY_NAME", line, StringUtils.defaultString(tokei._prefName) + "計"); // 市町村名
                } else if (null == tokei._finschoolcd) {
                    // 市町村単位
                    svf.VrsOutn("CITY_NAME", line, "小計"); // 市町村名
                } else {
                    // 学校単位
                    svf.VrsOutn("CITY_NAME", line, tokei._finschoolDistcdName); // 市町村名
                    svf.VrsOutn("FINSCHOOL_NAME1", line, StringUtils.defaultString(tokei._finschoolName) + "中学校"); // 出身学校名
                }

                svf.VrsOutn("HOPE1_1", line, getVal(tokei._shiganSengan1)); // 志願者 専願 男
                svf.VrsOutn("HOPE1_2", line, getVal(tokei._shiganSengan2)); // 志願者 専願 女
                svf.VrsOutn("HOPE1_3", line, getVal(tokei._shiganSengan)); // 志願者 専願
                svf.VrsOutn("HOPE2_1", line, getVal(tokei._shiganHeigan1)); // 志願者 併願 男
                svf.VrsOutn("HOPE2_2", line, getVal(tokei._shiganHeigan2)); // 志願者 併願 女
                svf.VrsOutn("HOPE2_3", line, getVal(tokei._shiganHeigan)); // 志願者 併願
                svf.VrsOutn("HOPE3_1", line, getVal(tokei._shigan1)); // 志願者 男
                svf.VrsOutn("HOPE3_2", line, getVal(tokei._shigan2)); // 志願者 女
                svf.VrsOutn("HOPE3_3", line, getVal(tokei._shigan)); // 志願者

                svf.VrsOutn("PASS1_1", line, getVal(tokei._gokakuSengan1)); // 合格者 専願 男
                svf.VrsOutn("PASS1_2", line, getVal(tokei._gokakuSengan2)); // 合格者 専願 女
                svf.VrsOutn("PASS1_3", line, getVal(tokei._gokakuSengan)); // 合格者 専願
                svf.VrsOutn("PASS2_1", line, getVal(tokei._gokakuHeigan1)); // 合格者 併願 男
                svf.VrsOutn("PASS2_2", line, getVal(tokei._gokakuHeigan2)); // 合格者 併願 女
                svf.VrsOutn("PASS2_3", line, getVal(tokei._gokakuHeigan)); // 合格者 併願
                svf.VrsOutn("PASS3_1", line, getVal(tokei._gokaku1)); // 合格者 男
                svf.VrsOutn("PASS3_2", line, getVal(tokei._gokaku2)); // 合格者 女
                svf.VrsOutn("PASS3_3", line, getVal(tokei._gokaku)); // 合格者

                svf.VrsOutn("ENT1_1", line, getVal(tokei._nyugakuSengan1)); // 入学者 専願 男
                svf.VrsOutn("ENT1_2", line, getVal(tokei._nyugakuSengan2)); // 入学者 専願 女
                svf.VrsOutn("ENT1_3", line, getVal(tokei._nyugakuSengan)); // 入学者 専願
                svf.VrsOutn("ENT2_1", line, getVal(tokei._nyugakuHeigan1)); // 入学者 併願 男
                svf.VrsOutn("ENT2_2", line, getVal(tokei._nyugakuHeigan2)); // 入学者 併願 女
                svf.VrsOutn("ENT2_3", line, getVal(tokei._nyugakuHeigan)); // 入学者 併願
                svf.VrsOutn("ENT3_1", line, getVal(tokei._nyugaku1)); // 入学者 男
                svf.VrsOutn("ENT3_2", line, getVal(tokei._nyugaku2)); // 入学者 女
                svf.VrsOutn("ENT3_3", line, getVal(tokei._nyugaku)); // 入学者
            }
            svf.VrEndPage();
            _hasData = true;
        }
        
    }

    private String getVal(String val) {
        //if (null == val) return "0";
        return val;
    }

    private static class Tokei {
        String _prefCd;
        String _prefName;
        String _finschoolDistcd;
        String _finschoolDistcdName;
        String _finschoolcd;
        String _finschoolDiv;
        String _finschoolName;
        String _shiganSengan1;
        String _shiganSengan2;
        String _shiganSengan;
        String _shiganHeigan1;
        String _shiganHeigan2;
        String _shiganHeigan;
        String _shigan1;
        String _shigan2;
        String _shigan;
        String _gokakuSengan1;
        String _gokakuSengan2;
        String _gokakuSengan;
        String _gokakuHeigan1;
        String _gokakuHeigan2;
        String _gokakuHeigan;
        String _gokaku1;
        String _gokaku2;
        String _gokaku;
        String _nyugakuSengan1;
        String _nyugakuSengan2;
        String _nyugakuSengan;
        String _nyugakuHeigan1;
        String _nyugakuHeigan2;
        String _nyugakuHeigan;
        String _nyugaku1;
        String _nyugaku2;
        String _nyugaku;

        public static List getTokeiList(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Tokei tokei = new Tokei();
                    tokei._prefCd = rs.getString("PREF_CD");
                    tokei._prefName = rs.getString("PREF_NAME");
                    tokei._finschoolDistcd = rs.getString("DISTRICTCD");
                    tokei._finschoolDistcdName = rs.getString("DISTRICTCD_NAME");
                    tokei._finschoolcd = rs.getString("FINSCHOOLCD");
                    tokei._finschoolDiv = rs.getString("FINSCHOOL_DIV");
                    tokei._finschoolName = rs.getString("FINSCHOOL_NAME");
                    tokei._shiganSengan1 = rs.getString("SHIGAN_SENGAN1");
                    tokei._shiganSengan2 = rs.getString("SHIGAN_SENGAN2");
                    tokei._shiganSengan = rs.getString("SHIGAN_SENGAN");
                    tokei._shiganHeigan1 = rs.getString("SHIGAN_HEIGAN1");
                    tokei._shiganHeigan2 = rs.getString("SHIGAN_HEIGAN2");
                    tokei._shiganHeigan = rs.getString("SHIGAN_HEIGAN");
                    tokei._shigan1 = rs.getString("SHIGAN1");
                    tokei._shigan2 = rs.getString("SHIGAN2");
                    tokei._shigan = rs.getString("SHIGAN");
                    tokei._gokakuSengan1 = rs.getString("GOKAKU_SENGAN1");
                    tokei._gokakuSengan2 = rs.getString("GOKAKU_SENGAN2");
                    tokei._gokakuSengan = rs.getString("GOKAKU_SENGAN");
                    tokei._gokakuHeigan1 = rs.getString("GOKAKU_HEIGAN1");
                    tokei._gokakuHeigan2 = rs.getString("GOKAKU_HEIGAN2");
                    tokei._gokakuHeigan = rs.getString("GOKAKU_HEIGAN");
                    tokei._gokaku1 = rs.getString("GOKAKU1");
                    tokei._gokaku2 = rs.getString("GOKAKU2");
                    tokei._gokaku = rs.getString("GOKAKU");
                    tokei._nyugakuSengan1 = rs.getString("NYUGAKU_SENGAN1");
                    tokei._nyugakuSengan2 = rs.getString("NYUGAKU_SENGAN2");
                    tokei._nyugakuSengan = rs.getString("NYUGAKU_SENGAN");
                    tokei._nyugakuHeigan1 = rs.getString("NYUGAKU_HEIGAN1");
                    tokei._nyugakuHeigan2 = rs.getString("NYUGAKU_HEIGAN2");
                    tokei._nyugakuHeigan = rs.getString("NYUGAKU_HEIGAN");
                    tokei._nyugaku1 = rs.getString("NYUGAKU1");
                    tokei._nyugaku2 = rs.getString("NYUGAKU2");
                    tokei._nyugaku = rs.getString("NYUGAKU");
                    list.add(tokei);
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
            stb.append(" WITH TMP AS ( ");
            stb.append(" SELECT ");
            stb.append("     BASE.EXAMNO ");
            stb.append("   , BASE.SHDIV ");
            stb.append("   , BASE.SEX ");
            stb.append("   , BASE.JUDGEMENT ");
            stb.append("   , BASE.PROCEDUREDIV ");
            stb.append("   , BASE.ENTDIV ");
            stb.append("   , VALUE(FINS.FINSCHOOL_PREF_CD, '') AS PREF_CD ");
            stb.append("   , VALUE(FINS.DISTRICTCD, '') AS DISTRICTCD ");
            stb.append("   , NMZ003.NAME1 AS DISTRICTCD_NAME ");
            stb.append("   , FINS.FINSCHOOL_DIV ");
            stb.append("   , FINS.FINSCHOOLCD ");
            stb.append("   , FINS.FINSCHOOL_NAME ");
            stb.append("   , '1' AS TOTAL_DUMMY ");
            stb.append("   , T_PREF.PREF_NAME ");
            stb.append(" FROM ENTEXAM_APPLICANTBASE_DAT BASE ");
            stb.append(" INNER JOIN FINSCHOOL_MST FINS ON FINS.FINSCHOOLCD = BASE.FS_CD ");
            stb.append(" LEFT JOIN NAME_MST NMZ003 ON NMZ003.NAMECD1 = 'Z003' AND NMZ003.NAMECD2 = FINS.DISTRICTCD ");
            stb.append(" LEFT JOIN PREF_MST T_PREF ON T_PREF.PREF_CD = FINS.FINSCHOOL_PREF_CD ");
            stb.append(" WHERE ");
            stb.append("     BASE.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("     AND BASE.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("     AND BASE.TESTDIV = '" + param._testDiv + "' ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("   T1.PREF_CD ");
            stb.append("   , T1.PREF_NAME ");
            stb.append("   , T1.DISTRICTCD ");
            stb.append("   , T1.DISTRICTCD_NAME ");
            stb.append("   , T1.FINSCHOOL_DIV ");
            stb.append("   , T1.FINSCHOOLCD ");
            stb.append("   , T1.FINSCHOOL_NAME ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '1' AND T1.SEX = '1' THEN 1 END) AS SHIGAN_SENGAN1 ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '1' AND T1.SEX = '2' THEN 1 END) AS SHIGAN_SENGAN2 ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '1'                  THEN 1 END) AS SHIGAN_SENGAN ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '2' AND T1.SEX = '1' THEN 1 END) AS SHIGAN_HEIGAN1 ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '2' AND T1.SEX = '2' THEN 1 END) AS SHIGAN_HEIGAN2 ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '2'                  THEN 1 END) AS SHIGAN_HEIGAN ");
            stb.append("   , SUM(CASE WHEN                    T1.SEX = '1' THEN 1 END) AS SHIGAN1 ");
            stb.append("   , SUM(CASE WHEN                    T1.SEX = '2' THEN 1 END) AS SHIGAN2 ");
            stb.append("   , SUM(                                               1    ) AS SHIGAN ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '1' AND T1.JUDGEMENT = '1' AND T1.SEX = '1' THEN 1 END) AS GOKAKU_SENGAN1 ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '1' AND T1.JUDGEMENT = '1' AND T1.SEX = '2' THEN 1 END) AS GOKAKU_SENGAN2 ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '1' AND T1.JUDGEMENT = '1'                  THEN 1 END) AS GOKAKU_SENGAN ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '2' AND T1.JUDGEMENT = '1' AND T1.SEX = '1' THEN 1 END) AS GOKAKU_HEIGAN1 ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '2' AND T1.JUDGEMENT = '1' AND T1.SEX = '2' THEN 1 END) AS GOKAKU_HEIGAN2 ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '2' AND T1.JUDGEMENT = '1'                  THEN 1 END) AS GOKAKU_HEIGAN ");
            stb.append("   , SUM(CASE WHEN                    T1.JUDGEMENT = '1' AND T1.SEX = '1' THEN 1 END) AS GOKAKU1 ");
            stb.append("   , SUM(CASE WHEN                    T1.JUDGEMENT = '1' AND T1.SEX = '2' THEN 1 END) AS GOKAKU2 ");
            stb.append("   , SUM(CASE WHEN                    T1.JUDGEMENT = '1'                  THEN 1 END) AS GOKAKU ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '1' AND T1.JUDGEMENT = '1' AND T1.PROCEDUREDIV = '1' AND T1.ENTDIV = '1' AND T1.SEX = '1' THEN 1 END) AS NYUGAKU_SENGAN1 ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '1' AND T1.JUDGEMENT = '1' AND T1.PROCEDUREDIV = '1' AND T1.ENTDIV = '1' AND T1.SEX = '2' THEN 1 END) AS NYUGAKU_SENGAN2 ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '1' AND T1.JUDGEMENT = '1' AND T1.PROCEDUREDIV = '1' AND T1.ENTDIV = '1'                  THEN 1 END) AS NYUGAKU_SENGAN ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '2' AND T1.JUDGEMENT = '1' AND T1.PROCEDUREDIV = '1' AND T1.ENTDIV = '1' AND T1.SEX = '1' THEN 1 END) AS NYUGAKU_HEIGAN1 ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '2' AND T1.JUDGEMENT = '1' AND T1.PROCEDUREDIV = '1' AND T1.ENTDIV = '1' AND T1.SEX = '2' THEN 1 END) AS NYUGAKU_HEIGAN2 ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '2' AND T1.JUDGEMENT = '1' AND T1.PROCEDUREDIV = '1' AND T1.ENTDIV = '1'                  THEN 1 END) AS NYUGAKU_HEIGAN ");
            stb.append("   , SUM(CASE WHEN                    T1.JUDGEMENT = '1' AND T1.PROCEDUREDIV = '1' AND T1.ENTDIV = '1' AND T1.SEX = '1' THEN 1 END) AS NYUGAKU1 ");
            stb.append("   , SUM(CASE WHEN                    T1.JUDGEMENT = '1' AND T1.PROCEDUREDIV = '1' AND T1.ENTDIV = '1' AND T1.SEX = '2' THEN 1 END) AS NYUGAKU2 ");
            stb.append("   , SUM(CASE WHEN                    T1.JUDGEMENT = '1' AND T1.PROCEDUREDIV = '1' AND T1.ENTDIV = '1'                  THEN 1 END) AS NYUGAKU ");
            stb.append(" FROM TMP T1 ");
            stb.append(" GROUP BY ");
            stb.append("     GROUPING SETS ( ");
            stb.append("                     (T1.DISTRICTCD, T1.DISTRICTCD_NAME, T1.PREF_CD, T1.FINSCHOOL_DIV, T1.FINSCHOOLCD, T1.FINSCHOOL_NAME) ");
            stb.append("                    ,(T1.DISTRICTCD, T1.DISTRICTCD_NAME, T1.PREF_CD, T1.PREF_NAME) ");
            stb.append("                    ,(T1.PREF_CD, T1.PREF_NAME) ");
            stb.append("                    ,(T1.TOTAL_DUMMY)");
            stb.append("                 ) ");
            stb.append(" ORDER BY ");
            stb.append("     T1.PREF_CD ");
            stb.append("   , T1.DISTRICTCD ");
            stb.append("   , T1.FINSCHOOL_DIV ");
            stb.append("   , T1.FINSCHOOLCD ");
            return stb.toString();
        }
    }

    private static class Param {
        final String _entexamyear;
        final String _loginDate;
        final String _applicantdiv;
        final String _testDiv;

        final String _applicantdivname;
        final String _testdivName1;
        final  boolean _seirekiFlg;

        Param(
                final DB2UDB db2,
                final HttpServletRequest request
        ) {
            _entexamyear  = request.getParameter("YEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _loginDate    = request.getParameter("LOGIN_DATE");

            _seirekiFlg = getSeirekiFlg(db2);
            _applicantdivname = getApplicantdivName(db2);
            _testdivName1 = StringUtils.defaultString(getNameMst(db2, "NAME1", "L004", _testDiv));
        }

        private static String getNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + namecd2 + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
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
