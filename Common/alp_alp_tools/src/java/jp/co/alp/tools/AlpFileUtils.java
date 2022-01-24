// kanji=漢字
/*
 * $Id: AlpFileUtils.java 56576 2017-10-22 11:25:31Z maeshiro $
 *
 * 作成日: 2006/06/26 11:08:01 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ファイル操作ユーティリティ。
 * @author tamura
 * @version $Id: AlpFileUtils.java 56576 2017-10-22 11:25:31Z maeshiro $
 */
public final class AlpFileUtils {
    private static final Collection ROOTS = new HashSet(Arrays.asList(File.listRoots()));

    /*pkg*/static final Log log = LogFactory.getLog(AlpFileUtils.class);

    private AlpFileUtils() {
        super();
    }

    //========================================================================

    public static File find(
            final File startDir,
            final String name
    ) throws FileNotFoundException {
        return find(startDir, null, name);
    }

    public static File find(
            final File startDir,
            final String dirName,
            final String name
    ) throws FileNotFoundException {
        File d = startDir.getAbsoluteFile();
        while (true) {
            d = d.getParentFile();
            if (ROOTS.contains(d)) {
                log.error("ROOTに到達した");
                throw new FileNotFoundException("ROOTに到達した");
            }

            if (null == dirName) {
                // 中間ディレクトリなし
                final File file = new File(d, name);
                if (!file.exists()) {
                    continue;
                }

//                log.debug("見つけた。[" + file + "]");
                return file;
            } else {
                // 中間ディレクトリあり
                final File dir = new File(d, dirName);
                if (!dir.exists()) {
                    continue;
                }

                final File file = new File(dir, name);
                if (!file.exists()) {
                    log.error(dir + "に、" + name + "が存在しない");
                    throw new FileNotFoundException(dir + "に、" + name + "が存在しない");
                }

//                log.debug("見つけた。[" + file + "]");
                return file;
            }
        }
    }

    //========================================================================

    public static Properties loadProp(final File file) throws IOException {
        InputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(file));
            final Properties rtn = new Properties();
            rtn.load(is);
            return rtn;
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    //========================================================================

    public static String[][] fileTimeComp(
            final File[] fromFiles,
            final File toDir
    ) {
        final String[][] rtn = new String[fromFiles.length][2];

        for (int i = 0; i < fromFiles.length; i++) {
            final File toFile = new File(toDir, fromFiles[i].getName());
            if (!fromFiles[i].exists() && !toFile.exists()) {
                rtn[i][0] = "<not";
                rtn[i][1] = "found>";
                continue;
            }
            if (!fromFiles[i].exists()) {
                rtn[i][0] = "<remote";
                rtn[i][1] = "only>";
                continue;
            }
            if (!toFile.exists()) {
                rtn[i][0] = "<local";
                rtn[i][1] = "only>";
                continue;
            }

            final long from = fromFiles[i].lastModified();
            final long to = toFile.lastModified();
            final long diff = from - to;
            if (0L == diff) {
                rtn[i][0] = "";
                rtn[i][1] = "<same>";
            } else if (0L < diff) {
                final String[] tmp = timeHumanReadable(diff);
                rtn[i][0] = tmp[0];
                rtn[i][1] = tmp[1];
            } else {
                final String[] tmp = timeHumanReadable(-diff);
                rtn[i][0] = "-" + tmp[0];
                rtn[i][1] = tmp[1];
            }
        }

        return rtn;
    }

    //========================================================================

    /**
     * 指定された一つのファイルを、ディレクトリにコピー。
     * コピー後のファイル名は、元と同じ。
     * @param fromFile コピー元のファイル
     * @param toDir コピー先のディレクトリ
     * @param preserveFileDate タイムスタンプをコピーする場合は<code>true</code>
     * @throws IOException IO例外
     */
    public static void copyFileToDir(
            final File fromFile,
            final File toDir,
            final boolean preserveFileDate
    ) throws IOException {
        checkOfFromFile(fromFile);
        checkOfToDir(toDir);

        final File toFile = new File(toDir, fromFile.getName());
        checkOfToFile(toFile, false);

        copyFileWithTemp0(fromFile, toFile, preserveFileDate);
    }

