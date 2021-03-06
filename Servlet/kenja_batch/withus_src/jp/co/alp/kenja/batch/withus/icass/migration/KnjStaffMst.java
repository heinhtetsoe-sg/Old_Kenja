// kanji=¿
/*
 * $Id: KnjStaffMst.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * ì¬ú: 2008/08/15 15:46:35 - JST
 * ì¬Ò: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * STAFF_MST ðìéB
 * @author takaesu
 * @version $Id: KnjStaffMst.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjStaffMst extends AbstractKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjStaffMst.class);

    public static final DecimalFormat _staffCdFormat = new DecimalFormat("00000000");
    public static final DecimalFormat _jobCdFormat = new DecimalFormat("0000");

    public KnjStaffMst() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "Eõ}X^"; }

    void migrate() throws SQLException {
        /* GAKKO_KANKEISHA.GAKKO_KANKEISHA_SHUBETSU_CODE
            0FVXeÇÒ
            1FwZ³Eõ
            2F@lEõ
            3FT|[gZEõ
            4FAoCg
            5F¦ÍZEõ
            6FÊÚw±À{{ÝEõ
            7FÛìÒ
            8FÆÒ
            9F¯¡
            99F»Ì¼
         */
        final List list = loadIcass();
        log.debug("gakko_kankeisha f[^=" + list.size());

        saveKnj(list);
    }

    private List loadIcass() throws SQLException {
        // SQL¶
        final String sql;
        sql = "WITH KYOTEN AS ("
            + "  SELECT"
            + "     T1.* "
            + " FROM "
            + "     KINMUSAKI_GAKUSHU_KYOTEN T1, "
            + "     (SELECT "
            + "          GAKKO_KANKEISHA_NO, "
            + "          MAX(SHUNIN_NENGAPPI) AS NENGAPPI "
            + "      FROM "
            + "          KINMUSAKI_GAKUSHU_KYOTEN "
            + "      GROUP BY "
            + "          GAKKO_KANKEISHA_NO "
            + "     ) T2 "
            + " WHERE "
            + "     T1.GAKKO_KANKEISHA_NO = T2.GAKKO_KANKEISHA_NO "
            + "     AND T1.SHUNIN_NENGAPPI = T2.NENGAPPI "
            + " ), SHOKU AS ( "
            + " SELECT "
            + "     T1.* "
            + " FROM "
            + "     SHOKUMEI T1, "
            + "     (SELECT "
            + "          GAKKO_KANKEISHA_NO, "
            + "          MAX(SHUNIN_NENGAPPI) AS NENGAPPI "
            + "      FROM "
            + "          SHOKUMEI "
            + "      GROUP BY "
            + "          GAKKO_KANKEISHA_NO "
            + "     ) T2 "
            + " WHERE "
            + "     T1.GAKKO_KANKEISHA_NO = T2.GAKKO_KANKEISHA_NO "
            + "     AND T1.SHUNIN_NENGAPPI = T2.NENGAPPI "
            + " ) "
            + " SELECT "
            + "     T1.*, "
            + "     T2.GAKUSHU_KYOTEN_CODE, "
            + "     T3.SHOKUMEI_CODE "
            + " FROM "
            + "     GAKKO_KANKEISHA T1 "
            + "     LEFT JOIN KYOTEN T2 ON T1.GAKKO_KANKEISHA_NO=T2.GAKKO_KANKEISHA_NO "
            + "     LEFT JOIN SHOKU T3 ON T1.GAKKO_KANKEISHA_NO=T3.GAKKO_KANKEISHA_NO "
            ;
        log.debug("sql=" + sql);

        // SQLÀs
        final List result;
        try {
            result = (List) _runner.query(_db2.conn, sql, _handler);
        } catch (final SQLException e) {
            log.error("ICASSf[^æÝÅG[", e);
            throw e;
        }

        return (null != result) ? result : Collections.EMPTY_LIST;
    }

    private void saveKnj(final List list) throws SQLException {
        int totalCount = 0;
        final String sql = "INSERT INTO STAFF_MST VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,current timestamp)";

        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Map map = (Map) it.next();
            try {
                final int insertCount = _runner.update(_db2.conn, sql, mapToStaffMstArray(map));
                if (1 != insertCount) {
                    throw new IllegalStateException("INSERTª1ÈO!:" + insertCount);
                }
                totalCount += insertCount;
            } catch (final SQLException e) {
                log.error("«ÒÖÌINSERTÅG[", e);
                throw e;
            }
        }
        _db2.commit();
        log.warn("}ü=" + totalCount);
    }

    private Object[] mapToStaffMstArray(final Map map) {
        /*
            [db2inst1@withus db2inst1]$ db2 describe table STAFF_MST

            ñ¼                           XL[}  ^Cv¼           ·³    Êæè NULL
            ------------------------------ --------- ------------------ -------- ----- ------
            STAFFCD                        SYSIBM    VARCHAR                   8     0 ¢¢¦
            STAFFNAME                      SYSIBM    VARCHAR                  60     0 Í¢
            STAFFNAME_SHOW                 SYSIBM    VARCHAR                  15     0 Í¢
            STAFFNAME_KANA                 SYSIBM    VARCHAR                 120     0 Í¢
            STAFFNAME_ENG                  SYSIBM    VARCHAR                  60     0 Í¢
            BELONGING_DIV                  SYSIBM    VARCHAR                   3     0 Í¢
            JOBCD                          SYSIBM    VARCHAR                   4     0 Í¢
            SECTIONCD                      SYSIBM    VARCHAR                   4     0 Í¢
            DUTYSHARECD                    SYSIBM    VARCHAR                   4     0 Í¢
            CHARGECLASSCD                  SYSIBM    VARCHAR                   1     0 Í¢
            STAFFSEX                       SYSIBM    VARCHAR                   1     0 Í¢
            STAFFBIRTHDAY                  SYSIBM    DATE                      4     0 Í¢
            STAFFZIPCD                     SYSIBM    VARCHAR                   8     0 Í¢
            STAFFPREF_CD                   SYSIBM    VARCHAR                   2     0 Í¢
            STAFFADDR1                     SYSIBM    VARCHAR                  75     0 Í¢
            STAFFADDR2                     SYSIBM    VARCHAR                  75     0 Í¢
            STAFFADDR3                     SYSIBM    VARCHAR                  75     0 Í¢
            STAFFTELNO                     SYSIBM    VARCHAR                  14     0 Í¢
            STAFFTELNO_SEARCH              SYSIBM    VARCHAR                  14     0 Í¢
            STAFFFAXNO                     SYSIBM    VARCHAR                  14     0 Í¢
            STAFFE_MAIL                    SYSIBM    VARCHAR                  25     0 Í¢
            REGISTERCD                     SYSIBM    VARCHAR                   8     0 Í¢
            UPDATED                        SYSIBM    TIMESTAMP                10     0 Í¢
            
              23 R[hªIð³êÜµ½B
         */
        // TODO: À¹æ!
        // TAKAESU: ÈºÌ¶ÆloadIcass\bhàÌSQL¶ªÜÆÜÁÄ¢éÆ©â·¢Í¸BHoge.java É static \bhÆµÄWñ!?
        String shimei = (String) map.get("SHIMEI");
        String sex = (String) map.get("SEIBETSU");
        String staffCd = (String) map.get("GAKKO_KANKEISHA_NO");
        final String shokumei = (String) map.get("shokumei_code");
        final Object[] rtn = {
                _staffCdFormat.format(Integer.parseInt(staffCd)),//TODO: [ß¹æ
                shimei,
                shimei.length() > 5 ? shimei.substring(0, 5) : shimei,
                map.get("KANA_SHIMEI"),
                map.get(""),// 5.Eõ¼p
                map.get("gakushu_kyoten_code"),
                null == shokumei ? null : _jobCdFormat.format(Integer.parseInt(shokumei)),//TODO: [ß¹æ
                map.get(""),// 8.®R[h
                map.get(""),// 9.Z±ª¶R[h
                map.get(""),// 10.öÆóæª
                null != sex && sex.equals("j") ? "1" : "2", // 11.Eõ«Ê
                map.get("SEINENGAPPI"),
                map.get("YUBIN_NO"),
                map.get("TODOFUKEN_NO"),
                map.get("ADDRESS1"),// 15.EõZ1
                map.get(""),// 16.EõFAXÔ
                map.get("ADDRESS2"),
                map.get("KEITAI_TEL_NO"),
                map.get(""),// 19.EõdbÔ(õp)
                map.get(""),// 20.EõFAXÔ
                map.get("PC_E_MAIL"),
                Param.REGISTERCD,
        };
        return rtn;
    }
}
// eof

