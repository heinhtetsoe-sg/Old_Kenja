// kanji=����
/*
 * $Id: MkGakkoKankeisha.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2008/03/22 12:08:51 - JST
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * �w�Z�֌W�҃}�X�^�f�[�^�B
 * @author takaesu
 * @version $Id: MkGakkoKankeisha.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class MkGakkoKankeisha extends Mk {
    /*pkg*/static final Log log = LogFactory.getLog(MkGakkoKankeisha.class);

    private final static String _FILE = "MK_GAKKO_KANKEISHA.csv";

    /** �w�Z�֌W�Ҏ�ʃR�[�h */
    private final static String SCHOOL_PERSONS_CODE = "00";

    public MkGakkoKankeisha(final DB2UDB db, final Param param, final String title) throws SQLException {
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
                        convStaffCd(rs.getString("staffcd")),
                        SCHOOL_PERSONS_CODE,
                        rs.getString("staffname"),
                        cutDateDelimit(param.getUpdate()),
                };
                list.add(fields);
            }
        } catch (final SQLException e) {
            log.fatal("�w�Z�֌W�҂̏��擾�ŃG���[");
            throw e;
        } finally {
            _db.commit();
            DbUtils.closeQuietly(null, null, rs);
        }

        // CSV�t�@�C���ɏ���
        toCsv("�w�Z�֌W��", _FILE, list);
    }

    void setHead(final List list) {
        final String[] header = {
                "�w�Z�敪",
                "�E���R�[�h",
                "�w�Z�֌W�Ҏ�ʃR�[�h",
                "����",
                "�X�V��",
        };
        list.add(header);
    }
    
    private String getSql() {
        final String sql;
        sql = "SELECT"
            + "  staffcd,"
            + "  staffname"
            + " FROM"
            + "  staff_mst"
            ;
        return sql;
    }
} // MkGakkoKankeisha

// eof
