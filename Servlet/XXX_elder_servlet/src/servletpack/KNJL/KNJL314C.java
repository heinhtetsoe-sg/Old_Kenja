// kanji=漢字
/*
 * $Id: 10410b3baff87c5e074d831b8fc5dbbd386303e9 $
 *
 * 作成日: 2011/12/01 17:23:26 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 10410b3baff87c5e074d831b8fc5dbbd386303e9 $
 */
public class KNJL314C {

    private static final Log log = LogFactory.getLog("KNJL314C.class");

    private boolean _hasData;
    private final static int MAX_LINE = 35;

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

            if ("9".equals(_param._testDiv)) {
                String[] testDivPrint = new String[]{};
                if("1".equals(_param._applicantDiv)) { 
                    testDivPrint = new String[]{"1","2"};
                } else if("2".equals(_param._applicantDiv)) {
                    if (_param.isGojo()) {
                        testDivPrint = new String[]{"3","4","5","7","8"}; 
                    } else {
                        testDivPrint = new String[]{"3","4","5"}; 
                    }
                }
                for (int i = 0; i < testDivPrint.length; i++) {
                    final String testDiv = testDivPrint[i];
                    printMain(db2, svf, testDiv);
                }
            } else {
                printMain(db2, svf, _param._testDiv);
            }

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf, final String testDiv) throws SQLException {
        if ("1".equals(_param._useStation)) {
            for (final Iterator iter = _param._stationSort.iterator(); iter.hasNext();) {
                final String stationDiv = (String) iter.next();
                final List printList = getPrintData(db2, stationDiv, testDiv);

                setPrintData(db2, svf, testDiv, printList, stationDiv);
            }
        } else {
            final List printList = getPrintData(db2, "", testDiv);

            setPrintData(db2, svf, testDiv, printList, "");
        }
    }

    private List getPrintData(final DB2UDB db2, final String paraStationDiv, final String testDiv) throws SQLException {
        final List retList = new ArrayList();
        final String sql = getOutPutSql(paraStationDiv, testDiv);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String examno = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String nameKana = rs.getString("NAME_KANA");
                final String gName = rs.getString("GNAME");
                final String gKana = rs.getString("GKANA");
                final String addr = rs.getString("ADDR");
                final String telno = rs.getString("TELNO");
                final String stationDiv = rs.getString("STATIONDIV");
                final int busUserCount = rs.getInt("BUS_USER_COUNT");
                final OutPutData outPutData = new OutPutData(
                        examno,
                        name,
                        nameKana,
                        gName,
                        gKana,
                        addr,
                        telno,
                        stationDiv,
                        busUserCount
                        );
                retList.add(outPutData);
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getOutPutSql(final String stationDiv, final String testDiv) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     T1.NAME_KANA, ");
        stb.append("     L1.GNAME, ");
        stb.append("     L1.GKANA, ");
        stb.append("     VALUE(L1.ADDRESS1, '') || VALUE(L1.ADDRESS2, '') AS ADDR, ");
        stb.append("     L1.TELNO, ");
        stb.append("     T1.STATIONDIV, ");
        stb.append("     VALUE(T1.BUS_USER_COUNT, 0) AS BUS_USER_COUNT ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT L1 ON T1.ENTEXAMYEAR = L1.ENTEXAMYEAR ");
        stb.append("          AND T1.EXAMNO = L1.EXAMNO ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._year + "' ");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND T1.TESTDIV = '" + testDiv + "' ");
        stb.append("     AND T1.BUS_USE = '1' ");
        if ("1".equals(_param._useStation)) {
            stb.append("     AND T1.STATIONDIV = '" + stationDiv + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.EXAMNO ");

        return stb.toString();
    }

    private void setPrintData(final DB2UDB db2, final Vrw32alp svf, final String testDiv, final List printList, final String paraStationDiv) {
        int totalPage = printList.size() / MAX_LINE;
        int hasuPage = printList.size() % MAX_LINE > 0 ? 1 : 0;
        totalPage = totalPage + hasuPage;
        int page = 1;
        setTitle(db2, svf, testDiv, paraStationDiv, totalPage, page);
        int printCnt = 1;
        int busCnt1 = 0;
        int busCnt2 = 0;
        int busCnt3 = 0;
        for (final Iterator itPrint = printList.iterator(); itPrint.hasNext();) {
            if (printCnt > MAX_LINE) {
                svf.VrEndPage();
                page++;
                setTitle(db2, svf, testDiv, paraStationDiv, totalPage, page);
                printCnt = 1;
            }
            final OutPutData outPutData = (OutPutData) itPrint.next();
            svf.VrsOutn("NO", printCnt, outPutData._examno);
            svf.VrsOutn("NAME" + (getMS932ByteCount(outPutData._name) > 30 ? "3" : getMS932ByteCount(outPutData._name) > 20 ? "2" : "1"), printCnt, outPutData._name);
            svf.VrsOutn("NAME_KANA" + (getMS932ByteCount(outPutData._nameKana) > 50 ? "3" : getMS932ByteCount(outPutData._nameKana) > 20 ? "2" : "1"), printCnt, outPutData._nameKana);
            svf.VrsOutn("GURD_NAME" + (getMS932ByteCount(outPutData._gName) > 30 ? "3" : getMS932ByteCount(outPutData._gName) > 20 ? "2" : "1"), printCnt, outPutData._gName);
            svf.VrsOutn("GURD_NAME_KANA" + (getMS932ByteCount(outPutData._gKana) > 50 ? "3" : getMS932ByteCount(outPutData._gKana) > 20 ? "2" : "1"), printCnt, outPutData._gKana);

            svf.VrsOutn("ADDRESS" + (getMS932ByteCount(outPutData._addr) > 72 ? "1_2" : "1"), printCnt, outPutData._addr);

            svf.VrsOutn("TELNO", printCnt, outPutData._telno);
            svf.VrsOutn("BUSUSE" + outPutData._stationDiv, printCnt, String.valueOf(outPutData._busUserCount));
            if ("1".equals(outPutData._stationDiv)) {
                busCnt1 += outPutData._busUserCount;
            } else if ("2".equals(outPutData._stationDiv)) {
                busCnt2 += outPutData._busUserCount;
            } else {
                busCnt3 += outPutData._busUserCount;
            }
            _hasData = true;
            printCnt++;
        }
        if (printCnt > 1) {
            svf.VrsOut("TOTAL_NAME", "計");
            svf.VrsOut("TOTAL_BUSUSE1", String.valueOf(busCnt1));
            svf.VrsOut("TOTAL_BUSUSE2", String.valueOf(busCnt2));
            svf.VrsOut("TOTAL_BUSUSE3", String.valueOf(busCnt3));
            svf.VrEndPage();
        }
    }

    private int getMS932ByteCount(final String str) {
        if (null == str) return 0;
        int ret = 0;
        try {
            ret = str.getBytes("MS932").length;
        } catch (Exception e) {
            log.error("exception!", e);
        }
        return ret;
    }

    private void setTitle(final DB2UDB db2, final Vrw32alp svf, final String testDiv, final String paraStationDiv, final int totalPage, final int page) {
        svf.VrSetForm("KNJL314C_G.frm", 1);
        svf.VrsOut("NENDO", _param.getNendo());
        final String applicant = _param.getNameMst(db2, "L003", _param._applicantDiv);
        final String test = _param.getNameMst(db2, "L004", testDiv);
        svf.VrsOut("TITLE", test + applicant + "入学試験");
        svf.VrsOut("SCHOOLNAME", _param.getSchoolName(db2));
        if ("1".equals(_param._useStation)) {
            final String stationName = (String) _param._stationMap.get(paraStationDiv);
            svf.VrsOut("SUBTITLE", stationName);
        } else {
            svf.VrsOut("SUBTITLE", "受験番号順");
        }
        svf.VrsOut("PAGE1", String.valueOf(page));
        svf.VrsOut("PAGE2", String.valueOf(totalPage));

        svf.VrsOut( "DATE", _param.getLoginDateString() + _param.getTimeString());
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    private class OutPutData {
        final String _examno;
        final String _name;
        final String _nameKana;
        final String _gName;
        final String _gKana;
        final String _addr;
        final String _telno;
        final String _stationDiv;
        final int _busUserCount;

        public OutPutData(
                final String examno,
                final String name,
                final String nameKana,
                final String gName,
                final String gKana,
                final String addr,
                final String telno,
                final String stationDiv,
                final int busUserCount
        ) {
            _examno       = examno;
            _name         = name;
            _nameKana     = nameKana;
            _gName        = gName;
            _gKana        = gKana;
            _addr         = addr;
            _telno        = telno;
            _stationDiv   = stationDiv;
            _busUserCount = busUserCount;
        }
    }
    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    class Param {
        final String _year;
        final String _applicantDiv;
        final String _testDiv;
        final String _useStation;
        final String _loginDate;

        final boolean _seirekiFlg;
        final String _z010SchoolCode;
        final Map _stationMap;
        final List _stationSort;

        Param(DB2UDB db2, HttpServletRequest request) {
            _year = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _useStation = request.getParameter("USESTATION");
            _loginDate = request.getParameter("LOGIN_DATE");
            // 西暦使用フラグ
            _seirekiFlg = getSeirekiFlg(db2);
            _z010SchoolCode = getSchoolCode(db2);

            _stationMap = new HashMap();
            _stationMap.put("1", "林間田園都市駅");
            _stationMap.put("2", "福神駅");
            _stationMap.put("3", "JR五条駅");

            _stationSort = new ArrayList();
            _stationSort.add("3");
            _stationSort.add("1");
            _stationSort.add("2");
        }

        String getNendo() {
            return _seirekiFlg ? _year+"年度":
                KNJ_EditDate.h_format_JP_N(_year+"-01-01")+"度";
        }

        String getLoginDateString() {
            return getDateString(_loginDate);
        }

        String getDateString(String dateFormat) {
            if (null != dateFormat) {
                return _seirekiFlg ?
                        dateFormat.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(dateFormat):
                            KNJ_EditDate.h_format_JP(dateFormat) ;
            }
            return "";
        }

        String getTimeString() {
            if (null != _loginDate) {
                Calendar cal = Calendar.getInstance();
                int hour = cal.get(Calendar.HOUR_OF_DAY);
                int minute = cal.get(Calendar.MINUTE);
                DecimalFormat df = new DecimalFormat("00");
                return df.format(hour) + "時" + df.format(minute) + "分現在";
            }
            return "";
        }

        private boolean getSeirekiFlg(DB2UDB db2) {
            boolean seirekiFlg = false;
            try {
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                if ( rs.next() ) {
                    if (rs.getString("NAME1").equals("2")) seirekiFlg = true; //西暦
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("getSeirekiFlg Exception", e);
            }
            return seirekiFlg;
        }

        private String getNameMst(DB2UDB db2, String namecd1,String namecd2) {
            
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT NAME1 ");
            sql.append(" FROM NAME_MST ");
            sql.append(" WHERE NAMECD1 = '"+namecd1+"' AND NAMECD2 = '"+namecd2+"' ");
            String name = null;
            try{
                PreparedStatement ps = db2.prepareStatement(sql.toString());
                ResultSet rs = ps.executeQuery();
                
                if (rs.next()) {
                   name = rs.getString("NAME1");
                }
            } catch (SQLException ex) {
                log.debug(ex);
            }
            
            return name;
        }

        private String getSchoolName(DB2UDB db2) {
            String certifKindCd = null;
            if ("1".equals(_applicantDiv)) certifKindCd = "105";
            if ("2".equals(_applicantDiv)) certifKindCd = "106";
            if (certifKindCd == null) return null;
            
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME ");
            sql.append(" FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '"+_year+"' AND CERTIF_KINDCD = '"+certifKindCd+"' ");
            String name = null;
            try{
                PreparedStatement ps = db2.prepareStatement(sql.toString());
                ResultSet rs = ps.executeQuery();
                
                if (rs.next()) {
                   name = rs.getString("SCHOOL_NAME");
                }
            } catch (SQLException ex) {
                log.error(ex);
            }
            
            return name;
        }

        private String getSchoolCode(DB2UDB db2) {
            String schoolCode = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("   CASE WHEN NAME1 = 'CHIBEN' THEN NAME2 ELSE NULL END AS SCHOOLCODE ");
                stb.append(" FROM ");
                stb.append("   NAME_MST T1 ");
                stb.append(" WHERE ");
                stb.append("   T1.NAMECD1 = 'Z010' ");
                stb.append("   AND T1.NAMECD2 = '00' ");
                
                PreparedStatement ps = db2.prepareStatement(stb.toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    schoolCode = rs.getString("SCHOOLCODE");
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("getSchoolCode Exception", e);
            }
            return schoolCode;
        }

        boolean isGojo() {
            return "30290053001".equals(_z010SchoolCode);
        }

        boolean isWakayama() {
            return "30300049001".equals(_z010SchoolCode);
        }
    }
}

// eof
