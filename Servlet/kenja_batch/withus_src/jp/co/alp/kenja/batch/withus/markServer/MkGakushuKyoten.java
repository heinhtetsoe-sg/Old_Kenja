// kanji=����
/*
 * $Id: MkGakushuKyoten.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2008/03/24 16:23:35 - JST
 * �쐬��: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.markServer;

import java.util.ArrayList;
import java.util.List;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.DbUtils;

import nao_package.db.DB2UDB;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

/**
 * �w�K���_�}�X�^�f�[�^�B
 * @author takaesu
 * @version $Id: MkGakushuKyoten.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class MkGakushuKyoten extends Mk {
    private final static String _FILE = "MK_GAKUSHU_KYOTEN.csv";

    public MkGakushuKyoten(final DB2UDB db, final Param param, final String title) throws SQLException {
        super(db, param, title);

        final List list = new ArrayList();

        // �w�b�_��ݒ�
        setHead(list);

        // DB�����荞��
        ResultSet rs = null;
        try {
            _db.query(getSql());
            rs = _db.getResultSet();
            while(rs.next()) {
                final String[] fields = {
                        param.getSchoolDiv(),
                        rs.getString("belonging_div"),
                        rs.getString("classification"),
                        rs.getString("schoolname1"),
                        rs.getString("schoolname3"),
                        rs.getString("order"),
                        cutDateDelimit(param.getUpdate()),
                };
                list.add(fields);
            }
        } catch (final SQLException e) {
            log.fatal("�w�K���_�̏��擾�ŃG���[");
            throw e;
        } finally {
            _db.commit();
            DbUtils.closeQuietly(null, null, rs);
        }

        // CSV�t�@�C���ɏ���
        toCsv("�w�K���_", _FILE, list);
    }
    
    void setHead(final List list) {
        final String[] header = {
                "�w�Z�敪",
                "�w�K���_�R�[�h",
                "�w�K���_��ʃR�[�h",
                "�w�K���_��",
                "�w�K���_����",
                "�o�͏���",
                "�X�V��",
        };
        list.add(header);
    }

    private String getSql() {
        final String sql;
        sql = "SELECT"
            + "  t1.belonging_div,"
            + "  CASE WHEN VALUE(t1.classification, '0') = '1' THEN '1' ELSE '2' END AS classification,"
            + "  t1.schoolname1,"
            + "  t1.schoolname3,"
            + "  t1.order"
            + " FROM"
            + "  belonging_mst t1"
            + " WHERE '" + _param.getDate() + "' BETWEEN t1.open_date AND VALUE(t1.close_date, '9999-12-31')"
            ;
        log.debug("sql=" + sql);
        return sql;
    }
} // MkGakushuKyoten

// eof
