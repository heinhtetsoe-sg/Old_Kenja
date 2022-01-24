// kanji=漢字
/*
 * $Id: KnjHtrainremarkDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/08/15 15:46:35 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * HTRAINREMARK_DAT を作る。
 * @author takaesu
 * @version $Id: KnjHtrainremarkDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjHtrainremarkDat extends AbstractKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjHtrainremarkDat.class);

    public KnjHtrainremarkDat() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "指導要録所見データ"; }

    void migrate() throws SQLException {
        final String sql;
        sql = "SELECT"
            + "  T1.* "
            + " FROM "
            + "  SEITO_NENDO_JOHO T1, "
            + "  SEITO T2 "
            + " WHERE "
            + "  T1.SHIGANSHA_RENBAN = T2.SHIGANSHA_RENBAN "
            ;
        log.debug("sql=" + sql);

        final List result = _runner.mapListQuery(sql);
        log.debug("データ件数=" + result.size());

        final Map wrk = new HashMap();
        for (final Iterator it = result.iterator(); it.hasNext();) {
            final Map map = (Map) it.next();

            final String year = (String) map.get("NENDO_CODE");
            final String schregno = _param.getSchregno((String) map.get("SHIGANSHA_RENBAN"));
            final String key = year + schregno;

            if (!wrk.containsKey(key)) {
                wrk.put(key, new HtrainRemakDat(year, schregno));
            }

            final HtrainRemakDat data = (HtrainRemakDat) wrk.get(key);
            data.add(map.get("SHIDO_SHOJIKO"));
        }

        saveKnj(wrk.values());
    }

    private void saveKnj(final Collection list) throws SQLException {
        int totalCount = 0;
        final String sql = "INSERT INTO htrainremark_dat VALUES(?,?,?,?,?,?,?,?,?,?,?,current timestamp)";

        for (final Iterator it = list.iterator(); it.hasNext();) {
            final HtrainRemakDat object = (HtrainRemakDat) it.next();
            try {
                final int insertCount = _runner.update(_db2.conn, sql, object.toArray());
                if (1 != insertCount) {
                    throw new IllegalStateException("INSERT件数が1件以外!:" + insertCount);
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

    private class HtrainRemakDat {
        String _year;
        String _schregno;
        String _annual;
//        String _totalStudyAct;
//        String _totalStudyVal;
        final Set _specialActRemark = new HashSet();
//        String _totalRemark;
//        String _attendRecRemark;
//        String _viewRemark;
//        String _behaverecRemark;

        public HtrainRemakDat(final String year, final String schregno) {
            _year = year;
            _schregno = schregno;
            _annual = "00";
        }

        public Object[] toArray() {
            final String tkn = (String) (_specialActRemark.isEmpty() ? null : specialToString());

            final Object[] rtn = {
                    _year,
                    _schregno,
                    _annual,   // 3. ANNUAL
                    null,
                    null,
                    "特記事項なし", // 6. SPECIALACTREMARK. 特別活動所見
                    tkn.equals("null") ? null : tkn,            // 7. TOTALREMARK. 総合所見
                    null,
                    null,
                    null,
                    Param.REGISTERCD,
            };
            return rtn;
        }

        public void add(final Object specialActRemark) {
            _specialActRemark.add(specialActRemark);
        }

        public String getKey() {
            return _year + _schregno;
        }

        public String specialToString() {
            StringBuffer buf = new StringBuffer();
            Iterator e = _specialActRemark.iterator();
            int maxIndex = _specialActRemark.size() - 1;
            for (int i = 0; i <= maxIndex; i++) {
                buf.append(String.valueOf(e.next()));
                if (i < maxIndex)
                    buf.append(", ");
            }
            return buf.toString();
        }
    }
}
// eof
