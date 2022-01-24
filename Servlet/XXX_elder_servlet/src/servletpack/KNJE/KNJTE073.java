/*
 * $Id: 9cac0872e52c2dcd71ad44af03bc93a38e8d8cd3 $
 *
 * 作成日: 2012/12/13
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;


import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.ShugakuDate;

/**
 * 催告状
 */
public class KNJTE073 {

    private static final Log log = LogFactory.getLog(KNJTE073.class);

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
    
    private void VrsOut(final String[] field1, final String[] data, final Vrw32alp svf) {
        if (null == data) {
            return;
        }
        for (int i = 0; i < Math.min(field1.length, data.length); i++) {
            svf.VrsOut(field1[i], data[i]);
        }
    }

    private List getShuugakuseiList(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List list = new ArrayList();
        try {
            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                final String shuugakuNo = rs.getString("SHUUGAKU_NO");
                Shuugakusei shuugakusei = getShugakusei(list, shuugakuNo);
                if (null == shuugakusei) {
                    shuugakusei = new Shuugakusei(shuugakuNo, rs.getString("MINOU_GK_TOTAL"),  rs.getString("ZIPCD"),  rs.getString("NAME"),  rs.getString("ADDR1"),  rs.getString("ADDR2"));
                    list.add(shuugakusei);
                }
                shuugakusei._choteiNoufuList.add(new ChoteiNoufu(rs.getString("CHOTEI_KAISU"), rs.getString("CHOTEI_YM"), rs.getString("NOUFU_KIGEN_ORG"), rs.getString("MINOU_GK")));
            }
            
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;
    }
    
