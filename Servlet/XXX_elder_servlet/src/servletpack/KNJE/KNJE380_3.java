// kanji=漢字
/*
 * $Id: 477d285720693809088bf6f619cbf9852d1eea0d $
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
 * @version $Id: 477d285720693809088bf6f619cbf9852d1eea0d $
 */
public class KNJE380_3 extends KNJE380Abstract {

    /**
     * コンストラクタ。
     * @param param
     * @param db2
     * @param svf
     */
    public KNJE380_3(
            final Param param,
            final DB2UDB db2,
            final Vrw32alp svf
    ) {
        super(param, db2, svf);
    }

    private static final Log log = LogFactory.getLog("KNJE380_3.class");

    private static final int MAX_LINE = 27;
    private static final int MAX_RETU = 2;
    private static final int PREF_MAX_LINE = 21;

    protected boolean printMain(final List printStudents, final String majorCd) throws SQLException {
        boolean hasData = false;
        final Map prefMap = getPrefMap();
        PrintData totalData = new PrintData();
        PrintData kengaiTotal = new PrintData();
        for (final Iterator itPrint = printStudents.iterator(); itPrint.hasNext();) {
            final Student student = (Student) itPrint.next();
            if (student._isShushoku && student._aftGradCourseDatShu._introductionDiv.equals(INTRODUCT_S)) {
                final String prefCd = student._aftGradCourseDatShu._prefCd;
                if (prefMap.containsKey(prefCd)) {
                    totalData = setCnt(totalData, student);
                    final PrefData prefData = (PrefData) prefMap.get(prefCd);
                    if (prefData._isSchoolPref) {
                        final String cityCd = student._aftGradCourseDatShu._cityCd;
                        if (prefData._cityMap.containsKey(cityCd)) {
                            final CityData cityData = (CityData) prefData._cityMap.get(cityCd);
                            cityData._printData = setCnt(cityData._printData, student);
                        } else {
                            prefData._printData = setCnt(prefData._printData, student);
                        }
                    } else {
                        prefData._printData = setCnt(prefData._printData, student);
                        kengaiTotal = setCnt(kengaiTotal, student);
                    }
                }
            }
            hasData = true;
        }
        _svf.VrSetForm("KNJE380_3.frm", 1);
        _svf.VrsOut("SCHOOL_NAME", _param._schoolMst._schoolName1);
        _svf.VrsOut("MAJORNAME", _param.getMajorName(majorCd));
        _svf.VrsOut("GRAD_YEAR", _param.changePrintYear(_param._ctrlYear) + "度　");
        int retuCnt = 1;
        int lineCnt = 1;
        for (final Iterator itPref = prefMap.keySet().iterator(); itPref.hasNext();) {
            final String prefCd = (String) itPref.next();
            final PrefData prefData = (PrefData) prefMap.get(prefCd);
            if (!prefData._isSchoolPref) {
                printOut("PREF" + retuCnt, prefData._prefName, retuCnt, lineCnt, prefData._printData);
                lineCnt++;
                if (lineCnt > MAX_LINE) {
                    lineCnt = 1;
                    retuCnt++;
                }
            } else {
                printOut("PREF" + MAX_RETU, prefData._prefName, MAX_RETU, PREF_MAX_LINE, prefData._printData);
                int cityCnt = 1;
                int cityLineCnt = PREF_MAX_LINE + 1;
                for (final Iterator itCity = prefData._cityMap.keySet().iterator(); itCity.hasNext();) {
                    final String cityCd = (String) itCity.next();
                    final CityData cityData = (CityData) prefData._cityMap.get(cityCd);
                    _svf.VrsOutn("CITY", cityCnt, cityData._cityName);
                    printOut("", "", MAX_RETU, cityLineCnt, cityData._printData);
                    cityCnt++;
                    cityLineCnt++;
                }
            }
        }
        printOut("PREF" + MAX_RETU, "県外小計", MAX_RETU, PREF_MAX_LINE - 1, kengaiTotal);
        printOut("", "", MAX_RETU, MAX_LINE, totalData);
        _svf.VrEndPage();
        return hasData;
    }

    private Map getPrefMap() throws SQLException {
        final Map retMap = new LinkedMap();
        final String prefSql = "SELECT * FROM PREF_MST ORDER BY PREF_CD ";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = _db2.prepareStatement(prefSql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String prefCd = rs.getString("PREF_CD");
                final String prefName = rs.getString("PREF_NAME");
                final PrefData prefData = new PrefData(prefCd, prefName);
                retMap.put(prefCd, prefData);
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            _db2.commit();
        }
        return retMap;
    }

    private void printOut(final String titleName, final String title, final int fieldNameCnt, final int fieldCnt, final PrintData data) {
        if (!titleName.equals("")) {
            _svf.VrsOutn(titleName, fieldCnt, title);
        }
        _svf.VrsOutn("TOTAL" + fieldNameCnt, fieldCnt, String.valueOf(data._totalCnt));
        _svf.VrsOutn("MALE" + fieldNameCnt, fieldCnt, String.valueOf(data._manCnt));
        _svf.VrsOutn("FEMALE" + fieldNameCnt, fieldCnt, String.valueOf(data._woManCnt));
    }

    private class PrefData {
        final String _prefCd;
        final String _prefName;
        final boolean _isSchoolPref;
        final Map _cityMap;
        PrintData _printData;

        public PrefData(final String prefCd, final String prefName) throws SQLException {
            _prefCd = prefCd;
            _prefName = prefName;
            _isSchoolPref = prefCd.equals(_param._schoolMst._prefCd) ? true : false;
            _cityMap = setCityMap();
            _printData = new PrintData();
        }

        private Map setCityMap() throws SQLException {
            final Map retMap = new LinkedMap();
            if (!_isSchoolPref) {
                return retMap;
            }
            final String prefSql = "SELECT * FROM CITY_MST WHERE PREF_CD = '" + _param._schoolMst._prefCd + "' AND CITY_FLG1 = '1' ORDER BY CITY_CD ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = _db2.prepareStatement(prefSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String cityCd = rs.getString("CITY_CD");
                    final String cityName = rs.getString("CITY_NAME");
                    final CityData cityData = new CityData(cityCd, cityName);
                    retMap.put(cityCd, cityData);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                _db2.commit();
            }
            return retMap;
        }
    }

    private class CityData {
        final String _cityCd;
        final String _cityName;
        PrintData _printData;

        public CityData(final String cityCd, final String cityName) throws SQLException {
            _cityCd = cityCd;
            _cityName = cityName;
            _printData = new PrintData();
        }
    }
}

// eof
