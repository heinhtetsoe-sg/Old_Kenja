// kanji=漢字
/*
 * $Id: Main.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/04/04
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.nbi.groupware;

import java.io.IOException;
import java.text.ParseException;

import java.sql.SQLException;

import nao_package.db.DB2UDB;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * NBIのグループウェアとの連携用CSV入出力。
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
            IOException,
            ParseException
    {
        final Param param = new Param(args);

        final DB2UDB db = new DB2UDB(param.getDbUrl(), "db2inst1", "db2inst1", DB2UDB.TYPE2);
        db.open();

        param.load(db);

        if (param.isOutputMode()) {
            final OutputCsv outputCsv = new OutputCsv(db, param);
            log.fatal("ファイル生成開始。出力先フォルダ=" + param.getFolder());
            outputCsv.doIt();
        } else {
            final InputCsv inputCsv = new InputCsv(db, param);
            log.fatal("ファイル取り込み開始。取り込みファイル=" + param.getFolder());
            inputCsv.doIt();
        }

        db.close();

        log.fatal("Done.");
    }
} // Main

// eof
