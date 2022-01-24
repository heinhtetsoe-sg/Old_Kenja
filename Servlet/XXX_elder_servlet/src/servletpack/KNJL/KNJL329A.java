// kanji=漢字
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: 33dd673a005492964fbd4344c72a0d0402b7a933 $
 */
public class KNJL329A {

    private static final Log log = LogFactory.getLog("KNJL329A.class");

    private boolean _hasData;

    private Param _param;

    /** 専願 */
    private static final String SENGAN = "1";
    /** 併願 */
    private static final String HEIGAN = "2";
    /** 1類 */
    private static final String RUI1 = "1";
    /** 2類 */
    private static final String RUI2 = "2";
    /** 進学 */
    private static final String SHINGAKU = "3";
    /** 内部 */
    private static final String NAIBU = "1";
    /** 外部 */
    private static final String GAIBU = "2";



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

            _hasData = print1(db2, svf);
            _hasData = print2(db2, svf);
            _hasData = print3(db2, svf);
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

    // 1:受験者数統計データ
    private boolean print1(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final Map print1Map = getPrint1Map(db2); //志願者Map

        String page = null;
        for (Iterator ite = print1Map.keySet().iterator(); ite.hasNext();) {
            final String key = (String)ite.next();
            final Toukei toukei = (Toukei)print1Map.get(key);

            if(!toukei._testdiv.equals(page)) {
                if(page != null) svf.VrEndPage();
                svf.VrSetForm("KNJL329A_1.frm", 1);
                final String testName = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT TESTDIV_NAME FROM ENTEXAM_TESTDIV_MST WHERE ENTEXAMYEAR = '" + _param._examYear + "' AND APPLICANTDIV = '" + _param._applicantDiv + "' AND TESTDIV = '" + toukei._testdiv + "' "));
                svf.VrsOut("TITLE", "受験者数統計データ"); //タイトル
                final String title = "2".equals(_param._applicantDiv) ? "高校入試" : "中学入試";
                svf.VrsOut("SUBTITLE", _param._examYear + "年度 " + title + " " + testName); //サブタイトル
                page = toukei._testdiv;
            }

            final String field;
            final int line;
            if(SENGAN.equals(toukei._shdiv)) {
                field = SENGAN;
                if(RUI1.equals(toukei._desirediv)) {
                    line = NAIBU.equals(toukei._div) ? 1 : 2;
                } else if (RUI2.equals(toukei._desirediv)) {
                    line = NAIBU.equals(toukei._div) ? 3 : 4;
                } else if (SHINGAKU.equals(toukei._desirediv)) {
                    line = NAIBU.equals(toukei._div) ? 5 : 6;
                } else {
                    line = 7;
                }
            } else {
                field = HEIGAN;
                if(RUI1.equals(toukei._desirediv)) {
                    line = 1;
                } else if (RUI2.equals(toukei._desirediv)) {
                    line = 2;
                } else if (SHINGAKU.equals(toukei._desirediv)) {
                    line = 3;
                } else {
                    line = 4;
                }
            }

            svf.VrsOutn("APPLICANT_NUM" + field, line, toukei._syutsugan); //出願数
            svf.VrsOutn("EXAM_NUM" + field, line, toukei._juken); //受験数

        }
        svf.VrEndPage();

        return true;

    }

    // 2:点数の統計データ
    private boolean print2(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final Map print2Map = getPrint2Map(db2); //科目毎の統計Map
        final Map print2TotalMap = getPrint2TotalMap(db2); //科目合計の統計Map

        String page = null;
        for (Iterator ite = print2Map.keySet().iterator(); ite.hasNext();) {
            final String key = (String)ite.next();
            final Score score = (Score)print2Map.get(key);

            if(!score._testdiv.equals(page)) {
                if(page != null) {
                    print2Total(svf, page, print2TotalMap);
                    svf.VrEndPage();
                }
                svf.VrSetForm("KNJL329A_2.frm", 1);
                final String testName = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT TESTDIV_NAME FROM ENTEXAM_TESTDIV_MST WHERE ENTEXAMYEAR = '" + _param._examYear + "' AND APPLICANTDIV = '" + _param._applicantDiv + "' AND TESTDIV = '" + score._testdiv + "' "));
                svf.VrsOut("TITLE", "点数の統計データ"); //タイトル
                final String title = "2".equals(_param._applicantDiv) ? "高校入試" : "中学入試";
                svf.VrsOut("SUBTITLE", _param._examYear + "年度 " + title + " " + testName); //サブタイトル
                for (Iterator ite2 = _param._subclassNameMap.keySet().iterator(); ite2.hasNext();) {
                    final String cd = (String)ite2.next();
                    final String subclassName = (String)_param._subclassNameMap.get(cd);
                    svf.VrsOut("CLASS_NAME" + cd, subclassName);
                }
                svf.VrsOut("CLASS_NAME6", "学科計");
                page = score._testdiv;
            }

            svf.VrsOutn("SCORE" + score._testsubclasscd, print2GetLine(score), score._highscore); //最高点
            svf.VrsOutn("SCORE" + score._testsubclasscd, print2GetLine(score) + 1, score._lowscore); //最低点
            svf.VrsOutn("SCORE" + score._testsubclasscd, print2GetLine(score) + 2, score._avg); //平均点
            svf.VrsOutn("SCORE" + score._testsubclasscd, print2GetLine(score) + 3, score._kakoavg); //平均点（昨年）
        }
        print2Total(svf, page, print2TotalMap);
        svf.VrEndPage();

        return true;
    }

    private void print2Total(final Vrw32alp svf, final String testDiv, final Map totalMap) {
        for (Iterator ite = totalMap.keySet().iterator(); ite.hasNext();) {
            final String key = (String)ite.next();
            final Score score = (Score)totalMap.get(key);

            if(!testDiv.equals(score._testdiv)) continue;
            svf.VrsOutn("SCORE6" , print2GetLine(score), score._highscore); //最高点
            svf.VrsOutn("SCORE6" , print2GetLine(score) + 1, score._lowscore); //最低点
            svf.VrsOutn("SCORE6" , print2GetLine(score) + 2, score._avg); //平均点
            svf.VrsOutn("SCORE6" , print2GetLine(score) + 3, score._kakoavg); //平均点（昨年）
        }

    }

    private int print2GetLine(final Score score) {
        final int line;
        if(GAIBU.equals(score._inout)) {
            if(SENGAN.equals(score._shdiv)) {
                if(RUI1.equals(score._desirediv)) {
                    line = 1;
                } else if(RUI2.equals(score._desirediv)) {
                    line = 5;
                } else {
                    line = 9;
                }
            } else {
                if(RUI1.equals(score._desirediv)) {
                    line = 13;
                } else if(RUI2.equals(score._desirediv)) {
                    line = 17;
                } else {
                    line = 21;
                }
            }
        } else if(NAIBU.equals(score._inout)) {
            if(SENGAN.equals(score._shdiv)) {
                if(RUI1.equals(score._desirediv)) {
                    line = 25;
                } else if(RUI2.equals(score._desirediv)) {
                    line = 29;
                } else {
                    line = 33;
                }
            } else line = 0;
        } else {
            line = 37;
        }
        return line;
    }

