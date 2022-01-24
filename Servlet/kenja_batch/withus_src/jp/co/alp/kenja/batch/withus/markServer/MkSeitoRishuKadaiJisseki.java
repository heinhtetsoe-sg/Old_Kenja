// kanji=����
/*
 * $Id: MkSeitoRishuKadaiJisseki.java 56574 2017-10-22 11:21:06Z maeshiro $
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
 * ���k���C�ۑ���уf�[�^�B
 * @author takaesu
 * @version $Id: MkSeitoRishuKadaiJisseki.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class MkSeitoRishuKadaiJisseki extends Mk {
    private final static String _FILE = "MK_SEITO_RISHU_KADAI_JISSEKI.csv";

    public MkSeitoRishuKadaiJisseki(final DB2UDB db, final Param param, final String title) throws SQLException {
        super(db, param, title);

        final List list = new ArrayList();
        
        // �w�b�_��ݒ�
        setHead(list);

        // DB�����荞��
        getScoreFromTest(param, list);
        getScoreFromReport(param, list);

        // CSV�t�@�C���ɏ���
        toCsv("���k���C�ۑ����", _FILE, list);
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
                "���C�ۑ��ʃR�[�h",
                "���{�ԍ�",
                "�ۑ背�x��",
                "��o�N����",
                "���_",
                "���񓾓_",
                "�ǎ����_",
                "��o��",
                "�E���R�[�h",
                "�X�V��",
        };
        list.add(header);
    }
    
    private void getScoreFromReport(final Param param, final List list) throws SQLException {
        ResultSet rs = null;
        try {
            final String sql = getSqlReport();
            log.debug("sql=" + sql);
            _db.query(sql);
            rs = _db.getResultSet();
            while(rs.next()) {
                final String score1 = rs.getString("commited_score1");  // ���񓾓_
                final String score2 = rs.getString("commited_score2");  // �ǎ����_
                final String count = (null == score2) ? "01" : "02";    // ��o��
                final String score = getScore(score1, score2);  // ���_
                
                final String[] fields = {
                        param.getSchoolDiv(),
                        rs.getString("schregno"),
                        "1",    // TODO: �w�ЍݐЃf�[�^�̉ے��R�[�h�icoursecd)���Z�b�g
                        Curriculum.getCurriculumYear(rs.getString("curriculum_cd")), 
                        _param.getYear(),
                        rs.getString("classcd"),
                        cutSubclassCd(rs.getString("subclasscd")),
                        "1",    // ���C���ԃR�[�h: 1=�ʔN
                        "1",    // ���C�ۑ��ʃR�[�h: 1=���|�[�g, 2=�e�X�g
                        rs.getString("report_seq"),
                        "0", // �ۑ背�x��
                        cutDateDelimit2(rs.getString("commited_date")),   // ��o�N����
                        score,
                        score1,
                        score2,
                        count,
                        convStaffCd(rs.getString("registercd")),
                        cutDateDelimit(param.getUpdate()),
                };
                list.add(fields);
            }
        } catch (final SQLException e) {
            log.fatal("���k���C�ۑ����(���|�[�g)�̏��擾�ŃG���[");
            throw e;
        } finally {
            _db.commit();
            DbUtils.closeQuietly(null, null, rs);
        }
        log.info("���k���C�ۑ����(�e�X�g+���|�[�g)�̃��R�[�h��=" + list.size());
    }

    public static String cutDateDelimit2(final String dateStr) {
        if (null == dateStr) {
            return null;
        }
        final String rtn = dateStr.substring(0, 4) + dateStr.substring(5, 7) + dateStr.substring(8, 10);
        return rtn;
    }

    /**
     * ���_�𓾂�B
     * @param score1 ���񓾓_
     * @param score2 �ǎ����_
     * @return �ǎ�������ꍇ�A���_�͍ō���30�D����������ꍇ�A�ǂ��炩�傫�����B<br>�ǎ��Ȃ���Ώ��񓾓_
     */
    public String getScore(final String score1, final String score2) {
        if (null == score2) {
            return score1;
        }

        final int value2 = Integer.valueOf(score2).intValue();
        if (value2 >= 30) {
            return "30";
        }

        final int value1 = Integer.valueOf(score1).intValue();
        
        return String.valueOf(Math.max(value1, value2));
    }

    private String getSqlReport() {
        final String sql;
        sql = "SELECT"
            + "  schregno,"
            + "  curriculum_cd,"
            + "  classcd,"
            + "  subclasscd,"
            + "  report_seq,"
            + "  CASE WHEN commited_date1 >= commited_date2 THEN commited_date1"
            + "       WHEN commited_date1 < commited_date2 THEN commited_date2"
            + "       ELSE commited_date1 END as commited_date,"
            + "  commited_score1,"
            + "  commited_score2,"
            + "  registercd"
            + " FROM"
            + "  rec_report_dat"
            + " WHERE"
            + "  commited_score1 is not null AND"
            + "  year='" + _param.getYear() + "'"
            ;
        return sql;
    }

    private void getScoreFromTest(final Param param, final List list) throws SQLException {
        ResultSet rs = null;
        try {
            _db.query(getSqlTest());
            rs = _db.getResultSet();
            while(rs.next()) {
                final String[] fields = {
                        param.getSchoolDiv(),
                        rs.getString("schregno"),
                        "1",    // TODO: �w�ЍݐЃf�[�^�̉ے��R�[�h�icoursecd)���Z�b�g
                        Curriculum.getCurriculumYear(rs.getString("curriculum_cd")), 
                        _param.getYear(),
                        rs.getString("classcd"),
                        cutSubclassCd(rs.getString("subclasscd")),
                        "1",    // ���C���ԃR�[�h: 1=�ʔN
                        "2",    // ���C�ۑ��ʃR�[�h: 1=���|�[�g, 2=�e�X�g
                        getTestCode(rs.getString("month")),
                        "0", // �ۑ背�x��
                        cutDateDelimit2(rs.getString("updated")),   // ��o�N����
                        rs.getString("score"),  // ���_
                        rs.getString("score"),  // ���񓾓_
                        "", // �ǎ����_
                        "01", // ��o��
                        convStaffCd(rs.getString("registercd")),
                        cutDateDelimit(param.getUpdate()),
                };
                list.add(fields);
            }
        } catch (final SQLException e) {
            log.fatal("���k���C�ۑ����(�e�X�g)�̏��擾�ŃG���[");
            throw e;
        } finally {
            _db.commit();
            DbUtils.closeQuietly(null, null, rs);
        }
        log.info("���k���C�ۑ����(�e�X�g)�̃��R�[�h��=" + list.size());
    }

    private String getSqlTest() {
        final String sql;
        sql = "SELECT"
            + "  schregno,"
            + "  curriculum_cd,"
            + "  classcd,"
            + "  subclasscd,"
            + "  month,"
            + "  updated,"
            + "  score,"
            + "  registercd"
            + " FROM"
            + "  rec_test_dat"
            + " WHERE"
            + "  score is not null AND"
            + "  year='" + _param.getYear() + "'"
            ;
        return sql;
    }

    private static String getTestCode(final String month) {
        // 9��?
        if ("09".equals(month)) {
            return "1";
        }
        // 3��?
        if ("03".equals(month)) {
            return "2";
        }
        log.warn("�e�X�g�őz��O�̕s���Ȍ�=" + month);
        return "?";
    }
} // MkSeitoRishuKadaiJisseki

// eof
