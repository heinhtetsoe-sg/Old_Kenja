// kanji=漢字
/*
 * $Id: KnjClassYdat.java 56574 2017-10-22 11:21:06Z maeshiro $
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
 * TODO: <賢者のテーブル名に書き換えてください。例) REC_REPORT_DAT>を作る。
 * @author takaesu
 * @version $Id: KnjClassYdat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjClassYdat extends AbstractKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjClassYdat.class);

    public KnjClassYdat() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "???"; }

    void migrate() throws SQLException {
        log.fatal("仕様が不明! データ移行はしない。");
    }
}
// eof

