/*
 * $Id: c274fadcf20df3faca030936b3633be7665ffb22 $
 *
 * 作成日: 2012/11/12
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.CustomerBarcode;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.ShugakuDate;

/**
 * 納入通知書・納付書 / 領収書
 */
public class KNJTE054 {

    private static final Log log = LogFactory.getLog(KNJTE054.class);

    private static final String PRGID_KNJTE054 = "KNJTE054"; // 納入通知書・領収書発行画面
    private static final String PRGID_KNJTE065 = "KNJTE065"; // 口座振替不能分・納付書一括発行画面
    private static final String PRGID_KNJTE066 = "KNJTE066"; // 口座振替不能分・納付書一括発行画面（速報データ突合）
    private static final String PRGID_KNJTE090 = "KNJTE090"; // 納付書一括再発行画面
    private static final String PRGID_KNJTG116 = "KNJTG116"; // 納付書再発行画面
    private static final String PRGID_KNJTG117 = "KNJTG117"; // 分割納付書画面

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

            //CustomerBarcode.test(db2, _param);

            _hasData = false;

            printMain(db2, svf);
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != _param) {
                for (final Iterator it = _param._psMap.values().iterator(); it.hasNext();) {
                    final PreparedStatement ps = (PreparedStatement) it.next();
                    DbUtils.closeQuietly(ps);
                }
            }

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
        final boolean isPrintCnsBarcode = "2".equals(_param._printDiv);
        
        final List list = getList(db2);
        
        log.info(" list size = " + list.size());
        
        try {
            int page = 0;
            for (final Iterator it = list.iterator(); it.hasNext();) {
                
                final ChoteiNoufu noufu = (ChoteiNoufu) it.next();
                page += 1;

                final String form;
                if (isPrintCnsBarcode) {
                    if (CvsBarcode.isNotTargetKingaku(noufu._henkanGk)) {
                        form = "KNJTE054_3.frm";
                        log.info(" form = " + form + " / " + noufu._shugakuNo);
                    } else {
                        form = "KNJTE054_2.frm";
                    }
                } else {
                    form = "KNJTE054_1.frm";
                }
                svf.VrSetForm(form, 1);

                svf.VrsOut("ZIP_NO", noufu._zipcd);
                if (getMS932Length(noufu._address1) > 40 || getMS932Length(noufu._address2) > 40) {
                    final String[] addr1 = KNJ_EditEdit.get_token(noufu._address1, 50, 2);
                    final String[] addr2 = KNJ_EditEdit.get_token(noufu._address2, 50, 2);
                    final List addr = new ArrayList();
                    if (null != addr1) {
                        if (!StringUtils.isBlank(addr1[0])) addr.add(addr1[0]);
                        if (!StringUtils.isBlank(addr1[1])) addr.add(addr1[1]);
                    }
                    if (null != addr2) {
                        if (!StringUtils.isBlank(addr2[0])) addr.add(addr2[0]);
                        if (!StringUtils.isBlank(addr2[1])) addr.add(addr2[1]);
                    }
                    for (int i = 0; i < 3; i++) {
                        final String[] fieldsNo = new String[] { String.valueOf(i * 2 + 1) + "_2",  String.valueOf(i * 2 + 1) + "_3",  String.valueOf(i * 2 + 2) + "_2",  String.valueOf(i * 2 + 2) + "_3" };
                        for (int j = 0; j < addr.size(); j++) {
                            svf.VrsOut("ADDRESSS" + fieldsNo[j], (String) addr.get(j));
                        }
                    }
                } else {
                    for (int i = 0; i < 3; i++) {
                        svf.VrsOut("ADDRESSS" + String.valueOf(i * 2 + 1) + "_1", noufu._address1);
                        svf.VrsOut("ADDRESSS" + String.valueOf(i * 2 + 2) + "_1", noufu._address2);
                    }
                }

                svf.VrsOut("NAME", noufu._name + "　様");
                
                String pageLineno = null;
                if (PRGID_KNJTE054.equals(_param._prgId) || PRGID_KNJTE065.equals(_param._prgId) || PRGID_KNJTE066.equals(_param._prgId) || PRGID_KNJTE090.equals(_param._prgId)) {
                    pageLineno = String.valueOf(page);
                }
                svf.VrsOut("REM", StringUtils.defaultString(noufu._shugakuNo) + " " + StringUtils.defaultString(noufu._choteiKaisu) + " " + StringUtils.defaultString(pageLineno));

                svf.VrsOut("TITLE1", "京都府　収納済通知書");
                svf.VrsOut("TITLE2", "京都府　収納書");
                if (PRGID_KNJTG116.equals(_param._prgId) || PRGID_KNJTE065.equals(_param._prgId) || PRGID_KNJTE066.equals(_param._prgId) || PRGID_KNJTG117.equals(_param._prgId) || PRGID_KNJTE090.equals(_param._prgId)) {
                    svf.VrsOut("TITLE3", "京都府　納付書・領収書");
                } else if (PRGID_KNJTE054.equals(_param._prgId)){
                    svf.VrsOut("TITLE3", "京都府　納入通知書・領収書");
                }

                svf.VrsOut("MONEY", noufu._henkanGk);
                svf.VrsOut("PAY_NO", noufu._noufubango);
                svf.VrsOut("CHECK_NO", ChoteiNoufu.kakuninBangou);
                svf.VrsOut("PAY_CLASS", ChoteiNoufu.noufuKubun);
                if (null != noufu._choteiNend) {
                    svf.VrsOut("YEAR", _param._shugakuDate.formatNen(noufu._choteiNend) + "度");
                }
                svf.VrsOut("PAY_LIMIT", _param._shugakuDate.formatDate(noufu._noufuKigen, false));
                svf.VrsOut("PAY_LIMIT2", _param._shugakuDate.formatDate(noufu._noufuKigen, false));
                if (null != _param._barcodeToriatsukaiKigen) {
                    svf.VrsOut("PAY_LIMIT2_CVS", _param._shugakuDate.formatDate(_param._barcodeToriatsukaiKigen, false));
                    svf.VrsOut("PAY_LIMIT3_CVS", _param._shugakuDate.formatDate(_param._barcodeToriatsukaiKigen, false));
                }
                svf.VrsOut("OCR1", noufu.createUpper(_param));
                svf.VrsOut("OCR2", noufu.createLower());
                svf.VrsOut("PAY_CONTENT1", "高等学校等修学資金貸付金返還金");
                
                String choteiYm = null;
                if (PRGID_KNJTG116.equals(_param._prgId) || PRGID_KNJTG117.equals(_param._prgId) || PRGID_KNJTE090.equals(_param._prgId)) {
                	choteiYm = noufu._choteiYm;
                } else if (PRGID_KNJTE054.equals(_param._prgId) || PRGID_KNJTE065.equals(_param._prgId) || PRGID_KNJTE066.equals(_param._prgId)) {
                	choteiYm = _param._choteiYm;
                }
                final String choteiYmstr = _param._shugakuDate.formatNentuki(choteiYm, false) + "分";

                svf.VrsOut("PAY_CONTENT2", choteiYmstr);
                svf.VrsOut("PAY_CONTENT3", "高等学校等修学資金貸");
                svf.VrsOut("PAY_CONTENT4", "付金返還金");
                svf.VrsOut("PAY_CONTENT5", choteiYmstr);
                svf.VrsOut("INQ", "京都府教育庁指導部高校教育課");
                svf.VrsOut("INQ2", "京都府教育庁");
                svf.VrsOut("INQ3", "指導部");
                svf.VrsOut("INQ4", "高校教育課");
                svf.VrsOut("NO", String.valueOf(page));
                svf.VrsOut("PAY_NAME", noufu._name + "　様");
                final String[] namea = KNJ_EditEdit.get_token(noufu._name + "　様", 20, 2);
                if (null != namea) {
                    for (int i = 0; i < 2; i++) {
                        svf.VrsOut("PAY_NAME" + (2 + i), namea[i]);
                    }
                }
                
//                svf.VrsOut("PAY_SPACE1", "京都府指定(代理)金融機関又は");
//                svf.VrsOut("PAY_SPACE2", "京都府収納代理金融機関");
                svf.VrsOut("PAY_SPACE3", "裏面に記載のとおり");
                if (PRGID_KNJTG116.equals(_param._prgId) || PRGID_KNJTG117.equals(_param._prgId) || PRGID_KNJTE090.equals(_param._prgId)) {
                    // 納付書
                    svf.VrsOut("PRINT_TITLE", "発行日");
                    svf.VrsOut("PRINTDAY", _param._shugakuDate.formatDate(noufu._reprintDate, false));
                } else if (PRGID_KNJTE065.equals(_param._prgId) || PRGID_KNJTE066.equals(_param._prgId)) {
                    // 納付書
                    svf.VrsOut("PRINT_TITLE", "発行日");
                    svf.VrsOut("PRINTDAY", _param._shugakuDate.formatDate(_param._issueDate, false));
                } else if (PRGID_KNJTE054.equals(_param._prgId)) {
                    // 納入通知書
                    svf.VrsOut("PRINT_TITLE", "上記の金額を納期限内に納入してください。");
                    svf.VrsOut("PRINTDAY", _param._shugakuDate.formatDate(_param._issueDate, false));
                    svf.VrsOut("PRINT_NAME1", "京都府歳入徴収者");
                    svf.VrsOut("PRINT_NAME2", "京都府知事");
                    // 印鑑
                    svf.VrsOut("STAMPK", getImageFile("SAINYU_CHOSHUSHA.bmp", "image/stamp"));
                }
                
                
                // 住所下カスタマーバーコード
                final String customerBarcode = CustomerBarcode.getCustomerBarcode(db2, _param._psMap, noufu._zipcd, StringUtils.defaultString(noufu._address1) + StringUtils.defaultString(noufu._address2));
                log.info(" noufu " + page + " = " + customerBarcode);
                svf.VrsOut("CUSTOMER_BARCODE", customerBarcode);

                if (isPrintCnsBarcode) {
                    if (CvsBarcode.isNotTargetKingaku(noufu._henkanGk)) {
                        svf.VrsOut("BARCODE_COMMENT1", "この納付書はコンビニでお取り扱いできません。");
                    } else {
                        // コンビニ収納用 左下バーコード
                        final int[] nentukihiInt = _param.getYearMonthDay(_param._barcodeToriatsukaiKigen);
                        final String noufuKigenYymmdd = _param.df02.format(nentukihiInt[0]) + _param.df02.format(nentukihiInt[1]) + _param.df02.format(nentukihiInt[2]);
//                        final String printKaisuPart = null == noufu._noufubangoBarcode && noufu._noufubangoBarcode.length() < 3 ? "0" : noufu._noufubangoBarcode.substring(1, 3);
//                        final String printKaisu1 = String.valueOf(Integer.parseInt(printKaisuPart) % 10); // 下一桁
                        final String printKaisu1 = "0"; // 固定0
                        //log.debug(" printKaisuPart = " + printKaisuPart + ", printKaisu1 = " + printKaisu1);
                        final String barcode = CvsBarcode.getValue(noufuKigenYymmdd, noufu._noufubangoBarcode, printKaisu1, noufu._henkanGk);
                        svf.VrsOut("BARCODE1", barcode);
                    }
                }
                
                svf.VrEndPage();
                
                _hasData = true;
            }
            
        } catch (Exception ex) {
            log.error("Exception:", ex);
        }
    }
    
    private static int getMS932Length(final String s) {
    	return KNJ_EditEdit.getMS932ByteLength(s);
    }
    
    private static List reverse(final List list) {
        List rtn = new ArrayList();
        for (final ListIterator li = list.listIterator(list.size()); li.hasPrevious();) {
            rtn.add(li.previous());
        }
        return rtn;
    }
    
    private static List take(final List list, final int count) {
        List rtn = new ArrayList();
        int c = 0;
        for (final Iterator it = list.iterator(); it.hasNext() && c < count; c++) {
            rtn.add(it.next());
        }
        return rtn;
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

    private List getList(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        final List list = new ArrayList();
        try {
            final String sql = sql();
            log.info(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                
                final ChoteiNoufu noufu = new ChoteiNoufu();
                
                noufu._zipcd = rs.getString("ZIPCD");
                noufu._address1 = rs.getString("ADDR1");
                noufu._address2 = rs.getString("ADDR2");
                noufu._name = rs.getString("NAME");
                
                noufu._choteiYm = rs.getString("CHOTEI_YM");
                if (PRGID_KNJTG117.equals(_param._prgId)) {
                    if ("1".equals(_param._knjgT117Radio)) {
                        noufu._henkanGk = _param._knjgT117HakkouGk;
                    } else {
                        // 20151207
                        noufu._henkanGk = rs.getString("HENKAN_GK");
                    }
                } else {
                    noufu._henkanGk = rs.getString("HENKAN_GK");
                }
                noufu._printKaisu = rs.getString("PRINT_KAISU");
                noufu._choteiKaisu = rs.getString("CHOTEI_KAISU");
                noufu._shugakuNo = rs.getString("SHUUGAKU_NO");
                noufu._choteiNend = rs.getString("CHOTEI_NEND");
                if (PRGID_KNJTG116.equals(_param._prgId) || PRGID_KNJTG117.equals(_param._prgId) || PRGID_KNJTE065.equals(_param._prgId) || PRGID_KNJTE066.equals(_param._prgId) || PRGID_KNJTE090.equals(_param._prgId)) {
                    noufu._noufuKigen = rs.getString("NOUFU_KIGEN");
                } else if (PRGID_KNJTE054.equals(_param._prgId)) {
                    noufu._noufuKigen = _param._noufuKigen;
                }
                noufu._noufubango = rs.getString("NOUFUBANGO");
                noufu._noufubangoBarcode = rs.getString("NOUFUBANGO_BARCODE");
                noufu._shiharaikinKbn = rs.getString("SHIHARAININ_KBN");
                if (PRGID_KNJTG116.equals(_param._prgId)) {
                    noufu._reprintDate = rs.getString("REPRINT_DATE"); // KNJTG116
                } else if (PRGID_KNJTG117.equals(_param._prgId) || PRGID_KNJTE090.equals(_param._prgId)) {
                    noufu._reprintDate = _param._loginDate;
                }

// 20151207
//                if (PRGID_KNJTG117.equals(_param._prgId) && "2".equals(_param._knjgT117Radio)) {
//                    // 指定枚数分印刷
//                    final String[] eachHakkouGk = _param.getEachHakkouGk();
//                    for (int i = 0; i < eachHakkouGk.length; i++) {
//                        final ChoteiNoufu c = (ChoteiNoufu) noufu.clone();
//                        c._henkanGk = eachHakkouGk[i];
//                        list.add(c);
//                    }
//
//                } else {
//                    list.add(noufu);
//                }
                list.add(noufu);
            }
            
        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        if (PRGID_KNJTE090.equals(_param._prgId)) {
            Collections.sort(list, new ChoteiNoufu.KNJTE090Comparator(_param));
        } else if (PRGID_KNJTG117.equals(_param._prgId)) { // 20151207
            // 指定枚数分印刷
            final int count = _param._knjgT117MihakkoGk.intValue() <= 0 ? 0 : "2".equals(_param._knjgT117Radio) ? _param.getEachHakkouGk().length : 1;
            final List set = reverse(take(reverse(list), count));
            list.clear();
            list.addAll(set);
        }
        return list;
    }

    public String sql() {
        final StringBuffer stb = new StringBuffer();
        
        stb.append(" WITH MAIN AS ( ");
        if (PRGID_KNJTG117.equals(_param._prgId)) {
            stb.append(" SELECT ");
            stb.append("     V1.CHOTEI_YM, ");
            stb.append("     CASE WHEN BUNKATU_KAISU = 0 THEN V1.HENKAN_GK ");
            stb.append("          ELSE V1.HAKKO_TOTAL_GK ");
            stb.append("     END AS HENKAN_GK, ");
            stb.append("     V1.PRINT_KAISU, "); // 印刷回数
            stb.append("     V1.CHOTEI_KAISU, ");
            stb.append("     V1.BUNKATU_KAISU, ");
            stb.append("     V1.SHUUGAKU_NO, ");
            stb.append("     V1.CHOTEI_NEND, ");
            stb.append("     V1.NOUFU_KIGEN, ");
            stb.append("     '1' || RIGHT(('00' || RTRIM(CHAR(V1.PRINT_KAISU    ))),2) || '00000' || RIGHT(('000' || RTRIM(CHAR(V1.CHOTEI_KAISU))),3) || V1.SHUUGAKU_NO AS NOUFUBANGO, ");
            stb.append("     '1' || RIGHT(('00' || RTRIM(CHAR(V1.PRINT_KAISU    ))),2) || '000'   || RIGHT(('000' || RTRIM(CHAR(V1.CHOTEI_KAISU))),3) || V1.SHUUGAKU_NO AS NOUFUBANGO_BARCODE, ");
            stb.append("     V2.KOJIN_NO, ");
            stb.append("     V2.SHIKIN_SHUBETSU, ");
            stb.append("     V2.SHIHARAININ_KBN ");
            stb.append(" FROM ");
            stb.append("     V_CHOTEI_NOUFU V1 ");
            stb.append("     LEFT JOIN V_SAIKEN_JOKYO V2 ON V1.SHUUGAKU_NO = V2.SHUUGAKU_NO  ");
            stb.append(" WHERE ");
            stb.append("     V1.SHUUGAKU_NO = '" + _param._shuugakuNo + "' AND ");
            stb.append("     V1.CHOTEI_KAISU = " + _param._knjgT117ChoteiKaisu + " ");
        } else if (PRGID_KNJTE065.equals(_param._prgId) || PRGID_KNJTE066.equals(_param._prgId) || PRGID_KNJTE090.equals(_param._prgId) || PRGID_KNJTG116.equals(_param._prgId)) {
            stb.append(" SELECT ");
            stb.append("     V1.CHOTEI_YM, ");
            if (PRGID_KNJTE090.equals(_param._prgId)) {
                stb.append("     V1.HENKAN_GK, ");
            } else {
                stb.append("     CASE WHEN BUNKATU_KAISU = 0 THEN V1.HENKAN_GK ");
                stb.append("          ELSE V1.HAKKO_TOTAL_GK ");
                stb.append("     END AS HENKAN_GK, ");
            }
            stb.append("     V1.PRINT_KAISU, ");
            stb.append("     V1.CHOTEI_KAISU, ");
            stb.append("     V1.BUNKATU_KAISU, ");
            stb.append("     V1.SHUUGAKU_NO, ");
            stb.append("     V1.CHOTEI_NEND, ");
            stb.append("     V1.NOUFU_KIGEN, ");
            if (PRGID_KNJTG116.equals(_param._prgId)) {
                stb.append("     '1' || RIGHT(('00' || RTRIM(CHAR(V1.PRINT_KAISU    ))),2) || '00000' || RIGHT(('000' || RTRIM(CHAR(V1.CHOTEI_KAISU))),3) || V1.SHUUGAKU_NO AS NOUFUBANGO, ");
                stb.append("     '1' || RIGHT(('00' || RTRIM(CHAR(V1.PRINT_KAISU    ))),2) || '000'   || RIGHT(('000' || RTRIM(CHAR(V1.CHOTEI_KAISU))),3) || V1.SHUUGAKU_NO AS NOUFUBANGO_BARCODE, ");
            } else {
                if ("1".equals(_param._updated)) {
                    // 更新後なので印刷回数を+1しない
                    stb.append("     '1' || RIGHT(('00' || RTRIM(CHAR(V1.PRINT_KAISU + 0))),2) || '00000' || RIGHT(('000' || RTRIM(CHAR(V1.CHOTEI_KAISU))),3) || V1.SHUUGAKU_NO AS NOUFUBANGO, ");
                    stb.append("     '1' || RIGHT(('00' || RTRIM(CHAR(V1.PRINT_KAISU + 0))),2) || '000'   || RIGHT(('000' || RTRIM(CHAR(V1.CHOTEI_KAISU))),3) || V1.SHUUGAKU_NO AS NOUFUBANGO_BARCODE, ");
                } else {
                    stb.append("     '1' || RIGHT(('00' || RTRIM(CHAR(V1.PRINT_KAISU + 1))),2) || '00000' || RIGHT(('000' || RTRIM(CHAR(V1.CHOTEI_KAISU))),3) || V1.SHUUGAKU_NO AS NOUFUBANGO, ");
                    stb.append("     '1' || RIGHT(('00' || RTRIM(CHAR(V1.PRINT_KAISU + 1))),2) || '000'   || RIGHT(('000' || RTRIM(CHAR(V1.CHOTEI_KAISU))),3) || V1.SHUUGAKU_NO AS NOUFUBANGO_BARCODE, ");
                }
            }
            stb.append("     V1.REPRINT_DATE, ");
            stb.append("     V2.KOJIN_NO, ");
            stb.append("     V2.SHIKIN_SHUBETSU, ");
            stb.append("     V2.SHIHARAININ_KBN ");
            stb.append(" FROM ");
            stb.append("     V_CHOTEI_NOUFU V1 ");
            stb.append("     LEFT JOIN V_SAIKEN_JOKYO V2 ON V1.SHUUGAKU_NO = V2.SHUUGAKU_NO  ");
            if (PRGID_KNJTE066.equals(_param._prgId)) {
                stb.append("     LEFT JOIN FURIKAE_TORIKOMI_DAT T1 ON V1.SHUUGAKU_NO = T1.SHUUGAKU_NO AND V1.NOUFU_KIGEN = T1.FURIKAE_DATE ");
            }
            if (PRGID_KNJTE090.equals(_param._prgId)) {
                stb.append("     left join (SELECT SHUGAKU_NO AS SHUUGAKU_NO, CHOTEI_KAISU, COUNT(*) AS SOKU_CNT FROM SHUNO_CONVENI_DAT WHERE D_DATA_CD = '01' GROUP BY SHUGAKU_NO, CHOTEI_KAISU) CVN on V1.SHUUGAKU_NO = CVN.SHUUGAKU_NO and V1.CHOTEI_KAISU = CVN.CHOTEI_KAISU ");
            }
            if (PRGID_KNJTE065.equals(_param._prgId)) {
                stb.append(" WHERE ");
                stb.append("     (V2.CHUUI_FLG IS NULL OR V2.CHUUI_FLG = '03') AND ");
                stb.append("     V1.CHOTEI_YM = '" + _param._choteiYm + "' ");
                stb.append("     AND V1.SHIHARAI_HOHO_CD = '1' ");
                stb.append("     AND V1.SHUNO_FLG = '0' ");
                stb.append("     AND V1.TORIKESI_FLG = '0' ");
            } else if (PRGID_KNJTE066.equals(_param._prgId)) {
                stb.append(" WHERE ");
                stb.append("     (V2.CHUUI_FLG IS NULL OR V2.CHUUI_FLG = '03') AND ");
                stb.append("     V1.CHOTEI_YM = '" + _param._choteiYm + "' ");
                stb.append("     AND V1.SHIHARAI_HOHO_CD = '1' ");
                stb.append("     AND V1.SHUNO_FLG = '0' ");
                stb.append("     AND V1.TORIKESI_FLG = '0' ");
                stb.append("     AND T1.FURIKAE_KEKKA <> '0' ");
            } else if (PRGID_KNJTE090.equals(_param._prgId)) {
                stb.append(" WHERE ");
                stb.append("     (V2.CHUUI_FLG IS NULL OR V2.CHUUI_FLG = '03') AND ");
                stb.append("     VALUE(CVN.SOKU_CNT, 0) < 1 AND ");
                stb.append("     V1.CHOTEI_YM BETWEEN '" + _param._sChoteiYm + "' AND '" + _param._eChoteiYm + "' ");
                stb.append("     AND V1.SHUNO_FLG = '0' ");
                stb.append("     AND V1.TORIKESI_FLG = '0' ");
                stb.append("     AND V1.BUNKATU_FLG = '0' ");
                stb.append("     AND V1.SHUUGAKU_NO IN " + SQLUtils.whereIn(true, _param._shuugakuNos) + " ");
            } else if (PRGID_KNJTG116.equals(_param._prgId)) {
                stb.append(" WHERE ");
                stb.append("     V1.SHUUGAKU_NO = '" + _param._shuugakuNo + "' ");
                stb.append("     AND (CHOTEI_KAISU, BUNKATU_KAISU) IN (");
                String union = "";
                for (int i = 0; i < _param._printChk.length; i++) {
                    if (_param._printChk[i].length() < 8) {
                        continue;
                    }
                    stb.append(union);
                    final String choteiKaisu = String.valueOf(Integer.parseInt(_param._printChk[i].substring(0, 4)));
                    final String bunkatuKaisu = String.valueOf(Integer.parseInt(_param._printChk[i].substring(4, 8)));
                    stb.append(" VALUES(").append(choteiKaisu).append(", ").append(bunkatuKaisu).append(") ");
                    union = " UNION ";
                }
                stb.append(" ) ");
            }
        } else if (PRGID_KNJTE054.equals(_param._prgId)) {
            stb.append(" SELECT ");
            stb.append("     V1.CHOTEI_YM, ");
            stb.append("     V1.HENKAN_GK, ");
            stb.append("     V1.PRINT_KAISU, ");
            stb.append("     V1.CHOTEI_KAISU, ");
            stb.append("     V1.BUNKATU_KAISU, ");
            stb.append("     V1.SHUUGAKU_NO, ");
            stb.append("     V1.CHOTEI_NEND, ");
            stb.append("     V1.NOUFU_KIGEN, ");
            stb.append("     '1' || '00' || '00000' || RIGHT(('000' || RTRIM(CHAR(V1.CHOTEI_KAISU))),3) || V1.SHUUGAKU_NO AS NOUFUBANGO, ");
            stb.append("     '1' || '00' ||   '000' || RIGHT(('000' || RTRIM(CHAR(V1.CHOTEI_KAISU))),3) || V1.SHUUGAKU_NO AS NOUFUBANGO_BARCODE, ");
            stb.append("     V2.KOJIN_NO, ");
            stb.append("     V2.SHIKIN_SHUBETSU, ");
            stb.append("     V2.SHIHARAININ_KBN ");
            stb.append(" FROM ");
            stb.append("     V_CHOTEI_NOUFU V1 ");
            stb.append("     LEFT JOIN V_SAIKEN_JOKYO V2 ON V1.SHUUGAKU_NO = V2.SHUUGAKU_NO  ");
            stb.append(" WHERE ");
            if ("2".equals(_param._output)) {
                stb.append("     V1.SHUUGAKU_NO = '" + _param._shuugakuNo + "' AND ");
            }
            stb.append("     (V2.CHUUI_FLG IS NULL OR V2.CHUUI_FLG = '03') AND ");
            stb.append("     V1.SHUNO_FLG = '0' AND ");
            stb.append("     V1.TORIKESI_FLG = '0' AND ");
            stb.append("     V1.SHIHARAI_HOHO_CD = '2' AND ");
            stb.append("     V1.HAITO_FLG = '0' AND ");
            stb.append("     V1.BUNKATU_FLG = '0' AND ");
            stb.append("     V1.CHOTEI_YM = '" + _param._choteiYm + "' ");
        }
        stb.append(" ) ");
        
        stb.append(" SELECT ");
        stb.append("     T1.*, ");
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
        stb.append("          WHEN S2.SOUFU_NAME IS NOT NULL THEN S2.SOUFU_NAME ");
        stb.append("          ELSE CONCAT(CONCAT(V3.FAMILY_NAME,'　'),V3.FIRST_NAME) ");
        stb.append("     END AS NAME ");
        stb.append(" FROM ");
        stb.append("     MAIN T1 ");
        stb.append("     LEFT JOIN V_KOJIN_HIST_DAT V3 ON T1.KOJIN_NO = V3.KOJIN_NO ");
        stb.append("     LEFT JOIN SOUFU_ADDRESS_DAT S1 ON T1.KOJIN_NO = S1.KOJIN_NO AND T1.SHUUGAKU_NO = S1.SHUUGAKU_NO ");
        stb.append("     LEFT JOIN SOUFU_ADDRESS_DAT S2 ON T1.KOJIN_NO = S2.KOJIN_NO AND S2.SHUUGAKU_NO = '9999999' ");
        stb.append(" ORDER BY T1.SHUUGAKU_NO, CHOTEI_KAISU, BUNKATU_KAISU ");
        return stb.toString();
    }
    
    private static class ChoteiNoufu {

        /**
         * KNJTE090用ソート。（指定修学生番号、調定年月昇順の順）
         */
        static class KNJTE090Comparator implements Comparator {
            private static final Integer MAX = new Integer(Integer.MAX_VALUE);
            final Map _shuugakuNoOrderMap;
            public KNJTE090Comparator(final Param param) {
                _shuugakuNoOrderMap = new HashMap();
                int no = 0;
                for (int i = 0; i < param._shuugakuNos.length; i++) {
                    final String shuugakuNo = param._shuugakuNos[i];
                    if (null == _shuugakuNoOrderMap.get(shuugakuNo)) {
                        _shuugakuNoOrderMap.put(shuugakuNo, new Integer(no));
                        no += 1;
                    }
                }
            }
            private Integer idx(final String shuugakuNo) {
                if (null == _shuugakuNoOrderMap.get(shuugakuNo)) {
                    return MAX;
                }
                return (Integer) _shuugakuNoOrderMap.get(shuugakuNo);
            }
            public int compare(final Object o1, final Object o2) {
                final ChoteiNoufu c1 = (ChoteiNoufu) o1;
                final ChoteiNoufu c2 = (ChoteiNoufu) o2;
                int cmp;
                cmp = idx(c1._shugakuNo).compareTo(idx(c2._shugakuNo));
                if (0 != cmp) {
                    return cmp;
                }
                cmp = c1._choteiYm.compareTo(c2._choteiYm);
                return cmp;
            }
        }
        
        static final DecimalFormat df11 = new DecimalFormat("00000000000");

        static final String kakuninBangou = "0000";   // 確認番号
        static final String noufuKubun = "334";       // 納付区分
        
        static final String kouzaBangou = "00000000000";       // 口座番号
        static final String haraikomiRyoukinFutanKubun = "2";  // 払込料金負担区分 2:統合財務システム
        static final String kikanId = "26000";                 // 機関ID
        static final String inshiZeiKubun = "0";               // 印紙税区分
        static final String shushi = "1";                      // 収支 1:歳入
        static final String kaikei = "01";                     // 会計 01:一般会計

        static final String yosanKubun = "1";                  // 予算区分 1:現年
        static final String kamokuId = "002508";               // 科目ID
        static final String shunouKubun1 = "8";                // 収納区分１ 8:納付書（標準帳票）
        static final String shunouKubun2 = "1";                // 収納区分２ 1:通常
        static final String jiyuSiyouRyouiki = "00000000000";  // 自由使用領域 (11桁)
        
        String _zipcd;
        String _address1;
        String _address2;
        String _name;
        String _henkanGk;
        String _printKaisu;
        String _choteiKaisu;
        String _choteiYm;
        String _shugakuNo;
        String _choteiNend;
        String _noufuKigen;
        String _noufubango;        // 納付番号18桁             ((1が1桁) || (0埋め印刷回数2桁) || (0が5桁) || (0埋め調停回数3桁) || (修学生番号7桁))
        String _noufubangoBarcode; // 納付番号バーコード用16桁 ((1が1桁) || (0埋め印刷回数2桁) || (0が3桁) || (0埋め調停回数3桁) || (修学生番号7桁))
        String _shiharaikinKbn;
        String _reprintDate;
        
        public Object clone() {
            ChoteiNoufu ano = new ChoteiNoufu();
            ano._zipcd = _zipcd;
            ano._address1 = _address1;
            ano._address2 = _address2;
            ano._name = _name;
            ano._henkanGk = _henkanGk;
            ano._printKaisu = _printKaisu;
            ano._choteiKaisu = _choteiKaisu;
            ano._choteiYm = _choteiYm;
            ano._shugakuNo = _shugakuNo;
            ano._choteiNend = _choteiNend;
            ano._noufuKigen = _noufuKigen;
            ano._noufubango = _noufubango;
            ano._noufubangoBarcode = _noufubangoBarcode;
            ano._shiharaikinKbn = _shiharaikinKbn;
            ano._reprintDate = _reprintDate;
            return ano;
        }
        
        // 上段OCR
        String createUpper(final Param param) {
            String choteiNendS = "";
            if (null == _choteiNend) {
            	choteiNendS = "00";
            } else {
            	final String nen = param._shugakuDate.nengoNenTukiHi(_choteiNend + "-04-01", false)[1];
            	if ("元".equals(nen)) {
            		choteiNendS = "01";
            	} else if (NumberUtils.isDigits(nen)) {
            		choteiNendS = param.df02.format(Integer.valueOf(nen));
            	}
            }
            
            // 口座番号11桁 + 返還額11桁 + 払込料金負担区分1桁 + 機関ID5桁 + 印紙税区分1桁
            // + 納付区分3桁 + 調定年度2桁 + 収支1桁 + 会計2桁 = 37桁
            
            final String s = kouzaBangou + df11.format(Long.parseLong(_henkanGk)) + haraikomiRyoukinFutanKubun + kikanId + inshiZeiKubun
                + noufuKubun + choteiNendS + shushi + kaikei;
            
            // チェックディジット2桁 + 37桁 = 39桁 
            return CheckDigit.calcDigitUpper(s) + s;
        }

        // 下段OCR
        String createLower() {
            // 予算区分1桁 + 科目ID6桁 + 収納区分1桁 + 収納区分２1桁 + 納付番号18桁 + 確認番号4桁 + 自由使用領域11桁 = 42桁

            final String s = yosanKubun + kamokuId + shunouKubun1 + shunouKubun2 + _noufubango + kakuninBangou + jiyuSiyouRyouiki;
            // チェックディジット2桁 + 42桁 = 44桁 
            return CheckDigit.calcDigitLower(s) + s;
        }
    }
    
    private static class CheckDigit {
        private static int n = 0; // 無効
        //                                 idx : 0, 1, 2, 3, 4, 5, 6, 7, 8, 9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44
        private static int[] w1 = new int[] {n, n, n, 9, n, 8, n, 7, n, 6, n, 5, n, 4, n, 3, n, 2, n, 9, n, 8, n, 7, n, 6, n, 5, n, 4, n, 3, n, 2, n, 9, n, 8, n, 7, n, 6, n, 5, n};
        private static int[] w2 = new int[] {n, n, 2, n, 3, n, 4, n, 5, n, 6, n, 7, n, 8, n, 9, n, 2, n, 3, n, 4, n, 5, n, 6, n, 7, n, 8, n, 9, n, 2, n, 3, n, 4, n, 5, n, 6, n, 7};
        
//        public static void calctest() {
//            
//            check("61", calcDigitUpper(     "0000000000000000003600226000033421101"));
//            check("22", calcDigitLower("100250881102000000061050501000000000000000"));
//        }
//        
//        private static void check(final String s1, final String s2) {
//            if (s1.equals(s2)) {
//                log.debug(" チェックディジットが一致しました :" + s1 + "," + s2);
//                return;
//            }
//            log.error(" チェックディジットが一致しません :" + s1 + "," + s2);
//        }
        
        public static String calcDigitUpper(String s) {
            final int max = 39;
            if (s.length() < max - 2) {
                log.warn(" 桁不足のため0追加 :" + (max - 2 - s.length()));
                s = s + StringUtils.repeat("0", max - 2 - s.length());
            }
            final List intList = toIntList(s);
            final Integer cd2 = cd(10, w1, 3, 39, 4, 38, intList);
            intList.add(0, cd2);
            final Integer cd1 = cd(11, w2, 2, 38, 3, 39, intList);
            return cd1.toString() + cd2.toString();
        }
        
        public static String calcDigitLower(String s) {
            final int max = 44;
            if (s.length() < max - 2) {
                log.warn(" 桁不足のため0追加 :" + (max - 2 - s.length()));
                s = s + StringUtils.repeat("0", max - 2 - s.length());
            }
            final List intList = toIntList(s);
            final Integer cd2 = cd(10, w1, 3, 43, 4, 44, intList);
            intList.add(0, cd2);
            final Integer cd1 = cd(11, w2, 2, 44, 3, 43, intList);
            return cd1.toString() + cd2.toString();
        }
        
        /**
         * 091109標準帳票ガイドライン別紙.pdf p.35にしたがって変換
         * （「英字又は記号の置き換え」について、実際は英字・記号は使用しないため処理を保留）
         */
        private static List toIntList(final String s) {
            final List list = new LinkedList();
            for (int i = 0; i < s.length(); i++) {
                final int ch = s.charAt(i);
                int val = -1;
                if ('0' <= ch && ch <= '9') {
                    val = ch - '0';
//                } else if ('A' <= ch && ch <= 'Z') { // 以下の「英字又は記号の置き換え」の処理は保留
//                    val = ch - 'A' + 10;
//                } else if ('*' == ch) { // ? 記号が不明
//                    val = 36;
//                } else if ('+' == ch) {
//                    val = 37;
//                } else if ('-' == ch) {
//                    val = 38;
//                } else if ('#' == ch) {
//                    val = 38;
                }
                if (-1 == val) {
                    log.warn("invalid character: " + s.charAt(i));
                } else {
                    list.add(new Integer(val));
                }
            }
//            for (int i = 0; i < list.size(); i++) {
//                log.debug(" i = " + (3 + i) + ", value = " + list.get(i));
//            }
            return list;
        }
        
        private static Integer cd(final int d, final int[] w, final int st1, final int ed1, final int st2,
                final int ed2, final List intList) {
            int sum = 0;
            for (int i = st1; i <= ed1; i += 2) {
                final Integer num = (Integer) intList.get(i - st1);
                sum += num.intValue() * w[i];
//                log.debug(" i = " + i + ":  num = " + num + ", w[" + i + "] = " + w[i] + ",  *= " + (num.intValue() * w[i]));
            }
            for (int j = st2; j <= ed2; j += 2) {
                final Integer num = (Integer) intList.get(j - st1);
                sum += num.intValue();
//                log.debug(" j = " + j + ":  num = " + num);
            }
            final int mod = sum % d;
            final int rtn = (10 != d && 10 == mod) ? 0 : mod;
//            log.debug(" sum = " + sum + ", cd = " + rtn);
            return new Integer(rtn);
        }

        
    }
    
    private static class CvsBarcode {
        static final String CnsCd = "29182"; // CNSコード 地方税59919、各種料金29182
        static final String riyouKigyouCd = "51714"; // 利用企業コード 5桁 ※古宿さん情報
        static final String saiHakkoKbn = "0"; // 再発行区分（再発行回数）1桁 (0:初回 1:１回目の再発行 2:２回目の再発行)
        static final String inshiFlg = "0"; // 印紙フラグ 1桁 (公金の場合、収入印紙は不要。よって支払い金額に関わらず、印紙フラグは『0』を設定)

        static final DecimalFormat df6 = new DecimalFormat("000000");
        
//        static {
//            final String s1 = "91" + "912345" + "0123456789012345678901" + "010331" + "0" + "123000";
//            log.debug("check1 = " + checkDigit(s1)); // should be "3"
//            final String s2 = "91" + "942386" + "1789539178539356927301" + "030731" + "1" + "031290";
//            log.debug("check2 = " + checkDigit(s2)); // should be "3"
//        }
        
        // バーコードの値
        private static String getValue(final String shiharaiKigenYymmdd, final String noufuBangoBarcode, final String printKaisu1, final String payment) {
            //final String s = "91" + ("9" + CnsCd) + riyouKigyouCd + noufuBangoBarcode + saiHakkoKbn + shiharaiKigenYymmdd + inshiFlg + df6.format(Integer.parseInt(payment));
            final String s = "91" + ("9" + CnsCd) + riyouKigyouCd + noufuBangoBarcode + printKaisu1 + shiharaiKigenYymmdd + inshiFlg + df6.format(Integer.parseInt(payment));
            // 43桁 + チェックディジット1桁 = 44桁
            return s + checkDigit(s);
        }

        // チェックディジットを得る
        private static String checkDigit(final String value) {
            if (value.length() != 43) {
                return " ";
            }
            // チェックディジットを含めた右からの桁が
            //   偶数の場合のその桁の値の合計をAとする。
            //   奇数の場合のその桁の値の合計をBとする。
            // Aの3倍とBの和をCとする。
            // 10からCの下1桁をひいたものをチェックディジットとする。（Cの下1桁が0の場合はチェックディジットは0とする。）
            int a = 0, b = 0;
            for (int keta = 2; keta <= 44; keta ++) {
                final int num = value.charAt(44 - keta) - '0';
                if (keta % 2 == 0) { // 偶数桁
                    a += num;
//                    log.debug(" keta => " + keta + "(偶数), num = " + num + ", a = " + a);
                } else { // 奇数桁
                    b += num;
//                    log.debug(" keta => " + keta + "(奇数), num = " + num + ", b = " + b);
                }
            }
            final int c = 3 * a + b;
            final int checkdigit = c % 10 == 0 ? 0 : (10 - (c % 10));
//            log.debug(" a = " + a + ", b = " + b + ", c = " + c + ", checkdigit = " + checkdigit);
            return String.valueOf(checkdigit);
        }
        
        private static boolean isNotTargetKingaku(final String gaku) {
            if (NumberUtils.isDigits(gaku) && Long.parseLong(gaku) > 300000) { // 30万円を超える場合、バーコード対象外
                return true;
            }
            return false;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
    	log.fatal("$Revision: 73865 $");
    	KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final DecimalFormat df02 = new DecimalFormat("00");

        private String _choteiYm; // KNJTE054・KNJTE065
        private String _sChoteiYm, _eChoteiYm; // KNJTE090
        private String[] _shuugakuNos; // KNJTE090
        private String _output; // KNJTE054
        private String _shuugakuNo; // KNJTE054・KNJTEG116・KNJTG117
        private String _noufuKigen; // KNJTE054
        private String _issueDate; // KNJTE054・KNJTE065
        private String[] _printChk; // KNJTG116
        private String _loginDate; // KNJTE065・KNJTG117・KNJTE090
        private String _barcodeToriatsukaiKigen;
        private final String _prgId;
        final String _documentRoot;
        private String _printDiv;
        private String _updated; // KNJTE065・KNJTE066
        final ShugakuDate _shugakuDate;

        private String _knjgT117Radio; // KNJTG117 1:「発行金額」、2:「発行枚数」
        private String _knjgT117Sai1Kojinno; // KNJTG117
        private String _knjgT117ChoteiKaisu; // KNJTG117
        private String _knjgT117HakkouGk; // KNJTG117
        private Integer _knjgT117HakkouMaisu = new Integer(-1); // KNJTG117
        private Integer _knjgT117MihakkoGk = new Integer(-1); // KNJTG117
        
        private Map _psMap = new HashMap();

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _documentRoot = request.getParameter("DOCUMENTROOT");
            _prgId = request.getParameter("PRGID");
            _shugakuDate = new ShugakuDate(db2);
            _shugakuDate._printBlank = true;
            _barcodeToriatsukaiKigen = _shugakuDate.d7toDateStr(request.getParameter("CON_DATE"));
            if (PRGID_KNJTG117.equals(_prgId)) {
                _shuugakuNo = request.getParameter("SHUUBETU_SHUUGAKUNO");
                _knjgT117Sai1Kojinno = request.getParameter("SAI1_KOJINNO");
                _knjgT117ChoteiKaisu = request.getParameter("CHOTEI_KAISU");
                _knjgT117Radio = request.getParameter("PRINT_RADIO");
                _knjgT117MihakkoGk = Integer.valueOf(request.getParameter("PRINT_MIH_GK"));
                if ("1".equals(_knjgT117Radio)) {
                    _knjgT117HakkouGk = request.getParameter("PRINT_HAK_GK");
                } else if ("2".equals(_knjgT117Radio)) {
                    _knjgT117HakkouMaisu = Integer.valueOf(request.getParameter("PRINT_HAK_MAI"));
                }
                _loginDate = request.getParameter("LOGIN_DATE");
                _printDiv = null != _barcodeToriatsukaiKigen ? "2" : "1";
            } else if (PRGID_KNJTE065.equals(_prgId) || PRGID_KNJTE066.equals(_prgId)) {
                _choteiYm = _shugakuDate.d5toYmStr(request.getParameter("CHOTEI_YM"));
                _issueDate = _shugakuDate.d7toDateStr(request.getParameter("ISSUE_DATE"));
                _loginDate = request.getParameter("LOGIN_DATE");
                _printDiv = request.getParameter("PRINT_DIV");
                _updated = request.getParameter("updated");
            } else if (PRGID_KNJTG116.equals(_prgId)) {
                _shuugakuNo = request.getParameter("SHUUBETU_SHUUGAKUNO");
                _printChk = null == request.getParameter("PRINT_CHK") ? new String[]{} : StringUtils.split(request.getParameter("PRINT_CHK"), ",");
                _printDiv = null != _barcodeToriatsukaiKigen ? "2" : "1";
            } else if (PRGID_KNJTE090.equals(_prgId)) {
                _sChoteiYm = _shugakuDate.d5toYmStr(request.getParameter("S_CHOTEI_YM"));
                _eChoteiYm = _shugakuDate.d5toYmStr(request.getParameter("E_CHOTEI_YM"));
                _shuugakuNos = StringUtils.split(request.getParameter("SHUUGAKU_NO_LIST"), ",");
                _loginDate = request.getParameter("LOGIN_DATE");
                _printDiv = request.getParameter("PRINT_DIV");
            } else if (PRGID_KNJTE054.equals(_prgId)) {
                _choteiYm = request.getParameter("CHOTEI_YM");
                _shuugakuNo = request.getParameter("SHUUGAKU_NO");
                _issueDate = _shugakuDate.d7toDateStr(request.getParameter("ISSUE_DATE"));
                _noufuKigen = _shugakuDate.d7toDateStr(request.getParameter("NOUFU_KIGEN"));
                _output = request.getParameter("OUTPUT");
                _printDiv = request.getParameter("PRINT_DIV");
            }
        }
        
        public String[] getEachHakkouGk() {
            if (PRGID_KNJTG117.equals(_prgId) && "2".equals(_knjgT117Radio)) {
                final String[] rtn = new String[_knjgT117HakkouMaisu.intValue()];
                final int divval = _knjgT117MihakkoGk.intValue() / _knjgT117HakkouMaisu.intValue();
                final String div = String.valueOf(divval);
                for (int i = 0; i < _knjgT117HakkouMaisu.intValue() - 1; i++) {
                    rtn[i] = div;
                }
                if (0 == _knjgT117MihakkoGk.intValue() % _knjgT117HakkouMaisu.intValue()) { // 割り切れる場合
                    rtn[_knjgT117HakkouMaisu.intValue() - 1] = div;
                } else {
                    rtn[_knjgT117HakkouMaisu.intValue() - 1] = String.valueOf(_knjgT117MihakkoGk.intValue() - divval * (_knjgT117HakkouMaisu.intValue() - 1));
                }
                return rtn;
            }
            return new String[] {};
        }
        
        public int[] getYearMonthDay(final String date) {
            if (null != date) {
                try {
                    final Date d = java.sql.Date.valueOf(date);
                    final Calendar cal = Calendar.getInstance();
                    cal.setTime(d);
                    final int year = cal.get(Calendar.YEAR) % 100;
                    final int month = cal.get(Calendar.MONTH) + 1;
                    final int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
                    
                    return new int[] {year, month, dayOfMonth};
                } catch (Exception e) {
                    log.error("format exception! date = " + date, e);
                }
            }
            return null;
        }
    }
}

// eof

