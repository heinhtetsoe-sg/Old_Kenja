// kanji=漢字
/*
 * $Id: KenjaProgramInfo.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2004/05/18 19:56:37 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.util.KenjaIOUtils;
import jp.co.alp.kenja.common.util.KenjaUtils;


/**
 * プログラムの情報。
 * @author tamura
 * @version $Id: KenjaProgramInfo.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public abstract class KenjaProgramInfo {
    /** */
    public static final String VERSION_TXT = "version.txt";

    /** log */
    private static final Log log = LogFactory.getLog(KenjaProgramInfo.class);

    protected volatile String _ver;

    /**
     * コンストラクタ。
     */
    public KenjaProgramInfo() {
    }

    /**
     * プログラム名称を得る。
     * @return プログラム名称
     */
    public abstract String getProgramName();

    /**
     * プログラムIDを得る。
     * @return プログラムID
     */
    public abstract String getProgramId();

    /**
     * プログラム・バージョンを得る。
     * @return プログラム・バージョン
     */
    public synchronized String getProgramVersion() {
        if (null != _ver) {
            return _ver;
        }

        _ver = getProgramVersion0();
        return _ver;
    }

    /*
     * プログラム・バージョンを得る。
     * @return プログラム・バージョン
     * @note _ver フィールドに代入しない
     */
    private String getProgramVersion0() {
        final URL url = KenjaUtils.getResourceAsURL(getClass(), VERSION_TXT);
        if (null == url) {
            log.warn("can not open url:" + VERSION_TXT);
            return "unknown";
        }
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            while (null != (line = br.readLine())) {
                log.debug("line=[" + line + "]");
                if (!StringUtils.isEmpty(line) && !line.startsWith("#")) {
                    log.info("ver=[" + line + "]");
                    return line;
                }
            }
        } catch (final IOException e) {
            log.error("IOException", e);
        } finally {
            KenjaIOUtils.closeQuietly(br);
        }

        log.warn("may be, file '" + VERSION_TXT + "' is empty.");
        return "unknown";
    }
} // KenjaProgramInfo

// eof
