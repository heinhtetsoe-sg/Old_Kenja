// kanji=����
/*
 * $Id: KnjCityMst.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2008/08/15 15:46:35 - JST
 * �쐬��: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TODO: <���҂̃e�[�u�����ɏ��������Ă��������B��) REC_REPORT_DAT>�����B
 * @author takaesu
 * @version $Id: KnjCityMst.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjCityMst extends AbstractKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjCityMst.class);

    public KnjCityMst() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "???"; }

    void migrate() throws SQLException {
        log.fatal("�d�l���s��! �f�[�^�ڍs�͂��Ȃ��B");
    }
}
// eof

