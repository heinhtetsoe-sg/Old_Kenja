// kanji=漢字
/*
 * $Id: 11bd0d67423d91c9e47029d4389ff41e27746cca $
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

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: 11bd0d67423d91c9e47029d4389ff41e27746cca $
 */
public class KNJE370 {

    private static final Log log = LogFactory.getLog("KNJE370.class");
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
        if (_param._isPrintSingaku) {
            KNJE370_1 printClass = new KNJE370_1(_param, db2, svf);
            _hasData = printClass.printMain(db2);
        } else {
            KNJE370_2 printClass = new KNJE370_2(_param, db2, svf);
            _hasData = printClass.printMain(db2);
        }
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
        log.fatal("$Revision: 70043 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    public class Param {
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _year;
        final String _semester;
        final String _ctrlDate;
        final String _classSelectedIn;
        final String[] _typeSelected;

        final boolean _isPrintSingaku;//進学ならtrue、就職ならfalse
        final String _senkouKind;//進学なら'0'、就職なら'1'
        final String _dataDiv;  //1:クラス指定 2:個人指定
        final String _kaipage;  //1生徒毎に改ページ

        final int _typeSelMaxCnt; //種別(設置区分)の最大選択件数

        String _gouhiCd1;//「E005 or E006」
        String _gouhiCd2;
        String _gouhiCd3;
        String _gouhiName;
        String _gouhiName2;
        String _kubunName;

        private boolean _isSeireki;
        boolean _isKisotsu = false;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");

            final String[] classSelected = request.getParameterValues("CLASS_SELECTED");
            _classSelectedIn = getClassSelectedIn(classSelected);

            String outDiv = request.getParameter("OUT_DIV");
            _isPrintSingaku = "1".equals(outDiv);
            _senkouKind = (_isPrintSingaku) ? "0" : "1";

            _dataDiv = request.getParameter("DATA_DIV");
            _kaipage = request.getParameter("KAIPAGE");
            String[] gouhi = StringUtils.split(request.getParameter("GOUHI"), "-");
            _gouhiCd1 = gouhi[0];
            _gouhiCd2 = gouhi[1];
            if ("MIX".equals(_gouhiCd2)) {
                _gouhiCd3 = gouhi[2];
                _gouhiName = getGouhiKubunName2(db2, _gouhiCd1, _gouhiCd3, "NAME1");
                _gouhiName2 = getGouhiKubunName2(db2, _gouhiCd1, _gouhiCd3, "NAME2");
            } else {
                _gouhiCd3 = "";
                _gouhiName = getGouhiKubunName(db2, _gouhiCd1, _gouhiCd2, "NAME1");
                _gouhiName2 = getGouhiKubunName(db2, _gouhiCd1, _gouhiCd2, "NAME2");
            }
            _typeSelMaxCnt = Integer.parseInt(request.getParameter("SELECTDATA_TYPE_CNT"));
            _typeSelected = request.getParameterValues("CATEGORY_SELECTED_TYPE");
            if (_isPrintSingaku) {
                if (_typeSelMaxCnt == _typeSelected.length) {
                    //全てとして扱う。
                    _kubunName = "全て";
                } else {
                    _kubunName = "";
                    String delimstr = "";
                    for (int ii = 0; ii < _typeSelected.length;ii++) {
                        if (ii >= 2) {
                            continue;
                        }
                        String kubun = _typeSelected[ii];
                        final String kubunCd1 = kubun.substring(0,4);
                        final String kubunCd2 = kubun.substring(5);
                        _kubunName += delimstr + getGouhiKubunName(db2, kubunCd1, kubunCd2, "NAME1");
                        if (_typeSelected.length > 2 && ii+1 >= 2) {
                            _kubunName += delimstr + "他";
                        }
                        delimstr = "、";
                    }
                }
            } else {
                _kubunName = "就職";
            }
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


        private String getGouhiKubunName2(final DB2UDB db2, String cd1, String namespare, final String field) {
            String rtnName = "";
            String sep = "";
            try {
                String sql = "SELECT " + field + " FROM NAME_MST WHERE NAMECD1='" + cd1 + "' AND NAMESPARE2='" + namespare + "' ";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                while( rs.next() ){
                    final String name = StringUtils.defaultString(rs.getString(field), "");
                    if(!"".equals(name)) {
                        rtnName = rtnName + sep + name;
                        sep = "・";
                    }
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

        public String changePrintDate(final DB2UDB db2, final String date) {
            if (null == date) {
                return "";
            }
            if (_isSeireki) {
                return date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date);
            } else {
                return KNJ_EditDate.h_format_JP(db2, date);
            }
        }

        public String changePrintYear(final DB2UDB db2, final String year) {
            if (null == year) {
                return "";
            }
            if (_isSeireki) {
                return year + "年度";
            } else {
                return KNJ_EditDate.gengou(db2, Integer.parseInt(year)) + "年度";
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
