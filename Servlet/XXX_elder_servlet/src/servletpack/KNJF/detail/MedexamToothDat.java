// kanji=漢字
/*
 * $Id: 6adcd048429470260d29ea6064fb657fe4a92106 $
 *
 * 作成日: 2009/10/08 13:44:44 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJF.detail;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.DB2UDB;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 6adcd048429470260d29ea6064fb657fe4a92106 $
 */
public class MedexamToothDat {

    private static final Log log = LogFactory.getLog("MedexamToothDat.class");
    protected final DB2UDB _db2;

    public String _jawsJointcd;
    public String _jawsJointcd2;
    public String _jawsJointcd3;
    public String _plaquecd;
    public String _gumcd;
    public String _calculuscd;
    public String _orthodontics;
    public String _upRBaby5;
    public String _upRBaby4;
    public String _upRBaby3;
    public String _upRBaby2;
    public String _upRBaby1;
    public String _upLBaby1;
    public String _upLBaby2;
    public String _upLBaby3;
    public String _upLBaby4;
    public String _upLBaby5;
    public String _lwRBaby5;
    public String _lwRBaby4;
    public String _lwRBaby3;
    public String _lwRBaby2;
    public String _lwRBaby1;
    public String _lwLBaby1;
    public String _lwLBaby2;
    public String _lwLBaby3;
    public String _lwLBaby4;
    public String _lwLBaby5;
    public String _babytooth;
    public String _remainbabytooth;
    public String _treatedbabytooth;
    public String _brackBabytooth;
    public String _upRAdult8;
    public String _upRAdult7;
    public String _upRAdult6;
    public String _upRAdult5;
    public String _upRAdult4;
    public String _upRAdult3;
    public String _upRAdult2;
    public String _upRAdult1;
    public String _upLAdult1;
    public String _upLAdult2;
    public String _upLAdult3;
    public String _upLAdult4;
    public String _upLAdult5;
    public String _upLAdult6;
    public String _upLAdult7;
    public String _upLAdult8;
    public String _lwRAdult8;
    public String _lwRAdult7;
    public String _lwRAdult6;
    public String _lwRAdult5;
    public String _lwRAdult4;
    public String _lwRAdult3;
    public String _lwRAdult2;
    public String _lwRAdult1;
    public String _lwLAdult1;
    public String _lwLAdult2;
    public String _lwLAdult3;
    public String _lwLAdult4;
    public String _lwLAdult5;
    public String _lwLAdult6;
    public String _lwLAdult7;
    public String _lwLAdult8;
    public String _adulttooth;
    public String _remainadulttooth;
    public String _treatedadulttooth;
    public String _lostadulttooth;
    public String _brackAdulttooth;
    public String _checkAdulttooth;
    public String _otherdiseasecd;
    public String _otherdisease;
    public String _otherdiseasecd2;
    public String _otherdisease2;
    public String _dentistremarkcd;
    public String _dentistremark;
    public String _dentistremarkdate;
    public String _dentisttreatcd;
    public String _dentisttreat;
    public String _date;

