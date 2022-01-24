// kanji=漢字
/*
 * $Id: 58bed6d45aa282670e2e19ef65c2af3a7818a4d5 $
 *
 * 作成日: 2009/12/19 16:54:05 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 58bed6d45aa282670e2e19ef65c2af3a7818a4d5 $
 */
public class KNJL324T {

    private static final Log log = LogFactory.getLog("KNJL324T.class");

    private boolean _hasData;
    private static final String FORMNAME = "KNJL324T.frm";
    private static final int LINE_MAX = 5;
    private static final int RETU_MAX = 20;

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
        final List printData = getPrintData(db2);
        for (final Iterator itPrint = printData.iterator(); itPrint.hasNext();) {
            final MajorData majorData = (MajorData) itPrint.next();
            printOut(svf, majorData);
        }
    }

    private void printOut(final Vrw32alp svf, final MajorData majorData) {

        svf.VrSetForm(FORMNAME, 1);
        setHead(svf, majorData);
        int lineCnt = 1;
        int retuCnt = 1;
        for (final Iterator itPrintData = majorData._printData.iterator(); itPrintData.hasNext();) {
            if (lineCnt > LINE_MAX) {
                lineCnt = 1;
                retuCnt++;
                if (retuCnt > RETU_MAX) {
                    svf.VrEndPage();
                    setHead(svf, majorData);
                    retuCnt = 1;
                }
            }
            final String examNo = (String) itPrintData.next();
            svf.VrsOutn("EXAMNO" + lineCnt, retuCnt, String.valueOf(Integer.parseInt(examNo)));
            lineCnt++;
            _hasData = true;
        }
        if (lineCnt > 1) {
            svf.VrEndPage();
        }
    }

    private void setHead(final Vrw32alp svf, final MajorData majorData) {
        final String year = KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度";
        svf.VrsOut("YEAR", year);
        final String setWeek = KNJ_EditDate.h_format_W(_param._date);
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._date) + "(" + setWeek + ")");
        svf.VrsOut("SCHOOL_NAME", _param._schoolMst._schoolName1);
        final String majorName =  (majorData._majorCd != null && majorData._majorCd.endsWith("0")) ? majorData._majorLName : majorData._majorSName;
        svf.VrsOut("SUBJECT", "<" + majorName + ">");
    }

    private List getPrintData(final DB2UDB db2) throws SQLException {
        final List retList = new ArrayList();
        final String majorSql = getMajorSql();
        PreparedStatement psPrint = null;
        ResultSet rsPrint = null;
        try {
            psPrint = db2.prepareStatement(majorSql);
            rsPrint = psPrint.executeQuery();
            while (rsPrint.next()) {
                final String majorCd = rsPrint.getString("MAJORCD");
                final String majorLName = rsPrint.getString("MAJORLNAME");
                final String majorLAbbv = rsPrint.getString("MAJORLABBV");
                final String majorSName = rsPrint.getString("MAJORSNAME");
                final String majorSAbbv = rsPrint.getString("MAJORSABBV");
                final MajorData majorData = new MajorData(majorCd, majorLName, majorLAbbv, majorSName, majorSAbbv);
                majorData.setPassData(db2);
                retList.add(majorData);
            }
        } finally {
            DbUtils.closeQuietly(null, psPrint, rsPrint);
            db2.commit();
        }
        
        return retList;
    }

    private String getMajorSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     * ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_MAJOR_MST ");
        stb.append(" ORDER BY ");
        stb.append("     MAJORCD ");

        return stb.toString();
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    private class MajorData {
        final String _majorCd;
        final String _majorLName;
        final String _majorLAbbv;
        final String _majorSName;
        final String _majorSAbbv;
        List _printData;

        public MajorData(
                final String majorCd,
                final String majorLName,
                final String majorLAbbv,
                final String majorSName,
                final String majorSAbbv
        ) {
            _majorCd = majorCd;
            _majorLName = majorLName;
            _majorLAbbv = majorLAbbv;
            _majorSName = majorSName;
            _majorSAbbv = majorSAbbv;
            _printData = new ArrayList();
        }

        public void setPassData(final DB2UDB db2) throws SQLException {
            final String passDataSql = getPassDataSql();
            PreparedStatement psPallData = null;
            ResultSet rsPassData = null;
            try {
                psPallData = db2.prepareStatement(passDataSql);
                rsPassData = psPallData.executeQuery();
                while (rsPassData.next()) {
                    final String examNo = rsPassData.getString("EXAMNO");
                    _printData.add(examNo);
                }
            } finally {
                DbUtils.closeQuietly(null, psPallData, rsPassData);
                db2.commit();
            }
        }

        private String getPassDataSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.EXAMNO ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.ENTEXAMYEAR = '" + _param._year + "' ");
            stb.append("     AND T1.TESTDIV = '1' ");
            stb.append("     AND T1.SHDIV = '1' ");
            stb.append("     AND T1.DESIREDIV = '1' ");
            stb.append("     AND T1.JUDGEMENT IN (SELECT I1.NAMECD2 FROM NAME_MST I1 WHERE NAMECD1 = 'L013' AND NAMESPARE1 = '1') ");
            stb.append("     AND T1.SUC_MAJORCD = '" + _majorCd + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.EXAMNO ");

            return stb.toString();
        }
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
        private final String _year;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _date;
        private final KNJSchoolMst _schoolMst;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _date = request.getParameter("DATE");
            _schoolMst = new KNJSchoolMst(db2, _ctrlYear);
        }

    }
}

// eof
