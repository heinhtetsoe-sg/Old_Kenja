// kanji=����
/*
 * $Id: MkKinmusakiGakushuKyoten.java 56574 2017-10-22 11:21:06Z maeshiro $
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
import org.apache.commons.lang.StringUtils;

import nao_package.db.DB2UDB;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

/**
 * �Ζ���w�K���_�}�X�^�f�[�^�B
 * @author takaesu
 * @version $Id: MkKinmusakiGakushuKyoten.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class MkKinmusakiGakushuKyoten extends Mk {
    private final static String _FILE = "MK_KINMUSAKI_GAKUSHU_KYOTEN.csv";

    public MkKinmusakiGakushuKyoten(final DB2UDB db, final Param param, final String title) throws SQLException {
        super(db, param, title);

        final List list = new ArrayList();

        // �w�b�_��ݒ�
        setHead(list);
        
        final String inaugurationStart = _param.getYear() + "0401"; // ���̔N�x��4��1��

        final int nextYear = Integer.parseInt(_param.getYear()) + 1;
        final String inaugurationEnd = String.valueOf(nextYear) + "0331";   // ���N�x��3��31��

        // DB�����荞��
        ResultSet rs = null;
        try {
            _db.query(getSql());
            rs = _db.getResultSet();
            while(rs.next()) {
                final String belongingDiv = rs.getString("belonging_div");
                final String[] fields = {
                        param.getSchoolDiv(),
                        convStaffCd(rs.getString("staffcd")),
                        StringUtils.isEmpty(belongingDiv) ? "001" : belongingDiv,
                        inaugurationStart,
                        inaugurationEnd,
                        cutDateDelimit(param.getUpdate()),
                };
                list.add(fields);
            }
        } catch (final SQLException e) {
            log.fatal("�Ζ���w�K���_�̏��擾�ŃG���[");
            throw e;
        } finally {
            _db.commit();
            DbUtils.closeQuietly(null, null, rs);
        }

        // CSV�t�@�C���ɏ���
        toCsv("�Ζ���w�K���_", _FILE, list);
    }

    void setHead(final List list) {
        final String[] header = {
                "�w�Z�敪",
                "�E���R�[�h",
                "�w�K���_�R�[�h",
                "�A�C�N����",
                "�ޔC�N����",
                "�X�V��",
        };
        list.add(header);
    }
    
    private String getSql() {
        final String sql;
        sql = "SELECT"
            + "  staffcd,"
            + "  belonging_div"
            + " FROM"
            + "  v_staff_mst"
            + " WHERE"
            + "  year='" + _param.getYear() + "'"
            + " ORDER BY belonging_div, staffcd"
            ;
        return sql;
    }
} // MkStaffBelonging

// eof
