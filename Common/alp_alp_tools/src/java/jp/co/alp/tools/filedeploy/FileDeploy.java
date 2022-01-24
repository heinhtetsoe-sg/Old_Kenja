// kanji=漢字
/*
 * $Id: FileDeploy.java 56576 2017-10-22 11:25:31Z maeshiro $
 *
 * 作成日: 2006/06/29 17:01:05 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.tools.filedeploy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.tools.AlpFileUtils;

/**
 * ファイルコピー実行。
 * @author tamura
 * @version $Id: FileDeploy.java 56576 2017-10-22 11:25:31Z maeshiro $
 */
public class FileDeploy {
    /*pkg*/static final Log log = LogFactory.getLog(FileDeploy.class);

    private final PrintStream _ps;
    private final Options _options;

    private File _dir;
    private FileDeployInfo _fileDeployInfo;
    private RemoteInfo _remoteInfo;
    private DirInfo _dirInfo;
    private File[] _files;

    private static String _cmdName;

    public FileDeploy(final PrintStream ps, final Options options) {
        super();
        _ps = ps;
        _options = options;
    }

    public void setup(final String dirName) throws IOException {
        _dir = new File(dirName).getCanonicalFile();
        _fileDeployInfo = FileDeployInfo.findFileDeployInfo(_dir);
        _remoteInfo = RemoteInfo.findRemoteInfo(_dir, _fileDeployInfo);
        _dirInfo = new DirInfo(_fileDeployInfo, _dir);
        _files = _dirInfo.getFiles();
    }

    public void doAll() throws IOException {
        for (final Iterator it = _options.getDirs().iterator(); it.hasNext();) {
            final String dirName = (String) it.next();
            doOne(_ps, _options, dirName);
        }
    }

    private static void doOne(
            final PrintStream ps,
            final Options options,
            final String dirName
    ) throws IOException {
        final File dir = new File(dirName).getCanonicalFile();
        final FileDeployInfo fileDeployInfo = FileDeployInfo.findFileDeployInfo(dir);
        final RemoteInfo remoteInfo = RemoteInfo.findRemoteInfo(dir, fileDeployInfo);
        final DirInfo dirInfo = new DirInfo(fileDeployInfo, dir);
        final File[] files = dirInfo.getFiles();

        if (options.isShowInfo()) {
            showInfo(ps, dir, fileDeployInfo, remoteInfo, dirInfo, files);
        }

        if (options.isCompTS()) {
            compTS(ps, remoteInfo, dirInfo, files);
        }
        
        if (options.isDiff()) {
            final String username = System.getProperty("user.name", "unknown");
            final String hostname = System.getProperty("env.computername", "unknown");
            _cmdName = "diff-" + username + "@" + hostname + ".bat";
            ps.println("cmdName=" + _cmdName);
            diff(ps, remoteInfo, dirInfo, files);
        }

        if (options.isDeploy()) {
            deploy(ps, remoteInfo, dirInfo, files);
        }
    }

    private static void showInfo(
            final PrintStream ps,
            final File dir,
            final FileDeployInfo fileDeployInfo,
            final RemoteInfo remoteInfo,
            final DirInfo dirInfo,
            final File[] files
    ) throws FileNotFoundException {
        ps.println("-- from-to-dir --");
        ps.println("fromDir= " + dir);
        final File toDir = new File(remoteInfo.getServerDir(), dirInfo.getSrcDir());
        ps.println("toDir= " + toDir);
        ps.println();
        ps.println("-- fileDeployInfo --");
        ps.println("basedir= " + fileDeployInfo.getBaseDir());
        ps.println("files= " + fileDeployInfo.getFilesPattern());
        ps.println();
        ps.println("-- remoteInfo --");
        ps.println("server= " + remoteInfo.getServerName());
        ps.println("serverDir= " + remoteInfo.getServerDir());
        ps.println();
        ps.println("-- dirInfo --");
        ps.println("srcdir= " + dirInfo.getSrcDir());
        ps.println("files= " + dirInfo.getFilesPattern());
        ps.println();
        ps.println("-- " + files.length + " file(s) --");
        for (int i = 0; i < files.length; i++) {
            ps.println((i + 1) + ": " + files[i].getName());
        }
        ps.println("--");
    }


