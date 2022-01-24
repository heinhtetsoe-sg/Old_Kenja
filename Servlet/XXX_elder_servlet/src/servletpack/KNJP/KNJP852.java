/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id$
 *
 * 作成日: 2018/08/10
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJP;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJP852 {

    private static final Log log = LogFactory.getLog(KNJP852.class);

    private boolean _hasData;
    private final int LM_MAX_NUMBER = 20;

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
        PrintData printData = new PrintData();
        //生徒データ読込
        printData.loadSchInfo(db2);
        //入金予定データ読込
        printData.loadCollectData(db2);

        for (final Iterator iter = printData._schregMap.keySet().iterator(); iter.hasNext();) {
            final String schregno = (String)iter.next();
            final SchInfo schInfo = (SchInfo)printData._schregMap.get(schregno);
            final SchSlipInfo schSlipInfo = (SchSlipInfo)printData._schregCollectMap.get(schregno);

            //入金項目が1件も無い生徒はスキップ
            if (schSlipInfo == null) {
                continue;
            }

            //フォームセット
            svf.VrSetForm("KNJP852.frm", 1);

            //ヘッダ部出力
            printHeader(db2, svf, schInfo);

            //文面出力
            printDocument(svf);

            //年間予定額表
            printNenkanYotei(svf, schSlipInfo);

            svf.VrEndPage();
            _hasData = true;
        }
    }

    private void printHeader(final DB2UDB db2, final Vrw32alp svf, final SchInfo schInfo) {
        //帳票日付
        final String warekiDate = KNJ_EditDate.h_format_JP(db2, _param._printDate);
        svf.VrsOut("DATE", warekiDate);

        //生徒情報
        printByLength(svf, "NAME1", schInfo._gName + "　　様", new int[] {30, 40, 50});
        final String hrName = schInfo.getGradeStr() + "学年　" + schInfo.getHrClassStr() + "組　" + schInfo.getAttendnoStr() + "番　" + schInfo._schregno;
        svf.VrsOut("HR_NAME", hrName);
        printByLength(svf, "NAME2", schInfo._name + "　　様", new int[] {30, 40, 50});

        if (!"3".equals(_param._addrDiv)) {
            //郵便番号
            final String dispZipcd = ("1".equals(_param._addrDiv)) ? schInfo._gZipCd : schInfo._sZipCd;
            svf.VrsOut("ZIPCD", "〒" + dispZipcd);
            //住所1
            final String dispAddr1 = ("1".equals(_param._addrDiv)) ? schInfo._gAddr1 : schInfo._sAddr1;
            printByLength(svf, "ADDR1", dispAddr1, new int[] {30, 40, 50, 60});
            //住所2
            final String dispAddr2 = ("1".equals(_param._addrDiv)) ? schInfo._gAddr2 : schInfo._sAddr2;
            printByLength(svf, "ADDR2", dispAddr2, new int[] {30, 40, 50, 60});
        }

        //学校名
        final String schoolName = StringUtils.defaultIfEmpty(_param._certifSchool._schoolName, "");
        svf.VrsOut("SCHOOL_NAME", _param._certifSchool._schoolName);
        //校長名
        final String principalName = StringUtils.defaultIfEmpty(_param._certifSchool._jobName, "") + "　" + StringUtils.defaultIfEmpty(_param._certifSchool._principalName, "");
        svf.VrsOut("PRINCIPAL_NAME", principalName);
    }

    private void printByLength(final Vrw32alp svf, final String fieldPrefix, final String value, final int[] pattern) {
        //バイト長取得
        final int byteLength = KNJ_EditEdit.getMS932ByteLength(value);

        //フィールド名末尾の数字を取得
        int fieldCnt = 1;
        for (int threshold : pattern) {
            if (threshold >= byteLength) {
                break;
            }
            fieldCnt++;
        }

        //印字
        final String fieldName = fieldPrefix + "_" + fieldCnt;
        svf.VrsOut(fieldName, value);
    }

    private void printDocument(final Vrw32alp svf) {
        //タイトル
        svf.VrsOut("TITLE", _param._documentMst._title);
        //文面
        final String[] setText = KNJ_EditEdit.get_token(_param._documentMst._text, _param._textMoji * 2, _param._textGyou);
        if (null != setText) {
            for (int textCnt = 0; textCnt < setText.length; textCnt++) {
                svf.VrsOutn("FIELD1",  textCnt + 1, setText[textCnt]);
            }
        }

    }

    private void printNenkanYotei(final Vrw32alp svf, SchSlipInfo schSlipInfo) {
        final Map hojyoMap      = new TreeMap(); //「県補助金」行の値を保持
        final Map genmenMap    = new TreeMap(); //「奨学費」行の値を保持
        final Map otherTotalMap = new TreeMap(); //その他合計行の値を保持
        final Map monthTotalMap = new TreeMap(); //月合計行の値を保持

        boolean otherLineFlg = false;
        int lmCnt = 1;

        final Map lmCdMap = schSlipInfo._lmCdMap;
        for (final Iterator niter1 = lmCdMap.keySet().iterator(); niter1.hasNext();) {
            final String lmCd = (String)niter1.next();
            final LMInfo lmInfo = (LMInfo)lmCdMap.get(lmCd);

            //項目名
            svf.VrsOutn("ITEM1", lmCnt, lmInfo._mName);

            //「他」「県補助金」「奨学日」行を除く通常項目の表示行数を取得
            final int NORMAL_LM_MAX_NUMBER = LM_MAX_NUMBER - schSlipInfo.getAddDispCount() - schSlipInfo.getOtherCount();
            final int CALC_LM_MAX_NUMBER = (schSlipInfo.getLMCount() > NORMAL_LM_MAX_NUMBER) ? NORMAL_LM_MAX_NUMBER - 1 : NORMAL_LM_MAX_NUMBER;

            for (int i = 4; i <= 15; i++) {
                final int monthVal = (i > 12) ? i - 12 : i;
                final String month = String.format("%02d", monthVal);
                final MonthInfo monthInfo = (MonthInfo)lmInfo._monthMap.get(month);

                final Integer planMoney   = monthInfo._planMoney;
                final Integer hojyoMoney  = monthInfo._hojyoMoney;
                final Integer genmenMoney = monthInfo._genmenMoney;

                svf.VrsOutn("MONEY" + (i - 3), lmCnt, planMoney.toString());

                //県補助金
                if (!hojyoMap.containsKey(month)) {
                    hojyoMap.put(month, 0);
                }
                int befHojyoTotal = ((Integer)hojyoMap.get(month)).intValue();
                hojyoMap.put(month, befHojyoTotal + hojyoMoney);

                //奨学費
                if (!genmenMap.containsKey(month)) {
                    genmenMap.put(month, 0);
                }
                int befGenmenTotal = ((Integer)genmenMap.get(month)).intValue();
                genmenMap.put(month, befGenmenTotal + genmenMoney);

                if (lmCnt > CALC_LM_MAX_NUMBER) {
                    otherLineFlg = true;

                    //月毎"その他"合計算出(項目数が20より後は"その他"項目としてまとめる)
                    if (!otherTotalMap.containsKey(month)) {
                        otherTotalMap.put(month, 0);
                    }
                    final int befOtherTotal = ((Integer)monthTotalMap.get(month)).intValue();
                    monthTotalMap.put(month, befOtherTotal + planMoney);
                }

                //月毎合計算出
                if (!monthTotalMap.containsKey(month)) {
                    monthTotalMap.put(month, 0);
                }
                final int befMonthTotal = ((Integer)monthTotalMap.get(month)).intValue();

                //追加表示項目の金額
                Integer addDispLMMoney = 0;
                if (schSlipInfo._schAddDisplmCdMap.containsKey(lmCd)) {
                    final DispLMSetting dispSetting = (DispLMSetting)schSlipInfo._schAddDisplmCdMap.get(lmCd);

                    addDispLMMoney = dispSetting.getDispMoney(schSlipInfo._schregno, month);
                }

                monthTotalMap.put(month, befMonthTotal + planMoney - hojyoMoney - genmenMoney + addDispLMMoney);
            }

            lmCnt++;
        }

        //その他行表示
        if (otherLineFlg) {
            final int otherLineCnt = lmCnt++;
            svf.VrsOutn("ITEM1", otherLineCnt, "他");
            for (int i = 4; i <= 15; i++) {
                final int monthVal = (i > 12) ? i - 12 : i;
                final String month = String.format("%02d", monthVal);

                final Integer otherTotal  = ((Integer)otherTotalMap.get(month)).intValue();
                svf.VrsOutn("MONEY" + (i - 3), otherLineCnt, otherTotal.toString());
            }
        }

        //追加表示項目行
        if (schSlipInfo._addDispFlg) {
            for (final Iterator adIter = schSlipInfo._schAddDisplmCdMap.keySet().iterator(); adIter.hasNext();) {
                final String lmCd = (String)adIter.next();
                final DispLMSetting dispSetting = (DispLMSetting)schSlipInfo._schAddDisplmCdMap.get(lmCd);
                final int addDispLineCnt = lmCnt++;
                svf.VrsOutn("ITEM1", addDispLineCnt, dispSetting._dispName);
                for (int i = 4; i <= 15; i++) { //全ての月で名称マスタに設定した金額をセット
                    final String month = String.format("%02d", (i <= 12) ? i : i - 12 );
                    svf.VrsOutn("MONEY" + (i - 3), addDispLineCnt, dispSetting.getDispMoney(schSlipInfo._schregno, month).toString());
                }
            }
        }

        //県補助金行
        if (schSlipInfo._hojyoLineFlg) {
            final int hojyoLineCnt = lmCnt++;
            svf.VrsOutn("ITEM1", hojyoLineCnt, "県補助金");
            for (int i = 4; i <= 15; i++) {
                final int monthVal = (i > 12) ? i - 12 : i;
                final String month = String.format("%02d", monthVal);

                final Integer hojyoTotal  = ((Integer)hojyoMap.get(month)).intValue();
                final Integer dispHojyoMoney = -1 * hojyoTotal;
                svf.VrsOutn("MONEY" + (i - 3), hojyoLineCnt, dispHojyoMoney.toString());
            }
        }
        //奨学費行
        if (schSlipInfo._genmenLineFlg) {
            final int genmenLineCnt = lmCnt++;
            svf.VrsOutn("ITEM1", genmenLineCnt, "奨学費");
            for (int i = 4; i <= 15; i++) {
                final int monthVal = (i > 12) ? i - 12 : i;
                final String month = String.format("%02d", monthVal);

                final Integer genmenTotal = ((Integer)genmenMap.get(month)).intValue();
                final Integer dispGenmenMoney = -1 * genmenTotal;
                svf.VrsOutn("MONEY" + (i - 3), genmenLineCnt, dispGenmenMoney.toString());
            }
        }

        //合計表示
        svf.VrsOutn("ITEM1", 21, "合計額");
        for (int i = 4; i <= 15; i++) {
            final int monthVal = (i > 12) ? i - 12 : i;
            final String month = String.format("%02d", monthVal);

            final Integer monthTotal  = ((Integer)monthTotalMap.get(month)).intValue();
            svf.VrsOutn("MONEY" + (i - 3), 21, monthTotal.toString());
        }
        svf.VrsOut("TOTAL_MONEY", schSlipInfo._totalPlanMoney.toString());
    }

    private String convertDate(final String date) {
        String rtnStr = "";
        if (date != null && !"".equals(date)) {
            String[] tmp = StringUtils.split(date, "-");
            rtnStr = String.format("%d年%d月%d日", Integer.valueOf(tmp[0]), Integer.valueOf(tmp[1]), Integer.valueOf(tmp[2]));
        }
        return rtnStr;
    }

    class PrintData {
       Map _schregMap = new LinkedHashMap();
       Map _schregCollectMap = new TreeMap();

       private String getCollectSql() {
           final StringBuffer stb = new StringBuffer();
           stb.append("    WITH MAX_SEMES AS ( ");
           stb.append("        SELECT ");
           stb.append("            SCHREGNO, ");
           stb.append("            YEAR, ");
           stb.append("            MAX(SEMESTER) AS SEMESTER ");
           stb.append("        FROM ");
           stb.append("            SCHREG_REGD_DAT ");
           stb.append("        GROUP BY ");
           stb.append("            SCHREGNO, ");
           stb.append("            YEAR ");
           stb.append("    ), REGD_DATA AS ( ");
           stb.append("        SELECT ");
           stb.append("            REGD.* ");
           stb.append("        FROM ");
           stb.append("            SCHREG_REGD_DAT REGD ");
           stb.append("            INNER JOIN MAX_SEMES SEM ");
           stb.append("                ON SEM.SCHREGNO  = REGD.SCHREGNO ");
           stb.append("                AND SEM.YEAR     = REGD.YEAR ");
           stb.append("                AND SEM.SEMESTER = REGD.SEMESTER ");
           stb.append("        WHERE ");
           stb.append("            REGD.YEAR = '" + _param._ctrlYear + "' ");
           if ("1".equals(_param._categoryIsClass)) {
               stb.append("  AND REGD.GRADE || '-' || REGD.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categorySelected));
           } else {
               stb.append("  AND REGD.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected));
           }
           stb.append("    ) ");
           stb.append("    SELECT ");
           stb.append("        PLAN_M.SCHREGNO, ");
           stb.append("        PLAN_M.PLAN_YEAR, ");
           stb.append("        PLAN_M.PLAN_MONTH, ");
           stb.append("        PLAN_M.COLLECT_L_CD, ");
           stb.append("        PLAN_M.COLLECT_M_CD, ");
           stb.append("        SUM(VALUE(PLAN_M.PLAN_MONEY, 0) - VALUE(PLAN_M.PAID_MONEY, 0)) AS PLAN_MONEY, ");
           stb.append("        MAX(MMST.COLLECT_M_NAME) AS COLLECT_M_NAME, ");
           stb.append("        SUM(REDP.DECISION_MONEY) AS HOJYO_MONEY, ");
           stb.append("        SUM(REDSP.DECISION_MONEY) AS GENMEN_MONEY ");
           stb.append("    FROM ");
           stb.append("        COLLECT_SLIP_PLAN_M_DAT PLAN_M ");
           stb.append("        INNER JOIN COLLECT_SLIP_DAT SLIP ");
           stb.append("            ON SLIP.SCHOOLCD     = PLAN_M.SCHOOLCD ");
           stb.append("            AND SLIP.SCHOOL_KIND = PLAN_M.SCHOOL_KIND ");
           stb.append("            AND SLIP.YEAR        = PLAN_M.YEAR ");
           stb.append("            AND SLIP.SLIP_NO     = PLAN_M.SLIP_NO ");
           stb.append("            AND SLIP.SCHREGNO    = PLAN_M.SCHREGNO ");
           stb.append("        INNER JOIN REGD_DATA AS REGD ");
           stb.append("            ON REGD.SCHREGNO = PLAN_M.SCHREGNO ");
           stb.append("        LEFT JOIN COLLECT_M_MST MMST ");
           stb.append("            ON MMST.SCHOOLCD      = PLAN_M.SCHOOLCD ");
           stb.append("            AND MMST.SCHOOL_KIND  = PLAN_M.SCHOOL_KIND ");
           stb.append("            AND MMST.YEAR         = PLAN_M.YEAR ");
           stb.append("            AND MMST.COLLECT_L_CD = PLAN_M.COLLECT_L_CD ");
           stb.append("            AND MMST.COLLECT_M_CD = PLAN_M.COLLECT_M_CD ");
           stb.append("        LEFT JOIN REDUCTION_PLAN_DAT REDP ");
           stb.append("            ON REDP.SCHOOLCD            = PLAN_M.SCHOOLCD ");
           stb.append("            AND REDP.SCHOOL_KIND        = PLAN_M.SCHOOL_KIND ");
           stb.append("            AND REDP.YEAR               = PLAN_M.YEAR ");
           stb.append("            AND REDP.REDUCTION_TARGET   = MMST.GAKUNOKIN_DIV ");
           stb.append("            AND REDP.SLIP_NO            = PLAN_M.SLIP_NO ");
           stb.append("            AND REDP.PLAN_YEAR          = PLAN_M.PLAN_YEAR ");
           stb.append("            AND REDP.PLAN_MONTH         = PLAN_M.PLAN_MONTH ");
           stb.append("            AND REDP.SCHREGNO           = PLAN_M.SCHREGNO ");
           stb.append("        LEFT JOIN REDUCTION_SCHOOL_PLAN_DAT REDSP ");
           stb.append("            ON REDSP.SCHOOLCD           = PLAN_M.SCHOOLCD ");
           stb.append("            AND REDSP.SCHOOL_KIND       = PLAN_M.SCHOOL_KIND ");
           stb.append("            AND REDSP.YEAR              = PLAN_M.YEAR ");
           stb.append("            AND REDSP.REDUCTION_TARGET  = MMST.GAKUNOKIN_DIV ");
           stb.append("            AND REDSP.SLIP_NO           = PLAN_M.SLIP_NO ");
           stb.append("            AND REDSP.PLAN_YEAR         = PLAN_M.PLAN_YEAR ");
           stb.append("            AND REDSP.PLAN_MONTH        = PLAN_M.PLAN_MONTH ");
           stb.append("            AND REDSP.SCHREGNO          = PLAN_M.SCHREGNO ");
           stb.append("    WHERE ");
           stb.append("        SLIP.SCHOOLCD 		  = '" + _param._schoolcd + "' ");
           stb.append("        AND SLIP.SCHOOL_KIND  = '" +  _param._schoolKind + "' ");
           stb.append("        AND SLIP.YEAR 		  = '" + _param._ctrlYear + "' ");
           stb.append("        AND SLIP.CANCEL_DATE IS NULL ");
           stb.append("    GROUP BY ");
           stb.append("        PLAN_M.SCHREGNO, ");
           stb.append("        PLAN_M.COLLECT_L_CD, ");
           stb.append("        PLAN_M.COLLECT_M_CD, ");
           stb.append("        PLAN_M.PLAN_YEAR, ");
           stb.append("        PLAN_M.PLAN_MONTH ");
           stb.append("    ORDER BY ");
           stb.append("        PLAN_M.SCHREGNO, ");
           stb.append("        PLAN_M.COLLECT_L_CD, ");
           stb.append("        PLAN_M.COLLECT_M_CD, ");
           stb.append("        PLAN_M.PLAN_YEAR, ");
           stb.append("        PLAN_M.PLAN_MONTH ");

           return stb.toString();
       }

       public void loadCollectData(final DB2UDB db2) {
           String sql = getCollectSql();
           log.debug("collect sql = " + sql);
           try {
               PreparedStatement ps = db2.prepareStatement(sql);
               ResultSet rs = ps.executeQuery();
               while (rs.next()) {
                   final String  schregno     = rs.getString("SCHREGNO");
                   final String  planYear     = rs.getString("PLAN_YEAR");
                   final String  planMonth    = rs.getString("PLAN_MONTH");
                   final String  lCd 	      = rs.getString("COLLECT_L_CD");
                   final String  mCd 	      = rs.getString("COLLECT_M_CD");
                   Integer planMoney          = Integer.valueOf(StringUtils.defaultIfEmpty(rs.getString("PLAN_MONEY"), "0"));
                   final String  mName        = rs.getString("COLLECT_M_NAME");
                   final Integer genmenMoney  = Integer.valueOf(StringUtils.defaultIfEmpty(rs.getString("GENMEN_MONEY"), "0"));
                   final Integer hojyoMoney   = Integer.valueOf(StringUtils.defaultIfEmpty(rs.getString("HOJYO_MONEY"), "0"));

                   //名称マスタP011に設定された除外項目をスキップ
                   if (_param._removeLMCdMap.containsKey(lCd + mCd)) {
                       continue;
                   }

                   //名称マスタP011に設定された加算項目に対して金額を加算
                   if (_param._plusMoneyLMCdMap.containsKey(lCd + mCd)) {
                       final Integer plusMoney = (Integer)_param._plusMoneyLMCdMap.get(lCd + mCd);

                       if (planMoney > 0) { //徴収額がある場合のみ加算
                           planMoney += plusMoney;

                           //城東追加処理 (城東の３年生は、２・３月分を３月にまとめて徴収)
                           if ("jyoto".equals(_param._z010SchoolName) && "03".equals(_param._gradeCd)) {
                               if ("02".equals(planMonth)) {
                                   planMoney += plusMoney; //2月分に3月分を足し込む
                               } else if ("03".equals(planMonth)) {
                                   planMoney -= plusMoney; //3月から2月に移行した分引く
                               }
                           }
                       }
                   }

                   //学籍番号単位
                   if (!_schregCollectMap.containsKey(schregno)) {
                       _schregCollectMap.put(schregno, new SchSlipInfo(schregno));
                   }
                   final SchSlipInfo schSlipInfo = (SchSlipInfo)(_schregCollectMap.get(schregno));

                   //名称マスタP011に設定された追加表示項目をセット
                   Integer dispMoney = 0;
                   if (_param._addDispLMCdMap.containsKey(lCd + mCd)) {
                       final DispLMSetting dispSetting = (DispLMSetting)_param._addDispLMCdMap.get(lCd + mCd);
                       schSlipInfo._schAddDisplmCdMap.put(lCd + mCd, dispSetting);
                       schSlipInfo._addDispFlg = true;
                       if (planMoney == 0) {
                           dispSetting.setNoDispMonth(schregno, planMonth);
                       }
                       dispMoney = dispSetting.getDispMoney(schregno, planMonth);
                   }

                   if (hojyoMoney != 0) { //県補助金ありのフラグを立てる
                       schSlipInfo._hojyoLineFlg = true;
                   }

                   if (genmenMoney != 0) { //奨学費ありのフラグを立てる
                       schSlipInfo._genmenLineFlg = true;
                   }
                   schSlipInfo._totalPlanMoney += (planMoney - genmenMoney - hojyoMoney + dispMoney); ////年間予定合計額

                   //項目単位
                   final Map lmCdMap = schSlipInfo._lmCdMap;
                   if (!lmCdMap.containsKey(lCd + mCd)) {
                       lmCdMap.put(lCd + mCd, new LMInfo(lCd, mCd, mName));
                   }
                   final LMInfo lmInfo = (LMInfo)(lmCdMap.get(lCd + mCd));

                   //月単位
                   final Map monthMap = lmInfo._monthMap;
                   monthMap.put(planMonth, new MonthInfo(planYear, planMonth, planMoney, genmenMoney, hojyoMoney));
               }
               ps.close();
               rs.close();
           } catch (Exception e) {
               log.error("Exception", e);
           } finally {
               db2.commit();
           }
       }

       private String getSchInfoSql() {
           final StringBuffer stb = new StringBuffer();
           stb.append("    WITH MAX_SEMES AS ( ");
           stb.append("        SELECT ");
           stb.append("            SCHREGNO, ");
           stb.append("            YEAR, ");
           stb.append("            MAX(SEMESTER) AS SEMESTER ");
           stb.append("        FROM ");
           stb.append("            SCHREG_REGD_DAT ");
           stb.append("        GROUP BY ");
           stb.append("            SCHREGNO, ");
           stb.append("            YEAR ");
           stb.append("    ), REGD_DATA AS ( ");
           stb.append("        SELECT ");
           stb.append("            REGD.* ");
           stb.append("        FROM ");
           stb.append("            SCHREG_REGD_DAT REGD ");
           stb.append("            INNER JOIN MAX_SEMES SEM ");
           stb.append("                ON SEM.SCHREGNO = REGD.SCHREGNO ");
           stb.append("                AND SEM.YEAR = REGD.YEAR ");
           stb.append("                AND SEM.SEMESTER = REGD.SEMESTER ");
           stb.append("        WHERE ");
           stb.append("            REGD.YEAR = '" + _param._ctrlYear + "' ");
           if ("1".equals(_param._categoryIsClass)) {
               stb.append("  AND REGD.GRADE || '-' || REGD.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categorySelected));
           } else {
               stb.append("  AND REGD.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected));
           }
           if (!"2".equals(_param._categoryIsClass) && !"1".equals(_param._outputDiv)) {
               //「クラス選択」で、かつ出力対象が全員でない場合は以下
               if ("2".equals(_param._outputDiv)) { //寮生
                   stb.append(" AND EXISTS ( ");
               } else {
                   stb.append(" AND NOT EXISTS ("); //寮生以外
               }
               stb.append("     SELECT ");
               stb.append("         'X' ");
               stb.append("     FROM ");
               stb.append("         SCHREG_DOMITORY_HIST_DAT DHIST ");
               stb.append("     WHERE ");
               stb.append("         DHIST.SCHREGNO = REGD.SCHREGNO ");
               stb.append("         AND '" + _param._printDate + "' BETWEEN DHIST.DOMI_ENTDAY AND VALUE (DHIST.DOMI_OUTDAY, '9999-12-31') ");
               stb.append(" ) ");
           }
           stb.append("    ) ");
           stb.append("    SELECT ");
           stb.append("        REGD.SCHREGNO, ");
           stb.append("        BASE.NAME, ");
           stb.append("        REGD.GRADE, ");
           stb.append("        GDAT.GRADE_CD, ");
           stb.append("        REGD.HR_CLASS, ");
           stb.append("        REGD.ATTENDNO, ");
           stb.append("        GUARD.GUARD_NAME, ");
           stb.append("        SAD.ZIPCD, ");
           stb.append("        SAD.ADDR1, ");
           stb.append("        SAD.ADDR2, ");
           stb.append("        GAD.GUARD_ZIPCD, ");
           stb.append("        GAD.GUARD_ADDR1, ");
           stb.append("        GAD.GUARD_ADDR2 ");
           stb.append("    FROM ");
           stb.append("        REGD_DATA REGD ");
           stb.append("    LEFT JOIN SCHREG_BASE_MST BASE ");
           stb.append("        ON BASE.SCHREGNO = REGD.SCHREGNO ");
           stb.append("    LEFT JOIN SCHREG_REGD_GDAT GDAT ");
           stb.append("        ON GDAT.YEAR  = REGD.YEAR ");
           stb.append("       AND GDAT.GRADE = REGD.GRADE ");
           stb.append("    LEFT JOIN GUARDIAN_DAT GUARD ");
           stb.append("        ON GUARD.SCHREGNO = REGD.SCHREGNO ");
           stb.append("    LEFT JOIN SCHREG_ADDRESS_DAT SAD ");
           stb.append("        ON SAD.SCHREGNO = REGD.SCHREGNO ");
           stb.append("       AND '" + _param._printDate + "' BETWEEN SAD.ISSUEDATE AND SAD.EXPIREDATE ");
           stb.append("    LEFT JOIN GUARDIAN_ADDRESS_DAT GAD ");
           stb.append("        ON GAD.SCHREGNO = REGD.SCHREGNO ");
           stb.append("       AND '" + _param._printDate + "' BETWEEN GAD.ISSUEDATE AND GAD.EXPIREDATE ");
           stb.append("    ORDER BY ");
           stb.append("       REGD.GRADE, ");
           stb.append("       REGD.HR_CLASS, ");
           stb.append("       REGD.ATTENDNO ");

           return stb.toString();
       }

       public void loadSchInfo(final DB2UDB db2) {
           String sql = getSchInfoSql();
           log.fatal("schInfo sql = " + sql);
           try {
               PreparedStatement ps = db2.prepareStatement(sql);
               ResultSet rs = ps.executeQuery();
               while (rs.next()) {
                   final String schregno  = rs.getString("SCHREGNO");
                   final String gradeCd   = rs.getString("GRADE_CD");
                   final String hrClass   = rs.getString("HR_CLASS");
                   final String attendNo  = rs.getString("ATTENDNO");
                   final String name      = rs.getString("NAME");
                   final String gName     = rs.getString("GUARD_NAME");
                   final String zipcd     = rs.getString("ZIPCD");
                   final String addr1	  = rs.getString("ADDR1");
                   final String addr2	  = rs.getString("ADDR2");
                   final String gZipcd	  = rs.getString("GUARD_ZIPCD");
                   final String gAddr1 	  = rs.getString("GUARD_ADDR1");
                   final String gAddr2 	  = rs.getString("GUARD_ADDR2");

                   SchInfo addData = new SchInfo(schregno, gradeCd, hrClass, attendNo, name, gName, zipcd, addr1, addr2, gZipcd, gAddr1, gAddr2);
                   _schregMap.put(schregno, addData);
               }
               ps.close();
               rs.close();
           } catch (Exception e) {
               log.error("Exception", e);
           } finally {
               db2.commit();
           }
       }
    }

    class SchInfo {
        final String _schregno;
        final String _gradeCd;
        final String _hrClass;
        final String _attendNo;
        final String _name;
        final String _gName;
        final String _sZipCd;
        final String _sAddr1;
        final String _sAddr2;
        final String _gZipCd;
        final String _gAddr1;
        final String _gAddr2;

        public SchInfo(final String schregno, final String gradeCd, final String hrClass, final String attendNo, final String name, final String gName, final String sZipCd, final String sAddr1, final String sAddr2, final String gZipCd, final String gAddr1, final String gAddr2) {
            _schregno  = schregno;
            _gradeCd   = gradeCd;
            _hrClass   = hrClass;
            _attendNo  = attendNo;
            _name  	   = name;
            _gName     = gName;
            _sZipCd    = sZipCd;
            _sAddr1    = sAddr1;
            _sAddr2    = sAddr2;
            _gZipCd    = gZipCd;
            _gAddr1    = gAddr1;
            _gAddr2    = gAddr2;
        }

        public String getGradeStr() {
            if (_gradeCd != null || _gradeCd != "") {
                return String.valueOf(Integer.valueOf(_gradeCd));
            } else {
                return "";
            }
        }
        public String getHrClassStr() {
            if (_hrClass != null || _hrClass != "") {
                return String.valueOf(Integer.valueOf(_hrClass));
            } else {
                return "";
            }
        }
        public String getAttendnoStr() {
            if (_attendNo != null || _attendNo != "") {
                return String.valueOf(Integer.valueOf(_attendNo));
            } else {
                return "";
            }
        }
    }

    class SchSlipInfo {
        final String  _schregno;
        Integer _totalPlanMoney;
        boolean _genmenLineFlg;
        boolean _hojyoLineFlg;
        final Map _lmCdMap;
        boolean _addDispFlg;
        final Map _schAddDisplmCdMap;

        public SchSlipInfo(final String schregno) {
            _schregno          = schregno;
            _genmenLineFlg     = false;
            _hojyoLineFlg      = false;
            _totalPlanMoney    = 0;
            _lmCdMap           = new TreeMap();
            _addDispFlg        = false;
            _schAddDisplmCdMap = new TreeMap();
        }
        public int getLMCount() {
            return _lmCdMap.size();
        }
        public int getAddDispCount() {
            return _schAddDisplmCdMap.size();
        }
        public int getOtherCount() {
            int cnt = 0;
            if (_hojyoLineFlg) {
                cnt++;
            }
            if (_genmenLineFlg) {
                cnt++;
            }
            return cnt;
        }
    }

    class LMInfo {
        final String  _mCd;
        final String  _lCd;
        final String  _mName;
        final Map _monthMap;

        public LMInfo(final String lCd, final String mCd, final String mName) {
            _lCd       = lCd;
            _mCd       = mCd;
            _mName     = mName;
            _monthMap  = new TreeMap();
        }
    }

    class MonthInfo {
        final String  _planYear;
        final String  _planMonth;
        final Integer _planMoney;
        final Integer _genmenMoney;
        final Integer _hojyoMoney;

        public MonthInfo(final String planYear, final String planMonth, final Integer planMoney, final Integer genmenMoney, final Integer hojyoMoney) {
            _planYear    = planYear;
            _planMonth   = planMonth;
            _planMoney   = planMoney;
            _genmenMoney = genmenMoney;
            _hojyoMoney  = hojyoMoney;
        }
    }



    //証明書学校データ
    private class CertifSchool {
        final String _schoolName;
        final String _jobName;
        final String _principalName;
        public CertifSchool(
                final String schoolName,
                final String jobName,
                final String principalName
        ) {
            _schoolName     = schoolName;
            _jobName        = jobName;
            _principalName  = principalName;
        }
    }

    //文面マスタ
    private class DocumentMst {
        private final String _title;
        private final String _text;
        public DocumentMst(
                final String title,
                final String text
        ) {
            _title  = title;
            _text   = text;
        }
    }

    //追加表示項目設定
    private class DispLMSetting {
        private final String _dispName;
        private final Integer _dispMoney;
        public Map _schNoDispMonth;

        public DispLMSetting(
                final String dispName,
                final Integer dispMoney
        ) {
            _dispName   = dispName;
            _dispMoney  = dispMoney;
            _schNoDispMonth = new TreeMap();
        }

        public Integer getDispMoney(final String schregno, final String planMonth) {
            Integer rtnMoney = _dispMoney;

            //城東追加処理 (城東の３年生は、２・３月分を３月にまとめて徴収)
            if ("jyoto".equals(_param._z010SchoolName) && "03".equals(_param._gradeCd)) {
                if ("02".equals(planMonth)) {
                    rtnMoney = _dispMoney * 2; //2月分に3月分を足し込む
                } else if ("03".equals(planMonth)) {
                    rtnMoney = 0; //3月から2月に移行した分引く
                }
            }

            List noDispmonthList = (List)_schNoDispMonth.get(schregno);
            if (noDispmonthList != null && noDispmonthList.contains(planMonth)) {
                rtnMoney = 0; //徴収額が無い場合補助金は無し
            }
            return rtnMoney;
        }

        public void setNoDispMonth(final String schregno, final String month) {
            if (!_schNoDispMonth.containsKey(schregno)) {
                _schNoDispMonth.put(schregno, new ArrayList());
            }
            List noDispmonthList = (List)_schNoDispMonth.get(schregno);
            noDispmonthList.add(month);
        }

    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 75206 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _categoryIsClass;
        final String _grade;
        final String _schoolKind;
        final String _gradeCd;
        final String _sOrderFlg;
        final String _hrClass;
        final String[] _categorySelected;
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _loginDate;
        final String _schoolcd;
        final String _prgid;
        final String _outputDiv;
        final String _addrDiv;
        final String _printDate;
        final String _documentCd;
        final CertifSchool _certifSchool;
        final DocumentMst _documentMst;
        final int _textMoji;
        final int _textGyou;
        final Map _plusMoneyLMCdMap;
        final Map _addDispLMCdMap;
        final Map _removeLMCdMap;
        final String _z010SchoolName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _categoryIsClass = request.getParameter("CATEGORY_IS_CLASS");
            final String[] orderwk = StringUtils.split(StringUtils.defaultString(request.getParameter("GRADE"), ""), "-");
            if (orderwk.length > 1) {
                _grade = orderwk[0];
                _sOrderFlg = orderwk[1];
            } else {
                //区切り文字が無い場合は、今まで通りの動作とする。
                _grade = request.getParameter("GRADE");
                _sOrderFlg = "0";
            }
            _ctrlYear = request.getParameter("YEAR");
            _schoolKind = getSchoolKind(db2);
            _gradeCd = getGradeCd(db2);
            _hrClass = request.getParameter("HR_CLASS");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _schoolcd = request.getParameter("SCHOOLCD");
            _prgid = request.getParameter("PRGID");
            _outputDiv = request.getParameter("OUTPUT_DIV");
            _addrDiv = request.getParameter("ADDRESS_DIV");
            String tmpDate = request.getParameter("PRINT_DATE");
            _printDate = tmpDate.replace("/", "-");
            _documentCd = request.getParameter("DOCUMENTCD");
            _textMoji = Integer.valueOf(request.getParameter("TEXT_MOJI"));
            _textGyou = Integer.valueOf(request.getParameter("TEXT_GYOU"));

            _certifSchool = getCertifSchool(db2);
            _documentMst = getDocumentMst(db2);

            //名称マスタP011設定
            _plusMoneyLMCdMap = getPlusMoneyLMCdMap(db2);
            _addDispLMCdMap   = getAddDispLMCdMap(db2);
            _removeLMCdMap    = getRemoveLMCdMap(db2);

            _z010SchoolName = getZ010(db2, "NAME1");
        }

        private String getZ010(final DB2UDB db2, final String field) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT " + field + " FROM NAME_MST WHERE NAMECD1='Z010' AND NAMECD2 = '00'"));
        }

        private String getGradeCd(final DB2UDB db2) {
            String retstr = "";
            String sql = " SELECT GRADE_CD FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _ctrlYear + "' AND GRADE = '" + _grade + "' ";
            try {
                log.debug("gradeCd sql = " + sql);
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    retstr = rs.getString("GRADE_CD");
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
            }
            return retstr;
        }

        private String getSchoolKind(final DB2UDB db2) {
            String retstr = "";
            String sql = " SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _ctrlYear + "' AND GRADE = '" + _grade + "' ";
            try {
                log.debug("schoolKind sql = " + sql);
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    retstr = rs.getString("SCHOOL_KIND");
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
            }
            return retstr;
        }

        /** 証明書学校データ */
        private CertifSchool getCertifSchool(final DB2UDB db2) {
            CertifSchool certifSchool = new CertifSchool(null, null, null);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {

                ps = db2.prepareStatement("SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _ctrlYear + "' AND CERTIF_KINDCD = '149' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    final String schoolName     = rs.getString("SCHOOL_NAME");
                    final String jobName        = rs.getString("JOB_NAME");
                    final String principalName  = rs.getString("PRINCIPAL_NAME");
                    certifSchool = new CertifSchool(schoolName, jobName, principalName);
                }
            } catch (SQLException ex) {
                log.debug("getCertif exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return certifSchool;
        }

        private DocumentMst getDocumentMst(final DB2UDB db2) {
            DocumentMst documentMst = new DocumentMst("", "");
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT * FROM DOCUMENT_MST WHERE DOCUMENTCD = '" + _documentCd + "'";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    final String title = rs.getString("TITLE");
                    final String text = rs.getString("TEXT");
                    documentMst = new DocumentMst(title, text);
                }
            } catch (SQLException ex) {
                log.debug("DOCUMENT_MST exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return documentMst;
        }

        //名称マスタP011(加算項目)
        private Map getPlusMoneyLMCdMap(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            final Map rtnMap = new TreeMap();

            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     NAME1 AS PLUS_LM_CD, ");
                stb.append("     NAME2 AS PLUS_MONEY ");
                stb.append(" FROM ");
                stb.append("     V_NAME_MST ");
                stb.append(" WHERE ");
                stb.append("     YEAR = '" + _ctrlYear + "' ");
                stb.append("     AND NAMECD1 = 'P011' ");
                stb.append("     AND NAMESPARE1 = '1' "); //1:加算項目設定
                stb.append("     AND NAME1 IS NOT NULL ");

                final String sql = stb.toString();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String plsLMCd = rs.getString("PLUS_LM_CD");
                    final Integer plsMoney = rs.getInt("PLUS_MONEY");
                    rtnMap.put(plsLMCd, plsMoney);
                }
            } catch (SQLException ex) {
                log.debug("DOCUMENT_MST exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtnMap;
        }

        //名称マスタP011(追加表示項目)
        private Map getAddDispLMCdMap(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            final Map rtnMap = new TreeMap();

            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     T1.NAME1 AS DISP_LM_NAME, ");
                stb.append("     T1.NAME2 AS DISP_MONEY, ");
                stb.append("     T2.NAME1 AS PLUS_LM_CD ");
                stb.append(" FROM ");
                stb.append("     V_NAME_MST T1 ");
                stb.append("     LEFT JOIN V_NAME_MST T2 ");
                stb.append("          ON T2.YEAR    = T1.YEAR ");
                stb.append("         AND T2.NAMECD1 = 'P011' ");
                stb.append("         AND T2.NAMECD2 = T1.NAME3 ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + _ctrlYear + "' ");
                stb.append("     AND T1.NAMECD1 = 'P011' ");
                stb.append("     AND T1.NAMESPARE1 = '2' "); //2:追加表示項目
                stb.append("     AND T2.NAME1 IS NOT NULL ");

                final String sql = stb.toString();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String  plsLMCd    = rs.getString("PLUS_LM_CD");
                    final String  dispName   = rs.getString("DISP_LM_NAME");
                    final Integer dispMoney  = rs.getInt("DISP_MONEY");

                    rtnMap.put(plsLMCd, new DispLMSetting(dispName, dispMoney));
                }
            } catch (SQLException ex) {
                log.debug("DOCUMENT_MST exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtnMap;
        }

        //名称マスタP011(除外項目)
        private Map getRemoveLMCdMap(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            final Map rtnMap = new TreeMap();

            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     NAME1 AS RM_LM_CD ");
                stb.append(" FROM ");
                stb.append("     V_NAME_MST ");
                stb.append(" WHERE ");
                stb.append("     YEAR = '" + _ctrlYear + "' ");
                stb.append("     AND NAMECD1 = 'P011' ");
                stb.append("     AND NAMESPARE1 = '3' "); //3:除外項目設定
                stb.append("     AND NAME1 IS NOT NULL ");

                final String sql = stb.toString();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String rmLMCd = rs.getString("RM_LM_CD");
                    rtnMap.put(rmLMCd, "");
                }
            } catch (SQLException ex) {
                log.debug("DOCUMENT_MST exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtnMap;
        }

    }
}

// eof
