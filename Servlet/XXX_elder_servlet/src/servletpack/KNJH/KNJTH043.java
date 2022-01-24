// kanji=漢字
/*
 * $Id: 5fc4a9ba1fd724a5b5acbfcd5e270cbdab841c53 $
 *
 * 作成日: 2014/08/13 10:07:37 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2014 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJH;

import java.io.IOException;
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
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.ShugakuDate;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 5fc4a9ba1fd724a5b5acbfcd5e270cbdab841c53 $
 */
public class KNJTH043 {

    private static final Log log = LogFactory.getLog("KNJTH043.class");

    public static final String PRG_KNJTH043 = "KNJTH043";
    public static final String PRG_KNJTH044 = "KNJTH044";

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
            init(response, svf);

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            printMain(db2, svf);

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            closeDb(db2);
            svf.VrQuit();
        }

    }

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }
    
    private List getPageList(final List printList, final int count) {
        final List pageList = new ArrayList();
        List current = null;
        int oldTotugouDiv = -1;
        for (final Iterator it = printList.iterator(); it.hasNext();) {
            final PrintData printData = (PrintData) it.next();
            if (null == current || current.size() >= count || (oldTotugouDiv < 4 && 4 <= printData._totugouDiv)) {
                current = new ArrayList();
                pageList.add(current);
            }
            current.add(printData);
            oldTotugouDiv = printData._totugouDiv;
        }
        return pageList;
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final List printListAll = getPrintData(db2);
        
        final List pageList = getPageList(printListAll, 50);
        
        for (int pi = 0; pi < pageList.size(); pi++) {
            final List printList = (List) pageList.get(pi);
            int gyouCnt = 1;
            setHead(db2, svf);
            svf.VrsOut("PAGE", String.valueOf(pi + 1) + "/" + String.valueOf(pageList.size()));
            for (final Iterator iter = printList.iterator(); iter.hasNext();) {
                final PrintData printData = (PrintData) iter.next();
                svf.VrsOutn("COMPARE", gyouCnt, "1".equals(printData._totugouFlg) ? "レ" : "");
                svf.VrsOutn("DECIDE", gyouCnt, "1".equals(printData._kakuteiFlg) ? "レ" : "");
                if (printData._totugouDiv <= 3 || printData._totugouDiv == 4) {
                    svf.VrsOutn("NO1", gyouCnt, printData._seiriNo);
                    final String kanaSoeji = getMS932Length(printData._kana1) > 28 ? "3" : getMS932Length(printData._kana1) > 16 ? "2" : "1";
                    svf.VrsOutn("KANA1_" + kanaSoeji, gyouCnt, printData._kana1);
                    svf.VrsOutn("BIRTHDAY1", gyouCnt, _param._shugakuDate.formatDateMarkDot(printData._birthday1));
                    final String nameSoeji = getMS932Length(printData._name1) > 28 ? "3" : getMS932Length(printData._name1) > 16 ? "2" : "1";
                    svf.VrsOutn("NAME1_" + nameSoeji, gyouCnt, printData._name1);
                    if (PRG_KNJTH043.equals(_param._prgId)) {
                        final String schoolSoeji = getMS932Length(printData._schoolName1) > 30 ? "3" : getMS932Length(printData._schoolName1) > 20 ? "2" : "1";
                        svf.VrsOutn("SCHOOL_NAME1_" + schoolSoeji, gyouCnt, printData._schoolName1);
                        svf.VrsOutn("ZIPNO1", gyouCnt, printData._zipcd1);
                        final String setAddr1 = printData._addr1_1 + printData._addr1_2 + printData._addr1_3;
                        final String addrSoeji = getMS932Length(setAddr1) > 50 ? "4_1" : getMS932Length(setAddr1) > 40 ? "3" : getMS932Length(setAddr1) > 30 ? "2" : "1";
                        svf.VrsOutn("ADDR1_" + addrSoeji, gyouCnt, setAddr1);
                    } else {
                        final String gNameSoeji = getMS932Length(printData._hogoName1) > 28 ? "3" : getMS932Length(printData._hogoName1) > 16 ? "2" : "1";
                        svf.VrsOutn("GUARD_NAME1_" + gNameSoeji, gyouCnt, printData._hogoName1);
                        svf.VrsOutn("ZIPNO1", gyouCnt, printData._hogoZipcd1);
                        final String setAddr1 = printData._hogoAddr1_1 + printData._hogoAddr1_2;
                        final String addrSoeji = getMS932Length(setAddr1) > 50 ? "4_1" : getMS932Length(setAddr1) > 40 ? "3" : getMS932Length(setAddr1) > 30 ? "2" : "1";
                        svf.VrsOutn("GUARD_ADDR1_" + addrSoeji, gyouCnt, setAddr1);
                    }
                    svf.VrsOutn("UPDATE", gyouCnt, _param._shugakuDate.formatDateMarkDot(printData._updated1));
                }
                if (printData._totugouDiv <= 3 || printData._totugouDiv == 5) {
                    svf.VrsOutn("NO2", gyouCnt, printData._kojinNo);
                    final String kanaSoeji2 = getMS932Length(printData._kana2) > 28 ? "3" : getMS932Length(printData._kana2) > 16 ? "2" : "1";
                    svf.VrsOutn("KANA2_" + kanaSoeji2, gyouCnt, printData._kana2);
                    svf.VrsOutn("BIRTHDAY2", gyouCnt, _param._shugakuDate.formatDateMarkDot(printData._birthday2));
                    final String nameSoeji2 = getMS932Length(printData._name2) > 28 ? "3" : getMS932Length(printData._name2) > 16 ? "2" : "1";
                    svf.VrsOutn("NAME2_" + nameSoeji2, gyouCnt, printData._name2);
                    if (PRG_KNJTH043.equals(_param._prgId)) {
                        final String schoolSoeji = getMS932Length(printData._schoolName2) > 30 ? "3" : getMS932Length(printData._schoolName2) > 20 ? "2" : "1";
                        svf.VrsOutn("SCHOOL_NAME2_" + schoolSoeji, gyouCnt, printData._schoolName2);
                        svf.VrsOutn("ZIPNO2", gyouCnt, printData._zipcd2);
                        final String setAddr2 = printData._hogoAddr2_1 + printData._hogoAddr2_2;
                        final String addrSoeji = getMS932Length(setAddr2) > 50 ? "4_1" : getMS932Length(setAddr2) > 40 ? "3" : getMS932Length(setAddr2) > 30 ? "2" : "1";
                        svf.VrsOutn("ADDR2_" + addrSoeji, gyouCnt, setAddr2);
                    } else {
                        final String gNameSoeji = getMS932Length(printData._hogoName2) > 28 ? "3" : getMS932Length(printData._hogoName2) > 16 ? "2" : "1";
                        svf.VrsOutn("GUARD_NAME2_" + gNameSoeji, gyouCnt, printData._hogoName2);
                        svf.VrsOutn("ZIPNO2", gyouCnt, printData._hogoZipcd2);
                        final String setAddr2 = printData._hogoAddr2_1 + printData._hogoAddr2_2;
                        final String addrSoeji = getMS932Length(setAddr2) > 50 ? "4_1" : getMS932Length(setAddr2) > 40 ? "3" : getMS932Length(setAddr2) > 30 ? "2" : "1";
                        svf.VrsOutn("GUARD_ADDR2_" + addrSoeji, gyouCnt, setAddr2);
                    }
                }
                _hasData = true;
                gyouCnt++;
            }
            svf.VrEndPage();
        }
    }

    private void setHead(final DB2UDB db2, final Vrw32alp svf) {
        final String formName = PRG_KNJTH043.equals(_param._prgId) ? PRG_KNJTH043 : PRG_KNJTH044;
        svf.VrSetForm(formName + ".frm", 1);
        final String setTitle = PRG_KNJTH043.equals(_param._prgId) ? "高校生給付型奨学金" : "母子家庭奨学金";
        svf.VrsOut("TITLE", setTitle + "と奨学給付金申請者突合リスト");
        svf.VrsOut("DATE", _param._shugakuDate.formatDate(_param._ctrlDate, false)); // 日付
    }

    private static int getMS932Length(final String s) {
    	return KNJ_EditEdit.getMS932ByteLength(s);
    }

    private List getPrintData(final DB2UDB db2) throws SQLException {
        final List retList = new ArrayList();
        final String printSql = getPrintSql();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(printSql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String totugouFlg = rs.getString("TOTUGOU_FLG");
                final String kakuteiFlg = rs.getString("KAKUTEI_FLG");
                final int totugouDiv = rs.getInt("TOTUGOU_DIV");
                final String seiriNo = rs.getString("SEIRI_NO");
                final String shinseiYear1 = rs.getString("SHINSEI_YEAR1");
                final String name1 = rs.getString("NAME1");
                final String kana1 = rs.getString("KANA1");
                final String birthday1 = rs.getString("BIRTHDAY1");
                final String schoolName1 = rs.getString("SCHOOL_NAME1");
                final String zipcd1 = rs.getString("ZIPCD1");
                final String addr1_1 = rs.getString("ADDR1_1");
                final String addr1_2 = rs.getString("ADDR1_2");
                final String addr1_3 = rs.getString("ADDR1_3");
                final String hogoName1 = rs.getString("HOGO_NAME1");
                final String hogoZipcd1 = rs.getString("HOGO_ZIPCD1");
                final String hogoAddr1_1 = rs.getString("HOGO_ADDR1_1");
                final String hogoAddr1_2 = rs.getString("HOGO_ADDR1_2");
                final String updated1 = rs.getString("UPDATED1");
                final String kojinNo = rs.getString("KOJIN_NO");
                final String shuugakuNo = rs.getString("SHUUGAKU_NO");
                final String name2 = rs.getString("NAME2");
                final String shinseiYear2 = rs.getString("SHINSEI_YEAR2");
                final String kana2 = rs.getString("KANA2");
                final String birthday2 = rs.getString("BIRTHDAY2");
                final String schoolName2 = rs.getString("SCHOOL_NAME2");
                final String zipcd2 = rs.getString("ZIPCD2");
                final String addr2_1 = rs.getString("ADDR2_1");
                final String addr2_2 = rs.getString("ADDR2_2");
                final String hogoName2 = rs.getString("HOGO_NAME2");
                final String hogoZipcd2 = rs.getString("HOGO_ZIPCD2");
                final String hogoAddr2_1 = rs.getString("HOGO_ADDR2_1");
                final String hogoAddr2_2 = rs.getString("HOGO_ADDR2_2");
                final PrintData printData = new PrintData(totugouFlg, kakuteiFlg, totugouDiv, seiriNo, shinseiYear1, name1, kana1, birthday1, schoolName1, zipcd1, addr1_1, addr1_2, addr1_3, hogoName1, hogoZipcd1, hogoAddr1_1, hogoAddr1_2, updated1, kojinNo, shuugakuNo, name2, shinseiYear2, kana2, birthday2, schoolName2, zipcd2, addr2_1, addr2_2, hogoName2, hogoZipcd2, hogoAddr2_1, hogoAddr2_2);
                retList.add(printData);
            }
        } catch (final Exception e) {
            DbUtils.closeQuietly(null, ps, rs);
            db2.close();
        }
        return retList;
    }

    /**
     * @return
     */
    private String getPrintSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append("  WITH MXSEQ_KSKDAT AS ( ");
        stb.append("    SELECT ");
        stb.append("      T0.* ");
        stb.append("    FROM ");
        stb.append("     KOJIN_SHINSEI_KYUHU_DAT T0 ");
        stb.append("    WHERE ");
        stb.append("      T0.SEQ = (SELECT MAX(J0.SEQ) FROM KOJIN_SHINSEI_KYUHU_DAT J0 WHERE J0.KOJIN_NO = T0.KOJIN_NO AND J0.SHINSEI_YEAR = T0.SHINSEI_YEAR) ");
        stb.append(" ), MAIN_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SEIRI_NO, ");
        stb.append("     T1.KOJIN_NO, ");
        stb.append("     T1.KAKUTEI_FLG, ");
        stb.append("     VALUE(T1.FAMILY_NAME,'') || '　' || VALUE(T1.FIRST_NAME,'') AS NAME1, ");
        stb.append("     VALUE(T1.SHINSEI_YEAR,'') AS SHINSEI_YEAR1, ");
        stb.append("     TRANSLATE_HK_K(VALUE(T1.FAMILY_NAME_KANA,'') || VALUE(T1.FIRST_NAME_KANA,'')) AS KANA1, ");
        stb.append("     T1.BIRTHDAY AS BIRTHDAY1, ");
        stb.append("     VALUE(SCHOOL_NAME, '') AS SCHOOL_NAME1, ");
        stb.append("     VALUE(ZIPCD, '') AS ZIPCD1, ");
        stb.append("     VALUE(ADDR1, '') AS ADDR1_1, ");
        stb.append("     VALUE(ADDR2, '') AS ADDR1_2, ");
        stb.append("     VALUE(ADDR3, '') AS ADDR1_3, ");
        stb.append("     VALUE(T1.HOGO_FAMILY_NAME,'') || '　' || VALUE(T1.HOGO_FIRST_NAME,'') AS HOGO_NAME1, ");
        stb.append("     VALUE(HOGO_ZIPCD, '') AS HOGO_ZIPCD1, ");
        stb.append("     VALUE(HOGO_ADDR1, '') AS HOGO_ADDR1_1, ");
        stb.append("     VALUE(HOGO_ADDR2, '') AS HOGO_ADDR1_2, ");
        stb.append("     DATE(T1.UPDATED) AS UPDATED1 ");
        stb.append(" FROM ");
        stb.append("     KYUHU_SONOTA_CSV_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.SONOTA_DIV = '" + _param._sonotaDiv + "' ");
        if ("1".equals(_param._notPrintKettei)) {
            stb.append("     AND VALUE(T1.KAKUTEI_FLG, '0') != '1' ");
        }
        stb.append(" ), JOIN_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.KOJIN_NO, ");
        stb.append("     T1.SHUUGAKU_NO, ");
        stb.append("     VALUE(L1.FAMILY_NAME,'') || '　' || VALUE(L1.FIRST_NAME,'') AS NAME2, ");
        stb.append("     VALUE(T1.SHINSEI_YEAR,'') AS SHINSEI_YEAR2, ");
        stb.append("     TRANSLATE_HK_K(VALUE(L1.FAMILY_NAME_KANA,'') || VALUE(L1.FIRST_NAME_KANA,'')) AS KANA2, ");
        stb.append("     L1.BIRTHDAY AS BIRTHDAY2, ");
        stb.append("     VALUE(L2.NAME, '') AS SCHOOL_NAME2, ");
        stb.append("     VALUE(L1.ZIPCD, '') AS ZIPCD2, ");
        stb.append("     VALUE(L1.ADDR1, '') AS ADDR2_1, ");
        stb.append("     VALUE(L1.ADDR2, '') AS ADDR2_2, ");
        stb.append("     VALUE(L3.FAMILY_NAME,'') || '　' || VALUE(L3.FIRST_NAME,'') AS HOGO_NAME2, ");
        stb.append("     VALUE(L3.ZIPCD, '') AS HOGO_ZIPCD2, ");
        stb.append("     VALUE(L3.ADDR1, '') AS HOGO_ADDR2_1, ");
        stb.append("     VALUE(L3.ADDR2, '') AS HOGO_ADDR2_2 ");
        stb.append(" FROM ");
        stb.append("     MXSEQ_KSKDAT T1 ");
        stb.append("     INNER JOIN V_KOJIN_HIST_DAT L1 ON T1.KOJIN_NO = L1.KOJIN_NO ");
        stb.append("     LEFT JOIN SCHOOL_DAT L2 ON T1.H_SCHOOL_CD = L2.SCHOOLCD ");
        stb.append("     LEFT JOIN SHINKENSHA_HIST_DAT L3 ON T1.HOGOSHA_CD = L3.SHINKEN_CD ");
        stb.append(" WHERE ");
        stb.append("     T1.SHINSEI_YEAR = '" + _param._shinseiYear + "' ");
        stb.append(" ), SELECT_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     CASE WHEN T1.KOJIN_NO = JOIN_T.KOJIN_NO ");
        stb.append("          THEN '1' ");
        stb.append("          ELSE '' ");
        stb.append("     END AS TOTUGOU_FLG, ");
        stb.append("     CASE WHEN VALUE(T1.KAKUTEI_FLG, '0') = '1' AND T1.KOJIN_NO = JOIN_T.KOJIN_NO ");
        stb.append("          THEN '1' ");
        stb.append("          ELSE '' ");
        stb.append("     END AS KAKUTEI_FLG, ");
        stb.append("     T1.SEIRI_NO, ");
        stb.append("     T1.NAME1, ");
        stb.append("     T1.SHINSEI_YEAR1, ");
        stb.append("     T1.KANA1, ");
        stb.append("     T1.BIRTHDAY1, ");
        stb.append("     T1.SCHOOL_NAME1, ");
        stb.append("     T1.ZIPCD1, ");
        stb.append("     T1.ADDR1_1, ");
        stb.append("     T1.ADDR1_2, ");
        stb.append("     T1.ADDR1_3, ");
        stb.append("     T1.HOGO_NAME1, ");
        stb.append("     T1.HOGO_ZIPCD1, ");
        stb.append("     T1.HOGO_ADDR1_1, ");
        stb.append("     T1.HOGO_ADDR1_2, ");
        stb.append("     T1.UPDATED1, ");
        stb.append("     '1' AS TOTUGOU_DIV, ");
        stb.append("     JOIN_T.KOJIN_NO, ");
        stb.append("     JOIN_T.SHUUGAKU_NO, ");
        stb.append("     JOIN_T.NAME2, ");
        stb.append("     JOIN_T.SHINSEI_YEAR2, ");
        stb.append("     JOIN_T.KANA2, ");
        stb.append("     JOIN_T.BIRTHDAY2, ");
        stb.append("     JOIN_T.ZIPCD2, ");
        stb.append("     JOIN_T.ADDR2_1, ");
        stb.append("     JOIN_T.ADDR2_2, ");
        stb.append("     JOIN_T.SCHOOL_NAME2, ");
        stb.append("     JOIN_T.HOGO_NAME2, ");
        stb.append("     JOIN_T.HOGO_ZIPCD2, ");
        stb.append("     JOIN_T.HOGO_ADDR2_1, ");
        stb.append("     JOIN_T.HOGO_ADDR2_2 ");
        stb.append(" FROM ");
        stb.append("     MAIN_T T1 ");
        stb.append("     INNER JOIN JOIN_T ON JOIN_T.SHINSEI_YEAR2 = T1.SHINSEI_YEAR1 ");
        stb.append("          AND JOIN_T.KANA2 = T1.KANA1 ");
        stb.append("          AND JOIN_T.BIRTHDAY2 = T1.BIRTHDAY1 ");

        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.TOTUGOU_FLG, ");
        stb.append("     T1.KAKUTEI_FLG, ");
        stb.append("     T1.TOTUGOU_DIV, ");
        stb.append("     T1.SEIRI_NO, ");
        stb.append("     T1.SHINSEI_YEAR1, ");
        stb.append("     T1.NAME1, ");
        stb.append("     T1.KANA1, ");
        stb.append("     T1.BIRTHDAY1, ");
        stb.append("     T1.SCHOOL_NAME1, ");
        stb.append("     T1.ZIPCD1, ");
        stb.append("     T1.ADDR1_1, ");
        stb.append("     T1.ADDR1_2, ");
        stb.append("     T1.ADDR1_3, ");
        stb.append("     T1.HOGO_NAME1, ");
        stb.append("     T1.HOGO_ZIPCD1, ");
        stb.append("     T1.HOGO_ADDR1_1, ");
        stb.append("     T1.HOGO_ADDR1_2, ");
        stb.append("     T1.UPDATED1, ");
        stb.append("     T1.KOJIN_NO, ");
        stb.append("     T1.SHUUGAKU_NO, ");
        stb.append("     T1.NAME2, ");
        stb.append("     T1.SHINSEI_YEAR2, ");
        stb.append("     T1.KANA2, ");
        stb.append("     T1.BIRTHDAY2, ");
        stb.append("     T1.SCHOOL_NAME2, ");
        stb.append("     T1.ZIPCD2, ");
        stb.append("     T1.ADDR2_1, ");
        stb.append("     T1.ADDR2_2, ");
        stb.append("     T1.HOGO_NAME2, ");
        stb.append("     T1.HOGO_ZIPCD2, ");
        stb.append("     T1.HOGO_ADDR2_1, ");
        stb.append("     T1.HOGO_ADDR2_2 ");
        stb.append(" FROM ");
        stb.append("     SELECT_T T1 ");
        if ("1".equals(_param._fuittiCheck)) {
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     '' AS TOTUGOU_FLG, ");
            stb.append("     '' AS KAKUTEI_FLG, ");
            stb.append("     '4' AS TOTUGOU_DIV, ");
            stb.append("     T1.SEIRI_NO, ");
            stb.append("     T1.SHINSEI_YEAR1, ");
            stb.append("     T1.NAME1, ");
            stb.append("     T1.KANA1, ");
            stb.append("     T1.BIRTHDAY1, ");
            stb.append("     T1.SCHOOL_NAME1, ");
            stb.append("     T1.ZIPCD1, ");
            stb.append("     T1.ADDR1_1, ");
            stb.append("     T1.ADDR1_2, ");
            stb.append("     T1.ADDR1_3, ");
            stb.append("     T1.HOGO_NAME1, ");
            stb.append("     T1.HOGO_ZIPCD1, ");
            stb.append("     T1.HOGO_ADDR1_1, ");
            stb.append("     T1.HOGO_ADDR1_2, ");
            stb.append("     T1.UPDATED1, ");
            stb.append("     '' AS KOJIN_NO, ");
            stb.append("     '' AS SHUUGAKU_NO, ");
            stb.append("     '' AS NAME2, ");
            stb.append("     '' AS SHINSEI_YEAR2, ");
            stb.append("     '' AS KANA2, ");
            stb.append("     '9999-12-31' AS BIRTHDAY2, ");
            stb.append("     '' AS SCHOOL_NAME2, ");
            stb.append("     '' AS ZIPCD2, ");
            stb.append("     '' AS ADDR2_1, ");
            stb.append("     '' AS ADDR2_2, ");
            stb.append("     '' AS HOGO_NAME2, ");
            stb.append("     '' AS HOGO_ZIPCD2, ");
            stb.append("     '' AS HOGO_ADDR2_1, ");
            stb.append("     '' AS HOGO_ADDR2_2 ");
            stb.append(" FROM ");
            stb.append("     MAIN_T T1 ");
            stb.append("     LEFT JOIN JOIN_T ON JOIN_T.SHINSEI_YEAR2 = T1.SHINSEI_YEAR1 ");
            stb.append("          AND JOIN_T.KANA2 = T1.KANA1 ");
            stb.append("          AND JOIN_T.BIRTHDAY2 = T1.BIRTHDAY1 ");
            stb.append(" WHERE ");
            stb.append("     JOIN_T.SHINSEI_YEAR2 IS NULL ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     '' AS TOTUGOU_FLG, ");
            stb.append("     '' AS KAKUTEI_FLG, ");
            stb.append("     '5' AS TOTUGOU_DIV, ");
            stb.append("     '999999999999999' AS SEIRI_NO, ");
            stb.append("     '' AS SHINSEI_YEAR1, ");
            stb.append("     '' AS NAME1, ");
            stb.append("     '' AS KANA1, ");
            stb.append("     '9999-12-31' AS BIRTHDAY1, ");
            stb.append("     '' AS SCHOOL_NAME1, ");
            stb.append("     '' AS ZIPCD1, ");
            stb.append("     '' AS ADDR1_1, ");
            stb.append("     '' AS ADDR1_2, ");
            stb.append("     '' AS ADDR1_3, ");
            stb.append("     '' AS HOGO_NAME1, ");
            stb.append("     '' AS HOGO_ZIPCD1, ");
            stb.append("     '' AS HOGO_ADDR1_1, ");
            stb.append("     '' AS HOGO_ADDR1_2, ");
            stb.append("     '9999-12-31' AS UPDATED1, ");
            stb.append("     T1.KOJIN_NO, ");
            stb.append("     T1.SHUUGAKU_NO, ");
            stb.append("     T1.NAME2, ");
            stb.append("     T1.SHINSEI_YEAR2, ");
            stb.append("     T1.KANA2, ");
            stb.append("     T1.BIRTHDAY2, ");
            stb.append("     T1.SCHOOL_NAME2, ");
            stb.append("     T1.ZIPCD2, ");
            stb.append("     T1.ADDR2_1, ");
            stb.append("     T1.ADDR2_2, ");
            stb.append("     T1.HOGO_NAME2, ");
            stb.append("     T1.HOGO_ZIPCD2, ");
            stb.append("     T1.HOGO_ADDR2_1, ");
            stb.append("     T1.HOGO_ADDR2_2 ");
            stb.append(" FROM ");
            stb.append("     JOIN_T T1 ");
            stb.append(" WHERE ");
            stb.append("     NOT EXISTS( ");
            stb.append("         SELECT ");
            stb.append("             'x' ");
            stb.append("         FROM ");
            stb.append("             SELECT_T E1 ");
            stb.append("         WHERE ");
            stb.append("             E1.KOJIN_NO = T1.KOJIN_NO ");
            stb.append("     ) ");
        }
        stb.append(" ORDER BY ");
        stb.append("     TOTUGOU_DIV, ");
        stb.append("     SEIRI_NO, ");
        stb.append("     KOJIN_NO ");

        return stb.toString();
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 74819 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** プリントデータ */
    private class PrintData {
        final String _totugouFlg;
        final String _kakuteiFlg;
        final int _totugouDiv;
        final String _seiriNo;
        final String _shinseiYear1;
        final String _name1;
        final String _kana1;
        final String _birthday1;
        final String _schoolName1;
        final String _zipcd1;
        final String _addr1_1;
        final String _addr1_2;
        final String _addr1_3;
        final String _hogoName1;
        final String _hogoZipcd1;
        final String _hogoAddr1_1;
        final String _hogoAddr1_2;
        final String _updated1;
        final String _kojinNo;
        final String _shuugakuNo;
        final String _name2;
        final String _shinseiYear2;
        final String _kana2;
        final String _birthday2;
        final String _schoolName2;
        final String _zipcd2;
        final String _addr2_1;
        final String _addr2_2;
        final String _hogoName2;
        final String _hogoZipcd2;
        final String _hogoAddr2_1;
        final String _hogoAddr2_2;
        public PrintData(
                final String totugouFlg,
                final String kakuteiFlg,
                final int totugouDiv,
                final String seiriNo,
                final String shinseiYear1,
                final String name1,
                final String kana1,
                final String birthday1,
                final String schoolName1,
                final String zipcd1,
                final String addr1_1,
                final String addr1_2,
                final String addr1_3,
                final String hogoName1,
                final String hogoZipcd1,
                final String hogoAddr1_1,
                final String hogoAddr1_2,
                final String updated1,
                final String kojinNo,
                final String shuugakuNo,
                final String name2,
                final String shinseiYear2,
                final String kana2,
                final String birthday2,
                final String schoolName2,
                final String zipcd2,
                final String addr2_1,
                final String addr2_2,
                final String hogoName2,
                final String hogoZipcd2,
                final String hogoAddr2_1,
                final String hogoAddr2_2
        ) {
            _totugouFlg     = totugouFlg;
            _kakuteiFlg     = kakuteiFlg;
            _totugouDiv     = totugouDiv;
            _seiriNo        = seiriNo;
            _shinseiYear1   = shinseiYear1;
            _name1          = name1;
            _kana1          = kana1;
            _birthday1      = birthday1;
            _schoolName1    = schoolName1;
            _zipcd1         = zipcd1;
            _addr1_1        = addr1_1;
            _addr1_2        = addr1_2;
            _addr1_3        = addr1_3;
            _hogoName1      = hogoName1;
            _hogoZipcd1     = hogoZipcd1;
            _hogoAddr1_1    = hogoAddr1_1;
            _hogoAddr1_2    = hogoAddr1_2;
            _updated1       = updated1;
            _kojinNo        = kojinNo;
            _shuugakuNo     = shuugakuNo;
            _name2          = name2;
            _shinseiYear2   = shinseiYear2;
            _kana2          = kana2;
            _birthday2      = birthday2;
            _schoolName2    = schoolName2;
            _zipcd2         = zipcd2;
            _addr2_1        = addr2_1;
            _addr2_2        = addr2_2;
            _hogoName2      = hogoName2;
            _hogoZipcd2     = hogoZipcd2;
            _hogoAddr2_1    = hogoAddr2_1;
            _hogoAddr2_2    = hogoAddr2_2;
        }
    }

    /** パラメータクラス */
    private class Param {
        private final String _shinseiYear;
        private final String _fuittiCheck;
        private final String _notPrintKettei;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _prgId;
        private final String _sonotaDiv;
        private final ShugakuDate _shugakuDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _shinseiYear = request.getParameter("SHINSEI_YEAR");
            _fuittiCheck = request.getParameter("HUITTI_CHECK");
            _notPrintKettei = request.getParameter("NOT_PRINT_KETTEI");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _prgId = request.getParameter("PRGID");
            _sonotaDiv = PRG_KNJTH043.equals(_prgId) ? "1" : "2";
            _shugakuDate = new ShugakuDate(db2);
        }
    }
}

// eof
