// kanji=����
/*
 * $Id: MkKyoka.java 56574 2017-10-22 11:21:06Z maeshiro $
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

import nao_package.db.DB2UDB;

import org.apache.commons.dbutils.DbUtils;

import jp.co.alp.kenja.batch.withus.Curriculum;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

/**
 * ���ȃ}�X�^�f�[�^�B
 * @author takaesu
 * @version $Id: MkKyoka.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class MkKyoka extends Mk {
    private final static String _FILE = "MK_KYOKA.csv";

    public MkKyoka(final DB2UDB db, final Param param, final String title) throws SQLException {
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
                        Curriculum.getCurriculumFirstYear(_param.getYear()),
                        rs.getString("classcd"),
                        rs.getString("classname"),
                        rs.getString("classabbv"),
                        rs.getString("showorder"),
                        cutDateDelimit(param.getUpdate()),
                };
                list.add(fields);
            }
        } catch (final SQLException e) {
            log.fatal("���Ȃ̏��擾�ŃG���[");
            throw e;
        } finally {
            _db.commit();
            DbUtils.closeQuietly(null, null, rs);
        }

        // CSV�t�@�C���ɏ���
        toCsv("����", _FILE, list);
    }

    void setHead(final List list) {
        final String[] header = {
                "�w�Z�敪",
                "����ے��K�p�N�x�R�[�h",
                "���ȃR�[�h",
                "���Ȗ�",
                "���ȗ���",
                "�o�͏���",
                "�X�V��",
        };
        list.add(header);
    }
    
    private String getSql() {
        final String sql;
        sql = "SELECT"
            + "  t1.classcd,"
            + "  t1.classname,"
            + "  t1.classabbv,"
            + "  value(t1.showorder, 0) AS showorder"
            + " FROM"
            + "  v_class_mst t1"
            + " WHERE"
            + "  t1.year='" + _param.getYear() + "' AND"
            + "  t1.inout_div='0'" // ���Z���O�敪: 0=���Z��, 1=���Z�O
           ;
        return sql;
    }
} // MkKyoka

// eof
