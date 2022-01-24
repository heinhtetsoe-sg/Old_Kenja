// kanji=����
/*
 * $Id: KnjRecGraduateDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2008/08/15 15:46:35 - JST
 * �쐬��: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.util.List;
import java.util.Map;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * REC_GRADUATE_DAT �����B
 * @author takaesu
 * @version $Id: KnjRecGraduateDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjRecGraduateDat extends AbstractKnj implements IKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjRecGraduateDat.class);

    public KnjRecGraduateDat() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "���ƔF�茋��"; }

    void migrate() throws SQLException {
        final String sql;
        sql = "SELECT"
            + "  t1.schregno,"
            + "  t1.grd_date,"
            + "  t1.grd_div,"
            + "  t2.total"
            + " FROM"
            + "  schreg_base_mst t1 LEFT JOIN ("
            + "      SELECT"
            + "        schregno,"
            + "        sum(get_credit) as total"
            + "      FROM"
            + "        rec_credit_admits"
            + "      GROUP BY"
            + "        schregno"
            + "      ) t2"
            + "  ON t1.schregno=t2.schregno"
            + " WHERE"
            + "  t1.grd_div = '1'"// 1=����
            ;
        log.debug("sql=" + sql);

        final List result = _runner.mapListQuery(sql);
        log.debug("�f�[�^����=" + result.size());

        _runner.listToKnj(result, "REC_GRADUATE_DAT", this);
    }

    /** ���ʊ������Ԑ�. */
    public static Integer SPECIAL_COUNT = new Integer(30);

    /** {@inheritDoc} */
    public Object[] mapToArray(final Map map) {
        final Object[] rtn = {
                map.get("schregno"),
                "1",// 1=���Ƃ�F�߂�. 0=�F�߂Ȃ�
                "1",// 1=�w��ς�
                "1",// 1=�K���C�ς�
                map.get("total"),
                SPECIAL_COUNT,
                Param.REGISTERCD,
        };

        return rtn;
    }
}
// eof

