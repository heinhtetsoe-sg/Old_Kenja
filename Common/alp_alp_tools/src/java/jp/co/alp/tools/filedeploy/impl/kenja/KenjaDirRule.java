// kanji=漢字
/*
 * $Id: KenjaDirRule.java 56576 2017-10-22 11:25:31Z maeshiro $
 *
 * 作成日: 2006/06/28 22:00:21 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.tools.filedeploy.impl.kenja;

import java.io.File;

import org.apache.oro.text.perl.Perl5Util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.tools.filedeploy.DirRule;

/**
 * 「賢者」のディレクトリ名のルール。
 * @author tamura
 * @version $Id: KenjaDirRule.java 56576 2017-10-22 11:25:31Z maeshiro $
 */
public class KenjaDirRule implements DirRule {
    /*pkg*/static final Log log = LogFactory.getLog(KenjaDirRule.class);

    private static final String RE = "m/^KNJ([A-Z]).*$/";
    private static final Perl5Util P5UTIL = new Perl5Util();

    /**
     * 引数なしコンストラクタ。
     * リフレクションで生成されるので、「引数なしコンストラクタ」は必須。
     * @see jp.co.alp.tools.filedeploy.FileDeployInfo#createDirRule(String)
     */
    public KenjaDirRule() {
        super();
    }

    /**
     * 元ディレクトリから、「賢者のディレクトリ名のルール」によって、先ディレクトリ名を返す。
     * 例:
     * "KNJX123"    -> "X/KNJX123"
     * "KNJX123_1"  -> "X/KNJX123_1"
     * @param dir 元ディレクトリ
     * @return 先ディレクトリ名
     * @throws IllegalArgumentException 元ディレクトリがルールにマッチしない場合
     */
    public String getSrcDir(final File dir) {
        final String name = dir.getName();
        if (!P5UTIL.match(RE, name)) {
            throw new IllegalArgumentException(dir + ":マッチしません");
        }
        final String subsysdir = P5UTIL.group(1);
        return subsysdir + "/" + name;
    }
} // KenjaDirRule

// eof
