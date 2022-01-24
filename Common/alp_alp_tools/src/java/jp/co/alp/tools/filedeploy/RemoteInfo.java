// kanji=����
/*
 * $Id: RemoteInfo.java 56576 2017-10-22 11:25:31Z maeshiro $
 *
 * �쐬��: 2006/06/29 10:13:54 - JST
 * �쐬��: tamura
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
 * �R�s�[��̃T�[�o�[�Ɋւ�����B
 * �Ƃ肠�����A�R�s�[��́u�P�Ɓv�̂݁B(�����T�[�o�[�͖��Ή�)
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
