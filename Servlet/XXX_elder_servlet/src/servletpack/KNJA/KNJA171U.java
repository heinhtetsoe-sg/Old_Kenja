// kanji=漢字
/*
 * $Id: 4a01dc078cf5e2e1df10e74e108e6fee43648813 $
 *
 * 作成日: 2017/08/14 10:24:21 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: 4a01dc078cf5e2e1df10e74e108e6fee43648813 $
 */
public class KNJA171U {

    private static final Log log = LogFactory.getLog("KNJA171U.class");

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
            log.fatal("$Revision: 69504 $");
            KNJServletUtils.debugParam(request, log);

            response.setContentType("application/pdf");

            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
            final Param param = new Param(db2, request);

            _param = param;

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        if ("1".equals(_param._form)) {
            printForm1(db2, svf);
        } else if ("2".equals(_param._form)) {
            printForm2(db2, svf);
        }
    }

    private void printForm1(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        String groupField = "GRADE_HR_CLASS";

        final int maxLine = 50;
        final List pageList = getPageList(groupField, getList(db2, sql(_param)), maxLine);
        final String form = "KNJA171U_1.frm";
        svf.VrSetForm(form, 1);

        for (int pi = 0; pi < pageList.size(); pi++) {
            final List dataList = (List) pageList.get(pi);
            final Map row0 = (Map) dataList.get(0);

            //ヘッダー部
            svf.VrsOut("TITLE", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度" + "　生徒マスタ一覧表"); // タイトル
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._kijunDate)); // 作成日
            svf.VrsOut("TEACHER_NAME", "担任：" + getString(row0, "TEACHER_NAME")); // 担任氏名

            //データ部
            for (int j = 0; j < dataList.size(); j++) {
                final Map row = (Map) dataList.get(j);

                final int line = j + 1;
                //生徒
                svf.VrsOutn("SCHREGNO", line, getString(row, "SCHREGNO")); // 学籍番号
                final String hrName = null == getString(row, "HR_NAME") ? "" : getString(row, "HR_NAME");
                final String attendno = null == getString(row, "ATTENDNO") ? "" : String.valueOf(Integer.parseInt(getString(row, "ATTENDNO")));
                svf.VrsOutn("HR_NAME", line, hrName + "-" + attendno); // 年組番
                svf.VrsOutn("NAME" + (getMS932ByteLength(getString(row, "NAME")) <= 20 ? "1" : getMS932ByteLength(getString(row, "NAME")) <= 30 ? "2" : "3"), line, getString(row, "NAME")); // 氏名
                svf.VrsOutn("KANA" + (getMS932ByteLength(getString(row, "KANA")) <= 20 ? "1" : getMS932ByteLength(getString(row, "KANA")) <= 30 ? "2" : "3"), line, getString(row, "KANA")); // 氏名かな
                svf.VrsOutn("BIRTHDAY", line, StringUtils.defaultString(getString(row, "BIRTHDAY")).replace('-', '/')); // 生年月日
                svf.VrsOutn("FIN_SCHOOL_CD", line, getString(row, "FINSCHOOLCD")); // 出身校コード
                svf.VrsOutn("ZIP", line, getString(row, "ZIPCD")); // 郵便番号
                svf.VrsOutn("ADDRESS" + (getMS932ByteLength(getString(row, "ADDR")) <= 60 ? "1" : getMS932ByteLength(getString(row, "ADDR")) <= 90 ? "2" : "3"), line, getString(row, "ADDR")); // 住所
                //保護者
                svf.VrsOutn("GUARD_NAME" + (getMS932ByteLength(getString(row, "GUARD_NAME")) <= 20 ? "1" : getMS932ByteLength(getString(row, "GUARD_NAME")) <= 30 ? "2" : "3"), line, getString(row, "GUARD_NAME")); // 氏名
                svf.VrsOutn("GUARD_KANA" + (getMS932ByteLength(getString(row, "GUARD_KANA")) <= 20 ? "1" : getMS932ByteLength(getString(row, "GUARD_KANA")) <= 30 ? "2" : "3"), line, getString(row, "GUARD_KANA")); // 氏名かな
                svf.VrsOutn("TEL", line, getString(row, "GUARD_TELNO")); // 電話番号
            }

            //フッター部
            String numALL = "";
            String seq = "";
            for (int n = 0; n <= 5; n++) {
                final String num = getString(row0, "IDOU_NAME" + n) + ":" + getString(row0, "CNT" + n) + "名";
                numALL += seq + num;
                seq = "、";
            }
            svf.VrsOut("NUM", numALL); // 人数

            svf.VrEndPage();
            _hasData = true;
        }
    }

    private void printForm2(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        String groupField = null;

        final int maxLine = 50;
        final List pageList = getPageList(groupField, getList(db2, sql(_param)), maxLine);
        final String form = "KNJA171U_2.frm";
        svf.VrSetForm(form, 1);

        int renban = 0;
        for (int pi = 0; pi < pageList.size(); pi++) {
            final List dataList = (List) pageList.get(pi);
            final Map row0 = (Map) dataList.get(0);

            //ヘッダー部
            svf.VrsOut("TITLE", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度　" + getString(row0, "GRADE_NAME1") + "　都県住所別　生徒マスタ（出身校付）"); // タイトル
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._kijunDate)); // 作成日

            //データ部
            for (int j = 0; j < dataList.size(); j++) {
                final Map row = (Map) dataList.get(j);

                final int line = j + 1;
                renban += 1;
                svf.VrsOutn("NO", line, String.valueOf(renban)); // 連番
                svf.VrsOutn("SCHREGNO", line, getString(row, "SCHREGNO")); // 学籍番号
                final String hrName = null == getString(row, "HR_NAME") ? "" : getString(row, "HR_NAME");
                final String attendno = null == getString(row, "ATTENDNO") ? "" : String.valueOf(Integer.parseInt(getString(row, "ATTENDNO")));
                svf.VrsOutn("HR_NAME", line, hrName + "-" + attendno); // 年組番
                svf.VrsOutn("NAME" + (getMS932ByteLength(getString(row, "NAME")) <= 20 ? "1" : getMS932ByteLength(getString(row, "NAME")) <= 30 ? "2" : "3"), line, getString(row, "NAME")); // 氏名
                svf.VrsOutn("KANA" + (getMS932ByteLength(getString(row, "KANA")) <= 20 ? "1" : getMS932ByteLength(getString(row, "KANA")) <= 30 ? "2" : "3"), line, getString(row, "KANA")); // 氏名かな
                svf.VrsOutn("ZIP", line, getString(row, "ZIPCD")); // 郵便番号
                svf.VrsOutn("ADDRESS" + (getMS932ByteLength(getString(row, "ADDR")) <= 60 ? "1" : getMS932ByteLength(getString(row, "ADDR")) <= 90 ? "2" : "3"), line, getString(row, "ADDR")); // 住所
                svf.VrsOutn("TEL", line, getString(row, "GUARD_TELNO")); // 電話番号
                svf.VrsOutn("FIN_SCHOOL_CD", line, getString(row, "FINSCHOOLCD")); // 出身校コード
                svf.VrsOutn("FIN_SCHOOL_NAME", line, getString(row, "FINSCHOOL_NAME")); // 出身校名
            }

            svf.VrEndPage();
            _hasData = true;
        }
    }

    private static String getString(final Map m, final String field) {
        if (null == m || m.isEmpty()) {
            return null;
        }
        if (!m.containsKey(field)) {
            throw new IllegalArgumentException("not defined: " + field + " in " + m.keySet());
        }
        return (String) m.get(field);
    }

    private static int getMS932ByteLength(final String str) {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes( "MS932").length;
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
    }

    private static List getPageList(final String groupField, final List list, final int max) {
        final List rtn = new ArrayList();
        List current = null;
        String oldGroupVal = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Map row = (Map) it.next();
            final boolean isDiffGroup = null != groupField && (null == oldGroupVal && null != getString(row, groupField) || null != oldGroupVal && !oldGroupVal.equals(getString(row, groupField)));
            if (null == current || current.size() >= max || isDiffGroup) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(row);
            if (null != groupField) {
                oldGroupVal = getString(row, groupField);
            }
        }
        return rtn;
    }

    private List getList(final DB2UDB db2, final String sql) {
        final List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            log.info(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                final Map m = new HashMap();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    m.put(meta.getColumnName(i), rs.getString(meta.getColumnName(i)));
                }
                list.add(m);
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;
    }

    //生徒マスタ一覧表
    private String sql(final Param param) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH TBL_IDOU AS ( ");
        //TRANSFERCD 1:留学 2:休学
        stb.append("     SELECT ");
        stb.append("         '1' AS ORDER_CD, ");
        stb.append("         D.TRANSFERCD AS IDOU_DIV, ");
        stb.append("         N1.NAME1 AS IDOU_NAME, ");
        stb.append("         D.TRANSFER_SDATE AS IDOU_DATE, ");
        stb.append("         Z.SCHREGNO, ");
        stb.append("         Z.GRADE, ");
        stb.append("         Z.HR_CLASS, ");
        stb.append("         Z.ATTENDNO ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT Z ");
        stb.append("         INNER JOIN SCHREG_BASE_MST M ON M.SCHREGNO = Z.SCHREGNO ");
        stb.append("         INNER JOIN SCHREG_REGD_HDAT Z1 ON Z1.YEAR = Z.YEAR AND Z1.SEMESTER = Z.SEMESTER AND Z1.GRADE = Z.GRADE AND Z1.HR_CLASS = Z.HR_CLASS ");
        stb.append("         INNER JOIN SCHREG_REGD_GDAT G ON G.YEAR = Z.YEAR AND G.GRADE = Z.GRADE ");
        stb.append("         LEFT JOIN SCHREG_TRANSFER_DAT D ON D.SCHREGNO = Z.SCHREGNO ");
        stb.append("         LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'A004' AND N1.NAMECD2 = D.TRANSFERCD ");
        stb.append("     WHERE ");
        stb.append("             Z.YEAR       = '" + param._year + "' ");
        stb.append("         AND Z.SEMESTER   = '" + param._semester + "' ");
        stb.append("         AND D.TRANSFERCD IN ('1','2') ");
        stb.append("         AND '" + param._kijunDate + "'  BETWEEN D.TRANSFER_SDATE AND D.TRANSFER_EDATE ");

        //GRD_DIV 2:退学 3:転学
        stb.append("     UNION ALL ");
        stb.append("     SELECT ");
        stb.append("         '2' AS ORDER_CD, ");
        stb.append("         M2.GRD_DIV AS IDOU_DIV, ");
        stb.append("         N1.NAME1 AS IDOU_NAME, ");
        stb.append("         M2.GRD_DATE AS IDOU_DATE, ");
        stb.append("         Z.SCHREGNO, ");
        stb.append("         Z.GRADE, ");
        stb.append("         Z.HR_CLASS, ");
        stb.append("         Z.ATTENDNO ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT Z ");
        stb.append("         INNER JOIN SCHREG_BASE_MST M ON M.SCHREGNO = Z.SCHREGNO ");
        stb.append("         INNER JOIN SCHREG_REGD_HDAT Z1 ON Z1.YEAR = Z.YEAR AND Z1.SEMESTER = Z.SEMESTER AND Z1.GRADE = Z.GRADE AND Z1.HR_CLASS = Z.HR_CLASS ");
        stb.append("         INNER JOIN SCHREG_REGD_GDAT G ON G.YEAR = Z.YEAR AND G.GRADE = Z.GRADE ");
        stb.append("         LEFT JOIN SCHREG_ENT_GRD_HIST_DAT M2 ON M2.SCHREGNO = Z.SCHREGNO AND M2.SCHOOL_KIND = G.SCHOOL_KIND ");
        stb.append("         LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'A003' AND N1.NAMECD2 = M2.GRD_DIV ");
        stb.append("     WHERE ");
        stb.append("             Z.YEAR       = '" + param._year + "' ");
        stb.append("         AND Z.SEMESTER   = '" + param._semester + "' ");
        stb.append("         AND M2.GRD_DIV IN ('2','3') ");
        stb.append("         AND M2.GRD_DATE  BETWEEN '" + param._year + "-04-01' AND '" + param._kijunDate + "' ");

        //COMEBACK_DATEがある人 復学
        stb.append("     UNION ALL ");
        stb.append("     SELECT ");
        stb.append("         '3' AS ORDER_CD, ");
        stb.append("         '1' AS IDOU_DIV, ");
        stb.append("         '復学' AS IDOU_NAME, ");
        stb.append("         M1.COMEBACK_DATE AS IDOU_DATE, ");
        stb.append("         Z.SCHREGNO, ");
        stb.append("         Z.GRADE, ");
        stb.append("         Z.HR_CLASS, ");
        stb.append("         Z.ATTENDNO ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT Z ");
        stb.append("         INNER JOIN SCHREG_BASE_MST M ON M.SCHREGNO = Z.SCHREGNO ");
        stb.append("         INNER JOIN SCHREG_REGD_HDAT Z1 ON Z1.YEAR = Z.YEAR AND Z1.SEMESTER = Z.SEMESTER AND Z1.GRADE = Z.GRADE AND Z1.HR_CLASS = Z.HR_CLASS ");
        stb.append("         INNER JOIN SCHREG_REGD_GDAT G ON G.YEAR = Z.YEAR AND G.GRADE = Z.GRADE ");
        stb.append("         LEFT JOIN SCHREG_ENT_GRD_HIST_COMEBACK_DAT M1 ON M1.SCHREGNO = Z.SCHREGNO AND M1.SCHOOL_KIND = G.SCHOOL_KIND ");
        stb.append("         LEFT JOIN SCHREG_ENT_GRD_HIST_DAT M2 ON M2.SCHREGNO = Z.SCHREGNO AND M2.SCHOOL_KIND = G.SCHOOL_KIND ");
        stb.append("     WHERE ");
        stb.append("             Z.YEAR       = '" + param._year + "' ");
        stb.append("         AND Z.SEMESTER   = '" + param._semester + "' ");
        stb.append("         AND M1.COMEBACK_DATE  BETWEEN '" + param._year + "-04-01' AND '" + param._kijunDate + "' ");
        stb.append(" ) ");
        stb.append(" , TBL_IDOU_CNT AS ( ");
        stb.append("     SELECT ");
        stb.append("         COUNT(SCHREGNO) AS CNT, ");
        stb.append("         GRADE, ");
        stb.append("         HR_CLASS, ");
        stb.append("         ORDER_CD, ");
        stb.append("         IDOU_DIV, ");
        stb.append("         IDOU_NAME ");
        stb.append("     FROM ");
        stb.append("         TBL_IDOU Z ");
        stb.append("     GROUP BY ");
        stb.append("         GRADE, ");
        stb.append("         HR_CLASS, ");
        stb.append("         ORDER_CD, ");
        stb.append("         IDOU_DIV, ");
        stb.append("         IDOU_NAME ");

        //在籍
        stb.append("     UNION ALL ");
        stb.append("     SELECT ");
        stb.append("         COUNT(Z.SCHREGNO) AS CNT, ");
        stb.append("         Z.GRADE, ");
        stb.append("         Z.HR_CLASS, ");
        stb.append("         '0' AS ORDER_CD, ");
        stb.append("         '0' AS IDOU_DIV, ");
        stb.append("         '在籍' AS IDOU_NAME ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT Z ");
        stb.append("         INNER JOIN SCHREG_BASE_MST M ON M.SCHREGNO = Z.SCHREGNO ");
        stb.append("         INNER JOIN SCHREG_REGD_HDAT Z1 ON Z1.YEAR = Z.YEAR AND Z1.SEMESTER = Z.SEMESTER AND Z1.GRADE = Z.GRADE AND Z1.HR_CLASS = Z.HR_CLASS ");
        stb.append("         INNER JOIN SCHREG_REGD_GDAT G ON G.YEAR = Z.YEAR AND G.GRADE = Z.GRADE ");
        stb.append("     WHERE ");
        stb.append("             Z.YEAR       = '" + param._year + "' ");
        stb.append("         AND Z.SEMESTER   = '" + param._semester + "' ");
        stb.append("         AND NOT EXISTS ( ");
        stb.append("                 SELECT ");
        stb.append("                     'X' ");
        stb.append("                 FROM ");
        stb.append("                     SCHREG_TRANSFER_DAT D ");
        stb.append("                 WHERE ");
        stb.append("                     D.SCHREGNO = Z.SCHREGNO ");
        stb.append("                     AND '" + param._kijunDate + "' BETWEEN D.TRANSFER_SDATE AND D.TRANSFER_EDATE ");
        stb.append("                     AND D.TRANSFERCD = '1' ");
        stb.append("                 ) ");
        stb.append("         AND NOT EXISTS ( ");
        stb.append("                 SELECT ");
        stb.append("                     'X' ");
        stb.append("                 FROM ");
        stb.append("                     SCHREG_ENT_GRD_HIST_DAT M2 ");
        stb.append("                 WHERE ");
        stb.append("                     M2.SCHREGNO = Z.SCHREGNO ");
        stb.append("                     AND M2.SCHOOL_KIND = G.SCHOOL_KIND ");
        stb.append("                     AND M2.GRD_DIV IN ('1','2','3','6') ");
        stb.append("                     AND M2.GRD_DATE < '" + param._kijunDate + "' ");
        stb.append("                 ) ");
        stb.append("     GROUP BY ");
        stb.append("         Z.GRADE, ");
        stb.append("         Z.HR_CLASS ");
        stb.append(" ) ");

        //メイン
        stb.append(" SELECT ");
        stb.append("     BM.SCHREGNO, ");
        stb.append("     RD.GRADE || RD.HR_CLASS AS GRADE_HR_CLASS, ");
        stb.append("     RD.GRADE, ");
        stb.append("     RD.HR_CLASS, ");
        stb.append("     RD.ATTENDNO, ");
        stb.append("     RG.GRADE_NAME1, ");
        stb.append("     RH.HR_NAME, ");
        stb.append("     SM.STAFFNAME AS TEACHER_NAME, ");
        stb.append("     BM.NAME, ");
        stb.append("     BM.NAME_KANA AS KANA, ");
        stb.append("     BM.BIRTHDAY, ");
        stb.append("     BM.FINSCHOOLCD, ");
        stb.append("     FM.FINSCHOOL_NAME, ");
        stb.append("     AD.ZIPCD, ");
        stb.append("     VALUE(AD.ADDR1,'') || VALUE(AD.ADDR2,'') AS ADDR, ");
        stb.append("     AD.ADDR1, ");
        stb.append("     AD.ADDR2, ");
        stb.append("     GD.GUARD_NAME, ");
        stb.append("     GD.GUARD_KANA, ");
        stb.append("     GA.GUARD_TELNO, ");
        //CNT
        stb.append("     VALUE(C0.CNT, 0) AS CNT0, ");
        stb.append("     VALUE(C1.CNT, 0) AS CNT1, ");
        stb.append("     VALUE(C2.CNT, 0) AS CNT2, ");
        stb.append("     VALUE(C3.CNT, 0) AS CNT3, ");
        stb.append("     VALUE(C4.CNT, 0) AS CNT4, ");
        stb.append("     VALUE(C5.CNT, 0) AS CNT5, ");
        stb.append("     VALUE(C0.IDOU_NAME, '在籍') AS IDOU_NAME0, ");
        stb.append("     VALUE(C1.IDOU_NAME, '留学') AS IDOU_NAME1, ");
        stb.append("     VALUE(C2.IDOU_NAME, '休学') AS IDOU_NAME2, ");
        stb.append("     VALUE(C3.IDOU_NAME, '退学') AS IDOU_NAME3, ");
        stb.append("     VALUE(C4.IDOU_NAME, '転学') AS IDOU_NAME4, ");
        stb.append("     VALUE(C5.IDOU_NAME, '復学') AS IDOU_NAME5 ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT RD ");
        stb.append("     INNER JOIN SCHREG_BASE_MST BM ON BM.SCHREGNO = RD.SCHREGNO ");
        stb.append("     INNER JOIN SCHREG_REGD_GDAT RG ON RG.YEAR = RD.YEAR AND RG.GRADE = RD.GRADE ");
        stb.append("     INNER JOIN SCHREG_REGD_HDAT RH ON RH.YEAR = RD.YEAR AND RH.SEMESTER = RD.SEMESTER AND RH.GRADE = RD.GRADE AND RH.HR_CLASS = RD.HR_CLASS ");
        stb.append("     LEFT JOIN STAFF_MST SM ON SM.STAFFCD = RH.TR_CD1 ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FM ON FM.FINSCHOOLCD = BM.FINSCHOOLCD ");
        stb.append("     LEFT JOIN ( ");
        stb.append("         SELECT ");
        stb.append("             SCHREGNO, ");
        stb.append("             MAX(ISSUEDATE) AS ISSUEDATE ");
        stb.append("         FROM ");
        stb.append("             SCHREG_ADDRESS_DAT ");
        stb.append("         GROUP BY ");
        stb.append("             SCHREGNO ");
        stb.append("     ) AD_MAX ON AD_MAX.SCHREGNO = RD.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_ADDRESS_DAT AD ON AD.SCHREGNO = AD_MAX.SCHREGNO AND AD.ISSUEDATE = AD_MAX.ISSUEDATE ");
        stb.append("     LEFT JOIN GUARDIAN_DAT GD ON GD.SCHREGNO = RD.SCHREGNO ");
        stb.append("     LEFT JOIN ( ");
        stb.append("         SELECT ");
        stb.append("             SCHREGNO, ");
        stb.append("             MAX(ISSUEDATE) AS ISSUEDATE ");
        stb.append("         FROM ");
        stb.append("             GUARDIAN_ADDRESS_DAT ");
        stb.append("         GROUP BY ");
        stb.append("             SCHREGNO ");
        stb.append("     ) GA_MAX ON GA_MAX.SCHREGNO = RD.SCHREGNO ");
        stb.append("     LEFT JOIN GUARDIAN_ADDRESS_DAT GA ON GA.SCHREGNO = GA_MAX.SCHREGNO AND GA.ISSUEDATE = GA_MAX.ISSUEDATE ");
        //CNT
        stb.append("     LEFT JOIN TBL_IDOU_CNT C0 ON C0.GRADE = RD.GRADE AND C0.HR_CLASS = RD.HR_CLASS AND C0.ORDER_CD = '0' AND C0.IDOU_DIV = '0' ");
        stb.append("     LEFT JOIN TBL_IDOU_CNT C1 ON C1.GRADE = RD.GRADE AND C1.HR_CLASS = RD.HR_CLASS AND C1.ORDER_CD = '1' AND C1.IDOU_DIV = '1' ");
        stb.append("     LEFT JOIN TBL_IDOU_CNT C2 ON C2.GRADE = RD.GRADE AND C2.HR_CLASS = RD.HR_CLASS AND C2.ORDER_CD = '1' AND C2.IDOU_DIV = '2' ");
        stb.append("     LEFT JOIN TBL_IDOU_CNT C3 ON C3.GRADE = RD.GRADE AND C3.HR_CLASS = RD.HR_CLASS AND C3.ORDER_CD = '2' AND C3.IDOU_DIV = '2' ");
        stb.append("     LEFT JOIN TBL_IDOU_CNT C4 ON C4.GRADE = RD.GRADE AND C4.HR_CLASS = RD.HR_CLASS AND C4.ORDER_CD = '2' AND C4.IDOU_DIV = '3' ");
        stb.append("     LEFT JOIN TBL_IDOU_CNT C5 ON C5.GRADE = RD.GRADE AND C5.HR_CLASS = RD.HR_CLASS AND C5.ORDER_CD = '3' AND C5.IDOU_DIV = '1' ");
        stb.append(" WHERE ");
        stb.append("     RD.YEAR = '" + param._year + "' ");
        stb.append("     AND RD.SEMESTER = '" + param._semester + "' ");
        stb.append("     AND RD.GRADE = '" + param._grade + "' ");
        stb.append("     AND RD.GRADE || RD.HR_CLASS IN " + SQLUtils.whereIn(true, param._category_selected) + " ");
        stb.append(" ORDER BY ");
        if ("2".equals(param._form)) {
            stb.append("     AD.ZIPCD, ");
        }
        stb.append("     RD.GRADE, ");
        stb.append("     RD.HR_CLASS, ");
        stb.append("     RD.ATTENDNO ");
        return stb.toString();
    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _semester;
        final String _form; // 1:生徒マスタ一覧表 2:都県住所別生徒マスタ一覧表
        final String _kijunDate;
        final String _grade;
        final String[] _category_selected;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("CTRL_SEMESTER");
            _form = request.getParameter("FORM");
            _kijunDate = StringUtils.defaultString(request.getParameter("DATE")).replace('/', '-');
            _grade = request.getParameter("GRADE");
            _category_selected = request.getParameterValues("CATEGORY_SELECTED");
            for (int i = 0; i < _category_selected.length; i++) {
                _category_selected[i] = StringUtils.split(_category_selected[i], "-")[0];
            }
        }
    }
}

// eof
