// kanji=漢字
/*
 * $Id: DaoKenjaTableStatus.java 74567 2020-05-27 13:21:04Z maeshiro $
 *
 * 作成日: 2004/07/06 19:57:51 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.dao.query;

import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.batch.dao.DaoBatchSchoolMaster;
import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.KenjaTableStatus;

/*
 *  describe table syscat.columns
 *  
 *                                 タイプ・
 *  列名                           スキーマ  タイプ名           長さ    位取り NULL
 *  ------------------------------ --------- ------------------ -------- ----- ------
 *  TABSCHEMA                      SYSIBM    VARCHAR                 128     0 いいえ
 *  TABNAME                        SYSIBM    VARCHAR                 128     0 いいえ
 *  COLNAME                        SYSIBM    VARCHAR                 128     0 いいえ
 *  COLNO                          SYSIBM    SMALLINT                  2     0 いいえ
 *  TYPESCHEMA                     SYSIBM    VARCHAR                 128     0 いいえ
 *  TYPENAME                       SYSIBM    VARCHAR                 128     0 いいえ
 *  LENGTH                         SYSIBM    INTEGER                   4     0 いいえ
 *  SCALE                          SYSIBM    SMALLINT                  2     0 いいえ
 *  DEFAULT                        SYSIBM    VARCHAR                 254     0 はい
 *  NULLS                          SYSIBM    CHARACTER                 1     0 いいえ
 *  CODEPAGE                       SYSIBM    SMALLINT                  2     0 いいえ
 *  LOGGED                         SYSIBM    CHARACTER                 1     0 いいえ
 *  COMPACT                        SYSIBM    CHARACTER                 1     0 いいえ
 *  COLCARD                        SYSIBM    BIGINT                    8     0 いいえ
 *  HIGH2KEY                       SYSIBM    VARCHAR                 254     0 はい
 *  LOW2KEY                        SYSIBM    VARCHAR                 254     0 はい
 *  AVGCOLLEN                      SYSIBM    INTEGER                   4     0 いいえ
 *  KEYSEQ                         SYSIBM    SMALLINT                  2     0 はい
 *  PARTKEYSEQ                     SYSIBM    SMALLINT                  2     0 はい
 *  NQUANTILES                     SYSIBM    SMALLINT                  2     0 いいえ
 *  NMOSTFREQ                      SYSIBM    SMALLINT                  2     0 いいえ
 *  NUMNULLS                       SYSIBM    BIGINT                    8     0 いいえ
 *  TARGET_TYPESCHEMA              SYSIBM    VARCHAR                 128     0 はい
 *  TARGET_TYPENAME                SYSIBM    VARCHAR                 128     0 はい
 *  SCOPE_TABSCHEMA                SYSIBM    VARCHAR                 128     0 はい
 *  SCOPE_TABNAME                  SYSIBM    VARCHAR                 128     0 はい
 *  SOURCE_TABSCHEMA               SYSIBM    VARCHAR                 128     0 はい
 *  SOURCE_TABNAME                 SYSIBM    VARCHAR                 128     0 はい
 *  DL_FEATURES                    SYSIBM    CHARACTER                10     0 はい
 *  SPECIAL_PROPS                  SYSIBM    CHARACTER                 8     0 はい
 *  HIDDEN                         SYSIBM    CHARACTER                 1     0 いいえ
 *  INLINE_LENGTH                  SYSIBM    INTEGER                   4     0 いいえ
 *  IDENTITY                       SYSIBM    CHARACTER                 1     0 いいえ
 *  GENERATED                      SYSIBM    CHARACTER                 1     0 いいえ
 *  TEXT                           SYSIBM    CLOB                2097154     0 はい
 *  REMARKS                        SYSIBM    VARCHAR                 254     0 はい
 *  COMPRESS                       SYSIBM    CHARACTER                 1     0 いいえ
 *  AVGDISTINCTPERPAGE             SYSIBM    DOUBLE                    8     0 はい
 *  PAGEVARIANCERATIO              SYSIBM    DOUBLE                    8     0 はい
 *  SUB_COUNT                      SYSIBM    SMALLINT                  2     0 いいえ
 *  SUB_DELIM_LENGTH               SYSIBM    SMALLINT                  2     0 いいえ
 *  
 *    41 レコードが選択されました。
 */

/**
 * 賢者のテーブル情報を読み込む。
 * @author maesiro
 * @version $Id: DaoKenjaTableStatus.java 74567 2020-05-27 13:21:04Z maeshiro $
 */
public final class DaoKenjaTableStatus extends AbstractDaoLoader<KenjaTableStatus> {
    /** テーブル名 */
    public static final String TABLE_NAME = "SYSCAT.COLUMNS";

    /** log */
    private static final Log log = LogFactory.getLog(DaoKenjaTableStatus.class);
    private static final AbstractDaoLoader<KenjaTableStatus> INSTANCE = new DaoKenjaTableStatus();

    /*
     * コンストラクタ。
     */
    private DaoKenjaTableStatus() {
        super(log);
    }

    /**
     * インスタンスを得る。
     * @return インスタンス
     */
    public static AbstractDaoLoader<KenjaTableStatus> getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    public Object mapToInstance(final Map<String, Object> map) {
        final String tabname = MapUtils.getString(map, "tabname");
        final String colname = MapUtils.getString(map, "colname");
        if (DaoControlMaster.TABLE_NAME.equals(tabname)) {
            if ("ATTEND_TERM".equals(colname)) {
                KenjaTableStatus.getInstance().setValidControlMasterAttendanceTerm(true);
            }
        } else if (DaoBatchSchoolMaster.TABLE_NAME.equals(tabname)) {
            if ("SCHOOL_KIND".equals(colname)) {
                KenjaTableStatus.getInstance().setValidSchoolMasterSchoolKind(true);
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getQuerySql() {
        return "select"
                + "    TABNAME as tabname,"
                + "    COLNAME as colname"
                + "  from " + TABLE_NAME
                + "  where"
                + "    TABSCHEMA = 'DB2INST1'"
                + "    and TABNAME IN ( "
                + "          '" + DaoControlMaster.TABLE_NAME + "', "
                + "          '" + DaoBatchSchoolMaster.TABLE_NAME + "' "
                + "       )"
                + "  order by"
                + "    TABNAME"
                + "    , COLNAME";
    }

    /**
     * {@inheritDoc}
     */
    public Object[] getQueryParams(final ControlMaster cm) {
        return null;
    }
} // DaoKenjaTableStatus

// eof