    private static void compTS(
            final PrintStream ps,
            final RemoteInfo remoteInfo,
            final DirInfo dirInfo,
            final File[] files
    ) throws IOException {
        ps.println();
        ps.println("== COMPare-TimeStamp ==");
        final File toDir = new File(remoteInfo.getServerDir(), dirInfo.getSrcDir());
        ps.println("remote is: " + toDir);
        ps.println("files: " + files.length);

        final String[][] comp = AlpFileUtils.fileTimeComp(files, toDir);
        int[] len = new int[2];
        for (int i = 0; i < comp.length; i++) {
            if (!StringUtils.isEmpty(comp[i][0])) {
                len[0] = Math.max(len[0], comp[i][0].length());
                len[1] = Math.max(len[1], comp[i][1].length());
            }
        }
        for (int i = 0; i < files.length; i++) {
            String str;
            if (StringUtils.isEmpty(comp[i][0])) {
                str = StringUtils.center(comp[i][1], len[0] + len[1] + 1);
            } else {
                final String num = StringUtils.leftPad(comp[i][0], len[0]);
                final String unit = StringUtils.rightPad(comp[i][1], len[1]);
                str = num + " " + unit;
            }
            ps.println((i + 1) + ": " + str + " " + files[i].getName());
        }
    }

    private static void diff(
            final PrintStream ps,
            final RemoteInfo remoteInfo,
            final DirInfo dirInfo,
            final File[] files
    ) throws IOException {
        ps.println();
        ps.println("== DIFF ==");
        final File toDir = new File(remoteInfo.getServerDir(), dirInfo.getSrcDir());
        ps.println("remote is: " + toDir);
        ps.println("files: " + files.length);

        int len = 0;
        for (int i = 0; i < files.length; i++) {
            len = Math.max(len, files[i].getName().length());
        }

        for (int i = 0; i < files.length; i++) {
            final File f1 = files[i];
            final File f2 = new File(toDir, files[i].getName());

            final String name = StringUtils.rightPad(f1.getName(), len);

            if (!f1.exists()) {
                ps.println("skip: " + name + " is LOCAL only");
                continue;
            }
            if (!f2.exists()) {
                ps.println("skip: " + name + " is REMOTE only");
                continue;
            }
            if (FileUtils.contentEquals(f1, f2)) {
                ps.println("skip: " + name + " are identify");
                continue;
            }

            final String[] cmdarray = new String[3];
            ps.println("      " + name + " are DIFF");

                cmdarray[0] = _cmdName;
            cmdarray[1] = "\"" + f1.getAbsolutePath() + "\"";
            cmdarray[2] = "\"" + f2.getAbsolutePath() + "\"";
            try {
                Runtime.getRuntime().exec(cmdarray);
            } catch (final IOException e) {
                e.printStackTrace(ps);
            }
        }
    }

    private static void deploy(
            final PrintStream ps,
            final RemoteInfo remoteInfo,
            final DirInfo dirInfo,
            final File[] files
    ) throws IOException {
        ps.println();
        ps.println("== DEPLOY ==");
        final File toDir = new File(remoteInfo.getServerDir(), dirInfo.getSrcDir());
        ps.println("copy to: " + toDir);
        ps.println("files: " + files.length);
        AlpFileUtils.copyFiles(files, toDir, true);
        saveDeployHist(toDir, files);
        ps.println("done");
    }

    private static void saveDeployHist(
            final File toDir,
            final File[] files
    ) {
        final String username = System.getProperty("user.name", "unknown");
        final String hostname = System.getProperty("env.computername", "unknown");

        PrintStream os = null;
        try {
            final File file = new File(toDir, "_" + username + "@" + hostname + ".txt");
            final Date date = new Date();
            os = new PrintStream(new FileOutputStream(file, true));
            os.println("====");
            os.println(username + "," + hostname + "," + date.toString());
            os.println("  " + files.length + " file(s)");
            for (int i = 0; i < files.length; i++) {
                os.println("  " + (i + 1) + ": " + files[i].getName());
            }
            os.flush();
        } catch (final IOException e) {
            log.error("IOException", e);
        } finally {
            IOUtils.closeQuietly(os);
        }
    }
} // FileDeploy

// eof
