// kanji=漢字
/*
 * $Id: FileDeployInfo.java 56576 2017-10-22 11:25:31Z maeshiro $
 *
 * 作成日: 2006/06/28 20:29:26 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.tools.filedeploy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.tools.AlpFileUtils;

/**
 * file deployの主要な設定ファイル。
 * <tt>_config/_filedeploy.properties</tt>が絶対に必要。
 * 以下は例。<pre>
 * rule=jp.co.alp.tools.filedeploy.impl.kenja.KenjaDirRule
 * basedir=src
 * files=(^.*readme.*\.txt$|^knj.*\.?.*$)
 *
 * &#64;hiro=//hiro/development
 *
 * &#64;lion=//lion/development
 *
 * &#64;kindai=//tokio/deve_ktest
 * &#64;oomiya=//tokio/deve_oomiya
 * &#64;tokio=//tokio/development
 * </pre>
 * @author tamura
 * @version $Id: FileDeployInfo.java 56576 2017-10-22 11:25:31Z maeshiro $
 */
public class FileDeployInfo {
    private static final String DIRCONFIG = "_config";
    private static final String PROPSNAME = "_filedeploy.properties";

    /*pkg*/static final Log log = LogFactory.getLog(FileDeployInfo.class);

    private final Properties _props;
    private DirRule _dirRule;

    private FileDeployInfo(final File file) throws IOException {
        super();
        _props = AlpFileUtils.loadProp(file);
    }

    public static FileDeployInfo findFileDeployInfo(final File startDir) throws IOException {
        return new FileDeployInfo(AlpFileUtils.find(startDir, DIRCONFIG, PROPSNAME));
    }

    public String getBaseDir() throws FileNotFoundException {
        final String dir = _props.getProperty("basedir");
        if (null == dir) {
            throw new FileNotFoundException("basedirが不明");
        }
        return dir;
    }

    public String getFilesPattern() {
        final String pattern = _props.getProperty("files");
        return pattern;
    }

    public String getServerDir(final String serverName) throws FileNotFoundException {
        final String dir = _props.getProperty(serverName);
        if (null == dir) {
            throw new FileNotFoundException(serverName + "のディレクトリが不明");
        }
        log.info("ServerDir=" + dir);
        return dir;
    }

    public synchronized DirRule getDirRule() {
        if (null == _dirRule) {
            final String name = _props.getProperty("rule");
            _dirRule = createDirRule(name);
        }

        return _dirRule;
    }

    private static DirRule createDirRule(final String name) {
        Class clazz = null;
        try {
            clazz = Class.forName(name);
        } catch (final ClassNotFoundException e) {
            log.error(name + "というclassがない", e);
            return null;
        }

        Object instance = null;
        try {
            instance = clazz.newInstance();
        } catch (final InstantiationException e) {
            log.error("InstantiationException", e);
            return null;
        } catch (final IllegalAccessException e) {
            log.error("IllegalAccessException", e);
            return null;
        }

        if (instance instanceof DirRule) {
            return (DirRule) instance;
        }

        log.error("型が一致しない:" + instance.getClass().getName());
        return null;
    }
} // FileDeployInfo

// eof
