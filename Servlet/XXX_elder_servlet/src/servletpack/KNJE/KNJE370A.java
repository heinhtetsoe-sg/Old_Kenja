// kanji=漢字
/*
 * $Id: 0805e6c774fdba7d31517e8b2e43b9b0f34678ac $
 *
 * 作成日: 2009/10/21 8:52:18 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: 0805e6c774fdba7d31517e8b2e43b9b0f34678ac $
 */
public class KNJE370A {

    private static final Log log = LogFactory.getLog("KNJE370A.class");
    private static final String KISOTSU = "ZZZZZ";

    private boolean _hasData;

    Param _param;

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
            log.debug("年組：" + _param._classSelectedIn);

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        KNJE370A_1 printClass = new KNJE370A_1(_param, db2, svf);
        _hasData = printClass.printMain();
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
        log.fatal("$Revision: 58327 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    public class Param {
    	public final String _ctrlYear;
    	public final String _ctrlSemester;
    	public final String _year;
    	public final String _semester;
    	public final String _ctrlDate;
    	public final String[] _classSelected;
    	public final String _classSelectedIn;

    	public final String _dataDiv;  //1:クラス指定 2:個人指定

        private boolean _isSeireki;
        public boolean _isKisotsu = false;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");

            _classSelected = request.getParameterValues("CLASS_SELECTED");
            _classSelectedIn = getClassSelectedIn(_classSelected);

            _dataDiv = request.getParameter("DATA_DIV");

            setSeirekiFlg(db2);
        }

        private String getClassSelectedIn(final String[] classSelected) {
            StringBuffer stb = new StringBuffer();
            stb.append("(");
            for (int i = 0; i < classSelected.length; i++) {
                if (0 < i) stb.append(",");
                stb.append("'" + classSelected[i] + "'");
                if (KISOTSU.equals(classSelected[i])) {
                    _isKisotsu = true;
                }
            }
            stb.append(")");
            return stb.toString();
        }

        private String getGouhiKubunName(final DB2UDB db2, String cd1, String cd2, final String field) {
            if ("ALL".equals(cd2)) {
                return "全て";
            }
            String rtnName = "";
            try {
                String sql = "SELECT " + field + " FROM NAME_MST WHERE NAMECD1='" + cd1 + "' AND NAMECD2='" + cd2 + "' ";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                while( rs.next() ){
                    rtnName = rs.getString(field);
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
            }
            return rtnName;
        }

        private void setSeirekiFlg(final DB2UDB db2) {
            try {
                _isSeireki = false;
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                while( rs.next() ){
                    if (rs.getString("NAME1").equals("2")) _isSeireki = true; //西暦
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
            }
        }

        public String changePrintDate(final String date) {
            if (null == date) {
                return "";
            }
            if (_isSeireki) {
                return date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date);
            } else {
                return KNJ_EditDate.h_format_JP(date);
            }
        }

        public String changePrintYear(final String year) {
            if (null == year) {
                return "";
            }
            if (_isSeireki) {
                return year + "年度";
            } else {
                return nao_package.KenjaProperties.gengou(Integer.parseInt(year)) + "年度";
            }
        }

        public boolean isNamecdE012(final String cd1) {
            return "E012".equals(cd1);
        }

        public boolean isNamecdE005(final String cd1) {
            return "E005".equals(cd1);
        }

        public boolean isNamecdE006(final String cd1) {
            return "E006".equals(cd1);
        }

    }
}

// eof
