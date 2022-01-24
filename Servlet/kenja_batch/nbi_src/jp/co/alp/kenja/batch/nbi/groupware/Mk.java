// kanji=漢字
/*
 * $Id: Mk.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/04/04
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.nbi.groupware;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.List;

import nao_package.db.DB2UDB;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.batch.opencsv.CSVWriter;

/**
 * NBIのグループウェア関連のCSV生成用。
 * @author takaesu
 * @version $Id: Mk.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public abstract class Mk {
    /*pkg*/static final Log log = LogFactory.getLog(Mk.class);

    protected final Param _param;
    protected final DB2UDB _db;

    public Mk(final DB2UDB db, final Param param, final String title) {
        _param = param;
        _db = db;

        log.info("★" + title);
    }

    protected void toCsv(final String subject, final String csvFile, final List data) {
        try {
            final String file = _param.getFullPath(csvFile);
            final OutputStream outputStream = new FileOutputStream(file);
            final Writer fileWriter = new PrintWriter(new OutputStreamWriter(outputStream, Param.encode));
            final CSVWriter writer = new CSVWriter(fileWriter, ',', '\0');
            writer.writeAll(data);
            writer.close();
            log.fatal("CSVファイル生成:" + file);
        } catch (final UnsupportedEncodingException e) {    // TODO: Exception の使い分け必要?
            log.fatal(subject + "の情報取得でエラー" + e);
        } catch (final FileNotFoundException e) {
            log.fatal(subject + "の情報取得でエラー" + e);
        } catch (final IOException e) {
            log.fatal(subject + "の情報取得でエラー" + e);
        }
    }

    /**
     * 文字列の頭1桁を除去する。<br>
     * Ex) "12345678" ⇒ "2345678"
     * @param staffCd 職員コード
     * @return 頭1桁を除去した職員コード
     */
    public static String convStaffCd(final String staffCd) {
        if (null == staffCd) {
            return null;
        }
        return staffCd.substring(1);
    }

    // 以下は不要のハズ
//    /**
//     * yyyy-mm-dd な文字列の区切り文字を除去する。
//     * @param dateStr 日付な文字列
//     * @return 5バイト目、8バイト目を除去した文字列。変換できないときはそのまま。
//     */
//    public static String cutDateDelimit(final String dateStr) {
//        if (null == dateStr) {
//            return null;
//        }
//        if (dateStr.length() != 10) {
//            return dateStr;
//        }
//        return dateStr.substring(0, 4) + dateStr.substring(5, 7) + dateStr.substring(8);
//    }
//
//    public static String cutDateDelimit2(final String dateStr) {
//        final String rtn = dateStr.substring(0, 4) + dateStr.substring(5, 7) + dateStr.substring(8, 10);
//        return rtn;
//    }
//    /**
//     * 科目コードの頭2バイトをカットする。
//     * @param subclassCd 科目コード
//     * @return 短くなった科目コード
//     */
//    public static String cutSubclassCd(final String subclassCd) {
//        if (null == subclassCd || subclassCd.length() < 2) {
//            return subclassCd;
//        }
//        final String rtn = subclassCd.substring(2);
//        return rtn;
//    }
} // Mk

// eof
