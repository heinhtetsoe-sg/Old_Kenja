// kanji=漢字
/*
 * $Id: KenjaThread.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2004/07/10 13:43:16 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.lang;

import java.security.AccessControlException;

import org.apache.oro.text.perl.Perl5Util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.util.IntValueMap;

/**
 * 賢者パッケージのスレッド。
 * @author tamura
 * @version $Id: KenjaThread.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public class KenjaThread extends Thread {
    /** log */
    /*pkg*/static final Log log = LogFactory.getLog(KenjaThread.class);

    /*pkg*/static ThreadGroup tg_;
    private static int initNumber_;

    private static final Perl5Util P5UTIL = new Perl5Util();
    private static final IntValueMap MAP = new IntValueMap();

    /**
     * コンストラクタ。
     * @param target ターゲット
     * @param name 名前
     */
    public KenjaThread(final Runnable target, final String name) {
        super(tg_, target, createNumberedName(name));
//        if (log.isTraceEnabled()) {
//            final PrintStream out = System.out;
//            out.println(">>thread-list");
//            getTopThreadGrooup().list();
//            out.println("<<thread-list");
//        }
    }

    /**
     * コンストラクタ。
     * @param name 名前
     */
    public KenjaThread(final String name) {
        this(null, name);
    }

    /*
     */
    private static synchronized String createNumberedName(final String name) {
        if (P5UTIL.match("m/[:\\-][0-9]+$/", name)) {
            // コロン(:)かハイフン(-)に続いて、数字で終わっている名前なら、そのまま返す。
            return name;
        }

        final int cnt = MAP.increment(name, 0);

        // ハイフン(-)と連番を付けた名前を返す。
        return name + "-" + String.valueOf(cnt);
    }

    //========================================================================

    /**
     * 最上位のスレッド・グループを得る。
     * @return 最上位のスレッド・グループ
     */
    public static ThreadGroup getTopThreadGrooup() {
        ThreadGroup tg = Thread.currentThread().getThreadGroup();
        while (true) {
            ThreadGroup tmp;
            try {
                tmp = tg.getParent();
            } catch (final AccessControlException e) {
                break;
            }
            if (null == tmp) {
                break;
            }
            tg = tmp;
        }
        return tg;
    }

    /**
     * スレッド・グループを得る。
     * @return スレッド・グループ
     */
    public static ThreadGroup getTG() { return tg_; }

    /**
     * 賢者のスレッド・グループを新しくする。
     */
    public static void renewalTG() {
        setTG(new ThreadGroup(getTopThreadGrooup(), "KENJA-TG-" + nextTGNum()));
    }

    /**
     * 賢者のスレッド・グループを廃棄する。
     */
    public static void destoryTG() {
        setTG(null);
    }

    /*
     */
    private static synchronized int nextTGNum() {
        return initNumber_++;
    }

    /*
     * スレッド・グループを設定する。
     * @param group スレッド・グループ
     */
    private static void setTG(final ThreadGroup group) {
        log.trace("setTG:" + (null == group ? "null" : group.getName()));
        if (null != tg_) {
            log.info("tg_.activeCount()=" + tg_.activeCount());
            if (!tg_.isDestroyed()) {
                try {
                    tg_.interrupt();
                } catch (final AccessControlException e) {
                    log.info("ignore execption...", e);
                }

                new DisposeTG(tg_).start();
            }
        }
        tg_ = group;
    }

    /**
     * スレッド・グループ廃棄。
     */
    private static class DisposeTG extends Thread {
        private final ThreadGroup _tg;
        protected DisposeTG(final ThreadGroup tg) {
            _tg = tg;
        }

        public void run() {
            log.trace("group=" + this.getThreadGroup());

            if (log.isTraceEnabled()) { getThreadGroup().list(); }

            if (null == _tg) {
                log.trace("ThreadGroup is null");
                return;
            }

            try {
                if (_tg.isDestroyed()) {
                    log.trace("ThreadGroup is destroyed");
                    return;
                }
                _tg.interrupt();
                _tg.destroy();
            } catch (final IllegalThreadStateException e) {
                log.info("ignore execption...", e);
            } catch (final AccessControlException e) {
                log.info("ignore execption...", e);
            }
        }
    } // DisposeTG
} // KenjaThread

// eof
