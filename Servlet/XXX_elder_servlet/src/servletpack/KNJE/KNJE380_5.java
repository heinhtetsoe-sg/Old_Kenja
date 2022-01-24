// kanji=漢字
/*
 * $Id: edf45857b654796447e9711e881e417181906db5 $
 *
 * 作成日: 2009/10/19 18:14:45 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import java.sql.SQLException;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJE.KNJE380.Param;
import servletpack.KNJE.KNJE380.Student;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: edf45857b654796447e9711e881e417181906db5 $
 */
public class KNJE380_5 extends KNJE380Abstract {

    /**
     * コンストラクタ。
     * @param param
     * @param db2
     * @param svf
     */
    public KNJE380_5(
            final Param param,
            final DB2UDB db2,
            final Vrw32alp svf
    ) {
        super(param, db2, svf);
    }

    private static final Log log = LogFactory.getLog("KNJE380_5.class");

    private static final String TOTAL = "TOTAL";

    private static int KIBO29 = 29;
    private static int KIBO99 = 99;
    private static int KIBO299 = 299;
    private static int KIBO499 = 499;
    private static int KIBO999 = 999;
    private static int KIBO1000 = 1000;

    protected boolean printMain(final List printStudents, final String majorCd) throws SQLException {
        boolean hasData = false;

        final Map printKoumoku = getPrintKoumoku();
        final Map printKibo = getPrintKibo();
        final Map printGassan = getGassan();
        for (final Iterator itPrint = printStudents.iterator(); itPrint.hasNext();) {
            final Student student = (Student) itPrint.next();
            if (student._isShushoku && student._aftGradCourseDatShu._introductionDiv.equals(INTRODUCT_S)) {
                PrintData printData = new PrintData();
                printData = setCnt(printData, student);

                final String lCd = student._aftGradCourseDatShu._jobtypeLcd;
                final String prefCd = student._aftGradCourseDatShu._prefCd;

                final PrintLDatas totalLDatas = (PrintLDatas) printKoumoku.get(TOTAL);
                totalLDatas.setLData(printData, prefCd);
                printKoumoku.put(TOTAL, totalLDatas);

                if (printKoumoku.containsKey(lCd)) {
                    final PrintLDatas printLDatas = (PrintLDatas) printKoumoku.get(lCd);
                    printLDatas.setLData(printData, prefCd);
                    printKoumoku.put(lCd, printLDatas);
                    if (printGassan.containsKey(lCd)) {
                        final String gassan = (String) printGassan.get(lCd);
                        PrintLDatas printGassanDatas = (PrintLDatas) printKoumoku.get(gassan);
                        printGassanDatas.setLData(printData, prefCd);
                        printKoumoku.put(gassan, printGassanDatas);
                    }
                }
                int soninZu = Integer.parseInt(student._aftGradCourseDatShu._companyMst._soninzu);
                if (KIBO29 >= soninZu) {
                    final String kiboKey = String.valueOf(KIBO29);
                    final PrintLDatas printLDatas = (PrintLDatas) printKibo.get(kiboKey);
                    printLDatas.setLData(printData, prefCd);
                    printKibo.put(kiboKey, printLDatas);
                } else if (KIBO99 >= soninZu) {
                    final String kiboKey = String.valueOf(KIBO99);
                    final PrintLDatas printLDatas = (PrintLDatas) printKibo.get(kiboKey);
                    printLDatas.setLData(printData, prefCd);
                    printKibo.put(kiboKey, printLDatas);
                } else if (KIBO499 >= soninZu) {
                    final String kiboKey = String.valueOf(KIBO499);
                    final PrintLDatas printLDatas = (PrintLDatas) printKibo.get(kiboKey);
                    printLDatas.setLData(printData, prefCd);
                    printKibo.put(kiboKey, printLDatas);
                } else if (KIBO999 >= soninZu) {
                    final String kiboKey = String.valueOf(KIBO999);
                    final PrintLDatas printLDatas = (PrintLDatas) printKibo.get(kiboKey);
                    printLDatas.setLData(printData, prefCd);
                    printKibo.put(kiboKey, printLDatas);
                } else if (KIBO1000 <= soninZu) {
                    final String kiboKey = String.valueOf(KIBO1000);
                    final PrintLDatas printLDatas = (PrintLDatas) printKibo.get(kiboKey);
                    printLDatas.setLData(printData, prefCd);
                    printKibo.put(kiboKey, printLDatas);
                }
            }
        }


        _svf.VrSetForm("KNJE380_5.frm", 1);
        _svf.VrsOut("SCHOOL_NAME", _param._schoolMst._schoolName1);
        _svf.VrsOut("MAJORNAME", _param.getMajorName(majorCd));
        _svf.VrsOut("GRAD_YEAR", _param.changePrintYear(_param._ctrlYear) + "度　");
        int lineCnt = 1;
        for (final Iterator itIndu = printKoumoku.keySet().iterator(); itIndu.hasNext();) {
            final String key = (String) itIndu.next();
            final PrintLDatas printLDatas = (PrintLDatas) printKoumoku.get(key);
            _svf.VrsOutn("TOTAL_ALL", lineCnt, String.valueOf(printLDatas._lAllPrintData._totalCnt));
            _svf.VrsOutn("TOTAL_MALE", lineCnt, String.valueOf(printLDatas._lAllPrintData._manCnt));
            _svf.VrsOutn("TOTAL_FEMALE", lineCnt, String.valueOf(printLDatas._lAllPrintData._woManCnt));
            _svf.VrsOutn("IN_ALL", lineCnt, String.valueOf(printLDatas._lInPrintData._totalCnt));
            _svf.VrsOutn("IN_MALE", lineCnt, String.valueOf(printLDatas._lInPrintData._manCnt));
            _svf.VrsOutn("IN_FEMALE", lineCnt, String.valueOf(printLDatas._lInPrintData._woManCnt));
            lineCnt++;
            hasData = true;
        }
        lineCnt = 1;
        final Map printKiboSort = new TreeMap();
        for (final Iterator itIndu = printKibo.keySet().iterator(); itIndu.hasNext();) {
            final String key = (String) itIndu.next();
            final PrintLDatas printLDatas = (PrintLDatas) printKibo.get(key);
            printKiboSort.put(new Integer(key), printLDatas);
        }
        for (final Iterator itIndu = printKiboSort.keySet().iterator(); itIndu.hasNext();) {
            final Integer key = (Integer) itIndu.next();
            final PrintLDatas printLDatas = (PrintLDatas) printKiboSort.get(key);
            _svf.VrsOutn("COMPANY_SCALE_ALL", lineCnt, String.valueOf(printLDatas._lAllPrintData._totalCnt));
            _svf.VrsOutn("COMPANY_SCALE_MALE", lineCnt, String.valueOf(printLDatas._lAllPrintData._manCnt));
            _svf.VrsOutn("COMPANY_SCALE_FEMALE", lineCnt, String.valueOf(printLDatas._lAllPrintData._woManCnt));
            lineCnt++;
        }
        _svf.VrEndPage();
        return hasData;
    }

