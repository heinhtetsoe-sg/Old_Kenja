// kanji=漢字
/*
 * $Id: KenjaCommandLineParameters.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2004/07/20 19:06:54 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.util;

import java.net.URL;
import java.util.Map;

import java.awt.Image;
import java.awt.Toolkit;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * パラメータのコマンドライン引数版。
 * @author tamura
 * @version $Id: KenjaCommandLineParameters.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public class KenjaCommandLineParameters extends KenjaParameters {
    /** log */
    private static final Log log = LogFactory.getLog(KenjaCommandLineParameters.class);

    private final Map _map = new LinkedMap();

    /**
     * コンストラクタ。
     * @param args コマンドライン引数
     */
    public KenjaCommandLineParameters(final String[] args) {
        super();
        if (null == args || 0 == args.length) {
            return;
        }

        int pos;
        String k, v;
        for (int i = 0; i < args.length; i++) {
            pos = args[i].indexOf('=');
            if (0 <= pos) {
                k = StringUtils.trimToEmpty(args[i].substring(0, pos));
                v = StringUtils.trimToEmpty(args[i].substring(pos + 1));
                _map.put(k, v);
            }
        }

        super._dbhost = (String) _map.get(PARAM_DBHOST);
        super._dbname = (String) _map.get(PARAM_DBNAME);
        super._staffcd = (String) _map.get(PARAM_STAFFCD);
        super._useCurriculumcd = Boolean.valueOf((String) _map.get(PARAM_USE_CURRICULUMCD)).booleanValue();
        super._isShowStaffcd = Boolean.valueOf(StringUtils.defaultString((String) _map.get(PARAM_IS_SHOW_STAFFCD), "true")).booleanValue();
        super._menuSCHOOLKIND = (String) _map.get(PARAM_SCHOOLKIND);
        super._menuSCHOOLCD = (String) _map.get(PARAM_SCHOOLCD);

        log.fatal(PARAM_DBHOST + "=" + getDbHost()
                + ", " + PARAM_DBNAME + "=" + getDbName()
                + ", " + PARAM_STAFFCD + "=" + getStaffCd()
                + ", " + PARAM_USE_CURRICULUMCD + "=" + useCurriculumcd()
                + ", " + PARAM_IS_SHOW_STAFFCD + "=" + isShowStaffcd()
                + ", " + PARAM_SCHOOLKIND + "=" + getMenuSCHOOLKIND()
                + ", " + PARAM_SCHOOLCD + "=" + getMenuSCHOOLCD()
                );
        log.fatal("map=" + _map);
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return _map.toString();
    }

    /**
     * {@inheritDoc}
     */
    public String getParameter(final String key) {
        return (String) _map.get(key);
    }

    /**
     * {@inheritDoc}
     */
    public URL getResourceURL(final String filename) {
        final URL url = KenjaUtils.getResourceAsURL(this.getClass(), filename);
        log.debug("command line=" + url);
        return url;
    }

    /**
     * {@inheritDoc}
     */
    public Image getImage(final URL url) {
        final Toolkit tk = Toolkit.getDefaultToolkit();
        return tk.getImage(url);
    }
} // KenjaCommandLineParameters

// eof
