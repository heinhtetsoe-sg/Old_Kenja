// kanji=漢字
/*
 * $Id: AbstractDaoUpdateAttendDat.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2007/02/08 20:30:19 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.dao;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.batch.accumulate.option.AccumulateOptions;
import jp.co.alp.kenja.batch.accumulate.option.Header;

/**
 * 出欠累積・欠席テーブル更新。
 * @author takaesu
 * @version $Id: AbstractDaoUpdateAttendDat.java 74552 2020-05-27 04:41:22Z maeshiro $
 */
public abstract class AbstractDaoUpdateAttendDat {
    /*
     * 複写区分。
     */
    protected static final String COPYCD = "0";

    /*pkg*/static final Log log = LogFactory.getLog(AbstractDaoUpdateAttendDat.class);

    protected final Connection _conn;
    protected final ControlMaster _cm;
    protected final AccumulateOptions _options;

    /**
     * コンストラクタ。
     * @param conn コネクション
     * @param cm コントロール・マスタ
     * @param options オプション
     */
    public AbstractDaoUpdateAttendDat(
            final Connection conn,
            final ControlMaster cm,
            final AccumulateOptions options
    ) {
        _conn = conn;
        _cm = cm;
        _options = options;
    }

    /**
     * ヘッダの指定範囲に該当するデータを削除する。
     * @param header ヘッダ
     * @throws SQLException SQL例外
     */
    public abstract void delete(final Header header) throws SQLException;
} // AbstractDaoUpdateAttendDat

// eof