    /**
     * コンストラクタ。
     */
    public MedexamToothDat(final DB2UDB db2, final String year, final String schregNo) throws SQLException {
        _db2 = db2;
        final String MtSql = getMtSql(year, schregNo);
        PreparedStatement psMt = null;
        ResultSet rsMt = null;
        try {
            psMt = _db2.prepareStatement(MtSql);
            rsMt = psMt.executeQuery();
            while (rsMt.next()) {
                _jawsJointcd = rsMt.getString("JAWS_JOINTCD");
                _jawsJointcd2 = rsMt.getString("JAWS_JOINTCD2");
                _jawsJointcd3 = rsMt.getString("JAWS_JOINTCD3");
                _plaquecd = rsMt.getString("PLAQUECD");
                _gumcd = rsMt.getString("GUMCD");
                _calculuscd = rsMt.getString("CALCULUS");
                _orthodontics = rsMt.getString("ORTHODONTICS");
                _upRBaby5 = rsMt.getString("UP_R_BABY5");
                _upRBaby4 = rsMt.getString("UP_R_BABY4");
                _upRBaby3 = rsMt.getString("UP_R_BABY3");
                _upRBaby2 = rsMt.getString("UP_R_BABY2");
                _upRBaby1 = rsMt.getString("UP_R_BABY1");
                _upLBaby1 = rsMt.getString("UP_L_BABY1");
                _upLBaby2 = rsMt.getString("UP_L_BABY2");
                _upLBaby3 = rsMt.getString("UP_L_BABY3");
                _upLBaby4 = rsMt.getString("UP_L_BABY4");
                _upLBaby5 = rsMt.getString("UP_L_BABY5");
                _lwRBaby5 = rsMt.getString("LW_R_BABY5");
                _lwRBaby4 = rsMt.getString("LW_R_BABY4");
                _lwRBaby3 = rsMt.getString("LW_R_BABY3");
                _lwRBaby2 = rsMt.getString("LW_R_BABY2");
                _lwRBaby1 = rsMt.getString("LW_R_BABY1");
                _lwLBaby1 = rsMt.getString("LW_L_BABY1");
                _lwLBaby2 = rsMt.getString("LW_L_BABY2");
                _lwLBaby3 = rsMt.getString("LW_L_BABY3");
                _lwLBaby4 = rsMt.getString("LW_L_BABY4");
                _lwLBaby5 = rsMt.getString("LW_L_BABY5");
                _babytooth = rsMt.getString("BABYTOOTH");
                _remainbabytooth = rsMt.getString("REMAINBABYTOOTH");
                _treatedbabytooth = rsMt.getString("TREATEDBABYTOOTH");
                _brackBabytooth = rsMt.getString("BRACK_BABYTOOTH");
                _upRAdult8 = rsMt.getString("UP_R_ADULT8");
                _upRAdult7 = rsMt.getString("UP_R_ADULT7");
                _upRAdult6 = rsMt.getString("UP_R_ADULT6");
                _upRAdult5 = rsMt.getString("UP_R_ADULT5");
                _upRAdult4 = rsMt.getString("UP_R_ADULT4");
                _upRAdult3 = rsMt.getString("UP_R_ADULT3");
                _upRAdult2 = rsMt.getString("UP_R_ADULT2");
                _upRAdult1 = rsMt.getString("UP_R_ADULT1");
                _upLAdult1 = rsMt.getString("UP_L_ADULT1");
                _upLAdult2 = rsMt.getString("UP_L_ADULT2");
                _upLAdult3 = rsMt.getString("UP_L_ADULT3");
                _upLAdult4 = rsMt.getString("UP_L_ADULT4");
                _upLAdult5 = rsMt.getString("UP_L_ADULT5");
                _upLAdult6 = rsMt.getString("UP_L_ADULT6");
                _upLAdult7 = rsMt.getString("UP_L_ADULT7");
                _upLAdult8 = rsMt.getString("UP_L_ADULT8");
                _lwRAdult8 = rsMt.getString("LW_R_ADULT8");
                _lwRAdult7 = rsMt.getString("LW_R_ADULT7");
                _lwRAdult6 = rsMt.getString("LW_R_ADULT6");
                _lwRAdult5 = rsMt.getString("LW_R_ADULT5");
                _lwRAdult4 = rsMt.getString("LW_R_ADULT4");
                _lwRAdult3 = rsMt.getString("LW_R_ADULT3");
                _lwRAdult2 = rsMt.getString("LW_R_ADULT2");
                _lwRAdult1 = rsMt.getString("LW_R_ADULT1");
                _lwLAdult1 = rsMt.getString("LW_L_ADULT1");
                _lwLAdult2 = rsMt.getString("LW_L_ADULT2");
                _lwLAdult3 = rsMt.getString("LW_L_ADULT3");
                _lwLAdult4 = rsMt.getString("LW_L_ADULT4");
                _lwLAdult5 = rsMt.getString("LW_L_ADULT5");
                _lwLAdult6 = rsMt.getString("LW_L_ADULT6");
                _lwLAdult7 = rsMt.getString("LW_L_ADULT7");
                _lwLAdult8 = rsMt.getString("LW_L_ADULT8");
                _adulttooth = rsMt.getString("ADULTTOOTH");
                _remainadulttooth = rsMt.getString("REMAINADULTTOOTH");
                _treatedadulttooth = rsMt.getString("TREATEDADULTTOOTH");
                _lostadulttooth = rsMt.getString("LOSTADULTTOOTH");
                _brackAdulttooth = rsMt.getString("BRACK_ADULTTOOTH");
                _otherdiseasecd = rsMt.getString("OTHERDISEASECD");
                _otherdiseasecd2 = rsMt.getString("OTHERDISEASECD2");
                _checkAdulttooth = rsMt.getString("CHECKADULTTOOTH");
                _otherdisease = rsMt.getString("OTHERDISEASE");
                _otherdisease2 = rsMt.getString("OTHERDISEASE2");
                _dentistremarkcd = rsMt.getString("DENTISTREMARKCD");
                _dentistremark = rsMt.getString("DENTISTREMARK");
                _dentistremarkdate = rsMt.getString("DENTISTREMARKDATE");
                _dentisttreatcd = rsMt.getString("DENTISTTREATCD");
                _dentisttreat = rsMt.getString("DENTISTTREAT");
                _date = rsMt.getString("TOOTH_DATE");
            }
        } finally {
            DbUtils.closeQuietly(null, psMt, rsMt);
            _db2.commit();
        }
    }

    private String getMtSql(final String year, final String schregNo) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.*, ");
        stb.append("     L1.TOOTH_DATE ");
        stb.append(" FROM ");
        stb.append("     V_MEDEXAM_TOOTH_DAT T1 ");
        stb.append("     LEFT JOIN MEDEXAM_HDAT L1 ON T1.YEAR = L1.YEAR ");
        stb.append("          AND T1.SCHREGNO = L1.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + year + "' ");
        stb.append("     AND T1.SCHREGNO = '" + schregNo + "' ");
        return stb.toString();
    }
}

// eof
