package servletpack.KNJL;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *
 *    学校教育システム 賢者 [入試管理]
 *
 **/

public class KNJL337Q {

    private static final Log log = LogFactory.getLog(KNJL337Q.class);

    private final SimpleDateFormat _dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final DecimalFormat _df02 = new DecimalFormat("00");
    
    private final Integer FUTUUKA_KENNAI_OTOKO = new Integer(1);
    private final Integer FUTUUKA_KENNAI_ONNA = new Integer(2);
    private final Integer FUTUUKA_KENGAI = new Integer(3);
    private final Integer FUTUUKA_SUISEN_KOCHO = new Integer(4);
    private final Integer FUTUUKA_SUISEN_JIKO = new Integer(5);
    private final Integer FUTUUKA_SUNDAI_CHUGAKU = new Integer(6);
    private final Integer SPORTS_IPPAN = new Integer(7);
    private final Integer SPORTS_SUISEN = new Integer(8);
    private final Integer ZENTAI = new Integer(9);
    
    private final String FINSCHOOLCD_SUNDAI_FUTSUU = "3008112"; // 駿台甲府・普通
    
    private final String EXAMCOURSECD_FUTUUKA = "11";
    private final String EXAMCOURSECD_SPORTS = "12";
    
    private final String TESTDIV0_2_SUISEN = "2";
    private final String TESTDIV0_3_IPPAN = "3";
    private final String TESTDIV0_9_SUNCHU = "9";

    private final String TESTDIV_3_SUISEN_KOCHO = "3";
    private final String TESTDIV_4_SUISEN_JIKO = "4";
//    private final String TESTDIV_9_SUNCHU = "9";

    private boolean _hasData;
    private Param _param;

    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        Vrw32alp svf     = new Vrw32alp();   //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2       = null;             //Databaseクラスを継承したクラス

        log.fatal("$Revision: 56855 $ $Date: 2017-10-31 12:11:33 +0900 (火, 31 10 2017) $"); // CVSキーワードの取り扱いに注意

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
            
            printMain(db2, svf);
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
     * listを最大数ごとにグループ化したリストを得る
     * @param list
     * @param max 最大数
     * @return listを最大数ごとにグループ化したリスト
     */
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
    
