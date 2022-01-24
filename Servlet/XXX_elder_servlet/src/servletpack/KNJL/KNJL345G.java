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

public class KNJL345G {

    private static final Log log = LogFactory.getLog(KNJL345G.class);

    private boolean _hasData;
    private Param _param;

    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        log.fatal("$Revision: 59128 $ $Date: 2018-03-14 11:31:22 +0900 (水, 14 3 2018) $"); // CVSキーワードの取り扱いに注意

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
        String course = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Tokei o = (Tokei) it.next();
            if (null == current || current.size() >= max || (null == course && null != o || null != course && !course.equals(o._course))) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
            course = o._course;
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
        final int maxLine = 50;
        final List dataListAll = Tokei.getTokeiList(db2, _param);
        final List pageList = getPageList(dataListAll, maxLine);
        final String form = "KNJL345G.frm";
        svf.VrSetForm(form, 1);

        for (int pi = 0; pi < pageList.size(); pi++) {
            svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "年度　" + StringUtils.defaultString(_param._applicantdivname) + " " + StringUtils.defaultString(_param._testdivname) + " 地域別集計表"); //
            svf.VrsOut("PAGE", String.valueOf(pi + 1)); // ページ
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate)); //

            final List dataList = (List) pageList.get(pi);
            for (int j = 0; j < dataList.size(); j++) {
                final int line = j + 1;
                final Tokei tokei = (Tokei) dataList.get(j);

                svf.VrsOut("HALL_NAME", tokei._examcourseName); // 会場名
                if (null == tokei._prefCd) {
                    // 総合計
                    svf.VrsOutn("CITY_NAME", line, "総合計"); // 市町村名
                } else if (null == tokei._finschoolDistcd) {
                    // 都道府県単位
                    svf.VrsOutn("CITY_NAME", line, StringUtils.defaultString(tokei._prefName) + "計"); // 市町村名
                } else {
                    // 学校単位
                    svf.VrsOutn("CITY_NAME", line, StringUtils.defaultString(tokei._finschoolDistcdName)); // 市町村名
                }

                svf.VrsOutn("HOPE1_1", line, getVal(tokei._shutuganSengan1)); // 出願者
                svf.VrsOutn("HOPE1_2", line, getVal(tokei._shutuganSengan2)); // 出願者
                svf.VrsOutn("HOPE1_3", line, getVal(tokei._shutuganSengan)); // 出願者
                svf.VrsOutn("HOPE2_1", line, getVal(tokei._shutuganHeigan1)); // 出願者
                svf.VrsOutn("HOPE2_2", line, getVal(tokei._shutuganHeigan2)); // 出願者
                svf.VrsOutn("HOPE2_3", line, getVal(tokei._shutuganHeigan)); // 出願者
                svf.VrsOutn("HOPE_TOTAL", line, getVal(tokei._shutugan)); // 出願者

                svf.VrsOutn("EXAM1_1", line, getVal(tokei._jukenSengan1)); // 受験者
                svf.VrsOutn("EXAM1_2", line, getVal(tokei._jukenSengan2)); // 受験者
                svf.VrsOutn("EXAM1_3", line, getVal(tokei._jukenSengan)); // 受験者
                svf.VrsOutn("EXAM2_1", line, getVal(tokei._jukenHeigan1)); // 受験者
                svf.VrsOutn("EXAM2_2", line, getVal(tokei._jukenHeigan2)); // 受験者
                svf.VrsOutn("EXAM2_3", line, getVal(tokei._jukenHeigan)); // 受験者
                svf.VrsOutn("EXAM_TOTAL", line, getVal(tokei._juken)); // 受験者

                svf.VrsOutn("PASS1_1", line, getVal(tokei._gokakuSengan1)); // 合格者
                svf.VrsOutn("PASS1_2", line, getVal(tokei._gokakuSengan2)); // 合格者
                svf.VrsOutn("PASS1_3", line, getVal(tokei._gokakuSengan)); // 合格者
                svf.VrsOutn("PASS2_1", line, getVal(tokei._gokakuHeigan1)); // 合格者
                svf.VrsOutn("PASS2_2", line, getVal(tokei._gokakuHeigan2)); // 合格者
                svf.VrsOutn("PASS2_3", line, getVal(tokei._gokakuHeigan)); // 合格者
                svf.VrsOutn("PASS_TOTAL", line, getVal(tokei._gokaku)); // 合格者

                svf.VrsOutn("FAILURE1_1", line, getVal(tokei._fugokakuSengan1)); // 不合格者
                svf.VrsOutn("FAILURE1_2", line, getVal(tokei._fugokakuSengan2)); // 不合格者
                svf.VrsOutn("FAILURE1_3", line, getVal(tokei._fugokakuSengan)); // 不合格者
                svf.VrsOutn("FAILURE2_1", line, getVal(tokei._fugokakuHeigan1)); // 不合格者
                svf.VrsOutn("FAILURE2_2", line, getVal(tokei._fugokakuHeigan2)); // 不合格者
                svf.VrsOutn("FAILURE2_3", line, getVal(tokei._fugokakuHeigan)); // 不合格者
                svf.VrsOutn("FAILURE_TOTAL", line, getVal(tokei._fugokaku)); // 不合格者

                svf.VrsOutn("PROCEDURE1_1", line, getVal(tokei._tetsuzukiSengan1)); // 手続者
                svf.VrsOutn("PROCEDURE1_2", line, getVal(tokei._tetsuzukiSengan2)); // 手続者
                svf.VrsOutn("PROCEDURE1_3", line, getVal(tokei._tetsuzukiSengan)); // 手続者
                svf.VrsOutn("PROCEDURE2_1", line, getVal(tokei._tetsuzukiHeigan1)); // 手続者
                svf.VrsOutn("PROCEDURE2_2", line, getVal(tokei._tetsuzukiHeigan2)); // 手続者
                svf.VrsOutn("PROCEDURE2_3", line, getVal(tokei._tetsuzukiHeigan)); // 手続者
                svf.VrsOutn("PROCEDURE_TOTAL", line, getVal(tokei._tetsuzuki)); // 手続者

                svf.VrsOutn("ENT1_1", line, getVal(tokei._nyugakuSengan1)); // 入学者
                svf.VrsOutn("ENT1_2", line, getVal(tokei._nyugakuSengan2)); // 入学者
                svf.VrsOutn("ENT1_3", line, getVal(tokei._nyugakuSengan)); // 入学者
                svf.VrsOutn("ENT2_1", line, getVal(tokei._nyugakuHeigan1)); // 入学者
                svf.VrsOutn("ENT2_2", line, getVal(tokei._nyugakuHeigan2)); // 入学者
                svf.VrsOutn("ENT2_3", line, getVal(tokei._nyugakuHeigan)); // 入学者
                svf.VrsOutn("ENT_TOTAL", line, getVal(tokei._nyugaku)); // 入学者
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
        String _course;
        String _examcourseName;
        String _prefCd;
        String _prefName;
        String _finschoolDistcd;
        String _finschoolDistcdName;
        String _shutuganSengan1;
        String _shutuganSengan2;
        String _shutuganSengan;
        String _shutuganHeigan1;
        String _shutuganHeigan2;
        String _shutuganHeigan;
        String _shutugan1;
        String _shutugan2;
        String _shutugan;
        String _jukenSengan1;
        String _jukenSengan2;
        String _jukenSengan;
        String _jukenHeigan1;
        String _jukenHeigan2;
        String _jukenHeigan;
        String _juken1;
        String _juken2;
        String _juken;
        String _gokakuSengan1;
        String _gokakuSengan2;
        String _gokakuSengan;
        String _gokakuHeigan1;
        String _gokakuHeigan2;
        String _gokakuHeigan;
        String _gokaku1;
        String _gokaku2;
        String _gokaku;
        String _fugokakuSengan1;
        String _fugokakuSengan2;
        String _fugokakuSengan;
        String _fugokakuHeigan1;
        String _fugokakuHeigan2;
        String _fugokakuHeigan;
        String _fugokaku1;
        String _fugokaku2;
        String _fugokaku;
        String _tetsuzukiSengan1;
        String _tetsuzukiSengan2;
        String _tetsuzukiSengan;
        String _tetsuzukiHeigan1;
        String _tetsuzukiHeigan2;
        String _tetsuzukiHeigan;
        String _tetsuzuki1;
        String _tetsuzuki2;
        String _tetsuzuki;
        String _nyugakuSengan1;
        String _nyugakuSengan2;
        String _nyugakuSengan;
        String _nyugakuHeigan1;
        String _nyugakuHeigan2;
        String _nyugakuHeigan;
        String _nyugaku1;
        String _nyugaku2;
        String _nyugaku;

        private static List getTokeiList(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();

            int getcntmax = 0;
            int getstrtcnt = 0;
            if ("1".equals(param._printtype)) {
            	getcntmax = 2;
            	getstrtcnt = 1;
            } else if ("3".equals(param._printtype)) {
            	getcntmax = 3;
            	getstrtcnt = 1;
            } else {
            	getcntmax = 3;
            	getstrtcnt = 2;
            }
            for (int cnt = getstrtcnt; cnt < getcntmax; cnt++) {
            	getTokeiList(db2, param, list, String.valueOf(cnt));
            }
            return list;
        }

        private static void getTokeiList(final DB2UDB db2, final Param param, final List list, final String gettype) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = sql(param, gettype);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Tokei tokei = new Tokei();
                    tokei._course = "1".equals(gettype) ? rs.getString("COURSE") : "";
                    tokei._examcourseName = "1".equals(gettype) ? rs.getString("EXAMCOURSE_NAME") : "";
                    tokei._prefCd = rs.getString("PREF_CD");
                    tokei._prefName = rs.getString("PREF_NAME");
                    tokei._finschoolDistcd = rs.getString("DISTRICTCD");
                    tokei._finschoolDistcdName = rs.getString("DISTRICTCD_NAME");
                    tokei._shutuganSengan1 = rs.getString("SHUTUGAN_SENGAN1");
                    tokei._shutuganSengan2 = rs.getString("SHUTUGAN_SENGAN2");
                    tokei._shutuganSengan = rs.getString("SHUTUGAN_SENGAN");
                    tokei._shutuganHeigan1 = rs.getString("SHUTUGAN_HEIGAN1");
                    tokei._shutuganHeigan2 = rs.getString("SHUTUGAN_HEIGAN2");
                    tokei._shutuganHeigan = rs.getString("SHUTUGAN_HEIGAN");
                    tokei._shutugan1 = rs.getString("SHUTUGAN1");
                    tokei._shutugan2 = rs.getString("SHUTUGAN2");
                    tokei._shutugan = rs.getString("SHUTUGAN");
                    tokei._jukenSengan1 = rs.getString("JUKEN_SENGAN1");
                    tokei._jukenSengan2 = rs.getString("JUKEN_SENGAN2");
                    tokei._jukenSengan = rs.getString("JUKEN_SENGAN");
                    tokei._jukenHeigan1 = rs.getString("JUKEN_HEIGAN1");
                    tokei._jukenHeigan2 = rs.getString("JUKEN_HEIGAN2");
                    tokei._jukenHeigan = rs.getString("JUKEN_HEIGAN");
                    tokei._juken1 = rs.getString("JUKEN1");
                    tokei._juken2 = rs.getString("JUKEN2");
                    tokei._juken = rs.getString("JUKEN");
                    tokei._gokakuSengan1 = rs.getString("GOKAKU_SENGAN1");
                    tokei._gokakuSengan2 = rs.getString("GOKAKU_SENGAN2");
                    tokei._gokakuSengan = rs.getString("GOKAKU_SENGAN");
                    tokei._gokakuHeigan1 = rs.getString("GOKAKU_HEIGAN1");
                    tokei._gokakuHeigan2 = rs.getString("GOKAKU_HEIGAN2");
                    tokei._gokakuHeigan = rs.getString("GOKAKU_HEIGAN");
                    tokei._gokaku1 = rs.getString("GOKAKU1");
                    tokei._gokaku2 = rs.getString("GOKAKU2");
                    tokei._gokaku = rs.getString("GOKAKU");
                    tokei._fugokakuSengan1 = rs.getString("FUGOKAKU_SENGAN1");
                    tokei._fugokakuSengan2 = rs.getString("FUGOKAKU_SENGAN2");
                    tokei._fugokakuSengan = rs.getString("FUGOKAKU_SENGAN");
                    tokei._fugokakuHeigan1 = rs.getString("FUGOKAKU_HEIGAN1");
                    tokei._fugokakuHeigan2 = rs.getString("FUGOKAKU_HEIGAN2");
                    tokei._fugokakuHeigan = rs.getString("FUGOKAKU_HEIGAN");
                    tokei._fugokaku1 = rs.getString("FUGOKAKU1");
                    tokei._fugokaku2 = rs.getString("FUGOKAKU2");
                    tokei._fugokaku = rs.getString("FUGOKAKU");
                    tokei._tetsuzukiSengan1 = rs.getString("TETSUZUKI_SENGAN1");
                    tokei._tetsuzukiSengan2 = rs.getString("TETSUZUKI_SENGAN2");
                    tokei._tetsuzukiSengan = rs.getString("TETSUZUKI_SENGAN");
                    tokei._tetsuzukiHeigan1 = rs.getString("TETSUZUKI_HEIGAN1");
                    tokei._tetsuzukiHeigan2 = rs.getString("TETSUZUKI_HEIGAN2");
                    tokei._tetsuzukiHeigan = rs.getString("TETSUZUKI_HEIGAN");
                    tokei._tetsuzuki1 = rs.getString("TETSUZUKI1");
                    tokei._tetsuzuki2 = rs.getString("TETSUZUKI2");
                    tokei._tetsuzuki = rs.getString("TETSUZUKI");
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
            return;
        }

        private static String sql(final Param param, final String summarytype) {
            final StringBuffer stb = new StringBuffer();
            final String summaryStr = "1".equals(summarytype) ? "" : "T1.EXAMCOURSE_NAME, ";

            stb.append(" WITH TMP AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.EXAMNO ");
            stb.append("   , T1.SHDIV ");
            stb.append("   , T1.SEX ");
            stb.append("   , T1.JUDGEMENT ");
            stb.append("   , T1.PROCEDUREDIV ");
            stb.append("   , T1.ENTDIV ");
            if ("1".equals(summarytype)) {
                stb.append("   , T1.DAI1_COURSECD || T1.DAI1_MAJORCD || T1.DAI1_COURSECODE AS COURSE ");
                stb.append("   , T4.EXAMCOURSE_NAME ");
            }
            stb.append("   , VALUE(T2.FINSCHOOL_PREF_CD, '') AS PREF_CD ");
            stb.append("   , VALUE(T2.DISTRICTCD, '') AS DISTRICTCD ");
            stb.append("   , NMZ003.NAME1 AS DISTRICTCD_NAME ");
            stb.append("   , T3.PREF_NAME ");
            stb.append(" FROM V_ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append(" INNER JOIN FINSCHOOL_MST T2 ON T2.FINSCHOOLCD = T1.FS_CD ");
            stb.append(" LEFT JOIN NAME_MST NMZ003 ON NMZ003.NAMECD1 = 'Z003' AND NMZ003.NAMECD2 = T2.DISTRICTCD ");
            stb.append(" LEFT JOIN PREF_MST T3 ON T3.PREF_CD =  T2.FINSCHOOL_PREF_CD  ");
            stb.append(" LEFT JOIN ENTEXAM_COURSE_MST T4 ON T4.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            stb.append("     AND T4.APPLICANTDIV = T1.APPLICANTDIV ");
            stb.append("     AND T4.TESTDIV = T1.TESTDIV ");
            stb.append("     AND T4.COURSECD = T1.DAI1_COURSECD ");
            stb.append("     AND T4.MAJORCD = T1.DAI1_MAJORCD ");
            stb.append("     AND T4.EXAMCOURSECD = T1.DAI1_COURSECODE ");
            stb.append(" WHERE ");
            stb.append("     T1.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("     AND T1.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("     AND T1.TESTDIV = '" + param._testdiv + "' ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            if ("1".equals(summarytype)) {
                stb.append("   T1.COURSE, ");
                stb.append("   T1.EXAMCOURSE_NAME, ");
            }
            stb.append("   T1.PREF_CD ");
            stb.append("   , T1.PREF_NAME ");
            stb.append("   , T1.DISTRICTCD ");
            stb.append("   , T1.DISTRICTCD_NAME ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '1' AND T1.SEX = '1' THEN 1 END) AS SHUTUGAN_SENGAN1 ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '1' AND T1.SEX = '2' THEN 1 END) AS SHUTUGAN_SENGAN2 ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '1'                  THEN 1 END) AS SHUTUGAN_SENGAN ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '2' AND T1.SEX = '1' THEN 1 END) AS SHUTUGAN_HEIGAN1 ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '2' AND T1.SEX = '2' THEN 1 END) AS SHUTUGAN_HEIGAN2 ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '2'                  THEN 1 END) AS SHUTUGAN_HEIGAN ");
            stb.append("   , SUM(CASE WHEN                    T1.SEX = '1' THEN 1 END) AS SHUTUGAN1 ");
            stb.append("   , SUM(CASE WHEN                    T1.SEX = '2' THEN 1 END) AS SHUTUGAN2 ");
            stb.append("   , SUM(                                               1    ) AS SHUTUGAN ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '1' AND VALUE(T1.JUDGEMENT, '') <> '4' AND T1.SEX = '1' THEN 1 END) AS JUKEN_SENGAN1 ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '1' AND VALUE(T1.JUDGEMENT, '') <> '4' AND T1.SEX = '2' THEN 1 END) AS JUKEN_SENGAN2 ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '1' AND VALUE(T1.JUDGEMENT, '') <> '4'                  THEN 1 END) AS JUKEN_SENGAN ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '2' AND VALUE(T1.JUDGEMENT, '') <> '4' AND T1.SEX = '1' THEN 1 END) AS JUKEN_HEIGAN1 ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '2' AND VALUE(T1.JUDGEMENT, '') <> '4' AND T1.SEX = '2' THEN 1 END) AS JUKEN_HEIGAN2 ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '2' AND VALUE(T1.JUDGEMENT, '') <> '4'                  THEN 1 END) AS JUKEN_HEIGAN ");
            stb.append("   , SUM(CASE WHEN                    VALUE(T1.JUDGEMENT, '') <> '4' AND T1.SEX = '1' THEN 1 END) AS JUKEN1 ");
            stb.append("   , SUM(CASE WHEN                    VALUE(T1.JUDGEMENT, '') <> '4' AND T1.SEX = '2' THEN 1 END) AS JUKEN2 ");
            stb.append("   , SUM(CASE WHEN                    VALUE(T1.JUDGEMENT, '') <> '4'                  THEN 1 END) AS JUKEN ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '1' AND       T1.JUDGEMENT = '1'       AND T1.SEX = '1' THEN 1 END) AS GOKAKU_SENGAN1 ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '1' AND       T1.JUDGEMENT = '1'       AND T1.SEX = '2' THEN 1 END) AS GOKAKU_SENGAN2 ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '1' AND       T1.JUDGEMENT = '1'                        THEN 1 END) AS GOKAKU_SENGAN ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '2' AND       T1.JUDGEMENT = '1'       AND T1.SEX = '1' THEN 1 END) AS GOKAKU_HEIGAN1 ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '2' AND       T1.JUDGEMENT = '1'       AND T1.SEX = '2' THEN 1 END) AS GOKAKU_HEIGAN2 ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '2' AND       T1.JUDGEMENT = '1'                        THEN 1 END) AS GOKAKU_HEIGAN ");
            stb.append("   , SUM(CASE WHEN                          T1.JUDGEMENT = '1'       AND T1.SEX = '1' THEN 1 END) AS GOKAKU1 ");
            stb.append("   , SUM(CASE WHEN                          T1.JUDGEMENT = '1'       AND T1.SEX = '2' THEN 1 END) AS GOKAKU2 ");
            stb.append("   , SUM(CASE WHEN                          T1.JUDGEMENT = '1'                        THEN 1 END) AS GOKAKU ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '1' AND       T1.JUDGEMENT = '2'       AND T1.SEX = '1' THEN 1 END) AS FUGOKAKU_SENGAN1 ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '1' AND       T1.JUDGEMENT = '2'       AND T1.SEX = '2' THEN 1 END) AS FUGOKAKU_SENGAN2 ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '1' AND       T1.JUDGEMENT = '2'                        THEN 1 END) AS FUGOKAKU_SENGAN ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '2' AND       T1.JUDGEMENT = '2'       AND T1.SEX = '1' THEN 1 END) AS FUGOKAKU_HEIGAN1 ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '2' AND       T1.JUDGEMENT = '2'       AND T1.SEX = '2' THEN 1 END) AS FUGOKAKU_HEIGAN2 ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '2' AND       T1.JUDGEMENT = '2'                        THEN 1 END) AS FUGOKAKU_HEIGAN ");
            stb.append("   , SUM(CASE WHEN                          T1.JUDGEMENT = '2'       AND T1.SEX = '1' THEN 1 END) AS FUGOKAKU1 ");
            stb.append("   , SUM(CASE WHEN                          T1.JUDGEMENT = '2'       AND T1.SEX = '2' THEN 1 END) AS FUGOKAKU2 ");
            stb.append("   , SUM(CASE WHEN                          T1.JUDGEMENT = '2'                        THEN 1 END) AS FUGOKAKU ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '1' AND       T1.JUDGEMENT = '1' AND T1.PROCEDUREDIV = '1' AND T1.SEX = '1' THEN 1 END) AS TETSUZUKI_SENGAN1 ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '1' AND       T1.JUDGEMENT = '1' AND T1.PROCEDUREDIV = '1' AND T1.SEX = '2' THEN 1 END) AS TETSUZUKI_SENGAN2 ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '1' AND       T1.JUDGEMENT = '1' AND T1.PROCEDUREDIV = '1'                  THEN 1 END) AS TETSUZUKI_SENGAN ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '2' AND       T1.JUDGEMENT = '1' AND T1.PROCEDUREDIV = '1' AND T1.SEX = '1' THEN 1 END) AS TETSUZUKI_HEIGAN1 ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '2' AND       T1.JUDGEMENT = '1' AND T1.PROCEDUREDIV = '1' AND T1.SEX = '2' THEN 1 END) AS TETSUZUKI_HEIGAN2 ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '2' AND       T1.JUDGEMENT = '1' AND T1.PROCEDUREDIV = '1'                  THEN 1 END) AS TETSUZUKI_HEIGAN ");
            stb.append("   , SUM(CASE WHEN                          T1.JUDGEMENT = '1' AND T1.PROCEDUREDIV = '1' AND T1.SEX = '1' THEN 1 END) AS TETSUZUKI1 ");
            stb.append("   , SUM(CASE WHEN                          T1.JUDGEMENT = '1' AND T1.PROCEDUREDIV = '1' AND T1.SEX = '2' THEN 1 END) AS TETSUZUKI2 ");
            stb.append("   , SUM(CASE WHEN                          T1.JUDGEMENT = '1' AND T1.PROCEDUREDIV = '1'                  THEN 1 END) AS TETSUZUKI ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '1' AND       T1.JUDGEMENT = '1' AND T1.PROCEDUREDIV = '1' AND T1.ENTDIV = '1' AND T1.SEX = '1' THEN 1 END) AS NYUGAKU_SENGAN1 ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '1' AND       T1.JUDGEMENT = '1' AND T1.PROCEDUREDIV = '1' AND T1.ENTDIV = '1' AND T1.SEX = '2' THEN 1 END) AS NYUGAKU_SENGAN2 ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '1' AND       T1.JUDGEMENT = '1' AND T1.PROCEDUREDIV = '1' AND T1.ENTDIV = '1'                  THEN 1 END) AS NYUGAKU_SENGAN ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '2' AND       T1.JUDGEMENT = '1' AND T1.PROCEDUREDIV = '1' AND T1.ENTDIV = '1' AND T1.SEX = '1' THEN 1 END) AS NYUGAKU_HEIGAN1 ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '2' AND       T1.JUDGEMENT = '1' AND T1.PROCEDUREDIV = '1' AND T1.ENTDIV = '1' AND T1.SEX = '2' THEN 1 END) AS NYUGAKU_HEIGAN2 ");
            stb.append("   , SUM(CASE WHEN T1.SHDIV = '2' AND       T1.JUDGEMENT = '1' AND T1.PROCEDUREDIV = '1' AND T1.ENTDIV = '1'                  THEN 1 END) AS NYUGAKU_HEIGAN ");
            stb.append("   , SUM(CASE WHEN                          T1.JUDGEMENT = '1' AND T1.PROCEDUREDIV = '1' AND T1.ENTDIV = '1' AND T1.SEX = '1' THEN 1 END) AS NYUGAKU1 ");
            stb.append("   , SUM(CASE WHEN                          T1.JUDGEMENT = '1' AND T1.PROCEDUREDIV = '1' AND T1.ENTDIV = '1' AND T1.SEX = '2' THEN 1 END) AS NYUGAKU2 ");
            stb.append("   , SUM(CASE WHEN                          T1.JUDGEMENT = '1' AND T1.PROCEDUREDIV = '1' AND T1.ENTDIV = '1'                  THEN 1 END) AS NYUGAKU ");
            stb.append(" FROM TMP T1 ");
            stb.append(" GROUP BY ");
            if ("1".equals(summarytype)) {
                stb.append("     GROUPING SETS((T1.COURSE, T1.EXAMCOURSE_NAME, T1.PREF_CD, T1.PREF_NAME, T1.DISTRICTCD, T1.DISTRICTCD_NAME) ");
                stb.append("                  ,(T1.COURSE, T1.EXAMCOURSE_NAME, T1.PREF_CD, T1.PREF_NAME) ");
                stb.append("                  ,(T1.COURSE, T1.EXAMCOURSE_NAME) ");
            } else {
                stb.append("     GROUPING SETS((T1.PREF_CD, T1.PREF_NAME, T1.DISTRICTCD, T1.DISTRICTCD_NAME) ");
                stb.append("                  ,(T1.PREF_CD, T1.PREF_NAME) ");
            }
            stb.append("                  ) ");
            stb.append(" ORDER BY ");
            if ("1".equals(summarytype)) {
                stb.append("   T1.COURSE, ");
            } else {
            }
            stb.append("   T1.PREF_CD, ");
            stb.append("   T1.DISTRICTCD ");
            return stb.toString();
        }
    }

    private static class Param {
        final String _entexamyear;
        final String _loginDate;
        final String _applicantdiv;
        final String _testdiv;

        final String _applicantdivname;
        final String _testdivname;
        final  boolean _seirekiFlg;
        final String _printtype;

        Param(
                final DB2UDB db2,
                final HttpServletRequest request
        ) {
            _entexamyear  = request.getParameter("YEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv      = request.getParameter("TESTDIV");
            _loginDate    = request.getParameter("LOGIN_DATE");

            _seirekiFlg = getSeirekiFlg(db2);
            _applicantdivname = getApplicantdivName(db2);
            _testdivname = getTestdivName(db2);

            //1:個別出力のみ(入試区分別コース別)、2:合計のみ(入試区分別)、3:個別、合計(出力は個別で1ページ毎)
            _printtype = request.getParameter("PRINT_TYPE") == null ? "2" : request.getParameter("PRINT_TYPE");
        }

        private String getApplicantdivName(DB2UDB db2) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'L003' AND NAMECD2 = '" + _applicantdiv + "'");
                rs = ps.executeQuery();
                if (rs.next() && null != rs.getString("NAME1")) {
                  rtn = rs.getString("NAME1");
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private String getTestdivName(DB2UDB db2) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'L004' AND NAMECD2 = '" + _testdiv + "'");
                rs = ps.executeQuery();
                if (rs.next() && null != rs.getString("NAME1")) {
                  rtn = rs.getString("NAME1");
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
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
