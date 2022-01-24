// kanji=����
/*
 * $Id: MkCourse.java 56574 2017-10-22 11:21:06Z maeshiro $
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

/**
 * �R�[�X�}�X�^�f�[�^�B
 * @author takaesu
 * @version $Id: MkCourse.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class MkCourse extends Mk {
    private final static String _FILE = "MK_COURSE.csv";

    public MkCourse(final DB2UDB db, final Param param, final String title) throws SQLException {
        super(db, param, title);

        final List list = new ArrayList();
        setHead(list);

        // DB�����荞��
        ResultSet rs = null;
        try {
            _db.query(getSql());
            rs = _db.getResultSet();
            while(rs.next()) {
                final String studentDiv = rs.getString("student_div");  // �R�[�X�R�[�h�Əo�͏��͓���
                final String name = rs.getString("name");   // �R�[�X���ƃR�[�X�����͓���
                final String[] fields = {
                        param.getSchoolDiv(),
                        "1",
                        "01",
                        "00",   // ��U�R�[�h
                        studentDiv,
                        name,
                        name,
                        studentDiv,
                        cutDateDelimit(param.getUpdate()),
                };
                list.add(fields);
            }
        } catch (final SQLException e) {
            log.fatal("�R�[�X�}�X�^�̏��擾�ŃG���[");
            throw e;
        } finally {
            _db.commit();
            DbUtils.closeQuietly(null, null, rs);
        }

        // CSV�t�@�C���ɏ���
        toCsv("�R�[�X�}�X�^", _FILE, list);
    }

    private String getSql() {
        final String sql;
        sql = "SELECT"
            + "  student_div,"
            + "  name"
            + " FROM"
            + "  studentdiv_mst"
            ;
        return sql;
    }

    void setHead(List list) {
        final String[] header = {
                "�w�Z�敪",
                "�ے��R�[�h",
                "�w�ȃR�[�h",
                "��U�R�[�h",
                "�R�[�X�R�[�h",
                "�R�[�X��",
                "�R�[�X����",
                "�o�͏���",
                "�X�V��",
        };
        list.add(header);
    }
} // MkStaffBelonging

// eof
