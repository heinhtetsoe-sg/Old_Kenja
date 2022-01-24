// kanji=漢字
/*
 * $Id: DaoInitLoaderAccumulateBatch.java 74567 2020-05-27 13:21:04Z maeshiro $
 *
 * 作成日: 2006/12/13 14:29:46 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.dao.loader;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.dbutils.DbUtils;

import jp.co.alp.kenja.batch.accumulate.option.AccumulateOptions;
import jp.co.alp.kenja.batch.dao.DaoAccumulateKintaiInfo;
import jp.co.alp.kenja.batch.dao.DaoBatchSchoolMaster;
import jp.co.alp.kenja.batch.dao.DaoCourseMst;
import jp.co.alp.kenja.batch.dao.DaoEntDate;
import jp.co.alp.kenja.common.dao.DbConnection;
import jp.co.alp.kenja.common.dao.query.DaoChairHomeRoom;
import jp.co.alp.kenja.common.dao.query.DaoExamItem;
import jp.co.alp.kenja.common.dao.query.DaoGroupClass;
import jp.co.alp.kenja.common.dao.query.DaoHomeRoom;
import jp.co.alp.kenja.common.dao.query.DaoInitLoader;
import jp.co.alp.kenja.common.dao.query.DaoKintai;
import jp.co.alp.kenja.common.dao.query.DaoLoadersWithCurriculum;
import jp.co.alp.kenja.common.dao.query.DaoPeriod;
import jp.co.alp.kenja.common.dao.query.DaoSemester;
import jp.co.alp.kenja.common.dao.query.DaoStudent;
import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.lang.enums.MyEnum.Category;
import jp.co.alp.kenja.common.util.KenjaParameters;

/**
 * 累積バッチのDB初期読み込み。
 * @author takaesu
 * @version $Id: DaoInitLoaderAccumulateBatch.java 74567 2020-05-27 13:21:04Z maeshiro $
 */
public class DaoInitLoaderAccumulateBatch implements DaoInitLoader {

    private AccumulateOptions _options;

    /**
     * コンストラクタ。
     */
    public DaoInitLoaderAccumulateBatch(final AccumulateOptions options) {
        super();
        _options = options;
    }

    /**
     * {@inheritDoc}
     */
    public void load(
            final Category category,
            final ControlMaster cm,
            final DbConnection dbcon,
            final Properties prop,
            final KenjaParameters params
    ) throws SQLException {
        Connection conn = null;
        try {
            conn = dbcon.getROConnection();
            final DaoLoadersWithCurriculum.Loaders loaders = new DaoLoadersWithCurriculum().getLoaders(params);
            DaoBatchSchoolMaster.getInstance(_options).load(conn, cm);

            DaoSemester.getInstance().load(conn, cm);
            DaoPeriod.getInstance().load(conn, cm);
            loaders.getDaoSubClassInstance().load(conn, cm);
            DaoGroupClass.getInstance().load(conn, cm);
            DaoHomeRoom.getInstance().load(conn, cm);
            loaders.getDaoChairInstance().load(conn, cm);
            DaoChairHomeRoom.getInstance().load(conn, cm);
            DaoKintai.getInstance().load(conn, cm);
            DaoAccumulateKintaiInfo.getInstance().load(conn, cm);
            DaoStudent.getInstance(_options.getSchoolKind()).load(conn, cm);
            DaoEntDate.getInstance().load(conn, cm);
            DaoCourseMst.getInstance().load(conn, cm);
            DaoExamItem.getInstance(prop).load(conn, cm);
        } finally {
            DbUtils.commitAndCloseQuietly(conn);
        }
    }
} // DaoInitLoaderAccumulateBatch

// eof
