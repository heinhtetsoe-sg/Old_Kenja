// kanji=����
/*
 * $Id: KnjRecReportInfo.java 56574 2017-10-22 11:21:06Z maeshiro $
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
 * REC_REPORT_INFO �����B
 * @author takaesu
 * @version $Id: KnjRecReportInfo.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjRecReportInfo extends AbstractKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjRecReportInfo.class);

    public KnjRecReportInfo() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "���|�[�g���"; }

    void migrate() throws SQLException {
        log.fatal("ICASS���̃f�[�^���s��! �f�[�^�ڍs�͂��Ȃ��B���Ȃ킿�A�S�āu�}�[�N���v�Ƃ݂Ȃ�");
    }
}

// eof
