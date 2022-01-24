// kanji=漢字
/*
 * $Id: ReflectHistoryParameters.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2006/09/22 14:57:43 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.reflectHistory;

import java.text.ParseException;
import java.util.Properties;

import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import jp.co.alp.kenja.common.util.KenjaCommandLineParameters;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 履歴反映のパラメータ。
 * @author maesiro
 * @version $Id: ReflectHistoryParameters.java 74552 2020-05-27 04:41:22Z maeshiro $
 */
public class ReflectHistoryParameters extends KenjaCommandLineParameters {
    /** 日付オプションのキーワード。 */
    public static final String DATE = "date";

    /** 実行の有無 */
    public static final String DO_RUN = "doRun";

    /*pkg*/static final Log log = LogFactory.getLog(ReflectHistoryParameters.class);

    /** 実施日付 */
    private final KenjaDateImpl _date;

    private final boolean _doRun;

    private final Properties _properties;

    /**
     * コンストラクタ。
     * @param args 引数
     * @param properties プロパティ
     */
    public ReflectHistoryParameters(final String[] args, final Properties properties) {
        super(args);
        log.debug("パラメータ=" + toString());

        final String dateStr = getParameter(DATE);
        try {
            _date = KenjaDateImpl.getInstance(dateStr);
        } catch (final ParseException e) {
            log.error("パラメータ不正(date):" + dateStr, e);
            throw new IllegalArgumentException("パラメータ不正(date):" + dateStr);
        }

        _properties = properties;
        log.debug("プロパティファイルの項目=" + _properties);

        _doRun = BooleanUtils.toBoolean(_properties.getProperty(DO_RUN));

        final String registerCd = getStaffCd();
        log.info("登録者コード(REGISTERCD/STAFFCD)=" + registerCd + ", length=" + registerCd.length());
    }

    /**
     * 実施日付を得る。
     * @return 実施日付
     */
    public KenjaDateImpl getKenjaDate() {
        return _date;
    }

    /**
     * 実行の有無を得る。
     * @return true なら処理を実行する
     */
    public boolean doRun() {
        return _doRun;
    }

    /**
     * プロパティーを得る。
     * @return プロパティー
     */
    public Properties getProperties() {
        return _properties;
    }

} // ReflectHistoryOptions

// eof
