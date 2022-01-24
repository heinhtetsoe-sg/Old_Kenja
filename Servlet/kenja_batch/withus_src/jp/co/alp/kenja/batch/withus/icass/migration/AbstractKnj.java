// kanji=漢字
/*
 * $Id: AbstractKnj.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/07/15 14:17:31 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.text.DecimalFormat;

import java.sql.SQLException;

import nao_package.db.DB2UDB;

import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * データ移行の共通クラス。
 * @author takaesu
 * @version $Id: AbstractKnj.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public abstract class AbstractKnj {
    /*pkg*/static final Log log = LogFactory.getLog(AbstractKnj.class);

    public static final DecimalFormat _subClassCdFormat = new DecimalFormat("0000");

    DB2UDB _db2;
    Param _param;

    final MapListHandler _handler = new MapListHandler();
    final QueryRunnerKnj _runner = new QueryRunnerKnj();

    public AbstractKnj() {
    }

    /**
     * コンストラクタ。
     * @param db2 DB
     * @param param パラメータ
     * @deprecated リフレクション機能を使うので廃止
     */
    public AbstractKnj(DB2UDB db2, final Param param) {
        init(db2, param);
    }

    public void init(final DB2UDB db2, final Param param) {
        _db2 = db2;
        _param = param;
        _runner.init(db2);
    }

    /**
     * データを移行する。
     */
    abstract void migrate() throws SQLException;

    /**
     * タイトルを得る。
     * @return タイトル
     */
    abstract String getTitle();

    /**
     * タイトルをログ出力した後、データを移行する。
     */
    public void migrateData() throws SQLException {
        log.info("★" + getTitle());
        migrate();
    }

    protected static String getInsertVal(final String str) {
        if (null != str && 0 < str.length()) {
            return "'" + str + "'";
        } else {
            return "null";
        }
    }

    protected static String getInsertChangeVal(final String str) {
        if (null != str && 0 < str.length()) {
            final StringBuffer retStr = new StringBuffer();
            final char[] chars = str.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                if (chars[i] == '\'') {
                    retStr.append("''");
                } else {
                    retStr.append(chars[i]);
                }
            }
            return "'" + retStr.toString() + "'";
        } else {
            return "null";
        }
    }

    protected static String getInsertVal(final Integer str) {
        if (null != str) {
            return str.toString();
        } else {
            return "null";
        }
    }

    /**
     * 数字(全半角)の出現箇所で文字列を分割
     * @param str
     * @return
     */
    protected static String[] divideStr(final String str) {
        String[] retStr = new String[2];
        retStr[0] = str;
        retStr[1] = "";
        if (null == str) {
            return retStr;
        }

        char[] strArray = str.toCharArray();
        int divideCnt = 0;
        for (int i = 0; i < strArray.length; i++) {
            if (strArray[i] == '0' || strArray[i] == '1' ||
                strArray[i] == '2' || strArray[i] == '3' ||
                strArray[i] == '4' || strArray[i] == '5' ||
                strArray[i] == '6' || strArray[i] == '7' ||
                strArray[i] == '8' || strArray[i] == '9' ||
                strArray[i] == '０' || strArray[i] == '１' ||
                strArray[i] == '２' || strArray[i] == '３' ||
                strArray[i] == '４' || strArray[i] == '５' ||
                strArray[i] == '６' || strArray[i] == '７' ||
                strArray[i] == '８' || strArray[i] == '９'
                
            ) {
                divideCnt = i;
                break;
            }
        }
    
        if (0 < divideCnt) {
            retStr[0] = str.substring(0, divideCnt);
            retStr[1] = str.substring(divideCnt);
        }
        return retStr;
    }

    /**
     * 文字列から、指定文字を削除
     */
    protected static String deleteStr(final String str, final String delStr) {
        if (null == str) {
            return null;
        }
        final StringBuffer retStb = new StringBuffer(str);
        if (0 < ((String) retStb.toString()).indexOf(delStr)) {
            for (; 0 < ((String) retStb.toString()).indexOf(delStr);) {
                final int delIndex = ((String) retStb.toString()).indexOf(delStr);
                retStb.delete(delIndex, delIndex + 1);
            }
        }
        return retStb.toString();
    }

    /**
     * 指定された文字数と、分割数で文字列を分割する。
     * 指定を超えた分は、捨てる。
     * @param str：文字列
     * @param dividlen：分割文字数
     * @param dividnum：分割数
     * @return 分割した文字配列
     */
    //TODO: StringUtils.split が似たような動作をするよ
    public static String[] retDividString(final String str, final int dividlen, final int dividnum) {
        String[] retStr = new String[dividnum];
        if (str == null || 0 == str.length()) {
            return retStr;
        }
    
        char[] strArray = str.toCharArray();
        int divideCnt = 0;
        int dividnumCnt = 0;
        for (int i = 0; i < strArray.length; i++) {
            divideCnt++;
            if (null == retStr[dividnumCnt]) {
                retStr[dividnumCnt] = "";
            }
            retStr[dividnumCnt] += strArray[i];
            if (divideCnt == dividlen) {
                dividnumCnt++;
                divideCnt = 0;
            }
            if (dividnum <= dividnumCnt) {
                break;
            }
        }
        return retStr;
    }

    /**
     * 期間の月数を得る。
     * @param fromDate 開始日付
     * @param toDate 終了日付
     * @return 月数。例) 2004-03-xx & 2005-09-xx ⇒ 19
     */
    public static Integer get月数(final String fromDate, final String toDate) {
        final int fYear = Integer.parseInt(fromDate.substring(0, 4));
        final int fMonth = Integer.parseInt(fromDate.substring(5, 7));

        final int tYear = Integer.parseInt(toDate.substring(0, 4));
        final int tMonth = Integer.parseInt(toDate.substring(5, 7));

        int ans = (tYear - fYear) * 12 + tMonth - fMonth;
        ans += 1;

        return new Integer(ans);
    }
} // AbstractKnj

// eof
