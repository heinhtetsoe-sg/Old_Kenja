// kanji=漢字
/*
 * $Id: AccumulateSummaryDayBatch.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2006/09/22 14:55:41 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.batch.accumulate.option.AccumulateOptions;

/**
 * 出欠日々累積データ生成。
 * @author maesiro
 * @version $Id: AccumulateSummaryDayBatch.java 74552 2020-05-27 04:41:22Z maeshiro $
 */
public final class AccumulateSummaryDayBatch {
    protected static final String PROPSNAME = "AccumulateSummaryBatch.properties";
    /*pkg*/static final Log log = LogFactory.getLog(AccumulateSummaryDayBatch.class);
    /**
     * コンストラクタ。
     * @param options オプション
     */
    private AccumulateSummaryDayBatch() {
    }

    /**
     * メイン。
     * @param args 引数
     * @throws SQLException SQL例外
     */
    public static void main(final String[] args) throws SQLException {
        final StopWatch sw = new StopWatch();
        sw.start();
        log.fatal("START");

        final Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(PROPSNAME));
        } catch (final FileNotFoundException e) {
            log.error("FileNotFoundException", e);
        } catch (final IOException e) {
            log.error("IOException", e);
        }

        final AccumulateOptions options = new AccumulateOptions(args, properties);
        if (options.doRun()) {
            final AccumulateSummaryBatchAttendDayDat asba = new AccumulateSummaryBatchAttendDayDat(options);
            asba.invoke();
        } else {
            log.fatal("処理しません。" + AccumulateOptions.DO_RUN + " is false. See [" + PROPSNAME + "] file");
        }
        log.fatal("Done!: " + sw);
    }

} // AccumulateSummaryBatch

// eof
