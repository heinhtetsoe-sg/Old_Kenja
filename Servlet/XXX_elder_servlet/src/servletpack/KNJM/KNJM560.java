// kanji=漢字
/*
 * $Id: 8193015078db9ca28b55838ad30fd71306d98a27 $
 *
 * 作成日: 2009/02/13 14:42:08 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2014 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJM;

import java.io.IOException;

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
import servletpack.KNJZ.detail.KNJ_Get_Info;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 8193015078db9ca28b55838ad30fd71306d98a27 $
 */
public class KNJM560 {

    private static final Log log = LogFactory.getLog("KNJM560.class");

    private static final String FORM_NAME = "KNJM560.frm";
    private boolean _hasData;

    Param _param;
    /**
     * KNJW.classから呼ばれる処理
     * @param request リクエスト
     * @param response レスポンス
     * @throws Exception I/O例外
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
            closeSvf(svf);
        }

    }

    /**
     * @param db2
     * @param svf
     */
    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        svf.VrSetForm(FORM_NAME, 4);

        final String mainSql = getChairStdSql();
        ResultSet rs = null;
        try {
            int pageCnt = 1;
            int dataCnt = 1;
            String befHrClass = "";
            db2.query(mainSql);
            rs = db2.getResultSet();
            while (rs.next()) {
                final String gradeHr = rs.getString("GRADE") + rs.getString("HR_CLASS");
                if (_param._sort.equals("1")) {
                    if (!befHrClass.equals("") && !befHrClass.equals(gradeHr) && dataCnt > 1) {
                        for (int i = dataCnt; i <= 4; i++) {
                            svf.VrsOut("NENDO", _param._year + "年度");
                            svf.VrsOut("CHAIRNAME", _param._chairCd + " " + _param._chirName);
                            svf.VrsOut("DATE", _param._date);
                            svf.VrEndRecord();
                            pageCnt++;
                        }
                        dataCnt = 1;
                    }
                }
                svf.VrsOut("NENDO", _param._year + "年度");
                svf.VrsOut("CHAIRNAME", _param._chairCd + " " + _param._chirName);
                svf.VrsOut("DATE", _param._date);
                int page = pageCnt / 48;
                page = page * 48 < pageCnt ? page + 1 : page;
                svf.VrsOut("PAGE", String.valueOf(page));
                svf.VrsOut("SORT", _param.getSortName());

                if ("3".equals(_param._sort)) {
                    svf.VrsOut("HR_NAME", rs.getString("SCHREGNO"));
                } else {
                    svf.VrsOut("HR_NAME", StringUtils.defaultString(rs.getString("HR_NAMEABBV")) + " " + StringUtils.defaultString(rs.getString("ATTENDNO")));
                }
                if (null != rs.getString("NAME")) {
                    final String field = rs.getString("NAME").length() > 6 ? "2" : "1";
                    svf.VrsOut("NAME" + field, rs.getString("NAME"));
                }
                svf.VrsOut("BARCODE", rs.getString("SCHREGNO"));
                befHrClass = gradeHr;
                dataCnt = dataCnt < 4 ? dataCnt + 1 : 1;
                svf.VrEndRecord();
                pageCnt++;
                _hasData = true;
            }
        } finally {
            // TODO: handle exception
        }
    }

    private String getChairStdSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     L1.SCHREGNO, ");
        stb.append("     L1.NAME, ");
        stb.append("     L1.NAME_KANA, ");
        stb.append("     L2.GRADE, ");
        stb.append("     L2.HR_CLASS, ");
        stb.append("     L3.HR_NAMEABBV, ");
        stb.append("     L2.ATTENDNO ");
        stb.append(" FROM ");
        stb.append("     CHAIR_STD_DAT T1 ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST L1 ON T1.SCHREGNO = L1.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT L2 ON L2.YEAR = '" + _param._year + "' ");
        stb.append("          AND L2.SEMESTER = '" + _param._semester + "' ");
        stb.append("          AND T1.SCHREGNO = L2.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT L3 ON L2.YEAR = L3.YEAR ");
        stb.append("          AND L2.SEMESTER = L3.SEMESTER ");
        stb.append("          AND L2.GRADE = L3.GRADE ");
        stb.append("          AND L2.HR_CLASS = L3.HR_CLASS ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.CHAIRCD = '" + _param._chairCd + "' ");
        stb.append(" ORDER BY ");
        if (_param._sort.equals("1")) {
            stb.append("     L2.GRADE, ");
            stb.append("     L2.HR_CLASS, ");
            stb.append("     L2.ATTENDNO ");
        } else if (_param._sort.equals("2")) {
            stb.append("     L1.NAME_KANA, ");
            stb.append("     L2.GRADE, ");
            stb.append("     L2.HR_CLASS, ");
            stb.append("     L2.ATTENDNO ");
        } else {
            if ("miyagiken".equals(_param._nameMstZ010Name1)) {
                stb.append("     SUBSTR(T1.SCHREGNO, 1, 4) DESC, ");
                stb.append("     SUBSTR(T1.SCHREGNO, 5, 4) ");
            } else {
                stb.append("     T1.SCHREGNO ");
            }
        }

        return stb.toString();
    }

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }
    private void closeSvf(final Vrw32alp svf) {
        if (!_hasData) {
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note", "note");
            svf.VrEndPage();
        }

        final int ret = svf.VrQuit();
        log.info("===> VrQuit():" + ret);
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _programid;
        private final String _year;
        private final String _semester;
        private final String _chairCd;
        private final String _chirName;
        private final String _sort;
        private final String _date;
        private final String _nameMstZ010Name1;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _programid = request.getParameter("PRGID");
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _chairCd = request.getParameter("CHAIRCD");
            _sort = request.getParameter("SORT");

            _chirName = getChairName(db2, _year, _semester, _chairCd);
            KNJ_Get_Info get_Info = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnVal = null;
            returnVal = get_Info.Control(db2);
            _date = KNJ_EditDate.h_format_thi(returnVal.val3, 0);
            _nameMstZ010Name1 = getNameMstZ010Name1(db2);
        }

        public String getSortName() {
            return _sort.equals("1") ? "クラス番号順" : _sort.equals("2") ? "あいうえお順" : "学籍番号順";
        }

        private String getChairName(
                final DB2UDB db2,
                final String year,
                final String semester,
                final String chairCd
        ) throws SQLException {
            String retSt = "";

            final String chairSql = "SELECT CHAIRNAME FROM CHAIR_DAT "
                                  + " WHERE YEAR = '" + _year + "' "
                                  + "       AND SEMESTER = '" + _semester + "' "
                                  + "       AND CHAIRCD = '" + _chairCd + "' ";

            ResultSet rs = null;
            try {
                db2.query(chairSql);
                rs = db2.getResultSet();
                while (rs.next()) {
                    retSt = rs.getString("CHAIRNAME");
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(rs);
            }
            return retSt;
        }
        

        private String getNameMstZ010Name1(
                final DB2UDB db2
        ) throws SQLException {
            String retSt = "";

            final String nameMstSql = "SELECT NAME1 FROM NAME_MST "
                                  + " WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ";

            ResultSet rs = null;
            try {
                db2.query(nameMstSql);
                rs = db2.getResultSet();
                while (rs.next()) {
                    retSt = rs.getString("NAME1");
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(rs);
            }
            return retSt;
        }
    }

}
 // KNJM560

// eof