    // 3:他試験の結果
    private boolean print3(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final Map print3SenganMap = getPrint3SenganMap(db2); //専願Map
        final Map print3HeiganMap = getPrint3HeiganMap(db2); //併願Map
        final Map print3NgMap = getPrint3NgMap(db2); //事前併願NGMap

        for (Iterator ite = _param._testMap.keySet().iterator(); ite.hasNext();) {
            final String testdiv = (String)ite.next();
            if(!"ALL".equals(_param._testDiv)) {
                if(!testdiv.equals(_param._testDiv)) continue;
            }
            svf.VrSetForm("KNJL329A_3.frm", 1);
            svf.VrsOut("TITLE", "他試験の結果"); //タイトル
            final String title = "2".equals(_param._applicantDiv) ? "高校入試" : "中学入試";
            svf.VrsOut("SUBTITLE", _param._examYear + "年度 " + title + " " + (String)_param._testMap.get(testdiv)); //サブタイトル

            int line = 1;
            //専願
            for(int course = 1; course <= 3; course++) {
                for(int inout = 1; inout <= 2; inout++) {
                    final String key = testdiv + SENGAN + course + inout;
                    final Toukei toukei = (Toukei)print3SenganMap.get(key);
                    if(toukei == null) {
                        line++;
                        continue;
                    }

                    svf.VrsOutn("APPLICANT_NUM1",line, toukei._syutsugan); //出願数
                    svf.VrsOutn("EXAM_NUM1", line, toukei._juken); //受験数
                    svf.VrsOutn("PASS1_1", line, toukei._goukaku1); //I類合格
                    svf.VrsOutn("PASS1_2", line, toukei._goukaku2); //Ⅱ類合格
                    svf.VrsOutn("PASS1_3", line, toukei._shingaku); //進学合格
                    svf.VrsOutn("PASS_TOTAL1", line, toukei._total); //合格合計
                    svf.VrsOutn("FAIL_TOTAL1", line, toukei._ng); //不合格者計

                    line++;
                }
            }
            final String senganTotalKey = testdiv + SENGAN + "99" + "99";
            final Toukei toukei1 = (Toukei)print3SenganMap.get(senganTotalKey);
            if(toukei1 != null) {
                svf.VrsOutn("APPLICANT_NUM1",7, toukei1._syutsugan); //出願数
                svf.VrsOutn("EXAM_NUM1", 7, toukei1._juken); //受験数
                svf.VrsOutn("PASS1_1", 7, toukei1._goukaku1); //I類合格
                svf.VrsOutn("PASS1_2", 7, toukei1._goukaku2); //Ⅱ類合格
                svf.VrsOutn("PASS1_3", 7, toukei1._shingaku); //進学合格
                svf.VrsOutn("PASS_TOTAL1", 7, toukei1._total); //合格合計
                svf.VrsOutn("FAIL_TOTAL1", 7, toukei1._ng); //不合格者計
            }

            //併願
            line = 1;
            for(int course = 1; course <= 3; course++) {
                for(int jizen = 0; jizen <= 2; jizen++) {

                    //併願不合格、内事前専願合格者
                    if(jizen == 2) {
                        final String ng = (String)print3NgMap.get(testdiv + course);
                        svf.VrsOutn("FAIL_TOTAL2", line, ng); //不合格者計
                    }

                    final String key = testdiv + HEIGAN + course + jizen;
                    final Toukei toukei = (Toukei)print3HeiganMap.get(key);
                    if(toukei == null) {
                        line++;
                        continue;
                    }

                    //併願合格者数
                    if(jizen == 0) {
                        svf.VrsOutn("APPLICANT_NUM2",line, toukei._syutsugan); //出願数
                        svf.VrsOutn("EXAM_NUM2", line, toukei._juken); //受験数
                        svf.VrsOutn("PASS2_1", line, toukei._goukaku1); //I類合格
                        svf.VrsOutn("PASS2_2", line, toukei._goukaku2); //Ⅱ類合格
                        svf.VrsOutn("PASS2_3", line, toukei._shingaku); //進学合格
                        svf.VrsOutn("PASS_TOTAL2", line, toukei._total); //合格合計
                        svf.VrsOutn("FAIL_TOTAL2", line, toukei._ng); //不合格者計
                    }
                    //内事前専願１合格者数
                    else if(jizen == 1) {
                        svf.VrsOutn("PASS2_2", line, toukei._goukaku2); //Ⅱ類合格
                        svf.VrsOutn("PASS2_3", line, toukei._shingaku); //進学合格
                    }
                    //内事前専願２合格者数
                    else if(jizen == 2) {
                        svf.VrsOutn("PASS2_3", line, toukei._shingaku); //進学合格
                    }
                    line++;
                }
            }

            //合計
            final String heiganTotalKey = testdiv + HEIGAN + "ZZ" + "ZZ";
            final Toukei toukei2 = (Toukei)print3HeiganMap.get(heiganTotalKey);
            if(toukei2 != null) {
                svf.VrsOutn("APPLICANT_NUM2",10, toukei2._syutsugan); //出願数
                svf.VrsOutn("EXAM_NUM2", 10, toukei2._juken); //受験数
                svf.VrsOutn("PASS2_1", 10, toukei2._goukaku1); //I類合格
                svf.VrsOutn("PASS2_2", 10, toukei2._goukaku2); //Ⅱ類合格
                svf.VrsOutn("PASS2_3", 10, toukei2._shingaku); //進学合格
                svf.VrsOutn("PASS_TOTAL2", 10, toukei2._total); //合格合計
            }

            svf.VrEndPage();
        }

        return true;

    }

    private String getFieldSchoolName(final String str) {
        final int keta = KNJ_EditEdit.getMS932ByteLength(str);
        return keta <= 30 ? "1" : "2";
    }

    private String getFieldName(final String str) {
        final int keta = KNJ_EditEdit.getMS932ByteLength(str);
        return keta <= 20 ? "1" : keta <= 30 ? "2" : "3";
    }

