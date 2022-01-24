// kanji=漢字
/*
 * $Id: 3f78e00fc48f322fc15a1e69621aeca1996a8b8f $
 *
 * 作成日: 2009/10/19 18:14:45 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJE.KNJE380.Param;
import servletpack.KNJE.KNJE380.Student;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 3f78e00fc48f322fc15a1e69621aeca1996a8b8f $
 */
public class KNJE380_4 extends KNJE380Abstract {

    /**
     * コンストラクタ。
     * @param param
     * @param db2
     * @param svf
     */
    public KNJE380_4(
            final Param param,
            final DB2UDB db2,
            final Vrw32alp svf
    ) {
        super(param, db2, svf);
    }

    private static final Log log = LogFactory.getLog("KNJE380_4.class");

    protected boolean printMain(final List printStudents, final String majorCd) throws SQLException {
        boolean hasData = false;

        PrintData printAllDatas = new PrintData();
        Map printMap = new HashMap();
        for (final Iterator itPrint = printStudents.iterator(); itPrint.hasNext();) {
            final Student student = (Student) itPrint.next();
            if (student._isShushoku && student._aftGradCourseDatShu._introductionDiv.equals(INTRODUCT_S)) {
                printAllDatas = setCnt(printAllDatas, student);
                PrintData printData = new PrintData();
                final String lCd = student._aftGradCourseDatShu._companyMst._industryLcd;
                final String mCd = student._aftGradCourseDatShu._companyMst._industryMcd;
                //大＋中分類毎に合算
                final String lmCd = (lCd + mCd);
                if (printMap.containsKey(lmCd)) {
                    printData = (PrintData) printMap.get(lmCd);
                }
                printData = setCnt(printData, student);
                printMap.put(lmCd, printData);
            }
        }

        final List printDataAll = getPrintDataAll(printMap);

        _svf.VrSetForm("KNJE380_4.frm", 4);
        _svf.VrsOut("SCHOOL_NAME", _param._schoolMst._schoolName1);
        _svf.VrsOut("MAJORNAME", _param.getMajorName(majorCd));
        _svf.VrsOut("GRAD_YEAR", _param.changePrintYear(_param._ctrlYear) + "度　");
        for (final Iterator itIndu = printDataAll.iterator(); itIndu.hasNext();) {
            final PrintLDatas printLDatas = (PrintLDatas) itIndu.next();
            _svf.VrsOut("JOBTYPE_LNAME", printLDatas.getLcd() + " " + printLDatas._titleName);
            _svf.VrsOut("JOBTYPE_MCD1", printLDatas.getMcd());
            _svf.VrsOut("TOTAL1", String.valueOf(printLDatas._lPrintData._totalCnt));
            _svf.VrsOut("MALE1", String.valueOf(printLDatas._lPrintData._manCnt));
            _svf.VrsOut("FEMALE1", String.valueOf(printLDatas._lPrintData._woManCnt));
            _svf.VrEndRecord();
            for (final Iterator itMap = printLDatas._mMap.keySet().iterator(); itMap.hasNext();) {
                final String key = (String) itMap.next();
                final PrintMDatas printMDatas = (PrintMDatas) printLDatas._mMap.get(key);
                _svf.VrsOut("JOBTYPE_MNAME", printMDatas.getMcd() + "  " + printMDatas._titleName);
                _svf.VrsOut("TOTAL2", String.valueOf(printMDatas._mPrintData._totalCnt));
                _svf.VrsOut("MALE2", String.valueOf(printMDatas._mPrintData._manCnt));
                _svf.VrsOut("FEMALE2", String.valueOf(printMDatas._mPrintData._woManCnt));
                _svf.VrEndRecord();
            }
            hasData = true;
        }
        _svf.VrsOut("TOTAL3", String.valueOf(printAllDatas._totalCnt));
        _svf.VrsOut("MALE3", String.valueOf(printAllDatas._manCnt));
        _svf.VrsOut("FEMALE3", String.valueOf(printAllDatas._woManCnt));
        _svf.VrEndRecord();
        return hasData;
    }

