// kanji=漢字
/*
 * $Id: KnjVirtualAccountSchDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/08/15 15:46:35 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * VIRTUAL_ACCOUNT_SCH_DAT を作る。
 * @author takaesu
 * @version $Id: KnjVirtualAccountSchDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjVirtualAccountSchDat extends AbstractKnj implements IKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjVirtualAccountSchDat.class);

    public KnjVirtualAccountSchDat() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "生徒仮想口座データ"; }

    void migrate() throws SQLException {
        final String sql;
        sql = "WITH hoge AS ("
            + "SELECT"
            + "   seito.*,"
            + "   schreg_base_mst.grd_div,"
            + "   schreg_base_mst.grd_date"
            + " FROM"
            + "   seito INNER JOIN schreg_base_mst ON seito.seito_no=schreg_base_mst.schregno"
            + ")"
            + "SELECT"
            + "   t1.shigansha_no,"
            + "   t1.shutsugan_nengappi,"
            + "   t1.grd_div,"
            + "   t1.grd_date,"
            + "   t2.*,"
            + "   t3.virtual_bank_cd"
            + " FROM hoge t1 INNER JOIN furikomi_koza t2"
            + "  ON t1.shigansha_renban=t2.shigansha_renban"
            + " LEFT JOIN virtual_bank_mst t3"
            + "  ON t2.bank_code=t3.bank_cd AND t2.branch_code=t3.branch_cd"
            ;
        log.debug("sql=" + sql);

        final List result = _runner.mapListQuery(sql);
        log.debug("データ件数=" + result.size());

        _runner.listToKnj(result, "virtual_account_sch_dat", this);
    }

    /** {@inheritDoc} */
    public Object[] mapToArray(final Map map) {
        final java.sql.Date grdDiv = (java.sql.Date) map.get("GRD_DATE");

        final java.sql.Date adjustEDate;
        if (null == grdDiv) {
            Calendar cal = Calendar.getInstance();
            cal.set(9999,Calendar.DECEMBER,30);
            Date date = cal.getTime();
            adjustEDate = new java.sql.Date(date.getTime());
        } else {
            adjustEDate = (java.sql.Date) map.get("GRD_DATE");
        }

        final Object[] rtn = {
                map.get("SHIGANSHA_NO"),
                map.get("SHUTSUGAN_NENGAPPI"),
                adjustEDate,
                map.get("VIRTUAL_BANK_CD"),
                map.get("KOZA_NO"),
                Param.REGISTERCD,
        };
        return rtn;
    }
}
// eof

