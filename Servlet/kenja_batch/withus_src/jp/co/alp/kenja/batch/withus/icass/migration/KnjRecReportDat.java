// kanji=����
/*
 * $Id: KnjRecReportDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2008/07/15 14:15:57 - JST
 * �쐬��: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.batch.withus.icass.migration.table_icass.SeitoRishuKadaiJisseki;

/**
 * REC_REPORT_DAT�����B
 * @author takaesu
 * @version $Id: KnjRecReportDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjRecReportDat extends AbstractKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjRecReportDat.class);

    public static final String KNJ_TABLE = "REC_REPORT_DAT";
    public static final String ICASS_TABLE = "SEITO_RISHU_KADAI_JISSEKI";

    public KnjRecReportDat() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "���|�[�g����"; }

    public void migrate() throws SQLException {
        for (int year=2005; year<=2008; year++) {
            int recordsPer = 200;
            for (int shigansha_renban=0; shigansha_renban<15000; shigansha_renban+= recordsPer) {
                int begin = shigansha_renban + 1;
                int end = shigansha_renban + recordsPer;
                final List list = loadIcass(begin,end,year);
                log.debug(year+"�N�x �u��ҘA�� = "+begin+" �` "+end);
                log.debug(ICASS_TABLE + "�f�[�^����=" + list.size());

                saveKnj(list);
            }
        }
    }

    private List loadIcass(int shBegin,int shEnd,int year) throws SQLException {
        final List rtn = new ArrayList();

        // SQL��
        final String sql;
        sql = "SELECT T1.* FROM " + ICASS_TABLE + " T1"
            + " WHERE "//TODO: �f�[�^�ʂ������̂ň��Ă��܂��B�΍���l����!�ʊw�������ŗǂ��͂�
            + "  T1.rishu_kadai_shubetsu_code = '1'" // 1=���|�[�g, 2=�e�X�g
            + " and int(T1.shigansha_renban) between "+shBegin+" and "+shEnd+" and T1.nendo_code = '"+year+"'"
            + " and exists(SELECT 'x' FROM SEITO T2 WHERE VALUE(T2.SEITO_NO, '') <> '' AND T1.SHIGANSHA_RENBAN = T2.SHIGANSHA_RENBAN)"
            ;
        log.debug("sql=" + sql);

        // SQL���s
        final List result;
        try {
            result = (List) _runner.query(_db2.conn, sql, _handler);//TODO: �f�[�^�ʂ������̂ň��Ă��܂��B�΍���l����!
        } catch (final SQLException e) {
            log.error("ICASS�f�[�^�捞�݂ŃG���[", e);
            throw e;
        }

        // ���ʂ̏���
        for (final Iterator it = result.iterator(); it.hasNext();) {
            final Map map = (Map) it.next();

            final SeitoRishuKadaiJisseki srkj = new SeitoRishuKadaiJisseki(_param, map);
            rtn.add(srkj);
        }
        return rtn;
    }

    private void saveKnj(final List list) throws SQLException {
        int totalCount = 0;
        final String sql = "INSERT INTO rec_report_dat VALUES(?,?,?,?,?,?,?,?,?,?,?,?,current timestamp)";

        for (final Iterator it = list.iterator(); it.hasNext();) {
            final SeitoRishuKadaiJisseki srkj = (SeitoRishuKadaiJisseki) it.next();
            try {
                final int insertCount = _runner.update(_db2.conn, sql, srkj.toRecReportDat());
                if (1 != insertCount) {
                    throw new IllegalStateException("INSERT������1���ȊO!:" + insertCount);
                }
                totalCount += insertCount;
            } catch (final SQLException e) {
                log.error("���҂ւ�INSERT�ŃG���[", e);
                throw e;
            }
        }
        _db2.commit();
        log.warn("�}������=" + totalCount);
    }
} // KnjRecReportDat

// eof
