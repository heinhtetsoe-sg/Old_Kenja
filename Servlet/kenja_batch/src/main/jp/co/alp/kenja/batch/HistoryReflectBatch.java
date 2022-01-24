/*
 * $Id: HistoryReflectBatch.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2015/06/11
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import jp.co.alp.kenja.batch.accumulate.option.AccumulateOptions;
import jp.co.alp.kenja.batch.reflectHistory.ReflectHistoryContext;
import jp.co.alp.kenja.batch.reflectHistory.ReflectHistoryParameters;
import jp.co.alp.kenja.batch.reflectHistory.dao.DaoGuarantorHistDat;
import jp.co.alp.kenja.batch.reflectHistory.dao.DaoGuardianHistDat;
import jp.co.alp.kenja.batch.reflectHistory.dao.DaoSchregBaseHistDat;
import jp.co.alp.kenja.batch.reflectHistory.dao.QueryUtils;
import jp.co.alp.kenja.batch.reflectHistory.dao.ReflectHistoryQueryUpdate;
import jp.co.alp.kenja.common.dao.DbConnection;
import jp.co.alp.kenja.common.dao.query.DaoSemester;

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class HistoryReflectBatch extends AbstractKenjaBatch {

    private static final Log log = LogFactory.getLog(HistoryReflectBatch.class);

    private static final String PROPSNAME = "HistoryReflectBatch.properties";

    public HistoryReflectBatch(final ReflectHistoryContext ctx) {
        super(ctx);
    }

    /**
     * {@inheritDoc}
     */
    public void batch(final DbConnection dbcon) throws SQLException {
        final ReflectHistoryContext ctx = (ReflectHistoryContext) getContext();
        DaoSemester.getInstance().load(dbcon.getROConnection(), ctx.getControlMaster());
        final List<ReflectHistoryQueryUpdate> daoList = getDaoHistoryList();

        for (final ReflectHistoryQueryUpdate dao : daoList) {
            final Collection<Map<String, String>> dataList = dao.query(dbcon, ctx);
            QueryUtils.update(dbcon, ctx, dao, dataList);
        }
    }

    public List<ReflectHistoryQueryUpdate> getDaoHistoryList() {
        final List<ReflectHistoryQueryUpdate> daoList = new ArrayList<ReflectHistoryQueryUpdate>();
        daoList.add(new DaoSchregBaseHistDat());
        daoList.add(new DaoGuardianHistDat());
        daoList.add(new DaoGuarantorHistDat());
        return daoList;
    }

    public static void main(String[] args) throws SQLException {
        final StopWatch sw = new StopWatch();
        sw.start();
        log.fatal("START");

        final Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(PROPSNAME));
        } catch (final FileNotFoundException e) {
            log.error("FileNotFoundException", e);
        } catch (final IOException e) {
            log.error("IOException", e);
        }

        final ReflectHistoryParameters parameters = new ReflectHistoryParameters(args, properties);
        if (parameters.doRun()) {
            final ReflectHistoryContext ctx = new ReflectHistoryContext(parameters);

            final HistoryReflectBatch batch = new HistoryReflectBatch(ctx);
            batch.invoke();
        } else {
            log.fatal("処理しません。" + AccumulateOptions.DO_RUN + " is false. See [" + PROPSNAME + "] file");
        }
        log.fatal("Done!: " + sw);
    }
}