    private List getPrintDataAll(final Map printMap) throws SQLException {
        final List retList = new ArrayList();

        final String induSql = getIndustrySql();
        PreparedStatement ps = null;
        ResultSet rs = null;

        String befLcd = "";
        String befGrpLcd = "";

        try {
            ps = _db2.prepareStatement(induSql);
            rs = ps.executeQuery();
            PrintLDatas printLDatas = null;
            while (rs.next()) {
                final String lCd = rs.getString("INDUSTRY_LCD");
                final String industryLname = rs.getString("INDUSTRY_LNAME");
                final String lGrpCd = null == rs.getString("L_GROUPCD") ? "" : rs.getString("L_GROUPCD");
                final String lName = null != rs.getString("L_GROUPNAME") ? rs.getString("L_GROUPNAME") : industryLname;
                final String mCd = rs.getString("INDUSTRY_MCD");
                final String industryMname = rs.getString("INDUSTRY_MNAME");
                final String mGrpCd = rs.getString("M_GROUPCD");
                final boolean noOutput = null != rs.getString("NO_OUTPUT") ? true : false;
                final String mName = null != rs.getString("M_GROUPNAME") ? rs.getString("M_GROUPNAME") : industryMname;

                final PrintData printData = (PrintData) printMap.get(lCd + mCd);
                //大分類の改行
                if ((!befLcd.equals(lCd) && lGrpCd.equals("")) ||
                    (!befLcd.equals(lCd) && !befGrpLcd.equals(lGrpCd))
                ) {
                    if (null != printLDatas) {
                        retList.add(printLDatas);
                    }
                    printLDatas = new PrintLDatas();
                }

                //大分類のデータ作成
                printLDatas.setLData(lCd, lName, printData, mCd);

                //出力ありの中分類
                if (!noOutput) {
                    //中分類のデータ作成
                    printLDatas.setMData(mCd, mName, mGrpCd, printData);
                }

                befLcd = lCd;
                befGrpLcd = lGrpCd;
            }
            if (null != printLDatas) {
                retList.add(printLDatas);
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            _db2.commit();
        }
        return retList;
    }

    private String getIndustrySql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     MST_L.INDUSTRY_LCD, ");
        stb.append("     MST_L.INDUSTRY_LNAME, ");
        stb.append("     MST_L.L_GROUPCD, ");
        stb.append("     GRP_L.L_GROUPNAME, ");
        stb.append("     MST_M.INDUSTRY_MCD, ");
        stb.append("     MST_M.INDUSTRY_MNAME, ");
        stb.append("     MST_M.M_GROUPCD, ");
        stb.append("     MST_M.NO_OUTPUT, ");
        stb.append("     GRP_M.M_GROUPNAME ");
        stb.append(" FROM ");
        stb.append("     INDUSTRY_L_MST MST_L ");
        stb.append("     LEFT JOIN INDUSTRY_M_MST MST_M ON MST_L.INDUSTRY_LCD = MST_M.INDUSTRY_LCD ");
        stb.append("     LEFT JOIN INDUSTRY_LGROUP_MST GRP_L ON MST_L.L_GROUPCD = GRP_L.L_GROUPCD ");
        stb.append("     LEFT JOIN INDUSTRY_MGROUP_MST GRP_M ON MST_M.M_GROUPCD = GRP_M.M_GROUPCD ");
        stb.append(" ORDER BY ");
        stb.append("     MST_L.INDUSTRY_LCD, ");
        stb.append("     VALUE(GRP_M.M_GROUPCD, '0'), ");
        stb.append("     MST_M.INDUSTRY_MCD ");
        return stb.toString();
    }

    private class PrintLDatas {
        String _titleName;
        Map _lCdMap;
        Map _mCdMap;
        Map _mMap;
        PrintData _lPrintData;