    /**
     * 複数のファイルを、ディレクトリにコピー。
     * コピー後のファイル名は、元と同じ。
     * @param fromFiles コピー元のファイル
     * @param toDir コピー先のディレクトリ
     * @param preserveFileDate タイムスタンプをコピーする場合は<code>true</code>
     * @throws IllegalArgumentException コピー元のファイルがnullまたは空
     * @throws IOException IO例外
     */
    public static void copyFiles(
            final File[] fromFiles,
            final File toDir,
            final boolean preserveFileDate
    ) throws IOException {
        if (null == fromFiles || 0 == fromFiles.length) {
            throw new IllegalArgumentException("コピー元のファイルがnullまたは空");
        }
        for (int i = 0; i < fromFiles.length; i++) {
            checkOfFromFile(fromFiles[i]);
        }
        checkOfToDir(toDir);
        final int num = fromFiles.length;

        final File[] toFiles = new File[num];
        final boolean[] exists = new boolean[num];
        for (int i = 0; i < num; i++) {
            toFiles[i] = new File(toDir, fromFiles[i].getName());
            checkOfToFile(toFiles[i], false);
            exists[i] = toFiles[i].exists() && toFiles[i].isFile();
        }

        for (int i = 0; i < num; i++) {
            if (exists[i]) {
                final File tmpFile = File.createTempFile("@" + toFiles[i].getName() + ".", ".tmp", toDir);
                copyFile0(fromFiles[i], tmpFile, preserveFileDate);
                if (!toFiles[i].delete()) {
                    throw new IOException(toFiles[i] + "を削除できなかった");
                }
                if (!tmpFile.renameTo(toFiles[i])) {
                    throw new IOException(tmpFile + "を" + toFiles[i] + "にrenameできなかった");
                }
            } else {
                copyFile0(fromFiles[i], toFiles[i], preserveFileDate);
            }
        }
    }

    /**
     * ファイル名フィルタを通過した複数のファイルを、ディレクトリにコピー。
     * コピー後のファイル名は、元と同じ。
     * @param fromDir コピー元のディレクトリ
     * @param filter ファイル名フィルタ
     * @param toDir コピー先のディレクトリ
     * @param preserveFileDate タイムスタンプをコピーする場合は<code>true</code>
     * @throws IllegalArgumentException コピー元のファイルがnullまたは空
     * @throws IOException IO例外
     */
    public static void copyFiles(
            final File fromDir,
            final FilenameFilter filter,
            final File toDir,
            final boolean preserveFileDate
    ) throws IOException {
        final File[] fromFiles = fromDir.listFiles(filter);
        copyFiles(fromFiles, toDir, preserveFileDate);
    }

    /**
     * ファイルのタイムスタンプをコピーする。
     * @param fromFile 元のファイル
     * @param toFile コピー先のファイル
     * @throws IOException IO例外
     */
    public static void copyTimeStamp(
            final File fromFile,
            final File toFile
    ) throws IOException {
        checkOfFromFile(fromFile);
        if (!toFile.exists()) {
            throw new FileNotFoundException("ファイルがない:" + toFile);
        }
        checkOfToFile(toFile, true);

        copyTimeStamp0(fromFile, toFile);
    }

    //========================================================================

    private static void checkOfFromFile(final File file) throws IOException {
        if (null == file) {
            throw new IOException("元ファイルのパスが不正");
        }
        if (!file.exists()) {
            throw new IOException("元ファイルが存在しない:" + file);
        }
        if (!file.isFile()) {
            throw new IOException("元ファイルがファイルではない:" + file);
        }
        if (!file.canRead()) {
            throw new IOException("元ファイルが読み込み不可:" + file);
        }
    }

    private static void checkOfToFile(
            final File file,
            final boolean checkOfToDir
    ) throws IOException {
        if (null == file) {
            throw new IOException("コピー先のパスが不正");
        }
        if (file.exists()) {
            if (!file.isFile()) {
                throw new IOException("コピー先に同名の「非ファイル」がある:" + file);
            }
            if (!file.canWrite()) {
                throw new IOException("コピー先が書き込み不可能:" + file);
            }
        } else {
            if (checkOfToDir) {
                final File dir = file.getParentFile();
                if (null == dir) {
                    throw new IOException("コピー先のパスが不正:" + file);
                }
                checkOfToDir(dir);
            }
        }
    }

