// kanji=漢字
/*
 * $Id: 290a915a53a4e8f17a25bb5e9df88d9d4530002b $
 *
 * 作成日: 2009/12/21 21:48:46 - JST
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

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 290a915a53a4e8f17a25bb5e9df88d9d4530002b $
 */
public class KNJL375M {

    private static final Log log = LogFactory.getLog("KNJL375M.class");

    private boolean _hasData;
    private static final String FORMNAME = "KNJL375M.frm";

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
        final BaseData tonaiData = getBaseData(db2, "1", "01", "都区内合計");
        final BaseData tokaData = getBaseData(db2,"1", "02", "都下");
        final BaseData saitamaData = getBaseData(db2,"1", "03", "埼玉");
        final BaseData tibaData = getBaseData(db2,"1", "04", "千葉");
        final BaseData kanagawaData = getBaseData(db2,"1", "05", "神奈川");
        final BaseData tihouData = getBaseData(db2,"1", "99", "地方");

        final BaseData kokurituTonaiData = getBaseData(db2, "2", "01", "国立");
        final BaseData kokurituTokaData = getBaseData(db2,"2", "02", "国立");
        final BaseData kokurituSaitamaData = getBaseData(db2,"2", "03", "国立");
        final BaseData kokurituTibaData = getBaseData(db2,"2", "04", "国立");
        final BaseData kokurituKanagawaData = getBaseData(db2,"2", "05", "国立");
        final BaseData kokurituTihouData = getBaseData(db2,"2", "99", "国立");

        final BaseData sirituTonaiData = getBaseData(db2, "3", "01", "私立");
        final BaseData sirituTokaData = getBaseData(db2,"3", "02", "私立");
        final BaseData sirituSaitamaData = getBaseData(db2,"3", "03", "私立");
        final BaseData sirituTibaData = getBaseData(db2,"3", "04", "私立");
        final BaseData sirituKanagawaData = getBaseData(db2,"3", "05", "私立");
        final BaseData sirituTihouData = getBaseData(db2,"3", "99", "私立");

        final BaseData kaigaiSonotaData = getBaseData(db2,"4", "99", "海外");

        final BaseData sonotaData = getBaseData(db2,"9", "99", "その他");

        svf.VrSetForm(FORMNAME, 4);
        final PrintData printTotalData = new PrintData("合計");
        int injiLineCnt = 0;
        //タイトル
        injiLineCnt += setRecordTitle(svf, 1, "公立", "志願者数", "合格者数");
        //都内印字
        injiLineCnt += printOutMeisai2(svf, tonaiData, true);
        //都内合計印字
        injiLineCnt += printOutTotal(svf, tonaiData._totalPrintData);
        printTotalData.addCnt(tonaiData._totalPrintData._siganCnt, tonaiData._totalPrintData._passCnt);
        //都下合計印字
        injiLineCnt += printOutTotal(svf, tokaData._totalPrintData);
        printTotalData.addCnt(tokaData._totalPrintData._siganCnt, tokaData._totalPrintData._passCnt);
        //埼玉合計印字
        injiLineCnt += printOutTotal(svf, saitamaData._totalPrintData);
        printTotalData.addCnt(saitamaData._totalPrintData._siganCnt, saitamaData._totalPrintData._passCnt);
        //千葉合計印字
        injiLineCnt += printOutTotal(svf, tibaData._totalPrintData);
        printTotalData.addCnt(tibaData._totalPrintData._siganCnt, tibaData._totalPrintData._passCnt);
        //神奈川合計印字
        injiLineCnt += printOutTotal(svf, kanagawaData._totalPrintData);
        printTotalData.addCnt(kanagawaData._totalPrintData._siganCnt, kanagawaData._totalPrintData._passCnt);
        //地方合計印字
        injiLineCnt += printOutTotal(svf, tihouData._totalPrintData);
        printTotalData.addCnt(tihouData._totalPrintData._siganCnt, tihouData._totalPrintData._passCnt);
        //空行
        injiLineCnt += printKara(svf);
        //タイトル
        injiLineCnt += setRecordTitle(svf, 1, "国・私・海外", "志願者数", "合格者数");
        //国立合計印字
        final PrintData kokurituPrint = new PrintData("国立");
        kokurituPrint.addCnt(kokurituTonaiData._totalPrintData._siganCnt, kokurituTonaiData._totalPrintData._passCnt);
        kokurituPrint.addCnt(kokurituTokaData._totalPrintData._siganCnt, kokurituTokaData._totalPrintData._passCnt);
        kokurituPrint.addCnt(kokurituSaitamaData._totalPrintData._siganCnt, kokurituSaitamaData._totalPrintData._passCnt);
        kokurituPrint.addCnt(kokurituTibaData._totalPrintData._siganCnt, kokurituTibaData._totalPrintData._passCnt);
        kokurituPrint.addCnt(kokurituKanagawaData._totalPrintData._siganCnt, kokurituKanagawaData._totalPrintData._passCnt);
        kokurituPrint.addCnt(kokurituTihouData._totalPrintData._siganCnt, kokurituTihouData._totalPrintData._passCnt);
        injiLineCnt += printOutTotal(svf, kokurituPrint);
        printTotalData.addCnt(kokurituPrint._siganCnt, kokurituPrint._passCnt);
        //私立合計印字
        final PrintData sirituPrint = new PrintData("私立");
        sirituPrint.addCnt(sirituTonaiData._totalPrintData._siganCnt, sirituTonaiData._totalPrintData._passCnt);
        sirituPrint.addCnt(sirituTokaData._totalPrintData._siganCnt, sirituTokaData._totalPrintData._passCnt);
        sirituPrint.addCnt(sirituSaitamaData._totalPrintData._siganCnt, sirituSaitamaData._totalPrintData._passCnt);
        sirituPrint.addCnt(sirituTibaData._totalPrintData._siganCnt, sirituTibaData._totalPrintData._passCnt);
        sirituPrint.addCnt(sirituKanagawaData._totalPrintData._siganCnt, sirituKanagawaData._totalPrintData._passCnt);
        sirituPrint.addCnt(sirituTihouData._totalPrintData._siganCnt, sirituTihouData._totalPrintData._passCnt);
        injiLineCnt += printOutTotal(svf, sirituPrint);
        printTotalData.addCnt(sirituPrint._siganCnt, sirituPrint._passCnt);
        //海外合計印字
        injiLineCnt += printOutTotal(svf, kaigaiSonotaData._totalPrintData);
        printTotalData.addCnt(kaigaiSonotaData._totalPrintData._siganCnt, kaigaiSonotaData._totalPrintData._passCnt);
        //空行
        injiLineCnt += printKara(svf);
        //合計
        injiLineCnt += printOutTotal(svf, printTotalData);

