// kanji=漢字
/*
 * $Id: e94faf27fb5c116a80ed2af21d1564660445d4d4 $
 *
 * 作成日: 2020/10/08
 * 作成者: shimoji
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJE;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class KNJE377A {
    private static final Log log = LogFactory.getLog(KNJE377A.class);

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

    /*----------------------------*
     * SVF出力                    *
     *----------------------------*/
    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJE377A.frm", 1);

        final String sql;
        if ("1".equals(_param._finschoolDiv)) {
            sql = "SELECT "
                    + "VALUE(FINSCHOOL_ZIPCD,'') AS ZIPCD,"
                    + "VALUE(FINSCHOOL_ADDR1,'') || VALUE(FINSCHOOL_ADDR2,'') AS ADDRESS,"
                    + "VALUE(FINSCHOOL_NAME, '') AS NAME, "
                    + "VALUE(T2.NAME1, '') AS NAME1 "
                + "FROM "
                    + "FINSCHOOL_MST T1 "
                + "LEFT JOIN NAME_MST T2 ON T2.NAMECD1 = 'L019' AND T2.NAMECD2 = T1.FINSCHOOL_TYPE "
                + "WHERE "
                    + "FINSCHOOLCD IN " + _param._classInState + " "
                + "ORDER BY "
                    + "FINSCHOOLCD";
        } else if ("2".equals(_param._finschoolDiv)) {
            sql = "SELECT "
                    + "VALUE(ZIPCD, '') AS ZIPCD,"
                    + "VALUE(ADDR1, '') || VALUE(ADDR2, '') AS ADDRESS,"
                    + "VALUE(SCHOOL_NAME, '') AS NAME, "
                    + "'' AS NAME1 "
                + "FROM "
                    + "COLLEGE_MST "
                + "WHERE "
                    + "SCHOOL_CD IN " + _param._classInState + " "
                + "ORDER BY "
                    + "SCHOOL_CD";
        } else {
            sql = "SELECT "
                    + "VALUE(ZIPCD, '') AS ZIPCD,"
                    + "VALUE(ADDR1, '') || VALUE(ADDR2, '') AS ADDRESS,"
                    + "VALUE(COMPANY_NAME, '') AS NAME, "
                    + "'' AS NAME1 "
                + "FROM "
                    + "COMPANY_MST "
                + "WHERE "
                    + "COMPANY_CD IN " + _param._classInState + " "
                + "ORDER BY "
                    + "COMPANY_CD";
        }
        log.debug(" sql=" + sql);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            int ia = Integer.parseInt(_param._poRow);    //行
            final int iaMax = 6;
            int ib = Integer.parseInt(_param._poCol);    //列
            while (rs.next()) {
                if (ib > 3) {
                    ib = 1;
                    ia++;
                    if (ia > iaMax) {
                        svf.VrEndPage();
                        _hasData = true;
                        ia = 1;
                    }
                }
                svf.VrsOutn("ZIPCODE"   + ib, ia, "〒" + rs.getString("ZIPCD"));   //郵便番号
                if ("1".equals(_param._useAddrField2) && KNJ_EditEdit.getMS932ByteLength(rs.getString("ADDRESS")) > 26 * 4) {
                    svf.VrsOutn("ADDRESS1_" + ib + "_2", ia, rs.getString("ADDRESS"));        //住所
                } else {
                    svf.VrsOutn("ADDRESS1_" + ib, ia, rs.getString("ADDRESS"));        //住所
                }
                String nameSet = rs.getString("NAME") + _param.getSchoolPrint(rs.getString("NAME1"));
                svf.VrsOutn("SCHOOLNAME" + ib, ia, h_finschoolname(nameSet, "御中"));
                ib++;
            }
            if (ib > 1) {
                svf.VrEndPage();
                _hasData = true;
            }
            svf.VrPrint();
        } catch (Exception ex) {
            log.error("read error!", ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

    }   //set_detail1の括り



    /*-----------------------------------------------------------------*
     * 住所の編集
     *-----------------------------------------------------------------*/
    public String h_finschoolname(final String finschoolname1, final String finschoolname2) {
        final StringBuffer finschoolname = new StringBuffer();
        try {
            if (finschoolname1 != null) {
                finschoolname.append(finschoolname1);
                final byte[] SendB = finschoolname1.getBytes();

                int j = 0;
                if (SendB.length > 24 && SendB.length <= 33) j = 1;
                if (SendB.length > 33) j = 2;
                if (SendB.length > 66) j = 3;

                for (int i = SendB.length; i < (33 * j); i++) {
                    finschoolname.append(" ");
                }

                if (j == 0) {
                    finschoolname.append(" ");
                    finschoolname.append(" ");
                }
                if (finschoolname2 != null) {
                    finschoolname.append(finschoolname2);
                }
            }
        } catch (Exception ex) {
            log.error("[LBA130H]h_finschoolname error!", ex);
        }
        return finschoolname.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 77345 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    public class Param {
        final String _year;
        final String _poRow;
        final String _poCol;
        final String[] _classSelected;
        final String _classInState;
        final String _finschoolDiv;
        final String _useAddrField2;
        final String _notPrintFinschooltypeName;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("year");
            _poRow = request.getParameter("POROW");
            _poCol = request.getParameter("POCOL");

            _finschoolDiv = request.getParameter("FINSCHOOLDIV");

            //対象学校コードの編集
            _classSelected = request.getParameterValues("SCHOOL_SELECTED"); // 学校コード
            final StringBuffer sbx = new StringBuffer();
            sbx.append("(");
            for (int ia = 0; ia < _classSelected.length; ia++) {
                if (_classSelected[ia] == null) {
                    break;
                }
                if (ia > 0) {
                    sbx.append(",");
                }
                sbx.append("'");
                sbx.append(_classSelected[ia]);
                sbx.append("'");
            }
            sbx.append(")");
            _classInState = sbx.toString();
            _useAddrField2 = request.getParameter("useAddrField2");
            _notPrintFinschooltypeName = request.getParameter("notPrintFinschooltypeName");
        }

        public String getSchoolPrint(final String name1) {
            final String retSchool;
            if ("2".equals(_finschoolDiv) || "3".equals(_finschoolDiv)) {
                retSchool = "";
            } else {
                if ("1".equals(_notPrintFinschooltypeName)) {
                    retSchool = "";
                } else {
                    retSchool = name1;
                }
            }
            return retSchool;
        }
    }

}   //クラスの括り

