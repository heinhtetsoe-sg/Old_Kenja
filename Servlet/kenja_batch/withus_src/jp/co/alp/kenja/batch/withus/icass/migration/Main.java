// kanji=����
/*
 * $Id: Main.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2008/07/15 14:18:15 - JST
 * �쐬��: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.util.Iterator;

import java.sql.SQLException;

import nao_package.db.DB2UDB;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * ICASS�̃f�[�^�ڍs�B
 * @author takaesu
 * @version $Id: Main.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class Main {
    /*pkg*/static final Log log = LogFactory.getLog(Main.class);

    public static void main(String[] args) throws
            SQLException,
            InstantiationException,
            IllegalAccessException,
            ClassNotFoundException
    {
        final Package pkg = Main.class.getPackage();
        if (null != pkg) {
            final String implementationVersion = pkg.getImplementationVersion();    // �N���X���[�_�ɂ��Anull�̉\������
            if (null != implementationVersion) {
                log.info("implementationVersion=" + implementationVersion);
            }
        }
        final Param param = new Param(args);
        final DB2UDB db = new DB2UDB(param.getDbUrl(), "db2inst1", "db2inst1", DB2UDB.TYPE2);
        db.open();

        param.load(db);
        ivoke(db, param);
        db.close();

        log.fatal("Done.");
    }

    private static void ivoke(DB2UDB db2, final Param param) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException {
        // TAKAESU: Param �̃����o�[���瓾���l���A�X�� Param�ɓn���Ă���̂ł܂Ƃ߂���
        for (final Iterator it = param._classes.iterator(); it.hasNext();) {
            final String className = (String) it.next();
            final AbstractKnj knj = param.createKnj(className);
            knj.init(db2, param);
            knj.migrateData();
            knj._db2.commit();
        }
    }
} // Main

// eof