        public PrintLDatas() {
            _mMap = new LinkedMap();
            _lPrintData = new PrintData();
            _lCdMap = new TreeMap();
            _mCdMap = new TreeMap();
        }

        public void setLData(
                final String lCd,
                final String title,
                final PrintData printData,
                final String mCd
        ) {
            _titleName = title;
            _lCdMap.put(lCd, lCd);
            _mCdMap.put(mCd, mCd);
            if (null != printData) {
                _lPrintData._manCnt += printData._manCnt;
                _lPrintData._woManCnt += printData._woManCnt;
                _lPrintData._totalCnt += printData._totalCnt;
            }
        }

        public void setMData(
                final String mCd,
                final String title,
                final String mGrpCd,
                final PrintData printData
        ) {
            PrintMDatas printMDatas = new PrintMDatas();
            final String setMcd = null != mGrpCd ? mGrpCd : mCd;
            if (_mMap.containsKey(setMcd)) {
                printMDatas = (PrintMDatas) _mMap.get(setMcd);
            }
            printMDatas.setMData(title, printData, mCd);
            _mMap.put(setMcd, printMDatas);
        }

        public String getLcd() {
            final StringBuffer stb = new StringBuffer();
            String sep = "";
            for (final Iterator iter = _lCdMap.keySet().iterator(); iter.hasNext();) {
                final String lCd = (String) iter.next();
                stb.append(sep + lCd);
                sep = ",";
            }
            return stb.toString();
        }

        public String getMcd() {
            final StringBuffer stb = new StringBuffer();
            int befMcd = 0;
            final String karaMoji = "\uFF5E";
            boolean karaFlg = false;
            String setData = "";
            for (final Iterator iter = _mCdMap.keySet().iterator(); iter.hasNext();) {
                final String mCd = (String) iter.next();
                final int intMCd = Integer.parseInt(mCd);
                if (befMcd == 0) {
                    stb.append("(" + mCd);
                } else {
                    if ((befMcd + 1) == intMCd) {
                        karaFlg = true;
                        setData = mCd;
                    } else {
                        final String setMcd = "," + mCd;
                        stb.append(karaFlg ? karaMoji + befMcd + setMcd : setMcd);
                        karaFlg = false;
                    }
                }
                befMcd = intMCd;
            }
            if (karaFlg) {
                stb.append(karaMoji + setData);
            }
            stb.append(")");
            return stb.toString();
        }
    }

    private class PrintMDatas {
        String _titleName;
        PrintData _mPrintData;
        Map _mCdMap;

        public PrintMDatas() {
            _mPrintData = new PrintData();
            _mCdMap = new TreeMap();
        }

        public void setMData(final String title, final PrintData printData, final String mCd) {
            _titleName = title;
            _mCdMap.put(mCd, mCd);
            if (null != printData) {
                _mPrintData._manCnt += printData._manCnt;
                _mPrintData._woManCnt += printData._woManCnt;
                _mPrintData._totalCnt += printData._totalCnt;
            }
        }

        public String getMcd() {
            final StringBuffer stb = new StringBuffer();
            int befMcd = 0;
            final String karaMoji = "\uFF5E";
            boolean karaFlg = false;
            String setData = "";
            for (final Iterator iter = _mCdMap.keySet().iterator(); iter.hasNext();) {
                final String mCd = (String) iter.next();
                final int intMCd = Integer.parseInt(mCd);
                if (befMcd == 0) {
                    stb.append(mCd);
                } else {
                    if ((befMcd + 1) == intMCd) {
                        karaFlg = true;
                        setData = mCd;
                    } else {
                        final String setMcd = "," + mCd;
                        stb.append(karaFlg ? karaMoji + befMcd + setMcd : setMcd);
                        karaFlg = false;
                    }
                }
                befMcd = intMCd;
            }
            if (karaFlg) {
                stb.append(karaMoji + setData);
            }
            return stb.toString();
        }

    }
}

// eof
