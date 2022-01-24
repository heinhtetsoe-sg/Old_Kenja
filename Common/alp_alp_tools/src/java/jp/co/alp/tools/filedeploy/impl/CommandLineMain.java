// kanji=����
/*
 * $Id: CommandLineMain.java 56576 2017-10-22 11:25:31Z maeshiro $
 *
 * �쐬��: 2006/06/28 21:01:00 - JST
 * �쐬��: tamura
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.tools.filedeploy.impl;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.tools.filedeploy.FileDeploy;

/**
 * �R�}���h���C���ł̃��C���B
 * @author tamura
 * @version $Id: CommandLineMain.java 56576 2017-10-22 11:25:31Z maeshiro $
 */
public class CommandLineMain {
    /*pkg*/static final Log log = LogFactory.getLog(CommandLineMain.class);

    public static void main(final String[] args) throws IOException {
        final CommandLineOptions options = new CommandLineOptions();
        options.setArgs(args);

        final FileDeploy fileDeploy = new FileDeploy(System.out, options);
        fileDeploy.doAll();
    }
} // CommandLineMain

// eof
