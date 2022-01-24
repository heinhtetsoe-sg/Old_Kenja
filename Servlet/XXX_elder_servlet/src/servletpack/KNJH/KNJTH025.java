// kanji=漢字
/*
 * 作成日: 2014/08/19 19:04:36 - JST
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
 */
public class KNJTH025 {

    private static final Log log = LogFactory.getLog("KNJTH025.class");

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

    private static int getMS932Length(final String s) {
        return KNJ_EditEdit.getMS932ByteLength(s);
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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final List distCdSchoolList = getDistCdSchoolList(db2);
        final int maxLine = 14; // 1ページあたり行数
        int tpage = 0; // 総ページ数
        for (final Iterator it = distCdSchoolList.iterator(); it.hasNext();) {
            final List schoolList = (List) it.next();
            for (int si = 0; si < schoolList.size(); si++) {
                final School school = (School) schoolList.get(si);

                school._printDataList.add(new Goukei("合計"));
                if (si == schoolList.size() - 1) {
                    school._printDataList.add(new Goukei("総合計"));
                }

                final int page = school._printDataList.size() / maxLine + (school._printDataList.size() % maxLine == 0 ? 0 : 1);
                tpage += page;
            }
        }

        int page = 0;
        for (final Iterator it = distCdSchoolList.iterator(); it.hasNext();) {
            long totalDataCnt = 0;
            long totalTotalMoney = 0;
            long totalTsuDataCnt = 0;
            long totalTsuTotalMoney = 0;
            final List schoolList = (List) it.next();
            for (int si = 0; si < schoolList.size(); si++) {
                final School school = (School) schoolList.get(si);

                final List pnoList = new ArrayList();
                long dataCnt = 0;
                long totalMoney = 0;
                long tsudataCnt = 0;
                long tsutotalMoney = 0;

                final List pageList = getPageList(school._printDataList, maxLine);

                for (int pi = 0; pi < pageList.size(); pi++) {
                    final List printDataList = (List) pageList.get(pi);

                    svf.VrSetForm("KNJTH025.frm", 4);
                    svf.VrsOut("FIELD1", "京都府奨学のための給付金(" + school._distName + ")支給　支出命令内訳書");
                    svf.VrsOut("SCHOOL_NAME", school._schoolName);
                    svf.VrsOut("PAYDAY", _param._shugakuDate.formatDate(_param._shiteiDate, false));
                    svf.VrsOut("DATE", _param._shugakuDate.formatDate(_param._date, false));
                    svf.VrsOut("PAGE", String.valueOf(page + pi + 1));
                    svf.VrsOut("PAGE2", String.valueOf(tpage));

                    for (final Iterator itDlist = printDataList.iterator(); itDlist.hasNext();) {
                        final Object o = itDlist.next();
                        if (o instanceof Goukei) {
                            final Goukei goukei = (Goukei) o;
                            if ("総合計".equals(goukei._name)) {
                                svf.VrsOut("GOUKEI2", "総合計");
                                svf.VrsOut("GOUKEI2_2", "(内) 通信費");
                                svf.VrsOut("SCOUNT2", String.valueOf(totalDataCnt));
                                svf.VrsOut("SCOUNT2_2", String.valueOf(totalTsuDataCnt));
                                svf.VrsOut("STOTAL_PAY_MONEY2", String.valueOf(totalTotalMoney));
                                svf.VrsOut("STOTAL_PAY_MONEY2_2", String.valueOf(totalTsuTotalMoney));
                                svf.VrEndRecord();

                            } else if ("合計".equals(goukei._name)) {
                                svf.VrsOut("PNO_COUNT", String.valueOf(pnoList.size())); //給付番号件数
                                svf.VrsOut("GOUKEI", "合計");
                                svf.VrsOut("GOUKEI_2", "(内) 通信費");
                                svf.VrsOut("SCOUNT", String.valueOf(dataCnt));
                                svf.VrsOut("SCOUNT_2", String.valueOf(tsudataCnt));
                                svf.VrsOut("STOTAL_PAY_MONEY", String.valueOf(totalMoney));
                                svf.VrsOut("STOTAL_PAY_MONEY_2", String.valueOf(tsutotalMoney));
                                svf.VrEndRecord();
                            }
                        } else if (o instanceof PrintData) {
                            final PrintData printData = (PrintData) o;
                            final int pmVal = Integer.parseInt(printData._shishutsuGk) + Integer.parseInt(printData._tsu_shishutsuGk) + Integer.parseInt(printData._tsuikaKyuhu_shishutsuGk);

                            svf.VrsOut("PNO", printData._shuugakuNo);
                            final String nameSoeji = getMS932Length(printData._hogoName) > 20 ? "2" : "1";
                            svf.VrsOut("NAME" + nameSoeji, printData._hogoName);
                            svf.VrsOut("KIND", printData._shikinName);
                            svf.VrsOut("ZIP_NO", printData._zipcd);
                            svf.VrsOut("ADDRESS1", printData._addr1);
                            svf.VrsOut("ADDRESS2", printData._addr2);
                            svf.VrsOut("PAY_FPERIOD", _param._shugakuDate.formatDate(printData._furikomiDate, false));
                            svf.VrsOut("PAY_MONEY", String.valueOf(pmVal));
                            svf.VrsOut("BANK_NAME", printData._bankname);
                            svf.VrsOut("BRANCH_NAME", printData._branchname);
                            svf.VrsOut("ITEM", printData._yokinName);
                            svf.VrsOut("AC_NUMBER", printData._accountNo);
                            svf.VrsOut("AC_NAME", printData._kouzaName);
                            svf.VrEndRecord();

                            if (!pnoList.contains(printData._shuugakuNo)) {
                                pnoList.add(printData._shuugakuNo);
                            }
                            dataCnt++;
                            totalMoney += pmVal;
                            totalDataCnt++;
                            totalTotalMoney += pmVal;
                            if ("1".equals(printData._tsuFlg)) {
                                tsudataCnt++;
                                tsutotalMoney += Integer.parseInt(printData._tsu_shishutsuGk);
                                totalTsuDataCnt++;
                                totalTsuTotalMoney += Integer.parseInt(printData._tsu_shishutsuGk);
                            }
                        }
                        _hasData = true;
                    }
                }
                page += pageList.size();
            }
        }
    }

