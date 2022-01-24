/*
 * $Id: e54133ccffb0ef6f138001abc9385f1619efc449 $
 *
 * 作成日: 2012/09/21
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJP;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.ShugakuDate;

/**
 * 京都府修学金 高等学校修学金貸与台帳 / 修学支度金貸与台帳
 */
public class KNJTP051 {

    private static final Log log = LogFactory.getLog(KNJTP051.class);

    private boolean _hasData;

    private static String PRGID_KNJTP052 = "KNJTP052";
    private static String PRGID_KNJTE101 = "KNJTE101";

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

    /**
     * @param source 元文字列
     * @param bytePerLine 1行あたりのバイト数
     * @return bytePerLineのバイト数ごとの文字列リスト
     */
    private static List getTokenList(final String source, final int bytePerLine) {

        if (source == null || source.length() == 0) {
            return Collections.EMPTY_LIST;
        }

        // String stoken[] = new String[f_cnt];
        final List tokenList = new ArrayList();        //分割後の文字列の配列
        // int lines = 0;                              // == stoken.size
        int startIndex = 0;                         //文字列の分割開始位置
        int byteLengthInLine = 0;                   //文字列の分割開始位置からのバイト数カウント
        // for (int s_cur = 0; s_cur < strx.length() && ib < f_cnt; s_cur++) {
        for (int idx = 0; idx < source.length(); idx += 1) {
            //改行マークチェック    04/09/28Modify
            if (source.charAt(idx) == '\r') {
                continue;
            }
            if (source.charAt(idx) == '\n') {
                // stoken[ib] = strx.substring(s_sta, s_cur);
                tokenList.add(source.substring(startIndex, idx));
                // lines += 1;
                byteLengthInLine = 0;
                startIndex = idx + 1;
            } else {
                final int sbytelen = getMS932Length(source.substring(idx, idx + 1));
                byteLengthInLine += sbytelen;
                if (byteLengthInLine > bytePerLine) {
                    // stoken[ib] = strx.substring(s_sta, s_cur);
                    tokenList.add(source.substring(startIndex, idx));
                    // lines += 1;
                    byteLengthInLine = sbytelen;
                    startIndex = idx;
                }
            }
        }
        if (byteLengthInLine > 0) {
            // stoken[lines] = strx.substring(s_sta);
            tokenList.add(source.substring(startIndex));
        }

        return tokenList;
    } //String get_token()の括り

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        final List kojinList = new ArrayList();
        if (PRGID_KNJTP052.equals(_param._prgid) || PRGID_KNJTE101.equals(_param._prgid)) {
            final Map shuugakuNoKojinNoMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT KOJIN_NO, SHUUGAKU_NO FROM V_KOJIN_SHUUGAKU_SHINSEI_HIST_DAT WHERE SHUUGAKU_NO IN " + SQLUtils.whereIn(true, _param._shuugakuNoList) + " ";
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    shuugakuNoKojinNoMap.put(rs.getString("SHUUGAKU_NO"), rs.getString("KOJIN_NO"));
                }
            } catch (final SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            for (int i = 0; i < _param._shuugakuNoList.length; i++) {
                if (null == shuugakuNoKojinNoMap.get(_param._shuugakuNoList[i])) {
                    continue;
                }
                kojinList.add(new Kojin((String) shuugakuNoKojinNoMap.get(_param._shuugakuNoList[i]), _param._shuugakuNoList[i]));
            }
        } else {
            kojinList.add(new Kojin(_param._kojinNo, null));
        }

        for (final Iterator itk = kojinList.iterator(); itk.hasNext();) {
            final Kojin kojin = (Kojin) itk.next();
            log.fatal(" kojin = " + kojin);
            kojin.load(db2, _param);

            final List printlist = kojin.groupShinseiHistByShuugakuNo();

            for (final Iterator it = printlist.iterator(); it.hasNext();) {
                final Map m = (Map) it.next();
                final KojinShinseiHistDat maxShinsei = (KojinShinseiHistDat) m.get(Kojin.KEY_KOJIN_SHINSEI_HIST_DAT_MAX);
                final List shuugakuNoShinseiList = (List) m.get(Kojin.KEY_KOJIN_SHINSEI_HIST_DAT_LIST);
                printMain12(svf, maxShinsei, shuugakuNoShinseiList);

                printMain3(svf, maxShinsei);
            }
        }
    }

    private String getShinkenCdForPrint(final String shinkenCd) {
        if (null == shinkenCd || shinkenCd.length() < 9) {
            return shinkenCd;
        }
        return shinkenCd.substring(0, 7) + "-" + shinkenCd.substring(7);
    }

    private void VrsOutn(final String[] field1, final int j, final String[] data, final Vrw32alp svf) {
        if (null == data) {
            return;
        }
        for (int i = 0; i < Math.min(field1.length, data.length); i++) {
            svf.VrsOutn(field1[i], j, data[i]);
        }
    }

    private void printAddress(final Vrw32alp svf, final int i, final String addr1, final String addr2) {

        int lines = 0;
        final String[] token2 = KNJ_EditEdit.get_token(addr2, 40, 2);
        if (null != token2) { for (int j = 0; j < token2.length; j++) { if (!StringUtils.isBlank(token2[j])) lines += 1; } }
        if (lines <= 2) {
            svf.VrsOutn("ADDRESS1", i, addr1); // 住所
            VrsOutn(new String[]{"ADDRESS2", "ADDRESS3"}, i, token2, svf);
        } else {
            final String[] addr1a = KNJ_EditEdit.get_token(addr1, 50, 2);
            final String[] addr2a = KNJ_EditEdit.get_token(addr2, 50, 2);
            final List addr = new ArrayList();
            if (null != addr1a && !StringUtils.isBlank(addr1a[0])) addr.add(addr1a[0]);
            if (null != addr1a && !StringUtils.isBlank(addr1a[1])) addr.add(addr1a[1]);
            if (null != addr2a && !StringUtils.isBlank(addr2a[0])) addr.add(addr2a[0]);
            if (null != addr2a && !StringUtils.isBlank(addr2a[1])) addr.add(addr2a[1]);
            final String[] fieldsNo = new String[] {"1_2", "1_3", "2_2", "2_3"};
            for (int j = 0; j < addr.size(); j++) {
                svf.VrsOutn("ADDRESSS" + fieldsNo[j], i, (String) addr.get(j));
            }
        }
    }

    private void printMain12(final Vrw32alp svf, final KojinShinseiHistDat maxShinsei, final List shinseiList) {

        final String form;
        final String title;
        if ("1".equals(maxShinsei._t030namespare3)) {
            form = "KNJTP051_1.frm";
            title = "高等学校等修学金貸与台帳";
        } else if ("2".equals(maxShinsei._t030namespare3)) {
            form = "KNJTP051_2.frm";
            title = "高等学校等修学支度金貸与台帳";
        } else {
            return;
        }

        svf.VrSetForm(form, 1);
        svf.VrsOut("TITLE", title);

        svf.VrsOut("NO", maxShinsei._shuugakuNo); // 修学生予定番号
        svf.VrsOut("DATE", _param._shugakuDate.formatDate(_param._loginDate)); // 日付
        svf.VrsOut("FUNO_ARI", maxShinsei._funoAri); // 日付

        // 上から2行表示
        int commentLine = 0;
        final List list1 = maxShinsei._jikoFlgCommentList;
        if (list1.size() > 0) {
            commentLine += 1;
            final String text1 = String.valueOf((String) list1.get(0));
            final String text2 = list1.size() == 1 ? "" : "、他" + String.valueOf(list1.size() - 1)+ "件";
            svf.VrsOut("FLG_COMMENT" + String.valueOf(commentLine), text1 + text2); // コメント
        }
        final List list2 = maxShinsei._happuDomeFlgCommentList;
        if (list2.size() > 0) {
            commentLine += 1;
            final String text1 = String.valueOf((String) list2.get(0));
            final String text2 = list2.size() == 1 ? "" : "、他" + String.valueOf(list2.size() - 1)+ "件";
            svf.VrsOut("FLG_COMMENT" + String.valueOf(commentLine), text1 + text2); // コメント
        }

        int i;
        i = 1;
        svf.VrsOutn("KANA", i, maxShinsei.getKana()); // フリガナ
        svf.VrsOutn("NAME", i, maxShinsei.getName()); // 氏名
        svf.VrsOutn("BIRTHDAY", i, _param._shugakuDate.formatDate(maxShinsei._kojinBirthday)); // 生年月日
        svf.VrsOutn("P_NO", i, maxShinsei._kojinNo); // 個人番号
        svf.VrsOutn("CITY_NO", i, maxShinsei._kojinCitycd); // 市町村コード
        svf.VrsOutn("ZIP_NO", i, maxShinsei._kojinZipcd); // 郵便番号
        svf.VrsOutn("TEL_NO", i, maxShinsei._kojinTelno1); // 電話番号
        printAddress(svf, i, maxShinsei._kojinAddr1, maxShinsei._kojinAddr2);
        final List remark0 = getTokenList(maxShinsei._kojinRemark, 34);
        for (int j = 0; j < remark0.size(); j++) {
            svf.VrsOutn("REMARK" + (j + 1), i, (String) remark0.get(j)); // 備考
        }

        if (null != maxShinsei._shinken1) {
            i = 3;
            ShinkenshaHistDat shinken1 = maxShinsei._shinken1;
            svf.VrsOutn("KANA", i, shinken1.getKana()); // フリガナ
            svf.VrsOutn("NAME", i, shinken1.getName()); // 氏名
            svf.VrsOutn("BIRTHDAY", i, _param._shugakuDate.formatDate(shinken1._birthday)); // 生年月日
            svf.VrsOutn("P_NO", i, getShinkenCdForPrint(maxShinsei._shinken1Cd)); // 個人番号
            svf.VrsOutn("CITY_NO", i, shinken1._citycd); // 市町村コード
            svf.VrsOutn("ZIP_NO", i, shinken1._zipcd); // 郵便番号
            svf.VrsOutn("TEL_NO", i, shinken1._telno1); // 電話番号
            printAddress(svf, i, shinken1._addr1, shinken1._addr2);
            final List remark = getTokenList(shinken1._remark, 34);
            for (int j = 0; j < remark.size(); j++) {
                svf.VrsOutn("REMARK" + (j + 1), i, (String) remark.get(j)); // 備考
            }
        }
        if (null != maxShinsei._rentai) {
            i = 2;
            ShinkenshaHistDat shinken2 = maxShinsei._rentai;
            svf.VrsOutn("KANA", i, shinken2.getKana()); // フリガナ
            svf.VrsOutn("NAME", i, shinken2.getName()); // 氏名
            svf.VrsOutn("BIRTHDAY", i, _param._shugakuDate.formatDate(shinken2._birthday)); // 生年月日
            svf.VrsOutn("P_NO", i, getShinkenCdForPrint(maxShinsei._rentaiCd)); // 個人番号
            svf.VrsOutn("CITY_NO", i, shinken2._citycd); // 市町村コード
            svf.VrsOutn("ZIP_NO", i, shinken2._zipcd); // 郵便番号
            svf.VrsOutn("TEL_NO", i, shinken2._telno1); // 電話番号
            printAddress(svf, i, shinken2._addr1, shinken2._addr2);
            final List remark = getTokenList(shinken2._remark, 34);
            for (int j = 0; j < remark.size(); j++) {
                svf.VrsOutn("REMARK" + (j + 1), i, (String) remark.get(j)); // 備考
            }
        }
        svf.VrsOut("SCHOOL_NAME1", maxShinsei._schoolName); // 学校名
        svf.VrsOut("COURSE1", maxShinsei._kateiName); // 課程
        svf.VrsOut("RES_APPLI_DAY1", _param._shugakuDate.formatDate(maxShinsei._taiyoyoyakuYoyakuShinseiDate)); // 予約申請日
        svf.VrsOut("RES_DET_DAY1", _param._shugakuDate.formatDate(maxShinsei._taiyoyoyakuKetteiDate)); // 予約決定日

        svf.VrsOut("GRAD_SC_MONTH", _param._shugakuDate.nengoNenTukiFromDate(maxShinsei._taiyoSotsugyouyoteiDate)); // 卒業予定月
        svf.VrsOut("LOAN_HOPE_MONEY", maxShinsei._yoyakuKibouGk); // 貸与希望額
        svf.VrsOut("LOAN_HOPE_PERIOD", _param._shugakuDate.nengoNenTukiFromDate(maxShinsei._taiyoSTaiyokibouDate) + "〜" + _param._shugakuDate.nengoNenTukiFromDate(maxShinsei._taiyoETaiyokibouDate)); // 貸与希望期間
        svf.VrsOut("PAY_REA_PERIOD", maxShinsei._taiyoShishutsuTotalMonths); // 支出実期間
        svf.VrsOut("PAY_REAL_MONEY", maxShinsei._taiyoShishutsuTotalGk); // 支出実総額
        svf.VrsOut("PAY_EXE_MONEY", maxShinsei._taiyoShishutsuShoriTotalGk); // 支出処理総額

        if ("1".equals(maxShinsei._t030namespare3)) {
            for (int j = 0; j < Math.min(5, shinseiList.size()); j++) {
                final KojinShinseiHistDat shinseii = (KojinShinseiHistDat) shinseiList.get(j);

                svf.VrsOutn("GRADE", j + 1, shinseii._grade); // 学年
                svf.VrsOutn("LOAN_APPLI_SUB_DAY1", j + 1, _param._shugakuDate.formatDateMarkDot(shinseii._shinseiDate)); // 貸与申請日
                svf.VrsOutn("LOAN_DET_SUB_DAY1", j + 1, _param._shugakuDate.formatDateMarkDot(shinseii._ketteiDate)); // 貸与決定日

                for (int k = 0; k < Math.min(3, shinseii._taiyokeikakuFurikomiDate.length); k++) {
                    svf.VrsOutn("PAY_DAY" + (k + 1), j + 1, _param._shugakuDate.formatDateMarkDot(shinseii._taiyokeikakuFurikomiDate[k])); // 支払日n回目
                    svf.VrsOutn("PAY_MONEY" + (k + 1), j + 1, shinseii._taiyokeikakuShishutsuGk[k]); // 支払金額n回目
                }
            }
        } else if ("2".equals(maxShinsei._t030namespare3)) {
            for (int j = 0; j < maxShinsei._taiyokeikakuYushiFurikomiDate.length; j++) {
                svf.VrsOut("LOAN_MONEY", maxShinsei._taiyokeikakuYushiShiharaiPlanGk[j]); // 貸与金額
                svf.VrsOut("PAY_DAY", _param._shugakuDate.formatDate(maxShinsei._taiyokeikakuYushiFurikomiDate[j])); // 支払日
                svf.VrsOut("PAY_MONEY", maxShinsei._taiyokeikakuYushiShishutsuGk[j]); // 支払金額
            }
        }

        // 返納
        svf.VrsOut("RET1_MONEY", maxShinsei._taiyoHennouYoteiTotalGk); // 返納金額
        svf.VrsOut("RET1_ALL_MONEY", maxShinsei._taiyoHennouTotalGk); // 返納実績総額
        svf.VrsOut("ST_ALL_MONEY", maxShinsei._saikenJokyoGakuShuno); // 収納総額
        svf.VrsOut("PAY1_PERSON", null); // 返納支払人
        svf.VrsOut("RET1_PERSON_METHOD", null); // 返納支払方法
        svf.VrsOut("RET_FROM_PERIOD", _param._shugakuDate.formatNentuki(maxShinsei._taiyoSHennouYm)); // 返納期間(from)
        svf.VrsOut("RET_TO_PERIOD", _param._shugakuDate.formatNentuki(maxShinsei._taiyoEHennouYm)); // 返納期間(to)

        // 返還
        svf.VrsOut("RET2_MONEY", maxShinsei._saikenJokyoHenkanGk); // 返還金額
        svf.VrsOut("RET2_METHOD", maxShinsei._saikenJokyoHenkanHouhouName); // 返還方法
        svf.VrsOut("RET2_NUM", maxShinsei._saikenJokyoHenkanKaisuu); // 返還回数
        svf.VrsOut("PAY_FIRST_MONEY", maxShinsei._saikenJokyoFirstHenkanGk); // 初回額
        svf.VrsOut("PAY_LAST_MONEY", maxShinsei._saikenJokyoLastHenkanGk); // 最終回額
        svf.VrsOut("RET2_FROM_PERIOD", _param._shugakuDate.formatNentuki(maxShinsei._saikenJokyoSHenkanYm)); // 返還期間(from)
        svf.VrsOut("RET2_TO_PERIOD", _param._shugakuDate.formatNentuki(maxShinsei._saikenJokyoEHenkanYm)); // 返還期間(to)
        svf.VrsOut("PAY2_PERSON", maxShinsei._henkanShiharaininKbnName); // 返還支払人
        svf.VrsOut("RET2_PERSON_METHOD", maxShinsei._henkanShiharaiHohoName); // 返還支払方法

        if (null != maxShinsei._furikaeKouza) {
            svf.VrsOut("BANK_NAME", maxShinsei._furikaeKouza._bankname); // 銀行名
            svf.VrsOut("BRANCH_NAME", maxShinsei._furikaeKouza._branchname); // 支店名
            svf.VrsOut("ITEM", maxShinsei._furikaeKouza._yokinDivName); // 種目
            svf.VrsOut("AC_NUMBER", maxShinsei._furikaeKouza._accountNo); // 口座番号
            svf.VrsOut("AC_NAME", maxShinsei._furikaeKouza.getMeigiKana()); // 口座名義人
        }

        // 返還猶予
        for (int j = 0; j < Math.min(3, maxShinsei._yuyoJiyuCd.length); j++) {
            svf.VrsOutn("RETPP_FROM_PERIOD", j + 1, _param._shugakuDate.formatNentuki(maxShinsei._yuyoSYuyoYm[j])); // 返還猶予期間(from)
            svf.VrsOutn("RETPP_TO_PERIOD", j + 1, _param._shugakuDate.formatNentuki(maxShinsei._yuyoEYuyoYm[j])); // 返還猶予期間(to)
            svf.VrsOutn("RETPP_REASON", j + 1, maxShinsei._yuyoJiyu[j]); // 返還猶予理由
        }

        // 返還免除
        svf.VrsOut("LOAN_EX_MONEY", maxShinsei._saikenJokyoHenkanGk); // 貸与金額
        svf.VrsOut("LOAN_EX_AL_MONEY", maxShinsei._saikenJokyoGakuShuno); // 返還済額
        svf.VrsOut("LOAN_EX_APPLI_MONEY", maxShinsei._menjoTotalGk); // 免除申請額
        svf.VrsOut("LOAN_EX_APPLI_REASON", maxShinsei._menjoJiyu); // 免除申請理由

        svf.VrEndPage();
        _hasData = true;
    }

    private void printMain3(final Vrw32alp svf, final KojinShinseiHistDat maxShinsei) {

        final String title;
        if ("1".equals(maxShinsei._t030namespare3)) {
            title = "高等学校等修学金貸与台帳";
        } else if ("2".equals(maxShinsei._t030namespare3)) {
            title = "高等学校等修学支度金貸与台帳";
        } else {
            return;
        }
        svf.VrSetForm("KNJTP051_3.frm", 1);
        svf.VrsOut("TITLE", title);
        svf.VrsOut("NO", maxShinsei._shuugakuNo); // 修学生番号
        svf.VrsOut("PAGE", String.valueOf(1)); // ページ
        svf.VrsOut("NAME", maxShinsei.getName()); // 修学生氏名
        svf.VrsOut("DATE", _param._shugakuDate.formatDate(_param._loginDate)); // 日付

        final List list = maxShinsei._choteiList;
        final int max = 60;
        int page = 0;
        int line = 0;
        BigDecimal zandaka = null;
        BigDecimal henkanGkTotal = new BigDecimal("0");
        BigDecimal shunoGkTotal = new BigDecimal("0");
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final ChoteiNoufuAdd chotei = (ChoteiNoufuAdd) it.next();
            if (null == zandaka && NumberUtils.isNumber(chotei._sumHenkanGk)) {
                zandaka = new BigDecimal(chotei._sumHenkanGk);
            }
            line += 1;
            if (line % max == 1) {
                if (page > 0) {
                    svf.VrEndPage();
                }
                page += 1;
            }
            final int i = (line % max == 0) ? max : line % max;
            svf.VrsOut("TITLE", title);
            svf.VrsOut("NO", maxShinsei._shuugakuNo); // 修学生番号
            svf.VrsOut("PAGE", String.valueOf(page)); // ページ
            svf.VrsOut("NAME", maxShinsei.getName()); // 修学生氏名
            svf.VrsOut("DATE", _param._shugakuDate.formatDate(_param._loginDate)); // 日付

            // 計画
            svf.VrsOutn("TIMES", i, String.valueOf(chotei._choteiKaisu)); // 回数
            svf.VrsOutn("OBJECT_YEARS", i, _param._shugakuDate.formatNentuki(chotei._choteiYm)); // 対象年月
            svf.VrsOutn("REFUND", i, chotei._henkanGk); // 返還金
            if (NumberUtils.isNumber(chotei._henkanGk)) {
                final BigDecimal bdHenkanGk = new BigDecimal(chotei._henkanGk);
                zandaka = zandaka.subtract(bdHenkanGk);
                henkanGkTotal = henkanGkTotal.add(bdHenkanGk);
            }
            svf.VrsOutn("BALANCE", i, zandaka.toString()); // 残高

            // 実績
            if (null != chotei._noufuKigen) {
                svf.VrsOutn("DUE_DATE", i, _param._shugakuDate.formatDate(chotei._noufuKigen)); // 納期限
            }
            if (null != chotei._shunoDate) {
                svf.VrsOutn("PAY_DAY", i, _param._shugakuDate.formatDate(chotei._shunoDate)); // 収納日
            }
            svf.VrsOutn("PAYMENT", i, chotei._shunoTotalGk); // 収納額
            if (NumberUtils.isNumber(chotei._shunoTotalGk)) {
                shunoGkTotal = shunoGkTotal.add(new BigDecimal(chotei._shunoTotalGk));
            }

            // 遅延
            svf.VrsOutn("DELAY_DAY", i, null); // 遅延日数
            svf.VrsOutn("DELAY_INTEREST", i, null); // 遅延利息
        }
        svf.VrsOutn("REFUND", max + 1, henkanGkTotal.toString());
        svf.VrsOutn("PAYMENT", max + 1, shunoGkTotal.toString());
        if (null != maxShinsei._minouGk || null != maxShinsei._minouCount) {
            svf.VrsOut("TEXT_KEIKA_MINOU", "納期限経過の未納額計");
        }
        if (null != maxShinsei._minouGk) {
            svf.VrsOut("KEIKA_MINOU", maxShinsei._minouGk);
        }
        if (null != maxShinsei._minouCount) {
            svf.VrsOut("KEIKA_MINOU_KAISUU", "（" + (StringUtils.repeat(" ", 3 - maxShinsei._minouCount.length())) + maxShinsei._minouCount + " 回分）");
        }
        if (null != maxShinsei._mitouraiGk || null != maxShinsei._mitouraiCount) {
            svf.VrsOut("TEXT_MITOURAI", "納期限未到来額");
        }
        if (null != maxShinsei._mitouraiGk) {
            svf.VrsOut("MITOURAI", maxShinsei._mitouraiGk);
        }
        if (null != maxShinsei._mitouraiCount) {
            svf.VrsOut("MITOURAI_KAISUU", "（" + (StringUtils.repeat(" ", 3 - maxShinsei._mitouraiCount.length())) + maxShinsei._mitouraiCount + " 回分）");
        }

        svf.VrEndPage();
        _hasData = true;
    }

    private static class Kojin {

        private static String KEY_SHUUGAKU_NO = "SHUUGAKU_NO";
        private static String KEY_KOJIN_SHINSEI_HIST_DAT_MAX = "KOJIN_SHINSEI_HIST_DAT_MAX";
        private static String KEY_KOJIN_SHINSEI_HIST_DAT_LIST = "KOJIN_SHINSEI_HIST_DAT_LIST";

        final String _kojinNo;
        final String _shuugakuNo;

        List _shinseiList = Collections.EMPTY_LIST;

        Kojin(final String kojinNo, final String shuugakuNo) {
            _kojinNo = kojinNo;
            _shuugakuNo = shuugakuNo;
        }

        public void load(final DB2UDB db2, final Param param) {
            _shinseiList = KojinShinseiHistDat.load(db2, param, this);
        }

        List groupShinseiHistByShuugakuNo() {
            final List rtn = new ArrayList();
            final TreeSet shuugakuNos = new TreeSet();
            for (final Iterator it = _shinseiList.iterator(); it.hasNext();) {
                final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) it.next();
                if (null != shinsei._shuugakuNo) {
                    shuugakuNos.add(shinsei._shuugakuNo);
                }
            }

            for (final Iterator it = shuugakuNos.iterator(); it.hasNext();) {
                final String shuugakuNo = (String) it.next();
                // 修学番号ごとに申請履歴データをグループ化
                final List shinseiList = new ArrayList();
                for (final Iterator it2 = _shinseiList.iterator(); it2.hasNext();) {
                    final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) it2.next();
                    if (shuugakuNo.equals(shinsei._shuugakuNo)) {
                        shinseiList.add(shinsei);
                    }
                }

                if (!shinseiList.isEmpty()) {
                    // 最新の申請履歴データ
                    final KojinShinseiHistDat shinseiMax = (KojinShinseiHistDat) new TreeSet(shinseiList).last();
                    final Map m = new HashMap();
                    m.put(KEY_SHUUGAKU_NO, shuugakuNo);
                    m.put(KEY_KOJIN_SHINSEI_HIST_DAT_MAX, shinseiMax);
                    m.put(KEY_KOJIN_SHINSEI_HIST_DAT_LIST, shinseiList);
                    rtn.add(m);
                }
            }
            return rtn;
        }

        public String toString() {
            return "Kojin(" + _kojinNo + ":" + _shuugakuNo + ")";
        }
    }

    private static class KojinShinseiHistDat implements Comparable {
        final String _kojinNo;
        final String _shinseiYear;
        final String _shikinShousaiDiv;
        final String _issuedate;
        final String _ukeYear;
        final String _ukeNo;
        final String _ukeEdaban;
        final String _shinseiDate;
        final String _shuugakuNo;
        final String _nenrei;
        final String _yoyakuKibouGk;
        final String _sYoyakuKibouYm;
        final String _eYoyakuKibouYm;
        final String _sTaiyoYm;
        final String _eTaiyoYm;
        final String _shinseiDiv;
        final String _keizokuKaisuu;
        final String _heikyuuShougakuStatus1;
        final String _heikyuuShougakuRemark1;
        final String _heikyuuShougakuStatus2;
        final String _heikyuuShougakuRemark2;
        final String _heikyuuShougakuStatus3;
        final String _heikyuuShougakuRemark3;
        final String _shitakuCancelChokuFlg;
        final String _shitakuCancelRiFlg;
        final String _hSchoolCd;
        final String _schoolName;
        final String _katei;
        final String _kateiName;
        final String _grade;
        final String _entDate;
        final String _hGradYm;
        final String _shitakukinTaiyoDiv;
        final String _heikyuuShitakuStatus1;
        final String _heikyuuShitakuRemark1;
        final String _heikyuuShitakuStatus2;
        final String _heikyuuShitakuRemark2;
        final String _heikyuuShitakuStatus3;
        final String _heikyuuShitakuRemark3;
        final String _yuushiFail;
        final String _yuushiFailDiv;
        final String _yuushiFailRemark;
        final String _bankCd;
        final String _yuushiCourseDiv;
        final String _rentaiCd;
        final String _shinken1Cd;
        final String _shinken2Cd;
        final String _shutaruCd;
        final String _shinseiKanryouFlg;
        final String _shinseiCancelFlg;
        final String _ketteiDate;
        final String _ketteiFlg;
        final String _t030namespare3;

        ShinkenshaHistDat _shinken1 = null;
        ShinkenshaHistDat _rentai = null;
        FurikaeKouzaDat _furikaeKouza = null;
        List _choteiList = Collections.EMPTY_LIST;

        String _kojinFamilyName;
        String _kojinFirstName;
        String _kojinFamilyNameKana;
        String _kojinFirstNameKana;
        String _kojinBirthday;
        String _kojinZipcd;
        String _kojinCitycd;
        String _kojinAddr1;
        String _kojinAddr2;
        String _kojinTelno1;
        String _kojinTelno2;
        String _kojinRemark;

        String _taiyoyoyakuYoyakuShinseiDate;
        String _taiyoyoyakuKetteiDate;

        String _taiyoSotsugyouyoteiDate;
        String _taiyoSTaiyokibouDate;
        String _taiyoETaiyokibouDate;
        String _taiyoShishutsuTotalMonths;
        String _taiyoShishutsuTotalGk;
        String _taiyoShishutsuShoriTotalGk;

        String _saikenJokyoGakuShuno;
        String _taiyoHennouYoteiTotalGk;
        String _taiyoHennouTotalGk;
        String _taiyoSHennouYm;
        String _taiyoEHennouYm;

        String _taiyoMenjyoTotalGk;

        String _saikenJokyoHenkanGk;
        String _saikenJokyoHenkanHouhouName;
        String _saikenJokyoHenkanKaisuu;
        String _saikenJokyoSHenkanYm;
        String _saikenJokyoEHenkanYm;
        String _saikenJokyoFirstHenkanGk;
        String _saikenJokyoLastHenkanGk;

        String[] _taiyokeikakuFurikomiDate = {};
        String[] _taiyokeikakuShishutsuGk = {};
        String[] _taiyokeikakuYushiFurikomiDate = {};
        String[] _taiyokeikakuYushiShishutsuGk = {};
        String[] _taiyokeikakuYushiShiharaiPlanGk = {};

        String[] _yuyoJiyuCd = {};
        String[] _yuyoJiyu = {};
        String[] _yuyoSYuyoYm = {};
        String[] _yuyoEYuyoYm = {};

        String _noufuGk;
        String _menjoTotalGk;
        String _menjoJiyu;

        String _henkanShiharaininKbnName;
        String _henkanShiharaiHohoName;

        List _happuDomeFlgCommentList = Collections.EMPTY_LIST;
        List _jikoFlgCommentList = Collections.EMPTY_LIST;

        String _funoAri;

        String _minouGk;
        String _minouCount;
        String _mitouraiGk;
        String _mitouraiCount;

        KojinShinseiHistDat(
                final String kojinNo,
                final String shinseiYear,
                final String shikinShousaiDiv,
                final String issuedate,
                final String ukeYear,
                final String ukeNo,
                final String ukeEdaban,
                final String shinseiDate,
                final String shuugakuNo,
                final String nenrei,
                final String yoyakuKibouGk,
                final String sYoyakuKibouYm,
                final String eYoyakuKibouYm,
                final String sTaiyoYm,
                final String eTaiyoYm,
                final String shinseiDiv,
                final String keizokuKaisuu,
                final String heikyuuShougakuStatus1,
                final String heikyuuShougakuRemark1,
                final String heikyuuShougakuStatus2,
                final String heikyuuShougakuRemark2,
                final String heikyuuShougakuStatus3,
                final String heikyuuShougakuRemark3,
                final String shitakuCancelChokuFlg,
                final String shitakuCancelRiFlg,
                final String hSchoolCd,
                final String schoolName,
                final String katei,
                final String kateiName,
                final String grade,
                final String entDate,
                final String hGradYm,
                final String shitakukinTaiyoDiv,
                final String heikyuuShitakuStatus1,
                final String heikyuuShitakuRemark1,
                final String heikyuuShitakuStatus2,
                final String heikyuuShitakuRemark2,
                final String heikyuuShitakuStatus3,
                final String heikyuuShitakuRemark3,
                final String yuushiFail,
                final String yuushiFailDiv,
                final String yuushiFailRemark,
                final String bankCd,
                final String yuushiCourseDiv,
                final String rentaiCd,
                final String shinken1Cd,
                final String shinken2Cd,
                final String shutaruCd,
                final String shinseiKanryouFlg,
                final String shinseiCancelFlg,
                final String ketteiDate,
                final String ketteiFlg,
                final String t030namespare3
        ) {
            _kojinNo = kojinNo;
            _shinseiYear = shinseiYear;
            _shikinShousaiDiv = shikinShousaiDiv;
            _issuedate = issuedate;
            _ukeYear = ukeYear;
            _ukeNo = ukeNo;
            _ukeEdaban = ukeEdaban;
            _shinseiDate = shinseiDate;
            _shuugakuNo = shuugakuNo;
            _nenrei = nenrei;
            _yoyakuKibouGk = yoyakuKibouGk;
            _sYoyakuKibouYm = sYoyakuKibouYm;
            _eYoyakuKibouYm = eYoyakuKibouYm;
            _sTaiyoYm = sTaiyoYm;
            _eTaiyoYm = eTaiyoYm;
            _shinseiDiv = shinseiDiv;
            _keizokuKaisuu = keizokuKaisuu;
            _heikyuuShougakuStatus1 = heikyuuShougakuStatus1;
            _heikyuuShougakuRemark1 = heikyuuShougakuRemark1;
            _heikyuuShougakuStatus2 = heikyuuShougakuStatus2;
            _heikyuuShougakuRemark2 = heikyuuShougakuRemark2;
            _heikyuuShougakuStatus3 = heikyuuShougakuStatus3;
            _heikyuuShougakuRemark3 = heikyuuShougakuRemark3;
            _shitakuCancelChokuFlg = shitakuCancelChokuFlg;
            _shitakuCancelRiFlg = shitakuCancelRiFlg;
            _hSchoolCd = hSchoolCd;
            _schoolName = schoolName;
            _katei = katei;
            _kateiName = kateiName;
            _grade = grade;
            _entDate = entDate;
            _hGradYm = hGradYm;
            _shitakukinTaiyoDiv = shitakukinTaiyoDiv;
            _heikyuuShitakuStatus1 = heikyuuShitakuStatus1;
            _heikyuuShitakuRemark1 = heikyuuShitakuRemark1;
            _heikyuuShitakuStatus2 = heikyuuShitakuStatus2;
            _heikyuuShitakuRemark2 = heikyuuShitakuRemark2;
            _heikyuuShitakuStatus3 = heikyuuShitakuStatus3;
            _heikyuuShitakuRemark3 = heikyuuShitakuRemark3;
            _yuushiFail = yuushiFail;
            _yuushiFailDiv = yuushiFailDiv;
            _yuushiFailRemark = yuushiFailRemark;
            _bankCd = bankCd;
            _yuushiCourseDiv = yuushiCourseDiv;
            _rentaiCd = rentaiCd;
            _shinken1Cd = shinken1Cd;
            _shinken2Cd = shinken2Cd;
            _shutaruCd = shutaruCd;
            _shinseiKanryouFlg = shinseiKanryouFlg;
            _shinseiCancelFlg = shinseiCancelFlg;
            _ketteiDate = ketteiDate;
            _ketteiFlg = ketteiFlg;
            _t030namespare3 = t030namespare3;
        }

        public String getName() {
            return StringUtils.defaultString(_kojinFamilyName) + "　" +  StringUtils.defaultString(_kojinFirstName);
        }

        public String getKana() {
            return StringUtils.defaultString(_kojinFamilyNameKana) + "　" +  StringUtils.defaultString(_kojinFirstNameKana);
        }

        public int compareTo(final Object obj) {
            final KojinShinseiHistDat other = (KojinShinseiHistDat) obj;
            return _shinseiYear.compareTo(other._shinseiYear);
        }

        public static List load(final DB2UDB db2, final Param param, final Kojin kojin) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final List list = new ArrayList();
            try {
                final String sql = sql(param, kojin);
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                     final String kojinNo = rs.getString("KOJIN_NO");
                     final String shinseiYear = rs.getString("SHINSEI_YEAR");
                     final String shikinShousaiDiv = rs.getString("SHIKIN_SHOUSAI_DIV");
                     final String issuedate = rs.getString("ISSUEDATE");
                     final String ukeYear = rs.getString("UKE_YEAR");
                     final String ukeNo = rs.getString("UKE_NO");
                     final String ukeEdaban = rs.getString("UKE_EDABAN");
                     final String shinseiDate = rs.getString("SHINSEI_DATE");
                     final String shuugakuNo = rs.getString("SHUUGAKU_NO");
                     final String nenrei = rs.getString("NENREI");
                     final String yoyakuKibouGk = rs.getString("YOYAKU_KIBOU_GK");
                     final String sYoyakuKibouYm = rs.getString("S_YOYAKU_KIBOU_YM");
                     final String eYoyakuKibouYm = rs.getString("E_YOYAKU_KIBOU_YM");
                     final String sTaiyoYm = rs.getString("S_TAIYO_YM");
                     final String eTaiyoYm = rs.getString("E_TAIYO_YM");
                     final String shinseiDiv = rs.getString("SHINSEI_DIV");
                     final String keizokuKaisuu = rs.getString("KEIZOKU_KAISUU");
                     final String heikyuuShougakuStatus1 = rs.getString("HEIKYUU_SHOUGAKU_STATUS1");
                     final String heikyuuShougakuRemark1 = rs.getString("HEIKYUU_SHOUGAKU_REMARK1");
                     final String heikyuuShougakuStatus2 = rs.getString("HEIKYUU_SHOUGAKU_STATUS2");
                     final String heikyuuShougakuRemark2 = rs.getString("HEIKYUU_SHOUGAKU_REMARK2");
                     final String heikyuuShougakuStatus3 = rs.getString("HEIKYUU_SHOUGAKU_STATUS3");
                     final String heikyuuShougakuRemark3 = rs.getString("HEIKYUU_SHOUGAKU_REMARK3");
                     final String shitakuCancelChokuFlg = rs.getString("SHITAKU_CANCEL_CHOKU_FLG");
                     final String shitakuCancelRiFlg = rs.getString("SHITAKU_CANCEL_RI_FLG");
                     final String hSchoolCd = rs.getString("H_SCHOOL_CD");
                     final String schoolName = rs.getString("SCHOOL_NAME");
                     final String katei = rs.getString("KATEI");
                     final String kateiName = rs.getString("KATEI_NAME");
                     final String grade = NumberUtils.isNumber(rs.getString("GRADE")) ? String.valueOf(Integer.parseInt(rs.getString("GRADE"))) : rs.getString("GRADE");
                     final String entDate = rs.getString("ENT_DATE");
                     final String hGradYm = rs.getString("H_GRAD_YM");
                     final String shitakukinTaiyoDiv = rs.getString("SHITAKUKIN_TAIYO_DIV");
                     final String heikyuuShitakuStatus1 = rs.getString("HEIKYUU_SHITAKU_STATUS1");
                     final String heikyuuShitakuRemark1 = rs.getString("HEIKYUU_SHITAKU_REMARK1");
                     final String heikyuuShitakuStatus2 = rs.getString("HEIKYUU_SHITAKU_STATUS2");
                     final String heikyuuShitakuRemark2 = rs.getString("HEIKYUU_SHITAKU_REMARK2");
                     final String heikyuuShitakuStatus3 = rs.getString("HEIKYUU_SHITAKU_STATUS3");
                     final String heikyuuShitakuRemark3 = rs.getString("HEIKYUU_SHITAKU_REMARK3");
                     final String yuushiFail = rs.getString("YUUSHI_FAIL");
                     final String yuushiFailDiv = rs.getString("YUUSHI_FAIL_DIV");
                     final String yuushiFailRemark = rs.getString("YUUSHI_FAIL_REMARK");
                     final String bankCd = rs.getString("BANK_CD");
                     final String yuushiCourseDiv = rs.getString("YUUSHI_COURSE_DIV");
                     final String rentaiCd = rs.getString("RENTAI_CD");
                     final String shinken1Cd = rs.getString("SHINKEN1_CD");
                     final String shinken2Cd = rs.getString("SHINKEN2_CD");
                     final String shutaruCd = rs.getString("SHUTARU_CD");
                     final String shinseiKanryouFlg = rs.getString("SHINSEI_KANRYOU_FLG");
                     final String shinseiCancelFlg = rs.getString("SHINSEI_CANCEL_FLG");
                     final String ketteiDate = rs.getString("KETTEI_DATE");
                     final String ketteiFlg = rs.getString("KETTEI_FLG");
                     final String t030namespare3 = rs.getString("T030_NAMESPARE3");

                     final KojinShinseiHistDat shinsei = new KojinShinseiHistDat(kojinNo, shinseiYear, shikinShousaiDiv, issuedate, ukeYear, ukeNo, ukeEdaban, shinseiDate, shuugakuNo, nenrei, yoyakuKibouGk, sYoyakuKibouYm, eYoyakuKibouYm, sTaiyoYm, eTaiyoYm, shinseiDiv, keizokuKaisuu, heikyuuShougakuStatus1, heikyuuShougakuRemark1, heikyuuShougakuStatus2, heikyuuShougakuRemark2, heikyuuShougakuStatus3, heikyuuShougakuRemark3, shitakuCancelChokuFlg, shitakuCancelRiFlg, hSchoolCd, schoolName, katei, kateiName,
                             grade, entDate, hGradYm, shitakukinTaiyoDiv, heikyuuShitakuStatus1, heikyuuShitakuRemark1, heikyuuShitakuStatus2, heikyuuShitakuRemark2, heikyuuShitakuStatus3, heikyuuShitakuRemark3, yuushiFail, yuushiFailDiv, yuushiFailRemark, bankCd, yuushiCourseDiv, rentaiCd, shinken1Cd, shinken2Cd, shutaruCd, shinseiKanryouFlg, shinseiCancelFlg, ketteiDate, ketteiFlg, t030namespare3);
                     list.add(shinsei);
                 }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
           } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
           }
           if (!list.isEmpty()) {
               setKojinHistDat(db2, param, list);
               setFurikaeKouzaDat(db2, param, list);
               setShinkenshaHistDat(db2, param, list);
               setTaiyoyoyakuHistDat(db2, param, list);
               setTaiyoDat(db2, param, list);
               setTaiyoKeikakuDat(db2, param, list);
               setMenjoDat(db2, param, list);
               setYuyoDat(db2, param, list);
               setNoufuDat(db2, param, list);
               setSaikenJokyo(db2, param, list);
               setChoteiNoufuAdd(db2, param, list);
               setFlgComment(db2, param, list);
               setFunoari(db2, param, list);
               setMinoGakuMitouraiGaku(db2, param, list);
           }

           return list;
        }

        private static String sql(final Param param, final Kojin kojin) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   T1.*, ");
            stb.append("   L3.NAME AS SCHOOL_NAME, ");
            stb.append("   L4.NAMESPARE3 AS T030_NAMESPARE3, ");
            stb.append("   L5.KATEI_NAME ");
            stb.append(" FROM KOJIN_SHINSEI_HIST_DAT T1 ");
            stb.append(" LEFT JOIN SCHOOL_DAT L3 ON L3.SCHOOLCD = T1.H_SCHOOL_CD ");
            stb.append(" LEFT JOIN NAME_MST L4 ON L4.NAMECD1 = 'T030' ");
            stb.append("     AND L4.NAMECD2 = T1.SHIKIN_SHOUSAI_DIV ");
            stb.append(" LEFT JOIN KATEI_MST L5 ON L5.KATEI = T1.KATEI ");
            stb.append(" WHERE KOJIN_NO = '" + kojin._kojinNo + "' ");
            if (null != kojin._shuugakuNo) {
                stb.append(" AND SHUUGAKU_NO = '" + kojin._shuugakuNo + "' ");
            }
            stb.append(" ORDER BY SHUUGAKU_NO, T1.SHINSEI_YEAR ");
            return stb.toString();
        }

        private static void setFurikaeKouzaDat(final DB2UDB db2, final Param param, final Collection shinseiList) {
            final Set shuugakuNos = new HashSet();
            for (final Iterator it = shinseiList.iterator(); it.hasNext();) {
                final KojinShinseiHistDat taiyoyoyaku = (KojinShinseiHistDat) it.next();
                shuugakuNos.add(taiyoyoyaku._shuugakuNo);
            }

            final Map kouzaBankDatMap = FurikaeKouzaDat.load(db2, shuugakuNos);

            for (final Iterator it = shinseiList.iterator(); it.hasNext();) {
                final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) it.next();
                final FurikaeKouzaDat bank = (FurikaeKouzaDat) kouzaBankDatMap.get(shinsei._shuugakuNo);
                if (null != bank) {
                    shinsei._furikaeKouza = (FurikaeKouzaDat) kouzaBankDatMap.get(shinsei._shuugakuNo);
                }
            }
        }

        private static void setShinkenshaHistDat(final DB2UDB db2, final Param param, final Collection shinseiList) {
            Set shinkencds1 = new HashSet();
            Set rentaicds = new HashSet();
            for (final Iterator it = shinseiList.iterator(); it.hasNext();) {
                final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) it.next();
                if (null != shinsei._shinken1Cd) {
                    shinkencds1.add(shinsei._shinken1Cd);
                }
                if (null != shinsei._rentaiCd) {
                    rentaicds.add(shinsei._rentaiCd);
                }
            }

            final Map shinken1Map = ShinkenshaHistDat.load(db2, shinkencds1);
            final Map rentaiMap = ShinkenshaHistDat.load(db2, rentaicds);

            for (final Iterator it = shinseiList.iterator(); it.hasNext();) {
                final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) it.next();
                final ShinkenshaHistDat shiken1 = (ShinkenshaHistDat) shinken1Map.get(shinsei._shinken1Cd);
                if (null != shiken1) {
                    shinsei._shinken1 = (ShinkenshaHistDat) shinken1Map.get(shinsei._shinken1Cd);
                }
                final ShinkenshaHistDat rentai = (ShinkenshaHistDat) rentaiMap.get(shinsei._rentaiCd);
                if (null != rentai) {
                    shinsei._rentai = (ShinkenshaHistDat) rentaiMap.get(shinsei._rentaiCd);
                }
            }
        }

        private static void setKojinHistDat(final DB2UDB db2, final Param param, final Collection shinseiList) {
            final Set kojinNos = new HashSet();
            for (final Iterator it = shinseiList.iterator(); it.hasNext();) {
                final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) it.next();
                kojinNos.add(shinsei._kojinNo);
            }
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map nameMap = new HashMap();
            try {
                final String sql = sqlKojinHistDat(kojinNos);
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                ResultSetMetaData meta = rs.getMetaData();
                while (rs.next()) {
                    final Map m = new HashMap();
                    for (int i = 1; i <= meta.getColumnCount(); i++) {
                        final String columnName = meta.getColumnLabel(i);
                        m.put(columnName, rs.getString(columnName));
                    }
                    nameMap.put(rs.getString("KOJIN_NO"), m);
                }

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            for (final Iterator it = shinseiList.iterator(); it.hasNext();) {
                final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) it.next();
                final Map name = (Map) nameMap.get(shinsei._kojinNo);
                if (null != name) {
                    shinsei._kojinFamilyName = (String) name.get("FAMILY_NAME");
                    shinsei._kojinFirstName = (String) name.get("FIRST_NAME");
                    shinsei._kojinFamilyNameKana = (String) name.get("FAMILY_NAME_KANA");
                    shinsei._kojinFirstNameKana = (String) name.get("FIRST_NAME_KANA");

                    shinsei._kojinBirthday = (String) name.get("BIRTHDAY");
                    shinsei._kojinZipcd = (String) name.get("ZIPCD");
                    shinsei._kojinCitycd = (String) name.get("CITYCD");
                    shinsei._kojinAddr1 = (String) name.get("ADDR1");
                    shinsei._kojinAddr2 = (String) name.get("ADDR2");
                    shinsei._kojinTelno1 = (String) name.get("TELNO1");
                    shinsei._kojinTelno2 = (String) name.get("TELNO2");
                    shinsei._kojinRemark = (String) name.get("REMARK");
                }
            }
        }

        private static String sqlKojinHistDat(final Collection kojinNos) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH MAX_HIST AS ( ");
            stb.append("     SELECT KOJIN_NO, MAX(ISSUEDATE) AS ISSUEDATE ");
            stb.append("     FROM KOJIN_HIST_DAT  ");
            stb.append("     WHERE KOJIN_NO IN " + SQLUtils.whereIn(true, toArray(kojinNos)) + " ");
            stb.append("     GROUP BY KOJIN_NO ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.* ");
            stb.append(" FROM KOJIN_HIST_DAT T1  ");
            stb.append(" INNER JOIN MAX_HIST T2 ON T2.KOJIN_NO = T1.KOJIN_NO AND T2.ISSUEDATE = T1.ISSUEDATE ");
            return stb.toString();
        }


        private static void setTaiyoyoyakuHistDat(final DB2UDB db2, final Param param, final Collection shinseiList) {
            final Set kojinNos = new HashSet();
            for (final Iterator it = shinseiList.iterator(); it.hasNext();) {
                final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) it.next();
                kojinNos.add(shinsei._kojinNo);
            }

            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map taiyoyoyakuHistDatMap = new HashMap();
            try {
                final String sql = sqlKojinTaiyoyoyakuHistDat(kojinNos);
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                ResultSetMetaData meta = rs.getMetaData();
                while (rs.next()) {
                    final Map m = new HashMap();
                    for (int i = 1; i <= meta.getColumnCount(); i++) {
                        final String columnName = meta.getColumnLabel(i);
                        m.put(columnName, rs.getString(columnName));
                    }
                    taiyoyoyakuHistDatMap.put(rs.getString("KOJIN_NO"), m);
                }

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            for (final Iterator it = shinseiList.iterator(); it.hasNext();) {
                final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) it.next();
                final Map taiyoyoyaku = (Map) taiyoyoyakuHistDatMap.get(shinsei._kojinNo);
                if (null != taiyoyoyaku) {
                    shinsei._taiyoyoyakuYoyakuShinseiDate = (String) taiyoyoyaku.get("YOYAKU_SHINSEI_DATE");
                    shinsei._taiyoyoyakuKetteiDate = (String) taiyoyoyaku.get("KETTEI_DATE");
                }
            }
        }

        private static String sqlKojinTaiyoyoyakuHistDat(final Collection kojinNos) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH MIN_YEAR AS (SELECT ");
            stb.append("     T1.KOJIN_NO, MIN(SHINSEI_YEAR) AS SHINSEI_YEAR ");
            stb.append(" FROM ");
            stb.append("     KOJIN_TAIYOYOYAKU_HIST_DAT T1  ");
            stb.append(" WHERE ");
            stb.append("     KOJIN_NO IN " + SQLUtils.whereIn(true, toArray(kojinNos)) + " ");
            stb.append(" GROUP BY ");
            stb.append("     KOJIN_NO ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.* ");
            stb.append(" FROM ");
            stb.append("     KOJIN_TAIYOYOYAKU_HIST_DAT T1  ");
            stb.append("     INNER JOIN MIN_YEAR T2 ON T2.KOJIN_NO = T1.KOJIN_NO AND T2.SHINSEI_YEAR = T1.SHINSEI_YEAR  ");
            return stb.toString();
        }

        private static void setTaiyoKeikakuDat(final DB2UDB db2, final Param param, final Collection shinseiList) {
            final Set keys = new HashSet();
            for (final Iterator it = shinseiList.iterator(); it.hasNext();) {
                final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) it.next();
                keys.add(shinsei._shuugakuNo + "-" + shinsei._shinseiYear);
            }

            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map taiyoMap = new HashMap();
            try {
                final String sql = sqlTaiyoKeikakuDat(keys, "1");
                log.debug(" taiyo keikaku sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                ResultSetMetaData meta = rs.getMetaData();
                while (rs.next()) {
                    final Map m = new HashMap();
                    for (int i = 1; i <= meta.getColumnCount(); i++) {
                        final String columnName = meta.getColumnLabel(i);
                        m.put(columnName, rs.getString(columnName));
                    }
                    final String key = rs.getString("SHUUGAKU_NO") + "-" + rs.getString("SHINSEI_YEAR");
                    if (null == taiyoMap.get(key)) {
                        taiyoMap.put(key, new ArrayList());
                    }
                    final List list = (List) taiyoMap.get(key);
                    list.add(m);
                }

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            for (final Iterator it = shinseiList.iterator(); it.hasNext();) {
                final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) it.next();
                final List taiyokeikaku = (List) taiyoMap.get(shinsei._shuugakuNo + "-" + shinsei._shinseiYear);
                if (null != taiyokeikaku) {
                    int size = 0;
                    for (int i = 0; i < taiyokeikaku.size(); i++) {
                        final Map m = (Map) taiyokeikaku.get(i);
                        if (!NumberUtils.isDigits((String) m.get("NAMECD2"))) {
                            continue;
                        }
                        final int j = Integer.parseInt((String) m.get("NAMECD2"));
                        size = Math.max(size, j);
                    }
                    shinsei._taiyokeikakuFurikomiDate = new String[size];
                    shinsei._taiyokeikakuShishutsuGk = new String[size];
                    boolean[] isset = new boolean[size];
                    for (int i = 0; i < taiyokeikaku.size(); i++) {
                        final Map m = (Map) taiyokeikaku.get(i);
                        if (!NumberUtils.isDigits((String) m.get("NAMECD2"))) {
                            continue;
                        }
                        int j = Integer.parseInt((String) m.get("NAMECD2"));
                        if (!isset[j - 1]) {
                        } else if (j - 1 - 1 >= 0 && !isset[j - 1 - 1]) {
                            j -= 1;
                        } else {
                            continue;
                        }
                        shinsei._taiyokeikakuFurikomiDate[j - 1] = (String) m.get("FURIKOMI_DATE");
                        shinsei._taiyokeikakuShishutsuGk[j - 1] = (String) m.get("SHISHUTSU_GK");
                        isset[j - 1] = true;
                    }
                }
            }

            final Map taiyoMap2 = new HashMap();
            try {
                final String sql = sqlTaiyoKeikakuDat(keys, "2");
                log.debug(" taiyo keikaku 2 sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                ResultSetMetaData meta = rs.getMetaData();
                while (rs.next()) {
                    final Map m = new HashMap();
                    for (int i = 1; i <= meta.getColumnCount(); i++) {
                        final String columnName = meta.getColumnLabel(i);
                        m.put(columnName, rs.getString(columnName));
                    }
                    final String key = rs.getString("SHUUGAKU_NO") + "-" + rs.getString("SHINSEI_YEAR");
                    if (null == taiyoMap2.get(key)) {
                        taiyoMap2.put(key, new ArrayList());
                    }
                    final List list = (List) taiyoMap2.get(key);
                    list.add(m);
                }

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            for (final Iterator it = shinseiList.iterator(); it.hasNext();) {
                final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) it.next();
                final List taiyokeikaku = (List) taiyoMap2.get(shinsei._shuugakuNo + "-" + shinsei._shinseiYear);
                if (null != taiyokeikaku) {
                    int size = 0;
                    for (int i = 0; i < taiyokeikaku.size(); i++) {
                        final Map m = (Map) taiyokeikaku.get(i);
                        if (!NumberUtils.isDigits((String) m.get("NAMECD2"))) {
                            continue;
                        }
                        final int j = Integer.parseInt((String) m.get("NAMECD2"));
                        size = Math.max(size, j);
                    }
                    shinsei._taiyokeikakuYushiFurikomiDate = new String[size];
                    shinsei._taiyokeikakuYushiShishutsuGk = new String[size];
                    shinsei._taiyokeikakuYushiShiharaiPlanGk = new String[size];
                    boolean[] isset = new boolean[size];
                    for (int i = 0; i < taiyokeikaku.size(); i++) {
                        final Map m = (Map) taiyokeikaku.get(i);
                        if (!NumberUtils.isDigits((String) m.get("NAMECD2"))) {
                            continue;
                        }
                        int j = Integer.parseInt((String) m.get("NAMECD2"));
                        if (!isset[j - 1]) {
                        } else if (j - 1 - 1 >= 0 && !isset[j - 1 - 1]) {
                            j -= 1;
                        } else {
                            continue;
                        }
                        shinsei._taiyokeikakuYushiFurikomiDate[j - 1] = (String) m.get("FURIKOMI_DATE");
                        shinsei._taiyokeikakuYushiShishutsuGk[j - 1] = (String) m.get("SHISHUTSU_GK");
                        shinsei._taiyokeikakuYushiShiharaiPlanGk[j - 1] = (String) m.get("SHIHARAI_PLAN_GK");
                        isset[j - 1] = true;
                    }
                }
            }
        }

        private static String sqlTaiyoKeikakuDat(final Collection keys, final String shikinShubetsu) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("    SHUUGAKU_NO, SHINSEI_YEAR, MIN(T2.NAMECD2) AS NAMECD2, FURIKOMI_DATE, SUM(SHISHUTSU_GK) AS SHISHUTSU_GK, SUM(SHIHARAI_PLAN_GK) AS SHIHARAI_PLAN_GK ");
            stb.append(" FROM ");
            stb.append("     TAIYO_KEIKAKU_DAT T1  ");
            stb.append("     INNER JOIN V_NAME_MST  T2 ON T2.YEAR = T1.SHINSEI_YEAR ");
            stb.append("                            AND T2.NAMECD1 = 'T035' ");
            stb.append("                            AND ( INT(VALUE(T2.NAME1, '99')) <= INT(VALUE(T2.NAME2, T2.NAME1, '99')) AND  MONTH(FURIKOMI_DATE) >= INT(VALUE(T2.NAME1, '99')) AND MONTH(FURIKOMI_DATE) <= INT(VALUE(T2.NAME2, T2.NAME1, '99')) ");
            stb.append("                               OR INT(VALUE(T2.NAME1, '99')) >  INT(VALUE(T2.NAME2, T2.NAME1, '99')) AND (MONTH(FURIKOMI_DATE) >= INT(VALUE(T2.NAME1, '99')) OR  MONTH(FURIKOMI_DATE) <= INT(VALUE(T2.NAME2, T2.NAME1, '99'))) ");
            stb.append("                                ) ");
            stb.append(" WHERE ");
            stb.append("     SHUUGAKU_NO || '-' || SHINSEI_YEAR IN " + SQLUtils.whereIn(true, toArray(keys)) + " ");
            stb.append("     AND FURIKOMI_DATE IS NOT NULL ");
            stb.append("     AND SHIKIN_SHUBETSU = '" + shikinShubetsu + "' ");
            stb.append(" GROUP BY ");
            stb.append("     SHUUGAKU_NO, SHINSEI_YEAR, FURIKOMI_DATE ");
            stb.append(" ORDER BY ");
            stb.append("     SHUUGAKU_NO, SHINSEI_YEAR, FURIKOMI_DATE DESC ");
            return stb.toString();
        }

        private static void setTaiyoDat(final DB2UDB db2, final Param param, final Collection shinseiList) {
            final Set shuugakuNos = new HashSet();
            for (final Iterator it = shinseiList.iterator(); it.hasNext();) {
                final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) it.next();
                shuugakuNos.add(shinsei._shuugakuNo);
            }

            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map taiyoMap = new HashMap();
            try {
                final String sql = sqlKojinTaiyoDat(shuugakuNos);
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                ResultSetMetaData meta = rs.getMetaData();
                while (rs.next()) {
                    final Map m = new HashMap();
                    for (int i = 1; i <= meta.getColumnCount(); i++) {
                        final String columnName = meta.getColumnLabel(i);
                        m.put(columnName, rs.getString(columnName));
                    }
                    taiyoMap.put(rs.getString("SHUUGAKU_NO"), m);
                }

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            for (final Iterator it = shinseiList.iterator(); it.hasNext();) {
                final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) it.next();
                final Map taiyo = (Map) taiyoMap.get(shinsei._shuugakuNo);
                if (null != taiyo) {
                    shinsei._taiyoSotsugyouyoteiDate = (String) taiyo.get("SOTUGYOUYOTEI_DATE");
                    shinsei._taiyoSTaiyokibouDate = (String) taiyo.get("S_TAIYOKIBOU_DATE");
                    shinsei._taiyoETaiyokibouDate = (String) taiyo.get("E_TAIYOKIBOU_DATE");
                    shinsei._taiyoShishutsuTotalMonths = (String) taiyo.get("SHISHUTSU_TOTAL_MONTHS");
                    shinsei._taiyoShishutsuTotalGk = (String) taiyo.get("SHISHUTSU_TOTAL_GK");
                    shinsei._taiyoShishutsuShoriTotalGk = (String) taiyo.get("SHISHUTSU_SHORI_TOTAL_GK");

                    shinsei._taiyoHennouYoteiTotalGk = (String) taiyo.get("HENNOU_YOTEITOTAL_GK");
                    shinsei._taiyoHennouTotalGk = (String) taiyo.get("HENNOU_TOTAL_GK");
                    shinsei._taiyoMenjyoTotalGk = (String) taiyo.get("MENJYO_TOTAL_GK");
                }
            }
        }

        private static String sqlKojinTaiyoDat(final Collection shuugakuNos) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.* ");
            stb.append(" FROM ");
            stb.append("     KOJIN_TAIYO_DAT T1  ");
            stb.append(" WHERE ");
            stb.append("     SHUUGAKU_NO IN " + SQLUtils.whereIn(true, toArray(shuugakuNos)) + " ");
            return stb.toString();
        }

        private static void setYuyoDat(final DB2UDB db2, final Param param, final Collection shinseiList) {
            final Set shuugakuNos = new HashSet();
            for (final Iterator it = shinseiList.iterator(); it.hasNext();) {
                final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) it.next();
                shuugakuNos.add(shinsei._shuugakuNo);
            }

            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map yuyoMap = new HashMap();
            try {
                final String sql = sqlYuyoDat(shuugakuNos);
                log.debug(" yuyo sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                ResultSetMetaData meta = rs.getMetaData();
                while (rs.next()) {
                    final Map m = new HashMap();
                    for (int i = 1; i <= meta.getColumnCount(); i++) {
                        final String columnName = meta.getColumnLabel(i);
                        m.put(columnName, rs.getString(columnName));
                    }
                    if (null == yuyoMap.get(rs.getString("SHUUGAKU_NO"))) {
                        yuyoMap.put(rs.getString("SHUUGAKU_NO"), new ArrayList());
                    }
                    List list = (List) yuyoMap.get(rs.getString("SHUUGAKU_NO"));
                    list.add(m);
                }

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            for (final Iterator it = shinseiList.iterator(); it.hasNext();) {
                final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) it.next();
                final List yuyo = (List) yuyoMap.get(shinsei._shuugakuNo);
                if (null != yuyo) {
                    shinsei._yuyoJiyuCd = new String[yuyo.size()];
                    shinsei._yuyoJiyu = new String[yuyo.size()];
                    shinsei._yuyoSYuyoYm = new String[yuyo.size()];
                    shinsei._yuyoEYuyoYm = new String[yuyo.size()];
                    for (int i = 0; i < yuyo.size(); i++) {
                        final Map m = (Map) yuyo.get(i);
                        shinsei._yuyoJiyuCd[i] = (String) m.get("YUYO_JIYU_CD");
                        shinsei._yuyoJiyu[i] = (String) m.get("YUYO_JIYU");
                        shinsei._yuyoSYuyoYm[i] = (String) m.get("S_YUYO_YM");
                        shinsei._yuyoEYuyoYm[i] = (String) m.get("E_YUYO_YM");
                    }
                }
            }
        }

        private static String sqlYuyoDat(final Collection shuugakuNos) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.*, ");
            stb.append("     NMT015.NAME1 AS YUYO_JIYU ");
            stb.append(" FROM ");
            stb.append("     YUYO_DAT T1  ");
            stb.append(" LEFT JOIN NAME_MST NMT015 ON NMT015.NAMECD1 = 'T015' ");
            stb.append("     AND NMT015.NAMECD2 = T1.YUYO_JIYU_CD  ");
            stb.append(" WHERE ");
            stb.append("     SHUUGAKU_NO IN " + SQLUtils.whereIn(true, toArray(shuugakuNos)) + " ");
            stb.append("     AND YUYO_KEKKA_CD = '2' ");
            stb.append(" ORDER BY ");
            stb.append("     SHUUGAKU_NO, YUYO_SEQ DESC ");
            return stb.toString();
        }

        private static String sqlMenjoDat(final Collection keys) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.*, ");
            stb.append("     NMT014.NAME1 AS MENJO_JIYU_NAME ");
            stb.append(" FROM ");
            stb.append("     MENJO_DAT T1  ");
            stb.append("     LEFT JOIN NAME_MST NMT014 ON NMT014.NAMECD1 = 'T014'  ");
            stb.append("     AND NMT014.NAMECD2 = T1.MENJO_JIYU_CD  ");
            stb.append(" WHERE ");
            stb.append("     SHUUGAKU_NO IN " + SQLUtils.whereIn(true, toArray(keys)) + " ");
            stb.append("     AND MENJO_KEKKA_CD = '2' ");
            return stb.toString();
        }

        private static void setMenjoDat(final DB2UDB db2, final Param param, final Collection shinseiList) {
            final Set shuugakuNos = new HashSet();
            for (final Iterator it = shinseiList.iterator(); it.hasNext();) {
                final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) it.next();
                shuugakuNos.add(shinsei._shuugakuNo);
            }

            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map menjoMap = new HashMap();
            try {
                final String sql = sqlMenjoDat(shuugakuNos);
                log.debug(" menjo sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                ResultSetMetaData meta = rs.getMetaData();
                while (rs.next()) {
                    final Map m = new HashMap();
                    for (int i = 1; i <= meta.getColumnCount(); i++) {
                        final String columnName = meta.getColumnLabel(i);
                        m.put(columnName, rs.getString(columnName));
                    }
                    menjoMap.put(rs.getString("SHUUGAKU_NO"), m);
                }

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            for (final Iterator it = shinseiList.iterator(); it.hasNext();) {
                final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) it.next();
                final Map saikenJokyo = (Map) menjoMap.get(shinsei._shuugakuNo);
                if (null != saikenJokyo) {
                    shinsei._menjoTotalGk = (String) saikenJokyo.get("MENJO_TOTAL_GK");
                    shinsei._menjoJiyu = (String) saikenJokyo.get("MENJO_JIYU_NAME");
                }
            }
        }


        private static void setFunoari(final DB2UDB db2, final Param param, final Collection shinseiList) {
            final Set shuugakuNos = new HashSet();
            for (final Iterator it = shinseiList.iterator(); it.hasNext();) {
                final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) it.next();
                shuugakuNos.add(shinsei._shuugakuNo);
            }

            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map choteiMap = new HashMap();
            try {
                final String sql = sqlFunoari(shuugakuNos);
                log.debug(" funo sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                ResultSetMetaData meta = rs.getMetaData();
                while (rs.next()) {
                    final Map m = new HashMap();
                    for (int i = 1; i <= meta.getColumnCount(); i++) {
                        final String columnName = meta.getColumnLabel(i);
                        m.put(columnName, rs.getString(columnName));
                    }
                    choteiMap.put(rs.getString("SHUUGAKU_NO"), m);
                }

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            for (final Iterator it = shinseiList.iterator(); it.hasNext();) {
                final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) it.next();
                final Map m = (Map) choteiMap.get(shinsei._shuugakuNo);
                if (null != m && NumberUtils.isDigits((String) m.get("COUNT")) && Integer.parseInt((String) m.get("COUNT")) > 0) {
                    shinsei._funoAri = "不納欠損あり";
                }
            }
        }

        private static String sqlFunoari(final Collection keys) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SHUUGAKU_NO, ");
            stb.append("     COUNT(*) AS COUNT ");
            stb.append(" FROM ");
            stb.append("     CHOTEI_DAT T1  ");
            stb.append(" WHERE ");
            stb.append("     SHUUGAKU_NO IN " + SQLUtils.whereIn(true, toArray(keys)) + " ");
            stb.append("     AND FUNO_FLG = '2' ");
            stb.append(" GROUP BY ");
            stb.append("     SHUUGAKU_NO ");
            return stb.toString();
        }

        private static void setMinoGakuMitouraiGaku(final DB2UDB db2, final Param param, final Collection shinseiList) {
            final Set shuugakuNos = new HashSet();
            for (final Iterator it = shinseiList.iterator(); it.hasNext();) {
                final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) it.next();
                shuugakuNos.add(shinsei._shuugakuNo);
            }

            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map minoMap = new HashMap();
            try {
                final String sql = sqlMinoGaku(shuugakuNos, param._loginDate);
                log.debug(" mino sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                ResultSetMetaData meta = rs.getMetaData();
                while (rs.next()) {
                    final Map m = new HashMap();
                    for (int i = 1; i <= meta.getColumnCount(); i++) {
                        final String columnName = meta.getColumnLabel(i);
                        m.put(columnName, rs.getString(columnName));
                    }
                    minoMap.put(rs.getString("SHUUGAKU_NO"), m);
                }

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            final Map mitouraiMap = new HashMap();
            try {
                final String sql = sqlMitouraiGaku(shuugakuNos, param._loginDate);
                log.debug(" mitourai sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                ResultSetMetaData meta = rs.getMetaData();
                while (rs.next()) {
                    final Map m = new HashMap();
                    for (int i = 1; i <= meta.getColumnCount(); i++) {
                        final String columnName = meta.getColumnLabel(i);
                        m.put(columnName, rs.getString(columnName));
                    }
                    mitouraiMap.put(rs.getString("SHUUGAKU_NO"), m);
                }

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            for (final Iterator it = shinseiList.iterator(); it.hasNext();) {
                final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) it.next();
                final Map mino = (Map) minoMap.get(shinsei._shuugakuNo);
                if (null != mino) {
                    shinsei._minouGk = (String) mino.get("MINOU_GK");
                    shinsei._minouCount = (String) mino.get("CNT");
                }
                final Map mitourai = (Map) mitouraiMap.get(shinsei._shuugakuNo);
                if (null != mitourai) {
                    shinsei._mitouraiGk = (String) mitourai.get("HENKAN_GK");
                    shinsei._mitouraiCount = (String) mitourai.get("CNT");
                }
            }
        }


        private static String sqlMinoGaku(final Collection keys, final String date) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SHUUGAKU_NO, ");
            stb.append("     SUM(MINOU_GK) AS MINOU_GK, ");
            stb.append("     COUNT(*) AS CNT ");
            stb.append(" FROM ");
            stb.append("     V_CHOTEI_NOUFU_ADD ");
            stb.append(" WHERE ");
            stb.append("     SHUUGAKU_NO IN " + SQLUtils.whereIn(true, toArray(keys)) + " AND ");
            stb.append("     TORIKESI_FLG = '0' AND ");
            stb.append("     MINOU_GK > 0 AND ");
            stb.append("     NOUFU_KIGEN < '" + date + "' ");
            stb.append(" GROUP BY ");
            stb.append("     SHUUGAKU_NO ");
            return stb.toString();
        }


        private static String sqlMitouraiGaku(final Collection keys, final String date) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SHUUGAKU_NO, ");
            stb.append("     SUM(HENKAN_GK) AS HENKAN_GK, ");
            stb.append("     COUNT(*) AS CNT ");
            stb.append(" FROM ");
            stb.append("     V_CHOTEI_NOUFU_ADD ");
            stb.append(" WHERE ");
            stb.append("     SHUUGAKU_NO IN " + SQLUtils.whereIn(true, toArray(keys)) + " AND ");
            stb.append("     TORIKESI_FLG = '0' AND ");
            stb.append("     ((MINOU_GK > 0 AND NOUFU_KIGEN > '" + date + "') ");
            stb.append("     OR ");
            stb.append("     CHOTEI_DATE IS NULL) ");
            stb.append(" GROUP BY ");
            stb.append("     SHUUGAKU_NO ");
            return stb.toString();
        }

        private static void setNoufuDat(final DB2UDB db2, final Param param, final Collection shinseiList) {
            final Set shuugakuNos = new HashSet();
            for (final Iterator it = shinseiList.iterator(); it.hasNext();) {
                final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) it.next();
                shuugakuNos.add(shinsei._shuugakuNo);
            }

            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map nooufuMap = new HashMap();
            try {
                final String sql = sqlNoufuDat(shuugakuNos);
                log.debug(" noufu sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                ResultSetMetaData meta = rs.getMetaData();
                while (rs.next()) {
                    final Map m = new HashMap();
                    for (int i = 1; i <= meta.getColumnCount(); i++) {
                        final String columnName = meta.getColumnLabel(i);
                        m.put(columnName, rs.getString(columnName));
                    }
                    nooufuMap.put(rs.getString("SHUUGAKU_NO"), m);
                }

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            for (final Iterator it = shinseiList.iterator(); it.hasNext();) {
                final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) it.next();
                final Map saikenJokyo = (Map) nooufuMap.get(shinsei._shuugakuNo);
                if (null != saikenJokyo) {
                    shinsei._noufuGk = (String) saikenJokyo.get("NOUFU_GK");
                }
            }
        }

        private static String sqlNoufuDat(final Collection keys) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SHUUGAKU_NO, ");
            stb.append("     SUM(HAKKO_TOTAL_GK) AS NOUFU_GK ");
            stb.append(" FROM ");
            stb.append("     NOUFU_DAT T1  ");
            stb.append(" WHERE ");
            stb.append("     SHUUGAKU_NO IN " + SQLUtils.whereIn(true, toArray(keys)) + " ");
            stb.append(" GROUP BY ");
            stb.append("     SHUUGAKU_NO ");
            return stb.toString();
        }

        private static String sqlSaikenJokyo(final Collection keys) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.*, ");
            stb.append("     NMT016.NAME1 AS HENKAN_HOHO_NAME, ");
            stb.append("     NMT038.NAME1 AS SHIHARAININ_KBN_NAME, ");
            stb.append("     NMT039.NAME1 AS SHIHARAI_HOHO_NAME ");
            stb.append(" FROM ");
            stb.append("     V_SAIKEN_JOKYO T1  ");
            stb.append("     LEFT JOIN NAME_MST NMT016 ON NMT016.NAMECD1 = 'T016' ");
            stb.append("         AND NMT016.NAMECD2 = T1.HENKAN_HOHO ");
            stb.append("     LEFT JOIN NAME_MST NMT038 ON NMT038.NAMECD1 = 'T038' ");
            stb.append("         AND NMT038.NAMECD2 = T1.SHIHARAININ_KBN ");
            stb.append("     LEFT JOIN NAME_MST NMT039 ON NMT039.NAMECD1 = 'T039' ");
            stb.append("         AND NMT039.NAMECD2 = T1.SHIHARAI_HOHO ");
            stb.append(" WHERE ");
            stb.append("     SHUUGAKU_NO IN " + SQLUtils.whereIn(true, toArray(keys)) + " ");
            return stb.toString();
        }

        private static void setSaikenJokyo(final DB2UDB db2, final Param param, final Collection shinseiList) {
            final Set shuugakuNos = new HashSet();
            for (final Iterator it = shinseiList.iterator(); it.hasNext();) {
                final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) it.next();
                shuugakuNos.add(shinsei._shuugakuNo);
            }

            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map saikenJokyoMap = new HashMap();
            try {
                final String sql = sqlSaikenJokyo(shuugakuNos);
                log.debug(" saiken jokyo sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                ResultSetMetaData meta = rs.getMetaData();
                while (rs.next()) {
                    final Map m = new HashMap();
                    for (int i = 1; i <= meta.getColumnCount(); i++) {
                        final String columnName = meta.getColumnLabel(i);
                        m.put(columnName, rs.getString(columnName));
                    }
                    saikenJokyoMap.put(rs.getString("SHUUGAKU_NO"), m);
                }

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            for (final Iterator it = shinseiList.iterator(); it.hasNext();) {
                final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) it.next();
                final Map saikenJokyo = (Map) saikenJokyoMap.get(shinsei._shuugakuNo);
                if (null != saikenJokyo) {
                    shinsei._saikenJokyoHenkanGk = (String) saikenJokyo.get("HENKAN_TOTAL_GK");
                    shinsei._saikenJokyoHenkanKaisuu = (String) saikenJokyo.get("COUNT_KEIKAKU");
                    shinsei._saikenJokyoSHenkanYm = (String) saikenJokyo.get("FIRST_CHOTEI_YM");
                    shinsei._saikenJokyoEHenkanYm = ymAddMonth((String) saikenJokyo.get("FIRST_CHOTEI_YM"), (String) saikenJokyo.get("HENKAN_KIKAN"));

                    shinsei._saikenJokyoFirstHenkanGk = (String) saikenJokyo.get("FIRST_HENKAN_GK");
                    shinsei._saikenJokyoLastHenkanGk = (String) saikenJokyo.get("LAST_HENKAN_GK");

                    shinsei._saikenJokyoHenkanHouhouName = (String) saikenJokyo.get("HENKAN_HOHO_NAME");
                    shinsei._henkanShiharaininKbnName = (String) saikenJokyo.get("SHIHARAININ_KBN_NAME");
                    shinsei._henkanShiharaiHohoName = (String) saikenJokyo.get("SHIHARAI_HOHO_NAME");

                    shinsei._saikenJokyoGakuShuno = (String) saikenJokyo.get("GAKU_SHUNO");
                }
            }
        }

        private static void setFlgComment(final DB2UDB db2, final Param param, final Collection shinseiList) {
            final Set kojinNos = new HashSet();
            for (final Iterator it = shinseiList.iterator(); it.hasNext();) {
                final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) it.next();
                kojinNos.add(shinsei._kojinNo);
            }
            final Set shuugakuNos = new HashSet();
            for (final Iterator it = shinseiList.iterator(); it.hasNext();) {
                final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) it.next();
                shuugakuNos.add(shinsei._shuugakuNo);
            }

            PreparedStatement ps = null;
            ResultSet rs = null;

            final Map kojinJikoMap = new HashMap();
            final Map saikenMap = new HashMap();
            try {
                final StringBuffer sqlJiko = new StringBuffer();
                sqlJiko.append(" SELECT ");
                sqlJiko.append("     NAME1, ");
                sqlJiko.append("     INPUT_DATE ");
                sqlJiko.append(" FROM ");
                sqlJiko.append("     KOJIN_JIKO_DAT T1 ");
                sqlJiko.append("     INNER JOIN NAME_MST T2 ON T1.JIKO_CODE = T2.NAMECD2 AND T2.NAMECD1 = 'T046' ");
                sqlJiko.append(" WHERE ");
                sqlJiko.append("     KOJIN_NO = ? ");
                sqlJiko.append(" ORDER BY ");
                sqlJiko.append("     INPUT_DATE DESC ");
                log.debug(" jikoDat sql = " + sqlJiko);
                ps = db2.prepareStatement(sqlJiko.toString());
                for (final Iterator it = kojinNos.iterator(); it.hasNext();) {
                    final String kojinNo = (String) it.next();
                    ps.setString(1, kojinNo);
                    rs = ps.executeQuery();

                    final List list = new ArrayList();
                    while (rs.next()) {
                        list.add(rs.getString("NAME1"));
                    }
                    kojinJikoMap.put(kojinNo, list);
                }

                final StringBuffer sqlSaiken = new StringBuffer();
                sqlSaiken.append(" SELECT ");
                sqlSaiken.append("     NAME1 ");
                sqlSaiken.append(" FROM ");
                sqlSaiken.append("     SAIKEN_DAT T1 ");
                sqlSaiken.append("     INNER JOIN NAME_MST T2 ON T1.CHUUI_FLG = T2.NAMECD2 AND T2.NAMECD1 = 'T047' ");
                sqlSaiken.append(" WHERE ");
                sqlSaiken.append("     SHUUGAKU_NO = ? ");
                log.debug(" sqikenDat sql = " + sqlSaiken);
                ps = db2.prepareStatement(sqlSaiken.toString());
                for (final Iterator it = shuugakuNos.iterator(); it.hasNext();) {
                    final String shuugakuNo = (String) it.next();
                    ps.setString(1, shuugakuNo);
                    rs = ps.executeQuery();

                    final List list = new ArrayList();
                    while (rs.next()) {
                        list.add(rs.getString("NAME1"));
                    }
                    saikenMap.put(shuugakuNo, list);
                }

            } catch (Exception ex) {
                log.fatal("exception! ex2", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            for (final Iterator it = shinseiList.iterator(); it.hasNext();) {
                final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) it.next();
                final List jikoList = (List) kojinJikoMap.get(shinsei._kojinNo);
                if (null != jikoList) {
                    shinsei._jikoFlgCommentList = jikoList;
                }
                final List saikenList = (List) saikenMap.get(shinsei._shuugakuNo);
                if (null != saikenList) {
                    shinsei._happuDomeFlgCommentList = saikenList;
                }
            }
        }

        private static String ymAddMonth(final String ym, final String months) {
            if (null == ym || ym.length() < 7 || !NumberUtils.isDigits(ym.substring(0, 4)) || !NumberUtils.isDigits(ym.substring(5, 7))) {
                return null;
            }
            if (null == months || !NumberUtils.isNumber(months) || Integer.parseInt(months) <= 1) {
                return ym;
            }
            int year = Integer.parseInt(ym.substring(0, 4));
            int month = Integer.parseInt(ym.substring(5, 7)) + Integer.parseInt(months) - 1;
            while (month > 12) {
                month -= 12;
                year += 1;
            }
            while (month <= 0) {
                month += 12;
                year -= 1;
            }
            final DecimalFormat df = new DecimalFormat("00");
            final String rtn = year + "-" + df.format(month);
//            log.debug(" rtn = " + rtn);
            return rtn;
        }

        private static void setChoteiNoufuAdd(final DB2UDB db2, final Param param, final Collection shinseiList) {
            final Set shuugakuNos = new HashSet();
            for (final Iterator it = shinseiList.iterator(); it.hasNext();) {
                final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) it.next();
                shuugakuNos.add(shinsei._shuugakuNo);
            }
            final Map m = ChoteiNoufuAdd.load(db2, shuugakuNos, param);
            for (final Iterator it = shinseiList.iterator(); it.hasNext();) {
                final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) it.next();
                final List choteiList = (List) m.get(shinsei._shuugakuNo);
                if (null != choteiList) {
                    shinsei._choteiList = choteiList;
                }
            }
        }
    }

    private static class FurikaeKouzaDat {
        final String _shuugakuNo;
        final String _kojinNo;
        final String _shiharaininKbn;
        final String _shiharaiHoho;
        final String _shikinShousaiDiv;
        final String _abbv3;
        final String _bankcd;
        final String _branchcd;
        final String _yokinDiv;
        final String _accountNo;
        final String _bankMeigiSeiKana;
        final String _bankMeigiMeiKana;
        final String _bankname;
        final String _branchname;
        final String _banknameKana;
        final String _branchnameKana;
        final String _yokinDivName;

        FurikaeKouzaDat(
                final String shuugakuNo,
                final String kojinNo,
                final String shiharaininKbn,
                final String shiharaiHoho,
                final String shikinShousaiDiv,
                final String abbv3,
                final String bankcd,
                final String branchcd,
                final String yokinDiv,
                final String accountNo,
                final String bankMeigiSeiKana,
                final String bankMeigiMeiKana,
                final String bankname,
                final String branchname,
                final String banknameKana,
                final String branchnameKana,
                final String yokinDivName
        ) {
            _shuugakuNo = shuugakuNo;
            _kojinNo = kojinNo;
            _shiharaininKbn = shiharaininKbn;
            _shiharaiHoho = shiharaiHoho;
            _shikinShousaiDiv = shikinShousaiDiv;
            _abbv3 = abbv3;
            _bankcd = bankcd;
            _branchcd = branchcd;
            _yokinDiv = yokinDiv;
            _accountNo = accountNo;
            _bankMeigiSeiKana = bankMeigiSeiKana;
            _bankMeigiMeiKana = bankMeigiMeiKana;
            _bankname = bankname;
            _branchname = branchname;
            _banknameKana = banknameKana;
            _branchnameKana = branchnameKana;
            _yokinDivName = yokinDivName;
        }

        public String getMeigiKana() {
            return StringUtils.defaultString(_bankMeigiSeiKana) + " " +  StringUtils.defaultString(_bankMeigiMeiKana);
        }

        public static Map load(final DB2UDB db2, final Collection shuugakuNos) {
            Map shuugakufurikaekouzadatMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(shuugakuNos);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String shuugakuNo = rs.getString("SHUUGAKU_NO");
                    final String kojinNo = rs.getString("KOJIN_NO");
                    final String shiharaininKbn = rs.getString("SHIHARAININ_KBN");
                    final String shiharaiHoho = rs.getString("SHIHARAI_HOHO");
                    final String shikinShousaiDiv = rs.getString("SHIKIN_SHOUSAI_DIV");
                    final String abbv3 = rs.getString("ABBV3");
                    final String bankcd = rs.getString("BANKCD");
                    final String branchcd = rs.getString("BRANCHCD");
                    final String yokinDiv = rs.getString("YOKIN_DIV");
                    final String accountNo = rs.getString("ACCOUNT_NO");
                    final String bankMeigiSeiKana = rs.getString("BANK_MEIGI_SEI_KANA");
                    final String bankMeigiMeiKana = rs.getString("BANK_MEIGI_MEI_KANA");
                    final String bankname = rs.getString("BANKNAME");
                    final String branchname = rs.getString("BRANCHNAME");
                    final String banknameKana = rs.getString("BANKNAME_KANA");
                    final String branchnameKana = rs.getString("BRANCHNAME_KANA");
                    final String yokinDivName = rs.getString("YOKIN_DIV_NAME");
                    final FurikaeKouzaDat furikaekouzadat = new FurikaeKouzaDat(shuugakuNo, kojinNo, shiharaininKbn, shiharaiHoho, shikinShousaiDiv, abbv3, bankcd, branchcd, yokinDiv, accountNo, bankMeigiSeiKana, bankMeigiMeiKana, bankname, branchname, banknameKana, branchnameKana, yokinDivName);
                    shuugakufurikaekouzadatMap.put(shuugakuNo, furikaekouzadat);
                 }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return shuugakufurikaekouzadatMap;
        }

        public static String sql(final Collection shuugakuNos) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT  ");
            stb.append("     T1.*,  ");
            stb.append("     T3.NAME1 AS YOKIN_DIV_NAME  ");
            stb.append(" FROM  ");
            stb.append("     V_FURIKAE_KOUZA_NEWEST T1  ");
            stb.append(" LEFT JOIN NAME_MST T3 ON T3.NAMECD1 = 'T032' AND T3.NAMECD2 = T1.YOKIN_DIV  ");
            stb.append(" WHERE SHUUGAKU_NO IN " + SQLUtils.whereIn(true, toArray(shuugakuNos)) + " ");
            return stb.toString();
        }
    }


    private static class ShinkenshaHistDat {

        final String _shinkenCd;
        final String _issuedate;
        final String _familyName;
        final String _firstName;
        final String _familyNameKana;
        final String _firstNameKana;
        final String _birthday;
        final String _shinseiNenrei;
        final String _zipcd;
        final String _citycd;
        final String _addr1;
        final String _addr2;
        final String _telno1;
        final String _telno2;
        final String _remark;

        ShinkenshaHistDat(
                final String shinkenCd,
                final String issuedate,
                final String familyName,
                final String firstName,
                final String familyNameKana,
                final String firstNameKana,
                final String birthday,
                final String shinseiNenrei,
                final String zipcd,
                final String citycd,
                final String addr1,
                final String addr2,
                final String telno1,
                final String telno2,
                final String remark
        ) {
            _shinkenCd = shinkenCd;
            _issuedate = issuedate;
            _familyName = familyName;
            _firstName = firstName;
            _familyNameKana = familyNameKana;
            _firstNameKana = firstNameKana;
            _birthday = birthday;
            _shinseiNenrei = shinseiNenrei;
            _zipcd = zipcd;
            _citycd = citycd;
            _addr1 = addr1;
            _addr2 = addr2;
            _telno1 = telno1;
            _telno2 = telno2;
            _remark = remark;
        }

        public String getName() {
            return StringUtils.defaultString(_familyName) + "　" +  StringUtils.defaultString(_firstName);
        }

        public String getKana() {
            return StringUtils.defaultString(_familyNameKana) + "　" +  StringUtils.defaultString(_firstNameKana);
        }

        public static Map load(final DB2UDB db2, final Collection shinkenCdSet) {
            Map map = new HashMap();
            if (shinkenCdSet.isEmpty()) {
                return map;
            }
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(shinkenCdSet);
                log.debug(" shinken sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String shinkenCd = rs.getString("SHINKEN_CD");
                    final String issuedate = rs.getString("ISSUEDATE");
                    final String familyName = rs.getString("FAMILY_NAME");
                    final String firstName = rs.getString("FIRST_NAME");
                    final String familyNameKana = rs.getString("FAMILY_NAME_KANA");
                    final String firstNameKana = rs.getString("FIRST_NAME_KANA");
                    final String birthday = rs.getString("BIRTHDAY");
                    final String shinseiNenrei = rs.getString("SHINSEI_NENREI");
                    final String zipcd = rs.getString("ZIPCD");
                    final String citycd = rs.getString("CITYCD");
                    final String addr1 = rs.getString("ADDR1");
                    final String addr2 = rs.getString("ADDR2");
                    final String telno1 = rs.getString("TELNO1");
                    final String telno2 = rs.getString("TELNO2");
                    final String remark = rs.getString("REMARK");
                    ShinkenshaHistDat shinkensha = new ShinkenshaHistDat(shinkenCd, issuedate,familyName, firstName, familyNameKana, firstNameKana,
                            birthday, shinseiNenrei, zipcd, citycd, addr1, addr2, telno1, telno2, remark);
                    map.put(shinkenCd, shinkensha);
                }
            } catch (Exception ex) {
                 log.fatal("exception!", ex);
            } finally {
                 DbUtils.closeQuietly(null, ps, rs);
                 db2.commit();
            }
            return map;
        }

        public static String sql(final Collection shinkenCdSet) {
            final StringBuffer stb = new StringBuffer();
//            stb.append(" WITH MAX_DATE AS ( ");
//            stb.append("   SELECT SHINKEN_CD, MAX(ISSUEDATE) AS ISSUEDATE ");
//            stb.append("   FROM SHINKENSHA_HIST_DAT ");
//            stb.append("   WHERE SHINKEN_CD IN " + SQLUtils.whereIn(true, toArray(shinkenCdSet)) + " ");
//            stb.append("   GROUP BY SHINKEN_CD ");
//            stb.append(" ) ");
            stb.append(" SELECT * ");
            stb.append(" FROM SHINKENSHA_HIST_DAT T1 ");
            stb.append("   WHERE SHINKEN_CD IN " + SQLUtils.whereIn(true, toArray(shinkenCdSet)) + " ");
//            stb.append(" INNER JOIN MAX_DATE T2 ON T2.SHINKEN_CD = T1.SHINKEN_CD AND T2.ISSUEDATE = T1.ISSUEDATE ");
            return stb.toString();
        }
    }

    private static class ChoteiNoufuAdd {
        final String _shuugakuNo;
        final String _choteiKaisu;
        final String _choteiYm;
        final String _torikesiFlg;
        final String _choteiDate;
        final String _shiharaiHohoCd;
        final String _noufuKigen;
        final String _henkanGk;
        final String _choteiNo;
        final String _taiyoNend;
        final String _choteiNend;
        final String _kaikeiNend;
        final String _bunkatuFlg;
        final String _tokusokuDate;
        final String _tokusokuKigen;
        final String _tokusokuHoryuFlg;
        final String _saikokuDate;
        final String _saikokuKaisu;
        final String _funoFlg;
        final String _funoDate;
        final String _funoKianDate;
        final String _funoKeteiDate;
        final String _haitoFlg;
        final String _shunoDate;
        final String _keisanDate;
        final String _shunoTotalGk;
        final String _shunoKokkoGk;
        final String _shunoTanpiGk;
        final String _shunoKoufuGk;
        final String _hakkoTotalGk;
        final String _mihakkoGk;
        final String _minouGk;
        final String _reprintDate;
        final String _printKaisu;
        final String _sumHenkanGk;

        ChoteiNoufuAdd(
                final String shuugakuNo,
                final String choteiKaisu,
                final String choteiYm,
                final String torikesiFlg,
                final String choteiDate,
                final String shiharaiHohoCd,
                final String noufuKigen,
                final String henkanGk,
                final String choteiNo,
                final String taiyoNend,
                final String choteiNend,
                final String kaikeiNend,
                final String bunkatuFlg,
                final String tokusokuDate,
                final String tokusokuKigen,
                final String tokusokuHoryuFlg,
                final String saikokuDate,
                final String saikokuKaisu,
                final String funoFlg,
                final String funoDate,
                final String funoKianDate,
                final String funoKeteiDate,
                final String haitoFlg,
                final String shunoDate,
                final String keisanDate,
                final String shunoTotalGk,
                final String shunoKokkoGk,
                final String shunoTanpiGk,
                final String shunoKoufuGk,
                final String hakkoTotalGk,
                final String mihakkoGk,
                final String minouGk,
                final String reprintDate,
                final String printKaisu,
                final String sumHenkanGk
        ) {
            _shuugakuNo = shuugakuNo;
            _choteiKaisu = choteiKaisu;
            _choteiYm = choteiYm;
            _torikesiFlg = torikesiFlg;
            _choteiDate = choteiDate;
            _shiharaiHohoCd = shiharaiHohoCd;
            _noufuKigen = noufuKigen;
            _henkanGk = henkanGk;
            _choteiNo = choteiNo;
            _taiyoNend = taiyoNend;
            _choteiNend = choteiNend;
            _kaikeiNend = kaikeiNend;
            _bunkatuFlg = bunkatuFlg;
            _tokusokuDate = tokusokuDate;
            _tokusokuKigen = tokusokuKigen;
            _tokusokuHoryuFlg = tokusokuHoryuFlg;
            _saikokuDate = saikokuDate;
            _saikokuKaisu = saikokuKaisu;
            _funoFlg = funoFlg;
            _funoDate = funoDate;
            _funoKianDate = funoKianDate;
            _funoKeteiDate = funoKeteiDate;
            _haitoFlg = haitoFlg;
            _shunoDate = shunoDate;
            _keisanDate = keisanDate;
            _shunoTotalGk = shunoTotalGk;
            _shunoKokkoGk = shunoKokkoGk;
            _shunoTanpiGk = shunoTanpiGk;
            _shunoKoufuGk = shunoKoufuGk;
            _hakkoTotalGk = hakkoTotalGk;
            _mihakkoGk = mihakkoGk;
            _minouGk = minouGk;
            _reprintDate = reprintDate;
            _printKaisu = printKaisu;
            _sumHenkanGk = sumHenkanGk;
        }

        public static Map load(final DB2UDB db2, final Collection shuugakunos, final Param param) {
            final Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(shuugakunos);
                log.debug(" chotei noufu sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String shuugakuNo = rs.getString("SHUUGAKU_NO");
                    final String choteiKaisu = rs.getString("CHOTEI_KAISU");
                    final String choteiYm = rs.getString("CHOTEI_YM");
                    final String torikesiFlg = rs.getString("TORIKESI_FLG");
                    final String choteiDate = rs.getString("CHOTEI_DATE");
                    final String shiharaiHohoCd = rs.getString("SHIHARAI_HOHO_CD");
                    final String noufuKigen = rs.getString("NOUFU_KIGEN");
                    final String henkanGk = rs.getString("HENKAN_GK");
                    final String choteiNo = rs.getString("CHOTEI_NO");
                    final String taiyoNend = rs.getString("TAIYO_NEND");
                    final String choteiNend = rs.getString("CHOTEI_NEND");
                    final String kaikeiNend = rs.getString("KAIKEI_NEND");
                    final String bunkatuFlg = rs.getString("BUNKATU_FLG");
                    final String tokusokuDate = rs.getString("TOKUSOKU_DATE");
                    final String tokusokuKigen = rs.getString("TOKUSOKU_KIGEN");
                    final String tokusokuHoryuFlg = rs.getString("TOKUSOKU_HORYU_FLG");
                    final String saikokuDate = rs.getString("SAIKOKU_DATE");
                    final String saikokuKaisu = rs.getString("SAIKOKU_KAISU");
                    final String funoFlg = rs.getString("FUNO_FLG");
                    final String funoDate = rs.getString("FUNO_DATE");
                    final String funoKianDate = rs.getString("FUNO_KIAN_DATE");
                    final String funoKeteiDate = rs.getString("FUNO_KETEI_DATE");
                    final String haitoFlg = rs.getString("HAITO_FLG");
                    final String shunoDate = rs.getString("SHUNO_DATE");
                    final String keisanDate = rs.getString("KEISAN_DATE");
                    final String shunoTotalGk = rs.getString("SHUNO_TOTAL_GK");
                    final String shunoKokkoGk = rs.getString("SHUNO_KOKKO_GK");
                    final String shunoTanpiGk = rs.getString("SHUNO_TANPI_GK");
                    final String shunoKoufuGk = rs.getString("SHUNO_KOUFU_GK");
                    final String hakkoTotalGk = rs.getString("HAKKO_TOTAL_GK");
                    final String mihakkoGk = rs.getString("MIHAKKO_GK");
                    final String minouGk = rs.getString("MINOU_GK");
                    final String reprintDate = rs.getString("REPRINT_DATE");
                    final String printKaisu = rs.getString("PRINT_KAISU");
                    final String sumHenkanGk = rs.getString("SUM_HENKAN_GK");
                    final ChoteiNoufuAdd choteinoufuadd = new ChoteiNoufuAdd(shuugakuNo, choteiKaisu, choteiYm, torikesiFlg, choteiDate, shiharaiHohoCd, noufuKigen, henkanGk, choteiNo, taiyoNend, choteiNend, kaikeiNend, bunkatuFlg, tokusokuDate, tokusokuKigen, tokusokuHoryuFlg, saikokuDate, saikokuKaisu, funoFlg, funoDate, funoKianDate, funoKeteiDate, haitoFlg, shunoDate, keisanDate, shunoTotalGk, shunoKokkoGk, shunoTanpiGk, shunoKoufuGk, hakkoTotalGk, mihakkoGk, minouGk, reprintDate, printKaisu, sumHenkanGk);
                    if (null == rtn.get(shuugakuNo)) {
                        rtn.put(shuugakuNo, new ArrayList());
                    }
                    List choteiList = (List) rtn.get(shuugakuNo);
                    choteiList.add(choteinoufuadd);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        public static String sql(final Collection shuugakunos) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.*, ");
            stb.append("     T2.SUM_HENKAN_GK ");
            stb.append(" FROM ");
            stb.append("     V_CHOTEI_NOUFU_ADD T1 ");
            stb.append("     INNER JOIN (SELECT SHUUGAKU_NO, SUM(HENKAN_GK) AS SUM_HENKAN_GK ");
            stb.append("                 FROM V_CHOTEI_NOUFU_ADD ");
            stb.append("                 WHERE ");
            stb.append("                 SHUUGAKU_NO IN " + SQLUtils.whereIn(true, toArray(shuugakunos))  + " ");
            stb.append("                 GROUP BY SHUUGAKU_NO ");
            stb.append("      ) T2 ON T2.SHUUGAKU_NO = T1.SHUUGAKU_NO ");
            stb.append(" WHERE ");
            stb.append("     T1.SHUUGAKU_NO IN " + SQLUtils.whereIn(true, toArray(shuugakunos))  + " ");
            stb.append(" ORDER BY ");
            stb.append("     T1.SHUUGAKU_NO, CHOTEI_KAISU ");
            return stb.toString();
        }
    }


    private static int getMS932Length(final String s) {
        return KNJ_EditEdit.getMS932ByteLength(s);
    }

    private static String[] toArray(final Collection col) {
        final String[] array = new String[col.size()];
        int i = 0;
        for (final Iterator it = col.iterator(); it.hasNext(); i++) {
            array[i] = (String) it.next();
        }
        return array;
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 67181 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _loginDate;
        private final String _kojinNo;
        private final String _prgid;
        private final String _shuugakuNoDiv;
        private final String[] _shuugakuNoList;
        private final ShugakuDate _shugakuDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _loginDate = getCtrlDate(db2);
            _prgid = request.getParameter("PRGID");
            if (PRGID_KNJTP052.equals(_prgid)) {
                _kojinNo = null;
                _shuugakuNoDiv = request.getParameter("SHUUGAKU_NO_DIV");
                if ("1".equals(_shuugakuNoDiv)) {
                    _shuugakuNoList = new String[] {request.getParameter("SHUUGAKU_NO")};
                } else { // if ("2".equals(_shuugakuNoDiv)) {
                    _shuugakuNoList = StringUtils.split(request.getParameter("SHUUGAKU_NO_LIST"), ",");
                }
            } else if (PRGID_KNJTE101.equals(_prgid)) {
                _kojinNo = null;
                _shuugakuNoDiv = null;
                final List list = new ArrayList();
                for (final Enumeration enums = request.getParameterNames(); enums.hasMoreElements();) {
                    final String name = (String) enums.nextElement();
                    if (!name.startsWith("CHK") || name.length() <= 3 || !StringUtils.isNumeric(name.substring(3)) || !"1".equals(request.getParameter(name))) {
                        continue;
                    }
                    list.add(request.getParameter("SNO" + name.substring(3)));
                }
                _shuugakuNoList = toArray(list);
            } else {
                _kojinNo = request.getParameter("KOJIN_NO");
                _shuugakuNoDiv = null;
                _shuugakuNoList = null;
            }
            _shugakuDate = new ShugakuDate(db2);
        }

        private String getCtrlDate(final DB2UDB db2) {
            String rtn = null;
            final String sql = " select ctrl_date from control_mst where ctrl_no = '01' ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    rtn = rs.getString("ctrl_date");
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

    }
}

// eof

