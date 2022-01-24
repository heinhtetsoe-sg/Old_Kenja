/*
 * $Id: 68cec7fc4c008bd64cf2ee17851762f6b9c1a863 $
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
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL389Q {

    private static final Log log = LogFactory.getLog(KNJL389Q.class);

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
                printMain(db2, svf, "KNJL389Q_1");
            }
            if ("1".equals(_param._check1)) {
                printMain(db2, svf, "KNJL389Q_2");
            }
            if ("1".equals(_param._check2)) {
                printMain(db2, svf, "KNJL389Q_3");
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

    private void printMain(final DB2UDB db2, final Vrw32alp svf, final String formName) {
        svf.VrSetForm(formName + ".frm", 4);
        String titleSoeji = "";
        int maxLine = 20;
        if ("KNJL389Q_2".equals(formName)) {
            titleSoeji = "１";
            maxLine = 25;
        }
        if ("KNJL389Q_3".equals(formName)) {
            titleSoeji = "２";
            maxLine = 25;
        }

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String cntSql = sql("CNT");
            log.debug(" cntSql =" + cntSql);
            ps = db2.prepareStatement(cntSql);
            rs = ps.executeQuery();
            rs.next();
            final int totalCnt = rs.getInt("CNT");
            final int pageSu = totalCnt / maxLine;
            final int pageAmari = totalCnt % maxLine;
            final int totalPage = pageSu + (pageAmari > 0 ? 1 : 0);

            final String sql = sql("");
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            final String nendo = _param._ctrlYear + "年度 ";
            svf.VrsOut("TITLE", nendo + "　駿高実戦模試申込チェックリスト" + titleSoeji);
            final String week = KNJ_EditDate.h_format_W(_param._examdate);
            svf.VrsOut("DATE", KNJ_EditDate.h_format_SeirekiJP(_param._examdate) + "（" + week + "）実施");
            svf.VrsOut("APPLY", String.valueOf(totalCnt));

            int pageCnt = 1;
            int lineCnt = 1;
            while (rs.next()) {
                if (lineCnt > maxLine) {
                    lineCnt = 1;
                    pageCnt++;
                }
                svf.VrsOut("PAGE1", String.valueOf(pageCnt));
                svf.VrsOut("PAGE2", String.valueOf(totalPage));
                final String rowNum = rs.getString("ROW_NUM");
                final String satNo = rs.getString("SAT_NO");
                final String name1 = rs.getString("NAME1");
                final String kana1 = rs.getString("KANA1");
                final String kana2 = rs.getString("KANA2");
                final String schoolcd = rs.getString("SCHOOLCD");
                final String finschoolName = rs.getString("FINSCHOOL_NAME");
                final String birthday = rs.getString("BIRTHDAY");
                final String sex = rs.getString("SEX");
                final String grade = rs.getString("GRADE");
                final String zipcode = rs.getString("ZIPCODE");
                final String addr1 = rs.getString("ADDR1");
                final String addr2 = rs.getString("ADDR2");
                final String telno1 = rs.getString("TELNO1");
                final String insiderno = rs.getString("INSIDERNO");
                final String prefName = rs.getString("PREF_NAME");
                final String groupcd = rs.getString("GROUPCD");
                final String groupname = rs.getString("GROUPNAME");
                final String placearea = rs.getString("PLACEAREA");
                final String placecd = rs.getString("PLACECD");
                final String inputDate = rs.getString("INPUT_DATE");

                svf.VrsOut("NO", rowNum);
                svf.VrsOut("EXAM_NO", satNo);
                svf.VrsOut("NAME", name1);
                final String kana = kana1 + "　" + kana2;
                String kanaNo = "";
                if ("KNJL389Q_1".equals(formName)) {
                    kanaNo = (KNJ_EditEdit.getMS932ByteLength(kana) <= 20) ? "1" : "2";
                }
                svf.VrsOut("KANA" + kanaNo, kana);
                svf.VrsOut("ATTEND_SCHOOL_CD", schoolcd);
                svf.VrsOut("DIFF_NOTE", "");
                final int finschoolNameKeta = KNJ_EditEdit.getMS932ByteLength(finschoolName);
                if (finschoolNameKeta > 40) {
                    svf.VrsOut("ATTEND_SCHOOL_NAME3", finschoolName);
                } else if (finschoolNameKeta > 30) {
                    svf.VrsOut("ATTEND_SCHOOL_NAME2", finschoolName);
                } else {
                    svf.VrsOut("ATTEND_SCHOOL_NAME", finschoolName);
                }
                svf.VrsOut("BIRTHDAY", birthday);
                svf.VrsOut("SEX", sex);
                svf.VrsOut("GRD", grade);
                svf.VrsOut("ZIP_NO", zipcode);
                svf.VrsOut("ADDR1", addr1);
                svf.VrsOut("ADDR2", addr2);
                svf.VrsOut("TEL_NO", telno1);
                svf.VrsOut("TEL_NO1", telno1);
                svf.VrsOut("TEL_NO2", insiderno);
                svf.VrsOut("HOMETOWN", prefName);
                svf.VrsOut("GROUP_NO", groupcd);
                svf.VrsOut("GROUP_NAME", groupname);
                svf.VrsOut("PLACE", placearea);
                svf.VrsOut("PLACE_CD", placecd);
                svf.VrsOut("INPUT_DATE", inputDate);

                svf.VrEndRecord();
                lineCnt++;
                _hasData = true;
            }

            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String sql(final String selectDiv) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        if ("CNT".equals(selectDiv)) {
            stb.append("     COUNT(*) AS CNT ");
        } else {
            stb.append("     ROW_NUMBER() OVER(ORDER BY t1.SAT_NO) as ROW_NUM, ");
            stb.append("     t1.SAT_NO, ");
            stb.append("     t1.NAME1, ");
            stb.append("     t1.KANA1, ");
            stb.append("     t1.KANA2, ");
            stb.append("     SUBSTR(t1.SCHOOLCD, 3, 5) as SCHOOLCD, ");
            stb.append("     t2.FINSCHOOL_NAME, ");
            stb.append("     SUBSTR(REPLACE(CAST(t1.BIRTHDAY AS VARCHAR(10)), '-', ''), 3, 6) as BIRTHDAY, ");
            stb.append("     t3.NAME2 as SEX, ");
            stb.append("     t4.NAME1 as GRADE, ");
            stb.append("     REPLACE(t1.ZIPCODE, '-', '') as ZIPCODE, ");
            stb.append("     t1.ADDR1, ");
            stb.append("     t1.ADDR2, ");
            stb.append("     t1.TELNO1, ");
            stb.append("     t1.INSIDERNO, ");
            stb.append("     t5.PREF_NAME, ");
            stb.append("     t1.GROUPCD, ");
            stb.append("     t6.GROUPNAME, ");
            stb.append("     t7.PLACEAREA, ");
            stb.append("     t1.PLACECD, ");
            stb.append("     REPLACE(CAST(t1.INPUT_DATE AS VARCHAR(10)), '-', '/') as INPUT_DATE ");
        }
        stb.append(" FROM ");
        stb.append("     SAT_APP_FORM_MST t1 ");
        stb.append("     left join FINSCHOOL_MST t2 on t1.SCHOOLCD = t2.FINSCHOOLCD and t2.FINSCHOOL_TYPE = '3' ");
        stb.append("     left join NAME_MST t3 on t1.SEX = t3.NAMECD2 and t3.NAMECD1 = 'Z002' ");
        stb.append("     left join NAME_MST t4 on t1.GRADUATION = t4.NAMECD2 and t4.NAMECD1 = 'L205' ");
        stb.append("     left join PREF_MST t5 on t2.FINSCHOOL_PREF_CD = t5.PREF_CD ");
        stb.append("     left join SAT_GROUP_DAT t6 on t1.GROUPCD = t6.GROUPCD and t6.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     left join SAT_EXAM_PLACE_DAT t7 on t1.PLACECD = t7.PLACECD and t7.YEAR = '" + _param._ctrlYear + "' ");
        stb.append(" WHERE ");
        stb.append("     t1.YEAR = '" + _param._ctrlYear + "' ");
        if ("2".equals(_param._place)) {
            stb.append("     AND t1.PLACECD = '" + _param._placeComb + "' ");
        }
        if ("2".equals(_param._input)) {
            if (null != _param._toDate && !"".equals(_param._toDate)) {
                stb.append("     AND t1.INPUT_DATE BETWEEN '" + StringUtils.replace(_param._fromDate, "/", "-") + "' AND '" + StringUtils.replace(_param._toDate, "/", "-") + "' ");
            } else {
                stb.append("     AND t1.INPUT_DATE = '" + StringUtils.replace(_param._fromDate, "/", "-") + "' ");
            }
        }
        if (!"CNT".equals(selectDiv)) {
            stb.append(" ORDER BY ");
            stb.append("     SAT_NO ");
        }
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 62113 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _check0;
        final String _check1;
        final String _check2;
        final String _place;
        final String _placeComb;
        final String _input;
        final String _fromDate;
        final String _toDate;
        final String _examdate;
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _check0 = request.getParameter("CHECK0");
            _check1 = request.getParameter("CHECK1");
            _check2 = request.getParameter("CHECK2");
            _place = request.getParameter("PLACE");
            _placeComb = request.getParameter("PLACE_COMB");
            _input = request.getParameter("INPUT");
            _fromDate = request.getParameter("FROM_DATE");
            _toDate = request.getParameter("TO_DATE");
            _examdate = request.getParameter("examDate");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
        }

    }
}

// eof

