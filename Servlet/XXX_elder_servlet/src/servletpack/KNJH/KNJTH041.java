/*
 * $Id: 93cdb8bf262a3c0fea9543ace871fe083712a3bc $
 *
 * 作成日: 2014/09/29
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJH;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.ShugakuDate;

public class KNJTH041 {

    private static final Log log = LogFactory.getLog(KNJTH041.class);

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
    
    private static int getMS932Length(final String s) {
        if (null != s) {
            try {
                return s.getBytes("MS932").length;
            } catch (Exception e) {
                log.fatal("exception!", e);
            }
        }
        return 0;
    }

    private List getPageList(final List printList, final int count) {
        final List pageList = new ArrayList();
        List current = null;
        for (final Iterator it = printList.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= count) {
                current = new ArrayList();
                pageList.add(current);
            }
            current.add(o);
        }
        return pageList;
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final String dataComb;
        if ("1".equals(_param._centerTitle)) {
            dataComb = "申請年度○、カナ氏名○、生年月日×";
        } else if ("2".equals(_param._centerTitle)) {
            dataComb = "申請年度○、カナ氏名×、生年月日○";
        } else if ("3".equals(_param._centerTitle)) {
            dataComb = "申請年度○、カナ氏名×、生年月日×";
        } else { // if ("0".equals(_CENTER_TITLE)) {
            dataComb = "申請年度○、カナ氏名○、生年月日○";
        }
        final int maxLine = 50;

        final List list = getPrintDataList(db2);
        final List pageList = getPageList(list, maxLine);
        
        for (int pi = 0; pi < pageList.size(); pi++) {
            final List printList = (List) pageList.get(pi);
            svf.VrSetForm("KNJTH041.frm", 1);
            
            svf.VrsOut("TITLE", "その他併給者の突合せ処理"); // タイトル
            svf.VrsOut("SUB_TITLE", "（" + dataComb + "）"); // サブタイトル
            if ("2".equals(_param._sonotaDiv)) {
                svf.VrsOut("SELECT", "母子家庭奨学金"); // 選択
            } else { // if ("1".equals(_param._sonotaDiv)) {
                svf.VrsOut("SELECT", "高校生給付型奨学金"); // 選択
            }
            svf.VrsOut("PAGE", String.valueOf(pi + 1) + "/" + String.valueOf(pageList.size()));
            svf.VrsOut("DATE", _param._shugakuDate.formatDate(_param._ctrlDate, false)); // 日付
            
            for (int li = 0, max = printList.size(); li < max; li++) {
                final int line = li + 1;
                final PrintData printData = (PrintData) printList.get(li);
                svf.VrsOutn("CONFLICT", line, printData._isError ? "●" : ""); // 重複
                
                svf.VrsOutn("NO2", line, null != printData._seiriNo ? printData._seiriNo.replace(':', '-') : printData._seiriNo); // 個人番号
                svf.VrsOutn("NAME2_" + String.valueOf(getMS932Length(printData._name2) > 30 ? "3" : getMS932Length(printData._name2) > 20 ? "2" : "1"), line, printData._name2); // 生徒氏名
                svf.VrsOutn("KANA2_" + String.valueOf(getMS932Length(printData._kana2) > 30 ? "3" : getMS932Length(printData._kana2) > 20 ? "2" : "1"), line, printData._kana2); // 生徒氏名かな
                svf.VrsOutn("SCHOOLNAME2_" + String.valueOf(getMS932Length(printData._schoolname2) > 20 ? "3" : getMS932Length(printData._schoolname2) > 14 ? "2" : "1"), line, printData._schoolname2); // 生徒氏名
                svf.VrsOutn("BIRTHDAY2", line, _param._shugakuDate.formatDateMarkDot(printData._birthday2)); // 生年月日
                svf.VrsOutn("UPDATE", line, _param._shugakuDate.formatDateMarkDot(printData._updated2)); // 整理番号

                if ("3".equals(_param._centerTitle) && printData._isError) {
                    svf.VrsOutn("NAME1_1", line, "重複データあり");
                } else {
                    svf.VrsOutn("NO1", line, printData._kojinNo); // 個人番号
                    svf.VrsOutn("NAME1_" + String.valueOf(getMS932Length(printData._name1) > 30 ? "3" : getMS932Length(printData._name1) > 20 ? "2" : "1"), line, printData._name1); // 生徒氏名
                    svf.VrsOutn("KANA1_" + String.valueOf(getMS932Length(printData._kana1) > 30 ? "3" : getMS932Length(printData._kana1) > 20 ? "2" : "1"), line, printData._kana1); // 生徒氏名かな
                    svf.VrsOutn("SCHOOLNAME1_" + String.valueOf(getMS932Length(printData._schoolname1) > 20 ? "3" : getMS932Length(printData._schoolname1) > 14 ? "2" : "1"), line, printData._schoolname1); // 生徒氏名
                    svf.VrsOutn("BIRTHDAY1", line, _param._shugakuDate.formatDateMarkDot(printData._birthday1)); // 生年月日
                }
                _hasData = true;
            }
            
            svf.VrEndPage();
        }
    }
    
    public List getPrintDataList(final DB2UDB db2) {
        final List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getDataSql();
            log.debug(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String cnt = rs.getString("CNT");
                final String kojinNo = rs.getString("KOJIN_NO");
                final String shuugakuNo = rs.getString("SHUUGAKU_NO");
                final String seiriNo = rs.getString("SEIRI_NO");
                final String shinseiYear1 = rs.getString("SHINSEI_YEAR1");
                final String name1 = rs.getString("NAME1");
                final String kana1 = rs.getString("KANA1");
                final String schoolname1 = rs.getString("SCHOOL_NAME1");
                final String birthday1 = rs.getString("BIRTHDAY1");
                final String shinseiYear2 = rs.getString("SHINSEI_YEAR2");
                final String name2 = rs.getString("NAME2");
                final String kana2 = rs.getString("KANA2");
                final String schoolname2 = rs.getString("SCHOOL_NAME2");
                final String birthday2 = rs.getString("BIRTHDAY2");
                final String kakuteiFlg = rs.getString("KAKUTEI_FLG");
                final String updated2 = rs.getString("UPDATED2");
                final boolean isError = "1".equals(rs.getString("ERROR"));
                final PrintData printdata = new PrintData(cnt, kojinNo, shuugakuNo, seiriNo, shinseiYear1, name1, kana1, schoolname1, birthday1, shinseiYear2, name2, kana2, schoolname2, birthday2, kakuteiFlg, updated2, isError);
                list.add(printdata);
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;
    }

    private String selectTWhere(final String tn) {
        final String selectTWhere;
        if ("1".equals(_param._centerTitle)) {
            //申請年度○、カナ氏名○、生年月日×
            selectTWhere = "T1.SHINSEI_YEAR2 = " + tn +".SHINSEI_YEAR1 AND T1.KANA2 = " + tn +".KANA1 AND T1.BIRTHDAY2 != " + tn +".BIRTHDAY1";
        } else if ("2".equals(_param._centerTitle)) {
          //申請年度○、カナ氏名×、生年月日○
          selectTWhere = "T1.SHINSEI_YEAR2 = " + tn +".SHINSEI_YEAR1 AND T1.KANA2 != " + tn +".KANA1 AND T1.BIRTHDAY2 = " + tn +".BIRTHDAY1";
        } else if ("3".equals(_param._centerTitle)) {
          //申請年度○、カナ氏名×、生年月日×
          selectTWhere = "T1.SHINSEI_YEAR2 = " + tn +".SHINSEI_YEAR1 AND T1.KANA2 != " + tn +".KANA1 AND T1.BIRTHDAY2 != " + tn +".BIRTHDAY1";
        } else { // if ("0".equals(_CENTER_TITLE)) {
          //申請年度○、カナ氏名○、生年月日○
          selectTWhere = "T1.SHINSEI_YEAR2 = " + tn +".SHINSEI_YEAR1 AND T1.KANA2 = " + tn +".KANA1 AND T1.BIRTHDAY2 = " + tn +".BIRTHDAY1";
        }
        return selectTWhere;
    }

    //リストデータ
    private String getDataSql()
    {
        final boolean printErrorDetail = !"3".equals(_param._centerTitle); // 「申請年度○、カナ氏名○、生年月日○」以外は詳細を出力する
        final StringBuffer stb = new StringBuffer();
        stb.append("  WITH MXSEQ_KSKDAT AS ( ");
        stb.append("    SELECT ");
        stb.append("      T0.* ");
        stb.append("    FROM ");
        stb.append("     KOJIN_SHINSEI_KYUHU_DAT T0 ");
        stb.append("    WHERE ");
        stb.append("      T0.SEQ = (SELECT MAX(J0.SEQ) FROM KOJIN_SHINSEI_KYUHU_DAT J0 WHERE J0.KOJIN_NO = T0.KOJIN_NO AND J0.SHINSEI_YEAR = T0.SHINSEI_YEAR) ");
        stb.append(" ), JOIN_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.KOJIN_NO, ");
        stb.append("     T1.SHUUGAKU_NO, ");
        stb.append("     VALUE(L1.FAMILY_NAME,'') || '　' || VALUE(L1.FIRST_NAME,'') AS NAME1, ");
        stb.append("     VALUE(T1.SHINSEI_YEAR,'') AS SHINSEI_YEAR1, ");
        stb.append("     TRANSLATE_HK_K(VALUE(L1.FAMILY_NAME_KANA,'') || VALUE(L1.FIRST_NAME_KANA,'')) AS KANA1, ");
        stb.append("     L2.NAME AS SCHOOL_NAME1, ");
        stb.append("     L1.BIRTHDAY AS BIRTHDAY1 ");
        stb.append(" FROM ");
        stb.append("     MXSEQ_KSKDAT T1 ");
        stb.append("     INNER JOIN V_KOJIN_HIST_DAT L1 ON T1.KOJIN_NO = L1.KOJIN_NO ");
        stb.append("     LEFT JOIN SCHOOL_DAT L2 ON L2.SCHOOLCD = T1.H_SCHOOL_CD ");
        stb.append(" WHERE ");
        stb.append("     T1.SHINSEI_YEAR = '" + _param._year + "' ");
        stb.append(" ), MAIN_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SEIRI_NO, ");
        stb.append("     T1.KOJIN_NO, ");
        stb.append("     VALUE(T1.SHINSEI_YEAR,'') AS SHINSEI_YEAR2, ");
        stb.append("     VALUE(T1.KAKUTEI_FLG, '0') AS KAKUTEI_FLG, ");
        stb.append("     VALUE(T1.FAMILY_NAME,'') || '　' || VALUE(T1.FIRST_NAME,'') AS NAME2, ");
        stb.append("     TRANSLATE_HK_K(VALUE(T1.FAMILY_NAME_KANA,'') || VALUE(T1.FIRST_NAME_KANA,'')) AS KANA2, ");
        stb.append("     T1.SCHOOL_NAME AS SCHOOL_NAME2, ");
        stb.append("     T1.BIRTHDAY AS BIRTHDAY2, ");
        stb.append("     DATE(T1.UPDATED) AS UPDATED2 ");
        stb.append(" FROM ");
        stb.append("     KYUHU_SONOTA_CSV_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.SONOTA_DIV = '" + _param._sonotaDiv + "' ");
        if ("1".equals(_param._outputApt)) {
            stb.append("     AND VALUE(T1.KOJIN_NO, '') = '' ");
        } else {
            stb.append("     AND VALUE(T1.KOJIN_NO, '') != '' ");
            if ("1".equals(_param._outputDiv)) {
                stb.append("     AND VALUE(T1.KAKUTEI_FLG, '0') = '0' ");
            }
        }
        //重複給付申請データチェック
        stb.append("  ), SELECT_KOJIN_COUNT AS ( ");
        stb.append("  SELECT ");
        stb.append("     COUNT(*) AS KOJIN_CNT, ");
        stb.append("     T2.KOJIN_NO, ");
        stb.append("     T2.SHUUGAKU_NO, ");
        stb.append("     T2.NAME1, ");
        stb.append("     T2.SHINSEI_YEAR1, ");
        stb.append("     T2.KANA1, ");
        stb.append("     T2.SCHOOL_NAME1, ");
        stb.append("     T2.BIRTHDAY1 ");
        stb.append("  FROM ");
        stb.append("     MAIN_T T1  ");
        stb.append("     INNER JOIN JOIN_T T2 ON " + selectTWhere("T2") + " ");
        stb.append("  GROUP BY ");
        stb.append("     T2.KOJIN_NO, ");
        stb.append("     T2.SHUUGAKU_NO, ");
        stb.append("     T2.NAME1, ");
        stb.append("     T2.SHINSEI_YEAR1, ");
        stb.append("     T2.KANA1, ");
        stb.append("     T2.SCHOOL_NAME1, ");
        stb.append("     T2.BIRTHDAY1 ");
        stb.append(" ), SELECT_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     COUNT(*) AS CNT, ");
        stb.append("     T1.SEIRI_NO, ");
        stb.append("     T1.SHINSEI_YEAR2, ");
        stb.append("     T1.NAME2, ");
        stb.append("     T1.KANA2, ");
        stb.append("     T1.SCHOOL_NAME2, ");
        stb.append("     T1.BIRTHDAY2, ");
        stb.append("     T1.KAKUTEI_FLG, ");
        stb.append("     T1.UPDATED2, ");
        stb.append("     T1.KOJIN_NO, ");
        stb.append("     T3.KOJIN_NO AS CHECK_KOJIN_NO ");
        stb.append(" FROM ");
        stb.append("     MAIN_T T1 ");
        stb.append("     INNER JOIN JOIN_T T2 ON " + selectTWhere("T2") + " ");
        stb.append("     LEFT JOIN SELECT_KOJIN_COUNT T3 ON " + selectTWhere("T3") + " ");
        //stb.append("                                    AND T3.KOJIN_CNT = 1 ");
        //完全一致は除く(申請年度○、カナ氏名○、生年月日○以外を選択時)
        if (!"0".equals(_param._centerTitle)) {
            stb.append("     WHERE ");
            stb.append("         NOT EXISTS (SELECT ");
            stb.append("                         'X' ");
            stb.append("                     FROM ");
            stb.append("                         SELECT_KOJIN_COUNT E1 ");
            stb.append("                     WHERE ");
            stb.append("                         T1.SHINSEI_YEAR2 = E1.SHINSEI_YEAR1 AND ");
            stb.append("                         T1.KANA2 = E1.KANA1 AND ");
            stb.append("                         T1.BIRTHDAY2 = E1.BIRTHDAY1 ");
            stb.append("                     ) ");
        }
        ////申請年度○、カナ氏名×、生年月日×を選択時は他の条件は除く
        if ("3".equals(_param._centerTitle)) {
            stb.append("       AND NOT EXISTS (SELECT ");
            stb.append("                          'X' ");
            stb.append("                      FROM ");
            stb.append("                          SELECT_KOJIN_COUNT E2 ");
            stb.append("                      WHERE ");
            stb.append("                          T1.SHINSEI_YEAR2 = E2.SHINSEI_YEAR1 AND ");
            stb.append("                          T1.KANA2 <> E2.KANA1 AND ");
            stb.append("                          T1.BIRTHDAY2 = E2.BIRTHDAY1 ");
            stb.append("                      ) ");
            stb.append("       AND NOT EXISTS (SELECT ");
            stb.append("                          'X' ");
            stb.append("                      FROM ");
            stb.append("                          SELECT_KOJIN_COUNT E3 ");
            stb.append("                      WHERE ");
            stb.append("                          T1.SHINSEI_YEAR2 = E3.SHINSEI_YEAR1 AND ");
            stb.append("                          T1.KANA2 = E3.KANA1 AND ");
            stb.append("                          T1.BIRTHDAY2 <> E3.BIRTHDAY1 ");
            stb.append("                      ) ");
        }
        stb.append(" GROUP BY ");
        stb.append("     T1.SEIRI_NO, ");
        stb.append("     T1.SHINSEI_YEAR2, ");
        stb.append("     T1.NAME2, ");
        stb.append("     T1.KANA2, ");
        stb.append("     T1.SCHOOL_NAME2, ");
        stb.append("     T1.BIRTHDAY2, ");
        stb.append("     T1.KAKUTEI_FLG, ");
        stb.append("     T1.UPDATED2, ");
        stb.append("     T1.KOJIN_NO, ");
        stb.append("     T3.KOJIN_NO ");
        stb.append(" ) ");
        stb.append(" SELECT DISTINCT ");
        stb.append("     T1.CNT, ");
        stb.append("     CASE WHEN T1.CNT > 1 THEN 1 ELSE 0 END AS ERROR, ");
        stb.append("     T1.SEIRI_NO, ");
        stb.append("     T1.SHINSEI_YEAR2, ");
        stb.append("     T1.NAME2, ");
        stb.append("     T1.KANA2, ");
        stb.append("     T1.SCHOOL_NAME2, ");
        stb.append("     T1.BIRTHDAY2, ");
        stb.append("     T1.KAKUTEI_FLG, ");
        stb.append("     T1.UPDATED2, ");
        stb.append("     T2.KOJIN_NO, ");
        stb.append("     T2.SHUUGAKU_NO, ");
        stb.append("     T2.SHINSEI_YEAR1, ");
        stb.append("     T2.NAME1, ");
        stb.append("     T2.KANA1, ");
        stb.append("     T2.SCHOOL_NAME1, ");
        stb.append("     T2.BIRTHDAY1 ");
        stb.append(" FROM ");
        stb.append("     SELECT_T T1 ");
        stb.append("     INNER JOIN JOIN_T T2 ON " + selectTWhere("T2") + " ");
        stb.append(" WHERE ");
        if (printErrorDetail) {
            stb.append("     T1.CNT >= 1 "); // すべて出力
        } else {
            stb.append("     T1.CNT = 1 ");
        }
        if ("2".equals(_param._outputApt)) {
            stb.append("     AND T2.KOJIN_NO = T1.KOJIN_NO ");
        }
        stb.append("     AND T1.CHECK_KOJIN_NO IS NOT NULL ");
        //重複データは未突合の場合のみ
        if (!printErrorDetail && !"2".equals(_param._outputApt)) {
            stb.append(" UNION ALL ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.CNT, ");
            stb.append("     1 AS ERROR, ");
            stb.append("     T1.SEIRI_NO, ");
            stb.append("     T1.SHINSEI_YEAR2, ");
            stb.append("     T1.NAME2, ");
            stb.append("     T1.KANA2, ");
            stb.append("     T1.SCHOOL_NAME2, ");
            stb.append("     T1.BIRTHDAY2, ");
            stb.append("     T1.KAKUTEI_FLG, ");
            stb.append("     T1.UPDATED2, ");
            stb.append("     'ERROR' AS KOJIN_NO, ");
            stb.append("     'ERROR' AS SHUUGAKU_NO, ");
            stb.append("     'ERROR' AS SHINSEI_YEAR1, ");
            stb.append("     '重複データあり' AS NAME1, ");
            stb.append("     '重複データあり' AS KANA1, ");
            stb.append("     '重複データあり' AS SCHOOL_NAME1, ");
            stb.append("     '9999-12-31' AS BIRTHDAY1 ");
            stb.append(" FROM ");
            stb.append("     SELECT_T T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.CNT > 1 ");
            stb.append("     OR T1.CHECK_KOJIN_NO IS NULL ");
        }
        stb.append(" ORDER BY ");
        stb.append("     SEIRI_NO, ");
        stb.append("     KANA1 ");
        System.out.print("sql ="  + stb.toString());
        return stb.toString();
    }
    
    private static class PrintData {
        final String _cnt;
        final String _kojinNo;
        final String _shuugakuNo;
        final String _seiriNo;
        final String _shinseiYear1;
        final String _name1;
        final String _kana1;
        final String _schoolname1;
        final String _birthday1;
        final String _shinseiYear2;
        final String _name2;
        final String _kana2;
        final String _schoolname2;
        final String _birthday2;
        final String _kakuteiFlg;
        final String _updated2;
        final boolean _isError;

        PrintData(
            final String cnt,
            final String kojinNo,
            final String shuugakuNo,
            final String seiriNo,
            final String shinseiYear1,
            final String name1,
            final String kana1,
            final String schoolname1,
            final String birthday1,
            final String shinseiYear2,
            final String name2,
            final String kana2,
            final String schoolname2,
            final String birthday2,
            final String kakuteiFlg,
            final String updated2,
            final boolean isError
        ) {
            _cnt = cnt;
            _kojinNo = kojinNo;
            _shuugakuNo = shuugakuNo;
            _seiriNo = seiriNo;
            _shinseiYear1 = shinseiYear1;
            _name1 = name1;
            _kana1 = kana1;
            _schoolname1 = schoolname1;
            _birthday1 = birthday1;
            _shinseiYear2 = shinseiYear2;
            _name2 = name2;
            _kana2 = kana2;
            _schoolname2 = schoolname2;
            _birthday2 = birthday2;
            _kakuteiFlg = kakuteiFlg;
            _updated2 = updated2;
            _isError = isError;
        }
    }
    
    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 74818 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _ctrlDate;
        final String _outputApt; // 1:未突合 2:突合済
        final String _outputDiv; // 突合済のみ 1:未確定のみ 2:すべて
        final String _centerTitle;
        final String _sonotaDiv; // 1:高校生給付型奨学金 2:母子家庭奨学金
        final ShugakuDate _shugakuDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _outputApt = request.getParameter("OUTPUT_APT");
            _outputDiv = request.getParameter("OUTPUT_DIV");
            _centerTitle = request.getParameter("CENTER_TITLE");
            _sonotaDiv = request.getParameter("SONOTA_DIV");
            _shugakuDate = new ShugakuDate(db2);
        }
    }
}

// eof

