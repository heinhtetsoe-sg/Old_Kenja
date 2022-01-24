/*
 * $Id: 2a92e35122051bb082129430dbdbd502fea0cddc $
 *
 * 作成日: 2017/02/28
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJM;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;

public class KNJM150 {

    private static final Log log = LogFactory.getLog(KNJM150.class);

    private boolean _hasData;
    private DecimalFormat dmf = new DecimalFormat();
    private Calendar cal1 = Calendar.getInstance( );

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {

        svf.VrSetForm("KNJM150.frm", 1);
        PreparedStatement ps1 = null;
        ResultSet rs = null;
        try {
            ps1 = db2.prepareStatement(prestatementReportHead());
            rs = ps1.executeQuery();
            String befSubclasscode = "";
            int lineCnt = 1;
            int maxLineCnt = 6;
            while (rs.next()) {
                //科目コードの変わり目
                if (!"".equals(befSubclasscode) && !befSubclasscode.equals(rs.getString("SUBCLASSCD"))) {
                    svf.VrEndPage();
                    lineCnt = 1;
                }
                if (lineCnt > maxLineCnt) {
                    svf.VrEndPage();
                    lineCnt = 1;
                }

                //科目名
                printSvfSubclassnameOut(svf, "B_SUBCLASS", rs.getString("SUBCLASSNAME"), lineCnt);
                //年度
                svf.VrsOutn("B_NENDO", lineCnt, _param._nendo);
                //回数
                svf.VrsOutn("B_NUMBER", lineCnt, "第" + rs.getString("STANDARD_SEQ") + "回" );
                //バーコード
                final String subclasscd = rs.getString("SUBCLASSCD");
                svf.VrsOutn("B_BARCODE", lineCnt, _param._year.substring(3,4)
                        + subclasscd
                        + dmf.format(rs.getInt("STANDARD_SEQ"))
                        + "0" );

                befSubclasscode = rs.getString("SUBCLASSCD");
                lineCnt++;
                _hasData = true;
            }
            if (_hasData) {
                svf.VrEndPage();
            }
        } finally {
            DbUtils.closeQuietly(null, ps1, rs);
        }
    }

    /**
     *   科目名印刷処理
     */
    private void printSvfSubclassnameOut(final Vrw32alp svf, final String fieldname, final String subclassname, final int lineCnt) {

        if (subclassname == null) return;
        if (subclassname.length() <= 6) {
            svf.VrsOutn(fieldname + "1", lineCnt, subclassname);
        } else {
            svf.VrsOutn(fieldname + "2", lineCnt, subclassname);
        }
    }


    /**
     *  SQL-STATEMENT作成 レポート課題集
     *
     */
    private String prestatementReportHead() {

        final StringBuffer stb = new StringBuffer();

        try{
            stb.append(" SELECT ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     W1.CLASSCD || W1.SCHOOL_KIND || W1.CURRICULUM_CD || W1.SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append("     W1.SUBCLASSCD, ");
            }
            stb.append("       W3.SUBCLASSNAME, W1.STANDARD_SEQ, ");
            stb.append("       W1.STANDARD_DATE ");
            stb.append(" FROM  REP_STANDARDDATE_DAT W1 ");
            stb.append("       LEFT JOIN V_SUBCLASS_MST W3 ON W1.SUBCLASSCD = W3.SUBCLASSCD AND W3.YEAR = '" + _param._year + "' ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("       AND W1.CLASSCD = W3.CLASSCD ");
                stb.append("       AND W1.SCHOOL_KIND = W3.SCHOOL_KIND ");
                stb.append("       AND W1.CURRICULUM_CD = W3.CURRICULUM_CD ");
            }
            stb.append(" WHERE  W1.YEAR='" + _param._year + "' ");
            if (_param._outPut.equals("2")){
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("     AND W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || W1.SUBCLASSCD ='" + _param._kamoku + "' ");
                } else {
                    stb.append("     AND W1.SUBCLASSCD='" + _param._kamoku + "' ");
                }
            }
            stb.append(" ORDER BY ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("       W1.CLASSCD, ");
                stb.append("       W1.SCHOOL_KIND, ");
                stb.append("       W1.CURRICULUM_CD, ");
            }
            stb.append("    W1.SUBCLASSCD, W1.STANDARD_SEQ ");
        } catch( Exception ex ){
            log.error("prestatementReportHead() error!"+ex );
        }
//log.debug("ps1"+stb);
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 64620 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _outPut;
        private final String _useCurriculumcd;
        private String _kamoku;
        private String _nendo;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _outPut = request.getParameter("OUTPUT");
            _kamoku = request.getParameter("KAMOKU");
            _useCurriculumcd = request.getParameter("useCurriculumcd");

            //年度
            try{
                _nendo = nao_package.KenjaProperties.gengou(Integer.parseInt(_year)) + "年度 (" + _year + ")";
            } catch( Exception ex ){
                log.error("setHead error!" + ex);
            }

            if (_nendo == null) {
                _nendo = "";
            }
        }

    }
}

// eof

