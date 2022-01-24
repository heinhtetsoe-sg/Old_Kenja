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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

public class KNJP853 {

    private static final Log log = LogFactory.getLog(KNJP853.class);

    private boolean _hasData;
    private final String KOUNOUKIN_CD = "999";
    private final int UTIWAKE_MAX_CNT = 20;

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
            final String schregno  = (String)iter.next();
            final SchInfo schInfo  = (SchInfo)printData._schregMap.get(schregno);
            final Map lmInfoMap    = (Map)printData._schregCollectMap.get(schregno);
            final List outputSchregNoList = (List)printData._outputSchregNoList;
            final String limitDate = (String)printData._limitDateMap.get(schregno);

            //課外費項目が1件も無い生徒はスキップ
            if (!outputSchregNoList.contains(schregno)) {
                continue;
            }

            //フォームセット
            svf.VrSetForm("KNJP853.frm", 1);

            //ヘッダ部出力
            printHeader(svf, schInfo);

            //文面出力
            printDocument(svf);

           //口座振替合計額欄ヘッダ出力
            printGoukeiHeader(svf, limitDate);

            //口座振替合計額欄出力
            printGoukei(svf, lmInfoMap);

            //注釈出力
            printNote(svf);

            //口座振替内訳出力
            printUtiwake(svf, lmInfoMap);

