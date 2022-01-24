// kanji=����
/*
 * $Id: Main.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2008/04/04
 * �쐬��: takaesu
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
 * NBI�̃O���[�v�E�F�A�Ƃ̘A�g�pCSV���o�́B
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
            log.fatal("�t�@�C�������J�n�B�o�͐�t�H���_=" + param.getFolder());
            outputCsv.doIt();
        } else {
            final InputCsv inputCsv = new InputCsv(db, param);
            log.fatal("�t�@�C����荞�݊J�n�B��荞�݃t�@�C��=" + param.getFolder());
            inputCsv.doIt();
        }

        db.close();

        log.fatal("Done.");
    }
} // Main

// eof
