// kanji=漢字
/*
 * $Id: 92a38e7299e8ed2098b6beea458c572379d7f8ac $
 *
 * 作成日: 2009/05/18 10:56:27 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2004-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJZ.detail;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;

import nao_package.db.DB2UDB;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 92a38e7299e8ed2098b6beea458c572379d7f8ac $
 */
public class KNJSchoolMst {
    private static Log log = LogFactory.getLog("KNJSchoolMst.class"); 
    
    public String _year;
    public String _foundedYear;
    public String _presentEst;
    public String _classiFication;
    public String _schoolName1;
    public String _schoolName2;
    public String _schoolName3;
    public String _schoolNameEng;
    public String _schoolZipcd;
    public String _schoolAddr1;
    public String _schoolAddr2;
    public String _schoolAddr1Eng;
    public String _schoolAddr2Eng;
    public String _schoolTelNo;
    public String _schoolFaxNo;
    public String _schoolMail;
    public String _schoolUrl;
    public String _schoolDiv;
    public String _semesterDiv;
    public String _gradeHval;
    public String _entranceDate;
    public String _graduateDate;
    public String _gradCredits;
    public String _gradCompCredits;
    public String _semesAssesscd;
    public String _semesFearVal;
    public String _gradeFearVal;
    public String _absentCov;
    public String _absentCovLate;
    public String _gvalCalc;
    public String _subOffDays;
    public String _subAbsent;
    public String _subSuspend;
    public String _subMourning;
    public String _subVirus;
    public String _subKoudome;
    public String _semOffDays;
    public String _jugyouJisuFlg;
    public String _risyuBunsi;
    public String _risyuBunbo;
    public String _syutokuBunsi;
    public String _syutokuBunbo;
    public String _risyuBunsiSpecial;
    public String _risyuBunboSpecial;
    public String _syutokuBunsiSpecial;
    public String _syutokuBunboSpecial;
    public String _jituSyusu;
    public String _jituJifun;
    public String _jituJifunSpecial;
    public String _amariKuriage;
    public String _kessekiWarnBunsi;
    public String _kessekiWarnBunbo;
    public String _kessekiOutBunsi;
    public String _kessekiOutBunbo;
    public String _tokubetuKatudoKansan;
    public String _syukessekiHanteiHou;
    public String _prefCd;
    public String _kyoikuIinkaiSchoolcd;

    public String HOUTEI = "1";
    public String JITU = "2";
    /**
     * コンストラクタ。
     */
    public KNJSchoolMst(final DB2UDB db2, final String year) throws SQLException {
        this(db2, year, new HashMap());
    }

