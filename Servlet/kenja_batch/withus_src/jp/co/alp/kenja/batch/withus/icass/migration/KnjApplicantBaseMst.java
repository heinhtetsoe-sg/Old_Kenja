// kanji=漢字
/*
 * $Id: KnjApplicantBaseMst.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/08/15 15:46:35 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * APPLICANT_BASE_MSTを作る。
 * @author takaesu
 * @version $Id: KnjApplicantBaseMst.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjApplicantBaseMst extends AbstractKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjApplicantBaseMst.class);

    public static final String KNJ_TABLE = "APPLICANT_BASE_MST";
    public static final String ICASS_TABLE = "SEITO";

    public KnjApplicantBaseMst() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "志願者データ"; }

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

            final Applicant applicant = new Applicant(map);
            rtn.add(applicant);
        }
        return rtn;
    }

    private String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SHOZOKU_MAX AS ( ");
        stb.append(" SELECT ");
        stb.append("     SHIGANSHA_RENBAN, ");
        stb.append("     MAX(SHOZOKU_KAISHI_NENGAPPI) AS SHOZOKU_KAISHI_NENGAPPI ");
        stb.append(" FROM ");
        stb.append("     SEITO_SHOZOKU ");
        stb.append(" GROUP BY ");
        stb.append("     SHIGANSHA_RENBAN ");
        stb.append(" ), SHOZOKU AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SHIGANSHA_RENBAN, ");
        stb.append("     T1.KATEI_CODE, ");
        stb.append("     T1.GAKKA_CODE, ");
        stb.append("     T1.SENKO_CODE, ");
        stb.append("     T1.COURSE_CODE, ");
        stb.append("     T1.GAKUSHU_KYOTEN_CODE ");
        stb.append(" FROM ");
        stb.append("     SEITO_SHOZOKU T1, ");
        stb.append("     SHOZOKU_MAX T2 ");
        stb.append(" WHERE ");
        stb.append("     T1.SHIGANSHA_RENBAN = T2.SHIGANSHA_RENBAN ");
        stb.append("     AND T1.SHOZOKU_KAISHI_NENGAPPI = T2.SHOZOKU_KAISHI_NENGAPPI ");
        stb.append(" ), MIN_SHIBO_JUNI AS ( ");
        stb.append("     SELECT SHIGANSHA_RENBAN, MIN(INT(SHIBO_JUNI)) AS SHIBO_JUNI ");
        stb.append("     FROM SHIBO ");
        stb.append("     GROUP BY SHIGANSHA_RENBAN, INT(SHIBO_JUNI) ");
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
        stb.append(" ), MIN_SHIBO AS ( ");
        stb.append("     SELECT T1.SHIGANSHA_RENBAN, GAKUSHU_KYOTEN_CODE, KATEI_CODE, GAKKA_CODE, SENKO_CODE, COURSE_CODE ");
        stb.append("     FROM SHIBO T1 ");
        stb.append("     LEFT JOIN MIN_SHIBO_JUNI L1 ON ");
        stb.append("         T1.SHIGANSHA_RENBAN = L1.SHIGANSHA_RENBAN AND ");
        stb.append("         INT(T1.SHIBO_JUNI) = L1.SHIBO_JUNI ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.SHIGANSHA_RENBAN, ");
        stb.append("     T1.NYUGAKU_NENDO, ");
        stb.append("     T1.SHUTSUGAN_NENGAPPI, ");
        stb.append("     T1.NYUGAKU_NENGAPPI, ");
        stb.append("     T1.SOTSUGYO_NENGAPPI, ");
        stb.append("     T1.SHUTSUGAN_KEITAI_CODE, ");
        stb.append("     T1.NYUGAKU_KEITAI_CODE, ");
        stb.append("     CASE WHEN T1.NYUGAKU_KEITAI_CODE = '9' ");
        stb.append("          THEN '2' ");
        stb.append("          ELSE '1' ");
        stb.append("     END AS COURSE_DIV, ");
        stb.append("     CASE WHEN L4.CURRICULUM_YEAR IS NOT NULL ");
        stb.append("          THEN L4.CURRICULUM_YEAR ");
        stb.append("          ELSE CASE WHEN MONTH(T1.NYUGAKU_NENGAPPI) > 3 ");
        stb.append("                    THEN YEAR(T1.NYUGAKU_NENGAPPI) ");
        stb.append("                    ELSE YEAR(T1.NYUGAKU_NENGAPPI) - 1 ");
        stb.append("               END ");
        stb.append("     END AS CURRICULUM_YEAR, ");
        stb.append("     T1.NYUUGAKU_NENJI, ");
        stb.append("     T1.SHIMEI, ");
        stb.append("     T1.KANA_SHIMEI, ");
        stb.append("     T1.KOKUSEKI, ");
        stb.append("     CASE WHEN T1.SEIBETSU = '男' ");
        stb.append("          THEN '1' ");
        stb.append("          ELSE '2' ");
        stb.append("     END AS SEIBETSU, ");
        stb.append("     T1.SEINENGAPPI, ");
        stb.append("     T1.YUBIN_NO, ");
        stb.append("     T1.TODOFUKEN_NO, ");
        stb.append("     T1.ADDRESS1, ");
        stb.append("     T1.ADDRESS2, ");
        stb.append("     T1.TEL_NO, ");
        stb.append("     T1.HOGOSHA_SHIMEI, ");
        stb.append("     T1.HOGOSHA_KANA_SHIMEI, ");
        stb.append("     T1.HOGOSHA_ZOKUGARA_CODE, ");
        stb.append("     T1.HOGOSHA_YUBIN_NO, ");
        stb.append("     T1.HOGOSHA_TODOFUKEN_NO, ");
        stb.append("     T1.HOGOSHA_ADDRESS1, ");
        stb.append("     T1.HOGOSHA_ADDRESS2, ");
        stb.append("     T1.HOGOSHA_TEL_NO, ");
        stb.append("     L2.SCHOOL_CD, ");
        stb.append("     T1.CHUGAKU_SOTSUGYO_NENTSUKI, ");
        stb.append("     CASE WHEN T1.GOHI_CODE = '1' ");
        stb.append("          THEN '3' ");
        stb.append("          ELSE '1' ");
        stb.append("     END AS GOHI_CODE, ");
        stb.append("     CASE WHEN T1.GOHI_CODE = '1' AND T1.NYUGAKU_JITAI_FLAG = '1' ");
        stb.append("          THEN '4' ");
        stb.append("          ELSE CASE WHEN T1.GOHI_CODE = '2' AND T1.NYUGAKU_JITAI_FLAG = '1' ");
        stb.append("                    THEN '2' ");
        stb.append("                    ELSE '' ");
        stb.append("               END ");
        stb.append("     END AS NYUGAKU_JITAI_FLAG, ");
        stb.append("     VALUE(VALUE(L3.KATEI_CODE,L1.KATEI_CODE), '9') AS KATEI_CODE, ");
        stb.append("     VALUE(VALUE(L3.GAKKA_CODE,L1.GAKKA_CODE), '99') AS GAKKA_CODE, ");
        stb.append("     VALUE(VALUE(L3.SENKO_CODE,L1.SENKO_CODE), '99') AS SENKO_CODE, ");
        stb.append("     VALUE(VALUE(L3.COURSE_CODE,L1.COURSE_CODE), '99') AS COURSE_CODE, ");
        stb.append("     VALUE(VALUE(L3.GAKUSHU_KYOTEN_CODE,L1.GAKUSHU_KYOTEN_CODE), '999') AS GAKUSHU_KYOTEN_CODE ");
        stb.append(" FROM ");
        stb.append("     SEITO T1 ");
        stb.append("     LEFT JOIN SHOZOKU L1 ON T1.SHIGANSHA_RENBAN = L1.SHIGANSHA_RENBAN ");
        stb.append("     LEFT JOIN CHUGAKU_YOMIKAE L2 ON T1.CHUGAKU_KANRI_NO = L2.CHUGAKU_KANRI_NO ");
        stb.append("     LEFT JOIN MIN_SHIBO L3 ON T1.SHIGANSHA_RENBAN = L3.SHIGANSHA_RENBAN ");
        stb.append("     LEFT JOIN TAKO_MIN L4 ON T1.SHIGANSHA_RENBAN = L4.SHIGANSHA_RENBAN ");
// TODO:java.lang.OutOfMemoryErrorが出たので、5000未満と5000以上で取り込んだ
//        stb.append(" WHERE ");
//        stb.append("     T1.SHIGANSHA_RENBAN >= '5000' ");
//        stb.append("     T1.SHIGANSHA_RENBAN < '5000' ");

        log.debug("sql=" + stb.toString());

        return stb.toString();
    }

    private class Applicant {
        final String _shiganshaRenban;
        final String _year;
        final String _applicantNo;
        final String _applicantDiv;
        final String _courseDiv;
        final String _belongingDiv;
        final String _applicationDate;
        final String _applicationForm;
        final String _procedureDiv;
        final String _entScheduleDate;
        final String _entAnnual;
        final String _curriculumYear;
        final String _claimSend;
        final String _mannerPayment;
        final Integer _credit;
        final String _remark1;
        final String _remark2;
        final String _grdScheduleDate;
        final String _schregno;
        final String _studentDiv;
        final String _coursecd;
        final String _majorcd;
        final String _coursecode;
        final String _name;
        final String _nameShow;
        final String _nameKana;
        final String _nameKanaShow;
        final String _sex;
        final String _birthday;
        final String _nationality;
        final String _addresscd;
        final String _zipcd;
        final String _prefCd;
        final String _addr1;
        final String _addr2;
        final String _addr3;
        final String _telno;
        final String _telnoSearch;
        final String _locationcd;
        final String _natpubpridiv;
        final String _fsCd;
        final String _fsGrddate;
        final String _fsGrdDiv;
        final String _gname;
        final String _gkana;
        final String _gsex;
        final String _grelationShip;
        final String _gzipCd;
        final String _gprefCd;
        final String _gAddr1;
        final String _gAddr2;
        final String _gAddr3;
        final String _gTelNo;
        final String _gTelNoSearch;
        final String _guarantorName;
        final String _guarantorKana;
        final String _guarantorSex;
        final String _guarantorRelationship;
        final String _guarantorZipcd;
        final String _guarantorPrefCd;
        final String _guarantorAddr1;
        final String _guarantorAddr2;
        final String _guarantorAddr3;
        final String _guarantorTelno;
        final String _guarantorTelnoSearch;
        final String _remark;

        public Applicant(final Map map) {
            _shiganshaRenban = (String) map.get("SHIGANSHA_RENBAN");

            _year = (String) map.get("NYUGAKU_NENDO");
            _applicantNo = _param.getApplicantNo(_shiganshaRenban);
            _applicantDiv = (String) map.get("NYUGAKU_KEITAI_CODE");
            _courseDiv = (String) map.get("COURSE_DIV");
            _belongingDiv = (String) map.get("GAKUSHU_KYOTEN_CODE");
            _applicationDate = (String) map.get("SHUTSUGAN_NENGAPPI");
            _applicationForm = (String) map.get("SHUTSUGAN_KEITAI_CODE");
            _schregno = _param.getSchregno(_shiganshaRenban);
            final String gouhi = (String) map.get("GOHI_CODE");
            final String jitai = (String) map.get("NYUGAKU_JITAI_FLAG");
            _procedureDiv = jitai.equals("") ? gouhi : jitai;
            _entScheduleDate = (String) map.get("NYUGAKU_NENGAPPI");
            _entAnnual = (String) map.get("NYUUGAKU_NENJI");
            _curriculumYear = null != (Number) map.get("CURRICULUM_YEAR") ? ((Number) map.get("CURRICULUM_YEAR")).toString() : null;
            _claimSend = "2";   // 固定：保護者
            _mannerPayment = "";    // TODO:未定
            _credit = new Integer(32); // 伝票データ移行後に再セット
            _remark1 = "";
            _remark2 = "";
            _grdScheduleDate = (String) map.get("SOTSUGYO_NENGAPPI");
            final String courseMst = (String) map.get("KATEI_CODE") + (String) map.get("GAKKA_CODE") + (String) map.get("SENKO_CODE") + (String) map.get("COURSE_CODE");
            _studentDiv = _param.getStudentDiv((String) map.get("GAKUSHU_KYOTEN_CODE"), courseMst);
            _coursecd = (String) map.get("KATEI_CODE");
            _majorcd = (String) map.get("GAKKA_CODE");
            _coursecode = (String) map.get("COURSE_CODE");
            _name = (String) map.get("SHIMEI");
            _nameShow = (String) map.get("SHIMEI");
            _nameKana = (String) map.get("KANA_SHIMEI");
            _nameKanaShow = (String) map.get("KANA_SHIMEI");
            _sex = (String) map.get("SEIBETSU");
            _birthday = (String) map.get("SEINENGAPPI");
            _nationality = (String) map.get("KOKUSEKI");
            _addresscd = "";    // TODO:未定
            _zipcd = (String) map.get("YUBIN_NO");
            _prefCd = (String) map.get("TODOFUKEN_NO");
            final String[] addr = divideStr((String) map.get("ADDRESS1"));
            _addr1 = addr[0];
            _addr2 = addr[1]; 
            _addr3 = (String) map.get("ADDRESS2");
            _telno = (String) map.get("TEL_NO");
            _telnoSearch = deleteStr(_telno, "-");
            _locationcd = "";   // TODO:未定
            _natpubpridiv = ""; // TODO:データ移行後に学校データからセット
            _fsCd = (String) map.get("SCHOOL_CD");
            _fsGrddate = (String) map.get("CHUGAKU_SOTSUGYO_NENTSUKI");
            _fsGrdDiv = "1";    // 固定1：卒業
            _gname = (String) map.get("HOGOSHA_SHIMEI");
            _gkana = (String) map.get("HOGOSHA_KANA_SHIMEI");
            final String zokugara = (String) map.get("HOGOSHA_ZOKUGARA_CODE");
            _gsex = _param.getZokugaraSex(zokugara);
            _grelationShip = _param.getZokugara(zokugara);
            _gzipCd = (String) map.get("HOGOSHA_YUBIN_NO");
            _gprefCd = (String) map.get("HOGOSHA_TODOFUKEN_NO");
            final String[] gAddr = divideStr((String) map.get("HOGOSHA_ADDRESS1"));
            _gAddr1 = gAddr[0];
            _gAddr2 = gAddr[1];
            _gAddr3 = (String) map.get("HOGOSHA_ADDRESS2");
            _gTelNo = (String) map.get("HOGOSHA_TEL_NO");
            _gTelNoSearch = deleteStr(_gTelNo, "-");
            _guarantorName = null;
            _guarantorKana = "";
            _guarantorSex = "";
            _guarantorRelationship = "";
            _guarantorZipcd = "";
            _guarantorPrefCd = "";
            _guarantorAddr1 = "";
            _guarantorAddr2 = "";
            _guarantorAddr3 = "";
            _guarantorTelno = "";
            _guarantorTelnoSearch = "";
            _remark = _shiganshaRenban; // TODO:テスト効率上げる為、志願者連番
        }

    }

    /*
     * [db2inst1@withus db2inst1]$ db2 describe table APPLICANT_BASE_MST
                                       タイプ・
        列名                           スキーマ  タイプ名           長さ    位取り NULL
        ------------------------------ --------- ------------------ -------- ----- ------
        YEAR                           SYSIBM    VARCHAR                   4     0 いいえ
        APPLICANTNO                    SYSIBM    VARCHAR                   7     0 いいえ
        APPLICANT_DIV                  SYSIBM    VARCHAR                   1     0 はい
        COURSE_DIV                     SYSIBM    VARCHAR                   1     0 はい
        BELONGING_DIV                  SYSIBM    VARCHAR                   3     0 はい
        APPLICATION_DATE               SYSIBM    DATE                      4     0 はい
        APPLICATION_FORM               SYSIBM    VARCHAR                   1     0 はい
        PROCEDURE_DIV                  SYSIBM    VARCHAR                   1     0 はい
        ENT_SCHEDULE_DATE              SYSIBM    DATE                      4     0 はい
        ENT_ANNUAL                     SYSIBM    VARCHAR                   2     0 はい
        CURRICULUM_YEAR                SYSIBM    VARCHAR                   4     0 はい
        CLAIM_SEND                     SYSIBM    VARCHAR                   1     0 はい
        MANNER_PAYMENT                 SYSIBM    VARCHAR                   1     0 はい
        CREDIT                         SYSIBM    SMALLINT                  2     0 はい
        REMARK1                        SYSIBM    VARCHAR                   9     0 はい
        REMARK2                        SYSIBM    VARCHAR                   9     0 はい
        GRD_SCHEDULE_DATE              SYSIBM    DATE                      4     0 はい
        SCHREGNO                       SYSIBM    VARCHAR                   8     0 はい
        STUDENT_DIV                    SYSIBM    VARCHAR                   2     0 はい
        COURSECD                       SYSIBM    VARCHAR                   1     0 はい
        MAJORCD                        SYSIBM    VARCHAR                   3     0 はい
        COURSECODE                     SYSIBM    VARCHAR                   4     0 はい
        NAME                           SYSIBM    VARCHAR                  60     0 はい
        NAME_SHOW                      SYSIBM    VARCHAR                  30     0 はい
        NAME_KANA                      SYSIBM    VARCHAR                 120     0 はい
        NAME_KANA_SHOW                 SYSIBM    VARCHAR                  60     0 はい
        SEX                            SYSIBM    VARCHAR                   1     0 はい
        BIRTHDAY                       SYSIBM    DATE                      4     0 はい
        NATIONALITY                    SYSIBM    VARCHAR                  69     0 はい
        ADDRESSCD                      SYSIBM    VARCHAR                   2     0 はい
        ZIPCD                          SYSIBM    VARCHAR                   8     0 はい
        PREF_CD                        SYSIBM    VARCHAR                   2     0 はい
        ADDR1                          SYSIBM    VARCHAR                  75     0 はい
        ADDR2                          SYSIBM    VARCHAR                  75     0 はい
        ADDR3                          SYSIBM    VARCHAR                  75     0 はい
        TELNO                          SYSIBM    VARCHAR                  14     0 はい
        TELNO_SEARCH                   SYSIBM    VARCHAR                  14     0 はい
        LOCATIONCD                     SYSIBM    VARCHAR                   2     0 はい
        NATPUBPRIDIV                   SYSIBM    VARCHAR                   1     0 はい
        FS_CD                          SYSIBM    VARCHAR                  11     0 はい
        FS_GRDDATE                     SYSIBM    DATE                      4     0 はい
        FS_GRD_DIV                     SYSIBM    VARCHAR                   1     0 はい
        GNAME                          SYSIBM    VARCHAR                  60     0 はい
        GKANA                          SYSIBM    VARCHAR                 120     0 はい
        GSEX                           SYSIBM    VARCHAR                   1     0 はい
        GRELATIONSHIP                  SYSIBM    VARCHAR                   2     0 はい
        GZIPCD                         SYSIBM    VARCHAR                   8     0 はい
        GPREF_CD                       SYSIBM    VARCHAR                   2     0 はい
        GADDR1                         SYSIBM    VARCHAR                  75     0 はい
        GADDR2                         SYSIBM    VARCHAR                  75     0 はい
        GADDR3                         SYSIBM    VARCHAR                  75     0 はい
        GTELNO                         SYSIBM    VARCHAR                  14     0 はい
        GTELNO_SEARCH                  SYSIBM    VARCHAR                  14     0 はい
        GUARANTOR_NAME                 SYSIBM    VARCHAR                  60     0 はい
        GUARANTOR_KANA                 SYSIBM    VARCHAR                 120     0 はい
        GUARANTOR_SEX                  SYSIBM    VARCHAR                   1     0 はい
        GUARANTOR_RELATIONSHIP         SYSIBM    VARCHAR                   2     0 はい
        GUARANTOR_ZIPCD                SYSIBM    VARCHAR                   8     0 はい
        GUARANTOR_PREF_CD              SYSIBM    VARCHAR                   2     0 はい
        GUARANTOR_ADDR1                SYSIBM    VARCHAR                  75     0 はい
        GUARANTOR_ADDR2                SYSIBM    VARCHAR                  75     0 はい
        GUARANTOR_ADDR3                SYSIBM    VARCHAR                  75     0 はい
        GUARANTOR_TELNO                SYSIBM    VARCHAR                  14     0 はい
        GUARANTOR_TELNO_SEARCH         SYSIBM    VARCHAR                  14     0 はい
        REMARK                         SYSIBM    VARCHAR                  45     0 はい
        REGISTERCD                     SYSIBM    VARCHAR                   8     0 はい
        UPDATED                        SYSIBM    TIMESTAMP                10     0 はい
        
        67 レコードが選択されました。
     */
    private void saveKnj(final List list) throws SQLException {
        int totalCount = 0;
        ResultSet rs = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Applicant applicant = (Applicant) it.next();
            final String insSql = getInsertSql(applicant);
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

    private String getInsertSql(final Applicant applicant) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" INSERT INTO APPLICANT_BASE_MST ");
        stb.append(" VALUES( ");
        stb.append(" " + getInsertVal(applicant._year) + ", ");
        stb.append(" " + getInsertVal(applicant._applicantNo) + ", ");
        stb.append(" " + getInsertVal(applicant._applicantDiv) + ", ");
        stb.append(" " + getInsertVal(applicant._courseDiv) + ", ");
        stb.append(" " + getInsertVal(applicant._belongingDiv) + ", ");
        stb.append(" " + getInsertVal(applicant._applicationDate) + ", ");
        stb.append(" " + getInsertVal(applicant._applicationForm) + ", ");
        stb.append(" " + getInsertVal(applicant._procedureDiv) + ", ");
        stb.append(" " + getInsertVal(applicant._entScheduleDate) + ", ");
        stb.append(" " + getInsertVal(applicant._entAnnual) + ", ");
        stb.append(" " + getInsertVal(applicant._curriculumYear) + ", ");
        stb.append(" " + getInsertVal(applicant._claimSend) + ", ");
        stb.append(" " + getInsertVal(applicant._mannerPayment) + ", ");
        stb.append(" " + getInsertVal(applicant._credit) + ", ");
        stb.append(" " + getInsertVal(applicant._remark1) + ", ");
        stb.append(" " + getInsertVal(applicant._remark2) + ", ");
        stb.append(" " + getInsertVal(applicant._grdScheduleDate) + ", ");
        stb.append(" " + getInsertVal(applicant._schregno) + ", ");
        stb.append(" " + getInsertVal(applicant._studentDiv) + ", ");
        stb.append(" " + getInsertVal(applicant._coursecd) + ", ");
        stb.append(" " + getInsertVal(applicant._majorcd) + ", ");
        stb.append(" " + getInsertVal(applicant._coursecode) + ", ");
        stb.append(" " + getInsertVal(applicant._name) + ", ");
        if (applicant._nameShow.length() > 10) {
            stb.append(" " + getInsertVal(applicant._nameShow.substring(0, 10)) + ", ");
        } else {
            stb.append(" " + getInsertVal(applicant._nameShow) + ", ");
        }
        stb.append(" " + getInsertVal(applicant._nameKana) + ", ");
        stb.append(" " + getInsertVal(applicant._nameKanaShow) + ", ");
        stb.append(" " + getInsertVal(applicant._sex) + ", ");
        stb.append(" " + getInsertVal(applicant._birthday) + ", ");
        stb.append(" " + getInsertVal(applicant._nationality) + ", ");
        stb.append(" " + getInsertVal(applicant._addresscd) + ", ");
        stb.append(" " + getInsertVal(applicant._zipcd) + ", ");
        stb.append(" " + getInsertVal(applicant._prefCd) + ", ");
        stb.append(" " + getInsertChangeVal(applicant._addr1) + ", ");
        stb.append(" " + getInsertChangeVal(applicant._addr2) + ", ");
        stb.append(" " + getInsertChangeVal(applicant._addr3) + ", ");
        stb.append(" " + getInsertVal(applicant._telno) + ", ");
        stb.append(" " + getInsertVal(applicant._telnoSearch) + ", ");
        stb.append(" " + getInsertVal(applicant._locationcd) + ", ");
        stb.append(" " + getInsertVal(applicant._natpubpridiv) + ", ");
        stb.append(" " + getInsertVal(applicant._fsCd) + ", ");
        stb.append(" " + getInsertVal(applicant._fsGrddate) + ", ");
        stb.append(" " + getInsertVal(applicant._fsGrdDiv) + ", ");
        stb.append(" " + getInsertVal(applicant._gname) + ", ");
        stb.append(" " + getInsertVal(applicant._gkana) + ", ");
        stb.append(" " + getInsertVal(applicant._gsex) + ", ");
        stb.append(" " + getInsertVal(applicant._grelationShip) + ", ");
        stb.append(" " + getInsertVal(applicant._gzipCd) + ", ");
        stb.append(" " + getInsertVal(applicant._gprefCd) + ", ");
        stb.append(" " + getInsertChangeVal(applicant._gAddr1) + ", ");
        stb.append(" " + getInsertChangeVal(applicant._gAddr2) + ", ");
        stb.append(" " + getInsertChangeVal(applicant._gAddr3) + ", ");
        stb.append(" " + getInsertVal(applicant._gTelNo) + ", ");
        stb.append(" " + getInsertVal(applicant._gTelNoSearch) + ", ");
        stb.append(" " + getInsertVal(applicant._guarantorName) + ", ");
        stb.append(" " + getInsertVal(applicant._guarantorKana) + ", ");
        stb.append(" " + getInsertVal(applicant._guarantorSex) + ", ");
        stb.append(" " + getInsertVal(applicant._guarantorRelationship) + ", ");
        stb.append(" " + getInsertVal(applicant._guarantorZipcd) + ", ");
        stb.append(" " + getInsertVal(applicant._guarantorPrefCd) + ", ");
        stb.append(" " + getInsertChangeVal(applicant._guarantorAddr1) + ", ");
        stb.append(" " + getInsertChangeVal(applicant._guarantorAddr2) + ", ");
        stb.append(" " + getInsertChangeVal(applicant._guarantorAddr3) + ", ");
        stb.append(" " + getInsertVal(applicant._guarantorTelno) + ", ");
        stb.append(" " + getInsertVal(applicant._guarantorTelnoSearch) + ", ");
        stb.append(" " + getInsertVal(applicant._remark) + ", ");
        stb.append(" '" + Param.REGISTERCD + "', ");
        stb.append(" current timestamp ");
        stb.append(" ) ");

        return stb.toString();
    }
}
// eof

