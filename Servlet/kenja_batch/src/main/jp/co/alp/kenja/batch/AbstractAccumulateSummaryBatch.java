// kanji=漢字
/*
 * $Id: AbstractAccumulateSummaryBatch.java 76357 2020-09-02 06:37:30Z maeshiro $
 *
 * 作成日: 2006/12/09 10:46:03 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch;

import java.sql.SQLException;
import java.util.Properties;

import jp.co.alp.kenja.batch.accumulate.option.AccumulateOptions;
import jp.co.alp.kenja.common.dao.DbConnection;
import jp.co.alp.kenja.common.dao.query.DaoInitLoader;
import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.lang.enums.MyEnum;
import jp.co.alp.kenja.common.util.KenjaParameters;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <<クラスの説明>>。
 * @author takaesu
 * @version $Id: AbstractAccumulateSummaryBatch.java 76357 2020-09-02 06:37:30Z maeshiro $
 */
public abstract class AbstractAccumulateSummaryBatch extends AbstractKenjaBatch {
    /*pkg*/static final Log log = LogFactory.getLog(AbstractAccumulateSummaryBatch.class);
    final AccumulateOptions _options;

    /**
     * コンストラクタ。
     * @param options オプション
     */
    public AbstractAccumulateSummaryBatch(final AccumulateOptions options) {
        super(options);
        if (null == options) {
            throw new IllegalArgumentException();
        }
        _options = options;
    }

    /**
     * マスタのデータをロードする
     * @param category カテゴリー
     * @param dbcon DB接続
     * @param cm コントロールマスタ
     * @param properties プロパティー
     * @param kenjaParams パラメータ
     * @throws Exception 例外
     */
    protected void loadMasterData(
            final MyEnum.Category category,
            final DaoInitLoader loader,
            final DbConnection dbcon,
            final ControlMaster cm,
            final Properties properties,
            final KenjaParameters kenjaParams
    ) throws Exception {
        try {
            log.info(">>>> マスタ読み込み開始");
            loader.load(category, cm, dbcon, properties, kenjaParams); // TODO: DaoInitLoader.java の Rev1.8.2.1 のコミットログを見よ
            log.info("<<<< マスタ読み込み終了");
        } catch (final SQLException e) {
            log.fatal("マスタ読み込みで例外発生", e);
            throw e;
        }
    }
} // AbstractAccumulateSummaryBatch

// eof
