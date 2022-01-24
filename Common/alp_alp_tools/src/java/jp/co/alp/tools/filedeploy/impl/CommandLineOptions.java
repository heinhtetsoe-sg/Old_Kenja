// kanji=漢字
/*
 * $Id: CommandLineOptions.java 56576 2017-10-22 11:25:31Z maeshiro $
 *
 * 作成日: 2006/06/30 11:18:16 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.tools.filedeploy.impl;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.tools.filedeploy.Options;

/**
 * コマンドライン版「オプション解析と保持」。
 * @author tamura
 * @version $Id: CommandLineOptions.java 56576 2017-10-22 11:25:31Z maeshiro $
 */
public class CommandLineOptions extends Options {
    /*pkg*/static final Log log = LogFactory.getLog(CommandLineOptions.class);

    private List _args;

    public CommandLineOptions() {
        super();
    }

    public void setArgs(final String[] args) {
        _args = argsToList(args);
        parse();
    }

    private static void printUsage(final PrintStream ps) {
        ps.println();
        ps.println("usage: kfu [オプション...] [ディレクトリ...]");
        ps.println("  オプション: 以下のいづれか一つまたは組み合わせを指定できます。");
        ps.println();
        ps.println("    -deploy");
        ps.println("    -copy");
        ps.println("      実際にコピーを実行します。");
        ps.println();
        ps.println("    -showInfo");
        ps.println("    -info");
        ps.println("      情報を表示します。");
        ps.println();
        ps.println("    -compTS");
        ps.println("      ファイルのタイムスタンプ(TS)を比較します。");
        ps.println();
        ps.println("    -diff");
        ps.println("      ファイルの内容を比較し、1バイトでも異なれば「外部比較ツール」を起動します。");
        ps.println();
        ps.println("    -help");
        ps.println("      簡単なヘルプ(これ)を表示します(他のオプションやディレクトリ指定は無効になる)。");
        ps.println();
        ps.println("  ディレクトリ: 一つまたは複数のディレクトリを指定できます。");
        ps.println("              : ディレクトリを一つも指定しなかった場合は、");
        ps.println("              : 現在のディレクトリが指定されたことになります。");
    }

    private void parse() {
        boolean option = true;

        for (final Iterator it = _args.iterator(); it.hasNext();) {
            final String arg = (String) it.next();
            if (StringUtils.isEmpty(arg)) {
                continue;
            }
            if ("-help".equalsIgnoreCase(arg)) {
                printUsage(System.out);
                return;
            }
        }

        for (final Iterator it = _args.iterator(); it.hasNext();) {
            final String arg = (String) it.next();
            if (StringUtils.isEmpty(arg)) {
                continue;
            }

            if (option) {
                if ('-' == arg.charAt(0)) {
                    if ("-deploy".equalsIgnoreCase(arg) || "-copy".equalsIgnoreCase(arg)) {
                        _deploy = true;
                        continue;
                    }
                    if ("-showInfo".equalsIgnoreCase(arg) || "-info".equalsIgnoreCase(arg)) {
                        _showInfo = true;
                        continue;
                    }
                    if ("-compTS".equalsIgnoreCase(arg)) {
                        _compTS = true;
                        continue;
                    }
                    if ("-diff".equalsIgnoreCase(arg)) {
                        _diff = true;
                        continue;
                    }

                    log.warn("不明なオプション:" + arg);
                } else {
                    option = false;
                }
            }

            if (!option){
                addDir(arg);
            }
        }

        if (getDirs().isEmpty()) {
            addDir(".");
        }
    }

    private static List argsToList(final String args[]) {
        final List rtn = new LinkedList();
        if (null != args && 0 != args.length) {
            for (int i = 0; i < args.length; i++) {
                rtn.add(args[i]);
            }
        }
        return rtn;
    }
} // CommandLineOptions

// eof
