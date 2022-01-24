// kanji=漢字
/*
 * $Id: KnjBelongingMst.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/08/06 16:16:24 - JST
 * 作成者: takara
 *
 * Copyright(C) 2004-2008 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

/**
 * BELONGING_MSTを作る。
 * @author takara
 * @version $Id: KnjBelongingMst.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjBelongingMst extends AbstractKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjRecReportDat.class);

    public static final String KNJ_TABLE = "BELONGING_MST";
    public static final String ICASS_TABLE = "GAKUSHU_KYOTEN";

    public KnjBelongingMst() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "所属マスタ"; }

    public void migrate() throws SQLException {
        final List list = loadIcass();
        log.debug(ICASS_TABLE + "データ件数=" + list.size());

        saveKnj(list);
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
            final String gakushuKyotenCode = (String) map.get("GAKUSHU_KYOTEN_CODE");
            final String foundedyear = (String) map.get("FOUNDEDYEAR");//nullが入っている
            final String presentMst = (String) map.get("PRESENT_EST");//nullが入っている
            final String gakushuKyotenShubetsucode = (String) map.get("GAKUSHU_KYOTEN_SHUBETSU_CODE");
            final String gakushuKyotenName = (String) map.get("GAKUSHU_KYOTEN_NAME");
            final String schoolname2 = (String) map.get("SCHOOLNAME2");//nullが入っている
            final String gakushuKyotenRname = (String) map.get("GAKUSHU_KYOTEN_R_NAME");
            final String schoolnameEng = (String) map.get("SCHOOLNAME_ENG");//nullが入っている
            final String yubinNo = (String) map.get("YUBIN_NO");
            final String todofukenNo = (String) map.get("TODOFUKEN_NO");
            final String address1 = (String) map.get("ADDRESS1");
            final String address2 = (String) map.get("ADDRESS2");
            final String schooladdr1Eng = (String) map.get("SCHOOLADDR1_ENG");//nullが入っている
            final String schooladdr2Eng = (String) map.get("SCHOOLADDR2_ENG");//nullが入っている
            final String telNo = (String) map.get("TEL_NO");
            final String schooltelnoSearch = (String) map.get("SCHOOLTELNO_SEARCH");//nullが入っている
            final String faxNo = (String) map.get("FAX_NO");
            final String eMail = (String) map.get("E_MAIL");
            final String foundedDate = (String) map.get("FOUNDED_DATE");//nullが入っている
            final String outJuni = (String) map.get("OUT_JUNI");
            final String kaisetsuNengappi = (String) map.get("KAISETSU_NENGAPPI");
            final String heisaNengappi = (String) map.get("HEISA_NENGAPPI");
            final String registercd = (String) map.get("REGISTERCD");//nullが入っている
            final String torokuDate = (String) map.get("TOROKU_DATE");
            final String koshinDate = (String) map.get("KOSHIN_DATE");

            final GakushuKyoten gaku = new GakushuKyoten(
                    gakushuKyotenCode,
                    foundedyear,
                    presentMst,
                    gakushuKyotenShubetsucode,
                    gakushuKyotenName,
                    schoolname2,
                    gakushuKyotenRname,
                    schoolnameEng,
                    yubinNo,
                    todofukenNo,
                    address1,
                    address2,
                    schooladdr1Eng,
                    schooladdr2Eng,
                    telNo,
                    schooltelnoSearch,
                    faxNo,
                    eMail,
                    foundedDate,
                    outJuni,
                    kaisetsuNengappi,
                    heisaNengappi,
                    registercd,
                    torokuDate,
                    koshinDate
            );
            rtn.add(gaku);
        }
        return rtn;
    }

    private String getSql() {
        final String sql;
        sql = "SELECT"
            + "  GAKUSHU_KYOTEN_CODE,"
            + "  cast(NULL as varchar(4)) as FOUNDEDYEAR,"
            + "  cast(NULL as varchar(3)) as PRESENT_EST,"
            + "  GAKUSHU_KYOTEN_SHUBETSU_CODE,"
            + "  GAKUSHU_KYOTEN_NAME,"
            + "  cast(NULL as varchar(90)) as SCHOOLNAME2,"
            + "  GAKUSHU_KYOTEN_R_NAME,"
            + "  cast(NULL as varchar(60)) as SCHOOLNAME_ENG,"
            + "  YUBIN_NO,"
            + "  TODOFUKEN_NO,"
            + "  ADDRESS1,"
            + "  ADDRESS2,"
            + "  cast(NULL as varchar(50)) as SCHOOLADDR1_ENG,"
            + "  cast(NULL as varchar(50)) as SCHOOLADDR2_ENG,"
            + "  TEL_NO,"
            + "  cast(NULL as varchar(14)) as SCHOOLTELNO_SEARCH,"
            + "  FAX_NO,"
            + "  E_MAIL,"
            + "  cast(NULL as date) as FOUNDED_DATE,"
            + "  OUT_JUNI,"
            + "  KAISETSU_NENGAPPI,"
            + "  value(HEISA_NENGAPPI, cast(null as varchar(20))) as HEISA_NENGAPPI,"
            + "  '00999990' as REGISTERCD,"
            + "  KOSHIN_DATE"
            + " FROM"
            + "  " + ICASS_TABLE ;
        log.debug("sql=" + sql);
        return sql;
    }

    /*
     * [takaesu@withus takaesu]$ db2 describe table BELONGING_MST
        列名                           スキーマ  タイプ名           長さ    位取り NULL
        ------------------------------ --------- ------------------ -------- ----- ------
        BELONGING_DIV                  SYSIBM    VARCHAR                   3     0 いいえ
        FOUNDEDYEAR                    SYSIBM    VARCHAR                   4     0 はい
        PRESENT_EST                    SYSIBM    VARCHAR                   3     0 はい
        CLASSIFICATION                 SYSIBM    VARCHAR                   1     0 はい
        SCHOOLNAME1                    SYSIBM    VARCHAR                  90     0 はい
        SCHOOLNAME2                    SYSIBM    VARCHAR                  90     0 はい
        SCHOOLNAME3                    SYSIBM    VARCHAR                  90     0 はい
        SCHOOLNAME_ENG                 SYSIBM    VARCHAR                  60     0 はい
        SCHOOLZIPCD                    SYSIBM    VARCHAR                   8     0 はい
        SCHOOLPREF_CD                  SYSIBM    VARCHAR                   2     0 はい
        SCHOOLADDR1                    SYSIBM    VARCHAR                  75     0 はい
        SCHOOLADDR2                    SYSIBM    VARCHAR                  75     0 はい
        SCHOOLADDR3                    SYSIBM    VARCHAR                  75     0 はい
        SCHOOLADDR1_ENG                SYSIBM    VARCHAR                  50     0 はい
        SCHOOLADDR2_ENG                SYSIBM    VARCHAR                  50     0 はい
        SCHOOLTELNO                    SYSIBM    VARCHAR                  14     0 はい
        SCHOOLTELNO_SEARCH             SYSIBM    VARCHAR                  14     0 はい
        SCHOOLFAXNO                    SYSIBM    VARCHAR                  14     0 はい
        SCHOOLMAIL                     SYSIBM    VARCHAR                  40     0 はい
        FOUNDED_DATE                   SYSIBM    DATE                      4     0 はい
        ORDER                          SYSIBM    SMALLINT                  2     0 はい
        OPEN_DATE                      SYSIBM    DATE                      4     0 いいえ
        CLOSE_DATE                     SYSIBM    DATE                      4     0 はい
        REGISTERCD                     SYSIBM    VARCHAR                   8     0 はい
        UPDATED                        SYSIBM    TIMESTAMP                10     0 はい
        
          25 レコードが選択されました。
     */
    private void saveKnj(List list) throws SQLException {
        int totalCount = 0;
        final String sql = "INSERT INTO BELONGING_MST VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,current timestamp)";

        for (final Iterator it = list.iterator(); it.hasNext();) {
            final GakushuKyoten gkkt = (GakushuKyoten) it.next();
//            gkkt.execute(gkkt);
            try {
                Object[] toArray = gkkt.toArray();
                final int insertCount = _runner.update(_db2.conn, sql, toArray);
                if (1 != insertCount) {
                    throw new IllegalStateException("INSERT件数が1件以外:" + insertCount);
                }
                totalCount += insertCount;                
            } catch (final SQLException e) {
                log.error("賢者へのINSERTでエラー", e);
                throw e;
            }
        }
        _db2.commit();
        log.warn("挿入件数=" + totalCount);
    }


    private class GakushuKyoten {
        final String _gakushuKyotenCode;
        final String _foundedYear;
        final String _presentMst;
        final String _gakushuKyotenShubetsuCode;
        final String _gakushuKyotenName;
        final String _schoolName2;
        final String _gakushuKyotenRName;
        final String _schoolNameEng;
        final String _yubinNo;
        final String _todofukenNo;
        final String _address1;
        final String _address2;
        final String _schoolAddr1Eng;
        final String _schoolAddr2Eng;
        final String _telNo;
        final String _schoolTelnoSearch;
        final String _faxNo;
        final String _eMail;
        final String _foundedDate;
        final Integer _outJuni;
        final String _kaisetsuNengappi;
        final String _heisaNengappi;
        final String _registercd;
        final String _torokuDate;
        final String _koshinDate;

        public GakushuKyoten(
                final String gakushuKyotenCode,
                final String foundedYear,
                final String presentMst,
                final String gakushuKyotenShubetsuCode,
                final String gakushuKyotenName,
                final String schoolName2,
                final String gakushuKyotenRName,
                final String schoolNameEng,
                final String yubinNo,
                final String todofukenNo,
                final String address1,
                final String address2,
                final String schoolAddr1Eng,
                final String schoolAddr2Eng,
                final String telNo,
                final String schoolTelnoSearch,
                final String faxNo,
                final String eMail,
                final String foundedDate,
                final String outJuni,
                final String kaisetsuNengappi,
                final String heisaNengappi,
                final String registercd,
                final String torokuDate,
                final String koshinDate
        ) {
            _gakushuKyotenCode = gakushuKyotenCode;
            _foundedYear = foundedYear;
            _presentMst = presentMst;
            _gakushuKyotenShubetsuCode = gakushuKyotenShubetsuCode;
            _gakushuKyotenName = gakushuKyotenName;
            _schoolName2 = schoolName2;
            _gakushuKyotenRName = gakushuKyotenRName;
            _schoolNameEng = schoolNameEng;
            _yubinNo = yubinNo;
            _todofukenNo = todofukenNo;
            _address1 = address1;
            _address2 = address2;
            _schoolAddr1Eng = schoolAddr1Eng;
            _schoolAddr2Eng = schoolAddr2Eng;
            _telNo = telNo;
            _schoolTelnoSearch = schoolTelnoSearch;
            _faxNo = faxNo;
            _eMail = eMail;
            _foundedDate = foundedDate;
            _outJuni = (null == outJuni) ? null : new Integer(outJuni);
            _kaisetsuNengappi = kaisetsuNengappi;
            _heisaNengappi = heisaNengappi;
            _registercd = registercd;
            _torokuDate = torokuDate;
            _koshinDate = koshinDate;            
        }

        public void execute(final GakushuKyoten gkkt) {
            final String sql = "INSERT INTO BELONGING_MST "
                + " VALUES("
                + "'" + gkkt._gakushuKyotenCode + "', "
                + "'" + gkkt._foundedYear + "', "
                + "'" + gkkt._presentMst + "', "
                + "'" + gkkt._gakushuKyotenShubetsuCode + "', "
                + "'" + gkkt._gakushuKyotenName + "', "
                + "'" + gkkt._schoolName2 + "', "
                + "'" + gkkt._gakushuKyotenRName + "', "
                + "'" + gkkt._schoolNameEng + "', "
                + "'" + gkkt._yubinNo + "', "
                + "'" + gkkt._todofukenNo + "', "
                + "'" + gkkt._address1 + "', "
                + "'" + gkkt._address2 + "', "
                + "'" + gkkt._schoolAddr1Eng + "', "
                + "'" + gkkt._schoolAddr2Eng + "', "
                + "'" + gkkt._telNo + "', "
                + "'" + gkkt._schoolTelnoSearch + "', "
                + "'" + gkkt._faxNo + "', "
                + "'" + gkkt._eMail + "', "
                + "'" + gkkt._foundedDate + "', "
                + gkkt._outJuni + ", "
                + "'" + gkkt._kaisetsuNengappi + "', "
                + "'" + gkkt._heisaNengappi + "', "
                + "'" + gkkt._registercd + "', "
                + "'" + gkkt._torokuDate + "', "
                + " current timestamp)";

            log.debug(sql);
        }

        public Object[] toArray() {
            final Object[] rtn = {
                    _gakushuKyotenCode,
                    _foundedYear,
                    _presentMst,
                    _gakushuKyotenShubetsuCode,
                    _gakushuKyotenName,
                    _schoolName2,
                    _gakushuKyotenRName,
                    _schoolNameEng,
                    _yubinNo,
                    _todofukenNo,
                    _address1,
                    null,// _address3
                    _address2,
                    _schoolAddr1Eng,
                    _schoolAddr2Eng,
                    _telNo,
                    _schoolTelnoSearch,
                    _faxNo,
                    _eMail,
                    _foundedDate,
                    _outJuni,
                    _kaisetsuNengappi,
                    _heisaNengappi,
                    _registercd,
            };


            return rtn;
        }
    }
} // BelongingMst

// eof
