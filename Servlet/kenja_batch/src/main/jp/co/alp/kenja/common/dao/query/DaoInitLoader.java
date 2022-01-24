// kanji=漢字
/*
 * $Id: DaoInitLoader.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2004/07/05 18:27:14 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.dao.query;

import java.util.Properties;

import java.sql.SQLException;

import jp.co.alp.kenja.common.dao.DbConnection;
import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.lang.enums.MyEnum;
import jp.co.alp.kenja.common.util.KenjaParameters;

/**
 * マスタを読み込むインタフェース。
 * @author tamura
 * @version $Id: DaoInitLoader.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public interface DaoInitLoader {
    /**
     * マスタを読み込む。
     * @param category カテゴリー
     * @param cm コントロール・マスタ
     * @param dbcon DB接続情報
     * @param prop プロパティ
     * @param params パラメータ
     * @throws SQLException 例外
     */
    void load(
            final MyEnum.Category category,
            final ControlMaster cm,
            final DbConnection dbcon,
            final Properties prop,
            final KenjaParameters params
    ) throws SQLException;
} // DaoInitLoader

// eof