    private List getDistCdSchoolList(final DB2UDB db2) throws SQLException {
        final List retList = new ArrayList();
        List schoolList = null;
        final String sql = getPrintSql();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            String oldSchoolDistcd = null;
            while (rs.next()) {

                final String schoolDistcd = rs.getString("SCHOOL_DISTCD");
                if (null == schoolList || ("1".equals(oldSchoolDistcd) || "2".equals(oldSchoolDistcd)) && "3".equals(schoolDistcd)) {
                    schoolList = new ArrayList();
                    retList.add(schoolList);
                }

                final String hSchoolCd = rs.getString("H_SCHOOL_CD");

                if (null == School.getSchool(hSchoolCd, schoolList)) {
                    final String schoolName = rs.getString("SCHOOL_NAME");
                    String distName = "";
                    if ("1".equals(rs.getString("SCHOOL_DISTCD")) || "2".equals(rs.getString("SCHOOL_DISTCD"))) {
                        distName = "国公立";
                    } else if ("3".equals(rs.getString("SCHOOL_DISTCD"))) {
                        distName = "私立";
                    }
                    schoolList.add(new School(hSchoolCd, schoolName, schoolDistcd, distName));
                }

                final School school = School.getSchool(hSchoolCd, schoolList);

                final String shuugakuNo = rs.getString("SHUUGAKU_NO");
                final String hogoName = rs.getString("HOGO_NAME");
                final String shikinName = rs.getString("SHIKIN_NAME");
                final String zipcd = rs.getString("ZIPCD");
                final String addr1 = rs.getString("ADDR1");
                final String addr2 = rs.getString("ADDR2");
                final String furikomiDate = rs.getString("FURIKOMI_DATE");
                final String shishutsuGk = rs.getString("SHISHUTSU_GK");
                final String tsu_shishutsuGk = rs.getString("TSU_SHISHUTSU_GK");
                final String tsuFlg = rs.getString("TSU_FLG");
                final String tsuikaKyuhu_shishutsuGk = rs.getString("TSUIKA_KYUHU_SHISHUTSU_GK");
                final String tsuikaKyuhuFlg = rs.getString("TSUIKA_KYUHU_FLG");
                final String bankname = rs.getString("BANKNAME");
                final String branchname = rs.getString("BRANCHNAME");
                final String yokinName = rs.getString("YOKIN_NAME");
                final String accountNo = rs.getString("ACCOUNT_NO");
                final String kouzaName = rs.getString("KOUZA_NAME");
                final PrintData printData = new PrintData(shuugakuNo, hogoName, shikinName, zipcd, addr1, addr2, furikomiDate, shishutsuGk, tsu_shishutsuGk, tsuFlg, tsuikaKyuhu_shishutsuGk, tsuikaKyuhuFlg, bankname, branchname, yokinName, accountNo, kouzaName);

                school._printDataList.add(printData);

                oldSchoolDistcd = schoolDistcd;
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getPrintSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH KEIKAKU AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SHUUGAKU_NO, ");
        stb.append("     T1.KOJIN_NO, ");
        stb.append("     T1.SEQ, ");
        stb.append("     T1.SHIKIN_SHUBETSU, ");
        stb.append("     N1.ABBV1 AS SHIKIN_NAME, ");
        stb.append("     T1.FURIKOMI_DATE, ");
        stb.append("     MAX(T1.SHISHUTSU_GK) AS SHISHUTSU_GK ");
        stb.append(" FROM ");
        stb.append("     KYUHU_KEIKAKU_DAT T1 ");
        stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'T008' ");
        stb.append("          AND T1.SHIKIN_SHUBETSU = N1.NAMECD2 ");
        stb.append(" WHERE ");
        stb.append("     T1.SHINSEI_YEAR = '" + _param._shinseiYear + "' ");
        stb.append("     AND T1.FURIKOMI_DATE = '" + _param._shiteiDate + "' ");
        stb.append("     AND VALUE(T1.KARI_TEISHI_FLG, '0') = '0' ");
        stb.append("     AND VALUE(T1.TEISHI_FLG, '0') = '0' ");
        stb.append("     AND T1.SHISHUTSU_GK >= 0  ");
        stb.append(" GROUP BY ");
        stb.append("     T1.SHUUGAKU_NO, ");
        stb.append("     T1.KOJIN_NO, ");
        stb.append("     T1.SEQ, ");
        stb.append("     T1.SHIKIN_SHUBETSU, ");
        stb.append("     N1.ABBV1, ");
        stb.append("     T1.FURIKOMI_DATE ");

        //通信費
        stb.append(" ), TSU_KEIKAKU AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SHUUGAKU_NO, ");
        stb.append("     T1.KOJIN_NO, ");
        stb.append("     T1.SEQ, ");
        stb.append("     T1.SHIKIN_SHUBETSU, ");
        stb.append("     N1.ABBV1 AS SHIKIN_NAME, ");
        stb.append("     T1.FURIKOMI_DATE, ");
        stb.append("     MAX(T1.SHISHUTSU_GK) AS SHISHUTSU_GK ");
        stb.append(" FROM ");
        stb.append("     TSUSHINHI_KEIKAKU_DAT T1 ");
        stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'T008' ");
        stb.append("          AND T1.SHIKIN_SHUBETSU = N1.NAMECD2 ");
        stb.append(" WHERE ");
        stb.append("     T1.SHINSEI_YEAR = '" + _param._shinseiYear + "' ");
        stb.append("     AND T1.FURIKOMI_DATE = '" + _param._shiteiDate + "' ");
        stb.append("     AND VALUE(T1.KARI_TEISHI_FLG, '0') = '0' ");
        stb.append("     AND VALUE(T1.TEISHI_FLG, '0') = '0' ");
        stb.append("     AND T1.SHISHUTSU_GK >= 0  ");
        stb.append(" GROUP BY ");
        stb.append("     T1.SHUUGAKU_NO, ");
        stb.append("     T1.KOJIN_NO, ");
        stb.append("     T1.SEQ, ");
        stb.append("     T1.SHIKIN_SHUBETSU, ");
        stb.append("     N1.ABBV1, ");
        stb.append("     T1.FURIKOMI_DATE ");
        //追加給付
        stb.append(" ), TSUIKA_KYUHU_KEIKAKU AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SHUUGAKU_NO, ");
        stb.append("     T1.KOJIN_NO, ");
        stb.append("     T1.SEQ, ");
        stb.append("     T1.SHIKIN_SHUBETSU, ");
        stb.append("     N1.ABBV1 AS SHIKIN_NAME, ");
        stb.append("     T1.FURIKOMI_DATE, ");
        stb.append("     MAX(T1.SHISHUTSU_GK) AS SHISHUTSU_GK ");
        stb.append(" FROM ");
        stb.append("     TSUIKA_KYUHU_KEIKAKU_DAT T1 ");
        stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'T008' ");
        stb.append("          AND T1.SHIKIN_SHUBETSU = N1.NAMECD2 ");
        stb.append(" WHERE ");
        stb.append("     T1.SHINSEI_YEAR = '" + _param._shinseiYear + "' ");
        stb.append("     AND T1.FURIKOMI_DATE = '" + _param._shiteiDate + "' ");
        stb.append("     AND VALUE(T1.KARI_TEISHI_FLG, '0') = '0' ");
        stb.append("     AND VALUE(T1.TEISHI_FLG, '0') = '0' ");
        stb.append("     AND T1.SHISHUTSU_GK >= 0  ");
        stb.append(" GROUP BY ");
        stb.append("     T1.SHUUGAKU_NO, ");
        stb.append("     T1.KOJIN_NO, ");
        stb.append("     T1.SEQ, ");
        stb.append("     T1.SHIKIN_SHUBETSU, ");
        stb.append("     N1.ABBV1, ");
        stb.append("     T1.FURIKOMI_DATE ");

        stb.append(" ), DAIRI_KEIKAKU AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SHUUGAKU_NO, ");
        stb.append("     T1.KOJIN_NO, ");
        stb.append("     T1.SHIKIN_SHUBETSU, ");
        stb.append("     N1.ABBV1 AS SHIKIN_NAME, ");
        stb.append("     T1.FURIKOMI_DATE, ");
        stb.append("     SUM(T1.BUN_FURIKOMI_GK) AS BUN_FURIKOMI_GK, ");
        stb.append("     SUM(T1.BUN_DAIRI_FURIKOMI_GK) AS BUN_DAIRI_FURIKOMI_GK ");
        stb.append(" FROM ");
        stb.append("     FURIKOMI_KYUHU_BUNMEISAI_DAT T1 ");
        stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'T008' ");
        stb.append("          AND T1.SHIKIN_SHUBETSU = N1.NAMECD2 ");
        stb.append(" WHERE ");
        stb.append("     T1.SHINSEI_YEAR = '" + _param._shinseiYear + "' ");
        stb.append("     AND T1.FURIKOMI_DATE = '" + _param._shiteiDate + "' ");
        stb.append("     AND (VALUE(T1.BUN_FURIKOMI_GK, 0) > 0 OR VALUE(T1.BUN_DAIRI_FURIKOMI_GK, 0) > 0)  ");
        stb.append(" GROUP BY ");
        stb.append("     T1.SHUUGAKU_NO, ");
        stb.append("     T1.KOJIN_NO, ");
        stb.append("     T1.SHIKIN_SHUBETSU, ");
        stb.append("     N1.ABBV1, ");
        stb.append("     T1.FURIKOMI_DATE ");
        stb.append(" ), KOUZA AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.KOJIN_NO, ");
        stb.append("     T1.TAISHOUSHA_DIV, ");
        stb.append("     T1.KOUZA_DIV, ");
        stb.append("     T1.S_DATE, ");
        stb.append("     T1.BANKCD, ");
        stb.append("     BANK.BANKNAME, ");
        stb.append("     T1.BRANCHCD, ");
        stb.append("     BANK.BRANCHNAME, ");
        stb.append("     T1.YOKIN_DIV, ");
        stb.append("     N1.ABBV1 AS YOKIN_NAME, ");
        stb.append("     T1.ACCOUNT_NO, ");
        stb.append("     CONCAT(CONCAT(T1.BANK_MEIGI_SEI_KANA, ' '), T1.BANK_MEIGI_MEI_KANA) AS KOUZA_NAME ");
        stb.append(" FROM ");
        stb.append("     KOJIN_KOUZA_BANK_DAT T1 ");
        stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'T032' ");
        stb.append("          AND T1.YOKIN_DIV = N1.NAMECD2 ");
        stb.append("     LEFT JOIN BANK_MST BANK ON T1.BANKCD = BANK.BANKCD ");
        stb.append("          AND T1.BRANCHCD = BANK.BRANCHCD, ");
        stb.append("     ( ");
        stb.append("     SELECT ");
        stb.append("         T1.KOJIN_NO, ");
        stb.append("         T1.TAISHOUSHA_DIV, ");
        stb.append("         T1.KOUZA_DIV, ");
        stb.append("         MAX(T1.S_DATE) AS S_DATE ");
        stb.append("     FROM ");
        stb.append("         KOJIN_KOUZA_BANK_DAT T1 ");
        stb.append("     WHERE ");
        stb.append("         T1.TAISHOUSHA_DIV IN ('4','5') ");
        stb.append("         AND T1.KOUZA_DIV = '2' ");
        stb.append("     GROUP BY ");
        stb.append("         T1.KOJIN_NO, ");
        stb.append("         T1.TAISHOUSHA_DIV, ");
        stb.append("         T1.KOUZA_DIV ");
        stb.append("     ) T2 ");
        stb.append(" WHERE ");
        stb.append("     T1.KOJIN_NO = T2.KOJIN_NO ");
        stb.append("     AND T1.TAISHOUSHA_DIV = T2.TAISHOUSHA_DIV ");
        stb.append("     AND T1.KOUZA_DIV = T2.KOUZA_DIV ");
        stb.append("     AND T1.S_DATE = T2.S_DATE ");
        stb.append(" ), MAIN_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.H_SCHOOL_CD, ");
        stb.append("     T1.KATEI_DIV, ");
        stb.append("     T1.GRADE, ");
        stb.append("     CAST(T1.GRADE as int) AS GRADE_INT, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     CAST(T1.ATTENDNO as int) AS ATTENDNO_INT, ");
        stb.append("     T1.KOJIN_NO, ");
        stb.append("     VALUE(SCHOOL.NAME, '') AS SCHOOL_NAME, ");
        stb.append("     SCHOOL.SCHOOL_DISTCD, ");
        stb.append("     N1.NAME1 AS DIST_NAME, ");
        stb.append("     T1.SHUUGAKU_NO, ");
        stb.append("     CONCAT(CONCAT(SHINKEN.FAMILY_NAME, '　'), SHINKEN.FIRST_NAME) AS HOGO_NAME, ");
        stb.append("     CASE WHEN TSU_KEIKAKU.SHISHUTSU_GK IS NOT NULL THEN TSU_KEIKAKU.SHIKIN_NAME ");
        stb.append("          WHEN TSUIKA_KYUHU_KEIKAKU.SHISHUTSU_GK IS NOT NULL THEN TSUIKA_KYUHU_KEIKAKU.SHIKIN_NAME ");
        stb.append("          ELSE KEIKAKU.SHIKIN_NAME ");
        stb.append("     END AS SHIKIN_NAME, ");
        stb.append("     SHINKEN.ZIPCD, ");
        stb.append("     SHINKEN.ADDR1, ");
        stb.append("     SHINKEN.ADDR2, ");
        stb.append("     CASE WHEN TSU_KEIKAKU.SHISHUTSU_GK IS NOT NULL THEN TSU_KEIKAKU.FURIKOMI_DATE ");
        stb.append("          WHEN TSUIKA_KYUHU_KEIKAKU.SHISHUTSU_GK IS NOT NULL THEN TSUIKA_KYUHU_KEIKAKU.FURIKOMI_DATE ");
        stb.append("          ELSE KEIKAKU.FURIKOMI_DATE ");
        stb.append("     END AS FURIKOMI_DATE, ");
        stb.append("     KEIKAKU.SHISHUTSU_GK, ");
        stb.append("     TSU_KEIKAKU.SHISHUTSU_GK AS TSU_SHISHUTSU_GK, ");
        stb.append("     CASE WHEN TSU_KEIKAKU.SHISHUTSU_GK IS NOT NULL THEN '1' ELSE '0' END AS TSU_FLG, ");
        stb.append("     TSUIKA_KYUHU_KEIKAKU.SHISHUTSU_GK AS TSUIKA_KYUHU_SHISHUTSU_GK, ");
        stb.append("     CASE WHEN TSUIKA_KYUHU_KEIKAKU.SHISHUTSU_GK IS NOT NULL THEN '1' ELSE '0' END AS TSUIKA_KYUHU_FLG, ");
        stb.append("     KOUZA.BANKNAME, ");
        stb.append("     KOUZA.BRANCHNAME, ");
        stb.append("     KOUZA.YOKIN_NAME, ");
        stb.append("     KOUZA.ACCOUNT_NO, ");
        stb.append("     KOUZA.KOUZA_NAME ");
        stb.append(" FROM ");
        stb.append("     KOJIN_SHINSEI_KYUHU_DAT T1 ");
        stb.append("     LEFT JOIN V_KOJIN_HIST_DAT KOJIN ON T1.KOJIN_NO = KOJIN.KOJIN_NO ");
        stb.append("     LEFT JOIN SCHOOL_DAT SCHOOL ON T1.H_SCHOOL_CD = SCHOOL.SCHOOLCD ");
        stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'T003' ");
        stb.append("          AND SCHOOL.SCHOOL_DISTCD = N1.NAMECD2 ");
        stb.append("     LEFT JOIN SHINKENSHA_HIST_DAT SHINKEN ON T1.HOGOSHA_CD = SHINKEN.SHINKEN_CD ");
        stb.append("     LEFT JOIN KEIKAKU ON T1.SHUUGAKU_NO = KEIKAKU.SHUUGAKU_NO ");
        stb.append("           AND T1.KOJIN_NO = KEIKAKU.KOJIN_NO ");
        stb.append("           AND T1.SEQ = KEIKAKU.SEQ ");
        stb.append("     LEFT JOIN KOUZA ON T1.KOJIN_NO = KOUZA.KOJIN_NO AND KOUZA.TAISHOUSHA_DIV = '4' ");
        stb.append("     LEFT JOIN TSU_KEIKAKU  ON T1.SHUUGAKU_NO = TSU_KEIKAKU.SHUUGAKU_NO ");
        stb.append("           AND T1.KOJIN_NO = TSU_KEIKAKU.KOJIN_NO ");
        stb.append("           AND T1.SEQ = TSU_KEIKAKU.SEQ ");
        stb.append("     LEFT JOIN TSUIKA_KYUHU_KEIKAKU ON T1.SHUUGAKU_NO = TSUIKA_KYUHU_KEIKAKU.SHUUGAKU_NO ");
        stb.append("           AND T1.KOJIN_NO = TSUIKA_KYUHU_KEIKAKU.KOJIN_NO ");
        stb.append("           AND T1.SEQ = TSUIKA_KYUHU_KEIKAKU.SEQ ");
        stb.append(" WHERE ");
        stb.append("     T1.SHINSEI_YEAR = '" + _param._shinseiYear + "' ");
        stb.append("     AND VALUE(T1.CANCEL_FLG, '0') = '0' ");
        stb.append("     AND VALUE(T1.KETTEI_FLG, '0') = '1' ");
        stb.append("     AND T1.KETTEI_DATE IS NOT NULL ");
        stb.append("     AND T1.H_SCHOOL_CD IN " + _param._schoolInState + " ");
        stb.append("     AND NOT EXISTS ( ");
        stb.append("         SELECT ");
        stb.append("             'X' ");
        stb.append("         FROM ");
        stb.append("             DAIRI_KEIKAKU ");
        stb.append("         WHERE ");
        stb.append("                 DAIRI_KEIKAKU.SHUUGAKU_NO = KEIKAKU.SHUUGAKU_NO ");
        stb.append("             AND DAIRI_KEIKAKU.KOJIN_NO = KEIKAKU.KOJIN_NO ");
        stb.append("             AND DAIRI_KEIKAKU.SHIKIN_SHUBETSU = KEIKAKU.SHIKIN_SHUBETSU ");
        stb.append("             AND (VALUE(DAIRI_KEIKAKU.BUN_FURIKOMI_GK, 0) > 0 OR VALUE(DAIRI_KEIKAKU.BUN_DAIRI_FURIKOMI_GK, 0) > 0) ");
        stb.append("     ) ");
        stb.append("     AND ( ");
        stb.append("          KEIKAKU.KOJIN_NO IS NOT NULL ");
        stb.append("          OR TSU_KEIKAKU.KOJIN_NO IS NOT NULL ");
        stb.append("          OR TSUIKA_KYUHU_KEIKAKU.KOJIN_NO IS NOT NULL ");
        stb.append("     ) ");
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     T1.H_SCHOOL_CD, ");
        stb.append("     T1.KATEI_DIV, ");
        stb.append("     T1.GRADE, ");
        stb.append("     CAST(T1.GRADE as int) AS GRADE_INT, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     CAST(T1.ATTENDNO as int) AS ATTENDNO_INT, ");
        stb.append("     T1.KOJIN_NO, ");
        stb.append("     VALUE(SCHOOL.NAME, '') AS SCHOOL_NAME, ");
        stb.append("     SCHOOL.SCHOOL_DISTCD, ");
        stb.append("     N1.NAME1 AS DIST_NAME, ");
        stb.append("     T1.SHUUGAKU_NO, ");
        stb.append("     CONCAT(CONCAT(SHINKEN.FAMILY_NAME, '　'), SHINKEN.FIRST_NAME) AS HOGO_NAME, ");
        stb.append("     DAIRI_KEIKAKU.SHIKIN_NAME, ");
        stb.append("     SHINKEN.ZIPCD, ");
        stb.append("     SHINKEN.ADDR1, ");
        stb.append("     SHINKEN.ADDR2, ");
        stb.append("     DAIRI_KEIKAKU.FURIKOMI_DATE, ");
        stb.append("     DAIRI_KEIKAKU.BUN_FURIKOMI_GK AS SHISHUTSU_GK, ");
        stb.append("     0 AS TSU_SHISHUTSU_GK, ");
        stb.append("     '0' AS TSU_FLG, ");
        stb.append("     0 AS TSUIKA_KYUHU_SHISHUTSU_GK, ");
        stb.append("     '0' AS TSUIKA_KYUHU_FLG, ");
        stb.append("     KOUZA.BANKNAME, ");
        stb.append("     KOUZA.BRANCHNAME, ");
        stb.append("     KOUZA.YOKIN_NAME, ");
        stb.append("     KOUZA.ACCOUNT_NO, ");
        stb.append("     KOUZA.KOUZA_NAME ");
        stb.append(" FROM ");
        stb.append("     KOJIN_SHINSEI_KYUHU_DAT T1 ");
        stb.append("     LEFT JOIN V_KOJIN_HIST_DAT KOJIN ON T1.KOJIN_NO = KOJIN.KOJIN_NO ");
        stb.append("     LEFT JOIN SCHOOL_DAT SCHOOL ON T1.H_SCHOOL_CD = SCHOOL.SCHOOLCD ");
        stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'T003' ");
        stb.append("          AND SCHOOL.SCHOOL_DISTCD = N1.NAMECD2 ");
        stb.append("     LEFT JOIN SHINKENSHA_HIST_DAT SHINKEN ON T1.HOGOSHA_CD = SHINKEN.SHINKEN_CD ");
        stb.append("     INNER JOIN KEIKAKU ON T1.SHUUGAKU_NO = KEIKAKU.SHUUGAKU_NO ");
        stb.append("           AND T1.KOJIN_NO = KEIKAKU.KOJIN_NO ");
        stb.append("           AND T1.SEQ = KEIKAKU.SEQ ");
        stb.append("     INNER JOIN KOUZA ON T1.KOJIN_NO = KOUZA.KOJIN_NO AND KOUZA.TAISHOUSHA_DIV = '4' ");
        stb.append("     INNER JOIN DAIRI_KEIKAKU ON DAIRI_KEIKAKU.SHUUGAKU_NO = KEIKAKU.SHUUGAKU_NO ");
        stb.append("                             AND DAIRI_KEIKAKU.KOJIN_NO = KEIKAKU.KOJIN_NO ");
        stb.append("                             AND DAIRI_KEIKAKU.SHIKIN_SHUBETSU = KEIKAKU.SHIKIN_SHUBETSU ");
        stb.append("                             AND VALUE(DAIRI_KEIKAKU.BUN_FURIKOMI_GK, 0) > 0 ");
        stb.append(" WHERE ");
        stb.append("     T1.SHINSEI_YEAR = '" + _param._shinseiYear + "' ");
        stb.append("     AND VALUE(T1.CANCEL_FLG, '0') = '0' ");
        stb.append("     AND VALUE(T1.KETTEI_FLG, '0') = '1' ");
        stb.append("     AND T1.KETTEI_DATE IS NOT NULL ");
        stb.append("     AND T1.H_SCHOOL_CD IN " + _param._schoolInState + " ");
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     T1.H_SCHOOL_CD, ");
        stb.append("     T1.KATEI_DIV, ");
        stb.append("     T1.GRADE, ");
        stb.append("     CAST(T1.GRADE as int) AS GRADE_INT, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     CAST(T1.ATTENDNO as int) AS ATTENDNO_INT, ");
        stb.append("     T1.KOJIN_NO, ");
        stb.append("     VALUE(SCHOOL.NAME, '') AS SCHOOL_NAME, ");
        stb.append("     SCHOOL.SCHOOL_DISTCD, ");
        stb.append("     N1.NAME1 AS DIST_NAME, ");
        stb.append("     T1.SHUUGAKU_NO, ");
        stb.append("     CONCAT(CONCAT(SHINKEN.FAMILY_NAME, '　'), SHINKEN.FIRST_NAME) AS HOGO_NAME, ");
        stb.append("     DAIRI_KEIKAKU.SHIKIN_NAME, ");
        stb.append("     SHINKEN.ZIPCD, ");
        stb.append("     SHINKEN.ADDR1, ");
        stb.append("     SHINKEN.ADDR2, ");
        stb.append("     DAIRI_KEIKAKU.FURIKOMI_DATE, ");
        stb.append("     DAIRI_KEIKAKU.BUN_DAIRI_FURIKOMI_GK AS SHISHUTSU_GK, ");
        stb.append("     0 AS TSU_SHISHUTSU_GK,  ");
        stb.append("     '0' AS TSU_FLG, ");
        stb.append("     0 AS TSUIKA_KYUHU_SHISHUTSU_GK, ");
        stb.append("     '0' AS TSUIKA_KYUHU_FLG, ");
        stb.append("     KOUZA.BANKNAME, ");
        stb.append("     KOUZA.BRANCHNAME, ");
        stb.append("     KOUZA.YOKIN_NAME, ");
        stb.append("     KOUZA.ACCOUNT_NO, ");
        stb.append("     KOUZA.KOUZA_NAME ");
        stb.append(" FROM ");
        stb.append("     KOJIN_SHINSEI_KYUHU_DAT T1 ");
        stb.append("     LEFT JOIN V_KOJIN_HIST_DAT KOJIN ON T1.KOJIN_NO = KOJIN.KOJIN_NO ");
        stb.append("     LEFT JOIN SCHOOL_DAT SCHOOL ON T1.H_SCHOOL_CD = SCHOOL.SCHOOLCD ");
        stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'T003' ");
        stb.append("          AND SCHOOL.SCHOOL_DISTCD = N1.NAMECD2 ");
        stb.append("     LEFT JOIN SHINKENSHA_HIST_DAT SHINKEN ON T1.HOGOSHA_CD = SHINKEN.SHINKEN_CD ");
        stb.append("     INNER JOIN KEIKAKU ON T1.SHUUGAKU_NO = KEIKAKU.SHUUGAKU_NO ");
        stb.append("           AND T1.KOJIN_NO = KEIKAKU.KOJIN_NO ");
        stb.append("           AND T1.SEQ = KEIKAKU.SEQ ");
        stb.append("     INNER JOIN KOUZA ON T1.KOJIN_NO = KOUZA.KOJIN_NO AND KOUZA.TAISHOUSHA_DIV = '5' ");
        stb.append("     INNER JOIN DAIRI_KEIKAKU ON DAIRI_KEIKAKU.SHUUGAKU_NO = KEIKAKU.SHUUGAKU_NO ");
        stb.append("                             AND DAIRI_KEIKAKU.KOJIN_NO = KEIKAKU.KOJIN_NO ");
        stb.append("                             AND DAIRI_KEIKAKU.SHIKIN_SHUBETSU = KEIKAKU.SHIKIN_SHUBETSU ");
        stb.append("                             AND VALUE(DAIRI_KEIKAKU.BUN_DAIRI_FURIKOMI_GK, 0) > 0 ");
        stb.append(" WHERE ");
        stb.append("     T1.SHINSEI_YEAR = '" + _param._shinseiYear + "' ");
        stb.append("     AND VALUE(T1.CANCEL_FLG, '0') = '0' ");
        stb.append("     AND VALUE(T1.KETTEI_FLG, '0') = '1' ");
        stb.append("     AND T1.KETTEI_DATE IS NOT NULL ");
        stb.append("     AND T1.H_SCHOOL_CD IN " + _param._schoolInState + " ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     H_SCHOOL_CD, ");
        stb.append("     KATEI_DIV, ");
        stb.append("     GRADE, ");
        stb.append("     GRADE_INT, ");
        stb.append("     MAX(HR_CLASS || ATTENDNO) AS HR_ATTEND, ");
        stb.append("     KOJIN_NO, ");
        stb.append("     SCHOOL_NAME, ");
        stb.append("     SCHOOL_DISTCD, ");
        stb.append("     DIST_NAME, ");
        stb.append("     SHUUGAKU_NO, ");
        stb.append("     HOGO_NAME, ");
        stb.append("     SHIKIN_NAME, ");
        stb.append("     ZIPCD, ");
        stb.append("     ADDR1, ");
        stb.append("     ADDR2, ");
        stb.append("     FURIKOMI_DATE, ");
        stb.append("     SUM(VALUE(SHISHUTSU_GK, 0)) AS SHISHUTSU_GK, ");
        stb.append("     SUM(VALUE(TSU_SHISHUTSU_GK, 0)) AS TSU_SHISHUTSU_GK,  ");
        stb.append("     TSU_FLG, ");
        stb.append("     SUM(VALUE(TSUIKA_KYUHU_SHISHUTSU_GK, 0)) AS TSUIKA_KYUHU_SHISHUTSU_GK,  ");
        stb.append("     TSUIKA_KYUHU_FLG, ");
        stb.append("     BANKNAME, ");
        stb.append("     BRANCHNAME, ");
        stb.append("     YOKIN_NAME, ");
        stb.append("     ACCOUNT_NO, ");
        stb.append("     KOUZA_NAME ");
        stb.append(" FROM ");
        stb.append("     MAIN_T ");
        stb.append(" GROUP BY ");
        stb.append("     H_SCHOOL_CD, ");
        stb.append("     KATEI_DIV, ");
        stb.append("     GRADE, ");
        stb.append("     GRADE_INT, ");
        stb.append("     KOJIN_NO, ");
        stb.append("     SCHOOL_NAME, ");
        stb.append("     SCHOOL_DISTCD, ");
        stb.append("     DIST_NAME, ");
        stb.append("     SHUUGAKU_NO, ");
        stb.append("     HOGO_NAME, ");
        stb.append("     SHIKIN_NAME, ");
        stb.append("     ZIPCD, ");
        stb.append("     ADDR1, ");
        stb.append("     ADDR2, ");
        stb.append("     FURIKOMI_DATE, ");
        stb.append("     TSU_FLG, ");
        stb.append("     TSUIKA_KYUHU_FLG, ");
        stb.append("     BANKNAME, ");
        stb.append("     BRANCHNAME, ");
        stb.append("     YOKIN_NAME, ");
        stb.append("     ACCOUNT_NO, ");
        stb.append("     KOUZA_NAME ");
        stb.append(" ORDER BY ");
        stb.append("     H_SCHOOL_CD, ");
        stb.append("     KATEI_DIV, ");
        stb.append("     GRADE_INT, ");
        stb.append("     HR_ATTEND, ");
        stb.append("     KOJIN_NO ");

        return stb.toString();
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    private static class School {
        final String _hSchoolCd;
        final String _schoolName;
        final String _schoolDistcd;
        final String _distName;
        final List _printDataList = new ArrayList();

        public School(
                final String hSchoolCd,
                final String schoolName,
                final String schoolDistcd,
                final String distName) {
            _hSchoolCd      = hSchoolCd;
            _schoolName     = schoolName;
            _schoolDistcd   = schoolDistcd;
            _distName       = distName;
        }

        private static School getSchool(final String hSchoolCd, final List list) {
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final School school = (School) it.next();
                if (school._hSchoolCd.equals(hSchoolCd)) {
                    return school;
                }
            }
            return null;
        }
    }

    private static class Goukei {
        final String _name;
        Goukei(final String name) {
            _name = name;
        }
    }

    private class PrintData {
        final String _shuugakuNo;
        final String _hogoName;
        final String _shikinName;
        final String _zipcd;
        final String _addr1;
        final String _addr2;
        final String _furikomiDate;
        final String _shishutsuGk;
        final String _tsu_shishutsuGk;
        final String _tsuFlg;
        final String _tsuikaKyuhu_shishutsuGk;
        final String _tsuikaKyuhuFlg;
        final String _bankname;
        final String _branchname;
        final String _yokinName;
        final String _accountNo;
        final String _kouzaName;

        public PrintData(
                final String shuugakuNo,
                final String hogoName,
                final String shikinName,
                final String zipcd,
                final String addr1,
                final String addr2,
                final String furikomiDate,
                final String shishutsuGk,
                final String tsu_shishutsuGk,
                final String tsuFlg,
                final String tsuikaKyuhu_shishutsuGk,
                final String tsuikaKyuhuFlg,
                final String bankname,
                final String branchname,
                final String yokinName,
                final String accountNo,
                final String kouzaName
        ) {
            _shuugakuNo     = shuugakuNo;
            _hogoName       = hogoName;
            _shikinName     = shikinName;
            _zipcd          = zipcd;
            _addr1          = addr1;
            _addr2          = addr2;
            _furikomiDate   = furikomiDate;
            _shishutsuGk    = shishutsuGk;
            _tsu_shishutsuGk = tsu_shishutsuGk;
            _tsuFlg          = tsuFlg;
            _tsuikaKyuhu_shishutsuGk = tsuikaKyuhu_shishutsuGk;
            _tsuikaKyuhuFlg = tsuikaKyuhuFlg;
            _bankname       = bankname;
            _branchname     = branchname;
            _yokinName      = yokinName;
            _accountNo      = accountNo;
            _kouzaName      = kouzaName;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 76922 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _shinseiYear;
        private final String _loginDate;
        private String _schoolInState;
        private final String _date;
        private final String _shiteiDate;
        private final ShugakuDate _shugakuDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _shinseiYear = request.getParameter("SHINSEI_YEAR");
            _loginDate = request.getParameter("LOGIN_DATE");
            _shugakuDate = new ShugakuDate(db2);
            _date = _shugakuDate.d7toDateStr(request.getParameter("DATE"));
            _shiteiDate = _shugakuDate.d7toDateStr(request.getParameter("SHITEI_DATE"));

            final String schools[] = request.getParameterValues("SCHOOL_SELECTED");
            String schoolInState = "( ";
            String sep = "";
            for (int ia = 0; ia < schools.length; ia++) {
                schoolInState += sep + "'" + schools[ia] + "'";
                sep = ", ";
            }
            _schoolInState = schoolInState + " )";
        }
    }
}

// eof
