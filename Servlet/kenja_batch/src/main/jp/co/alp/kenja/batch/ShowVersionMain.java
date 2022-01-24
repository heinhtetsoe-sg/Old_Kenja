// kanji=漢字
/*
 * $Id: ShowVersionMain.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2007/01/20 13:27:24 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

/**
 * バージョン情報表示など。
 * @author takaesu
 * @version $Id: ShowVersionMain.java 74552 2020-05-27 04:41:22Z maeshiro $
 */
public final class ShowVersionMain {
    /*pkg*/static final PrintStream OUT = System.out;
    /*pkg*/static final PrintStream ERR = System.err;

    private static final Class<?> MYCLASS = ShowVersionMain.class;

    /*
     * コンストラクタ。
     */
    private ShowVersionMain() {
    }

    /*pkg*/static void checkClasspath() {
        OUT.println("--java.class.path");
        final String sepa = System.getProperty("path.separator");
        final String cp = System.getProperty("java.class.path");
        final StringTokenizer st = new StringTokenizer(cp, sepa);
        while (st.hasMoreTokens()) {
            final String token = st.nextToken();
            final File file = new File(token);
            if (file.exists()) {
                OUT.println("    " + (file.isDirectory() ? "[D] " : "[F] ") + file);
            } else {
                ERR.println("    NG " + file + ": no such file or dir.");
            }
        }
    }

    /* "jp.co.alp.kenja.ShowVersionMain"を
     * "jp/co/alp/kenja/ShowVersionMain.class"に変換する
     */
    /*pkg*/static String classNameToFileName(final Class<?> clazz) {
        return clazz.getName().replace('.', '/') + ".class";
    }

    /*pkg*/static void printThisJar() {
        final ClassLoader loader = MYCLASS.getClassLoader();
        if (null == loader) {
            return;
        }

        final URL url = loader.getResource(classNameToFileName(MYCLASS));
        final String str = url.toString();
        final int i = str.indexOf('!');
        if (-1 != i) {
            OUT.println(str.substring(0, i));
        } else {
            OUT.println(url);
        }
    }

    /*pkg*/static void printVersion() throws IOException {
        final ClassLoader loader = MYCLASS.getClassLoader();
        if (null == loader) {
            return;
        }

        BufferedReader br = null;
        try {
            final URL url = loader.getResource("version.txt");
            br = new BufferedReader(new InputStreamReader(url.openStream()));
            OUT.println(br.readLine());
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            if (null != br) {
                try {
                    br.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*pkg*/static void printProps() {
        OUT.println("--properties");
        final Set<Object> set = new TreeSet<Object>(System.getProperties().keySet());
        for (final Iterator<Object> it = set.iterator(); it.hasNext();) {
            final String key = it.next().toString();
            final String val = System.getProperty(key);
            OUT.println("    " + key + "=" + val);
        }
    }

    /**
     * メイン。
     * @param args コマンドライン引数
     * @throws Exception 例外
     */
    public static void main(final String[] args) throws Exception {
        OUT.println("kenja-batch");
        printVersion();
        printThisJar();
        checkClasspath();
//        printProps();
    }
} // ShowVersionMain

// eof
