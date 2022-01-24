// kanji=����
/*
 * $Id: MkSchRegBaseMst.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2008/05/01 0:20:23 - JST
 * �쐬��: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.vqsServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.Database;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ���k���e�[�u���B
 * @author takaesu
 * @version $Id: MkSchRegBaseMst.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class MkSchRegBaseMst extends Mk {
    /*pkg*/static final Log log = LogFactory.getLog(MkSchRegBaseMst.class);

    /** �ے��}�X�^ */
    private Map _courseMst;
    /** �w�ȃ}�X�^ */
    private Map _majorMst;
    /** �R�[�X�}�X�^ */
    private Map _courseCodeMst;

    /** �w���敪�}�X�^ */
    private Map _studentDivMst;

    /** �����}�X�^ */
    private Map _belongingMst;

    /** ���k�̓s���{���R�[�h */
    private Map _studentAddress;

    public MkSchRegBaseMst(final Param param, final Database knj, final Database vqs, final String title) throws SQLException {
        super(param, knj, vqs, title);

        loadMasterData();

        final List list = loadData(knj);
        log.debug("����:�f�[�^��=" + list.size());

        saveVqs(list, vqs);
    }

    private void saveVqs(final List list, final Database vqs) throws SQLException {
        int count = 0;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final String[] data = (String[]) it.next();
            final String insertSql = createInsertSql();
            try {
                count += _runner.update(vqs.conn, insertSql, data);
            } catch (final SQLException e) {
                final int errorCode = e.getErrorCode();
                final String state = e.getSQLState();
                if (0 == errorCode && UNIQUE_VIOLATION.equals(state)) {
                    vqs.conn.rollback();    // ��

                    final String schregno = data[0];
                    final String updateSql = "UPDATE schreg_base_mst SET updated=current_timestamp WHERE schregno='" + schregno + "'";
                    try {
                        count += _runner.update(vqs.conn, updateSql);
                    } catch (final SQLException e1) {
                        log.error("VQS�ɍX�V�ŃG���[:" + insertSql);
                        throw e;
                    }
                    vqs.commit();   // TODO: ���t�@�N�^����! ���� rollback ���Ă��邹�����I
                } else {
                    log.error("VQS�ɑ}���ŃG���[:" + insertSql);
                    throw e;
                }
            }
        }
        vqs.commit();
        log.debug("VQS:�f�[�^��=" + count);
    }

    private String createInsertSql() {
        final String sql;

        sql = "INSERT INTO schreg_base_mst VALUES"
            + "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, current_timestamp)";

        return sql;
    }

    private List loadData(final Database knj) throws SQLException {
        final List rtn = new ArrayList();
        ResultSet rs = null;
        try {
            knj.query(sql());
            rs = knj.getResultSet();
            while (rs.next()) {
                final String schregno = rs.getString("schregno");
                final String prefCd = (String) _studentAddress.get(schregno);

                final String coursecd = rs.getString("coursecd");
                final String majorcd = rs.getString("majorcd");
                final String coursecode = rs.getString("coursecode");
                final String studentDiv = rs.getString("student_div");
                final String courseDiv = rs.getString("course_div");
                final String studentDivName = (String) _studentDivMst.get(courseDiv + studentDiv);

                final String belonging = rs.getString("grade");
                final String grdDate = rs.getString("grd_date");

                final String[] fields = {
                        schregno,
                        rs.getString("name"),
                        rs.getString("name_show"),
                        rs.getString("name_kana"),
                        rs.getString("sex"),
                        prefCd, // �s���{���R�[�h
                        coursecd,
                        (String) _courseMst.get(coursecd),  // �ے���
                        majorcd == null ? EMPTY : majorcd.substring(1),
                        (String) _majorMst.get(coursecd + majorcd), // �w�Ȗ�
                        coursecode == null ? EMPTY : coursecode.substring(2),    // TAKAESU: 4byte?
                        (String) _courseCodeMst.get(coursecode),    // �R�[�X��
                        studentDiv == null ? EMPTY : studentDiv.substring(1),    // �w���敪 // TODO: �e�[�u����� 1�o�C�g?
                        studentDivName, // �w���敪��
                        belonging,  // ����
                        (String) _belongingMst.get(belonging),  // ������
                        rs.getString("annual"), // �N��(postgre���� integer�^����insert�o����)
                        grdDate == null ? "INFINITY" : grdDate,    // ���Г��t    // TAKAESU: postgre�̗p��Ainfinity �������Ŏg���Ă͂����Ȃ�
                };
                rtn.add(nullToEmpty(fields));
            }
        } catch (final SQLException e) {
            log.error("���k���e�[�u���ŃG���[", e);
            throw e;
        } finally {
            knj.commit();
            DbUtils.closeQuietly(null, null, rs);
        }
        return rtn;
    }

    private void loadMasterData() throws SQLException {
        _studentAddress = loadStudentAddress();

        // �ے��A�w�ȁA�R�[�X
        _courseMst = DbToMap(_knj, "coursecd", "courseabbv", "SELECT coursecd, courseabbv FROM course_mst");
        _majorMst = DbToMap(_knj, "cd", "majorabbv", "SELECT coursecd || majorcd as cd, majorabbv FROM major_mst");
        _courseCodeMst = DbToMap(_knj, "coursecode", "coursecodename", "SELECT coursecode, coursecodename FROM coursecode_mst");

        // �w���敪
        _studentDivMst = DbToMap(_knj, "cd", "name", "SELECT course_div || student_div as cd, name FROM studentdiv_mst");

        // ����
        _belongingMst = DbToMap(_knj, "belonging_div", "schoolname3", "SELECT belonging_div, schoolname3 FROM belonging_mst");
    }

    private Map loadStudentAddress() throws SQLException {
        // TAKAESU: SQL���A�����ƊȌ��ɂ�����
        final String sql = "SELECT t2.schregno, t2.pref_cd FROM "
            + "(SELECT schregno, max(issuedate) as issuedate FROM schreg_address_dat GROUP BY schregno) t1 "
            + "INNER JOIN schreg_address_dat t2 ON t1.schregno=t2.schregno AND t1.issuedate=t2.issuedate"
            ;

        return DbToMap(_knj, "schregno", "pref_cd", sql);
    }

    public static Map DbToMap(final Database db, final String key, final String val, final String sql) throws SQLException {
        final Map rtn = new HashMap();

        ResultSet rs = null;
        db.query(sql);
        rs = db.getResultSet();
        while(rs.next()) {
            final String code = rs.getString(key);
            final String name = rs.getString(val);
            rtn.put(code, name);
        }

        return rtn;
    }

    private String sql() {
        final String sql;
        sql = "SELECT"
            + "  t1.schregno,"
            + "  t1.name,"
            + "  t1.name_show,"
            + "  t1.name_kana,"
            + "  t1.sex,"
            + "  t2.coursecd,"
            + "  t2.majorcd,"
            + "  t2.coursecode,"
            + "  t2.course_div,"
            + "  t2.student_div,"
            + "  t2.grade,"
            + "  t2.annual,"
            + "  t1.grd_date"
            + " FROM"
            + "  schreg_base_mst t1,"
            + "  schreg_regd_dat t2,"
            + "  schreg_regd_hdat t3"
            + " WHERE"
            + "  t1.schregno = t2.schregno AND"
            + "  t2.year = t3.year AND"
            + "  t2.semester = t3.semester AND"
            + "  t2.grade = t3.grade AND"
            + "  t2.hr_class = t3.hr_class AND"
            + "  t2.year='" + _param._year + "' AND"
            + "  t2.semester='" + _param._semester + "' AND"
            + "  (t2.student_div='03' OR t2.seat_col='1')"    // student_div="03:�̂ݐ�"
            ;
        return sql;
    }
} // MkSchRegBaseMst

// eof