    private Map getPrint1Map(final DB2UDB db2) throws SQLException {
        Map retMap = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try{
            final StringBuffer stb = new StringBuffer();
            //ベース表
            stb.append(" WITH BASE AS ( ");
            stb.append(" SELECT ");
            stb.append("     R1.TESTDIV, ");
            stb.append("     R1.RECEPTNO, ");
            stb.append("     R2.REMARK1 AS SHDIV, ");
            stb.append("     R2.REMARK2 AS DESIREDIV, ");
            if("2".equals(_param._applicantDiv)) {
                stb.append("     CASE WHEN BASE.FS_CD ='" + _param._jSchoolCd + "' THEN '" + NAIBU  + "' ELSE '" + GAIBU  + "' END AS INOUT, ");
            } else {
                stb.append("     '" + GAIBU + "' AS INOUT, "); //中学生は外部のみ
            }
            stb.append("     BASE.FS_CD, ");
            stb.append("     M1.TESTDIV_NAME AS TEST_NAME, ");
            stb.append("     M1.TEST_DATE1 AS TEST_DATE, ");
            stb.append("     N1.NAME1 AS SHDIV_NAME, ");
            stb.append("     N2.NAME1 AS DESIREDIV_NAME, ");
            stb.append("     R1.JUDGEDIV, ");
            stb.append("     R2.REMARK8, ");
            stb.append("     R2.REMARK9 ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_RECEPT_DAT R1 ");
            stb.append("     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT R2 ");
            stb.append("          ON R2.ENTEXAMYEAR  = R1.ENTEXAMYEAR ");
            stb.append("         AND R2.APPLICANTDIV = R1.APPLICANTDIV ");
            stb.append("         AND R2.TESTDIV      = R1.TESTDIV ");
            stb.append("         AND R2.EXAM_TYPE    = R1.EXAM_TYPE ");
            stb.append("         AND R2.RECEPTNO     = R1.RECEPTNO ");
            stb.append("         AND R2.SEQ          = '006' ");
            stb.append("     LEFT JOIN ENTEXAM_TESTDIV_MST M1 ");
            stb.append("          ON M1.ENTEXAMYEAR  = R1.ENTEXAMYEAR ");
            stb.append("         AND M1.APPLICANTDIV = R1.APPLICANTDIV ");
            stb.append("         AND M1.TESTDIV      = R1.TESTDIV ");
            stb.append("     LEFT JOIN V_NAME_MST N1 ");
            stb.append("          ON N1.YEAR  = R1.ENTEXAMYEAR ");
            stb.append("         AND N1.NAMECD1 = 'L006' ");
            stb.append("         AND N1.NAMECD2 = R2.REMARK1 ");
            stb.append("     LEFT JOIN V_NAME_MST N2 ");
            stb.append("          ON N2.YEAR  = R1.ENTEXAMYEAR ");
            final String namecd = "2".equals(_param._applicantDiv) ? "LH58" : "LJ58";
            stb.append("         AND N2.NAMECD1 = '" + namecd + "' ");
            stb.append("         AND N2.NAMECD2 = R2.REMARK2 ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT BASE ");
            stb.append("          ON BASE.ENTEXAMYEAR = R1.ENTEXAMYEAR ");
            stb.append("          AND BASE.APPLICANTDIV = R1.APPLICANTDIV ");
            stb.append("          AND BASE.TESTDIV = R1.TESTDIV ");
            stb.append("          AND BASE.EXAMNO = R1.EXAMNO ");
            stb.append(" WHERE ");
            stb.append("         R1.ENTEXAMYEAR  = '" + _param._examYear + "' ");
            stb.append("     AND R1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
            stb.append("     AND R1.EXAM_TYPE    = '1' ");
            if(!"ALL".equals(_param._testDiv)) {
                stb.append("     AND R1.TESTDIV    = '" + _param._testDiv + "' ");
            }
            stb.append(" ORDER BY ");
            stb.append("     R1.TESTDIV, ");
            stb.append("     R1.RECEPTNO ");
            stb.append(" ) ");
            //メイン表
            stb.append(" SELECT  "); //専願の内訳
            stb.append("   TESTDIV, ");
            stb.append("   SHDIV, ");
            stb.append("   DESIREDIV, ");
            stb.append("   INOUT, ");
            stb.append("   COUNT(RECEPTNO) AS SYUTSUGAN, ");
            stb.append("   COUNT(CASE WHEN JUDGEDIV <> '4' OR JUDGEDIV IS NULL THEN RECEPTNO END) AS JUKEN ");
            stb.append(" FROM ");
            stb.append("   BASE ");
            stb.append(" WHERE ");
            stb.append("   SHDIV = '" + SENGAN + "' ");
            stb.append(" GROUP BY ");
            stb.append("   TESTDIV, ");
            stb.append("   SHDIV, ");
            stb.append("   DESIREDIV, ");
            stb.append("   INOUT ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT  "); //専願の合計
            stb.append("   TESTDIV, ");
            stb.append("   SHDIV, ");
            stb.append("   '99' AS DESIREDIV, ");
            stb.append("   '99' AS INOUT, ");
            stb.append("   COUNT(RECEPTNO) AS SYUTSUGAN, ");
            stb.append("   COUNT(CASE WHEN JUDGEDIV <> '4' OR JUDGEDIV IS NULL THEN RECEPTNO END) AS JUKEN ");
            stb.append(" FROM ");
            stb.append("   BASE ");
            stb.append(" WHERE ");
            stb.append("   SHDIV = '" + SENGAN + "' ");
            stb.append(" GROUP BY ");
            stb.append("   TESTDIV, ");
            stb.append("   SHDIV ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT  "); //併願の内訳
            stb.append("   TESTDIV, ");
            stb.append("   SHDIV, ");
            stb.append("   DESIREDIV, ");
            stb.append("   'ZZZ' AS INOUT, ");
            stb.append("   COUNT(RECEPTNO) AS SYUTSUGAN, ");
            stb.append("   COUNT(CASE WHEN JUDGEDIV <> '4' OR JUDGEDIV IS NULL THEN RECEPTNO END) AS JUKEN ");
            stb.append(" FROM ");
            stb.append("   BASE ");
            stb.append(" WHERE ");
            stb.append("   SHDIV = '" + HEIGAN + "' ");
            stb.append(" GROUP BY ");
            stb.append("   TESTDIV, ");
            stb.append("   SHDIV, ");
            stb.append("   DESIREDIV ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT  "); //併願の合計
            stb.append("   TESTDIV, ");
            stb.append("   SHDIV, ");
            stb.append("   '99' AS DESIREDIV, ");
            stb.append("   '99' AS INOUT, ");
            stb.append("   COUNT(RECEPTNO) AS SYUTSUGAN, ");
            stb.append("   COUNT(CASE WHEN JUDGEDIV <> '4' OR JUDGEDIV IS NULL THEN RECEPTNO END) AS JUKEN ");
            stb.append(" FROM ");
            stb.append("   BASE ");
            stb.append(" WHERE ");
            stb.append("   SHDIV = '" + HEIGAN + "' ");
            stb.append(" GROUP BY ");
            stb.append("   TESTDIV, ");
            stb.append("   SHDIV ");
            stb.append(" ORDER BY ");
            stb.append("   TESTDIV, ");
            stb.append("   SHDIV, ");
            stb.append("   DESIREDIV, ");
            stb.append("   INOUT ");

            log.debug(" print1 sql =" + stb.toString());

            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();

            while (rs.next()) {
                final String testdiv = rs.getString("TESTDIV");
                final String shdiv = rs.getString("SHDIV");
                final String desirediv = rs.getString("DESIREDIV");
                final String inout = rs.getString("INOUT");
                final String syutsugan = rs.getString("SYUTSUGAN");
                final String juken = rs.getString("JUKEN");

                final String key = testdiv + shdiv + desirediv + inout;
                if(!retMap.containsKey(key)) {
                    final Toukei toukei = new Toukei(testdiv, shdiv, desirediv, inout, syutsugan, juken, null, null, null, null, null);
                    retMap.put(key, toukei);
                }

            }

        } catch (final SQLException e) {
            log.error("志願者の基本情報取得でエラー", e);
            throw e;
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retMap;

    }

    private Map getPrint2Map(final DB2UDB db2) throws SQLException {
        Map retMap = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;

        final String lastYear = String.valueOf(Integer.parseInt(_param._examYear) - 1);

        try {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   T1.ENTEXAMYEAR, ");
            stb.append("   T1.APPLICANTDIV, ");
            stb.append("   T1.TESTDIV, ");
            stb.append("   T1.INOUT, ");
            stb.append("   T1.SHDIV, ");
            stb.append("   T1.DESIREDIV, ");
            stb.append("   T1.TESTSUBCLASSCD, ");
            stb.append("   T1.HIGHSCORE, ");
            stb.append("   T1.LOWSCORE, ");
            stb.append("   CAST(ROUND(T1.AVG, 0) AS INT) AS AVG, ");
            stb.append("   (SELECT ");
            stb.append("     CAST(ROUND(AVG, 0) AS INT) AS AVG ");
            stb.append("    FROM ");
            stb.append("     ENTEXAM_TOKEI_INOUT_HIGH_LOW_HISTORY_DAT T2 ");
            stb.append("    WHERE ");
            stb.append("     T2.ENTEXAMYEAR = '" + lastYear + "' ");
            stb.append("     AND T2.APPLICANTDIV = T1.APPLICANTDIV ");
            stb.append("     AND T2.TESTDIV = T1.TESTDIV ");
            stb.append("     AND T2.INOUT = T1.INOUT ");
            stb.append("     AND T2.SHDIV = T1.SHDIV ");
            stb.append("     AND T2.DESIREDIV = T1.DESIREDIV ");
            stb.append("     AND T2.TESTSUBCLASSCD = T1.TESTSUBCLASSCD) AS KAKOAVG ");
            stb.append(" FROM ");
            stb.append("   ENTEXAM_TOKEI_INOUT_HIGH_LOW_HISTORY_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("   ENTEXAMYEAR = '" + _param._examYear + "' ");
            stb.append("   AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
            if(!"ALL".equals(_param._testDiv)) {
                stb.append("   AND T1.TESTDIV = '" + _param._testDiv + "' ");
            }
            stb.append("   AND T1.TESTSUBCLASSCD <> '9' ");
            stb.append(" ORDER BY ");
            stb.append("   T1.TESTDIV, ");
            stb.append("   T1.INOUT, ");
            stb.append("   T1.SHDIV, ");
            stb.append("   T1.DESIREDIV, ");
            stb.append("   T1.TESTSUBCLASSCD ");

            log.debug(" print2 sql =" + stb.toString());

            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();

            while (rs.next()) {
                final String testdiv = rs.getString("TESTDIV");
                final String inout = rs.getString("INOUT");
                final String shdiv = rs.getString("SHDIV");
                final String desirediv = rs.getString("DESIREDIV");
                final String testsubclasscd = rs.getString("TESTSUBCLASSCD");
                final String highscore = rs.getString("HIGHSCORE");
                final String lowscore = rs.getString("LOWSCORE");
                final String avg = rs.getString("AVG");
                final String kakoavg = rs.getString("KAKOAVG");


                final String key = testdiv + inout + shdiv + desirediv + testsubclasscd;
                if(!retMap.containsKey(key)) {
                    final Score score = new Score(testdiv, inout, shdiv, desirediv, testsubclasscd, highscore, lowscore, avg, kakoavg);
                    retMap.put(key, score);
                }

            }


        } catch (final SQLException e) {
            log.error("志願者の基本情報取得でエラー", e);
            throw e;
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        return retMap;
    }

    private Map getPrint2TotalMap(final DB2UDB db2) throws SQLException {
        Map retMap = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;

        final String lastYear = String.valueOf(Integer.parseInt(_param._examYear) - 1);
        final String namecd = "2".equals(_param._applicantDiv) ? "LH58" : "LJ58";

        try {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH BASE1 AS ( ");
            stb.append(" SELECT ");
            stb.append("     R1.TESTDIV, ");
            stb.append("     R1.RECEPTNO, ");
            if("2".equals(_param._applicantDiv)) {
                stb.append("     CASE WHEN BASE.FS_CD ='" + _param._jSchoolCd + "' THEN '" + NAIBU + "' ELSE '" + GAIBU + "' END AS INOUT, ");
            } else {
                stb.append("     '" + GAIBU + "' AS INOUT, "); //中学生は外部のみ
            }
            stb.append("     R2.REMARK1 AS SHDIV, ");
            stb.append("     R2.REMARK2 AS DESIREDIV, ");
            stb.append("     M1.TESTDIV_NAME AS TEST_NAME, ");
            stb.append("     M1.TEST_DATE1 AS TEST_DATE, ");
            stb.append("     N1.NAME1 AS SHDIV_NAME, ");
            stb.append("     N2.NAME1 AS DESIREDIV_NAME, ");
            stb.append("     SCORE1.SCORE AS SCORE1, ");
            stb.append("     SCORE2.SCORE AS SCORE2, ");
            stb.append("     SCORE3.SCORE AS SCORE3, ");
            stb.append("     SCORE4.SCORE AS SCORE4, ");
            stb.append("     SCORE5.SCORE AS SCORE5, ");
            stb.append("     VALUE(SCORE1.SCORE,0) + VALUE(SCORE2.SCORE,0) + VALUE(SCORE3.SCORE,0) + VALUE(SCORE4.SCORE,0) + VALUE(SCORE5.SCORE,0) AS TOTAL ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_RECEPT_DAT R1 ");
            stb.append("     INNER JOIN ENTEXAM_RECEPT_DETAIL_DAT R2 ");
            stb.append("          ON R2.ENTEXAMYEAR  = R1.ENTEXAMYEAR ");
            stb.append("         AND R2.APPLICANTDIV = R1.APPLICANTDIV ");
            stb.append("         AND R2.TESTDIV      = R1.TESTDIV ");
            stb.append("         AND R2.EXAM_TYPE    = R1.EXAM_TYPE ");
            stb.append("         AND R2.RECEPTNO     = R1.RECEPTNO ");
            stb.append("         AND R2.SEQ          = '006' ");
            stb.append("         AND VALUE(R2.REMARK8, '0') <> '4' ");
            stb.append("         AND VALUE(R2.REMARK9, '0') <> '4' ");
            stb.append("     LEFT JOIN ENTEXAM_TESTDIV_MST M1 ");
            stb.append("          ON M1.ENTEXAMYEAR  = R1.ENTEXAMYEAR ");
            stb.append("         AND M1.APPLICANTDIV = R1.APPLICANTDIV ");
            stb.append("         AND M1.TESTDIV      = R1.TESTDIV ");
            stb.append("     LEFT JOIN V_NAME_MST N1 ");
            stb.append("          ON N1.YEAR  = R1.ENTEXAMYEAR ");
            stb.append("         AND N1.NAMECD1 = 'L006' ");
            stb.append("         AND N1.NAMECD2 = R2.REMARK1 ");
            stb.append("     LEFT JOIN V_NAME_MST N2 ");
            stb.append("          ON N2.YEAR  = R1.ENTEXAMYEAR ");
            stb.append("         AND N2.NAMECD1 = '" + namecd + "' ");
            stb.append("         AND N2.NAMECD2 = R2.REMARK2 ");
            stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT SCORE1 ");
            stb.append("         ON SCORE1.ENTEXAMYEAR  = R1.ENTEXAMYEAR ");
            stb.append("         AND SCORE1.APPLICANTDIV = R1.APPLICANTDIV ");
            stb.append("         AND SCORE1.TESTDIV      = R1.TESTDIV ");
            stb.append("         AND SCORE1.EXAM_TYPE    = R1.EXAM_TYPE ");
            stb.append("         AND SCORE1.RECEPTNO     = R1.RECEPTNO ");
            stb.append("         AND SCORE1.TESTSUBCLASSCD = '1' ");
            stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT SCORE2 ");
            stb.append("         ON SCORE2.ENTEXAMYEAR  = R1.ENTEXAMYEAR ");
            stb.append("         AND SCORE2.APPLICANTDIV = R1.APPLICANTDIV ");
            stb.append("         AND SCORE2.TESTDIV      = R1.TESTDIV ");
            stb.append("         AND SCORE2.EXAM_TYPE    = R1.EXAM_TYPE ");
            stb.append("         AND SCORE2.RECEPTNO     = R1.RECEPTNO ");
            stb.append("         AND SCORE2.TESTSUBCLASSCD = '2' ");
            stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT SCORE3 ");
            stb.append("         ON SCORE3.ENTEXAMYEAR  = R1.ENTEXAMYEAR ");
            stb.append("         AND SCORE3.APPLICANTDIV = R1.APPLICANTDIV ");
            stb.append("         AND SCORE3.TESTDIV      = R1.TESTDIV ");
            stb.append("         AND SCORE3.EXAM_TYPE    = R1.EXAM_TYPE ");
            stb.append("         AND SCORE3.RECEPTNO     = R1.RECEPTNO ");
            stb.append("         AND SCORE3.TESTSUBCLASSCD = '3' ");
            stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT SCORE4 ");
            stb.append("         ON SCORE4.ENTEXAMYEAR  = R1.ENTEXAMYEAR ");
            stb.append("         AND SCORE4.APPLICANTDIV = R1.APPLICANTDIV ");
            stb.append("         AND SCORE4.TESTDIV      = R1.TESTDIV ");
            stb.append("         AND SCORE4.EXAM_TYPE    = R1.EXAM_TYPE ");
            stb.append("         AND SCORE4.RECEPTNO     = R1.RECEPTNO ");
            stb.append("         AND SCORE4.TESTSUBCLASSCD = '4' ");
            stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT SCORE5 ");
            stb.append("         ON SCORE5.ENTEXAMYEAR  = R1.ENTEXAMYEAR ");
            stb.append("         AND SCORE5.APPLICANTDIV = R1.APPLICANTDIV ");
            stb.append("         AND SCORE5.TESTDIV      = R1.TESTDIV ");
            stb.append("         AND SCORE5.EXAM_TYPE    = R1.EXAM_TYPE ");
            stb.append("         AND SCORE5.RECEPTNO     = R1.RECEPTNO ");
            stb.append("         AND SCORE5.TESTSUBCLASSCD = '5' ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT BASE ");
            stb.append("          ON BASE.ENTEXAMYEAR = R1.ENTEXAMYEAR ");
            stb.append("          AND BASE.APPLICANTDIV = R1.APPLICANTDIV ");
            stb.append("          AND BASE.TESTDIV = R1.TESTDIV ");
            stb.append("          AND BASE.EXAMNO = R1.EXAMNO ");
            stb.append(" WHERE ");
            stb.append("         R1.ENTEXAMYEAR  = '" + _param._examYear + "' ");
            stb.append("     AND R1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
            stb.append("     AND R1.EXAM_TYPE    = '1' ");
            if(!"ALL".equals(_param._testDiv)) {
                stb.append("     AND R1.TESTDIV = '" + _param._testDiv + "' ");
            }
            stb.append("     AND R2.REMARK1 IS NOT NULL ");
            stb.append(" ORDER BY ");
            stb.append("     R1.TESTDIV, ");
            stb.append("     INOUT, ");
            stb.append("     SHDIV, ");
            stb.append("     DESIREDIV, ");
            stb.append("     R1.RECEPTNO ");
            stb.append(" ), KEKKA1 AS ( ");
            stb.append(" SELECT  ");
            stb.append("  TESTDIV, ");
            stb.append("  INOUT, ");
            stb.append("  SHDIV, ");
            stb.append("  DESIREDIV, ");
            stb.append("  MAX(TOTAL) AS MAX, ");
            stb.append("  MIN(TOTAL) AS MIN, ");
            stb.append(" INT(AVG(TOTAL * 1.0) + 0.5) AS AVG ");
            stb.append(" FROM ");
            stb.append("  BASE1 ");
            stb.append(" GROUP BY ");
            stb.append("   TESTDIV, ");
            stb.append("  INOUT, ");
            stb.append("  SHDIV, ");
            stb.append("  DESIREDIV ");
            stb.append(" ), BASE2 AS ( ");
            stb.append(" SELECT ");
            stb.append("     R1.TESTDIV, ");
            stb.append("     R1.RECEPTNO, ");
            if("2".equals(_param._applicantDiv)) {
                stb.append("     CASE WHEN BASE.FS_CD ='" + _param._jSchoolCd + "' THEN '" + NAIBU + "' ELSE '" + GAIBU + "' END AS INOUT, ");
            } else {
                stb.append("     '" + GAIBU + "' AS INOUT, "); //中学生は外部のみ
            }
            stb.append("     R2.REMARK1 AS SHDIV, ");
            stb.append("     R2.REMARK2 AS DESIREDIV, ");
            stb.append("     M1.TESTDIV_NAME AS TEST_NAME, ");
            stb.append("     M1.TEST_DATE1 AS TEST_DATE, ");
            stb.append("     N1.NAME1 AS SHDIV_NAME, ");
            stb.append("     N2.NAME1 AS DESIREDIV_NAME, ");
            stb.append("     SCORE1.SCORE AS SCORE1, ");
            stb.append("     SCORE2.SCORE AS SCORE2, ");
            stb.append("     SCORE3.SCORE AS SCORE3, ");
            stb.append("     SCORE4.SCORE AS SCORE4, ");
            stb.append("     SCORE5.SCORE AS SCORE5, ");
            stb.append("     VALUE(SCORE1.SCORE,0) + VALUE(SCORE2.SCORE,0) + VALUE(SCORE3.SCORE,0) + VALUE(SCORE4.SCORE,0) + VALUE(SCORE5.SCORE,0) AS TOTAL ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_RECEPT_DAT R1 ");
            stb.append("     INNER JOIN ENTEXAM_RECEPT_DETAIL_DAT R2 ");
            stb.append("          ON R2.ENTEXAMYEAR  = R1.ENTEXAMYEAR ");
            stb.append("         AND R2.APPLICANTDIV = R1.APPLICANTDIV ");
            stb.append("         AND R2.TESTDIV      = R1.TESTDIV ");
            stb.append("         AND R2.EXAM_TYPE    = R1.EXAM_TYPE ");
            stb.append("         AND R2.RECEPTNO     = R1.RECEPTNO ");
            stb.append("         AND R2.SEQ          = '006' ");
            stb.append("         AND VALUE(R2.REMARK8, '0') <> '4' ");
            stb.append("         AND VALUE(R2.REMARK9, '0') <> '4' ");
            stb.append("     LEFT JOIN ENTEXAM_TESTDIV_MST M1 ");
            stb.append("          ON M1.ENTEXAMYEAR  = R1.ENTEXAMYEAR ");
            stb.append("         AND M1.APPLICANTDIV = R1.APPLICANTDIV ");
            stb.append("         AND M1.TESTDIV      = R1.TESTDIV ");
            stb.append("     LEFT JOIN V_NAME_MST N1 ");
            stb.append("          ON N1.YEAR  = R1.ENTEXAMYEAR ");
            stb.append("         AND N1.NAMECD1 = 'L006' ");
            stb.append("         AND N1.NAMECD2 = R2.REMARK1 ");
            stb.append("     LEFT JOIN V_NAME_MST N2 ");
            stb.append("          ON N2.YEAR  = R1.ENTEXAMYEAR ");
            stb.append("         AND N2.NAMECD1 = '" + namecd + "' ");
            stb.append("         AND N2.NAMECD2 = R2.REMARK2 ");
            stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT SCORE1 ");
            stb.append("         ON SCORE1.ENTEXAMYEAR  = R1.ENTEXAMYEAR ");
            stb.append("         AND SCORE1.APPLICANTDIV = R1.APPLICANTDIV ");
            stb.append("         AND SCORE1.TESTDIV      = R1.TESTDIV ");
            stb.append("         AND SCORE1.EXAM_TYPE    = R1.EXAM_TYPE ");
            stb.append("         AND SCORE1.RECEPTNO     = R1.RECEPTNO ");
            stb.append("         AND SCORE1.TESTSUBCLASSCD = '1' ");
            stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT SCORE2 ");
            stb.append("         ON SCORE2.ENTEXAMYEAR  = R1.ENTEXAMYEAR ");
            stb.append("         AND SCORE2.APPLICANTDIV = R1.APPLICANTDIV ");
            stb.append("         AND SCORE2.TESTDIV      = R1.TESTDIV ");
            stb.append("         AND SCORE2.EXAM_TYPE    = R1.EXAM_TYPE ");
            stb.append("         AND SCORE2.RECEPTNO     = R1.RECEPTNO ");
            stb.append("         AND SCORE2.TESTSUBCLASSCD = '2' ");
            stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT SCORE3 ");
            stb.append("         ON SCORE3.ENTEXAMYEAR  = R1.ENTEXAMYEAR ");
            stb.append("         AND SCORE3.APPLICANTDIV = R1.APPLICANTDIV ");
            stb.append("         AND SCORE3.TESTDIV      = R1.TESTDIV ");
            stb.append("         AND SCORE3.EXAM_TYPE    = R1.EXAM_TYPE ");
            stb.append("         AND SCORE3.RECEPTNO     = R1.RECEPTNO ");
            stb.append("         AND SCORE3.TESTSUBCLASSCD = '3' ");
            stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT SCORE4 ");
            stb.append("         ON SCORE4.ENTEXAMYEAR  = R1.ENTEXAMYEAR ");
            stb.append("         AND SCORE4.APPLICANTDIV = R1.APPLICANTDIV ");
            stb.append("         AND SCORE4.TESTDIV      = R1.TESTDIV ");
            stb.append("         AND SCORE4.EXAM_TYPE    = R1.EXAM_TYPE ");
            stb.append("         AND SCORE4.RECEPTNO     = R1.RECEPTNO ");
            stb.append("         AND SCORE4.TESTSUBCLASSCD = '4' ");
            stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT SCORE5 ");
            stb.append("         ON SCORE5.ENTEXAMYEAR  = R1.ENTEXAMYEAR ");
            stb.append("         AND SCORE5.APPLICANTDIV = R1.APPLICANTDIV ");
            stb.append("         AND SCORE5.TESTDIV      = R1.TESTDIV ");
            stb.append("         AND SCORE5.EXAM_TYPE    = R1.EXAM_TYPE ");
            stb.append("         AND SCORE5.RECEPTNO     = R1.RECEPTNO ");
            stb.append("         AND SCORE5.TESTSUBCLASSCD = '5' ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT BASE ");
            stb.append("          ON BASE.ENTEXAMYEAR = R1.ENTEXAMYEAR ");
            stb.append("          AND BASE.APPLICANTDIV = R1.APPLICANTDIV ");
            stb.append("          AND BASE.TESTDIV = R1.TESTDIV ");
            stb.append("          AND BASE.EXAMNO = R1.EXAMNO ");
            stb.append(" WHERE ");
            stb.append("         R1.ENTEXAMYEAR  = '" + lastYear + "' ");
            stb.append("     AND R1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
            stb.append("     AND R1.EXAM_TYPE    = '1' ");
            if(!"ALL".equals(_param._testDiv)) {
                stb.append("     AND R1.TESTDIV = '" + _param._testDiv + "' ");
            }
            stb.append(" ORDER BY ");
            stb.append("     R1.TESTDIV, ");
            stb.append("     INOUT, ");
            stb.append("     SHDIV, ");
            stb.append("     DESIREDIV, ");
            stb.append("     R1.RECEPTNO ");
            stb.append(" ), KEKKA2 AS ( ");
            stb.append(" SELECT  ");
            stb.append("  TESTDIV, ");
            stb.append("  INOUT, ");
            stb.append("  SHDIV, ");
            stb.append("  DESIREDIV, ");
            stb.append("  CAST(ROUND(AVG(TOTAL), 0) AS INT) AS AVG ");
            stb.append(" FROM ");
            stb.append("  BASE2 ");
            stb.append(" GROUP BY ");
            stb.append("   TESTDIV, ");
            stb.append("  INOUT, ");
            stb.append("  SHDIV, ");
            stb.append("  DESIREDIV ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("  T1.TESTDIV, ");
            stb.append("  T1.INOUT, ");
            stb.append("  T1.SHDIV, ");
            stb.append("  T1.DESIREDIV, ");
            stb.append("  '9' AS TESTSUBCLASSCD, ");
            stb.append("  T1.MAX, ");
            stb.append("  T1.MIN, ");
            stb.append("  T1.AVG, ");
            stb.append("  T2.AVG AS KAKOAVG ");
            stb.append(" FROM KEKKA1 T1 ");
            stb.append(" LEFT JOIN KEKKA2 T2 ON T1.TESTDIV = T2.TESTDIV AND T1.INOUT = T2.INOUT AND T1.SHDIV = T2.SHDIV AND T1.DESIREDIV = T2.DESIREDIV ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     T1.TESTDIV, ");
            stb.append("     '9' AS INOUT, ");
            stb.append("     '9' AS SHDIV, ");
            stb.append("     '9' AS DESIREDIV, ");
            stb.append("     '9' AS TESTSUBCLASSCD, ");
            stb.append("     MAX(T1.MAX) AS MAX, ");
            stb.append("     MIN(T1.MIN) AS MIN, ");
            stb.append("     AVG(T1.AVG) AS AVG, ");
            stb.append("     AVG(T2.AVG) AS KAKOAVG ");
            stb.append(" FROM ");
            stb.append("     KEKKA1 T1 ");
            stb.append(" LEFT JOIN KEKKA2 T2 ON T1.TESTDIV = T2.TESTDIV AND T1.INOUT = T2.INOUT AND T1.SHDIV = T2.SHDIV AND T1.DESIREDIV = T2.DESIREDIV ");
            stb.append(" GROUP BY ");
            stb.append("   T1.TESTDIV ");
            stb.append(" ORDER BY ");
            stb.append("   TESTDIV,INOUT,SHDIV,DESIREDIV ");

            log.debug(" print2Total sql =" + stb.toString());

            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();

            while (rs.next()) {
                final String testdiv = rs.getString("TESTDIV");
                final String inout = rs.getString("INOUT");
                final String shdiv = rs.getString("SHDIV");
                final String desirediv = rs.getString("DESIREDIV");
                final String testsubclasscd = rs.getString("TESTSUBCLASSCD");
                final String highscore = rs.getString("MAX");
                final String lowscore = rs.getString("MIN");
                final String avg = rs.getString("AVG");
                final String kakoavg = rs.getString("KAKOAVG");

                final String key = testdiv + inout + shdiv + desirediv + testsubclasscd;
                if(!retMap.containsKey(key)) {
                    final Score score = new Score(testdiv, inout, shdiv, desirediv, testsubclasscd, highscore, lowscore, avg, kakoavg);
                    retMap.put(key, score);
                }

            }


        } catch (final SQLException e) {
            log.error("志願者の基本情報取得でエラー", e);
            throw e;
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        return retMap;
    }

    private Map getPrint3SenganMap(final DB2UDB db2) throws SQLException {
        Map retMap = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;

        final String namecd = "2".equals(_param._applicantDiv) ? "LH58" : "LJ58";

        try{
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH BASE AS ( ");
            stb.append(" SELECT ");
            stb.append("     R1.TESTDIV, ");
            stb.append("     R1.RECEPTNO, ");
            stb.append("     R2.REMARK1 AS SHDIV, ");
            stb.append("     R2.REMARK2 AS DESIREDIV, ");
            if("2".equals(_param._applicantDiv)) {
                stb.append("     CASE WHEN BASE.FS_CD ='" + _param._jSchoolCd + "' THEN '" + NAIBU + "' ELSE '" + GAIBU + "' END AS INOUT, ");
            } else {
                stb.append("     '" + GAIBU + "' AS INOUT, "); //中学生は外部のみ
            }
            stb.append("     M1.TESTDIV_NAME AS TEST_NAME, ");
            stb.append("     M1.TEST_DATE1 AS TEST_DATE, ");
            stb.append("     N1.NAME1 AS SHDIV_NAME, ");
            stb.append("     N2.NAME1 AS DESIREDIV_NAME, ");
            stb.append("     R1.JUDGEDIV, ");
            stb.append("     R2.REMARK8, ");
            stb.append("     R2.REMARK9 ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_RECEPT_DAT R1 ");
            stb.append("     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT R2 ");
            stb.append("          ON R2.ENTEXAMYEAR  = R1.ENTEXAMYEAR ");
            stb.append("         AND R2.APPLICANTDIV = R1.APPLICANTDIV ");
            stb.append("         AND R2.TESTDIV      = R1.TESTDIV ");
            stb.append("         AND R2.EXAM_TYPE    = R1.EXAM_TYPE ");
            stb.append("         AND R2.RECEPTNO     = R1.RECEPTNO ");
            stb.append("         AND R2.SEQ          = '006' ");
            stb.append("     LEFT JOIN ENTEXAM_TESTDIV_MST M1 ");
            stb.append("          ON M1.ENTEXAMYEAR  = R1.ENTEXAMYEAR ");
            stb.append("         AND M1.APPLICANTDIV = R1.APPLICANTDIV ");
            stb.append("         AND M1.TESTDIV      = R1.TESTDIV ");
            stb.append("     LEFT JOIN V_NAME_MST N1 ");
            stb.append("          ON N1.YEAR  = R1.ENTEXAMYEAR ");
            stb.append("         AND N1.NAMECD1 = 'L006' ");
            stb.append("         AND N1.NAMECD2 = R2.REMARK1 ");
            stb.append("     LEFT JOIN V_NAME_MST N2 ");
            stb.append("          ON N2.YEAR  = R1.ENTEXAMYEAR ");
            stb.append("         AND N2.NAMECD1 = '" + namecd + "' ");
            stb.append("         AND N2.NAMECD2 = R2.REMARK2 ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT BASE ");
            stb.append("          ON BASE.ENTEXAMYEAR = R1.ENTEXAMYEAR ");
            stb.append("          AND BASE.APPLICANTDIV = R1.APPLICANTDIV ");
            stb.append("          AND BASE.TESTDIV = R1.TESTDIV ");
            stb.append("          AND BASE.EXAMNO = R1.EXAMNO ");
            stb.append(" WHERE ");
            stb.append("         R1.ENTEXAMYEAR  = '" + _param._examYear + "' ");
            stb.append("     AND R1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
            stb.append("     AND R1.EXAM_TYPE    = '1' ");
            stb.append("     AND R2.REMARK1 = '" + SENGAN + "' ");
            stb.append(" ORDER BY ");
            stb.append("     R1.TESTDIV, ");
            stb.append("     R1.RECEPTNO ");
            stb.append(" ) ");
            stb.append(" SELECT  ");
            stb.append("   TESTDIV, ");
            stb.append("   SHDIV, ");
            stb.append("   DESIREDIV, ");
            stb.append("   INOUT, ");
            stb.append("   COUNT(RECEPTNO) AS SYUTSUGAN, ");
            stb.append("   COUNT(CASE WHEN JUDGEDIV <> '4' OR JUDGEDIV IS NULL THEN RECEPTNO END) AS JUKEN, ");
            stb.append("   COUNT(CASE WHEN REMARK8 = '" + RUI1 + "' THEN RECEPTNO END) AS GOUKAKU1, ");
            stb.append("   COUNT(CASE WHEN REMARK8 = '" + RUI2 + "' THEN RECEPTNO END) AS GOUKAKU2, ");
            stb.append("   COUNT(CASE WHEN REMARK8 = '" + SHINGAKU + "' THEN RECEPTNO END) AS SHINGAKU, ");
            stb.append("   COUNT(CASE WHEN REMARK8 IN ('" + RUI1 + "','" + RUI2 + "','" + SHINGAKU + "') THEN RECEPTNO END) AS TOTAL, ");
            stb.append("   COUNT(CASE WHEN JUDGEDIV <> '4' OR JUDGEDIV IS NULL THEN RECEPTNO END) -  COUNT(CASE WHEN REMARK8 IN ('" + RUI1 + "','" + RUI2 + "','" + SHINGAKU + "') THEN RECEPTNO END) AS NG ");
            stb.append(" FROM ");
            stb.append("   BASE ");
            stb.append(" WHERE ");
            stb.append("   SHDIV = '" + SENGAN + "' ");
            stb.append(" GROUP BY ");
            stb.append("   TESTDIV, ");
            stb.append("   SHDIV, ");
            stb.append("   DESIREDIV, ");
            stb.append("   INOUT ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT  ");
            stb.append("   TESTDIV, ");
            stb.append("   SHDIV, ");
            stb.append("   '99' AS DESIREDIV, ");
            stb.append("   '99' AS INOUT, ");
            stb.append("   COUNT(RECEPTNO) AS SYUTSUGAN, ");
            stb.append("   COUNT(CASE WHEN JUDGEDIV <> '4' OR JUDGEDIV IS NULL THEN RECEPTNO END) AS JUKEN, ");
            stb.append("   COUNT(CASE WHEN REMARK8 = '" + RUI1 + "' THEN RECEPTNO END) AS GOUKAKU1, ");
            stb.append("   COUNT(CASE WHEN REMARK8 = '" + RUI2 + "' THEN RECEPTNO END) AS GOUKAKU2, ");
            stb.append("   COUNT(CASE WHEN REMARK8 = '" + SHINGAKU + "' THEN RECEPTNO END) AS SHINGAKU, ");
            stb.append("   COUNT(CASE WHEN REMARK8 IN ('" + RUI1 + "','" + RUI2 + "','" + SHINGAKU + "') THEN RECEPTNO END) AS TOTAL, ");
            stb.append("   COUNT(CASE WHEN JUDGEDIV <> '4' OR JUDGEDIV IS NULL THEN RECEPTNO END) -  COUNT(CASE WHEN REMARK8 IN ('" + RUI1 + "','" + RUI2 + "','" + SHINGAKU + "') THEN RECEPTNO END) AS NG ");
            stb.append(" FROM ");
            stb.append("   BASE ");
            stb.append(" WHERE ");
            stb.append("   SHDIV = '" + SENGAN + "' ");
            stb.append(" GROUP BY ");
            stb.append("   TESTDIV, ");
            stb.append("   SHDIV ");
            stb.append(" ORDER BY ");
            stb.append("   TESTDIV, ");
            stb.append("   SHDIV, ");
            stb.append("   DESIREDIV, ");
            stb.append("   INOUT ");

            log.debug(" print3Sengan sql =" + stb.toString());

            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();

            while (rs.next()) {
                final String testdiv = rs.getString("TESTDIV");
                final String shdiv = rs.getString("SHDIV");
                final String desirediv = rs.getString("DESIREDIV");
                final String inout = rs.getString("INOUT");
                final String syutsugan = rs.getString("SYUTSUGAN");
                final String juken = rs.getString("JUKEN");
                final String goukaku1 = rs.getString("GOUKAKU1");
                final String goukaku2 = rs.getString("GOUKAKU2");
                final String shingaku = rs.getString("SHINGAKU");
                final String total = rs.getString("TOTAL");
                final String ng = rs.getString("NG");

                final String key = testdiv + shdiv + desirediv + inout;
                if(!retMap.containsKey(key)) {
                    final Toukei toukei = new Toukei(testdiv, shdiv, desirediv, inout, syutsugan, juken, goukaku1, goukaku2, shingaku, total, ng);
                    retMap.put(key, toukei);
                }

            }

        } catch (final SQLException e) {
            log.error("志願者の基本情報取得でエラー", e);
            throw e;
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retMap;

    }

    private Map getPrint3HeiganMap(final DB2UDB db2) throws SQLException {
        Map retMap = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;

        final String namecd = "2".equals(_param._applicantDiv) ? "LH58" : "LJ58";

        try{
            final StringBuffer stb = new StringBuffer();
            //ベース表
            stb.append(" WITH BASE AS ( ");
            stb.append(" SELECT ");
            stb.append("     R1.TESTDIV, ");
            stb.append("     R1.RECEPTNO, ");
            stb.append("     R2.REMARK1 AS SHDIV, ");
            stb.append("     R2.REMARK2 AS DESIREDIV, ");
            stb.append("     M1.TESTDIV_NAME AS TEST_NAME, ");
            stb.append("     M1.TEST_DATE1 AS TEST_DATE, ");
            stb.append("     N1.NAME1 AS SHDIV_NAME, ");
            stb.append("     N2.NAME1 AS DESIREDIV_NAME, ");
            stb.append("     R1.JUDGEDIV, ");
            stb.append("     R2.REMARK8, ");
            stb.append("     R2.REMARK9, ");
            stb.append("     B029.REMARK8 AS JIZENSENGAN, ");
            stb.append("     NM_JS_COURSE.NAME1 AS JIZENSENGAN_NAME ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_RECEPT_DAT R1 ");
            stb.append("     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT R2 ");
            stb.append("          ON R2.ENTEXAMYEAR  = R1.ENTEXAMYEAR ");
            stb.append("         AND R2.APPLICANTDIV = R1.APPLICANTDIV ");
            stb.append("         AND R2.TESTDIV      = R1.TESTDIV ");
            stb.append("         AND R2.EXAM_TYPE    = R1.EXAM_TYPE ");
            stb.append("         AND R2.RECEPTNO     = R1.RECEPTNO ");
            stb.append("         AND R2.SEQ          = '006' ");
            stb.append("     LEFT JOIN ENTEXAM_TESTDIV_MST M1 ");
            stb.append("          ON M1.ENTEXAMYEAR  = R1.ENTEXAMYEAR ");
            stb.append("         AND M1.APPLICANTDIV = R1.APPLICANTDIV ");
            stb.append("         AND M1.TESTDIV      = R1.TESTDIV ");
            stb.append("     LEFT JOIN V_NAME_MST N1 ");
            stb.append("          ON N1.YEAR  = R1.ENTEXAMYEAR ");
            stb.append("         AND N1.NAMECD1 = 'L006' ");
            stb.append("         AND N1.NAMECD2 = R2.REMARK1 ");
            stb.append("     LEFT JOIN V_NAME_MST N2 ");
            stb.append("          ON N2.YEAR  = R1.ENTEXAMYEAR ");
            stb.append("         AND N2.NAMECD1 = '" + namecd + "' ");
            stb.append("         AND N2.NAMECD2 = R2.REMARK2 ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT B029 ");
            stb.append("      ON B029.ENTEXAMYEAR    = R1.ENTEXAMYEAR ");
            stb.append("     AND B029.APPLICANTDIV   = R1.APPLICANTDIV ");
            stb.append("     AND B029.EXAMNO         = R1.EXAMNO ");
            stb.append("     AND B029.SEQ            = '029' ");
            stb.append("     LEFT JOIN V_NAME_MST NM_JS_COURSE ");
            stb.append("      ON NM_JS_COURSE.YEAR       = B029.ENTEXAMYEAR ");
            stb.append("     AND NM_JS_COURSE.NAMECD1    = '" + namecd + "' ");
            stb.append("     AND NM_JS_COURSE.NAMECD2    = B029.REMARK8 ");
            stb.append(" WHERE ");
            stb.append("     R1.ENTEXAMYEAR  = '" + _param._examYear + "' ");
            stb.append("     AND R1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
            stb.append("     AND R1.EXAM_TYPE    = '1' ");
            stb.append("     AND R2.REMARK1 = '" + HEIGAN + "' ");
            stb.append(" ORDER BY ");
            stb.append("     R1.TESTDIV, ");
            stb.append("     R1.RECEPTNO ");
            stb.append(" ), BASE2  AS ( ");
            //併願合格者数
            stb.append(" SELECT ");
            stb.append("     TESTDIV, ");
            stb.append("     SHDIV, ");
            stb.append("     DESIREDIV, ");
            stb.append("     '0' AS DIV, ");
            stb.append("     COUNT(RECEPTNO) AS SYUTSUGAN, ");
            stb.append("     COUNT(CASE WHEN JUDGEDIV <> '4' OR JUDGEDIV IS NULL THEN RECEPTNO END) AS JUKEN, ");
            stb.append("     COUNT(CASE WHEN REMARK9 = '" + RUI1 + "' THEN RECEPTNO END) AS GOUKAKU1, ");
            stb.append("     COUNT(CASE WHEN REMARK9 = '" + RUI2 + "' THEN RECEPTNO END) AS GOUKAKU2, ");
            stb.append("     COUNT(CASE WHEN REMARK9 = '" + SHINGAKU + "' THEN RECEPTNO END) AS SHINGAKU, ");
            stb.append("     COUNT(CASE WHEN REMARK9 IN ('" + RUI1 + "','" + RUI2 + "','"+ SHINGAKU + "') THEN RECEPTNO END) AS TOTAL, ");
            stb.append("     COUNT(CASE WHEN JUDGEDIV <> '4' OR JUDGEDIV IS NULL THEN RECEPTNO END) -  COUNT(CASE WHEN REMARK9 IN ('" + RUI1 + "','" + RUI2 + "','"+ SHINGAKU + "') THEN RECEPTNO END) AS NG ");
            stb.append(" FROM ");
            stb.append("     BASE ");
            stb.append(" WHERE ");
            stb.append("   SHDIV = '" + HEIGAN + "' ");
            stb.append(" GROUP BY ");
            stb.append("     TESTDIV, ");
            stb.append("     SHDIV, ");
            stb.append("     DESIREDIV ");
            stb.append(" UNION ALL   ");
            //内事前専願１合格者数
            stb.append(" SELECT ");
            stb.append("     TESTDIV, ");
            stb.append("     SHDIV, ");
            stb.append("     DESIREDIV, ");
            stb.append("     '1' AS DIV, ");
            stb.append("     '0' AS SYUTSUGAN, ");
            stb.append("     '0' AS JUKEN, ");
            stb.append("     '0' AS GOUKAKU1, ");
            stb.append("     COUNT(CASE WHEN REMARK8 = '" + RUI1 + "' AND REMARK9 = '" + RUI2 + "' THEN RECEPTNO END) AS GOUKAKU2, ");
            stb.append("     COUNT(CASE WHEN REMARK8 = '" + RUI1 + "' AND REMARK9 = '" + SHINGAKU + "' THEN RECEPTNO END) AS SHINGAKU, ");
            stb.append("     '0' AS TOTAL, ");
            stb.append("     '0' AS NG ");
            stb.append(" FROM ");
            stb.append("     BASE ");
            stb.append(" WHERE ");
            stb.append("   SHDIV = '" + HEIGAN + "' ");
            stb.append(" GROUP BY ");
            stb.append("     TESTDIV, ");
            stb.append("     SHDIV, ");
            stb.append("     DESIREDIV ");
            stb.append(" UNION ALL   ");
            //内事前専願２合格者数
            stb.append(" SELECT ");
            stb.append("     TESTDIV, ");
            stb.append("     SHDIV, ");
            stb.append("     DESIREDIV, ");
            stb.append("     '2' AS DIV, ");
            stb.append("     '0' AS SYUTSUGAN, ");
            stb.append("     '0' AS JUKEN, ");
            stb.append("     '0' AS GOUKAKU1, ");
            stb.append("     '0' AS GOUKAKU2, ");
            stb.append("     COUNT(CASE WHEN REMARK8 = '" + RUI2 + "' AND REMARK9 = '" + SHINGAKU + "' THEN RECEPTNO END) AS SHINGAKU, ");
            stb.append("     '0' AS TOTAL, ");
            stb.append("     '0' AS NG ");
            stb.append(" FROM ");
            stb.append("     BASE ");
            stb.append(" WHERE ");
            stb.append("   SHDIV = '" + HEIGAN + "' ");
            stb.append(" GROUP BY ");
            stb.append("     TESTDIV, ");
            stb.append("     SHDIV, ");
            stb.append("     DESIREDIV ");
            stb.append(" ORDER BY ");
            stb.append("     TESTDIV, ");
            stb.append("     SHDIV, ");
            stb.append("     DESIREDIV ");
            //総合計
            stb.append(" ), TOTAL AS (SELECT ");
            stb.append("     TESTDIV, ");
            stb.append("     SHDIV, ");
            stb.append("     'ZZ' AS DESIREDIV, ");
            stb.append("     'ZZ' AS DIV, ");
            stb.append("     SUM(SYUTSUGAN) AS SYUTSUGAN, ");
            stb.append("     SUM(JUKEN) AS JUKEN, ");
            stb.append("     SUM(GOUKAKU1) AS GOUKAKU1, ");
            stb.append("     SUM(GOUKAKU2) AS GOUKAKU2, ");
            stb.append("     SUM(SHINGAKU) AS SHINGAKU, ");
            stb.append("     SUM(TOTAL) AS TOTAL, ");
            stb.append("     '0' AS NG ");
            stb.append(" FROM ");
            stb.append("     BASE2 ");
            stb.append(" GROUP BY ");
            stb.append("     TESTDIV, ");
            stb.append("     SHDIV ");
            stb.append(" )  ");
            stb.append(" SELECT ");
            stb.append("   BASE2.* ");
            stb.append(" FROM ");
            stb.append("   BASE2 ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("   TOTAL.* ");
            stb.append(" FROM ");
            stb.append("   TOTAL ");
            stb.append(" ORDER BY ");
            stb.append("   TESTDIV, SHDIV, DESIREDIV ");

            log.debug(" print3Heigan sql =" + stb.toString());

            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();

            while (rs.next()) {
                final String testdiv = rs.getString("TESTDIV");
                final String shdiv = rs.getString("SHDIV");
                final String desirediv = rs.getString("DESIREDIV");
                final String div = rs.getString("DIV");
                final String syutsugan = rs.getString("SYUTSUGAN");
                final String juken = rs.getString("JUKEN");
                final String goukaku1 = rs.getString("GOUKAKU1");
                final String goukaku2 = rs.getString("GOUKAKU2");
                final String shingaku = rs.getString("SHINGAKU");
                final String total = rs.getString("TOTAL");
                final String ng = rs.getString("NG");

                final String key = testdiv + shdiv + desirediv + div;
                if(!retMap.containsKey(key)) {
                    final Toukei toukei = new Toukei(testdiv, shdiv, desirediv, div, syutsugan, juken, goukaku1, goukaku2, shingaku, total, ng);
                    retMap.put(key, toukei);
                }
            }

        } catch (final SQLException e) {
            log.error("志願者の基本情報取得でエラー", e);
            throw e;
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retMap;

    }

    //併願不合格、専願合格者
    private Map getPrint3NgMap(final DB2UDB db2) throws SQLException {
        Map retMap = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;

        final String namecd = "2".equals(_param._applicantDiv) ? "LH58" : "LJ58";

        try{
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH BASE AS ( ");
            stb.append(" SELECT ");
            stb.append("     R1.TESTDIV, ");
            stb.append("     R1.RECEPTNO, ");
            stb.append("     R2.REMARK1 AS SHDIV, ");
            stb.append("     R2.REMARK2 AS DESIREDIV, ");
            stb.append("     M1.TESTDIV_NAME AS TEST_NAME, ");
            stb.append("     M1.TEST_DATE1 AS TEST_DATE, ");
            stb.append("     N1.NAME1 AS SHDIV_NAME, ");
            stb.append("     N2.NAME1 AS DESIREDIV_NAME, ");
            stb.append("     R1.JUDGEDIV, ");
            stb.append("     R2.REMARK8, ");
            stb.append("     R2.REMARK9, ");
            stb.append("     B029.REMARK8 AS JIZENSENGAN, ");
            stb.append("     NM_JS_COURSE.NAME1 AS JIZENSENGAN_NAME ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_RECEPT_DAT R1       ");
            stb.append("     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT R2 ");
            stb.append("          ON R2.ENTEXAMYEAR  = R1.ENTEXAMYEAR ");
            stb.append("         AND R2.APPLICANTDIV = R1.APPLICANTDIV ");
            stb.append("         AND R2.TESTDIV      = R1.TESTDIV ");
            stb.append("         AND R2.EXAM_TYPE    = R1.EXAM_TYPE ");
            stb.append("         AND R2.RECEPTNO     = R1.RECEPTNO ");
            stb.append("         AND R2.SEQ          = '006' ");
            stb.append("     LEFT JOIN ENTEXAM_TESTDIV_MST M1 ON M1.ENTEXAMYEAR  = R1.ENTEXAMYEAR AND M1.APPLICANTDIV = R1.APPLICANTDIV AND M1.TESTDIV = R1.TESTDIV ");
            stb.append("     LEFT JOIN V_NAME_MST N1  ON N1.YEAR  = R1.ENTEXAMYEAR AND N1.NAMECD1 = 'L006' AND N1.NAMECD2 = R2.REMARK1 ");
            stb.append("     LEFT JOIN V_NAME_MST N2  ON N2.YEAR  = R1.ENTEXAMYEAR AND N2.NAMECD1 = '" + namecd + "' AND N2.NAMECD2 = R2.REMARK2 ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT B029 ON B029.ENTEXAMYEAR    = R1.ENTEXAMYEAR      AND B029.APPLICANTDIV   = R1.APPLICANTDIV      AND B029.EXAMNO = R1.EXAMNO AND B029.SEQ = '029' ");
            stb.append("     LEFT JOIN V_NAME_MST NM_JS_COURSE ON NM_JS_COURSE.YEAR = B029.ENTEXAMYEAR AND NM_JS_COURSE.NAMECD1 = '" + namecd + "' AND NM_JS_COURSE.NAMECD2 = B029.REMARK8 ");
            stb.append(" WHERE ");
            stb.append("     R1.ENTEXAMYEAR  = '" + _param._examYear + "' AND ");
            stb.append("     R1.APPLICANTDIV = '" + _param._applicantDiv + "' AND ");
            stb.append("     R1.EXAM_TYPE = '1' AND ");
            stb.append("     R2.REMARK1 = '" + HEIGAN + "' ");
            stb.append(" ORDER BY ");
            stb.append("     R1.TESTDIV, ");
            stb.append("     R1.RECEPTNO ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     TESTDIV, ");
            stb.append("     DESIREDIV, ");
            stb.append("     COUNT(CASE WHEN (REMARK9 = '0' OR REMARK9 IS NULL) AND REMARK8 > 0 THEN RECEPTNO END) AS NG ");
            stb.append(" FROM ");
            stb.append("     BASE ");
            stb.append(" GROUP BY ");
            stb.append("     TESTDIV, ");
            stb.append("     DESIREDIV ");


            log.debug(" print3Ng sql =" + stb.toString());

            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();

            while (rs.next()) {
                final String testdiv = rs.getString("TESTDIV");
                final String desirediv = rs.getString("DESIREDIV");
                final String ng = rs.getString("NG");

                final String key = testdiv + desirediv;
                if(!retMap.containsKey(key)) {
                    retMap.put(key, ng);
                }
            }

        } catch (final SQLException e) {
            log.error("志願者の基本情報取得でエラー", e);
            throw e;
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retMap;

    }

    private class SubclassMst {
        final String _subclassCd;
        final String _subclassName;
        final String _remark2;

        public SubclassMst(final String subclassCd, final String subclassName, final String remark2) {
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            _remark2 = remark2;
        }
    }

    private class Score {
        final String _testdiv;
        final String _inout;
        final String _shdiv;
        final String _desirediv;
        final String _testsubclasscd;
        final String _highscore;
        final String _lowscore;
        final String _avg;
        final String _kakoavg;

        public Score (final String testdiv, final String inout, final String shdiv, final String desirediv, final String testsubclasscd, final String highscore, final String lowscore, final String avg, final String kakoavg) {
            _testdiv = testdiv;
            _inout = inout;
            _shdiv = shdiv;
            _desirediv = desirediv;
            _testsubclasscd = testsubclasscd;
            _highscore = highscore;
            _lowscore = lowscore;
            _avg = avg;
            _kakoavg = kakoavg;
        }
    }

    private class Toukei {
        final String _testdiv;
        final String _shdiv;
        final String _desirediv;
        final String _syutsugan;
        final String _juken;
        final String _div;
        final String _goukaku1;
        final String _goukaku2;
        final String _shingaku;
        final String _total;
        final String _ng;

        public Toukei (final String testdiv, final String shdiv, final String desirediv, final String div, final String syutsugan, final String juken, final String goukaku1, final String goukaku2, final String shingaku, final String total, final String ng ) {
            _testdiv = testdiv;
            _shdiv = shdiv;
            _desirediv = desirediv;
            _div = div;
            _syutsugan = syutsugan;
            _juken = juken;
            _goukaku1 = goukaku1;
            _goukaku2 = goukaku2;
            _shingaku = shingaku;
            _total = total;
            _ng = ng;
        }

    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Id: 33dd673a005492964fbd4344c72a0d0402b7a933 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _examYear;
        final String _applicantDiv; //入試制度
        final String _testDiv; //入試区分
        final String _date;
        final String _jSchoolCd; //中学コード
        final Map _testMap; //入試区分名称
        final Map _subclassNameMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _examYear = request.getParameter("ENTEXAMYEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _date = request.getParameter("LOGIN_DATE");
            _jSchoolCd = getJSchoolCd(db2);
            _testMap = getTestDiv(db2);
            _subclassNameMap = getSubclassName(db2);
        }

        private String getJSchoolCd(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAMESPARE1 FROM V_NAME_MST WHERE YEAR = '" + _examYear + "' AND NAMECD1 = 'A023' AND NAME1 = 'J' "));
        }

        private Map getTestDiv(final DB2UDB db2) {
            final Map retMap = new LinkedMap();
            for (final Map<String, String> row : KnjDbUtils.query(db2, "SELECT TESTDIV,TESTDIV_NAME FROM ENTEXAM_TESTDIV_MST WHERE ENTEXAMYEAR = '" + _examYear + "' AND APPLICANTDIV = '" + _applicantDiv + "' ")) {
                if(!retMap.containsKey(KnjDbUtils.getString(row, "TESTDIV"))) {
                    retMap.put(KnjDbUtils.getString(row, "TESTDIV"), KnjDbUtils.getString(row, "TESTDIV_NAME"));
                }
            }
            return retMap;
        }

        private Map getL008Name(final DB2UDB db2) {
            final Map retMap = new LinkedMap();
            for (final Map<String, String> row : KnjDbUtils.query(db2, "SELECT SEQ,NAME1 FROM ENTEXAM_SETTING_MST WHERE ENTEXAMYEAR = '" + _examYear + "' AND APPLICANTDIV = '" + _applicantDiv + "' AND SETTING_CD = 'L008' ORDER BY SEQ")) {
                if(!retMap.containsKey(KnjDbUtils.getString(row, "SEQ"))) {
                    retMap.put(KnjDbUtils.getString(row, "SEQ"), KnjDbUtils.getString(row, "NAME1"));
                }
            }
            return retMap;
        }

        private Map getSubclassName(final DB2UDB db2) {
            final Map retMap = new LinkedMap();
            for (final Map<String, String> row : KnjDbUtils.query(db2, "SELECT NAMECD2,NAME2 FROM V_NAME_MST WHERE YEAR = '" + _examYear + "' AND NAMECD1 = 'L009' ")) {
                if(!retMap.containsKey(KnjDbUtils.getString(row, "NAMECD2"))) {
                    retMap.put(KnjDbUtils.getString(row, "NAMECD2"), KnjDbUtils.getString(row, "NAME2"));
                }
            }
            return retMap;
        }

    }

    public static String h_format_Seireki_MD(final String date) {
        if (null == date) {
            return date;
        }
        SimpleDateFormat sdf = new SimpleDateFormat();
        String retVal = "";
        sdf.applyPattern("yyyy年M月d日");
        retVal = sdf.format(java.sql.Date.valueOf(date));

        return retVal;
    }
}

// eof