    private static List getMappedList(final Map map, final Object key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList());
        }
        return (List) map.get(key1);
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        String form = "";
        final int maxLine = 55;
        int maxColumn = 0;
        String title = "";
        List columnAllList = new ArrayList();
        
        final List applicantList = Applicant.getApplicantList(db2, _param);
        Map columnApplicantListMap = new HashMap();
        Map columnNameMap = new TreeMap();
        
        if ("1".equals(_param._taisyou)) {
            form = "KNJL337Q_2.frm";
            maxColumn = 9;
            title = "度数分布";

            if (TESTDIV0_2_SUISEN.equals(_param._testdiv0)) {
                // 推薦
                columnAllList.addAll(Arrays.asList(new Integer[]{FUTUUKA_KENNAI_OTOKO, FUTUUKA_KENNAI_ONNA, FUTUUKA_KENGAI, SPORTS_IPPAN, ZENTAI}));
            } else {
                // 一般
                columnAllList.addAll(Arrays.asList(new Integer[]{FUTUUKA_KENNAI_OTOKO, FUTUUKA_KENNAI_ONNA, FUTUUKA_KENGAI, FUTUUKA_SUISEN_KOCHO, FUTUUKA_SUISEN_JIKO, FUTUUKA_SUNDAI_CHUGAKU, SPORTS_IPPAN, SPORTS_SUISEN, ZENTAI}));
            }
            
            columnNameMap.put(FUTUUKA_KENNAI_OTOKO, "県内男");
            columnNameMap.put(FUTUUKA_KENNAI_ONNA, "県内女");
            columnNameMap.put(FUTUUKA_KENGAI, "県外");
            columnNameMap.put(FUTUUKA_SUISEN_KOCHO, "推薦");
            columnNameMap.put(FUTUUKA_SUISEN_JIKO, "自己");
            columnNameMap.put(FUTUUKA_SUNDAI_CHUGAKU, "駿中");
            columnNameMap.put(SPORTS_IPPAN, "ス入試");
            columnNameMap.put(SPORTS_SUISEN, "ス推");
            columnNameMap.put(ZENTAI, "全体");
            
            final List appliantListForColumn = new ArrayList(applicantList);

            for (final Iterator it = appliantListForColumn.iterator(); it.hasNext();) {
                final Applicant appl = (Applicant) it.next();
                
                Integer iColumn = null;
                if (null != appl._dai1Coursecode) {
                    if (TESTDIV0_2_SUISEN.equals(_param._testdiv0)) {

                        if (appl._dai1Coursecode.endsWith(EXAMCOURSECD_FUTUUKA)) { // 普通科
                            if ("1".equals(appl._kenNaigai)) { // 県内
                                if ("1".equals(appl._sex)) { // 男
                                    iColumn = FUTUUKA_KENNAI_OTOKO;
                                } else if ("2".equals(appl._sex)) { // 女
                                    iColumn = FUTUUKA_KENNAI_ONNA;
                                }
                            } else if ("2".equals(appl._kenNaigai)) { // 県外
                                iColumn = FUTUUKA_KENGAI;
                            }
                        } else if (appl._dai1Coursecode.endsWith(EXAMCOURSECD_SPORTS)) { // スポーツ科
                            if (TESTDIV_3_SUISEN_KOCHO.equals(appl._testdiv) || TESTDIV_4_SUISEN_JIKO.equals(appl._testdiv)) { // 校長推薦 or 自己推薦 // 推薦
                                iColumn = SPORTS_IPPAN;
                            }
                        }

                    } else if (TESTDIV0_3_IPPAN.equals(_param._testdiv0)) {

                        if (appl._dai1Coursecode.endsWith(EXAMCOURSECD_FUTUUKA)) { // 普通科
                            if (TESTDIV0_3_IPPAN.equals(appl._testdiv0)) { // 一般
                                if ("1".equals(appl._kenNaigai)) { // 県内
                                    if ("1".equals(appl._sex)) { // 男
                                        iColumn = FUTUUKA_KENNAI_OTOKO;
                                    } else if ("2".equals(appl._sex)) { // 女
                                        iColumn = FUTUUKA_KENNAI_ONNA;
                                    }
                                } else if ("2".equals(appl._kenNaigai)) { // 県外
                                    iColumn = FUTUUKA_KENGAI;
                                }
                            } else if (TESTDIV0_2_SUISEN.equals(appl._testdiv0)) { // 推薦
                                if (TESTDIV_3_SUISEN_KOCHO.equals(appl._testdiv)) { // 校長推薦
                                    iColumn = FUTUUKA_SUISEN_KOCHO;
                                } else if (TESTDIV_4_SUISEN_JIKO.equals(appl._testdiv)) { // 自己推薦
                                    iColumn = FUTUUKA_SUISEN_JIKO;
                                }
                            } else if (TESTDIV0_9_SUNCHU.equals(appl._testdiv0)) { // || TESTDIV_9_SUNCHU.equals(appl._testdiv) { // 駿中
                                iColumn = FUTUUKA_SUNDAI_CHUGAKU;
                            }
                        } else if (appl._dai1Coursecode.endsWith(EXAMCOURSECD_SPORTS)) { // スポーツ科
                            if (TESTDIV0_3_IPPAN.equals(appl._testdiv0)) {
                                iColumn = SPORTS_IPPAN;
                            } else if (TESTDIV_3_SUISEN_KOCHO.equals(appl._testdiv) || TESTDIV_4_SUISEN_JIKO.equals(appl._testdiv)) { // 校長推薦 or 自己推薦 // 推薦
                                iColumn = SPORTS_SUISEN;
                            }
                        }

                    } 
                }
                if (null != iColumn) {
                    getMappedList(columnApplicantListMap, iColumn).add(appl);
                    getMappedList(columnApplicantListMap, ZENTAI).add(appl);
                }
            }

        } else if ("2".equals(_param._taisyou)) {
            form = "KNJL337Q_2.frm";
            maxColumn = 9;
            title = "志望校別度数分布";
            
            for (final Iterator it = applicantList.iterator(); it.hasNext();) {
                final Applicant appl = (Applicant) it.next();
                if (null != appl._shSchoolcd) {
                    columnNameMap.put(appl._shSchoolcd, appl._shSchoolNameAbbv);
                    getMappedList(columnApplicantListMap, appl._shSchoolcd).add(appl);
                }
            }
            
            final List finschoolcdSet = new ArrayList(columnNameMap.keySet());
            if (finschoolcdSet.contains(FINSCHOOLCD_SUNDAI_FUTSUU)) { // 「駿台甲府・普通」があれば最初の列に表示
                columnAllList.add(FINSCHOOLCD_SUNDAI_FUTSUU);
                finschoolcdSet.remove(FINSCHOOLCD_SUNDAI_FUTSUU);
            }
            columnAllList.addAll(finschoolcdSet);
            log.info(" shschoolcd column = " + columnAllList);
        }
        
        final List scoreLineAllList = new ArrayList();
        // 満点-10 〜 20、5点刻み
        for (int s = _param._perfect - 10, min = 20, kizami = 5; s >= min; s -= kizami) {
            scoreLineAllList.add(new Integer(s));
        }
        
        final List columnPageList = getPageList(columnAllList, maxColumn);
        final List scorePageList = getPageList(scoreLineAllList, maxLine);
        for (int cpi = 0; cpi < columnPageList.size(); cpi++) {
            final List columnList = (List) columnPageList.get(cpi);
            
            for (int spi = 0; spi < scorePageList.size(); spi++) {
                final List scoreList = (List) scorePageList.get(spi);
                
                svf.VrSetForm(form, 4);
                
                svf.VrsOut("TITLE", Integer.parseInt(_param._entexamyear) + "年度　" + StringUtils.defaultString(_param._testdiv0Name) + "　" + title); // タイトル
                
                for (int i = 0; i < columnList.size(); i++) {
                    final Object iColumn = columnList.get(i);
                    svf.VrsOut("HOPE_SCHOOL_NAME" + String.valueOf(i + 1), (String) columnNameMap.get(iColumn)); // 志望校名
                }
                
                for (int j = 0; j < scoreList.size(); j++) {
                    final Integer scoreLower = (Integer) scoreList.get(j);
                    final Integer scoreUpper = j == 0 ? new Integer(_param._perfect + 1) : (Integer) scoreList.get(j - 1);
                    svf.VrsOut("RANK", String.valueOf(spi * maxLine + j + 1)); // 段階
                    svf.VrsOut("SCORE", scoreLower.toString() + "以上"); // 得点

                    for (int i = 0; i < columnList.size(); i++) {
                        final Object iColumn = columnList.get(i);
                        final List columnApplicantList = getMappedList(columnApplicantListMap, iColumn);
                        
                        if (getTargetScoreApplicantList(scoreLower, scoreUpper, columnApplicantList).size() > 0) {
                            final int ruikei = getTargetScoreApplicantList(scoreLower, null, columnApplicantList).size();
                            svf.VrsOut("NUM" + String.valueOf(i + 1), String.valueOf(ruikei)); // 人数
                        }
                    }
                    svf.VrEndRecord();
                }
                _hasData = true;
            }
        }
    }

    /**
     * 志願者のから対象得点範囲の志願者リストを抽出
     * @param lowScoreInclusive 得点範囲下限 指定しなければInteger.MIN_VALUE
     * @param highScoreExclusive 得点範囲上限 指定しなければInteger.MAX_VALUE
     * @param applicantList
     * @return
     */
    private List getTargetScoreApplicantList(Integer lowScoreInclusive, Integer highScoreExclusive, final List applicantList) {
        if (null == lowScoreInclusive) {
            lowScoreInclusive = new Integer(Integer.MIN_VALUE);
        }
        if (null == highScoreExclusive) {
            highScoreExclusive = new Integer(Integer.MAX_VALUE);
        }
        final List list = new ArrayList();
        for (final Iterator it = applicantList.iterator(); it.hasNext();) {
            final Applicant appl = (Applicant) it.next();
            if (NumberUtils.isNumber(appl._total4)) {
                final double total4 = Double.parseDouble(appl._total4);
                if (lowScoreInclusive.intValue() <= total4 && total4 < highScoreExclusive.intValue()) {
                    list.add(appl);
                }
            }
        }
        return list;
    }
    
    private static class Applicant {
        private String _examno;
        private String _total4;
        private String _dai1Coursecode;
        private String _testdiv0;
        private String _testdiv;
        private String _kenNaigai;
        private String _sex;
        private String _shSchoolcd;
        private String _shSchoolNameAbbv;

        public static List getApplicantList(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Applicant applicant = new Applicant();
                    applicant._examno = rs.getString("EXAMNO");
                    applicant._total4 = rs.getString("TOTAL4");
                    applicant._dai1Coursecode = rs.getString("DAI1_COURSECODE");
                    applicant._testdiv0 = rs.getString("TESTDIV0");
                    applicant._testdiv = rs.getString("TESTDIV");
                    applicant._kenNaigai = rs.getString("KEN_NAIGAI");
                    applicant._sex = rs.getString("SEX");
                    applicant._shSchoolcd = rs.getString("SH_SCHOOLCD1");
                    applicant._shSchoolNameAbbv = rs.getString("SH_SCHOOL1_NAME_ABBV");
                    list.add(applicant);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        public static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     B1.EXAMNO, ");
            stb.append("     R1.TOTAL4, ");
            //1. コース ");
            //0001:普通           (パターン01,02,03,04,05,06) ");
            //0002:スポーツ       (パターン07,08) ");
            stb.append("     B1.DAI1_COURSECODE, ");
            //2. L045 ");
            //1:海外・・・対象外 ");
            //2:推薦              (パターン04,05,08) ");
            //3:一般              (パターン01,02,03,07) ");
            //9:駿中              (パターン06) ");
            stb.append("     B1.TESTDIV0, ");
            //3. L004(パターン04,05のみ指定) ");
            //1:海外A・・・対象外 ");
            //2:海外B・・・対象外 ");
            //3:校長              (パターン04) ");
            //4:自己              (パターン05) ");
            //5:一般 ");
            //9:駿中 ");
            stb.append("     B1.TESTDIV, ");
            //4. 県内県外(パターン01,02,03) ");
            //1:県内              (パターン01,02) ");
            //2:県外              (パターン03) ");
            stb.append("     CASE WHEN F1.FINSCHOOL_PREF_CD  = '19' THEN '1' ");
            stb.append("          WHEN F1.FINSCHOOL_PREF_CD != '19' THEN '2' ");
            stb.append("     END AS KEN_NAIGAI, ");
            //5. 性別 ");
            //1:男              (パターン01) ");
            //2:女              (パターン02) ");
            stb.append("     B1.SEX, ");
            stb.append("     F2.FINSCHOOLCD AS SH_SCHOOLCD1, ");
            stb.append("     F2.FINSCHOOL_NAME_ABBV AS SH_SCHOOL1_NAME_ABBV ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_RECEPT_DAT R1 ");
            stb.append("     INNER JOIN V_ENTEXAM_APPLICANTBASE_DAT B1 ON R1.ENTEXAMYEAR = B1.ENTEXAMYEAR ");
            stb.append("             AND R1.APPLICANTDIV = B1.APPLICANTDIV ");
            stb.append("             AND R1.EXAMNO = B1.EXAMNO ");
            stb.append("     LEFT JOIN FINSCHOOL_MST F1 ON F1.FINSCHOOLCD = B1.FS_CD ");
            stb.append("     LEFT JOIN PREF_MST P1 ON P1.PREF_CD = F1.FINSCHOOL_PREF_CD ");
            stb.append("     LEFT JOIN V_NAME_MST N1 ON N1.YEAR = R1.ENTEXAMYEAR AND N1.NAMECD1 = 'L004' AND N1.NAMECD2 = R1.TESTDIV ");
            stb.append("     LEFT JOIN FINSCHOOL_MST F2 ON F2.FINSCHOOLCD = B1.SH_SCHOOLCD1 ");
            stb.append(" WHERE ");
            stb.append("     R1.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("     AND R1.APPLICANTDIV = '" + param._applicantdiv + "' ");
            //2:推薦 ");
            //度数分布（推薦） ");
            //志望校別度数分布（推薦） ");
            //3:一般 ");
            //度数分布（一般・基準・駿中） ");
            //志望校別度数分布（一般・基準・駿中） ");
            stb.append("     AND N1.ABBV3 = '" + param._testdiv0 + "' ");
            //stb.append("      AND N1.ABBV3 = '" + param._testdiv0 + "' ");
            //stb.append("      AND R1.TOTAL4 IS NOT NULL ");
            stb.append(" ORDER BY ");
            stb.append("     VALUE(R1.TOTAL4,-1) DESC, ");
            stb.append("     B1.EXAMNO ");
            return stb.toString();
        }
    }

    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv0;
        final String _loginDate;
        final String _taisyou;
        final boolean _seirekiFlg;
        final int _perfect;
        final String _testdiv0Name;

        Param(
                final DB2UDB db2,
                final HttpServletRequest request
        ) {
            _entexamyear  = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv0 = request.getParameter("TESTDIV");
            _loginDate    = request.getParameter("LOGIN_DATE");
            _taisyou   = request.getParameter("TAISYOU");
            _seirekiFlg = getSeirekiFlg(db2);
            _perfect = getPerfect(db2);
            _testdiv0Name = getTestdiv0Name(db2);
        }
        
        /* 西暦表示にするのかのフラグ  */
        private boolean getSeirekiFlg(final DB2UDB db2) {
            final String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z012' AND NAMECD2 = '00' AND NAME1 IS NOT NULL ";
            boolean seirekiFlg = false;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    if ("2".equals(rs.getString("NAME1"))) seirekiFlg = true; //西暦
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
        
        
        private String getTestdiv0Name(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String rtn = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("   L045.NAME1 ");
                stb.append(" FROM V_NAME_MST L045 ");
                stb.append(" WHERE ");
                stb.append("     L045.YEAR = '" + _entexamyear + "' ");
                stb.append("     AND L045.NAMECD1 = 'L045' ");
                stb.append("     AND L045.NAMECD2 = '" + _testdiv0 + "' ");
                
                final String sql = stb.toString();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("NAME1");
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private int getPerfect(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            int perfect = 0;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     T1.COURSECD || T1.MAJORCD || T1.EXAMCOURSECD AS COURSE ");
                stb.append("   , L045.NAMECD2 ");
                stb.append("   , L045.NAME1 ");
                stb.append("   , SUM(PERFECT) AS PERFECT ");
                stb.append(" FROM ENTEXAM_PERFECT_MST T1 ");
                stb.append(" INNER JOIN V_NAME_MST L004 ON L004.YEAR = T1.ENTEXAMYEAR ");
                stb.append("     AND L004.NAMECD1 = 'L004' ");
                stb.append("     AND L004.NAMECD2 = T1.TESTDIV ");
                stb.append(" INNER JOIN V_NAME_MST L045 ON L045.YEAR = T1.ENTEXAMYEAR ");
                stb.append("     AND L045.NAMECD1 = 'L045' ");
                stb.append("     AND L045.NAMECD2 = L004.ABBV3 ");
                stb.append(" WHERE ");
                stb.append("     T1.ENTEXAMYEAR = '" + _entexamyear + "' ");
                stb.append("     AND T1.APPLICANTDIV = '" + _applicantdiv + "' ");
                stb.append("     AND L045.NAMECD2 = '" + _testdiv0 + "' ");
                stb.append(" GROUP BY ");
                stb.append("     T1.COURSECD ");
                stb.append("   , T1.MAJORCD ");
                stb.append("   , T1.EXAMCOURSECD ");
                stb.append("   , T1.TESTDIV ");
                stb.append("   , L045.NAMECD2 ");
                stb.append("   , L045.NAME1 ");

                final String sql = stb.toString();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    perfect = Math.max(perfect, rs.getInt("PERFECT"));
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return perfect;
        }

    }
}//クラスの括り
