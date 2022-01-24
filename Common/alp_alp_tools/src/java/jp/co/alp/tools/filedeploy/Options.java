// kanji=漢字
/*
 * $Id: Options.java 56576 2017-10-22 11:25:31Z maeshiro $
 *
 * 作成日: 2006/06/30 9:39:43 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.tools.filedeploy;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * オプション解析と保持。
 * @author tamura
 * @version $Id: Options.java 56576 2017-10-22 11:25:31Z maeshiro $
 */
public abstract class Options {
    /*pkg*/static final Log log = LogFactory.getLog(Options.class);

    protected boolean _showInfo;
    protected boolean _deploy;
    protected boolean _compTS;
    protected boolean _diff;

    private final List _dirs = new LinkedList();

    public Options() {
        super();
        init();
    }

    private void init() {
        _showInfo = false;
        _deploy = false;
        _compTS = false;
        _diff = false;
    }

    protected void addDir(final String dir) {
        _dirs.add(dir);
    }

    public boolean isShowInfo() { return _showInfo; }
    public boolean isDeploy() { return _deploy; }
    public boolean isCompTS() { return _compTS; }
    public boolean isDiff() { return _diff; }

    public List getDirs() {
        return _dirs;
    }
} // Options

// eof