        //2列目タイトル
        injiLineCnt += setRecordTitle(svf, 2, "公立", "志願者数", "合格者数");
        //都下印字
        injiLineCnt += printOutMeisai2(svf, tokaData, false);
        //千葉印字
        injiLineCnt += printOutMeisai2(svf, tibaData, false);
        //神奈川印字
        injiLineCnt += printOutMeisai2(svf, kanagawaData, false);
        //埼玉印字
        injiLineCnt += printOutMeisai2(svf, saitamaData, false);
        //地方印字
        injiLineCnt += printOutMeisai2(svf, tihouData, false);

        //空行
        injiLineCnt += printKara(svf);
        //4列目タイトル2
        injiLineCnt += setRecordTitle(svf, 2, "国立", "", "");
        //国立都内印字
        injiLineCnt += printOutMeisai2(svf, kokurituTonaiData, false);
        //国立都下印字
        injiLineCnt += printOutMeisai2(svf, kokurituTokaData, false);
        //国立千葉印字
        injiLineCnt += printOutMeisai2(svf, kokurituTibaData, false);
        //国立神奈川印字
        injiLineCnt += printOutMeisai2(svf, kokurituKanagawaData, false);
        //国立埼玉印字
        injiLineCnt += printOutMeisai2(svf, kokurituSaitamaData, false);
        //国立地方印字
        injiLineCnt += printOutMeisai2(svf, kokurituTihouData, false);

        //空行
        injiLineCnt += printKara(svf);
        //4列目タイトル3
        injiLineCnt += setRecordTitle(svf, 2, "私立", "", "");
        //私立都内印字
        injiLineCnt += printOutMeisai2(svf, sirituTonaiData, false);
        //私立都下印字
        injiLineCnt += printOutMeisai2(svf, sirituTokaData, false);
        //私立千葉印字
        injiLineCnt += printOutMeisai2(svf, sirituTibaData, false);
        //私立神奈川印字
        injiLineCnt += printOutMeisai2(svf, sirituKanagawaData, false);
        //私立埼玉印字
        injiLineCnt += printOutMeisai2(svf, sirituSaitamaData, false);
        //私立地方印字
        injiLineCnt += printOutMeisai2(svf, sirituTihouData, false);

