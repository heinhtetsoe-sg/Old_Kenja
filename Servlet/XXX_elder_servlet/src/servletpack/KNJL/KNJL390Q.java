/*
 * $Id: a0e073e4bbd76429afd327771042305db51c3a67 $
 *
 * 作成日: 2017/03/17
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJL390Q {

    private static final Log log = LogFactory.getLog(KNJL390Q.class);

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

            if ("1".equals(_param._check0)) {
                printMain(db2, svf, "1");
            }
            if ("1".equals(_param._check1)) {
                printMain(db2, svf, "2");
            }
            if ("1".equals(_param._check2)) {
                printMain(db2, svf, "3");
            }
            if ("1".equals(_param._check3)) {
                printMain(db2, svf, "4");
            }
            if ("1".equals(_param._check4)) {
                printMain(db2, svf, "5");
            }
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

    private void printMain(final DB2UDB db2, final Vrw32alp svf, final String injiPatern) {
        printHyoushi(db2, svf, injiPatern);
        svf.VrSetForm("KNJL390Q_2.frm", 4);

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = sql(injiPatern);
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            setTitle(svf, injiPatern);

            int pageCnt = 1;
            int maxLine = 30;
            int lineCnt = 1;
            String befPlacecd = "";
            while (rs.next()) {

                final String rowNum = rs.getString("ROW_NUM");
                final String satNo = rs.getString("SAT_NO");
                final String name1 = rs.getString("NAME1");
                final String kana1 = rs.getString("KANA1");
                final String kana2 = rs.getString("KANA2");
                final String sex = rs.getString("SEX");
                final String birthday = rs.getString("BIRTHDAY");
                final String telno1 = rs.getString("TELNO1");
                final String finschoolNameAbbv = rs.getString("FINSCHOOL_NAME_ABBV");
                final String groupname = rs.getString("GROUPNAME");
                final String placecd = rs.getString("PLACECD");
                final String placearea = rs.getString("PLACEAREA");
                final String cnt = rs.getString("CNT");
                final String indKubun = rs.getString("IND_KUBUN");
                final String dantaiMoney = rs.getString("DANTAI_MONEY");
                final String kojinMoney = rs.getString("KOJIN_MONEY");
                final String kounaiseiMoney = rs.getString("KOUNAISEI_MONEY");

                if (lineCnt > maxLine) {
                    lineCnt = 1;
                    pageCnt++;
                }
                final int totalCnt = rs.getInt("CNT");
                final int pageSu = totalCnt / maxLine;
                final int pageAmari = totalCnt % maxLine;
                final int totalPage = pageSu + (pageAmari > 0 ? 1 : 0);
                svf.VrsOut("PAGE1", String.valueOf(pageCnt));
                svf.VrsOut("PAGE2", String.valueOf(totalPage));

                if (!"".equals(befPlacecd) && !befPlacecd.equals(placecd)) {
                    lineCnt = 1;
                    pageCnt = 1;
                    svf.VrEndPage();
                    svf.VrSetForm("KNJL390Q_2.frm", 4);
                    setTitle(svf, injiPatern);
                }
                svf.VrsOut("NUM", cnt);

                svf.VrsOut("SUBTITLE", placearea + "　会場");

                svf.VrsOut("NO", rowNum);
                svf.VrsOut("EXAM_NO", satNo);
                svf.VrsOut("NAME", name1);
                svf.VrsOut("KANA", kana1 + "　" + kana2);
                svf.VrsOut("SEX", sex);
                svf.VrsOut("BIRTHDAY", birthday);
                svf.VrsOut("TEL_NO", telno1);
                svf.VrsOut("ATTEND_SCHOOL_NAME", finschoolNameAbbv);
                if ("1".equals(injiPatern) || "2".equals(injiPatern)) {
                    svf.VrsOut("GROUP_NAME", groupname);
                } else {
                    if ("1".equals(indKubun)) {
                        svf.VrsOut("GROUP_NAME", kojinMoney);
                    }
                    if ("2".equals(indKubun)) {
                        svf.VrsOut("GROUP_NAME", dantaiMoney);
                    }
                    if ("3".equals(indKubun)) {
                        svf.VrsOut("GROUP_NAME", kounaiseiMoney);
                    }
                }

                svf.VrEndRecord();
                lineCnt++;
                befPlacecd = placecd;
                _hasData = true;
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private void setTitle(final Vrw32alp svf, final String injiPatern) {
        final String nendo = _param._ctrlYear + "年度 ";
        svf.VrsOut("TITLE", nendo + "　駿台甲府高校実戦模試　会場別名簿");
        svf.VrsOut("DATE", KNJ_EditDate.h_format_SeirekiJP(_param._ctrlDate));
        String setBikouTitle = "団体名";
        if (!"1".equals(injiPatern) && !"2".equals(injiPatern)) {
            setBikouTitle = "受験料　　　　　　　　　　備考";
        }
        svf.VrsOut("GROUP_TITLE", setBikouTitle);
    }

    private void printHyoushi(final DB2UDB db2, final Vrw32alp svf, final String injiPatern) {

        svf.VrSetForm("KNJL390Q_1.frm", 1);

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String nendo = _param._ctrlYear + "年度 ";
            svf.VrsOut("NENDO", nendo);
            String setTitle = "会場別受験者名簿";
            if ("3".equals(injiPatern) || "4".equals(injiPatern) || "5".equals(injiPatern)) {
                setTitle = "会場別未徴収受付名簿";
                if ("3".equals(injiPatern)) {
                	setTitle += "[個人]";
                } else if ("4".equals(injiPatern)) {
                	setTitle += "[団体]";
                }
            }
            svf.VrsOut("SUBTITLE", setTitle);
            svf.VrsOut("PRINT_DATE", StringUtils.replace(_param._ctrlDate, "-", "/") + "版");
            final String week = KNJ_EditDate.h_format_W(_param._examdate);
            svf.VrsOut("EXEC_DATE", KNJ_EditDate.h_format_SeirekiJP(_param._examdate) + "（" + week + "）実施");

            final String sql = sqlTitle(injiPatern);
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            int lineCnt = 1;
            while (rs.next()) {
                final String placeCnt = rs.getString("PLACE_CNT");
                final String allCnt = rs.getString("ALL_CNT");
                final String placearea = rs.getString("PLACEAREA");

                svf.VrsOutn("PLACE_NAME", lineCnt, placearea);
                svf.VrsOutn("NUM", lineCnt, placeCnt + " 名");
                svf.VrsOut("TOTAL", allCnt + " 名");

                lineCnt++;
                _hasData = true;
            }

            svf.VrEndPage();
            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String sqlTitle(final String injiPatern) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     t1.YEAR, ");
        stb.append("     t4.EXAM_DATE, ");
        stb.append("     t1.PLACECD, ");
        stb.append("     t1.PLACE_CNT, ");
        stb.append("     t2.ALL_CNT, ");
        stb.append("     t3.PLACEAREA ");
        stb.append(" FROM ");
        stb.append("     (SELECT ");
        stb.append("         YEAR, ");
        stb.append("         PLACECD, ");
        stb.append("         SUM(PLACE_CNT) as PLACE_CNT ");
        stb.append("     FROM ");
        stb.append("         (SELECT ");
        stb.append("             YEAR, ");
        stb.append("             case when PLACECD >= '19' AND PLACECD != '80' then '99' else PLACECD end as PLACECD, ");
        stb.append("             COUNT(SAT_NO) as PLACE_CNT ");
        stb.append("         FROM ");
        stb.append("             SAT_APP_FORM_MST ");
        stb.append("         WHERE ");
        stb.append("             YEAR = '" + _param._ctrlYear + "' ");
        if ("2".equals(_param._place)) {
            stb.append("         AND PLACECD = '" + _param._placeComb + "' ");
        }
        if (!"1".equals(injiPatern) && !"2".equals(injiPatern)) {
            stb.append("         AND SEND_KUBUN = '1' ");
            if ("3".equals(injiPatern)) {
                stb.append("         AND IND_KUBUN = '1' ");
            }
            if ("4".equals(injiPatern)) {
                stb.append("         AND IND_KUBUN = '2' ");
            }
            if ("5".equals(injiPatern)) {
                stb.append("         AND IND_KUBUN = '3' ");
                stb.append("         AND INOUT_KUBUN != '4' ");
            }
        }
        stb.append("         GROUP BY ");
        stb.append("             YEAR, ");
        stb.append("             PLACECD ");
        stb.append("         ) MAINT1 ");
        stb.append("     GROUP BY ");
        stb.append("         YEAR, ");
        stb.append("         PLACECD ");
        stb.append("     ) t1 ");
        stb.append("     left join (SELECT ");
        stb.append("                     YEAR, ");
        stb.append("                     COUNT(*) as ALL_CNT ");
        stb.append("                 FROM ");
        stb.append("                     SAT_APP_FORM_MST ");
        stb.append("                 WHERE ");
        stb.append("                     YEAR = '" + _param._ctrlYear + "' ");
        if ("2".equals(_param._place)) {
            stb.append("         AND PLACECD = '" + _param._placeComb + "' ");
        }
        if (!"1".equals(injiPatern) && !"2".equals(injiPatern)) {
            stb.append("                 AND SEND_KUBUN = '1' ");
            if ("3".equals(injiPatern)) {
                stb.append("                 AND IND_KUBUN = '1' ");
            }
            if ("4".equals(injiPatern)) {
                stb.append("                 AND IND_KUBUN = '2' ");
            }
            if ("5".equals(injiPatern)) {
                stb.append("                 AND IND_KUBUN = '3' ");
                stb.append("                 AND INOUT_KUBUN != '4' ");
            }
        }
        stb.append("                 GROUP BY ");
        stb.append("                     YEAR ");
        stb.append("                 ) t2 on t1.YEAR = t2.YEAR ");
        stb.append("     left join (SELECT ");
        stb.append("                     PLACECD, ");
        stb.append("                     PLACEAREA ");
        stb.append("                 FROM ");
        stb.append("                     SAT_EXAM_PLACE_DAT ");
        stb.append("                 WHERE ");
        stb.append("                     YEAR = '" + _param._ctrlYear + "' ");
        stb.append("                 AND ");
        stb.append("                     PLACECD < '19' OR PLACECD = '80' ");
        stb.append("                 UNION ");
        stb.append("                 SELECT ");
        stb.append("                     '99' AS PLACECD, ");
        stb.append("                     '海外' AS PLACEAREA ");
        stb.append("                 FROM ");
        stb.append("                     SAT_EXAM_PLACE_DAT ");
        stb.append("                 ) t3 on t1.PLACECD = t3.PLACECD ");
        stb.append("     left join SAT_INFO_MST t4 on t1.YEAR = t4.YEAR ");
        stb.append(" ORDER BY ");
        stb.append("     t1.PLACECD ");
        return stb.toString();
    }

    private String sql(final String injiPatern) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        if ("2".equals(injiPatern)) {
            stb.append("     ROW_NUMBER() OVER(PARTITION BY t1.PLACECD ORDER BY t1.KANA1, t1.KANA2) AS ROW_NUM, ");
        } else {
            stb.append("     ROW_NUMBER() OVER(PARTITION BY t1.PLACECD ORDER BY t1.SAT_NO) AS ROW_NUM, ");
        }
        stb.append("     t1.YEAR, ");
        stb.append("     t1.SAT_NO, ");
        stb.append("     t1.NAME1, ");
        stb.append("     t1.KANA1, ");
        stb.append("     t1.KANA2, ");
        stb.append("     t4.NAME2 as SEX, ");
        stb.append("     t1.BIRTHDAY, ");
        stb.append("     t1.TELNO1, ");
        stb.append("     t1.SCHOOLCD, ");
        stb.append("     t2.FINSCHOOL_NAME_ABBV, ");
        stb.append("     t1.GROUPCD, ");
        stb.append("     t3.GROUPNAME, ");
        stb.append("     t1.PLACECD, ");
        stb.append("     t5.PLACEAREA, ");
        stb.append("     t1.IND_KUBUN, ");
        stb.append("     VALUE(SAT_INFO.EXAM_AMOUNT1, 0) AS DANTAI_MONEY, ");
        stb.append("     VALUE(SAT_INFO.EXAM_AMOUNT2, 0) AS KOJIN_MONEY, ");
        stb.append("     VALUE(SAT_INFO.EXAM_AMOUNT3, 0) AS KOUNAISEI_MONEY, ");
        stb.append("     t6.CNT ");
        stb.append(" FROM ");
        stb.append("     ( ");
        stb.append("     SELECT ");
        stb.append("         YEAR, ");
        stb.append("         SAT_NO, ");
        stb.append("         IND_KUBUN, ");
        stb.append("         NAME1, ");
        stb.append("         KANA1, ");
        stb.append("         KANA2, ");
        stb.append("         SEX, ");
        stb.append("         substr(replace(CAST(BIRTHDAY AS VARCHAR(10)), '-', ''), 3, 6) as BIRTHDAY, ");
        stb.append("         TELNO1, ");
        stb.append("         PREFCD, ");
        stb.append("         PLACECD, ");
        stb.append("         SCHOOLCD, ");
        stb.append("         GROUPCD ");
        stb.append("     FROM ");
        stb.append("         SAT_APP_FORM_MST ");
        stb.append("     WHERE ");
        stb.append("         YEAR = '" + _param._ctrlYear + "' ");
        if ("2".equals(_param._place)) {
            stb.append("         AND PLACECD = '" + _param._placeComb + "' ");
        }
        if (!"1".equals(injiPatern) && !"2".equals(injiPatern)) {
            stb.append("         AND SEND_KUBUN = '1' ");
            if ("3".equals(injiPatern)) {
                stb.append("         AND IND_KUBUN = '1' ");
            }
            if ("4".equals(injiPatern)) {
                stb.append("         AND IND_KUBUN = '2' ");
            }
            if ("5".equals(injiPatern)) {
                stb.append("         AND IND_KUBUN = '3' ");
                stb.append("         AND INOUT_KUBUN != '4' ");
            }
        }
        stb.append("     ) t1 ");
        stb.append("     left join FINSCHOOL_MST t2 on t1.SCHOOLCD = t2.FINSCHOOLCD and t2.FINSCHOOL_TYPE = '3' ");
        stb.append("     left join SAT_GROUP_DAT t3 on t1.GROUPCD = t3.GROUPCD and t3.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     left join NAME_MST t4 on t1.SEX = t4.NAMECD2 and t4.NAMECD1 = 'Z002' ");
        stb.append("     left join SAT_EXAM_PLACE_DAT t5 on t1.PLACECD = t5.PLACECD and t5.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     left join ( ");
        stb.append("         SELECT ");
        stb.append("             YEAR, ");
        stb.append("             PLACECD, ");
        stb.append("             COUNT(*) as CNT ");
        stb.append("         FROM ");
        stb.append("             SAT_APP_FORM_MST ");
        stb.append("         WHERE ");
        stb.append("             YEAR = '" + _param._ctrlYear + "' ");
        if ("2".equals(_param._place)) {
            stb.append("         AND PLACECD = '" + _param._placeComb + "' ");
        }
        if (!"1".equals(injiPatern) && !"2".equals(injiPatern)) {
            stb.append("         AND SEND_KUBUN = '1' ");
            if ("3".equals(injiPatern)) {
                stb.append("         AND IND_KUBUN = '1' ");
            }
            if ("4".equals(injiPatern)) {
                stb.append("         AND IND_KUBUN = '2' ");
            }
            if ("5".equals(injiPatern)) {
                stb.append("         AND IND_KUBUN = '3' ");
                stb.append("         AND INOUT_KUBUN != '4' ");
            }
        }
        stb.append("         GROUP BY ");
        stb.append("             YEAR, ");
        stb.append("             PLACECD ");
        stb.append("     ) t6 on t1.YEAR = t6.YEAR and t1.PLACECD = t6.PLACECD ");
        stb.append("     left join SAT_INFO_MST SAT_INFO on t1.YEAR = SAT_INFO.YEAR ");
        stb.append(" ORDER BY ");
        stb.append("     PLACECD, ");
        if ("2".equals(injiPatern)) {
            stb.append("     KANA1, ");
            stb.append("     KANA2, ");
        }
        stb.append("     SAT_NO ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 62117 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _check0;
        private final String _check1;
        private final String _check2;
        private final String _check3;
        private final String _check4;
        private final String _place;
        private final String _placeComb;
        private final String _examdate;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _check0 = request.getParameter("CHECK0");
            _check1 = request.getParameter("CHECK1");
            _check2 = request.getParameter("CHECK2");
            _check3 = request.getParameter("CHECK3");
            _check4 = request.getParameter("CHECK4");
            _place = request.getParameter("PLACE");
            _placeComb = request.getParameter("PLACE_COMB");
            _examdate = request.getParameter("examDate");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
        }

    }
}

// eof

