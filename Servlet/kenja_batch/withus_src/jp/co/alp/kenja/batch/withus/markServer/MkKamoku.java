// kanji=����
/*
 * $Id: MkKamoku.java 56574 2017-10-22 11:21:06Z maeshiro $
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
import org.apache.commons.lang.StringUtils;

import jp.co.alp.kenja.batch.withus.Curriculum;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

/**
 * �Ȗڃ}�X�^�f�[�^�B
 * @author takaesu
 * @version $Id: MkKamoku.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class MkKamoku extends Mk {
    private final static String _FILE = "MK_KAMOKU.csv";

    public MkKamoku(final DB2UDB db, final Param param, final String title) throws SQLException {
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
                final String showOrder = rs.getString("showorder");
                final String[] fields = {
                        param.getSchoolDiv(),
                        Curriculum.getCurriculumYear(rs.getString("curriculum_cd")), 
                        rs.getString("classcd"),
                        rs.getString("subclasscd"),
                        rs.getString("subclassname"),
                        rs.getString("subclassabbv"),
                        StringUtils.isEmpty(showOrder) ? "0" : showOrder,
                        cutDateDelimit(param.getUpdate()),
                };
                list.add(fields);
            }
        } catch (final SQLException e) {
            log.fatal("�Ȗڂ̏��擾�ŃG���[");
            throw e;
        } finally {
            _db.commit();
            DbUtils.closeQuietly(null, null, rs);
        }

        // CSV�t�@�C���ɏ���
        toCsv("�Ȗ�", _FILE, list);
    }
    
    void setHead(final List list) {
        final String[] header = {
                "�w�Z�敪",
                "����ے��K�p�N�x�R�[�h",
                "���ȃR�[�h",
                "�ȖڃR�[�h",
                "�Ȗږ�",
                "�Ȗڗ���",
                "�o�͏���",
                "�X�V��",
        };
        list.add(header);
    }

    private String getSql() {
        final String sql;
        sql = "SELECT"
            + "  t1.classcd,"
            + "  t1.curriculum_cd,"
            + "  SUBSTR(t1.subclasscd,3,4) AS subclasscd,"
            + "  t1.subclassname,"
            + "  t1.subclassabbv,"
            + "  value(t1.showorder, 0) AS showorder"
            + " FROM"
            + "  v_subclass_mst t1"
            + " WHERE"
            + "  t1.year='" + _param.getYear() + "' AND"
            + "  t1.inout_div='0'" // ���Z���O�敪: 0=���Z��, 1=���Z�O
            ;
        return sql;
    }
} // MkKamoku

// eof