    private Map getPrintKoumoku() {
        final Map retMap = new TreeMap();
        retMap.put("A", new PrintLDatas());
        retMap.put("B", new PrintLDatas());
        retMap.put("C", new PrintLDatas());
        retMap.put("D", new PrintLDatas());
        retMap.put("E", new PrintLDatas());
        retMap.put("F", new PrintLDatas());
        retMap.put("G", new PrintLDatas());
        retMap.put("H", new PrintLDatas());
        retMap.put("I", new PrintLDatas());
        retMap.put("I1", new PrintLDatas());
        retMap.put("I2", new PrintLDatas());
        retMap.put("I3", new PrintLDatas());
        retMap.put("TOTAL", new PrintLDatas());
        return retMap;
    }

    private Map getPrintKibo() {
        final Map retMap = new TreeMap();
        retMap.put("29", new PrintLDatas());
        retMap.put("99", new PrintLDatas());
        retMap.put("299", new PrintLDatas());
        retMap.put("499", new PrintLDatas());
        retMap.put("999", new PrintLDatas());
        retMap.put("1000", new PrintLDatas());
        return retMap;
    }

    private Map getGassan() {
        final Map retMap = new TreeMap();
        retMap.put("I1", "I");
        retMap.put("I2", "I");
        retMap.put("I3", "I");
        return retMap;
    }

    private class PrintLDatas {
        PrintData _lAllPrintData;
        PrintData _lInPrintData;

        public PrintLDatas() {
            _lAllPrintData = new PrintData();
            _lInPrintData = new PrintData();
        }

        public void setLData(
                final PrintData printData,
                final String prefCd
        ) {
            if (null != printData) {
                _lAllPrintData._manCnt += printData._manCnt;
                _lAllPrintData._woManCnt += printData._woManCnt;
                _lAllPrintData._totalCnt += printData._totalCnt;
                if (prefCd.equals(_param._schoolMst._prefCd)) {
                    _lInPrintData._manCnt += printData._manCnt;
                    _lInPrintData._woManCnt += printData._woManCnt;
                    _lInPrintData._totalCnt += printData._totalCnt;
                }
            }
        }
    }
}

// eof
