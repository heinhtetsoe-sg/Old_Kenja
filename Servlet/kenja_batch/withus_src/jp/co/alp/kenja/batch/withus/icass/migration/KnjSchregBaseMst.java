// kanji=漢字
/*
 * $Id: KnjSchregBaseMst.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/08/15 15:46:35 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * SCHREG_BASE_MSTを作る。
 * @author takaesu
 * @version $Id: KnjSchregBaseMst.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjSchregBaseMst extends AbstractKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjSchregBaseMst.class);

    public static final String ICASS_TABLE = "SEITO";

    public KnjSchregBaseMst() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "学籍基礎データ"; }

    void migrate() throws SQLException {
        final List list = loadIcass();
        log.debug(ICASS_TABLE + "データ件数=" + list.size());

        try {
            saveKnj(list);
        } catch (final SQLException e) {
            _db2.conn.rollback();
            log.fatal("更新処理中にエラー! rollback した。");
            throw e;
        }
    }

    private List loadIcass() throws SQLException {
        final List rtn = new ArrayList();

        // SQL実行
        final List result;
        try {
            final String sql = getSql();
            result = (List) _runner.query(_db2.conn, sql, _handler);//TODO: データ量が多いので溢れてしまう。対策を考えろ!
        } catch (final SQLException e) {
            log.error("ICASSデータ取込みでエラー", e);
            throw e;
        }

        // 結果の処理
        for (final Iterator it = result.iterator(); it.hasNext();) {
            final Map map = (Map) it.next();

            final SchregBase schregBase = new SchregBase(map);
            rtn.add(schregBase);
        }
        return rtn;
    }

    private String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH IDO_MAX AS ( ");
        stb.append(" SELECT ");
        stb.append("     SHIGANSHA_RENBAN, ");
        stb.append("     MAX(GAKUSEKI_JOTAI_KAISHI_NENGAPPI) AS GAKUSEKI_JOTAI_KAISHI_NENGAPPI ");
        stb.append(" FROM ");
        stb.append("     SEITO_GAKUSEKI_IDO_RIREKI ");
        stb.append(" WHERE ");
        stb.append("     GAKUSEKI_JOTAI_CODE IN ('3', '5', '6', '9') ");
        stb.append(" GROUP BY ");
        stb.append("     SHIGANSHA_RENBAN ");

        stb.append(" ), IDO_DATA AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SHIGANSHA_RENBAN, ");
        stb.append("     T1.GAKUSEKI_JOTAI_KAISHI_NENGAPPI, ");
        stb.append("     T1.GAKUSEKI_JOTAI_CODE, ");
        stb.append("     T1.GAKUSEKI_IDO_RIYU, ");
        stb.append("     T1.TENSHUTSU_SAKI_KOKO_KANRI_NO, ");
        stb.append("     L2.NAME, ");
        stb.append("     VALUE(L2.ADDR1, '') || VALUE(L2.ADDR2, '') || VALUE(L2.ADDR3, '') AS GRD_ADDR ");
        stb.append(" FROM ");
        stb.append("     SEITO_GAKUSEKI_IDO_RIREKI T1 ");
        stb.append("     LEFT JOIN KOKO_YOMIKAE L1 ON T1.TENSHUTSU_SAKI_KOKO_KANRI_NO = L1.KOKO_KANRI_NO ");
        stb.append("     LEFT JOIN FIN_HIGH_SCHOOL_MST L2 ON L1.SCHOOL_CD = L2.SCHOOL_CD, ");
        stb.append("     IDO_MAX T2 ");
        stb.append(" WHERE ");
        stb.append("     T1.SHIGANSHA_RENBAN = T2.SHIGANSHA_RENBAN ");
        stb.append("     AND T1.GAKUSEKI_JOTAI_KAISHI_NENGAPPI = T2.GAKUSEKI_JOTAI_KAISHI_NENGAPPI ");

        stb.append(" ), TAKO_MIN AS ( ");
        stb.append(" SELECT ");
        stb.append("     SHIGANSHA_RENBAN, ");
        stb.append("     CASE WHEN MONTH(MIN(ZAISEKI_KIKAN_F)) > 3 ");
        stb.append("          THEN YEAR(MIN(ZAISEKI_KIKAN_F)) ");
        stb.append("          ELSE YEAR(MIN(ZAISEKI_KIKAN_F)) - 1 ");
        stb.append("     END AS CURRICULUM_YEAR ");
        stb.append(" FROM ");
        stb.append("     SEITO_TAKO_ZAISEKI_RIREKI ");
        stb.append(" WHERE ");
        stb.append("     VALUE(ZAISEKI_KIKAN_F, '') <> '' ");
        stb.append(" GROUP BY ");
        stb.append("     SHIGANSHA_RENBAN ");

        stb.append(" ), JIKOGAI_MIN AS ( ");
        stb.append("  SELECT DISTINCT ");
        stb.append("      SHIGANSHA_RENBAN ");
        stb.append("  FROM ");
        stb.append("      SEITO_JIKOGAI_RISHU_KAMOKU ");        
        stb.append(" ) ");

        stb.append(" SELECT ");
        stb.append("     T1.SHIGANSHA_RENBAN, ");
        stb.append("     T1.SOTSUGYO_NO, ");
        stb.append("     T1.SOTSUGYO_NENGAPPI, ");
        stb.append("     T1.NYUGAKU_NENGAPPI, ");
        stb.append("     T1.SHUTSUGAN_NENGAPPI, ");
        stb.append("     CASE VALUE(T1.NYUGAKU_KEITAI_CODE, '') ");
        stb.append("          WHEN '1' THEN '2' ");
        stb.append("          WHEN '2' THEN '3' ");
        stb.append("          WHEN '3' THEN '4' ");
        stb.append("          WHEN '4' THEN '5' ");
        stb.append("          WHEN '9' THEN '9' ");
        stb.append("          ELSE '' ");
        stb.append("     END AS ENT_DIV, ");
        stb.append("     CASE WHEN L3.CURRICULUM_YEAR IS NOT NULL ");
        stb.append("          THEN L3.CURRICULUM_YEAR ");
        stb.append("          ELSE CASE WHEN MONTH(T1.NYUGAKU_NENGAPPI) > 3 ");
        stb.append("                    THEN YEAR(T1.NYUGAKU_NENGAPPI) ");
        stb.append("                    ELSE YEAR(T1.NYUGAKU_NENGAPPI) - 1 ");
        stb.append("               END ");
        stb.append("     END AS CURRICULUM_YEAR, ");
        stb.append("     T1.SHIMEI, ");
        stb.append("     T1.KANA_SHIMEI, ");
        stb.append("     CASE WHEN T1.SEIBETSU = '男' ");
        stb.append("          THEN '1' ");
        stb.append("          ELSE '2' ");
        stb.append("     END AS SEIBETSU, ");
        stb.append("     T1.KETSUEKIGATA, ");
        stb.append("     T1.SEINENGAPPI, ");
        stb.append("     T1.KEITAI_TEL_NO, ");
        stb.append("     T1.HOGOSHA_SHIMEI, ");
        stb.append("     T1.HOGOSHA_ZOKUGARA_CODE, ");
        stb.append("     L5.ZOKUGARA_NAME AS HOGOSHA_ZOKUGARA_MEI, ");        
        stb.append("     T1.HOGOSHA_TEL_NO, ");
        stb.append("     T1.HOGOSHA_KEITAI_TEL_NO, ");
        stb.append("     L2.SCHOOL_CD, ");
        stb.append("     T1.CHUGAKU_SOTSUGYO_NENTSUKI, ");
        stb.append("     T1.TOKKI_JIKO, ");
        stb.append("     L1.GAKUSEKI_JOTAI_KAISHI_NENGAPPI, ");
        stb.append("     CASE VALUE(L1.GAKUSEKI_JOTAI_CODE, '') ");
        stb.append("          WHEN '3' THEN '3' ");
        stb.append("          WHEN '5' THEN '2' ");
        stb.append("          WHEN '6' THEN '6' ");
        stb.append("          WHEN '9' THEN '1' ");
        stb.append("          ELSE '' ");
        stb.append("     END AS GRD_DIV, ");
        stb.append("     L1.GAKUSEKI_IDO_RIYU, ");
        stb.append("     L1.NAME, ");
        stb.append("     L1.GRD_ADDR, ");
        stb.append("     CASE ");
        stb.append("          WHEN L4.TENGAKU_BUNSHO_CODE ='05' THEN '1' ");
        stb.append("     END AS EDUCATION_REC_GET_FLG1, ");
        stb.append("     CASE ");
        stb.append("          WHEN VALUE(L6.SHIGANSHA_RENBAN, '') <> ''");
//        stb.append("          T1.SHIGANSHA_RENBAN IN JIKOGAI_MIN ");
        stb.append("              THEN '1' ");
        stb.append("     END AS EDUCATION_REC_GET_FLG2 ");
        stb.append(" FROM ");
        stb.append("     SEITO T1 ");
        stb.append("     LEFT JOIN IDO_DATA L1 ON T1.SHIGANSHA_RENBAN = L1.SHIGANSHA_RENBAN ");
        stb.append("     LEFT JOIN CHUGAKU_YOMIKAE L2 ON T1.CHUGAKU_KANRI_NO = L2.CHUGAKU_KANRI_NO ");
        stb.append("     LEFT JOIN TAKO_MIN L3 ON T1.SHIGANSHA_RENBAN = L3.SHIGANSHA_RENBAN ");
        stb.append("     LEFT JOIN TENGAKU_JUSHIN_BUNSHO L4 ON T1.SHIGANSHA_RENBAN = L4.SHIGANSHA_RENBAN AND L4.TENGAKU_BUNSHO_CODE = '05' ");        
        stb.append("     LEFT JOIN ZOKUGARA_MASTER L5 ON T1.HOGOSHA_ZOKUGARA_CODE = L5.ZOKUGARA_CODE ");        
        stb.append("     LEFT JOIN JIKOGAI_MIN L6 ON T1.SHIGANSHA_RENBAN = L6.SHIGANSHA_RENBAN ");
        stb.append(" WHERE ");
        stb.append("     VALUE(T1.SEITO_NO, '') <> ''");

        log.debug("sql=" + stb.toString());

        return stb.toString();
    }

    private class SchregBase {
        final String _shiganshaRenban;
        final String _schregno;
        final String _inoutcd;
        final String _name;
        final String _nameShow;
        final String _nameKana;
        final String _nameKanaShow;
        final String _nameEng;
        final String _birthday;
        final String _sex;
        final String _bloodType;
        final String _bloodRh;
        final String _claimSend;
        final String _applicantno;
        final String _curriculumYear;
        final String _specialDiv;
        final String _educationRecGetFlg;
        final String _educationRecPutFlg;
        final String _mobilePhoneNo;
        final String _finschoolcd;
        final String _finishDate;
        final String _prischoolcd;
        final String _entDate;
        final String _entDiv;
        final String _entReason;
        final String _entSchool;
        final String _entAddr;
        final String _grdDate;
        final String _grdDiv;
        final String _grdReason;
        final String _grdSchool;
        final String _grdAddr;
        final String _grdNo;
        final String _grdTerm;
        final String _grdScheduleDate;
        final String _grdRecognitFlg;
        final String _remark1;
        final String _remark2;
        final String _remark3;
        final String _emergencycall;
        final String _emergencyname;
        final String _emergencyrelaName;
        final String _emergencytelno;
        final String _emergencycall2;
        final String _emergencyname2;
        final String _emergencyrelaName2;
        final String _emergencytelno2;

        public SchregBase(final Map map) {
            _shiganshaRenban = (String) map.get("SHIGANSHA_RENBAN");
            _schregno = _param.getSchregno(_shiganshaRenban);
            _inoutcd = "";    // TODO:未定
            _name = (String) map.get("SHIMEI");
            _nameShow = (String) map.get("SHIMEI");
            _nameKana = (String) map.get("KANA_SHIMEI");
            _nameKanaShow = (String) map.get("KANA_SHIMEI");
            _nameEng = "";    // TODO:未定
            _birthday = (String) map.get("SEINENGAPPI");
            _sex = (String) map.get("SEIBETSU");
            _bloodType = (String) map.get("KETSUEKIGATA");
            _bloodRh = "";    // TODO:未定
            _claimSend = "2";    // 固定：保護者
            _applicantno = _param.getApplicantNo(_shiganshaRenban);
            _curriculumYear = String.valueOf((Number) map.get("CURRICULUM_YEAR"));
            _specialDiv = "";    // TODO:未定
            boolean educationRecGetFlg1 = "1".equals((String) map.get("EDUCATION_REC_GET_FLG1"));
            boolean educationRecGetFlg2 = "1".equals((String) map.get("EDUCATION_REC_GET_FLG2"));            
            _educationRecGetFlg = (educationRecGetFlg1 || educationRecGetFlg2) ? "1" : null;
            _educationRecPutFlg = (educationRecGetFlg1 || educationRecGetFlg2) ? "1" : null;
            _mobilePhoneNo = (String) map.get("KEITAI_TEL_NO");
            _finschoolcd = (String) map.get("SCHOOL_CD");
            _finishDate = (String) map.get("CHUGAKU_SOTSUGYO_NENTSUKI");
            _prischoolcd = "";    // TODO:未定
            _entDate = (String) map.get("NYUGAKU_NENGAPPI");
            _entDiv = (String) map.get("ENT_DIV");
            _entReason = "";    // TODO:未定
            _entSchool = "";    // TODO:未定
            _entAddr = "";    // TODO:未定
            _grdDate = (String) map.get("GAKUSEKI_JOTAI_KAISHI_NENGAPPI");
            _grdDiv = (String) map.get("GRD_DIV");
            final String[] grdReason = retDividString((String) map.get("GAKUSEKI_IDO_RIYU"), 25, 1);
            _grdReason = grdReason[0];  // TODO:とりあえず75文字を超えた分は、カット
            _grdSchool = (String) map.get("NAME");
            _grdAddr = (String) map.get("GRD_ADDR");
            _grdNo = (String) map.get("SOTSUGYO_NO");
            _grdTerm = "";    // TODO:未定
            _grdScheduleDate = (String) map.get("SOTSUGYO_NENGAPPI");
            _grdRecognitFlg = "";    // TODO:未定
            final String[] remark = retDividString((String) map.get("TOKKI_JIKO"), 25, 3);
            _remark1 = remark[0];    // TODO:とりあえず75文字を超えた分は、カット
            _remark2 = remark[1];    // TODO:とりあえず
            _remark3 = remark[2];    // TODO:とりあえず
            _emergencycall = "";    // TODO:未定
            _emergencyname = (String) map.get("HOGOSHA_SHIMEI");
            final String hogoshaZokugaraMei = (String) map.get("HOGOSHA_ZOKUGARA_MEI");
            _emergencyrelaName = hogoshaZokugaraMei;
            _emergencytelno = (String) map.get("HOGOSHA_TEL_NO");
            _emergencycall2 = "";    // TODO:未定
            _emergencyname2 = (String) map.get("HOGOSHA_SHIMEI");
            _emergencyrelaName2 = hogoshaZokugaraMei;
            _emergencytelno2 = (String) map.get("HOGOSHA_KEITAI_TEL_NO");
        }
        
    }

    /*
     * [db2inst1@withus db2inst1]$ db2 describe table SCHREG_BASE_MST
        列名                           スキーマ  タイプ名           長さ    位取り NULL
        ------------------------------ --------- ------------------ -------- ----- ------
        SCHREGNO                       SYSIBM    VARCHAR                   8     0 いいえ
        INOUTCD                        SYSIBM    VARCHAR                   1     0 はい
        NAME                           SYSIBM    VARCHAR                  60     0 はい
        NAME_SHOW                      SYSIBM    VARCHAR                  30     0 はい
        NAME_KANA                      SYSIBM    VARCHAR                 120     0 はい
        NAME_KANA_SHOW                 SYSIBM    VARCHAR                  60     0 はい
        NAME_ENG                       SYSIBM    VARCHAR                  40     0 はい
        BIRTHDAY                       SYSIBM    DATE                      4     0 はい
        SEX                            SYSIBM    VARCHAR                   1     0 はい
        BLOODTYPE                      SYSIBM    VARCHAR                   2     0 はい
        BLOOD_RH                       SYSIBM    VARCHAR                   1     0 はい
        CLAIM_SEND                     SYSIBM    VARCHAR                   1     0 はい
        APPLICANTNO                    SYSIBM    VARCHAR                   7     0 はい
        CURRICULUM_YEAR                SYSIBM    VARCHAR                   4     0 はい
        SPECIAL_DIV                    SYSIBM    VARCHAR                   1     0 はい
        EDUCATION_REC_GET_FLG          SYSIBM    VARCHAR                   1     0 はい
        EDUCATION_REC_PUT_FLG          SYSIBM    VARCHAR                   1     0 はい
        MOBILE_PHONE_NO                SYSIBM    VARCHAR                  14     0 はい
        FINSCHOOLCD                    SYSIBM    VARCHAR                  11     0 はい
        FINISH_DATE                    SYSIBM    DATE                      4     0 はい
        PRISCHOOLCD                    SYSIBM    VARCHAR                   7     0 はい
        ENT_DATE                       SYSIBM    DATE                      4     0 はい
        ENT_DIV                        SYSIBM    VARCHAR                   1     0 はい
        ENT_REASON                     SYSIBM    VARCHAR                  75     0 はい
        ENT_SCHOOL                     SYSIBM    VARCHAR                  75     0 はい
        ENT_ADDR                       SYSIBM    VARCHAR                  75     0 はい
        GRD_DATE                       SYSIBM    DATE                      4     0 はい
        GRD_DIV                        SYSIBM    VARCHAR                   1     0 はい
        GRD_REASON                     SYSIBM    VARCHAR                  75     0 はい
        GRD_SCHOOL                     SYSIBM    VARCHAR                  75     0 はい
        GRD_ADDR                       SYSIBM    VARCHAR                  75     0 はい
        GRD_NO                         SYSIBM    VARCHAR                   8     0 はい
        GRD_TERM                       SYSIBM    VARCHAR                   4     0 はい
        GRD_SCHEDULE_DATE              SYSIBM    DATE                      4     0 はい
        GRD_RECOGNIT_FLG               SYSIBM    VARCHAR                   1     0 はい
        REMARK1                        SYSIBM    VARCHAR                  75     0 はい
        REMARK2                        SYSIBM    VARCHAR                  75     0 はい
        REMARK3                        SYSIBM    VARCHAR                  75     0 はい
        EMERGENCYCALL                  SYSIBM    VARCHAR                  60     0 はい
        EMERGENCYNAME                  SYSIBM    VARCHAR                  60     0 はい
        EMERGENCYRELA_NAME             SYSIBM    VARCHAR                  30     0 はい
        EMERGENCYTELNO                 SYSIBM    VARCHAR                  14     0 はい
        EMERGENCYCALL2                 SYSIBM    VARCHAR                  60     0 はい
        EMERGENCYNAME2                 SYSIBM    VARCHAR                  60     0 はい
        EMERGENCYRELA_NAME2            SYSIBM    VARCHAR                  30     0 はい
        EMERGENCYTELNO2                SYSIBM    VARCHAR                  14     0 はい
        REGISTERCD                     SYSIBM    VARCHAR                   8     0 はい
        UPDATED                        SYSIBM    TIMESTAMP                10     0 はい
        
        48 レコードが選択されました。
     */
    private void saveKnj(final List list) throws SQLException {
        int totalCount = 0;
        ResultSet rs = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final SchregBase schregBase = (SchregBase) it.next();
            final String insSql = getInsertSql(schregBase);
            try{
                _db2.stmt.executeUpdate(insSql);
            } catch(SQLException e) {
                log.error("SQLException: " + e.getMessage()+ ":" + ((SQLException)e).getSQLState());
                throw e;
            }
            totalCount++;
        }
        DbUtils.closeQuietly(rs);
        log.warn("挿入件数=" + totalCount);
    }

    private String getInsertSql(final SchregBase schregBase) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" INSERT INTO SCHREG_BASE_MST ");
        stb.append(" VALUES( ");
        stb.append(" " + getInsertVal(schregBase._schregno) + ", ");
        stb.append(" " + getInsertVal(schregBase._inoutcd) + ", ");
        stb.append(" " + getInsertVal(schregBase._name) + ", ");
        if (schregBase._nameShow.length() > 10) {
            stb.append(" " + getInsertVal(schregBase._nameShow.substring(0, 10)) + ", ");
        } else {
            stb.append(" " + getInsertVal(schregBase._nameShow) + ", ");
        }
        stb.append(" " + getInsertVal(schregBase._nameKana) + ", ");
        stb.append(" " + getInsertVal(schregBase._nameKanaShow) + ", ");
        stb.append(" " + getInsertVal(schregBase._nameEng) + ", ");
        stb.append(" " + getInsertVal(schregBase._birthday) + ", ");
        stb.append(" " + getInsertVal(schregBase._sex) + ", ");
        stb.append(" " + getInsertVal(schregBase._bloodType) + ", ");
        stb.append(" " + getInsertVal(schregBase._bloodRh) + ", ");
        stb.append(" " + getInsertVal(schregBase._claimSend) + ", ");
        stb.append(" " + getInsertVal(schregBase._applicantno) + ", ");
        stb.append(" " + getInsertVal(schregBase._curriculumYear) + ", ");
        stb.append(" " + getInsertVal(schregBase._specialDiv) + ", ");
        stb.append(" " + getInsertVal(schregBase._educationRecGetFlg) + ", ");
        stb.append(" " + getInsertVal(schregBase._educationRecPutFlg) + ", ");
        stb.append(" " + getInsertVal(schregBase._mobilePhoneNo) + ", ");
        stb.append(" " + getInsertVal(schregBase._finschoolcd) + ", ");
        stb.append(" " + getInsertVal(schregBase._finishDate) + ", ");
        stb.append(" " + getInsertVal(schregBase._prischoolcd) + ", ");
        stb.append(" " + getInsertVal(schregBase._entDate) + ", ");
        stb.append(" " + getInsertVal(schregBase._entDiv) + ", ");
        stb.append(" " + getInsertVal(schregBase._entReason) + ", ");
        stb.append(" " + getInsertVal(schregBase._entSchool) + ", ");
        stb.append(" " + getInsertVal(schregBase._entAddr) + ", ");
        stb.append(" " + getInsertVal(schregBase._grdDate) + ", ");
        stb.append(" " + getInsertVal(schregBase._grdDiv) + ", ");
        stb.append(" " + getInsertVal(schregBase._grdReason) + ", ");
        stb.append(" " + getInsertVal(schregBase._grdSchool) + ", ");
        stb.append(" " + getInsertVal(schregBase._grdAddr) + ", ");
        stb.append(" " + getInsertVal(schregBase._grdNo) + ", ");
        stb.append(" " + getInsertVal(schregBase._grdTerm) + ", ");
        stb.append(" " + getInsertVal(schregBase._grdScheduleDate) + ", ");
        stb.append(" " + getInsertVal(schregBase._grdRecognitFlg) + ", ");
        stb.append(" " + getInsertVal(schregBase._remark1) + ", ");
        stb.append(" " + getInsertVal(schregBase._remark2) + ", ");
        stb.append(" " + getInsertVal(schregBase._remark3) + ", ");
        stb.append(" " + getInsertVal(schregBase._emergencycall) + ", ");
        stb.append(" " + getInsertVal(schregBase._emergencyname) + ", ");
        stb.append(" " + getInsertVal(schregBase._emergencyrelaName) + ", ");
        stb.append(" " + getInsertVal(schregBase._emergencytelno) + ", ");
        stb.append(" " + getInsertVal(schregBase._emergencycall2) + ", ");
        stb.append(" " + getInsertVal(schregBase._emergencyname2) + ", ");
        stb.append(" " + getInsertVal(schregBase._emergencyrelaName2) + ", ");
        stb.append(" " + getInsertVal(schregBase._emergencytelno2) + ", ");
        stb.append(" '" + Param.REGISTERCD + "', ");
        stb.append(" current timestamp ");
        stb.append(" ) ");

        return stb.toString();
    }
}
// eof

