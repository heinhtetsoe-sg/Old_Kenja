// kanji=漢字
/*
 * $Id: Main.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2017/05/30 14:23:11 - JST
 * 作成者: m-yamashiro
 *
 * Copyright(C) 2017-2021 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.knjxTool;

import java.sql.SQLException;

import nao_package.db.DB2UDB;
import nao_package.db.Database;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * POSTGREサーバとの連携。
 *
 * @author m-yamashiro
 * @version $Id: Main.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class Main {
    /* pkg */static final Log log = LogFactory.getLog(Main.class);

    public static void main(final String[] args) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        try {
            final Package pkg = Main.class.getPackage();
            if (null != pkg) {
                final String implementationVersion = pkg.getImplementationVersion(); // クラスローダにより、nullの可能性あり
                if (null != implementationVersion) {
                    log.info("implementationVersion=" + implementationVersion);
                } else {
                    log.info("MANIFESTファイルからのバージョン取得には失敗しました。");
                }
            }

            final Param param = new Param(args);

            final Database knj = new DB2UDB(param._knjUrl, param._knjUser, param._knjPass, DB2UDB.TYPE2);

            try {
                knj.open();
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
                param.load(knj);
                log.fatal("パラメータ: " + param);

                if (param.existA023()) {
                    doIt(param, knj);
                }
            } catch (final SQLException e) {
                log.fatal("何らかのSQL例外が発生した!", e);
                throw e;
            }

            knj.close();
            log.fatal("Done. 連携/賢者の双方の DBクローズ完了");
        } catch (final Throwable e) {
            log.fatal("重大エラー発生!", e);
        }
    }

    private static void doIt(final Param param, final Database knj) throws SQLException {
        try {
            new RegdUpdate(param, knj, "学籍　TOOL_SCHREGD_DAT");
            new Score1update(param, knj, "試験（定期考査）TOOL_SCORE_DAT");
            new Score2update(param, knj, "試験（実力試験）TOOL_SCORE_DAT");
            new Score4update(param, knj, "試験（外部模試）TOOL_SCORE_DAT");
            new Score5update(param, knj, "試験（センター）TOOL_SCORE_DAT");
            new AttendUpdate(param, knj, "出欠　TOOL_ATTEND_DAT");
            new RecordUpdate(param, knj, "評定　TOOL_RECORD_DAT");
            new GradUpdate(param, knj, "大学合格　TOOL_AFT_GRAD_DAT");
            new UserUpdate(param, knj, "ユーザー　TOOL_USER_MST");
            new UpdateUpdate(param, knj, "TOOL_UPDATE_DAT");
        } catch (final SQLException e) {
            log.fatal("SQLException発生!");
            throw e;
        }
    }
} // Main

// eof
