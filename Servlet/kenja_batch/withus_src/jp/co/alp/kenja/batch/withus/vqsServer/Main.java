// kanji=漢字
/*
 * $Id: Main.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/04/30 10:55:29 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.vqsServer;

import java.sql.SQLException;

import nao_package.db.DB2UDB;
import nao_package.db.Database;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.batch.withus.Curriculum;
import jp.co.alp.kenja.batch.withus.vqsServer.db.PostgreSQL;

/**
 * VQSサーバとの連携。
 * @author takaesu
 * @version $Id: Main.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class Main {
    /*pkg*/static final Log log = LogFactory.getLog(Main.class);

    public static void main(final String[] args) throws
        SQLException,
        InstantiationException,
        IllegalAccessException,
        ClassNotFoundException
    {
        try {
            final Package pkg = Main.class.getPackage();
            if (null != pkg) {
                final String implementationVersion = pkg.getImplementationVersion();    // クラスローダにより、nullの可能性あり
                if (null != implementationVersion) {
                    log.info("implementationVersion=" + implementationVersion);
                } else {
                    log.info("MANIFESTファイルからのバージョン取得には失敗しました。");
                }
            }

            final Param param = new Param(args);

            final Database knj = new DB2UDB(param._knjUrl, "db2inst1", "db2inst1", DB2UDB.TYPE2);
            final Database vqs = new PostgreSQL(param._vqsUrl, param._vqsUser, param._vqsPass);

            try {
                knj.open();
                vqs.open();
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

            try {
                vqs.stmt = vqs.conn.createStatement();  // TAKAESU: もっと整理できる
        
                log.fatal("Start. VQS/賢者の双方の DBオープン完了");
        
                param.load(knj);
                log.fatal("パラメータ: " + param);
        
                Curriculum.loadCurriculumMst(knj);
                log.warn("名称マスタの教育課程情報を読込んだ。");
        
                doIt(param, knj, vqs);
            } catch (final SQLException e) {
                log.fatal("何らかのSQL例外が発生した!", e);
                throw e;
            }

            vqs.close();
            knj.close();
            log.fatal("Done. VQS/賢者の双方の DBクローズ完了");
        } catch (final Throwable e) {
            log.fatal("重大エラー発生!", e);
        }
    }

    private static void doIt(final Param param, final Database knj, final Database vqs) throws SQLException {
        try {
            // 賢者⇒VQS
            new MkSchRegistDat(param, knj, vqs, "生徒履修情報テーブル");
            new MkSchRegBaseMst(param, knj, vqs, "生徒情報テーブル");
            new MkStaffMst(param, knj, vqs, "講師情報テーブル");
            new MkSchResultDat(param, knj, vqs, "生徒成績情報テーブル");

            // VQS⇒賢者
            new MkSchAttendDat(param, knj, vqs, "生徒出席情報テーブル");
        } catch (final SQLException e) {
            log.fatal("SQLException発生!");
            throw e;
        }
    }
} // Main

// eof
