// kanji=漢字
/*
 * 作成日: 2014/08/15 9:35:11 - JST
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
import java.util.List;
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
 * <<クラスの説明>>。
 * @author m-yama
 */
public class KNJTH024 {

    private static final Log log = LogFactory.getLog("KNJTH024.class");

    public static final String KETTEI = "1";
    public static final String TORIKESI = "2";
    public static final String HENKOU = "3";

    public static final String SHORI_KETTEI = "1";
    public static final String SHORI_HENKOU = "2";

    public static final String KOJIN = "1";
    public static final String SCHOOL = "2";

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        String printSql = "";
        if (KETTEI.equals(_param._form)) {
            printSql = getKettei();
        } else if (TORIKESI.equals(_param._form)) {
            printSql = getTorikesi();
        } else {
            printSql = getHenkou();
        }
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(printSql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String form = (KETTEI.equals(_param._form)) ? "KNJTH024_1_T.frm" : "KNJTH024_" + _param._form + ".frm";
                svf.VrSetForm(form, 1);
                svf.VrsOut("ZIP_NO", rs.getString("ZIPCD"));
                printAddress(svf, rs.getString("ADDR1"), rs.getString("ADDR2"));
                svf.VrsOut("NAME", rs.getString("HOGO_NAME"));

                svf.VrsOut("CERT_NO", getBunshoBangou(rs, db2)); // 証明書番号
                final String title;
                if (KETTEI.equals(_param._form)) {
                    title = "京都府奨学のための給付金支給決定通知書";
                } else if (TORIKESI.equals(_param._form)) {
                    title = "京都府奨学のための給付金支給取消決定通知書";
                } else {
                    title = "京都府奨学のための給付金支給変更決定通知書";
                }
                svf.VrsOut("DATE", String.valueOf(KNJ_EditDate.h_format_JP(db2, rs.getString("PUT_DATE"))));

                svf.VrsOut("GOVERNER", _param._chijiName); // 知事名

//                svf.VrsOut("NENDO", KNJ_EditDate.h_format_JP_N(rs.getString("PUT_DATE")) + "度" + title);
                svf.VrsOut("NENDO", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._shinseiYear)) + "年度" + title);

                //決定
                svf.VrsOut("PRICE", rs.getString("KYUHU_KETTEI_GK"));
                svf.VrsOut("PRICE_2_1", rs.getString("KYUHU_GK")); //内訳 通信費以外
                svf.VrsOut("PRICE_2_2", rs.getString("TSUSHINHI_GK")); //内訳 通信費
                svf.VrsOut("PRICE_2_3", rs.getString("TSUIKA_KYUHU_GK")); //内訳 追加給付

                //取消
                svf.VrsOut("REASON", rs.getString("REMARK"));

                //変更
                svf.VrsOut("PRICE1", rs.getString("KYUHU_KETTEI_GK"));
                svf.VrsOut("PRICE2", rs.getString("HENKOU_MAE_GK"));

                final String schoolNamefield = KNJ_EditEdit.getMS932ByteLength(rs.getString("SCHOOL_NAME")) > 30 ? "_2" : "";
                final String studentNamefield = KNJ_EditEdit.getMS932ByteLength(rs.getString("KOJIN_NAME")) > 30 ? "_2" : "";
                svf.VrsOut("SCHOOL_NAME"+schoolNamefield, rs.getString("SCHOOL_NAME"));
                svf.VrsOut("STUDENT_NAME"+studentNamefield, rs.getString("KOJIN_NAME"));
                _hasData = true;
                svf.VrEndPage();

                if (KETTEI.equals(_param._form)) {
                    svf.VrSetForm("KNJTH024_" + _param._form + "_2.frm", 1);
                    final String grade = NumberUtils.isDigits(rs.getString("GRADE")) ? String.valueOf(Integer.parseInt(rs.getString("GRADE"))) : StringUtils.defaultString(rs.getString("GRADE"));
                    final String hrClass = NumberUtils.isDigits(rs.getString("HR_CLASS")) ? String.valueOf(Integer.parseInt(rs.getString("HR_CLASS"))) : StringUtils.defaultString(rs.getString("HR_CLASS"));
                    final String attendno = NumberUtils.isDigits(rs.getString("ATTENDNO")) ? String.valueOf(Integer.parseInt(rs.getString("ATTENDNO"))) : StringUtils.defaultString(rs.getString("ATTENDNO"));
                    final String hrName = grade + "年 " + hrClass + "組 " + attendno + "番";
                    svf.VrsOut("HR_NAME", hrName);
                    svf.VrsOut("STUDENT_NAME", StringUtils.defaultString(rs.getString("KOJIN_NAME")) + "　様");
                    svf.VrEndPage();
                }
            }
        } catch (final Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private String getKettei() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH MAX_KETTEI AS ( ");
        stb.append(" SELECT ");
        stb.append("     KOJIN_NO, ");
        stb.append("     SHINSEI_YEAR, ");
        stb.append("     SEQ, ");
        stb.append("     MAX(KETTEI_DATE) AS KETTEI_DATE ");
        stb.append(" FROM ");
        stb.append("     KOJIN_KYUHU_GK_KETTEI_HIST_DAT ");
        stb.append(" WHERE ");
        stb.append("     SHINSEI_YEAR = '" + _param._shinseiYear + "' ");
        stb.append("     AND SHORI_DIV = '" + SHORI_KETTEI + "' ");
        stb.append(" GROUP BY ");
        stb.append("     KOJIN_NO, ");
        stb.append("     SHINSEI_YEAR, ");
        stb.append("     SEQ ");
        stb.append(" ), MAIN_KETTEI AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.* ");
        stb.append(" FROM ");
        stb.append("     KOJIN_KYUHU_GK_KETTEI_HIST_DAT T1, ");
        stb.append("     MAX_KETTEI T2 ");
        stb.append(" WHERE ");
        stb.append("     T1.KOJIN_NO = T2.KOJIN_NO ");
        stb.append("     AND T1.SHINSEI_YEAR = T2.SHINSEI_YEAR ");
        stb.append("     AND T1.KETTEI_DATE = T2.KETTEI_DATE ");
        stb.append("     AND T1.SEQ = T2.SEQ ");
        //通信費
        stb.append(" ) , MAX_KETTEI_TSUSHINHI AS ( ");
        stb.append(" SELECT ");
        stb.append("     KOJIN_NO, ");
        stb.append("     SHINSEI_YEAR, ");
        stb.append("     SEQ, ");
        stb.append("     MAX(KETTEI_DATE) AS KETTEI_DATE ");
        stb.append(" FROM ");
        stb.append("     KOJIN_TSUSHINHI_GK_KETTEI_HIST_DAT ");
        stb.append(" WHERE ");
        stb.append("     SHINSEI_YEAR = '" + _param._shinseiYear + "' ");
        stb.append("     AND SHORI_DIV = '" + SHORI_KETTEI + "' ");
        stb.append(" GROUP BY ");
        stb.append("     KOJIN_NO, ");
        stb.append("     SHINSEI_YEAR, ");
        stb.append("     SEQ ");
        stb.append(" ), MAIN_KETTEI_TSUSHINHI AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.* ");
        stb.append(" FROM ");
        stb.append("     KOJIN_TSUSHINHI_GK_KETTEI_HIST_DAT T1, ");
        stb.append("     MAX_KETTEI_TSUSHINHI T2 ");
        stb.append(" WHERE ");
        stb.append("     T1.KOJIN_NO = T2.KOJIN_NO ");
        stb.append("     AND T1.SHINSEI_YEAR = T2.SHINSEI_YEAR ");
        stb.append("     AND T1.KETTEI_DATE = T2.KETTEI_DATE ");
        stb.append("     AND T1.SEQ = T2.SEQ ");
        //追加給付
        stb.append(" ) , MAX_KETTEI_TSUIKA_KYUHU AS ( ");
        stb.append(" SELECT ");
        stb.append("     KOJIN_NO, ");
        stb.append("     SHINSEI_YEAR, ");
        stb.append("     SEQ, ");
        stb.append("     MAX(KETTEI_DATE) AS KETTEI_DATE ");
        stb.append(" FROM ");
        stb.append("     KOJIN_TSUIKA_KYUHU_GK_KETTEI_HIST_DAT ");
        stb.append(" WHERE ");
        stb.append("     SHINSEI_YEAR = '" + _param._shinseiYear + "' ");
        stb.append("     AND SHORI_DIV = '" + SHORI_KETTEI + "' ");
        stb.append(" GROUP BY ");
        stb.append("     KOJIN_NO, ");
        stb.append("     SHINSEI_YEAR, ");
        stb.append("     SEQ ");
        stb.append(" ), MAIN_KETTEI_TSUIKA_KYUHU AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.* ");
        stb.append(" FROM ");
        stb.append("     KOJIN_TSUIKA_KYUHU_GK_KETTEI_HIST_DAT T1, ");
        stb.append("     MAX_KETTEI_TSUIKA_KYUHU T2 ");
        stb.append(" WHERE ");
        stb.append("     T1.KOJIN_NO = T2.KOJIN_NO ");
        stb.append("     AND T1.SHINSEI_YEAR = T2.SHINSEI_YEAR ");
        stb.append("     AND T1.KETTEI_DATE = T2.KETTEI_DATE ");
        stb.append("     AND T1.SEQ = T2.SEQ ");
        stb.append(" ) ");
        //メイン
        stb.append(" SELECT ");
        stb.append("     T1.SHUUGAKU_NO, ");
        if (KOJIN.equals(_param._classDiv)) {
            stb.append("     T1.UKE_YEAR, ");
            stb.append("     T1.UKE_NO, ");
            stb.append("     T1.UKE_EDABAN, ");
        } else {
            stb.append("     CASE WHEN K_TSUSHINHI.UKE_YEAR || '-' || K_TSUSHINHI.UKE_NO || '-' ||  K_TSUSHINHI.UKE_EDABAN = '" + _param._uke + "' THEN K_TSUSHINHI.UKE_YEAR ");
            stb.append("          WHEN K_TSUIKA_KYUHU.UKE_YEAR || '-' || K_TSUIKA_KYUHU.UKE_NO || '-' ||  K_TSUIKA_KYUHU.UKE_EDABAN = '" + _param._uke + "' THEN K_TSUIKA_KYUHU.UKE_YEAR ");
            stb.append("          ELSE T1.UKE_YEAR ");
            stb.append("     END AS UKE_YEAR, ");
            stb.append("     CASE WHEN K_TSUSHINHI.UKE_YEAR || '-' || K_TSUSHINHI.UKE_NO || '-' ||  K_TSUSHINHI.UKE_EDABAN = '" + _param._uke + "' THEN K_TSUSHINHI.UKE_NO ");
            stb.append("          WHEN K_TSUIKA_KYUHU.UKE_YEAR || '-' || K_TSUIKA_KYUHU.UKE_NO || '-' ||  K_TSUIKA_KYUHU.UKE_EDABAN = '" + _param._uke + "' THEN K_TSUIKA_KYUHU.UKE_NO ");
            stb.append("          ELSE T1.UKE_NO ");
            stb.append("     END AS UKE_NO, ");
            stb.append("     CASE WHEN K_TSUSHINHI.UKE_YEAR || '-' || K_TSUSHINHI.UKE_NO || '-' ||  K_TSUSHINHI.UKE_EDABAN = '" + _param._uke + "' THEN K_TSUSHINHI.UKE_EDABAN ");
            stb.append("          WHEN K_TSUIKA_KYUHU.UKE_YEAR || '-' || K_TSUIKA_KYUHU.UKE_NO || '-' ||  K_TSUIKA_KYUHU.UKE_EDABAN = '" + _param._uke + "' THEN K_TSUIKA_KYUHU.UKE_EDABAN ");
            stb.append("          ELSE T1.UKE_EDABAN ");
            stb.append("     END AS UKE_EDABAN, ");
        }
        if (KOJIN.equals(_param._classDiv)) {
            stb.append("     MAIN_KETTEI.KETTEI_DATE AS PUT_DATE, ");
        } else {
            stb.append("     CASE WHEN K_TSUIKA_KYUHU.UKE_YEAR || '-' || K_TSUIKA_KYUHU.UKE_NO || '-' ||  K_TSUIKA_KYUHU.UKE_EDABAN = '" + _param._uke + "' ");
            stb.append("          THEN TSUIKA_KYUHU.KETTEI_DATE ");
            stb.append("          ELSE MAIN_KETTEI.KETTEI_DATE ");
            stb.append("     END AS PUT_DATE, ");
        }
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.REMARK, ");
        stb.append("     CONCAT(CONCAT(KOJIN.FAMILY_NAME, '　'), KOJIN.FIRST_NAME) AS KOJIN_NAME, ");
        stb.append("     SCHOOL.SCHOOL_DISTCD, ");
        stb.append("     VALUE(SCHOOL.NAME, '') AS SCHOOL_NAME, ");
        stb.append("     CONCAT(CONCAT(SHINKEN.FAMILY_NAME, '　'), SHINKEN.FIRST_NAME) AS HOGO_NAME, ");
        stb.append("     SHINKEN.ZIPCD, ");
        stb.append("     SHINKEN.ADDR1, ");
        stb.append("     SHINKEN.ADDR2, ");
        if (KOJIN.equals(_param._classDiv)) {
            stb.append("     COALESCE(MAIN_KETTEI.KYUHU_KETTEI_GK, 0) + "); //指定受付の給付金
            stb.append("     CASE WHEN T1.UKE_YEAR || T1.UKE_NO || T1.UKE_EDABAN = K_TSUSHINHI.UKE_YEAR || K_TSUSHINHI.UKE_NO || K_TSUSHINHI.UKE_EDABAN ");
            stb.append("          THEN COALESCE(TSUSHINHI.TSUSHINHI_KETTEI_GK, 0) ");
            stb.append("          ELSE 0 ");
            stb.append("     END + "); //給付の受付と一致する通信費
            stb.append("     CASE WHEN T1.UKE_YEAR || T1.UKE_NO || T1.UKE_EDABAN = K_TSUIKA_KYUHU.UKE_YEAR || K_TSUIKA_KYUHU.UKE_NO || K_TSUIKA_KYUHU.UKE_EDABAN ");
            stb.append("          THEN COALESCE(TSUIKA_KYUHU.TSUIKA_KYUHU_KETTEI_GK, 0) ");
            stb.append("          ELSE 0 ");
            stb.append("     END "); //給付の受付と一致する追加給付
            stb.append("     AS KYUHU_KETTEI_GK, "); //給付決定額（上記合計）
            stb.append("     COALESCE(MAIN_KETTEI.KYUHU_KETTEI_GK, 0) AS KYUHU_GK, ");
            stb.append("     CASE WHEN T1.UKE_YEAR || T1.UKE_NO || T1.UKE_EDABAN = K_TSUSHINHI.UKE_YEAR || K_TSUSHINHI.UKE_NO || K_TSUSHINHI.UKE_EDABAN ");
            stb.append("          THEN COALESCE(TSUSHINHI.TSUSHINHI_KETTEI_GK, 0) ");
            stb.append("          ELSE 0 ");
            stb.append("     END AS TSUSHINHI_GK, ");
            stb.append("     CASE WHEN T1.UKE_YEAR || T1.UKE_NO || T1.UKE_EDABAN = K_TSUIKA_KYUHU.UKE_YEAR || K_TSUIKA_KYUHU.UKE_NO || K_TSUIKA_KYUHU.UKE_EDABAN ");
            stb.append("          THEN COALESCE(TSUIKA_KYUHU.TSUIKA_KYUHU_KETTEI_GK, 0) ");
            stb.append("          ELSE 0 ");
            stb.append("     END AS TSUIKA_KYUHU_GK, ");
        } else {

            stb.append("     CASE WHEN T1.UKE_YEAR || '-' || T1.UKE_NO || '-' ||  T1.UKE_EDABAN = '" + _param._uke + "' ");
            stb.append("          THEN COALESCE(MAIN_KETTEI.KYUHU_KETTEI_GK, 0) ");
            stb.append("          ELSE 0 ");
            stb.append("     END + "); //指定受付の給付金
            stb.append("     CASE WHEN K_TSUSHINHI.UKE_YEAR || '-' || K_TSUSHINHI.UKE_NO || '-' ||  K_TSUSHINHI.UKE_EDABAN = '" + _param._uke + "' ");
            stb.append("          THEN COALESCE(TSUSHINHI.TSUSHINHI_KETTEI_GK, 0) ");
            stb.append("          ELSE 0 ");
            stb.append("     END + "); //指定受付の通信費
            stb.append("     CASE WHEN K_TSUIKA_KYUHU.UKE_YEAR || '-' || K_TSUIKA_KYUHU.UKE_NO || '-' ||  K_TSUIKA_KYUHU.UKE_EDABAN = '" + _param._uke + "' ");
            stb.append("          THEN COALESCE(TSUIKA_KYUHU.TSUIKA_KYUHU_KETTEI_GK, 0) ");
            stb.append("          ELSE 0 ");
            stb.append("     END "); //指定受付の追加給付
            stb.append("     AS KYUHU_KETTEI_GK, "); //給付決定額（上記合計）
            stb.append("     CASE WHEN T1.UKE_YEAR || '-' || T1.UKE_NO || '-' ||  T1.UKE_EDABAN = '" + _param._uke + "' ");
            stb.append("          THEN COALESCE(MAIN_KETTEI.KYUHU_KETTEI_GK, 0) ");
            stb.append("          ELSE 0 ");
            stb.append("     END AS KYUHU_GK, ");
            stb.append("     CASE WHEN K_TSUSHINHI.UKE_YEAR || '-' || K_TSUSHINHI.UKE_NO || '-' ||  K_TSUSHINHI.UKE_EDABAN = '" + _param._uke + "' ");
            stb.append("          THEN COALESCE(TSUSHINHI.TSUSHINHI_KETTEI_GK, 0) ");
            stb.append("          ELSE 0 ");
            stb.append("     END AS TSUSHINHI_GK, ");
            stb.append("     CASE WHEN K_TSUIKA_KYUHU.UKE_YEAR || '-' || K_TSUIKA_KYUHU.UKE_NO || '-' ||  K_TSUIKA_KYUHU.UKE_EDABAN = '" + _param._uke + "' ");
            stb.append("          THEN COALESCE(TSUIKA_KYUHU.TSUIKA_KYUHU_KETTEI_GK, 0) ");
            stb.append("          ELSE 0 ");
            stb.append("     END AS TSUIKA_KYUHU_GK, ");
        }
        stb.append("     0 AS HENKOU_MAE_GK ");
        // ORDER BY
        stb.append("     , T1.H_SCHOOL_CD AS ORDER_H_SCHOOL_CD ");
        stb.append("     , T1.KATEI_DIV AS ORDER_KATEI_DIV ");
        stb.append("     , CAST(T1.GRADE as int) AS ORDER_GRADE ");
        stb.append("     , T1.HR_CLASS AS ORDER_HR_CLASS ");
        stb.append("     , CAST(T1.ATTENDNO as int) AS ORDER_ATTENDNO ");
        stb.append("     , T1.KOJIN_NO AS ORDER_KOJIN_NO ");
        stb.append("     , T1.SEQ AS ORDER_SEQ ");
        stb.append("     , '1' AS ORDER_KBN ");
        stb.append(" FROM ");
        stb.append("     KOJIN_SHINSEI_KYUHU_DAT T1 ");
        stb.append("     LEFT JOIN V_KOJIN_HIST_DAT KOJIN ON T1.KOJIN_NO = KOJIN.KOJIN_NO ");
        stb.append("     LEFT JOIN SCHOOL_DAT SCHOOL ON T1.H_SCHOOL_CD = SCHOOL.SCHOOLCD ");
        stb.append("     LEFT JOIN SHINKENSHA_HIST_DAT SHINKEN ON T1.HOGOSHA_CD = SHINKEN.SHINKEN_CD ");
        stb.append("     INNER JOIN MAIN_KETTEI ON T1.KOJIN_NO = MAIN_KETTEI.KOJIN_NO ");
        stb.append("           AND T1.SHINSEI_YEAR = MAIN_KETTEI.SHINSEI_YEAR ");
        stb.append("           AND T1.SEQ = MAIN_KETTEI.SEQ ");
        stb.append("     LEFT JOIN MAIN_KETTEI_TSUSHINHI TSUSHINHI ");
        stb.append("            ON T1.KOJIN_NO     = TSUSHINHI.KOJIN_NO ");
        stb.append("           AND T1.SHINSEI_YEAR = TSUSHINHI.SHINSEI_YEAR ");
        stb.append("           AND T1.SEQ          = TSUSHINHI.SEQ ");
        stb.append("     LEFT JOIN KOJIN_SHINSEI_TSUSHINHI_DAT K_TSUSHINHI ");
        stb.append("            ON T1.KOJIN_NO     = K_TSUSHINHI.KOJIN_NO ");
        stb.append("           AND T1.SHINSEI_YEAR = K_TSUSHINHI.SHINSEI_YEAR ");
        stb.append("           AND T1.SEQ          = K_TSUSHINHI.SEQ ");
        if (KOJIN.equals(_param._classDiv)) {
            stb.append("           AND T1.UKE_YEAR || T1.UKE_NO || T1.UKE_EDABAN = K_TSUSHINHI.UKE_YEAR || K_TSUSHINHI.UKE_NO || K_TSUSHINHI.UKE_EDABAN ");
        }
        stb.append("     LEFT JOIN MAIN_KETTEI_TSUIKA_KYUHU TSUIKA_KYUHU ");
        stb.append("            ON T1.KOJIN_NO     = TSUIKA_KYUHU.KOJIN_NO ");
        stb.append("           AND T1.SHINSEI_YEAR = TSUIKA_KYUHU.SHINSEI_YEAR ");
        stb.append("           AND T1.SEQ          = TSUIKA_KYUHU.SEQ ");
        stb.append("     LEFT JOIN KOJIN_SHINSEI_TSUIKA_KYUHU_DAT K_TSUIKA_KYUHU ");
        stb.append("            ON T1.KOJIN_NO     = K_TSUIKA_KYUHU.KOJIN_NO ");
        stb.append("           AND T1.SHINSEI_YEAR = K_TSUIKA_KYUHU.SHINSEI_YEAR ");
        stb.append("           AND T1.SEQ          = K_TSUIKA_KYUHU.SEQ ");
        if (KOJIN.equals(_param._classDiv)) {
            stb.append("           AND T1.UKE_YEAR || T1.UKE_NO || T1.UKE_EDABAN = K_TSUIKA_KYUHU.UKE_YEAR || K_TSUIKA_KYUHU.UKE_NO || K_TSUIKA_KYUHU.UKE_EDABAN ");
        }
        stb.append(" WHERE ");
        stb.append("     T1.SHINSEI_YEAR = '" + _param._shinseiYear + "' ");
        if (KOJIN.equals(_param._classDiv)) {
            stb.append("     AND T1.KOJIN_NO = '" + _param._kojinNo + "' ");
        }
        stb.append("     AND VALUE(T1.CANCEL_FLG, '0') = '0' ");
        stb.append("     AND VALUE(T1.KETTEI_FLG, '0') = '1' ");
        stb.append("     AND T1.KETTEI_DATE IS NOT NULL ");
        if (SCHOOL.equals(_param._classDiv)) {
            stb.append("     AND (T1.UKE_YEAR || '-' || T1.UKE_NO || '-' ||  T1.UKE_EDABAN = '" + _param._uke + "' ");
            stb.append("          OR ");
            stb.append("          K_TSUSHINHI.UKE_YEAR || '-' || K_TSUSHINHI.UKE_NO || '-' ||  K_TSUSHINHI.UKE_EDABAN = '" + _param._uke + "' ");
            stb.append("          OR ");
            stb.append("          K_TSUIKA_KYUHU.UKE_YEAR || '-' || K_TSUIKA_KYUHU.UKE_NO || '-' ||  K_TSUIKA_KYUHU.UKE_EDABAN = '" + _param._uke + "') ");
            stb.append("     AND T1.H_SCHOOL_CD IN " + _param._schoolInState + " ");
        }
        if (KOJIN.equals(_param._classDiv)) {
            //通信費
            stb.append(" UNION ");
            stb.append(" SELECT ");
            stb.append("     T1.SHUUGAKU_NO, ");
            stb.append("     K_TSUSHINHI.UKE_YEAR, ");
            stb.append("     K_TSUSHINHI.UKE_NO, ");
            stb.append("     K_TSUSHINHI.UKE_EDABAN, ");
            stb.append("     MAIN_KETTEI.KETTEI_DATE AS PUT_DATE, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.ATTENDNO, ");
            stb.append("     T1.REMARK, ");
            stb.append("     CONCAT(CONCAT(KOJIN.FAMILY_NAME, '　'), KOJIN.FIRST_NAME) AS KOJIN_NAME, ");
            stb.append("     SCHOOL.SCHOOL_DISTCD, ");
            stb.append("     VALUE(SCHOOL.NAME, '') AS SCHOOL_NAME, ");
            stb.append("     CONCAT(CONCAT(SHINKEN.FAMILY_NAME, '　'), SHINKEN.FIRST_NAME) AS HOGO_NAME, ");
            stb.append("     SHINKEN.ZIPCD, ");
            stb.append("     SHINKEN.ADDR1, ");
            stb.append("     SHINKEN.ADDR2, ");
            stb.append("     COALESCE(TSUSHINHI.TSUSHINHI_KETTEI_GK, 0) AS KYUHU_KETTEI_GK, ");
            stb.append("     0 AS KYUHU_GK, ");
            stb.append("     COALESCE(TSUSHINHI.TSUSHINHI_KETTEI_GK, 0) AS TSUSHINHI_GK, ");
            stb.append("     0 AS TSUIKA_KYUHU_GK, ");
            stb.append("     0 AS HENKOU_MAE_GK ");
            // ORDER BY
            stb.append("     , T1.H_SCHOOL_CD AS ORDER_H_SCHOOL_CD ");
            stb.append("     , T1.KATEI_DIV AS ORDER_KATEI_DIV ");
            stb.append("     , CAST(T1.GRADE as int) AS ORDER_GRADE ");
            stb.append("     , T1.HR_CLASS AS ORDER_HR_CLASS ");
            stb.append("     , CAST(T1.ATTENDNO as int) AS ORDER_ATTENDNO ");
            stb.append("     , T1.KOJIN_NO AS ORDER_KOJIN_NO ");
            stb.append("     , T1.SEQ AS ORDER_SEQ ");
            stb.append("     , '2' AS ORDER_KBN ");
            stb.append(" FROM ");
            stb.append("     KOJIN_SHINSEI_KYUHU_DAT T1 ");
            stb.append("     LEFT JOIN V_KOJIN_HIST_DAT KOJIN ON T1.KOJIN_NO = KOJIN.KOJIN_NO ");
            stb.append("     LEFT JOIN SCHOOL_DAT SCHOOL ON T1.H_SCHOOL_CD = SCHOOL.SCHOOLCD ");
            stb.append("     LEFT JOIN SHINKENSHA_HIST_DAT SHINKEN ON T1.HOGOSHA_CD = SHINKEN.SHINKEN_CD ");
            stb.append("     INNER JOIN MAIN_KETTEI ON T1.KOJIN_NO = MAIN_KETTEI.KOJIN_NO ");
            stb.append("           AND T1.SHINSEI_YEAR = MAIN_KETTEI.SHINSEI_YEAR ");
            stb.append("           AND T1.SEQ = MAIN_KETTEI.SEQ ");
            stb.append("     LEFT JOIN MAIN_KETTEI_TSUSHINHI TSUSHINHI ");
            stb.append("            ON T1.KOJIN_NO     = TSUSHINHI.KOJIN_NO ");
            stb.append("           AND T1.SHINSEI_YEAR = TSUSHINHI.SHINSEI_YEAR ");
            stb.append("           AND T1.SEQ          = TSUSHINHI.SEQ ");
            stb.append("     INNER JOIN KOJIN_SHINSEI_TSUSHINHI_DAT K_TSUSHINHI ");
            stb.append("            ON T1.KOJIN_NO     = K_TSUSHINHI.KOJIN_NO ");
            stb.append("           AND T1.SHINSEI_YEAR = K_TSUSHINHI.SHINSEI_YEAR ");
            stb.append("           AND T1.SEQ          = K_TSUSHINHI.SEQ ");
            stb.append(" WHERE ");
            stb.append("     T1.SHINSEI_YEAR = '" + _param._shinseiYear + "' ");
            stb.append("     AND T1.KOJIN_NO = '" + _param._kojinNo + "' ");
            stb.append("     AND VALUE(T1.CANCEL_FLG, '0') = '0' ");
            stb.append("     AND VALUE(T1.KETTEI_FLG, '0') = '1' ");
            stb.append("     AND T1.KETTEI_DATE IS NOT NULL ");
            stb.append("     AND T1.UKE_YEAR || '-' || T1.UKE_NO || '-' ||  T1.UKE_EDABAN <> K_TSUSHINHI.UKE_YEAR || '-' || K_TSUSHINHI.UKE_NO || '-' ||  K_TSUSHINHI.UKE_EDABAN "); //受付番号の異なる通信費
            //追加給付
            stb.append(" UNION ");
            stb.append(" SELECT ");
            stb.append("     T1.SHUUGAKU_NO, ");
            stb.append("     K_TSUIKA_KYUHU.UKE_YEAR, ");
            stb.append("     K_TSUIKA_KYUHU.UKE_NO, ");
            stb.append("     K_TSUIKA_KYUHU.UKE_EDABAN, ");
            stb.append("     TSUIKA_KYUHU.KETTEI_DATE AS PUT_DATE, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.ATTENDNO, ");
            stb.append("     T1.REMARK, ");
            stb.append("     CONCAT(CONCAT(KOJIN.FAMILY_NAME, '　'), KOJIN.FIRST_NAME) AS KOJIN_NAME, ");
            stb.append("     SCHOOL.SCHOOL_DISTCD, ");
            stb.append("     VALUE(SCHOOL.NAME, '') AS SCHOOL_NAME, ");
            stb.append("     CONCAT(CONCAT(SHINKEN.FAMILY_NAME, '　'), SHINKEN.FIRST_NAME) AS HOGO_NAME, ");
            stb.append("     SHINKEN.ZIPCD, ");
            stb.append("     SHINKEN.ADDR1, ");
            stb.append("     SHINKEN.ADDR2, ");
            stb.append("     COALESCE(TSUIKA_KYUHU.TSUIKA_KYUHU_KETTEI_GK, 0) AS KYUHU_KETTEI_GK, ");
            stb.append("     0 AS KYUHU_GK, ");
            stb.append("     0 AS TSUSHINHI_GK, ");
            stb.append("     COALESCE(TSUIKA_KYUHU.TSUIKA_KYUHU_KETTEI_GK, 0) AS TSUIKA_KYUHU_GK, ");
            stb.append("     0 AS HENKOU_MAE_GK ");
            // ORDER BY
            stb.append("     , T1.H_SCHOOL_CD AS ORDER_H_SCHOOL_CD ");
            stb.append("     , T1.KATEI_DIV AS ORDER_KATEI_DIV ");
            stb.append("     , CAST(T1.GRADE as int) AS ORDER_GRADE ");
            stb.append("     , T1.HR_CLASS AS ORDER_HR_CLASS ");
            stb.append("     , CAST(T1.ATTENDNO as int) AS ORDER_ATTENDNO ");
            stb.append("     , T1.KOJIN_NO AS ORDER_KOJIN_NO ");
            stb.append("     , T1.SEQ AS ORDER_SEQ ");
            stb.append("     , '3' AS ORDER_KBN ");
            stb.append(" FROM ");
            stb.append("     KOJIN_SHINSEI_KYUHU_DAT T1 ");
            stb.append("     LEFT JOIN V_KOJIN_HIST_DAT KOJIN ON T1.KOJIN_NO = KOJIN.KOJIN_NO ");
            stb.append("     LEFT JOIN SCHOOL_DAT SCHOOL ON T1.H_SCHOOL_CD = SCHOOL.SCHOOLCD ");
            stb.append("     LEFT JOIN SHINKENSHA_HIST_DAT SHINKEN ON T1.HOGOSHA_CD = SHINKEN.SHINKEN_CD ");
            stb.append("     INNER JOIN MAIN_KETTEI ON T1.KOJIN_NO = MAIN_KETTEI.KOJIN_NO ");
            stb.append("           AND T1.SHINSEI_YEAR = MAIN_KETTEI.SHINSEI_YEAR ");
            stb.append("           AND T1.SEQ = MAIN_KETTEI.SEQ ");
            stb.append("     LEFT JOIN MAIN_KETTEI_TSUIKA_KYUHU TSUIKA_KYUHU ");
            stb.append("            ON T1.KOJIN_NO     = TSUIKA_KYUHU.KOJIN_NO ");
            stb.append("           AND T1.SHINSEI_YEAR = TSUIKA_KYUHU.SHINSEI_YEAR ");
            stb.append("           AND T1.SEQ          = TSUIKA_KYUHU.SEQ ");
            stb.append("     INNER JOIN KOJIN_SHINSEI_TSUIKA_KYUHU_DAT K_TSUIKA_KYUHU ");
            stb.append("            ON T1.KOJIN_NO     = K_TSUIKA_KYUHU.KOJIN_NO ");
            stb.append("           AND T1.SHINSEI_YEAR = K_TSUIKA_KYUHU.SHINSEI_YEAR ");
            stb.append("           AND T1.SEQ          = K_TSUIKA_KYUHU.SEQ ");
            stb.append(" WHERE ");
            stb.append("     T1.SHINSEI_YEAR = '" + _param._shinseiYear + "' ");
            stb.append("     AND T1.KOJIN_NO = '" + _param._kojinNo + "' ");
            stb.append("     AND VALUE(T1.CANCEL_FLG, '0') = '0' ");
            stb.append("     AND VALUE(T1.KETTEI_FLG, '0') = '1' ");
            stb.append("     AND T1.KETTEI_DATE IS NOT NULL ");
            stb.append("     AND T1.UKE_YEAR || '-' || T1.UKE_NO || '-' ||  T1.UKE_EDABAN <> K_TSUIKA_KYUHU.UKE_YEAR || '-' || K_TSUIKA_KYUHU.UKE_NO || '-' ||  K_TSUIKA_KYUHU.UKE_EDABAN "); //受付番号の異なる追加給付
        }
        stb.append(" ORDER BY ");
        stb.append("     ORDER_H_SCHOOL_CD, ");
        stb.append("     ORDER_KATEI_DIV, ");
        stb.append("     ORDER_GRADE, ");
        stb.append("     ORDER_HR_CLASS, ");
        stb.append("     ORDER_ATTENDNO, ");
        stb.append("     ORDER_KOJIN_NO, ");
        stb.append("     ORDER_SEQ, ");
        stb.append("     ORDER_KBN "); //1:給付 2:通信費 3:追加給付
        log.info(stb.toString());
        return stb.toString();
    }

    private String getTorikesi() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SHUUGAKU_NO, ");
        stb.append("     T1.KETTEI_CANCEL_UKE_YEAR AS UKE_YEAR, ");
        stb.append("     T1.KETTEI_CANCEL_UKE_NO AS UKE_NO, ");
        stb.append("     T1.KETTEI_CANCEL_UKE_EDABAN AS UKE_EDABAN, ");
        stb.append("     T1.KETTEI_CANCEL_DATE AS PUT_DATE, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.REMARK, ");
        stb.append("     CONCAT(CONCAT(KOJIN.FAMILY_NAME, '　'), KOJIN.FIRST_NAME) AS KOJIN_NAME, ");
        stb.append("     SCHOOL.SCHOOL_DISTCD, ");
        stb.append("     VALUE(SCHOOL.NAME, '') AS SCHOOL_NAME, ");
        stb.append("     CONCAT(CONCAT(SHINKEN.FAMILY_NAME, '　'), SHINKEN.FIRST_NAME) AS HOGO_NAME, ");
        stb.append("     SHINKEN.ZIPCD, ");
        stb.append("     SHINKEN.ADDR1, ");
        stb.append("     SHINKEN.ADDR2, ");
        stb.append("     0 AS KYUHU_KETTEI_GK, ");
        stb.append("     0 AS KYUHU_GK, ");
        stb.append("     0 AS TSUSHINHI_GK, ");
        stb.append("     0 AS TSUIKA_KYUHU_GK, ");
        stb.append("     0 AS HENKOU_MAE_GK ");
        stb.append(" FROM ");
        stb.append("     KOJIN_SHINSEI_KYUHU_DAT T1 ");
        stb.append("     LEFT JOIN V_KOJIN_HIST_DAT KOJIN ON T1.KOJIN_NO = KOJIN.KOJIN_NO ");
        stb.append("     LEFT JOIN SCHOOL_DAT SCHOOL ON T1.H_SCHOOL_CD = SCHOOL.SCHOOLCD ");
        stb.append("     LEFT JOIN SHINKENSHA_HIST_DAT SHINKEN ON T1.HOGOSHA_CD = SHINKEN.SHINKEN_CD ");
        stb.append(" WHERE ");
        stb.append("     T1.SHINSEI_YEAR = '" + _param._shinseiYear + "' ");
        if (KOJIN.equals(_param._classDiv)) {
            stb.append("     AND T1.KOJIN_NO = '" + _param._kojinNo + "' ");
        }
        stb.append("     AND VALUE(T1.CANCEL_FLG, '0') = '1' ");
        stb.append("     AND VALUE(T1.KETTEI_FLG, '0') = '1' ");
        stb.append("     AND T1.KETTEI_DATE IS NOT NULL ");
        if (SCHOOL.equals(_param._classDiv)) {
            stb.append("     AND T1.UKE_YEAR || '-' || T1.UKE_NO || '-' ||  T1.UKE_EDABAN = '" + _param._uke + "' ");
            stb.append("     AND T1.H_SCHOOL_CD IN " + _param._schoolInState + " ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.H_SCHOOL_CD, ");
        stb.append("     T1.KATEI_DIV, ");
        stb.append("     CAST(T1.GRADE as int), ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     CAST(T1.ATTENDNO as int), ");
        stb.append("     T1.KOJIN_NO, ");
        stb.append("     T1.SEQ ");

        return stb.toString();
    }

    private String getHenkou() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH MAX_KETTEI AS ( ");
        stb.append(" SELECT ");
        stb.append("     KOJIN_NO, ");
        stb.append("     SHINSEI_YEAR, ");
        stb.append("     SEQ, ");
        stb.append("     MAX(KETTEI_DATE) AS KETTEI_DATE ");
        stb.append(" FROM ");
        stb.append("     KOJIN_KYUHU_GK_KETTEI_HIST_DAT ");
        stb.append(" WHERE ");
        stb.append("     SHINSEI_YEAR = '" + _param._shinseiYear + "' ");
        stb.append("     AND SHORI_DIV = '" + SHORI_HENKOU + "' ");
        stb.append(" GROUP BY ");
        stb.append("     KOJIN_NO, ");
        stb.append("     SHINSEI_YEAR, ");
        stb.append("     SEQ ");
        stb.append(" ), MAIN_KETTEI AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.* ");
        stb.append(" FROM ");
        stb.append("     KOJIN_KYUHU_GK_KETTEI_HIST_DAT T1, ");
        stb.append("     MAX_KETTEI T2 ");
        stb.append(" WHERE ");
        stb.append("     T1.KOJIN_NO = T2.KOJIN_NO ");
        stb.append("     AND T1.SHINSEI_YEAR = T2.SHINSEI_YEAR ");
        stb.append("     AND T1.KETTEI_DATE = T2.KETTEI_DATE ");
        stb.append("     AND T1.SEQ = T2.SEQ ");
        stb.append(" ), TYOKKIN_MAX_KETTEI AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.KOJIN_NO, ");
        stb.append("     T1.SHINSEI_YEAR, ");
        stb.append("     T1.SEQ, ");
        stb.append("     MAX(T1.KETTEI_DATE) AS KETTEI_DATE ");
        stb.append(" FROM ");
        stb.append("     KOJIN_KYUHU_GK_KETTEI_HIST_DAT T1, ");
        stb.append("     MAX_KETTEI T2 ");
        stb.append(" WHERE ");
        stb.append("     T1.KOJIN_NO = T2.KOJIN_NO ");
        stb.append("     AND T1.SHINSEI_YEAR = T2.SHINSEI_YEAR ");
        stb.append("     AND T1.KETTEI_DATE < T2.KETTEI_DATE ");
        stb.append(" GROUP BY ");
        stb.append("     T1.KOJIN_NO, ");
        stb.append("     T1.SHINSEI_YEAR, ");
        stb.append("     T1.SEQ ");
        stb.append(" ), TYOKKIN_KETTEI AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.* ");
        stb.append(" FROM ");
        stb.append("     KOJIN_KYUHU_GK_KETTEI_HIST_DAT T1, ");
        stb.append("     TYOKKIN_MAX_KETTEI T2 ");
        stb.append(" WHERE ");
        stb.append("     T1.KOJIN_NO = T2.KOJIN_NO ");
        stb.append("     AND T1.SHINSEI_YEAR = T2.SHINSEI_YEAR ");
        stb.append("     AND T1.KETTEI_DATE = T2.KETTEI_DATE ");
        stb.append("     AND T1.SEQ = T2.SEQ ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.SHUUGAKU_NO, ");
        stb.append("     MAIN_KETTEI.HENKOU_UKE_YEAR AS UKE_YEAR, ");
        stb.append("     MAIN_KETTEI.HENKOU_UKE_NO AS UKE_NO, ");
        stb.append("     MAIN_KETTEI.HENKOU_UKE_EDABAN AS UKE_EDABAN, ");
        stb.append("     MAIN_KETTEI.KETTEI_DATE AS PUT_DATE, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.REMARK, ");
        stb.append("     CONCAT(CONCAT(KOJIN.FAMILY_NAME, '　'), KOJIN.FIRST_NAME) AS KOJIN_NAME, ");
        stb.append("     SCHOOL.SCHOOL_DISTCD, ");
        stb.append("     VALUE(SCHOOL.NAME, '') AS SCHOOL_NAME, ");
        stb.append("     CONCAT(CONCAT(SHINKEN.FAMILY_NAME, '　'), SHINKEN.FIRST_NAME) AS HOGO_NAME, ");
        stb.append("     SHINKEN.ZIPCD, ");
        stb.append("     SHINKEN.ADDR1, ");
        stb.append("     SHINKEN.ADDR2, ");
        stb.append("     MAIN_KETTEI.KYUHU_KETTEI_GK AS KYUHU_KETTEI_GK, ");
        stb.append("     0 AS KYUHU_GK, ");
        stb.append("     0 AS TSUSHINHI_GK, ");
        stb.append("     0 AS TSUIKA_KYUHU_GK, ");
        stb.append("     TYOKKIN_KETTEI.KYUHU_KETTEI_GK AS HENKOU_MAE_GK ");
        stb.append(" FROM ");
        stb.append("     KOJIN_SHINSEI_KYUHU_DAT T1 ");
        stb.append("     LEFT JOIN V_KOJIN_HIST_DAT KOJIN ON T1.KOJIN_NO = KOJIN.KOJIN_NO ");
        stb.append("     LEFT JOIN SCHOOL_DAT SCHOOL ON T1.H_SCHOOL_CD = SCHOOL.SCHOOLCD ");
        stb.append("     LEFT JOIN SHINKENSHA_HIST_DAT SHINKEN ON T1.HOGOSHA_CD = SHINKEN.SHINKEN_CD ");
        stb.append("     INNER JOIN MAIN_KETTEI ON T1.KOJIN_NO = MAIN_KETTEI.KOJIN_NO ");
        stb.append("           AND T1.SHINSEI_YEAR = MAIN_KETTEI.SHINSEI_YEAR ");
        stb.append("           AND T1.SEQ = MAIN_KETTEI.SEQ ");
        stb.append("     INNER JOIN TYOKKIN_KETTEI ON T1.KOJIN_NO = TYOKKIN_KETTEI.KOJIN_NO ");
        stb.append("           AND T1.SHINSEI_YEAR = TYOKKIN_KETTEI.SHINSEI_YEAR ");
        stb.append("           AND T1.SEQ = TYOKKIN_KETTEI.SEQ ");
        stb.append(" WHERE ");
        stb.append("     T1.SHINSEI_YEAR = '" + _param._shinseiYear + "' ");
        if (KOJIN.equals(_param._classDiv)) {
            stb.append("     AND T1.KOJIN_NO = '" + _param._kojinNo + "' ");
        }
        stb.append("     AND VALUE(T1.CANCEL_FLG, '0') = '0' ");
        stb.append("     AND VALUE(T1.KETTEI_FLG, '0') = '1' ");
        stb.append("     AND T1.KETTEI_DATE IS NOT NULL ");
        if (SCHOOL.equals(_param._classDiv)) {
            stb.append("     AND T1.UKE_YEAR || '-' || T1.UKE_NO || '-' ||  T1.UKE_EDABAN = '" + _param._uke + "' ");
            stb.append("     AND T1.H_SCHOOL_CD IN " + _param._schoolInState + " ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.H_SCHOOL_CD, ");
        stb.append("     T1.KATEI_DIV, ");
        stb.append("     CAST(T1.GRADE as int), ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     CAST(T1.ATTENDNO as int), ");
        stb.append("     T1.KOJIN_NO, ");
        stb.append("     T1.SEQ ");

        return stb.toString();
    }

    public String getBunshoBangou(final ResultSet rs, final DB2UDB db2) {
        try {
            final String kind = "3".equals(rs.getString("SCHOOL_DISTCD")) ? "文教" : "教高";
            final String bangou = (null == rs.getString("UKE_NO")) ? "" : String.valueOf(Integer.parseInt(rs.getString("UKE_NO")));
            final String edaban = (null ==rs.getString("UKE_EDABAN") || Integer.parseInt(rs.getString("UKE_EDABAN")) == 1) ? "" : ("の" + Integer.parseInt(rs.getString("UKE_EDABAN")));
            final String gengou = KNJ_EditDate.gengou(db2, Integer.parseInt(rs.getString("UKE_YEAR")));
            final int waketa = (Integer.parseInt(rs.getString("UKE_YEAR")) == 2018 && Integer.parseInt(rs.getString("UKE_NO")) == 664) ? 2 : 1;
            final String wa = (gengou.length() < waketa) ? "" : gengou.substring(gengou.length() - waketa);
            return wa + kind + "第" + bangou + "号" + edaban;
        } catch (Exception e) {
            log.error("exception!", e);
        }
        return null;
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 77450 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _shinseiYear;
        private final String _classDiv;
        private final String _uke;
        private String _schoolInState;
        private final String _form;
        private final String _kojinNo;
        private final String _ctrlDate;
        final String _chijiName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _shinseiYear = request.getParameter("SHINSEI_YEAR");
            _classDiv = request.getParameter("CLASS_DIV");
            _uke = request.getParameter("UKE");

            if (SCHOOL.equals(_classDiv)) {
                final String schools[] = request.getParameterValues("SCHOOL_SELECTED");
                String schoolInState = "( ";
                String sep = "";
                for (int ia = 0; ia < schools.length; ia++) {
                    schoolInState += sep + "'" + schools[ia] + "'";
                    sep = ", ";
                }
                _schoolInState = schoolInState + " )";
            }
            _form = request.getParameter("FORM");
            _kojinNo = request.getParameter("KOJIN_NO");
            _ctrlDate = request.getParameter("LOGIN_DATE");
            _chijiName = getChijiName(db2);
        }

        private String getChijiName(DB2UDB db2) {
            String name = null;
            final String sql = " SELECT VALUE(CHIJI_YAKUSHOKU_NAME, '') || '　　' || VALUE(CHIJI_NAME, '') AS CHIJI_NAME FROM CHIJI_MST WHERE S_DATE = (SELECT MAX(S_DATE) FROM CHIJI_MST) ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    name = rs.getString("CHIJI_NAME");
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return name;
        }

    }
}

// eof
