// kanji=漢字
/*
 * $Id: DirInfo.java 56576 2017-10-22 11:25:31Z maeshiro $
 *
 * 作成日: 2006/06/28 19:45:56 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.tools.filedeploy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Properties;

import org.apache.oro.text.perl.Perl5Util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.tools.AlpFileUtils;

/**
 * プログラムID毎のディレクトリ情報。
 * <tt>_dir.properties</tt>があれば読み込む。なければ<code>FileDeployInfo</code>に従う。
 * @author tamura
 * @version $Id: DirInfo.java 56576 2017-10-22 11:25:31Z maeshiro $
 */
public class DirInfo implements FilenameFilter {
    /*pkg*/static final Log log = LogFactory.getLog(DirInfo.class);

    private static final String PROPSNAME = "_dir.properties";

    private static final Perl5Util P5UTIL = new Perl5Util();

    private final FileDeployInfo _filedeployInfo;
    private final File _dir;
    private final Properties _props;

    public DirInfo(
            final FileDeployInfo filedeployInfo,
            final File dir
    ) throws IOException {
        super();
        _filedeployInfo = filedeployInfo;
        _dir = dir.getCanonicalFile();

        Properties props = null;

        final File file = new File(_dir, PROPSNAME);
        if (file.exists()) {
            try {
                props = AlpFileUtils.loadProp(file);
            } catch (final IOException e) {
                log.fatal(PROPSNAME + "でエラー", e);
            }
        }

        _props = props;
    }

    public String getSrcDir() throws FileNotFoundException {
        if (null != _props) {
            final String srcdir = _props.getProperty("srcdir");
            if (null != srcdir) {
                return srcdir;
            }
        }
        final DirRule dirRule = _filedeployInfo.getDirRule();
        if (null == dirRule) {
            return null;
        }
        return _filedeployInfo.getBaseDir() + "/" + dirRule.getSrcDir(_dir);
    }

    public String getFilesPattern() {
        if (null != _props) {
            final String files = _props.getProperty("files");
            if (null != files) {
                return files;
            }
        }
        return _filedeployInfo.getFilesPattern();
    }

    public File[] getFiles() {
        final File[] files = _dir.listFiles(this);
        if (null == files) {
            return new File[0];
        }
        return files;
    }

    /**
     * {@inheritDoc}
     */
    public boolean accept(final File dir, final String name) {
        if (P5UTIL.match(getFilesPattern(), name)) {
//            System.out.println("o :" + name);
            return true;
        }
//        System.out.println("x :" + name);
        return false;
    }
} // DirInfo

// eof
