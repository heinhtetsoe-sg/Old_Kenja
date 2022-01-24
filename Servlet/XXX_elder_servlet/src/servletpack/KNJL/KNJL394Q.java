package servletpack.KNJL;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.text.SimpleDateFormat;
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

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 *
 *    学校教育システム 賢者 [SATシステム] 台帳印刷2
 *
 **/

public class KNJL394Q {

    private static final Log log = LogFactory.getLog(KNJL394Q.class);

    private boolean _hasData;
    private Param _param;

    private static final String FROM_TO_MARK = "\uFF5E";
    private static final String maru1 = "\u2460";
    private static final String maru2 = "\u2461";
    private static final String maru3 = "\u2462";

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
            log.fatal("$Revision: 56848 $ $Date: 2017-10-30 18:35:56 +0900 (月, 30 10 2017) $"); // CVSキーワードの取り扱いに注意
            KNJServletUtils.debugParam(request, log);
            _param = new Param(db2, request);

            _param._countRow = firstRow(getList(db2, sql1(_param)));
            _param._sexAvgMap = groupByField("SEX", getList(db2, sql2(_param)));


            //SVF出力
            for (int i = 0; i < _param._CHECK.length; i++) {
                if (!NumberUtils.isDigits(_param._CHECK[i])) {
                    log.warn(" invalid check:" + _param._CHECK[i]);
                    continue;
                }
                _param._checkInt = Integer.parseInt(_param._CHECK[i]);
                printMain(db2, svf); //帳票出力のメソッド
            }

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        String groupField = null;
        if (1 == _param._checkInt) {
            groupField = "SCHOOLCD";
        }

        final int maxLine = 25;
        final List pageList = getPageList(groupField, getList(db2, sql5(_param)), maxLine);
        final String form = (1 == _param._checkInt) ? "KNJL394Q_2.frm" : "KNJL394Q.frm";

        Map pageMap = new HashMap();
        if ("SCHOOLCD".equals(groupField)) {
            pageMap = getPageMap(pageList, groupField);
        }

