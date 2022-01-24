/*
 * $Id: d4fae857d36204c1d5e90f01cff08764136bd3ad $
 *
 * 作成日: 2018/01/10
 * 作成者: tawada
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJE377 {

    private static final Log log = LogFactory.getLog(KNJE377.class);

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJE377.frm", 1);

        final int poRowMax = 6;
        int poRow = Integer.parseInt(_param._poRow); //行
        int poCol = Integer.parseInt(_param._poCol); //列

        final List printList = getList(db2);
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final PrintData printData = (PrintData) iterator.next();

            if (poCol > 2) {
                poCol = 1;
                poRow++;
                if (poRow > poRowMax) {
                    svf.VrEndPage();
                    poRow = 1;
                }
            }

            svf.VrsOutn("ZIPCODE" + poCol, poRow, printData._zipCd);//郵便番号
            final String addr1Field = KNJ_EditEdit.getMS932ByteLength(printData._addr1) > 50 ? "_3": KNJ_EditEdit.getMS932ByteLength(printData._addr1) > 40 ? "_2": "_1";
            svf.VrsOutn("ADDRESS" + poCol + "_1" + addr1Field, poRow, printData._addr1);//住所１
            final String addr2Field = KNJ_EditEdit.getMS932ByteLength(printData._addr2) > 50 ? "_3": KNJ_EditEdit.getMS932ByteLength(printData._addr2) > 40 ? "_2": "_1";
            svf.VrsOutn("ADDRESS" + poCol + "_2" + addr2Field, poRow, printData._addr2);//住所２
            final String nameCField = KNJ_EditEdit.getMS932ByteLength(printData._name) > 38 ? "_2": "_1";
            svf.VrsOutn("CONPANY_NAME" + poCol + nameCField, poRow, printData._name);//
            final String posiField = KNJ_EditEdit.getMS932ByteLength(printData._position) > 36 ? "_2": "_1";
            svf.VrsOutn("JOB_NAME" + poCol + posiField, poRow, printData._position);//課係
            final String nameField = KNJ_EditEdit.getMS932ByteLength(printData._manager) > 38 ? "_3": KNJ_EditEdit.getMS932ByteLength(printData._manager) > 28 ? "_2": "_1";
            svf.VrsOutn("NAME" + poCol + nameField, poRow, printData._manager + "　様");//担当者

            poCol++;
            _hasData = true;
        }
        if (poCol > 1) {
            svf.VrEndPage();
        }
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String zipCd = rs.getString("SHUSHOKU_ZIPCD");
                final String addr1 = rs.getString("SHUSHOKU_ADDR1");
                final String addr2 = rs.getString("SHUSHOKU_ADDR2");
                final String name = rs.getString("COMPANY_NAME");
                final String position = rs.getString("DEPARTMENT_POSITION");
                final String manager = rs.getString("PERSONNEL_MANAGER");

                final PrintData aaa = new PrintData(zipCd, addr1, addr2, name, position, manager);
                retList.add(aaa);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT DISTINCT ");
        stb.append("     OFFER.SHUSHOKU_ZIPCD, ");
        stb.append("     OFFER.SHUSHOKU_ADDR1, ");
        stb.append("     OFFER.SHUSHOKU_ADDR2, ");
        stb.append("     OFFER.COMPANY_NAME, ");
        stb.append("     VALUE(OFFER.DEPARTMENT_POSITION, '') AS DEPARTMENT_POSITION, ");
        stb.append("     VALUE(OFFER.PERSONNEL_MANAGER, '担当者') AS PERSONNEL_MANAGER ");
        stb.append(" FROM ");
        stb.append("     JOB_OFFER_DAT OFFER  ");
        stb.append("     LEFT JOIN COMPANY_MST COMP ON OFFER.COMPANY_CD = COMP.COMPANY_CD ");
        stb.append(" WHERE ");
        stb.append("         OFFER.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND OFFER.SENKOU_NO IN "+ _param._categorySelectedIn +" ");

        return stb.toString();
    }

    private class PrintData {
        final String _zipCd;
        final String _addr1;
        final String _addr2;
        final String _name;
        final String _position;
        final String _manager;
        public PrintData(
                final String zipCd,
                final String addr1,
                final String addr2,
                final String name,
                final String position,
                final String manager
        ) {
            _zipCd = zipCd;
            _addr1 = addr1;
            _addr2 = addr2;
            _name = name;
            _position = position;
            _manager = manager;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 57892 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _poRow;
        private final String _poCol;
        private final String _categorySelectedIn;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("LOGIN_YEAR");
            _ctrlSemester = request.getParameter("LOGIN_SEMESTER");
            _ctrlDate = request.getParameter("LOGIN_DATE");
            _poRow = request.getParameter("POROW");//行1-6
            _poCol = request.getParameter("POCOL");//列1-2
            final String[] categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _categorySelectedIn = getCategorySelectedIn(categorySelected);
        }

        private String getCategorySelectedIn(final String[] categorySelected) {
            StringBuffer stb = new StringBuffer();
            stb.append("(");
            for (int i = 0; i < categorySelected.length; i++) {
                if (0 < i) stb.append(",");
                stb.append(categorySelected[i]);
            }
            stb.append(")");
            return stb.toString();
        }
    }
}

// eof