    public KNJSchoolMst(final DB2UDB db2, final String year, final Map paramMap) throws SQLException {
        String sqlWhere = "";
        String tablename = "V_SCHOOL_MST";
        boolean isOutputDebug = false;
        for (final Iterator it = paramMap.entrySet().iterator(); it.hasNext();) {
            final Map.Entry e = (Map.Entry) it.next();
            if ("TABLENAME".equals(e.getKey())) {
                tablename = "V_SCHOOL_GCM_MST";
            } else if ("outputDebug".equals(e.getKey())) {
                isOutputDebug ="1".equals(e.getValue());
            } else if (e.getValue() instanceof Number) {
                sqlWhere += " AND " + e.getKey() + " = " + e.getValue();
            } else {
                sqlWhere += " AND " + e.getKey() + " = '" + e.getValue() + "' ";
            }
        }
        String sql = "SELECT * FROM " + tablename + " WHERE YEAR = '" + year + "'" + sqlWhere;
        if (isOutputDebug) {
            log.info(" KNJSchoolMst sql = " + sql);
        }
        
        Map nameMap = Collections.EMPTY_MAP;
        ResultSet rs = null;
        db2.query(sql);
        rs = db2.getResultSet();
        try {
            nameMap = new HashMap();
            final ResultSetMetaData metaData = rs.getMetaData();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                nameMap.put(metaData.getColumnName(i), null);
            }

            while (rs.next()) {
                _year = getString("YEAR", rs, nameMap);
                _foundedYear = getString("FOUNDEDYEAR", rs, nameMap);
                _presentEst = getString("PRESENT_EST", rs, nameMap);
                _classiFication = getString("CLASSIFICATION", rs, nameMap);
                _schoolName1 = getString("SCHOOLNAME1", rs, nameMap);
                _schoolName2 = getString("SCHOOLNAME2", rs, nameMap);
                _schoolName3 = getString("SCHOOLNAME3", rs, nameMap);
                _schoolNameEng = getString("SCHOOLNAME_ENG", rs, nameMap);
                _schoolZipcd = getString("SCHOOLZIPCD", rs, nameMap);
                _schoolAddr1 = getString("SCHOOLADDR1", rs, nameMap);
                _schoolAddr2 = getString("SCHOOLADDR2", rs, nameMap);
                _schoolAddr1Eng = getString("SCHOOLADDR1_ENG", rs, nameMap);
                _schoolAddr2Eng = getString("SCHOOLADDR2_ENG", rs, nameMap);
                _schoolTelNo = getString("SCHOOLTELNO", rs, nameMap);
                _schoolFaxNo = getString("SCHOOLFAXNO", rs, nameMap);
                _schoolMail = getString("SCHOOLMAIL", rs, nameMap);
                _schoolUrl = getString("SCHOOLURL", rs, nameMap);
                _schoolDiv = getString("SCHOOLDIV", rs, nameMap);
                _semesterDiv = getString("SEMESTERDIV", rs, nameMap);
                _gradeHval = getString("GRADE_HVAL", rs, nameMap);
                _entranceDate = getString("ENTRANCE_DATE", rs, nameMap);
                _graduateDate = getString("GRADUATE_DATE", rs, nameMap);
                _gradCredits = getString("GRAD_CREDITS", rs, nameMap);
                _gradCompCredits = getString("GRAD_COMP_CREDITS", rs, nameMap);
                _semesAssesscd = getString("SEMES_ASSESSCD", rs, nameMap);
                _semesFearVal = getString("SEMES_FEARVAL", rs, nameMap);
                _gradeFearVal = getString("GRADE_FEARVAL", rs, nameMap);
                _absentCov = getString("ABSENT_COV", rs, nameMap);
                _absentCovLate = getString("ABSENT_COV_LATE", rs, nameMap);
                _gvalCalc = getString("GVAL_CALC", rs, nameMap);
                _subOffDays = getString("SUB_OFFDAYS", rs, nameMap);
                _subAbsent = getString("SUB_ABSENT", rs, nameMap);
                _subSuspend = getString("SUB_SUSPEND", rs, nameMap);
                _subMourning = getString("SUB_MOURNING", rs, nameMap);
                _subVirus = getString("SUB_VIRUS", rs, nameMap);
                _subKoudome = getString("SUB_KOUDOME", rs, nameMap);
                _semOffDays = getString("SEM_OFFDAYS", rs, nameMap);
                _jugyouJisuFlg = getString("JUGYOU_JISU_FLG", rs, nameMap);
                _risyuBunsi = getString("RISYU_BUNSI", rs, nameMap);
                _risyuBunbo = getString("RISYU_BUNBO", rs, nameMap);
                _syutokuBunsi = getString("SYUTOKU_BUNSI", rs, nameMap);
                _syutokuBunbo = getString("SYUTOKU_BUNBO", rs, nameMap);
                _risyuBunsiSpecial = getString("RISYU_BUNSI_SPECIAL", rs, nameMap);
                _risyuBunboSpecial = getString("RISYU_BUNBO_SPECIAL", rs, nameMap);
                _syutokuBunsiSpecial = getString("SYUTOKU_BUNSI_SPECIAL", rs, nameMap);
                _syutokuBunboSpecial = getString("SYUTOKU_BUNBO_SPECIAL", rs, nameMap);
                _jituSyusu = getString("JITU_SYUSU", rs, nameMap);
                _jituJifun = getString("JITU_JIFUN", rs, nameMap);
                _jituJifunSpecial = getString("JITU_JIFUN_SPECIAL", rs, nameMap);
                _amariKuriage = getString("AMARI_KURIAGE", rs, nameMap);
                _kessekiWarnBunsi = getString("KESSEKI_WARN_BUNSI", rs, nameMap);
                _kessekiWarnBunbo = getString("KESSEKI_WARN_BUNBO", rs, nameMap);
                _kessekiOutBunsi = getString("KESSEKI_OUT_BUNSI", rs, nameMap);
                _kessekiOutBunbo = getString("KESSEKI_OUT_BUNBO", rs, nameMap);
                _tokubetuKatudoKansan = getString("TOKUBETU_KATUDO_KANSAN", rs, nameMap);
                _syukessekiHanteiHou = getString("SYUKESSEKI_HANTEI_HOU", rs, nameMap);
                _prefCd = getString("PREF_CD", rs, nameMap);
                _kyoikuIinkaiSchoolcd = getString("KYOUIKU_IINKAI_SCHOOLCD", rs, nameMap);
                if (isOutputDebug) {
                    Map row = new HashMap();
                    for (final Iterator it = nameMap.keySet().iterator(); it.hasNext();) {
                        final String colname = (String) it.next();
                        row.put(colname, rs.getString(colname));
                    }
                    log.info(" KNJSchoolMst result = " + row);
                }
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        
        // outputUnreadColumn(nameMap);
    }

    private void outputUnreadColumn(final Map nameMap) {
        final List unreadList = new ArrayList();
        for (final Iterator it = nameMap.keySet().iterator(); it.hasNext();) {
            final String columnName = (String) it.next();
            final String readflg = (String) nameMap.get(columnName);
            if (null == readflg) {
                unreadList.add(columnName);
            }
        }
        if (!unreadList.isEmpty()) {
            log.fatal("### V_SCHOOL_MSTから読み込まれていないフィールドがあります。:" + unreadList);
        }
    }
    
    private String getString(final String colname, final ResultSet rs, final Map fieldMap) throws SQLException {
        if (fieldMap.keySet().contains(colname)) {
            fieldMap.put(colname, "read");
            return rs.getString(colname);
        }
        log.error("### V_SCHOOL_MSTにフィールドがありません。:" + colname);
        return null;
    }

    public boolean isHoutei() {
        return _jugyouJisuFlg == null || HOUTEI.equals(_jugyouJisuFlg);
    }

    public boolean isJitu() {
        return _jugyouJisuFlg != null && JITU.equals(_jugyouJisuFlg);
    }
    /**
     * @param args
     */
    public static void main(String[] args) {

    }

}
 // KNJ_SchoolMst

// eof
