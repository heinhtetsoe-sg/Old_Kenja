// kanji=漢字
/*
 * $Id: KnjRecReportInfo.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/08/15 15:46:35 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * REC_REPORT_INFO を作る。
 * @author takaesu
 * @version $Id: KnjRecReportInfo.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjRecReportInfo extends AbstractKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjRecReportInfo.class);

    public KnjRecReportInfo() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "レポート情報"; }

    void migrate() throws SQLException {
        log.fatal("ICASS側のデータが不明! データ移行はしない。すなわち、全て「マーク式」とみなす");
    }
}

// eof