            svf.VrEndPage();
            _hasData = true;
        }
    }

    private void printHeader(final Vrw32alp svf, final SchInfo schInfo) {
        //帳票日付
        svf.VrsOut("DATE", convertDate(_param._printDate));

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
        svf.VrsOut("SCHOOL_NAME", schoolName);
        //場所名
        final String placeName = StringUtils.defaultIfEmpty(_param._certifSchool._placeName, "");
        svf.VrsOut("PLACE_NAME", placeName);
        //校長名
        final String telNo = StringUtils.defaultIfEmpty(_param._certifSchool._telNo, "");
        svf.VrsOut("TEL_NO", telNo);
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

    private void printGoukeiHeader(final Vrw32alp svf, String limitDate) {
        final String payPlanDate = convertDateStrFormat(limitDate, "yyyy-mm-dd", "yyyy年m月d日");
        final String headerStr   = "【" + payPlanDate + " 口座振替金額" + "】";
        svf.VrsOut("PAY_PLAN_DATE", headerStr);
    }

    //日付フォーマット変換
    private String convertDateStrFormat(final String dateStr, final String format1, final String format2) {
        SimpleDateFormat sdf1 = new SimpleDateFormat(format1);
        SimpleDateFormat sdf2 = new SimpleDateFormat(format2);
        String convertedDateStr = "";
        try {
            Date parsedDate = sdf1.parse(dateStr);
            convertedDateStr =  sdf2.format(parsedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return convertedDateStr;
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

    private void printGoukei(final Vrw32alp svf, Map lmInfoMap) {
        boolean dataFlg = false;
        Integer totalMoney = 0;
        final Map otherMap = new LinkedHashMap();//課外費以外の項目
        final Map goukeiMap = new LinkedHashMap();//課外費項目

        for (final Iterator niter1 = lmInfoMap.keySet().iterator(); niter1.hasNext();) {
            final String lmCd   = (String)niter1.next();
            final LmInfo lmInfo = (LmInfo)lmInfoMap.get(lmCd);
            final String combinedLCd = lmInfo._combinedLCd;

            final Map tmpMap = (KOUNOUKIN_CD.equals(combinedLCd)) ? otherMap : goukeiMap;
            if (!tmpMap.containsKey(combinedLCd)) {
                tmpMap.put(combinedLCd, 0);
            }
            Integer calcGoukeiMoney = (Integer)tmpMap.get(combinedLCd);
            calcGoukeiMoney += lmInfo._planMoney;
            tmpMap.put(combinedLCd, calcGoukeiMoney);

            totalMoney += lmInfo._planMoney;
            dataFlg = true;
        }

        //各項目合計表示
        svf.VrsOut("MONEY_TITLE1", "校納金等");
        final String money1 = (otherMap.containsKey(KOUNOUKIN_CD)) ? ((Integer)otherMap.get(KOUNOUKIN_CD)).toString() : "-";
        svf.VrsOut("MONEY1", money1);

        for (final Iterator niter1 = goukeiMap.keySet().iterator(); niter1.hasNext();) {
            final String combinedLCd   = (String)niter1.next();
        }

        int colNum = 2;
        for (final Iterator niter1 = _param._p013MstMap.keySet().iterator(); niter1.hasNext();) {
            final String combinedLCd   = (String)niter1.next();
            final String combinedLName = (String)_param._p013MstMap.get(combinedLCd);

            svf.VrsOut("MONEY_TITLE" + colNum, combinedLName);
            if (goukeiMap.containsKey(combinedLCd)) {
                final Integer money2_6 = (Integer)goukeiMap.get(combinedLCd);
                svf.VrsOut("MONEY" + colNum, money2_6.toString());
            }
            colNum++;
        }

        //最終合計額
        if (dataFlg) {
            svf.VrsOut("TOTAL_MONEY", totalMoney.toString());
        }
    }

    private void printNote(final Vrw32alp svf) {

        //注意書1行目
        svf.VrsOutn("NOTE", 1, _param._certifSchool._note1);
        //注意書2行目
        svf.VrsOutn("NOTE", 2, _param._certifSchool._note2);
    }

    private void printUtiwake(final Vrw32alp svf, Map lmInfoMap) {
        int lmCnt = 1;
        Integer utiwakeTotal = 0;
        for (final Iterator niter1 = lmInfoMap.keySet().iterator(); niter1.hasNext();) {
            final String lmCd   = (String)niter1.next();
            final LmInfo lmInfo = (LmInfo)lmInfoMap.get(lmCd);
            final String combinedLCd = lmInfo._combinedLCd;

            //課外費項目以外は内訳に表示しない
            if (KOUNOUKIN_CD.equals(combinedLCd)) {
                continue;
            }

            //20行目を超えた場合無視
            if (lmCnt > UTIWAKE_MAX_CNT) {
                continue;
            }

            //項目名
            svf.VrsOutn("ITEM1", lmCnt, lmInfo._combinedLName);
            //内容
            svf.VrsOutn("ITEM2", lmCnt, lmInfo._mName);
            //金額
            svf.VrsOutn("SUBTOTAL_MONEY", lmCnt, String.valueOf(lmInfo._planMoney));

            utiwakeTotal += lmInfo._planMoney;
            lmCnt++;
        }

        if (lmCnt > 0) {
            //合計金額
            svf.VrsOutn("SUBTOTAL_MONEY", 21, utiwakeTotal.toString());
        }

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
       Map _schregCollectMap    = new TreeMap();
       List _outputSchregNoList = new ArrayList(); //名称マスタP013で設定した課外費項目の情報をもつ学籍番号のリスト
       Map _limitDateMap        = new TreeMap();

       private String getCollectSql() {
           final StringBuffer stb = new StringBuffer();
           stb.append("  WITH MAX_SEMES AS ( ");
           stb.append("      SELECT ");
           stb.append("          SCHREGNO, ");
           stb.append("          YEAR, ");
           stb.append("          MAX(SEMESTER) AS SEMESTER ");
           stb.append("      FROM ");
           stb.append("          SCHREG_REGD_DAT ");
           stb.append("      GROUP BY ");
           stb.append("          SCHREGNO, ");
           stb.append("          YEAR ");
           stb.append("  ), REGD_DATA AS ( ");
           stb.append("      SELECT ");
           stb.append("          REGD.* ");
           stb.append("      FROM ");
           stb.append("          SCHREG_REGD_DAT REGD ");
           stb.append("          INNER JOIN MAX_SEMES SEM ");
           stb.append("              ON SEM.SCHREGNO   = REGD.SCHREGNO ");
           stb.append("              AND SEM.YEAR      = REGD.YEAR ");
           stb.append("              AND SEM.SEMESTER  = REGD.SEMESTER ");
           stb.append("      WHERE ");
           stb.append("          REGD.YEAR = '" + _param._ctrlYear + "' ");
           stb.append("          AND REGD.GRADE || '-' || REGD.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categorySelected));
           stb.append("  ), COLLECT_DATA AS ( ");
           stb.append("      SELECT ");
           stb.append("          PLAN_M.SCHOOLCD, ");
           stb.append("          PLAN_M.SCHOOL_KIND, ");
           stb.append("          PLAN_M.YEAR, ");
           stb.append("          PLAN_M.SCHREGNO, ");
           stb.append("          PLAN_M.COLLECT_L_CD, ");
           stb.append("          PLAN_M.COLLECT_M_CD, ");
           stb.append("          SUM(PLAN_M.PLAN_MONEY) AS PLAN_MONEY, ");
           stb.append("          SUM( VALUE(CASE WHEN MMST.REDUCTION_DIV = '1' AND REDUC_C.OFFSET_FLG = '1' THEN REDUC_C.DECISION_MONEY ELSE 0 END, 0) + ");
           stb.append("          VALUE(CASE WHEN MMST.REDUCTION_DIV = '1' AND REDUC_C.ADD_OFFSET_FLG = '1' THEN REDUC_C.ADD_DECISION_MONEY ELSE 0 END, 0) + ");
           stb.append("          VALUE(CASE WHEN REDUC_D.OFFSET_FLG = '1' THEN REDUC_D.DECISION_MONEY ELSE 0 END, 0) + ");
           stb.append("          VALUE(BURDEN.BURDEN_CHARGE, 0) + ");
           stb.append("          VALUE(CASE WHEN SCHOOL_P.OFFSET_FLG = '1' THEN SCHOOL_P.DECISION_MONEY ELSE 0 END, 0) ");
           stb.append("          ) AS REDUCE_MONEY, ");
           stb.append("          MIN(LIMIT.PAID_LIMIT_DATE) AS PAID_LIMIT_DATE ");
           stb.append("      FROM ");
           stb.append("          COLLECT_SLIP_PLAN_M_DAT PLAN_M ");
           stb.append("          INNER JOIN REGD_DATA REGD ");
           stb.append("              ON REGD.SCHREGNO = PLAN_M.SCHREGNO ");
           stb.append("          INNER JOIN COLLECT_SLIP_DAT SL_D ");
           stb.append("              ON SL_D.SCHOOLCD = PLAN_M.SCHOOLCD ");
           stb.append("              AND SL_D.SCHOOL_KIND = PLAN_M.SCHOOL_KIND ");
           stb.append("              AND SL_D.YEAR = PLAN_M.YEAR ");
           stb.append("              AND SL_D.SCHREGNO = PLAN_M.SCHREGNO ");
           stb.append("              AND SL_D.SLIP_NO = PLAN_M.SLIP_NO ");
           stb.append("          LEFT JOIN COLLECT_SLIP_PLAN_LIMITDATE_DAT LIMIT ");
           stb.append("              ON LIMIT.SCHOOLCD = PLAN_M.SCHOOLCD ");
           stb.append("              AND LIMIT.SCHOOL_KIND = PLAN_M.SCHOOL_KIND ");
           stb.append("              AND LIMIT.YEAR        = PLAN_M.YEAR ");
           stb.append("              AND LIMIT.SCHREGNO    = PLAN_M.SCHREGNO ");
           stb.append("              AND LIMIT.SLIP_NO     = PLAN_M.SLIP_NO ");
           stb.append("              AND LIMIT.PLAN_YEAR   = PLAN_M.PLAN_YEAR ");
           stb.append("              AND LIMIT.PLAN_MONTH  = PLAN_M.PLAN_MONTH ");
           stb.append("          LEFT JOIN COLLECT_M_MST MMST ");
           stb.append("              ON MMST.SCHOOLCD        = PLAN_M.SCHOOLCD ");
           stb.append("              AND MMST.SCHOOL_KIND    = PLAN_M.SCHOOL_KIND ");
           stb.append("              AND MMST.YEAR           = PLAN_M.YEAR ");
           stb.append("              AND MMST.COLLECT_L_CD   = PLAN_M.COLLECT_L_CD ");
           stb.append("              AND MMST.COLLECT_M_CD   = PLAN_M.COLLECT_M_CD ");
           stb.append("          LEFT JOIN REDUCTION_COUNTRY_PLAN_DAT REDUC_C ");
           stb.append("              ON REDUC_C.SCHOOLCD     = PLAN_M.SCHOOLCD ");
           stb.append("              AND REDUC_C.SCHOOL_KIND = PLAN_M.SCHOOL_KIND ");
           stb.append("              AND REDUC_C.YEAR        = PLAN_M.YEAR ");
           stb.append("              AND REDUC_C.SCHREGNO    = PLAN_M.SCHREGNO ");
           stb.append("              AND REDUC_C.SLIP_NO     = PLAN_M.SLIP_NO ");
           stb.append("              AND REDUC_C.PLAN_YEAR   = PLAN_M.PLAN_YEAR ");
           stb.append("              AND REDUC_C.PLAN_MONTH  = PLAN_M.PLAN_MONTH ");
           stb.append("          LEFT JOIN REDUCTION_PLAN_DAT REDUC_D ");
           stb.append("              ON REDUC_D.SCHOOLCD     = PLAN_M.SCHOOLCD ");
           stb.append("              AND REDUC_D.SCHOOL_KIND = PLAN_M.SCHOOL_KIND ");
           stb.append("              AND REDUC_D.YEAR        = PLAN_M.YEAR ");
           stb.append("              AND REDUC_D.REDUCTION_TARGET = MMST.GAKUNOKIN_DIV ");
           stb.append("              AND REDUC_D.SCHREGNO    = PLAN_M.SCHREGNO ");
           stb.append("              AND REDUC_D.SLIP_NO     = PLAN_M.SLIP_NO ");
           stb.append("              AND REDUC_D.PLAN_YEAR   = PLAN_M.PLAN_YEAR ");
           stb.append("              AND REDUC_D.PLAN_MONTH  = PLAN_M.PLAN_MONTH ");
           stb.append("          LEFT JOIN REDUCTION_BURDEN_CHARGE_PLAN_DAT BURDEN ");
           stb.append("              ON BURDEN.SCHOOLCD     = PLAN_M.SCHOOLCD ");
           stb.append("              AND BURDEN.SCHOOL_KIND = PLAN_M.SCHOOL_KIND ");
           stb.append("              AND BURDEN.YEAR        = PLAN_M.YEAR ");
           stb.append("              AND BURDEN.REDUCTION_TARGET =  MMST.GAKUNOKIN_DIV ");
           stb.append("              AND BURDEN.SCHREGNO    = PLAN_M.SCHREGNO ");
           stb.append("              AND BURDEN.SLIP_NO     = PLAN_M.SLIP_NO ");
           stb.append("              AND BURDEN.PLAN_YEAR   = PLAN_M.PLAN_YEAR ");
           stb.append("              AND BURDEN.PLAN_MONTH  = PLAN_M.PLAN_MONTH ");
           stb.append("          LEFT JOIN REDUCTION_SCHOOL_PLAN_DAT SCHOOL_P ");
           stb.append("              ON SCHOOL_P.SCHOOLCD     = PLAN_M.SCHOOLCD ");
           stb.append("              AND SCHOOL_P.SCHOOL_KIND = PLAN_M.SCHOOL_KIND ");
           stb.append("              AND SCHOOL_P.YEAR        = PLAN_M.YEAR ");
           stb.append("              AND SCHOOL_P.REDUCTION_TARGET =  MMST.GAKUNOKIN_DIV ");
           stb.append("              AND SCHOOL_P.SCHREGNO    = PLAN_M.SCHREGNO ");
           stb.append("              AND SCHOOL_P.SLIP_NO     = PLAN_M.SLIP_NO ");
           stb.append("              AND SCHOOL_P.PLAN_YEAR   = PLAN_M.PLAN_YEAR ");
           stb.append("              AND SCHOOL_P.PLAN_MONTH  = PLAN_M.PLAN_MONTH ");
           stb.append("      WHERE ");
           stb.append("      	 PLAN_M.SCHOOLCD        = '" + _param._schoolcd + "' ");
           stb.append("      	 AND PLAN_M.SCHOOL_KIND = '" + _param._schoolKind + "' ");
           stb.append("      	 AND PLAN_M.YEAR        = '" + _param._ctrlYear + "' ");
           stb.append("      	 AND PLAN_M.PLAN_YEAR   = '" + _param._collectYear + "' ");
           stb.append("      	 AND PLAN_M.PLAN_MONTH  = '" + _param._collectMonth + "' ");
           stb.append("      	 AND PLAN_M.PAID_MONEY IS NULL ");
           stb.append("      GROUP BY ");
           stb.append("          PLAN_M.SCHOOLCD, ");
           stb.append("          PLAN_M.SCHOOL_KIND, ");
           stb.append("          PLAN_M.YEAR, ");
           stb.append("          PLAN_M.SCHREGNO, ");
           stb.append("          PLAN_M.COLLECT_L_CD, ");
           stb.append("          PLAN_M.COLLECT_M_CD ");
           stb.append("  ) ");
           stb.append("  SELECT ");
           stb.append("      COLLECT.SCHREGNO, ");
           stb.append("      VALUE (P013.NAMECD2, '999') AS COMBINED_L_CD, ");
           stb.append("      COLLECT.COLLECT_L_CD, ");
           stb.append("      COLLECT.COLLECT_M_CD, ");
           stb.append("      MMST.COLLECT_M_NAME, ");
           stb.append("      COLLECT.PLAN_MONEY, ");
           stb.append("      COLLECT.REDUCE_MONEY, ");
           stb.append("      COLLECT.PLAN_MONEY - COLLECT.REDUCE_MONEY AS REDUCED_PLAN_MONEY, ");
           stb.append("      COLLECT.PAID_LIMIT_DATE ");
           stb.append("  FROM ");
           stb.append("      COLLECT_DATA COLLECT ");
           stb.append("      LEFT JOIN V_NAME_MST P013 ");
           stb.append("           ON P013.YEAR    = COLLECT.YEAR ");
           stb.append("          AND P013.NAMECD1 = 'P013' ");
           stb.append("          AND COLLECT.COLLECT_L_CD || COLLECT.COLLECT_M_CD BETWEEN P013.NAMESPARE1 AND P013.NAMESPARE2 ");
           stb.append("      LEFT JOIN COLLECT_M_MST MMST ");
           stb.append("           ON MMST.SCHOOLCD     = COLLECT.SCHOOLCD ");
           stb.append("          AND MMST.SCHOOL_KIND  = COLLECT.SCHOOL_KIND ");
           stb.append("          AND MMST.YEAR         = COLLECT.YEAR ");
           stb.append("          AND MMST.COLLECT_L_CD = COLLECT.COLLECT_L_CD ");
           stb.append("          AND MMST.COLLECT_M_CD = COLLECT.COLLECT_M_CD ");
           stb.append("  WHERE ");
           stb.append("      COLLECT.PLAN_MONEY - COLLECT.REDUCE_MONEY > 0 ");
           stb.append("  ORDER BY ");
           stb.append("      INT(COMBINED_L_CD), ");
           stb.append("      COLLECT.COLLECT_L_CD, ");
           stb.append("      COLLECT.COLLECT_M_CD ");

           return stb.toString();
       }

       public void loadCollectData(final DB2UDB db2) {
           String sql = getCollectSql();
           log.fatal("collect sql = " + sql);
           try {
               PreparedStatement ps = db2.prepareStatement(sql);
               ResultSet rs = ps.executeQuery();
               while (rs.next()) {
                   final String  schregno      = rs.getString("SCHREGNO");
                   final String  combinedLCd   = rs.getString("COMBINED_L_CD");
                   final String  lCd 	       = rs.getString("COLLECT_L_CD");
                   final String  mCd 	       = rs.getString("COLLECT_M_CD");
                   final String  mName         = rs.getString("COLLECT_M_NAME");
                   Integer planMoney           = Integer.valueOf(StringUtils.defaultIfEmpty(rs.getString("PLAN_MONEY"), "0"));
                   final String  limitDate     = rs.getString("PAID_LIMIT_DATE");

                   //名称マスタP014で設定された除外項目をスキップ
                   if (_param._removeLMCdMap.containsKey(lCd + mCd)) {
                       continue;
                   }

                   //学籍番号毎に項目リストを取得
                   if (!_schregCollectMap.containsKey(schregno)) {
                       _schregCollectMap.put(schregno, new LinkedHashMap());
                   }
                   final Map lmInfoMap = (Map)_schregCollectMap.get(schregno);

                   //科目名取得(科目マスタから取得したものではなく、P013で設定した範囲の中で一番最初の科目コードの名称を取得)
                   final String combinedLName = (String)_param._p013MstMap.get(combinedLCd);

                   //引落予定日を取得
                   _limitDateMap.put(schregno, limitDate);

                   //名称マスタP013で設定した範囲の入金項目をもつ学籍番号を保持
                   if (!KOUNOUKIN_CD.equals(combinedLCd)) {
                       _outputSchregNoList.add(schregno);
                   }

                   //項目を追加
                   final LmInfo lmInfo = new LmInfo(combinedLCd, combinedLName, lCd, mCd, mName, planMoney);
                   lmInfoMap.put(lCd + mCd, lmInfo);
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
               stb.append("  AND REGD.GRADE || '-' || REGD.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categorySelected));
           if (!"1".equals(_param._outputDiv)) {
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
            _name      = name;
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

    //項目クラス
    class LmInfo {
        final String _combinedLCd;
        final String _combinedLName;
        final String _lCd;
        final String _mCd;
        final String _mName;
        final Integer _planMoney;

        public LmInfo(final String combinedLCd, final String combinedLName, final String lCd, final String mCd, final String mName, final Integer planMoney) {
            _combinedLCd   = combinedLCd;
            _lCd           = lCd;
            _combinedLName = combinedLName;
            _mCd           = mCd;
            _mName         = mName;
            _planMoney     = planMoney;
        }
    }

    //証明書学校データ
    private class CertifSchool {
        final String _schoolName;
        final String _placeName;
        final String _telNo;
        final String _note1;
        final String _note2;
        public CertifSchool(
                final String schoolName,
                final String placeName,
                final String telNo,
                final String note1,
                final String note2
        ) {
            _schoolName  = schoolName;
            _placeName   = placeName;
            _telNo       = telNo;
            _note1       = note1;
            _note2       = note2;
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

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 75206 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String[] _categorySelected;
        final String _grade;
        final String _schoolKind;
        final String _sOrderFlg;
        final String _hrClass;
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _loginDate;
        final String _schoolcd;
        final String _prgid;
        final String _outputDiv;
        final String _collectYear;
        final String _collectMonth;
        final String _addrDiv;
        final String _printDate;
        final String _documentCd;
        final CertifSchool _certifSchool;
        final DocumentMst _documentMst;
        final int _textMoji;
        final int _textGyou;
        final Map _p013MstMap;
        final Map _removeLMCdMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            final String[] orderwk = StringUtils.split(StringUtils.defaultString(request.getParameter("GRADE"), ""), "-");
            if (orderwk.length > 1) {
                _grade = orderwk[0];
                _sOrderFlg = orderwk[1];
            } else {
                //区切り文字が無い場合は、今まで通りの動作とする。
                _grade = request.getParameter("GRADE");
                _sOrderFlg = "0";
            }
            _ctrlYear   = request.getParameter("YEAR");
            _schoolKind = getSchoolKind(db2);
            _hrClass    = request.getParameter("HR_CLASS");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _loginDate  = request.getParameter("LOGIN_DATE");
            _schoolcd   = request.getParameter("SCHOOLCD");
            _prgid      = request.getParameter("PRGID");
            _collectMonth = request.getParameter("COLLECT_MONTH");
            _collectYear = (Integer.parseInt(_collectMonth) <= 3) ? String.valueOf(Integer.parseInt(_ctrlYear)) + 1 : _ctrlYear;
            _outputDiv   = request.getParameter("OUTPUT_DIV");
            _addrDiv     = request.getParameter("ADDRESS_DIV");
            String tmpDate = request.getParameter("PRINT_DATE");
            _printDate  = tmpDate.replace("/", "-");
            _documentCd = request.getParameter("DOCUMENTCD");
            _textMoji = Integer.valueOf(request.getParameter("TEXT_MOJI"));
            _textGyou = Integer.valueOf(request.getParameter("TEXT_GYOU"));

            _certifSchool  = getCertifSchool(db2);
            _documentMst   = getDocumentMst(db2);
            _p013MstMap    = getNameMstP013(db2);
            _removeLMCdMap = getRemoveLMCdMap(db2);
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
            CertifSchool certifSchool = new CertifSchool("", "", "", "", "");
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {

                ps = db2.prepareStatement("SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _ctrlYear + "' AND CERTIF_KINDCD = '150' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    final String schoolName     = rs.getString("SCHOOL_NAME");
                    final String placeName      = rs.getString("REMARK3");
                    final String telNo          = rs.getString("REMARK4");
                    final String note1          = rs.getString("REMARK5");
                    final String note2          = rs.getString("REMARK6");
                    certifSchool = new CertifSchool(schoolName, placeName, telNo, note1, note2);
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

        //名称マスタP013で設定したFrom～Toの範囲で1科目とみなす。科目名を取得する際は複数科目の中で一番科目コードが若いものの名称を取得
        private Map getNameMstP013(final DB2UDB db2) {
            Map rtnMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
               final StringBuffer stb = new StringBuffer();
               stb.append("      SELECT ");
               stb.append("          COMBINED_L_CD, ");
               stb.append("          COLLECT_L_NAME AS COMBINED_L_NAME ");
               stb.append("      FROM ( ");
               stb.append("          SELECT ");
               stb.append("              LMST.SCHOOLCD, ");
               stb.append("              LMST.SCHOOL_KIND, ");
               stb.append("              LMST.YEAR, ");
               stb.append("              P013.NAMECD2 AS COMBINED_L_CD, ");
               stb.append("              ROW_NUMBER() OVER(PARTITION BY P013.NAMECD2 ORDER BY MMST.COLLECT_L_CD) AS ROWNUM, ");
               stb.append("              LMST.COLLECT_L_NAME ");
               stb.append("          FROM ");
               stb.append("              COLLECT_M_MST MMST ");
               stb.append("              LEFT JOIN COLLECT_L_MST LMST ");
               stb.append("                   ON LMST.SCHOOLCD     = MMST.SCHOOLCD ");
               stb.append("                  AND LMST.SCHOOL_KIND  = MMST.SCHOOL_KIND ");
               stb.append("                  AND LMST.YEAR         = MMST.YEAR ");
               stb.append("                  AND LMST.COLLECT_L_CD = MMST.COLLECT_L_CD ");
               stb.append("              INNER JOIN V_NAME_MST P013 ");
               stb.append("                   ON P013.YEAR    = MMST.YEAR ");
               stb.append("                  AND P013.NAMECD1 = 'P013' ");
               stb.append("                  AND MMST.COLLECT_L_CD || MMST.COLLECT_M_CD BETWEEN P013.NAMESPARE1 AND P013.NAMESPARE2 ");
               stb.append("          WHERE ");
               stb.append("              LMST.SCHOOLCD        = '" + _schoolcd + "' ");
               stb.append("              AND LMST.SCHOOL_KIND = '" + _schoolKind + "' ");
               stb.append("              AND LMST.YEAR        = '" + _ctrlYear + "' ");
               stb.append("      ) ");
               stb.append("      WHERE ");
               stb.append("          ROWNUM = '1' ");
               final String sql = stb.toString();
                log.fatal("P013 sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String combinedLCd   = rs.getString("COMBINED_L_CD");
                    final String combinedLName = rs.getString("COMBINED_L_NAME");
                    rtnMap.put(combinedLCd, combinedLName);
                }
            } catch (SQLException ex) {
                log.debug("NAME_MST P014 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtnMap;
        }

        //名称マスタP014(除外項目)
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
                stb.append("     AND NAMECD1 = 'P014' ");
                stb.append("     AND NAME1 IS NOT NULL ");

                final String sql = stb.toString();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String rmLMCd = rs.getString("RM_LM_CD");
                    rtnMap.put(rmLMCd, "");
                }
            } catch (SQLException ex) {
                log.debug("NAME_MST P014 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtnMap;
        }

    }
}

// eof
