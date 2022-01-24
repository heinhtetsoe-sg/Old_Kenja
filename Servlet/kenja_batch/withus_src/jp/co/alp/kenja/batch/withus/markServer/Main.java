// kanji=漢字
/*
 * $Id: Main.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/03/21 10:31:50 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.markServer;

import java.io.IOException;

import java.sql.SQLException;

import nao_package.db.DB2UDB;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * マークサーバとの連携用CSV入出力。
 * @author takaesu
 * @version $Id: Main.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class Main {
    /*pkg*/static final Log log = LogFactory.getLog(Main.class);

    public static void main(final String[] args) throws
            SQLException,
            InstantiationException,
            IllegalAccessException,
            ClassNotFoundException,
            IOException
    {
        try {
            final Package pkg = Main.class.getPackage();
            if (null != pkg) {
                final String implementationVersion = pkg.getImplementationVersion();    // クラスローダにより、nullの可能性あり
                if (null != implementationVersion) {
                    log.info("implementationVersion=" + implementationVersion);
                }
            }
    
            final Param param = new Param(args);
    
            final DB2UDB db = new DB2UDB(param.getDbUrl(), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            try {
                db.open();
        
                param.load(db);
        
                if (param.isOutputMode()) {
                    final OutputCsv outputCsv = new OutputCsv(db, param);
                    log.fatal("ファイル生成開始。出力先フォルダ=" + param.getFile());
                    outputCsv.doIt();
                } else {
                    final InputCsv inputCsv = new InputCsv(db, param);
                    log.fatal("ファイル取り込み開始。取り込みファイル=" + param.getFile());
                    try {
                        inputCsv.doIt();
                    } catch (final IOException e) {
                        throw e;
                    }
                }
            } catch (final SQLException e) {
                log.fatal("何らかのSQL例外が発生した!", e);
                throw e;
            } catch (final InstantiationException e) {
                log.fatal("InstantiationException", e);
                throw e;
            } catch (final IllegalAccessException e) {
                log.fatal("IllegalAccessException", e);
                throw e;
            } catch (final ClassNotFoundException e) {
                log.fatal("ClassNotFoundException", e);
                throw e;
            }
            db.close();
    
            log.fatal("Done.");
        } catch (final Throwable e) {
            log.fatal("重大エラー発生!", e);
        }
    }
} // Main

// eof
