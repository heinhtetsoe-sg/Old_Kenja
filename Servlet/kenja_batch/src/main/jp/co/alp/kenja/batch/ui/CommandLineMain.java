// kanji=漢字
/*
 * $Id: CommandLineMain.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2006/09/22 14:30:14 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.ui;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;


import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * コマンドライン版のメイン。
 * @author takaesu
 * @version $Id: CommandLineMain.java 74552 2020-05-27 04:41:22Z maeshiro $
 * @deprecated 使いません。
 */
public final class CommandLineMain {
    private static final Log log = LogFactory.getLog(CommandLineMain.class);

    private static final String PROPSNAME = "AccumulateSummaryBatch.properties";

    private CommandLineMain() {
    }

    /**
     * メイン。
     * @param args 引数
     * @throws SQLException SQL例外
     * @deprecated 使いません。
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


        log.fatal("Done!: " + sw);
    }

} // CommandLineMain

// eof
