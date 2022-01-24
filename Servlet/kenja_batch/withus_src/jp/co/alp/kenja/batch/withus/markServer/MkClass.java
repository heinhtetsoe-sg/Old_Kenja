// kanji=����
/*
 * $Id: MkClass.java 56574 2017-10-22 11:21:06Z maeshiro $
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
 * �N���X�}�X�^�f�[�^�B
 * @author takaesu
 * @version $Id: MkClass.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class MkClass extends Mk {
    private final static String _FILE = "MK_CLASS.csv";

    public MkClass(final DB2UDB db, final Param param, final String title) throws SQLException {
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
                        rs.getString("grade"),
                        rs.getString("hr_class"),
                        rs.getString("hr_name"),
                        rs.getString("hr_name"),
                        cutDateDelimit(param.getUpdate()),
                };
                list.add(fields);
            }
        } catch (final SQLException e) {
            log.fatal("�N���X�̏��擾�ŃG���[");
            throw e;
        } finally {
            _db.commit();
            DbUtils.closeQuietly(null, null, rs);
        }

        // CSV�t�@�C���ɏ���
        toCsv("�N���X", _FILE, list);
    }
    
    void setHead(List list) {
        final String[] header = {
                "�w�Z�敪",
                "�w�K���_�R�[�h",
                "�N���X�R�[�h",
                "�N���X��",
                "�N���X����",
                "�X�V��",
        };
        list.add(header);
    }

    private String getSql() {
        final String sql;
        sql = "SELECT"
            + "  t1.grade,"
            + "  SUBSTR(t1.hr_class,2,2) AS hr_class,"
            + "  t1.hr_name"
            + " FROM"
            + "  schreg_regd_hdat t1"
            + " WHERE"
            + "  t1.year='" + _param.getYear() + "' AND"
            + "  t1.semester='" + _param.getSemester() + "'"
            ;
        return sql;
    }
} // MkClass

// eof
