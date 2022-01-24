/*
 * $Id: DaoEntDate.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2011/03/15
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.dao;

import java.util.Map;

import jp.co.alp.kenja.batch.accumulate.EntDate;
import jp.co.alp.kenja.common.dao.query.AbstractDaoLoader;
import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.Student;
import jp.co.alp.kenja.common.util.KenjaMapUtils;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * 入学日付を読み込む。
 * @author maesiro
 * @version $Id: DaoEntDate.java 74552 2020-05-27 04:41:22Z maeshiro $
 */
public final class DaoEntDate extends AbstractDaoLoader<EntDate> {

    /** T93/学籍基礎マスタ/テーブル名 */
    public static final String TABLE_NAME = "SCHREG_BASE_MST";

    private static final Log log = LogFactory.getLog(DaoEntDate.class);
    private static final DaoEntDate INSTANCE = new DaoEntDate();

    /*
     * コンストラクタ。
     */
    private DaoEntDate() {
        super(log);
    }

    /**
     * インスタンスを得る。
     * @return インスタンス
     */
    public static AbstractDaoLoader<EntDate> getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    public Object mapToInstance(final Map<String, Object> map) {

        final Student student = Student.getInstance(_cm.getCategory(), MapUtils.getString(map, "code"));
        if (null == student) {
            return null;
        }

        final EntDate entDate = EntDate.create(
                _cm.getCategory(),
                student.getCode(),
                KenjaMapUtils.getKenjaDateImpl(map, "entDate")
        );
        return entDate;
    }

    /**
     * {@inheritDoc}
     */
    public String getQuerySql() {
        final StringBuffer sql = new StringBuffer();
        sql.append("select");
        sql.append("    t1.SCHREGNO as code,");
        sql.append("    t1.ENT_DATE as entDate");
        sql.append("  from ").append(TABLE_NAME).append(" t1");
        return sql.toString();
    }

    /**
     * {@inheritDoc}
     */
    public Object[] getQueryParams(final ControlMaster cm) {
        return new Object[] {};
    }
}
