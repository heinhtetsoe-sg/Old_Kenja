// kanji=����
/*
 * $Id: MkSeitoRishuKamoku.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2008/03/24 16:23:35 - JST
 * �쐬��: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.markServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.DB2UDB;

import org.apache.commons.dbutils.DbUtils;

import jp.co.alp.kenja.batch.withus.Curriculum;

/**
 * ���k���C�Ȗڃf�[�^�B
 * @author takaesu
 * @version $Id: MkSeitoRishuKamoku.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class MkSeitoRishuKamoku extends Mk {
    private final static String _FILE = "MK_SEITO_RISHU_KAMOKU.csv";
    private final static String _���C���ԃR�[�h = "1";
    private final static String _�ۑ背�x�� = "0";

    /** �w�ЍݐЂ̉ے��R�[�h */
    final Map _studentCoursecd = new HashMap();

    public MkSeitoRishuKamoku(final DB2UDB db, final Param param, final String title) throws SQLException {
        super(db, param, title);

        final List list = new ArrayList();

        // �w�b�_��ݒ�
        setHead(list);

        loadStudentCoursecd();

        // DB�����荞��
        ResultSet rs = null;
        try {
            _db.query(getSql());
            rs = _db.getResultSet();
            while(rs.next()) {
                final String schregno = rs.getString("schregno");
                final String coursecd = (String) _studentCoursecd.get(schregno); 
                if (null == coursecd) {
                    log.warn("���k�̉ے��R�[�h���擾�o���Ȃ��B:" + schregno);
                    continue;
                }
                final String[] fields = {
                        param.getSchoolDiv(),
                        schregno,
                        coursecd,
                        Curriculum.getCurriculumYear(rs.getString("curriculum_cd")), 
                        rs.getString("year"),
                        rs.getString("classcd"),
                        rs.getString("subclasscd"),
                        _���C���ԃR�[�h,
                        _�ۑ背�x��,
                        cutDateDelimit(param.getUpdate()),
                };
                list.add(fields);
            }
        } catch (final SQLException e) {
            log.fatal("���k���C�Ȗڂ̏��擾�ŃG���[");
            throw e;
        } finally {
            _db.commit();
            DbUtils.closeQuietly(null, null, rs);
        }

        // CSV�t�@�C���ɏ���
        toCsv("���k���C�Ȗ�", _FILE, list);
    }
    
    private void loadStudentCoursecd() throws SQLException {
        ResultSet rs = null;
        try {
            _db.query("SELECT DISTINCT schregno, coursecd FROM schreg_regd_dat WHERE year='" + _param.getYear() + "'");
            rs = _db.getResultSet();
            while (rs.next()) {
                final String schregno = rs.getString("schregno");
                final String coursecd = rs.getString("coursecd");
                _studentCoursecd.put(schregno, coursecd);
            }
        } catch (final SQLException e) {
            log.fatal("���k�̉ے��R�[�h�擾�ŃG���[");
            throw e;
        } finally {
            DbUtils.closeQuietly(null, null, rs);
        }
    }

    void setHead(List list) {
        final String[] header = {
                "�w�Z�敪",
                "�w�Дԍ�",
                "�ے��R�[�h",
                "����ے��K�p�N�x�R�[�h",
                "�N�x�R�[�h",
                "���ȃR�[�h",
                "�ȖڃR�[�h",
                "���C���ԃR�[�h",
                "�ۑ背�x��",
                "�X�V��",
        };
        list.add(header);
    }

    private String getSql() {
        final String sql;
        sql = "SELECT"
            + "  schregno,"
            + "  curriculum_cd,"
            + "  year,"
            + "  classcd,"
            + "  SUBSTR(subclasscd,3,4) AS subclasscd"
            + " FROM"
            + "  comp_regist_dat"
            + " WHERE"
            + "  year='" + _param.getYear() + "'"
            ;
        return sql;
    }
} // MkSeitoRishuKamoku

// eof
