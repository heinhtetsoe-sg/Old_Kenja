// kanji=����
/*
 * $Id: MkKyousyokuin.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2008/04/04 11:56:45 - JST
 * �쐬��: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.nbi.groupware;

import java.util.ArrayList;
import java.util.List;

import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.DB2UDB;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ���E��CSV�B
 * @author takaesu
 * @version $Id: MkKyousyokuin.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class MkKyousyokuin extends Mk {

    /*pkg*/static final Log log = LogFactory.getLog(MkKyousyokuin.class);

    private final static String _FILE = "group0101.csv";

    public MkKyousyokuin(final DB2UDB db, final Param param, final String title) throws SQLException {
        super(db, param, title);

        final List list = new ArrayList();

        // DB�����荞��
        ResultSet rs = null;
        try {
            _db.query(getSql());
            rs = _db.getResultSet();
            while(rs.next()) {
                final String[] fields = {
                        param.getSchoolDiv(),
                        param.getYear(),
                        rs.getString("staffname"),
                        rs.getString("staffname_kana"),
                        getStaffCd(rs.getString("staffcd")),
                        rs.getString("jobcd"),
                };
                list.add(fields);
            }
        } catch (final SQLException e) {
            log.fatal("���E���̏��擾�ŃG���[");
            throw e;
        } finally {
            _db.commit();
            DbUtils.closeQuietly(null, null, rs);
        }
        log.info("���E���̃��R�[�h��=" + list.size());

        // CSV�t�@�C���ɏ���
        toCsv("���E��", _FILE, list);
    }

    private String getSql() {
        final String sql;
        sql = "SELECT"
            + "  staffname,"
            + "  staffname_kana,"
            + "  staffcd,"
            + "  t1.jobcd"  // TAKAESU: ���Q�����u�O�O�v�Œ�ŁA���Q�����O���[�v�E�F�A�Ŏg�p���Ă���E��R�[�h�ɂ��Ă��������͂��ł����A�u�O�P�O�O�v�u�O�Q�O�O�v�u�O�Q�P�O�v�̂悤�ȂR���̐������قƂ�ǂł��B
            + " FROM"
            + "  v_staff_mst t1 INNER JOIN job_ydat t2 ON t1.year=t2.year AND t1.jobcd=t2.jobcd"
            + " WHERE"
            + "  t1.year='" + _param.getYear() + "'"
            ;
        return sql;
    }

    private static String getStaffCd(final String staffCd) {
        if (null != staffCd && staffCd.length() >= 1) {
            if ('0' != staffCd.charAt(0)) {
                log.warn("�E���R�[�h�̓�1�����[���ȊO�B:" + staffCd);
            }
        }
        return convStaffCd(staffCd);
    }
} // MkKyousyokuin

// eof