    private Shuugakusei getShugakusei(final List list, final String shuugakuNo) {
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Shuugakusei shuugakusei = (Shuugakusei) it.next();
            if (shuugakusei._shuugakuNo.equals(shuugakuNo)) {
                return shuugakusei;
            }
        }
        return null;
    }
    
    private void printAddress(final Vrw32alp svf, final String addr1, final String addr2) {
        final String[] addr1a = KNJ_EditEdit.get_token(addr1, 50, 2);
        final String[] addr2a = KNJ_EditEdit.get_token(addr2, 50, 2);
        final List addr = new ArrayList();
        if (null != addr1a && !StringUtils.isBlank(addr1a[0])) addr.add(addr1a[0]);
        if (null != addr1a && !StringUtils.isBlank(addr1a[1])) addr.add(addr1a[1]);
        if (null != addr2a && !StringUtils.isBlank(addr2a[0])) addr.add(addr2a[0]);
        if (null != addr2a && !StringUtils.isBlank(addr2a[1])) addr.add(addr2a[1]);
        final String[] fieldsNo = new String[] {"1_2", "1_3", "2_2", "2_3"};
        for (int j = 0; j < addr.size(); j++) {
            svf.VrsOut("ADDRESSS" + fieldsNo[j], (String) addr.get(j));
        }
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final List list = getShuugakuseiList(db2);
        
        int pageall = 0;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Shuugakusei shuugakusei = (Shuugakusei) it.next();
            svf.VrSetForm("KNJTE073_1.frm", 4);
            pageall += 1;
            
            svf.VrsOut("REM", shuugakusei._shuugakuNo + "         " + String.valueOf(pageall));
            
            svf.VrsOut("STAMP", getImageFile("SAINYU_CHOSHUSHA.bmp", "image/stamp"));
            
            svf.VrsOut("ZIP_NO", shuugakusei._zipCd);
            printAddress(svf, shuugakusei._addr1, shuugakusei._addr2);
//            svf.VrsOut("ADDRESS1", shuugakusei._addr1);
//            VrsOut(new String[]{"ADDRESS2", "ADDRESS3"}, KNJ_EditEdit.get_token(shuugakusei._addr2, 40, 2), svf);
            svf.VrsOut("NAME", shuugakusei._name);
            
            svf.VrsOut("GOVERNER", "京都府歳入徴収者　京都府知事");
            svf.VrsOut("N_NO1", "京都府歳入徴収者　京都府知事");
            svf.VrsOut("NOTIFI_DAY", _param._shugakuDate.formatDate(_param._outputDate));
            svf.VrsOut("FIELD1", "京都府高等学校等修学資金返還金");

            svf.VrsOut("N_NO1", shuugakusei._shuugakuNo);
            svf.VrsOut("NON_PAY", shuugakusei._minouGkTotal);
            svf.VrsOut("LIMIT_DAY", _param._shugakuDate.formatDate(_param._nounyuuKigen));

            svf.VrsOut("CONTACT_ZIP_NO", "〒612-0064");
            svf.VrsOut("CONTACT_ADDRESS", "京都市伏見区桃山毛利長門西町");
            svf.VrsOut("CONTACT_ADDRESS2", "京都府総合教育センター内");
            svf.VrsOut("TELNO", "０７５-５７４-７５１８");
            
            final int p1count = Math.min(shuugakusei._choteiNoufuList.size(), 20);
            for (int i = 0; i < p1count; i++) {
                final ChoteiNoufu choteiNoufu = (ChoteiNoufu) shuugakusei._choteiNoufuList.get(i);
                svf.VrsOut("RECEPT_NO", choteiNoufu._choteiKaisu); // 調停回数
                svf.VrsOut("FIRST_LIMIT", _param._shugakuDate.formatNentuki(choteiNoufu._choteiYm));
                svf.VrsOut("LATE_MONEY", choteiNoufu._minouGk);
                svf.VrEndRecord();
            }
            for (int i = p1count + 1; i <= 20; i++) {
                svf.VrEndRecord();
            }

            if (shuugakusei._choteiNoufuList.size() > 20) {
                final List choteiNoufuListPage2 = shuugakusei._choteiNoufuList.subList(20, shuugakusei._choteiNoufuList.size());
                final List pageList = getPageList(choteiNoufuListPage2, 80);
                for (final Iterator pi = pageList.iterator(); pi.hasNext();) {
                    final List choteiNoufuList = (List) pi.next();
                    svf.VrSetForm("KNJTE073_2.frm", 4);
                    pageall += 1;
                    final int p2count = choteiNoufuList.size();
                    for (int i = 0; i < p2count; i++) {
                        final ChoteiNoufu choteiNoufu = (ChoteiNoufu) choteiNoufuList.get(i);
                        svf.VrsOut("RECEPT_NO", choteiNoufu._choteiKaisu); // 調停回数
                        svf.VrsOut("FIRST_LIMIT", _param._shugakuDate.formatNentuki(choteiNoufu._choteiYm));
                        svf.VrsOut("LATE_MONEY", choteiNoufu._minouGk);
                        svf.VrEndRecord();
                    }
                    for (int i = p2count + 1; i <= 80; i++) {
                        svf.VrEndRecord();
                    }
                }
            }
            _hasData = true;
        }
    }
    
    private List getPageList(final List list, final int size) {
        final List pageList = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= size) {
                current = new ArrayList();
                pageList.add(current);
            }
            current.add(o);
        }
        return pageList;
    }
    
    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     CASE WHEN S1.ZIP_CD IS NOT NULL THEN S1.ZIP_CD ");
        stb.append("          WHEN S2.ZIP_CD IS NOT NULL THEN S2.ZIP_CD ");
        stb.append("          ELSE V3.ZIPCD ");
        stb.append("     END AS ZIPCD, ");
        stb.append("     CASE WHEN S1.ADDR1 IS NOT NULL THEN S1.ADDR1 ");
        stb.append("          WHEN S2.ADDR1 IS NOT NULL THEN S2.ADDR1 ");
        stb.append("          ELSE V3.ADDR1 ");
        stb.append("     END AS ADDR1, ");
        stb.append("     CASE WHEN S1.ADDR2 IS NOT NULL THEN S1.ADDR2 ");
        stb.append("          WHEN S2.ADDR2 IS NOT NULL THEN S2.ADDR2 ");
        stb.append("          ELSE V3.ADDR2 ");
        stb.append("     END AS ADDR2, ");
        stb.append("     CASE WHEN S1.SOUFU_NAME IS NOT NULL THEN S1.SOUFU_NAME ");
        stb.append("              WHEN S2.SOUFU_NAME IS NOT NULL THEN S2.SOUFU_NAME ");
        stb.append("              ELSE CONCAT(CONCAT(V3.FAMILY_NAME,'　'),V3.FIRST_NAME) ");
        stb.append("     END AS NAME, ");
        stb.append("     T1.SHIKIN_SHUBETSU, ");
        stb.append("     V1.SHUUGAKU_NO, ");
        stb.append("     V1.CHOTEI_KAISU, ");
        stb.append("     V1.CHOTEI_YM, ");
        stb.append("     V1.NOUFU_KIGEN_ORG, ");
        stb.append("     V1.MINOU_GK, ");
        stb.append("     V2.MINOU_GK_TOTAL ");
        stb.append(" FROM ");
        stb.append("     V_CHOTEI_NOUFU_ADD V1     LEFT JOIN (SELECT ");
        stb.append("                                             SHUUGAKU_NO, ");
        stb.append("                                             SUM(MINOU_GK) AS MINOU_GK_TOTAL ");
        stb.append("                                         FROM ");
        stb.append("                                             V_CHOTEI_NOUFU_ADD ");
        stb.append("                                         WHERE ");
        stb.append("                                             NOUFU_KIGEN IS NOT NULL AND ");
        stb.append("                                             MINOU_GK > 0 AND ");
        stb.append("                                             TOKUSOKU_DATE IS NOT NULL ");
        stb.append("                                         GROUP BY ");
        stb.append("                                             SHUUGAKU_NO ");
        stb.append("                                         ) V2 ON V1.SHUUGAKU_NO = V2.SHUUGAKU_NO ");
        stb.append("     LEFT JOIN SAIKEN_DAT T1 ON V1.SHUUGAKU_NO = T1.SHUUGAKU_NO ");
        stb.append("     LEFT JOIN V_KOJIN_HIST_DAT V3 ON T1.KOJIN_NO = V3.KOJIN_NO ");
        stb.append("     LEFT JOIN SOUFU_ADDRESS_DAT S1 ON T1.KOJIN_NO = S1.KOJIN_NO AND T1.SHUUGAKU_NO = S1.SHUUGAKU_NO ");
        stb.append("     LEFT JOIN SOUFU_ADDRESS_DAT S2 ON T1.KOJIN_NO = S2.KOJIN_NO AND S2.SHUUGAKU_NO = '9999999' ");
        
        stb.append("     left join (SELECT SHUGAKU_NO AS SHUUGAKU_NO, CHOTEI_KAISU, COUNT(*) AS SOKU_CNT FROM SHUNO_CONVENI_DAT WHERE D_DATA_CD = '01' GROUP BY SHUGAKU_NO, CHOTEI_KAISU) CVN on V1.SHUUGAKU_NO = CVN.SHUUGAKU_NO and V1.CHOTEI_KAISU = CVN.CHOTEI_KAISU ");

        stb.append(" WHERE ");
        stb.append("     (CHUUI_FLG IS NULL OR CHUUI_FLG = '02') AND ");
        stb.append("     VALUE(CVN.SOKU_CNT, 0) < 1 AND ");
        stb.append("     V1.NOUFU_KIGEN IS NOT NULL ");
        stb.append("     AND V1.MINOU_GK > 0 ");
        stb.append("     AND V1.TOKUSOKU_DATE IS NOT NULL ");
        if (!StringUtils.isEmpty(_param._shuugakuNoStr)) {
            stb.append("     AND V1.SHUUGAKU_NO IN " + _param._shuugakuNoStr + " ");
        }
        stb.append(" ORDER BY ");
        stb.append("     V1.SHUUGAKU_NO, V1.CHOTEI_KAISU ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 67187 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }
    
    private static class Shuugakusei {
        final String _shuugakuNo;
        final String _minouGkTotal;
        final String _zipCd;
        final String _name;
        final String _addr1;
        final String _addr2;
        final List _choteiNoufuList = new ArrayList();
        Shuugakusei(final String shuugakuNo, final String minouGkTotal, final String zipCd, final String name, final String addr1, final String addr2) {
            _shuugakuNo = shuugakuNo;
            _minouGkTotal = minouGkTotal;
            _zipCd = zipCd;
            _name = name;
            _addr1 = addr1;
            _addr2 = addr2;
        }
    }
    
    private static class ChoteiNoufu {
        final String _choteiKaisu;
        final String _choteiYm;
        final String _noufuKigenOrg;
        final String _minouGk;
        ChoteiNoufu(final String choteiKaisu, final String choteiYm, final String noufuKigenOrg, final String minouGk) {
            _choteiKaisu = choteiKaisu;
            _choteiYm = choteiYm;
            _noufuKigenOrg = noufuKigenOrg;
            _minouGk = minouGk;
        }
    }
    
    /**
     * 写真データファイルの取得
     */
    private String getImageFile(final String filename, final String imageDir) {
        if (null == _param._documentRoot) {
            return null;
        } // DOCUMENTROOT
        if (null == imageDir) {
            return null;
        }
        final StringBuffer stb = new StringBuffer();
        stb.append(_param._documentRoot);
        stb.append("/");
        stb.append(imageDir);
        stb.append("/");
        stb.append(filename);
        final File file1 = new File(stb.toString());
        if (!file1.exists()) {
            return null;
        } // 写真データ存在チェック用
        return stb.toString();
    }

    /** パラメータクラス */
    private class Param {
        final String _shuugakuNoDiv;
        final String _shuugakuNoStr;
        final String _outputDate;
        final String _nounyuuKigen;
        final String _documentRoot;
        final ShugakuDate _shugakuDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _documentRoot = request.getParameter("DOCUMENTROOT");
            _shuugakuNoDiv = request.getParameter("SHUUGAKU_NO_DIV");
            if ("2".equals(_shuugakuNoDiv)) {
                final String[] splits = StringUtils.split(request.getParameter("SHUUGAKU_NO_LIST"), ",");
                if (null == splits) {
                    _shuugakuNoStr = null;
                } else {
                    final StringBuffer stb = new StringBuffer();
                    String comma = "";
                    stb.append("(");
                    for (int i = 0; i < splits.length; i++) {
                        stb.append(comma);
                        stb.append("'").append(splits[i]).append("'");
                        comma = ",";
                    }
                    stb.append(")");
                    _shuugakuNoStr = stb.toString();
                }
            } else {
                if (StringUtils.isBlank(request.getParameter("SHUUGAKU_NO"))) {
                    _shuugakuNoStr = null;
                } else {
                    final StringBuffer stb = new StringBuffer();
                    stb.append("(");
                    stb.append("'").append(request.getParameter("SHUUGAKU_NO")).append("'");
                    stb.append(")");
                    _shuugakuNoStr = stb.toString();
                }
            }
            _shugakuDate = new ShugakuDate(db2);
            _shugakuDate._printBlank = true;
            _outputDate = _shugakuDate.d7toDateStr(request.getParameter("OUTPUT_DATE"));
            _nounyuuKigen = _shugakuDate.d7toDateStr(request.getParameter("NOUNYUU_KIGEN"));
        }
    }
}

// eof

