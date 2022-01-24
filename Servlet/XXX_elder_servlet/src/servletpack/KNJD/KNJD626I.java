/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 *
 * 作成日: 2021/03/17
 * 作成者: ishimine
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJD626I {

    private static final Log log = LogFactory.getLog(KNJD626I.class);

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

    //評定分布表
    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final Map printMap = getPrintMap(db2);

        if (printMap.isEmpty()) return;

        //教科毎のループ
        for (Iterator iterator = printMap.values().iterator(); iterator.hasNext();) {
            final PrintData printData = (PrintData) iterator.next();

            svf.VrSetForm("KNJD626I.frm", 4);
            printTitle(svf, printData);

            int grpCd = 0; //男女別 グループコード
            int cnt = 0; //繰返し回数 グループコード

            //分布毎のループ
            for (Iterator ite = printData.bunpuMap.keySet().iterator(); ite.hasNext();) {
                final String bunpuKey = (String) ite.next();
                final Bunpu bunpu = (Bunpu) printData.bunpuMap.get(bunpuKey);

                //男女別帳票なら男女2レコードで1つのグループ
                if ("1".equals(_param._sex)) {
                    if ("1".equals(bunpu._sex)) {
                        grpCd++;
                    }
                    svf.VrsOut("GRPCD1", String.valueOf(grpCd));
                    svf.VrsOut("GRPCD2", String.valueOf(grpCd));
                    svf.VrsOut("GRPCD3", String.valueOf(grpCd));
                } else {
                    svf.VrsOut("GRPCD1", String.valueOf(cnt));
                    svf.VrsOut("GRPCD2", String.valueOf(cnt));
                    svf.VrsOut("GRPCD3", String.valueOf(cnt));
                }

                //男女別で女子の時以外は印字
                if (!"1".equals(_param._sex) ||  "1".equals(_param._sex) && "1".equals(bunpu._sex)) {
                    //科目名
                    final int subclassKeta = KNJ_EditEdit.getMS932ByteLength(bunpu._subclassname);
                    final String subclassField = subclassKeta <= 14 ? "1" : subclassKeta <= 20 ? "2" : "3";
                    svf.VrsOut("SUBCLASS_NAME" + subclassField, bunpu._subclassname);

                    //担当者
                    final int staffKeta = KNJ_EditEdit.getMS932ByteLength(bunpu._staffname);
                    final String staffField = staffKeta <= 14 ? "1" : staffKeta <= 20 ? "2" : "3";
                    svf.VrsOut("TR_NAME" + staffField, bunpu._staffname);

                    //担当クラス
                    if ("-".equals(bunpu._tantou)) {
                        svf.VrsOut("TR_HR_CENTER", bunpu._tantou);
                    } else {
                        final List item = KNJ_EditKinsoku.getTokenList(bunpu._tantou, 18, 3);
                        for (int idx = 0; idx < item.size(); idx++) {
                            svf.VrsOut("TR_HR" + (idx + 1), item.get(idx).toString());
                        }
                    }
                } else { //女子の時、担当クラスが4行以上あれば印字
                    final List item = KNJ_EditKinsoku.getTokenList(bunpu._tantou, 18, 6);
                    for (int idx = 3; idx < item.size(); idx++) {
                        svf.VrsOut("TR_HR" + (idx - 2), item.get(idx).toString());
                    }
                }

                //評定分布
                if ("1".equals(_param._sex)) {
                    if ("1".equals(bunpu._sex)){
                        svf.VrsOut("NUM_NAME", "男子人数");
                    } else {
                        svf.VrsOut("NUM_NAME", "女子人数");
                    }
                } else {
                    svf.VrsOut("NUM_NAME", "人数");
                }

                //集計がなければレコード埋め
                if (bunpu._cnt_01 == null) {
                    printUme(svf);
                    svf.VrEndRecord();
                    _hasData = true;
                    cnt++;
                    continue;
                }

                //評定人数
                svf.VrsOut("NUM1",  bunpu._cnt_10);
                svf.VrsOut("NUM2",  bunpu._cnt_09);
                svf.VrsOut("NUM3",  bunpu._cnt_08);
                svf.VrsOut("NUM4",  bunpu._cnt_07);
                svf.VrsOut("NUM5",  bunpu._cnt_06);
                svf.VrsOut("NUM6",  bunpu._cnt_05);
                svf.VrsOut("NUM7",  bunpu._cnt_04);
                svf.VrsOut("NUM8",  bunpu._cnt_03);
                svf.VrsOut("NUM9",  bunpu._cnt_02);
                svf.VrsOut("NUM10", bunpu._cnt_01);

                //人数割合
                final String per10 = calcPer(bunpu._cnt_10, bunpu._cnt_Score);
                final String per09 = calcPer(bunpu._cnt_09, bunpu._cnt_Score);
                final String per08 = calcPer(bunpu._cnt_08, bunpu._cnt_Score);
                final String per07 = calcPer(bunpu._cnt_07, bunpu._cnt_Score);
                final String per06 = calcPer(bunpu._cnt_06, bunpu._cnt_Score);
                final String per05 = calcPer(bunpu._cnt_05, bunpu._cnt_Score);
                final String per04 = calcPer(bunpu._cnt_04, bunpu._cnt_Score);
                final String per03 = calcPer(bunpu._cnt_03, bunpu._cnt_Score);
                final String per02 = calcPer(bunpu._cnt_02, bunpu._cnt_Score);
                final String per01 = calcPer(bunpu._cnt_01, bunpu._cnt_Score);
                svf.VrsOut("PER1", per10 + "%");
                svf.VrsOut("PER2", per09 + "%");
                svf.VrsOut("PER3", per08 + "%");
                svf.VrsOut("PER4", per07 + "%");
                svf.VrsOut("PER5", per06 + "%");
                svf.VrsOut("PER6", per05 + "%");
                svf.VrsOut("PER7", per04 + "%");
                svf.VrsOut("PER8", per03 + "%");
                svf.VrsOut("PER9", per02 + "%");
                svf.VrsOut("PER10", per01 + "%");

                //人数割合合計
                svf.VrsOut("TOTAL_PER1", totalPer(per10, per09) + "%");
                svf.VrsOut("TOTAL_PER2", totalPer(per08, per07) + "%");
                svf.VrsOut("TOTAL_PER3", totalPer(per06, per05) + "%");
                svf.VrsOut("TOTAL_PER4", totalPer(per04, per03) + "%");
                svf.VrsOut("TOTAL_PER5", totalPer(per02, per01) + "%");

                //小計
                svf.VrsOut("SUBTOTAL", bunpu._cnt_Score);

                //平均
                if ("0".equals(bunpu._cnt_Score)) {
                    svf.VrsOut("AVE", "0.00");
                } else {
                    BigDecimal add1 = new BigDecimal(0);
                    add1 = add1.add(new BigDecimal(multiplyHyoutei("10", bunpu._cnt_10)));
                    add1 = add1.add(new BigDecimal(multiplyHyoutei("9", bunpu._cnt_09)));
                    add1 = add1.add(new BigDecimal(multiplyHyoutei("8", bunpu._cnt_08)));
                    add1 = add1.add(new BigDecimal(multiplyHyoutei("7", bunpu._cnt_07)));
                    add1 = add1.add(new BigDecimal(multiplyHyoutei("6", bunpu._cnt_06)));
                    add1 = add1.add(new BigDecimal(multiplyHyoutei("5", bunpu._cnt_05)));
                    add1 = add1.add(new BigDecimal(multiplyHyoutei("4", bunpu._cnt_04)));
                    add1 = add1.add(new BigDecimal(multiplyHyoutei("3", bunpu._cnt_03)));
                    add1 = add1.add(new BigDecimal(multiplyHyoutei("2", bunpu._cnt_02)));
                    add1 = add1.add(new BigDecimal(multiplyHyoutei("1", bunpu._cnt_01)));
                    svf.VrsOut("AVE", add1.divide(new BigDecimal(bunpu._cnt_Score), 2, BigDecimal.ROUND_HALF_UP).toString());
                }

                //0orX
                svf.VrsOut("TRANSFER1", bunpu._cnt_Null);

                //第1種
                svf.VrsOut("TRANSFER2", bunpu._cnt_Trans);

                //合計人数
                svf.VrsOut("TOTAL", String.valueOf(Integer.valueOf(bunpu._cnt_Score) + Integer.valueOf(bunpu._cnt_Null) + Integer.valueOf(bunpu._cnt_Trans)));

                //*人数
                svf.VrsOut("SICK", bunpu._cnt_Over);

                svf.VrEndRecord();
                _hasData = true;
                cnt++;
            }
            svf.VrEndPage();
        }
    }

    //各評定人数÷小計人数　小数点第2位四捨五入
    private String calcPer(final String num, final String subTotal) {
        if ("0".equals(num)) {
            return "0.0";
        } else {
            BigDecimal b1 = new BigDecimal(num);
            BigDecimal b2 = new BigDecimal(subTotal);
            BigDecimal b3 = b1.divide(b2, 3, BigDecimal.ROUND_HALF_UP);
            return b3.multiply(BigDecimal.valueOf(100)).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
        }
    }

    //人数割合の合計
    private String totalPer(final String per1, final String per2) {
        BigDecimal b1 = new BigDecimal(per1);
        BigDecimal b2 = new BigDecimal(per2);
        return b1.add(b2).toString();
    }

    //各評定＊各評定人数
    private String multiplyHyoutei(final String hyoutei, final String num) {
        final int wk1 = Integer.parseInt(hyoutei);
        final int wk2 = Integer.parseInt(num);
        return String.valueOf(wk1 * wk2);
    }

    private void printTitle(final Vrw32alp svf, final PrintData printData) {
        svf.VrsOut("NENDO", _param._nendo); //年度

        final String title;
        if ("1".equals(_param._disp)) {
            if (_param._allClass) {
                title = "教科別";
            } else {
                title = "科目別";
            }
        } else {
            if ("1".equals(_param._tantou)) {
                title = "担当者別";
            } else {
                title = "科目別";
            }
        }

        svf.VrsOut("TITLE", _param._nendo + " " + _param._semeName + " " + _param._gradeName +  "　評定分布表　(" + title + ")"); //タイトル
        svf.VrsOut("DATE", "出力日 " + _param._outputDate); //日付
        svf.VrsOut("CLASS_NAME", printData._className); //教科名
    }

    //男子または女子が1人もいない場合のレコード埋め
    private void printUme(final Vrw32alp svf) {
        svf.VrsOut("NUM1",  "0");
        svf.VrsOut("NUM2",  "0");
        svf.VrsOut("NUM3",  "0");
        svf.VrsOut("NUM4",  "0");
        svf.VrsOut("NUM5",  "0");
        svf.VrsOut("NUM6",  "0");
        svf.VrsOut("NUM7",  "0");
        svf.VrsOut("NUM8",  "0");
        svf.VrsOut("NUM9",  "0");
        svf.VrsOut("NUM10", "0");
        svf.VrsOut("PER1", "0.0%");
        svf.VrsOut("PER2", "0.0%");
        svf.VrsOut("PER3", "0.0%");
        svf.VrsOut("PER4", "0.0%");
        svf.VrsOut("PER5", "0.0%");
        svf.VrsOut("PER6", "0.0%");
        svf.VrsOut("PER7", "0.0%");
        svf.VrsOut("PER8", "0.0%");
        svf.VrsOut("PER9", "0.0%");
        svf.VrsOut("PER10", "0.0%");
        svf.VrsOut("TOTAL_PER1", "0.0%");
        svf.VrsOut("TOTAL_PER2", "0.0%");
        svf.VrsOut("TOTAL_PER3", "0.0%");
        svf.VrsOut("TOTAL_PER4", "0.0%");
        svf.VrsOut("TOTAL_PER5", "0.0%");
        svf.VrsOut("SUBTOTAL", "0");
        svf.VrsOut("AVE", "0.00");
        svf.VrsOut("TRANSFER1", "0");
        svf.VrsOut("TRANSFER2", "0");
        svf.VrsOut("TOTAL", "0");
        svf.VrsOut("SICK", "0");
    }

    private Map getPrintMap(final DB2UDB db2) {
        final Map<String, PrintData> retMap = new LinkedHashMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = getHyouteiBunpuSql();
        log.debug(" sql =" + sql);
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String classCd = rs.getString("CLASSCD");
                final String schoolKind = rs.getString("SCHOOL_KIND");
                final String curriculumCd = rs.getString("CURRICULUM_CD");
                final String subclassCd = rs.getString("SUBCLASSCD");
                final String className = rs.getString("CLASSNAME");
                final String subclassName = rs.getString("SUBCLASSNAME");
                final String staffCd = rs.getString("STAFFCD");
                final String staffName = rs.getString("STAFFNAME");
                final String tantou = rs.getString("TANTOU");
                final String sex = rs.getString("SEX");
                final String cnt_10 = rs.getString("CNT_10");
                final String cnt_09 = rs.getString("CNT_09");
                final String cnt_08 = rs.getString("CNT_08");
                final String cnt_07 = rs.getString("CNT_07");
                final String cnt_06 = rs.getString("CNT_06");
                final String cnt_05 = rs.getString("CNT_05");
                final String cnt_04 = rs.getString("CNT_04");
                final String cnt_03 = rs.getString("CNT_03");
                final String cnt_02 = rs.getString("CNT_02");
                final String cnt_01 = rs.getString("CNT_01");
                final String cnt_Null = rs.getString("CNT_NULL");
                final String cnt_Trans = rs.getString("CNT_TRANS");
                final String cnt_Score = rs.getString("CNT_SCORE");
                final String cnt_Over = rs.getString("CNT_OVER");

                final String classKey = classCd + schoolKind;

                if (!retMap.containsKey(classKey)) {
                    final PrintData printData = new PrintData(classCd, className);
                    retMap.put(classKey, printData);
                }

                final PrintData printData = retMap.get(classKey);

                final String bunpuKey = classKey +  curriculumCd + subclassCd + "-" + staffCd + ("1".equals(_param._sex) ? "_" + sex : "");

                if (!printData.bunpuMap.containsKey(bunpuKey)) {
                    final Bunpu bunpu = new Bunpu(classCd, schoolKind, curriculumCd, subclassCd, className,
                            subclassName, staffName, tantou, cnt_10, cnt_09, cnt_08, cnt_07, cnt_06, cnt_05, cnt_04,
                            cnt_03, cnt_02, cnt_01, cnt_Null, cnt_Trans, cnt_Score, cnt_Over, sex);
                    printData.bunpuMap.put(bunpuKey, bunpu);
                }
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }

    private String getHyouteiBunpuSql() {
        final StringBuffer stb = new StringBuffer();

        //講座取得
        stb.append(" WITH CHAIR_T AS ( ");
        stb.append(" SELECT DISTINCT ");
        stb.append("     CHAIR.YEAR, ");
        stb.append("     CHAIR.SEMESTER, ");
        stb.append("     CHAIR.CHAIRCD, ");
        stb.append("     CHAIR.GROUPCD, ");
        stb.append("     CHAIR.CHAIRABBV, ");
        stb.append("     CHAIR.CLASSCD, ");
        stb.append("     CHAIR.SCHOOL_KIND, ");
        stb.append("     CHAIR.CURRICULUM_CD, ");
        stb.append("     CHAIR.SUBCLASSCD ");
        stb.append(" FROM ");
        stb.append("     CHAIR_DAT CHAIR ");
        stb.append(" WHERE ");
        stb.append("     CHAIR.YEAR = '" + _param._loginYear + "' AND ");
        stb.append("     CHAIR.SEMESTER = '" + _param._semester + "' AND ");
        if ("1".equals(_param._disp)) {
            stb.append("     CHAIR.CLASSCD || CHAIR.SCHOOL_KIND IN " + SQLUtils.whereIn(true, _param._categorySelected));
        } else {
            stb.append("     CHAIR.CLASSCD || CHAIR.SCHOOL_KIND || CHAIR.CURRICULUM_CD || CHAIR.SUBCLASSCD IN " + SQLUtils.whereIn(true, _param._categorySelected));
        }
        //担当者取得
        if ("1".equals(_param._tantou)) {
            stb.append(" ), STAFF_T AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     CSTF.STAFFCD, ");
            stb.append("     STFM.STAFFNAME, ");
            stb.append("     MIN(T1.CHAIRCD) AS CHAIR_MIN, "); //最小講座 ソート用
            stb.append("     LISTAGG(T1.CHAIRABBV || CCLS.TRGTGRADE || '-' || CCLS.TRGTCLASS, ',') AS TANTOU ");
            stb.append(" FROM ");
            stb.append("     CHAIR_T T1 ");
            stb.append("     INNER JOIN ");
            stb.append("         CHAIR_STF_DAT CSTF ");
            stb.append("          ON CSTF.YEAR      = T1.YEAR ");
            stb.append("         AND CSTF.SEMESTER  = T1.SEMESTER ");
            stb.append("         AND CSTF.CHAIRCD   = T1.CHAIRCD ");
            stb.append("         AND CSTF.CHARGEDIV = '1' ");
            stb.append("     INNER JOIN ");
            stb.append("         STAFF_MST STFM ");
            stb.append("          ON STFM.STAFFCD = CSTF.STAFFCD ");
            stb.append("     INNER JOIN ");
            stb.append("         CHAIR_CLS_DAT CCLS ");
            stb.append("          ON CCLS.YEAR      = T1.YEAR ");
            stb.append("         AND CCLS.SEMESTER  = T1.SEMESTER ");
            stb.append("         AND CCLS.CHAIRCD   = T1.CHAIRCD ");
            stb.append("         AND CCLS.GROUPCD   = T1.GROUPCD ");
            stb.append("         AND CCLS.TRGTGRADE = '" + _param._grade + "' ");
            stb.append(" GROUP BY ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     CSTF.STAFFCD, ");
            stb.append("     STFM.STAFFNAME ");
        }
        //スモールクラス取得
        if ("1".equals(_param._sClass)) {
            stb.append(" ), SCLASS_T AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     LISTAGG(CD004.REMARK2, ',') AS TANTOU ");
            stb.append(" FROM ");
            stb.append("     CHAIR_T T1       ");
            stb.append("     INNER JOIN ");
            stb.append("     CHAIR_DETAIL_DAT CD004 ");
            stb.append("      ON CD004.YEAR = T1.YEAR ");
            stb.append("     AND CD004.SEMESTER  = T1.SEMESTER ");
            stb.append("     AND CD004.CHAIRCD   = T1.CHAIRCD ");
            stb.append("     AND CD004.SEQ = '004' ");
            stb.append(" GROUP BY ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     T1.SUBCLASSCD   ");
        }
        //留学者取得
        stb.append(" ), TRANS_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     SCHREGNO, ");
        stb.append("     MAX(TRANSFERCD) AS TRANSFERCD ");
        stb.append(" FROM ");
        stb.append("     SCHREG_TRANSFER_DAT ");
        stb.append(" WHERE ");
        stb.append("     TRANSFERCD = '1' AND ");
        stb.append("         ((TRANSFER_SDATE BETWEEN '" + _param._semeSdate + "' AND '" + _param._semeEdate + "') ");
        stb.append("         OR  (TRANSFER_EDATE BETWEEN '" + _param._semeSdate + "' AND '" + _param._semeEdate + "') ");
        stb.append("         OR  (TRANSFER_SDATE <= '" + _param._semeSdate + "' AND TRANSFER_EDATE >= '" + _param._semeEdate + "')) ");
        stb.append(" GROUP BY ");
        stb.append("     SCHREGNO ");
        //対象生徒取得
        stb.append(" ), SCHREGNO_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     T1.CHAIRCD, ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     T1.CURRICULUM_CD, ");
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.SCHREGNO, ");
        stb.append("     BASE.SEX, ");
        stb.append("     CSTF.STAFFCD, ");
        stb.append("     TRANS.TRANSFERCD ");
        stb.append(" FROM ");
        stb.append("     CHAIR_T T1 ");
        stb.append("     INNER JOIN ");
        stb.append("         CHAIR_STD_DAT CSTD ");
        stb.append("          ON CSTD.YEAR     = T1.YEAR ");
        stb.append("         AND CSTD.SEMESTER = T1.SEMESTER ");
        stb.append("         AND CSTD.CHAIRCD  = T1.CHAIRCD ");
        stb.append("     INNER JOIN ");
        stb.append("         CHAIR_STF_DAT CSTF ");
        stb.append("          ON CSTF.YEAR      = T1.YEAR ");
        stb.append("         AND CSTF.SEMESTER  = T1.SEMESTER ");
        stb.append("         AND CSTF.CHAIRCD   = T1.CHAIRCD ");
        stb.append("         AND CSTF.CHARGEDIV = '1' ");
        stb.append("     INNER JOIN ");
        stb.append("         SCHREG_REGD_DAT REGD ");
        stb.append("          ON REGD.YEAR     = CSTD.YEAR ");
        stb.append("         AND REGD.SEMESTER = CSTD.SEMESTER ");
        stb.append("         AND REGD.GRADE    = '" + _param._grade + "' ");
        stb.append("         AND REGD.SCHREGNO = CSTD.SCHREGNO ");
        stb.append("     INNER JOIN ");
        stb.append("         SCHREG_BASE_MST BASE ");
        stb.append("          ON BASE.SCHREGNO = CSTD.SCHREGNO ");
        stb.append("     LEFT JOIN ");
        stb.append("         TRANS_T TRANS ");
        stb.append("          ON TRANS.SCHREGNO = REGD.SCHREGNO ");
        //出欠取得
        stb.append(" ), ATTEND_SUBCLASS AS ( ");
        stb.append(" SELECT ");
        stb.append("     SCH.YEAR, ");
        stb.append("     SCH.SEMESTER, ");
        stb.append("     SCH.CHAIRCD, ");
        stb.append("     SCH.SCHREGNO, ");
        stb.append("     SUM(VALUE(ATTEND.LESSON, 0)) - SUM(VALUE(ATTEND.OFFDAYS, 0)) - SUM(VALUE(ATTEND.ABROAD, 0)) AS LESSON, ");
        stb.append("     SUM(VALUE(ATTEND.SICK, 0)) + SUM(VALUE(ATTEND.NOTICE, 0)) + SUM(VALUE(ATTEND.NONOTICE, 0)) AS SICK ");
        stb.append(" FROM ");
        stb.append("     ATTEND_SUBCLASS_DAT ATTEND ");
        stb.append("     INNER JOIN ");
        stb.append("         SCHREGNO_T SCH ");
        stb.append("          ON SCH.YEAR = ATTEND.YEAR ");
        stb.append("         AND SCH.SEMESTER = ATTEND.SEMESTER ");
        stb.append("         AND SCH.CLASSCD = ATTEND.CLASSCD ");
        stb.append("         AND SCH.SCHOOL_KIND = ATTEND.SCHOOL_KIND ");
        stb.append("         AND SCH.CURRICULUM_CD = ATTEND.CURRICULUM_CD ");
        stb.append("         AND SCH.SUBCLASSCD = ATTEND.SUBCLASSCD ");
        stb.append("         AND SCH.SCHREGNO = ATTEND.SCHREGNO ");
        stb.append(" GROUP BY ");
        stb.append("     SCH.YEAR, ");
        stb.append("     SCH.SEMESTER, ");
        stb.append("     SCH.CHAIRCD, ");
        stb.append("     SCH.SCHREGNO ");
        //集計前評定
        stb.append(" ), HYOUTEI_BASE AS ( ");
        stb.append(" SELECT ");
        stb.append("     T2.YEAR, ");
        stb.append("     T2.SEMESTER, ");
        stb.append("     T2.CHAIRCD, ");
        stb.append("     T2.STAFFCD, ");
        stb.append("     T2.CLASSCD, ");
        stb.append("     T2.SCHOOL_KIND, ");
        stb.append("     T2.CURRICULUM_CD, ");
        stb.append("     T2.SUBCLASSCD, ");
        stb.append("     T2.SCHREGNO, ");
        stb.append("     T2.SEX, ");
        stb.append("     T2.TRANSFERCD, ");
        stb.append("     ATTEND.LESSON, ");
        stb.append("     ATTEND.SICK, ");
        stb.append("     RSCORE.SCORE, ");
        stb.append("     FLOAT(ATTEND.LESSON / 3.0) AS BORDER, ");
        stb.append("     CASE WHEN FLOAT(ATTEND.LESSON / 3.0) <= ATTEND.SICK THEN 1 ELSE 0 END AS OVER ");
        stb.append(" FROM ");
        stb.append("     SCHREGNO_T T2 ");
        stb.append("     INNER JOIN ");
        stb.append("         RECORD_SCORE_DAT RSCORE ");
        stb.append("          ON RSCORE.YEAR = T2.YEAR ");
        stb.append("         AND RSCORE.SEMESTER = T2.SEMESTER ");
        stb.append("         AND RSCORE.TESTKINDCD || RSCORE.TESTITEMCD || RSCORE.SCORE_DIV = '990008' ");
        stb.append("         AND RSCORE.CLASSCD = T2.CLASSCD ");
        stb.append("         AND RSCORE.SCHOOL_KIND = T2.SCHOOL_KIND ");
        stb.append("         AND RSCORE.CURRICULUM_CD = T2.CURRICULUM_CD ");
        stb.append("         AND RSCORE.SUBCLASSCD = T2.SUBCLASSCD ");
        stb.append("         AND RSCORE.SCHREGNO = T2.SCHREGNO ");
        stb.append("     LEFT JOIN ");
        stb.append("         ATTEND_SUBCLASS ATTEND ");
        stb.append("          ON ATTEND.YEAR = T2.YEAR ");
        stb.append("         AND ATTEND.SEMESTER = T2.SEMESTER ");
        stb.append("         AND ATTEND.CHAIRCD = T2.CHAIRCD ");
        stb.append("         AND ATTEND.SCHREGNO = T2.SCHREGNO ");
        //集計後評定
        stb.append(" ), RESULT AS ( ");
        stb.append(" SELECT ");
        stb.append("     T3.YEAR, ");
        stb.append("     T3.SEMESTER, ");
        stb.append("     T3.CLASSCD, ");
        stb.append("     T3.SCHOOL_KIND, ");
        stb.append("     T3.CURRICULUM_CD, ");
        stb.append("     T3.SUBCLASSCD, ");
        if ("1".equals(_param._tantou)) {
            stb.append("     T3.STAFFCD, ");
        }
        if ("1".equals(_param._sex)) {
            stb.append("     T3.SEX, ");
        } else {
            stb.append("     '' AS SEX, ");
        }
        stb.append("     SUM(CASE WHEN T3.SCORE = '10' THEN 1 ELSE 0 END) AS CNT_10, ");
        stb.append("     SUM(CASE WHEN T3.SCORE =  '9' THEN 1 ELSE 0 END) AS CNT_09, ");
        stb.append("     SUM(CASE WHEN T3.SCORE =  '8' THEN 1 ELSE 0 END) AS CNT_08, ");
        stb.append("     SUM(CASE WHEN T3.SCORE =  '7' THEN 1 ELSE 0 END) AS CNT_07, ");
        stb.append("     SUM(CASE WHEN T3.SCORE =  '6' THEN 1 ELSE 0 END) AS CNT_06, ");
        stb.append("     SUM(CASE WHEN T3.SCORE =  '5' THEN 1 ELSE 0 END) AS CNT_05, ");
        stb.append("     SUM(CASE WHEN T3.SCORE =  '4' THEN 1 ELSE 0 END) AS CNT_04, ");
        stb.append("     SUM(CASE WHEN T3.SCORE =  '3' THEN 1 ELSE 0 END) AS CNT_03, ");
        stb.append("     SUM(CASE WHEN T3.SCORE =  '2' THEN 1 ELSE 0 END) AS CNT_02, ");
        stb.append("     SUM(CASE WHEN T3.SCORE =  '1' THEN 1 ELSE 0 END) AS CNT_01, ");
        stb.append("     SUM(CASE WHEN T3.SCORE IS NULL AND T3.TRANSFERCD IS NULL THEN 1 ELSE 0 END) AS CNT_NULL, ");
        stb.append("     SUM(CASE WHEN T3.SCORE IS NULL AND T3.TRANSFERCD = '1' THEN 1 ELSE 0 END) AS CNT_TRANS, ");
        stb.append("     SUM(CASE WHEN T3.SCORE IS NOT NULL THEN 1 ELSE 0 END) AS CNT_SCORE, ");
        stb.append("     SUM(T3.OVER) AS CNT_OVER ");
        stb.append(" FROM ");
        stb.append("     HYOUTEI_BASE T3 ");
        stb.append(" GROUP BY ");
        stb.append("     T3.YEAR, ");
        stb.append("     T3.SEMESTER, ");
        stb.append("     T3.CLASSCD, ");
        stb.append("     T3.SCHOOL_KIND, ");
        stb.append("     T3.CURRICULUM_CD, ");
        stb.append("     T3.SUBCLASSCD ");
        if ("1".equals(_param._tantou)) {
            stb.append("     , T3.STAFFCD ");
        }
        if ("1".equals(_param._sex)) {
            stb.append("     , T3.SEX ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T3.CLASSCD, ");
        stb.append("     T3.SCHOOL_KIND, ");
        stb.append("     T3.CURRICULUM_CD, ");
        stb.append("     T3.SUBCLASSCD ");
        if ("1".equals(_param._sex)) {
            stb.append("     , T3.SEX ");
        }
        stb.append(" ) ");
        //男女紐づけ用
        if ("1".equals(_param._sex)) {
            stb.append(" , BOY_GIRL AS ( ");
            stb.append(" SELECT ");
            stb.append("     YEAR, ");
            stb.append("     SEMESTER, ");
            stb.append("     CLASSCD, ");
            stb.append("     SCHOOL_KIND, ");
            stb.append("     CURRICULUM_CD, ");
            stb.append("     SUBCLASSCD, ");
            if ("1".equals(_param._tantou)) {
                stb.append("     STAFFCD, ");
            }
            stb.append("     '1' AS SEX ");
            stb.append(" FROM RESULT ");
            stb.append(" GROUP BY ");
            stb.append("     YEAR, ");
            stb.append("     SEMESTER, ");
            stb.append("     CLASSCD, ");
            stb.append("     SCHOOL_KIND, ");
            stb.append("     CURRICULUM_CD, ");
            stb.append("     SUBCLASSCD ");
            if ("1".equals(_param._tantou)) {
                stb.append("     , STAFFCD ");
            }
            stb.append(" UNION ");
            stb.append(" SELECT ");
            stb.append("     YEAR, ");
            stb.append("     SEMESTER, ");
            stb.append("     CLASSCD, ");
            stb.append("     SCHOOL_KIND, ");
            stb.append("     CURRICULUM_CD, ");
            stb.append("     SUBCLASSCD, ");
            if ("1".equals(_param._tantou)) {
                stb.append("     STAFFCD, ");
            }
            stb.append("     '2' AS SEX ");
            stb.append(" FROM RESULT ");
            stb.append(" GROUP BY ");
            stb.append("     YEAR, ");
            stb.append("     SEMESTER, ");
            stb.append("     CLASSCD, ");
            stb.append("     SCHOOL_KIND, ");
            stb.append("     CURRICULUM_CD, ");
            stb.append("     SUBCLASSCD ");
            if ("1".equals(_param._tantou)) {
                stb.append("     , STAFFCD ");
            }
            stb.append(" ) ");
            //メイン表 男女用
            stb.append(" SELECT ");
            stb.append("     T4.*, ");
            if (!"1".equals(_param._sClass) && !"1".equals(_param._tantou)) {
                stb.append("     '' AS STAFFCD, ");
                stb.append("     '-' AS STAFFNAME, ");
                stb.append("     '-' AS TANTOU, ");
            } else if ("1".equals(_param._tantou)){
                stb.append("     T6.CHAIR_MIN, ");
                stb.append("     T6.STAFFNAME, ");
                stb.append("     T6.TANTOU, ");
            } else {
                stb.append("     '' AS STAFFCD, ");
                stb.append("     '-' AS STAFFNAME, ");
                stb.append("     T6.TANTOU, ");
            }
            stb.append("     T5.CNT_10, ");
            stb.append("     T5.CNT_09, ");
            stb.append("     T5.CNT_08, ");
            stb.append("     T5.CNT_07, ");
            stb.append("     T5.CNT_06, ");
            stb.append("     T5.CNT_05, ");
            stb.append("     T5.CNT_04, ");
            stb.append("     T5.CNT_03, ");
            stb.append("     T5.CNT_02, ");
            stb.append("     T5.CNT_01, ");
            stb.append("     T5.CNT_NULL, ");
            stb.append("     T5.CNT_TRANS, ");
            stb.append("     T5.CNT_SCORE, ");
            stb.append("     T5.CNT_OVER, ");
            stb.append("     CLASS.CLASSNAME, ");
            stb.append("     SUB.SUBCLASSNAME ");
            stb.append(" FROM ");
            stb.append("     BOY_GIRL T4 ");
            stb.append("     LEFT JOIN ");
            stb.append("         RESULT T5 ");
            stb.append("          ON T5.YEAR = T4.YEAR  ");
            stb.append("         AND T5.SEMESTER = T4.SEMESTER ");
            stb.append("         AND T5.CLASSCD = T4.CLASSCD ");
            stb.append("         AND T5.SCHOOL_KIND = T4.SCHOOL_KIND ");
            stb.append("         AND T5.CURRICULUM_CD = T4.CURRICULUM_CD ");
            stb.append("         AND T5.SUBCLASSCD = T4.SUBCLASSCD ");
            stb.append("         AND T5.SEX = T4.SEX ");
            if ("1".equals(_param._tantou)) {
                stb.append("         AND T5.STAFFCD = T4.STAFFCD ");
            }
            stb.append("     INNER JOIN ");
            stb.append("         CLASS_MST CLASS ");
            stb.append("          ON CLASS.CLASSCD = T4.CLASSCD ");
            stb.append("         AND CLASS.SCHOOL_KIND = T4.SCHOOL_KIND ");
            stb.append("     INNER JOIN ");
            stb.append("         SUBCLASS_MST SUB ");
            stb.append("          ON SUB.CLASSCD = T4.CLASSCD ");
            stb.append("         AND SUB.SCHOOL_KIND = T4.SCHOOL_KIND ");
            stb.append("         AND SUB.CURRICULUM_CD = T4.CURRICULUM_CD ");
            stb.append("         AND SUB.SUBCLASSCD = T4.SUBCLASSCD ");
            if ("1".equals(_param._tantou)) {
                stb.append("     LEFT JOIN ");
                stb.append("         STAFF_T T6 ");
                stb.append("              ON T6.YEAR = T4.YEAR ");
                stb.append("             AND T6.SEMESTER = T4.SEMESTER ");
                stb.append("             AND T6.CLASSCD = T4.CLASSCD ");
                stb.append("             AND T6.SCHOOL_KIND = T4.SCHOOL_KIND ");
                stb.append("             AND T6.CURRICULUM_CD = T4.CURRICULUM_CD ");
                stb.append("             AND T6.SUBCLASSCD = T4.SUBCLASSCD");
                stb.append("             AND T6.STAFFCD = T4.STAFFCD ");
            }
            if ("1".equals(_param._sClass)) {
                stb.append("     LEFT JOIN ");
                stb.append("         SCLASS_T T6 ");
                stb.append("              ON T6.YEAR = T4.YEAR ");
                stb.append("             AND T6.SEMESTER = T4.SEMESTER ");
                stb.append("             AND T6.CLASSCD = T4.CLASSCD ");
                stb.append("             AND T6.SCHOOL_KIND = T4.SCHOOL_KIND ");
                stb.append("             AND T6.CURRICULUM_CD = T4.CURRICULUM_CD ");
                stb.append("             AND T6.SUBCLASSCD = T4.SUBCLASSCD");
            }
            stb.append(" ORDER BY ");
            stb.append("     T4.CLASSCD, ");
            stb.append("     T4.SCHOOL_KIND, ");
            stb.append("     T4.CURRICULUM_CD, ");
            stb.append("     T4.SUBCLASSCD ");
            if ("1".equals(_param._tantou)) {
                stb.append("     ,T6.CHAIR_MIN ");
            }
        } else {
             //メイン表 男女合算
            stb.append(" SELECT ");
            stb.append("     T4.*, ");
            if (!"1".equals(_param._sClass) && !"1".equals(_param._tantou)) {
                stb.append("     '' AS STAFFCD, ");
                stb.append("     '-' AS STAFFNAME, ");
                stb.append("     '-' AS TANTOU, ");
            } else if ("1".equals(_param._tantou)){
                stb.append("     T5.CHAIR_MIN, ");
                stb.append("     T5.STAFFNAME, ");
                stb.append("     T5.TANTOU, ");
            } else {
                stb.append("     '' AS STAFFCD, ");
                stb.append("     '-' AS STAFFNAME, ");
                stb.append("     T5.TANTOU, ");
            }
            stb.append("     CLASS.CLASSNAME, ");
            stb.append("     SUB.SUBCLASSNAME ");
            stb.append(" FROM ");
            stb.append("     RESULT T4 ");
            stb.append("     INNER JOIN ");
            stb.append("         CLASS_MST CLASS ");
            stb.append("          ON CLASS.CLASSCD = T4.CLASSCD ");
            stb.append("         AND CLASS.SCHOOL_KIND = T4.SCHOOL_KIND ");
            stb.append("     INNER JOIN ");
            stb.append("         SUBCLASS_MST SUB ");
            stb.append("          ON SUB.CLASSCD = T4.CLASSCD ");
            stb.append("         AND SUB.SCHOOL_KIND = T4.SCHOOL_KIND ");
            stb.append("         AND SUB.CURRICULUM_CD = T4.CURRICULUM_CD ");
            stb.append("         AND SUB.SUBCLASSCD = T4.SUBCLASSCD ");
            if ("1".equals(_param._tantou)) {
                stb.append("     LEFT JOIN ");
                stb.append("         STAFF_T T5 ");
                stb.append("              ON T5.YEAR = T4.YEAR ");
                stb.append("             AND T5.SEMESTER = T4.SEMESTER ");
                stb.append("             AND T5.CLASSCD = T4.CLASSCD ");
                stb.append("             AND T5.SCHOOL_KIND = T4.SCHOOL_KIND ");
                stb.append("             AND T5.CURRICULUM_CD = T4.CURRICULUM_CD ");
                stb.append("             AND T5.SUBCLASSCD = T4.SUBCLASSCD");
                stb.append("             AND T5.STAFFCD = T4.STAFFCD ");
            }
            if ("1".equals(_param._sClass)) {
                stb.append("     LEFT JOIN ");
                stb.append("         SCLASS_T T5 ");
                stb.append("              ON T5.YEAR = T4.YEAR ");
                stb.append("             AND T5.SEMESTER = T4.SEMESTER ");
                stb.append("             AND T5.CLASSCD = T4.CLASSCD ");
                stb.append("             AND T5.SCHOOL_KIND = T4.SCHOOL_KIND ");
                stb.append("             AND T5.CURRICULUM_CD = T4.CURRICULUM_CD ");
                stb.append("             AND T5.SUBCLASSCD = T4.SUBCLASSCD");
            }
            stb.append(" ORDER BY ");
            stb.append("     T4.CLASSCD, ");
            stb.append("     T4.SCHOOL_KIND, ");
            stb.append("     T4.CURRICULUM_CD, ");
            stb.append("     T4.SUBCLASSCD ");
            if ("1".equals(_param._tantou)) {
                stb.append("     ,T5.CHAIR_MIN ");
            }
        }
        return stb.toString();
    }

    private class Bunpu {
        final String _classcd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclasscd;
        final String _subclassname;
        final String _staffname;
        final String _tantou;
        final String _cnt_10;
        final String _cnt_09;
        final String _cnt_08;
        final String _cnt_07;
        final String _cnt_06;
        final String _cnt_05;
        final String _cnt_04;
        final String _cnt_03;
        final String _cnt_02;
        final String _cnt_01;
        final String _cnt_Null;
        final String _cnt_Trans;
        final String _cnt_Score;
        final String _cnt_Over;
        final String _sex;

        public Bunpu (final String classcd, final String schoolKind, final String curriculumCd, final String subclasscd,
                final String classname, final String subclassname, final String staffname, final String tantou,
                final String cnt_10, final String cnt_09, final String cnt_08, final String cnt_07, final String cnt_06,
                final String cnt_05, final String cnt_04, final String cnt_03, final String cnt_02, final String cnt_01,
                final String cnt_Null, final String cnt_Trans, final String cnt_Score, final String cnt_Over, final String sex) {
            _classcd = classcd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _staffname = staffname;
            _tantou = tantou;
            _cnt_10 = cnt_10;
            _cnt_09 = cnt_09;
            _cnt_08 = cnt_08;
            _cnt_07 = cnt_07;
            _cnt_06 = cnt_06;
            _cnt_05 = cnt_05;
            _cnt_04 = cnt_04;
            _cnt_03 = cnt_03;
            _cnt_02 = cnt_02;
            _cnt_01 = cnt_01;
            _cnt_Null = cnt_Null;
            _cnt_Trans = cnt_Trans;
            _cnt_Score = cnt_Score;
            _cnt_Over = cnt_Over;
            _sex = sex;
        }
    }

    private class PrintData {
        final String _classCd;
        final String _className;
        final Map<String, Bunpu> bunpuMap = new LinkedHashMap();

        public PrintData (final String classCd, final String className) {
            _classCd = classCd;
            _className = className;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String[] _categorySelected;
        final String _semester;
        final String _loginYear;
        final String _loginDate;
        final String _outputDate;
        final String _nendo;
        final String _disp;
        final String _grade;
        final String _sex;
        final String _tantou;
        final String _sClass;
        final String _semeName;
        final String _semeSdate;
        final String _semeEdate;
        final String _gradeName;
        final boolean _allClass; //全教科選択ならtrue

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException, UnsupportedEncodingException {
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _semester = request.getParameter("SEMESTER");
            _loginYear = request.getParameter("CTRL_YEAR");
            _nendo = KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_loginYear)) + "年度";
            _loginDate = request.getParameter("CTRL_DATE").replace("/", "-");
            _disp = request.getParameter("DISP");
            _grade = request.getParameter("GRADE");
            _sex = request.getParameter("SEX");
            _tantou = request.getParameter("TANTOU");
            _sClass = request.getParameter("S_CLASS");
            _gradeName = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT GRADE_NAME1 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _loginYear + "' AND SCHOOL_KIND = 'H' AND GRADE = '" + _grade + "'"));

            final String selectCnt = String.valueOf(_categorySelected.length);
            _allClass = selectCnt.equals(request.getParameter("MAXCNT"));

            final Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            _outputDate = KNJ_EditDate.getAutoFormatDate(db2, sdf.format(date));

            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, "SELECT * FROM SEMESTER_MST WHERE YEAR='" + _loginYear+ "' AND SEMESTER = '" + _semester + "' "));
            _semeName = KnjDbUtils.getString(row, "SEMESTERNAME");
            _semeSdate = KnjDbUtils.getString(row, "SDATE");
            _semeEdate = StringUtils.defaultString(KnjDbUtils.getString(row, "EDATE"), "9999-99-99");
        }
    }
}
// eof