        _hasData = true;
    }

    //空行
    private int printKara(final Vrw32alp svf) {
        int retInt = 0;
        svf.VrsOut("KARA", "a");
        retInt++;
        svf.VrEndRecord();
        return retInt;
    }

    private int setRecordTitle(final Vrw32alp svf, final int fieldNo, final String title1, final String title2, final String title3) {
        int retInt = 0;
        svf.VrAttribute("AREADIV" + fieldNo, "Meido=100" ); 
        svf.VrsOut("AREADIV" + fieldNo, "a");
        if (fieldNo == 1) {
            svf.VrsOut("GROUP1", title1);
        }
        svf.VrsOut("NATPUB" + fieldNo, title1);
        svf.VrsOut("APPNO" + fieldNo, title2);
        svf.VrsOut("PASS" + fieldNo, title3);
        retInt++;
        svf.VrEndRecord();
        return retInt;
    }

    private int printOutMeisai2(final Vrw32alp svf, final BaseData baseData, final boolean inji0Flg) {
        String befNatpubCd = "";
        int retInt = 0;
        for (final Iterator iter = baseData._meisai.iterator(); iter.hasNext();) {
            final PrintData printData = (PrintData) iter.next();
            if (inji0Flg || 0 < printData._siganCnt) {
                if (!befNatpubCd.equals(baseData._natpubpriCd)) {
                    svf.VrsOut("AREADIV2", printData._areaDivName);
                }
                svf.VrsOut("GROUP2", printData._areaDivName);
                svf.VrsOut("NATPUB2", printData._areaName);
                svf.VrsOut("APPNO2", String.valueOf(printData._siganCnt));
                svf.VrsOut("PASS2", String.valueOf(printData._passCnt));
                retInt++;
                svf.VrEndRecord();
                befNatpubCd = baseData._natpubpriCd;
            }
        }
        return retInt;
    }

    private int printOutTotal(final Vrw32alp svf, final PrintData printData) {
        int retInt = 0;
        svf.VrsOut("GROUP1", printData._areaDivName);
        svf.VrsOut("NATPUB1", printData._areaDivName);
        svf.VrsOut("APPNO1", String.valueOf(printData._siganCnt));
        svf.VrsOut("PASS1", String.valueOf(printData._passCnt));
        retInt++;
        svf.VrEndRecord();
        return retInt;
    }

    private BaseData getBaseData(final DB2UDB db2, final String pubCd, final String areaDiv, final String totalName) throws SQLException {
        BaseData retData = null;
        final String dataSql = getDataSql(pubCd, areaDiv);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(dataSql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String setNatpubpriCd = rs.getString("NATPUBPRI_CD");
                final String setNatpubpriName = rs.getString("NATPUBPRI_NAME");
                final String setNatpubpriAbbv = rs.getString("NATPUBPRI_ABBV");
                final String setAreaDivCd = rs.getString("AREA_DIV_CD");
                final String setAreaDivName = rs.getString("AREA_DIV_NAME");
                final String setAreaDivAbbv = rs.getString("AREA_DIV_ABBV");
                final String setAreaCd = rs.getString("AREA_CD");
                final String setAreaName = rs.getString("AREA_NAME");
                final String setAreaAbbv = rs.getString("AREA_ABBV");
                final int siganCnt = rs.getInt("CNT");
                final int passCnt = rs.getInt("PASS_CNT");
                if (null == retData) {
                    retData = new BaseData(setNatpubpriCd, setNatpubpriName, setNatpubpriAbbv);
                }
                retData.setMeisai(setAreaDivCd, setAreaDivName, setAreaDivAbbv, setAreaCd, setAreaName, setAreaAbbv, siganCnt, passCnt);
                retData.setTotal(totalName, siganCnt, passCnt);
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retData;
    }

    private String getDataSql(final String natpubpriCd, final String areaDiv) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.NATPUBPRI_CD, ");
        stb.append("     PUB_M.NATPUBPRI_NAME, ");
        stb.append("     PUB_M.NATPUBPRI_ABBV, ");
        stb.append("     T1.AREA_DIV_CD, ");
        stb.append("     ARE_M.AREA_DIV_NAME, ");
        stb.append("     ARE_M.AREA_DIV_ABBV, ");
        stb.append("     T1.AREA_CD, ");
        stb.append("     T1.AREA_NAME, ");
        stb.append("     T1.AREA_ABBV, ");
        stb.append("     SUM(CASE WHEN APP.EXAMNO IS NOT NULL THEN 1 ELSE 0 END) AS CNT, ");
        stb.append("     SUM(CASE WHEN RECE.TOTAL4 IS NOT NULL THEN 1 ELSE 0 END) AS PASS_CNT ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_AREA_MST T1 ");
        stb.append("     LEFT JOIN ENTEXAM_AREA_DIV_MST ARE_M ON T1.NATPUBPRI_CD = ARE_M.NATPUBPRI_CD ");
        stb.append("          AND T1.AREA_DIV_CD = ARE_M.AREA_DIV_CD ");
        stb.append("     LEFT JOIN ENTEXAM_NATPUBPRI_MST PUB_M ON T1.NATPUBPRI_CD = PUB_M.NATPUBPRI_CD ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT APP ON APP.ENTEXAMYEAR = '" + _param._year + "' ");
        stb.append("          AND T1.NATPUBPRI_CD = APP.FS_NATPUBPRIDIV ");
        stb.append("          AND T1.AREA_DIV_CD = APP.FS_AREA_DIV ");
        stb.append("          AND T1.AREA_CD = APP.FS_AREA_CD ");
        stb.append("     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'L013' ");
        stb.append("          AND APP.JUDGEMENT = L1.NAMECD2 ");
        stb.append("          AND L1.NAMESPARE1 = '1' ");
        stb.append("     LEFT JOIN ENTEXAM_RECEPT_DAT RECE ON APP.ENTEXAMYEAR = RECE.ENTEXAMYEAR ");
        stb.append("          AND APP.APPLICANTDIV = RECE.APPLICANTDIV ");
        stb.append("          AND APP.TESTDIV = RECE.TESTDIV ");
        stb.append("          AND APP.EXAMNO = RECE.EXAMNO ");
        stb.append("          AND RECE.TOTAL4 >= " + _param._passScore + "  ");
        stb.append(" WHERE ");
        stb.append("     T1.NATPUBPRI_CD = '" + natpubpriCd + "' ");
        stb.append("     AND T1.AREA_DIV_CD = '" + areaDiv + "' ");
        stb.append(" GROUP BY ");
        stb.append("     T1.NATPUBPRI_CD, ");
        stb.append("     PUB_M.NATPUBPRI_NAME, ");
        stb.append("     PUB_M.NATPUBPRI_ABBV, ");
        stb.append("     T1.AREA_DIV_CD, ");
        stb.append("     ARE_M.AREA_DIV_NAME, ");
        stb.append("     ARE_M.AREA_DIV_ABBV, ");
        stb.append("     T1.AREA_CD, ");
        stb.append("     T1.AREA_NAME, ");
        stb.append("     T1.AREA_ABBV ");
        stb.append(" ORDER BY ");
        stb.append("     T1.NATPUBPRI_CD, ");
        stb.append("     T1.AREA_DIV_CD, ");
        stb.append("     T1.AREA_CD ");

        return stb.toString();
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    private class BaseData {
        final String _natpubpriCd;
        final String _natpubpriName;
        final String _natpubpriAbbv;
        final List _meisai;
        PrintData _totalPrintData;

        public BaseData(final String natpubpriCd, final String natpubpriName, final String natpubpriAbbv) {
            _natpubpriCd = natpubpriCd;
            _natpubpriName = natpubpriName;
            _natpubpriAbbv = natpubpriAbbv;
            _meisai = new ArrayList();
            _totalPrintData = null;
        }

        public void setMeisai(
                final String areaDivCd,
                final String areaDivName,
                final String areaDivAbbv,
                final String areaCd,
                final String areaName,
                final String areaAbbv,
                final int siganCnt,
                final int passCnt
        ) {
            final PrintData data = new PrintData(areaDivCd, areaDivName, areaDivAbbv, areaCd, areaName, areaAbbv, siganCnt, passCnt);
            _meisai.add(data);
        }

        public void setTotal(
                final String totalName,
                final int siganCnt,
                final int passCnt
        ) {
            if (null == _totalPrintData) {
                _totalPrintData = new PrintData(totalName);
            }
            _totalPrintData.addCnt(siganCnt, passCnt);
        }
    }

    private class PrintData {
        final String _areaDivCd;
        final String _areaDivName;
        final String _areaDivAbbv;
        final String _areaCd;
        final String _areaName;
        final String _areaAbbv;
        int _siganCnt;
        int _passCnt;

        public PrintData(
                final String areaDivCd,
                final String areaDivName,
                final String areaDivAbbv,
                final String areaCd,
                final String areaName,
                final String areaAbbv,
                final int siganCnt,
                final int passCnt
        ) {
            _areaDivCd = areaDivCd;
            _areaDivName = areaDivName;
            _areaDivAbbv = areaDivAbbv;
            _areaCd = areaCd;
            _areaName = areaName;
            _areaAbbv = areaAbbv;
            addCnt(siganCnt, passCnt);
        }

        public PrintData(
                final String areaDivName
        ) {
            _areaDivCd = "";
            _areaDivName = areaDivName;
            _areaDivAbbv = "";
            _areaCd = "";
            _areaName = "";
            _areaAbbv = "";
            addCnt(0, 0);
        }

        private void addCnt(final int siganCnt, final int passCnt) {
            _siganCnt += siganCnt;
            _passCnt += passCnt;
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return _areaDivName + ":" + _areaName + " = " + _siganCnt + " - " + _passCnt;
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
        private final String _passScore;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _passScore = request.getParameter("PASS_SCORE");
        }

    }
}

// eof