        for (int pi = 0; pi < pageList.size(); pi++) {
            final List dataList = (List) pageList.get(pi);

            svf.VrSetForm(form, 4);
            svf.VrsOut("TITLE", "駿台甲府高校実戦模試"); // タイトル
            svf.VrsOut("SUB_TITLE", getString(_param._titleMap, String.valueOf(_param._checkInt))); // 帳票名
            if ("SCHOOLCD".equals(groupField)) {
                final String cd = getString(firstRow(dataList), groupField);
                svf.VrsOut("PAGE1", getString(pageMap, "PAGE1_" + String.valueOf(pi))); // ページ
                svf.VrsOut("PAGE2", getString(pageMap, "PAGE2_" + cd)); // ページ
            } else {
                svf.VrsOut("PAGE1", String.valueOf(pi + 1)); // ページ
                svf.VrsOut("PAGE2", String.valueOf(pageList.size())); // ページ
            }
            svf.VrsOut("DATE", StringUtils.defaultString(StringUtils.replace(getString(_param._countRow, "EXAM_DATE"), "-", "/")) + "実施"); // 日付
            svf.VrsOut("NUM_ALL", getString(_param._countRow, "TOTAL_COUNT")); // 受験者
            svf.VrsOut("NUM_M", getString(_param._countRow, "MALE_COUNT")); // 受験者
            svf.VrsOut("NUM_F", getString(_param._countRow, "FEMALE_COUNT")); // 受験者

            svf.VrsOut("NUM_TITLE1_2", "男子");
            svf.VrsOut("NUM_TITLE1_3", "女子");
            svf.VrsOut("NUM_TITLE2_2", "男子");
            svf.VrsOut("NUM_TITLE2_3", "女子");
            // 中学校訪問、郵送
            final Map row0 = (Map) dataList.get(0);
            if ("SCHOOLCD".equals(groupField)) {
                svf.VrsOut("PLACE", getString(row0, "FINSCHOOL_NAME_ABBV"));
            }

            final int line3Kyoka = 1;
            final int lineEigo = 2;
            final int lineSugaku = 3;
            final int lineKokugo = 4;
            final Map sex0Row = getMappedMap(_param._sexAvgMap, "0");
            svf.VrsOutn("AVE_ALL", line3Kyoka, sishagonyu(getString(sex0Row, "TOTAL_AVG"))); // 平均点
            svf.VrsOutn("AVE_ALL", lineEigo, sishagonyu(getString(sex0Row, "ENGLISH_AVG"))); // 平均点
            svf.VrsOutn("AVE_ALL", lineSugaku, sishagonyu(getString(sex0Row, "MATH_AVG"))); // 平均点
            svf.VrsOutn("AVE_ALL", lineKokugo, sishagonyu(getString(sex0Row, "JAPANESE_AVG"))); // 平均点
            final Map sex1Row = getMappedMap(_param._sexAvgMap, "1");
            final Map sex2row = getMappedMap(_param._sexAvgMap, "2");
            svf.VrsOutn("AVE_M", line3Kyoka, sishagonyu(getString(sex1Row, "TOTAL_AVG"))); // 平均点
            svf.VrsOutn("AVE_F", line3Kyoka, sishagonyu(getString(sex2row, "TOTAL_AVG"))); // 平均点
            svf.VrsOutn("AVE_M", lineEigo, sishagonyu(getString(sex1Row, "ENGLISH_AVG"))); // 平均点
            svf.VrsOutn("AVE_F", lineEigo, sishagonyu(getString(sex2row, "ENGLISH_AVG"))); // 平均点
            svf.VrsOutn("AVE_M", lineSugaku, sishagonyu(getString(sex1Row, "MATH_AVG"))); // 平均点
            svf.VrsOutn("AVE_F", lineSugaku, sishagonyu(getString(sex2row, "MATH_AVG"))); // 平均点
            svf.VrsOutn("AVE_M", lineKokugo, sishagonyu(getString(sex1Row, "JAPANESE_AVG"))); // 平均点
            svf.VrsOutn("AVE_F", lineKokugo, sishagonyu(getString(sex2row, "JAPANESE_AVG"))); // 平均点

            for (int i = 0; i < dataList.size(); i++) {
                final Map row = (Map) dataList.get(i);

                svf.VrsOut("NO", getString(row, "LINE_NO")); // 連番
                svf.VrsOut("EXAM_NO", getString(row, "SAT_NO")); // 受験番号
                svf.VrsOut("NAME", getString(row, "NAME1")); // 氏名
                svf.VrsOut("EXPERIENCE", null); // 体験入学
                if ("0".equals(getString(row, "ABSENCE"))) {
                    svf.VrsOut("TOTAL_SCORE", "欠席"); // 合計点
                } else {
                    svf.VrsOut("TOTAL_SCORE", getString(row, "SCORE_TOTAL")); // 合計点
                }
                svf.VrsOut("DEVI", sishagonyu(getString(row, "DEVIATION"))); // 偏差値
                svf.VrsOut("RANK", getString(row, "ALL_RANK_TOTAL")); // 席次
                svf.VrsOut("SCORE1", getString(row, "SCORE_ENGLISH")); // 得点
                svf.VrsOut("SCORE2", getString(row, "SCORE_MATH")); // 得点
                svf.VrsOut("SCORE3", getString(row, "SCORE_JAPANESE")); // 得点
                svf.VrsOut("PREF_NAME", getString(row, "PREF_NAME")); // 都道府県名
                svf.VrsOut("JUDGE", getString(row, "JUDGE")); // 判定
                final String[] finschoolnameTok = KNJ_EditEdit.get_token(getString(row, "FINSCHOOL_NAME_ABBV"), 30, 2);
                if (null != finschoolnameTok) {
                    for (int j = 0; j < finschoolnameTok.length; j++) {
                        svf.VrsOut("FINSCHOOL_NAME" + String.valueOf(j + 1), finschoolnameTok[j]); // 出身中学名
                    }
                }
                svf.VrsOut("GRADE", getString(row, "GRADE")); // 学年
                svf.VrsOut("SEX", getString(row, "SEX")); // 性別

                if (1 == _param._checkInt) {
                    svf.VrsOut("GROUP_NAME1", getString(row, "GROUPNAME")); // 団体名
                    //svf.VrsOut("GROUP_NAME2", null); // 団体名 リンク

                    svf.VrsOut("HOPE1", getString(row, "HOPE1")); // 志望高校
                    svf.VrsOut("HOPE2", getString(row, "HOPE2")); // 志望高校
                    svf.VrsOut("HOPE3", getString(row, "HOPE3")); // 志望高校

                } else {
                    if (null != getString(row, "HOPE1") && null != getString(row, "HOPE2") && null != getString(row, "HOPE3")) {
                        final String hope23 = addHead(maru2, getString(row, "HOPE2")) + " " + addHead(maru3, getString(row, "HOPE3"));
                        if (getMS932Bytecount(hope23) > 30) {
                            final String hope123 = addHead(maru2, getString(row, "HOPE1")) + " " + addHead(maru2, getString(row, "HOPE2")) + " " + addHead(maru3, getString(row, "HOPE3"));
                            final String[] token = KNJ_EditEdit.get_token(hope123, 30, 2);
                            if (null != token) {
                                for (int j = 0; j < token.length; j++) {
                                    svf.VrsOut("HOPE" + String.valueOf(j + 1), token[j]); // 志望高校
                                }
                            }
                        } else {
                            svf.VrsOut("HOPE1", addHead(maru1, getString(row, "HOPE1"))); // 志望高校
                            svf.VrsOut("HOPE2", hope23); // 志望高校
                        }
                    } else {
                        svf.VrsOut("HOPE1", addHead(maru1, getString(row, "HOPE1"))); // 志望高校
                        svf.VrsOut("HOPE2", addHead(maru2, getString(row, "HOPE2"))); // 志望高校
                    }
                    svf.VrsOut("MOCK_DEVI1", getString(row, "MOCK_AUG_DEV")); // 模試偏差値
                    svf.VrsOut("MOCK_DEVI2", getString(row, "MOCK_SEP_DEV")); // 模試偏差値
                    final String[] groupnameTok = KNJ_EditEdit.get_token(getString(row, "GROUPNAME"), 30, 2);
                    if (null != groupnameTok) {
                        for (int j = 0; j < groupnameTok.length; j++) {
                            svf.VrsOut("GROUP_NAME" + String.valueOf(j + 1), groupnameTok[j]); // 団体名
                        }
                    }
                }
                svf.VrEndRecord();
                _hasData = true;
            }
        }
    }

    private static Map getPageMap(final List pageList, final String groupField) {
        final Map m = new HashMap();
        String oldSchoolcd = null;
        int pageno = 0;
        for (int pi = 0; pi < pageList.size(); pi++) {
            final List rowList = (List) pageList.get(pi);
            final String schoolcd = getString(firstRow(rowList), groupField);
            final boolean isDiffSchool = null != oldSchoolcd && !oldSchoolcd.equals(schoolcd);
            if (isDiffSchool) {
                pageno = 0;
            }
            pageno += 1;
            m.put("PAGE1_" + String.valueOf(pi), String.valueOf(pageno)); // ページ(分子)
            m.put("PAGE2_" + schoolcd, String.valueOf(pageno)); // ページ(分母)
            oldSchoolcd = schoolcd;
        }
        return m;
    }

    private String addHead(final String head, final String s) {
        if (StringUtils.isBlank(s)) {
            return "";
        }
        return head + s;
    }

    private String sishagonyu(final String numString) {
        if (!NumberUtils.isNumber(numString)) {
            return numString;
        }
        return new BigDecimal(numString).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private Map groupByField(final String field, final List rowList) {
        final Map m = new HashMap();
        for (final Iterator it = rowList.iterator(); it.hasNext();) {
            final Map row = (Map) it.next();
            m.put(getString(row, field), row);
        }
        return m;
    }

    private static Map getMappedMap(final Map map, final Object key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new HashMap());
        }
        return (Map) map.get(key1);
    }

    private List getList(final DB2UDB db2, final String sql) {
        final List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            log.info(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                final Map m = new HashMap();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    m.put(meta.getColumnName(i), rs.getString(meta.getColumnName(i)));
                }
                list.add(m);
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;
    }

    private String sql1(final Param param) {
        final StringBuffer stb = new StringBuffer();
        //ヘッダー(実施日)とフッダー(受験者人数(男女))　実施日はこれでしかとってきていないので、中学訪問用・郵送用でもここで実施日を取得するか、実施日だけ別で取得するか。
        stb.append(" SELECT ");
        stb.append("     t1.YEAR, ");
        stb.append("     t3.EXAM_DATE, ");       //試験日
        stb.append("     t1.TOTAL_COUNT, ");     //全体人数
        stb.append("     t2.MALE_COUNT, ");      //男子人数
        stb.append("     t2.FEMALE_COUNT ");     //女子人数
        stb.append(" FROM ");
        stb.append("     (SELECT ");
        stb.append("         YEAR, ");
        stb.append("         TOTAL_COUNT ");
        stb.append("     FROM ");
        stb.append("         SAT_AREA_RECORD_DAT ");
        stb.append("     WHERE ");
        stb.append("         YEAR = '" + _param._CTRL_YEAR + "' AND ");
        stb.append("         AREA = '9' ");
        stb.append("     ) t1 ");
        stb.append("     left join  (SELECT ");
        stb.append("                     YEAR, ");
        stb.append("                     MAX(ALL_MALE) as MALE_COUNT, ");
        stb.append("                     MAX(ALL_FEMALE) as FEMALE_COUNT ");
        stb.append("                 FROM ");
        stb.append("                     SAT_HOPE_DIST_DAT ");
        stb.append("                 WHERE ");
        stb.append("                     YEAR = '" + _param._CTRL_YEAR + "' ");
        stb.append("                 GROUP BY ");
        stb.append("                     YEAR ");
        stb.append("                 ) t2 on t1.YEAR = t2.YEAR ");
        stb.append("     left join SAT_INFO_MST t3 on t1.YEAR = t3.YEAR ");
        return stb.toString();
    }

    private String sql2(final Param param) {
        final StringBuffer stb = new StringBuffer();
        //フッダー　平均点(男女)
        stb.append(" SELECT ");
        stb.append("     a1.YEAR, ");            //
        stb.append("     a1.SEX, ");             //1:男子　2:女子　0:全体
        stb.append("     a1.TOTAL_AVG, ");       //三教科平均点
        stb.append("     a2.ENGLISH_AVG, ");     //英語平均点
        stb.append("     a3.MATH_AVG, ");        //数学平均点
        stb.append("     a4.JAPANESE_AVG ");     //国語平均点
        stb.append(" FROM ");
        stb.append("     (SELECT ");
        stb.append("         t1.YEAR, ");
        stb.append("         t2.SEX, ");
        stb.append("         TRUNC(AVG(FLOAT(t1.SCORE_TOTAL)),3) as TOTAL_AVG ");
        stb.append("     FROM ");
        stb.append("         SAT_EXAM_DAT t1 left join SAT_APP_FORM_MST t2 on t1.YEAR = t2.YEAR and t1.SAT_NO = t2.SAT_NO ");
        stb.append("     WHERE ");
        stb.append("         t1.SCORE_TOTAL IS NOT NULL ");
        stb.append("     AND ");
        stb.append("         t1.YEAR = '" + _param._CTRL_YEAR + "' ");
        stb.append("     GROUP BY ");
        stb.append("         t1.YEAR, ");
        stb.append("         t2.SEX ");
        stb.append("     ) a1 ");
        stb.append("     left join (SELECT ");
        stb.append("                     t1.YEAR, ");
        stb.append("                     t2.SEX, ");
        stb.append("                     TRUNC(AVG(FLOAT(t1.SCORE_ENGLISH)),3) as ENGLISH_AVG ");
        stb.append("                 FROM ");
        stb.append("                     SAT_EXAM_DAT t1 left join SAT_APP_FORM_MST t2 on t1.YEAR = t2.YEAR and t1.SAT_NO = t2.SAT_NO ");
        stb.append("                 WHERE ");
        stb.append("                     t1.SCORE_ENGLISH IS NOT NULL ");
        stb.append("                 AND ");
        stb.append("                     t1.YEAR = '" + _param._CTRL_YEAR + "' ");
        stb.append("                 GROUP BY ");
        stb.append("                     t1.YEAR, ");
        stb.append("                     t2.SEX ");
        stb.append("                 ) a2 on a1.YEAR = a2.YEAR and a1.SEX = a2.SEX ");
        stb.append("     left join (SELECT ");
        stb.append("                     t1.YEAR, ");
        stb.append("                     t2.SEX, ");
        stb.append("                     TRUNC(AVG(FLOAT(t1.SCORE_MATH)),3) as MATH_AVG ");
        stb.append("                 FROM ");
        stb.append("                     SAT_EXAM_DAT t1 left join SAT_APP_FORM_MST t2 on t1.YEAR = t2.YEAR and t1.SAT_NO = t2.SAT_NO ");
        stb.append("                 WHERE ");
        stb.append("                     t1.SCORE_MATH IS NOT NULL ");
        stb.append("                 AND ");
        stb.append("                     t1.YEAR = '" + _param._CTRL_YEAR + "' ");
        stb.append("                 GROUP BY ");
        stb.append("                     t1.YEAR, ");
        stb.append("                     t2.SEX ");
        stb.append("                 ) a3 on a1.YEAR = a3.YEAR and a1.SEX = a3.SEX ");
        stb.append("     left join (SELECT ");
        stb.append("                     t1.YEAR, ");
        stb.append("                     t2.SEX, ");
        stb.append("                     TRUNC(AVG(FLOAT(t1.SCORE_JAPANESE)),3) as JAPANESE_AVG ");
        stb.append("                 FROM ");
        stb.append("                     SAT_EXAM_DAT t1 left join SAT_APP_FORM_MST t2 on t1.YEAR = t2.YEAR and t1.SAT_NO = t2.SAT_NO ");
        stb.append("                 WHERE ");
        stb.append("                     t1.SCORE_JAPANESE IS NOT NULL ");
        stb.append("                 AND ");
        stb.append("                     t1.YEAR = '" + _param._CTRL_YEAR + "' ");
        stb.append("                 GROUP BY ");
        stb.append("                     t1.YEAR, ");
        stb.append("                     t2.SEX ");
        stb.append("                 ) a4 on a1.YEAR = a4.YEAR and a1.SEX = a4.SEX ");
        stb.append("     UNION ");
        stb.append("     (SELECT ");
        stb.append("         YEAR, ");
        stb.append("         '0', ");
        stb.append("         TOTAL_AVG, ");
        stb.append("         ENGLISH_AVG, ");
        stb.append("         MATH_AVG, ");
        stb.append("         JAPANESE_AVG ");
        stb.append("     FROM ");
        stb.append("         SAT_AREA_RECORD_DAT ");
        stb.append("     WHERE ");
        stb.append("         YEAR = '" + _param._CTRL_YEAR + "' AND ");
        stb.append("         AREA = '9' ");
        stb.append("     ) ");
        stb.append(" ORDER BY ");
        stb.append("     SEX ");
        return stb.toString();
    }

    private String sql5(final Param param) {
        //データ
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     t1.YEAR, ");                            //
        stb.append("     t1.SAT_NO, ");                          //受験番号
        stb.append("     VALUE(t1.ABSENCE, '0') AS ABSENCE, ");
        stb.append("     t1.NAME1, ");                           //氏名
        stb.append("     t2.SCORE_TOTAL, ");                     //三教科得点
        stb.append("     t2.DEVIATION, ");                       //三教科偏差値
        stb.append("     t2.ALL_RANK_TOTAL, ");                  //三教科席次
        stb.append("     t2.SCORE_ENGLISH, ");                   //英語得点
        stb.append("     t2.SCORE_MATH, ");                      //数学得点
        stb.append("     t2.SCORE_JAPANESE, ");                  //国語得点
        stb.append("     t1.PREFCD, ");                          //
        stb.append("     t3.PREF_NAME, ");                       //都道府県
        stb.append("     t1.SCHOOLCD, ");                        //
        stb.append("     t4.FINSCHOOL_NAME_ABBV, ");             //出身中学
        stb.append("     t2.JUDGE_SAT, ");                       //
        stb.append("     t5.NAME1 as JUDGE, ");                  //判定
        stb.append("     t1.GRADUATION, ");                      //
        stb.append("     t6.NAME1 as GRADE, ");                  //学年
        stb.append("     t1.SEX as SEXCD, ");                    //
        stb.append("     t7.NAME2 as SEX, ");                    //性別
        stb.append("     t1.HOPECD1, ");                         //
        stb.append("     t8.FINSCHOOL_NAME_ABBV as HOPE1, ");    //志望高校1
        stb.append("     t1.HOPECD2, ");                         //
        stb.append("     t9.FINSCHOOL_NAME_ABBV as HOPE2, ");    //志望高校2
        stb.append("     t1.HOPECD3, ");                         //
        stb.append("     t10.FINSCHOOL_NAME_ABBV as HOPE3, ");   //志望高校3
        stb.append("     t1.MOCK_AUG_DEV, ");                    //進研8月3科偏差
        stb.append("     t1.MOCK_SEP_DEV, ");                    //進研9月3科偏差
        stb.append("     t1.GROUPCD, ");                         //
        stb.append("     t11.GROUPNAME ");                       //団体名
        stb.append(" FROM ");
        stb.append("     SAT_APP_FORM_MST t1 ");
        stb.append("     left join SAT_EXAM_DAT t2 on t1.YEAR = t2.YEAR and t1.SAT_NO = t2.SAT_NO ");
        stb.append("     left join PREF_MST t3 on t1.PREFCD = t3.PREF_CD ");
        stb.append("     left join FINSCHOOL_MST t4 on t1.SCHOOLCD = t4.FINSCHOOLCD and t4.FINSCHOOL_TYPE = '3' ");
        stb.append("     left join NAME_MST t5 on t2.JUDGE_SAT = t5.NAMECD2 and t5.NAMECD1 = 'L200' ");
        stb.append("     left join NAME_MST t6 on t1.GRADUATION = t6.NAMECD2 and t6.NAMECD1 = 'L205' ");
        stb.append("     left join NAME_MST t7 on t1.SEX = t7.NAMECD2 and t7.NAMECD1 = 'Z002' ");
        stb.append("     left join FINSCHOOL_MST t8 on RIGHT('00000' || t1.HOPECD1, 5) = RIGHT(t8.FINSCHOOLCD, 5) and t8.FINSCHOOL_TYPE = '4' ");
        stb.append("     left join FINSCHOOL_MST t9 on RIGHT('00000' || t1.HOPECD2, 5) = RIGHT(t9.FINSCHOOLCD, 5) and t9.FINSCHOOL_TYPE = '4' ");
        stb.append("     left join FINSCHOOL_MST t10 on RIGHT('00000' || t1.HOPECD3, 5) = RIGHT(t10.FINSCHOOLCD, 5) and t10.FINSCHOOL_TYPE = '4' ");
        stb.append("     left join SAT_GROUP_DAT t11 on t1.GROUPCD = t11.GROUPCD and t1.YEAR = t11.YEAR ");
        stb.append(" WHERE ");
        stb.append("     t1.YEAR = '" + _param._CTRL_YEAR + "' ");
        if ("2".equals(_param._CHOICE)) {
            /*受験番号指定*/
            stb.append("     AND ");
            if (!StringUtils.isBlank(_param._EXAM_FROM) && !StringUtils.isBlank(_param._EXAM_TO)) {
                stb.append("         t1.SAT_NO BETWEEN '" + _param._EXAM_FROM + "' AND '" + _param._EXAM_TO + "' ");
            } else {
                stb.append("         t1.SAT_NO = '" + _param._EXAM_FROM + "' ");
            }
        }

        if (param._checkInt == 0) {
            /*甲府市内*/
            stb.append(" AND ");
            stb.append("     t1.ADDR1 LIKE '%甲府市%' ");
        } else if (param._checkInt == 1) {
            /*甲府市内中学校*/
            stb.append(" AND ");
            stb.append("     t4.FINSCHOOL_PREF_CD = '19' ");
            stb.append(" AND ");
            stb.append("     t4.DISTRICTCD = '01' ");
        } else if (param._checkInt == 2) {
            /*県内*/
            stb.append(" AND ");
            stb.append("     t1.INOUT_KUBUN = '1' ");
        } else if (param._checkInt == 3) {
            /*県内*/
            stb.append(" AND ");
            stb.append("     t1.INOUT_KUBUN = '1' ");
            /*男子*/
            stb.append(" AND ");
            stb.append("     t1.SEX = '1' ");
        } else if (param._checkInt == 4) {
            /*県外*/
            stb.append(" AND ");
            stb.append("     t1.INOUT_KUBUN IN ('2','3') ");     //県内扱と県外
        } else if (param._checkInt == 5) {
            /*県外*/
            stb.append(" AND ");
            stb.append("     t1.INOUT_KUBUN IN ('2','3') ");     //県内扱と県外
            /*男子*/
            stb.append(" AND ");
            stb.append("     t1.SEX = '1' ");
        } else if (param._checkInt == 6) {
            /*海外*/
            stb.append(" AND ");
            stb.append("     t1.INOUT_KUBUN = '4' ");
            /*男子*/
            stb.append(" AND ");
            stb.append("     t1.SEX = '1' ");
        } else if (param._checkInt == 7) {
            /*県外海外*/
            stb.append(" AND ");
            stb.append("     t1.INOUT_KUBUN IN ('2','3','4') ");
            /*女子*/
            stb.append(" AND ");
            stb.append("     t1.SEX = '2' ");
        } else if (param._checkInt == 8 || param._checkInt == 9) {
            /*校内生*/
            stb.append(" AND ");
            stb.append("     t1.IND_KUBUN = '3' ");
        }

        stb.append(" ORDER BY ");
        if (param._checkInt == 1) {
            /*学校別*/
            stb.append("     t1.SCHOOLCD, ");
            /*高得点順*/
            stb.append("     VALUE(t1.ABSENCE, '0') DESC, ");
            stb.append("     t2.ALL_RANK_TOTAL, ");
            stb.append("     t1.SAT_NO ");
        } else if (param._checkInt == 9) {
            /*受験番号順*/
            stb.append("     t1.SAT_NO ");
        } else {
            /*高得点順*/
            stb.append("     VALUE(t1.ABSENCE, '0') DESC, ");
            stb.append("     t2.ALL_RANK_TOTAL, ");
            stb.append("     t1.SAT_NO ");
        }
        return stb.toString();
    }

    private static Map firstRow(final List list) {
        if (null == list || list.isEmpty()) {
            return new HashMap();
        }
        return (Map) list.get(0);
    }

    private static String getString(final Map m, final String field) {
        if (null == m || m.isEmpty()) {
            return null;
        }
        if (!m.containsKey(field)) {
            throw new IllegalArgumentException("not defined: " + field + " in " + m.keySet());
        }
        return (String) m.get(field);
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

    private static String formatDate(final String date) {
        if (null == date) {
            return "";
        }
        final String nen = new SimpleDateFormat("yyyy年").format(Date.valueOf(date));
        return nen + KNJ_EditDate.h_format_JP_MD(date);
    }

    /**
     * listを最大数ごとにグループ化したリストを得る
     * @param list
     * @param max 最大数
     * @return listを最大数ごとにグループ化したリスト
     */
    private static List getPageList(final String groupField, final List list, final int max) {
        final List rtn = new ArrayList();
        List current = null;
        String oldGroupVal = null;
        int lineno = 0;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Map row = (Map) it.next();
            final boolean isDiffGroup = null != groupField && (null == oldGroupVal && null != getString(row, groupField) || null != oldGroupVal && !oldGroupVal.equals(getString(row, groupField)));
            if (null == current || current.size() >= max || isDiffGroup) {
                current = new ArrayList();
                rtn.add(current);
            }
            if (isDiffGroup) {
                lineno = 0;
            }
            lineno += 1;
            row.put("LINE_NO", String.valueOf(lineno));
            current.add(row);
            if (null != groupField) {
                oldGroupVal = getString(row, groupField);
            }
        }
        return rtn;
    }

    private static class Param {
        final String _CTRL_YEAR;
//        final String _CTRL_SEMESTER;
//        final String _CTRL_DATE;

        /**
         * 0:  地域別    甲府市内高得点順<br>
         * 1:  地域別    甲府市内中学校別高得点順br>
         * 2:  地域別    県内高得点順 <br>
         * 3:  地域別    県内男子高得点順<br>
         * 4:  地域別    県外高得点順<br>
         * 5:  地域別    県外男子高得点順<br>
         * 6:  地域別    海外男子高得点順 <br>
         * 7:  地域別    県外海外女子高得点順 <br>
         * 8:  校内生    校内生高得点順<br>
         * 9:  校内生    校内生受験番号順<br>
         */
        final String[] _CHECK;
//        final String _CHECK_CNT;
        final String _CHOICE; // 1:すべて 2:受験番号
        final String _EXAM_FROM; // 受験番号開始
        final String _EXAM_TO; // 受験番号終了
        final String _PRGID;
        final String _cmd;
        int _checkInt;
        final Map _titleMap;

        Map _countRow;
        Map _sexAvgMap;

        private Param(final DB2UDB db2, final HttpServletRequest request) {
            _CTRL_YEAR = request.getParameter("CTRL_YEAR");
//            _CTRL_SEMESTER = request.getParameter("CTRL_SEMESTER");
//            _CTRL_DATE = request.getParameter("CTRL_DATE");
            _CHECK = request.getParameterValues("CHECK[]");
//            _CHECK_CNT = request.getParameter("CHECK_CNT");
            _CHOICE = request.getParameter("CHOICE");
            _EXAM_FROM = request.getParameter("EXAM_FROM");
            _EXAM_TO = request.getParameter("EXAM_TO");
            _PRGID = request.getParameter("PRGID");
            _cmd = request.getParameter("cmd");

            _titleMap = new HashMap();
            _titleMap.put("0", "(地域別)甲府市内高得点順");
            _titleMap.put("1", "(地域別)甲府市内中学校別高得点順");
            _titleMap.put("2", "(地域別)県内高得点順");
            _titleMap.put("3", "(地域別)県内男子高得点順");
            _titleMap.put("4", "(地域別)県外高得点順");
            _titleMap.put("5", "(地域別)県外男子高得点順");
            _titleMap.put("6", "(地域別)海外男子高得点順");
            _titleMap.put("7", "(地域別)県外海外女子高得点順");
            _titleMap.put("8", "(校内生)校内生高得点順");
            _titleMap.put("9", "(校内生)校内生受験番号順");
        }
    }
}//クラスの括り