    private static void checkOfToDir(final File dir) throws IOException {
        if (null == dir) {
            throw new IOException("コピー先のパスが不正");
        }
        if (!dir.exists()) {
            throw new IOException("コピー先ディレクトリが存在しない:" + dir);
        }
        if (!dir.isDirectory()) {
            throw new IOException("コピー先ディレクトリがディレクトリではない:" + dir);
        }
        if (!dir.canWrite()) {
            throw new IOException("コピー先ディレクトリが書き込み不可:" + dir);
        }
    }

    //========================================================================

    private static void copyFileWithTemp0(
            final File fromFile,
            final File toFile,
            final boolean preserveFileDate
    ) throws IOException {
        if (toFile.exists() && toFile.isFile()) {
            final File tmpFile = File.createTempFile("@" + toFile.getName() + ".", ".tmp", toFile.getParentFile());
            copyFile0(fromFile, tmpFile, preserveFileDate);
            if (!toFile.delete()) {
                throw new IOException(toFile + "を削除できなかった");
            }
            if (!tmpFile.renameTo(toFile)) {
                throw new IOException(tmpFile + "を" + toFile + "にrenameできなかった");
            }
        } else {
            copyFile0(fromFile, toFile, preserveFileDate);
        }
    }

    private static void copyFile0(
            final File fromFile,
            final File toFile,
            final boolean preserveFileDate
    ) throws IOException {
        InputStream  in = null;
        OutputStream out = null;
        try {
            in = new BufferedInputStream(new FileInputStream(fromFile));
            out = new BufferedOutputStream(new FileOutputStream(toFile));

            int charData;
            while (-1 != (charData = in.read())) {
                out.write(charData);
            }
            out.flush();
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }

        if (preserveFileDate) {
            copyTimeStamp0(fromFile, toFile);
        }
    }

    private static void copyTimeStamp0(
            final File fromFile,
            final File toFile
    ) {
        long timeStamp = 0L;
        try {
            timeStamp = fromFile.lastModified();
        } catch (final SecurityException e) {
            log.warn(fromFile + "のタイムスタンプを得られない", e);
            return;
        }

        try {
            toFile.setLastModified(timeStamp);
        } catch (final SecurityException e) {
            log.warn(toFile + "のタイムスタンプを変更できない", e);
        }
    }

    //========================================================================

    private static final long ONE_SEC = 1000L;
    private static final long ONE_MINU = 60 * ONE_SEC;
    private static final long ONE_HOUR = 60 * ONE_MINU;
    private static final long ONE_DAY  = 24 * ONE_HOUR;
    private static final long ONE_WEEK = 7 * ONE_DAY;
    private static final long ONE_MONTH = 30 * ONE_DAY;
    private static final long ONE_YEAR = 365 * ONE_DAY;

    private static String[] timeHumanReadable(final long ms) {
        long n;
        String unit;
        boolean plural = true;
    xxx:
        {
            n = ms / ONE_YEAR;
            if (1 < n) {
                unit = "year";
                break xxx;
            }

            n = ms / ONE_MONTH;
            if (1 < n) {
                unit = "month";
                break xxx;
            }

            n = ms / ONE_WEEK;
            if (1 < n) {
                unit = "week";
                break xxx;
            }

            n = ms / ONE_DAY;
            if (1 < n) {
                unit = "day";
                break xxx;
            }

            n = ms / ONE_HOUR;
            if (1 < n) {
                unit = "hour";
                break xxx;
            }

            n = ms / ONE_MINU;
            if (1 < n) {
                unit = "minute";
                break xxx;
            }

            n = ms / ONE_SEC;
            if (1 < n) {
                unit = "sec";
                plural = false;
                break xxx;
            }

            n = ms;
            unit = "ms";
            plural = false;
        }

        final String[] rtn = new String[2];
        rtn[0] = String.valueOf(n);
        rtn[1] = unit + String.valueOf(1 != n && plural ? "s" : "");
        return rtn;
    }

} // AlpFileUtils

// eof
