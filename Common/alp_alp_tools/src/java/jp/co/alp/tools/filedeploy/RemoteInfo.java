// kanji=漢字
/*
 * $Id: RemoteInfo.java 56576 2017-10-22 11:25:31Z maeshiro $
 *
 * 作成日: 2006/06/29 10:13:54 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.tools.filedeploy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import jp.co.alp.tools.AlpFileUtils;

/**
 * コピー先のサーバーに関する情報。
 * とりあえず、コピー先は「単独」のみ。(複数サーバーは未対応)
 * @author tamura
 * @version $Id: RemoteInfo.java 56576 2017-10-22 11:25:31Z maeshiro $
 */
public class RemoteInfo {
    private static final String PROPSNAME = "_remote.properties";

    private final FileDeployInfo _filedeployInfo;
    private final Properties _props;

    private RemoteInfo(
            final FileDeployInfo filedeployInfo,
            final File file
    ) throws IOException {
        super();
        _filedeployInfo = filedeployInfo;
        _props = AlpFileUtils.loadProp(file);
    }

    public static RemoteInfo findRemoteInfo(
            final File startDir,
            final FileDeployInfo filedeployInfo
    ) throws IOException {
        return new RemoteInfo(filedeployInfo, AlpFileUtils.find(startDir, PROPSNAME));
    }

    public String getServerName() {
        final String name = _props.getProperty("server");
        return name;
    }

    public String getServerDir() throws FileNotFoundException {
        final String name = getServerName();
        final String serverDir = _filedeployInfo.getServerDir(name);
        return serverDir;
    }
} // RemoteInfo

// eof
