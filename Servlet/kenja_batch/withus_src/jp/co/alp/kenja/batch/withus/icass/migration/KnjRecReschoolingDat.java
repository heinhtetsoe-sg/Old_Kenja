// kanji=����
/*
 * $Id: KnjRecReschoolingDat.java 56574 2017-10-22 11:21:06Z maeshiro $
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
 * REC_RESCHOOLING_DAT(�ʐM�X�N�[�����O�s��)�����B
 * @author takaesu
 * @version $Id: KnjRecReschoolingDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjRecReschoolingDat extends AbstractKnj implements IKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjRecReschoolingDat.class);

    public KnjRecReschoolingDat() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "�ʐM�X�N�[�����O�s��"; }

    void migrate() throws SQLException {
        final String sql;
        sql = "SELECT DISTINCT"
            + "  nendo_code,"
            + "  shigansha_renban"
            + " FROM"
            + "  seito_shukko_schooling_sei"
            ;
        log.debug("sql=" + sql);

        final List result = _runner.mapListQuery(sql);
        log.debug("�f�[�^����=" + result.size());

        _runner.listToKnj(result, "rec_reschooling_dat", this);
    }

    /** {@inheritDoc} */
    public Object[] mapToArray(Map map) {
        final String shiganshaRenban = (String) map.get("SHIGANSHA_RENBAN");
        final String schregno = _param.getSchregno(shiganshaRenban);

        final Object[] rtn = {
                map.get("NENDO_CODE"),
                schregno,
                "S2",// �W���X�N�[�����O�s��(=��K)�̃R�[�h�� S2 ���߂���
                Param.REGISTERCD,
        };
        return rtn;
    }

    /**
     * @deprecated �d�l�ύX�Ńe�[�u���� SEITO_SHUKKO_JISSEKI ���� seito_shukko_schooling_sei �ɕς��������
     */
    class SeitoShukkoJisseki {
        final String _shiganshaRenban;
        final String _nendo;
        final String _shukkoNengappi;

        public SeitoShukkoJisseki(final Map map) {
            _shiganshaRenban = (String) map.get("SHIGANSHA_RENBAN");
            _nendo = (String) map.get("NENDO_CODE");
            _shukkoNengappi = (String) map.get("SHUKKO_NENGAPPI");
        }

        public Object[] toArray() {
            final String schregno = _param.getSchregno(_shiganshaRenban);

            final Object[] rtn = {
                    _nendo,
                    schregno,
                    "S2",//TODO: ��������?
                    Param.REGISTERCD,
            };
            return rtn;
        }
    }
}
// eof
